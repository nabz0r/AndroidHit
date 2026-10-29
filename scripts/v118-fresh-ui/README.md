# Fresh production native art qualification — 1.18

This independent SDK observer targets production `com.androidhit.lumadrift` only. Its own package is `com.androidhit.lumadrift.freshtest.v118`. It imports Android framework classes, reads preference values and sends actual touches at currently observed accessibility bounds. It does not import target classes, invoke target methods or solvers, use reflection, edit a preference, grant/unlock anything, buy, request ads, authenticate or contact a provider.

The accepted devices are the separately created `Luma118-Fresh-Art-QA` AVD (`emulator-5596`, historical E evidence), `Luma118-Final-G-QA` (`emulator-5598`, historical G evidence) and `Luma118-Final-H-QA` (`emulator-5600`, fresh H evidence). Their immutable creation receipts and empty-boot proofs live under `docs/validation/v1.18/native-fresh/device-creation/`, `native-fresh-g/device-creation/` and `native-fresh-h/device-creation/` respectively. No existing AVD was copied or modified. `prepare-device.py` and its final-G/H wrappers reject an existing name, receipt or occupied port. The runner accepts only those exact name/port/receipt-hash combinations and stores each profile's evidence separately. The game may be installed only after the parent supplies its frozen final hash. The observer runner never installs, replaces, clears or uninstalls the game.

Build independently from the game:

```sh
python3 scripts/v118-fresh-ui/build-observer.py --variant production
```

Then use `run.py` with the exact receipt hash, already installed candidate path/hash/version/code and the separately built observer. The runner verifies the live AVD, installed APK bytes and signing identity, force-stops the game, installs only its observer, executes a fresh token-bound phase, pulls screenshots/accessibility/state evidence and removes the observer. Every evidence label must be new; failures remain recorded.

Phases:

- `fresh-firstlight` requires empty game preferences before the first Activity launch and completes the three genuine introduction pieces through visible native targets. It then opens home, all four Mini LoL inventory pages and the locked Teemo studio form10 guard preview.
- `fresh-continue` continues an already genuine first-light completion after an observer failure; it cannot establish a new pristine launch.
- `six-languages` chooses all six languages through the real settings, then visits each home, each Mini LoL page body and the localized form10 guard preview. All13 names must be reachable on the actual inventory pages.
- `compact-english` repeats the same destinations after a host-recorded compact display override on this task-owned device.
- `cold` launches in a fresh native process and records its restored entry.

Every successful phase asserts exactly the documented180 starter coins, Nova as the sole owned/equipped companion, U1 as the only campaign unlock, no earned stars, guest copies, summon or expedition reward injection. Future-form and guard previews must announce that saved levels remain unchanged. Full read-only preferences and process IDs accompany screenshots. The native evidence qualifies this emulator and exact APK, not physical-phone comfort, apparel alignment, enjoyment or retention.

For H, `--costume-controls` additionally touches the actual `motion_outfit_toggle` in both directions and checks exact equality of all four preference maps after the studio preview. Language and compact phases also scroll to `wardrobe_costume_toggle`, touch original → worn → original, verify the localized labels and verify that only `companion_original_costume_v118` may change. That UI choice can legitimately add a `true` boolean that was absent at fresh launch; do not claim unfiltered preference equality across that first wardrobe choice. The first-light phase keeps the presentation choice absent and captures the calculated original-costume default before any persistent wardrobe interaction.

`verify-release.py` independently checks the frozen production/Lab hashes, release certificate, package identities, expected version names and shared code, and all 148 authored WebP sheets plus 148 metadata entries against measured source files. All 296 art files and every other shared asset must have identical bytes between variants, except the two compiled profiles `assets/dexopt/baseline.prof` and `assets/dexopt/baseline.profm`. The sole permitted extra file is production's `assets/identity-config.json`, which must match its production source. Every other missing, extra or differing asset rejects the candidate. Reports record the actual differences and distinguish this policy from equality of all packaged assets. This package proof remains distinct from native runtime proof.

Run the host-only asset-policy regression checks with `python3 scripts/v118-fresh-ui/test-asset-policy.py`; they do not build, install or launch either app.
