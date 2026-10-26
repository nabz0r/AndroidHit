package com.androidhit.lumadrift.wallet;

import android.app.Activity;
import android.content.Context;
import com.androidhit.lumadrift.commerce.CommerceConfig;
import com.androidhit.lumadrift.commerce.PlayGoldBilling;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Silent recovery for an existing Google account when the game returns to the
 * foreground. It cannot open checkout, show ads, create accounts or grant local gold.
 */
public final class WalletRecoveryController {
    private final WeakReference<Activity> owner;
    private final Context context;
    private final Runnable walletUpdated;
    private ProductionWalletClient wallet;
    private PlayGoldBilling billing;
    private final Set<String> verifying = new HashSet<>();
    private boolean foreground, closed;
    private String sessionUid = "";
    private long generation;

    public WalletRecoveryController(Activity activity, Runnable walletUpdated) {
        if (activity == null || walletUpdated == null) throw new IllegalArgumentException("Recovery owner required");
        owner = new WeakReference<>(activity);
        context = activity.getApplicationContext();
        this.walletUpdated = walletUpdated;
    }

    /** Main-thread owner callback. Guest and unconfigured builds do no network work. */
    public void onForeground() {
        if (closed) return;
        foreground = true;
        if (CommerceConfig.MODE_SANDBOX || !CommerceConfig.productionBillingReady()) { stopSession(); return; }
        String uid = ProductionWalletClient.currentUid(context);
        if (uid.isEmpty()) { stopSession(); return; }
        Activity activity = owner.get();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) { stopSession(); return; }
        if (wallet == null || !uid.equals(sessionUid)) {
            stopSession();
            sessionUid = uid;
            wallet = new ProductionWalletClient(context);
            if (!wallet.isConfigured()) { stopSession(); return; }
            final long request = generation;
            final String expectedUid = uid;
            billing = new PlayGoldBilling(activity, new PlayGoldBilling.Listener() {
                public void onStatus(String ignored) { }
                public void onProducts(List<PlayGoldBilling.GoldProduct> ignored) { }
                public void onPurchasePending(String ignored) { }
                public void onPurchaseNeedsVerification(String productId, String token) {
                    if (!current(request, expectedUid) || token == null || !verifying.add(token)) return;
                    wallet.verify(productId, token, new ProductionWalletClient.Callback() {
                        public void onSuccess(ProductionWalletClient.Wallet result) {
                            if (!current(request, expectedUid)) return;
                            verifying.remove(token);
                            walletUpdated.run();
                        }
                        public void onError(String ignored) {
                            if (current(request, expectedUid)) verifying.remove(token);
                            // Play owned purchases and the server's durable jobs retry
                            // later. Recovery never interrupts a puzzle with dialogs.
                        }
                    });
                }
            });
        }
        final long request = generation;
        final String expectedUid = sessionUid;
        wallet.refresh(new ProductionWalletClient.Callback() {
            public void onSuccess(ProductionWalletClient.Wallet result) { if (current(request, expectedUid)) walletUpdated.run(); }
            public void onError(String ignored) { /* The explicit wallet screen exposes retry/status controls. */ }
        });
        billing.connectForRecovery();
    }

    private boolean current(long request, String uid) {
        Activity activity = owner.get();
        return !closed && foreground && generation == request && uid.equals(sessionUid)
                && uid.equals(ProductionWalletClient.currentUid(context)) && activity != null
                && !activity.isFinishing() && !activity.isDestroyed();
    }
    public void onBackground() { foreground = false; stopSession(); }
    private void stopSession() {
        generation++;
        if (billing != null) { billing.close(); billing = null; }
        if (wallet != null) { wallet.close(); wallet = null; }
        verifying.clear(); sessionUid = "";
    }
    public void close() { if (closed) return; closed = true; foreground = false; stopSession(); owner.clear(); }
}
