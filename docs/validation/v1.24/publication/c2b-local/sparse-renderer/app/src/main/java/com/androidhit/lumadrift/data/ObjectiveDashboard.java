package com.androidhit.lumadrift.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Presentation only: reads existing progression/receipts and never grants, equips or purchases. */
public final class ObjectiveDashboard {
    public enum QuestKind { BOARDS, STARS, EVENTS }
    public enum ActionKind {
        CLAIM_LOGIN, CLAIM_JOURNEY, CLAIM_TRIAL, CLAIM_DAILY_QUEST, CLAIM_WEEKLY_QUEST,
        CLAIM_DAILY_EVENT, CLAIM_WEEKLY_EVENT, PLAY_JOURNEY, PLAY_DAILY_EVENT, PLAY_WEEKLY_EVENT, NONE
    }
    public static final class Quest {
        public final int index, progress, target, coins;
        public final String period;
        public final QuestKind kind;
        public final boolean weekly, claimed, ready;
        public final CollectionGoal.Route route;
        private Quest(int index, boolean weekly, String period, int progress, int target, int coins,
                      boolean claimed, boolean complete) {
            this.index=index; this.weekly=weekly; this.period=period; this.kind=QuestKind.values()[index];
            this.target=target; this.progress=Math.min(target,Math.max(0,progress)); this.coins=coins;
            this.claimed=claimed; this.ready=complete&&!claimed;
            this.route=ready?CollectionGoal.Route.REWARDS:kind==QuestKind.EVENTS?
                    (weekly?CollectionGoal.Route.WEEKLY:CollectionGoal.Route.DAILY):CollectionGoal.Route.JOURNEY;
        }
    }
    public static final class Trial {
        public final String id, nameKey;
        public final int unlockLevel, stock;
        public final boolean unlocked, claimed, ready;
        private Trial(GameStore store, Catalog.BoosterItem item) {
            id=item.id; nameKey=item.nameKey; unlockLevel=item.unlockLevel; stock=store.boosterStock(id);
            unlocked=store.boosterUnlocked(id); claimed=store.powerTrialClaimed(id); ready=store.powerTrialAvailable(id);
        }
    }
    public static final class Action {
        public final ActionKind kind;
        public final CollectionGoal.Route route;
        /** Power ID, gift power ID, or period ID for the corresponding claim. */
        public final String id;
        /** Quest/event indexes are zero-based; Journey levels/milestones are one-based; -1 otherwise. */
        public final int index;
        private Action(ActionKind kind, CollectionGoal.Route route, String id, int index) {
            this.kind=kind; this.route=route; this.id=id; this.index=index;
        }
    }
    public static final class Snapshot {
        public final String dayId, weekId, journeyLootId;
        public final int readyCount, dailyReadyCount, weeklyReadyCount, dailyQuestReadyCount,
                weeklyQuestReadyCount, trialsReadyCount, eventReadyCount, nextMilestone, loginDay, loginCoins;
        public final boolean loginDailyReady, dailyLoginReady, journeyReady, dailyEventReady, weeklyEventReady;
        public final List<Quest> dailyQuests, weeklyQuests;
        public final List<Trial> trials;
        public final List<String> readyTrialIds;
        public final Action nextAction;
        private Snapshot(GameStore store, MetaProgress meta) {
            dayId=store.dailyId(); weekId=store.weeklyId();
            List<Quest> daily=new ArrayList<>(), weekly=new ArrayList<>(); int dailyCount=0,weeklyCount=0;
            for(int index=0;index<MetaProgress.MISSION_COUNT;index++) {
                Quest quest=new Quest(index,false,meta.dayId(),meta.missionProgress(index),meta.missionTarget(index),
                        meta.missionCoins(index),meta.missionClaimed(index),meta.missionComplete(index));
                daily.add(quest); if(quest.ready)dailyCount++;
            }
            for(int index=0;index<MetaProgress.WEEKLY_MISSION_COUNT;index++) {
                Quest quest=new Quest(index,true,meta.weekId(),meta.weeklyMissionProgress(index),meta.weeklyMissionTarget(index),
                        meta.weeklyMissionCoins(index),meta.weeklyMissionClaimed(index),meta.weeklyMissionComplete(index));
                weekly.add(quest); if(quest.ready)weeklyCount++;
            }
            dailyQuests=Collections.unmodifiableList(daily); weeklyQuests=Collections.unmodifiableList(weekly);
            dailyQuestReadyCount=dailyCount; weeklyQuestReadyCount=weeklyCount;
            loginDay=store.dailyLoginDay(); loginCoins=store.dailyLoginCoins(loginDay);
            loginDailyReady=!store.dailyLoginClaimed(); dailyLoginReady=loginDailyReady;
            nextMilestone=store.nextLootMilestone(); journeyLootId=store.nextMilestoneLootId();
            journeyReady=nextMilestone>0&&store.completedLevels()>=nextMilestone;
            dailyEventReady=store.dailyCompletedCount()==Catalog.DAILY_COUNT&&!store.dailyBonusClaimed();
            weeklyEventReady=store.weeklyCompletedCount()==Catalog.WEEKLY_COUNT&&!store.weeklyBonusClaimed();
            eventReadyCount=(dailyEventReady?1:0)+(weeklyEventReady?1:0);
            List<Trial> trialList=new ArrayList<>(); List<String> readyIds=new ArrayList<>();
            for(Catalog.BoosterItem item:Catalog.BOOSTERS)if(Catalog.isPowerOffered(item.id)) {
                Trial trial=new Trial(store,item); trialList.add(trial); if(trial.ready)readyIds.add(trial.id);
            }
            trials=Collections.unmodifiableList(trialList); readyTrialIds=Collections.unmodifiableList(readyIds);
            trialsReadyCount=readyIds.size();
            dailyReadyCount=dailyQuestReadyCount+(loginDailyReady?1:0)+(dailyEventReady?1:0);
            weeklyReadyCount=weeklyQuestReadyCount+(weeklyEventReady?1:0);
            readyCount=dailyReadyCount+weeklyReadyCount+(journeyReady?1:0)+trialsReadyCount;
            nextAction=next(store,this);
        }
    }
    private ObjectiveDashboard() { }

