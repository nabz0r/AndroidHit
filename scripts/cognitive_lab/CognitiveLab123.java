package cognitive_lab;

import com.androidhit.lumadrift.data.EconomyCurve121;
import com.androidhit.lumadrift.engine.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Versioned, limited-perception 1.23 study. Historical runners and data remain separate. */
public final class CognitiveLab123 {
    static final String VERSION = "1.23-native-journey-no-preparation-1";
    static final long SEED = 2026103002L;
    static final String[] SCENARIOS = {"manual_native_123", "cause_hidden_123", "loaned_mixed_123", "energy_unlimited_123"};

    static final class Run {
        final CognitiveLab122.Run base = new CognitiveLab122.Run();
        int directCueManual, causeVisibleManual;
        double commonKnowledge() { double sum=0; for(int i=0;i<4;i++)sum+=base.learned[i]; return sum/4; }
    }

    static final class Outputs implements AutoCloseable {
        final CognitiveLab122.Outputs core;
        final BufferedWriter states, links;
        Outputs(Path out) throws IOException {
            core = new CognitiveLab122.Outputs(out);
            states = CognitiveLab122.EnvironmentWriter.open(out,"learned-rules.csv.gz",
                    "profileId,repetition,scenario,directCueFollowedManualExits,manualExitsWithCauseVisible,commonRuleMean,gate,path,switch,barrier,unusedShell");
            links = CognitiveLab122.EnvironmentWriter.open(out,"manual-assistance-events.csv.gz",
                    "profileId,repetition,scenario,day,level,attempt,step,pieceId,directCueFollow,cueSource,visibleCauseRule,decisionReason");
        }
        void finish(Run run, Environment123 env, int day, String outcome) throws IOException {
            if(outcome.equals("horizon_censored") && (env.budget.isExhausted() || env.board.isSolved()))
                throw new AssertionError("Terminal state cannot be horizon-censored");
            core.finish(run.base,env,day,outcome);
        }
        void state(Run run) throws IOException {
            double[] k=run.base.learned;
            CognitiveLab.Outputs.csv(states,Json.obj("profileId",run.base.profile,"repetition",run.base.repetition,"scenario",run.base.scenario,
                    "directCueFollowedManualExits",run.directCueManual,"manualExitsWithCauseVisible",run.causeVisibleManual,
                    "commonRuleMean",run.commonKnowledge(),"gate",k[0],"path",k[1],"switch",k[2],"barrier",k[3],"unusedShell",k[4]));
        }
        public void close() throws IOException {core.close();states.close();links.close();}
    }

    static void finish(Outputs output, Run run, Environment123 env, int day, String outcome) throws IOException {
        if(output==null)run.base.finish(env,outcome);else output.finish(run,env,day,outcome);
    }

