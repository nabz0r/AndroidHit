package com.androidhit.lumadrift.qa.v117audio;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Framework UI/AudioFlinger and read-only preference observations. No target imports or hooks. */
public final class AudioInstrumentation extends Instrumentation {
    private static final String PACKAGE="com.androidhit.lumadrift";
    private static final String[] SETTINGS={"Settings","Paramètres","Réglages","Einstellungen","Ajustes","Definições","Impostazioni"};
    private static final String[][] ROOTS={{"Adventure","Aventure","Abenteuer","Aventura","Avventura"},{"My home","Ma maison","Mein Zuhause","Mi hogar","A minha casa","La mia casa"},{"Collection","Sammlung","Colección","Coleção","Collezione"},{"Atelier","Taller"}};
    private static final String[] PROFILES={"Profile","Profil","Perfil","Profilo"};
    private static final String[] LEVEL_EXITS={"Leave level","Quitter le niveau","Level verlassen","Salir del nivel","Sair do nível","Esci dal livello"};
    private static final int MUSIC_MIN_BUFFER=220500;
    // Only the currently open puzzle may change naturally through public navigation.
    // Guardian sessions, reduced motion and every other session key remain protected.
    private static final List<String> ACTIVE_SESSION_KEYS=Arrays.asList("active","level","mode","event","event_id","history","challenge_code","elapsed_ms","hints","undos","content_version","budget_policy","run_id","budget_used","budget_extensions","budget_extra","pending_continuation","last_continuation","last_power_request","shield_charges","gear_grace","flow_remaining","hint_id","hint_ms","vision_ms","focus_ms","trail_ms","trail_ids","focus_ids");
    private UiAutomation ui;private Bundle arguments;private File output;private int pid,count,rawIndex;
    private final JSONArray checks=new JSONArray(),actions=new JSONArray(),observations=new JSONArray(),captures=new JSONArray();
    private Boolean originalMusic,originalEffects;private String originalLanguage="";
    private boolean restorationSucceeded;
    private String initialProtected="";
    private boolean protectedStatePreserved;
    private boolean recoveryOnly;
    private int systemMusicVolume=-1;private boolean systemMusicMuted;

