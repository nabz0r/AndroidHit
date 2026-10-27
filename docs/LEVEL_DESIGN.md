# Level design, typed actions and challenge identities

## Current V4 route

The 1.8 default is **L Journey 1–1,000 and M Remix 1–240** through `StrategyCatalog`, with T events for new daily/weekly attempts. Historical J/K continue through `BalancedCatalog.get/remix`; those APIs are not aliases for V4. Source behavior and [engine evidence](validation/v1.8/engine/README.md) remain separate from installed Android acceptance.

V4 freezes gate states, reauthored switch targets, key/seal dependencies, productive minima, typed certificates, margins and comparative labels. Geometry is inherited from validated layouts; new rule interactions are separately authored and versioned. There are 591 key/seal boards, 8,830 reauthored switch masks and 1,161 boards with a measured decision affecting productive completion cost. This is not presented as 1,240 newly hand-drawn geometries. [Full specification](PROGRESSION_V4.md).

## Readable mechanics and reversible choices

A piece exits along a clear ray through an open gate. A key's exit dissolves its linked seals. A switch XORs the indicated borders **after exit**, checking its old exit gate first. Both opening and closing consequences must remain visible when several badges fire.

L/M/T add `OPEN_GATE(direction)` for one action. A gate closed by a later switch can be reopened. Preview/cancel is free; commitment is charged once. The preview describes before/after masks and immediate blockers without exposing an optimality oracle or auto-rejecting every inefficient choice.

Policy-3 Undo reverses/refunds the last exit or opening and restores key/switch state, even at zero actions. Unrelated blocked-tap charges remain. The first five boards teach exits/blockers, 6–10 keys/seals, 11–20 manual openings/switches, then combined dependencies and differently priced productive routes. The separate [tutorial](TUTORIAL.md) consumes no campaign stock and does not replace the interrupted board.

## Offline authoring and certificates

[GenerateStrategyV4](../scripts/GenerateStrategyV4.java) writes [StrategyV4Layouts](../app/src/main/java/com/androidhit/lumadrift/engine/StrategyV4Layouts.java) and [authoring metrics](validation/v1.8/engine/strategy-authoring.csv). Starting a game parses a frozen row; it does not author, sort or personalize a layout.

Rows record source identity, exact productive minimum, margin, initial gates, switch masks, seals, typed certificate, structural metrics and labels. Each added seal lies after its key in a valid spatial dependency order. Switches are reauthored for the new manual-opening rules; the old exit certificate is not claimed to prove their new cost.

`Level.minimumActions` and V4 `parMoves` are the exact initial productive cost. **`Level.actionSolution` is the authoritative V4 certificate**, including gate actions. `Level.solution` retains the old piece-exit order/search preference; by itself it is not a playable V4 plan when manual openings are required.

Certificates are checked for legal typed transitions and complete clearing on load; the independent audit proves their optimality. A runtime certificate suffix is an optimal shortcut only if current history matches that globally shortest certificate's prefix. Comparative labels use recorded choice/cost/dependency metrics and explicit teaching introductions. [Score definition and limits](DIFFICULTY.md).

## Action APIs

| Value/API | Contract |
|---|---|
| Tile IDs 0–48 | Exits; historical `move(tileId)` remains available |
| `64 + Direction.ordinal()` | Opening; use `gateAction`, `isGateAction`, `gateDirection` |
| `performAction(action)` | Exit or opening; a gate result has `tile == null` |
| `previewAction(action)` | Immediate legality/blockers and before/after gates, without a solve verdict |
| `solve()` / `hint()` | Shortest typed V4 plan / first action; −1 hint means no next action |
| `history()` / `restoreHistory()` | Ordered typed replay; malformed input leaves the old board intact |
| `moves()` | Committed productive actions, including openings |
| `remainingCount()` | Pieces, not remaining productive cost |
| `Guidance.actionsToFinish` | Exact remaining productive minimum |

Gate actions are not tile coordinates. Flow, Trail, hints, accessibility and fixtures must branch on `isGateAction`. Reopening an already open gate is rejected without history or charge. Undo of a latest manual opening closes exactly that gate.

