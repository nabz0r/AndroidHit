package com.androidhit.lumadrift.engine;

/**
 * Pure per-run action budget. Every present-piece attempt (including a block)
 * and every successful undo costs one action. Selection, empty taps, an empty
 * undo, menus, hints and Vision do not; Flow pays for each actual attempt.
 * The controller validates the Board action and consumes its matching action.
 * A blocked attempt protected by an active Shield is deliberately not consumed.
 * No spending, advertising, clock or individual player signals affect policy.
 */
public final class MoveBudget {
    public static final int POLICY_VERSION = 2;
    public static final int MAX_EXTENSIONS = 2;
    public static final int GOLD_EXTENSION_COST = 100;
    private final int mode;
    private final int policyVersion;
    private final int minimum;
    private final int base;
    private int used;
    private int extensions;
    private int extraActions;

    /** Snapshot fields are policy data; the session owner binds them to its board/run identity. */
    public static final class Snapshot {
        public final int policyVersion;
        public final int mode;
        public final int minimumActions;
        public final int base;
        public final int used;
        public final int extensions;
        public final int extraActions;
        private Snapshot(MoveBudget budget) {
            policyVersion = budget.policyVersion;
            mode = budget.mode;
            minimumActions = budget.minimum;
            base = budget.base;
            used = budget.used;
            extensions = budget.extensions;
            extraActions = budget.extraActions;
        }
    }

    private MoveBudget(int mode, int minimum, int base, int policyVersion) {
        this.mode = mode;
        this.policyVersion = policyVersion;
        this.minimum = minimum;
        this.base = base;
    }

    /** 0 Journey, 1 daily, 2 weekly, 3 Remix, 4 fair unlimited friend challenge. */
    public static MoveBudget start(int mode, Level level) {
        return create(mode, level, POLICY_VERSION);
    }

    /** Finish an already-started policy-1 attempt without changing its allowance. */
    public static MoveBudget startLegacy(int mode, Level level) {
        return create(mode, level, 1);
    }

    private static MoveBudget create(int mode, Level level, int policyVersion) {
        if (level == null || mode < 0 || mode > 4) throw new IllegalArgumentException("Valid mode and level required");
        int count = policyVersion == 1 ? (mode == 3 ? LevelCatalog.REMIX_COUNT : LevelCatalog.LEVEL_COUNT)
                : mode == 3 ? BalancedCatalog.REMIX_COUNT : BalancedCatalog.LEVEL_COUNT;
        if ((mode == 0 || mode == 3) && (level.isEvent || level.number < 1 || level.number > count)) {
            throw new IllegalArgumentException("Campaign mode requires a numbered campaign board");
        }
        if ((mode == 1 || mode == 2) && !level.isEvent) throw new IllegalArgumentException("Event mode requires an event board");
        int minimum = level.tiles.size();
        // N exits are necessary because each exit removes one tile; certified content solves in N.
        int margin;
        if (policyVersion == 1) {
            if (mode == 0) margin = level.number <= 3 ? 8 : level.number <= 6 ? 6 : level.number <= 30 ? 5 : level.number <= 90 ? 4 : 3;
            else if (mode == 3) margin = level.number <= 3 ? 6 : level.number <= 12 ? 5 : level.number <= 60 ? 4 : 3;
            else margin = mode == 1 ? 5 : 4;
        } else if (mode == 0 || mode == 3) {
            margin = level.progressionVersion >= 3
                    ? BalancedCatalog.mistakeAllowance(level.number, mode == 3)
                    : BalancedCatalog.mistakeAllowanceV2(level.number, mode == 3);
        } else {
            int switches = 0, paired = 0;
            for (Tile tile : level.tiles) {
                if (tile.switchMask != 0) switches++;
                if (Integer.bitCount(tile.switchMask) > 1) paired++;
            }
            margin = 2 + (switches >= 5 || paired >= 2 ? 1 : 0) + (mode == 1 && minimum >= 17 ? 1 : 0);
        }
        return new MoveBudget(mode, minimum, mode == 4 ? 0 : minimum + margin, policyVersion);
    }

