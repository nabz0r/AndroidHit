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
import java.util.*;

/** Original vector art, adaptive interface and game presentation. AGPL-3.0. */
public final class LumaView extends View {
    private final Paint p = new Paint(3);
    private final Typeface bold = Typeface.create("sans-serif-black", Typeface.NORMAL);
    private final Typeface medium = Typeface.create("sans-serif-medium", Typeface.NORMAL);
    private final Typeface regular = Typeface.create("sans-serif", Typeface.NORMAL);
    private final GameStore store;
    private final SharedPreferences prefs;
    private final AudioEngine audio;
    private final MetaProgress meta;
    private final PlayJournal journal;
    private int cachedStars,cachedCompleted,journeyTab=0,xpReward=0;
    private String friendCode="";
    private long activeElapsed=0,activeStarted=0;
    private final ArrayList<Hit> hits = new ArrayList<>();
    private final ArrayList<Flight> flights = new ArrayList<>();
    private final Random random = new Random(72);
    private Catalog.SkinItem skin;
    private Catalog.CharacterItem character;
    private String screen = "home", toast = "";
    private float scale=1, ox=0, top=0, H=820, bottom=0, bx=37, by=250, cell=55;
    private int safeTop, safeBottom, chapter=0, collectionTab=0, eventTab=0;
    private Board board;
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
        super(c); store=new GameStore(c); prefs=c.getSharedPreferences("luma_session",0);
        meta=new MetaProgress(c);journal=new PlayJournal(c);
        audio=new AudioEngine(c); setFocusable(true); setFocusableInTouchMode(true);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        reduceMotion=prefs.getBoolean("reduce_motion",false);
        syncStyle(); restoreSession();
    }
    private String t(String key,Object... args){return Strings.get(store.resolvedLanguage(),key,args);}
    private void syncStyle(){
        skin=Catalog.skin(store.equippedSkin()); if(skin==null)skin=Catalog.SKINS[0];
        character=Catalog.character(store.equippedCharacter());if(character==null)character=Catalog.CHARACTERS[0];
        audio.setSfxEnabled(store.soundEnabled());audio.setMusicEnabled(store.musicEnabled());cachedStars=store.totalStars();cachedCompleted=store.completedLevels();
    }
    public void setSafeInsets(int a,int b){safeTop=a;safeBottom=b;invalidate();}
    public void resumeApp(){running=true;meta.recordVisit();if(screen.equals("play")&&!paused&&!help&&!won&&activeStarted==0)activeStarted=SystemClock.uptimeMillis();log("session_start","version","1.1.0","language",store.resolvedLanguage());syncStyle();audio.resume();invalidate();}
    public void pauseApp(){actionGeneration++;stopTimer();saveSession();running=false;audio.pause();}
    public void release(){actionGeneration++;running=false;audio.release();}
    public String screenName(){return screen;}
    public void restoreScreen(String name){if(name!=null&&(!name.equals("play")||board!=null))go(name);}
    public void back(){actionGeneration++;
        if(help){setHelp(false);invalidate();return;}
        if(screen.equals("play")){
            if(won){go("journey");return;}
            setPaused(!paused);restartConfirm=false;saveSession();invalidate();return;
        }
        if(!screen.equals("home")){go("home");return;}
        ((Activity)getContext()).moveTaskToBack(true);
    }
    private void sfx(AudioEngine.Sound s){audio.play(s);}
    private void haptic(){if(store.hapticsEnabled())performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);}
    private void go(String s){
        if(s.equals("journey"))chapter=((journeyTab==1?store.remixUnlockedLevel():store.unlockedLevel())-1)/30;
        if(screen.equals("play")&&!s.equals("play")&&board!=null&&!won)log("level_exit","mode",""+mode,"level",""+levelIndex,"remaining",""+board.remainingCount(),"event_index",""+eventIndex,"period",eventPeriod);
        stopTimer();actionGeneration++;screen=s;paused=false;restartConfirm=false;help=false;selected=-1;pressed=null;focused=hovered=-1;hits.clear();syncStyle();
        if(s.equals("play")&&!won&&running)activeStarted=SystemClock.uptimeMillis();
        invalidate();if(Build.VERSION.SDK_INT>=28)setAccessibilityPaneTitle(t(s));sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
    }
    private void log(String event,String... fields){Map<String,String> values=new HashMap<>();for(int i=0;i+1<fields.length;i+=2)values.put(fields[i],fields[i+1]);journal.record(event,values);}
    private void stopTimer(){if(activeStarted>0){activeElapsed+=Math.max(0,SystemClock.uptimeMillis()-activeStarted);activeStarted=0;}}
    private long elapsed(){return activeElapsed+(activeStarted>0?Math.max(0,SystemClock.uptimeMillis()-activeStarted):0);}
    private void setPaused(boolean value){actionGeneration++;if(value)stopTimer();paused=value;if(!value&&running&&!help&&!won&&activeStarted==0)activeStarted=SystemClock.uptimeMillis();}
    private void setHelp(boolean value){actionGeneration++;if(value)stopTimer();help=value;pressed=null;focused=hovered=-1;hits.clear();if(!value&&screen.equals("play")&&running&&!paused&&!won&&activeStarted==0)activeStarted=SystemClock.uptimeMillis();invalidate();}

    private void notice(String s){toast=s;toastUntil=SystemClock.uptimeMillis()+2700;announceForAccessibility(s);invalidate();}
    private void start(int index,int m,int e){
        if(m==3&&(!store.remixAvailable()||index>store.remixUnlockedLevel())){notice(t("remix_unlock"));return;}
        stopTimer();activeElapsed=0;activeStarted=0;friendCode="";
        levelIndex=Math.max(1,Math.min(240,index));mode=m;eventIndex=e;eventPeriod=m==1?store.dailyId():m==2?store.weeklyId():m==3?"remix-v1":"";
        Level l=m==0?LevelCatalog.get(levelIndex):m==3?LevelCatalog.remix(levelIndex):LevelCatalog.event(m==1?store.dailySeed(e):store.weeklySeed(e));
        board=new Board(l);coachId=m==0&&levelIndex==1?board.hint():-1;hintsUsed=undos=xpReward=0;won=paused=false;flights.clear();hintId=-1;visionUntil=0;winAt=0;
        go("play");log("level_start","mode",""+m,"level",""+levelIndex,"event_index",""+eventIndex,"period",eventPeriod);saveSession();
    }
    public void openChallenge(String input){
        String raw=input==null?"":input.trim();
        try{if(raw.startsWith("lumadrift://")){android.net.Uri uri=android.net.Uri.parse(raw);if(!"challenge".equals(uri.getHost())||uri.getPathSegments().size()!=1||uri.getQuery()!=null||uri.getFragment()!=null){notice(t("challenge_invalid"));return;}raw=uri.getLastPathSegment();}}
        catch(RuntimeException invalid){notice(t("challenge_invalid"));return;}
        ChallengeCode code=ChallengeCode.parse(raw);if(code==null){notice(t("challenge_invalid"));return;}
        stopTimer();activeElapsed=activeStarted=0;friendCode=raw;mode=4;eventIndex=0;eventPeriod="";levelIndex=code.kind==ChallengeCode.Kind.EVENT?1:(int)code.value;
        coachId=-1;board=new Board(code.kind==ChallengeCode.Kind.CAMPAIGN?LevelCatalog.get((int)code.value):code.kind==ChallengeCode.Kind.REMIX?LevelCatalog.remix((int)code.value):LevelCatalog.event(code.value));
        hintsUsed=undos=xpReward=0;won=paused=false;flights.clear();hintId=-1;visionUntil=0;winAt=0;go("play");log("challenge_open","kind",code.kind.name());saveSession();
    }
    private String currentCode(){if(mode==4&&!friendCode.isEmpty())return friendCode;if(board==null)return ChallengeCode.encodeCampaign(Math.min(240,store.unlockedLevel()));return mode==0?ChallengeCode.encodeCampaign(levelIndex):mode==3?ChallengeCode.encodeRemix(levelIndex):ChallengeCode.encodeEvent(board.level.seed);}
    private void shareBoard(){
        try{Level level=board==null?LevelCatalog.get(Math.min(240,store.unlockedLevel())):board.level;ShareComposer.shareChallenge((Activity)getContext(),level,currentCode(),store.resolvedLanguage(),won?resultStars:0,character.color);log("share_handoff","kind",mode==4?"friend":""+mode);}
        catch(java.io.IOException|android.content.ActivityNotFoundException failure){notice(t("share_error"));}
    }
    private void importCode(){
        android.widget.EditText input=new android.widget.EditText(getContext());input.setSingleLine(true);input.setHint(t("import_hint"));input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(100)});input.setPadding(30,20,30,20);
        new android.app.AlertDialog.Builder(getContext()).setTitle(t("import_challenge")).setView(input).setPositiveButton(t("play"),(d,w)->openChallenge(input.getText().toString())).setNegativeButton(t("cancel"),(d,w)->{}).show();
    }
    private void continueGame(){if(board!=null&&!won&&!board.isSolved()){go("play");return;}start(Math.min(240,store.unlockedLevel()),0,0);}
    private void saveSession(){
        SharedPreferences.Editor e=prefs.edit();
        if(board==null||won||board.isSolved()){e.putBoolean("active",false).apply();return;}
        StringBuilder history=new StringBuilder();for(int id:board.history()){if(history.length()>0)history.append(',');history.append(id);}
        e.putBoolean("active",true).putInt("level",levelIndex).putInt("mode",mode).putInt("event",eventIndex)
            .putString("event_id",eventPeriod)
            .putString("history",history.toString()).putString("challenge_code",friendCode).putLong("elapsed_ms",elapsed()).putInt("hints",hintsUsed).putInt("undos",undos).apply();
    }
    private void restoreSession(){
        if(!prefs.getBoolean("active",false))return;
        try{
            int m=prefs.getInt("mode",0), e=prefs.getInt("event",0), l=prefs.getInt("level",1);
            String id=prefs.getString("event_id","");
            if(m==1&&!id.equals(store.dailyId())||m==2&&!id.equals(store.weeklyId()))return;
            if(m<0||m>4||m==0&&!store.isLevelUnlocked(l)||m==1&&(e<0||e>2)||m==2&&(e<0||e>6)||m==3&&(!store.remixAvailable()||l<1||l>store.remixUnlockedLevel()))return;
            String savedCode=prefs.getString("challenge_code","");ChallengeCode friend=m==4?ChallengeCode.parse(savedCode):null;if(m==4&&friend==null)return;
            Level lev=m==0?LevelCatalog.get(l):m==3?LevelCatalog.remix(l):m==4?(friend.kind==ChallengeCode.Kind.CAMPAIGN?LevelCatalog.get((int)friend.value):friend.kind==ChallengeCode.Kind.REMIX?LevelCatalog.remix((int)friend.value):LevelCatalog.event(friend.value)):LevelCatalog.event(m==1?store.dailySeed(e):store.weeklySeed(e));
            Board b=new Board(lev);String raw=prefs.getString("history","");String[] parts=raw.isEmpty()?new String[0]:raw.split(",");int[] moves=new int[parts.length];
            for(int i=0;i<moves.length;i++)moves[i]=Integer.parseInt(parts[i]);
            if(b.restoreHistory(moves)&&!b.isSolved()){board=b;mode=m;eventIndex=e;levelIndex=l;eventPeriod=id;friendCode=savedCode;activeElapsed=Math.max(0,prefs.getLong("elapsed_ms",0));hintsUsed=prefs.getInt("hints",0);undos=prefs.getInt("undos",0);}
        }catch(RuntimeException ignored){prefs.edit().putBoolean("active",false).apply();}
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);frameTime=SystemClock.uptimeMillis();
        scale=Math.min(getWidth()/390f,(getHeight()-safeTop-safeBottom)/780f);
        ox=(getWidth()-390*scale)/2f;top=safeTop;H=(getHeight()-safeTop-safeBottom)/scale;
        c.drawColor(skin.bg);c.save();c.translate(ox,top);c.scale(scale,scale);hits.clear();
        background(c);
        switch(screen){
            case "play":drawPlay(c);break;case "journey":drawJourney(c);break;
            case "shop":drawShop(c);break;case "collection":drawCollection(c);break;
            case "events":drawEvents(c);break;case "settings":drawSettings(c);break;case "missions":drawMissions(c);break;case "journal":drawJournal(c);break;
            default:drawHome(c);
        }
        if(!screen.equals("play")&&!screen.equals("settings"))nav(c);
        if(help)drawHelp(c);
        if(frameTime<toastUntil){float y=screen.equals("play")?H-126:H-150;round(c,24,y,342,62,20,0xf52b304b);wrap(c,toast,195,y+25,300,13,WHITE,true,18);}
        if(focused>=0&&focused<hits.size()){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(WHITE);c.drawRoundRect(hits.get(focused).r,15,15,p);p.setStyle(Paint.Style.FILL);}
        c.restore();if(running)postInvalidateDelayed(reduceMotion?250:screen.equals("settings")?180:screen.equals("play")?32:48);
    }
    private void background(Canvas c){
        p.setShader(new RadialGradient(310,70,380,new int[]{alpha(skin.accent,45),alpha(skin.bg,0)},null,Shader.TileMode.CLAMP));c.drawRect(0,0,390,H,p);p.setShader(null);
        p.setShader(new RadialGradient(20,H*.65f,300,new int[]{alpha(skin.accent2,19),0},null,Shader.TileMode.CLAMP));c.drawRect(0,0,390,H,p);p.setShader(null);
        int world=screen.equals("play")&&board!=null?Math.max(0,(levelIndex-1)/30):screen.equals("journey")?chapter:0;
        if(world>0){p.setColor(alpha(skin.accent2,19));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);Path constellation=new Path();for(int i=0;i<7;i++){float x=40+(i*83+world*41)%310,y=100+(i*137+world*59)%490;if(i==0)constellation.moveTo(x,y);else constellation.lineTo(x,y);}c.drawPath(constellation,p);p.setStyle(Paint.Style.FILL);}
        for(int i=0;i<34;i++){float x=(i*79+23)%390,y=(i*137+42)%Math.max(1,(int)H);float pulse=reduceMotion?1f:.65f+.35f*(float)Math.sin(frameTime*.0008+i);p.setColor(alpha(WHITE,(int)(40*pulse)));c.drawCircle(x,y,i%4==0?1.5f:.8f,p);}
    }
    private static int alpha(int c,int a){return (c&0x00ffffff)|((Math.max(0,Math.min(255,a)))<<24);}
    private void round(Canvas c,float x,float y,float w,float h,float r,int color){p.setColor(color);p.setShader(null);p.setStyle(Paint.Style.FILL);c.drawRoundRect(x,y,x+w,y+h,r,r,p);}
    private void stroke(Canvas c,RectF r,float radius,int color,float width){p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(width);c.drawRoundRect(r,radius,radius,p);p.setStyle(Paint.Style.FILL);}
    private void text(Canvas c,String str,float x,float y,float size,int color,boolean heavy){
        p.setShader(null);p.setTypeface(heavy?bold:regular);p.setTextSize(size);p.setColor(color);p.setTextAlign(Paint.Align.LEFT);c.drawText(str,x,y,p);
    }
    private void center(Canvas c,String str,float x,float y,float size,int color,boolean heavy){p.setTypeface(heavy?bold:regular);p.setTextSize(size);text(c,str,x-p.measureText(str)/2,y,size,color,heavy);}
    private void fit(Canvas c,String str,float x,float y,float size,float max,int color,boolean heavy,boolean centered){
        p.setTypeface(heavy?bold:regular);p.setTextSize(size);float sw=p.measureText(str);if(sw>max)size*=max/sw;
        if(centered)center(c,str,x,y,size,color,heavy);else text(c,str,x,y,size,color,heavy);
    }
    private void wrap(Canvas c,String str,float x,float y,float max,float size,int color,boolean centered,float line){
        p.setTypeface(regular);p.setTextSize(size);StringBuilder row=new StringBuilder();float yy=y;
        for(String word:str.split(" ")){if(word.contains("\n")){String[] lines=str.split("\n");for(String l:lines){wrap(c,l,x,yy,max,size,color,centered,line);yy+=line;}return;}
            if(row.length()>0&&p.measureText(row+" "+word)>max){if(centered)center(c,row.toString(),x,yy,size,color,false);else text(c,row.toString(),x,yy,size,color,false);yy+=line;row.setLength(0);}if(row.length()>0)row.append(' ');row.append(word);}
        if(row.length()>0){if(centered)center(c,row.toString(),x,yy,size,color,false);else text(c,row.toString(),x,yy,size,color,false);}
    }
    private void hit(float x,float y,float w,float h,String name,Runnable action){hits.add(new Hit(new RectF(x,y,x+w,y+h),name,action));}
    private void label(float x,float y,float w,float h,String name){hits.add(new Hit(new RectF(x,y,x+w,y+h),name,null));}
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
        p.setShader(new RadialGradient(0,r*.9f,r*.9f,new int[]{alpha(who.color,60),0},null,Shader.TileMode.CLAMP));c.drawOval(-r*1.3f,r*.7f,r*1.3f,r*1.25f,p);p.setShader(null);
        if(who.shape==1){Path ears=new Path();ears.moveTo(-r*.8f,-r*.3f);ears.lineTo(-r*.75f,-r*1.13f);ears.lineTo(-r*.15f,-r*.6f);ears.moveTo(r*.8f,-r*.3f);ears.lineTo(r*.75f,-r*1.13f);ears.lineTo(r*.15f,-r*.6f);p.setColor(who.color);c.drawPath(ears,p);}
        if(who.shape==3){round(c,-r*.7f,-r*1.65f,r*.44f,r*1.25f,r*.23f,who.color);round(c,r*.25f,-r*1.65f,r*.44f,r*1.25f,r*.23f,who.color);}
        if(who.shape==4){Path tail=new Path();tail.moveTo(-r*.5f,r*.3f);tail.lineTo(-r*1.6f,-r*.75f);tail.lineTo(-r*.2f,-r*.65f);p.setColor(alpha(who.color,110));c.drawPath(tail,p);}
        p.setShader(new LinearGradient(-r,-r,r,r,new int[]{blend(who.color,WHITE,.28f),who.color,blend(who.color,INK,.14f)},null,Shader.TileMode.CLAMP));
        if(who.shape==0){Path blob=new Path();for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5;float rr=i%2==0?r*1.12f:r*.81f;float xx=(float)Math.cos(a)*rr,yy=(float)Math.sin(a)*rr;if(i==0)blob.moveTo(xx,yy);else blob.lineTo(xx,yy);}blob.close();c.drawPath(blob,p);c.drawCircle(0,0,r*.83f,p);}
        else if(who.shape==2)c.drawRoundRect(-r,-r*.82f,r,r*.85f,r*.4f,r*.4f,p);
        else c.drawOval(-r,-r,r,r,p);p.setShader(null);
        if(who.shape==2){p.setColor(skin.card);c.drawRoundRect(-r*.75f,-r*.42f,r*.75f,r*.3f,r*.25f,r*.25f,p);p.setColor(who.color);c.drawCircle(0,-r*1.05f,r*.12f,p);}
        if(who.shape==5){for(int i=0;i<3;i++){p.setColor(who.color);c.drawCircle((i-1)*r*.55f,r*.78f,r*.27f,p);}}
        p.setColor(who.shape==2?WHITE:INK);float ey=-r*.12f;boolean blink=!reduceMotion&&(frameTime/120)%43==0;
        if(happy||blink){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(r*.065f);c.drawArc(-r*.48f,ey-r*.12f,-r*.18f,ey+r*.16f,190,160,false,p);c.drawArc(r*.18f,ey-r*.12f,r*.48f,ey+r*.16f,190,160,false,p);p.setStyle(Paint.Style.FILL);}
        else{c.drawOval(-r*.43f,ey-r*.19f,-r*.24f,ey+r*.12f,p);c.drawOval(r*.24f,ey-r*.19f,r*.43f,ey+r*.12f,p);p.setColor(WHITE);c.drawCircle(-r*.31f,ey-r*.1f,r*.04f,p);c.drawCircle(r*.36f,ey-r*.1f,r*.04f,p);}
        p.setColor(alpha(0xffee6f92,100));c.drawOval(-r*.65f,r*.17f,-r*.37f,r*.3f,p);c.drawOval(r*.37f,r*.17f,r*.65f,r*.3f,p);
        p.setColor(INK);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(r*.045f);c.drawArc(-r*.17f,r*.15f,r*.17f,r*.42f,5,170,false,p);p.setStyle(Paint.Style.FILL);
        p.setColor(alpha(WHITE,120));c.drawOval(-r*.55f,-r*.75f,-r*.03f,-r*.52f,p);if(meta.badgeEquipped()&&who.id.equals(character.id))star(c,r*.75f,r*.67f,r*.25f,GOLD);c.restore();
    }
    private static int blend(int a,int b,float k){return Color.rgb((int)(Color.red(a)*(1-k)+Color.red(b)*k),(int)(Color.green(a)*(1-k)+Color.green(b)*k),(int)(Color.blue(a)*(1-k)+Color.blue(b)*k));}
    private void drawHome(Canvas c){
        text(c,"LUMA",24,57,38,WHITE,true);text(c,"DRIFT",25,78,15,skin.accent2,true);
        round(c,321,28,45,45,16,skin.card);icon(c,"settings",343,50,20,WHITE);hit(321,28,45,45,t("settings"),()->go("settings"));
        RectF hero=new RectF(24,103,366,299);p.setShader(new LinearGradient(24,103,366,299,new int[]{blend(skin.card,skin.accent,.25f),skin.card,blend(skin.card,skin.accent2,.14f)},null,Shader.TileMode.CLAMP));c.drawRoundRect(hero,28,28,p);p.setShader(null);stroke(c,hero,28,alpha(WHITE,26),1);
        p.setColor(alpha(skin.accent2,70));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);c.drawOval(192,132,350,273,p);p.setStyle(Paint.Style.FILL);
        text(c,t("free_opening"),45,141,14,skin.accent2,true);wrap(c,t("tagline"),45,177,162,20,WHITE,false,25);
        text(c,"240 + 240",45,266,24,GOLD,true);mascot(c,278,210,48,character,false);tinyTile(c,323,135,Direction.UP,PEACH);star(c,208,264,7,skin.accent2);
        boolean active=board!=null&&!won&&!board.isSolved();int display=active&&mode==0?levelIndex:Math.min(240,store.unlockedLevel());
        String title=active&&mode!=0?t(mode==1?"daily":mode==2?"weekly":mode==3?"remix":"friend_challenge"):t("level_number",display);
        String subtitle=active&&mode!=0?(mode==3?t("level_number",levelIndex):mode==4?friendCode:t("stage_number",eventIndex+1)):t("world_"+((display-1)/30+1));
        round(c,24,318,342,132,25,skin.card);fit(c,title,44,352,23,302,WHITE,true,false);fit(c,subtitle,44,376,12,300,skin.muted,false,false);
        button(c,t("continue")+"  →",44,393,302,43,PEACH,()->continueGame());
        round(c,24,469,342,111,24,skin.card);fit(c,t("rank",meta.rank()),43,497,17,212,WHITE,true,false);
        text(c,t("rank_progress",meta.rankProgress()),44,521,11,skin.muted,false);round(c,44,532,187,6,3,alpha(WHITE,13));round(c,44,532,187*meta.rankProgress()/120f,6,3,skin.accent2);
        fit(c,t("days_played",meta.daysPlayed()),44,561,10,220,skin.muted,false,false);
        label(35,475,200,99,t("rank",meta.rank())+", "+t("xp",meta.xp())+", "+t("rank_progress",meta.rankProgress()));
        int done=0;for(int i=0;i<3;i++)if(meta.missionComplete(i))done++;
        quietButton(c,t("missions"),243,484,107,()->go("missions"));center(c,done+" / 3",296,554,17,GOLD,true);
        quietButton(c,t("share_challenge"),24,601,165,()->shareBoard());quietButton(c,t("import_challenge"),201,601,165,()->importCode());
        if(H>790)fit(c,t("calm_mode"),195,H-111,11,340,skin.muted,false,true);
    }
    private void tinyTile(Canvas c,float x,float y,Direction d,int color){c.save();c.translate(x,y);c.rotate(reduceMotion?0:(float)Math.sin(frameTime*.001)*8);round(c,-19,-19,38,38,12,color);arrow(c,0,0,d,10,INK);c.restore();}
    private void nav(Canvas c){
        float y=H-87;round(c,14,y,362,71,24,0xf5232842);stroke(c,new RectF(14,y,376,y+71),24,alpha(WHITE,18),1);
        String[] ids={"home","journey","events","collection","shop"};
        for(int i=0;i<ids.length;i++){String s=ids[i];float x=50+i*72;boolean on=screen.equals(s);if(on)round(c,x-25,y+7,50,34,13,alpha(skin.accent,43));icon(c,s,x,y+25,20,on?skin.accent2:skin.muted);fit(c,t(s),x,y+56,9,64,on?WHITE:skin.muted,on,true);hit(x-33,y+3,66,64,t(s),()->{go(s);});}
    }
    private void drawJourney(Canvas c){
        boolean remix=journeyTab==1;int complete=0;if(remix){for(int i=1;i<=240;i++)if(store.remixStars(i)>0)complete++;}else complete=cachedCompleted;
        header(c,t("journey"),t(remix?"remix_progress":"journey_progress",complete));
        tabs(c,t("journey"),t("remix"),journeyTab,101,()->{journeyTab=0;chapter=(store.unlockedLevel()-1)/30;},()->{journeyTab=1;chapter=(store.remixUnlockedLevel()-1)/30;});
        if(remix&&!store.remixAvailable()){
            round(c,24,168,342,385,28,skin.card);mascot(c,195,276,52,character,false);icon(c,"lock",195,388,28,skin.accent2);wrap(c,t("remix_unlock"),195,439,286,19,WHITE,true,25);quietButton(c,t("continue"),44,485,302,()->start(Math.min(12,store.unlockedLevel()),0,0));return;
        }
        round(c,24,162,342,82,23,skin.card);text(c,t("chapter_number",chapter+1),44,187,11,skin.accent2,true);fit(c,(remix?"✦ ":"")+t("world_"+(chapter+1)),44,217,23,263,WHITE,true,false);
        icon(c,"back",324,185,15,chapter>0?WHITE:skin.muted);icon(c,"next",344,221,15,chapter<7?WHITE:skin.muted);
        hit(302,162,52,39,t("back"),()->{if(chapter>0)chapter--;});hit(315,202,48,40,t("next"),()->{if(chapter<7)chapter++;});
        float startY=263,step=Math.min(65,(H-374)/6f);int unlocked=remix?store.remixUnlockedLevel():store.unlockedLevel();
        for(int i=0;i<30;i++){final int number=chapter*30+i+1;float x=29+(i%5)*68,y=startY+(i/5)*step;boolean open=number<=unlocked;int stars=remix?store.remixStars(number):store.stars(number);boolean current=number==unlocked;
            if(current){p.setShader(new RadialGradient(x+29,y+24,35,new int[]{alpha(skin.accent,80),0},null,Shader.TileMode.CLAMP));c.drawCircle(x+29,y+24,35,p);p.setShader(null);}
            round(c,x,y,57,47,15,open?(stars>0?blend(skin.card,skin.accent,.18f):current?skin.accent:skin.card):alpha(skin.card,130));if(open)center(c,""+number,x+28.5f,y+30,17,current?INK:WHITE,true);else icon(c,"lock",x+28,y+23,15,alpha(skin.muted,150));
            for(int k=0;k<3;k++)star(c,x+17+k*11,y+52,4.5f,k<stars?GOLD:alpha(skin.muted,45));
            hit(x-2,y-2,61,59,(remix?t("remix")+" ":"")+t("level_number",number)+(open?"":" "+t("locked")),()->{if(open)start(number,remix?3:0,0);else notice(t("unlock_previous"));});
        }
        fit(c,remix?t("coach_dual"):t("calm_mode"),195,H-111,10,340,skin.muted,false,true);
    }
    private void tabs(Canvas c,String a,String b,int tab,float y,Runnable aa,Runnable bb){
        round(c,24,y,342,45,15,skin.card);round(c,28+tab*169,y+4,165,37,12,alpha(skin.accent,65));fit(c,a,110,y+29,14,148,tab==0?WHITE:skin.muted,true,true);fit(c,b,280,y+29,14,148,tab==1?WHITE:skin.muted,true,true);hit(24,y,171,45,a,aa);hit(195,y,171,45,b,bb);
    }
    private void drawCollection(Canvas c){
        header(c,t("collection"),t("cosmetic_only"));
        tabs(c,t("characters"),t("skins"),collectionTab,103,()->collectionTab=0,()->collectionTab=1);
        float cardH=Math.min(168,(H-275)/3f), y0=165;
        for(int i=0;i<6;i++){final int n=i;float x=24+(i%2)*178,y=y0+(i/2)*(cardH+10);boolean chars=collectionTab==0;
            Catalog.CharacterItem ch=Catalog.CHARACTERS[i];Catalog.SkinItem sk=Catalog.SKINS[i];
            String id=chars?ch.id:sk.id;boolean owned=chars?store.ownsCharacter(id):store.ownsSkin(id);boolean equipped=id.equals(chars?store.equippedCharacter():store.equippedSkin());int price=chars?ch.price:sk.price;
            round(c,x,y,164,cardH,23,chars?skin.card:sk.card);if(equipped)stroke(c,new RectF(x,y,x+164,y+cardH),23,skin.accent2,2);
            if(chars)mascot(c,x+82,y+57,31,ch,false);else{
                round(c,x+41,y+22,82,61,14,sk.bg);for(int k=0;k<4;k++){round(c,x+50+(k%2)*32,y+30+(k/2)*23,25,18,5,k%2==0?sk.accent:sk.accent2);}star(c,x+119,y+20,7,sk.accent2);
            }
            fit(c,t(chars?ch.nameKey:sk.nameKey),x+82,y+cardH-49,17,144,WHITE,true,true);
            String label=equipped?t("equipped"):owned?t("equip"):price+"  ◈";
            float yy=y+cardH-36;round(c,x+14,yy,136,27,10,equipped?alpha(skin.accent2,35):alpha(WHITE,14));fit(c,label,x+82,yy+19,12,123,equipped?skin.accent2:owned?WHITE:GOLD,true,true);
            hit(x,y,164,cardH,t(chars?ch.nameKey:sk.nameKey)+", "+label,()->{
                if(chars){if(!store.ownsCharacter(ch.id)&&!store.purchaseCharacter(ch.id)){notice(t("not_enough"));return;}store.equipCharacter(ch.id);}
                else{if(!store.ownsSkin(sk.id)&&!store.purchaseSkin(sk.id)){notice(t("not_enough"));return;}store.equipSkin(sk.id);}
                syncStyle();sfx(owned?AudioEngine.Sound.SELECT:AudioEngine.Sound.PURCHASE);haptic();
            });
        }
    }
    private void drawShop(Canvas c){
        header(c,t("shop"),t("earned_coins"));
        round(c,24,103,342,95,24,skin.card);coin(c,60,147,22);text(c,""+store.coins(),96,146,29,GOLD,true);fit(c,t("coins"),97,170,12,240,skin.muted,false,false);
        float y=216;
        for(Catalog.BoosterItem item:Catalog.BOOSTERS){
            round(c,24,y,342,112,23,skin.card);round(c,39,y+18,51,51,17,alpha(skin.accent,35));icon(c,item.id,64,y+43,27,skin.accent2);
            fit(c,t(item.nameKey)+" ×"+item.amount,104,y+30,18,161,WHITE,true,false);wrap(c,t(item.descriptionKey),104,y+51,165,11,skin.muted,false,14);
            text(c,t("stock")+": "+store.boosterStock(item.id),104,y+94,11,skin.muted,false);
            final String id=item.id;button(c,""+item.price+" ◈",278,y+31,72,45,GOLD,()->{if(store.purchaseBooster(id)){sfx(AudioEngine.Sound.PURCHASE);notice(t("purchase_success"));}else notice(t("not_enough"));});y+=124;
        }
        quietButton(c,t("characters")+" · "+t("skins"),24,y+5,342,()->go("collection"));
        fit(c,t("no_real_money"),195,H-108,11,340,skin.muted,false,true);
    }
    private void drawEvents(Canvas c){
        header(c,t("events"),eventTab==0?store.dailyId():store.weeklyId());
        tabs(c,t("daily"),t("weekly"),eventTab,103,()->eventTab=0,()->eventTab=1);
        boolean daily=eventTab==0;fit(c,t(daily?"daily_desc":"weekly_desc"),25,178,14,340,WHITE,true,false);
        fit(c,t(daily?"refresh_daily":"refresh_weekly"),25,200,11,340,skin.muted,false,false);
        int count=daily?3:7;float y0=219,h=daily?108:47,gap=daily?14:7;
        for(int i=0;i<count;i++){final int idx=i;int stars=daily?store.dailyStars(i):store.weeklyStars(i);boolean open=daily?store.isDailyUnlocked(i):store.isWeeklyUnlocked(i);float y=y0+i*(h+gap);
            round(c,24,y,342,h,19,skin.card);
            if(daily){round(c,40,y+20,60,60,18,alpha(skin.accent,45));icon(c,stars>0?"check":open?"events":"lock",70,y+50,26,open?skin.accent2:skin.muted);fit(c,t("stage_number",i+1),117,y+42,20,177,WHITE,true,false);for(int k=0;k<3;k++)star(c,126+k*19,y+70,7,k<stars?GOLD:alpha(skin.muted,55));icon(c,open?"next":"lock",338,y+52,18,open?WHITE:skin.muted);}
            else{center(c,""+(i+1),49,y+32,18,open?skin.accent2:skin.muted,true);fit(c,t("stage_number",i+1),76,y+31,14,160,WHITE,true,false);for(int k=0;k<3;k++)star(c,267+k*17,y+25,6,k<stars?GOLD:alpha(skin.muted,55));icon(c,open?"next":"lock",340,y+25,15,skin.muted);}
            hit(24,y,342,h,t("stage_number",i+1),()->{if(open)start(1,daily?1:2,idx);else notice(t("unlock_previous"));});
        }
        float bonusY=y0+count*(h+gap)+4;boolean claimed=daily?store.dailyBonusClaimed():store.weeklyBonusClaimed();boolean complete=(daily?store.dailyCompletedCount():store.weeklyCompletedCount())==count;int bonus=daily?100:250;
        String label=claimed?t("claimed"):t("claim")+"  +"+bonus+" ◈";
        if(bonusY+50<H-110)button(c,label,24,bonusY,342,46,complete&&!claimed?GOLD:blend(skin.muted,WHITE,.20f),()->{int got=daily?store.claimDailyCompletionReward():store.claimWeeklyCompletionReward();if(got>0){notice("+"+got+" "+t("coins"));sfx(AudioEngine.Sound.COIN);}else notice(t(claimed?"claimed":"unlock_previous"));});
        if(H>820)fit(c,t("event_offline"),195,H-109,10,340,skin.muted,false,true);
    }
    // Legal notices retain their original English license text; user-facing actions are localized.
    @android.annotation.SuppressLint("SetTextI18n")
    private void showLegal(){
        try{
            String license;try(java.io.InputStream in=getContext().getAssets().open("LICENSE.txt")){java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] buffer=new byte[4096];int count;while((count=in.read(buffer))!=-1)out.write(buffer,0,count);license=out.toString("UTF-8");}
            android.widget.TextView body=new android.widget.TextView(getContext());body.setText(t("privacy_text")+"\n\n© 2026 AndroidHit. Original vector art and synthesized audio.\n\n"+license);body.setTextSize(12);body.setPadding(30,20,30,20);body.setTextColor(WHITE);android.widget.ScrollView scroll=new android.widget.ScrollView(getContext());scroll.addView(body);
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
        settingsRow(c,"local_journal",journal.isEnabled(),163,()->{journal.setEnabled(!journal.isEnabled());if(journal.isEnabled())log("session_start","version","1.1.0","language",store.resolvedLanguage());});
        wrap(c,t("journal_desc"),27,245,336,14,skin.muted,false,21);
        round(c,24,321,342,150,24,skin.card);fit(c,t("journal_sessions",journal.sessions(),journal.activeDays()),43,355,19,305,WHITE,true,false);fit(c,t("stats"),43,387,13,305,skin.accent2,true,false);
        wrap(c,t("journal_returns",journalStatus(journal.returnDay1()),journalStatus(journal.returnDay7())),43,417,300,12,skin.muted,false,19);
        button(c,t("export_journal"),24,493,342,49,skin.accent2,()->{if(journal.eventCount()==0){notice(t("journal_empty"));return;}try{ShareComposer.exportJournal((Activity)getContext(),journal.exportJson(),store.resolvedLanguage());}catch(java.io.IOException|android.content.ActivityNotFoundException error){notice(t("journal_error"));}});
        quietButton(c,t("clear_journal"),24,558,342,()->new android.app.AlertDialog.Builder(getContext()).setTitle(t("clear_journal")).setMessage(t("confirm_clear_journal")).setPositiveButton(t("yes"),(dialog,which)->{ShareComposer.clearJournalExports((Activity)getContext());journal.clear();notice(t("journal_cleared"));}).setNegativeButton(t("cancel"),(dialog,which)->{}).show());
        wrap(c,t("export_note"),195,644,323,12,skin.muted,true,18);
    }
    private void drawSettings(Canvas c){
        header(c,t("settings"),"Luma Drift · 1.1.0");
        quietButton(c,"‹  "+t("back"),24,100,105,()->go("home"));
        settingsRow(c,"sound",store.soundEnabled(),151,()->{store.setSoundEnabled(!store.soundEnabled());syncStyle();});
        settingsRow(c,"music",store.musicEnabled(),211,()->{store.setMusicEnabled(!store.musicEnabled());syncStyle();});
        settingsRow(c,"haptics",store.hapticsEnabled(),271,()->{store.setHapticsEnabled(!store.hapticsEnabled());haptic();});
        settingsRow(c,"reduce_motion",reduceMotion,331,()->{reduceMotion=!reduceMotion;prefs.edit().putBoolean("reduce_motion",reduceMotion).apply();});
        text(c,t("language"),26,423,16,WHITE,true);
        for(int i=0;i<Strings.LANGUAGE_CODES.length;i++){final String code=Strings.LANGUAGE_CODES[i];float x=24+(i%3)*116,y=443+(i/3)*43;boolean on=store.language().equals(code);round(c,x,y,110,36,12,on?alpha(skin.accent,110):skin.card);String name=i==0?t("system"):Strings.LANGUAGE_NAMES[i];fit(c,name,x+55,y+23,12,100,on?WHITE:skin.muted,on,true);hit(x,y,110,36,name,()->{store.setLanguage(code);invalidate();});}
        quietButton(c,t("how_to_play"),24,590,342,()->setHelp(true));
        quietButton(c,t("local_journal"),24,648,342,()->go("journal"));
        quietButton(c,t("credits"),24,706,342,()->showLegal());
        fit(c,t("privacy_local"),195,773,10,338,skin.muted,false,true);

    }
    private void settingsRow(Canvas c,String key,boolean on,float y,Runnable a){round(c,24,y,342,56,17,skin.card);text(c,t(key),43,y+34,15,WHITE,true);round(c,302,y+14,44,28,14,on?skin.accent2:alpha(skin.muted,55));p.setColor(on?INK:WHITE);c.drawCircle(on?332:316,y+28,10,p);hit(24,y,342,56,t(key)+": "+t(on?"on":"off"),a);}
    private void drawPlay(Canvas c){
        if(board==null){go("home");return;}
        round(c,24,18,46,46,16,skin.card);icon(c,"pause",47,41,20,WHITE);hit(24,18,46,46,t("pause"),()->setPaused(true));
        fit(c,mode==0?t("level_number",levelIndex):mode==3?t("remix")+" "+levelIndex:t(mode==1?"daily":mode==2?"weekly":"friend_challenge"),191,41,mode==0?23:17,175,WHITE,true,true);
        fit(c,mode==0||mode==3?t("world_"+((levelIndex-1)/30+1)):mode==4?friendCode:t("stage_number",eventIndex+1),191,62,11,183,skin.muted,false,true);wallet(c,282,23);
        round(c,24,88,342,77,23,skin.card);mascot(c,62,126,22,character,false);
        String coach=mode==0&&levelIndex<=3?"coach_motion":mode==0&&levelIndex<=5?"coach_order":mode==0&&levelIndex==6?"coach_gate":mode==3&&levelIndex<=3?"coach_dual":"";
        if(!coach.isEmpty()){fit(c,t("free_opening"),99,113,13,216,skin.accent2,true,false);wrap(c,t(coach),99,136,211,11,WHITE,false,15);}
        else{fit(c,t("clear_board"),99,118,18,220,WHITE,true,false);fit(c,t("remaining")+"  "+board.remainingCount()+" / "+board.level.tiles.size(),100,143,12,218,skin.muted,false,false);}
        round(c,320,107,32,38,12,alpha(skin.accent,25));icon(c,"help",336,126,17,skin.accent2);hit(310,96,50,59,t("how_to_play"),()->setHelp(true));
        float boardW=300;cell=boardW/board.level.size;bx=(390-boardW)/2;by=Math.max(205,Math.min(240,H*.273f));
        round(c,bx-18,by-18,boardW+36,boardW+36,25,alpha(skin.card,175));stroke(c,new RectF(bx-18,by-18,bx+boardW+18,by+boardW+18),25,alpha(WHITE,16),1);
        for(int row=0;row<board.level.size;row++)for(int col=0;col<board.level.size;col++){
            round(c,bx+col*cell+3,by+row*cell+3,cell-6,cell-6,12,alpha(WHITE,4));p.setColor(alpha(WHITE,16));c.drawCircle(bx+(col+.5f)*cell,by+(row+.5f)*cell,1.5f,p);
        }
        drawGates(c,boardW);
        if(selected>=0&&board.isPresent(selected))drawRay(c,board.level.tile(selected));
        for(Tile tile:board.remainingTiles()){
            float x=bx+(tile.col+.5f)*cell,y=by+(tile.row+.5f)*cell;
            if(!reduceMotion&&tile.id==shakeId&&frameTime<shakeUntil)x+=(float)Math.sin((shakeUntil-frameTime)*.06)*4;
            boolean lit=tile.id==coachId||tile.id==hintId&&frameTime<hintUntil||frameTime<visionUntil&&board.canMove(tile.id);
            drawTile(c,tile,x,y,cell-8,lit);
            final int id=tile.id;
            String description=t("piece_description",tile.row+1,tile.col+1,t("direction_"+tile.direction.name().toLowerCase(Locale.ROOT)))+", "+t(board.canMove(id)?"gate_open":"blocked");
            if(tile.switchMask!=0){description+=", "+t("switch")+":";for(Direction target:Direction.values())if((tile.switchMask&target.bit)!=0)description+=" "+t("direction_"+target.name().toLowerCase(Locale.ROOT));}
            hit(bx+tile.col*cell,by+tile.row*cell,cell,cell,description,()->movePiece(id));
        }
        Iterator<Flight> it=flights.iterator();while(it.hasNext()){
            Flight f=it.next();float u=(frameTime-f.at)/(reduceMotion?180f:460f);if(u>=1){it.remove();continue;}float d=reduceMotion?0:(u*u)*330;
            c.saveLayerAlpha(0,0,390,H,(int)((1-u)*255));drawTile(c,f.tile,f.x+f.tile.direction.dc*d,f.y+f.tile.direction.dr*d,cell-8,false);c.restore();
            for(int i=0;i<4;i++){float tail=d-i*12;if(tail>0)star(c,f.x+f.tile.direction.dc*tail,f.y+f.tile.direction.dr*tail,3*(1-u),alpha(f.color,(int)(100*(1-u))));}
        }
        float controls=by+boardW+37;
        center(c,t("undo_free"),195,controls+5,11,skin.muted,false);
        float yy=controls+22;quietButton(c,"↶  "+t("undo"),24,yy,165,()->undoMove());quietButton(c,"↻  "+t("restart"),201,yy,165,()->{setPaused(true);restartConfirm=true;});
        if(mode==4){quietButton(c,t("copy_code"),24,yy+61,165,()->{android.content.ClipboardManager clipboard=(android.content.ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Luma Drift",friendCode));notice(t("challenge_code")+": "+friendCode);});quietButton(c,t("share_challenge"),201,yy+61,165,()->shareBoard());wrap(c,t("challenge_note"),195,yy+130,330,11,skin.muted,true,16);}
        else{
        String[] items={"hint","vision","flow"};for(int i=0;i<3;i++){final String id=items[i];float x=24+i*118,y=yy+61;
            round(c,x,y,106,70,19,skin.card);icon(c,id,x+28,y+26,23,skin.accent2);round(c,x+67,y+12,26,23,9,alpha(skin.accent,50));center(c,""+store.boosterStock(id),x+80,y+28,12,WHITE,true);fit(c,t(id),x+53,y+56,12,91,WHITE,true,true);hit(x,y,106,70,t("booster_"+id),()->useItem(id));
        }
        }
        if(won)hits.clear();
        if(paused)drawPause(c);
        if(won&&frameTime>=winAt)drawWin(c);
    }
    private int directionColor(Direction d){switch(d){case UP:return skin.accent2;case RIGHT:return PEACH;case DOWN:return skin.accent;default:return 0xff9dc9ff;}}
    private void arrow(Canvas c,float x,float y,Direction d,float size,int color){
        c.save();c.translate(x,y);c.rotate(d.ordinal()*90);p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(2,size*.21f));p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);Path q=new Path();q.moveTo(0,size*.7f);q.lineTo(0,-size*.7f);q.moveTo(-size*.53f,-size*.15f);q.lineTo(0,-size*.72f);q.lineTo(size*.53f,-size*.15f);c.drawPath(q,p);p.setStyle(Paint.Style.FILL);c.restore();
    }
    private void drawTile(Canvas c,Tile tile,float x,float y,float size,boolean lit){
        int color=directionColor(tile.direction);if(lit){p.setShader(new RadialGradient(x,y,size*.8f,new int[]{alpha(GOLD,135),0},null,Shader.TileMode.CLAMP));c.drawCircle(x,y,size*.8f,p);p.setShader(null);}
        round(c,x-size/2,y-size/2+4,size,size,12,blend(color,INK,.55f));
        p.setShader(new LinearGradient(x,y-size/2,x,y+size/2,blend(color,WHITE,.16f),color,Shader.TileMode.CLAMP));c.drawRoundRect(x-size/2,y-size/2,x+size/2,y+size/2,12,12,p);p.setShader(null);
        stroke(c,new RectF(x-size/2+1,y-size/2+1,x+size/2-1,y+size/2-1),11,alpha(WHITE,100),1);
        arrow(c,x,y,tile.direction,size*.32f,INK);
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
            c.restore();if(selected>=0&&board.isPresent(selected)&&(board.level.tile(selected).switchMask&d.bit)!=0)stroke(c,new RectF(x-22,y-22,x+22,y+22),12,GOLD,2);final String desc=t(open?"gate_open":"gate_closed")+" · "+t("direction_"+d.name().toLowerCase(Locale.ROOT));
            hit(x-23,y-23,46,46,desc,()->notice(desc));
        }
    }
    private void drawRay(Canvas c,Tile tile){
        float x=bx+(tile.col+.5f)*cell,y=by+(tile.row+.5f)*cell;boolean valid=board.canMove(tile.id);int color=valid?WHITE:0xffff7e94;
        float endX=tile.direction.dc==0?x:tile.direction.dc>0?bx+300:bx;
        float endY=tile.direction.dr==0?y:tile.direction.dr>0?by+300:by;
        p.setColor(alpha(color,180));p.setStrokeWidth(3);p.setStyle(Paint.Style.STROKE);p.setPathEffect(new DashPathEffect(new float[]{4,6},frameTime%10));c.drawLine(x,y,endX,endY,p);p.setPathEffect(null);p.setStyle(Paint.Style.FILL);
    }
    private void movePiece(int id){
        if(board==null||won||paused||help||!running)return;
        Tile tile=board.level.tile(id);Board.MoveResult move=board.move(id);selected=-1;
        if(!move.success){shakeId=id;shakeUntil=SystemClock.uptimeMillis()+300;sfx(AudioEngine.Sound.BLOCKED);haptic();notice(t(move.reason==Board.BlockReason.CLOSED_GATE?"gate_closed":"blocked"));return;}
        hintId=-1;coachId=mode==0&&levelIndex==1&&board.moves()<2?board.hint():-1;flights.add(new Flight(tile,SystemClock.uptimeMillis(),bx+(tile.col+.5f)*cell,by+(tile.row+.5f)*cell,directionColor(tile.direction)));
        sfx(tile.switchMask==0?AudioEngine.Sound.MOVE:AudioEngine.Sound.GATE);haptic();saveSession();
        if(move.solved)finishLevel();
        else if(board.isStuck())notice(t("no_safe_move"));
    }
    private void undoMove(){if(won||paused||help||!running)return;actionGeneration++;if(board!=null&&board.undo()){undos++;hintId=-1;flights.clear();sfx(AudioEngine.Sound.UNDO);saveSession();}else sfx(AudioEngine.Sound.TAP);}
    private void useItem(String id){
        if(board==null||won||paused||help||!running||mode==4){if(mode==4)notice(t("challenge_note"));return;}
        int next=board.hint();if(!id.equals("vision")&&next<0){notice(t("no_safe_move"));return;}
        if(store.boosterStock(id)<=0){notice(t("no_boosters"));return;}
        if(!store.useBooster(id))return;hintsUsed++;log("power_used","item",id,"mode",""+mode);sfx(AudioEngine.Sound.HINT);
        if(id.equals("hint")){hintId=next;hintUntil=SystemClock.uptimeMillis()+8000;}
        else if(id.equals("vision"))visionUntil=SystemClock.uptimeMillis()+8000;
        else{final Board target=board;final long token=++actionGeneration;for(int i=0;i<3;i++){postDelayed(()->{if(running&&target==board&&token==actionGeneration&&screen.equals("play")&&!paused&&!help&&!won){int safe=target.hint();if(safe>=0)movePiece(safe);}},i*430L);}}
        saveSession();
    }
    private String eventPeriod="";
    private void finishLevel(){
        stopTimer();won=true;resultStars=hintsUsed==0&&undos<=2?3:hintsUsed<=2?2:1;
        if(mode==0)reward=store.completeLevel(levelIndex,resultStars);
        else if(mode==1)reward=store.completeDaily(eventPeriod,eventIndex,resultStars);
        else if(mode==2)reward=store.completeWeekly(eventPeriod,eventIndex,resultStars);
        else if(mode==3)reward=store.completeRemix(levelIndex,resultStars);else reward=0;
        boolean badgeBefore=meta.badgeUnlocked();xpReward=meta.recordWin(mode,mode==1||mode==2?eventIndex:levelIndex,resultStars,eventPeriod);
        if(!badgeBefore&&meta.badgeUnlocked())notice(t("badge_unlocked"));
        log("level_complete","mode",""+mode,"level",""+levelIndex,"event_index",""+eventIndex,"period",eventPeriod,"stars",""+resultStars,"powers",""+hintsUsed,"undos",""+undos,"play_ms",""+activeElapsed);
        cachedStars=store.totalStars();cachedCompleted=store.completedLevels();winAt=SystemClock.uptimeMillis()+(reduceMotion?100:500);sfx(AudioEngine.Sound.WIN);saveSession();
        announceForAccessibility(t("victory")+", "+resultStars+" "+t("stars"));
    }
    private boolean restartConfirm=false;
    private void scrim(Canvas c){round(c,0,0,390,H,0,0xd2101428);hits.clear();}
    private void drawPause(Canvas c){
        scrim(c);float y=H/2-192;round(c,24,y,342,384,29,skin.card);mascot(c,195,y+74,33,character,false);
        center(c,t(restartConfirm?"restart":"paused"),195,y+137,25,WHITE,true);
        if(restartConfirm){wrap(c,t("confirm_restart"),195,y+168,280,13,skin.muted,true,18);
            button(c,t("restart"),44,y+224,302,49,PEACH,()->{actionGeneration++;board.restart();activeElapsed=0;activeStarted=0;undos=0;hintsUsed=0;coachId=mode==0&&levelIndex==1?board.hint():-1;hintId=-1;visionUntil=0;flights.clear();setPaused(false);restartConfirm=false;saveSession();});
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
            if(mode==0&&levelIndex<240)start(levelIndex+1,0,0);
            else if(mode==3&&levelIndex<240)start(levelIndex+1,3,0);
            else if(mode==1&&eventIndex<2&&eventPeriod.equals(store.dailyId()))start(1,1,eventIndex+1);
            else if(mode==2&&eventIndex<6&&eventPeriod.equals(store.weeklyId()))start(1,2,eventIndex+1);
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
        if(event.getAction()==MotionEvent.ACTION_DOWN){downX=x;downY=y;pressed=null;for(int i=hits.size()-1;i>=0;i--)if(hits.get(i).action!=null&&hits.get(i).r.contains(x,y)){pressed=hits.get(i);break;}
            if(screen.equals("play")&&!paused&&!help&&!won&&board!=null){int col=(int)((x-bx)/cell),row=(int)((y-by)/cell);if(x>=bx&&y>=by&&col<board.level.size&&row<board.level.size)for(Tile t:board.remainingTiles())if(t.row==row&&t.col==col)selected=t.id;}invalidate();return true;}
        if(event.getAction()==MotionEvent.ACTION_UP){Hit h=pressed;pressed=null;selected=-1;if(h!=null&&h.action!=null&&h.r.contains(x,y)&&Math.hypot(x-downX,y-downY)<22){sfx(AudioEngine.Sound.TAP);h.action.run();performClick();sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);}invalidate();return true;}
        if(event.getAction()==MotionEvent.ACTION_CANCEL){pressed=null;selected=-1;invalidate();return true;}return true;
    }
    @Override public boolean performClick(){super.performClick();return true;}
    @Override public boolean onKeyDown(int key,KeyEvent e){if(key==KeyEvent.KEYCODE_DPAD_DOWN||key==KeyEvent.KEYCODE_DPAD_RIGHT){focused=(focused+1)%Math.max(1,hits.size());invalidate();return true;}if(key==KeyEvent.KEYCODE_DPAD_UP||key==KeyEvent.KEYCODE_DPAD_LEFT){focused=(focused-1+hits.size())%Math.max(1,hits.size());invalidate();return true;}if(key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER){if(e.getRepeatCount()==0&&focused>=0&&focused<hits.size()&&hits.get(focused).action!=null)hits.get(focused).action.run();return true;}return super.onKeyDown(key,e);}
    private void accessibilityEvent(int id,int type){AccessibilityEvent event=AccessibilityEvent.obtain(type);event.setPackageName(getContext().getPackageName());event.setClassName("android.widget.Button");event.setSource(this,id);if(id>=0&&id<hits.size())event.setContentDescription(hits.get(id).name);getParent().requestSendAccessibilityEvent(this,event);}
    @Override public boolean onHoverEvent(MotionEvent e){AccessibilityManager am=(AccessibilityManager)getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);if(!am.isTouchExplorationEnabled())return super.onHoverEvent(e);float x=(e.getX()-ox)/scale,y=(e.getY()-top)/scale;int id=-1;for(int i=hits.size()-1;i>=0;i--)if(hits.get(i).r.contains(x,y)){id=i;break;}if(e.getAction()==MotionEvent.ACTION_HOVER_EXIT)id=-1;if(id!=hovered){if(hovered>=0)accessibilityEvent(hovered,AccessibilityEvent.TYPE_VIEW_HOVER_EXIT);hovered=id;if(id>=0)accessibilityEvent(id,AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);}return true;}
    @Override public AccessibilityNodeProvider getAccessibilityNodeProvider(){return new AccessibilityNodeProvider(){
        @Override public AccessibilityNodeInfo createAccessibilityNodeInfo(int id){
            if(id==HOST_VIEW_ID){AccessibilityNodeInfo info=AccessibilityNodeInfo.obtain(LumaView.this);onInitializeAccessibilityNodeInfo(info);info.setContentDescription("Luma Drift · "+t(screen));for(int i=0;i<hits.size();i++)info.addChild(LumaView.this,i);return info;}
            if(id<0||id>=hits.size())return null;Hit h=hits.get(id);AccessibilityNodeInfo info=AccessibilityNodeInfo.obtain();info.setSource(LumaView.this,id);info.setParent(LumaView.this);info.setPackageName(getContext().getPackageName());info.setClassName(h.action==null?"android.widget.TextView":"android.widget.Button");info.setContentDescription(h.name);info.setText(h.name);info.setClickable(h.action!=null);info.setEnabled(true);info.setFocusable(true);info.setVisibleToUser(true);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_ACCESSIBILITY_FOCUS);info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS);info.setAccessibilityFocused(focused==id);Rect r=new Rect((int)(ox+h.r.left*scale),(int)(top+h.r.top*scale),(int)(ox+h.r.right*scale),(int)(top+h.r.bottom*scale));info.setBoundsInParent(r);int[] loc=new int[2];getLocationOnScreen(loc);r.offset(loc[0],loc[1]);info.setBoundsInScreen(r);return info;
        }
        @Override public boolean performAction(int id,int action,Bundle args){if(id<0||id>=hits.size())return false;if(action==AccessibilityNodeInfo.ACTION_CLICK){final Hit target=hits.get(id);if(target.action==null)return false;final long token=actionGeneration;post(()->{if(running&&token==actionGeneration){target.action.run();invalidate();}});return true;}if(action==AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS){focused=id;accessibilityEvent(id,AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);invalidate();return true;}if(action==AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS){focused=-1;invalidate();return true;}return false;}
    };}
}
