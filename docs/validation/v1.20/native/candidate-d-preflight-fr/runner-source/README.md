# Native qualification observer — 1.20

This separate SDK-only package is `com.androidhit.lumadrift.v120test`. It targets only production `com.androidhit.lumadrift` and uses real native touchscreen input plus read-only preferences. It does not import target classes, invoke a target solver, edit preferences, grant ownership/currency/progress, alter clocks, log in, request an ad or perform a real-money payment.

The only authorized device is the existing task-owned `Luma119-Battle-QA`, `emulator-5602`, whose original creation receipt is pinned by SHA-256. The 1.19 earned profile is resumed intact; it is never cleared or replaced. `run.py` checks exact candidate and observer hashes, signer, version/code and pulled installed bytes. An owner-authorized update requires the exact prior installed APK SHA. Snapshot-only phases launch no Activity and send no input. A/B/C release candidates and failed phases are never overwritten.

U1–U30 input files remain byte-for-byte unchanged public gesture certificates from 1.19/1.18. U31–U40 contain only authored piece coordinates and exit order extracted from the same frozen `RelayLayouts.java`; the fixture manifest pins that source and every JSON hash. Both historical policy5 and new policy6 share this geometry. Scenarios assert the actual new policy separately. These files are gesture instructions, never game saves. New progress is earned by performing each real exit.

The observer adds read-only result receipt and session-energy checks, clock observations, real bounded waits, and a 50 ms post-gesture capture option for the brief third/sixth-exit reactions. Normal gestures still wait 480 ms. Screenshots are native original pixels; optional video is unedited emulator output. Neither qualifies sound, frame-time performance, physical-phone feel, comprehension or retention.

`build-observer.py --out /tmp/unique-output` builds only the observer with Android SDK tools and the existing signing identity. Every output path must be new. `set-display.py compact --label UNIQUE` temporarily uses 720×1280/density320/font1.3 only on this guarded AVD; `restore` uses the recorded original values. No Gradle game build or installation is performed by the observer builder.

All evidence goes to `docs/validation/v1.20/native/`. Each phase freezes its input program and source kit, records actual control bounds and gestures, reads six observed preference maps, and removes the observer afterward. Seed fields are consistently omitted from read-only snapshots. Original 1.19 evidence remains unchanged.
