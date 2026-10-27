package com.androidhit.lumadrift.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Mutable session. Illegal actions never change gates, pieces, or undo history. */
public final class Board {
    public enum BlockReason { NONE, MISSING_TILE, CLOSED_GATE, BLOCKED_PATH, LOCKED_BARRIER, GATE_ALREADY_OPEN, MANUAL_GATE_UNAVAILABLE }
    private static final Direction[] DIRECTIONS=Direction.values();
    public static int gateAction(Direction direction) { if (direction == null) throw new IllegalArgumentException("Direction required");return 64 + direction.ordinal(); }
    public static boolean isGateAction(int action) { return action >= 64 && action < 68; }
    public static Direction gateDirection(int action) { if (!isGateAction(action)) throw new IllegalArgumentException("Not a gate action");return DIRECTIONS[action - 64]; }

    /** Immediate mechanical consequences only: no hidden solvability verdict or correctness oracle. */
    public static final class ActionPreview {
        public final int action,previousGateMask,gateMask,openedMask,closedMask,blockingTileId;
        public final boolean legal;
        public final BlockReason reason;
        public final Barrier blockingBarrier;
        private ActionPreview(int action,int before,int after,BlockReason reason,int blocker,Barrier wall) {
            this.action=action;previousGateMask=before;gateMask=after;openedMask=after&~before;closedMask=before&~after;
            this.reason=reason;legal=reason==BlockReason.NONE;blockingTileId=blocker;blockingBarrier=wall;
        }
    }

    /** A legal exit can still close a gate needed later; legality is not completion. */
    public enum PositionStatus { SOLVED, PLAYABLE, NEEDS_ACTIONS, DEAD_END }

    /**
     * Exact, read-only help for the current run. A value of -1 for remainingActions
     * means unlimited. Recovery includes both paid undo actions and the pieces
     * restored by those undos; it never assumes that an ad or extra actions can
     * repair a closed-gate dead end. Recompute after actions, not every frame.
     */
    public static final class Guidance {
        public final PositionStatus status;
        public final int remainingActions;
        public final int remainingPieces;
        /** Exact next exit from the current position, or -1 when solved/dead. */
        public final int hintTileId;
        /** Exact necessary exits from the current position, or -1 when dead. */
        public final int actionsToFinish;
        /** Fewest undos to reach a solvable history prefix; -1 means no prefix. */
        public final int undoSteps;
        /** Undo actions plus necessary exits after undo; -1 if no recovery exists. */
        public final int actionsToRecoverAndFinish;
        public final boolean recoveryFitsBudget;

        private Guidance(PositionStatus status, int remainingActions, int remainingPieces,
                         int hintTileId, int actionsToFinish, int undoSteps) {
            this(status,remainingActions,remainingPieces,hintTileId,actionsToFinish,undoSteps,
                    undoSteps < 0 ? -1 : remainingPieces + 2 * undoSteps);
        }
        private Guidance(PositionStatus status,int remainingActions,int remainingPieces,int hintTileId,
                         int actionsToFinish,int undoSteps,int recoveryActions) {
            this.status = status;
            this.remainingActions = remainingActions;
            this.remainingPieces = remainingPieces;
            this.hintTileId = hintTileId;
            this.actionsToFinish = actionsToFinish;
            this.undoSteps = undoSteps;
            this.actionsToRecoverAndFinish = recoveryActions;
            this.recoveryFitsBudget = actionsToRecoverAndFinish >= 0
                    && (remainingActions == -1 || remainingActions >= actionsToRecoverAndFinish);
        }

        /** A stock hint should only be spent when it can help finish this run. */
        public boolean canUseHint() { return status == PositionStatus.PLAYABLE && hintTileId >= 0; }
    }

    public static final class MoveResult {
        public final boolean success;
        public final Tile tile;
        public final BlockReason reason;
        public final int blockingTileId;
        public final Barrier blockingBarrier;
        public final int previousGateMask;
        public final int gateMask;
        public final boolean solved;

