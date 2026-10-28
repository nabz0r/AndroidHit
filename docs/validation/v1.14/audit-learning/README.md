# N Journey — RC2 content qualification

This folder describes **1.14.0-sandbox-rc2**, frozen N Journey source SHA-256 `0a6de06054a377d37b5b8ab1e2374cdbe694c03983adeb1b5714f86aec54dafc`. It supersedes the RC1 content reports. [The complete RC1 source and reports](../audit-learning-rc1/) remain preserved with their original identity; native RC1 runs do not qualify RC2.

N1–N1000 use progression version5 and independently stored layouts. The first30 authored rows are byte-identical to archived RC1. The remaining970 boards preserve each L31–L1000 physical base exactly once: size, piece coordinates, arrows, switch masks and key/seal associations. **291 initial gate masks are reauthored by opening additional gates**; they are not identical initial states. Each N initial mask is a superset of its old L mask and is reached in L by those gate openings alone. The original L/M/T identities and layouts are unchanged.

## Learning sequence

| N levels | Lesson | Bound on new mechanics |
|---|---|---|
| 1–4 | Exits | 4×4, two to five independent exits, all gates open |
| 5–8 | Spatial blockers | 5×5, five to eight pieces, no switch/key/closed gate |
| 9–12 | Key and seal | One key and one seal, all gates open, no switch |
| 13–16 | Manual gate | One required opening, no key or switch; N15 has five pieces/six minimum actions |
| 17–20 | Simple switch | One switch changes one gate, no key |
| 21–24 | Switch order | Two simple switches, no key |
| 25–27 | Key plus switches | One key/seal and two simple switches |
| 28–30 | Combination | One key/seal, at most ten pieces and three simple switches |

`LearningCatalog.lesson(number)` exposes these phases for the UI. This metadata is not evidence that a first-time player understood them. N31 then offers two immediate exits, 12pieces, one required gate opening and13 minimum actions; N32/N33 require two/three openings. Every switch continues to change one gate.

## Composition and pacing

The generator globally matches all970 sources to fixed episode slots using exact minimum-cost assignments, then repairs adjacent transitions without relaxing phase caps. It reserves Normal boards throughout the route rather than consuming them in an early score sort. Thirty percent of post-introduction boards start with a legal exit: three per episode, totalling321/1000 with the introductions. Others still begin with a manual gate choice.

Hard/Expert quotas per10 increase from one in N41–60, to two through100, three through200, four through400, five through600, six through800 and seven through1000. N31–40 has zero, adjusting the total to the actual480 Hard/Expert sources. All97 episodes keep at least three Normal boards. All38 Experts appear after400. The maximum consecutive Hard/Expert run is four; every challenge episode finishes with a Normal whose structural score is at least20% below its preceding peak.

Difficulty labels follow the recalculated score thresholds: Normal below80, Hard80–109, Expert110+. No board is relabeled to manufacture a break. The actual totals are520 Normal,442 Hard and38 Expert. The [curve review](curve-review.md) and [full episode/window metrics](audit-curve.json) show rising medians by200-board tiers. A smaller local median dip at501–600 is retained and reported; there is no claim of per-level or every100-board monotonicity.

The nondecreasing mechanical envelopes are:

| Levels | Grid | Pieces | Simple switches | Keys | Seals | Minimum actions | Dependency depth |
|---|---:|---:|---:|---:|---:|---:|---:|
| 31–60 | 5 | 13 | 4 | 1 | 2 | 18 | 9 |
| 61–100 | 6 | 15 | 5 | 1 | 2 | 20 | 9 |
| 101–160 | 6 | 17 | 6 | 1 | 2 | 23 | 9 |
| 161–240 | 6 | 19 | 7 | 2 | 2 | 26 | 13 |
| 241–400 | 6 | 20 | 9 | 2 | 2 | 28 | 13 |
| 401–1000 | 7 | 20 | 9 | 2 | 3 | 28 | 13 |

Every transition after N30 is bounded to positive increases of35 structural points, two pieces, four minimum actions and two required gate openings. These are maximum increases, not target increases. The score combines measured cost choices along an optimal certificate, required openings, keys, maximum one-choice penalty and dependency depth. It is a project heuristic, not a calibrated estimate of cognition, win rate or enjoyment.

The content budget is a real limit: minimum +5 through16, +4 through100 and +3 later. Productive decisions remain meaningful on966 boards. The [branch audit](audit-learning-branches.csv) independently evaluates exact worst finite path lengths:910 boards have some legal path exceeding their initial budget,848 when gate openings must immediately enable an exit. These are existence properties, not failure rates or impossible boards. The first such path is atN28, after the individual mechanics have been introduced; under the exits-before-gates restriction it begins atN102. Blocked attempts, powers, Undo and extensions are excluded from those structural bounds.

## Executed qualification

[The RC2 contracts](audit-learning-contracts.json) pass **201143 checks**:

- All1000 boards remain unique under rotation, reflection, translation and reordered IDs, preserving linked key/seal associations.
- An independent bottom-up oracle evaluates24960432 gate states. Every initial minimum and published decision/forced/penalty/dependency score is independently checked after reauthoring.
- The actual runtime traverses all6052 reachable states and20429 legal transitions of the first30 boards, comparing previews, exits, openings and physical Undo to the independent model.
- All970 physical bases are compared field by field with preserved L; every changed initial state is reached in the real old board by opening only its additional gates.
- All1000 certificates execute in the N engine and roundtrip through typed history. Malformed histories are rejected atomically.
- Exact episode quotas, real score labels, maximum challenge run, late Normals, free-start variety, recovery after peaks, phase ceilings and every positive transition bound are asserted.
- [A second complete offline generation](audit-regeneration.json) produces a byte-identical frozen layout.

[Source identity](audit-source.json) records the generator, layouts and independent qualification inputs. Run `sh scripts/generate-learning-v114.sh`, then `sh scripts/test-learning-v114.sh`, then `python3 scripts/audit-learning-curve-v114.py` with the configured JDK. Generation is offline and deterministic; normal play does not regenerate or personalise the route.

These are content proofs. Budget/Undo policy, native explanations, migrations, signing, installation and device acceptance have separate implementation and qualification evidence. A native run of a known solution is not a first-time human playtest.
