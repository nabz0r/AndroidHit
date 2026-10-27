# Action budgets, optional continuation, and six powers

> Current 1.5: new runs use fixed board-specific N+2…4 allowances, G/H layouts (1,000 Journey + 240 Remix), explicit power confirmation and durable effect recovery. Advanced charms protect at most one additional blocked attempt per run. Existing policy-1 attempts keep the historical allowances below until finished/abandoned. [Current rules and acceptance](RELEASE_1_5.md) · [Measured maps](data/progression-v2-summary.md).


This is the implemented **Lab 1.3** contract. The [research note](MOVE_LIMITS_RESEARCH.md) records the official game and Google references checked before implementation. The original 1.2 APK and its historical evidence remain separate. Automated logic results below do not claim human enjoyment, retention, revenue, or live payment validation.

## What an action means

A board with **N pieces needs exactly N successful exits**: each exit removes one piece, and the certified solution achieves that lower bound. A successful-exit-only allowance would therefore add no decision cost. The action budget instead consumes:

| Action | Allowance cost |
| --- | ---: |
| Attempt a piece still on the board, successful or blocked | 1 |
| Successful undo | 1 |
| Blocked attempt protected by Shield | 0; consumes one Shield charge |
| Empty tap, missing piece, undo with no history, selection or focus | 0 |
| Help, pause, background time, Spark, Vision, Focus, Trail | 0 additional actions |
| Each actual Flow exit | 1; no additional charge for the Flow button |
| Restart | Free; starts a new run with its original allowance |

An undo restores a piece, so removing it again costs another action. For an ordinary completed run without protected blocks, the total is **N + blocked attempts + 2 × successful undos**. The allowance is based on the board and its progression position; it does not depend on payment history, advertising choices, losses, session length, or a player profile.

## Initial allowances

| Mode / progression | Initial allowance |
| --- | --- |
| Journey 1–3 | N + 8 |
| Journey 4–6 | N + 6 |
| Journey 7–30 | N + 5 |
| Journey 31–90 | N + 4 |
| Journey 91–240 | N + 3 |
| Remix 1–3 | N + 6 |
| Remix 4–12 | N + 5 |
| Remix 13–60 | N + 4 |
| Remix 61–240 | N + 3 |
| Daily | N + 5 |
| Weekly | N + 4 |
| Friend challenge | Unlimited; no ad/gold continuation |

Lab progression uses the [frozen balanced order](DIFFICULTY.md); the level number used for the allowance is its position in that order. Original C/R challenge layouts retain their original identities; friend practice stays unlimited on either family. A win on the final available action is a win. The controller checks solved state before displaying exhaustion.

## Continuing an exhausted run

Free Restart remains available. An optional continuation costs **100 gold** or requires the advertised earned-reward callback. Both paths share a limit of **two continuations per run**. There is no failure-based price increase.

Each continuation grants:

```text
max(5, remaining pieces + 3) additional actions
```

The offer can therefore cover every remaining exit plus three extra actions on a position that is still solvable. It does not repair an unsafe gate order, guarantee a win, or protect against subsequent mistakes. Undo or free restart remains necessary when the current board state has no completion.

`MoveBudget.copy()` prepares the extension separately. The controller binds any callback to the saved board/run/extension request and applies a candidate budget only after the corresponding reward/debit and session state have been persisted. Opening, closing, loading, or failing an ad is not an earned reward. Consent, age and explicit advertising choice remain independent requirements; Google sign-in is unrelated to ad consent. The Lab continues to use test advertising and simulated purchase configuration.

### Gold debit API

`GameStore.spendGoldOnce(requestId, 100)` returns 100 for a new debit, or 0 for a duplicate/refusal. `hasGoldSpent(requestId)` distinguishes a recorded duplicate from insufficient funds or invalid input. Amounts other than exactly 100 are rejected, as are invalid IDs. The same marker cannot debit twice, even if the remaining wallet is below 100.

The debit and a SHA-256 request marker are written together in one preferences commit, under the same process-wide lock as gold credits. Android can update its in-memory preferences before a failed disk commit; therefore the duplicate branch forces another durability commit before success. A caller must not treat `hasGoldSpent()` alone as durable fulfillment after a persistence error. The session controller retains its pending request so an interrupted continuation can be retried. This is a local sandbox ledger, not a verified payment service or server-authoritative wallet.

## Six progressively unlocked powers

Unlocks refer to reaching the indicated Journey level. Legacy saved inventory remains intact while an item is locked.

| Power | Unlock level | Pack base price | Pack size | Effect |
| --- | ---: | ---: | ---: | --- |
| Spark (`hint`) | 1 | 45 | 3 | Reveal a safe next exit |
| Vision | 5 | 40 | 3 | Highlight pieces that can currently exit for 8 seconds |
| Flow | 12 | 60 | 2 | Perform up to 3 safe exits; each spends an action |
| Shield | 25 | 70 | 2 | Protect the next 3 blocked attempts from spending actions |
| Focus | 45 | 80 | 2 | Highlight all safe moves for up to 10 seconds or until the next board change |
| Trail | 75 | 90 | 2 | Number up to the first 5 moves of one complete solution for up to 12 seconds or until the next board change |

A successful exit or Undo changes the board and clears Focus/Trail; a blocked attempt does not. Their expiry timers are visual previews, not puzzle time limits.

