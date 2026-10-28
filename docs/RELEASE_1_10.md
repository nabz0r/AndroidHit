# Luma Drift 1.10 — Companion & Objectives

28 October 2026 · production **1.10.0-rc3** / isolated Lab **1.10.0-sandbox-rc3**, version code **11**. The signed candidate, its original installed screenshots and the qualification below are retained together. The previous published 1.9 artifacts remain unchanged.

## What changes for the player

- **Home:** continue the saved attempt, see genuinely unclaimed rewards, follow one chosen collection item and open the optional Prism journal. The parent navigation destination stays selected on subpages; the title and wallet no longer overlap.
- **Companion:** one animated portrait combines the selected character/style, four cosmetic clothing slots and three existing equipment slots across Home, wardrobe, collection, play and previews. Reduced motion remains supported.
- **Wardrobe and collection:** Appearance/Equipment tabs, complete portrait, ownership/equipped/locked state, actual prices, acquisition source and progress. Previewing or pinning an item does not buy, grant or equip it. Acquisition and equipment require an explicit action.
- **Objectives:** Today, This week, Journey and Ready to collect separate available activities from earned rewards. Claims remain individual and use the existing amounts. Visit-cycle copy explains that breaks retain the cycle.
- **Events and shop:** the next event stage and the real completion bonus appear above the grid. Powers, appearances and account LumaGold have separate shop destinations. Native dialogs use the game's light pastel palette.
- **Prism journal:** ten read-only chapters use actual Journey clears and claimed guardian-fragment receipts. Guardian markers every ten levels connect the map to the journal; reading adds no reward or progression bypass.
- **Durable local saves:** completion bonuses and collection acquisition/equipment operations settle typed pending transactions before acknowledging success. Retrying a failed disk write settles the original operation without a second debit or reward.
- **Readability:** opaque selected-tab backgrounds replace seven incorrectly interpreted palette indices. Home, wardrobe, journal and collection pagination now scroll fully above the fixed menu.

[Illustrated UX and limits](UX_1_10.md) · [Story contract](PRISM_STORY_1_10.md) · [Qualification index](validation/v1.10/README.md) · [Roadmap](ROADMAP.md).

## Executed qualification on these exact bytes

| Evidence | Result and scope |
|---|---|
| Installed Lab | **2,514 passed checks / 96 original PNGs**, API36 ARM64, actual 16KiB pages. All 100 first Journey levels cleared through native SDK actions, tutorial, recovery, six powers, rewards, wardrobe, journal, map, guardians, rotation and six-language routes. Original guest preferences and rotation settings restored. |
| Genuine first install | **91 passed checks / 17 PNGs** on a new emulator where Lab and production were absent. Tutorial skipping, resuming, gate preview, double confirmation and activity recreation verified. |
| Installed production | **7 passed checks / 1 PNG**: exact APK startup and digital audio frame advance, background pause and foreground resume. Original guest preferences restored. |
| Visual assertions | **14 selected controls** sampled from original native screenshots have the expected opaque mint pixel; **four footer controls** remain entirely visible above fixed navigation. This is scoped evidence, not a universal readability claim. |
| Store/objective models | **443 dedicated assertions** plus seven store/catalog regression suites; durable failure/retry, ownership, pinning and existing reward receipts. Source models are separate from native acceptance. |
| Portrait / story / layout | **6,544 portrait command/API checks**, **308,412 story assertions**, **1,491 layout-model checks**. No FPS or human comprehension measurement is inferred. |
| Build and packages | Both signed APK/AAB flavors build; lint finishes with **0 errors and 11 retained warnings per flavor**. Official bundletool validation, signature and static 16KiB ZIP/ELF alignment pass. |
| Puzzle compatibility | **25 pre-existing engine/catalog classes byte-identical to ec57167**. This preserves puzzle rules and saved identities; it does not repeat every historical simulation or measure human difficulty. |

[Native reports](validation/v1.10/SDK_UI_FINAL.md) · [Build/lint](validation/v1.10/build/lint-summary.json) · [Binary audit](validation/v1.10/binaries/binary-final-candidate.json).

## Signed downloads

| Artifact | SHA-256 |
|---|---|
| [Production APK](../releases/luma-drift-1.10.0-rc3.apk) | `29a68cdcaeb61850d55afd101f03bd8c42a075cc6d6631205fc176e499dd70b4` |
| [Production AAB](../releases/luma-drift-1.10.0-rc3.aab) | `75396149b150b4b6bce01606ac359db858bb8e57a840f6cf5cfeab78d30d30dd` |
| [Isolated Lab APK](../releases/luma-drift-1.10.0-sandbox-rc3.apk) | `d1b085b4d9bbcf450037069ed15c7cea89b0e3e759b16e587cf3ca016a7c493f` |
| [Isolated Lab AAB](../releases/luma-drift-1.10.0-sandbox-rc3.aab) | `1f7c51e19866255bb4cb33bc76828db1dffaf9abc8f88ffbd47578abf2463247` |

Certificate SHA-256: `dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287`. Production and Lab use separate packages. RC1/RC2 were unpublished intermediate candidates: their original reports and manifests remain scoped separately, with their eight binaries preserved locally under the ignored qualification folder rather than added as public downloads. [Retention record](validation/v1.10/intermediate-retention.json).

## Delivery and remaining work

Website source, FR/EN copy and the five-image gallery follow the exact RC3 Lab captures. **Firebase Hosting 1.10 is deployed**, confirmed at 28 October 2026, 10:44:28.535 UTC from clean source`9be35d2bb4af136edbbba689d109cfb28b70de9e`, after all 89 payload hashes matched. The active management version is`cc107bd698c853bf`, FINALIZED/DEPLOY. [Hosting receipt](validation/site-v1.10/deployment.json) · [Original confirmation](validation/site-v1.10/hosting-confirmation.jpg). Local review covers 12 DOM states and 6 desktop rasters; mobile provider frames remain excluded from visual acceptance. No public-origin served-byte/render claim is made.

The documented L22→L23 structural increase, Guardian4→5 work increase and horizontal G1 pieces remain on the roadmap. This release preserves their logic rather than presenting a new human-calibrated difficulty curve. Google sign-in does not synchronize local gameplay saves. Test advertising is forced; real checkout and paid cosmetic draws stay gated. Two-account friendship/gift acceptance, provider acceptance, physical-device performance and human comprehension remain separate. No Cloud Run configuration or Google Play release is changed. No retention, revenue or hit outcome is inferred from these tests.

## GitHub delivery

Source, four signed artifacts and exact evidence are published together on [main source e785b115](https://github.com/nabz0r/AndroidHit/commit/e785b1158a02a810ac7045cecc120d70b124b28f). All four public downloads from that pinned commit return HTTP 200 and match the tested sizes/SHA-256. [Independent delivery record](validation/v1.10/publication.json). That Android delivery receipt changed no app, website payload or release artifact. The later [site source 9be35d2](https://github.com/nabz0r/AndroidHit/commit/9be35d2bb4af136edbbba689d109cfb28b70de9e) corrects two version captions and is now deployed with the Hosting receipt above. Android artifacts remain unchanged.
