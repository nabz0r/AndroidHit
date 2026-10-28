# First-install tutorial — rc2 candidate H

PASS: **91 native assertions and 17 original PNGs**, from a genuinely new guest installation of `1.10.0-sandbox-rc2` / code 11.

| Identity | Verified value |
| --- | --- |
| AVD / serial | `Luma-Release110-H` / `emulator-5596` |
| Runtime | Official API 36 ARM64 image, actual 16,384-byte pages |
| Installed Lab APK SHA-256 | `ad6acd88571998ea5f83ad698e709a817844998be468fe821a203fd325083a78` |
| Test APK SHA-256 | `28a7cb0215ffe88c064e303daf15bd3851ea843f015e1d21e2a2ef8104992c23` |
| Runner source SHA-256 | `aab114267347e497be84d63bde0848e9137432cb98405aed5d9ff64877198f08` |

H was created as a new AVD using the installed official `system-images;android-36;google_apis_ps16k;arm64-v8a` image and the Pixel 6 definition (1080×2400, density 420). It performed a cold boot without a saved snapshot. Lab and production packages were both genuinely absent before installation. The installed Lab package bytes match the APK hash above.

The first-arrival flow automatically offered optional practice. The native sequence completed all seven lessons and exercised an incorrect-direction drag, skip/resume, real rotations with Activity recreation, the 15-point gate-coach gesture, double gate confirmation and a genuine campaign drag. Portrait return was checked against the actual rotation, 1080×2400 screenshot dimensions and game-view bounds. The welcome gift added exactly 100 LumaCoins and two Sparks once; replay and repeated reward actions added no duplicate. Practice spent neither earned coins nor actual powers. There were no new native crash entries.

The runner used real accessibility targets and native touchscreen actions. Preference assertions were read-only; no profile values were seeded, edited or injected. No account, ad completion or payment provider was exercised. This result qualifies the rc2 Lab first-install/tutorial flow on this runtime. It is not the separate full-menu/palette or 100-level run, production-package qualification, a physical-device test or a retention measurement.

Rotation settings were restored. Production remained absent, so both production hashes are correctly `null`. The newly played guest profile was retained. After collecting the report, only H was stopped through the normal emulator shutdown command; it acknowledged the shutdown and the emulator process exited with code 0. Its userdata and QCOW2 files remain in the AVD directory. No other device was accessed during the H run.

F and G are earlier candidate evidence. Their original reports and images are retained unchanged and are not relabeled as rc2 results. In particular, passing accessibility actions in those earlier runs does not prove the later selected-color rendering fix; that correction has a separate visual/native validation scope.

Machine-readable evidence: [host-report.json](host-report.json), [report.json](report.json), [actions.json](actions.json). Original screenshots: `docs/screenshots/v1.10/ui-sdk-fresh-guest-h/`.

Command used after the rc2 app and test APKs were compiled:

```sh
python3 scripts/v110-ui-test/run-fresh-tutorial.py \
  --serial emulator-5596 \
  --test-apk app/build/outputs/apk/androidTest/lab/release/app-lab-release-androidTest.apk \
  --expected-apk app/build/outputs/apk/lab/release/app-lab-release.apk \
  --label fresh-guest-h
```

The fresh wrapper refuses an existing Lab installation or evidence directory. H must not be cleared and presented as another independent first installation.
