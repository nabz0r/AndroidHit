# Avatar equipment — 1.5 candidate contract

The 1.5 avatar combines one of the existing 24 cosmetic companions with **three equipment slots: Headwear, Aura and Charm**. Each slot has six fixed items, for **18 items in total**. Only the three equipped items contribute benefits; owning several items in a slot never adds their effects together. Companion identity and theme selection remain cosmetic.

This layer gives the lobby a visible record of the player's choices and offers longer-term collection goals. Its effect on comprehension or voluntary return is a design hypothesis. The [release acceptance record](RELEASE_1_5.md#acceptance-record) separates the implemented data contract from final UI, migration and runtime checks.

## Three small, explicit benefits

| Slot | Maximum benefit | Boundaries |
|---|---|---|
| Headwear | **+3 local earned coins** on a board's first victory | The equipped bonus is captured when `completeLevel`, `completeDaily`, `completeWeekly` or `completeRemix` records that first clear. A later claim uses that snapshot; replays, star improvements or changing equipment after victory cannot increase it. No friend-challenge or retroactive pre-feature bonus. The local balance cap still applies. |
| Aura | **5 local earned coins off a power pack** | `GameStore.boosterPrice()` applies `max(1, EconomyCurve price − equipped discount)`. This is a fixed coin discount per pack, not a percentage, extra stock, free pack or discount on real-money gold. Pack quantities remain defined by `Catalog`. |
| Charm | **One blocked attempt protected per board run** | The controller captures the equipped allowance at run start and persists its remaining charge. Protection avoids the allowance cost of that blocked attempt; it does not add actions, remove a tile or change gates. Multiple owned charms do not stack. Existing Shield charges are consumed first; the Charm charge is used when no Shield charge remains. Friend challenges receive no Charm protection. The final controller/restore behavior must pass installed acceptance. |

Some items are **appearance only**. Their rarity or acquisition cost does not imply an additional hidden benefit. The Common/Rare/Epic labels describe the fixed catalog; they are not drop probabilities. All boards retain a certified solution within their base allowance without equipment or consumable powers.

## Exact catalog

All quantities below are fixed in [`AvatarGearCatalog.java`](../app/src/main/java/com/androidhit/lumadrift/data/AvatarGearCatalog.java). Coin prices spend the existing **local** balance, separate from purchased gold.

| Slot | Item / stable ID | Rarity | Acquisition | Benefit while equipped |
|---|---|---|---|---|
| Headwear | Moonbud · `moonbud` | Common | Starter | Appearance only |
| Headwear | Lantern Leaf · `lantern_leaf` | Common | 8 distinct Journey clears | +1 first-victory coin |
| Headwear | Crescent Knot · `crescent_knot` | Rare | 3 lifetime weekly-board wins | +2 first-victory coins |
| Headwear | Tide Crown · `tide_crown` | Rare | 18 lifetime event stars | +2 first-victory coins |
| Headwear | Starfold Visor · `starfold_visor` | Rare | 600 local coins | +2 first-victory coins |
| Headwear | Prism Antlers · `prism_antlers` | Epic | 100 distinct Journey clears | +3 first-victory coins |
| Aura | Firefly Ring · `firefly_ring` | Common | Starter | Appearance only |
| Aura | Mint Orbit · `mint_orbit` | Common | 12 distinct Journey clears | −1 coin per power pack |
| Aura | Violet Ripple · `violet_ripple` | Rare | 4 lifetime weekly-board wins | −2 coins per power pack |
| Aura | Sunset Ribbon · `sunset_ribbon` | Rare | 21 lifetime event stars | −3 coins per power pack |
| Aura | Comet Halo · `comet_halo` | Rare | 800 local coins | −4 coins per power pack |
| Aura | Aurora Bloom · `aurora_bloom` | Epic | 150 distinct Journey clears | −5 coins per power pack |
| Charm | Pebble Heart · `pebble_heart` | Common | Starter | Appearance only |
| Charm | Dew Compass · `dew_compass` | Common | 20 distinct Journey clears | Appearance only |
| Charm | Copper Wish · `copper_wish` | Common | 450 local coins | Appearance only |
| Charm | Tide Anchor · `tide_anchor` | Rare | 24 lifetime event stars | One protected blocked attempt per run |
| Charm | Star Bell · `star_bell` | Rare | 7 lifetime weekly-board wins | One protected blocked attempt per run |
| Charm | Moon Seed · `moon_seed` | Epic | 1,100 local coins | One protected blocked attempt per run |

