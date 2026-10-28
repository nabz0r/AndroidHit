# First-install tutorial — candidate F

PASS: 85 native assertions and 17 retained PNGs on a genuinely new guest installation, using the intermediate v1.10/code 11 Lab candidate. This run is **not the final release qualification**: subsequent visual polish requires a new first-install run against its exact final APK bytes.

- AVD: `Luma-Release110-F`, exclusively `emulator-5592`.
- Official API 36 ARM64 image, actual runtime page size 16,384 bytes. The emulator performed a cold boot because no saved snapshot was present.
- Lab APK SHA-256: `aee689d42d2266c866a7f21939540e260a1fd406273f69f885559fc87433a9a5`; the installed package bytes match.
- Test APK SHA-256: `30c6993fc1c9e894a231e6d23886ae21f168b46a6d5f228b745a680906824930`.

The Lab package was absent before installation. The test exercised the optional initial invitation, all seven interactive lessons, an incorrect native drag, skip/resume, real rotations and Activity recreation, a native 15-point gate gesture, double gate confirmation, the exactly-once 100-LumaCoin/two-Spark welcome gift, free replay and a genuine campaign drag. Practice did not debit earned coins or actual powers. There were no new native crash log entries.

The runner used real accessibility targets and native touchscreen input, with read-only preference assertions. No profile values were seeded or edited, and no Google account, advertising or payment provider was exercised. This is a fresh Lab guest check, not a production-package or player-retention result. Production was absent before and after the test, so its hash is correctly `null` rather than an invented equality.

Rotation settings were restored. The new guest profile was retained. After the run the emulator was stopped through its normal emulator shutdown command, which returned `OK: killing emulator, bye bye`; the emulator process exited successfully. Its userdata and QCOW2 files remain in the AVD folder. No other emulator was accessed by this task.

Machine-readable results are in [host-report.json](host-report.json) and [report.json](report.json); [actions.json](actions.json) records the input sequence. PNGs are retained under `docs/screenshots/v1.10/ui-sdk-fresh-guest-f/`.

Reproduction command used:

```sh
python3 scripts/v110-ui-test/run-fresh-tutorial.py \
  --serial emulator-5592 \
  --test-apk app/build/outputs/apk/androidTest/lab/release/app-lab-release-androidTest.apk \
  --expected-apk app/build/outputs/apk/lab/release/app-lab-release.apk \
  --label fresh-guest-f
```

The fresh-install wrapper intentionally refuses an existing Lab profile or an existing evidence label. Do not clear F and relabel it as another first installation.
