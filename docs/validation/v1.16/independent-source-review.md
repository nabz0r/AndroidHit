# Independent source review — 1.16

2026-10-29. Read-only review of the new `LumaView` routes, `GameStore` integration, player/collection strings and home camera. App changes were made by the parent implementation agent; this reviewer changed only this report. No device, emulator, binary or player data was accessed for this review.

## Findings corrected during review

| Priority | Finding | Correction verified in source |
| --- | --- | --- |
| P1 | `drawFrame` initialized the summon pool to a fixed content height of 1200 before clamping scroll, while the renderer later expanded it to more than 3000. Scrolling down the avatar list would repeatedly snap back, leaving most entries unreachable. Several other new pages also had smaller heights before clamping than after drawing. | `LumaView.java:393–398` now initializes the final heights before `clampScroll`: the pool uses its actual row count; profile, badges, banner and reveal match their renderers. Collection and Atelier heights also match. |
| P2 | The floating decoration toolbar did not capture its background. Taps in its gaps/title could select or move a furniture item behind it; a drag could start a room pan. A first geometry guard still allowed the generic touch fallback to execute an underlying furniture hit. | `LumaView.java:1186,1196–1198` calculates the toolbar limit before furniture targets and clips both target intersections to it. `homePointer` rejects that region and the pan-start condition excludes it (`:998–1000`, `:2228`). Visible toolbar buttons retain their own targets. |
| P2 | A refused nickname save left the native dialog and keyboard covering the global save-retry panel. | The positive-button callback (`LumaView.java:1088`) catches `LocalSaveException`, dismisses the dialog, and enters the global blocked-save flow. The success notice is only issued after the store call succeeds. |
| P2 | Explicit Back buttons on the summon preview/reveal used forward navigation. They pushed the page being dismissed into history, so system Back reopened that preview and repeated visits accumulated extra entries. | `returnToMenu` (`LumaView.java:1120`) removes entries up to the intended parent, then navigates with history recording disabled. The preview/banner return buttons use it (`:1158`). |
| P2 | Restoring a compact room pan called `setCamera` while `HomeScene` still had its default 440-high viewport. A valid saved vertical pan could be clamped away before the compact viewport was installed. | `saveUiState` persists the home viewport height (`LumaView.java:210`); `restoreUiState` applies that viewport before the saved camera (`:219`). Width remains the fixed 390 logical units used by this interface. |
| P3 | New route IDs were used as accessibility pane titles instead of localized names. | `screenTitle` (`LumaView.java:778`) maps profile, badges, summon, pool and reveal to translated strings. |

Line numbers identify the reviewed source snapshot and may move with later edits. These are source corrections, not claims of successful native interactions.

## Contracts inspected

- Catalogue previews only read ownership and show art; the Equip action is conditional on ownership and `GameStore.equipSummonItem` verifies it again. Browsing a locked item does not grant ownership or spend coins.
- Summon debit, result, ownership, duplicate compensation, seed and last-reveal identity are placed in one `LocalSaveTransactions` write. Existing valid receipts replay without a second debit, and a request cannot be reused for another banner (`GameStore.java:1225–1272`).
- Reading a receipt and reopening the last reveal do not create a new draw. The UI keeps preview identity separate from paid/earned draw receipt identity.
- An outfit equips all four cosmetic slots together only after ownership of all four is verified. Existing character gear and avatar-style selections are not changed by this method (`GameStore.java:1192–1213`).
- Badge slots expose only earned, distinct IDs; moving a badge clears its previous slot atomically. Decoration awards are preserved before and after a layout change (`GameStore.java:1170–1189,1461–1465`).
- Nickname, flag and badge selection are durable local preferences. The chosen flag is validated against the supported list; it is independent of account identity or inferred location.
- The camera transform is shared by room drawing, inverse floor projection, sprite bounds and painter-order targeting. This review found no remaining separate outer fit transform in the new room renderer.

## Additional findings addressed before candidate A

- The initial `acquisitionCaption` review found no dedicated case for `EARNED_SUMMON_v116_1`. This was corrected before candidate A; acquired clothing now names the summon origin while retaining older provenance.
- The initial nickname review found invisible-only nonspacing marks such as U+034F were accepted. This was corrected before candidate A with explicit invisible-character handling and model regressions; valid accents and basic emoji remain supported.

Both refinements are complete in the frozen candidate. Native screenshots, compact-screen interaction, cold relaunch and any fault-injection tests must be reported separately by the release qualification; this document does not substitute for them.

## Reviewed source identities

```text
LumaView.java    a4623fdddf0d55e28f650e352d22119515a4a749648bbc14c9d0fddb65f247f6
HomeScene.java   ec7de8dd29f0d38b0b8e1e54d3e706468cb1d3b3717abda9db8bb3ef746c2a87
GameStore.java   5714e54fda5bff3105fa4a1c90310989b005f31e05f72d84d5a2591f03b95620
V116Strings.java 00ccda9a79d0737cd021685b4173b69398cf852384033b371aeda76124164bcf
```

`git diff --check` was clean at the end of this source review. No unresolved P1/P2 finding remained in its scope.

## Post-review refinements

The small provenance/nickname refinements listed above were completed before candidate A: earned-summon clothing now names its actual source, and invisible-only Unicode nicknames are refused (60,712 model assertions). Candidate B changes only the spacing of summon catalogue/reveal artwork and captions after native visual review; its final source identities are recorded in `candidate-b/source-freeze.json`.
