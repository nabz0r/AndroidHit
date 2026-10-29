# Native digital audio observer — 1.17

This observer is prepared for the retained task-owned `Luma-Refonte115-QA` AVD, exclusively `emulator-5586`. It must not run until `asset_pipeline117` explicitly releases that device and the parent supplies the frozen final APK/hash. The acknowledgment flag is a guard, not permission. The wrapper never starts, clears, seeds, resizes or replaces the target app or emulator.

The emulator has no audio output to the host. This workflow records Android's digital playback state; it cannot establish physical audibility, musical taste, timbre, speaker balance or subjective loop quality. Original app PCM waveform tests remain separate evidence. No external competing audio-focus application is introduced.

The separate framework-only instrumentation uses observed accessibility bounds and actual touchscreen events. It imports no target application classes and uses no application hooks, reflection, preference reads/writes, engine calls or state injection. It launches no account, payment or provider flow and makes no purchase or puzzle move. Its only file writes are new observer evidence. It uses existing public `LumaAudio` PID logs and `dumpsys media.audio_flinger` rows; raw output is retained.

The route observes four actual places (Adventure, Home, Collection, Atelier). For each it requires the latest native loaded-scene log to match the visible destination and the same long static AudioFlinger track's frame counter to advance. It then verifies Android HOME leaves all target tracks inactive and foreground resume restores frame advancement. It independently checks effects-on/music-off, both-off and effects-off/music-on using repeated harmless taps on the already selected English language object. Concurrent AudioFlinger samples distinguish long music buffers (at least 220,500 mono frames) from short effect buffers and preserve every raw row. The original language and both audio settings are restored through actual UI and asserted before exit. It does not claim native puzzle audio coverage; historical puzzle PCM is covered by the separate host contracts.

Build only after the Gradle owner has released the build slot:

```sh
./gradlew -I scripts/v117-audio-test/init.gradle :app:assembleProductionReleaseAndroidTest
```

The separately signed observer is copied to `app/build/qualification-artifacts/v1.17/audio-v117-production-test.apk`. Do not combine this init script with another observer's init script in one build. Build/SDK compilation alone is not native runtime evidence.

After explicit device handoff and exact final candidate authorization:

```sh
python3 scripts/v117-audio-test/run.py \
  --serial emulator-5586 --expected-avd Luma-Refonte115-QA \
  --expected-apk /absolute/path/to/frozen-final.apk --sha256 EXACT_FINAL_SHA256 \
  --test-apk app/build/qualification-artifacts/v1.17/audio-v117-production-test.apk \
  --label unique-final-audio \
  --device-handoff-confirmed --host-audio-unavailable
```

The installed production bytes must already match the supplied hash and version/code. New evidence goes under `docs/validation/v1.17/audio/native/<label>/` and screenshots under `docs/screenshots/v1.17/audio/<label>/`. Each run freezes its observer APK and complete source. Failed runs are preserved. The observer is removed, the target is stopped and the final installed APK hash is checked again. No app-state preservation beyond the settings restored visibly is inferred from this no-preference-access observer.

Activity launch is asynchronous and waits at most 6.5 seconds for the visible target package. Before changing either audio setting, the observer writes `initial-state.json` from the selected language and On/Off objects. A documented failed observer run may require UI-only baseline recovery with all three explicit options: `--recover-language Français --recover-music on --recover-effects on`. These values must come from the preserved prior checkpoint, screenshots or recorded original settings; they are not guessed defaults. The wrapper records the recovery request and the observer performs it through visible UI before starting the new observations.

The exact F run (1.17.0-rc4/code22, APK `fe556ae6…e8d5a8`) passed 38 native checks with 8 captures and 130 raw AudioFlinger dumps. [Final summary](../../docs/validation/v1.17/review/audio-native-final-f.json) and [preserved first-timeout triage](../../docs/validation/v1.17/review/audio-observer-timeout-triage.json) separate the completed route from its failed first attempt. The timeout recovery changed only this observer; the target APK remained unchanged. The AVD has since been handed back to its replay owner; this historical result does not authorize another device run.
