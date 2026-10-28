# v1.10 objectives and collection models

The JVM checks pass for the actual data models. This folder qualifies collection-goal metadata, objective readiness and local reward persistence. It does not qualify the native screen layout, physical storage hardware, Google sign-in, advertising, paid transactions or player retention.

## Changes and ownership

The scoped implementation changes `GameStore.java` and adds `CollectionGoal.java` and `ObjectiveDashboard.java`. It does not change prices, currencies, reward amounts, catalog thresholds, `MetaProgress`, providers or level rules. Native UI integration and release qualification are maintained separately.

Daily and weekly event completion bonuses now persist their existing receipt and balance together through `LocalSaveTransactions`. The receipt names remain `daily_bonus_<date>` and `weekly_bonus_<monday>`, with nominal rewards of 100 and 250 LumaCoins. Existing receipts remain valid. The existing saturating balance cap of 2,000,000,000 coins is preserved: a near-cap claim can credit less than its nominal returned amount, and its receipt remains consumed.

A refused commit raises `GameStore.LocalSaveException`; it is never acknowledged as a successful new grant. The exact pending transaction is retried before a later claim or receipt read. Once that original transaction becomes durable, a repeated claim returns zero rather than granting again. A claim captures its eligible period once, including when the clock crosses midnight or Monday during that call. A pending previous-period claim settles its original receipt after rollover and does not consume a receipt in the new period.

The same transaction mechanism now protects the explicit `claimAvatarGearUnlocks()` grant. Its existing `avatar_owned_v1_<id>` keys, source strings and qualification rules are unchanged. A grant never charges coins or equips an item. Unlock checks remain explicit lifecycle actions; neither presentation model calls them. A failed grant can be retried from another store handle, and a restart before a successful commit leaves the item legitimately claimable rather than partly granted on disk.

The six collection mutations `purchaseAvatarGear`, `equipAvatarGear`, `purchaseCharacter`, `purchaseSkin`, `equipCharacter` and `equipSkin` use that transaction mechanism as well. They flush an existing pending write before deciding whether an action is valid. Purchase ownership and the LumaCoin debit persist together; a retry of an already settled purchase returns `false` without another debit. Equipment changes only its own existing slot key. Catalog prices, locally and externally owned character/theme rules, insufficient-balance refusals and existing save keys remain unchanged. A save failure is typed, allowing the native UI to offer its normal save retry instead of acknowledging an undurable choice.

## Public APIs

`GameStore.trackedCollectionGoal()` returns a valid optional key, or an empty string. Keys are exactly `cosmetic:<catalog-id>` or `gear:<catalog-id>`. There are 24 cosmetic and 18 gear identities in the current catalogs. `setTrackedCollectionGoal(String)` returns `false` only for an invalid identity; a persistence refusal throws the typed save exception. `clearTrackedCollectionGoal()` explicitly clears the goal. The local key is `collection_goal_v110`.

An existing or upgraded profile starts without a chosen goal. Reaching a threshold, obtaining the item, equipping it or opening the screen does not silently change that choice. An unknown stored identity is ignored without writing a repair during rendering.

`CollectionGoal.snapshot(store)` returns an immutable snapshot, or `null` when no valid target is selected. It exposes the qualified key, raw item ID, localized name key, typed kind and slot, ownership, equipped status, actual progress and target, coin price and affordability. `completed` means **owned**, not just eligible or affordable. `route` and its alias `source` identify the primary activity (`JOURNEY`, `DAILY`, `WEEKLY`, `REWARDS`, `SHOP`). `acquisitionSource` separately reports an existing recorded acquisition source; an external owner without a local acquisition receipt retains an empty source instead of an inferred purchase.

Collection progress follows the existing acquisition rule. Login targets count claimed login gifts, not merely visited days. Weekly wins and event stars are cumulative item counters across periods. Event-star targets have a primary `DAILY` route but can also progress from weekly events. Shop goals use the actual spendable LumaCoin balance; affordability never purchases the target. Progress is clamped to its displayed target, and ownership acquired by another valid route still completes the goal.

