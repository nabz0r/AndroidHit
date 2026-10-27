import android.content.*;
import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.engine.*;
import com.androidhit.lumadrift.onboarding.*;
import java.nio.file.*;
import java.util.*;

/** Reproducible synthetic play policies on the real engine, never human retention data. */
public final class NewPlayerV19Audit {
    static long checks,profileAttempts,profileInterruptions; static Path output;
    static void check(boolean pass,String reason){checks++;if(!pass)throw new AssertionError(reason);}
    static final class Prefs implements SharedPreferences {
        final Map<String,Object> values=new HashMap<>();
        public Map<String,?> getAll(){return new HashMap<>(values);}
        public boolean contains(String k){return values.containsKey(k);}
        public int getInt(String k,int d){return (Integer)values.getOrDefault(k,d);}
        public boolean getBoolean(String k,boolean d){return (Boolean)values.getOrDefault(k,d);}
        public String getString(String k,String d){return (String)values.getOrDefault(k,d);}
        public Editor edit(){return new Editor(){final Map<String,Object> pending=new HashMap<>();
            public Editor putInt(String k,int v){pending.put(k,v);return this;}
            public Editor putBoolean(String k,boolean v){pending.put(k,v);return this;}
            public Editor putString(String k,String v){pending.put(k,v);return this;}
            public boolean commit(){values.putAll(pending);return true;}};}
    }
    static final class Ctx extends Context {
        final Map<String,Prefs> files=new HashMap<>();
        public SharedPreferences getSharedPreferences(String n,int m){return files.computeIfAbsent(n,k->new Prefs());}
        Ctx reboot(){Ctx c=new Ctx();files.forEach((n,p)->{Prefs q=new Prefs();q.values.putAll(p.values);c.files.put(n,q);});return c;}
    }
    static final class Clock implements GameStore.Clock,MetaProgress.Clock {
        long time=1793059200000L; public long now(){return time;}
    }
    enum Policy { OPTIMAL_CONTROL, LEGAL_RANDOM, ONE_STEP_PREVIEW, EXPLORATORY_TOUCH, PAUSE_AND_RESUME, GUIDED_RECOVERY }
    static final double[] ERROR_RATES={0.05,0.15,0.30};
    static final int SEEDS=16;
    /** Public application policies are called when present; the 1.8 baseline has neither API. */
    static final class Api {
        static java.lang.reflect.Method stars,limit,trial,trialAvailable,preview;
        static {try{Class<?> type=Class.forName("com.androidhit.lumadrift.engine.JourneyExperience");stars=type.getMethod("stars",MoveBudget.class);limit=type.getMethod("threeStarLimit",MoveBudget.class);}catch(ClassNotFoundException|NoSuchMethodException baseline){}
            try{trial=GameStore.class.getMethod("claimPowerTrial",String.class);trialAvailable=GameStore.class.getMethod("powerTrialAvailable",String.class);preview=GameStore.class.getMethod("nextMilestoneLootId");}catch(NoSuchMethodException baseline){}
        }
        static int stars(MoveBudget b,int hints){try{return stars==null?BaselineV19Score.stars(hints,b):(Integer)stars.invoke(null,b);}catch(ReflectiveOperationException e){throw new AssertionError(e);}}
        static int limit(MoveBudget b){try{return limit==null?-1:(Integer)limit.invoke(null,b);}catch(ReflectiveOperationException e){throw new AssertionError(e);}}
        static String trials(GameStore s){StringJoiner ids=new StringJoiner(";");if(trial==null)return "";try{for(Catalog.BoosterItem item:Catalog.BOOSTERS)if((Boolean)trialAvailable.invoke(s,item.id)){
                String status=trial.invoke(s,item.id).toString();check(status.equals("GRANTED"),"available trial fits fresh profile stock");ids.add(item.id);
                check(trial.invoke(s,item.id).toString().equals("ALREADY_CLAIMED"),"trial cannot repeat");
            }return ids.toString();}catch(ReflectiveOperationException e){throw new AssertionError(e);}}
        static String preview(GameStore s){try{return preview==null?"":(String)preview.invoke(s);}catch(ReflectiveOperationException e){throw new AssertionError(e);}}
    }
    static List<Integer> legal(Board b,boolean allGates){
        ArrayList<Integer> actions=new ArrayList<>();
        for(Tile t:b.remainingTiles())if(b.canMove(t.id))actions.add(t.id);
        if(allGates||actions.isEmpty())for(Direction d:Direction.values())if(!b.isGateOpen(d)){
            boolean needed=false;for(Tile t:b.remainingTiles())if(t.direction==d&&b.blockingTile(t.id)<0&&b.blockingBarrier(t.id)==null)needed=true;
            if(allGates||needed)actions.add(Board.gateAction(d));
        }
        return actions;
    }
    static int greedy(Board b,Random rng){
        List<Integer> actions=legal(b,false);check(!actions.isEmpty(),"recoverable board has a useful action");
        int best=Integer.MIN_VALUE;ArrayList<Integer> tied=new ArrayList<>();int[] saved=b.history();
        for(int action:actions){check(b.performAction(action).success,"preview candidate legal");int score=20*(Board.isGateAction(action)?0:1)+b.legalMoves().size();
            if(!Board.isGateAction(action)&&b.level.isKey(action))score+=2;
            if(score>best){best=score;tied.clear();}if(score==best)tied.add(action);
            check(b.undo()&&Arrays.equals(saved,b.history()),"one-step preview restores exact typed history");
        }
        return tied.get(rng.nextInt(tied.size()));
    }
    static final class Run {boolean solved;int blocked,undos,resumes,used,attempts,excess,remaining,remainingNeed,hints,stars,threeStarLimit;}
    static Run run(Level level,Policy policy,long seed,double error,boolean recover){
        Board board=new Board(level);MoveBudget budget=MoveBudget.start(0,level);Run run=new Run();Random rng=new Random(seed);
        int guard=level.minimumActions*8+80;
        while(!board.isSolved()&&budget.canAct()&&guard-->0){
            int action;
            if(policy==Policy.OPTIMAL_CONTROL)action=board.solve()[0];
            else if(policy==Policy.ONE_STEP_PREVIEW||policy==Policy.GUIDED_RECOVERY)action=greedy(board,rng);
            else {List<Integer> legal=legal(board,false);check(!legal.isEmpty(),"useful legal action exists");action=legal.get(rng.nextInt(legal.size()));}
            if((policy==Policy.EXPLORATORY_TOUCH||policy==Policy.PAUSE_AND_RESUME)&&rng.nextDouble()<error){
                List<Tile> tiles=board.remainingTiles();action=tiles.get(rng.nextInt(tiles.size())).id;
            }
            // Controller semantics: a present blocked tile attempt costs one; gate actions are validated first.
            boolean wasLegal=board.canPerformAction(action);check(budget.consumeAction(),"matching action budget available");
            Board.MoveResult result=board.performAction(action);check(result.success==wasLegal,"runtime result equals physical legality");
            run.attempts++;if(!result.success)run.blocked++;
            if((policy==Policy.PAUSE_AND_RESUME&&run.attempts%4==0)||run.attempts==2){
                int[] saved=board.history();Board restored=new Board(level);check(restored.restoreHistory(saved),"pause typed history restores");
                MoveBudget next=MoveBudget.restoreVersion(budget.policyVersion(),0,level,budget.used(),budget.extensions(),budget.extraActions(),restored.moves(),run.undos);
                check(next!=null&&next.remaining()==budget.remaining()&&restored.gateMask()==board.gateMask()&&restored.remainingMask()==board.remainingMask(),"pause restores exact board and budget");
                board=restored;budget=next;run.resumes++;
            }
        }
        check(guard>0,"finite attempt bound");
        if(recover&&!board.isSolved()){
            run.hints++;
            // An explicit free hint/Undo recovery branch; assistance rate is not an observed human parameter.
            while(board.canUndo()&&budget.remaining()<board.minimumActionsToFinish()){
                check(board.undo()&&budget.refundAction(),"free Undo refund at exhausted/short budget");run.undos++;
            }
            if(budget.remaining()>=board.minimumActionsToFinish()){
                for(int action:board.solve()){check(budget.consumeAction()&&board.performAction(action).success,"recovery hint plan follows actual state");run.attempts++;}
            }
        }
        run.solved=board.isSolved();run.used=budget.used();run.remaining=board.remainingCount();run.remainingNeed=board.minimumActionsToFinish();
        run.excess=budget.used()+run.remainingNeed-level.minimumActions;
        run.stars=run.solved?Api.stars(budget,run.hints):0;run.threeStarLimit=Api.limit(budget);
        check(run.remainingNeed>=0,"all resulting strategy states remain logically solvable");
        if(run.solved)check(budget.remaining()>=0,"completed attempt stays inside budget");
        return run;
    }
    static void metrics()throws Exception{
        StringBuilder csv=new StringBuilder("level,pieces,board_size,minimum,base,allowance,required_gate_opens,keys,seals,switches,paired_switches,decision_points,forced_optimal,max_one_choice_penalty,dependency_depth,structural_score,difficulty,beat,initial_legal_exits,initial_closed_gates,certificate_gate_opens,certificate_max_legal_actions\n");
        for(int n=1;n<=100;n++){
            Level l=StrategyCatalog.get(n);ProgressionInfo p=StrategyCatalog.info(n,false);MoveBudget budget=MoveBudget.start(0,l);Board b=new Board(l);
            int switches=0,paired=0;for(Tile t:l.tiles){if(t.switchMask!=0)switches++;if(Integer.bitCount(t.switchMask)>1)paired++;}
            int initial=b.legalMoves().size(),closed=4-Integer.bitCount(l.initialGateMask),gates=0,max=0;
            for(int action:l.actionSolution){max=Math.max(max,legal(b,true).size());if(Board.isGateAction(action))gates++;check(budget.consumeAction()&&b.performAction(action).success,"certified action fits original budget");}
            check(b.isSolved()&&budget.used()==l.minimumActions,"all 100 certified clears finish without goods");
            csv.append(String.format(Locale.ROOT,"%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%s,%s,%d,%d,%d,%d%n",n,l.tiles.size(),l.size,l.minimumActions,budget.base(),budget.mistakeAllowance(),p.requiredGateOpens,p.keyCount,p.barrierCount,switches,paired,p.decisionPoints,p.forcedOptimalActions,p.maxOneChoicePenalty,p.dependencyDepth,p.structuralScore,p.difficulty,p.beat,initial,closed,gates,max));
        }
        Files.writeString(output.resolve("levels-1-100.csv"),csv);
    }
    static void simulations()throws Exception{
        StringBuilder csv=new StringBuilder("policy,error_hypothesis,seed,level,solved,blocked,undo,resumes,used,attempts,excess_actions,remaining_pieces,remaining_need,stars,three_star_limit\n");
        StringBuilder totals=new StringBuilder("policy,error_hypothesis,attempts,solved,budget_exhausted,blocked,undos,resumes,consumed_actions\n");
        for(Policy policy:Policy.values())for(double error:(policy==Policy.EXPLORATORY_TOUCH||policy==Policy.PAUSE_AND_RESUME?ERROR_RATES:new double[]{0})){
            long count=0,solved=0,blocked=0,undos=0,resumes=0,used=0;
            for(int s=0;s<SEEDS;s++)for(int n=1;n<=100;n++){
                // Common random numbers allow paired comparisons between revisions.
                long seed=0x4C554D4100190000L+s*1009L+n*7919L;Run r=run(StrategyCatalog.get(n),policy,seed,error,policy==Policy.GUIDED_RECOVERY);
                count++;if(r.solved)solved++;blocked+=r.blocked;undos+=r.undos;resumes+=r.resumes;used+=r.used;
                csv.append(policy).append(',').append(error).append(',').append(seed).append(',').append(n).append(',').append(r.solved).append(',').append(r.blocked).append(',').append(r.undos).append(',').append(r.resumes).append(',').append(r.used).append(',').append(r.attempts).append(',').append(r.excess).append(',').append(r.remaining).append(',').append(r.remainingNeed).append(',').append(r.stars).append(',').append(r.threeStarLimit).append('\n');
            }
            totals.append(policy).append(',').append(error).append(',').append(count).append(',').append(solved).append(',').append(count-solved).append(',').append(blocked).append(',').append(undos).append(',').append(resumes).append(',').append(used).append('\n');
            System.out.println(policy+" error="+error+" solved="+solved+"/"+count);
        }
        Files.writeString(output.resolve("synthetic-attempts.csv"),csv);Files.writeString(output.resolve("synthetic-policy-totals.csv"),totals);
    }
    static TutorialSession.Action play(TutorialSession t){switch(t.requiredAction()){
        case MOVE:case TRY_BLOCKED:return t.move(t.expectedTileId());case PREVIEW_SWITCH:return t.previewSwitch(t.expectedTileId());case PREVIEW_GATE:return t.previewGate(t.expectedGate());case OPEN_GATE:return t.openGate(t.expectedGate());case UNDO:return t.undo();case HINT:return t.hint();case DONE:return t.advance();default:throw new AssertionError("unexpected tutorial phase");}}
    static void rewards()throws Exception{
        StringBuilder csv=new StringBuilder("stars,clears,unlocked,day,level_reward,mission_reward,weekly_reward,login_reward,coins,xp,rank,milestone_power,next_power_milestone,hint,vision,flow,shield,focus,trail,unlocked_powers,cosmetics_unlocked,gear_unlocked,trial_claims,next_loot_preview\n");
        StringBuilder tutorial=new StringBuilder("step,lesson,phase,required_action,board_actions,remaining_pieces,serialized_bytes\n");
        TutorialSession t=TutorialSession.start();int steps=0;while(!t.complete()){
            tutorial.append(steps).append(',').append(t.lessonIndex()+1).append(',').append(t.phase()).append(',').append(t.requiredAction()).append(',').append(t.board().moves()).append(',').append(t.board().remainingCount()).append(',').append(t.encode().length()).append('\n');
            check(play(t).accepted,"expected real tutorial action accepted");steps++;check(steps<40,"tutorial bounded");
        }
        check(steps==27,"baseline seven-lesson tutorial uses 27 real actions/transitions");Files.writeString(output.resolve("tutorial-steps.csv"),tutorial);
        for(int stars=1;stars<=3;stars++){
            Ctx context=new Ctx();Clock clock=new Clock();GameStore store=new GameStore(context,null,clock);MetaProgress meta=new MetaProgress(context,clock);
            store.saveTutorialState(t.encode());check(store.claimTutorialReward()==GameStore.TutorialRewardStatus.GRANTED,"tutorial gift actual GameStore");
            check(store.claimTutorialReward()==GameStore.TutorialRewardStatus.ALREADY_CLAIMED,"tutorial reward cannot repeat");
            String initialTrials=Api.trials(store);
            csv.append(stars).append(",0,1,").append(store.dailyId()).append(",0,0,0,0,").append(store.coins()).append(',').append(meta.xp()).append(',').append(meta.rank()).append(",,").append(store.nextLootMilestone());
            for(Catalog.BoosterItem item:Catalog.BOOSTERS)csv.append(',').append(store.boosterStock(item.id));
            csv.append(",hint,,,").append(initialTrials).append(',').append(Api.preview(store)).append('\n');
            for(int n=1;n<=100;n++){
                clock.time=1793059200000L+((n-1)/10)*86400000L;int login=store.claimDailyLogin();
                int reward=store.completeLevel(n,stars);reward+=store.claimAvatarVictoryBonus(0,n,"");meta.recordWin(0,n,stars,"");
                check(store.completeLevel(n,stars)==0,"identical star replay cannot farm currency");
                int mission=0,weekly=0;for(int m=0;m<3;m++){mission+=meta.claimMission(m,store);weekly+=meta.claimWeeklyMission(m,store);}
                String power=store.claimMilestoneLoot();List<String> cosmetics=store.claimCosmeticUnlocks(),gear=store.claimAvatarGearUnlocks();
                String trials=Api.trials(store);
                StringJoiner unlocked=new StringJoiner(";");for(Catalog.BoosterItem item:Catalog.BOOSTERS)if(store.boosterUnlocked(item.id))unlocked.add(item.id);
                csv.append(stars).append(',').append(n).append(',').append(store.unlockedLevel()).append(',').append(store.dailyId()).append(',').append(reward).append(',').append(mission).append(',').append(weekly).append(',').append(login).append(',').append(store.coins()).append(',').append(meta.xp()).append(',').append(meta.rank()).append(',').append(power).append(',').append(store.nextLootMilestone());
                for(Catalog.BoosterItem item:Catalog.BOOSTERS)csv.append(',').append(store.boosterStock(item.id));csv.append(',').append(unlocked).append(',').append(String.join(";",cosmetics)).append(',').append(String.join(";",gear)).append(',').append(trials).append(',').append(Api.preview(store)).append('\n');
                if(n%7==0){Ctx reboot=context.reboot();GameStore next=new GameStore(reboot,null,clock);MetaProgress nextMeta=new MetaProgress(reboot,clock);check(next.coins()==store.coins()&&next.completedLevels()==n&&nextMeta.xp()==meta.xp(),"actual progression persists across cold preference reconstruction");context=reboot;store=next;meta=nextMeta;}
            }
        }
        Files.writeString(output.resolve("actual-reward-ledger.csv"),csv);
    }
    static final class Profile {
        String id,tutorial,anchors;double offPlan,probe,sessionMin,sessionMax,latencyMin,latencyMax;int helpAfter,retries;
        Profile(String[] v){id=v[0];offPlan=Double.parseDouble(v[1]);probe=Double.parseDouble(v[2]);helpAfter=Integer.parseInt(v[3]);sessionMin=Double.parseDouble(v[4]);sessionMax=Double.parseDouble(v[5]);latencyMin=Double.parseDouble(v[6]);latencyMax=Double.parseDouble(v[7]);retries=Integer.parseInt(v[8]);tutorial=v[9];anchors=v[10];}
    }
    static void profiles()throws Exception{
        StringBuilder csv=new StringBuilder("profile,sensitivity,seed,level,attempt,solved,blocked,undo,help_units,bought_hint_packs,resumes,actions,stars,coins,hint_stock,trial_claims,modelled_action_seconds_min,modelled_action_seconds_max\n");
        StringBuilder totals=new StringBuilder("profile,sensitivity,seed,cleared,attempts,restarts,blocked,undo,help_units,bought_hint_packs,resumes,action_events,coins,hint_stock,modelled_action_seconds_min,modelled_action_seconds_max,social_menu_reads,anchors\n");
        List<String> lines=Files.readAllLines(output.resolve("profile-inputs.tsv"));
        for(int line=1;line<lines.size();line++){
            Profile profile=new Profile(lines.get(line).split("\t",-1));
            for(double sensitivity:new double[]{0.5,1.0,1.5})for(int seedIndex=0;seedIndex<4;seedIndex++){
                long seed=0x190027A0L+seedIndex*1009;Random rng=new Random(seed);Ctx context=new Ctx();Clock clock=new Clock();GameStore store=new GameStore(context,null,clock);MetaProgress meta=new MetaProgress(context,clock);
                TutorialSession tutorial=TutorialSession.start();int tutorialEvents=0;
                if(profile.tutorial.equals("skip"))tutorial.skip();
                else {
                    if(profile.tutorial.equals("skip_then_resume")){tutorial.skip();store.saveTutorialState(tutorial.encode());tutorial=TutorialSession.restore(store.tutorialState());tutorial.resume();}
                    while(!tutorial.complete()){
                        check(play(tutorial).accepted,"profile actual tutorial action");store.saveTutorialState(tutorial.encode());tutorialEvents++;
                        if(profile.tutorial.equals("interrupt_resume")&&tutorialEvents%3==0){context=context.reboot();store=new GameStore(context,null,clock);meta=new MetaProgress(context,clock);tutorial=TutorialSession.restore(store.tutorialState());}
                    }
                    check(store.claimTutorialReward()==GameStore.TutorialRewardStatus.GRANTED,"profile practice gift real ledger");
                }
                store.saveTutorialState(tutorial.encode());Api.trials(store);
                int attempts=0,restarts=0,cleared=0,blocked=0,undos=0,helps=0,packs=0,resumes=0,events=tutorialEvents,socialReads=0;
                double latency=(profile.latencyMin+profile.latencyMax)/2;
                int sessionActions=Math.max(1,(int)Math.floor(((profile.sessionMin+profile.sessionMax)/2)*60/latency));
                for(int n=1;n<=100;n++){
                    boolean solved=false;clock.time=1793059200000L+((n-1)/10)*86400000L;store.claimDailyLogin();
                    for(int attempt=0;attempt<=profile.retries&&!solved;attempt++){
                        attempts++;profileAttempts++;if(attempt>0)restarts++;Level level=StrategyCatalog.get(n);Board board=new Board(level);MoveBudget budget=MoveBudget.start(0,level);
                        int localBlocked=0,localUndo=0,localHelps=0,localPacks=0,localResumes=0,localActions=0,unproductive=0,guard=level.minimumActions*8+80;boolean followsHint=false;
                        while(!board.isSolved()&&budget.canAct()&&guard-->0){
                            int[] plan=board.solve();int action=plan[0];
                            if(!followsHint&&rng.nextDouble()<Math.min(0.9,profile.offPlan*sensitivity)){
                                List<Integer> alternatives=legal(board,false);action=alternatives.get(rng.nextInt(alternatives.size()));
                            }
                            if(!followsHint&&rng.nextDouble()<Math.min(0.9,profile.probe*sensitivity)){
                                List<Tile> tiles=board.remainingTiles();action=tiles.get(rng.nextInt(tiles.size())).id;
                            }
                            check(budget.consumeAction(),"profile consumes available budget");Board.MoveResult move=board.performAction(action);localActions++;events++;
                            if(!move.success){localBlocked++;unproductive++;}else if(action!=plan[0])unproductive++;
                            if(!followsHint&&unproductive>=profile.helpAfter){
                                if(store.boosterStock(Catalog.HINT)==0&&store.purchaseBooster(Catalog.HINT))localPacks++;
                                String request="audit:"+profile.id+":"+Math.round(sensitivity*100)+":"+seedIndex+":"+n+":"+attempt;
                                check(request.matches("[a-zA-Z0-9_:-]{1,160}"),"profile hint request uses the real public receipt format");
                                if(store.reserveBoosterUse(Catalog.HINT,request)){
                                    check(board.solve().length>0,"reserved stock hint targets an actual remaining action");check(store.completeBoosterUse(Catalog.HINT,request),"actual hint stock debit finalized");localHelps++;followsHint=true;
                                }
                            }
                            if(budget.remaining()<board.minimumActionsToFinish()&&board.canUndo()&&followsHint){
                                while(board.canUndo()&&budget.remaining()<board.minimumActionsToFinish()){
                                    check(board.undo()&&budget.refundAction(),"profile free reversal restores productive action");localUndo++;events++;
                                }
                                // Blocked attempts remain charged. Full rewind cannot manufacture their cost back.
                                if(budget.remaining()<board.minimumActionsToFinish())break;
                            }
                            if(events%sessionActions==0){
                                Board restored=new Board(level);check(restored.restoreHistory(board.history()),"profile interruption real board replay");
                                MoveBudget restoredBudget=MoveBudget.restoreVersion(3,0,level,budget.used(),0,0,restored.moves(),localUndo);check(restoredBudget!=null,"profile interruption budget restore");
                                board=restored;budget=restoredBudget;context=context.reboot();GameStore recovered=new GameStore(context,null,clock);MetaProgress recoveredMeta=new MetaProgress(context,clock);
                                check(recovered.coins()==store.coins()&&recovered.boosterStock(Catalog.HINT)==store.boosterStock(Catalog.HINT),"profile interruption ledger exact");store=recovered;meta=recoveredMeta;localResumes++;
                            }
                        }
                        check(guard>0,"profile action bound");solved=board.isSolved();int stars=solved?Api.stars(budget,localHelps):0;String trials="";
                        if(solved){cleared++;store.completeLevel(n,stars);store.claimAvatarVictoryBonus(0,n,"");meta.recordWin(0,n,stars,"");store.claimMilestoneLoot();
                            for(int m=0;m<3;m++){meta.claimMission(m,store);meta.claimWeeklyMission(m,store);}store.claimCosmeticUnlocks();store.claimAvatarGearUnlocks();trials=Api.trials(store);
                            if(profile.id.equals("social_collector")&&n%5==0){for(CosmeticCatalog.Item item:CosmeticCatalog.ITEMS)store.ownsCosmetic(item.id);socialReads++;}
                        }
                        blocked+=localBlocked;undos+=localUndo;helps+=localHelps;packs+=localPacks;resumes+=localResumes;profileInterruptions+=localResumes;
                        csv.append(profile.id).append(',').append(sensitivity).append(',').append(seed).append(',').append(n).append(',').append(attempt).append(',').append(solved).append(',').append(localBlocked).append(',').append(localUndo).append(',').append(localHelps).append(',').append(localPacks).append(',').append(localResumes).append(',').append(localActions).append(',').append(stars).append(',').append(store.coins()).append(',').append(store.boosterStock(Catalog.HINT)).append(',').append(trials).append(',').append(localActions*profile.latencyMin).append(',').append(localActions*profile.latencyMax).append('\n');
                    }
                    if(!solved)break;
                }
                totals.append(profile.id).append(',').append(sensitivity).append(',').append(seed).append(',').append(cleared).append(',').append(attempts).append(',').append(restarts).append(',').append(blocked).append(',').append(undos).append(',').append(helps).append(',').append(packs).append(',').append(resumes).append(',').append(events).append(',').append(store.coins()).append(',').append(store.boosterStock(Catalog.HINT)).append(',').append(events*profile.latencyMin).append(',').append(events*profile.latencyMax).append(',').append(socialReads).append(',').append(profile.anchors).append('\n');
            }
        }
        Files.writeString(output.resolve("profile-journey-attempts.csv"),csv);Files.writeString(output.resolve("profile-journey-totals.csv"),totals);
    }
    public static void main(String[] args)throws Exception{
        output=Path.of(args[0]);Files.createDirectories(output);TimeZone.setDefault(TimeZone.getTimeZone("UTC"));long start=System.nanoTime();
        metrics();simulations();rewards();profiles();double elapsed=(System.nanoTime()-start)/1e9;
        String summary="{\n  \"passed\": true,\n  \"label\": \""+args[1]+"\",\n  \"assertions\": "+checks+",\n  \"levels\": 100,\n  \"synthetic_independent_attempts\": 16000,\n  \"synthetic_sequential_journeys\": 72,\n  \"sequential_level_attempts\": "+profileAttempts+",\n  \"sequential_interruptions_restored\": "+profileInterruptions+",\n  \"seeds_per_level_policy\": "+SEEDS+",\n  \"elapsed_host_seconds\": "+elapsed+",\n  \"scope\": \"Real Board, MoveBudget, TutorialSession, GameStore and MetaProgress; JVM preference doubles; synthetic policies and error rates are assumptions, not people or retention forecasts.\"\n}\n";
        Files.writeString(output.resolve("summary.json"),summary);System.out.println("PASS "+checks+" checks in "+elapsed+" host seconds");
    }
}
