# New-player rewards audit — 1.8 baseline

Recorded 2026-10-27 before the 1.9 changes. This audit executes the production `GameStore` and `MetaProgress` with named, deterministic preference files, and reads native screen code. It does not simulate a real person's motivation, retention, spending, or demographic traits.

The exact baseline source hashes and assumptions are in [source-manifest.json](source-manifest.json). [RewardsBaseline.java](RewardsBaseline.java) emits [baseline.jsonl](baseline.jsonl) using the existing preference doubles in `scripts/RewardsCosmeticsV17Test.java`. Every scenario is a fresh local profile, all first clears at three stars on the same date, with no purchases, power spending, event clears, quest claims or equipment changes.

## What already works

- Practice is separate from the campaign. Its completed-save receipt awards exactly 100 earned coins and two hints; replay never awards a second gift.
- The login cycle is seven claimed visits, not consecutive days. Absence does not remove a reward or reset the cycle.
- First Journey clear gives 25 coins plus 10 per newly earned star: 35–55 coins. Replays do not farm coins, XP, quests or the five-clear gifts.
- Daily and weekly quest currency uses an idempotent `GameStore` grant, then a separate claim marker. Recovery of a partial cross-file operation does not duplicate the currency grant.
- Earned equipment ownership and seasonal wardrobe progress are permanent. Nothing forces Google login to learn or play.
- Power pack prices use only distinct Journey/Remix clears: bands change at 30/90/210/450. Failure, time, spending and user age do not affect the price.

## Confirmed gaps and bounded corrections

| Priority | Confirmed behavior | Consequence | Proposed correction |
| --- | --- | --- | --- |
| P1 | Home says the earned gift is ready, but that hit target opens Rewards. `drawRewards()` has no milestone gift claim. The claim exists only deep in Shop. | The announced reward leads to a screen that cannot deliver it. | Put the actual pending gift, quantity, milestone and claim button on Rewards; home and win should lead there. Keep the Shop entry as a shortcut. |
| P1 | Shield, Focus and Trail unlock with zero starter stock. Immediate five-clear gifts first give Shield at clear 40, Focus at 50 and Trail at 90. | A new feature is advertised at levels 25/45/75 but cannot be tried immediately without finding the shop, earning another gift or seeing an ad. | Give one free sample at the first unlocked Journey level, atomically with one permanent receipt per power; preserve existing stock/ownership and make full stock retryable. Inform the player on win/briefing. |
| P2 | Five-clear gift selection uses all powers unlocked **when the claim happens**. The same twenty milestones have different contents if claimed at once at clear 100. | No stable preview can truthfully promise a specific item before collection. Waiting changes old reward contents. | Freeze eligibility to each milestone's original Journey unlock threshold for newly unclaimed gifts; preserve old saved receipts and in-process retry identity. Expose one shared read-only preview used by claim and UI. |
| P2 | Victory reports coins/XP and Next, but no ready five-clear gift, newly available power or newly owned Journey gear. `syncStyle()` automatically acquires gear and discards the returned list. | Progression has real rewards, but their connection to the win is invisible. | Show a compact earned/available summary without adding a forced modal to every clear. Give a direct Rewards/Inventory route. |
| P2 | Home's Rewards entry is an unlabeled star. Quest and login availability have no count. | A beginner is unlikely to understand which free actions are ready. | Use a labeled Rewards entry with a real count of claims available, computed from current store/meta state. No invented badge or urgency. |
| P2 | Every successful power increments `hintsUsed`; stars are three only at zero powers and zero extensions. | Trying the newly introduced power immediately lowers the visible rating, despite the UI encouraging discovery. | Make this rule explicit, or decouple teaching/samples from star grading with a versioned scoring policy; root should choose and document the policy. Do not change old saved stars. |
| P3 | Entry to every next board goes through the full briefing screen with a second Play action. | A 100-board run adds 99 extra confirmation steps even when no new mechanic appears. | Retain first/new-mechanic briefing; offer direct next board for already taught mechanics, with an optional preview. Verify accessibility and save recovery. |

## Measured economy and unlock checkpoints

After the tutorial and first login claim the balance is **320 LumaCoins**, with five hints, three Visions and two Flows. Vision is usable after four clears; Flow after eleven. The unused preloaded quantities remain saved while locked.

| First available power | Clears needed | Stock immediately after the unlock in baseline | First matching five-clear gift, if each is claimed promptly |
| --- | ---: | ---: | ---: |
| Hint | 0 | 5 after tutorial | 5 |
| Vision | 4 | 3 | 10 |
| Flow | 11 | 2 | 15 |
| Shield | 24 | 0 | 40 |
| Focus | 44 | 0 | 50 |
| Trail | 74 | 0 | 90 |

At 100 perfect first clears, balance is **5,820 LumaCoins** before any quest coin claims; rank is 30. The hint pack price changes from 45 to 55 at 30 distinct clears, then to 65 at 90. This is a trace under the stated assumptions, not observed player behavior or a recommendation to increase prices.

With each gift claimed promptly, ending stock is `hint 10 / vision 7 / flow 6 / shield 4 / focus 2 / trail 1`. Deferring all twenty gifts until clear 100 ends at `9 / 7 / 5 / 3 / 3 / 3`. The difference comes from claim-time eligibility, not random draws.

## Scope of the proposed release

Keep the earned/premium currency separation, non-expiring earned Journey gifts, all six power definitions, existing unlock levels and logarithmic price bands. Repair discoverability, stable promises and the first-use opportunity. No age inference, demographic pricing, payment gating, fake scarcity or simulated human retention is needed to correct these gaps. Real-world demographic evidence belongs in the separate research report; it cannot turn these deterministic traces into human outcomes.
