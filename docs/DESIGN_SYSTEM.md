# Luma Drift design system

## 1.17 — an illustrated world with named touch targets

Four root places—Adventure, Home, Collection and Workshop—fill the scene. Their primary navigation is an illustrated object: portal, book, house entrance, mirror, capsule machine, wardrobe rail or blueprint. Object art and a short free caption replace visible button faces, pills, chips, tabs and a bottom navigation bar. Do not reintroduce a rectangle or pill around a caption and call it an object. Ordinary web links, language controls and native audio players on the website remain standard accessible web controls.

The companion and room are the composition's subjects. Keep central portraits large enough to read eyes, silhouette and equipped fabric; use decoration to frame them rather than create empty margins. Home preserves its close-up, whole-room, pan and pinch views. Clothing, furniture and room entrances retain distinct silhouettes. Physical targets remain large and labelled; removing visible buttons does not remove semantic actions, keyboard activation or accessibility names.

`ScenicUi` supplies original object art and captioned actions without generic button faces. Deeper choices use objects, rails and dials. Acquisition, equipment, inventory state, costs, odds and affirmative/cancel meanings remain explicit. A preview grants nothing, repeatable receipt reads do not spend, and drawing cannot mutate the store. Forms use a visible native text field and keyboard Done/Enter. Destructive or financial actions still require their explicit confirmation meaning even when rendered as scene objects.

Scene motion uses a bounded 220 ms crossfade driven by `SceneTransition`. Touch, keyboard and accessibility actions are guarded during the presentation; a touch begun during a transition is consumed through its ending. Reduced motion and lifecycle changes finish immediately. Optional root swipes have visible destination-object alternatives. Home pan, furniture editing, puzzle pieces, dialogs and vertical scrolling own their existing gestures before a background root swipe can begin. [Exact routing and input contract](design/v1.17/navigation-spec.md).

Use cream, plum and warm gold with clear silhouette separation. Scene text and costs require a quiet local reading area; do not place pale captions over equally bright scenery. Original raster sprites provide the character's authored colour, with code-native style accents and motion layered coherently. Ten biomes and five audio contexts supply atmosphere without encoding essential rules only in sound or hue.

[Primary-source research](design/v1.17/reference-research.md) records the observed methods and the limits of inference. The [inspected E visual scope](validation/v1.17/review/visual-candidate-e.json) passes with documented limits and closes C's wardrobe status contrast finding from a sampled 1.93:1 to 5.45:1. The compact German/Italian hit regression, normal six-language browsing and real owned-look routes pass separately. Full compact/secondary navigation, remaining gesture/lifecycle routes and installed sound keep their own pending gates. [B rejection](validation/v1.17/review/visual-candidate-b.json), [C contrast reservation](validation/v1.17/review/visual-candidate-c.json) and [raw C compact failure](validation/v1.17/native-interface/candidate-c-sixlangs-compact/browse/instrumentation.txt) remain preserved. [Qualification](validation/v1.17/README.md).

## Preserved design history

Earlier button, pill and vector-renderer prescriptions below describe their dated editions. The1.17 object-navigation contract takes precedence for current game work.

## 1.12 atelier direction

Use open illustrated compositions instead of nesting panels around every group. Surfaces have one soft fill, buttons use pill outlines and decorative swashes have curved ends. Amber round tokens identify LumaCoins; violet/cyan diamonds identify LumaGold, with complete names and honest balance state. `ShopIllustrations` supplies six original friendly power identities shared by shop, HUD, rewards and inventory; `KawaiiArt` retains the clothing system.

Item details are normal navigable pages. Show effect, stock, ownership and price together; preview does not spend or equip. Cached premium funds are display-only and labelled last-known. Decorative motion is bounded and freezes in reduced motion. [Art proof](validation/v1.12/art-rc2/README.md) · [Research](research/ux-v112.md) · [Release](RELEASE_1_12.md).

## 1.11 visibility corrections

