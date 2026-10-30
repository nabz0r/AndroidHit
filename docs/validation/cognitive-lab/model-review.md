# Independent cognitive model review

Review date: 2026-10-30. This is a source/host review of the new synthetic
laboratory, separate from native Android acceptance and human player research.

## Reviewed boundary

The reviewer read the current `RelayCatalog`, `Board`, `MoveBudget`,
`ElementalShell121`, `ExitChain121`, `JourneyExperience`, `EconomyCurve121`,
`SessionEnergyStore`, the relevant `LumaView` execution/help paths, and the new
profile, policy, environment, runner and dashboard source.

The agent does not import an engine `Board` or `Level`, a solver or a catalog
certificate. It reasons over a projection of visible piece geometry, gates,
shells and explicitly declared native cues. Native U1 coaching, the native
exact dead-end warning and a requested earned Focus are allowed assistance;
they do not establish unaided planning performance.

The execution adapter preserves current relay exits, free rejected taps,
non-refunding policy-6 Undo, two free Undo uses followed by earned-Coin fees,
attempt-local shells and the distinct-exit Focus quota. The session-energy and
Coin models are explicitly scoped in-memory approximations. They do not test
save recovery, account wallets, purchases or every source of game rewards.

The independently permuted IQ-like label has no causal role in decisions and
has no psychometric calibration. Rule knowledge, fatigue, frustration,
continuation and time overruns are declared synthetic model states. No source
review can convert them into measured human cognition, enjoyment, retention or
clinical addiction.

## Initial empirical checks

An independent smoke run produced 36 main trajectories and 1,132 attempt
records. Exact reconciliation of the compressed CSVs passed for started
attempts, completions, decision interactions, blocked taps, Undo, shell taps and
earned Focus uses. Coin conservation and policy-6 action conservation are also
guarded during execution.

The random-input coordinates include profile, repetition, modeled day, level,
attempt and decision position, with distinct availability, decision, duration
and stopping streams. Scenario identifiers are excluded from these streams.
Paired comparisons check matching profile/repetition identities and resample
whole-profile mean differences. Their intervals describe this synthetic
cohort and model, not a human population.

## Findings raised before publication

1. Selected traces initially lacked attempt identity and full observed state.
   The dashboard could merge separate retries as one path. The implementation
   owners were asked to add attempt/seed/state attribution and group retries
   separately.
2. The first campaign scope cap could be reached within the modeled horizon.
   Later days then counted zero-action sessions as modeled returns. The runner
   owner was asked to remove this administrative-censoring artifact before the
   recorded full study.
3. Dashboard wording initially described completed attempts as completed
   trajectories, described fatigue per action instead of per modeled minute,
   and implied selected trace records were exhaustive. The dashboard owner was
   asked to correct these meanings and state native assistance explicitly.

## Resolved findings and full-run verification

All three publication-blocking findings above were corrected before the
recorded full run:

- Traces carry attempt identity, deterministic decision seed, exact before and
  after piece masks/gates/budget, per-piece shell layers, before-decision
  cognitive state and the native cue source. The dashboard groups attempts
  separately and describes selected, potentially discontinuous records.
- The campaign scope is now U1–U400, with held-out probes U401/U573/U721/U1000.
  Clearing those 400 boards requires at least **3,094 actual piece exits**,
  already exceeding the **960 maximum decision interactions** allowed by the
  eight-day horizon, even before shell taps. The runner checks this bound.
  A session or day-specific continuation also requires actual interactions.
  The full run's maximum cleared level is 87; no administrative campaign-cap
  stop occurs.
- Completion and fatigue units were corrected in both dashboard languages.
  Native assistance and the exclusions are explicit. Shell-rule recognition
  now changes anticipated interaction burden; it is not just a passive label.

The reviewer independently reran `scripts/test-cognitive-lab.sh`: **28,139 host
assertions passed**, including 5,000 actual `SessionEnergyStore` parity events,
actual `GameStore` rewards across 1,000 levels and mechanics over all 1,000 U
boards. Two separate smoke runs produced byte-identical JSON and compressed
CSV payloads. These are JVM host assertions with Android test adapters, not
28,139 native-device assertions.

For the recorded full run, the reviewer independently read the compressed
raw data and checked:

| Check | Result |
| --- | --- |
| Main trajectories | 9,000 = 100 profiles × 30 repetitions × 3 scenarios |
| Attempt records | 305,510, exactly reconciled to trajectory starts |
| Decision interactions | 3,142,360, exactly reconciled |
| Blocked taps / Undo / shell taps | 159,010 / 47,962 / 1,061,141, exactly reconciled |
| Earned Focus uses | 3,586, exactly reconciled |
| Coach / dead-end-warning / Focus cue exposures | 18,000 / 55,114 / 6,014 |
| Selected traces | 300; 100 per scenario; unique attempt/step identities |
| Source hashes / output hashes | All 40 source and 7 output digests matched |
| Paired scenario means | All nine metrics independently recomputed from raw trajectories |
| Paired comparison units | 3,000 matched pairs and 100 profile clusters per main comparison |
| Additional controls | 800 trajectories, using 200 pairs / 20 clusters per ablation |
| Held-out transfer | 12,000 pairs on four boards beyond the campaign scope |

Every trajectory's paid-Undo debit and Coin conservation matched its attempt
rows. Selected shell/blocked/exit/Undo records preserved their corresponding
state and policy-6 action changes. The dashboard's eight import/export,
safe-text rendering, trace-grouping and actual-full-report tests also passed
independently.

## Validation status

**Accepted for the declared source, host-execution and synthetic-data scope.**
No blocking correctness or attribution finding remains in the reviewed
implementation and recorded outputs. Scenario effects remain consequences of
the chosen, uncalibrated utility/learning/timing formulas. In particular,
reward-muted changes declared reward salience and its explicit utility/time
terms; its result is not independent evidence about human reward behavior.

This review does not assert publication, remote CI success, public-origin
correctness, native Android UI acceptance or human predictive validity. Those
delivery scopes require their separate receipts.
