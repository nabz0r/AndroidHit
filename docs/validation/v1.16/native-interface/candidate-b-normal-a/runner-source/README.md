# Native1.16 upgrade and visual qualification

This separate Android SDK observer uses only the two retained, task-owned AVDs: `Luma-Refonte115-QA` on `emulator-5586` and `Luma-Refonte115-Showcase-QA` on `emulator-5588`. It refuses other devices, names, package versions and APK bytes. Neither this runner nor the observer starts, erases or seeds an emulator. The runner preserves the production profile, removes only its separate observer package and restores any display override.

The observer reads preferences to verify outcomes. All changes come from actual touches on observed accessibility bounds, a real gesture inside the observed home viewport, or framework `ACTION_SET_TEXT` on the visible native nickname editor. It calls no app, renderer or engine methods and uses no private reflection, preference writes, provider checkout or progression injection. Local summon seeds are excluded from exported state.

The `baseline` phase launches no Activity and sends no input. It reads legacy ownership, earned coins, progression, home, equipment, cosmetic and power receipt values and the complete saved puzzle session. All seed fields are excluded. The `upgrade` route runs this observer against the actual installed base before replacing it, then compares every observed value immediately after launching the new candidate. It works with the1.15 version/code17 package as well as a repeated candidate run. A standalone `--route baseline` can record only this snapshot by supplying that installed APK's exact hash, version and code.

The rest of the `upgrade` route requires the retained U1000 record and furnished living room. It checks the enlarged home, whole-room/detail controls, pan, zoom, native aquarium selection and preserved zoom across rooms. It edits the local nickname to `Luma QA 116`, chooses `LU`, equips three earned badges and refuses a locked collector badge when eligible. It opens both exact-odds catalogues, performs one actual companion draw for180 earned LumaCoins and one signature-outfit draw for260, verifies each durable result and any documented duplicate return, equips both results and reopens the same reveals without another charge. A separate force-stopped process verifies persistence. Exact house layouts, all progression, powers and the complete puzzle session must stay unchanged.

The `browse` route spends nothing. It captures adventure, home, collection, atelier, profile, badges, both banners, both odds catalogues, a full outfit preview and the last completed reveal in all six languages. Every browsing screenshot waits2850ms for transient notices to settle. It can run at the real320dp-wide compact display with `--compact`; the prior display size and density are restored even after failure.

Build the isolated observer after the candidate itself builds:

```sh
./gradlew -I scripts/v116-ui-test/init.gradle :app:assembleProductionReleaseAndroidTest
```

The copied observer artifact is `app/build/qualification-artifacts/v1.16/interface-v116-production-test.apk`.

Run with an explicit frozen APK/hash, observer APK, serial and a new evidence label:

```sh
python3 scripts/v116-ui-test/run.py \
  --serial emulator-5586 --expected-avd Luma-Refonte115-QA \
  --expected-apk /absolute/path/to/frozen.apk --sha256 EXACT_SHA256 \
  --test-apk /absolute/path/to/interface-v116-production-test.apk \
  --label candidate-a-upgrade --route upgrade
```

If upgrading the known installed1.15 APK, also provide `--expected-base-sha256` with its exact known hash. This authorizes an in-place install only when those precise bytes are present; a mismatching installed app aborts the run. An absent app requires the separate explicit `--install-if-absent` option.

Each attempt retains observer bytes, runner source hashes, native screenshots, accessibility snapshots, checks, actions and its host report under `docs/validation/v1.16/native-interface/<label>` and `docs/screenshots/v1.16/native-interface/<label>`. A failed attempt remains evidence and should receive a new label after repair.
