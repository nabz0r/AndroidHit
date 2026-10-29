# Native U1–U1000 replay — 1.17

This is a separate framework-only Android SDK observer for an explicitly identified final signed production 1.17 APK. Version, code and SHA-256 are required arguments; none silently defaults to a superseded candidate. It imports no game model, solver, renderer or private UI field. Target preferences are read only. No save, unlock, reward, history, wardrobe, currency or session is injected.

The retained task-owned AVD already completed U1–U1000 with three stars during the 1.15 qualification. This route **replays** those levels and requires **zero new first-win coins**. It cannot be described as a new-player progression or fresh rewards run. All 1,000 actual action histories, initial gate masks, relay effects, stored three-star results and untouched power stock are checked. Blocked-touch probes and two genuine U13 Undo operations remain part of the route.

The game is played with actual touchscreen events over observed accessible tile bounds, followed by its real Next object. The initial map jump uses the visible numeric field and keyboard Done. English is selected through the visible settings object for stable piece labels, then the original language is restored through the same UI. The final saved puzzle session naturally becomes the replayed session. Ownership, currencies, avatar/clothes, local profile, summon receipts, home layout and stock must survive unchanged.

The only authorized device is **`Luma-Refonte115-QA` / `emulator-5586`**. Do not launch the observer until the release owner has supplied the final candidate SHA-256, frozen visual/audio fixes, and the general UI observer has explicitly released this AVD. The wrapper never starts an emulator, installs/upgrades the game, changes its display, clears data or touches another device. Its separately named observer can be installed and removed after its exact signature/hash is checked.

The original 1.15 fixtures remain under `scripts/v115-relay-ui/assets/fixtures/`. Their 1,000 JSON files and original source manifest are reused byte for byte. Neither released evidence nor fixtures are rewritten. Host validation is available without building or accessing Android:

```sh
python3 scripts/v117-relay-ui/run.py --verify-fixtures-only
```

After the release owner announces the final build, build the separate observer. This standalone task compiles only framework test classes and packages the frozen certificates; it has no target compile/package/bundle dependencies:

```sh
./gradlew -I scripts/v117-relay-ui/build.gradle :app:packageV117RelayObserver
```

It is copied to `app/build/qualification-artifacts/v1.17/relay-production-test.apk`. Candidate and observer must have the existing release certificate SHA-256 `dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287`. The wrapper verifies every packaged fixture and both signing identities before device input.

After the explicit AVD handoff:

```sh
python3 scripts/v117-relay-ui/run.py \
  --serial emulator-5586 \
  --expected-apk /absolute/path/to/frozen-final-1.17.apk \
  --sha256 FINAL_CANDIDATE_SHA256 \
  --test-apk app/build/qualification-artifacts/v1.17/relay-production-test.apk \
  --expected-version 1.17.0-rc5 --expected-code 23 \
  --label final-replay1000 --from 1 --through 1000
```

Evidence is written into a new `docs/validation/v1.17/relay/<label>/`. Each completed level flushes progress and the continuous gesture/check journals. Original screenshots cover the first 30 levels, 50-level boundaries, route boundaries and selected gate/Undo probes. All 1,000 boards have observed piece target geometry and real saved action history; not every animation frame is captured. Failed/interrupted attempts remain failed evidence and are never overwritten.

The observer keeps a preview of its first 100 checks in the final JSON and streams the complete journal to `checks.jsonl`, avoiding a large in-memory list during a long art-heavy run. The APK bytes are pulled and hashed before and after qualification. A report cannot pass if the candidate changes, any level is absent, a replay reward is duplicated or protected state differs.

Only after one or more complete, passing, disjoint batches cover every level:

```sh
python3 scripts/v117-relay-ui/summarize.py \
  --report docs/validation/v1.17/relay/final-replay1000/host-report.json
```

The aggregator verifies evidence hashes, every check and touchscreen journal entry, exact certificate histories, all 1,000 retained results and zero duplicate rewards. Its timings describe emulator execution including SDK observation and PNG capture; they do not measure human enjoyment, audio quality or real-phone performance.

For the authorized long run, detach it only after the explicit main-device handoff:

```sh
python3 scripts/v117-relay-ui/launch.py \
  --candidate G --label candidate-g-replay1000-a \
  --apk /absolute/path/to/frozen-candidate-g.apk \
  --sha256 EXACT_AUDITED_G_SHA256 \
  --expected-version 1.17.0-rc5 --expected-code 23 \
  --observer /absolute/path/to/frozen-relay-observer.apk
```

The launcher writes the detached PID, exact command, console, remote progress
and expected report to `docs/validation/v1.17/relay/active-handoff.json`.
`summarize.py` infers one exact version/code from all
contributing reports and rejects any mixture. Preserved E reports/snapshots keep
their original arguments and are never relabelled F.

After the detached launch, the generic evidence watcher takes its exact candidate
identity from the immutable launch record, never from an older default:

```sh
python3 scripts/v117-relay-ui/watch.py \
  --launch-record docs/validation/v1.17/relay/candidate-g-replay1000-a-launch.json \
  --source-freeze docs/validation/v1.17/candidate-g/source-freeze.json \
  --expected-restored-language fr --stop-after-pass
```

Run this watcher detached when a long replay must survive agent interruption.
`--stop-after-pass` is permitted only with the release owner's explicit normal
stop authorization. Before any stop it verifies the actual full1000 report,
independent complete aggregate, all evidence hashes, original language restore,
byte-identical protected state/settings, all frozen app inputs, installed APK,
owned AVD and absence of its removed observer. It never accesses the separate
UI clone. Failed/interrupted attempts retain their own labels and must not
contribute to an accepted full1000 summary. F's817-level interrupted report is
preserved separately and is never relabelled G.
