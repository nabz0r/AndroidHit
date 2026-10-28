# Final RC2 production startup/audio

**PASS: 7 checks, 1 original full PNG frame.** The actual process exposes its accessible app window. AudioFlinger shows the same process-owned active music track advancing **21,991 mixed frames**, pausing after the actual background lifecycle and resuming in the foreground. This is a digital runtime observation, not a physical-speaker audition. [Audio record](prepare/audio-runtime.json).

The runner does not activate any piece, menu, Google, ad, wallet or payment control. The startup instrumentation APK is distinct from the shop instrumentation: SHA-256 `b3aca0f663085f398597d92cde446f74bbc510da7b8a324a158b5bd71cca731a`, [measured on the device during this run](../test-builds/installed-production-startup-test-apk.json).


Exact installed production `1.12.0-rc2` / code 13 / SHA-256 `de581253b64232ae45bcbd647862dd470d522068f26516f6417bf515e882403d`, on explicitly owned `hexkeep-play-36-16k` / emulator 5580, Android 36 / ARM64 / actual 16 KiB pages. This is an upgrade of an existing 1.10 guest, not a fresh-install claim. [Upgrade receipt](../install-rc2-production-preserved-upgrade/report.json).

[Host report](host-report.json), [native report](prepare/report.json), [exact evidence inventory](evidence-manifest.json), [final cross-integrity measurement](../integrity-after-production.json). The host restores the original complete preference file set byte for byte and records no new native crash. The final integrity check also proves the Lab APK, test APK and all three Lab preference files are unchanged by production work, and rotation still matches the original Lab snapshot. Emulator 5582 is excluded.
