# Development and architecture

Current source declares **production 1.8.0-rc1** and **Lab 1.8.0-sandbox**, code **9**. [Current release and exact bytes](RELEASE_1_8.md) · [V4 engine](PROGRESSION_V4.md) · [Tutorial](TUTORIAL.md) · [UX contract](UX_1_8.md). Build with JDK21 for the pinned lint analyzer; Java17 source remains supported.

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
