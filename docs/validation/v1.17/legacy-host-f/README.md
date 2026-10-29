# Accessible puzzle families and F host coverage

Production **1.17.0-rc4 / code22**, APK SHA-256
`fe556ae6f0420f41afb7e5db044ef925fe79d0695f0ac6190d6870daf2e8d5a8`.
This is a host/source coverage record, not Android execution or a claim that
every mode has been played natively on F.

The initial F workflow's [14-command / 135-file proof](../ci-host-rehearsal-f/summary.json)
fully traverses the U route and tests all N boards, but only samples or
round-trips several other active families. Nine unmodified existing suites
have now been executed against a byte-identical copy of the frozen F sources.
[Execution summary](execution-summary.json), [input hashes](input-sha256.json)
and individual command/log records retain the exact scope. All **161 APK
inputs** and **14,813 historical evidence files** remained unchanged.

## What is actually accessible

The inventory follows `LumaView.start`, `previewLevel`, `drawJourney`,
`drawGuardians`, `drawEvents`, `openChallenge` and `loadSession`, plus
`ChallengeCode.valid/resolve`. These are reachable modes or compatibility
routes, not additional U journey levels.

| Family | Reachability and size | Current host qualification |
| --- | --- | --- |
| U | Main Journey, U1–U1000 | Initial F `RelayV115Test`: every reachable U state, 191,954 states / 471,517 transitions. The separate real U1–U1000 Android replay remains its own qualification. |
| M | Remix map after the twelfth Journey clear; M1–M240 | Newly executed Strategy suite: all 240 M boards and every reachable action state, including manual gates, exact costs, save and Undo. |
| Guardian encounters | 100 optional encounters, one unlocked per ten distinct Journey clears | Newly executed Guardian suite: all 100 patterns, 6,197,718 reachable productive states and 34,045,573 transitions. Guardian Store additionally solves all 100 before exercising reward requests. Guardian labels are distinct from historical `LD1-G` codes. |
| Daily / Weekly / T | Three stages per day, seven per week. Both new event routes call `StrategyCatalog.event(seed)` | T has an unbounded count of dated/seed identities but selects only the **980 L21–L1000 geometries**. The Strategy audit represents all 980, plays their certificates and separately traverses all L states. Data tests cover event unlock order, stale-period rejection, rewards and duplicate claims; calendar tests use an independent `java.time` oracle. |
| P | Six sealed workshop challenges, opened from Events or imported codes | Newly executed Sealed suite: all P1–P6, every piece subset × 16 gate masks, independent legality/solver oracle, certificates, Undo, history and restart. |
| L / N | Historical Journey codes L1–L1000 and N1–N1000; saved older attempts can also resume | L: new Strategy exhaustive action-state traversal on all 1000. N: initial F Learning suite verifies all 1000 exact minima and certificates, all intro runtime states, typed history and physical-source compatibility. N is not replaced by U when importing or resuming. |
| J / K and G / H | Imported J1–J1000 / K1–K240 and G1–G1000 / H1–H240; corresponding old saved attempts retain their content version | New Progression V3 suite covers all 1240 J/K certificates, every certificate prefix against an independent oracle, Undo/save/budget, all 1240 G/H compatibility prefixes and exact independent reachable-subset policy metrics for both sets. Runtime arbitrary-state solver probes are sampled; this is not a Board traversal of every G/H/J/K state. |
| C / R and B / Q | Imported historical C/R/B/Q1–240 | Engine suite plays all C/R certificates, checks alternative starts, identity and hostile codes. B/Q are fixed reorderings of the same C/R geometries; all B/Q code IDs, preserved geometry/seed/solution/budget digests and legacy budget restoration are checked. Not every C/R/B/Q runtime branch is traversed. |
| E | Historical imported event seeds and old content-version event saves | Engine suite checks **1002 E seeds**, including extreme signed seeds; budget suite checks another declared 1002-seed sample and restoration. The accepted code domain is nonnegative through `Long.MAX_VALUE`. Unlike T, E uses the old procedural generator and has no exhaustively enumerated finite geometry closure. No claim covers every possible E seed. |

