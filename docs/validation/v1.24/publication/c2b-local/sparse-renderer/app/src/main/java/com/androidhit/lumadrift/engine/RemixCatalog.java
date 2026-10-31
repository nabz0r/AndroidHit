package com.androidhit.lumadrift.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/** Independent content family: paired gate switches, with no all-four toggle. */
final class RemixCatalog {
    private static final long FAMILY_SEED = 0x52454D49584C554DL;

    private RemixCatalog() { }

    static Level get(int number) {
        long seed = FAMILY_SEED ^ (number * 0x9E3779B97F4A7C15L);
        if (number <= 12) return curated(number, seed);
        int count = Math.min(18, 13 + (number - 13) / 38);
        int switches = Math.min(7, 3 + (number - 13) / 50);
        int doubles = switches - 1;
        return generate(number, (number - 1) / 30 + 1, number <= 60 ? 5 : 6,
                count, switches, doubles, seed, false);
    }

    /**
     * Selected, frozen teaching boards: open a pair (1..3), exchange gates
     * (4..6), then reason about optional exits (7..12). Rows are row,col,side,mask.
     */
    private static Level curated(int number, long seed) {
        int gates;
        int[][] rows;
        switch (number) {
            case 1:
                gates = 10;
                rows = new int[][] {{4,1,3,0},{4,2,3,0},{4,3,3,0},{0,4,1,5},{3,4,0,0},{3,3,2,0},{3,2,1,0},{0,1,0,0},{1,1,0,0},{2,1,0,0}};
                break;
            case 2:
                gates = 5;
                rows = new int[][] {{4,0,2,0},{2,2,0,10},{4,1,3,0},{2,1,1,0},{3,1,3,0},{1,1,2,0},{0,1,2,0},{0,4,3,0},{3,4,0,0},{3,3,1,0}};
                break;
            case 3:
                gates = 12;
                rows = new int[][] {{4,4,3,0},{3,4,2,3},{2,4,2,0},{0,3,1,0},{1,3,0,0},{2,3,0,0},{2,2,1,0},{3,3,2,0},{3,2,1,0},{1,2,2,0}};
                break;
            case 4:
                gates = 6;
                rows = new int[][] {{3,4,1,0},{2,4,2,0},{0,4,2,0},{0,3,1,0},{0,2,1,10},{3,1,2,0},{4,0,3,0},{1,0,2,0},{1,1,3,0},{1,2,3,3},{2,2,0,0}};
                break;
            case 5:
                gates = 12;
                rows = new int[][] {{0,1,3,0},{0,2,3,0},{0,4,3,9},{1,4,0,0},{4,0,2,0},{3,0,2,0},{2,0,2,0},{3,4,0,10},{3,3,1,0},{2,1,3,0},{3,2,1,0}};
                break;
            case 6:
                gates = 1;
                rows = new int[][] {{2,0,0,0},{1,3,0,0},{4,0,0,0},{3,3,0,6},{4,2,2,0},{3,2,2,8},{2,1,3,0},{3,1,1,0},{4,1,3,0},{1,1,2,0},{1,2,3,0}};
                break;
            case 7:
                gates = 6;
                rows = new int[][] {{4,4,2,0},{3,4,1,0},{2,4,2,0},{1,4,2,0},{2,3,1,0},{0,3,1,5},{1,3,1,10},{3,3,0,0},{0,1,0,6},{1,1,0,0},{2,1,0,0},{2,2,3,0}};
                break;
            case 8:
                gates = 10;
                rows = new int[][] {{1,4,1,0},{4,0,3,5},{3,0,2,0},{4,1,3,10},{3,1,2,10},{3,2,3,0},{2,1,2,0},{2,0,2,0},{1,3,1,0},{2,2,2,0},{2,3,3,0},{3,3,0,0}};
                break;
            case 9:
                gates = 14;
                rows = new int[][] {{3,0,2,0},{2,0,2,0},{3,1,3,0},{2,1,3,9},{4,1,0,0},{4,2,2,10},{4,3,3,0},{3,3,2,0},{2,3,2,2},{3,2,1,0},{2,2,1,0},{1,2,2,0}};
                break;
            case 10:
                gates = 6;
                rows = new int[][] {{1,4,1,0},{4,4,2,0},{3,4,2,10},{0,4,2,3},{0,3,1,0},{1,3,0,0},{2,3,0,0},{3,3,0,0},{0,2,3,6},{1,2,0,0},{2,2,0,0},{3,2,0,6},{3,1,1,0}};
                break;
            case 11:
                gates = 5;
                rows = new int[][] {{4,3,2,0},{3,3,2,10},{2,3,2,0},{2,2,1,0},{2,1,1,9},{3,2,1,5},{0,2,0,0},{1,2,0,12},{4,2,0,0},{4,1,1,0},{3,1,3,0},{1,1,2,0},{1,3,3,0}};
                break;
            case 12:
                gates = 4;
                rows = new int[][] {{4,2,2,0},{4,1,2,9},{4,3,3,0},{3,3,2,0},{2,3,2,3},{2,1,1,0},{0,1,2,3},{0,0,0,0},{0,4,0,0},{0,3,3,2},{1,3,0,0},{1,4,0,0},{1,2,1,0}};
                break;
            default: throw new IllegalArgumentException("Curated remix is 1..12");
        }
        ArrayList<Tile> tiles = new ArrayList<Tile>();
        int[] solution = new int[rows.length];
        for (int id = 0; id < rows.length; id++) {
            int[] row = rows[id];
            tiles.add(new Tile(id, row[0], row[1], Direction.values()[row[2]], row[3]));
            solution[id] = id;
        }
        return new Level(number, 1, 5, gates, tiles, solution, false, seed);
    }

