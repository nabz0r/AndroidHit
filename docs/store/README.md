# Luma Drift · Brand kit

![Luma Drift celestial brand artwork](luma-drift-feature.png)

Original promotional artwork for Luma Drift: Nova, the game's five-point companion, surrounded by the directional tiles and switch motif. The palette follows the default **Lumina** theme. The visuals contain no player counts, ratings, revenue claims or simulated gameplay screens.

## Assets

| File | Dimensions | Use |
| --- | --- | --- |
| [Feature graphic · PNG](luma-drift-feature.png) | **1024 × 500 px** | Repository banner and proposed store feature graphic |
| [Feature graphic · SVG](luma-drift-feature.svg) | 1024 × 500 viewBox | Editable vector source |
| [Icon · PNG](luma-drift-icon.png) | **512 × 512 px** | Proposed store icon; full square canvas |
| [Icon · SVG](luma-drift-icon.svg) | 512 × 512 viewBox | Editable vector source |
| [Asset manifest](assets.json) | JSON | Dimensions, file sizes and SHA-256 hashes |

These are promotional brand assets. The actual installed-app captures are maintained separately in [screenshots](../screenshots/). This folder does not establish Play approval, listing publication or permission to use a store's badges. The app's launcher resource is maintained separately; inspect the current source and APK to confirm which launcher icon is installed.

## Art direction

| Role | Color | Source |
| --- | --- | --- |
| Celestial background | `#11142B` | Lumina theme |
| Lavender | `#A99AFF` | Lumina accent |
| Mint | `#7EE8D1` | Lumina secondary accent |
| Nova gold | `#FFE29A` | Nova companion |
| Peach tiles | `#FFB398` | In-game directional palette |
| Ink | `#14182E` | In-game eyes and arrow contrast |

Nova's ten alternating-radius vertices, round central body, eye proportions and blush were redrawn from the original `mascot()` geometry in [LumaView.java](../../app/src/main/java/com/androidhit/lumadrift/LumaView.java). Colors and character identity come from [Catalog.java](../../app/src/main/java/com/androidhit/lumadrift/data/Catalog.java). Tile arrows and switch markers reflect the game's visual language. The orbit composition, typography and layouts were created for this kit.

The feature graphic uses the existing English tagline, “A little light. A clever escape.” The artwork is not language-dependent except for the feature graphic's short English copy. No third-party illustration, photograph, icon pack or franchise material is included. Typography uses the local Arial system fonts when rasterized; font files are not bundled.

## Reproduce

The SVG generator requires Python 3's standard library. PNG export uses macOS AppKit and Swift; it does not require a browser or an online image service.

```sh
python3 docs/store/generate_assets.py
CLANG_MODULE_CACHE_PATH=/private/tmp/luma-swift-cache swift docs/store/render_assets.swift
python3 docs/store/verify_assets.py
```

The SVG files remain editable in other vector tools. Text references Arial Black / Arial with a sans-serif fallback, so another renderer can produce slightly different letter spacing. The supplied PNGs are the reviewed render, and their exact hashes are in [assets.json](assets.json).

## Verification and provenance

Created for AndroidHit on **26 October 2026**. Both PNG dimensions were inspected after export. The full feature graphic and icon were opened for visual review, and the icon was additionally checked at **64 × 64 px**. The character, arrows and title remain readable; neither PNG includes an external border or a pre-applied rounded-square icon mask.

The generator and SVG source are included so the composition and colors can be reviewed. Assets follow the repository's [license](../../LICENSE). Store acceptance and conversion performance require separate review and real testing.
