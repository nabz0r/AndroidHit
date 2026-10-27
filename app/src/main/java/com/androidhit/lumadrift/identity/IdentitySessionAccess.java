package com.androidhit.lumadrift.identity;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

/** Shared session consent/identity boundary for account screens and authenticated wallet calls. */
final class IdentitySessionAccess {
    static final String PREFS = "luma_identity_state_v1";
    private static long consentGeneration;
    private IdentitySessionAccess() {}
    static SharedPreferences preferences(Context context) { return context.getSharedPreferences(PREFS, 0); }
    static boolean previouslySignedIn(Context context) { return preferences(context).getBoolean("previously_signed_in", false); }
    static boolean reauthenticationRequired(Context context) { return preferences(context).getBoolean("reauth_required", false); }
    static void requireReauthentication(Context context) {
        if (!reauthenticationRequired(context)) preferences(context).edit().putBoolean("reauth_required", true).apply();
    }
    static synchronized long beginSignIn() { return ++consentGeneration; }
    static synchronized boolean acceptSignIn(Context context, long request, FirebaseUser user) {
        if (request != consentGeneration || !google(user)) return false;
        preferences(context).edit().putString("signed_in_uid", user.getUid()).putBoolean("previously_signed_in", true)
                .putBoolean("reauth_required", false).putString("session_revision", java.util.UUID.randomUUID().toString()).apply();
        return true;
    }
    static synchronized boolean bindValidatedUser(Context context, FirebaseUser user) {
        if (!previouslySignedIn(context) || !google(user)) return false;
        String expected = preferences(context).getString("signed_in_uid", "");
        if (!expected.isEmpty()) return expected.equals(user.getUid());
        preferences(context).edit().putString("signed_in_uid", user.getUid()).apply(); return true;
    }
    static synchronized void revoke(Context context) {
        consentGeneration++;
        preferences(context).edit().putBoolean("previously_signed_in", false).putBoolean("reauth_required", false)
                .remove("signed_in_uid").apply();
    }
    static synchronized FirebaseAuth auth(Context context, boolean explicitSignIn) {
        if (!explicitSignIn && !previouslySignedIn(context)) return null;
        IdentityConfig config = IdentityConfig.load(context);
        if (!config.isConfigured()) return null;
        FirebaseApp app;
        try { app = FirebaseApp.getInstance("LumaIdentity"); }
        catch (IllegalStateException absent) {
            app = FirebaseApp.initializeApp(context.getApplicationContext(), new FirebaseOptions.Builder()
                    .setApiKey(config.apiKey).setApplicationId(config.appId).setProjectId(config.projectId).build(), "LumaIdentity");
        }
        app.setDataCollectionDefaultEnabled(false);
        return FirebaseAuth.getInstance(app);
    }
    static boolean google(FirebaseUser user) {
        if (user == null || user.isAnonymous()) return false;
        for (UserInfo provider : user.getProviderData()) if ("google.com".equals(provider.getProviderId())) return true;
        return false;
    }
    static FirebaseUser currentUser(Context context) {
        try {
            FirebaseAuth auth = auth(context, false);
            FirebaseUser user = auth == null ? null : auth.getCurrentUser();
            if (!google(user)) return null;
            String expected = preferences(context).getString("signed_in_uid", "");
            return expected.isEmpty() || expected.equals(user.getUid()) ? user : null;
        } catch (RuntimeException unavailable) { return null; }
    }
}