    static Level event(long seed) {
        int count = 14 + (int) ((seed ^ (seed >>> 32)) & 3L);
        return generate(0, 0, 6, count, 5, 4, seed, true);
    }

    /** Reverse construction is a constructive proof, independent of solver heuristics. */
    private static Level generate(int number, int chapter, int size, int count,
                                  int switches, int doubles, long seed, boolean event) {
        Random stream = new Random(seed ^ 0xBB67AE8584CAA73BL);
        Level best = null;
        int bestScore = Integer.MIN_VALUE;
        int attempts = 16;
        for (int attempt = 0; attempt < attempts; attempt++) {
            Random random = new Random(stream.nextLong());
            int[] types = switchTypes(count, switches, doubles, random);
            ArrayList<Tile> placed = new ArrayList<Tile>();
            int postGates = Direction.ALL_OPEN;
            boolean failed = false;
            for (int id = count - 1; id >= 0; id--) {
                Candidate selected = null;
                for (int mask = 0; mask < 15; mask++) {
                    if (Integer.bitCount(mask) != types[id]) continue;
                    int preGates = postGates ^ mask;
                    for (int row = 0; row < size; row++) {
                        for (int col = 0; col < size; col++) {
                            if (occupied(placed, row, col)) continue;
                            for (Direction direction : Direction.values()) {
                                if ((preGates & direction.bit) == 0 || !clear(placed, row, col, direction)) continue;
                                int dependencies = blocks(placed, row, col);
                                int interior = row > 0 && row < size - 1 && col > 0 && col < size - 1 ? 2 : 0;
                                double score = random.nextDouble() * 9 + dependencies * 4 + interior;
                                if (selected == null || score > selected.score) {
                                    selected = new Candidate(row, col, direction, mask, preGates, score);
                                }
                            }
                        }
                    }
                }
                if (selected == null) { failed = true; break; }
                placed.add(new Tile(id, selected.row, selected.col, selected.direction, selected.mask));
                postGates = selected.preGates;
            }
            if (failed) continue;
            Collections.sort(placed, new Comparator<Tile>() {
                public int compare(Tile a, Tile b) { return Integer.compare(a.id, b.id); }
            });
            int[] solution = new int[count];
            for (int i = 0; i < count; i++) solution[i] = i;
            Level level = new Level(number, chapter, size, postGates, placed, solution, event, seed);
            Board board = new Board(level);
            int legal = board.legalMoves().size();
            int blocked = 0, closed = 0, directions = 0;
            for (Tile tile : placed) {
                if (board.blockingTile(tile.id) >= 0) blocked++;
                if (!board.isGateOpen(tile.direction)) closed++;
                directions |= tile.direction.bit;
            }
            int score = blocked * 3 + closed + Integer.bitCount(directions) * 3;
            if (legal >= 2 && legal <= 4) score += 12;
            if (legal == 1) score -= 5;
            if (closed > 0) score += 7;
            if (score > bestScore) { best = level; bestScore = score; }
        }
        if (best == null) throw new IllegalStateException("Cannot construct remix");
        return best;
    }

    private static int[] switchTypes(int count, int switches, int doubles, Random random) {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        // Ordinary first/last actions clarify the before/after transition.
        for (int i = 1; i < count - 1; i++) ids.add(i);
        Collections.shuffle(ids, random);
        int[] types = new int[count];
        for (int i = 0; i < switches; i++) types[ids.get(i)] = i < doubles ? 2 : 1;
        return types;
    }

    private static boolean occupied(List<Tile> placed, int row, int col) {
        for (Tile tile : placed) if (tile.row == row && tile.col == col) return true;
        return false;
    }

    private static boolean clear(List<Tile> placed, int row, int col, Direction direction) {
        for (Tile tile : placed) if (onRay(row, col, direction, tile.row, tile.col)) return false;
        return true;
    }

    private static int blocks(List<Tile> placed, int row, int col) {
        int count = 0;
        for (Tile tile : placed) if (onRay(tile.row, tile.col, tile.direction, row, col)) count++;
        return count;
    }

    private static boolean onRay(int row, int col, Direction direction, int otherRow, int otherCol) {
        return direction.dr == 0 ? row == otherRow && (otherCol - col) * direction.dc > 0
                : col == otherCol && (otherRow - row) * direction.dr > 0;
    }

    private static final class Candidate {
        final int row, col, mask, preGates;
        final Direction direction;
        final double score;
        Candidate(int row, int col, Direction direction, int mask, int preGates, double score) {
            this.row = row; this.col = col; this.direction = direction;
            this.mask = mask; this.preGates = preGates; this.score = score;
        }
    }
}
