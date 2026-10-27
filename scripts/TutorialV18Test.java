import android.content.*;
import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.engine.*;
import com.androidhit.lumadrift.onboarding.*;
import java.util.*;
import java.util.concurrent.*;

/** Pure-session / real-GameStore contracts; no provider, account or installed UI is used. */
public final class TutorialV18Test {
    private static int checks;
    private static void check(boolean pass,String message){checks++;if(!pass)throw new AssertionError(message);}
    private static final class Prefs implements SharedPreferences {
        final Map<String,Object> memory=new HashMap<>(),disk=new HashMap<>();boolean fail,applyBeforeFailure;
        public Map<String,?> getAll(){return new HashMap<>(memory);}
        public boolean contains(String key){return memory.containsKey(key);}
        public int getInt(String key,int fallback){return (Integer)memory.getOrDefault(key,fallback);}
        public String getString(String key,String fallback){return (String)memory.getOrDefault(key,fallback);}
        public boolean getBoolean(String key,boolean fallback){return (Boolean)memory.getOrDefault(key,fallback);}
        public Editor edit(){return new Editor(){final Map<String,Object> writes=new HashMap<>();
            public Editor putInt(String k,int v){writes.put(k,v);return this;}
            public Editor putBoolean(String k,boolean v){writes.put(k,v);return this;}
            public Editor putString(String k,String v){writes.put(k,v);return this;}
            public boolean commit(){if(fail){fail=false;if(applyBeforeFailure)memory.putAll(writes);return false;}memory.putAll(writes);disk.putAll(writes);return true;}
        };}
        void set(String k,Object v){memory.put(k,v);disk.put(k,v);}
        void fail(boolean memoryBeforeDisk){fail=true;applyBeforeFailure=memoryBeforeDisk;}
        Prefs reboot(){Prefs p=new Prefs();p.memory.putAll(disk);p.disk.putAll(disk);return p;}
    }
    private static final class Ctx extends Context {
        final Map<String,Prefs> files=new HashMap<>();
        public SharedPreferences getSharedPreferences(String name,int mode){return files.computeIfAbsent(name,k->new Prefs());}
        Prefs save(){return (Prefs)getSharedPreferences("luma_drift_save_v1",0);}
        Ctx reboot(){Ctx c=new Ctx();files.forEach((name,p)->c.files.put(name,p.reboot()));return c;}
    }
    private static TutorialSession.Action play(TutorialSession session){
        switch(session.requiredAction()){
            case MOVE:case TRY_BLOCKED:return session.move(session.expectedTileId());
            case PREVIEW_SWITCH:return session.previewSwitch(session.expectedTileId());
            case PREVIEW_GATE:return session.previewGate(session.expectedGate());
            case OPEN_GATE:return session.openGate(session.expectedGate());
            case UNDO:return session.undo();
            case HINT:return session.hint();
            case DONE:return session.advance();
            default:throw new AssertionError("No action for "+session.requiredAction());
        }
    }
    private static TutorialSession finished(){TutorialSession t=TutorialSession.start();for(int guard=0;!t.complete()&&guard<40;guard++){check(play(t).accepted,"real expected action accepted");}check(t.complete(),"all lessons finish");return t;}
    private static void same(TutorialSession a,TutorialSession b,String message){
        check(a.encode().equals(b.encode()),message+" exact record");
        check(a.lessonIndex()==b.lessonIndex()&&a.phase()==b.phase()&&a.requiredAction()==b.requiredAction(),message+" same phase and request");
        check(a.instructionKey().equals(b.instructionKey())&&a.highlightedHintId()==b.highlightedHintId(),message+" same prompt and hint");
        if(a.board()==null){check(b.board()==null,message+" complete board absent");return;}
        check(a.board().gateMask()==b.board().gateMask()&&a.board().remainingMask()==b.board().remainingMask(),message+" actual gates and pieces");
        check(Arrays.equals(a.board().history(),b.board().history()),message+" typed history");
        Board.ActionPreview p=a.currentPreview(),q=b.currentPreview();check((p==null)==(q==null),message+" preview availability");
        if(p!=null)check(p.action==q.action&&p.openedMask==q.openedMask&&p.closedMask==q.closedMask,message+" preview mechanics");
    }
    private static void lessons(){
        TutorialSession t=TutorialSession.start();int steps=0;Set<Integer> seen=new HashSet<>();
        while(!t.complete()){
            seen.add(t.lessonIndex());same(t,TutorialSession.restore(t.encode()),"recreate at action "+steps);
            check(TutorialSession.isValidSave(t.encode()),"own save valid");
            String before=t.encode();TutorialSession.RequiredAction required=t.requiredAction();
            if(required!=TutorialSession.RequiredAction.DONE){check(!t.advance().accepted&&before.equals(t.encode()),"Next cannot solve lesson "+t.lessonIndex());}
            check(!t.move(48).accepted&&before.equals(t.encode()),"unrelated piece never changes board/phase");
            if(required!=TutorialSession.RequiredAction.UNDO)check(!t.undo().accepted&&before.equals(t.encode()),"unexpected Undo cannot skip required action");
            if(required!=TutorialSession.RequiredAction.HINT)check(!t.hint().accepted&&before.equals(t.encode()),"unexpected hint cannot advance");
            TutorialSession.Action result=play(t);check(result.accepted&&result.changed,"requested action changes real session");
            check(TutorialStrings.has(result.feedbackKey)&&TutorialStrings.has(t.instructionKey()),"all dynamic messages translated");
            if(required==TutorialSession.RequiredAction.TRY_BLOCKED)check(result.move!=null&&!result.move.success&&t.board().history().length==0,"blocked attempt teaches without charging/exit");
            if(required==TutorialSession.RequiredAction.PREVIEW_GATE)check(t.board().history().length==0&&!t.board().isGateOpen(Direction.UP)&&result.preview.openedMask==Direction.UP.bit,"gate preview stays read only");
            if(required==TutorialSession.RequiredAction.OPEN_GATE)check(t.spentActions()==1&&t.board().remainingCount()==1&&t.board().isGateOpen(Direction.UP),"manual gate is one real productive action");
            if(required==TutorialSession.RequiredAction.PREVIEW_SWITCH)check(result.preview.openedMask==Direction.UP.bit&&result.preview.closedMask==Direction.RIGHT.bit&&t.board().history().length==0,"switch preview discloses both exact edges without mutation");
            if(t.lessonIndex()==4&&t.phase()==2)check(t.board().isGateOpen(Direction.UP)&&!t.board().isGateOpen(Direction.RIGHT),"actual switch matches its preview");
            if(required==TutorialSession.RequiredAction.UNDO)check(t.spentActions()==0&&t.board().remainingCount()==2,"Undo returns piece and action without another cost");
            if(required==TutorialSession.RequiredAction.HINT)check(result.hintTileId==1&&t.highlightedHintId()==1&&t.spentActions()==0&&t.board().remainingCount()==2,"hint points at safe blocker without playing it");
            if(t.lessonComplete())check(t.board().isSolved(),"lesson only completes with genuine solved board");
            steps++;
        }
        check(seen.size()==7&&steps==27,"seven interactive lessons and seven explicit transitions");
        check(t.board()==null&&t.encode().equals(TutorialSession.COMPLETED_SAVE)&&t.requiredAction()==TutorialSession.RequiredAction.FINISHED,"one canonical finished state");
        same(t,TutorialSession.restore(t.encode()),"completed reconstruction");
        check(!t.advance().accepted&&!t.hint().accepted&&!t.undo().accepted&&!t.move(0).accepted,"completed practice cannot alter state");
        TutorialSession swipe=TutorialSession.start();String before=swipe.encode();
        check(!swipe.move(0,Direction.LEFT).accepted&&before.equals(swipe.encode()),"opposite swipe neither exits nor advances");
        check(swipe.move(0,Direction.RIGHT).accepted&&swipe.lessonComplete(),"actual correct swipe works like tap");
    }
    private static void skipReplayAndRecreate(){
        TutorialSession t=TutorialSession.start();int variants=0;
        while(!t.complete()){
            String active=t.encode();check(t.skip().accepted&&t.skipped(),"skip accepted at each phase");
            TutorialSession loaded=TutorialSession.restore(t.encode());same(t,loaded,"skip retained phase");
            check(!loaded.advance().accepted&&!loaded.move(0).accepted&&!loaded.hint().accepted,"skip does not secretly play");
            check(loaded.resume().accepted&&loaded.encode().equals(active),"resume exact phase/history");
            check(loaded.restartLesson().accepted&&loaded.phase()==0&&loaded.board().history().length==0,"restart current lesson retains its number");
            check(loaded.lessonIndex()==t.lessonIndex(),"restart does not advance or forget preceding lessons");
            check(loaded.replay().accepted&&loaded.lessonIndex()==0&&loaded.phase()==0&&!loaded.skipped(),"replay starts first lesson");
            t.resume();play(t);variants++;
        }
        check(variants==27,"skip/recreate/restart/replay covered every active phase");
        check(t.replay().accepted&&!t.complete()&&t.board().remainingCount()==1,"completed replay has fresh real board");
    }
    private static void invalidSaves(){
        String[] bad={null,"","T2|0|0|ACTIVE|","T1|-1|0|ACTIVE|","T1|7|0|ACTIVE|","T1|0|2|ACTIVE|","T1|0|0|UNKNOWN|","T1|0|1|ACTIVE|","T1|0|0|ACTIVE|0","T1|0|1|ACTIVE|1","T1|0|1|ACTIVE|0,0","T1|3|2|ACTIVE|0","T1|7|1|COMPLETE|","T1|7|0|COMPLETE|0","T1|0|0|ACTIVE|\n","T1|00|0|ACTIVE|","x".repeat(300)};
        for(String state:bad){check(!TutorialSession.isValidSave(state),"invalid state rejected: "+state);check(TutorialSession.restore(state).encode().equals(TutorialSession.start().encode()),"invalid state restarts practice safely");}
        check(TutorialSession.isValidSave("T1|3|2|ACTIVE|64"),"manual typed gate save accepted");
        check(!TutorialSession.isValidSave("T1|3|2|ACTIVE|65"),"wrong gate history rejected");
    }
    private static void saveFailures(){
        for(boolean memoryFirst:new boolean[]{false,true}){
            Ctx c=new Ctx();GameStore store=new GameStore(c);TutorialSession t=TutorialSession.start();store.saveTutorialState(t.encode());String old=t.encode();play(t);String next=t.encode();c.save().fail(memoryFirst);
            boolean threw=false;try{store.saveTutorialState(next);}catch(IllegalStateException expected){threw=true;}
            check(threw&&c.save().disk.get("tutorial_v18_state").equals(old),"failed phase commit never writes partial disk state");
            GameStore other=new GameStore(c);check(other.tutorialState().equals(next),"second handle flushes exact pending phase");
            same(t,TutorialSession.restore(new GameStore(c.reboot()).tutorialState()),"phase survives recovered save");
            Ctx cold=new Ctx();GameStore s=new GameStore(cold);s.saveTutorialState(old);cold.save().fail(memoryFirst);try{s.saveTutorialState(next);}catch(IllegalStateException expected){}
            check(new GameStore(cold.reboot()).tutorialState().equals(old),"process death immediately after failure keeps last durable phase");
        }
    }
    private static void progressionIsolation(){
        Ctx c=new Ctx();GameStore s=new GameStore(c);Map<String,?> baseline=c.save().getAll();TutorialSession t=TutorialSession.start();
        while(!t.complete()){play(t);s.saveTutorialState(t.encode());}
        Map<String,Object> after=new HashMap<>(c.save().memory);after.remove("tutorial_v18_state");
        check(after.equals(baseline),"all practice actions only save onboarding state, never real stock/currency/campaign/event/daily");
        check(s.completedLevels()==0&&s.unlockedLevel()==1&&s.totalStars()==0&&s.dailyLoginCount()==0&&s.boosterStock(Catalog.HINT)==3&&s.coins()==180,"explicit real progress and economy untouched");
        check(s.claimTutorialReward()==GameStore.TutorialRewardStatus.GRANTED&&s.coins()==280&&s.boosterStock(Catalog.HINT)==5,"exact 100 earned coins plus two hints");
        check(s.completedLevels()==0&&s.dailyCompletedCount()==0&&s.weeklyCompletedCount()==0&&s.dailyLoginCount()==0,"gift does not complete campaign or daily/weekly goals");
        check(s.claimTutorialReward()==GameStore.TutorialRewardStatus.ALREADY_CLAIMED,"double collection cannot add more");
        GameStore asGoogle=new GameStore(c,new GameStore.ExternalOwnership(){public boolean ownsCharacter(String id){return false;}public boolean ownsSkin(String id){return false;}});
        check(asGoogle.claimTutorialReward()==GameStore.TutorialRewardStatus.ALREADY_CLAIMED&&asGoogle.coins()==280,"changing identity/ownership does not create a new local reward");
        GameStore rebooted=new GameStore(c.reboot());check(rebooted.tutorialRewardClaimed()&&rebooted.claimTutorialReward()==GameStore.TutorialRewardStatus.ALREADY_CLAIMED&&rebooted.boosterStock(Catalog.HINT)==5,"reward receipt survives cold process recreation");
        t.replay();s.saveTutorialState(t.encode());check(!t.complete()&&s.claimTutorialReward()==GameStore.TutorialRewardStatus.ALREADY_CLAIMED,"replay never resets installation receipt");
    }
    private static void rewardFailures(){
        for(boolean memoryFirst:new boolean[]{false,true}){
            Ctx c=new Ctx();GameStore s=new GameStore(c);s.saveTutorialState(finished().encode());c.save().fail(memoryFirst);
            boolean threw=false;try{s.claimTutorialReward();}catch(IllegalStateException expected){threw=true;}
            check(threw&&!c.save().disk.containsKey("tutorial_v18_reward_claimed")&&c.save().disk.get("coins").equals(180)&&c.save().disk.get("booster_hint").equals(3),"failed reward commit has no partial disk credit or receipt");
            GameStore other=new GameStore(c);check(other.claimTutorialReward()==GameStore.TutorialRewardStatus.ALREADY_CLAIMED,"pending exact reward flushes instead of a second credit");
            GameStore durable=new GameStore(c.reboot());check(durable.coins()==280&&durable.boosterStock(Catalog.HINT)==5&&durable.tutorialRewardClaimed(),"retry saves receipt and both exact grants together");
            Ctx crash=new Ctx();GameStore before=new GameStore(crash);before.saveTutorialState(finished().encode());crash.save().fail(memoryFirst);try{before.claimTutorialReward();}catch(IllegalStateException expected){}
            GameStore cold=new GameStore(crash.reboot());check(!cold.tutorialRewardClaimed()&&cold.claimTutorialReward()==GameStore.TutorialRewardStatus.GRANTED&&cold.coins()==280&&cold.boosterStock(Catalog.HINT)==5,"cold retry from old disk grants exactly once");
        }
        Ctx c=new Ctx();GameStore s=new GameStore(c);check(s.claimTutorialReward()==GameStore.TutorialRewardStatus.NOT_COMPLETE,"no early gift");
        TutorialSession skipped=TutorialSession.start();skipped.skip();s.saveTutorialState(skipped.encode());check(s.claimTutorialReward()==GameStore.TutorialRewardStatus.NOT_COMPLETE,"skip never counts as completion");
        s.saveTutorialState(finished().encode());c.save().set("booster_hint",998);
        check(s.claimTutorialReward()==GameStore.TutorialRewardStatus.STORAGE_FULL&&!s.tutorialRewardClaimed()&&s.coins()==180&&s.boosterStock(Catalog.HINT)==998,"full stock keeps entire reward pending");
        c.save().set("booster_hint",997);check(s.claimTutorialReward()==GameStore.TutorialRewardStatus.GRANTED&&s.boosterStock(Catalog.HINT)==999,"exact two hints fit cap without clipping");
        Ctx coins=new Ctx();GameStore cap=new GameStore(coins);cap.saveTutorialState(finished().encode());coins.save().set("coins",1_999_999_950);
        check(cap.claimTutorialReward()==GameStore.TutorialRewardStatus.STORAGE_FULL&&!cap.tutorialRewardClaimed()&&cap.boosterStock(Catalog.HINT)==3,"coin cap cannot partially burn reward");
        coins.save().set("coins",1_999_999_900);check(cap.claimTutorialReward()==GameStore.TutorialRewardStatus.GRANTED&&cap.coins()==2_000_000_000,"exact coin-cap boundary");
    }
    private static void autoStart(){
        Ctx c=new Ctx();GameStore s=new GameStore(c);check(s.shouldAutoStartTutorial(),"brand new profile may start interactive practice");
        TutorialSession t=TutorialSession.start();s.saveTutorialState(t.encode());check(s.shouldAutoStartTutorial(),"started active tutorial resumes after activity recreation");
        t.skip();s.saveTutorialState(t.encode());check(!s.shouldAutoStartTutorial(),"skip is durable and never forced");
        t.resume();s.saveTutorialState(t.encode());check(new GameStore(c.reboot()).shouldAutoStartTutorial(),"cold resume preserves active choice");
        s.saveTutorialState(finished().encode());check(!s.shouldAutoStartTutorial(),"completed practice does not reopen automatically");
        Ctx old=new Ctx();old.save().set("initialized",true);old.save().set("coins",640);old.save().set("booster_hint",8);GameStore veteran=new GameStore(old);
        check(!veteran.shouldAutoStartTutorial()&&veteran.coins()==640&&veteran.boosterStock(Catalog.HINT)==8,"existing initialized profile is optional, including players with zero recorded clears");
        veteran.saveTutorialState(TutorialSession.start().encode());check(veteran.shouldAutoStartTutorial(),"veteran's voluntary active lesson can resume");
        Ctx progressed=new Ctx();GameStore past=new GameStore(progressed);past.completeLevel(1,3);check(!past.shouldAutoStartTutorial(),"campaign player is not interrupted by first-start offer");
        boolean threw=false;try{past.saveTutorialState("fake");}catch(IllegalArgumentException expected){threw=true;}check(threw,"store rejects malformed save envelopes");
    }
    private static void concurrentReward() throws Exception {
        Ctx c=new Ctx();new GameStore(c).saveTutorialState(finished().encode());GameStore a=new GameStore(c),b=new GameStore(c);CountDownLatch gate=new CountDownLatch(1);ExecutorService workers=Executors.newFixedThreadPool(2);
        try{Future<GameStore.TutorialRewardStatus> x=workers.submit(()->{gate.await();return a.claimTutorialReward();}),y=workers.submit(()->{gate.await();return b.claimTutorialReward();});gate.countDown();List<GameStore.TutorialRewardStatus> results=Arrays.asList(x.get(5,TimeUnit.SECONDS),y.get(5,TimeUnit.SECONDS));
            check(Collections.frequency(results,GameStore.TutorialRewardStatus.GRANTED)==1&&Collections.frequency(results,GameStore.TutorialRewardStatus.ALREADY_CLAIMED)==1,"concurrent separate handles grant exactly once");
            check(a.coins()==280&&b.boosterStock(Catalog.HINT)==5,"concurrent exact durable amounts");
        }finally{workers.shutdownNow();}
    }
    private static void localization(){
        for(String language:new String[]{"en","fr","de","es","pt","it"})for(String key:TutorialStrings.keys())check(!TutorialStrings.get(language,key,1,7).trim().isEmpty(),"nonempty explicit translation "+language+"/"+key);
        check(TutorialStrings.get("fr-FR","progress",2,7).equals("Leçon 2 / 7"),"French locale and formatting");
        check(TutorialStrings.get("xx","title_gate").equals(TutorialStrings.get("en","title_gate")),"unsupported language explicit English fallback");
        boolean threw=false;try{TutorialStrings.get("fr","missing");}catch(IllegalArgumentException expected){threw=true;}check(threw,"unknown localization cannot silently show key");
    }
    public static void main(String[]args)throws Exception{lessons();skipReplayAndRecreate();invalidSaves();saveFailures();progressionIsolation();rewardFailures();autoStart();concurrentReward();localization();System.out.println("PASS "+checks+" tutorial1.8 session, seven real boards, typed-history recreation, skip/replay, six-language and atomic one-time reward checks");System.out.println("SCOPE: pure JVM session and real GameStore with deterministic Android preference doubles; installed touch/rotation/rendering require separate SDK UI checks.");}
}
