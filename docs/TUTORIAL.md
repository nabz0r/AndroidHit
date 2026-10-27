# Interactive practice — 1.9

## Current native flow · 27 October 2026

The seven-lesson `TutorialSession`, typed `T1` save and one-time **100 earned LumaCoins + two Spark/hint charges** introduced in 1.8 remain intact. Version 1.9 changes how the native UI presents and receives those actions. It does not add practice inventory consumption, campaign wins, a required account, a paid step or another gift on replay. [Release](RELEASE_1_9.md) · [Player audit](PLAYER_AUDIT_2026.md).

The gate lesson had a real compact-screen defect: its illustrated 15-point vertical gesture crossed the page-scroll threshold and cancelled the expected gate preview. The [frozen 1.8 native reproduction](validation/v1.9/ui-baseline-lesson4/README.md) records that failure; the old accessibility click did not cover the same physical gesture. The 1.9 controller reserves the actual typed gate target during the gesture, shows a stationary tap coach for a gate, and offers an explicit Preview control followed by the separate Open confirmation. A tap and a directional piece swipe remain available as appropriate; a wrong-direction piece swipe does not advance or spend an action.

The current **FINAL5 Lab APK** is SHA-256 `acb3e1b0cfa71f84435d6cb0cf0fc73e3d4b434009deef972c54d86d9e7c09c7`. FINAL5 [full-100/menu/Guardian/language acceptance](validation/v1.9/ui-sdk-final5-full100/host-report.json) passes **2,277 checks / 79 PNGs** on the exact Lab APK, API 36 ARM64 with 16 KiB pages, and restores its saved test environment. The [first-install guest D run](validation/v1.9/ui-sdk-fresh-guest-d/host-report.json) passes **85 checks / 17 PNGs** on API 36 ARM64 with 4 KiB pages; Lab was absent beforehand, production absent before and after, its new guest profile remains and rotation settings were restored. [Production startup](validation/v1.9/ui-sdk-production-final5/host-report.json) passes **7 checks / 1 PNG** on the exact production APK and restores its saved environment. These are current-byte native results, separate from provider acceptance.

The [FINAL5 persistent-save fault run 3](validation/v1.9/ui-sdk-final5-storage-fault3/host-report.json) passes **524 checks / 14 PNGs** on the exact Lab APK. Two real permission cycles (`500 → 771`) deny writes and restore access: the pending Guardian layer peel remains on screen when Back is pressed and the physical Retry button saves it once; the reward remains exactly 100 pending coins through a still-blocked retry and Activity recreation, then durably changes **1,830 → 1,930 coins** with its receipt. A repeated claim adds zero. Memory and XML disk state are checked separately, and directory permissions, rotation and the saved test environment are restored. This is Activity recreation with an in-process pending transaction, **not process death while that transaction is pending**. The first two raw failed runs remain unchanged: [fault 1](validation/v1.9/ui-sdk-final5-storage-fault/host-report.json) stopped on the harness rotation wait before any permission lock, and [fault 2](validation/v1.9/ui-sdk-final5-storage-fault2/host-report.json) expected “Retry” while the actual control was labelled “Try again”. Neither failure is relabelled as a pass; run 3 supplies its own completed fault-recovery evidence.

The update preserves exact Studio, guardian-list and active-encounter restoration introduced in FINAL4. It additionally catches typed local-save write/read-flush failures behind a Retry screen, flushes the pending transaction without another grant, blocks Guardian navigation while the attempt is unsaved, and gives the gesture to the actual hit target so an underlying piece cannot swallow Retry. [Source durability tests](validation/v1.9/local-save/README.md) do not replace a native fault run.

The historical **FINAL4 Lab APK**, SHA-256 `f8d554a446d4511baafb4edc3012f4014d0ae420c02e6fffdc29fc58b93a9e08`, passed its [fresh-guest C SDK run](validation/v1.9/ui-sdk-fresh-guest-c/host-report.json): **85 checks / 17 original PNGs**, API 36, ARM64 and 4 KiB pages. Lab was absent before installation; production was absent both before and after. Its new guest test profile remains and rotation settings were restored. It covered the seven lessons, real gate gesture, preview rotation, skip/resume and once-only gift. Its separate [normal full-100 run](validation/v1.9/ui-sdk-final4-full100/host-report.json) passed **2,277 checks / 79 PNGs** and restored the saved test environment. These historical passes do not qualify FINAL5 or its later persistent-save-failure recovery, a real account, ad or purchase.

The retained [fresh-guest B SDK run](validation/v1.9/ui-sdk-fresh-guest-b/host-report.json) is **historical FINAL3 Lab**, SHA-256 `a16fbc03a75455805b7f3f491a8fc26cddc4e33637ec0b8bc1668700e83de36f`. It passed 85 checks, including the real 15-point gate gesture, tutorial preview across actual rotation/activity recreation, skip/resume, single opening after double confirmation, and a once-only welcome gift. Its new guest test profile was retained and its previous production APK was unchanged. That identified guest-flow result remains valid for those earlier bytes; it does not qualify FINAL5, real Google SSO, every physical device, or a restored profile that did not previously exist. Earlier raw global failures remain unchanged.

