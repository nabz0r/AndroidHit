# Original biome music — 1.7

The journey has ten original procedural arrangements, selected by `AudioEngine.setBiome(0..9)`. The map controller selects `min(9, (level - 1) / 100)` when its active world changes; gameplay can retain that level's world. The controller owns the world choice. This module does not poll the map or generate music during a render frame.

| Biome | Journey range | World | BPM | Arrangement |
|---:|---|---|---:|---|
| 0 | 1–100 | First Light | 75 | Original eight-bar C-major theme, pads and bells; historical PCM unchanged |
| 1 | 101–200 | Mint Meadows | 84 | D Lydian, five-note syncopated plucks and rising motif |
| 2 | 201–300 | Peach Coast | 68 | A Dorian, spacious three-note glass phrases |
| 3 | 301–400 | Cloud Gardens | 92 | F major, four-note wooden chimes and quicker chord answers |
| 4 | 401–500 | Crystal Lagoon | 72 | G natural minor, soft felt voice and extended pad overlaps |
| 5 | 501–600 | Star Orchard | 80 | B-flat Mixolydian, uneven four-note pluck placements |
| 6 | 601–700 | Moon Festival | 66 | E natural minor, slow glass phrases and low-register answers |
| 7 | 701–800 | Pumpkin Grove | 96 | E Lydian, playful five-note wooden phrases |
| 8 | 801–900 | Rose Aurora | 78 | B Dorian, four-note felt phrases and a separate chord sequence |
| 9 | 901–1000 | Dream Summit | 88 | D-flat major, five-note plucks and a closing rising motif |

The new themes change tempo, scale, chord progression, melodic contour, rhythm, bass answers and oscillator/envelope orchestration. They are compositions made by the local DSP source, rather than playback-rate changes to one recording. No downloaded recording, sound font, audio service, network access or new permission is used. `Synth.ambientLoop()` retains the previous 25.6-second identity; its overload `ambientLoop(int)` delegates to it for world zero. All eleven effects retain their historical PCM hashes, generated independently from the source at the recorded Git baseline in [legacy-source.txt](legacy-source.txt) and [legacy-pcm.sha256](legacy-pcm.sha256).

## Transition and lifecycle

`setBiome` validates 0–9, returns without work for an unchanged request, and coalesces changes on one low-priority worker. While a replacement is prepared, the current player fades toward silence over 180 ms. The replacement waits for that fade off the UI thread, with a bounded 280 ms fallback if the main looper cannot advance it. Only the still-current generation may publish. Its previous `AudioTrack` is paused and released before the successor starts a 900 ms entrance fade. A replaced or late candidate is always released. A short silence while synthesis finishes is possible; the implementation does not claim simultaneous crossfading.

Pause, music-off preferences, focus denial/loss, ducking and destruction remain authoritative when preparation finishes late. Returning to the already-loaded world discards an obsolete replacement and reuses the existing player. No world has a retained PCM cache: at most the current native static track and one candidate coexist. Java mixing uses one `double[]` plus one resulting `short[]` per generation. The largest PCM buffer is **1,282,910 bytes** (world 6). This establishes a source/model bound, not measured Android/native heap usage. The sound-effect cache retains its existing eleven small files.

`AudioTrack` stays in static PCM mode: the data is written before checking readiness and setting loop points, which are configured before playback. The implementation uses the platform's [documented static-data, loop and release APIs](https://developer.android.com/reference/android/media/AudioTrack).

## Validation

Run `scripts/test-audio-biomes-v17.sh`. [Automated results](automated-checks.txt) contain:

- 37 existing lifecycle/focus checks in the JVM Android model.
- 65 existing actual-PCM checks for the historical theme and effects.
- 110 additional biome lifecycle/concurrency checks, including 1,000 queued world changes coalesced into the latest request; controlled native writes completing after replacement or destruction; changing the initial world during first preparation; returning to the current track; music-off, background/focus and failed-write cases. Maximum live tracks in the model: two, settling to one or zero on release.
- 346 actual-PCM checks covering all ten deterministic, distinct arrangements; duration/tempo, DC offset, audible RMS, peak headroom, no silent second, wrapped-loop boundaries, invalid indices and historical theme/effect identity.

[Per-theme measurements and raw-PCM hashes](audio-biome-metrics.csv) are generated from the exact synthesis output. Loops last 20–29.091 seconds at 22,050 Hz mono PCM16. Measured RMS is 0.120572–0.140000 full scale; peak is at most 0.599994; boundary steps are 17–192 samples and smaller than the largest ordinary adjacent step in each composition. The default music gain remains 0.27. This does not measure perceived loudness on a user's speaker.

The [20-second preview montage](audio-biome-preview.wav) renders two seconds of each theme at the default gain, with preview-only fades. It is an offline rendering, not an Android capture. The separate [historical-theme preview](ambient-preview.wav) remains available.

These checks establish DSP output and the controlled JVM state model. They do **not** establish successful playback on an emulator or physical device, actual headphone/speaker quality, measured native allocation, transition timing under device load, or the root controller's world selection. Android integration and runtime capture must be recorded separately against the final built APK.
