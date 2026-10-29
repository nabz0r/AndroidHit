# Luma Drift 1.17 — a world at your fingertips

**Current status: G (1.17.0-rc5 / code 23) is being qualified after fixing a reproduced Guardian Back loop. F results below are preserved intermediate scopes, not final acceptance. Its U replay was deliberately stopped at 817/1,000; no completed F replay is claimed. Git main, canonical G packages and the existing public website are not yet updated.**

29 October 2026 · production **1.17.0-rc4**, isolated Lab **1.17.0-sandbox-rc4**, Android code **22**.

**Final qualification is in progress on frozen F.** Build/lint, binary audit, retained-save upgrade, fresh-player flow, primary normal/compact six-language browsing, owned outfits, native art and installed digital audio pass on the exact APK below. Primary and secondary screen routes pass in the scoped matrix below. The actual 1,000-level replay and supplementary legacy-mode checks are running. Canonical packaging and Git/Firebase delivery follow those results.
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

## Exact F evidence

| Scope | Observed result | Proof |
| --- | --- | --- |
| Retained E→F upgrade and cold persistence | 619 checks / 20 PNGs: 6 baseline E + 613 F | [Upgrade](validation/v1.17/native-interface/candidate-f-upgrade/host-report.json) |
| Four root scenes at actual 320 dp, EN/DE | 285 checks / 8 PNGs at each font scale 1.0 and 1.3 | [Standard font](validation/v1.17/native-interface/candidate-f-roots-compact/host-report.json), [large font](validation/v1.17/native-interface/candidate-f-roots-compact-largefont/host-report.json) |
| Clipped German/Italian target regression | 19 checks / 2 PNGs | [Regression](validation/v1.17/native-interface/candidate-f-clipped-language/host-report.json) |
| Primary scenes, companion/furniture catalogues and five outfit previews, all six languages | 276 checks / 177 PNGs normal, and 276 / 177 compact | [Normal](validation/v1.17/native-interface/candidate-f-sixlangs-normal/host-report.json), [compact](validation/v1.17/native-interface/candidate-f-sixlangs-compact/host-report.json) |
| Owned Nova/Miko/Halo in wardrobe, profile and collection, with five outfit previews each | 78 checks / 27 PNGs | [Owned route](validation/v1.17/native-interface/candidate-f-owned-rigs/host-report.json) |
| Secondary compact routes, all six languages | 580 checks / 891 PNGs, union of two disjoint runs | [Hashed aggregate](validation/v1.17/native-interface/candidate-f-secondary-compact-aggregate.json) |
| Secondary normal routes, EN/FR | 205 checks / 221 PNGs | [Actual route](validation/v1.17/native-interface/candidate-f-secondary-normal/host-report.json) |
| Genuine empty-device first launch, tutorial, eight first wins, two earned draws and cold persistence | 133 checks / 105 actual gestures / 49 original PNGs | [Fresh F aggregate](validation/v1.17/fresh-f/summary-f.json), [scope](validation/v1.17/fresh-f/README.md) |
| Pure-render native gallery | 144 complete looks, 24 isolated garments, 24 style samples / 50 PNGs | [F execution](validation/v1.17/art/native/candidate-f-art/host-report.json); not application UI, ownership or acquisition evidence |
| Inspected visual scope | 58 actual UI frames directly reviewed; 50/50 F gallery PNGs identical to the fully reviewed C sheets | [F review](validation/v1.17/review/visual-candidate-f.json), [gallery identity](validation/v1.17/review/c-to-f-renderer-pixel-equality.json) |
| Installed digital audio | 38 checks / 8 PNGs / 130 AudioFlinger observations | [Final native review](validation/v1.17/review/audio-native-final-f.json) |
| Native scene transitions | 23 checks and an original SDK screen recording of three routes | [Report](validation/v1.17/native-interface/candidate-f-transitions-v2/host-report.json) |
| Cached enlarged-home rendering | 19 checks / 2 PNGs; cached sprite state stable while frames advance | [Observed scope](validation/v1.17/native-interface/candidate-f-steady-home/host-report.json) |

