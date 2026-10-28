package com.androidhit.lumadrift.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Read-only narrative view of existing Journey clears and claimed Guardian receipts. */
public final class PrismStory {
    public static final int CHAPTER_COUNT = 10;
    public static final int JOURNEY_PER_CHAPTER = 100;
    public static final int GUARDIANS_PER_CHAPTER = 10;
    private static final List<Chapter> CHAPTERS;
    static {
        ArrayList<Chapter> chapters = new ArrayList<>();
        for (int number = 1; number <= CHAPTER_COUNT; number++) chapters.add(new Chapter(number));
        CHAPTERS = Collections.unmodifiableList(chapters);
    }

    /** Definitions contain keys, never translated strings, rewards or mutable state. */
    public static final class Chapter {
        public final int number, firstJourney, lastJourney, unlockJourneyClears;
        public final int firstGuardian, lastGuardian;
        public final String biomeKey, titleKey, storyIntroKey, storyAfterKey, objectiveKey;
        private Chapter(int number) {
            this.number = number;
            unlockJourneyClears = (number - 1) * JOURNEY_PER_CHAPTER;
            firstJourney = unlockJourneyClears + 1;
            lastJourney = number * JOURNEY_PER_CHAPTER;
            firstGuardian = (number - 1) * GUARDIANS_PER_CHAPTER + 1;
            lastGuardian = number * GUARDIANS_PER_CHAPTER;
            biomeKey = "biome_" + (number - 1);
            titleKey = "prism_title_" + number;
            storyIntroKey = "prism_intro_" + number;
            storyAfterKey = "prism_after_" + number;
            objectiveKey = "prism_objective_" + number;
        }
    }

    /** Progress counts existing facts. Reading this class never claims a fragment. */
    public static final class ChapterProgress {
        public final Chapter chapter;
        public final boolean unlocked, completed;
        public final int journeyProgress, journeyTarget, fragmentsClaimed, fragmentTarget;
        public final String titleKey, storyIntroKey, storyAfterKey, objectiveKey;
        private final int nextGuardian;
        private ChapterProgress(Chapter chapter, int clears, Set<Integer> receipts) {
            this.chapter = chapter;
            unlocked = clears >= chapter.unlockJourneyClears;
            journeyTarget = JOURNEY_PER_CHAPTER;
            fragmentTarget = GUARDIANS_PER_CHAPTER;
            journeyProgress = Math.max(0, Math.min(journeyTarget, clears - chapter.unlockJourneyClears));
            int claimed = 0, next = 0;
            for (int n = chapter.firstGuardian; n <= chapter.lastGuardian; n++) {
                if (receipts.contains(n)) claimed++;
                else if (next == 0) next = n;
            }
            fragmentsClaimed = claimed;
            completed = claimed == fragmentTarget;
            nextGuardian = next;
            titleKey = chapter.titleKey;
            storyIntroKey = chapter.storyIntroKey;
            storyAfterKey = chapter.storyAfterKey;
            objectiveKey = chapter.objectiveKey;
        }
        /** First missing receipt in this chapter, including a locked encounter; zero if complete. */
        public int nextGuardian() { return nextGuardian; }
    }

    /** Immutable defensive snapshot. Clear count and receipt ownership are independent supplied facts. */
    public static final class Snapshot {
        public final int journeyClears, fragmentsClaimed;
        public final List<ChapterProgress> chapters;
        private final Set<Integer> receipts;
        private Snapshot(int journeyClears, Set<Integer> suppliedReceipts) {
            this.journeyClears = Math.max(0, Math.min(1000, journeyClears));
            TreeSet<Integer> accepted = new TreeSet<>();
            if (suppliedReceipts != null) for (Integer n : suppliedReceipts)
                if (n != null && n >= 1 && n <= 100) accepted.add(n);
            receipts = Collections.unmodifiableSet(accepted);
            fragmentsClaimed = accepted.size();
            ArrayList<ChapterProgress> progress = new ArrayList<>();
            for (Chapter chapter : CHAPTERS) progress.add(new ChapterProgress(chapter, this.journeyClears, receipts));
            chapters = Collections.unmodifiableList(progress);
        }
        /** Chapter at the current clear boundary; chapter 10 remains selected at 1000. */
        public ChapterProgress currentChapter() { return forChapter(Math.min(CHAPTER_COUNT, journeyClears / JOURNEY_PER_CHAPTER + 1)); }
        /** Invalid chapter identifiers are rejected rather than silently targeting another chapter. */
        public ChapterProgress forChapter(int number) { requireChapter(number); return chapters.get(number - 1); }
        /** Earliest missing Guardian receipt globally; zero only when all 100 are present. */
        public int nextGuardian() {
            for (int number = 1; number <= 100; number++) if (!receipts.contains(number)) return number;
            return 0;
        }
        public boolean guardianClaimed(int number) { return number >= 1 && number <= 100 && receipts.contains(number); }
        public boolean guardianUnlocked(int number) { return number >= 1 && number <= 100 && journeyClears >= number * 10; }
        public Set<Integer> guardianReceipts() { return receipts; }
    }

    private PrismStory() { }
    public static List<Chapter> chapters() { return CHAPTERS; }
    public static Snapshot snapshot(int journeyClears, Set<Integer> receipts) { return new Snapshot(journeyClears, receipts); }
    public static ChapterProgress forChapter(int number, int journeyClears, Set<Integer> receipts) {
        requireChapter(number);
        return snapshot(journeyClears, receipts).forChapter(number);
    }
    private static void requireChapter(int number) {
        if (number < 1 || number > CHAPTER_COUNT) throw new IllegalArgumentException("Prism chapter 1..10 required");
    }
}