Portrait and compact layouts keep the board, instruction and current action connected; gate/switch confirmation controls stay within the visible preview. The [current validation index](validation/v1.9/README.md) records other final native runs and any retained pilot failures separately. Do not treat an unfinished hundred-level run or a source test as completed final-byte acceptance.

Later, the Rewards route exposes named five-clear gifts and one optional trial charge per unlocked power. Their receipts are separate from the tutorial completion gift. The new action-based star rule in normal play excludes hint count, elapsed time and raw Undo count; the tutorial therefore teaches help without itself charging inventory or removing a star. Guardian layers and one-cell movement have their own visible encounter explanations and free next-gesture hint; they are **not** extra lessons in the seven-board tutorial. [Guardian rules](GUARDIANS.md).

## Preserved core session contract introduced in 1.8

The source/persistence contract below is retained. Its original JVM counts are source evidence; current native claims must use the identified 1.9 reports above and in the release index.

The tutorial uses seven small, dedicated engine boards. It is a separate `TutorialSession`, with no Android, identity, advertising, purchase, campaign or daily/weekly dependency. The UI must route practice inputs through that session; reading its `Board` for drawing is allowed, directly mutating it is not. Source/JVM qualification is described below; this document alone does not claim installed rendering or touch acceptance.

## What the player actually does

| Lesson | Required interaction | Actual engine effect |
|---|---|---|
| Follow the arrow | Tap the highlighted piece, or swipe along its direction | One real exit; an opposite swipe does nothing |
| Clear the path | Try a genuinely blocked piece, exit its blocker, then exit the first piece | `BLOCKED_PATH` is observed before the path becomes clear; a blocked attempt costs nothing |
| Key and seal | Try the sealed ray, exit the key, then exit the first piece | The fixed barrier is active while its actual key remains, and disappears when that key exits |
| Reopen a gate | Preview the closed upper gate, explicitly open it, then exit upward | The preview is read-only; typed action `64` opens UP and costs one productive action, followed by the piece exit |
| Switch consequences | Preview the switch, exit it, then use the opened edge | The preview discloses UP opening and RIGHT closing, and the real exit produces exactly those changes |
| Undo | Exit a piece, press Undo, exit it again, then finish | V4 Undo restores that piece and removes its preceding action; no extra action or consumable is charged |
| Spark | Press the practice Spark, play its genuinely safe highlighted piece, then finish | The exact engine hint is shown without playing it; no local inventory charge or move is spent in practice |

`advance()` only succeeds when the lesson's requested actions have happened **and** its actual board is solved. It cannot play a piece or skip unfinished objectives. A lesson can be restarted free, the tutorial can be paused with its exact phase retained, and any completed tutorial can be replayed. Those controls do not grant campaign stars, level access, daily/weekly progress or repeat rewards.

Practice has 20 teaching actions and seven explicit lesson transitions. Three intentional observations—blocked path, seal and previews—teach the consequence through the real engine rather than presenting a slideshow. The switch, manual-gate and Undo demonstrations use the V4 action model; historical mini-boards keep their immutable original piece/barrier rules. All tutorial level numbers are standalone identifiers, not campaign unlocks.

## Session API

Sources:

- `app/src/main/java/com/androidhit/lumadrift/onboarding/TutorialSession.java`
- `app/src/main/java/com/androidhit/lumadrift/onboarding/TutorialStrings.java`
- The tutorial methods and one-time local reward in `data/GameStore.java`.

```java
TutorialSession practice = TutorialSession.restore(store.tutorialState());
// Save even a newly opened first step before accepting input.
store.saveTutorialState(practice.encode());

TutorialSession.Action action = practice.move(pieceId); // tap
// Or practice.move(pieceId, swipeDirection), previewSwitch(pieceId),
// previewGate(direction), openGate(direction), undo(), hint(), advance(),
// restartLesson(), skip(), resume(), replay().
if (action.changed) store.saveTutorialState(practice.encode());
```

The action methods **mutate the existing session** and return an immutable `Action`. `accepted` means the requested teaching action matched; for the deliberate blocked attempt this is true while `action.move.success` is false. Only animate an exiting piece when that engine move actually succeeds. `changed` means the new lesson/phase/board record must be saved. `feedbackKey` is a `TutorialStrings` key. `move`, `preview` and `hintTileId` expose the real mechanical result where relevant.

The renderer can read:

