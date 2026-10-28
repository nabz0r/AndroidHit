# Production RC2 — 40 levels, real visual changes and cold process

**PASS: 935 native assertions**, separately recorded as 912 in the preparation process and 23 after a real force-stop/new process. The route plays **Journey levels 1–40**, then exercises menus, native gestures, zero-action Undo recovery, earned-item purchases/equipment, a different companion, a free style, the selected theme, and persistence of the actual equipped drawing.

Exact production APK `1.11.0-rc2` / code 12: SHA-256 `5a77d0c5b15dcdbb919bc3ea248ac9ac7b1b642898ee0fdd6cd8f855a1d09811`. Native test APK: `30a76bf57c54bdc62e32a5c4027fe8ccc3ffb687495fd82d1e4e83e58e1d5faf`. Device: `Luma-Release111-K`, emulator 5586, Android 36 arm64, 16 KiB pages. The [first production installation/startup](../../production-startup-k/README.md) is separate evidence.

## Native route

The unchanged [host report](host-report.json) records all checks, the installed bytes, the exact wrapper/runner hashes, captured files and the original guest preference backup. [Preparation](prepare/report.json) contains 120 individual Journey assertions (three per level) within its 912 assertions: a real saved clear, three visible stars and certified action allowance for each of levels 1–40. These are executed SDK actions, not an injected progress profile. This run does **not** establish 100 native levels or every possible player move.

The new guest earns the LumaCoins used by the bounded appearance route. Closing an unowned preview grants nothing; purchasing clothing leaves the worn art unchanged until explicit Equip. The four clothes, Lantern Leaf head equipment, Miko companion, Retro arcade style, and Lagoon theme are applied through real controls. Actual portrait/scene pixels are compared separately from labels and saved IDs. Google login, ads, LumaGold checkout and cosmetic capsules are never invoked. No server-wallet balance is established; an unsigned server wallet remains unknown rather than a measured zero.

[Cold checks](cold/report.json) observe process **3982 → 5250**, with no target process after force-stop and before restart. The equipped identity and exact portrait pixels survive the distinct process. The test's [final equipped original](../../../../screenshots/v1.11/ui-bugs/ui-sdk-final-rc2-production40/prepare/visual-final-equipped-original.png) was also inspected: Miko, four mixed clothes and the head/aura/charm composition are visible, with complete observed item names. This is a bounded device observation, not a human preference or recognition score.

## Independent pixel verification

There are **41 original full-frame PNGs and 19 explicitly labelled, unscaled analysis crops**. Crops are not additional screenshots of different game states. Original PNGs were not edited. [Independent Pillow verification](host-raster-verification.json) establishes that all 19 retained crops exactly match their original frame at the observed native bounds, and all 16 recalculated RGB-difference counts match the native observations.

For example, applying Miko changes 37,465 portrait pixels; the free Retro style changes 24,626; the selected theme changes 26,576 pixels in each observed blank Home/play region. Preview closure, purchase without Equip, theme-independent portrait identity and cold portrait restoration leave the expected portrait pixels unchanged. These counts use the report's maximum-RGB-channel difference above 8, rather than a human perceptual threshold.

The independent verifier rereads the retained third frame for each state. Three-frame stability/noise is an observation of the native runner; the earlier two frames are not retained and therefore are not independently reconstructed by Pillow. `measurement_verified=true` remains distinct from `product_native_passed=true`.

## Restoration and isolation

The production package was first installed as a real new guest before this run. The wrapper then backed up that actual guest, exercised the route, and restored **all three original production preference files exactly**, including their file set. This restoration is explicit test isolation, not evidence that the 40 test clears remain on the device and not a seeded solution profile. The restored guest has 180 LumaCoins / 3 Hint / 3 Vision / 2 Flow, Nova/Lumina, no campaign clears and its original active tutorial.

The separately captured [integrity receipt](integrity-after.json) verifies all three production files against `backup_manifest`; it also verifies the Lab APK, its test APK and all three Lab preference files are unchanged from before any production action. Rotation settings are restored and no new native crash entry is recorded. This does not claim restoration of every virtual-device file. Neither emulator 5580 nor the user's 5582 was modified by this run.

The first-install/startup source snapshot includes the exact production-only K guard. The [evidence manifest](evidence-manifest.json) freezes this report, the unmodified native outputs, independent measurements and original/crop files separately from the initial installation, Lab, renderer-host and source-fingerprint evidence. No additional product/test source or build was created for this reviewer’s production run.
