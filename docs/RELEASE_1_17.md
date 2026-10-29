# Luma Drift 1.17 — a world at your fingertips

29 October 2026 · production **1.17.0-rc4**, isolated Lab **1.17.0-sandbox-rc4**, Android code **22**.

**Global qualification is in progress.** Candidate F passes four-package build/lint, independent binary verification and the shared responsive-root geometry tests. Exact-F native interface, fresh-player, audio, visual and 1,000-level replay results are required before canonical packaging, Git publication or Firebase delivery.
<!-- FINAL_117: RELEASE_STATUS — replace only after all final candidate gates pass. -->

F reserves separate caption and navigation space on compact screens, without shrinking the companion or object art. Its cached `SceneRootLayout` is shared by live drawing and independent geometry tests. Independent review found two compact composition defects after E's scoped passes: the Collection sibling crossed its companion caption, and an Atelier plant crossed the furniture caption. The [grouped E review](validation/v1.17/review/grouped-e-layout-review.json) and original E evidence remain preserved. E's previous wardrobe-contrast and clipped-language corrections are retained.

Production F is **71,827,197 bytes**, SHA-256 `fe556ae6f0420f41afb7e5db044ef925fe79d0695f0ac6190d6870daf2e8d5a8`.
The [F source freeze](validation/v1.17/candidate-f/source-freeze.json) identifies **161 inputs**, SHA-256 `e76c23760a2190620f4e1ee9af1aac4097f51b90d64764e4db7a16c4d416305e`.

## Audited F build outputs — delivery still pending

The [independent binary audit](validation/v1.17/candidate-f/binaries/binary-final-candidate.json) checks the exact four outputs, source freeze, signatures, bundle validation and static 16 KiB ZIP/ELF alignment. These local outputs are not a release-wide acceptance or publication claim.

| Output | Bytes | SHA-256 |
| --- | ---: | --- |
| Production APK · `1.17.0-rc4` | 71,827,197 | `fe556ae6f0420f41afb7e5db044ef925fe79d0695f0ac6190d6870daf2e8d5a8` |
| Production AAB · `1.17.0-rc4` | 74,721,780 | `e901e5067b586ae83b603b39b25365739744ba26a178b9b806196aa2f53dbb55` |
| Isolated Lab APK · `1.17.0-sandbox-rc4` | 71,826,884 | `6f1124463dc16155e3a891994882bb0247c08926c0cd4e3d13a6c46c79571bc2` |
| Isolated Lab AAB · `1.17.0-sandbox-rc4` | 74,713,869 | `c8772eb0d53880990a8ba03f70af04aac99b8cb13a4863f8153b319397805cbc` |

[Build/lint](validation/v1.17/candidate-f/build.log) passes in 48 seconds. [Verification](validation/v1.17/candidate-f/binaries/binary-verification.txt) confirms the production/Lab IDs, versions, code22, min API26, target API36 and existing signing certificate `dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287`. Both AAB signatures and pinned official bundletool1.18.1 validation pass. Eight 64-bit native payloads / 20 ELF LOAD segments pass static16KiB checks. Separately, the [native F upgrade device record](validation/v1.17/native-interface/candidate-f-upgrade/host-report.json) reports Android API36, arm64-v8a and actual16,384-byte pages; this scopes the observed emulator runtime, not every device/provider path.

Canonical filenames/manifests and public downloads remain gated by final native acceptance.
<!-- FINAL_117: ARTIFACT_DOWNLOADS — add canonical links only after exact delivery verification. -->

## What changes

Four illustrated places—Adventure, Home, Collection and Workshop—use touchable
scene objects and free captions instead of visible game button faces, pills,
tabs and a bottom navigation bar. A portal starts/resumes play; books, mirrors,
wardrobe rails, capsules, entrances and blueprints lead to their corresponding
tasks. Optional root swipes supplement named objects. Costs, odds, ownership,
confirmation meanings and semantic/keyboard actions remain explicit.

The bundled art supplies original sprites for **24 companions, 24 garments and
42 furnishings**, three new place illustrations and room materials. The same
portrait composes body, hat, dress, paired shoes and gloves across the game.
Species-specific rig metadata separates anatomy from existing saved item IDs.
The three **10 × 10 rooms**, close-up/whole-room views, pan, pinch, local nickname,
chosen flag, six earned badges and three badge slots are retained.

Scene presentation uses a bounded **220 ms crossfade**, input guards and immediate
completion for reduced motion or lifecycle interruption. Rendering and
transitions do not repeat navigation, drawing or store mutations.

Five contexts across ten biomes supply **50 synthesized arrangements**: ten
historical puzzle themes plus forty place-specific arrangements. Three new
scene/fabric/reveal earcons join the eleven preserved effects. Music and effects
remain independently controlled. [Art and actual DSP previews](ART_AUDIO.md).

The U1–U1000 rules and existing saves remain. Piece exits control doors; repeated
Undo is free. Companion arrivals retain 23 possible companions for 180 earned
LumaCoins, excluding free Nova. Five complete outfits cost 260 earned coins per
arrival. Fixed odds, duplicate returns, recoverable receipts, existing direct
purchases, individual clothing capsules and twelve free styles remain.

## Evidence available now

