# Luma Drift1.9 — final installed-APK SDK qualification

All four final behavioral suites below passed on the same **FINAL5** game bytes. Every screenshot is an original Android UiAutomation capture; no player progress, stock or receipt was injected. This is installed-APK execution, separate from compilation, source-oracle simulations and human playtesting.

| Suite | Actual runtime | Checks | Original captures | Verdict |
|---|---|---:|---:|---|
| [Full tutorial→100, menus and mechanics](ui-sdk-final5-full100/README.md) | API36 ARM64,16KiB, QA5580; compact1080×1920 and rotations |2277|79|PASS; preference bytes/file set, size and rotation restored |
| [True first installation D](ui-sdk-fresh-guest-d/README.md) | Official Google Play API36 ARM64,4KiB, new QA5588 |85|17|PASS; original rotation restored, real guest retained |
| [Two actual storage failures](ui-sdk-final5-storage-fault3/README.md) | Official Google APIs API36 ARM64,16KiB, new QA5590;30 real clears |524|14|PASS; both permission cycles and original profile/settings restored |
| [Production startup/audio](ui-sdk-production-final5/README.md) | API36 ARM64,16KiB, QA5580 |7|1|PASS; existing production profile restored |

## Exact game artifacts

- Lab1.9/code10 APK: `acb3e1b0cfa71f84435d6cb0cf0fc73e3d4b434009deef972c54d86d9e7c09c7`.
- Production1.9/code10 APK: `b02225c0464bf35cc1c04f64f0a4c8b1db610bde36b293bae9790f1a9e2c2b05`.

Each host report verifies the actual installed hash against the frozen artifact and binds the independent SDK test APK and source snapshots. Signature/package/Bundle checks remain separate release evidence. The test APKs are independent packages; no QA hook is packaged into the game. The precise [harness method and reproduction commands](../../../scripts/v19-ui-test/README.md) explain their ownership and guest-only restrictions. Only explicitly owned QA serials are accepted; prior user emulator5582 is untouched.

## What was actually exercised

The full run played all100 levels through actual native controls and certified typed actions. Its [individual JSON](ui-sdk-final5-full100/journey-100-individual.json) and [CSV](ui-sdk-final5-full100/journey-100-individual.csv) contain300 passed clear/visible-three-star/budget assertions from the passing suite. Seven practice lessons, optional skip/resume, genuine wrong/right SDK drags, the coached15-logical-point gate gesture, visible previews, zero-spend cancellation, physical confirmation/double confirmation, free Undo at zero actions, earned powers, milestone-stable gift, all6 explicit trial claims,12 free styles, actual guardian layers/cellwise movement/restore/one-time rewards, native scrolling, compact landscape and6 languages passed.

First installation D proves the automatic optional invitation on a genuinely new Lab profile, its actual teaching boards and one-time100 LumaCoins+2 Sparks reward. Production was absent before and after on that new device; null hashes explicitly represent absence, not a claimed production APK comparison. Its newly created local guest was retained, not erased to manufacture freshness.

The separate fault suite first earns Guardian3 through30 real clears. Two bounded0500 permission locks prove previous disk state remains unchanged when the real app's write fails. Physical retry, blocked Back, repeat retry under lock, actual Activity recreation, exact pending hit, and the100-coin receipt/balance recover without duplication. It observes both the cache and the actual XML disk file. The infrastructure controller restores the exact original771 mode in finally before preference restoration. This is not preference-value preparation.

The production observer found the actual target process's music track advancing **22491 mixed frames**. It paused on a real background lifecycle and resumed on return. That is digital playback evidence, not physical speaker audition. It invoked no game/menu/account/ad/purchase control.

## Retained failures and scope limits

The unchanged1.8 [gate-coach baseline](ui-baseline-lesson4/README.md) remains a raw expected FAIL:15-point drift was canceled at phase0. Intermediate candidates retain the [offscreen preview failure](ui-sdk-final-full100/README.md) and [actual guardian rotation restoration failure](ui-sdk-final3-menus/README.md). FINAL4 passed its own bytes and remains explicitly superseded. Two FINAL5 fault-harness attempts also remain raw FAIL, with [rotation timing](ui-sdk-final5-storage-fault/README.md) and [actual-label selection](ui-sdk-final5-storage-fault2/README.md) explanations; no app fix was inferred from them. Original report hashes and the [pilot PNG curation manifest](pilot-screenshot-curation.json) distinguish original capture counts from curated illustrations in Git.

These suites do not measure human retention, age-specific behavior, spending or hit potential. Source-certified paths are not human difficulty observations. Synthetic cohort research is documented separately. Storage tests cover permission-denied writes and Activity recreation, not process death while a receipt exists only in memory. Google SSO, live advertisements and real purchases were not exercised by these guest-only runs. The [final rewards JVM report](rewards/implementation-final5-tests.json) separately passes65736 assertions on the actual data/scoring classes, with source hashes and deterministic Android preference doubles; it is not Android execution evidence.
