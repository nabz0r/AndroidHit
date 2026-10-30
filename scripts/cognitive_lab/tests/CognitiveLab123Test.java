package cognitive_lab;

import com.androidhit.lumadrift.engine.*;
import java.nio.file.*;

/** Regression fixtures execute real budget transitions; solver use here is fixture-only. */
public final class CognitiveLab123Test {
    static int checks;
    static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }

    static void exit(Environment env, int id) {
        Environment.Result result = env.apply(Agent122.decision(id, "regression_fixture"),
                env.observation(env.seconds, 10000), env.seconds, 1);
        check(result.kind.equals("exit"), "fixture commits an actual native exit");
    }

    static void undo(Environment env) {
        Environment.Result result = env.apply(Agent122.decision(CognitiveAgent.UNDO, "regression_fixture"),
                env.observation(env.seconds, 10000), env.seconds, 1);
        check(result.kind.equals("undo"), "fixture commits actual non-refunding Undo");
    }

    static Environment exhaustedUnsolved() {
        Environment env = new Environment(1, 1, 0);
        int first = env.board.solve()[0];
        while (env.budget.remaining() > 0) {
            exit(env, first);
            if (env.budget.remaining() > 0) undo(env);
        }
        check(!env.board.isSolved() && env.budget.remaining() == 0,
                "last productive action exhausts budget with a piece remaining");
        return env;
    }

    static void horizonRegression() {
        Environment exhausted = exhaustedUnsolved();
        check(AttemptOutcome123.resolved(exhausted, false).equals("budget_exhausted"),
                "classify immediately before daily utility/time/cap can stop the loop");
        check(AttemptOutcome123.atHorizon(exhausted).equals("budget_exhausted"),
                "final-day zero budget cannot be mislabeled horizon_censored");
        exhausted.actions = CognitiveLab.MAX_ATTEMPT_ACTIONS;
        check(AttemptOutcome123.resolved(exhausted, true).equals("budget_exhausted"),
                "real exhaustion takes precedence over coincident cap or requested stop");
        Environment open = new Environment(1, 1, 0);
        check(AttemptOutcome123.resolved(open, false) == null, "open attempt remains open");
        check(AttemptOutcome123.atHorizon(open).equals("horizon_censored"),
                "positive-budget unfinished attempt remains genuinely censored");
        check(AttemptOutcome123.resolved(open, true).equals("agent_give_up"), "explicit stop distinct");
        open.actions = CognitiveLab.MAX_ATTEMPT_ACTIONS;
        check(AttemptOutcome123.atHorizon(open).equals("attempt_action_cap"), "administrative cap distinct");

        Environment solved = new Environment(1, 1, 0);
        int first = solved.board.solve()[0];
        for (int n = 0; n < solved.budget.mistakeAllowance(); n++) { exit(solved, first); undo(solved); }
        for (int id : solved.board.solve()) exit(solved, id);
        check(solved.board.isSolved() && solved.budget.remaining() == 0, "last allowed exit wins");
        check(AttemptOutcome123.atHorizon(solved).equals("completed"), "victory precedes zero-budget failure");
    }

    static void nativeFactory() {
        for (int level = 1; level <= 1000; level++) {
            Environment123 current = new Environment123(level, 1, 0, "manual_native_123");
            check(current.shells.totalLayers() == 0, "current factory no preparation U" + level);
            check(current.row122("fixture",0,0,"horizon_censored").get("scenario").equals("manual_native_123"),
                    "export condition differs from internal cue policy U" + level);
            Environment122 historical = new Environment122(level, 1, 0, "control_v121");
            check(historical.shells.totalLayers() == (level <= 10 ? 0 : 5),
                    "historical factory remains explicit U" + level);
        }
    }

    static void boundary(Path root) throws Exception {
        for (String name : new String[]{"CognitiveAgent", "Agent122"}) {
            String policy = Files.readString(root.resolve("scripts/cognitive_lab/" + name + ".java"));
            for (String forbidden : new String[]{"import com.androidhit", ".solve(", ".hint(", ".guidance(", ".solution", ".isSafeMove("})
                check(!policy.contains(forbidden), "bounded policy has no oracle " + name + forbidden);
        }
    }

    public static void main(String[] args) throws Exception {
        horizonRegression(); nativeFactory(); boundary(Paths.get(args[0]));
        System.out.println(Json.encode(Json.obj("passed", true, "checks", checks,
                "scope", "Immediate terminal classification including final-day regression, solved-at-zero priority, genuine censoring, 1000 current/historical factory boundaries and no solver in bounded policy.")));
    }
}
