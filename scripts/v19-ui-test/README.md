# Native 1.9 UI qualification

This independent Android SDK instrumentation source set exercises the **installed release APK**, through its real accessibility controls. It adds no test code, dependencies, test hooks or fake stock to the game. It never signs in, opens an ad, purchases currency, injects player progress, edits preferences, uses reflection or sends shell input events. Preference reads are assertions only. All PNGs are the original `UiAutomation.takeScreenshot()` result, encoded directly and copied without retouching.

The original 1.8 harness and its evidence remain unchanged. This harness requires version **1.9/code10** and compares the actual installed APK hash with the frozen APK before testing.

## Build

Generate the real typed optimal actions from the source engine, then build the independent test APK:

```sh
python3 scripts/v19-ui-test/generate-fixtures.py --count 100
JAVA_HOME=/tmp/luma-jdk21/jdk-21.0.13+9/Contents/Home \
  ./gradlew -I scripts/v19-ui-test/init.gradle :app:assembleLabReleaseAndroidTest
```

`StrategyUiFixture.java` obtains each board through `StrategyCatalog`, verifies its exact productive minimum and replays every action before emitting its coordinates. It also finds real level11 states for a gate hint, a gate-first Flow and a blocked-at-zero Undo test. The fixtures contain no account data, rewards or app profile. Their manifest binds them to source SHA-256 hashes. A source oracle is not native execution evidence or measured human difficulty.

## Run on the designated QA emulator

The `gestures` case additionally uses native `UiAutomation.injectInputEvent(MotionEvent)` with touchscreen input, six MOVE events and a roughly200ms drag. Its start point and distance come from the actual visible accessibility node bounds observed immediately before the action; the log retains that node and bounds. It uses no arbitrary desktop coordinates, `adb input`, hidden API or app hook. A wrong-direction practice drag must preserve the phase; the correct drag and a campaign drag must produce exactly one real action.

The wrapper accepts only the isolated root-capable **emulator-5580** or explicitly owned **emulator-5590**, requires actual **16KiB pages**, refuses a stored signed-in Google identity, and checks the frozen APK bytes. It backs up all private preference files outside the repository and restores their complete file set and bytes in `finally`. It also restores the two original rotation settings after tutorial or landscape tests. The explicit --compact option temporarily configures the owned QA display to1080×1920 through the SDK and verifies original size/rotation restored. Density, fonts, time and animation settings are unchanged.

The app APK must already be installed. The wrapper only installs the test APK. Keep private backups outside Git.

First run a small UI pass against a candidate:

```sh
python3 scripts/v19-ui-test/run.py --label candidate-menus \
  --cases menus,map,languages \
  --expected-apk app/build/outputs/apk/lab/release/app-lab-release.apk
```

Then qualify a frozen release with real progression and mechanics:

```sh
python3 scripts/v19-ui-test/run.py --label final-lab --journey-through 100
```

The default expected Lab artifact is `releases/luma-drift-1.9.0-sandbox.apk`. Use `--expected-apk` only to identify another actual frozen candidate; it never installs that candidate automatically. Use a new label for every run. Existing evidence directories are never overwritten.

Evidence is written under `docs/validation/v1.9/ui-sdk-LABEL` and `docs/screenshots/v1.9/ui-sdk-LABEL`. `report.json`, `actions.json`, each accessibility snapshot, and `host-report.json` identify the actual native actions, checks, installed APK, runtime and restoration result. A successful build or a PNG alone is not a passed behavioral test.

## Cases and limits

