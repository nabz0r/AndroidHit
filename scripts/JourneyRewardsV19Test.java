import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.engine.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.nio.file.*;

/** Actual store and budget code with Android memory-before-disk preference doubles. */
public final class JourneyRewardsV19Test {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static void saveFailure(Runnable action,String message){boolean rejected=false;try{action.run();}catch(IllegalStateException expected){rejected=true;}check(rejected,message);}
    private static RewardsCosmeticsV17Test.Ctx context(){return new RewardsCosmeticsV17Test.Ctx();}
    private static GameStore store(RewardsCosmeticsV17Test.Ctx c){return new GameStore(c);}
    private static GameStore progressed(RewardsCosmeticsV17Test.Ctx c,int clears){GameStore s=store(c);for(int i=1;i<=clears;i++)s.completeLevel(i,3);return s;}
    private static String expectedGift(int milestone){List<String> ids=new ArrayList<>();for(Catalog.BoosterItem item:Catalog.BOOSTERS)if(item.unlockLevel<=milestone+1)ids.add(item.id);return ids.get((milestone/5-1)%ids.size());}
    private static int totalStock(GameStore s){int sum=0;for(Catalog.BoosterItem item:Catalog.BOOSTERS)sum+=s.boosterStock(item.id);return sum;}

    private static void explicitTrials(){
        RewardsCosmeticsV17Test.Ctx c=context();GameStore s=store(c);Map<String,?> initial=c.save().getAll();
        check(s.powerTrialAvailable("hint")&&!s.powerTrialClaimed("hint"),"first hint sample is explicit and pending");
        check(s.boosterStock("hint")==3&&c.save().getAll().equals(initial),"reading availability never creates stock or receipts");
        for(String invalid:new String[]{null,"","missing","../hint","hint:second"}){
            check(!s.powerTrialAvailable(invalid)&&!s.powerTrialClaimed(invalid),"invalid trial reads rejected");
            check(s.claimPowerTrial(invalid)==GameStore.PowerTrialStatus.INVALID_POWER,"invalid trial claim rejected");
        }
        for(Catalog.BoosterItem item:Catalog.BOOSTERS)if(item.unlockLevel>1){
            int count=s.boosterStock(item.id);check(s.claimPowerTrial(item.id)==GameStore.PowerTrialStatus.LOCKED,"locked power cannot grant a trial");
            check(!s.powerTrialAvailable(item.id)&&s.boosterStock(item.id)==count&&!s.powerTrialClaimed(item.id),"locked claim changes no stock or receipt");
        }
        int granted=0;
        for(int level=1;level<=75;level++){
            for(Catalog.BoosterItem item:Catalog.BOOSTERS){
                boolean pending=level>=item.unlockLevel&&!s.powerTrialClaimed(item.id);
                check(s.powerTrialAvailable(item.id)==pending,"exact pending trial boundary "+item.id+" at "+level);
                if(pending){int before=s.boosterStock(item.id),coins=s.coins();
                    check(s.claimPowerTrial(item.id)==GameStore.PowerTrialStatus.GRANTED,"one explicit sample at unlock");granted++;
                    check(s.boosterStock(item.id)==before+1&&s.coins()==coins,"sample changes only exact stock");
                    check(!s.powerTrialAvailable(item.id)&&s.powerTrialClaimed(item.id),"receipt removes claim availability");
                    check(store(c).claimPowerTrial(item.id)==GameStore.PowerTrialStatus.ALREADY_CLAIMED&&s.boosterStock(item.id)==before+1,"second handle cannot duplicate");
                }
            }
            s.completeLevel(level,3);
        }
        check(granted==6,"all six catalog powers have exactly one sample");
        for(Catalog.BoosterItem item:Catalog.BOOSTERS)check(store(c.reboot()).powerTrialClaimed(item.id),"sample receipt survives cold recreation");
    }

