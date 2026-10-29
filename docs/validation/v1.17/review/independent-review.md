# Independent art, navigation and runtime review — 1.17

Status: A and B were visually rejected; C retained a P2 wardrobe-label contrast defect and E retained two compact root composition defects. F corrects those defects and passes the 58-frame inspected UI scope. All 50 separately executed F renderer PNGs are byte-identical to the completely reviewed C originals. Android digital playback separately passes 38 native checks and 8 captures after a preserved observer-only timeout. This document is not unconditional full-release acceptance.

## Scope actually inspected

- Original Luma 1.16 Collection and furnished House screenshots, for a real before/after reference.
- All 20 original art PNGs in `app/src/main/assets/art/v117/`: four companion sheets (24 characters), four clothing sheets (24 pieces), seven furniture sheets (42 objects), two action-object sheets (12 props), and three full scene backgrounds.
- Source: LumaView screen sizing, touch ownership, transitions, root gestures, text editing, modal confirmation, accessibility provider and lifecycle; ArtLibrary; CharacterSprites; CompanionPortrait; ScenicUi; SceneActionView; the four standalone account/social/vault/commerce activities; AudioEngine and scene arrangement changes in Synth.
- New native screenshots have not been inferred from the asset sheets or promotional compositions.

## Findings and status

| Priority | Finding | State |
| --- | --- | --- |
| P1 | Whole-atlas cache held only eight 6 MiB atlases. A furnished home with one companion, four clothing sheets and navigation props could need more atlases than the 48 MiB cache, causing repeated synchronous decode/alpha scans. | Corrected: full-resolution per-cell BitmapRegionDecoder cache, clipping before decode, no cache increase. Host contract passes; native stable-frame counters remain required. |
| P2 | Language targets were 39 logical units high with 43-unit row spacing. | Final E source uses 58-unit targets, 62-unit row spacing, a physical 48 dp expansion floor and settings content height 1110. Expansion stays centered and clips without moving a partly hidden row onto the preceding language. Native compact-size qualification remains separate. |
| P2 | Canvas object-action minima used 48 logical units on a 390-unit scene rather than a physical dp floor. On a viewport 360 dp wide, 48 units equal about 44.3 dp. | Integrator corrected the physical dp floor and E additionally fixes offscreen target relocation. The dedicated compact native qualification remains separate from this visual review. |
| P1 | Native A placed the complete glove pair over the chest and duplicated feet. B separated the pieces but retained Nova's raised hand and some species' original boot edges; rectangular masks also damaged adjoining art. | Corrected and visually verified in all C looks, exact E renderer pixels, and the three fresh E actual Wardrobe frames. |
| Review risk | Detailed painted props and faces must remain legible when scaled onto the actual small phone interface; a high-resolution sheet alone is insufficient. | Reviewed normal E frames show detailed art. Compact native qualification remains a separate report. |

## Art observations

The new cast has distinctly different silhouettes: the fox, owl, turtle, robot, snail, panda, cactus and humanoid characters are recognizable independently of their names and colors. Faces, hairstyles, textiles and metallic trim are substantially richer than the flat circular bodies in 1.16. The five signature themes have clearly different ornament, shape and material treatments.

Furniture has separate recognizable shapes and functions, including library, piano, greenhouse, record player, terrarium, pond and kitchen. These are independent original sprites rather than a single pre-furnished picture. Scene backgrounds follow a common astral palette while separating outdoor journey, collection hall and craft atelier.

All 20 source PNGs were viewed. Eight sampled sprite sheets were also checked from their actual alpha bytes: sampled cell corners are transparent, and nontransparent area varies by art. The image viewer's dark RGB under transparency is not evidence of an opaque rectangle. Final alpha and source-integrity audit belongs to the asset pipeline report.

This asset review does not prove that clothing is anatomically aligned, that furniture fits the live floor, or that labels contrast against the composited backgrounds.

## Navigation and input source review

The four roots have visible object destinations plus optional horizontal swipes. Swipe ownership begins only on free scene space, never a pressed target, modal, furniture editing state or home camera area. Vertical scrolling cancels root swipe eligibility. Touches arriving during a transition are discarded through their release; accessibility clicks also check transition state and the action generation. Back cancels or completes presentation and remains available.

No app-owned Button, AlertDialog.Builder or Spinner construction remains in the four standalone activities reviewed. Their affirmative/cancel actions remain distinct. Consent is still a real checkable control, and disabled actions remain disabled. External Google, Android and payment-provider interfaces are not replaced by game art.

