import android.content.Context;
import android.content.SharedPreferences;
import com.androidhit.lumadrift.data.Catalog;
import com.androidhit.lumadrift.data.EconomyCurve;
import com.androidhit.lumadrift.data.GameStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JVM checks for test-mode prices and local gold fulfillment. The adapter models
 * Android's memory-before-disk commit behavior; it does not inject real storage faults.
 * Reference: AOSP SharedPreferencesImpl.commit() / commitToMemory().
 */
public final class EconomyTest {
    private static int checks;
    private static final class MemoryPreferences implements SharedPreferences {
        final Map<String, Object> values = new HashMap<>();
        final Map<String, Object> durableValues = new HashMap<>();
        boolean failNextCommit;
        boolean applyMemoryThenFail;
        synchronized void simulateProcessRestart() { values.clear(); values.putAll(durableValues); }
        @Override public synchronized boolean contains(String key) { return values.containsKey(key); }
        @Override public synchronized int getInt(String key, int fallback) { return (Integer) values.getOrDefault(key, fallback); }
        @Override public synchronized String getString(String key, String fallback) { return (String) values.getOrDefault(key, fallback); }
        @Override public synchronized boolean getBoolean(String key, boolean fallback) { return (Boolean) values.getOrDefault(key, fallback); }
        @Override public Editor edit() { return new Editor() {
            private final Map<String, Object> pending = new HashMap<>();
            @Override public Editor putInt(String key, int value) { pending.put(key, value); return this; }
            @Override public Editor putString(String key, String value) { pending.put(key, value); return this; }
            @Override public Editor putBoolean(String key, boolean value) { pending.put(key, value); return this; }
            @Override public boolean commit() {
                synchronized (MemoryPreferences.this) {
                    if (failNextCommit) {
                        failNextCommit = false;
                        if (applyMemoryThenFail) values.putAll(pending);
                        return false;
                    }
                    values.putAll(pending);
                    // Android writes the whole current memory map, including a prior
                    // memory-only attempt, when a subsequent commit succeeds.
                    durableValues.clear(); durableValues.putAll(values);
                    return true;
                }
            }
        }; }
    }
    private static final class TestContext extends Context {
        final MemoryPreferences preferences = new MemoryPreferences();
        @Override public SharedPreferences getSharedPreferences(String name, int mode) { return preferences; }
    }
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static void testCurve() {
        int[] boundaries = {0, 29, 30, 89, 90, 209, 210, 449, 450, 480};
        int[] bands = {0, 0, 1, 1, 2, 2, 3, 3, 4, 4};
        for (int i = 0; i < boundaries.length; i++) check(EconomyCurve.band(boundaries[i]) == bands[i], "Exact logarithmic band at " + boundaries[i]);
        int[] bases = {45, 40, 60};
        int[][] prices = {{45,55,65,75,85}, {40,50,60,65,75}, {60,75,85,100,110}};
        int[] sample = {0,30,90,210,450};
        for (int item = 0; item < bases.length; item++) {
            for (int band = 0; band < sample.length; band++) check(EconomyCurve.price(bases[item], sample[band]) == prices[item][band], "Published exact price for item " + item + " band " + band);
            int previous = 0;
            for (int progress = 0; progress <= 480; progress++) {
                int price = EconomyCurve.price(bases[item], progress);
                check(price >= previous && price >= bases[item] && price <= 2 * bases[item] && price % 5 == 0, "Monotonic five-coin price within base and cap at " + progress);
                check(price <= 2 * 55 && price <= 2 * 71, "Every helper pack stays within two three-star Journey or Remix rewards at " + progress);
                previous = price;
            }
            check(EconomyCurve.price(bases[item], -1) == bases[item], "Negative progress cannot discount below initial price");
            check(EconomyCurve.price(bases[item], Integer.MAX_VALUE) == prices[item][4], "Progress beyond content count cannot inflate prices further");
        }
        check(EconomyCurve.price(0, 480) == 0 && EconomyCurve.price(-1, 480) == 0, "Invalid base cannot produce a credit or negative cost");
        check(EconomyCurve.price(Integer.MAX_VALUE, 480) == Integer.MAX_VALUE, "Large base arithmetic does not overflow");
    }

