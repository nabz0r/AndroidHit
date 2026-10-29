# Website1.18 — prepared, not publicly verified

The existing `luma-drift-production.web.app` site is prepared for the owner-requested1.18 H update. This folder records local preparation. No Firebase deployment or public response-byte verification is claimed here yet.

`prepare-site-v118.py` validates frozen H source/art identities, both exact signed APK hashes and original production Android captures before writing public files. It does not alter or copy an APK. The six FR/EN PNGs are byte-identical to native H captures. Existing account/privacy/configuration/audio and1.17 archive files remain frozen in `preserved-files.json`.

The gallery contains37 identities ×4 forms ×20 poses. Its148 WebP copies total293,692,016 bytes; lean per-form metadata totals2,053,882 bytes, and the roster is12,738 bytes. Six native PNGs total7,945,396 bytes. No atlas is requested on initial page display. The first deliberate Nova choice fetches one1,674,260-byte sheet; image and metadata caches each retain at most four entries. Geometry and pixels are copied from H, not newly generated for the website.

Local checks:15 Node tests and65 provenance/static checks pass. Actual browser review covers desktop French and a working gallery, plus320 CSS-pixel French Lune form3/jump and English Ahri form10/hug. The latter original viewport captures are in `browser/`. No horizontal overflow or console error was observed in those states. These samples are not exhaustive browser, phone or outfit-fit qualification.

## Finish publication

1. The owner assigns an immutable artifact metadata commit containing the H release manifest and source. APKs above100 MiB remain external artifacts, not Git blobs.
2. Run `python3 scripts/prepare-site-v118.py --artifact-commit EXACT40CHARCOMMIT`. This updates only the prepared website/manifest/provenance, preserving the immutable H delivery manifest.
3. With both ignored H APK copies in `public/releases/`, run `python3 scripts/check-site-v118.py --publication`. This audits physical bytes and writes `release/source-manifest.json`, including every public payload hash and the artifact metadata commit.
4. Commit the audited site and source manifest. From that exact clean commit, the owner runs `deploy-site-v118.py --commit EXACTSITECOMMIT --apk-sha256 EXACTPRODSHA --output /outside/checkout/new-receipts --deploy`. Its dry preflight without `--deploy` does not publish. The tool verifies external physical APKs against immutable committed metadata; it never requires an APK Git blob.
5. Keep the authenticated Firebase management receipt separate. Run `verify-site-v118-publication.py --commit EXACTSITECOMMIT --apk-sha256 EXACTPRODSHA --deployment /outside/checkout/new-receipts/deployment.json --output /new/public-origin.json` to stream and hash every public payload, both H APKs and three directory aliases. Browser rendering remains a separate scope.

The Mini LoL notice links the official Riot policy and explicitly states that permission has not been verified. It does not grant a license or establish compliance. Publication and external-IP decisions belong to the owner.
