# Versioned runners

The original `CognitiveLab` runner and historical output data remain available.
`CognitiveLab122` adds explicitly exposed cause feedback without starting
knowledge or learning multipliers, actual six-family companion operations,
complete blocking records, corrected day-stop attribution and a separate native
shared-charge tactical chapter. See
[`docs/validation/cognitive-lab-v122/README.md`](../../docs/validation/cognitive-lab-v122/README.md).

Use `scripts/run-cognitive-lab-v122.sh` and `scripts/test-cognitive-lab-v122.sh`.
The documentation below describes the original experiment.

# Local cognitive hypothesis laboratory

Run from the repository root:

```sh
./scripts/run-cognitive-lab.sh
./scripts/run-cognitive-lab.sh --smoke --output /tmp/luma-lab-smoke
./scripts/run-cognitive-lab.sh --help
./scripts/test-cognitive-lab.sh
```

Requires Java 17+ and Python 3 for checks. No network, model API, human data, Android device, or paid service is used. The wrapper selects `JAVA_HOME`, Homebrew Java 17, or the current Java installation, in that order. Outputs default to `docs/validation/cognitive-lab/latest`.

## What the model means

The default run has **100 synthetic profiles × 30 repetitions × 3 scenarios × 8 modeled days**, plus 800 matched ablation trajectories and 12,000 paired held-out probes. It measures consequences of declared assumptions. It does **not** estimate real human retention, intelligence, enjoyment, addiction, or acquisition.

Profiles independently stratify planning depth, search budget, working memory, attention lapses, learning, forgetting, fatigue, frustration sensitivity, time availability, reward sensitivity, and three interests. These are arbitrary hypothetical ranges, not human distributions. A separately shuffled fictional IQ-like label has no causal role; a test changes only this label and checks that decisions stay identical.

Every profile starts with 180 Coins and a fresh U1 journey. Only first-clear rewards and paid Undo change the Coin ledger. Inventory, companion skills, cosmetics, missions, tutorial-completion grants, login rewards, purchases, ads, multiplayer, and social acquisition are outside this experiment. Collection and exploration preferences are utility terms, not simulations of the corresponding product screens.

## Decision and engine boundary

`CognitiveAgent` receives a projection of visible pieces, gates, barriers, shell layers, remaining actions, affordability of Undo, and native rendered assistance. It has no `Board`, `Level`, catalog, certificate, or solver reference.

For each decision, probabilistic rule recognition depends on learned rule knowledge. The agent searches its own approximate model, bounded by planning depth, working memory, and a strict node cap. It may overlook gates, blockers, switching consequences, or shell tap burden. Recent blocked choices and undone choices occupy bounded memory. Attention can instead select a random visible piece. Fatigue affects duration, attention, planning, and continuation utility. Feedback updates rule knowledge; overnight forgetting and recovery apply even on days without play.

`Environment` commits transitions through the actual `Board`, `MoveBudget`, `ElementalShell121`, and `ExitChain121` classes. Policy 6 rejected taps are free; productive exits consume an action; Undo never refunds that action. Shell taps are free, happen before path legality, take modeled time, survive Undo, and reset with a new attempt. The first two Undos of an attempt are free in Coins; subsequent ones use the actual `EconomyCurve121` price.

The native game intentionally provides some exact help. The simulation models the U1 first-two-exit coach, the rendered dead-end warning, and the explicitly requested earned Focus after three distinct exits. Exact help remains inside the environment; only the UI cue reaches the agent. Focus lasts eight modeled seconds and is usable once per attempt. The selected traces label the cue source, while every attempt records cue exposure counts. No evaluation suffix or hidden solvability score is supplied to mental search.

The in-memory energy ledger mirrors native eligibility, initial quantity, refill, and capacity behavior. It excludes persistence, clock rollback, receipt recovery, and purchases. The test compares it against the actual `SessionEnergyStore` over 5,000 randomized events; Coin entitlements and paid Undo are compared against the actual `GameStore`.

## Sessions and comparisons

Day 0 starts are forced. Later modeled gameplay sessions arise from exogenous availability and a declared utility function. Continuation combines interests, recent outcomes, reward salience, frustration, fatigue, and elapsed/planned time. There is no fitted or targeted D1/D7 value. Reward-muted scenarios change assumed salience, not actual Coin rewards. Overrun is time beyond the planned session, not an addiction diagnosis.

Scenarios share profile, repetition, availability, and keyed decision seeds. `teaching_support` adds hypothetical starting knowledge and feedback learning, with a modeled 12-second explanation at U1/U11; `reward_muted` scales utility reward salience to 0.15. These interventions are hypotheses, not shipped changes to the app.

Four paired controls remove learning, remove fatigue, limit planning, or remove reward utility. Controls use the first 20 fixed profiles and first 10 repetitions. Held-out probes use U401/U573/U721/U1000; they compare the same profile with cold versus final trained knowledge, resetting fatigue, frustration, memory, and Coins. Native warnings and free Focus are available to both conditions. Thus probe success includes declared native assistance.

The main campaign scope is U1–U400. A configuration guard proves its productive-exit minimum exceeds the entire 960-decision horizon. Consequently the cap cannot create artificial later returns. Only a day with an actual decision counts as a modeled gameplay session. Active attempts persist across modeled days and are marked horizon-censored at the end.

Paired interval estimates resample whole profile mean differences 1,000 times. They describe sensitivity within the synthetic experiment, not a human population confidence interval. A weak or absent planning effect is a valid outcome; more planning is not forced to win.

## Outputs

| File | Contents |
| --- | --- |
| `summary.json` | Scope, configuration, metric definitions, scenario and profile × scenario aggregates |
| `profiles.json` | Exact generated parameters and explicitly noncausal fictional labels |
| `paired-comparisons.json` | Paired differences, profile-cluster bootstrap intervals, held-out probes, controls |
| `traces.json` | Up to 100 selected real decisions per scenario, with attempt identity, seed, masks, shell layers, before/after state, candidates and cue source |
| `report.json` | Single-file combination of the four JSON datasets for dashboard import |
| `trajectories.csv.gz` | Every main profile × repetition × scenario outcome and conservation ledger |
| `level-results.csv.gz` | Every main attempt, including failed, retried and horizon-censored attempts |
| `provenance.json` | Source hashes, deterministic output hashes, separately labeled host runtime |

Decompress raw records with `gzip -dc FILE.csv.gz`. `completionRate` is the mean per-trajectory fraction of started attempts that complete before the horizon, not the fraction finishing U400. `blockedRate` averages each trajectory's blocked taps divided by all decisions, including shell taps, Undo and Focus. D1/D7 are day-index gameplay indicators, not continuous retention. All modeled timestamps are arbitrary day indices; host elapsed time appears only in provenance.

The full JSON and compressed CSV payloads reproduce byte-for-byte for an identical source tree and effective configuration. Provenance has a variable runtime field and is intentionally excluded from the byte-identity assertion. The test performs two complete smoke runs, verifies seven deterministic payloads, checks CSV schemas and all three trace scenarios, and verifies native mechanics on all 1,000 U boards.
