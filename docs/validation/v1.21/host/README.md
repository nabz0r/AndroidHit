# 1.21 host simulations and regression evidence

These are executed Java host/model/transaction/geometry suites. No APK, emulator,
physical phone, human playthrough, live provider or retention result is claimed.
Assertion counts include repetitions and describe different scopes; they are not
added into a score or a count of players.

## Frozen B dependency update

[B dependency comparison](candidate-b-host-dependency-comparison.json) verifies
all 493 B inputs and exactly two A→B changes: `LumaView.java` and
`JourneyMapStrings.java`. All 154 protected Java/build inputs used by the pure
host models remain identical. The prior 59 Java/model/summon scopes are reused
under their original A records, with no gratuitous full-matrix rerun and no
relabeling as B native execution. The changed map-copy class independently
passes [240 actual JVM checks](candidate-b-map-labels/report.json), and the
latest staged site/demo command independently passes
[21 tests](candidate-b-site-model/run-summary.json). B layout/touch/XP/scroll
changes require their own B native evidence.

## Candidate A final scoped result

[Accepted index](candidate-a-host-index.json): **60 distinct executed commands /
simulations accepted** against source inputs matching the frozen candidate A.
All 493 candidate inputs were rechecked unchanged; each of the four accepted
host snapshots contains the same 156 Java/build inputs shared with that
candidate. The remaining candidate assets are package/source identity evidence,
not inputs executed by the host tests.

The [full integration matrix](final-integration/run-summary.json) actually
finished **57 PASS / 1 FAIL in 154.573 seconds**. Its preserved failure was the
historical scene test's hardcoded 220 ms completion after the explicit 1.21
300 ms transition change. The [corrected test retry](transition-contract-retry/run-summary.json)
passes 14,843 assertions with the declared 300 ms duration and stricter 2.5%
travel bound. No application fix or different APK was involved. The accepted
index uses that retry plus the other 57 passing scopes, the separate fixed-seed
summon analysis and 21 staged site/simulation tests. It does not rewrite the
failed matrix as an originally all-green run.

| Final new/changed scope | Result |
| --- | --- |
| [Elemental shells](final-integration/elemental-shells.json) | 291,912 assertions; all 1,000 certified plans; 4,950 taps; 22,770 actual companion exits |
| [Balance animation and direct-exit Focus](final-integration/arcade-presentation.json) | 71,010 assertions; real U1–U1000 exits and Undo/re-exit anti-farming |
| [Independent kitchen](final-integration/home-kitchen.json) | 627 assertions; original three starter bytes; all free floor paths; separate cold saves and paid 440-Coin furnishing |
| [Logarithmic economy](final-integration/economy-curve.json) | 47,008 assertions; actual versioned/legacy store deltas and earned-Coin Undo reservation/refund/complete fault recovery |
| [Energy capacity](final-integration/session-energy-121.json) | 6,044 assertions; 10→16 cap without free grant; old codec, pending debit/recovery and refill cadence |
| [Menu 1.21](final-integration/menu-121.log) | 5,232 host geometry/localization checks |
| [Chapter map](final-integration/saga-map.log) | 177,034 checks; all Journey/Remix jumps, 13 viewports, culling and pinned-hit bounds |
| [Staged site and independent demo](site-model-final/site-and-simulation-121.log) | 21 Node tests; 9 site-runtime and 12 demo/model parity tests, DOM doubles only |
| [Actual source summon pools](summon-analysis-final/summon-simulation/summary.json) | 800,000 fixed-seed draw calls; 800,060 assertions; FR/EN item and modeled-cohort CSVs |

[Economy arithmetic and limits](economy-analysis.md). The original full U
reachable-state test and all-identity action coverage still pass in the final
snapshot with their respective 13,312,469 and 2,313,118 assertions. No new
Android playthrough is inferred.

The CI source workflow now includes the new sources, scripts and native-economy
fixture. This local record does not claim that hosted CI has run. Earlier
development failures also remain: the initial kitchen test assumed the wrong
historical wallpaper, and the initial energy test recreated the wrong preferences
instance. Corrected test witnesses pass; no product source was changed to hide
those failures.

