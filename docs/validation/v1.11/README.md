# 1.11 pre-Play qualification

This record concerns the equipment-visibility bug hunt, not a Google Play upload or commercial activation. Exact binaries are production 1.11.0-rc2 and isolated Lab 1.11.0-sandbox-rc2, both code 12.

| Evidence | Status and scope |
| --- | --- |
| [Baseline](baseline.json) | Published 1.10 RC3 source/artifact identities; native comparison is recorded separately, including unsuccessful fixture attempts. |
| [Store and initialization](store-bugs/README.md) | 683 appearance/pending-read checks, 41 first-grant checks and 17 external-ownership lifecycle checks pass. Seventy-two catalog identities are exercised normally. Existing store/objective/inventory/economy/reward/save and engine regressions pass. Controlled JVM failures are distinct from physical disk acceptance. |
| [Renderer](avatar-bugs/README.md) | 52,080 host-raster assertions and 240 clothing swaps; 6,544 command regressions; 249 six-language formatting assertions. Before/after independent host rasters and exact algorithm/limits are retained. |
| [Compatibility](source-compatibility.json) | 41 engine/catalog/onboarding/support sources unchanged vs 1.10. No saved puzzle identity, allowance, catalog ID, price or equipment effect is migrated. |
| [Current RC2 fresh installation](fresh-install-rc2/README.md) | New owned API36/ARM64/16KiB AVD. 83 tutorial checks, 45 avatar checks and 36 six-language navigation checks pass, with 47 unique original PNGs. The initial 180/3/3/2 persistent starter state is directly observed before the tutorial; EN/FR worn names are fully readable in the recorded views. |
| [Intermediate RC1 fresh installation](fresh-install/README.md) | RC1 on a new owned API36/ARM64/16KiB AVD, Lab/production absent before installation;83 tutorial checks and15 original PNGs pass. Initial180 is visible on Home; the first disk snapshot was captured after the welcome gift and is not labelled an initial disk observation. |
| [Native visual audit](ui-bugs/README.md) | Lab RC2 passes 1,589 checks/50 clears (115 original PNGs + 19 crops); production RC2 passes 935 checks/40 clears (41 originals + 19 crops). Real earned purchases, actual portrait/background comparisons and distinct prepare/cold processes pass; original guest/rotation settings are restored. [Lab](ui-bugs/ui-sdk-final-rc2-lab50/host-report.json) · [Production](ui-bugs/ui-sdk-final-rc2-production40/host-report.json). |
| [Build/lint](build/lint-summary.json) | Both release variants build. Lint: 0 errors, 11 retained warnings per flavor. [Final build log](build/build-rc2.txt). |
| [Packages](binaries-rc2/binary-final-candidate.json) | Four APK/AAB signatures, pinned official bundletool validation, 16KiB ZIP/native alignment and stable input/artifact hashes pass. Package acceptance does not imply physical performance or Play acceptance. |

The old 1.10 wardrobe/style tests did not purchase/equip clothes or assert actual companion pixels. Their retained results qualify their original scopes only. The independent baseline and corrected tests remain separate from each other and from any fixture failure.

Test advertisements are forced, the wallet purchase origin is empty and paid cosmetic draws are disabled. No Google login, real two-account friend/gift path, real ad, real-money Play purchase/refund, public-origin website probe or physical phone is qualified by this record. Earned-coin purchases are covered by the native runs. No backend, Firebase Hosting or Play publication is changed by the app hunt.

[Final native qualification and independent visual comparisons](ui-bugs/SDK_UI_FINAL.md).

RC1 signed binaries are retained locally with [exact source/artifact identities](intermediate-retention.json); they are not current public downloads. Its raw observations are kept separate from RC2, including the compact-label defect that motivated RC2. No previous published package is changed.

Production starts on K from a genuinely absent package: [first-start/audio record](production-startup-k/README.md),7 checks/1original PNG. The following40-clear retained-profile wrapper restores that actual guest baseline. Google/ads/LumaGold actions remain prohibited. The current-byte fresh K and installed Lab/production runs are separate from the intermediate RC1/J observations and the deliberately failing1.10 scene baseline.
