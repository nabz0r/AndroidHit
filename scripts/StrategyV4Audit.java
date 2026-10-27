import com.androidhit.lumadrift.engine.*;
import java.nio.file.*;
import java.util.*;

/** Independent bottom-up gate-cube oracle plus exhaustive runtime action/Undo traversal. */
public final class StrategyV4Audit {
    static long checks,states,edges,boards,referenceStates,plans;static long worstNs;static int peakBytes,peakMemo;
    static final Direction[] DIRECTIONS=Direction.values();
    static final StringBuilder csv=new StringBuilder("family,number,pieces,minimum,base,keys,seals,spatial_subsets,reachable_action_states,action_edges,planner_cached_states,planner_cache_bytes,max_observed_cost_ns\n");
    static void check(boolean ok,String detail){checks++;if(!ok)throw new AssertionError(detail);}
    static final class Reference {
        final Level level;final long[] obstruction;final byte[] costs;long spatial;
        Reference(Level level){this.level=level;obstruction=new long[level.tiles.size()];costs=new byte[1<<(level.tiles.size()+4)];for(int g=0;g<16;g++)costs[g]=1;
            for(Tile t:level.tiles)for(int row=t.row+t.direction.dr,col=t.col+t.direction.dc;row>=0&&col>=0&&row<level.size&&col<level.size;row+=t.direction.dr,col+=t.direction.dc){for(Tile b:level.tiles)if(b.row==row&&b.col==col)obstruction[t.id]|=1L<<b.id;for(Barrier b:level.barriers)if(b.row==row&&b.col==col)obstruction[t.id]|=1L<<b.keyTileId;}
            fill(level.allTilesMask);
        }
        void fill(long mask){int start=(int)(mask<<4);if(costs[start]!=0)return;spatial++;long free=0;
            for(Tile t:level.tiles)if((mask&(1L<<t.id))!=0&&(mask&obstruction[t.id])==0){free|=1L<<t.id;fill(mask&~(1L<<t.id));}
            if(free==0)throw new AssertionError("Spatially cyclic content");
            // Children have fewer pieces. For one mask, all supersets of a gate state are
            // numerically larger; relaxing them 15→0 independently solves its four-bit cube.
            for(int g=15;g>=0;g--){int best=200;
                for(Tile t:level.tiles)if((free&(1L<<t.id))!=0&&(g&t.direction.bit)!=0)best=Math.min(best,(costs[(int)(((mask&~(1L<<t.id))<<4)|(g^t.switchMask))]&255)-1);
                for(Direction d:DIRECTIONS)if((g&d.bit)==0)best=Math.min(best,1+(costs[start|(g|d.bit)]&255)-1);
                if(best>=200)throw new AssertionError("Gate cube did not recover");costs[start|g]=(byte)(best+1);
            }
        }
        int cost(long mask,int gates){return Long.bitCount(mask)+(costs[(int)((mask<<4)|gates)]&255)-1;}
        boolean legal(long mask,int gates,Tile tile){return(mask&(1L<<tile.id))!=0&&(mask&obstruction[tile.id])==0&&(gates&tile.direction.bit)!=0;}
    }
    static long boardStates,boardEdges,boardNs;
    static void walk(Board board,Reference ref,BitSet seen){
        long mask=board.remainingMask();int gates=board.gateMask(),key=(int)((mask<<4)|gates);if(seen.get(key))return;seen.set(key);states++;boardStates++;
        long begin=System.nanoTime();int cost=board.minimumActionsToFinish();long elapsed=System.nanoTime()-begin;boardNs=Math.max(boardNs,elapsed);worstNs=Math.max(worstNs,elapsed);
        check(cost==ref.cost(mask,gates),"Independent exact productive cost");check(board.hasSolution()&&!board.isDeadEnd(),"Every reachable strategy state is recoverable");
        check(board.isSolved()==(mask==0),"Solved state");check(!board.isStuck(),"Manual gate recovery prevents logical stuck state");
        if(boardStates%2048==1||mask==0){int[] saved=board.history();int[] plan=board.solve();plans++;check(plan.length==cost,"Shortest typed hint plan");Board copy=new Board(board.level);check(copy.restoreHistory(saved),"Typed save replays");for(int action:plan)check(copy.performAction(action).success,"Hint action legal");check(copy.isSolved(),"Hint plan completes");check(Arrays.equals(saved,board.history()),"Planning is nonmutating");if(mask!=0){Board.Guidance guidance=board.guidance(cost);check(guidance.actionsToFinish==cost&&guidance.canUseHint(),"Exact guidance fits required actions");check(board.guidance(cost-1).status==Board.PositionStatus.NEEDS_ACTIONS,"One short action detected");}}
        for(Tile tile:board.level.tiles){boolean allowed=ref.legal(mask,gates,tile);check(board.canMove(tile.id)==allowed,"Independent ray/key/gate legality");Board.ActionPreview preview=board.previewAction(tile.id);check(preview.legal==allowed,"Preview legality");if(allowed){check(preview.gateMask==(gates^tile.switchMask)&&preview.openedMask==((gates^tile.switchMask)&~gates)&&preview.closedMask==(gates&~(gates^tile.switchMask)),"Preview physical consequences");check(board.performAction(tile.id).success,"Runtime exit transition");edges++;boardEdges++;walk(board,ref,seen);check(board.undo()&&board.remainingMask()==mask&&board.gateMask()==gates,"Exit Undo restores exact state");}}
        for(Direction d:DIRECTIONS){int action=Board.gateAction(d);boolean allowed=mask!=0&&(gates&d.bit)==0;Board.ActionPreview preview=board.previewAction(action);check(preview.legal==allowed,"Gate preview legality");if(allowed){check(preview.openedMask==d.bit&&preview.closedMask==0,"Opening cannot close gates");check(board.performAction(action).success&&board.gateMask()==(gates|d.bit),"Runtime gate transition");edges++;boardEdges++;walk(board,ref,seen);check(board.undo()&&board.remainingMask()==mask&&board.gateMask()==gates,"Gate Undo restores exact state");}}
    }
    static void budget(Level level,boolean remix){
        Board board=new Board(level);MoveBudget budget=MoveBudget.start(remix?3:0,level);check(budget.policyVersion()==3&&budget.minimumActions()==level.minimumActions,"Versioned productive budget");
        for(int action:level.actionSolution)check(budget.consumeAction()&&board.performAction(action).success,"Certificate fits budget without goods");check(board.isSolved(),"Certificate complete");
        int base=budget.base();while(board.canUndo()){check(board.undo()&&budget.refundAction(),"Undo refunds productive action for free");check(MoveBudget.restoreVersion(3,remix?3:0,level,budget.used(),0,0,board.moves(),0)!=null,"Refunding ledger restores");}
        check(budget.used()==0&&budget.remaining()==base,"Complete rollback restores all productive budget");check(!budget.refundAction(),"Empty refund rejected");
        check(board.performAction(level.actionSolution[0]).success&&budget.consumeAction(),"Zero-budget fixture action");while(budget.consumeAction()){}check(budget.isExhausted()&&board.undo()&&budget.refundAction()&&budget.remaining()==1,"Undo works and refunds at zero");
        check(MoveBudget.restoreVersion(2,remix?3:0,level,0,0,0,0,0)==null,"V4 cannot use old budget policy");
    }
    static void board(Level level,boolean remix){
        boards++;Reference ref=new Reference(level);referenceStates+=(ref.spatial+1)*16;check(ref.cost(level.allTilesMask,level.initialGateMask)==level.minimumActions,"Frozen minimum independently certified");budget(level,remix);
        Board runtime=new Board(level);boardStates=boardEdges=boardNs=0;walk(runtime,ref,new BitSet(1<<(level.tiles.size()+4)));check(runtime.moves()==0,"Traversal preserves initial history");
        peakMemo=Math.max(peakMemo,runtime.plannerCachedStates());peakBytes=Math.max(peakBytes,runtime.plannerCacheBytes());check(runtime.plannerCachedStates()<=StrategySolver.MAX_CACHED_STATES,"Bounded exact cache");
        ProgressionInfo info=StrategyCatalog.info(level.number,remix);csv.append(remix?'M':'L').append(',').append(level.number).append(',').append(level.tiles.size()).append(',').append(level.minimumActions).append(',').append(MoveBudget.start(remix?3:0,level).base()).append(',').append(info.keyCount).append(',').append(level.barriers.size()).append(',').append(ref.spatial+1).append(',').append(boardStates).append(',').append(boardEdges).append(',').append(runtime.plannerCachedStates()).append(',').append(runtime.plannerCacheBytes()).append(',').append(boardNs).append('\n');
        String code=remix?ChallengeCode.encodeStrategyRemix(level.number):ChallengeCode.encodeStrategy(level.number);check(ChallengeCode.parse(code).resolve()==level,"Frozen L/M identity resolves");
    }
    static void events(){Set<String>geometries=new HashSet<>();Set<Integer>sourceNumbers=new HashSet<>();
        for(long seed=0;seed<30000&&sourceNumbers.size()<980;seed++){Level event=StrategyCatalog.event(seed);check(event.isEvent&&event.progressionVersion==4&&event.seed==seed,"Typed event identity");
            String fingerprint=event.size+":"+event.initialGateMask+":"+Arrays.toString(event.actionSolution)+":"+event.tiles.get(0).row+":"+event.tiles.get(0).col;
            if(!geometries.add(fingerprint))continue;Board board=new Board(event);for(int action:event.actionSolution)check(board.performAction(action).success,"Event certificate legal");check(board.isSolved(),"Event certificate completes");
            // Identity has an infinite seed domain, but selects only these audited L geometries.
            long mixed=seed^(seed>>>33);mixed*=0xff51afd7ed558ccdL;mixed^=mixed>>>33;sourceNumbers.add(21+(int)Math.floorMod(mixed,980L));
            String code=ChallengeCode.encodeStrategyEvent(seed);check(ChallengeCode.parse(code).resolve().minimumActions==event.minimumActions,"T code retains rule family");
        }
        check(sourceNumbers.size()==980,"All 980 possible event geometries represented");
        for(long seed:new long[]{Long.MIN_VALUE,-1,Long.MAX_VALUE,0,369472826910L}){Level event=StrategyCatalog.event(seed);Board board=new Board(event);for(int action:event.actionSolution)check(board.performAction(action).success,"Extreme-seed event certificate");check(board.isSolved(),"Extreme event seed completes");}
    }
    static void legacyRecovery(){for(int n:new int[]{7,8,13}){Level old=BalancedCatalog.get(n);Board board=new Board(old);MoveBudget budget=MoveBudget.start(0,old);int[]bad=n==7?new int[]{2,3,5}:n==8?new int[]{0,1,2,5}:new int[]{0,1,2,3,8};for(int id:bad)check(budget.consumeAction()&&board.move(id).success,"Legacy trap fixture");check(board.isDeadEnd(),"Legacy content unchanged");MoveBudget migrated=MoveBudget.toReversible(0,old,budget.used(),0,0,board.moves(),0);check(migrated!=null&&migrated.base()==budget.base(),"Legacy policy2 migration retains base");check(board.undo()&&migrated.refundAction()&&board.hasSolution(),"Legacy trap repaired without added goods or action cost");}}
    public static void main(String[]args)throws Exception{Path report=Path.of(args.length==0?"docs/validation/v1.8/engine":args[0]);Files.createDirectories(report);long start=System.nanoTime();legacyRecovery();for(boolean remix:new boolean[]{false,true})for(int n=1;n<=(remix?240:1000);n++){board(remix?StrategyCatalog.remix(n):StrategyCatalog.get(n),remix);if(n%100==0)System.out.println((remix?"M":"L")+n+" states="+states+" checks="+checks);}events();Files.writeString(report.resolve("strategy-all-states.csv"),csv);String summary="PASS StrategyV4Audit boards="+boards+" reachable_states="+states+" action_edges="+edges+" reference_gate_states="+referenceStates+" checks="+checks+" sampled_complete_plans="+plans+" peak_cache_states="+peakMemo+" peak_cache_bytes="+peakBytes+" worst_observed_cost_ns="+worstNs+" elapsed_seconds="+((System.nanoTime()-start)/1e9);Files.writeString(report.resolve("strategy-all-states.txt"),summary+"\n");System.out.println(summary);}
}
