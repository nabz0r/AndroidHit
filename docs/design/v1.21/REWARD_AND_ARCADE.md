# Luma Drift 1.21 — companions, arcade feedback and a legible economy

## Product decision

The character is the player's persistent companion, a member of the puzzle team and the subject of copy-based visual evolution. These are three visible roles, with separate controls. Player XP measures first-clear progression; it does not substitute for companion copies. Clothing changes appearance; the six puzzle talents keep their declared effects. A rarer illustration does not silently improve a talent or change the odds of winning a board.

The new home leads with a large original companion stage. Play, the four room shortcuts, squad and boutique provide direct routes. The map uses original floating terrain, ten world palettes, animated clouds and route particles, and the actual next unlocked board. New gameplay backgrounds use slow layered terrain, drifting clouds, edge particles and reactions to real exits. The board and its arrows retain contrast; reduced motion stops the decoration. Transition and reward animations do not make a transaction happen.

## What the references support

The official [Crash Fever quest FAQ](https://crashfever.com/faq/faq_cat/faq6/) describes distinct unit skills and an explicit charged Fever action. Its [unit FAQ](https://crashfever.com/faq/faq_cat/faq5/) distinguishes rarity, skill readiness and awakening. The useful product principle is to connect the visible unit to an action the player can understand and trigger. Luma implements its own earned Focus and existing six talent families; it does not claim to reproduce Crash Fever combat, artwork, names or awakening rules. Quake/Street Fighter were user-specified references for responsive gestures and lively stages, not for copied assets or a measured equivalence in game feel.

A preregistered survey of 2,728 respondents found an association between loot-box spending and problem-gambling symptoms, but used a cross-sectional design: it does not establish which causes which. See [Etchells et al., 2022](https://pmc.ncbi.nlm.nih.gov/articles/PMC9382208/). A 2025 longitudinal replication followed a much smaller cleaned sample (126 young adults); some results depended on Bayesian informative priors, with substantial attrition and few gambling initiations. It adds prospective evidence and uncertainty, not a formula for manufacturing addiction. See [Palmer et al., 2025](https://pmc.ncbi.nlm.nih.gov/articles/PMC12044733/).

These studies motivate avoiding loss chasing, opaque spending and misleading anticipation. They do not demonstrate that a particular animation, schedule or economy is safe, optimal, enjoyable, or non-addictive for every player. No neurologist or economist has certified this game. The numeric terms below are transparent design choices tested as models, not clinical findings or audited player behaviour.

## Arrivals and evolution

The existing 23-companion earned-Coin banner keeps its unconditional per-item probabilities: ten items at 6%, eight at 3.75%, five at 2%. Together they sum to 100%. Nova remains the free starter and is not in that pool. One draw costs 180 earned LumaCoins. The signature-outfit pool and its 260-Coin price remain separately identified.

A draw shows the price and post-spend balance before confirmation. The outcome and debit are durably decided before presentation. The reveal has one bounded opening, can be skipped and then stays readable; it does not display a carousel of rare items that were never won. Reopening a receipt does not roll again. There is no fabricated countdown, personalised odds, disguised automatic repeat purchase or claim that a duplicate makes the next rare result more likely. Gold is not used for these earned-Coin arrivals.

Copies retain their existing cumulative thresholds: 1, 2, 8, 14, 22, 32, 44, 58, 74 and 92. The four authored forms appear at companion levels 1, 3, 6 and 10. Higher thresholds remain prototype balancing values. The evolution screen labels unearned forms as previews, shows the actual owned total, explains the puzzle talent and links to the earned expedition route. It does not grant copies when an animation plays.

## Logarithmic economy, version v121_1

Let U be the campaign board number, bounded to 1–1000, and s the awarded stars.

- First-clear entitlement: `round((25 + 10s) * (1 + log1p(max(0,U-10)/25) / log1p(990/25)))`. U1–10 retain the former amounts; U1000 reaches exactly twice that base. A star improvement pays only the difference between the new and old entitlements. Existing stars and balances are never rebased.
- Power-pack quote: `ceil(base * (1 + .4 * log1p(max(0,U-10)/50) / log1p(990/50)))`, followed by the actual equipped-loadout discount. U1–10 keep their base; growth reaches 1.4 before discounts at U1000. The quote depends on progression, not losses, spending history or inferred vulnerability.
- Undo: two free uses per new 1.21 attempt. Paid Undo number p then costs `min(40,ceil(8 + 4log1p(U/25) + 4log1p(p)))` earned Coins. Every paid use requires confirmation. A wallet reservation is completed only after the undo is saved; recovery refunds a reservation without a saved effect, or finalizes a saved effect once. The previous productive exit remains counted for the move budget and star grade. Historical saved attempts retain their prior terms.
- Energy capacity: `10 + floor(6 * log1p(max(0,U-10)/50) / log1p(990/50))`, bounded to 10–16. Increasing capacity does not grant energy and does not accrue refill time retroactively while the old cap was full. Natural refill remains one point per ten real minutes. Eligible new attempts cost one; resuming the same attempt is free. Opening practice or U1–10 remains free.
- Account Gold remains separate from earned Coins. A ten-energy pack remains 50 Gold, only usable at zero energy, with production gates closed and a clearly separate Lab test wallet. There is no automatic Coin-to-Gold exchange or premium balance inferred from gameplay.

All authored boards have a zero-purchase solution. Spending can provide correction or assistance; it is not required by the certificate. The simulations evaluate those arithmetic routes and optional spend scenarios. They do not estimate conversion, player income, retention or human tolerance for energy pacing.

## Additional skill and elements

New U11+ campaign and expedition attempts get at most three deterministic shells: two ice layers, two fire layers and one water layer. Their shapes, colours and remaining counts differ. Each deliberate tap removes one layer for zero moves; the player then exits the original piece in its arrow direction. The original board geometry, keys, gates and exit certificate stay intact. This is a tactile barrier layer, not a claim of three fully different elemental combat systems.

Only an actual certified companion/power exit can pierce a shell. Hint or path previews do not clear it. Broken shells persist through Undo in the same attempt; a new attempt recreates them. Old saved attempts stay bare. Saved shell state is tied to the complete original level identity and rejects malformed or foreign data.

Three distinct manual exits without a correction charge one free Focus. It reveals the next safe exit after an explicit tap and uses no inventory or Coins. Undo/re-exit cannot farm it; automatic exits do not charge it. There is no real-time success deadline. It adds a deliberate clean-sequence skill incentive while retaining accessible taps and the puzzle's planning core.

Blocked movement gets a short red message below the board, plus the actual piece/blocker outline and accessible explanation. A refused move never displays a fictitious debit: the UI reports either the real loss or zero moves lost under the applicable policy. Remaining moves stay visible in a fixed top band. The first two free Undo uses and later Coin quotes are explicit.

## Acceptance boundary

Automated source models establish arithmetic, solvability, persistence and bounded geometry under the recorded cases. Native instrumentation establishes the actual installed candidate and the executed emulator gestures and pixels. Local browser review is separate from Firebase management publication. The saved public-origin refusal remains in force; no alternate tool or indirect renderer is used to bypass it.

Human comprehension, phone feel, audio listening quality, long-term return and real-provider acceptance are separate measurements. “Gacha ready” in release records means the disclosed transaction/reveal flows passed their stated technical checks; it is not a clinical, legal, commercial or human-enjoyment certification.
