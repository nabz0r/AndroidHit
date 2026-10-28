# Development and architecture

## Current 1.15 destinations, relay route and rooms

Production is **1.15.0-rc1**, Lab **1.15.0-sandbox-rc1**, version code **17**. The four release artifacts and both lint tasks pass using the locally verified JDK17. Release lint reports zero errors and13 warnings per flavor; the new portrait presentation accounts for two orientation advisories. Phone portrait is the qualified layout scope.

`LumaView` remains the native Canvas controller, with dedicated renderers for `adventure`, `home`, `collection_hub`, `atelier` and isolated `play`. `PremiumUi` supplies draw-only surfaces, illustrations and four-destination navigation; it owns no state or hit targets. The controller owns clipped semantic targets and touch dispatch. Modal scrims clear background targets and block board gestures. Item detail retains its caller-aware return path. New copy is in `V115Strings`; existing catalogues and account/provider boundaries remain in place.

`RelayCatalog`/`RelayLayouts` identify the new **U** family independently of historical boards. U uses engine progression version6, campaign content version7 and budget policy5. Piece exits XOR their linked gate mask; matching keys release seals. Manual gate actions are absent. `RelaySolver` is bounded to16 pieces; independent host tests enumerate reachable states and transitions. A legal order can deadlock, rejected taps are free, and repeated Undo refunds committed exits. Three stars represent completion. Saved N campaign content6 retains its board and policy4; explicit historical challenge codes retain their resolvers. A loaded old attempt is preserved until the player starts a new one.

`HomeCatalog` has42 designs and three rooms (`living`, `bedroom`, `garden`). `HomeLayout` uses a10 ×10 grid and two orientations. The strict old6 ×6 record retains its exact coordinates in the living room; other rooms have separate durable records. Permanent ownership is global, with one instance per design per room. `GameStore` keeps ownership/debit atomic and separates purchase, draft, placement and surfaces. `HomeScene` owns a bounded zoom/pan transform shared by painting and hit geometry. Enlarged furniture targets stay centred; painter-order sprite targeting takes precedence, with the outer touch margin accepted as a fallback.

The new host contracts are included in `.github/workflows/puzzle-contracts.yml`, alongside retained N/guide/power contracts. A local rehearsal using only its exact sparse checkout passes; remote CI is a separate publication step.

```sh
sh scripts/test-relay-v115.sh
bash scripts/test-home-v115.sh
bash scripts/test-home-v115-geometry.sh
```

Native observers are separate signed SDK test packages: `scripts/v115-ui-test`, `scripts/v115-relay-ui` and `scripts/v115-upgrade-ui`. Their wrappers require the exact owned AVD, package/version and APK hashes. State changes use actual visible controls and touchscreen input; preference reads independently check persistence. No player progress, wallet or piece history is injected. Keep original failures, signed observer identities and unedited PNGs distinct from later passed scopes. [Gameplay contract](GAMEPLAY_1_15.md) · [Release](RELEASE_1_15.md) · [Qualification](validation/v1.15/README.md).

## Preserved 1.13 room and first-touch architecture

`HomeCatalog` defines twelve original furnishings and separate four-choice wall/floor collections, all using existing LumaCoins. `HomeLayout` is immutable and Android-independent: a six-by-six room, integer furniture coordinates, two rotations and a persisted companion cell. Solid footprints cannot overlap or cover the companion; the two-by-two rug is traversable and may underlay furniture. Four-neighbour breadth-first search rejects unreachable companion destinations.

`GameStore` owns the durable boundary in the existing `luma_drift_save_v1` preferences. Furniture/surface debit and ownership commit together through `LocalSaveTransactions.LOCK` and `write`; all house reads flush exact pending writes. A single complete layout record saves placement and companion position. Missing home keys return the free starter layout without a migration write or wallet reset. Refused mutations do not partially modify durable state. A retry flushes the original transaction instead of spending twice. Purchase, placement, storage and equipped surface are separate actions.

