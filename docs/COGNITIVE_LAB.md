# Cognitive Lab — local synthetic player study

The lab compares **100 parameterized synthetic profiles** on the current Luma
Drift puzzle engine. It measures the behavior of these agents under declared
assumptions. The Android playtest release remains **1.21.0-rc1 / code 29**.

[Interactive FR/EN report](https://luma-drift-production.web.app/lab/cognitive/index.html)
· [Recorded outputs](validation/cognitive-lab/latest/summary.json)
· [Source and data archive](../public/lab/cognitive/cognitive-lab-local.zip)
· [Site payload manifest](validation/cognitive-lab/site-manifest.json)

## Run locally

Requirements: a Java 17 JDK, Python 3.9+ and a POSIX shell. The runner uses the
repository's actual Java puzzle engine and an in-memory pacing ledger. It does
not start Android, call a provider, load an LLM or send gameplay data anywhere.

```sh
bash scripts/test-cognitive-lab.sh
bash scripts/run-cognitive-lab.sh
python3 -m http.server 8794 --bind 127.0.0.1 --directory public
```

Open `http://127.0.0.1:8794/lab/cognitive/index.html`. The website displays the
published snapshot. After a new local run, import its report in the dashboard or
prepare a new snapshot with `python3 scripts/package-cognitive-lab.py --prepare`.
Preparation verifies the earlier site assets; deployment is a separate action.
The runner's help documents shorter smoke runs and custom output directories.

## What a profile means

Each profile has a stable identifier and explicit parameters for planning,
memory, attention, learning, forgetting, fatigue, frustration, available time and
reward salience. These inputs are engineering hypotheses, not fitted estimates
for the human population. Sampling 100 combinations increases coverage of the
model; it does not create 100 independent human observations or a representative
market sample.

The optional IQ label is **hypothetical and uncalibrated**. It is not the result
of an intelligence test and does not diagnose a person. Decisions must depend on
the explicit agent parameters. Cognitive measures describe agent performance
and internal model state, not changes to a human's intelligence, health or
general cognitive ability.

Reward sensitivity, continuation, stopping and session overruns are modeled
separately. Clinical gaming disorder involves impaired control, increasing
priority and continued play despite negative consequences; this lab does not
diagnose it. [WHO definition](https://www.who.int/news-room/questions-and-answers/item/addictive-behaviours-gaming-disorder).

## Decision and execution boundaries

The environment executes actual `RelayCatalog`, `Board` and current budget
rules. The agent receives a visible-state description and uses bounded
reasoning. Its decision code cannot read a certified plan, an optimal solver,
minimum remaining actions or hidden game-state annotations.

Exact in-game help is permitted through the first-board coach, the native
dead-end warning and explicitly requested earned Focus. The environment records
the source and use of each cue. Such assistance is counted
separately from unaided decisions. The evaluator can use reference information
for checking outcomes, never to silently repair an agent's move.

This is a headless puzzle/economy study. Training uses the current U1–U400 catalogue; the held-out
geometry probes are U401, U573, U721 and U1000. Each primary trajectory spans eight
modeled days, with at most one available session per day. The session-energy
ledger matches the native regeneration/capacity rules but does not execute
Android persistence, clock rollback or purchase receipts. Coins start at an
explicit hypothetical 180; the real economy curve supplies eligible rewards and
Undo prices. Inventory powers, quests, daily-login gifts, companion skills and
ownership/evolution/navigation are excluded. Collection/exploration preferences
enter the declared motivation formula only.

The lab does not validate a rendered menu,
phone touch targets, audio perception, accessibility, emotional attachment to
the artwork, real companion choice, social behavior, advertisements, payments or
store publication. Any excluded gameplay subsystem is listed in the generated
scope record and the run notes below.

## Experiments

The full study uses 100 profiles, 30 repetitions per profile and paired random
inputs across three scenarios. Scenario changes concern the synthetic agent's
assumptions; they are not Android features delivered by this lab.

| Scenario | Change under study |
| --- | --- |
| Current model | Declared baseline learning, motivation and fatigue model |
| Teaching support | Hypothetical improvement to rule learning |
| Reward muted | Hypothetical reduction in reward salience |

Matched controls, held-out transfer and parameter sensitivity check whether a
result is specific to one mechanism or a fragile consequence of the assumed
formula. Identical random inputs support paired comparisons; different action
paths are allowed to diverge. Repetitions quantify stochastic variation within
the model. They do not eliminate parameter uncertainty or establish human
predictive validity.

Useful measures include solved attempts, blocked actions, excess actions,
assistance use, progression, resource balances, modeled session length,
stopping reasons, learning state, transfer and time overruns. A modeled next-day
or day-seven return is an output of the assumed scheduling and utility model.
It must never be reported as observed human retention or used as proof that the
game can reach 100 million people.

The methodological separation between believable agents, task agents and human
proxies matters: each supports a different claim and needs its own validation.
See [Karetnikov et al., 2026](https://doi.org/10.1038/s43588-026-01060-3) and
[Lin, 2026](https://doi.org/10.1177/25152459251410153). These references motivate
validation boundaries; they do not calibrate the numeric profile parameters.

## Evidence and reproducibility

The output records configuration, seeds, source hashes, per-trajectory and
per-level outcomes, selected actual decision traces and paired comparisons.
Profile repetitions remain grouped when interpreting differences. Reports must
keep between-profile heterogeneity, within-profile randomness and assumptions
separate. Raw CSV files are retained in compressed form when appropriate.

The old [1.9 audit](PLAYER_AUDIT_2026.md) is historical evidence. Its main profile
policy repeatedly started from a solver plan and ran an older campaign. Those
results are not relabeled as cognitive-agent results or compared as if they
used the same model.

The new site manifest preserves every earlier Hosting payload except the
intentional homepage link addition. Android packages, accepted native captures,
account pages and historical release receipts retain their identities. Local
browser checks, GitHub CI and Firebase management receipts are recorded
separately; none silently substitutes for public-origin observation.

## Recorded run and delivery

The completed run, validation counts, observed limitations and publication
receipts are recorded in [the delivery report](validation/cognitive-lab/README.md).
