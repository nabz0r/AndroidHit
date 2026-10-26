import android.content.Context;
import android.content.SharedPreferences;
import com.androidhit.lumadrift.data.GameStore;
import com.androidhit.lumadrift.data.MetaProgress;
import com.androidhit.lumadrift.data.PlayJournal;
import com.androidhit.lumadrift.data.Strings;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/** Regression checks for first-clear progression, atomic claims and opt-in local observations. */
public final class MetaTest {
    private static int checks;
    private static final class MemoryPreferences implements SharedPreferences {
        private final Map<String, Object> values = new HashMap<>();
        private boolean failNextCommit;
        @Override public boolean contains(String key) { return values.containsKey(key); }
        @Override public int getInt(String key, int fallback) { return (Integer) values.getOrDefault(key, fallback); }
        @Override public String getString(String key, String fallback) { return (String) values.getOrDefault(key, fallback); }
        @Override public boolean getBoolean(String key, boolean fallback) { return (Boolean) values.getOrDefault(key, fallback); }
        @Override public Editor edit() { return new Editor() {
            private final Map<String, Object> pending = new HashMap<>();
            @Override public Editor putInt(String key, int value) { pending.put(key, value); return this; }
            @Override public Editor putString(String key, String value) { pending.put(key, value); return this; }
            @Override public Editor putBoolean(String key, boolean value) { pending.put(key, value); return this; }
            @Override public boolean commit() {
                if (failNextCommit) { failNextCommit = false; return false; }
                values.putAll(pending); return true;
            }
        }; }
    }
    private static final class TestContext extends Context {
        private final Map<String, MemoryPreferences> files = new HashMap<>();
        @Override public SharedPreferences getSharedPreferences(String name, int mode) {
            return files.computeIfAbsent(name, ignored -> new MemoryPreferences());
        }
    }
    private static final class Clock implements MetaProgress.Clock, PlayJournal.Clock {
        private long time;
        Clock(String day) { set(day); }
        void set(String day) {
            try { time = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(day).getTime() + 43_200_000L; }
            catch (Exception invalid) { throw new AssertionError(invalid); }
        }
        @Override public long now() { return time; }
    }
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static void testProgression() {
        TestContext context = new TestContext();
        Clock clock = new Clock("2026-10-26");
        MetaProgress meta = new MetaProgress(context, clock);
        GameStore store = new GameStore(context);
        check(meta.xp() == 0 && meta.rank() == 1 && meta.rankProgress() == 0, "Fresh rank starts without invented XP");
        check(meta.daysPlayed() == 1, "First visit counts once");
        meta.recordVisit();
        check(new MetaProgress(context, clock).daysPlayed() == 1, "Repeated visits on one date are deduplicated");
        check(meta.claimMission(0, store) == 0, "Incomplete mission cannot pay");
        check(meta.recordWin(0, 1, 3, null) == 35, "First clear grants exact XP");
        check(meta.recordWin(0, 1, 3, null) == 0, "Replay grants no duplicate XP");
        check(meta.recordWin(0, 1, 1, null) == 0, "Worse replay grants no duplicate XP");
        check(meta.missionProgress(0) == 1 && meta.missionProgress(1) == 3, "Only first clear counts for missions");
        check(meta.recordWin(0, 2, 1, null) == 25, "One-star first clear XP");
        check(meta.recordWin(0, 2, 3, null) == 0, "Improved stars cannot farm mission XP");
        check(meta.missionProgress(1) == 4, "Star improvement does not count as a new-board mission clear");
        check(meta.recordWin(1, 0, 3, "2026-10-25") == 0, "Expired daily board cannot count for missions");
        check(meta.recordWin(1, 0, 3, "2026-10-26") == 35, "Current daily board grants first-clear XP");
        check(meta.missionComplete(0) && meta.missionComplete(1) && meta.missionComplete(2), "All three missions can complete in three first clears");
        int balance = store.coins();
        for (int i = 0; i < 3; i++) {
            check(meta.claimMission(i, store) == meta.missionCoins(i), "Mission payout " + i);
            check(meta.missionClaimed(i), "Mission claim persisted " + i);
            check(meta.claimMission(i, store) == 0, "Repeated mission claim cannot pay twice " + i);
        }
        check(store.coins() == balance + 120, "Wallet credited exact total mission reward");
        check(new MetaProgress(context, clock).claimMission(0, new GameStore(context)) == 0, "Claims survive all store recreation");
        check(meta.rank() == 1 && meta.rankProgress() == 95, "XP progress is computed exactly");
        check(meta.recordWin(2, 0, 3, "2026-10-19") == 0, "Old weekly period rejected");
        check(meta.recordWin(2, 0, 3, "2026-10-26") == 35, "Monday weekly period accepted");
        check(meta.rank() == 2 && meta.rankProgress() == 10, "Rank crosses threshold without dropping remainder");
        for (int i = 3; i <= 5; i++) meta.recordWin(0, i, 3, null);
        check(meta.completedBoards() == 7 && meta.badgeUnlocked(), "Seven unique boards unlock free cosmetic badge");
        check(meta.badgeEquipped(), "Earned badge starts equipped");
        meta.setBadgeEquipped(false);
        check(!new MetaProgress(context, clock).badgeEquipped(), "Badge equipment persists separately");
        check(meta.recordWin(3, 1, 3, "remix-v1") == 35, "Remix boards have distinct identity from Journey");
        check(meta.recordWin(3, 1, 3, "remix-v1") == 0, "Remix replay cannot farm XP");
        check(meta.recordWin(4, 200, 3, "challenge") == 0, "Friend challenges cannot farm XP or quest credit");
        check(meta.recordWin(0, 0, 3, null) == 0 && meta.recordWin(0, 241, 3, null) == 0, "Invalid campaign bounds rejected");
        check(meta.recordWin(1, -1, 3, "2026-10-26") == 0 && meta.recordWin(1, 3, 3, "2026-10-26") == 0, "Invalid daily bounds rejected");
        check(meta.recordWin(3, 1, 3, "invalid/identity") == 0, "Malformed remix identity rejected");
        check(meta.recordWin(3, 0, 3, "remix-v1") == 0 && meta.recordWin(3, 241, 3, "remix-v1") == 0, "Remix board bounds match the fixed 240-board catalog");
        check(meta.recordWin(3, 2, 3, "remix-v2") == 0, "Unknown Remix namespace cannot farm alternate identities");
        check(meta.recordWin(0, 6, 0, null) == 0 && meta.recordWin(0, 6, 4, null) == 0, "Invalid star counts cannot grant progress");
        check(meta.missionTarget(-1) == 0 && meta.missionProgress(3) == 0 && !meta.missionComplete(3), "Invalid mission indexes rejected");

        int xp = meta.xp();
        clock.set("2026-11-03");
        meta.recordVisit();
        check(meta.daysPlayed() == 2 && meta.xp() == xp, "Taking eight days away retains all XP and days played");
        check(meta.missionProgress(0) == 0 && !meta.missionClaimed(0), "Daily mission state resets without affecting earned progression");
        check(meta.recordWin(0, 1, 3, null) == 0, "Prior-day campaign clear cannot be farmed on the next visit");
        check(meta.recordWin(3, 1, 3, "remix-v1") == 0, "Remix first-clear identity survives date changes");
        check(meta.recordWin(1, 0, 3, "2026-11-03") == 35, "New daily board is independently eligible");
        check(meta.missionProgress(0) == 1 && meta.missionProgress(2) == 1, "New date receives only new unique board progress");
        check(meta.recordWin(2, 0, 3, "2026-11-02") == 35, "Weekly period uses current Monday across month boundary");
        clock.set("2026-10-26");
        meta.recordVisit();
        check(meta.daysPlayed() == 2, "Revisited local date never increments days twice");
        check(meta.claimMission(0, store) == 0, "Previously claimed date remains idempotent after date revisit");
    }

