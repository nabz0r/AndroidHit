# Native SDK UI pilot — 1.7

The Android SDK framework runner is in [scripts/v17-ui-test](../../../scripts/v17-ui-test/README.md). It controls the actual application's `AccessibilityNodeProvider` through public `UiAutomation` actions and records original native screenshots. It does not use manual coordinate input, a desktop/browser harness, app test hooks, reflection or preference injection.

These pilot results bind specifically to installed **Lab 1.7.0-sandbox / code8**, APK SHA-256:

```
05c5ba485a0f992f5004e15c4c5853cee3c5e4942f57fc710df3156f4f75dfd3
```

Runtime: **Android API36, arm64-v8a, 16KB page-size kernel**, emulator-5580, 1080×2340, density440. They precede the later release UI margin, landscape and accessibility-event fixes and must not be relabelled as final-release evidence.

| Pilot | Result | Actual scope |
|---|---|---|
| [Menus, map, languages](ui-sdk-pilot-menus5/host-report.json) | **25 checks passed**, 24 unique original PNGs | Real daily gift +40 coins, repeat +0; four equipped starter wardrobe slots with preview/equip at no cost; shop's earned versus closed premium currency; map jump1/13/100/101/1000 plus native accessibility scrolling; actual FR/EN menu screenshots |
| [Gameplay](ui-sdk-pilot-gameplay1/host-report.json) | **26 checks passed**, 7 original PNGs | Public J13 challenge imported and cleared with no reward/stock use; J1–12 genuinely played through UI actions; normal J13 unlocked, exact next safe hint4, one charge/no move spent, repeat while active consumes none; legal bad branch0,1,2,3,8 causes a disclosed gate dead end; native undo/restart repairs it, and normal J13 clears inside its action allowance |

Both wrappers recorded **`restored:true`** after stopping the application, restoring every original private preference file byte for byte, deleting newly created preference files, and verifying the complete file set and hashes. Backup contents remain outside the repository and are not publication assets. No Google login, ad view, paid purchase or live social exchange occurred. Progress used to reach J13 was earned by actual UI actions and then restored, rather than injected into a profile.

Early harness calibration attempts exposed two test expectations that needed correction: the button is “Collect”, and native Android dialog buttons expose uppercase labels. They also revealed stale accessibility labels after a virtual action: the pilot did not emit the content-change event used by normal touch handling. The runner requests a fresh public SDK accessibility cache before each tree query, and the application task independently added proper action/scroll events for the later build. Those calibration attempts were not successful behavioral runs and are not counted in the results above.

The later extended runner additionally supports unowned wardrobe previews, the honest signed-out social screen, and a cosmetic capsule purchased solely with existing earned coins. Those extra cases require a separately recorded final run; this pilot record does not claim they passed.

## Published evidence selection

The reports above retain every captured PNG hash and all behavioral results. Superseded pilot image files are retained locally outside the repository (`/tmp/luma-v17-superseded-ui`), rather than duplicating 28MB of earlier UI in the public release. The public screenshot directory contains the complete final passing runs identified in [SDK_UI_FINAL.md](SDK_UI_FINAL.md), plus the failed final oracle capture. No historical published evidence or APK has been replaced.