    static Run run(CognitiveLab.Config c, Profile p, int rep, String scenario, Outputs output) throws IOException {
        Run run=new Run(); CognitiveLab122.Run t=run.base;
        t.profile=p.id;t.repetition=rep;t.scenario=scenario;
        Agent122 policy=new Agent122(p);CognitiveAgent agent=policy.base;
        Environment.Energy energy=new Environment.Energy();Environment123 attempt=null;
        boolean unlimited=scenario.equals("energy_unlimited_123");
        int level=1,ordinal=0;double recentSuccess=0,recentReward=0;
        for(int day=0;day<c.days;day++) {
            if(day>0)agent.overnight();
            Random availability=CognitiveLab.random(c,p,rep,day,111);
            double planned=p.timeAvailabilityMinutes*(.65+.7*availability.nextDouble());
            boolean available=day==0||availability.nextDouble()<.72+.20*Math.min(1,p.timeAvailabilityMinutes/26);
            double desire=CognitiveLab.startUtility(p,agent,recentSuccess,recentReward,"current_model");
            boolean returns=day==0||(available&&CognitiveLab.random(c,p,rep,day,112).nextDouble()<CognitiveLab.sigmoid(desire*2.4));
            if(!returns) {
                String reason=available?"modeled_return_utility":"exogenous_unavailability";
                t.stop(reason);CognitiveLab122.writeDay(output==null?null:output.core,t,day,0,0,reason,0);continue;
            }
            double elapsed=0;int dayActions=0,failures=0,exhaustedToday=0;String stop="day_action_cap";
            while(dayActions<CognitiveLab.MAX_ACTIONS_PER_DAY&&level<=CognitiveLab.CAMPAIGN_CAP) {
                double clock=day*86400+18*3600+elapsed*60;energy.refresh(clock);
                if(attempt==null) {
                    if(!unlimited&&!energy.reserve(level,clock)){stop="energy_empty";break;}
                    attempt=new Environment123(level,++ordinal,day,scenario);t.attempts++;
                    t.maxLevelStarted=Math.max(t.maxLevelStarted,level);
                    if(level==43)t.starts43++;if(level==48)t.starts48++;
                }
                if(AttemptOutcome123.resolved(attempt,false)!=null)
                    throw new AssertionError("A terminal attempt survived a prior action or daily boundary");
                int beforeManual=attempt.manualExits,beforeAuto=attempt.automatedExits,beforeUndo=attempt.undos;
                int beforeRepeated=attempt.repeatedErrors,beforeCue=attempt.causalCueObservations;
                int[] beforeFamily=attempt.familyUses.clone();
                Agent122.Observation observation=attempt.observe(clock,t.coins,false);
                int actionIndex=attempt.actions;
                CognitiveAgent.Decision decision=policy.choose(observation,CognitiveLab.random(c,p,rep,day,level,attempt.ordinal,actionIndex,222));
                double seconds=CognitiveLab.duration(p,agent,decision,CognitiveLab.random(c,p,rep,day,level,attempt.ordinal,actionIndex,223));
                Environment.Result result=attempt.apply122(decision,observation,clock,seconds);
                dayActions++;elapsed+=seconds/60;t.actions++;t.coins-=result.coinCost;t.coinsSpent+=result.coinCost;
                policy.feedback(observation,decision,result.kind,result.reason,seconds);
                if(result.kind.equals("undo")) {
                    int restored=CognitiveLab.findRestored(observation.board.present,attempt.board.remainingMask());
                    if(restored>=0)agent.remember(attempt.board.remainingMask(),attempt.board.gateMask(),restored);
                }
                t.manualExits+=attempt.manualExits-beforeManual;t.automatedExits+=attempt.automatedExits-beforeAuto;
                t.undos+=attempt.undos-beforeUndo;t.repeatedErrors+=attempt.repeatedErrors-beforeRepeated;
                t.cueExposures+=attempt.causalCueObservations-beforeCue;
                for(int f=0;f<6;f++)t.familyUses[f]+=attempt.familyUses[f]-beforeFamily[f];
                if(result.kind.equals("blocked")) {
                    t.blocked++;int reason=result.reason.equals("CLOSED_GATE")?0:result.reason.equals("LOCKED_BARRIER")?2:1;
                    t.blockedReasons[reason]++;
                    if(output!=null)CognitiveLab.Outputs.csv(output.core.blocks,Json.obj("profileId",p.id,"repetition",rep,"scenario",scenario,"day",day,"level",level,"attempt",attempt.ordinal,"step",attempt.actions,"pieceId",decision.action,"presentMask",Long.toString(attempt.board.remainingMask()),"gateMask",attempt.board.gateMask(),"blockedReason",result.reason,"sameStatePieceReasonCount",attempt.sameErrorCount,"visibleCauseRule",observation.causeRule,"visibleCauseSource",observation.causeSource));
                }
                if(result.kind.equals("exit")) {
                    boolean direct=decision.reason.startsWith("follow_visible_");
                    if(direct)run.directCueManual++;
                    if(observation.causeRule>=0)run.causeVisibleManual++;
                    if(output!=null&&(direct||observation.causeRule>=0))
                        CognitiveLab.Outputs.csv(output.links,Json.obj("profileId",p.id,"repetition",rep,"scenario",scenario,"day",day,"level",level,"attempt",attempt.ordinal,"step",attempt.actions,"pieceId",decision.action,"directCueFollow",direct,"cueSource",decision.reason.contains("vision_legal")?"companion_vision":observation.board.cueSource,"visibleCauseRule",observation.causeRule,"decisionReason",decision.reason));
                }
                if(result.kind.equals("shell"))throw new AssertionError("Native 1.23 produced a shell action");
                if(result.kind.equals("focus_visible"))t.focusUses++;
                if(decision.action<=Agent122.SKILL_BASE){t.skillRequests++;if(result.kind.equals("skill_unavailable"))t.skillUnavailable++;else{t.skillUses++;if(decision.action==Agent122.COMBO)t.comboUses++;}}
                if(output!=null&&rep==0&&Integer.parseInt(p.id.substring(1))<=4&&output.core.traceCounts.getOrDefault(scenario,0)<80&&(level<=2||result.kind.equals("blocked")||decision.action<=Agent122.SKILL_BASE||observation.causeRule>=0)) {
                    output.core.traceCounts.merge(scenario,1,Integer::sum);
                    output.core.traces.add(Json.obj("profileId",p.id,"repetition",rep,"scenario",scenario,"day",day,"level",level,"attempt",attempt.ordinal,"step",attempt.actions,"visibleCue",observation.board.cueSource,"visibleCauseRule",observation.causeRule,"decision",decision.json(),"outcome",result.kind));
                }

                // Resolve the actual transition BEFORE any time/utility/day-cap stop.
                // In particular, an exhausted final-day attempt is a failure, never censoring.
                String outcome=AttemptOutcome123.resolved(attempt,result.kind.equals("give_up"));
                if("completed".equals(outcome)) {
                    t.cleared++;int reward=EconomyCurve121.campaignReward(level,attempt.stars());
                    t.coins+=reward;t.coinsEarned+=reward;recentSuccess=.7*recentSuccess+.3;
                    recentReward=.65*recentReward+.35*Math.min(1,reward/60d);agent.frustration*=.65;
                    finish(output,run,attempt,day,outcome);attempt=null;level++;
                } else if(outcome!=null) {
                    finish(output,run,attempt,day,outcome);attempt=null;failures++;
                    if(outcome.equals("budget_exhausted")) {
                        exhaustedToday++;agent.frustration=Math.min(2,agent.frustration+.16*p.frustrationSensitivity);recentSuccess=-1;
                    } else {recentSuccess=.7*recentSuccess-.3;recentReward*=.6;}
                    if(failures>=3) {stop=exhaustedToday==3?"repeated_budget_exhaustion":"repeated_attempt_failures";break;}
                }
                double utility=CognitiveLab.continueUtility(p,agent,elapsed/planned,recentSuccess,recentReward,"current_model");
                if(elapsed>=planned*(1.12+.45*p.rewardSensitivity)){stop="availability_hard_limit";break;}
                if(dayActions>=4&&CognitiveLab.random(c,p,rep,day,dayActions,333).nextDouble()>CognitiveLab.sigmoid(utility*3.5)&&((dayActions%5)==0||attempt==null)) {
                    stop=elapsed>=planned?"utility_stop_after_planned_time":"utility_stop";break;
                }
            }
            if(attempt!=null&&AttemptOutcome123.resolved(attempt,false)!=null)throw new AssertionError("Terminal attempt at daily boundary");
            if(level>CognitiveLab.CAMPAIGN_CAP)stop="administrative_campaign_scope_cap";
            t.stop(stop);CognitiveLab122.writeDay(output==null?null:output.core,t,day,dayActions,exhaustedToday,stop,elapsed*60);
            if(dayActions>0){t.sessions++;if(day==1)t.d1=true;if(day==7)t.d7=true;t.sessionMinutes+=elapsed;t.overrun+=Math.max(0,elapsed-planned);}
            if(level>CognitiveLab.CAMPAIGN_CAP)break;
        }
        if(attempt!=null)finish(output,run,attempt,c.days-1,AttemptOutcome123.atHorizon(attempt));
        t.knowledge=agent.meanKnowledge();t.frustration=agent.frustration;t.nodes=agent.totalNodes;t.learned=agent.knowledge.clone();
        t.energySpent=energy.spent;t.energyRefilled=energy.refilled;
        if(t.coins!=180+t.coinsEarned-t.coinsSpent||t.coins<0||t.blocked!=t.blockedReasons[0]+t.blockedReasons[1]+t.blockedReasons[2]||t.attempts!=t.outcomes.values().stream().mapToInt(Integer::intValue).sum())throw new AssertionError("Run conservation");
        if(output!=null){CognitiveLab.Outputs.csv(output.core.trajectories,t.json());output.state(run);}
        return run;
    }

