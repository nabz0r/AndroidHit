# Native expedition qualification — 1.18

This separate SDK-only observer imports Android framework classes and frozen existing U1–U30 certificate coordinates. It does not import game classes, invoke a target method or solver, use reflection, modify a preference, seed a profile, make an external purchase, request an ad, authenticate, or contact a provider. Every touch uses a presently observed visible native accessibility rectangle. Scrolling uses Android accessibility actions. Screenshots are original Android windows with matching accessibility observations.

The host runner accepts only the newly created task-owned `Luma118-Expedition-QA` AVD at `emulator-5594`. Its explicit creation receipt hash, exact installed candidate hash/version/code and release signature must all match. The runner never creates, starts, wipes, installs, upgrades or uninstalls the target game. It installs only its separately signed observer and removes that observer after each phase. Existing player and QA profiles remain untouched.

Build independently of the application's Gradle tasks:

```sh
python3 scripts/v118-ui-test/build-observer.py --variant lab
```

The output is `app/build/qualification-artifacts/v1.18/lab/expedition-observer.apk`. The compiler uses installed SDK36/JDK17 and the existing release signing configuration. No signing credentials appear in output or source snapshots.

Run a named phase against already owner-installed frozen bytes, supplying exact provenance:

```sh
python3 scripts/v118-ui-test/run.py \
  --serial emulator-5594 --expected-avd Luma118-Expedition-QA \
  --creation-receipt /absolute/path/to/device-creation/creation.json \
  --creation-sha256 EXACT_CREATION_SHA256 \
  --expected-apk /absolute/path/to/frozen-lab.apk --sha256 EXACT_APK_SHA256 \
  --test-apk app/build/qualification-artifacts/v1.18/lab/expedition-observer.apk \
  --expected-version EXACT_VERSION --expected-code EXACT_CODE \
  --phase migration-launch --label unique-evidence-label
```

Each run freezes the observer APK/source/fixtures and writes a new evidence directory under `docs/validation/v1.18/native-expedition/`. Current instrumentation success, the requested phase and a fresh host UUID echoed by native code are mandatory. Failed runs remain failed evidence. The target is force-stopped before each phase, and report process IDs permit actual cold-process comparison. Complete game/meta/puzzle preferences are observed before/after, with seed-named keys excluded. The isolated `luma_expedition_session_v118` is recorded independently from the original campaign session.

`snapshot` launches no Activity and sends no input. `migration-launch` performs an actual Activity launch without user input. `cold` launches the real app in a distinct native process and captures its restored entry. `baseline-play` requires a genuinely fresh1.17 profile, completes the three real first-light pieces, selects English through actual settings, opens U1 and its public Play briefing, then clears the two certified pieces. `baseline-continue` is narrowly scoped to the retained real first-light completion after the original observer missed that public briefing; it does not recreate a fresh profile. JSON scenarios describe further actual UI routes; `solve-current` reads the actual selected session's numeric board and uses only its frozen certificate at observed piece targets.

The immutable1.17 baseline is `legacy-baseline-b/native/legacy-baseline.json`: 235 current coins (180 starter balance plus 55 genuinely earned from U1), three U1 stars, unlocked U2, original Nova ownership and an inactive durable U1 session. First-light completion occurred in `legacy-baseline-a`, which then failed on its observer's unhandled public briefing; that failure remains recorded. B passes the continuation without resetting or injecting any state. `device-creation/baseline-stopped.json` records graceful shutdown and the baseline digest.

`compare-migration.py` compares every original observed preference value after a real1.18 launch. Only new `expedition_v118_`/`companion_copies_v118_` keys and the separate expedition session file may appear. A difference in any original value remains a failure.

Native evidence qualifies actual behavior on this QA emulator. It does not establish physical-phone comfort, audience understanding, enjoyment, retention or economy balance.

## Executed pre-art B functional scope

The owner-authorized frozen Lab APK is `docs/validation/v1.18/candidates/pre-art-b/luma-drift-1.18-lab-pre-art-b.apk`, SHA-256 `39ba2542bd21948e2ac14062ae3767ed5e537561bc59a538813dfad439480878`, code25. This is functional preflight evidence; later source/art changes require their own final-byte qualification.

- `pre-art-b-migration`: an actual retained install and first1.18 Activity launch preserve all58 original observed values exactly.
- `pre-art-b-expedition-start`: actual Nova reveal, U8 certificate win and fork checkpoint pass. Its accepted90° request did not change the sensorPortrait display, so `rotation-scope-rejected.json` expressly excludes those images as landscape/recreation evidence.
- `pre-art-b-expedition-finish`: original failed observer retained. The KEY fork selected U11 correctly, but the observer initially expected labels beginning with `Piece`; the actual label begins `Key1, Piece`. No piece moved in that failed phase.
- `pre-art-b-expedition-finish-selected`: the same selected U11 is genuinely resumed, then U11/U14/U25 finish the original four-board run. Two actual framework `Activity.recreate()` calls produce different Activity instances (victory and claimed receipt). Target/discovery copies, completion count,235-coin unchanged wallet and N/Level2/2of8 Nova pass. All58 original values, including the separate campaign session, remain unchanged.
- `pre-art-b-claimed-cold`: a distinct process observes exactly the preceding completed state before launch; the full snapshot digests match.

The original `home-with-souvenir.png` caught an app crossfade and is excluded as a settled destination capture by `visual-scope-reservations.json`. Screenshot capture now waits for actual presentation before attributing a frame to its destination. B also shows the known discovery portrait under the departure companion's name and mixes departure Level1 with current2of8 copies on the receipt; these are recorded visual reservations for the final candidate, while the next-run starter correctly shows Nova Level2.


## Expanded-art final candidate scenarios

The final-art observer adds real studio input across20 poses, four form previews and four persisted stances, actual Activity recreation, a separate cold-process observation, all13 Mini LoL guest names across four pages, earned-coin Ashe ownership and a four-puzzle Nova expedition targeting Ashe copies. The wallet is funded only by actual U2–U8 first wins on the retained genuine baseline, then a real520-coin purchase. Optional phases cover Miko rerouting and Orbit's third project route after actual campaign-earned funding. Each phase retains its exact candidate hash and fresh execution token. Preparation and scenario source do not themselves qualify behavior.
