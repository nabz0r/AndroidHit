#!/usr/bin/env python3
"""Run the real session controller against deterministic asynchronous Android/Firebase doubles.

No Google account, token endpoint or owner configuration is used. This validates application
transitions, not a real provider login. The doubles live only in a temporary build directory.
"""
from pathlib import Path
import os
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
SOURCES = {
'android/content/Context.java': '''package android.content;
public class Context {
 public static SharedPreferences prefs = new SharedPreferences();
 public Context getApplicationContext(){return this;}
 public SharedPreferences getSharedPreferences(String key,int mode){return prefs;}
}''',
'android/content/SharedPreferences.java': '''package android.content;
import java.util.*;
public class SharedPreferences {
 public interface OnSharedPreferenceChangeListener {void onSharedPreferenceChanged(SharedPreferences p,String k);}
 private final Map<String,Object> values=new HashMap<>();
 public final List<OnSharedPreferenceChangeListener> listeners=new ArrayList<>();
 public boolean getBoolean(String k,boolean d){return values.containsKey(k)?(Boolean)values.get(k):d;}
 public String getString(String k,String d){return values.containsKey(k)?(String)values.get(k):d;}
 public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener l){listeners.add(l);}
 public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener l){listeners.remove(l);}
 public Editor edit(){return new Editor();}
 public final class Editor {
  Map<String,Object> pending=new LinkedHashMap<>();
  public Editor putBoolean(String k,boolean v){pending.put(k,v);return this;}
  public Editor putString(String k,String v){pending.put(k,v);return this;}
  public Editor remove(String k){pending.put(k,null);return this;}
  public void apply(){
   List<String> changed=new ArrayList<>();
   for(String k:pending.keySet()){Object v=pending.get(k);if(!Objects.equals(values.get(k),v))changed.add(k);if(v==null)values.remove(k);else values.put(k,v);}
   for(String k:changed)for(OnSharedPreferenceChangeListener l:new ArrayList<>(listeners))l.onSharedPreferenceChanged(SharedPreferences.this,k);
  }
 }
}''',
'android/app/Activity.java': 'package android.app; public class Activity extends android.content.Context {}',
'android/os/CancellationSignal.java': 'package android.os; public class CancellationSignal {public boolean cancelled; public void cancel(){cancelled=true;}}',
'android/os/Bundle.java': '''package android.os; public class Bundle extends java.util.HashMap<String,String> {}''',
'android/util/Base64.java': '''package android.util; public class Base64 {public static final int URL_SAFE=8; public static byte[] decode(String s,int flags){return java.util.Base64.getUrlDecoder().decode(s);}}''',
'androidx/core/content/ContextCompat.java': '''package androidx.core.content; public class ContextCompat {public static java.util.concurrent.Executor getMainExecutor(android.content.Context c){return Runnable::run;}}''',
'androidx/credentials/CredentialManagerCallback.java': '''package androidx.credentials; public interface CredentialManagerCallback<T,E extends Exception>{void onResult(T value);void onError(E error);}''',
'androidx/credentials/Credential.java': '''package androidx.credentials; public class Credential {public String getType(){return "";}public android.os.Bundle getData(){return new android.os.Bundle();}}''',
'androidx/credentials/CustomCredential.java': '''package androidx.credentials;
public class CustomCredential extends Credential {
 private final String type;private final android.os.Bundle data;
 public CustomCredential(String t,android.os.Bundle d){type=t;data=d;}
 public String getType(){return type;}public android.os.Bundle getData(){return data;}
}''',
'androidx/credentials/GetCredentialResponse.java': '''package androidx.credentials; public class GetCredentialResponse {private final Credential value;public GetCredentialResponse(Credential c){value=c;}public Credential getCredential(){return value;}}''',
'androidx/credentials/GetCredentialRequest.java': '''package androidx.credentials; public class GetCredentialRequest {public Object option;public static class Builder{private final GetCredentialRequest value=new GetCredentialRequest();public Builder addCredentialOption(Object o){value.option=o;return this;}public GetCredentialRequest build(){return value;}}}''',
'androidx/credentials/ClearCredentialStateRequest.java': 'package androidx.credentials; public class ClearCredentialStateRequest {}',
'androidx/credentials/CredentialManager.java': '''package androidx.credentials;
import androidx.credentials.exceptions.*;
public class CredentialManager {
 public static CredentialManager latest;public static int chooserCalls,clearCalls;public static boolean failClear;
 public GetCredentialRequest request;public CredentialManagerCallback<GetCredentialResponse,GetCredentialException> callback;
 public static CredentialManager create(android.content.Context c){latest=new CredentialManager();return latest;}
 public void getCredentialAsync(android.app.Activity a,GetCredentialRequest r,android.os.CancellationSignal s,java.util.concurrent.Executor e,CredentialManagerCallback<GetCredentialResponse,GetCredentialException> c){latest=this;chooserCalls++;request=r;callback=c;}
 public void clearCredentialStateAsync(ClearCredentialStateRequest r,android.os.CancellationSignal s,java.util.concurrent.Executor e,CredentialManagerCallback<Void,ClearCredentialException> c){clearCalls++;if(failClear)c.onError(new ClearCredentialException());else c.onResult(null);}
 public void googleResult(boolean validNonce){
  com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption o=(com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption)request.option;
  String claims="{\\"aud\\":\\""+o.clientId+"\\",\\"nonce\\":\\""+(validNonce?o.nonce:"wrong")+"\\"}";
  String token="header."+java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(claims.getBytes(java.nio.charset.StandardCharsets.UTF_8))+".signature";
  android.os.Bundle data=new android.os.Bundle();data.put("token",token);
  callback.onResult(new GetCredentialResponse(new CustomCredential(com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL,data)));
 }
}''',
'com/google/android/libraries/identity/googleid/GetSignInWithGoogleOption.java': '''package com.google.android.libraries.identity.googleid;
public class GetSignInWithGoogleOption {public String clientId,nonce; public static class Builder{private final GetSignInWithGoogleOption value=new GetSignInWithGoogleOption();public Builder(String id){value.clientId=id;}public Builder setNonce(String n){value.nonce=n;return this;}public GetSignInWithGoogleOption build(){return value;}}}''',
'com/google/android/libraries/identity/googleid/GoogleIdTokenCredential.java': '''package com.google.android.libraries.identity.googleid;
public class GoogleIdTokenCredential {public static final String TYPE_GOOGLE_ID_TOKEN_CREDENTIAL="google.id";private String token; public static GoogleIdTokenCredential createFrom(android.os.Bundle data){GoogleIdTokenCredential v=new GoogleIdTokenCredential();v.token=data.get("token");return v;}public String getIdToken(){return token;}}''',
'com/google/android/gms/tasks/OnCompleteListener.java': '''package com.google.android.gms.tasks; public interface OnCompleteListener<T>{void onComplete(Task<T> task);}''',
'com/google/android/gms/tasks/Task.java': '''package com.google.android.gms.tasks;
import java.util.*;import java.util.concurrent.Executor;
public class Task<T> {
 private T value;private Exception error;private boolean done;private final List<Runnable> callbacks=new ArrayList<>();
 public Task<T> addOnCompleteListener(Executor e,OnCompleteListener<T> c){Runnable r=()->e.execute(()->c.onComplete(this));if(done)r.run();else callbacks.add(r);return this;}
 public boolean isSuccessful(){return done&&error==null;}public T getResult(){return value;}public Exception getException(){return error;}
 public void succeed(T v){if(done)throw new AssertionError("already done");value=v;finish();}
 public void fail(Exception e){if(done)throw new AssertionError("already done");error=e;finish();}
 private void finish(){done=true;for(Runnable r:new ArrayList<>(callbacks))r.run();callbacks.clear();}
}''',
'com/google/firebase/FirebaseApp.java': '''package com.google.firebase;
public class FirebaseApp {public static FirebaseApp app;public static int initializations;public static FirebaseApp getInstance(String n){if(app==null)throw new IllegalStateException();return app;}public static FirebaseApp initializeApp(android.content.Context c,FirebaseOptions o,String n){initializations++;return app=new FirebaseApp();}public void setDataCollectionDefaultEnabled(boolean enabled){if(enabled)throw new AssertionError("unexpected collection");}}''',
'com/google/firebase/FirebaseOptions.java': '''package com.google.firebase;public class FirebaseOptions{public static class Builder{public Builder setApiKey(String s){return this;}public Builder setApplicationId(String s){return this;}public Builder setProjectId(String s){return this;}public FirebaseOptions build(){return new FirebaseOptions();}}}''',
'com/google/firebase/auth/UserInfo.java': 'package com.google.firebase.auth; public interface UserInfo {String getProviderId();}',
'com/google/firebase/auth/AuthCredential.java': 'package com.google.firebase.auth; public class AuthCredential {}',
'com/google/firebase/auth/GoogleAuthProvider.java': 'package com.google.firebase.auth; public class GoogleAuthProvider {public static AuthCredential getCredential(String a,String b){return new AuthCredential();}}',
'com/google/firebase/auth/AuthResult.java': 'package com.google.firebase.auth; public class AuthResult {private FirebaseUser user;public AuthResult(FirebaseUser u){user=u;}public FirebaseUser getUser(){return user;}}',
'com/google/firebase/auth/GetTokenResult.java': 'package com.google.firebase.auth; public class GetTokenResult {private String token;public GetTokenResult(String t){token=t;}public String getToken(){return token;}}',
'com/google/firebase/auth/FirebaseUser.java': '''package com.google.firebase.auth;
import java.util.*;import com.google.android.gms.tasks.Task;
public class FirebaseUser {
 public final String uid;public String name,email;public boolean anonymous;public String provider="google.com";
 public final ArrayDeque<Task<Void>> reloads=new ArrayDeque<>(),deletes=new ArrayDeque<>();
 public final ArrayDeque<Task<GetTokenResult>> tokens=new ArrayDeque<>();
 public final ArrayDeque<Task<AuthResult>> reauths=new ArrayDeque<>();
 public FirebaseUser(String id,String n){uid=id;name=n;email=id+"@example.invalid";}
 public String getUid(){return uid;}public String getDisplayName(){return name;}public String getEmail(){return email;}public boolean isAnonymous(){return anonymous;}
 public java.util.List<UserInfo> getProviderData(){return java.util.List.of(()->provider);}
 public Task<Void> reload(){Task<Void> t=new Task<>();reloads.add(t);return t;}
 public Task<GetTokenResult> getIdToken(boolean force){Task<GetTokenResult> t=new Task<>();tokens.add(t);return t;}
 public Task<AuthResult> reauthenticateAndRetrieveData(AuthCredential c){Task<AuthResult> t=new Task<>();reauths.add(t);return t;}
 public Task<Void> delete(){Task<Void> t=new Task<>();deletes.add(t);return t;}
 public void refreshed(){reloads.remove().succeed(null);tokens.remove().succeed(new GetTokenResult("verified.firebase.token"));}
}''',
'com/google/firebase/auth/FirebaseAuth.java': '''package com.google.firebase.auth;
import java.util.*;import com.google.android.gms.tasks.Task;
public class FirebaseAuth {
 public interface AuthStateListener {void onAuthStateChanged(FirebaseAuth a);}public interface IdTokenListener {void onIdTokenChanged(FirebaseAuth a);}
 public static FirebaseAuth instance=new FirebaseAuth();public FirebaseUser user;public int signOutCalls;
 public final List<AuthStateListener> authListeners=new ArrayList<>();public final List<IdTokenListener> tokenListeners=new ArrayList<>();
 public final ArrayDeque<Task<AuthResult>> signIns=new ArrayDeque<>();
 public static FirebaseAuth getInstance(com.google.firebase.FirebaseApp app){return instance;}
 public FirebaseUser getCurrentUser(){return user;}
 public void addAuthStateListener(AuthStateListener l){authListeners.add(l);l.onAuthStateChanged(this);}public void removeAuthStateListener(AuthStateListener l){authListeners.remove(l);}
 public void addIdTokenListener(IdTokenListener l){tokenListeners.add(l);l.onIdTokenChanged(this);}public void removeIdTokenListener(IdTokenListener l){tokenListeners.remove(l);}
 public Task<AuthResult> signInWithCredential(AuthCredential c){Task<AuthResult> t=new Task<>();signIns.add(t);return t;}
 public void signOut(){signOutCalls++;user=null;fire();}
 public void setUser(FirebaseUser u){user=u;fire();}
 public void finishSignIn(FirebaseUser u){setUser(u);signIns.remove().succeed(new AuthResult(u));}
 public void fire(){for(AuthStateListener l:new ArrayList<>(authListeners))l.onAuthStateChanged(this);for(IdTokenListener l:new ArrayList<>(tokenListeners))l.onIdTokenChanged(this);}
}''',
'com/androidhit/lumadrift/identity/IdentityConfig.java': '''package com.androidhit.lumadrift.identity;
public class IdentityConfig {public static boolean configured=true;public String webClientId="test-client.apps.googleusercontent.com",apiKey="test",appId="test",projectId="test"; public static IdentityConfig load(android.content.Context c){return new IdentityConfig();}public boolean isConfigured(){return configured;}}''',
'com/androidhit/lumadrift/commerce/CommerceConfig.java': '''package com.androidhit.lumadrift.commerce;public class CommerceConfig{public static boolean MODE_SANDBOX=true;public static boolean productionBillingReady(){return !MODE_SANDBOX;}}''',
'com/androidhit/lumadrift/wallet/ProductionWalletClient.java': '''package com.androidhit.lumadrift.wallet;
public class ProductionWalletClient {public static class Wallet{} public interface Callback{void onSuccess(Wallet w);void onError(String k);}public static Callback deletion;public ProductionWalletClient(android.content.Context c){} public void deleteAccount(Callback c){deletion=c;}public void close(){}}''',
'com/androidhit/lumadrift/social/SocialClient.java': '''package com.androidhit.lumadrift.social;
public class SocialClient {
 public interface Callback{void onSuccess(org.json.JSONObject result);void onError(String code);}
 public static boolean serverConfigured;public static Callback deletion;public static int creations,deletionCalls,closeCalls;
 public static final java.util.List<String> cleared=new java.util.ArrayList<>();
 public SocialClient(android.content.Context context){creations++;}
 public static boolean configured(){return serverConfigured;}
 public void deleteAccount(Callback callback){deletionCalls++;deletion=callback;}
 public static void clearCache(android.content.Context context,String uid){cleared.add(uid);}
 public void close(){closeCalls++;}
 public static void reset(){serverConfigured=false;deletion=null;creations=deletionCalls=closeCalls=0;cleared.clear();}
}''',
'com/androidhit/lumadrift/social/CosmeticVaultClient.java': '''package com.androidhit.lumadrift.social;
public class CosmeticVaultClient {
 public static final java.util.List<String> cleared=new java.util.ArrayList<>();
 public static void clearCache(android.content.Context context,String uid){cleared.add(uid);}
}''',
'org/json/JSONObject.java': '''package org.json;
public class JSONObject {private final String json;public JSONObject(String s){json=s;}public String optString(String k){int a=json.indexOf((char)34+k+(char)34);if(a<0)return "";a=json.indexOf(':',a)+1;a=json.indexOf((char)34,a)+1;int b=json.indexOf((char)34,a);return b<a?"":json.substring(a,b);}}''',
}
for name in ('GetCredentialException', 'GetCredentialCancellationException', 'ClearCredentialException'):
    parent = 'GetCredentialException' if name == 'GetCredentialCancellationException' else 'Exception'
    SOURCES[f'androidx/credentials/exceptions/{name}.java'] = f'package androidx.credentials.exceptions; public class {name} extends {parent} {{}}'
