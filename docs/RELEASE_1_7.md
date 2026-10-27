# Luma Drift 1.7 — route recovery and a dressed-up journey

**27 October 2026 · published signed production `1.7.0-rc1` / Lab `1.7.0-sandbox` · code 8 · candidate under qualification**

The new source adds exact dead-end guidance, a scrollable journey, separate currencies, a visual wardrobe and a return loop that does not punish absence. This document distinguishes implemented behavior from final package, installed UI, service deployment and commercial acceptance. The [1.7 evidence directory](validation/v1.7/README.md) owns the executed record. Preserved [1.6 artifacts and tests](RELEASE_1_6.md) keep their original identities and limits.

## Signed package identities

The production and isolated Lab release APK/AAB files are packaged at code 8. The full release build/lint completed successfully; package metadata and source-input fingerprints are in [production](validation/v1.7/package-production.json), [Lab](validation/v1.7/package-lab.json) and their release manifests. Installed behavior and deployment are recorded separately below.

| Artifact | Bytes | SHA-256 |
| --- | ---: | --- |
| [Production APK](../releases/luma-drift-1.7.0-rc1.apk) | 5,594,242 | `7eeaa816853533f441acfc73f4ad3f6af0fb2cbdd6fd0eae1fda3f64c388a2bf` |
| [Production AAB](../releases/luma-drift-1.7.0-rc1.aab) | 8,948,843 | `b5bfc1e51b272bc8046c3e79efcf95ae79814913e87d522ff6062e3f81d6ec75` |
| [Lab APK](../releases/luma-drift-1.7.0-sandbox.apk) | 5,593,925 | `d7fea345c1f33cc8e114688ee06d8b60ed1278cc29eeb328c677c6c1858ba049` |
| [Lab AAB](../releases/luma-drift-1.7.0-sandbox.aab) | 8,941,491 | `2e6f8412e42979b00238689134abd335b10fc040780d6f36b6085fed37a757c8` |

Both package manifests declare sample ads, the paid cosmetic draw gate false, an empty real-billing wallet origin and a configured social/catalogue origin, with `live_services_validated: false`. A configured origin is not deployed-route or provider acceptance.

