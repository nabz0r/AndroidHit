# Luma 1.18 companion art

This versioned set contains the 24 Luma companions and 13 requested League of Legends guests. Each character has four separately drawn evolution forms at copy levels **1, 3, 6 and 10**, each with twenty authored poses. The complete set is **148 sheets / 2,960 poses**. Rarity and copy level are separate game properties; the four visual milestones do not add four rarity tiers.

## Files and provenance

Packaged files live in `app/src/main/assets/art/v118/`:

```
<character-id>-evo01-poses.webp
<character-id>-evo01-poses.json
<character-id>-evo03-poses.webp
<character-id>-evo03-poses.json
<character-id>-evo06-poses.webp
<character-id>-evo06-poses.json
<character-id>-evo10-poses.webp
<character-id>-evo10-poses.json
```

The built-in image generator produced the raster originals. Unmodified PNG originals are retained under `originals/`; generation and repair prompts are saved beside this document. Approved layout or anatomical corrections were made by the same image generator. Superseded source material, where retained, is under `rejected/`. The previous art atlases were not overwritten.

The guest identities use the Riot Data Dragon classic splash references recorded in `references/lol/sources.json`; those JPEG references are research inputs and are not packaged in the Android assets. The rendering style follows the existing Luma companion art.

`measure-atlas.py` packages each selected original with `cwebp -lossless -z 9 -exact`. It decodes the result and compares every RGBA pixel with the original. Encoding does not resize, repaint, erase fringes, threshold alpha or recolor the artwork. The raw original and packaged WebP hashes are recorded in each manifest.

## Fixed pose contract

The drawings follow four columns and five rows, in this exact order:

| Row | Column 1 | Column 2 | Column 3 | Column 4 |
| --- | --- | --- | --- | --- |
| 1 | 0 front | 1 back | 2 left | 3 right |
| 2 | 4 walk front | 5 walk back | 6 walk left | 7 walk right |
| 3 | 8 jump up | 9 land | 10 hug | 11 crouch |
| 4 | 12 happy | 13 sad | 14 surprise | 15 think |
| 5 | 16 calm | 17 ready | 18 guard | 19 proud |

The four stances are player choices, independent of evolution. Runtime movement alternates the neutral and walk drawing of the same facing; jumping uses authored rise and landing drawings. Reduced motion retains the requested semantic pose without the extra movement transform.

## Source rectangles and coordinates

Generated atlases vary in pixel dimensions and do not have perfectly uniform cells. The runtime uses each frame's `sourceRect` and optional `sourceClip`, rather than dividing the bitmap into twenty equal rectangles. Rectangles use `[left, top, right, bottom]`, with exclusive right and bottom edges. `sourceClip` is a simple polygon of 3–256 absolute atlas-pixel points, bounded by `sourceRect`; it clips the body extraction only. Bounding rectangles may overlap, while the actual polygon domains separate neighbouring poses. `initialBodyBounds` records the initial connected component. `bodyBounds` and `bodyArea` record the alpha≥128 extent and count inside the actual extraction domain, with `bodyAlphaThreshold: 128`.

Every manifest has one fixed `pixelsPerUnit` based on the median height of its four neutral standing drawings: `standingHeightPixels / 3.15`. Every frame retains this scale, including crouching and jumping. `originX` is its body ground centre and `originY` is the row ground baseline. Convert absolute atlas pixels into the existing portrait coordinate system with:

```
localX = (pixelX - originX) / pixelsPerUnit
localY = (pixelY - originY) / pixelsPerUnit + 1.65
localWidth = pixelWidth / pixelsPerUnit
localHeight = pixelHeight / pixelsPerUnit
```

The measurement script may raise its initial connected-component analysis threshold from alpha 128 through 252 to distinguish bodies joined by faint generated fringes. `componentAlphaThreshold` records that choice. This changes only metadata analysis; all original alpha values remain intact in the packaged WebP. Early manifests without this field used 128.

The earlier `adaptive-alpha-density-gutters-v2` pass searched alpha-density valleys for a disjoint rectangular partition. Its diagnostics and snapshots remain under `metadata-before-bounds/` and `docs/validation/v1.18/frame-bounds-repair.json`. Native E review then identified small neighbouring fragments where silhouette bounding boxes overlap.

The selected extraction is `curved-alpha-gutters-selected-v1`: curved gutter polygons for all 148 sheets. On 138 sheets, the selected curve preserves every initially measured component and crosses no opaque interior pixel. Ten flagged sheets use a curve bounded to ±12 pixels around the recorded rectangular gutter, preventing a shortcut through a detached crescent or chapeau. The ten exceptions were compared visually across all twenty before/after poses. A zero component-loss count alone is not treated as anatomical proof: rejected unconstrained experiments cut Lune's detached crescents despite that count. Tiny contact ambiguities remain explicit diagnostics for native review.

