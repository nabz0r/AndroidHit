package com.androidhit.lumadrift.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Six hand-authored, independent P challenges; no campaign rewards, timer or forced items. */
public final class SealedCatalog {
    public static final int COUNT = 6;
    private static final Level[] CACHE = new Level[COUNT];
    private SealedCatalog() { }

    public static synchronized Level get(int number) {
        if (number < 1 || number > COUNT) throw new IllegalArgumentException("Sealed challenge must be 1..6");
        if (CACHE[number - 1] == null) CACHE[number - 1] = create(number);
        return CACHE[number - 1];
    }

    private static Level create(int number) {
        int gates = 15;
        int[][] rows;
        int[] solution;
        List<Barrier> barriers;
        switch (number) {
            case 1: // One key opens the visibly obstructed horizontal path.
                rows = new int[][]{{1,4,1,0},{2,0,1,0},{4,1,0,0},{0,4,2,0}};
                solution = new int[]{0,1,2,3};
                barriers = Arrays.asList(new Barrier(2,2,0));
                break;
            case 2: // One key opens two different fixed cells.
                rows = new int[][]{{0,4,0,0},{2,0,1,0},{3,4,3,0},{4,0,2,0},{1,3,0,0}};
                solution = new int[]{0,1,2,3,4};
                barriers = Arrays.asList(new Barrier(2,2,0),new Barrier(3,2,0));
                break;
            case 3: // The second key is itself behind the first seal.
                rows = new int[][]{{0,4,0,0},{2,0,1,0},{3,4,3,0},{4,1,0,0},{1,4,1,0}};
                solution = new int[]{0,1,2,3,4};
                barriers = Arrays.asList(new Barrier(2,2,0),new Barrier(3,2,1));
                break;
            case 4: // A key opens a fixed cell and the required exit gate together.
                gates = 3;
                rows = new int[][]{{1,4,1,4},{0,2,2,0},{3,0,2,8},{4,4,3,0},{0,0,0,0}};
                solution = new int[]{0,1,2,3,4};
                barriers = Arrays.asList(new Barrier(2,2,0));
                break;
            case 5: // Taking the key too early seals the remaining downward exit forever.
                rows = new int[][]{{0,4,1,4},{4,0,2,0},{2,0,1,0},{1,3,0,0}};
                solution = new int[]{1,0,2,3};
                barriers = Arrays.asList(new Barrier(2,2,0));
                break;
            default: // Two key dependencies combined with two order-sensitive gate changes.
                rows = new int[][]{{4,1,2,0},{0,4,1,4},{2,0,1,1},{3,4,3,0},{1,0,0,0},{4,0,1,0},{1,3,0,0}};
                solution = new int[]{0,4,6,1,2,3,5};
                barriers = Arrays.asList(new Barrier(2,2,1),new Barrier(3,2,2));
                break;
        }
        ArrayList<Tile> tiles = new ArrayList<>();
        for (int id = 0; id < rows.length; id++) {
            int[] row = rows[id];
            tiles.add(new Tile(id,row[0],row[1],Direction.values()[row[2]],row[3]));
        }
        return new Level(number,0,5,gates,tiles,solution,false,0x5345414C45440000L+number,barriers);
    }
}
