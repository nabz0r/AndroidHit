# 1.17 fresh first-run observer

This separate framework observer imports no target app, model, engine, renderer,
preferences or reflection. It sends actual touch input to visible Android SDK
targets and records original native captures. Only a newly created task-specific
AVD named `Luma117-Fresh-…-QA` on `emulator-5590` is accepted, with its own
creation receipt. Existing AVDs, including retained 5586, are never adopted,
cleared or used by this observer.

`build.gradle` compiles only public Android 36 observer classes and packages
frozen U1–U10 guidance JSONs. It has no target compile/package/bundle dependency.
The existing release signing configuration is used without printing credentials:

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
  ./gradlew -I scripts/v117-fresh-ui/build.gradle :app:packageV117FreshObserver
```

`create-device.py` requires an explicit never-existing task AVD name and evidence
directory. It checks that 5590/5591 are free, then creates empty userdata; it
never clones, resets or replaces an AVD. Current F preparation uses:

```sh
python3 scripts/v117-fresh-ui/create-device.py \
  --name Luma117-Fresh-F-QA --output docs/validation/v1.17/fresh-f/device
```

The wrapper requires explicit `--apk`, `--sha256`, `--expected-version`,
`--expected-code`, `--device-receipt` and `--output-root`. There are no candidate
identity defaults. Every phase verifies the actual installed bytes and observer
signer. Only onboarding installs the game, only when absent. A prelaunch install
failure may resume only with its own receipt proving no instrumentation began.
Target data is never cleared, reinstalled, seeded or written by the observer.

`qualify-all.py` accepts the same identity arguments plus `--candidate`,
`--label-prefix` and optionally the exact `--observer` APK. It plays First Light,
all seven real lessons and their once-only gift, eight U first victories, two
funded local draws (180 + 260), equipment and a distinct cold process. Each phase
uses a new label. `--resume-cold --cold-label NEW_LABEL` reuses the three passed
phases and plays only the cold route, without another win or spend. Cold reads an
observer-owned public-UI checkpoint; it does not inject a target save.

Before this route, save exact hashes of all four candidate build outputs in the
new evidence root's `candidate-bytes-before.json`. The coordinator independently
rechecks them, removes only the verified observer and stops only its receipt-owned
5590 AVD normally at the end, preserving userdata and creation/shutdown receipts.
Its single current resume pointer is `docs/validation/v1.17/fresh/active-handoff.json`.
Frozen C/D/E reports and source copies remain unchanged; F gets its own directory.
This route does not access providers, accounts, ads, payments or the retained main
profile, and does not replace the full U1–U1000 replay.