    private static void testProgressAndPurchases() {
        TestContext context = new TestContext();
        GameStore store = new GameStore(context);
        check(store.economyProgress() == 0, "New save has zero priced progression");
        check(store.boosterPrice("hint") == 45 && store.boosterPrice("vision") == 40 && store.boosterPrice("flow") == 60, "All initial item prices preserved");
        check(store.boosterPrice("missing") == 0 && !store.purchaseBooster("missing"), "Unknown helper cannot be purchased for free");
        check(store.purchaseBooster("hint") && store.purchaseBooster("vision") && store.purchaseBooster("flow"), "Starting wallet can buy one of each helper pack");
        check(store.coins() == 35 && store.economyProgress() == 0, "Spending never raises the price progression");
        store.useBooster("hint");
        store.completeLevel(240, 3);
        store.completeLevel(1, 0);
        store.completeDaily(0, 3);
        store.completeWeekly(0, 3);
        check(store.economyProgress() == 0 && store.boosterPrice("hint") == 45, "Power use, rejected wins and events cannot raise helper prices");
        check(store.completeLevel(1, 1) == 35, "One-star Journey income remains unchanged");
        check(store.economyProgress() == 1, "First Journey clear raises progress once");
        check(store.completeLevel(1, 3) == 20 && store.economyProgress() == 1, "Star improvement preserves income without repricing a second clear");
        check(store.completeLevel(1, 3) == 0 && store.economyProgress() == 1, "Replay does not advance the price curve");
        store.grantGold("sandbox:funds:one", 1000);
        store.purchaseCharacter("miko");
        check(store.economyProgress() == 1 && store.boosterPrice("hint") == 45, "Simulated funding and cosmetic purchases cannot change prices");
        check(Catalog.character("miko").price == 160 && Catalog.skin("aurora").price == 450, "Cosmetic catalog prices remain fixed");

        TestContext fullContext = new TestContext();
        GameStore full = new GameStore(fullContext);
        int earned = 0;
        for (int level = 1; level <= 240; level++) {
            earned += full.completeLevel(level, 3);
            check(full.economyProgress() == level, "Journey distinct-clear progression " + level);
        }
        check(earned == 240 * 55, "Full Journey three-star earned income is unchanged");
        for (int level = 1; level <= 240; level++) {
            earned += full.completeRemix(level, 3);
            check(full.economyProgress() == 240 + level, "Remix distinct-clear progression " + level);
        }
        check(earned == 240 * (55 + 71) && full.coins() == 180 + earned, "All 480 boards preserve their total earned income");
        check(full.economyProgress() == 480 && full.boosterPrice("hint") == 85 && full.boosterPrice("vision") == 75 && full.boosterPrice("flow") == 110, "Maximum content has the documented bounded live prices");
        int oldWallet = full.coins(), oldStock = full.boosterStock("hint");
        check(full.purchaseBooster("hint"), "Earned progression can afford the late-game helper price");
        check(full.coins() == oldWallet - 85 && full.boosterStock("hint") == oldStock + 3, "Purchase debits live price, not old catalog base");
        check(full.economyProgress() == 480 && new GameStore(fullContext).boosterPrice("hint") == 85, "Purchase leaves prices stable across save recreation");
        fullContext.preferences.edit().putInt("coins", 45).commit();
        oldStock = full.boosterStock("hint");
        check(!full.purchaseBooster("hint") && full.coins() == 45 && full.boosterStock("hint") == oldStock, "Having only the initial base price cannot buy a later priced item");
        fullContext.preferences.edit().putInt("coins", 85).commit();
        check(full.purchaseBooster("hint") && full.coins() == 0, "Exact live price is sufficient and debited precisely");
    }

