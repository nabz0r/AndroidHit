# Art direction and original audio

## 1.17 bundled sprite art and scene music

The new art library supplies original illustrated bodies for all 24 existing companion identities, all 24 garments and all 42 furnishings. Three full-screen place illustrations and three room-material atlases extend the same plum, gold and soft candy palette. The generated assets are bundled with the app; no image-generation service or sprite download runs during play. [Generation prompts](design/v1.17/art-prompts.json) and [asset/identity audit](validation/v1.17/assets/final-art-audit.json) identify the sources. These audits establish packaged coverage, not anatomical quality or final native visual acceptance.

`ArtLibrary` decodes authored sprite regions lazily into a shared 48 MiB cache. `ArtRegions117` records source rectangles and exclusions when a drawing crosses an atlas seam. `CompanionRig`, `CharacterSprites` and the shared `CompanionPortrait` align body, dress, paired gloves, paired shoes and hat in one transform. Item IDs, prices, ownership and save records remain independent of render metadata. The 12 free styles are visual treatments over the original sprites; 24 × 12 combinations do not mean 288 individually authored images.

Actual application screenshots and isolated renderer sheets have different roles. The separate native gallery renders 144 companion/outfit compositions, 24 isolated garments and 24 representative style samples through public pure draw APIs. It does not acquire, equip or prove ownership of those objects in a game. The historical [B gallery](validation/v1.17/art/native/candidate-b-art/host-report.json) passed mechanically but its [visual review](validation/v1.17/review/visual-candidate-b.json) rejected fit and contrast. The [current H gallery](validation/v1.17/art/native/candidate-h-art-gallery/host-report.json) passes 144 compositions, 24 isolated garments and 24 style samples across 50 PNGs. An [independent recomputation](validation/v1.17/review/c-to-h-renderer-pixel-equality.json) confirms all 50 are byte-identical to the fully visually reviewed C originals. This transfers the reviewed anatomy and documented P3 limits by exact pixel equality. F's [50-sheet equality](validation/v1.17/review/c-to-f-renderer-pixel-equality.json) and [58 actual-frame review](validation/v1.17/review/visual-candidate-f.json) remain historical F evidence. Actual H wardrobe views of owned Nova, Miko and Halo separately verify the layered gloves, boots, hat and readable ownership captions in the product UI.

The [current H visual review](validation/v1.17/review/visual-candidate-h.json) directly inspects 97 actual application originals: the four initial normal roots, normal/320dp-font1.3 puzzle HUDs, actual U1/resume audio-route screens, complete scrolled footer/Powers/Share views, genuine fresh Buzz/Aurora reveals/equipped/cold Profile, all 16 final FR/EN website sources, 20 other multilingual/compact/font1.3 samples, three actual owned wardrobes and 30 targeted secondary originals (26 compact across six languages, 4 normal EN/FR). The completed normal and compact primary matrices each record 277 assertions/177 PNGs. Secondary routes separately pass 546 assertions/885 compact PNGs in six languages and 206 assertions/220 normal PNGs in EN/FR. The independent review claims its listed subset, not every capture. Positive stock/detail states and guest pages do not prove zero-stock activation, live commerce or external sign-in. The [final mode preflight](validation/v1.17/native-interface/candidate-h-modes-v2-acceptance.json) records 150 normal plus 154 compact assertions/88 PNGs, while the independent review identifies precisely its smaller viewed subset. H closes the legacy gate/budget/instruction collision and proves the three compact actions fully visible after actual scrolling, with complete nominal 48dp targets and 8dp inset margins. The first compact observer had accepted a clipped target; [that coverage gap](validation/v1.17/review/compact-h-first-reachability-gap.json) was corrected in the observer without changing H. A [separate fresh H route](validation/v1.17/fresh-h/summary-h.json) records 133 checks/105 actual touches/49 PNGs and keeps Buzz plus four Aurora garments and three equipment items after a distinct cold process. These are actual earned outcomes, not the historical F Tula/Moonlight or G Emberling result.

