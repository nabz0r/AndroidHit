# Proposed level-management redesign — not implemented or shipped

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
