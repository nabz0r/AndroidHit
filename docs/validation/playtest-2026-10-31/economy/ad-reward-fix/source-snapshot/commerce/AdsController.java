package com.androidhit.lumadrift.commerce;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.google.android.libraries.ads.mobile.sdk.MobileAds;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.AdValue;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig;
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd;
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Adult-only, manual-load rewarded ads. No UMP request or ad SDK initialization
 * happens before the separate app opt-in. UMP permission is not a claim that a
 * user agreed to personalization. Every UI callback runs on the main thread.
 */
public final class AdsController {
    public enum AgeStatus { UNKNOWN, MINOR, ADULT }
    /** The same real earned callback fulfills one explicitly described reward. */
    public enum RewardPlacement { GOLD, CONTINUATION, POWER }
    /** A genuine earned event may still be refused when its item cannot be fulfilled. */
    static final class RewardUnavailableException extends RuntimeException {
        final String statusKey;
        RewardUnavailableException(String statusKey){this.statusKey=statusKey;}
    }
    public interface Listener {
        void onStatus(String statusKey);
        void onReward(String uniqueShowId, int gold);
        /** Device-local opt-in journal only; never forward to a remote analytics service. */
        void onEvent(String event, Map<String, String> fields);
    }

    private static final AtomicBoolean SDK_READY = new AtomicBoolean(false);
    private static final AtomicBoolean SDK_INITIALIZING = new AtomicBoolean(false);
    private final WeakReference<Activity> activity;
    private final Listener listener;
    private final SharedPreferences prefs;
    private final RewardPlacement rewardPlacement;
    private final String rewardItemId;
    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile AgeStatus age;
    private volatile boolean optedIn, consentReady, closed, localMetrics;
    private volatile long epoch;
    private ConsentInformation consent;
    private RewardedAd cached;
    private RewardedAd presenting;
    private boolean loading;
    private ShowSession activeShow;
    private String status = "ads_opt_in_required";

    private static final class ShowSession {
        final String id = "ad:" + UUID.randomUUID();
        final AtomicBoolean rewarded = new AtomicBoolean(false);
        final long epoch;
        boolean cancelled;
        ShowSession(long epoch) { this.epoch = epoch; }
    }

    public AdsController(Activity activity, Listener listener) {
        this(activity, listener, RewardPlacement.GOLD);
    }

    public AdsController(Activity activity, Listener listener, RewardPlacement placement) {
        this(activity,listener,placement,"");
    }

    public AdsController(Activity activity, Listener listener, RewardPlacement placement, String itemId) {
        if (activity == null || listener == null) throw new IllegalArgumentException("Activity and listener required");
        if (placement == null) throw new IllegalArgumentException("Reward placement required");
        if(placement==RewardPlacement.POWER&&(itemId==null||!itemId.matches("[a-z]{1,20}")))throw new IllegalArgumentException("Power reward requires an item");
        this.activity = new WeakReference<>(activity);
        this.listener = listener;
        rewardPlacement = placement;
        rewardItemId = itemId;
        prefs = activity.getApplicationContext().getSharedPreferences("luma_commerce_privacy_v1", Context.MODE_PRIVATE);
        AgeStatus stored;
        try { stored = AgeStatus.valueOf(prefs.getString("age_status", "UNKNOWN")); }
        catch (RuntimeException malformed) { stored = AgeStatus.UNKNOWN; }
        age = stored;
        optedIn = age == AgeStatus.ADULT && prefs.getBoolean("explicit_ads_opt_in", false);
    }

    public AgeStatus ageStatus() { return age; }
    public boolean isExplicitOptIn() { return optedIn; }
    public String statusKey() { return status; }
    public boolean isLoading() { return loading || SDK_INITIALIZING.get(); }
    public boolean isAdShowing() { return presenting != null || activeShow != null; }
    public boolean isRewardedReady() { return allowed() && consentReady && cached != null && activeShow == null; }
    public void setLocalMetricsEnabled(boolean enabled) { localMetrics = enabled; }

