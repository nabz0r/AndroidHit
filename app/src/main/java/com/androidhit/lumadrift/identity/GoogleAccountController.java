package com.androidhit.lumadrift.identity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.CancellationSignal;
import androidx.credentials.*;
import androidx.credentials.exceptions.*;
import com.google.android.libraries.identity.googleid.*;
import com.google.firebase.auth.*;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.Executor;

/** Credential Manager -> Google assertion -> Firebase verification, with observable session state. */
public final class GoogleAccountController {
    public interface Listener { void onState(SessionSnapshot snapshot); }
    private final Activity activity;
    private final Listener listener;
    private final IdentityConfig config;
    private final SessionTracker tracker;
    private final CredentialManager manager;
    private final SharedPreferences prefs;
    private final Executor main;
    private FirebaseAuth auth;
    private CancellationSignal cancellation;
    private com.androidhit.lumadrift.wallet.ProductionWalletClient wallet;
    private com.androidhit.lumadrift.social.SocialClient social;
    private boolean closed, resumed, signingIn, deleting, refreshInFlight, reauthRequired;
    private String observedUid = "";
    private final FirebaseAuth.AuthStateListener authListener = ignored -> reconcile(true);
    private final FirebaseAuth.IdTokenListener tokenListener = ignored -> reconcile(true);
    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceListener = (ignored, key) -> {
        if ("previously_signed_in".equals(key) || "signed_in_uid".equals(key)
                || "reauth_required".equals(key) || "session_revision".equals(key)) reconcile(false);
    };

    public GoogleAccountController(Activity activity, Listener listener) {
        this.activity = activity; this.listener = listener; config = IdentityConfig.load(activity);
        tracker = new SessionTracker(config.isConfigured()); manager = CredentialManager.create(activity);
        prefs = IdentitySessionAccess.preferences(activity);
        main = androidx.core.content.ContextCompat.getMainExecutor(activity);
        prefs.registerOnSharedPreferenceChangeListener(preferenceListener);
        if (IdentitySessionAccess.previouslySignedIn(activity)) initialize(false);
    }
    public SessionSnapshot snapshot() { return tracker.snapshot(); }
    public boolean isConfigured() { return config.isConfigured(); }
    public boolean isSignedIn() { return snapshot().signedIn; }
    public String displayName() { return snapshot().displayName; }
    public void announce() { emit(); }
    public static FirebaseUser currentGoogleUser(Context context) { return IdentitySessionAccess.currentUser(context); }
    /** Wallet work must wait for an explicit reconnect after a known authentication failure. */
    public static FirebaseUser currentWalletUser(Context context) {
        return IdentitySessionAccess.reauthenticationRequired(context) ? null : currentGoogleUser(context);
    }

