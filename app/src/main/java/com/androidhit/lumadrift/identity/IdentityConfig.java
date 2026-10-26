package com.androidhit.lumadrift.identity;

import android.content.Context;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** Public OAuth/Firebase identifiers only; no service-account key belongs in an APK. */
public final class IdentityConfig {
    public final String webClientId,apiKey,appId,projectId;
    private IdentityConfig(String web,String key,String app,String project){webClientId=web;apiKey=key;appId=app;projectId=project;}
    public static IdentityConfig load(Context context){
        try(InputStream in=context.getAssets().open("identity-config.json")){
            ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[2048];int n;
            while((n=in.read(buffer))!=-1){if(out.size()+n>16384)return empty();out.write(buffer,0,n);}
            JSONObject json=new JSONObject(out.toString(StandardCharsets.UTF_8.name()));
            return new IdentityConfig(json.optString("webClientId","").trim(),json.optString("apiKey","").trim(),json.optString("appId","").trim(),json.optString("projectId","").trim());
        }catch(Exception unavailable){return empty();}
    }
    private static IdentityConfig empty(){return new IdentityConfig("","","","");}
    public boolean isConfigured(){return webClientId.matches("[A-Za-z0-9_-]{10,}\\.apps\\.googleusercontent\\.com")
        && apiKey.matches("AIza[A-Za-z0-9_-]{30,60}")&&appId.matches("1:[0-9]+:android:[a-fA-F0-9]+")
        &&projectId.matches("[a-z][a-z0-9-]{4,60}")&&!projectId.startsWith("example")&&!projectId.startsWith("demo-");}
}