        private MoveResult(boolean success, Tile tile, BlockReason reason,
                           int blocker, Barrier barrier, int oldGates, int newGates, boolean solved) {
            this.success = success;
            this.tile = tile;
            this.reason = reason;
            this.blockingTileId = blocker;
            this.blockingBarrier = barrier;
            this.previousGateMask = oldGates;
            this.gateMask = newGates;
            this.solved = solved;
        }
    }

    public static final class Snapshot {
        public final long remainingMask;
        public final int gateMask;
        public final int moves;
        public final int remaining;
        public final boolean solved;
        public final boolean[] present;

        private Snapshot(long remainingMask, int gateMask, int moves, int count) {
            this.remainingMask = remainingMask;
            this.gateMask = gateMask;
            this.moves = moves;
            this.remaining = Long.bitCount(remainingMask);
            this.solved = remainingMask == 0;
            this.present = new boolean[count];
            for (int i = 0; i < count; i++) present[i] = (remainingMask & (1L << i)) != 0;
        }
    }

    public final Level level;
    private long remainingMask;
    private int gates;
    private final ArrayList<Integer> history = new ArrayList<Integer>();
    private final StrategySolver.Session strategy;

    public Board(Level level) {
        if (level == null) throw new IllegalArgumentException("A level is required");
        this.level = level;
        strategy = level.progressionVersion == 4 ? new StrategySolver.Session(level) : null;
        restart();
    }

    public int gateMask() { return gates; }
    public long remainingMask() { return remainingMask; }
    public int moves() { return history.size(); }
    public int remainingCount() { return Long.bitCount(remainingMask); }
    public boolean isSolved() { return remainingMask == 0; }
    public boolean canUndo() { return !history.isEmpty(); }
    public boolean isPresent(int id) {
        return level.tile(id) != null && (remainingMask & (1L << id)) != 0;
    }
    public boolean isGateOpen(Direction direction) { return (gates & direction.bit) != 0; }
    public boolean supportsManualGates() { return strategy != null; }
    public int plannerCachedStates() { return strategy == null ? 0 : strategy.cachedStates(); }
    public int plannerCacheBytes() { return strategy == null ? 0 : strategy.cacheBytes(); }
    public Snapshot snapshot() { return new Snapshot(remainingMask, gates, moves(), level.tiles.size()); }

    public List<Tile> remainingTiles() {
        ArrayList<Tile> result = new ArrayList<Tile>();
        for (Tile tile : level.tiles) if (isPresent(tile.id)) result.add(tile);
        return Collections.unmodifiableList(result);
    }

    public List<Tile> legalMoves() {
        ArrayList<Tile> result = new ArrayList<Tile>();
        for (Tile tile : level.tiles) if (canMove(tile.id)) result.add(tile);
        return result;
    }

    public boolean canMove(int id) { return blockReason(id) == BlockReason.NONE; }
    public boolean canPerformAction(int action) { return previewAction(action).legal; }
    public ActionPreview previewAction(int action) {
        if (isGateAction(action)) {
            Direction d=gateDirection(action);BlockReason reason=!supportsManualGates()?BlockReason.MANUAL_GATE_UNAVAILABLE:
                    isSolved()||isGateOpen(d)?BlockReason.GATE_ALREADY_OPEN:BlockReason.NONE;
            return new ActionPreview(action,gates,reason==BlockReason.NONE?gates|d.bit:gates,reason,-1,null);
        }
        BlockReason reason=blockReason(action);Tile tile=level.tile(action);
        return new ActionPreview(action,gates,reason==BlockReason.NONE?gates^tile.switchMask:gates,reason,
                tile==null?-1:blockingTile(action),tile==null?null:blockingBarrier(action));
    }

    /** Gate is checked before the path; UI can show the relevant closed border. */
    public BlockReason blockReason(int id) {
        Tile tile = level.tile(id);
        if (!isPresent(id)) return BlockReason.MISSING_TILE;
        if ((gates & tile.direction.bit) == 0) return BlockReason.CLOSED_GATE;
        int blocking = blockingTile(id);
        Barrier barrier = blockingBarrier(id);
        if (barrier != null && (blocking < 0 || distance(tile, barrier.row, barrier.col)
                < distance(tile, level.tile(blocking).row, level.tile(blocking).col))) return BlockReason.LOCKED_BARRIER;
        return blocking >= 0 ? BlockReason.BLOCKED_PATH : BlockReason.NONE;
    }

