package com.androidhit.lumadrift.social;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import com.androidhit.lumadrift.BuildConfig;
import com.androidhit.lumadrift.commerce.BillingPolicy;
import com.androidhit.lumadrift.data.CosmeticCatalog;
import com.androidhit.lumadrift.data.V17Strings;
import com.androidhit.lumadrift.identity.GoogleAccountController;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import org.json.*;
import java.nio.file.*;
import java.util.*;

/** Real server-generated payloads plus controlled JVM auth/persistence adapters; no network. */
public final class CosmeticVaultTest {
    private static int checks;
    private static final String UID="vault-test-user-A";
    private static void check(boolean good,String message){checks++;if(!good)throw new AssertionError(message);}
    private interface Throwing{void run()throws Exception;}
    private static void reject(Throwing action,String message)throws Exception{try{action.run();throw new AssertionError(message);}catch(IllegalArgumentException|JSONException expected){checks++;}}
    private static JSONObject copy(JSONObject value){return new JSONObject(value.toString());}
    private static final class Prefs implements SharedPreferences {
        final Map<String,String> values=new HashMap<>();boolean fail;
        public String getString(String key,String fallback){return values.getOrDefault(key,fallback);}
        public Editor edit(){return new Editor(){final Map<String,String> edits=new HashMap<>();final Set<String> removed=new HashSet<>();
            public Editor putString(String key,String value){edits.put(key,value);return this;}
            public Editor remove(String key){removed.add(key);return this;}
            public boolean commit(){for(String key:removed)values.remove(key);values.putAll(edits);if(fail){fail=false;return false;}return true;}
        };}
    }
    private static final class MemoryContext extends Context {
        final Prefs prefs=new Prefs();
        public SharedPreferences getSharedPreferences(String file,int mode){return prefs;}
    }
    private static final class Capture<T> implements CosmeticVaultClient.Callback<T> {
        T value;String error="";int callbacks;
        public void onSuccess(T result){value=result;callbacks++;}
        public void onError(String code){error=code;callbacks++;}
    }
    private static void drain(){Handler.advanceBy(0);}
    private static void protocol(JSONObject fixtures)throws Exception{
        JSONObject fresh=fixtures.getJSONObject("fresh"),guaranteed=fixtures.getJSONObject("guaranteed"),complete=fixtures.getJSONObject("complete");
        CosmeticVaultProtocol.Catalog normal=CosmeticVaultProtocol.catalog(UID,fresh);
        check(!normal.enabled&&normal.balance==0&&normal.version==0&&normal.owned.isEmpty(),"Fresh catalogue truth has no fabricated balance or ownership");
        check(normal.entries.size()==24&&!normal.guaranteeNext(),"Full real catalogue parsed");
        double total=0;for(CosmeticVaultProtocol.Entry entry:normal.entries){total+=entry.probability;check(entry.probability==entry.item.chanceBasisPoints()/10000.0,"Base item odds match server payload");}
        check(Math.abs(total-1)<1e-12,"Base odds cover all outcomes");
        CosmeticVaultProtocol.Catalog pity=CosmeticVaultProtocol.catalog(UID,guaranteed);
        check(pity.guaranteeNext()&&pity.pity==19&&pity.owned.size()==23,"Guarantee state parsed");
        for(CosmeticVaultProtocol.Entry entry:pity.entries)check(entry.probability==(entry.item.id.equals("gloves_aurora")?1:0),"Actual conditional guarantee odds, not stale base percentages");
        check(!CosmeticVaultProtocol.catalog(UID,complete).guaranteeNext(),"Complete collection cannot promise an unavailable new item");
        for(String field:new String[]{"enabled","balance","version","price","duplicateRefund","guarantee","pity","owned","items","gameplayBenefit","currency"}){
            JSONObject bad=copy(fresh);bad.remove(field);reject(()->CosmeticVaultProtocol.catalog(UID,bad),"Missing catalogue field rejected: "+field);
        }
        for(Object badNumber:new Object[]{100.0,"100",new java.math.BigInteger("18446744073709551716")}){
            JSONObject bad=copy(fresh).put("price",badNumber);reject(()->CosmeticVaultProtocol.catalog(UID,bad),"Fractional/string/overflow price rejected");
        }
        for(String field:new String[]{"balance","version","pity"}){JSONObject bad=copy(fresh).put(field,1.5);reject(()->CosmeticVaultProtocol.catalog(UID,bad),"Exact integer required: "+field);}
        for(JSONObject bad:new JSONObject[]{copy(fresh).put("balance",1_000_000_001L),copy(fresh).put("balance",-1_000_000_001L),copy(fresh).put("version",-1),copy(fresh).put("pity",20),copy(fresh).put("pity",-1),copy(fresh).put("enabled","false"),copy(fresh).put("gameplayBenefit",true),copy(fresh).put("currency","LumaCoins"),copy(fresh).put("duplicateRefund",49),copy(fresh).put("guarantee",21)})reject(()->CosmeticVaultProtocol.catalog(UID,bad),"Changed or invalid terms rejected");
        for(int index=0;index<24;index++){
            JSONObject bad=copy(fresh);bad.getJSONArray("items").getJSONObject(index).put("probability",.999);
            reject(()->CosmeticVaultProtocol.catalog(UID,bad),"Tampered odds rejected at every item");
        }
        JSONObject duplicate=copy(fresh);duplicate.getJSONArray("items").put(1,duplicate.getJSONArray("items").getJSONObject(0));
        reject(()->CosmeticVaultProtocol.catalog(UID,duplicate),"Duplicate catalogue ID rejected");
        reject(()->CosmeticVaultProtocol.catalog(UID,copy(fresh).put("owned",new JSONArray().put("unknown_hat"))),"Unknown entitlements rejected");
        reject(()->CosmeticVaultProtocol.catalog(UID,copy(fresh).put("owned",new JSONArray().put("hat_dew").put("hat_dew"))),"Duplicate entitlements rejected");
        JSONObject enabled=copy(fresh).put("enabled",true).put("balance",500);
        CosmeticVaultProtocol.Catalog live=CosmeticVaultProtocol.catalog(UID,enabled);
        check(!CosmeticVaultProtocol.canDraw(false,live,UID)&&!CosmeticVaultProtocol.canDraw(true,normal,UID),"Each independent closed gate prevents a draw");
        check(CosmeticVaultProtocol.canDraw(true,live,UID)&&!CosmeticVaultProtocol.canDraw(true,live,"other"),"Both gates still require the matching account");
        CosmeticVaultProtocol.Catalog empty=CosmeticVaultProtocol.catalog(UID,copy(enabled).put("balance",0));
        check(!CosmeticVaultProtocol.canDraw(true,empty,UID)&&CosmeticVaultProtocol.canDraw(true,empty,UID,true),"A prior receipt can be resumed after an unknown debit depleted the balance");
        JSONObject result=fixtures.getJSONObject("receipt");
        CosmeticVaultProtocol.Receipt receipt=CosmeticVaultProtocol.receipt(UID,"fixture-draw-01",result);
        check(!receipt.duplicate&&receipt.refund==0&&receipt.owned.contains(receipt.itemId)&&receipt.pity==0,"Real server draw receipt parsed");
        reject(()->CosmeticVaultProtocol.receipt(UID,"different-request",result),"Response cannot acknowledge another request");
        reject(()->CosmeticVaultProtocol.receipt(UID,"fixture-draw-01",copy(result).put("refund",4_294_967_296L)),"Refund integer cannot wrap to zero");
        reject(()->CosmeticVaultProtocol.receipt(UID,"fixture-draw-01",copy(result).put("currency","LumaCoins")),"Receipt never represents earned currency");
        reject(()->CosmeticVaultProtocol.receipt(UID,"fixture-draw-01",copy(result).put("owned",new JSONArray())),"Acquisition without entitlement rejected");
        reject(()->CosmeticVaultProtocol.receipt(UID,"fixture-draw-01",copy(result).put("pity",1)),"New account item resets guarantee counter");
        for(String language:new String[]{"en","fr","de","es","pt","it"}){
            for(String key:CosmeticVaultStrings.keys())check(!CosmeticVaultStrings.get(language,key).isBlank(),"Every vault message is translated");
            for(CosmeticCatalog.Item item:CosmeticCatalog.ITEMS)check(!V17Strings.get(language,item.nameKey).equals(item.nameKey),"Every real cosmetic name is translated");
            check(CosmeticVaultStrings.get(language,"balance",500).contains("500"),"Balance interpolation works in every language");
        }
    }
    private static void client(JSONObject fixture)throws Exception{
        MemoryContext context=new MemoryContext();FirebaseUser user=new FirebaseUser(UID);GoogleAccountController.user=user;
        CosmeticVaultClient client=new CosmeticVaultClient(context);
        CosmeticVaultProtocol.Catalog live=CosmeticVaultProtocol.catalog(UID,copy(fixture).put("enabled",true).put("balance",500));
        for(int i=0;i<1000;i++){Capture<CosmeticVaultProtocol.Receipt> capture=new Capture<>();client.draw(live,capture);drain();check("PAID_COSMETIC_DRAWS_DISABLED".equals(capture.error)&&capture.callbacks==1,"Immutable build-off path stops before auth/request persistence");}
        check(user.tokenCalls==0&&context.prefs.values.isEmpty(),"Closed build never requests a token or creates a paid pending request");
        BuildConfig.PAID_COSMETIC_DRAWS_ENABLED=true; // JVM stub only, never Android BuildConfig.
        Capture<CosmeticVaultProtocol.Receipt> denied=new Capture<>();client.draw(CosmeticVaultProtocol.catalog(UID,fixture),denied);drain();
        check("PAID_COSMETIC_DRAWS_DISABLED".equals(denied.error)&&user.tokenCalls==0,"Server-off flag independently blocks requests");
        Capture<CosmeticVaultProtocol.Receipt> first=new Capture<>();client.draw(live,first);drain();String original=client.pendingRequest();
        check(BillingPolicy.validRequestId(original)&&"VAULT_SIGN_IN_REQUIRED".equals(first.error),"Failed test authentication leaves a durable request identity");
        for(int repeat=0;repeat<20;repeat++){Capture<CosmeticVaultProtocol.Receipt> retry=new Capture<>();client.draw(live,retry);drain();check(original.equals(client.pendingRequest()),"Every failure/retry retains its original receipt");}
        client.close();client=new CosmeticVaultClient(context);check(original.equals(client.pendingRequest()),"Client recreation resumes the same pending request");
        CosmeticVaultProtocol.Catalog depleted=CosmeticVaultProtocol.catalog(UID,copy(fixture).put("enabled",true).put("balance",0));
        int calls=user.tokenCalls;Capture<CosmeticVaultProtocol.Receipt> resume=new Capture<>();client.draw(depleted,resume);drain();
        check(user.tokenCalls==calls+1&&original.equals(client.pendingRequest()),"An uncertain completed debit can still retrieve the old receipt at zero balance");
        context.prefs.values.clear();context.prefs.fail=true;calls=user.tokenCalls;
        Capture<CosmeticVaultProtocol.Receipt> saveFailure=new Capture<>();client.draw(live,saveFailure);drain();String memoryId=client.pendingRequest();
        check("VAULT_LOCAL_SAVE_FAILED".equals(saveFailure.error)&&calls==user.tokenCalls,"Memory-before-disk failure blocks any token/network work");
        Capture<CosmeticVaultProtocol.Receipt> afterSave=new Capture<>();client.draw(live,afterSave);drain();
        check(!memoryId.isEmpty()&&memoryId.equals(client.pendingRequest()),"Save retry recommits the same in-memory request before auth");

        Task<GetTokenResult> late=new Task<>(true,new GetTokenResult("never-transmitted-fixture-token"),true);user.nextTask=late;
        Capture<CosmeticVaultProtocol.Receipt> changed=new Capture<>();client.draw(live,changed);
        GoogleAccountController.user=new FirebaseUser("vault-test-user-B");late.complete();drain();
        check("VAULT_SIGN_IN_REQUIRED".equals(changed.error)&&client.pendingRequest().isEmpty(),"A token completed after account change cannot be sent or expose the old request");
        context.prefs.values.put("owned:"+BillingPolicy.accountHash(UID),new JSONObject().put("version",2).put("owned",new JSONArray().put("hat_cupid")).toString());
        check(!CosmeticVaultClient.ownsCosmetic(context,"hat_cupid"),"Account B cannot borrow account A ownership");
        GoogleAccountController.user=user;check(CosmeticVaultClient.ownsCosmetic(context,"hat_cupid"),"Matching account can use validated cached entitlements");
        GoogleAccountController.user=null;check(!CosmeticVaultClient.ownsCosmetic(context,"hat_cupid"),"Guest session cannot use an account cosmetic cache");
        GoogleAccountController.user=user;CosmeticVaultClient.clearCache(context,UID);check(!CosmeticVaultClient.ownsCosmetic(context,"hat_cupid"),"Logout/deletion cache clear removes optional account ownership");
        context.prefs.values.put("owned:"+BillingPolicy.accountHash(UID),"malformed");check(!CosmeticVaultClient.ownsCosmetic(context,"hat_cupid"),"Corrupt cache never grants ownership");
        Task<GetTokenResult> afterClose=new Task<>(true,new GetTokenResult("never-transmitted-fixture-token"),true);user.nextTask=afterClose;
        Capture<CosmeticVaultProtocol.Receipt> released=new Capture<>();client.draw(live,released);client.close();afterClose.complete();drain();
        check(released.callbacks==0,"Destruction suppresses a late token callback");
        BuildConfig.PAID_COSMETIC_DRAWS_ENABLED=false;GoogleAccountController.user=null;
    }
    public static void main(String[] args)throws Exception{
        JSONObject fixtures=new JSONObject(Files.readString(Paths.get(args[0])));
        protocol(fixtures);client(fixtures.getJSONObject("fresh"));
        System.out.println("PASS "+checks+" cosmetic vault protocol/client model checks: actual offline server fixtures, strict conditional odds, 24 entitlements, six languages, independent paid gates, 1000 blocked requests, pending retry/save races, UID isolation and close guards.");
        System.out.println("No real sign-in, ID token, HTTPS request, purchase, LumaGold debit or device rendering occurred. Mutable switches belong only to the JVM test BuildConfig adapter.");
    }
}
