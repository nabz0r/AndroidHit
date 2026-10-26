# Art direction and original audio

Luma Drift uses an original, code-drawn visual system: a dark celestial backdrop, soft colored light, tactile rounded pieces, clear arrow marks, and small expressive companions. The interface takes general puzzle-game conventions—short sessions, visible progression, immediate feedback, and readable rewards—without copying another game's artwork, characters, screens, or sound recordings.

## Visual implementation

`LumaView.java` draws the interface and illustrations directly with Android Canvas primitives, paths, gradients, and platform typefaces. There are no downloaded textures, illustration packs, sprite sheets, stock icons, or bundled font files. Rendering remains sharp at different device resolutions; the layout scales from a 390-unit design width and respects the top and bottom system-bar insets.

Pieces use both direction arrows and color, so color is not the only gameplay cue. Switch pieces carry a smaller direction badge. Gates expose their open/closed state through shape and lock/arrow icons. Movement produces a short exit trail; blocked actions shake the piece; hints and vision use an outlined glow. Home and victory screens show the equipped companion.

The six companions are actual vector drawings with different silhouettes. They are cosmetic and do not alter puzzle rules.

| Companion | Silhouette | Base color | Coin price |
| --- | --- | --- | ---: |
| Nova | Rounded star | `#FFE29A` | Included |
| Miko | Cat with pointed ears | `#FF94C2` | 160 |
| Orbit | Robot with visor and antenna | `#8DEBDD` | 220 |
| Pip | Long-eared rabbit | `#CBB0FF` | 300 |
| Astra | Comet with a translucent tail | `#8DC8FF` | 420 |
| Zig | Soft ghost with scalloped base | `#DAF79D` | 600 |

Six skins change the background, cards, accents, and gameplay palette. They are also cosmetic.

| Skin | Background | Card | Primary accent | Secondary accent | Coin price |
| --- | --- | --- | --- | --- | ---: |
| Lumina | `#11142B` | `#232744` | `#A99AFF` | `#7EE8D1` | Included |
| Lagoon | `#081F2A` | `#123745` | `#70E1CF` | `#A5CCFF` | 180 |
| Blossom | `#291429` | `#46223F` | `#FF9CC7` | `#E2B0FF` | 240 |
| Ember | `#281A1C` | `#442B2B` | `#FFBB81` | `#FF8A9F` | 300 |
| Midnight | `#0B1229` | `#192747` | `#8AB9FF` | `#D1B0FF` | 360 |
| Aurora | `#152120` | `#263C36` | `#C1EE9C` | `#86DFC2` | 450 |

The collection provides immediate previews and equips owned cosmetics. Purchases use locally earned coins; the application does not charge real money for these items. Companion and skin display names are localized through the existing string catalog.

## Sound design

`audio/Synth.java` synthesizes all music and effects from mathematical oscillators and envelopes. Bell tones combine a sine fundamental with quiet decaying harmonics; movement and undo sounds use short pitch sweeps. Attack/release envelopes avoid hard starts and stops. No sampled instrument, downloaded track, sound library, speech service, or generative API is used at runtime.

`audio/AudioEngine.java` prepares the effects on a background worker and caches mono 16-bit, 22,050 Hz WAV files in the application's cache directory. SoundPool plays the short effects with up to seven concurrent streams. AudioTrack plays the ambient loop from a static PCM buffer. Calls made before an effect has finished loading are safely ignored.

| Effect API | Duration | Intended feedback |
| --- | ---: | --- |
| `TAP` | 0.250 s | Light interface contact |
| `SELECT` | 0.250 s | Navigation or cosmetic selection |
| `MOVE` | 0.250 s | Ordinary piece exit |
| `BLOCKED` | 0.200 s | A move cannot leave the board |
| `GATE` | 0.460 s | A switch piece changes gate state |
| `UNDO` | 0.360 s | Restore the previous move |
| `HINT` | 0.640 s | Booster activation |
| `COIN` | 0.340 s | Claim an event completion reward |
| `PURCHASE` | 0.740 s | Buy a cosmetic or booster with earned coins |
| `WIN` | 1.450 s | Board completion |
| `LOSE` | 0.600 s | Available descending cue; currently not triggered by the interface |

The original ambient loop lasts **12.8 seconds at 75 BPM**. It combines four soft chord pads, sparse bell notes, and quiet delayed repeats. Note tails wrap into the next iteration instead of being abruptly cut at the loop boundary. The tone and rhythm are deliberately restrained for repeated puzzle sessions.

Music and effects have independent enable switches in Settings, persisted by GameStore. AudioEngine also exposes separate volume controls for integration. Its default gains are 0.24 for music and 0.72 for effects. The music pauses and all active effects stop when the Activity enters the background. The engine relinquishes audio focus, handles focus loss or ducking, and releases its worker, SoundPool, and AudioTrack on destruction. No audio permission is requested.

The cached effects occupy approximately **0.245 MB** in total; the music's PCM buffer is **564,480 bytes**. Cache files can be regenerated if Android clears them. No audio downloads are required.

## Provenance and validation boundaries

All game illustrations, interface paths, palettes, companion shapes, and procedural audio in this implementation were created for this repository. There are **zero bundled third-party art or audio assets**. Android Canvas, Android audio APIs, and device-provided system typefaces are platform facilities. The source is distributed under the repository's AGPL-3.0 license; the Android platform and developer tools retain their own licenses. This provenance statement is not a trademark availability opinion about the game or companion names.

The audio sources compile against the Android 36 API with Java 8 source compatibility. A pure-Java DSP check generated every effect and the music loop: all effect endpoints are zero, the largest effect peak is 14,453 out of 32,767, and the music loop's boundary step is 15 sample units compared with a maximum ordinary adjacent-sample step of 1,175. These measurements show headroom and loop continuity in the generated buffers; they do not measure speaker quality or end-to-end input latency.

Base-palette calculations give normal body text at least 10.9:1 contrast against the skin cards and muted text at least 5.8:1. These checks cover the listed solid card colors, not every translucent layer, disabled state, or illustration. The application includes arrow shapes, localized labels, keyboard navigation, virtual accessibility nodes, and a reduced-motion preference, but this document does not claim complete accessibility certification.

No physical-device sound audition, headphone test, Bluetooth latency test, battery benchmark, color-calibrated display review, or full TalkBack audit was performed for this art/audio document. Use the release validation evidence for any separate emulator checks. Final production acceptance should include those physical and assistive-technology checks.