    /** Nearest present tile on this tile's ray, or -1. Independent of gate state. */
    public int blockingTile(int id) {
        Tile tile = level.tile(id);
        if (tile == null || !isPresent(id)) return -1;
        return blockingTile(level, remainingMask, tile);
    }

    public boolean isBarrierActive(Barrier barrier) {
        return barrier != null && level.barriers.contains(barrier) && isPresent(barrier.keyTileId);
    }

    /** Nearest still-sealed fixed cell on the tile's ray, independent of other pieces. */
    public Barrier blockingBarrier(int id) {
        Tile tile = level.tile(id);
        if (!isPresent(id)) return null;
        return blockingBarrier(level, remainingMask, tile);
    }

    public MoveResult move(int id) {
        Tile tile = level.tile(id);
        BlockReason reason = blockReason(id);
        int oldGates = gates;
        if (reason != BlockReason.NONE) {
            return new MoveResult(false, tile, reason, blockingTile(id), blockingBarrier(id), gates, gates, isSolved());
        }
        remainingMask &= ~(1L << id);
        gates ^= tile.switchMask;
        history.add(id);
        return new MoveResult(true, tile, BlockReason.NONE, -1, null, oldGates, gates, isSolved());
    }

    /** V4 gate actions are productive actions; ordinary exits preserve the historical move API. */
    public MoveResult performAction(int action) {
        if (!isGateAction(action)) return move(action);
        ActionPreview preview=previewAction(action);int before=gates;
        if (!preview.legal) return new MoveResult(false,null,preview.reason,-1,null,before,before,isSolved());
        gates=preview.gateMask;history.add(action);
        return new MoveResult(true,null,BlockReason.NONE,-1,null,before,gates,isSolved());
    }

    public boolean undo() {
        if (history.isEmpty()) return false;
        int id = history.remove(history.size() - 1);
        if (isGateAction(id)) gates &= ~gateDirection(id).bit;
        else {remainingMask |= 1L << id;gates ^= level.tile(id).switchMask;}
        return true;
    }

    public void restart() {
        remainingMask = level.allTilesMask;
        gates = level.initialGateMask;
        history.clear();
    }

    public int[] history() {
        int[] result = new int[history.size()];
        for (int i = 0; i < result.length; i++) result[i] = history.get(i);
        return result;
    }

    /** Replays an atomic validated save. Malformed saves leave the current session intact. */
    public boolean restoreHistory(int[] saved) {
        int maximum=level.tiles.size();
        if(supportsManualGates()){maximum+=4;for(Tile tile:level.tiles)maximum+=Integer.bitCount(tile.switchMask);}
        if (saved == null || saved.length > maximum) return false;
        Board candidate = new Board(level);
        for (int id : saved) if (!candidate.performAction(id).success) return false;
        remainingMask = candidate.remainingMask;
        gates = candidate.gates;
        history.clear();
        history.addAll(candidate.history);
        return true;
    }

    /** A safe next piece; -1 means solved or a position requiring undo. */
    public int hint() {
        int[] solution = solve();
        return solution == null || solution.length == 0 ? -1 : solution[0];
    }

    /** Exact completion, or null when the current position cannot be completed. */
    public int[] solve() {
        if(strategy==null)return Solver.solve(level, remainingMask, gates);
        // Every suffix of a globally shortest certificate is itself shortest.
        boolean prefix=history.size()<=level.actionSolution.length;
        for(int i=0;prefix&&i<history.size();i++)prefix=history.get(i)==level.actionSolution[i];
        if(prefix)return java.util.Arrays.copyOfRange(level.actionSolution,history.size(),level.actionSolution.length);
        return strategy.solve(remainingMask,gates);
    }
    public int minimumActionsToFinish() { return strategy==null?(hasSolution()?remainingCount():-1):strategy.minimumActions(remainingMask,gates); }
    public boolean hasSolution() { return supportsManualGates() || solve() != null; }
    public boolean isStuck() { return !isSolved() && legalMoves().isEmpty() && !supportsManualGates(); }
    /** Includes doomed positions that still have one or more legal exits. */
    public boolean isDeadEnd() { return !supportsManualGates() && !isSolved() && !hasSolution(); }

