# Optional Guardian 1–10: six synthetic behaviour profiles

This complement is **separate** from the paired tutorial-to-100 Journey audit. Those 72 Journey runs did not include optional Guardians. Here, six profiles × three sensitivity multipliers × four seeds × ten Guardians produce **720 scheduled optional encounters**, using real `GuardianEncounter`, `Board`, `MoveBudget` and `GameStore` implementations. No application source or existing first-100 evidence was changed to produce this complement.

Run:

```sh
bash scripts/test-guardian-profiles-v19.sh
```

The runner snapshots application Java sources before compilation. [source-inputs.json](source-inputs.json) records the tested hashes, including the [2026 research profile anchors](../../../../research/2026-new-player/profile-anchors.json); [report-hashes.json](report-hashes.json) records output hashes. [profile-inputs.tsv](profile-inputs.tsv) uses the same assumed parameters as the main first-player audit. Aggregate 2026 publications motivate coverage dimensions only. They do not calibrate the probe frequencies, help threshold, session windows or ability of a young or older player.

## Results

The [summary](summary.json) records **183,733 passed assertions**, all **720 encounters actually solved**, **580 blocked probes** including **407 blocked one-cell pieces**, **1,656 layer peels**, **6,696 one-cell steps**, **4,320 genuine exits**, **180 free-help activations** and **791 cold restorations**. Of the restorations, 720 are mandatory checkpoints after two productive hits and 71 occur at the modelled session boundary. **467 restores contain repeated piece IDs**, exercising the layer-hit/step save format.

| Synthetic behaviour | Encounters solved | Blocked probes | Free-help activations | Cold restorations |
| --- | ---: | ---: | ---: | ---: |
| Practising novice | 120 | 109 | 43 | 132 |
| Impatient skipper | 120 | 174 | 39 | 131 |
| Careful explorer | 120 | 72 | 22 | 132 |
| Interrupted commuter | 120 | 103 | 38 | 156 |
| Independent planner | 120 | 19 | 0 | 120 |
| Social collector | 120 | 103 | 38 | 120 |

These labels describe scripted behaviour, not people, survey samples or demographic averages. Adult Gen Z and older-adult labels may be crossed with every row using identical settings; no age-specific mechanics, rewards or predicted completion rates are introduced.

Each of the 72 synthetic sessions actually solves 100 V4 `Board` certificates under real budgets before calling the corresponding `completeLevel` transactions. Thus **7,200 real JVM board completions** establish the ten-clear unlocks. A Guardian reward request is made only after its encounter is solved. Each first clear returns exactly 100 LumaCoins; duplicate, independent-handle and cold-replayed requests return zero. Across all synthetic sessions, the 720 receipts grant 72,000 earned coins in total. Every session ends at 6,680 LumaCoins: initial 180 + 100 three-star Journey clears at 55 + ten Guardian rewards at 100. Free help and the simulation never invoke an advertisement, a paid wallet, a real-money purchase or a stock Hint debit.

## Method and recovery edges

Before assistance, each selection follows the current exact reference plan with the declared off-plan probability selecting another legal productive piece. Every legal Guardian action reduces true remaining work by one, so such alternative legal orders remain shortest. The separate assumed probe probability deliberately selects a currently blocked piece when one exists; otherwise it selects a present piece and records its actual outcome. This is a targeted error-coverage policy, rather than an estimate of accidental mis-taps or a model of a person's understanding.

Every action checks the physical outcome: the first hit on a two-layer piece peels in place; a footprint piece advances exactly one cell; an exit removes the piece; a blocked attempt spends one action without reducing work. The action log does not label a successful layer peel a human mistake or infer frustration from it. At the assumed help threshold, free guidance becomes available and the scripted policy follows it. A failed attempt would use the declared free-restart bound and the already available free guidance. Cold checkpoints serialize the bounded canonical encounter history through an isolated test-session preference adapter, reconstruct it and the real wallet into new instances, and compare every coordinate, layer, counter and receipt.

The 720 profile-driven encounters required **zero restarts and zero natural zero-budget undos** under these assumptions. The report preserves that zero rather than inventing exhausted players. To cover recovery explicitly, [forced-recovery-edges.csv](forced-recovery-edges.csv) contains **18 separate deterministic boundary cases**: all six coverage labels crossed with Guardians 3, 5 and 10. Each reaches a real front/back dependency, deliberately spends blocked probes to zero, undoes one successful action, verifies that only that action is refunded, cold-restores the exact record, restarts for free and completes a real plan. A further fixed contract independently checks the same zero-budget rule. These forced cases are **not** counted as additional behavioural encounters or demographic observations.

## Evidence and limits

- [encounter-attempts.csv](encounter-attempts.csv): every scheduled encounter and actual attempt, mechanics, help, recovery, reward and remaining work.
- [actions.csv](actions.csv): each selected piece, before/after coordinates and layers, work, budget, result and repeated-ID history.
- [cold-restores.csv](cold-restores.csv): checkpoint reason, canonical history, work, consumed actions and distinct/repeated IDs.
- [profile-totals.csv](profile-totals.csv): each profile/sensitivity/seed session and its real wallet outcome.
- [forced-recovery-edges.csv](forced-recovery-edges.csv): isolated exhausted-budget recovery contracts.

Session windows and action latency determine artificial interruption boundaries; host execution time is neither human playtime nor Android input latency. The test-session adapter validates serialization and store reconstruction, not the installed activity's save keys or native touch flow. Native lesson gestures, Guardian hit feedback, thickness, avatar appearance, real Google accounts and friend/gift traffic require their own evidence. Nothing here measures enjoyment, voluntary return, D1/D7, commercial retention or a clinical effect.