`HomeScene` draws the room and returns actual tile/object hit geometry. `LumaView` owns selection, draft placement/rotation, catalogue/details and accessible actions; the shared `CompanionPortrait` supplies actual equipped clothing and gear. Movement persists the valid destination before acknowledgement, then animates its safe path. Cuddle, rest and lamp/activity poses are transient; they are not ownership, need or reward state. Confirmed room state, not a placement preview or temporary pose, is restored across a cold process.

`FirstLight` uses a real three-piece `Board`. Only its local exit history and finished/skip marker are saved; it never awards a campaign win, LumaCoins or inventory. Existing progressed/tutorial profiles are not forced through it. The regular seven optional lessons remain separate and retain their existing once-only reward. Source tests invoke the actual board and store.

The RC2 replacement corrects a reproduced RC1 tutorial Later→Resume crash: obsolete feedback timing must be cleared when practice state changes, and terminal/skipped lesson state must never request an empty tutorial copy key. RC1 native smoke passes did not exercise that path; the failing whole run and corrected candidate retain separate evidence.

Run `bash scripts/test-home-v113.sh` and `bash scripts/test-first-light-v113.sh`, then the retained store, tutorial and engine regressions appropriate to the change. The new native runner is `scripts/v113-ui-test`; its protocol records exact APKs, genuine earned funding, profile restoration, distinct cold processes and original capture files. [Home rules](HOME_1_13.md) · [Qualification](validation/v1.13/README.md).

Current source declares production **1.13.0-rc2** and Lab **1.13.0-sandbox-rc2**, code **14**. Build with JDK21 for the pinned lint analyzer; Java17 source remains supported. The sections below retain earlier implementation details and dated test scope.

## Preserved 1.12 UI and transaction boundary

`LumaView` adds ordinary `item` pages with measured semantic text, explicit purchase/Equip and caller-aware Close/Back. Item kind/identity, long-page height and return history survive Activity recreation. Displayed premium balance is sampled on UI refresh/resume/auth changes, outside the frame loop; `ProductionWalletClient.cachedBalance` validates the active UID, exact integer fields, version and range, and never supplies spend authorization.

`ShopIllustrations` is draw-only; shared `SweetUi` APIs retain their bounds. `CommerceArt`/`CommerceCopy` style the native wallet and retain provider-supplied prices. `GameStore.purchaseBooster` now tracks its exact coin/stock write through `LocalSaveTransactions`, under the existing lock. IDs, prices, payouts, stock limits and rules are unchanged.

Run `python3 scripts/test-commerce-v112.py`, `python3 scripts/test-v112-copy.py`, `bash scripts/test-store-booster-v112.sh`, and the preserved art/store checks. The new native runners live in `scripts/v112-ui-test` and `scripts/v112-commerce-ui`; historical 1.11 harness/results are not rewritten. [Acceptance](validation/v1.12/README.md).

## Preserved 1.11 bug fixes

`CompanionPortrait` adjusts composition within its existing `2r` extent. `WorldBackdrop` reuses a selected-theme HSV wash on the garden and after the animated play scene. `LumaView` returns to the actual equipped portrait, names worn slots and handles failed/obsolete equipment callbacks. `GameStore` settles exact pending transactions before ownership/loadout reads and tracks first-profile initialization without throwing from its constructor. `V111Strings` supplies four feedback/accessibility keys in six languages. IDs, prices, effects and saved puzzle models are unchanged.

Run `test-store-appearance-v111.sh`, `test-store-init-v111.sh` and `test-store-externals-v111.sh`. Renderer/copy runners are in `validation/v1.11/avatar-bugs/`; native instrumentation is `scripts/v111-ui-test/`. Its approved devices are explicit, purchase budgets come from real clears, exact APK hashes are checked and retained profiles are restored. Actual art comparisons and separate cold processes replace reliance on stored IDs alone. [Qualification](validation/v1.11/README.md).


