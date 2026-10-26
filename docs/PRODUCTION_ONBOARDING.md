# Activate Google sign-in, advertising and gold purchases

**Owner checklist · Official requirements checked 26 October 2026 · Preparation, not an activation report**

One Google Account can own Play Console, Firebase/Google Cloud and AdMob. Use an account controlled by the actual publisher, enable two-step verification and retain its recovery methods. You do not need three Gmail addresses. Identity documents, payment details and contracts belong in Google's consoles, never in this repository or a chat.

The delivered Lab has Google sign-in integration and tested Google sample advertising, but no owner configuration. Real checkout deliberately refuses to launch. **A production wallet backend and actual purchase fulfillment still need implementation. Supplying IDs alone does not enable safe real sales.** [Current implementation and evidence](MONETIZATION_SETUP.md).

## 1. Establish the production application

Use **`com.androidhit.lumadrift`** for the production game. Keep **`com.androidhit.lumadrift.sandbox`** for the existing Lab, with separate test currency and configuration. The current 1.3 AAB belongs to the sandbox package: do not upload it into the production application's record. A matching production AAB must be prepared next. A first Play upload fixes the package identity, so check it before submitting.

| Service | Create | Cost / prerequisite | Information to return |
|---|---|---|---|
| [Play Console](https://play.google.com/console/signup) | Developer account and Luma Drift application | US$25 one-time registration; identity verification | Account type/status, application link when available, product setup and prices |
| [Firebase](https://console.firebase.google.com/) | One production project and Android application | Google authentication can start on Spark without a billing account | Public client configuration listed below |
| [AdMob](https://admob.google.com/) | Publisher account, Android application, rewarded units | No signup fee; payments/account verification | App ID, three rewarded-unit IDs, publisher/app-ads.txt information |
| Google Cloud billing | Billing account linked to that Firebase project | Needed for the proposed Cloud Run wallet deployment; Firebase becomes Blaze | Project ID and billing-enabled status, never card details |

Google's [Play registration guide](https://support.google.com/googleplay/android-developer/answer/6112435?hl=en), [Firebase billing plans](https://firebase.google.com/docs/projects/billing/firebase-pricing-plans) and [AdMob signup guide](https://support.google.com/admob/answer/7356092?hl=en) describe these requirements. Cloud free quotas do not guarantee a zero bill; budget alerts are notifications, not spending caps.

## 2. Create the Play Console account first

1. Choose **Personal** if publishing as yourself, or **Organization** if an actual organization owns the game. Both can monetize. Organization registration normally requires its D-U-N-S number and organization verification; do not invent a company to bypass personal-account requirements. [Account types](https://support.google.com/googleplay/android-developer/answer/13634885?hl=en).
2. Complete the requested identity/contact checks. New personal accounts also require verification using a physical, non-rooted Android device running Android 10 or later through the Play Console app. [Device verification](https://support.google.com/googleplay/android-developer/answer/14316361?hl=en).
3. Create **Luma Drift**, category **Game**, free download, containing ads and in-app purchases. Complete the merchant payments profile for purchases under the account's monetization/payment settings; enter banking/tax information directly into Google. [Payments setup](https://support.google.com/googleplay/android-developer/answer/7161426?hl=en).
4. Once the production AAB is prepared, use **Test and release → Testing → Internal testing** for initial installation and purchase qualification. Enable Play App Signing and retain the existing upload-signing continuity. Return the public **App signing key certificate** SHA-1 and SHA-256 from App integrity/Play App Signing; this certificate can differ from the upload key. [App signing](https://support.google.com/googleplay/android-developer/answer/9842756).
5. A new personal account created after 13 November 2023 needs a **closed test with at least 12 testers opted in continuously for at least 14 days**, followed by an application for production access. Internal testing can begin earlier but does not satisfy this closed-test gate. Public availability is subject to Google's review. [Testing requirements](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en).

The developer display name, support email, countries, intended audience, content rating, ads declaration, Data safety, privacy policy and account-deletion declarations must describe the actual production app. The repository's development privacy notes are not automatically a complete production policy.

## 3. Enable Google authentication in Firebase

1. Create a production Firebase project, for example with display name **Luma Drift Production**. Its project ID is chosen/assigned by Google and must be returned exactly. Analytics is optional and is not used by the current implementation.
2. Add an **Android** app with package **`com.androidhit.lumadrift`**. Register these existing public release-certificate fingerprints, which we already possess:

```text
SHA-1
B6:C3:6F:61:2E:94:F0:40:2E:2E:B5:51:0E:89:A8:7B:DD:6D:BE:D1

SHA-256
DC:C0:68:61:63:0B:B1:AC:0D:AF:76:F8:F2:D2:2D:E6:33:23:8C:8B:36:A1:99:05:0B:A5:BD:CB:5D:DC:C2:87
```

3. Open **Authentication → Sign-in method → Google**, enable the provider and choose the support email. Add Play's actual app-signing fingerprints once available; otherwise a direct install may sign in while a Play install fails.
4. In Google Auth Platform, configure the app name, support/developer contacts and public homepage/privacy/terms links; use an external audience for a public game. Follow any branding review the console requests. We need basic Google identity, not Gmail/Drive permissions. [Branding configuration](https://support.google.com/cloud/answer/15549049?hl=en), [audience settings and basic-sign-in exceptions](https://support.google.com/cloud/answer/15549945?hl=en).
5. After enabling Google, download a fresh **`google-services.json`** from the matching Firebase Android app. Send that public client configuration, or extract these four values:

| Game field | Location in `google-services.json` | Expected shape |
|---|---|---|
| `projectId` | `project_info.project_id` | Your exact project ID |
| `appId` | Matching client's `client_info.mobilesdk_app_id` | `1:NUMBER:android:HEX` |
| `apiKey` | Matching client's `api_key[].current_key` | `AIza…` |
| `webClientId` | Matching client's `oauth_client[]` entry with `client_type: 3` | `NUMBER-STRING.apps.googleusercontent.com` |

Select the client for `com.androidhit.lumadrift`. **The Web application OAuth client ID is required, not the Android OAuth client ID.** No OAuth client secret is needed in the app. This project initializes Firebase programmatically from its own ignored local configuration file; sending the Google file does not mean we will commit it unchanged.

Firebase client configuration is intended to ship with the app and is different from a privileged service-account JSON key. Apply appropriate API restrictions; server access controls protect data. Use a separate Firebase project if later enabling Lab identity to avoid mixing real and test users/data. [Firebase Google sign-in](https://firebase.google.com/docs/auth/android/google-signin), [configuration fields](https://firebase.google.com/docs/android/google-services-plugin-and-file), [API-key guidance](https://firebase.google.com/docs/projects/api-keys), [environment separation](https://firebase.google.com/docs/projects/dev-workflows/general-best-practices).

## 4. Create AdMob and rewarded placements

1. Sign up using the same publisher-controlled Google Account. If it already has AdSense, use the associated account rather than creating duplicates. Choose the actual payment country/legal entity carefully; the AdMob payment country cannot simply be changed later. Enter payment/identity information only in Google. [Account setup](https://support.google.com/admob/answer/7356092?hl=en), [payment country](https://support.google.com/admob/answer/7356219?hl=en).
2. **Apps → Add app → Android → Not published** lets you prepare Luma Drift before public listing. Use the production package when linking its eventual Play listing. [Add an unpublished app](https://support.google.com/admob/answer/9989980?hl=en).
3. Create three units of format **Rewarded**, with the following suggested names. Three units are our measurement/configuration choice, not a Google requirement. Current code uses one shared unit; the production integration must route each placement to its own supplied ID.

| Unit name | Offer shown in the game | Console reward suggestion |
|---|---|---|
| `luma_gold` | 35 gold | Quantity 35, item `gold` |
| `luma_continue` | One continuation, disclosing the exact extra actions | Quantity 1, item `continuation` |
| `luma_power` | One selected unlocked power | Quantity 1, item `power` |

Return the **AdMob app ID** (`ca-app-pub-…~…`, tilde) and each **ad unit ID** (`ca-app-pub-…/…`, slash). These are public configuration; no AdMob API secret is needed. [Rewarded units](https://support.google.com/admob/answer/7311747?hl=en), [where to find IDs](https://support.google.com/admob/answer/7356431?hl=en-uk).

4. In **Privacy & messaging**, publish the European regulations message linked to this app and its privacy-policy URL, with appropriate languages. Configure applicable US state messaging if distributing there. Adult declaration and the local opt-in do not replace UMP's consent/privacy choices. We will test acceptance, refusal, later withdrawal, unavailable forms and offline behavior. [European messages](https://support.google.com/admob/answer/10113207?hl=en-GB), [UMP implementation](https://developers.google.com/admob/android/privacy).
5. Copy AdMob's personalized **app-ads.txt** entry. Provide a public developer website and host the exact entry at its root `/app-ads.txt`; put that website's address in the Play listing. Return the full entry or publisher ID (`pub-…`). New AdMob apps require this verification before full ad serving, followed by app readiness review. A supported public store listing must be linked for readiness; creating IDs is possible earlier. [Verification requirement](https://support.google.com/admob/answer/14538460?hl=en), [hosting instructions](https://support.google.com/admob/answer/9363762?hl=en), [readiness review](https://support.google.com/admob/answer/10564477?hl=en-GB).

During development, use Google's sample creatives or registered test devices, including when testing production IDs. Do not click your own live advertising. IDs, account approval, consent qualification and actual ad availability are separate milestones.

## 5. Create the three gold products

After a production-package release containing Billing has been **published to the internal testing track**, open **Monetize with Play → Products → One-time products**. Uploading a bundle alone does not complete this prerequisite. Product identifiers cannot be renamed later. [Billing prerequisites](https://developer.android.com/google/play/billing/getting-ready), [current product setup](https://support.google.com/googleplay/android-developer/answer/16430488?hl=en).

| Exact product ID | Gold credited per purchased unit | Suggested display name |
|---|---:|---|
| `luma_gold_500` | 500 | Gold — 500 |
| `luma_gold_1400` | 1,400 | Gold — 1,400 |
| `luma_gold_3600` | 3,600 | Gold — 3,600 |

For each, add the description, a **Buy** purchase option (suggested option ID `buy`), the actual base price, countries and required tax/compliance details, then activate. Return the exact product IDs, option IDs, prices/currency and activation status. Consumable fulfillment is implemented through consumption in the app/backend; do not look for an assumed universal “consumable” checkbox. The game must show Google's returned localized price, not a hardcoded imitation.

Add test accounts both to the internal testing track and to **Settings → License testing**. These are separate settings. Verify that the purchase sheet identifies a test and use Google's test payment instruments for approval, decline and pending completion. **Internal-track membership alone does not prevent real charges.** [Purchase testing](https://developer.android.com/google/play/billing/test).

## 6. Prepare the wallet's cloud account, then implement the service

Use the same production Firebase/Google Cloud project for identity and the proposed Cloud Run + Firestore + Pub/Sub wallet. Link a Cloud Billing account (Blaze) when ready for deployment and return the project ID and billing-enabled status. The server is not yet written or deployed. There is no existing endpoint for you to supply.

The implementation must bind purchases to verified Firebase users, verify Play tokens server-side, credit each transaction once, consume after durable delivery, and reconcile pending purchases, refunds and revocations. **All gold grants and spends must be authoritative on the server**, rather than importing a client-editable paid balance. Lab simulation currency must stay isolated. Ad rewards need appropriate server verification and replay protection too. [Purchase security](https://developer.android.com/google/play/billing/security).

Once the service identity exists, enable **Google Play Android Developer API**. Add its service-account **email address** in Play Console → Users and permissions, scoped to Luma Drift and the documented purchase-verification/order permissions; do not give global administrator access. A managed Cloud Run identity can authenticate without exporting a JSON private key. [Developer API setup and permissions](https://developers.google.com/android-publisher/getting_started), [managed server credentials](https://firebase.google.com/docs/admin/setup).

For Real-time Developer Notifications, a later deployment creates a Pub/Sub topic and authenticated delivery. Grant `google-play-developer-notifications@system.gserviceaccount.com` Publisher on that topic only. In Play monetization setup, enter its full `projects/PROJECT_ID/topics/TOPIC` name, enable one-time-product notifications and send a test. Notifications trigger verification; they do not independently authorize arbitrary gold. Return the topic/service email only after these resources exist. No need to create them blindly now. [RTDN setup](https://developer.android.com/google/play/billing/getting-ready).

## 7. Public pages and handoff

Provide a developer website, support email, privacy-policy URL and external account-deletion request URL. If no website exists yet, say so: we can prepare the pages and publishing steps next. Account creation requires an in-app deletion route and an external request resource; the existing Firebase-only deletion must be extended to cover the production wallet/account policy. [Play account-deletion requirements](https://support.google.com/googleplay/android-developer/answer/13327111?hl=en-en).

Copy this checklist and fill available values. **The first useful batch is Play account status, Firebase configuration and AdMob IDs.** Product status, Play signing certificate and cloud service details can follow after the corresponding upload/deployment.

```text
Publisher: personal / organization
Play account: created / verification pending / verified
Play application link (when available):
Production package: com.androidhit.lumadrift
Support email:
Developer website: existing URL / not yet created
Privacy policy URL: existing URL / not yet created
Account deletion request URL: existing URL / not yet created

Firebase projectId:
Firebase appId:
Firebase apiKey:
OAuth webClientId:
(Or attach the matching google-services.json client configuration.)
Google sign-in provider: enabled / pending
Play app-signing SHA-1 and SHA-256: available values / pending first upload

AdMob app ID: ca-app-pub-...~...
Rewarded gold unit: ca-app-pub-.../...
Rewarded continuation unit: ca-app-pub-.../...
Rewarded power unit: ca-app-pub-.../...
Publisher ID or complete app-ads.txt line:
Privacy messages: published / pending

luma_gold_500: option ID / base price + currency / active or pending
luma_gold_1400: option ID / base price + currency / active or pending
luma_gold_3600: option ID / base price + currency / active or pending
Internal test track and license testers: ready / pending
Cloud billing on the production Firebase project: enabled / not yet enabled
```

Do not send Google passwords, two-factor codes, OAuth client secrets, service-account private-key JSON, private signing keys, bank details or identity documents. The requested IDs and certificate fingerprints are public configuration.

## What happens after handoff

Prepare a production build with the correct package/configuration, route the three rewarded units, implement the server wallet and Play purchase lifecycle, publish the required public pages, then qualify real Google login and Play license-test purchases on actual Play installs. Test cancellation, pending/declined payment, duplicate delivery, recovery after reinstall, refunds, ad refusal and deletion before enabling real traffic. Finish the store declarations, closed test if required, AdMob verification and production review. No source edit, sample-ad screenshot or simulated purchase alone establishes production activation.
