# Native companion-team observer — 1.19

This separate framework-only instrumentation package is `com.androidhit.lumadrift.v119test`. Its only target is production `com.androidhit.lumadrift`. It sends actual touchscreen gestures to currently observed accessibility bounds and reads saved preferences. It does not import target code, use reflection, call a target solver, edit preferences, grant ownership/progress/currency, authenticate, request ads or perform real-money purchases.

## Isolated device and builds

`prepare-device.py` created the new empty `Luma119-Battle-QA` AVD at `emulator-5602`, Android 36 ARM64 with 16 KiB pages. It refuses an existing name, evidence directory or occupied port. No existing profile or userdata was copied or modified. Immutable creation and empty-boot receipts are under `docs/validation/v1.19/native/device-creation/`. Audio output is disabled for this UI qualification and does not qualify sound quality.

`build-observer.py` uses Android SDK `javac`/`d8`/`aapt2`/`zipalign` and the existing project signing identity to build only the observer. It does not invoke Gradle or share its outputs with the game's build. Every observer output directory must be new. The SDK runner's framework APIs are deprecated in places but remain available in the qualified image.

```sh
python3 scripts/v119-ui-test/build-observer.py --out /tmp/luma119-native-observer/observer-new-label
```

The game must not be installed until the parent provides its frozen candidate path, SHA-256, version and code. `run.py` requires explicit game and observer hashes, checks both signatures, the live AVD and its exact creation receipt, then checks the pulled installed APK against the frozen hash. `--first-install` is accepted only while the game is absent. The separately authorized `--upgrade-from-sha256 PREVIOUS_HASH` checks the exact prior installed bytes before updating this task device with `install -r`, preserving its legitimately earned profile. It never clears or uninstalls the game. The observer is installed and removed for each phase. `--fresh` asserts empty game and board preference maps before the first Activity launch.

```sh
python3 scripts/v119-ui-test/run.py \
  --candidate /absolute/frozen-candidate.apk --sha256 EXPECTED_GAME_SHA256 \
  --observer /absolute/battle-observer.apk --observer-sha256 EXPECTED_OBSERVER_SHA256 \
  --version EXPECTED_VERSION --code EXPECTED_CODE \
  --phase fresh-guest --label candidate-a-fresh-guest \
  --steps scripts/v119-ui-test/scenarios/fresh-guest.json --first-install --fresh
```

## Genuine UI program

The JSON step list is copied into evidence and sent as a fresh token-bound instrumentation argument. It has at most 250 explicit steps. Core operations include `firstlight`, `language`, `tap-id`, `tap`, `open-level`, `piece`, `solve`, `earn-through`, `draw-avatars`, `choose-owned-team`, `assert-owned-team`, `assert-battle`, `remember-battle`, `assert-battle-unchanged`, `capture`, `snapshot`, `back` and `adventure`.

`assets/fixtures/` contains unchanged public U1–U30 gesture certificates copied from the frozen 1.18 harness, with exact SHA-256 provenance. These are coordinates for actual piece gestures, **not save fixtures**. The solver is not imported or invoked. The observer requires a performed history to be a valid prefix before continuing its public gesture script. Funding comes from real first campaign victories, each checked against its actual 55-coin reward. A draw uses the real earned-coin UI, reads its committed receipt and checks exact debit plus duplicate compensation. Outcomes remain random; the observer never edits a seed or forces a character.

`choose-owned-team` refuses IDs absent from the observed owned set and manipulates only reachable `squad_slot_N`, `squad_member_ID` and `squad_save` controls. `buy-earned-avatar` pays an actual displayed LumaCoin price and checks the exact debit and ownership transition. Talent and combo checks read the actual `battle_v119` session record after real commands. `snapshot-only` reads the four observed preference maps without launching an Activity or sending input; it supports before/after update equality checks. A cold phase uses a distinct process because the host force-stops the target before every instrumentation run; prior process IDs and encoded session/team values must be compared as separate evidence.

`set-display.py compact --label NEW_LABEL` changes only this task AVD to 720×1280, density 320 and font scale 1.3, retaining its exact original settings in a new receipt. `restore --label SAME_LABEL` restores those values. Compact scenarios touch the actual centres of navigation targets, including the formerly overlapping All levels and Rewards controls. Reduced motion is changed through the game's real Settings UI and checked read-only. It is never enabled by a preference write.

`capture-fast` captures the live frame immediately after a normal gesture delay so short talent effects can be inspected. `--record-video-seconds 45` optionally records an unedited 45-second native UI video at 720×1600 during a phase. It does not measure frame rate, sound or physical-phone performance. Normal original screenshots remain 1080×2400; compact screenshots retain their actual 720×1280 size.

Each evidence label must be new; failed attempts remain preserved. Reports include exact bytes/signers, device properties, original PNGs, accessibility bounds and labels, actual touch log, read-only preference snapshots and source hashes. Model correctness, native execution, visual review and phone comfort are separate acceptance claims.