## Preserved 1.10 additions

`CompanionPortrait` composes the selected character/style with four clothes and three equipment slots. `CollectionGoal` reads one explicitly pinned item. `ObjectiveDashboard` reads earned, unclaimed actions across the existing local stores. `PrismStory` supplies a read-only ten-chapter narrative from actual Journey clears and claimed guardian receipts. `V110Strings` and `PrismStoryStrings` cover six languages. Native dialogs share the pastel game palette.

GameStore uses the existing typed durable transaction boundary for event completion bonuses, automatic gear grants, character/theme/gear purchases and equipment. Keys, prices, rewards and ownership rules are preserved. An incomplete local write blocks acknowledgment and retries the original operation. No remote server or payment activation is introduced.

Run `bash scripts/test-objective-models-v110.sh`, `bash scripts/test-prism-story-v110.sh`, and the two renderer runners under `docs/validation/v1.10/avatar/`. The native framework is `scripts/v110-ui-test/`; it requires the named owned emulator and exact installed APK, never seeds player progress, and records original screenshots. Retained-profile tests restore original preferences/settings; first-install tests retain a genuinely new guest profile. [UX contract](UX_1_10.md) · [Qualification index](validation/v1.10/README.md).

## Historical1.9 additions

`GuardianCatalog`/`GuardianEncounter` isolate optional layered/cellwise puzzles from published Board/L/M/T rules. `JourneyExperience` grades real action budgets. `AvatarStyles`/`AvatarStage` draw the 288 combinations; `V19Strings` keeps new copy in all six languages. GameStore retains explicit versioned receipts for samples, gifts and guardian coins, plus a free style preference. [Audit](PLAYER_AUDIT_2026.md) · [Guardian design](GUARDIANS.md) · [Executed checks](validation/v1.9/README.md).

Run the dedicated scripts `test-new-player-v19.sh`, `test-guardian-v19.sh`, `test-guardian-store-v19.sh` and `test-journey-rewards-v19.sh`. Native instrumentation lives in `scripts/v19-ui-test/`; it targets the named QA emulator, checks exact APK bytes and restores original preferences/display settings. Earlier 1.8 scripts/reports retain their scope.


Preserved 1.11 source declared **production 1.11.0-rc2** and **Lab 1.11.0-sandbox-rc2**, code **12**. [Qualification and exact-byte evidence](validation/v1.11/README.md) · [V4 engine](PROGRESSION_V4.md) · [Tutorial](TUTORIAL.md) · [Preserved 1.10 UX contract](UX_1_10.md). Build with JDK21 for the pinned lint analyzer; Java17 source remains supported.

The component/build details below retain their earlier historical references; current content uses StrategyCatalog L/M/T, content save5 and reversible budget policy3. Current 1.7 policy2 saves migrate with their exact old layout/base; historical codes are unchanged. New exact guidance runs off the UI thread on one worker with one coalesced queued request.

## Components

| Area | Responsibility |
|---|---|
| `engine/` | Pure Java boards, typed exits/gate openings, bounded exact V4 planner; frozen 1,000 L + 240 M rows, finite T event closure and all historical identities |
| `onboarding/` | Seven pure-Java practice lessons, validated phase/history save and six-language contextual instructions |
| `data/` | Local coins/inventory/progress/events/settings, six-language copy, XP/missions, 18 items across three equipment slots, durable power-use receipts and optional local play journal |
| `LumaView` / `MainActivity` / `ui/` | Canvas game, full saga/briefing, tutorial, pastel presentation, native virtual controls, matching directional drags, local session persistence and wallet recovery |
| `commerce/` | Three guarded rewarded placements, UMP, real Billing offer/owned-purchase adapter, isolated Lab simulator and production wallet UI |
| `wallet/` | HTTPS authenticated client, account-scoped permanent cosmetic cache, foreground owned-purchase recovery |
| `identity/` | Explicit Credential Manager → Google → Firebase Auth login, guarded session snapshots/foreground refresh, optional restoration and account deletion |
| `sharing/` | Board cards and explicit local-journal export through a grant-scoped read-only provider |
| `audio/` | Original PCM synthesis, SoundPool effects, AudioTrack music and focus/lifecycle handling |
| `server/` | Firebase/Play verification, Firestore ledger, permanent cosmetic spending, refunds/retries/deletion, HTTP boundaries and deployment preparation |
| `public/` | Static FR/EN developer/privacy/deletion pages, configurable external Google deletion and app-ads.txt |