All native rows identify production F's exact hash. Some execute on a physical
copy of the retained task QA disk, made while its source was normally stopped.
Seven copied disk hashes and the complete initial save/session match. The
[immutable clone receipt](validation/v1.17/secondary-clone/creation.json) and
[baseline comparison](validation/v1.17/native-interface/candidate-f-clone-identity/identity-comparison.json)
separate those interactions from the single real E→F upgrade. No preferences,
coins, unlocks, ownership or puzzle histories were seeded.

The secondary route matrix is intentionally explicit: six compact languages
are split across the original and its verified clone; normal secondary routes
cover EN/FR. Primary browsing and outfit/catalogue coverage separately cover
all six languages at both sizes. A full six-by-two secondary matrix is not claimed.

The fresh route genuinely earns 440 coins from eight first victories. Its
observed starting 180 and tutorial gift of 100 bring the wallet to 720; actual
Tula and Moonlight draws spend 180 + 260 and leave 280. The equipped seven-slot
portrait, balance and receipt survive a distinct cold process with no further
debit. The separate fresh F AVD is normally stopped with userdata retained.

E's two compact Collection/Atelier findings are closed in the inspected F
frames. Earlier wardrobe status contrast is retained at **5.45:1**, up from
C's sampled **1.93:1**. Documented art limits remain: a thin sole-like edge under
Jelli's front shoe, free styles supplied as surrounding accents, and some high
collars close to the chin. This is a bounded inspection, not every-pixel or
universal accessibility acceptance. Failed and intermediate runs retain their
own identities in the [qualification register](validation/v1.17/README.md).

The [F sparse host rehearsal](validation/v1.17/ci-host-rehearsal-f/summary.json)
passes **14 configured commands from exactly 135 files**. It includes
**202,444** root-layout assertions over 420 configurations, **10,922** scene and
gesture checks, **19,857** sprite geometry checks, **1,714** audio DSP/lifecycle
checks and **18** website controller tests. The exhaustive relay model passes
**13,312,469** checks over 1,000 boards, 191,954 states and 471,517 transitions.
These host results are separate from native gameplay and remote Actions.

The [DSP report](validation/v1.17/audio/scenes/final-b/verification.json) retains
its historical directory name; its Synth/AudioEngine source hashes equal F.
It covers 50 loops and 14 effects, with preserved historical puzzle PCM. Four
10-second previews use the actual app synthesizer, biome 0 and gain 0.27.
Installed audio separately verifies all four place contexts, HOME/resume and
independent music/effects toggles through AudioFlinger. The emulator has no
host audio output: physical timbre/speaker listening and competition with an
external focus owner were not tested.

The native transition recording establishes observed destinations and fades.
Its SDK capture cadence and timestamp gaps do not establish 60 fps phone
performance. Reduced motion and lifecycle/input guards also have separate
executable host and native checks.

## Remaining final gates

- Complete the supplementary host and native checks of retained modes.
- Complete and independently aggregate the actual F U1–U1000 native replay.
- Stage the four exact canonical delivery files and their manifests.
- Prepare and accept the sixteen-image FR/EN site against that replay proof.
- Publish immutable Git/Firebase commits, verify remote CI and compare every
  public HTTPS payload against its deployment receipt.

The replay uses a retained profile that already genuinely earned three stars
on all U boards. It plays each board through actual Android touches, preserves
profile/ownership/home/stock/settings and must award **zero duplicate first-win
coins**. It is distinct from the eight new-player first victories. Frozen
certificates guide input; no engine win hook or state seed is used.
[Replay protocol](../scripts/v117-relay-ui/README.md) ·
[Live handoff/progress pointer](validation/v1.17/relay/active-handoff.json).

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
