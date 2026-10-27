# Luma Drift 1.7: rewards, currencies and wardrobe

This document describes the candidate implementation. It does not certify a live payment, a live ad reward, server deployment, or account synchronization. Release qualification and the final APK evidence remain separate.

## Two separate balances

**LumaCoins** are the existing device-local earned balance. Previous saves keep the exact integer balance, stars, existing characters, skins, power stock and legacy equipment. Completing new boards, improving stars, quests and the new daily gift can earn LumaCoins. Replaying an already completed board does not create new quest credit.

**LumaGold** is the existing account-bound premium server wallet. This release does not convert it into LumaCoins. The new local cosmetic capsule accepts earned LumaCoins only. It does not call billing, accept a payment receipt, spend the server wallet, or grant cash value. Existing internal legacy methods named `grantGold` / `spendGoldOnce` retain their historical explicit sandbox meaning; those names are not a production balance bridge.

The prepared native account vault loads the authenticated premium catalogue, account ownership and conditional probabilities. Paid draws require both `BuildConfig.PAID_COSMETIC_DRAWS_ENABLED` and server `PAID_COSMETIC_DRAWS_ENABLED`; both remain false. These are technical gates, not proof of commercial qualification. The account catalogue has its own authoritative ownership: an item already earned locally may still be new to the account. A future account guarantee must not promise an item new to the local wardrobe. No paid cosmetic draw is available in this candidate.

## Premium account vault: prepared, closed

The native vault is reached from the LumaGold shop card. Before authenticated loading, it shows no invented balance, ownership or percentages. Its explicit catalogue request returns the account balance/version, owned IDs and conditional per-item probabilities. The prepared terms are 100 account LumaGold per draw, 50 LumaGold for an account duplicate, and a guaranteed account-new item after 19 consecutive account duplicates if the account still lacks an item. That guarantee changes the next eligible pool and displayed probabilities; the local capsule’s unconditional odds must not be reused as premium conditional odds.

The premium pool uses the same 24 IDs, including starter designs. An account-new item may already be locally earned, so the guarantee is strictly **new to the account**. Local earned ownership is not uploaded as trusted account ownership. Account entitlements can appear in the wardrobe only for the current account through its scoped cache; logout/account change removes their eligibility unless that design is also owned locally. This does not turn an account entitlement into a permanent local grant. An earned capsule treats an already visible owned design as a duplicate and does not silently copy account ownership into the local ledger.

Both draw gates remain false. Prepared retry/receipt code and synthetic protocol tests are not a paid sale or real draw. Before any activation, qualify two real identity/account flows, catalogue availability, payment/refund/recovery and the ownership/product decision. [Vault contracts and 1,552 model checks](validation/v1.7/vault/README.md).

## Daily gift: seven claimed visits

| Gift in cycle | LumaCoins |
| --- | ---: |
| 1 | 40 |
| 2 | 50 |
| 3 | 60 |
| 4 | 70 |
| 5 | 80 |
| 6 | 100 |
| 7 | 150 |

One gift may be claimed per local calendar date. The next successful claim advances the cycle. Taking a break does not reset it and no previously earned reward is removed. The cycle repeats after the seventh claimed visit. The local save also retains the lifetime claimed-visit count used by four wardrobe unlocks.

`GameStore.claimDailyLogin()` returns the credited amount for a new claim and zero for an already claimed date. `dailyLoginDay()`, `dailyLoginClaimed()`, `dailyLoginCount()` and `dailyLoginCoins(day)` expose the screen state. A date older than or equal to the latest claimed date cannot claim again. This local calendar rule is not a server-backed anti-cheat system: changing a device clock forward can change local reward availability.

## Quests have two independent calendars

| Period | Objective | Reward |
| --- | --- | ---: |
| Daily | First-clear 3 different boards | 35 LumaCoins |
| Daily | Earn 6 stars on first clears | 40 LumaCoins |
| Daily | First-clear 1 daily/weekly event board | 45 LumaCoins |
| Weekly | First-clear 10 different boards | 150 LumaCoins |
| Weekly | Earn 24 stars on first clears | 180 LumaCoins |
| Weekly | First-clear 3 daily/weekly event boards | 200 LumaCoins |

Daily quests rotate on the device's local date. Weekly quests use the local calendar date of Monday, including across year boundaries. Midnight rotates daily quests without clearing weekly progress. A new week does not erase permanent inventory or currency. A board's first-clear identity is permanent; replaying a Journey or Remix board on a later day or week cannot farm quest progress. Daily and weekly event boards have distinct period identities.

`MetaProgress` keeps the existing daily mission API and adds `weeklyMissionProgress/Target/Coins/Complete/Claimed`, `claimWeeklyMission(index, store)` and public `weekId()`. Reward receipts and their LumaCoins share one atomic GameStore transaction. The separate quest claimed flag can recover after interruption without paying twice.

## A four-slot cosmetic wardrobe

