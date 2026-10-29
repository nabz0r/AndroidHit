package com.androidhit.lumadrift.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

/**
 * Local game progression and sandbox economy ledger. These methods never verify
 * a real payment; replaying an already earned star cannot farm rewards.
 * Level numbers are 1-based. Event puzzle indexes are 0-based.
 */
// Synchronous small local transactions intentionally persist economy before action returns.
@SuppressLint("ApplySharedPref")
public final class GameStore {
    /** A retryable local transaction failure, including a read that flushes a pending write. */
    public static final class LocalSaveException extends IllegalStateException {
        private static final long serialVersionUID = 1L;
        public LocalSaveException(String message) { super(message); }
    }
    public interface Clock { long now(); }
    /** Optional account-bound permanent ownership; never imports purchased gold into local coins. */
    public interface ExternalOwnership {
        boolean ownsCharacter(String id);
        boolean ownsSkin(String id);
        default boolean ownsCosmetic(String id) { return false; }
    }
    private static final String PREFS = "luma_drift_save_v1";
    private static final int MAX_COINS = 2_000_000_000;
    private static final int MAX_STOCK = 999;
    private static final Object GOLD_GRANT_LOCK = LocalSaveTransactions.LOCK;
    // The no-argument loot API must retry the same milestone after an Android
    // memory-before-disk failure, including through another handle in this process.
    private static final Map<SharedPreferences, LootRetry> LOOT_RETRIES = new WeakHashMap<>();
    // A failed commit may already have cleared the on-disk escrow's identifiers
    // in Android's memory map. Keep the exact finalization until its flush succeeds.
    private static final Map<SharedPreferences, PowerCommitRetry> POWER_COMMIT_RETRIES = new WeakHashMap<>();
    private static final class PowerCommitRetry {
        final String id, requestId;
        final boolean completion;
        PowerCommitRetry(String id, String requestId, boolean completion) {
            this.id = id; this.requestId = requestId; this.completion = completion;
        }
    }
    private static final class LootRetry {
        final int milestone; final String item;
        LootRetry(int milestone, String item) { this.milestone = milestone; this.item = item; }
    }
    private final SharedPreferences prefs;
    private final ExternalOwnership externalOwnership;
    private final Clock clock;

    public GameStore(Context context) {
        this(context, null);
    }

    public GameStore(Context context, ExternalOwnership ownership) {
        this(context, ownership, System::currentTimeMillis);
    }

    /** Injectable local calendar clock for rollover and save-recovery verification. */
    public GameStore(Context context, ExternalOwnership ownership, Clock clock) {
        if (clock == null) throw new IllegalArgumentException("Missing clock");
        this.clock = clock;
        externalOwnership = ownership;
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        synchronized (GOLD_GRANT_LOCK) {
            try {
                // Another handle may hold an initialization applied only to memory.
                LocalSaveTransactions.flush(prefs);
                if (!prefs.contains("initialized")) {
                    Map<String,Object> write = new HashMap<>();
                    write.put("initialized", true);
                    write.put("tutorial_v18_fresh_profile", true);
                    write.put("coins", 180);
                    write.put("booster_hint", 3);
                    write.put("booster_vision", 3);
                    write.put("booster_flow", 2);
                    write.put("character", "nova");
                    write.put("skin", "lumina");
                    write.put("owned_character_nova", true);
                    write.put("owned_skin_lumina", true);
                    LocalSaveTransactions.write(prefs, write);
                }
            } catch (LocalSaveException pending) {
                // Construction must remain safe. The exact write stays pending;
                // the first guarded read/action retries it or exposes the error.
            }
        }
    }

    /** Flush the exact pending transaction before the UI resumes, without replaying its grant. */
    public synchronized void retryPendingLocalSave() { LocalSaveTransactions.flush(prefs); }

    public synchronized int coins() { LocalSaveTransactions.flush(prefs); return Math.max(0, prefs.getInt("coins", 0)); }

    public synchronized int unlockedLevel() {
        return Math.max(1, Math.min(Catalog.LEVEL_COUNT, prefs.getInt("unlocked", 1)));
    }

    public synchronized boolean isLevelUnlocked(int level) {
        return level >= 1 && level <= unlockedLevel();
    }

    public synchronized int stars(int level) {
        if (level < 1 || level > Catalog.LEVEL_COUNT) return 0;
        return clampStars(prefs.getInt("stars_" + level, 0));
    }

    public synchronized int totalStars() {
        int total = 0;
        for (int i = 1; i <= Catalog.LEVEL_COUNT; i++) total += stars(i);
        return total;
    }

    public synchronized int completedLevels() {
        int total = 0;
        for (int i = 1; i <= Catalog.LEVEL_COUNT; i++) if (stars(i) > 0) total++;
        return total;
    }

    /** First completion: 25 coins + 10/star; improved stars: 10/new star. */
    public synchronized int completeLevel(int level, int earnedStars) {
        synchronized (GOLD_GRANT_LOCK) {
            if (!isLevelUnlocked(level)) return 0;
            int nextStars = clampStars(earnedStars);
            if (nextStars == 0) return 0;
            int oldStars = stars(level);
            int reward = nextStars > oldStars ? (oldStars == 0 ? 25 : 0) + 10 * (nextStars - oldStars) : 0;
            SharedPreferences.Editor edit = prefs.edit();
            edit.putInt("stars_" + level, Math.max(oldStars, nextStars));
            edit.putInt("unlocked", Math.min(Catalog.LEVEL_COUNT, Math.max(unlockedLevel(), level + 1)));
            edit.putInt("coins", safeAdd(coins(), reward));
            recordFirstAvatarWin(edit,0,level,"",oldStars,nextStars);
            if (!edit.commit()) throw new IllegalStateException("Could not persist Journey reward");
            return reward;
        }
    }

    public synchronized String equippedCharacter() {
        LocalSaveTransactions.flush(prefs);
        String id = prefs.getString("character", "nova");
        return Catalog.character(id) != null && ownsCharacter(id) ? id : "nova";
    }

    public synchronized String equippedSkin() {
        LocalSaveTransactions.flush(prefs);
        String id = prefs.getString("skin", "lumina");
        return Catalog.skin(id) != null && ownsSkin(id) ? id : "lumina";
    }

    public synchronized boolean ownsCharacter(String id) {
        LocalSaveTransactions.flush(prefs);
        return Catalog.character(id) != null && ("nova".equals(id) || prefs.getBoolean("owned_character_" + id, false)
                || externalOwnership != null && externalOwnership.ownsCharacter(id));
    }

    public synchronized boolean ownsSkin(String id) {
        LocalSaveTransactions.flush(prefs);
        return Catalog.skin(id) != null && ("lumina".equals(id) || prefs.getBoolean("owned_skin_" + id, false)
                || externalOwnership != null && externalOwnership.ownsSkin(id));
    }

    public synchronized boolean purchaseCharacter(String id) {
        synchronized (GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            Catalog.CharacterItem item = Catalog.character(id);
            if (item == null || ownsCharacter(id) || coins() < item.price) return false;
            Map<String,Object> write = new HashMap<>();
            write.put("coins", coins() - item.price);
            write.put("owned_character_" + id, true);
            LocalSaveTransactions.write(prefs, write);
            return true;
        }
    }

    public synchronized boolean purchaseSkin(String id) {
        synchronized (GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            Catalog.SkinItem item = Catalog.skin(id);
            if (item == null || ownsSkin(id) || coins() < item.price) return false;
            Map<String,Object> write = new HashMap<>();
            write.put("coins", coins() - item.price);
            write.put("owned_skin_" + id, true);
            LocalSaveTransactions.write(prefs, write);
            return true;
        }
    }

    public synchronized boolean equipCharacter(String id) {
        synchronized (GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            if (!ownsCharacter(id)) return false;
            Map<String,Object> write = new HashMap<>();
            write.put("character", id);
            LocalSaveTransactions.write(prefs, write);
            return true;
        }
    }

    public synchronized boolean equipSkin(String id) {
        synchronized (GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            if (!ownsSkin(id)) return false;
            Map<String,Object> write = new HashMap<>();
            write.put("skin", id);
            LocalSaveTransactions.write(prefs, write);
            return true;
        }
    }

    /** Available units exclude the one held by an unfinished effect reservation. */
    public synchronized int boosterStock(String id) {
        if (Catalog.booster(id) == null) return 0;
        synchronized (GOLD_GRANT_LOCK) {
            PendingBoosterUse pending = pendingBoosterUseLocked();
            return Math.max(0, storedBoosterStock(id) - (pending != null && id.equals(pending.id) ? 1 : 0));
        }
    }

    private int storedBoosterStock(String id) {
        // A failed gift commit must be durable before an existing power path can
        // consume that stock; later receipt recovery must not overwrite its debit.
        LocalSaveTransactions.flush(prefs);
        return Math.max(0, Math.min(MAX_STOCK, prefs.getInt("booster_" + id, 0)));
    }

    /** One global in-flight effect; permanent receipt IDs prevent duplicate spending. */
    public static final class PendingBoosterUse {
        public final String id, requestId;
        private PendingBoosterUse(String id, String requestId) { this.id = id; this.requestId = requestId; }
    }
    private static final String POWER_PENDING_ID = "power_use_pending_id_v1";
    private static final String POWER_PENDING_REQUEST = "power_use_pending_request_v1";
    private static String powerUseReceipt(String requestId) { return "power_use_v1_" + sha256(requestId); }
    private PendingBoosterUse pendingBoosterUseLocked() {
        String id = prefs.getString(POWER_PENDING_ID, ""), request = prefs.getString(POWER_PENDING_REQUEST, "");
        if (Catalog.booster(id) == null || !validRewardId(request)) return null;
        return ("reserved:" + id).equals(prefs.getString(powerUseReceipt(request), "")) ? new PendingBoosterUse(id, request) : null;
    }
    public synchronized PendingBoosterUse pendingBoosterUse() {
        synchronized (GOLD_GRANT_LOCK) { return pendingBoosterUseLocked(); }
    }

    /**
     * Run before inspecting pendingBoosterUse during same-process reconciliation.
     * Used/cancelled memory receipts are deliberately not exposed as new escrow
     * and cannot reduce available stock twice. After process death the original
     * durable reservation and the saved run marker remain the recovery source.
     */
    public synchronized void retryPowerCommit() {
        synchronized (GOLD_GRANT_LOCK) {
            PowerCommitRetry retry = POWER_COMMIT_RETRIES.get(prefs);
            if (retry == null) return;
            boolean persisted = retry.completion ? completeBoosterUse(retry.id, retry.requestId)
                    : cancelBoosterUse(retry.id, retry.requestId);
            if (!persisted) throw new IllegalStateException("Could not reconcile power finalization");
        }
    }

    private void persistPowerFinalization(SharedPreferences.Editor edit, String id, String requestId,
                                          boolean completion, String failureMessage) {
        if (!edit.commit()) {
            POWER_COMMIT_RETRIES.put(prefs, new PowerCommitRetry(id, requestId, completion));
            throw new IllegalStateException(failureMessage);
        }
        PowerCommitRetry retry = POWER_COMMIT_RETRIES.get(prefs);
        if (retry != null && retry.id.equals(id) && retry.requestId.equals(requestId)) POWER_COMMIT_RETRIES.remove(prefs);
    }

