# Native motion renderer qualification

This observer calls the game's public pure portrait renderer on actual Android
Canvas bitmaps. It opens no Activity and calls no GameStore, puzzle engine,
touchscreen input, ownership or save API. It checks decoded authored frames,
records contact sheets and records the native cache budget. A successful run is
rendering evidence; anatomical garment fitting and visual quality need inspection
of the resulting images and actual app screens separately.

The host accepts only the receipt-owned `Luma118-Expedition-QA` device on
`emulator-5594`. The creation receipt SHA, frozen game APK SHA, frozen observer
SHA, release signer and version/code must match. It installs and removes only its
separate observer. It never installs, upgrades, clears, uninstalls, starts or
creates the target game/device; it force-stops the game before the isolated run.
Existing profiles and other devices are outside its scope.

First compile the final release variant and build the observer:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:assembleLabRelease --no-daemon
python3 scripts/v118-motion-render/build-observer.py --variant lab
```

The owner installs the final candidate on5594 independently. Freeze its exact bytes
and the observer bytes, then run with a new label:

```bash
python3 scripts/v118-motion-render/run.py \
  --serial emulator-5594 --expected-avd Luma118-Expedition-QA \
  --creation-receipt /absolute/path/to/device-creation/creation.json \
  --creation-sha256 EXACT_CREATION_SHA256 \
  --expected-apk /absolute/path/to/frozen-final-lab.apk --sha256 EXACT_APK_SHA256 \
  --test-apk /absolute/path/to/motion-observer.apk --test-sha256 EXACT_OBSERVER_SHA256 \
  --variant lab --expected-version EXACT_1.18_VERSION --expected-code 25 \
  --complete true --label final37-motion-a
```

`--complete true` requires all37 identities ×4 authored forms ×20 poses:

- 148 authored source sheets and2960 exact registered frames.
- Two looks across every frame: body and dew outfit with head/aura/charm gear.
- All24 existing garments separately on eight Nova poses.
- 6112 total composites,296 character contact sheets and24 garment sheets:
  320 native PNG images.

`--complete false` permits an incomplete pilot asset set. Every packaged sheet
still needs20 poses and all its frames/looks must render. This mode is explicitly
recorded and cannot qualify final37 coverage.

Evidence is written once to
`docs/validation/v1.18/native-motion-render/<label>/`. A UUID binds the current
instrumentation and pulled directory, preventing a previous report from passing.
Every PNG filename, SHA-256, dimensions and total count are checked against the
native manifest. Registry coverage, metadata/decode failures and cache budget are
checked independently. The installed game APK is pulled and hashed before and
after. Only the observer APK is removed at the end; native outputs are retained.

The host freezes all game Java sources, observer sources and compiled release API
classes, checking that compiled classes are at least as recent as corresponding
sources. It records every packaged v118 JSON/WebP byte hash and requires the APK
asset set and bytes to equal the current source assets, including metadata image
hashes. Sources, compiled API classes and asset hashes must remain unchanged
throughout the run. These are reproducible source/build witnesses, not a formal
proof that Java source maps to APK dex; the tested installed APK itself is bound
by its exact hash. No source changes or asset generation should overlap a final
run.

The generated art metadata separately records `anchorsMeasured` and
`apparelQualified`. Render success never changes those flags or upgrades a
provisional anchor into a measured/visually qualified fitting.