    static Map<String,Object> label(String id) {
        switch(id) {
            case "manual_native_123":return Json.obj("fr","Mécanique 1.23 · sans talents activés","en","1.23 mechanics · no activated companions");
            case "cause_hidden_123":return Json.obj("fr","Cause masquée · contrôle expérimental","en","Cause hidden · experimental control");
            case "loaned_mixed_123":return Json.obj("fr","Trio prêté · expérimental","en","Loaned trio · experimental");
            default:return Json.obj("fr","Énergie illimitée · sensibilité expérimentale","en","Unlimited energy · experimental sensitivity");
        }
    }

    static Map<String,Object> aggregate(String scenario,List<Run> runs) {
        List<CognitiveLab122.Run> bases=new ArrayList<>();int direct=0,causal=0;double common=0;
        for(Run r:runs){bases.add(r.base);direct+=r.directCueManual;causal+=r.causeVisibleManual;common+=r.commonKnowledge();}
        Map<String,Object> out=CognitiveLab122.aggregate(scenario,bases);out.put("label",label(scenario));
        out.put("directCueFollowedManualExits",direct);out.put("manualExitsWithCauseVisible",causal);
        out.put("meanCommonRuleKnowledge",runs.isEmpty()?0:common/runs.size());return out;
    }

    static Map<String,Object> probe(CognitiveLab.Config c,Profile p,int rep,int level,double[] learned,String training,BufferedWriter writer)throws IOException {
        Agent122 policy=new Agent122(p);if(learned!=null)System.arraycopy(learned,0,policy.base.knowledge,0,5);
        Environment123 env=new Environment123(level,1,0,"manual_native_123");int coins=180;String outcome=null;
        while(outcome==null) {
            Agent122.Observation o=env.observe(env.seconds,coins,true);
            if(o.board.visibleHint!=-1||o.causeRule!=-1||o.visionLegal!=0||o.board.focusReady||o.board.deadEndWarning||o.skillFamilies.length!=0)throw new AssertionError("Cue-free probe leaked assistance");
            CognitiveAgent.Decision d=policy.choose(o,CognitiveLab.random(c,p,rep,level,env.actions,701),false);
            double seconds=CognitiveLab.duration(p,policy.base,d,CognitiveLab.random(c,p,rep,level,env.actions,702));
            Environment.Result result=env.apply122(d,o,env.seconds,seconds);coins-=result.coinCost;policy.feedback(o,d,result.kind,result.reason,seconds);
            if(result.kind.equals("undo")){int restored=CognitiveLab.findRestored(o.board.present,env.board.remainingMask());if(restored>=0)policy.base.remember(env.board.remainingMask(),env.board.gateMask(),restored);}
            outcome=AttemptOutcome123.resolved(env,result.kind.equals("give_up"));
        }
        if(env.automatedExits!=0||env.shellTaps!=0)throw new AssertionError("Common probe must be manual and shell-free");
        Map<String,Object> row=Json.obj("profileId",p.id,"repetition",rep,"trainingScenario",training,"level",level,"outcome",outcome,"actions",env.actions,"manualExits",env.manualExits,"blocked",env.blocked,"undos",env.undos,"closedGateBlocked",env.blockedReasons[0],"pieceBlocked",env.blockedReasons[1],"barrierBlocked",env.blockedReasons[2],"remaining",env.board.remainingCount(),"budgetRemaining",env.budget.remaining());
        CognitiveLab.Outputs.csv(writer,row);return row;
    }

