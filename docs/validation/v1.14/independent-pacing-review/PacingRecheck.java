import com.androidhit.lumadrift.engine.*;
import java.nio.file.*;
import java.util.*;

/** Reviewer-side recomputation. Does not call the curriculum generator or its metrics. */
public final class PacingRecheck {
    static int checks;
    static void require(boolean ok,String why){checks++;if(!ok)throw new AssertionError(why);}
    static boolean ahead(Tile t,int row,int col){
        return t.direction.dr==0 ? row==t.row&&(col-t.col)*t.direction.dc>0
                : col==t.col&&(row-t.row)*t.direction.dr>0;
    }
    static int depth(Level l){
        int[] depths=new int[l.tiles.size()];int maximum=0;
        for(int action:l.actionSolution){if(Board.isGateAction(action))continue;Tile tile=l.tile(action);int value=1;
            for(Tile other:l.tiles)if(other.id!=tile.id&&ahead(tile,other.row,other.col))value=Math.max(value,depths[other.id]+1);
            for(Barrier wall:l.barriers)if(ahead(tile,wall.row,wall.col))value=Math.max(value,depths[wall.keyTileId]+1);
            depths[tile.id]=value;maximum=Math.max(maximum,value);
        }return maximum;
    }
    public static void main(String[]args)throws Exception{
        StringBuilder out=new StringBuilder("number,source,size,pieces,minimum_actions,required_gate_opens,margin,initial_legal_exits,switches,keys,seals,decision_points,forced_optimal,max_one_choice_penalty,dependency_depth,structural_score,difficulty,initial_gates,changed_initial_gates\n");
        for(int n=1;n<=1000;n++){
            Level l=LearningCatalog.get(n);ProgressionInfo info=LearningCatalog.info(n);
            AuditJourneyV114.Costs oracle=new AuditJourneyV114.Costs(l);
            long mask=l.allTilesMask;int gates=l.initialGateMask,decisions=0,forced=0,penalty=0;
            int minimum=oracle.min(mask,gates),initialExits=0,switches=0;Set<Integer>keys=new HashSet<>();
            require(minimum==l.minimumActions,"Exact minimum N"+n);
            for(Tile t:l.tiles){if(oracle.spatial(mask,t)&&(gates&t.direction.bit)!=0)initialExits++;if(t.switchMask!=0)switches++;}
            for(Barrier b:l.barriers)keys.add(b.keyTileId);
            for(int certificate:l.actionSolution){
                int optimum=oracle.min(mask,gates),bestCount=0,worst=optimum;int[] candidates=oracle.actions(mask,gates,1);
                for(int action:candidates){long next=mask;int changed=gates;
                    if(Board.isGateAction(action))changed|=Board.gateDirection(action).bit;
                    else{next&=~(1L<<action);changed^=l.tile(action).switchMask;}
                    int cost=1+oracle.min(next,changed);if(cost==optimum)bestCount++;worst=Math.max(worst,cost);
                }
                if(candidates.length>1&&worst>optimum)decisions++;if(bestCount==1)forced++;penalty=Math.max(penalty,worst-optimum);
                if(Board.isGateAction(certificate))gates|=Board.gateDirection(certificate).bit;
                else{mask&=~(1L<<certificate);gates^=l.tile(certificate).switchMask;}
            }
            require(mask==0,"Certificate complete N"+n);
            int dependency=depth(l);
            int score=(int)Math.round(100.0*decisions/minimum)+3*(minimum-l.tiles.size())+2*keys.size()+2*penalty+dependency;
            int difficulty=n<=30?0:score>=110?2:score>=80?1:0;
            require(decisions==info.decisionPoints&&forced==info.forcedOptimalActions&&penalty==info.maxOneChoicePenalty&&dependency==info.dependencyDepth,"Every recomputed component N"+n);
            require(score==info.structuralScore&&difficulty==info.difficulty.ordinal(),"Recomputed score and class N"+n);
            int source=LearningCatalog.sourceNumber(n),changedInitial=0;
            if(source!=0){Level old=StrategyCatalog.get(source);changedInitial=l.initialGateMask==old.initialGateMask?0:1;require((l.initialGateMask|old.initialGateMask)==l.initialGateMask,"Opening-only change N"+n);}
            out.append(String.format(Locale.ROOT,"%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d%n",n,source,l.size,l.tiles.size(),minimum,minimum-l.tiles.size(),LearningCatalog.allowance(n),initialExits,switches,keys.size(),l.barriers.size(),decisions,forced,penalty,dependency,score,difficulty,l.initialGateMask,changedInitial));
            if(n%100==0)System.out.println("independently recalculated N"+n);
        }
        Files.writeString(Paths.get(args[0]),out);System.out.println("PASS "+checks+" assertions; every stored metric and difficulty agrees with independent finite-state costs.");
    }
}
