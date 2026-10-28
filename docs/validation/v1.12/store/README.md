# Booster purchase durability

A focused source review found an ignored raw `SharedPreferences.Editor.commit()` result in `GameStore.purchaseBooster`. `prefs` is the raw Android preferences handle; `LocalSaveTransactions` only protects calls routed explicitly through it. It is not a transparent preference wrapper.

The [baseline observation](baseline-observation.json) executes the preserved [baseline store source](baseline-GameStore.java) with the existing JVM failure double and public purchase calls. Both modes returned success after a failed disk write. With memory updated before disk, the live state temporarily showed 135 coins and six hints, while cold recreation retained 180 coins and three hints. Calling the old explicit retry did not persist the purchase, because no pending transaction existed. This report intentionally records `product_passed: false`.

The fix changes only `purchaseBooster`: its existing lock, price, unlock and capacity checks remain; the exact coin debit and stock grant now share a tracked `LocalSaveTransactions.write`. A refused write raises the existing typed save error, and the guarded save retry persists the original pair without repeating the purchase action.

The [focused test](../../../../scripts/StoreBoosterV112Test.java) passes [1,670 checks](booster-regression.json) across all six powers. It covers ordinary durable buys, both failure models, repeated refusal, guarded reads, independent handles, exact retry, no duplicate debit/grant, process loss before retry, invalid/locked/underfunded requests and capacity. Progress and funding use public store APIs; no Android profile is seeded. Run `./scripts/test-store-booster-v112.sh /tmp/luma-v112-booster-result.json` from the repository root.

Existing regression suites also pass: [appearance 683](appearance-regression.json), [initialization 41](initialization-regression.json), [external ownership 17](externals-regression.json), [economy 6,611](economy-regression.txt), and [local save 229](local-save-regression.txt). These JVM models do not qualify physical flash, live advertising or Play payment providers.
