# Productive budgets, refundable Undo and six powers

> Historical rules for the retained 1.8–1.9 releases. The current 1.14 candidate uses one Return per attempt, free blocked tries and a real action limit; see [1.14 gameplay](GAMEPLAY_1_14.md) and [1.14 qualification](validation/v1.14/README.md). Statements below about default routes, powers or policy3 describe those historical releases.

## Current 1.8 accounting

Fresh attempts use **policy 3**, including new attempts on preserved content. Default L/M and T boards support manual gate openings. Their allowance is the **exact minimum productive exits/openings plus a fixed margin**. No power or purchase is needed to finish an authored initial board. [V4 specification](PROGRESSION_V4.md).

| Action | Policy-3 cost |
|---|---:|
| Successful piece exit | 1 |
| Confirmed opening of a closed gate | 1 |
| Present but blocked piece attempt | 1 unless protected |
| Successful Undo of an exit/opening | 0; refunds the original action |
| Shield-protected blocked attempt | 0; consumes one protection charge |
| Empty tap, missing piece, empty Undo, already open gate | 0 |
| Inspect/cancel a preview, help, menus, pause/background time | 0 |
| Spark, Vision, Focus or Trail activation | No additional actions; one confirmed stock unit |
| Each actual Flow exit/opening | 1; activation has no extra action cost |
| Restart | Free; creates a new reversible attempt |

Undo restores exact board/gate/key state and refunds the productive action even at zero. It cannot refund an empty history, unrelated blocked taps, previously used powers or continuation currency. Undo alone does not lower the current star result. Blocked attempts may still exhaust a margin, so free restart remains available.

All reached V4 states remain logically completable, but not necessarily within the current remaining allowance. Guidance distinguishes a fitting exact plan, insufficient actions and a historical gate-order dead end. The controller checks victory before exhaustion, including on the final action.

## Initial allowances and guidance

| Range/mode | Fixed allowance |
|---|---|
| L/M 1–10 | Exact minimum +5 |
| L/M 11–20 | Exact minimum +4 |
| L/M 21 onward | Exact minimum +2 to +4, frozen per board |
| New daily/weekly T | Exact minimum plus a margin from fixed event characteristics |
| Shared challenge/P practice | Unlimited; no continuation route |

L/M productive minima range from **5–28**, initial allowances **10–31**. [All values](validation/v1.8/engine/strategy-authoring.csv). Old content retains its geometry; a new reversible attempt on C/R/B/Q/G/H/J/K still needs N exits because manual gates are not retrofitted to those rules.

`Level.minimumActions` is the initial optimum. `Guidance.actionsToFinish` is the exact remaining productive cost, which can exceed remaining pieces. The UI searches an isolated typed-history snapshot in a single background worker and discards obsolete generations. Pending guidance shows a thinking state and cannot reserve a power. Host timings are not Android frame-time guarantees.

## Optional continuation

Refundable Undo/free restart are the recovery choices. A continuation costs **100 earned LumaCoins** or the specifically announced earned-ad callback. Both share a maximum of **two per run**. Purchased LumaGold cannot authorize this local debit.

For exact remaining productive cost R, policy 3 adds:

```text
max(5, R + 3) actions
```

R includes required gate operations. The offer waits for valid guidance; a historical dead route is offered repair/restart rather than a claim that extra actions alone fix its gate order. Further inefficient/blocked actions can still lose the extended attempt.

The controller saves a run-bound request, prepares a copied budget, and applies it only after matching debit/reward and session persistence. An ad's load/cancel/failure is not an earned reward; late callbacks cannot move to another attempt. Refundable Undo does not reset the extension quota or reimburse its currency.

The legacy-named `GameStore.spendGoldOnce(requestId, 100)` spends **local earned LumaCoins**. Debit/request marker share a commit; duplicate retries flush persistence without charging twice. This is not an account-wallet authorization or Play receipt. Age/consent and sample-ad/checkout gates retain their [separate contract](MONETIZATION_SETUP.md).

## Six powers

Unlocks refer to the unlocked Journey level; retained old stock cannot bypass a milestone. Base prices are LumaCoins before progression bands or disclosed equipment discounts.

