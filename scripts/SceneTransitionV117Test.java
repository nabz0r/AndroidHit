import com.androidhit.lumadrift.ui.SceneTransition;
import com.androidhit.lumadrift.ui.SceneTransition.Direction;
import com.androidhit.lumadrift.ui.SceneTransition.Frame;
import com.androidhit.lumadrift.ui.SceneTransition.RootSwipe;

/** Contracts for real route transitions and gesture ownership, independent of Android drawing. */
public final class SceneTransitionV117Test {
    private static int checks;
    private static void check(boolean value, String note) {
        checks++;
        if (!value) throw new AssertionError(note);
    }
    private static void near(float actual, float expected, String note) {
        check(Math.abs(actual - expected) < .00001f, note + ": " + actual + " != " + expected);
    }
    private static void complete(Frame frame) {
        check(!frame.active && !frame.blocksInput, "completed scene accepts input");
        near(frame.progress, 1f, "completed progress");
        near(frame.incomingAlpha, 1f, "destination fully visible");
        near(frame.outgoingAlpha, 0f, "old scene fully absent");
        near(frame.incomingOffsetX, 0f, "destination has no lingering offset");
        near(frame.outgoingOffsetX, 0f, "no outgoing translation after completion");
    }
    private static void transitionContracts() {
        SceneTransition state = new SceneTransition();
        complete(state.frame(500));
        for (Direction direction : Direction.values()) {
            state.begin(1000, false, direction);
            Frame first = state.frame(1000);
            check(first.active && first.blocksInput, "new animated scene blocks input");
            near(first.incomingAlpha, 0f, "old scene is initially visible");
            near(first.outgoingAlpha, 1f, "outgoing starts opaque");
            float priorAlpha = -1f, priorProgress = -1f;
            for (int ms = 0; ms < SceneTransition.DURATION_MS; ms++) {
                Frame frame = state.frame(1000 + ms);
                check(frame.active && frame.blocksInput, "all animation samples block input");
                check(frame.incomingAlpha >= priorAlpha, "destination never fades backwards");
                check(frame.progress >= priorProgress, "progress is monotone");
                check(frame.incomingAlpha >= 0f && frame.incomingAlpha <= 1f, "bounded incoming opacity");
                check(frame.outgoingAlpha >= 0f && frame.outgoingAlpha <= 1f, "bounded outgoing opacity");
                near(frame.incomingAlpha + frame.outgoingAlpha, 1f, "crossfade has complementary weights");
                check(frame.incomingAlpha255() >= 0 && frame.incomingAlpha255() <= 255, "Canvas alpha bounds");
                if (direction == Direction.DISSOLVE) {
                    near(frame.incomingOffsetX, 0f, "dissolve has no motion");
                    near(frame.outgoingOffsetX, 0f, "dissolve has no outgoing motion");
                } else {
                    float sign = direction == Direction.BACK || direction == Direction.RIGHT ? -1f : 1f;
                    check(sign * frame.incomingOffsetX >= 0f, "incoming follows direction");
                    check(sign * frame.outgoingOffsetX <= 0f, "outgoing recedes in matching direction");
                    check(Math.abs(frame.incomingOffsetX) <= .04501f, "travel limited to 4.5 percent of viewport");
                }
                priorAlpha = frame.incomingAlpha;
                priorProgress = frame.progress;
            }
            complete(state.frame(1220));
            complete(state.frame(1100));
        }
        state.begin(2000, false, Direction.FORWARD);
        Frame midpoint = state.frame(2110);
        near(state.frame(2050).progress, midpoint.progress, "clock regression cannot reverse active transition");
        check(state.blocksInput(2050), "clock regression cannot unlock transition");
        long generation = state.generation();
        state.begin(2110, false, Direction.BACK);
        check(state.generation() > generation, "replacement invalidates old announcement token");
        near(state.frame(2110).progress, 0f, "replacement begins a new presentation timeline");
        check(state.frame(2210).active, "replacement does not inherit old deadline");
        complete(state.frame(2330));

        state.begin(3000, false, Direction.LEFT);
        state.frame(3040);
        generation = state.generation();
        state.finish();
        check(state.generation() > generation, "lifecycle completion invalidates token");
        complete(state.frame(3040));
        state.begin(4000, true, Direction.FORWARD);
        complete(state.frame(4000));
        complete(state.frame(3999));
        state.begin(5000, false, null);
        near(state.frame(5000).incomingOffsetX, 0f, "unknown direction uses dissolve");
        state.begin(Long.MIN_VALUE, false, Direction.DISSOLVE);
        complete(state.frame(Long.MAX_VALUE));
        state.begin(Long.MAX_VALUE - 20, false, Direction.DISSOLVE);
        check(state.frame(Long.MAX_VALUE).active, "near-maximum valid timestamp remains active");

        SceneTransition fast = new SceneTransition(), slow = new SceneTransition();
        fast.begin(100, false, Direction.LEFT);slow.begin(100, false, Direction.LEFT);
        for (long time = 100; time < 210; time += 7) fast.frame(time);
        near(fast.frame(210).incomingAlpha, slow.frame(210).incomingAlpha, "fade does not depend on draw cadence");
    }
    private static void swipeContracts() {
        RootSwipe swipe = new RootSwipe();
        swipe.begin(30, 70, false);
        check(!swipe.isTracking(), "puzzle, object, modal and home-pan owners refuse navigation");
        check(swipe.end(200, 70) == RootSwipe.Result.NONE, "ineligible swipe cannot navigate");
        swipe.begin(40, 70, true);
        check(!swipe.move(50, 70), "small jitter remains a tap");
        check(swipe.end(50, 70) == RootSwipe.Result.NONE, "small release does not navigate");
        swipe.begin(200, 100, true);
        check(swipe.move(160, 105), "clear horizontal drag takes ownership");
        check(swipe.end(100, 110) == RootSwipe.Result.LEFT, "leftward root navigation");
        check(!swipe.isTracking(), "release ends ownership");
        check(swipe.end(300, 100) == RootSwipe.Result.NONE, "second release cannot navigate twice");
        swipe.begin(30, 80, true);
        check(swipe.end(110, 83) == RootSwipe.Result.RIGHT, "rightward root navigation");
        swipe.begin(30, 80, true);
        check(swipe.end(80, 83) == RootSwipe.Result.NONE, "short horizontal drag is safely cancelled");
        swipe.begin(30, 80, true);
        check(!swipe.move(33, 110), "vertical scrolling takes ownership first");
        check(!swipe.isTracking(), "vertical gesture locks out scene navigation");
        check(swipe.end(200, 110) == RootSwipe.Result.NONE, "vertical-then-horizontal scrolling cannot navigate");
        swipe.begin(30, 80, true);
        check(swipe.end(100, 140) == RootSwipe.Result.NONE, "ambiguous diagonal gesture does not navigate");
        swipe.begin(30, 80, true);swipe.move(75, 80);swipe.cancel();
        check(swipe.end(160, 80) == RootSwipe.Result.NONE, "system cancellation discards gesture");
        swipe.begin(Float.NaN, 80, true);
        check(!swipe.isTracking(), "invalid down coordinate refused");
        swipe.begin(30, Float.POSITIVE_INFINITY, true);
        check(!swipe.isTracking(), "infinite down coordinate refused");
        swipe.begin(30, 80, true);
        check(!swipe.move(Float.NEGATIVE_INFINITY, 80), "invalid movement cancels gesture");
        check(swipe.end(200, 80) == RootSwipe.Result.NONE, "invalid gesture cannot later navigate");
        for (float width : new float[]{320, 390, 600}) {
            swipe.begin(width * .5f, 100, true);
            check(swipe.end(width * .5f - RootSwipe.COMMIT_DISTANCE, 100) == RootSwipe.Result.LEFT,
                    "threshold is inclusive in logical coordinates");
        }
    }
    public static void main(String[] args) {
        transitionContracts();swipeContracts();
        System.out.println("PASS " + checks + " scene transition and root gesture contracts; native compositing and route review still required");
    }
}
