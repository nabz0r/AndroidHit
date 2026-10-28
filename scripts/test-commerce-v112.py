#!/usr/bin/env python3
"""Actual wallet display cache and commerce copy with fail-closed provider doubles; no requests."""
from pathlib import Path
import tempfile,subprocess,os,hashlib,json,glob
root=Path(__file__).resolve().parents[1]
java=Path(os.environ['JAVA_HOME'])/'bin'
jar=sorted(glob.glob(str(Path.home()/'.gradle/caches/modules-2/files-2.1/org.json/json/*/*/json-*.jar')))[-1]
stubs={
'android/content/Context.java':'''package android.content; public abstract class Context { public Context getApplicationContext(){return this;} public abstract SharedPreferences getSharedPreferences(String n,int m); }''',
'android/content/SharedPreferences.java':'''package android.content; public interface SharedPreferences {String getString(String k,String d);Editor edit();interface Editor {Editor putString(String k,String v);Editor remove(String k);boolean commit();}}''',
'android/os/Looper.java':'''package android.os; public class Looper {public static Looper getMainLooper(){return new Looper();}}''',
'android/os/Handler.java':'''package android.os; public class Handler {public Handler(Looper x){}public boolean post(Runnable r){throw new AssertionError("No asynchronous request allowed");}}''',
'com/google/android/gms/tasks/Task.java':'''package com.google.android.gms.tasks; public class Task<T>{public boolean isSuccessful(){return false;} public T getResult(){return null;}public Exception getException(){return null;}public Task<T> addOnCompleteListener(Listener<T> x){throw new AssertionError("No token call allowed");}public interface Listener<T>{void onComplete(Task<T> task);}}''',
'com/google/firebase/auth/FirebaseUser.java':'''package com.google.firebase.auth;import com.google.android.gms.tasks.Task; public class FirebaseUser {private String uid;public FirebaseUser(String x){uid=x;}public String getUid(){return uid;}public String getDisplayName(){return "Test";}public Task<GetTokenResult> getIdToken(boolean force){throw new AssertionError("No token call allowed");}}''',
'com/google/firebase/auth/GetTokenResult.java':'''package com.google.firebase.auth; public class GetTokenResult {public String getToken(){return "";}}''',
'com/google/firebase/auth/FirebaseAuthInvalidUserException.java':'''package com.google.firebase.auth; public class FirebaseAuthInvalidUserException extends Exception {}''',
'com/google/firebase/auth/FirebaseAuthInvalidCredentialsException.java':'''package com.google.firebase.auth; public class FirebaseAuthInvalidCredentialsException extends Exception {}''',
'com/androidhit/lumadrift/identity/GoogleAccountController.java':'''package com.androidhit.lumadrift.identity;import android.content.Context;import com.google.firebase.auth.FirebaseUser;public class GoogleAccountController {public static FirebaseUser user;public static FirebaseUser currentWalletUser(Context c){return user;}}''',
'com/androidhit/lumadrift/commerce/CommerceConfig.java':'''package com.androidhit.lumadrift.commerce;public class CommerceConfig {public static boolean MODE_SANDBOX;public static String PURCHASE_VERIFIER_ENDPOINT="";public static boolean productionBillingReady(){return false;}public static int goldAmount(String product){return 0;}}''',
'com/androidhit/lumadrift/commerce/CommerceV112HostTest.java':r'''package com.androidhit.lumadrift.commerce;
import android.content.*;import com.androidhit.lumadrift.wallet.ProductionWalletClient;import com.androidhit.lumadrift.identity.GoogleAccountController;import com.google.firebase.auth.FirebaseUser;import org.json.*;import java.util.*;import java.lang.reflect.*;
public final class CommerceV112HostTest {
 static int checks;static void check(boolean v,String m){checks++;if(!v)throw new AssertionError(m);}
 static final class Prefs implements SharedPreferences {final Map<String,String> values=new HashMap<>();public String getString(String k,String d){return values.getOrDefault(k,d);} public Editor edit(){return new Editor(){public Editor putString(String k,String v){values.put(k,v);return this;}public Editor remove(String k){values.remove(k);return this;}public boolean commit(){return true;}};}}
 static final class Ctx extends Context {final Prefs p=new Prefs();public SharedPreferences getSharedPreferences(String n,int m){if(!"luma_server_wallet_v1".equals(n))throw new AssertionError("Unexpected persistence");return p;}}
 static Ctx c=new Ctx(); static void raw(String uid,String value){c.p.values.put(BillingPolicy.accountHash(uid),value);}static JSONObject snapshot(String uid,long amount,long version)throws Exception{return new JSONObject().put("uid",uid).put("balance",amount).put("version",version).put("characters",new JSONArray().put("miko")).put("skins",new JSONArray().put("lagoon"));}
 public static void main(String[] args)throws Exception {
  GoogleAccountController.user=new FirebaseUser("account-a");
  check(ProductionWalletClient.cachedBalance(c)==null,"absent cache is unknown");
  raw("account-a","{\"characters\":[\"miko\"],\"skins\":[\"lagoon\"],\"version\":4}");
  check(ProductionWalletClient.cachedBalance(c)==null,"old entitlement-only cache is unknown");
  check(ProductionWalletClient.ownsCharacter(c,"miko")&&ProductionWalletClient.ownsSkin(c,"lagoon"),"old entitlements remain compatible");
  for(long amount:new long[]{-1000000000L,-1,0,1234,1000000000L}){raw("account-a",snapshot("account-a",amount,4).toString());check(Long.valueOf(amount).equals(ProductionWalletClient.cachedBalance(c)),"exact signed balance "+amount);}
  for(long amount:new long[]{-1000000001L,1000000001L,Long.MAX_VALUE,Long.MIN_VALUE}){raw("account-a",snapshot("account-a",amount,4).toString());check(ProductionWalletClient.cachedBalance(c)==null,"out of range "+amount);}
  for(String raw:new String[]{"", "{}", "bad-json", snapshot("account-b",7,4).toString(),snapshot("account-a",7,-1).toString(),snapshot("account-a",7,4).put("balance","7").toString(),snapshot("account-a",7,4).put("balance",7.1).toString(),snapshot("account-a",7,4).put("version",4.1).toString(),snapshot("account-a",7,4).put("uid",JSONObject.NULL).toString()}){raw("account-a",raw);check(ProductionWalletClient.cachedBalance(c)==null,"invalid cache unknown: "+raw);}
  raw("account-a",snapshot("account-a",250,4).toString());GoogleAccountController.user=null;check(ProductionWalletClient.cachedBalance(c)==null,"guest unknown");GoogleAccountController.user=new FirebaseUser("account-b");check(ProductionWalletClient.cachedBalance(c)==null,"other account unknown");GoogleAccountController.user=new FirebaseUser("account-a");CommerceConfig.MODE_SANDBOX=true;check(ProductionWalletClient.cachedBalance(c)==null,"Lab never reads premium");CommerceConfig.MODE_SANDBOX=false;
  ProductionWalletClient client=new ProductionWalletClient(c);
  Constructor<ProductionWalletClient.Wallet> constructor=ProductionWalletClient.Wallet.class.getDeclaredConstructor(String.class,JSONObject.class);constructor.setAccessible(true);
  Method cache=ProductionWalletClient.class.getDeclaredMethod("cache",ProductionWalletClient.Wallet.class);cache.setAccessible(true);
  JSONObject response=snapshot("account-a",99,5).put("inventory",new JSONObject());cache.invoke(client,constructor.newInstance("account-a",response));
  check(Long.valueOf(99).equals(ProductionWalletClient.cachedBalance(c)),"successful snapshot includes balance");check(ProductionWalletClient.ownsCharacter(c,"miko")&&ProductionWalletClient.ownsSkin(c,"lagoon"),"successful snapshot preserves entitlements");
  cache.invoke(client,constructor.newInstance("account-a",snapshot("account-a",999,3).put("inventory",new JSONObject())));check(Long.valueOf(99).equals(ProductionWalletClient.cachedBalance(c)),"older version never overwrites newer");
  ProductionWalletClient.clearCache(c,"account-b");check(Long.valueOf(99).equals(ProductionWalletClient.cachedBalance(c)),"other account removal isolated");ProductionWalletClient.clearCache(c,"account-a");check(ProductionWalletClient.cachedBalance(c)==null&&!ProductionWalletClient.ownsCharacter(c,"miko"),"logout deletion clear full optional snapshot");client.close();
  Field field=CommerceCopy.class.getDeclaredField("COPY");field.setAccessible(true);@SuppressWarnings("unchecked") Map<String,String[]> copy=(Map<String,String[]>)field.get(null);
  for(var e:copy.entrySet()){check(e.getValue().length==6,"six translations "+e.getKey());for(String language:new String[]{"en","fr","de","es","pt","it"}){String text=CommerceCopy.get(language,e.getKey(),35);check(!text.isBlank()&&!text.equals(e.getKey())&&!text.contains("%d"),"formatted "+language+":"+e.getKey());}}
  check(CommerceCopy.get("fr-FR","shop").equals(CommerceCopy.get("fr","shop")),"regional copy normalization");check(CommerceCopy.get("unknown","shop").equals(CommerceCopy.get("en","shop")),"fallback copy");
  System.out.println("Commerce 1.12: "+checks+" host checks passed; actual wallet cache and six-language copy; provider doubles, no network");
 }
}'''
}
with tempfile.TemporaryDirectory(prefix='luma-commerce-v112-') as td:
    temp=Path(td)
    for name,source in stubs.items():
        p=temp/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(source)
    actual=[root/'app/src/main/java/com/androidhit/lumadrift'/name for name in ['wallet/ProductionWalletClient.java','commerce/BillingPolicy.java','commerce/CommerceCopy.java','data/Strings.java']]
    cmd=[str(java/'javac'),'-cp',jar,'-d',str(temp/'classes')]+[str(temp/name) for name in stubs]+[str(p) for p in actual]
    subprocess.run(cmd,check=True)
    subprocess.run([str(java/'java'),'-cp',str(temp/'classes')+':'+jar,'com.androidhit.lumadrift.commerce.CommerceV112HostTest'],check=True)
    print(json.dumps({'sources':{str(p.relative_to(root)):hashlib.sha256(p.read_bytes()).hexdigest() for p in actual},'json_jar_sha256':hashlib.sha256(Path(jar).read_bytes()).hexdigest()},indent=2))
