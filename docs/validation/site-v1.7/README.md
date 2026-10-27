# Website 1.7 — local qualification, Hosting pending

**27 October 2026.** The existing Firebase site’s 1.7 source and localhost browser checks pass. **Hosting publication of this revision is not yet claimed.** The [preserved 1.6 deployment](../site-v1.6/README.md) remains a separate record. No public Firebase-origin request or served-byte comparison was performed for this revision.

## Exact source and imagery

The homepage links production **1.7.0-rc1 / code 8**, 5,594,242 bytes, SHA-256 `7eeaa816853533f441acfc73f4ad3f6af0fb2cbdd6fd0eae1fda3f64c388a2bf`. The offline check independently compares those bytes with the release manifest. It does not fetch the remote APK.

The [source manifest](source-manifest.json) fingerprints **47 Hosting payloads** and Firebase configuration. The [asset record](source-assets.json) identifies 37 image/vector assets, including **11 exact unedited copies** from the [passing final Lab menus2 run](../v1.7/ui-sdk-final-menus2/host-report.json). That run binds 38 native assertions/32 captures to Lab APK `d7fea345c1f33cc8e114688ee06d8b60ed1278cc29eeb328c677c6c1858ba049`, API 36/16KB, a guest session without injected profile and original preferences restored. Website captions explicitly identify Lab imagery; they are not production-menu or authenticated-account evidence. Original Nova/Atlas and older assets retain their own provenance.

## Executed local checks

| Check | Result | Scope |
| --- | --- | --- |
| [Offline source/DOM/files/hash](static-check.json) | **2,219 assertions**, 3 HTML pages, 66 local references, zero failures | Local files/configuration only; no browser or network |
| [CUA matrix](browser-matrix.json) | **18 views**: homepage/privacy/deletion × FR/EN × measured CSS widths **320/390/1366** | Localhost; no wrong-language visible nodes or broken visible images recorded |
| [Loaded images](browser-images.json) | English localized image sources decoded after real page scrolling | Actual local browser image state; screenshots retain native source provenance |
| [Language/deletion continuity](browser-continuity.json) | Actual EN button and privacy/deletion links retain EN; guest deletion disabled and confirmation hidden | Google login and account deletion never invoked |
| [Deletion request core](deletion-core-tests.txt) | **24 tests passed** | Synthetic request/configuration contracts, not real accounts |

The browser matrix records measured CSS widths separately from requested viewport dimensions because the browser has 110% zoom. The final run targets actual 320/390/1366 CSS pixels; an earlier exploratory zoomed run is not counted as another acceptance matrix.

Five unedited CUA local page captures are under [screenshots/site-v1.7](../../screenshots/site-v1.7/): [desktop FR](../../screenshots/site-v1.7/home-desktop-fr.png), [mobile FR](../../screenshots/site-v1.7/home-mobile-fr.png), [mobile EN](../../screenshots/site-v1.7/home-mobile-en.png), [gallery EN](../../screenshots/site-v1.7/gallery-mobile-en.png) and [guest deletion EN](../../screenshots/site-v1.7/delete-mobile-en.png). They demonstrate local source rendering, not a deployed website.

Run `python3 scripts/check-site-v17.py` and `node --test public/tests/deletion-core.test.mjs` from the repository root. Browser checks use CUA on localhost; the historical autonomous browser runner is not executed. The static checker rejects new product-image copies unless their final native report passes, preferences are restored, the exact final APK matches and each recorded PNG hash agrees.

## Delivery and remaining boundaries

The 1.7 source describes the continuous map, exact safe hints/dead-end recovery, four-slot wardrobe, seven-visit gift cycle, quests, separate currencies and disclosed earned-capsule odds. Prepared premium draws remain closed by independent APK/server gates. Account-new ownership and local-earned ownership are distinct.

Server deployment, authenticated two-account social/catalogue behavior and Hosting publication are separate records. This local website acceptance establishes none of real Google authentication/deletion, friend exchange, paid draws, live ads, real checkout, Play approval, full accessibility, native-speaker review or retention. A later Firebase completion must record its exact source commit and frozen payload manifest before this revision is described as published.