## Baseline before the 1.21 refactor

[Run record](baseline/run-summary.json): **52/52 suites PASS**, no failures, wall
time **139.044 seconds**, three concurrent workers. The copied application inputs
match HEAD `d607dd38615e137ad0eafda51e33d93bcf213fec`; the only status entry at
capture was this new untracked evidence directory. [Exact source hashes and
snapshot method](baseline/source-inputs.json). Every result retains the exact
command, timestamps, duration, exit status and log hash. Existing test assertions
were run unchanged. Tests with report arguments write here; any incidental legacy
output remains inside the disposable source copy, not the historical repository
evidence.

| Executed scope | Result |
| --- | --- |
| [All U1–U1000 reachable states](baseline/relay/relay-host.json) | 13,312,469 assertions; 191,954 states; 471,517 transitions |
| [All U1–U1000 × 37 companion identities](baseline/companion-policy6/host-report.json) | 2,313,118 assertions; current and preserved action policies; certified drafts, undo and anti-farming |
| [Actual Journey and result transactions](baseline/focused-journey.json) | 178,587 assertions; 4,000 solved runs and 1,000 deliberately exhausted runs across the 1,000 boards |
| [Preserved N1–N1000 curriculum](baseline/learning/audit-learning-contracts.json) | 201,143 assertions; independent branch metrics on every board; 24,960,432 independent gate states; exhaustive runtime first 30 |
| [Historical L/M/event strategy](baseline/strategy/strategy-all-states.txt) | 1,240 campaign/Remix boards plus finite event pool; 13,254,058 reachable states; 57,403,554 edges; 773,092,810 assertions |
| [All 100 Guardian encounters](baseline/guardians/summary.json) | 6,197,718 reachable productive states; 34,045,573 edges; 472,484,607 assertions |
| [Historical board audit](baseline/board-audit.log) | 1,240 boards; 1,101,868 reachable states; 49,063,063 assertions |
| [Economy](baseline/economy.log) / [meta progression](baseline/meta.log) | 6,611 and 362 assertions respectively; meta journal JSON export also round-trips |
| [Current session energy](baseline/session-energy.log) / [menu transactions](baseline/menu.log) | 20,202 and 73,838 assertions respectively |

The full 52-command record also includes N1–N1000 pacing/certificates, legacy
codes, sealed challenges, action budgets, daily/weekly/Remix transactions,
collection, expedition model/store, durable rewards/local-save failure doubles,
profile/summon/ownership, tutorial, first light, inventory, wallet recovery and
premium transport, home/scene/puzzle geometry. Audio, raster artwork, website,
Android builds and provider activation are outside this matrix.

## New elemental-shell model

[Contract](elemental-contract.md) ·
[Initial passing test](elemental-initial/elemental-shells.json) ·
[Exact initial tested inputs](elemental-initial/source-inputs.json).

The initial model test passes **291,909 assertions**, all **1,000 certified U
plans**, **4,950 purposeful layer taps**, and **22,770 actual certified companion
exits**. Each intermediate layer state round-trips canonically. The original
arrows, switches and keys remain unchanged; 652 boards have original keys. This
initial report precedes the extra same-number/same-seed rotated-geometry rejection
test and is preserved under its actual source hashes.

The [completed geometry-rejection pass](elemental-geometry/elemental-shells.json)
passes **291,912 assertions** with the same 1,000 solved plans, 4,950 taps and
22,770 certified companion exits. The added witness is a separately solvable
quarter-turned puzzle retaining the same number, seed and tile IDs; its different
full geometry correctly rejects the original saved shells. [Exact tested
inputs](elemental-geometry/source-inputs.json).

Run `python3 docs/validation/v1.21/host/run-host-simulations.py <new-scope-name>`
to snapshot current inputs and execute the matrix, now including the new shell
suite. Each scope name must be new; results are never overwritten. Optional
remaining arguments select explicit suite labels. Running inside a copied input
tree prevents concurrent UI edits from producing a mixed compilation.
