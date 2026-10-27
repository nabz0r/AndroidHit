package com.androidhit.lumadrift.identity;

/** Main-thread session transitions and stale-result guard, independent of Android/Firebase. */
final class SessionTracker {
    private final boolean configured;
    private SessionSnapshot snapshot;
    private long generation;
    private boolean closed;
    private String expectedUid = "";
    SessionTracker(boolean configured) {
        this.configured = configured;
        snapshot = guestSnapshot(configured ? "account_guest" : "google_not_configured");
    }
    SessionSnapshot snapshot() { return snapshot; }
    long begin(String uid, String key, boolean hasAccount) {
        if (closed) return -1;
        expectedUid = uid == null ? "" : uid;
        snapshot = new SessionSnapshot(SessionSnapshot.State.REFRESHING, key, configured, true,
                hasAccount, "", "", "");
        return ++generation;
    }
    boolean current(long request) { return !closed && request == generation; }
    boolean connected(long request, String uid, String name, String email, boolean googleProvider, boolean anonymous) {
        if (!current(request)) return false;
        if (!googleProvider || anonymous || uid == null || uid.isEmpty()
                || (!expectedUid.isEmpty() && !expectedUid.equals(uid))) {
            failure(request, "account_google_only", false, false); return false;
        }
        snapshot = new SessionSnapshot(SessionSnapshot.State.CONNECTED, "account_signed_in", configured,
                false, true, uid, name == null || name.trim().isEmpty() ? "Google" : name, email);
        expectedUid = uid; return true;
    }
    void failure(long request, String key, boolean reauth, boolean hasAccount) {
        if (!current(request)) return;
        snapshot = new SessionSnapshot(reauth ? SessionSnapshot.State.REAUTH_REQUIRED : SessionSnapshot.State.ERROR,
                key, configured, false, hasAccount, "", "", "");
    }
    void guest(String key) {
        if (closed) return;
        generation++; expectedUid = ""; snapshot = guestSnapshot(key);
    }
    private SessionSnapshot guestSnapshot(String key) {
        return new SessionSnapshot(SessionSnapshot.State.GUEST, key, configured, false, false, "", "", "");
    }
    void close() { closed = true; generation++; }
}
