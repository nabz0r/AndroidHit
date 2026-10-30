package com.androidhit.lumadrift.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable, validated board description plus one proven completion. */
public final class Level {
    public final int number;
    /** One based campaign chapter, or zero for a standalone/event board. */
    public final int chapter;
    public final int size;
    public final int initialGateMask;
    public final List<Tile> tiles;
    /** Empty for all historical and G/H boards; explicit fixed cells in the separate P prototype. */
    public final List<Barrier> barriers;
    public final int parMoves;
    /** Exact productive-action minimum; historical rules need one exit per tile. */
    public final int minimumActions;
    public final boolean isEvent;
    public final long seed;
    /** 0: historical/event/P; 2: frozen G/H; 3: frozen J/K. Not the UI save schema. */
    public final int progressionVersion;
    /** A certified completion from the initial board. Do not mutate this array. */
    public final int[] solution;
    /** Certified action plan. V4 uses 64+direction ordinal for manual gate openings. */
    public final int[] actionSolution;
    private final Tile[] byId;
    public final long allTilesMask;

    public Level(int number, int chapter, int size, int initialGateMask,
                 List<Tile> tiles, int[] solution, boolean isEvent, long seed) {
        this(number, chapter, size, initialGateMask, tiles, solution, isEvent, seed, Collections.emptyList());
    }

    public Level(int number, int chapter, int size, int initialGateMask,
                 List<Tile> tiles, int[] solution, boolean isEvent, long seed, List<Barrier> barriers) {
        this(number, chapter, size, initialGateMask, tiles, solution, isEvent, seed, barriers, 0);
    }

    public Level(int number, int chapter, int size, int initialGateMask,
                 List<Tile> tiles, int[] solution, boolean isEvent, long seed,
                 List<Barrier> barriers, int progressionVersion) {
        this(number, chapter, size, initialGateMask, tiles, solution, isEvent, seed,
                barriers, progressionVersion, tiles.size(), solution);
    }

    public Level(int number, int chapter, int size, int initialGateMask,
                 List<Tile> tiles, int[] solution, boolean isEvent, long seed,
                 List<Barrier> barriers, int progressionVersion, int minimumActions, int[] actionSolution) {
        if (size < 2 || size > 7 || tiles.isEmpty() || tiles.size() > 49
                || (initialGateMask & ~15) != 0 || solution.length != tiles.size() || barriers == null) {
            throw new IllegalArgumentException("Invalid level definition");
        }
        this.number = number;
        this.chapter = chapter;
        this.size = size;
        this.initialGateMask = initialGateMask;
        this.isEvent = isEvent;
        this.seed = seed;
        if (progressionVersion != 0 && progressionVersion != 2 && progressionVersion != 3 && progressionVersion != 4 && progressionVersion != 5 && progressionVersion != 6)
            throw new IllegalArgumentException("Unsupported progression version");
        if (minimumActions < tiles.size() || actionSolution == null || actionSolution.length != minimumActions
                || minimumActions > tiles.size() * 5 + 4)
            throw new IllegalArgumentException("Invalid productive-action certificate");
        if (progressionVersion < 4 && (minimumActions != tiles.size()
                || !java.util.Arrays.equals(solution, actionSolution)))
            throw new IllegalArgumentException("Historical action certificate changed");
        if (progressionVersion == 6 && (tiles.size() > RelaySolver.MAX_PIECES || minimumActions != tiles.size()
                || !java.util.Arrays.equals(solution, actionSolution)))
            throw new IllegalArgumentException("Relay certificates contain exits only");
        this.progressionVersion = progressionVersion;
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
        for (Barrier barrier : barriers) {
            if (barrier == null || barrier.row >= size || barrier.col >= size
                    || barrier.keyTileId >= byId.length || occupied[barrier.row * size + barrier.col]) {
                throw new IllegalArgumentException("Invalid/overlapping barrier or missing key piece");
            }
            occupied[barrier.row * size + barrier.col] = true;
        }
        this.barriers = Collections.unmodifiableList(new ArrayList<>(barriers));
        this.solution = solution.clone();
        this.minimumActions = minimumActions;
        this.parMoves = minimumActions;
        this.actionSolution = actionSolution.clone();
        this.allTilesMask = (1L << tiles.size()) - 1L;
        if(progressionVersion>=4){
            long mask=allTilesMask;int gates=initialGateMask;
            for(int action:this.actionSolution){
                if(Board.isGateAction(action)){
                    if(progressionVersion==6)throw new IllegalArgumentException("Relay gates are controlled by pieces");
                    int bit=Board.gateDirection(action).bit;
                    if(mask==0||(gates&bit)!=0)throw new IllegalArgumentException("Illegal gate action certificate");
                    gates|=bit;
                }else{
                    Tile tile=action>=0&&action<byId.length?byId[action]:null;
                    if(tile==null||!Board.canMove(this,mask,gates,tile))throw new IllegalArgumentException("Illegal exit action certificate");
                    mask&=~(1L<<action);gates^=tile.switchMask;
                }
            }
            if(mask!=0)throw new IllegalArgumentException("Incomplete action certificate");
        }
    }

    public Tile tile(int id) {
        return id >= 0 && id < byId.length ? byId[id] : null;
    }

    public boolean isKey(int tileId) {
        for (Barrier barrier : barriers) if (barrier.keyTileId == tileId) return true;
        return false;
    }
}