C's wardrobe contrast defect closes at a sampled 5.45:1 versus 1.93:1; E's compact Collection/Atelier overlaps close in F and remain separated in the viewed H roots. Nonblocking limits remain: Jelli has a thin sole-like edge, free styles are accent ornaments rather than new painted costumes, some high collars approach the chin, and long captions can truncate at small widths/larger fonts. Theme entries reuse the map glyph, and a faint isolated gold edge remains at the right of some small FR/IT theme glyphs without intersecting labels or targets. Garments layer over original character accessories rather than repainting every whole body. This is scoped visual acceptance, not an all-screen claim. No renderer sheet or clearly labelled [ANALYSIS crop](validation/v1.17/review/analysis-h/manifest.json) is a marketing application screenshot.

`Synth.sceneLoop(biome, scene)` provides 50 arrangements: `PUZZLE`, `ADVENTURE`, `HOME`, `COLLECTION` and `ATELIER` across ten biomes. The ten puzzle PCM buffers and eleven historical effects are preserved exactly. Forty new place arrangements share the biome's harmonic identity with different orchestration. Three short new earcons identify scene changes, fabric and reveals. The audio worker coalesces scene/biome requests, rejects obsolete generations and retains the existing music/effects, focus, background and release boundaries.

The [executed DSP and controlled lifecycle report](validation/v1.17/audio/scenes/final-b/verification.json) passes 1,714 host checks: 1,638 DSP checks and 76 lifecycle-model checks. It covers uniqueness, RMS, headroom, DC, non-silent windows, loop seams, preservation and bounded concurrent tracks. The historical puzzle PCM remains untouched; new loops have a symmetric 256-frame boundary correction. This is host DSP and a stubbed `AudioTrack` lifecycle model, not proof of installed playback or a physical speaker/headphone audition. H retains those audio source hashes and [re-executes the existing DSP/lifecycle contracts](validation/v1.17/ci-host-rehearsal-h-complete/21-test-audio-scenes-v117.txt).

The exact H APK (`15546854…924a47`, 1.17.0-rc6/code24) separately passes [50 native Android digital audio checks with 10 captures](validation/v1.17/audio/native/candidate-h-main-audio-b/host-report.json) on the retained API36 task AVD. This clean route observes ADVENTURE, HOME, COLLECTION, ATELIER and an already-earned U1/PUZZLE, without touching a piece or consuming a hint/Undo. The framework-only observer uses actual UI, public scene logs, read-only protected preference snapshots and 135 raw AudioFlinger dumps, without app imports, engine/renderer hooks, reflection, preference injection or direct preference writes. The [independent review](validation/v1.17/review/audio-native-final-h.json) recomputes the five scene frame advances directly from live current-PID server rows and checks the latest loaded-scene logs. Android HOME makes every current target track inactive; resume advances the same PUZZLE track. Effects-only taps activate short tracks with music inactive, both-off taps activate neither, and music-only taps leave short tracks inactive.

French, Home and both original On settings are restored through actual UI. The captured protected game/meta/non-active-session snapshot matches both the clean-run origin and the original snapshot from before a [preserved H observer-label failure](validation/v1.17/audio/native/candidate-h-main-audio-a/host-report.json). That observer had searched for `Exit` while the public caption was `Leave level`; a corrected observer and a [separate 11-check/two-capture restoration](validation/v1.17/audio/native/candidate-h-main-audio-recovery-a/host-report.json) completed before the fresh clean route. Its checks are not added to 50/10. Naturally selected active-puzzle fields are documented separately; native assertions confirm zero gameplay action/cost. The observer is removed and the exact H APK remains installed. Fifty checks include identity, UI, state protection and restoration; they are not 50 recordings.

The historical [F observer timeout](validation/v1.17/review/audio-observer-timeout-triage.json), [F38/8 result](validation/v1.17/review/audio-native-final-f.json) and [G35/7 result](validation/v1.17/review/audio-native-final-g.json) remain unchanged under their own identities. The F timeout occurred after HOME with synchronous Activity launch as the next source call; no thread stack establishes its exact cause. The emulator has no host audio output, so H's result establishes digital playback state, **not physical listening, musical taste or phone-speaker balance**. Native music for all ten biomes, puzzle-move effects and a real competing external audio-focus app are outside the five sampled contexts.

