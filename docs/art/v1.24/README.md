# Pulse art —1.24

Original white/cyan rounded virtual-world artwork unifies the puzzle, resource counters, equipment and home. Crash Fever was a visual reference; no franchise interface image, sprite or font was copied into this pack.

The19 packaged WebP files total6,340,660bytes and provide87 registered sprites:48 furnishings,18 equipment objects,4 arrows,8 closed/open locks,6 resource icons and3 element symbols. Three portrait world backgrounds and one six-surface material atlas complete the environments. Nunito is bundled separately with its SIL OFL license.

- [Generation jobs, original paths, prompts and exact hashes](generation-jobs.json)
- [Verbatim earlier generation request inputs](generation-requests.json)
- [Read-only alpha measurements and explicit source windows](alpha-measurements.json)
- [Font source and hashes](font-provenance.json)
- [Removed obsolete runtime art](../../validation/v1.24/ui-pulse/legacy-assets-removal.json)

Built-in image generation produced the original PNGs. Standard WebP encoding preserves alpha; there was no manual painting, compositing or background-removal shortcut. Atlas windows were measured because several authored objects cross nominal grid seams. Their core silhouettes do not touch the atlas edges. Runtime aspect-preserving fits and native visual review are separate from these file checks.

The home keeps saved placement, ownership, room and equipment IDs. Only new fallback kitchen/garden layouts gain two additional free furnishings. Previously saved layouts are decoded unchanged. The default camera shows the whole room; pinch and drag reveal its details.

`package-art.py` encodes originals without replacing them. `inspect-atlas.py` only reads pixels and writes measurements. Earlier ornate arrow experiments are excluded from the packaged artwork.
