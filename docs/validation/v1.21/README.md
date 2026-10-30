# 1.21 qualification and delivery

The frozen release candidate is **B**, production `1.21.0-rc1` and separate Lab
`1.21.0-sandbox-rc1`, both version code **29**. Its [immutable candidate record](candidate-b/candidate.json)
pins all 493 application/build inputs and both signed APKs. The
[canonical package metadata](../../../releases/manifest-1.21.0-rc1.json) records
package identity, byte count, SHA-256 and the existing signing certificate.
Package collection is not a publication or native acceptance receipt.

**B passes the scoped native release gates.** The production
[final index](native/candidate-b-final-index.json) contains 13 passed phases,
574 native assertions, 149 actual touch actions, 52 original PNGs and two
recordings. Its 38 derived comparisons remain separate. The isolated Lab
[final index](native-lab/candidate-b-final-index.json) contains three passed
phases, 99 assertions, 41 actions, nine PNGs and one recording, with 24 derived
comparisons separate. The six production preference maps remain identical
before and after Lab testing.

| Scope | Evidence and interpretation |
| --- | --- |
| Model and simulation regression | [Host index](host/README.md), including every U1–U1000 reachable board, all companion identities, old puzzle families, elemental layers, kitchen, versioned economy, energy and transaction recovery. B's dependency comparison preserves the original tested source identities. |
| Economy and summon analysis | [Executed arithmetic/cohort simulation](host/economy-analysis.md) and [design/research](../../design/v1.21/REWARD_AND_ARCADE.md). Fixed-seed samples and modeled routes do not measure player behavior. |
| Exact B production Android | [Native qualification](native/README.md), with executed touches, unedited media, cold restoration and an earned profile. Candidate A's failed observations remain preserved under their original names. |
| Separate Lab Android | [Isolated Lab purchase/energy qualification](native-lab/README.md) is recorded separately from production; test Gold is not a real purchase. |
| Website and independent browser simulation | [Site preparation and local browser records](../site-v1.21/README.md). The fictional resettable browser profile does not modify Android saves. |
| APK transfer | [Delta transport record](apk-delta/transport-b.json), [exact remote reconstruction receipts](apk-delta/cloudshell-transfer.json) and separate local receipts bind exact signed B bytes; reconstruction does not replace signature or native checks. |

Production APK SHA-256:
`e382a73037418146bf57e1530998efeb312953ea8b9b5bb61b548af7cbf90bcc`.
Lab APK SHA-256:
`9c0998683f09a1d940a97a0dc5badea98a5ab5c84ea2ba814917e30f8df5235b`.

New attempts opt into the 1.21 economy: two free Undo operations followed by
explicit confirmed Coin costs, logarithmic first-clear entitlements and power
prices, and an energy capacity increasing from 10 to 16 without a balance grant.
Older saved attempts retain their declared policy. Element clearing is free;
blocked policy-6 moves remain free. Talent Charge, session energy, earned Coins
and account Gold have separate purposes and persistence.

The [delivery register](publication/release-delivery.json) records source C1
`35bb82229d8175968919b5979264e5094a1cc8dc` and website C2
`71bba893a5fbfeb808e5c44840b6701862255453`. The exact C2
[hosted contracts pass](publication/c2/actions-40976045727/summary.json), and
[Firebase management](../site-v1.21/hosting/deployment.json) confirms
DEPLOY/FINALIZED version `d08a2aa131c05c4f`, released
`2026-10-30T00:08:28.331Z`. [Original receipt collection and hashes](../site-v1.21/hosting/receipt-collection.json)
and [unchanged tested/deployed source](publication/c3-source-equivalence.json)
keep delivery evidence separate from application qualification.

The saved public-origin refusal remains in force:
neither public response bytes nor public rendering are inferred from local QA or
Firebase management. Physical-phone experience, owner acceptance, enjoyment,
retention and real payment/provider approval remain unmeasured by these checks.
