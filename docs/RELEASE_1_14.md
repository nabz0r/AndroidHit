# Luma Drift 1.14 — make every action count

28 October 2026 · production **1.14.0-rc2**, separate Lab **1.14.0-sandbox-rc2**, version code **16**.

This candidate addresses a reproduced first-session problem: a closed gate looked like an obstacle, while the game required the player to touch it and spend an action opening it. The reported historical L15 is solvable, but five refused attempts could consume its four-action margin before the rule was understood. The new route separates teaching from later combinations and makes action costs visible.

**Delivery status: signed-byte and scoped native RC2 qualification complete; public delivery in progress.** RC1 evidence is retained separately. The final RC2 source, package hashes and two-session native coverage below identify the candidate; GitHub downloads and Firebase Hosting require their own delivery receipts.

[Gameplay rules](GAMEPLAY_1_14.md) · [UX decisions](UX_1_14.md) · [Puzzle/UI audit](validation/v1.14/puzzle-ui-audit.md) · [Qualification](validation/v1.14/README.md).

## What changes

- **Learn in the main route.** Thirty authored opening boards introduce straight exits, piece blockers, numbered keys/seals, manual gate openings, switches and their combinations. A rule note appears with the board and the briefing explains the mechanic before Play. The separate three-piece encounter and seven optional practice lessons remain.
- **Plan within a real budget.** A successful exit or manual gate opening costs one action. Refused attempts cost nothing. One Return per attempt restores the preceding productive action and refunds it; its used quota survives a cold process restart. Exhaustion can still lose a run. Restarting is free, and the policy-4 failure screen does not offer a new paid continuation.
- **See cause and effect.** Closed sides are visibly touchable controls marked −1. A switch preview lists the sides it opens or closes. Actual changed gates and newly available exits receive brief causal feedback. A blocked path identifies the relevant side, piece or matching key without revealing a complete solution.
- **Explore a versioned 1,000-board Journey.** The N family starts with 30 authored teaching boards. Its later route is assigned globally across 97 ten-board episodes with reserved recoveries and increasing mechanical bounds. Later piece positions, directions, switches and seals come from 970 existing layouts; 291 initial gate states are reauthored by opening additional gates in N. The old L family remains unchanged. Difficult choices remain; mathematical certification is not a measured human difficulty curve.
- **Keep the companion’s home.** The furnished room, clothing, hugs, movement, furniture previews, ownership and earned LumaCoins remain. Obsolete Shield offers are retired for the free-refusal policy while historical ownership and recorded purchase decisions are preserved.

## Compatibility

N challenge codes identify the new progression and never replace the meaning of an L code. Existing L/M/T and earlier challenge identities retain their resolvers. A saved in-progress board is reconstructed with its saved content family; upgrading its action ledger does not silently replace its geometry. A new Journey attempt uses N.

History, ledger version, productive action count and used Return count are committed together. New policy-4 restoration validates the productive count against actual board history. Existing earned balances, house state, collection ownership and already-recorded transaction receipts remain local.

Google sign-in does not synchronize gameplay or the house. Test ads remain forced; real checkout and premium draws remain disabled. This candidate does not establish a Google Play upload or live provider acceptance.

## Frozen RC2 artifacts