`scripts/v118-art/apply-source-clips.py` merges geometry fields only, saves the preceding manifests under `metadata-before-source-clips/`, and verifies that all source bytes and nongeometry fields—including origins, scale, attachments and garment neck limits—remain identical. The selected proposals, review captures and application receipt are under `docs/validation/v1.18/source-clip-selected/`. `frameBoundsPartition` retains the recorded rectangular gutter reference; `sourceClip` defines the effective extraction.

Seam measurements alone cannot distinguish every generated fringe from anatomy. The before/after review at `docs/validation/v1.18/frame-bounds-review.html` displays the earlier difficult sheets, using the preserved old PNG when a layout was regenerated. Actual interleaved layouts were regenerated with wider transparent gutters: Orbit forms 6/10, Vela forms 6/10, Lune form 6, Jelli forms 3/6, Annie form 6, Lux form 1, Miko form 6, Soraka form 6 and Buzz forms 3/6. Native E review additionally led to isolated Buzz form 10 and Inky form 6 layout repairs, bringing this set to **15 layout repairs**. Their superseded PNGs remain under `rejected/`. Source pixels were not manually erased or repainted; rectangle and polygon clipping are atlas extraction geometry.

## Wearable attachments and validation limits

Each frame supplies nine absolute pixel anchors: `HAT`, `DRESS`, `LEFT_GLOVE`, `RIGHT_GLOVE`, `LEFT_SHOE`, `RIGHT_SHOE`, `HEAD_GEAR`, `CHARM` and `AURA`. Each anchor contains its centre, size, rotation, visibility and draw layer. Rear poses 1 and 5 place the legacy frontal dress under the authored body with `FAR_LIMB`; they do not paste a frontal bodice onto a rear view.

The G metadata uses pose-aware anatomical estimates on all 148 sheets. **Every frame has `anchorsMeasured: false` and `apparelQualified: false`**. Earlier manually inspected Nova base annotations remain in the saved preceding manifests; they do not qualify the current G estimates. Successful decoding and source preservation are different from apparel fitting, which still requires visual render proof.

Wearables are non-destructive overlays: the authored body remains intact underneath hands, shoes and garments. The source extraction polygon is applied separately to the body, so it removes neighbouring atlas fragments without erasing body pixels to make clothing fit. The original outfit can remain visible around an overlaid accessory; complete garment replacement is not claimed by these metadata flags.

The H wardrobe adds a durable presentation choice. A profile with no explicit cosmetic selection shows the companion's original costume by default. Existing explicitly saved cosmetic selections keep their previous presentation. The `wardrobe_costume_toggle` switches between the original costume and the retained outfit; it does not clear selected items, remove ownership or spend coins. Choosing a cosmetic or a signature outfit explicitly enables the retained outfit. Equipment selections and progression are preserved. The sole new durable mode key is `luma_drift_save_v1.companion_original_costume_v118` (`true` for the original costume, `false` for the retained outfit).

The companion studio's outfit toggle remains an independent preview. Opening a studio starts with the original costume, and a real Activity recreation retains that preview through its saved UI state. Previewing an outfit does not write the wardrobe mode. The three SDK wardrobe scenarios and `compare-costume-choice.py` are prepared to check original → outfit → original, both real Activity recreations and two cold processes, allowing only the exact mode key to change among all observed preferences. Preparation and compilation do not qualify the H behavior; the named native runs and their comparison are required.

The originals can contain very faint colored pixels around silhouettes. Those pixels have not been silently removed. The independent integrity audit proves source preservation and valid coordinates; it does not certify every pose's anatomy, direction, expression, clothing fit or degree of visual evolution.

## Reproduce the integrity audit

Use a Python environment with NumPy and Pillow. On the current workstation:

```sh
/Users/n/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 scripts/v118-art/audit-assets.py
```

The audit reads all expected 37 character identities from a frozen release list and compares them with `Catalog.java`. It checks all 148 original/manifest/WebP triples, actual decoded RGBA equality, hashes and sizes, strict asset names, twenty ordered distinct frame crops, rectangle/polygon bounds, fixed scale, nine attachment types and runtime geometry limits. Polygon domains must omit **zero** alpha≥128 pixels and share **zero** such pixels between poses; their enclosing rectangles may overlap. The audit also verifies that recomputed body bounds and area match each polygon's actual pixels. These analysis thresholds never modify source pixels. Missing sheets or a structural mismatch fail the audit. It writes `docs/validation/v1.18/art-integrity.json` atomically and exits nonzero on failure. A successful audit also writes `assets-manifest.json`, the deterministic list of all 296 packaged JSON/WebP files, their sizes and SHA-256 hashes. The report includes the manifest file hash and aggregate asset-tree hash for build freeze verification. Union coverage alone cannot prove that each pixel belongs to the correct pose; the report explicitly records this limit.

For the local interactive artwork gallery, refresh `gallery-manifest.json` with:

```sh
python3 scripts/v118-art/build-gallery.py
```

Serve the repository root and open `docs/art/v118/gallery.html`. This gallery is a review aid; native Android screenshots and the renderer observer under `docs/validation/v1.18/` provide separate runtime evidence.
