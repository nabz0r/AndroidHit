<h1 align="center">Luma Drift</h1>
<p align="center"><strong>A world at your fingertips.</strong><br>A tactile puzzle Journey and an illustrated home for your companion.</p>
<p align="center"><strong>1.17.0-rc6 · original sprites, places to touch and a living soundscape</strong><br><a href="docs/RELEASE_1_17.md">Release status</a> · <a href="docs/validation/v1.17/README.md">Evidence & pending acceptance</a> · <a href="docs/design/v1.17/reference-research.md">Primary-source design research</a></p>

**Current status: H (1.17.0-rc6 / code 24) is under native qualification after fixing the Guardian Back loop and legacy puzzle HUD overlap. Earlier candidate evidence remains historical. Final native H acceptance, canonical packages, Git main and Firebase publication are pending.**

Four separate places—Adventure, Home, Collection and Workshop—use illustrated scenery and touchable objects in place of visible button rows, pills and a navigation bar. Optional swipes connect the places; named object targets keep navigation discoverable. Short scene fades respect reduced motion. Deeper menus retain clear costs, ownership, confirmations and accessible actions.

The bundled original art covers **24 companions, 24 garments and 42 furnishings**, with three scene illustrations and new room materials. Portraits compose the actual four clothing slots over each companion's anatomy. The three 10 × 10 rooms retain close-up, pan, pinch and whole-room views. A chosen nickname, flag and earned badges belong to the local player card.

Five sound contexts × ten biomes provide **50 synthesized arrangements**: the ten historical puzzle themes plus forty new place arrangements. Music and effects remain separate. [Actual synthesizer previews and measured host scope](docs/ART_AUDIO.md) distinguish generated PCM from recorded Android or physical-speaker output.

The U1–U1000 puzzle rules, earned-coin economy and existing save identities are retained. Companion arrivals still contain 23 possible companions for 180 earned coins; the outfit pool contains five complete looks for 260. Odds and duplicate returns are shown before spending. Test ads, closed real checkout and disabled paid premium draws remain. Google sign-in does not synchronize the local house, profile or game save.

The [release table](docs/RELEASE_1_17.md) records the audited H bytes and source freeze. H fixes a reproduced Guardian return loop, legacy puzzle gates covering their budget/help, and misnamed event previews. Shared geometry gives gates and captions separate space, keeps the board useful on short screens and scrolls complete object actions into view. The [source review](docs/validation/v1.17/review/source-candidate-h-preflight.json), native route/visual reports and [website publication register](docs/validation/site-v1.17/README.md) keep their distinct scopes. [Build and contracts](docs/DEVELOPMENT.md) · [Installation and activation boundaries](docs/PRODUCTION_RELEASE.md).

## Preserved 1.16 delivery

The home now fills its actual scene viewport, with a closer default camera, finger pan and an explicit whole-room view. Larger collection portraits, illustrated Atelier cards, accented pill actions and icon-medallion navigation give objects and characters more room.

Choose a local nickname, a flag and three badges from six earned milestones. Two separate arrivals banners use earned LumaCoins: 23 companions and five complete four-piece outfits. Exact odds and duplicate returns are shown before drawing; debit, outcome, ownership and the last result are saved together. Existing purchases, clothing capsules, 12 free avatar styles, furniture positions and puzzle rules are retained.

**[Signed 1.16 production APK](releases/luma-drift-1.16.0-rc1.apk)** · [AAB](releases/luma-drift-1.16.0-rc1.aab) · [Manifest](releases/manifest-1.16.0-rc1.json) · [Separate Lab APK](releases/luma-drift-1.16.0-sandbox-rc1.apk).

Candidate B uses code 18 and the existing signing certificate. Install production over the previous production app to preserve local data. Nickname, flag, badges, house and gameplay remain local; Google sign-in does not synchronize them. Test ads and disabled paid-provider gates are retained. The executed native, visual and publication scopes are recorded in the [current qualification register](docs/validation/v1.16/README.md); the 1.15 full 1,000-level route below remains historical evidence.

