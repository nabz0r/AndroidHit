package com.androidhit.lumadrift.data;

/**
 * Transparent progression-only prices. No spending, failure, session or payment signal
 * affects this curve. At most 480 distinct Journey and Remix clears are counted.
 */
public final class EconomyCurve {
    public static final int MAX_PROGRESS = 480;
    public static final int MAX_BAND = 4;

    private EconomyCurve() { }

    /** floor(log2(1 + progress / 30)); integer thresholds are 30, 90, 210 and 450. */
    public static int band(int completedBoards) {
        int progress = Math.max(0, Math.min(MAX_PROGRESS, completedBoards));
        int value = 1 + progress / 30;
        return Math.min(MAX_BAND, 31 - Integer.numberOfLeadingZeros(value));
    }

    /** Round base * (1 + 0.20 * band) up to five coins, capped at twice the base. */
    public static int price(int basePrice, int completedBoards) {
        if (basePrice <= 0) return 0;
        long numerator = (long) basePrice * (5 + band(completedBoards));
        long rounded = ((numerator + 24) / 25) * 5;
        return (int) Math.min(Integer.MAX_VALUE, Math.min(2L * basePrice, rounded));
    }
}
