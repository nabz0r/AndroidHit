package com.androidhit.lumadrift.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.androidhit.lumadrift.engine.Board;
import com.androidhit.lumadrift.engine.MoveBudget;
import java.util.HashMap;
import java.util.Map;

/** Isolated native chapter. It never reads or replaces a campaign session, energy or ownership. */
public final class TacticalChapterSession122 {
    public static final String PREFS="luma_tactical122", STATE_KEY="state", VERSION="tactical_session122_1";
    public static final int COMPLETION_COINS=TacticalChapterCatalog.COINS_REWARD;
    private final SharedPreferences prefs;
    private final GameStore store;
    private TacticalChapterRun run;
    private Board board;
    private MoveBudget budget;
    private int undos,attempt,hint=-1,lastReward;
    private long hintUntil;
    private boolean corrupt;

    public TacticalChapterSession122(Context context,GameStore store){
        if(context==null||store==null)throw new IllegalArgumentException("Context and existing GameStore required");
        prefs=context.getApplicationContext().getSharedPreferences(PREFS,Context.MODE_PRIVATE);this.store=store;reload();
    }
    public TacticalChapterSession122(Context context){this(context,new GameStore(context));}
    public TacticalChapterRun run(){synchronized(LocalSaveTransactions.LOCK){return run;}}
    /** UI gets read copies; only this session can mutate the persistable engine/ledger tuple. */
    public Board board(){synchronized(LocalSaveTransactions.LOCK){return copy(board);}}
    public MoveBudget budget(){synchronized(LocalSaveTransactions.LOCK){return budget==null?null:budget.copy();}}
    public int undos(){synchronized(LocalSaveTransactions.LOCK){return undos;}}
    public int attempt(){synchronized(LocalSaveTransactions.LOCK){return attempt;}}
    public boolean isCorrupt(){synchronized(LocalSaveTransactions.LOCK){return corrupt;}}
    public int lastRewardCoins(){synchronized(LocalSaveTransactions.LOCK){return lastReward;}}
    public int hintId(){synchronized(LocalSaveTransactions.LOCK){long now=System.currentTimeMillis();return hint>=0&&board!=null&&board.isPresent(hint)&&now<=hintUntil&&now>=hintUntil-8000?hint:-1;}}
    public boolean hasSouvenir(TacticalChapterCatalog.Episode episode){synchronized(LocalSaveTransactions.LOCK){if(episode==null)return false;Object saved=prefs.getAll().get(souvenirKey(episode));return Boolean.TRUE.equals(saved)||(run!=null&&run.episode==episode&&run.finished()&&run.claimed);}}

