package com.androidhit.lumadrift.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Optional deterministic encounters. Never changes published L/M/T puzzle identities. */
public final class GuardianCatalog {
    public static final int COUNT = 100;
    private static final Definition[] CACHE = new Definition[COUNT];

    public static final class Definition {
        public final int number, size, minimumActions, baseBudget, totalWork;
        public final boolean cellwise;
        public final String titleKey;
        public final List<GuardianEncounter.Piece> pieces;

        private Definition(int number, int size, List<GuardianEncounter.Piece> pieces) {
            this.number = number;
            this.size = size;
            this.cellwise = number >= 5;
            this.titleKey = "guardian_" + (number - 1) % 10;
            this.pieces = Collections.unmodifiableList(new ArrayList<>(pieces));
            int work = 0;
            for (GuardianEncounter.Piece piece : pieces) work += piece.layers - 1
                    + (piece.cellwise ? distanceToExit(size, piece) : 1);
            this.minimumActions = work;
            this.totalWork = work;
            this.baseBudget = work + (number <= 4 ? 4 : number <= 20 ? 3 : 2);
        }
    }

    private GuardianCatalog() { }

    /** One encounter becomes available for each ten distinct Journey clears. */
    public static int unlockedCount(int journeyClears) {
        return Math.max(0, Math.min(COUNT, journeyClears / 10));
    }

    public static synchronized Definition get(int number) {
        if (number < 1 || number > COUNT) throw new IllegalArgumentException("Guardian number 1..100 required");
        if (CACHE[number - 1] != null) return CACHE[number - 1];
        int size = number >= 5 ? 5 : 4;
        int count = number <= 2 ? 4 : number <= 4 ? 5 : 6 + number % 3;
        ArrayList<GuardianEncounter.Piece> pieces = new ArrayList<>();
        // Within a row, every arrow points towards the same edge. Pieces in front
        // create visible dependencies; every successful action decreases finite work.
        for (int id = 0; id < count; id++) {
            int row = id % size, slot = id / size;
            boolean right = ((number + row) & 1) == 0;
            int offset = ((number / 3 + row) % 2);
            int col = right ? slot * 2 + offset : size - 1 - slot * 2 - offset;
            int layers = id == 0 || ((number >>> (id % 7)) & 1) != 0 ? 2 : 1;
            boolean cellwise = number >= 5 && (id % 3 != 2 || number >= 20);
            pieces.add(new GuardianEncounter.Piece(id, row, col,
                    right ? Direction.RIGHT : Direction.LEFT, layers, cellwise, true));
        }
        return CACHE[number - 1] = new Definition(number, size, pieces);
    }

    static int distanceToExit(int size, GuardianEncounter.Piece piece) {
        switch (piece.direction) {
            case RIGHT: return size - piece.col;
            case LEFT: return piece.col + 1;
            case UP: return piece.row + 1;
            default: return size - piece.row;
        }
    }
}
