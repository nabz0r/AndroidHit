# Puzzle rules and native UI audit

Date: 2026-10-28. Scope: the released 1.13 rc2 puzzle UI and the evolving 1.14 source. Read-only review of the controller, renderer, board rules, saved-run routing, budget, and native captures. Only the new guide, localized copy, and their host tests were implemented by this reviewer. Native 1.14 qualification is separate; this document does not claim it has passed.

## What made the reported level 15 look impossible

The released **L15** is mechanically solvable from its initial state: 5×5 cells, 10 pieces, an exact minimum of 12 productive actions, and 16 available actions. Its left side starts closed. The left-moving piece at row 2, column 2 has a small upward switch badge. That badge names the affected gate, not another movement direction.

One certified completion, in one-based row/column notation:

1. Exit r4c1 ↓, r1c2 →, r3c5 →, r3c4 →, r3c3 →, r5c4 ↓, r4c4 ↓.
2. Open the LEFT gate manually.
3. Exit r2c2 ←. Its switch closes the UP gate after the exit.
4. Open UP manually.
5. Exit r4c2 ↑, then r4c3 ←.

This was checked against the actual engine. The native baseline is `docs/screenshots/v1.13/ui-bugs/ui-sdk-playtest24-b/prepare/journey-15-start-en.png`.

The 1.13 screen says “10 little stars to guide home” and shows tiny locked gate symbols. It does not visibly explain that a locked side is itself a button. `V18Strings.gate_action` contains that instruction but was not rendered by `LumaView`. The optional gate practice link was below Play, around content y=843. FirstLight introduces switch causality, but cannot replace the later, distinct lesson about manually opening a side. These are concrete comprehension risks, not proof that every player is confused.

The new **N15** is a different versioned board. It isolates one manual gate, has no switches or seals, and must not be described as an unchanged L15 fix. Existing L saves and L challenge codes keep their own geometry.

## Mechanical distinctions the UI must teach

| Mechanic | Actual rule | Necessary explanation |
| --- | --- | --- |
| Large arrow | Straight exit direction; no rotation and no turning | Tap a piece or swipe along the large arrow. |
| Piece obstruction | Every present piece on the ray blocks, regardless of its own arrow | Identify the nearest blocking row/column; a refused attempt is free under policy 4. |
| Side gate | One state per direction, shared by every lane on that side | A centered gate symbol is not the sole physical hole a piece must line up with. |
| Manual opening | A closed side may be opened in typed-action rules; opening costs one productive action | Render a real button with explicit cost and preview/confirmation. Historical switch-only rules must not promise this control. |
| Small switch arrow | Names the side toggled after the piece leaves; open becomes closed and closed becomes open | Distinguish the large movement arrow from the small effect badge and show the actual before/after change. |
| K key and seal | A numbered seal remains while its matching key piece is present | The key must leave the board. Equal K numbers are an association, not a prescribed numerical order. |
| Budget | Real limit on productive actions in policy 4 | A legal but wasteful gate opening can lose a run; a refused attempt does not. |
| Return | One correction per policy-4 attempt, with its action refunded | Persist the used return count, show ×0 once spent, and offer a free restart. |

`LearningGuide` provides observations and causal explanations. It does not run a solver, spend an item, choose an optimal next move, or mutate a board. A gate focus means “this closed side is relevant to a remaining piece,” not “this is the best next action.”

## Review of the current 1.14 corrections

### Rendering and touch ownership

An intermediate enlarged-gate implementation overlapped edge cells. That was reported and corrected before this review snapshot. The current geometry is consistent for all grid sizes:

| Surface | Visible geometry / hit target |
| --- | --- |
| Board | x=55..335, y=275..555; width 280 |
| Left gate | center (29,415); visible x=6..52; hit x=4..54 |
| Right gate | center (361,415); visible x=338..384; hit x=336..386 |
| Upper gate | center (195,249); visible y=226..272; hit y=224..274 |
| Lower gate | center (195,581); visible y=558..604; hit y=556..606 |
| Budget | ends at y=219, before the upper gate hit target |
| Feedback | begins below the lower gate; Return/Restart moved below the two-line feedback |

The new closed gate displays **−1**, meaning the opening consumes one action. Its 50×50 hit target does not overlap a board cell. Tile taps use the drawn cell bounds; drag ownership and rays derive width from `cell * level.size`, removing the old fixed-300 mismatch after the board was narrowed.

