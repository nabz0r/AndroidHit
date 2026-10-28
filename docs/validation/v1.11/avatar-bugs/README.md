# Avatar composition audit — 1.11 candidate

The renderer receives the selected character, style and loadout. This audit did **not** reproduce two different characters or equipment variants producing identical final pixels. It reproduced a weaker distinction when all four clothes and three equipment slots are composed: clothing covers much of the character, and small equipment competes with the style's own hat or visor.

The production change is confined to `CompanionPortrait.java`: move and slightly reduce the four clothing overlays, move the head ornament and charm to their own side of the portrait, and enlarge the aura. The upper silhouette stays visible. Public signatures, the `2r` art extent, IDs, ownership, prices, catalog drawings and `AvatarStage` are unchanged. All art remains original code-native drawing.

## Evidence and limits

These PNGs are an **independent Java2D adaptation of the existing Android Canvas drawing source**. They evaluate transformed geometry, alpha and final occlusion. They are **not native Android screenshots**, device layout, frame-rate, TalkBack, save-state, ownership or human recognition tests. The accompanying command recorder is a separate API regression check; passing it does not establish visual acceptance.

The baseline source is pinned to commit `10c78c7cfead1c20b7fb2c7636273c21c11d2f56`, before this renderer patch. Both atlases show all 24 characters, fully dressed, in three styles. CSV comparisons cover all 12 styles at raster radii 18 and 32 pixels. These radii are host drawing sizes, not a claim about physical Android density.

| Check | Baseline | Candidate |
| --- | ---: | ---: |
| Fully dressed character pairs | 6,624 | 6,624 |
| Pairs with identical final pixels | 0 | 0 |
| Median dressed/bare difference ratio, r18 | 0.590 | 0.857 |
| Minimum dressed/bare difference ratio, r18 | 0.294 | 0.441 |
| Median dressed/bare difference ratio, r32 | 0.591 | 0.860 |
| Minimum dressed/bare difference ratio, r32 | 0.288 | 0.417 |
| Equipment swaps | 8,640 | 8,640 |
| Swaps with identical final pixels | 0 | 0 |
| Minimum changed head-ornament pixels, r18 | 42 | 83 |
| Minimum changed aura pixels, r18 | 38 | 172 |
| Minimum changed charm pixels, r18 | 29 | 70 |

“Dressed/bare difference ratio” divides the number of changed final pixels between two dressed characters by the changed pixels between those same bare characters. A pixel differs when its largest ARGB channel difference is at least 24. The ratio is an occlusion diagnostic, **not** a percentage of users recognizing the character. Minimum final character differences increase from 96 to 105 pixels at r18. Differences in the alpha outline also remain nonzero for every compared character pair.

Compare [baseline atlas](baseline-fully-dressed-atlas.png) and [candidate atlas](candidate-fully-dressed-atlas.png). The [comparison](comparison.json), [baseline summary](baseline-summary.json), [candidate summary](candidate-summary.json) and the four CSVs retain the measured counts rather than just a selected illustration.

## Contracts

- [Raster contract](raster-contract.json): **52,080 assertions pass**, over 8,640 complete compositions (24 characters × 12 styles × 6 matching clothing/equipment variant sets × 5 poses). Reduced-motion output is identical at timestamps 0 and 98,457. The measured foreground extent is at most **1.9375r**, within declared `2r`. Moving output at time 1,100 is also checked. This is six complete sets, not every possible cross-product of the seven slots.
- All **240** independent clothing swaps (12 styles × 4 slots × 5 alternative variants) leave changed final pixels after compositing; minimum **113** at r18.
- [Command regression](command-regression/host-validation.json): **6,544 assertions pass**, including the pre-existing six-language `V110Strings` contracts. This retains recording coverage without treating draw-call changes as proof of readable pixels.
- [V111Strings](v111-strings.json): **249 assertions pass**, across all 4 keys and 6 languages. Checks cover formatted names/counts; literal `%`, `$` and accents; regional codes; null/`auto` device language; unsupported-language English fallback; unknown keys; missing and wrongly typed formatting arguments. This is formatting/selection validation, not a native-speaker or screen-reader review.
- [Android API compile](android-api-compile.txt): standalone compilation succeeds against the real Android 36 API. No app package, install or emulator is created by these scripts.

The Java2D adapter explicitly models `Paint.setColor` alpha replacement and source-over composition. It supports only the primitives used by this renderer; non-null shaders fail instead of being silently ignored. Its font labels use the host font and are not app UI evidence.

## Reproduce

From the repository root, with JDK 21 and the Android 36 SDK installed:

```sh
./docs/validation/v1.11/avatar-bugs/run-raster.sh baseline
./docs/validation/v1.11/avatar-bugs/run-raster.sh candidate
./docs/validation/v1.11/avatar-bugs/run-raster.sh contract
python3 docs/validation/v1.11/avatar-bugs/compare-raster.py
./docs/validation/v1.11/avatar-bugs/run-api-command.sh
```

The baseline mode extracts pinned source into a temporary directory and never changes the working source. Its [reproduction record](baseline-reproduction.json) confirms that the rerun of all four baseline outputs was byte-identical. Candidate and contract modes compile current source. The API/command script reads the immutable 1.10 command-test source but writes only this 1.11 directory. All temporary class files are removed on exit. Set `JAVA_HOME` or use the local JDK fallback stated in the scripts; set `ANDROID_HOME` if the SDK is outside its standard macOS location.

## Integration review

Read-only review of the accompanying `LumaView` and `WorldBackdrop` diff found that successful equip feedback now follows the boolean result, save failures interrupt the action, and navigating to the equipped wardrobe preserves return history. The theme background is used on the garden and after the play-style background. The offer-reopen callback was flagged for an action-generation guard; the parent added it before the final build. These statements describe source review, not device execution.

Parent-owned native runs and final artifact manifests qualify the eventual app binary separately. The [source manifest](source-manifest.json) records baseline/current dependencies and hashes every evidence file. This folder does not declare release, Play, advertising, purchase or deployment status.
