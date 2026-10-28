# Native 1.10 UI framework and identified run reports

This is a separate copy of the recorded [1.9 framework](../v19-ui-test/README.md), targeting **1.10/code 11** through `com.androidhit.lumadrift.qa.v110`. Historical source, captures and reports remain unchanged. The [preparation inventory](../../docs/validation/v1.10/ui-framework/preparation.json) records the initial source-only copy; later compilation and device runs have separate, identified reports below. Its original no-execution fields describe that preparation step, not the current release status.

The independent SDK instrumentation still operates the installed game through actual accessibility controls and native touchscreen events at observed control bounds. It does not seed progression, fabricate stock, sign in, open ads or make purchases. The restoration wrapper refuses a stored Google identity, verifies the exact installed APK against the selected frozen artifact, preserves private preference files outside Git and restores their file set/bytes and original display/rotation in `finally`. The fresh-install helper requires the Lab package to be genuinely absent and retains the new guest; it never deletes an old profile to manufacture a first arrival.

## Recorded runs and current boundary

The selected packages are now **1.10.0-rc3 / 1.10.0-sandbox-rc3, code 11**. Current Lab SHA-256 is `d1b085b4d9bbcf450037069ed15c7cea89b0e3e759b16e587cf3ca016a7c493f` and production SHA-256 is `29a68cdcaeb61850d55afd101f03bd8c42a075cc6d6631205fc176e499dd70b4`. [Current qualification status and methods](../../docs/validation/v1.10/SDK_UI_FINAL.md).

[Fresh I](../../docs/validation/v1.10/ui-sdk-fresh-guest-i/host-report.json) passes **91 checks / 17 PNGs** on the exact rc3 Lab, API 36 ARM64 with actual 16 KiB pages. The [main rc3 run](../../docs/validation/v1.10/ui-sdk-final3-full100/host-report.json) passes **2,514 checks / 96 original PNGs**, including all 300 completion/three-star/budget assertions across Journey 1–100, fourteen actual selected pixels and four complete footer targets. Original private profile files and rotation settings were restored. The separate [rc3 production startup](../../docs/validation/v1.10/ui-sdk-production-final3/host-report.json) passes **7 checks / 1 original PNG**, with its original environment restored; its scope is startup/lifecycle/digital audio only.

Earlier rc1/rc2 runs remain **intermediate**: rc1 [full 2,472/92](../../docs/validation/v1.10/ui-sdk-final-full100/host-report.json), [fresh G 91/17](../../docs/validation/v1.10/ui-sdk-fresh-guest-g/host-report.json) and [production 7/1](../../docs/validation/v1.10/ui-sdk-production-final/host-report.json); rc2 [full 2,500/92](../../docs/validation/v1.10/ui-sdk-final2-full100/host-report.json) and [fresh H 91/17](../../docs/validation/v1.10/ui-sdk-fresh-guest-h/host-report.json). Their exact original hashes stay in their reports and do not qualify rc3. The [raw companion-pilot FAIL](../../docs/validation/v1.10/ui-sdk-companion-pilot/host-report.json) and [earlier fresh F](../../docs/validation/v1.10/ui-sdk-fresh-guest-f/host-report.json) also retain their intermediate scope. [Retention record](../../docs/validation/v1.10/intermediate-retention.json) identifies eight unreleased rc1/rc2 APK/AAB files preserved byte for byte locally in an ignored build folder, with archived manifests and original reports/captures. Those binaries are not downloadable release files on main.

## Preserved mechanics and pending screens

The default scenario is now `tutorial,gestures,map,journey,recovery`, through **100 real Journey levels**. Tutorial phase/preview skip, resume and actual recreation assertions are retained. Certified typed actions, gate confirmations, star/budget assertions and zero-action Undo remain. Other carried mechanics selectors are `challenge`, `powers`, `landscape` and `guardians`; their source availability does not alone establish acceptance. The linked rc3 report identifies its executed selectors; the broader main route adds menus (including hub), avatar, story, challenge, powers, rewards, styles, guardians, landscape and languages.

The stable 1.10 selectors **`hub`, `avatar`, `story`** are now implemented, alongside adapted `menus`, `rewards`, `styles` and `languages`. They assert explicit selected tabs/parent navigation, no auto-claim from browsing, a preview that does not grant/equip/debit, pinning that changes only `collection_goal_v110`, ten chapters and actual fragment counters, locked Guardian access and recreation. Reward buttons are selected using the actual semantic row and native control bounds. Localized captures include French Home, companion, ready rewards, story, shop appearances and events. Their results and original French captures are recorded in the identified 2,514-check rc3 main report; selector availability by itself is not acceptance.

