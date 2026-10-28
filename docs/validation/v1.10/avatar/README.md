# Companion portrait and copy — 1.10 source validation

This folder validates the new shared portrait renderer and its 56 interface labels. It does not qualify an APK or claim that the integrated screens have been inspected on Android.

## Integration contract

`CompanionPortrait.draw(Canvas, CharacterItem, x, y, r, Style, Pose, time, reduceMotion, AvatarLoadout, List<CosmeticCatalog.Item>)` draws a visual snapshot supplied by the caller. Create one renderer per UI thread and reuse it. Reserve approximately `CompanionPortrait.ART_EXTENT * r` around its centre for the dressed illustration; the caller owns clipping, safe areas and hit targets.

The layers, back to front, are the equipment aura, the selected `AvatarStage` character and free visual style, dress, shoes, gloves, hat, head ornament and charm. This keeps all four wardrobe slots and all three existing equipment slots visible in one portrait. The 18 equipment glyphs use original geometry, with differences in shape as well as colour. `drawGearGlyph(Canvas, Item, x, y, r)` provides the same standalone glyphs for cards and slots; its overload also accepts `time` and `reduceMotion`.

The body, clothes and equipment share one motion transform. Reduced motion freezes every layer at the same pose. The renderer reuses its drawing objects and does not create bitmaps. The command counts below are not a frame-time or allocation measurement.

The renderer does not read `GameStore`, grant items, equip items, save preferences, purchase anything or check ownership. A null loadout or cosmetic list omits that group of overlays. For a visual preview, supply an alternate loadout and append the alternate cosmetic to the list: the last valid catalog item in each cosmetic slot wins. The caller must label a preview, enforce ownership before applying it and decide whether to persist an action. The renderer never changes the supplied list or loadout.

`V110Strings.get(language, key, arguments...)` follows `Strings.normalize` and supplies EN, FR, DE, ES, PT and IT. Regional codes use their base language; unsupported languages fall back to English. Missing keys throw an exception. `keys()` is immutable. The copy distinguishes viewing an acquisition source, previewing an item and applying owned items.

## Evidence and reproduction

- Run `./docs/validation/v1.10/avatar/run-host.sh` for the recording-Canvas tests. [host-validation.json](host-validation.json) records 6,544 successful assertions, 1,440 companion/style/pose combinations, 18 gear glyphs, 24 cosmetic overrides and all 56 keys in six languages. [command-fingerprints.csv](command-fingerprints.csv) stores the actual command-stream fingerprints.
- Run `./docs/validation/v1.10/avatar/run-android-api.sh` for a standalone Java compilation against an installed Android SDK. [android-api-compile.json](android-api-compile.json) records the SDK and Java compiler used. This does not build or launch the app.
- [source-manifest.json](source-manifest.json) records source, test and host-double hashes for this check. Existing app files are dependencies; this task changes only the two new production classes and this validation folder.

The host checks use the real catalog, style and drawing source with recording replacements for Android graphics classes. They check deterministic reduced-motion drawing, balanced Canvas state, distinct combinations and gear geometry, all cosmetic overrides, duplicate-slot precedence, null/invalid-input handling and formatted translations. They do not rasterise pixels. Native screenshots, clipping at actual UI sizes, device performance, saving, purchases and player preferences require separate integrated checks.