Keep the companion face/silhouette readable under four clothing slots, give the three equipment overlays separate space within the existing2r extent, name the actual worn objects and return to the portrait after applying them. The selected theme has a restrained scene wash while cards/text retain their readable light surfaces. Preview, acquisition and Equip remain separate explicit actions. [Renderer audit](validation/v1.11/avatar-bugs/README.md) · [Native scope](validation/v1.11/ui-bugs/README.md).

The cream/lilac/plum visual family remains. [1.9 UX](UX_1_9.md) adds an action-preview modal anchored to the visible window, explicit reward/sample states, visibly stacked boss pieces with layer counts, footprint-marked one-cell movement and the [288 original companion combinations](validation/v1.9/avatar/README.md). Animation changes art only, preserves fixed controls and follows actual progress; reduced motion disables the movement. The retained 1.8 foundation follows.

[Current UX contract](UX_1_8.md) · [Native capture provenance](RELEASE_1_8.md) · [Original illustration prompt and hash](art/dream-glade-v18.json).

## Palette and hierarchy

The game and Firebase pages share cream/lilac paper surfaces, dark plum typography, peach primary actions and mint progress. Android theme IDs and ownership remain stable; their six palettes were refreshed. Primary content is opaque enough to read over the original Dream Glade environment. Native text rendering darkens light label colors; body text has a physical size floor with support up to the existing 1.3 font preference. This is not a claim that every native pixel pair or accessibility mode has been certified.

| Role | Current treatment |
| --- | --- |
| Text / secondary | Plum `#463252` / muted plum, separate from white artwork highlights |
| Main action | Peach soft-volume button, dark label, short press feedback |
| Standard surface | Warm paper or pale lilac, flat soft fill with no stacked bevel |
| Actual progress | Mint bar with measured completion, never a fabricated near miss |
| Arrow direction | Up mint; Right peach; Down lilac; Left blue; arrow shapes remain essential |
| Key / seal | Gold key plus matching identifier; a lock is a fixed cell, not a movable piece |
| Gate preview | Current state plus immediate opens/closes markers and an explicit confirmation |

The direction palette is the same in `LumaView` and `SweetUi`. No meaning relies solely on hue or sound.

## Shape, composition and motion

`SweetUi` draws plush arrow pieces, an original expressive star guide, an illustrated moving fingertip, gate controls, progress and soft surfaces. `ShopIllustrations` supplies the six power silhouettes; `KawaiiArt` supplies the clothing drawings. Canvas helper drawing reuses its Paint/Path/RectF objects. `WorldBackdrop` decodes one RGB565 image and reuses it with ten gentle biome tints; this is one original scene, not ten separately painted scenes.

The current adventure is the home focus. Session status is explicit, with direct map/events/inventory/shop navigation. Details such as acquisition sources and cost confirmation remain adjacent to the action. The tutorial is skippable, replayable and self-paced; small landscape windows use two columns. Long menus scroll rather than scaling the entire UI down to fit.

Reduced motion removes demonstration travel, bobbing and pulse motion while retaining static instruction and emphasis. Android's disabled animator setting is respected. Music and effects have separate controls; app background/foreground audio is separately observed. No essential interaction waits for an animation to finish.

## Provenance and review

Dream Glade was generated through the built-in image generation tool, without franchise imagery, reference screenshots or image edits. Exact prompt, dimensions, copies and SHA-256 are [recorded](art/dream-glade-v18.json). Native screenshots are unedited Android SDK captures from an identified installed APK. Website frames and promotional SVGs are compositions, not Android execution evidence.

The old night/Atlas direction, earlier screenshots and delivered packages remain in historical release records [1.7](RELEASE_1_7.md), [1.6](RELEASE_1_6.md) and [1.5](RELEASE_1_5.md). They are not relabelled as 1.8 tests. Physical-phone comfort, color-vision review, TalkBack reading order and full large-text coverage still need human evaluation; passing model and screenshot checks does not substitute for it.
