package cognitive_lab;

import com.androidhit.lumadrift.engine.*;
import com.androidhit.lumadrift.data.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;

/** Separate four-stage chapter probes. No campaign coins, energy, shells or return utility. */
final class TacticalLab122 {
    static final String[] POLICIES={"manual","early","reserve_last_two"};
    static final int[] CAPACITIES={4,6,8};
    static final class Env extends Environment122 {
        TacticalChapterRun run;
        Env(TacticalChapterRun run,int ordinal){super(run.currentLevel(),ordinal,0,"cause_immediate");this.run=run;for(Tile tile:board.level.tiles)shells.clear(tile.id);}
        Agent122.Observation observe(double clock,int coins,boolean removed){
            Agent122.Observation o=super.observe(clock,coins,removed);CognitiveAgent.Observation b=o.board;
            // Tactical screen has no earned campaign Focus or U1 coach.
            CognitiveAgent.Observation projection=new CognitiveAgent.Observation(b.pieces,b.walls,b.present,b.gates,b.shells,b.remainingBudget,b.visibleHint,false,b.canUndo,b.deadEndWarning,0,b.cueSource);
            int[] families=new int[removed?0:run.team().size()];boolean[] ready=new boolean[families.length];for(int s=0;s<families.length;s++){families[s]=TacticalChapterCatalog.talent(s).effect.ordinal();ready[s]=run.canUse(s);}
            return new Agent122.Observation(projection,o.causeRule,o.causeSource,0,families,ready,!removed&&run.comboReady());
        }
        Result apply122(CognitiveAgent.Decision d,Agent122.Observation o,double clock,double duration){
            if(d.action>Agent122.SKILL_BASE)return super.apply122(d,o,clock,duration);
            actions++;seconds+=duration;skillRequests++;boolean combo=d.action==Agent122.COMBO;int slot=combo?0:Agent122.SKILL_BASE-d.action;
            TacticalChapterAction.Draft draft=TacticalChapterAction.prepare(board,budget,undos,run,slot,combo,board.solve(),run.encode());
            if(draft==null){skillUnavailable++;return new Result("skill_unavailable","NONE",0,duration);}
            board=draft.board;budget=draft.budget;run=draft.run;undos=draft.undos;skillUses++;familyUses[draft.effect.ordinal()]++;if(combo)comboUses++;
            for(int id:draft.performed){automatedExits++;exits++;}if(draft.rewound.length>0)chain.correction();hint=draft.hintId;hintUntil=clock+duration+draft.hintDurationMs/1000d;lastRule=-1;
            return new Result("skill_"+draft.effect.name().toLowerCase(Locale.ROOT),"NONE",0,duration);
        }
    }
    static Map<String,Object> run(CognitiveLab.Config c,List<Profile> profiles,Path out)throws Exception{
        List<Object> summaries=new ArrayList<>();BufferedWriter runs=CognitiveLab122.EnvironmentWriter.open(out,"chapter-results.csv.gz","profileId,repetition,episode,capacity,policy,stagesCompleted,finished,actions,manualExits,automatedExits,blocked,repeatedErrors,skillRequests,skillUses,skillUnavailable,comboUses,chargeRemaining,outcome,modeledSeconds,routeIds");
        BufferedWriter stages=CognitiveLab122.EnvironmentWriter.open(out,"chapter-stage-results.csv.gz","profileId,repetition,episode,capacity,policy,stage,layoutId,actions,manualExits,automatedExits,blocked,closedGateBlocked,pieceBlocked,barrierBlocked,repeatedErrors,undos,budgetUsed,budgetRemaining,skillUses,skillUnavailable,chargeBefore,chargeAfter,outcome");
        try{for(TacticalChapterCatalog.Episode episode:TacticalChapterCatalog.Episode.values())for(int capacity:CAPACITIES)for(String policy:POLICIES){int n=0,finished=0,actions=0,manual=0,automatic=0,blocks=0,exhausted=0,administrative=0,spent=0,requests=0,used=0,unavailable=0,combos=0,repeated=0,completedStages=0;int[] stageStarts=new int[4],stageClears=new int[4];
            for(Profile profile:profiles)for(int rep=0;rep<c.repetitions;rep++){
                Agent122 agent=new Agent122(profile);TacticalChapterRun chapter=TacticalChapterRun.start("lab_"+profile.id+"_"+rep,CognitiveLab.mix(c.seed,profile.id.hashCode(),rep),episode,capacity);int runActions=0,runManual=0,runAuto=0,runBlocks=0,runRepeated=0,runRequests=0,runUses=0,runUnavailable=0,runCombos=0;double seconds=0;String finalOutcome="completed";List<String> routes=new ArrayList<>();
                for(int stage=0;stage<4;stage++){
                    List<TacticalChapterCatalog.Layout> options=chapter.options();int option=CognitiveLab.random(c,profile,rep,episode.ordinal(),stage,3301).nextInt(options.size());chapter=chapter.chooseRoute(options.get(option).id);routes.add(chapter.currentLayout().id);Env env=new Env(chapter,stage+1);int chargeBefore=chapter.chargeRemaining();stageStarts[stage]++;
                    while(!env.board.isSolved()&&!env.budget.isExhausted()&&env.actions<CognitiveLab.MAX_ATTEMPT_ACTIONS){
                        Agent122.Observation o=env.observe(seconds,100000,false);boolean skills=!policy.equals("manual")&&(!policy.equals("reserve_last_two")||stage>=2);CognitiveAgent.Decision d=agent.choose(o,CognitiveLab.random(c,profile,rep,episode.ordinal(),stage,env.actions,3302),skills);double duration=CognitiveLab.duration(profile,agent.base,d,CognitiveLab.random(c,profile,rep,episode.ordinal(),stage,env.actions,3303));Environment.Result result=env.apply122(d,o,seconds,duration);seconds+=duration;agent.feedback(o,d,result.kind,result.reason,duration);
                        if(result.kind.equals("undo")){int restored=CognitiveLab.findRestored(o.board.present,env.board.remainingMask());if(restored>=0)agent.base.remember(env.board.remainingMask(),env.board.gateMask(),restored);}if(result.kind.equals("give_up"))break;
                    }
                    String outcome=env.board.isSolved()?"completed":env.budget.isExhausted()?"budget_exhausted":env.actions>=CognitiveLab.MAX_ATTEMPT_ACTIONS?"administrative_attempt_cap":"agent_give_up";
                    runActions+=env.actions;runManual+=env.manualExits;runAuto+=env.automatedExits;runBlocks+=env.blocked;runRepeated+=env.repeatedErrors;runRequests+=env.skillRequests;runUses+=env.skillUses;runUnavailable+=env.skillUnavailable;runCombos+=env.comboUses;chapter=env.run;
                    CognitiveLab.Outputs.csv(stages,Json.obj("profileId",profile.id,"repetition",rep,"episode",episode.name(),"capacity",capacity,"policy",policy,"stage",stage,"layoutId",env.run.currentLayout().id,"actions",env.actions,"manualExits",env.manualExits,"automatedExits",env.automatedExits,"blocked",env.blocked,"closedGateBlocked",env.blockedReasons[0],"pieceBlocked",env.blockedReasons[1],"barrierBlocked",env.blockedReasons[2],"repeatedErrors",env.repeatedErrors,"undos",env.undos,"budgetUsed",env.budget.used(),"budgetRemaining",env.budget.remaining(),"skillUses",env.skillUses,"skillUnavailable",env.skillUnavailable,"chargeBefore",chargeBefore,"chargeAfter",chapter.chargeRemaining(),"outcome",outcome));
                    if(!outcome.equals("completed")){finalOutcome=outcome;if(outcome.equals("budget_exhausted"))exhausted++;if(outcome.equals("administrative_attempt_cap"))administrative++;break;}
                    stageClears[stage]++;TacticalChapterRun awarded=chapter.recordWin(chapter.currentVictoryId(),env.board,env.stars());if(awarded==null)throw new AssertionError("Actual chapter win receipt rejected");chapter=awarded;
                }
                n++;if(chapter.finished())finished++;completedStages+=chapter.completedStages;actions+=runActions;manual+=runManual;automatic+=runAuto;blocks+=runBlocks;repeated+=runRepeated;spent+=capacity-chapter.chargeRemaining();requests+=runRequests;used+=runUses;unavailable+=runUnavailable;combos+=runCombos;
                CognitiveLab.Outputs.csv(runs,Json.obj("profileId",profile.id,"repetition",rep,"episode",episode.name(),"capacity",capacity,"policy",policy,"stagesCompleted",chapter.completedStages,"finished",chapter.finished(),"actions",runActions,"manualExits",runManual,"automatedExits",runAuto,"blocked",runBlocks,"repeatedErrors",runRepeated,"skillRequests",runRequests,"skillUses",runUses,"skillUnavailable",runUnavailable,"comboUses",runCombos,"chargeRemaining",chapter.chargeRemaining(),"outcome",finalOutcome,"modeledSeconds",seconds,"routeIds",String.join("|",routes)));
            }
            summaries.add(Json.obj("episode",episode.name(),"capacity",capacity,"policy",policy,"runs",n,"finished",finished,"completionRate",(double)finished/n,"stagesCompleted",completedStages,"stageStarts",stageStarts,"stageClears",stageClears,"actions",actions,"manualExits",manual,"automatedExits",automatic,"blocked",blocks,"repeatedErrors",repeated,"skillRequests",requests,"skillUses",used,"skillUnavailable",unavailable,"comboUses",combos,"chargeSpent",spent,"budgetExhaustedRuns",exhausted,"administrativeCensoredRuns",administrative));
        }}finally{runs.close();stages.close();}
        return Json.obj("modelVersion",TacticalChapterCatalog.VERSION,"boardFamily",TacticalChapterCatalog.BOARD_FAMILY,"scope","Separate four-stage native chapter, one attempt per selected stage; no shell/coin/session-energy or return utility; route seeds identical across capacities/policies; fixed loan team nova/hoot/orbit. Charge and one-use talents persist across boards, no recharge. Capacities4/8 and spending policies are experimental; native default6.","cues","Immediate blocked cause3200ms and native dead-end warning; all solution information only via explicit GUIDE/FLOW/resonance native action. No Focus chain or campaign coach.","policies",Json.obj("manual","Never activate talents; same visible engine feedback","early","Same bounded skill request policy as campaign; available from first board","reserve_last_two","Decline talents on boards1/2, allow same request policy on boards3/4"),"comparisons",summaries);
    }
}