The Android app includes no Firebase Analytics, Firestore client or gameplay cloud-save service. The server uses Firestore only for purchased value, ownership and its operational records. [Privacy/data inventory](PRIVACY.md).

## Build environment and channels

Java 17 source compatibility, Gradle wrapper 8.14.3, AGP 8.11.0, compile/target SDK 36 and minimum Android API 26. The final 1.5 release/lint run used **JDK 21.0.13**; pure engine/data suites also run on JDK 17. Set `JAVA_HOME` and `ANDROID_HOME`, or add untracked `local.properties` containing the local `sdk.dir`. No credentials belong in these public instructions.

```sh
./gradlew assembleProductionDebug
./gradlew assembleProductionRelease bundleProductionRelease lintProductionRelease
./gradlew assembleLabRelease bundleLabRelease lintLabRelease
```

The `channel` dimension selects `production` (`com.androidhit.lumadrift`, label Luma Drift) or `lab` (`.sandbox`, label Luma Drift Lab). `BuildConfig.MODE_SANDBOX` controls simulation boundaries. Production includes its own public Firebase configuration asset; Lab does not. Both delivered variants use sample ad creatives. The production manifest has the owner's AdMob app ID and the controller selects the three actual placement IDs only in an explicit qualified live build.

After deployment/qualification, `-PlumaWalletUrl=https://REVIEWED_ORIGIN` supplies the wallet origin and `-PlumaLiveAds=true` disables test-unit forcing for production. The normal candidate has an empty origin; its real purchase launch remains blocked. Build settings alone do not prove Play/AdMob approval or actual fulfillment. [Activation report](validation/v1.4/production-readiness.json).

Lint uses standalone 9.4.0 through `android.experimental.lint.version`, an experimental tooling override required by the SDK metadata. The preserved **1.5** production and Lab reports have **0 errors and 6 warnings each**: three intentional small synchronous preference commits, two update advisories and one `SetTextI18n` notice. Those counts are historical; the integrated 1.6 run needs its own recorded result. [Executed 1.5 build](validation/v1.5/android-build.txt).

## Signing and collecting artifacts

Private signing settings remain in ignored `signing.properties` and `.signing/`. Preserve the existing release/upload identity for updates. Never publish a keystore, password or service-account private key. Without signing configuration a release output must be signed before installation; the build does not silently substitute an Android debug key.

```properties
storeFile=.signing/YOUR_PRIVATE_KEYSTORE.jks
storePassword=LOCAL_SECRET
keyAlias=YOUR_ALIAS
keyPassword=LOCAL_SECRET
```

After both flavors are built and verified:

```sh
python3 scripts/package-release.py --variant lab
python3 scripts/package-release.py --variant production
python3 scripts/check-production-config.py
```

Collection reads the actual output metadata and generated BuildConfig, checks channel/package/version, records source-input hashes and refuses to overwrite an already versioned artifact with different bytes. Run production last to keep it as the current manifest. Historical artifacts remain in `releases/`. Final checks include signature, bundle validation, 16 KiB structural alignment, actual install/launch and the installed base APK hash. [Evidence](VALIDATION.md).

