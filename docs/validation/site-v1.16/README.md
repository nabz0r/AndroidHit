# Website 1.16 preparation

This updates the existing `luma-drift-production` Firebase Hosting site. It does not create a Sites project, change legal/account-deletion pages, alter Firebase configuration, activate real checkout or publish anything by itself.

The landing page introduces the closer home camera, the local nickname and chosen flag, six earned badges with three equipped slots, and two earned-LumaCoin arrivals banners: 23 companions (free Nova excluded) and five four-piece outfits. Exact probabilities, duplicate compensation, existing direct purchases and the separate legacy clothing capsule remain explicit in French and English.

`index.template.html` stays outside the public payload. Its release hash and size fields are filled only after the accepted final release and all native screenshots pass input checks. The preparer writes `public/index.html` last, after copying the exact final APK, AAB, manifest and sixteen native PNGs. It never transforms an image.

## Final capture map

Create `capture-map.json` here only after selecting passing evidence for the accepted final APK. Its `images` array must contain exactly eight keys in each locale, `fr` and `en`: `adventure`, `home`, `collection`, `atelier`, `profile`, `summon`, `reveal`, `badges`.

Each entry has these fields:

| Field | Required content |
| --- | --- |
| `public` | `public/assets/screenshots/v1.16/` plus the key, `-fr.png` or `-en.png` |
| `source` | Exact full native PNG path under `docs/screenshots/v1.16/` |
| `evidence` | Passing native host report under `docs/validation/v1.16/` |
| `locale` | The actual capture locale, `fr` or `en` |
| `description` | Actual scene, player state and any material limit visible in the capture |

The native host report must declare `passed: true`, the independently supplied final APK hash, and the original screenshot filename/hash. Current QA captures are 1080 × 2340. An unrelated APK, missing capture, partial/edited capture, wrong language or extra current-edition public PNG is rejected before public bytes are written.

## Freeze, check and publish

Run these only after accepting the final signed release and native report. `FINAL_APK_SHA256` is its independent 64-character hash; `ARTIFACT_COMMIT` is the immutable commit containing the canonical APK/AAB. These task-specific variables are ordinary placeholders in these instructions, never values to put into the public page.

```sh
python3 scripts/prepare-site-v116.py --apk-sha256 "$FINAL_APK_SHA256" --check-only
python3 scripts/prepare-site-v116.py --apk-sha256 "$FINAL_APK_SHA256"
python3 scripts/check-site-v116.py --apk-sha256 "$FINAL_APK_SHA256" --source-commit "$ARTIFACT_COMMIT"
```

The offline checker records each public byte, source screenshot, native report, preparer, template and relevant verification script. It checks the current bilingual page and exact release downloads; the historical parser and configuration checks remain shared. Historical edition scripts, tests and evidence are not edited. JavaScript language contracts are in `scripts/test-site-v116.mjs`.

Browser QA is a separate CUA task on a local preview, including FR/EN, desktop/mobile widths, keyboard focus, reduced motion and image/download references. No screenshot manipulation or independent browser-control library is part of this workflow.

After that audit, commit the complete website payload and evidence. The root task owns publication. With the already authenticated Firebase CLI, the deployment script checks the clean commit, every frozen input, release artifact and public byte before an explicit `--deploy` to the existing Hosting project:

```sh
python3 scripts/deploy-site-v116.py --apk-sha256 "$FINAL_APK_SHA256" --commit "$SITE_COMMIT" --output "$DEPLOYMENT_RECEIPT_DIR" --deploy
python3 scripts/verify-site-v116-publication.py --apk-sha256 "$FINAL_APK_SHA256" --commit "$SITE_COMMIT" --deployment "$DEPLOYMENT_RECEIPT_DIR/deployment.json"
```

`DEPLOYMENT_RECEIPT_DIR` must be outside the checkout so the checked source remains clean. Public HTTPS byte verification and live-browser rendering are separate from Firebase management confirmation. None of these checks imply Play publication, real payment acceptance or performance on the user's phone.

## Accepted release input

The root task supplied production candidate B: APK SHA-256 `516be7f14ff2b80683de658b6df767f4b19dbb44580d57be36a307d330ed5a66`, canonical artifact commit `35b749582d9ce794754ef8ea3ae1a5197093e3f2`. The passing normal B report indexes all sixteen selected images. Offline verification passed 2,583 assertions across three HTML pages and 83 local references. Compact-device acceptance and browser/publication checks remain separate.

Both arrivals captures show the Companions banner (23 companions, 180 LumaCoins). The French capture is selected from the additive normal B banner run, which explicitly selects each banner and visits all 23 companion names through Inky at the real bottom. The English capture remains from the first passing normal B run. Localized alternatives and captions describe the visible selection. The observer filename alone is never treated as proof of the selected tab. The reveal is the actual saved Star parade duplicate, compensated with 100 LumaCoins, rather than a guaranteed new outfit.

## Executed local acceptance

Final B input preparation passes with16unaltered native images. The offline audit passes2,583assertions and83local references across three pages, with immutable artifact commit `35b749582d9ce794754ef8ea3ae1a5197093e3f2`. [Source manifest](release/source-manifest.json).

[Actual browser observations](browser/local-browser.json) pass FR/EN at1280,768,375and320CSSpx, with17DOM/layout observations and14retained visual states. Keyboard disclosure and visible focus pass; two loaded reduced-motion CSS rules are inspected without changing OS preferences. The three provisional transition/cache frames are explicitly excluded from visual acceptance; the fresh-origin final collection frame shows the correct23-companion French banner and caption. [Browser source/image hashes](browser/local-integrity.json).

Hosting deployment and public-origin bytes/rendering are recorded separately after publication.

## Published result

The existing [Hosting site](https://luma-drift-production.web.app/#screens) is **FINALIZED** at 2026-10-29T00:01:49.165Z, version `963fcf70004ea230`, from clean source `ea1980f7dbc3bfe65233065d4d902beba083301f`. The [management receipt](deployment/deployment.json) verifies its exact151payloads and129,437,422bytes. The separate [public HTTP audit](public-origin.json) verifies all151responses plus three aliases, with matching sizes and SHA-256, including the exact B production downloads. Live FR/EN browser evidence is stored in `browser/live-browser.json`; local responsive and keyboard checks remain their own scope. No Play/GitHub publication or provider activation is claimed.
