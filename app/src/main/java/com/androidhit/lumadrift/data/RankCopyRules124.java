package com.androidhit.lumadrift.data;

/** Pure first-win/rank policy, shared by the native coordinator and synthetic controls. */
public final class RankCopyRules124 {
    public static final int XP_PER_RANK = 120;
    public static final int MAX_CAPTAIN_DUPLICATE_COINS = 45;
    private RankCopyRules124() { }
    public static int xpForStars(int stars) {
        if (stars < 1 || stars > 3) throw new IllegalArgumentException("Stars 1..3 required");
        return 20 + 5 * stars;
    }
    public static int rankForXp(int xp) {
        if (xp < 0) throw new IllegalArgumentException("Nonnegative XP required");
        return 1 + xp / XP_PER_RANK;
    }
    public static int ranksCrossed(int beforeXp, int afterXp) {
        if (afterXp < beforeXp) throw new IllegalArgumentException("XP must not move backwards");
        return rankForXp(afterXp) - rankForXp(beforeXp);
    }
    /** Existing ranks are never replayed as a backlog of companion rewards. */
    public static int migrationFloor(int existingXp) { return rankForXp(existingXp); }
}
