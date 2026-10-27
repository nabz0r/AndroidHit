package com.androidhit.lumadrift;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.accessibility.*;
import com.androidhit.lumadrift.audio.AudioEngine;
import com.androidhit.lumadrift.data.*;
import com.androidhit.lumadrift.engine.*;
import com.androidhit.lumadrift.sharing.ShareComposer;
import com.androidhit.lumadrift.ui.*;
import com.androidhit.lumadrift.identity.*;
import java.util.*;

/** Original puzzle presentation, generated environment and adaptive canvas UI. AGPL-3.0. */
// Budget/board/receipt commits deliberately finish before a paid continuation is acknowledged.
@android.annotation.SuppressLint("ApplySharedPref")
public final class LumaView extends View {
    private final android.text.TextPaint p = new android.text.TextPaint(3);
    private final Typeface bold = Typeface.create("sans-serif-black", Typeface.NORMAL);
    private final Typeface medium = Typeface.create("sans-serif-medium", Typeface.NORMAL);
    private final Typeface regular = Typeface.create("sans-serif", Typeface.NORMAL);
    private final GameStore store;
    private final SharedPreferences prefs;
    private final AudioEngine audio;
    private final WorldBackdrop backdrop;
    private final GoogleAccountController identity;
    private SessionSnapshot profileSnapshot;
    private ExperienceLayout layout;
    private float viewportH=840, contentScroll=0, drawingScroll=0, drawingClipBottom=840, scrollAtDown=0;
    private boolean scrolling=false,navigatingBack=false;
    private final ArrayDeque<String> menuHistory=new ArrayDeque<>();
    private int inventoryFilter=0, gearSlotFilter=-1, moveStreak=0;
    private int briefingIndex=1,briefingMode=0,briefingEvent=0;
    private Level briefingLevel;
    private String briefingReturn="home";
    private long gatePulseUntil=0,successUntil=0;
    private float successX=195,successY=350;

