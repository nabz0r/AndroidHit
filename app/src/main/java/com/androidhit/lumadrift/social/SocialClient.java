package com.androidhit.lumadrift.social;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.androidhit.lumadrift.BuildConfig;
import com.androidhit.lumadrift.commerce.BillingPolicy;
import com.androidhit.lumadrift.identity.GoogleAccountController;
import com.google.firebase.auth.FirebaseUser;
import org.json.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/** Optional authenticated social API. Guest play does not create a social profile. */
public final class SocialClient implements AutoCloseable {
    public interface Callback { void onSuccess(JSONObject result); void onError(String code); }
    public interface Listener { void onSnapshot(Snapshot snapshot); void onError(String code); }
    public static final class Friend {
        public final String code,nickname,avatar;
        /** 0 means not shared. Positions are self-reported, never verified ranking data. */
        public final int level;
        Friend(JSONObject value) throws JSONException {
            code=value.getString("code");nickname=value.getString("nickname");avatar=value.getString("avatar");
            level=value.isNull("level")?0:value.getInt("level");
            if(!code.matches("[A-Z0-9]{16}")||nickname.length()>32||!avatar.matches("[a-z][a-z0-9_]{0,31}")||level<0||level>1000)throw new JSONException("Invalid friend");
        }
    }
    public static final class Gift {
        public final String id,fromName;public final boolean claimed;
        Gift(JSONObject value) throws JSONException {id=value.getString("id");fromName=value.getString("fromName");claimed=value.getBoolean("claimed");JSONObject reward=value.getJSONObject("reward");if(!id.matches("[a-f0-9]{64}")||fromName.length()>32||!"power".equals(reward.optString("kind"))||!"hint".equals(reward.optString("itemId"))||reward.getInt("amount")!=1)throw new JSONException("Invalid gift");}
    }
    public static final class Snapshot {
        public final String uid,code,nickname,avatar;public final int level,giftsRemaining;public final boolean shareProgress;
        public final List<Friend> friends,incoming,outgoing;public final List<Gift> gifts;
        Snapshot(String uid,JSONObject json) throws JSONException {
            this.uid=uid;JSONObject me=json.getJSONObject("me");Friend own=new Friend(me);code=own.code;nickname=own.nickname;avatar=own.avatar;level=own.level;shareProgress=me.getBoolean("shareProgress");
            friends=readFriends(json.getJSONArray("friends"),40);incoming=readFriends(json.getJSONArray("incoming"),20);outgoing=readFriends(json.getJSONArray("outgoing"),20);
            JSONArray array=json.getJSONArray("gifts");if(array.length()>64)throw new JSONException("Too many gifts");List<Gift> values=new ArrayList<>();for(int i=0;i<array.length();i++)values.add(new Gift(array.getJSONObject(i)));gifts=Collections.unmodifiableList(values);
            giftsRemaining=json.getJSONObject("limits").getInt("giftsRemaining");if(giftsRemaining<0||giftsRemaining>3)throw new JSONException("Invalid quota");
        }
        private static List<Friend> readFriends(JSONArray array,int max) throws JSONException {if(array.length()>max)throw new JSONException("Too many friends");List<Friend> list=new ArrayList<>();for(int i=0;i<array.length();i++)list.add(new Friend(array.getJSONObject(i)));return Collections.unmodifiableList(list);}
    }
    private static final String PREFS="luma_social_cache_v1";
    private final Context context;private final Handler main=new Handler(Looper.getMainLooper());private final ExecutorService io=Executors.newSingleThreadExecutor();private volatile boolean closed;
    public SocialClient(Context context){this.context=context.getApplicationContext();}
    public static boolean configured(){return !BillingPolicy.serverBase(BuildConfig.SOCIAL_API_BASE_URL).isEmpty();}
    public static String currentUid(Context context){FirebaseUser user=GoogleAccountController.currentWalletUser(context);return user==null?"":user.getUid();}
    public String uid(){return currentUid(context);}
    public static boolean hasSavedProfile(Context context){String uid=currentUid(context);return !uid.isEmpty()&&context.getSharedPreferences(PREFS,0).contains(BillingPolicy.accountHash(uid));}
    /** Same-account cache only; short lifetime, no network or invented fallback friends. */
    public static List<Friend> cachedFriends(Context context){String uid=currentUid(context);if(uid.isEmpty())return Collections.emptyList();try{JSONObject saved=new JSONObject(context.getSharedPreferences(PREFS,0).getString(BillingPolicy.accountHash(uid),""));long age=System.currentTimeMillis()-saved.getLong("savedAt");if(age<0||age>15*60000L)return Collections.emptyList();return new Snapshot(uid,saved.getJSONObject("snapshot")).friends;}catch(Exception absent){return Collections.emptyList();}}
    public static void clearCache(Context context,String uid){if(uid!=null&&!uid.isEmpty())context.getSharedPreferences(PREFS,0).edit().remove(BillingPolicy.accountHash(uid)).apply();}
    public void refresh(Listener listener){final String requestedUid=uid();request("GET","",null,new Callback(){public void onSuccess(JSONObject result){try{Snapshot snapshot=new Snapshot(requestedUid,result);if(!requestedUid.equals(uid())){listener.onError("SOCIAL_SIGN_IN_REQUIRED");return;}com.androidhit.lumadrift.data.GameStore local=new com.androidhit.lumadrift.data.GameStore(context);if(snapshot.shareProgress&&(snapshot.level!=local.unlockedLevel()||!snapshot.avatar.equals(local.equippedCharacter()))){updateProfile(snapshot.nickname,local.equippedCharacter(),local.unlockedLevel(),true,new Callback(){public void onSuccess(JSONObject ignored){refresh(listener);}public void onError(String code){listener.onError(code);}});return;}context.getSharedPreferences(PREFS,0).edit().putString(BillingPolicy.accountHash(requestedUid),new JSONObject().put("savedAt",System.currentTimeMillis()).put("snapshot",result).toString()).apply();listener.onSnapshot(snapshot);}catch(Exception malformed){listener.onError("SOCIAL_INVALID_RESPONSE");}}public void onError(String code){listener.onError(code);}});}
    public void updateProfile(String nickname,String avatar,int level,boolean share,Callback callback){try{request("POST","/profile",new JSONObject().put("nickname",nickname).put("avatar",avatar).put("level",level).put("shareProgress",share),callback);}catch(JSONException invalid){fail(callback,"INVALID_PROFILE");}}
    public void deleteAccount(Callback callback){final String uid=uid();request("DELETE","",null,new Callback(){public void onSuccess(JSONObject result){if(!result.optBoolean("deleted",false)){callback.onError("SOCIAL_INVALID_RESPONSE");return;}clearCache(context,uid);callback.onSuccess(result);}public void onError(String code){callback.onError(code);}});}
    public void invite(String code,Callback callback){pair("/request",code,callback);}
    public void remove(String code,Callback callback){pair("/remove",code,callback);}
    public void sendGift(String code,Callback callback){pair("/gift",code,callback);}
    public void respond(String code,boolean accept,Callback callback){try{request("POST","/respond",new JSONObject().put("code",cleanCode(code)).put("accept",accept).put("requestId",UUID.randomUUID().toString()),callback);}catch(Exception invalid){fail(callback,"INVALID_FRIEND_CODE");}}
    public void claim(String id,Callback callback){try{request("POST","/claim",new JSONObject().put("giftId",id).put("requestId","claim:"+id),callback);}catch(Exception invalid){fail(callback,"INVALID_GIFT_ID");}}
    private void pair(String path,String code,Callback callback){try{request("POST",path,new JSONObject().put("code",cleanCode(code)).put("requestId",UUID.randomUUID().toString()),callback);}catch(Exception invalid){fail(callback,"INVALID_FRIEND_CODE");}}
    private static String cleanCode(String code){String value=code==null?"":code.replace(" ","").replace("-","").toUpperCase(Locale.ROOT);if(!value.matches("[A-Z0-9]{16}"))throw new IllegalArgumentException();return value;}
    private void request(String method,String path,JSONObject body,Callback callback){
        if(closed)return;if(!configured()){fail(callback,"SOCIAL_NOT_CONFIGURED");return;}FirebaseUser user=GoogleAccountController.currentWalletUser(context);if(user==null){fail(callback,"SOCIAL_SIGN_IN_REQUIRED");return;}final String uid=user.getUid();
        user.getIdToken(false).addOnCompleteListener(task->{if(closed)return;if(!uid.equals(uid())){fail(callback,"SOCIAL_SIGN_IN_REQUIRED");return;}if(!task.isSuccessful()||task.getResult()==null||task.getResult().getToken()==null){fail(callback,"SOCIAL_UNAVAILABLE");return;}String token=task.getResult().getToken();try{io.execute(()->execute(method,path,body,token,uid,callback));}catch(RejectedExecutionException ignored){}});
    }
    private void execute(String method,String path,JSONObject body,String token,String uid,Callback callback){HttpURLConnection connection=null;try{
        connection=(HttpURLConnection)new URL(BillingPolicy.serverBase(BuildConfig.SOCIAL_API_BASE_URL)+("DELETE".equals(method)?"/v1/account":"/v1/social"+path)).openConnection();connection.setInstanceFollowRedirects(false);connection.setUseCaches(false);connection.setConnectTimeout(12000);connection.setReadTimeout(20000);connection.setRequestMethod(method);connection.setRequestProperty("Authorization","Bearer "+token);connection.setRequestProperty("Accept","application/json");
        if(body!=null){byte[] bytes=body.toString().getBytes(StandardCharsets.UTF_8);connection.setRequestProperty("Content-Type","application/json; charset=utf-8");connection.setDoOutput(true);connection.setFixedLengthStreamingMode(bytes.length);try(OutputStream output=connection.getOutputStream()){output.write(bytes);}}
        int status=connection.getResponseCode();InputStream input=status>=200&&status<300?connection.getInputStream():connection.getErrorStream();JSONObject response;
        try(InputStream in=input;ByteArrayOutputStream bytes=new ByteArrayOutputStream()){if(in==null)throw new IOException();byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1){if(bytes.size()+n>131072)throw new IOException();bytes.write(buffer,0,n);}response=new JSONObject(bytes.toString("UTF-8"));}
        final JSONObject result=response;final String error=status==401?"SOCIAL_SIGN_IN_REQUIRED":status==404&&response.optJSONObject("error")!=null&&"NOT_FOUND".equals(response.optJSONObject("error").optString("code"))?"SOCIAL_NOT_DEPLOYED":response.optJSONObject("error")==null?"SOCIAL_UNAVAILABLE":response.optJSONObject("error").optString("code","SOCIAL_UNAVAILABLE");
        main.post(()->{if(closed||(!uid.equals(uid())&&!("DELETE".equals(method)&&uid().isEmpty())))return;if(status>=200&&status<300)callback.onSuccess(result);else callback.onError(error);});
    }catch(Exception unavailable){main.post(()->{if(!closed&&(uid.equals(uid())||"DELETE".equals(method)&&uid().isEmpty()))callback.onError("SOCIAL_UNAVAILABLE");});}finally{if(connection!=null)connection.disconnect();}}
    private void fail(Callback callback,String code){main.post(()->{if(!closed)callback.onError(code);});}
    public void close(){closed=true;io.shutdownNow();}
}
