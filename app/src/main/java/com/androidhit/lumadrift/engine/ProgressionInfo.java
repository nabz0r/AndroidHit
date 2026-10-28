package com.androidhit.lumadrift.engine;

import java.util.HashSet;

/** Authored labels and transparent provenance, not calibrated human difficulty. */
public final class ProgressionInfo {
    public enum Difficulty { NORMAL, HARD, EXPERT }
    public enum Beat { INTRODUCTION, PRACTICE, PEAK, RECOVERY }
    public enum Objective { CLEAR_BOARD, OPEN_SEALS }
    public enum Element { ARROWS, GATES, DUAL_GATES, KEYS_AND_SEALS }

    public final int number;
    public final int version;
    public final int minimumActions;
    public final int requiredGateOpens;
    public final int decisionPoints,forcedOptimalActions,maxOneChoicePenalty,dependencyDepth,structuralScore;
    public final Difficulty difficulty;
    public final Beat beat;
    public final Objective objective;
    public final Element element;
    public final String difficultyKey;
    public final String beatKey;
    public final String objectiveKey;
    public final String elementKey;
    public final int keyCount;
    public final int barrierCount;
    public final int sourceNumber;
    public final String sourceFamily;
    public final int mistakeAllowance;
    public final int pieces;
    public final int size;

    ProgressionInfo(Level level, boolean remix, int sourceNumber, int allowance,
                    Difficulty difficulty, Beat beat) {
        this(level,remix,sourceNumber,allowance,difficulty,beat,0,0,0,0,0);
    }
    ProgressionInfo(Level level, boolean remix, int sourceNumber, int allowance,
                    Difficulty difficulty, Beat beat,int decisions,int forced,int penalty,int depth,int score) {
        this.number = level.number;
        this.version=level.progressionVersion;this.minimumActions=level.minimumActions;
        this.requiredGateOpens=level.minimumActions-level.tiles.size();
        decisionPoints=decisions;forcedOptimalActions=forced;maxOneChoicePenalty=penalty;dependencyDepth=depth;structuralScore=score;
        this.difficulty = difficulty;
        this.beat = beat;
        this.sourceNumber = sourceNumber;
        this.sourceFamily = level.progressionVersion==6?"U":level.progressionVersion==5?(sourceNumber>0?"L":"N"):level.progressionVersion==4?(remix?"K":"J"):(remix ? "H" : "G");
        this.mistakeAllowance = allowance;
        this.pieces = level.tiles.size();
        this.size = level.size;
        this.barrierCount = level.barriers.size();
        HashSet<Integer> keys = new HashSet<>();
        for (Barrier barrier : level.barriers) keys.add(barrier.keyTileId);
        this.keyCount = keys.size();
        boolean switches = false, paired = false;
        for (Tile tile : level.tiles) {
            switches |= tile.switchMask != 0;
            paired |= Integer.bitCount(tile.switchMask) > 1;
        }
        this.objective = keyCount > 0 ? Objective.OPEN_SEALS : Objective.CLEAR_BOARD;
        this.element = keyCount > 0 ? Element.KEYS_AND_SEALS : paired ? Element.DUAL_GATES
                : switches||requiredGateOpens>0 ? Element.GATES : Element.ARROWS;
        this.difficultyKey = "difficulty_" + difficulty.name().toLowerCase(java.util.Locale.ROOT);
        this.beatKey = "beat_" + beat.name().toLowerCase(java.util.Locale.ROOT);
        this.objectiveKey = "objective_" + objective.name().toLowerCase(java.util.Locale.ROOT);
        this.elementKey = "element_" + element.name().toLowerCase(java.util.Locale.ROOT);
    }
}
