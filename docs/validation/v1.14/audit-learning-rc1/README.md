# N Journey content qualification

The new, separately identified **N1–N1000** Journey uses progression version5 and self-contained frozen geometry. Its first thirty boards are explicitly authored; N31–N1000 use exactly the 970 distinct geometries from preserved L31–L1000, with freshly computed optimal certificates and a different offline order. L/M/T and earlier identities do not resolve through N.

## Learning sequence

| N levels | Lesson | Bound on new mechanics |
|---|---|---|
| 1–4 | Exits | 4×4, two to five independently exiting pieces, all gates open |
| 5–8 | Spatial blockers | 5×5, five to eight pieces, no switch, key or closed gate |
| 9–12 | Key and seal | One key and one seal, all gates open, no switch |
| 13–16 | Manual gate | One required opening, no key or switch; N15 is five pieces/six actions |
| 17–20 | Simple switch | One switch changes one gate, no key |
| 21–24 | Switch order | Two one-gate switches, no key |
| 25–27 | Key plus switches | One key/seal and two simple switches |
| 28–30 | Combination | One key/seal, at most ten pieces and three simple switches |

`LearningCatalog.lesson(number)` exposes these labels to the explanatory UI. This metadata does not itself prove players learned them. The rules begin producing real productive-cost choices at N17; the corpus contains **941 boards with measured alternative productive costs**.

## Mechanical envelopes after the introduction

Offline selection respects source bands before comparing structural scores. Within each band the deterministic scheduler targets rising score quantiles with small peaks/recoveries, while penalising abrupt increases in piece count, necessary actions, switch count and dependency depth. The tests enforce these phase ceilings:

| Levels | Grid | Pieces | Simple switches | Keys | Seals | Minimum actions | Dependency depth |
|---|---:|---:|---:|---:|---:|---:|---:|
| 31–60 | 5 | 13 | 3 | 1 | 2 | 18 | 9 |
| 61–100 | 6 | 15 | 4 | 1 | 2 | 19 | 9 |
| 101–160 | 6 | 17 | 5 | 1 | 2 | 21 | 9 |
| 161–240 | 6 | 19 | 6 | 2 | 2 | 26 | 13 |
| 241–400 | 6 | 20 | 9 | 2 | 2 | 27 | 12 |
| 401–1000 | 7 | 20 | 9 | 2 | 3 | 28 | 12 |

Every switch mask changes one gate. These are upper bounds, not a claim of monotonic difficulty. Recovery boards intentionally reduce complexity. All source layouts remain available under their original identities.

The first qualification measures maximum adjacent increases of **two pieces, four minimum actions and34 structural points**. The old L22→L23 increase was68 structural points. A structural score is a project heuristic on one exact certificate, not a model of cognition, enjoyment or failure rate.

The route retains genuine budget choices: [challenge onset](audit-challenge-onset.json) finds a legal over-budget branch on916 boards, or860 when gates must immediately enable an exit. The first appears at N28, after its component rules have been introduced. Under the exits-before-gates restriction the first is N74. These are existence properties, not player failure rates, and do not say a board is logically impossible.

## Executed checks

[Contracts](audit-learning-contracts.json) currently pass **191,673 checks**:

- 1,000 boards unique under rotation, reflection, translation and reordered piece IDs, preserving key/seal associations.
- 24,960,432 gate states evaluated by an independent bottom-up oracle. Its minimum agrees with every declared N minimum; exact maximum policy lengths remain in [branch rows](audit-learning-branches.csv).
- The actual runtime traverses every reachable state and legal transition of the first thirty boards: **6,052 states /20,429 transitions**, with previews, legal exits/openings and Undo compared against the independent model.
- All970 reordered geometries match the retained source board field by field: size, initial gates, coordinates, directions, switch masks and linked seals.
- All stored N certificates execute and restore through the actual N engine; malformed history is rejected atomically.
- Introduction isolation, bounded solver size, phase ceilings and positive structural-step limits are asserted.

[Authoring rows](audit-learning-authoring.csv) expose the actual data, rather than only labels. [Source identity](audit-source.json) records the authoring and test inputs. The retained L/M engine has its own [exhaustive regression](../audit-preserved-engine/strategy-all-states.txt).

Run `sh scripts/generate-learning-v114.sh` to reproduce the frozen catalogue, then `sh scripts/test-learning-v114.sh` with the configured JDK. Generation is offline and deterministic; normal play neither regenerates nor personalises difficulty.

Budget policy, bounded Undo, blocked-tap handling, native onboarding, save migration, package signing and device acceptance belong to their own implementation and qualification. These content checks do not establish their completion or replace first-time player observations.
