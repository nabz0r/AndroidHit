package com.androidhit.lumadrift.progression;

import com.androidhit.lumadrift.engine.Barrier;
import com.androidhit.lumadrift.engine.Board;
import com.androidhit.lumadrift.engine.Direction;
import com.androidhit.lumadrift.engine.Tile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Mechanical explanations only. This guide never runs a solver, changes a board,
 * consumes an item, or calls a merely legal move an optimal move.
 */
public final class LearningGuide {
    private static final List<String> LESSONS = Arrays.asList(
            "exits", "blockers", "key", "gate", "switch", "switch_order",
            "key_switch", "combination", "practice", "relay", "relay_order", "relay_key", "relay_mastery",
            "relay_inspection", "relay_anticipation", "relay_double");

    public static final class Context {
        public final String lessonKey, titleKey, descriptionKey, hudKey;
        /** Closed sides used by a remaining piece. A side controls every lane on that side. */
        public final int closedGateMask;
        /** An explanation target, never a solver recommendation. Null on historical switch-only rules. */
        public final Direction focusGate;
        public final Object[] descriptionArgs;

        private Context(String lesson, int closed, boolean manual) {
            lessonKey = lesson;
            titleKey = "lesson_" + lesson + "_title";
            descriptionKey = "lesson_" + lesson + "_note";
            hudKey = "hud_" + lesson;
            closedGateMask = closed;
            Direction first = null;
            if (manual) for (Direction direction : Direction.values()) {
                if ((closed & direction.bit) != 0) { first = direction; break; }
            }
            focusGate = first;
            descriptionArgs = new Object[0];
        }
    }

    public static final class Feedback {
        public final String key;
        /** Compact cause for policy-4 free rejected attempts; other policies must use key. */
        public final String shortKey;
        /** Pass directly to LearningStrings.get; Direction arguments are localized there. */
        public final Object[] args;
        public final Direction focusGate;
        public final int blockingTileId, keyNumber;

        private Feedback(String key, Direction gate, int blocker, int keyNumber, Object... args) {
            this.key = key; shortKey = key + "_short"; this.args = args; focusGate = gate;
            blockingTileId = blocker; this.keyNumber = keyNumber;
        }
    }

    private LearningGuide() { }

    /** Use the authored lesson for the new route. Unknown lesson names fall back to actual mechanics. */
    public static Context context(Board board, String authoredLesson) {
        if (board == null) throw new IllegalArgumentException("A board is required");
        String lesson = LESSONS.contains(authoredLesson) ? authoredLesson : inferLesson(board);
        if (board.isSolved()) lesson = "solved";
        int closed = 0;
        for (Tile tile : board.remainingTiles()) {
            if (!board.isGateOpen(tile.direction)) closed |= tile.direction.bit;
        }
        return new Context(lesson, closed, board.supportsManualGates());
    }

    /** Historical boards retain their own mechanics; their displayed level number does not identify a lesson. */
    public static Context context(Board board) { return context(board, null); }

    /**
     * Presentation-only 1.23 teaching sequence. Frozen levels, certificates and
     * historical lesson lookup remain unchanged, including for saved attempts.
     * Inspection explains immediate consequences, never hidden solution safety.
     */
    public static Context contextJourney123(Board board, String authoredLesson) {
        Context current = context(board, authoredLesson);
        if (board.level.progressionVersion != 6 || board.isSolved()) return current;
        int number = board.level.number;
        String lesson = current.lessonKey;
        if (number >= 13 && number <= 16) lesson = "relay_inspection";
        else if (number >= 17 && number <= 28) lesson = "relay_anticipation";
        else if (number >= 61 && number <= 64) lesson = "relay_double";
        return lesson.equals(current.lessonKey) ? current
                : new Context(lesson, current.closedGateMask, board.supportsManualGates());
    }

    /** Visible affordance based only on remaining relay marks, with no solver call. */
    public static boolean inspectionRecommended(Board board) {
        if (board == null || board.level.progressionVersion != 6 || board.isSolved()) return false;
        for (Tile tile : board.remainingTiles()) if (tile.isSwitch()) return true;
        return false;
    }

    private static String inferLesson(Board board) {
        boolean key = false;
        int switches = 0;
        for (Barrier barrier : board.level.barriers) if (board.isBarrierActive(barrier)) key = true;
        for (Tile tile : board.remainingTiles()) if (tile.isSwitch()) switches++;
        if(board.level.progressionVersion==6){
            if(key&&switches>0)return "relay_key";
            if(switches>1)return "relay_order";
            if(switches==1)return "relay";
        }
        if (key && switches > 0) return "key_switch";
        if (key) return "key";
        for (Tile tile : board.remainingTiles()) if (!board.isGateOpen(tile.direction)) {
            if (board.supportsManualGates()) return "gate";
            return switches > 0 ? "switch" : "practice";
        }
        if (switches > 1) return "switch_order";
        if (switches == 1) return "switch";
        for (Tile tile : board.remainingTiles()) if (board.blockingTile(tile.id) >= 0) return "blockers";
        return "exits";
    }

    /** Match Board.blockReason's precedence exactly: closed side, nearest seal, then nearest piece. */
    public static Feedback blockedFeedback(Board board, int tileId) {
        if (board == null) throw new IllegalArgumentException("A board is required");
        Tile tile = board.level.tile(tileId);
        Board.BlockReason reason = board.blockReason(tileId);
        if (tile == null || reason == Board.BlockReason.MISSING_TILE)
            return new Feedback("piece_gone", null, -1, 0);
        if (reason == Board.BlockReason.CLOSED_GATE)
            return new Feedback(board.level.progressionVersion==6?"blocked_gate_relay":board.supportsManualGates() ? "blocked_gate_manual" : "blocked_gate_switch",
                    tile.direction, -1, 0, tile.direction);
        if (reason == Board.BlockReason.LOCKED_BARRIER) {
            Barrier barrier = board.blockingBarrier(tileId);
            int number = keyNumber(board, barrier == null ? -1 : barrier.keyTileId);
            return new Feedback("blocked_key", null, barrier == null ? -1 : barrier.keyTileId, number, number);
        }
        if (reason == Board.BlockReason.BLOCKED_PATH) {
            int blocker = board.blockingTile(tileId);
            Tile obstacle = board.level.tile(blocker);
            return new Feedback("blocked_piece", null, blocker, 0,
                    obstacle == null ? 0 : obstacle.row + 1, obstacle == null ? 0 : obstacle.col + 1);
        }
        return new Feedback("path_clear", null, -1, 0);
    }

    /** Same stable first-seen key numbering as the native puzzle renderer, including shared seals. */
    public static int keyNumber(Board board, int keyTileId) {
        if (board == null) throw new IllegalArgumentException("A board is required");
        ArrayList<Integer> keys = new ArrayList<>();
        for (Barrier barrier : board.level.barriers) if (!keys.contains(barrier.keyTileId)) keys.add(barrier.keyTileId);
        int index = keys.indexOf(keyTileId);
        return index < 0 ? 0 : index + 1;
    }
}
