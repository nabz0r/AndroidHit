# Luma Drift · level design and challenge protocol

## Current 1.5 content and identities

| Family | Current API | Code / bound |
|---|---|---|
| Journey v2 | `BalancedCatalog.get(1..1000)` | G, 1–1,000 |
| Remix v2 | `BalancedCatalog.remix(1..240)` | H, 1–240 |
| Previous balanced Journey / Remix | `BalancedCatalog.legacyGet(1..240)` / `.legacyRemix(1..240)` | B / Q, unchanged |
| Original Journey / Remix | `LevelCatalog.get(1..240)` / `.remix(1..240)` | C / R, unchanged |
| Existing event seed | `LevelCatalog.event(long)` | E, nonnegative Java `long` |
| Sealed passages prototypes | `SealedCatalog.get(1..6)` | P, 1–6 |

All families use the canonical `LD1-<family>-<base36 value>-<CRC32>` structure. New encoders are `encodeBalancedV2()`, `encodeBalancedRemixV2()` and `encodeSealed()`. Reusing B/Q or C/R for new layouts would break shared challenges, so those routes remain frozen. Valid old active content-2/policy-1 sessions keep their board and allowance; new runs select content 3/policy 2. Earned progress remains at numbered slots. [Current compatibility and installed upgrade evidence](RELEASE_1_5.md).

The G/H fixtures are generated and analyzed offline, stored in `ProgressionV2Layouts`, and parsed/cached lazily. Runtime play does not generate or rebalance them. All have exact certified solutions, fixed N+2…4 allowances, and measured risky ordering choices after the introduction. The [current audit](data/progression-v2-summary.md) checks layout uniqueness under rotation/reflection/translation and reports exact legal-choice and seeded blind-tap baselines, without treating them as human difficulty ratings.

`Level.barriers` adds immutable `Barrier(row, col, keyTileId)` definitions. An active wall blocks an exit ray until its matching key leaves; restoring the key closes it again. Both Board and the exact solver derive this from remaining pieces. P1–P6 contain 1–2 fixed cells and 4–7 pieces, remain separate from the 1,240 progression boards, and run as unlimited/no-reward practice without powers. [Exact/oracle tests](validation/v1.5/sealed-tests.txt) · [Installed key/Undo/restoration checks](validation/v1.5/sealed-ui.txt).

## Historical v1.1 design and protocol evidence

The v1.1 engine adds a distinct **Remix** campaign with paired gate switches. The original 240 campaign boards and their save histories remain compatible. This is a new rule interaction and a controlled introduction, rather than a change to players' existing progression.

## The rule players can reason about

A tile exits in its arrow direction when its ray is clear and that side's gate is open. A switch activates **after** its tile has left:

- An ordinary tile changes no gate.
- A single switch changes one gate.
- A paired switch changes exactly two gates at the same time. An open gate closes; a closed gate opens.

There is no all-four toggle in Remix. Both affected sides must be shown on the tile and in the move preview. Undo reverses the tile removal and both gate changes atomically. The exit itself uses the gate state before the switch fires, so a switch may close its own exit after leaving.

Example: with the top and right gates open, an upward tile that changes the right and left gates can leave through the top, close the right, and open the left. The top stays open. A leftward tile can now exit if its path is clear.

## Content families and compatibility

| Family | API | Content | Compatibility contract |
| --- | --- | --- | --- |
| Original campaign | `LevelCatalog.get(1..240)` | Existing progression and single switches | Unchanged layouts, IDs, initial gates and certified solutions |
| Remix | `LevelCatalog.remix(1..240)` | 240 separate boards; paired switches throughout | Distinct progress; original saves never become Remix saves |
| Existing daily/weekly challenge | `LevelCatalog.event(long)` | Existing deterministic seeded event boards | Existing event generation retained |
| Paired-switch seed validation | `LevelCatalog.remixEvent(long)` | Independent 6×6 paired-switch boards | Used for arbitrary-seed engine validation; not an additional advertised event mode |

The product unlocks Remix after original campaign level 12. This keeps the first experience focused on movement and the existing gate rule. The engine remains independent of progress storage and does not grant unlocks or rewards.

The original board fingerprint, captured before the change, is:

```text
7cdbb8a4d94fe8ddb468a2ade2fa214b258cedd6bc5cbe24a74ee7c029b2893a
```

The v1.1 Remix board fingerprint is:

```text
9839d4f6fcf785e14f6a8e66c368a2a797c8d77a1594813d56735c1ddab35208
```

The test fingerprints serialize board number, size, initial gate state and each stable tile's ID, row, column, direction and switch mask. Any later content change must preserve the boards already referenced by `LD1` challenge codes, or introduce a new protocol version. It must not silently remap a friend's code.

## First twelve Remix boards

These layouts are explicit, frozen definitions in `RemixCatalog`, selected to introduce one interaction at a time. They are not regenerated at launch.

| Board | Teaching intent | Tiles | Switches (paired) | Initial legal / unsafe exits | Ray dependencies | Ray depth |
| --- | --- | ---: | ---: | ---: | ---: | ---: |
| 1 | Open top and bottom together | 10 | 1 (1) | 2 / 0 | 10 | 4 |
| 2 | Rotate the rule: open left and right | 10 | 1 (1) | 2 / 0 | 12 | 6 |
| 3 | Read an adjacent pair | 10 | 1 (1) | 2 / 0 | 12 | 4 |
| 4 | Exchange an open side for a closed side | 11 | 2 (2) | 2 / 0 | 12 | 5 |
| 5 | Link two paired transitions | 11 | 2 (2) | 2 / 0 | 13 | 6 |
| 6 | Combine one paired and one single switch | 11 | 2 (1) | 2 / 0 | 11 | 4 |
| 7 | Introduce a reversible ordering fork | 12 | 3 (3) | 3 / 1 | 14 | 4 |
| 8 | Interleave paired switches and ray clearing | 12 | 3 (3) | 2 / 0 | 16 | 6 |
| 9 | Recognize a gate-dependent fork | 12 | 3 (2) | 2 / 1 | 15 | 8 |
| 10 | Plan a longer transition chain | 13 | 4 (4) | 2 / 0 | 20 | 7 |
| 11 | Carry the rule across all four sides | 13 | 4 (4) | 2 / 0 | 19 | 7 |
| 12 | Resolve a mixed-switch chain | 13 | 4 (3) | 2 / 0 | 14 | 8 |

