package com.androidhit.lumadrift.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Device-local progression. Replaying a board never farms XP or mission credit. */
@SuppressLint("ApplySharedPref")
public final class MetaProgress {
    public interface Clock { long now(); }
    public static final int MISSION_COUNT = 3;
    public static final int XP_PER_RANK = 120;
    public static final int BADGE_BOARDS = 7;
    private static final int[] TARGETS = {3, 6, 1};
    private static final int[] REWARDS = {35, 40, 45};
    private final SharedPreferences prefs;
    private final Clock clock;

    public MetaProgress(Context context) { this(context, System::currentTimeMillis); }

    /** Injectable clock keeps date-boundary behavior testable without changing device time. */
    public MetaProgress(Context context, Clock clock) {
        this.clock = clock;
        prefs = context.getApplicationContext().getSharedPreferences("luma_drift_meta_v1", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("initialized", false)) migrate(new GameStore(context));
        recordVisit();
    }

    private void migrate(GameStore store) {
        SharedPreferences.Editor edit = prefs.edit().putBoolean("initialized", true);
        int boards = 0, xp = 0;
        for (int i = 1; i <= Catalog.LEVEL_COUNT; i++) {
            int stars = store.stars(i);
            if (stars > 0) {
                edit.putBoolean("seen_campaign_" + i, true);
                boards++; xp += winXp(stars);
            }
        }
        for (int i = 0; i < Catalog.DAILY_COUNT; i++) {
            int stars = store.dailyStars(i);
            if (stars > 0) {
                edit.putBoolean("seen_daily_" + store.dailyId() + "_" + i, true);
                boards++; xp += winXp(stars);
            }
        }
        for (int i = 0; i < Catalog.WEEKLY_COUNT; i++) {
            int stars = store.weeklyStars(i);
            if (stars > 0) {
                edit.putBoolean("seen_weekly_" + store.weeklyId() + "_" + i, true);
                boards++; xp += winXp(stars);
            }
        }
        edit.putInt("xp", xp).putInt("boards", boards).commit();
    }

    /** Counts distinct days played; taking a break never removes progress or a reward. */
    public synchronized void recordVisit() {
        String key = "visit_" + dayId();
        if (!prefs.getBoolean(key, false)) prefs.edit().putBoolean(key, true)
                .putInt("days", safeAdd(daysPlayed(), 1)).commit();
    }

    public synchronized int xp() { return Math.max(0, prefs.getInt("xp", 0)); }
    public synchronized int rank() { return 1 + xp() / XP_PER_RANK; }
    public synchronized int rankProgress() { return xp() % XP_PER_RANK; }
    public synchronized int daysPlayed() { return Math.max(0, prefs.getInt("days", 0)); }
    public synchronized int completedBoards() { return Math.max(0, prefs.getInt("boards", 0)); }
    public synchronized boolean badgeUnlocked() { return completedBoards() >= BADGE_BOARDS; }
    public synchronized boolean badgeEquipped() { return badgeUnlocked() && prefs.getBoolean("badge_equipped", true); }
    public synchronized void setBadgeEquipped(boolean equipped) {
        if (badgeUnlocked()) prefs.edit().putBoolean("badge_equipped", equipped).commit();
    }

    public String dayId() { return formatDay(clock.now()); }

    public synchronized int missionProgress(int mission) {
        if (!validMission(mission)) return 0;
        return Math.min(TARGETS[mission], Math.max(0, prefs.getInt("mission_" + dayId() + "_" + mission, 0)));
    }
    public int missionTarget(int mission) { return validMission(mission) ? TARGETS[mission] : 0; }
    public int missionCoins(int mission) { return validMission(mission) ? REWARDS[mission] : 0; }
    public synchronized boolean missionComplete(int mission) {
        return validMission(mission) && missionProgress(mission) >= missionTarget(mission);
    }
    public synchronized boolean missionClaimed(int mission) {
        return validMission(mission) && prefs.getBoolean("claim_" + dayId() + "_" + mission, false);
    }

    /** Currency and its idempotency token share GameStore's single atomic transaction. */
    public synchronized int claimMission(int mission, GameStore store) {
        if (store == null || !missionComplete(mission) || missionClaimed(mission)) return 0;
        String day = dayId();
        int reward = store.grantMilestone("mission:" + day + ":" + mission, missionCoins(mission));
        // Mark even when the wallet already has the token (e.g. process death between saves).
        prefs.edit().putBoolean("claim_" + day + "_" + mission, true).commit();
        return reward;
    }

    public synchronized int recordWin(int mode, int index, int stars, String period) {
        String kind = mode == 0 ? "campaign" : mode == 1 ? "daily" : mode == 2 ? "weekly" : mode == 3 ? "remix" : "";
        return recordWin(kind, index, stars, period);
    }

    /**
     * Call only after the board is solved. Campaign indexes are 1-based; events are 0-based.
     * Remix period is an immutable board namespace (the challenge seed is suitable).
     * XP and all mission credit are awarded only on that board's first completion.
     */
    public synchronized int recordWin(String mode, int index, int stars, String period) {
        if (stars < 1 || stars > 3 || !validBoard(mode, index, period)) return 0;
        String identity = "campaign".equals(mode) ? mode + "_" + index : mode + "_" + period + "_" + index;
        String seen = "seen_" + identity;
        if (prefs.getBoolean(seen, false)) return 0;
        recordVisit();
        String day = dayId();
        int rewardXp = winXp(stars);
        SharedPreferences.Editor edit = prefs.edit().putBoolean(seen, true)
                .putInt("xp", safeAdd(xp(), rewardXp))
                .putInt("boards", safeAdd(completedBoards(), 1));
        edit.putInt("mission_" + day + "_0", Math.min(TARGETS[0], missionProgress(0) + 1));
        edit.putInt("mission_" + day + "_1", Math.min(TARGETS[1], missionProgress(1) + stars));
        if ("daily".equals(mode) || "weekly".equals(mode)) edit.putInt("mission_" + day + "_2", 1);
        return edit.commit() ? rewardXp : 0;
    }

    private boolean validBoard(String mode, int index, String period) {
        if ("campaign".equals(mode)) return index >= 1 && index <= Catalog.LEVEL_COUNT;
        if ("daily".equals(mode)) return index >= 0 && index < Catalog.DAILY_COUNT && dayId().equals(period);
        if ("weekly".equals(mode)) return index >= 0 && index < Catalog.WEEKLY_COUNT && weekId().equals(period);
        return "remix".equals(mode) && index >= 1 && index <= Catalog.REMIX_COUNT && "remix-v1".equals(period);
    }

    private String weekId() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(clock.now());
        calendar.add(Calendar.DAY_OF_MONTH, -(calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7);
        return formatDay(calendar.getTimeInMillis());
    }

    private static String formatDay(long time) { return new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(new Date(time)); }
    private static boolean validMission(int mission) { return mission >= 0 && mission < MISSION_COUNT; }
    private static int winXp(int stars) { return 20 + 5 * stars; }
    private static int safeAdd(int value, int increment) { return (int) Math.min(2_000_000_000L, (long) value + increment); }
}
