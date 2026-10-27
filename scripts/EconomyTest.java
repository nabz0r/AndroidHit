import android.content.Context;
import android.content.SharedPreferences;
import com.androidhit.lumadrift.data.Catalog;
import com.androidhit.lumadrift.data.EconomyCurve;
import com.androidhit.lumadrift.data.GameStore;
import com.androidhit.lumadrift.data.PowerUsePolicy;
import com.androidhit.lumadrift.data.AvatarGearCatalog;
import com.androidhit.lumadrift.data.AvatarLoadout;
import com.androidhit.lumadrift.data.GearStrings;

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
        @Override public synchronized Map<String, ?> getAll() { return new HashMap<>(values); }
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
        check(store.purchaseBooster("hint") && !store.purchaseBooster("vision") && !store.purchaseBooster("flow"), "Only the first helper can be purchased before its public unlock milestones");
        check(store.boosterStock("vision") == 3 && store.boosterStock("flow") == 2, "Legacy starter stocks stay saved while locked");
        check(!store.useBooster("vision") && !store.useBooster("flow"), "Locked legacy stocks cannot bypass unlock milestones");
        check(store.coins() == 135 && store.economyProgress() == 0, "Spending never raises the price progression");
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
        for (int level = 1; level <= Catalog.LEVEL_COUNT; level++) {
            earned += full.completeLevel(level, 3);
            check(full.economyProgress() == level, "Journey distinct-clear progression " + level);
        }
        check(earned == Catalog.LEVEL_COUNT * 55, "Full Journey three-star earned income is unchanged");
        for (int level = 1; level <= Catalog.REMIX_COUNT; level++) {
            earned += full.completeRemix(level, 3);
            check(full.economyProgress() == Catalog.LEVEL_COUNT + level, "Remix distinct-clear progression " + level);
        }
        check(earned == Catalog.LEVEL_COUNT * 55 + Catalog.REMIX_COUNT * 71 && full.coins() == 180 + earned, "All Journey and Remix boards preserve their total earned income");
        check(full.economyProgress() == Catalog.LEVEL_COUNT + Catalog.REMIX_COUNT && full.boosterPrice("hint") == 85 && full.boosterPrice("vision") == 75 && full.boosterPrice("flow") == 110, "Maximum content has the documented bounded live prices");
        int oldWallet = full.coins(), oldStock = full.boosterStock("hint");
        check(full.purchaseBooster("hint"), "Earned progression can afford the late-game helper price");
        check(full.coins() == oldWallet - 85 && full.boosterStock("hint") == oldStock + 3, "Purchase debits live price, not old catalog base");
        check(full.economyProgress() == Catalog.LEVEL_COUNT + Catalog.REMIX_COUNT && new GameStore(fullContext).boosterPrice("hint") == 85, "Purchase leaves prices stable across save recreation");
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

    private static String[] invalidRewardIds() {
        return new String[]{null,"","bad key","bad/id","bad.id","\n","étoile","x".repeat(161)};
    }
    private static TestContext progressed(int clears) {
        TestContext context=new TestContext();GameStore store=new GameStore(context);
        for(int i=1;i<=clears;i++)store.completeLevel(i,3);
        return context;
    }
    private static int totalPowerStock(GameStore store) {
        int sum=0;for(Catalog.BoosterItem item:Catalog.BOOSTERS)sum+=store.boosterStock(item.id);return sum;
    }
    private static void expectFailure(Runnable action,String message) {
        boolean failed=false;try{action.run();}catch(IllegalStateException expected){failed=true;}check(failed,message);
    }
    private static void testContinuationDebits() throws InterruptedException {
        TestContext context=new TestContext();GameStore store=new GameStore(context);
        check(GameStore.CONTINUE_GOLD_COST==100,"Continuation cost is exactly the published 100 gold");
        for(String id:invalidRewardIds())check(store.spendGoldOnce(id,100)==0&&!store.hasGoldSpent(id),"Malformed debit ID rejected");
        for(int amount:new int[]{Integer.MIN_VALUE,-1,0,1,99,101,100000,Integer.MAX_VALUE})check(store.spendGoldOnce("continue:bad:amount",amount)==0,"Only exact continuation price accepted");
        check(store.coins()==180&&!store.hasGoldSpent("continue:bad:amount"),"Rejected debits change neither balance nor token");
        check(store.spendGoldOnce("continue:one",100)==100&&store.coins()==80,"First continuation debits exactly100");
        check(store.spendGoldOnce("continue:one",100)==0&&store.hasGoldSpent("continue:one")&&store.coins()==80,"Duplicate succeeds idempotently even when remaining balance is below100");
        check(new GameStore(context).hasGoldSpent("continue:one")&&new GameStore(context).spendGoldOnce("continue:one",100)==0,"Debit identity survives independent handle recreation");
        check(store.spendGoldOnce("continue:low",100)==0&&!store.hasGoldSpent("continue:low"),"Insufficient balance leaves request retryable");
        store.grantGold("continue:funding",100);check(store.spendGoldOnce("continue:low",100)==100&&store.coins()==80,"Previously unfunded request can later debit once");
        check(store.spendGoldOnce("continue:one",99)==0&&store.coins()==80,"A changed amount cannot bypass strict fixed price");
        context.preferences.edit().putInt("coins",100).commit();check(store.spendGoldOnce("continue:exact",100)==100&&store.coins()==0,"Exact balance is sufficient and never negative");
        check(store.grantGold("continue:shared",500)==500&&store.spendGoldOnce("continue:shared",100)==100&&store.coins()==400,"Credit and debit use independent hashed namespaces");
        for(String key:context.preferences.values.keySet())if(key.startsWith("gold_spend_v1_"))check(key.matches("gold_spend_v1_[0-9a-f]{64}"),"Debit preference key contains SHA256, not raw request ID");
        TestContext fail=new TestContext();GameStore failed=new GameStore(fail);fail.preferences.failNextCommit=true;
        expectFailure(()->failed.spendGoldOnce("continue:disk",100),"Unapplied failed debit throws before continuation success");
        check(failed.coins()==180&&!failed.hasGoldSpent("continue:disk"),"Failure before memory leaves balance/token unchanged");
        check(new GameStore(fail).spendGoldOnce("continue:disk",100)==100&&failed.coins()==80,"Failed debit retries once through another store handle");
        TestContext memory=new TestContext();GameStore mem=new GameStore(memory);memory.preferences.applyMemoryThenFail=true;memory.preferences.failNextCommit=true;
        expectFailure(()->mem.spendGoldOnce("continue:memory",100),"Android-like memory-only debit is not reported as durable");
        check(mem.coins()==80&&mem.hasGoldSpent("continue:memory")&&(Integer)memory.preferences.durableValues.get("coins")==180,"Memory-before-disk debit fixture reproduces Android ordering");
        memory.preferences.failNextCommit=true;expectFailure(()->new GameStore(memory).spendGoldOnce("continue:memory",100),"Duplicate debit retry also throws if its durability flush fails");
        check(mem.spendGoldOnce("continue:memory",100)==0&&mem.coins()==80,"Successful retry flushes without charging a second100");
        memory.preferences.simulateProcessRestart();check(new GameStore(memory).coins()==80&&new GameStore(memory).hasGoldSpent("continue:memory"),"Retried debit and token survive simulated restart together");
        TestContext concurrent=new TestContext();new GameStore(concurrent);AtomicInteger sum=new AtomicInteger(),errors=new AtomicInteger();CountDownLatch ready=new CountDownLatch(1);List<Thread> threads=new ArrayList<>();
        for(int i=0;i<12;i++){Thread t=new Thread(()->{try{ready.await();sum.addAndGet(new GameStore(concurrent).spendGoldOnce("continue:parallel",100));}catch(Exception e){errors.incrementAndGet();}});threads.add(t);t.start();}
        ready.countDown();for(Thread t:threads)t.join();check(errors.get()==0&&sum.get()==100&&new GameStore(concurrent).coins()==80,"Twelve independent duplicate callbacks debit once");
    }
    private static void testPowerUnlocks() {
        int[] thresholds={1,5,12,25,45,75};String[] ids={"hint","vision","flow","shield","focus","trail"};
        check(Catalog.BOOSTERS.length==6,"Six powerups in stable catalog order");TestContext context=new TestContext();GameStore store=new GameStore(context);
        check(store.boosterStockLimit()==999,"Public stock cap");
        for(int i=0;i<ids.length;i++)check(Catalog.BOOSTERS[i].id.equals(ids[i])&&Catalog.BOOSTERS[i].unlockLevel==thresholds[i],"Published power milestone "+ids[i]);
        for(int level=1;level<=75;level++){
            for(int i=0;i<ids.length;i++){
                check(store.boosterUnlocked(ids[i])==(level>=thresholds[i]),"Exact Journey unlock boundary for "+ids[i]+" at"+level);
                if(level<thresholds[i]){int stock=store.boosterStock(ids[i]),coins=store.coins();check(!store.useBooster(ids[i])&&!store.purchaseBooster(ids[i]),"Locked power cannot be used/bought");check(store.boosterStock(ids[i])==stock&&store.coins()==coins,"Locked attempt preserves saved stock and currency");}
            }
            store.completeLevel(level,3);
        }
        check(!store.boosterUnlocked("missing")&&!store.useBooster("missing")&&!store.purchaseBooster("missing"),"Unknown power cannot bypass milestone gates");
        for(String id:ids)check(store.purchaseBooster(id),"Every power is purchasable after milestone75");
    }
    private static void testPowerRewards() throws InterruptedException {
        TestContext context=new TestContext();GameStore store=new GameStore(context);int initial=store.boosterStock("hint");
        for(String id:invalidRewardIds())check(store.grantBoosterOnce("hint",id)==0&&!store.hasBoosterGranted("hint",id),"Malformed earned-power ID rejected");
        check(store.grantBoosterOnce("vision","power:locked")==0&&!store.hasBoosterGranted("vision","power:locked"),"Locked power cannot be granted by an ad callback");
        check(store.grantBoosterOnce("missing","power:missing")==0,"Unknown earned power rejected");
        check(store.grantBoosterOnce("hint","power:one")==1&&store.boosterStock("hint")==initial+1,"One earned ad request grants one unit, not a pack");
        check(store.grantBoosterOnce("hint","power:one")==0&&store.hasBoosterGranted("hint","power:one")&&store.boosterStock("hint")==initial+1,"Earned callback is idempotent");
        for(int i=1;i<=4;i++)store.completeLevel(i,3);
        check(store.boosterUnlocked("vision")&&store.grantBoosterOnce("vision","power:one")==0&&!store.hasBoosterGranted("vision","power:one"),"A request bound to one power cannot grant another unlocked power");
        context.preferences.edit().putInt("booster_hint",999).commit();check(store.grantBoosterOnce("hint","power:cap")==0&&!store.hasBoosterGranted("hint","power:cap"),"Full stock refuses reward without consuming its request");
        context.preferences.edit().putInt("booster_hint",998).commit();check(store.grantBoosterOnce("hint","power:cap")==1&&store.boosterStock("hint")==999,"Same reward can retry when room becomes available");
        TestContext memory=new TestContext();GameStore mem=new GameStore(memory);memory.preferences.applyMemoryThenFail=true;memory.preferences.failNextCommit=true;
        expectFailure(()->mem.grantBoosterOnce("hint","power:memory"),"Memory-only power reward throws");
        check(mem.boosterStock("hint")==4&&mem.hasBoosterGranted("hint","power:memory"),"Failed Android-like reward leaves one memory unit and token");
        memory.preferences.failNextCommit=true;expectFailure(()->new GameStore(memory).grantBoosterOnce("hint","power:memory"),"Duplicate power flush must persist before success");
        check(new GameStore(memory).grantBoosterOnce("hint","power:memory")==0&&mem.boosterStock("hint")==4,"Power retry flush never grants twice");
        memory.preferences.simulateProcessRestart();check(new GameStore(memory).boosterStock("hint")==4&&new GameStore(memory).hasBoosterGranted("hint","power:memory"),"Reward and token reload together");
        TestContext concurrent=new TestContext();new GameStore(concurrent);AtomicInteger sum=new AtomicInteger();CountDownLatch ready=new CountDownLatch(1);List<Thread> threads=new ArrayList<>();
        for(int i=0;i<12;i++){Thread t=new Thread(()->{try{ready.await();sum.addAndGet(new GameStore(concurrent).grantBoosterOnce("hint","power:parallel"));}catch(InterruptedException e){throw new AssertionError(e);}});threads.add(t);t.start();}
        ready.countDown();for(Thread t:threads)t.join();check(sum.get()==1&&new GameStore(concurrent).boosterStock("hint")==4,"Twelve independent earned callbacks grant only one unit");
    }
    private static void testMilestoneLoot() throws InterruptedException {
        TestContext context=new TestContext();GameStore store=new GameStore(context);check(store.nextLootMilestone()==5&&store.claimMilestoneLoot().isEmpty(),"First loot is earned at five Journey clears");
        store.completeDaily(0,3);store.completeWeekly(0,3);check(store.claimMilestoneLoot().isEmpty(),"Event rewards cannot unlock Journey loot");
        int rewards=0;
        for(int level=1;level<=Catalog.LEVEL_COUNT;level++){
            store.completeLevel(level,3);
            if(level%5==0){int before=totalPowerStock(store);List<String> eligible=new ArrayList<>();for(Catalog.BoosterItem item:Catalog.BOOSTERS)if(store.boosterUnlocked(item.id))eligible.add(item.id);
                String expected=eligible.get((level/5-1)%eligible.size());String got=store.claimMilestoneLoot();check(expected.equals(got),"Deterministic cycle at Journey milestone"+level);check(totalPowerStock(store)==before+1,"One unit per earned milestone");
                rewards++;check(store.nextLootMilestone()==(level+5<=Catalog.LEVEL_COUNT?level+5:0),"Next milestone advances once");check(store.claimMilestoneLoot().isEmpty()&&totalPowerStock(store)==before+1,"Repeated claim cannot farm stock");
                store.completeLevel(level,3);check(store.claimMilestoneLoot().isEmpty(),"Replaying cleared Journey cannot earn another loot milestone");
            }
        }
        check(rewards==Catalog.LEVEL_COUNT/5&&new GameStore(context).nextLootMilestone()==0,"All catalog milestones persist and stop at the end");
        TestContext full=progressed(5);GameStore capped=new GameStore(full);full.preferences.edit().putInt("booster_hint",999).commit();check(capped.claimMilestoneLoot().isEmpty()&&capped.nextLootMilestone()==5,"Full selected stock does not consume milestone");
        full.preferences.edit().putInt("booster_hint",998).commit();check(capped.claimMilestoneLoot().equals("hint")&&capped.boosterStock("hint")==999,"Milestone retries after stock has room");
        TestContext memory=progressed(5);GameStore mem=new GameStore(memory);int before=mem.boosterStock("hint");memory.preferences.applyMemoryThenFail=true;memory.preferences.failNextCommit=true;
        expectFailure(mem::claimMilestoneLoot,"Memory-only loot award is not reported as durable");
        check(mem.boosterStock("hint")==before+1&&new GameStore(memory).nextLootMilestone()==5,"Failed loot stays at the same milestone through a second handle");
        memory.preferences.failNextCommit=true;expectFailure(()->new GameStore(memory).claimMilestoneLoot(),"Loot retry does not advance if its flush fails");
        check(new GameStore(memory).claimMilestoneLoot().equals("hint")&&mem.boosterStock("hint")==before+1&&mem.nextLootMilestone()==10,"Successful retry acknowledges original loot without adding another unit");
        memory.preferences.simulateProcessRestart();check(new GameStore(memory).claimMilestoneLoot().isEmpty()&&new GameStore(memory).boosterStock("hint")==before+1,"Persisted loot cannot reappear after simulated reload");
        TestContext noMemory=progressed(5);GameStore noMem=new GameStore(noMemory);noMemory.preferences.failNextCommit=true;expectFailure(noMem::claimMilestoneLoot,"Loot failure before memory throws");check(noMem.nextLootMilestone()==5&&noMem.claimMilestoneLoot().equals("hint")&&noMem.boosterStock("hint")==4,"Unapplied loot failure retries exactly once");
        TestContext concurrent=progressed(5);AtomicInteger granted=new AtomicInteger();CountDownLatch ready=new CountDownLatch(1);List<Thread> threads=new ArrayList<>();
        for(int i=0;i<12;i++){Thread t=new Thread(()->{try{ready.await();if(!new GameStore(concurrent).claimMilestoneLoot().isEmpty())granted.incrementAndGet();}catch(InterruptedException e){throw new AssertionError(e);}});threads.add(t);t.start();}
        ready.countDown();for(Thread t:threads)t.join();check(granted.get()==1&&new GameStore(concurrent).boosterStock("hint")==4,"Concurrent milestone callers collect one unit exactly once");
        TestContext catchup=progressed(75);GameStore late=new GameStore(catchup);for(int i=0;i<15;i++)check(late.claimMilestoneLoot().equals(Catalog.BOOSTERS[i%6].id),"Late claims cycle deterministically across currently unlocked powers");check(late.nextLootMilestone()==80&&late.claimMilestoneLoot().isEmpty(),"Missed eligible milestones can be caught up without advancing unearned milestones");
    }
    private static void testConcurrentPowerUseAndPurchases() throws InterruptedException {
        TestContext useContext=new TestContext();new GameStore(useContext);AtomicInteger uses=new AtomicInteger();CountDownLatch useReady=new CountDownLatch(1);List<Thread> users=new ArrayList<>();
        for(int i=0;i<12;i++){Thread t=new Thread(()->{try{useReady.await();if(new GameStore(useContext).useBooster("hint"))uses.incrementAndGet();}catch(InterruptedException e){throw new AssertionError(e);}});users.add(t);t.start();}
        useReady.countDown();for(Thread t:users)t.join();check(uses.get()==3&&new GameStore(useContext).boosterStock("hint")==0,"Concurrent independent handles can consume only the three actual starting units");
        TestContext buyContext=new TestContext();new GameStore(buyContext);AtomicInteger buys=new AtomicInteger();CountDownLatch buyReady=new CountDownLatch(1);List<Thread> buyers=new ArrayList<>();
        for(int i=0;i<12;i++){Thread t=new Thread(()->{try{buyReady.await();if(new GameStore(buyContext).purchaseBooster("hint"))buys.incrementAndGet();}catch(InterruptedException e){throw new AssertionError(e);}});buyers.add(t);t.start();}
        buyReady.countDown();for(Thread t:buyers)t.join();check(buys.get()==4&&new GameStore(buyContext).coins()==0&&new GameStore(buyContext).boosterStock("hint")==15,"Concurrent pack purchases serialize balance and stock: four affordable packs only");
    }
    private static void testPowerEffectReservations() throws InterruptedException {
        TestContext context=progressed(75);GameStore store=new GameStore(context);int original=store.boosterStock("hint"),coins=store.coins();
        for(String invalid:invalidRewardIds())check(!store.reserveBoosterUse("hint",invalid),"Invalid effect request cannot reserve stock");
        check(!store.reserveBoosterUse("missing","effect:unknown"),"Unknown effect cannot reserve stock");
        check(store.reserveBoosterUse("hint","effect:first")&&store.boosterStock("hint")==original-1,"Reservation holds one available charge");
        check((Integer)context.preferences.values.get("booster_hint")==original&&store.coins()==coins,"Reservation does not charge raw inventory or currency before activation");
        GameStore.PendingBoosterUse pending=new GameStore(context).pendingBoosterUse();check(pending!=null&&pending.id.equals("hint")&&pending.requestId.equals("effect:first"),"Reservation restores through a new store handle");
        check(new GameStore(context).reserveBoosterUse("hint","effect:first")&&store.boosterStock("hint")==original-1,"Reservation retry cannot hold a second charge");
        check(!store.reserveBoosterUse("vision","effect:first")&&!store.reserveBoosterUse("vision","effect:second"),"A request and active reservation cannot be rebound to another effect");
        check(!store.completeBoosterUse("vision","effect:first")&&!store.cancelBoosterUse("hint","effect:other"),"Unrelated completion/cancellation cannot release escrow");
        check(store.completeBoosterUse("hint","effect:first")&&store.boosterStock("hint")==original-1&&store.pendingBoosterUse()==null,"Only actual activation completion charges one unit");
        check(new GameStore(context).completeBoosterUse("hint","effect:first")&&store.boosterStock("hint")==original-1,"Duplicate completion never charges twice");
        check(!store.cancelBoosterUse("hint","effect:first")&&!store.reserveBoosterUse("hint","effect:first"),"Used effect cannot be refunded or reactivated with its old request");
        check(store.reserveBoosterUse("hint","effect:cancel")&&store.cancelBoosterUse("hint","effect:cancel")&&store.boosterStock("hint")==original-1,"Failed effect releases its reservation without spending a charge");
        check(store.cancelBoosterUse("hint","effect:cancel")&&!store.completeBoosterUse("hint","effect:cancel")&&!store.reserveBoosterUse("hint","effect:cancel"),"Cancellation is idempotent and its request is terminal");
        context.preferences.edit().putInt("booster_hint",0).commit();check(!store.reserveBoosterUse("hint","effect:empty")&&store.pendingBoosterUse()==null,"Empty stock never creates an activation");
        TestContext locked=new TestContext();GameStore lock=new GameStore(locked);check(!lock.reserveBoosterUse("vision","effect:locked")&&lock.boosterStock("vision")==3,"Locked legacy inventory cannot be reserved");

        TestContext reward=new TestContext();GameStore rewards=new GameStore(reward);rewards.grantBoosterOnce("hint","effect:reward");rewards.reserveBoosterUse("hint","effect:hold");
        check(rewards.grantBoosterOnce("hint","effect:reward")==0&&rewards.boosterStock("hint")==3,"Duplicate reward persistence preserves the held charge");
        check(rewards.cancelBoosterUse("hint","effect:hold")&&rewards.boosterStock("hint")==4,"Releasing escrow restores the preexisting reward exactly once");
        reward.preferences.edit().putInt("booster_hint",999).commit();rewards.reserveBoosterUse("hint","effect:cap");
        check(rewards.grantBoosterOnce("hint","effect:overcap")==0&&!rewards.purchaseBooster("hint"),"Escrow still counts towards physical stock capacity");
        check(rewards.cancelBoosterUse("hint","effect:cap")&&rewards.boosterStock("hint")==999,"Cancellation cannot overflow or lose a held charge at stock cap");
        TestContext loot=progressed(5);GameStore loots=new GameStore(loot);loot.preferences.edit().putInt("booster_hint",999).commit();loots.reserveBoosterUse("hint","effect:loot-cap");
        check(loots.claimMilestoneLoot().isEmpty()&&loots.nextLootMilestone()==5,"Milestone cannot overfill a reserved physical inventory");loots.cancelBoosterUse("hint","effect:loot-cap");

        TestContext parallel=new TestContext();new GameStore(parallel);AtomicInteger reserved=new AtomicInteger(),completed=new AtomicInteger(),errors=new AtomicInteger();CountDownLatch ready=new CountDownLatch(1);List<Thread> threads=new ArrayList<>();
        for(int i=0;i<12;i++){final int index=i;Thread t=new Thread(()->{try{ready.await();if(new GameStore(parallel).reserveBoosterUse("hint","effect:parallel:"+index))reserved.incrementAndGet();}catch(Exception failure){errors.incrementAndGet();}});threads.add(t);t.start();}
        ready.countDown();for(Thread t:threads)t.join();check(errors.get()==0&&reserved.get()==1&&new GameStore(parallel).boosterStock("hint")==2,"Concurrent distinct activations can reserve only one in-flight effect");
        String request=new GameStore(parallel).pendingBoosterUse().requestId;threads.clear();CountDownLatch commitReady=new CountDownLatch(1);
        for(int i=0;i<12;i++){Thread t=new Thread(()->{try{commitReady.await();if(new GameStore(parallel).completeBoosterUse("hint",request))completed.incrementAndGet();}catch(Exception failure){errors.incrementAndGet();}});threads.add(t);t.start();}
        commitReady.countDown();for(Thread t:threads)t.join();check(errors.get()==0&&completed.get()==12&&new GameStore(parallel).boosterStock("hint")==2,"Twelve completion retries still charge exactly one physical unit");
    }
    private static void testPowerReservationPersistenceFailures() {
        TestContext before=new TestContext();GameStore beforeStore=new GameStore(before);before.preferences.failNextCommit=true;
        expectFailure(()->beforeStore.reserveBoosterUse("hint","effect:before"),"Reservation must not report success before persistence");
        check(beforeStore.boosterStock("hint")==3&&beforeStore.pendingBoosterUse()==null,"Failure before memory leaves inventory unreserved");
        TestContext reserve=new TestContext();GameStore reserveStore=new GameStore(reserve);reserve.preferences.applyMemoryThenFail=true;reserve.preferences.failNextCommit=true;
        expectFailure(()->reserveStore.reserveBoosterUse("hint","effect:reserve"),"Memory-only reservation is not an activation");
        check(reserveStore.boosterStock("hint")==2&&reserveStore.pendingBoosterUse()!=null,"Memory-only reservation is visible for cancellation or retry");
        check(new GameStore(reserve).cancelBoosterUse("hint","effect:reserve")&&reserveStore.boosterStock("hint")==3,"Failed activation releases its held unit through a second handle");
        reserve.preferences.simulateProcessRestart();check(new GameStore(reserve).boosterStock("hint")==3,"Cancelled failed reservation survives reload without a charge");
        TestContext abandoned=new TestContext();GameStore abandonedStore=new GameStore(abandoned);abandonedStore.reserveBoosterUse("hint","effect:abandoned");abandoned.preferences.simulateProcessRestart();
        check(new GameStore(abandoned).pendingBoosterUse()!=null&&new GameStore(abandoned).cancelBoosterUse("hint","effect:abandoned")&&new GameStore(abandoned).boosterStock("hint")==3,"Restore can cancel a reservation absent from the effect/run snapshot");
        TestContext finished=new TestContext();GameStore finishedStore=new GameStore(finished);finishedStore.reserveBoosterUse("hint","effect:finished");finished.preferences.applyMemoryThenFail=true;finished.preferences.failNextCommit=true;
        expectFailure(()->finishedStore.completeBoosterUse("hint","effect:finished"),"Memory-only completion must be retried");
        check(finishedStore.boosterStock("hint")==2&&finishedStore.pendingBoosterUse()==null,"Applied completion has exactly one in-memory charge");
        finished.preferences.failNextCommit=true;expectFailure(()->new GameStore(finished).completeBoosterUse("hint","effect:finished"),"Repeated completion cannot claim durability when flushing still fails");
        check(new GameStore(finished).completeBoosterUse("hint","effect:finished")&&finishedStore.boosterStock("hint")==2,"Completion retry flushes the original charge without another debit");
        finished.preferences.simulateProcessRestart();check(new GameStore(finished).completeBoosterUse("hint","effect:finished")&&new GameStore(finished).boosterStock("hint")==2,"Committed effect and stock remain charged once after reload");
        TestContext crashed=new TestContext();GameStore crashedStore=new GameStore(crashed);crashedStore.reserveBoosterUse("hint","effect:crash");crashed.preferences.applyMemoryThenFail=true;crashed.preferences.failNextCommit=true;
        expectFailure(()->crashedStore.completeBoosterUse("hint","effect:crash"),"Crash fixture interrupts completion persistence");crashed.preferences.simulateProcessRestart();
        check(new GameStore(crashed).pendingBoosterUse()!=null&&new GameStore(crashed).completeBoosterUse("hint","effect:crash")&&new GameStore(crashed).boosterStock("hint")==2,"Saved run marker allows restored escrow to complete exactly once");
        TestContext cancelled=new TestContext();GameStore cancelledStore=new GameStore(cancelled);cancelledStore.reserveBoosterUse("hint","effect:cancelled");cancelled.preferences.applyMemoryThenFail=true;cancelled.preferences.failNextCommit=true;
        expectFailure(()->cancelledStore.cancelBoosterUse("hint","effect:cancelled"),"Cancellation must also be durable");
        check(new GameStore(cancelled).cancelBoosterUse("hint","effect:cancelled")&&cancelledStore.boosterStock("hint")==3,"Cancellation retry never fabricates another inventory unit");
        cancelled.preferences.simulateProcessRestart();check(new GameStore(cancelled).boosterStock("hint")==3&&new GameStore(cancelled).pendingBoosterUse()==null,"Cancelled stock reloads without escrow");
    }
    private static void testPowerInputPolicy() {
        PowerUsePolicy policy=new PowerUsePolicy();
        for(String id:new String[]{null,"","missing"})check(!policy.tryBegin(id,false,1000),"Unknown IDs cannot start input ownership");
        check(!policy.tryBegin("hint",true,1000)&&!policy.isBusy(),"An effect that is already active rejects use without taking ownership");
        check(!policy.tryBegin("hint",false,-1),"Invalid monotonic time rejects activation");
        check(policy.tryBegin("hint",false,1000)&&policy.isBusy("hint"),"Fresh activation takes input ownership");
        check(!policy.tryBegin("hint",false,1800)&&!policy.tryBegin("vision",false,1800),"Repeated and different powers cannot interrupt an in-flight effect");
        policy.finish("vision",1800);check(policy.isBusy("hint"),"Unrelated callback cannot finish another power");
        policy.finish("hint",1800);check(!policy.isBusy()&&!policy.tryBegin("vision",false,2249),"Input debounce starts after completion");
        check(policy.tryBegin("vision",false,2250),"A new valid effect can start after debounce");policy.cancel();check(!policy.isBusy()&&!policy.tryBegin("flow",false,2251),"Cancellation frees sequence ownership but prevents an immediate double tap");
        check(policy.tryBegin("flow",false,2700)&&!policy.tryBegin("flow",false,9000),"Flow remains exclusive for its whole asynchronous sequence");policy.finish("flow",9000);
        check(PowerUsePolicy.durationMillis("hint")==8000&&PowerUsePolicy.durationMillis("vision")==8000&&PowerUsePolicy.durationMillis("focus")==10000&&PowerUsePolicy.durationMillis("trail")==12000,"Descriptions share the exact established effect durations");
        check(PowerUsePolicy.FLOW_MAX_MOVES==3&&PowerUsePolicy.SHIELD_CHARGES==3&&PowerUsePolicy.TRAIL_STEPS==5,"Established effect quantities remain stable");
    }
    private static void testPowerCommitReconciliation() {
        for(boolean memoryApplied:new boolean[]{false,true})for(boolean completion:new boolean[]{false,true}) {
            TestContext context=new TestContext();GameStore store=new GameStore(context);
            String request="effect:reconcile:"+memoryApplied+":"+completion;
            store.reserveBoosterUse("hint",request);
            context.preferences.applyMemoryThenFail=memoryApplied;context.preferences.failNextCommit=true;
            expectFailure(()->{if(completion)store.completeBoosterUse("hint",request);else store.cancelBoosterUse("hint",request);},"Failed finalization must remain recoverable through the reconciliation API");
            check(store.boosterStock("hint")==(!memoryApplied||completion?2:3),"Used receipt never reserves a second inventory unit and cancelled receipt holds none");
            context.preferences.failNextCommit=true;
            expectFailure(()->new GameStore(context).retryPowerCommit(),"Reconciliation must not report success while its flush still fails");
            new GameStore(context).retryPowerCommit();
            check(store.pendingBoosterUse()==null&&store.boosterStock("hint")== (completion?2:3),"Only reconciliation clears the failed finalization without another debit");
            context.preferences.simulateProcessRestart();
            GameStore restored=new GameStore(context);restored.retryPowerCommit();
            check(restored.pendingBoosterUse()==null&&restored.boosterStock("hint")== (completion?2:3),"Reconciliation alone persists exact stock through simulated process reload");
            context.preferences.failNextCommit=true;restored.retryPowerCommit();
            check(context.preferences.failNextCommit,"Successful reconciliation clears the memory retry and later calls perform no write");
            context.preferences.failNextCommit=false;
        }
    }
    private static void testAvatarGearModelAndHistory() {
        check(Catalog.LEVEL_COUNT==1000&&Catalog.REMIX_COUNT==240,"Journey expands to1000 while Remix stays240");
        check(AvatarGearCatalog.ITEMS.size()==18,"Exactly eighteen deterministic avatar items");
        java.util.Set<String> ids=new java.util.HashSet<>();int[] counts=new int[3];
        for(AvatarGearCatalog.Item item:AvatarGearCatalog.ITEMS) {
            check(ids.add(item.id)&&item.id.matches("[a-z_]+"),"Stable unique equipment ID "+item.id);counts[item.slot.ordinal()]++;
            check(item.winBonusCoins>=0&&item.winBonusCoins<=3&&item.packDiscountCoins>=0&&item.packDiscountCoins<=5&&item.blockedGrace>=0&&item.blockedGrace<=1,"Per-item effects bounded "+item.id);
            check((item.source==AvatarGearCatalog.Source.EARNED_COINS)==(item.priceCoins>0),"Source explicitly separates soft-shop purchase from earned unlock "+item.id);
            for(String lang:new String[]{"en","fr","de","es","pt","it"}) {
                check(!GearStrings.get(lang,item.nameKey).equals(item.nameKey),"Localized original name "+lang+":"+item.id);
                check(!GearStrings.sourceCaption(lang,item,new AvatarGearCatalog.Progress(1000,7,30)).isBlank(),"Localized exact source "+lang+":"+item.id);
                check(!GearStrings.effectCaption(lang,item).isBlank(),"Localized exact effect "+lang+":"+item.id);
            }
        }
        check(counts[0]==6&&counts[1]==6&&counts[2]==6,"Six choices for each of three mutually exclusive slots");
        AvatarLoadout invalid=new AvatarLoadout("moon_seed","missing","moonbud");check(invalid.headId.equals("moonbud")&&invalid.auraId.equals("firefly_ring")&&invalid.charmId.equals("pebble_heart"),"Wrong-slot and unknown loadouts fall back to starter equipment");
        AvatarLoadout max=new AvatarLoadout("prism_antlers","aurora_bloom","moon_seed");check(max.winBonusCoins()==3&&max.packDiscountCoins()==5&&max.blockedGracePerRun()==1,"A full loadout applies one capped benefit from each slot");
        check(max.discountedPackPrice(1)==1&&max.discountedPackPrice(5)==1&&max.discountedPackPrice(0)==0,"Discount never creates free stock or a negative price");
        TestContext context=progressed(8);GameStore store=new GameStore(context);check(!store.ownsAvatarGear("lantern_leaf"),"Earned condition awaits one explicit persistent claim");
        List<String> acquired=store.claimAvatarGearUnlocks();check(acquired.contains("lantern_leaf")&&store.ownsAvatarGear("lantern_leaf"),"Journey threshold grants deterministic ownership");
        check(store.avatarGearAcquisitionSource("lantern_leaf").equals("JOURNEY_CLEARS")&&store.claimAvatarGearUnlocks().isEmpty(),"Claim records source and repeated claims add nothing");
        check(!store.purchaseAvatarGear("prism_antlers")&&!store.equipAvatarGear("prism_antlers"),"Progress-only item cannot be bought or equipped while locked");
        check(store.equipAvatarGear("lantern_leaf")&&store.avatarLoadout().winBonusCoins()==1,"Earned headwear gives its small declared benefit");
        // Existing historical keys predate the current period; migration must not
        // depend on opening the equipment screen before midnight or Monday.
        for(int i=0;i<7;i++)context.preferences.edit().putInt("weekly_2025-02-03_"+i,3).commit();
        for(int i=0;i<3;i++)context.preferences.edit().putInt("daily_2025-02-04_"+i,3).commit();
        context.preferences.edit().putString("weekly_2025-02-03_9","bad").commit();
        AvatarGearCatalog.Progress progress=store.avatarGearProgress();check(progress.journeyClears==8&&progress.weeklyWins==7&&progress.eventStars==30,"Legacy weekly wins and daily/weekly stars count across old periods exactly once");
        store.claimAvatarGearUnlocks();check(store.ownsAvatarGear("star_bell")&&store.ownsAvatarGear("tide_anchor")&&store.ownsAvatarGear("sunset_ribbon"),"Historical progress unlocks permanent weekly/event gear");
        context.preferences.values.keySet().removeIf(key->key.startsWith("weekly_")||key.startsWith("daily_"));
        check(new GameStore(context).ownsAvatarGear("star_bell")&&new GameStore(context).equipAvatarGear("star_bell"),"Claimed ownership never expires with event rotation");
        check(store.avatarLoadout().blockedGracePerRun()==1,"Advanced charm grants one protected blocked attempt per run");
    }
    private static void testAvatarGearPurchasesAndBonus() throws InterruptedException {
        TestContext context=progressed(13);GameStore store=new GameStore(context);store.claimAvatarGearUnlocks();store.equipAvatarGear("lantern_leaf");
        context.preferences.edit().putInt("coins",5000).commit();int before=store.coins();
        check(store.purchaseAvatarGear("starfold_visor")&&store.coins()==before-600,"Soft shop charges exact earned-coin equipment price");
        check(!new GameStore(context).purchaseAvatarGear("starfold_visor")&&store.coins()==before-600,"Duplicate equipment purchase cannot debit twice across handles");
        check(store.avatarGearAcquisitionSource("starfold_visor").equals("EARNED_COINS"),"Purchased item retains explicit local acquisition source");
        store.completeLevel(14,1);int initial=store.coins();store.equipAvatarGear("starfold_visor");
        check(store.claimAvatarVictoryBonus(0,14,"")==1&&store.coins()==initial+1,"First victory snapshots the headwear then active, not a stronger later item");
        check(store.claimAvatarVictoryBonus(0,14,"")==0,"Repeated victory bonus claim pays once");
        store.completeLevel(14,3);check(store.claimAvatarVictoryBonus(0,14,"")==0,"Star improvements and replays cannot farm avatar coins");
        check(store.claimAvatarVictoryBonus(0,1,"")==0,"Old first win with starter headwear cannot gain a retroactive bonus");
        String day=store.dailyId(),week=store.weeklyId();store.completeDaily(day,0,3);store.completeWeekly(week,0,3);store.completeRemix(1,3);
        check(store.claimAvatarVictoryBonus(1,0,day)==2&&store.claimAvatarVictoryBonus(2,0,week)==2&&store.claimAvatarVictoryBonus(3,1,"")==2,"Headwear supports each first Journey/Remix/daily/weekly victory with a distinct stable identity");
        check(store.claimAvatarVictoryBonus(4,1,"")==0&&store.claimAvatarVictoryBonus(0,1001,"")==0,"Friend challenges and invalid indexes cannot claim bonus coins");
        int fullPrice=store.boosterPrice("hint");check(store.purchaseAvatarGear("comet_halo")&&store.equipAvatarGear("comet_halo"),"An aura can be bought with earned currency and equipped");
        check(store.boosterPrice("hint")==fullPrice-4,"Aura discount is applied by central boosterPrice only");
        int oldCoins=store.coins(),oldStock=store.boosterStock("hint"),price=store.boosterPrice("hint");
        check(store.purchaseBooster("hint")&&store.coins()==oldCoins-price&&store.boosterStock("hint")==oldStock+3,"Explicit power-pack purchase spends the displayed discounted price");
        check(store.avatarLoadout().headId.equals("starfold_visor")&&new GameStore(context).avatarLoadout().auraId.equals("comet_halo"),"Changing one slot preserves and persists other slots");
        check(store.purchaseAvatarGear("moon_seed")&&store.equipAvatarGear("moon_seed")&&store.avatarLoadout().blockedGracePerRun()==1,"Owning multiple charms cannot stack blocked-attempt protection");
        store.completeLevel(15,3);AtomicInteger sum=new AtomicInteger();CountDownLatch ready=new CountDownLatch(1);List<Thread> callers=new ArrayList<>();
        for(int i=0;i<12;i++){Thread t=new Thread(()->{try{ready.await();sum.addAndGet(new GameStore(context).claimAvatarVictoryBonus(0,15,""));}catch(InterruptedException e){throw new AssertionError(e);}});callers.add(t);t.start();}
        ready.countDown();for(Thread t:callers)t.join();check(sum.get()==2,"Twelve independent bonus callbacks pay the first-win reward once");
        check(!store.equipAvatarGear("missing")&&!store.purchaseAvatarGear("missing"),"Unknown gear cannot change equipment or wallet");
    }
    private static void testAvatarGearPersistenceFailures() {
        TestContext context=new TestContext();GameStore store=new GameStore(context);context.preferences.edit().putInt("coins",2000).commit();
        context.preferences.applyMemoryThenFail=true;context.preferences.failNextCommit=true;
        expectFailure(()->store.purchaseAvatarGear("starfold_visor"),"Memory-only gear purchase is not reported as durable success");
        check(store.coins()==1400&&store.ownsAvatarGear("starfold_visor"),"Purchase failure fixture has one in-memory debit and ownership");
        check(!new GameStore(context).purchaseAvatarGear("starfold_visor")&&store.coins()==1400,"Purchase retry flushes the original ownership without another debit");
        context.preferences.simulateProcessRestart();check(new GameStore(context).ownsAvatarGear("starfold_visor")&&new GameStore(context).coins()==1400,"Equipment purchase and debit reload together");
        store.equipAvatarGear("starfold_visor");store.completeLevel(1,3);int before=store.coins();context.preferences.failNextCommit=true;
        expectFailure(()->store.claimAvatarVictoryBonus(0,1,""),"Failed avatar bonus commit must be retried");
        check(new GameStore(context).claimAvatarVictoryBonus(0,1,"")==0&&store.coins()==before+2,"Memory-applied avatar bonus retry cannot credit twice");
        context.preferences.simulateProcessRestart();check(new GameStore(context).coins()==before+2&&new GameStore(context).claimAvatarVictoryBonus(0,1,"")==0,"Bonus stays claimed after process reload");
        TestContext earned=progressed(8);GameStore rewards=new GameStore(earned);earned.preferences.applyMemoryThenFail=true;earned.preferences.failNextCommit=true;
        expectFailure(rewards::claimAvatarGearUnlocks,"Failed unlock persistence is visible to the caller");
        rewards.claimAvatarGearUnlocks();earned.preferences.simulateProcessRestart();check(new GameStore(earned).ownsAvatarGear("lantern_leaf"),"Unlock retry flushes historical earned ownership even after memory-before-disk failure");
    }
    public static void main(String[] args) throws InterruptedException {
        testCurve(); testProgressAndPurchases(); testGold(); testConcurrentCallbacks(); testMemoryBeforeDiskFailure();
        testContinuationDebits(); testPowerUnlocks(); testPowerRewards(); testMilestoneLoot(); testConcurrentPowerUseAndPurchases();
        testPowerEffectReservations(); testPowerReservationPersistenceFailures(); testPowerInputPolicy(); testPowerCommitReconciliation();
        testAvatarGearModelAndHistory(); testAvatarGearPurchasesAndBonus(); testAvatarGearPersistenceFailures();
        System.out.println("PASS " + checks + " progression-price, power milestones/rewards/loot, continuation debits and atomic test-gold checks");
        System.out.println("SANDBOX ONLY: no live payment, Play verification, ad completion or revenue is claimed by these JVM checks");
        System.out.println("DURABILITY MODEL: memory-before-disk failures and process reload are simulated; no physical storage fault was injected");
    }
}
