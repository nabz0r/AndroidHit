package com.androidhit.lumadrift.qa;

import android.app.*;
import android.content.*;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.os.*;
import android.view.accessibility.AccessibilityNodeInfo;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

/** Production-only startup/lifecycle/audio observation; never invokes gameplay or provider controls. */
public final class ProductionStartupInstrumentation extends Instrumentation {
    private static final String TARGET="com.androidhit.lumadrift";
    private UiAutomation ui;private File output;private Activity activity;private int checks;private final JSONArray results=new JSONArray(),captures=new JSONArray();
    @Override public void onCreate(Bundle arguments){super.onCreate(arguments);start();}
    @Override public void onStart(){new Thread(()->{Bundle result=new Bundle();boolean passed=false;try{
        if(!TARGET.equals(getTargetContext().getPackageName()))throw new AssertionError("Production startup runner only");
        ui=getUiAutomation();output=new File(getTargetContext().getExternalFilesDir(null),"v17-sdk-ui");if(!output.exists()&&!output.mkdirs())throw new IOException("Output unavailable");
        PackageInfo version=getTargetContext().getPackageManager().getPackageInfo(TARGET,0);check(version.versionCode==8&&"1.7.0-rc1".equals(version.versionName),"Actual production1.7.0-rc1/code8 package loaded");
        Intent launch=new Intent().setClassName(TARGET,"com.androidhit.lumadrift.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);activity=startActivitySync(launch);SystemClock.sleep(1700);
        AccessibilityNodeInfo root=ui.getRootInActiveWindow();check(root!=null&&containsTarget(root),"Production launch exposes its actual native accessible window");if(root!=null)root.recycle();capture("home-production-native");
        int pid=android.os.Process.myPid(),uid=android.os.Process.myUid();boolean music=getTargetContext().getSharedPreferences("luma_drift_save_v1",0).getBoolean("music",true);
        JSONObject audio=new JSONObject().put("pid",pid).put("uid",uid).put("music_enabled_preference",music).put("scope","Actual AudioFlinger observation from the target process; digital frames, not physical speaker audition");
        String first=dump("dumpsys media.audio_flinger");write("audio-flinger-foreground-first.txt",first);JSONArray a=tracks(first,pid);audio.put("foreground_first",a);SystemClock.sleep(1000);
        String second=dump("dumpsys media.audio_flinger");write("audio-flinger-foreground-second.txt",second);JSONArray b=tracks(second,pid);audio.put("foreground_second",b);write("audio-focus.txt",dump("dumpsys audio"));
        if(music){JSONObject active=firstActive(a);check(active!=null,"Production music has an active track owned by its actual process");JSONObject advance=find(b,active.getString("id"));check(advance!=null&&advance.getBoolean("active")&&advance.getLong("frames")>active.getLong("frames"),"The same production audio track advances real mixed frames");audio.put("observed_frame_advance",advance.getLong("frames")-active.getLong("frames"));
            check(ui.performGlobalAction(2),"SDK sends the app to background for lifecycle audio observation");SystemClock.sleep(650);String bg=dump("dumpsys media.audio_flinger");write("audio-flinger-background.txt",bg);JSONArray background=tracks(bg,pid);audio.put("background",background);check(firstActive(background)==null,"Production music pauses after actual background lifecycle");
            Intent resume=new Intent().setClassName(TARGET,"com.androidhit.lumadrift.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);runOnMainSync(()->getTargetContext().startActivity(resume));SystemClock.sleep(1000);String fg=dump("dumpsys media.audio_flinger");write("audio-flinger-resumed.txt",fg);JSONArray resumed=tracks(fg,pid);audio.put("foreground_after_resume",resumed);check(firstActive(resumed)!=null,"Production music resumes after actual foreground lifecycle");
        }else check(true,"Original production profile has music disabled; setting respected and playback not claimed");
        write("audio-runtime.json",audio.toString(2));passed=true;result.putString("summary","PASS production startup/audio "+checks+" checks");
    }catch(Throwable failure){result.putString("failure",failure.toString());try{if(output!=null){capture("production-failure");StringWriter stack=new StringWriter();failure.printStackTrace(new PrintWriter(stack));write("failure.txt",stack.toString());}}catch(Throwable ignored){}}
    finally{try{if(output!=null)write("report.json",new JSONObject().put("passed",passed).put("checkCount",checks).put("checks",results).put("captures",captures).put("package",TARGET).put("sdk",Build.VERSION.SDK_INT).put("abi",Build.SUPPORTED_ABIS[0]).put("scope","Production startup and lifecycle/audio SDK observation only; no piece/menu/account/ad/payment action").toString(2));}catch(Throwable ignored){}finish(passed?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);}},"luma-production-sdk-observation").start();}
    private boolean containsTarget(AccessibilityNodeInfo node){if(TARGET.contentEquals(node.getPackageName()==null?"":node.getPackageName()))return true;for(int i=0;i<node.getChildCount();i++){AccessibilityNodeInfo child=node.getChild(i);if(child==null)continue;boolean target=containsTarget(child);child.recycle();if(target)return true;}return false;}
    private String dump(String command)throws IOException{try(InputStream input=new ParcelFileDescriptor.AutoCloseInputStream(ui.executeShellCommand(command));ByteArrayOutputStream bytes=new ByteArrayOutputStream()){byte[] buffer=new byte[8192];int n;while((n=input.read(buffer))!=-1){if(bytes.size()+n>2000000)throw new IOException("Oversized Android diagnostic");bytes.write(buffer,0,n);}return bytes.toString("UTF-8");}}
    private JSONArray tracks(String dump,int pid)throws Exception{JSONArray array=new JSONArray();Pattern row=Pattern.compile("^\\s*S\\s+(\\d+)\\s+(yes|no)\\s+"+pid+"/\\s*\\d+\\s+.*$");Pattern frames=Pattern.compile("\\b(?:false|true)\\s+([0-9a-fA-F]{8})\\s+\\d+\\s+");for(String line:dump.split("\\n")){Matcher match=row.matcher(line);if(!match.matches())continue;Matcher frame=frames.matcher(line);if(!frame.find())continue;array.put(new JSONObject().put("id",match.group(1)).put("active","yes".equals(match.group(2))).put("frames",Long.parseLong(frame.group(1),16)).put("row",line.trim()));}return array;}
    private JSONObject firstActive(JSONArray rows)throws Exception{for(int i=0;i<rows.length();i++)if(rows.getJSONObject(i).getBoolean("active"))return rows.getJSONObject(i);return null;}
    private JSONObject find(JSONArray rows,String id)throws Exception{for(int i=0;i<rows.length();i++)if(id.equals(rows.getJSONObject(i).getString("id")))return rows.getJSONObject(i);return null;}
    private void capture(String name)throws IOException{Bitmap pixels=ui.takeScreenshot();if(pixels==null)throw new IOException("No native screenshot");try(OutputStream file=new FileOutputStream(new File(output,name+".png"))){pixels.compress(Bitmap.CompressFormat.PNG,100,file);}finally{pixels.recycle();}captures.put(name);}
    private void check(boolean value,String message)throws Exception{checks++;results.put(new JSONObject().put("pass",value).put("message",message));if(!value)throw new AssertionError(message);Bundle status=new Bundle();status.putString("stream","PASS "+message+"\n");sendStatus(0,status);}
    private void write(String name,String text)throws IOException{try(OutputStream file=new FileOutputStream(new File(output,name))){file.write(text.getBytes(StandardCharsets.UTF_8));}}
}
