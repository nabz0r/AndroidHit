import com.androidhit.lumadrift.progression.LearningStrings;
import com.androidhit.lumadrift.engine.*;
import com.androidhit.lumadrift.progression.LearningGuide;
import java.util.*;

/** Mechanical causes, historical-rule separation, localization, and no solver/state mutations. */
public final class LearningGuideTest {
    private static int checks;
    private static void check(boolean condition, String name) {
        checks++;
        if (!condition) throw new AssertionError(name);
    }
    private static void unchanged(Board board, long mask, int gates, int[] history, int cache) {
        check(board.remainingMask() == mask && board.gateMask() == gates
                && Arrays.equals(board.history(), history) && board.plannerCachedStates() == cache,
                "Guide is read-only and does not invoke exact search");
    }
    private static void matchesReason(Board board, Tile tile) {
        LearningGuide.Feedback feedback = LearningGuide.blockedFeedback(board, tile.id);
        Board.BlockReason reason = board.blockReason(tile.id);
        switch (reason) {
            case CLOSED_GATE:
                check(feedback.key.equals(board.supportsManualGates() ? "blocked_gate_manual" : "blocked_gate_switch")
                        && feedback.focusGate == tile.direction && feedback.args[0] == tile.direction,
                        "Closed gate gives actual direction and supported rule");
                break;
            case LOCKED_BARRIER:
                Barrier barrier = board.blockingBarrier(tile.id);
                check(feedback.key.equals("blocked_key") && feedback.blockingTileId == barrier.keyTileId
                        && feedback.keyNumber == LearningGuide.keyNumber(board, barrier.keyTileId),
                        "Seal explains its actual key");
                break;
            case BLOCKED_PATH:
                Tile blocker = board.level.tile(board.blockingTile(tile.id));
                check(feedback.key.equals("blocked_piece") && feedback.blockingTileId == blocker.id
                        && feedback.args[0].equals(blocker.row + 1) && feedback.args[1].equals(blocker.col + 1),
                        "Nearest blocking piece uses one-based coordinates");
                break;
            case NONE: check(feedback.key.equals("path_clear"), "Clear means mechanically legal, not solver optimal"); break;
            default: throw new AssertionError("Unexpected present tile reason: " + reason);
        }
    }
    public static void main(String[] args) {
        Level manual = new Level(1,0,2,0,
                Arrays.asList(new Tile(0,0,0,Direction.UP,0)),new int[]{0},false,0,
                Collections.emptyList(),4,2,new int[]{64,0});
        Board gateBoard = new Board(manual);
        check(LearningGuide.context(gateBoard).lessonKey.equals("gate"), "Manual gate inferred from actual state");
        check(LearningGuide.context(gateBoard).focusGate == Direction.UP, "Manual side targets a closed used gate");
        check(LearningGuide.context(gateBoard,"exits").lessonKey.equals("exits"), "Authored lesson takes precedence");
        check(LearningGuide.context(gateBoard,"invalid").lessonKey.equals("gate"), "Invalid lesson falls back safely");
        LearningGuide.Feedback gateFeedback = LearningGuide.blockedFeedback(gateBoard,0);
        check(LearningStrings.get("fr-FR",gateFeedback.key,gateFeedback.args).contains("du haut"), "Gate direction localized inside feedback");
        check(LearningStrings.get("en",gateFeedback.key,gateFeedback.args).startsWith("The top gate"), "English direction grammar");
        Level historical = new Level(1,0,2,0,
                Arrays.asList(new Tile(0,0,0,Direction.UP,0)),new int[]{0},false,0);
        Board old = new Board(historical);
        check(LearningGuide.context(old).focusGate == null, "Historical rules never receive a manual-opening focus");
        check(!LearningGuide.context(old).lessonKey.equals("gate"), "Historical closed side not advertised as button");
        check(LearningGuide.blockedFeedback(old,0).key.equals("blocked_gate_switch"), "Historical blocked cause retains switch rule");
        Level sealed = new Level(1,0,3,15,
                Arrays.asList(new Tile(0,0,0,Direction.RIGHT,0),new Tile(1,2,2,Direction.DOWN,0)),
                new int[]{1,0},false,0,
                Arrays.asList(new Barrier(0,1,1),new Barrier(1,1,1)));
        Board keyBoard = new Board(sealed);
        check(LearningGuide.keyNumber(keyBoard,1) == 1, "Shared seals retain the same key number");
        check(LearningGuide.keyNumber(keyBoard,0) == 0, "An ordinary tile has no K number");
        check(LearningGuide.context(keyBoard).lessonKey.equals("key"), "Key lesson reflects active seal");
        matchesReason(keyBoard,keyBoard.level.tile(0));
        check(keyBoard.move(1).success, "Fixture key exits");
        check(LearningGuide.blockedFeedback(keyBoard,0).key.equals("path_clear"), "Explained seal disappears with key exit");
        check(LearningGuide.blockedFeedback(keyBoard,1).key.equals("piece_gone"), "Removed tile feedback");
        check(LearningGuide.blockedFeedback(keyBoard,Integer.MAX_VALUE).key.equals("piece_gone"), "Invalid tile feedback safe");
        Level nearest = new Level(1,0,3,15,
                Arrays.asList(new Tile(0,0,0,Direction.RIGHT,0),new Tile(1,0,1,Direction.RIGHT,0),new Tile(2,2,2,Direction.DOWN,0)),
                new int[]{2,1,0},false,0,Arrays.asList(new Barrier(0,2,2)));
        Board nearBoard = new Board(nearest);
        check(LearningGuide.blockedFeedback(nearBoard,0).key.equals("blocked_piece"), "Closer tile outranks farther seal like the engine");
        Board l15 = new Board(StrategyCatalog.get(15));
        check(LearningGuide.blockedFeedback(l15,4).focusGate == Direction.LEFT, "Reported L15 cause is closed LEFT");
        check(l15.performAction(Board.gateAction(Direction.LEFT)).success && l15.performAction(4).success,
                "Reported L15 switch can exit after opening LEFT");
        check(LearningGuide.blockedFeedback(l15,5).focusGate == Direction.UP, "L15 switch closes UP and explanation follows actual toggle");
        for (int n=1;n<=30;n++) {
            Board board = new Board(StrategyCatalog.get(n));
            for (int step=0;step<=board.level.actionSolution.length;step++) {
                long mask=board.remainingMask();int gates=board.gateMask(),cache=board.plannerCachedStates();int[] history=board.history();
                LearningGuide.Context context=LearningGuide.context(board);
                check(context.titleKey.equals("lesson_"+context.lessonKey+"_title")
                        && context.descriptionKey.equals("lesson_"+context.lessonKey+"_note")
                        && context.hudKey.equals("hud_"+context.lessonKey), "Context keys match actual lesson");
                for (Tile tile:board.remainingTiles()) matchesReason(board,tile);
                unchanged(board,mask,gates,history,cache);
                if (step<board.level.actionSolution.length)
                    check(board.performAction(board.level.actionSolution[step]).success,"Certified diagnostic step");
            }
            check(LearningGuide.context(board).lessonKey.equals("solved"), "Solved board has no stale instruction");
        }
        for (int n=1;n<=30;n++) {
            Board board = new Board(LearningCatalog.get(n));
            String authored = LearningCatalog.lesson(n);
            for (int step=0;step<=board.level.actionSolution.length;step++) {
                long mask=board.remainingMask();int gates=board.gateMask(),cache=board.plannerCachedStates();int[] history=board.history();
                LearningGuide.Context context=LearningGuide.context(board,authored);
                check(context.lessonKey.equals(board.isSolved()?"solved":authored), "New route preserves authored lesson without looking up a solution");
                for (Tile tile:board.remainingTiles()) matchesReason(board,tile);
                unchanged(board,mask,gates,history,cache);
                if (step<board.level.actionSolution.length)
                    check(board.performAction(board.level.actionSolution[step]).success,"New learning route diagnostic step");
            }
        }
        String[] languages={"en","fr","de","es","pt","it"};
        for(String key:LearningStrings.keys()) for(String language:languages) {
            String base=key.endsWith("_short")?key.substring(0,key.length()-6):key;
            Object[] values=base.equals("blocked_piece")?new Object[]{2,3}:
                    base.startsWith("blocked_gate_")?new Object[]{Direction.LEFT}:
                    base.equals("blocked_key")||base.equals("undo_remaining")||base.equals("clear_path")||base.equals("last_moves")||base.equals("run_failed_note")?new Object[]{2}:
                    base.equals("action_target")||base.equals("needs_actions")?new Object[]{4,12}:new Object[0];
            String value=LearningStrings.get(language,key,values);
            check(!value.isBlank()&&!value.contains("%d")&&!value.contains("%s"),"Formatted localized copy "+key+" "+language);
            if(key.startsWith("hud_"))check(value.codePointCount(0,value.length())<=75,"HUD copy stays within 75 characters: "+key+" "+language);
            if(key.endsWith("_short"))check(value.codePointCount(0,value.length())<=55,"Compact feedback stays within 55 characters: "+key+" "+language);
            if(key.startsWith("blocked_gate_"))check(!value.contains("LEFT"),"No raw enum direction in localized copy");
        }
        check(LearningStrings.get("fr_CA","undo_one").equals("Retour ×1"),"Regional locale normalized");
        check(LearningStrings.get(null,"undo_one").equals("Undo ×1"),"Null locale falls back to English");
        check(LearningStrings.get("ja","undo_one").equals("Undo ×1"),"Unsupported locale falls back to English");
        System.out.println("PASS "+checks+" learning guide checks; actual block causes, L15 toggles, N1..30 and L1..30, six languages, no solver or state mutation.");
    }
}