- `tutorial`: optional automatic invitation is observed only when actually offered. On a preserved older profile, that first-arrival branch is explicitly not claimed. All seven practice lessons require their actual piece/key/gate/preview/Undo/hint controls. Skip retains the precise teaching phase, resume restores it, and rotation recreates the key lesson without losing its state. First completion grants exactly **100 LumaCoins and 2 Sparks**; an existing receipt or repeated claim grants zero additional rewards. Replay is free. No practice phase consumes real stock or campaign progress.
- `gestures`: real wrong/correct-direction tutorial drags and a real campaign drag through Android SDK touch events.
- `menus`: real navigation and ownership labels, earned shop, daily rewards and distinct weekly route.
- `map`: native level jumps to1,7,8,13,100,101,500,1000 and real accessibility scrolling.
- `journey`: actually clears levels1 through `--journey-through`, with certified coordinate actions and typed gate openings. Every opening preview is checked before confirmation, and each exact productive path must visibly award three stars. No progress is granted by modifying a save. Initial fixture bundle covers100 levels; regenerate with a larger `--count` before asking for more.
- `recovery`: uses legitimately unlocked level11, follows a real gate-opening prefix, then makes genuine blocked attempts until zero actions. Undo must remain accessible and refund exactly the final productive gate action without currency or stock. Run after a sufficiently long `journey` in the same invocation, or on a real already-unlocked profile.
- `powers`: the level11 exact hint must point to a gate, debit one earned Spark and play no action. Active repeat taps must not spend twice. A genuinely unlocked, earned Flow must execute its certified gate-and-exit sequence and charge one action per opening/exit. Stock is never fabricated; insufficient real progress/stock fails explicitly. The default suite plays to at least30 before this case.
- `landscape`: real SDK rotation checks the compact map header, an accessible level101 and a48dp jump target; original settings are restored.
- `languages`: actual native Home, map and saved tutorial board captures in English, French, German, Spanish, Portuguese and Italian, using the visible language settings. English and French also capture the wardrobe, rewards, level1000 and a key tutorial lesson reached through actual practice actions. No captured text or image is translated afterward.
- `challenge` (optional): imports the public typed L13 code through the real dialog, plays its real actions and verifies no campaign reward or stock spend.

The `actions.json` log contains only labels of controls actually clicked. The framework's reported pass/fail is checked because `am instrument` can exit the host shell successfully even when an assertion fails. Failure still retains its capture and runs preference restoration.

## Production startup and digital audio

The separate production runner performs only startup and lifecycle/audio observation. It does not invoke game, menu, identity, ad or payment controls. AudioFlinger observations demonstrate real digital track frames owned by that process, not physical speaker audition. An existing music-off preference is respected.

```sh
JAVA_HOME=/tmp/luma-jdk21/jdk-21.0.13+9/Contents/Home \
  ./gradlew -I scripts/v19-ui-test/init.gradle -PlumaUiTarget=production \
  :app:assembleProductionReleaseAndroidTest
python3 scripts/v19-ui-test/run.py --variant production --cases startup --label final-production
```

The default frozen production artifact is `releases/luma-drift-1.9.0-rc1.apk`. The actual version, installed package identity, APK hash, startup and audio results remain separate facts in the release documentation.

## Genuine first installation on the second device

`run-fresh-tutorial.py` is deliberately separate from the restoration wrapper. It accepts only the explicitly owned **emulator-5584**, **emulator-5586** or **emulator-5588**, requires the Lab package to be genuinely absent, and never erases a prior profile to manufacture that condition. It installs the frozen Lab APK and the independent test APK, then runs `tutorial,gestures` with `expectFreshTutorial=true`.

```sh
python3 scripts/v19-ui-test/run-fresh-tutorial.py --test-apk /absolute/path/to/frozen-ui-tests.apk --expected-apk /absolute/path/to/frozen-lab.apk --label fresh-guest-b
```

Evidence is written to `ui-sdk-LABEL` in the1.9 validation and screenshot folders. The script asserts the automatic optional tutorial actually appears on first arrival, verifies the same interactive lessons/reward/gesture behavior, restores original rotation settings, and checks the production package state remains unchanged. If production is absent on a genuinely new device, both hashes are null and absence is asserted; this is not reported as a successful production APK comparison. The newly created local QA guest profile is retained and stopped. It does not touch a Google account or an existing Lab profile. This second Google Play emulator uses its actual4KiB runtime; the separate main regression device provides16KiB coverage.

## Added 1.9 regression cases and stronger touch scope

The separate package is com.androidhit.lumadrift.qa.v19. Candidate tests normally require 1.9/code10. When root is editing game sources, build only the independent test APK without app compilation/minification by adding `-x :app:compileLabReleaseJavaWithJavac -x :app:minifyLabReleaseWithR8` to the test build command. The task graph packages only the test APK; the wrapper never installs the game APK.

- All 1.9 piece selections use real SDK taps at observed native bounds. Manual gate lesson4 uses the hand coach's15logical-point drift, six MOVE events, a real rotated preview, exact skip/resume, and a physical double confirmation that must record only one opening. Practice remains separate from stocks/progress.
- `rewards`: after actual Journey progression, the earned gift is directly claimable on Rewards and grants its milestone-stable preview item. Six explicit power samples each grant exactly one stock unit and receipt, cost no currency and remove the repeat claim control.
- `styles`: all 12 free styles are selected through native controls, verifying saved IDs, unchanged currency/companion and active style after leaving/reopening the studio. Real rotations must preserve the actual studio pane, as well as the style ID.
- `guardians`: real source-certified Guardian 1 and 5 plans operate visible layers/cellwise pieces. The first hit peels a layer; free Undo restores it and refunds its action. One-cell movement stays present, exact history and actual visible screen survive leave/reopen and rotation, observed one-cell displacement matches the source grid, and explicit claim grants100 earned coins once. The guardians menu also retains its actual pane across rotation. Fixture plans contain no app progress or profile.

