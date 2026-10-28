# Luma Drift design system

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
