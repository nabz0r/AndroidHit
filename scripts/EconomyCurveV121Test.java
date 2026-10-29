import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.engine.*;
import java.nio.file.*;
import java.util.*;

/** Numerical scenarios plus actual old/new reward and earned-Coin receipt APIs. */
public final class EconomyCurveV121Test {
    private static long checks;
    private static void check(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
    private static GameStore store(RewardsCosmeticsV17Test.Ctx context){return new GameStore(context,null,new RewardsCosmeticsV17Test.Clock("2026-10-30"));}
    private static long incomeThree,incomeOne,maximumBaseUndoCost,hintPackEveryBoard;
    private static double medianUndoShare,maxUndoShare;
    private static String csv;
    private static void allBoards(){
        StringBuilder rows=new StringBuilder("level,reward_1_star,reward_2_stars,reward_3_stars,base_correction_allowance,paid_undos_after_two_free,max_base_undo_cost,corrected_net_income,hint_pack_price,reward_multiplier,boost_multiplier\n");
        List<Double> shares=new ArrayList<>();double previousReward=1,previousBoost=1;int previousPrice=0,previousCapacity=10;
        for(int number=1;number<=1000;number++){
            double reward=EconomyCurve121.campaignMultiplier(number),boost=EconomyCurve121.boostMultiplier(number);
            check(reward>=previousReward&&reward>=1&&reward<=2,"income curve monotone and bounded");
            check(boost>=previousBoost&&boost>=1&&boost<=1.4,"boost curve gentler monotone and bounded");
            previousReward=reward;previousBoost=boost;
            int capacity=EconomyCurve121.energyCapacity(number);
            check(capacity>=previousCapacity&&capacity>=10&&capacity<=16,"energy storage ceiling is monotone10..16");previousCapacity=capacity;
            int r1=EconomyCurve121.campaignReward(number,1),r2=EconomyCurve121.campaignReward(number,2),r3=EconomyCurve121.campaignReward(number,3);
            check(r1<r2&&r2<r3&&EconomyCurve121.campaignReward(number,0)==0,"stars retain ordered total entitlements");
            check(r1+(r2-r1)+(r3-r2)==r3,"upgrades telescope exactly without granting another first-clear base");
            if(number<=10)check(r1==35&&r2==45&&r3==55&&boost==1,"opening ten payouts and base prices unchanged");
            Level level=RelayCatalog.get(number);Board free=new Board(level);MoveBudget freeBudget=MoveBudget.start(0,level);
            ElementalShell121 elements=ElementalShell121.create(level,0,number);
            for(int id:level.solution){while(elements.layers(id)>0)elements.tap(id);check(freeBudget.attempt(free,id).success,"real certificate needs no purchased Undo or power");}
            check(free.isSolved()&&JourneyExperience.stars(freeBudget)==3&&freeBudget.used()==level.minimumActions,"zero-expense certified route gives actual three stars");

            Board corrected=new Board(level);MoveBudget budget=MoveBudget.start(0,level);int spent=0,paid=0;
            for(int undo=0;undo<budget.mistakeAllowance();undo++){
                check(budget.attempt(corrected,level.solution[0]).success&&budget.undo(corrected,undo),"maximum base correction fixture uses real exit and Undo");
                if(undo>=EconomyCurve121.freeUndos())spent+=EconomyCurve121.undoPrice(number,++paid);
            }
            for(int id:level.solution)check(budget.attempt(corrected,id).success,"certificate still completes after full allowed corrections");
            check(corrected.isSolved(),"base allowance scenario actually solvable");
            int correctedIncome=EconomyCurve121.campaignReward(number,JourneyExperience.stars(budget));
            check(spent<=correctedIncome,"within-base correction expense does not exceed that board's corrected first-clear reward");
            double share=spent/(double)correctedIncome;shares.add(share);maxUndoShare=Math.max(maxUndoShare,share);
            int firstUndo=EconomyCurve121.undoPrice(number,1);
            check(firstUndo>=previousPrice&&firstUndo<=40,"first paid Undo monotone and bounded");previousPrice=firstUndo;
            int prior=0;for(int index:new int[]{1,2,3,5,10,100,Integer.MAX_VALUE}){int cost=EconomyCurve121.undoPrice(number,index);check(cost>=prior&&cost<=40,"successive paid Undo price never decreases or exceeds cap");prior=cost;}
            int hint=EconomyCurve121.boostPrice(45,number);
            check(hint>=45&&hint<=63,"actual 45-Coin hint pack rises gently to at most63");
            check(EconomyCurve121.boostPrice(0,number)==0&&EconomyCurve121.boostPrice(Integer.MAX_VALUE,number)==Integer.MAX_VALUE,"zero and extreme base prices cannot wrap");
            incomeThree+=r3;incomeOne+=r1;maximumBaseUndoCost+=spent;hintPackEveryBoard+=hint;
            rows.append(String.format(Locale.ROOT,"%d,%d,%d,%d,%d,%d,%d,%d,%d,%.8f,%.8f\n",number,r1,r2,r3,budget.mistakeAllowance(),paid,spent,correctedIncome-spent,hint,reward,boost));
        }
        Collections.sort(shares);medianUndoShare=(shares.get(499)+shares.get(500))/2;csv=rows.toString();
        check(EconomyCurve121.campaignReward(1000,3)==110&&EconomyCurve121.campaignReward(1000,1)==70,"last board reaches exact declared double payout");
        check(EconomyCurve121.energyCapacity(1)==10&&EconomyCurve121.energyCapacity(10)==10&&EconomyCurve121.energyCapacity(1000)==16,"energy ceiling exact endpoints and free opening");
        check(EconomyCurve121.freeUndos()==2&&EconomyCurve121.premiumEnergyPriceGold()==50&&EconomyCurve121.premiumEnergyUnits()==10,"free allotment and premium quote remain explicit");
    }
    private static void actualRewards(){
        RewardsCosmeticsV17Test.Ctx context=new RewardsCosmeticsV17Test.Ctx();GameStore value=store(context);
        int before=value.coins();value.activateEconomy121();check(value.coins()==before&&value.economy121Enabled(),"activation changes no existing wallet");
        long total=0;
        for(int level=1;level<=1000;level++){
            int paid=value.completeLevel(level,1,true);check(paid==EconomyCurve121.campaignReward(level,1),"actual new first-clear pays exact entitlement");total+=paid;
            paid=value.completeLevel(level,3,true);check(paid==EconomyCurve121.campaignReward(level,3)-EconomyCurve121.campaignReward(level,1),"actual star improvement pays only entitlement difference");total+=paid;
            check(value.completeLevel(level,3,true)==0&&value.completeLevel(level,1,true)==0,"replay and lower grade cannot farm new curve");
        }
        check(total==incomeThree&&value.coins()==before+incomeThree,"all1000 actual store deltas match numerical three-star total");
        GameStore cold=store(context.reboot());check(cold.economy121Enabled()&&cold.coins()==value.coins()&&cold.completeLevel(1000,3,true)==0,"cold replay preserves completed entitlements");
        RewardsCosmeticsV17Test.Ctx legacyContext=new RewardsCosmeticsV17Test.Ctx();GameStore legacy=store(legacyContext);
        for(int level=1;level<=1000;level++)check(legacy.completeLevel(level,3)==55,"unchanged old API retains every legacy payout");
        check(legacy.coins()==180+55000&&!legacy.economy121Enabled(),"old route is not silently repriced or activated");
        for(boolean memory:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx failed=new RewardsCosmeticsV17Test.Ctx();GameStore attempt=store(failed);
            for(int level=1;level<42;level++)attempt.completeLevel(level,3,true);
            int prior=attempt.coins(),first=EconomyCurve121.campaignReward(42,1);failed.save().fail(memory);boolean refused=false;
            try{attempt.completeLevel(42,1,true);}catch(IllegalStateException expected){refused=true;}
            check(refused,"failed new-curve first clear is never acknowledged");
            failed=failed.reboot();GameStore afterLoss=store(failed);check(afterLoss.coins()==prior&&afterLoss.stars(42)==0,"failed entitlement leaves old complete durable state after process loss");
            afterLoss.completeLevel(42,1,true);check(afterLoss.coins()==prior+first&&afterLoss.stars(42)==1,"cold retry grants new fractional entitlement once");
            int beforeUpgrade=afterLoss.coins();failed=failed.reboot();attempt=store(failed);failed.save().fail(memory);refused=false;
            try{attempt.completeLevel(42,3,true);}catch(IllegalStateException expected){refused=true;}
            check(refused,"failed star upgrade is not acknowledged");
            attempt.completeLevel(42,3,true);GameStore saved=store(failed.reboot());
            check(saved.coins()==beforeUpgrade+EconomyCurve121.campaignReward(42,3)-first&&saved.stars(42)==3,"memory-before-disk upgrade retry keeps exact delta with no duplicate base");
        }
    }
    private static void undoReceipts(){
        RewardsCosmeticsV17Test.Ctx context=new RewardsCosmeticsV17Test.Ctx();GameStore value=store(context);int initial=value.coins();
        String first="00000000-0000-0000-0000-000000000121",second="00000000-0000-0000-0000-000000000122";
        int quote=EconomyCurve121.undoPrice(1000,1);
        check(value.reserveUndo121(first,1000,1)&&value.coins()==initial-quote,"real earned-Coin reservation debits exact declared quote");
        check(value.reserveUndo121(first,1000,1)&&value.coins()==initial-quote&&!value.reserveUndo121(second,1000,1),"duplicate reserve is idempotent and concurrent quote refused");
        GameStore cold=store(context.reboot());check(cold.pendingUndo121()!=null&&cold.pendingUndo121().requestId.equals(first),"unconsumed Undo reservation survives cold reconstruction");
        check(cold.cancelUndo121(first)&&cold.coins()==initial&&cold.cancelUndo121(first)&&cold.coins()==initial,"no durable Undo returns reserved coins exactly once");
        check(cold.reserveUndo121(second,1000,1)&&cold.completeUndo121(second)&&cold.coins()==initial-quote,"durable Undo finalizes existing debit");
        check(cold.completeUndo121(second)&&!cold.cancelUndo121(second)&&cold.coins()==initial-quote,"used receipt cannot refund or debit twice");
        for(boolean memory:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx failed=new RewardsCosmeticsV17Test.Ctx();GameStore a=store(failed);failed.save().fail(memory);boolean refused=false;
            try{a.reserveUndo121(first,10,1);}catch(GameStore.LocalSaveException expected){refused=true;}
            check(refused,"failed real Undo reservation surfaces retryable save boundary");
            GameStore reboot=store(failed.reboot());check(reboot.coins()==180&&reboot.pendingUndo121()==null,"failed reservation cannot become durable debit after process loss");
            RewardsCosmeticsV17Test.Ctx completion=new RewardsCosmeticsV17Test.Ctx();GameStore pending=store(completion);
            check(pending.reserveUndo121(first,1000,1),"completion failure fixture reserves exact quote");completion.save().fail(memory);refused=false;
            try{pending.completeUndo121(first);}catch(GameStore.LocalSaveException expected){refused=true;}
            check(refused,"failed completion surfaced instead of acknowledged");
            GameStore restored=store(completion.reboot());check(restored.pendingUndo121()!=null&&restored.coins()==180-quote,"cold failed completion keeps exact reserved debit");
            check(restored.completeUndo121(first)&&restored.completeUndo121(first)&&restored.coins()==180-quote,"retry after durable board finalizes once without another debit");
            RewardsCosmeticsV17Test.Ctx cancellation=new RewardsCosmeticsV17Test.Ctx();GameStore reserved=store(cancellation);
            check(reserved.reserveUndo121(first,1000,1),"refund failure fixture reserves exact quote");cancellation.save().fail(memory);refused=false;
            try{reserved.cancelUndo121(first);}catch(GameStore.LocalSaveException expected){refused=true;}
            check(refused,"failed refund not reported successful");
            GameStore after=store(cancellation.reboot());check(after.pendingUndo121()!=null&&after.coins()==180-quote,"cold failed refund retains original exact reservation");
            check(after.cancelUndo121(first)&&after.cancelUndo121(first)&&after.coins()==180,"refund recovery restores exact coins once");
        }
    }
    private static void invalid(){
        for(int level:new int[]{-1,0,1001,Integer.MAX_VALUE}){boolean refused=false;try{EconomyCurve121.campaignReward(level,3);}catch(IllegalArgumentException expected){refused=true;}check(refused,"out-of-route curve input rejected");}
        for(int stars:new int[]{-1,4}){boolean refused=false;try{EconomyCurve121.campaignReward(1,stars);}catch(IllegalArgumentException expected){refused=true;}check(refused,"invalid grade rejected");}
        boolean refused=false;try{EconomyCurve121.undoPrice(1,0);}catch(IllegalArgumentException expected){refused=true;}check(refused,"paid ordinal starts at one");
    }
    public static void main(String[] args)throws Exception{
        allBoards();actualRewards();undoReceipts();invalid();
        String report="{\n  \"passed\":true,\n  \"checks\":"+checks+",\n  \"boards\":1000,\n  \"three_star_zero_spend_route_income\":"+incomeThree+",\n  \"one_star_route_income\":"+incomeOne+",\n  \"max_base_allowance_route_undo_cost\":"+maximumBaseUndoCost+",\n  \"median_base_undo_share_of_corrected_reward\":"+String.format(Locale.ROOT,"%.6f",medianUndoShare)+",\n  \"maximum_base_undo_share_of_corrected_reward\":"+String.format(Locale.ROOT,"%.6f",maxUndoShare)+",\n  \"optional_one_hint_pack_each_board_cost\":"+hintPackEveryBoard+",\n  \"scope\":\"Actual U1-U1000 certified zero-Coin-expense route and full base correction allowance, monotone versioned logarithmic reward/price math, actual new/legacy GameStore entitlement deltas, real earned-Coin reservation/cancel/complete and cold/failure doubles. No measured player median, real purchase, retention, monetary exchange rate or optimal-balance claim. Repeated attempts or purchases can cost more than first-clear income.\"\n}\n";
        if(args.length>0){Path out=Path.of(args[0]);Files.createDirectories(out.getParent());Files.writeString(out,report);Files.writeString(out.resolveSibling("economy-curve-1000.csv"),csv);}System.out.print(report);
    }
}
