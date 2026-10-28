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
import com.androidhit.lumadrift.onboarding.*;
import com.androidhit.lumadrift.identity.*;
import com.androidhit.lumadrift.social.SocialClient;
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
    private SocialClient mapSocial;
    private ExperienceLayout layout;
    private float viewportH=840, contentScroll=0, drawingScroll=0, drawingClipBottom=840, scrollAtDown=0;
    private boolean scrolling=false,navigatingBack=false,drawingMapNodes=false;
    private final ArrayDeque<String> menuHistory=new ArrayDeque<>();
    private int inventoryFilter=0, gearSlotFilter=-1, moveStreak=0;
    private int briefingIndex=1,briefingMode=0,briefingEvent=0;
    private Level briefingLevel;
    private String briefingReturn="home";
    private long gatePulseUntil=0,successUntil=0;
    private float successX=195,successY=350;

    private final KawaiiArt art=new KawaiiArt();
    private final SweetUi sweet=new SweetUi();
    private final AvatarStage avatarStage=new AvatarStage();
    private final CompanionPortrait companionPortrait=new CompanionPortrait();
    private final ArrayList<CosmeticCatalog.Item> wornCosmetics=new ArrayList<>();
    private ObjectiveDashboard.Snapshot hubObjectives;
    private CollectionGoal hubGoal;
    private PrismStory.Snapshot hubStory;
    private long hubRefreshAt;
    private int rewardTab=0,wardrobeMode=0,wardrobeGearSlot=0,shopCategory=0,shopLookPage=0,storyChapter=0;
    private GuardianEncounter guardian;private boolean guardianSaveBlocked,localSaveBlocked;private int guardianHint=-1;
    private String avatarStyle="plush_classic";private int gestureGate=-1;private boolean gestureGuardian;
    private int wardrobeSlot=0,wardrobePage=0,questTab=0,activeBiome=-1;
    private int[] guidanceSavedHistory=new int[0];
    private Board guidanceBoard;private long guidanceMask=-1;private int guidanceGates=-1,guidanceBudget=-2,guidanceHistory=0;private Board.Guidance guidance;
    private final MetaProgress meta;
    private final PlayJournal journal;
    private int cachedStars,cachedCompleted,cachedEconomyProgress,journeyTab=0,xpReward=0;
    private String friendCode="";
    private long activeElapsed=0,activeStarted=0;
    private final ArrayList<Hit> hits = new ArrayList<>();
    private final ArrayList<Flight> flights = new ArrayList<>();
    private final Random random = new Random(72);
    private Catalog.SkinItem skin=Catalog.SKINS[0];
    private Catalog.CharacterItem character=Catalog.CHARACTERS[0];
    private String screen = "home", toast = "";
    private float scale=1, ox=0, top=0, H=820, bottom=0, bx=37, by=250, cell=55;
    private int safeTop, safeBottom, chapter=0, collectionTab=0, collectionPage=0, eventTab=0;
    private Board board;
    private TutorialSession tutorial;
    private long tutorialChangedAt;
    private final ArrayList<Flight> practiceFlights=new ArrayList<>();
    private String tutorialFeedback="";private long tutorialFeedbackUntil;private boolean tutorialSaveBlocked,tutorialLoadBlocked,tutorialGiftClaimed;private float tutorialBoardLeft=69,tutorialBoardTop=278,tutorialBoardWidth=252;
    private int pendingAction=-1,gesturePiece=-1;
    private Board.ActionPreview actionPreview;
    private final java.util.concurrent.ExecutorService guideWorker=new java.util.concurrent.ThreadPoolExecutor(1,1,0L,java.util.concurrent.TimeUnit.MILLISECONDS,new java.util.concurrent.ArrayBlockingQueue<Runnable>(1),new java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy());
    private volatile long guidanceGeneration;private boolean guidancePending;private int[] guidancePlan=new int[0];private final HashSet<Integer> guidanceSafe=new HashSet<>();
    private MoveBudget moveBudget;
    private String runId="", pendingContinuation="", lastAppliedContinuation="";
    private int shieldCharges=0,shopPage=0,gearGrace=0,flowRemaining=0;
    private int contentVersion=5;
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
    private final int PEACH=0xffffb398, WHITE=0xfff7f5ff, TEXT=0xff463252, INK=0xff30203f, GOLD=0xffffd992;
    private static class Hit {
        RectF r; String name; Runnable action; int gate=-1,guardianPiece=-1; boolean selected;
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
            public boolean ownsCosmetic(String id) { return com.androidhit.lumadrift.social.CosmeticVaultClient.ownsCosmetic(c,id); }
        }); prefs=c.getSharedPreferences("luma_session",0);
        meta=new MetaProgress(c);journal=new PlayJournal(c);
        audio=new AudioEngine(c);backdrop=new WorldBackdrop(c);
        identity=new GoogleAccountController((Activity)c,snapshot->{if(mapSocial!=null&&(profileSnapshot==null||!profileSnapshot.uid.equals(snapshot.uid))){mapSocial.close();mapSocial=null;}profileSnapshot=snapshot;profileSignedIn=snapshot.signedIn;profileName=snapshot.displayName;postInvalidate();});
        profileSnapshot=identity.snapshot();chapter=(store.unlockedLevel()-1)/10;
        setFocusable(true); setFocusableInTouchMode(true);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        reduceMotion=prefs.getBoolean("reduce_motion",false)||!android.animation.ValueAnimator.areAnimatorsEnabled();
        syncStyle(); restoreSession();freezeEffects();
        tutorial=TutorialSession.start();
        try{tutorial=TutorialSession.restore(store.tutorialState());tutorialGiftClaimed=store.tutorialRewardClaimed();
            if(store.shouldAutoStartTutorial()){screen="tutorial";if(tutorial.skipped())tutorial.resume();persistTutorial();}
        }catch(IllegalStateException failure){tutorialLoadBlocked=tutorialSaveBlocked=true;screen="tutorial";}
    }
    private String t(String key,Object... args){
        if(key.equals("booster_focus_desc")&&(board==null||board.level.progressionVersion>=4))return n("focus_desc");
        if(key.equals("coins"))return "LumaCoins";if(key.equals("wardrobe")||key.equals("rewards"))return v(key);
        if(!com.androidhit.lumadrift.commerce.CommerceConfig.MODE_SANDBOX&&key.equals("commerce_entry"))key="commerce_title";
        return Strings.get(store.resolvedLanguage(),key,args);
    }
    private String u(String key,Object... args){return UiStrings.get(store.resolvedLanguage(),key,args);}
    private String e(String key,Object...args){return ExperienceStrings.get(store.resolvedLanguage(),key,args);}
    private String v(String key,Object...args){return SagaStrings.get(store.resolvedLanguage(),key,args);}
    private String q(String key,Object...args){return V19Strings.get(store.resolvedLanguage(),key,args);}
    private String a(String key,Object...args){return V110Strings.get(store.resolvedLanguage(),key,args);}
    private String storyText(String key){return PrismStoryStrings.get(store.resolvedLanguage(),key);}
    private String n(String key,Object...args){return V18Strings.get(store.resolvedLanguage(),key,args);}
    private String tt(String key,Object...args){return TutorialStrings.get(store.resolvedLanguage(),key,args);}
    private String w(String key){return V17Strings.get(store.resolvedLanguage(),key);}
    private String worldName(int level){return v("biome_"+SagaMap.biome(level));}
    private void openSocial(){getContext().startActivity(new android.content.Intent(getContext(),com.androidhit.lumadrift.social.SocialActivity.class));}
    private void refreshFriends(){if(!SocialClient.configured()||!profileSignedIn||!SocialClient.hasSavedProfile(getContext()))return;if(mapSocial!=null)mapSocial.close();SocialClient client=new SocialClient(getContext());mapSocial=client;client.refresh(new SocialClient.Listener(){public void onSnapshot(SocialClient.Snapshot value){client.close();if(mapSocial==client)mapSocial=null;postInvalidate();}public void onError(String message){client.close();if(mapSocial==client)mapSocial=null;/* Keep cache or an empty honest map. */}});}
    private void openAccount(){getContext().startActivity(new android.content.Intent(getContext(),com.androidhit.lumadrift.identity.AccountActivity.class));}
    private void syncStyle(){
        try{syncStyleUnsafe();}catch(GameStore.LocalSaveException failure){blockLocalSave();}
    }
    private void syncStyleUnsafe(){
        skin=Catalog.skin(store.equippedSkin()); if(skin==null)skin=Catalog.SKINS[0];
        character=Catalog.character(store.equippedCharacter());if(character==null)character=Catalog.CHARACTERS[0];
        try{store.claimAvatarGearUnlocks();store.claimCosmeticUnlocks();}catch(IllegalStateException failed){/* Keep the last durable wardrobe. */}
        loadout=store.avatarLoadout();gearProgress=store.avatarGearProgress();avatarStyle=store.avatarStyle();
        wornCosmetics.clear();for(CosmeticCatalog.Slot slot:CosmeticCatalog.Slot.values()){CosmeticCatalog.Item item=CosmeticCatalog.find(store.equippedCosmetic(slot));if(item!=null)wornCosmetics.add(item);}
        hubRefreshAt=0;
        profileSignedIn=profileSnapshot!=null&&profileSnapshot.signedIn;
        profileName=profileSignedIn?profileSnapshot.displayName:"";
        audio.setSfxEnabled(store.soundEnabled());audio.setMusicEnabled(store.musicEnabled());cachedStars=store.totalStars();cachedCompleted=store.completedLevels();cachedEconomyProgress=store.economyProgress();
    }
    public void setSafeInsets(int a,int b){safeTop=a;safeBottom=b;invalidate();}
    public void resumeApp(){
        try{resumeAppUnsafe();}catch(GameStore.LocalSaveException failure){blockLocalSave();}
    }
    private void resumeAppUnsafe(){running=true;identity.onResume();thawEffects();if(powerSaveBlocked){powerSaveBlocked=!saveSession();if(!powerSaveBlocked)reconcilePowerReservation();}if(board!=null&&board.isSolved()&&!won)finishLevel();resumeFlow();meta.recordVisit();if(screen.equals("play")&&!paused&&!help&&!won&&!outOfMoves()&&activeStarted==0)activeStarted=SystemClock.uptimeMillis();applyPendingContinuation();if(prefs.getBoolean("session_migration_notice",false)){notice(t("moves_migration"));prefs.edit().putBoolean("session_migration_notice",false).commit();}log("session_start","version",BuildConfig.VERSION_NAME,"language",store.resolvedLanguage());syncStyle();audio.resume();refreshFriends();invalidate();}
    public void pauseApp(){if(guardian!=null)persistGuardian();identity.onPause();actionGeneration++;freezeEffects();stopTimer();saveSession();running=false;audio.pause();}
    public void release(){if(mapSocial!=null){mapSocial.close();mapSocial=null;}identity.close();actionGeneration++;running=false;audio.release();backdrop.close();guideWorker.shutdownNow();}
    public String screenName(){return screen;}
    public void saveUiState(Bundle out){
        out.putInt("reward_tab_v110",rewardTab);out.putInt("wardrobe_mode_v110",wardrobeMode);out.putInt("wardrobe_gear_v110",wardrobeGearSlot);out.putInt("shop_category_v110",shopCategory);out.putInt("shop_look_page_v110",shopLookPage);out.putInt("story_chapter_v110",storyChapter);
        out.putString("screen",screen);out.putBoolean("ui_paused",paused);out.putBoolean("ui_help",help);out.putBoolean("ui_restart",restartConfirm);out.putInt("wardrobe_slot",wardrobeSlot);out.putInt("wardrobe_page",wardrobePage);out.putInt("quest_tab",questTab);out.putInt("chapter",chapter);out.putInt("journey_tab",journeyTab);out.putInt("collection_tab",collectionTab);out.putInt("collection_page",collectionPage);out.putInt("inventory_filter",inventoryFilter);out.putInt("gear_slot",gearSlotFilter);out.putInt("event_tab",eventTab);out.putInt("shop_page",shopPage);out.putFloat("content_scroll",contentScroll);out.putStringArrayList("menu_history",new ArrayList<>(menuHistory));
        if(guardian!=null){out.putString("guardian_ui_attempt",guardian.encode());out.putBoolean("guardian_ui_save_blocked",guardianSaveBlocked);}
        if(briefingLevel!=null){out.putInt("preview_index",briefingIndex);out.putInt("preview_mode",briefingMode);out.putInt("preview_event",briefingEvent);out.putString("preview_return",briefingReturn);}
    }
    public void restoreUiState(Bundle state){
        rewardTab=Math.max(0,Math.min(3,state.getInt("reward_tab_v110",0)));wardrobeMode=Math.max(0,Math.min(1,state.getInt("wardrobe_mode_v110",0)));wardrobeGearSlot=Math.max(0,Math.min(2,state.getInt("wardrobe_gear_v110",0)));shopCategory=Math.max(0,Math.min(2,state.getInt("shop_category_v110",0)));shopLookPage=Math.max(0,Math.min(2,state.getInt("shop_look_page_v110",0)));storyChapter=Math.max(0,Math.min(10,state.getInt("story_chapter_v110",0)));
        wardrobeSlot=Math.max(0,Math.min(3,state.getInt("wardrobe_slot",0)));wardrobePage=Math.max(0,Math.min(1,state.getInt("wardrobe_page",0)));questTab=state.getInt("quest_tab",0)==1?1:0;chapter=Math.max(0,state.getInt("chapter",chapter));journeyTab=state.getInt("journey_tab",0)==1?1:0;collectionTab=Math.max(0,Math.min(3,state.getInt("collection_tab",0)));collectionPage=Math.max(0,state.getInt("collection_page",0));inventoryFilter=Math.max(0,Math.min(2,state.getInt("inventory_filter",0)));gearSlotFilter=Math.max(-1,Math.min(2,state.getInt("gear_slot",-1)));eventTab=Math.max(0,Math.min(2,state.getInt("event_tab",0)));shopPage=Math.max(0,Math.min(1,state.getInt("shop_page",0)));
        if(state.containsKey("preview_index")){briefingMode=Math.max(0,Math.min(3,state.getInt("preview_mode",0)));briefingIndex=Math.max(1,Math.min(briefingMode==3?BalancedCatalog.REMIX_COUNT:BalancedCatalog.LEVEL_COUNT,state.getInt("preview_index",1)));briefingEvent=Math.max(0,Math.min(briefingMode==1?2:6,state.getInt("preview_event",0)));briefingReturn=state.getString("preview_return","home");briefingLevel=briefingMode==0?StrategyCatalog.get(briefingIndex):briefingMode==3?StrategyCatalog.remix(briefingIndex):StrategyCatalog.event(briefingMode==1?store.dailySeed(briefingEvent):store.weeklySeed(briefingEvent));}
        GuardianEncounter restoredGuardian=GuardianEncounter.restore(state.getString("guardian_ui_attempt",""));
        if(restoredGuardian!=null&&store.guardianUnlocked(restoredGuardian.definition.number)){guardian=restoredGuardian;guardianSaveBlocked=state.getBoolean("guardian_ui_save_blocked",false);}
        restoreScreen(state.getString("screen"));if(screen.equals("play")){paused=state.getBoolean("ui_paused",false);help=state.getBoolean("ui_help",false);restartConfirm=paused&&state.getBoolean("ui_restart",false);if(paused||help){actionGeneration++;stopTimer();freezeEffects();}}menuHistory.clear();ArrayList<String> history=state.getStringArrayList("menu_history");if(history!=null)for(String item:history)if(isMenu(item))menuHistory.addLast(item);contentScroll=Math.max(0,state.getFloat("content_scroll",0));
    }
    private boolean isMenu(String value){return Arrays.asList("home","journey","collection","events","shop","settings","briefing","missions","journal","wardrobe","rewards","tutorial","styles","guardians","story").contains(value);}
    public void restoreScreen(String name){
        if("guardian".equals(name)){
            if(guardian==null)guardian=GuardianEncounter.restore(prefs.getString("guardian_v19_session",""));
            guardianHint=-1;
            go(guardian!=null&&store.guardianUnlocked(guardian.definition.number)?"guardian":"guardians");
        }else if(name!=null&&(isMenu(name)||name.equals("play")&&board!=null))go(name);
    }
    public void back(){actionGeneration++;
        if(localSaveBlocked){retryLocalSave();return;}
        if(screen.equals("guardian")){if(persistGuardian())go("guardians");return;}
        if(screen.equals("tutorial")){leaveTutorial();return;}
        if(pendingAction>=0){pendingAction=-1;actionPreview=null;invalidate();return;}
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
        contentScroll=s.equals("journey")?SagaMap.scrollTo(journeyTab==1?store.remixUnlockedLevel():store.unlockedLevel(),viewportH,journeyTab==1?BalancedCatalog.REMIX_COUNT:BalancedCatalog.LEVEL_COUNT):0;scrolling=false;
        if(screen.equals("play")&&!s.equals("play")&&board!=null&&!won)log("level_exit","mode",""+mode,"level",""+levelIndex,"remaining",""+board.remainingCount(),"event_index",""+eventIndex,"period",eventPeriod);
        stopTimer();actionGeneration++;pendingAction=-1;actionPreview=null;gesturePiece=-1;screen=s;paused=false;restartConfirm=false;help=false;selected=-1;pressed=null;focused=hovered=-1;hits.clear();syncStyle();
        if(s.equals("play")&&!won&&running&&!outOfMoves()){thawEffects();activeStarted=SystemClock.uptimeMillis();resumeFlow();}else freezeEffects();
        invalidate();if(Build.VERSION.SDK_INT>=28)setAccessibilityPaneTitle(screenTitle(s));sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
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
        levelIndex=Math.max(1,Math.min(m==3?BalancedCatalog.REMIX_COUNT:BalancedCatalog.LEVEL_COUNT,index));contentVersion=5;moveStreak=0;mode=m;eventIndex=e;eventPeriod=m==1?store.dailyId():m==2?store.weeklyId():m==3?"remix-v1":"";
        Level l=m==0?StrategyCatalog.get(levelIndex):m==3?StrategyCatalog.remix(levelIndex):StrategyCatalog.event(m==1?store.dailySeed(e):store.weeklySeed(e));
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
        if(board==null)return ChallengeCode.encodeStrategy(Math.min(BalancedCatalog.LEVEL_COUNT,store.unlockedLevel()));
        if(mode==0)return contentVersion==2?ChallengeCode.encodeBalanced(levelIndex):contentVersion==3?ChallengeCode.encodeBalancedV2(levelIndex):contentVersion==4?ChallengeCode.encodeBalancedV3(levelIndex):ChallengeCode.encodeStrategy(levelIndex);
        if(mode==3)return contentVersion==2?ChallengeCode.encodeBalancedRemix(levelIndex):contentVersion==3?ChallengeCode.encodeBalancedRemixV2(levelIndex):contentVersion==4?ChallengeCode.encodeBalancedRemixV3(levelIndex):ChallengeCode.encodeStrategyRemix(levelIndex);
        return board.level.progressionVersion>=4?ChallengeCode.encodeStrategyEvent(board.level.seed):ChallengeCode.encodeEvent(board.level.seed);
    }
    private void shareBoard(){
        try{Level level=board==null?StrategyCatalog.get(Math.min(BalancedCatalog.LEVEL_COUNT,store.unlockedLevel())):board.level;ShareComposer.shareChallenge((Activity)getContext(),level,currentCode(),store.resolvedLanguage(),won?resultStars:0,character.color);log("share_handoff","kind",mode==4?"friend":""+mode);}
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
        if(prefs.getInt("content_version",0)!=2&&prefs.getInt("content_version",0)!=3&&prefs.getInt("content_version",0)!=4&&prefs.getInt("content_version",0)!=5){prefs.edit().putBoolean("active",false).putBoolean("session_migration_notice",true).commit();return;}
        try{
            contentVersion=prefs.getInt("content_version",2);
            int m=prefs.getInt("mode",0), e=prefs.getInt("event",0), l=prefs.getInt("level",1);
            String id=prefs.getString("event_id","");
            if(m==1&&!id.equals(store.dailyId())||m==2&&!id.equals(store.weeklyId()))return;
            if(m<0||m>4||m==0&&!store.isLevelUnlocked(l)||m==1&&(e<0||e>2)||m==2&&(e<0||e>6)||m==3&&(!store.remixAvailable()||l<1||l>store.remixUnlockedLevel()))return;
            String savedCode=prefs.getString("challenge_code","");ChallengeCode friend=m==4?ChallengeCode.parse(savedCode):null;if(m==4&&friend==null)return;
            Level lev=m==0?(contentVersion==2?BalancedCatalog.legacyGet(l):contentVersion==3?BalancedCatalog.getV2(l):contentVersion==4?BalancedCatalog.get(l):StrategyCatalog.get(l)):m==3?(contentVersion==2?BalancedCatalog.legacyRemix(l):contentVersion==3?BalancedCatalog.remixV2(l):contentVersion==4?BalancedCatalog.remix(l):StrategyCatalog.remix(l)):m==4?challengeLevel(friend):contentVersion>=5?StrategyCatalog.event(m==1?store.dailySeed(e):store.weeklySeed(e)):LevelCatalog.event(m==1?store.dailySeed(e):store.weeklySeed(e));
            Board b=new Board(lev);String raw=prefs.getString("history","");String[] parts=raw.isEmpty()?new String[0]:raw.split(",");int[] moves=new int[parts.length];
            for(int i=0;i<moves.length;i++)moves[i]=Integer.parseInt(parts[i]);
            if(b.restoreHistory(moves)){
                int policy=prefs.getInt("budget_policy",0);
                MoveBudget restored=MoveBudget.restoreVersion(policy,m,lev,prefs.getInt("budget_used",-1),prefs.getInt("budget_extensions",-1),prefs.getInt("budget_extra",-1),b.moves(),prefs.getInt("undos",0));
                String restoredRun=prefs.getString("run_id","");
                if(policy==2&&restored!=null)restored=MoveBudget.toReversible(m,lev,prefs.getInt("budget_used",-1),prefs.getInt("budget_extensions",-1),prefs.getInt("budget_extra",-1),b.moves(),prefs.getInt("undos",0));
                if(restored==null||!restoredRun.matches("[a-f0-9-]{36}"))return;
                moveBudget=restored;runId=restoredRun;pendingContinuation=prefs.getString("pending_continuation","");lastAppliedContinuation=prefs.getString("last_continuation","");
                if(!pendingContinuation.isEmpty()&&!pendingContinuation.matches("[ag]:"+runId+":\\d{1,2}"))pendingContinuation="";
                shieldCharges=Math.max(0,Math.min(3,prefs.getInt("shield_charges",0)));board=b;mode=m;eventIndex=e;levelIndex=l;eventPeriod=id;friendCode=savedCode;activeElapsed=Math.max(0,prefs.getLong("elapsed_ms",0));hintsUsed=prefs.getInt("hints",0);undos=prefs.getInt("undos",0);
                gearGrace=Math.max(0,Math.min(1,prefs.getInt("gear_grace",0)));flowRemaining=Math.max(0,Math.min(2,prefs.getInt("flow_remaining",0)));
                long now=SystemClock.uptimeMillis();hintId=prefs.getInt("hint_id",-1);hintUntil=now+Math.min(8000,Math.max(0,prefs.getLong("hint_ms",0)));
                visionUntil=now+Math.min(8000,Math.max(0,prefs.getLong("vision_ms",0)));focusUntil=now+Math.min(10000,Math.max(0,prefs.getLong("focus_ms",0)));trailUntil=now+Math.min(12000,Math.max(0,prefs.getLong("trail_ms",0)));
                trail=parseIds(prefs.getString("trail_ids",""),5);for(int safe:parseIds(prefs.getString("focus_ids",""),68))if(board.isPresent(safe)||Board.isGateAction(safe)&&board.canPerformAction(safe))safeHighlights.add(safe);
                if(flowRemaining>0)powerGate.tryBegin("flow",false,now);
                if(policy==2&&!saveSession())powerSaveBlocked=true;
            }
        }catch(RuntimeException ignored){prefs.edit().putBoolean("active",false).apply();}
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);int checkpoint=c.save();
        try{if(localSaveBlocked)drawLocalSaveRetry(c);else drawFrame(c);}
        catch(GameStore.LocalSaveException failure){c.restoreToCount(checkpoint);checkpoint=c.save();blockLocalSave();drawLocalSaveRetry(c);}
        finally{c.restoreToCount(checkpoint);}
    }
    private void blockLocalSave(){
        localSaveBlocked=true;actionGeneration++;stopTimer();freezeEffects();pressed=null;gesturePiece=gestureGate=-1;gestureGuardian=false;hits.clear();invalidate();
    }
    private void retryLocalSave(){
        try{store.retryPendingLocalSave();localSaveBlocked=false;syncStyle();if(!localSaveBlocked)resumeApp();}
        catch(GameStore.LocalSaveException failure){blockLocalSave();}
        invalidate();
    }
    private void drawLocalSaveRetry(Canvas c){
        frameTime=SystemClock.uptimeMillis();layout=new ExperienceLayout(getWidth(),getHeight(),safeTop,safeBottom,getResources().getDisplayMetrics().density,getResources().getConfiguration().fontScale);
        scale=layout.scale;ox=layout.offsetX;top=safeTop;viewportH=layout.viewportHeight;drawingScroll=0;drawingClipBottom=viewportH;hits.clear();
        c.drawColor(skin.bg);c.save();c.translate(ox,top);c.scale(scale,scale);float y=Math.max(16,viewportH/2-130);
        sweet.surface(c,new RectF(24,y,366,y+260),SweetUi.CREAM,true);
        wrap(c,t("commerce_save_error"),195,y+58,290,18,TEXT,true,25);label(44,y+25,302,103,t("commerce_save_error"));
        button(c,t("retry"),44,y+151,302,52,PEACH,()->retryLocalSave());c.restore();if(running)postInvalidateDelayed(500);
    }
    private void drawFrame(Canvas c){
        frameTime=SystemClock.uptimeMillis();
        layout=new ExperienceLayout(getWidth(),getHeight(),safeTop,safeBottom,getResources().getDisplayMetrics().density,getResources().getConfiguration().fontScale);
        scale=layout.scale;ox=layout.offsetX;top=safeTop;viewportH=layout.viewportHeight;H=screen.equals("journey")&&!(journeyTab==1&&!store.remixAvailable())?SagaMap.height(journeyTab==1?BalancedCatalog.REMIX_COUNT:BalancedCatalog.LEVEL_COUNT):Math.max(layout.contentHeight,screen.equals("guardians")?4570:screen.equals("guardian")?900:screen.equals("styles")?1140:screen.equals("rewards")?rewardContentHeight():screen.equals("wardrobe")?1290:screen.equals("shop")?1060:screen.equals("story")?1040:screen.equals("home")?1110:screen.equals("briefing")?950:screen.equals("tutorial")?viewportH<480&&!tutorial.complete()?433:820:840);
        if(screen.equals("tutorial")&&viewportH<480&&!tutorial.complete())H=Math.max(viewportH,433);
        contentScroll=clampScroll(contentScroll);
        boolean navigation=!screen.equals("play")&&!screen.equals("settings")&&!screen.equals("briefing")&&!screen.equals("tutorial")&&!screen.equals("guardian");
        c.drawColor(skin.bg);c.save();c.translate(ox,top);c.scale(scale,scale);hits.clear();
        background(c);c.save();drawingScroll=contentScroll;drawingClipBottom=viewportH-(navigation?95:0);
        c.clipRect(0,0,390,drawingClipBottom);c.translate(0,-contentScroll);
        switch(screen){
            case "story":drawPrismStory(c);break;case "guardians":drawGuardians(c);break;case "guardian":drawGuardian(c);break;case "styles":drawStyles(c);break;case "tutorial":drawTutorial(c);break;case "play":drawPlay(c);break;case "journey":drawJourney(c);break;
            case "briefing":drawBriefing(c);break;case "shop":drawShop(c);break;case "collection":drawCollection(c);break;
            case "wardrobe":drawWardrobe(c);break;case "rewards":drawRewards(c);break;case "events":drawEvents(c);break;case "settings":drawSettings(c);break;case "missions":drawMissions(c);break;case "journal":drawJournal(c);break;
            default:drawHome(c);
        }
        if(help)drawHelp(c);c.restore();drawingScroll=0;drawingClipBottom=viewportH;
        if(navigation){float contentHeight=H;H=viewportH;nav(c);H=contentHeight;}
        if(frameTime<toastUntil&&!screen.equals("play")){float y=Math.max(8,viewportH-(navigation?169:90));round(c,24,y,342,65,20,0xfff6e9ff);wrap(c,toast,195,y+25,302,14,TEXT,true,19);}
        if(focused>=0&&focused<hits.size()){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(WHITE);c.drawRoundRect(hits.get(focused).r,15,15,p);p.setStyle(Paint.Style.FILL);}
        if(maximumScroll()>0){float track=viewportH-24,thumb=Math.max(25,track*viewportH/H),yy=12+(track-thumb)*contentScroll/maximumScroll();round(c,384,yy,3,thumb,2,alpha(WHITE,105));}
        c.restore();if(running)postInvalidateDelayed(reduceMotion?250:screen.equals("play")?20:50);
    }
    private float maximumScroll(){return localSaveBlocked?0:Math.max(0,H-viewportH);}
    private float clampScroll(float amount){return Math.max(0,Math.min(maximumScroll(),amount));}
    private void background(Canvas c){
        int visibleLevel=screen.equals("journey")?SagaMap.at(contentScroll+viewportH*.5f,journeyTab==1?BalancedCatalog.REMIX_COUNT:BalancedCatalog.LEVEL_COUNT):screen.equals("play")?levelIndex:store.unlockedLevel();int biome=SagaMap.biome(visibleLevel);if(biome!=activeBiome){activeBiome=biome;audio.setBiome(biome);}backdrop.setBiome(biome);
        backdrop.draw(c,390,viewportH,screen.equals("play")||screen.equals("collection")||screen.equals("shop")||screen.equals("settings"),skin.bg);
        if(screen.equals("play")||screen.equals("guardian")){float progress=screen.equals("guardian")&&guardian!=null?1f-guardian.snapshot().remainingWork/(float)guardian.snapshot().totalWork:board==null?0:1f-board.remainingCount()/(float)board.level.tiles.size();avatarStage.drawBackdrop(c,390,viewportH,avatarStyle,progress,frameTime,reduceMotion);}
        for(int i=0;i<18;i++){float x=(i*79+23)%390,y=(i*137+42)%Math.max(1,(int)viewportH);float pulse=reduceMotion?1f:.65f+.35f*(float)Math.sin(frameTime*.0008+i);p.setColor(alpha(WHITE,(int)(35*pulse)));c.drawCircle(x,y,i%4==0?1.4f:.7f,p);}
    }
    private static int alpha(int c,int a){return (c&0x00ffffff)|((Math.max(0,Math.min(255,a)))<<24);}
    private void round(Canvas c,float x,float y,float w,float h,float r,int color){p.setColor(color);p.setShader(null);p.setStyle(Paint.Style.FILL);c.drawRoundRect(x,y,x+w,y+h,r,r,p);}
    private void stroke(Canvas c,RectF r,float radius,int color,float width){p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(width);c.drawRoundRect(r,radius,radius,p);p.setStyle(Paint.Style.FILL);}
    private float minimumTextSize(){return layout==null?12:layout.minimumTextSize;}
    /** Preserve hue while keeping UI text readable on the new light surfaces. */
    private int readableText(int color){
        int opaque=color|0xff000000;double luminance=Color.luminance(opaque);
        if(luminance>.22)opaque=blend(opaque,INK,.60f);
        return opaque;
    }
    private void text(Canvas c,String str,float x,float y,float size,int color,boolean heavy){
        p.setShader(null);p.setTypeface(heavy?bold:regular);p.setTextSize(Math.max(minimumTextSize(),size));p.setColor(readableText(color));p.setTextAlign(Paint.Align.LEFT);c.drawText(str,x,y,p);
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
        if(r.intersect(0,drawingMapNodes?SagaMap.header(viewportH):0,390,drawingClipBottom))hits.add(new Hit(r,name,action));
    }
    private void gateHit(float x,float y,Direction direction,String name,Runnable action){
        int before=hits.size();hit(x-25,y-25,50,50,name,action);if(hits.size()>before)hits.get(before).gate=direction.ordinal();
    }
    private void guardianHit(float x,float y,float size,int piece,String name,Runnable action){
        int before=hits.size();hit(x,y,size,size,name,action);if(hits.size()>before)hits.get(before).guardianPiece=piece;
    }
    private void label(float x,float y,float w,float h,String name){hit(x,y,w,h,name,null);}
    private void button(Canvas c,String label,float x,float y,float w,float h,int color,Runnable action){
        int tone=color==PEACH?SweetUi.PEACH:color==GOLD?SweetUi.CREAM:SweetUi.MINT;
        boolean down=pressed!=null&&pressed.name.equals(label);
        sweet.button(c,new RectF(x,y,x+w,y+h),tone,down);
        fit(c,label,x+w/2,y+h/2+6,17,w-24,INK,true,true);hit(x,y,w,Math.max(48,h),label,action);
    }
    private void quietButton(Canvas c,String label,float x,float y,float w,Runnable action){
        sweet.surface(c,new RectF(x,y,x+w,y+47),SweetUi.LILAC,false);
        stroke(c,new RectF(x,y,x+w,y+47),15,0xffd5bfdf,1);
        fit(c,label,x+w/2,y+29,14,w-14,TEXT,true,true);hit(x,y,w,48,label,action);
    }
    private void header(Canvas c,String title,String subtitle){
        fit(c,title,24,54,30,242,TEXT,true,false);if(subtitle!=null)fit(c,subtitle,25,80,12,330,skin.muted,false,false);wallet(c,282,15);
    }
    private void wallet(Canvas c,float x,float y){hit(x,y-6,84,48,t("coins")+": "+store.coins(),()->{if(!screen.equals("shop"))go("shop");});round(c,x,y,84,35,18,alpha(skin.card,225));coin(c,x+18,y+17,8);fit(c,""+store.coins(),x+33,y+23,14,47,GOLD,true,false);}
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
    private void openTutorial(boolean replay){
        if(powerSaveBlocked){notice(t("commerce_save_error"));return;}
        if(tutorialSaveBlocked){go("tutorial");return;}
        try{
            tutorial=TutorialSession.restore(store.tutorialState());tutorialGiftClaimed=store.tutorialRewardClaimed();
            if(replay||tutorial.complete()&&tutorialGiftClaimed)tutorial.replay();else if(tutorial.skipped())tutorial.resume();
            persistTutorial();tutorialFeedback="";tutorialChangedAt=SystemClock.uptimeMillis();practiceFlights.clear();go("tutorial");
        }catch(IllegalStateException failure){tutorialLoadBlocked=tutorialSaveBlocked=true;go("tutorial");}
    }
    private void retryTutorialSave(){
        try{
            if(tutorialLoadBlocked){tutorial=TutorialSession.restore(store.tutorialState());tutorialGiftClaimed=store.tutorialRewardClaimed();tutorialLoadBlocked=false;}
            if(persistTutorial()){
                tutorialGiftClaimed=store.tutorialRewardClaimed();
                if(tutorial.skipped())goRoot("home");
            }
        }catch(IllegalStateException failure){tutorialSaveBlocked=true;notice(t("save_error"));}
    }
    private boolean persistTutorial(){
        try{store.saveTutorialState(tutorial.encode());tutorialSaveBlocked=false;return true;}
        catch(IllegalStateException failure){tutorialSaveBlocked=true;notice(t("save_error"));return false;}
    }
    private void leaveTutorial(){
        if(tutorialSaveBlocked){retryTutorialSave();return;}
        if(!tutorial.complete())tutorial.skip();
        if(persistTutorial()){practiceFlights.clear();goRoot("home");}
    }
    private void practiceAction(java.util.function.Supplier<TutorialSession.Action> operation){
        if(tutorialSaveBlocked){notice(t("save_error"));return;}
        TutorialSession.Action result=operation.get();
        if(result.changed&&!persistTutorial())return;
        if(result.move!=null){
            if(result.move.success){
                sfx(result.move.tile==null||result.move.tile.isSwitch()?AudioEngine.Sound.GATE:AudioEngine.Sound.MOVE);
                if(result.move.tile!=null){Tile tile=result.move.tile;float unit=tutorialBoardWidth/tutorial.board().level.size;practiceFlights.add(new Flight(tile,SystemClock.uptimeMillis(),tutorialBoardLeft+(tile.col+.5f)*unit,tutorialBoardTop+(tile.row+.5f)*unit,directionColor(tile.direction)));}
            }else{sfx(AudioEngine.Sound.BLOCKED);haptic();}
        }else sfx(result.hintTileId>=0?AudioEngine.Sound.HINT:AudioEngine.Sound.SELECT);
        tutorialFeedback=result.feedbackKey;tutorialFeedbackUntil=SystemClock.uptimeMillis()+2400;tutorialChangedAt=SystemClock.uptimeMillis();
        if(result.accepted)announceForAccessibility(tt(result.feedbackKey));
        selected=-1;pressed=null;gesturePiece=-1;actionGeneration++;invalidate();sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
    }
    private void practicePiece(int id,Direction swipe){
        if(tutorialSaveBlocked){notice(t("save_error"));return;}
        if(tutorial.requiredAction()==TutorialSession.RequiredAction.PREVIEW_SWITCH)practiceAction(()->tutorial.previewSwitch(id));
        else practiceAction(()->tutorial.move(id,swipe));
    }
    private void practiceGate(Direction direction){
        if(tutorialSaveBlocked){notice(t("save_error"));return;}
        if(tutorial.requiredAction()==TutorialSession.RequiredAction.PREVIEW_GATE)practiceAction(()->tutorial.previewGate(direction));
        else practiceAction(()->tutorial.openGate(direction));
    }
    private void claimPracticeGift(){
        if(!persistTutorial())return;
        try{GameStore.TutorialRewardStatus state=store.claimTutorialReward();
            if(state==GameStore.TutorialRewardStatus.GRANTED){tutorialGiftClaimed=true;syncStyle();sfx(AudioEngine.Sound.COIN);notice(n("learn_reward"));}
            else if(state==GameStore.TutorialRewardStatus.ALREADY_CLAIMED){tutorialGiftClaimed=true;notice(n("tutorial_collected"));}
            else notice(state==GameStore.TutorialRewardStatus.STORAGE_FULL?tt("reward_full"):t("save_error"));
        }catch(IllegalStateException failure){tutorialSaveBlocked=true;notice(t("save_error"));}
    }
    private void drawTutorial(Canvas c){
        if(tutorialSaveBlocked){
            hits.clear();sweet.surface(c,new RectF(24,155,366,485),SweetUi.CREAM,true);
            wrap(c,t("commerce_save_error"),195,218,295,18,TEXT,true,27);
            button(c,t("retry"),44,387,302,53,PEACH,()->retryTutorialSave());return;
        }
        if(viewportH<480&&!tutorial.complete()){drawTutorialCompact(c);return;}
        fit(c,n("learn"),195,43,24,340,TEXT,true,true);
        if(tutorial.complete()){
            sweet.surface(c,new RectF(24,103,366,668),SweetUi.CREAM,true);
            sweet.mascot(c,195,218,65,frameTime,reduceMotion,SweetUi.CELEBRATE);
            fit(c,n("tutorial_done"),195,334,25,300,TEXT,true,true);
            wrap(c,n("learn_reward"),195,377,290,19,TEXT,true,27);
            fit(c,n("learn_note"),195,448,15,293,skin.muted,false,true);
            button(c,tutorialGiftClaimed?n("tutorial_collected"):n("tutorial_claim"),44,489,302,55,GOLD,()->claimPracticeGift());
            button(c,n("start_adventure"),44,565,302,53,PEACH,()->previewLevel(Math.min(BalancedCatalog.LEVEL_COUNT,store.unlockedLevel()),0,0));
            quietButton(c,n("learn_replay"),24,699,342,()->openTutorial(true));return;
        }
        fit(c,n("lesson",tutorial.lessonIndex()+1,tutorial.lessonCount()),24,76,14,240,skin.accent,true,false);
        fit(c,n("later"),326,76,13,74,TEXT,true,true);hit(282,52,90,47,n("later"),()->leaveTutorial());
        float entrance=reduceMotion?1f:Math.min(1f,(frameTime-tutorialChangedAt)/240f);float dy=(1-entrance)*(1-entrance)*10;
        c.save();c.translate(0,dy);
        sweet.surface(c,new RectF(24,101,366,239),tutorial.lessonComplete()?SweetUi.MINT:SweetUi.CREAM,true);
        sweet.mascot(c,62,154,24,frameTime,reduceMotion,tutorial.lessonComplete()?SweetUi.CELEBRATE:SweetUi.HAPPY);
        fit(c,tt("title_"+tutorial.lessonKey()),105,132,18,240,TEXT,true,false);
        wrap(c,tt(tutorial.instructionKey()),105,159,235,14,TEXT,false,21);
        label(24,101,342,138,tt("title_"+tutorial.lessonKey())+". "+tt(tutorial.instructionKey()));c.restore();
        drawTutorialBoard(c,69,278,252);
        Board.ActionPreview preview=tutorial.currentPreview();
        float y=575;
        if(tutorial.lessonComplete())button(c,n("continue_lesson")+"  →",24,y,342,53,PEACH,()->practiceAction(()->tutorial.advance()));
        else if(tutorial.requiredAction()==TutorialSession.RequiredAction.HINT)button(c,n("practice_hint"),24,y,342,53,GOLD,()->practiceAction(()->tutorial.hint()));
        else if(tutorial.requiredAction()==TutorialSession.RequiredAction.UNDO)button(c,n("practice_undo"),24,y,342,53,PEACH,()->practiceAction(()->tutorial.undo()));
        else if(tutorial.requiredAction()==TutorialSession.RequiredAction.PREVIEW_GATE)button(c,q("preview_gate"),24,y,342,53,PEACH,()->practiceGate(tutorial.expectedGate()));
        else if(preview!=null){
            StringBuilder changes=new StringBuilder();for(Direction direction:Direction.values())if(((preview.openedMask|preview.closedMask)&direction.bit)!=0){if(changes.length()>0)changes.append(" · ");changes.append(n((preview.openedMask&direction.bit)!=0?"opens":"closes",t("direction_"+direction.name().toLowerCase(Locale.ROOT))));}
            fit(c,changes.toString(),195,225,13,315,TEXT,true,true);
            button(c,tutorial.expectedGate()!=null?n("opening",t("direction_up")):n("confirm_move"),24,575,342,53,PEACH,()->{if(tutorial.expectedGate()!=null)practiceGate(tutorial.expectedGate());else practicePiece(tutorial.expectedTileId(),null);});
        }else fit(c,frameTime<tutorialFeedbackUntil?tt(tutorialFeedback):n("learn_practice"),195,602,14,338,skin.muted,false,true);
        for(int i=0;i<tutorial.lessonCount();i++)sweet.stepChip(c,90+i*35,687,10,i==tutorial.lessonIndex(),i<tutorial.lessonIndex());
        quietButton(c,n("learn_skip"),24,721,342,()->leaveTutorial());
        fit(c,n("learn_skip_note"),195,792,12,340,skin.muted,false,true);
    }

    private void drawTutorialCompact(Canvas c){
        fit(c,n("learn"),24,31,21,262,TEXT,true,false);
        fit(c,n("lesson",tutorial.lessonIndex()+1,tutorial.lessonCount()),25,62,13,240,skin.accent,true,false);
        quietButton(c,n("later"),288,40,78,()->leaveTutorial());
        sweet.surface(c,new RectF(210,96,366,278),SweetUi.CREAM,true);
        fit(c,tt("title_"+tutorial.lessonKey()),221,121,15,134,TEXT,true,false);
        wrap(c,tt(tutorial.instructionKey()),221,146,133,13,TEXT,false,18);
        label(210,96,156,182,tt("title_"+tutorial.lessonKey())+". "+tt(tutorial.instructionKey()));
        drawTutorialBoard(c,30,103,146);
        boolean done=tutorial.lessonComplete();TutorialSession.RequiredAction required=tutorial.requiredAction();
        if(done)button(c,n("continue_lesson")+" →",217,230,142,48,PEACH,()->practiceAction(()->tutorial.advance()));
        else if(required==TutorialSession.RequiredAction.HINT)button(c,t("booster_hint"),217,230,142,48,GOLD,()->practiceAction(()->tutorial.hint()));
        else if(required==TutorialSession.RequiredAction.UNDO)button(c,t("undo"),217,230,142,48,PEACH,()->practiceAction(()->tutorial.undo()));
        else if(required==TutorialSession.RequiredAction.PREVIEW_GATE)button(c,n("preview"),217,230,142,48,PEACH,()->practiceGate(tutorial.expectedGate()));
        else if(tutorial.currentPreview()!=null)button(c,required==TutorialSession.RequiredAction.OPEN_GATE?n("open_short"):n("confirm_short"),217,230,142,48,PEACH,()->{if(tutorial.expectedGate()!=null)practiceGate(tutorial.expectedGate());else practicePiece(tutorial.expectedTileId(),null);});
        for(int i=0;i<tutorial.lessonCount();i++)sweet.stepChip(c,90+i*35,310,10,i==tutorial.lessonIndex(),i<tutorial.lessonIndex());
        quietButton(c,n("learn_skip"),24,345,342,()->leaveTutorial());
        fit(c,n("learn_practice"),195,417,12,338,skin.muted,false,true);
    }
    private void drawTutorialBoard(Canvas c,float left,float top,float width){
        tutorialBoardLeft=left;tutorialBoardTop=top;tutorialBoardWidth=width;
        Board practice=tutorial.board();float unit=width/practice.level.size;
        sweet.surface(c,new RectF(left-20,top-20,left+width+20,top+width+20),SweetUi.LILAC,true);
        for(int row=0;row<practice.level.size;row++)for(int col=0;col<practice.level.size;col++){
            round(c,left+col*unit+3,top+row*unit+3,unit-6,unit-6,15,0x50ffffff);
        }
        Board.ActionPreview preview=tutorial.currentPreview();
        for(Direction direction:Direction.values()){
            boolean open=practice.isGateOpen(direction);float x=left+width/2+direction.dc*(width/2+15),y=top+width/2+direction.dr*(width/2+15);
            boolean chosen=direction==tutorial.expectedGate()||preview!=null&&((preview.openedMask|preview.closedMask)&direction.bit)!=0;
            sweet.gate(c,x,y,16,direction,open,chosen,frameTime,reduceMotion);
            String gateLabel=t(open?"gate_open":"gate_closed")+" · "+t("direction_"+direction.name().toLowerCase(Locale.ROOT));
            gateHit(x,y,direction,gateLabel,()->practiceGate(direction));
        }
        for(Barrier barrier:practice.level.barriers)if(practice.isBarrierActive(barrier)){
            float x=left+(barrier.col+.5f)*unit,y=top+(barrier.row+.5f)*unit;
            sweet.surface(c,new RectF(x-unit*.39f,y-unit*.39f,x+unit*.39f,y+unit*.39f),SweetUi.CREAM,true);icon(c,"lock",x,y,29,TEXT);
            label(x-unit/2,y-unit/2,unit,unit,u("sealed_wall",1));
        }
        for(Tile tile:practice.remainingTiles()){
            float x=left+(tile.col+.5f)*unit,y=top+(tile.row+.5f)*unit;boolean lit=tile.id==tutorial.expectedTileId()||tile.id==tutorial.highlightedHintId();
            sweet.tile(c,new RectF(x-unit*.38f,y-unit*.38f,x+unit*.38f,y+unit*.38f),tile.direction,directionColor(tile.direction),tile.isSwitch(),false,lit,frameTime,reduceMotion);
            if(practice.level.isKey(tile.id))sweet.key(c,x-unit*.26f,y-unit*.25f,10,tile.direction,frameTime,reduceMotion);
            final int id=tile.id;String description=t("piece_description",tile.row+1,tile.col+1,t("direction_"+tile.direction.name().toLowerCase(Locale.ROOT)));
            hit(left+tile.col*unit,top+tile.row*unit,unit,unit,description,()->practicePiece(id,null));
        }
        Iterator<Flight> flight=practiceFlights.iterator();while(flight.hasNext()){
            Flight f=flight.next();float amount=(frameTime-f.at)/(reduceMotion?100f:360f);if(amount>=1){flight.remove();continue;}float dist=reduceMotion?0:amount*amount*280;
            c.saveLayerAlpha(0,0,390,H,Math.round(255*(1-amount)));sweet.tile(c,new RectF(f.x-unit*.38f+f.tile.direction.dc*dist,f.y-unit*.38f+f.tile.direction.dr*dist,f.x+unit*.38f+f.tile.direction.dc*dist,f.y+unit*.38f+f.tile.direction.dr*dist),f.tile.direction,directionColor(f.tile.direction),f.tile.isSwitch(),false,false,frameTime,reduceMotion);c.restore();
        }
        if(!tutorial.lessonComplete()){
            int target=tutorial.expectedTileId();Tile tile=practice.level.tile(target);
            if(tile!=null&&practice.isPresent(target)){
                float x=left+(tile.col+.5f)*unit,y=top+(tile.row+.5f)*unit;
                sweet.handCoach(c,x,y,x+tile.direction.dc*55,y+tile.direction.dr*55,frameTime-tutorialChangedAt,reduceMotion);
            }else if(tutorial.expectedGate()!=null){Direction direction=tutorial.expectedGate();float x=left+width/2+direction.dc*(width/2+15),y=top+width/2+direction.dr*(width/2+15);sweet.handCoach(c,x,y,x,y,frameTime-tutorialChangedAt,reduceMotion);}
        }
    }
    private String screenTitle(String key){return key.equals("story")?a("story_book"):key.equals("wardrobe")?a("avatar"):key.equals("styles")?q("styles"):key.equals("guardians")||key.equals("guardian")?q("guardians"):key.equals("briefing")?e("objective"):key.equals("tutorial")?n("learn"):t(key);}
    private void drawStyles(Canvas c){
        header(c,q("styles"),q("style_count"));
        int i=0;for(AvatarStyles.Style style:AvatarStyles.all()){
            float x=24+(i%3)*117,y=128+(i/3)*229;boolean active=style.id.equals(avatarStyle);
            sweet.surface(c,new RectF(x,y,x+108,y+210),active?SweetUi.MINT:SweetUi.CREAM,true);
            companionPortrait.draw(c,character,x+54,y+71,26,style,AvatarStage.Pose.HAPPY,frameTime,reduceMotion,loadout,wornCosmetics);
            fit(c,style.name(store.resolvedLanguage()),x+54,y+142,14,98,TEXT,true,true);
            fit(c,active?q("style_active"):q("style_choose"),x+54,y+177,12,98,TEXT,false,true);
            hit(x,y,108,210,style.name(store.resolvedLanguage())+", "+(active?q("style_active"):q("style_choose")),()->{try{store.equipAvatarStyle(style.id);syncStyle();sfx(AudioEngine.Sound.SELECT);}catch(IllegalStateException failure){notice(t("save_error"));}});i++;
        }
    }
    private void drawPrismStory(Canvas c){
        ensureHub();PrismStory.ChapterProgress progress=storyChapter==0?hubStory.currentChapter():hubStory.forChapter(storyChapter);PrismStory.Chapter chapter=progress.chapter;
        header(c,a("story_book"),a("chapter",chapter.number)+" · "+v(chapter.biomeKey));
        sweet.surface(c,new RectF(24,103,366,330),SweetUi.LILAC,true);dressedMascot(c,326,139,14);
        fit(c,storyText(chapter.titleKey),44,141,21,240,TEXT,true,false);
        wrap(c,storyText(progress.completed?chapter.storyAfterKey:chapter.storyIntroKey),44,183,300,15,TEXT,false,23);
        fit(c,a("story_progress",hubStory.fragmentsClaimed,100),44,309,12,300,skin.muted,true,false);label(24,103,342,227,storyText(chapter.titleKey)+". "+storyText(progress.completed?chapter.storyAfterKey:chapter.storyIntroKey)+". "+a("story_progress",hubStory.fragmentsClaimed,100));
        for(int i=1;i<=10;i++){
            final int n=i;PrismStory.ChapterProgress row=hubStory.forChapter(i);float x=24+((i-1)%5)*69,y=350+((i-1)/5)*55;
            round(c,x,y,64,48,14,chapter.number==i?SweetUi.light(SweetUi.MINT):row.unlocked?skin.card:0xffe5dfec);fit(c,""+i+(row.completed?" ✓":row.unlocked?"":" ·"),x+32,y+30,15,56,TEXT,true,true);
            int before=hits.size();hit(x,y,64,48,a("chapter",i)+", "+(row.completed?a("all_claimed"):row.unlocked?a("ready"):t("locked")),()->{storyChapter=n;invalidate();});if(hits.size()>before)hits.get(hits.size()-1).selected=chapter.number==i;
        }
        sweet.surface(c,new RectF(24,480,366,601),SweetUi.CREAM,true);fit(c,a("objectives"),44,510,15,300,TEXT,true,false);wrap(c,storyText(chapter.objectiveKey),44,539,300,14,skin.muted,false,21);
        label(24,480,342,121,a("objectives")+". "+storyText(chapter.objectiveKey));
        label(24,615,342,72,a("journey_short")+" "+chapter.firstJourney+"–"+chapter.lastJourney+", "+progress.journeyProgress+" / "+progress.journeyTarget);
        label(24,700,342,77,q("guardians")+", "+a("story_progress",progress.fragmentsClaimed,progress.fragmentTarget));
        fit(c,a("journey_short")+"  "+chapter.firstJourney+"–"+chapter.lastJourney,24,633,16,342,TEXT,true,false);fit(c,progress.journeyProgress+" / "+progress.journeyTarget,24,661,13,342,skin.muted,false,false);
        sweet.progress(c,new RectF(24,674,366,683),progress.journeyProgress/(float)progress.journeyTarget,SweetUi.MINT);
        fit(c,q("guardians"),24,722,16,342,TEXT,true,false);fit(c,a("story_progress",progress.fragmentsClaimed,progress.fragmentTarget),24,750,13,342,skin.muted,false,false);
        sweet.progress(c,new RectF(24,764,366,773),progress.fragmentsClaimed/(float)progress.fragmentTarget,SweetUi.LILAC);
        int next=progress.nextGuardian();boolean available=next>0&&hubStory.guardianUnlocked(next);
        String action=next==0?a("back_journey"):available?q("guardian",next)+"  →":a("play_journey");
        button(c,action,24,798,342,50,PEACH,()->{if(available)openGuardian(next);else continueJourney();});
        fit(c,next==0?a("all_claimed"):available?q("guardian_reward"):q("guardian_locked",next*10),195,879,13,336,skin.muted,false,true);
        quietButton(c,q("guardians"),24,912,342,()->go("guardians"));
    }
    private void drawGuardians(Canvas c){
        header(c,q("guardians"),q("guardian_unlock"));
        wrap(c,q("guardian_story"),24,110,342,14,TEXT,false,20);
        for(int number=1;number<=GuardianCatalog.COUNT;number++){
            final int n=number;float x=24+((n-1)%2)*177,y=200+((n-1)/2)*84;
            if(y<contentScroll-95||y>contentScroll+viewportH+90)continue;
            boolean unlocked=store.guardianUnlocked(n),done=store.guardianCleared(n);
            sweet.surface(c,new RectF(x,y,x+165,y+73),done?SweetUi.MINT:SweetUi.CREAM,true);
            fit(c,q("guardian",n)+(done?"  ✓":""),x+12,y+27,17,144,TEXT,true,false);
            fit(c,done?q("guardian_done"):unlocked?t("play"):q("guardian_locked",n*10),x+12,y+53,12,144,skin.muted,false,false);
            hit(x,y,165,73,q("guardian",n)+", "+(unlocked?t("play"):t("locked")),()->{if(unlocked)openGuardian(n);else notice(q("guardian_locked",n*10));});
        }
    }
    private void openGuardian(int number){
        if(!store.guardianUnlocked(number)){notice(q("guardian_locked",number*10));return;}
        GuardianEncounter saved=GuardianEncounter.restore(prefs.getString("guardian_v19_session",""));
        guardian=saved!=null&&saved.definition.number==number&&(!saved.isSolved()||!store.guardianCleared(number))?saved:GuardianEncounter.start(number);
        guardianHint=-1;guardianSaveBlocked=false;go("guardian");persistGuardian();
    }
    private boolean persistGuardian(){
        if(guardian==null)return true;
        boolean saved=prefs.edit().putString("guardian_v19_session",guardian.encode()).commit();
        guardianSaveBlocked=!saved;if(!saved)notice(t("save_error"));return saved;
    }
    private void guardianMove(int id){
        if(guardian==null||guardianSaveBlocked)return;
        GuardianEncounter.MoveResult result=guardian.move(id);guardianHint=-1;
        if(!persistGuardian())return;
        if(result.success){sfx(result.peeled?AudioEngine.Sound.GATE:AudioEngine.Sound.MOVE);haptic();if(result.solved)sfx(AudioEngine.Sound.WIN);}
        else if(result.blocked){sfx(AudioEngine.Sound.BLOCKED);notice(t("blocked"));}
        invalidate();sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
    }
    private void drawGuardian(Canvas c){
        if(guardian==null){go("guardians");return;}
        GuardianEncounter.Snapshot snap=guardian.snapshot();
        round(c,24,18,48,48,16,skin.card);icon(c,"back",48,42,21,TEXT);hit(24,18,48,48,t("back"),()->{if(persistGuardian())go("guardians");});
        fit(c,q("guardian",snap.number),210,51,25,244,TEXT,true,true);
        AvatarStyles.Style scene=AvatarStyles.all().get((snap.number-1)%AvatarStyles.all().size());
        fit(c,scene.name(store.resolvedLanguage()),25,105,18,242,TEXT,true,false);
        companionPortrait.draw(c,character,326,115,21,AvatarStyles.find(avatarStyle),snap.solved?AvatarStage.Pose.WIN:AvatarStage.Pose.THINK,frameTime,reduceMotion,loadout,wornCosmetics);
        fit(c,q("guardian_work",snap.totalWork-snap.remainingWork,snap.totalWork),25,143,14,300,TEXT,true,false);
        sweet.progress(c,new RectF(25,157,365,168),1f-snap.remainingWork/(float)snap.totalWork,SweetUi.MINT);
        wrap(c,guardian.definition.cellwise?q("guardian_step"):q("guardian_double"),25,198,340,14,TEXT,false,21);
        if(guardianSaveBlocked){sweet.surface(c,new RectF(24,268,366,570),SweetUi.CREAM,true);wrap(c,t("save_error"),195,337,296,18,TEXT,true,25);button(c,t("retry"),44,422,302,52,PEACH,()->persistGuardian());return;}
        float unit=270f/snap.size;for(int r=0;r<snap.size;r++)for(int col=0;col<snap.size;col++)round(c,60+col*unit+2,270+r*unit+2,unit-4,unit-4,12,0x70ffffff);
        for(GuardianEncounter.Piece piece:snap.pieces)if(piece.present){
            float x=60+(piece.col+.5f)*unit,y=270+(piece.row+.5f)*unit,w=unit*.76f;
            // Separate visible slabs and an explicit count communicate a real additional hit.
            for(int layer=piece.layers-1;layer>0;layer--)sweet.surface(c,new RectF(x-w/2+layer*3,y-w/2+layer*7,x+w/2+layer*3,y+w/2+layer*7),0xffb29ac8,true);
            sweet.tile(c,new RectF(x-w/2,y-w/2,x+w/2,y+w/2),piece.direction,directionColor(piece.direction),false,false,piece.id==guardianHint,frameTime,reduceMotion);
            if(piece.layers>1){round(c,x+w*.04f,y-w*.46f,w*.44f,22,7,GOLD);fit(c,"×"+piece.layers,x+w*.26f,y-w*.46f+16,12,w*.40f,TEXT,true,true);}
            if(piece.cellwise){p.setColor(TEXT);c.drawCircle(x-7,y+w*.31f,2.7f,p);c.drawCircle(x+2,y+w*.24f,2.7f,p);}
            guardianHit(60+piece.col*unit,270+piece.row*unit,unit,piece.id,q("guardian_piece",piece.id+1,t("direction_"+piece.direction.name().toLowerCase(Locale.ROOT)),piece.layers,piece.cellwise?q("guardian_cell"):""),()->guardianMove(piece.id));
        }
        fit(c,t("moves_left",snap.remainingActions,guardian.definition.baseBudget),195,582,20,340,TEXT,true,true);
        if(snap.solved){
            wrap(c,q("guardian_empty"),195,615,330,15,TEXT,true,21);
            button(c,store.guardianCleared(snap.number)?q("guardian_done"):q("guardian_reward"),24,663,342,57,GOLD,()->{try{int got=store.completeGuardian(snap.number);syncStyle();notice(got>0?"+100 LumaCoins":store.guardianCleared(snap.number)?q("guardian_done"):q("stock_full"));}catch(IllegalStateException failure){notice(t("save_error"));}});
        }else{
            if(snap.remainingActions==0)wrap(c,q("guardian_retry"),195,613,330,14,TEXT,true,21);
            else button(c,q("guardian_hint"),24,609,342,49,PEACH,()->{int[] plan=guardian.optimalPlan();guardianHint=plan.length==0?-1:plan[0];invalidate();});
            quietButton(c,t("undo_free"),24,690,165,()->{if(guardian.undo()){guardianHint=-1;persistGuardian();sfx(AudioEngine.Sound.UNDO);}});
            quietButton(c,t("restart_free"),201,690,165,()->{guardian.restart();guardianHint=-1;persistGuardian();});
        }
        quietButton(c,q("guardians"),24,767,342,()->{if(persistGuardian())go("guardians");});
        fit(c,q("guardian_unlock"),195,850,12,335,skin.muted,false,true);
    }

    private void ensureHub(){
        long now=SystemClock.uptimeMillis();if(hubObjectives!=null&&now<hubRefreshAt)return;
        hubObjectives=ObjectiveDashboard.snapshot(store,meta);hubGoal=CollectionGoal.snapshot(store);
        HashSet<Integer> receipts=new HashSet<>();for(int i=1;i<=100;i++)if(store.guardianCleared(i))receipts.add(i);
        hubStory=PrismStory.snapshot(cachedCompleted,receipts);hubRefreshAt=now+1000;
    }
    private void savedAction(Runnable action){try{action.run();syncStyle();invalidate();}catch(GameStore.LocalSaveException failure){blockLocalSave();}}
    private void openRewards(int tab){rewardTab=Math.max(0,Math.min(3,tab));go("rewards");}
    private void continueJourney(){if(board!=null&&!won&&!board.isSolved()&&mode==0)goRoot("play");else previewLevel(Math.min(BalancedCatalog.LEVEL_COUNT,store.unlockedLevel()),0,0);}
    private String goalName(CollectionGoal goal){return goal.kind==CollectionGoal.Kind.COSMETIC?w(goal.nameKey):g(goal.nameKey);}
    private void goalGlyph(Canvas c,CollectionGoal goal,float x,float y,float radius){
        if(goal.kind==CollectionGoal.Kind.COSMETIC){CosmeticCatalog.Item item=CosmeticCatalog.find(goal.id);art.cosmetic(c,item.slot.name(),CosmeticCatalog.ITEMS.indexOf(item)%6,x,y,radius,cosmeticColor(item));}
        else companionPortrait.drawGearGlyph(c,AvatarGearCatalog.find(goal.id),x,y,radius);
    }
    private void sourceRoute(CollectionGoal.Route route){
        switch(route){case JOURNEY:continueJourney();break;case DAILY:eventTab=0;go("events");break;case WEEKLY:eventTab=1;go("events");break;case SHOP:shopCategory=1;go("shop");break;default:openRewards(0);}
    }
    private void openGoal(CollectionGoal goal){
        if(goal.owned||goal.priceCoins>0){if(goal.kind==CollectionGoal.Kind.COSMETIC)cosmeticOffer(CosmeticCatalog.find(goal.id));else previewGear(AvatarGearCatalog.find(goal.id));}
        else sourceRoute(goal.route);
    }
    private void toggleGoal(String key){savedAction(()->{if(key.equals(store.trackedCollectionGoal()))store.clearTrackedCollectionGoal();else store.setTrackedCollectionGoal(key);notice(a(key.equals(store.trackedCollectionGoal())?"tracked_goal":"empty_goal"));});}
    private void claimLogin(String period){savedAction(()->{if(!period.equals(store.dailyId())){notice(a("period_changed"));return;}int got=store.claimDailyLogin();notice(got>0?"+"+got+" LumaCoins":t("claimed"));if(got>0)sfx(AudioEngine.Sound.COIN);});}
    private void claimQuest(ObjectiveDashboard.Quest quest){savedAction(()->{
        if(!quest.period.equals(quest.weekly?meta.weekId():meta.dayId())){notice(a("period_changed"));return;}
        int got=quest.weekly?meta.claimWeeklyMission(quest.index,store):meta.claimMission(quest.index,store);
        notice(got>0?"+"+got+" LumaCoins":quest.claimed?t("claimed"):t("mission_note"));if(got>0)sfx(AudioEngine.Sound.COIN);
    });}
    private void claimEventBonus(boolean daily,String period){savedAction(()->{
        if(!period.equals(daily?store.dailyId():store.weeklyId())){notice(a("period_changed"));return;}
        int got=daily?store.claimDailyCompletionReward():store.claimWeeklyCompletionReward();notice(got>0?"+"+got+" LumaCoins":t("claimed"));if(got>0)sfx(AudioEngine.Sound.COIN);
    });}
    private void collectJourneyGift(int milestone){savedAction(()->{if(store.nextLootMilestone()!=milestone){notice(t("claimed"));return;}claimJourneyGift();});}
    private void launchObjective(ObjectiveDashboard.Action action){
        switch(action.kind){case PLAY_DAILY_EVENT:eventTab=0;openEventStage(true,Math.max(0,action.index));break;case PLAY_WEEKLY_EVENT:eventTab=1;openEventStage(false,Math.max(0,action.index));break;case PLAY_JOURNEY:previewLevel(Math.max(1,action.index),0,0);break;default:openRewards(3);}
    }
    private String questTitle(ObjectiveDashboard.Quest quest){return V17Strings.format(store.resolvedLanguage(),new String[]{"quest_boards","quest_stars","quest_events"}[quest.index],quest.target);}
    private void questRoute(ObjectiveDashboard.Quest quest){if(quest.ready)claimQuest(quest);else if(quest.claimed)notice(t("claimed"));else sourceRoute(quest.route);}
    private void drawGoalCard(Canvas c,float y,CollectionGoal goal){
        sweet.surface(c,new RectF(24,y,366,y+170),goal.owned?SweetUi.MINT:SweetUi.CREAM,true);
        fit(c,a("tracked_goal"),44,y+26,12,252,skin.muted,true,false);goalGlyph(c,goal,330,y+37,20);
        fit(c,goalName(goal),44,y+54,20,250,TEXT,true,false);
        String progress=goal.owned?a("goal_owned"):(goal.priceCoins>0?goal.progress+" / "+goal.target+" LumaCoins":a("goal_progress",goal.progress,goal.target));
        fit(c,progress,44,y+83,13,300,goal.owned?skin.accent2:skin.muted,true,false);
        sweet.progress(c,new RectF(44,y+94,346,y+101),goal.owned?1f:goal.target==0?0:Math.min(1,goal.progress/(float)goal.target),SweetUi.MINT);
        label(24,y,342,106,a("tracked_goal")+", "+goalName(goal)+", "+progress);
        button(c,a(goal.owned||goal.priceCoins>0?"preview":"view_source"),44,y+112,302,48,PEACH,()->openGoal(goal));
    }
    private float rewardContentHeight(){ensureHub();return rewardTab==3?Math.max(600,320+hubObjectives.readyCount*153):rewardTab==2?1510:rewardTab==0?1070:880;}
    private String navParent(){
        if(Arrays.asList("collection","wardrobe","styles").contains(screen))return "collection";
        if(Arrays.asList("events","rewards","missions").contains(screen))return "events";
        if(Arrays.asList("journey","guardians","story").contains(screen))return "journey";
        return screen;
    }


    private void drawHome(Canvas c){
        ensureHub();ObjectiveDashboard.Snapshot dash=hubObjectives;
        text(c,"LUMA DRIFT",24,44,27,TEXT,true);wallet(c,229,17);
        sweet.surface(c,new RectF(325,15,366,60),SweetUi.LILAC,false);icon(c,"settings",345,37,21,TEXT);hit(320,12,50,50,t("settings"),()->go("settings"));
        sweet.surface(c,new RectF(24,77,366,148),SweetUi.CREAM,true);
        String name=profileName.trim();if(name.contains(" "))name=name.substring(0,name.indexOf(' '));
        boolean guest=profileSnapshot==null||profileSnapshot.state==SessionSnapshot.State.GUEST;
        String title=profileSignedIn?u("welcome",name):guest?u("guest"):t(profileSnapshot.statusKey);
        round(c,36,89,45,45,15,profileSignedIn?0xffd2f3e7:0xfff6d8e1);icon(c,profileSignedIn?"check":"profile",58,111,24,TEXT);
        fit(c,title,94,107,18,191,TEXT,true,false);fit(c,profileSignedIn?u("signed"):guest?u("guest_note"):t("account"),94,132,12,190,skin.muted,false,false);
        hit(24,77,276,71,t("account")+": "+title+", "+(profileSignedIn?u("signed"):u("guest_note")),()->openAccount());
        star(c,334,105,14,GOLD);fit(c,""+dash.readyCount,334,133,13,48,TEXT,true,true);hit(309,83,48,59,a("ready_count",dash.readyCount),()->openRewards(3));
        boolean active=board!=null&&!won&&!board.isSolved();int display=active&&mode==0?levelIndex:Math.min(BalancedCatalog.LEVEL_COUNT,store.unlockedLevel());
        sweet.surface(c,new RectF(24,166,366,354),SweetUi.LILAC,true);
        fit(c,a("chapter",(display-1)/100+1)+" · "+worldName(display),43,193,12,190,skin.muted,true,false);
        fit(c,active&&mode!=0?t(mode==1?"daily":mode==2?"weekly":mode==3?"remix":"friend_challenge"):t("level_number",display),43,232,27,181,TEXT,true,false);
        dressedMascot(c,296,220,32);hit(232,166,128,112,a("show_avatar"),()->go("wardrobe"));
        if(!active||mode==0&&contentVersion>=5)difficultyPill(c,StrategyCatalog.info(display,false),43,249,181);
        button(c,t(active?"resume":"play")+"  →",43,291,304,49,PEACH,()->continueGame());
        if(hubGoal!=null)drawGoalCard(c,373,hubGoal);else{
            sweet.surface(c,new RectF(24,373,366,543),SweetUi.CREAM,true);art.power(c,dash.readyCount>0?"hint":"trail",326,414,22,frameTime,reduceMotion);
            fit(c,a("objectives"),44,402,13,240,skin.muted,true,false);fit(c,dash.readyCount>0?a("ready_count",dash.readyCount):a("next_action"),44,432,22,242,TEXT,true,false);
            fit(c,dash.readyCount>0?a("claim_reward"):a("play_journey"),44,460,13,290,skin.muted,false,false);
            button(c,a(dash.readyCount>0?"rewards_all":"play_journey")+"  →",44,482,302,48,PEACH,()->{if(dash.readyCount>0)openRewards(3);else launchObjective(dash.nextAction);});
        }
        lobbyTile(c,24,563,"events",a("today"),store.dailyCompletedCount()+" / 3",a("ready_count",dash.dailyReadyCount),()->{eventTab=0;go("events");});
        int guardian=hubStory.nextGuardian();boolean guardianReady=guardian>0&&hubStory.guardianUnlocked(guardian);
        lobbyTile(c,202,563,"journey",a("story_book"),a("chapter",hubStory.currentChapter().chapter.number),guardianReady?q("guardian",guardian):guardian>0?t("level_number",guardian*10):a("all_claimed"),()->go("story"));
        sweet.surface(c,new RectF(24,677,366,815),SweetUi.MINT,true);dressedMascot(c,81,733,24);
        fit(c,a("avatar"),139,710,19,208,TEXT,true,false);fit(c,a("owned_count",store.ownedCosmeticCount(),24),139,738,12,206,skin.muted,false,false);
        quietButton(c,a("appearance"),139,755,208,()->go("wardrobe"));hit(24,677,108,138,a("show_avatar"),()->go("wardrobe"));
        quietButton(c,tutorial.complete()&&!tutorialGiftClaimed?n("tutorial_claim"):tutorial.complete()?n("learn_replay"):n("learn_continue"),24,835,342,()->openTutorial(false));
        quietButton(c,v("friends"),24,902,165,()->openSocial());quietButton(c,a("visual_styles"),201,902,165,()->go("styles"));
        quietButton(c,t("import_challenge"),24,969,342,()->importCode());
    }

    private void lobbyTile(Canvas c,float x,float y,String ico,String title,String subtitle,String detail,Runnable action){
        round(c,x,y,164,93,21,skin.card);round(c,x+12,y+12,31,31,11,alpha(skin.accent,38));icon(c,ico,x+27,y+27,17,skin.accent2);
        fit(c,title,x+52,y+30,15,100,TEXT,true,false);fit(c,subtitle,x+13,y+58,12,139,skin.muted,false,false);fit(c,detail,x+13,y+79,13,139,GOLD,true,false);
        hit(x,y,164,93,title+", "+subtitle+", "+detail,action);
    }
    private void tinyTile(Canvas c,float x,float y,Direction d,int color){c.save();c.translate(x,y);c.rotate(reduceMotion?0:(float)Math.sin(frameTime*.001)*8);round(c,-19,-19,38,38,12,color);arrow(c,0,0,d,10,INK);c.restore();}

    private void nav(Canvas c){
        float y=H-87;round(c,10,y,370,76,24,0xfffcf7ff);stroke(c,new RectF(10,y,380,y+76),24,0xffd5bfdf,1);
        String parent=navParent();String[] ids={"home","journey","events","collection","shop"};String[] captions={t("home"),e("nav_map"),e("nav_events"),e("nav_inventory"),t("shop")};
        for(int i=0;i<ids.length;i++){String target=ids[i];float x=47+i*74;boolean on=parent.equals(target);if(on)round(c,x-28,y+7,56,34,13,alpha(skin.accent2,30));icon(c,target,x,y+25,21,on?skin.accent2:skin.muted);fit(c,captions[i],x,y+62,12,70,on?TEXT:skin.muted,on,true);int before=hits.size();hit(x-35,y+3,70,70,captions[i],()->goRoot(target));if(hits.size()>before)hits.get(hits.size()-1).selected=on;}
    }

    private int difficultyColor(ProgressionInfo info){return info.difficulty==ProgressionInfo.Difficulty.EXPERT?0xffe7b2ff:info.difficulty==ProgressionInfo.Difficulty.HARD?PEACH:skin.accent2;}
    private void difficultyPill(Canvas c,ProgressionInfo info,float x,float y,float width){int color=difficultyColor(info);round(c,x,y,width,29,12,alpha(color,24));fit(c,e(info.difficultyKey)+" · "+e(info.beatKey),x+width/2,y+20,12,width-14,color,true,true);}
    private int cosmeticColor(CosmeticCatalog.Item item){String id=item.id;if(id.contains("pumpkin")||id.contains("witch"))return 0xffffb473;if(id.contains("cupid")||id.contains("valentine"))return 0xffffa9cf;if(id.contains("moon"))return 0xffb7b0ff;if(id.contains("star"))return GOLD;if(id.contains("aurora"))return 0xffa8dfff;return 0xff91e5cc;}

    private void dressedMascot(Canvas c,float x,float y,float r){
        companionPortrait.draw(c,character,x,y,r,AvatarStyles.find(avatarStyle),screen.equals("play")?frameTime<gatePulseUntil?AvatarStage.Pose.GATE:frameTime<successUntil?AvatarStage.Pose.HAPPY:AvatarStage.Pose.THINK:AvatarStage.Pose.IDLE,frameTime,reduceMotion,loadout,wornCosmetics);
    }

    private String cosmeticSource(CosmeticCatalog.Item item){String key=item.source==CosmeticCatalog.Source.LOGIN_DAYS?"source_login":item.source==CosmeticCatalog.Source.WEEKLY_WINS?"source_weekly":item.source==CosmeticCatalog.Source.EVENT_STARS?"source_event":item.source==CosmeticCatalog.Source.EARNED_COINS?"source_coins":"source_starter";return V17Strings.format(store.resolvedLanguage(),key,item.requirement);}
    private void drawWardrobe(Canvas c){
        header(c,a("avatar"),a("owned_count",store.ownedCosmeticCount(),24));
        sweet.surface(c,new RectF(24,103,366,422),SweetUi.LILAC,true);
        dressedMascot(c,195,213,43);
        for(int i=0;i<4;i++){
            final int slot=i;CosmeticCatalog.Slot type=CosmeticCatalog.Slot.values()[i];CosmeticCatalog.Item item=CosmeticCatalog.find(store.equippedCosmetic(type));float x=i%2==0?36:286,y=i<2?130:230;
            round(c,x,y,68,72,15,wardrobeMode==0&&wardrobeSlot==i?SweetUi.light(SweetUi.MINT):skin.card);art.cosmetic(c,type.name(),CosmeticCatalog.ITEMS.indexOf(item)%6,x+34,y+25,17,cosmeticColor(item));fit(c,v(new String[]{"hat","dress","shoes","gloves"}[i]),x+34,y+57,12,62,TEXT,true,true);
            int before=hits.size();hit(x,y,68,72,v(new String[]{"hat","dress","shoes","gloves"}[i])+", "+w(item.nameKey)+", "+e("equipped"),()->{wardrobeMode=0;wardrobeSlot=slot;wardrobePage=0;});if(hits.size()>before)hits.get(hits.size()-1).selected=wardrobeMode==0&&wardrobeSlot==i;
        }
        fit(c,t(character.nameKey),195,331,20,300,TEXT,true,true);fit(c,AvatarStyles.find(avatarStyle).name(store.resolvedLanguage()),195,353,12,300,skin.muted,false,true);
        quietButton(c,a("visual_styles"),111,366,168,()->go("styles"));
        fit(c,a("gear_slots"),24,453,15,342,TEXT,true,false);
        for(int i=0;i<3;i++){
            final int slot=i;AvatarGearCatalog.Item worn=loadout.item(AvatarGearCatalog.Slot.values()[i]);float x=24+i*120;
            sweet.surface(c,new RectF(x,469,x+102,554),wardrobeMode==1&&wardrobeGearSlot==i?SweetUi.MINT:SweetUi.CREAM,true);gearGlyph(c,worn,x+51,496,19);fit(c,g(worn.slot.key()),x+51,538,12,94,TEXT,true,true);
            hit(x,469,102,85,g(worn.nameKey)+", "+e("equipped"),()->{wardrobeMode=1;wardrobeGearSlot=slot;wardrobePage=0;});
        }
        String[] tabs={a("appearance"),a("equipment")};for(int i=0;i<2;i++){final int mode=i;float x=24+i*174;round(c,x,574,168,50,16,wardrobeMode==i?SweetUi.light(SweetUi.MINT):skin.card);fit(c,tabs[i],x+84,605,15,156,TEXT,true,true);int before=hits.size();hit(x,574,168,50,tabs[i],()->{wardrobeMode=mode;wardrobePage=0;});if(hits.size()>before)hits.get(hits.size()-1).selected=wardrobeMode==i;}
        if(wardrobeMode==0){
            ArrayList<CosmeticCatalog.Item> items=new ArrayList<>();for(CosmeticCatalog.Item item:CosmeticCatalog.ITEMS)if(item.slot==CosmeticCatalog.Slot.values()[wardrobeSlot])items.add(item);
            for(int i=0;i<3;i++)drawWardrobeItem(c,items.get(wardrobePage*3+i),644+i*128);
        }else{
            ArrayList<AvatarGearCatalog.Item> items=new ArrayList<>();for(AvatarGearCatalog.Item item:AvatarGearCatalog.ITEMS)if(item.slot==AvatarGearCatalog.Slot.values()[wardrobeGearSlot])items.add(item);
            for(int i=0;i<3;i++){
                AvatarGearCatalog.Item item=items.get(wardrobePage*3+i);float y=644+i*128;boolean owned=store.ownsAvatarGear(item.id),on=item.id.equals(loadout.item(item.slot).id);round(c,24,y,342,116,22,on?SweetUi.light(SweetUi.MINT):skin.card);gearGlyph(c,item,67,y+54,25);
                String state=on?e("equipped"):owned?e("owned"):e("to_unlock");fit(c,g(item.nameKey),113,y+29,18,235,TEXT,true,false);fit(c,state,113,y+53,12,235,owned?skin.accent2:skin.muted,true,false);fit(c,GearStrings.effectCaption(store.resolvedLanguage(),item),113,y+78,12,235,skin.muted,false,false);fit(c,owned?g(item.slot.key()):item.priceCoins>0?item.priceCoins+" LumaCoins":shortGearSource(item),113,y+102,12,235,GOLD,true,false);hit(24,y,342,116,g(item.nameKey)+", "+state,()->previewGear(item));
            }
        }
        quietButton(c,"‹",24,1040,67,()->wardrobePage=0);fit(c,(wardrobePage+1)+" / 2",195,1069,14,170,TEXT,true,true);quietButton(c,"›",299,1040,67,()->wardrobePage=1);
        for(int k=0;k<Catalog.BOOSTERS.length;k++){Catalog.BoosterItem power=Catalog.BOOSTERS[k];float xx=47+k*59;art.power(c,power.id,xx,1122,14,frameTime,reduceMotion);fit(c,"×"+store.boosterStock(power.id),xx,1152,12,47,TEXT,true,true);hit(xx-24,1101,48,58,t(power.nameKey)+", "+e("power_stock",store.boosterStock(power.id)),()->powerOffer(power.id));}
        quietButton(c,a("open_inventory")+"  →",24,1176,342,()->go("collection"));
    }
    private void drawWardrobeItem(Canvas c,CosmeticCatalog.Item item,float y){
        boolean owned=store.ownsCosmetic(item.id),on=item.id.equals(store.equippedCosmetic(item.slot));round(c,24,y,342,116,22,on?SweetUi.light(SweetUi.MINT):skin.card);
        art.cosmetic(c,item.slot.name(),CosmeticCatalog.ITEMS.indexOf(item)%6,67,y+54,25,owned?cosmeticColor(item):0xff8c91ad);fit(c,w(item.nameKey),113,y+29,18,235,TEXT,true,false);
        String state=on?e("equipped"):owned?e("owned"):e("to_unlock");fit(c,state+" · "+w(item.rarity.name().toLowerCase(Locale.ROOT)),113,y+53,12,235,owned?skin.accent2:skin.muted,true,false);fit(c,owned?v("cosmetic_only"):cosmeticSource(item),113,y+78,12,235,skin.muted,false,false);fit(c,on?e("equipped"):owned?e("action_equip"):item.priceCoins>0?item.priceCoins+" LumaCoins":store.cosmeticProgress(item.id)+" / "+item.requirement,113,y+102,12,235,GOLD,true,false);hit(24,y,342,116,w(item.nameKey)+", "+state+", "+cosmeticSource(item),()->cosmeticOffer(item));
    }
    private View companionPreview(Catalog.CharacterItem who,AvatarLoadout gear,List<CosmeticCatalog.Item> clothes){
        final AvatarStyles.Style style=AvatarStyles.find(avatarStyle);final ArrayList<CosmeticCatalog.Item> snapshot=new ArrayList<>(clothes);final boolean still=reduceMotion;
        View preview=new View(getContext()){
            @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);canvas.drawColor(0xfffff4fc);float r=Math.min(getWidth(),getHeight())/4.3f;companionPortrait.draw(canvas,who,getWidth()/2f,getHeight()/2f,r,style,AvatarStage.Pose.HAPPY,SystemClock.uptimeMillis(),still,gear,snapshot);if(!still&&isAttachedToWindow())postInvalidateDelayed(50);}
            @Override protected void onMeasure(int width,int height){setMeasuredDimension(View.MeasureSpec.getSize(width),(int)(220*getResources().getDisplayMetrics().density));}
        };preview.setContentDescription(a("preview_only"));return preview;
    }
    private View cosmeticPreview(CosmeticCatalog.Item item){ArrayList<CosmeticCatalog.Item> clothes=new ArrayList<>(wornCosmetics);clothes.add(item);return companionPreview(character,loadout,clothes);}
    private String acquisitionCaption(String source,String fallback){
        if(source==null||source.isEmpty())return a("source_account");
        if(source.equals("EARNED_CAPSULE"))return a("acquired_with",a("source_capsule"));
        if(source.equals("EARNED_COINS"))return a("acquired_with","LumaCoins");
        return a("acquired_with",fallback);
    }
    private void cosmeticOffer(CosmeticCatalog.Item item){
        boolean owned=store.ownsCosmetic(item.id),equipped=item.id.equals(store.equippedCosmetic(item.slot));String key="cosmetic:"+item.id;
        String state=equipped?e("equipped"):owned?e("owned"):e("to_unlock");
        android.app.AlertDialog.Builder dialog=new android.app.AlertDialog.Builder(getContext()).setTitle(w(item.nameKey)).setView(cosmeticPreview(item)).setMessage(state+"\n"+v("cosmetic_only")+"\n"+(owned?acquisitionCaption(store.cosmeticAcquisitionSource(item.id),cosmeticSource(item)):cosmeticSource(item))+"\n"+a("preview_only")).setNegativeButton(t("close"),null).setNeutralButton(a(key.equals(store.trackedCollectionGoal())?"untrack_item":"track_item"),(d,w)->toggleGoal(key));
        if(owned&&!equipped)dialog.setPositiveButton(e("action_equip"),(d,w)->savedAction(()->{if(store.equipCosmetic(item.id))sfx(AudioEngine.Sound.SELECT);}));
        else if(!owned&&item.priceCoins>0)dialog.setPositiveButton(item.priceCoins+" LumaCoins",(d,w)->savedAction(()->{if(store.purchaseCosmetic(item.id)){sfx(AudioEngine.Sound.PURCHASE);notice(e("owned"));}else notice(t("not_enough"));}));
        else if(!owned)dialog.setPositiveButton(a("view_source"),(d,w)->{if(item.source==CosmeticCatalog.Source.LOGIN_DAYS)openRewards(0);else{eventTab=item.source==CosmeticCatalog.Source.WEEKLY_WINS?1:0;go("events");}});
        dialog.show();
    }
    private void openCapsule(){String receipt=UUID.randomUUID().toString();new android.app.AlertDialog.Builder(getContext()).setTitle(v("loot")).setMessage(w("capsule_note")+"\n\n"+w("capsule_odds")+"\n"+w("capsule_item_odds")+"\n\n"+w("capsule_duplicates")).setNegativeButton(t("cancel"),null).setPositiveButton(v("open_capsule"),(d,w)->performCapsule(receipt)).show();}
    private void performCapsule(String receipt){try{GameStore.LootResult got=store.drawCosmeticLoot(receipt);if(got==null){notice(w("not_enough_coins"));return;}syncStyle();CosmeticCatalog.Item item=CosmeticCatalog.find(got.itemId);new android.app.AlertDialog.Builder(getContext()).setTitle(w(item.nameKey)).setView(cosmeticPreview(item)).setMessage(got.duplicate?v("duplicate",got.compensationCoins):v("cosmetic_only")).setPositiveButton(e("action_equip"),(dialog,which)->{try{if(store.equipCosmetic(item.id))syncStyle();else notice(t("save_error"));}catch(IllegalStateException failure){notice(t("save_error"));}}).setNegativeButton(t("close"),null).show();sfx(AudioEngine.Sound.PURCHASE);}catch(IllegalStateException failure){new android.app.AlertDialog.Builder(getContext()).setTitle(t("save_error")).setMessage(t("commerce_save_error")).setPositiveButton(t("retry"),(d,w)->performCapsule(receipt)).setNegativeButton(t("close"),null).show();}}
    private void claimJourneyGift(){
        try{String got=store.claimMilestoneLoot();syncStyle();if(got.isEmpty())notice(q("stock_full"));else{sfx(AudioEngine.Sound.COIN);notice(t("loot_received",t("booster_"+got)));}}catch(IllegalStateException failed){notice(t("save_error"));}
    }
    private void claimTrial(String id){
        try{GameStore.PowerTrialStatus result=store.claimPowerTrial(id);syncStyle();notice(result==GameStore.PowerTrialStatus.GRANTED?t("loot_received",t("booster_"+id)):result==GameStore.PowerTrialStatus.STORAGE_FULL?q("stock_full"):t("claimed"));if(result==GameStore.PowerTrialStatus.GRANTED)sfx(AudioEngine.Sound.COIN);}catch(IllegalStateException failure){notice(t("save_error"));}
    }

    private void selectRewardTab(int tab){rewardTab=tab;contentScroll=0;actionGeneration++;hits.clear();invalidate();}
    private void rewardTabs(Canvas c){
        String[] labels={a("today"),a("this_week"),a("journey_short"),a("rewards_all")};
        for(int i=0;i<4;i++){final int tab=i;float x=24+i*86;if(tab==rewardTab)sweet.surface(c,new RectF(x,102,x+84,152),SweetUi.MINT,true);else round(c,x,102,84,50,15,skin.card);fit(c,labels[i],x+42,133,12,77,TEXT,tab==rewardTab,true);int before=hits.size();hit(x,102,84,50,labels[i],()->selectRewardTab(tab));if(hits.size()>before)hits.get(hits.size()-1).selected=tab==rewardTab;}
    }
    private void rewardRow(Canvas c,float y,String title,String detail,String action,String power,Runnable run){
        sweet.surface(c,new RectF(24,y,366,y+141),SweetUi.CREAM,true);
        if(power==null)coin(c,49,y+35,12);else art.power(c,power,49,y+35,17,frameTime,reduceMotion);
        fit(c,title,77,y+35,18,270,TEXT,true,false);fit(c,detail,44,y+67,13,302,skin.muted,false,false);
        label(24,y,342,81,title+", "+detail);button(c,action,44,y+83,302,48,PEACH,run);
    }
    private void drawQuestCard(Canvas c,float y,ObjectiveDashboard.Quest quest){
        sweet.surface(c,new RectF(24,y,366,y+153),quest.claimed?SweetUi.LILAC:quest.ready?SweetUi.MINT:SweetUi.CREAM,true);
        fit(c,questTitle(quest),44,y+29,18,302,TEXT,true,false);
        fit(c,quest.progress+" / "+quest.target,44,y+57,14,121,skin.muted,true,false);fit(c,"+"+quest.coins+" LumaCoins",252,y+57,13,175,skin.accent2,true,true);
        sweet.progress(c,new RectF(44,y+72,346,y+79),quest.progress/(float)quest.target,SweetUi.MINT);
        String action=quest.claimed?t("claimed"):quest.ready?a("claim_reward"):a(quest.kind==ObjectiveDashboard.QuestKind.EVENTS?quest.weekly?"play_weekly":"play_daily":"play_journey");
        label(24,y,342,88,questTitle(quest)+", "+quest.progress+" / "+quest.target+", +"+quest.coins+" LumaCoins");button(c,action,44,y+93,302,48,quest.ready?GOLD:PEACH,()->questRoute(quest));
    }
    private void drawRewards(Canvas c){
        ensureHub();ObjectiveDashboard.Snapshot dash=hubObjectives;header(c,a("objectives"),a("ready_count",dash.readyCount));rewardTabs(c);
        if(rewardTab==3){drawReadyRewards(c,dash);return;}
        if(rewardTab==2){
            int milestone=dash.nextMilestone;String gift=dash.journeyLootId;
            rewardRow(c,174,q("gifts"),milestone==0?e("saga_complete"):q("gift_progress",Math.min(cachedCompleted,milestone),milestone,t("booster_"+gift)),dash.journeyReady?q("claim_gift",t("booster_"+gift)):a("play_journey"),gift.isEmpty()?null:gift,()->{if(dash.journeyReady)collectJourneyGift(milestone);else continueJourney();});
            fit(c,q("trials"),25,350,20,340,TEXT,true,false);
            for(int i=0;i<dash.trials.size();i++){ObjectiveDashboard.Trial trial=dash.trials.get(i);float y=373+i*158;String detail=trial.claimed?t("claimed"):trial.unlocked?q("trial",t(trial.nameKey)):e("power_unlock_at",trial.unlockLevel);
                rewardRow(c,y,t(trial.nameKey),detail,trial.ready?a("claim_reward"):trial.claimed?t("claimed"):a("play_journey"),trial.id,()->{if(trial.ready)claimTrial(trial.id);else if(!trial.claimed)continueJourney();else notice(t("claimed"));});
            }return;
        }
        boolean weekly=rewardTab==1;float y=174;
        if(!weekly){
            rewardRow(c,y,v("login_day",dash.loginDay),"+"+dash.loginCoins+" LumaCoins",dash.loginDailyReady?a("claim_daily"):t("claimed"),null,()->claimLogin(dash.dayId));
            wrap(c,a("visit_note"),44,y+163,302,12,skin.muted,false,18);label(24,y+145,342,67,a("visit_note"));y+=217;
        }else{fit(c,dash.weekId,25,y+12,13,335,skin.muted,false,false);y+=29;}
        for(ObjectiveDashboard.Quest quest:weekly?dash.weeklyQuests:dash.dailyQuests){drawQuestCard(c,y,quest);y+=169;}
        quietButton(c,a(weekly?"play_weekly":"play_daily")+"  →",24,y+9,342,()->{eventTab=weekly?1:0;go("events");});
    }
    private void drawReadyRewards(Canvas c,ObjectiveDashboard.Snapshot dash){
        float y=174;
        if(dash.loginDailyReady){rewardRow(c,y,v("login_day",dash.loginDay),"+"+dash.loginCoins+" LumaCoins",a("claim_daily"),null,()->claimLogin(dash.dayId));y+=153;}
        for(ObjectiveDashboard.Quest quest:dash.dailyQuests)if(quest.ready){rewardRow(c,y,a("today")+" · "+questTitle(quest),"+"+quest.coins+" LumaCoins",a("claim_reward"),null,()->claimQuest(quest));y+=153;}
        for(ObjectiveDashboard.Quest quest:dash.weeklyQuests)if(quest.ready){rewardRow(c,y,a("this_week")+" · "+questTitle(quest),"+"+quest.coins+" LumaCoins",a("claim_reward"),null,()->claimQuest(quest));y+=153;}
        if(dash.dailyEventReady){rewardRow(c,y,t("daily")+" · "+t("bonus"),"+100 LumaCoins",a("claim_reward"),null,()->claimEventBonus(true,dash.dayId));y+=153;}
        if(dash.weeklyEventReady){rewardRow(c,y,t("weekly")+" · "+t("bonus"),"+250 LumaCoins",a("claim_reward"),null,()->claimEventBonus(false,dash.weekId));y+=153;}
        if(dash.journeyReady){rewardRow(c,y,q("gifts"),t("booster_"+dash.journeyLootId)+" · "+dash.nextMilestone,q("claim_gift",t("booster_"+dash.journeyLootId)),dash.journeyLootId,()->collectJourneyGift(dash.nextMilestone));y+=153;}
        for(ObjectiveDashboard.Trial trial:dash.trials)if(trial.ready){rewardRow(c,y,t(trial.nameKey),q("trial",t(trial.nameKey)),a("claim_reward"),trial.id,()->claimTrial(trial.id));y+=153;}
        if(dash.readyCount==0){dressedMascot(c,195,263,43);wrap(c,a("all_claimed"),195,389,305,21,TEXT,true,28);quietButton(c,a("today"),24,437,342,()->selectRewardTab(0));}
    }

    private void drawJourney(Canvas c){
        boolean remix=journeyTab==1;int total=remix?BalancedCatalog.REMIX_COUNT:BalancedCatalog.LEVEL_COUNT,unlocked=remix?store.remixUnlockedLevel():store.unlockedLevel();
        if(remix&&!store.remixAvailable()){header(c,e("nav_map"),t("remix_unlock"));tabs(c,t("journey"),t("remix"),journeyTab,101,()->{journeyTab=0;contentScroll=0;},()->{});round(c,24,170,342,350,27,skin.card);dressedMascot(c,195,270,52);wrap(c,t("remix_unlock"),195,392,292,18,TEXT,true,25);quietButton(c,t("continue"),44,449,302,()->previewLevel(Math.min(12,store.unlockedLevel()),0,0));return;}
        int first=SagaMap.firstVisible(contentScroll,total),last=SagaMap.lastVisible(contentScroll,viewportH,total);
        drawingMapNodes=true;c.save();c.clipRect(0,contentScroll+SagaMap.header(viewportH),390,contentScroll+drawingClipBottom);Path route=new Path();for(int n=first;n<=last;n++){float x=SagaMap.x(n),y=SagaMap.y(n);if(n==first)route.moveTo(x,y);else{float px=SagaMap.x(n-1),py=SagaMap.y(n-1);route.cubicTo(px,py+48,x,y-48,x,y);}}
        p.setColor(0xffd5c0e7);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(17);p.setStrokeCap(Paint.Cap.ROUND);c.drawPath(route,p);p.setColor(alpha(skin.accent2,100));p.setStrokeWidth(4);c.drawPath(route,p);p.setStyle(Paint.Style.FILL);
        java.util.List<SocialClient.Friend> friends=SocialClient.cachedFriends(getContext());
        for(int n=first;n<=last;n++){final int number=n;float x=SagaMap.x(n),y=SagaMap.y(n);boolean open=n<=unlocked,current=n==unlocked;int stars=remix?store.remixStars(n):store.stars(n);ProgressionInfo info=StrategyCatalog.info(n,remix);int color=difficultyColor(info);
            if((n-1)%100==0){round(c,27,y-65,336,34,15,0xfafff7ff);fit(c,worldName(n)+"  ·  "+n+"–"+Math.min(total,n+99),195,y-43,14,315,TEXT,true,true);}
            if(current){p.setColor(alpha(color,reduceMotion?65:55+(int)(Math.sin(frameTime*.004)*20)));c.drawCircle(x,y,39,p);}
            p.setColor(0xffb4a0c9);c.drawOval(x-34,y-23,x+34,y+35,p);p.setColor(open?stars>0?0xffa6dac8:current?color:0xffe5d8f2:0xffe1d9e7);c.drawCircle(x,y,30,p);stroke(c,new RectF(x-29,y-29,x+29,y+29),29,alpha(WHITE,open?110:35),1.5f);
            if(open)center(c,""+n,x,y+8,20,current?INK:TEXT,true);else{icon(c,"lock",x,y-4,15,skin.muted);center(c,""+n,x,y+19,12,skin.muted,true);}
            for(int k=0;k<3;k++)star(c,x+(k-1)*14,y+40,6,k<stars?GOLD:alpha(WHITE,60));
            if(info.keyCount>0){round(c,x-41,y-25,23,23,8,GOLD);icon(c,"lock",x-29,y-14,13,INK);}if(info.difficulty!=ProgressionInfo.Difficulty.NORMAL)star(c,x+29,y-26,9,color);
            hit(x-38,y-31,76,79,t("level_number",n)+", "+e(info.difficultyKey)+", "+e(info.elementKey)+(open?"":", "+t("locked")),()->{if(open)previewLevel(number,remix?3:0,0);else notice(t("unlock_previous"));});
            if(!remix&&n%10==0){final int guardianNumber=n/10;float gx=x<195?306:24;boolean done=store.guardianCleared(guardianNumber),available=store.guardianUnlocked(guardianNumber);round(c,gx,y-15,60,48,13,done?SweetUi.light(SweetUi.MINT):available?SweetUi.light(SweetUi.LILAC):skin.card);icon(c,done?"check":available?"events":"lock",gx+14,y+9,13,TEXT);fit(c,"G"+guardianNumber,gx+39,y+14,12,36,TEXT,true,true);hit(gx,y-15,60,48,q("guardian",guardianNumber)+", "+(done?q("guardian_done"):available?t("play"):q("guardian_locked",n)),()->{if(available)openGuardian(guardianNumber);else{storyChapter=(guardianNumber-1)/10+1;go("story");}});}
            if(!remix){int i=0;for(SocialClient.Friend friend:friends)if(friend.level==n&&i++<2){float fx=x+(x<195?59:-59),fy=y+(i-1)*29;round(c,fx-24,fy-13,48,26,12,0xffb8a1ef);fit(c,friend.nickname,fx,fy+5,12,44,INK,true,true);hit(fx-25,fy-14,50,28,friend.nickname+", "+t("level_number",n),()->openSocial());}}
        }
        drawingMapNodes=false;c.restore();c.save();c.translate(0,contentScroll);float old=drawingScroll;drawingScroll=0;if(viewportH<500){round(c,0,0,390,58,0,0xfff7f0fc);fit(c,e("nav_map"),17,26,18,112,TEXT,true,false);fit(c,t(remix?"remix":"journey"),17,47,12,112,skin.accent2,true,false);hit(5,0,122,57,t("journey")+" / "+t("remix"),()->{journeyTab=1-journeyTab;contentScroll=0;});quietButton(c,v("current_level"),131,6,112,()->contentScroll=SagaMap.scrollTo(unlocked,viewportH,total));quietButton(c,v("jump"),250,6,116,()->jumpMap(total));drawingScroll=old;c.restore();return;}round(c,0,0,390,194,0,0xfff7f0fc);header(c,e("nav_map"),worldName(SagaMap.at(contentScroll+viewportH*.5f,total))+"  ·  "+total);
        tabs(c,t("journey"),t("remix"),journeyTab,101,()->{journeyTab=0;contentScroll=SagaMap.scrollTo(store.unlockedLevel(),viewportH,BalancedCatalog.LEVEL_COUNT);},()->{journeyTab=1;contentScroll=SagaMap.scrollTo(store.remixUnlockedLevel(),viewportH,BalancedCatalog.REMIX_COUNT);});
        quietButton(c,v("current_level"),24,153,120,()->contentScroll=SagaMap.scrollTo(unlocked,viewportH,total));quietButton(c,v("jump"),151,153,100,()->jumpMap(total));quietButton(c,a("story_book"),266,153,100,()->go("story"));drawingScroll=old;c.restore();
    }
    private void jumpMap(int total){android.widget.EditText field=new android.widget.EditText(getContext());field.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);field.setHint("1–"+total);new android.app.AlertDialog.Builder(getContext()).setTitle(v("jump")).setMessage(v("jump_note")).setView(field).setPositiveButton(t("continue"),(d,w)->{try{int n=Integer.parseInt(field.getText().toString());if(n<1||n>total)throw new NumberFormatException();contentScroll=SagaMap.scrollTo(n,viewportH,total);invalidate();}catch(NumberFormatException invalid){notice("1–"+total);}}).setNegativeButton(t("cancel"),null).show();}
    private void previewLevel(int index,int m,int event){
        if(m==3&&(!store.remixAvailable()||index>store.remixUnlockedLevel())){notice(t("remix_unlock"));return;}
        if(m==0&&!store.isLevelUnlocked(index)){notice(t("unlock_previous"));return;}
        briefingIndex=index;briefingMode=m;briefingEvent=event;
        briefingLevel=m==0?StrategyCatalog.get(index):m==3?StrategyCatalog.remix(index):StrategyCatalog.event(m==1?store.dailySeed(event):store.weeklySeed(event));
        briefingReturn=screen.equals("play")?m==0||m==3?"journey":"events":screen;if(screen.equals("play")){menuHistory.clear();menuHistory.push(briefingReturn);}go("briefing");
    }
    private void drawBriefing(Canvas c){
        if(briefingLevel==null){go("home");return;}
        round(c,24,18,48,48,16,skin.card);icon(c,"back",48,42,21,TEXT);hit(24,18,48,48,t("back"),()->backMenu());wallet(c,282,24);
        fit(c,briefingMode==0?t("level_number",briefingIndex):briefingMode==3?t("remix")+" "+briefingIndex:t(briefingMode==1?"daily":"weekly"),195,113,31,330,TEXT,true,true);
        ProgressionInfo info=briefingMode==0||briefingMode==3?StrategyCatalog.info(briefingIndex,briefingMode==3):null;
        if(info!=null)difficultyPill(c,info,75,137,240);
        round(c,24,188,342,363,27,0xfafff9ff);fit(c,e("objective"),44,219,13,290,skin.accent2,true,false);
        String objective=briefingLevel.barriers.isEmpty()?e("objective_clear",briefingLevel.tiles.size()):e("objective_open_seals");
        wrap(c,objective,44,249,294,20,TEXT,false,26);
        float size=152,cc=size/briefingLevel.size,left=119,top=302;
        for(int r=0;r<briefingLevel.size;r++)for(int col=0;col<briefingLevel.size;col++)round(c,left+col*cc+1,top+r*cc+1,cc-2,cc-2,7,alpha(WHITE,8));
        for(Tile tile:briefingLevel.tiles){float x=left+(tile.col+.5f)*cc,y=top+(tile.row+.5f)*cc;drawTile(c,tile,x,y,cc-4,false);if(briefingLevel.isKey(tile.id)){p.setColor(GOLD);c.drawCircle(x-cc*.27f,y-cc*.28f,4,p);}}
        for(Barrier wall:briefingLevel.barriers){float x=left+(wall.col+.5f)*cc,y=top+(wall.row+.5f)*cc;round(c,x-cc*.42f,y-cc*.42f,cc*.84f,cc*.84f,7,alpha(GOLD,45));icon(c,"lock",x,y,cc*.5f,GOLD);}
        MoveBudget budget=MoveBudget.start(briefingMode,briefingLevel);fit(c,e("action_budget",budget.total()),195,492,20,300,GOLD,true,true);
        if(info!=null)fit(c,e(info.elementKey),195,526,14,300,TEXT,true,true);label(24,188,342,363,e("objective")+": "+objective+". "+e("action_budget",budget.total())+(info==null?"":". "+e(info.difficultyKey)+", "+e(info.beatKey)+", "+e(info.elementKey)));
        fit(c,q("score",JourneyExperience.threeStarLimit(budget)),195,574,12,330,skin.muted,false,true);
        Catalog.BoosterItem next=InventoryPresentation.nextPower(store.unlockedLevel());fit(c,next==null?e("all_powers_unlocked"):e("unlock_next",t(next.nameKey),next.unlockLevel),195,593,14,330,TEXT,false,true);
        button(c,e("action_play")+"  →",44,633,302,54,PEACH,()->start(briefingIndex,briefingMode,briefingEvent));
        quietButton(c,e("tab_powers"),44,709,302,()->{collectionTab=3;collectionPage=0;go("collection");});
        if(briefingLevel.minimumActions>briefingLevel.tiles.size()){wrap(c,q("next_plan"),195,790,330,13,TEXT,true,20);quietButton(c,q("practice_gate"),44,843,302,()->openTutorial(false));}
    }
    private void tabs(Canvas c,String a,String b,int tab,float y,Runnable aa,Runnable bb){
        round(c,24,y,342,45,15,skin.card);round(c,28+tab*169,y+4,165,37,12,alpha(skin.accent,65));fit(c,a,110,y+29,14,148,tab==0?TEXT:skin.muted,true,true);fit(c,b,280,y+29,14,148,tab==1?TEXT:skin.muted,true,true);hit(24,y,171,45,a,aa);hit(195,y,171,45,b,bb);
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
    private void gearGlyph(Canvas c,AvatarGearCatalog.Item item,float x,float y,float r){companionPortrait.drawGearGlyph(c,item,x,y,r);}
    private void collectionSegments(Canvas c){
        String[] names={e("tab_companions"),e("tab_themes"),e("tab_equipment"),e("tab_powers")};
        round(c,24,198,342,50,17,0xfff4eafa);
        for(int i=0;i<4;i++){final int tab=i;float x=27+i*84;if(collectionTab==i)round(c,x,202,82,42,13,skin.accent2);fit(c,names[i],x+41,228,12,76,collectionTab==i?INK:TEXT,collectionTab==i,true);hit(x,198,84,50,names[i],()->{collectionTab=tab;collectionPage=0;gearSlotFilter=-1;});}
    }
    private void drawCollection(Canvas c){
        boolean chars=collectionTab==0,gear=collectionTab==2,powers=collectionTab==3;
        java.util.List<InventoryPresentation.Entry> all=powers?Collections.emptyList():chars?InventoryPresentation.companions(store):gear?InventoryPresentation.gear(store,gearSlotFilter<0?null:AvatarGearCatalog.Slot.values()[gearSlotFilter]):InventoryPresentation.themes(store);
        InventoryPresentation.Counts counts=InventoryPresentation.counts(all);
        header(c,e("nav_inventory"),powers?e("tab_powers"):e("collection_count",counts.owned,counts.total));
        round(c,24,99,342,85,22,0xfafff8ff);dressedMascot(c,68,141,19);fit(c,t(character.nameKey),112,129,19,151,TEXT,true,false);fit(c,a("look"),112,155,13,151,skin.muted,false,false);quietButton(c,a("avatar"),274,117,76,()->go("wardrobe"));
        collectionSegments(c);
        String[] filters={e("filter_all"),e("filter_owned"),e("filter_locked")};round(c,24,262,342,45,15,0xfffcf5ff);
        for(int i=0;i<3;i++){final int filter=i;float x=27+i*112;if(inventoryFilter==i)round(c,x,266,110,37,12,alpha(skin.accent2,28));fit(c,filters[i],x+55,290,12,102,inventoryFilter==i?skin.accent2:skin.muted,true,true);hit(x,260,112,48,filters[i],()->{inventoryFilter=filter;collectionPage=0;});}
        if(powers){drawPowerInventory(c);return;}
        InventoryPresentation.Filter filter=inventoryFilter==1?InventoryPresentation.Filter.OWNED:inventoryFilter==2?InventoryPresentation.Filter.LOCKED:InventoryPresentation.Filter.ALL;
        java.util.List<InventoryPresentation.Entry> shown=InventoryPresentation.filter(all,filter);
        InventoryPresentation.Page page=InventoryPresentation.page(shown.size(),3,collectionPage);collectionPage=page.page;
        for(int i=0;i<page.size();i++){
            InventoryPresentation.Entry entry=shown.get(page.first-1+i);float y=323+i*124;boolean selected=entry.selected(),owned=entry.owned();
            round(c,24,y,342,112,21,selected?0xffdef1ec:0xfffcf4ff);if(selected)stroke(c,new RectF(24,y,366,y+112),21,skin.accent2,2);
            round(c,36,y+17,65,74,18,alpha(selected?skin.accent2:skin.accent,26));
            if(gear)gearGlyph(c,entry.gear,68,y+53,23);else if(chars)companionPortrait.draw(c,Catalog.character(entry.id),68,y+53,16,AvatarStyles.find(avatarStyle),AvatarStage.Pose.IDLE,frameTime,reduceMotion,loadout,wornCosmetics);else{Catalog.SkinItem theme=Catalog.skin(entry.id);round(c,47,y+31,42,46,12,theme.bg);round(c,53,y+39,14,14,5,theme.accent);round(c,71,y+57,12,12,4,theme.accent2);}
            String title=gear?g(entry.nameKey):t(entry.nameKey);fit(c,title,114,y+30,19,225,TEXT,true,false);
            String state=e(entry.stateKey());fit(c,state,114,y+54,13,220,selected?skin.accent2:owned?TEXT:GOLD,true,false);
            String source=owned?gear?InventoryPresentation.effectCaption(store.resolvedLanguage(),entry):e("effect_appearance"):InventoryPresentation.sourceCaption(store.resolvedLanguage(),entry);
            fit(c,source,114,y+82,13,231,owned?skin.muted:GOLD,false,false);
            if(gear&&gearSlotFilter>=0)fit(c,g(entry.gear.slot.key()),114,y+102,12,225,skin.muted,false,false);
            if(selected)icon(c,"check",343,y+50,17,skin.accent2);else icon(c,"next",343,y+51,16,TEXT);
            hit(24,y,342,112,title+", "+state+", "+source,()->{if(gear)previewGear(entry.gear);else previewCosmetic(chars,chars?characterIndex(entry.id):themeIndex(entry.id));});
        }
        if(shown.isEmpty())wrap(c,e("collection_empty"),195,398,294,18,TEXT,true,25);
        inventoryPager(c,page);
    }
    private int characterIndex(String id){for(int i=0;i<Catalog.CHARACTERS.length;i++)if(Catalog.CHARACTERS[i].id.equals(id))return i;return 0;}
    private int themeIndex(String id){for(int i=0;i<Catalog.SKINS.length;i++)if(Catalog.SKINS[i].id.equals(id))return i;return 0;}
    private void inventoryPager(Canvas c,InventoryPresentation.Page page){if(page.pageCount<=1)return;float y=H-139;quietButton(c,"‹",24,y,62,()->{if(page.hasPrevious())collectionPage--;});fit(c,e("saga_page",page.page+1,page.pageCount),195,y+29,14,190,TEXT,true,true);quietButton(c,"›",304,y,62,()->{if(page.hasNext())collectionPage++;});}
    private void drawPowerInventory(Canvas c){
        HashSet<String> active=new HashSet<>();for(Catalog.BoosterItem booster:Catalog.BOOSTERS)if(board!=null&&powerActive(booster.id))active.add(booster.id);
        ArrayList<InventoryPresentation.Power> shown=new ArrayList<>();for(InventoryPresentation.Power power:InventoryPresentation.powers(store,active))if(inventoryFilter==0||inventoryFilter==1&&power.unlocked&&(power.stock>0||power.active)||inventoryFilter==2&&!power.unlocked)shown.add(power);
        InventoryPresentation.Page page=InventoryPresentation.page(shown.size(),3,collectionPage);collectionPage=page.page;
        for(int i=0;i<page.size();i++){InventoryPresentation.Power power=shown.get(page.first-1+i);float y=323+i*124;round(c,24,y,342,112,21,power.active?0xffdbf0ea:0xfffcf4ff);round(c,37,y+23,57,62,17,alpha(power.unlocked?skin.accent2:skin.muted,26));if(power.unlocked)art.power(c,power.id,65,y+52,27,frameTime,reduceMotion);else icon(c,"lock",65,y+52,27,skin.muted);
            fit(c,t(power.nameKey),111,y+31,20,225,TEXT,true,false);fit(c,e(power.state.key()),111,y+57,14,220,power.active?skin.accent2:power.unlocked?TEXT:GOLD,true,false);
            fit(c,power.unlocked?e("power_stock",power.stock):e("power_unlock_at",power.unlockLevel),111,y+87,13,230,power.unlocked?skin.muted:GOLD,false,false);
            hit(24,y,342,112,t(power.nameKey)+", "+e(power.state.key())+", "+e("power_stock",power.stock),()->powerOffer(power.id));
        }
        if(shown.isEmpty())wrap(c,e("collection_empty"),195,398,294,18,TEXT,true,25);inventoryPager(c,page);
    }
    private String shortGearSource(AvatarGearCatalog.Item item){
        String source=item.source==AvatarGearCatalog.Source.JOURNEY_CLEARS?t("journey"):item.source==AvatarGearCatalog.Source.WEEKLY_WINS?t("weekly"):item.source==AvatarGearCatalog.Source.EVENT_STARS?t("events"):g(item.source.key());
        return source+" · "+Math.min(item.requirement,gearProgress.value(item.source))+"/"+item.requirement;
    }
    private void previewGear(AvatarGearCatalog.Item item){
        boolean owned=store.ownsAvatarGear(item.id),equipped=item.id.equals(loadout.item(item.slot).id);String key="gear:"+item.id;
        AvatarLoadout preview=new AvatarLoadout(item.slot==AvatarGearCatalog.Slot.HEAD?item.id:loadout.headId,item.slot==AvatarGearCatalog.Slot.AURA?item.id:loadout.auraId,item.slot==AvatarGearCatalog.Slot.CHARM?item.id:loadout.charmId);
        String message=g(item.slot.key())+" · "+g(item.rarity.key())+"\n"+(equipped?t("equipped"):owned?u("owned"):u("get"))+"\n\n"+GearStrings.effectCaption(store.resolvedLanguage(),item)+"\n"+(owned?acquisitionCaption(store.avatarGearAcquisitionSource(item.id),GearStrings.sourceCaption(store.resolvedLanguage(),item,gearProgress)):GearStrings.sourceCaption(store.resolvedLanguage(),item,gearProgress))+"\n\n"+a("preview_only");
        android.app.AlertDialog.Builder dialog=new android.app.AlertDialog.Builder(getContext()).setTitle(g(item.nameKey)).setView(companionPreview(character,preview,wornCosmetics)).setMessage(message).setNegativeButton(t("close"),null).setNeutralButton(a(key.equals(store.trackedCollectionGoal())?"untrack_item":"track_item"),(d,w)->toggleGoal(key));
        if(!equipped&&(owned||item.source==AvatarGearCatalog.Source.EARNED_COINS))dialog.setPositiveButton(owned?t("equip"):u("coin_price",item.priceCoins),(d,w)->savedAction(()->{
            if(!owned&&!store.purchaseAvatarGear(item.id)){notice(t("not_enough"));return;}if(store.equipAvatarGear(item.id)){sfx(owned?AudioEngine.Sound.SELECT:AudioEngine.Sound.PURCHASE);haptic();}
        }));
        else if(!owned)dialog.setPositiveButton(a("view_source"),(d,w)->{sourceRoute(item.source==AvatarGearCatalog.Source.JOURNEY_CLEARS?CollectionGoal.Route.JOURNEY:item.source==AvatarGearCatalog.Source.WEEKLY_WINS?CollectionGoal.Route.WEEKLY:item.source==AvatarGearCatalog.Source.EVENT_STARS?CollectionGoal.Route.DAILY:CollectionGoal.Route.REWARDS);});
        dialog.show();
    }
    private void previewCosmetic(boolean chars,int index){
        Catalog.CharacterItem ch=chars?Catalog.CHARACTERS[index]:null;Catalog.SkinItem sk=chars?null:Catalog.SKINS[index];
        String id=chars?ch.id:sk.id;boolean owned=chars?store.ownsCharacter(id):store.ownsSkin(id),equipped=id.equals(chars?store.equippedCharacter():store.equippedSkin());int price=chars?ch.price:sk.price;
        android.widget.LinearLayout layout=new android.widget.LinearLayout(getContext());layout.setOrientation(android.widget.LinearLayout.VERTICAL);layout.setPadding(30,10,30,20);
        View preview=new View(getContext()){@Override protected void onDraw(Canvas canvas){super.onDraw(canvas);float center=getWidth()/2f,unit=getHeight()/3.3f;if(chars)companionPortrait.draw(canvas,ch,center,getHeight()*.5f,Math.min(getWidth(),getHeight())/4.3f,AvatarStyles.find(avatarStyle),AvatarStage.Pose.HAPPY,SystemClock.uptimeMillis(),reduceMotion,loadout,wornCosmetics);else{round(canvas,center-unit,getHeight()*.2f,unit*2,unit*1.65f,unit*.3f,sk.bg);for(int i=0;i<4;i++)round(canvas,center-unit*.8f+(i%2)*unit*.85f,getHeight()*.3f+(i/2)*unit*.65f,unit*.65f,unit*.5f,unit*.1f,i%2==0?sk.accent:sk.accent2);}}};
        preview.setContentDescription(t(chars?ch.nameKey:sk.nameKey)+". "+t("cosmetic_only"));layout.addView(preview,new android.widget.LinearLayout.LayoutParams(-1,(int)(165*getResources().getDisplayMetrics().density)));
        android.widget.TextView note=new android.widget.TextView(getContext());note.setText((equipped?t("equipped"):owned?u("owned"):u("get"))+"\n"+t("companion_wallet_note",store.coins())+"\n"+t("cosmetic_only"));note.setTextColor(skin.muted);note.setGravity(Gravity.CENTER);note.setTextSize(16);layout.addView(note);
        android.app.AlertDialog.Builder dialog=new android.app.AlertDialog.Builder(getContext()).setTitle(t(chars?ch.nameKey:sk.nameKey)).setView(layout).setNegativeButton(t("close"),(d,w)->{});
        if(!equipped)dialog.setPositiveButton(owned?t("equip"):t("companion_purchase",price),(d,w)->savedAction(()->{
            boolean success=owned||(chars?store.purchaseCharacter(id):store.purchaseSkin(id));if(!success){notice(t("not_enough"));return;}
            if(chars)store.equipCharacter(id);else store.equipSkin(id);syncStyle();sfx(owned?AudioEngine.Sound.SELECT:AudioEngine.Sound.PURCHASE);haptic();invalidate();
        }));else dialog.setPositiveButton(t("equipped"),(d,w)->{});
        dialog.show();
    }
    private void selectShopCategory(int category){shopCategory=category;contentScroll=0;actionGeneration++;hits.clear();invalidate();}
    private void drawShop(Canvas c){
        header(c,t("shop"),"LumaCoins  /  LumaGold");
        sweet.surface(c,new RectF(24,102,366,180),SweetUi.CREAM,true);coin(c,54,135,17);fit(c,""+store.coins(),84,143,27,232,skin.accent2,true,false);fit(c,"LumaCoins · "+v("earned"),43,165,12,290,skin.muted,false,false);
        String[] tabs={e("tab_powers"),a("appearance"),"LumaGold"};for(int i=0;i<3;i++){final int category=i;float x=24+i*116;round(c,x,198,110,50,16,shopCategory==i?SweetUi.light(SweetUi.MINT):skin.card);fit(c,tabs[i],x+55,229,13,103,TEXT,true,true);int before=hits.size();hit(x,198,110,50,tabs[i],()->selectShopCategory(category));if(hits.size()>before)hits.get(hits.size()-1).selected=shopCategory==i;}
        if(shopCategory==0){
            for(int i=0;i<3;i++){
                Catalog.BoosterItem item=Catalog.BOOSTERS[shopPage*3+i];String id=item.id;boolean unlocked=store.boosterUnlocked(id);float y=272+i*124;
                sweet.surface(c,new RectF(24,y,366,y+112),SweetUi.CREAM,true);if(unlocked)art.power(c,id,61,y+44,25,frameTime,reduceMotion);else icon(c,"lock",61,y+44,25,skin.muted);
                fit(c,t(item.nameKey),104,y+30,19,238,TEXT,true,false);fit(c,unlocked?u("stock",store.boosterStock(id)):t("power_level",item.unlockLevel),104,y+55,12,238,skin.muted,false,false);
                fit(c,unlocked?u("pack",item.amount,store.boosterPrice(id)):t("power_unlock",item.unlockLevel),44,y+91,13,289,skin.accent2,true,false);icon(c,"next",340,y+86,15,TEXT);hit(24,y,342,112,t(item.nameKey)+", "+(unlocked?u("pack",item.amount,store.boosterPrice(id)):t("power_unlock",item.unlockLevel)),()->powerOffer(id));
            }
            quietButton(c,"‹",24,658,67,()->shopPage=0);fit(c,t("powers_page",shopPage+1,2),195,687,12,190,TEXT,true,true);quietButton(c,"›",299,658,67,()->shopPage=1);
            quietButton(c,a("journey_rewards")+"  →",24,730,342,()->openRewards(2));quietButton(c,a("open_inventory")+"  →",24,796,342,()->{collectionTab=3;collectionPage=0;go("collection");});
        }else if(shopCategory==1){
            sweet.surface(c,new RectF(24,269,366,418),SweetUi.LILAC,true);art.power(c,"trail",61,309,23,frameTime,reduceMotion);fit(c,v("loot"),102,300,19,242,TEXT,true,false);fit(c,"150 LumaCoins",102,326,15,240,skin.accent2,true,false);wrap(c,w("capsule_odds"),44,362,300,12,skin.muted,false,18);hit(24,269,342,149,v("loot")+", "+w("capsule_note"),()->openCapsule());
            ArrayList<CosmeticCatalog.Item> items=new ArrayList<>();for(CosmeticCatalog.Item item:CosmeticCatalog.ITEMS)if(item.priceCoins>0)items.add(item);
            int pages=(items.size()+2)/3;shopLookPage=Math.max(0,Math.min(pages-1,shopLookPage));for(int i=0;i<3&&shopLookPage*3+i<items.size();i++)drawWardrobeItem(c,items.get(shopLookPage*3+i),437+i*124);
            quietButton(c,"‹",24,830,67,()->{if(shopLookPage>0)shopLookPage--;});fit(c,(shopLookPage+1)+" / "+pages,195,859,14,170,TEXT,true,true);quietButton(c,"›",299,830,67,()->{if(shopLookPage+1<pages)shopLookPage++;});quietButton(c,a("avatar")+"  →",24,900,342,()->go("wardrobe"));
        }else{
            sweet.surface(c,new RectF(24,269,366,545),SweetUi.LILAC,true);star(c,60,310,20,0xff9460ac);fit(c,"LumaGold",101,315,25,242,TEXT,true,false);fit(c,a("gold_account"),44,355,17,300,TEXT,true,false);wrap(c,v("premium"),44,391,300,14,skin.muted,false,21);wrap(c,a("gold_closed"),44,467,300,14,TEXT,true,21);
            button(c,a("view_wallet")+"  →",24,571,342,50,PEACH,()->openCommerce());quietButton(c,v("wardrobe")+" · LumaGold  →",24,641,342,()->getContext().startActivity(new android.content.Intent(getContext(),com.androidhit.lumadrift.social.CosmeticVaultActivity.class)));wrap(c,v("separate"),195,746,323,14,skin.muted,true,22);
        }
    }
    private void powerOffer(String id){
        Catalog.BoosterItem item=Catalog.booster(id);boolean unlocked=store.boosterUnlocked(id);
        android.app.AlertDialog.Builder offer=new android.app.AlertDialog.Builder(getContext()).setTitle(t(item.nameKey)).setMessage(t(item.descriptionKey)+"\n\n"+(unlocked?t("stock")+": "+store.boosterStock(id):t("power_unlock",item.unlockLevel))).setNegativeButton(t("close"),(d,w)->{});
        if(unlocked){offer.setPositiveButton(t("power_buy",item.amount,store.boosterPrice(id)),(d,w)->savedAction(()->{if(store.purchaseBooster(id)){sfx(AudioEngine.Sound.PURCHASE);notice(t("purchase_success"));}else notice(t("not_enough"));}));offer.setNeutralButton(t("power_ad"),(d,w)->getContext().startActivity(com.androidhit.lumadrift.commerce.CommerceActivity.powerIntent(getContext(),id)));}
        offer.show();
    }
    private void eventSegments(Canvas c){String[] names={t("daily"),t("weekly"),u("sealed")};round(c,24,103,342,50,15,skin.card);for(int i=0;i<3;i++){final int n=i;float x=27+i*113;if(eventTab==i)round(c,x,107,110,37,12,alpha(skin.accent,60));fit(c,names[i],x+55,132,12,100,TEXT,eventTab==i,true);int before=hits.size();hit(x,103,110,50,names[i],()->{eventTab=n;actionGeneration++;hits.clear();invalidate();});if(hits.size()>before)hits.get(hits.size()-1).selected=eventTab==i;}}
    private void drawSealedMenu(Canvas c){
        header(c,u("sealed"),u("sealed_note"));eventSegments(c);round(c,24,163,342,147,24,skin.card);
        icon(c,"lock",55,193,25,GOLD);fit(c,u("sealed"),82,198,20,260,TEXT,true,false);wrap(c,u("sealed_rule"),44,229,300,12,skin.muted,false,18);
        for(int i=1;i<=SealedCatalog.COUNT;i++){final int level=i;float x=24+((i-1)%2)*178,y=332+((i-1)/2)*91;round(c,x,y,164,77,20,skin.card);icon(c,i<3?"lock":"events",x+28,y+37,23,GOLD);fit(c,t("stage_number",i),x+55,y+33,16,99,TEXT,true,false);fit(c,t("play")+" →",x+55,y+56,12,99,skin.accent2,true,false);hit(x,y,164,77,u("sealed")+" · "+t("stage_number",i),()->openChallenge(ChallengeCode.encodeSealed(level)));}
        fit(c,u("sealed_unlimited"),195,642,11,340,skin.accent2,true,true);
    }
    private void drawEvents(Canvas c){
        if(eventTab==2){drawSealedMenu(c);return;}
        boolean daily=eventTab==0;int count=daily?Catalog.DAILY_COUNT:Catalog.WEEKLY_COUNT,completed=daily?store.dailyCompletedCount():store.weeklyCompletedCount(),bonus=daily?100:250;
        boolean claimed=daily?store.dailyBonusClaimed():store.weeklyBonusClaimed(),complete=completed==count;
        header(c,t("events"),daily?store.dailyId():store.weeklyId());
        eventSegments(c);
        RectF hero=new RectF(24,162,366,310);p.setAlpha(255);p.setShader(new LinearGradient(24,162,366,310,new int[]{blend(skin.card,skin.accent,.24f),skin.card},null,Shader.TileMode.CLAMP));c.drawRoundRect(hero,25,25,p);p.setShader(null);
        wrap(c,t(daily?"event_goal_daily":"event_goal_weekly"),44,189,235,14,TEXT,false,19);
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
            float x=daily?24+i*117:i<4?24+i*88:68+(i-4)*88,y=daily?507:507+(i/4)*77,w=daily?108:78,h=daily?110:68;
            round(c,x,y,w,h,20,stars>0?blend(skin.card,skin.accent2,.12f):skin.card);
            if(open&&stars==0)stroke(c,new RectF(x,y,x+w,y+h),20,skin.accent2,1.7f);
            if(daily){
                round(c,x+w/2-19,y+13,38,38,14,alpha(open?skin.accent:skin.muted,45));
                if(stars>0)icon(c,"check",x+w/2,y+32,20,skin.accent2);else if(!open)icon(c,"lock",x+w/2,y+32,18,skin.muted);else center(c,""+(i+1),x+w/2,y+39,22,TEXT,true);
                fit(c,t("stage_number",i+1),x+w/2,y+75,13,w-12,open?TEXT:skin.muted,true,true);
                for(int k=0;k<3;k++)star(c,x+w/2+(k-1)*17,y+94,6,k<stars?GOLD:alpha(skin.muted,55));
            }else{
                center(c,""+(i+1),x+w/2,y+29,21,open?TEXT:skin.muted,true);
                if(!open)icon(c,"lock",x+w-14,y+15,11,skin.muted);else if(stars>0)icon(c,"check",x+w-14,y+15,11,skin.accent2);
                for(int k=0;k<3;k++)star(c,x+w/2+(k-1)*15,y+51,5.5f,k<stars?GOLD:alpha(skin.muted,55));
            }
            String stageLabel=t("stage_number",i+1)+", "+t(stars>0?"retry":"play")+", "+stars+" "+t("stars")+(open?"":", "+t("locked"));
            hit(x,y,w,h,stageLabel,()->openEventStage(daily,idx));
        }
        quietButton(c,a("objectives")+"  →",24,690,342,()->openRewards(daily?0:1));
        final int nextStage=next<0?0:next;final String period=daily?store.dailyId():store.weeklyId();
        button(c,t(complete?"event_replay":"event_play_next",nextStage+1),24,347,342,50,PEACH,()->{if(!period.equals(daily?store.dailyId():store.weeklyId())){syncStyle();notice(a("period_changed"));return;}openEventStage(daily,nextStage);});
        float rewardY=414;sweet.surface(c,new RectF(24,rewardY,366,rewardY+77),SweetUi.CREAM,true);
        fit(c,t("bonus"),42,rewardY+27,13,158,TEXT,true,false);coin(c,51,rewardY+54,9);text(c,"+"+bonus,70,rewardY+61,24,skin.accent2,true);label(24,rewardY,177,77,t("bonus")+": "+bonus+" LumaCoins");
        button(c,t(claimed?"claimed":complete?"claim":"locked"),208,rewardY+15,142,48,complete&&!claimed?GOLD:SweetUi.LILAC,()->{
            if(complete&&!claimed)claimEventBonus(daily,period);else notice(claimed?t("claimed"):t("event_bonus_locked",Math.max(0,count-(daily?store.dailyCompletedCount():store.weeklyCompletedCount()))));
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
        round(c,24,103,342,93,24,skin.card);fit(c,t("rank",meta.rank()),44,135,22,300,TEXT,true,false);text(c,t("rank_progress",meta.rankProgress()),44,160,12,skin.muted,false);round(c,44,174,300,5,3,alpha(WHITE,18));round(c,44,174,300*meta.rankProgress()/120f,5,3,skin.accent2);
        String[] keys={"mission_one","mission_two","mission_three"};String[] icons={"journey","collection","events"};
        for(int i=0;i<3;i++){final int idx=i;float y=214+i*119;round(c,24,y,342,105,22,skin.card);icon(c,icons[i],51,y+28,21,skin.accent2);wrap(c,t(keys[i]),77,y+25,260,14,TEXT,false,18);text(c,t("mission_progress",meta.missionProgress(i),meta.missionTarget(i)),44,y+83,14,skin.muted,true);
            label(40,y+10,305,85,t(keys[i])+", "+t("mission_progress",meta.missionProgress(i),meta.missionTarget(i)));
            boolean claimed=meta.missionClaimed(i),ready=meta.missionComplete(i);String title=claimed?t("claimed"):ready?t("claim")+" +"+meta.missionCoins(i)+" ◈":"+"+meta.missionCoins(i)+" ◈";
            quietButton(c,title,198,y+60,150,()->{if(meta.missionComplete(idx)){try{int got=meta.claimMission(idx,store);if(got>0){sfx(AudioEngine.Sound.COIN);notice("+"+got+" "+t("coins"));}else notice(t("claimed"));}catch(IllegalStateException failed){notice(t("save_error"));}}else notice(t("mission_note"));});
        }
        float yy=579;round(c,24,yy,342,70,20,skin.card);star(c,51,yy+32,15,meta.badgeUnlocked()?GOLD:skin.muted);fit(c,t("badge"),78,yy+27,15,260,TEXT,true,false);fit(c,t(meta.badgeUnlocked()?meta.badgeEquipped()?"equipped":"equip":"badge_desc"),78,yy+48,11,260,skin.muted,false,false);hit(24,yy,342,70,t("badge"),()->{if(meta.badgeUnlocked()){meta.setBadgeEquipped(!meta.badgeEquipped());sfx(AudioEngine.Sound.SELECT);}else notice(t("badge_desc"));});
        if(H>790)fit(c,t("days_note"),195,H-110,11,340,skin.muted,false,true);
    }
    private String journalStatus(String status){return t(PlayJournal.OBSERVED.equals(status)?"journal_observed":PlayJournal.NOT_OBSERVED_LOCAL.equals(status)?"journal_not_observed":"journal_unknown");}
    private void drawJournal(Canvas c){
        header(c,t("local_journal"),t("local_only"));quietButton(c,"‹ "+t("back"),24,99,107,()->go("settings"));
        settingsRow(c,"local_journal",journal.isEnabled(),163,()->{journal.setEnabled(!journal.isEnabled());if(journal.isEnabled())applyPendingContinuation();if(prefs.getBoolean("session_migration_notice",false)){notice(t("moves_migration"));prefs.edit().putBoolean("session_migration_notice",false).commit();}log("session_start","version",BuildConfig.VERSION_NAME,"language",store.resolvedLanguage());});
        wrap(c,t("journal_desc"),27,245,336,14,skin.muted,false,21);
        round(c,24,321,342,150,24,skin.card);fit(c,t("journal_sessions",journal.sessions(),journal.activeDays()),43,355,19,305,TEXT,true,false);fit(c,t("stats"),43,387,13,305,skin.accent2,true,false);
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
        settingsRow(c,"reduce_motion",reduceMotion,331,()->{reduceMotion=!reduceMotion||!android.animation.ValueAnimator.areAnimatorsEnabled();prefs.edit().putBoolean("reduce_motion",reduceMotion).apply();});
        text(c,t("language"),26,423,16,TEXT,true);
        for(int i=0;i<Strings.LANGUAGE_CODES.length;i++){final String code=Strings.LANGUAGE_CODES[i];float x=24+(i%3)*116,y=443+(i/3)*43;boolean on=store.language().equals(code);round(c,x,y,110,36,12,on?alpha(skin.accent,110):skin.card);String name=i==0?t("system"):Strings.LANGUAGE_NAMES[i];fit(c,name,x+55,y+23,12,100,on?TEXT:skin.muted,on,true);hit(x,y,110,36,name,()->{store.setLanguage(code);invalidate();});}
        quietButton(c,n("learn_replay"),24,590,342,()->openTutorial(false));
        quietButton(c,t("local_journal"),24,648,165,()->go("journal"));
        quietButton(c,t("account"),201,648,165,()->getContext().startActivity(new android.content.Intent(getContext(),com.androidhit.lumadrift.identity.AccountActivity.class)));
        quietButton(c,t("credits"),24,706,342,()->showLegal());
        fit(c,t(BuildConfig.MODE_SANDBOX?"sandbox_privacy_local":"production_privacy_local"),195,773,10,338,skin.muted,false,true);

    }
    private void settingsRow(Canvas c,String key,boolean on,float y,Runnable a){round(c,24,y,342,56,17,skin.card);text(c,t(key),43,y+34,15,TEXT,true);round(c,302,y+14,44,28,14,on?skin.accent2:alpha(skin.muted,55));p.setColor(on?INK:WHITE);c.drawCircle(on?332:316,y+28,10,p);hit(24,y,342,56,t(key)+": "+t(on?"on":"off"),a);}
    /** Exact search runs on an isolated snapshot, never in the animation or input thread. */
    private void ensureGuidance(){
        if(board==null)return;
        int remaining=moveBudget==null||moveBudget.isUnlimited()?-1:moveBudget.remaining();int[] history=board.history();int historyHash=Arrays.hashCode(history);
        if(guidanceBoard==board&&guidanceMask==board.remainingMask()&&guidanceGates==board.gateMask()&&guidanceBudget==remaining&&guidanceHistory==historyHash&&Arrays.equals(history,guidanceSavedHistory))return;
        guidanceBoard=board;guidanceMask=board.remainingMask();guidanceGates=board.gateMask();guidanceBudget=remaining;guidanceHistory=historyHash;guidanceSavedHistory=history;
        guidance=null;guidancePlan=new int[0];guidanceSafe.clear();guidancePending=true;
        final long token=++guidanceGeneration;final Board target=board;final Level level=board.level;
        if(guideWorker.isShutdown())return;
        guideWorker.execute(()->{
            if(token!=guidanceGeneration||Thread.currentThread().isInterrupted())return;
            Board snapshot=new Board(level);if(!snapshot.restoreHistory(history))return;
            Board.Guidance result=snapshot.guidance(remaining);int[] plan=snapshot.solve();HashSet<Integer> safe=new HashSet<>();
            if(result.canUseHint()){
                // V4 paths remain recoverable; Focus highlights the efficient next action, not every legal tap.
                if(level.progressionVersion>=4){if(plan!=null&&plan.length>0)safe.add(plan[0]);}
                else for(Tile tile:snapshot.legalMoves())if(snapshot.isSafeMove(tile.id))safe.add(tile.id);
            }
            if(token!=guidanceGeneration||Thread.currentThread().isInterrupted())return;
            post(()->{if(token!=guidanceGeneration||board!=target)return;guidance=result;guidancePlan=plan==null?new int[0]:plan;guidanceSafe.clear();guidanceSafe.addAll(safe);guidancePending=false;if(!pendingContinuation.isEmpty())applyPendingContinuation();invalidate();sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);});
        });
    }
    private int continuationNeed(){ensureGuidance();return guidance!=null&&guidance.actionsToFinish>0?guidance.actionsToFinish:-1;}
    private void showDeadEnd(){
        ensureGuidance();if(guidance==null){notice(n("thinking"));return;}
        android.app.AlertDialog.Builder dialog=new android.app.AlertDialog.Builder(getContext()).setTitle(v("dead_end")).setMessage(n("recover_note")).setNeutralButton(t("restart"),(d,w)->restartRun()).setNegativeButton(t("close"),null);
        if(guidance.undoSteps>0){int steps=guidance.undoSteps;dialog.setPositiveButton(n("recover"),(d,w)->{for(int i=0;i<steps;i++)undoMove();});}dialog.show();
    }
    private void drawDeadEnd(Canvas c){
        scrim(c);float y=H/2-205;sweet.surface(c,new RectF(24,y,366,y+410),SweetUi.CREAM,true);
        fit(c,v("dead_end"),195,y+47,23,305,TEXT,true,true);wrap(c,n("recover_note"),195,y+86,290,14,skin.muted,true,21);
        button(c,n("recover"),44,y+214,302,50,PEACH,()->{int steps=guidance==null?1:Math.max(1,guidance.undoSteps);for(int i=0;i<steps;i++)undoMove();});
        quietButton(c,t("restart_free"),44,y+279,302,()->restartRun());quietButton(c,t("home"),44,y+342,302,()->go("home"));
    }
    private void requestAction(int action){
        if(board==null||paused||help||won||outOfMoves()||powerSaveBlocked||flowRemaining>0)return;
        Board.ActionPreview preview=board.previewAction(action);
        if(preview.legal&&(Board.isGateAction(action)||board.level.tile(action).isSwitch())){
            pendingAction=action;actionPreview=preview;selected=Board.isGateAction(action)?-1:action;actionGeneration++;invalidate();announceForAccessibility(n("preview"));
        }else movePiece(action);
    }
    private void drawActionPreview(Canvas c){
        if(pendingAction<0||actionPreview==null)return;
        // Modal coordinates follow the visible window, independent of board/page scroll.
        c.save();c.translate(0,contentScroll);float oldScroll=drawingScroll;drawingScroll=0;hits.clear();
        round(c,0,0,390,viewportH,0,0x96463252);
        float height=Math.min(330,viewportH-16),y=Math.max(8,(viewportH-height)/2);int action=pendingAction;Board.ActionPreview preview=actionPreview;
        sweet.surface(c,new RectF(24,y,366,y+height),SweetUi.CREAM,true);fit(c,n("preview"),195,y+29,20,304,TEXT,true,true);
        StringBuilder description=new StringBuilder(n("preview"));int i=0;
        for(Direction direction:Direction.values())if(((preview.openedMask|preview.closedMask)&direction.bit)!=0){
            String changed=n((preview.openedMask&direction.bit)!=0?"opens":"closes",t("direction_"+direction.name().toLowerCase(Locale.ROOT)));
            fit(c,changed,44,y+56+i++*22,14,300,TEXT,true,false);description.append(". ").append(changed);
        }
        if(i==0)fit(c,n("choose_action"),195,y+64,14,301,TEXT,false,true);
        if(height>=315)wrap(c,n("preview_note"),44,y+153,302,12,skin.muted,false,17);
        label(39,y+5,312,Math.max(125,height-120),description.toString());
        button(c,Board.isGateAction(action)?n("opening",t("direction_"+Board.gateDirection(action).name().toLowerCase(Locale.ROOT))):n("confirm_move"),44,y+height-112,302,48,PEACH,()->{pendingAction=-1;actionPreview=null;movePiece(action);});
        quietButton(c,n("cancel_preview"),44,y+height-54,302,()->{pendingAction=-1;actionPreview=null;selected=-1;});
        drawingScroll=oldScroll;c.restore();
    }
    private void drawPlay(Canvas c){
        if(board==null){go("home");return;}ensureGuidance();
        round(c,24,18,46,46,16,skin.card);icon(c,"pause",47,41,20,TEXT);hit(24,18,46,46,t("pause"),()->setPaused(true));
        fit(c,mode==0?t("level_number",levelIndex):mode==3?t("remix")+" "+levelIndex:t(mode==1?"daily":mode==2?"weekly":"friend_challenge"),191,41,mode==0?23:17,175,TEXT,true,true);
        fit(c,mode==0||mode==3?worldName(levelIndex):mode==4?friendCode:t("stage_number",eventIndex+1),191,62,11,183,skin.muted,false,true);wallet(c,282,23);
        round(c,24,88,342,77,23,skin.card);dressedMascot(c,62,126,18);
        String coach=board.supportsManualGates()?"":mode==0&&levelIndex<=3?"coach_motion":mode==0&&levelIndex<=5?"coach_order":mode==0&&levelIndex==6?"coach_gate":mode==3&&levelIndex<=3?"coach_dual":"";
        if(!coach.isEmpty()){fit(c,t("free_opening"),99,113,13,216,skin.accent2,true,false);wrap(c,t(coach),99,136,211,11,TEXT,false,15);}
        else{fit(c,n(board.remainingCount()<=3?"nearly_home":"goal",board.remainingCount()),99,118,18,220,TEXT,true,false);fit(c,n("path_progress",board.level.tiles.size()-board.remainingCount(),board.level.tiles.size()),100,143,12,218,skin.muted,false,false);}
        round(c,320,107,32,38,12,alpha(skin.accent,25));icon(c,"help",336,126,17,skin.accent2);hit(310,96,50,59,t("how_to_play"),()->setHelp(true));
        sweet.progress(c,new RectF(101,153,308,159),1f-board.remainingCount()/(float)board.level.tiles.size(),SweetUi.MINT);
        boolean low=moveBudget!=null&&!moveBudget.isUnlimited()&&moveBudget.remaining()<=3;
        int budgetColor=low?0xffff9caf:skin.accent2;
        round(c,24,175,342,44,16,blend(skin.card,budgetColor,.15f));
        String budgetLabel=(moveBudget.isUnlimited()?t("moves_unlimited"):t("moves_left",moveBudget.remaining(),moveBudget.total()));
        fit(c,budgetLabel,195,203,20,318,budgetColor,true,true);label(24,175,342,44,budgetLabel);
        float boardW=300;cell=boardW/board.level.size;bx=(390-boardW)/2;by=249;
        round(c,bx-18,by-18,boardW+36,boardW+36,25,alpha(skin.card,175));stroke(c,new RectF(bx-18,by-18,bx+boardW+18,by+boardW+18),25,alpha(WHITE,16),1);
        for(int row=0;row<board.level.size;row++)for(int col=0;col<board.level.size;col++){
            round(c,bx+col*cell+3,by+row*cell+3,cell-6,cell-6,12,0x48e3d5ef);p.setColor(alpha(WHITE,16));c.drawCircle(bx+(col+.5f)*cell,by+(row+.5f)*cell,1.5f,p);
        }
        drawGates(c,boardW);drawBarriers(c);
        if(selected>=0&&board.isPresent(selected))drawRay(c,board.level.tile(selected));
        for(Tile tile:board.remainingTiles()){
            float x=bx+(tile.col+.5f)*cell,y=by+(tile.row+.5f)*cell;
            if(!reduceMotion&&tile.id==shakeId&&frameTime<shakeUntil)x+=(float)Math.sin((shakeUntil-frameTime)*.06)*4;
            boolean lit=tile.id==coachId||tile.id==hintId&&frameTime<hintUntil||frameTime<visionUntil&&board.canMove(tile.id)||frameTime<focusUntil&&safeHighlights.contains(tile.id);
            drawTile(c,tile,x,y,cell-8,lit);if(tile.id==hintId&&frameTime<hintUntil){float pulse=reduceMotion?1f:.6f+.4f*(float)Math.sin(frameTime*.009);stroke(c,new RectF(x-cell*.47f,y-cell*.47f,x+cell*.47f,y+cell*.47f),13,alpha(GOLD,(int)(150+105*pulse)),3);star(c,x,y-cell*.62f,8+3*pulse,GOLD);}if(board.level.isKey(tile.id)){round(c,x-cell*.43f,y-cell*.43f,20,20,6,GOLD);center(c,"K"+sealedKeyNumber(tile.id),x-cell*.43f+10,y-cell*.43f+14,10,INK,true);}
            if(frameTime<trailUntil)for(int k=0;k<trail.length;k++)if(trail[k]==tile.id){round(c,x-cell*.39f,y-cell*.4f,19,19,7,GOLD);center(c,""+(k+1),x-cell*.39f+9.5f,y-cell*.4f+14,11,INK,true);}
            final int id=tile.id;
            String description=(board.level.isKey(tile.id)?u("sealed_key",sealedKeyNumber(tile.id))+", ":"")+t("piece_description",tile.row+1,tile.col+1,t("direction_"+tile.direction.name().toLowerCase(Locale.ROOT)))+", "+t(board.canMove(id)?"gate_open":"blocked");
            if(frameTime<focusUntil&&safeHighlights.contains(id))description+=", "+t("power_safe");
            if(frameTime<trailUntil)for(int k=0;k<trail.length;k++)if(trail[k]==id)description+=", "+t("power_step",k+1);
            if(tile.switchMask!=0){description+=", "+t("switch")+":";for(Direction target:Direction.values())if((tile.switchMask&target.bit)!=0)description+=" "+t("direction_"+target.name().toLowerCase(Locale.ROOT));}
            hit(bx+tile.col*cell,by+tile.row*cell,cell,cell,description,()->requestAction(id));
        }
        Iterator<Flight> it=flights.iterator();while(it.hasNext()){
            Flight f=it.next();float u=(frameTime-f.at)/(reduceMotion?180f:460f);if(u>=1){it.remove();continue;}float d=reduceMotion?0:(u*u)*330;
            c.saveLayerAlpha(0,0,390,H,(int)((1-u)*255));drawTile(c,f.tile,f.x+f.tile.direction.dc*d,f.y+f.tile.direction.dr*d,cell-8,false);c.restore();
            for(int i=0;i<4;i++){float tail=d-i*12;if(tail>0)star(c,f.x+f.tile.direction.dc*tail,f.y+f.tile.direction.dr*tail,3*(1-u),alpha(f.color,(int)(100*(1-u))));}
        }
        if(!reduceMotion&&frameTime<successUntil){float elapsed=1-(successUntil-frameTime)/600f;int a=(int)(160*(1-elapsed));p.setColor(alpha(skin.accent2,a));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);c.drawCircle(successX,successY,8+elapsed*30,p);p.setStyle(Paint.Style.FILL);for(int k=0;k<6;k++){double angle=k*Math.PI/3;star(c,successX+(float)Math.cos(angle)*(10+elapsed*28),successY+(float)Math.sin(angle)*(10+elapsed*28),3*(1-elapsed),alpha(GOLD,a));}}
        if(guidance!=null&&guidance.status==Board.PositionStatus.DEAD_END&&!paused&&!won&&!outOfMoves()){round(c,30,by+boardW-42,330,38,12,0xffffe6ed);fit(c,v("path_warning"),195,by+boardW-17,12,312,GOLD,true,true);hit(30,by+boardW-42,330,38,v("dead_end"),()->showDeadEnd());}
        float controls=by+boardW+37;
        String feedback=frameTime<toastUntil?toast:hintId>=0&&frameTime<hintUntil?(Board.isGateAction(hintId)?n("hint_gate"):v("hint_pulse")):flowRemaining>0?u("flow_running"):shieldCharges>0?t("shield_left",shieldCharges):gearGrace>0?u("gear_protect",gearGrace):mode==4?t("undo_free"):n("budget_strategy",moveBudget.minimumActions(),moveBudget.mistakeAllowance());
        fit(c,feedback,195,controls+5,13,338,frameTime<toastUntil?GOLD:skin.muted,false,true);
        float yy=controls+22;quietButton(c,"↶  "+t("undo"),24,yy,165,()->undoMove());quietButton(c,"↻  "+t("restart"),201,yy,165,()->{setPaused(true);restartConfirm=true;});
        if(mode==4){quietButton(c,t("copy_code"),24,yy+61,165,()->{android.content.ClipboardManager clipboard=(android.content.ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Luma Drift",friendCode));notice(t("challenge_code")+": "+friendCode);});quietButton(c,t("share_challenge"),201,yy+61,165,()->shareBoard());wrap(c,t("challenge_note"),195,yy+130,330,11,skin.muted,true,16);}
        else{
        for(int i=0;i<Catalog.BOOSTERS.length;i++){Catalog.BoosterItem item=Catalog.BOOSTERS[i];final String id=item.id;float x=24+(i%3)*118,y=yy+57+(i/3)*57;boolean unlocked=store.boosterUnlocked(id);
            boolean active=powerActive(id);round(c,x,y,106,51,15,active?blend(skin.card,skin.accent2,.17f):skin.card);if(active)stroke(c,new RectF(x,y,x+106,y+51),15,skin.accent2,1);if(unlocked)art.power(c,id,x+21,y+21,16,frameTime,reduceMotion);else icon(c,"lock",x+20,y+20,18,skin.muted);fit(c,unlocked?active?u("active"):"×"+store.boosterStock(id):t("power_level",item.unlockLevel),x+70,y+23,11,57,skin.muted,false,true);fit(c,t(item.nameKey),x+53,y+43,11,94,unlocked?TEXT:skin.muted,true,true);hit(x,y,106,51,t(item.nameKey)+(unlocked?": "+(active?u("active"):u("stock",store.boosterStock(id))):", "+t("power_level",item.unlockLevel)),()->useItem(id));
        }
        }
        if(won)hits.clear();
        if(outOfMoves()&&!paused&&!won){ensureGuidance();if(guidance!=null&&guidance.status==Board.PositionStatus.DEAD_END)drawDeadEnd(c);else drawOutOfMoves(c);}
        if(pendingAction>=0&&!paused&&!outOfMoves()&&!won)drawActionPreview(c);
        if(paused)drawPause(c);
        if(won&&frameTime>=winAt)drawWin(c);else if(powerSaveBlocked)drawSaveRetry(c);
    }
    private int directionColor(Direction d){switch(d){case UP:return 0xff91e5cc;case RIGHT:return 0xffffb398;case DOWN:return 0xffceb3f7;default:return 0xffa4cefa;}}
    private void arrow(Canvas c,float x,float y,Direction d,float size,int color){
        c.save();c.translate(x,y);c.rotate(d.ordinal()*90);p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(2,size*.21f));p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);Path q=new Path();q.moveTo(0,size*.7f);q.lineTo(0,-size*.7f);q.moveTo(-size*.53f,-size*.15f);q.lineTo(0,-size*.72f);q.lineTo(size*.53f,-size*.15f);c.drawPath(q,p);p.setStyle(Paint.Style.FILL);c.restore();
    }
    private void drawTile(Canvas c,Tile tile,float x,float y,float size,boolean lit){
        sweet.tile(c,new RectF(x-size/2,y-size/2,x+size/2,y+size/2),tile.direction,directionColor(tile.direction),tile.isSwitch(),false,lit,frameTime,reduceMotion);
        if(tile.isSwitch()){
            int index=0;for(Direction direction:Direction.values())if((tile.switchMask&direction.bit)!=0){float xx=x+size*.32f,yy=y-size*.31f+index++*size*.31f;
                p.setColor(0xfffff9ff);c.drawCircle(xx,yy,size*.17f,p);arrow(c,xx,yy,direction,size*.12f,TEXT);
            }
        }
    }
    private void drawGates(Canvas c,float width){
        for(Direction direction:Direction.values()){
            boolean open=board.isGateOpen(direction);float x=bx+width/2+direction.dc*(width/2+12),y=by+width/2+direction.dr*(width/2+12);
            boolean highlighted=actionPreview!=null&&((actionPreview.openedMask|actionPreview.closedMask)&direction.bit)!=0||hintId==Board.gateAction(direction)&&frameTime<hintUntil||frameTime<focusUntil&&safeHighlights.contains(Board.gateAction(direction));
            sweet.gate(c,x,y,13,direction,open,highlighted,frameTime,reduceMotion);
            if(actionPreview!=null&&((actionPreview.openedMask|actionPreview.closedMask)&direction.bit)!=0){
                int newColor=(actionPreview.openedMask&direction.bit)!=0?0xff327a69:0xffb44469;
                icon(c,(actionPreview.openedMask&direction.bit)!=0?"check":"lock",x+direction.dc*15,y+direction.dr*15,16,newColor);
            }
            String desc=t(open?"gate_open":"gate_closed")+" · "+t("direction_"+direction.name().toLowerCase(Locale.ROOT));
            if(!open&&board.supportsManualGates())desc+=" · "+n("opening",t("direction_"+direction.name().toLowerCase(Locale.ROOT)));
            if(frameTime<trailUntil)for(int step=0;step<trail.length;step++)if(trail[step]==Board.gateAction(direction)){
                round(c,x+9,y-24,20,20,7,GOLD);center(c,""+(step+1),x+19,y-10,12,TEXT,true);desc+=", "+t("power_step",step+1);
            }
            if(hintId==Board.gateAction(direction)&&frameTime<hintUntil)desc+=", "+n("hint_gate");
            final String label=desc;gateHit(x,y,direction,desc,()->{if(!open&&board.supportsManualGates())requestAction(Board.gateAction(direction));else notice(label);});
        }
    }
    private int sealedKeyNumber(int tileId){ArrayList<Integer> keys=new ArrayList<>();for(Barrier barrier:board.level.barriers)if(!keys.contains(barrier.keyTileId))keys.add(barrier.keyTileId);return keys.indexOf(tileId)+1;}
    private void drawBarriers(Canvas c){for(Barrier wall:board.level.barriers){float x=bx+(wall.col+.5f)*cell,y=by+(wall.row+.5f)*cell;boolean active=board.isBarrierActive(wall);if(!active){stroke(c,new RectF(x-cell*.4f,y-cell*.4f,x+cell*.4f,y+cell*.4f),10,alpha(GOLD,25),1);continue;}round(c,x-cell*.44f,y-cell*.44f,cell*.88f,cell*.88f,11,blend(skin.card,GOLD,.16f));stroke(c,new RectF(x-cell*.44f,y-cell*.44f,x+cell*.44f,y+cell*.44f),11,GOLD,1.6f);icon(c,"lock",x,y-3,cell*.36f,GOLD);fit(c,"K"+sealedKeyNumber(wall.keyTileId),x,y+cell*.31f,10,cell*.7f,GOLD,true,true);String caption=u("sealed_wall",sealedKeyNumber(wall.keyTileId));hit(x-cell/2,y-cell/2,cell,cell,caption,()->notice(caption));}}
    private void drawRay(Canvas c,Tile tile){
        float x=bx+(tile.col+.5f)*cell,y=by+(tile.row+.5f)*cell;boolean valid=board.canMove(tile.id);int color=valid?0xff79529e:0xffb9496e;
        float endX=tile.direction.dc==0?x:tile.direction.dc>0?bx+300:bx;
        float endY=tile.direction.dr==0?y:tile.direction.dr>0?by+300:by;
        p.setColor(alpha(color,180));p.setStrokeWidth(3);p.setStyle(Paint.Style.STROKE);p.setPathEffect(new DashPathEffect(new float[]{4,6},frameTime%10));c.drawLine(x,y,endX,endY,p);p.setPathEffect(null);p.setStyle(Paint.Style.FILL);
    }
    private void movePiece(int id){
        if(board==null||won||paused||help||!running||powerSaveBlocked||flowRemaining>0&&!flowExecuting||!Board.isGateAction(id)&&!board.isPresent(id)||outOfMoves())return;
        if(Board.isGateAction(id)&&!board.canPerformAction(id))return;
        actionGeneration++;
        boolean protectedBlock=!board.canPerformAction(id)&&(shieldCharges>0||gearGrace>0);
        if(protectedBlock){if(shieldCharges>0)shieldCharges--;else gearGrace--;}else if(!moveBudget.consumeAction())return;
        Tile tile=board.level.tile(id);Board.MoveResult move=board.performAction(id);selected=-1;
        if(!move.success){moveStreak=0;shakeId=id;shakeUntil=SystemClock.uptimeMillis()+300;sfx(AudioEngine.Sound.BLOCKED);haptic();if(!persistMove())return;if(outOfMoves())exhausted();else notice(t(protectedBlock?"shield_saved":move.reason==Board.BlockReason.CLOSED_GATE?"gate_closed":"blocked"));if(move.reason==Board.BlockReason.LOCKED_BARRIER){Barrier wall=board.blockingBarrier(id);if(wall!=null)notice(u("sealed_wall",sealedKeyNumber(wall.keyTileId)));}return;}
        moveStreak++;successX=tile==null?195:bx+(tile.col+.5f)*cell;successY=tile==null?by+150:by+(tile.row+.5f)*cell;successUntil=SystemClock.uptimeMillis()+600;
        if(tile==null||tile.isSwitch()||board.level.isKey(tile.id))gatePulseUntil=SystemClock.uptimeMillis()+700;
        safeHighlights.clear();trailUntil=focusUntil=0;hintId=-1;
        coachId=mode==0&&levelIndex==1&&board.moves()<2?board.hint():-1;
        if(tile!=null)flights.add(new Flight(tile,SystemClock.uptimeMillis(),bx+(tile.col+.5f)*cell,by+(tile.row+.5f)*cell,directionColor(tile.direction)));
        sfx(tile==null||tile.isSwitch()||board.level.isKey(tile.id)?AudioEngine.Sound.GATE:AudioEngine.Sound.MOVE);haptic();if(!persistMove())return;
        ensureGuidance();if(move.solved)finishLevel();else if(outOfMoves())exhausted();
    }
    private boolean persistMove(){if(saveSession()||saveSession())return true;powerSaveBlocked=true;actionGeneration++;stopTimer();notice(t("commerce_save_error"));return false;}
    private void undoMove(){
        if(board==null||won||paused||help||!running||powerSaveBlocked||flowRemaining>0||!board.canUndo())return;
        if(!prepareAbandon())return;actionGeneration++;
        if(moveBudget.policyVersion()==MoveBudget.REVERSIBLE_POLICY_VERSION){
            if(!board.undo()||!moveBudget.refundAction())return;
        }else{if(!moveBudget.consumeAction()||!board.undo())return;}
        moveStreak=0;undos++;hintId=-1;pendingAction=-1;actionPreview=null;trailUntil=focusUntil=0;safeHighlights.clear();flights.clear();sfx(AudioEngine.Sound.UNDO);
        if(!persistMove())return;ensureGuidance();if(!outOfMoves()&&screen.equals("play")&&activeStarted==0)activeStarted=SystemClock.uptimeMillis();
    }
    private boolean powerActive(String id){long now=effectsPausedAt>0?effectsPausedAt:SystemClock.uptimeMillis();return id.equals("hint")?hintId>=0&&now<hintUntil:id.equals("vision")?now<visionUntil:id.equals("focus")?now<focusUntil:id.equals("trail")?now<trailUntil:id.equals("shield")?shieldCharges>0:flowRemaining>0;}
    private void useItem(String id){
        if(board==null||won||paused||help||!running||mode==4||outOfMoves()||powerSaveBlocked)return;
        Catalog.BoosterItem item=Catalog.booster(id);if(item==null)return;
        ensureGuidance();if(guidance==null){notice(n("thinking"));return;}if(guidance.status==Board.PositionStatus.DEAD_END){showDeadEnd();return;}if(guidance.status==Board.PositionStatus.NEEDS_ACTIONS){notice(v("not_enough_moves"));return;}
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
        ensureGuidance();if(guidance==null){notice(n("thinking"));return;}if(!guidance.canUseHint()){notice(v(guidance.status==Board.PositionStatus.DEAD_END?"path_warning":"not_enough_moves"));return;}
        int next=guidance.hintTileId;if(!id.equals("vision")&&!id.equals("shield")&&next<0){notice(t("no_safe_move"));return;}
        int steps=Math.min(PowerUsePolicy.FLOW_MAX_MOVES,guidancePlan.length);
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
            else if(id.equals("focus")){safeHighlights.clear();safeHighlights.addAll(guidanceSafe);focusUntil=now+PowerUsePolicy.durationMillis(id);}
            else if(id.equals("trail")){int[] solution=guidancePlan;trail=Arrays.copyOf(solution,Math.min(PowerUsePolicy.TRAIL_STEPS,solution.length));trailUntil=now+PowerUsePolicy.durationMillis(id);}
            else if(id.equals("flow")){if(!moveBudget.consumeAction()||!board.performAction(next).success)throw new IllegalStateException("Flow first move failed");flowRemaining=steps-1;hintId=-1;safeHighlights.clear();trailUntil=focusUntil=0;}
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
            ensureGuidance();if(guidancePending){resumeFlow();return;}int safe=guidancePlan.length==0?-1:guidancePlan[0];if(safe<0||outOfMoves()){flowRemaining=0;powerGate.finish("flow",SystemClock.uptimeMillis());saveSession();return;}
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
    private static int[] parseIds(String text,int max){try{if(text.isEmpty())return new int[0];String[] split=text.split(",");if(split.length>max)return new int[0];int[] ids=new int[split.length];for(int i=0;i<ids.length;i++){ids[i]=Integer.parseInt(split[i]);if(ids[i]<0||ids[i]>=68)return new int[0];}return ids;}catch(RuntimeException invalid){return new int[0];}}
    private boolean outOfMoves(){return moveBudget!=null&&moveBudget.isExhausted()&&board!=null&&!board.isSolved();}
    private void exhausted(){actionGeneration++;stopTimer();flights.clear();log("moves_exhausted","mode",""+mode,"level",""+levelIndex,"used",""+moveBudget.used());saveSession();announceForAccessibility(t("moves_exhausted"));invalidate();}
    private Level challengeLevel(ChallengeCode code){return code.resolve();}
    private void resetMoveRun(){
        if(!pendingContinuation.isEmpty())com.androidhit.lumadrift.commerce.CommerceActivity.acknowledgeContinuationReward(getContext(),pendingContinuation);
        moveBudget=MoveBudget.startReversible(mode,board.level);flowRemaining=0;powerGate.cancel();lastPowerRequest="";effectsPausedAt=0;runId=UUID.randomUUID().toString();pendingContinuation=lastAppliedContinuation="";shieldCharges=0;gearGrace=mode==4?0:loadout.blockedGracePerRun();focusUntil=trailUntil=0;trail=new int[0];safeHighlights.clear();
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
        if(!outOfMoves()||!moveBudget.canExtend()||continuationNeed()<1)return;
        if(!pendingContinuation.isEmpty()&&pendingContinuation.startsWith("a:")){
            if(com.androidhit.lumadrift.commerce.CommerceActivity.hasContinuationReward(getContext(),pendingContinuation)){applyPendingContinuation();return;}
            com.androidhit.lumadrift.commerce.CommerceActivity.acknowledgeContinuationReward(getContext(),pendingContinuation);
        }
        pendingContinuation="g:"+runId+":"+(moveBudget.extensions()+1);
        if(!saveSession()){notice(t("commerce_save_error"));return;}
        try{store.spendGoldOnce(pendingContinuation,MoveBudget.GOLD_EXTENSION_COST);if(!store.hasGoldSpent(pendingContinuation)){notice(t("not_enough"));return;}applyPendingContinuation();}catch(IllegalStateException failed){notice(t("commerce_save_error"));}
    }
    private void continueWithAd(){
        if(!outOfMoves()||!moveBudget.canExtend()||continuationNeed()<1)return;
        if(pendingContinuation.startsWith("g:")&&store.hasGoldSpent(pendingContinuation)){applyPendingContinuation();return;}
        pendingContinuation="a:"+runId+":"+(moveBudget.extensions()+1);
        if(!saveSession()){notice(t("commerce_save_error"));return;}
        getContext().startActivity(com.androidhit.lumadrift.commerce.CommerceActivity.continuationIntent(getContext(),pendingContinuation).putExtra(com.androidhit.lumadrift.commerce.CommerceActivity.EXTRA_CONTINUATION_MOVES,moveBudget.extensionActions(continuationNeed())));
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
        MoveBudget before=moveBudget,candidate=moveBudget.copy();if(!candidate.extend(continuationNeed()))return;
        String previous=lastAppliedContinuation;moveBudget=candidate;lastAppliedContinuation=request;
        if(!saveSession()){moveBudget=before;lastAppliedContinuation=previous;notice(t("commerce_save_error"));return;}
        log("moves_extended","source",request.startsWith("g:")?"gold":"rewarded_ad","extra",""+(candidate.total()-before.total()));
        com.androidhit.lumadrift.commerce.CommerceActivity.acknowledgeContinuationReward(getContext(),request);pendingContinuation="";saveSession();
        if(screen.equals("play")&&running&&!paused&&!help)activeStarted=SystemClock.uptimeMillis();notice(t("moves_added",candidate.total()-before.total()));invalidate();
    }
    private void drawOutOfMoves(Canvas c){
        scrim(c);float y=Math.max(24,H/2-290);sweet.surface(c,new RectF(24,y,366,y+585),SweetUi.CREAM,true);
        sweet.mascot(c,195,y+57,29,frameTime,reduceMotion,SweetUi.THINKING);
        fit(c,t("moves_exhausted"),195,y+114,25,300,TEXT,true,true);
        wrap(c,board.canUndo()?n("undo_refund"):n("plan_tip"),195,y+148,291,14,skin.muted,true,21);
        String finalBudget=t("moves_left",moveBudget.remaining(),moveBudget.total());fit(c,finalBudget,195,y+202,15,290,TEXT,true,true);label(44,y+181,302,30,finalBudget);
        if(board.canUndo()&&moveBudget.policyVersion()==MoveBudget.REVERSIBLE_POLICY_VERSION)button(c,n("undo_refund"),44,y+226,302,51,PEACH,()->undoMove());
        else button(c,t("restart_free"),44,y+226,302,51,PEACH,()->restartRun());
        int need=continuationNeed(),extra=moveBudget.extensionActions(need);
        if(moveBudget.canExtend()&&need>0){
            quietButton(c,t("moves_gold",extra,MoveBudget.GOLD_EXTENSION_COST),44,y+295,302,()->continueWithGold());
            quietButton(c,t("moves_ad",extra),44,y+356,302,()->continueWithAd());
        }else fit(c,guidancePending?n("thinking"):t("moves_cap"),195,y+325,13,290,skin.muted,false,true);
        quietButton(c,t("restart_free"),44,y+427,145,()->restartRun());quietButton(c,t("exit"),201,y+427,145,()->{saveSession();go("home");});
        fit(c,t("moves_retries",moveBudget.extensions(),MoveBudget.MAX_EXTENSIONS),195,y+516,12,302,skin.muted,false,true);
        fit(c,t("moves_test_only"),195,y+555,12,300,skin.muted,false,true);
    }
    private String eventPeriod="";
    private void finishLevel(){
        if(won||board==null||!board.isSolved())return;
        stopTimer();resultStars=JourneyExperience.stars(moveBudget);
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
    private void drawSaveRetry(Canvas c){scrim(c);float y=H/2-130;round(c,24,y,342,260,25,skin.card);wrap(c,t("commerce_save_error"),195,y+54,290,17,TEXT,true,25);button(c,t("retry"),44,y+143,302,49,skin.accent2,()->{powerSaveBlocked=!saveSession();if(!powerSaveBlocked){reconcilePowerReservation();if(!powerSaveBlocked){if(board.isSolved())finishLevel();else resumeFlow();}}});}
    private boolean restartConfirm=false;
    private void scrim(Canvas c){round(c,0,0,390,H,0,0xd2101428);hits.clear();}
    private void drawPause(Canvas c){
        scrim(c);float y=H/2-192;round(c,24,y,342,384,29,skin.card);mascot(c,195,y+74,33,character,false);
        center(c,t(restartConfirm?"restart":"paused"),195,y+137,25,TEXT,true);
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
        scrim(c);float y=Math.max(20,H/2-335);
        sweet.surface(c,new RectF(24,y,366,y+670),SweetUi.CREAM,true);
        companionPortrait.draw(c,character,195,y+67,29,AvatarStyles.find(avatarStyle),AvatarStage.Pose.WIN,frameTime,reduceMotion,loadout,wornCosmetics);
        center(c,t("victory"),195,y+138,27,TEXT,true);
        for(int k=0;k<3;k++)star(c,141+k*54,y+174,18,k<resultStars?GOLD:alpha(skin.muted,70));
        fit(c,"+"+reward+" LumaCoins · "+t("xp_earned",xpReward),195,y+224,18,304,TEXT,true,true);
        label(44,y+114,302,130,t("victory")+", "+resultStars+" "+t("stars")+", +"+reward+" LumaCoins");
        int milestone=store.nextLootMilestone();boolean giftReady=milestone>0&&cachedCompleted>=milestone;
        if(giftReady)button(c,q("claim_gift",t("booster_"+store.nextMilestoneLootId())),44,y+265,302,49,GOLD,()->claimJourneyGift());
        else fit(c,milestone==0?e("saga_complete"):n("gift_near",Math.max(0,milestone-cachedCompleted)),195,y+296,13,302,TEXT,true,true);
        Catalog.BoosterItem trial=null;for(Catalog.BoosterItem power:Catalog.BOOSTERS)if(store.powerTrialAvailable(power.id))trial=power;
        if(trial!=null){final String id=trial.id;button(c,q("trial",t(trial.nameKey)),44,y+326,302,49,SweetUi.MINT,()->claimTrial(id));}
        else quietButton(c,q("styles"),44,y+326,302,()->go("styles"));
        int boss=store.nextGuardian();if(store.guardianUnlocked(boss)&&!store.guardianCleared(boss))quietButton(c,q("guardian",boss)+"  →",44,y+389,302,()->openGuardian(boss));
        else fit(c,q("guardian_unlock"),195,y+420,12,302,skin.muted,false,true);
        button(c,(mode==4?t("retry"):t("next"))+"  →",44,y+455,302,53,PEACH,()->{
            if(mode==0&&levelIndex<BalancedCatalog.LEVEL_COUNT)previewLevel(levelIndex+1,0,0);
            else if(mode==3&&levelIndex<BalancedCatalog.REMIX_COUNT)previewLevel(levelIndex+1,3,0);
            else if(mode==1&&eventIndex<2&&eventPeriod.equals(store.dailyId()))previewLevel(1,1,eventIndex+1);
            else if(mode==2&&eventIndex<6&&eventPeriod.equals(store.weeklyId()))previewLevel(1,2,eventIndex+1);
            else if(mode==4)openChallenge(friendCode);else go(mode==0||mode==3?"journey":"events");
        });
        quietButton(c,t("share_result"),44,y+529,302,()->shareBoard());
        quietButton(c,t("journey"),44,y+598,144,()->go("journey"));quietButton(c,v("rewards"),202,y+598,144,()->go("rewards"));
    }
    private void drawHelp(Canvas c){
        scrim(c);float y=Math.max(24,H/2-230);sweet.surface(c,new RectF(24,y,366,y+460),SweetUi.CREAM,true);
        sweet.mascot(c,195,y+67,31,frameTime,reduceMotion,SweetUi.HAPPY);
        fit(c,n("learn"),195,y+137,24,300,TEXT,true,true);
        wrap(c,board!=null&&board.supportsManualGates()?n("plan_tip"):t("tutorial_path"),195,y+180,289,16,TEXT,true,24);
        wrap(c,n("learn_note"),195,y+262,290,14,skin.muted,true,20);
        button(c,n("learn_replay"),44,y+302,302,52,PEACH,()->{setHelp(false);saveSession();openTutorial(false);});
        quietButton(c,t("got_it"),44,y+383,302,()->setHelp(false));
    }
    @Override public boolean onTouchEvent(MotionEvent event){
        float x=(event.getX()-ox)/scale,y=(event.getY()-top)/scale;
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            downX=x;downY=y;scrollAtDown=contentScroll;scrolling=false;pressed=null;gesturePiece=-1;gestureGate=-1;gestureGuardian=false;
            for(int i=hits.size()-1;i>=0;i--)if(hits.get(i).action!=null&&hits.get(i).r.contains(x,y)){pressed=hits.get(i);break;}
            Board target=null;float left=bx,up=by,width=300;
            if(!localSaveBlocked&&screen.equals("play")&&!paused&&!help&&!won&&!outOfMoves()&&pendingAction<0)target=board;
            else if(!localSaveBlocked&&screen.equals("tutorial")&&!tutorial.complete()){target=tutorial.board();left=tutorialBoardLeft;up=tutorialBoardTop;width=tutorialBoardWidth;}
            if(target!=null){float yy=y+contentScroll,unit=width/target.level.size;
                if(x>=left&&x<left+width&&yy>=up&&yy<up+width)for(Tile tile:target.remainingTiles())if(tile.row==(int)((yy-up)/unit)&&tile.col==(int)((x-left)/unit)){gesturePiece=tile.id;selected=tile.id;break;}
            }
            // Touch ownership follows the topmost actual visible hit target, never guessed geometry.
            if(pressed!=null){gestureGate=pressed.gate;if(!localSaveBlocked&&screen.equals("guardian")&&pressed.guardianPiece>=0){gesturePiece=pressed.guardianPiece;gestureGuardian=true;}}
            invalidate();return true;
        }
        if(event.getAction()==MotionEvent.ACTION_MOVE){
            if(gesturePiece<0&&gestureGate<0&&layout!=null&&maximumScroll()>0&&Math.abs(y-downY)>=22&&Math.abs(y-downY)>Math.abs(x-downX)){
                scrolling=true;pressed=null;selected=-1;contentScroll=clampScroll(scrollAtDown+downY-y);invalidate();
            }return true;
        }
        if(event.getAction()==MotionEvent.ACTION_UP){
            Hit target=pressed;pressed=null;int piece=gesturePiece;gesturePiece=-1;selected=-1;float dx=x-downX,dy=y-downY;float distance=(float)Math.hypot(dx,dy);
            if(gestureGate>=0&&!scrolling&&distance<40){Direction gate=Direction.values()[gestureGate];gestureGate=-1;if(screen.equals("tutorial"))practiceGate(gate);else if(board!=null&&!board.isGateOpen(gate))requestAction(Board.gateAction(gate));performClick();}
            else if(gestureGuardian&&piece>=0&&!scrolling){Direction direction=Math.abs(dx)>Math.abs(dy)?dx>0?Direction.RIGHT:Direction.LEFT:dy>0?Direction.DOWN:Direction.UP;GuardianEncounter.Piece selectedPiece=guardian==null?null:guardian.piece(piece);if(distance<22||selectedPiece!=null&&selectedPiece.direction==direction){guardianMove(piece);performClick();}else notice(tt("wrong_direction"));}
            else if(!scrolling&&piece>=0&&distance>=22){
                Direction direction=Math.abs(dx)>Math.abs(dy)?dx>0?Direction.RIGHT:Direction.LEFT:dy>0?Direction.DOWN:Direction.UP;
                Board source=screen.equals("tutorial")?tutorial.board():board;Tile tile=source==null?null:source.level.tile(piece);
                if(tile!=null&&direction==tile.direction){sfx(AudioEngine.Sound.TAP);if(screen.equals("tutorial"))practicePiece(piece,direction);else requestAction(piece);performClick();}
                else notice(tt("wrong_direction"));
            }else if(!scrolling&&target!=null&&target.action!=null&&target.r.contains(x,y)&&distance<22){sfx(AudioEngine.Sound.TAP);target.action.run();performClick();}
            scrolling=false;invalidate();sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);return true;
        }
        if(event.getAction()==MotionEvent.ACTION_CANCEL){pressed=null;selected=-1;gesturePiece=-1;gestureGate=-1;gestureGuardian=false;scrolling=false;invalidate();return true;}return true;
    }
    @Override public boolean performClick(){super.performClick();return true;}
    @Override public boolean onKeyDown(int key,KeyEvent e){if(key==KeyEvent.KEYCODE_DPAD_DOWN||key==KeyEvent.KEYCODE_DPAD_RIGHT){focused=(focused+1)%Math.max(1,hits.size());invalidate();return true;}if(key==KeyEvent.KEYCODE_DPAD_UP||key==KeyEvent.KEYCODE_DPAD_LEFT){focused=(focused-1+hits.size())%Math.max(1,hits.size());invalidate();return true;}if(key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER){if(e.getRepeatCount()==0&&focused>=0&&focused<hits.size()&&hits.get(focused).action!=null)hits.get(focused).action.run();return true;}return super.onKeyDown(key,e);}
    private void accessibilityEvent(int id,int type){AccessibilityEvent event=AccessibilityEvent.obtain(type);event.setPackageName(getContext().getPackageName());event.setClassName("android.widget.Button");event.setSource(this,id);if(id>=0&&id<hits.size())event.setContentDescription(hits.get(id).name);getParent().requestSendAccessibilityEvent(this,event);}
    @Override public boolean onHoverEvent(MotionEvent e){AccessibilityManager am=(AccessibilityManager)getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);if(!am.isTouchExplorationEnabled())return super.onHoverEvent(e);float x=(e.getX()-ox)/scale,y=(e.getY()-top)/scale;int id=-1;for(int i=hits.size()-1;i>=0;i--)if(hits.get(i).r.contains(x,y)){id=i;break;}if(e.getAction()==MotionEvent.ACTION_HOVER_EXIT)id=-1;if(id!=hovered){if(hovered>=0)accessibilityEvent(hovered,AccessibilityEvent.TYPE_VIEW_HOVER_EXIT);hovered=id;if(id>=0)accessibilityEvent(id,AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);}return true;}
    @Override public AccessibilityNodeProvider getAccessibilityNodeProvider(){return new AccessibilityNodeProvider(){
        @Override public AccessibilityNodeInfo createAccessibilityNodeInfo(int id){
            if(id==HOST_VIEW_ID){AccessibilityNodeInfo info=AccessibilityNodeInfo.obtain(LumaView.this);onInitializeAccessibilityNodeInfo(info);info.setContentDescription("Luma Drift · "+screenTitle(screen));if(layout!=null&&maximumScroll()>0){info.setScrollable(true);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);}for(int i=0;i<hits.size();i++)info.addChild(LumaView.this,i);return info;}
            if(id<0||id>=hits.size())return null;Hit h=hits.get(id);AccessibilityNodeInfo info=AccessibilityNodeInfo.obtain();info.setSource(LumaView.this,id);info.setParent(LumaView.this);info.setPackageName(getContext().getPackageName());info.setClassName(h.action==null?"android.widget.TextView":"android.widget.Button");info.setContentDescription(h.name);info.setText(h.name);info.setSelected(h.selected);info.setClickable(h.action!=null);info.setEnabled(true);info.setFocusable(true);info.setVisibleToUser(true);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_ACCESSIBILITY_FOCUS);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS);info.setAccessibilityFocused(focused==id);Rect r=new Rect((int)(ox+h.r.left*scale),(int)(top+h.r.top*scale),(int)(ox+h.r.right*scale),(int)(top+h.r.bottom*scale));info.setBoundsInParent(r);int[] loc=new int[2];getLocationOnScreen(loc);r.offset(loc[0],loc[1]);info.setBoundsInScreen(r);return info;
        }
        @Override public boolean performAction(int id,int action,Bundle args){if(id==HOST_VIEW_ID&&layout!=null&&(action==AccessibilityNodeInfo.ACTION_SCROLL_FORWARD||action==AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)){contentScroll=clampScroll(contentScroll+(action==AccessibilityNodeInfo.ACTION_SCROLL_FORWARD?1:-1)*viewportH*.65f);invalidate();sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SCROLLED);sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);return true;}if(id<0||id>=hits.size())return false;if(action==AccessibilityNodeInfo.ACTION_CLICK){final Hit target=hits.get(id);if(target.action==null)return false;final long token=actionGeneration;post(()->{if(running&&token==actionGeneration){target.action.run();invalidate();sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);}});return true;}if(action==AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS){focused=id;accessibilityEvent(id,AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);invalidate();return true;}if(action==AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS){focused=-1;invalidate();return true;}return false;}
    };}
}
