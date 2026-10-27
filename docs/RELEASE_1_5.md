# Luma Drift 1.5 — avatar and progression candidate

**27 October 2026 · source version `1.5.0-rc1` / code 6 · production package `com.androidhit.lumadrift`**

This quality candidate centers the lobby on the player's avatar, introduces three equipment slots with 18 fixed items, expands Journey to 1,000 boards alongside 240 Remix boards, adds six Sealed passages prototypes, tightens power activation and repairs the missing ambient music. Signed APK/AAB files, installed Android evidence and remaining acceptance boundaries are recorded in [the release](RELEASE.md) and [validation](VALIDATION.md).

## Player-facing changes

| Area | Candidate behavior | Implementation reference |
|---|---|---|
| Lobby | Equipped companion and equipment remain visible beside the next Journey board, with access to progression, missions and collection | [`LumaView.java`](../app/src/main/java/com/androidhit/lumadrift/LumaView.java) |
| Equipment | One item in each of Head, Aura and Charm; 18 fixed items, explicit unlock sources and limited bonuses | [Exact equipment contract](AVATAR_EQUIPMENT.md) |
| Journey | 1,000 fixed boards in progression version 2; 240 separate Remix boards | [`BalancedCatalog.java`](../app/src/main/java/com/androidhit/lumadrift/engine/BalancedCatalog.java), [`ProgressionV2Layouts.java`](../app/src/main/java/com/androidhit/lumadrift/engine/ProgressionV2Layouts.java) |
| Actions | Each board has a fixed allowance above the N certified exits; no player- or payment-dependent difficulty adjustment | [`MoveBudget.java`](../app/src/main/java/com/androidhit/lumadrift/engine/MoveBudget.java), `BalancedCatalog.mistakeAllowance()` |
| Sealed passages | Six separate prototype challenges with fixed wall cells opened by their matching key piece; no progression rewards or powers | [`SealedCatalog.java`](../app/src/main/java/com/androidhit/lumadrift/engine/SealedCatalog.java), [`Barrier.java`](../app/src/main/java/com/androidhit/lumadrift/engine/Barrier.java) |
| Powers | Explicit activation ownership, active-effect rejection and a 450 ms input debounce; six existing powers remain | [`PowerUsePolicy.java`](../app/src/main/java/com/androidhit/lumadrift/data/PowerUsePolicy.java) |
| Music | Original eight-bar, 25.6-second loop; repaired static PCM loading, entrance/toggle fades, focus handling and immediate background pause | [Audio change and executed checks](validation/v1.5/audio/README.md) |

The companion identity and theme remain cosmetic. Equipment benefits are separate, bounded and earned locally. There is no new paid equipment product, randomized paid reward or equipment cloud-save service. No benefit adds purchased gold or changes a gate, exit direction or board layout.

## Progression and compatibility contract

Current `BalancedCatalog.get()` and `.remix()` select the frozen version-2 Journey/Remix layouts. Their new challenge identities are **G (Journey 1–1,000)** and **H (Remix 1–240)**. C/R identify the original 240-board routes; B/Q retain the former balanced 240-board routes through `legacyGet()` / `legacyRemix()`. Old friend codes must continue to resolve to their original boards.

The new move policy uses **N successful exits plus a fixed mistake margin**. `mistakeAllowance()` stores that margin with each frozen board. The margin is 2–4 actions, including the early teaching allowance; it does not react to purchases, losses, elapsed time or ad choices. The content generation and exact choice analysis run offline, rather than generating or changing difficulty during play.

An interrupted older run retains its original board and budget policy until it is completed or abandoned; new runs use the new progression. The [actual 1.4 → 1.5 Lab upgrade](validation/v1.5/active-save-upgrade.json) preserved the active run ID, original board/history, policy 1, 12/13 remaining actions, balance and stock. Starting a fresh run then selected content 3 / policy 2 with 9/9 actions. That test predates the isolated share-image renderer fix; [the source comparison](validation/v1.5/binary-source-diff.json) confirms migration and storage sources are unchanged.

The [version-2 structural audit](data/progression-v2-summary.md) compares the same first 240 positions: under uniform choice among legal exits, mean modeled clear probability changes from **89.04% → 30.02%** for Journey and **57.96% → 22.55%** for Remix. A separate seeded blind-tap simulation counts blocked taps under each version's own action policy; the audit does not show a decrease under every technical policy. The full 1,000-board Journey has no 1,000-board historical counterpart. The audit also canonicalizes the eight rotations/reflections, translations, arrows, switches and gates to check distinct layouts within each route.

