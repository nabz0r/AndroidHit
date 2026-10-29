package com.androidhit.lumadrift.ui;

/**
 * Clock-driven presentation state. It never changes a route, schedules work or runs an action.
 * The caller supplies an uptime clock and composites its own outgoing and incoming scene layers.
 */
public final class SceneTransition {
    public static final long DURATION_MS = 220L;
    private static final float TRAVEL = .045f;

    public enum Direction { DISSOLVE, FORWARD, BACK, LEFT, RIGHT }

    /** Offsets are fractions of the viewport width, not pixels or density-independent units. */
    public static final class Frame {
        public final boolean active;
        public final boolean blocksInput;
        public final float progress;
        public final float incomingAlpha;
        public final float outgoingAlpha;
        public final float incomingOffsetX;
        public final float outgoingOffsetX;

        private Frame(boolean active, float progress, float incomingAlpha, float outgoingAlpha,
                      float incomingOffsetX, float outgoingOffsetX) {
            this.active = active;
            this.blocksInput = active;
            this.progress = progress;
            this.incomingAlpha = incomingAlpha;
            this.outgoingAlpha = outgoingAlpha;
            this.incomingOffsetX = incomingOffsetX;
            this.outgoingOffsetX = outgoingOffsetX;
        }

        public int incomingAlpha255() { return Math.round(incomingAlpha * 255f); }
        public int outgoingAlpha255() { return Math.round(outgoingAlpha * 255f); }
    }

    private static final Frame COMPLETE = new Frame(false, 1f, 1f, 0f, 0f, 0f);
    private boolean active;
    private long startedAt;
    private long furthestElapsed;
    private long generation;
    private Direction direction = Direction.DISSOLVE;

    /**
     * Replace any previous transition. Reduced motion completes synchronously, including when a
     * previous animated transition was still running. Call only for an actual change of scene.
     */
    public void begin(long now, boolean reducedMotion, Direction direction) {
        generation++;
        this.direction = direction == null ? Direction.DISSOLVE : direction;
        startedAt = now;
        furthestElapsed = 0L;
        active = !reducedMotion;
    }

    /**
     * Querying advances presentation only. Clamping elapsed time also prevents a regressing clock
     * from reversing a fade or temporarily unlocking input. Supply the same time to every layer.
     */
    public Frame frame(long now) {
        if (!active) return COMPLETE;
        long elapsed;
        if (now <= startedAt) elapsed = 0L;
        else {
            // Saturate subtraction if an invalid clock wraps across the signed long boundary.
            elapsed = now - startedAt;
            if (elapsed < 0L) elapsed = Long.MAX_VALUE;
        }
        furthestElapsed = Math.max(furthestElapsed, elapsed);
        if (furthestElapsed >= DURATION_MS) {
            active = false;
            return COMPLETE;
        }
        float progress = furthestElapsed / (float) DURATION_MS;
        float remaining = 1f - progress;
        float eased = 1f - remaining * remaining * remaining;
        float travel = direction == Direction.DISSOLVE ? 0f :
                direction == Direction.BACK || direction == Direction.RIGHT ? -TRAVEL : TRAVEL;
        return new Frame(true, progress, eased, 1f - eased,
                travel * (1f - eased), -travel * eased * .45f);
    }

    public boolean blocksInput(long now) { return frame(now).blocksInput; }

    /** Finish on pause, detach, recreation or a blocking save error; no delayed action survives. */
    public void finish() {
        active = false;
        furthestElapsed = DURATION_MS;
        generation++;
    }

    /** Optional token for a caller's pending accessibility announcement or snapshot ownership. */
    public long generation() { return generation; }

    /**
     * Optional horizontal root-scene gesture. Eligibility must be decided on ACTION_DOWN by the
     * caller: this helper deliberately knows nothing about puzzle tiles, home pan or modal state.
     * Coordinates use the same logical scene units as the hit targets. A vertical excursion locks
     * ownership out for the remainder of the gesture, so a scrolling gesture cannot change scenes.
     */
    public static final class RootSwipe {
        public enum Result { NONE, LEFT, RIGHT }
        public static final float START_DISTANCE = 18f;
        public static final float COMMIT_DISTANCE = 64f;
        public static final float HORIZONTAL_DOMINANCE = 1.5f;
        private boolean tracking;
        private boolean horizontal;
        private float downX, downY;

        public void begin(float x, float y, boolean eligible) {
            cancel();
            if (!eligible || !finite(x) || !finite(y)) return;
            tracking = true;
            downX = x;
            downY = y;
        }

        /** True means horizontal ownership has started; cancel any pending object tap. */
        public boolean move(float x, float y) {
            if (!tracking) return false;
            if (!finite(x) || !finite(y)) { cancel(); return false; }
            float dx = Math.abs(x - downX), dy = Math.abs(y - downY);
            if (!horizontal && dy >= START_DISTANCE && dy >= dx) {
                cancel();
                return false;
            }
            if (dx >= START_DISTANCE && dx >= dy * HORIZONTAL_DOMINANCE) horizontal = true;
            return horizontal;
        }

        public Result end(float x, float y) {
            if (!move(x, y)) { cancel(); return Result.NONE; }
            float dx = x - downX, dy = Math.abs(y - downY);
            Result result = Math.abs(dx) >= COMMIT_DISTANCE &&
                    Math.abs(dx) >= dy * HORIZONTAL_DOMINANCE ?
                    dx < 0f ? Result.LEFT : Result.RIGHT : Result.NONE;
            cancel();
            return result;
        }

        public boolean isTracking() { return tracking; }
        public void cancel() { tracking = false; horizontal = false; }
        private static boolean finite(float value) {
            return !Float.isNaN(value) && !Float.isInfinite(value);
        }
    }
}
