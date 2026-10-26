# Release validation — Luma Drift 1.0.0

Evidence collected on 26 October 2026. This document separates static/build checks, engine/data tests, installed-app interaction and untested physical behavior.

## Build and package

- JDK 17, Gradle 8.14.3, Android Gradle Plugin 8.11.0; SDK/build-tools 36.
- `assembleRelease`, `bundleRelease`, `lintRelease`: successful. The final Android lint text is **No issues found**. Gradle reports its own deprecated-build-feature notice; compilation uses some platform APIs retained for compatibility.
- APK signature: verifies with v2, one RSA-3072 release signer. [Signature output](validation/apk-signature.txt).
- AAB: bundletool 1.18.1 `validate` exits 0. [Output](validation/bundle-validation.txt). JAR signature verifies with the qualifications documented in [RELEASE.md](RELEASE.md).
- Manifest inspection: application ID `com.androidhit.lumadrift`, version 1.0.0/1, min 26, target 36. Only declared permission: `VIBRATE`. No Internet permission.
- [SHA256SUMS](../releases/SHA256SUMS.txt) and [manifest](../releases/manifest.json) record delivered bytes and build-source inputs. This is not a claim that timestamps and a private signing key yield a bit-identical build elsewhere.

## Logic tests

| Suite | Actual result | Coverage |
|---|---|---|
| [Engine](validation/engine-tests.txt) | 190,238 assertions | All 240 campaign boards, 1,002 event seeds, deterministic generation, exact solutions, 804 alternative legal starts, 12 recoverable dead ends, illegal moves, undo, restart and atomic history restore |
| [Data](validation/data-tests.txt) | 3,219 assertions | Unlocks, coins, all 720 campaign stars, repeat-reward prevention, star improvements, shop debit, inventory/equipment, local event periods, unique bonuses, stale-period rejection, language completeness and placeholder consistency |
| [Lint](validation/lint.txt) | No issues found | Android static checks; explicit narrow suppressions for legacy back fallback, intentionally synchronous preference transactions and original legal text |

Data tests use in-memory SharedPreferences stubs and do not establish disk durability or Android process behavior. Those are exercised separately below.

## Installed Android 16 interaction

An isolated read-only AVD based on the Google APIs ARM64 Android 16/API 36 image was used at 1080×2340. The signed release APK was installed and launched. The emulator was headless, software-rendered and host audio was disabled.

[Main interaction log](SMOKE_RESULTS.txt) records:

- Campaign levels 1–6 completed through touch controls, including a switch/gate board.
- Three-star unassisted rewards and two-star assisted reward observed.
- Undo restores the board; a blocked move leaves piece count unchanged.
- Force-stop/relaunch preserves an interrupted game.
- Flow stops remaining delayed actions when the app backgrounds.
- Shop purchase deducts the exact price; a companion purchase/equip is exercised.
- French UI and save survive process restart; all six language choices are exercised.
- Crash buffer clean after this sequence.

The final binary was reinstalled after the last cleanup and used for the event and final layout checks. The cleanup changed redundant API branches, legal-text lint metadata and orientation configuration; its engine/data tests and lint were rerun.

[Event interaction log](EVENT_SMOKE_RESULTS.txt) records the separately executed daily/weekly sequence and bonus claims. The test script uses the same deterministic seeds but operates every piece and button through the installed interface. Its first run stopped on an ambiguous test selector (`Collect` also matched `Collection`); the selector was corrected and the sequence rerun. That harness error was not an application crash.

Screenshots in [screenshots/](screenshots/) are captures of the installed application, not mockups. They include home, gate play, powers, victory, shop, companions, themes, events and language settings. Static source review also corrected delayed-action session targeting, victory-window interaction and stale accessibility actions before release.

[Layout and license checks](LAYOUT_RESULTS.txt) passed at 720×1280/320 dpi, including the weekly bonus control. A 1280×720 landscape resize rendered without a crash; its portrait composition is centered and scaled. The embedded license dialog was opened. Audio-focus excerpts show Luma Drift owning focus in the [foreground](validation/audio-foreground.txt) and releasing it in the [background](validation/audio-background.txt).

[Final binary check](FINAL_BINARY_CHECK.txt) reinstalled the last APK and verified interrupted event continuation after the last home-caption correction.

## Limits

- No physical Android phone or tablet was tested in this session. Android 8/API 26 emulation was attempted, but its ADB shell/install connection did not complete; it does not count as a passing compatibility test.
- Headless emulation verifies that the audio code can run with the game; it does not provide subjective listening, speaker quality, Bluetooth latency or acoustic evidence. DSP buffer measurements are documented in [ART_AUDIO.md](ART_AUDIO.md).
- No full TalkBack user session, accessibility certification, low-end battery benchmark, large device matrix or real-world retention study was performed.
- Event dates and local storage are user-controlled. No anti-cheat, server reward authority or real-money store is claimed.
- Google Play publication, policy acceptance, trademark clearance and commercial performance remain unverified.

A passing logic or emulator test is evidence for the checked behavior, not a guarantee of a perfect experience on every device.
