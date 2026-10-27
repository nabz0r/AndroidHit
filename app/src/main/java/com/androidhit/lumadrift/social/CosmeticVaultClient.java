package com.androidhit.lumadrift.social;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import com.androidhit.lumadrift.BuildConfig;
import com.androidhit.lumadrift.commerce.BillingPolicy;
import com.androidhit.lumadrift.data.CosmeticCatalog;
import com.androidhit.lumadrift.identity.GoogleAccountController;
import com.google.firebase.auth.FirebaseUser;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/** Account-only premium catalogue. Never reads or debits the earned LumaCoins ledger. */
public final class CosmeticVaultClient implements AutoCloseable {
    interface Callback<T>{void onSuccess(T result);void onError(String code);}
    private static final String PREFS="luma_cosmetic_vault_v1";
    private static final Object CACHE_LOCK=new Object();
    private final Context context;
    private final Handler main=new Handler(Looper.getMainLooper());
    private final ExecutorService io=new ThreadPoolExecutor(1,1,30,TimeUnit.SECONDS,new ArrayBlockingQueue<>(2),r->{Thread thread=new Thread(r,"Luma-cosmetic-vault");thread.setDaemon(true);return thread;});
    private volatile boolean closed;
    private volatile HttpURLConnection connection;
    public CosmeticVaultClient(Context context){this.context=context.getApplicationContext();}
    public static boolean configured(){return !BillingPolicy.serverBase(BuildConfig.SOCIAL_API_BASE_URL).isEmpty();}
    public static String currentUid(Context context){FirebaseUser user=GoogleAccountController.currentWalletUser(context);return user==null?"":user.getUid();}
    public String uid(){return currentUid(context);}
    /** Optional permanent-ownership cache, bound to the current verified Google account. */
    public static boolean ownsCosmetic(Context context,String itemId){
        if(CosmeticCatalog.find(itemId)==null)return false;String uid=currentUid(context);if(uid.isEmpty())return false;
        synchronized(CACHE_LOCK){try{
            String saved=context.getSharedPreferences(PREFS,0).getString("owned:"+BillingPolicy.accountHash(uid),"");
            JSONObject value=new JSONObject(saved);
            if(CosmeticVaultProtocol.integer(value,"version")<0)return false;
            return CosmeticVaultProtocol.owned(value.getJSONArray("owned")).contains(itemId);
        }catch(Exception absent){return false;}}
    }
    public static void clearCache(Context context,String uid){
        if(uid==null||uid.isEmpty())return;
        synchronized(CACHE_LOCK){context.getSharedPreferences(PREFS,0).edit().remove("owned:"+BillingPolicy.accountHash(uid)).commit();}
    }
    String pendingRequest(){String uid=uid();return uid.isEmpty()?"":prefs().getString("pending:"+BillingPolicy.accountHash(uid),"");}
    void catalog(Callback<CosmeticVaultProtocol.Catalog> callback){
        final String requestedUid=uid();
        request("GET","/v1/wallet/cosmetic-catalog",null,requestedUid,new Callback<JSONObject>(){
            public void onSuccess(JSONObject value){try{if(!requestedUid.equals(uid())){callback.onError("VAULT_SIGN_IN_REQUIRED");return;}CosmeticVaultProtocol.Catalog catalog=CosmeticVaultProtocol.catalog(requestedUid,value);cache(catalog.uid,catalog.version,catalog.owned,null);callback.onSuccess(catalog);}catch(Exception invalid){callback.onError("VAULT_INVALID_RESPONSE");}}
            public void onError(String code){callback.onError(code);}
        });
    }
    /** Both the immutable build gate and authenticated server flag must permit a draw. */
    void draw(CosmeticVaultProtocol.Catalog catalog,Callback<CosmeticVaultProtocol.Receipt> callback){
        if(!BuildConfig.PAID_COSMETIC_DRAWS_ENABLED||catalog==null||!catalog.enabled){fail(callback,"PAID_COSMETIC_DRAWS_DISABLED");return;}
        final String uid=uid();
        if(uid.isEmpty()||!catalog.uid.equals(uid)){fail(callback,"VAULT_SIGN_IN_REQUIRED");return;}
        if(!CosmeticVaultProtocol.canDraw(BuildConfig.PAID_COSMETIC_DRAWS_ENABLED,catalog,uid,!pendingRequest().isEmpty())){fail(callback,"INSUFFICIENT_GOLD");return;}
        final String requestId;
        try{
            synchronized(CACHE_LOCK){String key="pending:"+BillingPolicy.accountHash(uid),saved=prefs().getString(key,"");
                requestId=saved.isEmpty()?"cosmetic:"+UUID.randomUUID():saved;
                if(!BillingPolicy.validRequestId(requestId)||!prefs().edit().putString(key,requestId).commit())throw new IllegalStateException();
            }
        }catch(Exception saveFailed){fail(callback,"VAULT_LOCAL_SAVE_FAILED");return;}
        try{request("POST","/v1/wallet/cosmetic-draw",new JSONObject().put("requestId",requestId),uid,new Callback<JSONObject>(){
            public void onSuccess(JSONObject value){try{
                if(!uid.equals(uid())){callback.onError("VAULT_SIGN_IN_REQUIRED");return;}
                CosmeticVaultProtocol.Receipt receipt=CosmeticVaultProtocol.receipt(uid,requestId,value);
                // A verified result is delivered even if this optional cache cannot persist:
                // the server receipt remains authoritative, and no blind automatic redraw follows.
                cache(uid,receipt.version,receipt.owned,requestId);callback.onSuccess(receipt);
            }catch(Exception invalid){callback.onError("VAULT_INVALID_RESPONSE");}}
            public void onError(String code){callback.onError(code);}
        });}catch(JSONException invalid){fail(callback,"VAULT_INVALID_RESPONSE");}
    }
    private SharedPreferences prefs(){return context.getSharedPreferences(PREFS,0);}
    private void cache(String uid,long version,Set<String> owned,String completed){
        synchronized(CACHE_LOCK){try{
            String key="owned:"+BillingPolicy.accountHash(uid);SharedPreferences.Editor edit=prefs().edit();
            String saved=prefs().getString(key,"");long previous=-1;
            try{if(!saved.isEmpty())previous=CosmeticVaultProtocol.integer(new JSONObject(saved),"version");}catch(Exception invalidCache){/* Replace with the authenticated server response. */}
            if(previous<=version)
                edit.putString(key,new JSONObject().put("version",version).put("owned",new JSONArray(owned)).toString());
            if(completed!=null&&completed.equals(prefs().getString("pending:"+BillingPolicy.accountHash(uid),"")))
                edit.putString("pending:"+BillingPolicy.accountHash(uid),"");
            edit.commit();
        }catch(Exception optionalCache){/* A later authenticated catalogue restores server ownership. */}}
    }
    private void request(String method,String path,JSONObject body,String expectedUid,Callback<JSONObject> callback){
        if(closed)return;
        if(!configured()){fail(callback,"VAULT_NOT_CONFIGURED");return;}
        FirebaseUser user=GoogleAccountController.currentWalletUser(context);
        if(user==null||!expectedUid.equals(user.getUid())){fail(callback,"VAULT_SIGN_IN_REQUIRED");return;}
        final String uid=user.getUid();
        user.getIdToken(false).addOnCompleteListener(task->{
            if(closed)return;
            if(!uid.equals(uid())){fail(callback,"VAULT_SIGN_IN_REQUIRED");return;}
            if(!task.isSuccessful()||task.getResult()==null||task.getResult().getToken()==null||task.getResult().getToken().isEmpty()){fail(callback,"VAULT_SIGN_IN_REQUIRED");return;}
            final String token=task.getResult().getToken();
            try{io.execute(()->execute(method,path,body,token,uid,callback));}
            catch(RejectedExecutionException stopped){if(!closed)fail(callback,"VAULT_UNAVAILABLE");}
        });
    }
    private void execute(String method,String path,JSONObject body,String token,String uid,Callback<JSONObject> callback){
        HttpURLConnection current=null;
        try{
            if(closed)return;
            if(!uid.equals(uid())){fail(callback,"VAULT_SIGN_IN_REQUIRED");return;}
            current=(HttpURLConnection)new URL(BillingPolicy.serverBase(BuildConfig.SOCIAL_API_BASE_URL)+path).openConnection();connection=current;
            current.setInstanceFollowRedirects(false);current.setUseCaches(false);current.setConnectTimeout(12000);current.setReadTimeout(20000);
            current.setRequestMethod(method);current.setRequestProperty("Authorization","Bearer "+token);current.setRequestProperty("Accept","application/json");
            if(body!=null){byte[] bytes=body.toString().getBytes(StandardCharsets.UTF_8);current.setRequestProperty("Content-Type","application/json; charset=utf-8");current.setDoOutput(true);current.setFixedLengthStreamingMode(bytes.length);try(OutputStream output=current.getOutputStream()){output.write(bytes);}}
            int status=current.getResponseCode();InputStream input=status>=200&&status<300?current.getInputStream():current.getErrorStream();
            JSONObject value;
            try(InputStream in=input;ByteArrayOutputStream bytes=new ByteArrayOutputStream()){
                if(in==null)throw new IOException();byte[] buffer=new byte[4096];int n;
                while((n=in.read(buffer))!=-1){if(bytes.size()+n>131072)throw new IOException();bytes.write(buffer,0,n);}
                value=new JSONObject(bytes.toString(StandardCharsets.UTF_8.name()));
            }
            final JSONObject result=value;
            String code=status==401?"VAULT_SIGN_IN_REQUIRED":"VAULT_UNAVAILABLE";
            if(value.optJSONObject("error")!=null)code=value.getJSONObject("error").optString("code",code);
            final String error=code;
            main.post(()->{if(closed)return;if(!uid.equals(uid())){callback.onError("VAULT_SIGN_IN_REQUIRED");return;}if(status>=200&&status<300)callback.onSuccess(result);else callback.onError(error);});
        }catch(Exception failed){main.post(()->{if(!closed)callback.onError(uid.equals(uid())?"VAULT_UNAVAILABLE":"VAULT_SIGN_IN_REQUIRED");});}
        finally{if(current!=null)current.disconnect();if(connection==current)connection=null;}
    }
    private <T>void fail(Callback<T> callback,String code){main.post(()->{if(!closed)callback.onError(code);});}
    public void close(){closed=true;HttpURLConnection active=connection;if(active!=null)active.disconnect();io.shutdownNow();}
}
