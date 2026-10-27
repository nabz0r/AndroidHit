package com.androidhit.lumadrift.engine;

import java.util.ArrayList;

/** Cheap frozen lookup. Candidate selection and exact metrics exist only in offline tools. */
final class ProgressionV3Catalog {
    private static final Level[] JOURNEY = new Level[BalancedCatalog.LEVEL_COUNT];
    private static final Level[] REMIX = new Level[BalancedCatalog.REMIX_COUNT];
    private static final ProgressionInfo[] JOURNEY_INFO = new ProgressionInfo[BalancedCatalog.LEVEL_COUNT];
    private static final ProgressionInfo[] REMIX_INFO = new ProgressionInfo[BalancedCatalog.REMIX_COUNT];
    private ProgressionV3Catalog() { }

    static Level get(int number, boolean remix) {
        String[] row = row(number, remix);
        Level[] cache = remix ? REMIX : JOURNEY;
        if (cache[number - 1] == null) {
            int source = Integer.parseInt(row[0]);
            Level old = remix ? BalancedCatalog.remixV2(source) : BalancedCatalog.getV2(source);
            boolean plainKeyLesson = row.length > 5 && row[5].equals("1");
            java.util.List<Tile> tiles = old.tiles;
            if (plainKeyLesson) {
                ArrayList<Tile> ordinary = new ArrayList<>();
                for (Tile tile : old.tiles) ordinary.add(new Tile(tile.id, tile.row, tile.col, tile.direction, 0));
                tiles = ordinary;
            }
            ArrayList<Barrier> barriers = new ArrayList<>();
            if (!row[4].isEmpty()) for (String encoded : row[4].split(";")) {
                String[] fields = encoded.split(",");
                barriers.add(new Barrier(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]), Integer.parseInt(fields[2])));
            }
            cache[number - 1] = new Level(number, (number - 1) / 30 + 1, old.size,
                    plainKeyLesson ? 15 : old.initialGateMask, tiles, old.solution, false,
                    old.seed ^ 0x56334A4B00000000L ^ number, barriers, 3);
            (remix ? REMIX_INFO : JOURNEY_INFO)[number - 1] = new ProgressionInfo(cache[number - 1],
                    remix, source, Integer.parseInt(row[1]), ProgressionInfo.Difficulty.values()[Integer.parseInt(row[2])],
                    ProgressionInfo.Beat.values()[Integer.parseInt(row[3])]);
        }
        return cache[number - 1];
    }

    static ProgressionInfo info(int number, boolean remix) {
        get(number, remix);
        return (remix ? REMIX_INFO : JOURNEY_INFO)[number - 1];
    }

    static int allowance(int number, boolean remix) { return Integer.parseInt(row(number, remix)[1]); }

    private static String[] row(int number, boolean remix) {
        int maximum = remix ? BalancedCatalog.REMIX_COUNT : BalancedCatalog.LEVEL_COUNT;
        if (number < 1 || number > maximum) throw new IllegalArgumentException("Level must be 1.." + maximum);
        return (remix ? ProgressionV3Layouts.REMIX : ProgressionV3Layouts.JOURNEY)[number - 1].split("\\|", -1);
    }
}