    private static void testGold() {
        TestContext context = new TestContext();
        GameStore store = new GameStore(context);
        int initial = store.coins();
        check(!store.isGoldGranted("sandbox:purchase:gold"), "Unfulfilled transaction is distinguishable from an applied duplicate");
        for (String invalid : new String[]{null,"","bad key","bad/id","bad.id","\n","étoile","x".repeat(161)}) check(store.grantGold(invalid, 100) == 0, "Malformed transaction identity rejected");
        for (String invalid : new String[]{null,"","bad key","bad/id","bad.id","\n","étoile","x".repeat(161)}) check(!store.isGoldGranted(invalid), "Malformed transaction query cannot claim fulfillment");
        for (int amount : new int[]{Integer.MIN_VALUE,-1,0,100001,Integer.MAX_VALUE}) check(store.grantGold("sandbox:amount:retry", amount) == 0, "Malformed transaction amount rejected");
        check(store.coins() == initial, "Rejected grant never credits currency");
        check(store.grantGold("sandbox:amount:retry", 1) == 1, "Invalid request does not consume a later valid transaction");
        check(store.grantGold("sandbox:purchase:gold", 500) == 500, "Explicit sandbox transaction credits requested gold");
        check(store.isGoldGranted("sandbox:purchase:gold"), "Applied transaction exposes its fulfillment state");
        check(store.grantGold("sandbox:purchase:gold", 500) == 0 && store.grantGold("sandbox:purchase:gold", 100000) == 0, "Repeated callback or amount change cannot grant a transaction twice");
        check(store.isGoldGranted("sandbox:purchase:gold") && new GameStore(context).isGoldGranted("sandbox:purchase:gold"), "Replay and store reload retain the fulfilled transaction query");
        check(new GameStore(context).grantGold("sandbox:purchase:gold", 500) == 0, "Transaction fulfillment survives store recreation");
        check(store.grantGold("x".repeat(160), 100000) == 100000, "Maximum approved transaction ID and amount accepted");
        check(store.coins() == initial + 100501, "Approved grants sum exactly once");
        check(store.economyProgress() == 0 && store.boosterPrice("hint") == 45, "Simulated gold never changes the price curve");
        check(store.grantMilestone("sandbox:purchase:gold", 35) == 35, "Milestone and gold transaction namespaces remain separate");
        check(!context.preferences.values.containsKey("gold_tx_v1_sandbox:purchase:gold"), "Raw transaction identity is not stored as a preference key");
        int hashedKeys = 0;
        for (String key : context.preferences.values.keySet()) if (key.startsWith("gold_tx_v1_")) {
            check(key.matches("gold_tx_v1_[0-9a-f]{64}"), "Fulfillment keys contain a fixed SHA-256 digest");
            hashedKeys++;
        }
        check(hashedKeys == 3, "Only approved distinct gold transactions create fulfillment tokens");

        int beforeFailure = store.coins();
        context.preferences.failNextCommit = true;
        boolean failed = false;
        try { store.grantGold("sandbox:failed:retry", 250); }
        catch (IllegalStateException persistenceFailure) { failed = true; }
        check(failed && store.coins() == beforeFailure, "Simulated failed durable write does not report a successful grant");
        check(new GameStore(context).grantGold("sandbox:failed:retry", 250) == 250, "Failed transaction can retry after persistence recovers");
        check(store.coins() == beforeFailure + 250 && store.grantGold("sandbox:failed:retry", 250) == 0, "Retried transaction is still paid once");

        context.preferences.edit().putInt("coins", 1_999_999_901).commit();
        check(store.grantGold("sandbox:cap:retry", 100) == 0 && store.coins() == 1_999_999_901, "Full-wallet boundary rejects the complete grant without truncation");
        check(!store.isGoldGranted("sandbox:cap:retry"), "Full-wallet rejection is distinguishable from an applied transaction");
        context.preferences.edit().putInt("coins", 1_999_999_900).commit();
        check(store.grantGold("sandbox:cap:retry", 100) == 100 && store.coins() == 2_000_000_000, "Wallet-space rejection does not consume a retryable transaction token");
        check(new GameStore(context).isGoldGranted("sandbox:cap:retry"), "Successfully retried full-wallet transaction reports applied after reload");
        check(store.grantGold("sandbox:cap:full", 1) == 0 && store.coins() == 2_000_000_000, "A full wallet cannot overflow");
    }

