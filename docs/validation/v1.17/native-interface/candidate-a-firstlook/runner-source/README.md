# Native art and interface observer — 1.17

This separate framework-only observer works exclusively on the retained task-owned `Luma-Refonte115-QA` AVD at `emulator-5586`. It never starts, erases or seeds an emulator. It never writes target preferences, calls target application/engine/rendering methods, uses reflection, purchases, draws, authenticates, contacts providers or touches another device. Actual UI actions use observed accessibility bounds, SDK touchscreen gestures and editable-text/IME accessibility actions.

The wrapper requires an exact APK path/hash, version/code and separately signed observer. It refuses a differing installed candidate except for an explicitly hashed upgrade base, and preserves the production profile. It removes only its own observer package. Compact runs use an actual 320 dp-wide display (`880×1440`, 440 dpi), then restore the exact prior display overrides, including after failure.

The `upgrade` route runs four distinct native processes:

1. `baseline`: read-only snapshot on the installed 1.16 package, before replacement. It requires the retained 51,510 earned coins and genuinely completed U1000. It observes legacy ownership, progression, furnishings, equipment, cosmetics, profile, flags, badges, completed summon receipts and the complete puzzle session. Seed fields are excluded. It launches no Activity and sends no input.
2. `assets`: SDK `BitmapFactory` decodes the 102 original sprite cells (24 companions, 24 garments, 42 furniture designs, 12 navigation objects) and three scene paintings from the installed APK. It records hashes, dimensions, visible alpha bounds and transparent separation. This is packaged-asset evidence, separately scoped from actual app screenshots.
3. `prepare`: after proving upgrade preservation, actual UI actions traverse the four roots through scene objects and horizontal gestures, pan the home, select/cancel the retained aquarium, edit the nickname with IME Done, change/restore the flag, select an already-earned badge and reopen the existing receipt. No purchase or draw is made. Screenshots are actual app windows. The final checkpoint is French with the original living-room layout and retained equipment.
4. `cold`: after force-stop, a distinct process verifies durable identity, ownership, equipment, receipts, wallet, progression, stock, complete session and exact home layouts, then captures the home/profile/existing reveal.

The `browse` route captures all six languages: four scene roots, profile/badges, all 24 companion catalogue objects across six pages, explicit companion/signature banners, the stable end of both 23/5-entry pools, all five full outfit previews and the existing receipt. It records visible-name coverage and verifies unchanged coins, receipts, progression, stock, session, home ownership and exact layouts. It finishes in French. The narrower `banners` route captures the same pool/outfit/reveal scope without repeating roots/catalogues. The standalone `assets` route only inspects packaged art. A standalone `baseline` run can be supplied the exact installed 1.16 APK/hash/version/code.

Build the separately signed observer after the candidate owner releases Gradle:

```sh
./gradlew -I scripts/v117-ui-test/init.gradle :app:assembleProductionReleaseAndroidTest
```

The frozen output is `app/build/qualification-artifacts/v1.17/interface-v117-production-test.apk`. Building does not install either package. The owner must authorize the exact candidate hash before the first upgrade run.

```sh
python3 scripts/v117-ui-test/run.py \
  --serial emulator-5586 --expected-avd Luma-Refonte115-QA \
  --expected-apk /absolute/path/to/frozen.apk --sha256 EXACT_SHA256 \
  --test-apk app/build/qualification-artifacts/v1.17/interface-v117-production-test.apk \
  --expected-base-sha256 EXACT_INSTALLED_1_16_SHA256 \
  --label unique-upgrade --route upgrade
```

Every run writes new evidence directories under `docs/validation/v1.17/native-interface/` and `docs/screenshots/v1.17/native-interface/`. The observer APK and source snapshot are frozen per run. Only screenshots named in that phase's fresh report are copied; stale device files are not attributed to a new phase. Failed runs stay recorded as failures. This observer does not replace independent visual review or the separate actual-touch U1–U1000 replay.
