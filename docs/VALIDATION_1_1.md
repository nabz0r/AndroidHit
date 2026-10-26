# Release validation — Luma Drift 1.1.0

Evidence collected on **26 October 2026**. Logic, package integrity and installed Android behavior are separate checks. No player study, physical-phone acceptance or commercial outcome is claimed.

## Build and package

| Check | Observed result |
| --- | --- |
| Toolchain | JDK 17, Gradle 8.14.3, AGP 8.11.0, SDK/build-tools 36 |
| Release tasks | `assembleRelease bundleRelease lintRelease` successful; [lint: no issues](validation/lint.txt) |
| APK | One RSA-3072 release signer; [v2 signature verifies](validation/apk-signature.txt) |
| AAB | [bundletool validation passes](validation/bundle-validation.txt); [JAR signature verifies](validation/aab-signature.txt) with self-signed/timestamp/streaming-layout qualifications |
| Manifest | [Version 1.1.0 / code 2, min 26, target 36](validation/package-badging.txt); only VIBRATE permission, no Internet permission |
| Exact bytes | [Release hashes](RELEASE.md), [SHA256SUMS](../releases/SHA256SUMS.txt), [source-input manifest](../releases/manifest-1.1.0.json) |

Gradle also reports its deprecated-build-feature notice. The supplied signing identity is local and private; source hashes do not imply a timestamp-independent reproducible signed binary.

## Logic and persistence contracts

| Suite | Result | Scope |
| --- | ---: | --- |
| [Engine](validation/engine-tests.txt) | **435,314 assertions** | 240 Journey +240 Remix +1,002 event +1,002 paired-switch seeds; exact solutions, frozen original fingerprints, gates, undo, restart, atomic restore and challenge-code parsing/fuzzing |
| [Data](validation/data-tests.txt) | **4,823 checks** | Campaign/Remix unlocks and independent stars, rewards and replay prevention, all 240 Remix completions, milestone claim idempotence, shop, events, dates and six-language completeness/format arguments |
| [Meta](validation/meta-tests.txt) | **362 checks + valid JSON export** | First-clear XP/ranks, migration, missions, failed-commit retry, calendar/DST boundaries, badge, bounded opt-in journal and Unicode/control-character round trip |

The engine checks 1,543 alternate legal starts; 53 lead to states correctly requiring undo. Every published campaign and Remix board has a verified complete solution; not every legal move preserves solvability. The data/meta tests use in-memory Android preference stubs. They do not prove physical disk durability or human difficulty quality.

## Installed Android 16 / API 36

The release APK was installed on an isolated ARM64 Google APIs emulator at 1080×1920. Host sound output was disabled. [Actual v1.1 UI log](validation/v11-ui-smoke.txt) and [reproducible script](../scripts/smoke-v11.py) record:

- Journey 1–12 completed by operating pieces through the actual UI; prior clears returned 0 coins/XP, new clears returned the expected 55 coins/35 XP.
- Remix unlock after 12; paired gates change together; process restart restores board/gates; Undo restores both; Remix 1 gives 71 coins/35 XP and opens Remix 2.
- Two completed daily missions pay 35 and 40 once; repeat claims give 0 and do not alter XP/progress.
- C/R/E friend boards match exact fixtures, have no powers, and yield 0 coins/XP/mission credit. A locked friend Remix does not unlock personal progression.
- An invalid URI leaves the active game intact. Friend code/history survive force-stop/relaunch.
- Sharing opens the real Android image chooser and is cancelled; no destination is selected and no message is sent.
- Journal starts off; opt-in, JSON export chooser/cancel, clear, empty-export behavior and persisted opt-out work. Wallet stays unchanged.
- Pause and help permanently cancel queued Flow actions. Crash buffer stays clean.

The first harness attempt expected native button `Yes`, while Android rendered `YES`; the harness selector was made case-insensitive and the complete sequence then passed. This was a test-selector failure, not an application crash.

## Signed 1.0 → 1.1 upgrade

A genuine 1.0 installation was played before installing 1.1 with `-r` and the same certificate. Three clears migrate to 105 XP without completing today's missions; partial level 4 restores six pieces, balance 300 after a prior purchase remains, and explicit French remains. [Before state](validation/upgrade-before.json) · [Observed checks](validation/upgrade-results.txt).

The final package adds the Nova launcher icon after the full behavior run; it is reinstalled for the final screenshot/launch sequence. [Final-binary record](validation/final-v11-binary.txt). No gameplay code changed between those runs.

## Visual and historical coverage

The [visual guide](VISUAL_GUIDE.md) uses genuine 1080×1920 captures from this release. The [brand kit](store/README.md) is original promotional art, clearly separate from gameplay captures. Screen images are not mockups or claims of market performance.

The original 1.0 [main interaction](SMOKE_RESULTS.txt), [event sequence](EVENT_SMOKE_RESULTS.txt), [small-screen/landscape checks](LAYOUT_RESULTS.txt) and [audio focus excerpts](validation/audio-foreground.txt) remain historical regression evidence. They exercised all daily/weekly stages/bonuses, shop debit, languages, 720×1280 and 1280×720 layouts, embedded license and foreground/background audio focus. They were run on 1.0 and do not become fresh 1.1 device results by being linked here. Frozen campaign/event fingerprints and current data tests protect those content contracts.

## Practical limits

No physical phone/tablet, subjective sound audition, Bluetooth latency, battery/thermal benchmark, full TalkBack user session or large device matrix was tested. API 26 is the declared minimum; an earlier API 26 emulator attempt failed to complete its ADB connection and is not a passing compatibility result. Headless audio execution is not listening evidence.

Dates/storage are user-controlled; there is no server reward authority. Google Play publication, policy acceptance, trademark availability, retention, virality, acquisition cost and real revenue remain unverified. Passing covered tests does not guarantee a perfect experience on every device.