The [original H transition recording](validation/v1.17/native-interface/candidate-h-transitions/transitions/transitions.mp4) has a passing 24-assertion native route and records three actual object taps: Adventure → Home → Collection → Home. Its [independent review](validation/v1.17/review/motion-h/review.json) inspects 21 full 1080 × 2340 decoded frames around the three changes. Fade/short horizontal movement and correct settled destinations are visible; no blank content or stranded outgoing snapshot is seen in the sampled frames. These are clearly labelled ANALYSIS H264 derivatives, not native PNG screenshots or website images. The MP4 contains 101 frames over 7.985700 seconds, averaging 12.65 encoded frames/s; the SDK control interval is 8.283 seconds. Adjacent PTS gaps reach 872.066 ms at the first transition. It therefore **does not establish 60 fps, smooth animation pacing or physical-phone fluidity**, and does not separate app, emulator and encoder causes. The video has no audio stream. The separate [F motion review](validation/v1.17/review/motion-f/review.json) remains historical F evidence.

Four optional 10-second previews use the app's exact synthesizer, biome 0 and default music gain 0.27. The entrance follows the engine's 900 ms fade; only the preview ending adds a 450 ms export fade. These files are **synthesizer renders, not phone recordings**: [Adventure](validation/v1.17/audio/scenes/final-b/scene-adventure-preview.wav), [Home](validation/v1.17/audio/scenes/final-b/scene-home-preview.wav), [Collection](validation/v1.17/audio/scenes/final-b/scene-collection-preview.wav), [Workshop](validation/v1.17/audio/scenes/final-b/scene-atelier-preview.wav). The staged website copies them byte for byte and never autoplays them.

[Research](design/v1.17/reference-research.md) records the cognitive/artistic observations from official Crash Fever, King, Apple and Android sources. No franchise artwork, characters, sprites, recordings or code is included, and those references do not establish future retention or audience size. [Current acceptance register](validation/v1.17/README.md).

## Preserved art and audio records

The sections below describe their identified historical renderer and release. Statements about vector-only companions or ten music arrangements do not describe1.17.

## 1.12 original shop illustrations

Six friendly power silhouettes, an amber coin, a faceted violet/cyan LumaGold gem and a capsule use code-native original art. The same powers appear in gameplay, inventory and the shop. Rounded accents and flatter pill surfaces reduce the stacked-box treatment. Draw helpers reuse Paint/Path/RectF objects, never change economy/state, and freeze temporal art in reduced motion. [Independent final raster scope](validation/v1.12/art-rc2/README.md). The existing original music and its separate controls are retained.

## 1.11 equipment visibility

The shared portrait now leaves more character silhouette visible under clothing and separates/enlarges Head, Aura and Charm. Existing original code art, styles, identities, equipment effects and music are retained. The selected scene theme now tints both the garden and animated gameplay. [Measured composition and limits](validation/v1.11/avatar-bugs/README.md) · [Release](RELEASE_1_11.md).

## Preserved 1.9 companions and moving stages

The original code renderer adds **24 companion silhouettes × 12 visual treatments**, five poses and actual-progress parallax with a quiet central reading area. Cyberpunk, arcade, modern pixel, sakura, ocean, space, steampunk, candy, Halloween, Valentine, ice and plush treatments change geometry/accessories as well as colour. These are 288 combinations, not 288 hand-painted assets. Styles are free and do not grant unowned companions or gameplay benefits. Reduced motion freezes the temporal and travelling effects. [Renderer and native atlas evidence](validation/v1.9/avatar/README.md) · [UX](UX_1_9.md).

The original Dream Glade illustration and ten synthesized music arrangements remain. No new third-party game art, recording, font or generated illustration was added in 1.9.

## Preserved 1.8 presentation

Cream/lilac surfaces, plum text, soft-volume arrow pieces, original animated star tutor and fingertip, six distinct power silhouettes and refreshed four-slot clothing form the current native/web family. Dream Glade replaces the default Atlas background; its built-in generation prompt and exact pixel hash are [recorded](art/dream-glade-v18.json). The image is copied unchanged into Android and public assets. Ten subtle tints share this one scene.

