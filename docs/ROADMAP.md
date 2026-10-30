# Luma Drift roadmap

## Current 1.21 — delivered for playtesting

The signed B / code 29 packages are frozen in the [artifact manifest](../releases/manifest-1.21.0-rc1.json). The full-body avatar, real player XP, companion-copy evolution, illustrated offers and direct house/living/kitchen/garden routes are implemented. Appearance, puzzle-team skills, player XP, copies, earned Coins and account Gold have distinct explanations. The kitchen is a separate free ambient room; its purchasable furnishing stays a separate 440-Coin object.

The journey crosses ten illustrated zones. Eligible new U11+ and expedition attempts use free elemental-shell taps and a once-per-attempt Focus earned by three distinct manual exits without correction. Scene fades and real balance/XP feedback respect reduced motion. The versioned economy adds gradual first-clear rewards, gentler boost-price growth, two free Undos before explicit Coin fees, and a 10→16 session-energy capacity that grants no extra energy when it grows. Real checkout remains closed.

[Host evidence](validation/v1.21/host/README.md) records executed model/store/geometry scopes separately from native runs, preserves the original transition-test failure and its corrected expectation, and verifies B's unchanged model dependencies. [Eight exact B site frames](validation/v1.21/native/site-media-candidate-b-accepted/capture-map.json) and the [independent simulation's local browser report](validation/site-v1.21/browser-local/simulation/report.json) are ready. [Production B acceptance](validation/v1.21/native/candidate-b-final-index.json) passes 13 phases / 574 assertions / 149 touches; [Lab B acceptance](validation/v1.21/native-lab/candidate-b-final-index.json) passes three phases / 99 assertions / 41 touches. Their 38 and 24 derived comparisons are separate. The real Lab test-Gold debit and cold-energy recovery preserve all six production preference maps. Application source, artifact metadata and original evidence are published at [C1 `35bb8222`](https://github.com/nabz0r/AndroidHit/commit/35bb82229d8175968919b5979264e5094a1cc8dc). Website source and local QA are published at [C2 `71bba893`](https://github.com/nabz0r/AndroidHit/commit/71bba893a5fbfeb808e5c44840b6701862255453); hosted CI passes both jobs / all 36 steps on exact C2. [Hosting](validation/site-v1.21/hosting/deployment.json) confirms exact C2 as DEPLOY/FINALIZED, version `d08a2aa131c05c4f`, released `2026-10-30T00:08:28.331Z`, with all 525 physical payload hashes verified in the clean deployment checkout. The exact original remote receipt bytes are retained in the [delivery register](validation/v1.21/publication/release-delivery.json); public response bytes/rendering remain unverified under the saved permission refusal. [Release register](RELEASE_1_21.md).

[Download production](https://luma-drift-production.web.app/releases/luma-drift-1.21.0-rc1.apk) · [Separate Lab](https://luma-drift-production.web.app/releases/luma-drift-1.21.0-sandbox-rc1.apk) · [Playable simulation](https://luma-drift-production.web.app/lab/ui-1-21/index.html) · [Website](https://luma-drift-production.web.app/). Use actual owner/physical-phone play to assess comprehension, touch comfort, perceived motion, audio and voluntary return. Model probabilities and controlled simulations do not establish an enjoyable or habit-forming game. Keep identity, consent, advertising and real purchase/refund acceptance separately qualified.

## Preserved 1.20 — delivered for playtesting

The menu source now implements a light home with one Play action, the equipped companion and owned team, actual ready-gift counts and a five-place navigation. Persistent Coins/Gold/session-energy stays outside the scrolling menu viewport. Daily/weekly gifts use the existing durable claims, remain visible until an explicit close and become non-interactive after collection. The shop is an illustrated single-product carousel with actual price/stock/ownership and preview routes. [Menu contract](UX_1_20_MENUS.md) · [73,838 latest menu assertions over 504 configurations](validation/v1.20/menu/shop-e/README.md).

Production F has passed [11 exact-package native scopes](validation/v1.20/native/README.md): 457 assertions/180 touchscreen actions/76 original PNGs/one 60-second recording, with 38 read-only derived comparisons separate. Actual three/two/one-star first clears, gifts, result/zero-energy/expedition cold restoration, compact FR/EN, six-language menus and wallet swipe isolation are recorded. Six [exact-F site frames](validation/site-v1.20/native-capture-map.json) and [local canonical packages](../releases/manifest-1.20.0-rc1.json) are ready. Application/artifact source is published at [C2 `7ea28bc6`](https://github.com/nabz0r/AndroidHit/commit/7ea28bc6125e73d0543b264081266aeff510b071); website/local-QA source is [C3 `eaae0fba`](https://github.com/nabz0r/AndroidHit/commit/eaae0fbad15b074409f608e5a2afe7913c3bee3b). Source contracts and scoped local FR/EN browser review pass. [Hosting confirms](validation/site-v1.20/hosting/deployment.json) C3 as DEPLOY/FINALIZED, version `94f0979d7a02c3b2`, released `2026-10-29T22:29:43.998Z`. [Delivery record](validation/v1.20/publication/release-delivery.json). The local record is a derived transcription of management UI observations, with original remote receipts inventoried separately. Public-origin bytes/render remain unobserved under the saved permission boundary; management and localhost evidence retain separate scopes. The separate [Lab refill/cold scope](validation/v1.20/native-lab/README.md) passes 48 native assertions/13 touches/6 PNGs plus 12 separate derived checks: 500 → 450 test Gold, energy 0 → 10 → 9/cold 9 and unchanged production preferences. Two observer-stopped prerequisite/retry attempts remain excluded from passing totals.

E's six-family skill evidence remains E, with unchanged core-model hashes documented in the F index; C/D and failed observer attempts stay preserved. Public-origin browser access was refused: use local browser and authenticated Hosting management scopes without a workaround. [Release scope](RELEASE_1_20.md) · [Website workflow](validation/site-v1.20/README.md).

New relay policy 6 makes Undo free without refunding an earlier exit and grades actual correction use rather than elapsed time. Session energy has 10 units with 1 per 10 minutes regeneration; companion Charge remains a separate 3-to-6 attempt-local counter. A prepared 10-unit/50-LumaGold refill has measured client/server receipts and production gates closed, using separate Lab test Gold. [Gameplay/UX](UX_1_20.md) · [Premium protocol](ENERGY_PURCHASE_1_20.md) · [Scoped evidence index](validation/v1.20/README.md).

Next product work is owner/physical-phone playtesting, then separately qualified identity, consent, ad and Play purchase/refund workflows. Physical-phone comfort, audio, first-player comprehension and voluntary return remain unmeasured. A premium-energy adapter or wallet balance is not a provider purchase/refund acceptance result. Keep real checkout/ads qualification separate from this UI release.

## Preserved 1.19 — companions that act in the puzzle

[Release state](RELEASE_1_19.md) · [Gameplay](GAMEPLAY_1_19.md) · [UX](UX_1_19.md). All three owner-approved axes are implemented in Android source: owned teams of one to three; six role families across 37 identity definitions; and a portrait-led dock with reactions and bounded skill feedback. Team composition affects available commands, while outfits retain their separate visual role. Two different used role families enable one explicit Resonance action; three members are optional.

The shared attempt starts with 3 energy, caps at 6, and credits each distinct exited piece once. Each member has one use; automatic exits pay the normal board budget and REWIND uses real refunded history. Current-attempt teams are fixed, with saved edits applied on the next board. U geometry, progression identities, inventory and commercial gates are retained.

Host storage, model, action-draft and geometry checks have separate versioned evidence. C (`prototype2` / code 27) passes ten exact-package native scopes: 419 assertions, 209 real gestures, 58 original PNGs and a 45-second recording. These cover six skill families, owned teams, combo, anti-farm, compact FR/EN, six-language squad views, Expedition context and cold persistence on one isolated emulator. B’s compact navigation failure remains preserved. [Native report and limits](validation/v1.19/native/README.md).

Both signed C packages are frozen at artifact/source commit `e267ac192f3aea268efe4e1a3a92a41d5b5f1518`. C3 website source `ccc3e025a088034d3b1f65d294e1e9cf68eb6e20` is deployed as Firebase version `a64a7ef1faadf791` on 2026-10-29T20:29:13.810Z. Independent complete-byte checking passes 500 responses/2,092,742,901 bytes, including both APKs. Later public browser observation was blocked by a saved permission, with no workaround; local responsive review has its own receipt. [Published 1.19 scope and browser boundary](validation/site-v1.19/README.md).

Remaining product/provider work is carried into 1.20: physical-phone and owner acceptance, rotation/filesystem-fault/TalkBack coverage where absent, and distinct real identity/ad/purchase/refund qualification. Host/emulator counts do not establish enjoyment or voluntary return. Existing local/public-byte evidence must not be presented as a public-render pass after the saved permission rejection.

## Preserved 1.18 — evolving companions and expedition prototype

The H/code25 prototype and its existing Firebase website are recorded in [1.18 release evidence](RELEASE_1_18.md). Four forms and twenty poses cover 37 identities; the three expedition pilot skills remain distinct from the new 1.19 puzzle role system. The owner’s 29 October UI rejection remains historical evidence rather than being overwritten by automated passes.

## Preserved 1.17 — an illustrated world to touch

[Release](RELEASE_1_17.md) · [Object-navigation and original-art research](design/v1.17/reference-research.md) · [Design system](DESIGN_SYSTEM.md) · [Exact qualification](validation/v1.17/README.md). H is production1.17.0-rc6/code24. Four separate scenes, original companion/clothing/furniture sprites, visible object destinations, optional swipes, bounded transitions and five synthesized sound contexts replace the previous presentation. The three10×10 rooms, local nickname/flag/badges, earned-coin arrivals, save identities and U puzzle rules remain.

Build/signature/static alignment and the complete24-command host workflow pass. Fresh install, actual five-context digital audio, strict normal/compact mode actions, exact all 1,000 retained replay and the complete scoped UI/art matrix pass. Canonical packages equal the audited H bytes. Git/Firebase delivery and public download hashes pass; public browser acceptance covers eight responsive FR/EN states, keyboard access and four deliberate audio previews. Earlier candidates retain their original rejected/partial results.

Next product work after delivery should be driven by physical-phone play: discoverability of objects/swipes, reading actual clothing/room detail, comfort of short-window scrolling, music/effect balance through speakers/headphones, and first-player understanding of the relay rule. User comprehension, long-term return and a future million-player audience are not established by model/SDK checks. Provider/Play activation remains its own controlled work.

## Preserved 1.14 — make the puzzle understandable and consequential

[Release](RELEASE_1_14.md) · [Gameplay](GAMEPLAY_1_14.md) · [UX](UX_1_14.md) · [Qualification](validation/v1.14/README.md). Replace implicit gate controls with visible one-action openings, teach the core mechanics through thirty authored opening boards, and introduce the versioned N progression across 1,000 Journey boards. Keep a real action budget, free refusals and one persisted Return per attempt. Existing challenge identities and active geometry remain reconstructible.

RC1 source checks covered N certificates/minima, opening-state contracts and the strict ledger. A further review found an excessive Hard/Expert streak and a long easy valley, so RC2 reauthors the later route with global episodic pacing and reserved recoveries. The corrected source passes 201,143 content checks and an independent 4,970-assertion curriculum review: all 97 later episodes have three free starts and at least three Normals, the maximum Hard/Expert run is four, and 200-board score medians rise. There are 291 opening-only initial gate changes in N while old L identities stay fixed. RC2 signed-byte checks and native coverage of all 1,000 boards pass, with the declared access prerequisite for the final200 in a separate session. Source and all four signed files are published at578661da; the existing website is deployed from4466cb4a, with exact public payload and FR/EN browser receipts. Historical 1.13 delivery is retained below.

Next product decisions:

1. Observe first-time players explaining a closed side, large/small arrow, K association and costly switch order while playing the exact candidate.
2. Review costly legal branches and progression transitions against observed mistakes; structural labels alone do not establish human difficulty.
3. Check physical Android phones, large text, TalkBack, interruptions and native-speaker copy.
4. Measure voluntary replay and completed return cohorts before adding more rooms, furniture or progression systems.
5. Resume the separately controlled Play/provider qualification with real checkout and premium draws closed until their own evidence passes.

## Preserved 1.13 — a home around the main puzzle

[Release](RELEASE_1_13.md) · [Home contract](HOME_1_13.md) · [Executed qualification](validation/v1.13/README.md). Connect existing earned puzzle rewards to a room the player can arrange: twelve furnishings, a companion who wears the actual equipped look, hugs, path-based movement, interactive furniture, wall/floor choices and a pinned wish. Introduce the core gesture immediately with a skippable three-piece board; keep the existing optional seven-lesson route.

The implementation preserves puzzle geometry, budgets, progression, inventory and commercial gates. The six-language home model has 891 source/durability checks; the actual opening has 18 source checks. The final RC2 retained-Lab route passes 798 native checks; targeted/fresh/production scopes retain separate reports. [Main publication](validation/v1.13/publication.json) verifies four public binaries from source `67774c14`; [Hosting delivery](validation/site-v1.13/deployment.json) and [public-origin checks](validation/site-v1.13/public-origin.json) verify the 108-file website from that same source. These are separate acceptance layers. No human retention result is inferred from these checks.

Next decisions:

1. Observe new players through the first switch, their first room interaction and the return to the main puzzle. Measure what they understand and what they choose to repeat.
2. Test the exact candidate on physical Android phones, large text and TalkBack, then obtain native-speaker review of the new home copy.
3. Review the preserved L22→L23 structural jump and Guardian 4→5 mechanic introduction with human error/comprehension observations. The house release does not recalibrate difficulty.
4. Validate small real player cohorts and voluntary return before extending the furniture catalogue or adding rooms. D1/D7 retention and international appeal remain hypotheses.
5. Resume owner-controlled Play internal testing and qualify Google/two-account/social, consent, real ad eligibility and the licensed purchase/pending/refund/recovery lifecycle independently. Keep live checkout and premium draws closed until those gates are satisfied.

## Preserved 1.12 — readable shop and choices

[Release](RELEASE_1_12.md) · [Qualification](validation/v1.12/README.md) · [Grounded UX decisions](research/ux-v112.md). Replace repeated item dialogs with normal pages; expose both currencies and honest wallet state; make power silhouettes/effects recognizable; lighten stacked surfaces. Correct the reproduced earned-power refused-write transaction and preserve live-run/rotation navigation. Scoped final native checks pass: Lab 1,179/40 clears, production 7 startup +35 shop, account-wallet 45/6 languages.

Website 1.12 is deployed from source `91ffac7a` at 28 October 2026,14:08:23.437 UTC, after all 98 source payloads matched. It reflects the atelier, original powers, equipped companion and both currencies. [Responsive local checks and confirmed Hosting receipt](validation/site-v1.12/README.md) remain separate from app qualification.

Next: validate the exact APK on physical phones, observe first-time comprehension of the two currencies and purchase/Equip distinction, then resume internal Play/provider qualification. The L22→L23 and Guardian 4→5 difficulty jumps remain separate work. Cosmetic improvements are not measured retention.

## Preserved candidate 1.11 — pre-Play bug hunt

[Release](RELEASE_1_11.md) · [Exact artifacts](validation/v1.11/candidate.json) · [Qualification](validation/v1.11/README.md). Fix actual equipment visibility and clear success feedback before adding more content: show the applied portrait, name each worn object, retain character silhouette under clothes, separate small equipment and apply the selected scene theme. Correct refused-save read consistency and the first-profile grant transaction. Keep all existing catalog IDs, prices, effects, puzzle layouts and saved progression.

The new native visual audit purchases with genuinely earned coins and compares actual art through a cold process restart. Host tests cover 72 ownership/equipment identities and exact finite-state strategy/guardian rules; these are not human difficulty, engagement or retention measurements. The known L22→L23 and Guardian4→5 progression jumps remain a separate design task.

Scoped native qualification is complete: 1,589 Lab checks over 50 clears, 935 production checks over 40 clears, and 164 fresh-install tutorial/avatar/language checks. [Final native report](validation/v1.11/ui-bugs/SDK_UI_FINAL.md).

Next: review the exact new APK on a physical phone, then resume internal Play testing. Qualify Google and two real accounts, UMP consent/decline, licensed Play purchase/pending/refund/recovery and real ad eligibility separately. These signed bytes force test ads, keep wallet checkout unconfigured and paid draws disabled. The website's current confirmed deployment remains 1.10.


## Preserved candidate 1.10 — Companion & Objectives

[Release](RELEASE_1_10.md) · [UX](UX_1_10.md) · [Native qualification](validation/v1.10/SDK_UI_FINAL.md) · [Screen/provenance matrix](validation/v1.10/ux-readability/README.md) · [Prism journal](PRISM_STORY_1_10.md). Production **1.10.0-rc3** and isolated **1.10.0-sandbox-rc3**, code 11, have identified signed artifacts and final-byte Android evidence. The Play Console walkthrough remains paused at the owner's request.

Home connects actual unclaimed rewards, the next activity, a chosen collection goal and the companion's appearance. A shared portrait combines the existing 24 companions and 12 free styles with four cosmetic slots and three bounded-benefit equipment slots. Full-character previews distinguish looking, owning, buying and equipping. The read-only ten-chapter journal follows existing Journey clears and claimed optional guardian receipts; it creates no currency or new fragments.

The rc3 main Android run passes **2,514 assertions / 96 original PNGs**, including Journey 1–100, menus, source-backed goal/preview behaviour, fourteen real selected-colour samples, four fully reachable footer targets and six-language navigation. A genuine first installation separately passes 91 checks; production startup/audio passes 7. Source models separately cover 6,544 portrait/copy assertions, 443 objective/store assertions and 308,412 journal assertions. Earlier functional passes did not catch transparent palette-index colours or clipped footers; those failures remain documented rather than being relabelled as current acceptance.

**Website 1.10 is deployed:**89 payloads from source `9be35d2b` were verified before Firebase completion and live-channel finalization at 28 October 2026, 10:44:28.535 UTC. [Delivery proof](validation/site-v1.10/deployment.json). The eighteen native PNG copies and current APK route retain their exact provenance. Local browser review covers 12 FR/EN DOM states and six desktop rasters; incomplete mobile rasters are excluded. [Website qualification scope](validation/site-v1.10/README.md).

Next: obtain complete mobile browser frames for visual acceptance, then test modest physical phones, large fonts, TalkBack and native-speaker copy. Actual 1.10 filesystem-fault acceptance and commercial/provider qualification remain incomplete; source persistence tests do not replace them. L/M/T identities, budgets and G1 guardians remain unchanged, including the L23 structural jump. Human difficulty, enjoyment, retention and revenue remain unmeasured.

## Published 1.9 — first journey, guardians and expressive companions

[Release](RELEASE_1_9.md) · [2026 player audit](PLAYER_AUDIT_2026.md) · [UX](UX_1_9.md) · [Guardians](GUARDIANS.md). The release fixes reproduced native lesson-4 and compact preview failures, makes earned gifts and free power samples visible, grades actions rather than hint use, and adds 100 optional encounters plus 288 visual combinations. Synthetic policy experiments and original 2026 survey aggregates retain their separate scopes; human enjoyment and return rates remain unmeasured.

The owner reported Play account approval on **27 October 2026**. The Console walkthrough is now paused for further app development. Actual Google identity, two-account gifts, consent and license-test purchases remain later qualification work. Approval of the developer account does not itself publish an app or activate commerce.

## Preserved 1.8 milestone — all levels, learning and a readable world

[Delivered scope, artifacts and evidence](RELEASE_1_8.md) · [V4 content](PROGRESSION_V4.md) · [Tutorial](TUTORIAL.md) · [UX](UX_1_8.md). The user prioritized every level and presentation over Google/social/commercial activation. The former manual-gate proposal is implemented in separate L/M/T families, with exact productive-action budgets and free refunded correction. Historical J/K and older codes remain unchanged.

Next decisions, in order:

1. Observe first-time players on the7-lesson tutorial and levels1–30. Check recognition of the key/seal, a switch preview, a paid action versus free preview, and free Undo at 0. No payment or ad is needed for a certified completion.
2. Calibrate the measured structural progression with human errors/comprehension rather than claiming normal/hard/expert labels predict CandyCrush-like retention. Review levels31–100 andlater biome peaks on modest physical phones, large fonts and TalkBack.
3. Evaluate first-session clarity, voluntary replay and completed D1/D7 cohorts with explicit observation/opt-in tooling. No false near miss, fake limited stock or automatic price escalation is part of the fixed route.
4. Resume the [two-real-account Google/friends/gift qualification](QUALIFICATION_SOCIALE.md). Prepared virtual Androids and source simulations are not actual Google-account acceptance.
5. Qualify UMP consent/decline, live AdMob eligibility andPlay license-test purchase/pending/refund/recovery before producing a separately reviewed live build. Paid cosmetics remain disabled on client and server.

## Preserved milestone — 1.7 route recovery, wardrobe and service rollout

Published signed production **1.7.0-rc1 / code 8**, with isolated **1.7.0-sandbox**; all four public downloads have [independent size/hash verification](validation/v1.7/publication.json). [Scope and qualification](RELEASE_1_7.md) · [UX](UX_1_7.md) · [Economy](ECONOMY_1_7.md) · [Executed record](validation/v1.7/README.md). Final Lab/production native runs pass with restored preferences, and [Cloud Run deployment/basic probes](validation/v1.7/cloud-run.json) are recorded. [Website 1.7 publication](validation/site-v1.7/README.md) is confirmed from exact main `4c5f5ca8`, with all 47 source payloads hash-verified before Firebase completion. Real authenticated two-account and commercial qualification remain separate.

**Open level-management issue:** the [J1–30 diagnosis and next-release proposal](LEVEL_MANAGEMENT_REVIEW.md) reproduces legal dead ends on J7/J8 and distinguishes initial solvability from fair exploration. The current N-plus-margin budget chiefly charges errors/Undo. A simulated manual-gate action and a versioned authored prototype are proposals only; no rule, board or budget change is applied to 1.7 during real-account qualification.

Completed source contracts:

- Exhaustive reachability of all 1,000 Journey and 240 Remix boards confirms initial completion inside the unchanged allowances. J13 demonstrates why a legal choice can close the eventual solution while another exit remains. Exact guidance distinguishes safe play, insufficient actions and a dead route, with Undo/free restart and safe current-state hints. [Independent audit and reproduction](validation/v1.7/board-audit.md).
- A scrollable 1–1,000 map, level jump/current-node return, and ten 100-level atmosphere ranges. Biome art tint and original audio change with the viewed route; physical scrolling/readability/playback remain separate acceptance.
- A seven-claimed-visit gift cycle without absence reset, daily/weekly first-clear quests and atomic receipts. Existing local balances, progression and equipment remain.
- A four-slot 24-item visual wardrobe, including permanent Halloween/Valentine designs, explicit owned/equipped/unowned states, source progress and earned direct purchases. The existing 18 Head/Aura/Charm equipment items remain a separate bounded-benefit system.
- LumaCoins/LumaGold presentation separates local earned funds and the premium server wallet. The local 150-LumaCoins capsule shows category/per-item probabilities and rarity-based duplicate compensation; no LumaGold is spent.
- Authenticated friend/gift service and client source, with mutual friendship, opt-in map snapshots and limited one-hint gifts. The deployed revision passes health and unauthenticated-gate probes; real-account feature acceptance remains to qualify. The native premium account catalogue is prepared, while both APK and server draw gates remain false. Account ownership/conditional probabilities are distinct from the untrusted earned local wardrobe.

Next acceptance, in order:

1. Signed final APK/AAB packaging is complete. The final Lab menu run passes 38 native assertions with 32 captures and original preferences restored. Final Lab gameplay also passes 34 checks/9 captures; separate production startup passes 7 checks/1 capture and observes digital music advance/pause/resume. Physical quality, full six-language layout, large-text and human comprehension remain separate acceptance. Preserve the prior 1.6 bytes and evidence.
2. Website source/imagery and 18 FR/EN CUA views at actual 320/390/1366 CSS pixels pass. Firebase Hosting completion now confirms publication from exact main `4c5f5ca8bc5f74173e283377275bb352c76534c7` after verifying all 47 payload hashes. [Delivery](validation/site-v1.7/README.md) records CLI/management scope; no Firebase public-origin byte probe was performed.
3. Server revision `luma-wallet-00005-pps` is deployed at 100% traffic, with paid draws false and recorded health/unauthenticated-gate probes. Qualify two real accounts, mutual requests/removal, opt-in visibility, sender/receiver limits, account switching, deletion and full-stock/retry gift handling. Never seed fictional map friends.
4. Observe first-time players reaching J13, the first key lesson and later peaks. Measure whether the dead-end explanation, restart/Undo choice, slot/ownership state and two currencies are understood. Test with physical phones and assistive technologies.
5. Keep paid draws, live ads and real checkout closed until their separate provider, policy, purchase/refund/recovery and release checks pass. Resolve the premium pool versus local-earned ownership decision: an account-new guarantee currently permits a design already earned locally. An earned-currency capsule simulation does not qualify a real paid draw.
6. Wait for complete player cohorts before making retention, monetization or hit claims. Add features only where observation supports the next decision.

The fixed-seed local capsule simulation is an implementation check, not a conversion or engagement result. All 1,240 solvable starting boards do not imply that every later gate state remains solvable; recovery clarity is the product change.

## Preserved milestone — 1.6 saga, ownership and session clarity

The 1.6 signed candidate delivers production `1.6.0-rc1` and isolated Lab `1.6.0-sandbox`, both code 7. It adds a ten-board saga map, explicit prelevel objective/element/difficulty, scrollable layouts, a unified inventory presentation with owned/equipped/locked states, and guarded Google-session refresh states. New frozen J/K content has 1,000 Journey and 240 Remix boards, with keys taught at Journey 12/14/17 and revisited sparsely. [Candidate scope](RELEASE_1_6.md) · [UX contract](UX_1_6.md) · [Content and compatibility](PROGRESSION_V3.md).

Completed host-side progression checks cover all 1,240 certificates, an independent exact model, genuine key/seal dependencies, geometric uniqueness and active-history/budget restoration. The V3 suite records **582,831 checks**; regenerated rows are byte-identical and pre-change/post-change historical family fingerprints match. These checks qualify their source contracts, not an installed APK or a human difficulty curve. Normal/Hard/Expert remain authored comparative labels. [Measured audit](data/progression-v3-summary.md) · [Executed host reports](validation/v1.6/).

Recorded candidate acceptance:

- Both signed release APKs/AABs, build/lint, exact source hashes, signature/bundle/16KB alignment checks: [1.6 qualification](VALIDATION_1_6.md).
- Actual Lab 1.5→1.6 replacement preserves the original active board, budget and economy on API 26; prelevel cancellation preserves it too.
- Final Lab: 88 menu/layout checks with six languages, 320dp, landscape and font preference 1.3. Final production: 43 menu checks plus real advancing AudioFlinger playback. Both run on an actual 16KB-page API 36 emulator. Exact hashes and restored profiles/settings are recorded.

The matching FR/EN website presents real final 1.6 captures, progressive keys, clear collection states and the signed APK download. [Website implementation and delivery](validation/site-v1.6/README.md) separately record its 18 local responsive states, source provenance and Hosting outcome.

Next gates are human first-time comprehension and difficulty calibration, physical-phone/audio/TalkBack/large-text review, and separate authorized provider and commercial qualification. Captions abbreviate where necessary at 130% text; some compact targets remain below 48dp. A fixed solver-certified curve is not measured D1/D7 retention. Real Google success, Play license-test purchase/refund/recovery and live-ad eligibility remain distinct from model tests and provider cancellation. [Evidence and limits](VALIDATION_1_6.md).

## Preserved milestone — 1.5 avatar, progression and playback candidate

The current source work adds an avatar-centered lobby, three equipment slots with 18 fixed items earned through local play or local coins, a 1,000-board Journey plus 240-board Remix, a bounded power-activation policy and repaired original ambient music. The signed candidate has passed recorded package, active-save migration, emulator UI/gameplay and Android playback checks. Physical devices and player comprehension remain the next acceptance stage. [Delivered scope and acceptance](RELEASE_1_5.md) · [Equipment contract](AVATAR_EQUIPMENT.md) · [Audio evidence](validation/v1.5/audio/README.md).

Equipment gives explicit, limited benefits rather than changing the puzzle's gate or exit rules. The new progression uses fixed offline-generated boards and board-specific mistake allowances; neither board generation nor allowances adapt to payment history. Six separate Sealed passages prototypes explore keys that dissolve fixed walls; they use unlimited friend challenges without progression rewards or powers. Structural difficulty analysis and the prototype engine's exact checks are separate from player comprehension and enjoyment. The existing companion identities and themes stay cosmetic.

Completed technical checks: signed production/Lab APK/AAB and lint; fixed board bounds and legacy identities; actual old active-save upgrade; all six power activations and interrupted Flow recovery; earned-coin equipment purchase/equip/restore; all six sealed challenges; six-language emulator navigation; final share images and advancing Android audio. [Executed evidence and exact-byte scope](VALIDATION.md).

The current website/GitHub presentation work uses the [shared design system](DESIGN_SYSTEM.md), actual game captures and truthful release links. [Web revision checks](PUBLIC_PAGES.md) and [independent Hosting delivery](validation/site-v1.5/delivery.json) now record this completed visual update. This is separate from provider readiness, player acceptance and a new Android release.

Product checks recorded for the 1.5 milestone:

1. Play the identified candidate on modest and current physical phones; check readable labels, comfortable targets, TalkBack, interruptions and speakers/headphones. Landscape needs physical review because the Canvas column is reduced.
2. Observe first-time players using the six sealed prototypes. Record whether they recognize matching keys, understand that Undo reseals walls, and anticipate key/gate ordering. The preserved 1.5 G/H Journey stays wall-free; the separately versioned 1.6 J/K source now includes sparse keys, with its human comprehension study still pending.
3. Compare confusion and voluntary replay with ordinary gate puzzles before expanding the mechanic. Keep the human comprehension and complete cohort criteria below.

This work does not activate paid equipment, cloud progression, live ads or production sales. Local equipment ownership stays separate from purchased-gold server entitlements. The commercial and operating gates established during 1.4 preparation remain open below. Retention and revenue are still unmeasured.

## 1.4 production preparation — deployment baseline

The 1.4 preparation candidate implemented the production puzzle, owner Google configuration, three ad placements, real Play checkout adapter, server-authoritative purchased gold/permanent cosmetics, server deletion and FR/EN public pages. Cloud Run and the stable qualification site are now deployed; observed public routing/CORS and owner-reported account flows have distinct evidence limits. Sample creatives and checkout gates remain in place. [Delivered scope and deployment follow-up](PRODUCTION_RELEASE.md) · [Executed evidence](VALIDATION.md).

Next activation gates, in order:

1. Finish Play/merchant/product configuration, upload the production AAB internally and register Play's signing fingerprints in Firebase.
2. Qualify the deployed wallet with scoped managed identities and Firestore rules/indexes; deploy the pending RTDN/scheduled recovery and verify real purchase, pending, refund and deletion behavior. Automatic audit purging is not active.
3. Complete legal publisher information and independently qualify the published site's authenticated account/deletion flow. Public contact, the Web configuration, stable-site delivery and CORS are already recorded; owner-reported deletion does not substitute for independent acceptance.
4. Qualify owner UMP messages and app-ads.txt/AdMob readiness using test devices, then prepare a new explicit live-ad build.
5. Complete physical-device checks, the applicable Play closed test and production review before a staged public launch.

Paid-wallet power-ups/continuations and authoritative progression rewards are future integrations. The first paid wallet supports permanent companions/themes; earned offline gameplay remains separate. Retention and revenue still need real-player evidence.

The sections below retain earlier milestones and their historical evidence; they do not override these current activation gates.


**Build the memorable rule. Observe players. Scale only when the evidence improves.**

Updated 27 October 2026. This roadmap records priorities and exit criteria. Future stages are proposals, not delivered features or committed launch dates. See [the 1.7 candidate](RELEASE_1_7.md) for current source work, [preserved 1.6](RELEASE_1_6.md) for its separate delivery, [preserved 1.5](RELEASE_1_5.md) for its identified artifacts, [CHANGELOG.md](CHANGELOG.md) for versioned scope and [VALIDATION.md](VALIDATION.md) for the preserved installed checks.

## 1.0 — A playable foundation

Delivered: 240 deterministic campaign boards in eight chapters; free undo/restart; three powers; six original companions and six themes; earned-coin shop; local daily and weekly events; six languages; original vector art and synthesized music/effects; local save/resume. The original release has build, logic, signature and Android 16 emulator evidence. Physical-device acceptance and player retention remain unmeasured.

## 1.1 — Make the rule easier to learn and worth discussing

Delivered in 1.1; executed runtime coverage belongs in [VALIDATION.md](VALIDATION.md).

| Workstream | Release scope | Acceptance evidence |
|---|---|---|
| Learn by playing | Contextual movement, blocking and gate coaching; guided first moves and optional full help | Fresh-install interaction, gate introduction and accessibility labels |
| Distinctive decisions | Optional 240-board Remix route after campaign board 12, with dual-gate switches; original campaign retained | Exact-solver coverage, determinism, undo and save restoration |
| Fair return loop | Three daily missions, XP/rank and seven-board cosmetic badge; no lost-day punishment or forced streak | Reward idempotency, period rollover, rank and persistence tests |
| Player-to-player handoff | Reproducible C/R/E codes, original board cards and Android sharing; no friend-mode powers or progression gains | Code round trip, invalid-code rejection, handoff and reward rules |
| Learn without a backend | Local journal off by default, explicit JSON export and clear controls | Default state, bounded storage, exported fields and clear behavior |
| Presentation and delivery | Updated interface, screenshots, store assets, documentation, signed APK/AAB | Build/lint, installed final-binary interaction, signature and checksums |

The release validation document is the authority for completion of these checks. A line in this roadmap is not a passing test result.

## 1.2.0-sandbox — Prepare monetization without real sales

**Delivered test application.** The Lab APK is packaged and installed; covered Android 16 SDK and UI cases passed. The stable 1.1 APK and its evidence remain available separately. The owner requested test mode, so this milestone does not activate production advertising or payments. [Executed checks and remaining limits](VALIDATION.md).

| Workstream | Delivered Lab scope | Acceptance contract |
|---|---|---|
| Isolation | `com.androidhit.lumadrift.sandbox`, own save and wallet, visible Lab label | Install beside stable; verify no balance/progress contamination |
| Rewarded test ad | Adult declaration + explicit opt-in + UMP eligibility + manual load; Google sample IDs | Observe refusal/unavailable path and any actual test creative separately; earn 35 once only on the reward callback |
| Gold simulation | Native no-charge 500/1,400/3,600 pack dialog; cancel/pending/complete; idempotent grant | Pending survives restart, no premature credit, duplicate completion adds zero |
| Billing boundary | Billing 9.1 connection/product queries; actual checkout disabled | SDK status and disabled checkout evidence; no claim of a license-test purchase |
| Item economy | Progression bands based on distinct campaign + Remix clears | Formula/boundary tests and later free-player affordability observations |
| Local observations | Optional journal ad callbacks; no Firebase Analytics or remote gameplay analytics | Default-off behavior, export inspection, test-labelled values excluded from income |
| Expanded collection | 24 total companions, original six preserved, three pages of eight, native preview/buy/equip | Catalog/purchase contracts, all-page visual review and installed interaction |
| Clear local events | Goals, stage progress/rewards, next stage, reset countdown and visible bonus | Device-calendar boundary tests and daily/weekly UI/claim checks |
| Optional identity | Credential Manager + Firebase Auth; no owner config by default, guest-first UI | Not-configured behavior now; actual provider login/sign-out/deletion only after owner setup |

The [Lab setup guide](MONETIZATION_SETUP.md) distinguishes a local simulation, a Google test ad, a Billing query and a future Play license-test purchase. Passing one does not establish the others. [Research](MONETIZATION_RESEARCH.md) records the official sources used before implementation.

[Companion/event logic checks](validation/lab/collection-events-tests.txt) record 87,121 checks, including 604 time zones and 14,496 reset-boundary scenarios. This is evidence for deterministic contracts, not a human visual review. [Collection guide](COMPANIONS_EVENTS.md) · [Optional identity setup](GOOGLE_SIGN_IN.md).

The [installed collection/event run](validation/lab/collection-events-ui-smoke.txt) separately browsed the 24 cards, exercised native preview/purchase/equip, completed ten event boards and checked one-time bonuses across the six-language loop. A Google rewarded **test** creative and +35 reward, UMP refusal and local purchase simulations were also exercised. Identity evidence remains limited to [guest/not-configured English/French handling](validation/lab/identity-ui.txt). Production account setup, actual Google login, Play license-test purchases and an authoritative wallet remain future work.

## 1.3.0-sandbox — Make decisions and recovery explicit

**Delivered test update; final signed package and recorded Android 16 gameplay/SDK cases passed.** This updates the existing sandbox application, preserves stable 1.1 and retains 1.2 evidence as historical. It does not activate production sales or advertising.

| Workstream | Delivered scope | Evidence / next acceptance |
|---|---|---|
| Board progression | Frozen order of the existing 240 Journey + 240 Remix layouts within their original chapters; Journey 1–6 and Remix 1–3 fixed | [19,054 structural/solver checks](validation/v1.3/difficulty-tests.txt); real-player difficulty remains unknown |
| Action allowance | Piece count plus an explicit margin; present-piece blocked/legal taps and successful Undo cost one | Certified solutions fit; [ledger suite](validation/v1.3/move-budget-tests.txt) and covered Android exhaustion/restart cases pass |
| Optional recovery | Free restart; fixed 100 sandbox-gold or earned-test-ad extension, max two per attempt | Two exact 100-gold debits/cap and a real earned-ad +10-action callback pass in the recorded run; hostile persistence cases use a separate adapter |
| Six gradual powers | Spark/Vision/Flow/Shield/Focus/Trail at Journey 1/5/12/25/45/75; two shop pages and previews | Recorded unlock, stock debit, Shield recreation, Focus/Trail and Flow action checks pass; human comprehension remains unknown |
| Free inventory | One deterministic eligible power per five distinct Journey clears | Logic contracts and a seeded five-clear Android milestone claim pass; no paid random reward |
| Honest test ads | Ordinary commerce +35 test gold, continuation +actions, or +1 selected unlocked power; each placement grants only its announced reward | Actual continuation and Spark callbacks pass with zero extra gold; no-watch Back grants zero; ordinary +35 remains historical 1.2 evidence |
| Compatibility | C/R/E layouts preserved; balanced B/Q codes; old interrupted sandbox board resets with notice while progression/wallet remain | Restore/migration/deep-link cases, separate from stable package compatibility |

[Difficulty audit and CSV](DIFFICULTY.md) document lower structural score jumps, not measured enjoyment. [Move-limit research](MOVE_LIMITS_RESEARCH.md) distinguishes finite-move puzzle precedents from Crash Fever's different defeat/continue model. [Exact gameplay contract](GAMEPLAY.md#historical-lab-13-reference).

The [final installed 1.3 run](validation/v1.3/move-power-ui-smoke.txt) covers the recorded action/power/gold/loot cases and two actual Google sample-ad rewards: +10 actions and Spark ×1, with no extra gold. Its temporary Journey-75 access, wallet/inventory and five-clear profile were synthetic; six original game-owned preference files were restored byte for byte. [Final package integrity and scope](VALIDATION.md). The earlier +35-gold report remains 1.2 history. A separate [non-fixture Journey-1 run](validation/v1.3/exact-zero-ui.txt) confirms victory on the final available action and normal two-star rewards. Do not replace the missing player study with more successful scripted clears.

## Next — Physical quality and comprehension

**Entry:** use an identified stable build, with the tested package and device recorded. Test Lab-specific surfaces separately when that package has passed its own checks.

Test on at least one modest physical Android phone and one current phone, plus a smaller viewport and a large-screen configuration. Cover suspend/resume, interruptions, actual speakers/headphones, rapid taps, temperature/battery behavior, text readability and TalkBack. Fix regressions before the next cohort. Do not infer low-end performance from a powerful development host.

Observe 20 first-time players using the protocol in [HIT_AUDIT.md](HIT_AUDIT.md). Prioritize recurring confusion over requests for more features. Localize any changed help in all six languages and review it with speakers before marking translation quality validated.

**Exit:** publish a short device matrix, recurring confusion counts, fixes and retest results. Internal comprehension targets are met or explicitly revised with reasons.

## Next — Measure voluntary return

**Entry:** first-session friction and physical-device blockers have been addressed.

Run a fresh 100–300-player test, wait for complete D1 and D7 windows, and report enrolled denominators, missing observations and channel. The optional local journal can explain individual sessions; it does not aggregate cohorts. Use a consented study process to establish return rates.

**Exit:** a versioned cohort report, including the result if it is disappointing. The working targets are 60% reaching board 5, 30% D1 return and 10% D7 return. These are project hypotheses, not industry benchmarks. If the result is weak, revise onboarding, early difficulty or the hook and test a fresh cohort before spending more.

## Next — Store readiness and discovery experiments

**Entry:** player evidence justifies a public test.

Complete publisher identity, support contact, privacy policy hosting, content rating, target-audience declarations, Data safety, signing enrollment and the account's required testing process in Play Console. Run the Play pre-launch report. Resolve the actual report rather than assuming an uploaded bundle is approved.

For personal developer accounts created after 13 November 2023, Google's current guidance requires a closed test with at least 12 opted-in testers continuously for 14 days before applying for production access. Applicability depends on the actual publisher account; satisfying the count does not guarantee approval. [Official testing requirements](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en), checked 26 October 2026.

Test truthful store variants one variable at a time when traffic supports it: icon readability, first gameplay screenshot and explanation of gate consequences. Record visitors, installs, retained players and creative version. Do not optimize click-through by promising gameplay the app does not contain.

**Exit:** store review outcome, real conversion data and a measured source of qualified new players. The current work does not submit the app or contact testers.

## Later — Commercial tests and expanded content

The Lab prepares test controls now; **real traffic and real sales remain future work**. Before production, configure the owner's AdMob/UMP and Play accounts, verify actual license-test purchase behavior, qualify a backend wallet and token-verification service, review SDK terms and complete store/privacy declarations. Remove simulation controls from the production route. No service is deployed merely by documenting its contract.

Only run commercial experiments after retention is worth protecting. Model net revenue using observed impressions, fill, acquisition cost and retention; do not substitute a hypothetical eCPM or test paid-event callback for a business result. Preserve stable earned progress and keep Lab gold outside production entitlements.

Consider more mechanics, curated level packs, seasonal art or synchronized events only when player evidence points to a specific need. The wallet service is deployed but its provider and purchase lifecycle still need qualification. Cloud progression saves, remote leaderboards and live operations require separate integrations, an operating budget and privacy review; none is implied by a Google sign-in adapter, local equipment save or local event system.

## Stop or change direction when

- Players understand the rule but still choose not to return after two focused revisions.
- Acquiring retained players costs more than the conservative measured value they create.
- Maintaining additional systems consumes the time needed to improve the puzzle itself.

The outcome can be a smaller sustainable puzzle, a mechanic pivot, or ending the experiment. Adding content indefinitely is not a growth strategy.
