package com.androidhit.lumadrift.engine;

/** Exact V4 productive-cost planner: exits cost one, opening a closed gate costs one. */
public final class StrategySolver {
    private StrategySolver() { }
    /** Every frozen V4 board has at most 30,000 spatial subsets, hence <480,000 gate states. */
    public static final int MAX_CACHED_STATES = 524288;
    private static final int IMPOSSIBLE = 1000000;
    private static final Direction[] DIRECTIONS=Direction.values();

    /** One board-local bounded cache, never a process-wide collection of 1,240 plans. */
    public static final class Session {
        private final Level level;
        private final long[] obstruction;
        private long[] keys = new long[256];
        private byte[] values = new byte[256];
        private int entries;
        public Session(Level level) {
            if (level == null || level.tiles.size() > 20) throw new IllegalArgumentException("V4 planner supports at most 20 pieces");
            this.level = level;obstruction = new long[level.tiles.size()];
            for (Tile tile : level.tiles) {
                for (Tile other : level.tiles) if (other.id != tile.id && onRay(tile, other.row, other.col)) obstruction[tile.id] |= 1L << other.id;
                for (Barrier wall : level.barriers) if (onRay(tile, wall.row, wall.col)) obstruction[tile.id] |= 1L << wall.keyTileId;
            }
        }
        private static boolean onRay(Tile tile, int row, int col) {
            return tile.direction.dr == 0 ? row == tile.row && (col - tile.col) * tile.direction.dc > 0
                    : col == tile.col && (row - tile.row) * tile.direction.dr > 0;
        }
        public int cachedStates() { return entries; }
        /** Primitive arrays use at most 9 MiB at the documented cache bound. */
        public int cacheBytes() { return keys.length * Long.BYTES + values.length; }
        private static int slot(long key, int length) {
            long h = key ^ (key >>> 33);h *= 0xff51afd7ed558ccdL;h ^= h >>> 33;
            return (int) h & (length - 1);
        }
        private int get(long key) {
            int at = slot(key, keys.length);
            while (keys[at] != 0) { if (keys[at] == key) return (values[at] & 255) - 1;at = (at + 1) & (keys.length - 1); }
            return -1;
        }
        private void put(long key, int value) {
            if (value >= 254) throw new IllegalStateException("Unexpected spatially unsolvable V4 board");
            if (entries >= MAX_CACHED_STATES) throw new IllegalStateException("V4 exact planner state bound exceeded");
            if ((entries + 1) * 2 >= keys.length) grow();
            int at = slot(key, keys.length);while (keys[at] != 0 && keys[at] != key) at = (at + 1) & (keys.length - 1);
            if (keys[at] == 0) entries++;keys[at] = key;values[at] = (byte) (value + 1);
        }
        private void grow() {
            if (keys.length >= 1 << 20) return;
            long[] previousKeys = keys;byte[] previousValues = values;
            keys = new long[previousKeys.length << 1];values = new byte[keys.length];
            for (int i = 0; i < previousKeys.length; i++) if (previousKeys[i] != 0) {
                int at = slot(previousKeys[i], keys.length);while (keys[at] != 0) at = (at + 1) & (keys.length - 1);
                keys[at] = previousKeys[i];values[at] = previousValues[i];
            }
        }
        private int extra(long mask, int gates) {
            if (mask == 0) return 0;
            long key = (mask << 4) | gates;int saved = get(key);if (saved >= 0) return saved;
            int best = IMPOSSIBLE, requiredDirections = 0;boolean legal = false;
            for (Tile tile : level.tiles) if ((mask & (1L << tile.id)) != 0) {
                requiredDirections |= tile.direction.bit;
                if ((mask & obstruction[tile.id]) == 0 && (gates & tile.direction.bit) != 0) legal = true;
            }
            int lowerBound = legal ? 0 : 1;
            for (int id : level.solution) {
                Tile tile = level.tile(id);
                if ((mask & (1L << id)) == 0 || (mask & obstruction[id]) != 0 || (gates & tile.direction.bit) == 0) continue;
                best = Math.min(best, extra(mask & ~(1L << id), gates ^ tile.switchMask));
                if (best == lowerBound) { put(key, best);return best; }
            }
            for (Direction direction : DIRECTIONS) if ((gates & direction.bit) == 0 && (requiredDirections & direction.bit) != 0) {
                best = Math.min(best, 1 + extra(mask, gates | direction.bit));
                if (best == lowerBound) break;
            }
            if (best == IMPOSSIBLE) throw new IllegalStateException("V4 spatial dependency cycle");
            put(key, best);return best;
        }
        public int minimumActions(long mask, int gates) {
            if ((mask & ~level.allTilesMask) != 0 || (gates & ~15) != 0) throw new IllegalArgumentException("State outside level");
            return Long.bitCount(mask) + extra(mask, gates);
        }
        public int[] solve(long mask, int gates) {
            int length = minimumActions(mask, gates);int[] result = new int[length];int index = 0;
            while (mask != 0) {
                int value = extra(mask, gates);boolean moved = false;
                for (int id : level.solution) {
                    Tile tile = level.tile(id);
                    if ((mask & (1L << id)) != 0 && (mask & obstruction[id]) == 0 && (gates & tile.direction.bit) != 0
                            && extra(mask & ~(1L << id), gates ^ tile.switchMask) == value) {
                        result[index++] = id;mask &= ~(1L << id);gates ^= tile.switchMask;moved = true;break;
                    }
                }
                if (moved) continue;
                for (Direction direction : DIRECTIONS) if ((gates & direction.bit) == 0 && 1 + extra(mask, gates | direction.bit) == value) {
                    result[index++] = Board.gateAction(direction);gates |= direction.bit;moved = true;break;
                }
                if (!moved) throw new IllegalStateException("Exact plan reconstruction failed");
            }
            if (index != length) throw new IllegalStateException("Exact plan length changed");
            return result;
        }
    }
}