    private boolean initialize(boolean explicit) {
        if (auth != null) return true;
        try {
            auth = IdentitySessionAccess.auth(activity, explicit);
            if (auth == null) return false;
            auth.addAuthStateListener(authListener); auth.addIdTokenListener(tokenListener); return true;
        } catch (RuntimeException invalid) {
            long request = tracker.begin("", "account_refreshing", false);
            tracker.failure(request, "google_error", false, false); emit(); return false;
        }
    }
    /** Silent refresh on foreground entry; never opens an account chooser. */
    public void onResume() {
        if (closed) return;
        resumed = true;
        if (!signingIn && !deleting) reconcile(true); else emit();
    }
    public void onPause() { resumed = false; }
    private void reconcile(boolean force) {
        if (closed || signingIn || deleting) return;
        reauthRequired = IdentitySessionAccess.reauthenticationRequired(activity);
        if (reauthRequired && !force) {
            if (!snapshot().busy) {
                long request = tracker.begin("", "account_refreshing", currentGoogleUser(activity) != null);
                tracker.failure(request, "account_reauth", true, currentGoogleUser(activity) != null);
            }
            emit(); return;
        }
        if (reauthRequired && snapshot().signedIn) {
            long request = tracker.begin(snapshot().uid, "account_refreshing", true);
            tracker.failure(request, "account_reauth", true, true); emit();
        }
        if (!IdentitySessionAccess.previouslySignedIn(activity)) {
            observedUid = "";
            if (snapshot().state != SessionSnapshot.State.GUEST || snapshot().hasGoogleAccount) {
                tracker.guest(config.isConfigured() ? "account_guest" : "google_not_configured"); emit();
            }
            return;
        }
        if (!initialize(false)) return;
        FirebaseUser user = currentGoogleUser(activity);
        if (user == null) {
            boolean wrongProvider = auth.getCurrentUser() != null && !IdentitySessionAccess.google(auth.getCurrentUser());
            observedUid = ""; refreshInFlight = false;
            if (wrongProvider) {
                IdentitySessionAccess.revoke(activity); auth.signOut(); tracker.guest("account_google_only");
            } else {
                reauthRequired = true;
                IdentitySessionAccess.requireReauthentication(activity);
                long request = tracker.begin("", "account_refreshing", false);
                tracker.failure(request, "account_reauth", true, IdentitySessionAccess.google(auth.getCurrentUser()));
            }
            emit(); return;
        }
        boolean changed = !user.getUid().equals(observedUid);
        observedUid = user.getUid();
        if (refreshInFlight || (!force && !changed && snapshot().state == SessionSnapshot.State.CONNECTED)) return;
        // While backgrounded, invalidate stale display immediately, but defer network work to resume.
        if (!resumed) { tracker.begin(user.getUid(), "account_refreshing", true); emit(); return; }
        refresh(user);
    }
    private void refresh(FirebaseUser user) {
        refreshInFlight = true;
        long request = tracker.begin(user.getUid(), "account_refreshing", true); emit();
        user.reload().addOnCompleteListener(main, task -> {
            if (!tracker.current(request)) return;
            if (!task.isSuccessful()) { sessionFailure(request, task.getException()); return; }
            FirebaseUser current = currentGoogleUser(activity);
            if (current == null || !user.getUid().equals(current.getUid())) { reconcileLost(request); return; }
            validateToken(request, current);
        });
    }
    private void validateToken(long request, FirebaseUser user) {
        refreshInFlight = true;
        user.getIdToken(false).addOnCompleteListener(main, task -> {
            if (!tracker.current(request)) return;
            FirebaseUser current = currentGoogleUser(activity);
            if (current == null || !user.getUid().equals(current.getUid())) { reconcileLost(request); return; }
            if (!task.isSuccessful() || task.getResult() == null || task.getResult().getToken() == null || task.getResult().getToken().isEmpty()) {
                sessionFailure(request, task.getException()); return;
            }
            if (!IdentitySessionAccess.bindValidatedUser(activity, current)) { reconcileLost(request); return; }
            refreshInFlight = false;
            reauthRequired = IdentitySessionAccess.reauthenticationRequired(activity);
            if (reauthRequired) tracker.failure(request, "account_reauth", true, true);
            else tracker.connected(request, current.getUid(), current.getDisplayName(), current.getEmail(),
                    IdentitySessionAccess.google(current), current.isAnonymous());
            emit();
        });
    }
    private void reconcileLost(long request) {
        if (!tracker.current(request)) return;
        refreshInFlight = false; observedUid = "";
        if (IdentitySessionAccess.previouslySignedIn(activity)) {
            reauthRequired = true; tracker.failure(request, "account_reauth", true, false);
            IdentitySessionAccess.requireReauthentication(activity);
        } else tracker.guest("account_guest");
        emit();
    }
    private void sessionFailure(long request, Exception error) {
        boolean reauth = error instanceof FirebaseAuthInvalidUserException
                || error instanceof FirebaseAuthInvalidCredentialsException
                || error instanceof FirebaseAuthRecentLoginRequiredException;
        reauthRequired |= reauth;
        if (reauthRequired) IdentitySessionAccess.requireReauthentication(activity);
        refreshInFlight = false;
        tracker.failure(request, reauthRequired ? "account_reauth" : "account_session_error", reauthRequired,
                currentGoogleUser(activity) != null); emit();
    }

