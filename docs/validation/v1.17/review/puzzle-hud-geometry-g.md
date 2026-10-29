# Candidate G: blocking legacy puzzle HUD geometry

Status: **P2 confirmed on original native G frames; release rejected pending H.** This is a source/visual diagnosis, not a new gameplay test. No application source, test or device was changed for this review.

Exact G production APK: `f85c258341082462b51c6e6ddd466edc07b17b97044d72cfa27c39d20d2cd8a8`, `1.17.0-rc5` / code23. Reviewed `LumaView.java` SHA256: `841e40dc756964e0199c8ecf34604befca65d8d4938dfb6c529bb18f3f7de384`.

## Native evidence

The original 1080×2340 PNGs are under `docs/screenshots/v1.17/native-interface/candidate-g-other-modes-v2/modes/`:

- `modes-weekly-hud-en.png`: the closed UP gate covers the central `27 / 27 moves` budget; the DOWN gate intersects the instruction; both side gates are clipped.
- `modes-daily-hud-en.png`: the closed DOWN gate intersects the instruction; both side gates are clipped. Its open UP gate does not hide the budget, demonstrating why an all-open screenshot would miss the closed-door defect.

The separate native functional report passed123 checks /37 captures: `docs/validation/v1.17/native-interface/candidate-g-other-modes-v2/host-report.json`, SHA256 `fc9c790fa94fe71cc720e9fedd0bcfadd447a85d083d519e16381841b8cdb21d`. Functional input success does not establish readable composition.

## Exact source mechanism

Coordinates below are the 390-unit canvas, before device scaling. `drawPlay` starts non-Relay boards at `by=190`, draws the budget with baseline151 and accessible label bounds `[24,131,366,156]`, then puts gate centers26 units outside each board edge. Current board width is `min(304,max(180,viewportH-410))`.

| Element | Exact extent relative to gate center |
|---|---|
| Closed manual gate | rectangle ±23, stroke1.4 adds0.7: ±23.7 |
| Gate highlight outline | rectangle ±24, stroke2: ±25 |
| Open/nonmanual SweetUi gate | bounded by about±14 after13/24 scale, including animated highlight |
| Gate hit target | ±25;50×50 logical regardless of density |
| Trail step badge | x+9 through x+29; y−24 through y−4 |
| Preview check/lock | outward15 plus a size16 icon; contained by the above conservative±25 envelope |

For W304 and by190:

- UP center `(195,164)`; manual visual y140.3..187.7, target y139..189. Budget label y131..156 intersects it. This occurs whenever the upper gate is closed in a manual-gate board, independent of board size/count.
- DOWN center y520; visual y496.3..543.7, target y495..545. `twoLines` starts its text layout at535 (`by+W+41`), so the closed gate overlaps the first8.7 logical units of that text. **This argument is a layout top, not a baseline.**
- LEFT center x17, target x−8..42, visual x−6.7..40.7. RIGHT center x373, target x348..398, visual x349.3..396.7. A right Trail badge can extend through402. The viewport is0..390.

`hit` clips these targets, and deliberately excludes play/guardian/tutorial/firstlight from menu target enlargement. On the normal440dpi/1080px screen,50 logical units are≈50.35dp. At320dp width they are≈41.03dp. A48dp target at320dp requires58.5 logical units. This gate-target issue is distinct from the exact cell targets deliberately retained for dense puzzle tiles.

## Catalogue reach and maximum extents

Manual gates are enabled exactly for `Level.progressionVersion`4 or5 (`Board.strategy != null`), therefore L/M/T and N. All other ordinary boards still draw gates and register50×50 gate hit targets, so the hit-extent clipping rule must apply consistently to U and historical imports too. Guardian uses a separate renderer and has no four-gate rail.

The following inventory reads the frozen lookup literals and their source mappings; it does not execute a second generator or claim further solver coverage:

