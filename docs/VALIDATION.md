# Luma Drift 1.5 validation

27 October 2026 · **production 1.5.0-rc1 / Lab1.5.0-sandbox, code 6**. This report identifies executed checks and their boundaries. [Exact artifacts/signing](RELEASE.md) · [Release behavior](RELEASE_1_5.md).

## Automated checks on integrated sources

| Area | Passed assertions | Evidence |
|---|---:|---|
| Board engine, solutions, undo, snapshots and sharing | 437,039 | [Engine](validation/v1.5/engine-tests.txt) |
| Per-run budgets, old policies and extensions | 390,327 | [Budget](validation/v1.5/move-budget-tests.txt) |
| 1,000 + 240 layouts and structural difficulty | 161,985 | [Progression](validation/v1.5/difficulty-tests.txt) |
| Keys, fixed walls, exact oracle and reversibility | 10,425 | [Sealed engine](validation/v1.5/sealed-tests.txt) |
| Earned economy, equipment and power transactions | 6,608 | [Economy](validation/v1.5/economy-tests.txt) |
| Storage, catalog bounds and localized data | 8,787 | [Data](validation/v1.5/data-tests.txt) |
| First clears, missions, migration and local journal | 362 + JSON export | [Meta](validation/v1.5/meta-tests.txt) |
| Companions and event clock boundaries | 87,121 | [Events](validation/v1.5/collection-events-tests.txt) |
| Billing policy and production copy | 248 | [Billing](validation/v1.5/billing-tests.txt) |
| Wallet recovery lifecycle, provider doubles | 19 | [Recovery](validation/v1.5/wallet-recovery-tests.txt) |
| Continuation receipts and memory-before-disk failure | 42 | [Receipts](validation/v1.5/commerce-receipts-tests.txt) |
| Audio state/focus model and actual PCM DSP | 36 + 65 | [Audio](validation/v1.5/audio/README.md) |

These assertions include independent oracles, concurrency, malformed input, period boundaries and persistence failures. They are not numbers of players, frames or completed online transactions. SharedPreferences/provider adapters isolate unit failure cases; installed tests cover actual Android behavior separately.

## Build and package integrity

Both signed APK/AAB variants and both release lint tasks passed with the final source set. [Gradle log](validation/v1.5/android-build.txt). Java 17 source was compiled under the recorded JDK21 toolchain. The pinned lint analyzer can run on this runtime. Existing deprecation/SDK warnings are recorded; no lint error is hidden.

The [binary audit](validation/v1.5/binary-final-candidate.json) checks source/outputs freshness, hashes every source input and rechecks that bytes remain stable during verification. APK signature, pinned AAB payload signatures, official bundletool validation, 16 KiB ZIP alignment and ELF load segments pass. [APK signatures](validation/v1.5/apk-signatures.txt) · [AAB signatures](validation/v1.5/aab-signatures.txt) · [Bundle validation](validation/v1.5/bundletool-validation.txt) · [Native alignment](validation/v1.5/native-elf-alignment.txt).

All four final packages were independently downloaded from public main after publication; sizes and SHA-256 hashes match the tested local artifacts. [Public delivery proof](validation/v1.5/publication.json). This adds distribution integrity evidence, not another device run.

## Installed Android acceptance

**Final production APK: `33c4dbd6c14fa3e72ec52c7d98475b1a9738b175bad9216c4bc4c7c5bdd817a2`.** It was installed and launched on Android 16 / API 36 emulators with ordinary and actual 16 KiB page kernels. The final Lab APK is independently signed/validated but was not reinstalled after the isolated share-card correction.

The full functional pass ran on the immediately preceding production/Lab candidates, SHA-256 `24a128e3f25fc73e698e46e7d46a30b3358e8d88f08dd59d2b9e8984e3ddf049` / `bb7731632a0523ae9e86f1093fbbab2e82e3bc06acd4d9e3ed4cd289c0a1a065`. The [source-input comparison](validation/v1.5/binary-source-diff.json) establishes that **only `ShareComposer.java` changed** afterward: the final fix adds missing sealed walls and matching key badges to exported images. Engine, data, views, translations, powers, migrations and audio inputs are byte-identical. Earlier reports retain their actual APK hashes; they are not relabelled as final-byte tests.

