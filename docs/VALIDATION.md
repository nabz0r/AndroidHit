# Validation — Luma Drift Lab 1.2.0-sandbox

Evidence collected **26 October 2026**. Current source builds the separate Lab package `com.androidhit.lumadrift.sandbox`. The [stable 1.1 report](VALIDATION_1_1.md) remains a historical record for the offline app. A simulation, installed interaction, signature check and commercial result are distinct evidence.

## Build and exact package

| Check | Observed result |
| --- | --- |
| Toolchain | Java 17, Gradle 8.14.3, AGP 8.11.0, compile/target API36, build-tools 36 |
| Release | `assembleRelease bundleRelease lintRelease` succeeds with R8 and resource shrinking |
| Lint | [0 errors, four advisory warnings](validation/lab/lint.txt): target/compile API37 availability, Gradle 8.14.6 availability and a preview lint release |
| APK | [One RSA-3072 signer; scheme v2 verifies](validation/lab/apk-signature.txt) |
| AAB | [bundletool validates](validation/lab/bundle-validation.txt); [JAR signature verifies](validation/lab/aab-signature.txt) with self-signed/timestamp/streaming-layout qualifications |
| Package | [Version 1.2.0-sandbox / code 3, minimum API26, target36](validation/lab/package-badging.txt) |
| Source/artifact match | Every source-input hash and artifact length/hash matches [the manifest](../releases/manifest-1.2.0-sandbox.json); [audit](validation/lab/manifest-audit.json) |

Latest Google/AndroidX dependencies contain Kotlin 2.4 metadata. The build uses the experimental standalone lint override **9.4.0** so the analyzer can read it while retaining the tested AGP/Gradle toolchain. This is documented configuration, not a suppressed incompatibility error. Gradle also reports deprecated build features; compatibility with a future major Gradle version is not established.

The merged-manifest audit lists all SDK permissions/providers. Firebase's automatic initialization provider and an advertising auto-init provider are absent. AndroidX startup remains. This static audit does not establish absence of every network request. Billing SDK connection/query and explicitly requested Google routes can contact Google; the optional app journal is separate.

## Deterministic simulation and persistence

| Suite | Passing result | Coverage |
| --- | ---: | --- |
| [Engine](validation/lab/engine-tests.txt) | **435,314 assertions** | 240 Journey, 240 Remix, 1,002 event seeds and 1,002 paired-switch seeds; verified solutions, original fingerprints, undo/restart/gates, restore and challenge parser/fuzzing |
| [Data](validation/lab/data-tests.txt) | **7,083 checks** | Campaign/Remix rewards/unlocks, replay prevention, all Remix completions, shop, six-language completeness/formatting, dates/events and catalog strings |
| [Meta](validation/lab/meta-tests.txt) | **362 checks + valid JSON round trip** | XP, missions, migration, failed-commit retry, date/DST boundaries, bounded optional journal and Unicode/control characters |
| [Economy](validation/lab/economy-tests.txt) | **3,474 checks** | Bounded logarithmic bands, affordability/integer limits, strict unique grant IDs, duplicates, wallet cap, pending retry and Android preference memory-before-disk-failure modeling |
| [Collection/calendar](validation/lab/collection-events-tests.txt) | **87,121 checks** | 24 unique companions, six frozen legacy entries, budget arithmetic, **604 time zones / 14,496 boundary cases**, independent date oracle, Monday/year/leap-day/DST and final-millisecond rounding |

The engine exercises 1,543 alternate legal starts; 53 correctly require undo. Published boards have verified complete solutions, but every possible move order need not remain solvable. Preference tests use Android stubs with explicit failure modeling; they do not establish physical disk durability, server transaction safety, human difficulty, enjoyment or retention. Item-price bands depend only on distinct Journey/Remix clears, never spending, failures or advertising choices.

## Actual Android interaction

The Lab APK was installed on an isolated ARM64 Google APIs Android 16/API36 emulator at 1080 × 1920. Host sound was disabled. No advertising install link, share destination or real payment action was selected.

### Companions and events

The [full UI run](validation/lab/collection-events-ui-smoke.txt) browsed all 24 cards on three pages and all six themes, cancelled previews without spending, bought Miko for exactly 160, confirmed an owned companion without another debit and equipped Nova for free.

