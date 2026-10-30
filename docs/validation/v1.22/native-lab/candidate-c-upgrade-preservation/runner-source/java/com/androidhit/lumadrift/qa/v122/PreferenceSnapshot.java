package com.androidhit.lumadrift.qa.v122;

import android.app.Instrumentation;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Arrays;

/** SDK-only read of persistent preference bytes; never launches an Activity or writes a preference. */
public abstract class PreferenceSnapshot extends Instrumentation {
    private Bundle arguments;
    protected abstract String expectedTarget();
    public void onCreate(Bundle args){super.onCreate(args);arguments=args==null?new Bundle():args;start();}
    public void onStart(){new Thread(()->{
        Bundle result=new Bundle();boolean passed=false;
        try{
            String target=getTargetContext().getPackageName(),token=arguments.getString("runToken",""),name=arguments.getString("snapshotName","");
            if(!target.equals(expectedTarget())||!token.matches("[a-f0-9-]{36}")||!name.matches("[a-z][a-z0-9_-]{0,60}"))throw new AssertionError("Exact target/token/name required");
            PackageInfo info=getTargetContext().getPackageManager().getPackageInfo(target,0);
            JSONObject files=new JSONObject();File directory=new File(getTargetContext().getDataDir(),"shared_prefs");File[] entries=directory.listFiles();if(!directory.isDirectory()||entries==null)throw new AssertionError("Existing readable preference directory required");if(entries.length==0)throw new AssertionError("Preserved profile must contain persistent preference files");{Arrays.sort(entries,(a,b)->a.getName().compareTo(b.getName()));for(File entry:entries)if(entry.isFile()){
                byte[] bytes=Files.readAllBytes(entry.toPath()),hash=MessageDigest.getInstance("SHA-256").digest(bytes);StringBuilder hex=new StringBuilder();for(byte b:hash)hex.append(String.format("%02x",b&255));files.put(entry.getName(),new JSONObject().put("bytes",bytes.length).put("sha256",hex.toString()));
            }}
            JSONObject report=new JSONObject().put("passed",true).put("package",target).put("version",info.versionName).put("versionCode",info.versionCode).put("runToken",token).put("snapshotName",name).put("preferenceFiles",files).put("scope","Read-only persistent preference file hashes; no Activity, target imports, target methods, reflection, preference writes, grants or clear.");
            File output=new File(getTargetContext().getExternalFilesDir(null),"v122-native-lab-isolation/"+token+"/"+name);if(output.exists()||!output.mkdirs())throw new AssertionError("Fresh output required");try(FileOutputStream stream=new FileOutputStream(new File(output,"report.json"))){stream.write(report.toString(2).getBytes(StandardCharsets.UTF_8));}passed=true;result.putString("summary","Read-only persistent preference hashes recorded");
        }catch(Throwable failure){result.putString("failure",failure.toString());}finally{finish(passed?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);}
    },"luma122-readonly-preference-snapshot").start();}
}
