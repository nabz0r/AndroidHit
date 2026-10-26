package com.androidhit.lumadrift.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable, validated board description plus one proven completion. */
public final class Level {
    public final int number;
    /** One based campaign chapter (1..8), or zero for an event. */
    public final int chapter;
    public final int size;
    public final int initialGateMask;
    public final List<Tile> tiles;
    public final int parMoves;
    public final boolean isEvent;
    public final long seed;
    /** A certified completion from the initial board. Do not mutate this array. */
    public final int[] solution;
    private final Tile[] byId;
    public final long allTilesMask;

    public Level(int number, int chapter, int size, int initialGateMask,
                 List<Tile> tiles, int[] solution, boolean isEvent, long seed) {
        if (size < 2 || size > 7 || tiles.isEmpty() || tiles.size() > 49
                || (initialGateMask & ~15) != 0 || solution.length != tiles.size()) {
            throw new IllegalArgumentException("Invalid level definition");
        }
        this.number = number;
        this.chapter = chapter;
        this.size = size;
        this.initialGateMask = initialGateMask;
        this.isEvent = isEvent;
        this.seed = seed;
        this.tiles = Collections.unmodifiableList(new ArrayList<Tile>(tiles));
        this.byId = new Tile[tiles.size()];
        boolean[] occupied = new boolean[size * size];
        for (Tile tile : tiles) {
            if (tile.id < 0 || tile.id >= tiles.size() || byId[tile.id] != null
                    || tile.row < 0 || tile.row >= size || tile.col < 0 || tile.col >= size
                    || occupied[tile.row * size + tile.col]) {
                throw new IllegalArgumentException("Invalid or duplicate tile");
            }
            byId[tile.id] = tile;
            occupied[tile.row * size + tile.col] = true;
        }
        boolean[] used = new boolean[tiles.size()];
        for (int id : solution) {
            if (id < 0 || id >= used.length || used[id]) {
                throw new IllegalArgumentException("Invalid solution order");
            }
            used[id] = true;
        }
        this.solution = solution.clone();
        this.parMoves = tiles.size();
        this.allTilesMask = (1L << tiles.size()) - 1L;
    }

    public Tile tile(int id) {
        return id >= 0 && id < byId.length ? byId[id] : null;
    }
}
