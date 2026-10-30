# Native 1.23 exact-candidate observer

This separate SDK-only observer sends actual touchscreen events at observed
Android accessibility bounds. It saves original PNGs/AX snapshots and reads
preferences. It imports no game classes, calls no game solver, changes no game
preferences and grants no progress, ownership or currency. Copied public U1–U60
certificates are disclosed gesture instructions, not a player-understanding test.

Only three pinned task AVDs are accepted: retained `emulator-5602` / `Luma119-Battle-QA`
plus newly created `emulator-5630` / `Luma123-Fresh-QA` (C first-install)
and `emulator-5632` / `Luma123-FinalFresh-QA` (D first-install). The runner checks their
immutable creation-receipt hashes, exact APK/signing/version `1.23.0-rc1`/code31
and installed bytes. First install requires absence; in-place upgrade requires
the coordinator-authorized exact previous SHA. It never resets, clears or
uninstalls the game. It installs/uninstalls only its own observer package.

Build with `python3 scripts/v123-ui-test/build-observer.py --out <new-dir>`.
After the coordinator supplies a frozen game identity, run `run.py` with explicit
`--device`, `--candidate`, `--sha256`, `--version`, `--code`, `--observer`,
`--observer-sha256`, `--phase`, `--label`, and `--steps`. Use `--first-install`
only for absent targets, `--upgrade-from-sha256` only for an authorized exact
update. Unique evidence directories under `docs/validation/v1.23/native/`
preserve successful and failed phases. Host programs and compiled inputs are
snapshotted separately; changing Java/manifest/assets requires a new build.

Context tests use real Try/Cancel/Apply and return controls, then compare exact
run/history/budget/Undo/companion state and Coins. The explicit Bundle test additionally calls the public SDK `Activity.recreate()`
on the actual Activity main thread; Android invokes save/destroy/create. It never
injects a Bundle or calls a private game method. This lifecycle request is counted
separately from touchscreen gestures. Lifecycle timer checks use real
Home actions and Activity foreground intents, then compare persisted elapsed
milliseconds; no private timer API is called. `long-press-piece` derives its
position from an actually visible piece and holds for a bounded 500–1200ms.
`set-display.py` only changes display/font settings on the three pinned AVDs and
requires an exact matching receipt to restore them. Normal 1080×2400 media and
compact/font/reduced-motion evidence are separate.

A retained saved 1.22 board may still contain its old shells. New 1.23 attempts
must have none. C first-install results remain C evidence; an upgrade to D is
reported as an upgrade, never relabeled as D first-install evidence. A failed
script target is preserved with its trace and a separately named corrected run.

Screenshots and scripted native gestures do not establish human comprehension,
enjoyment, retention, physical-device performance or store acceptance.
