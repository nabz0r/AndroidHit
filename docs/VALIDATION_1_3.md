# Validation — Luma Drift Lab 1.3.0-sandbox

**26 October 2026 · version code 4 · package com.androidhit.lumadrift.sandbox**

Package integrity, deterministic simulation, actual Android interaction and business outcomes are distinct evidence. This report concerns the current Lab; [Lab 1.2](VALIDATION_1_2.md) and [stable 1.1](VALIDATION_1_1.md) keep their historical results.

## Build and integrity

| Check | Result |
| --- | --- |
| Toolchain | JDK17, Gradle8.14.3, AGP8.11.0, compile/target/build-tools36; R8/resource shrinking |
| Release tasks | assembleRelease, bundleRelease and lintRelease succeed |
| Lint | [0 errors / four advisory warnings](validation/v1.3/lint.txt): newer target/compile SDK, Gradle and preview lint availability |
| APK | [One RSA-3072 signer; scheme v2 verifies](validation/v1.3/apk-signature.txt) |
| AAB | [bundletool validates](validation/v1.3/bundle-validation.txt); [JAR signature verifies](validation/v1.3/aab-signature.txt) with self-signed/timestamp/streaming-layout qualifications |
| Package | [1.3.0-sandbox, code4, min26, target36](validation/v1.3/package-badging.txt) |
| Exact bytes | [Release sizes/hashes](RELEASE.md), [versioned source manifest](../releases/manifest-1.3.0-sandbox.json), [merged-manifest audit](validation/v1.3/manifest-audit.json) |

Standalone lint **9.4.0**, selected with the documented experimental override, reads Kotlin2.4 metadata in the latest SDK dependencies while retaining the tested AGP/Gradle setup. No analyzer incompatibility error is suppressed. Gradle also reports deprecated build features. A future major Gradle version has not been qualified.

The merged manifest has no Firebase automatic initialization provider or advertising auto-init provider; AndroidX startup remains. Its permission list is recorded in the audit. Static inspection does not establish absence of every SDK network request. Billing queries and explicitly requested Google routes can contact Google independently of the device-local journal.

## Deterministic checks

| Suite | Passing result | Scope |
| --- | ---: | --- |
| [Engine](validation/v1.3/engine-tests.txt) | 435,796 assertions | Original 240 Journey +240 Remix, 1,002 event +1,002 paired-switch seeds, frozen layouts, solutions, restore/undo/gates and C/R/E/B/Q challenge parsing/fuzzing |
| [Budget](validation/v1.3/move-budget-tests.txt) | 340,768 checks | 1,962 original/balanced/event boards; 100,000 corrupt saves, 20,000 valid snapshots, 100,000 unlimited friend actions, exact-zero win, allowance/quota/extension/restart/overflow contracts |
| [Economy](validation/v1.3/economy-tests.txt) | 4,586 checks | Six unlocks, preserved locked stock, progression prices, exact100 debit/idempotence, unique power grants, free loot every5 clears, caps, concurrent store handles and modeled memory-before-disk failures |
| [Data/languages](validation/v1.3/data-tests.txt) | 7,941 checks | Progression/rewards, shop, events, cosmetics and EN/FR/DE/ES/PT/IT keys/placeholders |
| [Difficulty](validation/v1.3/difficulty-tests.txt) | 19,054 checks | Every mapped solution, solver equivalence, complete frozen permutations, retained chapter pools/tutorials/source layouts and measured reduction of structural jumps |
| [Collection/calendar](validation/v1.3/collection-events-tests.txt) | 87,121 checks | 24 companions, legacy six frozen, budget and 604 zones /14,496 calendar boundaries including DST23/25-hour days |
| [Meta/journal](validation/v1.3/meta-tests.txt) | 362 checks + JSON round trip | XP/missions, migration, local observation bounds, Unicode/control characters and retry modeling |
| [Ad receipts](validation/v1.3/commerce-receipts-tests.txt) | 42 checks | Actual compiled receipt methods with Android adapters: memory-before-disk failure, durable retry, duplicate/show binding, malformed IDs, request isolation and ack/recreation |

The [480-row report and before/after plot](DIFFICULTY.md) show mean absolute adjacent structural change from12.21→4.26 in Journey and14.45→3.57 in Remix. Upward jumps above30 structural points drop10→1 and14→1. These are declared structural heuristics, not player pass rates. No layout was removed or regenerated.

Preference fault adapters model memory and durable maps separately; no physical storage fault was injected. Exact solutions establish feasibility, not fun. The original engine also checks1,543 alternate legal starts;53 correctly require undo. Not every legal choice preserves solvability.

## Installed Android and actual ads

The final signed release APK ran on an isolated ARM64 Google APIs Android16/API36 emulator at1080×1920. The [complete interaction report](validation/v1.3/move-power-ui-smoke.txt) independently verifies the installed base.apk SHA-256 against the delivered **313cee4693293c24d0775ca73bb00fc063e799f523bcb67e82a1bd975c4ba2da**.