    private final MetaProgress meta;
    private final PlayJournal journal;
    private int cachedStars,cachedCompleted,cachedEconomyProgress,journeyTab=0,xpReward=0;
    private String friendCode="";
    private long activeElapsed=0,activeStarted=0;
    private final ArrayList<Hit> hits = new ArrayList<>();
    private final ArrayList<Flight> flights = new ArrayList<>();
    private final Random random = new Random(72);
    private Catalog.SkinItem skin;
    private Catalog.CharacterItem character;
    private String screen = "home", toast = "";
    private float scale=1, ox=0, top=0, H=820, bottom=0, bx=37, by=250, cell=55;
    private int safeTop, safeBottom, chapter=0, collectionTab=0, collectionPage=0, eventTab=0;
    private Board board;
    private MoveBudget moveBudget;
    private String runId="", pendingContinuation="", lastAppliedContinuation="";
    private int shieldCharges=0,shopPage=0,gearGrace=0,flowRemaining=0;
    private int contentVersion=4;
    private String lastPowerRequest="",profileName="";
    private boolean profileSignedIn=false,collectionOwnedOnly=false,flowExecuting=false,powerSaveBlocked=false;
    private long effectsPausedAt=0;
    private final PowerUsePolicy powerGate=new PowerUsePolicy();
    private long flowScheduledGeneration=-1;
    private AvatarLoadout loadout;
    private AvatarGearCatalog.Progress gearProgress;
    private long focusUntil=0,trailUntil=0;
    private int[] trail=new int[0];
    private final HashSet<Integer> safeHighlights=new HashSet<>();
    private int levelIndex=1, mode=0, eventIndex=0, hintsUsed=0, undos=0, reward=0, resultStars=0;
    private int coachId=-1;
    private int selected=-1, hintId=-1, shakeId=-1, focused=-1, hovered=-1;
    private long toastUntil, hintUntil, visionUntil, shakeUntil, winAt, frameTime;
    private boolean running=true, paused=false, won=false, help=false, reduceMotion=false;
    private Hit pressed;
    private long actionGeneration=0;
    private float downX, downY;
    private final int PEACH=0xffffb398, WHITE=0xfff7f5ff, INK=0xff14182e, GOLD=0xffffd992;
    private static class Hit {
        RectF r; String name; Runnable action;
        Hit(RectF r,String n,Runnable a){this.r=r;name=n;action=a;}
    }
    private static class Flight {
        Tile tile; long at; float x,y; int color;
        Flight(Tile t,long a,float x,float y,int c){tile=t;at=a;this.x=x;this.y=y;color=c;}
    }
    public LumaView(Context c) {
        super(c); store=new GameStore(c, new GameStore.ExternalOwnership() {
            public boolean ownsCharacter(String id) { return com.androidhit.lumadrift.wallet.ProductionWalletClient.ownsCharacter(c,id); }
            public boolean ownsSkin(String id) { return com.androidhit.lumadrift.wallet.ProductionWalletClient.ownsSkin(c,id); }
        }); prefs=c.getSharedPreferences("luma_session",0);
        meta=new MetaProgress(c);journal=new PlayJournal(c);
        audio=new AudioEngine(c);backdrop=new WorldBackdrop(c);
        identity=new GoogleAccountController((Activity)c,snapshot->{profileSnapshot=snapshot;profileSignedIn=snapshot.signedIn;profileName=snapshot.displayName;postInvalidate();});
        profileSnapshot=identity.snapshot();chapter=(store.unlockedLevel()-1)/10;
        setFocusable(true); setFocusableInTouchMode(true);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        reduceMotion=prefs.getBoolean("reduce_motion",false);
        syncStyle(); restoreSession();freezeEffects();
    }
    private String t(String key,Object... args){
        if(!com.androidhit.lumadrift.commerce.CommerceConfig.MODE_SANDBOX&&key.equals("commerce_entry"))key="commerce_title";
        return Strings.get(store.resolvedLanguage(),key,args);
    }
    private String u(String key,Object... args){return UiStrings.get(store.resolvedLanguage(),key,args);}
    private String e(String key,Object...args){return ExperienceStrings.get(store.resolvedLanguage(),key,args);}
    private String worldName(int level){int zone=(level-1)/30;return u("zone",zone+1)+" · "+t("world_"+(zone%8+1));}
    private void openAccount(){getContext().startActivity(new android.content.Intent(getContext(),com.androidhit.lumadrift.identity.AccountActivity.class));}
    private void syncStyle(){
        skin=Catalog.skin(store.equippedSkin()); if(skin==null)skin=Catalog.SKINS[0];
        character=Catalog.character(store.equippedCharacter());if(character==null)character=Catalog.CHARACTERS[0];
        try{store.claimAvatarGearUnlocks();}catch(IllegalStateException failed){/* Keep the last durable wardrobe. */}
        loadout=store.avatarLoadout();gearProgress=store.avatarGearProgress();
        profileSignedIn=profileSnapshot!=null&&profileSnapshot.signedIn;
        profileName=profileSignedIn?profileSnapshot.displayName:"";
        audio.setSfxEnabled(store.soundEnabled());audio.setMusicEnabled(store.musicEnabled());cachedStars=store.totalStars();cachedCompleted=store.completedLevels();cachedEconomyProgress=store.economyProgress();
    }
    public void setSafeInsets(int a,int b){safeTop=a;safeBottom=b;invalidate();}
    public void resumeApp(){running=true;identity.onResume();thawEffects();if(powerSaveBlocked){powerSaveBlocked=!saveSession();if(!powerSaveBlocked)reconcilePowerReservation();}if(board!=null&&board.isSolved()&&!won)finishLevel();resumeFlow();meta.recordVisit();if(screen.equals("play")&&!paused&&!help&&!won&&!outOfMoves()&&activeStarted==0)activeStarted=SystemClock.uptimeMillis();applyPendingContinuation();if(prefs.getBoolean("session_migration_notice",false)){notice(t("moves_migration"));prefs.edit().putBoolean("session_migration_notice",false).commit();}log("session_start","version",BuildConfig.VERSION_NAME,"language",store.resolvedLanguage());syncStyle();audio.resume();invalidate();}
    public void pauseApp(){identity.onPause();actionGeneration++;freezeEffects();stopTimer();saveSession();running=false;audio.pause();}
    public void release(){identity.close();actionGeneration++;running=false;audio.release();backdrop.close();}
    public String screenName(){return screen;}
    public void saveUiState(Bundle out){
        out.putString("screen",screen);out.putBoolean("ui_paused",paused);out.putBoolean("ui_help",help);out.putBoolean("ui_restart",restartConfirm);out.putInt("chapter",chapter);out.putInt("journey_tab",journeyTab);out.putInt("collection_tab",collectionTab);out.putInt("collection_page",collectionPage);out.putInt("inventory_filter",inventoryFilter);out.putInt("gear_slot",gearSlotFilter);out.putInt("event_tab",eventTab);out.putInt("shop_page",shopPage);out.putFloat("content_scroll",contentScroll);out.putStringArrayList("menu_history",new ArrayList<>(menuHistory));
        if(briefingLevel!=null){out.putInt("preview_index",briefingIndex);out.putInt("preview_mode",briefingMode);out.putInt("preview_event",briefingEvent);out.putString("preview_return",briefingReturn);}
    }
    public void restoreUiState(Bundle state){
        chapter=Math.max(0,state.getInt("chapter",chapter));journeyTab=state.getInt("journey_tab",0)==1?1:0;collectionTab=Math.max(0,Math.min(3,state.getInt("collection_tab",0)));collectionPage=Math.max(0,state.getInt("collection_page",0));inventoryFilter=Math.max(0,Math.min(2,state.getInt("inventory_filter",0)));gearSlotFilter=Math.max(-1,Math.min(2,state.getInt("gear_slot",-1)));eventTab=Math.max(0,Math.min(2,state.getInt("event_tab",0)));shopPage=Math.max(0,Math.min(1,state.getInt("shop_page",0)));
        if(state.containsKey("preview_index")){briefingMode=Math.max(0,Math.min(3,state.getInt("preview_mode",0)));briefingIndex=Math.max(1,Math.min(briefingMode==3?BalancedCatalog.REMIX_COUNT:BalancedCatalog.LEVEL_COUNT,state.getInt("preview_index",1)));briefingEvent=Math.max(0,Math.min(briefingMode==1?2:6,state.getInt("preview_event",0)));briefingReturn=state.getString("preview_return","home");briefingLevel=briefingMode==0?BalancedCatalog.get(briefingIndex):briefingMode==3?BalancedCatalog.remix(briefingIndex):LevelCatalog.event(briefingMode==1?store.dailySeed(briefingEvent):store.weeklySeed(briefingEvent));}
        restoreScreen(state.getString("screen"));if(screen.equals("play")){paused=state.getBoolean("ui_paused",false);help=state.getBoolean("ui_help",false);restartConfirm=paused&&state.getBoolean("ui_restart",false);if(paused||help){actionGeneration++;stopTimer();freezeEffects();}}menuHistory.clear();ArrayList<String> history=state.getStringArrayList("menu_history");if(history!=null)for(String item:history)if(isMenu(item))menuHistory.addLast(item);contentScroll=Math.max(0,state.getFloat("content_scroll",0));
    }
    private boolean isMenu(String value){return Arrays.asList("home","journey","collection","events","shop","settings","briefing","missions","journal").contains(value);}
    public void restoreScreen(String name){if(name!=null&&(isMenu(name)||name.equals("play")&&board!=null))go(name);}
    public void back(){actionGeneration++;
        if(help){setHelp(false);invalidate();return;}
        if(screen.equals("play")){
            if(won){go("journey");return;}
            setPaused(!paused);restartConfirm=false;saveSession();invalidate();return;
        }
        if(!screen.equals("home")){backMenu();return;}
        ((Activity)getContext()).moveTaskToBack(true);
    }
    private void sfx(AudioEngine.Sound s){audio.play(s);}
    private void haptic(){if(store.hapticsEnabled())performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);}
    private void go(String s){
        if(!navigatingBack&&!screen.equals(s)&&!screen.equals("play"))menuHistory.push(screen);
        contentScroll=0;scrolling=false;
        if(screen.equals("play")&&!s.equals("play")&&board!=null&&!won)log("level_exit","mode",""+mode,"level",""+levelIndex,"remaining",""+board.remainingCount(),"event_index",""+eventIndex,"period",eventPeriod);
        stopTimer();actionGeneration++;screen=s;paused=false;restartConfirm=false;help=false;selected=-1;pressed=null;focused=hovered=-1;hits.clear();syncStyle();
        if(s.equals("play")&&!won&&running&&!outOfMoves()){thawEffects();activeStarted=SystemClock.uptimeMillis();resumeFlow();}else freezeEffects();
        invalidate();if(Build.VERSION.SDK_INT>=28)setAccessibilityPaneTitle(s.equals("briefing")?e("objective"):t(s));sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
    }
    private void goRoot(String destination){menuHistory.clear();go(destination);menuHistory.clear();}
    private void backMenu(){String destination=menuHistory.isEmpty()?"home":menuHistory.pop();navigatingBack=true;try{go(destination);}finally{navigatingBack=false;}}
    private void log(String event,String... fields){Map<String,String> values=new HashMap<>();for(int i=0;i+1<fields.length;i+=2)values.put(fields[i],fields[i+1]);journal.record(event,values);}
    private void stopTimer(){if(activeStarted>0){activeElapsed+=Math.max(0,SystemClock.uptimeMillis()-activeStarted);activeStarted=0;}}
    private long elapsed(){return activeElapsed+(activeStarted>0?Math.max(0,SystemClock.uptimeMillis()-activeStarted):0);}
    private void setPaused(boolean value){actionGeneration++;if(value){stopTimer();freezeEffects();}paused=value;if(!value){thawEffects();resumeFlow();}if(!value&&running&&!help&&!won&&!outOfMoves()&&activeStarted==0)activeStarted=SystemClock.uptimeMillis();}
    private void setHelp(boolean value){actionGeneration++;if(value){stopTimer();freezeEffects();}help=value;if(!value){thawEffects();resumeFlow();}pressed=null;focused=hovered=-1;hits.clear();if(!value&&screen.equals("play")&&running&&!paused&&!won&&!outOfMoves()&&activeStarted==0)activeStarted=SystemClock.uptimeMillis();invalidate();}

    private void notice(String s){toast=s;toastUntil=SystemClock.uptimeMillis()+2700;announceForAccessibility(s);invalidate();}
    private void start(int index,int m,int e){
        if(m==3&&(!store.remixAvailable()||index>store.remixUnlockedLevel())){notice(t("remix_unlock"));return;}
        if(!prepareAbandon())return;
        stopTimer();activeElapsed=0;activeStarted=0;friendCode="";
        levelIndex=Math.max(1,Math.min(m==3?BalancedCatalog.REMIX_COUNT:BalancedCatalog.LEVEL_COUNT,index));contentVersion=4;moveStreak=0;mode=m;eventIndex=e;eventPeriod=m==1?store.dailyId():m==2?store.weeklyId():m==3?"remix-v1":"";
        Level l=m==0?BalancedCatalog.get(levelIndex):m==3?BalancedCatalog.remix(levelIndex):LevelCatalog.event(m==1?store.dailySeed(e):store.weeklySeed(e));
        board=new Board(l);resetMoveRun();coachId=m==0&&levelIndex==1?board.hint():-1;hintsUsed=undos=xpReward=0;won=paused=false;flights.clear();hintId=-1;visionUntil=0;winAt=0;
        goRoot("play");log("level_start","mode",""+m,"level",""+levelIndex,"event_index",""+eventIndex,"period",eventPeriod);saveSession();
    }
    public void openJournal(){go("journal");}
    private void openCommerce(){getContext().startActivity(new android.content.Intent(getContext(),com.androidhit.lumadrift.commerce.CommerceActivity.class));}
    public void openChallenge(String input){
        String raw=input==null?"":input.trim();
        try{if(raw.startsWith("lumadrift://")){android.net.Uri uri=android.net.Uri.parse(raw);if(!"challenge".equals(uri.getHost())||uri.getPathSegments().size()!=1||uri.getQuery()!=null||uri.getFragment()!=null){notice(t("challenge_invalid"));return;}raw=uri.getLastPathSegment();}}
        catch(RuntimeException invalid){notice(t("challenge_invalid"));return;}
        ChallengeCode code=ChallengeCode.parse(raw);if(code==null){notice(t("challenge_invalid"));return;}
        if(!prepareAbandon())return;
        stopTimer();activeElapsed=activeStarted=0;friendCode=raw;mode=4;eventIndex=0;eventPeriod="";levelIndex=code.kind==ChallengeCode.Kind.EVENT?1:(int)code.value;
        coachId=-1;contentVersion=4;moveStreak=0;board=new Board(challengeLevel(code));resetMoveRun();
        hintsUsed=undos=xpReward=0;won=paused=false;flights.clear();hintId=-1;visionUntil=0;winAt=0;goRoot("play");log("challenge_open","kind",code.kind.name());saveSession();
    }
    private String currentCode(){
        if(mode==4&&!friendCode.isEmpty())return friendCode;
        if(board==null)return ChallengeCode.encodeBalancedV3(Math.min(BalancedCatalog.LEVEL_COUNT,store.unlockedLevel()));
        if(mode==0)return contentVersion==2?ChallengeCode.encodeBalanced(levelIndex):contentVersion==3?ChallengeCode.encodeBalancedV2(levelIndex):ChallengeCode.encodeBalancedV3(levelIndex);
        if(mode==3)return contentVersion==2?ChallengeCode.encodeBalancedRemix(levelIndex):contentVersion==3?ChallengeCode.encodeBalancedRemixV2(levelIndex):ChallengeCode.encodeBalancedRemixV3(levelIndex);
        return ChallengeCode.encodeEvent(board.level.seed);
    }
    private void shareBoard(){
        try{Level level=board==null?BalancedCatalog.get(Math.min(BalancedCatalog.LEVEL_COUNT,store.unlockedLevel())):board.level;ShareComposer.shareChallenge((Activity)getContext(),level,currentCode(),store.resolvedLanguage(),won?resultStars:0,character.color);log("share_handoff","kind",mode==4?"friend":""+mode);}
        catch(java.io.IOException|android.content.ActivityNotFoundException failure){notice(t("share_error"));}
    }
    private void importCode(){
        android.widget.EditText input=new android.widget.EditText(getContext());input.setSingleLine(true);input.setHint(t("import_hint"));input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(100)});input.setPadding(30,20,30,20);
        new android.app.AlertDialog.Builder(getContext()).setTitle(t("import_challenge")).setView(input).setPositiveButton(t("play"),(d,w)->openChallenge(input.getText().toString())).setNegativeButton(t("cancel"),(d,w)->{}).show();
    }
    private void continueGame(){if(board!=null&&!won&&!board.isSolved()){goRoot("play");return;}previewLevel(Math.min(BalancedCatalog.LEVEL_COUNT,store.unlockedLevel()),0,0);}
    private boolean saveSession(){
        SharedPreferences.Editor e=prefs.edit().putString("last_power_request",lastPowerRequest);
        if(board==null||won){return e.putBoolean("active",false).commit();}
        StringBuilder history=new StringBuilder();for(int id:board.history()){if(history.length()>0)history.append(',');history.append(id);}
        e.putBoolean("active",true).putInt("level",levelIndex).putInt("mode",mode).putInt("event",eventIndex)
            .putString("event_id",eventPeriod)
            .putString("history",history.toString()).putString("challenge_code",friendCode).putLong("elapsed_ms",elapsed()).putInt("hints",hintsUsed).putInt("undos",undos)
            .putInt("content_version",contentVersion).putInt("budget_policy",moveBudget.policyVersion()).putString("run_id",runId)
            .putInt("budget_used",moveBudget.used()).putInt("budget_extensions",moveBudget.extensions()).putInt("budget_extra",moveBudget.extraActions())
            .putString("pending_continuation",pendingContinuation).putString("last_continuation",lastAppliedContinuation).putInt("shield_charges",shieldCharges).putInt("gear_grace",gearGrace)
            .putInt("flow_remaining",flowRemaining).putInt("hint_id",hintId)
            .putLong("hint_ms",effectRemaining(hintUntil)).putLong("vision_ms",effectRemaining(visionUntil))
            .putLong("focus_ms",effectRemaining(focusUntil)).putLong("trail_ms",effectRemaining(trailUntil))
            .putString("trail_ids",joinIds(trail)).putString("focus_ids",joinIds(safeHighlights));
        return e.commit();
    }
    private void restoreSession(){
        lastPowerRequest=prefs.getString("last_power_request","");
        reconcilePowerReservation();
        if(!prefs.getBoolean("active",false))return;
        if(prefs.getInt("content_version",0)!=2&&prefs.getInt("content_version",0)!=3&&prefs.getInt("content_version",0)!=4){prefs.edit().putBoolean("active",false).putBoolean("session_migration_notice",true).commit();return;}
        try{
            contentVersion=prefs.getInt("content_version",2);
            int m=prefs.getInt("mode",0), e=prefs.getInt("event",0), l=prefs.getInt("level",1);
            String id=prefs.getString("event_id","");
            if(m==1&&!id.equals(store.dailyId())||m==2&&!id.equals(store.weeklyId()))return;
            if(m<0||m>4||m==0&&!store.isLevelUnlocked(l)||m==1&&(e<0||e>2)||m==2&&(e<0||e>6)||m==3&&(!store.remixAvailable()||l<1||l>store.remixUnlockedLevel()))return;
            String savedCode=prefs.getString("challenge_code","");ChallengeCode friend=m==4?ChallengeCode.parse(savedCode):null;if(m==4&&friend==null)return;
            Level lev=m==0?(contentVersion==2?BalancedCatalog.legacyGet(l):contentVersion==3?BalancedCatalog.getV2(l):BalancedCatalog.get(l)):m==3?(contentVersion==2?BalancedCatalog.legacyRemix(l):contentVersion==3?BalancedCatalog.remixV2(l):BalancedCatalog.remix(l)):m==4?challengeLevel(friend):LevelCatalog.event(m==1?store.dailySeed(e):store.weeklySeed(e));
            Board b=new Board(lev);String raw=prefs.getString("history","");String[] parts=raw.isEmpty()?new String[0]:raw.split(",");int[] moves=new int[parts.length];
            for(int i=0;i<moves.length;i++)moves[i]=Integer.parseInt(parts[i]);
            if(b.restoreHistory(moves)){
                int policy=prefs.getInt("budget_policy",0);
                MoveBudget restored=policy==1?MoveBudget.restoreLegacy(m,lev,prefs.getInt("budget_used",-1),prefs.getInt("budget_extensions",-1),prefs.getInt("budget_extra",-1),b.moves(),prefs.getInt("undos",0)):policy==2?MoveBudget.restore(m,lev,prefs.getInt("budget_used",-1),prefs.getInt("budget_extensions",-1),prefs.getInt("budget_extra",-1),b.moves(),prefs.getInt("undos",0)):null;
                String restoredRun=prefs.getString("run_id","");
                if(restored==null||!restoredRun.matches("[a-f0-9-]{36}"))return;
                moveBudget=restored;runId=restoredRun;pendingContinuation=prefs.getString("pending_continuation","");lastAppliedContinuation=prefs.getString("last_continuation","");
                if(!pendingContinuation.isEmpty()&&!pendingContinuation.matches("[ag]:"+runId+":\\d{1,2}"))pendingContinuation="";
                shieldCharges=Math.max(0,Math.min(3,prefs.getInt("shield_charges",0)));board=b;mode=m;eventIndex=e;levelIndex=l;eventPeriod=id;friendCode=savedCode;activeElapsed=Math.max(0,prefs.getLong("elapsed_ms",0));hintsUsed=prefs.getInt("hints",0);undos=prefs.getInt("undos",0);
                gearGrace=Math.max(0,Math.min(1,prefs.getInt("gear_grace",0)));flowRemaining=Math.max(0,Math.min(2,prefs.getInt("flow_remaining",0)));
                long now=SystemClock.uptimeMillis();hintId=prefs.getInt("hint_id",-1);hintUntil=now+Math.min(8000,Math.max(0,prefs.getLong("hint_ms",0)));
                visionUntil=now+Math.min(8000,Math.max(0,prefs.getLong("vision_ms",0)));focusUntil=now+Math.min(10000,Math.max(0,prefs.getLong("focus_ms",0)));trailUntil=now+Math.min(12000,Math.max(0,prefs.getLong("trail_ms",0)));
                trail=parseIds(prefs.getString("trail_ids",""),5);for(int safe:parseIds(prefs.getString("focus_ids",""),63))if(board.isPresent(safe))safeHighlights.add(safe);
                if(flowRemaining>0)powerGate.tryBegin("flow",false,now);
            }
        }catch(RuntimeException ignored){prefs.edit().putBoolean("active",false).apply();}
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);frameTime=SystemClock.uptimeMillis();
        layout=new ExperienceLayout(getWidth(),getHeight(),safeTop,safeBottom,getResources().getDisplayMetrics().density,getResources().getConfiguration().fontScale);
        scale=layout.scale;ox=layout.offsetX;top=safeTop;viewportH=layout.viewportHeight;H=layout.contentHeight;
        contentScroll=layout.clampScroll(contentScroll);
        boolean navigation=!screen.equals("play")&&!screen.equals("settings")&&!screen.equals("briefing");
        c.drawColor(skin.bg);c.save();c.translate(ox,top);c.scale(scale,scale);hits.clear();
        background(c);c.save();drawingScroll=contentScroll;drawingClipBottom=viewportH-(navigation?95:0);
        c.clipRect(0,0,390,drawingClipBottom);c.translate(0,-contentScroll);
        switch(screen){
            case "play":drawPlay(c);break;case "journey":drawJourney(c);break;
            case "briefing":drawBriefing(c);break;case "shop":drawShop(c);break;case "collection":drawCollection(c);break;
            case "events":drawEvents(c);break;case "settings":drawSettings(c);break;case "missions":drawMissions(c);break;case "journal":drawJournal(c);break;
            default:drawHome(c);
        }
        if(help)drawHelp(c);c.restore();drawingScroll=0;drawingClipBottom=viewportH;
        if(navigation){float contentHeight=H;H=viewportH;nav(c);H=contentHeight;}
        if(frameTime<toastUntil&&!screen.equals("play")){float y=Math.max(8,viewportH-(navigation?169:90));round(c,24,y,342,65,20,0xff303551);wrap(c,toast,195,y+25,302,14,WHITE,true,19);}
        if(focused>=0&&focused<hits.size()){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(WHITE);c.drawRoundRect(hits.get(focused).r,15,15,p);p.setStyle(Paint.Style.FILL);}
        if(layout.maximumScroll>0){float track=viewportH-24,thumb=Math.max(25,track*viewportH/H),yy=12+(track-thumb)*contentScroll/layout.maximumScroll;round(c,384,yy,3,thumb,2,alpha(WHITE,105));}
        c.restore();if(running)postInvalidateDelayed(reduceMotion?250:screen.equals("play")?20:50);
    }
    private void background(Canvas c){
        backdrop.draw(c,390,viewportH,screen.equals("play")||screen.equals("collection")||screen.equals("shop")||screen.equals("settings"),skin.bg);
        for(int i=0;i<18;i++){float x=(i*79+23)%390,y=(i*137+42)%Math.max(1,(int)viewportH);float pulse=reduceMotion?1f:.65f+.35f*(float)Math.sin(frameTime*.0008+i);p.setColor(alpha(WHITE,(int)(35*pulse)));c.drawCircle(x,y,i%4==0?1.4f:.7f,p);}
    }
    private static int alpha(int c,int a){return (c&0x00ffffff)|((Math.max(0,Math.min(255,a)))<<24);}
    private void round(Canvas c,float x,float y,float w,float h,float r,int color){p.setColor(color);p.setShader(null);p.setStyle(Paint.Style.FILL);c.drawRoundRect(x,y,x+w,y+h,r,r,p);}
    private void stroke(Canvas c,RectF r,float radius,int color,float width){p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(width);c.drawRoundRect(r,radius,radius,p);p.setStyle(Paint.Style.FILL);}
    private float minimumTextSize(){return layout==null?12:layout.minimumTextSize;}
    private void text(Canvas c,String str,float x,float y,float size,int color,boolean heavy){
        p.setShader(null);p.setTypeface(heavy?bold:regular);p.setTextSize(Math.max(minimumTextSize(),size));p.setColor(color);p.setTextAlign(Paint.Align.LEFT);c.drawText(str,x,y,p);
    }
    private void center(Canvas c,String str,float x,float y,float size,int color,boolean heavy){size=Math.max(minimumTextSize(),size);p.setTypeface(heavy?bold:regular);p.setTextSize(size);text(c,str,x-p.measureText(str)/2,y,size,color,heavy);}
    private void fit(Canvas c,String str,float x,float y,float size,float max,int color,boolean heavy,boolean centered){
        size=Math.max(minimumTextSize(),size);float minimum=minimumTextSize();p.setTypeface(heavy?bold:regular);p.setTextSize(size);float sw=p.measureText(str);
        if(sw>max)size=Math.max(minimum,size*max/sw);
        p.setTextSize(size);String visible=android.text.TextUtils.ellipsize(str,p,max,android.text.TextUtils.TruncateAt.END).toString();
        if(centered)center(c,visible,x,y,size,color,heavy);else text(c,visible,x,y,size,color,heavy);
    }
    private void wrap(Canvas c,String str,float x,float y,float max,float size,int color,boolean centered,float line){
        size=Math.max(minimumTextSize(),size);line=Math.max(line,size*1.3f);p.setTypeface(regular);p.setTextSize(size);StringBuilder row=new StringBuilder();float yy=y;
        for(String paragraph:str.split("\n",-1)){
            row.setLength(0);
            for(String word:paragraph.split(" ")){
                if(row.length()>0&&p.measureText(row+" "+word)>max){if(centered)center(c,row.toString(),x,yy,size,color,false);else text(c,row.toString(),x,yy,size,color,false);yy+=line;row.setLength(0);}
                if(row.length()>0)row.append(' ');row.append(word);
            }
            if(row.length()>0){if(centered)center(c,row.toString(),x,yy,size,color,false);else text(c,row.toString(),x,yy,size,color,false);}yy+=line;
        }
    }
    private void hit(float x,float y,float w,float h,String name,Runnable action){
        RectF r=new RectF(x,y-drawingScroll,x+w,y+h-drawingScroll);
        if(r.intersect(0,0,390,drawingClipBottom))hits.add(new Hit(r,name,action));
    }
    private void label(float x,float y,float w,float h,String name){hit(x,y,w,h,name,null);}
    private void button(Canvas c,String label,float x,float y,float w,float h,int color,Runnable action){
        round(c,x,y+4,w,h,18,alpha(0xff000000,60));round(c,x,y,w,h,18,color);
        round(c,x+10,y+3,w-20,1,1,alpha(WHITE,55));fit(c,label,x+w/2,y+h/2+6,17,w-24,INK,true,true);hit(x,y,w,h,label,action);
    }
    private void quietButton(Canvas c,String label,float x,float y,float w,Runnable action){round(c,x,y,w,45,15,alpha(skin.accent,24));fit(c,label,x+w/2,y+28,14,w-14,WHITE,true,true);hit(x,y,w,45,label,action);}
    private void header(Canvas c,String title,String subtitle){
        fit(c,title,24,54,30,285,WHITE,true,false);if(subtitle!=null)fit(c,subtitle,25,80,12,330,skin.muted,false,false);wallet(c,282,15);
    }
    private void wallet(Canvas c,float x,float y){hit(x,y,84,35,t("coins")+": "+store.coins(),()->{if(!screen.equals("shop"))go("shop");});round(c,x,y,84,35,18,alpha(skin.card,225));coin(c,x+18,y+17,8);fit(c,""+store.coins(),x+33,y+23,14,47,GOLD,true,false);}
    private void coin(Canvas c,float x,float y,float r){p.setColor(GOLD);c.drawCircle(x,y,r,p);p.setColor(0xffdfac61);p.setStrokeWidth(1.5f);p.setStyle(Paint.Style.STROKE);c.drawCircle(x,y,r*.7f,p);p.setStyle(Paint.Style.FILL);text(c,"•",x-r*.25f,y+r*.38f,r,GOLD,true);}
    private void icon(Canvas c,String name,float x,float y,float size,int color){
        p.setColor(color);p.setStrokeWidth(2.2f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);p.setStyle(Paint.Style.STROKE);float s=size/20;
        c.save();c.translate(x,y);c.scale(s,s);Path q=new Path();
        switch(name){
            case "profile":c.drawCircle(0,-5,4,p);q.moveTo(-8,9);q.cubicTo(-8,0,8,0,8,9);break;
            case "home":q.moveTo(-9,-1);q.lineTo(0,-9);q.lineTo(9,-1);q.moveTo(-6,-2);q.lineTo(-6,8);q.lineTo(6,8);q.lineTo(6,-2);break;
            case "journey":q.moveTo(-7,8);q.cubicTo(12,8,-12,-8,7,-8);c.drawCircle(-7,8,2,p);c.drawCircle(7,-8,2,p);break;
            case "events":q.moveTo(1,-10);q.lineTo(-7,2);q.lineTo(0,2);q.lineTo(-1,10);q.lineTo(8,-2);q.lineTo(1,-2);q.close();break;
            case "collection":q.moveTo(-9,7);q.lineTo(-9,-5);q.lineTo(-3,0);q.lineTo(0,-8);q.lineTo(4,0);q.lineTo(9,-5);q.lineTo(9,7);q.close();break;
            case "shop":c.drawRoundRect(-8,-3,8,9,2,2,p);q.moveTo(-4,-3);q.lineTo(-4,-7);q.quadTo(0,-12,4,-7);q.lineTo(4,-3);break;
            case "settings":c.drawCircle(0,0,7,p);c.drawCircle(0,0,2,p);for(int i=0;i<8;i++){double a=i*Math.PI/4;c.drawLine((float)Math.cos(a)*7,(float)Math.sin(a)*7,(float)Math.cos(a)*10,(float)Math.sin(a)*10,p);}break;
            case "back":q.moveTo(4,-7);q.lineTo(-4,0);q.lineTo(4,7);break;
            case "next":q.moveTo(-4,-7);q.lineTo(4,0);q.lineTo(-4,7);break;
            case "pause":c.drawLine(-4,-7,-4,7,p);c.drawLine(4,-7,4,7,p);break;
            case "undo":q.moveTo(-8,0);q.cubicTo(-3,-10,10,-8,8,2);q.cubicTo(7,7,3,9,-2,7);q.moveTo(-8,-6);q.lineTo(-8,0);q.lineTo(-2,0);break;
            case "retry":q.moveTo(7,-3);q.cubicTo(3,-11,-9,-7,-8,1);q.cubicTo(-7,9,4,11,8,3);q.moveTo(7,-9);q.lineTo(7,-3);q.lineTo(1,-3);break;
            case "hint":c.drawCircle(0,-3,6,p);q.moveTo(-3,4);q.lineTo(-3,7);q.lineTo(3,7);q.lineTo(3,4);q.moveTo(-2,10);q.lineTo(2,10);break;
            case "vision":q.moveTo(-10,0);q.quadTo(0,-12,10,0);q.quadTo(0,12,-10,0);c.drawCircle(0,0,3,p);break;
            case "flow":for(int i=-1;i<=1;i++){q.moveTo(i*6-2,-6);q.lineTo(i*6+3,0);q.lineTo(i*6-2,6);}break;
            case "shield":q.moveTo(0,-10);q.lineTo(8,-6);q.lineTo(6,4);q.quadTo(3,8,0,10);q.quadTo(-3,8,-6,4);q.lineTo(-8,-6);q.close();break;
            case "focus":c.drawCircle(0,0,6,p);c.drawCircle(0,0,2,p);c.drawLine(-10,0,-7,0,p);c.drawLine(7,0,10,0,p);c.drawLine(0,-10,0,-7,p);c.drawLine(0,7,0,10,p);break;
            case "trail":q.moveTo(-8,7);q.lineTo(-2,0);q.lineTo(2,3);q.lineTo(8,-7);q.moveTo(1,-7);q.lineTo(8,-7);q.lineTo(8,0);break;
            case "lock":c.drawRoundRect(-6,-1,6,9,2,2,p);q.moveTo(-4,-1);q.lineTo(-4,-5);q.cubicTo(-4,-11,4,-11,4,-5);q.lineTo(4,-1);break;
            case "check":q.moveTo(-7,0);q.lineTo(-2,5);q.lineTo(8,-6);break;
            case "sound":q.moveTo(-8,-3);q.lineTo(-4,-3);q.lineTo(1,-8);q.lineTo(1,8);q.lineTo(-4,3);q.lineTo(-8,3);q.close();q.moveTo(5,-5);q.quadTo(11,0,5,5);break;
            case "music":q.moveTo(-3,5);q.lineTo(-3,-7);q.lineTo(7,-9);q.lineTo(7,3);c.drawOval(-8,3,-1,8,p);c.drawOval(2,1,9,6,p);break;
            case "heart":q.moveTo(0,8);q.cubicTo(-18,-3,-7,-13,0,-5);q.cubicTo(7,-13,18,-3,0,8);break;
            case "help":c.drawCircle(0,0,10,p);q.moveTo(-3,-4);q.cubicTo(-1,-8,7,-5,2,0);q.lineTo(1,2);c.drawPoint(1,6,p);break;
            default:c.drawCircle(0,0,7,p);
        }c.drawPath(q,p);c.restore();p.setStyle(Paint.Style.FILL);
    }
    private void star(Canvas c,float x,float y,float r,int color){Path q=new Path();for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5;float rr=i%2==0?r:r*.47f;float xx=x+(float)Math.cos(a)*rr,yy=y+(float)Math.sin(a)*rr;if(i==0)q.moveTo(xx,yy);else q.lineTo(xx,yy);}q.close();p.setColor(color);c.drawPath(q,p);}
    private void mascot(Canvas c,float x,float y,float r,Catalog.CharacterItem who,boolean happy){
        c.save();c.translate(x,y);float bob=reduceMotion?0:(float)Math.sin(frameTime*.0018)*r*.035f;c.translate(0,bob);
        p.setAlpha(255);p.setShader(new RadialGradient(0,r*.9f,r*.9f,new int[]{alpha(who.color,60),0},null,Shader.TileMode.CLAMP));c.drawOval(-r*1.3f,r*.7f,r*1.3f,r*1.25f,p);p.setShader(null);
        if(who.shape>=6)drawCompanionBody(c,r,who);else{
        if(who.shape==1){Path ears=new Path();ears.moveTo(-r*.8f,-r*.3f);ears.lineTo(-r*.75f,-r*1.13f);ears.lineTo(-r*.15f,-r*.6f);ears.moveTo(r*.8f,-r*.3f);ears.lineTo(r*.75f,-r*1.13f);ears.lineTo(r*.15f,-r*.6f);p.setColor(who.color);c.drawPath(ears,p);}
        if(who.shape==3){round(c,-r*.7f,-r*1.65f,r*.44f,r*1.25f,r*.23f,who.color);round(c,r*.25f,-r*1.65f,r*.44f,r*1.25f,r*.23f,who.color);}
        if(who.shape==4){Path tail=new Path();tail.moveTo(-r*.5f,r*.3f);tail.lineTo(-r*1.6f,-r*.75f);tail.lineTo(-r*.2f,-r*.65f);p.setColor(alpha(who.color,110));c.drawPath(tail,p);}
        p.setAlpha(255);p.setShader(new LinearGradient(-r,-r,r,r,new int[]{blend(who.color,WHITE,.28f),who.color,blend(who.color,INK,.14f)},null,Shader.TileMode.CLAMP));
        if(who.shape==0){Path blob=new Path();for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5;float rr=i%2==0?r*1.12f:r*.81f;float xx=(float)Math.cos(a)*rr,yy=(float)Math.sin(a)*rr;if(i==0)blob.moveTo(xx,yy);else blob.lineTo(xx,yy);}blob.close();c.drawPath(blob,p);c.drawCircle(0,0,r*.83f,p);}
        else if(who.shape==2)c.drawRoundRect(-r,-r*.82f,r,r*.85f,r*.4f,r*.4f,p);
        else c.drawOval(-r,-r,r,r,p);p.setShader(null);
        if(who.shape==2){p.setColor(skin.card);c.drawRoundRect(-r*.75f,-r*.42f,r*.75f,r*.3f,r*.25f,r*.25f,p);p.setColor(who.color);c.drawCircle(0,-r*1.05f,r*.12f,p);}
        if(who.shape==5){for(int i=0;i<3;i++){p.setColor(who.color);c.drawCircle((i-1)*r*.55f,r*.78f,r*.27f,p);}}
        }
        if(who.shape==6)c.translate(-r*.28f,0);
        p.setColor(who.shape==2||who.shape==22?WHITE:INK);float ey=-r*.12f;boolean blink=!reduceMotion&&(frameTime/120)%43==0;
        if(happy||blink){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(r*.065f);c.drawArc(-r*.48f,ey-r*.12f,-r*.18f,ey+r*.16f,190,160,false,p);c.drawArc(r*.18f,ey-r*.12f,r*.48f,ey+r*.16f,190,160,false,p);p.setStyle(Paint.Style.FILL);}
        else{c.drawOval(-r*.43f,ey-r*.19f,-r*.24f,ey+r*.12f,p);c.drawOval(r*.24f,ey-r*.19f,r*.43f,ey+r*.12f,p);p.setColor(WHITE);c.drawCircle(-r*.31f,ey-r*.1f,r*.04f,p);c.drawCircle(r*.36f,ey-r*.1f,r*.04f,p);}
        p.setColor(alpha(0xffee6f92,100));c.drawOval(-r*.65f,r*.17f,-r*.37f,r*.3f,p);c.drawOval(r*.37f,r*.17f,r*.65f,r*.3f,p);
        p.setColor(INK);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(r*.045f);c.drawArc(-r*.17f,r*.15f,r*.17f,r*.42f,5,170,false,p);p.setStyle(Paint.Style.FILL);
        p.setColor(alpha(WHITE,120));c.drawOval(-r*.55f,-r*.75f,-r*.03f,-r*.52f,p);if(meta.badgeEquipped()&&who.id.equals(character.id))star(c,r*.75f,r*.67f,r*.25f,GOLD);if(who.id.equals(character.id)&&loadout!=null)drawLoadout(c,r,who.shape);c.restore();
    }
    /** Original silhouettes drawn in unit coordinates; no downloaded or licensed character art. */
    private void drawCompanionBody(Canvas c,float r,Catalog.CharacterItem who){
        c.save();c.scale(r,r);int color=who.color,light=blend(color,WHITE,.42f),dark=blend(color,INK,.35f);
        switch(who.shape){
            case 6: { // Crescent moon: the inner curve belongs to the silhouette, not a background cutout.
                Path q=new Path();q.moveTo(.55f,-.96f);q.cubicTo(-.05f,-1.3f,-1.12f,-.67f,-1.05f,.13f);q.cubicTo(-.99f,1.06f,.25f,1.25f,.85f,.54f);q.cubicTo(.12f,.72f,.04f,-.22f,.55f,-.96f);q.close();p.setColor(color);c.drawPath(q,p);star(c,.68f,-.57f,.22f,light);break;
            }
            case 7: // Fox, pointed ears and two cream cheek patches.
                companionPolygon(c,color,-.96f,.24f,-.97f,-1.04f,-.35f,-.65f,0,-.81f,.35f,-.65f,.97f,-1.04f,.96f,.24f,0,1.02f);
                companionPolygon(c,light,-.93f,.2f,-.12f,.13f,0,.83f);companionPolygon(c,light,.93f,.2f,.12f,.13f,0,.83f);break;
            case 8: // Owl, tufted outline and eye discs.
                companionPolygon(c,color,-.84f,-.22f,-.74f,-1.02f,-.25f,-.65f,.25f,-.65f,.74f,-1.02f,.84f,-.22f);
                companionOval(c,-.94f,-.79f,.94f,1.02f,color);companionOval(c,-.79f,-.59f,.04f,.38f,light);companionOval(c,-.04f,-.59f,.79f,.38f,light);
                companionPolygon(c,GOLD,-.11f,.23f,.11f,.23f,0,.43f);break;
            case 9: // Bee, translucent wings and striped abdomen.
                companionOval(c,-1.24f,-.98f,-.25f,.01f,alpha(WHITE,175));companionOval(c,.25f,-.98f,1.24f,.01f,alpha(WHITE,175));
                companionOval(c,-.88f,-.81f,.88f,.94f,color);round(c,-.69f,.45f,1.38f,.16f,.08f,dark);round(c,-.48f,.74f,.96f,.13f,.06f,dark);
                companionLine(c,-.3f,-.74f,-.42f,-1.1f,dark,.07f);companionLine(c,.3f,-.74f,.42f,-1.1f,dark,.07f);break;
            case 10: // Turtle, shell flippers and a scalloped ridge.
                for(int i=0;i<4;i++){float x=i%2==0?-1.01f:.59f,y=i<2?-.52f:.38f;companionOval(c,x,y,x+.42f,y+.61f,dark);}
                companionOval(c,-.76f,-1.07f,.76f,.8f,color);companionOval(c,-.68f,-.81f,.68f,.91f,light);
                companionLine(c,-.5f,.66f,0,.43f,dark,.055f);companionLine(c,0,.43f,.5f,.66f,dark,.055f);break;
            case 11: // Jellyfish, dome with five rounded tentacles.
                for(int i=0;i<5;i++)round(c,-.87f+i*.34f,.37f,.24f,.85f+(i%2)*.15f,.12f,i%2==0?color:light);
                companionOval(c,-1,-.98f,1,.75f,color);round(c,-1,.27f,2,.42f,.2f,light);break;
            case 12: // Moth, four broad wing lobes and antennae.
                companionOval(c,-1.35f,-1.04f,-.09f,.35f,color);companionOval(c,.09f,-1.04f,1.35f,.35f,color);
                companionOval(c,-1.06f,-.01f,-.09f,.94f,light);companionOval(c,.09f,-.01f,1.06f,.94f,light);
                companionOval(c,-.63f,-.76f,.63f,.86f,color);companionLine(c,-.2f,-.76f,-.53f,-1.17f,light,.07f);companionLine(c,.2f,-.76f,.53f,-1.17f,light,.07f);break;
            case 13: // Cloud: five rounded lobes, flat soft lower edge.
                companionOval(c,-1.16f,-.4f,-.1f,.71f,color);companionOval(c,-.77f,-.99f,.48f,.55f,light);companionOval(c,.03f,-.69f,1.07f,.64f,color);
                round(c,-1.07f,-.15f,2.18f,.93f,.42f,color);break;
            case 14: // Sun, twelve rays and a warm center.
                for(int i=0;i<12;i++){double a=i*Math.PI/6;float x=(float)Math.cos(a),y=(float)Math.sin(a);companionPolygon(c,color,x*1.29f,y*1.29f,x*.88f-y*.16f,y*.88f+x*.16f,x*.88f+y*.16f,y*.88f-x*.16f);}
                companionOval(c,-.88f,-.88f,.88f,.88f,light);break;
            case 15: // Snail, lower foot, shell, and rounded antenna tips.
                round(c,-1.04f,.53f,2.24f,.42f,.2f,dark);companionOval(c,.4f,-.05f,1.24f,.88f,color);
                companionLine(c,.72f,.12f,.65f,-.47f,color,.09f);companionLine(c,1.04f,.12f,1.17f,-.42f,color,.09f);
                companionOval(c,-.98f,-.98f,.88f,.83f,color);p.setColor(alpha(light,180));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.075f);c.drawArc(-.67f,-.74f,.61f,.53f,40,290,false,p);p.setStyle(Paint.Style.FILL);break;
            case 16: // Axolotl, three external gills on each side.
                for(int i=0;i<3;i++){float yy=-.61f+i*.43f;companionLine(c,-.79f,yy,-1.29f,yy+(i-1)*.19f,dark,.15f);companionLine(c,.79f,yy,1.29f,yy+(i-1)*.19f,dark,.15f);}
                companionOval(c,-1,-.85f,1,.94f,color);break;
            case 17: // Cactus with raised arms, blossom, and discreet spines.
                round(c,-.65f,-1.03f,1.3f,2,.56f,color);round(c,-1.1f,-.39f,.63f,.98f,.26f,dark);round(c,.47f,-.72f,.63f,1.1f,.26f,dark);
                companionLine(c,-.34f,.54f,-.34f,.74f,light,.045f);companionLine(c,.31f,.57f,.31f,.8f,light,.045f);star(c,.39f,-.99f,.23f,PEACH);break;
            case 18: { // Hedgehog, explicit spike outline and a soft inner face.
                Path q=new Path();for(int i=0;i<28;i++){double a=-Math.PI/2+i*Math.PI/14;float rr=i%2==0?1.14f:.91f;float xx=(float)Math.cos(a)*rr,yy=(float)Math.sin(a)*rr;if(i==0)q.moveTo(xx,yy);else q.lineTo(xx,yy);}q.close();p.setColor(dark);c.drawPath(q,p);companionOval(c,-.84f,-.76f,.84f,.97f,color);break;
            }
            case 19: // Penguin, flippers, cream face, and small feet.
                companionOval(c,-1.05f,-.08f,-.5f,.83f,dark);companionOval(c,.5f,-.08f,1.05f,.83f,dark);companionOval(c,-.88f,-1,.88f,.99f,color);
                companionOval(c,-.69f,-.72f,.69f,.86f,blend(WHITE,color,.13f));round(c,-.69f,.86f,.5f,.19f,.09f,GOLD);round(c,.19f,.86f,.5f,.19f,.09f,GOLD);break;
            case 20: // Ringed planet: back ring, round body, foreground arc.
                p.setColor(light);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.2f);c.save();c.rotate(-23);c.drawOval(-1.35f,-.35f,1.35f,.35f,p);c.restore();p.setStyle(Paint.Style.FILL);
                companionOval(c,-.87f,-.87f,.87f,.87f,color);p.setColor(light);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.18f);c.save();c.rotate(-23);c.drawArc(-1.35f,-.35f,1.35f,.35f,0,180,false,p);c.restore();p.setStyle(Paint.Style.FILL);break;
            case 21: // Dragon, wings, horns, and a rounded snout.
                companionPolygon(c,dark,-.65f,-.18f,-1.35f,-.65f,-1.1f,.49f,-.51f,.68f);companionPolygon(c,dark,.65f,-.18f,1.35f,-.65f,1.1f,.49f,.51f,.68f);
                companionPolygon(c,GOLD,-.63f,-.6f,-.62f,-1.17f,-.18f,-.79f);companionPolygon(c,GOLD,.63f,-.6f,.62f,-1.17f,.18f,-.79f);
                companionOval(c,-.9f,-.94f,.9f,.95f,color);companionOval(c,-.5f,.3f,.5f,.86f,light);break;
            case 22: // Panda, rounded ears and dark eye patches.
                companionOval(c,-1.02f,-1.09f,-.26f,-.36f,dark);companionOval(c,.26f,-1.09f,1.02f,-.36f,dark);companionOval(c,-.98f,-.91f,.98f,1,color);
                companionOval(c,-.65f,-.48f,-.12f,.31f,dark);companionOval(c,.12f,-.48f,.65f,.31f,dark);break;
            case 23: // Squid, tapered head and eight curling arms.
                for(int i=0;i<8;i++)round(c,-.92f+i*.23f,.44f,.17f,.63f+(i%3)*.13f,.085f,i%2==0?color:light);
                companionPolygon(c,color,0,-1.16f,-.95f,-.16f,-.91f,.62f,.91f,.62f,.95f,-.16f);companionOval(c,-.91f,-.49f,.91f,.7f,color);break;
            default:companionOval(c,-1,-1,1,1,color);
        }
        c.restore();p.setStyle(Paint.Style.FILL);p.setShader(null);
    }
    private void companionOval(Canvas c,float l,float t,float r,float b,int color){p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawOval(l,t,r,b,p);}
    private void companionPolygon(Canvas c,int color,float... xy){Path q=new Path();q.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)q.lineTo(xy[i],xy[i+1]);q.close();p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawPath(q,p);}
    private void companionLine(Canvas c,float x,float y,float xx,float yy,int color,float w){p.setColor(color);p.setStrokeWidth(w);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,xx,yy,p);}
    private static int blend(int a,int b,float k){return Color.rgb((int)(Color.red(a)*(1-k)+Color.red(b)*k),(int)(Color.green(a)*(1-k)+Color.green(b)*k),(int)(Color.blue(a)*(1-k)+Color.blue(b)*k));}
    private void drawHome(Canvas c){
        text(c,"LUMA",24,43,28,WHITE,true);text(c,"DRIFT",121,43,19,skin.accent2,true);
        wallet(c,229,17);round(c,325,15,41,44,14,skin.card);icon(c,"settings",345,37,21,WHITE);hit(320,12,50,50,t("settings"),()->go("settings"));
        round(c,24,78,342,84,22,0xf5262b49);round(c,38,96,46,46,16,alpha(profileSignedIn?skin.accent2:PEACH,30));icon(c,profileSignedIn?"check":"profile",61,119,23,profileSignedIn?skin.accent2:PEACH);
        String name=profileName.trim();if(name.contains(" "))name=name.substring(0,name.indexOf(' '));
        boolean guest=profileSnapshot==null||profileSnapshot.state==SessionSnapshot.State.GUEST;
        String profileTitle=profileSignedIn?u("welcome",name):guest?u("guest"):t(profileSnapshot.statusKey);
        fit(c,profileTitle,100,112,18,245,WHITE,true,false);
        String status=profileSignedIn?u("signed"):guest?u("guest_note"):t("account");
        fit(c,status,100,139,13,240,profileSignedIn?skin.accent2:skin.muted,false,false);
        hit(24,78,342,84,t("account")+": "+profileTitle+", "+status,()->openAccount());
        boolean active=board!=null&&!won&&!board.isSolved();int display=active&&mode==0?levelIndex:Math.min(BalancedCatalog.LEVEL_COUNT,store.unlockedLevel());
        round(c,24,182,342,274,27,0xf5292e4d);stroke(c,new RectF(24,182,366,456),27,alpha(WHITE,45),1);
        fit(c,e("saga_chapter",(display-1)/10+1),44,211,13,190,skin.accent2,true,false);
        String title=active&&mode!=0?t(mode==1?"daily":mode==2?"weekly":mode==3?"remix":"friend_challenge"):t("level_number",display);
        fit(c,title,44,253,29,205,WHITE,true,false);fit(c,worldName(display),44,278,13,195,skin.muted,false,false);
        mascot(c,297,270,40,character,false);
        if(!active||mode==0&&contentVersion>=4)difficultyPill(c,BalancedCatalog.info(display,false),44,296,175);
        round(c,44,339,302,7,4,alpha(WHITE,22));round(c,44,339,302f*Math.min(5,cachedCompleted%5)/5f,7,4,skin.accent2);
        int milestone=store.nextLootMilestone();fit(c,milestone==0?e("saga_complete"):u("loot_progress",cachedCompleted,milestone),44,370,13,300,WHITE,false,false);
        label(44,190,302,140,title+", "+worldName(display));
        button(c,t(active?"resume":"play")+"  →",44,389,302,50,PEACH,()->continueGame());
        lobbyTile(c,24,477,"journey",e("nav_map"),e("saga_range",1,BalancedCatalog.LEVEL_COUNT),cachedCompleted+" / "+BalancedCatalog.LEVEL_COUNT,()->go("journey"));
        lobbyTile(c,202,477,"events",e("nav_events"),t("daily")+" / "+t("weekly"),store.dailyCompletedCount()+"/3 · "+store.weeklyCompletedCount()+"/7",()->go("events"));
        lobbyTile(c,24,581,"collection",e("nav_inventory"),e("tab_equipment"),t(character.nameKey),()->go("collection"));
        lobbyTile(c,202,581,"shop",t("shop"),e("earned_short"),store.coins()+" ◈",()->go("shop"));
        quietButton(c,t("import_challenge"),24,693,342,()->importCode());
    }
    private void lobbyTile(Canvas c,float x,float y,String ico,String title,String subtitle,String detail,Runnable action){
        round(c,x,y,164,93,21,skin.card);round(c,x+12,y+12,31,31,11,alpha(skin.accent,38));icon(c,ico,x+27,y+27,17,skin.accent2);
        fit(c,title,x+52,y+30,15,100,WHITE,true,false);fit(c,subtitle,x+13,y+58,12,139,skin.muted,false,false);fit(c,detail,x+13,y+79,13,139,GOLD,true,false);
        hit(x,y,164,93,title+", "+subtitle+", "+detail,action);
    }
    private void tinyTile(Canvas c,float x,float y,Direction d,int color){c.save();c.translate(x,y);c.rotate(reduceMotion?0:(float)Math.sin(frameTime*.001)*8);round(c,-19,-19,38,38,12,color);arrow(c,0,0,d,10,INK);c.restore();}
    private void nav(Canvas c){
        float y=H-87;round(c,10,y,370,76,24,0xff242a46);stroke(c,new RectF(10,y,380,y+76),24,alpha(WHITE,38),1);
        String[] ids={"home","journey","events","collection","shop"};String[] captions={t("home"),e("nav_map"),e("nav_events"),e("nav_inventory"),t("shop")};
        for(int i=0;i<ids.length;i++){String target=ids[i];float x=47+i*74;boolean on=screen.equals(target);if(on)round(c,x-28,y+7,56,34,13,alpha(skin.accent2,30));icon(c,target,x,y+25,21,on?skin.accent2:skin.muted);fit(c,captions[i],x,y+62,12,70,on?WHITE:skin.muted,on,true);hit(x-35,y+3,70,70,captions[i],()->goRoot(target));}
    }
    private int difficultyColor(ProgressionInfo info){return info.difficulty==ProgressionInfo.Difficulty.EXPERT?0xffe7b2ff:info.difficulty==ProgressionInfo.Difficulty.HARD?PEACH:skin.accent2;}
    private void difficultyPill(Canvas c,ProgressionInfo info,float x,float y,float width){int color=difficultyColor(info);round(c,x,y,width,29,12,alpha(color,24));fit(c,e(info.difficultyKey)+" · "+e(info.beatKey),x+width/2,y+20,12,width-14,color,true,true);}
    private void drawJourney(Canvas c){
        boolean remix=journeyTab==1;int total=remix?BalancedCatalog.REMIX_COUNT:BalancedCatalog.LEVEL_COUNT;
        InventoryPresentation.Page page=InventoryPresentation.sagaPage(total,chapter);chapter=page.page;
        header(c,e("nav_map"),e("saga_range",page.first,page.last));
        tabs(c,t("journey"),t("remix"),journeyTab,101,()->{journeyTab=0;chapter=(store.unlockedLevel()-1)/10;},()->{journeyTab=1;chapter=(store.remixUnlockedLevel()-1)/10;});
        if(remix&&!store.remixAvailable()){round(c,24,170,342,350,27,skin.card);mascot(c,195,260,55,character,false);wrap(c,t("remix_unlock"),195,375,292,19,WHITE,true,26);quietButton(c,t("continue"),44,449,302,()->previewLevel(Math.min(12,store.unlockedLevel()),0,0));return;}
        round(c,24,163,342,65,20,0xf5272c49);fit(c,e("saga_chapter",chapter+1),44,190,20,220,WHITE,true,false);fit(c,worldName(page.first),44,214,12,247,skin.accent2,false,false);
        int unlocked=remix?store.remixUnlockedLevel():store.unlockedLevel();
        Path route=new Path();float[] xs={88,279,279,88,88,279,279,88,88,279};
        for(int i=0;i<page.size();i++){float x=xs[i],y=270+(i/2)*82+(i%2)*20;if(i==0)route.moveTo(x,y);else{float px=xs[i-1],py=270+((i-1)/2)*82+((i-1)%2)*20;route.cubicTo(px,(py+y)/2,x,(py+y)/2,x,y);}}
        p.setColor(0x992c3352);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(13);p.setStrokeCap(Paint.Cap.ROUND);c.drawPath(route,p);p.setColor(alpha(skin.accent2,70));p.setStrokeWidth(3);c.drawPath(route,p);p.setStyle(Paint.Style.FILL);
        for(int i=0;i<page.size();i++){final int number=page.first+i;float x=xs[i],y=270+(i/2)*82+(i%2)*20;boolean open=number<=unlocked,current=number==unlocked;int stars=remix?store.remixStars(number):store.stars(number);ProgressionInfo info=BalancedCatalog.info(number,remix);int color=difficultyColor(info);
            if(current){float radius=reduceMotion?36:36+(float)Math.sin(frameTime*.004)*2;p.setColor(alpha(color,45));c.drawCircle(x,y,radius,p);}
            p.setColor(0xff131b30);c.drawCircle(x,y+4,28,p);p.setColor(open?stars>0?0xff436365:current?color:0xff424968:0xff242b46);c.drawCircle(x,y,27,p);
            p.setColor(open?alpha(WHITE,115):alpha(WHITE,25));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(current?2.5f:1);c.drawCircle(x,y,25,p);p.setStyle(Paint.Style.FILL);
            if(open)center(c,""+number,x,y+8,21,current?INK:WHITE,true);else icon(c,"lock",x,y,18,skin.muted);
            for(int k=0;k<3;k++)star(c,x+(k-1)*12,y+36,5,k<stars?GOLD:alpha(WHITE,55));
            if(info.difficulty!=ProgressionInfo.Difficulty.NORMAL){star(c,x+25,y-20,8,color);if(info.difficulty==ProgressionInfo.Difficulty.EXPERT)star(c,x+35,y-11,5,color);}
            if(info.keyCount>0){round(c,x-39,y-27,21,21,8,GOLD);icon(c,"lock",x-28,y-17,13,INK);}
            hit(x-35,y-31,70,74,t("level_number",number)+", "+e(info.difficultyKey)+", "+e(info.elementKey)+(open?"":", "+t("locked")),()->{if(open)previewLevel(number,remix?3:0,0);else notice(t("unlock_previous"));});
        }
        float y=H-151;quietButton(c,"‹",24,y,55,()->{if(page.hasPrevious())chapter--;});quietButton(c,t("level_number",unlocked)+"  →",91,y,208,()->previewLevel(unlocked,remix?3:0,0));quietButton(c,"›",311,y,55,()->{if(page.hasNext())chapter++;});
        label(24,y-30,342,25,e("saga_page",chapter+1,page.pageCount));fit(c,e("saga_page",chapter+1,page.pageCount),195,y-8,12,300,WHITE,true,true);
    }
    private void previewLevel(int index,int m,int event){
        if(m==3&&(!store.remixAvailable()||index>store.remixUnlockedLevel())){notice(t("remix_unlock"));return;}
        if(m==0&&!store.isLevelUnlocked(index)){notice(t("unlock_previous"));return;}
        briefingIndex=index;briefingMode=m;briefingEvent=event;
        briefingLevel=m==0?BalancedCatalog.get(index):m==3?BalancedCatalog.remix(index):LevelCatalog.event(m==1?store.dailySeed(event):store.weeklySeed(event));
        briefingReturn=screen.equals("play")?m==0||m==3?"journey":"events":screen;if(screen.equals("play")){menuHistory.clear();menuHistory.push(briefingReturn);}go("briefing");
    }
    private void drawBriefing(Canvas c){
        if(briefingLevel==null){go("home");return;}
        round(c,24,18,48,48,16,skin.card);icon(c,"back",48,42,21,WHITE);hit(24,18,48,48,t("back"),()->backMenu());wallet(c,282,24);
        fit(c,briefingMode==0?t("level_number",briefingIndex):briefingMode==3?t("remix")+" "+briefingIndex:t(briefingMode==1?"daily":"weekly"),195,113,31,330,WHITE,true,true);
        ProgressionInfo info=briefingMode==0||briefingMode==3?BalancedCatalog.info(briefingIndex,briefingMode==3):null;
        if(info!=null)difficultyPill(c,info,75,137,240);
        round(c,24,188,342,363,27,0xf62a304d);fit(c,e("objective"),44,219,13,290,skin.accent2,true,false);
        String objective=briefingLevel.barriers.isEmpty()?e("objective_clear",briefingLevel.tiles.size()):e("objective_open_seals");
        wrap(c,objective,44,249,294,20,WHITE,false,26);
        float size=152,cc=size/briefingLevel.size,left=119,top=302;
        for(int r=0;r<briefingLevel.size;r++)for(int col=0;col<briefingLevel.size;col++)round(c,left+col*cc+1,top+r*cc+1,cc-2,cc-2,7,alpha(WHITE,8));
        for(Tile tile:briefingLevel.tiles){float x=left+(tile.col+.5f)*cc,y=top+(tile.row+.5f)*cc;drawTile(c,tile,x,y,cc-4,false);if(briefingLevel.isKey(tile.id)){p.setColor(GOLD);c.drawCircle(x-cc*.27f,y-cc*.28f,4,p);}}
        for(Barrier wall:briefingLevel.barriers){float x=left+(wall.col+.5f)*cc,y=top+(wall.row+.5f)*cc;round(c,x-cc*.42f,y-cc*.42f,cc*.84f,cc*.84f,7,alpha(GOLD,45));icon(c,"lock",x,y,cc*.5f,GOLD);}
        MoveBudget budget=MoveBudget.start(briefingMode,briefingLevel);fit(c,e("action_budget",budget.total()),195,492,20,300,GOLD,true,true);
        if(info!=null)fit(c,e(info.elementKey),195,526,14,300,WHITE,true,true);label(24,188,342,363,e("objective")+": "+objective+". "+e("action_budget",budget.total())+(info==null?"":". "+e(info.difficultyKey)+", "+e(info.beatKey)+", "+e(info.elementKey)));
        Catalog.BoosterItem next=InventoryPresentation.nextPower(store.unlockedLevel());fit(c,next==null?e("all_powers_unlocked"):e("unlock_next",t(next.nameKey),next.unlockLevel),195,593,14,330,WHITE,false,true);
        button(c,e("action_play")+"  →",44,633,302,54,PEACH,()->start(briefingIndex,briefingMode,briefingEvent));
        quietButton(c,e("tab_powers"),44,709,302,()->{collectionTab=3;collectionPage=0;go("collection");});
    }
    private void tabs(Canvas c,String a,String b,int tab,float y,Runnable aa,Runnable bb){
        round(c,24,y,342,45,15,skin.card);round(c,28+tab*169,y+4,165,37,12,alpha(skin.accent,65));fit(c,a,110,y+29,14,148,tab==0?WHITE:skin.muted,true,true);fit(c,b,280,y+29,14,148,tab==1?WHITE:skin.muted,true,true);hit(24,y,171,45,a,aa);hit(195,y,171,45,b,bb);
    }
    private String g(String key,Object...args){return GearStrings.get(store.resolvedLanguage(),key,args);}
    private void drawLoadout(Canvas c,float r,int shape){
        int head=AvatarGearCatalog.ITEMS.indexOf(loadout.item(AvatarGearCatalog.Slot.HEAD))%6;
        int aura=AvatarGearCatalog.ITEMS.indexOf(loadout.item(AvatarGearCatalog.Slot.AURA))%6;
        int charm=AvatarGearCatalog.ITEMS.indexOf(loadout.item(AvatarGearCatalog.Slot.CHARM))%6;
        int color=aura<2?skin.accent2:aura<4?PEACH:GOLD;
        p.setColor(alpha(color,90));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(r*.035f);c.drawOval(-r*1.25f,-r*1.13f,r*1.25f,r*1.12f,p);p.setStyle(Paint.Style.FILL);
        for(int i=0;i<3+aura;i++){double angle=i*Math.PI*2/(3+aura)+(reduceMotion?0:frameTime*.00015);star(c,(float)Math.cos(angle)*r*1.21f,(float)Math.sin(angle)*r*1.12f,r*.055f,color);}
        float y=shape==3?-r*1.52f:-r*.88f;
        if(head==0){companionLine(c,0,y,0,y-r*.25f,skin.accent2,r*.07f);companionOval(c,-r*.02f,y-r*.38f,r*.23f,y-r*.16f,skin.accent2);}
        else if(head==1){companionPolygon(c,skin.accent2,-r*.4f,y,0,y-r*.42f,r*.34f,y-r*.08f);}
        else if(head==2){p.setColor(GOLD);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(r*.11f);c.drawArc(-r*.31f,y-r*.35f,r*.31f,y+r*.07f,35,285,false,p);p.setStyle(Paint.Style.FILL);}
        else if(head==3){companionPolygon(c,GOLD,-r*.62f,y,-r*.65f,y-r*.31f,-r*.27f,y-r*.08f,0,y-r*.4f,r*.28f,y-r*.08f,r*.65f,y-r*.31f,r*.62f,y);}
        else if(head==4){round(c,-r*.76f,y-r*.08f,r*1.52f,r*.24f,r*.08f,skin.accent2);star(c,0,y,r*.14f,WHITE);}
        else{for(int sign:new int[]{-1,1}){companionLine(c,sign*r*.4f,y,sign*r*.67f,y-r*.58f,GOLD,r*.1f);companionLine(c,sign*r*.56f,y-r*.31f,sign*r*.91f,y-r*.37f,skin.accent2,r*.08f);}}
        float xx=r*.76f,yy=r*.72f;companionLine(c,xx,yy-r*.13f,xx,yy+r*.16f,GOLD,r*.035f);
        if(charm%3==0)icon(c,"heart",xx,yy+r*.2f,r*.3f,charm>=3?GOLD:PEACH);else if(charm%3==1)star(c,xx,yy+r*.2f,r*.17f,charm>=3?GOLD:skin.accent2);else{p.setColor(charm>=3?GOLD:skin.accent);c.drawCircle(xx,yy+r*.2f,r*.14f,p);}
    }
    private void gearGlyph(Canvas c,AvatarGearCatalog.Item item,float x,float y,float r){
        int n=AvatarGearCatalog.ITEMS.indexOf(item)%6;int col=item.rarity==AvatarGearCatalog.Rarity.EPIC?0xffd6adff:item.rarity==AvatarGearCatalog.Rarity.RARE?0xff8fe8da:GOLD;
        p.setAlpha(255);p.setShader(new RadialGradient(x,y,r*1.6f,new int[]{alpha(col,60),0},null,Shader.TileMode.CLAMP));c.drawCircle(x,y,r*1.6f,p);p.setShader(null);
        if(item.slot==AvatarGearCatalog.Slot.HEAD){icon(c,n==0?"heart":n==1?"trail":n==2?"music":"collection",x,y,r*1.5f,col);if(n>=3)star(c,x+r*.6f,y-r*.5f,r*.27f,WHITE);}
        else if(item.slot==AvatarGearCatalog.Slot.AURA){p.setColor(col);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);c.drawOval(x-r,y-r*.6f,x+r,y+r*.6f,p);p.setStyle(Paint.Style.FILL);for(int i=0;i<3+n;i++){double a=i*Math.PI*2/(3+n);star(c,x+(float)Math.cos(a)*r,y+(float)Math.sin(a)*r*.6f,r*.13f,WHITE);}}
        else{icon(c,n%3==0?"heart":n%3==1?"focus":"music",x,y,r*1.35f,col);star(c,x+r*.65f,y-r*.65f,r*.2f,WHITE);}
    }
    private void collectionSegments(Canvas c){
        String[] names={e("tab_companions"),e("tab_themes"),e("tab_equipment"),e("tab_powers")};
        round(c,24,198,342,50,17,0xff272e4b);
        for(int i=0;i<4;i++){final int tab=i;float x=27+i*84;if(collectionTab==i)round(c,x,202,82,42,13,skin.accent2);fit(c,names[i],x+41,228,12,76,collectionTab==i?INK:WHITE,collectionTab==i,true);hit(x,198,84,50,names[i],()->{collectionTab=tab;collectionPage=0;gearSlotFilter=-1;});}
    }
    private void drawCollection(Canvas c){
        boolean chars=collectionTab==0,gear=collectionTab==2,powers=collectionTab==3;
        java.util.List<InventoryPresentation.Entry> all=powers?Collections.emptyList():chars?InventoryPresentation.companions(store):gear?InventoryPresentation.gear(store,gearSlotFilter<0?null:AvatarGearCatalog.Slot.values()[gearSlotFilter]):InventoryPresentation.themes(store);
        InventoryPresentation.Counts counts=InventoryPresentation.counts(all);
        header(c,e("nav_inventory"),powers?e("tab_powers"):e("collection_count",counts.owned,counts.total));
        round(c,24,99,342,85,22,0xf52a304e);mascot(c,68,145,26,character,false);fit(c,t(character.nameKey),112,129,19,228,WHITE,true,false);fit(c,t(skin.nameKey),112,155,13,103,skin.muted,false,false);
        for(int i=0;i<3;i++){final int slot=i;AvatarGearCatalog.Item worn=loadout.item(AvatarGearCatalog.Slot.values()[i]);gearGlyph(c,worn,239+i*43,150,10);hit(219+i*43,127,42,51,g(worn.nameKey)+", "+e("equipped"),()->{collectionTab=2;gearSlotFilter=slot;collectionPage=0;});}
        collectionSegments(c);
        String[] filters={e("filter_all"),e("filter_owned"),e("filter_locked")};round(c,24,262,342,45,15,0xff252c49);
        for(int i=0;i<3;i++){final int filter=i;float x=27+i*112;if(inventoryFilter==i)round(c,x,266,110,37,12,alpha(skin.accent2,28));fit(c,filters[i],x+55,290,12,102,inventoryFilter==i?skin.accent2:skin.muted,true,true);hit(x,262,112,45,filters[i],()->{inventoryFilter=filter;collectionPage=0;});}
        if(powers){drawPowerInventory(c);return;}
        InventoryPresentation.Filter filter=inventoryFilter==1?InventoryPresentation.Filter.OWNED:inventoryFilter==2?InventoryPresentation.Filter.LOCKED:InventoryPresentation.Filter.ALL;
        java.util.List<InventoryPresentation.Entry> shown=InventoryPresentation.filter(all,filter);
        InventoryPresentation.Page page=InventoryPresentation.page(shown.size(),3,collectionPage);collectionPage=page.page;
        for(int i=0;i<page.size();i++){
            InventoryPresentation.Entry entry=shown.get(page.first-1+i);float y=323+i*124;boolean selected=entry.selected(),owned=entry.owned();
            round(c,24,y,342,112,21,selected?0xff314c55:0xff292f4d);if(selected)stroke(c,new RectF(24,y,366,y+112),21,skin.accent2,2);
            round(c,36,y+17,65,74,18,alpha(selected?skin.accent2:skin.accent,26));
            if(gear)gearGlyph(c,entry.gear,68,y+53,23);else if(chars)mascot(c,68,y+56,25,Catalog.character(entry.id),false);else{Catalog.SkinItem theme=Catalog.skin(entry.id);round(c,47,y+31,42,46,12,theme.bg);round(c,53,y+39,14,14,5,theme.accent);round(c,71,y+57,12,12,4,theme.accent2);}
            String title=gear?g(entry.nameKey):t(entry.nameKey);fit(c,title,114,y+30,19,225,WHITE,true,false);
            String state=e(entry.stateKey());fit(c,state,114,y+54,13,220,selected?skin.accent2:owned?WHITE:GOLD,true,false);
            String source=owned?gear?InventoryPresentation.effectCaption(store.resolvedLanguage(),entry):e("effect_appearance"):InventoryPresentation.sourceCaption(store.resolvedLanguage(),entry);
            fit(c,source,114,y+82,13,231,owned?skin.muted:GOLD,false,false);
            if(gear&&gearSlotFilter>=0)fit(c,g(entry.gear.slot.key()),114,y+102,12,225,skin.muted,false,false);
            if(selected)icon(c,"check",343,y+50,17,skin.accent2);else icon(c,"next",343,y+51,16,WHITE);
            hit(24,y,342,112,title+", "+state+", "+source,()->{if(gear)previewGear(entry.gear);else previewCosmetic(chars,chars?characterIndex(entry.id):themeIndex(entry.id));});
        }
        if(shown.isEmpty())wrap(c,e("collection_empty"),195,398,294,18,WHITE,true,25);
        inventoryPager(c,page);
    }
    private int characterIndex(String id){for(int i=0;i<Catalog.CHARACTERS.length;i++)if(Catalog.CHARACTERS[i].id.equals(id))return i;return 0;}
    private int themeIndex(String id){for(int i=0;i<Catalog.SKINS.length;i++)if(Catalog.SKINS[i].id.equals(id))return i;return 0;}
    private void inventoryPager(Canvas c,InventoryPresentation.Page page){if(page.pageCount<=1)return;float y=H-139;quietButton(c,"‹",24,y,62,()->{if(page.hasPrevious())collectionPage--;});fit(c,e("saga_page",page.page+1,page.pageCount),195,y+29,14,190,WHITE,true,true);quietButton(c,"›",304,y,62,()->{if(page.hasNext())collectionPage++;});}
    private void drawPowerInventory(Canvas c){
        HashSet<String> active=new HashSet<>();for(Catalog.BoosterItem booster:Catalog.BOOSTERS)if(board!=null&&powerActive(booster.id))active.add(booster.id);
        ArrayList<InventoryPresentation.Power> shown=new ArrayList<>();for(InventoryPresentation.Power power:InventoryPresentation.powers(store,active))if(inventoryFilter==0||inventoryFilter==1&&power.unlocked&&(power.stock>0||power.active)||inventoryFilter==2&&!power.unlocked)shown.add(power);
        InventoryPresentation.Page page=InventoryPresentation.page(shown.size(),3,collectionPage);collectionPage=page.page;
        for(int i=0;i<page.size();i++){InventoryPresentation.Power power=shown.get(page.first-1+i);float y=323+i*124;round(c,24,y,342,112,21,power.active?0xff304f53:0xff292f4d);round(c,37,y+23,57,62,17,alpha(power.unlocked?skin.accent2:skin.muted,26));icon(c,power.unlocked?power.id:"lock",65,y+52,29,power.unlocked?skin.accent2:skin.muted);
            fit(c,t(power.nameKey),111,y+31,20,225,WHITE,true,false);fit(c,e(power.state.key()),111,y+57,14,220,power.active?skin.accent2:power.unlocked?WHITE:GOLD,true,false);
            fit(c,power.unlocked?e("power_stock",power.stock):e("power_unlock_at",power.unlockLevel),111,y+87,13,230,power.unlocked?skin.muted:GOLD,false,false);
            hit(24,y,342,112,t(power.nameKey)+", "+e(power.state.key())+", "+e("power_stock",power.stock),()->powerOffer(power.id));
        }
        if(shown.isEmpty())wrap(c,e("collection_empty"),195,398,294,18,WHITE,true,25);inventoryPager(c,page);
    }
    private String shortGearSource(AvatarGearCatalog.Item item){
        String source=item.source==AvatarGearCatalog.Source.JOURNEY_CLEARS?t("journey"):item.source==AvatarGearCatalog.Source.WEEKLY_WINS?t("weekly"):item.source==AvatarGearCatalog.Source.EVENT_STARS?t("events"):g(item.source.key());
        return source+" · "+Math.min(item.requirement,gearProgress.value(item.source))+"/"+item.requirement;
    }
    private void previewGear(AvatarGearCatalog.Item item){
        boolean owned=store.ownsAvatarGear(item.id),equipped=item.id.equals(loadout.item(item.slot).id);
        String message=g(item.slot.key())+" · "+g(item.rarity.key())+"\n"+(equipped?t("equipped"):owned?u("owned"):u("get"))+"\n\n"+GearStrings.effectCaption(store.resolvedLanguage(),item)+"\n\n"+GearStrings.sourceCaption(store.resolvedLanguage(),item,gearProgress)+"\n\n"+u("local");
        android.app.AlertDialog.Builder dialog=new android.app.AlertDialog.Builder(getContext()).setTitle(g(item.nameKey)).setMessage(message).setNegativeButton(t("close"),null);
        if(!equipped&&(owned||item.source==AvatarGearCatalog.Source.EARNED_COINS))dialog.setPositiveButton(owned?t("equip"):u("coin_price",item.priceCoins),(d,w)->{
            try{if(!owned&&!store.purchaseAvatarGear(item.id)){notice(t("not_enough"));return;}if(store.equipAvatarGear(item.id)){syncStyle();sfx(owned?AudioEngine.Sound.SELECT:AudioEngine.Sound.PURCHASE);haptic();invalidate();}}catch(IllegalStateException failed){notice(t("commerce_save_error"));}
        });dialog.show();
    }
    private void previewCosmetic(boolean chars,int index){
        Catalog.CharacterItem ch=chars?Catalog.CHARACTERS[index]:null;Catalog.SkinItem sk=chars?null:Catalog.SKINS[index];
        String id=chars?ch.id:sk.id;boolean owned=chars?store.ownsCharacter(id):store.ownsSkin(id),equipped=id.equals(chars?store.equippedCharacter():store.equippedSkin());int price=chars?ch.price:sk.price;
        android.widget.LinearLayout layout=new android.widget.LinearLayout(getContext());layout.setOrientation(android.widget.LinearLayout.VERTICAL);layout.setPadding(30,10,30,20);
        View preview=new View(getContext()){@Override protected void onDraw(Canvas canvas){super.onDraw(canvas);float center=getWidth()/2f,unit=getHeight()/3.3f;if(chars)mascot(canvas,center,getHeight()*.54f,unit,ch,false);else{round(canvas,center-unit,getHeight()*.2f,unit*2,unit*1.65f,unit*.3f,sk.bg);for(int i=0;i<4;i++)round(canvas,center-unit*.8f+(i%2)*unit*.85f,getHeight()*.3f+(i/2)*unit*.65f,unit*.65f,unit*.5f,unit*.1f,i%2==0?sk.accent:sk.accent2);}}};
        preview.setContentDescription(t(chars?ch.nameKey:sk.nameKey)+". "+t("cosmetic_only"));layout.addView(preview,new android.widget.LinearLayout.LayoutParams(-1,(int)(165*getResources().getDisplayMetrics().density)));
        android.widget.TextView note=new android.widget.TextView(getContext());note.setText((equipped?t("equipped"):owned?u("owned"):u("get"))+"\n"+t("companion_wallet_note",store.coins())+"\n"+t("cosmetic_only"));note.setTextColor(skin.muted);note.setGravity(Gravity.CENTER);note.setTextSize(16);layout.addView(note);
        android.app.AlertDialog.Builder dialog=new android.app.AlertDialog.Builder(getContext()).setTitle(t(chars?ch.nameKey:sk.nameKey)).setView(layout).setNegativeButton(t("close"),(d,w)->{});
        if(!equipped)dialog.setPositiveButton(owned?t("equip"):t("companion_purchase",price),(d,w)->{
            boolean success=owned||(chars?store.purchaseCharacter(id):store.purchaseSkin(id));if(!success){notice(t("not_enough"));return;}
            if(chars)store.equipCharacter(id);else store.equipSkin(id);syncStyle();sfx(owned?AudioEngine.Sound.SELECT:AudioEngine.Sound.PURCHASE);haptic();invalidate();
        });else dialog.setPositiveButton(t("equipped"),(d,w)->{});
        dialog.show();
    }
    private void drawShop(Canvas c){
        header(c,t("shop"),u("lobby_shop"));
        round(c,24,102,342,91,24,blend(skin.card,skin.accent,.12f));coin(c,54,137,17);fit(c,""+store.coins(),84,143,27,113,GOLD,true,false);fit(c,u("earned"),43,174,11,155,skin.muted,false,false);
        quietButton(c,u("equipment"),210,119,140,()->{collectionTab=2;collectionPage=0;go("collection");});
        for(int i=0;i<3;i++){
            Catalog.BoosterItem item=Catalog.BOOSTERS[shopPage*3+i];String id=item.id;boolean unlocked=store.boosterUnlocked(id);float y=207+i*108;
            round(c,24,y,342,100,22,skin.card);round(c,36,y+16,48,48,16,alpha(skin.accent,35));icon(c,unlocked?id:"lock",60,y+40,25,unlocked?skin.accent2:skin.muted);
            fit(c,t(item.nameKey),96,y+30,19,239,WHITE,true,false);fit(c,unlocked?u("stock",store.boosterStock(id)):t("power_level",item.unlockLevel),96,y+50,11,240,skin.accent2,false,false);
            fit(c,unlocked?u("pack",item.amount,store.boosterPrice(id)):t("power_unlock",item.unlockLevel),43,y+87,13,257,GOLD,true,false);icon(c,"next",339,y+82,15,skin.accent2);
            hit(24,y,342,100,t(item.nameKey)+", "+(unlocked?u("pack",item.amount,store.boosterPrice(id)):t("power_unlock",item.unlockLevel)),()->powerOffer(id));
        }
        quietButton(c,"‹",24,535,67,()->{shopPage=0;});fit(c,t("powers_page",shopPage+1,2),195,563,12,190,WHITE,true,true);quietButton(c,"›",299,535,67,()->{shopPage=1;});
        int milestone=store.nextLootMilestone();boolean ready=milestone>0&&cachedCompleted>=milestone;
        String loot=milestone==0?t("claimed"):ready?u("ready"):u("loot_progress",cachedCompleted,milestone);
        quietButton(c,loot,24,589,342,()->{if(milestone==0){notice(t("claimed"));return;}try{String got=store.claimMilestoneLoot();if(got.isEmpty())notice(t("loot_locked",store.nextLootMilestone()));else{syncStyle();notice(t("loot_received",t("booster_"+got)));sfx(AudioEngine.Sound.PURCHASE);}}catch(IllegalStateException failed){notice(t("commerce_save_error"));}});
        // Account gold and advertising have their own explicit, localized commerce surface.
        quietButton(c,t("commerce_entry")+"  →",24,644,342,()->openCommerce());
    }
    private void powerOffer(String id){
        Catalog.BoosterItem item=Catalog.booster(id);boolean unlocked=store.boosterUnlocked(id);
        android.app.AlertDialog.Builder offer=new android.app.AlertDialog.Builder(getContext()).setTitle(t(item.nameKey)).setMessage(t(item.descriptionKey)+"\n\n"+(unlocked?t("stock")+": "+store.boosterStock(id):t("power_unlock",item.unlockLevel))).setNegativeButton(t("close"),(d,w)->{});
        if(unlocked){offer.setPositiveButton(t("power_buy",item.amount,store.boosterPrice(id)),(d,w)->{if(store.purchaseBooster(id)){sfx(AudioEngine.Sound.PURCHASE);notice(t("purchase_success"));}else notice(t("not_enough"));});offer.setNeutralButton(t("power_ad"),(d,w)->getContext().startActivity(com.androidhit.lumadrift.commerce.CommerceActivity.powerIntent(getContext(),id)));}
        offer.show();
    }
    private void eventSegments(Canvas c){String[] names={t("daily"),t("weekly"),u("sealed")};round(c,24,103,342,45,15,skin.card);for(int i=0;i<3;i++){final int n=i;float x=27+i*113;if(eventTab==i)round(c,x,107,110,37,12,alpha(skin.accent,60));fit(c,names[i],x+55,132,12,100,WHITE,eventTab==i,true);hit(x,103,110,45,names[i],()->eventTab=n);}}
    private void drawSealedMenu(Canvas c){
        header(c,u("sealed"),u("sealed_note"));eventSegments(c);round(c,24,163,342,147,24,skin.card);
        icon(c,"lock",55,193,25,GOLD);fit(c,u("sealed"),82,198,20,260,WHITE,true,false);wrap(c,u("sealed_rule"),44,229,300,12,skin.muted,false,18);
        for(int i=1;i<=SealedCatalog.COUNT;i++){final int level=i;float x=24+((i-1)%2)*178,y=332+((i-1)/2)*91;round(c,x,y,164,77,20,skin.card);icon(c,i<3?"lock":"events",x+28,y+37,23,GOLD);fit(c,t("stage_number",i),x+55,y+33,16,99,WHITE,true,false);fit(c,t("play")+" →",x+55,y+56,12,99,skin.accent2,true,false);hit(x,y,164,77,u("sealed")+" · "+t("stage_number",i),()->openChallenge(ChallengeCode.encodeSealed(level)));}
        fit(c,u("sealed_unlimited"),195,642,11,340,skin.accent2,true,true);
    }
    private void drawEvents(Canvas c){
        if(eventTab==2){drawSealedMenu(c);return;}
        boolean daily=eventTab==0;int count=daily?Catalog.DAILY_COUNT:Catalog.WEEKLY_COUNT,completed=daily?store.dailyCompletedCount():store.weeklyCompletedCount(),bonus=daily?100:250;
        boolean claimed=daily?store.dailyBonusClaimed():store.weeklyBonusClaimed(),complete=completed==count;
        header(c,t("events"),daily?store.dailyId():store.weeklyId());
        eventSegments(c);
        RectF hero=new RectF(24,162,366,310);p.setAlpha(255);p.setShader(new LinearGradient(24,162,366,310,new int[]{blend(skin.card,skin.accent,.24f),skin.card},null,Shader.TileMode.CLAMP));c.drawRoundRect(hero,25,25,p);p.setShader(null);
        wrap(c,t(daily?"event_goal_daily":"event_goal_weekly"),44,189,235,14,WHITE,false,19);
        mascot(c,322,209,24,Catalog.CHARACTERS[daily?6:20],complete);
        fit(c,t("event_progress",completed,count),44,234,13,270,skin.accent2,true,false);
        round(c,44,246,302,7,4,alpha(WHITE,20));if(completed>0)round(c,44,246,302f*completed/count,7,4,skin.accent2);
        long minutes=EventSchedule.remainingMinutes(System.currentTimeMillis(),!daily,TimeZone.getDefault());
        String time=minutes>=1440?t("event_time_dhm",minutes/1440,(minutes/60)%24,minutes%60):t("event_time_hm",minutes/60,minutes%60);
        fit(c,t("event_resets",time),44,278,12,302,GOLD,true,false);
        fit(c,t("event_stage_reward",daily?70:105,daily?90:135),44,298,11,302,skin.muted,false,false);
        fit(c,t("event_clock_note"),195,330,10,340,skin.muted,false,true);
        label(24,162,342,148,t(daily?"event_goal_daily":"event_goal_weekly")+" "+t("event_progress",completed,count)+". "+t("event_resets",time)+". "+t("event_stage_reward",daily?70:105,daily?90:135));
        label(24,314,342,24,t("event_clock_note"));
        int next=-1;
        for(int i=0;i<count;i++){
            final int idx=i;int stars=daily?store.dailyStars(i):store.weeklyStars(i);boolean open=daily?store.isDailyUnlocked(i):store.isWeeklyUnlocked(i);
            if(next<0&&stars==0&&open)next=i;
            float x=daily?24+i*117:i<4?24+i*88:68+(i-4)*88,y=daily?347:347+(i/4)*77,w=daily?108:78,h=daily?110:68;
            round(c,x,y,w,h,20,stars>0?blend(skin.card,skin.accent2,.12f):skin.card);
            if(open&&stars==0)stroke(c,new RectF(x,y,x+w,y+h),20,skin.accent2,1.7f);
            if(daily){
                round(c,x+w/2-19,y+13,38,38,14,alpha(open?skin.accent:skin.muted,45));
                if(stars>0)icon(c,"check",x+w/2,y+32,20,skin.accent2);else if(!open)icon(c,"lock",x+w/2,y+32,18,skin.muted);else center(c,""+(i+1),x+w/2,y+39,22,WHITE,true);
                fit(c,t("stage_number",i+1),x+w/2,y+75,13,w-12,open?WHITE:skin.muted,true,true);
                for(int k=0;k<3;k++)star(c,x+w/2+(k-1)*17,y+94,6,k<stars?GOLD:alpha(skin.muted,55));
            }else{
                center(c,""+(i+1),x+w/2,y+29,21,open?WHITE:skin.muted,true);
                if(!open)icon(c,"lock",x+w-14,y+15,11,skin.muted);else if(stars>0)icon(c,"check",x+w-14,y+15,11,skin.accent2);
                for(int k=0;k<3;k++)star(c,x+w/2+(k-1)*15,y+51,5.5f,k<stars?GOLD:alpha(skin.muted,55));
            }
            String stageLabel=t("stage_number",i+1)+", "+t(stars>0?"retry":"play")+", "+stars+" "+t("stars")+(open?"":", "+t("locked"));
            hit(x,y,w,h,stageLabel,()->openEventStage(daily,idx));
        }
        quietButton(c,t("missions")+"  →",24,H-326,342,()->go("missions"));
        final int nextStage=next<0?0:next;
        button(c,t(complete?"event_replay":"event_play_next",nextStage+1),24,H-258,342,48,skin.accent2,()->openEventStage(daily,nextStage));
        float rewardY=H-192;round(c,24,rewardY,342,84,22,skin.card);
        fit(c,t("bonus"),42,rewardY+27,13,158,WHITE,true,false);coin(c,51,rewardY+55,9);text(c,"+"+bonus,70,rewardY+62,24,GOLD,true);
        label(24,rewardY,177,84,t("bonus")+": "+bonus+" "+t("coins"));
        button(c,t(claimed?"claimed":complete?"claim":"locked"),208,rewardY+20,142,44,complete&&!claimed?GOLD:blend(skin.muted,WHITE,.2f),()->{
            int got=daily?store.claimDailyCompletionReward():store.claimWeeklyCompletionReward();
            if(got>0){syncStyle();notice("+"+got+" "+t("coins"));sfx(AudioEngine.Sound.COIN);}
            else notice((daily?store.dailyBonusClaimed():store.weeklyBonusClaimed())?t("claimed"):t("event_bonus_locked",Math.max(0,count-(daily?store.dailyCompletedCount():store.weeklyCompletedCount()))));
        });
    }
    private void openEventStage(boolean daily,int index){
        // Recheck the current calendar period at activation, even if midnight passed after drawing.
        if(daily?store.isDailyUnlocked(index):store.isWeeklyUnlocked(index))previewLevel(1,daily?1:2,index);else notice(t("unlock_previous"));
    }
    // Legal notices retain their original English license text; user-facing actions are localized.
    @android.annotation.SuppressLint("SetTextI18n")
    private void showLegal(){
        try{
            String license;try(java.io.InputStream in=getContext().getAssets().open("LICENSE.txt")){java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] buffer=new byte[4096];int count;while((count=in.read(buffer))!=-1)out.write(buffer,0,count);license=out.toString("UTF-8");}
            String notices;try(java.io.InputStream in=getContext().getAssets().open("THIRD_PARTY_NOTICES.txt")){java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] buffer=new byte[2048];int count;while((count=in.read(buffer))!=-1)out.write(buffer,0,count);notices=out.toString("UTF-8");}
            android.widget.TextView body=new android.widget.TextView(getContext());body.setText(t(BuildConfig.MODE_SANDBOX?"sandbox_privacy_text":"production_privacy_text")+"\n\n© 2026 AndroidHit. Original puzzle art, generated Aurora Atlas environment and synthesized audio.\n\n"+notices+"\n\n"+license);body.setTextSize(12);body.setPadding(30,20,30,20);body.setTextColor(WHITE);android.widget.ScrollView scroll=new android.widget.ScrollView(getContext());scroll.addView(body);
            new android.app.AlertDialog.Builder(getContext()).setTitle(t("credits")).setView(scroll).setPositiveButton(t("close"),(d,w)->{}).setNeutralButton(t("source_code"),(d,w)->{try{getContext().startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW,android.net.Uri.parse("https://github.com/nabz0r/AndroidHit")));}catch(android.content.ActivityNotFoundException ignored){notice("github.com/nabz0r/AndroidHit");}}).show();
        }catch(java.io.IOException ignored){notice("github.com/nabz0r/AndroidHit");}
    }
    private void drawMissions(Canvas c){
        header(c,t("missions"),meta.dayId());
        label(24,103,342,93,t("rank",meta.rank())+", "+t("xp",meta.xp())+", "+t("rank_progress",meta.rankProgress()));
        round(c,24,103,342,93,24,skin.card);fit(c,t("rank",meta.rank()),44,135,22,300,WHITE,true,false);text(c,t("rank_progress",meta.rankProgress()),44,160,12,skin.muted,false);round(c,44,174,300,5,3,alpha(WHITE,18));round(c,44,174,300*meta.rankProgress()/120f,5,3,skin.accent2);
        String[] keys={"mission_one","mission_two","mission_three"};String[] icons={"journey","collection","events"};
        for(int i=0;i<3;i++){final int idx=i;float y=214+i*119;round(c,24,y,342,105,22,skin.card);icon(c,icons[i],51,y+28,21,skin.accent2);wrap(c,t(keys[i]),77,y+25,260,14,WHITE,false,18);text(c,t("mission_progress",meta.missionProgress(i),meta.missionTarget(i)),44,y+83,14,skin.muted,true);
            label(40,y+10,305,85,t(keys[i])+", "+t("mission_progress",meta.missionProgress(i),meta.missionTarget(i)));
            boolean claimed=meta.missionClaimed(i),ready=meta.missionComplete(i);String title=claimed?t("claimed"):ready?t("claim")+" +"+meta.missionCoins(i)+" ◈":"+"+meta.missionCoins(i)+" ◈";
            quietButton(c,title,198,y+60,150,()->{if(meta.missionComplete(idx)){try{int got=meta.claimMission(idx,store);if(got>0){sfx(AudioEngine.Sound.COIN);notice("+"+got+" "+t("coins"));}else notice(t("claimed"));}catch(IllegalStateException failed){notice(t("save_error"));}}else notice(t("mission_note"));});
        }
        float yy=579;round(c,24,yy,342,70,20,skin.card);star(c,51,yy+32,15,meta.badgeUnlocked()?GOLD:skin.muted);fit(c,t("badge"),78,yy+27,15,260,WHITE,true,false);fit(c,t(meta.badgeUnlocked()?meta.badgeEquipped()?"equipped":"equip":"badge_desc"),78,yy+48,11,260,skin.muted,false,false);hit(24,yy,342,70,t("badge"),()->{if(meta.badgeUnlocked()){meta.setBadgeEquipped(!meta.badgeEquipped());sfx(AudioEngine.Sound.SELECT);}else notice(t("badge_desc"));});
        if(H>790)fit(c,t("days_note"),195,H-110,11,340,skin.muted,false,true);
    }
    private String journalStatus(String status){return t(PlayJournal.OBSERVED.equals(status)?"journal_observed":PlayJournal.NOT_OBSERVED_LOCAL.equals(status)?"journal_not_observed":"journal_unknown");}
    private void drawJournal(Canvas c){
        header(c,t("local_journal"),t("local_only"));quietButton(c,"‹ "+t("back"),24,99,107,()->go("settings"));
        settingsRow(c,"local_journal",journal.isEnabled(),163,()->{journal.setEnabled(!journal.isEnabled());if(journal.isEnabled())applyPendingContinuation();if(prefs.getBoolean("session_migration_notice",false)){notice(t("moves_migration"));prefs.edit().putBoolean("session_migration_notice",false).commit();}log("session_start","version",BuildConfig.VERSION_NAME,"language",store.resolvedLanguage());});
        wrap(c,t("journal_desc"),27,245,336,14,skin.muted,false,21);
        round(c,24,321,342,150,24,skin.card);fit(c,t("journal_sessions",journal.sessions(),journal.activeDays()),43,355,19,305,WHITE,true,false);fit(c,t("stats"),43,387,13,305,skin.accent2,true,false);
        wrap(c,t("journal_returns",journalStatus(journal.returnDay1()),journalStatus(journal.returnDay7())),43,417,300,12,skin.muted,false,19);
        button(c,t("export_journal"),24,493,342,49,skin.accent2,()->{if(journal.eventCount()==0){notice(t("journal_empty"));return;}try{ShareComposer.exportJournal((Activity)getContext(),journal.exportJson(),store.resolvedLanguage());}catch(java.io.IOException|android.content.ActivityNotFoundException error){notice(t("journal_error"));}});
        quietButton(c,t("clear_journal"),24,558,342,()->new android.app.AlertDialog.Builder(getContext()).setTitle(t("clear_journal")).setMessage(t("confirm_clear_journal")).setPositiveButton(t("yes"),(dialog,which)->{ShareComposer.clearJournalExports((Activity)getContext());journal.clear();notice(t("journal_cleared"));}).setNegativeButton(t("cancel"),(dialog,which)->{}).show());
        wrap(c,t("export_note"),195,644,323,12,skin.muted,true,18);
    }
    private void drawSettings(Canvas c){
        header(c,t("settings"),(BuildConfig.MODE_SANDBOX?"Luma Drift Lab":"Luma Drift")+" · "+BuildConfig.VERSION_NAME);
        quietButton(c,"‹  "+t("back"),24,100,105,()->go("home"));
        settingsRow(c,"sound",store.soundEnabled(),151,()->{store.setSoundEnabled(!store.soundEnabled());syncStyle();});
        settingsRow(c,"music",store.musicEnabled(),211,()->{store.setMusicEnabled(!store.musicEnabled());syncStyle();});
        settingsRow(c,"haptics",store.hapticsEnabled(),271,()->{store.setHapticsEnabled(!store.hapticsEnabled());haptic();});
        settingsRow(c,"reduce_motion",reduceMotion,331,()->{reduceMotion=!reduceMotion;prefs.edit().putBoolean("reduce_motion",reduceMotion).apply();});
        text(c,t("language"),26,423,16,WHITE,true);
        for(int i=0;i<Strings.LANGUAGE_CODES.length;i++){final String code=Strings.LANGUAGE_CODES[i];float x=24+(i%3)*116,y=443+(i/3)*43;boolean on=store.language().equals(code);round(c,x,y,110,36,12,on?alpha(skin.accent,110):skin.card);String name=i==0?t("system"):Strings.LANGUAGE_NAMES[i];fit(c,name,x+55,y+23,12,100,on?WHITE:skin.muted,on,true);hit(x,y,110,36,name,()->{store.setLanguage(code);invalidate();});}
        quietButton(c,t("how_to_play"),24,590,342,()->setHelp(true));
        quietButton(c,t("local_journal"),24,648,165,()->go("journal"));
        quietButton(c,t("account"),201,648,165,()->getContext().startActivity(new android.content.Intent(getContext(),com.androidhit.lumadrift.identity.AccountActivity.class)));
        quietButton(c,t("credits"),24,706,342,()->showLegal());
        fit(c,t(BuildConfig.MODE_SANDBOX?"sandbox_privacy_local":"production_privacy_local"),195,773,10,338,skin.muted,false,true);

    }
    private void settingsRow(Canvas c,String key,boolean on,float y,Runnable a){round(c,24,y,342,56,17,skin.card);text(c,t(key),43,y+34,15,WHITE,true);round(c,302,y+14,44,28,14,on?skin.accent2:alpha(skin.muted,55));p.setColor(on?INK:WHITE);c.drawCircle(on?332:316,y+28,10,p);hit(24,y,342,56,t(key)+": "+t(on?"on":"off"),a);}
    private void drawPlay(Canvas c){
        if(board==null){go("home");return;}
        round(c,24,18,46,46,16,skin.card);icon(c,"pause",47,41,20,WHITE);hit(24,18,46,46,t("pause"),()->setPaused(true));
        fit(c,mode==0?t("level_number",levelIndex):mode==3?t("remix")+" "+levelIndex:t(mode==1?"daily":mode==2?"weekly":"friend_challenge"),191,41,mode==0?23:17,175,WHITE,true,true);
        fit(c,mode==0||mode==3?worldName(levelIndex):mode==4?friendCode:t("stage_number",eventIndex+1),191,62,11,183,skin.muted,false,true);wallet(c,282,23);
        round(c,24,88,342,77,23,skin.card);mascot(c,62,126,22,character,false);
        String coach=mode==0&&levelIndex<=3?"coach_motion":mode==0&&levelIndex<=5?"coach_order":mode==0&&levelIndex==6?"coach_gate":mode==3&&levelIndex<=3?"coach_dual":"";
        if(!coach.isEmpty()){fit(c,t("free_opening"),99,113,13,216,skin.accent2,true,false);wrap(c,t(coach),99,136,211,11,WHITE,false,15);}
        else{fit(c,board.level.barriers.isEmpty()?t("clear_board"):u("sealed"),99,118,18,220,WHITE,true,false);fit(c,t("remaining")+"  "+board.remainingCount()+" / "+board.level.tiles.size(),100,143,12,218,skin.muted,false,false);}
        round(c,320,107,32,38,12,alpha(skin.accent,25));icon(c,"help",336,126,17,skin.accent2);hit(310,96,50,59,t("how_to_play"),()->setHelp(true));
        boolean low=moveBudget!=null&&!moveBudget.isUnlimited()&&moveBudget.remaining()<=3;
        int budgetColor=low?0xffff9caf:skin.accent2;
        round(c,24,175,342,44,16,blend(skin.card,budgetColor,.15f));
        String budgetLabel=(moveBudget.isUnlimited()?t("moves_unlimited"):t("moves_left",moveBudget.remaining(),moveBudget.total()));
        fit(c,budgetLabel,195,203,20,318,budgetColor,true,true);label(24,175,342,44,budgetLabel);
        float boardW=300;cell=boardW/board.level.size;bx=(390-boardW)/2;by=249;
        round(c,bx-18,by-18,boardW+36,boardW+36,25,alpha(skin.card,175));stroke(c,new RectF(bx-18,by-18,bx+boardW+18,by+boardW+18),25,alpha(WHITE,16),1);
        for(int row=0;row<board.level.size;row++)for(int col=0;col<board.level.size;col++){
            round(c,bx+col*cell+3,by+row*cell+3,cell-6,cell-6,12,alpha(WHITE,4));p.setColor(alpha(WHITE,16));c.drawCircle(bx+(col+.5f)*cell,by+(row+.5f)*cell,1.5f,p);
        }
        drawGates(c,boardW);drawBarriers(c);
        if(selected>=0&&board.isPresent(selected))drawRay(c,board.level.tile(selected));
        for(Tile tile:board.remainingTiles()){
            float x=bx+(tile.col+.5f)*cell,y=by+(tile.row+.5f)*cell;
            if(!reduceMotion&&tile.id==shakeId&&frameTime<shakeUntil)x+=(float)Math.sin((shakeUntil-frameTime)*.06)*4;
            boolean lit=tile.id==coachId||tile.id==hintId&&frameTime<hintUntil||frameTime<visionUntil&&board.canMove(tile.id)||frameTime<focusUntil&&safeHighlights.contains(tile.id);
            drawTile(c,tile,x,y,cell-8,lit);if(board.level.isKey(tile.id)){round(c,x-cell*.43f,y-cell*.43f,20,20,6,GOLD);center(c,"K"+sealedKeyNumber(tile.id),x-cell*.43f+10,y-cell*.43f+14,10,INK,true);}
            if(frameTime<trailUntil)for(int k=0;k<trail.length;k++)if(trail[k]==tile.id){round(c,x-cell*.39f,y-cell*.4f,19,19,7,GOLD);center(c,""+(k+1),x-cell*.39f+9.5f,y-cell*.4f+14,11,INK,true);}
            final int id=tile.id;
            String description=(board.level.isKey(tile.id)?u("sealed_key",sealedKeyNumber(tile.id))+", ":"")+t("piece_description",tile.row+1,tile.col+1,t("direction_"+tile.direction.name().toLowerCase(Locale.ROOT)))+", "+t(board.canMove(id)?"gate_open":"blocked");
            if(frameTime<focusUntil&&safeHighlights.contains(id))description+=", "+t("power_safe");
            if(frameTime<trailUntil)for(int k=0;k<trail.length;k++)if(trail[k]==id)description+=", "+t("power_step",k+1);
            if(tile.switchMask!=0){description+=", "+t("switch")+":";for(Direction target:Direction.values())if((tile.switchMask&target.bit)!=0)description+=" "+t("direction_"+target.name().toLowerCase(Locale.ROOT));}
            hit(bx+tile.col*cell,by+tile.row*cell,cell,cell,description,()->movePiece(id));
        }
        Iterator<Flight> it=flights.iterator();while(it.hasNext()){
            Flight f=it.next();float u=(frameTime-f.at)/(reduceMotion?180f:460f);if(u>=1){it.remove();continue;}float d=reduceMotion?0:(u*u)*330;
            c.saveLayerAlpha(0,0,390,H,(int)((1-u)*255));drawTile(c,f.tile,f.x+f.tile.direction.dc*d,f.y+f.tile.direction.dr*d,cell-8,false);c.restore();
            for(int i=0;i<4;i++){float tail=d-i*12;if(tail>0)star(c,f.x+f.tile.direction.dc*tail,f.y+f.tile.direction.dr*tail,3*(1-u),alpha(f.color,(int)(100*(1-u))));}
        }
        if(!reduceMotion&&frameTime<successUntil){float elapsed=1-(successUntil-frameTime)/600f;int a=(int)(160*(1-elapsed));p.setColor(alpha(skin.accent2,a));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);c.drawCircle(successX,successY,8+elapsed*30,p);p.setStyle(Paint.Style.FILL);for(int k=0;k<6;k++){double angle=k*Math.PI/3;star(c,successX+(float)Math.cos(angle)*(10+elapsed*28),successY+(float)Math.sin(angle)*(10+elapsed*28),3*(1-elapsed),alpha(GOLD,a));}}
        float controls=by+boardW+37;
        String feedback=frameTime<toastUntil?toast:flowRemaining>0?u("flow_running"):shieldCharges>0?t("shield_left",shieldCharges):gearGrace>0?u("gear_protect",gearGrace):mode==4?t("undo_free"):u("budget",moveBudget.minimumActions(),moveBudget.mistakeAllowance());
        fit(c,feedback,195,controls+5,13,338,frameTime<toastUntil?GOLD:skin.muted,false,true);
        float yy=controls+22;quietButton(c,"↶  "+t("undo"),24,yy,165,()->undoMove());quietButton(c,"↻  "+t("restart"),201,yy,165,()->{setPaused(true);restartConfirm=true;});
        if(mode==4){quietButton(c,t("copy_code"),24,yy+61,165,()->{android.content.ClipboardManager clipboard=(android.content.ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Luma Drift",friendCode));notice(t("challenge_code")+": "+friendCode);});quietButton(c,t("share_challenge"),201,yy+61,165,()->shareBoard());wrap(c,t("challenge_note"),195,yy+130,330,11,skin.muted,true,16);}
        else{
        for(int i=0;i<Catalog.BOOSTERS.length;i++){Catalog.BoosterItem item=Catalog.BOOSTERS[i];final String id=item.id;float x=24+(i%3)*118,y=yy+57+(i/3)*57;boolean unlocked=store.boosterUnlocked(id);
            boolean active=powerActive(id);round(c,x,y,106,51,15,active?blend(skin.card,skin.accent2,.17f):skin.card);if(active)stroke(c,new RectF(x,y,x+106,y+51),15,skin.accent2,1);icon(c,unlocked?id:"lock",x+20,y+20,18,unlocked?skin.accent2:skin.muted);fit(c,unlocked?active?u("active"):"×"+store.boosterStock(id):t("power_level",item.unlockLevel),x+70,y+23,11,57,skin.muted,false,true);fit(c,t(item.nameKey),x+53,y+43,11,94,unlocked?WHITE:skin.muted,true,true);hit(x,y,106,51,t(item.nameKey)+(unlocked?": "+(active?u("active"):u("stock",store.boosterStock(id))):", "+t("power_level",item.unlockLevel)),()->useItem(id));
        }
        }
        if(won)hits.clear();
        if(outOfMoves()&&!paused&&!won)drawOutOfMoves(c);
        if(paused)drawPause(c);
        if(won&&frameTime>=winAt)drawWin(c);else if(powerSaveBlocked)drawSaveRetry(c);
    }
    private int directionColor(Direction d){switch(d){case UP:return skin.accent2;case RIGHT:return PEACH;case DOWN:return skin.accent;default:return 0xff9dc9ff;}}
    private void arrow(Canvas c,float x,float y,Direction d,float size,int color){
        c.save();c.translate(x,y);c.rotate(d.ordinal()*90);p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(2,size*.21f));p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);Path q=new Path();q.moveTo(0,size*.7f);q.lineTo(0,-size*.7f);q.moveTo(-size*.53f,-size*.15f);q.lineTo(0,-size*.72f);q.lineTo(size*.53f,-size*.15f);c.drawPath(q,p);p.setStyle(Paint.Style.FILL);c.restore();
    }
    private void drawTile(Canvas c,Tile tile,float x,float y,float size,boolean lit){
        int color=directionColor(tile.direction);if(lit){p.setAlpha(255);p.setShader(new RadialGradient(x,y,size*.8f,new int[]{alpha(GOLD,135),0},null,Shader.TileMode.CLAMP));c.drawCircle(x,y,size*.8f,p);p.setShader(null);}
        float radius=Math.min(13,size*.27f);
        round(c,x-size/2-1,y-size/2+5,size+2,size, radius,blend(color,INK,.66f));
        p.setAlpha(255);p.setShader(new LinearGradient(x,y-size/2,x,y+size/2,blend(color,WHITE,.16f),color,Shader.TileMode.CLAMP));c.drawRoundRect(x-size/2,y-size/2,x+size/2,y+size/2,12,12,p);p.setShader(null);
        stroke(c,new RectF(x-size/2+1,y-size/2+1,x+size/2-1,y+size/2-1),radius,alpha(WHITE,125),1.25f);
        round(c,x-size*.36f,y-size*.39f,size*.72f,size*.08f,3,alpha(WHITE,85));
        arrow(c,x,y+1,tile.direction,size*.35f,INK);
        if(tile.switchMask!=0){
            int k=0;for(Direction target:Direction.values())if((tile.switchMask&target.bit)!=0){float sx=x+size*.31f,sy=y-size*.29f+k*size*.32f;p.setColor(INK);c.drawCircle(sx,sy,size*.20f,p);p.setColor(directionColor(target));c.drawCircle(sx,sy,size*.15f,p);arrow(c,sx,sy,target,size*.12f,INK);k++;}
        }
        if(lit)stroke(c,new RectF(x-size/2-2,y-size/2-2,x+size/2+2,y+size/2+2),14,GOLD,2);
    }
    private void drawGates(Canvas c,float w){
        int used=0;for(Tile t:board.level.tiles)used|=t.switchMask;
        for(Direction d:Direction.values()){
            boolean open=board.isGateOpen(d);boolean active=(used&d.bit)!=0;
            float x=bx+w/2+d.dc*(w/2+12), y=by+w/2+d.dr*(w/2+12);
            if(!active){arrow(c,x,y,d,5,alpha(skin.muted,85));continue;}
            c.save();c.translate(x,y);c.rotate(d.ordinal()*90);int col=directionColor(d);
            round(c,-39,-5,78,10,5,alpha(col,open?90:230));if(open){round(c,-14,-6,28,12,1,skin.bg);arrow(c,0,0,Direction.UP,7,col);}else{icon(c,"lock",0,0,13,INK);}
            c.restore();if(!reduceMotion&&frameTime<gatePulseUntil){float pulse=1-(gatePulseUntil-frameTime)/700f;p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(alpha(col,(int)(180*(1-pulse))));c.drawCircle(x,y,12+pulse*22,p);p.setStyle(Paint.Style.FILL);}if(selected>=0&&board.isPresent(selected)&&(board.level.tile(selected).switchMask&d.bit)!=0)stroke(c,new RectF(x-22,y-22,x+22,y+22),12,GOLD,2);final String desc=t(open?"gate_open":"gate_closed")+" · "+t("direction_"+d.name().toLowerCase(Locale.ROOT));
            hit(x-23,y-23,46,46,desc,()->notice(desc));
        }
    }
    private int sealedKeyNumber(int tileId){ArrayList<Integer> keys=new ArrayList<>();for(Barrier barrier:board.level.barriers)if(!keys.contains(barrier.keyTileId))keys.add(barrier.keyTileId);return keys.indexOf(tileId)+1;}
    private void drawBarriers(Canvas c){for(Barrier wall:board.level.barriers){float x=bx+(wall.col+.5f)*cell,y=by+(wall.row+.5f)*cell;boolean active=board.isBarrierActive(wall);if(!active){stroke(c,new RectF(x-cell*.4f,y-cell*.4f,x+cell*.4f,y+cell*.4f),10,alpha(GOLD,25),1);continue;}round(c,x-cell*.44f,y-cell*.44f,cell*.88f,cell*.88f,11,blend(skin.card,GOLD,.16f));stroke(c,new RectF(x-cell*.44f,y-cell*.44f,x+cell*.44f,y+cell*.44f),11,GOLD,1.6f);icon(c,"lock",x,y-3,cell*.36f,GOLD);fit(c,"K"+sealedKeyNumber(wall.keyTileId),x,y+cell*.31f,10,cell*.7f,GOLD,true,true);String caption=u("sealed_wall",sealedKeyNumber(wall.keyTileId));hit(x-cell/2,y-cell/2,cell,cell,caption,()->notice(caption));}}
    private void drawRay(Canvas c,Tile tile){
        float x=bx+(tile.col+.5f)*cell,y=by+(tile.row+.5f)*cell;boolean valid=board.canMove(tile.id);int color=valid?WHITE:0xffff7e94;
        float endX=tile.direction.dc==0?x:tile.direction.dc>0?bx+300:bx;
        float endY=tile.direction.dr==0?y:tile.direction.dr>0?by+300:by;
        p.setColor(alpha(color,180));p.setStrokeWidth(3);p.setStyle(Paint.Style.STROKE);p.setPathEffect(new DashPathEffect(new float[]{4,6},frameTime%10));c.drawLine(x,y,endX,endY,p);p.setPathEffect(null);p.setStyle(Paint.Style.FILL);
    }
    private void movePiece(int id){
        if(board==null||won||paused||help||!running||powerSaveBlocked||flowRemaining>0&&!flowExecuting||!board.isPresent(id)||outOfMoves())return;
        boolean protectedBlock=!board.canMove(id)&&(shieldCharges>0||gearGrace>0);
        if(protectedBlock){if(shieldCharges>0)shieldCharges--;else gearGrace--;}else if(!moveBudget.consumeAction())return;
        Tile tile=board.level.tile(id);Board.MoveResult move=board.move(id);selected=-1;
        if(!move.success){moveStreak=0;shakeId=id;shakeUntil=SystemClock.uptimeMillis()+300;sfx(AudioEngine.Sound.BLOCKED);haptic();if(!persistMove())return;if(outOfMoves())exhausted();else notice(t(protectedBlock?"shield_saved":move.reason==Board.BlockReason.CLOSED_GATE?"gate_closed":"blocked"));if(move.reason==Board.BlockReason.LOCKED_BARRIER){Barrier wall=board.blockingBarrier(id);if(wall!=null)notice(u("sealed_wall",sealedKeyNumber(wall.keyTileId)));}return;}
        moveStreak++;successX=bx+(tile.col+.5f)*cell;successY=by+(tile.row+.5f)*cell;successUntil=SystemClock.uptimeMillis()+600;
        if(tile.switchMask!=0||board.level.isKey(tile.id))gatePulseUntil=SystemClock.uptimeMillis()+700;
        safeHighlights.clear();trailUntil=0;focusUntil=0;
        hintId=-1;coachId=mode==0&&levelIndex==1&&board.moves()<2?board.hint():-1;flights.add(new Flight(tile,SystemClock.uptimeMillis(),bx+(tile.col+.5f)*cell,by+(tile.row+.5f)*cell,directionColor(tile.direction)));
        sfx(tile.switchMask==0&&!board.level.isKey(tile.id)?AudioEngine.Sound.MOVE:AudioEngine.Sound.GATE);haptic();if(!persistMove())return;
        if(move.solved)finishLevel();else if(outOfMoves())exhausted();else if(board.isStuck())notice(t("no_safe_move"));
    }
    private boolean persistMove(){if(saveSession()||saveSession())return true;powerSaveBlocked=true;actionGeneration++;stopTimer();notice(t("commerce_save_error"));return false;}
    private void undoMove(){
        if(board==null||won||paused||help||!running||powerSaveBlocked||flowRemaining>0||outOfMoves())return;
        actionGeneration++;
        if(board.canUndo()&&moveBudget.consumeAction()&&board.undo()){moveStreak=0;undos++;hintId=-1;trailUntil=focusUntil=0;safeHighlights.clear();flights.clear();sfx(AudioEngine.Sound.UNDO);if(!persistMove())return;if(outOfMoves())exhausted();}else sfx(AudioEngine.Sound.TAP);
    }
    private boolean powerActive(String id){long now=effectsPausedAt>0?effectsPausedAt:SystemClock.uptimeMillis();return id.equals("hint")?hintId>=0&&now<hintUntil:id.equals("vision")?now<visionUntil:id.equals("focus")?now<focusUntil:id.equals("trail")?now<trailUntil:id.equals("shield")?shieldCharges>0:flowRemaining>0;}
    private void useItem(String id){
        if(board==null||won||paused||help||!running||mode==4||outOfMoves()||powerSaveBlocked)return;
        Catalog.BoosterItem item=Catalog.booster(id);if(item==null)return;
        if(!store.boosterUnlocked(id)){notice(t("power_unlock",item.unlockLevel));return;}
        if(powerActive(id)||powerGate.isBusy()){notice(u("power_busy"));return;}
        if(store.boosterStock(id)<=0){powerOffer(id);return;}
        new android.app.AlertDialog.Builder(getContext()).setTitle(t(item.nameKey))
            .setMessage(t(item.descriptionKey)+"\n\n"+u("power_cost",store.boosterStock(id)))
            .setPositiveButton(u("power_confirm"),(d,w)->activatePower(id)).setNegativeButton(t("cancel"),null).show();
    }
    /** Inventory escrow is confirmed only after the effect and request are durably saved. */
    private void activatePower(String id){
        if(board==null||won||paused||help||!running||mode==4||outOfMoves()||powerSaveBlocked||!store.boosterUnlocked(id))return;
        int next=board.hint();if(!id.equals("vision")&&!id.equals("shield")&&next<0){notice(t("no_safe_move"));return;}
        int steps=Math.min(PowerUsePolicy.FLOW_MAX_MOVES,board.remainingCount());
        if(id.equals("flow")&&moveBudget.remaining()<steps){notice(t("flow_need_moves",steps));return;}
        long now=SystemClock.uptimeMillis();if(!powerGate.tryBegin(id,powerActive(id),now)){notice(u("power_busy"));return;}
        String request=UUID.randomUUID().toString();boolean saved=false,reserved=false;
        int oldHints=hintsUsed,oldHint=hintId,oldShield=shieldCharges;long oldHintTime=hintUntil,oldVision=visionUntil,oldFocus=focusUntil,oldTrailTime=trailUntil;
        int[] oldTrail=trail.clone(),oldHistory=board.history();HashSet<Integer> oldSafe=new HashSet<>(safeHighlights);MoveBudget oldBudget=moveBudget.copy();String oldRequest=lastPowerRequest;
        try{
            reserved=store.reserveBoosterUse(id,request);if(!reserved){notice(t("no_boosters"));return;}
            hintsUsed++;lastPowerRequest=request;
            if(id.equals("hint")){hintId=next;hintUntil=now+PowerUsePolicy.durationMillis(id);}
            else if(id.equals("vision"))visionUntil=now+PowerUsePolicy.durationMillis(id);
            else if(id.equals("shield"))shieldCharges=PowerUsePolicy.SHIELD_CHARGES;
            else if(id.equals("focus")){safeHighlights.clear();for(Tile tile:board.legalMoves())if(board.isSafeMove(tile.id))safeHighlights.add(tile.id);focusUntil=now+PowerUsePolicy.durationMillis(id);}
            else if(id.equals("trail")){int[] solution=board.solve();trail=Arrays.copyOf(solution,Math.min(PowerUsePolicy.TRAIL_STEPS,solution.length));trailUntil=now+PowerUsePolicy.durationMillis(id);}
            else if(id.equals("flow")){if(!moveBudget.consumeAction()||!board.move(next).success)throw new IllegalStateException("Flow first move failed");flowRemaining=steps-1;hintId=-1;safeHighlights.clear();trailUntil=focusUntil=0;}
            saved=saveSession()||saveSession();if(!saved)throw new IllegalStateException("Power effect save failed");
            if(!store.completeBoosterUse(id,request))throw new IllegalStateException("Power completion missing");
            sfx(AudioEngine.Sound.HINT);log("power_used","item",id,"mode",""+mode);haptic();
            if(board.isSolved())finishLevel();else if(id.equals("flow"))resumeFlow();
        }catch(IllegalStateException failed){
            if(!saved){board.restoreHistory(oldHistory);moveBudget=oldBudget;hintsUsed=oldHints;hintId=oldHint;hintUntil=oldHintTime;visionUntil=oldVision;focusUntil=oldFocus;trailUntil=oldTrailTime;trail=oldTrail;shieldCharges=oldShield;safeHighlights.clear();safeHighlights.addAll(oldSafe);flowRemaining=0;lastPowerRequest=oldRequest;
                if(saveSession()||saveSession()){try{store.cancelBoosterUse(id,request);}catch(IllegalStateException blocked){powerSaveBlocked=true;}}
                else powerSaveBlocked=true;
            }else powerSaveBlocked=true;
            notice(t("commerce_save_error"));
        }finally{if(!id.equals("flow")||flowRemaining==0)powerGate.finish(id,SystemClock.uptimeMillis());invalidate();}
    }
    private void resumeFlow(){
        if(flowScheduledGeneration==actionGeneration||flowRemaining<=0||powerSaveBlocked||!running||paused||help||!screen.equals("play")||board==null||won)return;
        final long token=actionGeneration;flowScheduledGeneration=token;final Board target=board;
        postDelayed(()->{if(flowScheduledGeneration==token)flowScheduledGeneration=-1;if(token!=actionGeneration||target!=board||!running||paused||help||!screen.equals("play")||won||powerSaveBlocked)return;
            int safe=board.hint();if(safe<0||outOfMoves()){flowRemaining=0;powerGate.finish("flow",SystemClock.uptimeMillis());saveSession();return;}
            flowRemaining--;flowExecuting=true;try{movePiece(safe);}finally{flowExecuting=false;}
            if(powerSaveBlocked){if(flowRemaining==0)powerGate.finish("flow",SystemClock.uptimeMillis());return;}
            if(flowRemaining>0&&!won)resumeFlow();else{flowRemaining=0;powerGate.finish("flow",SystemClock.uptimeMillis());saveSession();}invalidate();
        },430);
    }
    private void reconcilePowerReservation(){
        try{store.retryPowerCommit();GameStore.PendingBoosterUse pending=store.pendingBoosterUse();if(pending==null)return;
            if(pending.requestId.equals(lastPowerRequest))store.completeBoosterUse(pending.id,pending.requestId);else store.cancelBoosterUse(pending.id,pending.requestId);
        }catch(IllegalStateException blocked){powerSaveBlocked=true;}
    }
    private long effectRemaining(long until){return Math.max(0,until-(effectsPausedAt>0?effectsPausedAt:SystemClock.uptimeMillis()));}
    private void freezeEffects(){if(effectsPausedAt==0)effectsPausedAt=SystemClock.uptimeMillis();}
    private void thawEffects(){if(effectsPausedAt==0||paused||help||!screen.equals("play"))return;long delta=Math.max(0,SystemClock.uptimeMillis()-effectsPausedAt);hintUntil+=delta;visionUntil+=delta;focusUntil+=delta;trailUntil+=delta;effectsPausedAt=0;}
    private static String joinIds(int[] ids){StringBuilder text=new StringBuilder();for(int id:ids){if(text.length()>0)text.append(',');text.append(id);}return text.toString();}
    private static String joinIds(Set<Integer> ids){int[] values=new int[ids.size()];int i=0;for(int id:ids)values[i++]=id;Arrays.sort(values);return joinIds(values);}
    private static int[] parseIds(String text,int max){try{if(text.isEmpty())return new int[0];String[] split=text.split(",");if(split.length>max)return new int[0];int[] ids=new int[split.length];for(int i=0;i<ids.length;i++){ids[i]=Integer.parseInt(split[i]);if(ids[i]<0||ids[i]>=63)return new int[0];}return ids;}catch(RuntimeException invalid){return new int[0];}}
    private boolean outOfMoves(){return moveBudget!=null&&moveBudget.isExhausted()&&board!=null&&!board.isSolved();}
    private void exhausted(){actionGeneration++;stopTimer();flights.clear();log("moves_exhausted","mode",""+mode,"level",""+levelIndex,"used",""+moveBudget.used());saveSession();announceForAccessibility(t("moves_exhausted"));invalidate();}
    private Level challengeLevel(ChallengeCode code){return code.resolve();}
    private void resetMoveRun(){
        if(!pendingContinuation.isEmpty())com.androidhit.lumadrift.commerce.CommerceActivity.acknowledgeContinuationReward(getContext(),pendingContinuation);
        moveBudget=contentVersion==2?MoveBudget.startLegacy(mode,board.level):MoveBudget.start(mode,board.level);flowRemaining=0;powerGate.cancel();lastPowerRequest="";effectsPausedAt=0;runId=UUID.randomUUID().toString();pendingContinuation=lastAppliedContinuation="";shieldCharges=0;gearGrace=mode==4?0:loadout.blockedGracePerRun();focusUntil=trailUntil=0;trail=new int[0];safeHighlights.clear();
    }
    private boolean prepareAbandon(){
        if(powerSaveBlocked){notice(t("commerce_save_error"));return false;}
        if(!pendingContinuation.isEmpty()&&!pendingContinuation.equals(lastAppliedContinuation)&&((pendingContinuation.startsWith("g:")&&store.hasGoldSpent(pendingContinuation))||(pendingContinuation.startsWith("a:")&&com.androidhit.lumadrift.commerce.CommerceActivity.hasContinuationReward(getContext(),pendingContinuation)))){
            applyPendingContinuation();return false;
        }
        return true;
    }
    private void restartRun(){if(!prepareAbandon())return;moveStreak=0;actionGeneration++;board.restart();resetMoveRun();activeElapsed=0;activeStarted=0;undos=0;hintsUsed=0;coachId=mode==0&&levelIndex==1?board.hint():-1;hintId=-1;visionUntil=0;flights.clear();setPaused(false);restartConfirm=false;saveSession();invalidate();}
    private void continueWithGold(){
        if(!outOfMoves()||!moveBudget.canExtend())return;
        if(!pendingContinuation.isEmpty()&&pendingContinuation.startsWith("a:")){
            if(com.androidhit.lumadrift.commerce.CommerceActivity.hasContinuationReward(getContext(),pendingContinuation)){applyPendingContinuation();return;}
            com.androidhit.lumadrift.commerce.CommerceActivity.acknowledgeContinuationReward(getContext(),pendingContinuation);
        }
        pendingContinuation="g:"+runId+":"+(moveBudget.extensions()+1);
        if(!saveSession()){notice(t("commerce_save_error"));return;}
        try{store.spendGoldOnce(pendingContinuation,MoveBudget.GOLD_EXTENSION_COST);if(!store.hasGoldSpent(pendingContinuation)){notice(t("not_enough"));return;}applyPendingContinuation();}catch(IllegalStateException failed){notice(t("commerce_save_error"));}
    }
    private void continueWithAd(){
        if(!outOfMoves()||!moveBudget.canExtend())return;
        if(pendingContinuation.startsWith("g:")&&store.hasGoldSpent(pendingContinuation)){applyPendingContinuation();return;}
        pendingContinuation="a:"+runId+":"+(moveBudget.extensions()+1);
        if(!saveSession()){notice(t("commerce_save_error"));return;}
        getContext().startActivity(com.androidhit.lumadrift.commerce.CommerceActivity.continuationIntent(getContext(),pendingContinuation).putExtra(com.androidhit.lumadrift.commerce.CommerceActivity.EXTRA_CONTINUATION_MOVES,moveBudget.extensionActions(board.remainingCount())));
    }
    private void applyPendingContinuation(){
        if(board==null||moveBudget==null||pendingContinuation.isEmpty())return;
        String request=pendingContinuation;
        if(request.equals(lastAppliedContinuation)){
            if(!saveSession()){notice(t("commerce_save_error"));return;}
            if(com.androidhit.lumadrift.commerce.CommerceActivity.acknowledgeContinuationReward(getContext(),request)){pendingContinuation="";saveSession();}return;
        }
        if(!request.matches("[ag]:"+runId+":\\d{1,2}")||!outOfMoves()||!moveBudget.canExtend())return;
        boolean earned;
        if(request.startsWith("g:")){
            if(!store.hasGoldSpent(request))return;
            try{store.spendGoldOnce(request,MoveBudget.GOLD_EXTENSION_COST);earned=true;}catch(IllegalStateException failed){notice(t("commerce_save_error"));return;}
        }else earned=com.androidhit.lumadrift.commerce.CommerceActivity.hasContinuationReward(getContext(),request);
        if(!earned)return;
        MoveBudget before=moveBudget,candidate=moveBudget.copy();if(!candidate.extend(board.remainingCount()))return;
        String previous=lastAppliedContinuation;moveBudget=candidate;lastAppliedContinuation=request;
        if(!saveSession()){moveBudget=before;lastAppliedContinuation=previous;notice(t("commerce_save_error"));return;}
        log("moves_extended","source",request.startsWith("g:")?"gold":"rewarded_ad","extra",""+(candidate.total()-before.total()));
        com.androidhit.lumadrift.commerce.CommerceActivity.acknowledgeContinuationReward(getContext(),request);pendingContinuation="";saveSession();
        if(screen.equals("play")&&running&&!paused&&!help)activeStarted=SystemClock.uptimeMillis();notice(t("moves_added",candidate.total()-before.total()));invalidate();
    }
    private void drawOutOfMoves(Canvas c){
        scrim(c);float y=Math.max(30,H/2-260);round(c,24,y,342,520,30,skin.card);wallet(c,262,y+18);mascot(c,195,y+58,29,character,false);
        center(c,t("moves_exhausted"),195,y+111,25,WHITE,true);wrap(c,t("moves_exhausted_note"),195,y+141,290,12,skin.muted,true,17);
        int extra=moveBudget.extensionActions(board.remainingCount());
        String finalBudget=t("moves_left",moveBudget.remaining(),moveBudget.total());fit(c,finalBudget,195,y+183,12,290,WHITE,true,true);label(44,y+168,302,22,finalBudget);
        fit(c,t("moves_retries",moveBudget.extensions(),MoveBudget.MAX_EXTENSIONS),195,y+200,12,300,skin.accent2,false,true);
        if(moveBudget.canExtend()){
            button(c,t("moves_gold",extra,MoveBudget.GOLD_EXTENSION_COST),44,y+221,302,48,GOLD,()->continueWithGold());
            quietButton(c,t("moves_ad",extra),44,y+283,302,()->continueWithAd());
        }else wrap(c,t("moves_cap"),195,y+248,288,13,skin.muted,true,19);
        button(c,t("restart_free"),44,y+347,302,48,skin.accent2,()->restartRun());
        quietButton(c,t("exit"),44,y+412,302,()->{saveSession();go("home");});
        fit(c,t("moves_test_only"),195,y+487,10,300,skin.muted,false,true);
    }
    private String eventPeriod="";
    private void finishLevel(){
        if(won||board==null||!board.isSolved())return;
        stopTimer();resultStars=hintsUsed==0&&undos<=2&&moveBudget.extensions()==0?3:hintsUsed<=2&&moveBudget.extensions()<=1?2:1;
        try{
            if(mode==0)reward=store.completeLevel(levelIndex,resultStars);
            else if(mode==1)reward=store.completeDaily(eventPeriod,eventIndex,resultStars);
            else if(mode==2)reward=store.completeWeekly(eventPeriod,eventIndex,resultStars);
            else if(mode==3)reward=store.completeRemix(levelIndex,resultStars);else reward=0;
            if(mode!=4)reward+=store.claimAvatarVictoryBonus(mode,mode==1||mode==2?eventIndex:levelIndex,eventPeriod);
            boolean badgeBefore=meta.badgeUnlocked();xpReward=meta.recordWin(mode,mode==1||mode==2?eventIndex:levelIndex,resultStars,eventPeriod);
            won=true;flowRemaining=0;powerGate.cancel();syncStyle();
            if(!badgeBefore&&meta.badgeUnlocked())notice(t("badge_unlocked"));
            log("level_complete","mode",""+mode,"level",""+levelIndex,"event_index",""+eventIndex,"period",eventPeriod,"stars",""+resultStars,"powers",""+hintsUsed,"undos",""+undos,"play_ms",""+activeElapsed);
            winAt=SystemClock.uptimeMillis()+(reduceMotion?100:500);sfx(AudioEngine.Sound.WIN);saveSession();announceForAccessibility(t("victory")+", "+resultStars+" "+t("stars"));
        }catch(IllegalStateException failure){won=false;powerSaveBlocked=true;saveSession();notice(t("commerce_save_error"));}
    }
    private void drawSaveRetry(Canvas c){scrim(c);float y=H/2-130;round(c,24,y,342,260,25,skin.card);wrap(c,t("commerce_save_error"),195,y+54,290,17,WHITE,true,25);button(c,t("retry"),44,y+143,302,49,skin.accent2,()->{powerSaveBlocked=!saveSession();if(!powerSaveBlocked){reconcilePowerReservation();if(!powerSaveBlocked){if(board.isSolved())finishLevel();else resumeFlow();}}});}
    private boolean restartConfirm=false;
    private void scrim(Canvas c){round(c,0,0,390,H,0,0xd2101428);hits.clear();}
    private void drawPause(Canvas c){
        scrim(c);float y=H/2-192;round(c,24,y,342,384,29,skin.card);mascot(c,195,y+74,33,character,false);
        center(c,t(restartConfirm?"restart":"paused"),195,y+137,25,WHITE,true);
        if(restartConfirm){wrap(c,t("confirm_restart"),195,y+168,280,13,skin.muted,true,18);
            button(c,t("restart"),44,y+224,302,49,PEACH,()->{restartRun();});
            quietButton(c,t("cancel"),44,y+291,302,()->{setPaused(false);restartConfirm=false;});
        }else{
            button(c,t("resume"),44,y+174,302,49,PEACH,()->setPaused(false));
            quietButton(c,t("how_to_play"),44,y+239,302,()->setHelp(true));
            quietButton(c,t("exit"),44,y+299,302,()->{saveSession();go("home");});
        }
    }
    private void drawWin(Canvas c){
        scrim(c);float y=H/2-266;
        if(!reduceMotion)for(int i=0;i<28;i++){float age=(frameTime-winAt)*.00024f;star(c,(i*83+29)%390,(i*47+age*430)%H,2+i%3,i%2==0?PEACH:skin.accent2);}
        round(c,24,y,342,532,30,skin.card);mascot(c,195,y+76,43,character,true);
        label(44,y+130,302,198,t("victory")+", "+resultStars+" "+t("stars")+", "+t("reward")+": "+reward+" "+t("coins")+", "+t("xp_earned",xpReward));
        center(c,t("victory"),195,y+157,27,WHITE,true);center(c,t("cleared"),195,y+182,13,skin.muted,false);
        for(int k=0;k<3;k++)star(c,141+k*54,y+220,k==1?23:18,k<resultStars?GOLD:alpha(skin.muted,70));
        round(c,81,y+254,228,48,17,alpha(GOLD,15));coin(c,124,y+278,11);text(c,"+"+reward,144,y+286,23,GOLD,true);fit(c,t("xp_earned",xpReward),241,y+284,13,95,skin.accent2,true,true);
        fit(c,mode==4?t("challenge_note"):xpReward==0?t("replay_reward"):t("rank",meta.rank()),195,y+328,11,300,skin.muted,false,true);
        button(c,(mode==4?t("retry"):t("next"))+"  →",44,y+352,302,48,PEACH,()->{
            if(mode==0&&levelIndex<BalancedCatalog.LEVEL_COUNT)previewLevel(levelIndex+1,0,0);
            else if(mode==3&&levelIndex<BalancedCatalog.REMIX_COUNT)previewLevel(levelIndex+1,3,0);
            else if(mode==1&&eventIndex<2&&eventPeriod.equals(store.dailyId()))previewLevel(1,1,eventIndex+1);
            else if(mode==2&&eventIndex<6&&eventPeriod.equals(store.weeklyId()))previewLevel(1,2,eventIndex+1);
            else if(mode==4)openChallenge(friendCode);else go(mode==0||mode==3?"journey":"events");
        });
        quietButton(c,t("share_result"),44,y+415,302,()->shareBoard());quietButton(c,t("journey"),44,y+475,144,()->go("journey"));quietButton(c,t("missions"),202,y+475,144,()->go("missions"));
    }
    private void drawHelp(Canvas c){
        scrim(c);float y=H/2-270;round(c,24,y,342,540,29,skin.card);
        center(c,t("tutorial_title"),195,y+44,25,WHITE,true);
        label(42,y+60,306,385,t("tutorial_tap")+" "+t("tutorial_path")+" "+t("tutorial_gate")+" "+t("tutorial_plan")+" "+t("tutorial_stars"));
        String[] keys={"tutorial_tap","tutorial_path","tutorial_gate","tutorial_plan","tutorial_stars"};
        String[] ico={"flow","vision","events","check","heart"};
        for(int i=0;i<keys.length;i++){float yy=y+78+i*76;round(c,42,yy,35,35,12,alpha(skin.accent,40));icon(c,ico[i],59,yy+17,18,skin.accent2);wrap(c,t(keys[i]),91,yy+13,253,12,WHITE,false,17);}
        button(c,t("got_it"),44,y+465,302,48,PEACH,()->{setHelp(false);prefs.edit().putBoolean("tutorial_seen",true).apply();});
    }
    @Override public boolean onTouchEvent(MotionEvent event){
        float x=(event.getX()-ox)/scale,y=(event.getY()-top)/scale;
        if(event.getAction()==MotionEvent.ACTION_DOWN){downX=x;downY=y;scrollAtDown=contentScroll;scrolling=false;pressed=null;for(int i=hits.size()-1;i>=0;i--)if(hits.get(i).action!=null&&hits.get(i).r.contains(x,y)){pressed=hits.get(i);break;}
            if(screen.equals("play")&&!paused&&!help&&!won&&!outOfMoves()&&board!=null){float boardY=y+contentScroll;int col=(int)((x-bx)/cell),row=(int)((boardY-by)/cell);if(x>=bx&&boardY>=by&&col<board.level.size&&row<board.level.size)for(Tile tile:board.remainingTiles())if(tile.row==row&&tile.col==col)selected=tile.id;}invalidate();return true;}
        if(event.getAction()==MotionEvent.ACTION_MOVE){if(layout!=null&&layout.maximumScroll>0&&Math.abs(y-downY)>10&&Math.abs(y-downY)>Math.abs(x-downX)){scrolling=true;pressed=null;selected=-1;contentScroll=layout.clampScroll(scrollAtDown+downY-y);invalidate();}return true;}
        if(event.getAction()==MotionEvent.ACTION_UP){Hit target=pressed;pressed=null;selected=-1;if(!scrolling&&target!=null&&target.action!=null&&target.r.contains(x,y)&&Math.hypot(x-downX,y-downY)<22){sfx(AudioEngine.Sound.TAP);target.action.run();performClick();sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);}scrolling=false;invalidate();return true;}
        if(event.getAction()==MotionEvent.ACTION_CANCEL){pressed=null;selected=-1;scrolling=false;invalidate();return true;}return true;
    }
    @Override public boolean performClick(){super.performClick();return true;}
    @Override public boolean onKeyDown(int key,KeyEvent e){if(key==KeyEvent.KEYCODE_DPAD_DOWN||key==KeyEvent.KEYCODE_DPAD_RIGHT){focused=(focused+1)%Math.max(1,hits.size());invalidate();return true;}if(key==KeyEvent.KEYCODE_DPAD_UP||key==KeyEvent.KEYCODE_DPAD_LEFT){focused=(focused-1+hits.size())%Math.max(1,hits.size());invalidate();return true;}if(key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER){if(e.getRepeatCount()==0&&focused>=0&&focused<hits.size()&&hits.get(focused).action!=null)hits.get(focused).action.run();return true;}return super.onKeyDown(key,e);}
    private void accessibilityEvent(int id,int type){AccessibilityEvent event=AccessibilityEvent.obtain(type);event.setPackageName(getContext().getPackageName());event.setClassName("android.widget.Button");event.setSource(this,id);if(id>=0&&id<hits.size())event.setContentDescription(hits.get(id).name);getParent().requestSendAccessibilityEvent(this,event);}
    @Override public boolean onHoverEvent(MotionEvent e){AccessibilityManager am=(AccessibilityManager)getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);if(!am.isTouchExplorationEnabled())return super.onHoverEvent(e);float x=(e.getX()-ox)/scale,y=(e.getY()-top)/scale;int id=-1;for(int i=hits.size()-1;i>=0;i--)if(hits.get(i).r.contains(x,y)){id=i;break;}if(e.getAction()==MotionEvent.ACTION_HOVER_EXIT)id=-1;if(id!=hovered){if(hovered>=0)accessibilityEvent(hovered,AccessibilityEvent.TYPE_VIEW_HOVER_EXIT);hovered=id;if(id>=0)accessibilityEvent(id,AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);}return true;}
    @Override public AccessibilityNodeProvider getAccessibilityNodeProvider(){return new AccessibilityNodeProvider(){
        @Override public AccessibilityNodeInfo createAccessibilityNodeInfo(int id){
            if(id==HOST_VIEW_ID){AccessibilityNodeInfo info=AccessibilityNodeInfo.obtain(LumaView.this);onInitializeAccessibilityNodeInfo(info);info.setContentDescription("Luma Drift · "+(screen.equals("briefing")?e("objective"):t(screen)));if(layout!=null&&layout.maximumScroll>0){info.setScrollable(true);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);}for(int i=0;i<hits.size();i++)info.addChild(LumaView.this,i);return info;}
            if(id<0||id>=hits.size())return null;Hit h=hits.get(id);AccessibilityNodeInfo info=AccessibilityNodeInfo.obtain();info.setSource(LumaView.this,id);info.setParent(LumaView.this);info.setPackageName(getContext().getPackageName());info.setClassName(h.action==null?"android.widget.TextView":"android.widget.Button");info.setContentDescription(h.name);info.setText(h.name);info.setClickable(h.action!=null);info.setEnabled(true);info.setFocusable(true);info.setVisibleToUser(true);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_ACCESSIBILITY_FOCUS);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS);info.setAccessibilityFocused(focused==id);Rect r=new Rect((int)(ox+h.r.left*scale),(int)(top+h.r.top*scale),(int)(ox+h.r.right*scale),(int)(top+h.r.bottom*scale));info.setBoundsInParent(r);int[] loc=new int[2];getLocationOnScreen(loc);r.offset(loc[0],loc[1]);info.setBoundsInScreen(r);return info;
        }
        @Override public boolean performAction(int id,int action,Bundle args){if(id==HOST_VIEW_ID&&layout!=null&&(action==AccessibilityNodeInfo.ACTION_SCROLL_FORWARD||action==AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)){contentScroll=layout.clampScroll(contentScroll+(action==AccessibilityNodeInfo.ACTION_SCROLL_FORWARD?1:-1)*viewportH*.65f);invalidate();return true;}if(id<0||id>=hits.size())return false;if(action==AccessibilityNodeInfo.ACTION_CLICK){final Hit target=hits.get(id);if(target.action==null)return false;final long token=actionGeneration;post(()->{if(running&&token==actionGeneration){target.action.run();invalidate();}});return true;}if(action==AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS){focused=id;accessibilityEvent(id,AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);invalidate();return true;}if(action==AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS){focused=-1;invalidate();return true;}return false;}
    };}
}
