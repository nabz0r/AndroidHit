# Interactive practice — 1.8

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