The new wardrobe is separate from the pre-existing three-slot equipment system. Every **new wardrobe item is visual only**: hats, outfits, shoes and gloves do not modify movement allowance, coins, prices, powers, drop chances or difficulty. Owning multiple cosmetics never stacks benefits.

Each slot has six items, for 24 total. Four dew starters are owned immediately. Locally earned and direct LumaCoins-purchased items remain locally owned permanently; changing language, date, calendar season or equipped item cannot remove ownership.

| Slot | Free starter | Claimed daily gifts | Weekly event first clears | Lifetime event stars | Direct LumaCoins shop |
| --- | --- | --- | --- | --- | --- |
| Hat | `hat_dew` | `hat_moon`: 2 | `hat_star`: 2 | `hat_pumpkin`: 12 | `hat_cupid`: 450; `hat_aurora`: 650 |
| Outfit | `dress_dew` | `dress_moon`: 4 | `dress_star`: 4 | `dress_valentine`: 18 | `dress_witch`: 500; `dress_aurora`: 700 |
| Shoes | `shoes_dew` | `shoes_moon`: 7 | `shoes_star`: 6 | `shoes_pumpkin`: 24 | `shoes_cupid`: 400; `shoes_aurora`: 600 |
| Gloves | `gloves_dew` | `gloves_moon`: 10 | `gloves_star`: 8 | `gloves_valentine`: 30 | `gloves_witch`: 400; `gloves_aurora`: 600 |

Weekly wins and event stars accumulate from the existing saved event history. Halloween and Valentine designs are permanent collections with earnable archive thresholds, rather than disappearing ownership or a real-world seasonal availability claim.

`CosmeticCatalog.ITEMS` is the stable catalogue. `GameStore` exposes ownership, source, progress, claimability, earned unlock collection, direct purchase, equip, current item per slot and owned count. `claimCosmeticUnlocks()` belongs after reward/first-clear handling or when opening the wardrobe, rather than in every animation frame. All new labels and item names have English, French, German, Spanish, Portuguese and Italian entries in `V17Strings`.

## Earned cosmetic capsule

One capsule costs **150 earned LumaCoins**. It selects one item from the same 24-item catalogue. Ownership does not change the displayed probabilities. There is no undisclosed pity rule or reroll.

| Rarity | Items | Category probability | Probability per item | Duplicate compensation |
| --- | ---: | ---: | ---: | ---: |
| Common | 8 | 60% | 7.5% | 25 LumaCoins |
| Rare | 8 | 30% | 3.75% | 60 LumaCoins |
| Epic | 8 | 10% | 1.25% | 100 LumaCoins |

A new item is permanently added to ownership. A duplicate gives the listed earned LumaCoins compensation in the same transaction as the 150-coin debit. All results have no exchange, transfer or cash-out value. The confirmation screen must show cost, individual/category odds and duplicate compensation before spending.

`drawCosmeticLoot(requestId)` uses a per-save seed and a stable request identity. Retrying the same request returns the same outcome, with `replayed = true` and `chargedCoins = 0`, without spending again or rerolling. A null result means an invalid identity or insufficient balance. A successful result exposes `itemId`, `duplicate`, `compensationCoins`, `chargedCoins` and `replayed`.

The deterministic seed is a local recovery mechanism, not a trusted remote lottery or anti-tampering claim. Production paid draws require their own authoritative server transaction and qualification; they must not reuse this local earned-currency ledger.

## Persistence and social gift receipt

A local reward transaction commits its balance changes, permanent ownership and receipt together. Android can change its in-memory SharedPreferences state even when disk persistence fails. `LocalSaveTransactions` retains the exact unfinished write across store handles in the same process and flushes it before a later local economic operation. A failed write surfaces as a persistence error. A process restarted from the last durable save can safely retry its unrecorded date or request.

`claimSocialGift(uid, giftId)` applies one verified social-service gift as one hint stock unit. It hashes the account and gift identity into the receipt, never credits LumaCoins or LumaGold, rejects malformed identifiers, and never spends a receipt when stock is already 999. A duplicate returns false. The social screen retains a receipt retry so a full inventory or interrupted local save can recover later. This local receipt does not itself authenticate a backend response; its caller must use the authenticated social service and preserve the captured account identity.

## Executed checks

Run `bash scripts/test-rewards-cosmetics-v17.sh`, `bash scripts/test-meta.sh` and `bash scripts/test-data.sh`. Current outputs are in [`validation/v1.7/`](validation/v1.7/).

The new suite covers the seven-visit cycle, gaps, backwards dates, year rollover, cold recreation, failed writes before and after in-memory application, exact debit and compensation, permanent seasonal ownership, source thresholds, invalid/unowned equipment, daily/weekly separation, first-clear deduplication, gift limits, all six language entries, and concurrent independent store handles.

Its fixed-seed 100,000-outcome simulation measured **60,072 common, 29,862 rare and 10,066 epic** outcomes. That is an implementation distribution check, not player, revenue, conversion or retention evidence. Native screen/rendering and final APK installation tests belong in the release validation record.