**[See the eight new screens](https://luma-drift-production.web.app/#screens)** · **[Download the exact tested Android APK](https://luma-drift-production.web.app/releases/luma-drift-1.16.0-rc1.apk)**. The FR/EN site serves sixteen unedited B captures and the signed production APK/AAB/manifest. All 151 public payloads and three directory aliases match the deployed source. [Hosting and browser evidence](docs/validation/site-v1.16/README.md).

<p align="center"><img src="public/assets/screenshots/v1.16/home-fr.png" width="230" alt="Actual closer furnished home in French"><img src="public/assets/screenshots/v1.16/profile-fr.png" width="230" alt="Actual local player card with nickname flag and three earned badges"><img src="public/assets/screenshots/v1.16/summon-fr.png" width="230" alt="Actual companion arrivals banner and earned-coin cost"></p>

## Preserved 1.15 delivery

<p align="center"><strong>1.15.0-rc1 · separated screens, relay puzzles and a larger home</strong><br><a href="docs/RELEASE_1_15.md">Release & evidence</a> · <a href="docs/GAMEPLAY_1_15.md">Gameplay and compatibility</a> · <a href="docs/validation/v1.15/README.md">Qualification</a></p>
<p align="center">1,000 versioned relay Journey boards · 30 staged opening boards · 6 languages<br>Four dedicated destinations · Three 10 × 10 rooms · 42 furniture designs</p>

**[See the redesigned website and native gallery](https://luma-drift-production.web.app/#screens)** · **[Download the signed 1.15 APK](https://luma-drift-production.web.app/releases/luma-drift-1.15.0-rc1.apk)**. The existing Firebase site now serves the exact delivered production APK/AAB and twelve unedited native frames. [Website source, browser and public-byte evidence](docs/validation/site-v1.15/README.md).

## Make your move, then make yourself at home — 1.15

Adventure, Home, Collection and Atelier now have dedicated screens. Adventure starts the next puzzle directly. Play keeps the board and its essential controls together; the wallet and global navigation return when you leave the puzzle. Illustrated item pages, a visual inventory grid, a larger wardrobe and concise reward panels give artwork and choices more space.

The new **U Journey** changes gates through piece exits and matching keys. There is no manual gate-opening action in U. Choose an order that keeps later exits possible; a wrong order can deadlock the position. Blocked tries cost nothing, every correction can be undone and refunded, and restart is free. Three stars acknowledge completion, rather than an unmeasured efficiency grade. Historical challenge codes and an active saved run retain their original boards and rules.

The home has a living room, bedroom and garden, each with a **10 × 10** floor, zoom and pan. **Forty-two furniture designs**, including an aquarium, grand piano, greenhouse, telescope, arcade cabinet and pond, have distinct original drawings and two orientations. Previewing, buying, placing, rotating and cancelling remain separate actions. Old furnishings, positions and earned balances are preserved.

[Download the signed 1.15 APK](releases/luma-drift-1.15.0-rc1.apk). The exact final production bytes complete **U1–U1000 on Android: 49,575 native checks, 12,016 actual touch gestures and 55,000 genuinely earned victory coins**, with no injected progression. Normal/320 dp screens, six languages, a real Lab 1.14 upgrade, charged powers, earned furniture purchases and cold restoration have separate passing records. The four signed binaries pass the independent [signature, manifest, bundle and static alignment audit](docs/validation/v1.15/binaries-e/binary-final-candidate.json). The [independent relay audit](docs/validation/v1.15/relay-engine/relay-host.json) passes **13,312,469 checks** across all 1,000 U boards; home transactions and projected geometry pass **2,074** and **6,494** checks. See the [qualification register](docs/validation/v1.15/README.md) for exact scopes and limits.

An active older puzzle keeps its original rules. To start the new U board at your current Journey level, tap its numbered node in Adventure; restarting the retained old attempt preserves that old board. Install the APK over the existing signed production app to keep local data.

<p align="center"><img src="docs/screenshots/v1.15/gallery/adventure-fr.png" width="230" alt="Actual 1.15 Adventure destination in French"><img src="docs/screenshots/v1.15/gallery/puzzle-u13-en.png" width="230" alt="Actual 1.15 U13 puzzle with piece-controlled gates"><img src="docs/screenshots/v1.15/gallery/atelier-en.png" width="230" alt="Actual 1.15 Atelier with distinct illustrated furniture worlds"></p>

Unedited final-E production frames from the passed native interface and U1–100 scopes. [Exact source/hash provenance](docs/screenshots/v1.15/gallery/provenance.json).

Portrait phones are the supported presentation. Gameplay and home saves remain local. Source/model checks, emulator execution, physical-phone experience and live commercial services are distinct scopes. No external game's artwork, characters, audio or code is included.

## Preserved 1.14 delivery

<p><strong>1.14.0-rc2 · source, signed artifacts and website delivered</strong><br><a href="docs/RELEASE_1_14.md">Release & evidence</a> · <a href="docs/GAMEPLAY_1_14.md">Historical gameplay rules</a> · <a href="docs/validation/v1.14/README.md">Qualification</a></p>

## Every action has a clear meaning — 1.14

The main puzzle now teaches its rules in the route itself. Straight exits lead to blockers, matching K keys, manual gate openings and switch order before those mechanics combine. A closed side is a real button with a visible one-action cost. The large arrow gives movement; small arrows name the gates changed after exit.

- **Make decisions within a real budget.** A successful exit or gate opening costs one action. A blocked try costs nothing. One Return per attempt restores the preceding position and refunds its action; its used quota survives a cold process restart. Inefficient productive choices can still lose an attempt. Restarting is free.
- **Learn through the first thirty boards.** The new N route starts with thirty authored teaching boards, then combines 970 existing piece layouts with a globally paced sequence and revised initial gate states where needed. Contextual rules and immediate causal feedback explain changes without a constant solution overlay.
- **Keep old puzzles identifiable.** N challenge codes have their own resolver. Existing L/M/T and earlier codes retain their boards. An in-progress save keeps its original geometry when its ledger is upgraded; the next new Journey attempt uses N.
- **Come home between puzzles.** Arrange twelve furnishings, dress and hug the companion, move it along a clear floor path, preview placements and work towards a chosen furnishing with existing earned LumaCoins.

[Release scope and current evidence](docs/RELEASE_1_14.md) · [The reported L15 diagnosis](docs/validation/v1.14/puzzle-ui-audit.md). The RC2 source contracts, independent curriculum review, signed-byte checks and scoped native acceptance pass. Installed-candidate acceptance, signed artifact hashes, public download verification and Firebase delivery remain separate recorded steps. Mathematical solvability is not a human difficulty or retention result.

The RC2 [content contracts](docs/validation/v1.14/audit-learning/audit-learning-contracts.json) pass 201,143 checks across 1,000 boards. A separate [curriculum review](docs/validation/v1.14/independent-pacing-review/README.md) recomputes every score and class: all 97 later episodes retain three starts with a free exit, and the longest Hard/Expert run is four. The median structural score rises over each 200-board stage. [Archived RC1 content](docs/validation/v1.14/audit-learning-rc1/README.md), intermediate installed runs and older release images do not certify the final RC2 APK. The final native images are original RC2 captures. Public artifact delivery has its own exact-byte receipt.

<p align="center"><img src="public/assets/screenshots/v1.14/puzzle-en.png" width="230" alt="Final native RC2 N15 puzzle with a one-action gate and one Undo"><img src="public/assets/screenshots/v1.14/home-en.png" width="230" alt="Final native RC2 companion home with an earned sofa"><img src="public/assets/screenshots/v1.14/furniture-en.png" width="230" alt="Final native RC2 furniture catalogue with earned prices and ownership"></p>

Unedited **1.14.0-sandbox-rc2** full frames from the globally passed/restored [716-check native first30/gallery route](docs/validation/v1.14/ui-bugs/ui-sdk-final30-gallery-rc2-a/host-report.json). [Exact image provenance](docs/validation/site-v1.14/screenshot-provenance.json). All 1,000 final N boards have recorded native three-star wins across ten complete passed batches: **55,423 checks, 19,920 productive actions and 142 indexed original PNGs**. This is coverage across two sessions, not a passed continuous marathon. C keeps its global FAIL/restored result: its eight complete batches cover N1–800; its failed ninth batch contributes nothing. The separate restored D session covers N801–1000 after the explicitly recorded synthetic prerequisite `unlocked=801`; no stars, coins, session, history or solution actions are injected by that setup. [Coverage receipt](docs/validation/v1.14/ui-bugs/ui-sdk-coverage801-1000-rc2-d/independent-coverage-check/coverage.json).

[Production APK](releases/luma-drift-1.14.0-rc2.apk) · [AAB](releases/luma-drift-1.14.0-rc2.aab) · [Production manifest](releases/manifest-1.14.0-rc2.json) · [Lab APK](releases/luma-drift-1.14.0-sandbox-rc2.apk) · [Lab manifest](releases/manifest-1.14.0-sandbox-rc2.json). The [RC2 four-artifact binary audit](docs/validation/v1.14/binaries-rc2/binary-final-candidate.json) passes. Source and signed artifacts are published on [main at `578661da`](https://github.com/nabz0r/AndroidHit/commit/578661daf81be8896668d427bef061f5f0fff700); [four independent public-download checks](docs/validation/v1.14/publication.json) return HTTP200 with exact sizes and SHA-256. The [puzzle-contract CI run](https://github.com/nabz0r/AndroidHit/actions/runs/40754557397) succeeds. The existing [Firebase site](https://luma-drift-production.web.app/) is deployed from clean website commit `4466cb4ab637da9c35f454e8aee552bce64e250e`, version `9cd94929689ee2f6`, finalized **2026-10-28T19:36:42.298Z**. Its 117 public payloads and three directory aliases independently match their exact source bytes. Actual public desktop FR/EN rendering passes; mobile/compact review remains the separate 19-state local scope. [Hosting/public-browser evidence](docs/validation/site-v1.14/README.md).

Android 8+ / API 26. Production package `com.androidhit.lumadrift`; separate Lab package `com.androidhit.lumadrift.sandbox`. Gameplay, house state and earned LumaCoins remain local. Google sign-in does not synchronize them. Test ads are forced; real checkout and premium draws remain disabled.

```sh
./gradlew assembleProductionRelease bundleProductionRelease lintProductionRelease
./gradlew assembleLabRelease bundleLabRelease lintLabRelease
bash scripts/test-learning-v114.sh
bash scripts/test-deliberate-play-v114.sh
bash scripts/test-learning-guide.sh
bash scripts/test-home-v113.sh
node --test public/tests/site-language-v114.test.mjs public/tests/deletion-core.test.mjs
```

[Development](docs/DEVELOPMENT.md) · [Roadmap](docs/ROADMAP.md) · [Documentation](docs/README.md). Source and original repository assets: **AGPL-3.0**. [Third-party notices](docs/THIRD_PARTY_NOTICES.md).

The sections below preserve dated releases and their original rules. Their test counts and deployment receipts do not certify the then-current 1.15 release, whose U recovery rules and evidence are recorded above.

## Preserved 1.13 — your little home

The puzzle remains the main game. Clear pieces in the right order, earn existing LumaCoins, then return to a room you can arrange around your companion.

- **Make yourself at home.** Move, rotate and store furniture in a room seen from three quarters. Preview a placement before confirming. Bed, lamp, plant, rug and cushion are included; seven more furnishings use earned LumaCoins. Four wall finishes and four floors include two free choices each.
- **Spend a moment together.** Tap a clear floor tile to call your companion, share a hug or use a furnished corner. The room displays the companion and clothing you actually equipped. Hugs, light and rest animations are temporary; furniture, ownership, colours and companion position are saved.
- **Choose a little wish.** Pin one unowned furnishing, see your actual coin progress, then buy and place it when you choose. A purchase grants ownership; it never silently places furniture. Stored objects stay yours.
- **Find the first click sooner.** A three-piece opening introduces one switch on a real puzzle board, with skip and exact local history recovery. It grants no campaign progress or invented reward. The existing seven optional lessons remain replayable.

[Home rules and economy](docs/HOME_1_13.md) · [Changes and limits](docs/RELEASE_1_13.md). Puzzle layouts, fixed budgets, old challenges, earned progression and existing equipment are preserved. The L22→L23 structural jump and the Guardian 4→5 transition remain separate design work. Human enjoyment, comprehension and D1/D7 retention are unmeasured.

<p align="center"><img src="public/assets/screenshots/v1.13/home-en.png" width="230" alt="Actual English RC2 personalised home and equipped companion"><img src="public/assets/screenshots/v1.13/house-decor-fr.png" width="230" alt="Actual French RC2 room editor and furniture placement preview"><img src="public/assets/screenshots/v1.13/puzzle-en.png" width="230" alt="Actual English RC2 main puzzle"></p>

Unedited **Lab 1.13.0-sandbox-rc2** full frames from the [798-check restored native route](docs/validation/v1.13/ui-bugs/ui-sdk-rc2-full13/host-report.json), with actual Journey play and earned purchases. These are emulator observations, not human retention evidence. [Exact nine-image source/hash provenance](docs/validation/site-v1.13/screenshot-provenance.json).

## RC2 release and qualification

Production **1.13.0-rc2** and separate Lab **1.13.0-sandbox-rc2**, version code **14**, replace the unshipped RC1 candidate after a real tutorial Later→Resume crash was reproduced. The new signed production APK is **7,806,558 bytes / 7.81 MB**, SHA-256 `c68be62fd940e753fc06c4b5677a48cafaf077a4710755b3bdf96467d571e31b`.

[Production APK](releases/luma-drift-1.13.0-rc2.apk) · [AAB](releases/luma-drift-1.13.0-rc2.aab) · [Production manifest](releases/manifest-1.13.0-rc2.json) · [Lab APK](releases/luma-drift-1.13.0-sandbox-rc2.apk) · [Lab manifest](releases/manifest-1.13.0-sandbox-rc2.json).

Published on `main` from [source `67774c14`](https://github.com/nabz0r/AndroidHit/commit/67774c143a60017049207adcedf2ea63fe048d97). Four independently streamed public APK/AAB downloads return **HTTP 200**, matching all frozen sizes and SHA-256 hashes (**38,006,707 bytes total**). [Publication receipt](docs/validation/v1.13/publication.json).

The [current qualification record](docs/validation/v1.13/README.md) separates source checks, exact binary checks, final installed acceptance and publication. The completed source checks include **891 home model/durability assertions** and **18 checks of the actual three-piece opening**. RC1 observations and its successful build/binary checks remain historical: they do not qualify the corrected RC2. The [RC2 build](docs/validation/v1.13/build-rc2.txt) and [binary audit](docs/validation/v1.13/binaries-rc2/binary-verification.txt) pass. The [targeted RC2 tutorial pilot](docs/validation/v1.13/ui-bugs/ui-sdk-rc2-tutorial-fast/host-report.json) passes 90 native checks, including quick Later→Resume below the old feedback window. Separate RC2 scopes pass **55 genuine fresh-Lab checks**, **7 production startup checks** and **35 production house checks**. The complete [retained-Lab route](docs/validation/v1.13/ui-bugs/ui-sdk-rc2-full13/host-report.json) passes **798 checks /13 actually played Journey boards**, including six languages, compact/landscape room controls and a distinct cold process; original preferences and display settings are restored. GitHub and Hosting results have their own delivery receipts.

The FR/EN [website](https://luma-drift-production.web.app/) is updated to 1.13 from the same source `67774c14`: **108 files /77,610,834 bytes**, Firebase version `170cf6d048c1b40f`, finalized **28 October 2026 at 16:03:50.235 UTC**. The [public-origin verification](docs/validation/site-v1.13/public-origin.json) independently confirms all 108 payloads plus three directory routes, HTTP200 with exact bytes/SHA-256. Its 2,150 source assertions, 14 local browser states and actual RC2 game captures have their own [site qualification record](docs/validation/site-v1.13/). Later documentation-only commits do not replace the deployed source identity.

Android 8+ / API 26; production package `com.androidhit.lumadrift`. Installing the same signed package over the existing app retains its local save. Google identity does not synchronize gameplay or the house. Test ads remain forced; real checkout and premium draws remain disabled. This delivery does not establish a Google Play upload, approval, live purchase or real-account acceptance.

## Build and inspect

Use JDK 21, the Android SDK and the Gradle wrapper; Java source compatibility is 17. Private signing files are ignored and never published.

```sh
./gradlew assembleProductionRelease bundleProductionRelease lintProductionRelease
./gradlew assembleLabRelease bundleLabRelease lintLabRelease
bash scripts/test-home-v113.sh
bash scripts/test-first-light-v113.sh
bash scripts/test-store-appearance-v111.sh
bash scripts/test-store-init-v111.sh
bash scripts/test-store-booster-v112.sh
bash scripts/test-tutorial-v18.sh
python3 scripts/check-site-v113.py --apk-sha256 c68be62fd940e753fc06c4b5677a48cafaf077a4710755b3bdf96467d571e31b
node --test public/tests/site-language-v113.test.mjs public/tests/deletion-core.test.mjs
```

[Development](docs/DEVELOPMENT.md) · [Native SDK runner](scripts/v113-ui-test/README.md) · [Roadmap](docs/ROADMAP.md). Original game source and repository assets: **AGPL-3.0**. [Notices](docs/THIRD_PARTY_NOTICES.md). No Animal Crossing, Candy Crush or Crash Fever artwork, characters, audio or code is included.

The sections below preserve earlier releases and their dated evidence. Their test totals and deployment receipts do not certify 1.13.

## Preserved little atelier — 1.12

- **Know which currency you are using.** Amber round LumaCoins and violet/cyan LumaGold diamonds have complete names and distinct balances. Unknown account funds show a dash and an explicit state; last-known funds cannot authorize a spend.
- **Browse without interruptions.** Power, companion, theme, clothing and equipment details are ordinary pages with a large preview, effect/source, stock/state, price and Close. Buying and equipping are separate choices.
- **Recognize your tools.** Six original friendly power illustrations match the shop, inventory, rewards and play HUD. Flatter surfaces, pills and open curved accents replace stacked bevels and repeated panels.
- **Keep the action dependable.** Exact earned-power debit/grant is tracked through storage refusal and retry. Empty-power browsing returns to the same live run, including through Activity recreation.

[Changes, exact artifacts and acceptance](docs/RELEASE_1_12.md) · [Primary research and UX hypotheses](docs/research/ux-v112.md). Human enjoyment and retention remain unmeasured. Test ads and commercial gates are retained; no Play upload is part of this release.

<p align="center"><img src="docs/screenshots/v1.12/ui-bugs/ui-sdk-final-rc2-lab40/prepare/ux-home-wallets-fr.png" width="230" alt="Actual French 1.12 RC2 Home with separate earned/premium balances"><img src="docs/screenshots/v1.12/ui-bugs/ui-sdk-final-rc2-lab40/prepare/ux-shop-powers-fr.png" width="230" alt="Actual French1.12RC2 atelier, illustrated powers, stock and price"><img src="docs/screenshots/v1.12/ui-bugs/ui-sdk-final-rc2-lab40/prepare/ux-power-detail-fr.png" width="230" alt="Actual French1.12RC2 integrated power detail, effect, price and Close"></p>

Unedited final **Lab 1.12.0-sandbox-rc2** frames. **1,179 native checks / 40 real Journey clears** pass, including all six shop languages, actual earned purchases, live-run return through rotation, equipped-art comparisons and a distinct cold process. The guest legitimately replays L13 for the return test; the 2,425 coins shown are its observed earned balance. The unknown account LumaGold is not a simulated balance. [Executed evidence](docs/validation/v1.12/ui-bugs/ui-sdk-final-rc2-lab40/README.md).

**Preserved website 1.12 delivery:** [Open Luma Drift](https://luma-drift-production.web.app/). The refreshed FR/EN page shows nine exact native RC2 captures, the atelier, equipped portrait, clear currencies and the current signed APK. Firebase confirms98 source payloads from`91ffac7a`, with a finalized live release at**28 October 2026,14:08:23.437 UTC**. [Delivery and local checks](docs/validation/site-v1.12/README.md).

## Preserved equipment fixes — 1.11

- **Apply, then see the result.** Equipping a companion, clothing or gear returns to the portrait. Compact clothing labels show the actual variant; all seven worn slots retain full names in their accessible descriptions and offers. Clothing purchase opens an owned offer with an explicit Equip button.
- **Keep the companion recognizable.** Clothing leaves more of the face and silhouette visible. Headwear, aura and charm have their own space and are larger. The same original renderer is used in Home, wardrobe, inventory, previews and gameplay.
- **Make the theme count.** The garden and animated play scene now use the selected theme. The installed 1.10 baseline reproduced zero scene-pixel change despite equipping Lagoon.
- **Recover the exact save.** Ownership/equipment reads settle pending writes; failed first-profile initialization no longer permits a later starter-wallet reset. Controlled failures and normal 72-item acquisition/equipment are tested separately from native rendering.

<p align="center"><img src="docs/screenshots/v1.11/fresh-install-rc2/home-en.png" width="230" alt="Actual RC2 fresh guest Home with 180 coins and the visible starter companion"><img src="docs/screenshots/v1.11/fresh-install-rc2/avatar-followup/avatar-slots-en.png" width="230" alt="Actual RC2 wardrobe: four complete Dew labels and three named equipment slots"><img src="docs/screenshots/v1.11/ui-bugs/ui-sdk-final-rc2-lab50/prepare/visual-final-equipped-original.png" width="230" alt="Actual RC2: earned Cupid and Little witch clothing, Lantern Leaf and Miko in free Retro arcade style"></p>

The first two unedited **Lab 1.11.0-sandbox-rc2** captures show a genuinely new installation. Final installed qualification separately passes **1,589 Lab checks/50 clears** and **935 production checks/40 clears**, including earned purchases, actual equipped art and real cold relaunches. [Bug hunt, exact bytes and limits](docs/RELEASE_1_11.md) · [Qualification](docs/validation/v1.11/README.md) · [Fresh arrival and six-language follow-up](docs/validation/v1.11/fresh-install-rc2/README.md). The third frame is the later 50-clear qualification profile after real purchases; its 1,140 coins are the observed remaining balance, not a seeded budget. Test advertisements are forced and real checkout remains gated. No Play upload is performed.

## Preserved 1.10 screens and foundation

<p align="center"><img src="docs/screenshots/v1.10/ui-sdk-final3-full100/home-fr-localized.png" width="230" alt="Actual French rc3 Home: guest, pinned Cupid goal and resumed level11"><img src="docs/screenshots/v1.10/ui-sdk-final3-full100/avatar-fr.png" width="230" alt="Actual French rc3 companion with four cosmetic and three equipment slots"><img src="docs/screenshots/v1.10/ui-sdk-final3-full100/story-fr.png" width="230" alt="Actual French rc3 journal: chapter2, two claimed fragments and zero clears in its band"></p>

Unedited **Lab 1.10.0-sandbox-rc3** Android captures. The native route really completes the first 100 Journey levels, then replays boards: Home resumes **L11**, while the journal shows chapter 2. The Cupid goal is affordable but still unowned; its preview grants nothing. These are local guest states, not Google identity or payment evidence. [Capture provenance](docs/validation/site-v1.10/captures.json).

**Preserved 1.10 website delivery:** [Luma Drift 1.10 was deployed](https://luma-drift-production.web.app/). Firebase CLI and live-channel management confirm 89 files from source `9be35d2b`, finalized at **28 October 2026, 10:44:28.535 UTC**. [Exact delivery proof](docs/validation/site-v1.10/deployment.json). Local browser review covers 12 FR/EN DOM states and six accepted desktop renders; the incomplete mobile provider images are excluded from visual acceptance. No public-origin byte probe, account action or provider transaction is inferred. [Scope and limits](docs/validation/site-v1.10/README.md).

## Companion & Objectives — 1.10

- **Know what is available.** Home displays real unclaimed rewards, a chosen collection goal and its actual progress. Today, This week, Journey and Ready to collect each have a distinct objective tab. Viewing a page does not collect anything.
- **See the same companion everywhere.** One portrait combines the selected character/style with hat, dress, shoes, gloves, head equipment, aura and charm. Inventory, wardrobe and full-character previews share it. Twelve visual treatments are free; existing equipment retains its stated bounded effects.
- **Choose one item to work towards.** Pin an item from the existing 24 cosmetics and 18 equipment pieces. Read its progress and actual acquisition source. Previewing, viewing a source, purchasing and equipping stay separate actions; unowned items cannot be equipped by previewing them.
- **Read the Prism journal.** Ten original chapter stories follow the existing biomes. Journey clears and claimed optional guardian fragments are separate facts. Reading the journal grants no reward and makes no guardian mandatory.
- **Find the next event action.** The next daily/weekly stage and the existing 100/250-LumaCoin completion bonuses are visible earlier. Bonus claims retain their once-only receipt across local-save failure and retry.
- **Keep the currencies clear.** The shop separates Powers, Appearance and the account LumaGold wallet. Local earned LumaCoins remain distinct from premium account funds. Test ads are forced, real checkout is gated and paid cosmetic draws stay disabled.

<p align="center"><img src="docs/screenshots/v1.10/ui-sdk-final3-full100/ready-fr.png" width="230" alt="Actual French rc3 ready list: six unclaimed rewards"><img src="docs/screenshots/v1.10/ui-sdk-final3-full100/shop-looks-fr.png" width="230" alt="Actual French rc3 appearance shop: earned-coin capsule and unowned hats"><img src="docs/screenshots/v1.10/ui-sdk-final3-full100/avatar-preview-cupid-en.png" width="230" alt="Actual English rc3 unowned Cupid preview with price, goal and Close controls"></p>

The pastel native dialogs, visible selected tabs and reachable lower controls are checked separately from their accessibility state. The earlier candidate's transparent selection surfaces and clipped footers remain in the [qualification history](docs/validation/v1.10/README.md). [UX contract](docs/UX_1_10.md) · [Readability/provenance matrix](docs/validation/v1.10/ux-readability/README.md) · [Journal model](docs/PRISM_STORY_1_10.md).

## Every move still matters

A piece follows its arrow. Keys dissolve matching seals, switches preview their gate changes, and a closed gate can be opened manually for one action. Each L/M board keeps its fixed productive-action allowance and margin. **Undo is free and refunds the reversed action, including at zero moves. Restart is free.** Briefings show the action target for three stars; hints and Undo counters are not score penalties.

Seven optional, replayable practice boards teach arrows, blockers, keys, gates, switches, refunded Undo and Spark. Completing them grants **100 LumaCoins + two Sparks once**. Practice consumes no inventory and grants no campaign wins. Six powers retain explicit free one-charge samples at their existing unlock levels.

Every ten distinct Journey clears unlocks another optional Prism guardian. Two-layer pieces need two actions; from guardian 5, marked pieces move one cell per action. Free guidance, Undo and restart remain. A solved guardian offers its existing **100 earned LumaCoins once**, only when claimed. The main route does not require it.

The world retains ten 100-level atmospheres, original synthesized music, separate music/effects controls and reduced motion. The 24 companions × 12 styles are **288 code-drawn combinations**, not 288 hand-painted images. Cosmetic clothing and existing bounded-benefit equipment remain different systems. A missed visit does not reset the seven-gift cycle.

[Gameplay](docs/GAMEPLAY.md) · [Tutorial](docs/TUTORIAL.md) · [Guardians](docs/GUARDIANS.md) · [Art/audio](docs/ART_AUDIO.md) · [Economy](docs/ECONOMY_1_7.md).

## Preserved 1.10 checks and limits

The final rc3 [native full-route run](docs/validation/v1.10/ui-sdk-final3-full100/host-report.json) passes **2,514 assertions** with **96 original PNGs**, covering the first 100 Journey levels, objectives, preview/ownership, goal persistence, all ten journal chapters, styles, guardians, navigation, footer bounds and six languages. Original preferences and rotation are restored. A [genuine fresh installation I](docs/validation/v1.10/ui-sdk-fresh-guest-i/README.md) separately passes **91 checks** and retains its new guest. [Production startup and actual audio](docs/validation/v1.10/SDK_UI_FINAL.md) are separate from the Lab route. These are emulator observations, not physical-device or human-comprehension acceptance.

Source checks separately cover **6,544 portrait/translation assertions**, **443 objective/store assertions**, and **308,412 read-only journal assertions** across every clear-count boundary from 0 to 1000. Existing economy, inventory, data, local-save and Journey regressions are retained in the [qualification index](docs/validation/v1.10/README.md). Signed APK/AAB checks, official bundletool and 16KiB ZIP/native alignment belong to the [exact binary audit](docs/validation/v1.10/binaries/binary-final-candidate.json).

**Puzzle geometry and difficulty are unchanged.** The documented L22→L23 structural jump from 17 to 85 remains. Structural scores, source simulations and successful scripted clears do not predict human enjoyment, D1/D7 retention or revenue. [The 2026 research and synthetic audit](docs/PLAYER_AUDIT_2026.md) keeps published survey aggregates distinct from assumed game profiles. No individual social data is scraped. [Current backlog](docs/PRISM_STORY_1_10.md#unchanged-structural-backlog).

## Preserved signed 1.12 artifacts

| Artifact | Download | SHA-256 / manifest |
| --- | --- | --- |
| Production APK · 7.77 MB | [1.12.0-rc2](releases/luma-drift-1.12.0-rc2.apk) | `de581253b64232ae45bcbd647862dd470d522068f26516f6417bf515e882403d` |
| Production AAB | [1.12.0-rc2](releases/luma-drift-1.12.0-rc2.aab) | [Manifest](releases/manifest-1.12.0-rc2.json) |
| Separate Lab APK/AAB | [Lab APK](releases/luma-drift-1.12.0-sandbox-rc2.apk) · [AAB](releases/luma-drift-1.12.0-sandbox-rc2.aab) | [Manifest](releases/manifest-1.12.0-sandbox-rc2.json) |

Android 8+ / API 26; code 13; production package `com.androidhit.lumadrift`. Install over the existing app to retain local saves; signing identity is unchanged. [Installation](docs/RELEASE.md) · [Checksums](releases/SHA256SUMS.txt) · [Qualification](docs/validation/v1.12/README.md). [Preserved 1.11 delivery](docs/RELEASE_1_11.md) and its exact artifacts remain available.

Published on `main` from [source 32bf07ea](https://github.com/nabz0r/AndroidHit/commit/32bf07ea2ef2aa9dd962d81145bf075c6ca05b2e). Four pinned public downloads independently return HTTP 200 with the exact signed-candidate sizes and SHA-256 hashes. [Delivery receipt](docs/validation/v1.12/publication.json).

Google identity does not synchronize local gameplay. Actual two-account gifts, real consent/ads and Play purchase lifecycle remain separately qualified. Website 1.12 has its separate [Hosting receipt and local browser qualification](docs/validation/site-v1.12/README.md). No Play or server deployment occurs in the Android release.
