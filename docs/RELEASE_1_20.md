# Luma Drift 1.20 — production F accepted, delivery pending

**Production F is accepted; publication is pending.** Production `1.20.0-rc1` and separate Lab `1.20.0-sandbox-rc1`, code28, retain the frozen bytes below. The root accepted the [final exact-F native index](validation/v1.20/native/candidate-f-final-index.json):11 passing reports,457 native assertions,180 touchscreen actions,76 original PNGs and one unedited60-second recording. Another38 read-only derived assertions are separate. Six exact-F FR/EN home/shop/victory frames are [recorded](validation/site-v1.20/native-capture-map.json). [Canonical package collection](validation/v1.20/publication/package-collection.json) passes and writes the [immutable local artifact manifest](../releases/manifest-1.20.0-rc1.json); source publication, local-site acceptance and Hosting1.20 remain pending. The separate [Lab test-Gold purchase/cold scope](validation/v1.20/native-lab/README.md) passes48 native assertions/13 touchscreen actions/6 PNGs, with12 derived assertions separate:500→450 test Gold, energy0→10→9/cold9, no second credit/debit and exact production-profile preservation.

E is intermediate. Its six-family skill traces remain E reports with129 assertions/74 gestures; the final index verifies six unchanged core model hashes in F. The [E wallet source/geometry witness](validation/v1.20/hud-audit/README.md), C startup crash and failed observer executions remain preserved with their actual scope.

## Candidate F identity

[Immutable F source/package record](validation/v1.20/candidate-f/candidate.json), covering 481 frozen app inputs · [F build and lint](validation/v1.20/candidate-f/build-lint.txt). Build, lint, signing and exact bytes pass separately from native acceptance.

| Channel | Version / code | Bytes | Candidate F SHA-256 |
| --- | --- | ---: | --- |
| Production | `1.20.0-rc1` /28 | 368,936,921 | `e4fa1a70dbb2e842c9a16a9f22d0f2a49a4316ccbc6013fe0acfa233093f5d26` |
| Separate Lab | `1.20.0-sandbox-rc1` /28 | 368,920,220 | `7c8ccfde3a0050b35b0312a61e2cb07973c594629bbf97493ec8bbe2829410cf` |

## Preserved intermediate E identity and earlier failures

[Immutable E source/package record](validation/v1.20/candidate-e/candidate.json), covering481 frozen inputs · [E build and lint](validation/v1.20/candidate-e/build-lint.txt). The preceding [D record](validation/v1.20/candidate-d/candidate.json) remains intermediate.

| Channel | Version / code | Bytes | Candidate SHA-256 |
| --- | --- | ---: | --- |
| Production | `1.20.0-rc1` /28 | 368,936,921 | `43db86e350d5247692d0b72d35cefecd304fb5b2f72f59a0d95788f95ea8be8f` |
| Separate Lab | `1.20.0-sandbox-rc1` /28 | 368,920,220 | `2c3dcc327c8b46b9a613fc368adf2164fba1b2a3c9a11fdbbbff31a5547b010b` |

The retained signer is `dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287`. These are candidate byte identities, not download or deployment receipts. Large APKs remain ignored external artifacts.

Earlier 1.20 candidate C crashed at production guest startup when a nullable Gold balance was unboxed, before a valid 1.20 UI frame or gameplay gesture. [Failure status](validation/v1.20/candidate-c/qualification-status.json) · [Original Android runtime failure](validation/v1.20/native/candidate-c-preflight-fr/failure-android-runtime.txt). D corrects nullable Gold handling and adjusts victory-art bounds and animation scheduling. Its native preflight and cold-victory scopes pass, but the normal shop view truncates a category label and places the main action beneath navigation. E shortens the category labels and moves only product content up 144 logical units with matched draw/hit transforms; its exact bytes remain frozen above. A later E review established a possible piece-direction swipe under the fixed wallet at a526.5-unit viewport; F adds a selection guard for the wallet's0–42 band. The witness is source/geometry evidence, not a native reproduction. Earlier build/JVM passes do not cancel C's failure or qualify a later candidate. No 1.19 images are reused as 1.20 media.

## Resulting interaction

The home has a light background, equipped companion, actual owned team, one Play action, real ready-gift count and a visible boutique. Five fixed destinations are Home, Squad, Collection, Gifts and Boutique; house and atelier remain secondary. The header distinguishes a device-local guest from a connected Google identity without promising cloud synchronization.

LumaCoins, LumaGold, session energy and companion Charge are separate values:

| Value | Meaning and limit | Source / use |
| --- | --- | --- |
| LumaCoins | Earned device-local currency | Victories and eligible gifts; earned shop prices |
| LumaGold | Separate authenticated account wallet; unavailable production balance stays unavailable | Premium flows remain gated; Lab uses an isolated 500-Gold test wallet |
| Session energy | Starts/caps at 10; regenerates 1 per 10 minutes | New campaign U11+, daily/weekly, Remix and expedition attempts cost 1; tutorial, campaign U1–U10 and friend practice are free. Same-attempt resume is free; victory itself costs nothing. |
| Companion Charge | Attempt-local, starts at 3 and caps at 6 | A distinct exited piece credits 1 once; talents spend their declared cost. Undo/re-exit cannot farm Charge. It does not pay for a new attempt. |

Existing saved attempts retain their action policy. New relay policy6 keeps physical refusals free and makes Undo itself free while retaining the previous exit's cost. Re-exiting a piece therefore spends another action. Three stars require the minimum productive exits; two permit half the correction allowance; larger corrections or a continuation give one. The visible active-play clock pauses with app/help/pause and does not grade stars. [Gameplay and interaction contract](UX_1_20.md).

