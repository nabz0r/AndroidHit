package com.androidhit.lumadrift.wallet;

import android.content.SharedPreferences;
import com.androidhit.lumadrift.commerce.BillingPolicy;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/** Durable account-scoped outbox. A failed preference commit never authorizes a request. */
final class EnergyPurchaseJournal {
    static final String SKU="energy_refill10", LAB_UID="luma-energy-premium-test-v120";
    static final int PRICE=50, AMOUNT=10;
    private static final Object LOCK=new Object();
    private static final Map<SharedPreferences,Map<String,Object>> DIRTY=new WeakHashMap<>();
    private final SharedPreferences prefs;
    static final class State {
        final String phase,spendId,useId;
        State(String phase,String spendId,String useId){this.phase=phase;this.spendId=spendId;this.useId=useId;}
        boolean pending(){return !phase.equals("EMPTY");}
        String encode(){return "1|"+phase+"|"+spendId+"|"+useId;}
        boolean same(State other){return other!=null&&spendId.equals(other.spendId)&&useId.equals(other.useId)&&phase.equals(other.phase);}
    }
    EnergyPurchaseJournal(SharedPreferences prefs){this.prefs=prefs;}
    private static String prefix(String uid){return "account_"+BillingPolicy.accountHash(uid)+"_";}
    private static boolean uuid(String value){return value.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");}
    private void flush(){
        Map<String,Object> values=DIRTY.get(prefs);if(values==null)return;
        SharedPreferences.Editor edit=prefs.edit();
        for(Map.Entry<String,Object> e:values.entrySet()){
            Object v=e.getValue();if(v instanceof String)edit.putString(e.getKey(),(String)v);
            else if(v instanceof Integer)edit.putInt(e.getKey(),(Integer)v);
            else if(v instanceof Boolean)edit.putBoolean(e.getKey(),(Boolean)v);
            else throw new IllegalArgumentException("Unsupported energy journal type");
        }
        if(!edit.commit())throw new IllegalStateException("Energy purchase save unavailable");
        DIRTY.remove(prefs);
    }
    private void write(Map<String,Object> values){flush();DIRTY.put(prefs,new HashMap<>(values));flush();}
    State state(String uid){synchronized(LOCK){
        flush();String raw=prefs.getString(prefix(uid)+"outbox","");
        if(raw.isEmpty())return new State("EMPTY","","");
        String[] v=raw.split("\\|",-1);
        if(v.length!=4||!v[0].equals("1")||!v[1].matches("SPEND|USE|CREDIT")||!uuid(v[3])||(!v[2].isEmpty()&&!uuid(v[2]))||v[1].equals("SPEND")&&v[2].isEmpty())throw new IllegalStateException("Invalid energy purchase journal");
        return new State(v[1],v[2],v[3]);
    }}
    State start(String uid,boolean buying){synchronized(LOCK){
        State old=state(uid);if(old.pending())return old;
        State next=new State(buying?"SPEND":"USE",buying?UUID.randomUUID().toString():"",UUID.randomUUID().toString());
        write(Map.of(prefix(uid)+"outbox",next.encode()));return next;
    }}
    boolean advance(String uid,State expected,String phase){synchronized(LOCK){
        State old=state(uid);if(!old.same(expected))return false;
        if(!(old.phase.equals("SPEND")&&phase.equals("USE")||old.phase.equals("USE")&&phase.equals("CREDIT")))throw new IllegalArgumentException("Invalid energy phase");
        write(Map.of(prefix(uid)+"outbox",new State(phase,old.spendId,old.useId).encode()));return true;
    }}
    boolean clear(String uid,State expected){synchronized(LOCK){
        if(!state(uid).same(expected)||!expected.phase.equals("CREDIT"))return false;
        write(Map.of(prefix(uid)+"outbox",""));return true;
    }}
    /** The server checks old receipts before inventory, so this definitive refusal spent nothing. */
    boolean discardUnconsumedUse(String uid,State expected){synchronized(LOCK){
        if(!state(uid).same(expected)||!expected.phase.equals("USE"))return false;
        write(Map.of(prefix(uid)+"outbox","",prefix(uid)+"stock",0));return true;
    }}
    void snapshot(String uid,int stock,long balance,long version){synchronized(LOCK){
        if(stock<0||stock>10000||balance< -1000000000L||balance>1000000000L||version<0)throw new IllegalArgumentException("Invalid energy wallet");
        flush();String p=prefix(uid);
        if(Long.parseLong(prefs.getString(p+"version","-1"))>version)return;
        write(Map.of(p+"stock",stock,p+"balance",(int)balance,p+"version",Long.toString(version)));
    }}
    int stock(String uid){synchronized(LOCK){flush();return prefs.getInt(prefix(uid)+"stock",0);}}
    int balance(String uid){synchronized(LOCK){flush();return prefs.getInt(prefix(uid)+"balance",0);}}
    void seedLab(){synchronized(LOCK){
        flush();String p=prefix(LAB_UID);if(!prefs.getBoolean(p+"seeded",false))write(Map.of(p+"seeded",true,p+"balance",500,p+"stock",0));
    }}
    void labSpend(State expected){synchronized(LOCK){
        seedLab();if(!state(LAB_UID).same(expected)||!expected.phase.equals("SPEND"))return;
        String p=prefix(LAB_UID),key=p+"spent_"+expected.spendId;
        if(prefs.getBoolean(key,false))return;
        int balance=balance(LAB_UID),stock=stock(LAB_UID);
        if(balance<PRICE)throw new IllegalArgumentException("wallet_insufficient");
        if(stock>=10000)throw new IllegalArgumentException("energy_inventory_limit");
        write(Map.of(p+"balance",balance-PRICE,p+"stock",stock+1,key,true));
    }}
    void labUse(State expected){synchronized(LOCK){
        seedLab();if(!state(LAB_UID).same(expected)||!expected.phase.equals("USE"))return;
        String p=prefix(LAB_UID),key=p+"used_"+expected.useId;
        if(prefs.getBoolean(key,false))return;
        int stock=stock(LAB_UID);if(stock<1)throw new IllegalArgumentException("energy_no_pack");
        write(Map.of(p+"stock",stock-1,key,true));
    }}
    static String receiptHash(String uid,String useId){
        if(!uuid(useId))throw new IllegalArgumentException("Use UUID required");
        BillingPolicy.accountHash(uid);
        try{
            byte[] digest=MessageDigest.getInstance("SHA-256").digest((SKU+"\n"+uid+"\n"+useId).getBytes(StandardCharsets.UTF_8));
            StringBuilder out=new StringBuilder(64);for(byte value:digest)out.append(Character.forDigit((value&255)>>>4,16)).append(Character.forDigit(value&15,16));return out.toString();
        }catch(java.security.NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}
    }
}
