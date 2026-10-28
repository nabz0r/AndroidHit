# Native Lab 1.14 → 1.15 migration observer

Prepared for the newly owned `Luma-Refonte115-QA` AVD at `emulator-5586`. The Lab package must initially be absent. The production package is never stopped, updated, cleared or read privately. No device command should run until the root agent freezes the candidate APKs.

The independent framework instrumentation targets `com.androidhit.lumadrift.sandbox` on both versions. It has no app/engine imports and uses no reflection. Its preference access is read-only. Actual visible native controls and touchscreen events are the only source of state changes.

Old phase: skip optional introduction, select English, tap the actual observed floor tile `(1,4)` to save the original six-by-six house, open the public N15 challenge deep link, play the first genuine piece exit from a host-replayed certificate, then save exact session/economy/ownership/house observations and remaining piece geometry. The observer neither purchases furniture nor fabricates campaign progress.

New phase: after an ordinary `adb install -r` of the frozen candidate, launch in a new process and resume the exact partial historical N15. Compare history, run id, policy, coins, ownership and unchanged room-record bytes, plus actual native rows/columns/arrows. Visit the enlarged house and return to the same partial board. Optional `--purchase-cactus` observes a real130-coin cactus purchase from the retained180 starter coins, checks ownership and exact debit, enters placement then uses visible Cancel without saving, reopens Place/Rotate/Place and independently parses the committed ten-by-ten record. All prior furniture positions and the historical partial N15 remain unchanged; final coins are50. This option requires a candidate with a visible Cancel control.

Optional `--finish-old` completes the remaining certificate through real UI controls and starts the new U journey; default execution preserves the partial N15 as the retained state.

`content_version=6` identifies N campaign sessions and `7` identifies U campaign sessions. Public challenge sessions use `mode=4`, `content_version=4` and an explicit `challenge_code`; this observer therefore compares their exact real stored values rather than misclassifying a challenge as campaign progress. N15 retains budget policy4 and the new U route uses policy5.

After source freeze, build the signed observer and generate its N15 code/certificate on the host:

```sh
bash scripts/v115-upgrade-ui/build-observer.sh
```

The observer artifact is `app/build/qualification-artifacts/v1.15/upgrade-lab-test.apk`. The wrapper verifies all three APK package identities, exact target versions and matching signing certificates on the host before any installation. No build command installs or launches anything on the emulator.

After APK/hash freeze and root authorization to execute:

```sh
python3 scripts/v115-upgrade-ui/run.py \
  --serial emulator-5586 --expected-avd Luma-Refonte115-QA \
  --old-apk releases/luma-drift-1.14.0-sandbox-rc2.apk --old-sha256 OLD_EXACT_SHA256 \
  --new-apk /absolute/path/to/frozen-1.15-lab.apk --new-sha256 NEW_EXACT_SHA256 \
  --test-apk app/build/qualification-artifacts/v1.15/upgrade-lab-test.apk --test-sha256 TEST_EXACT_SHA256 \
  --label upgrade-final-a --purchase-cactus --execute-frozen-candidates
```

Reports and untouched screenshots go to `docs/validation/v1.15/upgrade-native/<label>` and `docs/screenshots/v1.15/upgrade-native/<label>`. The separate observer is uninstalled afterward; the owned QA Lab profile and exact final app remain. Passing this scope proves the observed historical challenge and saved house survived the real APK update. It does not establish all historical sessions, paid ownership migration, physical-device storage reliability or human understanding.

## Continuing an observed old phase without resetting it

The first final-E run preserved a genuine old N15 exit and house move, then failed on a harness assumption: friend challenges are unlimited and keep `budget_used=0`. The actual `history=1` proved the exit occurred. This was an observer expectation error, not an application failure. Its original report and screenshots remain under `final-e-upgrade`.

The corrected observer can continue that exact old Lab profile with `--resume-old-run <preserved failed report directory>`. It requires the retained old APK hash and a genuinely failed old-phase report, records that report's hash, observes the old active session and four remaining native pieces, and neither clears the profile nor replays the exit. Use a new label so the initial failure remains reviewable. The observer APK hash changes when its expectation or continuation protocol changes; target APK hashes remain frozen.
