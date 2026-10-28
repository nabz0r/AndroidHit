# Native 1.15 interface qualification

This observer is independent of the preserved 1.14 learning runner. Its application ID is `com.androidhit.lumadrift.refontetest`. It targets only the newly created, task-owned `Luma-Refonte115-QA` AVD and an explicitly selected emulator serial. It never targets a historical/user device, rewrites saved progress, calls app rendering or private engine methods, buys an item, opens an ad or signs in.

The prepare route uses actual native accessibility bounds and touchscreen events: dismiss the optional first-touch practice, choose English, browse all four destinations, select all three rooms, zoom in/out, enter/leave decorating, inspect companion/equipment/catalogue/map, then capture Adventure and House in all six supported languages. It finishes with French and the Garden selected through visible controls. A separate force-stopped process verifies the persisted language, room, home layouts/surfaces and equipped choices while checking earned coins/progression remain unchanged. Original, unedited PNGs and corresponding native accessibility snapshots are indexed by exact hashes.

Build after the 1.15 candidate sources are stable:

```sh
./gradlew -I scripts/v115-ui-test/init.gradle :app:assembleProductionReleaseAndroidTest
```

Then freeze the candidate APK and its hash before running the observer:

```sh
python3 scripts/v115-ui-test/run.py \
  --serial emulator-5586 --expected-avd Luma-Refonte115-QA \
  --expected-apk /absolute/path/to/frozen-1.15.apk --sha256 EXACT_SHA256 \
  --test-apk app/build/qualification-artifacts/v1.15/interface-production-test.apk \
  --variant production --expected-version 1.15.0-rc1 --expected-code 17 \
  --label interface-final-a --install-if-absent
```

For a separate compact pass, append `--compact` and use a fresh label. This configures only the owned AVD to880×1440 pixels at440dpi (320dp wide), runs prepare and cold at that actual size, then restores the original size and density overrides explicitly. Both before/during/after values are recorded, and failed restoration prevents acceptance. The app uses portrait orientation; landscape content support is not implied.

For Lab use `assembleLabReleaseAndroidTest`, the `interface-lab-test.apk`, `--variant lab` and its actual version name. A fresh label is mandatory. The wrapper installs only when absent and explicitly requested; an existing candidate must already match the exact frozen bytes. The owned QA profile and candidate are intentionally retained; the isolated observer is removed. It does not clear or fabricate a profile. Its reports do not imply physical-device testing, human comprehension, aesthetic acceptance, provider acceptance or validation of all puzzle solutions. Puzzle-rule qualification has its own runner.
