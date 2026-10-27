import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.engine.*;
import com.androidhit.lumadrift.onboarding.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** Real public store calls under consecutive failures, including reads that flush pending writes. */
public final class LocalSaveV19Test {
    static int checks,failures,cases;static final StringBuilder trace=new StringBuilder("case,memory_before_disk,stage,typed_exception,disk_unchanged\n");
    static void check(boolean ok,String reason){checks++;if(!ok)throw new AssertionError(reason);}
    static GameStore store(RewardsCosmeticsV17Test.Ctx context){return new GameStore(context,null,()->1793059200000L);}
    static void fail(RewardsCosmeticsV17Test.Prefs prefs,boolean memoryFirst,Runnable action,String name,String stage,Map<String,Object> disk){
        prefs.fail(memoryFirst);boolean typed=false;
        try{action.run();}catch(GameStore.LocalSaveException expected){typed=true;check(expected instanceof IllegalStateException,"existing IllegalStateException handlers remain compatible");}
        catch(IllegalStateException wrong){throw new AssertionError("Wrong failure type for "+name+"/"+stage,wrong);}
        check(typed,"consecutive failure has explicit retryable type "+name+"/"+stage);failures++;
        check(prefs.disk.equals(disk),"no partial durable state "+name+"/"+stage);
        trace.append(name).append(',').append(memoryFirst).append(',').append(stage).append(",true,true\n");
    }
    static void trial(boolean memoryFirst){cases++;RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore a=store(c),b=store(c);int stock=a.boosterStock(Catalog.HINT),coins=a.coins();Map<String,Object> disk=new HashMap<>(c.save().disk);
        fail(c.save(),memoryFirst,()->a.claimPowerTrial(Catalog.HINT),"power_trial","write",disk);
        fail(c.save(),memoryFirst,()->b.powerTrialAvailable(Catalog.HINT),"power_trial","availability_read_flush",disk);
        fail(c.save(),memoryFirst,()->a.boosterStock(Catalog.HINT),"power_trial","stock_read_flush",disk);
        fail(c.save(),memoryFirst,()->b.retryPendingLocalSave(),"power_trial","explicit_retry_still_fails",disk);
        b.retryPendingLocalSave();check(a.powerTrialClaimed(Catalog.HINT)&&a.boosterStock(Catalog.HINT)==stock+1&&a.coins()==coins,"one exact sample after eventual durable retry");
        check(a.claimPowerTrial(Catalog.HINT)==GameStore.PowerTrialStatus.ALREADY_CLAIMED&&b.claimPowerTrial(Catalog.HINT)==GameStore.PowerTrialStatus.ALREADY_CLAIMED,"cross handle duplicate never credits another sample");
        a.retryPendingLocalSave();GameStore cold=store(c.reboot());check(cold.powerTrialClaimed(Catalog.HINT)&&cold.boosterStock(Catalog.HINT)==stock+1&&cold.claimPowerTrial(Catalog.HINT)==GameStore.PowerTrialStatus.ALREADY_CLAIMED,"sample and receipt remain exact after cold restart");
    }
    static void guardian(boolean memoryFirst){cases++;RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore a=store(c);for(int n=1;n<=10;n++)a.completeLevel(n,3);
        GuardianEncounter encounter=GuardianEncounter.start(1);for(int id:encounter.optimalPlan())check(encounter.move(id).success,"actual Guardian solved before local reward request");check(encounter.isSolved(),"real Guardian complete");
        int coins=a.coins();Map<String,Object> disk=new HashMap<>(c.save().disk);GameStore b=store(c);
        fail(c.save(),memoryFirst,()->a.completeGuardian(1),"guardian_reward","write",disk);
        fail(c.save(),memoryFirst,()->b.guardianCleared(1),"guardian_reward","receipt_read_flush",disk);
        fail(c.save(),memoryFirst,()->a.coins(),"guardian_reward","balance_read_flush",disk);
        fail(c.save(),memoryFirst,()->b.retryPendingLocalSave(),"guardian_reward","explicit_retry_still_fails",disk);
        b.retryPendingLocalSave();check(a.guardianCleared(1)&&a.coins()==coins+100&&a.completedLevels()==10,"Guardian retry saves receipt and full 100 coins without Journey advance");
        check(a.completeGuardian(1)==0&&b.completeGuardian(1)==0,"Guardian reward repeated after retry returns zero");
        GameStore cold=store(c.reboot());cold.retryPendingLocalSave();check(cold.guardianCleared(1)&&cold.coins()==coins+100&&cold.completeGuardian(1)==0,"durable Guardian claim survives cold replay");
    }
    static TutorialSession completePractice(){TutorialSession t=TutorialSession.start();int guard=40;while(!t.complete()&&guard-->0){TutorialSession.Action action;
        switch(t.requiredAction()){case MOVE:case TRY_BLOCKED:action=t.move(t.expectedTileId());break;case PREVIEW_SWITCH:action=t.previewSwitch(t.expectedTileId());break;case PREVIEW_GATE:action=t.previewGate(t.expectedGate());break;case OPEN_GATE:action=t.openGate(t.expectedGate());break;case UNDO:action=t.undo();break;case HINT:action=t.hint();break;case DONE:action=t.advance();break;default:throw new AssertionError("practice transition");}check(action.accepted,"actual practice action accepted");}check(t.complete(),"actual practice completed");return t;}
    static void tutorial(boolean memoryFirst){cases++;RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore a=store(c);a.saveTutorialState(completePractice().encode());int coins=a.coins(),stock=a.boosterStock(Catalog.HINT);Map<String,Object> disk=new HashMap<>(c.save().disk);
        fail(c.save(),memoryFirst,()->a.claimTutorialReward(),"tutorial_reward","write",disk);
        fail(c.save(),memoryFirst,()->a.tutorialRewardClaimed(),"tutorial_reward","receipt_read_flush",disk);
        fail(c.save(),memoryFirst,()->a.tutorialState(),"tutorial_reward","practice_read_flush",disk);
        fail(c.save(),memoryFirst,()->a.retryPendingLocalSave(),"tutorial_reward","explicit_retry_still_fails",disk);
        a.retryPendingLocalSave();check(a.tutorialRewardClaimed()&&a.coins()==coins+100&&a.boosterStock(Catalog.HINT)==stock+2&&a.completedLevels()==0,"practice retry retains exact two-part gift and no campaign credit");
        GameStore cold=store(c.reboot());check(cold.claimTutorialReward()==GameStore.TutorialRewardStatus.ALREADY_CLAIMED&&cold.coins()==coins+100&&cold.boosterStock(Catalog.HINT)==stock+2,"practice replay cannot duplicate gift after consecutive failures");
    }
    static void style(boolean memoryFirst){cases++;RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore a=store(c);int coins=a.coins();Map<String,Object> disk=new HashMap<>(c.save().disk);
        fail(c.save(),memoryFirst,()->a.equipAvatarStyle("ice"),"avatar_style","write",disk);
        fail(c.save(),memoryFirst,()->a.avatarStyle(),"avatar_style","style_read_flush",disk);
        fail(c.save(),memoryFirst,()->a.coins(),"avatar_style","unrelated_balance_read_flush",disk);
        fail(c.save(),memoryFirst,()->a.retryPendingLocalSave(),"avatar_style","explicit_retry_still_fails",disk);
        a.retryPendingLocalSave();check(a.avatarStyle().equals("ice")&&a.coins()==coins&&store(c.reboot()).avatarStyle().equals("ice"),"visual-only retry becomes durable without any debit");
    }
    static void coldBeforeRetry(boolean memoryFirst){cases++;RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore old=store(c);int coins=old.coins(),stock=old.boosterStock(Catalog.HINT);Map<String,Object> disk=new HashMap<>(c.save().disk);
        fail(c.save(),memoryFirst,()->old.claimPowerTrial(Catalog.HINT),"cold_before_retry","write",disk);
        fail(c.save(),memoryFirst,()->old.powerTrialClaimed(Catalog.HINT),"cold_before_retry","read_flush",disk);
        GameStore cold=store(c.reboot());check(!cold.powerTrialClaimed(Catalog.HINT)&&cold.coins()==coins&&cold.boosterStock(Catalog.HINT)==stock,"process death after repeated failures exposes only last durable map");
        cold.retryPendingLocalSave();check(cold.claimPowerTrial(Catalog.HINT)==GameStore.PowerTrialStatus.GRANTED&&cold.boosterStock(Catalog.HINT)==stock+1&&cold.claimPowerTrial(Catalog.HINT)==GameStore.PowerTrialStatus.ALREADY_CLAIMED,"cold retry creates only the one missing durable receipt");
    }
    static void metaCompatibility(boolean memoryFirst){cases++;RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore wallet=store(c);MetaProgress meta=new MetaProgress(c,()->1793059200000L);Map<String,Object> disk=new HashMap<>(c.meta().disk);
        fail(c.meta(),memoryFirst,()->meta.recordWin(0,1,3,""),"meta_progress","write",disk);
        fail(c.meta(),memoryFirst,()->meta.xp(),"meta_progress","xp_read_flush",disk);
        fail(c.meta(),memoryFirst,()->meta.completedBoards(),"meta_progress","progress_read_flush",disk);
        wallet.retryPendingLocalSave();check(c.meta().disk.equals(disk),"GameStore retry intentionally flushes its own preference file only");
        int xp=meta.xp();check(xp>0&&meta.completedBoards()==1&&meta.recordWin(0,1,3,"")==0,"meta read eventually flushes one exact win under same typed exception contract");
        MetaProgress cold=new MetaProgress(c.reboot(),()->1793059200000L);check(cold.xp()==xp&&cold.completedBoards()==1,"meta retry remains durable after cold reconstruction");
    }
    static void concurrentRetry()throws Exception{cases++;RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore a=store(c),b=store(c);int before=a.boosterStock(Catalog.HINT);Map<String,Object> disk=new HashMap<>(c.save().disk);
        fail(c.save(),true,()->a.claimPowerTrial(Catalog.HINT),"concurrent_retry","write",disk);
        fail(c.save(),true,()->b.retryPendingLocalSave(),"concurrent_retry","retry_still_fails",disk);
        ExecutorService workers=Executors.newFixedThreadPool(2);CountDownLatch go=new CountDownLatch(1);
        try{Future<?> x=workers.submit(()->{try{go.await();a.retryPendingLocalSave();}catch(InterruptedException e){Thread.currentThread().interrupt();throw new AssertionError(e);}}),y=workers.submit(()->{try{go.await();b.retryPendingLocalSave();}catch(InterruptedException e){Thread.currentThread().interrupt();throw new AssertionError(e);}});go.countDown();x.get(5,TimeUnit.SECONDS);y.get(5,TimeUnit.SECONDS);}finally{workers.shutdownNow();}
        check(a.boosterStock(Catalog.HINT)==before+1&&b.powerTrialClaimed(Catalog.HINT)&&store(c.reboot()).boosterStock(Catalog.HINT)==before+1,"concurrent explicit retry never replays a grant");
    }
    public static void main(String[]args)throws Exception{Path out=Path.of(args[0]);Files.createDirectories(out);long start=System.nanoTime();
        for(boolean memoryFirst:new boolean[]{false,true}){trial(memoryFirst);guardian(memoryFirst);tutorial(memoryFirst);style(memoryFirst);coldBeforeRetry(memoryFirst);metaCompatibility(memoryFirst);}concurrentRetry();
        Files.writeString(out.resolve("failure-trace.csv"),trace);String summary="{\n  \"passed\":true,\n  \"assertions\":"+checks+",\n  \"scenarios\":"+cases+",\n  \"consecutive_commit_failures_exercised\":"+failures+",\n  \"maximum_consecutive_failures_per_scenario\":4,\n  \"memory_before_disk_modes\":[false,true],\n  \"elapsed_host_seconds\":"+((System.nanoTime()-start)/1e9)+",\n  \"scope\":\"Real public GameStore/MetaProgress/TutorialSession/GuardianEncounter APIs with established preference doubles. Explicit typed write/read-flush failures, durable retry, receipts, inventory, process reconstruction and concurrent retry. This does not qualify native rendering, physical flash failures or provider access.\"\n}\n";
        Files.writeString(out.resolve("summary.json"),summary);System.out.println(summary);
    }
}
