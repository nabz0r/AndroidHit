# Production RC2 — first installation on K

**PASS: 7 native startup/lifecycle/audio assertions and 1 original PNG**, on the exact production APK `1.11.0-rc2` / code 12, Android 36 arm64 with 16 KiB pages.

- Game APK: `releases/luma-drift-1.11.0-rc2.apk`, SHA-256 `5a77d0c5b15dcdbb919bc3ea248ac9ac7b1b642898ee0fdd6cd8f855a1d09811`.
- Startup test APK: `v111-audio-production-test.apk`, SHA-256 `e801e396c3365a7e63ecb17903c0556fd93d7dda76bf0bb01dfe3c9cb0a191fa`.
- Device: retained `Luma-Release111-K`, emulator 5586. The AVD was restarted with its own userdata after the parent changed the QA plan; [receipt](../qa-devices-resume-production.json). J remained stopped.

[Before-install provenance](before-install-provenance.json) establishes that production and its test package were genuinely absent before their first installation. This is separate from the already-qualified Lab guest on K. The exact installed production bytes are recorded in [host evidence](host-report.json), alongside the original [native assertions](report.json), [instrumentation output](instrumentation.txt) and [original startup frame](../../../screenshots/v1.11/production-startup-k/startup-production-native.png).

The native SDK observer loads the exact production package, observes its accessible native window, then inspects its AudioFlinger tracks. Music has an active track belonging to the actual process, and that same track advances mixed digital frames. A real background transition pauses the track; foreground resume starts it again. [Audio runtime](audio-runtime.json) and the original diagnostic text files are retained. This measures digital playback and lifecycle behavior, **not physical-speaker listening or human musical preference**.

No menu, piece, account, ad or payment control is invoked by this startup case. The actual first-launch [persisted XML](first-startup-persisted-prefs.xml) shows initialized state, 180 LumaCoins / 3 Hint / 3 Vision / 2 Flow, Nova/Lumina selected and owned, a fresh active first tutorial, and no welcome receipt. No older production profile existed to restore; the new production guest is retained. This is not an existing-profile restoration claim or a simulated seeded profile.

All three Lab preference files and its exact APK were hashed before any production action. The startup [host report](host-report.json) confirms the three Lab files are byte-identical after startup. The before/after crash buffers are also identical. The Lab app was not navigated or mutated.

The [source snapshot](runner-source/) includes the exact startup/Candidate code and production-only K wrapper guard before the subsequent bounded production-40 run. The [evidence manifest](evidence-manifest.json) hashes this startup evidence independently. Startup qualification remains separate from the later 40-level, menu, recovery, equipment-pixel and cold-process results; it does not imply 100 native levels, live purchases, account qualification, real ads, Play publication or deployment.
