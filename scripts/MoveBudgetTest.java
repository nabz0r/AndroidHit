import com.androidhit.lumadrift.engine.*;
import java.util.Arrays;
import java.util.Random;

/** Pure deterministic policy, actual Board actions and corrupt-save simulations. */
public final class MoveBudgetTest {
    private static long checks;
    private static int boards;
    private static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
    private static void expectInvalid(int mode,Level level){boolean rejected=false;try{MoveBudget.start(mode,level);}catch(IllegalArgumentException expected){rejected=true;}check(rejected,"Invalid start rejected");}
    private static Level blockedFixture(){return new Level(1,1,2,15,Arrays.asList(new Tile(0,0,0,Direction.RIGHT,0),new Tile(1,0,1,Direction.UP,0)),new int[]{1,0},false,17);}
    private static void solution(Level level,int mode){
        boards++;Board board=new Board(level);MoveBudget budget=MoveBudget.start(mode,level);
        check(budget.minimumActions()==level.tiles.size(),"Exact necessary N exits");
        check(budget.base()>level.tiles.size(),"Initial certified completion has mistake margin");
        for(int id:level.solution){
            check(budget.canAct()&&budget.consumeAction(),"Certified completion fits without continuation");
            check(board.move(id).success,"Actual certified exit succeeds");
            MoveBudget restored=MoveBudget.restore(mode,level,budget.used(),budget.extensions(),budget.extraActions(),board.moves(),0);
            check(restored!=null&&restored.remaining()==budget.remaining(),"Every valid history prefix restores");
        }
        check(board.isSolved(),"Actual board solved");check(budget.remaining()==budget.base()-level.tiles.size(),"Only N actions were required");
        check(budget.extensions()==0&&!budget.canExtend(),"No continuation needed for any certified board");
    }
    private static void policies(){
        int[] n={1,3,4,6,7,30,31,90,91,240};int[] margin={8,8,6,6,5,5,4,4,3,3};
        for(int i=0;i<n.length;i++){Level level=LevelCatalog.get(n[i]);check(MoveBudget.startLegacy(0,level).base()-level.tiles.size()==margin[i],"Published Journey boundary "+n[i]);}
        int[] r={1,3,4,12,13,60,61,240};int[] rm={6,6,5,5,4,4,3,3};
        for(int i=0;i<r.length;i++){Level level=LevelCatalog.remix(r[i]);check(MoveBudget.startLegacy(3,level).base()-level.tiles.size()==rm[i],"Published Remix boundary "+r[i]);}
        Level event=LevelCatalog.event(1);check(MoveBudget.startLegacy(1,event).base()==event.tiles.size()+5,"Daily margin");check(MoveBudget.startLegacy(2,event).base()==event.tiles.size()+4,"Weekly margin");
        for(int number=1;number<=240;number++)for(boolean remix:new boolean[]{false,true}){
            Level board=remix?BalancedCatalog.remix(number):BalancedCatalog.get(number);
            MoveBudget current=MoveBudget.start(remix?3:0,board);
            check(current.policyVersion()==2&&current.snapshot().policyVersion==2,"Current attempt uses version two");
            check(current.mistakeAllowance()==BalancedCatalog.mistakeAllowance(number,remix),"Per-layout frozen allowance");
            check(current.mistakeAllowance()>=2&&current.mistakeAllowance()<=4,"Every board permits a free undo repair");
            Level old=remix?BalancedCatalog.legacyRemix(number):BalancedCatalog.legacyGet(number);
            MoveBudget legacy=MoveBudget.startLegacy(remix?3:0,old);legacy.consumeAction();
            check(legacy.policyVersion()==1&&legacy.snapshot().policyVersion==1&&legacy.copy().policyVersion()==1,"Interrupted legacy attempt keeps its policy");
            MoveBudget restored=MoveBudget.restoreLegacy(remix?3:0,old,1,0,0,0,0);
            check(restored!=null&&restored.base()==legacy.base()&&restored.used()==1&&restored.policyVersion()==1,"Legacy attempt resumes its original allowance");
        }
        check(MoveBudget.GOLD_EXTENSION_COST==100&&MoveBudget.MAX_EXTENSIONS==2,"Transparent fixed price and shared quota");
        expectInvalid(-1,event);expectInvalid(5,event);expectInvalid(0,event);expectInvalid(3,event);expectInvalid(1,LevelCatalog.get(1));expectInvalid(2,LevelCatalog.get(1));expectInvalid(0,null);
    }
    private static void actualActions(){
        Level level=blockedFixture();Board board=new Board(level);MoveBudget budget=MoveBudget.start(0,level);
        check(!board.undo()&&budget.used()==0,"Empty undo does not consume a controller action");
        for(int i=0;i<budget.base();i++){
            check(board.blockReason(0)==Board.BlockReason.BLOCKED_PATH,"Present blocked piece remains blocked");
            check(!board.move(0).success&&budget.consumeAction(),"A blocked present-piece attempt consumes one action");
        }
        check(board.moves()==0&&board.remainingCount()==2&&budget.isExhausted(),"Failure attempts exhaust budget without removing pieces");
        check(!budget.consumeAction()&&budget.remaining()==0,"Further consumption never underflows");
        for(int invalid:new int[]{Integer.MIN_VALUE,-1,0,3,Integer.MAX_VALUE})check(budget.extensionActions(invalid)==0,"Invalid remaining count cannot grant actions");
        MoveBudget prepared=budget.copy();check(prepared.extend(2)&&prepared.remaining()==5,"Prepared extension grants at least five actions");
        check(budget.remaining()==0&&budget.extensions()==0,"Preparing extension never mutates the active run");
        check(!prepared.extend(2)&&prepared.extensions()==1,"Duplicate callback while actions remain cannot extend again");
        while(prepared.canAct())check(prepared.consumeAction(),"Consume first extension");
        check(prepared.extend(2)&&prepared.extensions()==2,"Second earned extension allowed");
        while(prepared.canAct())check(prepared.consumeAction(),"Consume second extension");
        check(!prepared.extend(2)&&!prepared.canExtend(),"Ad and gold share a maximum of two continuations");
        MoveBudget restored=MoveBudget.restore(0,level,prepared.used(),2,prepared.extraActions(),0,0);
        check(restored!=null&&restored.isExhausted(),"Exhausted two-extension state restores");
        MoveBudget fresh=MoveBudget.start(0,level);check(fresh.used()==0&&fresh.extensions()==0&&fresh.extraActions()==0,"Restart resets counters without currency or extension use");
        // Successful move + undo + redo consumes three actions but leaves one tile removed.
        board.restart();check(fresh.consumeAction()&&board.move(1).success,"Successful exit costs one");
        check(board.undo()&&fresh.consumeAction(),"Successful undo costs one");
        check(fresh.consumeAction()&&board.move(1).success,"Redo costs another one");
        check(board.moves()==1&&fresh.used()==3,"Real undo accounting is moves + twice undos");
        check(MoveBudget.restore(0,level,3,0,0,1,1)!=null,"Coherent undo ledger restores");
        check(MoveBudget.restore(0,level,2,0,0,1,1)==null,"A free undo or redo cannot be smuggled through restore");
        // A win on the very last action is valid; the controller checks Board.isSolved before exhaustion UI.
        board.restart();fresh=MoveBudget.start(0,level);
        for(int i=0;i<fresh.base()-2;i++)check(!board.move(0).success&&fresh.consumeAction(),"Waste only the explicit margin");
        for(int id:level.solution)check(fresh.consumeAction()&&board.move(id).success,"Final exact-budget solution exits");
        check(board.isSolved()&&fresh.remaining()==0,"Last action wins; no continuation is necessary");
        Level many=LevelCatalog.event(77);MoveBudget large=MoveBudget.start(1,many);while(large.canAct())large.consumeAction();
        check(large.extensionActions(many.tiles.size())==many.tiles.size()+3,"Continuation covers every remaining exit plus three mistakes");
        check(large.extend(many.tiles.size())&&large.remaining()>=many.tiles.size()+3,"A large unsolved board never gets only five actions");
    }
    private static void snapshotsAndFuzz(){
        Level level=LevelCatalog.remix(240);MoveBudget start=MoveBudget.start(3,level);int base=start.base(),n=level.tiles.size();
        int[][] invalid={{-1,0,0,0,0},{0,-1,0,0,0},{0,3,0,0,0},{0,0,1,0,0},{base+1,0,0,0,0},{base,1,4,0,0},{base,1,n+4,0,0},{base-1,1,5,0,0},{base,2,10,0,0},{0,0,0,n+1,0},{0,0,0,0,-1},{1,0,0,0,1}};
        for(int[] a:invalid)check(MoveBudget.restore(3,level,a[0],a[1],a[2],a[3],a[4])==null,"Malformed snapshot rejected");
        MoveBudget.Snapshot snapshot=start.snapshot();check(snapshot.policyVersion==2&&snapshot.mode==3&&snapshot.base==base&&snapshot.minimumActions==n,"Snapshot describes public policy");
        check(snapshot.used==0&&snapshot.extensions==0&&snapshot.extraActions==0,"Snapshot fields are independent values");start.consumeAction();check(snapshot.used==0,"Previously taken snapshot remains immutable");
        Random random=new Random(0x4D4F564542554447L);
        for(int i=0;i<100000;i++){
            int[] a={0,0,0,0,0};int column=i%5;
            a[column]=random.nextBoolean()?Integer.MIN_VALUE:Integer.MAX_VALUE;
            check(MoveBudget.restore(3,level,a[0],a[1],a[2],a[3],a[4])==null,"Extreme-value corrupt save rejects without overflow");
        }
        for(int i=0;i<20000;i++){
            MoveBudget budget=MoveBudget.start(3,level);int steps=random.nextInt(base+1);for(int k=0;k<steps;k++)budget.consumeAction();
            int moves=random.nextInt(Math.min(n,budget.used())+1),undos=random.nextInt((budget.used()-moves)/2+1);
            MoveBudget restored=MoveBudget.restore(3,level,budget.used(),0,0,moves,undos);
            check(restored!=null&&restored.used()==steps&&restored.remaining()==base-steps,"Valid generated counters restore independently");
            check(budget.used()==steps&&budget.extensions()==0,"Restore does not mutate source budget");
        }
        check(MoveBudget.restore(-1,level,0,0,0,0,0)==null&&MoveBudget.restore(3,null,0,0,0,0,0)==null,"Invalid restore identity returns null");
    }
    private static void friend(){
        Level level=LevelCatalog.get(1);MoveBudget friend=MoveBudget.start(4,level);
        for(int i=0;i<100000;i++)check(friend.consumeAction()&&friend.canAct(),"Friend challenge remains unlimited");
        check(friend.isUnlimited()&&friend.remaining()==-1&&friend.used()==0&&friend.base()==0,"No hidden friend budget ledger");
        check(!friend.isExhausted()&&!friend.canExtend()&&!friend.extend(1),"Friend has no ad/gold continuation");
        check(MoveBudget.restore(4,level,0,0,0,level.tiles.size(),Integer.MAX_VALUE)!=null,"Unlimited friend history does not overflow limited-budget accounting");
        check(MoveBudget.restore(4,level,1,0,0,0,0)==null&&MoveBudget.restore(4,level,0,1,5,0,0)==null,"Paid or consumed friend snapshots rejected");
    }
    public static void main(String[] args){
        long began=System.nanoTime();policies();actualActions();snapshotsAndFuzz();friend();
        for(int i=1;i<=LevelCatalog.LEVEL_COUNT;i++)solution(LevelCatalog.get(i),0);
        for(int i=1;i<=LevelCatalog.REMIX_COUNT;i++)solution(LevelCatalog.remix(i),3);
        for(int i=1;i<=BalancedCatalog.LEVEL_COUNT;i++)solution(BalancedCatalog.get(i),0);
        for(int i=1;i<=BalancedCatalog.REMIX_COUNT;i++)solution(BalancedCatalog.remix(i),3);
        for(int i=0;i<1000;i++){long seed=i<500?20261300L+i:new Random(i*137L).nextLong();solution(LevelCatalog.event(seed),i%2==0?1:2);}
        solution(LevelCatalog.event(Long.MIN_VALUE),1);solution(LevelCatalog.event(Long.MAX_VALUE),2);
        System.out.println("PASS "+checks+" move-budget checks; "+boards+" real certified boards, 100000 corrupt saves, 20000 valid snapshots, 100000 unlimited friend actions.");
        System.out.println("Verified: blocked attempts/undo accounting, fixed100gold, max2continuations, remaining+3 extension floor5, last-action win, copy isolation and overflow rejection.");
        System.out.printf(java.util.Locale.ROOT,"Runtime %.2fs. No ad completion, payment, retention or human difficulty is inferred.%n",(System.nanoTime()-began)/1e9);
    }
}