The ten original synthesized biome arrangements are retained, with separate music/effects settings and reduced motion. No new licensed music or third-party game asset was added. [Preserved 1.8 UX](UX_1_8.md) · [Design system](DESIGN_SYSTEM.md) · [Final 1.8 captures/evidence](RELEASE_1_8.md). The sections below document preserved earlier artwork/audio and must not be read as a claim that older captures show 1.8.


Luma Drift combines code-drawn puzzle pieces and companions with a bundled generated environment: a dark celestial backdrop, soft colored light, tactile rounded pieces, clear arrow marks, and small expressive companions. The interface takes general puzzle-game conventions—short sessions, visible progression, immediate feedback, and readable rewards—without copying another game's artwork, characters, screens, or sound recordings.

The **1.7 candidate** adds code-native kawaii power illustrations and a four-slot visual wardrobe, a continuously scrollable 1–1,000 route with ten 100-level tints and original procedural arrangements, and exact current-state hint/recovery feedback. All 24 new Hat/Outfit/Shoes/Gloves items are appearance-only; the preserved 18 Head/Aura/Charm equipment items remain a separate bounded-benefit system. The existing 24 companion silhouettes, six themes and Aurora Atlas stay. [1.7 scope](RELEASE_1_7.md) · [UX](UX_1_7.md) · [Economy](ECONOMY_1_7.md). Final package, installed screens and playback require the [1.7 evidence record](validation/v1.7/README.md); historical captures do not establish new behavior.

Stable 1.1 originally included six companions. **Lab 1.2.0-sandbox** expanded the collection to 24, preserving those original IDs, prices and shapes; later candidates retain that collection. Stable screenshots remain evidence for their identified release. The expanded collection and account/commerce surfaces have separately labelled [Lab captures and interaction evidence](COMPANIONS_EVENTS.md). Historical guest/missing-configuration account images are not evidence of successful Google login.

## 1.7 native wardrobe and biome atmosphere

`ui/KawaiiArt.java` draws original power and wardrobe shapes with Canvas paths, circles, rounded forms and reusable Paint/Path state. Hat, outfit, shoes and gloves are recognizable slot shapes; a companion preview layers the equipped visual items. This is native code art, not a downloaded sprite pack, generated runtime image or another game's character. Slot names, ownership, source, quantity and price remain explicit foreground information. Rarity does not imply a gameplay bonus. Exact caption and physical legibility remain acceptance work.

`ui/SagaMap.java` defines virtual route coordinates and visible-node ranges. Journey 1–1,000 is grouped into ten 100-level atmosphere ranges: First Light, Mint Meadows, Peach Coast, Cloud Gardens, Crystal Lagoon, Star Orchard, Moon Festival, Pumpkin Grove, Rose Aurora and Dream Summit. `WorldBackdrop` applies biome tints to the original Atlas environment; these are not ten newly generated or uniquely illustrated background images. The player's six palette themes and the route's biome atmosphere are separate controls.

`Synth.ambientLoop(biome)` prepares ten eight-bar arrangements. Biome zero preserves the original 25.6-second buffer; the other nine vary mode, harmony, melody, rhythm, tempo and timbre. `AudioEngine.setBiome()` coalesces repeated requests on its worker, rejects obsolete generation results and releases replaced tracks. The short changeover fade and existing music/effect preferences remain independent. No network music or generation service runs during play. [Executed biome audio checks](validation/v1.7/audio/README.md) pass 558 DSP/lifecycle checks, including worker races, ten distinct buffers, historical effect/theme preservation and bounded model allocation. The generated arrangements span 66–96 BPM and 20–29.091 seconds, with maximum PCM size 1,282,910 bytes; the worker retains no ten-loop cache. [Metrics](validation/v1.7/audio/audio-biome-metrics.csv) · [20-second montage](validation/v1.7/audio/audio-biome-preview.wav). These are host measurements and an excerpt from source buffers. Only an identified installed playback record establishes actual Android output; none of these is a speaker/headphone audition.

The actual safe hint pulse follows a safe piece in the current remaining board. It cannot advertise a winning hint in a state with no solution or insufficient actions. Dead-end and action-shortage feedback have text/recovery meanings in addition to animation. [Reproducible J13 mechanism](validation/v1.7/board-audit-journey13.md).

