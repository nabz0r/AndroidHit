import android.content.Context;
import android.content.SharedPreferences;
import com.androidhit.lumadrift.data.Catalog;
import com.androidhit.lumadrift.data.GameStore;
import com.androidhit.lumadrift.data.Strings;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Dependency-free regression checks for currency, progress and translation data.
 * SharedPreferences is an in-memory adapter here; emulator tests cover actual
 * Android storage and activity recreation separately.
 */
public final class DataTest {
    private static int checks;

    private static final class MemoryPreferences implements SharedPreferences {
        private final Map<String, Object> values = new HashMap<>();
        @Override public boolean contains(String key) { return values.containsKey(key); }
        @Override public int getInt(String key, int fallback) {
            return values.containsKey(key) ? (Integer) values.get(key) : fallback;
        }
        @Override public String getString(String key, String fallback) {
            return values.containsKey(key) ? (String) values.get(key) : fallback;
        }
        @Override public boolean getBoolean(String key, boolean fallback) {
            return values.containsKey(key) ? (Boolean) values.get(key) : fallback;
        }
        @Override public Editor edit() { return new MemoryEditor(); }
        private final class MemoryEditor implements Editor {
            private final Map<String, Object> pending = new HashMap<>();
            @Override public Editor putInt(String key, int value) { pending.put(key, value); return this; }
            @Override public Editor putString(String key, String value) { pending.put(key, value); return this; }
            @Override public Editor putBoolean(String key, boolean value) { pending.put(key, value); return this; }
            @Override public boolean commit() { values.putAll(pending); return true; }
        }
    }

    private static final class TestContext extends Context {
        private final MemoryPreferences preferences = new MemoryPreferences();
        @Override public SharedPreferences getSharedPreferences(String name, int mode) { return preferences; }
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static void testEconomyAndProgress() {
        TestContext context = new TestContext();
        GameStore store = new GameStore(context);
        check(store.coins() == 180, "Starting currency");
        check(store.unlockedLevel() == 1, "Only first campaign level starts unlocked");
        check(!store.equipCharacter("zig"), "Unowned companion cannot be equipped");
        check(!store.equipSkin("aurora"), "Unowned theme cannot be equipped");
        check(store.purchaseCharacter("miko"), "Affordable companion can be bought");
        check(store.coins() == 20, "Purchase debits exact price");
        check(!store.purchaseCharacter("miko"), "An owned companion cannot be bought twice");
        check(store.coins() == 20, "Duplicate purchase does not debit currency");
        check(!store.purchaseSkin("missing"), "Unknown cosmetic cannot be bought");
        check(!store.purchaseSkin("lagoon"), "Unaffordable purchase rejected");
        check(store.coins() == 20, "Rejected purchase does not change wallet");
        check(store.equipCharacter("miko"), "Owned companion can be equipped");
        check(new GameStore(context).equippedCharacter().equals("miko"), "Store recreation retains equipment");

        check(store.completeLevel(240, 3) == 0, "Locked campaign level cannot pay rewards");
        check(store.completeLevel(1, 0) == 0, "Incomplete level cannot pay or unlock");
        check(store.unlockedLevel() == 1, "An incomplete level does not unlock the next");
        check(store.completeLevel(1, 1) == 35, "First completion reward");
        check(store.completeLevel(1, 1) == 0, "Repeated stars do not produce repeat rewards");
        check(store.completeLevel(1, 3) == 20, "Improved best pays only newly earned stars");
        check(store.completeLevel(1, 2) == 0, "Worse replay does not produce a reward");
        check(store.stars(1) == 3, "Worse replay does not overwrite best stars");
        check(store.unlockedLevel() == 2, "Completing a level unlocks its successor");

        check(store.purchaseBooster(Catalog.HINT), "Booster pack can be bought");
        check(store.boosterStock(Catalog.HINT) == 6, "Booster pack credited once");
        check(store.useBooster(Catalog.HINT), "An available booster can be consumed");
        check(store.boosterStock(Catalog.HINT) == 5, "Booster use debits one item");
        check(!store.useBooster("missing"), "Unknown booster rejected");
        for (int i = 0; i < 5; i++) check(store.useBooster(Catalog.HINT), "Stock remains usable");
        check(!store.useBooster(Catalog.HINT), "Zero stock cannot be consumed");
        check(store.boosterStock(Catalog.HINT) == 0, "Stock cannot become negative");

        for (int level = 2; level <= Catalog.LEVEL_COUNT; level++) {
            check(store.completeLevel(level, 3) == 55, "Campaign completion " + level);
        }
        check(store.unlockedLevel() == 240, "Unlocked level capped at last campaign level");
        check(store.totalStars() == 720, "All campaign stars counted");
        check(store.completedLevels() == 240, "All campaign completions counted");
        check(store.completeLevel(241, 3) == 0, "Out-of-range level cannot pay rewards");
        check(store.completeLevel(-1, 3) == 0, "Negative level cannot pay rewards");
        check(new GameStore(context).stars(240) == 3, "Recreated store retains progression");

        int balance = store.coins();
        for (int level = 1; level <= 240; level++) store.completeLevel(level, 3);
        check(store.coins() == balance, "Replaying entire campaign cannot farm earned rewards");
        check(store.purchaseSkin("lagoon"), "Earned currency buys a theme");
        balance = store.coins();
        check(!store.purchaseSkin("lagoon"), "Owned theme cannot be bought twice");
        check(store.coins() == balance, "Duplicate theme purchase does not debit wallet");
        check(store.equipSkin("lagoon"), "Owned theme can be equipped");
        check(new GameStore(context).equippedSkin().equals("lagoon"), "Recreated store retains theme");
    }

