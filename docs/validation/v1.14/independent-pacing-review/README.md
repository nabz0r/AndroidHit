# Independent RC2 curriculum review

**PASS for the agreed structural curriculum contract. No blocking content defect found before the RC2 build.** This is a source and finite-state review, not native RC2 qualification or a measurement of human difficulty.

Reviewed `LearningLayouts.java` SHA-256 `0a6de06054a377d37b5b8ab1e2374cdbe694c03983adeb1b5714f86aec54dafc`, the generator, its tests, `audit-curve.json`, and `curve-review.md`. Historical RC1 evidence remains archived and does not qualify RC2.

The reviewer [recomputed](PacingRecheck.java) the exact minimum, every decision/forced-action/penalty/dependency component, structural score, and difficulty class for all 1,000 loaded boards. The computation uses the finite-state obstruction/gate oracle in `AuditJourneyV114.Costs`, independently implemented from the runtime solver, and does not call the generator or its `choiceMetrics` method. It follows the actual stored certificate when calculating its path-dependent metrics. All **4,970 assertions passed**, with no disagreement against the app's published metadata. The [raw recomputation](recomputed.csv), [execution log](recomputed.txt), and [review data](review.json) are retained.

| Contract | Independently observed |
|---|---|
| Curriculum uses effective initial gates | All minima, score components and classes match after reauthoring; 291 of 970 inherited bases changed initial gates, always by opening extra gates |
| Recoveries remain available through N1000 | All 97 ten-board episodes from N31 contain exactly three starts with an immediately legal piece exit and at least three Normal boards |
| A peak has a real following recovery | All 96 episodes containing a Hard/Expert end on a Normal board with a free exit and score at most 63; the smallest peak-to-end score drop is 29.6% |
| No long wall of labeled challenges | Maximum consecutive Hard/Expert boards is four, versus 153 in the archived RC1 curve |
| Demand grows by 200-board stages | Median scores are 46 → 66 → 79.5 → 90 → 100; median decision counts rise 2 → 7 → 9 → 10 → 11.5 → 11.5 → 14 → 15 → 16 → 17 over successive 100-board windows |
| Bounded neighboring rises after N30 | Maximum increase: score 35, pieces two, minimum actions four, required openings two |
| Early challenge onset is deliberate | First Hard N49; first Expert N429. N31 bridges N30 with 12 pieces, minimum 13, one required opening and two immediately legal exits |

There are 520 Normal boards including the first 30 lessons, 442 Hard and 38 Expert. The pool after N30 therefore has 490 Normal boards. No Hard/Expert is obtained merely by changing a label: all such labels follow the recomputed thresholds of 80 and 110.

Two qualifications remain useful for later human playtests. First, the median score falls from 79.5 over N401–500 to 73.5 over N501–600; both windows still contain 50 Hard/Expert and have median 11.5 decisions. This is compatible with the increasing 200-board stages and does not recreate RC1's 200-board valley. Second, physical occupancy falls when the grid expands from 6 to 7: median occupied-cell fractions are about 51% at N301–400 and 37% at N401–500. Late demand comes from more consequential ordering choices, rather than continual visual crowding. Neither source metrics nor these labels prove a player's learning speed, enjoyment, win rate, or retention.

The repeated episode positions make the intended rhythm predictable. This is a reasonable structural release choice; human testing should check whether players experience the last Normal as relief and whether late switch ordering remains legible. Native RC2 execution, budget/Return persistence, device input, and the final gallery still require their separate RC2 proofs.
