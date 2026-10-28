import com.androidhit.lumadrift.engine.*;
import java.nio.file.*;

/** A deliberately wasteful but mechanically legal N28 attempt; no Android state. */
public final class LearningFailureFixture {
    private static final String[] LABELS={"up","right","down","left"};
    public static void main(String[] args)throws Exception{
        Level level=LearningCatalog.get(28);Board board=new Board(level);MoveBudget budget=MoveBudget.startDeliberate(0,level);int[] sequence={65,66,67,0,65,1,66,2,67,3,4,5};
        StringBuilder out=new StringBuilder("{\"number\":28,\"family\":\"N\",\"size\":").append(level.size).append(",\"initialGateMask\":").append(level.initialGateMask).append(",\"minimum\":").append(level.minimumActions).append(",\"base\":").append(budget.base()).append(",\"prefix\":[");
        for(int i=0;i<sequence.length;i++){
            int id=sequence[i],before=board.gateMask();boolean gate=Board.isGateAction(id);Tile tile=gate?null:level.tile(id);if(!board.canPerformAction(id)||!budget.consumeAction()||!board.performAction(id).success||board.isSolved())throw new AssertionError("Not a genuine unsolved legal attempt at "+i);
            if(i>0)out.append(',');out.append("{\"action\":").append(id).append(",\"type\":\"").append(gate?"OPEN_GATE":"EXIT").append("\",\"gatesBefore\":").append(before).append(",\"gatesAfter\":").append(board.gateMask());
            if(gate)out.append(",\"direction\":").append(Board.gateDirection(id).ordinal()).append(",\"directionLabel\":\"").append(LABELS[Board.gateDirection(id).ordinal()]).append('"');
            else out.append(",\"row\":").append(tile.row).append(",\"col\":").append(tile.col).append(",\"direction\":").append(tile.direction.ordinal()).append(",\"switch\":").append(tile.switchMask);
            out.append('}');
        }
        if(!budget.isExhausted()||board.remainingCount()!=2||budget.undoRemaining(0)!=1)throw new AssertionError("Expected real exhausted N28 with two remaining pieces");
        out.append("],\"remainingAtFailure\":").append(board.remainingCount());if(!budget.undo(board,0)||budget.undo(board,1)||budget.remaining()!=1||board.remainingCount()!=3)throw new AssertionError("Single return contract differs");
        out.append(",\"remainingAfterUndo\":").append(board.remainingCount()).append(",\"actionsLeftAfterUndo\":").append(budget.remaining()).append("}\n");Files.writeString(Paths.get(args[0]),out.toString());System.out.println("N28:12 legal productive actions exhaust budget with2 pieces; one return leaves3 pieces and1 action.");
    }
}