`SceneTransition` is clock-driven, has a 220 ms duration, finishes synchronously under reduced motion, and does not execute navigation or transaction callbacks. Full screen snapshots remain a separate allocation and need device memory/runtime qualification.

## Cache contract evidence

`art-library-host.json`: PASS, 2,771 checks (including all102 measured source regions and cached glove halves). The test compiles the actual ArtLibrary source and uses real packaged PNGs through host Android/image doubles. It verifies:

- 24 representative simultaneous sprites remain 512 × 512 cells at 25,165,824 bytes of residency.
- 100 repeated frames do not reopen or decode art after warm-up.
- Clipped sprites do not decode; small-budget eviction remains bounded.
- Previously drawn bitmap references remain usable after explicit cache trim.
- Odd-sized 107 × 67 source sheets preserve all six cell boundaries; alpha scanning uses cell-local coordinates.
- Every region decoder closes, diagnostics are immutable, and the tested alpha/decode paths have no failures.

The pre-existing pure geometry suite also passes 19,857 checks. Host image doubles do not validate the Android PNG region decoder, actual heap usage, GPU upload, frame pacing or the emulator's audio output. Those distinctions are intentional.

The actual decode path now emits one `LumaArt117` informational record per newly decoded sprite, containing only its public art key, dimensions, total decode count, evictions and resident bytes. A native observer can compare the log before and after a stationary warm scene without invoking the renderer or diagnostic methods. The log does not run per frame. No stable native B interval has been observed by this reviewer yet.

## Original source-region correction

`render-regions.json` records all 102 inventory identities, the 17 unchanged source PNG hashes, exact source rectangles and render-only exclusion rectangles. `ArtRegions117` is the runtime registration source for the same geometry. The companion, clothing and furniture registrations and both action-object sheets use it. Equal-cell registration remains available for unrelated callers and is covered by the odd-size compatibility test.

This correction preserves full-resolution original pixels. Several painted subjects cross the nominal 512-pixel grid: for example, the large sofa extends to source x=1107 instead of ending at x=1024. Some neighboring alpha components also enter another subject's bounding rectangle. The source rectangle is widened around the intended subject, and a single composed clip path excludes the measured neighboring components when drawing. No source PNG or cached bitmap pixels are rewritten.

The audit reports zero lost primary opaque pixels and zero retained foreign primary opaque pixels for each selected region. Its primary-component threshold is alpha 128, with alpha 8 used for visible bounds and protected edge pixels. This is geometric evidence, not an artistic oracle. In `actions-1.png`, the mannequin and settings subjects touch at source row 529; the metadata partitions that short seam. Tiny detached accessories and Android antialiased clip results still require actual native visual review. The manifest intentionally records those limitations instead of treating the numeric audit as final acceptance.

## Independent audio source review

No new concrete P1/P2 race was identified in the scene-selection change. The constructor's preparation and subsequent place/biome requests share one coalescing worker. A shared generation invalidates stale DSP output before installation. Loaded identity now includes both biome and scene, so changing place at a fixed biome cannot falsely reuse the old arrangement. A stale candidate is released in `finally`.

Pause immediately silences and relinquishes focus; release invalidates generation, removes fade callbacks and shuts the worker down. Transient focus suppression and ducking are checked before playback. Switching back to an already loaded scene while its fade is running restores its target gain instead of installing stale output.

A runtime AudioTrack preparation failure still requires a later changed scene/biome to retry; the reviewed change does not add an automatic retry loop. This resembles the prior error behavior and has not been reproduced on the candidate device.

PCM generation, numeric waveform checks, AudioFlinger state and a human listening test are different evidence. Source review cannot establish pleasing timbre, balanced output on phone speakers, audibility on the user's device, or absence of audible loop seams. Any delivery report must keep hardware listening limitations separate.

The independently read `audio/scenes/final-b/verification.json` reports 1,714 passing host checks: 1,638 DSP checks and 76 controlled lifecycle-model checks. It covers 50 biome/place loops, including 40 new place arrangements, and three new earcons. The new arrangements have a measured boundary step of zero; the historical puzzle arrangements and effects remain preserved. Its four ten-second scene previews are synthesized app PCM, not recordings from Android or physical speakers. These figures establish the stated waveform/model scope only.

## Native candidate A findings

Five original English 1080 × 2340 screenshots were viewed: Collection, custom Profile, furnished Home, Atelier and last summon reveal. Their exact paths and SHA-256 hashes are recorded in `visual-candidate-a.json`. The 206 functional native checks remain valid within their functional scope; the images do not pass visual acceptance.

