# The Prism notebook — 1.10 source contract

The notebook gives Nova ten original chapter introductions, objectives and endings, aligned with the ten existing 100-level Journey biomes. It reads existing Journey clear counts and **claimed Guardian reward receipts**. It grants nothing, records nothing and changes no board, action budget, Guardian, account or saved game. Native presentation and release qualification are separate from this source contract.

The story follows First Light, Mint Meadows, Peach Coast, Cloud Gardens, Crystal Lagoon, Star Orchard, Moon Festival, Pumpkin Grove, Rose Aurora and Dream Summit. Each chapter has its own small scene, a short objective and an ending. The language is about exploration and return at the player's pace; it introduces no deadline, expiring fragment, new collectible or reward. Forty keys provide 240 texts in English, French, German, Spanish, Portuguese and Italian. These are authored translations; automatic coverage checks are not native-speaker review or installed readability evidence.

## Progress comes from existing facts

| Chapter | Existing Journey band | Opens after distinct clears | Guardian receipts counted |
| --- | --- | ---: | --- |
| 1 | 1–100 | 0 | 1–10 |
| 2 | 101–200 | 100 | 11–20 |
| 3 | 201–300 | 200 | 21–30 |
| 4 | 301–400 | 300 | 31–40 |
| 5 | 401–500 | 400 | 41–50 |
| 6 | 501–600 | 500 | 51–60 |
| 7 | 601–700 | 600 | 61–70 |
| 8 | 701–800 | 700 | 71–80 |
| 9 | 801–900 | 800 | 81–90 |
| 10 | 901–1000 | 900 | 91–100 |

Every chapter shows two different facts: Journey progress `0..100`, and claimed fragments `0..10`. Completing a hundred Journey levels does not claim their optional Guardian rewards. A chapter ending becomes complete only when all ten of its existing Guardian receipts are supplied. The story does not equate an unlocked or merely solved Guardian with a collected fragment.

A Guardian keeps its original threshold of `number × 10` Journey clears. The first unlock is 10, the one-cell encounter unlock is 50 and Guardian 100 requires 1000. Notebook chapter visibility is different: chapter 1 is visible immediately, chapter 2 at 100, through chapter 10 at 900. No map coordinates are generated; the UI uses the existing level/Guardian numbers and map layout.

## API for the notebook and map

- [`PrismStory.chapters()`](../app/src/main/java/com/androidhit/lumadrift/engine/PrismStory.java) returns ten immutable definitions, each with Journey/Guardian bounds, the existing `biome_0..9` key and `titleKey`, `storyIntroKey`, `storyAfterKey`, `objectiveKey`.
- `PrismStory.snapshot(journeyClears, receipts)` takes a defensive copy and returns immutable chapter progress. `snapshot.forChapter(1..10)` and the static `PrismStory.forChapter(number, journeyClears, receipts)` expose a specific chapter. Invalid chapter identifiers throw `IllegalArgumentException` rather than silently selecting another chapter.
- `snapshot.currentChapter()` follows the current hundred-clear band: 0–99 → 1, 100–199 → 2, and 900–1000 → 10. It does not force an unfinished older Guardian into the current map position.
- `nextGuardian()` returns the first missing receipt, globally or in that chapter. It may identify a **locked** Guardian; callers must check `snapshot.guardianUnlocked(number)` before offering Play. Zero means all receipts in the applicable range are present. `guardianClaimed(number)` reads ownership; it does not solve an encounter.
- [`PrismStoryStrings.get(locale, key)`](../app/src/main/java/com/androidhit/lumadrift/data/PrismStoryStrings.java) resolves the forty story keys. Regional language tags normalize through the existing locale policy; unsupported languages use English. Unknown keys are rejected.

Clear input is clamped to `0..1000`, including integer extremes. Null receipt input means none. Null/out-of-range receipt members are ignored, and duplicate identifiers count once. Receipt ownership and clear count remain independent supplied facts: a retained receipt is not erased merely because a caller provides a lower clear count. This is a read-only display model, not a validator of forged receipts or a server purchase authority. The caller must provide actual local receipt ownership. Input collections, existing receipts and preferences remain untouched.

## Tested boundaries and scope

Run `bash scripts/test-prism-story-v110.sh`. [Executed source report](validation/v1.10/prism-story/summary.json) and [method/outputs](validation/v1.10/prism-story/README.md) cover **308,412 assertions** across **1,001 distinct clear counts (0–1000 inclusive)**, all ten chapters, the ten-clear Guardian thresholds, sparse/out-of-order/duplicate/invalid inputs, defensive-copy immutability and six-language coverage. No profile values are seeded into an app and no Google, ad, wallet or purchase is exercised.

The runner compares seven frozen puzzle source files byte for byte with released baseline `ec57167`: L/M/T catalog definitions, budgets, Board, challenge-code routing and both G1 sources. [Fingerprints](validation/v1.10/prism-story/source-inputs.json). Native notebook layout, touch/scroll, large text, rotation and return navigation require their own identified APK acceptance after UI integration. This report does not establish human comprehension, pleasure, difficulty calibration or retention.

## Unchanged structural backlog

The first-100 baseline still has an L22→L23 structural score increase **17→85**, choice points **0→8**, minimum **14→11** and initial action budget **17→14**. Those are source-derived dependency/choice measures, not human failure probabilities. [Preserved input rows and provenance](validation/v1.10/prism-story/unchanged-structural-backlog.json). Earlier briefing, switch previews and free recovery remain; this notebook does not rebalance the curve.

The frozen G1 catalog contains **690 pieces across 100 encounters, all horizontal**. Its rows point toward one edge, and each productive action reduces remaining work by exactly one. Guardian 4 requires **7** minimum actions; Guardian 5 requires **28** when one-cell movement appears. Existing oracle evidence establishes their solvability, not variety or how they feel. These sources remain unchanged in 1.10.

A later study should observe whether people predict switch effects, distinguish paid action from free preview, understand zero-budget Undo and recognize one-cell movement. A mixed-axis Guardian family or extra mechanics workshop requires a separately versioned proposal and new solvability/save tests. Neither is included in this release scope. No improvement in measured retention, enjoyment or a human Candy Crush-like curve is claimed.