Source/artifacts were published at [main 4c5f5ca8](https://github.com/nabz0r/AndroidHit/commit/4c5f5ca8bc5f74173e283377275bb352c76534c7), tree `937609296309afc56247e581745a3456bbd15905`. [Independent public raw-download verification](validation/v1.7/publication.json) records HTTP 200 and exact byte/hash matches for all four APK/AAB files. This establishes repository delivery, not Play approval.

## The level-13 question

All **1,000 Journey J boards and 240 Remix K boards** finish within their original initial allowance without a power, ad, extension or payment. The independent audit visits **1,101,868 reachable subsets** and **3,348,959 legal transitions** and passes **49,063,063 assertions**. No geometry, seed, identity, solution certificate or starting budget was changed.

The frozen J13 (`LD1-J-D-6881B9AC`) has ten pieces and 13 initial actions. One full completion is `[0,1,2,3,4,5,6,7,8,9]`, leaving three actions. But the legal prefix `[0,1,2,3,8]` closes a gate required later. Another legal exit remains, so the old `isStuck()` returned false even though no completion remained. One Undo restores a path; adding actions alone does not reopen the gate. [Full diagnosis and coordinates](validation/v1.7/board-audit-journey13.md).

The user's exact installed build/save was unavailable. This is a reproducible failure mechanism in J13, not a claim to have reconstructed their tap sequence. All eight historical board-13 families are initially solvable too. [Independent method, data and limits](validation/v1.7/board-audit.md).

## Candidate scope

| Area | Implemented behavior | Evidence/contract |
| --- | --- | --- |
| Actual board state | Exact `PLAYABLE`, `NEEDS_ACTIONS`, `DEAD_END`, `SOLVED` guidance; safe next-piece hints and undo recovery | [Board audit](validation/v1.7/board-audit.md), [UX](UX_1_7.md) |
| Journey | Continuous map from 1 to 1,000, visible locked positions, current-level return and level jump; ten 100-level biomes | [UX](UX_1_7.md) |
| Atmosphere | Biome tint and ten original procedural arrangements; old biome-zero buffer retained | [Art/audio](ART_AUDIO.md) |
| Daily return | Seven claimed visits with 40/50/60/70/80/100/150 earned coins, no lost-day reset | [Economy](ECONOMY_1_7.md) |
| Quests | Three daily and three weekly objectives, distinct period IDs, first-clear credit and idempotent claims | [Economy](ECONOMY_1_7.md) |
| Wardrobe | 24 visual-only items, Hat/Outfit/Shoes/Gloves, permanent ownership, exact source/progress, native avatar preview | [Economy](ECONOMY_1_7.md), [UX](UX_1_7.md) |
| Currency | LumaCoins earned locally; LumaGold in the separate premium account wallet; no automatic conversion | [Economy](ECONOMY_1_7.md) |
| Earned capsule | 150 LumaCoins, category/individual odds, rarity-based duplicate compensation, atomic debit/outcome/ownership | [Economy and odds](ECONOMY_1_7.md) |
| Social source | Authenticated mutual friends, opt-in shared map position and limited one-hint gifts; honest unavailable/retry states | [Server](../server/README.md), [UX](UX_1_7.md) |
| Premium preparation | Native authenticated account catalogue/odds and authoritative server cosmetic transaction code; APK and server draw gates both false | [Server](../server/README.md) |

The new four-slot wardrobe is not the pre-existing functional equipment system. All new wardrobe items are cosmetic. Existing 18 Head/Aura/Charm items keep their documented bounded benefits and saves. Existing 24 companions, six themes, six powers, events and historic challenge families remain.

## Executed source checks

- [Board reachability audit](validation/v1.7/board-audit.md): all 1,240 boards, reachable states, exact hints, gates/keys/seals, recovery and fixed allowances.
- [Rewards/wardrobe checks](validation/v1.7/rewards-cosmetics-tests.txt): 100,953 checks including failed writes before/after memory application, cold recreation, direct purchases, odds, gift caps, 12 concurrent store handles and six-language entries.
- Fixed-seed capsule simulation: 100,000 outcomes, **60,072 common / 29,862 rare / 10,066 epic**. This checks implementation distribution, not human response or revenue.
- [Account vault](validation/v1.7/vault/README.md): 1,552 protocol/client model checks for actual offline server fixtures, account-scoped ownership, conditional odds, false build/server gates, receipt recovery and account-change races; no real provider/network/draw is inferred.
- [Social copy](validation/v1.7/social-strings-tests.txt): 1,076 source/JVM checks across 59 extracted UI/error copy pairs and all six languages; original FR/EN accessibility labels remain exact, with formatted gift quota. This is not installed/provider or native-speaker acceptance.
- [Biome audio](validation/v1.7/audio/README.md): 558 host DSP/lifecycle checks, ten generated arrangements and retained byte-identical historical theme/effects; installed playback is a separate check.
- [Existing data](validation/v1.7/data-tests.txt): 8,882 checks. [Meta progression](validation/v1.7/meta-tests.txt): 362 checks plus exported JSON validation.

These reports identify their own source scope. JVM Android/storage doubles are synthetic; they do not inject a physical disk failure. A passing exhaustive model does not establish first-time comprehension, human difficulty or retention.

## Installed final Lab menu acceptance

[Final menus2](validation/v1.7/ui-sdk-final-menus2/host-report.json) passes **38 native assertions** with **32 unedited PNGs**, on Lab APK `d7fea345c1f33cc8e114688ee06d8b60ed1278cc29eeb328c677c6c1858ba049`, API 36 arm64 and a 16,384-byte page-size kernel. It covers one daily-gift credit and a zero-credit repeat, all four starter slots/equip/no-spend cancellation, earned/premium shop distinction, disabled guest vault, native map jumps to 1/13/100/101/1000 and scroll, honest social guest state, complete earned-capsule odds/exact debit/outcome/equip and FR/EN presentation.

The run uses an actual guest qualification session without preference injection; initial welcome/earned coins, daily reward and capsule outcome remain test-session activity. The original private preferences were restored byte for byte. This is not a real Google sign-in, social connection, premium purchase, physical-phone or production-package test. The first final-menu attempt failed a stale test label and retains its own report; menus2 is the passing source for these images. [Illustrated tour](VISUAL_GUIDE.md).

## Separate final gameplay and production observations

[Final native SDK qualification](validation/v1.7/SDK_UI_FINAL.md) also records **34 Lab gameplay checks/9 captures**: actual Journey 1–13 play without seeded progress, J13 safe hint/one-charge behavior, unaffordable-hint refusal, legal dead-end recovery and landscape. The separate final production package passes **7 startup checks/1 capture**; its actual existing guest profile resumes a preselected friend challenge. AudioFlinger advances **21,992 frames** and observes background pause/foreground resume. All original preferences are restored. These are identified emulator observations, not real Google/social/payment or physical speaker acceptance.

## Package, deployment and acceptance gates

Final APK/AAB identities, signature/alignment checks, native screenshots and Android scenarios are associated with their exact artifacts in [1.7 qualification](validation/v1.7/README.md) and [Install & verify](RELEASE.md). No 1.6 binary or screenshot is relabelled as a 1.7 result.

[Cloud Run revision `luma-wallet-00005-pps`](validation/v1.7/cloud-run.json) is deployed at 100% traffic from source `23a19645`, with paid draws false and a three-instance maximum. Recorded probes return health 200 and unauthenticated 401 on social/catalogue paths. These are deployment/basic access-gate checks: mutual requests, two real authenticated accounts, privacy opt-in, account switching, limits and receipt recovery still need their own real-account acceptance. A friend position is a shared snapshot, not cloud progression. Real Google login remains independent of cached session/controller tests.

The native account vault prepares authenticated catalogue loading, account ownership, conditional odds and transaction recovery. Both APK BuildConfig and server environment draw gates remain false. An account-new cosmetic can already exist locally; no account guarantee promises a locally new item. Paid cosmetic draws remain closed and there is no active premium sale. Sample ads and existing checkout gates remain. Live ad demand, Play license-test purchase/refund/recovery, store approval and revenue are unqualified unless an explicit later record says otherwise.

The [website’s local qualification](validation/site-v1.7/README.md) now passes 2,219 source assertions, 24 deletion-core tests and 18 CUA views at actual CSS widths 320/390/1366 across all three routes and FR/EN. Its eleven copied final Lab images have exact native provenance. Firebase Hosting completion now confirms publication from exact main `4c5f5ca8bc5f74173e283377275bb352c76534c7`, with all 47 payloads hash-verified before deployment. The separate delivery record uses CLI/management evidence; no public Firebase-origin byte comparison was performed. [Roadmap and remaining human/device gates](ROADMAP.md).
