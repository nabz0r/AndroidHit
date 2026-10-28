# Visual identity bug audit — 1.11

The first baseline is the published 1.10 RC3 Lab on the explicitly owned API 36/ARM64/16 KiB emulator 5580. The new [SDK method](../../../../scripts/v111-ui-test/README.md) adds actual cropped raster changes and a separate cold target process to the previous state/accessibility checks. Its [1.10 source inventory](v110-framework-provenance.json) preserves provenance. All 117 copied source files and all 106 oracle JSON files retain their historical originals.

Current status: **1.11 RC2 Lab 50 and production 40 both PASS**, with independently recomputed appearance/theme raster changes and a separate cold target process. The 1.10 baseline C retains its global FAIL with two actual theme-rendering failures; [final qualification](SDK_UI_FINAL.md) records the exact scopes and frozen artifacts. The baseline earns its own progression and coins through real certified UI actions, then purchases/equips items. It restores the original guest profile afterward. The test’s full screenshot and explicitly labelled unscaled crop are different evidence types.

Confirmed prior coverage gap: old avatar tests exercised preview/pin non-mutation, not wardrobe purchases/equips. Free-style tests asserted stored IDs and labels without comparing the avatar art. The fourteen mint-pixel checks concerned tab/chapter backgrounds. Their earlier PASS remains valid for those scopes and does not establish visible equipped-item rendering.

No provider, real ad/purchase, phone, human-difficulty or retention qualification is claimed. Play/commercial activation remains paused.

The first [setup pilot](ui-sdk-baseline-rc3-a/host-report.json) stopped after 3 recorded checks before progression or purchases. The OS already disabled animations, so the visible setting was On while its explicit preference remained false. The harness now activates that real setting explicitly; the raw FAIL and successful original-profile/rotation restoration are retained. No app defect is inferred from this setup failure.

The second [baseline setup run](ui-sdk-baseline-rc3-b/host-report.json) records 2,076 checks, including the real 100-level route, then globally fails before any purchase because a native dialog exposes `450 LUMACOINS` rather than the harness’s case-sensitive price string. Its original profile and rotation settings were restored. This is a harness-selector failure; the completed recorded checks do not make the global result PASS. Its [exact source archive](baseline-runner-source/manifest.json) is retained.

The third baseline requests 40 real Journey clears, enough to finance 2090 LumaCoins of explicit clothing/companion/theme purchases from a new 180-coin guest. It additionally selects the actual Head slot and restores Moonbud through its real UI before comparing Lantern Leaf. Dialog button matching is case-insensitive. [Source archive](baseline-c-runner-source/manifest.json). The completed [raw C report](ui-sdk-baseline-rc3-c/host-report.json) records 929 checks; the two theme comparisons fail, while the separate 22-check cold phase passes.


## Measured 1.10 baseline

[Analysis record](baseline-analysis.json) and [comparison CSV](baseline-raster-comparisons.csv) are derived from the unchanged raw reports/actions. Journey 1–40 has all 120 completion/visible-three-star/certified-budget assertions passing. The 41 full-frame SDK PNGs and 19 explicitly labelled unscaled crops are separate image types; all 19 three-frame observations measured zero repeat-frame changes above RGB 8.

| Explicit action | Changed art pixels above RGB 8 |
| --- | ---: |
| Equip Hat Cupid |12,582 |
| Equip Little witch outfit |9,209 |
| Equip Cupid shoes |4,771 |
| Equip Little witch gloves |18,290 |
| Equip earned Lantern Leaf |2,328 |
| Equip companion Miko |27,815 |
| Select Retro arcade |15,584 |
| Select Lagoon: blank Home background |**0 — fails** |
| Select Lagoon: blank gameplay background |**0 — fails** |

The worn items visibly change after explicit Equip in this route. Closing previews, buying clothing without Equip and changing the theme leave the worn portrait unchanged, as intended. The completed actions spend 1750 on clothing, 160 on Miko and 180 on Lagoon from a 2380-coin wallet (180 starter gift + 2200 from 40 real first clears); the observed 290-coin balance and exact worn identity survive a real force-stop/new process (PID 6347→7336), with 0 changed art pixels. The original guest profile and rotation settings are then restored.

The theme comparisons reproduce an actual rendering defect even though the theme ID and debit are correct. No clothing ownership/persistence/render failure is reproduced by these operations. That does not resolve whether the purchase/equip flow is understandable or every item is comfortably readable; 1.11 separately improves the explicit Equip offer, applied feedback and item labels. Pixel differences are not a human-perception or enjoyment score. Baseline geometry is anchored to 1.10’s Hat node; 1.11 uses its explicit portrait node, so within-run comparisons are authoritative, not direct cross-version pixel equality.

Candidate 1.11 RC1/fresh J remains intermediate after a separately observed truncated starter label. The final RC2 [Lab 50 report](ui-sdk-final-rc2-lab50/host-report.json) passes 1,589 checks (115 original frames + 19 analysis crops); [production 40](ui-sdk-final-rc2-production40/host-report.json) passes 935 checks (41 originals + 19 analysis crops). Their 150 and 120 individual board assertions respectively cover clear, visible three stars and certified budget for every level on those routes. Both restore the original guest profile and rotation settings.

Independent PNG checks reproduce all 16 recorded comparisons per route: [Lab verification](ui-sdk-final-rc2-lab50/host-raster-verification.json) and [production verification](ui-sdk-final-rc2-production40/host-raster-verification.json). The four explicitly equipped clothes, Lantern Leaf, Miko and free Retro style change the portrait. Preview closing and buying without Equip change 0 portrait pixels; the ordinary cold relaunch also changes 0 pixels. Lagoon changes all 26,576 sampled blank-background pixels in both Home and gameplay, fixing the actual 1.10 defect in these observed routes. Each route's 19 three-frame native observations reports 0 repeat-noise pixels above RGB 8.

The separate [fresh K RC2 qualification](../fresh-install-rc2/README.md) passes 83 tutorial, 45 avatar and 36 language checks with 47 original frames; [production startup](../production-startup-k/README.md) separately passes 7 lifecycle/digital-audio checks with 1 original frame. No intermediate result is relabelled as RC2. Neither ordinary cold persistence nor these new-device observations qualify pending write failure or provider/Play readiness.
