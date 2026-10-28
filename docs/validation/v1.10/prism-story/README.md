# Prism notebook source evidence — 1.10

Run `bash scripts/test-prism-story-v110.sh` from the repository root. This compiles the real `PrismStory`, `PrismStoryStrings` and frozen G1 code with Java 17 in a temporary directory. There is no Android preference adapter or device mutation in this test; the new API only reads supplied counts/receipt sets. [Product/API contract](../../../PRISM_STORY_1_10.md).

[summary.json](summary.json) records **308,412 assertions**, all **1,001 integer clear states 0–1000**, ten chapter ranges and six languages / forty keys / 240 texts. Checks cover:

- Guardian unlocks at each exact ten-clear boundary, including 10, 50 and 1000; chapter visibility at 0, 100, …, 900.
- Separate Journey and fragment progress; no receipt inferred from clears or completion inferred from an unlock.
- Sparse ownership, first missing fragment, all receipts, null/duplicate/out-of-range members, integer extremes and invalid chapter identifiers.
- Immutable definitions, snapshots and receipt views; caller mutation cannot change a prior snapshot, and snapshot creation leaves the caller's set unchanged.
- Complete translated text, supported regional-language normalization, English fallback, no unresolved placeholders and a 240-character upper bound per text. This is not visual acceptance or native-speaker review.

[chapter-boundaries.csv](chapter-boundaries.csv) records one normal prefix-receipt snapshot per clear count, including the next missing Guardian; these are deterministic API cases, **not player telemetry**. [six-language-story.csv](six-language-story.csv) contains all 240 authored strings for review.

[source-inputs.json](source-inputs.json) fingerprints the new source/test and proves seven frozen puzzle files identical to the released `ec57167` baseline. [unchanged-structural-backlog.json](unchanged-structural-backlog.json) copies the already measured L22/L23 rows and their source hash. The test separately reads actual G1 definitions: 690 horizontal pieces, zero vertical pieces, Guardian4/5 minima 7/28. It does not change or rerun the complete 6.2-million-state Guardian oracle.

The initial test authoring caught two harness expectation mistakes (a sparse-set count and a conflation of invalid chapter 11 with valid Guardian 11); both expectations were corrected before this recorded successful run. No application change was needed for those failures. Source assertions do not qualify native rendering, storage, provider access, retention or enjoyment. Native notebook acceptance and final binary evidence are separate release work.
