# Development and architecture

Current source builds **production 1.4.0-rc1** and **Lab 1.4.0-sandbox**, both Android version code 5. The signed historical 1.1/1.2/1.3 packages and their reports are preserved. [Current release](RELEASE.md) · [Scope and architecture diagram](PRODUCTION_RELEASE.md) · [Executed checks](VALIDATION.md).

## Components

| Area | Responsibility |
|---|---|
| `engine/` | Pure Java boards, gates, solver, frozen original and measured 480-board order, bounded action ledger, friend codes |
| `data/` | Local coins/inventory/progress/events/settings, six-language copy, XP/missions and optional local play journal |
| `LumaView` / `MainActivity` | Canvas game, lifecycle, accessible virtual controls, effects, local session persistence and signed-in wallet recovery |
| `commerce/` | Three guarded rewarded placements, UMP, real Billing offer/owned-purchase adapter, isolated Lab simulator and production wallet UI |
| `wallet/` | HTTPS authenticated client, account-scoped permanent cosmetic cache, foreground owned-purchase recovery |
| `identity/` | Explicit Credential Manager → Google → Firebase Auth login, optional session restoration and account deletion |
| `sharing/` | Board cards and explicit local-journal export through a grant-scoped read-only provider |
| `audio/` | Original PCM synthesis, SoundPool effects, AudioTrack music and focus/lifecycle handling |
| `server/` | Firebase/Play verification, Firestore ledger, permanent cosmetic spending, refunds/retries/deletion, HTTP boundaries and deployment preparation |
| `public/` | Static FR/EN developer/privacy/deletion pages, configurable external Google deletion and app-ads.txt |

The Android app includes no Firebase Analytics, Firestore client or gameplay cloud-save service. The server uses Firestore only for purchased value, ownership and its operational records. [Privacy/data inventory](PRIVACY.md).

## Build environment and channels

JDK 17, Gradle wrapper 8.14.3, AGP 8.11.0, compile/target SDK 36 and minimum Android API 26. Set `JAVA_HOME` and `ANDROID_HOME`, or add untracked `local.properties` containing the local `sdk.dir`. No credentials belong in these public instructions.

```sh
./gradlew assembleProductionDebug
./gradlew assembleProductionRelease bundleProductionRelease lintProductionRelease
./gradlew assembleLabRelease bundleLabRelease lintLabRelease
```

The `channel` dimension selects `production` (`com.androidhit.lumadrift`, label Luma Drift) or `lab` (`.sandbox`, label Luma Drift Lab). `BuildConfig.MODE_SANDBOX` controls simulation boundaries. Production includes its own public Firebase configuration asset; Lab does not. Both delivered variants use sample ad creatives. The production manifest has the owner's AdMob app ID and the controller selects the three actual placement IDs only in an explicit qualified live build.

After deployment/qualification, `-PlumaWalletUrl=https://REVIEWED_ORIGIN` supplies the wallet origin and `-PlumaLiveAds=true` disables test-unit forcing for production. The normal candidate has an empty origin; its real purchase launch remains blocked. Build settings alone do not prove Play/AdMob approval or actual fulfillment. [Activation report](validation/v1.4/production-readiness.json).

Lint uses standalone 9.4.0 through `android.experimental.lint.version`, an experimental tooling override required by the SDK metadata. The current production and Lab lint runs have **0 errors and 5 warnings each**: three intentional small synchronous preference commits and two update advisories. The release does not claim zero warnings or a tooling upgrade.

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
scripts/test-engine.sh
scripts/test-data.sh
scripts/test-meta.sh
scripts/test-economy.sh
scripts/test-collection-events.sh
scripts/test-difficulty.sh
scripts/test-move-budget.sh
scripts/test-billing-policy.sh
scripts/test-wallet-recovery.sh
./gradlew compileLabReleaseJavaWithJavac
python3 scripts/test-commerce-receipts.py --variant lab
python3 scripts/smoke-production.py --serial emulator-5580
```

The UI script refuses phone serials and does not reset application data or select a Google account. Its recorded fresh-game run completes Journey 1 after normalizing language; reuse with a save already beyond that board requires choosing the intended board explicitly. Never reset a physical device for a test. Inspect captures and the crash buffer; compilation is not runtime validation.

The JVM Android/storage/provider adapters are explicit test doubles and are not packaged. They model disk failure, callbacks and accounts, not actual Android flash faults or Google receipts. The server's independent **real Firestore emulator** test exercises its production database transaction adapter/rules; Firebase identity and Play payments still need owner-account qualification. [Server tests](../server/TESTING.md), [web tests](PUBLIC_PAGES.md#executed-validation).

## Persistence and value boundaries

Local SharedPreferences retain earned coins, stars/unlocks, settings, inventory, XP and local calendar/event claims. A session snapshot stores board identity/history, the action allowance, run/request IDs, Shield charges and applied continuation state. Content version 2 resets an older interrupted board with a notice while retaining earned progress/economy at numbered slots. The old original layout identities remain in C/R/E friend codes; B/Q codes identify balanced layouts. Date/clock/save data are device-controlled and never authorize paid gold.

Present-piece taps, including blocked attempts, and successful Undo consume actions; every board has a certified solution within its initial budget. Local gold/ad continuations share a maximum of two, cost 100 earned coins or the announced rewarded offer and add `max(5, remaining pieces+3)`. Free restart remains. Six local powers unlock at Journey 1/5/12/25/45/75; every five distinct Journey clears can claim one deterministic eligible power. Local power prices use the bounded progression curve; paid cosmetics use fixed server catalog prices.

Purchased gold never enters `GameStore.coins`. `GameStore.ExternalOwnership` supplies account-only permanent cosmetics without persisting them as local ownership flags. Sign-out removes that source; already locally earned cosmetics remain. The UID-bound cache is not trusted by the server. The server independently verifies tokens, prices, balance and uniqueness, including concurrent requests/refunds. [Android wallet contract](WALLET_ANDROID.md), [server ledger](../server/README.md).

Foreground recovery contacts Billing/wallet only for an existing Google session with a configured server origin. It never launches a payment or displays an ad. Pause, destruction or account switches invalidate callbacks. The explicit wallet screen gives retry/error controls; guest gameplay remains independent.

## Identity, privacy and deployment

The owner public configuration in `app/src/production/assets/identity-config.json` is a client identifier set, not a private key. The ignored main-source identity file remains available for local development; keep Lab/test identity in a separate Firebase project. Register the actual Play app-signing fingerprints in addition to the direct-release fingerprints. [Google setup](GOOGLE_SIGN_IN.md).

The account screen deletes via the wallet API when its origin is configured, requiring recent authentication; it does not fall back to Firebase-only deletion on backend failure. The prepared server handles durable deletion/retry and limited audit retention. The external deletion site needs a Firebase Web app, authorized domain, public contact and the deployed API; it is currently disabled. [Pages](PUBLIC_PAGES.md).

Use managed Cloud Run credentials, deny direct client Firestore access and qualify RTDN/maintenance/IAM before enabling checkout. No Docker build, billable deployment, owner Google account operation or live payment was performed here. [Deployment runbook](../server/DEPLOYMENT.md).

The AGPL-3.0 project license is preserved for original code/art/audio. Google SDKs, branding and other dependencies retain their own terms. [Third-party notices](THIRD_PARTY_NOTICES.md). Publication/SDK approval and a commercial hit are not established by a build or simulation.