    /**
     * Restore validated counters, or null. Board history itself must have been
     * validated by Board.restoreHistory first. An undo and the corresponding
     * removed-and-restored move account for at least two consumed actions.
     * Callers must also validate the board/run identity and snapshot version.
     */
    public static MoveBudget restore(int mode, Level level, int used, int extensions,
                                     int extraActions, int boardMoves, int undos) {
        return restorePolicy(mode, level, used, extensions, extraActions, boardMoves, undos, POLICY_VERSION);
    }

    public static MoveBudget restoreLegacy(int mode, Level level, int used, int extensions,
                                           int extraActions, int boardMoves, int undos) {
        return restorePolicy(mode, level, used, extensions, extraActions, boardMoves, undos, 1);
    }

    private static MoveBudget restorePolicy(int mode, Level level, int used, int extensions,
                                            int extraActions, int boardMoves, int undos, int policyVersion) {
        MoveBudget candidate;
        try { candidate = create(mode, level, policyVersion); }
        catch (IllegalArgumentException invalid) { return null; }
        if (used < 0 || extensions < 0 || extensions > MAX_EXTENSIONS || extraActions < 0
                || boardMoves < 0 || boardMoves > candidate.minimum || undos < 0) return null;
        if (candidate.isUnlimited()) {
            return used == 0 && extensions == 0 && extraActions == 0 ? candidate : null;
        }
        int maxExtension = Math.max(5, candidate.minimum + 3);
        if (extensions == 0 && extraActions != 0) return null;
        if (extensions > 0 && (extraActions < extensions * 5
                || extraActions > extensions * maxExtension
                || used < candidate.base + (extensions - 1) * 5)) return null;
        long minimumUsed = (long) boardMoves + 2L * undos;
        if (used < minimumUsed || used > candidate.base + extraActions) return null;
        candidate.used = used;
        candidate.extensions = extensions;
        candidate.extraActions = extraActions;
        return candidate;
    }

    public int minimumActions() { return minimum; }
    public int policyVersion() { return policyVersion; }
    /** Initial errors/undo allowance, displayed separately from necessary piece exits. */
    public int mistakeAllowance() { return isUnlimited() ? 0 : base - minimum; }
    public int base() { return base; }
    public int used() { return used; }
    public int extensions() { return extensions; }
    public int extraActions() { return extraActions; }
    public int total() { return base + extraActions; }
    /** -1 means unlimited; unlimited friend actions do not accumulate a budget counter. */
    public int remaining() { return isUnlimited() ? -1 : total() - used; }
    public boolean isUnlimited() { return mode == 4; }
    public boolean canAct() { return isUnlimited() || remaining() > 0; }
    public boolean isExhausted() { return !isUnlimited() && remaining() == 0; }
    public boolean canExtend() { return isExhausted() && extensions < MAX_EXTENSIONS; }

    /** Consume once for one validated attempt/undo; never decreases below zero. */
    public boolean consumeAction() {
        if (isUnlimited()) return true;
        if (!canAct()) return false;
        used++;
        return true;
    }

    /** Enough for every remaining exit plus three mistakes; invalid/solved counts return zero. */
    public int extensionActions(int remainingPieces) {
        return remainingPieces >= 1 && remainingPieces <= minimum ? Math.max(5, remainingPieces + 3) : 0;
    }

    /**
     * Extend only an exhausted, eligible run. Advertising and gold share this
     * two-extension quota. The controller grants it only after an earned reward
     * or an idempotently persisted gold debit, and guards stale run callbacks.
     */
    public boolean extend(int remainingPieces) {
        int amount = extensionActions(remainingPieces);
        if (!canExtend() || amount == 0) return false;
        extraActions += amount;
        extensions++;
        return true;
    }

    /** Prepare an extension without mutating the active run before wallet/reward persistence. */
    public MoveBudget copy() {
        MoveBudget result = new MoveBudget(mode, minimum, base, policyVersion);
        result.used = used;
        result.extensions = extensions;
        result.extraActions = extraActions;
        return result;
    }

    public Snapshot snapshot() { return new Snapshot(this); }
}
