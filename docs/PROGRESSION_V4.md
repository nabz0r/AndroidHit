# V4 strategy progression — productive actions and reversible play

This is the **1.8 source implementation**, separate from the preserved 1.7 Android packages and their real-account qualification. Android integration, signed-package acceptance and human difficulty testing have their own records. No hit, retention, revenue or successful real payment is inferred from these checks.

## What changed

The new route has **1,000 L Journey boards and 240 M Remix boards**. An exit costs one action; **opening a closed directional gate costs one action**. Gate switches still XOR their advertised borders after an exit. A gate can be opened again when a later switch closes it. Opening an already open gate is rejected and must not be charged by the controller.

The initial allowance is **the exact minimum productive action count plus a fixed margin**, rather than the old tile count plus mistakes. Across the 1,240 authored boards, minimum productive costs range from 5 to 28 actions and initial allowances from 10 to 31. The first ten boards of each route teach spatial reading and keys without requiring a gate operation. All remaining **1,220 boards require between one and eight manual gate openings in an optimal completion**.

There are **591 boards with keys/seals**, 728 distinct board-key associations and 1,367 sealed cells. Keys are pieces: exiting a key dissolves its linked sealed cells, and Undo restores them. Beyond the introductory lessons, **8,830 switch masks were reauthored**. Geometry comes from preserved, validated layouts, while the new version separately freezes gate states, switch targets, seals, productive costs, typed certificates and comparative labels. These are new rules/content identities; no old shared board is reinterpreted.

**1,161 boards contain a decision on their certified optimal route where another immediately productive action leads to a higher exact completion cost.** This is a concrete planning distinction; it does not mean people will find every choice interesting. The other boards include teaching, forced-order and recovery cases. More tiles and lower margins alone are not treated as evidence of strategic depth.

## Teaching and comparative labels

| Route range | Introduced content |
|---|---|
| 1–5 | Directional exits, then visible path blockers; no switches or seals |
| 6–10 | Keys and linked seals, with fixed open gates |
| 11–13 | One necessary manual gate opening; switches introduced gradually |
| 14–16 | Two necessary openings where available, plus a small number of switches |
| 17–20 | Up to three necessary openings and reversible gate combinations |
| 21 onward | Reauthored switches, variable productive costs, keys/seals and combined dependencies |

The offline authoring report contains the exact values for every board: [strategy-authoring.csv](validation/v1.8/engine/strategy-authoring.csv). Opening counts are minima over all valid orders, not just a decorative certificate length.

For each step of one optimal action certificate, the authoring tool evaluates other legal exits and closed-gate openings that immediately enable a spatially free piece. It records:

- `meaningful_decisions`: steps with multiple productive choices and at least one more expensive completion;
- `forced_optimal_steps`: steps with exactly one optimal next productive action;
- `max_one_choice_cost_penalty`: largest extra optimal cost caused by one alternative at a certificate prefix;
- `dependency_depth`: longest tile/key dependency chain;
- `structural_score`: rounded `100 × meaningful_decisions / minimum_actions`, plus `3 × required_gate_opens + 2 × keys + 2 × max_one_choice_cost_penalty + dependency_depth`.

After the first 20 teaching boards, score below 80 is Normal, 80–109 is Hard and 110 or more is Expert. The current distribution is **644 Normal, 551 Hard and 45 Expert**. The first 20 remain explicitly identified as teaching boards, rather than being presented as human-calibrated peaks. Mechanic introductions are explicit; a recovery beat requires a measured structural score below 80% of its predecessor, while other Hard/Expert boards are peaks. Labels and beats are frozen in the metadata; they no longer come solely from a level-number pattern.

These are **technical comparative labels**, not measured human difficulty or a Candy Crush retention curve. The coefficient choices and margins remain design decisions. First-session comprehension, enjoyment, voluntary replay and a fair difficulty curve still require observed players and physical-device checks.

## Actions, previews and hints

`Board` preserves ordinary tile IDs and adds encoded gate actions:

```java
int action = Board.gateAction(Direction.UP); // 64 + direction ordinal: 64..67
boolean gate = Board.isGateAction(action);
Direction direction = Board.gateDirection(action);
Board.ActionPreview preview = board.previewAction(action);
Board.MoveResult result = board.performAction(action);
```

Tile exits remain IDs 0–48 and use the existing `move(tileId)` API. `performAction` accepts either kind. A successful gate action has `MoveResult.tile == null`; consumers must not create a tile flight or read tile coordinates for it. `previewAction` returns `action`, `legal`, `reason`, `previousGateMask`, `gateMask`, `openedMask`, `closedMask`, `blockingTileId` and `blockingBarrier`.

**The preview describes immediate mechanics only.** It does not call the planner or expose a hidden solvability verdict. The controller should preview a switch's opening/closing consequences and charge only a committed action. It should not automatically reject every nonoptimal choice; that would turn tapping into a correctness oracle.

On V4 boards, `solve()` returns a shortest **typed action plan**, and `hint()` returns its first encoded action, including 64–67 when a gate operation is needed. An empty completed plan returns hint −1. Trail, Flow, highlighted hints, accessibility actions and tutorials must all distinguish a gate action from a tile ID. `Guidance.actionsToFinish` is the exact productive minimum from the current state, and can exceed `remainingCount()`. Insufficient actions are distinct from a logical loss.

`history()` and atomic `restoreHistory()` include gate actions. `moves()` counts committed productive actions; `remainingCount()` still counts pieces. The maximum valid history is bounded by exits, four initial openings, and the number of gate bits closed by switches. V4 certificates are checked for actual legal transitions and completion when a level is loaded. Their optimality is independently verified offline.

## Undo and save compatibility

