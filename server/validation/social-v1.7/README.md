# Social and closed premium-draw backend validation — 1.7

Executed locally on macOS arm64, 27 October 2026. These results qualify local implementation behavior, not live Firebase authentication, production IAM, real gift exchange, Play checkout or paid monetization.

| Suite | Observed result | Runtime / boundary |
|---|---|---|
| Core, real HTTP sockets, production adapters/config, social, cosmetic draw | **62 passed, 0 failed, 0 skipped** | Node **24.22.1**; in-memory transactions; fake Firebase/Play providers; actual loopback HTTP sockets |
| Real Firestore adapter contention and direct client-deny rules | **3 passed, 0 failed, 0 skipped** | Node **24.22.1**, Java **21.0.13**, Firebase CLI **15.34.1**, official Firestore emulator **1.22.0**, loopback `demo-luma-wallet` only |
| Java source integration | Parent release build records | Production and Lab Java compilation reported successful by the main release task; emulator/device social guest states must be recorded there |

Evidence: [Runtime](runtime.txt), [Node output](node24-tests.txt), [Firestore output](firestore-node24.txt). The Firestore SDK emitted a metadata-discovery warning with no cloud credential; the demo-project guard and loopback emulator were the only database targets. No provider account, production document or real payment was used. An earlier run under Node 26 also passed, but the evidence here uses Node 24 to match the supported Firebase CLI runtime.

Reproduce the first suite with Node 24:

```sh
npm test
npm run check
```

Reproduce the second with Node 24 and Java 21+ on PATH:

```sh
npm run test:firestore
```

The social cases cover explicit mutual approval, rejection/withdrawal/removal, expired invitations, 40-friend / 20-pending bounds, a 20-per-day outgoing quota, opaque code collision handling, opt-in level privacy and clamping, three daily sends, one gift per pair/day, five first claims per recipient/day, bounded inboxes, idempotent concurrent send/claim receipts, wrong-owner claims, expiry, finite earned-only gift rewards, deletion/retry and purge despite a failed Play poll. The Firestore social case uses two independent service/store handles, 8 concurrent invitations, 12 concurrent gift sends yielding one gift/quota debit, 8 concurrent claims yielding the same grant, and deletion of the profile/code/relationships/gifts.

The prepared premium draw cases cover 24 disclosed item probabilities, no gameplay benefits, default-closed gate, concurrent exactly-once debit, receipt conflict, duplicate 50% refund, published conditional twentieth-draw guarantee, no balance, malformed requests and deleted accounts. They do not assert that paid loot-box use is approved in any country or for any age group. The candidate keeps the draw flag false.
