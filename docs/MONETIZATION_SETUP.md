# Production and Lab monetization setup

**Current candidate: 1.4.0-rc1 / code 5 · 26 October 2026.** Owner Google/Firebase and three AdMob placement IDs are configured. The server and real Billing adapter are implemented. Cloud deployment, Play products/qualification and public privacy/deletion hosting are still external milestones. [Production scope](PRODUCTION_RELEASE.md) · [Owner console checklist](PRODUCTION_ONBOARDING.md).

## Choose the channel

| | Production candidate | Isolated Lab |
|---|---|---|
| Package | `com.androidhit.lumadrift` | `com.androidhit.lumadrift.sandbox` |
| Version | 1.4.0-rc1, code 5 | 1.4.0-sandbox, code 5 |
| Firebase | Supplied production public client config | No production identity asset |
| Advertising | Owner app/three-unit configuration; sample units forced in delivered build | Google sample app/unit |
| Purchases | Actual Play launch/verification code; blocked with no deployed server origin | Clearly labelled no-charge simulator; no real checkout |
| Currency | Earned local coins separate from account purchased gold | Local simulated/earned coins only |

Production updates the original stable package with the same signing identity. Lab installs alongside it. Never transfer Lab balances into paid gold or upload the `.sandbox` AAB to the production record.

## Build candidate and qualify later

```sh
./gradlew assembleProductionRelease bundleProductionRelease lintProductionRelease
./gradlew assembleLabRelease bundleLabRelease lintLabRelease
python3 scripts/check-production-config.py
```

After the owner deploys and qualifies the wallet, pass its reviewed HTTPS origin with `-PlumaWalletUrl=https://YOUR_SERVICE_ORIGIN`. The client rejects HTTP, embedded credentials, paths/query/fragment and redirects. A production store must successfully fetch the current signed-in Google user's server wallet before enabling a purchase button. An absent/unavailable backend leaves guest play and local coins available, with a clear unavailable state.

`-PlumaLiveAds=true` selects the owner's actual placement units in the production flavor. The delivered candidate does not use that switch. Keep sample creatives or registered Google test devices for development; verify the actual owner UMP messages and approval separately before a new public live build.

## Rewarded placements and privacy

| Placement | Owner unit | Earned local reward |
|---|---|---|
| Gold | `ca-app-pub-6914185503996225/7013317595` | 35 earned coins |
| Continuation | `ca-app-pub-6914185503996225/7504272185` | Exact announced extra actions; no extra ordinary-gold reward |
| Power | `ca-app-pub-6914185503996225/7093208640` | One selected unlocked power |

Owner AdMob app ID: `ca-app-pub-6914185503996225~9670436379`. Test forcing maps all purposes to Google's documented sample unit. Existing adult declaration, explicit opt-in, UMP `canRequestAds`, privacy options and manual-load gates still apply. No advertising action credits the separate purchased wallet. Refusal/unavailability preserves free restart and ordinary play. [Consent/data details](PRIVACY.md).

The native game and continuation/power screens do not force a purchase. Local continuations cost 100 earned coins or one earned rewarded video, with at most two continuations per run. Existing move/power persistence is documented in [the action contract](MOVE_BUDGET_POWERUPS.md). Its 1.3 SDK evidence remains historical; a change in configuration does not establish a live owner ad impression.

## Play products and server

Create `luma_gold_500`, `luma_gold_1400` and `luma_gold_3600` as one-time **Buy** products after publishing an internal release containing Billing. Disable multi-quantity for this initial integration. The owner selects actual prices/countries/tax settings; the app displays Google-returned localized prices and uses the matching ordinary offer token. It does not invent a fallback Play price. [Owner steps](PRODUCTION_ONBOARDING.md#5-create-the-three-gold-products).

The [server](../server/README.md) verifies a token against Play and the SHA-256 Firebase UID binding, credits once in Firestore, consumes after durable delivery and reconciles pending/refunded/voided states. Its catalog independently sets quantities, cosmetic SKUs and prices. Paid gold buys permanent companions/themes through this integration; cloud paid powers/continuations remain disabled. Negative refund debt blocks further cosmetic spending until covered.

The [Android client](WALLET_ANDROID.md) never adds a paid receipt to editable local coins, consumes a receipt locally or trusts a client-provided price. It queries unfinished purchases on signed-in configured foreground sessions, binds callbacks to the active account and restores permanent cosmetic ownership. Cancellation, pending state and an unverified callback do not mint gold.

Follow the [managed-identity deployment runbook](../server/DEPLOYMENT.md); no private service-account key belongs in the app or repository. Internal HTTP routes authenticate distinct Pub/Sub/Scheduler identities, direct Firebase database access is denied, and recovery/deletion jobs are durable. No cloud command has been executed against the owner's project by this preparation.

## Qualification before real traffic

Use Play **license testers** plus the internal track. Track membership alone does not prevent real charges; check Google's test payment instruments. Qualify approved/declined/pending flows, consumption after durable credit, duplicate delivery, process/reinstall/two-device recovery, account isolation, refunds/RTDN, server outage and deletion against the actual deployment. [Official Billing test guidance](https://developer.android.com/google/play/billing/test).

Finish the [public pages](PUBLIC_PAGES.md), owner support/contact details, Firebase Web domain, store Data safety/audience/ads declarations, app-ads.txt and AdMob readiness. Complete the applicable Play closed test/review. Record physical-device and actual-service results separately from local modeling, HTTP doubles, Firestore emulator transactions or a successful APK build.

[Current validation](VALIDATION.md) lists executed checks. The [preserved 1.3 test guide](MONETIZATION_SETUP_1_3.md) describes the earlier Lab implementation and its historical future-work statements; it does not override the current server/client implementation.