The code parser accepts fifteen families:
`C R E B Q G H P J K L M T N U`. Codes carry board identifiers only and
friend-code play uses mode 4 without campaign/event rewards. Historical saved
Journey/Remix attempts use the original catalogue selected by `content_version`;
new ordinary Journey play uses U, new Remix uses M, and new dated events use T.

The three-exit First Light and seven guided tutorial practice boards are
separate onboarding exercises, rather than catalogue or reward-bearing
Journey levels. The exact F fresh-player route already exercises the guided
practice before its eight genuine U wins. The added legacy suites do not
pretend to be a new exhaustive tutorial-state audit.

## Newly executed suites

| Existing suite | F result and actual meaning |
| --- | --- |
| `test-strategy-v4.sh` | 33,611 targeted contracts plus **773,092,810** exhaustive assertions over all **1240 L/M boards**, **13,254,058** reachable states and **57,403,554** action edges; all **980** possible T geometry sources represented. |
| `test-guardian-v19.sh` | **472,484,607** assertions; all **100** encounters and every productive state, plus blocked-budget/save/Undo contracts. |
| `test-sealed.sh` | **10,425** checks; all six P boards and every subset/gate combination. |
| `test-progression-v3.sh` | **582,831** checks; all **1240 J/K boards**, frozen G/H sessions, independent probability model, sampled arbitrary subset/gate checks, IDs, budgets and seals. |
| `test-engine.sh` | **437,039** assertions; C240 + R240 + **1002** E seeds + **1002** paired-switch generator seeds, codes and hostile inputs. Paired-switch generator seeds are additional technical coverage; they are not another active dated mode. |
| `test-move-budget.sh` | **390,327** checks; **2722** actual certified runs, **100,000** corrupt snapshots, **20,000** valid snapshots and unlimited friend actions. |
| `test-guardian-store-v19.sh` | **5146** assertions; **100** encounters solved before reward requests, once-only rewards, **24** concurrent two-handle trials and refused-write behavior. Preference doubles, not Android storage or anti-cheat validation. |
| `test-data.sh` | **8882** economy/progression/Remix/event/localization checks, including all three daily and seven weekly reward stages, stale periods and duplicate claims. Store methods are invoked in the host model; this does not claim UI gameplay. |
| `test-collection-events.sh` | **87,121** checks; **604** time zones / **14,496** boundary scenarios and DST 23/25-hour days, using an independent time oracle. |

The nine suites pass in **82.671 seconds**, with at most two independent host
processes in parallel. These times are host execution durations, not Android
FPS, user playtime or human difficulty. Scripts/tests were not changed.
`test-progression-v3.sh` normally writes a historical fingerprint path; its
unaltered execution ran in a disposable source copy, so that side effect
could not rewrite the repository's preserved evidence.

## Native limits and useful remaining checks

The relay observer explicitly requires `content_version=7` and budget policy
5, reads U fixtures and advances through U boards. Its eventual 1000-level
success cannot certify M, Guardian, P, Daily/Weekly or imported legacy play.
The F secondary interface routes inspect those menus, without solving their
boards. Historical 1.9/1.15 native runs retain their own APK identities.

No finite active catalogue is wholly absent from the executed host checks.
However, the report deliberately does not claim exhaustive Board traversal
for every old family or every possible E seed. Existing independent
certificates, fixed compatibility hashes and seed samples provide meaningful
coverage; repeating the implementation in a new test would add little.

A bounded **actual F native smoke** is useful for the remaining UI integration
gap: enter M, Guardian, P and current Daily/Weekly through visible objects,
perform their distinct rules (manual gate, layered/cellwise Guardian and key),
Undo/restart, complete a representative board and cold-resume it. That would
verify the redesigned touch/navigation path, not exhaust every old mode.
The native-test owner has this separate work; no device was accessed here.
Public wording should say **“the U1–U1000 Journey was replayed”**, rather than
“all levels in every mode were played.”

The CI sparse list now includes the existing legacy contracts and their model
dependencies. Its exact isolated **23-command / 160-file** rehearsal is recorded
separately in [the complete F rehearsal](../ci-host-rehearsal-f-complete/summary.json).
The initial 14/135 record remains unchanged. Remote Actions is still a
separate publication gate.