“Unsafe” means a legal initial exit leaves no exact completion without undo. The first six boards have no unsafe **initial** choices; this is not a claim that every later move is safe. A ray dependency counts a tile directly ahead of another tile's arrow in the initial layout, including more distant blockers. Ray depth is the longest chain of those spatial dependencies. It excludes gate transitions, so it is a structural measure rather than a player difficulty rating.

## Later pacing and construction

- Boards 13–50 contain 13 tiles, 51–88 contain 14, 89–126 contain 15, 127–164 contain 16, 165–202 contain 17, and 203–240 contain 18.
- Boards 1–60 use a 5×5 grid; boards 61–240 use 6×6.
- Generated Remix boards have 3–7 switches. All but one are paired switches; the remaining switch changes a single gate.
- The certified first and last exits are ordinary tiles, which separates the feedback for leaving from the feedback for a switch.

The generator constructs a legal solution backwards. When inserting a tile, it chooses a clear exit ray and computes the required previous gate state by XOR. Reversing those insertions is a certified completion. Sixteen deterministic candidates are compared for spatial blockers, gate dependencies, varied directions and a useful initial choice count. Generation does not depend on network access or an AI service.

Across the 240 Remix boards, the measured averages are **15.30 tiles**, **4.69 switches** including **3.73 paired switches**, **2.55 legal initial exits**, **0.14 unsafe initial exits**, **19.87 ray dependencies**, and **6.21 ray depth**. Each board has a distinct full layout signature. These measurements demonstrate structural variety and exact solvability. Human playtests must establish clarity, perceived difficulty and enjoyment; these figures do not predict retention or commercial success.

## Friend challenge codes

`ChallengeCode` is a pure Java board identifier with no Android dependency:

```java
String code = ChallengeCode.encodeRemix(7); // LD1-R-7-B3B86BE2
ChallengeCode challenge = ChallengeCode.parse(code);
// challenge.kind == ChallengeCode.Kind.REMIX
// challenge.value == 7L
```

| Family | Example | Resolved board |
| --- | --- | --- |
| Original | `LD1-C-1-473F0790` | Original campaign 1 |
| Remix | `LD1-R-7-B3B86BE2` | Remix 7 |
| Event | `LD1-E-C29IA-98173BD8` | Existing event seed 20261026 |

The canonical format is `LD1-<C/R/E>-<uppercase base36 value>-<8 uppercase hexadecimal CRC32 digits>`. The checksum covers everything before the final separator. Original and Remix values are bounded to 1–240. Event seeds use the full exact nonnegative Java `long` range, 0–9223372036854775807. No value passes through floating-point arithmetic. The longest valid code is 28 characters.

`parse` returns `null` for unknown versions, families, negative values, overflow, out-of-range boards, leading zeros, lowercase, extra whitespace, non-ASCII value characters, oversized input, malformed checksums or altered content. The UI may extract a code from its custom challenge URI before calling this strict parser; the engine parser itself accepts only a code.

The Android app accepts either code text or `lumadrift://challenge/<code>` through the import dialog and its `VIEW` intent. The UI trims surrounding pasted whitespace, extracts one URI path segment, and rejects query strings or fragments before parsing. `MainActivity` handles both a new launch and `onNewIntent` when the activity is already visible. In a friend session, `C` resolves to `get`, `R` to `remix`, and `E` to the existing `event` generator. The saved session retains the code and replays the validated move history against that same family and value. A friend may open a locked board, but that session cannot unlock progression or award coins, XP or mission credit; consumable powers are disabled.

A checksum catches copying errors and many alterations. It does not authenticate the sender or establish a verified score. A code contains no user identifier, reward, leaderboard result or saved run. Shared challenges must not modify campaign unlock state or provide repeatable reward farming.

## Reproducible validation

Run `scripts/test-engine.sh` with JDK 17. The dependency-free suite checks:

- All 240 original boards and the pre-change content fingerprint.
- All 240 Remix boards, their content fingerprint, distinct signatures and structural constraints.
- 1002 existing event seeds and 1002 arbitrary paired-switch seeds, including both `long` extremes.
- Every certified exit, exact gate transition, undo, restart, save replay and corrupt-save atomicity.
- Exact hints and alternative legal starts, including losing choices that correctly require undo.
- A dedicated paired-switch fixture that opens one gate and closes another in the same action.
- All original/Remix challenge codes, 1007 event round trips including `Long.MAX_VALUE`, malformed/overflow/tamper cases and 10000 arbitrary parser inputs.

The recorded run completed **435314 assertions** over the 2484 boards/seeds, including **1543 alternate legal starts**, of which **53** correctly required undo. Runtime on this development machine was **5.67 seconds**. See [the captured test output](validation/engine-tests.txt). This is engine correctness evidence; Android input, layout, audio, install/update and physical-device performance require their own checks.

For emulator interaction, `EngineFixture remix 7` prints the board size and certified tap positions. Existing original/event fixture arguments remain supported.
