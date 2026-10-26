# Art direction and original audio

Luma Drift uses an original, code-drawn visual system: a dark celestial backdrop, soft colored light, tactile rounded pieces, clear arrow marks, and small expressive companions. The interface takes general puzzle-game conventions—short sessions, visible progression, immediate feedback, and readable rewards—without copying another game's artwork, characters, screens, or sound recordings.

Stable 1.1 includes six companions. **Lab 1.2.0-sandbox** expands that collection to 24 and retains the original six IDs, prices and shapes. Stable screenshots remain evidence for the stable release; the expanded collection and account/commerce surfaces have separately labelled [Lab captures and interaction evidence](COMPANIONS_EVENTS.md). The account images show guest/missing configuration, not a successful Google login.

## Visual implementation

`LumaView.java` draws the puzzle interface and illustrations directly with Android Canvas primitives, paths, gradients, and platform typefaces. Game artwork uses no downloaded texture/illustration packs, sprite sheets or bundled font files. The Lab's optional account control separately uses Google's official G branding asset; it is not an original companion illustration. Rendering scales from a 390-unit design width and respects the top and bottom system-bar insets.

Pieces use both direction arrows and color, so color is not the only gameplay cue. Switch pieces carry a smaller direction badge. Remix switches carry two badges and toggle both indicated gates; selected switches outline the affected gate rails. Early campaign boards highlight a safe opening and show contextual guidance. Gates expose their open/closed state through shape and lock/arrow icons. Movement produces a short exit trail; blocked actions shake the piece; hints and vision use an outlined glow. Home and victory screens show the equipped companion.

The launcher uses Nova’s gold five-point silhouette, matching the game and original [brand kit](store/README.md). The home screen includes a rank card and mission entry. Seven distinct first clears unlock an optional star badge for the equipped companion. Original challenge cards render the initial board, gate rails, direction badges and an exact share code; they do not invent a ranking or score.

All companions are vector drawings with shape-specific geometry. The Lab adds silhouettes rather than recoloring one common outline. Their expressions share the visual system; every companion remains cosmetic and does not alter puzzle rules. The original six below remain in both releases.

| Companion | Silhouette | Base color | Coin price |
| --- | --- | --- | ---: |
| Nova | Rounded star | `#FFE29A` | Included |
| Miko | Cat with pointed ears | `#FF94C2` | 160 |
| Orbit | Robot with visor and antenna | `#8DEBDD` | 220 |
| Pip | Long-eared rabbit | `#CBB0FF` | 300 |
| Astra | Comet with a translucent tail | `#8DC8FF` | 420 |
| Zig | Soft ghost with scalloped base | `#DAF79D` | 600 |

The Lab adds 18 companions:

| Companion | Silhouette | Base color | Coin price |
|---|---|---|---:|
| Lune | Crescent moon | `#FBE6A1` | 180 |
| Fenn | Fox with pointed ears | `#FFAF85` | 240 |
| Hoot | Owl | `#CBB0E9` | 280 |
| Buzz | Bee with wings | `#FFE084` | 320 |
| Tula | Turtle | `#9BE6BF` | 360 |
| Jelli | Jellyfish | `#B9B5FF` | 400 |
| Vela | Moth with four wing lobes | `#E5A9DE` | 440 |
| Nimbi | Cloud | `#C5E3FA` | 480 |
| Soli | Sun | `#FFC58C` | 520 |
| Mochi | Snail with spiral shell | `#D2B3F5` | 560 |
| Nixi | Axolotl with external gills | `#FFB6D6` | 620 |
| Prickly | Cactus with blossom | `#ADDEA2` | 680 |
| Quill | Hedgehog | `#DDB8A4` | 740 |
| Waddles | Penguin | `#A5C6EC` | 800 |
| Halo | Ringed planet | `#A8D9E8` | 860 |
| Emberling | Dragon | `#D0B1F7` | 920 |
| Bambu | Panda | `#D4ECE2` | 1,000 |
| Inky | Squid | `#F1ABBC` | 1,100 |

Collection presents eight companions per page across three pages. Opening a card shows a larger native preview before an explicit purchase/equip action. Geometry remains code-native, with no image-generation API or remote asset download during play. [Full collection and event design](COMPANIONS_EVENTS.md).

<p><img src="screenshots/lab/collection-page-1.png" width="225" alt="Lab companions page 1"><img src="screenshots/lab/collection-page-2.png" width="225" alt="Lab companions page 2"><img src="screenshots/lab/collection-page-3.png" width="225" alt="Lab companions page 3"></p>

Actual Android captures show all 24 card silhouettes. The installed UI run also opened native previews and exercised purchase/equip. This is separate from a physical-device, calibrated-display or complete assistive-technology review.

Six skins change the background, cards, accents, and gameplay palette. They are also cosmetic.

| Skin | Background | Card | Primary accent | Secondary accent | Coin price |
| --- | --- | --- | --- | --- | ---: |
| Lumina | `#11142B` | `#232744` | `#A99AFF` | `#7EE8D1` | Included |
| Lagoon | `#081F2A` | `#123745` | `#70E1CF` | `#A5CCFF` | 180 |
| Blossom | `#291429` | `#46223F` | `#FF9CC7` | `#E2B0FF` | 240 |
| Ember | `#281A1C` | `#442B2B` | `#FFBB81` | `#FF8A9F` | 300 |
| Midnight | `#0B1229` | `#192747` | `#8AB9FF` | `#D1B0FF` | 360 |
| Aurora | `#152120` | `#263C36` | `#C1EE9C` | `#86DFC2` | 450 |

The collection previews and equips cosmetics. Stable purchases use earned coins; the Lab's separate wallet can also receive clearly labelled simulated packs or earned test-ad rewards. No real money is charged in either delivered mode. Companion and skin names use the six-language string catalog. Localized text completeness is distinct from native-speaker review.

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

The puzzle illustrations, palettes, companion shapes and procedural audio were created for this repository. Stable 1.1 bundled no third-party game art/audio. The Lab additionally includes the official Google G for its optional sign-in control, under Google's separate branding rules; that mark is not relicensed as original AGPL artwork. Google SDKs can also contain their own resources. [Third-party notices](THIRD_PARTY_NOTICES.md).

Android Canvas, Android audio APIs and device-provided system typefaces are platform facilities. Original project source/art/audio retain the repository's AGPL-3.0 license; platform facilities, SDKs, marks and tools retain their own terms. This provenance statement is not a trademark availability opinion about the game or companion names.

The audio sources compile against the Android 36 API with Java 17 source compatibility. A pure-Java DSP check generated every effect and the music loop: all effect endpoints are zero, the largest effect peak is 14,453 out of 32,767, and the music loop's boundary step is 15 sample units compared with a maximum ordinary adjacent-sample step of 1,175. These measurements show headroom and loop continuity in the generated buffers; they do not measure speaker quality or end-to-end input latency.

Base-palette calculations give normal body text at least 10.9:1 contrast against the skin cards and muted text at least 5.8:1. These checks cover the listed solid card colors, not every translucent layer, disabled state, or illustration. The application includes arrow shapes, localized labels, keyboard navigation, virtual accessibility nodes, and a reduced-motion preference, but this document does not claim complete accessibility certification.

No physical-device sound audition, headphone test, Bluetooth latency test, battery benchmark, color-calibrated display review, or full TalkBack audit was performed for this art/audio document. Use the release validation evidence for any separate emulator checks. Final production acceptance should include those physical and assistive-technology checks.
