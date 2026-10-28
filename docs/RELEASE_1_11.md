# Luma Drift 1.11 — equipment visibility and pre-Play bug hunt

28 October 2026. Production **1.11.0-rc2**, isolated Lab **1.11.0-sandbox-rc2**, code **12**. Frozen RC2 passes the scoped native and package qualification below. No Play upload has been performed.

## Player-facing fixes

Changing a companion or equipping clothing/equipment now returns to the wardrobe portrait at the top. Each of the four clothing slots and three equipment slots shows the name actually worn. Compact clothing captions omit the repeated slot prefix and use two bounded lines for longer variant phrases; full names remain accessible in inventory and offers. The wardrobe links directly to owned companions and to the free visual styles. Buying clothing opens its owned offer with an explicit Equip action; purchase and equipment remain separate decisions.

The shared portrait gives the companion's face, ears and silhouette more room. Clothing is smaller and lower; head equipment, aura and charm are larger and more separated. Home, wardrobe, collection, gameplay and native previews use the same renderer. Catalog identities, prices, equipment effects and existing selections are preserved.

The selected theme now colours the garden and the animated gameplay background. The exact 1.10 RC3 baseline confirms 0 changed pixels in each of the observed Home/play background regions despite a successful theme purchase/equipment. Previously the garden ignored the supplied theme colour, and the style backdrop covered the theme during play. Companion identity, visual treatment, clothing, functional gear and scene theme remain separate choices.

Preview equipment callbacks now check success before playing confirmation. Capsule equipment uses the standard save-retry boundary. The accessible equipped portrait names the actual character, style and seven worn slots; read-only labels do not advertise a click action. An obsolete purchase callback cannot reopen an offer after navigation.

## Persistence defects

Seven character/theme/gear readers now settle pending local transactions before displaying ownership or equipment. Controlled disk failures reproduced inconsistent snapshots in the old code: old ownership/loadout with the newly settled coin balance, or undurable in-memory selection. Clothes already had this boundary. The correction retries the original transaction without another debit or grant.

The first profile's starting grant is now a tracked transaction too. The old constructor ignored a refused commit: a later purchase could persist while `initialized` did not, allowing a cold reconstruction to reset coins to 180. The constructor now retains the exact pending initialization and the existing guarded UI reads expose Retry while storage remains unavailable. It does not introduce an uncaught first-launch exception.

## Qualification and limits

[Work and acceptance](WORK_1_11.md) · [Store fault reproduction](validation/v1.11/store-bugs/README.md) · [Renderer raster audit](validation/v1.11/avatar-bugs/README.md) · [Final native visual audit](validation/v1.11/ui-bugs/SDK_UI_FINAL.md).

The earlier 1.10 tests checked preview/pin behaviour, persisted style IDs and selected-control colours. They did not buy/equip clothes or compare actual portrait pixels. Their historical results remain unchanged and are not retroactively presented as visual equipment acceptance. The new test earns coins through actual native level completions, buys/equips through the UI, compares original art crops and crosses a real cold-process boundary. The completed 1.10 baseline already shows actual clothing, gear, character and treatment changes and a durable cold restart; it does not reproduce a normal-save selection loss or a total renderer cache failure. Native theme absence and poor composition/feedback are separate findings.

Host tests establish 72 normal acquisition/equipment identities, controlled transaction recovery, six-language copy formatting and bounded renderer geometry. Raster difference counts are not human recognition rates. [Exact artifact identities](validation/v1.11/candidate.json) and [package verification](validation/v1.11/binaries-rc2/binary-final-candidate.json) are recorded. Native outcomes below belong to these exact frozen bytes.

