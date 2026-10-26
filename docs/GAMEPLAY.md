# Luma Drift gameplay

Luma Drift is an offline, turn based extraction puzzle. Tap a piece to send it in the direction of its arrow. Clear every piece from the board. There is no countdown, life counter, energy system, server, or AI model running during a game.

## Rules

1. A piece travels in a straight row or column until it exits the board. Pieces do not rotate or change position inside the board.
2. Every cell between the piece and the relevant board edge must be empty. A piece pointing at another piece cannot leave, even if that other piece points somewhere else.
3. The gate for that direction must be open. Each side has one shared gate state: closing the upper gate blocks every upward pointing piece, regardless of its column.
4. A piece with a small switch badge toggles its indicated gate **after it leaves**. An open gate closes, and a closed gate opens. The piece checks the old gate state before moving.
5. An illegal tap leaves the board, gates, successful move count, and undo history unchanged. It costs no coins or booster.
6. Removing the final piece completes the board. Gate states no longer matter when the board is empty.

The large arrow specifies the piece's travel direction. The smaller switch badge specifies the gate that will change; these directions can differ. Directional arrows supplement the four direction colors.

## Campaign

The campaign contains **240 deterministic boards in eight chapters of 30**. Chapter and board selection are available in Journey. Completing a campaign board unlocks the following board; completed boards remain available for replay.

| Chapter | Boards | Grid | Piece count | Switch pieces per board |
| --- | --- | --- | --- | --- |
| 1 | 1–30 | 5 × 5 | 5–10 | 0 for boards 1–5; 1 thereafter |
| 2 | 31–60 | 5 × 5 | 10–12 | 1–2 |
| 3 | 61–90 | 5 × 5 | 11–13 | 2 |
| 4 | 91–120 | 5 × 5 | 12–14 | 2–3 |
| 5 | 121–150 | 6 × 6 | 13–15 | 3–4 |
| 6 | 151–180 | 6 × 6 | 14–16 | 4 |
| 7 | 181–210 | 6 × 6 | 15–17 | 4–5 |
| 8 | 211–240 | 6 × 6 | 16–18 | 5 |

Boards 1–3 teach directional motion with immediately free paths and open gates. Boards 4–5 introduce pieces blocking other pieces, with all gates open. Switches appear from board 6. Later chapters increase the board area, piece count, and number of gate transitions. These are implementation parameters, not measured difficulty ratings from player testing.

### Why every generated board has a solution

The generator builds boards backwards. At each insertion it selects an empty cell, a direction with a clear exit path, and the gate state required before that piece exits. It records any switch transition in reverse. Reversing this insertion order produces a legal complete solution.

Generation compares several deterministic candidates and favors layouts with real blocking dependencies, varied directions, and a manageable number of initial choices. The stored solution is a proof of solvability. It is not a requirement to follow one fixed order: other legal orders may also complete a board.

Some legal moves can make a later completion impossible because they change the gates in an unhelpful order. The game allows unlimited free undo and restart. No board requires a booster to complete it.

## Undo, restart, and powers

**Undo** restores the last removed piece and reverses exactly that piece's gate transition. It is free and can be repeated back to the initial state. **Restart** restores the original board and gates; it starts a fresh scoring attempt after confirmation. Previously consumed powers remain consumed.

| Power | Behavior | Shop pack |
| --- | --- | --- |
| Spark (Hint) | Highlights one next move that preserves a complete solution for eight seconds | 3 for 45 coins |
| Vision | Highlights all pieces that can legally leave in the current state for eight seconds | 3 for 40 coins |
| Flow | Plays up to three safe moves automatically, stopping if the board is completed | 2 for 60 coins |

Vision shows legal moves; a legal move is not always a safe move. Spark and Flow use the exact solver to find moves from which every remaining piece can still leave. If the current position cannot be completed, Spark and Flow request an undo or restart instead of consuming a power.

The initial save contains 180 coins, three Sparks, three Visions, and two Flows. The shop spends coins earned inside the game. This build has no advertising, real money purchases, or payment integration.

## Stars, rewards, and cosmetics

A completed attempt earns:

- **3 stars:** no powers used and at most two undos;
- **2 stars:** at most two powers used, when the three star condition is not met;
- **1 star:** more than two powers used.

Blocked taps have no scoring penalty. There is no speed bonus. A first campaign completion pays 25 coins plus 10 per star, so a first three star completion pays 55 coins. Improving an existing record pays only 10 coins for each newly earned star. Replaying without improving the record pays no further coins.

Six characters and six visual themes are available in Collection. Nova and Lumina are included. Other characters cost 160–600 coins; other themes cost 180–450 coins. These purchases change appearance and do not alter the rules, solution, or board difficulty.

## Daily and weekly events

Daily events contain three sequential stages; weekly events contain seven. Each board uses a deterministic seed derived from its period and stage, so reopening a stage during the same period returns the same layout. Event boards use a 6 × 6 grid with 15–18 pieces and four switches.

Periods follow the device's local calendar. A daily event refreshes with the local date. Weekly periods start on Monday and use that Monday's date as their identifier. These are local offline events, not server synchronized competitions. Changing the device clock can change the available period.

A first daily stage completion pays 60 coins plus 10 per star. A first weekly stage completion pays 90 coins plus 15 per star. Subsequent improvements pay only the additional star difference. Clearing all daily stages allows a one time 100 coin claim for that day; clearing all weekly stages allows a one time 250 coin claim for that week. A board opened during an expired period cannot claim rewards for the next period.

## Saves and settings

Progression, coins, inventory, cosmetic ownership, equipped appearance, and settings are stored in private local preferences. An unfinished board stores its successful move history and replays that history during restoration. Invalid histories are rejected atomically, and an event save from an expired period is not restored. Saves do not synchronize between devices, and uninstalling or clearing application storage removes them.

The interface supports English, French, German, Spanish, Portuguese, and Italian, plus automatic selection from the system language with English fallback. Sound effects, music, haptics, and reduced motion have separate settings. The interface includes keyboard navigation and virtual accessibility nodes; the existence of these code paths does not establish a completed accessibility audit.

## Engine API and verification

The engine lives under `app/src/main/java/com/androidhit/lumadrift/engine/` and has no Android dependency:

- `LevelCatalog.get(1..240)` returns an immutable campaign description; `LevelCatalog.event(seed)` returns a reproducible event board.
- `Board.move(id)` validates the gate and path, removes one piece, and applies its switch. It returns a result explaining success or the blocking reason.
- `undo()`, `restart()`, `history()`, and `restoreHistory(history)` handle reversible sessions.
- `snapshot()`, `remainingTiles()`, `isPresent(id)`, `gateMask()`, and `blockingTile(id)` support rendering without exposing mutable session internals.
- `solve()`, `hint()`, and `isSafeMove(id)` run an exact acyclic search with memoization. Every move removes a piece, so the search does not need a depth cutoff or approximate answer.

Run the engine suite with:

```sh
scripts/test-engine.sh
```

The suite uses a JDK, not the Android runtime. It verifies all **240 campaign solutions and 1,002 event seeds**, including extreme signed seed values, deterministic generation, alternate player choices, safe hints, gate transitions, invalid moves, undo, restart, and atomic session restoration. The recorded run passed **190,238 assertions** and checked **804 alternate legal starts**, including **12 positions correctly identified as requiring undo**. Its observed elapsed time was **2.37 seconds on the development host**, which is not an Android performance measurement.

These checks establish engine behavior for the covered cases. APK installation, graphics, sound playback, touch handling, Android lifecycle behavior, and device compatibility require separate runtime validation and are recorded in the project's release evidence.
