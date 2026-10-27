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

/**
 * Local game progression and sandbox economy ledger. These methods never verify
 * a real payment; replaying an already earned star cannot farm rewards.
 * Level numbers are 1-based. Event puzzle indexes are 0-based.
 */
// Synchronous small local transactions intentionally persist economy before action returns.
@SuppressLint("ApplySharedPref")
public final class GameStore {
    /** Optional account-bound permanent ownership; never imports purchased gold into local coins. */
    public interface ExternalOwnership {
        boolean ownsCharacter(String id);
        boolean ownsSkin(String id);
    }
    private static final String PREFS = "luma_drift_save_v1";
    private static final int MAX_COINS = 2_000_000_000;
    private static final int MAX_STOCK = 999;
    private static final Object GOLD_GRANT_LOCK = new Object();
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

    public GameStore(Context context) {
        this(context, null);
    }

    public GameStore(Context context, ExternalOwnership ownership) {
        externalOwnership = ownership;
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!prefs.contains("initialized")) {
            prefs.edit().putBoolean("initialized", true)
                    .putInt("coins", 180)
                    .putInt("booster_hint", 3)
                    .putInt("booster_vision", 3)
                    .putInt("booster_flow", 2)
                    .putString("character", "nova")
                    .putString("skin", "lumina")
                    .putBoolean("owned_character_nova", true)
                    .putBoolean("owned_skin_lumina", true)
                    .commit();
        }
    }

    public synchronized int coins() { return Math.max(0, prefs.getInt("coins", 0)); }

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
        String id = prefs.getString("character", "nova");
        return Catalog.character(id) != null && ownsCharacter(id) ? id : "nova";
    }

    public synchronized String equippedSkin() {
        String id = prefs.getString("skin", "lumina");
        return Catalog.skin(id) != null && ownsSkin(id) ? id : "lumina";
    }

    public synchronized boolean ownsCharacter(String id) {
        return Catalog.character(id) != null && ("nova".equals(id) || prefs.getBoolean("owned_character_" + id, false)
                || externalOwnership != null && externalOwnership.ownsCharacter(id));
    }

    public synchronized boolean ownsSkin(String id) {
        return Catalog.skin(id) != null && ("lumina".equals(id) || prefs.getBoolean("owned_skin_" + id, false)
                || externalOwnership != null && externalOwnership.ownsSkin(id));
    }

    public synchronized boolean purchaseCharacter(String id) {
        Catalog.CharacterItem item = Catalog.character(id);
        if (item == null || ownsCharacter(id) || coins() < item.price) return false;
        prefs.edit().putInt("coins", coins() - item.price)
                .putBoolean("owned_character_" + id, true).commit();
        return true;
    }

    public synchronized boolean purchaseSkin(String id) {
        Catalog.SkinItem item = Catalog.skin(id);
        if (item == null || ownsSkin(id) || coins() < item.price) return false;
        prefs.edit().putInt("coins", coins() - item.price)
                .putBoolean("owned_skin_" + id, true).commit();
        return true;
    }

    public synchronized boolean equipCharacter(String id) {
        if (!ownsCharacter(id)) return false;
        prefs.edit().putString("character", id).commit();
        return true;
    }

    public synchronized boolean equipSkin(String id) {
        if (!ownsSkin(id)) return false;
        prefs.edit().putString("skin", id).commit();
        return true;
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
        AvatarGearCatalog.Item item=AvatarGearCatalog.find(id);
        return item!=null&&(item.source==AvatarGearCatalog.Source.STARTER||!prefs.getString("avatar_owned_v1_"+id,"").isEmpty());
    }
    public synchronized String avatarGearAcquisitionSource(String id) {
        AvatarGearCatalog.Item item=AvatarGearCatalog.find(id);
        if(item==null)return "";
        return item.source==AvatarGearCatalog.Source.STARTER?AvatarGearCatalog.Source.STARTER.name():prefs.getString("avatar_owned_v1_"+id,"");
    }
    /** Call on resume, after a win or opening equipment; not from every rendering frame. */
    public synchronized List<String> claimAvatarGearUnlocks() {
        synchronized(GOLD_GRANT_LOCK) {
            AvatarGearCatalog.Progress progress=avatarGearProgress();
            List<String> unlocked=new ArrayList<>();SharedPreferences.Editor edit=prefs.edit();
            for(AvatarGearCatalog.Item item:AvatarGearCatalog.ITEMS)if(AvatarGearCatalog.claimable(item,progress)) {
                String key="avatar_owned_v1_"+item.id;
                if(!ownsAvatarGear(item.id))unlocked.add(item.id);
                // Flush previously memory-applied writes too after a disk failure.
                String acquired=prefs.getString(key, "");
                edit.putString(key,acquired.isEmpty()?item.source.name():acquired);
            }
            if(!edit.commit())throw new IllegalStateException("Could not persist earned avatar equipment");
            return Collections.unmodifiableList(unlocked);
        }
    }
    /** Existing ownership makes repeated purchase calls a no-op; only device-earned coins are debited. */
    public synchronized boolean purchaseAvatarGear(String id) {
        AvatarGearCatalog.Item item=AvatarGearCatalog.find(id);
        if(item==null||item.source!=AvatarGearCatalog.Source.EARNED_COINS)return false;
        synchronized(GOLD_GRANT_LOCK) {
            String key="avatar_owned_v1_"+id,owned=prefs.getString(key,"");
            if(!owned.isEmpty()) {
                if(!prefs.edit().putString(key,owned).putInt("coins",coins()).commit())throw new IllegalStateException("Could not persist recorded avatar purchase");
                return false;
            }
            if(coins()<item.priceCoins)return false;
            if(!prefs.edit().putString(key,item.source.name()).putInt("coins",coins()-item.priceCoins).commit())throw new IllegalStateException("Could not persist avatar purchase");
            return true;
        }
    }
    public synchronized boolean equipAvatarGear(String id) {
        AvatarGearCatalog.Item item=AvatarGearCatalog.find(id);if(item==null||!ownsAvatarGear(id))return false;
        synchronized(GOLD_GRANT_LOCK) {
            if(!prefs.edit().putString("avatar_slot_v1_"+item.slot.name(),id).commit())throw new IllegalStateException("Could not persist avatar equipment");
            return true;
        }
    }
    public synchronized AvatarLoadout avatarLoadout() {
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
            Catalog.BoosterItem item = Catalog.booster(id);
            if (item == null || !boosterUnlocked(id)) return false;
            int price = boosterPrice(id);
            if (coins() < price || storedBoosterStock(id) > MAX_STOCK - item.amount) return false;
            prefs.edit().putInt("coins", coins() - price)
                    .putInt("booster_" + id, storedBoosterStock(id) + item.amount).commit();
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
                if (!prefs.contains("power_loot_v1_" + milestone)) return milestone;
            }
            return 0;
        }
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
            String token = "power_loot_v1_" + milestone;
            String recorded = prefs.getString(token, "");
            if (!recorded.isEmpty()) {
                if (pending == null || !pending.item.equals(recorded) || Catalog.booster(recorded) == null) return "";
                boolean persisted = prefs.edit().putString(token, recorded).putInt("booster_" + recorded, storedBoosterStock(recorded)).commit();
                if (!persisted) throw new IllegalStateException("Could not persist recorded milestone loot");
                LOOT_RETRIES.remove(prefs);
                return recorded;
            }
            int eligible = 0;
            for (Catalog.BoosterItem item : Catalog.BOOSTERS) if (boosterUnlocked(item.id)) eligible++;
            if (eligible == 0) return "";
            int target = (milestone / 5 - 1) % eligible;
            String id = "";
            for (Catalog.BoosterItem item : Catalog.BOOSTERS) if (boosterUnlocked(item.id) && target-- == 0) { id = item.id; break; }
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

    public String dailyId() { return dayId(new Date()); }

    /** ISO-style Monday start, represented by that Monday's local calendar date. */
    public String weeklyId() {
        Calendar calendar = Calendar.getInstance();
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
        int count = 0;
        for (int i = 0; i < Catalog.DAILY_COUNT; i++) if (dailyStars(i) > 0) count++;
        return count;
    }

    public synchronized int weeklyCompletedCount() {
        int count = 0;
        for (int i = 0; i < Catalog.WEEKLY_COUNT; i++) if (weeklyStars(i) > 0) count++;
        return count;
    }

    public synchronized boolean dailyBonusClaimed() { return prefs.getBoolean("daily_bonus_" + dailyId(), false); }
    public synchronized boolean weeklyBonusClaimed() { return prefs.getBoolean("weekly_bonus_" + weeklyId(), false); }

    public synchronized int claimDailyCompletionReward() {
        if (dailyCompletedCount() != Catalog.DAILY_COUNT || dailyBonusClaimed()) return 0;
        prefs.edit().putBoolean("daily_bonus_" + dailyId(), true).putInt("coins", safeAdd(coins(), 100)).commit();
        return 100;
    }

    public synchronized int claimWeeklyCompletionReward() {
        if (weeklyCompletedCount() != Catalog.WEEKLY_COUNT || weeklyBonusClaimed()) return 0;
        prefs.edit().putBoolean("weekly_bonus_" + weeklyId(), true).putInt("coins", safeAdd(coins(), 250)).commit();
        return 250;
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
        String token = "milestone_" + uniqueKey;
        if (prefs.getBoolean(token, false)) return 0;
        boolean persisted = prefs.edit().putBoolean(token, true).putInt("coins", safeAdd(coins(), amount)).commit();
        if (!persisted) throw new IllegalStateException("Could not persist milestone reward");
        return amount;
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
    private static int safeAdd(int balance, int amount) { return (int) Math.min(MAX_COINS, (long) balance + Math.max(0, amount)); }
}
