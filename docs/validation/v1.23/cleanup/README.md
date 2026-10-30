# Bounded public cleanup — 1.23

The [retirement manifest](public-retirements.json) records 14 pre-1.22 APK/AAB copies removed from the local `public/releases/` directory, totalling **3,136,669,533 logical bytes**. Before each removal, both the public copy and its retained canonical `releases/` copy matched the original C3 baseline size and complete SHA-256. Six removed copies were Git tracked; eight were already ignored. This does not shrink historical Git objects or guarantee the same amount of physical disk space reclaimed.

The retained 1.22 downloads, release metadata, canonical binaries, historical technical/native/synthetic evidence, and original finalized Hosting versions remain available in their original scopes. Historical JSON metadata records an old identity; it is not a current download link. The next source manifest must describe each removed path under `retired_files` with its exact original size and hash.

The [1.23 transport contract](../site-transport/README.md) permits only these exact removals. Its provider gate requires a clone of C3 excluding 14 anchored paths, then verifies every retained provider hash and the unchanged configuration. The separate public-response gate requires HTTP 404 for all 14 retired paths without reading obsolete response bodies. **Local removal is complete; actual Hosting retirement and public 404 acceptance remain pending separate receipts.**

## Additional inventory and deliberate preservation

Read-only inventory found 176 files under `public/assets/screenshots/`, totalling 136,013,467 bytes, with **no byte-identical duplicate group** by complete SHA-256 plus size. Removing old captures would also change the accepted historical C3 map and gallery/evidence relationships. No capture was deleted.

The historical `public/lab/cognitive/cognitive-lab-local.zip` is still used by the archive selector in `public/lab/cognitive/lab.js`. The old `public/lab/ui-1-21/` runtime remains a documented historical target checked by `scripts/check-site-v121.py`; it has no APK/AAB links to these retired binaries. Both were preserved. The only root Markdown files are README, PLAN and PROGRESS; no unreferenced root mockup was found. The 1.19 collection prototype and the recent clarity prototype are design/audit history, not disposable generated files.

An incoming-reference search over public HTML, JavaScript, modules, CSS, JSON and Markdown found old binary names in seven historical release metadata files, one excluded historical test, and the old homepage archive anchors. The metadata and historical test are preserved. The homepage is owned by the release preparation and must replace old archive anchors with the retained 1.22 archive before publication. Its final reference gate belongs to site preparation/check, not this earlier local-cleanup record.

This cleanup performs no Git history rewrite, no canonical binary deletion, and no deletion of past release/audit evidence. Restoring a removed path is documented per entry in the retirement manifest.
