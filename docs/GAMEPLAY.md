# Luma Drift gameplay and economy

## Current 1.8 source contract

The default route is **1,000 L Journey boards and 240 M Remix boards**, selected through `StrategyCatalog`. New daily/weekly stages use T events. Historical J/K still resolve through `BalancedCatalog`; older shared codes and valid active boards retain their original geometry. This page describes source behavior, separately from installed Android and real-service qualification. [V4 specification and evidence](PROGRESSION_V4.md).

Luma Drift is an offline directional puzzle: clear every piece, opening gates and freeing paths in an efficient order. There is no countdown, life counter, energy system or runtime AI model. Optional account, social and commerce services do not make puzzle solving server-dependent.

## Play a board

1. A piece exits along its large arrow when every intervening tile and active sealed cell is clear and that border's gate is open. Pieces do not rotate or move to another cell.
2. In L/M/T, select a **closed gate**, inspect the consequence preview and confirm to open it. The opening costs one action. Inspecting/cancelling is free; an already open gate cannot be charged as another opening.
3. Small switch badges change their indicated gates **after the piece exits**. Open becomes closed and closed becomes open. The badge direction can differ from the piece's exit arrow; the preview shows the immediate before/after consequences.
4. A key piece dissolves its linked sealed cells when it leaves. Undo restores that piece and its seals. Opening seals alone does not win: every piece must leave.
5. A successful exit or manual opening costs one action. A present but blocked piece attempt also costs one, unless Shield or equipped protection absorbs it. Empty taps, previews, help and menus cost nothing.
6. Removing the final piece wins, including on the final available action. Gate states no longer matter when the board is empty.

A preview explains mechanics without automatically rejecting every nonoptimal choice. All reached L/M/T states remain logically completable: manual openings can reopen gates, and removing pieces cannot add path blockers. A less efficient sequence can still exceed its remaining budget. [Full level-design contract](LEVEL_DESIGN.md).

## Budget and recovery

Each initial allowance is the **exact minimum productive cost plus a fixed margin**. The minimum includes necessary gate openings, not just the piece count N. Across L/M, minima range from **5–28 actions**, allowances from **10–31**. The briefing/HUD distinguishes the minimum and margin. These values do not adapt to ads, purchases, failures, time or player profiles.

Policy 3 **Undo is free and refunds its original productive action**, including a gate opening. It reverses the last committed exit/opening and its key/switch effects, even at zero actions when history exists. It does not silently refund unrelated blocked attempts, used powers or continuation currency. Undo alone does not lower the current star result. Free restart remains available if blocked taps consumed the margin.

Restart restores the same board's initial state and a new reversible allowance. A fresh attempt, including a restarted historical board, uses policy 3. A resumed policy-1 attempt keeps its historical charged Undo; policy-2 migration is described below. [Exact budget and powers](MOVE_BUDGET_POWERUPS.md).

At exhaustion, refundable Undo and free restart remain the recovery choices. Optional continuations cost **100 earned LumaCoins** or their announced earned-ad reward, with a shared maximum of two per run. They add `max(5, exact remaining productive cost + 3)` actions. Loading/cancelling an ad is not an earned reward. Purchased account LumaGold does not fund these local continuations.

## Learn and progress

| Range | Main lesson |
|---|---|
| 1–5 | Directional exits, then visible blockers |
| 6–10 | Keys and linked seals, without required gate operations |
| 11–13 | One required opening and gradual switch introduction |
| 14–20 | More required openings and reversible gate combinations |
| 21 onward | Combined switches, keys/seals and differently priced productive routes |

The separate interactive tutorial teaches exits, blockers, keys, manual gates, switch previews, Undo and hints. Its progress/replay does not replace an interrupted campaign board; its completion gift uses a one-time receipt. [Tutorial](TUTORIAL.md).

The map scrolls across Journey 1–1,000 with ten 100-level visual/audio biomes, level jump and current-node return. Normal/Hard/Expert are structural comparative labels, not calibrated human pass rates. [Difficulty measurements](DIFFICULTY.md).