    public void signIn() {
        if (closed || snapshot().busy) return;
        if (!config.isConfigured()) { tracker.guest("google_not_configured"); emit(); return; }
        if (!initialize(true)) return;
        FirebaseUser previous = currentGoogleUser(activity);
        signingIn = true;
        long request = tracker.begin(previous == null ? "" : previous.getUid(), "google_loading", previous != null);
        long consent = IdentitySessionAccess.beginSignIn(); emit();
        byte[] random = new byte[32]; new SecureRandom().nextBytes(random);
        String nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        try {
            GetSignInWithGoogleOption option = new GetSignInWithGoogleOption.Builder(config.webClientId).setNonce(nonce).build();
            GetCredentialRequest wanted = new GetCredentialRequest.Builder().addCredentialOption(option).build();
            cancellation = new CancellationSignal();
            manager.getCredentialAsync(activity, wanted, cancellation, main, new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                public void onResult(GetCredentialResponse response) {
                    if (!tracker.current(request)) return;
                    try {
                        Credential credential = response.getCredential();
                        if (!(credential instanceof CustomCredential) || !GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
                            signInFailed(request, "google_error"); return;
                        }
                        GoogleIdTokenCredential google = GoogleIdTokenCredential.createFrom(credential.getData());
                        String token = google.getIdToken(); String[] parts = token.split("\\.");
                        if (parts.length != 3 || token.length() > 16384) { signInFailed(request, "google_error"); return; }
                        org.json.JSONObject claims = new org.json.JSONObject(new String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE), java.nio.charset.StandardCharsets.UTF_8));
                        if (!nonce.equals(claims.optString("nonce")) || !config.webClientId.equals(claims.optString("aud"))) {
                            signInFailed(request, "google_error"); return;
                        }
                        // Parsed claims are only request binding; Firebase must verify the assertion.
                        AuthCredential assertion = GoogleAuthProvider.getCredential(token, null);
                        com.google.android.gms.tasks.Task<AuthResult> task = previous == null
                                ? auth.signInWithCredential(assertion) : previous.reauthenticateAndRetrieveData(assertion);
                        // Do not bind completion to Activity.onStop: Credential Manager can pause this screen.
                        task.addOnCompleteListener(main, result -> {
                            FirebaseUser verified = result.isSuccessful() && result.getResult() != null ? result.getResult().getUser() : null;
                            boolean accepted = verified != null && IdentitySessionAccess.acceptSignIn(activity, consent, verified);
                            if (!tracker.current(request)) {
                                if (!accepted && !IdentitySessionAccess.previouslySignedIn(activity)) auth.signOut();
                                return;
                            }
                            signingIn = false;
                            if (!accepted) { sessionFailure(request, result.getException()); return; }
                            FirebaseUser current = currentGoogleUser(activity);
                            if (current == null || !verified.getUid().equals(current.getUid())) { reconcileLost(request); return; }
                            reauthRequired = false; observedUid = current.getUid(); validateToken(request, current);
                        });
                    } catch (Exception malformed) { signInFailed(request, "google_error"); }
                }
                public void onError(GetCredentialException error) {
                    signInFailed(request, error instanceof GetCredentialCancellationException ? "google_cancelled" : "google_error");
                }
            });
        } catch (RuntimeException unavailable) { signInFailed(request, "google_error"); }
    }
    private void signInFailed(long request, String key) {
        if (!tracker.current(request)) return;
        signingIn = false;
        tracker.failure(request, key, reauthRequired, currentGoogleUser(activity) != null); emit();
    }
    public void signOut() {
        if (closed) return;
        String oldUid = currentGoogleUser(activity) == null ? "" : currentGoogleUser(activity).getUid();
        com.androidhit.lumadrift.social.SocialClient.clearCache(activity, oldUid);
        com.androidhit.lumadrift.social.CosmeticVaultClient.clearCache(activity, oldUid);
        signingIn = false; deleting = false; refreshInFlight = false; reauthRequired = false; observedUid = "";
        tracker.guest("account_guest");
        IdentitySessionAccess.revoke(activity);
        if (cancellation != null) cancellation.cancel();
        if (auth != null) auth.signOut();
        clearProvider(); emit();
    }
    public void deleteAccount() {
        if (closed || !snapshot().canDeleteAccount || auth == null) return;
        FirebaseUser user = currentGoogleUser(activity);
        if (user == null) { tracker.guest("account_guest"); emit(); return; }
        deleting = true;
        long request = tracker.begin(user.getUid(), "account_refreshing", true); emit();
        if (com.androidhit.lumadrift.social.SocialClient.configured()) {
            if (social == null) social = new com.androidhit.lumadrift.social.SocialClient(activity);
            social.deleteAccount(new com.androidhit.lumadrift.social.SocialClient.Callback() {
                public void onSuccess(org.json.JSONObject result) { deleted(request, user.getUid()); }
                public void onError(String code) {
                    if (!deletionCurrent(request, user.getUid())) return;
                    deleting = false; boolean reauth = "RECENT_LOGIN_REQUIRED".equals(code) || "SOCIAL_SIGN_IN_REQUIRED".equals(code);
                    reauthRequired |= reauth;
                    if (reauthRequired) IdentitySessionAccess.requireReauthentication(activity);
                    tracker.failure(request, reauth ? "account_reauth" : "account_session_error", reauth, currentGoogleUser(activity) != null); emit();
                }
            }); return;
        }
        if (!com.androidhit.lumadrift.commerce.CommerceConfig.MODE_SANDBOX && com.androidhit.lumadrift.commerce.CommerceConfig.productionBillingReady()) {
            if (wallet == null) wallet = new com.androidhit.lumadrift.wallet.ProductionWalletClient(activity);
            wallet.deleteAccount(new com.androidhit.lumadrift.wallet.ProductionWalletClient.Callback() {
                public void onSuccess(com.androidhit.lumadrift.wallet.ProductionWalletClient.Wallet ignored) { deleted(request, user.getUid()); }
                public void onError(String key) {
                    if (!deletionCurrent(request, user.getUid())) return;
                    deleting = false; boolean reauth = "account_recent_login".equals(key) || "wallet_sign_in_required".equals(key);
                    reauthRequired |= reauth;
                    if (reauthRequired) IdentitySessionAccess.requireReauthentication(activity);
                    tracker.failure(request, reauth ? "account_reauth" : "account_session_error", reauth, currentGoogleUser(activity) != null); emit();
                }
            }); return;
        }
        user.delete().addOnCompleteListener(main, task -> {
            if (!deletionCurrent(request, user.getUid())) return;
            if (task.isSuccessful()) deleted(request, user.getUid());
            else { deleting = false; sessionFailure(request, task.getException()); }
        });
    }
    private boolean deletionCurrent(long request, String expectedUid) {
        if (!tracker.current(request)) return false;
        FirebaseUser current = auth == null ? null : auth.getCurrentUser();
        if (current != null && !expectedUid.equals(current.getUid())) {
            deleting = false; refreshInFlight = false;
            reconcile(true);
            return false;
        }
        return true;
    }
    private void deleted(long request, String expectedUid) {
        if (!deletionCurrent(request, expectedUid)) return;
        com.androidhit.lumadrift.social.SocialClient.clearCache(activity, expectedUid);
        com.androidhit.lumadrift.social.CosmeticVaultClient.clearCache(activity, expectedUid);
        signOut(); tracker.guest("account_deleted"); emit();
    }
    private void clearProvider() {
        try {
            manager.clearCredentialStateAsync(new ClearCredentialStateRequest(), null, main,
                    new CredentialManagerCallback<Void, ClearCredentialException>() {
                        public void onResult(Void value) {}
                        public void onError(ClearCredentialException error) { /* Local sign-out is already complete. */ }
                    });
        } catch (RuntimeException ignored) { /* Provider cleanup cannot restore an app session. */ }
    }
    private void emit() { if (!closed && listener != null) listener.onState(snapshot()); }
    public void close() {
        if (closed) return;
        closed = true; tracker.close();
        prefs.unregisterOnSharedPreferenceChangeListener(preferenceListener);
        if (auth != null) { auth.removeAuthStateListener(authListener); auth.removeIdTokenListener(tokenListener); }
        if (cancellation != null) cancellation.cancel();
        if (wallet != null) wallet.close();
        if (social != null) social.close();
    }
}
