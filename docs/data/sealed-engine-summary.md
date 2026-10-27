# Sealed passages: prototype engine scope

Six independent `LD1-P` challenges introduce fixed cells that block every crossing ray while their associated key piece is present. Removing that key opens its cells. Undo restores the key and closes the same cells; restart and validated history reconstruction derive the obstacle state from the ordinary remaining-piece mask. There is no independent mutable obstacle flag to desynchronize.

The prototype is isolated from Journey, Remix and historical challenges. The existing Level constructor supplies an empty barrier list. All C/R/B/Q/G/H layouts remain obstacle-free and unchanged by this addition. The P pack is intended for mode 4: unlimited actions, no progression or currency rewards, no paid continuation, no date refresh. Its UI integration and Android execution are separate evidence from these engine checks.

| Challenge | Piece count | Fixed cells | Rule introduced |
|---|---:|---:|---|
| P1 | 4 | 1 | One exiting key opens a visibly obstructed ray |
| P2 | 5 | 2 | One key opens two cells |
| P3 | 5 | 2 | A second key is behind the first key's seal |
| P4 | 5 | 1 | A key opens a fixed cell and toggles the required exit gate |
| P5 | 4 | 1 | Taking a legal key too soon closes a gate needed by another piece |
| P6 | 7 | 2 | Two key dependencies combined with two order-sensitive gate changes |

`SealedCatalog.get(1..6)` returns immutable cached descriptions with explicit certified solution orders. `Level.barriers` is an immutable list of `Barrier(row,col,keyTileId)`. `Board.blockingBarrier(tileId)` identifies the nearest active obstacle on a ray, and `LOCKED_BARRIER` explains a blocked attempt when that obstacle is the nearest blocker. Piece IDs, gate states and barrier coordinates are validated; overlap, nonexistent keys, duplicate fixed cells and malformed histories are rejected.

Run `scripts/test-sealed.sh`. The recorded final run passed **10,425 checks**, including every piece subset and all 16 gate masks on all six boards, compared with a separate brute-force fixed-cell oracle. It also checks every returned solution, undo/reclose, restart, history restoration, key-order traps, malicious definitions, challenge codes and isolation from all 1,240 current and 480 historical board definitions.

The progression audit is separate: `scripts/test-difficulty.sh` records 1,000 Journey and 240 Remix boards, exact random-legal baselines, seeded blind taps and geometric uniqueness under eight rotations/reflections, translations and reordered piece IDs. This does not prove uniqueness under every possible abstract puzzle-graph isomorphism. Neither suite establishes that people find the new obstacles enjoyable; observing players remains necessary.

The four final JVM reports are under `docs/validation/v1.5/`: `engine-tests.txt`, `move-budget-tests.txt`, `difficulty-tests.txt` and `sealed-tests.txt`. These are host-side engine checks, not phone performance or Android interaction measurements.