A Journey clear unlocks its successor; earlier boards remain replayable. Remix unlocks after Journey 12 and has separate stars/progress. Three daily and seven weekly local-calendar stages use T for new attempts. T selects among audited L21–L1000 geometries; it does not promise a unique new geometry for every seed.

## Powers, possessions and currencies

Six powers unlock at Journey **1/5/12/25/45/75**. Spark can indicate an opening; Trail and Flow include typed gate actions. Flow performs at most three productive actions and pays their normal costs. V4 Focus highlights one efficient next action; Vision shows currently legal piece exits. Stock use requires confirmation and a saved one-charge receipt. [Effects and persistence](MOVE_BUDGET_POWERUPS.md).

Current scoring grants three stars with no power activations or continuations, two with at most two activations and one continuation, and otherwise one. Undo alone is excluded from that penalty. First Journey completion grants 25 LumaCoins plus 10 per earned star; an improved record pays 10 per new star. An unchanged replay grants no repeat victory payment. Explicit equipment and other reward receipts are separate.

Collection distinguishes 24 companions, six themes, 18 functional equipment items and six power stocks. The wardrobe adds 24 appearance-only Hat/Outfit/Shoes/Gloves designs. Wardrobe items do not change board rules, prices or power stock. Existing Head/Aura/Charm equipment retains its separately disclosed bounded benefits. [Equipment](AVATAR_EQUIPMENT.md) · [Wardrobe, daily gifts, quests and earned capsules](ECONOMY_1_7.md).

**LumaCoins are earned and local; LumaGold is a separate premium account balance.** Packs, the 150-LumaCoins earned capsule and local continuations spend LumaCoins. Premium account draws keep separate gates. Google sign-in does not create a gameplay cloud save or convert the balances. Commercial activation follows its [separate setup](MONETIZATION_SETUP.md).

## Shared practice and old attempts

Shared challenges open as **unlimited, reward-free mode 4** on their actual family. L/M/T retain typed manual openings; old codes retain old gate rules. Practice grants no coins, XP, stars, quest progress, powers or continuation offers. The six P workshop challenges remain available under practice rules.

| Saved content | Board retained | Budget behavior |
|---|---|---|
| Content 5 | L/M or T | Policy 3; free/refunding Undo |
| Content 4 | J/K | Policy 2 migrates to 3, preserving the board |
| Content 3 | G/H | Same migration, original geometry retained |
| Content 2 | B/Q | Policy 1 stays historical when resumed; restart creates a reversible attempt |
| Shared code | Its actual family | Unlimited practice; manual gates only where supported |

Policy-2 migration preserves base/extensions, subtracts historical `2 × prior Undos` overhead, retains blocked-attempt charges and immediately saves policy 3. Stars, unlocks and inventories retain their local recorded positions. [Compatibility details](LEVEL_DESIGN.md).

Historical artifacts and rules remain separately documented: [1.7](RELEASE_1_7.md), [1.6](RELEASE_1_6.md), [1.5](RELEASE_1_5.md), [1.3](RELEASE_1_3.md). Their N-plus-margin, charged-Undo and installed-test results are not relabelled as L/M/T behavior.

<a id="lab-13-action-allowance"></a>
## Historical Lab 1.3 reference

Earlier links to the Lab 1.3 allowance refer to its N-plus-margin, charged-Undo implementation, preserved in [the original release](RELEASE_1_3.md) and [its validation](VALIDATION_1_3.md). They do not describe policy-3 L/M/T behavior.

<a id="lab-item-prices"></a>
## Price-curve reference

The original Lab price study used fixed first-clear thresholds 30/90/210/450. The current capped curve and equipment-discount boundary are described in [earned inventory and prices](MOVE_BUDGET_POWERUPS.md#earned-inventory-and-prices); [historical economy checks](validation/v1.3/economy-tests.txt) retain the old 480-board test scope.
