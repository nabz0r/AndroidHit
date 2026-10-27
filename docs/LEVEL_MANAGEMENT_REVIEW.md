# Level management review — open issue after 1.7

**Historical 1.7 diagnosis; its correction is now implemented in the separate 1.8 source candidate.** See [V4 rules, productive budgets, compatibility and exhaustive evidence](PROGRESSION_V4.md). The shipped 1.7 packages remain unchanged; installed 1.8 acceptance is recorded separately.

**Preserved diagnosis and original proposal; no gameplay change is applied to 1.7.** Source baseline: `245f9f59f01ca1f5a4226b14ab8e8c707bcaf896`. Real Google/two-account qualification keeps that release unchanged. The old J/K level-management problem remains reproducible under its preserved rules; the new L/M route implements manual gate recovery and productive-cost budgets.

[Exact J1–30 metrics](validation/early-levels-v1.7/J1-30-metrics.csv) · [J7 reproduction](validation/early-levels-v1.7/J7-reproduction.md) · [J8 reproduction](validation/early-levels-v1.7/J8-reproduction.md) · [Proposed manual-gate metrics](validation/early-levels-v1.7/manual-gate-proposal-metrics.csv) · [Reproduce the audit](../scripts/level-rework-audit/run.sh) · [Execution record](validation/early-levels-v1.7/README.md)

## Measured diagnosis

Read-only audit of `/Users/n/androidhit` at `245f9f59f01ca1f5a4226b14ab8e8c707bcaf896`. The original audit ran under `/tmp`; the later documentation-only addition preserves its measured baseline. No application, site, runtime, release or artifact was changed. This is a technical design diagnosis, not a human playtest or retention study.

## Result

The user's complaint is reproducible. Every J1–30 initial board has a solution, but **25 of the 30 boards permit a legal exit that irreversibly loses the remaining position unless it is undone**. Ten allow that losing decision on the very first exit. The existing certificate tests prove that some good order exists; they do not prove that playable choices are forgiving or legible.

The independent cell-ray model enumerated **2,254 reachable piece subsets, 5,639 legal transitions, 420 losing subsets and 355 losing subsets that still show legal exits**. Each subset's gate mask is uniquely determined by the removed tiles, so this also enumerates every reachable `(remainingMask, gateMask)` state. Every state's solvability, legal exits and each candidate exit's safety were compared to the actual `Board` implementation. No solver disagreement was found.

| Level | Pieces / initial actions | Reachable states | Losing states | Losing while legal exits remain | Earliest losing exit | Uniform-legal completion reference |
|---|---:|---:|---:|---:|---:|---:|
| J7 | 9 / 12 | 52 | 6 | 0 | 3rd | 72.222% |
| J8 | 10 / 14 | 90 | 6 | 0 | 4th | 76.389% |
| J13 | 10 / 13 | 51 | 6 | 4 | 5th | 60.455% |

The last column is the exact probability of finishing if a mathematical agent repeatedly chooses uniformly from currently legal exits, with no undo, blocked taps or hint. It is **not a prediction of real-player success or a measured difficulty score**. State percentages are also not player probabilities: distinct subsets can have very different path multiplicities.

Trap-free J1–30 boards: **1, 2, 3, 12, 17**. A losing initial exit exists in **10, 16, 18, 20, 21, 22, 23, 26, 28, 30**. See `J1-30-metrics.csv` for all 30 boards, counts, codes, budgets, earliest losing depths and the reference probabilities.

## J7: a legal third exit freezes the board

Identity `LD1-J-7-A18D992A`, initial gates **13**, nine tiles, twelve actions. Coordinates below are **one-based**; IDs are zero-based. Gate bits: UP=1, RIGHT=2, DOWN=4, LEFT=8.

1. Exit tile **2**, row **4**, column **4**, DOWN. It opens RIGHT (gates 15).
2. Exit tile **3**, row **4**, column **5**, DOWN.
3. Exit tile **5**, row **4**, column **3**, RIGHT. Its switch closes LEFT (gates 7).

Every move is legal. Before the third exit, legal choices are `[0,1,4,5]`; `[0,1,4]` remain solvable, while `5` loses. After `5`, six pieces and nine actions remain, but **no exit is legal**. This is not exhaustion. Closing LEFT prevents the pieces that would unblock the UP switch from leaving, so the gate cannot be reopened.

