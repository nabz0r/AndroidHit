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
    public static void main(String[] args) throws InterruptedException {
        testCurve(); testProgressAndPurchases(); testGold(); testConcurrentCallbacks(); testMemoryBeforeDiskFailure();
        testContinuationDebits(); testPowerUnlocks(); testPowerRewards(); testMilestoneLoot(); testConcurrentPowerUseAndPurchases();
        System.out.println("PASS " + checks + " progression-price, power milestones/rewards/loot, continuation debits and atomic test-gold checks");
        System.out.println("SANDBOX ONLY: no live payment, Play verification, ad completion or revenue is claimed by these JVM checks");
        System.out.println("DURABILITY MODEL: memory-before-disk failures and process reload are simulated; no physical storage fault was injected");
    }
}
