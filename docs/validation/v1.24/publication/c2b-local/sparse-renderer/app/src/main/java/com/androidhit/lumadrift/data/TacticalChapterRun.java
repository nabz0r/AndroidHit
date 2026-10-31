package com.androidhit.lumadrift.data;

import com.androidhit.lumadrift.engine.Board;
import com.androidhit.lumadrift.engine.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Immutable four-board sortie and strict replay codec. Board, budget and this ledger save atomically. */
public final class TacticalChapterRun {
    public final String id;
    public final long seed;
    public final TacticalChapterCatalog.Episode episode;
    public final int capacity,completedStages;
    public final boolean claimed;
    private final TacticalChapterCatalog.Layout selected;
    private final int used;
    private final boolean comboSpent;
    private final List<StageResult> results;
    private final List<String> events;
    public static final class StageResult {
        public final int stage,stars;
        public final String layoutId,victoryId;
        private final int[] history;
        private StageResult(int stage,String layoutId,String victoryId,int stars,int[] history){this.stage=stage;this.layoutId=layoutId;this.victoryId=victoryId;this.stars=stars;this.history=history.clone();}
        public int[] history(){return history.clone();}
    }
    private TacticalChapterRun(String id,long seed,TacticalChapterCatalog.Episode episode,int capacity,
                               TacticalChapterCatalog.Layout selected,int used,boolean comboSpent,List<StageResult> results,boolean claimed,List<String> events){
        this.id=id;this.seed=seed;this.episode=episode;this.capacity=capacity;this.selected=selected;this.used=used;this.comboSpent=comboSpent;
        this.results=Collections.unmodifiableList(new ArrayList<>(results));completedStages=results.size();this.claimed=claimed;this.events=Collections.unmodifiableList(new ArrayList<>(events));
    }
    private static boolean token(String value){return value!=null&&value.length()>0&&value.length()<=96&&value.matches("[A-Za-z0-9:_-]+");}
    public static TacticalChapterRun start(String id,long seed,TacticalChapterCatalog.Episode episode){return start(id,seed,episode,TacticalChapterCatalog.DEFAULT_CHARGE);}
    /** Capacities 4 and 8 are explicit experimental variants, never inferred from ownership or spending. */
    public static TacticalChapterRun start(String id,long seed,TacticalChapterCatalog.Episode episode,int capacity){
        if(!token(id)||episode==null||(capacity!=4&&capacity!=6&&capacity!=8))return null;
        return new TacticalChapterRun(id,seed,episode,capacity,null,0,false,Collections.emptyList(),false,Collections.emptyList());
    }
    public static TacticalChapterRun start(long seed,TacticalChapterCatalog.Episode episode){return episode==null?null:start("t122_"+episode.name().toLowerCase(java.util.Locale.ROOT)+"_"+Long.toUnsignedString(seed,36),seed,episode);}
    private TacticalChapterRun next(TacticalChapterCatalog.Layout layout,int slots,boolean combo,List<StageResult> wins,boolean collected,String event){List<String> log=new ArrayList<>(events);log.add(event);return new TacticalChapterRun(id,seed,episode,capacity,layout,slots,combo,wins,collected,log);}
    public boolean finished(){return completedStages==TacticalChapterCatalog.STAGES;}
    public int stage(){return completedStages;}
    public List<StageResult> results(){return results;}
    public List<TacticalChapterCatalog.Layout> options(){return finished()?Collections.emptyList():TacticalChapterCatalog.options(episode,completedStages);}
    public TacticalChapterCatalog.Layout currentLayout(){return selected;}
    public Level currentLevel(){return selected==null?null:selected.level();}
    public List<String> team(){return TacticalChapterCatalog.loanTeam();}
    public int chargeRemaining(){int spent=0;for(int slot=0;slot<team().size();slot++)if(used(slot))spent+=TacticalChapterCatalog.talent(slot).cost;return capacity-spent;}
    public int energy(){return chargeRemaining();}
    public boolean used(int slot){return slot>=0&&slot<team().size()&&(used&(1<<slot))!=0;}
    public boolean canUse(int slot){CompanionBattle.Definition talent=TacticalChapterCatalog.talent(slot);return selected!=null&&!finished()&&talent!=null&&!used(slot)&&chargeRemaining()>=talent.cost;}
    public boolean comboReady(){return selected!=null&&!finished()&&!comboSpent&&Integer.bitCount(used)>=2;}
    public boolean comboSpent(){return comboSpent;}
    /** A durable stage scope, unrelated to the campaign's overlapping numeric board field. */
    public String currentBoardScope(){return selected==null?null:id+":"+completedStages+":"+selected.id;}
    public String currentVictoryId(){return currentBoardScope();}
    public TacticalChapterRun chooseRoute(String layoutId){
        if(finished()||layoutId==null)return null;if(selected!=null)return selected.id.equals(layoutId)?this:null;
        for(TacticalChapterCatalog.Layout layout:options())if(layout.id.equals(layoutId))return next(layout,used,comboSpent,results,claimed,"C,"+layout.id);
        return null;
    }
    /** Caller commits only after TacticalChapterAction has prepared a real effect. No exits replenish Charge. */
    public TacticalChapterRun useTalent(int slot,String scope){
        if(!canUse(slot)||!currentBoardScope().equals(scope))return null;
        return next(selected,used|(1<<slot),comboSpent,results,claimed,"T,"+slot+","+scope);
    }
    public TacticalChapterRun useTalent(int slot,String scope,String expectedEncoded){return encode().equals(expectedEncoded)?useTalent(slot,scope):null;}
    public TacticalChapterRun useCombo(String scope){if(!comboReady()||!currentBoardScope().equals(scope))return null;return next(selected,used,true,results,claimed,"R,"+scope);}
    public TacticalChapterRun useCombo(String scope,String expectedEncoded){return encode().equals(expectedEncoded)?useCombo(scope):null;}
    /** Exact scope, exact geometry and replayed solved history; a flag or board number cannot award a stage. */
    public TacticalChapterRun recordWin(String victoryId,Board solved,int stars){
        if(victoryId==null||solved==null||stars<1||stars>3||!solved.isSolved())return null;
        for(StageResult prior:results)if(prior.victoryId.equals(victoryId)){TacticalChapterCatalog.Layout old=TacticalChapterCatalog.find(prior.layoutId);return prior.stars==stars&&old.matches(solved.level)&&Arrays.equals(prior.history,solved.history())?this:null;}
        if(finished()||selected==null||!currentVictoryId().equals(victoryId)||!selected.matches(solved.level))return null;
        Board replay=new Board(selected.level());if(!replay.restoreHistory(solved.history())||!replay.isSolved()||replay.gateMask()!=solved.gateMask()||replay.moves()!=solved.moves())return null;
        List<StageResult> wins=new ArrayList<>(results);wins.add(new StageResult(completedStages,selected.id,victoryId,stars,replay.history()));
        StringBuilder history=new StringBuilder();for(int action:replay.history()){if(history.length()>0)history.append('.');history.append(action);}
        return next(null,used,comboSpent,wins,claimed,"W,"+victoryId+","+selected.id+","+stars+","+history);
    }
    public TacticalChapterRun recordWin(String victoryId,String scope,Board solved,int stars,String expectedEncoded){return encode().equals(expectedEncoded)&&scope!=null&&scope.equals(currentBoardScope())?recordWin(victoryId,solved,stars):null;}
    public int copiesReward(){return finished()?TacticalChapterCatalog.COPIES_REWARD:0;}
    public int homeContributionReward(){return finished()?TacticalChapterCatalog.HOME_CONTRIBUTION:0;}
    public int coinsReward(){return finished()?TacticalChapterCatalog.COINS_REWARD:0;}
    public String souvenirId(){return finished()?TacticalChapterCatalog.souvenirId(episode):null;}
    public String completionRequestId(){return "tactical:"+id;}
    /** Rewards and claimed marker belong in one GameStore transaction, not inside this pure contract. */
    public TacticalChapterRun claim(){if(!finished())return null;return claimed?this:next(null,used,comboSpent,results,true,"Q");}
    public String encode(){return TacticalChapterCatalog.VERSION+"|"+id+"|"+seed+"|"+episode.name()+"|"+capacity+"|"+String.join(";",events);}
    public static TacticalChapterRun decode(String encoded){
        if(encoded==null||encoded.length()>16384)return null;
        try{
            String[] fields=encoded.split("\\|",-1);if(fields.length!=6||!TacticalChapterCatalog.VERSION.equals(fields[0]))return null;
            TacticalChapterRun run=start(fields[1],Long.parseLong(fields[2]),TacticalChapterCatalog.Episode.valueOf(fields[3]),Integer.parseInt(fields[4]));if(run==null)return null;
            if(!fields[5].isEmpty()){
                String[] events=fields[5].split(";",-1);if(events.length>20)return null;
                for(String event:events){String[] parts=event.split(",",-1);
                    if(parts.length==2&&parts[0].equals("C"))run=run.chooseRoute(parts[1]);
                    else if(parts.length==3&&parts[0].equals("T"))run=run.useTalent(Integer.parseInt(parts[1]),parts[2]);
                    else if(parts.length==2&&parts[0].equals("R"))run=run.useCombo(parts[1]);
                    else if(parts.length==5&&parts[0].equals("W")){
                        TacticalChapterCatalog.Layout layout=TacticalChapterCatalog.find(parts[2]);if(layout==null)return null;
                        String[] values=parts[4].split("\\.",-1);if(values.length!=layout.level().tiles.size())return null;
                        int[] history=new int[values.length];for(int i=0;i<values.length;i++)history[i]=Integer.parseInt(values[i]);
                        Board proof=new Board(layout.level());if(!proof.restoreHistory(history))return null;run=run.recordWin(parts[1],proof,Integer.parseInt(parts[3]));
                    }else if(parts.length==1&&parts[0].equals("Q"))run=run.claim();else return null;
                    if(run==null)return null;
                }
            }
            return encoded.equals(run.encode())?run:null;
        }catch(IllegalArgumentException invalid){return null;}
    }
}