The previously suspected glove mismatch is visible: the pair sits on the chest, leaving original hands exposed. The hat floats and the footwear produces additional feet. Equal-grid source clipping is also visible: the large Atelier sofa loses its right side, and pieces of neighboring props appear beside the table and navigation objects. These are P1 visual defects for this requested release, not optional art preferences.

The new house shell also lacks the detail of the painted furniture, leaves wide floor gaps and uses the same miniature house for all three room destinations. It remains a P2 direction/coherence issue against the user's stated request.

The next B candidate implemented normalized body fitting plus separate hand/foot layers and measured regions for all 102 source sprites, with original PNG hashes preserved, overlap clips and no loss of the measured opaque primary subject components. The following B review distinguishes what those source changes corrected in actual rendering from defects that remained.

## Native candidate B findings

The exact production APK is `26467d23c04bf387f3c048b9ce3d7ddf9ef55cadc816129c5beb7a45be05db68`. The independent review covers the four supplied root/Profile frames, Wardrobe/Collection/Profile for Nova, Miko and Halo, and five actual outfit previews for each of those three owned characters: 28 original app UI frames. Twelve additional Android-rendered QA sheets cover six complete looks each for Vela, Nimbi, Soli, Mochi, Nixi, Prickly, Quill, Waddles, Halo, Emberling, Bambu and Inky. These are explicitly separate renderer probes, not app UI or ownership evidence. Paths and PNG hashes are in `visual-candidate-b.json`.

The actual B images establish substantial corrections. The large Atelier sofa is complete, and the previously visible neighboring pieces beside mirror and blueprint props are absent in the reviewed roots. Halo's hat connects with the hair, gloves occupy the two hands and shoes no longer produce the obvious extra feet seen in A. The house has detailed wallpaper and wood material, and the three room destinations now use distinct sofa, bed and greenhouse art. The reviewed app frames have no old pill/tab-bar/button plates.

B still fails visual acceptance for three concrete rig defects. Nova retains its raised bare hand beside the glove in all three app portrait contexts and five outfit previews. Original left boot remnants remain on Quill, Waddles and Emberling, with a smaller point on Bambu in the separate native gallery. Finally, hard rectangular original-ink masks punch conspicuous rectangular gaps through adjoining coat, tail or tentacle art; this is visible in Miko's actual outfit previews and several gallery species. The source-owner has received precise native coordinates and source-derived full foot centers. Residue centers must not be mistaken for anatomical centers.

The low-contrast Collection heading is also visible over the busy dark background. The large flat pastel house window remains visibly inconsistent with the detailed wall material and furniture. Nova headwear placement and Nova/Miko necklines need a final check after the P1 anatomy fixes: some headwear leaves open space above the hair, and several collars reach the mouth.

The source-owner reports C changes to measured anatomical anchors, oval palm exclusions, tapered boot silhouettes and Nova's hat height. The integrator also plans the gradient-paint alpha reset and physical 48 dp target correction. None of those C changes is treated as visually accepted until the next native frames and renderer sheets are inspected.

## Native candidate C findings

The exact C production APK is `eb67c6214d2868b21d02a0367d743bd2fed45b408b4d544523b8876ef3b78fff`. Thirteen actual UI frames were viewed: four root/Profile captures and Wardrobe/Collection/Profile for each of Nova, Miko and Halo. The independent gallery review then covered every one of the 24 character sheets with six complete painted looks (144 combinations), all 24 isolated garment PNGs and the 12 free visual treatments on both Nova and Halo (24 treatment previews). All 63 inspected PNG paths/hashes and scope labels are in `visual-candidate-c.json`.

The concrete A/B anatomy and source-clipping blockers are corrected in these C pixels. Nova's raised bare hand no longer protrudes next to its glove in the real app. Quill, Waddles, Emberling and Bambu no longer retain the old boot pieces. The hard rectangular holes in tails, coats and tentacles are absent at the conspicuous scale seen in B. Hats connect with the head silhouettes; different standing and lifted-foot poses remain distinguishable. The sofa and reviewed navigation props are whole. Detailed wallpaper, wood floor and painted window now form a coherent house material treatment. Collection and Atelier headings have a reliable light wash.

The remaining P2 is wardrobe status copy. `Owned`, `To unlock` and `Equipped` cross the upper shelf edge in grey ink over brown wood. The actual opaque text/shelf colors sampled in the Nova Wardrobe PNG are `#62647c` and `#ba8b73`, a contrast ratio of approximately 1.93:1. Moving this copy onto pale background with dark ink would address both overlap and contrast. The integrator has been notified; the reviewer did not edit frozen app sources.

