import com.androidhit.lumadrift.engine.*;
import java.util.ArrayList;

/** Prints source-oracle board metadata and a certified path; never interacts with Android. */
public final class V15Fixture {
    private static int keyNumber(Level level,int id) {
        ArrayList<Integer> keys=new ArrayList<>();
        for(Barrier barrier:level.barriers)if(!keys.contains(barrier.keyTileId))keys.add(barrier.keyTileId);
        return keys.indexOf(id)+1;
    }
    private static String activeWalls(Board board) {
        StringBuilder result=new StringBuilder("[");boolean first=true;
        for(int i=0;i<board.level.barriers.size();i++)if(board.isBarrierActive(board.level.barriers.get(i))) {
            if(!first)result.append(',');first=false;result.append(i);
        }
        return result.append(']').toString();
    }
    public static void main(String[] args) {
        if(args.length!=2)throw new IllegalArgumentException("Family J/K/G/H/P/B/Q/C/R/E and value required");
        String family=args[0];long value=Long.parseLong(args[1]);Level level;String code;int mode=4;
        switch(family) {
            case "G":level=BalancedCatalog.getV2((int)value);code=ChallengeCode.encodeBalancedV2((int)value);mode=0;break;
            case "H":level=BalancedCatalog.remixV2((int)value);code=ChallengeCode.encodeBalancedRemixV2((int)value);mode=3;break;
            case "J":level=BalancedCatalog.get((int)value);code=ChallengeCode.encodeBalancedV3((int)value);mode=0;break;
            case "K":level=BalancedCatalog.remix((int)value);code=ChallengeCode.encodeBalancedRemixV3((int)value);mode=3;break;
            case "P":level=SealedCatalog.get((int)value);code=ChallengeCode.encodeSealed((int)value);break;
            case "B":level=BalancedCatalog.legacyGet((int)value);code=ChallengeCode.encodeBalanced((int)value);break;
            case "Q":level=BalancedCatalog.legacyRemix((int)value);code=ChallengeCode.encodeBalancedRemix((int)value);break;
            case "C":level=LevelCatalog.get((int)value);code=ChallengeCode.encodeCampaign((int)value);break;
            case "R":level=LevelCatalog.remix((int)value);code=ChallengeCode.encodeRemix((int)value);break;
            case "E":level=LevelCatalog.event(value);code=ChallengeCode.encodeEvent(value);mode=1;break;
            default:throw new IllegalArgumentException("Unknown family");
        }
        Board board=new Board(level);MoveBudget budget=MoveBudget.start(mode,level);
        StringBuilder output=new StringBuilder("{\"family\":\"").append(family).append("\",\"code\":\"").append(code)
                .append("\",\"number\":").append(level.number).append(",\"chapter\":").append(level.chapter)
                .append(",\"size\":").append(level.size).append(",\"seed\":").append(level.seed)
                .append(",\"gates\":").append(level.initialGateMask).append(",\"base\":").append(budget.base())
                .append(",\"margin\":").append(budget.mistakeAllowance()).append(",\"policy\":").append(budget.policyVersion())
                .append(",\"tiles\":[");
        for(Tile tile:level.tiles) {
            if(tile.id>0)output.append(',');
            output.append("{\"id\":").append(tile.id).append(",\"row\":").append(tile.row).append(",\"col\":").append(tile.col)
                    .append(",\"direction\":").append(tile.direction.ordinal()).append(",\"switch\":").append(tile.switchMask)
                    .append(",\"keyNumber\":").append(keyNumber(level,tile.id)).append('}');
        }
        output.append("],\"barriers\":[");
        for(int i=0;i<level.barriers.size();i++) {
            Barrier wall=level.barriers.get(i);if(i>0)output.append(',');
            output.append("{\"row\":").append(wall.row).append(",\"col\":").append(wall.col)
                    .append(",\"key\":").append(wall.keyTileId).append(",\"keyNumber\":").append(keyNumber(level,wall.keyTileId)).append('}');
        }
        output.append("],\"steps\":[");
        for(int i=0;i<level.solution.length;i++) {
            int id=level.solution[i];Tile tile=level.tile(id);
            if(!board.move(id).success)throw new AssertionError("Fixture path is not a certified completion");
            if(i>0)output.append(',');
            output.append("{\"id\":").append(id).append(",\"row\":").append(tile.row).append(",\"col\":").append(tile.col)
                    .append(",\"gates\":").append(board.gateMask()).append(",\"activeBarriers\":").append(activeWalls(board)).append('}');
        }
        output.append("]}");
        if(!board.isSolved())throw new AssertionError("Fixture did not clear its board");
        System.out.println(output);
    }
}
