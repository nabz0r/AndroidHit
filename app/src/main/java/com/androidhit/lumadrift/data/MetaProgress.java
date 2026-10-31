package com.androidhit.lumadrift.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

/** Device-local progression. Replaying a board never farms XP or mission credit. */
@SuppressLint("ApplySharedPref")
public final class MetaProgress {
    public interface Clock { long now(); }
    public static final int MISSION_COUNT = 3;
    public static final int WEEKLY_MISSION_COUNT = 3;
    private static final int[] WEEKLY_TARGETS = {10, 24, 3};
    private static final int[] WEEKLY_REWARDS = {150, 180, 200};
    public static final int XP_PER_RANK = RankCopyRules124.XP_PER_RANK;
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
        synchronized (LocalSaveTransactions.LOCK) {
            LocalSaveTransactions.flush(prefs);
            String key = "visit_" + dayId();
            if (!prefs.getBoolean(key, false)) {
                Map<String,Object> write=new HashMap<>();write.put(key,true);write.put("days",safeAdd(daysPlayed(),1));
                LocalSaveTransactions.write(prefs,write);
            }
        }
    }

    public synchronized int xp() { LocalSaveTransactions.flush(prefs); return Math.max(0, prefs.getInt("xp", 0)); }
    public synchronized int rank() { return 1 + xp() / XP_PER_RANK; }
    public synchronized int rankProgress() { return xp() % XP_PER_RANK; }
    public synchronized int daysPlayed() { LocalSaveTransactions.flush(prefs); return Math.max(0, prefs.getInt("days", 0)); }
    public synchronized int completedBoards() { LocalSaveTransactions.flush(prefs); return Math.max(0, prefs.getInt("boards", 0)); }
    public synchronized boolean badgeUnlocked() { return completedBoards() >= BADGE_BOARDS; }
    public synchronized boolean badgeEquipped() { return badgeUnlocked() && prefs.getBoolean("badge_equipped", true); }
    public synchronized void setBadgeEquipped(boolean equipped) {
        if (badgeUnlocked()) prefs.edit().putBoolean("badge_equipped", equipped).commit();
    }

    public String dayId() { return formatDay(clock.now()); }

    public synchronized int missionProgress(int mission) {
        LocalSaveTransactions.flush(prefs);
        if (!validMission(mission)) return 0;
        return Math.min(TARGETS[mission], Math.max(0, prefs.getInt("mission_" + dayId() + "_" + mission, 0)));
    }
    public int missionTarget(int mission) { return validMission(mission) ? TARGETS[mission] : 0; }
    public int missionCoins(int mission) { return validMission(mission) ? REWARDS[mission] : 0; }
    public synchronized boolean missionComplete(int mission) {
        return validMission(mission) && missionProgress(mission) >= missionTarget(mission);
    }
    public synchronized boolean missionClaimed(int mission) {
        LocalSaveTransactions.flush(prefs);
        return validMission(mission) && prefs.getBoolean("claim_" + dayId() + "_" + mission, false);
    }

    /** Currency and its idempotency token share GameStore's single atomic transaction. */
    public synchronized int claimMission(int mission, GameStore store) {
        LocalSaveTransactions.flush(prefs);
        if (store == null || !missionComplete(mission) || missionClaimed(mission)) return 0;
        String day = dayId();
        // Do not hold the cross-file save lock while entering another store handle.
        int reward = store.grantMilestone("mission:" + day + ":" + mission, missionCoins(mission));
        synchronized (LocalSaveTransactions.LOCK) {
            LocalSaveTransactions.flush(prefs);
            Map<String,Object> write=new HashMap<>();write.put("claim_"+day+"_"+mission,true);
            LocalSaveTransactions.write(prefs,write);
        }
        return reward;
    }

    public synchronized int weeklyMissionProgress(int mission) {
        LocalSaveTransactions.flush(prefs);
        return validWeeklyMission(mission)?Math.min(WEEKLY_TARGETS[mission],Math.max(0,prefs.getInt("weekly_mission_"+weekId()+"_"+mission,0))):0;
    }
    public int weeklyMissionTarget(int mission) { return validWeeklyMission(mission)?WEEKLY_TARGETS[mission]:0; }
    public int weeklyMissionCoins(int mission) { return validWeeklyMission(mission)?WEEKLY_REWARDS[mission]:0; }
    public synchronized boolean weeklyMissionComplete(int mission) {
        return validWeeklyMission(mission)&&weeklyMissionProgress(mission)>=weeklyMissionTarget(mission);
    }
    public synchronized boolean weeklyMissionClaimed(int mission) {
        LocalSaveTransactions.flush(prefs);
        return validWeeklyMission(mission)&&prefs.getBoolean("weekly_claim_"+weekId()+"_"+mission,false);
    }
    public synchronized int claimWeeklyMission(int mission,GameStore store) {
        LocalSaveTransactions.flush(prefs);
        if(store==null||!weeklyMissionComplete(mission)||weeklyMissionClaimed(mission))return 0;
        String week=weekId();int reward=store.grantMilestone("weeklymission:"+week+":"+mission,weeklyMissionCoins(mission));
        synchronized(LocalSaveTransactions.LOCK) {
            LocalSaveTransactions.flush(prefs);
            Map<String,Object> write=new HashMap<>();write.put("weekly_claim_"+week+"_"+mission,true);
            LocalSaveTransactions.write(prefs,write);
        }
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
        synchronized(LocalSaveTransactions.LOCK) {
            LocalSaveTransactions.flush(prefs);
            if (stars < 1 || stars > 3 || !validBoard(mode, index, period)) return 0;
            String identity = boardIdentity(mode,index,period),seen="seen_"+identity;
            if (prefs.getBoolean(seen, false)) return 0;
            int rewardXp=winXp(stars);
            LocalSaveTransactions.write(prefs,firstWinWrites(mode,stars,seen,rewardXp));
            return rewardXp;
        }
    }

    /** A run-bound XP snapshot is committed with the first-win marker, never reconstructed from a mutable avatar. */
    public synchronized RankVictoryXp124 recordWin124(int mode,int index,int stars,String period,String runId,String captainId) {
        String kind=mode==0?"campaign":mode==1?"daily":mode==2?"weekly":mode==3?"remix":"";
        if(runId==null||!runId.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")||captainId==null||!captainId.matches("[a-z0-9_]{0,60}"))throw new IllegalArgumentException("Run and explicit captain snapshot required");
        synchronized(LocalSaveTransactions.LOCK){
            LocalSaveTransactions.flush(prefs);
            if(stars<1||stars>3)return null;
            String identity=boardIdentity(kind,index,period),key="rank_xp_v124_"+runId,record=prefs.getString(key,"");
            if(!record.isEmpty()){
                RankVictoryXp124 old=RankVictoryXp124.decode(record);
                if(old==null)throw new GameStore.LocalSaveException("Invalid run XP receipt");
                if(!old.boardKey.equals(identity)||old.stars!=stars)throw new IllegalArgumentException("Run XP receipt context changed");
                return old;
            }
            // An award already earned in its active period must remain recoverable
            // after midnight/week rollover; a new expired event still earns nothing.
            if(!validBoard(kind,index,period))return null;
            String seen="seen_"+identity;boolean first=!prefs.getBoolean(seen,false);int before=xp();
            int after=first?safeAdd(before,winXp(stars)):before;
            RankVictoryXp124 award=new RankVictoryXp124(runId,identity,captainId,stars,before,after,first);
            Map<String,Object> write=first?firstWinWrites(kind,stars,seen,winXp(stars)):new HashMap<>();
            write.put(key,award.encode());LocalSaveTransactions.write(prefs,write);return award;
        }
    }
    private static String boardIdentity(String mode,int index,String period){return "campaign".equals(mode)?mode+"_"+index:mode+"_"+period+"_"+index;}
    private Map<String,Object> firstWinWrites(String mode,int stars,String seen,int rewardXp){
        recordVisit();String day=dayId(),week=weekId();
        Map<String,Object> write=new HashMap<>();write.put(seen,true);
        write.put("xp",safeAdd(xp(),rewardXp));write.put("boards",safeAdd(completedBoards(),1));
        write.put("mission_"+day+"_0",Math.min(TARGETS[0],missionProgress(0)+1));
        write.put("mission_"+day+"_1",Math.min(TARGETS[1],missionProgress(1)+stars));
        write.put("weekly_mission_"+week+"_0",Math.min(WEEKLY_TARGETS[0],weeklyMissionProgress(0)+1));
        write.put("weekly_mission_"+week+"_1",Math.min(WEEKLY_TARGETS[1],weeklyMissionProgress(1)+stars));
        if("daily".equals(mode)||"weekly".equals(mode)){
            write.put("mission_"+day+"_2",1);
            write.put("weekly_mission_"+week+"_2",Math.min(WEEKLY_TARGETS[2],weeklyMissionProgress(2)+1));
        }
        return write;
    }

    private boolean validBoard(String mode, int index, String period) {
        if ("campaign".equals(mode)) return index >= 1 && index <= Catalog.LEVEL_COUNT;
        if ("daily".equals(mode)) return index >= 0 && index < Catalog.DAILY_COUNT && dayId().equals(period);
        if ("weekly".equals(mode)) return index >= 0 && index < Catalog.WEEKLY_COUNT && weekId().equals(period);
        return "remix".equals(mode) && index >= 1 && index <= Catalog.REMIX_COUNT && "remix-v1".equals(period);
    }

    public String weekId() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(clock.now());
        calendar.add(Calendar.DAY_OF_MONTH, -(calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7);
        return formatDay(calendar.getTimeInMillis());
    }

    private static String formatDay(long time) { return new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(new Date(time)); }
    private static boolean validMission(int mission) { return mission >= 0 && mission < MISSION_COUNT; }
    private static boolean validWeeklyMission(int mission) { return mission>=0&&mission<WEEKLY_MISSION_COUNT; }
    private static int winXp(int stars) { return RankCopyRules124.xpForStars(stars); }
    private static int safeAdd(int value, int increment) { return (int) Math.min(2_000_000_000L, (long) value + increment); }
}