One Undo repairs this position: it restores tile5 and gates15. Under the current paid-action Undo policy, Undo plus the seven restored exits costs **8** actions; **9** remain, so the first correction fits. The complete board's margin3 supports at most **one** such wrong-exit/Undo pair without blocked taps. Two mistakes already add4 actions above the9 mandatory exits.

Full completion in tile IDs: `[0,1,2,3,4,5,6,7,8]`.

## J8: a legal fourth exit freezes the board

Identity `LD1-J-8-313284BB`, initial gates **13**, ten tiles, fourteen actions.

1. Exit tile **0**, row **2**, column **2**, UP.
2. Exit tile **1**, row **2**, column **1**, LEFT.
3. Exit tile **2**, row **2**, column **3**, LEFT.
4. Exit tile **5**, row **2**, column **4**, LEFT. Its switch closes DOWN (gates9).

Before the fourth exit, legal choices are `[3,5,8]`; `[3,8]` remain solvable, while `5` loses. Afterwards six pieces and ten actions remain, but no exit is legal. The remaining DOWN pieces cannot leave; the switch that would reopen DOWN exits LEFT but is blocked by pieces whose own path depends on DOWN reopening.

One Undo repairs it. Undo plus seven restored exits costs **8** actions; **10** remain. The margin4 allows at most **two** full wrong-exit/Undo corrections without blocked taps.

Full completion in tile IDs: `[0,1,2,3,4,5,6,7,8,9]`.

Full per-tile source tables are in `J7-reproduction.md` / `J8-reproduction.md`. `J7-fixture-zero-based.json` and `J8-fixture-zero-based.json` use zero-based coordinates for automation. `J7-states.csv` and `J8-states.csv` list every reachable state and a shortest legal prefix reaching it.

## Why the current authoring creates these traps

1. `GenerateProgressionV3.java:29–39` deliberately targets a uniform-legal completion probability around **0.70** for J4–9. It does not target zero irreversible branches.
2. Only 30 existing candidates are considered per bucket (`:115–119`), selected greedily and removed (`:121–138`). A desired recovery beat can therefore fail badly when an appropriate candidate is no longer available. J11 targets .95 but selects .6111; J15 targets .95 but selects .5880; J21/J25 target .95 but select .5. The beat and difficulty labels are independently assigned from position, not constrained by the resulting risk.
3. The exact authoring probability is a valid machine statistic, but the metric cannot distinguish visual reasoning, a teachable gate interaction, an obscure dependency and a blind closure trap. Current selection uses no acceptance test for early irreversible choices, explicit consequence preview, or recoverability after ordinary trial and error.
4. Every completed order consists of exactly **N successful exits for N tiles**, regardless of order (`MoveBudget.java:72–73`). Thus the budget N+margin is principally a tolerance for blocked taps and Undo, not a Candy Crush style efficiency objective with several productive solutions of different lengths. Lowering this margin makes exploration more punitive; it does not introduce a deeper route optimization problem.

## Why it feels unfair despite existing guidance

`LumaView.drawRay:924–928` shows an immediate legal exit as white even if the exit destroys every solution. Gate highlights (`:918`) identify affected borders but do not show which open gate will close, what pieces become trapped, or the next-state verdict. `movePiece:930–941` consumes the action and commits the exit before showing the dead-end warning. The extra gate lesson ends at J6 (`:842–844`). The warning therefore often appears only after a choice that the interface presented as valid.

The exact dead-end guidance is technically correct and avoids charging an unusable hint. However, Undo costs an action, re-exiting the restored piece costs another, and Undo is unavailable at zero actions (`:944–947`). A dead-end repair action is only offered when it already fits the remaining allowance (`:834`). More actions alone do not repair closed-gate logic: they only help if the correct Undo is available and explained. The ordinary exhausted flow should never imply that an ad or gold can fix a logically lost position by itself.

## Design alternatives and trade-offs