    /** An active sortie resumes. Switching away cannot silently reset its shared reserve. */
    public boolean startEpisode(TacticalChapterCatalog.Episode episode){
        if(episode==null)return false;reload();
        synchronized(LocalSaveTransactions.LOCK){
            refreshLocked();
            if(run!=null&&!run.finished())return run.episode==episode;
            if(run!=null&&run.finished()&&!run.claimed)return false;
            Object oldSerial=prefs.getAll().get("serial");int serial=oldSerial instanceof Integer?(Integer)oldSerial:0;
            if(serial<0||serial==Integer.MAX_VALUE)return false;serial++;
            TacticalChapterRun next=TacticalChapterRun.start("t122_"+episode.name().toLowerCase(java.util.Locale.ROOT)+"_"+serial,System.currentTimeMillis()^serial,episode);
            persistLocked(next,null,null,0,0,-1,0,serial,false);return true;
        }
    }
    public boolean choose(String layoutId){
        synchronized(LocalSaveTransactions.LOCK){
            refreshLocked();if(run==null)return false;TacticalChapterRun next=run.chooseRoute(layoutId);if(next==null)return false;
            if(next==run)return board!=null;
            Board fresh=new Board(next.currentLevel());persistLocked(next,fresh,MoveBudget.start(0,fresh.level),0,0,-1,0,null,false);return true;
        }
    }
    public Board.MoveResult move(int pieceId){
        synchronized(LocalSaveTransactions.LOCK){
            refreshLocked();if(board==null||budget==null||board.isSolved()||Board.isGateAction(pieceId))return null;
            Board next=copy(board);MoveBudget ledger=budget.copy();Board.MoveResult result=ledger.attempt(next,pieceId);
            if(result==null||!result.success)return result;
            persistLocked(run,next,ledger,undos,attempt,-1,0,null,false);return result;
        }
    }
    /** Undo has no coin debit; policy6 deliberately keeps the action previously spent on that exit. */
    public boolean undo(){
        synchronized(LocalSaveTransactions.LOCK){
            refreshLocked();if(board==null||budget==null||undos==Integer.MAX_VALUE)return false;
            Board next=copy(board);MoveBudget ledger=budget.copy();if(!ledger.undo(next,undos))return false;
            persistLocked(run,next,ledger,undos+1,attempt,-1,0,null,false);return true;
        }
    }
    public TacticalChapterAction.Draft useTalent(int slot){return useTalent(slot,false);}
    public TacticalChapterAction.Draft useCombo(){return useTalent(-1,true);}
    /** Exact search is confined to explicitly requested certified assistance, not passive cues or normal moves. */
    public TacticalChapterAction.Draft useTalent(int slot,boolean combo){
        synchronized(LocalSaveTransactions.LOCK){
            refreshLocked();if(run==null||board==null||budget==null)return null;
            if(combo?!run.comboReady():!run.canUse(slot))return null;
            CompanionBattle.Definition talent=combo?TacticalChapterCatalog.talent(0):TacticalChapterCatalog.talent(slot);
            if(talent==null)return null;
            int[] plan=!combo&&talent.effect==CompanionBattle.Effect.REWIND?null:board.solve();
            TacticalChapterAction.Draft draft=TacticalChapterAction.prepare(board,budget,undos,run,slot,combo,plan,run.encode());
            if(draft==null)return null;
            int nextHint=draft.hintId;long until=nextHint<0?0:System.currentTimeMillis()+draft.hintDurationMs;
            persistLocked(draft.run,draft.board,draft.budget,draft.undos,attempt,nextHint,until,null,false);return draft;
        }
    }
    /** Retry only this layout. Used talents, Charge and Resonance remain consumed across retries. */
    public boolean resetAttempt(){
        synchronized(LocalSaveTransactions.LOCK){
            refreshLocked();if(run==null||run.currentLayout()==null||attempt==Integer.MAX_VALUE)return false;
            Board fresh=new Board(run.currentLevel());persistLocked(run,fresh,MoveBudget.start(0,fresh.level),0,attempt+1,-1,0,null,false);return true;
        }
    }
    public boolean finishIfSolved(){
        boolean finished;
        synchronized(LocalSaveTransactions.LOCK){refreshLocked();finished=run!=null&&run.finished()||advanceSolvedLocked();}
        if(finished)settleReward();return finished;
    }
    private boolean advanceSolvedLocked(){
        if(run==null||board==null||!board.isSolved())return false;
        TacticalChapterRun advanced=run.recordWin(run.currentVictoryId(),board,undos==0?3:undos==1?2:1);if(advanced==null)return false;
        persistLocked(advanced,null,null,0,0,-1,0,null,false);
        return true;
    }
    /** Finished unclaimed state is durable before wallet settlement. Its fixed milestone recovers a crash between files. */
    private void settleReward(){
        String runId;TacticalChapterCatalog.Episode episode;
        synchronized(LocalSaveTransactions.LOCK){refreshLocked();if(run==null||!run.finished()||run.claimed)return;runId=run.id;episode=run.episode;}
        // Never hold the preference lock while calling GameStore: its grant lock has the opposite order.
        int earned=store.grantMilestone("tactical122_"+episode.name().toLowerCase(java.util.Locale.ROOT),COMPLETION_COINS);
        synchronized(LocalSaveTransactions.LOCK){refreshLocked();lastReward=earned;if(run!=null&&run.id.equals(runId)&&run.finished()&&!run.claimed)persistLocked(run.claim(),null,null,0,0,-1,0,null,true);}
    }
    public void save(){synchronized(LocalSaveTransactions.LOCK){refreshLocked();if(run!=null)persistLocked(run,board,budget,undos,attempt,hint,hintUntil,null,false);}}
    /** Cold recreation also settles a durable last exit or an interrupted fixed completion reward. */
    public boolean reload(){synchronized(LocalSaveTransactions.LOCK){refreshLocked();if(board!=null&&board.isSolved())advanceSolvedLocked();}settleReward();synchronized(LocalSaveTransactions.LOCK){return !corrupt;}}
    private void refreshLocked(){
        LocalSaveTransactions.flush(prefs);Object raw=prefs.getAll().get(STATE_KEY);
        if(raw==null){clearLocked(false);return;}
        if(!(raw instanceof String)||!decodeLocked((String)raw))clearLocked(true);
    }
    private void clearLocked(boolean invalid){run=null;board=null;budget=null;undos=0;attempt=0;hint=-1;hintUntil=0;corrupt=invalid;}
    private static Board copy(Board from){if(from==null)return null;Board clone=new Board(from.level);if(!clone.restoreHistory(from.history()))throw new IllegalStateException("Invalid live chapter board");return clone;}
    private static String souvenirKey(TacticalChapterCatalog.Episode episode){return "souvenir_"+episode.name().toLowerCase(java.util.Locale.ROOT);}
    private static String encode(TacticalChapterRun run,Board board,MoveBudget budget,int undos,int attempt,int hint,long hintUntil){
        StringBuilder history=new StringBuilder();if(board!=null)for(int id:board.history()){if(history.length()>0)history.append(',');history.append(id);}
        String ledger="-";if(budget!=null){MoveBudget.Snapshot s=budget.snapshot();ledger=s.policyVersion+","+s.mode+","+s.used+","+s.extensions+","+s.extraActions+","+undos;}
        return VERSION+'\n'+run.encode()+'\n'+(board==null?"-":history.toString())+'\n'+ledger+'\n'+attempt+","+hint+","+hintUntil;
    }
    private void persistLocked(TacticalChapterRun next,Board nextBoard,MoveBudget nextBudget,int nextUndos,int nextAttempt,int nextHint,long nextHintUntil,Integer serial,boolean souvenir){
        if(next==null||(next.currentLayout()==null)!=(nextBoard==null)||(nextBoard==null)!=(nextBudget==null)
                ||nextBoard!=null&&!next.currentLayout().matches(nextBoard.level))throw new IllegalArgumentException("Coherent chapter tuple required");
        String encoded=encode(next,nextBoard,nextBudget,nextUndos,nextAttempt,nextHint,nextHintUntil);
        Map<String,Object> values=new HashMap<>();values.put(STATE_KEY,encoded);if(serial!=null)values.put("serial",serial);if(souvenir)values.put(souvenirKey(next.episode),true);
        LocalSaveTransactions.write(prefs,values);
        run=next;board=copy(nextBoard);budget=nextBudget==null?null:nextBudget.copy();undos=nextUndos;attempt=nextAttempt;hint=nextHint;hintUntil=nextHintUntil;corrupt=false;
    }
    private boolean decodeLocked(String encoded){
        if(encoded==null||encoded.length()>24576)return false;
        try{
            String[] lines=encoded.split("\n",-1);if(lines.length!=5||!VERSION.equals(lines[0]))return false;
            TacticalChapterRun saved=TacticalChapterRun.decode(lines[1]);if(saved==null)return false;
            String[] ui=lines[4].split(",",-1);if(ui.length!=3)return false;int retries=Integer.parseInt(ui[0]),cue=Integer.parseInt(ui[1]);long until=Long.parseLong(ui[2]);
            if(retries<0||cue< -1||until<0||(cue==-1)!=(until==0))return false;
            Board restored=null;MoveBudget ledger=null;int returns=0;
            if(saved.currentLayout()==null){if(!lines[2].equals("-")||!lines[3].equals("-")||retries!=0||cue!=-1)return false;}
            else {
                if(lines[2].equals("-")||lines[3].equals("-"))return false;restored=new Board(saved.currentLevel());
                String[] pieces=lines[2].isEmpty()?new String[0]:lines[2].split(",",-1);if(pieces.length>restored.level.tiles.size())return false;
                int[] history=new int[pieces.length];for(int i=0;i<history.length;i++)history[i]=Integer.parseInt(pieces[i]);if(!restored.restoreHistory(history))return false;
                String[] counters=lines[3].split(",",-1);if(counters.length!=6)return false;
                int policy=Integer.parseInt(counters[0]),mode=Integer.parseInt(counters[1]),used=Integer.parseInt(counters[2]),extensions=Integer.parseInt(counters[3]),extra=Integer.parseInt(counters[4]);returns=Integer.parseInt(counters[5]);
                if(policy!=6||mode!=0||extensions!=0||extra!=0)return false;
                ledger=MoveBudget.restoreVersion(policy,mode,restored.level,used,extensions,extra,restored.moves(),returns);if(ledger==null)return false;
                if(cue>=0&&(!saved.used(0)||!restored.isPresent(cue)))return false;
            }
            if(!encoded.equals(encode(saved,restored,ledger,returns,retries,cue,until)))return false;
            run=saved;board=restored;budget=ledger;undos=returns;attempt=retries;hint=cue;hintUntil=until;corrupt=false;return true;
        }catch(IllegalArgumentException invalid){return false;}
    }
}
