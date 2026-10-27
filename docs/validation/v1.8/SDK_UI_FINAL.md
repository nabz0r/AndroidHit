# Android SDK qualification — Luma Drift 1.8

The frozen 1.8 APKs were installed and exercised on two real Android emulator runtimes. The main Lab run completed Journey levels **1–100 through actual game controls**, then stopped on an outdated selector in the test harness during the English wardrobe capture. Its raw global result remains **FAIL**. The corrected menu/language run passed separately against exactly the same game APK. No game rebuild was needed for that selector correction.

| Run | Raw global result | Successful assertions | What this run establishes |
| --- | --- | ---: | --- |
| [final-full](ui-sdk-final-full/host-report.json) | **FAIL — harness selector** | 2,015 | Seven interactive lessons, genuine touch drags, Journey 1–100, gate previews/confirmations, free recovery at zero, typed gate hint/Flow, landscape map checks; then a missing wardrobe selector in the language capture routine |
| [final-menus](ui-sdk-final-menus/host-report.json) | **PASS** | 47 | Corrected menu selectors; native Home, map and practice captures in EN/FR/DE/ES/PT/IT, plus wardrobe/rewards/key lesson in EN/FR |
| [fresh-guest](ui-sdk-fresh-guest/host-report.json) | **PASS** | 77 | Genuine first Lab installation on the second emulator; automatic optional practice, skip/resume, all seven lessons, exact-once welcome gift and actual touch drags |
| [final-production](ui-sdk-final-production/host-report.json) | **PASS** | 7 | Actual production startup and digital music playback, background pause and foreground resume |

These are assertion counts from overlapping runs, not counts of unique scenarios or independent human playtests. The native [reports](ui-sdk-final-full/report.json), [action log](ui-sdk-final-full/actions.json) and [failure](ui-sdk-final-full/failure.txt) preserve the complete first run, including its failure. The successful [menu report](ui-sdk-final-menus/report.json) does not retroactively turn the earlier global result into PASS.

## Exact application and test identity

Both variants are version 1.8/code9. The main regression device is Android 36/arm64 with actual **16,384-byte pages**, 1080×2340 pixels and density 440. The second Google Play emulator is Android 36/arm64 with actual **4,096-byte pages**, 720×1600 pixels. These are the recorded runtimes; this is not a claim about every Android device or physical hardware.

| Frozen game APK | SHA-256 |
| --- | --- |
| [Lab 1.8.0-sandbox](../../../releases/luma-drift-1.8.0-sandbox.apk) | `f93467a11073a0f01c9b09ae6e73aebe211f700661a21c54097cebf1063a034d` |
| [Production 1.8.0-rc1](../../../releases/luma-drift-1.8.0-rc1.apk) | `aab583241c42179e5cffb80bdd32f9a502be124a3e8a75ccf94936e538db87d4` |

The wrappers compare the actual installed APK bytes with the frozen artifact. APK integrity, Android signing/alignment and AAB checks are separate release checks; a successful Gradle build is not substituted for native execution.

| Test execution | Test APK SHA-256 | Runner source SHA-256 |
| --- | --- | --- |
| final-full and fresh-guest | `d9a19c06c9faa514e2eaf67a469ac284c9c7bf0eae1f610b3b9b4660559016b3` | `e78db0c617f47b19aa6e779e5c1a921aaa5daf1ac42d00e34824a93b623781fa` |
| final-menus | `eae117d4e8be88fdb057dd895d99c77dc5846f5824cf97d6cf73994e7de68d64` | `459c2282579a5b063cc5b7f4fbf32e5d66107fc268e109c21e8ba46d775040c2` |
| final-production | `152f936a23956e4c41fcc3a598c88d9d10cb190ae51d63419b00e1a07ae31f78` | `994847aa5898fc1a4f94946b6e8cc33d515e36e584becd71ac80683cbe0808f5` |