## Visual implementation

`LumaView.java` draws puzzle pieces, companions and controls with Android Canvas primitives, paths, gradients, and platform typefaces. Version 1.6 adds the original generated **Aurora Atlas** environment, bundled in the APK as one PNG and decoded once into an RGB565 bitmap by `WorldBackdrop`. It is quietened behind gameplay and inventory cards. No image service or asset download runs during play; no sprite pack or bundled font is used. The [asset record](art/aurora-atlas-v1.json) preserves the exact image-generation prompt, tool, original output path, dimensions and SHA-256. The environment is illustrative art, not a screenshot or evidence of runtime. The Lab's optional account control separately uses Google's official G branding asset; it is not an original companion illustration. Rendering scales from a 390-unit design width and respects the top and bottom system-bar insets.

Pieces use both direction arrows and color, so color is not the only gameplay cue. Switch pieces carry a smaller direction badge. Remix switches carry two badges and toggle both indicated gates; selected switches outline the affected gate rails. Early campaign boards highlight a safe opening and show contextual guidance. Gates expose their open/closed state through shape and lock/arrow icons. Movement produces a short exit trail; blocked actions shake the piece; hints and vision use an outlined glow. Home and victory screens show the equipped companion.

The 1.5 **Sealed passages** prototypes add fixed wall cells and matching key markings. A wall disappears when its key piece exits and reappears when Undo restores that key; gate changes remain a separate part of the move. The menu explicitly identifies six prototypes with no rewards or powers. [Installed checks](validation/v1.5/sealed-ui.txt) exercised all six, including P1 blocking/opening/Undo/restoration. The final shared PNG renderer also includes walls and key markings, with [P1/P6 share evidence](validation/v1.5/sealed-share-ui.txt). Human first-time comprehension remains unmeasured.

Version 1.6 also uses keys and seals in the normal frozen J/K route: 167 Journey boards and 29 Remix boards. Journey 12 teaches one key and one seal with open gates and no switches; boards 14 and 17 practice the element. The workshop's P identities stay separate. The matching key/seal markings represent a real dependency, not a decorative obstacle. [Content, certificates and compatibility](PROGRESSION_V3.md).

Lab 1.3 adds a visible action allowance and six original power icons: a Spark bulb, Vision eye, Flow arrows, Shield outline, Focus target and Trail path. Shield displays remaining protected blocks. Focus outlines exact safe moves for up to ten seconds; Trail numbers up to five moves of a complete solution for up to twelve seconds. Either preview ends at the next successful exit or Undo. The [historical 1.3 interface evidence](VALIDATION_1_3.md) uses a clearly disclosed synthetic emulator profile; the [final runtime report](validation/v1.3/move-power-ui-smoke.txt) records exact stock/action behavior and restoration. This is separate from physical-device or assistive-technology acceptance.

The launcher uses Nova’s gold five-point silhouette, matching the game and original [brand kit](store/README.md). The 1.5 lobby centers the equipped companion and its equipment, the next Journey board, and access to progression, missions and the collection. Actual layout acceptance remains a release check. Seven distinct first clears unlock the existing optional star badge. Original challenge cards render the initial board, gate rails, direction badges and an exact share code; they do not invent a ranking or score.

All companions are vector drawings with shape-specific geometry. The expanded collection adds silhouettes rather than recoloring one common outline. Their expressions share the visual system; the companion identity itself remains cosmetic and does not alter puzzle rules. Equipment is a separate system, with explicit slots and capped benefits. The original six below remain available.

| Companion | Silhouette | Base color | Coin price |
| --- | --- | --- | ---: |
| Nova | Rounded star | `#FFE29A` | Included |
| Miko | Cat with pointed ears | `#FF94C2` | 160 |
| Orbit | Robot with visor and antenna | `#8DEBDD` | 220 |
| Pip | Long-eared rabbit | `#CBB0FF` | 300 |
| Astra | Comet with a translucent tail | `#8DC8FF` | 420 |
| Zig | Soft ghost with scalloped base | `#DAF79D` | 600 |

