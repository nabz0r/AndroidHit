package com.androidhit.lumadrift.qa.v117audio;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.view.InputDevice;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Framework UI and AudioFlinger observation only. No application imports, hooks or save reads. */
public final class AudioInstrumentation extends Instrumentation {
    private static final String PACKAGE="com.androidhit.lumadrift";
    private static final String[] SETTINGS={"Settings","Paramètres","Réglages","Einstellungen","Ajustes","Definições","Impostazioni"};
    private static final String[][] ROOTS={{"Adventure","Aventure","Abenteuer","Aventura","Avventura"},{"My home","Ma maison","Mein Zuhause","Mi hogar","A minha casa","La mia casa"},{"Collection","Sammlung","Colección","Coleção","Collezione"},{"Atelier","Taller"}};
    private static final String[] PROFILES={"Profile","Profil","Perfil","Profilo"};
    private static final int MUSIC_MIN_BUFFER=220500;
    private UiAutomation ui;private Bundle arguments;private File output;private int pid,count,rawIndex;
    private final JSONArray checks=new JSONArray(),actions=new JSONArray(),observations=new JSONArray(),captures=new JSONArray();
    private Boolean originalMusic,originalEffects;private String originalLanguage="";
    private boolean restorationSucceeded;
    private int systemMusicVolume=-1;private boolean systemMusicMuted;

    @Override public void onCreate(Bundle args){super.onCreate(args);arguments=args==null?new Bundle():args;start();}
    @Override public void onStart(){new Thread(()->{
        boolean passed=false;Bundle result=new Bundle();String failure="";
        try{
            check(PACKAGE.equals(getTargetContext().getPackageName()),"Exact production target package");
            PackageInfo info=getTargetContext().getPackageManager().getPackageInfo(PACKAGE,0);
            check(arguments.getString("expectedVersion","1.17.0-rc4").equals(info.versionName)&&info.versionCode==Integer.parseInt(arguments.getString("expectedCode","22")),"Exact candidate version/code");
            pid=android.os.Process.myPid();ui=getUiAutomation();AccessibilityServiceInfo service=ui.getServiceInfo();
            service.flags|=AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS|AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;ui.setServiceInfo(service);
            output=new File(getTargetContext().getExternalFilesDir(null),"v117-digital-audio");if(!output.exists()&&!output.mkdirs())throw new IllegalStateException("Cannot create observer evidence directory");
            AudioManager audio=(AudioManager)getTargetContext().getSystemService(android.content.Context.AUDIO_SERVICE);
            systemMusicVolume=audio.getStreamVolume(AudioManager.STREAM_MUSIC);systemMusicMuted=audio.isStreamMute(AudioManager.STREAM_MUSIC);
            check(systemMusicVolume>0&&!systemMusicMuted,"Android media stream has a positive unmuted digital output setting (no host speaker output)");
            if(!arguments.getString("recoveryLanguage","").isEmpty()){
                launch();settings();top();tap("English");
                setState("Sound effects",Boolean.parseBoolean(arguments.getString("recoveryEffects")));setState("Music",Boolean.parseBoolean(arguments.getString("recoveryMusic")));
                tap(arguments.getString("recoveryLanguage"));
                check(selectedLanguage().equals(arguments.getString("recoveryLanguage")),"Prior timed-out observer baseline recovered through actual UI");capture("prior-timeout-settings-recovered");
            }
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
            ui.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);SystemClock.sleep(700);
            Snapshot background=snapshot("background");check(background.activeCount()==0,"All target audio tracks inactive after Android HOME");
            launch();SystemClock.sleep(900);Pair resumed=advancingMusic("resumed");
            observations.put(new JSONObject().put("phase","resume").put("trackId",resumed.id).put("frameAdvance",resumed.advance));
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
        }catch(Throwable error){failure=error.toString();result.putString("failure",failure);try{StringWriter stack=new StringWriter();error.printStackTrace(new PrintWriter(stack));write("failure.txt",stack.toString());capture("failure");}catch(Throwable ignored){}}
        finally{
            try{restore();}catch(Throwable error){passed=false;failure+="; restoration: "+error;}
            try{if(output!=null)write("report.json",new JSONObject().put("passed",passed&&restorationSucceeded).put("checkCount",count).put("checks",checks).put("actions",actions).put("observations",observations).put("captures",captures).put("processId",pid).put("systemMusicVolume",systemMusicVolume).put("systemMusicMuted",systemMusicMuted).put("settingsRestored",restorationSucceeded).put("originalLanguage",originalLanguage).put("originalMusic",originalMusic).put("originalEffects",originalEffects).put("failure",failure).put("scope","Actual framework touchscreen actions, AudioFlinger rows/frame counters and existing public audio logs for four places, music/effects independence and Android HOME pause/resume. No app imports, renderer/engine hooks, reflection, preference access, state injection, puzzle move, purchase or provider operation. Emulator has no host audio output: no physical listening, timbre or speaker-quality claim. No real external audio-focus contender was introduced. Historical puzzle audio is covered separately by PCM contracts, not this four-place UI route.").toString(2));}catch(Throwable ignored){}
            result.putString("summary",(passed&&restorationSucceeded?"PASS ":"FAIL ")+count+" digital audio checks");finish(passed&&restorationSucceeded?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);
        }
    },"luma-v117-digital-audio-observer").start();}

    private void restore()throws Exception{
        if(ui==null||originalMusic==null||originalEffects==null||originalLanguage.isEmpty())return;
        settings();top();setState("Sound effects",originalEffects);setState("Music",originalMusic);
        check(state("Sound effects")==originalEffects&&state("Music")==originalMusic,"Original two audio settings restored through actual UI");
        tap(originalLanguage);check(selectedLanguage().equals(originalLanguage),"Original selected language restored through actual UI");
        restorationSucceeded=true;capture("settings-restored");back();
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
    private void settings()throws Exception{for(int i=0;i<16;i++){top();if(hasSettingState())return;if(has(SETTINGS,true)){tap(SETTINGS);return;}int root=currentRoot();if(root>0)tap(ROOTS[root-1]);else back();}throw new AssertionError("Cannot reach Settings through actual UI");}
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