The exact [runner before the selector correction](sdk-runner-before-selector-fix.java) is retained as read-only evidence; its bytes match the first source hash. The [corrected runner](../../../scripts/v18-ui-test/java/com/androidhit/lumadrift/qa/CandidateUiInstrumentation.java), [production observer](../../../scripts/v18-ui-test/java/com/androidhit/lumadrift/qa/ProductionStartupInstrumentation.java), [restoring wrapper](../../../scripts/v18-ui-test/run.py), [fresh-install wrapper](../../../scripts/v18-ui-test/run-fresh-tutorial.py) and [usage instructions](../../../scripts/v18-ui-test/README.md) remain independently inspectable. The source-certified [fixture manifest](../../../scripts/v18-ui-test/assets/fixtures/manifest.json) binds the action oracle to the engine source.

## What was actually played

The SDK runner reads each board's certified action coordinates and executes them through the game's real accessible controls. It does **not** insert that history into a save. The app records each exit/opening itself; the runner checks the resulting typed history, action allowance and earned completion. All levels from 1 through 100 were cleared this way on the installed frozen Lab APK. See the original [level 100 start](../../screenshots/v1.8/ui-sdk-final-full/journey-100-start-en.png), [level 100 completion](../../screenshots/v1.8/ui-sdk-final-full/journey-100-cleared-en.png) and [recorded native actions](ui-sdk-final-full/actions.json).

Gate previews must leave history and budget unchanged until confirmation. Real blocked attempts bring a source-certified attempt to zero remaining actions; Undo must still remove the last actual gate opening and refund exactly one action, without currency or stock. A Spark must point to the exact gate action, debit one charge and play no move. Flow must execute its certified gate-and-exit sequence exactly once and charge one action per productive operation. Active repeat taps cannot debit the charge a second time. [Zero-action recovery](../../screenshots/v1.8/ui-sdk-final-full/zero-actions-recovery-en.png), [free Undo](../../screenshots/v1.8/ui-sdk-final-full/zero-actions-undone-en.png), [gate hint](../../screenshots/v1.8/ui-sdk-final-full/hint-gate-en.png) and [Flow](../../screenshots/v1.8/ui-sdk-final-full/flow-gate-sequence-en.png) retain native pixels from these cases.

The gesture cases use real **`UiAutomation.injectInputEvent(MotionEvent)` touchscreen events**. Each origin and distance comes from an actually observed accessible piece's screen bounds, followed by six MOVE events over roughly 200 ms. A wrong-direction practice drag leaves phase/history unchanged; the correct rightward drag advances the lesson once. A normal Journey drag follows the source arrow and records exactly one action. These are actual Android touch events, not `adb input`, desktop coordinate automation, reflection, hidden APIs or an app test hook. [Wrong-direction practice](../../screenshots/v1.8/ui-sdk-final-full/tutorial-wrong-drag-en.png) and [campaign drag](../../screenshots/v1.8/ui-sdk-final-full/journey-native-drag-en.png) accompany the action log.

The seven lessons require the requested piece, blocker, key, gate preview/opening, switch preview, free Undo and free practice hint actions. Skip and resume preserve the exact phase/history. Rotation retains the key lesson; the [compact landscape board](../../screenshots/v1.8/ui-sdk-final-full/tutorial-key-landscape-en.png) is captured directly. First completion grants exactly **100 LumaCoins and two Sparks** with one durable receipt. Repeated claims and replay grant no additional gift; practice spends no real power stock and grants no campaign progress.

## The selector failure and corrected run

After all mechanics and the 100-level Journey had completed, `languages()` looked for a wardrobe control equal to `Dressing room` or starting with `Dressing room,`. The real accessible caption was `Dressing room · 4 / 24`. The harness therefore raised `Missing clickable control`; its 2,015 recorded assertions were all successful, but the requested whole procedure was incomplete. The [failure screenshot](../../screenshots/v1.8/ui-sdk-final-full/failure.png), [stack trace](ui-sdk-final-full/failure.txt) and both raw reports remain intact.

