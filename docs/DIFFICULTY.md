# V4 difficulty: productive costs and measured limits

## Current 1.8 route

Default progression is **1,000 L Journey and 240 M Remix boards**, with T events from an audited geometry pool. J/K and earlier families remain preserved. V4 adds a one-action manual gate opening, an exact productive-cost allowance and free/refunding Undo. Its budget is no longer only a tile count plus penalties for mistakes. [Rules and APIs](PROGRESSION_V4.md).

Every reached L/M/T state is **logically recoverable**. An inefficient sequence can still exceed its remaining budget and require Undo/refund or free restart. These properties are separate. The measurements below do not establish human pass rates, a Candy Crush balancing algorithm, enjoyment, retention or revenue.

## Frozen content metrics

| Measure | L/M result |
|---|---:|
| Boards | 1,240 |
| Exact productive minimum | 5–28 actions |
| Initial allowance | 10–31 actions |
| Boards requiring manual openings in an optimal plan | 1,220 |
| Key/seal boards | 591 |
| Sealed cells | 1,367 |
| Reauthored switch masks | 8,830 |
| Boards with a measured optimal-route decision affecting cost | 1,161 |
| Spatial/key dependency depth | 1–13 |

The first ten boards of each route need no manual opening: 1–5 teach exits/blockers, 6–10 keys/seals. Boards 11–20 add required openings and switches gradually. The [per-board metrics](validation/v1.8/engine/strategy-authoring.csv) report actual costs rather than infer them from a label.

| Minimum required openings | Boards |
|---:|---:|
| 0 | 20 |
| 1 | 167 |
| 2 | 333 |
| 3 | 354 |
| 4 | 223 |
| 5 | 83 |
| 6 | 43 |
| 7 | 13 |
| 8 | 4 |

Margins are fixed: +5 at 1–10, +4 at 11–20, then +2 to +4. Layouts and budgets do not adapt to payment, ads, failures, elapsed time or individual profiles. Different productive routes can cost more than the minimum; refunded Undo removes an action, while unrelated blocked-attempt charges stay spent.

## Structural score and comparative labels

The offline tool follows one fixed optimal action certificate. At each prefix it evaluates legal exits and closed-gate openings that immediately enable a spatially free piece, using the exact remaining cost.

| Symbol | Metric |
|---|---|
| D | Certificate steps with several productive choices, including a more expensive completion |
| A | Exact minimum productive action count |
| G | Minimum necessary openings, A minus piece count |
| K | Distinct linked key pieces |
| P | Largest extra completion cost from one alternative at a certificate prefix |
| H | Longest spatial/key dependency chain |

```text
score = round(100 × D / A) + 3G + 2K + 2P + H
```

`forced_optimal_steps` separately counts prefixes with exactly one optimal next productive action. Scores range from **1–124**. After the first 20 teaching boards, Normal is below 80, Hard is 80–109 and Expert is 110 or more. The current route contains **644 Normal, 551 Hard and 45 Expert** boards. First-20 teaching overrides and mechanic introductions are explicit authoring choices, not fitted human ratings.

Introductions have their own beat. Recovery requires a measured score below 80% of the preceding board; otherwise a Hard/Expert board is a Peak and another board Practice. These descriptors are frozen from their metrics rather than assigned solely by level-number slots. The margin schedule remains a separate design choice.

Weights, thresholds and reference certificate are project heuristics. Its decision counts are **not every possible human decision** and can differ on another equally optimal route. A forced action can be obvious or difficult to discover. Exhaustive cost correctness does not validate these coefficients as a model of cognition or enjoyment.

## Exhaustive validation and runtime bounds

[Final engine evidence](validation/v1.8/engine/README.md) records:

- **773,092,810 assertions** across all 1,240 current boards;
- **13,254,058 reachable action states** and **57,403,554 legal transitions**, including openings;
- independent bottom-up evaluation of **31,230,944 gate states**, agreeing with runtime exact costs;
- every reached physical preview and Undo, plus 13,839 sampled complete typed plans/save replays;
- geometric uniqueness, policy migration, malformed saves and zero-action refunds;
- all 980 selectable T geometries and extreme signed seeds, alongside preserved-family regressions.

T maps to L21–L1000, placing every possible event geometry inside the audited finite closure. This is not every 64-bit seed individually enumerated or a unique new layout for each date.

The board-local runtime cache caps at 524,288 entries. The audit observed 279,897 entries and 9,437,184 bytes of steady arrays at peak; growth can temporarily retain old/new arrays. Its worst individual cost call was **33.89 ms on the development host**, with **50.18 seconds** for the final exhaustive run. These are not Android latency, frame rates, total app memory or low-end-device guarantees.

UI guidance searches an isolated typed-history snapshot in a single background worker and discards stale generations. Frames do not solve the board repeatedly. Pending results show a thinking state and cannot consume a power. Installed interruption, accessibility and physical-device latency have separate acceptance.

Reproduce with JDK 17:

```sh
./scripts/test-strategy-v4.sh
```

[Frozen metadata](../app/src/main/java/com/androidhit/lumadrift/engine/StrategyV4Layouts.java) regenerates byte-identically through the explicit offline tool. Normal play does not regenerate, sort or personalize the progression.

## Historical measurements

The [1.7 review](LEVEL_MANAGEMENT_REVIEW.md) reproduces why solvable J7/J8/J13 starts could become lost after legal exits. Its uniform-legal probabilities are neither player rates nor V4 results. J/K retain those old interactions for code compatibility; L/M/T changes recovery rather than relabelling the old metric.

Older evidence retains its scope: [V3 summary](data/progression-v3-summary.md), [V2 summary](data/progression-v2-summary.md), and the [original 480-board score audit](data/difficulty-summary.md), [chart](data/difficulty-curves.svg) and [rows](data/difficulty.csv). Their scores, path samples and adjacent-change improvements are not current V4 measurements. The preserved [difficulty test suite](../scripts/test-difficulty.sh) is separate from the current strategy audit.

## What still needs players

Observe whether first-time players predict the preview, distinguish an exit arrow from switch targets, find Undo at zero and understand why productive routes cost different amounts. Record actual family/code, budget policy, device and retry history when investigating a reported level: “level 7” alone can refer to several preserved boards.

Measure confusion, blocked attempts, voluntary recovery and abandonment before concluding that the curve works. Wait for complete cohorts before retention claims. [Player-study protocol](HIT_AUDIT.md). Technical solvability and presentation do not substitute for those observations.