| Alternative | Exact practical effect | Important cost / constraint |
|---|---|---|
| Reject any losing exit before commit | `Board.isSafeMove(id)` before `consumeAction()`/`Board.move()`, with no stock, currency or action charge; no newly entered logical dead end | Free rejection is an unlimited correctness oracle. Players can tap until accepted, weakening the earlier request for deeper reasoning. Best as a short-term accessibility/safety mode, not the entire redesign. |
| Author monotonic opening levels | Fixed open gates and/or unlock-only keys/seals; no XOR closures. Removing a legal piece cannot add a blocker, so any initially solvable board stays solvable | Spatial reading/dependencies remain, but the consequential order puzzle is removed. With one-piece-per-exit, merely increasing N is not deeper. Existing plain-key lesson machinery proves this can be implemented without a new runtime generator. |
| Fully reversible order puzzles | Free Undo **and refund of the original exit's action**, available even at zero; clearly preview gate before/after and explain the trapped dependency | Removing the Undo charge alone is insufficient: the already spent wrong exit still consumes margin. Current restore validation assumes `used >= boardMoves + 2*undos`, so this requires a versioned budget/session policy and a deliberate scoring rule. It preserves order reasoning, while restarting/brute-forcing becomes a choice rather than a paywall. |
| Add variable-cost productive mechanics / explicit objectives | Rotations, repositioning or chain effects can make successful solutions require different numbers of actions, so a move allowance can measure planning efficiency | A real gameplay redesign, requiring new authored content, accessibility, save identity and solver/test models. It cannot be claimed as achieved by reordering the current layouts or adding more tiles. |

## Recommendation

**Do not treat the report as another request to add +2 actions.** For the immediate correction, make logical recovery nonpunitive and explicit: give a concrete before/after gate preview, a direct return to the last solvable position, and ensure such recovery never requires an ad, gold or a hint item. Keep legitimate blocked-tap or score goals separate from recovery from hidden logic traps.

For the next authored progression, use a new content identity: a genuinely trap-free opening built around visible spatial dependencies and monotonic keys/seals, then explicit reversible gate lessons with forgiving exploration. Gate puzzles with deliberate losing branches can remain in a clearly introduced challenge/Remix track. Preserve old J/K challenge resolution and in-progress sessions; changing XOR to OR globally silently changes their rules and certificates.

If the desired main mode must remain both forgiving and strategically move-limited, prototype at least one variable-cost productive mechanic before authoring another thousand maps. Define its goals and exact solver first; otherwise N+margin remains a mistake counter and the same design problem will recur under new artwork.

Authoring acceptance should explicitly separate: initial solvability; all-reachable-state trap freedom where required; first-risk depth; semantic explanation of each closure; number of forced moves/dependency depth; recovery cost; and reference-agent statistics. Never infer human difficulty solely from a certificate, a label or a uniform-random agent. Human playtesting should measure whether the consequence is understood before the fatal tap, how often players need recovery, and frustration, without claiming those outcomes from this audit.

## Reproduce

The audit source [EarlyTrapAudit.java](../scripts/level-rework-audit/EarlyTrapAudit.java) compiles with the repository's pure engine Java files; it does not load Android or change app state. Run `./scripts/level-rework-audit/run.sh` from the repository root; compiled classes stay in a disposable temporary directory. The independent model walks cells, derives gate XOR from each subset, explores every reachable state, and checks runtime `Board.hasSolution`, legality and `Board.isSafeMove` against its own results.


## Proposed redesign — not implemented or shipped

Baseline: `245f9f59f01ca1f5a4226b14ab8e8c707bcaf896`. Keep this installed release unchanged during real Google/two-account qualification. This document is a scoped proposal backed by offline simulations, not a claim that these rules exist in the game.

## Product decision

Retain readable directional pieces and the gate identity, but prototype an explicit **Open gate** action that costs one move. A piece exit still costs one move. A closed directional border is a meaningful interactable control, with a visible one-action price and before/after preview. There is no paid item, ad or purchased gold needed to operate a gate. Tapping an already open gate only explains it and consumes nothing.

This makes a wrong XOR closure recoverable without resetting earlier progress, and introduces productive actions that need not remove a tile. It gives the budget a possible role beyond taxing erroneous taps. It must be evaluated in a small authored prototype first: blindly adding the control to the current thousand boards is not a difficulty rework.

## Exact offline simulation on the old first 30 boards

The proposed rules were modeled independently as `(remaining piece mask, current 4 gate bits)`. Legal exits retain the old ray/key rules and switch XOR; opening any currently closed bit costs one extra action. Dynamic programming minimizes the number of additional gate openings. Within a mask, opening only adds a gate bit; exits decrease the piece count, so this model is finite and acyclic.