Pack prices use the existing public progression-only logarithmic curve. No power adds hidden allowance or changes the continuation formula. Inventory is capped at **999 units per power**. Purchases and use are rejected before unlock; a saved Vision/Flow starter stock cannot bypass that rule.

### Earned shop reward

`grantBoosterOnce(powerId, requestId)` grants exactly **one unit**, rather than a shop pack. It rechecks known ID, unlock, stock capacity, and request uniqueness. A request is bound globally to its first power, so a duplicated show ID cannot reward a different power. Full stock rejects without consuming the request ID. Marker and stock share one commit; duplicate retries flush memory-before-disk failures without awarding another unit.

`hasBoosterGranted(powerId, requestId)` observes the recorded target. As with gold, a grant call must first return without a persistence error before that observation is treated as durable. The ad controller must also verify its own placement, earned callback, current consent and active target; this data method does not certify an ad completion.

### Free milestone loot

Every **five distinct Journey clears** earns one free power unit. Daily, weekly, Remix, star improvements, and replays do not advance this count. Claims catch up oldest milestone first; the final milestone follows the Journey catalog size.

The reward is deterministic. Build the catalog-order list of powers currently unlocked, then choose:

```text
((milestone / 5) - 1) modulo unlocked power count
```

This is earned loot, without a paid draw or random rarity. A player claiming late may have a larger eligible set than one claiming immediately. Full stock for the selected power leaves the milestone unclaimed. `nextLootMilestone()` reports the earliest pending/unclaimed threshold, or 0 when every catalog milestone is collected.

Stock and milestone marker share one commit. Because the no-argument claim method derives its next request internally, an in-process retry remembers the failed milestone through a weak preferences-keyed retry record. A memory-before-disk failure cannot silently advance to the next milestone; retry flushes the original unit and returns its ID without incrementing stock again. Independent GameStore handles in the same process share that retry state. Actual process death reloads the durable preferences ledger.

## Saved-budget validation

`MoveBudget.restore(mode, level, used, extensions, extraActions, boardMoves, undos)` returns a validated candidate or null. The session controller separately validates the board's replayed history, content version, policy version and run identity.

The pure policy rejects negative fields, unsupported mode/level pairs, more than two extensions, extra actions without an extension, extension totals outside their possible range, use beyond the allowance, and a ledger with `used < boardMoves + 2 × undos`. Arithmetic uses a wider intermediate for the undo lower bound, so corrupt integer extremes cannot wrap into a valid save. Unlimited friend snapshots have zero budget/extension counters.

These checks detect invalid state combinations; they do not make editable local preferences an anti-cheat or payment authority.

## Verified logic evidence

| Suite | Actual result | Coverage |
| --- | --- | --- |
| [Move budget](validation/v1.3/move-budget-tests.txt) | 340,768 passing checks | 1,962 certified real boards: original 240 Journey + 240 Remix, balanced 240 + 240, and 1,002 event seeds; 100,000 corrupt saves, 20,000 valid snapshots, 100,000 unlimited friend actions; blocked/undo costs, exact-zero win, copy isolation, extension floor/quota, restart and overflow |
| [Economy](validation/v1.3/economy-tests.txt) | 4,586 passing checks | Public prices, six unlocks, preserved locked inventory, one-unit ad rewards, five-clear deterministic loot, 100-gold debits, duplicate requests, stock/full-wallet boundaries, simulated memory-before-disk failures and concurrent independent store handles |
| [Data/localization](validation/v1.3/data-tests.txt) | 7,941 passing checks | Existing campaign/Remix/event economy, milestones, cosmetics and language data |

```sh
./scripts/test-move-budget.sh
./scripts/test-economy.sh
./scripts/test-data.sh
```

The preferences fault adapter models memory and durable maps separately. No physical device storage fault, live payment, ad completion, human difficulty or revenue is inferred from these JVM suites. Installed-APK and actual SDK evidence belong in the release validation report.

## Installed final 1.3 evidence

The [final signed APK run](validation/v1.3/move-power-ui-smoke.txt) passed the recorded action/exhaustion/recreation, two exact 100-gold continuations, duplicate/cap, free restart, unlimited friend, unlock, six-power, pack and free-loot cases. Two actual Google sample ads then granted +10 actions and Spark ×1, with zero added gold or cross-purpose reward; a recreated process did not repeat the continuation. The [local journal](validation/v1.3/ad-journal-export.json) contains two test impressions/earned events and two paid diagnostics at USD 0 micros / UNKNOWN, not revenue.

The fixture deliberately seeded Journey-75 access, 10,000 test gold, four units of each power and five clears for the loot case. It is not earned progression or a 75-board playthrough. All six original game-owned preference files were restored byte for byte, and the installed APK independently matched the final delivered SHA-256. [Profile and restoration hashes](validation/v1.3/move-power-profile.json). No physical-device quality, player retention, live payment or production ad result follows from this run.

A separate [non-fixture exact-zero check](validation/v1.3/exact-zero-ui.txt) played Journey 1 on the existing save: four exit/Undo cycles plus five certified exits spent all 13 actions, produced a normal two-star victory and awarded 45 coins/30 XP. No fixture/save edit, ad or purchase was used. This result is separate from the seeded milestone/power/SDK profile.
