# Native 1.7 UI qualification with Android SDK Instrumentation

This standalone test source set uses **only Android SDK framework APIs**: `Instrumentation`, `UiAutomation`, native `AccessibilityNodeInfo` / the app's `AccessibilityNodeProvider`, editable dialog actions and `takeScreenshot`. It does not use a browser, desktop automation, coordinate taps, shell input events, hidden APIs, reflection, app test hooks, profile injection, fabricated coins, account login, advertising or payments. No test dependency or test code is added to the delivered game APK.

The init script attaches these sources to the existing `androidTest` source set for the **Lab release variant**. AGP reuses its existing release signing configuration. The script does not read, print or duplicate signing credentials. Application sources remain unchanged by this harness.

## Build

From the repository with JDK 17 and the existing Android SDK:

```sh
python3 scripts/v17-ui-test/generate-fixtures.py
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
  ./gradlew -I scripts/v17-ui-test/init.gradle :app:assembleLabReleaseAndroidTest
```

The first test build may download previously uncached transitive variants of the app's existing dependencies. The runner itself has no external library dependency. Target APK must already be installed at `com.androidhit.lumadrift.sandbox`, version 1.7/code8. This harness does not install or replace the game.

Test-only output:

```
app/build/outputs/apk/androidTest/lab/release/app-lab-release-androidTest.apk
```

## Run, with byte-exact restoration

Only the explicitly designated root-capable QA emulator **emulator-5580** is accepted. The wrapper stops the Lab app, backs up every private preference file outside the repository, refuses a stored signed-in identity, installs only the test APK and runs the SDK runner. Its `finally` block stops the app and restores every original preference file byte for byte, removes preferences newly created during testing, then verifies the complete set and hashes. Backups contain private app data and must **never** be committed.

```sh
python3 scripts/v17-ui-test/run.py --serial emulator-5580 --label pilot1
```

A smaller first pass:

```sh
python3 scripts/v17-ui-test/run.py --label menus1 --cases menus,map,languages
```

The optional `social` case captures the honest guest screen without opening Google or creating friends. The `capsule` case uses the existing earned coin balance only: it checks full item odds before confirmation, one 150-coin debit, the disclosed rarity-based duplicate compensation if any, and free equip. It never opens paid checkout; a profile below 150 earned coins is reported explicitly instead of being seeded.

The `journey` case really plays the first 12 Journey boards using the certified source-oracle fixture and accessible row/column controls, then exercises the normal J13 hint and dead end and clears J13. Temporary legitimate progress/rewards are restored afterward. It does not grant access by editing preferences. The `challenge` case imports the public J13 challenge through the actual native input dialog, solves it by normal piece actions, and verifies it spends no stock and grants no coins. Imported challenge mode deliberately has no hint/power buttons; the separate normal Journey case covers hints.

If today's gift was already collected in the original profile, the test verifies repeated collection adds zero; it does not alter time or erase the original receipt to manufacture a new claim. On an unclaimed day it also verifies the first claim adds exactly the advertised amount.

Evidence is written to `docs/validation/v1.7/ui-sdk-LABEL` and `docs/screenshots/v1.7/ui-sdk-LABEL`. Every PNG is the native SDK screenshot directly encoded as PNG and copied without cropping, retouching or composition. Accessibility snapshots and the host report identify the actual installed APK hash, runtime, checks and restoration result. Use a fresh label for each candidate/run to avoid mixing evidence. A screenshot or successful build alone is not a passed behavioral test; inspect both `report.json` and `host-report.json`.

The framework runner's exit code is checked through its actual JSON result because `am instrument` can exit its shell process successfully even when a test fails. A failing test still runs preference restoration and retains the failure report/capture. The app remains stopped after restoration. No window size/density, font, animation or clock setting is changed. The optional landscape case restores the original rotation and its two recorded settings, as described below.

## Production startup and digital audio observation

A separate SDK-only `ProductionStartupInstrumentation` runner is restricted to the real production package and performs **no gameplay, menu, provider, advertising or payment action**. It launches the actual activity, captures native pixels, reads AudioFlinger twice to verify the same process-owned music track advances frames, sends the app to background through the SDK global HOME action, verifies pause, resumes it through a normal SDK intent, and verifies resumed playback. An original music-off setting is respected rather than silently enabled. AudioFlinger evidence demonstrates digital playback, not physical speaker/headphone audition.

Build it without leaving a Gradle daemon behind:

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
  ./gradlew -I scripts/v17-ui-test/init.gradle -PlumaUiTarget=production \
  :app:assembleProductionReleaseAndroidTest --offline --no-daemon
```

After the main release task installs the frozen production APK:

```sh
python3 scripts/v17-ui-test/run.py --variant production --cases startup --label final-production
```

The wrapper permits only the `startup` case for production, verifies version1.7.0-rc1/code8 and the actual installed APK hash, refuses a signed-in identity, uses the separate production test component, and performs the same complete preference backup/restoration. Test APK outputs remain under ignored `app/build/`; no runner is embedded in the game.

The optional Lab `landscape` case uses `UiAutomation.setRotation` to check level101 below the compact pinned map header with a 48dp touch target, then restores the original orientation. The host additionally records and restores the exact original `user_rotation` and `accelerometer_rotation` settings in `finally`. It never changes window size/density, fonts, time or animation settings.
