# Server validation evidence — 26 October 2026

The original measurements below cover the server source in the 1.4 release candidate. They did not establish a deployed backend, real Google sign-in, a real Play purchase, a configured merchant account, a container build, or production load capacity.

**Later deployment follow-up:** the owner has deployed Cloud Run; external probes reached the application and rejected an anonymous wallet request. The original `/healthz` probe conflicted with a documented reserved URL. The corrected `/health` route passed the expanded **43-case suite under Node 24.22.1**; the redeployed public route now returns HTTP 200, with anonymous wallet rejection and the final website preflight also observed. [Incident, observed results and exact scope](../docs/validation/cloud-run-health-route.md). The original 42-case and Firestore-emulator records below remain historical evidence rather than being rewritten as new results.

## Measured results

| Check | Result | Actual environment |
|---|---|---|
| Core, HTTP socket and production adapter/config tests | **42 passed, 0 failed, 0 skipped** | Node **24.22.1**, macOS arm64; fake Firebase/Play providers for this suite |
| Firestore production adapter integration and security rules | **2 passed, 0 failed, 0 skipped** | Node **24.22.1**, Java **21.0.13**, Firebase CLI **15.34.1**, official Firestore emulator **1.22.0**, `demo-luma-wallet` only |
| Source syntax check | Passed | `npm run check` |
| Dependency audit | **0 reported vulnerabilities** | `npm audit --omit=dev`; pinned `firebase-admin` **14.5.0**, `google-auth-library` **11.3.1**, committed lockfile |
| Owner Google Cloud / Play / Firebase account operations | **Not performed** | No resources, keys, charges or real purchase accounts used |

An earlier Firestore integration attempt using a custom fake Admin SDK credential was refused by the SDK. The test setup was corrected to the emulator-supported default credential path and rerun successfully under Node 24. The emulator produced a metadata-discovery warning when no cloud credentials were present; the explicit `demo-` project and loopback emulator remained the only data target. No authentication/purchase provider test was represented as a real-money test.

## What the tests exercise

The in-memory adapter serializes committed transactions. The independent Firestore adapter test uses the real emulator transaction engine: **12 concurrent purchase claims across two adapter/service instances** create exactly one 500-gold credit, then **8 concurrent retries of one cosmetic redemption** produce one debit. The test restores ownership, verifies refund debt, completes account deletion, checks token/order redaction, and exercises deployed-style rules against direct unauthenticated REST reads/writes. It does not infer multi-region throughput or live IAM correctness from an emulator.

The 42-test suite covers:

- Every Play pack's exact server-defined gold amount; unknown product/SKU and extra client price/gold fields rejected.
- **64 simultaneous claims** of one purchase token, cross-user token replay before/after credit, and strict SHA-256 UID binding, including malicious non-ASCII owner strings.
- Wrong product, multiple line items, unsupported quantity, rental/preorder and invalid provider state; no arbitrary client credit route.
- Pending payments grant nothing; pending `refundableQuantity: 0` or omitted default fields remain nonterminal and later complete successfully. Unknown consumed tokens cannot re-import gold.
- Consumption outages after credit, an ambiguous database response after a durable credit, and a crash after Google consumes but before the consumed marker is persisted. Retries never produce a second credit.
- Fixed Catalog companion/skin prices compared directly with Android source; one stable idempotent spend receipt, ownership restoration, conflicting request rejection and concurrent insufficient-funds protection.
- Reserved server inventory/use primitives (production-disabled), exact one-unit use and request/action conflict handling.
- Replayed refunds and debt after spending; authenticated Voided API chargebacks that still appear purchased in ProductPurchaseV2; strict order binding and monotonic revocation.
- A batch-size-one retry regression: one permanently failing consumption row is backed off so another purchase can still be consumed. No first-row starvation.
- Recent Google login before deletion; deletion provider outage, crash after Firebase deletion but before redaction, blocked concurrent new spending/purchases while frozen, and completion from a durable job without a deleted user needing to log in again.
- Configured audit expiry purge, no raw token/order left in a completed deletion audit, and no replay after an old consumed token's audit expires.
- Real loopback HTTP routes, missing/expired/non-Google authentication, exact schemas, bounded JSON, safe error bodies, configured CORS/preflight, distinct Pub/Sub/Scheduler identities and expected subscription.
- UID rate limits shared by two service instances. Process work remains bounded **after clients disconnect**, while health checks still respond. No player-IP assumption is made about Cloud Run proxy sockets.
- Production fails closed on missing IDs, insecure/wildcard CORS, emulator settings, downloaded key-file configuration and invalid retention settings. Google API host/path/encoding, request timeout and partial-refund pagination are asserted.

## Reproduce

From `server/`, with Node 24:

```sh
npm ci
npm run check
npm test
npm audit --omit=dev
```

HTTP tests require loopback sockets. If a development sandbox disallows binding a local port, run that suite with permission for local networking; do not silently replace HTTP tests with mocks or mark the blocked run as passed.

The separate emulator suite needs Java 21+ and local ports 8787/9150 plus Firebase's emulator hub. It downloads an official emulator if it is not cached. It refuses to run unless `FIRESTORE_EMULATOR_HOST` points to `127.0.0.1:PORT`; the project ID is hardcoded to `demo-luma-wallet`:

```sh
npm run test:firestore
```

Do not set the owner's project ID or credentials for this suite. The production entry point refuses emulator environment variables. Database test setup uses the Admin SDK's emulator credential path; the owner must independently verify deployed client-deny rules and managed-identity IAM before activating real purchases.

## Remaining deployment evidence

Follow [the production acceptance gate](DEPLOYMENT.md#6-internal-track-acceptance-gate-before-real-sales): real owner Firebase Google authentication, Play license-test approved/declined/pending cases, server consumption, restart/reinstall/two-device restoration, refunds/RTDN, authenticated Scheduler, direct database denials, account deletion and operational alerting. Record measured latency/traffic against the actual deployment before describing it as production load-tested. No physical phone or Google merchant checkout was used for these server tests.

## 1.7 social and closed premium-draw follow-up

The expanded suite passes **62 tests under Node 24.22.1**, and the real local Firestore emulator passes **3 integration/rules tests**. [Current records and scope](validation/social-v1.7/README.md) include raw output, finite gift replay/concurrency, opt-in map privacy, mutual deletion cleanup, closed premium draws and actual conditional odds. These local results do not qualify production Google login, provider accounts, live gift delivery or real-money products. Earlier 42/43-case records above remain historical evidence.

## 1.20 prepared energy-purchase follow-up

The complete server suite now passes **69 tests under Node24.22.1**, and local Firestore passes **4 integration/rules tests**. The added energy case verifies12 concurrent retries of a50-Gold purchase and12 uses across two independent Firestore adapters: one pack, one debit, one ten-unit UID-bound receipt. Memory/HTTP cases additionally cover distinct-request stock/balance races and lost replies after committed spend/use.

[Pinned source hashes and raw records](../docs/validation/v1.20/energy-purchase/README.md) also include84 Android-controller/journal assertions,36 checks of the actual wallet client with transport doubles, and1,552 shared vault regression assertions. New energy sales remain disabled on both client and server, with no backend deployment or real payment. [Protocol and installation-local limits](../docs/ENERGY_PURCHASE_1_20.md).
