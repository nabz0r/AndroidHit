package com.androidhit.lumadrift.data;

import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.Map;

/** Device-local session pacing, separate from companion skill charge and both currencies.
 * A pending debit is reconciled against the durable board run after process recreation.
 * This is not an authoritative anti-cheat or cross-device energy account.
 */
public final class SessionEnergyStore {
    public static final int MAX=10;
    public static final long REFILL_MS=600_000L;
    private static final String KEY="session_energy_v120";
    private final SharedPreferences prefs;
    public static final class Snapshot {
        public final int count; public final long nextInMs;
        Snapshot(int count,long nextInMs){this.count=count;this.nextInMs=nextInMs;}
    }
    private static final class State {
        int count;long anchor;String pending="",last="";
        State(int count,long anchor){this.count=count;this.anchor=anchor;}
        String encode(){return "1|"+count+"|"+anchor+"|"+pending+"|"+last;}
    }
    public SessionEnergyStore(SharedPreferences prefs){if(prefs==null)throw new IllegalArgumentException("Preferences required");this.prefs=prefs;}
    public static boolean costsEnergy(int mode,int level){return mode!=4&&(mode!=0||level>10);}
    private State read(long now){
        if(now<0)throw new IllegalArgumentException("Positive clock required");
        LocalSaveTransactions.flush(prefs);
        String raw=prefs.getString(KEY,"");if(raw.isEmpty())return new State(MAX,now);
        try{
            String[] v=raw.split("\\|",-1);if(v.length!=5||!v[0].equals("1"))throw new IllegalArgumentException();
            State s=new State(Integer.parseInt(v[1]),Long.parseLong(v[2]));s.pending=v[3];s.last=v[4];
            if(s.count<0||s.count>MAX||s.anchor<0||!optionalId(s.pending)||!optionalId(s.last))throw new IllegalArgumentException();
            // Keep the high-water anchor on clock rollback; returning to the old
            // time must not earn the same refill interval twice.
            if(s.count<MAX){long ticks=(now-s.anchor)/REFILL_MS;if(ticks>0){s.count=(int)Math.min(MAX,s.count+Math.min(MAX,ticks));s.anchor=s.count==MAX?now:s.anchor+ticks*REFILL_MS;}}
            return s;
        }catch(RuntimeException malformed){throw new IllegalStateException("Invalid energy save",malformed);}
    }
    private static boolean optionalId(String id){return id.isEmpty()||id.matches("[a-f0-9-]{36}");}
    private void write(State s){Map<String,Object> values=new HashMap<>();values.put(KEY,s.encode());LocalSaveTransactions.write(prefs,values);}
    public synchronized Snapshot snapshot(long now){State s=read(now);writeIfChanged(s);return new Snapshot(s.count,s.count==MAX?0:Math.min(REFILL_MS,Math.max(1,REFILL_MS-(now-s.anchor))));}
    private void writeIfChanged(State s){if(!s.encode().equals(prefs.getString(KEY,"")))write(s);}
    /** Idempotent for the current run. Never reserve a second board while the first is pending. */
    public synchronized boolean reserve(String runId,long now){
        if(!optionalId(runId)||runId.isEmpty())throw new IllegalArgumentException("Run UUID required");
        State s=read(now);if(runId.equals(s.pending)||runId.equals(s.last))return true;
        if(!s.pending.isEmpty()||s.count==0){writeIfChanged(s);return false;}
        if(s.count==MAX)s.anchor=now;s.count--;s.pending=runId;write(s);return true;
    }
    public synchronized void confirm(String runId,long now){State s=read(now);if(s.pending.equals(runId)&&!runId.isEmpty()){s.last=runId;s.pending="";write(s);}}
    /** Call once on startup, using the last successfully persisted normal/expedition board UUIDs. */
    public synchronized void recover(String normalRun,String expeditionRun,long now){
        State s=read(now);if(s.pending.isEmpty()){writeIfChanged(s);return;}
        if(s.pending.equals(normalRun)||s.pending.equals(expeditionRun))s.last=s.pending;
        else s.count=Math.min(MAX,s.count+1);
        s.pending="";if(s.count==MAX)s.anchor=now;write(s);
    }
    /** Verified paid entitlement only. Receipt keys are account-bound hashes, never raw tokens. */
    public synchronized boolean hasReceipt(String receiptHash){
        if(receiptHash==null||!receiptHash.matches("[a-f0-9]{64}"))throw new IllegalArgumentException("Receipt hash required");
        LocalSaveTransactions.flush(prefs);return prefs.getBoolean("energy_receipt_"+receiptHash,false);
    }
    public synchronized boolean creditReceipt(String receiptHash,int amount,long now){
        if(receiptHash==null||!receiptHash.matches("[a-f0-9]{64}")||amount<1||amount>MAX)throw new IllegalArgumentException("Verified bounded receipt required");
        State s=read(now);String key="energy_receipt_"+receiptHash;
        if(prefs.getBoolean(key,false))return false;
        Map<String,Object> values=new HashMap<>();s.count=Math.min(MAX,s.count+amount);if(s.count==MAX)s.anchor=now;
        values.put(KEY,s.encode());values.put(key,true);LocalSaveTransactions.write(prefs,values);return true;
    }
}
