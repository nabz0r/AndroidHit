import com.androidhit.lumadrift.engine.*;
import com.androidhit.lumadrift.ui.*;
import com.androidhit.lumadrift.data.*;
import java.nio.file.*;
import java.util.*;

/** Real frozen U boards, exact current/legacy ledgers, result codec and actual store receipts. */
public final class FocusedJourneyV120Test {
    static long checks;static int boards,solvedRuns,exhausted;
    static void check(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
    static MoveBudget fresh(Level level){MoveBudget value=MoveBudget.start(0,level);check(value.policyVersion()==6&&value.managesUndo()&&!value.refundsUndo(),"new focused U policy");return value;}
    static void coherent(Level level,Board board,MoveBudget budget,int undos){
        MoveBudget restored=MoveBudget.restoreVersion(6,0,level,budget.used(),budget.extensions(),budget.extraActions(),board.moves(),undos);
        check(restored!=null&&restored.used()==budget.used()&&restored.remaining()==budget.remaining(),"actual history+corrections restores exactly");
    }
    static void campaign(){
        Set<Integer> limits=new HashSet<>();
        for(int n=1;n<=1000;n++){
            Level level=RelayCatalog.get(n);boards++;MoveBudget shape=fresh(level);limits.add(shape.base());
            check(shape.minimumActions()==level.solution.length&&shape.minimumActions()==level.tiles.size(),"certified exact necessary exits");
            for(int returns=0;returns<=shape.mistakeAllowance()+1;returns++){
                Board board=new Board(level);MoveBudget budget=fresh(level);int undos=0;
                int used=budget.used();check(!budget.attempt(board,Board.gateAction(Direction.UP)).success&&budget.used()==used,"rejected manual gate stays free");
                for(int k=0;k<returns;k++){
                    check(budget.attempt(board,level.solution[0]).success,"real correction exit");int spent=budget.used();
                    check(budget.undo(board,undos++),"real position restoration");
                    check(budget.used()==spent&&board.moves()==0,"Undo adds no action and refunds no spent exit");coherent(level,board,budget,undos);
                }
                for(int id:level.solution){if(!budget.canAct())break;check(budget.attempt(board,id).success,"real certified exit");coherent(level,board,budget,undos);}
                if(returns<=shape.mistakeAllowance()){
                    check(board.isSolved(),"all1000 remain solvable within original allowance after declared corrections");solvedRuns++;
                    int expected=returns==0?3:returns<=Math.max(1,shape.mistakeAllowance()/2)?2:1;
                    check(JourneyExperience.stars(budget)==expected,"honest three/two/one star correction boundary");
                }else{
                    check(!board.isSolved()&&budget.isExhausted()&&budget.supportsEarnedContinuation(),"real finite limit can exhaust");exhausted++;
                    int need=board.remainingCount(),amount=budget.extensionActions(need),ordinal=budget.extensions()+1;
                    check(amount>=need&&amount>=5&&budget.settleEarnedExtension(ordinal,need),"earned extension covers actual certified remainder");
                    int total=budget.total();check(!budget.settleEarnedExtension(ordinal,need)&&budget.total()==total,"duplicate reward ordinal grants nothing");
                    for(int k=board.moves();k<level.solution.length;k++)check(budget.attempt(board,level.solution[k]).success,"extended real suffix finishes");
                    check(board.isSolved()&&JourneyExperience.stars(budget)==1,"continuation is one star without altering geometry");coherent(level,board,budget,undos);
                }
            }
            MoveBudget historical=MoveBudget.restoreVersion(5,0,level,0,0,0,0,0);Board old=new Board(level);
            check(historical!=null&&historical.refundsUndo()&&!historical.supportsEarnedContinuation(),"policy5 retains published semantics");
            check(historical.attempt(old,level.solution[0]).success&&historical.undo(old,0)&&historical.used()==0,"historical Undo still refunds");
            check(MoveBudget.restoreVersion(5,0,level,0,0,0,0,1)!=null,"historical snapshot remains readable");
            check(MoveBudget.restoreVersion(6,0,level,0,0,0,0,1)==null,"policy6 refuses smuggled refunded correction");
        }
        check(limits.size()>3,"the1000 real board budgets are varied rather than fixed13");
    }
    static VictoryReceipt pending(int mode){return VictoryReceipt.pending("01234567-89ab-cdef-0123-456789abcdef",mode,1,0,mode==5?"01234567-89ab-cdef-0123-456789abcdef":mode==3?"remix-v1":mode==1||mode==2?"2026-10-29":"",3,180,0,43210,mode==4?0:4,mode==4?0:8,0,"nova");}
    static void receipts(){
        for(int mode=0;mode<=5;mode++){
            VictoryReceipt p=pending(mode);check(!p.ready&&VictoryReceipt.decode(p.encode()).encode().equals(p.encode()),"strict pending roundtrip allmodes including unlimitedfriend");
            VictoryReceipt ready=p.settle(235,35);check(ready.ready&&ready.coins==55&&ready.xp==35,"exact durable balance delta");
            check(VictoryReceipt.decode(ready.encode()).encode().equals(ready.encode())&&ready.settle(999,999)==ready,"ready result cannot be re-incremented");
            check(ready.matches(p.runId,mode,1,0,p.period)&&!ready.matches("new-run",mode,1,0,p.period),"receipt bound to real run identity");
        }
        String good=pending(0).encode();String[] parts=good.split("\\|",-1);
        for(int index:new int[]{3,4,5,7,8,9,10,11,12,13,14,15})for(String bad:new String[]{"-1","9223372036854775808","+1","01","NaN"}){
            String[] copy=parts.clone();copy[index]=bad;check(VictoryReceipt.decode(String.join("|",copy))==null,"corrupt/noncanonical numeric receipt rejected");
        }
        for(String bad:new String[]{null,"",good+"|extra",good.replace("v120_1","future"),good.replace("nova","../nova"),good.replace("|P|","|x|")})check(VictoryReceipt.decode(bad)==null,"malformed record never partially loads");
        boolean refused=false;try{pending(0).settle(179,0);}catch(IllegalStateException expected){refused=true;}check(refused,"backward balances do not invent reward");
    }
    static void durableAwards(){
        for(int interrupted=0;interrupted<=3;interrupted++){
            RewardsCosmeticsV17Test.Ctx ctx=new RewardsCosmeticsV17Test.Ctx();RewardsCosmeticsV17Test.Clock clock=new RewardsCosmeticsV17Test.Clock("2026-10-29");
            GameStore store=new GameStore(ctx,null,clock);MetaProgress meta=new MetaProgress(ctx,clock);VictoryReceipt initial=pending(0);
            ctx.getSharedPreferences("luma_victory_v120",0).edit().putString("receipt",initial.encode()).commit();
            if(interrupted>=1)store.completeLevel(1,3);
            if(interrupted>=2)store.claimAvatarVictoryBonus(0,1,"");
            if(interrupted>=3)meta.recordWin(0,1,3,"");
            ctx=ctx.reboot();store=new GameStore(ctx,null,clock);meta=new MetaProgress(ctx,clock);
            VictoryReceipt restored=VictoryReceipt.decode(ctx.getSharedPreferences("luma_victory_v120",0).getString("receipt",""));
            store.completeLevel(1,3);store.claimAvatarVictoryBonus(0,1,"");meta.recordWin(0,1,3,"");VictoryReceipt ready=restored.settle(store.coins(),meta.xp());
            check(ready.coins==55&&ready.xp==35&&store.coins()==235&&meta.xp()==35,"actual stores recover exact reward after each interrupted award stage");
            for(int retry=0;retry<20;retry++){store.completeLevel(1,3);store.claimAvatarVictoryBonus(0,1,"");meta.recordWin(0,1,3,"");check(ready.settle(store.coins(),meta.xp())==ready&&store.coins()==235&&meta.xp()==35,"redraw/retry does not regrant");}
        }
    }
    static void layout(){
        for(float height:new float[]{480,510,600,720,900})for(float font:new float[]{12,16,18,21})for(float touch:new float[]{48,52,60})for(boolean gifts:new boolean[]{false,true}){
            VictoryLayout a=new VictoryLayout(height,font,touch,gifts);
            check(!a.next.intersects(a.map)&&!a.next.intersects(a.shop)&&!a.map.intersects(a.shop),"victory actual controls disjoint");
            check(a.map.bottom<=height&&a.next.top>0&&a.map.height()>=touch&&a.next.height()>=touch,"visible qualified compact controls retain targetsize");
            if(gifts)check(a.gifts.bottom<a.next.top&&a.rewardY<a.gifts.top,"real gifts row has separate space");
            check(a.starsY<a.metricsY&&a.metricsY<a.rewardY,"title stars counters rewards ordered");
        }
        check(VictoryLayout.displayedStars(3,179,false)==0&&VictoryLayout.displayedStars(3,180,false)==1&&VictoryLayout.displayedStars(3,430,false)==2&&VictoryLayout.displayedStars(3,680,false)==3,"earned stars sequence boundaries");
        check(VictoryLayout.displayedStars(2,0,true)==2&&VictoryLayout.rewardProgress(0,true)==1,"reducedmotion immediately presents exact result");
        check(VictoryLayout.rewardProgress(749,false)==0&&VictoryLayout.rewardProgress(1400,false)==1,"reward count animation is bounded");
        check(VictoryLayout.clock(43210).equals("00:43")&&VictoryLayout.clock(3600000).equals("60:00"),"informational clock has no deadline");
        for(String lang:new String[]{"en","fr","de","es","pt","it"})for(String key:new String[]{"moves","time","undo_rule","nice","saved","shop","map","clock_note"})check(!V120ResultStrings.get(lang,key).equals(key),"localized visible result copy");
    }
    public static void main(String[] args)throws Exception{
        campaign();receipts();durableAwards();layout();String report="{\n  \"passed\":true,\n  \"checks\":"+checks+",\n  \"frozen_u_boards\":"+boards+",\n  \"solved_runs_within_allowance\":"+solvedRuns+",\n  \"actually_exhausted_runs\":"+exhausted+",\n  \"scope\":\"Actual frozen U1-U1000, policy6 and preserved5, finite correction budgets, three grades, idempotent extension ordinals, strict display receipts, actual GameStore/MetaProgress cold interruption recovery and pure compact layout. Not Android rendering, ads, real payments, sound or human acceptance.\"\n}\n";
        if(args.length>0){Path out=Path.of(args[0]);Files.createDirectories(out.getParent());Files.writeString(out,report);}System.out.print(report);
    }
}
