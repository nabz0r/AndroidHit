package com.androidhit.lumadrift.engine;

/** Transparent action-only grading. Time, rejected taps and information powers never affect it. */
public final class JourneyExperience {
    public static final int SCORE_VERSION = 2;

    private JourneyExperience() { }

    /** Three stars allow half of the original action margin, rounded down. */
    public static int threeStarLimit(MoveBudget budget) {
        requireBudget(budget);
        return budget.minimumActions() + (budget.policyVersion() == MoveBudget.FOCUSED_RELAY_POLICY_VERSION ? 0
                : Math.max(0, budget.base() - budget.minimumActions()) / 2);
    }

    /** Current U attempts earn two stars through half the original correction reserve. */
    public static int twoStarLimit(MoveBudget budget) {
        requireBudget(budget);
        return budget.policyVersion() == MoveBudget.FOCUSED_RELAY_POLICY_VERSION
                ? budget.minimumActions() + Math.max(1, budget.mistakeAllowance() / 2) : budget.base();
    }

    /** Policy6 retains spent exits after a return; older ledgers keep their published scoring. */
    public static int stars(MoveBudget budget) {
        requireBudget(budget);
        if (budget.isUnlimited()) return 3;
        if (budget.extensions() != 0) return 1;
        if (budget.used() <= threeStarLimit(budget)) return 3;
        return budget.used() <= twoStarLimit(budget) ? 2 : 1;
    }

    private static void requireBudget(MoveBudget budget) {
        if (budget == null) throw new IllegalArgumentException("Missing action budget");
    }
}
