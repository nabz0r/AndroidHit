# 1.21 economy arithmetic and modeled limits

These are explicit design parameters and source simulations, not measured
player behavior or a claim of optimal balance. The new terms apply through
versioned new attempts; existing balances and completed rewards are not rebased.
The legacy reward API remains unchanged and is retested across all 1,000 levels.

## Declared curves

For U level `u` in 1–1000, let `p = max(0, u − 10)`.

- First-clear multiplier: `1 + log(1 + p/25) / log(1 + 990/25)`, bounded 1–2.
- Total first-clear entitlement at `s` stars: nearest integer to
  `(25 + 10s) × multiplier`; zero stars pays zero. An improvement pays the
  difference between new and old entitlements, with no second 25-Coin base.
- Boost multiplier: `1 + 0.4 × log(1 + p/50) / log(1 + 990/50)`, bounded 1–1.4;
  round the resulting Coin quote upward. U1–U10 retain original base prices.
- Two Undo uses are free per new attempt. Paid Undo ordinal `k`, beginning at 1
  after those free uses, costs
  `min(40, ceil(8 + 4log(1 + u/25) + 4log(1 + k)))` earned Coins.
- Session storage: `10 + floor(6 × log(1 + p/50) / log(1 + 990/50))`, reaching 16
  at U1000. Raising storage grants no energy and no retroactive full-cap ticks.
  Regeneration remains one unit per ten minutes; same-attempt resume stays free.
- The premium quote remains 50 account Gold for 10 session units, only at zero
  energy. The larger storage cap does not change that protocol or convert Coins
  into Gold. Real production purchase gates remain separate.

The reward grows faster than boost prices so later progress improves the ratio
between first-clear income and optional assistance. Undo becomes a modest,
bounded optional expense after two free corrections. These are design choices
whose clarity and effects still need real-player observation.

## Executed numerical scenarios

[All-level CSV](final-integration/economy-curve-1000.csv) ·
[Actual store and numerical result](final-integration/economy-curve.json).

Across U1–U1000, exact three-star first clears pay **96,154 Coins** in total,
compared with **55,000** through the unchanged legacy API. Exact one-star first
clears pay **61,191 Coins**. These totals exclude the initial wallet, companion
bonuses, gifts, quests, event rewards, duplicate refunds and optional expenses.

Every actual certified U plan still completes with **zero Coin expense** and
zero purchased powers or Undo. Shell preparation is bounded to five free taps
on U11+. This host result does not bypass session energy, demonstrate 1,000
continuous native games or measure human difficulty.

The separate correction scenario uses the **full actual base action allowance**
on each board: real exit, real Undo, then the original certified plan. After the
two free corrections, paid Undo costs **22,514 Coins** across the 1,000 boards.
Each board remains solvent against its own corrected first-clear reward in this
specific scenario. The median expense/reward ratio across boards is **36.7647%**,
maximum **37.8788%**; this is a median over deterministic board scenarios, not a
median observed player expense. Repeated attempts, additional assistance or
continuations can cost more than a board's once-only reward.

Buying one 45-base-Coin hint pack on every board would cost **58,337 Coins**.
That is a deliberately separate optional spending scenario, not a necessary
route or observed purchase pattern. It also accumulates more hint stock than
the certified route requires.

At U42, three-star total entitlement is **67 Coins**, first paid Undo **15**,
the base-45 hint pack **48**, and energy storage **10**. At U1000 those values
are **110**, **26**, **63** and **16** respectively. Declared star totals and
bounded costs do not establish retention, fairness perceived by players or
revenue.

Actual store checks include refused first-clear/star-upgrade commits before and
after memory changes, process reconstruction, exact entitlement retry, Undo
reserve/cancel/complete idempotence, failed finalization/refund and cold recovery.
They use the established preference doubles, not physical flash faults.

## Fixed summon pools and copy burden

[Result](summon-analysis-final/summon-simulation/summary.json) ·
[FR/EN exact item table](summon-analysis-final/summon-simulation/items-fr-en.csv) ·
[Modeled cohorts and exact independent-draw arithmetic](summon-analysis-final/summon-simulation/cohorts.csv).

The simulation calls the real `SummonCatalog.draw` **800,000 times** with saved
fixed seed/request strings: 2,000 modeled empty collections × 200 draws for each
banner. No actual wallet is spent. Exact pool probabilities and independent-draw
formulas are kept beside the empirical output; sampling does not prove RNG
security or predict real player behavior.

The avatar banner has 23 entries and costs 180 earned Coins: ten entries at 6%
each, eight at 3.75%, five at 2%. Nova is the separate free starter. Signatures
have five entries at 40/25/15/12/8% and cost 260 Coins. There is no pity or
adaptive price/odds in this source pool. The new income curve leaves these
fixed banner prices and probabilities unchanged.

For a specific 2% avatar, independent-draw expectation is **50 draws for one
copy**, **400 for eight**, **4,600 for 92**: gross 9,000 / 72,000 / 828,000 Coins
before duplicate refunds and other income. Eight copies reaches companion
level three under the current prototype thresholds; 92 reaches level ten.
The source explicitly marks levels four through ten as provisional balance.
Signature duplicates return Coins and do not use companion-copy thresholds.

When all items in a banner are already owned, expected refund per draw is 61.5
Coins for avatars and 98 for signatures, giving expected net draw expenses of
118.5 and 162. Earlier collection stages refund less, since new items are not
duplicates. The cohort CSV models that distinction exactly. After 200 draws,
91.3% of the fixed avatar cohorts and 100% of the fixed signature cohorts contain
their entire respective pools; those are simulated collection rates.

The CSV's first-clear equivalents are arithmetic gross Coin ratios at 55 or 110
Coins per clear. They exclude duplicate refunds, other earnings, optional
spending and session waiting. They are **not** hours, days, willingness to pay,
conversion, engagement or retention estimates. The long expected random-copy
burden also means a target-copy expedition route has a different role from an
unconditional random banner; observing the real player loop remains necessary.
