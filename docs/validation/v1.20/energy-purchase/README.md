# 1.20 energy-purchase qualification

All results below are local tests. Production energy sales are closed on both Android and server. No cloud deployment, real Firebase/Play purchase or real Gold debit is inferred.

| Measured check | Result | Real code / controlled boundary |
|---|---|---|
| Controller and durable journal |84 assertions PASS | Actual `EnergyPurchaseController` and `EnergyPurchaseJournal`; explicit Android energy/HTTP doubles |
| Wallet request/response transport |36 assertions PASS | Actual `ProductionWalletClient`; fake Firebase token tasks and `URLConnection`, no socket or TLS exchange |
| Complete server suite |69 tests PASS | Node24.22.1; real loopback HTTP sockets, fake Firebase/Play providers, memory transaction adapter |
| Firestore integration/rules |4 tests PASS | Node24.22.1, Java21.0.13, Firebase CLI15.34.1, Firestore emulator1.22.0, loopback `demo-luma-wallet` only |
| Shared vault adapters regression |1,552 assertions PASS | Existing cosmetic vault suite, offline real-server fixtures; no production request |

The Firestore energy case uses two independent production adapters/services:12 concurrent identical purchases debit50 Gold once and add one pack;12 identical uses consume that pack once and return one stable, ten-unit UID-bound receipt. Wrong-account use is refused. Existing refund/deletion, client-deny rules and social integration cases also passed. The emulator's SDK metadata-discovery warning is retained in the output; the suite refuses any non-loopback emulator and hardcodes the demo project.

The server HTTP case deliberately loses a database reply after a committed spend/use, receives503, then replays the same IDs successfully. Distinct concurrent requests cannot overdraw either stock or Gold. Closing new sales preserves earlier receipts and stock redemption.

The client transport test initially failed its decimal-balance assertion: Maven `org.json` represented1.5 as `BigDecimal`, which the former generic `Number.longValue` allowance accepted. The failure is preserved in `client-transport-before-exact-integer-fix.txt`. `exactLong` now accepts only `Integer`/`Long`; the subsequent36-case run passes. This was measured against the JVM contract library, not an observed malicious server response on a physical Android device.

Raw output: [controller](client-jvm.txt), [actual client transport](client-transport-jvm.txt), [Node24 server](server-node24.txt), [local Firestore](firestore-node24.txt), [shared vault regression](shared-vault-regression.txt). The [hashed summary](summary.json) pins source/test/log inputs and `org.json20240303` (SHA256 `3cf6cd6892e32e2b4c1c39e0f52f5248a2f5b37646fdfbb79a66b46b618414ed`). The [protocol and limitations](../../../ENERGY_PURCHASE_1_20.md) describe all recovery phases and the installation-local energy boundary.

Reproduce from repository root with Java17 and a real `org.json` JVM jar via `LUMA_JSON_JAR`:

```sh
bash scripts/test-energy-purchase-v120.sh
bash scripts/test-wallet-energy-transport-v120.sh
```

From `server/`, using Node24 and Java21+:

```sh
npm test
npm run check
npm run test:firestore
```

Android application build, native Lab UX, physical-device feel and all production-provider acceptance remain separately recorded by the release owner.
