# Luma Drift data and privacy — production preparation

Updated **26 October 2026**. This is the implementation/data inventory for **1.4.0-rc1**, package `com.androidhit.lumadrift`, and the separate `.sandbox` Lab. The owner has not yet supplied its legal publisher/contact details or hosted the public policy. This repository document is not a claim that the privacy/store submission is complete. [Prepared FR/EN public policy and deletion pages](PUBLIC_PAGES.md).

## Separate releases and currencies

Preserved stable **1.1.0** has no account, Internet permission, advertising or billing SDK. Its [historical privacy statement](PRIVACY_LAB_HISTORY.md) also records the old Lab 1.2/1.3 behavior. Current production **1.4.0-rc1** includes Google Auth, advertising/UMP and Play Billing integrations. Its supplied Firebase configuration is complete, but an actual completed login has not been demonstrated. Sample creatives are forced and the wallet origin is empty; real checkout is blocked. Current **Lab 1.4** stays on `.sandbox`, uses sample ads and a no-charge purchase simulator, and does not include the production Firebase asset.

Earned coins, powers, board progression and event rewards are device-local. Purchased gold is a separate authenticated server wallet when deployed. Its first integration buys permanent companions/themes. A local coin balance, simulator transaction or ad reward never credits purchased gold. Account cosmetics can be cached for display/equip while signed into their matching account; that cache is not a source of server funds or purchase proof.

## Data paths

| Path | Data and purpose | Destination / control |
|---|---|---|
| Offline game | Stars, unlocks, earned coins, inventory, cosmetics, language/audio settings, board session/action counters, XP/missions and local event dates | Private app storage; guest play works without a login |
| Optional local journal | Bounded play events, language/version, dates, level/results, ad callbacks when enabled | Off by default; up to 2,000 device-local entries; explicit export only |
| Google sign-in | Selected Google identity, app authentication credential/session; UI can show display name/email | Google/Credential Manager and Firebase Authentication after explicit sign-in; prior sessions can restore |
| Optional advertising | Consent/age choices; SDK data can include IP address, advertising/device identifiers, interactions and diagnostics according to Google/configuration | Google advertising/UMP only through the controller's adult declaration, opt-in and applicable UMP eligibility; manual ad load |
| Play purchases | Product/token/account binding, payment state, order and refund information | Google Play handles payment; app sends opaque token to authenticated wallet API; no card-entry form is implemented in the game |
| Deployed wallet | Firebase UID binding, purchased balance, permanent ownership, request IDs, token/order audit and refund/consumption state | Owner's proposed Cloud Run/Firestore service; server-only value mutations |
| Public site | Static page requests; optional explicit web Google sign-in/deletion | Hosting provider; Firebase Web identity and API only after explicit configured sign-in; no site analytics or remote fonts |

No Firebase Analytics, Crashlytics, remote gameplay analytics, contact list, microphone, camera or location feature is implemented. This does not mean Google SDKs or hosting providers process no technical data. [Google advertising SDK disclosure](https://developers.google.com/admob/android/next-gen/privacy/play-data-disclosure), [Firebase privacy information](https://firebase.google.com/support/privacy).

## Consent and identity remain independent

The advertising controller starts with age unspecified and advertising disabled. An adult declaration is not age verification. Declining/losing advertising leaves puzzles and free restart usable. The player must explicitly enable advertising, pass UMP's request eligibility and choose to load/watch. Privacy choices are available through the supported UMP control. Turning the app's advertising switch off blocks new controller requests and discards its cached ad; it cannot recall previously processed Google data.

The owner still needs its applicable published consent messages, audience/region handling and store declarations. A sample creative or local switch does not certify those settings or prove an absence of all SDK traffic. [UMP guidance](https://developers.google.com/admob/android/privacy).

Identity is initialized on explicit sign-in or restoration of a previously signed-in configured session; Firebase auto-initialization is removed and its default data collection setting is disabled. Signing in does not enable advertising or upload offline progression. Raw identity/purchase tokens and emails are not put into the game's play journal. The named Firebase Auth SDK manages its session. A current Google/Firebase identity is required for paid-wallet requests.

## Wallet retention, security and deletion

The server is implemented but **not deployed** by this release. It verifies Firebase Google tokens, uses managed server credentials, independently queries Play, and transactionally binds/credits purchases once before consumption. The ledger retains opaque purchase tokens and order references while an account is active so unfinished purchases/refunds can be reconciled; direct client Firestore access is denied. Request-body/token logging is not enabled by the application. Cloud platform logs/backups still require deliberate owner configuration.

When the server origin is configured, Android deletion invokes `DELETE /v1/account` after confirmation and requires recent Google authentication. A service error does not fall back to deleting only Firebase. The server freezes value changes, deletes the app's Firebase user, removes active balance/ownership and request records, and redacts raw tokens/order references. A durable job retries incomplete work. Its minimal hashed transaction/deletion audit has a configurable default **365-day** retention, then a scheduled purge; the owner must review this period and monitor the deployed purge. This is an engineering default, not an assertion of a legal retention obligation. [Server behavior and evidence](../server/README.md).

Without a configured wallet origin, this build cannot create a paid wallet; its account action uses Firebase's app-user deletion. Deletion never deletes the person's Google Account. Local offline progression and already exported files are separate; clear Android app storage/uninstall to remove the local save. The app's backup/transfer rules exclude its save data. Deletion does not itself refund a Google Play purchase.

The external deletion page is prepared to sign in using a separately registered Firebase **Web** app, confirm the request and call the same API. It is deliberately disabled until its Web configuration/domain/API are present. In-app instructions alone do not complete Play's external deletion requirement. [Remaining publication gates](PUBLIC_PAGES.md), [Google account-deletion requirements](https://support.google.com/googleplay/android-developer/answer/13327111?hl=en).

## Exports, permissions and contact

Sharing is explicit. Challenge cards contain a board code and game/repository link; journal exports contain the selected local observations and can reveal play habits. Android's share sheet lets the player choose a destination; the game never selects a recipient or automatically uploads the journal. Cache files use a read-only, grant-scoped provider. Copies already delivered are controlled by their recipients.

The [exact merged production manifest](validation/v1.4/manifest-audit.json) records SDK-contributed permissions, including Internet, advertising ID and Billing. Declaring a permission alone does not establish which data was transmitted. Source inspection, signatures, local simulations, a rendered policy and emulator screenshots do not establish universal compliance, Google approval or a working production service.

The owner must supply a public support address and legal publisher information before launch. Do not put account identifiers, purchase tokens, passwords or identity documents into public GitHub issues. A public issue tracker is for non-sensitive technical reports, not a private account-deletion request route.