`event-retry` and `guardian-storage-fault` remain blocked pending their final 1.10 fault scenario. The host refuses them before device access and direct instrumentation rejects them; no requested case is silently skipped. The copied filesystem controller operates only actual directory permissions on an explicitly owned root-capable QA target and reads cache/XML separately. It does not simulate in-memory storage, edit preference values or establish process-death recovery. Its presence is not new 1.10 runtime evidence. The old 1.8 reproduction remains solely in its historical framework; the new wrapper has no `--allow-v18` mode.

The production selector remains restricted to `startup`, requiring exact production 1.10.0-rc3/code 11. It observes lifecycle and digital audio without operating gameplay, provider or payment controls. The linked current rc3 startup report passes seven checks on the exact production hash, with the original environment restored. Its generic wrapper `journey_through:100` field does not indicate production gameplay. Digital audio does not establish physical speaker quality.

## Fixture provenance

All **106 oracle JSON files**, including `assets/fixtures/manifest.json`, are byte-identical to the 1.9 copy. This includes the 100 L boards, recovery/power cases and three G1 encounters. The copied manifest retains its original source paths and hashes as provenance; it is not relabelled as a newly generated 1.10 oracle. The Java fixture generator is unchanged; its Python launcher only points to the new framework folder. No fixture generator was run during preparation. A future regeneration must be explicit and preserve the frozen L/M/T/G1 identities and budgets.

## Reproducing an identified qualification

The commands below reproduce a run; they are not proof of its result. Freeze the game APK, confirm each requested screen contract and build the independent test source set:

```sh
JAVA_HOME=/tmp/luma-jdk21/jdk-21.0.13+9/Contents/Home \
  ./gradlew -I scripts/v110-ui-test/init.gradle \
  -x :app:compileLabReleaseJavaWithJavac -x :app:minifyLabReleaseWithR8 \
  :app:assembleLabReleaseAndroidTest
```

Select an existing explicitly owned guest QA target and a unique evidence label. The main restoration wrapper accepts only emulator-5580/5590 with actual 16 KiB pages; it installs only the test APK and never installs the game APK. Its default expected artifact is `releases/luma-drift-1.10.0-sandbox-rc3.apk`.

```sh
python3 scripts/v110-ui-test/run.py --label candidate-mechanics \
  --cases tutorial,gestures,map,journey,recovery --journey-through 100 \
  --expected-apk /absolute/path/to/frozen-lab.apk
```

The first-install helper accepts the previous explicitly owned devices plus **emulator-5592 / 5594 / 5596 / 5598**, the new Luma-Release110-F/G/H/I targets. F/G/H/I must have actual 16 KiB pages. Every fresh target must have the Lab package genuinely absent; existing devices may already have a profile and are refused. There is no deletion, preference injection or simulated guest profile to make a device eligible.

Reports go to `docs/validation/v1.10/ui-sdk-LABEL`; original PNGs go to `docs/screenshots/v1.10/ui-sdk-LABEL`, copied without retouching. Test files use the separate on-device `v110-sdk-ui` output folder. Unique labels prevent overwriting any previous report. Build success, prepared assertions and original 1.9 passes do not establish a successful 1.10 native run.

## Observed rotation barrier

The 1.10 intermediate menu pilot retained a landscape PNG after a same-named header appeared during return to portrait. Its report/captures remain historical. Rotation helpers now wait up to nine seconds for the actual display rotation, unmodified SDK screenshot dimensions and (on return) native game-view dimensions to match the pre-rotation observation, twice consecutively. They then check the intended screen and saved state. Localized captures also require the initial observed viewport. Poll intervals are not treated as evidence that recreation finished. The host backs up/restores rotation settings for every rotating case, including avatar, story, styles and guardians. This change is test synchronization, not an app fix. Its tutorial branch is included in the identified 91-check fresh I run. The current 2,514-check rc3 report records the broader screen/state outcomes and 28 layout-barrier observations (not a count of rotations); the original returned-portrait companion PNG is 1080×2340.

## Native flat-colour and footer checks

The rc3 main scenario checks actual SDK screenshot pixels for the fourteen selected flat controls (Shop Powers/Appearance, companion Appearance/Equipment, ten chapter chips): centreX and 20%down the observed control, opaque mint `ffe1fff2`, RGB tolerance ±3. Gradient surfaces are excluded. Its four footer targets are Home Enter a challenge, companion Open inventory, notebook Prism guardians and Collection next page with Companions/All. They must reach stable bottom bounds through SDK scrolling, retain at least 47dp of visible height and stay entirely above fixed navigation. The current rc3 action log records all fourteen samples exactly `ffe1fff2` and all four targets at 48.36dp, fully above fixed navigation after three stable bottom observations. Original captures and observed bounds/pixels are logged; prepared checks alone would not establish a native result.
