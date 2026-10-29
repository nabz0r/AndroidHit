# V3 progression: exact model audit

This is a deterministic technical model, not calibrated human difficulty, retention or enjoyment. The legal policy chooses uniformly among legal exits; its success probability is computed exactly over every reachable subset. Blind taps select any present piece and consume one action even when blocked; 100 fixed seeded runs are simulated per board. No powers, purchases, retries, undo or extensions are used in either baseline.

## Journey

- Boards: 1000; key boards: 167; fixed cells: 390.
- Mean exact legal-random success: V2 11.96% → V3 12.62%.
- Seeded blind-tap wins: V2 372/100000 → V3 449/100000.
- Peak→next-wave recovery increases: 96/99 pairs; mean technical ratio V2 1.4091 → V3 2.0926.

| Authored beat | Boards | V2 exact mean at same positions | V3 exact mean |
|---|---:|---:|---:|
|INTRODUCTION|5|90.28%|95.00%|
|PRACTICE|401|12.14%|13.73%|
|PEAK|296|10.49%|8.83%|
|RECOVERY|298|11.87%|13.51%|

| Authored label | Boards | V3 exact mean |
|---|---:|---:|
|NORMAL|605|14.92%|
|HARD|298|9.83%|
|EXPERT|97|6.87%|

## Remix

- Boards: 240; key boards: 29; fixed cells: 46.
- Mean exact legal-random success: V2 22.55% → V3 22.90%.
- Seeded blind-tap wins: V2 16/24000 → V3 21/24000.
- Peak→next-wave recovery increases: 20/23 pairs; mean technical ratio V2 1.1675 → V3 1.5016.

| Authored beat | Boards | V2 exact mean at same positions | V3 exact mean |
|---|---:|---:|---:|
|INTRODUCTION|4|87.50%|81.56%|
|PRACTICE|93|21.42%|21.24%|
|PEAK|72|21.15%|18.24%|
|RECOVERY|71|21.78%|26.49%|

| Authored label | Boards | V3 exact mean |
|---|---:|---:|
|NORMAL|147|26.33%|
|HARD|72|17.94%|
|EXPERT|21|15.89%|

## Validation scope

All 1,240 J/K certified completions fit N exits within the initial N+3..5 allowance. Each legal choice along every certificate is compared with a separate exact reference model; saved histories, gates, key states and budget counters are replayed at every certificate prefix. Selected key boards additionally compare arbitrary piece subsets and all 16 gate masks with a brute-force oracle. All 1,240 G/H active-session prefixes and policy-2 counters are separately restored using their original layouts and allowances. Historical C/R/B/Q/G/H/P and event codes, geometry, seeds, solution orders and allowances are protected by the pre-change SHA-256 fingerprints.

The reported exact probability is one deliberately naive policy. It excludes human learning, intent, accessibility, frustration, retries and commercial outcomes. Normal/Hard/Expert are authored comparative labels; they have not been calibrated against people. Runtime, Android UI and upgrade installation are separate checks.
