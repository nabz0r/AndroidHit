package com.androidhit.lumadrift.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/** Offline deterministic campaign and date/seed based bonus boards. */
public final class LevelCatalog {
    public static final int LEVEL_COUNT = 240;
    public static final int CHAPTER_COUNT = 8;
    public static final int LEVELS_PER_CHAPTER = 30;
    public static final int REMIX_COUNT = 240;
    private static final Level[] CACHE = new Level[LEVEL_COUNT];
    private static final Level[] REMIX_CACHE = new Level[REMIX_COUNT];

    private LevelCatalog() { }

    /** Campaign number is one based. */
    public static synchronized Level get(int number) {
        if (number < 1 || number > LEVEL_COUNT) throw new IllegalArgumentException("Level must be 1..240");
        if (CACHE[number - 1] == null) CACHE[number - 1] = campaign(number);
        return CACHE[number - 1];
    }

    /** Stable for any supplied seed, including negative values and date keys. */
    public static Level event(long seed) {
        int count = 15 + (int) ((seed ^ (seed >>> 32)) & 3L);
        return generate(0, 0, 6, count, 4, seed, true);
    }

    /**
     * Separate, save-stable dual-switch campaign. It never changes the original
     * campaign's numbers, seeds or solutions. The UI unlocks it after level 12.
     */
    public static synchronized Level remix(int number) {
        if (number < 1 || number > REMIX_COUNT) throw new IllegalArgumentException("Remix must be 1..240");
        if (REMIX_CACHE[number - 1] == null) REMIX_CACHE[number - 1] = RemixCatalog.get(number);
        return REMIX_CACHE[number - 1];
    }

    /** Deterministic dual-switch board for independent seed/solver validation. */
    public static Level remixEvent(long seed) { return RemixCatalog.event(seed); }

    private static Level campaign(int number) {
        int chapter = (number - 1) / LEVELS_PER_CHAPTER + 1;
        long seed = 0x4C554D4144524946L ^ (number * 0x9E3779B97F4A7C15L);
        if (number <= 3) return introduction(number, seed);
        int inChapter = (number - 1) % LEVELS_PER_CHAPTER;
        int count;
        if (chapter == 1) count = 7 + Math.min(3, inChapter / 8);
        else count = Math.min(18, 8 + chapter + inChapter / 10);
        int switches = number <= 5 ? 0 : Math.min(5, 1 + (number - 6) / 45);
        return generate(number, chapter, chapter >= 5 ? 6 : 5, count, switches, seed, false);
    }

    private static Level introduction(int number, long seed) {
        ArrayList<Tile> tiles = new ArrayList<Tile>();
        // All tutorial rays are immediately free: tapping teaches motion before ordering.
        add(tiles, 0, 1, Direction.UP);
        add(tiles, 1, 4, Direction.RIGHT);
        add(tiles, 4, 3, Direction.DOWN);
        add(tiles, 3, 0, Direction.LEFT);
        add(tiles, 2, 2, Direction.UP);
        if (number >= 2) add(tiles, 4, 0, Direction.LEFT);
        if (number >= 3) {
            add(tiles, 0, 4, Direction.UP);
            add(tiles, 4, 4, Direction.RIGHT);
        }
        return new Level(number, 1, 5, Direction.ALL_OPEN, tiles, order(tiles.size()), false, seed);
    }

    private static void add(List<Tile> tiles, int row, int col, Direction direction) {
        tiles.add(new Tile(tiles.size(), row, col, direction, 0));
    }

