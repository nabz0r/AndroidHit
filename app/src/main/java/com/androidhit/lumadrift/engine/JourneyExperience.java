package com.androidhit.lumadrift.engine;

/** Transparent action-only grading. The caller invokes this only for a solved board. */
public final class JourneyExperience {
    public static final int SCORE_VERSION = 1;

    private JourneyExperience() { }

    /** Three stars allow half of the original action margin, rounded down. */
    public static int threeStarLimit(MoveBudget budget) {
        requireBudget(budget);
        return budget.minimumActions() + Math.max(0, budget.base() - budget.minimumActions()) / 2;
    }

    /** Hints, time and Undo count do not enter the score; refunding Undo restores actions. */
    public static int stars(MoveBudget budget) {
        requireBudget(budget);
        if (budget.isUnlimited()) return 3;
        if (budget.extensions() != 0) return 1;
        if (budget.used() <= threeStarLimit(budget)) return 3;
        return budget.used() <= budget.base() ? 2 : 1;
    }

    private static void requireBudget(MoveBudget budget) {
        if (budget == null) throw new IllegalArgumentException("Missing action budget");
    }
}