    private static void testMigration() {
        TestContext context = new TestContext();
        GameStore old = new GameStore(context);
        old.completeLevel(1, 1);
        old.completeLevel(2, 3);
        old.equipCharacter("nova");
        int coins = old.coins();
        Clock clock = new Clock("2026-10-26");
        MetaProgress meta = new MetaProgress(context, clock);
        check(meta.xp() == 60 && meta.completedBoards() == 2, "Upgrade migrates previous campaign completion into XP once");
        check(meta.missionProgress(0) == 0, "Legacy progress does not complete new daily missions");
        check(meta.recordWin(0, 1, 3, null) == 0, "Legacy campaign boards remain protected from XP farming");
        check(new MetaProgress(context, clock).xp() == 60, "Recreation never reruns migration XP payout");
        check(new GameStore(context).coins() == coins && new GameStore(context).stars(2) == 3, "Upgrade preserves existing wallet and campaign stars");

        TestContext interrupted = new TestContext();
        GameStore wallet = new GameStore(interrupted);
        MetaProgress recover = new MetaProgress(interrupted, clock);
        recover.recordWin(0, 1, 3, null);
        recover.recordWin(0, 2, 3, null);
        recover.recordWin(0, 3, 3, null);
        check(wallet.grantMilestone("mission:2026-10-26:0", 35) == 35, "Simulated wallet-before-meta commit");
        int paid = wallet.coins();
        check(recover.claimMission(0, wallet) == 0 && recover.missionClaimed(0), "Claim recovers safely when wallet token exists before meta flag");
        check(wallet.coins() == paid, "Recovery never duplicates currency");
        interrupted.files.get("luma_drift_save_v1").failNextCommit = true;
        boolean failureSurfaced = false;
        try { recover.claimMission(1, wallet); }
        catch (IllegalStateException persistenceFailure) { failureSurfaced = true; }
        check(failureSurfaced && !recover.missionClaimed(1), "Failed wallet persistence never marks mission claimed");
        check(wallet.coins() == paid, "Failed wallet persistence never changes balance");
        check(recover.claimMission(1, wallet) == 40 && wallet.coins() == paid + 40, "Mission claim retries after wallet persistence failure");

        TestContext sundayContext = new TestContext();
        Clock sunday = new Clock("2027-01-03");
        MetaProgress yearBoundary = new MetaProgress(sundayContext, sunday);
        check(yearBoundary.recordWin(2, 0, 3, "2026-12-28") == 35, "Sunday weekly period remains the previous year's Monday");
        sunday.set("2027-01-04");
        check(yearBoundary.recordWin(2, 0, 3, "2027-01-04") == 35, "New Monday starts a distinct weekly board identity");
    }

