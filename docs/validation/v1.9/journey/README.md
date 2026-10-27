# First-player audit: tutorial through Journey 100

This is an **engineering audit with synthetic action policies**, using the real application engine and progression stores. It is not a study of human players and does not predict retention, enjoyment, spending or success among a generation. The accompanying [2026 research and collection limits](../../../research/2026-new-player/README.md) motivate which contexts to cover; the numeric error, help, session and retry parameters are declared assumptions in [profile-anchors.json](../../../research/2026-new-player/profile-anchors.json).

## Reproduction and evidence

Run the same current harness against the immutable 1.8 source and the revised working tree:

```sh
bash scripts/test-new-player-v19.sh baseline-39908e2 docs/validation/v1.9/journey/baseline-39908e2 39908e2
bash scripts/test-new-player-v19.sh candidate
python3 scripts/NewPlayerV19Report.py
bash scripts/test-guardian-v19.sh
```

The baseline runner extracts only Java application sources from commit `39908e2`; it does not replace files in the checkout. Both versions compile the same audit and the same Android preference adapters. Each report includes input hashes. The candidate run must be refreshed after application policy changes, before packaging the release.

| Evidence | What it measures |
| --- | --- |
| [Baseline summary](baseline-39908e2/summary.json), [candidate summary](candidate/summary.json) | Passed contracts, actual simulated attempt and interruption counts, host execution time |
| [Level measurements](candidate/levels-1-100.csv) | Exact productive minima, budgets, keys, seals, switches, choice metrics and structural pacing for every level |
| [Paired comparison](comparison.json) | Invariance of geometry/budgets/action outcomes; actual reward and score differences |
| [Independent attempts](candidate/synthetic-attempts.csv) | Every seed, level, action policy, exhausted budget, remaining work and grade |
| [Policy totals](candidate/synthetic-policy-totals.csv) | Aggregates of the 16,000 independent attempts |
| [Sequential journeys](candidate/profile-journey-totals.csv), [attempt detail](candidate/profile-journey-attempts.csv) | 72 complete or bounded-stop tutorial-to-100 journeys, with real stock, earned-coin purchases, recovery, rewards and cold reconstruction |
| [Reward ledger](candidate/actual-reward-ledger.csv) | Real GameStore/MetaProgress rewards, inventories, trials and progress for 1-, 2- and 3-star clears |
| [Tutorial phase trace](candidate/tutorial-steps.csv) | Actual requested actions, lesson state, remaining pieces and bounded save representation |
| [Guardian oracle](guardians/summary.json) | Separate new encounter engine, including exhaustive productive states, work lower bound, undo and repeated-piece-ID saves |
| [Optional Guardian profile complement](guardian-profiles/README.md) | Separate 720 scheduled Guardian 1–10 encounters, six synthetic behaviours, real unlock boards, cold reconstruction and explicit forced recovery edges |

`baseline/` is the initial exploratory engine/reward run. `pilot-39908e2-help-id/` retains a later harness pilot whose hint request identifiers included a decimal point and were therefore rejected by GameStore. That pilot still exercised the engine, but did not cover the intended stock-hint branch. A subsequent pilot encountered a bounded-loop assertion: after spending too many blocked probes, full Undo cannot refund those failed attempts. The final harness uses valid receipt IDs and restarts such a run rather than repeatedly undoing and replaying one action. These are harness corrections, not evidence that the game granted free currency or that 1.8 had been improved at those points.

## Findings and corrections

The first ten boards introduce arrows and keys without switch-order penalties. The mean structural score rises from 2.2 at levels 1–5 to 18.1 at 11–20, then 52.1 at 21–30. Level 23 has an adjacent score increase from 17 to 85. This score measures authored dependencies and gate choices; it is **not** a calibrated probability that a person fails. Later large jumps also exist, so a transparent briefing, readily replayable mechanics lesson and visible recovery matter more than declaring every neighbouring board equally hard.