    private static void testConcurrentCallbacks() throws InterruptedException {
        TestContext context = new TestContext();
        new GameStore(context);
        AtomicInteger paid = new AtomicInteger();
        AtomicInteger failures = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        List<Thread> callbacks = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Thread callback = new Thread(() -> {
                try { start.await(); paid.addAndGet(new GameStore(context).grantGold("sandbox:duplicate:parallel", 250)); }
                catch (Exception failure) { failures.incrementAndGet(); }
            });
            callbacks.add(callback); callback.start();
        }
        start.countDown();
        for (Thread callback : callbacks) callback.join();
        check(failures.get() == 0 && paid.get() == 250 && new GameStore(context).coins() == 430, "Duplicate callbacks through independent store handles fulfill exactly once");
    }

    private static void testMemoryBeforeDiskFailure() {
        TestContext context = new TestContext();
        GameStore store = new GameStore(context);
        context.preferences.applyMemoryThenFail = true;
        context.preferences.failNextCommit = true;
        boolean firstFailed = false;
        try { store.grantGold("sandbox:memory:retry", 250); }
        catch (IllegalStateException persistenceFailure) { firstFailed = true; }
        check(firstFailed && store.coins() == 430 && store.isGoldGranted("sandbox:memory:retry"), "Android-like failed commit can expose a memory-only credit and token");
        check((Integer) context.preferences.durableValues.get("coins") == 180, "Memory-only failed credit is not durable proof");
        context.preferences.failNextCommit = true;
        boolean duplicateFailed = false;
        try { store.grantGold("sandbox:memory:retry", 250); }
        catch (IllegalStateException persistenceFailure) { duplicateFailed = true; }
        check(duplicateFailed && (Integer) context.preferences.durableValues.get("coins") == 180, "Duplicate branch cannot return success while flushing the recorded token still fails");
        check(store.grantGold("sandbox:memory:retry", 250) == 0 && store.coins() == 430, "Retry flushes the existing memory credit without adding gold twice");
        context.preferences.simulateProcessRestart();
        GameStore reloaded = new GameStore(context);
        check(reloaded.coins() == 430 && reloaded.isGoldGranted("sandbox:memory:retry"), "Successful retry persists both original credit and fulfillment token across simulated process restart");
        check(reloaded.grantGold("sandbox:memory:retry", 250) == 0 && reloaded.coins() == 430, "Persisted retry remains idempotent after process restart");

        TestContext killedContext = new TestContext();
        GameStore beforeKill = new GameStore(killedContext);
        killedContext.preferences.applyMemoryThenFail = true;
        killedContext.preferences.failNextCommit = true;
        try { beforeKill.grantGold("sandbox:memory:killed", 250); }
        catch (IllegalStateException expected) { /* The UI retains the pending transaction. */ }
        killedContext.preferences.simulateProcessRestart();
        GameStore afterKill = new GameStore(killedContext);
        check(afterKill.coins() == 180 && !afterKill.isGoldGranted("sandbox:memory:killed"), "Process death before retry discards uncommitted credit and token together");
        check(afterKill.grantGold("sandbox:memory:killed", 250) == 250 && afterKill.coins() == 430, "Retained pending transaction can fulfill once after an uncommitted attempt is lost");
    }

    public static void main(String[] args) throws InterruptedException {
        testCurve(); testProgressAndPurchases(); testGold(); testConcurrentCallbacks(); testMemoryBeforeDiskFailure();
        System.out.println("PASS " + checks + " progression-price, affordability, live-purchase and atomic test-gold checks");
        System.out.println("SANDBOX ONLY: no live payment, Play verification, ad completion or revenue is claimed by these JVM checks");
        System.out.println("DURABILITY MODEL: memory-before-disk failures and process reload are simulated; no physical storage fault was injected");
    }
}
