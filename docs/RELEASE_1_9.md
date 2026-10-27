# Luma Drift 1.9 — a tested first journey and Prism guardians

Production **1.9.0-rc1 / code 10** and isolated **1.9.0-sandbox / code 10**. Android 8+ / API 26, target 36. This is a gameplay candidate; real advertising, checkout and paid cosmetic draws remain closed.

## Delivered changes

- Fixed the reproduced lesson-4 finger/scroll conflict. The demonstrated touch is owned by its real gate target, with a static tap coach, an explicit preview action and visible compact confirmation.
- Fixed the separately reproduced L11 compact-window bug: gate/switch previews now stay inside the visible window, including Confirm and Cancel, independent of page scroll. Tap and scroll thresholds share the same boundary.
- Fixed the later native rotation/recreation regression, first corrected in **FINAL4**: Avatar Studio and the guardian list restore their exact screen, and an active guardian restores its typed attempt and pending save-error state instead of falling back to Home. This changes UI restoration, not the frozen puzzle geometry or reward policy.
- FINAL5 adds a typed durable-save recovery screen for repeated local write/read-flush failures. Retry flushes the existing pending transaction rather than granting it again. Guardian navigation waits for a successful save, and native gestures use the actual hit target so the Retry button cannot be swallowed by an underlying piece. These paths have separate source fault tests; native FINAL5 fault qualification remains distinct.
- Journey gifts are collectable where Home sends the player, and directly from victory. Previewed gift contents are stable even when collected later. Each of six powers offers one explicit free sample at its existing unlock level.
- Stars measure the initial action budget: three within minimum plus half the margin, two within the original allowance, one after an extension. Hints, elapsed time and Undo count are not score inputs.
- Added **100 optional guardians**, one after each ten Journey clears. Thick two-layer pieces appear from Guardian 1; one-cell pieces from Guardian 5. An actual work meter, one saved active guardian attempt, free recovery and a once-only 100-LumaCoin fragment support the story.
- Added **288 visual combinations**: the 24 existing companions across 12 free original styles, five poses and a scene that responds to actual play progress. Character ownership remains required; treatments grant no advantage. Reduced motion disables their movement.
- Audited the tutorial and first 100 levels with real engine/economy code and six explicitly synthetic behaviour profiles, grounded in separately dated public aggregate research published in 2026. No demographic error rate, player cohort or D1/D7 result is invented.

[New-player findings and sources](PLAYER_AUDIT_2026.md) · [UX contract](UX_1_9.md) · [Guardian rules](GUARDIANS.md) · [Avatar drawing/atlas evidence](validation/v1.9/avatar/README.md).

## Exact signed packages

| Channel | File | Bytes | SHA-256 |
| --- | --- | ---: | --- |
| Production | [APK](../releases/luma-drift-1.9.0-rc1.apk) | 7740394 | `b02225c0464bf35cc1c04f64f0a4c8b1db610bde36b293bae9790f1a9e2c2b05` |
| Production | [AAB](../releases/luma-drift-1.9.0-rc1.aab) | 11110771 | `71ee11a6cc06d4342c09b09065c8500986a80bac8700da9cc98004a9df000e41` |
| Lab | [APK](../releases/luma-drift-1.9.0-sandbox.apk) | 7723693 | `acb3e1b0cfa71f84435d6cb0cf0fc73e3d4b434009deef972c54d86d9e7c09c7` |
| Lab | [AAB](../releases/luma-drift-1.9.0-sandbox.aab) | 11102455 | `ce4b35a5e59fa91ebf67bf4bc41d66ea478e58e1cadce63dc1cba10733820fc7` |

The public signing certificate is unchanged: SHA-256 `dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287`. [Binary verification](validation/v1.9/binary-final-candidate.json) separately records signatures, official bundletool validation and 16KB ZIP/native ELF alignment.

## Scope of the evidence

