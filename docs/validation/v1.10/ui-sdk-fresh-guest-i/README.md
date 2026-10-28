# First-install tutorial — rc3 candidate I

PASS: **91 native assertions and 17 original PNGs** on the first installation of `1.10.0-sandbox-rc3` / code 11 into a genuinely new guest profile.

| Identity | Verified value |
| --- | --- |
| AVD / serial | `Luma-Release110-I` / `emulator-5598` |
| Runtime | Official API 36 ARM64 image; actual page size 16,384 bytes |
| Installed Lab APK SHA-256 | `d1b085b4d9bbcf450037069ed15c7cea89b0e3e759b16e587cf3ca016a7c493f` |
| Test APK SHA-256 | `162ee39e551deb68ac6a7671759ce77a6c06638e731b62493258616bb70737c6` |
| Runner source SHA-256 | `89d6113c2b4cbfb2bf4bfccb02458da76320799c574b8bcdabb9b3d8ed270035` |

The AVD was newly created from the installed official `system-images;android-36;google_apis_ps16k;arm64-v8a` package using the Pixel 6 definition (1080×2400, density 420). It booted at cold start without a snapshot. Both Lab and production packages were absent before installation. The installed Lab package hash matches the exact rc3 APK above.

The SDK sequence checked the optional first-arrival invitation, all seven interactive lessons, an incorrect-direction drag, skip/resume, rotations with Activity recreation, the native 15-point gate-coach gesture, double gate confirmation and a genuine campaign drag. Portrait return was checked against actual device rotation, PNG dimensions and game-view bounds. The welcome gift added exactly 100 LumaCoins and two Sparks once; repeated actions and replay added no duplicate. Practice spent neither earned coins nor actual power charges. No new native crash entries were observed.

Input used real accessibility targets and native touchscreen gestures. Assertions read preferences without editing, seeding or injecting profile values. Accounts, advertising and payment providers were not exercised. This is the rc3 Lab first-install/tutorial qualification on this runtime; it does not replace the separate full-menu/footer/palette and 100-level checks, production-package checks, physical-device tests or player-retention measurement.

Rotation settings were restored. Production remained absent, with explicit `null` production hashes. The newly played guest profile was retained. Only I was stopped after report collection, using the normal emulator shutdown command; the emulator acknowledged it and exited with code 0. Its userdata and QCOW2 files remain available. Earlier F/G/H reports and images retain their original candidate identities and are not relabeled as rc3 evidence.

Raw evidence: [host-report.json](host-report.json), [report.json](report.json), [actions.json](actions.json). Original PNGs: `docs/screenshots/v1.10/ui-sdk-fresh-guest-i/`.

Command used after the rc3 APK and test APK were built:

```sh
python3 scripts/v110-ui-test/run-fresh-tutorial.py \
  --serial emulator-5598 \
  --test-apk app/build/outputs/apk/androidTest/lab/release/app-lab-release-androidTest.apk \
  --expected-apk app/build/outputs/apk/lab/release/app-lab-release.apk \
  --label fresh-guest-i
```

The wrapper refuses an existing Lab installation or evidence directory. Do not clear I and present it as another independent first installation.
