# Website 1.9 — FINAL5 source qualification

**Current status: native imagery, release source checks and local browser review passed; publication is recorded separately by the coordinating release run.** This file does not claim a Firebase deployment or a public-origin render/byte verification.

The homepage describes the unchanged 1,000 L / 240 M boards, the corrected gate-practice sequence, 100 optional guardian encounters, two-layer pieces from guardian 1 and one-cell movement from guardian 5/50 clears, accessible Journey gifts, single-charge power trials, action-based stars, and 24 × 12 combinatorial companion treatments. All 12 visual treatments are free; choosing one does not grant an unowned companion. Five poses and actual-progress parallax are explained with reduced-motion support. The original pastel design and Dream Glade illustration remain; no franchise art or new illustration is implied.

## Exact artifact and images

The production download is **1.9.0-rc1 / code 10**, **7,740,394 bytes / 7.74 MB decimal**, SHA-256 `b02225c0464bf35cc1c04f64f0a4c8b1db610bde36b293bae9790f1a9e2c2b05`. The [release manifest](../../../releases/manifest-1.9.0-rc1.json) supplies the configuration and input hashes. Test ads remain forced, real checkout remains closed, and live services are not qualified.

Thirteen [public PNG copies](captures.json) come from the exact FINAL5 Lab APK `acb3e1b0cfa71f84435d6cb0cf0fc73e3d4b434009deef972c54d86d9e7c09c7`:

- Ten FR/EN images cover Home, dressing room, Rewards, level-1,000 map exploration and replayed key/seal practice. Two further English images show the real studio and guardian 5 after layer/cell actions. They match the [passing full run](../v1.9/ui-sdk-final5-full100/host-report.json): **2,277 checks**, all first 100 levels played, original preference bytes/display/rotation restored afterward.
- One English first-lesson image comes from [fresh installation D](../v1.9/ui-sdk-fresh-guest-d/host-report.json): **85 checks**, no Lab package before installation, original rotation restored, and the genuinely new guest retained. No pre-existing guest profile is claimed to have been restored.

The longer run earned 100 Journey clears before the later language captures, then replayed earlier boards. **Home resumes L11**, not L101. Rewards still exposes the unclaimed Vision gift at milestone 10; all six sample claims are recorded. Map exploration at level 1,000 does not unlock that level. These capture-specific states are explained on the page instead of being presented as a freshly installed or fully completed account.

All copies are the original native PNG bytes with no crop, resize or pixel editing. The ten localized images, studio and guardian are **1080 × 1920**; the fresh lesson is **720 × 1600**. The HTML reserves those exact dimensions. English-only captures have bilingual descriptions that explicitly identify the English interface. The source [asset registry](release/source-assets.json) retains native AX snapshot hashes, actual capture sequence and observed progress labels.

## Checks

| Evidence | Recorded result | Boundary |
| --- | --- | --- |
| [Release source check](release/static-check.json) | **3,183 assertions passed**, three pages, **71 references** | Offline DOM/copy/file/hash/JS/configuration checks; not browser rendering |
| [Source manifest](release/source-manifest.json) | **71 deployable payload files**, exact SHA-256/bytes | Local source bytes, not served Hosting bytes; reference and payload counts happen to both be 71 |
| [Node contracts](node-tests-final.txt) | **32 tests passed** | Local language and deletion-request doubles; no real Google account or deletion request |
| [Local browser observations](browser-check.json) | **18 FR/EN states passed**: three pages at actual CSS widths320/390/1366 | CUA on localhost, visible language/heading, no horizontal overflow, loaded visible assets; no real account operation |
| [Native release summary](../v1.9/SDK_UI_FINAL.md) | Full play, fresh guest, storage recovery and production startup passed on FINAL5 | Installed Android execution, not human retention, physical speaker audition or live commerce |

The checker accepts current images only from passing `ui-sdk-final5-*` records tied to an APK actually present in releases, or the specifically verified fresh-install D run. A longer run must restore its original profile; a fresh run must prove package absence before install and truthful retention afterward. Each PNG requires its matching passing native result, capture-sequence entry and observed AX snapshot. Earlier failed/superseded candidates are not eligible. No zero-clear or 100-clear state is inferred from a filename or requested run length.

The 1.9 [source-preparation report](source-prepare/static-check.json) and [expected pending-release rejection](release-gate-pending/static-check.json) remain as earlier preparation evidence. The latter deliberately failed while tokens, old image paths and the frozen artifact were absent; it is not the current release verdict. Historical 1.7/1.8 validation folders remain unchanged.

Reproduce current source acceptance:

```sh
python3 scripts/check-site-v19.py --phase release \
  --apk-sha256 b02225c0464bf35cc1c04f64f0a4c8b1db610bde36b293bae9790f1a9e2c2b05
node --test public/tests/*.test.mjs
```

## Shared pages and publication boundary

The privacy copy now identifies 1.9 and describes local guardian/session/style/trial/receipt storage. Privacy and deletion HTML asset-query versions are refreshed to load the current shared stylesheet/controller for returning visitors. The deletion protocol, Firebase identity modules, public runtime configuration, Google asset and separate confirmation boundary remain unchanged. No Analytics SDK, remote font or new network framework is added.

[Public-page documentation](../../PUBLIC_PAGES.md) links the current release and preserved historical delivery records. The [2026 research dossier](../../research/2026-new-player/README.md) records aggregate sources, dates and simulation limits; the homepage does not turn that research into a retention or hit claim. Any later GitHub/Firebase delivery record must identify the frozen source commit and payload manifest separately from local CUA review. No publication or provider approval follows from this source PASS alone.

The [saved French desktop capture](local-home-fr-desktop.png) is a localhost browser render with the exact current native Home image. Earlier reused French navigation still displayed a preparation document; those observations are retained and were not accepted. Explicit reload observed the final source in all six homepage combinations. This is separate from Firebase served-byte/render verification.