| Installed check | Executed result and scope | Evidence |
|---|---|---|
| Final EN/FR UI repeat | 14 cases / 24 captures on exact final production bytes; guest profile, lobby, collection/ownership/filter, equipment preview and earned-coin shop inspected; balance 235 / Spark stock 3 unchanged; original English/display settings restored | [Final scoped report](validation/v1.5/ui-smoke.txt), [identity/settings JSON](validation/v1.5/ui-smoke.json) |
| Six-language lobby/collection/shop | 58 cases, 96 PNG/XML captures; English, French, German, Portuguese, Italian, Spanish; 720×1280 and 1280×720; guest status, 24/6/18 collection totals, filters and eight cancelled purchase confirmations preserved balance/stock | [Full predecessor run](validation/v1.5/pre-share-image-fix/ui-smoke.txt) |
| All six powers | Spark, Vision, Flow, Shield, Focus and Trail activated; exactly one stock debit, active reuse rejected, persisted effects restored | [Power run](validation/v1.5/powers-ui-smoke.txt), [hints/effects](validation/v1.5/effects-hint-ui-smoke.txt) |
| Flow interruptions | Pause, help and process recreation freeze pending exits, then finish exactly three actions for one item; last-piece victory credits once | [Executed scenarios](validation/v1.5/powers-ui-smoke.txt) |
| Protection ordering | Shield absorbs three blocked taps, equipped Charm one, fifth costs one action; remaining charges survive recreation | [Executed scenarios](validation/v1.5/powers-ui-smoke.txt) |
| Equipment and final Journey board | Cancel costs zero; Starfold confirm debits exactly 600 **synthetic earned coins**, equips once and survives recreation. Zone 34 contains 991–1,000, actual board 1,000 solves for three stars; no 1,001 | [Executed scenarios](validation/v1.5/powers-ui-smoke.txt) |
| Genuine active-save upgrade | Actual 1.4 UI created the interrupted run; no seeding. Board/history/budget/run ID/coins/stock retained; new attempt uses new policy | [Upgrade JSON](validation/v1.5/active-save-upgrade.json) |
| All six P challenges | Actual taps solve P1–P6; blocked ray/key/open/Undo/recreate verified; zero coins/XP/items/progression changes | [Predecessor gameplay](validation/v1.5/sealed-ui.txt) |
| Final P sharing and key regression | P1/P6 Android-generated cards display matching walls/keys; real choosers opened and cancelled; final P1 block/key/Undo repeated | [Share run](validation/v1.5/sealed-share-ui.txt), [visual review](validation/v1.5/sealed-share-visual-review.txt), [final gameplay](validation/v1.5/sealed-final-key-undo.txt) |
| Old share-image compatibility | Actual C1 exported PNG before/after is byte-identical | [PNG comparison](validation/v1.5/legacy-share-regression.json) |
| Final Android ambient playback | AudioFlinger track advances 22,491 frames, pauses in background and resumes in foreground | [Final runtime JSON](validation/v1.5/audio-runtime.json) |

The power/equipment/boundary tests used a disclosed temporary **Lab-only synthetic profile**: Journey access up to 1,000, 10,000 local coins, six items per power and an equipped Charm. Those are fixtures, not earned progress or paid transactions. Original preferences were backed up and restored byte for byte; test-created files were removed and temporary native window-animation settings restored. P gameplay and the active-save upgrade used actual UI without preference seeding. [Power profile](validation/v1.5/powers-profile.json) · [Hints profile](validation/v1.5/effects-hint-profile.json).

Driver timing pilots remain labelled in their filenames; early taps during closing native dialogs were corrected in the driver. Only the passing reruns establish those cases. Reports under `prefinal-build4` are historical and exposed copy ambiguities subsequently corrected. No Google login, ad reward or real checkout is inferred from these UI runs.

Landscape coverage establishes bounds/navigation at the captured size. The Canvas interface reduces the portrait column in landscape; physical readability, comfortable targets and comprehensive TalkBack usability remain open. Screenshots are actual emulator captures; [the visual guide](VISUAL_GUIDE.md) distinguishes final EN/FR captures from earlier unchanged-layout observations.

## Remaining acceptance

Physical speakers/headphones, modest/modern phones, native-speaker review, first-time-player comprehension, D1/D7 cohorts and market performance are unmeasured. Emulator frame/track evidence cannot establish tactile quality, physical loudness or retention.

Commercial/provider qualification remains separate: Android Google login success, actual installed UMP acceptance/refusal, live ad readiness, Play product/license-test purchases and full pending/refund/RTDN/scheduled reconciliation remain open. The shipped quality candidate uses sample ads and blocks checkout; no simulation establishes revenue or production approval. [Owner activation](PRODUCTION_ONBOARDING.md).

Historical results retain their original scope: [1.4](VALIDATION_1_4.md), [1.3](VALIDATION_1_3.md), [1.2](VALIDATION_1_2.md), [1.1](VALIDATION_1_1.md).

## Website presentation evidence

The 1.5 public-site/GitHub refresh is separate from Android runtime acceptance. Its proof set is [`validation/site-v1.5/`](validation/site-v1.5/) with local website captures under [`screenshots/site-v1.5/`](screenshots/site-v1.5/). [Static checks](validation/site-v1.5/static-check.txt), [18 FR/EN responsive states and synthetic-only deletion cases](validation/site-v1.5/browser-check.txt), and [24 deletion-core tests](validation/site-v1.5/deletion-core.txt) passed locally. No real identity/deletion is inferred, and Hosting deployment still requires its actual result/served-resource record. The earlier public-page/contact browser run and Hosting observations retain their recorded dates/revisions. [Web acceptance boundary](PUBLIC_PAGES.md#15-visual-refresh-and-new-evidence) · [Shared design system](DESIGN_SYSTEM.md).

The [website asset provenance](validation/site-v1.5/source-assets.json) preserves 12 exact copied images/vectors and their actual source scopes; [the 23-file public manifest](validation/site-v1.5/source-manifest.json) binds local checks to source bytes. Neither manifest is a Hosting publication result.