Daily login, daily/weekly quests, event completion, Journey and power-trial gifts use their existing durable claim APIs. A positive committed result opens an announced reveal that waits for click/Back, then fades out 180ms or closes immediately with reduced motion. Claimed rows have no claim callback. The illustrated shop shows one featured item at a time, actual stock/ownership/prices and previews before any draw/purchase. [Menu contract](UX_1_20_MENUS.md).

The premium refill protocol prepares 10 session units for 50 LumaGold, only at zero energy. Server debit/stock and spend/use receipts are account-bound; local credit is UUID-idempotent. Production client and server gates remain closed. Unconsumed packs can follow the account; credited energy and its markers remain installation-local. Clearing app data does not restore an already-consumed local credit. No new production energy-backend deployment, external purchase or live Gold debit is claimed. [Protocol, measured tests and limits](ENERGY_PURCHASE_1_20.md).

## Executed scopes

Counts describe executed assertions/tests, including repeated checks; they are not unique features or players and should not be added into an acceptance score.

| Evidence | Result and scope |
| --- | --- |
| [Latest menu/shop-E layout/data](validation/v1.20/menu/shop-e/README.md) |73,838 assertions over 504 physical configurations; short six-language category labels, primary-action reachability and aligned transforms, actual claim/duplicate/refused-commit models. Earlier 72,282 scope is preserved separately. |
| [Focused Journey/result](validation/v1.20/host/focused-journey-final-art.json) |178,587 assertions; all 1,000 U boards,4,000 solved runs within allowances and 1,000 deliberately exhausted runs, policy5/6, grades, continuation ordinals, real local receipt recovery and pure layout |
| [Session energy/reactions](validation/v1.20/host/session-energy.txt) |20,202 JVM assertions; local pacing and reaction contracts |
| [Companion policy6](validation/v1.20/host/companion-policy6/host-report.json) |2,313,118 assertions over 1,000 boards ×37 identities; pure draft/budget/history/anti-farm contracts |
| [Exhaustive U model](validation/v1.20/host/relay-current/relay-host.json) |13,312,469 assertions;191,954 reachable states and 471,517 transitions; source-level puzzle/persistence contracts |
| [Staged website JS](validation/site-v1.20/runtime-contracts/README.md) |57 local Node tests, including9 staged1.20 runtime behaviours with DOM doubles; no prepared1.20 page/browser/network/remote-CI claim |
| [Exact-F native acceptance](validation/v1.20/native/README.md) |11 passing reports;457 native assertions/180 touchscreen actions/76 PNGs/one60-second video;38 derived comparisons separate. Real grades, gifts, cold/zero-energy/expedition, compact and six-language menu paths; one isolated emulator |
| [HUD source/geometry review](validation/v1.20/hud-audit/README.md) | E witness preserved as source/geometry evidence; exact F independently passes16 four-direction wallet swipes, including an observed piece/HUD overlap |
| [Exact Lab purchase/cold](validation/v1.20/native-lab/README.md) |2 passing F reports;48 native assertions/13 touchscreen actions/6 PNGs,12 derived checks separate; two observer-stopped attempts excluded, including the182-assertion/110-touch earned prerequisite |
| [Premium protocol](validation/v1.20/energy-purchase/README.md) |84 controller assertions,36 actual-client transport assertions,69 Node24 server tests,4 local Firestore integration tests and 1,552 shared-vault regression assertions; no provider/native/payment acceptance |

## Acceptance and publication gates

| Gate | Current state |
| --- | --- |
| F signed-byte identity | Frozen above: 481 inputs, exact production/Lab hashes, build/lint and retained signature |
| Accepted production native candidate/report | PASS: final F index/root acceptance,11 reports; E skill reports remain separately identified |
| Canonical artifact manifest/immutable source commit | Local exact-F collection/manifest PASS; immutable publication commit pending |
| Six original home/shop/victory captures, FR/EN | PASS: six exact-F originals and capture map; E originals remain intermediate |
| Prepared1.20 public source and local browser receipt | Media and canonical artifacts accepted; source preparation/local-browser receipt pending |
| Lab refill/cold native qualification | PASS:48 native assertions/13 actions/6 PNGs, plus12 separate derived checks; no real funds or provider activation |
| Clean deployment source commit / Hosting receipt | Pending source qualification and deployment |
| Public-origin render/byte observation | Unverified under the saved origin permission boundary; no workaround |
| Live ads, real checkout, Play publication | Separate qualification; not activated here |

[Evidence index](validation/v1.20/README.md) · [Staged website workflow](validation/site-v1.20/README.md). Source preparation uses the six accepted exact-F originals and collected package metadata. Gallery/audio/account/configuration and older archives stay protected; the policy update is intentional, separately hash-recorded and must not be described as unchanged C3 legal content. Later1.19→1.20 byte deltas must reconstruct and verify both complete final APKs before Hosting. No 1.20 deployment is inferred from scripts, local files or signed-package verification.

The1.19 C3 site is the last published delivery: [Hosting](validation/site-v1.19/hosting/deployment.json), [500-response full-byte verification](validation/site-v1.19/public-origin.json), and a separately [blocked public browser observation](validation/site-v1.19/browser-public/blocked.json). Public-origin browser access must not be replaced with another browser/tool/indirect renderer. Local-site browser review and authenticated Hosting management remain separate allowed scopes.

Physical-phone comfort, actual speaker/headphone quality, first-player comprehension, voluntary return, retention and revenue remain unmeasured. Host checks and isolated emulator results cannot establish a hit.