V4 uses **budget policy 3**. A real Undo is free and refunds its original productive action, including a gate opening. It remains usable at zero actions. The controller must first Undo one actual history entry, then call `MoveBudget.refundAction()`, and persist the board/budget together. It must not refund an empty history or charge a second action for Undo. Unrelated previously charged blocked taps are not fabricated into successful moves or refunded implicitly.

Relevant APIs:

```java
MoveBudget.start(mode, level);           // V4 -> policy 3; legacy -> policy 2
MoveBudget.startReversible(mode, level); // new reversible attempt on preserved content
MoveBudget.restoreVersion(policy, mode, level,
    used, extensions, extraActions, boardActionCount, undos);
MoveBudget.toReversible(mode, legacyLevel,
    oldUsed, extensions, extraActions, boardActionCount, oldUndos);
```

`POLICY_VERSION` remains 2 for historical callers; `REVERSIBLE_POLICY_VERSION` is 3. `restoreVersion` explicitly routes policies 1, 2 and 3. V4 levels reject policy-1/2 restoration. A policy-2 migration preserves its original base/extension allowance and refunds the historical `2 × oldUndos` overhead; blocked-attempt charges remain. Unlimited friend challenges migrate with a zero ledger. The migrated policy-3 snapshot must be saved immediately. Rare policy-1 attempts retain their old validation and allowance rather than silently changing their accounting.

The old `Board.guidance(remaining)` repair formula retains policy-1/2 paid-Undo semantics for preserved boards. A controller using policy 3 must allow genuine Undo even when that historical `recoveryFitsBudget` value is false, including at zero. Logical repair and previously exhausted blocked-attempt margins are separate; free restart remains available.

The budget extension argument represents the required remaining productive actions on V4. The controller should use the exact guidance cost, rather than the number of remaining tiles. Extensions remain quota-limited and separately gated; no ad or purchase is required to make an initially authored board finishable or to undo a hidden gate mistake.

## Content identities and events

| Family | Lookup | Contract |
|---|---|---|
| C/R, B/Q, G/H, P | Existing catalogues | Preserved rules, geometry, seeds, certificates and code checksums |
| J/K | `BalancedCatalog.get/remix` | Preserved V3 route, including historical gate traps |
| L/M | `StrategyCatalog.get/remix` | New frozen V4 strategy route |
| E | `LevelCatalog.event(seed)` | Preserved historical event rules |
| T | `StrategyCatalog.event(seed)` | New V4 event rule set |

`ChallengeCode.encodeStrategy`, `encodeStrategyRemix` and `encodeStrategyEvent` emit L/M/T identities. Existing parsers retain every historical family. CRC32 detects copying errors; it is not authentication and does not carry account ownership or rewards.

The T event seed space deterministically selects among **the 980 already audited L21–L1000 geometries**. Its event identity retains the supplied seed and the chosen level's exact action certificate. Consequently every possible T event is inside the finite audited geometry closure, without pretending to enumerate every 64-bit seed. This is not a promise of a newly generated unique board for every seed. All 980 selectable geometries and extreme signed seeds are exercised; share-code values retain the existing nonnegative-long domain. E and old active event sessions remain unchanged.

## Planner bounds and measured evidence

Every V4 spatial dependency graph is acyclic. Removing a piece only removes path/key blockers, and closed gates can be opened manually. Therefore any state reached by legal productive actions remains logically completable; exhaustion can still require Undo or free restart. `hasSolution`, `isDeadEnd` and `isSafeMove` use this certified structural property without searching on every render frame.

Exact planning caches `(remainingMask, gateMask)` costs in one board-local primitive table, capped at 524,288 entries and approximately 9 MiB of steady cache arrays. No cache of 1,240 runtime planners is retained. A globally optimal certificate's current prefix gives its optimal suffix immediately. Other positions use the exact bounded planner. Temporary arrays during cache growth and Java/native/UI memory are separate from the quoted steady cache size.

Guidance computation must run on a bounded worker using an immutable history snapshot and generation/identity checks. Rendering a frame must not launch a new search. The independent full audit observed a worst individual productive-cost call around 33 ms on the development host; this is **not an Android latency guarantee**. Installed low-end behavior and UI cancellation remain distinct acceptance.

The measured exhaustive baseline is recorded in [strategy-all-states.txt](validation/v1.8/engine/strategy-all-states.txt), [per-board states](validation/v1.8/engine/strategy-all-states.csv) and [contracts](validation/v1.8/engine/strategy-contracts.txt):

- all 1,240 new boards, **13,254,058 reachable action states** and **57,403,554 legal action transitions**;
- an independent cell-ray model fills **31,230,944 gate states** by bottom-up relaxation, then compares exact productive minima to runtime;
- every reachable transition, physical preview and Undo is checked; 13,839 complete typed plans/save replays are sampled additionally;
- 1,240 unique boards under D4 transforms, translations and reordered IDs; all 980 possible event geometries; malformed histories/certificates and zero-action refunds;
- policy-2 migration, unlimited friend migration, retained old family fingerprints and separate legacy engine/budget/seal/V3 regressions.

The audit reports its exact assertions, cache peaks and execution time. These counts describe their source/model scope, not 13 million manually played Android positions. Installed tutorial, typed Flow/hints, saving, accessibility and lifecycle acceptance are reported separately by the Android validation owner.

Reproduce with JDK17:

```sh
./scripts/test-strategy-v4.sh
```

Offline metadata authoring is in [GenerateStrategyV4.java](../scripts/GenerateStrategyV4.java). It must be explicitly compiled/run when deliberately changing the new rule set; opening a map or starting a game never runs it. The frozen data is [StrategyV4Layouts.java](../app/src/main/java/com/androidhit/lumadrift/engine/StrategyV4Layouts.java). Source hashes and complete host execution records are kept in [the engine evidence directory](validation/v1.8/engine/).
