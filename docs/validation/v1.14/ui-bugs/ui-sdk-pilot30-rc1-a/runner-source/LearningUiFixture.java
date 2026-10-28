import com.androidhit.lumadrift.engine.*;
import java.nio.file.*;

/** Read-only N catalogue certificates, replayed mechanically before SDK use. */
public final class LearningUiFixture {
    private static final String[] LABELS={"up","right","down","left"};
    private static String step(Level level,Board board,int action){
        int before=board.gateMask();boolean gate=Board.isGateAction(action);Tile tile=gate?null:level.tile(action);
        if(!board.performAction(action).success)throw new AssertionError("Illegal authored N action on "+level.number);
        StringBuilder out=new StringBuilder("{\"action\":").append(action).append(",\"type\":\"").append(gate?"OPEN_GATE":"EXIT").append("\",\"gatesBefore\":").append(before).append(",\"gatesAfter\":").append(board.gateMask());
        if(gate)out.append(",\"direction\":").append(Board.gateDirection(action).ordinal()).append(",\"directionLabel\":\"").append(LABELS[Board.gateDirection(action).ordinal()]).append('"');
        else out.append(",\"id\":").append(action).append(",\"row\":").append(tile.row).append(",\"col\":").append(tile.col).append(",\"direction\":").append(tile.direction.ordinal()).append(",\"switch\":").append(tile.switchMask);
        return out.append('}').toString();
    }
    private static String fixture(int n){
        Level level=LearningCatalog.get(n);Board board=new Board(level);MoveBudget budget=MoveBudget.startDeliberate(0,level);int[] plan=level.actionSolution;
        if(plan.length!=level.minimumActions)throw new AssertionError("N certificate length differs at "+n);
        StringBuilder out=new StringBuilder("{\"family\":\"N\",\"number\":").append(n).append(",\"version\":5,\"contentVersion\":6,\"code\":\"").append(ChallengeCode.encodeLearning(n)).append("\",\"size\":").append(level.size).append(",\"pieces\":").append(level.tiles.size()).append(",\"minimum\":").append(level.minimumActions).append(",\"base\":").append(budget.base()).append(",\"margin\":").append(budget.mistakeAllowance()).append(",\"steps\":[");
        for(int i=0;i<plan.length;i++){if(i>0)out.append(',');out.append(step(level,board,plan[i]));}
        if(!board.isSolved())throw new AssertionError("Authored N path does not solve "+n);
        return out.append("]}\n").toString();
    }
    public static void main(String[] args)throws Exception{
        if(args.length!=2)throw new IllegalArgumentException("Fresh output directory and count required");Path output=Paths.get(args[0]);int count=Integer.parseInt(args[1]);if(count<1||count>1000)throw new IllegalArgumentException("N count1..1000");Files.createDirectories(output);
        for(int n=1;n<=count;n++)Files.writeString(output.resolve("n"+n+".json"),fixture(n));
        System.out.println("N certificates mechanically replayed: "+count+". No runtime/profile or independent optimality claim.");
    }
}