The [RC2 binary audit](validation/v1.14/binaries-rc2/binary-final-candidate.json) passes signature, alignment, native-library and bundle checks for all four exact outputs. Their source inputs and bytes stayed unchanged during verification. The four exact files are published at [source/artifact commit578661da](https://github.com/nabz0r/AndroidHit/commit/578661daf81be8896668d427bef061f5f0fff700); [independent public HTTP checks](validation/v1.14/publication.json) confirm every size and SHA-256.

| Signed artifact | Bytes | SHA-256 |
| --- | ---: | --- |
| [Production APK](../releases/luma-drift-1.14.0-rc2.apk) | 7,888,478 | `28bc2ee6263193978cfbdefd6effb62d3d0b1a950454113ef9a0198eb4d39a44` |
| [Production AAB](../releases/luma-drift-1.14.0-rc2.aab) | 11,301,347 | `6d12eac18c5253a1f53cdd299323469da065f11ef4c39c0a41834516c60c2bec` |
| [Lab APK](../releases/luma-drift-1.14.0-sandbox-rc2.apk) | 7,888,169 | `cbc74c3214f3c64c888f34b9452be162d3e3e168f1c218d1a4537410ef005ff0` |
| [Lab AAB](../releases/luma-drift-1.14.0-sandbox-rc2.aab) | 11,292,187 | `1a0ac425b70d11bef9af509e5283431c0dcd489b34b6b944288664c0dce261f8` |

The RC2 [strict ledger run](validation/v1.14/deliberate-play-rc2.txt) passes **24,474 checks**, including **19,920 productive actions** over all 1,000 certificates; the [guide/localization run](validation/v1.14/learning-guide-rc2.txt) passes **4,867 checks**. The changed productive-action total follows the reauthored effective initial gates. Native acceptance is assessed separately against these exact APK hashes.

## Intermediate RC1 evidence — retained for provenance

The **RC1-only** [frozen binary audit](validation/v1.14/binaries-rc1/binary-final-candidate.json) records the signed **1.14.0-rc1 / code 15** bytes below, their signature/alignment/bundle checks and unchanged source inputs during verification. RC2 needs its own new build, manifests, native reports and immutable download receipt.

| Signed artifact | Bytes | SHA-256 |
| --- | ---: | --- |
| Production APK | 7,888,482 | `d1fc106eb8eeff1b3bc6e18818c7941278943ae526047bd84c607c6d3b7935a6` |
| Production AAB | 11,300,891 | `ceaa9b3b0d62fda9be07c04acc941a4171672f893d1d9cbc0a65a3be0efdd413` |
| Lab APK | 7,888,169 | `abba40b4f592a0002470419a9f4c72bfdb59473697f6326b76c5fa730a5c695c` |
| Lab AAB | 11,291,698 | `22c3ad0f66f9267f0c62bc0381b71f4fde3aa8410d178cfaf9eb1f6d85471a50` |

The [real 1.13 RC2-to-1.14 RC1 upgrade route](validation/v1.14/ui-bugs/ui-sdk-upgrade-a/host-report.json) passes 283 checks and retains 22 native captures. It starts with the old APK, installs the frozen **1.14 RC1** Lab candidate, preserves the checkpoint before its first new launch, and crosses distinct processes while checking the old L15 and new N15. The wrapper restores the original preference files while intentionally retaining the updated installation. This scoped upgrade pass does not replace the full new-route acceptance.

| Scope | Recorded result | What it establishes |
| --- | ---: | --- |
| [Archived RC1 N content contracts](validation/v1.14/audit-learning-rc1/audit-learning-contracts.json) | 191,673 checks across 1,000 boards | Independent initial minima, 30 opening-board reachable states, authored lesson bounds and the 970 preserved geometry correspondences. |
| [Deliberate action ledger](validation/v1.14/deliberate-play.txt) | 24,775 checks | 20,221 real productive actions through all 1,000 certificates, free L15 refusals, one Return, saved quota, genuine budget failure and free retry. |
| [Guide and six-language copy](validation/v1.14/learning-guide.txt) | 4,867 checks | Exact mechanical causes, N1..30/L1..30 prefixes, historical rule separation, no solver/state mutation and bounded compact copy. |
| [House regression](validation/v1.14/home-regression.txt) | 891 checks | Preserved home model and durability contracts. |
| [Retired power offers](validation/v1.14/audit-power-offers/audit-power-offers.json) | 868 checks | Real store/presentation APIs, historical stock/receipt preservation and refused-write recovery on JVM preference doubles. |

## RC2 acceptance

The frozen RC2 corpus SHA-256 is `0a6de06054a377d37b5b8ab1e2374cdbe694c03983adeb1b5714f86aec54dafc`. Its [source contracts](validation/v1.14/audit-learning/audit-learning-contracts.json) pass **201,143 checks** and [offline regeneration](validation/v1.14/audit-learning/audit-regeneration.json) is byte-identical. A separate [review](validation/v1.14/independent-pacing-review/README.md) passes **4,970 assertions**, recomputing all minimums, score components and difficulty classes from the effective boards.

All 97 later episodes contain three starts with an immediately legal exit and at least three Normal boards. The 96 episodes containing a challenge end on a Normal board with a free exit and a lower score. Hard/Expert runs are at most four boards. Median structural scores per 200 boards rise **46 → 66 → 79.5 → 90 → 100**; the smaller local dip at N501–600 is disclosed in the [curve review](validation/v1.14/audit-learning/curve-review.md). Classes describe structural cost choices, not calibrated human difficulty. RC1 screenshots are not current website imagery.

Current installed RC2 scopes pass against the frozen APK hashes:

| Native scope | Checks / PNGs | Boundary |
| --- | ---: | --- |
| [Lab N15 and cold quota](validation/v1.14/ui-bugs/ui-sdk-final-n15-core-rc2-a/host-report.json) | 270 / 14 | Refused taps are free, Spark works, one Return refunds its action, second Return refused and quota preserved after cold restart; original profile restored. |
| [Lab first30 and gallery](validation/v1.14/ui-bugs/ui-sdk-final30-gallery-rc2-a/host-report.json) | 716 / 101 | N1–30 real UI clears, actual N28 exhaustion/free retry, gate rules, one Return, house purchases/placement/hug and a distinct cold house process; original profile restored. |
| [N1–1000 coverage across two sessions](validation/v1.14/ui-bugs/ui-sdk-coverage801-1000-rc2-d/independent-coverage-check/coverage.json) | 55,423 / 142 | Ten complete native batches; declared access-only prerequisite for N801–1000, originals restored; C remains globally failed. |
| [Production startup](validation/v1.14/ui-bugs/ui-sdk-production-startup-rc2-a/host-report.json) | 7 / 1 | Exact code16 production package, visible native launch and real lifecycle/audio behavior; profile restored. |
| [Production first30](validation/v1.14/ui-bugs/ui-sdk-production-play30-rc2-a/host-report.json) | 779 / 103 | N1–30 real UI clears, six languages, house, compact320 and landscape; original preferences/display/rotation restored. |

The gallery run retains 97 original full frames plus four analysis crops; production first30 retains 99 full frames plus four analysis crops. Analysis crops are diagnostic evidence and are never used as website product images.

All 1,000 final N boards have recorded native three-star wins across ten complete passed batches: **55,423 checks, 19,920 productive actions and 142 indexed original PNGs**. This is coverage across two sessions, not a passed continuous marathon. C keeps its global FAIL/restored result: its eight complete batches cover N1–800; its failed ninth batch contributes nothing. The separate restored D session covers N801–1000 after the explicitly recorded synthetic prerequisite `unlocked=801`; no stars, coins, session, history or solution actions are injected by that setup. [Independent coverage receipt](validation/v1.14/ui-bugs/ui-sdk-coverage801-1000-rc2-d/independent-coverage-check/coverage.json). The nine website frames remain from the separate passed/restored RC2 gallery scope, with exact source filename/hash provenance.

Final binary integrity, installed Android acceptance, exact public download bytes and Hosting delivery remain separate evidence layers. Native coverage and the targeted acceptance above are complete; public delivery is recorded only after its own reports pass. Native captures on the website must come from the exact final 1.14 APK and a passed report; no 1.13 image is relabelled as 1.14.

Physical-phone comfort, first-time comprehension, native-speaker review, enjoyment, retention and international appeal remain to measure with people.