    @Override public void onCreate(Bundle args){super.onCreate(args);arguments=args==null?new Bundle():args;start();}
    @Override public void onStart(){new Thread(()->{
        boolean passed=false;Bundle result=new Bundle();String failure="";
        try{
            check(PACKAGE.equals(getTargetContext().getPackageName()),"Exact production target package");
            PackageInfo info=getTargetContext().getPackageManager().getPackageInfo(PACKAGE,0);
            check(arguments.getString("expectedVersion","").equals(info.versionName)&&info.versionCode==Integer.parseInt(arguments.getString("expectedCode","")),"Exact candidate version/code");
            check(arguments.getString("runToken","").matches("[a-f0-9-]{36}"),"Unique host execution token");
            recoveryOnly=Boolean.parseBoolean(arguments.getString("recoveryOnly","false"));
            String label=arguments.getString("label","");check(label.matches("[A-Za-z0-9_-]{1,55}"),"Unique bounded evidence label");
            pid=android.os.Process.myPid();ui=getUiAutomation();AccessibilityServiceInfo service=ui.getServiceInfo();
            service.flags|=AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS|AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;ui.setServiceInfo(service);
            output=new File(getTargetContext().getExternalFilesDir(null),"v117-digital-audio/"+label);if(output.exists()||!output.mkdirs())throw new IllegalStateException("Audio evidence directory must be new");
            AudioManager audio=(AudioManager)getTargetContext().getSystemService(android.content.Context.AUDIO_SERVICE);
            systemMusicVolume=audio.getStreamVolume(AudioManager.STREAM_MUSIC);systemMusicMuted=audio.isStreamMute(AudioManager.STREAM_MUSIC);
            check(systemMusicVolume>0&&!systemMusicMuted,"Android media stream has a positive unmuted digital output setting (no host speaker output)");
            if(!arguments.getString("recoveryLanguage","").isEmpty()){
                launch();settings();top();tap("English");
                setState("Sound effects",Boolean.parseBoolean(arguments.getString("recoveryEffects")));setState("Music",Boolean.parseBoolean(arguments.getString("recoveryMusic")));
                tap(arguments.getString("recoveryLanguage"));
                check(selectedLanguage().equals(arguments.getString("recoveryLanguage")),"Preserved failed-observer settings recovered through actual UI");capture("prior-observer-settings-recovered");
            }
            if(recoveryOnly){
                check(!arguments.getString("recoveryLanguage","").isEmpty(),"Recovery-only route requires documented original visible values");originalLanguage=arguments.getString("recoveryLanguage");originalMusic=Boolean.parseBoolean(arguments.getString("recoveryMusic"));originalEffects=Boolean.parseBoolean(arguments.getString("recoveryEffects"));back();navigate(1);check(currentRoot()==1,"Documented settings recovery returns through visible UI to the home root");capture("prior-observer-home-recovered");initialProtected=protectedState().toString();write("protected-before.json",initialProtected);write("session-before.json",preferences("luma_session").toString(2));restorationSucceeded=true;passed=true;
            }else{
            initialProtected=protectedState().toString();write("protected-before.json",initialProtected);write("session-before.json",preferences("luma_session").toString(2));
            launch();settings();top();originalLanguage=selectedLanguage();check(!originalLanguage.isEmpty(),"Original language observed from selected UI object");
            tap("English");originalEffects=state("Sound effects");originalMusic=state("Music");
            write("initial-state.json",new JSONObject().put("originalLanguage",originalLanguage).put("originalMusic",originalMusic).put("originalEffects",originalEffects).put("processId",pid).put("scope","Visible UI observations before this run changes either audio setting").toString(2));capture("settings-before-en");
            setState("Sound effects",false);setState("Music",true);SystemClock.sleep(1200);
            for(int index=0;index<4;index++){
                String expected=new String[]{"ADVENTURE","HOME","COLLECTION","ATELIER"}[index];
                navigate(index);String log=awaitLoadedScene(expected);Pair pair=advancingMusic("scene-"+index);write("scene-"+index+"-audio-log.txt",log);
                check(true,"Latest actual loaded scene announcement is "+expected);
                observations.put(new JSONObject().put("scene",expected).put("trackId",pair.id).put("frameAdvance",pair.advance));capture("scene-"+expected.toLowerCase(java.util.Locale.ROOT)+"-en");
            }
            openRetainedPuzzle();String puzzleLog=awaitLoadedScene("PUZZLE");Pair puzzle=advancingMusic("scene-puzzle");write("scene-puzzle-audio-log.txt",puzzleLog);
            check(true,"Latest actual loaded scene announcement is PUZZLE");observations.put(new JSONObject().put("scene","PUZZLE").put("trackId",puzzle.id).put("frameAdvance",puzzle.advance));capture("scene-puzzle-u1-en");
            ui.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);SystemClock.sleep(700);
            Snapshot background=snapshot("background");check(background.activeCount()==0,"All target audio tracks inactive after Android HOME");
            launch();SystemClock.sleep(900);awaitLoadedScene("PUZZLE");Pair resumed=advancingMusic("resumed-puzzle");
            observations.put(new JSONObject().put("phase","resume").put("trackId",resumed.id).put("frameAdvance",resumed.advance));
            checkUnplayedPuzzle("Android HOME and foreground resume");capture("scene-puzzle-resumed-en");tap("Pause");tap(LEVEL_EXITS);check(currentRoot()==1,"Visible puzzle Pause and Leave level return to the actual home root");
            settings();top();setState("Music",false);setState("Sound effects",true);SystemClock.sleep(1000);
            Snapshot musicOff=snapshot("music-off-effects-on");check(musicOff.activeMusic()==0,"Music inactive independently while effects remain enabled");
            Sampling effectsOn=sampleTaps("effects-on-music-off");check(effectsOn.shortActivity(),"Actual target short-track activity after visible taps with effects on and music off");check(!effectsOn.musicActivity(),"Music stays inactive during effects-only taps");
            setState("Sound effects",false);SystemClock.sleep(900);Sampling bothOff=sampleTaps("effects-off-music-off");
            check(!bothOff.anyActivity(),"No active or advancing target tracks from the same visible taps with both settings off");
            setState("Music",true);SystemClock.sleep(1000);Pair independent=advancingMusic("music-on-effects-off");
            check(!state("Sound effects")&&state("Music"),"Independent Music On and Sound effects Off remain visible");
            Sampling musicOnly=sampleTaps("effects-off-music-on");check(musicOnly.musicActivity(),"Music advances during effects-disabled taps");check(!musicOnly.shortActivity(),"No target short-track activity during effects-disabled taps");
            observations.put(new JSONObject().put("phase","music-only").put("trackId",independent.id).put("frameAdvance",independent.advance));capture("settings-independent-en");
            passed=true;
            }
        }catch(Throwable error){failure=error.toString();result.putString("failure",failure);try{StringWriter stack=new StringWriter();error.printStackTrace(new PrintWriter(stack));write("failure.txt",stack.toString());capture("failure");}catch(Throwable ignored){}}
        finally{
            try{if(!recoveryOnly)restore();if(!initialProtected.isEmpty()){String after=protectedState().toString();write("protected-after.json",after);write("session-after.json",preferences("luma_session").toString(2));protectedStatePreserved=initialProtected.equals(after);check(protectedStatePreserved,"Complete game/meta and non-puzzle session preferences remain byte-identical after visible restoration");}}catch(Throwable error){passed=false;failure+="; restoration: "+error;}
            try{if(output!=null)write("report.json",new JSONObject().put("passed",passed&&restorationSucceeded&&protectedStatePreserved).put("recoveryOnly",recoveryOnly).put("runToken",arguments.getString("runToken","")).put("label",arguments.getString("label","")).put("checkCount",count).put("checks",checks).put("actions",actions).put("observations",observations).put("captures",captures).put("processId",pid).put("systemMusicVolume",systemMusicVolume).put("systemMusicMuted",systemMusicMuted).put("settingsRestored",restorationSucceeded).put("protectedStatePreserved",protectedStatePreserved).put("activePuzzleSessionChangesAllowed",new JSONArray(ACTIVE_SESSION_KEYS)).put("originalLanguage",originalLanguage).put("originalMusic",originalMusic).put("originalEffects",originalEffects).put("failure",failure).put("scope",recoveryOnly?"Recovery only through actual UI from documented pre-failure original values, with independent comparison to the original protected checkpoint on the host. No five-context playback claim from this recovery phase.":"Actual framework touchscreen actions, AudioFlinger rows/frame counters and public audio logs for all five contexts including a publicly opened already-earned U1 with no piece touch. Android HOME/resume is exercised from that puzzle, followed by independent music/effects toggles and home/original-language restoration. Complete game/meta and every non-active-puzzle session key are observed read-only and compared; original/current active sessions are preserved as evidence and never injected. No app imports, renderer/engine hooks, reflection, preference writes, puzzle move, purchase or provider operation. Emulator has no host audio output: no physical listening, timbre or speaker-quality claim. No real external audio-focus contender was introduced.").toString(2));}catch(Throwable ignored){}
            result.putString("summary",(passed&&restorationSucceeded&&protectedStatePreserved?"PASS ":"FAIL ")+count+" digital audio checks");finish(passed&&restorationSucceeded&&protectedStatePreserved?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);
        }
    },"luma-v117-digital-audio-observer").start();}

    private void restore()throws Exception{
        if(ui==null||originalMusic==null||originalEffects==null||originalLanguage.isEmpty())return;
        settings();top();setState("Sound effects",originalEffects);setState("Music",originalMusic);
        check(state("Sound effects")==originalEffects&&state("Music")==originalMusic,"Original two audio settings restored through actual UI");
        tap(originalLanguage);check(selectedLanguage().equals(originalLanguage),"Original selected language restored through actual UI");
        capture("settings-restored");back();navigate(1);check(currentRoot()==1,"Actual home root restored after original language and independent audio settings");checkUnplayedPuzzle("Final home restoration");restorationSucceeded=true;capture("home-restored");
    }
    private SharedPreferences session(){return getTargetContext().getSharedPreferences("luma_session",0);}
    private JSONObject preferences(String name)throws Exception{return values(getTargetContext().getSharedPreferences(name,0).getAll());}
    private JSONObject values(Map<String,?> source)throws Exception{JSONObject result=new JSONObject();for(Map.Entry<String,?> item:new TreeMap<>(source).entrySet()){Object value=item.getValue();if(value instanceof Set){List<String> list=new ArrayList<>();for(Object entry:(Set<?>)value)list.add(String.valueOf(entry));Collections.sort(list);value=new JSONArray(list);}result.put(item.getKey(),value);}return result;}
    private JSONObject protectedState()throws Exception{Map<String,Object> stableSession=new TreeMap<>();for(Map.Entry<String,?> item:session().getAll().entrySet())if(!ACTIVE_SESSION_KEYS.contains(item.getKey()))stableSession.put(item.getKey(),item.getValue());return new JSONObject().put("game",preferences("luma_drift_save_v1")).put("meta",preferences("luma_drift_meta_v1")).put("non_active_puzzle_session",values(stableSession));}
    private void checkUnplayedPuzzle(String phase)throws Exception{SharedPreferences s=session();check(s.getInt("level",-1)==1&&s.getInt("mode",-1)==0&&s.getString("history","missing").isEmpty()&&s.getInt("budget_used",-1)==0&&s.getInt("hints",-1)==0&&s.getInt("undos",-1)==0,"Publicly opened U1 remains unplayed with zero action, hint or Undo cost: "+phase);}
    private void openRetainedPuzzle()throws Exception{
        check(getTargetContext().getSharedPreferences("luma_drift_save_v1",0).getInt("stars_1",0)==3,"Audio puzzle entry uses an already earned three-star U1");navigate(0);tap("All levels");tap("Explore a level");
        AccessibilityNodeInfo field=null;long end=SystemClock.uptimeMillis()+5000;while(field==null&&SystemClock.uptimeMillis()<end){for(AccessibilityNodeInfo node:nodes()){if(field==null&&node.isVisibleToUser()&&"android.widget.EditText".contentEquals(node.getClassName()))field=AccessibilityNodeInfo.obtain(node);node.recycle();}if(field==null)SystemClock.sleep(70);}
        check(field!=null,"Puzzle audio route exposes the real visible map jump EditText");Bundle text=new Bundle();text.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,"1");check(field.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,text),"Visible map jump field accepts U1 text");
        boolean submitted=Build.VERSION.SDK_INT>=30&&field.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER.getId());field.recycle();if(!submitted){long now=SystemClock.uptimeMillis();check(ui.injectInputEvent(new KeyEvent(now,now,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER,0),true)&&ui.injectInputEvent(new KeyEvent(now,SystemClock.uptimeMillis(),KeyEvent.ACTION_UP,KeyEvent.KEYCODE_ENTER,0),true),"Actual keyboard Enter submits visible U1 map jump");}actions.put(new JSONObject().put("kind",submitted?"Accessibility ACTION_IME_ENTER":"UiAutomation keyboard Enter").put("label","Visible map jump U1"));SystemClock.sleep(350);
        String level="";for(AccessibilityNodeInfo node:nodes()){if(level.isEmpty()&&node.isVisibleToUser()&&node.isClickable()&&name(node).matches("^Level 1(?:,.*)?$"))level=name(node);node.recycle();}check(!level.isEmpty(),"Requested U1 is a genuinely visible map target");tap(level);tap("Play");checkUnplayedPuzzle("Initial actual U1 board");check(has(new String[]{"Pause"},true),"Actual puzzle HUD is visible and interactive before audio sampling");write("session-puzzle-entry.json",preferences("luma_session").toString(2));
    }
    private Pair advancingMusic(String phase)throws Exception{
        Snapshot first=snapshot(phase+"-first");SystemClock.sleep(1050);Snapshot second=snapshot(phase+"-second");
        for(Track a:first.tracks)for(Track b:second.tracks)if(a.music()&&a.active&&b.active&&a.id.equals(b.id)&&b.frames>a.frames){check(true,"Same native music track frame counter advances: "+phase);return new Pair(a.id,b.frames-a.frames);}
        throw new AssertionError("No advancing music track: "+phase);
    }
    private String awaitLoadedScene(String expected)throws Exception{
        long end=SystemClock.uptimeMillis()+6500;String log="";Pattern ready=Pattern.compile("Original ambient ready:.*?; scene=(\\w+)");
        do{log=shell("logcat -d --pid="+pid+" -v brief -s LumaAudio:I");Matcher found=ready.matcher(log);String latest="";while(found.find())latest=found.group(1);if(latest.equals(expected))return log;SystemClock.sleep(150);}while(SystemClock.uptimeMillis()<end);
        write("scene-load-failure-log.txt",log);throw new AssertionError("Latest loaded native audio scene did not become "+expected);
    }
    private Sampling sampleTaps(String phase)throws Exception{
        final Sampling samples=new Sampling();final Throwable[] failure={null};final Rect target=bounds(new String[]{"English"});
        Thread collector=new Thread(()->{try{long end=SystemClock.uptimeMillis()+1900;do{samples.snapshots.add(snapshot(phase));SystemClock.sleep(35);}while(SystemClock.uptimeMillis()<end);}catch(Throwable error){failure[0]=error;}},"luma-audio-flinger-sampling");
        collector.start();SystemClock.sleep(100);for(int i=0;i<4;i++){touch(target,"English (already selected; harmless repeated tap)");SystemClock.sleep(180);}collector.join(6000);
        if(collector.isAlive())throw new AssertionError("AudioFlinger sampler timeout");if(failure[0]!=null)throw new AssertionError("AudioFlinger sampler failed",failure[0]);
        check(samples.snapshots.size()>=3,"At least three real AudioFlinger snapshots for "+phase);
        observations.put(new JSONObject().put("phase",phase).put("snapshots",samples.snapshots.size()).put("shortTrackActivity",samples.shortActivity()).put("musicActivity",samples.musicActivity()));return samples;
    }
    private Snapshot snapshot(String phase)throws Exception{
        String raw=shell("dumpsys media.audio_flinger");String filename=String.format(java.util.Locale.ROOT,"%s-%03d.txt",phase,rawIndex++);write(filename,raw);Snapshot result=new Snapshot();
        Pattern own=Pattern.compile("\\b"+pid+"\\s*/\\s*\\d+\\b"),counters=Pattern.compile("\\b(?:true|false)\\s+([0-9a-fA-F]{8})\\s+(\\d+)\\s+");
        for(String line:raw.split("\\n")){
            String row=line.trim();if(!row.matches("^[SF]\\s+\\d+\\s+(?:yes|no)\\s+.*")||!own.matcher(row).find())continue;
            String[] parts=row.split("\\s+");Matcher values=counters.matcher(row);if(!values.find())throw new AssertionError("Unsupported target AudioFlinger row schema: "+row);
            result.tracks.add(new Track(parts[1],parts[2].equals("yes"),Long.parseLong(values.group(1),16),Long.parseLong(values.group(2))));
        }
        JSONObject parsed=new JSONObject().put("raw",filename).put("elapsedRealtime",SystemClock.elapsedRealtime()).put("tracks",result.json());synchronized(observations){observations.put(parsed);}return result;
    }
    private static final class Track{
        final String id;final boolean active;final long frames,buffer;
        Track(String id,boolean active,long frames,long buffer){this.id=id;this.active=active;this.frames=frames;this.buffer=buffer;}
        boolean music(){return buffer>=MUSIC_MIN_BUFFER;}
        JSONObject json()throws Exception{return new JSONObject().put("id",id).put("active",active).put("frames",frames).put("bufferFrames",buffer).put("kind",music()?"long_static_music":"short_effect");}
    }
    private static final class Snapshot{
        final List<Track> tracks=new ArrayList<>();int activeCount(){int n=0;for(Track t:tracks)if(t.active)n++;return n;}int activeMusic(){int n=0;for(Track t:tracks)if(t.active&&t.music())n++;return n;}
        JSONArray json()throws Exception{JSONArray a=new JSONArray();for(Track t:tracks)a.put(t.json());return a;}
    }
    private static final class Pair{final String id;final long advance;Pair(String id,long advance){this.id=id;this.advance=advance;}}
    private static final class Sampling{
        final List<Snapshot> snapshots=new ArrayList<>();
        boolean activity(boolean music){Map<String,Long> before=new HashMap<>();for(Snapshot s:snapshots)for(Track t:s.tracks)if(t.music()==music){Long previous=before.put(t.id,t.frames);if(t.active||previous!=null&&t.frames>previous)return true;}return false;}
        boolean musicActivity(){return activity(true);}boolean shortActivity(){return activity(false);}boolean anyActivity(){return musicActivity()||shortActivity();}
    }
    private String shell(String command)throws Exception{try(ParcelFileDescriptor pipe=ui.executeShellCommand(command);InputStream in=new ParcelFileDescriptor.AutoCloseInputStream(pipe);ByteArrayOutputStream bytes=new ByteArrayOutputStream()){byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)bytes.write(buffer,0,n);return bytes.toString("UTF-8");}}
    private void launch(){
        // startActivitySync can wait forever for a new Activity when HOME merely
        // resumes the existing task. Launch asynchronously and observe foreground.
        getTargetContext().startActivity(new Intent().setClassName(PACKAGE,"com.androidhit.lumadrift.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        long end=SystemClock.uptimeMillis()+6500;
        do{AccessibilityNodeInfo root=ui.getRootInActiveWindow();boolean own=root!=null&&PACKAGE.contentEquals(root.getPackageName());if(root!=null)root.recycle();if(own){SystemClock.sleep(800);return;}SystemClock.sleep(100);}while(SystemClock.uptimeMillis()<end);
        throw new AssertionError("Production Activity did not reach foreground within bounded launch wait");
    }
    private String selectedLanguage(){String selected="";for(AccessibilityNodeInfo n:nodes()){if(n.isSelected()&&n.isClickable()&&n.isVisibleToUser())selected=name(n);n.recycle();}return selected;}
    private boolean state(String label){for(AccessibilityNodeInfo n:nodes()){String name=name(n);boolean match=n.isVisibleToUser()&&n.isClickable()&&name.startsWith(label+": ");n.recycle();if(match){if(name.equals(label+": On"))return true;if(name.equals(label+": Off"))return false;throw new AssertionError("Unexpected visible toggle state: "+name);}}throw new AssertionError("Missing visible toggle: "+label);}
    private void setState(String label,boolean enabled)throws Exception{top();if(state(label)!=enabled)tap(label+": "+(enabled?"Off":"On"));check(state(label)==enabled,"Visible "+label+" state is "+(enabled?"On":"Off"));}
    private void settings()throws Exception{for(int i=0;i<16;i++){top();if(hasSettingState())return;if(has(SETTINGS,true)){tap(SETTINGS);return;}if(has(LEVEL_EXITS,true)){tap(LEVEL_EXITS);continue;}int root=currentRoot();if(root>0)tap(ROOTS[root-1]);else back();}throw new AssertionError("Cannot reach Settings through actual UI");}
    private boolean hasSettingState(){boolean found=false;for(AccessibilityNodeInfo n:nodes()){String value=name(n);if(n.isVisibleToUser()&&n.isClickable())for(String label:new String[]{"Sound effects","Effets sonores","Soundeffekte","Efectos de sonido","Efeitos sonoros","Effetti sonori"})if(value.startsWith(label+": "))found=true;n.recycle();}return found;}
    private int currentRoot(){if(has(SETTINGS,true))return 0;if(has(new String[]{"Home scene","Pièce de la maison","Zimmeransicht","Vista de la casa","Vista da casa","Vista della casa"}))return 1;if(has(PROFILES,true))return 2;if(has(new String[]{"Walls","Murs","Wände","Paredes","Pareti"},true)&&has(new String[]{"Floor","Sol","Boden","Suelo","Chão","Pavimento"},true))return 3;return -1;}
    private void navigate(int wanted)throws Exception{for(int i=0;i<15;i++){top();int current=currentRoot();if(current==wanted)return;if(current>=0)tap(ROOTS[current+(current<wanted?1:-1)]);else back();}throw new AssertionError("Cannot reach scene root "+wanted);}
    private static String name(AccessibilityNodeInfo node){CharSequence d=node.getContentDescription();return d!=null&&!d.toString().isEmpty()?d.toString():node.getText()==null?"":node.getText().toString();}
    private static boolean matches(String name,String[] labels){for(String label:labels)if(name.equalsIgnoreCase(label)||name.equalsIgnoreCase(label+" →")||name.equalsIgnoreCase(label+"  →"))return true;return false;}
    private void collect(AccessibilityNodeInfo node,List<AccessibilityNodeInfo> list){if(list.size()>600)return;list.add(AccessibilityNodeInfo.obtain(node));for(int i=0;i<node.getChildCount();i++){AccessibilityNodeInfo child=node.getChild(i);if(child!=null){collect(child,list);child.recycle();}}}
    private List<AccessibilityNodeInfo> nodes(){if(Build.VERSION.SDK_INT>=33)ui.clearCache();List<AccessibilityNodeInfo> list=new ArrayList<>();AccessibilityNodeInfo root=ui.getRootInActiveWindow();if(root!=null){collect(root,list);root.recycle();}return list;}
    private boolean has(String[] labels){return has(labels,false);}
    private boolean has(String[] labels,boolean clickable){boolean found=false;for(AccessibilityNodeInfo n:nodes()){if(n.isVisibleToUser()&&(!clickable||n.isClickable())&&matches(name(n),labels))found=true;n.recycle();}return found;}
    private Rect bounds(String[] labels){Rect result=null;for(AccessibilityNodeInfo n:nodes()){if(result==null&&n.isVisibleToUser()&&n.isEnabled()&&n.isClickable()&&matches(name(n),labels)){result=new Rect();n.getBoundsInScreen(result);}n.recycle();}if(result==null||result.width()<=0||result.height()<=0)throw new AssertionError("Missing visible touch object "+java.util.Arrays.toString(labels));return result;}
    private void tap(String... labels)throws Exception{long end=SystemClock.uptimeMillis()+4000;Rect r=null;do{try{r=bounds(labels);}catch(AssertionError unavailable){SystemClock.sleep(75);}}while(r==null&&SystemClock.uptimeMillis()<end);if(r==null)throw new AssertionError("Touch object unavailable "+java.util.Arrays.toString(labels));touch(r,labels[0]);SystemClock.sleep(360);}
    private void touch(Rect rect,String label)throws Exception{long down=SystemClock.uptimeMillis();inject(down,MotionEvent.ACTION_DOWN,rect.exactCenterX(),rect.exactCenterY());SystemClock.sleep(24);inject(down,MotionEvent.ACTION_UP,rect.exactCenterX(),rect.exactCenterY());actions.put(new JSONObject().put("label",label).put("bounds",rect.toShortString()).put("kind","actual touchscreen"));}
    private void inject(long down,int action,float x,float y){MotionEvent event=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,x,y,0);event.setSource(InputDevice.SOURCE_TOUCHSCREEN);boolean sent=ui.injectInputEvent(event,true);event.recycle();if(!sent)throw new AssertionError("Touch injection refused");}
    private void top(){for(int i=0;i<12;i++){AccessibilityNodeInfo root=ui.getRootInActiveWindow();if(root==null)return;boolean sent=root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);root.recycle();if(!sent)return;SystemClock.sleep(90);}}
    private void back(){ui.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);SystemClock.sleep(380);}
    private void capture(String id)throws Exception{Bitmap bitmap=ui.takeScreenshot();if(bitmap==null)throw new AssertionError("No native screenshot");try(OutputStream out=new FileOutputStream(new File(output,id+".png"))){if(!bitmap.compress(Bitmap.CompressFormat.PNG,100,out))throw new AssertionError("PNG write failed");}finally{bitmap.recycle();}captures.put(id);}
    private void write(String name,String value)throws Exception{if(output==null)return;try(FileOutputStream out=new FileOutputStream(new File(output,name))){out.write(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));}}
    private void check(boolean passed,String name)throws Exception{checks.put(new JSONObject().put("passed",passed).put("name",name));count++;if(!passed)throw new AssertionError(name);}
}