    /**
     * Build a proof backwards. A newly inserted piece can leave from the current
     * board and its required pre-exit gate state is known. Reversing the insertions
     * is consequently a legal full solution, including every gate transition.
     */
    private static Level generate(int number, int chapter, int size, int count,
                                  int switchCount, long seed, boolean event) {
        Level best = null;
        int bestScore = Integer.MIN_VALUE;
        Random stream = new Random(seed ^ (event ? 0x6A09E667F3BCC909L : 0L));
        for (int attempt = 0; attempt < 12; attempt++) {
            Random random = new Random(stream.nextLong());
            ArrayList<Tile> placed = new ArrayList<Tile>();
            boolean[] switchAt = switchLocations(count, switchCount, random);
            int postGates = Direction.ALL_OPEN;
            boolean failed = false;
            for (int id = count - 1; id >= 0; id--) {
                ArrayList<Candidate> options = new ArrayList<Candidate>();
                int maskStart = switchAt[id] ? 1 : 0;
                int maskEnd = switchAt[id] ? 15 : 0;
                for (int mask = maskStart; mask <= maskEnd; mask++) {
                    if (switchAt[id] && Integer.bitCount(mask) != 1) continue;
                    int preGates = postGates ^ mask;
                    for (int row = 0; row < size; row++) {
                        for (int col = 0; col < size; col++) {
                            if (occupied(placed, row, col)) continue;
                            for (Direction direction : Direction.values()) {
                                if ((preGates & direction.bit) == 0 || !clear(placed, row, col, direction)) continue;
                                int dependencies = blocksExisting(placed, row, col);
                                // Prefer interior positions and real ordering dependencies.
                                int interior = row > 0 && row < size - 1 && col > 0 && col < size - 1 ? 2 : 0;
                                double score = random.nextDouble() * 8 + dependencies * 3 + interior;
                                options.add(new Candidate(row, col, direction, mask, preGates, score));
                            }
                        }
                    }
                }
                if (options.isEmpty()) { failed = true; break; }
                Candidate selected = Collections.max(options, new Comparator<Candidate>() {
                    public int compare(Candidate a, Candidate b) { return Double.compare(a.score, b.score); }
                });
                placed.add(new Tile(id, selected.row, selected.col, selected.direction, selected.switchMask));
                postGates = selected.preGates;
            }
            if (failed) continue;
            Collections.sort(placed, new Comparator<Tile>() {
                public int compare(Tile a, Tile b) { return a.id - b.id; }
            });
            Level level = new Level(number, chapter, size, postGates, placed, order(count), event, seed);
            Board initial = new Board(level);
            int legal = initial.legalMoves().size();
            int blocked = 0;
            int initiallyClosed = 0;
            int directions = 0;
            for (Tile tile : placed) {
                if (initial.blockingTile(tile.id) >= 0) blocked++;
                if (!initial.isGateOpen(tile.direction)) initiallyClosed++;
                directions |= tile.direction.bit;
            }
            int score = blocked * 2 + Integer.bitCount(directions) * 2 + initiallyClosed;
            // Multiple choices invite reasoning. A single forced start remains playable.
            if (legal >= 2 && legal <= 5) score += 8;
            if (legal == 1) score -= 3;
            if (blocked >= count / 3) score += 5;
            if (switchCount > 0 && initiallyClosed > 0) score += 5;
            if (score > bestScore) { best = level; bestScore = score; }
        }
        if (best == null) throw new IllegalStateException("Could not construct solvable board");
        return best;
    }

    private static int[] order(int count) {
        int[] order = new int[count];
        for (int i = 0; i < count; i++) order[i] = i;
        return order;
    }

    private static boolean[] switchLocations(int count, int switchCount, Random random) {
        boolean[] switches = new boolean[count];
        int placed = 0;
        // Keep one ordinary final piece so the last action always has clear feedback.
        while (placed < Math.min(switchCount, count - 1)) {
            int index = random.nextInt(count - 1);
            if (!switches[index]) { switches[index] = true; placed++; }
        }
        return switches;
    }

    private static boolean occupied(List<Tile> placed, int row, int col) {
        for (Tile tile : placed) if (tile.row == row && tile.col == col) return true;
        return false;
    }

    private static boolean clear(List<Tile> placed, int row, int col, Direction direction) {
        for (Tile tile : placed) if (onRay(row, col, direction, tile.row, tile.col)) return false;
        return true;
    }

    private static int blocksExisting(List<Tile> placed, int row, int col) {
        int count = 0;
        for (Tile tile : placed) if (onRay(tile.row, tile.col, tile.direction, row, col)) count++;
        return count;
    }

    private static boolean onRay(int row, int col, Direction direction, int otherRow, int otherCol) {
        return direction.dr == 0 ? row == otherRow && (otherCol - col) * direction.dc > 0
                : col == otherCol && (otherRow - row) * direction.dr > 0;
    }

    private static final class Candidate {
        final int row, col, switchMask, preGates;
        final Direction direction;
        final double score;
        Candidate(int row, int col, Direction direction, int switchMask, int preGates, double score) {
            this.row = row; this.col = col; this.direction = direction;
            this.switchMask = switchMask; this.preGates = preGates; this.score = score;
        }
    }
}
