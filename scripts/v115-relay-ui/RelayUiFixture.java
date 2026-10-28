import com.androidhit.lumadrift.engine.*;
import java.nio.file.*;

/** Host-only read/replay of the frozen catalogue. No Android or preference access. */
public final class RelayUiFixture {
    static String fixture(int number){
        Level level=RelayCatalog.get(number);Board board=new Board(level);MoveBudget ledger=MoveBudget.start(0,level);
        if(board.supportsManualGates()||ledger.policyVersion()!=5||level.progressionVersion!=6)throw new AssertionError("Wrong U mechanics");
        StringBuilder out=new StringBuilder("{\"family\":\"U\",\"number\":").append(number).append(",\"version\":6,\"contentVersion\":7,\"policy\":5,\"code\":\"").append(ChallengeCode.encodeRelay(number)).append("\",\"size\":").append(level.size).append(",\"pieces\":").append(level.tiles.size()).append(",\"initialGates\":").append(level.initialGateMask).append(",\"base\":").append(ledger.base()).append(",\"blockedFirst\":");
        Tile blocked=null;for(Tile t:level.tiles)if(!board.canMove(t.id)){blocked=t;break;}
        if(blocked==null)out.append("null");else out.append("{\"id\":").append(blocked.id).append(",\"row\":").append(blocked.row).append(",\"col\":").append(blocked.col).append(",\"reason\":\"").append(board.blockReason(blocked.id).name()).append("\"}");
        out.append(",\"steps\":[");
        for(int i=0;i<level.actionSolution.length;i++){
            int id=level.actionSolution[i];if(Board.isGateAction(id))throw new AssertionError("U certificate contains a gate action");Tile t=level.tile(id);int before=board.gateMask();if(!board.performAction(id).success)throw new AssertionError("Illegal U certificate");
            if(i>0)out.append(',');out.append("{\"action\":").append(id).append(",\"row\":").append(t.row).append(",\"col\":").append(t.col).append(",\"direction\":").append(t.direction.ordinal()).append(",\"switch\":").append(t.switchMask).append(",\"key\":").append(level.isKey(id)).append(",\"gatesBefore\":").append(before).append(",\"gatesAfter\":").append(board.gateMask()).append(",\"remainingAfter\":").append(board.remainingCount()).append('}');
        }
        if(!board.isSolved())throw new AssertionError("Incomplete U certificate");return out.append("]}\n").toString();
    }
    public static void main(String[] args)throws Exception{Path output=Path.of(args[0]);Files.createDirectories(output);for(int n=1;n<=1000;n++)Files.writeString(output.resolve("u"+n+".json"),fixture(n));System.out.println("PASS 1000 U native fixtures mechanically replayed on host; no Android injection or independent optimality claim.");}
}