| Executed evidence | Result and scope |
| --- | --- |
| [Installed Lab RC2](validation/v1.11/ui-bugs/ui-sdk-final-rc2-lab50/host-report.json) | **1,589 checks**, 50 real Journey clears,115 original PNGs + 19 labelled unscaled analysis crops. Tutorial, menus, map, recovery, six powers/rewards/styles, guardians, six-language routes, actual purchases/equipment and separate cold process. Original guest files and rotation restored. |
| [Installed production RC2](validation/v1.11/ui-bugs/ui-sdk-final-rc2-production40/host-report.json) | **935 checks**, 40 real Journey clears,41 original PNGs + 19 analysis crops. Genuine earned-coin purchases, equipment/style/theme changes, menus/gestures/recovery and separate cold process. The actual newly created guest baseline is restored. No provider action. |
| [Fresh RC2 guest K](validation/v1.11/fresh-install-rc2/README.md) | **83 tutorial + 45 avatar + 36 language-route checks**, 47 original PNGs. Lab and production absent before first install; actual initial 180 coins/3 Hint/3 Vision/2 Flow observed on disk before practice, then exactly one +100/+2 welcome gift. Complete compact names observed in EN/FR. |
| [First production startup/audio](validation/v1.11/production-startup-k/README.md) | **7 checks / 1 original PNG**. Actual production install, active mixed audio frames advance and pause/resume with lifecycle. Lab/K preferences remain unchanged. Digital playback is not a physical speaker audition. |
| [Host store/failure regressions](validation/v1.11/store-bugs/regression-summary.json) | 683 appearance + 41 initialization + 17 external-ownership checks; existing store/objective/inventory/reward/save, tutorial, budget, all 1,240 strategy boards and 100 guardian finite-state regressions pass. These are model assertions, not human play. |
| [Build and packages](validation/v1.11/binaries-rc2/binary-final-candidate.json) | Both signed APK/AAB flavors pass signatures, pinned official bundletool and 16KiB ZIP/native alignment; lint 0 errors / 11 warnings each. All 41 scoped engine/catalog/onboarding/support sources remain unchanged. |

The same native art method finds **0 changed scene pixels in baseline 1.10** and **26,576 in each corrected Home/play region in Lab**. Closing a preview or buying clothing without Equip changes 0 art pixels; actual clothing, Lantern Leaf, Miko and free-treatment equipment each change the portrait. The final portrait survives the cold process with 0 changed pixels and the same durable identities/coin balance. Independent PNG analysis checks exact crops and native difference counts; this is visible-change evidence, not a recognition or enjoyment rate.

The puzzle rules and catalogs are unchanged. Human difficulty, enjoyment and physical-phone performance remain separate work, including the known L22→L23 and Guardian4→5 progression jumps. Google sign-in still does not synchronize device-local gameplay. Test ads remain forced, the wallet purchase URL remains empty and paid cosmetic draws remain disabled. No live provider, payment, Firebase deployment or Play release is implied by this bug hunt.

## Signed candidate files

| Channel | APK | AAB |
| --- | --- | --- |
| Production | [1.11.0-rc2](../releases/luma-drift-1.11.0-rc2.apk) | [1.11.0-rc2](../releases/luma-drift-1.11.0-rc2.aab) |
| Isolated Lab | [1.11.0-sandbox-rc2](../releases/luma-drift-1.11.0-sandbox-rc2.apk) | [1.11.0-sandbox-rc2](../releases/luma-drift-1.11.0-sandbox-rc2.aab) |

Keep the existing installed app and its local data when updating production. Lab has a separate package/profile. Both retain certificate SHA-256 `dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287`. The [candidate record](validation/v1.11/candidate.json) gives full file sizes, hashes, source inputs and compiled gates. The website remains on its separately recorded 1.10 deployment; this app-only bug hunt does not imply a new Hosting deployment.

Published on `main` in [source commit aef6f991](https://github.com/nabz0r/AndroidHit/commit/aef6f991caf34c7f3822d5080bc449fccda977f3), tree `c405ee48a62e059ceb092f62f6330b96f5e82de8`. Independent downloads of all four pinned artifacts return HTTP 200 with the exact candidate sizes and hashes. [GitHub publication receipt](validation/v1.11/publication.json). No Play upload accompanies this delivery.

## Intermediate retention

RC1 was an unpublished qualification build. Its four original binaries are preserved locally in ignored build storage, with full sizes/hashes in the [retention record](validation/v1.11/intermediate-retention.json). The two exact RC1 source inputs changed by RC2 are preserved and hash-checked against the original manifest; all other inputs match RC2. RC1 fresh-install/follow-up results remain identified as RC1. The four public downloads above are RC2 only; no older published APK/AAB is removed or rewritten.