A minor P3 is a thin brown sole-like line below Jelli's front shoe in the separate six-look QA sheet. It is materially smaller than the duplicated boots that rejected B. The twelve free treatments are subtle surrounding ornaments, not twelve newly illustrated costumes, and must be described accurately. High collars can overlap the lower chin; the composition is improved and usable but does not establish bespoke tailoring for every outfit and pose.

The C anatomy/source-clipping pass does not substitute for compact/six-language runtime evidence, actual U1–U1000 replay, native performance measurements or the separate Android audio observer. None of those outcomes is inferred from gallery PNGs.

## Prepared Android digital audio observer

`scripts/v117-audio-test` contains a separate framework-only instrumentation observer, exact-candidate/AVD wrapper, build configuration and scope documentation. It was built and executed on exact F only after explicit device-owner handoff and the parent's frozen APK/hash. Its final native result is recorded below.

The route observes the four actual places via UI, requires the latest loaded-scene PID log and advancing AudioFlinger music counters, exercises HOME pause/resume, independently checks music/effects enabled states using harmless repeated UI taps, then restores the original settings/language. It accesses no target classes or preferences and makes no purchase or puzzle move. The emulator has no host audio output: the resulting evidence is digital playback state, never physical listening or a claim about musical taste or phone-speaker quality.

## Final candidate E visual findings

The exact E production APK is `48134c248b88a277d93d6745be6efd7a825f13629ea591e6b41e11cbefabf1eb`, version 1.17.0-rc3/code21. Fourteen fresh actual application frames were directly inspected: Adventure, furnished Home, Collection, Atelier, Badges and Wardrobe for Nova, Miko and Halo, followed by six normal-phone language samples (German Collection/catalogue, Italian Profile/exclusive banner, French Home/reveal). Their exact paths and hashes are recorded in `visual-candidate-e.json`.

The wardrobe P2 is corrected in all three owned-character frames. `Owned`, `To unlock` and `Equipped` now sit on a pale background, with a soft shadow below the text rather than a wood edge crossing it. The same original Nova sample rectangle now has opaque text `#62647c` over predominant background `#fcf7f7`, approximately 5.45:1 contrast. The new Badges and German catalogue frames also keep status copy clear of the shadow. The reviewed root headings and whole props remain readable and intact. No new P1/P2 was found within these fourteen frames. Some long German destination captions use an ellipsis, a minor remaining presentation limitation rather than evidence that the accessible full label or destination is missing.

The entire 50-file E native renderer gallery was independently compared with the exact C originals already visually inspected. Every whole PNG hash matches; `c-to-e-renderer-pixel-equality.json` records each pair. This transfers the prior visual inspection of all 144 complete looks, 24 isolated garments and 24 free-treatment previews without pretending those separate renderer probes are application UI or ownership evidence. The minor Jelli edge and limits of the ornamental free treatments remain documented. This result does not replace the separate compact/six-language, U1–U1000, performance or Android audio qualifications.

Five additional actual compact frames were then inspected: German/Italian targeted language regression, German Collection, Italian Profile and French Home. The two language captures show the expected selected language without the prior relocated Italian target. The compact Collection does reveal a P2: the bottom-left Home prop crosses the beginning of `Meine Begleiter`, and its target shares some area with the companion destination. Centers remain distinct, so a center-touch route can pass while the overlap persists. The source places the companion row at `viewportH*.76` with caption `+49`, while the sibling Home is at `viewportH-78`. A compact-specific upper bound for the row would reserve a clear bottom band for sibling navigation. This was reported to the integrator and remains a reservation against unqualified compact visual acceptance; no frozen app code was changed by this reviewer. The E review now records 19 actual UI PNGs.

## Candidate F responsive composition and fresh portrait findings

The exact F production APK is `fe556ae6f0420f41afb7e5db044ef925fe79d0695f0ac6190d6870daf2e8d5a8`, version 1.17.0-rc4/code22. Twenty actual root frames were directly inspected: four normal English roots, eight 320dp English/German roots at font 1.0 and eight at font 1.3. `visual-candidate-f.json` records their complete original PNG hashes, plus two actual fresh-player Tula/Moonlight equipped-result and cold-Profile frames.

The Collection and Atelier P2 defects are corrected in these F pixels. Companion/equipment labels have their own space above sibling navigation. The plant remains visibly below `Furniture ·42` / `Möbel ·42`, and material destinations use separate horizontal lanes. The large companion, sofa and furniture sizes are retained. Atelier captions stay readable against the floor with the light text shadow; conservative caption rectangles are not mistaken for actual opaque glyphs. No new P1/P2 was found in the 20 root frames. Long captions still truncate, particularly at font 1.3, which is recorded as a presentation limitation rather than silently described as full text.

