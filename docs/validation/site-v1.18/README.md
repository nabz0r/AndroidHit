# Website 1.18 — published prototype and UX proposal

The existing [FR/EN website](https://luma-drift-production.web.app/) released clean source `98917d050b626fe1693e6b1a3522b386353a8aa6` (C2) on **2026-10-29T18:13:48.028Z**. Authenticated Firebase management confirms DEPLOY/FINALIZED for version `1e51ed3275f5cb9c`. [Deployment receipt](hosting/attempt-serial/deployment.json) and [live channel receipt](hosting/attempt-serial/live-management.json) retain that exact scope. Independent public response bytes pass in the separate receipt below. Actual public browser rendering remains a separate review in progress.

## Source and immutable Android artifacts

Frozen H source/artifact metadata is published on Git main at `4972565151521c5910b2e5d90921491f00acb7eb` (C1). Both C1 and C2 pass the hosted contract workflow; [publication preflight receipts](../v1.18/publication-preflight/) retain remote tree, binary-object and CI checks. Site C2 has public tree `8afba794c29612978233a6f913271b336c3207ce`; [immutable C2 source manifest](https://github.com/nabz0r/AndroidHit/blob/98917d050b626fe1693e6b1a3522b386353a8aa6/docs/validation/site-v1.18/release/source-manifest.json) SHA-256 is `2ec5daab1cbe90d1328524ea58939ffdfa936fbf16ab8a4ac7e69e86886183ca`. The current local manifest contains the single-payload corrective candidate described below.

The deployment preflight verified all **486 payloads / 1,351,822,598 bytes**, including the external physical APK copies against the immutable C1 metadata. APKs exceed GitHub's individual-file limit and are intentionally external artifacts rather than Git blobs.

| Artifact | Public URL | Exact H SHA-256 |
|---|---|---|
| Production | [1.18.0-prototype1 APK](https://luma-drift-production.web.app/releases/luma-drift-1.18.0-prototype1.apk) | `3a30526941d69bd6e65cd148c7eb7fa5b47647f85f7b53f9a043b26b5abdbbd2` |
| Lab | [1.18.0-sandbox-prototype1 APK](https://luma-drift-production.web.app/releases/luma-drift-1.18.0-sandbox-prototype1.apk) | `96981f3d9b366713b0842204c69e3a1e46a9e31bfb6d0196b1e278ef0902ee06` |

The canonical H release manifest remains its historical creation snapshot, including its original local-publication fields. It was not rewritten to manufacture evidence of a later publication. These Hosting and public-origin receipts describe the later delivery. No Android source, art or APK was rebuilt for this website publication.

## Website contents and local checks

The gallery contains 37 identities × 4 forms × 20 poses. Its 148 WebP copies total 293,692,016 bytes; lean metadata totals 2,053,882 bytes, and the roster is 12,738 bytes. Six unedited native H FR/EN PNGs total 7,945,396 bytes. No atlas is requested on initial page display; a deliberate character selection loads its sheet, with image and metadata caches limited to four entries each. Existing account/privacy/configuration/audio and 1.17 archive files remain frozen in [preserved-files.json](preserved-files.json).

The standalone [collection/character/wardrobe proposal](https://luma-drift-production.web.app/lab/ui-1-19/) is a self-contained HTML file (initial C2:4,015,601 bytes; corrective candidate:4,018,204 bytes). It embeds reduced previews for 37 identities and 148 forms, with 24 clothing images; [provenance](ux-prototype-provenance.json) records the exact copy and preview scope. It changes only demonstration state in memory. Clothing is presented beside the character; it does not qualify worn overlay fitting. The [UX study](../../design/v1.19/UX_RETHINK_FR.md) covers the broader navigation and invocation journey beyond these three prototype views.

Fifteen Node tests and **68 local provenance/publication checks** pass. Earlier local browser samples are retained in `browser/`; they are not substituted for checks of the public origin. The owner rejected Android H clarity and perceived fluidity. This proposal is not an implemented Android refactor or a human UX acceptance result.

## Deployment attempts

The default-concurrency attempt exhausted six upload retries for one screenshot hash after 203 of 372 new uploads. Its [original CLI excerpt](hosting/attempt-default/deploy-cli-excerpt.txt) and preflight remain retained. Version `40d7ceaebf5d1117` was observed CREATED, not released. No exact underlying network cause was established.

The retry used the unchanged C2 source and exact APKs with `FIREBASE_HOSTING_UPLOAD_CONCURRENCY=1`, supported by the installed official Firebase CLI 15.33.0. It uploaded the remaining files, finalized and released version `1e51ed3275f5cb9c`. [Full retained CLI excerpt](hosting/attempt-serial/deploy-cli-excerpt.txt) and [transfer provenance](hosting/receipt-transfer.json) preserve the result. Credentials were never included in the exported receipts.

## Independent public checks

[Public-byte verification](public-origin.json) passes **489/489 responses / 1,351,893,314 streamed bytes / zero failures**, recorded2026-10-29T18:23:25.147276Z. This covers all486 payloads and three directory aliases, including both complete H APKs, against the committed C2 source manifest. Browser checks separately inspect FR/EN responsive layouts, deliberate gallery choices, keyboard navigation and the standalone proposal; final browser review remains in progress under `browser-public/`. Management status alone does not prove either scope.

The existing Mini LoL notice links the official Riot policy and records that permission has not been verified. Publication does not grant a license or establish an endorsement.

## Wardrobe prototype correction

The [C2 public browser report](browser-public/observations.json) passes the main FR/EN website checks, while recording a genuine prototype wardrobe failure: at1280×720 the clothes viewport had zero height. Collection/detail/navigation samples passed; trial/application did not. Original failure screenshots remain retained.

The corrective candidate changes only `/lab/ui-1-19/index.html` in the public payload: responsive CSS reserves space for clothes, preview, header and action; the neutral caption “Aperçu” remains accurate before and after application. Embedded assets and JavaScript behavior are preserved. [Local report](prototype-layout-fix/local/report.json) passes actual Lune trial/application at1280×720 and320×720, with224px and157px choice areas respectively. The native320 capture has a documented compositor scale defect and is excluded from mobile visual acceptance. [Exact local payload delta](prototype-layout-fix/local-payload-delta.json) retains source hash and sizes. Corrective public deployment and targeted public checks remain pending at this checkpoint.
