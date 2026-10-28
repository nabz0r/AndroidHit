import com.androidhit.lumadrift.engine.*;
import java.util.Arrays;

/** Failure, correction, cold-save and family contracts for the actual action ledger. */
public final class DeliberatePlayV114Test {
    private static long checks;
    private static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
    public static void main(String[] args){
        Level old15=StrategyCatalog.get(15); Board board=new Board(old15);
        MoveBudget budget=MoveBudget.startDeliberate(0,old15);
        check(budget.base()==16&&budget.minimumActions()==12,"Reported old L15 is retained exactly");
        for(int i=0;i<50;i++){
            Board.MoveResult result=budget.attempt(board,4);
            check(result!=null&&!result.success&&result.reason==Board.BlockReason.CLOSED_GATE,"A refused exit explains the actual closed gate");
            check(budget.used()==0&&board.moves()==0&&board.remainingCount()==10,"Fifty blocked tries spend nothing and do not create undo history");
        }
        check(!budget.undo(board,0)&&budget.used()==0,"An empty return cannot manufacture a move");
        check(budget.attempt(board,0).success&&budget.used()==1,"A real exit spends exactly one action");
        check(budget.undo(board,0)&&budget.used()==0&&board.moves()==0,"The one correction restores the position and its productive action");
        check(budget.attempt(board,1).success,"Play again after the correction");
        int[] before=board.history();
        check(!budget.undo(board,1)&&Arrays.equals(before,board.history())&&budget.used()==1,"A second return changes neither board nor budget");
        Board coldBoard=new Board(old15);check(coldBoard.restoreHistory(before),"Cold restore preserves typed history");
        MoveBudget cold=MoveBudget.restoreVersion(4,0,old15,budget.used(),0,0,coldBoard.moves(),1);
        check(cold!=null&&cold.undoRemaining(1)==0&&!cold.undo(coldBoard,1),"Cold restore cannot refresh the used return");
        check(MoveBudget.restoreVersion(4,0,old15,5,0,0,1,1)==null,"Policy4 rejects unaccounted blocked charges");
        check(MoveBudget.restoreVersion(4,0,old15,0,0,0,1,1)==null,"Policy4 rejects an undercounted productive action");
        MoveBudget oldBudget=MoveBudget.startReversible(0,old15);
        for(int i=0;i<5;i++)check(oldBudget.consumeAction(),"Reproduce former five paid refusals");
        check(oldBudget.remaining()==11,"Former reported deficit is reproducible");
        MoveBudget migrated=MoveBudget.toDeliberate(0,old15,oldBudget,0,0);
        check(migrated!=null&&migrated.remaining()==16&&migrated.policyVersion()==4,"Upgrade removes refusal charges without replacing old geometry");
        Board pendingBoard=new Board(old15);check(pendingBoard.performAction(0).success,"Prepare an actual old productive history");
        MoveBudget exhaustedOld=MoveBudget.restoreVersion(3,0,old15,16,0,0,1,0);
        check(exhaustedOld!=null&&exhaustedOld.isExhausted(),"An earned old continuation can await settlement at zero");
        MoveBudget pendingUpgrade=MoveBudget.toDeliberate(0,old15,exhaustedOld,1,0);
        check(pendingUpgrade!=null&&pendingUpgrade.remaining()==15&&!pendingUpgrade.canExtend(),"Refunding old rejections removes exhaustion but not the earned receipt");
        check(pendingUpgrade.settleEarnedExtension(1,11)&&pendingUpgrade.extensions()==1&&pendingUpgrade.extraActions()==14,"The previously earned exact benefit settles after the refund");
        check(!pendingUpgrade.settleEarnedExtension(1,11)&&pendingUpgrade.extraActions()==14,"The same receipt ordinal cannot grant a second benefit");
        MoveBudget settledCold=MoveBudget.restoreVersion(4,0,old15,pendingUpgrade.used(),1,14,1,0);
        check(settledCold!=null&&settledCold.remaining()==29&&!settledCold.settleEarnedExtension(1,11),"Cold restoration preserves the settled extension without duplication");
        Level legacy=BalancedCatalog.legacyGet(1);MoveBudget ancient=MoveBudget.startLegacy(0,legacy);
        check(MoveBudget.toDeliberate(0,legacy,ancient,0,0)==null,"Policy1 finishes under its historical allowance rather than silently losing moves");
        check(MoveBudget.restoreVersion(1,0,legacy,0,0,0,0,0).base()==ancient.base(),"Ancient allowance survives cold restoration exactly");
        MoveBudget alreadyUsed=MoveBudget.toDeliberate(0,old15,oldBudget,0,7);
        check(alreadyUsed!=null&&alreadyUsed.undoRemaining(7)==0,"Historical repeated returns do not grant a new correction");
        String oldCode="LD1-L-F-8202A432";
        check(oldCode.equals(ChallengeCode.encodeStrategy(15))&&ChallengeCode.parse(oldCode).resolve()==old15,"Existing L15 code still resolves its historical board");
        int totalActions=0;
        for(int n=1;n<=1000;n++){
            Level level=LearningCatalog.get(n); Board run=new Board(level);MoveBudget allowance=MoveBudget.start(0,level);
            check(allowance.policyVersion()==4&&allowance.base()>level.minimumActions,"Each N board starts with the actual limited action policy");
            for(int action:level.actionSolution){check(allowance.attempt(run,action).success,"Every certified exit/open is committed through the same ledger as UI");totalActions++;}
            check(run.isSolved()&&allowance.used()==level.minimumActions&&JourneyExperience.stars(allowance)==3,"All1000 finish in budget without a booster, correction or extension");
            String code=ChallengeCode.encodeLearning(n);ChallengeCode parsed=ChallengeCode.parse(code);
            check(parsed!=null&&parsed.kind==ChallengeCode.Kind.LEARNING&&parsed.resolve()==level,"Every new N shared code resolves exact content");
            MoveBudget.Snapshot snapshot=allowance.copy().snapshot();
            check(MoveBudget.restoreVersion(snapshot.policyVersion,0,level,snapshot.used,snapshot.extensions,snapshot.extraActions,run.moves(),0)!=null,"Every completed N ledger validates its saved productive count");
        }
        // A legal but wasteful route really fails; restarting supplies a new attempt, not infinite returns.
        Level hard=StrategyCatalog.get(23);board=new Board(hard);budget=MoveBudget.startDeliberate(0,hard);
        int[] inefficient={64,65,66,67};
        for(int action:inefficient)if(board.canPerformAction(action))check(budget.attempt(board,action).success,"Unneeded gate opening still costs one");
        boolean exhausted=false;
        while(!board.isSolved()){
            int[] plan=board.solve();check(plan!=null&&plan.length>0,"Logical completion and remaining allowance are separate");
            if(!budget.canAct()){exhausted=true;break;}
            check(budget.attempt(board,plan[0]).success,"Continue a legal route");
        }
        // Find a deterministic costly gate order if this particular board's four initial opens fit.
        if(!exhausted){
            for(int n=21;n<=1000&&!exhausted;n++){
                board=new Board(LearningCatalog.get(n));budget=MoveBudget.start(0,board.level);
                for(int step=0;step<200&&!board.isSolved();step++){
                    if(!budget.canAct()){exhausted=true;break;}
                    int worst=-1,worstCost=-1;
                    for(int action=0;action<68;action++)if(board.canPerformAction(action)){
                        Board candidate=new Board(board.level);check(candidate.restoreHistory(board.history()),"Branch trial is isolated");candidate.performAction(action);
                        int cost=candidate.minimumActionsToFinish();if(cost>worstCost){worst=action;worstCost=cost;}
                    }
                    check(worst>=0&&budget.attempt(board,worst).success,"Choose a real costly action, not an invalid tap");
                }
            }
        }
        check(exhausted&&!board.isSolved()&&budget.isExhausted(),"Inefficient productive choices can still lose the attempt");
        long mask=board.remainingMask();int gates=board.gateMask();
        check(budget.attempt(board,board.solve()[0])==null&&mask==board.remainingMask()&&gates==board.gateMask(),"Zero actions blocks further productive play atomically");
        check(budget.undo(board,0)&&budget.remaining()==1&&!budget.undo(board,1),"Even at zero there is just one refunded correction");
        board.restart();budget=MoveBudget.startDeliberate(0,board.level);
        check(budget.undoRemaining(0)==1&&budget.used()==0,"A free restart begins a new explicit attempt");
        for(int action:board.level.actionSolution)check(budget.attempt(board,action).success,"A restarted run can complete without purchases");
        check(board.isSolved(),"Free retry completion");
        System.out.println("PASS "+checks+" deliberate-play checks; "+totalActions+" productive actions across1000 boards, blocked L15, one-return persistence, strict failure, retry and codes.");
    }
}
