# First-install tutorial — final candidate G

PASS: **91 native assertions and 17 original PNGs**, on a genuinely new guest installation of the final v1.10/code 11 Lab candidate.

| Identity | Verified value |
| --- | --- |
| AVD / serial | `Luma-Release110-G` / `emulator-5594` |
| Runtime | Official API 36 ARM64 image, actual 16,384-byte pages |
| Installed Lab APK SHA-256 | `70e693abb589f409a74446cc2593a0faefe30967c73c32fc6d6bebf7bc5272f4` |
| Test APK SHA-256 | `71529b0ffff93acc75d9a3ae80f0fe51b54f13a1bb8bdf4dbef5c32713d8ab56` |
| Test runner source SHA-256 | `a74b6dc1f1e29d39731fcb1e84c0e6bb92dcfd61909641eb0c76dfe5bca1cd37` |

G was created as a new AVD from the already installed official `system-images;android-36;google_apis_ps16k;arm64-v8a` image using the Pixel 6 device definition. It booted without a saved snapshot. Both Lab and production packages were absent before installation. The installed Lab package hash matches the tested APK bytes.

The real first-arrival flow automatically offered the optional tutorial. Native input completed all seven lessons, checked a wrong-direction drag, skip/resume, rotations and Activity recreation, the 15-point gate-coach gesture, double gate confirmation and a real campaign drag. Portrait return was checked against the actual rotation, screenshot dimensions of 1080×2400 and the game view bounds. The welcome gift credited exactly 100 LumaCoins and two Sparks once; repeating the action or replaying did not award it again. Practice spent neither earned coins nor actual power charges. No new native crash entries were observed.

The harness used real accessibility targets and native touchscreen input. Preference assertions were read-only: no profile values were seeded, edited or injected. No Google account, ad completion or payment provider was exercised. This result qualifies the Lab first-install/tutorial flow on this runtime, not the production package, every gameplay level, physical devices or retention.

Rotation settings were restored. Production remained absent, so its hashes are explicitly `null`; no production-package hash equivalence is claimed. The real new guest profile was retained. After the report was collected, only G was stopped through the normal emulator shutdown command; it acknowledged the shutdown and its process exited with code 0. Its userdata files remain available. Existing F evidence remains labeled as an intermediate candidate and was not overwritten.

Machine-readable evidence is in [host-report.json](host-report.json), [report.json](report.json) and [actions.json](actions.json). Original PNGs are in `docs/screenshots/v1.10/ui-sdk-fresh-guest-g/`.

Command used after the final app and test APKs were built:

```sh
python3 scripts/v110-ui-test/run-fresh-tutorial.py \
  --serial emulator-5594 \
  --test-apk app/build/outputs/apk/androidTest/lab/release/app-lab-release-androidTest.apk \
  --expected-apk app/build/outputs/apk/lab/release/app-lab-release.apk \
  --label fresh-guest-g
```

The fresh wrapper refuses an existing Lab installation or an existing evidence label. G must not be cleared and presented as another independent first installation.
