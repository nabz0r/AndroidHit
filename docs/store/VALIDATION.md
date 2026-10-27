# Brand kit verification · 27 October 2026

Five original SVG sources and their PNG exports were generated locally for the Luma Drift 1.5 presentation. A separate 64 × 64 icon preview was exported for inspection.

## Executed checks

- SVG syntax, unique IDs, local `use` references and matching viewBoxes passed. No embedded raster image, external image, script or foreign-object element is present. The wide scene has no visible text elements.
- All PNG chunk checksums and decompressed scanlines passed. Pixel depth, RGB/RGBA encoding and dimensions match [`asset-specs.json`](asset-specs.json).
- The 1024 × 500 feature is 24-bit RGB without alpha. The 512 × 512 icon is 32-bit RGBA and smaller than 1024 KB. The 640 × 640 Nova has both fully transparent and fully opaque pixels.
- Every main PNG has an opaque gold pixel at the expected Nova body position. This catches the missing-body failure found in the initial AppKit export with nested SVG references; the corrected generator keeps references one level deep.
- The generator's in-memory SVG output matches the saved sources byte for byte. Current exports and generator inputs have SHA-256 hashes in [`assets.json`](assets.json).
- All 65 local references across the repository README and the two kit documents resolve; image alternative text and the exact release counts remain present. The scoped diff whitespace check passed.
- Public homepage and APK-link HTTP HEAD requests returned 200. This records availability, not a fresh download/hash verification or publication of the new website artwork.

## Visual inspection

The repository banner, store feature, full-size icon, transparent Nova and wide scene were opened after export. The character, eyes, bud, orbit, hanging heart, arrows and matching K1 key/wall remain visible. The 64-pixel icon preview retains the character's face and directional-piece silhouette. No blocking text clipping was observed in the banner or feature graphic.

The sources were compared with the installed [1.5 lobby](../screenshots/v1.5/home-en.png) and [sealed puzzle card](../screenshots/v1.5/sharing/sealed-p6-card.png). These are visual/provenance checks, not evidence of store approval, conversion performance, a newly built APK or physical-device quality.

Reproduction uses the three commands in the [kit README](README.md#reproduce). PNG rendering was checked with macOS AppKit; SVG generation and structural verification use Python's standard library. Font spacing can differ on another platform.
