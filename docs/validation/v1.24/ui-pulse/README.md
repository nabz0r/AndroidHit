# Pulse UI 1.24 — presentation contract

The original UI uses luminous white surfaces, cyan/blue control faces, coral/orange accents and rounded Nunito typography. It does not reuse Crash Fever artwork. `PulseUi124` only paints: callers keep their existing hit rectangles, accessibility nodes, transaction callbacks, clocks and saved state.

`PulseUi124.install(assets)` loads Nunito weights 400, 600, 800 and 900. `face(weight)` is queried when drawing; `panel`, `action`, `metric`, `orb`, `symbol`, `number` and `focus` draw onto a caller-owned Canvas/RectF. Bright accents belong to surfaces and sprites; `INK`, `MUTED`, `BLUE_INK`, `CORAL_INK` and `ORANGE_INK` keep small text readable on white. Font provenance and license are in `docs/art/v1.24/font-provenance.json` and `app/src/main/assets/fonts/OFL-Nunito.txt`.

`SceneActionView` remains a real Android Button for accessibility. Its consent toggle remains a real checked control. Native confirmations keep their affirmative/cancel actions and scroll when needed. A leading native drawable, such as the Google logo, replaces the generic control glyph instead of overlapping it.

The puzzle dock displays each talent family and the exact remaining/capacity snapshot. Charge is a separate value. A talent with uses left can still be unavailable because of insufficient Charge. Legacy attempts keep their decoded one-use capacity; painting does not migrate them. The contextual companion sheet explains the actual effect and cost and retains the original activation/return callbacks. Pause and restart retain their confirmation, save, energy and timer behavior. Six-language copy no longer claims every new attempt has one talent use.

`BattleDockLayout124` chooses two columns when physical targets or large text cannot fit four. `CompanionSheetLayout123` keeps its historical constructor and adds `(viewport, minimumTouch, descriptionHeight, minimumText)` with explicit metadata baselines and a scrollable content height. `PauseLayout124` keeps action targets apart on short windows. Resource sprites use `pulse/icon/{coins,gold,energy,xp,copies,charge}` through the app's art library; precise Canvas glyphs remain a fallback.

## Validation scope

`host-checks.json` records 3,282 pause/sheet/localization checks, 2,563 dock geometry checks, 48 actual six-language reward/try-on format checks, and successful Android API 36 javac of 16 presentation sources against existing compiled dependencies. These checks cover target separation, viewport/content bounds, large text metadata spacing, two/four column selection, six-language counts/costs and source compatibility of the helpers. That host receipt alone accepts no APK or native screen. The later exact-D native qualification is recorded below.

The frozen 1.23 Lab baseline remains separately documented under `docs/validation/playtest-2026-10-31/native-lab/`. It does not qualify these new source changes. The later exact-D review binds the new APK; its accepted scope is recorded in `native-retest-d.json` and the native Lab reports. Outfit-return qualification belongs to the separate production reports.

`legacy-assets-audit.json` records the read-only identification of 13 old PNG payloads totaling 35,200,036 bytes whose live painting consumers moved to 1.24. The coordinator subsequently authorized their removal: `legacy-assets-removal.json` preserves each original size/hash and the before/after Java hashes. Only those PNGs, the 42-key legacy furniture registration and its seven measured source-window cases were removed. Companions, garments, navigation art and v118 remain intact.

The historical fixtures and original evidence remain unchanged. To reproduce their exact-set/source-region checks, use a separate checkout of `5cc870701c8039e664d1c00a90d0586c3d00521c` (or their own original release commit/APK), where all 23 original PNGs and registry are available. Those original-art tests describe that historical asset set, rather than the reduced 1.24 runtime.

## Exact-D native retest

`native-retest-d.json` binds the final sandbox rc2/code33 APK and all six retained phases: 330 assertions, 53 actual touchscreen actions and 22 original PNGs. The observed home, pause/restart and companion sheet are opaque; the XP capsule stays clear of Settings. Short-window/font1.3 controls remain reachable through real scrolling. Actual GUIDE cost/use, public SDK background/recreate preservation and clock freeze/resume are checked separately from painting. The native screenshots are reviewed evidence, not a claim of human comprehension or physical-device acceptance. Historical B rejection and `alpha-correction.json` keep their original status; this later receipt records the D result.
