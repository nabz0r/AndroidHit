import com.androidhit.lumadrift.engine.*;
import java.nio.file.*;
import java.util.*;

/** Independent coordinate/work oracle, exhaustive productive-state traversal, save and budget contracts. */
public final class GuardianV19Test {
    static long checks,states,edges,plans;static int largestStates;static long boardStates,boardEdges;
    static void check(boolean ok,String reason){checks++;if(!ok)throw new AssertionError(reason);}
    static int work(GuardianEncounter.Snapshot s){int result=0;for(GuardianEncounter.Piece p:s.pieces)if(p.present){result+=p.layers-1;if(!p.cellwise)result++;else switch(p.direction){case RIGHT:result+=s.size-p.col;break;case LEFT:result+=p.col+1;break;case UP:result+=p.row+1;break;case DOWN:result+=s.size-p.row;break;}}return result;}
    static boolean allowed(GuardianEncounter.Snapshot s,GuardianEncounter.Piece p){if(!p.present)return false;if(p.layers>1)return true;
        for(GuardianEncounter.Piece other:s.pieces)if(other.present&&other.id!=p.id){int dr=other.row-p.row,dc=other.col-p.col;
            boolean ray=p.direction.dr==0?dr==0&&dc*p.direction.dc>0:dc==0&&dr*p.direction.dr>0;
            if(ray&&(!p.cellwise||Math.abs(dr)+Math.abs(dc)==1))return false;
        }return true;
    }
    static long key(GuardianEncounter.Snapshot s){long key=0;for(GuardianEncounter.Piece p:s.pieces)if(p.present)key|=(long)(((p.col+1)<<1)|(p.layers-1))<<(4*p.id);return key;}
    static void same(GuardianEncounter.Snapshot a,GuardianEncounter.Snapshot b,String reason){check(a.number==b.number&&a.size==b.size&&a.used==b.used&&a.remainingActions==b.remainingActions&&a.remainingWork==b.remainingWork&&a.solved==b.solved,reason+" counters");
        for(int i=0;i<a.pieces.size();i++){GuardianEncounter.Piece p=a.pieces.get(i),q=b.pieces.get(i);check(p.id==q.id&&p.row==q.row&&p.col==q.col&&p.layers==q.layers&&p.present==q.present&&p.direction==q.direction&&p.cellwise==q.cellwise,reason+" piece");}}
    static void walk(GuardianEncounter e,HashSet<Long> seen){GuardianEncounter.Snapshot before=e.snapshot();long stateKey=key(before);if(!seen.add(stateKey))return;
        states++;boardStates++;check(seen.size()<=500000,"explicit per-encounter memory bound");check(before.remainingWork==work(before),"independent exact remaining work");check(before.remainingActions==e.definition.baseBudget-before.used,"exact budget ledger");check(before.used+before.remainingWork==before.totalWork,"one work per productive action");
        if(boardStates%4096==1||before.solved){String encoded=e.encode();GuardianEncounter restored=GuardianEncounter.restore(encoded);check(restored!=null&&restored.encode().equals(encoded),"canonical typed repeated-ID save");same(before,restored.snapshot(),"save actual state");
            int[] plan=e.optimalPlan();plans++;check(plan.length==work(before),"shortest plan equals independent work lower bound");for(int action:plan)check(restored.move(action).success,"plan action legal");check(restored.isSolved(),"plan actually completes");check(encoded.equals(e.encode()),"planning nonmutating");}
        boolean any=false;
        for(GuardianEncounter.Piece p:before.pieces){boolean legal=allowed(before,p);check(e.canMove(p.id)==legal,"independent coordinate legality");if(!legal)continue;any=true;
            String saved=e.encode();GuardianEncounter.MoveResult move=e.move(p.id);edges++;boardEdges++;check(move.success&&move.remainingWork==before.remainingWork-1&&move.remainingActions==before.remainingActions-1,"successful layer or move consumes exactly one work/action");
            GuardianEncounter.Piece after=e.piece(p.id);if(p.layers>1)check(move.peeled&&!move.removed&&!move.stepped&&after.layers==p.layers-1&&after.row==p.row&&after.col==p.col,"double hit peels in place");
            else if(p.cellwise&&!move.removed)check(move.stepped&&after.row==p.row+p.direction.dr&&after.col==p.col+p.direction.dc,"cellwise advances exactly one cell");
            else check(move.removed&&!after.present,"exit actually removes piece");
            walk(e,seen);check(e.undo()&&e.encode().equals(saved),"Undo refunds and exactly restores parent state");same(before,e.snapshot(),"Undo immutable snapshot");
        }
        check(before.solved||any,"every reachable productive state can progress");
    }
    static void counterContracts(int n){GuardianEncounter e=GuardianEncounter.start(n);String fresh=e.encode();check(!e.undo()&&!e.move(-1).success&&fresh.equals(e.encode()),"empty/missing actions are free");
        GuardianEncounter.Snapshot initial=e.snapshot();boolean thick=false,cellwise=false;for(GuardianEncounter.Piece p:initial.pieces){thick|=p.layers==2;cellwise|=p.cellwise;}
        check(thick,"every encounter contains the advertised double layer");check(cellwise==(n>=5),"cellwise introduced at encounter five");
        int blocked=-1;for(GuardianEncounter.Piece p:initial.pieces)if(p.present&&!allowed(initial,p)){blocked=p.id;break;}
        if(blocked<0){for(GuardianEncounter.Piece p:initial.pieces)if(p.layers>1){e.move(p.id);if(!e.canMove(p.id)){blocked=p.id;break;}e.undo();}}
        if(blocked>=0){int id=blocked;String before=e.encode();GuardianEncounter.Snapshot s=e.snapshot();GuardianEncounter.MoveResult block=e.move(id);check(block.blocked&&!block.success&&key(e.snapshot())==key(s)&&e.snapshot().used==s.used+1,"blocked attempt spends one action without changing pieces");
            while(e.canAct())check(e.move(id).blocked,"repeated blocked probe remains blocked");check(!e.move(id).success&&e.remainingActions()==0,"exhaustion never underflows");
            GuardianEncounter restored=GuardianEncounter.restore(e.encode());check(restored!=null&&restored.remainingActions()==0,"blocked charge survives save");e.restart();check(fresh.equals(e.encode()),"restart is free and resets work/budget");}
        int first=e.optimalPlan()[0];e.move(first);int usedBefore=e.snapshot().used;int[] history=e.history();check(history.length==1&&history[0]==first,"successful history includes layer hits");
        e.history()[0]=999;check(e.history()[0]==first,"history is defensive copy");check(e.undo()&&e.snapshot().used==usedBefore-1&&fresh.equals(e.encode()),"productive Undo refunds actual one action");
        boolean unmodifiable=false;try{e.snapshot().pieces.clear();}catch(UnsupportedOperationException expected){unmodifiable=true;}check(unmodifiable,"snapshot pieces list immutable");
        for(String bad:new String[]{null,"","G2|1|0|","G1|0|0|","G1|101|0|","G1|01|0|","G1|1|-1|","G1|1|0|0","G1|1|999|","G1|1|1|999","G1|1|1|0,0","G1|1|0|\n","x".repeat(1025)})check(GuardianEncounter.restore(bad)==null,"invalid/counterfeit bounded save rejected");
    }
    public static void main(String[]args)throws Exception{Path output=Path.of(args[0]);Files.createDirectories(output);long start=System.nanoTime();
        StringBuilder csv=new StringBuilder("encounter,size,pieces,cellwise,minimum,budget,reachable_productive_states,productive_edges\n");HashSet<String> fingerprints=new HashSet<>();
        for(int n=1;n<=100;n++){GuardianCatalog.Definition d=GuardianCatalog.get(n);check(GuardianCatalog.unlockedCount(n*10)==n&&GuardianCatalog.unlockedCount(n*10-1)==n-1,"ten-clear deterministic gate");
            StringBuilder fingerprint=new StringBuilder().append(d.size);for(GuardianEncounter.Piece p:d.pieces)fingerprint.append('|').append(p.row).append(',').append(p.col).append(',').append(p.direction).append(',').append(p.layers).append(',').append(p.cellwise);check(fingerprints.add(fingerprint.toString()),"each encounter geometry/layer pattern unique");
            counterContracts(n);GuardianEncounter e=GuardianEncounter.start(n);boardStates=boardEdges=0;HashSet<Long> seen=new HashSet<>();walk(e,seen);largestStates=Math.max(largestStates,seen.size());check(e.snapshot().used==0&&e.remainingWork()==d.totalWork,"full traversal restores initial encounter");
            csv.append(n).append(',').append(d.size).append(',').append(d.pieces.size()).append(',').append(d.cellwise).append(',').append(d.minimumActions).append(',').append(d.baseBudget).append(',').append(boardStates).append(',').append(boardEdges).append('\n');if(n%10==0)System.out.println("Guardian"+n+" states="+states+" assertions="+checks);
        }
        check(GuardianCatalog.unlockedCount(-99)==0&&GuardianCatalog.unlockedCount(Integer.MAX_VALUE)==100,"unlock clamp");
        Files.writeString(output.resolve("encounters.csv"),csv);String summary="{\n  \"passed\":true,\n  \"encounters\":100,\n  \"unique_patterns\":"+fingerprints.size()+",\n  \"assertions\":"+checks+",\n  \"reachable_productive_states\":"+states+",\n  \"productive_edges\":"+edges+",\n  \"sampled_complete_plans\":"+plans+",\n  \"largest_state_set\":"+largestStates+",\n  \"elapsed_host_seconds\":"+((System.nanoTime()-start)/1e9)+",\n  \"scope\":\"Independent coordinate and finite-work oracle; all reachable productive states on all 100 encounters; blocked-budget, undo, repeat-ID save and malformed-record contracts. No human playtime or retention estimate.\"\n}\n";Files.writeString(output.resolve("summary.json"),summary);System.out.println(summary);
    }
}
