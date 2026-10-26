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

/**
 * Local save with atomic economy updates. There is no network, ad currency,
 * real-money purchase, or repeat reward for replaying an already earned star.
 * Level numbers are 1-based. Event puzzle indexes are 0-based.
 */
// Synchronous small local transactions intentionally persist economy before action returns.
@SuppressLint("ApplySharedPref")
public final class GameStore {
    private static final String PREFS = "luma_drift_save_v1";
    private static final int MAX_COINS = 2_000_000_000;
    private static final int MAX_STOCK = 999;
    private static final Object GOLD_GRANT_LOCK = new Object();
    private final SharedPreferences prefs;

    public GameStore(Context context) {
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
        if (!isLevelUnlocked(level)) return 0;
        int nextStars = clampStars(earnedStars);
        if (nextStars == 0) return 0;
        int oldStars = stars(level);
        int reward = nextStars > oldStars ? (oldStars == 0 ? 25 : 0) + 10 * (nextStars - oldStars) : 0;
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt("stars_" + level, Math.max(oldStars, nextStars));
        edit.putInt("unlocked", Math.min(Catalog.LEVEL_COUNT, Math.max(unlockedLevel(), level + 1)));
        edit.putInt("coins", safeAdd(coins(), reward));
        edit.commit();
        return reward;
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
        return Catalog.character(id) != null && ("nova".equals(id) || prefs.getBoolean("owned_character_" + id, false));
    }

    public synchronized boolean ownsSkin(String id) {
        return Catalog.skin(id) != null && ("lumina".equals(id) || prefs.getBoolean("owned_skin_" + id, false));
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

    public synchronized int boosterStock(String id) {
        if (Catalog.booster(id) == null) return 0;
        return Math.max(0, Math.min(MAX_STOCK, prefs.getInt("booster_" + id, 0)));
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
        return item == null ? 0 : EconomyCurve.price(item.price, economyProgress());
    }

    public synchronized boolean purchaseBooster(String id) {
        Catalog.BoosterItem item = Catalog.booster(id);
        if (item == null) return false;
        int price = boosterPrice(id);
        if (coins() < price || boosterStock(id) > MAX_STOCK - item.amount) return false;
        prefs.edit().putInt("coins", coins() - price)
                .putInt("booster_" + id, boosterStock(id) + item.amount).commit();
        return true;
    }

    public synchronized boolean useBooster(String id) {
        int count = boosterStock(id);
        if (count <= 0) return false;
        prefs.edit().putInt("booster_" + id, count - 1).commit();
        return true;
    }

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
        if (index < 0 || index >= count) return 0;
        int nextStars = clampStars(earnedStars);
        if (nextStars == 0) return 0;
        int oldStars = eventStars(kind, period, index, count);
        int reward = nextStars > oldStars ? (oldStars == 0 ? base : 0) + starReward * (nextStars - oldStars) : 0;
        prefs.edit().putInt(kind + "_" + period + "_" + index, Math.max(oldStars, nextStars))
                .putInt("coins", safeAdd(coins(), reward)).commit();
        return reward;
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
        return Math.max(1, Math.min(240, prefs.getInt("remix_unlocked", 1)));
    }
    public synchronized boolean remixAvailable() { return unlockedLevel() >= 13; }
    public synchronized int remixStars(int level) {
        return level >= 1 && level <= 240 ? clampStars(prefs.getInt("remix_stars_" + level, 0)) : 0;
    }
    public synchronized int completeRemix(int level, int stars) {
        if (!remixAvailable() || level < 1 || level > remixUnlockedLevel()) return 0;
        int next = clampStars(stars), old = remixStars(level);
        if (next == 0) return 0;
        int reward = next > old ? (old == 0 ? 35 : 0) + 12 * (next - old) : 0;
        boolean persisted = prefs.edit().putInt("remix_stars_" + level, Math.max(old, next))
                .putInt("remix_unlocked", Math.min(240, Math.max(remixUnlockedLevel(), level + 1)))
                .putInt("coins", safeAdd(coins(), reward)).commit();
        if (!persisted) throw new IllegalStateException("Could not persist Remix reward");
        return reward;
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
