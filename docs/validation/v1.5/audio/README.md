# Original ambient music repair — 1.5 candidate

The former `MODE_STATIC` music track was discarded while it was in `STATE_NO_STATIC_DATA`: the engine required `STATE_INITIALIZED` before its first PCM write. Android documents `STATE_NO_STATIC_DATA` as a successfully initialized static track awaiting data. The repair writes PCM first, then checks readiness and installs the loop. See [Android AudioTrack state documentation](https://developer.android.com/reference/android/media/AudioTrack#STATE_NO_STATIC_DATA).

The music is an original 25.6-second, eight-bar composition at 75 BPM, synthesized locally from pads, a low-register voice, bells and wrapped echoes. No recording, downloaded music or external audio library is used. The source remains `audio/Synth.java`.

`ambient-preview.wav` is a 16-second PCM16 mono / 22,050 Hz excerpt at the actual default music gain, including the game's 900 ms entrance fade and a preview-only ending fade. `audio-levels.txt` records the exact digital levels. At the default music gain of 0.27, RMS is −28.45 dBFS and peak is −15.94 dBFS. Move effects retain their 0.72 gain: their RMS is about −26.48 dBFS. These are PCM levels, not calibrated speaker loudness or LUFS.

Music fades in after initial loading and foreground resume, fades out when disabled, and changes level smoothly during focus ducking. Backgrounding or losing focus pauses immediately; resume reuses the same track and playback position. Release cancels gain callbacks and frees the track, effects pool and preparation worker. The existing persisted music/effects switches and the existing `LumaView` lifecycle calls need no change.

## Automated checks

Run `bash scripts/test-audio.sh docs/validation/v1.5/audio` from the repository root.

- **36 lifecycle/focus regression checks**, using a deterministic JVM Android model. Cases cover the static-track initial state, preparation/background races, denied/transient focus, duck/recovery, rapid toggles, independent effects, unavailable effects cache, and idempotent release.
- **65 DSP checks** on the actual generated samples: deterministic output, duration, peak/RMS/DC, no silent one-second gaps, effect envelopes and mix headroom. The loop boundary step is 17 samples, below the largest ordinary adjacent step of 2,710 samples.
- Both production audio sources compile against the actual Android 36 API. This checks API compatibility; it does not execute Android audio services.
- `static-load-regression.txt` reproduces the missing PCM write with the original source, then passes the identical static-load check with the repair. This is a JVM model, not a device capture.

## Runtime acceptance still needed

The root task should install the complete candidate and check `LumaAudio` log entries (`Original ambient ready`, `Ambient playing`, `Ambient paused`) together with advancing playback, the saved off/on switches, background/return and audio focus. A physical speaker/headphone audition and actual output capture remain separate from these JVM/DSP checks. No physical-device audition is claimed here.
