import com.androidhit.lumadrift.engine.*;
import java.util.*;

/** Fixed-cell key mechanics: exact independent oracle, reversible history and hostile inputs. */
public final class SealedTest {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++;if(!condition)throw new AssertionError(message);
    }
    private static void rejects(Runnable operation,String message) {
        boolean rejected=false;
        try{operation.run();}catch(IllegalArgumentException expected){rejected=true;}
        check(rejected,message);
    }
    private static boolean onRay(Tile tile,int row,int col) {
        int dr=row-tile.row,dc=col-tile.col;
        return tile.direction.dr==0?dr==0&&dc*tile.direction.dc>0:dc==0&&dr*tile.direction.dr>0;
    }
    /** Intentionally separate from Board.canMove and the production solver. */
    private static boolean allowed(Level level,long mask,int gates,Tile tile) {
        if((mask&(1L<<tile.id))==0||(gates&tile.direction.bit)==0)return false;
        for(Tile other:level.tiles)if(other.id!=tile.id&&(mask&(1L<<other.id))!=0&&onRay(tile,other.row,other.col))return false;
        for(Barrier barrier:level.barriers)if((mask&(1L<<barrier.keyTileId))!=0&&onRay(tile,barrier.row,barrier.col))return false;
        return true;
    }
    private static boolean oracle(Level level,long mask,int gates,Map<Long,Boolean> cache) {
        if(mask==0)return true;
        long key=(mask<<4)|gates;
        if(cache.containsKey(key))return cache.get(key);
        for(Tile tile:level.tiles)if(allowed(level,mask,gates,tile)
                &&oracle(level,mask&~(1L<<tile.id),gates^tile.switchMask,cache)) {
            cache.put(key,true);return true;
        }
        cache.put(key,false);return false;
    }
    private static void board(int number) {
        Level level=SealedCatalog.get(number);
        check(level==SealedCatalog.get(number)&&level.number==number,"Stable standalone identity");
        check(!level.isEvent&&level.chapter==0&&!level.barriers.isEmpty(),"Prototype is distinct from dated events/campaigns");
        ChallengeCode code=ChallengeCode.parse(ChallengeCode.encodeSealed(number));
        check(code!=null&&code.kind==ChallengeCode.Kind.SEALED&&code.value==number,"P challenge round trip");
        Board board=new Board(level);
        for(Barrier barrier:level.barriers)check(board.isBarrierActive(barrier)&&level.isKey(barrier.keyTileId),"Each seal has a visible active key");
        int obstructed=0;
        for(Tile tile:level.tiles)if(board.blockingBarrier(tile.id)!=null) {
            obstructed++;
            long mask=board.remainingMask();int gates=board.gateMask();
            check(!board.move(tile.id).success,"A sealed ray really blocks movement");
            check(board.remainingMask()==mask&&board.gateMask()==gates&&board.moves()==0,"Blocked obstacle tap is atomic");
            check(!board.restoreHistory(new int[]{tile.id})&&board.remainingMask()==mask,"History cannot pass through a closed seal");
        }
        check(obstructed>0,"Every prototype board contains an actual initially obstructed ray");
        for(int id:level.solution) {
            long before=board.remainingMask();int gates=board.gateMask();
            check(board.move(id).success,"Hand-authored certified solution exits");
            for(Barrier barrier:level.barriers)if(barrier.keyTileId==id) {
                check(!board.isBarrierActive(barrier),"Key exit opens its fixed cell");
            }
            check(board.undo()&&board.remainingMask()==before&&board.gateMask()==gates,"Undo restores pieces, gates and seals together");
            for(Barrier barrier:level.barriers)if(barrier.keyTileId==id)check(board.isBarrierActive(barrier),"Undo reseals a key's fixed cells");
            check(board.move(id).success,"Key exit can be redone");
            Board restored=new Board(level);
            check(restored.restoreHistory(board.history())&&restored.remainingMask()==board.remainingMask()
                    &&restored.gateMask()==board.gateMask(),"Serialized history reconstructs obstacle state without extra mutable flags");
            for(Barrier barrier:level.barriers)check(restored.isBarrierActive(barrier)==board.isBarrierActive(barrier),"Restored seal state matches");
        }
        check(board.isSolved(),"Prototype completes without powers");
        MoveBudget budget=MoveBudget.start(4,level);
        check(budget.isUnlimited()&&!budget.canExtend(),"Workshop uses unlimited friend rules without paid recovery");
        board.restart();
        check(board.moves()==0&&board.remainingCount()==level.tiles.size(),"Restart restores the full prototype");
        for(Barrier barrier:level.barriers)check(board.isBarrierActive(barrier),"Restart reseals every fixed cell");
        check(board.blockingBarrier(-1)==null&&!board.isBarrierActive(new Barrier(0,0,0)),"Foreign/invalid obstacle lookups are harmless");

        Map<Long,Boolean> oracleCache=new HashMap<>();
        for(long mask=0;mask<=level.allTilesMask;mask++)for(int gates=0;gates<16;gates++) {
            int[] solution=Solver.solve(level,mask,gates);
            check((solution!=null)==oracle(level,mask,gates,oracleCache),"Exact solver agrees with independent fixed-cell oracle on every subset/gate state");
            if(solution!=null) {
                long remaining=mask;int activeGates=gates;
                for(int id:solution) {
                    Tile tile=level.tile(id);
                    check(tile!=null&&allowed(level,remaining,activeGates,tile),"Returned exact path respects every still-sealed cell");
                    remaining&=~(1L<<id);activeGates^=tile.switchMask;
                }
                check(remaining==0,"Every returned obstacle solution removes every requested piece");
            }
        }
    }
    private static Level fixture(List<Barrier> barriers) {
        return new Level(1,0,3,15,Arrays.asList(new Tile(0,0,0,Direction.UP,0)),new int[]{0},false,1,barriers);
    }
    private static void hostileInputs() {
        rejects(()->new Barrier(-1,0,0),"Negative row rejected");
        rejects(()->new Barrier(0,-1,0),"Negative column rejected");
        rejects(()->new Barrier(0,0,-1),"Negative key rejected");
        rejects(()->fixture(null),"Missing barrier list rejected");
        rejects(()->fixture(Arrays.asList((Barrier)null)),"Null barrier rejected");
        rejects(()->fixture(Arrays.asList(new Barrier(3,0,0))),"Off-board barrier rejected");
        rejects(()->fixture(Arrays.asList(new Barrier(1,1,1))),"Unknown key rejected");
        rejects(()->fixture(Arrays.asList(new Barrier(1,1,Integer.MAX_VALUE))),"Huge key cannot wrap a bit shift");
        rejects(()->fixture(Arrays.asList(new Barrier(0,0,0))),"Fixed cell cannot overlap a piece/key");
        rejects(()->fixture(Arrays.asList(new Barrier(1,1,0),new Barrier(1,1,0))),"Fixed cells cannot overlap");
        ArrayList<Barrier> mutable=new ArrayList<>();mutable.add(new Barrier(1,1,0));Level copied=fixture(mutable);mutable.clear();
        check(copied.barriers.size()==1,"Caller mutation cannot remove level barriers");
        boolean immutable=false;try{copied.barriers.clear();}catch(UnsupportedOperationException expected){immutable=true;}
        check(immutable,"Level barrier list is immutable");
        for(int number:new int[]{Integer.MIN_VALUE,-1,0,7,Integer.MAX_VALUE}) {
            rejects(()->SealedCatalog.get(number),"Invalid sealed catalog number rejected");
            rejects(()->ChallengeCode.encodeSealed(number),"Invalid sealed code number rejected");
        }
    }
    public static void main(String[] args) {
        for(int number=1;number<=SealedCatalog.COUNT;number++)board(number);
        Board first=new Board(SealedCatalog.get(1));
        Board.MoveResult sealed=first.move(1);
        check(!sealed.success&&sealed.reason==Board.BlockReason.LOCKED_BARRIER
                &&sealed.blockingBarrier==first.level.barriers.get(0),"UI receives the precise fixed-cell blocker and reason");
        check(first.move(0).success&&first.move(1).success,"The formerly obstructed path becomes traversable after its key exits");
        check(!new Board(SealedCatalog.get(5)).isSafeMove(0),"Taking the final key too early is a real gate-order trap");
        Board fifth=new Board(SealedCatalog.get(5));check(fifth.move(1).success&&fifth.isSafeMove(0),"Clearing the downward piece makes the key safe");
        hostileInputs();
        for(int number=1;number<=240;number++) {
            check(LevelCatalog.get(number).barriers.isEmpty()&&LevelCatalog.remix(number).barriers.isEmpty(),"C/R historical maps retain empty obstacle sets");
            check(BalancedCatalog.legacyGet(number).barriers.isEmpty()&&BalancedCatalog.legacyRemix(number).barriers.isEmpty(),"B/Q historical maps retain empty obstacle sets");
            check(BalancedCatalog.remix(number).barriers.isEmpty(),"H progression remains independent from prototype");
        }
        for(int number=1;number<=1000;number++)check(BalancedCatalog.get(number).barriers.isEmpty(),"G progression remains independent from prototype");
        System.out.println("PASS "+checks+" sealed-cell checks; all 6 P boards, every piece-subset ×16 gate states, exact independent oracle, undo/history/restart, hostile definitions and isolation.");
    }
}
