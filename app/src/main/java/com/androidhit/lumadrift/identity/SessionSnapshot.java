package com.androidhit.lumadrift.identity;

/** Immutable presentation state. A cached Firebase user alone is never CONNECTED. */
public final class SessionSnapshot {
    public enum State { GUEST, CONNECTED, REFRESHING, REAUTH_REQUIRED, ERROR }
    public final State state;
    public final String statusKey, uid, displayName, email;
    public final boolean configured, busy, hasGoogleAccount, signedIn;
    public final boolean canSignIn, canSignOut, canDeleteAccount;

    SessionSnapshot(State state, String key, boolean configured, boolean busy,
                    boolean hasGoogleAccount, String uid, String name, String email) {
        this.state = state; statusKey = key; this.configured = configured;
        this.busy = busy; this.hasGoogleAccount = hasGoogleAccount;
        signedIn = state == State.CONNECTED;
        // Never expose a previous profile while refreshing, disconnected or in error.
        this.uid = signedIn ? clean(uid, 128) : "";
        displayName = signedIn ? clean(name, 48) : "";
        this.email = signedIn ? clean(email, 254) : "";
        canSignIn = configured && !busy && !signedIn;
        canSignOut = hasGoogleAccount;
        canDeleteAccount = signedIn && !busy;
    }
    static String clean(String value, int limit) {
        if (value == null) return "";
        String result = value.replaceAll("[\\p{Cntrl}]", " ").trim();
        return result.length() > limit ? result.substring(0, limit) : result;
    }
}
