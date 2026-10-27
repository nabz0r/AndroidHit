# Audio model handoff repair

The initial regression run failed at “The entrance fades progressively”; the failure is retained in `audio-tests-initial-failure.txt`. A second isolated run reproduced it. The existing model's volatile PLAYING flag becomes visible before the worker schedules its first fade. Advancing the virtual clock immediately could therefore move it 450ms before that handoff, despite the old20ms wall-time sleep.

`AudioEngineTest` now awaits an actually pending fade task before advancing model time. It keeps the same progressive-volume bounds and adds a handoff assertion. No Android audio source or gain changed. The corrected run passes37 lifecycle/focus checks and65 DSP checks, recorded in `audio-tests.txt`. These are JVM/model/PCM results; Android playback and audible quality are separate evidence.
