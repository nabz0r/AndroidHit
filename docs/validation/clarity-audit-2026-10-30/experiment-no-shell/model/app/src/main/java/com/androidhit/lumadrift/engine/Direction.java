package com.androidhit.lumadrift.engine;

/** A tile travels in a straight line to the gate on this side of the board. */
public enum Direction {
    UP(-1, 0), RIGHT(0, 1), DOWN(1, 0), LEFT(0, -1);

    public final int dr;
    public final int dc;
    public final int bit;

    Direction(int dr, int dc) {
        this.dr = dr;
        this.dc = dc;
        this.bit = 1 << ordinal();
    }

    public static final int ALL_OPEN = 15;
}
