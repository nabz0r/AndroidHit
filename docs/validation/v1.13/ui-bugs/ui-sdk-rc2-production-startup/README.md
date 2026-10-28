# RC2 production native startup/audio

Frozen 1.13.0-rc2/code14 passes **7 native checks** with **1 original full frame** on owned emulator-5586 / Luma-Release111-K. The actual target exposes its accessible native window, owns a music track with advancing mixed frames, pauses it on background and resumes it on foreground. This is digital runtime audio evidence, not physical speaker audition.

Original preference file bytes and rotation are restored and isolated hometest is removed. [Host report](host-report.json), [native checks](prepare/report.json), [AudioFlinger observations](prepare/audio-runtime.json), [exact pre-run source](source-snapshot.json). No account, advertising or payment control is invoked.
