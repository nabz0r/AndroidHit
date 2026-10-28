# Website 1.12 — atelier, items and clear currencies

The site presents production **1.12.0-rc2 / code 13**, Android 8+, exact signed APK **7,773,794 bytes** / SHA-256 `de581253b64232ae45bcbd647862dd470d522068f26516f6417bf515e882403d`. Existing app bytes and commercial gates remain unchanged. Hosting publication is recorded separately after these source and local checks.

## What changed

The FR/EN homepage leads with the actual 1.12 Home, then explains the new atelier, six recognizable powers, ordinary scrollable item pages, purchase/Equip distinction, equipped companion, earned LumaCoins and account LumaGold. It also keeps the seven free lessons, Undo/refund, 1,000 Journey boards, Remix and optional guardians. The full signed APK, exact version, byte count, checksum and release/qualification links replace the old1.10 download.

Pastel cream/plum styling is retained, with quieter open surfaces, round earned coins and violet faceted premium tokens. Layouts adapt to small screens. There is no new tracking SDK, remote font, autoplay, account request or monetization activation. Privacy metadata is updated to28October2026/1.12; its substantive data terms and deletion code remain unchanged. Existing no-cache/no-store/security headers remain unchanged.

## Actual image provenance

Nine full **1080×2340 native Lab RC2 PNGs** are exact copies of final40-clear qualification originals: four FR/EN pairs for Home, Powers, Spark detail and Appearance; one equipped portrait in English. [Source paths, SHA-256 and evidence](screenshot-provenance.json). No mockup, crop or retouch substitutes for a capture. Home is the legitimate40-clear guest replayingL13 with2,425earnedcoins and unknown account balance. It is not a new guest or authenticated Google state. The equipped portrait follows actual earned-coin purchases. Decorative mascot/scene assets are promotional art, separate from game captures.

## Checks and limits

- [Offline source check](release/static-check.json): **2,050 assertions**, three HTML pages,64 local references, exact current APK/manifest and full PNG hashes. Frozen deletion/runtime config, app-ads and Firebase config are checked byte-for-byte. Historical website evidence stays unchanged.
- [32 Node cases](release/node-tests.txt): current language routing/title/image/metadata/contact behavior and fail-closed deletion request contracts. Use the explicit current test command below; the v18 filename remains historical and expects its original title.
- [Local browser matrix](browser/report.json): **12 DOM states**, three pages ×FR/EN×desktop/mobile, with no horizontal overflow, correct visible/pressed language, preserved internal language and disabled permanent deletion before identity/confirmation. Twelve original JPEG viewport frames plus gallery/collection/currency views are retained. Final desktop English Home was recaptured after observing its image loaded; the initial race frame is excluded.
- Requested viewports are1440×1000 and390×844; the provider returns proportionally scaled full frames1425×990 and375×812. They are viewport captures, not full-page or physical-device evidence. Selected desktop/mobile rasters were visually reviewed for legible headings, body, currencies, navigation and authentic game imagery.

No public Firebase origin was requested for testing. Actual Google sign-in/deletion, friends, ads, purchases, human comprehension, accessibility certification and retention are outside this refresh. The site clearly retains test-ad/closed-checkout qualification state.

## Reproduce

```sh
python3 scripts/check-site-v112.py
node --test public/tests/site-language-v112.test.mjs public/tests/deletion-core.test.mjs
python3 -m http.server 8765 --directory public --bind 127.0.0.1
```

The separate [deployment helper](../../../scripts/deploy-site-v112.py) accepts an exact clean commit and an output directory outside the checkout. It verifies the entire Hosting payload set and hashes, deploys only Hosting when `--deploy` is specified, and inspects whitelisted authenticated live-channel management metadata. Its access token stays in memory and is never printed. It never requests a public Hosting URL. The source manifest is not itself deployment proof.