The package table identifies **FINAL5**. FINAL5 [full-100/menu/Guardian/language acceptance](validation/v1.9/ui-sdk-final5-full100/host-report.json) passes **2,277 checks / 79 PNGs** on the exact Lab APK, API 36 ARM64 with 16 KiB pages, and restores its saved test environment. The [first-install guest D run](validation/v1.9/ui-sdk-fresh-guest-d/host-report.json) passes **85 checks / 17 PNGs** on API 36 ARM64 with 4 KiB pages; Lab was absent beforehand, production absent before and after, its new guest profile remains and rotation settings were restored. [Production startup](validation/v1.9/ui-sdk-production-final5/host-report.json) passes **7 checks / 1 PNG** on the exact production APK and restores its saved environment. These are current-byte native results, separate from provider acceptance.

The [FINAL5 persistent-save fault run 3](validation/v1.9/ui-sdk-final5-storage-fault3/host-report.json) passes **524 checks / 14 PNGs** on the exact Lab APK. Two real permission cycles (`500 → 771`) deny writes and restore access: the pending Guardian layer peel remains on screen when Back is pressed and the physical Retry button saves it once; the reward remains exactly 100 pending coins through a still-blocked retry and Activity recreation, then durably changes **1,830 → 1,930 coins** with its receipt. A repeated claim adds zero. Memory and XML disk state are checked separately, and directory permissions, rotation and the saved test environment are restored. This is Activity recreation with an in-process pending transaction, **not process death while that transaction is pending**. The first two raw failed runs remain unchanged: [fault 1](validation/v1.9/ui-sdk-final5-storage-fault/host-report.json) stopped on the harness rotation wait before any permission lock, and [fault 2](validation/v1.9/ui-sdk-final5-storage-fault2/host-report.json) expected “Retry” while the actual control was labelled “Try again”. Neither failure is relabelled as a pass; run 3 supplies its own completed fault-recovery evidence.

Historical FINAL4 Lab SHA-256 `f8d554a446d4511baafb4edc3012f4014d0ae420c02e6fffdc29fc58b93a9e08` passed the [fresh-guest C run](validation/v1.9/ui-sdk-fresh-guest-c/host-report.json), **85 checks / 17 PNGs**, and the [normal full-100 run](validation/v1.9/ui-sdk-final4-full100/host-report.json), **2,277 checks / 79 PNGs**. C was a first Lab installation with production absent before and after; its new guest profile remains and rotation settings were restored. The full run restored its saved test environment. These results do not cover FINAL5 or the later persistent-save failure fixes. Historical FINAL3 evidence, including fresh-guest B on Lab SHA-256 `a16fbc03a75455805b7f3f491a8fc26cddc4e33637ec0b8bc1668700e83de36f` and all retained raw global failures, keeps its original scope.

The first-100 audit performs 16,000 independent trials and 72 sequential synthetic journeys per version, with the same seeds and assumptions before/after. The FINAL5-source candidate trace passes 1,153,590 assertions and includes 7,216 sequential attempts and 814 restored interruptions. It uses actual Board, MoveBudget, TutorialSession, GameStore and MetaProgress code with declared Android preference adapters. [Comparison](validation/v1.9/journey/comparison.json). These are controlled robustness/economy experiments, not observed human sessions or a retention forecast.

All 1,240 published L/M geometries and their budgets remain unchanged. Their exact reachable-state audit passes again. The structural spike near L23 is identified and contextual help remains available; human difficulty has not been calibrated by a demographic survey. Historical challenge families and old receipts are retained.

The guardian oracle passes 472,484,607 assertions over all 100 encounters. The separately rerun [optional-Guardian profiles](validation/v1.9/journey/guardian-profiles/README.md) pass 183,733 assertions over 720 programmed encounters on the current source. The [durable-save source tests](validation/v1.9/local-save/README.md) pass 229 assertions across 13 scenarios and 44 injected failures, including consecutive write and read-flush failures; these do not replace a native fault run. The [executed validation index](validation/v1.9/README.md) separates source audits, identified native APK runs, retained pilot failures, actual Android atlas exports, production startup and website publication. A passed source test does not certify real Google accounts, gifts between accounts, ads, purchases, physical speakers or low-end performance.

The owner reports the **Play developer account approved on 27 October 2026**. Creating the Play application, uploading the signed AAB and completing provider/commerce qualification remain the next steps. No Play app or public store release is claimed by this GitHub delivery. Existing Cloud Run code and service configuration are unchanged.
