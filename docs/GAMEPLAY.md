# Luma Drift gameplay and economy

This guide covers the signed **stable 1.1.0 game** and the separate **1.2.0-sandbox Lab**. The puzzle rules and content are shared. Stable 1.1 uses fixed earned-coin prices and has no advertising or billing SDK. The Lab adds test-only commerce and a progression price curve in its own private installation; its gold cannot transfer to stable saves. Current source builds the Lab. Executed evidence is scoped by package in [VALIDATION.md](VALIDATION.md).

Luma Drift is a turn based extraction puzzle that can be played offline. Tap a piece to send it in the direction of its arrow. Clear every piece from the board. The puzzle has no countdown, life counter, energy system, gameplay server or runtime AI model. The optional Lab advertising/Billing routes can contact Google separately from puzzle play.

## Rules

1. A piece travels in a straight row or column until it exits the board. Pieces do not rotate or change position inside the board.
2. Every cell between the piece and the relevant board edge must be empty. A piece pointing at another piece cannot leave, even if that other piece points somewhere else.
3. The gate for that direction must be open. Each side has one shared gate state: closing the upper gate blocks every upward pointing piece, regardless of its column.
4. A piece with a small switch badge toggles its indicated gate **after it leaves**. An open gate closes, and a closed gate opens. Remix pieces can have two badges: both indicated gates change at once. The piece checks the old gate state before moving.
5. An illegal tap leaves the board, gates, successful move count, and undo history unchanged. It costs no coins or booster.
6. Removing the final piece completes the board. Gate states no longer matter when the board is empty.

The large arrow specifies the piece's travel direction. Smaller switch badges specify the gates that will change; these directions can differ from the travel arrow. Directional arrows supplement the four direction colors.

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

Contextual coaching appears beside these early boards: movement for boards 1–3, order for boards 4–5, and gates on board 6. The opening highlights safe moves on board 1. The full rule explanation remains available from Pause or Settings. The guide does not impose a time limit or consume inventory.

## Remix — 240 additional boards

Complete campaign board **12** to unlock the Remix tab in Journey. It has its own 240-board progression, stars and rewards; it does not replace or regenerate the original campaign. Like other modes, Remix uses the game's single interrupted-board save slot. Starting another board replaces that active session. Switches can toggle two gate directions after exiting, creating choices about which paths to open and which to close.

The first 12 Remix boards are fixed teaching layouts: first opening a pair of exits, then exchanging gate states, then reasoning about alternative exits. Later boards use a deterministic constructive generator. Remix boards 1–60 use a 5 × 5 grid; later boards use 6 × 6. The difficulty parameters and teaching sequence still require observation with real first-time players.

A first Remix completion pays 35 coins plus 12 per star: 71 coins for a three-star completion. An improved record pays 12 coins per new star. Repeating the same result does not pay again. Undo, restart and optional powers follow the same rules as the campaign.

### Why every generated board has a solution

The generator builds boards backwards. At each insertion it selects an empty cell, a direction with a clear exit path, and the gate state required before that piece exits. It records any switch transition in reverse. Reversing this insertion order produces a legal complete solution.

Generation compares several deterministic candidates and favors layouts with real blocking dependencies, varied directions, and a manageable number of initial choices. The stored solution is a proof of solvability. It is not a requirement to follow one fixed order: other legal orders may also complete a board.

Some legal moves can make a later completion impossible because they change the gates in an unhelpful order. The game allows unlimited free undo and restart. No board requires a booster to complete it.

## Undo, restart, and powers

**Undo** restores the last removed piece and reverses exactly that piece's gate transition. It is free and can be repeated back to the initial state. **Restart** restores the original board and gates; it starts a fresh scoring attempt after confirmation. Previously consumed powers remain consumed.

| Power | Behavior | Stable 1.1 shop pack |
| --- | --- | --- |
| Spark (Hint) | Highlights one next move that preserves a complete solution for eight seconds | 3 for 45 coins |
| Vision | Highlights all pieces that can legally leave in the current state for eight seconds | 3 for 40 coins |
| Flow | Plays up to three safe moves automatically, stopping if the board is completed | 2 for 60 coins |

