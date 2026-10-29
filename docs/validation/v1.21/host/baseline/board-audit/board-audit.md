# Board reachability audit — 1.7

All 1,000 frozen Journey J boards and 240 frozen Remix K boards are initially solvable within their existing action budgets. No geometry, seed, certificate, challenge identity or budget allowance was changed.

- Boards: 1240.
- Reachable piece subsets checked: 1101868.
- Legal state transitions checked: 3348959.
- Losing subsets: 494983.
- Losing subsets that still offer legal exits: 442270.
- Assertions: 49063063.
- Runtime: 96.436 seconds.
- CSV SHA-256: `f1ed69b848e7a973c51588d677f4c20d38c6f2daa54b001e2cf9659179752a2f`.

## Method and limits

An independent model walks each ray cell by cell, derives gates from XOR of removed pieces, and computes exact winning/losing reachability over every reachable subset. It does not use the runtime solver, Board geometry, or a certificate as an oracle. Every subset is then reached through legal Board moves. Legal exits, solver completions, safe next hints, nonmutating diagnostics, minimum undo recovery and action cost are compared with that independent model. Keys and sealed cells are included. Gates for a reachable subset are unique because XOR commutes. This makes subset enumeration exhaustive for every legal history's board state; recovery is additionally checked against the actual traversal history.

Every certificate is separately replayed within its initial fixed action budget with no power, ad, purchase or extension. Historical C/B/G/J and R/Q/H/K number13 definitions are all independently solvable. Historical code/layout/allowance fingerprints are checked separately by the runner.

Journey13 has a reproducible legal choice sequence that permanently closes an exit while other exits remain legal. The previous UI only checked `isStuck()` (zero legal exits), so that doom could go unreported. The user's exact installed version and saved history were not available, so this is a reproduced mechanism rather than proof of their particular sequence. Android presentation and integration are separate tests.

## Controller API

Call `board.guidance(moveBudget.remaining())` after any exit, undo, restart, restore or continuation, not every render frame. `-1` actions means unlimited. `Board.Guidance.status` is `SOLVED`, `PLAYABLE`, `NEEDS_ACTIONS`, or `DEAD_END`. The result provides `hintTileId`, `actionsToFinish`, `undoSteps`, `actionsToRecoverAndFinish`, and `recoveryFitsBudget`. `canUseHint()` forbids spending hint stock in a dead or already unaffordable run. `Board.isDeadEnd()` differs deliberately from the historical immediate-stuck predicate.

In `DEAD_END`, display undo recovery when it fits, plus free restart. Never imply that a rewarded ad or added actions alone repairs gate order. In `NEEDS_ACTIONS`, the layout still has an exact completion but its required exits exceed remaining actions. A hint always follows the actual remaining gates/pieces, not just the initial certificate. Cosmetic equipment must not change this solvability model.