    static Map<String,Object> transfer(CognitiveLab.Config c,List<Profile> profiles,Map<String,List<Run>> runs)throws IOException {
        List<Object> summaries=new ArrayList<>();Map<String,Boolean> cold=new HashMap<>();int coldCompleted=0;
        try(BufferedWriter writer=CognitiveLab122.EnvironmentWriter.open(c.output,"probe-results.csv.gz","profileId,repetition,trainingScenario,level,outcome,actions,manualExits,blocked,undos,closedGateBlocked,pieceBlocked,barrierBlocked,remaining,budgetRemaining")) {
            for(Profile p:profiles)for(int rep=0;rep<c.repetitions;rep++)for(int level:CognitiveLab.HELD_OUT) {
                Map<String,Object> row=probe(c,p,rep,level,null,"cold_untrained",writer);
                boolean done=row.get("outcome").equals("completed");cold.put(p.id+":"+rep+":"+level,done);if(done)coldCompleted++;
            }
            for(String scenario:SCENARIOS) {
                int count=0,completed=0,trainedOnly=0,coldOnly=0;Map<String,Integer> outcomes=new TreeMap<>();
                for(Run r:runs.get(scenario)) {
                    Profile p=profiles.get(Integer.parseInt(r.base.profile.substring(1))-1);
                    for(int level:CognitiveLab.HELD_OUT) {
                        Map<String,Object> row=probe(c,p,r.base.repetition,level,r.base.learned,scenario,writer);
                        String outcome=(String)row.get("outcome");outcomes.merge(outcome,1,Integer::sum);count++;
                        boolean done=outcome.equals("completed"),before=cold.get(p.id+":"+r.base.repetition+":"+level);
                        if(done)completed++;if(done&&!before)trainedOnly++;if(!done&&before)coldOnly++;
                    }
                }
                summaries.add(Json.obj("scenario",scenario,"probes",count,"completed",completed,"coldReferenceCompleted",coldCompleted,"trainedOnlyCompleted",trainedOnly,"coldOnlyCompleted",coldOnly,"outcomes",outcomes));
            }
        }
        return Json.obj("heldOutLevels",CognitiveLab.HELD_OUT,"commonEnvironment","Same native1.23 shell-free Board/budget,180 Coins; coach,Focus,dead-end warning,causal cues and companions all hidden/disabled for every training condition and cold reference.","reset","Fatigue/frustration/memory reset; only final learned rule state transplanted. Different campaign exposure remains.","coldProbeCount",cold.size(),"coldCompleted",coldCompleted,"trainedProbeCount",cold.size()*SCENARIOS.length,"coldRowsReusedAsReferenceNotReexecuted",true,"scenarios",summaries);
    }

