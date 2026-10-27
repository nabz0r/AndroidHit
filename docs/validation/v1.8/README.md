# Luma Drift 1.8 — executed evidence

Production **1.8.0-rc1 / code 9** and Lab **1.8.0-sandbox / code 9**. [Release and exact binaries](../../RELEASE_1_8.md) · [Website qualification and delivery](../site-v1.8/README.md).

## What was checked

| Layer | Executed result | Evidence |
| --- | --- | --- |
| New engine/catalogue | PASS: 773,092,810 assertions; 13,254,058 reachable states; 57,403,554 legal transitions; all 1,240 L/M boards and finite 980-geometry T closure | [Independent models and methodology](engine/README.md) |
| Existing engine compatibility | Historical families unchanged; engine, budget, seals and J/K regressions pass | [Engine records](engine/README.md#preserved-rules) |
| Data, economy and adapters | All 15 host suites exit successfully; provider/preference doubles are explicitly scoped | [Regression summary](regression/summary.json) |
| Build and lint | Both release variants build; each lint report has 0 errors / 12 warnings | [Build log](build-lint.txt) |
| Binary integrity | Both APKs and AABs pass signatures, official bundletool validation and 16KB ZIP/native ELF alignment | [Final candidate verification](binary-final-candidate.json) |
| Android mechanics | First 100 Journey boards actually completed; 2,015 core assertions pass; whole original harness remains FAIL due to later wardrobe-selector mismatch | [Raw full report](ui-sdk-final-full/host-report.json) |
| Corrected menu procedure | Separate PASS: 47 checks, six languages and 37 native PNGs; identical game APK | [Menu report](ui-sdk-final-menus/host-report.json) |
| Fresh arrival | Separate PASS: 77 checks on a second newly installed Lab; actual automatic tutorial, skip/resume, reward-once and directional touches | [Fresh-install report](ui-sdk-fresh-guest/host-report.json) |
| Installed production | Separate PASS: 7 checks; visible startup, 22,491 digital audio frames, pause/resume | [Production report](ui-sdk-final-production/host-report.json) |
| Local website | 2,508 source assertions, 32 JS tests, FR/EN pages at 320/390/1366 CSS widths and an original final browser capture | [Site evidence](../site-v1.8/README.md) |

[SDK_UI_FINAL.md](SDK_UI_FINAL.md) explains the raw failure, corrected runner, exact APKs, actual gestures, temporary profile restoration and the intentionally retained fresh guest. [CURATION.json](CURATION.json) inventories original capture hashes; omitted redundant PNGs have local-only archives rather than invented downloadable links. Pilot failures remain in their own directories.

## Integrity and commercial boundary

Production APK SHA-256: `aab583241c42179e5cffb80bdd32f9a502be124a3e8a75ccf94936e538db87d4`.

Lab APK SHA-256: `f93467a11073a0f01c9b09ae6e73aebe211f700661a21c54097cebf1063a034d`.

The [binary source manifest](binary-final-candidate.json) fixes the application inputs used for the final artifacts. Source simulation, installed UI behavior, visual review and human experience are separate evidence. No physical handset, speaker audition, full spoken TalkBack review, complete UI playthrough of all 1,000 levels, real Google account, friend/gift transaction, live ad or real purchase is claimed. Paid draws, real checkout and live advertising remain closed. The server was not changed or redeployed for this release. Human difficulty and retention still need observed cohorts.

Publication and independent download hashes are recorded after delivery; the signed files above are the frozen candidates under review.
