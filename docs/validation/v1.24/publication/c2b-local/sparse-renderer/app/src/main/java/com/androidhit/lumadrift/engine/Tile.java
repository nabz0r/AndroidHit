package com.androidhit.lumadrift.engine;

/** Immutable level piece. IDs are dense, zero based, and stable for a level. */
public final class Tile {
    public final int id;
    public final int row;
    public final int col;
    public final Direction direction;
    /** Gates XORed immediately after this tile exits; zero means an ordinary tile. */
    public final int switchMask;

    public Tile(int id, int row, int col, Direction direction, int switchMask) {
        if (direction == null || (switchMask & ~15) != 0) {
            throw new IllegalArgumentException("Invalid tile direction or gate mask");
        }
        this.id = id;
        this.row = row;
        this.col = col;
        this.direction = direction;
        this.switchMask = switchMask;
    }

    public boolean isSwitch() { return switchMask != 0; }
}