`check-production-config.py --require-live` refuses the current candidate. `--evidence config/activation.example.json` supplies explicit human-reviewed qualification records; leave them false until the named actual-service tests pass and reference their report. This records reviewed evidence; it does not query Google approvals or turn an invented URL into a deployment.

## Tests

```sh
scripts/test-strategy-v4.sh
scripts/test-tutorial-v18.sh
scripts/test-engine.sh
scripts/test-progression-v3.sh
scripts/test-data.sh
scripts/test-meta.sh
scripts/test-economy.sh
scripts/test-collection-events.sh
scripts/test-difficulty.sh
scripts/test-move-budget.sh
scripts/test-sealed.sh
scripts/test-inventory-presentation.sh
python3 scripts/test-identity-session.py
scripts/test-audio.sh
scripts/test-billing-policy.sh
scripts/test-wallet-recovery.sh
./gradlew compileLabReleaseJavaWithJavac
python3 scripts/test-commerce-receipts.py --variant lab
python3 scripts/smoke-production.py --serial emulator-5580
```

The UI script refuses phone serials and does not reset application data or select a Google account. Its recorded fresh-game run completes Journey 1 after normalizing language; reuse with a save already beyond that board requires choosing the intended board explicitly. Never reset a physical device for a test. Inspect captures and the crash buffer; compilation is not runtime validation.

The JVM Android/storage/provider adapters are explicit test doubles and are not packaged. They model disk failure, callbacks and accounts, not actual Android flash faults or Google receipts. The server's independent **real Firestore emulator** test exercises its production database transaction adapter/rules; Firebase identity and Play payments still need owner-account qualification. [Server tests](../server/TESTING.md), [web tests](PUBLIC_PAGES.md).

