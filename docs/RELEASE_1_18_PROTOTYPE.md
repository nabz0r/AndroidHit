# Luma Drift 1.18 — prototype published; Android UX rework open

## Retour du propriétaire —2026-10-29

L’essai humain de l’APK1.18H signale un échec de clarté de sélection, d’interaction, de navigation et de sensation de fluidité. La refonte UX est ouverte et n’a pas encore été implémentée dans une nouvelle APK : les acceptances automatisées restent des validations fonctionnelles dans leur périmètre et n’impliquent pas une UX validée. Aucun FPS ni diagnostic causal de performance n’est inventé. Les APK, manifestes et témoins H restent immuables. La [proposition1.19](design/v1.19/UX_RETHINK_FR.md) et sa [maquette interactive](https://luma-drift-production.web.app/lab/ui-1-19/index.html?revision=a2a044047f9232b871bd2c035c27d3f24d8ad26c) sont des éléments de revue, avec profil fictif en mémoire du navigateur et sans écriture Android, pas une nouvelle implémentation du jeu ni une évolution de l’économie.


This H prototype delivers four-puzzle Observatory expeditions, copy progression, animated companion forms and the owner-requested Mini LoL guest series. [Production APK URL](https://luma-drift-production.web.app/releases/luma-drift-1.18.0-prototype1.apk) · [Lab APK URL](https://luma-drift-production.web.app/releases/luma-drift-1.18.0-sandbox-prototype1.apk) · [Expected hashes and limits](../releases/manifest-1.18.0-prototype1.json). Source, artwork and H artifact metadata are published at Git [C1 `4972565151521c5910b2e5d90921491f00acb7eb`](https://github.com/nabz0r/AndroidHit/commit/4972565151521c5910b2e5d90921491f00acb7eb). The existing [Firebase site](https://luma-drift-production.web.app/) is deployed from [C3 `a2a044047f9232b871bd2c035c27d3f24d8ad26c`](https://github.com/nabz0r/AndroidHit/commit/a2a044047f9232b871bd2c035c27d3f24d8ad26c), version `381fee5ff7548191`, released **2026-10-29T18:40:32.706Z**. [Deployment receipt](validation/site-v1.18/hosting/corrective-c3/deployment.json). [Initial C2 complete public HTTPS-byte verification](validation/site-v1.18/public-origin.json) passes489/489 responses, including both exact H APKs. Main-site public browser checks pass; the C2 prototype wardrobe failure and its published CSS correction have separate receipts in the [publication status](RELEASE_1_18.md). APK files are external artifacts rather than large Git blobs. The immutable H manifest is the historical creation/local-qualification record, not a mutable current-publication receipt. The published1.17 packages and their evidence remain historical and unchanged.

Local website preparation passes **68 publication/provenance checks and15 Node tests**. Those local checks, deployment management, public download bytes, public browser behavior and the owner’s Android UX acceptance retain separate scopes.

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

The fresh C3 [public browser report](validation/site-v1.18/prototype-layout-fix/browser-public/observations.json) passes20/20 assertions at1280×720 and320×720. Trial/application, Return with context, Home committed state and draft resume are checked; this qualifies the browser proposal only. The C2 prototype failure remains intact as historical evidence.
