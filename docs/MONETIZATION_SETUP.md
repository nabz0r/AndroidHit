# Luma Drift Lab: monetization tests and later activation

**1.2.0-sandbox · Version code 3 · Test only · No configured owner AdMob or Play accounts · 26 October 2026**

The signed **Luma Drift 1.1.0 offline APK** is preserved. Monetization preparation belongs to a separate **Luma Drift Lab** app:

| Property | Stable game | Monetization Lab |
| --- | --- | --- |
| Version | 1.1.0 | 1.2.0-sandbox, code 3 |
| Android application ID | `com.androidhit.lumadrift` | `com.androidhit.lumadrift.sandbox` |
| Android label | Luma Drift | Luma Drift Lab |
| Wallet/save | Stable private app data | Separate private app data and simulated wallet |
| Real-money checkout | Absent | Disabled; local simulator only |

Both use the Java namespace `com.androidhit.lumadrift`; the distinct Android application ID provides separate installation/data boundaries. The Lab can coexist with the stable game and does not upgrade its wallet. This is not a production monetization release. The [research note](MONETIZATION_RESEARCH.md) supplies sources; [Validation](VALIDATION.md) records actual checks.

## Four different things called “test”

| Mode | What actually happens | What it proves |
| --- | --- | --- |
| Local purchase simulator | Native, clearly labelled no-charge controls simulate one pack at a time: cancel, pending or purchased. | Local wallet rules and idempotent simulated delivery. It is not a Google receipt. |
| Google test advertising | The real SDK requests Google's test creative after explicit adult opt-in, UMP eligibility and the load button. | SDK/network/rendering integration when it actually succeeds. No real revenue. |
| Billing SDK connection/query | Billing 9.1 connects and queries product details; actual checkout remains disabled. | Connection/API behavior, including unavailable Play/products. No purchase proof. |
| Play license-test purchase — future | An actual Play sheet uses configured products, a license tester and a test payment instrument. | Purchase lifecycle in that configuration. Not available with the owner's current setup. |

A simulation must never display a fake Google purchase sheet, a fabricated receipt, a fabricated consent certificate or revenue presented as real. The local sandbox cannot test a server it is not connected to. Account-dependent tests stay marked pending until their actual evidence exists.

## Use the account-free Lab

The puzzle remains usable without advertising or purchasing. Lab monetization screens identify test mode. The simulator uses local identifiers and never opens a fake Google payment sheet. Pending and cancelled simulations grant no gold; a completed simulation grants its selected pack once. This is test state inside the separate Lab application, with no transfer into stable or future purchased currency.

Advertising requires the player to declare an adult state and explicitly enable contact with Google. An unknown age or minor state blocks that route. This declaration is not identity verification. UMP then determines SDK request eligibility. No ad loads automatically: the player must use the load control, then choose the rewarded action when available. Unavailability, refusal and form/network errors leave the game usable.

For a real advertising SDK smoke test, Google's documented sample identifiers are:

| Purpose | Google sample value |
| --- | --- |
| AdMob app | `ca-app-pub-3940256099942544~3347511713` |
| Rewarded ad unit | `ca-app-pub-3940256099942544/5224354917` |

Use test creatives throughout development. An available test creative does not demonstrate an owner-configured UMP form, ad account approval or production demand. Sample IDs do not create the owner's AdMob configuration. [Google rewarded-ad testing instructions](https://developers.google.com/admob/android/next-gen/rewarded)

The Lab can record ad impression and paid-event callbacks in its opt-in local journal. It adds no Firebase Analytics or remote gameplay analytics, advertising-campaign attribution backend or automatic journal export. This does not make Google SDK traffic local: after opt-in the advertising route can contact Google and process SDK data under the applicable choices. Record separately whether a real SDK test ad appeared, a service was unavailable or an action was cancelled. A successful load request alone earns nothing.

## Later: configure AdMob and consent