Use a new evidence label for every attempt:

    python3 scripts/v19-ui-test/run.py --compact --cases tutorial,gestures --label candidate-tutorial --expected-apk /absolute/path/to/frozen-lab.apk
    python3 scripts/v19-ui-test/run.py --journey-through 100 --cases tutorial,gestures,menus,map,journey,recovery,powers,rewards,styles,guardians,landscape,languages --label candidate-full --expected-apk /absolute/path/to/frozen-lab.apk

The restricted `--allow-v18 --compact --cases gate-drift` mode verifies the exact published 1.8/code9 APK and reproduces the canceled coach gesture. It retains a raw **FAIL** report. Only that unchanged phase-0 failure qualifies as `expected_failure_observed:true`; any other failure or an unexpectedly passing gate is rejected. The source snapshot and reproducible native evidence are in docs/validation/v1.9/ui-baseline-lesson4/. Both private preference bytes and original display/rotation are restored. This baseline does not rebuild the game or seed any app state.

The first-install helper accepts only the explicitly owned QA emulator5584,5586 or5588 and deliberately refuses an existing Lab package. Device B5584 has an older production APK whose bytes must stay unchanged. New devices C5586 and D5588 have no production package; their continued absence is checked explicitly. Prior emulator5582 data is left intact. Fresh qualification requires actual Lab absence and never erases a profile. Pass `--serial emulator-5586` or `--serial emulator-5588` to select the explicit new device and always use a unique evidence label.

The default capture policy keeps original evidence milestones and phase0of each tutorial lesson, rather than every intermediate animation/phase. Every action/check remains in the full JSON logs. Pass `--capture-all` for all diagnostic captures. This affects only evidence volume, never gameplay or which assertions run.


## Explicit filesystem-failure regression

The optional `guardian-storage-fault` case is restricted to the owned guest Lab targets on emulator5580 or5590. It requires Guardian3 to have been unlocked by at least30 real clears in the same run or real existing progress. The source oracle supplies only the authored board and its real first layer-peeling action.

A test-owned file handshake asks the host controller to temporarily set only the private preference **directory permissions** to0500. No saved values, app hooks or clock settings are edited. The actual app performs one native layer hit; the test must see Retry, the pending state in the real preference cache, and the unchanged prior state in the actual XML file. Back must preserve the blocked panel and pending hit. The host then restores the exact original permission mode. A native touch at60% across and50% down the Retry control's observed bounds must dismiss the blocked panel and persist exactly the single pending hit. This point overlaps an underlying piece region in the former buggy UI, so accessibility activation alone would miss the regression.

After completing the actual Guardian3 board, a second bounded permission fault targets its100-coin reward. The UI must retain a single pending balance/receipt in memory while the actual disk file still has the old balance and no receipt. A retry while still locked and a real landscape/portrait activity recreation must stay recoverable without a crash. After permissions are restored, native Retry must make both balance and receipt durable; another claim adds zero coins. No receipt is erased to prepare the test.

`storage-fault-infrastructure.json` records original/final modes, both bounded lock/restore cycles, handshake events and cleanup result independently of the game's assertions. The controller restores permissions in `finally` even if the native test fails, before the normal wrapper restores all original preference files byte for byte. Its source hash, the wrapper hash, native action log and separate `guardian-storage-fault.json` bind the scope. This is an infrastructure fault experiment, not player-state injection or ordinary successful-storage coverage.

    python3 scripts/v19-ui-test/run.py --compact --journey-through 30 --cases journey,guardian-storage-fault --label candidate-storage-fault --test-apk /absolute/path/to/frozen-ui-tests.apk --expected-apk /absolute/path/to/frozen-lab.apk


For parallel exception testing, QA device E5590 is an explicitly owned new official API36 Google APIs16KiB emulator. The wrapper still requires the actual page size and guest-only identity checks. Its initial preferences must be created by a normal first launch of the installed game; no profile values are seeded. The separate30-level run earns Guardian3 legitimately. The ordinary full100 suite remains on5580, with its independent verdict. Neither5582 nor arbitrary serials are accepted.
