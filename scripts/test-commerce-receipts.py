#!/usr/bin/env python3
"""JVM storage-failure checks against the built CommerceActivity bytecode.

Run after compileReleaseJavaWithJavac. Minimal runtime-only Context/Activity
adapters avoid an Android runtime; SharedPreferences simulates Android's
memory-before-disk behavior. This does not simulate or certify an ad display.
"""
import argparse
from pathlib import Path
import os
import subprocess
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parents[1]
CONTEXT = r'''
package android.content;
public class Context {
 public static final int MODE_PRIVATE=0;
 public static SharedPreferences testPreferences;
 public Context getApplicationContext(){return this;}
 public SharedPreferences getSharedPreferences(String name,int mode){
  if(!name.equals("luma_ad_continuations_v1"))throw new AssertionError("Unrelated preferences: "+name);
  return testPreferences;
 }
}
'''
ACTIVITY = "package android.app; public class Activity extends android.content.Context {}"
TEST = r'''
import android.content.Context;
import android.content.SharedPreferences;
import com.androidhit.lumadrift.commerce.CommerceActivity;
import java.lang.reflect.*;
import java.util.*;

public final class CommerceReceiptTest {
 static int checks;
 static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 static final class Memory implements InvocationHandler {
  final Map<String,Object> memory=new HashMap<>(),disk=new HashMap<>();boolean fail;int commits;
  SharedPreferences preferences(){return (SharedPreferences)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{SharedPreferences.class},this);}
  void restart(){memory.clear();memory.putAll(disk);}
  public Object invoke(Object proxy,Method method,Object[] args){
   String name=method.getName();
   if(name.equals("getString")){Object value=memory.get(args[0]);if(value==null)return args[1];return (String)value;}
   if(name.equals("edit")){
    Map<String,Object> pending=new HashMap<>();
    return Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{SharedPreferences.Editor.class},(editor,m,a)->{
     if(m.getName().equals("putString")){pending.put((String)a[0],a[1]);return editor;}
     if(m.getName().equals("remove")){pending.put((String)a[0],null);return editor;}
     if(m.getName().equals("commit")){
      commits++;for(Map.Entry<String,Object> entry:pending.entrySet()){if(entry.getValue()==null)memory.remove(entry.getKey());else memory.put(entry.getKey(),entry.getValue());}
      if(fail)return false;disk.clear();disk.putAll(memory);return true;
     }
     throw new AssertionError("Unexpected Editor API: "+m);
    });
   }
   throw new AssertionError("Unexpected preference API: "+method);
  }
 }
 public static void main(String[] ignored)throws Exception{
  Memory memory=new Memory();Context.testPreferences=memory.preferences();Context context=new Context();
  CommerceActivity owner=new CommerceActivity();
  Field request=CommerceActivity.class.getDeclaredField("continuationRequest");request.setAccessible(true);
  Method save=CommerceActivity.class.getDeclaredMethod("saveContinuationReward",String.class);save.setAccessible(true);
  String first="a:12345678-1234-1234-1234-123456789abc:1",second="a:12345678-1234-1234-1234-123456789abc:2";
  String show="ad:12345678-1234-1234-1234-123456789abc",other="ad:87654321-4321-4321-4321-cba987654321";
  check(!CommerceActivity.hasContinuationReward(context,first),"No receipt without earned callback");
  check(memory.commits==0,"Missing receipt does not write/create one");
  request.set(owner,first);memory.fail=true;
  check(!(boolean)save.invoke(owner,show),"Earned receipt reports initial disk failure");
  check(!memory.memory.isEmpty()&&memory.disk.isEmpty(),"Adapter reproduced Android memory-before-disk failure");
  check(!CommerceActivity.hasContinuationReward(context,first),"Unflushed in-memory receipt must not become claimable");
  check(memory.disk.isEmpty(),"Failed claim check did not persist a receipt");
  memory.fail=false;
  check(CommerceActivity.hasContinuationReward(context,first),"Retry flushes actual earned receipt");
  check(memory.disk.size()==1,"Retry durably stored exactly one receipt");
  memory.restart();check(CommerceActivity.hasContinuationReward(context,first),"Durable receipt survives process recreation");
  check(!CommerceActivity.hasContinuationReward(context,second),"Receipt is bound to request identity");
  check((boolean)save.invoke(owner,other),"Duplicate request can flush an existing receipt");
  check(memory.disk.values().contains(show)&&!memory.disk.values().contains(other),"Duplicate did not replace first earned show identity");
  request.set(owner,second);check((boolean)save.invoke(owner,other),"A distinct request records its own earned show");
  check(memory.disk.size()==2,"Two requests remain independent");
  for(String bad:new String[]{null,"","a/../x","line\nbreak","é","x".repeat(161)}){
   int before=memory.commits;
   check(!CommerceActivity.hasContinuationReward(context,bad),"Malformed request refused");
   check(!CommerceActivity.acknowledgeContinuationReward(context,bad),"Malformed ack refused");
   check(memory.commits==before,"Malformed input causes no storage mutation");
  }
  check(memory.disk.keySet().stream().allMatch(k->k.matches("earned_[a-f0-9]{64}")),"Storage keys hash request IDs");
  request.set(owner,first);
  check(!(boolean)save.invoke(owner,"fabricated-reward"),"Malformed show identifier cannot be stored");
  memory.fail=true;
  check(!CommerceActivity.acknowledgeContinuationReward(context,first),"Ack disk failure is observable");
  check(memory.disk.size()==2,"Failed ack has not deleted durable receipt");
  memory.restart();memory.fail=false;
  check(CommerceActivity.hasContinuationReward(context,first),"Receipt remains retryable after failed ack and restart");
  check(CommerceActivity.acknowledgeContinuationReward(context,first),"Ack succeeds after the run's external commit");
  memory.restart();check(!CommerceActivity.hasContinuationReward(context,first),"Acked receipt cannot be read again");
  check(CommerceActivity.hasContinuationReward(context,second),"Ack did not consume another request");
  check(CommerceActivity.acknowledgeContinuationReward(context,second),"Second ack succeeds");
  check(memory.disk.isEmpty(),"No stale receipts remain after both acknowledgements");
  System.out.println("PASS commerce continuation receipts: "+checks+" checks; Android memory-before-disk failure, durable retry, request isolation, malformed input, duplicate show binding, ack/recreation. JVM adapter only; actual SDK ads covered separately.");
 }
}
'''


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--report", default="docs/validation/v1.3/commerce-receipts-tests.txt")
    args = parser.parse_args()
    sdk = Path(os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT") or Path.home() / "Library/Android/sdk")
    android = sdk / "platforms/android-36/android.jar"
    classes = ROOT / "app/build/intermediates/javac/release/compileReleaseJavaWithJavac/classes"
    target = classes / "com/androidhit/lumadrift/commerce/CommerceActivity.class"
    source = ROOT / "app/src/main/java/com/androidhit/lumadrift/commerce/CommerceActivity.java"
    if not target.exists() or target.stat().st_mtime < source.stat().st_mtime:
        raise SystemExit("Compile current release Java first; this check refuses stale app bytecode")
    java = Path(os.environ.get("JAVA_HOME") or "/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home") / "bin"
    with tempfile.TemporaryDirectory(prefix="luma-commerce-receipts-") as temporary:
        work = Path(temporary)
        paths = {"android/content/Context.java": CONTEXT, "android/app/Activity.java": ACTIVITY, "CommerceReceiptTest.java": TEST}
        for name, content in paths.items():
            path = work / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content, encoding="utf-8")
        jars = []
        cache = Path.home() / ".gradle/caches/modules-2/files-2.1"
        for group, artifact, version in (("com.google.android.libraries.ads.mobile.sdk", "ads-mobile-sdk", "1.6.0"), ("com.google.android.ump", "user-messaging-platform", "4.0.0"), ("com.android.billingclient", "billing", "9.1.0")):
            aar = next((cache / group / artifact / version).glob("*/*.aar"), None)
            if aar:
                jar = work / (artifact + ".jar")
                with zipfile.ZipFile(aar) as archive:
                    jar.write_bytes(archive.read("classes.jar"))
                jars.append(jar)
        app_cp = os.pathsep.join(map(str, [classes, android, *jars]))
        subprocess.run([str(java / "javac"), "-cp", app_cp, "-d", str(work), *map(lambda name: str(work / name), paths)], check=True, timeout=30)
        result = subprocess.run([str(java / "java"), "-cp", str(work) + os.pathsep + app_cp, "CommerceReceiptTest"], check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, timeout=30)
        report = ROOT / args.report
        report.parent.mkdir(parents=True, exist_ok=True)
        report.write_text(result.stdout, encoding="utf-8")
        print(result.stdout, end="")


if __name__ == "__main__":
    main()