Vision shows legal moves; a legal move is not always a safe move. Spark and Flow use the exact solver to find moves from which every remaining piece can still leave. If the current position cannot be completed, Spark and Flow request an undo or restart instead of consuming a power.

The initial save contains 180 coins, three Sparks, three Visions, and two Flows. Stable 1.1 spends only earned coins and contains no advertising, real-money purchases or payment SDK. The Lab adds clearly labelled no-charge gold simulations and optional test ads; its item prices are explained below.

## Lab item prices

The Lab counts distinct campaign + Remix boards cleared, from 0 to 480. Replays, stars earned again, events, failures, purchases, time played and advertising choices do not increase this count. It selects a band using `floor(log2(1 + progress / 30))`, implemented with integer thresholds and capped at band 4.

Booster prices use `base × (1 + 0.20 × band)`, rounded up to the next five coins and capped at twice the base price. Within the present 480-board range the highest multiplier before rounding is 1.8. This is a transparent design hypothesis, not a proven revenue optimization.

| Distinct campaign + Remix clears | Band | Spark ×3 | Vision ×3 | Flow ×2 |
|---|---:|---:|---:|---:|
| 0–29 | 0 | 45 | 40 | 60 |
| 30–89 | 1 | 55 | 50 | 75 |
| 90–209 | 2 | 65 | 60 | 85 |
| 210–449 | 3 | 75 | 65 | 100 |
| 450–480 | 4 | 85 | 75 | 110 |

Cosmetic prices remain fixed. Inventory is retained, and puzzle access does not depend on buying items. Every board remains solvable without powers. Stable 1.1 retains the first row's fixed prices throughout its progression.

## Lab test gold and rewarded ads

Shop opens a visibly labelled native Lab screen. It uses the Lab's separate private wallet; installing it does not upgrade or copy the stable game's coins.

The three packs contain **500, 1,400 or 3,600 test gold**. The app's own dialog clearly states that no charge occurs. Cancel grants nothing. Pending records the simulated transaction without gold and survives restart. Complete credits it once; retrying the same transaction credits zero. This is a local simulator, not a Google payment sheet, receipt, real purchase or recoverable paid entitlement. The Billing SDK can connect/query products, but actual checkout is disabled.

Advertising is a separate test route. The player must declare an adult state and explicitly enable Google contact. Unknown/minor states block the route. UMP's `canRequestAds()` must permit requests before ad initialization, and a manual load action is still required. If a Google test ad becomes available, the optional rewarded action grants **35 test gold once** on an earned-reward callback. Loading, closing or failing an ad does not itself grant gold. The age selection is a declaration, not identity verification; UMP eligibility is not proof of agreement to every advertising purpose.

The journal can record impression and paid-event callbacks when enabled. Test callback values, including currency/micros/precision, are diagnostic data, not revenue. Refusing advertising or encountering an unavailable service leaves the puzzle playable. [Detailed test instructions and remaining account/backend dependencies](MONETIZATION_SETUP.md) · [Privacy differences](PRIVACY.md).

## Stars, rewards, and cosmetics

A completed attempt earns:

- **3 stars:** no powers used and at most two undos;
- **2 stars:** at most two powers used, when the three star condition is not met;
- **1 star:** more than two powers used.

Blocked taps have no scoring penalty. There is no speed bonus. A first campaign completion pays 25 coins plus 10 per star, so a first three star completion pays 55 coins. Improving an existing record pays only 10 coins for each newly earned star. Replaying without improving the record pays no further coins.

Stable 1.1 offers six companions and six themes. The Lab preserves those six companion IDs/prices and adds **18 original silhouettes, for 24 companions total**. Its Collection has three pages of eight companions and a separate six-theme tab. Tap a card to open a native preview, then choose the displayed purchase/equip action; opening a preview does not spend coins. Nova and Lumina are included. Paid companions range from 160 to 1,100 coins in the Lab; themes remain 180–450 coins. Cosmetics change appearance and do not alter rules, solutions or board difficulty. [Full collection and event guide](COMPANIONS_EVENTS.md).