| Power | Unlock | Base pack | Current effect |
|---|---:|---|---|
| Spark (`hint`) | 1 | 3 for 45 | Show one exact next action for up to 8 seconds, including a required opening |
| Vision | 5 | 3 for 40 | Highlight immediately legal piece exits for 8 seconds; no optimal-order verdict |
| Flow | 12 | 2 for 60 | Perform up to 3 exact-plan productive actions, including openings; each costs one action |
| Shield | 25 | 2 for 70 | Protect the next 3 blocked piece attempts from spending actions |
| Focus | 45 | 2 for 80 | On V4, highlight one efficient next action for up to 10 seconds or until board change |
| Trail | 75 | 2 for 90 | Number up to 5 typed solution actions for up to 12 seconds or until board change |

Legacy Focus retains its all-safe-exits display. V4's current implementation highlights the first optimal action, not every equally optimal alternative. Gate instructions use **64–67**, not tile coordinates. Hint rendering, Trail and Flow distinguish them from exits. Vision remains a view of legal piece exits, not manual gate controls.

A successful exit, opening or Undo clears Focus/Trail; a blocked attempt does not change geometry. Flow requires enough actions for its at-most-three-action sequence before stock reservation. Pause/help/background freeze visual-effect duration; Shield charges and remaining Flow actions persist. Flow resumes from fresh generation-checked guidance without a second stock debit.

## One-charge activation and persistence

A stocked power opens an explicit effect/stock confirmation. Cancel consumes nothing. Already-active duplicates, overlapping activation/Flow and input inside the 450 ms debounce are rejected. Guidance must be ready and fit the allowance before reservation.

Activation reserves one unit, saves effect/session/request, then completes the stock receipt once. Failure before the effect is saved restores typed history, budget and prior effects before cancelling the reservation. A saved effect whose inventory commit fails blocks further actions behind retry/reconciliation. Recreated sessions distinguish a saved activation from an unused reservation. These source contracts do not claim installed storage-failure acceptance.

Powers and continuations are disabled in unlimited shared practice, including P boards. Free tutorial hint/Undo is separate from campaign inventory.

## Earned inventory and prices

Every five distinct Journey clears earns one deterministic power unit from the unlocked pool. Oldest eligible milestones are claimed once; replay, Remix and events do not advance them. A selected full-stock reward remains unclaimed. Stock caps at 999 units per power. A rewarded power placement grants **one selected unlocked unit**, not its whole pack or a currency reward.

The public price curve is `floor(log2(1 + progress / 30))`, with thresholds 30/90/210/450 and cap band 4. Each band adds 20% of base, rounded up to five coins with a twice-base ceiling. The input clamps at 480 despite the expanded route. Only distinct Journey/Remix clears advance it; failures, purchases, time, events and ads do not. Equipment applies only its [disclosed bounded discount](AVATAR_EQUIPMENT.md).

## Saved policies

| Attempt | Budget/Undo restoration |
|---|---|
| New attempt or restart | `startReversible`; policy 3, free/refunding Undo |
| Valid saved policy 2 | Restore old board/ledger, migrate with `toReversible`, save policy 3 immediately |
| Valid saved policy 1 | Keep historical allowance and charged Undo until finished/restarted |
| Unlimited shared practice | Zero budget/extension ledger; typed gates only on V4 |

Policy-2 migration preserves base and extension totals, subtracts `2 × prior successful Undos`, and keeps blocked-attempt costs. Unlimited snapshots migrate without subtracting nonexistent charges. Policy 1 is **not silently upgraded**; restarting freely creates a reversible attempt.

`restoreVersion` validates policy, mode/content, counters, allowance and extension bounds. Policy 1/2 retains `used >= boardActions + 2 × undos`; policy 3 uses `used >= boardActions`. The controller separately validates typed replay, family and run identity. Local consistency is not payment authority.

[Current exhaustive evidence](validation/v1.8/engine/README.md) covers all 1,240 boards, exact costs, transitions, previews, typed saves and zero-action refunds. [33,611 targeted contracts](validation/v1.8/engine/strategy-contracts.txt) include migration/malformed state. [390,327 legacy budget checks](validation/v1.8/engine/legacy-budget.txt) retain their original policy semantics.

```sh
./scripts/test-strategy-v4.sh
./scripts/test-move-budget.sh
```

JVM results do not qualify Android power presentation, actual ad callbacks, physical storage failures, real payments or human difficulty. Earlier installed results remain scoped to [1.7](RELEASE_1_7.md), [1.6](VALIDATION_1_6.md) and [1.3](VALIDATION_1_3.md).
