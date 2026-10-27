# Monetization: evidence and design decisions

> Historical research before Lab 1.2, dated 26 October 2026. Future-work/configuration statements below describe that decision point; owner IDs, deployed services and implemented 1.5 adapters are documented in [current setup](MONETIZATION_SETUP.md). Research is not provider or revenue qualification.


**Research date: 26 October 2026 · Primary sources only · Luma Drift Lab 1.2.0-sandbox · No real payments**

This note records the research completed before changing monetization behavior. No production AdMob or Google Play configuration has been supplied and requested test mode. The resulting architecture preserves the signed **1.1.0 offline game** and prepares a separate **Luma Drift Lab 1.2.0-sandbox**, version code 3, application ID `com.androidhit.lumadrift.sandbox`. Its private save and wallet are isolated by Android from the stable `com.androidhit.lumadrift` application. Simulated gold cannot become a paid entitlement in the stable game. See the [test and activation guide](MONETIZATION_SETUP.md).

## Scope of the Lab

The Lab combines two deliberately different routes: real Google SDK test advertising after explicit adult opt-in and UMP eligibility, and clearly labelled local purchase simulations. Ads load only after the player presses the load control. Unknown-age and minor states block advertising. Billing 9.1 queries can exercise the SDK connection, but launching an actual Play checkout fails closed. There is no account-bound Play purchase evidence without configured products and license testers.

Advertising impression and paid-event callbacks can enter the optional **local** journal. No Firebase analytics, remote gameplay reporting, acquisition-attribution service or production wallet backend is included. Google's advertising SDK itself can communicate with Google after opt-in; “local journal” does not mean “the advertising SDK sends nothing.” Exact runtime results belong in the validation report, not in this research note.

## What we can conclude

| Area | Evidence-backed constraint | Luma Drift design decision |
| --- | --- | --- |
| Rewarded ads | The player must choose the ad and understand the reward. | Adult opt-in, explicit test-ad load, then a voluntary rewarded action; no automatic preload on launch. |
| Consent | Advertising choices require a proper consent flow and later access to privacy options where required. | Local opt-in precedes Google contact; UMP gates requests, and the game remains usable after refusal or failure. |
| Paid gold | Play purchases need purchase-state handling and verified, idempotent delivery. | Simulate three packs in an isolated Lab app; keep real checkout disabled. |
| Item economy | No official source reviewed establishes a universal logarithmic pricing rule. | Use a documented, bounded logarithmic curve as a design hypothesis, then simulate and play-test it. |
| Success | A valid integration does not establish retention, ad demand or profitability. | Report actual observations and keep revenue assumptions separate. |

The right-hand column contains project choices, not claims that Google mandates these exact placements or prices. The age selection limits this Lab; it does not verify identity or age.

## Rewarded advertising without interrupting a puzzle

Google's rewarded-ad rules require an affirmative choice, a clear explanation of the action and reward, and delivery of the promised benefit when earned. Skipping or declining must not obstruct normal app use. Rewards can be internal, non-transferable game goods; cash and equivalent direct monetary rewards are prohibited. [Rewarded inventory policy](https://support.google.com/admanager/answer/7496282?hl=en)

For this game, an optional shop placement is a better starting hypothesis than surprise interruptions during a board. Google recommends matching the reward to the player's context and testing its size against the rest of the economy. Its promotional case studies are examples, not expected results for Luma Drift. [AdMob rewarded-ad playbook](https://admob.google.com/home/resources/rewarded-ads-playbook/)

The chosen starting scope excludes forced interstitials, randomized rewards and a payment requirement to finish puzzles. That limits the first experiment's moving parts and protects the core game loop.

## Consent is more than an advertising toggle

Google requires a certified consent management platform integrated with TCF for personalized advertising in the EEA, UK and Switzerland. UMP is Google's app implementation; a homemade accept button is not a replacement for that configured message. [Google CMP requirements](https://support.google.com/admob/answer/13554116?hl=en)

The current UMP setup guide specifies version **4.0.0**. On each launch with advertising enabled, request updated consent information, present a form if required, and check `canRequestAds()` before requesting advertising. First-time Google contact in the Lab also requires the local adult opt-in. Use UMP's privacy-options requirement to expose the supported form. Do not infer authorization from a stale app-owned boolean. [UMP integration](https://developers.google.com/admob/android/privacy)