These technical policies are not human play and do not establish calibrated difficulty, improved enjoyment or retention. The generation script and CSV are [reproducible source artifacts](data/progression-v2-audit.csv). Observe players using the [roadmap's study protocol](ROADMAP.md#next--physical-quality-and-comprehension) before treating the additional content as a product improvement.

## Sealed passages — six prototypes

The Events menu gains a separate **Sealed passages** tab. P1–P6 are six hand-authored 5×5 boards with 4–7 pieces. Fixed wall cells block a piece's exit ray while the associated key piece remains on the board. Releasing that key dissolves its matching wall or walls; Undo restores the key and reseals them. Gates retain their normal directional rules, so a key that also toggles a gate may need to leave later rather than as soon as it becomes legal.

The set introduces one key/one wall, one key/two walls, a two-key dependency, a key/gate combination and two order-sensitive examples. [`SealedCatalog`](../app/src/main/java/com/androidhit/lumadrift/engine/SealedCatalog.java) fixes the definitions and certified solutions. `LD1-P-…` challenge codes identify these prototypes without changing the C/R/B/Q/G/H families.

These are independent, unlimited-action challenges in the existing friend-challenge mode, separate from the dated daily/weekly event rotation. They award no local coins, XP, campaign progression or equipment first-clear bonus and cannot consume powers or Charm protection. Completing them does not count as a weekly win or an event-star source for equipment.

The executed [sealed-cell engine report](validation/v1.5/sealed-tests.txt) records **10,425 checks** against an independent exact oracle over every piece subset and all 16 gate states, plus undo, history restore, restart, hostile definitions and content isolation. This establishes engine contracts. It does not establish that the final Android UI depicts the keys/walls clearly or that players prefer the mechanic; installed interaction and human comprehension remain separate acceptance checks.

## Powers and recovery

Spark, Vision, Flow, Shield, Focus and Trail retain separate unlocks and stock. `PowerUsePolicy` rejects unknown powers, an already-active effect, an in-flight activation/Flow sequence and taps inside its 450 ms debounce. Flow remains in flight until its sequence finishes or is explicitly interrupted. This policy must be connected to the actual board/effect state; a disabled icon alone does not establish safe stock accounting.

The integrated runtime must verify that successful activation debits exactly one item, rejected or interrupted pre-activation does not lose stock, and rapid taps cannot overlap effects or create duplicate board actions. The three rewarded placements remain exclusive: local coins, continuation actions or one selected unlocked power. No ad callback grants multiple placement rewards. Free restart remains available. [Earlier action/power contract and historical evidence](MOVE_BUDGET_POWERUPS.md).

## Audio

The former music loader expected `STATE_INITIALIZED` while a new static track correctly reported `STATE_NO_STATIC_DATA`; it returned before writing PCM and released the track. The repair loads the buffer before checking readiness. Music preparation also survives an unavailable effects cache.

At default gain **0.27**, the new loop measures **−28.45 dBFS RMS / −15.94 dBFS peak**; effects keep gain **0.72**. Initial play and resume fade in over **900 ms**, music disable fades over **180 ms**, and focus ducking changes the gain smoothly. Background/focus loss pauses immediately; release cancels pending fades and frees the players. Existing saved music/effects switches are retained. These are measured digital levels and verified model contracts, not calibrated physical loudness.

[Listen to the 16-second preview](validation/v1.5/audio/ambient-preview.wav) · [Measurements](validation/v1.5/audio/audio-levels.txt) · [Before/after loading regression](validation/v1.5/audio/static-load-regression.txt).

## Commercial and data boundaries

The candidate does not establish live-ad readiness, a completed Play license-test purchase, qualified Android Google login or production approval. The server wallet and public pages retain the separately documented [1.4 deployment observations and remaining gates](PRODUCTION_RELEASE.md). Local equipment is outside that paid-wallet entitlement catalog. No locally edited preference can authorize server funds.

The final build record must state its wallet origin and test-ad forcing, rather than infer settings from source defaults. Continue to qualify consent, ads and purchases using the [owner setup](PRODUCTION_ONBOARDING.md) and [Android wallet contract](WALLET_ANDROID.md). Human comprehension, D1/D7 return and revenue remain unmeasured; more content or equipment is not evidence that those outcomes improve.

## Acceptance record

| Check | Executed status | Evidence / remaining boundary |
|---|---|---|
| Automated regression | **1,103,064 assertions passed** across engine, exact solvers, budgets, data, economy, meta, events, billing/recovery and audio | [Counts and logs](VALIDATION.md); not players or online transactions |
| 1,000 + 240 content | All frozen layouts certified; no symmetry/translation duplicates within either route; old code identities preserved | [Structural audit](data/progression-v2-summary.md) |
| Sealed prototype engine | **10,425 exact/oracle and reversibility checks passed** | [Engine log](validation/v1.5/sealed-tests.txt) |
| APK/AAB and lint | Both release variants built, signed and structurally validated; final hashes recorded | [Artifacts](RELEASE.md), [build](validation/v1.5/android-build.txt), [binary audit](validation/v1.5/binary-final-candidate.json) |
| Installed functional pass | All six powers, interruptions, protection ordering, equipment purchase/equip/restore, level 1,000, six P boards and actual 1.4 active-save upgrade passed | [Installed scope and fixtures](VALIDATION.md#installed-android-acceptance); full run predates share-renderer-only correction |
| Installed UI | Six languages and smaller portrait/landscape bounds inspected; original balance/inventory preserved | [Full scoped run](validation/v1.5/pre-share-image-fix/ui-smoke.txt); native-speaker/physical accessibility review remains open |
| Final sharing/gameplay | P1/P6 exports show walls and keys; final P1 block/open/Undo passes; old C1 PNG identical | [Share evidence](validation/v1.5/sealed-share-ui.txt), [legacy comparison](validation/v1.5/legacy-share-regression.json) |
| Final EN/FR UI repeat | **14 cases / 24 captures passed** on exact final production APK; earned-coin confirmations and inventory unchanged | [Final report](validation/v1.5/ui-smoke.txt) |
| Final Android music | Actual track advances, pauses on background and resumes | [Runtime](validation/v1.5/audio-runtime.json); digital playback, no physical audition |
| Physical sound/device quality | **Pending** | Modest/current phones, speakers/headphones, physical latency, touch comfort and TalkBack |
| Commercial/provider qualification | **Pending** | Real Android identity/UMP, Play product/license-test lifecycle and live-ad readiness; candidate forces sample ads and blocks checkout |
| Human comprehension/retention | **Unmeasured** | First-time observations and complete cohort windows in [the roadmap](ROADMAP.md) |

The [source comparison](validation/v1.5/binary-source-diff.json) identifies the final share-image-only correction; it does not rename predecessor runtime reports. Every installed report states its actual APK identity. Historical reports remain preserved.