The six-power acceptance run uses an **explicit synthetic fixture**: Journey75 access,10,000 local test gold,4 of each power and5 seeded three-star clears for loot eligibility. This is not evidence that75 levels or those5 clears were played. [Fixture and six-file before/after hashes](validation/v1.3/move-power-profile.json) show the original game-owned preferences restored byte for byte. A separate [gallery fixture](validation/v1.3/gallery-profile.json) was also restored. Stable's different package was untouched.

Actual visible touch controls established:

- A present-piece block spends1 without removing a piece; an exit spends1; successful Undo spends1; empty Undo spends0.
- The exhausted board,13-action budget and run identifier survive force-stop/relaunch. Each gold continuation debits exactly100 and adds the disclosed10 actions. Recreating the process repeats neither debit nor extension. Two continuations block a third; free Restart resets the attempt without debit.
- Balanced friend B6 accepts attempts beyond the campaign allowance, has no power/continuation purchase and gives no gold. Unlimited state survives recreation.
- Locked Vision at Journey1 offers no acquisition. All six effects work on the unlocked fixture: one-stock debit, Focus's actual safe-move labels, Trail's exact Step1…5, Shield's three protected blocked attempts across recreation and fourth block costing1, Flow's three actual exits costing3.
- A Shield shop pack adds2 for the displayed70 gold. Milestone loot adds one Spark with no gold, using the declared synthetic five-clear eligibility.
- Entering the continuation consent surface and pressing Back without an earned callback grants0 actions/0 gold.

**Two actual official Google rewarded Test Ads rendered and earned their rewards.** The continuation gave exactly10 actions/quota1 and0 gold, with no repeat after process recreation. The power offer gave Spark×1 and0 gold, persisted after recreation, and offered no continuation bonus. The [actual local SDK journal](validation/v1.3/ad-journal-export.json) contains exactly2 earned events (continuation and hint×1),2 impressions and2 paid callbacks at **USD0 micros / UNKNOWN precision / test=true**. These test values establish no income. The share chooser opened and was cancelled; no recipient, advertising Install link or real checkout was selected. The crash buffer stayed clean.

A separate [non-synthetic play run](validation/v1.3/exact-zero-ui.txt) actually completed Journey1 after four exit/Undo cycles and the five-piece certified route. The final exit used action13 of13 and displayed victory instead of exhaustion: **2 stars,45 earned coins,30 XP; wallet6,935→6,980**. It used no preference edits, power, advertising or purchase. This tests the exact-zero precedence on the delivered Android binary, alongside the broader pure simulations.

The [final 720×1280 and1280×720 run](validation/v1.3/layout-ui.txt) checks all three collection pages, cancelled preview, both event screens, both shop pages, all six power controls and guest account. Clickable controls stay inside the display; the commerce action ends above navigation. The wallet stays unchanged. The emulator's previous size/density overrides and English Home are restored. This is a layout check, not a physical-phone comfort or full accessibility acceptance study.

The final controller audit also checks persistence ordering: flush an existing gold debit before applying its budget, persist the session before acknowledging an ad receipt, and apply a previously earned/paid continuation before replacing or abandoning its pending request. JVM failure models are separate from actual physical storage-fault testing.

## Preserved evidence and limits

The [1.2 collection/event run](validation/lab/collection-events-ui-smoke.txt) browsed all24 cards, checked previews/exact purchase/equip, completed all ten event stages and unique bonuses, and checked six languages. Its [final-binary collection recheck](validation/lab/collection-final-ui-smoke.txt), [guest/account check EN/FR](validation/lab/identity-ui.txt), [ordinary commerce/consent](validation/commerce-ui-smoke.txt), [ordinary +35 test-ad journal](validation/commerce-ad-metrics-smoke.txt) and [720×1280/1280×720 layout run](validation/lab/layout-ui.txt) remain explicitly 1.2 evidence. They are not relabeled fresh1.3 tests.

Stable1.1 [actual play/share/journal](validation/v11-ui-smoke.txt) and [signed1.0→1.1 upgrade](validation/upgrade-results.txt) remain scoped to that separate offline package. Original 1.0 audio-focus and layout logs are historical. Host audio was disabled during headless emulator testing; execution is not subjective listening evidence.

No physical phone/tablet, full TalkBack user session, Bluetooth latency, battery/thermal benchmark or broad API/device matrix was tested. API26 is declared; an earlier API26 emulator connection failed and does not prove compatibility. No live Google sign-in/sign-out/deletion ran because the owner configuration is absent. No Play license-test purchase, server-authoritative wallet, real ad revenue, acquisition cost or retention outcome is established. [Google setup](GOOGLE_SIGN_IN.md) · [Test/production boundary](MONETIZATION_SETUP.md).
