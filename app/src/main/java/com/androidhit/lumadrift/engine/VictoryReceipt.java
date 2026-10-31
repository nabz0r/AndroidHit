package com.androidhit.lumadrift.engine;

/** A run-bound display receipt. It never grants currency, stars or XP. */
public final class VictoryReceipt {
    public static final String VERSION="v120_1";
    public static final String RANK_VERSION="v124_1";
    public final String runId,period,avatarId;
    public final int mode,level,event,stars,beforeCoins,beforeXp,coins,xp,used,total,corrections;
    public final long elapsedMs;
    public final boolean ready;
    public final boolean rankAware;
    public final String captainId,rankRewardStatus;
    public final int rankBefore,rankAfter,rankCopies,rankCoins;
    private VictoryReceipt(boolean ready,String runId,int mode,int level,int event,String period,int stars,
                           int beforeCoins,int beforeXp,int coins,int xp,long elapsedMs,int used,int total,int corrections,String avatarId){
        this(ready,runId,mode,level,event,period,stars,beforeCoins,beforeXp,coins,xp,elapsedMs,used,total,corrections,avatarId,false,"",0,0,0,0,"LEGACY");
    }
    private VictoryReceipt(boolean ready,String runId,int mode,int level,int event,String period,int stars,
                           int beforeCoins,int beforeXp,int coins,int xp,long elapsedMs,int used,int total,int corrections,String avatarId,
                           boolean rankAware,String captainId,int rankBefore,int rankAfter,int rankCopies,int rankCoins,String rankStatus){
        this.rankAware=rankAware;this.captainId=captainId;this.rankBefore=rankBefore;this.rankAfter=rankAfter;this.rankCopies=rankCopies;this.rankCoins=rankCoins;rankRewardStatus=rankStatus;
        this.ready=ready;this.runId=runId;this.mode=mode;this.level=level;this.event=event;this.period=period;this.stars=stars;
        this.beforeCoins=beforeCoins;this.beforeXp=beforeXp;this.coins=coins;this.xp=xp;this.elapsedMs=elapsedMs;
        this.used=used;this.total=total;this.corrections=corrections;this.avatarId=avatarId;
    }
    public static VictoryReceipt pending(String runId,int mode,int level,int event,String period,int stars,
                                         int beforeCoins,int beforeXp,long elapsedMs,int used,int total,int corrections,String avatarId){
        VictoryReceipt value=new VictoryReceipt(false,runId,mode,level,event,period,stars,beforeCoins,beforeXp,0,0,elapsedMs,used,total,corrections,avatarId);
        if(!valid(value))throw new IllegalArgumentException("Valid solved-run receipt required");return value;
    }
    /** New runs persist the actually played team captain before any reward is applied. Empty means legacy/no durable captain. */
    public static VictoryReceipt pending124(String runId,int mode,int level,int event,String period,int stars,
                                         int beforeCoins,int beforeXp,long elapsedMs,int used,int total,int corrections,String avatarId,String captainId){
        VictoryReceipt value=new VictoryReceipt(false,runId,mode,level,event,period,stars,beforeCoins,beforeXp,0,0,elapsedMs,used,total,corrections,avatarId,true,captainId,0,0,0,0,"PENDING");
        if(!valid(value))throw new IllegalArgumentException("Valid solved-run and captain snapshot required");return value;
    }
    /** Display metadata only; the coordinator/store must already have durably fulfilled these awards. */
    public VictoryReceipt withRankReward(String captain,int fromRank,int toRank,int copies,int compensationCoins,String status){
        if(ready)return this;
        if(rankAware&&!captainId.equals(captain))throw new IllegalStateException("Victory captain snapshot changed");
        if(!rankAware&&!captain.isEmpty())throw new IllegalStateException("Legacy victory has no durable captain");
        VictoryReceipt value=new VictoryReceipt(false,runId,mode,level,event,period,stars,beforeCoins,beforeXp,0,0,elapsedMs,used,total,corrections,avatarId,true,captain,fromRank,toRank,copies,compensationCoins,status);
        if(!valid(value))throw new IllegalArgumentException("Valid rank reward result required");return value;
    }
    /** Capture the actual durable balance deltas after idempotent award calls. */
    public VictoryReceipt settle(int currentCoins,int currentXp){
        if(ready)return this;
        if(currentCoins<beforeCoins||currentXp<beforeXp)throw new IllegalStateException("Victory balances moved backwards");
        VictoryReceipt value=new VictoryReceipt(true,runId,mode,level,event,period,stars,beforeCoins,beforeXp,currentCoins-beforeCoins,currentXp-beforeXp,elapsedMs,used,total,corrections,avatarId,rankAware,captainId,rankBefore,rankAfter,rankCopies,rankCoins,rankRewardStatus.equals("PENDING")?"NONE":rankRewardStatus);
        if(!valid(value))throw new IllegalStateException("Invalid settled victory");return value;
    }
    /** Durable per-run receipts remain authoritative if balances change while a grant is pending. */
    public VictoryReceipt settleWithAwardedTotals(int awardedCoins,int awardedXp){
        if(ready)return this;
        if(!rankAware||awardedCoins<0||awardedXp<0)throw new IllegalStateException("Invalid per-run victory award");
        VictoryReceipt value=new VictoryReceipt(true,runId,mode,level,event,period,stars,beforeCoins,beforeXp,awardedCoins,awardedXp,elapsedMs,used,total,corrections,avatarId,rankAware,captainId,rankBefore,rankAfter,rankCopies,rankCoins,rankRewardStatus.equals("PENDING")?"NONE":rankRewardStatus);
        if(!valid(value))throw new IllegalStateException("Invalid settled victory");return value;
    }
    public boolean matches(String run,int gameMode,int number,int eventIndex,String eventPeriod){
        return runId.equals(run)&&mode==gameMode&&level==number&&event==eventIndex&&period.equals(eventPeriod);
    }
    public String encode(){return (rankAware?RANK_VERSION:VERSION)+"|"+(ready?"R":"P")+"|"+runId+"|"+mode+"|"+level+"|"+event+"|"+period+"|"+stars+"|"+beforeCoins+"|"+beforeXp+"|"+coins+"|"+xp+"|"+elapsedMs+"|"+used+"|"+total+"|"+corrections+"|"+avatarId+(rankAware?"|"+captainId+"|"+rankBefore+"|"+rankAfter+"|"+rankCopies+"|"+rankCoins+"|"+rankRewardStatus:"");}
    public static VictoryReceipt decode(String encoded){
        if(encoded==null||encoded.length()>850)return null;
        try{
            String[] v=encoded.split("\\|",-1);boolean modern=v.length==23&&RANK_VERSION.equals(v[0]);if(!(modern||v.length==17&&VERSION.equals(v[0]))||!("R".equals(v[1])||"P".equals(v[1])))return null;
            VictoryReceipt value=new VictoryReceipt("R".equals(v[1]),v[2],Integer.parseInt(v[3]),Integer.parseInt(v[4]),Integer.parseInt(v[5]),v[6],Integer.parseInt(v[7]),Integer.parseInt(v[8]),Integer.parseInt(v[9]),Integer.parseInt(v[10]),Integer.parseInt(v[11]),Long.parseLong(v[12]),Integer.parseInt(v[13]),Integer.parseInt(v[14]),Integer.parseInt(v[15]),v[16],modern,modern?v[17]:"",modern?Integer.parseInt(v[18]):0,modern?Integer.parseInt(v[19]):0,modern?Integer.parseInt(v[20]):0,modern?Integer.parseInt(v[21]):0,modern?v[22]:"LEGACY");
            return valid(value)&&value.encode().equals(encoded)?value:null;
        }catch(IllegalArgumentException bad){return null;}
    }
    private static boolean valid(VictoryReceipt v){
        return v.runId!=null&&v.runId.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")
                &&v.mode>=0&&v.mode<=5&&v.level>=1&&v.level<=1000&&v.event>=0&&v.event<=10
                &&validPeriod(v)&&v.stars>=1&&v.stars<=3
                &&v.beforeCoins>=0&&v.beforeXp>=0&&v.coins>=0&&v.xp>=0&&(long)v.beforeCoins+v.coins<=Integer.MAX_VALUE
                &&(long)v.beforeXp+v.xp<=Integer.MAX_VALUE&&v.elapsedMs>=0&&v.elapsedMs<=31536000000L
                &&v.used>=0&&v.used<=10000&&v.total>=0&&v.total<=10000&&v.used<=v.total
                &&v.corrections>=0&&v.corrections<=10000&&v.avatarId!=null&&v.avatarId.matches("[a-z0-9_]{1,60}")
                &&(v.ready||v.coins==0&&v.xp==0)&&validRank(v);
    }
    private static boolean validRank(VictoryReceipt v){
        if(!v.rankAware)return v.captainId.isEmpty()&&v.rankBefore==0&&v.rankAfter==0&&v.rankCopies==0&&v.rankCoins==0&&v.rankRewardStatus.equals("LEGACY");
        if(v.captainId==null||!v.captainId.matches("[a-z0-9_]{0,60}")||v.rankBefore<0||v.rankAfter<v.rankBefore||v.rankCopies<0||v.rankCopies>v.rankAfter-v.rankBefore||v.rankCoins<0||(long)v.rankCoins>45L*(v.rankAfter-v.rankBefore))return false;
        if(!v.rankRewardStatus.equals("PENDING")&&!v.rankRewardStatus.equals("NONE")&&!v.rankRewardStatus.equals("COPY")&&!v.rankRewardStatus.equals("MAX_COMPENSATION")&&!v.rankRewardStatus.equals("LEGACY_NO_CAPTAIN")&&!v.rankRewardStatus.equals("REPLAY")&&!v.rankRewardStatus.equals("INELIGIBLE")&&!v.rankRewardStatus.equals("MIGRATED"))return false;
        if(v.ready&&v.rankRewardStatus.equals("PENDING"))return false;
        return !v.captainId.isEmpty()||v.rankCopies==0&&v.rankCoins==0;
    }
    private static boolean validPeriod(VictoryReceipt v){
        if(v.period==null)return false;
        if(v.mode==0||v.mode==4)return v.period.isEmpty()&&v.event==0;
        if(v.mode==3)return v.period.equals("remix-v1")&&v.event==0;
        if(v.mode==5)return v.event<10&&v.period.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");
        try{return v.event<(v.mode==1?3:7)&&java.time.LocalDate.parse(v.period).toString().equals(v.period);}
        catch(java.time.DateTimeException bad){return false;}
    }
}
