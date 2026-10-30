# Luma Drift 1.22 — living map and tactical choices

Production `1.22.0-rc1` / Lab `1.22.0-sandbox-rc1`, Android code **30**.
Qualification and publication status are recorded in the
[delivery register](validation/v1.22/README.md). A successful build alone is not
release acceptance.

The exact application/lab source and artifact metadata are published as
[C1](https://github.com/nabz0r/AndroidHit/commit/716bf73066892d034510db80630cd1f227738372);
the initial website is published as
[C2](https://github.com/nabz0r/AndroidHit/commit/13ebe82be9b57569df50627449cc748f5245521c),
then the final lab loading correction as
[C3](https://github.com/nabz0r/AndroidHit/commit/b122c0acbd172a7912c69b0b6914b8b0f9b2a243).
The existing Firebase site is released from C3 as version `24e34cf6e6af1ed8`.
Public bytes, scoped browser checks and [both C3 remote contract workflows](validation/v1.22/publication/c3/ci/receipt.json)
pass. The correction changes no qualified Android package or experiment data.

| Signed Android variant | Version / code | SHA-256 |
| --- | --- | --- |
| [Production APK](https://luma-drift-production.web.app/releases/luma-drift-1.22.0-rc1.apk) | `1.22.0-rc1` / 30 | `9b2593388de22b1f50f49bea05b9b02008d4574f5cbb9012fb0df5a64c47a44e` |
| [Lab APK](https://luma-drift-production.web.app/releases/luma-drift-1.22.0-sandbox-rc1.apk) | `1.22.0-sandbox-rc1` / 30 | `882481a2da12cc0e4a36af804d211a12c942fab2bbaef88d80ed740e2a3490f8` |

The [artifact manifest](../releases/manifest-1.22.0-rc1.json) records sizes,
package identities and ignored physical delivery paths. Source publication does
not imply that an APK was stored as a Git blob or downloaded from the public site.

## Native changes

The Journey/Remix map uses an independent two-dimensional camera and a minimap.
Selecting an accessible node moves the actual equipped captain along the route;
short trips follow the path and direction. Pan, pinch and minimap inspection
change the view. “Follow me” restores tracking. Exploring never grants a level,
spends energy or starts a puzzle. Play stays explicit and available while the
presentation moves. Long or cross-sector trips settle without a forced wait.
Reduced motion retains the destination and state. Extremely short windows use a
scrollable level selector; locked Remix keeps its progression explanation.

Keys and their locks share a larger silhouette, color and number. Inspecting a
lock highlights its real key association. A real key exit sends a short visual
signal and opens its linked locks; Undo restores their actual state. A blocked
move displays the engine's immediate cause for 3.2 seconds. Holding a revealed
piece opens explicit consequence inspection, including gate changes and newly
available exits. Holding a covered piece explains its free preparation instead.
The campaign's ordinary taps and rules remain unchanged. U43/U48 display the
inspection affordance, alongside its earlier introduction at U29/U31.

The Echo Observatory is a separate **T122** chapter with twelve new boards and
two episodes. Each run crosses four boards, including two route choices. Nova,
Hoot and Orbit are loaned, with six shared Charge and one use of each talent for
the entire run: information (2), one-exit rewind (2), or up to two assisted exits
(3). Using two roles enables one explicit Resonance. Retries and stage changes
retain spent Charge. Every board remains manually solvable.

The chapter keeps a separate durable session. It consumes no campaign energy,
paid power or elemental shell. Its Undo has no Coin cost and does not refund the
productive action already spent. Completing an episode for the first time grants
120 earned Coins and its local souvenir marker. Replays do not repeat that
milestone; the loan does not grant ownership or copies. Finished settlement can
resume after a cold process. Corrupted chapter state has an explicit confirmed
restart path that preserves campaign and wallet.

## Evidence and interpretation

The design study combines the historical U43/U48 concentration, the versioned
synthetic lab and identified primary research. The
[1.22 lab](validation/cognitive-lab-v122/README.md) runs 21,000 campaign
trajectories, 84,000 probes without assistance and 54,000 chapter runs. Immediate
causal feedback gives a modest modeled improvement; execution talents increase
assisted progression while some unaided probe results decline. Both outcomes are
published. The native key inspection and visual map are outside the simulation.

[Current host and Android evidence](validation/v1.22/README.md) distinguish engine
contracts, durable preference doubles, installed package identity, real emulator
gestures and original screenshots. The [lab source binding](validation/cognitive-lab-v122/candidate-c-source-binding.json)
binds all 51 recorded source hashes to the current candidate C sources. Fifty
files supply executable/model inputs; `LumaView.java` is retained as UI context
and is not compiled as a lab driver.

Final C production passes 417 retained-profile assertions / 96 actual gestures /
31 original PNGs and a separate 51 first-install assertions / 19 gestures /
12 PNGs. Lab C passes 33 assertions / 4 gestures / 2 PNGs, with all seven
production preference maps unchanged. Candidate B's complete chapter executions
remain separately labeled B evidence; C cold reopening does not grant another
reward. See the [accepted-phase manifest](validation/v1.22/native/retained-production-accepted.json).

The [prepared website manifest](validation/site-v1.22/release/source-manifest.json)
binds artifact commit `716bf73066892d034510db80630cd1f227738372` to the exact tested Android bytes and eight original FR/EN captures,
with the archive, gallery, account/privacy routes and deliberate audio controls
preserved. Its schematic browser demo has fictional levels and locks; it is not
the Android game or player progress. C2 Hosting version `990135f689f144c0` is
released; the [original public receipt](validation/site-v1.22/public-origin-c2.json)
verifies all 35 new/changed bodies / 803,549,445 bytes, including both full APKs.
It does not re-observe historical responses or establish browser rendering.

The actual [public lab race failure](validation/site-v1.22/browser-public/lab/archive-race-c2-failure.json)
and [pre-fix browser report](validation/site-v1.22/browser-public/lab/report-before-fix.json)
remain preserved and failed. C3 is separately
[DEPLOY/FINALIZED](validation/site-v1.22/hosting/c3/deployment/deployment.json),
released at **2026-10-30T15:32:03.071Z**, with three uploads and 569 provider paths
preserved out of 572. Configuration is unchanged. Its
[three complete replacement responses](validation/site-v1.22/public-origin-c3.json)
/ 32,426,083 bytes pass; unchanged bodies retain their C2 receipts. The
[final byte bridge](validation/site-v1.22/public-origin-final-bridge.json) joins
35 actual C2 responses and three actual C3 replacements; it does not claim 35
fresh C3 downloads.
The [public lab retest](validation/site-v1.22/browser-public/lab-racefix/report.json)
passes 15 checks, including eight ready FR/EN responsive states and rapid
archive/current reversal. The [final browser register](validation/site-v1.22/browser-public/final-acceptance.json)
keeps unchanged C2 homepage/demo observations separate from the corrected C3 lab
and final desktop homepage revisit. No failed frame contributes acceptance.

[C3 input integrity](validation/v1.22/publication/c3-input-integrity.json) verifies
all 502 Android and 51 lab-generation inputs against immutable receipts, both
canonical APKs and all current public/script identities. It performs no build or
rerun and does not substitute for public response or browser observation.

Prepared source passes [1,248 offline checks](validation/site-v1.22/source-qa-prepared.json)
and [87 JavaScript interaction tests](validation/site-v1.22/runtime-tests-prepared.json).
The [source comparison](validation/site-v1.22/prepared-source-diff.json) records
570 physical payloads: 535 historical files unchanged, four intentional updates,
31 additions and no removal. Local browser, Hosting management and public-origin
response/render acceptance remain separate scopes.

No human participation, retention lift, millions-of-players forecast, physical
phone frame-rate measurement or Play approval follows from these checks. Real
checkout and production provider acceptance keep their prior gates.