    /**
     * Holds one existing unit without charging it yet. Retry the same request
     * after a disk failure; other effects cannot steal this reservation. Save the
     * matching request with the actual effect/run state before completing it.
     */
    public synchronized boolean reserveBoosterUse(String id, String requestId) {
        if (!boosterUnlocked(id) || !validRewardId(requestId)) return false;
        synchronized (GOLD_GRANT_LOCK) {
            String key = powerUseReceipt(requestId), status = prefs.getString(key, "");
            PendingBoosterUse pending = pendingBoosterUseLocked();
            if (!status.isEmpty() && !status.equals("reserved:" + id)) return false;
            if (pending != null && (!pending.id.equals(id) || !pending.requestId.equals(requestId))) return false;
            if (storedBoosterStock(id) <= 0) return false;
            if (!prefs.edit().putString(key, "reserved:" + id).putString(POWER_PENDING_ID, id)
                    .putString(POWER_PENDING_REQUEST, requestId).putInt("booster_" + id, storedBoosterStock(id)).commit())
                throw new IllegalStateException("Could not persist power reservation");
            return true;
        }
    }

    /** Call only after the effect actually started and its run snapshot was saved. */
    public synchronized boolean completeBoosterUse(String id, String requestId) {
        if (Catalog.booster(id) == null || !validRewardId(requestId)) return false;
        synchronized (GOLD_GRANT_LOCK) {
            String key = powerUseReceipt(requestId), status = prefs.getString(key, "");
            if (status.equals("used:" + id)) {
                // Android may have applied a failed commit to memory. A retry
                // flushes the recorded charge without subtracting another unit.
                persistPowerFinalization(prefs.edit().putString(key, status).putInt("booster_" + id, storedBoosterStock(id)),
                        id, requestId, true, "Could not persist recorded power use");
                return true;
            }
            PendingBoosterUse pending = pendingBoosterUseLocked();
            if (!status.equals("reserved:" + id) || pending == null || !id.equals(pending.id)
                    || !requestId.equals(pending.requestId) || storedBoosterStock(id) <= 0) return false;
            persistPowerFinalization(prefs.edit().putInt("booster_" + id, storedBoosterStock(id) - 1).putString(key, "used:" + id)
                    .putString(POWER_PENDING_ID, "").putString(POWER_PENDING_REQUEST, ""),
                    id, requestId, true, "Could not persist power use");
            return true;
        }
    }

    /** Failure/cancel before activation releases escrow; no stock unit is consumed. */
    public synchronized boolean cancelBoosterUse(String id, String requestId) {
        if (Catalog.booster(id) == null || !validRewardId(requestId)) return false;
        synchronized (GOLD_GRANT_LOCK) {
            String key = powerUseReceipt(requestId), status = prefs.getString(key, "");
            if (status.equals("cancelled:" + id)) {
                persistPowerFinalization(prefs.edit().putString(key, status).putInt("booster_" + id, storedBoosterStock(id)),
                        id, requestId, false, "Could not persist recorded power cancellation");
                return true;
            }
            PendingBoosterUse pending = pendingBoosterUseLocked();
            if (!status.equals("reserved:" + id) || pending == null || !id.equals(pending.id) || !requestId.equals(pending.requestId)) return false;
            persistPowerFinalization(prefs.edit().putString(key, "cancelled:" + id).putInt("booster_" + id, storedBoosterStock(id))
                    .putString(POWER_PENDING_ID, "").putString(POWER_PENDING_REQUEST, ""),
                    id, requestId, false, "Could not persist power cancellation");
            return true;
        }
    }

