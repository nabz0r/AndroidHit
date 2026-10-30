package cognitive_lab;

import com.androidhit.lumadrift.engine.*;
import com.androidhit.lumadrift.data.EconomyCurve121;
import java.util.*;

/** Real mechanics and native rendered assistance. Hidden references never reach the policy. */
final class Environment {
    static final class Energy {
        int count=10,cap=10,spent,refilled;double anchor;
        void refresh(double now){if(count<cap){int ticks=(int)((now-anchor)/600);if(ticks>0){int added=Math.min(cap-count,ticks);count+=added;refilled+=added;anchor=count==cap?now:anchor+ticks*600;}}}
        void capacity(int requested,double now){refresh(now);if(requested>cap){if(count==cap)anchor=now;cap=requested;}}
        boolean reserve(int level,double now){capacity(EconomyCurve121.energyCapacity(level),now);if(level<=10)return true;if(count==0)return false;if(count==cap)anchor=now;count--;spent++;return true;}
    }
    final Board board;final MoveBudget budget;final ElementalShell121 shells;final ExitChain121 chain=new ExitChain121();
    final CognitiveAgent.Piece[] pieces;final CognitiveAgent.Wall[] walls;final int level,ordinal,startDay;
    int undos,actions,blocked,shellTaps,focusUses,focusRequests,exits,hintsUsed,paidUndoCoins,coachCueObservations,deadEndWarningObservations,focusCueObservations;double seconds;int visibleFocus=-1;double focusExpires;
    Environment(int level,int ordinal,int day){this.level=level;this.ordinal=ordinal;startDay=day;Level source=RelayCatalog.get(level);board=new Board(source);budget=MoveBudget.start(0,source);shells=ElementalShell121.create(source,0,level);
        pieces=new CognitiveAgent.Piece[source.tiles.size()];for(Tile p:source.tiles)pieces[p.id]=new CognitiveAgent.Piece(p.id,p.row,p.col,p.direction.ordinal(),p.switchMask);
        walls=new CognitiveAgent.Wall[source.barriers.size()];for(int i=0;i<walls.length;i++){Barrier w=source.barriers.get(i);walls[i]=new CognitiveAgent.Wall(w.row,w.col,w.keyTileId);}}
    CognitiveAgent.Observation observation(double modelClock,int coins){
        // The live native UI displays an exact dead-end warning. It is a declared
        // environment cue, never a planner evaluation score or hidden safe suffix.
        Board.Guidance guidance=board.guidance(budget.remaining());boolean dead=guidance.status==Board.PositionStatus.DEAD_END;
        int cue=-1;String source="none";if(level==1&&board.moves()<2){cue=board.hint();source="native_u1_coach";}
        else if(visibleFocus>=0&&modelClock<=focusExpires&&board.isPresent(visibleFocus)){cue=visibleFocus;source="native_earned_focus";}
        if(dead)deadEndWarningObservations++;if(source.equals("native_u1_coach"))coachCueObservations++;if(source.equals("native_earned_focus"))focusCueObservations++;
        int[] layers=new int[pieces.length];for(int i=0;i<layers.length;i++)layers[i]=shells.layers(i);
        int cost=undos<2?0:EconomyCurve121.undoPrice(level,undos-1);
        return new CognitiveAgent.Observation(pieces,walls,board.remainingMask(),board.gateMask(),layers,budget.remaining(),cue,chain.ready(),board.canUndo()&&coins>=cost,dead,cost,source);
    }
    static final class Result {final String kind,reason;final int coinCost;final double seconds;Result(String kind,String reason,int cost,double seconds){this.kind=kind;this.reason=reason;coinCost=cost;this.seconds=seconds;}}
    Result apply(CognitiveAgent.Decision decision,CognitiveAgent.Observation before,double clock,double duration){
        actions++;String kind="",reason="NONE";int cost=0;
        if(decision.action==CognitiveAgent.STOP)kind="give_up";
        else if(decision.action==CognitiveAgent.FOCUS){focusRequests++;Board.Guidance nativeHelp=board.guidance(budget.remaining());
            if(chain.ready()&&nativeHelp.canUseHint()&&chain.use()){visibleFocus=nativeHelp.hintTileId;focusExpires=clock+duration+8;focusUses++;hintsUsed++;kind="focus_visible";}else kind="focus_unavailable";}
        else if(decision.action==CognitiveAgent.UNDO){if(before.canUndo&&budget.undo(board,undos)){cost=before.undoCost;paidUndoCoins+=cost;undos++;chain.correction();visibleFocus=-1;kind="undo";}else kind="undo_unavailable";}
        else if(shells.layers(decision.action)>0){shells.tap(decision.action);shellTaps++;kind="shell";}
        else {Board.MoveResult result=budget.attempt(board,decision.action);if(result==null)kind="budget_exhausted";
            else if(!result.success){blocked++;chain.correction();kind="blocked";reason=result.reason.name();}
            else{exits++;shells.clear(decision.action);chain.exit(decision.action);visibleFocus=-1;kind="exit";}}
        seconds+=duration;
        if(budget.used()!=board.moves()+undos)throw new AssertionError("Policy6 conservation violated");
        return new Result(kind,reason,cost,duration);
    }
    int stars(){if(!board.isSolved())return 0;if(budget.used()<=budget.minimumActions())return 3;return budget.used()<=budget.minimumActions()+Math.max(1,budget.mistakeAllowance()/2)?2:1;}
    Map<String,Object> row(String profile,int repetition,String scenario,int day,String outcome){return Json.obj("profileId",profile,"repetition",repetition,"scenario",scenario,"attempt",ordinal,"level",level,"startDay",startDay,"endDay",day,"outcome",outcome,"actions",actions,"exits",exits,"blocked",blocked,"undos",undos,"shellTaps",shellTaps,"focusRequests",focusRequests,"focusUses",focusUses,"paidUndoCoins",paidUndoCoins,"budgetUsed",budget.used(),"budgetRemaining",budget.remaining(),"remaining",board.remainingCount(),"modeledSeconds",seconds,"stars",stars(),"coachCueObservations",coachCueObservations,"deadEndWarningObservations",deadEndWarningObservations,"focusCueObservations",focusCueObservations);}
}
