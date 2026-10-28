# Original shop illustrations: host raster evidence

The nine glyphs are original code-native drawings from `ui/ShopIllustrations.java`: six powers, an earned amber leaf coin, a violet/cyan premium diamond, and a cosmetic capsule. The [atlas](shop-art-atlas.png) is a host-rendered contact sheet in that order. No external image, brand artwork, downloaded asset, bitmap dependency, or image generator was used.

[438 checks passed](raster-result.json) at radii 14, 20, 28, 40 and 64. The test checks visible output, bounded allocation (1.18 × radius, including anti-aliasing), balanced canvas transforms, exact reduced-motion invariance at distant timestamps, a visible response to the supplied clock, and distinct alpha silhouettes for all 36 glyph pairs. The minimum pair difference at radius 40 is 1,043 pixels. Header decoration is also checked for bounded output and reduced-motion behavior. These checks establish geometry and rendering properties; they do not measure human recognition or player preference.

The Java2D adapter is copied from the repository's independent [1.11 avatar raster adapter](../../v1.11/avatar-bugs/host/android/graphics/), with a new minimal `RectF` value adapter. It executes the original Java drawing code using Java2D paths and actual compositing. These are **host pixels, not Android screenshots**. No app data, economy state, provider, or emulator is accessed. The atlas was visually inspected for silhouette, contrast and small decorative details.

`SweetUi.java`, `ShopIllustrations.java` and `Direction.java` also compiled against the official local Android 36 `android.jar` using JDK 21. [Source inputs](source-inputs.json) record the exact two changed UI source hashes at the time of the recorded check. Shared paper surfaces, buttons and navigation were simplified; gameplay tile, gate and key methods were unchanged.

Run `./docs/validation/v1.12/art/run.sh /tmp/luma-v112-art-rerun` from the repository root to compile and re-run the raster test. The script writes to the supplied output directory and leaves this recorded evidence untouched. It requires JDK 21 (the existing local task JDK is the fallback) and no additional libraries.
