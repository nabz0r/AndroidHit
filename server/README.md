# Luma Drift paid gold wallet

This is a server implementation undergoing deployment qualification, **not a qualified payment service**. The owner has deployed Cloud Run and reported database/rules/IAM setup; external probes confirm application routing and rejection of an unauthenticated wallet request. The first public health check exposed Cloud Run's reserved `/healthz` path; use `/health` after deploying the correction. Play products, completed Google sign-in and internal-track purchase/refund tests remain necessary before selling gold. No real payment was used to develop or qualify it. [Deployment incident and evidence](../docs/validation/cloud-run-health-route.md).

The production package is `com.androidhit.lumadrift`. Lab earned/test coins remain local and cannot be imported into this wallet. This release redeems paid gold for permanent companions and skins at fixed, published catalog prices. Boosters and continuation inventory have tested server primitives, but their paid redemption is disabled in the production entry point until their Android server-use flow ships.

## Run and validate

Use Node 24 LTS, then `npm ci`, `npm run check`, and `npm test` from this directory. HTTP tests bind only to loopback, with fake Firebase/Google providers. The separate Firestore emulator tests exercise the production database adapter and actual client-deny rules; see [test evidence and reproduction](TESTING.md). Tests never need a service-account key or a real purchase.

For production, [follow the deployment runbook](DEPLOYMENT.md). The container runs as an unprivileged user and receives Application Default Credentials from its attached Cloud Run service account. Startup refuses key-file and emulator environment variables. Copy values from [.env.example](.env.example) into Cloud Run configuration; the file is a template, not a working deployment.

## HTTP contract

All `/v1/*` routes require `Authorization: Bearer FIREBASE_ID_TOKEN`. The server verifies the token including revocation and requires `firebase.sign_in_provider == "google.com"`; anonymous/custom/password identities do not receive a paid wallet. No Firebase client directly reads or writes Firestore.

| Route | Input | Result |
|---|---|---|
| `GET /v1/wallet` | No query or body | Wallet and restored cosmetic ownership |
| `GET /v1/inventory` | No query or body | Same wallet shape |
| `POST /v1/purchases/verify` | `{ "productId": "luma_gold_500", "purchaseToken": "opaque Play token" }` | Wallet plus `purchase` result |
| `POST /v1/wallet/spend` | `{ "requestId": "persisted UUID", "sku": "character:miko" }` | Wallet plus stable `entitlement` receipt |
| `POST /v1/inventory/use` | `{ "requestId": "persisted UUID", "sku": "booster:hint" }` | Reserved; production returns `SKU_NOT_AVAILABLE` |
| `DELETE /v1/account` | No body, or `{}`; Google authentication within 5 minutes | `{ "deleted": true }` after deletion completes |
| `GET /health` | Public | `{ "status": "ok" }`, process health only; use this path on Cloud Run |

Every wallet result has this flat shape:

```json
{
  "balance": 340,
  "version": 2,
  "characters": ["miko"],
  "skins": [],
  "inventory": { "boosters": {}, "continuations": 0 }
}
```

Purchase verification adds `purchase: { "status": "PURCHASED", "productId": "luma_gold_500", "credited": true, "consumptionPending": false }`. A repeated valid request has `credited: false` and returns the current wallet; pending payments have `status: "PENDING"` without credit. Consumption can temporarily remain pending **after a durable credit**; it is retried by the scheduled worker. The Android client never consumes or acknowledges these purchases itself.

Spending adds `entitlement: { "id": "persisted UUID", "sku": "character:miko", "kind": "character", "itemId": "miko", "amount": 1 }`. The Android client must persist the request ID before sending, retry the same ID after ambiguous failures, and apply ownership from the UID-bound server snapshot. Clearing Android storage does not erase server ownership. The same request ID with a different SKU/action fails with `409 REQUEST_CONFLICT`.

Errors are `{ "error": { "code": "INSUFFICIENT_GOLD" } }`, with no raw upstream message. Common codes are `401 UNAUTHENTICATED`/`RECENT_LOGIN_REQUIRED`, `403 GOOGLE_LOGIN_REQUIRED`/`PURCHASE_OWNER_MISMATCH`/`PURCHASE_ALREADY_CLAIMED`, `409 INSUFFICIENT_GOLD`/`ALREADY_OWNED`/`REQUEST_CONFLICT`, `410 ACCOUNT_DELETED`/`PURCHASE_REVOKED`, `429 RATE_LIMITED`, and `503 PLAY_UNAVAILABLE`/`SERVICE_UNAVAILABLE`. `DELETION_RETRY_PENDING` means a durable, frozen-account deletion job remains for automatic recovery. Show a localized explanation, rather than exposing these internal code names to players.

## Value and payment guarantees

The server allowlists exactly `luma_gold_500 → 500`, `luma_gold_1400 → 1400`, and `luma_gold_3600 → 3600`. It ignores client gold amounts/prices and rejects extra request fields. The production Play catalog must disable multi-quantity purchases; this implementation validates quantity **exactly one**. The actual localized money price comes from Google Play, not this server.