The expanded collection adds 18 companions:

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

The 1.6 collection presents three full-width rows per page across Companions, Themes, Equipment and Powers, with All/Owned/To unlock filters and explicit selected/equipped/owned/unlock states. Power rows show stock, active status and unlock level. Opening a card shows a larger native preview before an explicit purchase/equip action. Companion and item geometry remains code-native. There is no image-generation API or remote asset download during play. The eight-card screenshots below retain their historical Lab 1.2 scope. [Full collection and event design](COMPANIONS_EVENTS.md).

<p><img src="screenshots/lab/collection-page-1.png" width="225" alt="Lab companions page 1"><img src="screenshots/lab/collection-page-2.png" width="225" alt="Lab companions page 2"><img src="screenshots/lab/collection-page-3.png" width="225" alt="Lab companions page 3"></p>

Historical Lab 1.2 Android captures show all 24 card silhouettes; they do not depict the later six-power/budget interfaces. The installed UI run also opened native previews and exercised purchase/equip. This is separate from a physical-device, calibrated-display or complete assistive-technology review.

Six skins change the background, cards, accents, and gameplay palette. They are also cosmetic.

| Skin | Background | Card | Primary accent | Secondary accent | Coin price |
| --- | --- | --- | --- | --- | ---: |
| Lumina | `#11142B` | `#232744` | `#A99AFF` | `#7EE8D1` | Included |
| Lagoon | `#081F2A` | `#123745` | `#70E1CF` | `#A5CCFF` | 180 |
| Blossom | `#291429` | `#46223F` | `#FF9CC7` | `#E2B0FF` | 240 |
| Ember | `#281A1C` | `#442B2B` | `#FFBB81` | `#FF8A9F` | 300 |
| Midnight | `#0B1229` | `#192747` | `#8AB9FF` | `#D1B0FF` | 360 |
| Aurora | `#152120` | `#263C36` | `#C1EE9C` | `#86DFC2` | 450 |

The collection previews and equips cosmetics. The offline shop uses earned local coins; the Lab's separate balance can also receive clearly labelled simulated packs or earned test-ad rewards. The production account wallet is a separate service for permanent cosmetics; its commercial activation and purchase qualification remain pending. Equipment purchases use local earned coins. [Current value and validation boundaries](RELEASE_1_6.md). Companion, theme and equipment text uses the six-language catalog; automated text completeness is distinct from native-speaker review.

## Final 1.7 capture provenance

The [final Lab menus2 report](validation/v1.7/ui-sdk-final-menus2/host-report.json) identifies delivered Lab APK `d7fea345…`, 38 native assertions and 32 unedited captures on API 36/16KB. Its existing guest profile receives one daily gift and performs an earned capsule draw; no preference injection, provider authentication or real payment is used. The wrapper restores all original private preferences afterward. [Current illustrated tour](VISUAL_GUIDE.md).

The website’s eleven `assets/screenshots/v1.7/` copies come byte for byte from that passing directory. `scripts/check-site-v17.py` accepts a new image only when the native host report is passing/restored, its exact final APK matches, and the source PNG hash matches the capture record. Public captions identify these as Lab images. The generated Atlas, original Nova vectors and earlier 1.6/1.5 assets keep their own pixel/version provenance.

## Preserved Android captures and website derivatives

The preserved 1.6 product references are the unedited PNGs under [final production 1.6 captures](screenshots/v1.6/ui/final-production/). Their [installed run report](validation/v1.6/ui-final-production/report.json) identifies production version 1.6.0-rc1, code 7, and its exact APK hash. The [device record](validation/v1.6/ui-device-runtime.json) identifies an API-36 arm64 emulator with a 16,384-byte kernel page size, 1080 × 2340 pixels and 440-dpi density. The menu run used no injected profile and restored the existing preferences. Home visibly resumes an existing practice challenge. These observations do not establish physical-phone quality, successful Google sign-in or commerce activation.

