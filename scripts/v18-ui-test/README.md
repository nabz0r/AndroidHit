# Native 1.8 UI qualification

This independent Android SDK instrumentation source set exercises the **installed release APK**, through its real accessibility controls. It adds no test code, dependencies, test hooks or fake stock to the game. It never signs in, opens an ad, purchases currency, injects player progress, edits preferences, uses reflection or sends shell input events. Preference reads are assertions only. All PNGs are the original `UiAutomation.takeScreenshot()` result, encoded directly and copied without retouching.

The original 1.7 harness and its evidence remain unchanged. This harness requires version **1.8/code9** and compares the actual installed APK hash with the frozen APK before testing.

## Build

Generate the real typed optimal actions from the source engine, then build the independent test APK:

```sh
python3 scripts/v18-ui-test/generate-fixtures.py --count 100
JAVA_HOME=/tmp/luma-jdk21/jdk-21.0.13+9/Contents/Home \
  ./gradlew -I scripts/v18-ui-test/init.gradle :app:assembleLabReleaseAndroidTest
```

`StrategyUiFixture.java` obtains each board through `StrategyCatalog`, verifies its exact productive minimum and replays every action before emitting its coordinates. It also finds real level11 states for a gate hint, a gate-first Flow and a blocked-at-zero Undo test. The fixtures contain no account data, rewards or app profile. Their manifest binds them to source SHA-256 hashes. A source oracle is not native execution evidence or measured human difficulty.

## Run on the designated QA emulator

The `gestures` case additionally uses native `UiAutomation.injectInputEvent(MotionEvent)` with touchscreen input, six MOVE events and a roughly200ms drag. Its start point and distance come from the actual visible accessibility node bounds observed immediately before the action; the log retains that node and bounds. It uses no arbitrary desktop coordinates, `adb input`, hidden API or app hook. A wrong-direction practice drag must preserve the phase; the correct drag and a campaign drag must produce exactly one real action.

The wrapper accepts only the isolated root-capable **emulator-5580**, requires actual **16KiB pages**, refuses a stored signed-in Google identity, and checks the frozen APK bytes. It backs up all private preference files outside the repository and restores their complete file set and bytes in `finally`. It also restores the two original rotation settings after tutorial or landscape tests. It does not modify resolution, density, fonts, time or animation settings.

The app APK must already be installed. The wrapper only installs the test APK. Keep private backups outside Git.

First run a small UI pass against a candidate:

```sh
python3 scripts/v18-ui-test/run.py --label candidate-menus \
  --cases menus,map,languages \
  --expected-apk app/build/outputs/apk/lab/release/app-lab-release.apk
```

Then qualify a frozen release with real progression and mechanics:

```sh
python3 scripts/v18-ui-test/run.py --label final-lab --journey-through 100
```

The default expected Lab artifact is `releases/luma-drift-1.8.0-sandbox.apk`. Use `--expected-apk` only to identify another actual frozen candidate; it never installs that candidate automatically. Use a new label for every run. Existing evidence directories are never overwritten.

Evidence is written under `docs/validation/v1.8/ui-sdk-LABEL` and `docs/screenshots/v1.8/ui-sdk-LABEL`. `report.json`, `actions.json`, each accessibility snapshot, and `host-report.json` identify the actual native actions, checks, installed APK, runtime and restoration result. A successful build or a PNG alone is not a passed behavioral test.

## Cases and limits

- `tutorial`: optional automatic invitation is observed only when actually offered. On a preserved older profile, that first-arrival branch is explicitly not claimed. All seven practice lessons require their actual piece/key/gate/preview/Undo/hint controls. Skip retains the precise teaching phase, resume restores it, and rotation recreates the key lesson without losing its state. First completion grants exactly **100 LumaCoins and 2 Sparks**; an existing receipt or repeated claim grants zero additional rewards. Replay is free. No practice phase consumes real stock or campaign progress.
- `gestures`: real wrong/correct-direction tutorial drags and a real campaign drag through Android SDK touch events.
- `menus`: real navigation and ownership labels, earned shop, daily rewards and distinct weekly route.
- `map`: native level jumps to1,7,8,13,100,101,500,1000 and real accessibility scrolling.
- `journey`: actually clears levels1 through `--journey-through`, with certified coordinate actions and typed gate openings. Every opening preview is checked before confirmation. No progress is granted by modifying a save. Initial fixture bundle covers100 levels; regenerate with a larger `--count` before asking for more.
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
  ./gradlew -I scripts/v18-ui-test/init.gradle -PlumaUiTarget=production \
  :app:assembleProductionReleaseAndroidTest
python3 scripts/v18-ui-test/run.py --variant production --cases startup --label final-production
```

The default frozen production artifact is `releases/luma-drift-1.8.0-rc1.apk`. The actual version, installed package identity, APK hash, startup and audio results remain separate facts in the release documentation.

## Genuine first installation on the second device

`run-fresh-tutorial.py` is deliberately separate from the restoration wrapper. It accepts only the owned **emulator-5582**, requires the Lab package to be genuinely absent, and never erases a prior profile to manufacture that condition. It installs the frozen Lab APK and the independent test APK, then runs `tutorial,gestures` with `expectFreshTutorial=true`.

```sh
python3 scripts/v18-ui-test/run-fresh-tutorial.py
```

Evidence is written to `ui-sdk-fresh-guest` in the1.8 validation and screenshot folders. The script asserts the automatic optional tutorial actually appears on first arrival, verifies the same interactive lessons/reward/gesture behavior, restores original rotation settings, and checks the production APK bytes remain unchanged. The newly created local QA guest profile is retained and stopped. It does not touch a Google account or an existing Lab profile. This second Google Play emulator uses its actual4KiB runtime; the separate main regression device provides16KiB coverage.