`test-economy.sh` includes the power input/reservation model and avatar equipment/first-victory receipts. Installed Flow interruptions and equipment controls have separate [1.5 runtime evidence](VALIDATION.md#installed-android-acceptance); a JVM pass alone does not establish those UI behaviors.

## Persistence and value boundaries

Local SharedPreferences retain earned coins, stars/unlocks, settings, inventory, XP, equipment ownership/loadout and local calendar/event claims. A session snapshot stores board identity/history, content/budget versions, action allowance, run/request IDs, Shield/Charm protection, timed-preview remainder, Flow steps and applied continuation state. New runs use **content 5 / policy 3** and L/M/T; content4 J/K remains restorable. The following historical accounting descriptions refer to policies1/2, not new refunded Undo. Valid content-3 attempts restore the exact G/H layout and original policy-2 allowance through `getV2()` / `remixV2()`; content-2 attempts retain B/Q and their saved policy. `Level.progressionVersion` (3 for J/K, 2 for G/H, 0 otherwise) selects the correct frozen margin independently of the UI save schema. Unsupported session formats reset with a notice while earned progress/economy remain at numbered slots. C/R/B/Q/G/H/P/E friend identities are preserved by `ChallengeCode.resolve()`. Date/clock/save data are device-controlled and never authorize paid gold. [Content compatibility and host checks](PROGRESSION_V3.md).

Present-piece taps, including unprotected blocked attempts, and successful Undo consume actions; every J/K progression board has a certified N-exit solution within its fixed N+3…5 budget. G/H retains N+2…4. Earned-coin/ad continuations share a maximum of two, cost 100 local earned coins or the announced rewarded offer and add `max(5, remaining pieces+3)`. Free restart remains. Six local powers unlock at Journey 1/5/12/25/45/75; every five distinct Journey clears can claim one deterministic eligible power. Activation requires confirmation, rejects active/overlapping use, reserves stock and durably saves the effect before committing the one-unit receipt. Interrupted Flow resumes its saved remaining exits. Local power prices use the bounded progression curve and equipped Aura discount; paid cosmetics use fixed server catalog prices.

V3 authoring is offline: `scripts/generate-progression-v3.sh` reuses frozen G/H positions/solutions, explicitly stores sparse seals and the neutral-switch Journey-12 lesson, and writes `ProgressionV3Layouts.java`. Phones only parse/cache those rows. `BalancedCatalog.info()` returns cached metadata without solver work. `scripts/test-progression-v3.sh` compares independent exact reachability, all 1,240 certificates, save prefixes, geometric uniqueness and the pre-change historical fingerprints. Its [582,831-check report](validation/v1.6/progression-tests.txt) and [byte-identical regeneration](validation/v1.6/progression-reproduction.txt) are host-side evidence, not device performance. [Full method](PROGRESSION_V3.md).

Equipment has one Head, Aura and Charm slot. Caps are +3 first-victory coins, −5 coins per power pack and one protected blocked attempt per run. Acquisitions, valid loadout and first-victory bonus receipts are local; P/friend practice grants no economic, XP or mission reward and has no consumable powers. Companion/theme identity itself remains cosmetic. [Exact table and persistence](AVATAR_EQUIPMENT.md).

Purchased gold never enters `GameStore.coins`. `GameStore.ExternalOwnership` supplies account-only permanent cosmetics without persisting them as local ownership flags. Sign-out removes that source; already locally earned cosmetics remain. The UID-bound cache is not trusted by the server. The server independently verifies tokens, prices, balance and uniqueness, including concurrent requests/refunds. [Android wallet contract](WALLET_ANDROID.md), [server ledger](../server/README.md).

Foreground recovery contacts Billing/wallet only for an existing Google session with a configured server origin. It never launches a payment or displays an ad. Pause, destruction or account switches invalidate callbacks. The explicit wallet screen gives retry/error controls; guest gameplay remains independent.

## Identity, privacy and deployment

The owner public configuration in `app/src/production/assets/identity-config.json` is a client identifier set, not a private key. The ignored main-source identity file remains available for local development; keep Lab/test identity in a separate Firebase project. Register the actual Play app-signing fingerprints in addition to the direct-release fingerprints. [Google setup](GOOGLE_SIGN_IN.md).

The account screen deletes via the wallet API when its origin is configured, requiring recent authentication; it does not fall back to Firebase-only deletion on backend failure. The server handles durable deletion/retry and limited audit retention. The external deletion pages are configured and published on the existing Firebase Hosting site; successful Web sign-in/deletion is owner-reported, while Android identity and the remaining provider/commercial cases retain their separate qualification limits. This account flow does not synchronize gameplay or equipment. [Pages and observed deployment scope](PUBLIC_PAGES.md).

Use managed Cloud Run credentials, deny direct client Firestore access and qualify RTDN/maintenance/IAM before enabling checkout. Existing Hosting/Cloud Run deployment observations are recorded separately; compiling the app does not establish authenticated wallet behavior, real purchases or service approval. [Deployment runbook](../server/DEPLOYMENT.md) · [Observed Hosting scope](validation/hosting-preview.md).

The AGPL-3.0 project license is preserved for original code/art/audio. Google SDKs, branding and other dependencies retain their own terms. [Third-party notices](THIRD_PARTY_NOTICES.md). Publication/SDK approval and a commercial hit are not established by a build or simulation.

## Identified 1.6 outputs

Use `python3 scripts/verify-v16-binaries.py --build-log docs/validation/v1.6/release-build.txt` after the four release outputs are complete and application inputs are frozen. It checks signatures, pinned bundletool schema, native/ZIP alignment and source/artifact stability. UI runs: `scripts/smoke-v16.py` (explicit emulator 5580), `scripts/smoke-powers-v16.py` (explicit Lab synthetic profile, mandatory backup/restore), and `scripts/check-audio-runtime.py` (mandatory expected APK hash). Read each CLI/help and [actual qualification](VALIDATION_1_6.md) before running; host and provider models do not replace real Android checks. Earlier smoke scripts keep their release-specific semantics.