- `lessonIndex()` (zero-based), `lessonCount()` (7), `lessonKey()` and `phase()` / `phaseCount()`.
- `board()`, which becomes null only once all lessons are complete.
- `requiredAction()`: `MOVE`, `TRY_BLOCKED`, `PREVIEW_SWITCH`, `PREVIEW_GATE`, `OPEN_GATE`, `UNDO`, `HINT`, `DONE`, `FINISHED`.
- `expectedTileId()` and `expectedGate()` for an accessible focus cue; `instructionKey()` for the current instruction; `lessonComplete()`, `complete()` and `skipped()`.
- `spentActions()` for the actual current history length, `currentPreview()` for a preview surviving recreation, and `highlightedHintId()` for the restored hint cue.

Resolve text with `TutorialStrings.get(language, key, args...)`. The title key is `"title_" + practice.lessonKey()`. All instruction and feedback keys, UI controls, reward/error states and titles have explicit English, French, German, Spanish, Portuguese and Italian entries. Unknown keys throw instead of exposing internal IDs. Unsupported language tags fall back to English; regional tags such as `fr-FR` resolve correctly.

## Persistence, rotation and migration

The bounded `T1` record contains the current lesson, teaching phase, `ACTIVE` / `SKIPPED` / `COMPLETE` state and the **typed engine action history** in one preference value. Gate actions and exits therefore cannot be saved separately from the teaching phase. `restore()` reconstructs the deterministic phase through the same actual actions and rejects mismatched/noncanonical histories, future versions and out-of-range values. Invalid or absent state returns a fresh practice session; it never implies completion or grants a reward. `isValidSave()` allows a caller to distinguish that fallback from a valid first step.

`GameStore.saveTutorialState()` persists the whole record through the existing local transaction helper. Save after every changed action, preview, pause, resume, restart or replay. If persistence throws, show the translated save error and retry before accepting another input. Android may update its in-memory preferences before a failing disk commit: the transaction helper retains and flushes the exact pending record. Immediate process death after failure resumes the last complete disk record. Practice actions have no economic side effects, so repeating a last uncommitted practice action cannot spend stock or farm currency.

`GameStore.shouldAutoStartTutorial()` has two distinct paths:

- An explicit saved `ACTIVE` practice resumes automatically after process/activity recreation, including a veteran who voluntarily opened it.
- An empty state auto-starts only for a local profile first initialized with the 1.8 marker, with no cleared campaign board. Existing initialized saves are not marked retroactively and are never forced into a new tutorial. `SKIPPED` and `COMPLETE` never auto-start.

The UI should keep a visible optional tutorial/resume/replay entry and preserve the player's separate campaign session. Pausing practice is not deleting the campaign run. Before navigating away, persist `skip()`; on a deliberate return, persist `resume()`. Opening a new optional replay must persist `replay()` rather than silently replacing saved practice.

## One-time local gift

Once `complete()` is true, save its canonical finished state before calling:

```java
GameStore.TutorialRewardStatus status = store.claimTutorialReward();
```

The result is `GRANTED`, `ALREADY_CLAIMED`, `STORAGE_FULL` or `NOT_COMPLETE`.

The fixed gift is **100 earned LumaCoins and two Spark/hint charges**, never purchased LumaGold. One transaction writes the completed marker, one-time receipt, exact coin balance and exact hint stock. It has no Firebase UID key: switching between guest and Google on the same local installation/profile cannot produce another gift. Replaying or restarting practice never removes its receipt. Different `GameStore` handles use the same transaction lock.

The gift does not silently clip at the existing coin/stock caps. If there is insufficient room for the full100 coins or both hints, `STORAGE_FULL` leaves the entire receipt unclaimed and retryable after making room. A failed commit throws; it never returns successful collection. A retry flushes the original pending transaction instead of crediting again. If the process dies immediately after a failed disk commit, the last complete on-disk state permits exactly one subsequent collection.

This is a **local installation/profile** earned-item ledger, consistent with the game's current local saves. It is not a server-authoritative cross-device entitlement or a guarantee against clearing application data/reinstalling. It does not open advertising, Billing or paid draws.

## Reproducible source tests

```sh
scripts/test-tutorial-v18.sh
```

The current suite passes **1,454 assertions** against the real session/engine/GameStore, using deterministic Android preference doubles. It checks every teaching phase, exact state reconstruction, typed gate history, read-only previews, blocker/seal behavior, correct and opposite swipes, refunded Undo, safe hint persistence, Next rejection, skip/resume/restart/replay, malformed/future saves, old-player migration, no campaign/event/daily/stock side effects, six-language copy, exact gift amounts, repeat/concurrent claims, storage boundaries, failures before/after Android memory mutation, and immediate process recreation.

The separate existing reward/cosmetic suite also passes **100,953 assertions** after these GameStore additions. These are JVM/source contracts. Native screen layout, accessibility, physical touch/swipe behavior and actual device rotation must be verified separately on the integrated frozen APK; a modelled state reconstruction is not presented as an installed-device rotation test.