All 24 companions together cost **12,200 coins**. The initial 180 coins plus first three-star clears of all 240 original campaign boards total **13,380 coins**, before other purchases. That arithmetic shows a free earning path for the companion set, not an estimate of completion time or a promise that all companions and themes fit the same budget. Daily/weekly/Remix and mission rewards provide additional earned currency. The total companion-and-theme catalog costs 13,730 coins.

## XP, missions and the constellation badge

A first recorded completion of an eligible campaign, Remix, daily or weekly board grants **20 + 5 × stars XP**: 25, 30 or 35 XP. Rank starts at 1 and increases every 120 XP. Improving or repeating a previously completed board does not grant further XP or mission credit. On upgrade, existing campaign records and available current-period event records seed the progression; the game cannot reconstruct expired event history that is absent from the save.

Three optional missions refresh with the local calendar date:

| Mission | Credit required that day | Claim reward |
|---|---|---:|
| Clear boards | First recorded completion of 3 eligible boards | 35 coins |
| Collect stars | 6 stars from first recorded completions | 40 coins |
| Play an event | First recorded completion of 1 daily or weekly stage | 45 coins |

Each reward can be claimed once per date. Unclaimed daily opportunities expire with that date, but taking a break never removes XP, rank, coins or unlocked cosmetics. The distinct days played counter is cumulative, not a streak.

After seven first recorded board completions, a small cosmetic badge becomes available for the companion. It can be equipped or removed from Missions. It does not change the rules or rewards. Friend challenge boards do not contribute to these systems.

## Daily and weekly events

Daily events contain three sequential stages; weekly events contain seven. Each board uses a deterministic seed derived from its period and stage, so reopening a stage during the same period returns the same layout. Event boards use a 6 × 6 grid with 15–18 pieces and four switches.

Periods follow the device's local calendar. A daily event refreshes with the local date. Weekly periods start on Monday and use that Monday's date as their identifier. These are local offline events, not server synchronized competitions. Changing the device clock can change the available period.

The Lab event screen makes the goal, completed-stage count, stage reward range and next playable stage explicit. A countdown shows the next local midnight for daily boards or Monday midnight for weekly boards, using calendar arithmetic across daylight-saving changes. It rounds remaining time up to a minute. The bonus control stays visible and explains missing stages until eligible, then offers the one-time claim. Stable 1.1's older event layout remains unchanged in its preserved APK.

The [installed Lab event run](validation/lab/collection-events-ui-smoke.txt) cleared all three daily boards for 270 coins plus the 100-coin bonus, and all seven weekly boards for 945 plus 250, at three stars. Repeated bonus claims granted zero. These are observed test outcomes for those periods, not player income forecasts.

A first daily stage completion pays 60 coins plus 10 per star. A first weekly stage completion pays 90 coins plus 15 per star. Subsequent improvements pay only the additional star difference. Clearing all daily stages allows a one time 100 coin claim for that day; clearing all weekly stages allows a one time 250 coin claim for that week. A board opened during an expired period cannot claim rewards for the next period.

## Friend challenges and sharing

Home offers challenge sharing and manual code entry. A versioned code identifies one initial board: **C** for a campaign number, **R** for a Remix number, or **E** for an event seed. The app also handles its `lumadrift://challenge/...` links when Android routes them to the installed game. A copy checksum catches typing mistakes; it is not authentication or anti-cheat.

The code carries no player identity, wallet, inventory, campaign unlocks or move history. Opening it starts a separate practice challenge on the same layout and initial gate state. It can be played regardless of the recipient's normal progression, but grants **no coins, XP, mission credit or campaign/Remix unlocks**. Powers are unavailable in this mode; free undo and restart remain available. An interrupted friend challenge can resume locally.

Sharing renders an original 1080 × 1350 card from the initial board and gates, with the challenge code and optional result stars. Android's share sheet lets the player choose an application or cancel. The game does not select contacts, send a message automatically, create matchmaking or verify delivery. A share-sheet handoff is not evidence that someone received or played a puzzle. Receiving apps may handle the accompanying text/link differently; manual code entry remains available.

