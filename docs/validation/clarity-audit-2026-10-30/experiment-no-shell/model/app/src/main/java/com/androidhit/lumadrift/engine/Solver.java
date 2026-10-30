package com.androidhit.lumadrift.engine;

import java.util.HashSet;
import java.util.Set;

/** Exact acyclic solver: every legal action removes exactly one piece. */
public final class Solver {
    private Solver() { }

    public static int[] solve(Level level, long remaining, int gates) {
        if ((remaining & ~level.allTilesMask) != 0 || (gates & ~15) != 0) {
            throw new IllegalArgumentException("State does not belong to this level");
        }
        int[] path = new int[Long.bitCount(remaining)];
        Set<Long> dead = new HashSet<Long>();
        return search(level, remaining, gates, path, 0, dead) ? path : null;
    }

    private static boolean search(Level level, long remaining, int gates, int[] path,
                                  int depth, Set<Long> dead) {
        if (remaining == 0) return true;
        long key = (remaining << 4) | gates;
        if (dead.contains(key)) return false;
        // Certified order is an efficient search preference, never a correctness shortcut.
        for (int id : level.solution) {
            Tile tile = level.tile(id);
            if (!Board.canMove(level, remaining, gates, tile)) continue;
            path[depth] = id;
            if (search(level, remaining & ~(1L << id), gates ^ tile.switchMask,
                    path, depth + 1, dead)) return true;
        }
        dead.add(key);
        return false;
    }
}