At purchase launch, Android sets `obfuscatedAccountId` to lowercase hexadecimal **SHA-256 of the exact Firebase UID**. The server calls [Google's ProductPurchaseV2 API](https://developers.google.com/android-publisher/api-ref/rest/v3/purchases.productsv2/getproductpurchasev2), using its configured package, and checks product, owner hash, purchase state, quantity, refundability, and consumption state. It never accepts a client purchase signature or notification body as purchase proof.

A transaction over the wallet and globally token-hashed purchase document commits the credit once. Only after that durable commit does the server [consume through Google Play](https://developers.google.com/android-publisher/api-ref/rest/v3/purchases.products/consume). A consumed token missing from the ledger cannot create a new credit. Retries and provider/database crash boundaries cannot turn a purchase into a second credit. These mechanisms follow [Google's purchase security guidance](https://developer.android.com/google/play/billing/security); the live integration still needs end-to-end validation with the owner's Play account.

Paid cosmetic prices match Android's `Catalog.java` and do not depend on untrusted local progress. Free Nova/Lumina are never sold. Purchase/spend/use balances have a one-billion-gold lifetime-credit ceiling; this bounds balance to `−1,000,000,000…1,000,000,000`. A verified refund subtracts the original credit once, and may leave debt if gold was already spent. Debt blocks further gold spending; existing cosmetic ownership is retained. Later purchases first repay that debt. This explicit revocation policy must be described in the production store terms and support process.

There is no arbitrary credit endpoint, guest-to-paid gold migration, rewarded-ad gold import, or local-wallet balance upload. Advertisements and earned coins cannot mint paid gold. Firestore transactions are the source of paid value and ownership; changing local Android preferences can change local gameplay but cannot authorize a server purchase or debit.

## Refunds, retries, and maintenance

`POST /internal/rtdn` accepts only a Google-signed OIDC token with the exact configured Pub/Sub service-account email, verified email flag, Google issuer, audience equal to `SERVICE_URL`, and exact expected subscription. Notification IDs are stored only after processing succeeds. [RTDN is a state-change hint](https://developer.android.com/google/play/billing/rtdn-reference); the server fetches Google state before granting/revoking. Pending states are also retried without needing the player to reopen the game.

Chargebacks can appear in the [Voided Purchases API while other order sources disagree](https://developers.google.com/android-publisher/voided-purchases). Therefore authenticated Voided API rows revoke directly, after matching the recorded token and order, even when ProductPurchaseV2 still says purchased. The server scans an overlapping 29-day window, follows pagination (maximum 100 pages per invocation), rejects unsupported partial quantities, and records revocation monotonically. Replayed or out-of-order success events cannot reinstate a revoked credit. Unknown tokens are ignored until a verified client claim; an already-consumed token is never re-imported.

`POST /internal/maintenance` requires the distinct configured scheduler identity. Run it **every five minutes**, with retries, to consume durable credits, check pending purchases, finish interrupted account deletions, reconcile voids, and purge expired audit/rate-limit/deduplication documents. A Firestore lease prevents simultaneous workers. Due-time ordering and per-record backoff prevent one broken row from indefinitely starving later work. A failure returns 503 for retry; no message containing a token/profile is logged. Monitor failures, oldest due job, deletion backlog, pagination limits, and counts—not just process health. More than 29 days without successful void reconciliation creates an unrecoverable API coverage gap; alert long before that.

## Data boundaries

Wallet, requests and purchase document keys hash their UID/token identifiers. Active purchase rows retain the opaque token and order ID in private Firestore to enable consumption and reconciliation; Google/Firestore encrypt storage. These rows are never client-readable, never included in HTTP success/error responses, and never logged by application code. Hashes are pseudonymous identifiers, not anonymous data.

Deletion first freezes value changes and creates a durable job, then deletes the Firebase user and erases the wallet, inventory and spend/use receipts. The retained purchase audit contains only token-hash key, hashed owner, product, quantity, credit/refund totals, timestamps and terminal flags; it drops raw UID, token and order ID. A completed wallet/deletion tombstone and audit are retained for `AUDIT_RETENTION_DAYS` (365 by default), then purged in bounded maintenance batches. This is a scheduled purge, **not a claim that Firestore TTL was enabled**; deploy the worker and monitor backlog. An interrupted deletion temporarily retains the UID in its restricted job so it can finish; completion erases it. Cloud platform logs/backups have their own configured retention, which the owner must document.

CORS permits exact HTTPS origins only, including the external account-deletion site, without cookies or wildcard origins. Requests have bounded headers/JSON, exact schemas, and an opaque-token length limit. A per-UID Firestore rate limit works across instances, while bounded concurrent work protects each process. Socket IPs are not treated as player IPs behind Cloud Run. Edge rate limits, App Check/Play Integrity, budget alerts, operational monitoring, and real device/load tests are additional deployment hardening; none were silently marked as deployed here.
