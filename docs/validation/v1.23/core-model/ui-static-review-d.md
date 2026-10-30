# Independent static integration review — D source

Reviewed `app/src/main/java/com/androidhit/lumadrift/LumaView.java`, SHA-256 `d11da9e7c9169b588a88c2cfcd467299c3160146b339aaef45c67e687825ca2e`, after the integration owner corrected the issues below. **No remaining material defect identified in the inspected paths.** This is source review, not an APK/native acceptance result. The reviewer did not modify application sources during this review.

| Observed before correction | D source correction verified |
| --- | --- |
| The activation callback cleared the talent sheet before preparation. A pending planner or absent target then returned with the timer/effects still frozen and no visible sheet. | Line 1671 calls activation without dismissing the sheet. Refusal paths 1701–1707 retain it; successful preparation closes through the committed play transition. |
| Clearing sheet hit targets did not block the separate board swipe path. A swipe over an underlying piece could consume a hidden move. | Lines 3596/3605 reject board touch and hold targeting while the sheet is open; requestAction 3017, inspection 3026 and movePiece 3198 also guard it. Opening the sheet 1645 cancels pending gesture/hold ownership. |
| Newly introduced sheet/wardrobe context and the uncommitted four-slot draft were absent from the Activity Bundle, losing the preview and return context on recreation. | Lines 304–306 save them. Lines 341–354 restore only bounded scroll, owned matching-slot cosmetic IDs and an owned actor. A play sheet additionally requires the same run, history and battle state. The draft remains separate from GameStore. |
| Foreground resume called thawEffects even with the talent sheet retained, allowing hint/vision/trail durations to expire while the sheet stayed open. | Line 3331 keeps effects frozen while battleSheet123 is true. Closing clears that flag before thawing. |

The normal Voyage and Expédition wardrobe routes retain the same board/run and mode: `go` line 383 excludes wardrobe from expedition teardown, while the return method resumes play and restores its saved scroll. Trying owned clothes only changes the draft; Cancel reloads the worn look; Apply invokes the separately host-tested atomic four-slot-plus-costume transaction. Returning without Apply discards the draft. Other companion equipment controls remain outside this in-play draft.

The current six cosmetics per slot fit the two-row content calculation, and footer hit removal uses the same visible coordinate system as its fixed footer. This inspection does not qualify arbitrary future catalogue growth or untested window sizes. The HUD preserves the dedicated inspection/anticipation/double-relay lessons instead of replacing them with the generic hold instruction, and current-policy help uses the two-free-Undo/then-displayed-price explanation.

Native D acceptance still needs to exercise actual sheet refusal/activation, sheet swipes without move mutation, foreground/recreation with a retained sheet, and draft cancel/apply/return from both Voyage and Expédition. The static review does not establish human comprehension, visual appeal or physical-device compatibility.
