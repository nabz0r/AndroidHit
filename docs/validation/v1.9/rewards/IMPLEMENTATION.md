# Reward and action-grading corrections — 1.9

The [baseline](BASELINE.md) records the 1.8 behavior before these edits. Its source hashes and JSONL traces remain unchanged. The following implementation is device-local and does not alter Play purchases, premium currency, real ad configuration, catalog unlock levels or power pack prices.

## Stable five-clear gifts

`GameStore.nextMilestoneLootId()` previews the earliest unclaimed Journey gift even before the milestone is earned. The item is selected using only the milestone ordinal and powers unlocked by the level that milestone opens (`min(1000, milestone + 1)`). The same preview is used by `claimMilestoneLoot()`; collecting later no longer changes the contents.

New receipts use `power_loot_v19_<milestone>`. Existing `power_loot_v1_<milestone>` receipts remain recognized, unchanged and already claimed, including historical items that could differ from the new preview rule. Existing stock is not recalculated. A pending in-process storage retry retains its exact original milestone and item. Full physical stock leaves the milestone unclaimed and retryable.

The preview itself writes no receipt or stock. A storage-failure retry remains an explicit claim operation, not an automatic grant from screen rendering.

## Explicit free sample for each power

All six catalog powers have one sample per local profile. The sample becomes available at the unchanged Journey unlock level. Existing players may claim a sample for each already unlocked power too; their saved units are retained. Nothing is granted automatically by construction, rendering, unlocking or signing in.

APIs:

- `powerTrialAvailable(id)` reports an unlocked sample with no receipt; it remains pending when stock is full.
- `powerTrialClaimed(id)` reports its saved receipt.
- `claimPowerTrial(id)` returns `PowerTrialStatus.GRANTED`, `ALREADY_CLAIMED`, `LOCKED`, `STORAGE_FULL` or `INVALID_POWER`. A storage failure throws rather than reporting success.

The `power_trial_v19_<id>` receipt and one stock unit share one `LocalSaveTransactions` write under the common local economy lock. The capacity check counts physical stock, including any unit reserved by an unfinished effect. No clipping or receipt consumption occurs at 999 units. Retry first flushes an exact pending write, so another store handle cannot grant a second sample or overwrite a later debit.

These receipts are local to the installed profile. They are independent of Google account identity and are not a cross-device entitlement. They never grant LumaGold or imply an ad view/payment.

## Transparent stars from actions

The new `engine.JourneyExperience` owns grading policy `SCORE_VERSION = 1`. It receives only a valid `MoveBudget`; the controller calls it after an actual solved board.

```
three-star limit = minimumActions + floor(max(0, base - minimumActions) / 2)
3 stars: no extension and actions used <= three-star limit
2 stars: no extension and actions used <= initial base
1 star: any extension
unlimited friend challenge: 3 stars
```

Hints, time and raw Undo count are not grading inputs. A real free Undo refunds its productive action using the existing reversible budget, so it can restore the three-star range. Extension history still gives one star after a refund. Missing budgets are rejected with `IllegalArgumentException`. Existing stored stars are not recalculated; `GameStore.completeLevel()` keeps the best stored rating as before.

`JourneyExperience` lives in `engine` so existing data-only test compilation gains no dependency on engine sources. No historical board geometry or move-budget policy is changed.

## Verification and source boundaries

Run `bash scripts/test-journey-rewards-v19.sh`. [The dedicated report](implementation-tests.json) records **65,736 assertions**, all **1,240 Journey/Remix board grading cutoffs**, **200 immediate/deferred gift pairs**, all six samples, stock capacity with escrow, invalid/locked input, 36 concurrent mixed claims, memory-before-disk failures and cold recreation. [The log](implementation-tests.txt) records the run. The report's source hashes identify this implementation checkpoint; the root integration may subsequently extend `GameStore` with unrelated avatar style APIs and rerun the suite.

After root integrated the avatar/guardian APIs and froze the final3 application sources, the same dedicated suite passed again with **65,736 assertions**. [Final source-bound report](implementation-final-tests.json), [final log](implementation-final-tests.txt). These hashes identify the integrated source, rather than the earlier reward-only checkpoint.

[Economy regression](economy-regression.txt): **6,611 checks passed**. One old assertion in `scripts/EconomyTest.java` was updated from claim-time eligibility to milestone-time eligibility, and legacy receipt preservation was added. Existing 1.8 reports were not edited. [Other regressions](regressions.json) cover data (8,882), meta (362), rewards/cosmetics (100,953), tutorial (1,454) and inventory presentation (1,482), all passed.

These are actual production Java APIs executed on a JVM with deterministic Android preference doubles, including Android's memory-before-disk failure ordering. They verify ledger and policy behavior. They do not inject faults into physical storage, demonstrate external service operation, measure human retention, or assign behavior by age. Native rendering, touch navigation and the tutorial-to-level sequence require the root Android integration checks.

Ownership of this subtask: `data/GameStore.java`, new `engine/JourneyExperience.java`, dedicated reward tests/reports and the explicitly authorized Economy expectation update. Root owns native UI, six-language copy, build/release files, avatars, site, packaging and publication.