    /** Lifetime event history is already saved by date; rotating periods never remove earned gear progress. */
    public synchronized AvatarGearCatalog.Progress avatarGearProgress() {
        int weekly=0,eventStars=0;
        for(Map.Entry<String,?> entry:prefs.getAll().entrySet()) {
            String key=entry.getKey();Object value=entry.getValue();
            if(!(value instanceof Integer))continue;
            boolean week=key.matches("weekly_[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-6]");
            boolean daily=key.matches("daily_[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-2]");
            if(!week&&!daily)continue;
            int stars=clampStars((Integer)value);
            if(week&&stars>0)weekly=safeAdd(weekly,1);
            eventStars=safeAdd(eventStars,stars);
        }
        return new AvatarGearCatalog.Progress(completedLevels(),weekly,eventStars);
    }
    public synchronized boolean ownsAvatarGear(String id) {
        LocalSaveTransactions.flush(prefs);
        AvatarGearCatalog.Item item=AvatarGearCatalog.find(id);
        return item!=null&&(item.source==AvatarGearCatalog.Source.STARTER||!prefs.getString("avatar_owned_v1_"+id,"").isEmpty());
    }
    public synchronized String avatarGearAcquisitionSource(String id) {
        LocalSaveTransactions.flush(prefs);
        AvatarGearCatalog.Item item=AvatarGearCatalog.find(id);
        if(item==null)return "";
        return item.source==AvatarGearCatalog.Source.STARTER?AvatarGearCatalog.Source.STARTER.name():prefs.getString("avatar_owned_v1_"+id,"");
    }
    /** Call on resume, after a win or opening equipment; not from every rendering frame. */
    public synchronized List<String> claimAvatarGearUnlocks() {
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            AvatarGearCatalog.Progress progress=avatarGearProgress();
            List<String> unlocked=new ArrayList<>();Map<String,Object> write=new HashMap<>();
            for(AvatarGearCatalog.Item item:AvatarGearCatalog.ITEMS)if(AvatarGearCatalog.claimable(item,progress)) {
                String key="avatar_owned_v1_"+item.id;
                if(!ownsAvatarGear(item.id))unlocked.add(item.id);
                // Flush previously memory-applied writes too after a disk failure.
                String acquired=prefs.getString(key, "");
                write.put(key,acquired.isEmpty()?item.source.name():acquired);
            }
            LocalSaveTransactions.write(prefs,write);
            return Collections.unmodifiableList(unlocked);
        }
    }
    /** Existing ownership makes repeated purchase calls a no-op; only device-earned coins are debited. */
    public synchronized boolean purchaseAvatarGear(String id) {
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            AvatarGearCatalog.Item item=AvatarGearCatalog.find(id);
            if(item==null||item.source!=AvatarGearCatalog.Source.EARNED_COINS||ownsAvatarGear(id))return false;
            if(coins()<item.priceCoins)return false;
            Map<String,Object> write=new HashMap<>();
            write.put("avatar_owned_v1_"+id,item.source.name());
            write.put("coins",coins()-item.priceCoins);
            LocalSaveTransactions.write(prefs,write);
            return true;
        }
    }
    public synchronized boolean equipAvatarGear(String id) {
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            AvatarGearCatalog.Item item=AvatarGearCatalog.find(id);if(item==null||!ownsAvatarGear(id))return false;
            Map<String,Object> write=new HashMap<>();
            write.put("avatar_slot_v1_"+item.slot.name(),id);
            LocalSaveTransactions.write(prefs,write);
            return true;
        }
    }
    public synchronized AvatarLoadout avatarLoadout() {
        LocalSaveTransactions.flush(prefs);
        String[] ids=new String[3];int i=0;
        for(AvatarGearCatalog.Slot slot:AvatarGearCatalog.Slot.values()) {
            String id=prefs.getString("avatar_slot_v1_"+slot.name(),AvatarGearCatalog.starter(slot).id);
            AvatarGearCatalog.Item item=AvatarGearCatalog.find(id);
            ids[i++]=item!=null&&item.slot==slot&&ownsAvatarGear(id)?id:AvatarGearCatalog.starter(slot).id;
        }
        return new AvatarLoadout(ids[0],ids[1],ids[2]);
    }
    private static String avatarBoardIdentity(int mode,int index,String period) {
        if(mode==0)return index>=1&&index<=Catalog.LEVEL_COUNT?"journey:"+index:"";
        if(mode==3)return index>=1&&index<=Catalog.REMIX_COUNT?"remix:"+index:"";
        if((mode!=1&&mode!=2)||period==null||!period.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"))return "";
        int count=mode==1?Catalog.DAILY_COUNT:Catalog.WEEKLY_COUNT;
        return index>=0&&index<count?(mode==1?"daily:":"weekly:")+period+":"+index:"";
    }
    private void recordFirstAvatarWin(SharedPreferences.Editor edit,int mode,int index,String period,int oldStars,int newStars) {
        if(oldStars>0||newStars<=0)return;
        String identity=avatarBoardIdentity(mode,index,period);if(identity.isEmpty())return;
        String key="avatar_win_v1_"+sha256(identity);
        if(!prefs.contains(key))edit.putString(key,"pending:"+avatarLoadout().winBonusCoins());
    }
    /**
     * A first-clear snapshot created by completeLevel/Daily/Weekly/Remix is the
     * only source of this small bonus. Replays and later equipment changes cannot
     * create/increase it; old pre-feature wins have no retroactive pending bonus.
     */
    public synchronized int claimAvatarVictoryBonus(int mode,int index,String period) {
        String identity=avatarBoardIdentity(mode,index,period);if(identity.isEmpty())return 0;
        synchronized(GOLD_GRANT_LOCK) {
            String key="avatar_win_v1_"+sha256(identity),record=prefs.getString(key,"");
            if(record.startsWith("claimed:")) {
                if(!prefs.edit().putString(key,record).putInt("coins",coins()).commit())throw new IllegalStateException("Could not persist recorded avatar victory bonus");
                return 0;
            }
            if(!record.matches("pending:[0-3]"))return 0;
            int amount=Math.max(0,Math.min(Integer.parseInt(record.substring(8)),MAX_COINS-coins()));
            if(!prefs.edit().putString(key,"claimed:"+amount).putInt("coins",safeAdd(coins(),amount)).commit())throw new IllegalStateException("Could not persist avatar victory bonus");
            return amount;
        }
    }

    public int boosterStockLimit() { return MAX_STOCK; }

    /** Saved old stocks remain intact; access follows the public Journey milestone. */
    public synchronized boolean boosterUnlocked(String id) {
        Catalog.BoosterItem item = Catalog.booster(id);
        return item != null && unlockedLevel() >= item.unlockLevel;
    }

    private static String powerTrialReceipt(String id) { return "power_trial_v19_" + id; }

    /** One free, explicitly claimed sample per local profile and catalog power. */
    public enum PowerTrialStatus { GRANTED, ALREADY_CLAIMED, LOCKED, STORAGE_FULL, INVALID_POWER, NOT_OFFERED }

    public synchronized boolean powerTrialClaimed(String id) {
        if (Catalog.booster(id) == null) return false;
        synchronized (GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            return prefs.getBoolean(powerTrialReceipt(id), false);
        }
    }

    /** An earned pending sample; full stock leaves it available for a later retry. */
    public synchronized boolean powerTrialAvailable(String id) {
        synchronized (GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            return Catalog.isPowerOffered(id) && boosterUnlocked(id) && !powerTrialClaimed(id);
        }
    }

    /**
     * Receipt and one physical stock unit share the same atomic local write.
     * Existing stock, purchases and past usage do not consume this welcome sample.
     * A storage failure throws; retry flushes that exact grant before any new use.
     */
    public synchronized PowerTrialStatus claimPowerTrial(String id) {
        if (Catalog.booster(id) == null) return PowerTrialStatus.INVALID_POWER;
        synchronized (GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            if (powerTrialClaimed(id)) return PowerTrialStatus.ALREADY_CLAIMED;
            if (!Catalog.isPowerOffered(id)) return PowerTrialStatus.NOT_OFFERED;
            if (!boosterUnlocked(id)) return PowerTrialStatus.LOCKED;
            int stock = storedBoosterStock(id);
            if (stock >= MAX_STOCK) return PowerTrialStatus.STORAGE_FULL;
            Map<String,Object> write = new HashMap<>();
            write.put(powerTrialReceipt(id), true);
            write.put("booster_" + id, stock + 1);
            LocalSaveTransactions.write(prefs, write);
            return PowerTrialStatus.GRANTED;
        }
    }

    /** Distinct first clears only. Events, replays, purchases and failures cannot raise prices. */
    public synchronized int economyProgress() {
        int completed = 0;
        for (int level = 1; level <= Catalog.LEVEL_COUNT; level++) {
            if (stars(level) > 0) completed++;
            if (remixStars(level) > 0) completed++;
        }
        return completed;
    }

    public synchronized int boosterPrice(String id) {
        Catalog.BoosterItem item = Catalog.booster(id);
        return item == null ? 0 : avatarLoadout().discountedPackPrice(EconomyCurve.price(item.price, economyProgress()));
    }

    public synchronized boolean purchaseBooster(String id) {
        synchronized (GOLD_GRANT_LOCK) {
            // Settle any already-decided purchase before refusing a retired offer.
            // The pending transaction owns its old exact debit/stock pair.
            LocalSaveTransactions.flush(prefs);
            Catalog.BoosterItem item = Catalog.booster(id);
            if (item == null || !Catalog.isPowerOffered(id) || !boosterUnlocked(id)) return false;
            int price = boosterPrice(id);
            if (coins() < price || storedBoosterStock(id) > MAX_STOCK - item.amount) return false;
            Map<String,Object> write = new HashMap<>();
            write.put("coins", coins() - price);
            write.put("booster_" + id, storedBoosterStock(id) + item.amount);
            // Do not report success while Android has only changed memory. Keep
            // this exact debit and grant together for the guarded save retry.
            LocalSaveTransactions.write(prefs, write);
            return true;
        }
    }

    /** Legacy stock-only debit. Gameplay effects use reserve/complete/cancel instead. */
    public synchronized boolean useBooster(String id) {
        synchronized (GOLD_GRANT_LOCK) {
            if (!boosterUnlocked(id)) return false;
            int count = boosterStock(id);
            if (count <= 0) return false;
            if (!prefs.edit().putInt("booster_" + id, storedBoosterStock(id) - 1).commit())
                throw new IllegalStateException("Could not persist legacy power use");
            return true;
        }
    }

    /** Read-only observation; grantBoosterOnce must succeed without a persistence error first. */
    public synchronized boolean hasBoosterGranted(String id, String requestId) {
        if (Catalog.booster(id) == null || !validRewardId(requestId)) return false;
        synchronized (GOLD_GRANT_LOCK) {
            return id.equals(prefs.getString("booster_reward_v1_" + sha256(requestId), ""));
        }
    }

    /** One earned unit, not a purchased pack. One request can reward only one power. */
    public synchronized int grantBoosterOnce(String id, String requestId) {
        if (!boosterUnlocked(id) || !validRewardId(requestId)) return 0;
        synchronized (GOLD_GRANT_LOCK) {
            String token = "booster_reward_v1_" + sha256(requestId);
            String granted = prefs.getString(token, "");
            if (!granted.isEmpty()) {
                if (!id.equals(granted)) return 0;
                boolean persisted = prefs.edit().putString(token, id).putInt("booster_" + id, storedBoosterStock(id)).commit();
                if (!persisted) throw new IllegalStateException("Could not persist recorded power reward");
                return 0;
            }
            int stock = storedBoosterStock(id);
            if (stock >= MAX_STOCK) return 0;
            boolean persisted = prefs.edit().putString(token, id).putInt("booster_" + id, stock + 1).commit();
            if (!persisted) throw new IllegalStateException("Could not persist power reward");
            return 1;
        }
    }

    /** Earliest unclaimed five-clear Journey milestone, or zero after the whole catalog. */
    public synchronized int nextLootMilestone() {
        synchronized (GOLD_GRANT_LOCK) {
            LootRetry retry = LOOT_RETRIES.get(prefs);
            if (retry != null) return retry.milestone;
            for (int milestone = 5; milestone <= Catalog.LEVEL_COUNT; milestone += 5) {
                if (!prefs.contains("power_loot_v1_" + milestone)
                        && !prefs.contains("power_loot_v19_" + milestone)) return milestone;
            }
            return 0;
        }
    }

    /**
     * Stable preview for the earliest unclaimed five-clear gift, even before earned.
     * Eligibility is frozen to the Journey level opened by that milestone, rather
     * than the player's level when they visit the rewards screen. Old receipts and
     * an exact in-process storage retry retain their original item.
     */
    public synchronized String nextMilestoneLootId() {
        synchronized (GOLD_GRANT_LOCK) {
            LootRetry retry = LOOT_RETRIES.get(prefs);
            if (retry != null) return retry.item;
            return milestoneLootId(nextLootMilestone());
        }
    }

    private String milestoneLootId(int milestone) {
        if (milestone < 5 || milestone > Catalog.LEVEL_COUNT || milestone % 5 != 0) return "";
        int eligible = 0;
        int earnedUnlock = Math.min(Catalog.LEVEL_COUNT, milestone + 1);
        for (Catalog.BoosterItem item : Catalog.BOOSTERS) if (Catalog.isPowerOffered(item.id) && item.unlockLevel <= earnedUnlock) eligible++;
        if (eligible == 0) return "";
        int target = (milestone / 5 - 1) % eligible;
        for (Catalog.BoosterItem item : Catalog.BOOSTERS)
            if (Catalog.isPowerOffered(item.id) && item.unlockLevel <= earnedUnlock && target-- == 0) return item.id;
        return "";
    }

    private String milestoneLootReceipt(int milestone) {
        String current = "power_loot_v19_" + milestone;
        return prefs.contains(current) || !prefs.contains("power_loot_v1_" + milestone)
                ? current : "power_loot_v1_" + milestone;
    }

    /**
     * Earn one deterministic unlocked power per five distinct Journey clears.
     * Claim oldest first; no paid/random loot and no reward from replay/event wins.
     * Item and claim token share one commit. A failed in-process call remains on
     * that same milestone until retry durably saves it, without another stock unit.
     */
    public synchronized String claimMilestoneLoot() {
        synchronized (GOLD_GRANT_LOCK) {
            LootRetry pending = LOOT_RETRIES.get(prefs);
            int milestone = pending == null ? nextLootMilestone() : pending.milestone;
            if (milestone == 0 || completedLevels() < milestone) return "";
            String token = milestoneLootReceipt(milestone);
            String recorded = prefs.getString(token, "");
            if (!recorded.isEmpty()) {
                if (pending == null || !pending.item.equals(recorded) || Catalog.booster(recorded) == null) return "";
                boolean persisted = prefs.edit().putString(token, recorded).putInt("booster_" + recorded, storedBoosterStock(recorded)).commit();
                if (!persisted) throw new IllegalStateException("Could not persist recorded milestone loot");
                LOOT_RETRIES.remove(prefs);
                return recorded;
            }
            // A pending decision can predate retirement. Never replace its item
            // with the contents of a newly generated offer during exact retry.
            String id = pending == null ? milestoneLootId(milestone) : pending.item;
            if (id.isEmpty()) return "";
            int stock = storedBoosterStock(id);
            if (stock >= MAX_STOCK) return "";
            LOOT_RETRIES.put(prefs, new LootRetry(milestone, id));
            boolean persisted = prefs.edit().putString(token, id).putInt("booster_" + id, stock + 1).commit();
            if (!persisted) throw new IllegalStateException("Could not persist milestone loot");
            LOOT_RETRIES.remove(prefs);
            return id;
        }
    }

    private static boolean validRewardId(String id) { return id != null && id.matches("[a-zA-Z0-9_:-]{1,160}"); }

    public synchronized boolean soundEnabled() { return prefs.getBoolean("sound", true); }
    public synchronized boolean musicEnabled() { return prefs.getBoolean("music", true); }
    public synchronized boolean hapticsEnabled() { return prefs.getBoolean("haptics", true); }
    public synchronized void setSoundEnabled(boolean enabled) { prefs.edit().putBoolean("sound", enabled).commit(); }
    public synchronized void setMusicEnabled(boolean enabled) { prefs.edit().putBoolean("music", enabled).commit(); }
    public synchronized void setHapticsEnabled(boolean enabled) { prefs.edit().putBoolean("haptics", enabled).commit(); }

    /** "auto" uses the system language and falls back to English. */
    public synchronized String language() {
        String code = prefs.getString("language", "auto");
        return "auto".equals(code) || Strings.supports(code) ? code : "auto";
    }

    public synchronized String resolvedLanguage() {
        String code = language();
        return "auto".equals(code) ? Strings.normalize(Locale.getDefault().getLanguage()) : code;
    }

    public synchronized void setLanguage(String code) {
        if ("auto".equals(code) || Strings.supports(code)) prefs.edit().putString("language", code).commit();
    }

    public synchronized boolean tutorialSeen() { return prefs.getBoolean("tutorial_seen", false); }
    public synchronized void setTutorialSeen(boolean seen) { prefs.edit().putBoolean("tutorial_seen", seen).commit(); }

    /** One local practice save, including lesson, teaching phase and typed board history. */
    public synchronized String tutorialState() {
        synchronized (GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            String state = prefs.getString("tutorial_v18_state", "");
            return validTutorialEnvelope(state) ? state : "";
        }
    }
    /** The UI must pass TutorialSession.encode(); that class additionally validates the exact history. */
    public synchronized void saveTutorialState(String state) {
        if (!validTutorialEnvelope(state)) throw new IllegalArgumentException("Invalid tutorial save");
        synchronized (GOLD_GRANT_LOCK) {
            Map<String,Object> write = new HashMap<>();
            write.put("tutorial_v18_state", state);
            LocalSaveTransactions.write(prefs, write);
        }
    }
    private static final String TUTORIAL_COMPLETED_SAVE = "T1|7|0|COMPLETE|";
    private static boolean validTutorialEnvelope(String state) {
        return state != null && state.length() <= 256 && (TUTORIAL_COMPLETED_SAVE.equals(state)
                || state.matches("T1\\|[0-6]\\|[0-4]\\|(ACTIVE|SKIPPED)\\|(?:[0-9]{1,2}(?:,[0-9]{1,2})*)?"));
    }
    /** Resume explicit active practice; only profiles first initialized in 1.8 start automatically. */
    public synchronized boolean shouldAutoStartTutorial() {
        synchronized (GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            String state = tutorialState();
            if (state.contains("|ACTIVE|")) return true;
            return prefs.getBoolean("tutorial_v18_fresh_profile", false) && state.isEmpty()
                    && !tutorialRewardClaimed() && unlockedLevel() == 1 && completedLevels() == 0;
        }
    }
    public synchronized boolean tutorialRewardClaimed() {
        synchronized (GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            return prefs.getBoolean("tutorial_v18_reward_claimed", false);
        }
    }
    public enum TutorialRewardStatus { GRANTED, ALREADY_CLAIMED, STORAGE_FULL, NOT_COMPLETE }
    /**
     * Exact once per local installation/profile, independent of guest/Google identity.
     * A completed practice save is mandatory. No campaign/event/daily counter is updated.
     * Receipt, 100 earned coins and two hints commit together; full storage stays retryable.
     */
    public synchronized TutorialRewardStatus claimTutorialReward() {
        synchronized (GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            if (tutorialRewardClaimed()) return TutorialRewardStatus.ALREADY_CLAIMED;
            if (!TUTORIAL_COMPLETED_SAVE.equals(tutorialState())) return TutorialRewardStatus.NOT_COMPLETE;
            int balance = coins(), hints = storedBoosterStock(Catalog.HINT);
            if (balance > MAX_COINS - 100 || hints > MAX_STOCK - 2) return TutorialRewardStatus.STORAGE_FULL;
            Map<String,Object> write = new HashMap<>();
            write.put("tutorial_v18_state", TUTORIAL_COMPLETED_SAVE);
            write.put("tutorial_v18_reward_claimed", true);
            write.put("coins", balance + 100);
            write.put("booster_" + Catalog.HINT, hints + 2);
            LocalSaveTransactions.write(prefs, write);
            return TutorialRewardStatus.GRANTED;
        }
    }

    public String dailyId() { return dayId(new Date(clock.now())); }

    /** ISO-style Monday start, represented by that Monday's local calendar date. */
    public String weeklyId() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(clock.now());
        int daysAfterMonday = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        calendar.add(Calendar.DAY_OF_MONTH, -daysAfterMonday);
        return dayId(calendar.getTime());
    }

    public synchronized int dailyStars(int index) {
        return eventStars("daily", dailyId(), index, Catalog.DAILY_COUNT);
    }

    public synchronized int weeklyStars(int index) {
        return eventStars("weekly", weeklyId(), index, Catalog.WEEKLY_COUNT);
    }

    public synchronized boolean isDailyUnlocked(int index) {
        return index >= 0 && index < Catalog.DAILY_COUNT && (index == 0 || dailyStars(index - 1) > 0);
    }

    public synchronized boolean isWeeklyUnlocked(int index) {
        return index >= 0 && index < Catalog.WEEKLY_COUNT && (index == 0 || weeklyStars(index - 1) > 0);
    }

    /** Each event puzzle pays once per date, then only the newly earned star difference. */
    public synchronized int completeDaily(int index, int earnedStars) {
        if (!isDailyUnlocked(index)) return 0;
        return completeEvent("daily", dailyId(), index, earnedStars, Catalog.DAILY_COUNT, 60, 10);
    }

    /** A board opened before midnight cannot claim a reward for the following day's board. */
    public synchronized int completeDaily(String expectedPeriod, int index, int earnedStars) {
        return dailyId().equals(expectedPeriod) ? completeDaily(index, earnedStars) : 0;
    }

    public synchronized int completeWeekly(int index, int earnedStars) {
        if (!isWeeklyUnlocked(index)) return 0;
        return completeEvent("weekly", weeklyId(), index, earnedStars, Catalog.WEEKLY_COUNT, 90, 15);
    }

    public synchronized int completeWeekly(String expectedPeriod, int index, int earnedStars) {
        return weeklyId().equals(expectedPeriod) ? completeWeekly(index, earnedStars) : 0;
    }

    public synchronized int dailyCompletedCount() {
        return eventCompletedCount("daily", dailyId(), Catalog.DAILY_COUNT);
    }

    public synchronized int weeklyCompletedCount() {
        return eventCompletedCount("weekly", weeklyId(), Catalog.WEEKLY_COUNT);
    }

    private int eventCompletedCount(String kind, String period, int limit) {
        int count = 0;
        for (int i = 0; i < limit; i++) if (eventStars(kind, period, i, limit) > 0) count++;
        return count;
    }

    public synchronized boolean dailyBonusClaimed() {
        synchronized (GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            return prefs.getBoolean("daily_bonus_" + dailyId(), false);
        }
    }
    public synchronized boolean weeklyBonusClaimed() {
        synchronized (GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            return prefs.getBoolean("weekly_bonus_" + weeklyId(), false);
        }
    }

    public synchronized int claimDailyCompletionReward() {
        synchronized (GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            String period = dailyId();
            if (eventCompletedCount("daily", period, Catalog.DAILY_COUNT) != Catalog.DAILY_COUNT
                    || prefs.getBoolean("daily_bonus_" + period, false)) return 0;
            Map<String,Object> write = new HashMap<>();
            write.put("daily_bonus_" + period, true);
            write.put("coins", safeAdd(coins(), 100));
            LocalSaveTransactions.write(prefs, write);
            return 100;
        }
    }

    public synchronized int claimWeeklyCompletionReward() {
        synchronized (GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            String period = weeklyId();
            if (eventCompletedCount("weekly", period, Catalog.WEEKLY_COUNT) != Catalog.WEEKLY_COUNT
                    || prefs.getBoolean("weekly_bonus_" + period, false)) return 0;
            Map<String,Object> write = new HashMap<>();
            write.put("weekly_bonus_" + period, true);
            write.put("coins", safeAdd(coins(), 250));
            LocalSaveTransactions.write(prefs, write);
            return 250;
        }
    }

    /** One optional local catalog target; obtaining it never silently changes the pin. */
    public synchronized String trackedCollectionGoal() {
        synchronized (GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            String key = prefs.getString("collection_goal_v110", "");
            return CollectionGoal.validKey(key) ? key : "";
        }
    }
    /** False means an unknown catalog target, never a failed persistence acknowledgement. */
    public synchronized boolean setTrackedCollectionGoal(String key) {
        if (!CollectionGoal.validKey(key)) return false;
        synchronized (GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            Map<String,Object> write = new HashMap<>();
            write.put("collection_goal_v110", key);
            LocalSaveTransactions.write(prefs, write);
            return true;
        }
    }
    public synchronized void clearTrackedCollectionGoal() {
        synchronized (GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            Map<String,Object> write = new HashMap<>();
            write.put("collection_goal_v110", "");
            LocalSaveTransactions.write(prefs, write);
        }
    }

    /** Stable seeds make an event board repeatable throughout its local date. */
    public long dailySeed(int index) { return stableSeed("daily:" + dailyId() + ":" + index); }
    public long weeklySeed(int index) { return stableSeed("weekly:" + weeklyId() + ":" + index); }

    private int eventStars(String kind, String period, int index, int count) {
        if (index < 0 || index >= count) return 0;
        return clampStars(prefs.getInt(kind + "_" + period + "_" + index, 0));
    }

    private int completeEvent(String kind, String period, int index, int earnedStars, int count, int base, int starReward) {
        synchronized (GOLD_GRANT_LOCK) {
            if (index < 0 || index >= count) return 0;
            int nextStars = clampStars(earnedStars);
            if (nextStars == 0) return 0;
            int oldStars = eventStars(kind, period, index, count);
            int reward = nextStars > oldStars ? (oldStars == 0 ? base : 0) + starReward * (nextStars - oldStars) : 0;
            SharedPreferences.Editor edit=prefs.edit().putInt(kind + "_" + period + "_" + index, Math.max(oldStars, nextStars))
                    .putInt("coins", safeAdd(coins(), reward));
            recordFirstAvatarWin(edit,"daily".equals(kind)?1:2,index,period,oldStars,nextStars);
            if(!edit.commit())throw new IllegalStateException("Could not persist event reward");
            return reward;
        }
    }

    private static String dayId(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(date);
    }

    private static long stableSeed(String text) {
        long hash = 0xcbf29ce484222325L;
        for (int i = 0; i < text.length(); i++) {
            hash ^= text.charAt(i);
            hash *= 0x100000001b3L;
        }
        return hash & Long.MAX_VALUE;
    }

    /** Dedicated Remix progression. Existing campaign saves and rewards remain unchanged. */
    public synchronized int remixUnlockedLevel() {
        return Math.max(1, Math.min(Catalog.REMIX_COUNT, prefs.getInt("remix_unlocked", 1)));
    }
    public synchronized boolean remixAvailable() { return unlockedLevel() >= 13; }
    public synchronized int remixStars(int level) {
        return level >= 1 && level <= Catalog.REMIX_COUNT ? clampStars(prefs.getInt("remix_stars_" + level, 0)) : 0;
    }
    public synchronized int completeRemix(int level, int stars) {
        synchronized (GOLD_GRANT_LOCK) {
            if (!remixAvailable() || level < 1 || level > Catalog.REMIX_COUNT || level > remixUnlockedLevel()) return 0;
            int next = clampStars(stars), old = remixStars(level);
            if (next == 0) return 0;
            int reward = next > old ? (old == 0 ? 35 : 0) + 12 * (next - old) : 0;
            SharedPreferences.Editor edit = prefs.edit().putInt("remix_stars_" + level, Math.max(old, next))
                    .putInt("remix_unlocked", Math.min(Catalog.REMIX_COUNT, Math.max(remixUnlockedLevel(), level + 1)))
                    .putInt("coins", safeAdd(coins(), reward));
            recordFirstAvatarWin(edit,3,level,"",old,next);
            if (!edit.commit()) throw new IllegalStateException("Could not persist Remix reward");
            return reward;
        }
    }
    /** Reward and its claim token are persisted together. Never accepts arbitrary external intent amounts. */
    public synchronized int grantMilestone(String uniqueKey, int amount) {
        if (uniqueKey == null || !uniqueKey.matches("[a-zA-Z0-9_:-]{1,100}") || amount < 1 || amount > 500) return 0;
        synchronized (GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            String token = "milestone_" + uniqueKey;
            if (prefs.getBoolean(token, false)) {
                if (!prefs.edit().putBoolean(token, true).putInt("coins", coins()).commit())
                    throw new IllegalStateException("Could not persist recorded milestone reward");
                return 0;
            }
            boolean persisted = prefs.edit().putBoolean(token, true).putInt("coins", safeAdd(coins(), amount)).commit();
            if (!persisted) throw new IllegalStateException("Could not persist milestone reward");
            return amount;
        }
    }

    /** Applies a verified social-service hint gift once on this device; never credits either currency. */
    public synchronized boolean claimSocialGift(String uid,String giftId) {
        if(uid==null||uid.trim().isEmpty()||!uid.matches("[^\\p{Cntrl}]{1,128}")
                ||giftId==null||!giftId.matches("[a-fA-F0-9]{64}"))return false;
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            String token="social_gift_v17_"+sha256(uid+":"+giftId.toLowerCase(Locale.ROOT));
            if(prefs.getBoolean(token,false))return false;
            int stock=storedBoosterStock(Catalog.HINT);if(stock>=MAX_STOCK)return false;
            Map<String,Object> write=new HashMap<>();write.put(token,true);write.put("booster_"+Catalog.HINT,stock+1);
            LocalSaveTransactions.write(prefs,write);return true;
        }
    }

    private static final int[] LOGIN_COINS = {40, 50, 60, 70, 80, 100, 150};

    /** Seven claimed visits, never a consecutive-day streak. Missing days lose nothing. */
    public synchronized int dailyLoginCount() {
        LocalSaveTransactions.flush(prefs);
        return Math.max(0, prefs.getInt("login_claims_v17", 0));
    }
    public synchronized boolean dailyLoginClaimed() {
        LocalSaveTransactions.flush(prefs);
        String latest=prefs.getString("login_latest_v17", "");
        // A backwards local clock cannot collect an older date or rewind the cycle.
        return !latest.isEmpty() && dailyId().compareTo(latest)<=0;
    }
    public synchronized int dailyLoginDay() {
        int count=dailyLoginCount();
        return dailyLoginClaimed() && count>0 ? (count-1)%7+1 : count%7+1;
    }
    public int dailyLoginCoins(int day) { return day>=1&&day<=7?LOGIN_COINS[day-1]:0; }
    public synchronized int claimDailyLogin() {
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            if(dailyLoginClaimed())return 0;
            int count=dailyLoginCount(), reward=dailyLoginCoins(count%7+1);
            Map<String,Object> write=new HashMap<>();
            write.put("login_latest_v17",dailyId());
            write.put("login_claims_v17",safeAdd(count,1));
            write.put("coins",safeAdd(coins(),reward));
            LocalSaveTransactions.write(prefs,write);
            return reward;
        }
    }

    /** The four wardrobe slots are strictly cosmetic and separate from legacy gear. */
    public synchronized boolean ownsCosmetic(String id) {
        LocalSaveTransactions.flush(prefs);
        CosmeticCatalog.Item item=CosmeticCatalog.find(id);
        return item!=null&&(item.source==CosmeticCatalog.Source.STARTER||(externalOwnership!=null&&externalOwnership.ownsCosmetic(id))||!prefs.getString("cosmetic_owned_v17_"+id,"").isEmpty());
    }
    public synchronized String cosmeticAcquisitionSource(String id) {
        if(!ownsCosmetic(id))return "";
        CosmeticCatalog.Item item=CosmeticCatalog.find(id);
        return item.source==CosmeticCatalog.Source.STARTER?"STARTER":prefs.getString("cosmetic_owned_v17_"+id,"");
    }
    public synchronized int cosmeticProgress(String id) {
        CosmeticCatalog.Item item=CosmeticCatalog.find(id);if(item==null)return 0;
        if(item.source==CosmeticCatalog.Source.LOGIN_DAYS)return dailyLoginCount();
        AvatarGearCatalog.Progress progress=avatarGearProgress();
        return item.source==CosmeticCatalog.Source.WEEKLY_WINS?progress.weeklyWins:
                item.source==CosmeticCatalog.Source.EVENT_STARS?progress.eventStars:0;
    }
    public synchronized boolean cosmeticClaimable(String id) {
        CosmeticCatalog.Item item=CosmeticCatalog.find(id);
        return item!=null&&!ownsCosmetic(id)&&item.source!=CosmeticCatalog.Source.EARNED_COINS
                &&cosmeticProgress(id)>=item.requirement;
    }
    /** Seasonal designs stay owned permanently and remain earnable through the event archive. */
    public synchronized List<String> claimCosmeticUnlocks() {
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            List<String> unlocked=new ArrayList<>();Map<String,Object> write=new HashMap<>();
            for(CosmeticCatalog.Item item:CosmeticCatalog.ITEMS)if(cosmeticClaimable(item.id)) {
                unlocked.add(item.id);write.put("cosmetic_owned_v17_"+item.id,item.source.name());
            }
            if(!write.isEmpty())LocalSaveTransactions.write(prefs,write);
            return Collections.unmodifiableList(unlocked);
        }
    }
    public synchronized boolean purchaseCosmetic(String id) {
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            CosmeticCatalog.Item item=CosmeticCatalog.find(id);
            if(item==null||item.source!=CosmeticCatalog.Source.EARNED_COINS||ownsCosmetic(id)||coins()<item.priceCoins)return false;
            Map<String,Object> write=new HashMap<>();write.put("coins",coins()-item.priceCoins);
            write.put("cosmetic_owned_v17_"+id,"EARNED_COINS");LocalSaveTransactions.write(prefs,write);return true;
        }
    }
    private static final String ORIGINAL_COMPANION_COSTUME_KEY="companion_original_costume_v118";
    /** Implicit starter slots are not a wardrobe choice; old explicitly saved selections remain visible. */
    public synchronized boolean usesOriginalCompanionCostume() {
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            Object stored=prefs.getAll().get(ORIGINAL_COMPANION_COSTUME_KEY);
            if(stored instanceof Boolean)return (Boolean)stored;
            for(CosmeticCatalog.Slot slot:CosmeticCatalog.Slot.values())
                if(prefs.contains("cosmetic_slot_v17_"+slot.name()))return false;
            return true;
        }
    }
    /** Changes presentation only. The four selected items, ownership, gear and progression stay intact. */
    public synchronized boolean setUseOriginalCompanionCostume(boolean original) {
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);Map<String,Object> write=new HashMap<>();
            write.put(ORIGINAL_COMPANION_COSTUME_KEY,original);LocalSaveTransactions.write(prefs,write);return true;
        }
    }
    public synchronized boolean equipCosmetic(String id) {
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            CosmeticCatalog.Item item=CosmeticCatalog.find(id);if(item==null||!ownsCosmetic(id))return false;
            Map<String,Object> write=new HashMap<>();write.put("cosmetic_slot_v17_"+item.slot.name(),id);write.put(ORIGINAL_COMPANION_COSTUME_KEY,false);
            LocalSaveTransactions.write(prefs,write);return true;
        }
    }
    public synchronized String equippedCosmetic(CosmeticCatalog.Slot slot) {
        LocalSaveTransactions.flush(prefs);if(slot==null)return "";
        String id=prefs.getString("cosmetic_slot_v17_"+slot.name(),CosmeticCatalog.starter(slot).id);
        CosmeticCatalog.Item item=CosmeticCatalog.find(id);
        return item!=null&&item.slot==slot&&ownsCosmetic(id)?id:CosmeticCatalog.starter(slot).id;
    }
    public synchronized int ownedCosmeticCount() {
        int count=0;for(CosmeticCatalog.Item item:CosmeticCatalog.ITEMS)if(ownsCosmetic(item.id))count++;return count;
    }
    public static final class LootResult {
        public final String itemId;
        public final boolean duplicate, replayed;
        public final int compensationCoins, chargedCoins;
        private LootResult(String id,boolean duplicate,int compensation,int charged,boolean replayed) {
            itemId=id;this.duplicate=duplicate;compensationCoins=compensation;chargedCoins=charged;this.replayed=replayed;
        }
    }
    private LootResult readCosmeticLoot(String receipt,boolean replayed) {
        String[] parts=receipt.split(":");if(parts.length!=3)return null;
        CosmeticCatalog.Item item=CosmeticCatalog.find(parts[0]);
        if(item==null||!("0".equals(parts[1])||"1".equals(parts[1])))return null;
        int compensation;
        try { compensation=Integer.parseInt(parts[2]); } catch(NumberFormatException invalid) { return null; }
        boolean duplicate="1".equals(parts[1]);
        if(compensation!=(duplicate?item.duplicateCoins():0))return null;
        return new LootResult(item.id,duplicate,compensation,replayed?0:CosmeticCatalog.LOOT_PRICE_COINS,replayed);
    }
    /** 150 earned LumaCoins only. Stable request, debit, outcome and ownership share one commit. */
    public synchronized LootResult drawCosmeticLoot(String requestId) {
        if(!validRewardId(requestId))return null;
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            String token="cosmetic_draw_v17_"+sha256(requestId),recorded=prefs.getString(token,"");
            if(!recorded.isEmpty())return readCosmeticLoot(recorded,true);
            if(coins()<CosmeticCatalog.LOOT_PRICE_COINS)return null;
            String seed=prefs.getString("cosmetic_seed_v17","");if(seed.isEmpty())seed=UUID.randomUUID().toString();
            CosmeticCatalog.Item item=CosmeticCatalog.draw(seed,requestId);boolean duplicate=ownsCosmetic(item.id);
            int compensation=duplicate?item.duplicateCoins():0;
            Map<String,Object> write=new HashMap<>();write.put("cosmetic_seed_v17",seed);
            write.put("coins",safeAdd(coins()-CosmeticCatalog.LOOT_PRICE_COINS,compensation));
            if(!duplicate)write.put("cosmetic_owned_v17_"+item.id,"EARNED_CAPSULE");
            write.put(token,item.id+":"+(duplicate?"1":"0")+":"+compensation);
            LocalSaveTransactions.write(prefs,write);
            return new LootResult(item.id,duplicate,compensation,CosmeticCatalog.LOOT_PRICE_COINS,false);
        }
    }

    /** The player card is a local cosmetic identity, never an account display name or a location claim. */
    public synchronized String playerNickname() {
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);
            String clean=PlayerProfile.normalizeNickname(prefs.getString("player_nickname_v116",PlayerProfile.DEFAULT_NICKNAME));
            return clean.isEmpty()?PlayerProfile.DEFAULT_NICKNAME:clean;
        }
    }
    public synchronized String playerFlag() {
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);String flag=prefs.getString("player_flag_v116",PlayerProfile.DEFAULT_FLAG);
            return PlayerProfile.validFlag(flag)?flag:PlayerProfile.DEFAULT_FLAG;
        }
    }
    public synchronized boolean setPlayerNickname(String nickname) {
        String clean=PlayerProfile.normalizeNickname(nickname);if(clean.isEmpty())return false;
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);Map<String,Object> write=new HashMap<>();
            write.put("player_nickname_v116",clean);LocalSaveTransactions.write(prefs,write);return true;
        }
    }
    public synchronized boolean setPlayerFlag(String flag) {
        if(!PlayerProfile.validFlag(flag))return false;
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);Map<String,Object> write=new HashMap<>();
            write.put("player_flag_v116",flag);LocalSaveTransactions.write(prefs,write);return true;
        }
    }
    /** Distinct furniture designs placed across all rooms, rather than repeated copies or floor surfaces. */
    private int placedHomeDesignsLocked(String replacementRoom,HomeLayout replacement) {
        Set<String> designs=new HashSet<>();
        for(String room:HomeCatalog.ROOMS) {
            HomeLayout layout=room.equals(replacementRoom)?replacement:homeLayoutLocked(room);
            if(layout!=null)for(HomeLayout.Placement item:layout.placements())designs.add(item.itemId);
        }
        return designs.size();
    }
    private int ownedCharacterCountLocked() {
        int total=0;for(Catalog.CharacterItem item:Catalog.CHARACTERS)if(ownsCharacter(item.id))total++;return total;
    }
    private int badgeProgressLocked(BadgeCatalog.Item item,String replacementRoom,HomeLayout replacement,int additionalAvatar) {
        if(item==null)return 0;
        return item.metric==BadgeCatalog.Metric.LEVELS?completedLevels():item.metric==BadgeCatalog.Metric.AVATARS?
                ownedCharacterCountLocked()+additionalAvatar:placedHomeDesignsLocked(replacementRoom,replacement);
    }
    /** Merge newly earned permanent awards into the same local transaction as the triggering change. */
    private void appendBadgeAwardsLocked(Map<String,Object> write,String room,HomeLayout layout,int additionalAvatar) {
        for(BadgeCatalog.Item item:BadgeCatalog.ITEMS)if(!prefs.getBoolean("badge_earned_v116_"+item.id,false)
                &&badgeProgressLocked(item,room,layout,additionalAvatar)>=item.requirement)
            write.put("badge_earned_v116_"+item.id,true);
    }
    private void refreshBadgeAwardsLocked() {
        Map<String,Object> write=new HashMap<>();appendBadgeAwardsLocked(write,null,null,0);
        if(!write.isEmpty())LocalSaveTransactions.write(prefs,write);
    }
    public synchronized int badgeProgress(String id) {
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);BadgeCatalog.Item item=BadgeCatalog.find(id);
            if(item==null)return 0;refreshBadgeAwardsLocked();
            int progress=badgeProgressLocked(item,null,null,0);
            return prefs.getBoolean("badge_earned_v116_"+id,false)?Math.max(progress,item.requirement):progress;
        }
    }
    public synchronized boolean badgeUnlocked(String id) {
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);if(BadgeCatalog.find(id)==null)return false;
            refreshBadgeAwardsLocked();return prefs.getBoolean("badge_earned_v116_"+id,false);
        }
    }
    /** Always three slots. Empty strings represent deliberately empty slots. No automatic badges are worn. */
    public synchronized List<String> equippedBadges() {
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);refreshBadgeAwardsLocked();List<String> result=new ArrayList<>();Set<String> seen=new HashSet<>();
            for(int slot=0;slot<BadgeCatalog.EQUIPPED_SLOTS;slot++) {
                String id=prefs.getString("player_badge_v116_"+slot,"");
                result.add(BadgeCatalog.find(id)!=null&&prefs.getBoolean("badge_earned_v116_"+id,false)&&seen.add(id)?id:"");
            }
            return Collections.unmodifiableList(result);
        }
    }
    /** Moving an existing badge clears its previous slot in the same commit. */
    public synchronized boolean equipBadge(int slot,String id) {
        if(slot<0||slot>=BadgeCatalog.EQUIPPED_SLOTS||id==null)return false;
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);if(!id.isEmpty()&&!badgeUnlocked(id))return false;
            List<String> before=equippedBadges();Map<String,Object> write=new HashMap<>();
            for(int index=0;index<BadgeCatalog.EQUIPPED_SLOTS;index++)
                write.put("player_badge_v116_"+index,index==slot?id:before.get(index).equals(id)?"":before.get(index));
            LocalSaveTransactions.write(prefs,write);return true;
        }
    }
    public synchronized boolean ownsSignatureOutfit(String id) {
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);SignatureOutfits.Outfit outfit=SignatureOutfits.find(id);if(outfit==null)return false;
            for(String cosmetic:outfit.cosmeticIds)if(!ownsCosmetic(cosmetic))return false;return true;
        }
    }
    /** All four visual slots change together; existing gear and free avatar styles keep their selections. */
    public synchronized boolean equipSignatureOutfit(String id) {
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);if(!ownsSignatureOutfit(id))return false;
            SignatureOutfits.Outfit outfit=SignatureOutfits.find(id);Map<String,Object> write=new HashMap<>();
            for(CosmeticCatalog.Slot slot:CosmeticCatalog.Slot.values())write.put("cosmetic_slot_v17_"+slot.name(),outfit.item(slot));
            write.put(ORIGINAL_COMPANION_COSTUME_KEY,false);
            LocalSaveTransactions.write(prefs,write);return true;
        }
    }
    public synchronized boolean ownsSummonItem(String id) {
        SummonCatalog.Item item=SummonCatalog.find(id);
        return item!=null&&(item.kind==SummonCatalog.Kind.AVATAR?ownsCharacter(item.avatarId):ownsSignatureOutfit(item.outfitId));
    }
    public synchronized boolean equipSummonItem(String id) {
        SummonCatalog.Item item=SummonCatalog.find(id);
        return item!=null&&(item.kind==SummonCatalog.Kind.AVATAR?equipCharacter(item.avatarId):equipSignatureOutfit(item.outfitId));
    }
    public static final class SummonResult {
        public final SummonCatalog.Banner banner;
        public final String itemId, requestId;
        public final boolean duplicate,replayed;
        public final int compensationCoins,chargedCoins;
        private SummonResult(SummonCatalog.Banner banner,String itemId,String requestId,boolean duplicate,int compensation,int charged,boolean replayed) {
            this.banner=banner;this.itemId=itemId;this.requestId=requestId;this.duplicate=duplicate;
            compensationCoins=compensation;chargedCoins=charged;this.replayed=replayed;
        }
    }
    private static String summonToken(String requestId) { return "summon_"+SummonCatalog.VERSION+"_"+sha256(requestId); }
    private SummonResult readSummonReceipt(String record,String requestId,boolean replayed) {
        String[] parts=record.split(":",-1);if(parts.length!=4)return null;
        SummonCatalog.Banner banner;
        try{banner=SummonCatalog.Banner.valueOf(parts[0]);}catch(IllegalArgumentException invalid){return null;}
        SummonCatalog.Item item=SummonCatalog.find(banner,parts[1]);
        if(item==null||!("0".equals(parts[2])||"1".equals(parts[2])))return null;
        int compensation;try{compensation=Integer.parseInt(parts[3]);}catch(NumberFormatException invalid){return null;}
        boolean duplicate="1".equals(parts[2]);if(compensation!=(duplicate?item.duplicateCoins:0))return null;
        return new SummonResult(banner,item.id,requestId,duplicate,compensation,replayed?0:SummonCatalog.priceCoins(banner),replayed);
    }
    /** A read recovers only a completed transaction. It never creates a new draw or debits coins. */
    public synchronized SummonResult summonReceipt(String requestId) {
        if(!validRewardId(requestId))return null;
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);return readSummonReceipt(prefs.getString(summonToken(requestId),""),requestId,true);
        }
    }
    public synchronized String lastSummonRequestId() {
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);String id=prefs.getString("summon_last_request_"+SummonCatalog.VERSION,"");
            return validRewardId(id)&&readSummonReceipt(prefs.getString(summonToken(id),""),id,true)!=null?id:"";
        }
    }
    public synchronized SummonResult lastSummonResult() { return summonReceipt(lastSummonRequestId()); }
    /** Versioned pool, seed, exact outcome, debit, ownership and last reveal identity commit atomically. */
    public synchronized SummonResult drawSummon(SummonCatalog.Banner banner,String requestId) {
        if(banner==null||!validRewardId(requestId))return null;
        synchronized(GOLD_GRANT_LOCK) {
            LocalSaveTransactions.flush(prefs);String token=summonToken(requestId),record=prefs.getString(token,"");
            if(!record.isEmpty()) {
                SummonResult old=readSummonReceipt(record,requestId,true);return old!=null&&old.banner==banner?old:null;
            }
            int price=SummonCatalog.priceCoins(banner);if(coins()<price)return null;
            String seed=prefs.getString("summon_seed_"+SummonCatalog.VERSION,"");if(seed.isEmpty())seed=UUID.randomUUID().toString();
            SummonCatalog.Item item=SummonCatalog.draw(seed,banner,requestId);boolean duplicate=ownsSummonItem(item.id);
            int compensation=duplicate?item.duplicateCoins:0;Map<String,Object> write=new HashMap<>();
            write.put("summon_seed_"+SummonCatalog.VERSION,seed);write.put("coins",safeAdd(coins()-price,compensation));
            if(!duplicate) {
                if(item.kind==SummonCatalog.Kind.AVATAR)write.put("owned_character_"+item.avatarId,true);
                else for(String cosmetic:SignatureOutfits.find(item.outfitId).cosmeticIds)if(!ownsCosmetic(cosmetic))
                    write.put("cosmetic_owned_v17_"+cosmetic,"EARNED_SUMMON_"+SummonCatalog.VERSION);
            }
            write.put(token,banner.name()+":"+item.id+":"+(duplicate?"1":"0")+":"+compensation);
            write.put("summon_last_request_"+SummonCatalog.VERSION,requestId);
            // New avatar receipts also record a copy. Old receipts remain immutable and replay never backfills copies.
            if(item.kind==SummonCatalog.Kind.AVATAR)appendCompanionCopyLocked(write,item.avatarId);
            appendBadgeAwardsLocked(write,null,null,!duplicate&&item.kind==SummonCatalog.Kind.AVATAR?1:0);
            LocalSaveTransactions.write(prefs,write);
            return new SummonResult(banner,item.id,requestId,duplicate,compensation,price,false);
        }
    }

    private static final String EXPEDITION_KEY="expedition_v118_run";
    public static final int EXPEDITION_HOME_TARGET=12;
    public synchronized int companionCopies(String id){
        synchronized(GOLD_GRANT_LOCK){LocalSaveTransactions.flush(prefs);if(CompanionProgression.find(id)==null)return 0;
            return Math.min(CompanionProgression.MAX_COPIES,CompanionProgression.initialCopies(ownsCharacter(id),prefs.getInt("companion_copies_v118_"+id,0)));}
    }
    public synchronized CompanionProgression.Stats companionStats(String id){return CompanionProgression.stats(companionCopies(id));}
    public synchronized String companionStance(String id){synchronized(GOLD_GRANT_LOCK){LocalSaveTransactions.flush(prefs);String stance=prefs.getString("companion_stance_v118_"+id,"CALM");return validCompanionStance(stance)?stance:"CALM";}}
    private static boolean validCompanionStance(String stance){return "CALM".equals(stance)||"READY".equals(stance)||"GUARD".equals(stance)||"PROUD".equals(stance);}
    public synchronized boolean setCompanionStance(String id,String stance){
        synchronized(GOLD_GRANT_LOCK){LocalSaveTransactions.flush(prefs);if(!ownsCharacter(id)||!validCompanionStance(stance))return false;Map<String,Object> write=new HashMap<>();write.put("companion_stance_v118_"+id,stance);LocalSaveTransactions.write(prefs,write);return true;}
    }
    private int appendCompanionCopyLocked(Map<String,Object> write,String id){
        String key="companion_copies_v118_"+id;
        int before=write.containsKey(key)?(Integer)write.get(key):companionCopies(id);
        int after=Math.min(CompanionProgression.MAX_COPIES,CompanionProgression.addCopies(before,1));
        write.put(key,after);return before;
    }
    public synchronized ExpeditionRun expeditionRun(){
        synchronized(GOLD_GRANT_LOCK){LocalSaveTransactions.flush(prefs);return ExpeditionRun.decode(prefs.getString(EXPEDITION_KEY,""));}
    }
    public synchronized int expeditionHomeProgress(){synchronized(GOLD_GRANT_LOCK){LocalSaveTransactions.flush(prefs);return Math.max(0,Math.min(EXPEDITION_HOME_TARGET,prefs.getInt("expedition_v118_home",0)));}}
    public synchronized int expeditionsCompleted(){synchronized(GOLD_GRANT_LOCK){LocalSaveTransactions.flush(prefs);return Math.max(0,prefs.getInt("expedition_v118_completed",0));}}
    public synchronized ExpeditionRun beginExpedition(String companion,ExpeditionRun.Goal goal,String target,String requestId){
        if(!validRewardId(requestId)||!CompanionProgression.expeditionAvailable(companion)||!ownsCharacter(companion)||goal==null)return null;
        synchronized(GOLD_GRANT_LOCK){LocalSaveTransactions.flush(prefs);ExpeditionRun old=expeditionRun();
            if(old!=null&&old.id.equals(requestId))return old;
            if(expeditionReward(requestId)!=null)return null;
            if(old!=null&&!old.claimed)return old.id.equals(requestId)?old:null;
            if(goal==ExpeditionRun.Goal.COPIES&&!ownsCharacter(target)||goal==ExpeditionRun.Goal.HOME&&!ExpeditionCatalog.SOUVENIR_ID.equals(target))return null;
            long seed=UUID.randomUUID().getMostSignificantBits()&Long.MAX_VALUE;
            ExpeditionRun next=ExpeditionRun.start(requestId,seed,companion,companionStats(companion).level,goal,target);
            if(next==null)return null;Map<String,Object> write=new HashMap<>();write.put(EXPEDITION_KEY,next.encode());LocalSaveTransactions.write(prefs,write);return next;}
    }
    public synchronized ExpeditionRun chooseExpedition(String id,String optionId){
        synchronized(GOLD_GRANT_LOCK){LocalSaveTransactions.flush(prefs);ExpeditionRun old=expeditionRun();if(old==null||!old.id.equals(id))return null;
            ExpeditionRun next=old.choose(optionId);if(next==null)return null;Map<String,Object> write=new HashMap<>();write.put(EXPEDITION_KEY,next.encode());LocalSaveTransactions.write(prefs,write);return next;}
    }
    public synchronized ExpeditionRun useExpeditionSkill(String id,String optionId){
        synchronized(GOLD_GRANT_LOCK){LocalSaveTransactions.flush(prefs);ExpeditionRun old=expeditionRun();if(old==null||!old.id.equals(id))return null;
            ExpeditionRun next=old.useSkill(optionId);if(next==null)return null;Map<String,Object> write=new HashMap<>();write.put(EXPEDITION_KEY,next.encode());LocalSaveTransactions.write(prefs,write);return next;}
    }
    /** UI calls only after solving the exact selected U board. Stage receipt and route advance persist together. */
    public synchronized ExpeditionRun completeExpeditionStage(String id,String victoryId,int boardNumber,int stars){
        synchronized(GOLD_GRANT_LOCK){LocalSaveTransactions.flush(prefs);ExpeditionRun old=expeditionRun();if(old==null||!old.id.equals(id))return null;
            ExpeditionRun next=old.complete(victoryId,boardNumber,stars);if(next==null)return null;Map<String,Object> write=new HashMap<>();write.put(EXPEDITION_KEY,next.encode());LocalSaveTransactions.write(prefs,write);return next;}
    }
    public static final class ExpeditionReward{
        public final String runId,targetId,discoveryId;
        public final int targetBefore,targetAfter,discoveryBefore,discoveryAfter,homeBefore,homeAfter,coins;
        public final boolean discoveryDuplicate;
        private ExpeditionReward(String runId,String targetId,String discoveryId,int tb,int ta,int db,int da,int hb,int ha,int coins,boolean duplicate){
            this.runId=runId;this.targetId=targetId;this.discoveryId=discoveryId;targetBefore=tb;targetAfter=ta;discoveryBefore=db;discoveryAfter=da;homeBefore=hb;homeAfter=ha;this.coins=coins;discoveryDuplicate=duplicate;}
        private String encode(){return runId+"|"+targetId+"|"+discoveryId+"|"+targetBefore+"|"+targetAfter+"|"+discoveryBefore+"|"+discoveryAfter+"|"+homeBefore+"|"+homeAfter+"|"+coins+"|"+(discoveryDuplicate?1:0);}
        private static ExpeditionReward decode(String text){try{String[] p=text.split("\\|",-1);if(p.length!=11||!validRewardId(p[0])||SummonCatalog.find(SummonCatalog.Banner.AVATARS,p[2])==null||!p[1].isEmpty()&&CompanionProgression.find(p[1])==null)return null;
                int[] n=new int[8];for(int i=0;i<n.length;i++){n[i]=Integer.parseInt(p[i+3]);if(n[i]<0)return null;}if(n[7]>1)return null;
                if(n[0]>92||n[1]>92||n[2]>92||n[3]>92||n[4]>12||n[5]>12||n[4]%4!=0||n[5]%4!=0||n[6]>165)return null;
                if(n[3]!=Math.min(92,n[2]+1)||n[7]!=(n[2]>0?1:0))return null;
                if(p[1].isEmpty()){if(n[0]!=0||n[1]!=0||n[5]!=Math.min(12,n[4]+4))return null;}
                else if(n[0]<1||n[1]!=Math.min(92,n[0]+1)||n[4]!=n[5])return null;
                int expected=(p[1].isEmpty()?n[4]==12:n[0]==92)?45:0;if(n[2]==92)expected+=SummonCatalog.find(SummonCatalog.Banner.AVATARS,p[2]).duplicateCoins;if(n[6]!=expected)return null;
                return new ExpeditionReward(p[0],p[1],p[2],n[0],n[1],n[2],n[3],n[4],n[5],n[6],n[7]==1);
            }catch(RuntimeException invalid){return null;}}
    }
    private static String expeditionRewardKey(String id){return "expedition_v118_reward_"+sha256(id);}
    public synchronized ExpeditionReward expeditionReward(String id){if(!validRewardId(id))return null;synchronized(GOLD_GRANT_LOCK){LocalSaveTransactions.flush(prefs);ExpeditionReward result=ExpeditionReward.decode(prefs.getString(expeditionRewardKey(id),""));return result!=null&&result.runId.equals(id)?result:null;}}
    /** Free expedition discovery uses the published avatar odds. Copies replace refunds here; paid-coin arrivals retain their old refunds. */
    public synchronized ExpeditionReward claimExpedition(String id){
        if(!validRewardId(id))return null;
        synchronized(GOLD_GRANT_LOCK){LocalSaveTransactions.flush(prefs);ExpeditionReward previous=expeditionReward(id);if(previous!=null)return previous;
            ExpeditionRun run=expeditionRun();if(run==null||!run.id.equals(id)||!run.finished()||run.claimed)return null;
            Map<String,Object> write=new HashMap<>();int targetBefore=0,targetAfter=0,homeBefore=expeditionHomeProgress(),homeAfter=homeBefore,overflow=0;String target="";
            if(run.goal==ExpeditionRun.Goal.COPIES){target=run.targetId;if(!ownsCharacter(target))return null;targetBefore=appendCompanionCopyLocked(write,target);targetAfter=Math.min(CompanionProgression.MAX_COPIES,targetBefore+1);if(targetBefore>=CompanionProgression.MAX_COPIES)overflow+=45;}
            else{homeAfter=Math.min(EXPEDITION_HOME_TARGET,homeBefore+run.homeContributionReward());write.put("expedition_v118_home",homeAfter);if(homeBefore==EXPEDITION_HOME_TARGET)overflow+=45;}
            SummonCatalog.Item item=SummonCatalog.draw("expedition:"+run.seed,SummonCatalog.Banner.AVATARS,id);
            boolean duplicate=ownsCharacter(item.avatarId);int discoveryBefore=appendCompanionCopyLocked(write,item.avatarId),discoveryAfter=Math.min(CompanionProgression.MAX_COPIES,discoveryBefore+1);
            write.put("owned_character_"+item.avatarId,true);if(discoveryBefore>=CompanionProgression.MAX_COPIES)overflow+=item.duplicateCoins;
            write.put("coins",safeAdd(coins(),overflow));write.put("expedition_v118_completed",safeAdd(expeditionsCompleted(),1));write.put(EXPEDITION_KEY,run.claim().encode());
            ExpeditionReward result=new ExpeditionReward(id,target,item.avatarId,targetBefore,targetAfter,discoveryBefore,discoveryAfter,homeBefore,homeAfter,overflow,duplicate);
            write.put(expeditionRewardKey(id),result.encode());appendBadgeAwardsLocked(write,null,null,duplicate?0:1);LocalSaveTransactions.write(prefs,write);return result;}
    }

    /**
     * Whether this save currently records the transaction ID. Call grantGold without
     * a persistence error before treating an in-memory token as durably fulfilled.
     */
    public synchronized boolean isGoldGranted(String transactionId) {
        if (transactionId == null || !transactionId.matches("[a-zA-Z0-9_:-]{1,160}")) return false;
        synchronized (GOLD_GRANT_LOCK) {
            return prefs.getBoolean("gold_tx_v1_" + sha256(transactionId), false);
        }
    }

    /**
     * Atomic local fulfillment, currently for explicit sandbox simulations only.
     * This method does not verify a payment. Any future live caller must first verify
     * a unique PURCHASED transaction with a trusted backend and pass its stable ID.
     * Raw purchase tokens/IDs are never used as preference keys or exported here.
     * Full-wallet requests are rejected without consuming the transaction ID.
     */
    public synchronized int grantGold(String transactionId, int amount) {
        if (transactionId == null || !transactionId.matches("[a-zA-Z0-9_:-]{1,160}")
                || amount < 1 || amount > 100_000) return 0;
        // Also serialize independent GameStore handles sharing this process's save.
        synchronized (GOLD_GRANT_LOCK) {
            String token = "gold_tx_v1_" + sha256(transactionId);
            if (prefs.getBoolean(token, false)) {
                // Android commits memory before disk. A previous failed commit can
                // leave the token and credit in memory: flush them before success.
                boolean persisted = prefs.edit().putBoolean(token, true).putInt("coins", coins()).commit();
                if (!persisted) throw new IllegalStateException("Could not persist recorded gold transaction");
                return 0;
            }
            if (coins() > MAX_COINS - amount) return 0;
            boolean persisted = prefs.edit().putBoolean(token, true).putInt("coins", coins() + amount).commit();
            if (!persisted) throw new IllegalStateException("Could not persist gold transaction");
            return amount;
        }
    }

    /** Fixed, public continuation cost; kept equal to MoveBudget.GOLD_EXTENSION_COST by tests. */
    public static final int CONTINUE_GOLD_COST = 100;

    /**
     * Whether this process currently records a continuation debit. As with gold
     * grants, first call spendGoldOnce without a persistence error before treating
     * an in-memory marker as a durable transaction.
     */
    public synchronized boolean hasGoldSpent(String requestId) {
        if (requestId == null || !requestId.matches("[a-zA-Z0-9_:-]{1,160}")) return false;
        synchronized (GOLD_GRANT_LOCK) {
            return prefs.getBoolean("gold_spend_v1_" + sha256(requestId), false);
        }
    }

    /**
     * Continuation-only local debit: exactly 100 gold, never a real payment.
     * Returns 100 for a new debit, 0 for a recorded duplicate or invalid/unfunded
     * request. hasGoldSpent distinguishes a duplicate from a refusal. Amounts
     * other than the published fixed cost are rejected, including on retry.
     * Debit and hashed marker use one commit, under the same process-wide lock
     * as credits. A duplicate flushes Android's memory-before-disk state before
     * reporting success, so a failed commit can retry without a second debit.
     */
    public synchronized int spendGoldOnce(String requestId, int amount) {
        if (requestId == null || !requestId.matches("[a-zA-Z0-9_:-]{1,160}")
                || amount != CONTINUE_GOLD_COST) return 0;
        synchronized (GOLD_GRANT_LOCK) {
            String token = "gold_spend_v1_" + sha256(requestId);
            if (prefs.getBoolean(token, false)) {
                boolean persisted = prefs.edit().putBoolean(token, true).putInt("coins", coins()).commit();
                if (!persisted) throw new IllegalStateException("Could not persist recorded gold debit");
                return 0;
            }
            int balance = coins();
            if (balance < amount) return 0;
            boolean persisted = prefs.edit().putBoolean(token, true).putInt("coins", balance - amount).commit();
            if (!persisted) throw new IllegalStateException("Could not persist gold debit");
            return amount;
        }
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            char[] hex = "0123456789abcdef".toCharArray();
            StringBuilder result = new StringBuilder(64);
            for (byte part : digest) {
                result.append(hex[(part & 0xff) >>> 4]).append(hex[part & 15]);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException unavailable) {
            throw new IllegalStateException("SHA-256 unavailable", unavailable);
        }
    }

    private static int clampStars(int stars) { return Math.max(0, Math.min(3, stars)); }
    /** Local visual choice only; never changes gameplay, prices or account ownership. */
    public synchronized String avatarStyle() {
        LocalSaveTransactions.flush(prefs);
        return AvatarStyles.find(prefs.getString("avatar_style_v19", "plush_classic")).id;
    }
    public synchronized boolean equipAvatarStyle(String id) {
        boolean valid=false;for(AvatarStyles.Style style:AvatarStyles.all())if(style.id.equals(id))valid=true;
        if(!valid)return false;
        synchronized(LocalSaveTransactions.LOCK){LocalSaveTransactions.flush(prefs);Map<String,Object> write=new HashMap<>();write.put("avatar_style_v19",id);LocalSaveTransactions.write(prefs,write);return true;}
    }
    public synchronized boolean guardianUnlocked(int number) { return number>=1&&number<=100&&completedLevels()>=number*10; }
    public synchronized boolean guardianCleared(int number) { LocalSaveTransactions.flush(prefs);return number>=1&&number<=100&&prefs.getBoolean("guardian_v19_"+number,false); }
    /** First pending encounter, including its actual locked requirement. */
    public synchronized int nextGuardian() { for(int n=1;n<=100;n++)if(!guardianCleared(n))return n;return 100; }
    /** Call only after the native GuardianEncounter is solved. Currency and receipt commit together. */
    public synchronized int completeGuardian(int number) {
        synchronized(LocalSaveTransactions.LOCK){
            LocalSaveTransactions.flush(prefs);
            if(!guardianUnlocked(number)||guardianCleared(number)||coins()>MAX_COINS-100)return 0;
            Map<String,Object> write=new HashMap<>();write.put("guardian_v19_"+number,true);write.put("coins",coins()+100);
            LocalSaveTransactions.write(prefs,write);return 100;
        }
    }

    /** The three-piece first touch is practice only: it never grants progress, currency or items. */
    public synchronized boolean firstLightSeen(){
        synchronized(LocalSaveTransactions.LOCK){LocalSaveTransactions.flush(prefs);return prefs.getBoolean("first_light_seen_v113",false);}
    }
    public synchronized String firstLightHistory(){
        synchronized(LocalSaveTransactions.LOCK){LocalSaveTransactions.flush(prefs);String history=prefs.getString("first_light_history_v113","");return validFirstLightHistory(history)?history:"";}
    }
    public synchronized void saveFirstLightHistory(String history){
        if(!validFirstLightHistory(history))throw new IllegalArgumentException("Invalid first light history");
        synchronized(LocalSaveTransactions.LOCK){LocalSaveTransactions.flush(prefs);if(firstLightSeen())return;Map<String,Object> write=new HashMap<>();write.put("first_light_history_v113",history);LocalSaveTransactions.write(prefs,write);}
    }
    private static boolean validFirstLightHistory(String history){
        if(history==null||history.length()>5||!history.matches("(?:[0-2](?:,[0-2])*)?"))return false;
        boolean[] seen=new boolean[3];if(!history.isEmpty())for(String id:history.split(",")){int index=Integer.parseInt(id);if(seen[index])return false;seen[index]=true;}return true;
    }
    /** Completion and explicit skip share one durable marker; neither simulates a Journey victory. */
    public synchronized void finishFirstLight(){
        synchronized(LocalSaveTransactions.LOCK){LocalSaveTransactions.flush(prefs);if(firstLightSeen())return;Map<String,Object> write=new HashMap<>();write.put("first_light_seen_v113",true);write.put("first_light_history_v113","");LocalSaveTransactions.write(prefs,write);}
    }
    public synchronized boolean shouldStartFirstLight(){
        synchronized(LocalSaveTransactions.LOCK){LocalSaveTransactions.flush(prefs);return prefs.getBoolean("tutorial_v18_fresh_profile",false)&&!firstLightSeen()&&!tutorialSeen()&&!tutorialRewardClaimed()&&tutorialState().isEmpty()&&unlockedLevel()==1&&completedLevels()==0;}
    }

    /** Each room has a complete atomic layout. The living room keeps its original save key. */
    public synchronized String homeRoom(){
        synchronized(LocalSaveTransactions.LOCK){LocalSaveTransactions.flush(prefs);return homeRoomLocked();}
    }
    private String homeRoomLocked(){String id=prefs.getString("home_room_v115",HomeCatalog.DEFAULT_ROOM);return HomeCatalog.validRoom(id)?id:HomeCatalog.DEFAULT_ROOM;}
    public synchronized boolean setHomeRoom(String room){
        synchronized(LocalSaveTransactions.LOCK){LocalSaveTransactions.flush(prefs);if(!HomeCatalog.validRoom(room))return false;Map<String,Object> write=new HashMap<>();write.put("home_room_v115",room);LocalSaveTransactions.write(prefs,write);return true;}
    }
    private static String homeLayoutKey(String room){return HomeCatalog.DEFAULT_ROOM.equals(room)?"home_layout_v113":"home_layout_v115_"+room;}
    private static String homeSurfaceKey(String room,boolean wall){String kind=wall?"wallpaper":"floor";return HomeCatalog.DEFAULT_ROOM.equals(room)?"home_"+kind+"_v113":"home_"+kind+"_v115_"+room;}
    public synchronized HomeLayout homeLayout(){
        synchronized(LocalSaveTransactions.LOCK){LocalSaveTransactions.flush(prefs);return homeLayoutLocked(homeRoomLocked());}
    }
    /** Invalid room ids return null. Reading an old save never rewrites or relocates it. */
    public synchronized HomeLayout homeLayout(String room){
        synchronized(LocalSaveTransactions.LOCK){LocalSaveTransactions.flush(prefs);return HomeCatalog.validRoom(room)?homeLayoutLocked(room):null;}
    }
    private HomeLayout homeLayoutLocked(String room){
        HomeLayout layout=HomeLayout.decode(prefs.getString(homeLayoutKey(room),""));
        if(layout==null)return HomeLayout.starter(room);
        for(HomeLayout.Placement p:new ArrayList<>(layout.placements()))if(!ownsHomeItemLocked(p.itemId))layout=layout.tryRemove(p.id);
        return layout;
    }
    private boolean ownsHomeItemLocked(String id){int price=HomeCatalog.price(id);return price>=0&&(price==0||prefs.getBoolean("home_owned_v113_"+id,false));}
    public synchronized boolean ownsHomeItem(String id){synchronized(LocalSaveTransactions.LOCK){LocalSaveTransactions.flush(prefs);return ownsHomeItemLocked(id);}}
    /** Debit and permanent ownership are one exact, retryable transaction. Buying never places an object. */
    public synchronized boolean purchaseHomeItem(String id){
        synchronized(LocalSaveTransactions.LOCK){
            LocalSaveTransactions.flush(prefs);int price=HomeCatalog.price(id);if(price<0||ownsHomeItemLocked(id)||coins()<price)return false;
            Map<String,Object> write=new HashMap<>();write.put("coins",coins()-price);write.put("home_owned_v113_"+id,true);
            if(id.equals(prefs.getString("home_goal_v113","")))write.put("home_goal_v113","");
            LocalSaveTransactions.write(prefs,write);return true;
        }
    }
    public synchronized boolean placeHomeItem(String id,int x,int y,int rotation){
        synchronized(LocalSaveTransactions.LOCK){LocalSaveTransactions.flush(prefs);if(HomeCatalog.item(id)==null||!ownsHomeItemLocked(id))return false;String room=homeRoomLocked();return saveHomeLayoutLocked(room,homeLayoutLocked(room).tryPlace(id,x,y,rotation));}
    }
    public synchronized boolean removeHomeItem(String id){
        synchronized(LocalSaveTransactions.LOCK){LocalSaveTransactions.flush(prefs);String room=homeRoomLocked();return saveHomeLayoutLocked(room,homeLayoutLocked(room).tryRemove(id));}
    }
    public synchronized boolean moveHomeCompanion(int x,int y){
        synchronized(LocalSaveTransactions.LOCK){LocalSaveTransactions.flush(prefs);String room=homeRoomLocked();return saveHomeLayoutLocked(room,homeLayoutLocked(room).tryMove(x,y));}
    }
    private boolean saveHomeLayoutLocked(String room,HomeLayout next){
        if(next==null)return false;Map<String,Object> write=new HashMap<>();
        // Preserve awards achieved by the old arrangement even if this edit removes furniture.
        appendBadgeAwardsLocked(write,null,null,0);appendBadgeAwardsLocked(write,room,next,0);
        write.put(homeLayoutKey(room),next.encode());LocalSaveTransactions.write(prefs,write);return true;
    }
    public synchronized String homeWallpaper(){synchronized(LocalSaveTransactions.LOCK){LocalSaveTransactions.flush(prefs);return homeSurfaceLocked(homeRoomLocked(),true);}}
    public synchronized String homeFloor(){synchronized(LocalSaveTransactions.LOCK){LocalSaveTransactions.flush(prefs);return homeSurfaceLocked(homeRoomLocked(),false);}}
    public synchronized String homeWallpaper(String room){synchronized(LocalSaveTransactions.LOCK){LocalSaveTransactions.flush(prefs);return HomeCatalog.validRoom(room)?homeSurfaceLocked(room,true):null;}}
    public synchronized String homeFloor(String room){synchronized(LocalSaveTransactions.LOCK){LocalSaveTransactions.flush(prefs);return HomeCatalog.validRoom(room)?homeSurfaceLocked(room,false):null;}}
    private String homeSurfaceLocked(String room,boolean wall){
        String fallback=wall&&!HomeCatalog.DEFAULT_ROOM.equals(room)?"wallpaper_sage":wall?HomeCatalog.DEFAULT_WALLPAPER:HomeCatalog.DEFAULT_FLOOR;
        String id=prefs.getString(homeSurfaceKey(room,wall),fallback);
        return (wall?HomeCatalog.wallpaper(id):HomeCatalog.floor(id))!=null&&ownsHomeItemLocked(id)?id:fallback;
    }
    public synchronized boolean setHomeWallpaper(String id){return setHomeSurface(id,true);}
    public synchronized boolean setHomeFloor(String id){return setHomeSurface(id,false);}
    private boolean setHomeSurface(String id,boolean wall){
        synchronized(LocalSaveTransactions.LOCK){LocalSaveTransactions.flush(prefs);if((wall?HomeCatalog.wallpaper(id):HomeCatalog.floor(id))==null||!ownsHomeItemLocked(id))return false;Map<String,Object> write=new HashMap<>();write.put(homeSurfaceKey(homeRoomLocked(),wall),id);LocalSaveTransactions.write(prefs,write);return true;}
    }
    public synchronized String homeGoal(){
        synchronized(LocalSaveTransactions.LOCK){LocalSaveTransactions.flush(prefs);String id=prefs.getString("home_goal_v113","");return HomeCatalog.item(id)!=null&&!ownsHomeItemLocked(id)?id:"";}
    }
    /** One optional furniture wish. Empty clears it; owned furniture cannot become a shopping wish. */
    public synchronized boolean setHomeGoal(String id){
        synchronized(LocalSaveTransactions.LOCK){LocalSaveTransactions.flush(prefs);if(id==null||(!id.isEmpty()&&(HomeCatalog.item(id)==null||ownsHomeItemLocked(id))))return false;Map<String,Object> write=new HashMap<>();write.put("home_goal_v113",id);LocalSaveTransactions.write(prefs,write);return true;}
    }

    private static int safeAdd(int balance, int amount) { return (int) Math.min(MAX_COINS, (long) balance + Math.max(0, amount)); }
}