    /**
     * Distinguishes a real gate-order dead end from an otherwise solvable run
     * lacking actions. The certificate is only a search preference: hints are
     * solved again from the actual remaining pieces and gate state.
     */
    public Guidance guidance(int remainingActions) {
        if (remainingActions < -1) throw new IllegalArgumentException("Remaining actions must be -1 or nonnegative");
        int remaining = remainingCount();
        int[] solution = solve();
        if (remaining == 0) return new Guidance(PositionStatus.SOLVED, remainingActions, 0, -1, 0, 0);
        if (solution != null) {
            PositionStatus status = remainingActions >= 0 && remainingActions < solution.length
                    ? PositionStatus.NEEDS_ACTIONS : PositionStatus.PLAYABLE;
            return new Guidance(status, remainingActions, remaining, solution[0], solution.length, 0, solution.length);
        }
        long recoveredMask = remainingMask;
        int recoveredGates = gates;
        for (int index = history.size() - 1; index >= 0; index--) {
            int id = history.get(index);
            recoveredMask |= 1L << id;
            recoveredGates ^= level.tile(id).switchMask;
            if (Solver.solve(level, recoveredMask, recoveredGates) != null) {
                return new Guidance(PositionStatus.DEAD_END, remainingActions, remaining, -1, -1,
                        history.size() - index);
            }
        }
        return new Guidance(PositionStatus.DEAD_END, remainingActions, remaining, -1, -1, -1);
    }

    /** A bonus preview never mutates the board or spends anything. */
    public boolean isSafeMove(int id) {
        if(supportsManualGates())return canPerformAction(id);
        Tile tile = level.tile(id);
        return canMove(id) && Solver.solve(level, remainingMask & ~(1L << id),
                gates ^ tile.switchMask) != null;
    }

    static boolean canMove(Level level, long remaining, int gateMask, Tile tile) {
        return (remaining & (1L << tile.id)) != 0
                && (gateMask & tile.direction.bit) != 0
                && blockingTile(level, remaining, tile) < 0
                && blockingBarrier(level, remaining, tile) == null;
    }

    private static int distance(Tile tile, int row, int col) {
        return Math.abs(tile.row - row) + Math.abs(tile.col - col);
    }

    private static Barrier blockingBarrier(Level level, long remaining, Tile tile) {
        Barrier closest = null;
        int closestDistance = Integer.MAX_VALUE;
        for (Barrier barrier : level.barriers) {
            if ((remaining & (1L << barrier.keyTileId)) == 0) continue;
            int dr = barrier.row - tile.row, dc = barrier.col - tile.col;
            boolean onRay = tile.direction.dr == 0 ? dr == 0 && dc * tile.direction.dc > 0
                    : dc == 0 && dr * tile.direction.dr > 0;
            int distance = Math.abs(dr) + Math.abs(dc);
            if (onRay && distance < closestDistance) { closest = barrier; closestDistance = distance; }
        }
        return closest;
    }

    static int blockingTile(Level level, long remaining, Tile tile) {
        int closest = -1;
        int distance = Integer.MAX_VALUE;
        for (Tile other : level.tiles) {
            if (other.id == tile.id || (remaining & (1L << other.id)) == 0) continue;
            int dr = other.row - tile.row;
            int dc = other.col - tile.col;
            boolean onRay = tile.direction.dr == 0
                    ? dr == 0 && dc * tile.direction.dc > 0
                    : dc == 0 && dr * tile.direction.dr > 0;
            int steps = Math.abs(dr) + Math.abs(dc);
            if (onRay && steps < distance) { closest = other.id; distance = steps; }
        }
        return closest;
    }
}