The fresh Tula/Moonlight result and cold Profile retain the same hat, separate gloves and two boots. The shell remains a distinct silhouette behind the garment. These are actual fresh-player UI outcomes, not a renderer ownership simulation.

The corrected compact secondary preflight subsequently passed 205 functional checks and produced 298 actual images. Twenty-four of those originals were directly inspected: English/German examples of Shop Powers/Looks/Gold, guest Commerce/Vault, Wardrobe hats, furniture catalogue and Settings, including top/middle/bottom scroll positions. No new P1/P2 was found. Costs, preview information, disabled actions and long explanatory text remain readable; the old wood-under-status defect stays absent. Whole source props remain recognizable at these real compact sizes. The F visual record now contains 46 actual UI images. This scoped visual pass does not claim review of all 298 secondary images, full six-language secondary completion, Android digital audio, full 1000-level replay or the remaining release qualification.

Twelve further originals from the final six-language normal/compact routes were reviewed: Adventure EN, furnished Home FR, Collection DE, Profile IT, companion probabilities ES and signature banner PT in both sizes. No additional P1/P2 was found. The formal F record now contains 58 directly reviewed application frames. Independently recomputed whole-file comparison of all 50 F renderer PNGs with C is recorded in `c-to-f-renderer-pixel-equality.json`; every hash matches, transferring the complete 144-look/24-garment/24-treatment review to the separately executed F gallery.

## Final F native digital audio findings

`audio-native-final-f.json` summarizes the exact F native run: 38 checks and 8 captures, with 130 original AudioFlinger dumps. Four scene music counters advance by 22,991–23,491 frames per measured interval; the latest PID log identifies each expected place. HOME makes all target tracks inactive, then the same Atelier track resumes and advances. Effects-only sampling has short-track activity and no music across 38 snapshots; both-off has neither across 39; music-only has advancing music and no short-track activity across 39. Android media volume is 5 and unmuted. French and the two original On settings are restored through actual UI. The observer is removed, target stopped and installed F hash verified before handing 5586 back to the replay owner.

The first 240-second observer timeout remains a failed attempt. Its four scene pairs and HOME dump were preserved in `audio/native/candidate-f-audio/partial-native`, with exact hashes and attribution limits in `audio-observer-timeout-triage.json`. The next source call after the HOME dump was `Instrumentation.startActivitySync`; replacing it with asynchronous Activity launch plus a bounded foreground wait allowed the same frozen F to finish. No thread stack was captured, so the specific wait-monitor attribution is an inference. The retry explicitly recovers the documented original UI baseline before recording its own initial state, then completes the full route. No target app source or save was edited to make it pass.

This is digital Android playback and UI evidence, not physical audition: the emulator has no host audio output. It does not establish preferred timbre, speaker quality, Bluetooth latency, native puzzle audio playback or behavior against a real external audio-focus contender. Host PCM/lifecycle-model coverage remains separate.

## Final F transition video findings

`motion-f/review.json` records the original MP4 (`1b0d6bb4ffb77ba2743cb78f76f5de26fef12e7ebceee4af13e9ea6a9ae71182`), the SDK route timeline, stream metadata and 28 independently viewed decoded frames. This is the passing second native recording on the isolated 5592 task clone, with exact F identity and 23 native checks. The earlier recording's post-recording permission failure remains preserved separately. The new ANALYSIS PNGs retain full decoded frame dimensions and content; they are not UiAutomation screenshot originals and must not be used as website or marketing screenshots.

All three sampled routes reach the correct destination: Adventure → Home, Home → Collection, then Collection → Home. The incoming scene uses a light fade with a short horizontal movement, and the outgoing heading disappears in the settled samples. The large house objects and Collection portrait remain intact. No new P1/P2 composition defect, blank frame or persistent outgoing layer is visible in these samples. The video supplies no audio stream.

Temporal acceptance remains limited. The 1080 × 2340 H264 stream lasts 8.047722 seconds and contains 110 encoded video frames, averaging about 13.67 frames/s. Gaps adjacent to the three route changes are 690.622, 385.778 and 181.622 ms. These gaps prevent a claim of smooth 220 ms animation, 60 fps or physical-device fluidity. The observer's route windows include accessibility lookup, touchscreen events and waits, so they are not animation timing measurements. No trace isolates app loading/rendering from emulator scheduling or recording/encoder delays. The actual transition appearance passes this scoped visual review; high-frame-rate fluency is not established by this MP4.
