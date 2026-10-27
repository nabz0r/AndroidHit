# Android purchased-gold wallet

The **1.5.0-rc1 / code 6** production store implements Google Play checkout and an authenticated server client, but the delivered APK embeds an empty server origin and blocks checkout. The wallet itself is deployed with recorded public routing/CORS checks; actual Android login and Play purchase lifecycle remain unqualified. [Exact candidate](RELEASE.md) · [Deployment observations](PRODUCTION_RELEASE.md).

## Player experience and currency separation

The production commerce screen shows **purchased gold** separately from **gold earned on this device**. Paid gold is never added to `GameStore.coins`, imported from a local balance, or restored from an editable client preference. A signed-in player can refresh their server balance and permanent companions/themes. The first Android integration exposes purchased gold for permanent companions and themes only. Power-ups, puzzle progress, optional ad rewards and the ordinary earned-gold shop remain device-local.

Cosmetics bought with earned gold remain local. Cosmetics bought through the account wallet are restored from the server after Google sign-in and a wallet refresh. They are available in the game's ordinary collection through the account-scoped ownership bridge. The six supported languages have production-specific wallet text; the production UI contains no purchase simulator.

If a previously spent purchase is refunded, the server balance can become negative. The store explains that new purchased gold first covers that outstanding balance. An old successful response cannot replace a newer wallet version in the current screen or ownership cache.

## Checkout and recovery

`PlayGoldBilling` queries the three allowlisted one-time product IDs. The displayed localized price and checkout offer token come from the same current Google Play `ProductDetails` response. Rental, preorder and discount offers are excluded from this initial implementation. No fallback currency price is invented.

Checkout requires all of the following:

- A production build with an explicit, valid HTTPS wallet origin.
- The real verification adapter enabled and a signed-in Google/Firebase user.
- A Play connection and an eligible currently fetched ordinary buy offer.
- A successful wallet refresh before the store enables purchase buttons.

The launch parameters bind the purchase to the lowercase SHA-256 hash of the Firebase UID via `setObfuscatedAccountId`. The client sends observed purchase tokens only to the authenticated verifier. **It never credits, acknowledges or consumes a Play purchase locally.** The server verifies the provider response, grants the allowlisted amount transactionally once, and consumes only after durable fulfillment. [Google Play integration](https://developer.android.com/google/play/billing/integrate), [offer selection](https://developer.android.com/google/play/billing/one-time-product-multi-purchase-options-offers).

Both `PENDING` and `PURCHASED` observations are submitted for server verification. Pending observations add no gold; storing their token allows server notification/maintenance recovery. Opening or returning to the commerce screen queries owned purchases again. `WalletRecoveryController` also refreshes the wallet and queries owned purchases whenever the main game enters the foreground, provided the production endpoint is configured and a Google user is already signed in. This recovery path does not query the product catalogue, launch checkout, show ads, create accounts, grant local gold or display toast messages. Backgrounding/destroying the activity closes its recovery session; generation and UID checks discard callbacks from previous sessions or accounts. A token already consumed after a lost client response is recovered through a fresh server wallet fetch. Purchase tokens and Firebase bearer tokens are not stored in the local journal or purchase simulator preferences.

## Android API and contract

Implementation: `com.androidhit.lumadrift.wallet.ProductionWalletClient`.

| Method | Purpose |
|---|---|
| `refresh(Callback)` | `GET /v1/wallet` |
| `verify(productId, purchaseToken, Callback)` | `POST /v1/purchases/verify` |
| `spend(sku, requestId, Callback)` | `POST /v1/wallet/spend` |
| `deleteAccount(Callback)` | `DELETE /v1/account`; successful deletion clears that UID's cosmetic cache |
| `close()` | Discard callbacks and shut down the owner's network executor |
| `ownsCharacter(Context, id)` / `ownsSkin(Context, id)` | Static access to the current signed-in UID's restored cosmetic cache |

`Callback.onSuccess(Wallet)` and `Callback.onError(statusKey)` run on the main thread. Successful account deletion supplies `null` rather than a wallet. Recent-authentication failures map to `account_recent_login`; the account UI can request a fresh login. A completed deletion cannot be reported as a success to a different newly signed-in account.

The base URL is an HTTPS **origin**, such as `https://wallet-example.run.app`, with no path, query, fragment, embedded username, or nonstandard port. The client refuses HTTP and redirects, so it cannot forward a bearer token to a redirected host. Requests have connection/read timeouts and response-size limits. Firebase identity restoration occurs only after an earlier explicit sign-in; anonymous/non-Google users cannot open checkout.

A successful response has this shape:

```json
{
  "balance": 500,
  "version": 1,
  "characters": [],
  "skins": [],
  "inventory": {"boosters": {}, "continuations": 0}
}
```

Verification adds `purchase: {status, productId, credited, consumptionPending}`. Spending adds `entitlement: {id, sku, kind, itemId, amount}`. The Android UI offers `character:<id>` and `skin:<id>` at catalog prices; the server independently owns the SKU and price allowlist. The server's wider consumable-inventory API is not advertised as playable Android paid-power integration in this release.

A cosmetic spend writes a UUID request ID to account-scoped local preferences before sending. Ambiguous failures reuse that ID; successful responses clear it. The server's transaction and unique-cosmetic constraint enforce the actual debit and ownership. Preference caching is an offline display convenience, never payment evidence.

## Verification and remaining activation

Run `scripts/test-billing-policy.sh` for the credential-destination, UID-binding, request-ID and six-language formatting checks. Run `scripts/test-wallet-recovery.sh` for 19 deterministic lifecycle checks covering guest/configuration gates, duplicate callbacks, retry, account switching, backgrounding and destruction. This controller test uses explicit provider doubles and never represents a Google transaction. Neither suite certifies the remote deployment. The production Java sources also compile against Billing 9.1.0 and the pinned Firebase/advertising dependencies.

Before enabling actual checkout, qualify the deployed server and configure its origin in a new internal Android build, authorize its managed identity for the specific Play application, enable the three products, and test approved/declined/pending payments with Play **license testers** installed through the internal track. Test reconnects, process death, refund reconciliation, account switching and deletion against that deployed service. [Google's billing test procedure](https://developer.android.com/google/play/billing/test).

The preparation APK uses Google sample ad creatives even though the owner's three placement IDs are configured. Real ad serving requires an explicit qualified build with test forcing disabled. Gold, continuation and power placements select their respective configured unit when enabled. Existing age, explicit opt-in and Google UMP consent gates remain mandatory.

The 18 local avatar equipment items and their limited benefits are outside the server wallet cosmetic SKU catalog. Google login does not synchronize equipment, puzzle progression or earned coins. [Equipment ownership boundary](AVATAR_EQUIPMENT.md).
