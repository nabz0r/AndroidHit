# Luma Drift 1.9 — executed qualification

Production **1.9.0-rc1 / code 10**, separate Lab **1.9.0-sandbox / code 10**. [Exact release and hashes](../../RELEASE_1_9.md) · [Research-linked player audit](../../PLAYER_AUDIT_2026.md) · [Website qualification](../site-v1.9/README.md).

## Source, economy and packages

| Layer | Executed result | Evidence |
| --- | --- | --- |
| Same first-100 puzzles before/after | 16,000 independent policy trials and 72 sequential synthetic journeys per version; 7,216 sequential attempts and 814 saved/recreated interruptions in the candidate | [Comparison and method](journey/README.md) |
| Ordinary route exact states | PASS again: all 1,240 L/M boards, 13,254,058 states, 57,403,554 transitions, 773,092,810 assertions | [Executed V4 log](engine/strategy-all-states.txt) |
| New guardians | PASS: 100 unique encounters, 6,197,718 productive states, 34,045,573 transitions, 472,484,607 assertions | [Independent guardian summary](journey/guardians/summary.json) |
| Synthetic guardian profiles | 183,733 checks; 720 encounters resolved after 7,200 actual JVM Journey clears, 791 cold restores, plus 18 forced edge cases | [Declared profiles and trace](journey/guardian-profiles/README.md) |
| Gift/trial/grading policy | 65,736 checks, stable previews, historical receipts, explicit one-unit samples, full inventory and durable retries | [Policy implementation](rewards/IMPLEMENTATION.md) |
| Guardian/store/style integration | 5,146 checks; 100 actual solved encounters before reward requests, once-only claims, concurrency and storage failures | [Store integration](journey/guardian-store/README.md) |
| Original avatar renderer | 7,249 recording-Canvas contracts; 288 combinations/five poses; 14 native raster exports from namespace-only source copies | [Renderer evidence](avatar/README.md) |
| Persistent local-save failures | 229 assertions/13 scenarios, 44 failed commits, typed read/write errors, exact pending retry and once-only grants | [Typed recovery and seven current reruns](local-save/README.md) |
| Existing systems | Thirteen grouped host suites pass; source-only identity/social/vault tests are separate reports, without real providers | [Regression summary](regression/summary.json) |
| Build/lint | Both release variants and bundles build; each release lint has zero errors and 12 warnings | [Build log](build-lint.txt) |
| Four binary outputs | Signatures, official bundletool validation, 16KB ZIP and native ELF alignment pass; app inputs remain stable | [Final binary audit](binary-final-candidate.json) |

The source first-100 simulation uses behaviour/error assumptions and declared assistance. The 2026 survey anchors are real aggregate research; they are not a calibration of those errors, an Android-only global population or observed Luma players. No D1/D7, revenue, social-account acceptance or acquisition forecast is inferred.

## Native regression history

The lesson-4 bug was reproduced on the exact published Lab 1.8 APK: the illustrated 15-point gate gesture was interpreted as page scrolling. [Before-fix host report](ui-baseline-lesson4/host-report.json) records the expected failing action, with original preferences/display/rotation restored. The preceding pilot stopped earlier because an old test helper assumed an offscreen skip button; its separate raw failure is retained.

A code-10 pilot fixes the same physical gesture and completes the seven lessons, rotation, skip/resume and once-only gift with 85 checks, but has a different APK hash from the final candidate. Its report remains [ui-sdk-pilot-compact](ui-sdk-pilot-compact/host-report.json), without a final-byte claim.

A following compact run discovered the real L11 preview layout bug after 232 assertions: its primary controls were below the visible window. The [raw failing report](ui-sdk-final-full100/host-report.json) is retained. That run is **FAIL**, despite its historical folder name. The final source pins this modal inside the viewport and exposes Confirm/Cancel, without needing an automatic test scroll.

The FINAL3 run reached all 100 levels and passed 300 individual clear/grade/budget assertions, then stopped on a combined harness assumption about an offscreen guardian reward button. Its [raw failure and extracted individual results](ui-sdk-final3-full100/README.md) remain explicitly scoped. A subsequent run found a real rotation bug: the active guardian save survived, but its screen reopened as Home. That [raw failing report](ui-sdk-final3-menus/host-report.json) is retained. The final source now restores the guardian screen from its bounded save (and pending Bundle state), plus the style and guardian menus, while preserving scroll.

The selected FINAL5 candidate has new signed bytes. Its [full native run](ui-sdk-final5-full100/host-report.json) passes **2,277 checks**, including all 100 Journey clears, visible grading/budgets, gate gestures, power charges, reward receipts, twelve styles, guardians 1/5, rotation and six languages; 79 original PNGs are retained. The [genuine fresh-install D run](ui-sdk-fresh-guest-d/host-report.json) passes **85 checks** on an independently created Google Play emulator with 4KB pages. The Lab package and production package were absent before installation; the newly earned guest save is retained. The [production startup run](ui-sdk-production-final5/host-report.json) passes **7 checks** on 16KB-page Android, including actual process-owned audio frame progress and background/foreground suspension/resumption. Existing preferences/display/rotation are restored byte-for-byte by the normal and production wrappers; the genuine first-install run retains its new save instead.

The [FINAL4 full run](ui-sdk-final4-full100/host-report.json) had also passed 2,277 checks, but remains superseded. Review found hidden-piece interception of Retry and unsafe navigation during a failed guardian save, plus a persistent local-transaction error that could escape rendering. FINAL5 uses typed visible hit ownership, save-gated navigation and a typed full-screen retry boundary. The separate [real-permission storage-fault run](ui-sdk-final5-storage-fault3/README.md) passes **524 checks** with 14 original captures. After 30 actual Journey clears it exercises two permission-denied writes: a pending guardian peel with blocked navigation, then a solved guardian’s once-only 100-coin reward. Repeated retry while locked, physical retry at the formerly intercepted target, rotation and exact cache/disk reconciliation pass. Both directory modes and the original profile/settings are restored. This covers Activity recreation and these write refusals, not process death while a pending receipt exists only in memory. Its first preflight stopped during an insufficient rotation wait; the second reached the real blocked write but stopped because the harness expected “Retry” instead of the actual English “Try again”. Both raw failures and restoration evidence remain. The [native art atlas](avatar/README.md) passed using unchanged renderer source bodies, separately from the app screens. [Installed-APK summary](SDK_UI_FINAL.md). Publication receipts are added only after delivery.

The [pilot screenshot curation manifest](pilot-screenshot-curation.json) lists redundant unpublished superseded-candidate PNGs omitted from the release tree. Raw reports, their original capture hashes, the reproduced gate evidence and failure illustrations remain; final-byte qualification images are untouched. A report's original capture count is not a count of the curated illustrations retained in Git.

## Boundaries

Host preference doubles exercise memory-before-disk failures but do not inject physical flash faults. Android screenshots/gestures establish the identified app states, not human appeal. Physical low-end phones, speaker audition, full spoken TalkBack, expanded-font combinations and real player comprehension/retention remain to measure. Google identity, friends/gifts between real accounts, live advertising, Play purchases/refunds and paid draws remain separately gated. No server code or deployment changed in this gameplay release.