    static Map<String,Object> oracleControls(CognitiveLab.Config c)throws IOException {
        List<Object> rows=new ArrayList<>();Set<Integer> levels=new TreeSet<>();for(int n=1;n<=100;n++)levels.add(n);for(int n:CognitiveLab.HELD_OUT)levels.add(n);
        for(int n:levels) {
            Level level=RelayCatalog.get(n);Board board=new Board(level);MoveBudget budget=MoveBudget.start(0,level);int[] plan=board.solve();
            if(plan==null)throw new AssertionError("Oracle initial board unsolvable");
            for(int id:plan){Board.MoveResult result=budget.attempt(board,id);if(result==null||!result.success)throw new AssertionError("Oracle certified exit rejected");}
            if(!board.isSolved()||budget.used()!=level.minimumActions||ElementalShell121.createJourney123(level,0,n).totalLayers()!=0)throw new AssertionError("Oracle parity");
            rows.add(Json.obj("level",n,"solved",true,"productiveActions",budget.used(),"budgetRemaining",budget.remaining()));
        }
        return Json.obj("scope","Omniscient engine certification only. These104 boards are not agent trajectories, training data, human games or evidence of comprehension.","policyReceivesOracle",false,"boards",rows);
    }

    static Map<String,Object> sourceHashes(CognitiveLab.Config c)throws Exception {
        Map<String,Object> hashes=CognitiveLab122.sourceHashes(c);
        // Android rendering is not compiled by this driver and can be qualified separately.
        hashes.remove("app/src/main/java/com/androidhit/lumadrift/LumaView.java");
        for(String path:new String[]{"scripts/run-cognitive-lab-v123.sh","scripts/test-cognitive-lab-v123.sh","scripts/verify-cognitive-lab-v123.py","scripts/test-support/android/graphics/Color.java","scripts/cognitive_lab/tests/CognitiveLab123Test.java"})hashes.put(path,CognitiveLab.sha(c.root.resolve(path)));
        return hashes;
    }