1. Register the exact application ID used for the intended release. The present Lab is `com.androidhit.lumadrift.sandbox`; the stable game is `com.androidhit.lumadrift`. Create the matching AdMob app and rewarded unit. Do not assume one package's configuration qualifies the other.
2. Publish a privacy policy that describes the exact SDK, purposes, data recipients, player controls and support route. Configure the applicable messages in AdMob's Privacy & messaging area, including the policy link and appropriate vendors/languages.
3. Configure and test the European regulations message using UMP. Enable applicable US state messaging and honor its supported signals. Do not treat a European message as covering every country automatically.
4. Decide the intended audience and age handling. Set UMP's under-age parameter and advertising age treatment separately. A child flag is not an age-verification service.
5. For advertising-enabled launches, update consent, present required forms and provide UMP privacy options when required. Keep SDK initialization behind the local adult/opt-in gate and UMP eligibility. A refusal must leave normal play available.
6. Exercise fresh consent, refusal, later withdrawal, stale consent, form failure and offline startup on registered test devices. Use UMP debug geography only in test builds; do not ship forced test geography or routine consent resets.

The message has to exist in the owner's AdMob account before its account-specific integration can be qualified. A sample app ID does not prove that this owner-configured flow was tested. If UMP cannot establish eligibility, ad requests stay blocked. [UMP setup and testing](https://developers.google.com/admob/android/privacy), [EEA configuration](https://developers.google.com/admob/android/next-gen/privacy/gdpr), [US state configuration](https://developers.google.com/admob/android/next-gen/privacy/us-states)

Re-audit the merged manifest and the exact chosen SDK's data disclosure. Complete the Play advertising-ID declaration and Data safety form from observed app behavior and vendor documentation. A consent UI screenshot alone cannot establish compliance or absence of pre-consent network traffic. [Advertising ID requirements](https://support.google.com/googleplay/android-developer/answer/6048248?hl=en)

## Later: create the gold products

Use three consumable one-time product definitions, with fixed quantities:

| Proposed product ID | Gold per unit | Lab behavior now / future price source |
| --- | ---: | --- |
| `luma_gold_500` | 500 | No-charge simulation / active Play product details |
| `luma_gold_1400` | 1,400 | No-charge simulation / active Play product details |
| `luma_gold_3600` | 3,600 | No-charge simulation / active Play product details |

These identifiers are the proposed account-configuration contract. Before activation, confirm that app, backend and Play Console use exactly the same IDs. Do not silently rename a product that has already been sold. The owner selects base prices, territories and taxes in Play Console; this guide invents no production price or discount.

