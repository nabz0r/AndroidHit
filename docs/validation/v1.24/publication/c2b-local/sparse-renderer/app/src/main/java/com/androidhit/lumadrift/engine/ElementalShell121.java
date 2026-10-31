package com.androidhit.lumadrift.engine;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Attempt-local shells around otherwise unchanged U pieces. Shell taps are not
 * Board exits and spend no MoveBudget action. Callers persist this state beside
 * the attempt, keep broken shells through Undo, and start afresh on a new run.
 */
public final class ElementalShell121 {
    public enum Kind { NONE, ICE, FIRE, WATER }
    public enum TapResult { NONE, CRACKED, CLEARED }

    private static final String PREFIX = "ES121";
    private final String identity;
    private final Kind[] kinds;
    private final int[] layers;

    private ElementalShell121(Level level) {
        if (level == null) throw new IllegalArgumentException("Level required");
        identity = identity(level);
        kinds = new Kind[level.tiles.size()];
        layers = new int[kinds.length];
        java.util.Arrays.fill(kinds, Kind.NONE);
    }

    /** New campaign/expedition U11+ only; legacy/event/friend runs stay bare. */
    public static ElementalShell121 create(Level level, int mode, int index) {
        ElementalShell121 value = empty(level);
        if ((mode != 0 && mode != 5) || level.progressionVersion != 6) return value;
        if (index != level.number) throw new IllegalArgumentException("U index must match the level");
        if (index <= 10) return value;
        assign(level, value);
        return value;
    }

    /**
     * New 1.23 Journey attempts start without preparation taps. The historical
     * factory and ES121 decoder stay intact for saved runs and reproducible
     * experiments; expedition and other modes retain their existing contract.
     */
    public static ElementalShell121 createJourney123(Level level, int mode, int index) {
        if (level != null && mode == 0 && level.progressionVersion == 6) {
            if (index != level.number) throw new IllegalArgumentException("U index must match the level");
            return empty(level);
        }
        return create(level, mode, index);
    }

    /** Used for historical saved attempts and a caller's rejected-code fallback. */
    public static ElementalShell121 empty(Level level) {
        return new ElementalShell121(level);
    }

    public Kind kind(int tileId) {
        return valid(tileId) && layers[tileId] > 0 ? kinds[tileId] : Kind.NONE;
    }

    public int layers(int tileId) {
        return valid(tileId) ? layers[tileId] : 0;
    }

    /** Exactly one purposeful free tap removes exactly one existing layer. */
    public TapResult tap(int tileId) {
        if (!valid(tileId) || layers[tileId] == 0) return TapResult.NONE;
        layers[tileId]--;
        return layers[tileId] == 0 ? TapResult.CLEARED : TapResult.CRACKED;
    }

    /** Call only after a certified companion action actually removed this ID. */
    public boolean clear(int tileId) {
        if (!valid(tileId) || layers[tileId] == 0) return false;
        layers[tileId] = 0;
        return true;
    }

    public int totalLayers() {
        int total = 0;
        for (int value : layers) total += value;
        return total;
    }

    public int remainingCount() {
        int count = 0;
        for (int value : layers) if (value > 0) count++;
        return count;
    }

    /** Canonical positive-layer rows; an omitted row means already cleared. */
    public String encode() {
        StringBuilder text = new StringBuilder(PREFIX).append('|').append(identity)
                .append('|').append(layers.length).append('|');
        boolean first = true;
        for (int id = 0; id < layers.length; id++) if (layers[id] > 0) {
            if (!first) text.append(';');
            text.append(id).append(':').append(kinds[id].name()).append(':').append(layers[id]);
            first = false;
        }
        return text.toString();
    }

    /**
     * Strict decoder: no partial restore. Full puzzle identity, canonical IDs,
     * assigned element and remaining layers must all match. Invalid data is null.
     */
    public static ElementalShell121 decode(Level level, String encoded) {
        if (level == null || encoded == null || encoded.length() > 1024) return null;
        String[] fields = encoded.split("\\|", -1);
        if (fields.length != 4 || !PREFIX.equals(fields[0])) return null;
        ElementalShell121 value = empty(level);
        if (!value.identity.equals(fields[1]) || !Integer.toString(value.layers.length).equals(fields[2])) return null;
        ElementalShell121 assigned = empty(level);
        if (level.progressionVersion == 6 && level.number > 10) assign(level, assigned);
        int previous = -1;
        if (!fields[3].isEmpty()) for (String row : fields[3].split(";", -1)) {
            String[] parts = row.split(":", -1);
            if (parts.length != 3) return null;
            int id, count;
            try { id = Integer.parseInt(parts[0]); count = Integer.parseInt(parts[2]); }
            catch (NumberFormatException rejected) { return null; }
            if (!Integer.toString(id).equals(parts[0]) || !Integer.toString(count).equals(parts[2])
                    || !value.valid(id) || id <= previous || count <= 0
                    || count > assigned.layers[id] || !assigned.kinds[id].name().equals(parts[1])) return null;
            value.kinds[id] = assigned.kinds[id];
            value.layers[id] = count;
            previous = id;
        }
        return value.encode().equals(encoded) ? value : null;
    }

    private boolean valid(int id) { return id >= 0 && id < layers.length; }

    private static void assign(Level level, ElementalShell121 value) {
        List<Integer> ids = new ArrayList<>();
        for (Tile tile : level.tiles) ids.add(tile.id);
        // Rank actual IDs by a stable hash of the complete puzzle and that ID.
        ids.sort(Comparator.comparing((Integer id) -> digest(value.identity + ":" + id))
                .thenComparingInt(Integer::intValue));
        Kind[] order = {Kind.ICE, Kind.FIRE, Kind.WATER};
        for (int i = 0; i < Math.min(order.length, ids.size()); i++) {
            int id = ids.get(i);
            value.kinds[id] = order[i];
            value.layers[id] = order[i] == Kind.WATER ? 1 : 2;
        }
    }

    private static String identity(Level level) {
        StringBuilder text = new StringBuilder("elemental-puzzle-v1|")
                .append(level.number).append('|').append(level.chapter).append('|').append(level.size)
                .append('|').append(level.initialGateMask).append('|').append(level.isEvent)
                .append('|').append(level.seed).append('|').append(level.progressionVersion)
                .append('|').append(level.minimumActions).append('|').append(level.parMoves)
                .append('|').append(level.allTilesMask).append('|').append(level.tiles.size());
        for (int id = 0; id < level.tiles.size(); id++) {
            Tile tile = level.tile(id);
            text.append("|T:").append(tile.id).append(':').append(tile.row).append(':').append(tile.col)
                    .append(':').append(tile.direction.ordinal()).append(':').append(tile.switchMask);
        }
        text.append("|B:").append(level.barriers.size());
        for (Barrier barrier : level.barriers) text.append('|').append(barrier.row).append(':')
                .append(barrier.col).append(':').append(barrier.keyTileId);
        text.append("|S:").append(level.solution.length);
        for (int id : level.solution) text.append(':').append(id);
        text.append("|A:").append(level.actionSolution.length);
        for (int id : level.actionSolution) text.append(':').append(id);
        return digest(text.toString());
    }

    private static String digest(String text) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(64);
            for (byte value : bytes) {
                int unsigned = value & 255;
                hex.append(Character.forDigit(unsigned >>> 4, 16)).append(Character.forDigit(unsigned & 15, 16));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
    }
}