    private static void trialCapacityAndFailure(){
        for(Catalog.BoosterItem item:Catalog.BOOSTERS){
            RewardsCosmeticsV17Test.Ctx c=context();GameStore s=progressed(c,75);c.save().edit().putInt("booster_"+item.id,999).commit();
            check(s.powerTrialAvailable(item.id)&&s.claimPowerTrial(item.id)==GameStore.PowerTrialStatus.STORAGE_FULL,"full physical stock remains retryable");
            check(!s.powerTrialClaimed(item.id)&&s.boosterStock(item.id)==999,"full stock does not clip or consume receipt");
            c.save().edit().putInt("booster_"+item.id,998).commit();
            check(s.claimPowerTrial(item.id)==GameStore.PowerTrialStatus.GRANTED&&s.boosterStock(item.id)==999,"same sample can be claimed after making room");
        }
        RewardsCosmeticsV17Test.Ctx held=context();GameStore heldStore=store(held);held.save().edit().putInt("booster_hint",999).commit();
        check(heldStore.reserveBoosterUse("hint","v19:physical-cap"),"reserve physical unit fixture");
        check(heldStore.boosterStock("hint")==998&&heldStore.claimPowerTrial("hint")==GameStore.PowerTrialStatus.STORAGE_FULL,"escrow counts towards physical stock cap");
        for(boolean memory:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx c=context();GameStore s=store(c),second=store(c);c.save().fail(memory);
            saveFailure(()->s.claimPowerTrial("hint"),"failed grant is never reported as durable");
            check(c.save().disk.get("booster_hint").equals(3)&&!c.save().disk.containsKey("power_trial_v19_hint"),"disk contains neither half of failed grant");
            // Isolate the claim's flush from constructor recovery of the same pending write.
            c.save().fail(memory);saveFailure(()->second.claimPowerTrial("hint"),"cross-handle retry waits for durability");
            check(store(c).claimPowerTrial("hint")==GameStore.PowerTrialStatus.ALREADY_CLAIMED&&s.boosterStock("hint")==4,"retry flushes original sample once");
            GameStore cold=store(c.reboot());check(cold.powerTrialClaimed("hint")&&cold.boosterStock("hint")==4,"receipt and exact stock reload together");
            check(s.useBooster("hint")&&s.boosterStock("hint")==3&&s.claimPowerTrial("hint")==GameStore.PowerTrialStatus.ALREADY_CLAIMED,"later use cannot be overwritten by duplicate retry");
            RewardsCosmeticsV17Test.Ctx killed=context();GameStore before=store(killed);killed.save().fail(memory);saveFailure(()->before.claimPowerTrial("hint"),"cold interrupted sample");
            GameStore restarted=store(killed.reboot());check(restarted.claimPowerTrial("hint")==GameStore.PowerTrialStatus.GRANTED&&restarted.boosterStock("hint")==4,"kill before durability leaves grant retryable from disk");
        }
    }

    private static void trialConcurrency()throws Exception{
        RewardsCosmeticsV17Test.Ctx c=context();progressed(c,75);int before=totalStock(store(c));
        AtomicInteger granted=new AtomicInteger(),errors=new AtomicInteger();CountDownLatch ready=new CountDownLatch(1);List<Thread> workers=new ArrayList<>();
        for(int n=0;n<36;n++){String id=Catalog.BOOSTERS[n%6].id;Thread thread=new Thread(()->{try{ready.await();if(store(c).claimPowerTrial(id)==GameStore.PowerTrialStatus.GRANTED)granted.incrementAndGet();}catch(Exception error){errors.incrementAndGet();}});workers.add(thread);thread.start();}
        ready.countDown();for(Thread thread:workers)thread.join();
        check(errors.get()==0&&granted.get()==6&&totalStock(store(c))==before+6,"36 duplicate/mixed callbacks grant exactly six samples");
    }

    private static void stableGifts(){
        RewardsCosmeticsV17Test.Ctx now=context(),later=context();GameStore immediate=store(now),deferred=progressed(later,1000);
        Map<String,?> previewState=now.save().getAll();
        check(immediate.nextMilestoneLootId().equals("hint")&&immediate.claimMilestoneLoot().isEmpty(),"future gift preview is available before earned");
        check(now.save().getAll().equals(previewState),"preview creates no receipt or stock");
        for(int clear=1;clear<=1000;clear++){
            immediate.completeLevel(clear,3);
            if(clear%5!=0)continue;
            String expected=expectedGift(clear);int current=totalStock(immediate),late=totalStock(deferred);
            check(immediate.nextMilestoneLootId().equals(expected)&&deferred.nextMilestoneLootId().equals(expected),"same preview independent of claim timing at "+clear);
            check(immediate.claimMilestoneLoot().equals(expected)&&deferred.claimMilestoneLoot().equals(expected),"claim matches each exact stable preview at "+clear);
            check(totalStock(immediate)==current+1&&totalStock(deferred)==late+1,"one physical unit per milestone");
            check(immediate.claimMilestoneLoot().isEmpty(),"same milestone cannot be reclaimed before next threshold");
            check(now.save().contains("power_loot_v19_"+clear)&&!now.save().contains("power_loot_v1_"+clear),"new gift receipt uses v19 namespace");
        }
        check(immediate.nextLootMilestone()==0&&immediate.nextMilestoneLootId().isEmpty()&&deferred.nextLootMilestone()==0,"all 200 gifts exhaust cleanly");
        for(Catalog.BoosterItem item:Catalog.BOOSTERS)check(immediate.boosterStock(item.id)==deferred.boosterStock(item.id),"immediate/deferred reward totals identical");
        RewardsCosmeticsV17Test.Ctx legacy=context();GameStore old=progressed(legacy,15);
        legacy.save().edit().putString("power_loot_v1_5","trail").putInt("booster_trail",1).commit();
        check(old.nextLootMilestone()==10&&old.nextMilestoneLootId().equals(expectedGift(10)),"old already-earned receipt is retained even with historical item");
        check(old.claimMilestoneLoot().equals(expectedGift(10))&&old.boosterStock("trail")==1,"legacy receipt does not duplicate or rewrite stock");
        check(legacy.save().getString("power_loot_v1_5","").equals("trail")&&!legacy.save().contains("power_loot_v19_5"),"old receipt bytes unchanged");
        for(boolean memory:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx c=context();GameStore s=progressed(c,75);String preview=s.nextMilestoneLootId();int before=s.boosterStock(preview);c.save().fail(memory);
            saveFailure(s::claimMilestoneLoot,"gift durability failure is visible");
            check(store(c).nextMilestoneLootId().equals(preview)&&store(c).nextLootMilestone()==5,"in-process retry preserves exact gift and milestone");
            c.save().fail(memory);saveFailure(()->store(c).claimMilestoneLoot(),"gift retry must persist first");
            check(store(c).claimMilestoneLoot().equals(preview)&&s.boosterStock(preview)==before+1,"gift retry acknowledges exactly one physical unit");
            check(store(c.reboot()).nextLootMilestone()==10,"recovered gift receipt persists across cold recreation");
        }
    }