    /** Invoke once per app launch. It performs no network work while opted out. */
    public void onAppLaunch() {
        onMain(() -> { if (allowed()) refreshConsent(); else report(optedIn ? "adult_required" : "ads_opt_in_required"); });
    }

    public void setAgeStatus(AgeStatus value) {
        onMain(() -> {
            if (closed || value == null) return;
            if (value == age) return;
            discardAds(); consentReady = false;
            age = value;
            if (value != AgeStatus.ADULT) optedIn = false;
            boolean saved = prefs.edit().putString("age_status", value.name())
                    .putBoolean("explicit_ads_opt_in", optedIn).commit();
            if (!saved) { optedIn = false; report("commerce_save_error"); return; }
            report(value == AgeStatus.ADULT ? "ads_opt_in_required" : "adult_required");
        });
    }

    public void setExplicitOptIn(boolean enabled) {
        onMain(() -> {
            if (closed) return;
            discardAds(); consentReady = false;
            if (enabled && age != AgeStatus.ADULT) { report("adult_required"); return; }
            optedIn = enabled;
            if (!prefs.edit().putBoolean("explicit_ads_opt_in", enabled).commit()) {
                optedIn = false; report("commerce_save_error"); return;
            }
            if (enabled) refreshConsent(); else report("ads_opt_in_required");
        });
    }

    /** Refreshes UMP before this session can load ads. This never loads an ad. */
    public void refreshConsent() {
        onMain(() -> {
            if (!gate()) return;
            Activity owner = owner(); if (owner == null) return;
            discardAds(); consentReady = false;
            final long request = epoch;
            report("consent_loading");
            if (consent == null) consent = UserMessagingPlatform.getConsentInformation(owner);
            ConsentRequestParameters params = new ConsentRequestParameters.Builder()
                    .setTagForUnderAgeOfConsent(false).build();
            consent.requestConsentInfoUpdate(owner, params,
                    () -> onMain(() -> {
                        if (!current(request)) return;
                        Activity foreground = owner(); if (foreground == null) return;
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(foreground,
                                error -> onMain(() -> {
                                    if (current(request)) updateConsentGate(error == null ? "consent_ready" : "consent_error");
                                }));
                    }),
                    error -> onMain(() -> { if (current(request)) updateConsentGate("consent_error"); }));
        });
    }

    public boolean isPrivacyOptionsRequired() {
        return consent != null && consent.getPrivacyOptionsRequirementStatus()
                == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
    }

    public void showPrivacyOptions() {
        onMain(() -> {
            if (!gate()) return;
            if (!isPrivacyOptionsRequired()) { report("privacy_not_required"); return; }
            Activity owner = owner(); if (owner == null) return;
            discardAds(); consentReady = false;
            final long request = epoch;
            UserMessagingPlatform.showPrivacyOptionsForm(owner, error -> onMain(() -> {
                if (current(request)) updateConsentGate(error == null ? "consent_ready" : "consent_error");
            }));
        });
    }

    private void updateConsentGate(String fallback) {
        consentReady = allowed() && consent != null && consent.canRequestAds();
        report(consentReady ? "consent_ready" : fallback.equals("consent_ready") ? "consent_error" : fallback);
        event("ads_consent_state", "can_request_ads", Boolean.toString(consentReady),
                "privacy_options_required", Boolean.toString(isPrivacyOptionsRequired()));
    }

