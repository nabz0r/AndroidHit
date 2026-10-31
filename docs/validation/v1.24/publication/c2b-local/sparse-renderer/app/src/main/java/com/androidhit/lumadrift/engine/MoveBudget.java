package com.androidhit.lumadrift.engine;

/**
 * Pure per-run action budget. Policy4 charges productive actions, rejects blocked
 * taps for free, and permits one refunded return per attempt. Historical policies
 * retain their own accounting. Selection, menus and previews never spend actions.
 * Flow pays for each committed action. Board, ledger and return count save together.
 * No spending, advertising, clock or individual player signals affect policy.
 */
public final class MoveBudget {
    public static final int POLICY_VERSION = 2;
    public static final int REVERSIBLE_POLICY_VERSION = 3;
    /** Real action limit, free rejected taps, one refunded correction per attempt. */
    public static final int DELIBERATE_POLICY_VERSION = 4;
    /** U relay route: rejected taps are free and every real exit can be undone and refunded. */
    public static final int RELAY_POLICY_VERSION = 5;
    /** New U attempts: rejected taps and Undo are free, but a spent exit is not refunded. */
    public static final int FOCUSED_RELAY_POLICY_VERSION = 6;
    public static final int UNDO_LIMIT = 1;
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
        return create(mode, level, level != null && level.progressionVersion == 6 ? FOCUSED_RELAY_POLICY_VERSION : level != null && level.progressionVersion == 5 ? DELIBERATE_POLICY_VERSION
                : level != null && level.progressionVersion == 4 ? REVERSIBLE_POLICY_VERSION : POLICY_VERSION);
    }
    public static MoveBudget startDeliberate(int mode, Level level) { return create(mode, level, DELIBERATE_POLICY_VERSION); }
    /** New attempts on preserved content may opt into free, refunding Undo without changing its geometry. */
    public static MoveBudget startReversible(int mode,Level level) { return create(mode,level,REVERSIBLE_POLICY_VERSION); }

    /** Finish an already-started policy-1 attempt without changing its allowance. */
    public static MoveBudget startLegacy(int mode, Level level) {
        return create(mode, level, 1);
    }

    private static MoveBudget create(int mode, Level level, int policyVersion) {
        if (level == null || mode < 0 || mode > 4) throw new IllegalArgumentException("Valid mode and level required");
        if((level.progressionVersion==6)!=(policyVersion==RELAY_POLICY_VERSION||policyVersion==FOCUSED_RELAY_POLICY_VERSION))
            throw new IllegalArgumentException("U boards require the distinct relay budget policy");
        int count = policyVersion == 1 ? (mode == 3 ? LevelCatalog.REMIX_COUNT : LevelCatalog.LEVEL_COUNT)
                : mode == 3 ? BalancedCatalog.REMIX_COUNT : BalancedCatalog.LEVEL_COUNT;
        if ((mode == 0 || mode == 3) && (level.isEvent || level.number < 1 || level.number > count)) {
            throw new IllegalArgumentException("Campaign mode requires a numbered campaign board");
        }
        if ((mode == 1 || mode == 2) && !level.isEvent) throw new IllegalArgumentException("Event mode requires an event board");
        int minimum = policyVersion >= REVERSIBLE_POLICY_VERSION ? level.minimumActions : level.tiles.size();
        // N exits are necessary because each exit removes one tile; certified content solves in N.
        int margin;
        if (policyVersion == 1) {
            if (mode == 0) margin = level.number <= 3 ? 8 : level.number <= 6 ? 6 : level.number <= 30 ? 5 : level.number <= 90 ? 4 : 3;
            else if (mode == 3) margin = level.number <= 3 ? 6 : level.number <= 12 ? 5 : level.number <= 60 ? 4 : 3;
            else margin = mode == 1 ? 5 : 4;
        } else if (mode == 0 && level.progressionVersion == 6) {
            margin = RelayCatalog.allowance(level.number);
        } else if (mode == 0 && level.progressionVersion == 5) {
            margin = LearningCatalog.allowance(level.number);
        } else if ((mode == 0 || mode == 3) && level.progressionVersion == 4) {
            margin = StrategyCatalog.allowance(level.number,mode==3);
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
        return restorePolicy(mode, level, used, extensions, extraActions, boardMoves, undos,
                level != null && level.progressionVersion == 6 ? FOCUSED_RELAY_POLICY_VERSION : level != null && level.progressionVersion == 5 ? DELIBERATE_POLICY_VERSION
                        : level != null && level.progressionVersion == 4 ? REVERSIBLE_POLICY_VERSION : POLICY_VERSION);
    }
    /** Explicit version routing preserves policy-1/2 saves; no guessing from the current release. */
    public static MoveBudget restoreVersion(int policy,int mode,Level level,int used,int extensions,
                                            int extraActions,int boardActions,int undos) {
        if(policy<1||policy>FOCUSED_RELAY_POLICY_VERSION)return null;
        if(level!=null&&level.progressionVersion>=4&&policy<REVERSIBLE_POLICY_VERSION)return null;
        if(level!=null&&level.progressionVersion==5&&policy!=DELIBERATE_POLICY_VERSION)return null;
        if(level!=null&&level.progressionVersion==6&&policy!=RELAY_POLICY_VERSION&&policy!=FOCUSED_RELAY_POLICY_VERSION)return null;
        if((policy==RELAY_POLICY_VERSION||policy==FOCUSED_RELAY_POLICY_VERSION)&&(level==null||level.progressionVersion!=6))return null;
        return restorePolicy(mode,level,used,extensions,extraActions,boardActions,undos,policy);
    }
    /** Upgrade the ledger, retaining geometry and earned extensions. Rejected taps have no history entry. */
    public static MoveBudget toDeliberate(int mode, Level level, MoveBudget previous, int boardActions, int undos) {
        // Policy1 has different historical allowances: finish it intact, then start a new policy4 attempt.
        if (previous == null || previous.policyVersion == 1 || boardActions < 0 || undos < 0) return null;
        MoveBudget candidate = restorePolicy(mode, level, previous.isUnlimited() ? 0 : boardActions,
                previous.extensions, previous.extraActions, boardActions, undos, DELIBERATE_POLICY_VERSION);
        return candidate;
    }
    /** Deterministic policy-2 upgrade: retain its base/extensions; refund historical Undo overhead only. */
    public static MoveBudget toReversible(int mode,Level level,int used,int extensions,int extraActions,int boardActions,int undos) {
        MoveBudget old=restorePolicy(mode,level,used,extensions,extraActions,boardActions,undos,2);
        if(old==null)return null;
        if(old.isUnlimited())return restorePolicy(mode,level,0,0,0,boardActions,undos,3);
        long refunded=(long)used-2L*undos;
        if(refunded<0||refunded>Integer.MAX_VALUE)return null;
        MoveBudget result=restorePolicy(mode,level,(int)refunded,extensions,extraActions,boardActions,undos,3);
        return result!=null&&result.base==old.base?result:null;
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
                || boardMoves < 0 || (policyVersion<3&&boardMoves>candidate.minimum) || undos < 0) return null;
        if((policyVersion==RELAY_POLICY_VERSION||policyVersion==FOCUSED_RELAY_POLICY_VERSION)&&boardMoves>level.tiles.size())return null;
        if (candidate.isUnlimited()) {
            return used == 0 && extensions == 0 && extraActions == 0 ? candidate : null;
        }
        int maxExtension = Math.max(5, policyVersion>=3?candidate.minimum*3+7:candidate.minimum+3);
        if (extensions == 0 && extraActions != 0) return null;
        if (extensions > 0 && (extraActions < extensions * 5
                || extraActions > extensions * maxExtension
                || policyVersion<3&&used<candidate.base+(extensions-1)*5)) return null;
        long minimumUsed = policyVersion>=3?boardMoves:(long)boardMoves+2L*undos;
        if (policyVersion == FOCUSED_RELAY_POLICY_VERSION) {
            if ((long)used != (long)boardMoves + undos) return null;
        } else if (policyVersion >= DELIBERATE_POLICY_VERSION && used != boardMoves) return null;
        if (used < minimumUsed || used > candidate.base + extraActions) return null;
        candidate.used = used;
        candidate.extensions = extensions;
        candidate.extraActions = extraActions;
        return candidate;
    }

    public int minimumActions() { return minimum; }
    public int policyVersion() { return policyVersion; }
    public boolean rejectsAreFree() { return policyVersion >= DELIBERATE_POLICY_VERSION; }
    public int undoRemaining(int usedUndos) { return policyVersion == DELIBERATE_POLICY_VERSION ? Math.max(0, UNDO_LIMIT - usedUndos) : Integer.MAX_VALUE; }
    public boolean refundsUndo() { return policyVersion >= REVERSIBLE_POLICY_VERSION && policyVersion != FOCUSED_RELAY_POLICY_VERSION; }
    /** The ledger owns the atomic Undo operation, including non-refunding policy6. */
    public boolean managesUndo() { return policyVersion >= REVERSIBLE_POLICY_VERSION; }
    public boolean isRelayPolicy() { return policyVersion == RELAY_POLICY_VERSION || policyVersion == FOCUSED_RELAY_POLICY_VERSION; }
    public boolean supportsEarnedContinuation() { return !rejectsAreFree() || policyVersion == FOCUSED_RELAY_POLICY_VERSION; }
    /** Policy 4 commits a real action or returns the physical refusal without charging it. */
    public Board.MoveResult attempt(Board board, int action) {
        if (!rejectsAreFree()) throw new IllegalStateException("Use the historical controller for preserved policies");
        if (board == null || !canAct()) return null;
        Board.MoveResult result = board.performAction(action);
        if (result.success && !consumeAction()) throw new IllegalStateException("Action allowance changed during commit");
        return result;
    }
    /** Validate quota before changing either board or ledger. Caller persists usedUndos + 1 together. */
    public boolean undo(Board board, int usedUndos) {
        if (board == null || board.level == null || usedUndos < 0 || undoRemaining(usedUndos) == 0
                || !board.canUndo() || !managesUndo() || (!isUnlimited() && used <= 0)) return false;
        if (!board.undo()) return false;
        return policyVersion == FOCUSED_RELAY_POLICY_VERSION || refundAction();
    }
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
    /** Refund exactly the productive action just undone. The controller must first undo a real action. */
    public boolean refundAction() {
        if(!refundsUndo())return false;
        if(isUnlimited())return true;
        if(used<=0)return false;
        used--;return true;
    }

    /** Enough for every remaining exit plus three mistakes; invalid/solved counts return zero. */
    public int extensionActions(int remainingPieces) {
        int max=policyVersion>=3?minimum*3+4:minimum;
        return remainingPieces >= 1 && remainingPieces <= max ? Math.max(5, remainingPieces + 3) : 0;
    }

    /**
     * Extend only an exhausted, eligible run. Advertising and gold share this
     * two-extension quota. The controller grants it only after an earned reward
     * or an idempotently persisted gold debit, and guards stale run callbacks.
     */
    public boolean extend(int remainingPieces) {
        if (!canExtend()) return false;
        return settleEarnedExtension(extensions + 1, remainingPieces);
    }

    /** A previously earned receipt must settle even if a migration refunded rejected taps. */
    public boolean settleEarnedExtension(int ordinal, int remainingPieces) {
        int amount = extensionActions(remainingPieces);
        if (isUnlimited() || ordinal != extensions + 1 || extensions >= MAX_EXTENSIONS || amount == 0) return false;
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