`canRequestAds()` is an SDK eligibility check, not proof of consent to every purpose or personalized advertising. Consent contains purposes and vendors. UMP's under-age flag also does not automatically configure the advertising SDK's age treatment. [EEA consent and age handling](https://developers.google.com/admob/android/next-gen/privacy/gdpr)

Ad requests now have an age-treatment API for children, teenagers and an unspecified age. Audience declarations and age handling must be resolved before public advertising activation; an attractive puzzle should not silently treat every player as an adult. [Current targeting guide](https://developers.google.com/admob/android/next-gen/targeting)

US state privacy choices are an additional configuration concern. UMP can write GPP signals and Google supports restricted data processing; European consent configuration alone is not a worldwide privacy implementation. [US state guidance](https://developers.google.com/admob/android/next-gen/privacy/us-states)

## SDK versions, data and measurement

The current Next-Gen guide uses **GMA Next-Gen 1.6.0**, with minimum Android API 24 and compile SDK 35. It requires background-thread initialization. Consent and relevant request configuration must precede initialization because initialization can trigger preloading. The separately documented `play-services-ads` **25.5.0** is labelled Legacy; its API must not be mixed with Next-Gen examples. [Next-Gen setup](https://developers.google.com/admob/android/next-gen/quick-start), [Legacy setup](https://developers.google.com/admob/android/quick-start)

The advertising ID permission is a manifest permission, not an Android runtime consent dialog. Current legacy SDKs merge it automatically. Advertising identifiers must respect the user's reset/deletion controls; do not substitute a persistent device ID when advertising ID access is unavailable. [Advertising ID policy](https://support.google.com/googleplay/android-developer/answer/6048248?hl=en)

An ad SDK introduces data handling beyond a local play journal. Google's Next-Gen disclosure lists IP address, interactions, diagnostics and device/account identifiers, with TLS in transit. Adding networking or advertising requires a revised privacy statement and Play Data safety answers based on the exact shipped SDK and configuration. Removing advertising ID access does not establish that no data leaves the device. [Next-Gen SDK data disclosure](https://developers.google.com/admob/android/next-gen/privacy/play-data-disclosure)

For ad measurement, the SDK exposes impression callbacks and `onAdPaid` values with currency, micros and precision. Impression-level revenue must be enabled in AdMob. A value can be estimated or unknown; a test callback is not proof of actual income. Local diagnostic events, AdMob reporting, purchase validation and third-party acquisition attribution serve different purposes. [Impression-level revenue](https://developers.google.com/admob/android/next-gen/impression-level-ad-revenue)

## Paid gold and trustworthy fulfillment

The latest Billing release documented on the research date is **9.1.0**, released 18 June 2026. The normal submission deadline for Billing 7 passed on 31 August 2026, with a possible extension through 1 November; Billing 8 remains accepted through 31 August 2027. A fresh integration should target a current supported release rather than depend on an extension. [Billing releases](https://developer.android.com/google/play/billing/release-notes), [Deprecation schedule](https://developer.android.com/google/play/billing/deprecation-faq)

Gold packs are consumable one-time products. Read current product details and display the localized Play price. Process purchase updates and query outstanding purchases when returning to the foreground. `PENDING` does not grant gold. A completed purchase needs verified delivery and consumption; consumption fulfills acknowledgement for a consumable. The standard acknowledgement window is three days from completion. [Billing integration](https://developer.android.com/google/play/billing/integrate)

A future production design needs a backend that validates Google purchase tokens and prevents duplicate fulfillment. That backend is a separate dependency, not included in the Lab or promised as deployed. A local wallet is editable and cannot provide reliable ownership across reinstall or devices. Service credentials stay on the server. Google recommends server verification and acknowledgement/consumption; a token is a better uniqueness key than an order ID. [Billing security guidance](https://developer.android.com/google/play/billing/security)

Cancelled, refunded and charged-back orders need reconciliation after initial delivery. Google exposes the Voided Purchases API with purchase token, reason and quantity information. A successful purchase UI test alone does not cover this lifecycle. [Voided Purchases API](https://developers.google.com/android-publisher/api-ref/rest/v3/purchases.voidedpurchases)

Virtual currency bought in this game stays in this title. Product terms and prices must be clear; paid random-item mechanisms require advance odds disclosure. Fixed, explicit packs avoid introducing a loot-box system into the initial release. [Play payments policy](https://support.google.com/googleplay/android-developer/answer/9858738?hl=en)

## A logarithmic curve is a balancing choice

No official Google source reviewed states that all successful games use logarithmic item prices. A logarithm grows slowly; an exponential curve grows faster. They produce very different player experiences.

The proposed interpretation is a gentle increase in repeatable booster prices as unique-board progression grows, with a stated ceiling. Cosmetic prices and real-money pack contents remain explicit. The curve must not use personal spending, advertising consent, repeated failure or a player's apparent willingness to pay as inputs.

Before activation, compare each curve step with normal board income, mission income and optional ad rewards. Check a free player's time to obtain a booster, early-game affordability, the long-run ceiling and whether buying gold materially changes challenge fairness. Preserve existing inventory and balances during migration. Record the actual implemented formula and samples in the economy documentation; the proposal here is not a claim that a particular formula has already passed play-testing.

## What remains unproven

There are no live monetization results for this project: no measured fill rate, eCPM, conversion, payer retention, acquisition cost or profit. A useful later experiment measures ad offers, opted-in starts, impressions, completions, reward delivery and purchase outcomes alongside retention. Test-mode events must be excluded from business reporting.

Account setup, an owner-configured UMP message, actual Play license-test purchases, a production wallet backend and store review are future milestones. SDK connection attempts and the Lab's native purchase simulator do not establish them. The stable offline APK remains available separately.
