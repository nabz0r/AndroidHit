# Luma Drift 1.10 qualification

Final signed candidates: production **1.10.0-rc3**, Lab **1.10.0-sandbox-rc3**, code 11. [Release notes and full hashes](../../RELEASE_1_10.md).

[Source and four public downloads verified](publication.json): source commit e785b115, HTTP 200 and exact tested sizes/SHA-256. That dated Android delivery receipt changes documentation only and retains its earlier Hosting context. The subsequent [1.10 Hosting confirmation](../site-v1.10/deployment.json) is separate.

## Final candidate evidence

| Report | Executed result |
|---|---|
| [Full installed route](ui-sdk-final3-full100/host-report.json) | PASS **2,514 checks / 96 original PNGs**; exact Lab SHA `d1b085b4d9bbcf450037069ed15c7cea89b0e3e759b16e587cf3ca016a7c493f`; native SDK/accessibility/touch actions; Journey1–100, tutorial, recovery, powers, rewards, collection, story, guardians, rotation, six languages; guest files and rotation restored. |
| [Fresh guest I](ui-sdk-fresh-guest-i/host-report.json) | PASS **91 checks / 17 PNGs**; exact final Lab bytes on a real first installation, no previous Lab/production package and no profile injection. Guest profile preserved. |
| [Production startup](ui-sdk-production-final3/host-report.json) | PASS **7 checks / 1 PNG**; exact production SHA `29a68cdcaeb61850d55afd101f03bd8c42a075cc6d6631205fc176e499dd70b4`, launch and digital audio frame advance/pause/resume; guest files restored. |
| [Native acceptance details](SDK_UI_FINAL.md) | Explicit fixture provenance, runner/API boundaries, 300 individual Journey assertions, 14 original pixel samples and four fully visible footer controls. |
| [Build and lint](build/gradle-release-lint.txt) | Both signed APK/AAB variants build; **0 errors and 11 retained warnings per flavor** in [lint summary](build/lint-summary.json). |
| [Binary audit](binaries/binary-final-candidate.json) | Certificate verification, official bundletool and static16KiB ZIP/ELF checks for the four exact RC3 outputs. |
| [Engine compatibility](engine-compatibility.json) | 25 pre-existing engine/catalog sources byte-identical to ec57167; no human-difficulty inference or rerun of every historical simulation. |
| [Store/objective models](objectives/) | 443 dedicated assertions and seven regression suites, scoped to the final source hashes. |
| [Portrait checks](avatar/) / [Story checks](prism-story/) | 6,544 command/API checks and 308,412 story assertions; no raster/FPS or human comprehension claim. |
| [Readability matrix](ux-readability/) | Screen/locale coverage and explicit limits; source layout and native observation remain separate. |

Original final PNGs are in [full route](../../screenshots/v1.10/ui-sdk-final3-full100/), [fresh I](../../screenshots/v1.10/ui-sdk-fresh-guest-i/) and [production](../../screenshots/v1.10/ui-sdk-production-final3/). These are SDK screenshots, not mockups. No screenshot is edited to invent a player, reward, ownership or account state.

## Intermediate observations retained

- First development Lab `aee689…a9a5`: fresh F passed85 checks; the companion pilot stopped after202 at an earned-gift prerequisite because its selected cases had not cleared Journey. It restored the original guest files. This is not final evidence.
- RC1 Lab `70e693…72f4`: fresh G passed91/17; full100 passed2,472/92; RC1 production startup passed7/1. These runs predate the selected-tab palette and footer corrections.
- RC2 Lab `ad6acd…3a78`: fresh H passed91/17 and full100 passed2,500/92. Fourteen selected-control pixel checks passed. Footer clipping found by visual review was corrected for RC3; RC2 is not the final release.
- [RC1→RC2 diff](rc2-diff.json) records opaque ARGB conversion and journal portrait placement. [RC2→RC3 diff](rc3-diff.json) records footer geometry and version labels. [Palette source regression](palette-boundary.json) and [footer geometry](footer-geometry.json) retain the reproduced mechanisms.
- [Intermediate artifact retention](intermediate-retention.json) records original RC1/RC2 manifests and eight byte-preserved local binaries. No intermediate binary is presented as the published RC3 download.

## Limits and website

These runs establish the recorded emulator behavior, not enjoyment, retention, global difficulty calibration, physical-device performance or production-service acceptance. Existing L23/G5 difficulty discontinuities and horizontal G1 pieces remain documented. No paid draw, real checkout, real Google account or two-account social acceptance was executed.

[Website validation](../site-v1.10/) checks the RC3 source/gallery/download linkage locally. Firebase Hosting 1.10 is confirmed at 28 October 2026, 10:44:28.535 UTC after 89 source payload hashes matched on the clean deployment checkout. [Delivery receipt](../site-v1.10/deployment.json). Local browser review covers 12 DOM states and 6 accepted desktop rasters; incomplete mobile provider captures stay excluded from visual acceptance. No public-origin served-byte assertion is made.
