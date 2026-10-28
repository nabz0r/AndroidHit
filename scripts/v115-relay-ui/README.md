# Native U relay observer — 1.15

This observer performs real Android touchscreen input against the installed signed release. It has its own test package and source set; production source is untouched.

The frozen fixtures cover U1–U1000. `RelayUiFixture.java` mechanically replays the catalogue on the host and exports row/column/action identities, gate effects and one blocked target. Android imports no engine, solver, renderer or private target field. It discovers actual accessible piece bounds at each board, touches those observed positions, then reads the persisted session to verify each exact action. `Next` is touched after each real victory. No progression, coins, history or session are injected.

The pilot defaults to U1–U100, with first-win provenance required for U1. It also verifies five free blocked attempts on selected levels, the inert U13 gate and two genuine refunded U13 Undo operations. Initial/victory screenshots are retained for the first30, boundaries and every50. A successful pilot leaves U101 genuinely unlocked and opened, allowing `--from 101 --through 1000` in a subsequent batch. The QA profile is retained.

The only authorized device is the newly owned `Luma-Refonte115-QA` AVD. The wrapper requires an explicit serial, exact candidate file/hash, exact version/code and signed observer APK. It refuses a differing installed candidate or observer and does not clear profiles or uninstall packages. Do not run until the candidate owner freezes the APK and releases the AVD for this route.

Fixtures have already been frozen. Explicit regeneration requires preserving/removing the old inventory deliberately, then:

```sh
python3 scripts/v115-relay-ui/generate-fixtures.py
```

Build the separate observer after production is frozen:

```sh
./gradlew -I scripts/v115-relay-ui/init.gradle :app:assembleProductionReleaseAndroidTest
```

It copies to `app/build/qualification-artifacts/v1.15/relay-production-test.apk`. The release variant's signing configuration applies. No install occurs during this build.

Example pilot, substituting the owner's exact APK, SHA-256 and version:

```sh
python3 scripts/v115-relay-ui/run.py \
  --serial emulator-5586 \
  --expected-apk /absolute/path/to/frozen-candidate.apk \
  --sha256 EXACT_SHA256 \
  --test-apk app/build/qualification-artifacts/v1.15/relay-production-test.apk \
  --expected-version EXACT_VERSION --expected-code 17 \
  --label pilot100 --from 1 --through 100
```

`--install-if-absent` permits installing the explicitly hashed candidate only when absent. The wrapper installs its separately signed observer when absent. Evidence goes to a new `docs/validation/v1.15/relay-native-<label>/`: original PNGs, accessibility snapshots, JSONL touch journal, incremental per-level progress, installed-byte check, observer result and host hashes. Failed or interrupted scopes are retained as such. The timing report describes emulator/SDK execution with checks and captures; it does not measure human difficulty or enjoyment.

The completed E pilot used the separately archived original observer under `docs/validation/v1.15/relay-native-observer-e/`. The continuation observer under `relay-native-observer-fast/` removes duplicate accessibility reads: the single-tap persisted history still proves direct commitment, and all four gate labels are still checked after each relay. Both source/APK versions are retained independently; the candidate game and fixtures are identical. Their different routes and concurrent QA activity on the development host prevent treating their timings as a controlled app-performance comparison.

After both required native batches pass, combine them with:

```sh
python3 scripts/v115-relay-ui/summarize.py
```

The aggregator requires each U1–U1000 exactly once, continuous gesture journals, exact fixture histories, genuine first three-star wins and their actual rewards of 55 coins. A separate fresh-device bootstrap or showcase is not included in this continuous-route coverage.
