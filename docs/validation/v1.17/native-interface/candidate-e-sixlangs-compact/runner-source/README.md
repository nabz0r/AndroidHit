# Native art and interface observer — 1.17

This separate framework-only observer works exclusively on the retained task-owned `Luma-Refonte115-QA` AVD at `emulator-5586`. It never starts, erases or seeds an emulator. It never writes target preferences, calls target application/engine/rendering methods, uses reflection, purchases, draws, authenticates, contacts providers or touches another device. Actual UI actions use observed accessibility bounds, SDK touchscreen gestures and editable-text/IME accessibility actions.

The current delivery guard targets **1.17.0-rc3 / code21**. The wrapper requires an exact APK path/hash, version/code and separately signed observer. It refuses a differing installed candidate except for an explicitly hashed upgrade base, and preserves the production profile. It removes only its own observer package. Compact runs use an actual 320 dp-wide display (`880×1440`, 440 dpi), then restore the exact prior display overrides, including after failure. Earlier rc1/code19 candidate evidence stays immutable in its original directories.

The `upgrade` route runs four distinct native processes:

1. `baseline`: read-only snapshot on the exact explicitly hashed installed base, before replacement. The first qualification witnesses 1.16→1.17; subsequent reviewed candidates preserve that same profile. It requires the retained 51,510 earned coins and genuinely completed U1000. It observes legacy ownership, progression, furnishings, equipment, cosmetics, profile, flags, badges, completed summon receipts and the complete puzzle session. Seed fields are excluded. It launches no Activity and sends no input.
2. `assets`: SDK `BitmapFactory` decodes the 102 original sprite cells (24 companions, 24 garments, 42 furniture designs, 12 navigation objects) and six scene/material paintings from the installed APK. It records hashes, dimensions, visible alpha bounds and transparent separation. This is packaged-asset evidence, separately scoped from actual app screenshots.
3. `prepare`: after proving upgrade preservation, actual UI actions traverse the four roots through scene objects and horizontal gestures, pan the home, select/cancel the retained aquarium, edit the nickname with IME Done, change/restore the flag, select an already-earned badge and reopen the existing receipt. No purchase or draw is made. Screenshots are actual app windows. The final checkpoint is French with the original living-room layout and retained equipment.
4. `cold`: after force-stop, a distinct process verifies durable identity, ownership, equipment, receipts, wallet, progression, stock, complete session and exact home layouts, then captures the home/profile/existing reveal.

The `browse` route captures all six languages: four scene roots, profile/badges, all 24 companion catalogue objects across six pages, explicit companion/signature banners, the stable end of both 23/5-entry pools, all five full outfit previews and the existing receipt. It records visible-name coverage and verifies unchanged coins, receipts, progression, stock, session, home ownership and exact layouts. It finishes in French. The narrower `banners` route captures the same pool/outfit/reveal scope without repeating roots/catalogues. The standalone `assets` route only inspects packaged art. A standalone `baseline` run can be supplied the exact installed 1.16 APK/hash/version/code.

Build the separately signed observer after the candidate owner releases Gradle:

```sh
python3 scripts/v117-ui-test/prepare-copy.py
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

The `rig` route equips only the already-owned Nova, Miko and Halo through actual inventory controls, captures each wardrobe/collection/profile and all five outfit previews per actor (15 previews), then restores Halo, the original four garment slots and French. It asserts no coin, receipt, stock, progression, puzzle-session or layout change.

The `performance` route warms the actual enlarged living room, observes target-PID sprite decode logs twice while Android's rendered-frame total increases, and records Android `gfxinfo` and `meminfo` without invoking target cache or renderer methods. The bound assertion covers sprite residency recorded by real decode logs; total process memory also includes the observer. Emulator measurements are not a physical-device performance guarantee.

The `secondary` route uses a frozen, source-hashed label fixture for actual accessibility/touch navigation in all six languages. It scrolls the retained settings, missions, local journal, rules, tutorial replay, legal text, map/story/guardian menu, three event tabs, four reward tabs, inventory categories, all four wardrobe slots, three gear slots, twelve-style studio, three rooms, home catalogue categories, shop categories and guest account/social/vault/commerce Activities. It never claims, purchases, draws, signs in, requests an ad or sends social actions. Ordinary tutorial replay/leave updates are recorded separately. An actual reduced-motion dial toggle is verified with two identical native room-frame hashes, then its original value is restored. French/living room is restored at delivery.

The `language-regression --compact` route reproduces the real rc1 320dp failure: a partially offscreen Italian target had been shifted upward over visible German. It checks the observed targets do not overlap, touches German and verifies the saved language, then scrolls to Italian and verifies that distinct action. It restores French without altering the economy or puzzle session.

The `transitions` route records an unedited eight-second Android SDK video of actual Adventure→Home→Collection→Home object navigation with normal motion, writes its hash and native uptime chronology, then restores the original motion preference and French. The video is technical transition evidence; public screenshots remain the separately captured original app windows.
