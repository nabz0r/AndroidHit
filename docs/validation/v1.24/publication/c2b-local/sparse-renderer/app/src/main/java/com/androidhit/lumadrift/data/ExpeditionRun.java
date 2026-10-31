package com.androidhit.lumadrift.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Immutable sortie and strict event codec. Persist one encoded state in the store's existing atomic transaction. */
public final class ExpeditionRun {
    public enum Goal { COPIES, HOME }
    public static final class StageResult {
        public final int stage,boardNumber,stars;
        public final String optionId,victoryId;
        private StageResult(int stage,OptionState selected,String victoryId,int stars){this.stage=stage;optionId=selected.option.id;boardNumber=selected.option.boardNumber;this.victoryId=victoryId;this.stars=stars;}
    }
    private static final class OptionState {
        final ExpeditionCatalog.Option option;
        OptionState(ExpeditionCatalog.Option option){this.option=option;}
    }
    public final String id,companionId,targetId;
    public final long seed;
    public final int companionLevel,completedStages;
    public final Goal goal;
    public final boolean claimed;
    private final OptionState selected;
    private final List<StageResult> stages;
    private final List<String> log;
    private final Set<String> revealed,replaced;
    private final Set<Integer> projects;
    private final int spent;

    private ExpeditionRun(String id,long seed,String companionId,int level,Goal goal,String targetId,
                          OptionState selected,List<StageResult> stages,List<String> log,Set<String> revealed,
                          Set<String> replaced,Set<Integer> projects,int spent,boolean claimed) {
        this.id=id;this.seed=seed;this.companionId=companionId;companionLevel=level;this.goal=goal;this.targetId=targetId;
        this.selected=selected;this.stages=Collections.unmodifiableList(new ArrayList<>(stages));completedStages=stages.size();
        this.log=Collections.unmodifiableList(new ArrayList<>(log));this.revealed=Collections.unmodifiableSet(new HashSet<>(revealed));
        this.replaced=Collections.unmodifiableSet(new HashSet<>(replaced));this.projects=Collections.unmodifiableSet(new HashSet<>(projects));
        this.spent=spent;this.claimed=claimed;
    }
    private static boolean token(String id){return id!=null&&id.length()>=1&&id.length()<=128&&id.matches("[A-Za-z0-9:_-]+");}
    public static ExpeditionRun start(String id,long seed,String companionId,int level,Goal goal,String targetId){
        if(!token(id)||!CompanionProgression.expeditionAvailable(companionId)||level<1||level>10||goal==null)return null;
        if(goal==Goal.COPIES?CompanionProgression.find(targetId)==null:!ExpeditionCatalog.SOUVENIR_ID.equals(targetId))return null;
        return new ExpeditionRun(id,seed,companionId,level,goal,targetId,null,Collections.emptyList(),Collections.emptyList(),Collections.emptySet(),Collections.emptySet(),Collections.emptySet(),0,false);
    }
    private ExpeditionRun next(OptionState selection,List<StageResult> results,Set<String> peeks,Set<String> replacements,Set<Integer> openProjects,int charges,boolean collected,String event){
        List<String> events=new ArrayList<>(log);events.add(event);return new ExpeditionRun(id,seed,companionId,companionLevel,goal,targetId,selection,results,events,peeks,replacements,openProjects,charges,collected);
    }
    public boolean finished(){return completedStages==ExpeditionCatalog.STAGES;}
    public int stage(){return completedStages;}
    public List<StageResult> results(){return stages;}
    public int totalStars(){int total=0;for(StageResult stage:stages)total+=stage.stars;return total;}
    public int skillCapacity(){return CompanionProgression.skillCharges(companionLevel);}
    public int skillRemaining(){return skillCapacity()-spent;}
    public CompanionProgression.Skill skill(){return CompanionProgression.find(companionId).skill;}
    public ExpeditionCatalog.Option selectedOption(){return selected==null?null:selected.option;}
    public List<ExpeditionCatalog.Option> options(){
        if(finished())return Collections.emptyList();String previous=stages.isEmpty()?"":stages.get(stages.size()-1).optionId;
        List<ExpeditionCatalog.Option> result=new ArrayList<>();for(ExpeditionCatalog.Option option:ExpeditionCatalog.baseOptions(seed,completedStages,previous))
            result.add(replaced.contains(option.id)?ExpeditionCatalog.replacement(option,seed):option);
        if(projects.contains(completedStages))result.add(ExpeditionCatalog.project(seed,completedStages,goal,result));
        return Collections.unmodifiableList(result);
    }
    public ExpeditionCatalog.Option option(String optionId){for(ExpeditionCatalog.Option option:options())if(option.id.equals(optionId))return option;return null;}
    public boolean revealed(String optionId){return revealed.contains(optionId);}
    public boolean projectOpened(){return projects.contains(completedStages);}
    public boolean canUseSkill(String optionId){
        if(finished()||selected!=null||skillRemaining()<=0)return false;ExpeditionCatalog.Option option=option(optionId);
        switch(skill()){
            case REVEAL:return option!=null&&option.futureBoardNumber>0&&!revealed.contains(optionId);
            case REROUTE:return option!=null&&!option.alternate&&!option.project&&!replaced.contains(optionId);
            case PROJECT:return !projects.contains(completedStages)&&(option!=null||"project".equals(optionId)||"".equals(optionId));
            default:return false;
        }
    }
    /** Nova peeks at a candidate beyond the visible next puzzle. Miko substitutes a real board; Orbit adds a project route. */
    public ExpeditionRun useSkill(String optionId){
        if(!canUseSkill(optionId))return null;Set<String> peeks=new HashSet<>(revealed),replacements=new HashSet<>(replaced);Set<Integer> openProjects=new HashSet<>(projects);
        switch(skill()){
            case REVEAL:peeks.add(optionId);break;
            case REROUTE:replacements.add(optionId);break;
            case PROJECT:openProjects.add(completedStages);optionId="project";break;
            default:return null;
        }
        return next(null,stages,peeks,replacements,openProjects,spent+1,claimed,"S,"+optionId);
    }
    /** Selecting a route is durable before the puzzle opens. A selected route cannot silently change mid-board. */
    public ExpeditionRun choose(String optionId){
        if(finished())return null;if(selected!=null)return selected.option.id.equals(optionId)?this:null;
        ExpeditionCatalog.Option choice=option(optionId);if(choice==null)return null;
        return next(new OptionState(choice),stages,revealed,replaced,projects,spent,claimed,"C,"+optionId);
    }
    /** The store verifies the actual saved solved board; this pure model rejects mismatched and reused victory identities. */
    public ExpeditionRun complete(String victoryId,int boardNumber,int stars){
        if(!token(victoryId)||stars<1||stars>3)return null;
        for(StageResult result:stages)if(result.victoryId.equals(victoryId))return result.boardNumber==boardNumber&&result.stars==stars?this:null;
        if(finished()||selected==null||selected.option.boardNumber!=boardNumber)return null;
        List<StageResult> results=new ArrayList<>(stages);results.add(new StageResult(completedStages,selected,victoryId,stars));
        return next(null,results,revealed,replaced,projects,spent,claimed,"W,"+victoryId+","+boardNumber+","+stars);
    }
    /** Completion rewards are a contract, not a grant. GameStore applies and marks claimed in one atomic commit. */
    public int copiesReward(){return finished()&&goal==Goal.COPIES?ExpeditionCatalog.COPIES_REWARD:0;}
    public int homeContributionReward(){return finished()&&goal==Goal.HOME?ExpeditionCatalog.HOME_CONTRIBUTION:0;}
    public String completionRequestId(){return "expedition:"+id;}
    public ExpeditionRun claim(){if(!finished())return null;if(claimed)return this;return next(null,stages,revealed,replaced,projects,spent,true,"Q");}
    public String encode(){StringBuilder out=new StringBuilder(ExpeditionCatalog.VERSION).append('|').append(id).append('|').append(seed).append('|').append(companionId).append('|').append(companionLevel).append('|').append(goal.name()).append('|').append(targetId).append('|');for(int n=0;n<log.size();n++){if(n>0)out.append(';');out.append(log.get(n));}return out.toString();}
    /** Invalid, noncanonical or future-version records never partially restore a route or create a reward. */
    public static ExpeditionRun decode(String encoded){
        if(encoded==null||encoded.length()>8192)return null;
        try{
            String[] fields=encoded.split("\\|",-1);if(fields.length!=8||!ExpeditionCatalog.VERSION.equals(fields[0]))return null;
            ExpeditionRun run=start(fields[1],Long.parseLong(fields[2]),fields[3],Integer.parseInt(fields[4]),Goal.valueOf(fields[5]),fields[6]);if(run==null)return null;
            if(!fields[7].isEmpty()){
                String[] events=fields[7].split(";",-1);if(events.length>20)return null;
                for(String event:events){String[] parts=event.split(",",-1);
                    if(parts.length==2&&"C".equals(parts[0]))run=run.choose(parts[1]);
                    else if(parts.length==2&&"S".equals(parts[0]))run=run.useSkill(parts[1]);
                    else if(parts.length==4&&"W".equals(parts[0]))run=run.complete(parts[1],Integer.parseInt(parts[2]),Integer.parseInt(parts[3]));
                    else if(parts.length==1&&"Q".equals(parts[0]))run=run.claim();else return null;
                    if(run==null)return null;
                }
            }
            return encoded.equals(run.encode())?run:null;
        }catch(IllegalArgumentException invalid){return null;}
    }
}