    public static void main(String[] args)throws Exception {
        CognitiveLab.Config c=new CognitiveLab.Config();c.seed=SEED;c.repetitions=20;c.output=Paths.get("docs/validation/cognitive-lab-v123/latest");
        for(int i=0;i<args.length;i++)switch(args[i]) {
            case "--smoke":c.smoke=true;c.profiles=6;c.repetitions=2;break;
            case "--profiles":c.profiles=Integer.parseInt(args[++i]);break;
            case "--repetitions":c.repetitions=Integer.parseInt(args[++i]);break;
            case "--seed":c.seed=Long.parseLong(args[++i]);break;
            case "--output":c.output=Paths.get(args[++i]);break;
            case "--root":c.root=Paths.get(args[++i]).toAbsolutePath().normalize();break;
            default:throw new IllegalArgumentException(args[i]);
        }
        if(c.profiles<1||c.profiles>100||c.repetitions<1||c.repetitions>100)throw new IllegalArgumentException("Bounded cohort required");
        if(Files.exists(c.output))throw new IllegalArgumentException("Refusing to overwrite existing output "+c.output);
        int scopeMinimum=0;for(int n=1;n<=CognitiveLab.CAMPAIGN_CAP;n++)scopeMinimum+=RelayCatalog.get(n).minimumActions;
        if(scopeMinimum<=c.days*CognitiveLab.MAX_ACTIONS_PER_DAY)throw new IllegalArgumentException("Campaign scope can censor this horizon");
        long started=System.nanoTime();Map<String,Object> before=sourceHashes(c);List<Profile> profiles=Profile.generate(c.profiles,c.seed);
        Map<String,List<Run>> runs=new LinkedHashMap<>();List<Object> summaries=new ArrayList<>(),profileResults=new ArrayList<>(),comparisons=new ArrayList<>();List<Object> traces;
        try(Outputs output=new Outputs(c.output)) {
            for(String scenario:SCENARIOS) {
                List<Run> values=new ArrayList<>();for(Profile p:profiles)for(int rep=0;rep<c.repetitions;rep++)values.add(run(c,p,rep,scenario,output));
                runs.put(scenario,values);summaries.add(aggregate(scenario,values));
                for(Profile p:profiles){List<Run> selected=new ArrayList<>();for(Run r:values)if(r.base.profile.equals(p.id))selected.add(r);Map<String,Object> row=aggregate(scenario,selected);row.put("profileId",p.id);row.put("scenario",scenario);profileResults.add(row);}
                System.out.println("1.23 completed "+scenario+": "+values.size()+" limited-perception trajectories");
            }
            traces=output.core.traces;
        }
        for(int i=1;i<SCENARIOS.length;i++) {
            List<CognitiveLab.Trajectory> base=new ArrayList<>(),target=new ArrayList<>();for(Run r:runs.get(SCENARIOS[0]))base.add(r.base);for(Run r:runs.get(SCENARIOS[i]))target.add(r.base);
            Map<String,Object> comparison=CognitiveLab.compare(SCENARIOS[i],base,target,c.seed);comparison.put("baseline",SCENARIOS[0]);
            ((Map<?,?>)comparison.get("metrics")).remove("meanKnowledge");comparisons.add(comparison);
        }
        System.out.println("Running common-environment cold and trained probes");Map<String,Object> transfer=transfer(c,profiles,runs);
        Map<String,Object> oracle=oracleControls(c);Json.write(c.output.resolve("oracle-controls.json"),oracle);
        Map<String,Object> scope=Json.obj("kind","synthetic_local_hypothesis_lab","humanParticipants",0,"humanRetentionPrediction",false,"iqCalibration",false,"clinicalAssessment",false,"gameVersion","1.23","modelVersion",VERSION,
                "engine","Actual Board,MoveBudget policy6,createJourney123,Eco121 and explicit native ability effects;100% shell-free Journey attempts.",
                "primary","Manual engine condition with immediate blocked-cause feedback, native U1 coach, earned Focus and dead-end warning. No activated companion or ownership model.",
                "experimental","Hide last-error cause; fully loaned nova/hoot/orbit trio; bypass pacing-energy reservation. No native economy adjustment is implied.",
                "terminalClassification","After each action: solved,budget exhausted,give-up,administrative attempt cap,then availability/utility/day cap. A positive-budget open attempt alone is horizon-censored. Failure feedback now occurs immediately.",
                "cueAssumption","Displayed cause is temporarily recognized with certainty; no initial learning bonus. Structured visible pieces/keys/relations are already identified.",
                "solverBoundary","Bounded policy has no solver. Only explicit native help/talent activation can expose certified information; separate oracle certification is never injected into policy search.",
                "timingLimit","Legacy five-rule duration formula retained, including unused shell term, as an uncalibrated timing assumption; report common-rule knowledge separately.",
                "exclusions",Arrays.asList("No rendering,visual perception,motor behavior,emotion,comprehension or human retention measurement","New1.23 inspection teaching/navigation/HUD are not simulated","No ownership/acquisition/equipment,stock powers,ads,purchases,social systems or Android persistence","Tactical chapter unchanged and not rerun in this protocol"));
        Map<String,Object> config=Json.obj("modelVersion",VERSION,"profiles",c.profiles,"repetitions",c.repetitions,"seed",c.seed,"days",c.days,"scenarios",Arrays.asList(SCENARIOS),"maxActionsPerDay",CognitiveLab.MAX_ACTIONS_PER_DAY,"maxAttemptActions",CognitiveLab.MAX_ATTEMPT_ACTIONS,"campaignScopeCap",CognitiveLab.CAMPAIGN_CAP,"initialKnowledgeBonus",0,"learningRateMultiplier",1,"causalCueSeconds",3.2,"nativeFactory","ElementalShell121.createJourney123","oracleControlsSeparate",true);
        Map<String,Object> summary=Json.obj("schemaVersion",1,"runId",VERSION+"-"+c.seed,"scope",scope,"config",config,"totals",Json.obj("trajectories",c.profiles*c.repetitions*SCENARIOS.length,"profileCount",c.profiles,"repetitionsPerProfile",c.repetitions,"scenarioCount",SCENARIOS.length,"humanParticipants",0),"scenarios",summaries,"profileResults",profileResults);
        List<Object> ps=new ArrayList<>();for(Profile p:profiles)ps.add(p.json());Map<String,Object> profileData=Json.obj("schemaVersion",1,"generation","Same uncalibrated Latin-hypercube parameter generator;no human sample", "profiles",ps);
        Map<String,Object> paired=Json.obj("schemaVersion",1,"pairedSeeds",true,"comparisons",comparisons,"commonEnvironmentTransfer",transfer);
        Map<String,Object> traceData=Json.obj("selection","First4 profiles repetition0,max80 per condition;nonrepresentative. All blocks and manual assistance events separately exported.","traces",traces);
        Json.write(c.output.resolve("summary.json"),summary);Json.write(c.output.resolve("profiles.json"),profileData);Json.write(c.output.resolve("paired-comparisons.json"),paired);Json.write(c.output.resolve("traces.json"),traceData);
        Json.write(c.output.resolve("report.json"),Json.obj("schemaVersion",1,"summary",summary,"profiles",profileData,"pairedComparisons",paired,"oracleControls",oracle,"traces",traceData));
        Map<String,Object> after=sourceHashes(c);if(!before.equals(after))throw new AssertionError("Source changed during run; discard this unbound execution");
        Map<String,Object> files=new TreeMap<>();try(java.util.stream.Stream<Path> paths=Files.list(c.output)){for(Path p:(Iterable<Path>)paths.filter(Files::isRegularFile)::iterator)files.put(p.getFileName().toString(),Json.obj("sha256",CognitiveLab.sha(p),"bytes",Files.size(p)));}
        Json.write(c.output.resolve("provenance.json"),Json.obj("schemaVersion",1,"modelVersion",VERSION,"seed",c.seed,"elapsedSeconds",(System.nanoTime()-started)/1e9,"javaRuntimeVersion",System.getProperty("java.runtime.version"),"sourceSha256",after,"sourceHashesStableBeforeAndAfter",true,"uncompiledUiContext","app/src/main/java/com/androidhit/lumadrift/LumaView.java","outputs",files,"historicalOutputsNotRewritten",true));
        System.out.println("1.23 data ready "+c.output+"; all behavioral results modeled");
    }
}
