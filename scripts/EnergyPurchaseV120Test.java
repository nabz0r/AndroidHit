package com.androidhit.lumadrift.wallet;

import android.content.Context;
import android.content.SharedPreferences;
import com.androidhit.lumadrift.commerce.BillingPolicy;
import com.androidhit.lumadrift.commerce.CommerceConfig;
import com.androidhit.lumadrift.data.SessionEnergyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public final class EnergyPurchaseV120Test {
    static int checks;
    static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    static final class Prefs implements SharedPreferences {
        final Map<String,Object> memory=new HashMap<>(),disk=new HashMap<>();int fail;
        Prefs reboot(){Prefs p=new Prefs();p.memory.putAll(disk);p.disk.putAll(disk);return p;}
        public Map<String,?> getAll(){return new HashMap<>(memory);}
        public boolean contains(String key){return memory.containsKey(key);}
        public int getInt(String k,int d){return (Integer)memory.getOrDefault(k,d);}
        public String getString(String k,String d){return (String)memory.getOrDefault(k,d);}
        public boolean getBoolean(String k,boolean d){return (Boolean)memory.getOrDefault(k,d);}
        public Editor edit(){return new Editor(){final Map<String,Object> changes=new HashMap<>();
            public Editor putInt(String k,int v){changes.put(k,v);return this;}
            public Editor putString(String k,String v){changes.put(k,v);return this;}
            public Editor putBoolean(String k,boolean v){changes.put(k,v);return this;}
            public boolean commit(){memory.putAll(changes);if(fail>0){fail--;return false;}disk.putAll(changes);return true;}
        };}
    }
    static final class Ctx extends Context {
        final Prefs prefs;Ctx(){this(new Prefs());}Ctx(Prefs p){prefs=p;}
        public SharedPreferences getSharedPreferences(String name,int mode){check(name.equals("luma_energy_purchases_v120"),"Dedicated premium prefs only");return prefs;}
    }
    static final class Events implements EnergyPurchaseController.Listener {
        int changes;final List<String> errors=new ArrayList<>();
        public void onChanged(){changes++;}public void onError(String key){errors.add(key);}
    }
    static void reset(){ProductionWalletClient.created.clear();ProductionWalletClient.currentUid="google-A";ProductionWalletClient.configured=true;CommerceConfig.ready=true;CommerceConfig.MODE_SANDBOX=false;}
    static EnergyPurchaseController controller(Ctx context,SessionEnergyStore e,Events events){return new EnergyPurchaseController(context,e,events,false,true);}
    static ProductionWalletClient latest(){return ProductionWalletClient.created.get(ProductionWalletClient.created.size()-1);}
    static JSONObject receipt(String uid,String id,int amount)throws Exception{return new JSONObject().put("id",id).put("sku","energy_refill10").put("kind","energy").put("itemId","energy_refill10").put("amount",amount).put("owner",BillingPolicy.accountHash(uid));}
    static ProductionWalletClient.Wallet reply(String uid,long balance,long version,int stock,String id,boolean use)throws Exception{
        JSONObject r=receipt(uid,id,10);return new ProductionWalletClient.Wallet(uid,balance,version,new JSONObject().put("energyRefills",stock),use?null:r,use?r:null);
    }
    static void gates(){
        reset();Ctx ctx=new Ctx();SessionEnergyStore e=new SessionEnergyStore();Events events=new Events();
        ProductionWalletClient.currentUid="";EnergyPurchaseController guest=controller(ctx,e,events);guest.onForeground();check(ProductionWalletClient.created.isEmpty(),"Guest starts no network");guest.buyRefill();check(ProductionWalletClient.created.isEmpty(),"Guest cannot buy");guest.close();
        ProductionWalletClient.currentUid="google-A";EnergyPurchaseController closed=new EnergyPurchaseController(ctx,e,events);closed.onForeground();closed.buyRefill();check(!closed.available()&&ProductionWalletClient.created.isEmpty(),"Public production gate is closed");closed.close();
        e.count=1;EnergyPurchaseController paid=controller(ctx,e,events);paid.buyRefill();check(ProductionWalletClient.created.isEmpty(),"Buying above zero does not debit");check(events.errors.contains("energy_only_empty"),"Zero-only rule readable");paid.close();
    }
    static void retries()throws Exception{
        reset();Ctx ctx=new Ctx();SessionEnergyStore e=new SessionEnergyStore();Events events=new Events();EnergyPurchaseController a=controller(ctx,e,events);
        a.buyRefill();ProductionWalletClient one=latest();String spend=one.spends.get(0).id;a.buyRefill();check(one.spends.size()==1,"Busy double tap cannot create a second spend");
        one.spends.get(0).callback.onError("wallet_unavailable");a.close();
        EnergyPurchaseController b=controller(ctx,e,events);b.onForeground();ProductionWalletClient two=latest();check(two.spends.get(0).id.equals(spend),"Restart reuses spend UUID after ambiguous debit");
        two.spends.get(0).callback.onSuccess(reply("google-A",450,2,1,spend,false));String use=two.uses.get(0).id;
        two.uses.get(0).callback.onError("wallet_unavailable");b.close();
        EnergyPurchaseController c=controller(ctx,e,events);c.onForeground();ProductionWalletClient three=latest();check(three.spends.isEmpty()&&three.uses.get(0).id.equals(use),"Restart reuses use UUID without buying again");
        three.uses.get(0).callback.onSuccess(reply("google-A",450,3,0,use,true));check(e.count==10&&e.credits==1&&c.ownedRefills()==0,"Exactly ten energy and no remaining pack");
        three.uses.get(0).callback.onSuccess(reply("google-A",450,3,0,use,true));check(e.credits==1,"Duplicate response cannot duplicate local credit");
        check(new EnergyPurchaseJournal(ctx.prefs).state("google-A").phase.equals("EMPTY"),"Outbox acknowledged only after energy receipt");c.close();
    }
    static void lateRefill()throws Exception{
        reset();Ctx ctx=new Ctx();SessionEnergyStore e=new SessionEnergyStore();Events events=new Events();EnergyPurchaseController c=controller(ctx,e,events);c.buyRefill();ProductionWalletClient w=latest();
        String buy=w.spends.get(0).id;e.count=1;w.spends.get(0).callback.onSuccess(reply("google-A",450,2,1,buy,false));check(w.uses.isEmpty()&&c.ownedRefills()==1,"Regeneration preserves full purchased stock instead of truncating");
        c.useStoredRefill();check(w.uses.isEmpty(),"Stock is not consumed above zero");e.count=0;c.useStoredRefill();String use=w.uses.get(0).id;
        e.count=2;w.uses.get(0).callback.onSuccess(reply("google-A",450,3,0,use,true));check(e.credits==0&&c.ownedRefills()==1,"Late use receipt stays durable while energy is nonempty");
        c.close();EnergyPurchaseController restarted=controller(ctx,e,events);e.count=0;restarted.onForeground();check(e.count==10&&e.credits==1&&restarted.ownedRefills()==0,"Deferred receipt survives recreation and grants all ten");restarted.close();
    }
    static void accountsAndMalformed()throws Exception{
        reset();Ctx ctx=new Ctx();SessionEnergyStore e=new SessionEnergyStore();Events events=new Events();EnergyPurchaseController c=controller(ctx,e,events);c.buyRefill();ProductionWalletClient a=latest();String buy=a.spends.get(0).id;
        ProductionWalletClient.currentUid="google-B";c.onForeground();a.spends.get(0).callback.onSuccess(reply("google-A",450,2,1,buy,false));check(e.credits==0&&c.ownedRefills()==0&&a.closed,"Late A callback cannot affect B");
        ProductionWalletClient.currentUid="google-A";c.onForeground();ProductionWalletClient back=latest();check(back.spends.get(0).id.equals(buy),"Original account keeps its original pending purchase");
        back.spends.get(0).callback.onSuccess(reply("google-A",450,2,1,buy,false));String use=back.uses.get(0).id;
        ProductionWalletClient.Wallet forged=reply("google-A",450,3,0,use,true);forged.use.put("owner",BillingPolicy.accountHash("google-B"));back.uses.get(0).callback.onSuccess(forged);check(e.credits==0,"Wrong-owner receipt cannot grant energy");
        c.useStoredRefill();ProductionWalletClient.Call retry=back.uses.get(1);check(retry.id.equals(use),"Malformed response retains original use request");
        ProductionWalletClient.Wallet shortPack=reply("google-A",450,3,0,use,true);shortPack.use.put("amount",9);retry.callback.onSuccess(shortPack);check(e.credits==0,"Partial paid amount is rejected");
        c.useStoredRefill();back.uses.get(2).callback.onSuccess(reply("google-A",450,3,0,use,true));check(e.credits==1,"Valid original receipt can still recover");c.close();
        check(!EnergyPurchaseJournal.receiptHash("google-A",use).equals(EnergyPurchaseJournal.receiptHash("google-B",use)),"Local receipt hashes bind exact UID and use UUID");
    }
    static void failedPersistence()throws Exception{
        reset();Ctx ctx=new Ctx();SessionEnergyStore e=new SessionEnergyStore();Events events=new Events();ctx.prefs.fail=1;EnergyPurchaseController c=controller(ctx,e,events);c.buyRefill();check(ProductionWalletClient.created.isEmpty(),"Failed pre-request disk commit authorizes no network");
        c.buyRefill();ProductionWalletClient w=latest();String buy=w.spends.get(0).id;w.spends.get(0).callback.onSuccess(reply("google-A",450,2,1,buy,false));String use=w.uses.get(0).id;
        e.afterCredit=()->ctx.prefs.fail=1;w.uses.get(0).callback.onSuccess(reply("google-A",450,3,0,use,true));check(e.credits==1&&events.errors.contains("commerce_save_error"),"Credit survives failed acknowledgement");c.close();
        Ctx reboot=new Ctx(ctx.prefs.reboot());e.afterCredit=null;ProductionWalletClient.created.clear();EnergyPurchaseController r=controller(reboot,e,events);r.onForeground();check(e.credits==1&&e.count==10,"Crash after credit does not grant twice");
        check(ProductionWalletClient.created.isEmpty(),"Already credited receipt is acknowledged locally without HTTP");check(new EnergyPurchaseJournal(reboot.prefs).state("google-A").phase.equals("EMPTY"),"Credit marker settles old journal even at full energy");r.close();
    }
    static void labAndJournal()throws Exception{
        reset();ProductionWalletClient.currentUid="";Ctx ctx=new Ctx();SessionEnergyStore e=new SessionEnergyStore();Events events=new Events();EnergyPurchaseController lab=new EnergyPurchaseController(ctx,e,events,true,false);
        lab.onForeground();check(lab.testGoldBalance()==500&&lab.isTestMode(),"Lab seeds clearly separate premium test Gold");lab.buyRefill();check(lab.testGoldBalance()==450&&e.credits==1&&e.count==10,"Lab pays50 test Gold and receives10 once");check(ProductionWalletClient.created.isEmpty(),"Lab never initializes production wallet or Google");
        for(String language:new String[]{"fr","en","de","es","it","pt"})check(!lab.statusLabel(language).isBlank(),"Readable Lab label "+language);lab.close();
        EnergyPurchaseJournal journal=new EnergyPurchaseJournal(new Prefs());List<EnergyPurchaseJournal.State> states=java.util.Collections.synchronizedList(new ArrayList<>());List<Thread> threads=new ArrayList<>();
        for(int i=0;i<32;i++){Thread t=new Thread(()->states.add(journal.start("google-A",true)));threads.add(t);t.start();}for(Thread t:threads)t.join();for(EnergyPurchaseJournal.State state:states)check(state.same(states.get(0)),"Concurrent controllers share one durable request");
        journal.snapshot("google-A",0,450,3);journal.snapshot("google-A",1,500,2);check(journal.stock("google-A")==0&&journal.balance("google-A")==450,"Stale wallet response cannot restore used stock");
    }
    static void otherDeviceConsumes()throws Exception{
        reset();Ctx ctx=new Ctx();SessionEnergyStore e=new SessionEnergyStore();Events events=new Events();EnergyPurchaseController c=controller(ctx,e,events);c.buyRefill();ProductionWalletClient w=latest();String buy=w.spends.get(0).id;
        w.spends.get(0).callback.onSuccess(reply("google-A",450,2,1,buy,false));
        w.uses.get(0).callback.onError("wallet_item_insufficient");
        check(c.ownedRefills()==0&&new EnergyPurchaseJournal(ctx.prefs).state("google-A").phase.equals("EMPTY"),"Definitive stock refusal settles an unconsumed request");
        c.buyRefill();check(w.spends.size()==2&&!w.spends.get(1).id.equals(buy),"A pack used on another device cannot trap future purchases");c.close();
    }
    public static void main(String[] args)throws Exception{gates();retries();lateRefill();accountsAndMalformed();failedPersistence();labAndJournal();otherDeviceConsumes();System.out.println("Energy purchase1.20: "+checks+" checks passed (real controller/journal, explicit HTTP/energy doubles; no real payment)");}
}