All 100 shortest certificates fit their original budgets without a power, advertisement or purchase. In 1,600 independent seeded attempts each, legal random choices finished 1,583 and the one-step heuristic finished 1,589. A guided Undo plus exact-plan control finished all 1,600. Adding blocked exploratory probes shows sensitivity to the assumed probe frequency: at 5%, 15% and 30%, the corresponding exploratory policy finished 1,494, 1,171 and 646 of 1,600 attempts. Those percentages belong to scripted actions, not surveyed players. Recreating the board and budget after interruptions produces the same outcomes.

The baseline rewarded tutorial completion with hints but removed the third star when any hint was used. The revised public `JourneyExperience` policy grades the consumed action budget, with a visible threshold; time, hint count and Undo count do not enter it. Undo refunds the reversed productive action, while failed blocked probes remain charged. The baseline runner extracts and compiles the exact grading expression from the archived `finishLevel` source, recording its expression and source hash; the candidate calls the public `JourneyExperience` implementation directly. It does not maintain a separate copied economy or score model.

Baseline stock also exposed a long gap between a new power becoming usable and a first free copy: Shield was available entering level 25 but first appeared in five-clear loot at 40 clears; Focus at 45/50; Trail at 75/90. The revised `claimPowerTrial` transaction offers one explicitly claimed unit per power at its real unlock, with a persistent receipt and no clipping when storage is full. The ledger records the claimed trial before the entering level, including the zero-clear Hint trial. Five-clear loot has a stable, named preview so the displayed next goal remains the item that can actually be collected.

The complete seven-lesson practice contains 27 real actions and explicit transitions. This JVM trace cannot discover a finger animation masking a native touch target. The separately recorded Android regression for lesson 4 and compact layouts is required alongside this audit; a successful accessibility click cannot substitute for a real touch gesture.

## Simulation method and honest limits

The 16,000 independent trials use six policies: shortest-plan control; legal random choice; one-step physical preview; exploratory touch; the same touch policy with forced save/recreate; and a guided recovery control. Three probe frequencies are used for each of the two exploratory policies, with 16 seeds for each policy/level combination. Legal choice omits gratuitously opening irrelevant gates when a piece can already exit; when no exit exists it opens a closed gate needed by a physically clear arrow. The heuristic scores an immediate exit, the resulting count of legal exits and a small key preference; it never searches future states. The exact solver is used for the explicit control/help branches and final measurement, rather than quietly rescuing every policy.

The 72 sequential journeys cross six behaviour profiles, sensitivity multipliers 0.5/1/1.5 and four seeds. The same seed stream is used before/after. A profile follows a reference shortest action with declared deviations and blocked probes; this is a controlled perturbation test, not a model of a person's reasoning. At the declared help threshold it reserves and consumes a real Hint unit; if necessary, it buys a real Hint pack with previously earned coins. It uses free Undo when the remaining exact work no longer fits, then free restart if failed probes made even a full rewind insufficient. The retry bound is a test stop rule, not measured churn. No advertisement, LumaGold or purchase with real money is used.

Every ten clears advances the injected calendar by one day for daily/weekly reward rollover coverage. Session interruptions use the profile's declared window and assumed action latency; ranges in the reports are **modelled action time**, not measured human session length. Menu-reading counts for the collector exercise ownership reads only; they neither create friends nor send a gift. Adult Gen Z 18+ and older-adult 50+ labels may be crossed with every profile using identical inputs. Repeating a label would not create new independent observations, so results are not duplicated and no demographic completion ranking is published.

Progression is stored through real `GameStore` and `MetaProgress` with small in-memory Android preference adapters. Cold reconstruction copies the persisted maps into new context/store instances; it does not validate Android storage latency, Google identity, online friendship, backend gift delivery or rendering. Human playtests are still needed for clarity, comfort, perceived challenge, pleasure and voluntary return.
