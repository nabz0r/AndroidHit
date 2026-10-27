import com.androidhit.lumadrift.engine.*;
import java.util.*;

/** Targeted compatibility, malformed save, refund and authored-identity checks. */
public final class StrategyV4ContractTest {
    static int checks;static void check(boolean good,String detail){checks++;if(!good)throw new AssertionError(detail);}
    static void invalid(Runnable action){try{action.run();throw new AssertionError("Expected rejected input");}catch(IllegalArgumentException expected){checks++;}}
    public static void main(String[]args){
        Set<String> unique=new HashSet<>();int changedSwitches=0,keys=0,meaningful=0;
        for(boolean remix:new boolean[]{false,true})for(int n=1;n<=(remix?240:1000);n++){
            Level level=remix?StrategyCatalog.remix(n):StrategyCatalog.get(n);ProgressionInfo info=StrategyCatalog.info(n,remix);
            check(unique.add(ProgressionAnalysis.canonical(level)),"V4 maps unique under D4, translation and reordered IDs: "+(remix?"M":"L")+n);
            check(info.version==4&&info.minimumActions==level.minimumActions&&info.requiredGateOpens==level.minimumActions-level.tiles.size(),"Actual prelevel data");
            check(n>5||level.barriers.isEmpty(),"First five are spatial lessons");
            if(n>=6&&n<=10)check(info.keyCount>0&&info.requiredGateOpens==0,"Keys taught before gate operations");
            if(n>=11)check(info.requiredGateOpens>=1,"Productive cost exceeds removal count");
            if(!level.barriers.isEmpty())keys++;if(info.decisionPoints>0)meaningful++;
            if(n>=21){Level old=remix?BalancedCatalog.remix(info.sourceNumber):BalancedCatalog.get(info.sourceNumber);for(Tile t:level.tiles)if(t.switchMask!=old.tile(t.id).switchMask)changedSwitches++;}
            Board board=new Board(level);for(int action:level.actionSolution)check(board.performAction(action).success,"Typed path success");int[]saved=board.history();Board restored=new Board(level);check(restored.restoreHistory(saved)&&restored.isSolved(),"Typed history may exceed piece count");
            check(!restored.restoreHistory(new int[]{Integer.MAX_VALUE})&&restored.isSolved(),"Malformed restore is atomic");
            check(!restored.performAction(64).success&&restored.isSolved(),"Solved board cannot spend another gate action");
        }
        invalid(()->StrategyCatalog.get(0));invalid(()->StrategyCatalog.get(1001));invalid(()->StrategyCatalog.remix(0));invalid(()->StrategyCatalog.remix(241));
        invalid(()->Board.gateDirection(63));invalid(()->Board.gateDirection(68));
        Level legacy=BalancedCatalog.get(7);Board old=new Board(legacy);check(!old.performAction(64).success&&old.moves()==0,"Manual action never changes J/K rules");check(!old.restoreHistory(new int[]{64}),"Legacy saves reject gate actions");
        MoveBudget budget=MoveBudget.start(0,legacy);for(int id:new int[]{2,3})check(budget.consumeAction()&&old.move(id).success,"Migration exit");check(old.undo()&&budget.consumeAction(),"Historical paid Undo");check(budget.consumeAction(),"Historical blocked attempt");
        MoveBudget migrated=MoveBudget.toReversible(0,legacy,budget.used(),0,0,old.moves(),1);check(migrated!=null&&migrated.policyVersion()==3&&migrated.base()==budget.base()&&migrated.used()==2,"Policy2 migration refunds Undo overhead, retains blocked attempt");
        check(old.undo()&&migrated.refundAction()&&migrated.used()==1,"Refund original exit once, retains blocked attempts");
        MoveBudget unlimited=MoveBudget.toReversible(4,legacy,0,0,0,0,123);check(unlimited!=null&&unlimited.isUnlimited()&&unlimited.policyVersion()==3,"Unlimited old challenges migrate without negative refunds");
        Level v4=StrategyCatalog.get(11);MoveBudget b=MoveBudget.start(0,v4);while(b.consumeAction()){}check(b.extend(v4.minimumActions),"Continuation fixture");check(b.refundAction()&&MoveBudget.restoreVersion(3,0,v4,b.used(),b.extensions(),b.extraActions(),0,0)!=null,"Undo after extension can lower used below old exhaustion threshold");
        invalid(()->new Level(1,0,2,0,Arrays.asList(new Tile(0,0,0,Direction.UP,0)),new int[]{0},false,0,Collections.emptyList(),4,1,new int[]{0}));
        invalid(()->new Level(1,0,2,15,Arrays.asList(new Tile(0,0,0,Direction.UP,0)),new int[]{0},false,0,Collections.emptyList(),4,2,new int[]{64,0}));
        for(long seed:new long[]{0,1,Long.MAX_VALUE}){String code=ChallengeCode.encodeStrategyEvent(seed);check(ChallengeCode.parse(code).kind==ChallengeCode.Kind.STRATEGY_EVENT,"T event kind");check(ChallengeCode.parse(ChallengeCode.encodeEvent(seed)).resolve().progressionVersion==0,"E event remains historical");}
        check(changedSwitches>0&&keys>=500&&meaningful>=1000,"New route changes mechanics beyond gate-only reskin");
        System.out.println("PASS "+checks+" strategy contract checks;1240unique V4maps;key_boards="+keys+" meaningful_decision_boards="+meaningful+" reauthored_switches="+changedSwitches+";legacy/migration/malformed/zero-action contracts.");
    }
}
