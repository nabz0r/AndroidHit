package cognitive_lab;

import com.androidhit.lumadrift.engine.*;
import com.androidhit.lumadrift.data.*;
import java.util.*;

/** Actual 1.21 mechanics plus explicitly exposed 1.22 interventions. */
class Environment122 extends Environment {
    final String scenario;CompanionBattle.State companion;
    int manualExits,automatedExits,skillRequests,skillUses,comboUses,skillUnavailable,causalCueObservations,repeatedErrors;
    final int[] blockedReasons=new int[3],familyUses=new int[6],cueByRule=new int[5];
    String lastError="",lastReason="NONE";int lastRule=-1,sameErrorCount;
    int hint=-1;int[] shownTrail=new int[0];double hintUntil,trailUntil,visionUntil,causeUntil;
    final Map<String,Integer> errorCounts=new HashMap<>();
    Environment122(int level,int ordinal,int day,String scenario){super(level,ordinal,day);this.scenario=scenario;initTeam();}
    Environment122(Level level,int ordinal,int day,String scenario){super(level,ordinal,day);this.scenario=scenario;initTeam();}
    void initTeam(){
        if(scenario.equals("team_information"))companion=CompanionBattle.State.start(team(0,1,2));
        else if(scenario.equals("team_execution"))companion=CompanionBattle.State.start(team(3,4,5));
        else if(scenario.equals("v122_combined"))companion=CompanionBattle.State.start(Arrays.asList("nova","hoot","orbit"));
    }
    static List<String> team(int... families){List<String> ids=new ArrayList<>();for(int family:families)for(CompanionBattle.Definition d:CompanionBattle.all())if(d.effect.ordinal()==family){ids.add(d.id);break;}return ids;}
    Agent122.Observation observe(double clock,int coins,boolean removeAllCues){
        CognitiveAgent.Observation o=super.observation(clock,coins);int rule=-1;String source="none";
        if(!removeAllCues&&lastRule>=0&&clock<=causeUntil){boolean expose=scenario.equals("cause_immediate")||scenario.equals("cause_repeat")&&sameErrorCount>=2||(scenario.equals("cause_fading")||scenario.equals("v122_combined"))&&(cueByRule[lastRule]==0||sameErrorCount>=2);
            if(expose){rule=lastRule;source="visible_blocked_"+lastReason;causalCueObservations++;cueByRule[rule]++;}}
        int cue=o.visibleHint;String cueSource=o.cueSource;long legal=0;
        if(!removeAllCues){if(hint>=0&&clock<=hintUntil&&board.isPresent(hint)){cue=hint;cueSource="companion_guide";}
            else if(clock<=trailUntil)for(int id:shownTrail)if(board.isPresent(id)){cue=id;cueSource="companion_trail";break;}
            if(visionUntil>0&&clock<=visionUntil)for(Tile tile:board.legalMoves())legal|=1L<<tile.id;
        }
        if(removeAllCues){cue=-1;cueSource="none";}
        CognitiveAgent.Observation projected=new CognitiveAgent.Observation(o.pieces,o.walls,o.present,o.gates,o.shells,o.remainingBudget,cue,!removeAllCues&&o.focusReady,o.canUndo,!removeAllCues&&o.deadEndWarning,o.undoCost,cueSource);
        int n=removeAllCues||companion==null?0:companion.team().size();int[] families=new int[n];boolean[] ready=new boolean[n];
        for(int slot=0;slot<n;slot++){families[slot]=CompanionBattle.find(companion.team().get(slot)).effect.ordinal();ready[slot]=companion.canUse(slot);}
        return new Agent122.Observation(projected,rule,source,legal,families,ready,!removeAllCues&&companion!=null&&companion.comboReady());
    }
    Result apply122(CognitiveAgent.Decision d,Agent122.Observation o,double clock,double duration){
        if(d.action<=Agent122.SKILL_BASE){
            actions++;seconds+=duration;skillRequests++;boolean combo=d.action==Agent122.COMBO;int slot=combo?0:Agent122.SKILL_BASE-d.action;
            // Solver continuation is passed only to the real native ability executor.
            // It is never stored in the policy observation or its search/candidates.
            CompanionBattleAction.Draft draft=CompanionBattleAction.prepare(board,budget,0,undos,companion,slot,combo,board.solve());
            if(draft==null){skillUnavailable++;return new Result("skill_unavailable","NONE",0,duration);}
            int family=draft.effect.ordinal();familyUses[family]++;skillUses++;if(combo)comboUses++;
            board=draft.board;budget=draft.budget;companion=draft.state;undos=draft.undos;
            for(int id:draft.performed){automatedExits++;exits++;shells.clear(id);}
            if(draft.rewound.length>0)chain.correction();hint=draft.hintId;hintUntil=clock+duration+draft.hintDurationMs/1000d;
            shownTrail=draft.trail;trailUntil=clock+duration+draft.trailDurationMs/1000d;visionUntil=draft.visionDurationMs>0?clock+duration+draft.visionDurationMs/1000d:0;
            lastRule=-1;return new Result("skill_"+draft.effect.name().toLowerCase(Locale.ROOT),"NONE",0,duration);
        }
        Result result=super.apply(d,o.board,clock,duration);
        if(result.kind.equals("exit")){manualExits++;if(companion!=null)companion=companion.creditExit(d.action);lastRule=-1;sameErrorCount=0;}
        else if(result.kind.equals("blocked")){
            int i=result.reason.equals("CLOSED_GATE")?0:result.reason.equals("LOCKED_BARRIER")?2:1;blockedReasons[i]++;
            causeUntil=clock+duration+3.2;lastReason=result.reason;lastRule=i==0?0:i==2?3:1;
            String key=board.remainingMask()+":"+board.gateMask()+":"+d.action+":"+result.reason;
            int count=errorCounts.getOrDefault(key,0)+1;errorCounts.put(key,count);if(count>1)repeatedErrors++;
            sameErrorCount=count;lastError=key;
        } else {lastRule=-1;if(!result.kind.equals("shell"))sameErrorCount=0;}
        return result;
    }
    Map<String,Object> row122(String profile,int repetition,int day,String outcome){Map<String,Object> row=super.row(profile,repetition,scenario,day,outcome);row.put("manualExits",manualExits);row.put("automatedExits",automatedExits);row.put("closedGateBlocked",blockedReasons[0]);row.put("pieceBlocked",blockedReasons[1]);row.put("barrierBlocked",blockedReasons[2]);row.put("repeatedErrors",repeatedErrors);row.put("causalCueObservations",causalCueObservations);row.put("skillRequests",skillRequests);row.put("skillUses",skillUses);row.put("skillUnavailable",skillUnavailable);row.put("comboUses",comboUses);row.put("familyUses",familyMap());row.put("horizonCensored",outcome.equals("horizon_censored"));row.put("administrativeCensored",outcome.equals("attempt_action_cap"));return row;}
    Map<String,Object> familyMap(){Map<String,Object> result=new LinkedHashMap<>();for(CompanionBattle.Effect e:CompanionBattle.Effect.values())result.put(e.name(),familyUses[e.ordinal()]);return result;}
}
