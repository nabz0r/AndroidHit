import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.engine.*;
import java.util.*;
import java.util.concurrent.*;

/** Real Session122 and GameStore on durable preference doubles, including memory-before-disk failure. */
public final class TacticalChapterSessionV122Test {
    private static int checks,completed;
    private static void check(boolean valid,String message){checks++;if(!valid)throw new AssertionError(message);}
    private static GameStore store(RewardsCosmeticsV17Test.Ctx context){return new GameStore(context,null,new RewardsCosmeticsV17Test.Clock("2026-10-30"));}
    private static RewardsCosmeticsV17Test.Prefs prefs(RewardsCosmeticsV17Test.Ctx c){return c.files.get(TacticalChapterSession122.PREFS);}
    private static void failure(Runnable action,String message){boolean failed=false;try{action.run();}catch(IllegalStateException expected){failed=true;}check(failed,message);}
    private static String snapshot(TacticalChapterSession122 s){return s.run().encode()+":"+(s.board()==null?"-":Arrays.toString(s.board().history()))+":"+(s.budget()==null?"-":s.budget().used())+":"+s.undos()+":"+s.attempt();}
    private static void playBoard(TacticalChapterSession122 session){
        for(int guard=0;guard<30;guard++){
            Board b=session.board();if(b.isSolved())return;int[] continuation=b.solve();check(continuation!=null&&continuation.length>0,"test oracle supplies valid completion only to test driver");Board.MoveResult action=session.move(continuation[0]);check(action!=null&&action.success,"actual session exit commits");
        }
        throw new AssertionError("board did not finish");
    }
    private static void ready(TacticalChapterSession122 s){check(s.run()!=null,"active run exists");if(s.run().currentLayout()==null)check(s.choose(s.run().options().get(0).id),"durable route selection");}
    private static void throughFirstThree(TacticalChapterSession122 s){for(int stage=0;stage<3;stage++){ready(s);playBoard(s);check(s.finishIfSolved(),"real solved board advances");}ready(s);playBoard(s);}
    private static void isolationAndCold(){
        RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore wallet=store(c);Map<String,Object> campaign=new HashMap<>(c.save().disk);TacticalChapterSession122 s=new TacticalChapterSession122(c,wallet);
        check(s.run()==null&&!s.isCorrupt(),"empty chapter has no invented progress");check(s.startEpisode(TacticalChapterCatalog.Episode.KEYS),"first episode starts");check(!s.startEpisode(TacticalChapterCatalog.Episode.RELAYS),"active sortie cannot silently reset through episode switch");ready(s);
        Board detached=s.board();int first=detached.level.solution[0];detached.move(first);MoveBudget detachedBudget=s.budget();detachedBudget.consumeAction();check(s.board().moves()==0&&s.budget().used()==0,"UI copies cannot mutate persistable board or ledger");
        check(s.useTalent(1)==null&&s.run().chargeRemaining()==6,"no history no repair charge");TacticalChapterAction.Draft guide=s.useTalent(0);check(guide!=null&&s.run().chargeRemaining()==4&&s.hintId()==first,"information has durable effect and debit");
        RewardsCosmeticsV17Test.Ctx cold=c.reboot();GameStore coldWallet=store(cold);s=new TacticalChapterSession122(cold,coldWallet);check(s.run().used(0)&&s.run().chargeRemaining()==4&&s.hintId()==first,"cold restore preserves guide use and active cue");
        check(s.move(first).success&&s.hintId()==-1,"next board action clears stale guide cue");check(s.undo()&&s.board().moves()==0&&s.budget().used()==1&&s.undos()==1,"manual Undo costs no Coins and keeps spent action");
        int charge=s.run().chargeRemaining();check(s.resetAttempt()&&s.attempt()==1&&s.board().moves()==0&&s.budget().used()==0,"explicit retry resets board budget");check(s.run().used(0)&&s.run().chargeRemaining()==charge&&s.useTalent(0)==null,"retry never refreshes shared talent reserve");
        check(c.save().disk.equals(campaign)&&cold.save().disk.equals(campaign),"all pre-completion activity leaves campaign wallet ownership and energy unchanged");
        String state=snapshot(s);TacticalChapterSession122 another=new TacticalChapterSession122(cold,coldWallet);check(snapshot(another).equals(state),"full tuple restores coherently");check(another.move(first).success,"second handle actual move");check(s.move(first)!=null&&!s.move(first).success,"stale handle refreshes and cannot apply duplicate exit");
        check(s.run().chargeRemaining()==4,"manual exits never recharge reserve");
    }
    private static void completeAndReward(){
        for(TacticalChapterCatalog.Episode episode:TacticalChapterCatalog.Episode.values())for(int branch=0;branch<4;branch++){
            RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore wallet=store(c);int coins=wallet.coins();TacticalChapterSession122 s=new TacticalChapterSession122(c,wallet);check(s.startEpisode(episode),"episode start");
            for(int stage=0;stage<4;stage++){
                int choice=stage==1?branch%2:stage==2?branch/2:0;check(s.choose(s.run().options().get(choice).id),"actual branch selected");playBoard(s);check(s.finishIfSolved(),"stage receipt commits");
                c=c.reboot();wallet=store(c);s=new TacticalChapterSession122(c,wallet);check(s.run().completedStages==stage+1,"stage progress cold restores");check(wallet.coins()==coins+(stage==3?120:0),"only finale grants fixed120");
            }
            completed++;check(s.run().finished()&&s.run().claimed&&s.hasSouvenir(episode),"claim and earned souvenir recover");check(s.finishIfSolved()&&wallet.coins()==coins+120,"repeated finalization does not reward twice");
            check(s.startEpisode(episode),"completed episode can replay");throughFirstThree(s);check(s.finishIfSolved()&&wallet.coins()==coins+120,"same episode replay earns no second milestone");check(s.hasSouvenir(episode),"souvenir persists through repeat run");
            TacticalChapterCatalog.Episode other=episode==TacticalChapterCatalog.Episode.KEYS?TacticalChapterCatalog.Episode.RELAYS:TacticalChapterCatalog.Episode.KEYS;check(s.startEpisode(other)&&s.hasSouvenir(episode),"next episode preserves earlier souvenir");
            check(wallet.companionCopies("hoot")==0&&wallet.companionCopies("orbit")==0,"loaned companions never become owned or receive copies");
        }
    }
    private static void commitFailures(){
        for(boolean memory:new boolean[]{false,true})for(boolean reboot:new boolean[]{false,true})for(String operation:new String[]{"choose","move","talent","undo","retry"}){
            RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore wallet=store(c);TacticalChapterSession122 s=new TacticalChapterSession122(c,wallet);s.startEpisode(TacticalChapterCatalog.Episode.KEYS);if(!operation.equals("choose"))ready(s);
            if(operation.equals("undo")||operation.equals("retry"))check(s.move(s.board().level.solution[0]).success,"failure fixture actual history");
            String prior=snapshot(s);Map<String,Object> disk=new HashMap<>(prefs(c).disk);TacticalChapterSession122 active=s;prefs(c).fail(memory);
            switch(operation){case "choose":failure(()->active.choose(active.run().options().get(0).id),"route failure surfaced");break;case "move":failure(()->active.move(active.board().level.solution[0]),"exit failure surfaced");break;case "talent":failure(()->active.useTalent(0),"talent failure surfaced");break;case "undo":failure(active::undo,"undo failure surfaced");break;default:failure(active::resetAttempt,"retry failure surfaced");}
            check(prefs(c).disk.equals(disk),"failed transaction has no partial durable fields");check(snapshot(s).equals(prior),"unpersisted candidate does not publish into live session");
            if(reboot){c=c.reboot();wallet=store(c);s=new TacticalChapterSession122(c,wallet);check(snapshot(s).equals(prior),"cold process discards uncommitted tuple");}
            else {check(s.reload(),"warm recovery flushes one complete pending tuple");check(!snapshot(s).equals(prior),"warm replay publishes committed whole tuple");check(new TacticalChapterSession122(c,wallet).run().encode().equals(s.run().encode()),"second handle sees exact recovery");}
        }
    }
    private static void completionFailures(){
        for(boolean memory:new boolean[]{false,true})for(boolean cold:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore wallet=store(c);int coins=wallet.coins();TacticalChapterSession122 s=new TacticalChapterSession122(c,wallet);s.startEpisode(TacticalChapterCatalog.Episode.RELAYS);throughFirstThree(s);TacticalChapterSession122 active=s;c.save().fail(memory);failure(active::finishIfSolved,"wallet failure after durable finale surfaced");
            check(s.run().finished()&&!s.run().claimed&&!s.hasSouvenir(TacticalChapterCatalog.Episode.RELAYS),"interrupted reward remains finished unclaimed");if(cold){c=c.reboot();wallet=store(c);s=new TacticalChapterSession122(c,wallet);}else check(s.reload(),"warm wallet recovery");
            check(s.run().claimed&&wallet.coins()==coins+120&&s.hasSouvenir(TacticalChapterCatalog.Episode.RELAYS),"wallet retry grants exactly one reward and souvenir");check(s.finishIfSolved()&&wallet.coins()==coins+120,"recovered completion stays idempotent");
        }
        // Explicit crash fixture: wallet receipt exists, chapter marker has not committed yet.
        for(boolean memory:new boolean[]{false,true}){
            RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore wallet=store(c);int coins=wallet.coins();TacticalChapterSession122 s=new TacticalChapterSession122(c,wallet);s.startEpisode(TacticalChapterCatalog.Episode.KEYS);throughFirstThree(s);
            TacticalChapterRun finished=s.run().recordWin(s.run().currentVictoryId(),s.board(),3);String record=TacticalChapterSession122.VERSION+"\n"+finished.encode()+"\n-\n-\n0,-1,0";prefs(c).edit().putString(TacticalChapterSession122.STATE_KEY,record).commit();check(wallet.grantMilestone("tactical122_keys",120)==120,"fixture wallet receipt committed before chapter marker");prefs(c).fail(memory);TacticalChapterSession122 active=s;failure(active::reload,"claim marker failure surfaced");
            RewardsCosmeticsV17Test.Ctx boot=c.reboot();GameStore bootWallet=store(boot);TacticalChapterSession122 restored=new TacticalChapterSession122(boot,bootWallet);check(restored.run().claimed&&restored.hasSouvenir(TacticalChapterCatalog.Episode.KEYS)&&bootWallet.coins()==coins+120,"cold claim-marker recovery does not duplicate existing milestone");
        }
    }
    private static void corruptSaves(){
        RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore wallet=store(c);TacticalChapterSession122 s=new TacticalChapterSession122(c,wallet);s.startEpisode(TacticalChapterCatalog.Episode.KEYS);ready(s);String valid=(String)prefs(c).disk.get(TacticalChapterSession122.STATE_KEY);int coins=wallet.coins();
        String[] parts=valid.split("\n",-1);List<String> invalid=new ArrayList<>();invalid.add(valid+"\n");invalid.add(valid.replace(TacticalChapterSession122.VERSION,"future_session"));String[] altered=parts.clone();altered[3]="6,0,1,0,0,0";invalid.add(String.join("\n",altered));altered=parts.clone();altered[2]="0,0";invalid.add(String.join("\n",altered));altered=parts.clone();altered[4]="0,0,1000";invalid.add(String.join("\n",altered));
        for(String record:invalid){prefs(c).edit().putString(TacticalChapterSession122.STATE_KEY,record).commit();TacticalChapterSession122 broken=new TacticalChapterSession122(c,wallet);check(broken.isCorrupt()&&broken.run()==null&&broken.board()==null&&broken.budget()==null,"invalid record never partially restores board or resource");check(broken.move(0)==null&&!broken.finishIfSolved()&&wallet.coins()==coins,"invalid record cannot award progression or reward");check(prefs(c).getString(TacticalChapterSession122.STATE_KEY,"").equals(record),"invalid evidence preserved until explicit new episode");}
        check(s.startEpisode(TacticalChapterCatalog.Episode.KEYS)&&!s.isCorrupt(),"explicit start safely replaces invalid record");
    }
    private static void concurrentHandles()throws Exception{
        ExecutorService pool=Executors.newFixedThreadPool(3,work->{Thread thread=new Thread(work);thread.setDaemon(true);return thread;});
        try{
            RewardsCosmeticsV17Test.Ctx c=new RewardsCosmeticsV17Test.Ctx();GameStore wallet=store(c),otherWallet=store(c);int coins=wallet.coins();TacticalChapterSession122 first=new TacticalChapterSession122(c,wallet);first.startEpisode(TacticalChapterCatalog.Episode.KEYS);TacticalChapterSession122 second=new TacticalChapterSession122(c,otherWallet);throughFirstThree(first);
            CountDownLatch ready=new CountDownLatch(1);Future<Boolean> a=pool.submit(()->{ready.await();return first.finishIfSolved();});Future<Boolean> b=pool.submit(()->{ready.await();return second.finishIfSolved();});Future<Integer> funds=pool.submit(()->{ready.await();int total=0;for(int i=0;i<20;i++)total+=otherWallet.grantMilestone("tactical_concurrent_"+i,5);return total;});ready.countDown();
            check(a.get(8,TimeUnit.SECONDS)&&b.get(8,TimeUnit.SECONDS)&&funds.get(8,TimeUnit.SECONDS)==100,"parallel finalization and another wallet handle finish without lock inversion");check(wallet.coins()==coins+220&&first.reload()&&first.run().claimed&&second.reload()&&second.run().claimed,"parallel wallets grant exactly one episode reward");
            check(first.startEpisode(TacticalChapterCatalog.Episode.RELAYS),"next episode for simultaneous command");ready(first);TacticalChapterSession122 third=new TacticalChapterSession122(c,otherWallet);CountDownLatch use=new CountDownLatch(1);Future<TacticalChapterAction.Draft> useA=pool.submit(()->{use.await();return first.useTalent(0);});Future<TacticalChapterAction.Draft> useB=pool.submit(()->{use.await();return third.useTalent(0);});use.countDown();
            TacticalChapterAction.Draft one=useA.get(8,TimeUnit.SECONDS),two=useB.get(8,TimeUnit.SECONDS);check((one==null)!=(two==null),"simultaneous duplicate talent has one persisted winner");check(first.reload()&&first.run().chargeRemaining()==4,"parallel use debits once");
        }finally{pool.shutdownNow();}
    }
    public static void main(String[] args)throws Exception{isolationAndCold();completeAndReward();commitFailures();completionFailures();corruptSaves();concurrentHandles();System.out.println("{\"passed\":true,\"checks\":"+checks+",\"completeRoutes\":"+completed+",\"scope\":\"Actual isolated chapter session and GameStore, all branch routes, loans, persistent Charge, reset, no campaign mutation before reward, atomic writes, before/after-memory disk failures, cold recovery, milestone/claim crash windows, concurrent handles and strict corrupt-record refusal. Not Android filesystem/gestures/rendering or human enjoyment.\"}");}
}