| Family | Board dimensions/counts | Manual gate |
|---|---|---|
| C / B | 5×5:120;6×6:120 each | no |
| R / Q | 5×5:60;6×6:180 each | no |
| G / J / L | 5×5:90;6×6:390;7×7:520 each | L only |
| H / K / M | 5×5:90;6×6:150 each | M only |
| N | 4×4:4;5×5:86;6×6:390;7×7:520 | yes |
| U | 4×4:4;5×5:296;6×6:700 | no |
| New daily/weekly T | finite source closure L21..1000:5×5:70;6×6:390;7×7:520 | yes |
| Historical E / paired generated events | 6×6 for every seed by constructor | no |
| Sealed P | six5×5 boards | no |
| Guardians | 4×4:4;5×5:96 | separate renderer |

The collision formulas do not depend on cell count. The7×7 legacy boards constrain how much the puzzle can be shrunk while retaining useful piece detail; evaluate an actual7×7 H frame instead of only a4×4 introductory board. At264 logical width, a7×7 cell is37.71 logical units (≈30.94dp at320dp). Do not claim48dp puzzle cells.

## Minimal shared geometry contract for H

Use a small Android-independent geometry helper consumed by the real renderer and hit registration. Accept viewport height, measured budget/feedback height, physical48dp converted to logical units, whether a budget is present, and required control/power content. Return the board rectangle, gate centers/targets, budget/feedback rectangles, actions/powers origins and content height. Measure text with the actual selected font/StaticLayout rather than duplicating Android text metrics in a host test.

Required independent invariants:

1. Let `halfTouch=max(25,48*density/scale/2)`, `rail=max(26,halfTouch+1)` and `horizontalExtent=max(halfTouch,29)`. The increased rail prevents a large gate target covering edge cells. Targets and current Trail ornaments remain entirely inside margins: `W <= 390-2*(rail+horizontalExtent+8)`:264 normal and255 at320dp. Keep this width constant across heights; use real scroll for shorter windows.
2. H moves the numeric budget to x24..156 (top131,bottom160), outside the UP target by at least8; full localized wording remains in its accessibility label. Unlimited is visibly infinity. Thus the budget no longer needs an entire separate vertical band. To clear the full manual highlight ink below progress124+8, `board.top=max(182,132+rail+25)`:183 normal /187.25 at320dp. The initial proposal of182 with an increased rail was arithmetically insufficient and was corrected before freezing H.
3. `feedback.top >= board.bottom +rail+max(25,halfTouch)+gap`. This is the **top** of the text layout. H reserves the greater of42 and the real `StaticLayout("Mg\nMg")` two-line height at the current font floor, retaining stable placement during one/two-line toasts. The actual feedback uses that same text method and width.
4. Undo/Restart start after the feedback bottom plus a gap, and their full targets do not overlap feedback, DOWN target or power targets. Compact windows use three106-unit object/caption columns in one row, with12-unit gaps; the row reserves42-unit art plus two lines and is genuinely scrollable. Larger windows keep the action pair and full power grid only when their complete final target fits, rather than selecting by `viewportH<680`.
5. `H(play)` must equal at least the largest content/control bottom plus padding. Current `drawFrame` overrides it to `max(viewportH,550)`; simply moving controls downward can leave them both clipped and unreachable because scroll has no extra extent. Calculate the content height before its scroll clamp, or explicitly support a second pass before clamping/drawing.
6. All header, modal and target coordinates remain in the same visible/content coordinate system after scrolling. Preserve help, preview, pause and save-failure gating. Do not fix this by removing budget/help or hiding a mode.

Useful tests are rectangle disjointness, all required extents inside the frame/content, real48dp gate targets, monotonic measured text growth and last-action reachability over compact/normal heights and font1.0/1.3. Feed the real helper into these tests; avoid a test that restates the production formula with the same expected arithmetic.

The implemented H helper and independent host contracts pass379,634 assertions over3,400 actual-helper layouts: `review/play-scene-layout-h/host-report.json`. The complete renderer/hitmap integration was separately source-reviewed. These are host geometry results; native H text/overlay/original-pixel acceptance is still required.

## Release gate

Before restarting a1000-level native replay, smoke actual H Remix/manual UP+DOWN closures, daily/weekly7×7, Sealed unlimited and U, in normal and320dp/font1.3. Capture budget, both sides, feedback, preview/help and the final power/share action; verify the actual native bounds and reachable scroll. Then independently review those original pixels. Guardian Back can remain accepted from G's123-check regression, but H needs its own identity-bound smoke after the renderer integration.
