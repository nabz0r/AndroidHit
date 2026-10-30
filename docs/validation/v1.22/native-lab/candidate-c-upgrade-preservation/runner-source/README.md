# Native Lab122 observer - preserved profiles only

Build only this SDK observer, outside Gradle. Target sandbox version is exactly
`1.22.0-sandbox-rc1`, code30, frozen candidate C SHA-256
`882481a2da12cc0e4a36af804d211a12c942fab2bbaef88d80ed740e2a3490f8`.
Production candidate C must already be installed with SHA-256
`9b2593388de22b1f50f49bea05b9b02008d4574f5cbb9012fb0df5a64c47a44e`.

The only permitted device is `emulator-5602`, AVD `Luma119-Battle-QA`, with the
unchanged creation receipt hash pinned in the runner. **Coordinate ownership of
the AVD with the production tester; never run both observers concurrently.**

The preserved sandbox1.21 prior APK is
`9c0998683f09a1d940a97a0dc5badea98a5ab5c84ea2ba814917e30f8df5235b`,
recorded by the existing1.21 upgrade/refill/cold-recovery receipts. No first
install, target uninstall, clear, grant, direct preference write, clock change,
certificate solve, progression or purchase is accepted by this smoke runner.
The older fixture assets remain unchanged source context; no fixture is injected
or used by the launch/capture program.

The observer APK has separate read-only preference-hash instrumentation for
production and sandbox. These record persistent preference file hashes without
starting an Activity. The first upgrade audit compares the sandbox before update
and after update, before the first1.22 Activity. Every run compares production
preferences before and after Lab work. The runner requires the adjacent observer
build receipt and checks every source hash against the compiled SDK snapshot. Only the temporary observer is uninstalled.

Use unique output/evidence directories. First build the observer:

```sh
python3 scripts/v122-lab-ui-test/build-observer.py --out /tmp/luma122-lab-native-observer/observer-c
```

Once the production tester explicitly releases the AVD, run the **snapshot-only
upgrade audit first**, with exact observer hash from its build receipt. Then run
`scenarios/launch-fr-en-preserved.json` without an upgrade flag. Both commands are
printed in the handoff receipt. The FR/EN program uses actual settings taps,
captures the actual home, and checks Coins, unlock progression and test-Gold
unchanged. It allows legitimate elapsed energy refill and normal UI language/
visit metadata; it does not claim unchanged sandbox timing metadata.

Evidence is written under `docs/validation/v1.22/native-lab/<unique-label>`.
Compilation alone is not install, launch, gesture, isolation or device acceptance
proof. The scripts must not overwrite historical evidence.