Only the test selector changed to accept the actual wardrobe label prefix. The game APK stayed byte-identical. The separate `menus,map,languages` run then completed with 47 assertions and 37 original PNGs. This run includes all six languages and the intended English/French documentation images: [Home](../../screenshots/v1.8/ui-sdk-final-menus/home-fr-localized.png), [wardrobe](../../screenshots/v1.8/ui-sdk-final-menus/wardrobe-fr-localized.png), [rewards](../../screenshots/v1.8/ui-sdk-final-menus/rewards-fr-localized.png), [map 1000](../../screenshots/v1.8/ui-sdk-final-menus/map-1000-fr.png) and [key lesson](../../screenshots/v1.8/ui-sdk-final-menus/tutorial-key-fr.png).

## Profiles, restoration and provider boundaries

The main emulator tests use a guest profile. Before each restoring-wrapper run, all private preference files are backed up outside the repository; a stored signed-in identity is refused. Every original file is restored byte for byte afterward, and any newly created preference files are removed. `final-full`, `final-menus` and `final-production` each record `restored:true`. The full run also records restoration of both original rotation settings. The menu run did not request rotation changes.

The second emulator had **no Lab package before installation**. The fresh-install wrapper records that absence, installs the actual frozen Lab/test APKs and checks that the optional tutorial really appears automatically. It does not clear an older profile or inject data to simulate first arrival. This newly created local QA guest profile is intentionally retained and stopped; **it is not described as restored**. Its rotation settings are restored, and the separately installed production APK's hash is unchanged before/after testing.

The menu photographs were taken after the previous main-profile restoration: they show a genuine local guest with **zero completed Journey levels**, its real starter ownership and actual practice actions. They are UI evidence, not photographs proving the 100-level completion. The earlier full-run completion records provide that distinct evidence. No run opens Google authentication, exchanges real friends/gifts, watches an advertisement or makes a purchase. Real-provider acceptance is outside this qualification.

## Production audio observation

The production observer launches the installed production APK and checks its own actual process's AudioFlinger track. The same active track advances by **22,491 digital frames**, pauses after the SDK HOME action and resumes when the activity returns to foreground. See [audio-runtime.json](ui-sdk-final-production/audio-runtime.json), the raw AudioFlinger diagnostics in that folder and the [production startup PNG](../../screenshots/v1.8/ui-sdk-final-production/startup-production-native.png).

This demonstrates digital playback and lifecycle behavior. It is not a physical speaker/headphone audition, a music-composition evaluation or proof of performance on every handset.

## Capture curation and limits

Every PNG is a direct Android SDK screenshot encoded once, without cropping, compositing, retouching or translation after capture. [CURATION.json](CURATION.json) lists the exact SHA-256, original repository path, retention decision and local archive location for every curated full/fresh capture. The raw native and host reports are unchanged and still describe the original complete capture sets.

The repository retains **32 full-run, 7 fresh-install, all 37 menu and the 1 production PNG**. The other 81 PNGs were moved byte-identically to `/tmp/luma-v18-superseded-ui/{final-full,fresh-guest}-unselected/`, saving 72,981,066 bytes from the repository payload. Those archive paths are local-only, not downloadable files in a fresh Git checkout. Intermediate accessibility JSON and action/report data remain in the repository. The curated images referenced by this document and the public site remain present.

This SDK exercise covers 100 Journey levels, not 1,000 levels played through the UI. Engine-wide solvability/cost simulations are separate evidence. It checks accessible controls, real touch integration, selected orientation behavior and native pixels; it does not establish human difficulty, enjoyment, retention, commercial success or the quality of every translation. No font-size setting was changed. Enlarged-font/device combinations, a spoken TalkBack audit, complete seven-lesson play in every landscape configuration and prolonged physical-device play remain outside this run's scope.