## Saves and settings

Progression, coins, inventory, cosmetic ownership, equipped appearance, XP/rank, mission claims, badge state and settings are stored in private local preferences. An unfinished board stores its successful move history and replays that history during restoration. Invalid histories are rejected atomically, and an event save from an expired period is not restored. The active play timer pauses for help, pause and background time. It does not impose a time limit or change the star rules. Saves do not synchronize between devices, and uninstalling or clearing application storage removes them.

The interface supports English, French, German, Spanish, Portuguese, and Italian, plus automatic selection from the system language with English fallback. Sound effects, music, haptics, and reduced motion have separate settings. The interface includes keyboard navigation and virtual accessibility nodes; the existence of these code paths does not establish a completed accessibility audit.

## Optional Lab Google account

Settings → Account is independent of puzzle access and advertising consent. Guest play remains available. The default Lab has no owner OAuth/Firebase configuration, so its sign-in control reports that setup is missing instead of inventing a Google profile.

With valid owner configuration, the adapter requests a Google credential through Android Credential Manager and passes it to Firebase Auth for verification before showing a signed-in user. This does not upload coins, progression or the local journal, and does not provide cloud saves or purchased-wallet recovery. Signing out leaves local game progress intact. The confirmed delete action requests deletion of the Firebase app account; it does not delete the player's Google account or local saves. A recent sign-in may be required. No actual successful configured login/deletion is claimed by these implementation notes. [Setup and evidence limits](GOOGLE_SIGN_IN.md).

## Optional local play journal

Settings → Local journal provides a diagnostic record that is **off by default**. Enabling it records foreground sessions, board starts/completions/exits, power use and share-sheet handoffs. Entries include local date, UTC timestamp and relevant gameplay fields such as mode, board, event period, stars, undos or active play duration. The record is capped at 2,000 event entries and is never uploaded automatically.

The screen shows observed sessions and active days. D1/D7 indicators refer to the date of first journal opt-in plus one or seven local calendar days, not the installation date. A recorded event on that target date means observed. An elapsed target date without an event is only “not observed locally” while observation remained enabled; incomplete or paused observation is unknown. These are single-device observations, not global retention, acquisition or a cohort dashboard.

Export creates a JSON file and opens Android's share sheet; the player chooses its destination. Disabling the journal stops new entries. Clearing it resets the observation window and removes app-held staged journal exports, without changing game progress. Clearing cannot recall files already copied or sent to another app. See [PRIVACY.md](PRIVACY.md) for data and sharing details.

## Engine API and verification

The engine lives under `app/src/main/java/com/androidhit/lumadrift/engine/` and has no Android dependency:

- `LevelCatalog.get(1..240)` returns an immutable campaign description; `LevelCatalog.remix(1..240)` returns the separate Remix family; `LevelCatalog.event(seed)` returns a reproducible daily/weekly board.
- `Board.move(id)` validates the gate and path, removes one piece, and applies its switch. It returns a result explaining success or the blocking reason.
- `undo()`, `restart()`, `history()`, and `restoreHistory(history)` handle reversible sessions.
- `snapshot()`, `remainingTiles()`, `isPresent(id)`, `gateMask()`, and `blockingTile(id)` support rendering without exposing mutable session internals.
- `solve()`, `hint()`, and `isSafeMove(id)` run an exact acyclic search with memoization. Every move removes a piece, so the search does not need a depth cutoff or approximate answer.

Run the engine suite with:

```sh
scripts/test-engine.sh
```

The suite uses a JDK, not the Android runtime. Its coverage includes campaign and Remix solutions, event seeds, deterministic generation, alternate player choices, safe hints, gate transitions, invalid moves, undo, restart, atomic session restoration and challenge-code validation. Exact counts and results belong to the versioned [validation evidence](VALIDATION.md); timings on the development host are not Android performance measurements.

These checks establish engine behavior for the covered cases. APK installation, graphics, sound playback, touch handling, Android lifecycle behavior, and device compatibility require separate runtime validation and are recorded in the project's release evidence.
