# Bounded public cleanup — 1.23

The [retirement manifest](public-retirements.json) records 14 pre-1.22 APK/AAB copies removed from the local `public/releases/` directory, totalling **3,136,669,533 logical bytes**. Before each removal, both the public copy and its retained canonical `releases/` copy matched the original C3 baseline size and complete SHA-256. Six removed copies were Git tracked; eight were already ignored. This does not shrink historical Git objects or guarantee the same amount of physical disk space reclaimed.

The retained 1.22 downloads, release metadata, canonical binaries, historical technical/native/synthetic evidence, and original finalized Hosting versions remain available in their original scopes. Historical JSON metadata records an old identity; it is not a current download link. The [C2 source manifest](../../site-v1.23/release/source-manifest.json) records all 14 removed paths under `retired_files` with their original sizes and hashes.

The [1.23 transport contract](../site-transport/README.md) permits only these exact removals. The [final provider map](../../site-v1.23/hosting/receipts/final-deployment/verified-provider-files.json) excludes the 14 anchored paths and preserves every retained provider hash and serving configuration, as recorded by the [deployment](../../site-v1.23/hosting/receipts/final-deployment/deployment.json). The separate [public collector](../../site-v1.23/public-response.json) received HTTP 404 for all 14 retired paths without reading obsolete response bodies. **Local removal, Hosting retirement and public HTTP 404 acceptance are complete in these bounded scopes.**

The [retirement closeout](final-delivery.json) joins those original identities and receipts without deleting their canonical or historical evidence.

## Git-tip accounting

The immutable [C1 tree comparison](repo-tip-c1.json) separates public-copy cleanup from Git storage:

| Scope | Logical bytes |
| --- | ---: |
| All 14 public copies removed, including ignored copies | 3,136,669,533 |
| Six tracked binary paths removed | 185,306,489 |
| Net reduction by tracked path at the C1 tip | 77,749,280 |
| Increase in unique Git blob bytes at that tip | 99,448,969 |

New evidence and source paths add 107,602,303 bytes across 2,792 paths. These measurements exclude compression, packfiles, ignored files, filesystem clones and historical Git/Firebase versions. They do not establish an equal physical disk saving or a smaller complete Git history.

## Additional inventory and deliberate preservation

Read-only inventory found 176 files under `public/assets/screenshots/`, totalling 136,013,467 bytes, with **no byte-identical duplicate group** by complete SHA-256 plus size. Removing old captures would also change the accepted historical C3 map and gallery/evidence relationships. No capture was deleted.

The historical `public/lab/cognitive/cognitive-lab-local.zip` is still used by the archive selector in `public/lab/cognitive/lab.js`. The old `public/lab/ui-1-21/` runtime remains a documented historical target checked by `scripts/check-site-v121.py`; it has no APK/AAB links to these retired binaries. Both were preserved. The only root Markdown files are README, PLAN and PROGRESS; no unreferenced root mockup was found. The 1.19 collection prototype and the recent clarity prototype are design/audit history, not disposable generated files.

An incoming-reference search over public HTML, JavaScript, modules, CSS, JSON and Markdown found old binary names in seven historical release metadata files, one excluded historical test, and the old homepage archive anchors. The metadata and historical test are preserved. The published C2 homepage replaces those archive anchors with the retained 1.22 archive; the [site preparation](../../site-v1.23/preparation.json) and [public delivery](../../site-v1.23/README.md) record that later gate separately from the earlier inventory.

This cleanup performs no Git history rewrite, no canonical binary deletion, and no deletion of past release/audit evidence. Restoring a removed path is documented per entry in the retirement manifest.
