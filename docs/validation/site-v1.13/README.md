# Website 1.13 — house and main puzzle

The FR/EN source presents **“Solve a little. Come home.” / “Un puzzle. Un petit chez-toi.”** The home leads the page; the unchanged 1,000 Journey +240 Remix puzzle route stays explicit. This source record is distinct from installed app acceptance, browser rendering and Firebase Hosting delivery.

## Source and controls

- `public/index.html` shows the native room, furniture/decorating, main puzzle and companion cuddle, with a current **1.13.0-rc2/code14** APK route, actual size and SHA-256.
- `public/assets/site.css` supplies responsive layouts, visible keyboard focus, reduced motion, zoom-friendly viewport and 44×44 language controls without external fonts or tracking.
- `public/assets/site.js` switches visible language, title, description, screenshot sources, alt text and same-origin legal-page routes. Language state lives in the URL; it introduces no cookies or local storage.
- The privacy update adds only the house/opening/local-journal disclosure and current edition metadata. [Retention baseline](privacy-retained-policy.json) proves prior policy paragraphs and section headings remain. Account deletion runtime, Firebase public configuration, advertising record and Hosting configuration retain their existing bytes.

The candidate keeps test ads, disabled real checkout and premium draws, and device-local game/home progress. Existing Google identity does not synchronize gameplay. No human enjoyment, retention, physical-phone or Play/provider acceptance is inferred from this website.

## Exact release

Production RC2 APK SHA-256: `c68be62fd940e753fc06c4b5677a48cafaf077a4710755b3bdf96467d571e31b`, **7,806,558 bytes / 7.81 MB decimal**. The page resolves its metadata from that exact signed freeze; the checker still refuses unresolved placeholders or mismatched bytes. [Production manifest](../../../releases/manifest-1.13.0-rc2.json) · [Android qualification](../v1.13/README.md).

Both download actions target the exact `main/releases/luma-drift-1.13.0-rc2.apk` raw GitHub route. Inspecting local links does not prove remote availability. The release operator records the actual main commit and independent public-download result separately.

## Native image contract

The page requires nine original full-frame PNG copies: FR/EN home, decoration, furniture and puzzle, plus the explicitly English `house-hug-en.png`. Their source paths, native report, locale, hash and dimensions are retained in [the provenance manifest](screenshot-provenance.json). All nine are unedited 1080×2340 PNGs from the passing/restored 798-check RC2 full13 route. Each displayed file must be byte-identical to a capture indexed by a passing, restored exact **RC2** candidate run. RC1 images and the failed full13 route are excluded from current product capture acceptance. Labelled analysis crops and a globally failed native run cannot qualify a public game screenshot.

CSS backgrounds, device frames, stars and the heart are website decoration. They are not passed off as game captures. The English hug screenshot remains labelled English in both site languages.

## Local commands

```sh
node --test public/tests/site-language-v113.test.mjs public/tests/deletion-core.test.mjs
python3 scripts/check-site-v113.py --self-test
python3 scripts/check-site-v113.py \
  --apk-sha256 c68be62fd940e753fc06c4b5677a48cafaf077a4710755b3bdf96467d571e31b \
  --report-dir docs/validation/site-v1.13/release
python3 -m http.server 8763 --bind 127.0.0.1 --directory public
```

The 32 JavaScript language/deletion tests pass in [their retained output](language-and-deletion-tests.txt). The offline checker reads all three HTML pages, local links, bilingual visibility and metadata, every Hosting payload, exact APK bytes/hash/version/gates, policy retention and original capture provenance. It rejects missing captures, unresolved APK placeholders and a failed native evidence source. It does not visit external origins or start a browser. The [final source report](release/static-check.json) passes **2,150 assertions**, with **108 public payloads** and twelve frozen native capture inputs in [the exact source manifest](release/source-manifest.json). The checker parser self-test also passes its inherited-language visibility, hidden-control and exact plus-tag regressions. Earlier draft/structure records remain outside final acceptance.

The [final CUA browser report](browser/report.json) passes **14 recorded DOM states**, with seven original JPEG viewport captures. FR/EN desktop and mobile hero views are visually reviewed, as are lower mobile catalogue/puzzle views and the compact companion section. Room/decor/catalogue/puzzle/hug/download geometry, decoded native images, language-specific metadata/legal routes, both RC2 download actions, disclosures and visible keyboard focus are checked.

Actual CSS viewports are 1366×900, 390×844 and 320×740. The browser’s pre-existing 110% zoom means its original JPEGs are 1353×891, 376×814 and 306×708; these dimensions are recorded rather than labelled as PNGs or resized. The 320px review found a three-pixel decorative-heart overhang, corrected by sizing/centering that element; final FR/EN widths match their scroll widths without a global overflow mask. The original viewport is restored, the owned preview tab closed and the local preview server stopped.

[Desktop French view](browser/desktop-fr.jpg) · [Desktop English view](browser/desktop-en.jpg) · [Mobile French](browser/mobile-fr.jpg) · [Mobile English](browser/mobile-en.jpg) · [Catalogue](browser/mobile-catalog-en.jpg) · [Puzzle](browser/mobile-puzzle-en.jpg). These are local rendering evidence; early reviews with missing captures do not qualify the final site.

## Hosting boundary

No 1.13 deployment receipt is recorded in this document yet. The preserved [1.12 delivery](../site-v1.12/README.md) keeps its original dated scope.

`scripts/deploy-site-v113.py` accepts an exact clean source commit only. It verifies every public payload, unchanged Firebase target/configuration, the current passing source manifest, frozen APK and release metadata, checker inputs and capture provenance before any optional Hosting deploy. The Firebase CLI/live-channel result is retained outside the deployment checkout, then incorporated by the release operator. It does not upload an app to Play or activate provider services.

## Independent public-origin verification — pending deployment

After a real finalized Hosting receipt is retained, the release operator can run:

```sh
python3 scripts/verify-v113-site-publication.py --commit "$DEPLOYED_SOURCE_COMMIT"
```

The required value is the immutable forty-character commit actually named by the finalized Firebase receipt. The verifier requires the passing RC2 source manifest and matching `public` tree, then streams each public HTTPS payload plus `/`, `/privacy/` and `/delete-account/`. HTTP status, bytes and SHA-256 must match exactly. Its eventual output is `docs/validation/site-v1.13/public-origin.json`; no such pass is claimed before execution. Browser rendering and actual account/provider operations remain separate.
