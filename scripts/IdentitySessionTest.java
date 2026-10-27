package com.androidhit.lumadrift.identity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.credentials.CredentialManager;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import com.androidhit.lumadrift.commerce.CommerceConfig;
import com.androidhit.lumadrift.data.Strings;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.*;
import java.util.ArrayList;
import java.util.List;

/** Deterministic tests of the real controller; provider and platform operations are doubles. */
public final class IdentitySessionTest {
    private static int checks, scenarios;
    private static void check(boolean value, String message) {
        checks++; if (!value) throw new AssertionError(message);
    }
    private static void state(GoogleAccountController controller, SessionSnapshot.State expected) {
        check(controller.snapshot().state == expected, "Expected " + expected + ", got " + controller.snapshot().state);
        if (expected != SessionSnapshot.State.CONNECTED) {
            check(!controller.snapshot().signedIn, "Unverified state cannot claim signed in");
            check(controller.snapshot().uid.isEmpty() && controller.snapshot().displayName.isEmpty()
                    && controller.snapshot().email.isEmpty(), "Unverified state cannot expose a cached profile");
        }
    }
    private static final class Fixture {
        final Activity activity = new Activity();
        final List<SessionSnapshot> events = new ArrayList<>();
        final FirebaseUser user;
        final GoogleAccountController controller;
        Fixture(boolean restored) {
            Context.prefs = new SharedPreferences(); FirebaseApp.app = null; FirebaseApp.initializations = 0;
            FirebaseAuth.instance = new FirebaseAuth(); IdentityConfig.configured = true;
            CredentialManager.chooserCalls = 0; CredentialManager.clearCalls = 0; CredentialManager.failClear = false;
            CommerceConfig.MODE_SANDBOX = true;
            user = new FirebaseUser("alice", "Alice");
            if (restored) {
                Context.prefs.edit().putBoolean("previously_signed_in", true).putString("signed_in_uid", user.uid).apply();
                FirebaseAuth.instance.user = user;
            }
            controller = new GoogleAccountController(activity, events::add);
        }
        void connect() {
            controller.onResume(); state(controller, SessionSnapshot.State.REFRESHING);
            check(user.reloads.size() == 1, "Foreground must reload exactly once, including after background initialization");
            user.refreshed(); state(controller, SessionSnapshot.State.CONNECTED);
        }
        void close() { controller.close(); }
    }
    private static void guestAndConfiguration() {
        scenarios++; Fixture f = new Fixture(false);
        f.controller.onResume(); f.controller.onPause(); f.controller.onResume();
        state(f.controller, SessionSnapshot.State.GUEST);
        check(FirebaseApp.initializations == 0, "Guest must not initialize Firebase or require network");
        check(CredentialManager.chooserCalls == 0, "Lifecycle must never open an account chooser");
        check(f.controller.snapshot().canSignIn && !f.controller.snapshot().canDeleteAccount, "Guest actions");
        f.close();
        IdentityConfig.configured = false;
        GoogleAccountController missing = new GoogleAccountController(new Activity(), ignored -> {});
        missing.onResume(); missing.signIn(); state(missing, SessionSnapshot.State.GUEST);
        check("google_not_configured".equals(missing.snapshot().statusKey), "Missing owner config explained honestly");
        check(!missing.snapshot().configured && !missing.snapshot().canSignIn, "Unconfigured Google unavailable");
        check(CredentialManager.chooserCalls == 0, "No provider request with absent config"); missing.close();
    }
    private static void lifecycleAndProfile() {
        scenarios++; Fixture f = new Fixture(true); f.connect();
        check("Alice".equals(f.controller.snapshot().displayName), "Verified display name");
        check("alice@example.invalid".equals(f.controller.snapshot().email), "Verified email");
        f.controller.onPause(); f.user.name = "Alice updated\n";
        f.controller.onResume(); state(f.controller, SessionSnapshot.State.REFRESHING);
        f.user.refreshed(); check("Alice updated".equals(f.controller.snapshot().displayName), "Refresh must update and sanitize profile");
        check(CredentialManager.chooserCalls == 0, "Resume must stay silent"); f.close();
    }
    private static void networkFailureAndRetry() {
        scenarios++; Fixture f = new Fixture(true); f.connect();
        f.controller.onPause(); f.controller.onResume();
        f.user.reloads.remove().fail(new java.io.IOException("offline"));
        state(f.controller, SessionSnapshot.State.ERROR);
        check("account_session_error".equals(f.controller.snapshot().statusKey), "Network failure is not a false auth denial");
        check(f.controller.snapshot().canSignIn && f.controller.snapshot().canSignOut
                && !f.controller.snapshot().canDeleteAccount, "Retry and sign-out remain actionable");
        f.controller.onResume(); f.user.refreshed(); state(f.controller, SessionSnapshot.State.CONNECTED); f.close();
    }
    private static void invalidTokenAndExplicitReauth() {
        scenarios++; Fixture f = new Fixture(true); f.connect();
        f.controller.onResume(); f.user.reloads.remove().succeed(null);
        f.user.tokens.remove().fail(new FirebaseAuthInvalidCredentialsException());
        state(f.controller, SessionSnapshot.State.REAUTH_REQUIRED);
        check(GoogleAccountController.currentWalletUser(f.activity) == null, "Known auth failure pauses wallet requests until reconnect");
        f.controller.onResume(); f.user.refreshed();
        state(f.controller, SessionSnapshot.State.REAUTH_REQUIRED);
        check(CredentialManager.chooserCalls == 0, "Reauth prompt requires a user action");
        f.controller.signIn(); CredentialManager.latest.googleResult(true);
        check(f.user.reauths.size() == 1 && FirebaseAuth.instance.signIns.isEmpty(), "Existing account must use Firebase reauthentication");
        // The real completion is intentionally not tied to an Activity-scoped onStop listener.
        f.controller.onPause(); f.user.reauths.remove().succeed(new AuthResult(f.user));
        f.user.tokens.remove().succeed(new GetTokenResult("fresh.firebase.token"));
        state(f.controller, SessionSnapshot.State.CONNECTED);
        check(GoogleAccountController.currentWalletUser(f.activity) == f.user, "Explicit reconnect restores the wallet identity");
        check(CredentialManager.chooserCalls == 1, "Only explicit reconnect opens provider"); f.close();
    }
    private static void externalSignOutAndWrongAccount() {
        scenarios++; Fixture f = new Fixture(true); f.connect();
        FirebaseAuth.instance.signOut(); state(f.controller, SessionSnapshot.State.REAUTH_REQUIRED);
        check(f.controller.snapshot().canSignIn, "Remote session loss can reconnect"); f.close();
        f = new Fixture(true); f.connect();
        FirebaseAuth.instance.setUser(new FirebaseUser("mallory", "Other person"));
        state(f.controller, SessionSnapshot.State.REAUTH_REQUIRED);
        check(GoogleAccountController.currentGoogleUser(f.activity) == null, "Unexpected UID must never authorize wallet"); f.close();
    }
    private static void wrongProviders() {
        for (boolean anonymous : new boolean[]{true, false}) {
            scenarios++; Fixture f = new Fixture(true);
            f.user.anonymous = anonymous; if (!anonymous) f.user.provider = "password";
            f.controller.onResume(); state(f.controller, SessionSnapshot.State.GUEST);
            check("account_google_only".equals(f.controller.snapshot().statusKey), "Reject anonymous/non-Google account");
            check(GoogleAccountController.currentGoogleUser(f.activity) == null, "Rejected provider cannot authorize wallet"); f.close();
        }
    }
    private static void signOutDominatesRefresh() {
        scenarios++; Fixture f = new Fixture(true); f.connect();
        f.controller.onResume(); com.google.android.gms.tasks.Task<Void> late = f.user.reloads.remove();
        f.controller.signOut(); state(f.controller, SessionSnapshot.State.GUEST);
        check(CredentialManager.clearCalls == 1, "Explicit sign-out clears Credential Manager state");
        check(!Context.prefs.getBoolean("previously_signed_in", true), "Persist explicit sign-out before callbacks");
        late.succeed(null); state(f.controller, SessionSnapshot.State.GUEST);
        check(f.user.tokens.isEmpty(), "Stale refresh cannot request or publish token after sign-out"); f.close();
    }
    private static void signOutDominatesPendingSignIn() {
        scenarios++; Fixture f = new Fixture(false); f.controller.onResume();
        f.controller.signIn(); CredentialManager.latest.googleResult(true);
        check(FirebaseAuth.instance.signIns.size() == 1, "Google assertion reaches Firebase, not a fabricated profile");
        f.controller.signOut(); FirebaseAuth.instance.finishSignIn(f.user);
        state(f.controller, SessionSnapshot.State.GUEST);
        check(!IdentitySessionAccess.previouslySignedIn(f.activity), "Late Firebase result cannot restore revoked consent");
        check(GoogleAccountController.currentGoogleUser(f.activity) == null, "Late result cannot restore wallet identity"); f.close();
    }
    private static void closeRemovesListeners() {
        scenarios++; Fixture f = new Fixture(true); f.connect(); f.controller.onResume();
        com.google.android.gms.tasks.Task<Void> late = f.user.reloads.remove(); int before = f.events.size();
        f.close(); late.succeed(null); FirebaseAuth.instance.signOut();
        check(f.events.size() == before, "Destroyed screen cannot receive session callbacks");
        check(FirebaseAuth.instance.authListeners.isEmpty() && FirebaseAuth.instance.tokenListeners.isEmpty()
                && Context.prefs.listeners.isEmpty(), "Destroy unregisters auth, token and preference listeners");
    }
    private static void cancellationAndBindingFailure() {
        scenarios++; Fixture f = new Fixture(false); f.controller.onResume(); f.controller.signIn();
        CredentialManager.latest.callback.onError(new GetCredentialCancellationException());
        state(f.controller, SessionSnapshot.State.ERROR);
        check("google_cancelled".equals(f.controller.snapshot().statusKey), "Cancellation is recoverable and explicit");
        check(f.controller.snapshot().canSignIn, "Cancellation must not strand a busy screen");
        f.controller.signIn(); CredentialManager.latest.googleResult(false);
        state(f.controller, SessionSnapshot.State.ERROR);
        check(FirebaseAuth.instance.signIns.isEmpty(), "Nonce mismatch never reaches Firebase");
        check(!IdentitySessionAccess.previouslySignedIn(f.activity), "Parsed JWT is never a trusted sign-in"); f.close();
    }
    private static void recentLoginDeleteAndCleanupFailure() {
        scenarios++; Fixture f = new Fixture(true); f.connect();
        f.controller.deleteAccount(); f.user.deletes.remove().fail(new FirebaseAuthRecentLoginRequiredException());
        state(f.controller, SessionSnapshot.State.REAUTH_REQUIRED);
        check(f.controller.snapshot().canSignIn && !f.controller.snapshot().canDeleteAccount, "Delete failure exposes reconnect action");
        CredentialManager.failClear = true; f.controller.signOut(); state(f.controller, SessionSnapshot.State.GUEST);
        check(GoogleAccountController.currentGoogleUser(f.activity) == null, "Provider cleanup failure cannot undo local logout"); f.close();
    }
    private static void observerAcrossAccountScreen() {
        scenarios++; Fixture f = new Fixture(false); f.controller.onResume();
        List<SessionSnapshot> accountEvents = new ArrayList<>();
        GoogleAccountController account = new GoogleAccountController(f.activity, accountEvents::add);
        account.onResume(); f.controller.onPause(); account.signIn(); CredentialManager.latest.googleResult(true);
        FirebaseAuth.instance.finishSignIn(f.user);
        // Background observer learns the account exists; it must not publish a connected badge.
        state(f.controller, SessionSnapshot.State.REFRESHING);
        f.user.tokens.remove().succeed(new GetTokenResult("verified.firebase.token"));
        state(account, SessionSnapshot.State.CONNECTED);
        f.controller.onResume(); f.user.refreshed(); state(f.controller, SessionSnapshot.State.CONNECTED);
        account.signOut(); state(f.controller, SessionSnapshot.State.GUEST);
        check(f.controller.snapshot().displayName.isEmpty(), "Other screen's logout clears the home profile immediately");
        account.close(); f.close();
    }
    private static void emptyTokenAndTranslations() {
        scenarios++; Fixture f = new Fixture(true); f.controller.onResume(); f.user.reloads.remove().succeed(null);
        f.user.tokens.remove().succeed(new GetTokenResult("")); state(f.controller, SessionSnapshot.State.ERROR); f.close();
        String[] keys = {"account_refreshing", "account_reauth", "account_session_error", "account_google_only", "google_reconnect"};
        for (String language : new String[]{"en", "fr", "de", "es", "pt", "it"}) for (String key : keys) {
            check(!Strings.get(language, key).isEmpty() && !Strings.get(language, key).equals(key), "Missing " + language + " copy for " + key);
        }
    }
    private static void reauthSurvivesScreensAndRecreation() {
        scenarios++; Fixture f = new Fixture(true); f.connect();
        GoogleAccountController observer = new GoogleAccountController(f.activity, ignored -> {});
        observer.onResume(); f.user.refreshed(); state(observer, SessionSnapshot.State.CONNECTED);
        f.controller.deleteAccount(); f.user.deletes.remove().fail(new FirebaseAuthRecentLoginRequiredException());
        state(f.controller, SessionSnapshot.State.REAUTH_REQUIRED);
        state(observer, SessionSnapshot.State.REAUTH_REQUIRED);
        f.close(); observer.close();
        GoogleAccountController recreated = new GoogleAccountController(f.activity, ignored -> {});
        recreated.onResume(); f.user.refreshed(); state(recreated, SessionSnapshot.State.REAUTH_REQUIRED);
        GoogleAccountController home = new GoogleAccountController(f.activity, ignored -> {});
        home.onResume(); f.user.refreshed(); state(home, SessionSnapshot.State.REAUTH_REQUIRED); home.onPause();
        recreated.signIn(); CredentialManager.latest.googleResult(true);
        f.user.reauths.remove().succeed(new AuthResult(f.user));
        f.user.tokens.remove().succeed(new GetTokenResult("new.firebase.token"));
        state(recreated, SessionSnapshot.State.CONNECTED);
        home.onResume(); f.user.refreshed(); state(home, SessionSnapshot.State.CONNECTED);
        check(!IdentitySessionAccess.reauthenticationRequired(f.activity), "Explicit reauthentication clears the shared marker");
        recreated.close(); home.close();
    }
    private static void bindLegacyRestoredUid() {
        scenarios++; Fixture f = new Fixture(true);
        Context.prefs.edit().remove("signed_in_uid").apply();
        f.connect();
        check(f.user.uid.equals(Context.prefs.getString("signed_in_uid", "")), "A validated pre-upgrade session binds its UID");
        FirebaseAuth.instance.setUser(new FirebaseUser("other", "Wrong account"));
        state(f.controller, SessionSnapshot.State.REAUTH_REQUIRED);
        check(GoogleAccountController.currentGoogleUser(f.activity) == null, "A legacy session cannot later drift to another UID"); f.close();
    }
    public static void main(String[] args) {
        guestAndConfiguration(); lifecycleAndProfile(); networkFailureAndRetry(); invalidTokenAndExplicitReauth();
        externalSignOutAndWrongAccount(); wrongProviders(); signOutDominatesRefresh(); signOutDominatesPendingSignIn();
        closeRemovesListeners(); cancellationAndBindingFailure(); recentLoginDeleteAndCleanupFailure();
        observerAcrossAccountScreen(); emptyTokenAndTranslations(); reauthSurvivesScreensAndRecreation(); bindLegacyRestoredUid();
        System.out.println("PASS identity session: " + scenarios + " scenarios, " + checks + " assertions (real controller, deterministic platform/provider doubles).");
        System.out.println("NOT VALIDATED: actual Google chooser, OAuth/Firebase server exchange, real account reconnect/sign-out/deletion on a device.");
    }
}