- All **420** old losing subsets become recoverable under this proposed mechanic.
- Across all **2,254** old reachable subsets, at most **2** additional openings are needed.
- All those states can finish within the old initial margins if the previous history consists only of successful exits and no already spent blocked attempts/Undo. This does not claim that arbitrary players can never exhaust a budget.
- Every original certificate still needs **zero** manual operations. Therefore the optimal old-board cost remains **N**: the new action helps repair mistakes but is not yet a mandatory planning choice on this old content.
- The same old margins would make this opening very permissive. New authoring must deliberately include mandatory/optional gate operations and compare optimal productive costs; simply tightening margins again would reproduce punitive recovery.

J7 old trap `[2,3,5]`: remaining6 pieces,9 actions. Proposed recovery costs8 actions:

`open LEFT → exit0 → exit1 → exit4 → exit6 → open LEFT → exit7 → exit8`.

Two openings are necessary because tile6 closes LEFT again after it has been manually reopened. A single-open UI promise would be false for this state.

J8 old trap `[0,1,2,5]`: remaining6 pieces,10 actions. Proposed recovery costs7:

`open DOWN → exit3 → exit4 → exit6 → exit8 → exit9 → exit7`.

The exact DP values for each old level are in `manual-gate-proposal-metrics.csv`; the temporary model is `ManualGateAudit.java`. These are proposal simulations, not tests of Android implementation.

## A bounded implementation plan

1. **Prototype only 12–20 authored boards**, including the J7/J8 trap shapes, one-key lessons, clear safe alternatives, a planned mandatory gate operation, and a board where two sensible complete routes have different productive action costs. Use a separate rule/content identity. Leave C/R/B/Q/G/H/J/K code resolution and saved attempts untouched.
2. Introduce typed actions such as `EXIT(tileId)` and `OPEN_GATE(direction)`. Keep board legality separate from optimal-cost assessment. Save the full ordered action history or a validated equivalent, current gate state, budget-policy version and rule-set identity. Existing tile-ID-only history cannot reconstruct manually opened gates, and the old Undo accounting validator cannot validate new free/refunding rules.
3. Replace the exit-only completion API for the new rules with an exact minimum-action plan. Existing `Solver` already includes gates in its state key, but its fixed `int[N]` output and recursion only represent exits. New hints must be able to recommend a gate operation, and Flow must either understand typed actions or be unavailable with a clear reason in the prototype. Guidance must compare remaining actions to `remaining exits + minimum required openings`, not just remaining tiles.
4. Add a preview layer showing the affected border's **open → closed / closed → open** state and the resulting dependency. Show remaining productive minimum and mistake margin separately. Provide free, explicit recovery from a hidden consequence during lessons; never sell a continuation as a cure for an unexplained logical freeze. Do not automatically mark every losing choice red globally: that turns the interface into a correctness oracle and invites brute-force selection.
5. Author budgets from the new exact productive cost and the lesson's intended slack, not tile count alone. Require a real distribution of productive route costs on strategic boards. Track dependency depth, forced actions, number and clarity of gate interactions, and cognitive novelty separately from random-reference completion and board size.

## Acceptance before replacing the main progression

- Independent reference model and runtime agree on every reachable state for the small prototype, including gate operations, Undo, save/restore, zero-action behavior, typed hints and exact optimal costs.
- No reachable logical loss is mistaken for mere budget exhaustion. Every gate-only freeze has an explicit recoverable operation; key/seal dependencies cannot contain a spatial cycle.
- The new rule set does not mutate old layouts, historical challenge codes, legacy save validation, or ongoing Google/two-account test artifacts.
- Deterministic performance bounds: no search on every draw frame; cancelable worker and generation guards for exact guidance. Dense DP is appropriate for this small offline prototype, not automatically for an Android board with22 pieces (`16 × 2^22` integer entries alone approaches256MiB).
- Installed tests cover action charges exactly once, gate-operation persistence, Undo and backgrounding mid-operation, stale hints, no-stock/power races, and exhaustion without accidental paid escalation.
- Human tests separately check whether players predict a gate's result before committing, can explain why the J7/J8 shape freezes, discover the recovery control unaided, and find two-route planning engaging. Automated solvability cannot answer these questions.

## Decision boundary

If manual gates feel like a repair tax rather than a strategic tool, retain the monotonic trap-free main mode and move irreversible XOR ordering to a clearly labeled optional puzzle track with free/refunding Undo. Do not author1000 replacements until the small prototype demonstrates a real, understandable planning objective. The current real-account qualification baseline remains frozen throughout this design work.