The pointer handler accepts taps and direction-matching swipes. A wrong-direction swipe reports the problem without dispatching a board action. Switch and manual-gate previews clear underlying hit targets and suppress board gestures while pending. Canvas scroll coordinates and visible hit coordinates use the same scroll transform.

### Budget, return, and save identity

The current policy-4 controller charges successful actions, keeps refusals free, and never consumes Shield charges for those refusals. Return checks the quota before mutating the board and ledger. Restart resets the per-attempt return counter. Saved history, ledger policy, used actions, and `undos` are written in the same committed preferences editor. Policy-4 restoration requires the productive-action ledger to equal the restored board history length.

The renderer's zero-action recovery condition was changed from a policy-3 equality check to `hasReturn()`, so policy 4 can expose its remaining correction at zero. Old “undo freely” helper text was replaced in the affected help/recovery surfaces. Previously active Shield/gear-grace status is no longer shown as a working protection under the free-refusal policy. The Shield activation guard also preserves owned stock.

New Journey uses content version 6 and N challenge identity; preserved L/M/T routes keep their resolvers. An active old Journey save is reconstructed using its saved content family before its budget ledger is upgraded. Restarting that run does not silently exchange its board for a different N geometry. The next newly started Journey board uses N.

### Observation and failure feedback

The controller compares the actual sets of legal exits before and after an action. Highlighted newly freed pieces therefore describe immediate mechanical consequences, not solver-safe choices. Gate effects use the actual previous/current gate-mask XOR. Compact refusal messages include the cause and the free-attempt rule. Full causes remain available for accessibility.

The low-budget message can state the exact required/remaining action counts without revealing a full plan. A failed policy-4 run leads to a free restart, the one remaining Return if available, or Home; new paid continuation offers are being removed from that policy while already-earned continuation records remain recoverable. This keeps a budget failure legible as a planning consequence.

## Guide and copy validation

`scripts/test-learning-guide.sh` currently passes **4,867 checks**. It covers the actual closed-side/seal/nearest-piece precedence, L15's LEFT-then-UP switch consequence, every certified prefix in N1..30 and L1..30, stable shared-key numbering, historical manual-gate separation, no board/cache mutation, all six locales, 75-character maximum HUD notes, and 55-character maximum compact refusal messages.

The short-message cost claim applies only to the free-refusal policy. Other policies must use `Feedback.key`, not `Feedback.shortKey`.

This host evidence validates mechanical copy and helper behavior. Native layout, font-scale behavior, real touch dispatch, process-death restoration, animation timing, and perceived difficulty require the separate simulator pass. Mathematical solvability alone does not establish that the rules are understandable or the puzzle is enjoyable.


## Secondary frozen-RC2 dialog observation

On 2026-10-28, the actual RC2/code16 Lab screenshot of the native **Explore a level** dialog shows its empty numeric hint, “1–1000”, in very pale text on a light input background. The dialog title, explanation and Continue/Cancel buttons remain readable. This is an observed contrast issue in a secondary navigation dialog, separate from the puzzle rules and productive-action budget.

Exact unedited frame: [marathon A native dialog frame](../../screenshots/v1.14/ui-bugs/ui-sdk-marathon1000-rc2-a/batch-0001-0100/failure.png). Repository path: `docs/screenshots/v1.14/ui-bugs/ui-sdk-marathon1000-rc2-a/batch-0001-0100/failure.png`. PNG SHA256: `17271c47f804564d63e10a39108037c4ceb0062f3a6101e484e7c48c45fba9ae`. Installed APK SHA256: `cbc74c3214f3c64c888f34b9452be162d3e3e168f1c218d1a4537410ef005ff0`. The frame belongs to a globally failed harness run and is used here only as a visual observation, never as a passing gameplay or1000-level proof.

`LumaView.jumpMap` creates a native `EditText` and assigns its input type and hint, but leaves text and hint colors to the theme. The screenshot was captured before entering a value; it does not establish the contrast of every entered value, device theme or physical phone. A later UI maintenance pass can explicitly align the field colors with the dialog palette. No app bytes or captures were changed for this observation.

The same run also exposed a separate harness race: it inspected the dialog input once after150ms, before the editable node was available. The revised SDK now waits for an actual visible editable node and verifies the exact entered value. Subsequent real batch openings passed. This synchronization fix does not change the pale hint, gameplay budget, fixture data or app source. The secondary contrast issue is recorded without reopening the frozen core candidate during the native marathon.