`ObjectiveDashboard.snapshot(store, meta)` returns typed daily and weekly quests, all six power trial states, login reward information, event bonus readiness, the next Journey gift and an explicit next action. Lists are immutable. Quest indexes are zero-based; Journey levels and milestones are one-based. The model does not grant, claim, purchase, equip, record a visit or update progression.

`readyCount` counts current visible claim actions: each quest, each trial, each event completion bonus, today's login gift and **one oldest outstanding Journey gift action**. It is not a sum of coin amounts or a count of every historical unclaimed Journey gift. `dailyReadyCount` groups login, daily quests and the daily event bonus; `weeklyReadyCount` groups weekly quests and the weekly event bonus. Trial and Journey counts remain separate.

Next-action priority is login, earned daily quests, earned weekly quests, earned event bonuses, the oldest Journey gift, then earned trial samples. When none is ready, the action points to a genuinely uncleared unlocked Journey board, then an uncleared available daily or weekly event. It does not suggest a replay as new mission credit.

Readiness means earned and unclaimed. The real claim API still checks capacity, current period and durable persistence at the time of the action. Snapshot construction uses existing public getters; it is not a cross-store, period-locked database transaction. A subsequent refresh updates day/week changes. A clean snapshot creates no writes. Existing pending local transactions may be flushed by established store getters, and a refused flush remains a visible typed error.

## Reproduction and evidence

Run `bash scripts/test-objective-models-v110.sh`. An optional first argument selects the JSON output; the default points into this v1.10 folder. The runner compiles the real data classes with the established Android preference doubles and uses a temporary build directory.

`ObjectiveModelsV110Test.java` checks 443 assertions, including:

- Daily and weekly bonus writes, receipts and exactly-once payout across two memory-before-disk failure modes, consecutive refused writes, shared handles, 24 concurrent claimers, calendar boundaries and cold reconstruction before and after recovery.
- Existing bonus receipts and near-cap behavior; no amount, cap or receipt migration.
- All 42 typed target identities, invalid keys, explicit pin/clear, cold persistence, source counters, eligibility without acquisition, equipment observation and external ownership without invented provenance.
- Gear unlock failures and recovery through shared handles, disk/cache divergence, exact ownership keys and sources, no automatic equipment or coin change, and cold reconstruction before and after commit.
- Character, theme and gear purchases/equipment across both failure modes, repeated refusals, shared handles, exact debit/ownership/slot writes, cold reconstruction before and after recovery, 12 concurrent buyers per category, external character/theme ownership and invalid or unaffordable actions. Funding setup uses valid existing grants, each within the current 500-coin limit.
- Three daily and three weekly quests, six trials, real-progress readiness, immutable snapshots, no automatic grant, actual explicit claim transitions and day/week refresh.

Failure injection is modeled by JVM preference doubles that separately retain memory and committed disk values. Both a commit failure with unchanged memory and one with already-updated memory are exercised. Cold reconstruction creates a new preference instance from the committed map. These are controlled simulations of persistence behavior, not physical flash faults or a native Android process-kill test.

| Check | Assertions | Result |
| --- | ---: | --- |
| New objective and persistence model checks | 443 | PASS |
| Data, campaign, rewards and localization regression | 8,882 | PASS |
| Meta progression and journal regression | 362 | PASS; JSON/Unicode export also passes |
| Economy and power progression regression | 6,611 | PASS |
| Inventory presentation regression | 1,482 | PASS |
| Rewards, wardrobe and quest regression | 100,953 | PASS |
| Local-save regression | 229 | PASS |
| Journey rewards and grading regression | 65,736 | PASS |

All regressions in this folder were rerun after the final collection-purchase/equipment migrations. Historical v1.7/v1.9 test sources are reused unchanged; their outputs are redirected here and their historical evidence is not replaced. `source-manifest.json` records the scoped implementation and test dependency hashes. `model-tests.json`, the individual regression outputs and `regression-summary.json` retain the machine-readable or raw results.
