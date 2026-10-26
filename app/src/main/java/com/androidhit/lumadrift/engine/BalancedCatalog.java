package com.androidhit.lumadrift.engine;

/**
 * Frozen Lab progression order. Original definitions, seeds and C/R challenge
 * identities remain in LevelCatalog. No sorting or solver analysis runs on a phone.
 */
public final class BalancedCatalog {
    public static final int LEVEL_COUNT = 240;
    public static final int ORDER_VERSION = 1;
    private static final int[] JOURNEY_ORDER = {
            1, 2, 3, 4, 5, 6, 8, 7, 12, 9, 14, 13, 10, 11, 15,
            17, 16, 21, 22, 24, 19, 23, 28, 18, 20, 29, 27, 30, 25, 26,
            33, 39, 36, 32, 40, 31, 34, 37, 42, 38, 50, 35, 41, 49, 43,
            48, 45, 46, 47, 44, 52, 54, 53, 59, 60, 56, 51, 58, 57, 55,
            68, 63, 67, 70, 69, 61, 62, 64, 65, 66, 76, 79, 73, 71, 80,
            78, 75, 83, 88, 89, 86, 82, 74, 84, 87, 77, 72, 90, 81, 85,
            95, 92, 94, 96, 100, 98, 93, 97, 99, 101, 108, 110, 91, 107, 104,
            103, 109, 116, 120, 102, 106, 114, 111, 113, 118, 119, 115, 105, 117, 112,
            124, 125, 128, 134, 121, 127, 122, 123, 133, 129, 137, 130, 139, 135, 126,
            136, 138, 140, 131, 142, 150, 132, 141, 148, 144, 143, 146, 147, 145, 149,
            153, 157, 158, 152, 155, 163, 165, 169, 151, 168, 156, 160, 154, 164, 167,
            170, 162, 159, 176, 179, 161, 166, 175, 172, 177, 173, 174, 171, 180, 178,
            185, 182, 184, 189, 183, 186, 181, 196, 187, 197, 190, 195, 188, 193, 199,
            205, 192, 198, 204, 194, 209, 200, 191, 201, 210, 203, 208, 207, 202, 206,
            221, 214, 216, 225, 215, 228, 219, 227, 217, 224, 226, 218, 223, 212, 213,
            233, 230, 222, 211, 236, 235, 220, 231, 232, 238, 229, 234, 239, 240, 237
    };
    private static final int[] REMIX_ORDER = {
            1, 2, 3, 4, 6, 5, 9, 17, 27, 18, 26, 19, 7, 29, 25,
            24, 14, 16, 8, 13, 20, 15, 23, 21, 22, 28, 30, 12, 11, 10,
            50, 31, 37, 40, 43, 46, 48, 49, 32, 36, 51, 35, 41, 56, 57,
            38, 59, 44, 54, 47, 39, 58, 33, 34, 53, 42, 52, 55, 45, 60,
            61, 72, 75, 69, 71, 83, 70, 73, 65, 62, 85, 74, 88, 79, 81,
            66, 64, 68, 78, 76, 80, 84, 63, 67, 77, 86, 82, 87, 89, 90,
            92, 96, 102, 110, 91, 94, 104, 108, 109, 103, 106, 107, 97, 95, 112,
            105, 98, 93, 120, 100, 111, 101, 116, 119, 99, 115, 113, 114, 117, 118,
            135, 125, 121, 129, 143, 122, 128, 147, 142, 126, 132, 123, 140, 138, 149,
            146, 124, 131, 150, 145, 133, 134, 141, 137, 139, 144, 148, 130, 136, 127,
            162, 160, 151, 153, 157, 156, 154, 152, 155, 159, 158, 172, 171, 161, 179,
            164, 165, 170, 177, 166, 168, 163, 178, 175, 167, 169, 176, 173, 174, 180,
            196, 183, 184, 191, 202, 193, 192, 186, 190, 194, 188, 200, 199, 181, 209,
            195, 205, 207, 182, 197, 187, 185, 189, 203, 210, 198, 201, 204, 208, 206,
            212, 221, 211, 219, 231, 224, 223, 229, 230, 233, 215, 220, 225, 237, 227,
            238, 214, 239, 213, 216, 228, 232, 236, 218, 235, 226, 217, 240, 234, 222
    };
    private static final Level[] JOURNEY = new Level[LEVEL_COUNT];
    private static final Level[] REMIX = new Level[LEVEL_COUNT];

    private BalancedCatalog() { }

    public static synchronized Level get(int number) { return get(number, false); }
    public static synchronized Level remix(int number) { return get(number, true); }

    /** Original source identity, useful for provenance and frozen-order tests. */
    public static int sourceNumber(int number, boolean remix) {
        requireNumber(number);
        return (remix ? REMIX_ORDER : JOURNEY_ORDER)[number - 1];
    }

    private static Level get(int number, boolean remix) {
        requireNumber(number);
        Level[] cache = remix ? REMIX : JOURNEY;
        if (cache[number - 1] == null) {
            int source = sourceNumber(number, remix);
            Level original = remix ? LevelCatalog.remix(source) : LevelCatalog.get(source);
            cache[number - 1] = new Level(number, (number - 1) / 30 + 1,
                    original.size, original.initialGateMask, original.tiles,
                    original.solution, original.isEvent, original.seed);
        }
        return cache[number - 1];
    }

    private static void requireNumber(int number) {
        if (number < 1 || number > LEVEL_COUNT) throw new IllegalArgumentException("Level must be 1..240");
    }
}
