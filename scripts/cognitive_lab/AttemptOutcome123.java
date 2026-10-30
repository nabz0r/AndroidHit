package cognitive_lab;

/** Terminal state takes precedence over administrative censoring in the 1.23 protocol. */
final class AttemptOutcome123 {
    private AttemptOutcome123() { }

    static String resolved(Environment env, boolean requestedStop) {
        if (env.board.isSolved()) return "completed";
        if (env.budget.isExhausted()) return "budget_exhausted";
        if (requestedStop) return "agent_give_up";
        if (env.actions >= CognitiveLab.MAX_ATTEMPT_ACTIONS) return "attempt_action_cap";
        return null;
    }

    static String atHorizon(Environment env) {
        String terminal = resolved(env, false);
        return terminal == null ? "horizon_censored" : terminal;
    }
}
