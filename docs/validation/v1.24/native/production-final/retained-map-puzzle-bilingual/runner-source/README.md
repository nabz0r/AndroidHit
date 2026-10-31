# Native final production QA 1.24 rc2

Framework-only observer copied from the dated 31 October production harness. It keeps the v123 observer namespace, but strictly gates the release manifest SHA, production APK path/bytes/hash/signer, version `1.24.0-rc2` and code `33` before touching a pinned AVD. The SDK build receipt must match every current harness source.

Fresh device: `Luma124-Fresh-QA`, emulator-5636; creation receipt is immutable. Retained5602 is supported only for the root-owned upgrade scenario and is never selected by default. No clear/uninstall of the game, ownership/currency injection, target engine imports or target solver. Public authored U certificates select real native piece gestures; these scenarios are acceptance checks rather than a limited-perception player model.

`build-observer.py --out /tmp/UNIQUE` builds only the observer with Android SDK tools. `run.py` requires exact manifest, candidate and observer identities. Each label writes a new directory under `docs/validation/v1.24/native/production/`; failed runs are preserved. An explicit first installation requires package absence. Every normal phase cold-stops the target before launch, then uninstalls the observer afterward.

Programs: `steps/fresh-rank-capacity.json` completes the real introduction and U1–U4, checks rank2/Nova2 copies, starts U5 with captured capacity2, activates GUIDE twice separated by an actual manual exit (Charge3→1→2→0), then saves unfinished. `steps/cold-relaunch-en.json` independently cold-launches and checks those exact durable values before changing language through Settings and capturing EN. No expected rank or battle values are written into the target.

Evidence includes exact source snapshots, read-only preference snapshots, actual accessibility trees, unedited PNG screenshots and original instrumentation results. The observer is removed even on failure; the game and its userdata remain. Native emulator acceptance is not human comprehension, enjoyment, retention, physical-phone acceptance or commerce validation.
