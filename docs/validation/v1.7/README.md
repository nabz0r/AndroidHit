# Luma Drift 1.7 — release evidence

This directory separates source contracts, installed APK behavior and deployment. Final production `1.7.0-rc1` and Lab `1.7.0-sandbox`, code 8, were built and verified on 27 October 2026. Source, package, installed runtime and deployment evidence have separate scopes. Real commerce and two-account qualification remain open.

## Board 13 and the complete route

[Independent exhaustive audit](board-audit.md) verifies all 1,000 Journey and 240 Remix boards in their initial fixed budgets, with no power, ad or purchase. It compares 1,101,868 reachable states and 3,348,959 transitions through 49,063,063 assertions. Historical layouts, codes, certificates and budgets are unchanged. [Journey 13 reproduction](board-audit-journey13.md) explains the legal door-order trap and recovery.

The new `Board.guidance()` distinguishes playable, insufficient-actions and actual dead-end states. Extra moves cannot repair a dead end. The UI uses the exact next safe move, blocks useless hint reservations, signals the trap and offers a free restart; when the current budget allows it, a dialog can undo the calculated prefix. These UX changes passed the installed final Lab checks below.

## Source checks

- [Rewards/cosmetic tests](rewards-cosmetics-tests.txt): 100,953 assertions including date/weekly rollover, idempotent claims, Android memory-before-disk failures, multiple handles and 100,000 cosmetic draws.
- [Map geometry](saga-map-geometry.txt): 172,072 checks of all 1,240 jump positions, viewport heights 266.67–1600, bounded visible ranges, compact header and reachable controls.
- [Audio](audio/README.md): 558 DSP/lifecycle/concurrency checks across ten original biome arrangements. Original theme 0 and eleven effects retain their original hashes; max two tracks during modeled transitions. JVM observations do not establish Android playback.
- [Server](../../../server/validation/social-v1.7/README.md): Node unit/HTTP tests and real Firestore-emulator transactions, quotas, receipts and deletion. Test identities and synthetic purchase records are not real players or payments.

## Pilots and final installed checks

SDK instrumentation uses the native Android framework and real accessible controls, without browser/desktop automation, profile injection, real sign-in, ad loads or payments. The wrapper restores all existing private preferences byte for byte and records the installed APK hash. Failed pilot and initial final-run oracle assertions remain in their own directories. The passing final runs are:

- [Final menus](ui-sdk-final-menus2/host-report.json): 38 assertions, 32 native PNGs. Daily +40 then +0, four slot previews/cancel, earned capsule purchase and reward, guest social and closed premium vault, FR/EN and map 1/13/100/101/1000.
- [Final gameplay](ui-sdk-final-gameplay/host-report.json): 34 assertions, 9 native PNGs. Actual Journey 1–13, safe hint and single charge, undo/restart recovery, insufficient-actions guard, imported fair challenge, landscape. Original preferences and rotation settings restored exactly.
- [Final production](ui-sdk-final-production/host-report.json): 7 SDK assertions. Real launch, advancing AudioFlinger track, background pause and foreground resume; original preferences restored. Audio output was observed in the emulator, not judged on a physical speaker.

Lab APK SHA-256: `d7fea345c1f33cc8e114688ee06d8b60ed1278cc29eeb328c677c6c1858ba049`. Production APK SHA-256: `7eeaa816853533f441acfc73f4ad3f6af0fb2cbdd6fd0eae1fda3f64c388a2bf`. Both installed on API 36 arm64 with actual 16,384-byte pages. All four signed APK/AAB artifacts, native alignment, bundletool schema and stable source hashes passed the [binary audit](binary-verification.txt). [Build/lint](build-lint.txt) completed successfully; 11 nonblocking warnings per variant remain, including dependency notices and deliberate synchronous preference writes.

## Commercial and privacy boundary

Earned LumaCoins, local wardrobe/quests and account LumaGold remain separate. New wardrobe clothes are cosmetic only; the previously shipped eighteen functional gear items keep their explicit limited effects. Earned capsules disclose exact probabilities and duplicate compensation before confirmation. Native and server paid-draw gates remain off; real Play Billing and ads retain their qualification gates. Social friendship sharing is opt-in, and no fake friends or remote success is synthesized for screenshots.

Hosting and Cloud Run deployment, real two-account exchanges, actual Google success, real ad eligibility, payments/refunds and physical-device/player acceptance require their own evidence. No retention, revenue or hit outcome is inferred from these tests.

## Executed server deployment

The existing Cloud Run service was updated from main commit `23a196451a58fbdbbdecca72f3880dcb089f3c12`, exact server tree `83e1699aadaf7be279c8e8b1171cdb4ef99007be`. Revision `luma-wallet-00005-pps` serves 100% of traffic under the same managed identity, with maximum three instances and `PAID_COSMETIC_DRAWS_ENABLED=false`. Health returns 200; unauthenticated social and cosmetic-catalog requests return 401. [Observed management/probe record](cloud-run.json). No two-real-account friendship exchange or payment acceptance is inferred.
