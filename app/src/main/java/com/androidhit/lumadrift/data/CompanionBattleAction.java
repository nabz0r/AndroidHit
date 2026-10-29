package com.androidhit.lumadrift.data;

import com.androidhit.lumadrift.engine.Board;
import com.androidhit.lumadrift.engine.MoveBudget;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Prepare a companion command without mutating any supplied board, ledger or state.
 * The caller persists every returned session field together before publishing VFX.
 * A supplied safe plan is checked by linear replay through completion, never solved here.
 */
public final class CompanionBattleAction {
    private CompanionBattleAction() { }

    public static final class Draft {
        public final Board board;
        public final MoveBudget budget;
        public final CompanionBattle.State state;
        public final int undos,hintId,hintDurationMs,visionDurationMs,trailDurationMs,color;
        public final int[] trail,performed,rewound;
        public final CompanionBattle.Effect effect;
        public final boolean combo;
        private Draft(Board board,MoveBudget budget,CompanionBattle.State state,int undos,
                      int hintId,int hintDurationMs,int visionDurationMs,int trailDurationMs,
                      int[] trail,int[] performed,int[] rewound,int color,CompanionBattle.Effect effect,boolean combo) {
            this.board=board;this.budget=budget;this.state=state;this.undos=undos;this.hintId=hintId;
            this.hintDurationMs=hintDurationMs;this.visionDurationMs=visionDurationMs;this.trailDurationMs=trailDurationMs;
            this.trail=trail.clone();this.performed=performed.clone();this.rewound=rewound.clone();
            this.color=color;this.effect=effect;this.combo=combo;
        }
    }

    /**
     * mode is the UI mode: 0 Journey or 5 expedition. Both use the U relay ledger's
     * mode 0. Ownership and the request's current run identity remain caller checks.
     * A refused/no-effect draft returns null and spends nothing in the original session.
     */
    public static Draft prepare(Board board,MoveBudget budget,int mode,int undos,
                                CompanionBattle.State state,int slot,boolean combo,int[] certifiedPlan) {
        if(board==null||budget==null||state==null||undos<0||(mode!=0&&mode!=5)
                ||board.level.progressionVersion!=6||board.isSolved())return null;
        MoveBudget.Snapshot oldBudget=budget.snapshot();
        if(oldBudget.mode!=0||!budget.isRelayPolicy())return null;
        Board.Snapshot original=board.snapshot();int[] history=board.history();
        Board candidate=new Board(board.level);
        if(!candidate.restoreHistory(history)||candidate.remainingMask()!=original.remainingMask
                ||candidate.gateMask()!=original.gateMask||candidate.moves()!=original.moves)return null;
        MoveBudget ledger=MoveBudget.restoreVersion(oldBudget.policyVersion,oldBudget.mode,board.level,
                oldBudget.used,oldBudget.extensions,oldBudget.extraActions,candidate.moves(),undos);
        if(ledger==null||ledger.base()!=oldBudget.base||ledger.minimumActions()!=oldBudget.minimumActions)return null;

        CompanionBattle.Definition definition;
        CompanionBattle.Effect effect;
        CompanionBattle.State successor;
        int amount,color;
        if(combo) {
            if(!state.comboReady())return null;
            definition=null;effect=CompanionBattle.Effect.PULSE;amount=1;
            color=CompanionBattle.find(state.team().get(0)).color;
            successor=state.useCombo();
        } else {
            if(!state.canUse(slot))return null;
            definition=CompanionBattle.find(state.team().get(slot));
            if(definition==null)return null;
            effect=definition.effect;amount=definition.amount;color=definition.color;
            successor=state.use(slot);
        }
        if(successor==null)return null;
        int hint=-1,hintMs=0,visionMs=0,trailMs=0;
        int[] shown=new int[0];List<Integer> performed=new ArrayList<>(),rewound=new ArrayList<>();
        if(effect==CompanionBattle.Effect.REWIND) {
            while(rewound.size()<amount&&candidate.canUndo()) {
                if(undos==Integer.MAX_VALUE)return null;
                int[] prior=candidate.history();int id=prior[prior.length-1];
                if(!ledger.undo(candidate,undos))break;
                undos++;rewound.add(id);
            }
            if(rewound.isEmpty())return null;
        } else if(effect==CompanionBattle.Effect.VISION) {
            if(candidate.legalMoves().isEmpty())return null;
            visionMs=definition.durationMs;
        } else {
            int[] plan=certifiedPlan==null?null:certifiedPlan.clone();
            if(!certifiesCompletion(candidate,plan))return null;
            switch(effect) {
                case GUIDE:
                    hint=plan[0];hintMs=definition.durationMs;break;
                case TRAIL:
                    shown=Arrays.copyOf(plan,Math.min(amount,plan.length));trailMs=definition.durationMs;break;
                case PULSE:
                case FLOW:
                    for(int index=0;index<Math.min(amount,plan.length);index++) {
                        Board.MoveResult result=ledger.attempt(candidate,plan[index]);
                        if(result==null||!result.success||result.tile==null)return null;
                        performed.add(result.tile.id);
                        successor=successor.creditExit(result.tile.id);
                        if(successor==null)return null;
                    }
                    if(performed.isEmpty())return null;
                    break;
                default:return null;
            }
        }
        return new Draft(candidate,ledger,successor,undos,hint,hintMs,visionMs,trailMs,shown,
                integers(performed),integers(rewound),color,effect,combo);
    }

    /** The complete supplied continuation must be legal from this exact position. */
    private static boolean certifiesCompletion(Board from,int[] plan) {
        if(plan==null||plan.length==0||plan.length!=from.remainingCount())return false;
        Board verification=new Board(from.level);
        if(!verification.restoreHistory(from.history()))return false;
        for(int id:plan) {
            if(id<0||id>=64||from.level.tile(id)==null)return false;
            Board.MoveResult result=verification.performAction(id);
            if(!result.success||result.tile==null)return false;
        }
        return verification.isSolved();
    }
    private static int[] integers(List<Integer> values) {
        int[] result=new int[values.size()];for(int index=0;index<result.length;index++)result[index]=values.get(index);return result;
    }
}
