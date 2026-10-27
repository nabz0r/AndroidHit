import com.androidhit.lumadrift.engine.*;
import java.nio.file.*;
import java.util.*;

/** Source-only action oracle: outputs certified actions, not app progress, currency or account data. */
public final class StrategyUiFixture {
    private static final String[] LABELS={"up","right","down","left"};
    private static String step(Level level,Board board,int action){
        int before=board.gateMask();boolean gate=Board.isGateAction(action);Tile tile=gate?null:level.tile(action);
        if(!board.performAction(action).success)throw new AssertionError("Illegal certified action");
        StringBuilder s=new StringBuilder("{\"action\":").append(action).append(",\"type\":\"").append(gate?"OPEN_GATE":"EXIT").append("\",\"gatesBefore\":").append(before).append(",\"gatesAfter\":").append(board.gateMask());
        if(gate)s.append(",\"direction\":").append(Board.gateDirection(action).ordinal()).append(",\"directionLabel\":\"").append(LABELS[Board.gateDirection(action).ordinal()]).append('"');
        else s.append(",\"id\":").append(action).append(",\"row\":").append(tile.row).append(",\"col\":").append(tile.col).append(",\"direction\":").append(tile.direction.ordinal()).append(",\"switch\":").append(tile.switchMask);
        return s.append('}').toString();
    }
    private static String fixture(int n){
        Level level=StrategyCatalog.get(n);Board board=new Board(level);MoveBudget budget=MoveBudget.start(0,level);int[] plan=board.solve();
        if(plan.length!=level.minimumActions)throw new AssertionError("Minimum certificate differs");
        StringBuilder s=new StringBuilder("{\"family\":\"L\",\"number\":").append(n).append(",\"version\":4,\"code\":\"").append(ChallengeCode.encodeStrategy(n)).append("\",\"size\":").append(level.size).append(",\"pieces\":").append(level.tiles.size()).append(",\"minimum\":").append(level.minimumActions).append(",\"base\":").append(budget.base()).append(",\"margin\":").append(budget.mistakeAllowance()).append(",\"steps\":[");
        for(int i=0;i<plan.length;i++){if(i>0)s.append(',');s.append(step(level,board,plan[i]));}
        if(!board.isSolved())throw new AssertionError("UI path did not clear the board");
        return s.append("]}\n").toString();
    }
    public static void main(String[] args)throws Exception{
        if(args.length!=2)throw new IllegalArgumentException("Output directory and count required");Path out=Paths.get(args[0]);int count=Integer.parseInt(args[1]);if(count<1||count>1000)throw new IllegalArgumentException("Count1..1000");Files.createDirectories(out);
        for(int n=1;n<=count;n++)Files.writeString(out.resolve("l"+n+".json"),fixture(n));
        boolean recovery=false,powers=false;
        for(int n=11;n<=Math.min(100,count);n++){
            Level level=StrategyCatalog.get(n);Board board=new Board(level);int[] plan=board.solve();List<String> prefix=new ArrayList<>();
            for(int i=0;i<plan.length;i++){
                int action=plan[i];
                if(Board.isGateAction(action)&&!powers){
                    Board flowBoard=new Board(level);if(!flowBoard.restoreHistory(board.history()))throw new AssertionError("Flow prefix restore failed");
                    int[] flowPlan=flowBoard.solve();List<String> flow=new ArrayList<>();for(int f=0;f<Math.min(3,flowPlan.length);f++)flow.add(step(level,flowBoard,flowPlan[f]));
                    String value="{\"number\":"+n+",\"gateAction\":"+action+",\"prefix\":["+String.join(",",prefix)+"],\"flow\":["+String.join(",",flow)+"]}\n";
                    Files.writeString(out.resolve("powers.json"),value);powers=true;
                }
                prefix.add(step(level,board,action));
                if(Board.isGateAction(action)&&!recovery){
                    Tile blocked=null;for(Tile t:level.tiles)if(board.isPresent(t.id)&&t.switchMask==0&&!board.canMove(t.id)){blocked=t;break;}
                    if(blocked!=null){
                        MoveBudget budget=MoveBudget.start(0,level);
                        String value="{\"number\":"+n+",\"base\":"+budget.base()+",\"undoAction\":"+action+",\"prefix\":["+String.join(",",prefix)+"],\"blocked\":{\"id\":"+blocked.id+",\"row\":"+blocked.row+",\"col\":"+blocked.col+"}}\n";
                        Files.writeString(out.resolve("recovery.json"),value);recovery=true;
                    }
                }
            }
            if(recovery&&powers)break;
        }
        if(count>=30&&(!recovery||!powers))throw new AssertionError("Need source-certified gate recovery/power fixtures");
        System.out.println("Certified native action fixtures: "+count+"; recovery="+recovery+"; gate power="+powers);
    }
}