    /** Ready means earned/unclaimed; each real claim still enforces capacity and durable persistence. */
    public static Snapshot snapshot(GameStore store, MetaProgress meta) {
        return new Snapshot(Objects.requireNonNull(store,"store"),Objects.requireNonNull(meta,"meta"));
    }
    private static Action next(GameStore store, Snapshot state) {
        if(state.loginDailyReady)return new Action(ActionKind.CLAIM_LOGIN,CollectionGoal.Route.REWARDS,state.dayId,-1);
        for(Quest quest:state.dailyQuests)if(quest.ready)return new Action(ActionKind.CLAIM_DAILY_QUEST,CollectionGoal.Route.REWARDS,quest.period,quest.index);
        for(Quest quest:state.weeklyQuests)if(quest.ready)return new Action(ActionKind.CLAIM_WEEKLY_QUEST,CollectionGoal.Route.REWARDS,quest.period,quest.index);
        if(state.dailyEventReady)return new Action(ActionKind.CLAIM_DAILY_EVENT,CollectionGoal.Route.DAILY,state.dayId,-1);
        if(state.weeklyEventReady)return new Action(ActionKind.CLAIM_WEEKLY_EVENT,CollectionGoal.Route.WEEKLY,state.weekId,-1);
        if(state.journeyReady)return new Action(ActionKind.CLAIM_JOURNEY,CollectionGoal.Route.REWARDS,state.journeyLootId,state.nextMilestone);
        if(!state.readyTrialIds.isEmpty())return new Action(ActionKind.CLAIM_TRIAL,CollectionGoal.Route.REWARDS,state.readyTrialIds.get(0),-1);
        for(int level=1;level<=store.unlockedLevel();level++)if(store.stars(level)==0)
            return new Action(ActionKind.PLAY_JOURNEY,CollectionGoal.Route.JOURNEY,"",level);
        for(int index=0;index<Catalog.DAILY_COUNT;index++)if(store.dailyStars(index)==0&&store.isDailyUnlocked(index))
            return new Action(ActionKind.PLAY_DAILY_EVENT,CollectionGoal.Route.DAILY,state.dayId,index);
        for(int index=0;index<Catalog.WEEKLY_COUNT;index++)if(store.weeklyStars(index)==0&&store.isWeeklyUnlocked(index))
            return new Action(ActionKind.PLAY_WEEKLY_EVENT,CollectionGoal.Route.WEEKLY,state.weekId,index);
        return new Action(ActionKind.NONE,CollectionGoal.Route.REWARDS,"",-1);
    }
}