| Exact E scope | Observed result | Proof |
| --- | --- | --- |
| Retained-profile C→E upgrade and cold persistence | 619 checks / 20 PNGs; includes 6 baseline checks on C, then 613 checks on E | [Upgrade](validation/v1.17/native-interface/candidate-e-upgrade/host-report.json) |
| Original offscreen language-target regression at 320 dp width | 19 checks / 2 PNGs | [Compact regression](validation/v1.17/native-interface/candidate-e-clipped-language/host-report.json) |
| Actual owned Nova/Miko/Halo and five outfit previews | 78 checks / 27 PNGs | [Rig route](validation/v1.17/native-interface/candidate-e-owned-rigs/host-report.json) |
| Normal six-language browsing | 276 checks / 177 PNGs | [Normal route](validation/v1.17/native-interface/candidate-e-sixlangs-normal/host-report.json) |
| Genuine empty-device first launch, tutorial, eight first wins, two earned draws and cold persistence | 133 checks / 105 actual gestures / 49 original PNGs | [Fresh aggregate](validation/v1.17/fresh-e/summary-e.json), [scope and failed-observer exclusion](validation/v1.17/fresh-e/README.md) |
| Separate pure-render gallery | 144 complete looks, 24 isolated garments, 24 style samples / 50 PNGs | [E execution](validation/v1.17/art/native/candidate-e-art/host-report.json); not app UI or ownership evidence |
| Inspected visual scope | PASS with documented nonblocking limits; 14 actual UI frames directly reviewed | [E review](validation/v1.17/review/visual-candidate-e.json), [50/50 complete gallery PNG byte equality C→E](validation/v1.17/review/c-to-e-renderer-pixel-equality.json) |

The fresh route earns 440 coins from eight genuine first victories. Its observed
180 starting balance and 100 tutorial gift bring the wallet to 720; actual Zig
and Star parade draws spend 180 + 260 and leave 280. The equipped seven-slot
portrait, wallet and saved reveal survive a distinct cold process without
another debit. The separate E-only AVD is stopped normally with userdata
preserved; it does not touch the retained main profile.

The inspected E wardrobe sample closes C's P2 status-copy contrast finding from
**1.93:1 to 5.45:1**. Nonblocking limits remain explicit: a thin sole-like edge
under Jelli's front shoe, free styles that add accent ornaments rather than new
painted costumes, and some high collars near the chin. This is inspected visual
acceptance, not an assertion that every screen/pixel or accessibility criterion
has passed. [Raw C reservation and compact failure](validation/v1.17/README.md#preserved-intermediate-findings)
remain unchanged; D's onboarding alone does not certify E.

The [current local E sparse CI rehearsal](validation/v1.17/ci-host-rehearsal-e/summary.json)
passes **13 configured commands from exactly 132 files**, with source/log hashes.
It includes **10,922** transition/gesture checks, **19,857** sprite geometry checks,
**1,714** audio DSP/lifecycle-model checks, **18** old/current website-controller
tests and retained exhaustive puzzle/home/profile/economy contracts. This is
not a remote GitHub Actions result or installed gameplay evidence.

The [audio report](validation/v1.17/audio/scenes/final-b/verification.json)
records PCM exports and source hashes that match frozen E. Four 10-second
previews use the exact app synthesizer, biome 0 and music gain 0.27; they are
synthesizer renders, not phone recordings. Actual installed audio/lifecycle and
listening evidence remain separate.

## Final evidence still required

| Final gate | Current status / replacement marker |
| --- | --- |
| Candidate and source freeze | E frozen/verified; global acceptance pending — `FINAL_117_CANDIDATE_IDENTITY` |
| Four canonical APK/AAB files and manifests | Local outputs audited above; delivery links/manifests pending — `FINAL_117_ARTIFACTS` |
| Build/lint, signatures, bundles and static 16 KiB ZIP/ELF | PASS on E — `FINAL_117_BINARY_AUDIT` |
| Complete native normal/compact six-language routes, forms and gestures | Passed scoped routes above; full compact/secondary matrix pending — `FINAL_117_NATIVE_INTERFACE` |
| Actual UI and complete rig/art inspection | Inspected E visual scope PASS with documented limits — `FINAL_117_VISUAL_ACCEPTANCE` |
| Installed sound routing/lifecycle and audition scope | Pending — `FINAL_117_INSTALLED_AUDIO` |
| Real installed U1–U1000 replay on exact E | Prepared, not launched — `FINAL_117_NATIVE_REPLAY` |
| Sixteen original FR/EN website captures and responsive browser review | Pending — `FINAL_117_WEBSITE_ACCEPTANCE` |
| Clean source commit, verified public downloads and Firebase byte receipt | Pending — `FINAL_117_PUBLICATION` |

The replay uses the retained task QA profile that already earned three stars on
all U boards. It must play every board through real Android touches, preserve
profile/ownership/home/stock/settings and award **zero duplicate first-win coins**.
It is replay evidence, separate from the eight genuine new-player first wins.
Frozen certificates guide touches; no app/engine win hook or state seed is
permitted. The [signed observer and detached launcher](../scripts/v117-relay-ui/README.md)
are ready, with a [resume pointer](validation/v1.17/relay/active-handoff.json).
Device handoff from interface to audio to replay is mandatory before launch.

## Installation and activation boundary

Once accepted, install production as an update with the existing signing
identity and keep application data. Lab remains a separate package/profile.
Android 8/API 26+ is the configured minimum. The audited local sizes/hashes above are real; canonical download links belong
in that table only after final delivery verification.

Test ads remain forced; real checkout, paid premium draws and the unconfigured
wallet origin remain closed. Local progression, home and player card are not
synchronized by Google sign-in. This release does not establish Play upload,
live provider qualification, physical-phone performance, calibrated speaker
quality, human enjoyment, retention or future audience size.

The existing Firebase website has an [unpublished 1.17 template](validation/site-v1.17/README.md).
Final preparation requires the accepted APK/source freeze, sixteen exact native
captures and a rerun-verified full 1,000 replay summary. Website, Git and public
download delivery have separate receipts; none is inferred from a build.