The separately published website 1.6 copies those captures for its FR/EN presentation. A surrounding phone frame, CSS crop, glow or brand illustration is a web composition; it must not be labelled as an unedited Android capture. Keep source paths, derivative steps and byte hashes in the versioned site evidence under `docs/validation/site-v1.6/`. The original [1.5 brand kit manifest](store/assets.json), [1.5 site asset record](validation/site-v1.5/source-assets.json) and [Aurora Atlas pixel provenance](art/aurora-atlas-v1.json) retain their distinct source/version identities. The [site qualification and delivery record](validation/site-v1.6/README.md) separately identifies the executed local page checks and Firebase publication; Android screenshots alone do not establish website delivery.

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

The preserved 1.5 original ambient loop, retained as 1.7 biome zero, lasts **25.6 seconds at 75 BPM**. Its eight bars combine overlapping chord pads, a low-register voice, bell phrases and quiet delayed repeats. Note tails wrap into the next iteration. This replaces the earlier 12.8-second phrase. A static-track initialization defect previously discarded the music before its first PCM write; the repaired engine writes the buffer before requiring `STATE_INITIALIZED`. [Cause, before/after regression and test scope](validation/v1.5/audio/README.md).

Music and effects have independent enable switches in Settings, persisted by GameStore. AudioEngine also exposes separate volume controls. Its default gains are **0.27 for music and 0.72 for effects**. Initial music playback and foreground resume fade in over 900 ms; disabling music fades out over 180 ms. Focus ducking also changes the level smoothly. Backgrounding or losing focus pauses output immediately, without finishing an audible fade in the background. Resume reuses the same track and position. Destruction cancels fade callbacks and releases the worker, SoundPool and AudioTrack. No audio permission is requested.

The cached effects occupy approximately **0.245 MB** in total; the music's PCM buffer is **1,128,960 bytes**. Music preparation is independent of effects-cache availability. Cache files can be regenerated if Android clears them. No audio downloads are required.

[Listen to the 16-second preview](validation/v1.5/audio/ambient-preview.wav). It uses the actual default music gain and entrance fade; its final fade only closes the excerpt. [Measured PCM levels](validation/v1.5/audio/audio-levels.txt).

## Provenance and validation boundaries

The puzzle illustrations, palettes, companion shapes and procedural audio were created for this repository. Aurora Atlas was generated with the built-in image generation tool for this repository; it is separately identified so code-native geometry and generated pixels are not confused. Stable 1.1 bundled no third-party game art/audio. The Lab additionally includes the official Google G for its optional sign-in control, under Google's separate branding rules; that mark is not relicensed as original AGPL artwork. Google SDKs can also contain their own resources. [Third-party notices](THIRD_PARTY_NOTICES.md).

Android Canvas, Android audio APIs and device-provided system typefaces are platform facilities. Original project source/art/audio retain the repository's AGPL-3.0 license; platform facilities, SDKs, marks and tools retain their own terms. This provenance statement is not a trademark availability opinion about the game or companion names.

The 1.5 audio sources compile against Android 36 with Java 17 source compatibility. [Executed checks](validation/v1.5/audio/automated-checks.txt) include **65 DSP checks** on the generated buffers and **36 lifecycle/focus checks using a JVM Android model**. All effect endpoints are zero. At the default music gain, the loop measures **−28.45 dBFS RMS and −15.94 dBFS peak**; its boundary step is **17 sample units**, compared with a maximum ordinary adjacent step of **2,710**. These are digital PCM measurements, not LUFS, speaker quality, output capture or end-to-end latency. The model reproduces the former zero-write failure and passes after the repair. On the exact final production APK, [actual Android audio-service observations](validation/v1.5/audio-runtime.json) show advancing playback, background pause and foreground resume; this is not a physical speaker/headphone audition.

Base-palette calculations give normal body text at least 10.9:1 contrast against the skin cards and muted text at least 5.8:1. These checks cover the listed solid card colors, not every translucent layer, disabled state, or illustration. The application includes arrow shapes, localized labels, keyboard navigation, virtual accessibility nodes, and a reduced-motion preference, but this document does not claim complete accessibility certification.

No physical-device sound audition, headphone test, Bluetooth latency test, battery benchmark, color-calibrated display review, or full TalkBack audit was performed for this art/audio document. Use the release validation evidence for any separate emulator checks. Final production acceptance should include those physical and assistive-technology checks.