The four coin items cost **2,950 local coins in total**. This arithmetic describes the fixed catalog; it is not a time-to-unlock estimate or an assertion that spending on equipment, powers and cosmetics fits the same budget.

## Unlocks and persistence

Journey progress counts distinct completed Journey boards. Weekly progress counts **won weekly boards**, not complete weeks or a consecutive streak. Event stars sum the saved best stars of daily and weekly boards across stored periods. `avatarGearProgress()` reads the existing local history, so earlier recorded event play contributes to these lifetime requirements. Calendar rollover does not delete already earned ownership or reset these equipment requirements.

`claimAvatarGearUnlocks()` persists eligible starter/milestone items when the controller calls it on resume, after a win or when opening equipment. It is not called on every drawing frame. `purchaseAvatarGear()` only accepts a catalog item whose source is `EARNED_COINS`, checks its fixed cost and stores ownership with the debit. Repeating an already-owned purchase cannot debit again. The equipment preview's explicit purchase action then calls `equipAvatarGear()`; that method accepts only an owned item and replaces its corresponding slot. Eligible milestone acquisition alone does not replace the current loadout.

Ownership, its acquisition-source label and loadout are stored in **GameStore's device-local preferences**. `avatarLoadout()` rejects invalid, wrong-slot or unowned saved IDs and falls back to that slot's starter. A valid loadout has exactly one item per slot; [`AvatarLoadout.java`](../app/src/main/java/com/androidhit/lumadrift/data/AvatarLoadout.java) derives benefits from those three validated IDs.

Headwear bonuses have a separate local receipt. A new first victory records `pending:0..3` with the currently equipped Headwear effect; `claimAvatarVictoryBonus(mode, index, period)` credits it once and records the claim. Existing pre-feature wins produce no retroactive pending bonus. Daily/weekly identities include the saved period and board index. A new equipment choice cannot turn a completed replay into a new rewarded first clear.

This is an offline progression system. It has no Firebase equipment sync, cross-device restoration, purchased-gold import, trading or cash value. The Lab's local simulations remain test data. The separate server wallet continues to authorize its own permanent cosmetic entitlements; local equipment preferences never authorize server funds. [Android wallet boundary](WALLET_ANDROID.md).

## Implementation and acceptance

| Source | Responsibility |
|---|---|
| [`AvatarGearCatalog.java`](../app/src/main/java/com/androidhit/lumadrift/data/AvatarGearCatalog.java) | Stable IDs, slots, rarity, source thresholds/prices and per-item caps |
| [`AvatarLoadout.java`](../app/src/main/java/com/androidhit/lumadrift/data/AvatarLoadout.java) | Slot validation, starter fallback, non-stacking benefits and minimum pack cost |
| [`GameStore.java`](../app/src/main/java/com/androidhit/lumadrift/data/GameStore.java) | Local ownership/equip, lifetime progress, first-victory receipts and discounted pack price |
| [`GearStrings.java`](../app/src/main/java/com/androidhit/lumadrift/data/GearStrings.java) | Names, source progress and exact effect captions in English, French, German, Spanish, Portuguese and Italian |
| [`LumaView.java`](../app/src/main/java/com/androidhit/lumadrift/LumaView.java) | Lobby/equipment controls, first-win claim integration and saved per-run Charm state |

Before delivery, verify displayed source, price and benefit against every catalog row, including appearance-only charms; ownership/equip across restart; milestone/calendar behavior; duplicate/persistence-failure handling; first-clear snapshot versus replay; and Charm exhaustion/restoration. Inspect every language for formatting and clipping. A matching string catalog or JVM test does not establish native-speaker quality, a clear physical-phone UI or player preference. Final results belong in [Release 1.5](RELEASE_1_5.md).

## Design sources and limits

Unity's historical [2024 Mobile Growth and Monetization Report](https://legacy-activation.unity3d.com/resources/mobile-growth-monetization-report-2024), particularly sections 2.2–2.3, discusses progression stages, rewards and the importance of understandable currencies. It reports observations from Unity's ecosystem, with no Luma Drift player cohort. Our inference is to make each equipment source, cost and limited effect visible, then test whether players understand and value it; those observations do not validate this catalog, its prices or a retention/revenue forecast.

Google Play's [Payments policy](https://support.google.com/googleplay/android-developer/answer/9858738?hl=en), checked 27 October 2026, requires clear and accurate purchase terms/pricing and consistency with the Play billing interface. The current equipment catalog uses local coins. Any future real-money offer needs its own applicable billing and entitlement integration, exact visible value and qualification. This document does not claim Play approval or completed commercial activation.
