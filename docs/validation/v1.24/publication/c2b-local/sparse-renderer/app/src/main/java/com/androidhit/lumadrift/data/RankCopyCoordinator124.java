package com.androidhit.lumadrift.data;

import com.androidhit.lumadrift.engine.VictoryReceipt;

/** Cross-file recovery uses immutable XP/run receipts and atomic GameStore rank grants. */
public final class RankCopyCoordinator124 {
    private final GameStore store;
    private final MetaProgress meta;
    public RankCopyCoordinator124(GameStore store,MetaProgress meta){
        if(store==null||meta==null)throw new IllegalArgumentException("Progress stores required");
        this.store=store;this.meta=meta;
    }
    /** Always called before new-policy XP, so a failed migration cannot create retroactive gifts. */
    public int initializeMigrationFloor(){return store.initializeRankCopyFloor124(meta.rank());}

    public VictoryReceipt settleVictoryReceipt(VictoryReceipt receipt,boolean solved){
        if(receipt==null||!solved)throw new IllegalArgumentException("An actual solved-run receipt is required");
        if(receipt.ready)return receipt;
        int floor=initializeMigrationFloor();
        int baseCoins=receipt.rankAware?store.freezeVictoryCoins124(receipt.runId,receipt.beforeCoins):0;
        String captain=receipt.rankAware?receipt.captainId:"";
        int index=receipt.mode==1||receipt.mode==2?receipt.event:receipt.level;
        RankVictoryXp124 xp=meta.recordWin124(receipt.mode,index,receipt.stars,receipt.period,receipt.runId,captain);
        int from=meta.rank(),to=from,copies=0,coins=0;String status="INELIGIBLE";
        if(xp!=null){
            if(!xp.captainId.equals(captain))throw new IllegalStateException("Persisted victory captain changed");
            from=RankCopyRules124.rankForXp(xp.beforeXp);to=RankCopyRules124.rankForXp(xp.afterXp);
            status=captain.isEmpty()?"LEGACY_NO_CAPTAIN":xp.firstWin?"NONE":"REPLAY";
            if(xp.firstWin&&to>from){
                status="MIGRATED";
                for(int rank=from+1;rank<=to;rank++){
                    if(rank<=floor)continue;
                    RankCopyReward124 award=store.claimRankCopy124(xp,rank);
                    if(award==null)throw new IllegalStateException("Rank reward recipient or receipt unavailable");
                    copies+=award.copies();coins+=award.coins;status=award.status;
                }
            }
        }
        VictoryReceipt result=receipt.withRankReward(captain,from,to,copies,coins,status);
        // Older pending receipts may already contain XP from the old grant path.
        // Their original baseline remains authoritative; new receipts use exact per-run XP.
        return receipt.rankAware?result.settleWithAwardedTotals(Math.addExact(baseCoins,coins),xp==null?0:xp.afterXp-xp.beforeXp)
            :result.settle(store.coins(),meta.xp());
    }
}
