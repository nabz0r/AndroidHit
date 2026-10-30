# Native 1.22 observer — prepared, not yet candidate acceptance

The framework-only observer sends actual touchscreen events at observed Android
accessibility bounds, saves unedited screenshots/AX snapshots and reads target
preferences. It imports no target classes, calls no target solver, writes no
target preference, injects no save and grants no progress, ownership or currency.
The copied U1–U60 certificates remain public gesture instructions with their
original hashes; the new map geometry is used only to touch the visible minimap,
then the actual native node is located through AX.

Only the existing task emulator is supported: `emulator-5602`,
`Luma119-Battle-QA`, bound to its immutable v1.19 creation receipt. No reset,
clear-data or target uninstall is permitted. The runner requires an explicit
candidate SHA-256, version and code, verifies signer and installed bytes, and
requires the exact previous installed SHA for an authorized in-place update.
Signing passwords pass through child-process environment variables; they are
never placed in command-line arguments or evidence.

**Do not install or launch a candidate until the release coordinator supplies
its exact candidate identity.** Building this separate observer does not install
or run the game. The current scenarios also require the genuinely retained
profile to have U42 unlocked; they fail rather than manufacture that progress.

## Build and run

Build with `python3 scripts/v122-ui-test/build-observer.py --out <unique-output>`.
Read that output's `build-receipt.json` for the actual observer SHA. Once the
exact game identity is supplied, use:

```text
python3 scripts/v122-ui-test/run.py
  --candidate <exact-game-apk> --sha256 <exact-game-sha>
  --version <exact-version> --code <exact-code>
  --observer <exact-observer-apk> --observer-sha256 <exact-observer-sha>
  --phase world-fr --label candidate-a-world-fr
  --steps scripts/v122-ui-test/scenarios/world-navigation-fr.json
  --upgrade-from-sha256 <exact-previous-installed-sha>
  --record-video-seconds 45
```

The text above describes one command; join its lines or use normal shell
continuations. Only the first update needs `--upgrade-from-sha256`; subsequent
phases must find the exact candidate already installed. `--snapshot-only` avoids
all Activity launch and touch actions. Output goes to a new immutable directory
under `docs/validation/v1.22/native/`; accepted and failed evidence is never
overwritten. The observer uninstalls only its own test package after the phase.

## Scenarios and evidence boundaries

- `world-navigation-fr.json` / `world-navigation-en.json`: real horizontal pan,
  changed observed node coordinates, actual two-pointer pinch, minimap framing,
  explicit follow, locked-node refusal, and short U35→U41 walking captured at
  distinct actual times. Full actual game profile and board identity/history
  stay unchanged; session energy cannot decrease. Natural regeneration is
  allowed. Map-only destination preferences may change.
- `world-play-real-level.json`: selecting U42 stays on the map, Play opens its
  briefing, briefing Play starts the actual campaign level with empty history.
  Starting that real eligible attempt can spend the normal session energy.
- `key-cause-unlock-undo-fr.json`: actual U42 lock inspection preserves game
  state; certified real exits 0,1,2 release key 1, remove its lock AX target, then
  a real free Undo restores both the key and lock. Native screenshots retain the
  observed relationship, opening and restoration without editing pixels.
- `snapshot-only.json`: read-only retained-state collection with no Activity
  launch when used with the runner's matching flag.

Generic `tap-id-at`, `drag-id` and `pinch-id` operations derive coordinates from
actual AX bounds and inject SDK touchscreen input. `map-begin-travel` avoids the
usual settle delay so the next screenshot can capture actual travel. The
observer does not infer successful animation from a final preference alone;
original recordings and original intermediate screenshots must be inspected.
Screenshots prove observed rendering, not physical-phone comfort or frame rate.

The tactical chapter, cold restoration, compact layout, reduced motion and
complete profile preservation each need separately recorded candidate phases.
Prepared scripts or a successfully compiled/signed observer are not those
acceptance results. Later changes to source/scenarios require a fresh observer
build receipt and exact hash before a new phase.