The Lab adapter uses Billing **9.1.0** for real SDK connection and queries, but intentionally refuses to launch real checkout. The current account-free setup has no active Play products to prove a purchase. A future enabled route must display returned, localized product prices and handle missing offers gracefully. The simulator must not present an invented price as a Play price. [Billing integration](https://developer.android.com/google/play/billing/integrate)

Prepare an internal Play test configuration with license testers. Check that the purchase sheet identifies a test purchase. An internal test track by itself does not prevent charges: a tester using a normal payment method can create a real purchase. Use the documented test instruments for approval, decline and delayed completion. [Play Billing testing](https://developer.android.com/google/play/billing/test)

## Future dependency: a production wallet service

No production backend is included or deployed by the Lab. The following is a separate future dependency before trustworthy real sales:

1. Bind an authenticated player identity to an authoritative wallet. Define account recovery and device changes before promising recoverable purchased currency.
2. Send the purchase token and expected product to the service over HTTPS. Independently query Google with server-held credentials; check package, product, purchase state and quantity against the product allowlist.
3. Use the token as a unique fulfillment key. In one durable transaction, record verified ownership and credit the correct gold amount exactly once. Replaying the same callback returns the original outcome.
4. Consume the verified consumable after durable delivery, and retry consumption safely. A retry must never add gold again. Keep uncompleted work recoverable across app or server restarts.
5. Reconcile refunds, revoked purchases and chargebacks. Store an auditable reversal separately from the original grant, with a defined policy for already-spent gold.
6. Make wallet spending authoritative too. A server that verifies purchases but trusts an arbitrary client-provided balance is not a secure paid economy.

Google recommends moving purchase verification and sensitive fulfillment logic to a backend and explains the acknowledgement deadline. [Billing security](https://developer.android.com/google/play/billing/security)

Configure Real-time Developer Notifications with the owner's Pub/Sub setup, opt in to one-time-product notifications, authenticate delivery and use notifications to trigger a fresh Google state lookup. Reconcile missed work, including voided purchases. A notification is not a reason to credit arbitrary client quantities. [RTDN reference](https://developer.android.com/google/play/billing/rtdn-reference), [One-time purchase lifecycle](https://developer.android.com/google/play/billing/lifecycle/one-time), [Voided Purchases](https://developers.google.com/android-publisher/api-ref/rest/v3/purchases.voidedpurchases)

Keep service-account keys, backend signing secrets, private app-signing keys and store credentials out of the APK and Git. Use server secret storage with limited permissions. Public app IDs and product IDs are configuration, not secrets. Never log complete purchase tokens into public diagnostics or screenshots.

## Lab acceptance checks

| Scenario | Required Lab result |
| --- | --- |
| Stable and Lab installed together | Separate saves/wallets; no test-gold transfer. |
| Unknown or minor age; advertising disabled | No advertising request; game still usable. |
| Adult opt-in but no load action | No automatic ad preload. |
| UMP unavailable or request not eligible | Clear unavailable state; no unauthorized ad request. |
| Test ad closes or fails before reward | No fabricated reward. |
| Earned-reward callback repeats | One reward for that opportunity. |
| Simulator cancel or pending | No gold. |
| Simulator purchased repeats | One local grant for that simulated transaction. |
| Real Billing checkout requested | Refused; no real payment sheet launched. |
| Optional journal disabled | No app journal measurement; Google SDK behavior remains a separate disclosure. |

These are acceptance requirements, not a list of assumed passes. See the validation report for executed evidence.

## Future live-service acceptance matrix

| Scenario | Required result |
| --- | --- |
| Decline advertising or lose connectivity | Puzzle progression remains usable; no unauthorized ad request. |
| Close or fail an ad before earning | No fabricated reward; the player can retry later. |
| User cancels checkout | No gold, no alarming payment-failure claim. |
| Purchase remains pending | Pending status; zero gold until verified completion. |
| Completion arrives while app is closed | Reconciliation delivers once on recovery. |
| Duplicate token or concurrent callback | One grant, stable wallet total. |
| Crash after wallet credit | Retry consumes/reconciles without another credit. |
| Invalid product, token or quantity | No entitlement; clear recoverable error. |
| Refund or chargeback | Reconciled ledger reversal and support-visible outcome. |
| Reinstall or second device | Recover the verified account wallet according to documented account rules. |
| Lab versus production data | Simulated gold never becomes purchased gold. |
| Long progression | Item-price ceiling, affordability and integer bounds stay valid. |

Run the matrix at the appropriate layer. Local automated checks cover deterministic rules; Google SDK tests cover device integration; Play license tests cover the purchase lifecycle; server tests cover durable fulfillment. Passing one layer does not imply the others passed.

## Enable production only with evidence

Before real traffic, replace sample configuration with the owner's approved setup, complete privacy/store declarations, qualify the wallet service, remove simulation controls from the production route and publish a fresh validation report. Keep a way to disable new offers if a service fails while preserving recovery of existing purchases.

No step here authorizes an actual test charge, store submission or live ad experiment without its account configuration and a clearly identified release. The requested deliverable remains test-only until the owner explicitly changes that scope.

## Recorded test result

The installed final Lab APK passed the [simulated-pack and consent run](validation/commerce-ui-smoke.txt) and [real Google sample-ad metrics run](validation/commerce-ad-metrics-smoke.txt). One sample ad produced one impression, one 35-gold reward and a paid callback with USD 0 micros / UNKNOWN precision / test=true. The latter is a test diagnostic, not income. The [actual device-local JSON](validation/lab-ad-journal-export.json) contains no identity or advertising ID. The journal and staged export cache were cleared after capture, observation/ads were disabled and age returned to unspecified. These results do not qualify owner-specific AdMob messaging, Google login or Play license-test purchases.