    private static void testEvents() {
        TestContext context = new TestContext();
        GameStore store = new GameStore(context);
        check(store.completeDaily(2, 3) == 0, "Daily stages unlock in order");
        check(store.claimDailyCompletionReward() == 0, "Daily bonus cannot be claimed early");
        check(store.completeDaily(0, 1) == 70, "First daily reward");
        check(store.completeDaily(0, 3) == 20, "Daily improvement pays only newly earned stars");
        check(store.completeDaily(0, 3) == 0, "Daily replay cannot farm currency");
        check(store.completeDaily(1, 3) == 90, "Second daily stage");
        check(store.completeDaily(2, 3) == 90, "Third daily stage");
        check(store.claimDailyCompletionReward() == 100, "Daily completion bonus");
        check(store.claimDailyCompletionReward() == 0, "Daily bonus cannot be claimed twice");
        check(new GameStore(context).dailyBonusClaimed(), "Daily claim survives store recreation");
        check(store.completeDaily("1900-01-01", 0, 3) == 0, "A stale daily board cannot claim a current reward");
        check(store.completeDaily(null, 0, 3) == 0, "Missing daily period rejected");

        check(store.completeWeekly(6, 3) == 0, "Weekly stages unlock in order");
        check(store.claimWeeklyCompletionReward() == 0, "Weekly bonus cannot be claimed early");
        for (int stage = 0; stage < 7; stage++) {
            check(store.completeWeekly(stage, 3) == 135, "Weekly completion " + stage);
        }
        check(store.claimWeeklyCompletionReward() == 250, "Weekly completion bonus");
        check(store.claimWeeklyCompletionReward() == 0, "Weekly bonus cannot be claimed twice");
        check(store.completeWeekly("1900-01-01", 0, 3) == 0, "A stale weekly board cannot claim a current reward");
        check(store.completeWeekly(null, 0, 3) == 0, "Missing weekly period rejected");
        check(store.dailySeed(0) == store.dailySeed(0), "Daily seed is stable within its date");
        check(store.dailySeed(0) != store.dailySeed(1), "Daily stages have distinct seeds");
        check(store.weeklySeed(0) != store.weeklySeed(1), "Weekly stages have distinct seeds");
        check(store.dailyId().matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"), "Daily identifier uses a calendar date");
        check(store.weeklyId().matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"), "Weekly identifier uses a calendar date");
        check(store.unlockedLevel() == 1, "Events do not unlock campaign levels");
    }

    private static void testRemixProgression() {
        TestContext context = new TestContext();
        GameStore store = new GameStore(context);
        check(!store.remixAvailable() && store.remixUnlockedLevel() == 1, "Fresh save cannot enter Remix");
        int initialCoins = store.coins();
        check(store.completeRemix(1, 3) == 0, "Remix cannot pay before its Journey prerequisite");
        check(store.coins() == initialCoins && store.remixStars(1) == 0, "Blocked Remix attempt leaves wallet and stars unchanged");
        for (int level = 1; level <= 11; level++) store.completeLevel(level, 1);
        check(!store.remixAvailable(), "Eleven Journey clears are insufficient to unlock Remix");
        store.completeDaily(0, 3);
        check(!store.remixAvailable(), "Daily clear cannot replace the twelfth Journey prerequisite");
        check(store.completeRemix(1, 3) == 0, "Remix reward remains blocked one Journey clear before unlock");
        store.completeLevel(12, 1);
        check(store.remixAvailable(), "Twelfth Journey clear unlocks Remix");
        check(new GameStore(context).remixAvailable(), "Remix availability persists store recreation");

        int journeyUnlock = store.unlockedLevel(), journeyStars = store.totalStars();
        int dailyStars = store.dailyStars(0), balance = store.coins();
        check(store.remixStars(1) == 0, "Journey clear does not pre-complete matching Remix board");
        check(store.completeRemix(2, 3) == 0, "Locked Remix board cannot skip progression");
        check(store.completeRemix(0, 3) == 0 && store.completeRemix(-1, 3) == 0, "Zero and negative Remix indexes rejected");
        check(store.completeRemix(241, 3) == 0, "Past-end Remix index rejected");
        check(store.completeRemix(1, 0) == 0 && store.completeRemix(1, -1) == 0, "Incomplete Remix board cannot pay or unlock");
        check(store.coins() == balance && store.remixUnlockedLevel() == 1, "Rejected Remix attempts preserve wallet and next board");

        check(store.completeRemix(1, 1) == 47, "First one-star Remix clear pays its first-clear reward");
        check(store.coins() == balance + 47 && store.remixUnlockedLevel() == 2, "First Remix reward credited while next board unlocks");
        check(store.completeRemix(1, 1) == 0, "Same-score Remix replay cannot farm currency");
        check(store.completeRemix(1, 3) == 24, "Remix star improvement pays only two newly earned stars");
        check(store.completeRemix(1, 2) == 0 && store.remixStars(1) == 3, "Worse Remix replay preserves best without reward");
        check(store.coins() == balance + 71, "First clear and improvements sum to the exact three-star reward");
        check(store.unlockedLevel() == journeyUnlock && store.totalStars() == journeyStars, "Remix leaves Journey unlocks and stars independent");
        check(store.dailyStars(0) == dailyStars && store.weeklyStars(0) == 0, "Remix leaves event progression independent");
        GameStore restored = new GameStore(context);
        check(restored.remixStars(1) == 3 && restored.remixUnlockedLevel() == 2 && restored.coins() == store.coins(), "Recreated store retains Remix stars, unlock and wallet");

        for (int level = 2; level <= Catalog.LEVEL_COUNT; level++) {
            check(restored.completeRemix(level, 3) == 71, "First complete Remix reward " + level);
        }
        check(restored.remixUnlockedLevel() == 240 && restored.remixStars(240) == 3, "Remix progression reaches and caps at final board");
        check(restored.remixStars(0) == 0 && restored.remixStars(-1) == 0 && restored.remixStars(241) == 0, "Out-of-range Remix star reads are empty");
        balance = restored.coins();
        for (int level = 1; level <= Catalog.LEVEL_COUNT; level++) restored.completeRemix(level, 3);
        check(restored.coins() == balance, "Replaying the full Remix journey cannot farm earned currency");
        check(restored.completeRemix(241, 3) == 0 && restored.remixUnlockedLevel() == 240, "Completed Remix journey cannot create a 241st board");
        check(restored.unlockedLevel() == 13 && restored.totalStars() == 12, "Completing all Remix boards never advances the independent Journey");
        check(new GameStore(context).remixStars(240) == 3, "Final Remix completion persists recreation");
    }

    private static void testMilestoneClaims() {
        TestContext context = new TestContext();
        GameStore store = new GameStore(context);
        int initial = store.coins();
        for (String key : new String[]{null, "", "contains space", "../escape", "bad/key", "line\nbreak", "étoile", "a".repeat(101)}) {
            check(store.grantMilestone(key, 35) == 0, "Malformed milestone identity rejected: " + key);
        }
        for (int amount : new int[]{Integer.MIN_VALUE, -1, 0, 501, Integer.MAX_VALUE}) {
            check(store.grantMilestone("mission:retry", amount) == 0, "Invalid milestone amount rejected: " + amount);
        }
        check(store.coins() == initial, "Rejected milestone requests never mutate currency");
        check(store.grantMilestone("mission:retry", 35) == 35, "Rejected amounts do not consume a valid future claim token");
        check(store.coins() == initial + 35, "Milestone credits the exact approved amount");
        check(store.grantMilestone("mission:retry", 35) == 0, "Duplicate milestone token cannot pay again");
        check(store.grantMilestone("mission:retry", 500) == 0, "Changing an already claimed amount cannot bypass idempotency");
        check(new GameStore(context).grantMilestone("mission:retry", 35) == 0, "Milestone token survives store recreation");
        check(store.coins() == initial + 35, "Repeated milestone requests preserve the credited balance");
        check(store.grantMilestone("mission:2026-10-26:1", 1) == 1, "Minimum approved reward and dated identity accepted");
        check(store.grantMilestone("a".repeat(100), 500) == 500, "Maximum approved reward and identity length accepted");
        check(store.coins() == initial + 536, "Independent milestone claims sum precisely");
        check(store.unlockedLevel() == 1 && store.totalStars() == 0 && store.remixStars(1) == 0, "Milestone reward cannot alter board progression");
        check(!store.ownsSkin("aurora") && store.boosterStock(Catalog.HINT) == 3, "Milestone reward does not silently grant cosmetics or powers");
    }

    private static void testLanguagesAndSettings() throws ReflectiveOperationException {
        TestContext context = new TestContext();
        GameStore store = new GameStore(context);
        for (String language : new String[]{"en", "fr", "de", "es", "pt", "it"}) {
            check(Strings.supports(language), "Language supported: " + language);
            store.setLanguage(language);
            check(store.resolvedLanguage().equals(language), "Selected language retained: " + language);
            check(Strings.get(language, "level_number", 5).contains("5"), "Numeric copy formats: " + language);
            check(!Strings.get(language, "tutorial_gate").isEmpty(), "Rules translated: " + language);
            String piece = Strings.get(language, "piece_description", 2, 3, Strings.get(language, "direction_up"));
            check(piece.contains("2") && piece.contains("3") && !piece.contains("%"), "Accessible piece description formats: " + language);
        }
        check(Strings.normalize("fr-FR").equals("fr"), "Regional language normalized");
        check(Strings.normalize("fr_FR-custom").equals("fr"), "Mixed locale separators normalized");
        check(Strings.normalize("zh").equals("en"), "Unknown language falls back to English");
        store.setLanguage("unknown");
        check(store.resolvedLanguage().equals("it"), "Unsupported preference does not replace a valid language");

        for (Catalog.CharacterItem item : Catalog.CHARACTERS) check(Strings.has(item.nameKey), "Companion has localized name");
        for (Catalog.SkinItem item : Catalog.SKINS) check(Strings.has(item.nameKey), "Theme has localized name");
        for (Catalog.BoosterItem item : Catalog.BOOSTERS) check(Strings.has(item.descriptionKey), "Booster has localized description");
        Field field = Strings.class.getDeclaredField("TEXT");
        field.setAccessible(true);
        Map<?, ?> copy = (Map<?, ?>) field.get(null);
        check(copy.size() >= 140, "Complete copy catalog is available");
        for (Map.Entry<?, ?> row : copy.entrySet()) {
            String[] translations = (String[]) row.getValue();
            check(translations.length == 6, "Every copy key has six translations: " + row.getKey());
            for (String value : translations) {
                check(value != null && !value.trim().isEmpty(), "Translation is nonempty: " + row.getKey());
                check(count(translations[0], "%d") == count(value, "%d"), "Numeric placeholders agree: " + row.getKey());
                check(count(translations[0], "%s") == count(value, "%s"), "Text placeholders agree: " + row.getKey());
            }
        }

        store.setSoundEnabled(false);
        store.setMusicEnabled(false);
        store.setHapticsEnabled(false);
        GameStore restored = new GameStore(context);
        check(!restored.soundEnabled() && !restored.musicEnabled() && !restored.hapticsEnabled(), "Store recreation retains audio and haptic preferences");
        check(restored.language().equals("it"), "Store recreation retains language");
        store.setTutorialSeen(true);
        check(new GameStore(context).tutorialSeen(), "Tutorial acknowledgement retained");
    }

    private static int count(String text, String fragment) {
        int count = 0;
        for (int position = 0; (position = text.indexOf(fragment, position)) >= 0; position += fragment.length()) count++;
        return count;
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        testEconomyAndProgress();
        testEvents();
        testRemixProgression();
        testMilestoneClaims();
        testLanguagesAndSettings();
        System.out.println("PASS " + checks + " data, economy, campaign, Remix, milestone, event and localization checks");
    }
}
