package com.androidhit.lumadrift.data;

import com.androidhit.lumadrift.engine.Board;
import com.androidhit.lumadrift.engine.MoveBudget;
import java.util.ArrayList;
import java.util.List;

/** Native draft using the ordinary U engine and policy6, with a separate four-board resource ledger. */
public final class TacticalChapterAction {
    private TacticalChapterAction(){}
    public static final class Draft {
        public final Board board;
        public final MoveBudget budget;
        public final TacticalChapterRun run;
        public final int undos,hintId,hintDurationMs,color;
        public final int[] performed,rewound;
        public final CompanionBattle.Effect effect;
        public final boolean combo;
        private Draft(Board board,MoveBudget budget,TacticalChapterRun run,int undos,int hintId,int hintDurationMs,int color,
                      List<Integer> performed,List<Integer> rewound,CompanionBattle.Effect effect,boolean combo){
            this.board=board;this.budget=budget;this.run=run;this.undos=undos;this.hintId=hintId;this.hintDurationMs=hintDurationMs;this.color=color;
            this.performed=integers(performed);this.rewound=integers(rewound);this.effect=effect;this.combo=combo;
        }
    }
    /** No search here. A supplied full continuation is checked only by linear mechanical replay. */
    public static Draft prepare(Board board,MoveBudget budget,int undos,TacticalChapterRun run,int slot,boolean combo,int[] certifiedPlan,String expectedRunEncoded){
        if(board==null||budget==null||run==null||expectedRunEncoded==null||!run.encode().equals(expectedRunEncoded)||undos<0
                ||run.currentLayout()==null||!run.currentLayout().matches(board.level)||board.isSolved())return null;
        MoveBudget.Snapshot old=budget.snapshot();if(old.mode!=0||old.policyVersion!=MoveBudget.FOCUSED_RELAY_POLICY_VERSION)return null;
        Board candidate=new Board(board.level);if(!candidate.restoreHistory(board.history())||candidate.remainingMask()!=board.remainingMask()||candidate.gateMask()!=board.gateMask())return null;
        MoveBudget ledger=MoveBudget.restoreVersion(old.policyVersion,old.mode,board.level,old.used,old.extensions,old.extraActions,candidate.moves(),undos);
        if(ledger==null||ledger.base()!=old.base||ledger.minimumActions()!=old.minimumActions)return null;
        CompanionBattle.Definition definition=combo?TacticalChapterCatalog.talent(0):TacticalChapterCatalog.talent(slot);
        if(definition==null||(combo?!run.comboReady():!run.canUse(slot)))return null;
        CompanionBattle.Effect effect=combo?CompanionBattle.Effect.PULSE:definition.effect;
        int amount=combo?1:definition.amount,hint=-1,hintMs=0;
        List<Integer> performed=new ArrayList<>(),rewound=new ArrayList<>();
        if(effect==CompanionBattle.Effect.REWIND){
            while(rewound.size()<amount&&candidate.canUndo()){
                if(undos==Integer.MAX_VALUE)return null;int[] prior=candidate.history();int id=prior[prior.length-1];
                if(!ledger.undo(candidate,undos))break;undos++;rewound.add(id);
            }
            if(rewound.isEmpty())return null;
        }else{
            int[] plan=certifiedPlan==null?null:certifiedPlan.clone();if(!certifies(candidate,plan))return null;
            if(effect==CompanionBattle.Effect.GUIDE){hint=plan[0];hintMs=definition.durationMs;}
            else if(effect==CompanionBattle.Effect.FLOW||effect==CompanionBattle.Effect.PULSE){
                for(int i=0;i<Math.min(amount,plan.length);i++){Board.MoveResult result=ledger.attempt(candidate,plan[i]);if(result==null||!result.success||result.tile==null)return null;performed.add(result.tile.id);}
                if(performed.isEmpty())return null;
            }else return null;
        }
        TacticalChapterRun successor=combo?run.useCombo(run.currentBoardScope(),expectedRunEncoded):run.useTalent(slot,run.currentBoardScope(),expectedRunEncoded);
        if(successor==null)return null;
        return new Draft(candidate,ledger,successor,undos,hint,hintMs,definition.color,performed,rewound,effect,combo);
    }
    private static boolean certifies(Board board,int[] plan){
        if(plan==null||plan.length==0||plan.length!=board.remainingCount())return false;
        Board proof=new Board(board.level);if(!proof.restoreHistory(board.history()))return false;
        for(int id:plan){if(id<0||id>=64||board.level.tile(id)==null||!proof.performAction(id).success)return false;}
        return proof.isSolved();
    }
    private static int[] integers(List<Integer> values){int[] result=new int[values.size()];for(int i=0;i<result.length;i++)result[i]=values.get(i);return result;}
}