    /** Explicit user action only: SDK initialization is deferred until this call. */
    public void loadRewarded() {
        onMain(() -> {
            if (!gate()) return;
            if (!consentReady || consent == null || !consent.canRequestAds()) { report("consent_error"); return; }
            if (loading || activeShow != null) return;
            if (cached != null) { report("ads_ready"); return; }
            final long request = epoch;
            if (SDK_READY.get()) { loadSingle(request); return; }
            if (!SDK_INITIALIZING.compareAndSet(false, true)) { report("ads_initializing"); return; }
            report("ads_initializing");
            Activity owner = owner();
            if (owner == null) { SDK_INITIALIZING.set(false); return; }
            Context application = owner.getApplicationContext();
            new Thread(() -> {
                if (!current(request) || !consentReady) { SDK_INITIALIZING.set(false); return; }
                try {
                    InitializationConfig config = new InitializationConfig.Builder(CommerceConfig.appId())
                            .disableSdkCrashReporting().build();
                    MobileAds.initialize(application, config, initialized -> {
                        SDK_READY.set(true); SDK_INITIALIZING.set(false);
                        onMain(() -> { if (current(request) && consentReady) loadSingle(request); });
                    });
                } catch (RuntimeException | LinkageError failure) {
                    SDK_INITIALIZING.set(false);
                    onMain(() -> { if (current(request)) report("ads_failed"); });
                }
            }, "Luma-Ads-Init").start();
        });
    }

    private void loadSingle(long request) {
        if (!current(request) || !consentReady || loading) return;
        loading = true; report("ads_loading");
        event("ad_load_requested", "test", Boolean.toString(CommerceConfig.MODE_SANDBOX || CommerceConfig.FORCE_TEST_ADS));
        RewardedAd.load(new AdRequest.Builder(CommerceConfig.rewardedId(rewardPlacement)).build(), new AdLoadCallback<RewardedAd>() {
            @Override public void onAdLoaded(RewardedAd ad) {
                onMain(() -> {
                    if (!current(request) || !consentReady) { destroyAd(ad); return; }
                    loading = false;
                    cached = ad; report("ads_ready"); event("ad_loaded", "test", Boolean.toString(CommerceConfig.MODE_SANDBOX || CommerceConfig.FORCE_TEST_ADS));
                });
            }
            @Override public void onAdFailedToLoad(LoadAdError error) {
                onMain(() -> { if (current(request)) { loading = false; report("ads_failed"); event("ad_load_failed", "test", Boolean.toString(CommerceConfig.MODE_SANDBOX || CommerceConfig.FORCE_TEST_ADS)); } });
            }
        });
    }

