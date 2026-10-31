package com.androidhit.lumadrift.data;

/** Pure input gate: one effect activation/sequence at a time, with explicit active-state rejection. */
public final class PowerUsePolicy {
    public static final long DEBOUNCE_MS = 450;
    public static final int FLOW_MAX_MOVES = 3, SHIELD_CHARGES = 3, TRAIL_STEPS = 5;
    private String inFlight = "";
    private long nextAllowedAt;

    public static boolean known(String id) {
        return "hint".equals(id) || "vision".equals(id) || "flow".equals(id)
                || "shield".equals(id) || "focus".equals(id) || "trail".equals(id);
    }
    public static long durationMillis(String id) {
        if ("hint".equals(id) || "vision".equals(id)) return 8000;
        if ("focus".equals(id)) return 10000;
        if ("trail".equals(id)) return 12000;
        return 0;
    }
    /** The caller supplies its actual board/effect active state, never just an icon state. */
    public synchronized boolean tryBegin(String id, boolean effectActive, long elapsedRealtimeMs) {
        if (!known(id) || elapsedRealtimeMs < 0 || effectActive || !inFlight.isEmpty() || elapsedRealtimeMs < nextAllowedAt) return false;
        inFlight = id;
        nextAllowedAt = delayAfter(elapsedRealtimeMs);
        return true;
    }
    /** Keep Flow in flight until its final successful step or explicit interruption. */
    public synchronized void finish(String id, long elapsedRealtimeMs) {
        if (!inFlight.equals(id)) return;
        inFlight = "";
        nextAllowedAt = Math.max(nextAllowedAt, delayAfter(Math.max(0, elapsedRealtimeMs)));
    }
    /** Cancels only input ownership; GameStore cancellation handles an unactivated reservation. */
    public synchronized void cancel() { inFlight = ""; }
    public synchronized boolean isBusy() { return !inFlight.isEmpty(); }
    public synchronized boolean isBusy(String id) { return !inFlight.isEmpty() && inFlight.equals(id); }
    public synchronized String inFlightId() { return inFlight; }
    private static long delayAfter(long now) { return now > Long.MAX_VALUE - DEBOUNCE_MS ? Long.MAX_VALUE : now + DEBOUNCE_MS; }
}
