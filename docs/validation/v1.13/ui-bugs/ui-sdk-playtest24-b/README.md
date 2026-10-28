# Native simulator audit: Journey 1–24

This is a measured product-audit replay on the unchanged **1.13.0-sandbox-rc2 / code 14** APK, not a new release. The runner knows the certified solutions and executes actual native touchscreen controls; it does not inject the profile, puzzle, currency or progression.

**PASS: 410 native checks, 50 original full frames.** Each of the 24 levels has a real departure and three-star victory capture at 1080×2340. Original guest preferences are restored byte for byte, original rotation is restored and the isolated hometest package is removed.

| Level | Grid | Pieces | Played actions | Gate opens | Switch exits | Allowance | Margin | SDK known-solution s | Win controls |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|
|1|5×5|5|5|0|0|10|5|2.207|5|
|2|5×5|6|6|0|0|11|5|2.180|5|
|3|5×5|8|8|0|0|13|5|3.034|5|
|4|5×5|8|8|0|0|13|5|2.948|5|
|5|5×5|8|8|0|0|13|5|2.543|6|
|6|5×5|9|9|0|0|14|5|3.251|6|
|7|5×5|11|11|0|0|16|5|4.266|6|
|8|5×5|8|8|0|0|13|5|2.665|6|
|9|5×5|11|11|0|0|16|5|3.961|6|
|10|5×5|12|12|0|0|17|5|4.163|7|
|11|5×5|10|11|1|0|15|4|5.398|7|
|12|5×5|9|10|1|0|14|4|4.138|7|
|13|5×5|10|11|1|1|15|4|4.773|7|
|14|5×5|11|13|2|1|17|4|5.977|7|
|15|5×5|10|12|2|1|16|4|5.439|7|
|16|5×5|8|10|2|2|14|4|4.993|7|
|17|5×5|8|11|3|2|15|4|6.137|7|
|18|5×5|9|12|3|2|16|4|5.419|7|
|19|5×5|10|13|3|2|17|4|6.849|7|
|20|5×5|11|14|3|2|18|4|7.171|7|
|21|5×5|9|11|2|3|15|4|6.272|7|
|22|5×5|11|14|3|3|17|3|6.929|7|
|23|5×5|9|11|2|3|14|3|6.198|7|
|24|5×5|12|15|3|3|18|3|7.741|7|

All 24 boards use a 5×5 grid. The route performs **254 productive actions**, including **31 manual gate openings** and **25 switch-piece exits**, earning **55 coins per three-star win**: 180 → 1,500 coins before restoration. No purchase or extra gift collection is used.

Levels **22 → 23 → 24** have **11 → 9 → 12 pieces**, **14 → 11 → 15 productive actions**, **3 → 2 → 3 manual gate openings**, three switch exits each and three spare actions each. Recorded known-solution execution takes **6.929 → 6.198 → 7.741 seconds**. These times do not measure thinking: the shorter level 23 replay does not rule out a larger planning burden. Historical catalogue metadata reports decision points **0 → 8 → 10** and structural scores **17 → 85 → 84**; this definition counts costly alternatives along one optimal certificate, not every possible human choice. Seven catalogue fields per level match the retained fixtures (168 comparisons); no new strategy search is claimed.

Victory exposes **five clickable controls at level 1**, **six at level 5**, and **seven at levels 13, 22, 23, 24**. The level 24 offer becomes “Try Shield · free ×1”. These are observed screen choices; their effect on comprehension or retention remains unmeasured.

The initial `playtest24` attempt is separately retained as a global failure: its newly added label counter omitted the key piece at level 6. The corrected counter includes `Key 1, Piece…`, while the app and solutions remain unchanged.

[Measured per-level data](journey-measurements.csv), [native observations](prepare/journey-observations.json), [host report](host-report.json), [48 level frame paths and hashes](playtest-summary.json), [exact pre-run test source](source-snapshot.json).

Limits: API 36 arm64 emulator with 16 KiB pages; fixed automation waits and known solutions; no human comprehension, preference, retention, physical-device compatibility or unscripted difficulty measurement. Root owns the visual review and overall product conclusions.