for name in ('FirebaseAuthInvalidUserException', 'FirebaseAuthInvalidCredentialsException', 'FirebaseAuthRecentLoginRequiredException'):
    SOURCES[f'com/google/firebase/auth/{name}.java'] = f'package com.google.firebase.auth; public class {name} extends Exception {{}}'

java_home = os.environ.get('JAVA_HOME')
if java_home and (Path(java_home) / 'bin/javac').exists():
    java_bin = Path(java_home) / 'bin'
elif Path('/opt/homebrew/opt/openjdk@17/bin/javac').exists():
    java_bin = Path('/opt/homebrew/opt/openjdk@17/bin')
else:
    java_bin = Path(shutil.which('javac')).parent
with tempfile.TemporaryDirectory(prefix='luma-identity-session-') as temporary:
    build = Path(temporary)
    files = []
    for relative, content in SOURCES.items():
        path = build / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content)
        files.append(str(path))
    actual = ROOT / 'app/src/main/java/com/androidhit/lumadrift/identity'
    files += [str(actual / f'{name}.java') for name in ('SessionSnapshot', 'SessionTracker', 'IdentitySessionAccess', 'GoogleAccountController')]
    files += [str(ROOT / 'app/src/main/java/com/androidhit/lumadrift/data/Strings.java'), str(ROOT / 'scripts/IdentitySessionTest.java')]
    subprocess.run([str(java_bin / 'javac'), '-d', str(build), *files], check=True)
    subprocess.run([str(java_bin / 'java'), '-cp', str(build), 'com.androidhit.lumadrift.identity.IdentitySessionTest'], check=True)
