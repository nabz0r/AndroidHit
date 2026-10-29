# Journey 13: reproducible diagnosis

The reported player save and exact installed version were not available. This reproduces the frozen 1.6 J13 board and checks the other historical board-13 identities separately. It does not identify the user's exact tap sequence.

- Identity: `LD1-J-D-6881B9AC`; source G20; progression version 3.
- Initial gate bits: 7; pieces: 10; base actions: 13; error/undo allowance: 3; sealed cells: 0.
- Solvable with the initial action allowance, no power or purchase: **yes**.

Coordinates below are one based; IDs remain zero based. A gate bit is UP=1, RIGHT=2, DOWN=4, LEFT=8. The exit occurs before its switch XORs the gates.

| Tile ID | Row | Column | Exit | Switch bits |
|---:|---:|---:|---|---:|
|0|5|3|DOWN|0|
|1|5|1|DOWN|8|
|2|5|2|LEFT|0|
|3|5|4|LEFT|0|
|4|1|3|UP|0|
|5|1|5|LEFT|0|
|6|2|5|RIGHT|0|
|7|4|5|UP|1|
|8|4|4|DOWN|1|
|9|4|3|RIGHT|0|

## Full exact completion

| Step | Tile | Row | Column | Gates after exit | Actions left |
|---:|---:|---:|---:|---:|---:|
|1|0|5|3|7|12|
|2|1|5|1|15|11|
|3|2|5|2|15|10|
|4|3|5|4|15|9|
|5|4|1|3|15|8|
|6|5|1|5|15|7|
|7|6|2|5|15|6|
|8|7|4|5|14|5|
|9|8|4|4|15|4|
|10|9|4|3|15|3|

## Smallest legal-but-doomed prefix

Exit tile IDs `[0, 1, 2, 3, 8]` in that order. Every exit is legal. The remaining gate mask is `14`; the remaining-piece bit mask is `752`. Legal exits still visible: `[6]`. The independent model and runtime solver both find **no completion**.

The old `isStuck()` returns `false` here, because it checks only whether any exit is legal. That leaves a doomed run unannounced. `guidance()` now returns `DEAD_END`, no spendable hint, and `undoSteps=1`. Recovery requires 7 actions including undo and restored exits; 8 remain. Restart remains free. Extra actions alone do not reopen a gate.

## Other board-13 identities

| Family | Pieces | Initially solvable |
|---|---:|---|
|C13|8|yes|
|B13|8|yes|
|G13|9|yes|
|J13|10|yes|
|R13|13|yes|
|Q13|12|yes|
|H13|12|yes|
|K13|12|yes|