It solved all **three daily and seven weekly boards through actual touch controls** using certified solutions. Daily first clears paid 270 plus the 100 completion bonus; weekly clears paid 945 plus 250. Repeated completion-bonus claims paid zero. Collection and both event screens were exercised in EN/FR/DE/ES/PT/IT; accessible summaries and screen bounds passed, then English was restored. The crash buffer was clean. The [final-binary recheck](validation/lab/collection-final-ui-smoke.txt) also verified corrected thumbnail spacing, Orbit purchase at exactly 220, duplicate/equip at zero, daily/weekly replay and collected-bonus guards. The [screen guide](COMPANIONS_EVENTS.md) uses real captures.

### Test commerce and consent

The [commerce run](validation/commerce-ui-smoke.txt) exercised default unknown-age/ad-off state, minor blocking, simulator cancel, pending across force-stop/relaunch, then completion. Packs of 500, 1,400 and 3,600 each granted once; repeats granted zero. Starting wallet 180 became 5,680 after these packs. Real checkout remained disabled. Local-journal on/off survived restart.

An actual UMP sample consent form appeared; **Do not consent** was selected. Later privacy choices and revocation/minor blocking were exercised. Google's request-eligibility result is not presented as personalized-ad consent. The sample integration does not qualify a future owner-configured privacy message or establish legal compliance.

An actual official Google rewarded **Test Ad** loaded and rendered. Watching to its earned-reward state and closing it changed the wallet **5,680 → 5,715**, exactly 35 once. No ad install link was followed. The journal was off for this initial ad run; it therefore proves no paid/impression journal record. The [final-binary metrics run](validation/commerce-ad-metrics-smoke.txt) then enabled the journal and observed exactly one impression, one earned reward and one paid-event callback: **USD 0 micros, UNKNOWN precision, test=true**. Wallet **6,900 → 6,935**. The [actual local JSON](validation/lab-ad-journal-export.json) records those callbacks; it is not revenue. Export opened the chooser and was cancelled. Journal and staged exports were cleared, and journal/ads were turned off with age unspecified. The [capture/cleanup report](validation/commerce-capture-refresh.txt) confirms the final state.

Native harness adjustments for Android's uppercase dialog labels, scroll position and asynchronous UMP form arrival are documented in the reports. Failed harness assumptions are not relabeled as application crashes or successful product checks.

### Google account

The [identity run](validation/lab/identity-ui.txt) opened Settings → Account in English and French, confirmed guest state and the missing-configuration explanation, pressed the Google control and confirmed no fake login, wallet credit or advertising opt-in. English was restored and the crash buffer stayed clean.

**Actual Google account selection, Firebase sign-in, sign-out and account deletion have not run**: the owner's matching OAuth/Firebase configuration is absent. The code compiles against Credential Manager/Firebase Auth; this is not live-login evidence. [Configuration and future acceptance matrix](GOOGLE_SIGN_IN.md).

## Stable regression evidence and limits

The [stable 1.1 UI run](validation/v11-ui-smoke.txt) covers Journey 1–12, Remix paired gates/restore/undo, missions, fair C/R/E friend boards, invalid links, native share/export chooser cancellation, local journal and cancellation of queued Flow on pause/help. The [signed 1.0 → 1.1 upgrade](validation/upgrade-results.txt) preserves progress, partial board, wallet and French and migrates XP once. These remain stable evidence, not newly executed Lab SDK tests.

Historical 1.0 [small-screen/landscape](LAYOUT_RESULTS.txt) and [audio-focus checks](validation/audio-foreground.txt) remain labeled historical. No physical phone, subjective sound audition, battery/thermal benchmark, Bluetooth latency, full TalkBack session or broad API/device matrix is claimed. API26 is declared; an earlier API26 emulator failed to connect and is not a passing compatibility result.

No configured live purchase service, server-authoritative wallet, cloud save, production ad revenue or remote gameplay analytics is present. Local time/data can be changed. Google Play approval, real-account acceptance, retention, acquisition cost, virality and commercial success remain unmeasured. Passing these checks establishes the covered behaviors, not a perfect experience on every device.
