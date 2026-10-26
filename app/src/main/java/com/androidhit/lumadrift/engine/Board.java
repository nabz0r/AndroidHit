package com.androidhit.lumadrift.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Mutable session. Illegal actions never change gates, pieces, or undo history. */
public final class Board {
    public enum BlockReason { NONE, MISSING_TILE, CLOSED_GATE, BLOCKED_PATH }

    public static final class MoveResult {
        public final boolean success;
        public final Tile tile;
        public final BlockReason reason;
        public final int blockingTileId;
        public final int previousGateMask;
        public final int gateMask;
        public final boolean solved;

        private MoveResult(boolean success, Tile tile, BlockReason reason,
                           int blocker, int oldGates, int newGates, boolean solved) {
            this.success = success;
            this.tile = tile;
            this.reason = reason;
            this.blockingTileId = blocker;
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

    public Board(Level level) {
        if (level == null) throw new IllegalArgumentException("A level is required");
        this.level = level;
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

    /** Gate is checked before the path; UI can show the relevant closed border. */
    public BlockReason blockReason(int id) {
        Tile tile = level.tile(id);
        if (!isPresent(id)) return BlockReason.MISSING_TILE;
        if ((gates & tile.direction.bit) == 0) return BlockReason.CLOSED_GATE;
        return blockingTile(id) >= 0 ? BlockReason.BLOCKED_PATH : BlockReason.NONE;
    }

    /** Nearest present tile on this tile's ray, or -1. Independent of gate state. */
    public int blockingTile(int id) {
        Tile tile = level.tile(id);
        if (tile == null || !isPresent(id)) return -1;
        return blockingTile(level, remainingMask, tile);
    }

    public MoveResult move(int id) {
        Tile tile = level.tile(id);
        BlockReason reason = blockReason(id);
        int oldGates = gates;
        if (reason != BlockReason.NONE) {
            return new MoveResult(false, tile, reason, blockingTile(id), gates, gates, isSolved());
        }
        remainingMask &= ~(1L << id);
        gates ^= tile.switchMask;
        history.add(id);
        return new MoveResult(true, tile, BlockReason.NONE, -1, oldGates, gates, isSolved());
    }

    public boolean undo() {
        if (history.isEmpty()) return false;
        int id = history.remove(history.size() - 1);
        remainingMask |= 1L << id;
        gates ^= level.tile(id).switchMask;
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
        if (saved == null || saved.length > level.tiles.size()) return false;
        Board candidate = new Board(level);
        for (int id : saved) if (!candidate.move(id).success) return false;
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
    public int[] solve() { return Solver.solve(level, remainingMask, gates); }
    public boolean hasSolution() { return solve() != null; }
    public boolean isStuck() { return !isSolved() && legalMoves().isEmpty(); }

    /** A bonus preview never mutates the board or spends anything. */
    public boolean isSafeMove(int id) {
        Tile tile = level.tile(id);
        return canMove(id) && Solver.solve(level, remainingMask & ~(1L << id),
                gates ^ tile.switchMask) != null;
    }

    static boolean canMove(Level level, long remaining, int gateMask, Tile tile) {
        return (remaining & (1L << tile.id)) != 0
                && (gateMask & tile.direction.bit) != 0
                && blockingTile(level, remaining, tile) < 0;
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
