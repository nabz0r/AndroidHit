package com.androidhit.lumadrift.data;
/** Explicit JVM energy-store double. Real persistence is qualified by the store's own suite. */
public final class SessionEnergyStore {
    public int count,credits;
    public Runnable afterCredit;
    private final java.util.Set<String> receipts=new java.util.HashSet<>();
    public static final class Snapshot {public final int count;Snapshot(int count){this.count=count;}}
    public Snapshot snapshot(long now){return new Snapshot(count);}
    public boolean hasReceipt(String hash){return receipts.contains(hash);}
    public boolean creditReceipt(String hash,int amount,long now){
        if(!receipts.add(hash))return false;
        if(count!=0||amount!=10)throw new AssertionError("A paid pack cannot be truncated");
        count=10;credits++;if(afterCredit!=null)afterCredit.run();return true;
    }
}