    private static void grades(){
        check(JourneyExperience.SCORE_VERSION==1,"explicit action grading policy version");
        for(boolean remix:new boolean[]{false,true})for(int number=1;number<=(remix?240:1000);number++){
            Level level=remix?StrategyCatalog.remix(number):StrategyCatalog.get(number);MoveBudget budget=MoveBudget.start(remix?3:0,level);
            int threshold=level.minimumActions+(budget.base()-level.minimumActions)/2;
            check(JourneyExperience.threeStarLimit(budget)==threshold,"grade threshold equals productive minimum plus half margin");
            for(int used=0;used<=budget.base();used++){
                check(JourneyExperience.stars(budget)==(used<=threshold?3:2),"inclusive three/two-star cutoff "+number+" used "+used);
                check(JourneyExperience.stars(budget.copy())==JourneyExperience.stars(budget),"copy preserves grading");
                if(used<budget.base())budget.consumeAction();
            }
            check(budget.extend(1)&&JourneyExperience.stars(budget)==1,"an extension awards one star");
            check(budget.refundAction()&&JourneyExperience.stars(budget)==1,"extension history remains visible after free refund");
        }
        Level level=StrategyCatalog.get(13);MoveBudget budget=MoveBudget.start(0,level);int limit=JourneyExperience.threeStarLimit(budget);
        for(int used=0;used<=limit;used++)budget.consumeAction();
        check(JourneyExperience.stars(budget)==2&&budget.refundAction()&&JourneyExperience.stars(budget)==3,"free Undo refunds into the three-star range");
        MoveBudget unlimited=MoveBudget.start(4,level);for(int i=0;i<100;i++)unlimited.consumeAction();
        check(unlimited.isUnlimited()&&JourneyExperience.stars(unlimited)==3,"unlimited friend challenge always three stars");
        for(int mode:new int[]{1,2})for(int seed=0;seed<50;seed++){
            MoveBudget event=MoveBudget.start(mode,StrategyCatalog.event(seed));
            check(JourneyExperience.threeStarLimit(event)==event.minimumActions()+event.mistakeAllowance()/2,"event grade follows exact supplied budget");
        }
        boolean first=false,second=false;try{JourneyExperience.stars(null);}catch(IllegalArgumentException expected){first=true;}try{JourneyExperience.threeStarLimit(null);}catch(IllegalArgumentException expected){second=true;}
        check(first&&second,"missing budget cannot produce a grade");
    }

    public static void main(String[] args)throws Exception{
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));explicitTrials();trialCapacityAndFailure();trialConcurrency();stableGifts();grades();
        String report="{\n  \"passed\": true,\n  \"assertions\": "+checks+",\n  \"catalog_boards_scored\": 1240,\n  \"gift_milestones_compared\": 200,\n  \"trial_powers\": 6,\n  \"scope\": \"Actual GameStore, LocalSaveTransactions, MoveBudget and JourneyExperience on a JVM; deterministic Android preference doubles including memory-before-disk and cold recreation. No human retention or external purchase claim.\"\n}\n";
        if(args.length>0){Path path=Path.of(args[0]);Files.createDirectories(path.getParent());Files.writeString(path,report);}
        System.out.println("PASS "+checks+" v1.9 reward, explicit power trial, durability, stable gift and action-only grading checks");
    }
}