History can exceed N: its bound includes exits, four initial openings and gate bits closed by switches. Typed history/budget are saved together. Dropping gate entries would make replayed gate state incorrect.

## Recoverability and planning

An initial certificate proves an acyclic spatial dependency order. Legal removal only deletes piece/key blockers. Gate changes cannot add spatial blockers, and every closed required border can be opened manually. From any reached subset, a spatially free piece can therefore leave after opening its gate; repeating clears the board.

This proves forward logical recoverability for all L/M/T branches, **not that every branch fits its remaining budget**. Exact planning measures inefficient choices; Undo/refund and free restart handle exhaustion. Fast V4 `hasSolution`/`isDeadEnd` use the certified structural property, while exact productive costs are computed separately.

The UI searches an isolated typed-history snapshot in a background worker and discards stale generations. Physical previews do not call the planner. Development-host measurements do not establish Android or low-end latency.

## Identity protocol

Codes retain `LD1-<family>-<base36 value>-<CRC32>`. CRC32 detects copying errors; it carries no authentication, reward or ownership proof.

| Family | Lookup | Scope |
|---|---|---|
| L/M | `StrategyCatalog.get(1..1000)` / `.remix(1..240)` | Current V4 route |
| T | `StrategyCatalog.event(long)` | Current V4 event identity |
| J/K | `BalancedCatalog.get(1..1000)` / `.remix(1..240)` | Preserved V3 |
| G/H | `BalancedCatalog.getV2(1..1000)` / `.remixV2(1..240)` | Preserved V2 |
| B/Q | `BalancedCatalog.legacyGet(1..240)` / `.legacyRemix(1..240)` | Original balanced order |
| C/R | `LevelCatalog.get(1..240)` / `.remix(1..240)` | Original campaign/Remix |
| E | `LevelCatalog.event(long)` | Historical event rules |
| P | `SealedCatalog.get(1..6)` | Historical workshop practice |

`encodeStrategy`, `encodeStrategyRemix` and `encodeStrategyEvent` emit L/M/T. Existing encoders/checksums keep their meanings. Shared codes open their actual family in unlimited, reward-free mode 4, without powers or continuations. Manual gates are not retrofitted into J/K or earlier families.

T maps deterministically to the **980 audited L21–L1000 geometries** while retaining its seed identity. Its finite geometry closure is fully audited; it does not generate a unique layout per seed. Event APIs accept signed `long`; canonical E/T codes retain the nonnegative `long` domain including `Long.MAX_VALUE`, without floating-point conversion.

## Saved-board compatibility

| UI content version | Route retained |
|---|---|
| 5 | L/M or new T |
| 4 | J/K |
| 3 | G/H |
| 2 | B/Q |

UI content version, `Level.progressionVersion`, budget policy and challenge family have separate roles. An old valid active board is restored from its original family, never swapped for a same-numbered L board. Stars, unlocks, balances and equipment retain their local recorded positions.

New attempts/restarts use `startReversible` policy 3. Saved policy 2 restores its board/ledger, migrates through `toReversible`, then saves immediately: base/extensions stay fixed, historical `2 × prior Undos` overhead is refunded, blocked-attempt costs remain. Saved policy 1 stays charged when resumed; finishing or freely restarting leaves that old attempt. [Budget validation](MOVE_BUDGET_POWERUPS.md).

## Verification and archives

[Current exhaustive evidence](validation/v1.8/engine/README.md) covers 1,240 unique boards under D4 transforms, translation and reordered IDs; 13,254,058 reachable action states; 57,403,554 transitions; independent exact minima; previews; Undo; typed saves; zero-action refunds; migration; and all 980 T geometries. Complete runtime plans are sampled in addition to exhaustive cost/transition checks.

```sh
./scripts/test-strategy-v4.sh
```

Historical methods and original evidence remain separate: [V3 J/K](PROGRESSION_V3.md), [V2 G/H](data/progression-v2-summary.md), [B/Q measurements](data/difficulty-summary.md), [P workshop](validation/v1.5/sealed-tests.txt), and [1.7 trap diagnosis](LEVEL_MANAGEMENT_REVIEW.md). Preserved release reports are not rewritten as V4 acceptance.
