# Luma Drift 1.18 — prototype; publication prepared

This H prototype delivers four-puzzle Observatory expeditions, copy progression, animated companion forms and the owner-requested Mini LoL guest series. [Prepared production APK URL](https://luma-drift-production.web.app/releases/luma-drift-1.18.0-prototype1.apk) · [Prepared Lab APK URL](https://luma-drift-production.web.app/releases/luma-drift-1.18.0-sandbox-prototype1.apk) · [Exact hashes and limits](../releases/manifest-1.18.0-prototype1.json). Git and the existing Firebase publication are prepared at the owner’s request. Public release and download-byte verification remain pending; see the [publication status](RELEASE_1_18.md). APK files are external artifacts rather than large Git blobs. The published1.17 packages and their evidence remain historical and unchanged.

## Player behavior

- Companion rarity stays fixed at N/R/SR/SSR. Copies of the same identity determine levels1–10; there is no XP or separate awakening rank.
- Four separately drawn bodies appear at levels1,3,6,10. Intermediate levels use small additional details. This is four authored forms per identity, not ten different bodies.
- Every form has20 authored poses: four directions standing, four walking, jump/landing, hug/crouch, four emotions and four stances. Calm/ready/guard/proud are saved per owned companion.
- Existing paid-in-coins companion draws retain their prices, probabilities, refunds and receipts. New duplicate arrivals also add a copy. Earlier receipts do not retroactively mint copies.
- An Observatory run contains four actual U boards and two forks. Nova reveals a possible encounter, Miko replaces a proposal, Orbit opens a project route. These are the three available prototype signatures.
- The project awards an owned companion copy or4/12 progress toward the home project, plus a free random arrival from the published original avatar pool. The copy target is independent of the expedition actor, including owned Mini LoL guests.
- Route, signature use, stage wins and claims persist independently of the active campaign puzzle. Claims are atomic and repeat-safe.
- Original costumes display by default when no earlier explicit garment selection exists. The wardrobe can switch between original costume and saved clothing; equipping an item activates the saved wardrobe atomically. The studio switch previews clothing without changing that durable choice.
- Thirteen Mini LoL guests join the24 original companions: Ashe, Ezreal, Soraka, Ahri, Jinx, Lux, Teemo, Yasuo, Annie, Lulu, Sona, Miss Fortune and Vi. They are grouped in the inventory, use earned-coin prototype acquisition and have the same form/pose/stance contract. They are outside the historical random banner.

The cumulative copy thresholds are1/2/8/14/22/32/44/58/74/92. Values above the owner's initial1/2/8 example are provisional. Maximum-level surplus compensation and signature charges are also prototype balance values, not measured retention findings.

## Artwork before package delivery

The artwork comprises37 identities ×4 forms ×20 poses =2,960 drawings, stored as148 transparent lossless WebP atlases with measured metadata. Original PNGs and generation/repair prompts are retained under [art/v118](art/v118/README.md). All generation used the built-in image tool. Official Riot splash references are outside packaged assets; [third-party notices](THIRD_PARTY_NOTICES.md) identify the requested fan-art series.

The [local review gallery](art/v118/gallery.html) selects character, evolution, pose and stance without granting game ownership. It must be served from the repository root. `scripts/v118-art/build-gallery.py` refreshes its manifest. Source preservation, frame cropping, anatomical wearable fitting and actual Android behavior have distinct validation scopes.

## Validation register

Current source contracts are in `validation/v1.18/expedition-model.json`, `expedition-store.json` and `companion-motion-model.json`. They cover immutable route/copy semantics, actual store APIs with durable preference doubles, refusal/recovery/concurrency, guest purchases and target-copy projects. These JVM checks are not Android UI or visual quality claims.

H exact-byte validation passes:12,093native Canvas checks/6,112composites;118retained-profile UI checks/73real gestures;588genuinely fresh production checks/170real gestures, including six languages, compact presentation and cold restoration. The two signed artifacts retain the exact tested hashes. See the [validation register](validation/v1.18/README.md) for source witnesses, separate historical E gameplay evidence, preserved failures, device receipts and scope limits.

Wearable anchors are mostly provisional. Successful decoding and rendering never automatically mark every garment fitting as visually approved. Levels beyond the initial milestones, rare-item value, event cadence, physical-phone comfort, audience understanding and enjoyment remain subjects for playtesting.