    private static void testJournal() {
        TestContext context = new TestContext();
        Clock clock = new Clock("2026-10-26");
        PlayJournal journal = new PlayJournal(context, clock);
        check(!journal.isEnabled() && journal.eventCount() == 0 && journal.firstObservedDate().isEmpty(), "Journal is off and has no invented install timestamp");
        journal.record("session_start", null);
        check(journal.eventCount() == 0 && journal.sessions() == 0, "Opt-out records no activity");
        check(journal.returnDay1().equals(PlayJournal.UNKNOWN) && journal.returnDay7().equals(PlayJournal.UNKNOWN), "Unobserved return windows stay unknown");
        journal.setEnabled(true);
        check(journal.firstObservedDate().equals("2026-10-26"), "Observation begins on explicit opt-in date");
        Map<String, String> fields = new HashMap<>();
        fields.put("test", "quote\" slash\\ newline\n control\u0001 emoji🙂");
        fields.put("mode", "campaign");
        journal.record("session_start", fields);
        journal.record("board_win", null);
        check(journal.eventCount() == 2 && journal.sessions() == 1 && journal.activeDays() == 1, "Session and event counts have separate precise definitions");
        String json = journal.exportJson();
        check(json.contains("\\\"") && json.contains("\\\\") && json.contains("\\n") && json.contains("\\u0001"), "Export escapes JSON strings and controls");
        check(json.contains("single-device") && json.contains("first_observed_local_date") && !json.contains("installation_date"), "Export labels observations without fabricated installation or global metrics");
        check(new PlayJournal(context, clock).eventCount() == 2, "Local journal persists recreation");
        clock.set("2026-10-27");
        check(journal.returnDay1().equals(PlayJournal.UNKNOWN), "D1 absence is unknown before local day ends");
        journal.record("session_start", null);
        check(journal.returnDay1().equals(PlayJournal.OBSERVED), "D1 recorded return is observed on exact calendar day");
        check(journal.activeDays() == 2 && journal.sessions() == 2, "Multiple observation dates counted once each");
        clock.set("2026-11-03");
        check(journal.returnDay7().equals(PlayJournal.NOT_OBSERVED_LOCAL), "Completed D7 window without event is explicitly local non-observation");
        journal.setEnabled(false);
        int size = journal.eventCount();
        journal.record("session_start", fields);
        check(journal.eventCount() == size && journal.sessions() == 2, "Disabling stops future recording immediately");
        journal.setEnabled(true);
        check(journal.returnDay7().equals(PlayJournal.UNKNOWN), "Paused observation cannot claim a missing-return measurement");
        check(journal.returnDay1().equals(PlayJournal.OBSERVED), "Already observed return remains valid after observation pause");
        journal.clear();
        check(journal.eventCount() == 0 && journal.sessions() == 0 && journal.activeDays() == 0, "Clear removes all events and summary data");
        check(journal.firstObservedDate().equals("2026-11-03") && journal.returnDay7().equals(PlayJournal.UNKNOWN), "Clear starts a new honest observation window");
        check(new GameStore(context).coins() == 180, "Clearing journal never clears gameplay economy");
        for (int i = 0; i < PlayJournal.MAX_EVENTS + 4; i++) journal.record("bounded_" + i, null);
        check(journal.eventCount() == PlayJournal.MAX_EVENTS, "Journal has a strict 2,000-event bound");
        check(!journal.exportJson().contains("\"bounded_3\"") && journal.exportJson().contains("\"bounded_2003\""), "Ring retention drops oldest records and retains latest");
        journal.setEnabled(false);
        journal.clear();
        check(journal.firstObservedDate().isEmpty() && journal.eventCount() == 0, "Disabled clear removes observation origin");

        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Luxembourg"));
        Clock daylightClock = new Clock("2026-03-28");
        PlayJournal daylight = new PlayJournal(new TestContext(), daylightClock);
        daylight.setEnabled(true);
        daylight.record("session_start", null);
        daylightClock.set("2026-03-29");
        daylight.record("session_start", null);
        check(daylight.returnDay1().equals(PlayJournal.OBSERVED), "D1 uses local calendar days across daylight-saving transition");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    private static void testCopy() {
        String[] keys = {"remix", "share_challenge", "import_challenge", "challenge_code", "challenge_invalid", "friend_challenge", "challenge_note", "share_result", "copy_code", "import_hint", "local_journal", "journal_desc", "export_journal", "clear_journal", "journal_empty", "export_note", "missions", "mission_boards", "mission_stars", "mission_event", "rank", "xp", "days_played", "badge", "free_opening", "tutorial_next", "tutorial_gate_live", "tutorial_dual_live", "stats", "local_only", "share_handoff", "share_caption", "share_card_title", "share_card_subtitle", "share_card_footer", "coach_motion", "coach_order", "coach_gate", "coach_dual", "journey_progress", "remix_progress", "mission_one", "mission_two", "mission_three", "journal_error", "tutorial_badge"};
        for (String key : keys) for (String language : new String[]{"en", "fr", "de", "es", "pt", "it"}) {
            check(Strings.has(key) && !Strings.get(language, key).equals(key), "New feature text translated: " + language + "/" + key);
        }
    }

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        if (args.length > 0 && args[0].equals("--export")) {
            PlayJournal journal = new PlayJournal(new TestContext(), new Clock("2026-10-26"));
            journal.setEnabled(true);
            Map<String, String> fields = new HashMap<>();
            fields.put("message", "quotation\" backslash\\ newline\n tab\t control\u0001 emoji🙂");
            journal.record("session_start", fields);
            System.out.print(journal.exportJson());
            return;
        }
        testProgression(); testMigration(); testJournal(); testCopy();
        System.out.println("PASS " + checks + " first-clear progression, claims, migration, journal and feature-copy checks");
    }
}
