package com.androidhit.lumadrift.engine;

/** Fixed sealed cell. It disappears exactly while its associated key piece is absent. */
public final class Barrier {
    public final int row;
    public final int col;
    public final int keyTileId;

    public Barrier(int row, int col, int keyTileId) {
        if (row < 0 || col < 0 || keyTileId < 0) throw new IllegalArgumentException("Nonnegative barrier coordinates/key required");
        this.row = row;
        this.col = col;
        this.keyTileId = keyTileId;
    }
}