    public void showRewarded() {
        onMain(() -> {
            if (!gate()) return;
            if (!isRewardedReady()) { report("ads_not_ready"); return; }
            Activity owner = owner(); if (owner == null) return;
            final RewardedAd ad = cached;
            cached = null;
            final ShowSession show = new ShowSession(epoch);
            activeShow = show;
            presenting = ad;
            ad.setAdEventCallback(new RewardedAdEventCallback() {
                @Override public void onAdShowedFullScreenContent() {
                    onMain(() -> { if (validShow(show)) report("ads_showing"); });
                }
                @Override public void onAdDismissedFullScreenContent() {
                    onMain(() -> {
                        // A dismissal is not a consent withdrawal. Keep this session
                        // valid for an earned-reward callback already in the queue.
                        boolean ownsPresentation = presenting == ad;
                        if (ownsPresentation) presenting = null;
                        if (activeShow == show) activeShow = null;
                        destroyAd(ad);
                        if (ownsPresentation && current(show.epoch)) report("ads_closed");
                    });
                }
                @Override public void onAdFailedToShowFullScreenContent(FullScreenContentError error) {
                    onMain(() -> {
                        show.cancelled = true;
                        boolean ownsPresentation = presenting == ad;
                        if (ownsPresentation) presenting = null;
                        if (activeShow == show) activeShow = null;
                        destroyAd(ad);
                        if (ownsPresentation && current(show.epoch)) report("ads_failed");
                    });
                }
                @Override public void onAdImpression() {
                    onMain(() -> { if (validShow(show)) event("ad_impression", "test", Boolean.toString(CommerceConfig.MODE_SANDBOX || CommerceConfig.FORCE_TEST_ADS)); });
                }
                @Override public void onAdPaid(AdValue value) {
                    onMain(() -> {
                        if (validShow(show)) event("ad_paid", "value_micros", Long.toString(value.getValueMicros()),
                                "currency", value.getCurrencyCode(), "precision", value.getPrecisionType().toString(),
                                "test", Boolean.toString(CommerceConfig.MODE_SANDBOX || CommerceConfig.FORCE_TEST_ADS));
                    });
                }
            });
            try {
                ad.show(owner, reward -> onMain(() -> {
                    if (validShow(show) && show.rewarded.compareAndSet(false, true)) {
                        // Never trust a provider-supplied amount/type for our wallet.
                        try { listener.onReward(show.id, rewardPlacement == RewardPlacement.GOLD ? CommerceConfig.REWARDED_GOLD : 0); }
                        catch (RewardUnavailableException rejected) { report(rejected.statusKey); return; }
                        catch (RuntimeException saveFailure) { report("commerce_save_error"); return; }
                        if (rewardPlacement == RewardPlacement.GOLD) {
                            event("ad_reward_earned", "gold", Integer.toString(CommerceConfig.REWARDED_GOLD),
                                    "test", Boolean.toString(CommerceConfig.MODE_SANDBOX || CommerceConfig.FORCE_TEST_ADS));
                            report("ads_rewarded");
                        } else if(rewardPlacement==RewardPlacement.CONTINUATION) {
                            event("ad_reward_earned", "reward", "continuation", "test", Boolean.toString(CommerceConfig.MODE_SANDBOX || CommerceConfig.FORCE_TEST_ADS));
                            report("ads_continue_earned");
                        } else {
                            event("ad_reward_earned", "reward", "power", "item", rewardItemId, "amount", "1", "test", Boolean.toString(CommerceConfig.MODE_SANDBOX || CommerceConfig.FORCE_TEST_ADS));
                            report("ads_power_earned");
                        }
                    }
                }));
            } catch (RuntimeException failure) {
                show.cancelled = true; activeShow = null; presenting = null; destroyAd(ad); report("ads_failed");
            }
        });
    }

    private boolean validShow(ShowSession show) { return !show.cancelled && current(show.epoch) && consentReady; }
    private boolean current(long request) { return !closed && request == epoch && allowed(); }
    private boolean allowed() { return !closed && optedIn && age == AgeStatus.ADULT; }
    private Activity owner() {
        Activity value = activity.get();
        return value == null || value.isFinishing() || value.isDestroyed() ? null : value;
    }
    private boolean gate() {
        if (closed || owner() == null) return false;
        if (age != AgeStatus.ADULT) { report("adult_required"); return false; }
        if (!optedIn) { report("ads_opt_in_required"); return false; }
        if (!CommerceConfig.MODE_SANDBOX && !CommerceConfig.FORCE_TEST_ADS && !CommerceConfig.productionAdsReady()) { report("production_blocked"); return false; }
        return true;
    }
    private void discardAds() {
        epoch++; loading = false;
        if (activeShow != null) activeShow.cancelled = true;
        activeShow = null;
        RewardedAd oldCache = cached; cached = null; destroyAd(oldCache);
        RewardedAd oldPresentation = presenting; presenting = null; destroyAd(oldPresentation);
    }
    private static void destroyAd(RewardedAd ad) {
        if (ad != null) try { ad.destroy(); } catch (RuntimeException ignored) { /* Policy remains withdrawn. */ }
    }
    private void event(String name, String... fields) {
        if (!localMetrics || closed || !optedIn) return;
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i + 1 < fields.length; i += 2) values.put(fields[i], fields[i + 1]);
        listener.onEvent(name, Collections.unmodifiableMap(values));
    }
    private void report(String key) { if (!closed) { status = key; listener.onStatus(key); } }
    private void onMain(Runnable action) { if (Looper.myLooper() == main.getLooper()) action.run(); else main.post(action); }

    /** Call on owner destruction, not onPause: a fullscreen ad pauses its parent. */
    public void close() {
        onMain(() -> { discardAds(); consentReady = false; closed = true; activity.clear(); });
    }
}
