# Luma Drift data and privacy inventory

## 1.17 local profile, collection and bundled art

The chosen local nickname, explicit display flag, earned/equipped badges,
companion/outfit ownership, draw receipts and last arrival are stored in the
existing private game save. The flag is chosen by the player; it is not
inferred from location. These fields are separate from Google account identity
and are not synchronized or uploaded by this release. The optional local
journal and explicit exports retain their existing limits.

The new sprites, clothes, furniture and room materials are bundled files.
No image-generation request, remote sprite fetch, analytics service or new
permission is added during play. Music is synthesized on the device.
The four website sound excerpts are static WAV exports. Existing SDK/account,
backup, deletion and commercial-provider boundaries remain below.
[Current qualification](validation/v1.17/README.md) · [Art/audio sources](ART_AUDIO.md).

Updated **28 October 2026** for **production 1.13.0-rc2 / code 14**, package `com.androidhit.lumadrift`, and isolated **Lab 1.13.0-sandbox-rc2**. This document describes implemented data paths and qualification limits. It is not a completed privacy/store submission or an assessment of every publisher obligation. Public publisher label: **nabilux**; private support correspondence: **nabz0r+gaming@gmail.com**. [FR/EN public policy and deletion pages](PUBLIC_PAGES.md).

## 1.13 home, first encounter and journal

Furniture/surface ownership, confirmed placements/rotations, the companion’s floor position, chosen wall/floor and one furnishing wish are stored only in the existing private `luma_drift_save_v1` preferences. Keys are `home_layout_v113`, `home_owned_v113_<id>`, `home_wallpaper_v113`, `home_floor_v113` and `home_goal_v113`. The skippable three-piece opening stores its actual action history and seen marker in `first_light_history_v113` / `first_light_seen_v113`. It creates no new account identifier or server entitlement.

Hug, light and rest/activity animations are temporary; the house does not store relationship/need scores or neglect penalties. Existing equipped character/clothing/gear is reused rather than copied into a separate network profile. Google sign-in does not synchronize the room or gameplay; backup exclusions and local uninstall/data-clearing consequences remain.

When the existing optional local journal is enabled, it can record home movement/interactions and purchases alongside other game actions. It stays off by default and bounded to 2,000 entries; no automatic home/journal transport, analytics SDK, contact import or demographic tracking is added. Export remains the player’s explicit Android chooser action. [Home state contract](HOME_1_13.md).

## Versions, local data and separate currencies

Preserved **1.1.0** has no account, Internet permission, advertising or Billing SDK. [Historical privacy](PRIVACY_LAB_HISTORY.md) records that release and earlier Lab behavior; its no-account statements do not describe 1.13.

Current production includes Google Auth, advertising/UMP and Play Billing integrations. Owner public Firebase configuration is present, but completed Android login is not qualified. The delivered APK forces sample creatives and embeds an empty wallet origin, so real checkout is blocked. The Lab uses its own package/private storage, sample ads and a no-charge simulator; it does not bundle production identity configuration.

Stars, progression, local event history, XP/missions, earned coins, six power inventories, equipped avatar and the **18 equipment items** stay on the device. Headwear first-win receipts, Aura discounts and per-run Charm protection are local game state. Optional Google identity does not upload or synchronize them. Backup/transfer rules exclude the save; uninstalling or clearing app storage removes local progress. [Equipment contract](AVATAR_EQUIPMENT.md).

Purchased gold is a separate authenticated server wallet. The first Android integration can redeem permanent companions/themes when configured and qualified; it does not import earned coins, simulator transactions or ad rewards. Account cosmetics may have a UID-bound display/equip cache. That cache cannot authorize server funds or prove a purchase.

## Data paths

| Path | Data and purpose | Destination / control |
|---|---|---|
| Offline game | Stars, unlocks, earned coins, equipment/ownership, power stocks, cosmetics, language/audio settings, board/action/protection counters, XP and local event dates | Private app storage; guest play needs no account |
| Optional local journal | Bounded play events, language/version, dates/results and enabled ad observations | Off by default; at most 2,000 local entries; explicit export only |
| Google sign-in | Selected Google identity, app authentication credential/session, display name/email in account UI | Credential Manager and Firebase Authentication after explicit sign-in; a prior configured session may restore |
| Optional advertising | Age/consent choices; SDK technical data according to Google and actual configuration | Google SDKs through declared-adult, explicit opt-in, UMP eligibility and manual load/watch controls |
| Play purchases | Product/token/account binding, payment state and order/refund information | Google Play payment UI; opaque token sent to authenticated wallet API; no in-game card-entry form |
| Server wallet | Firebase UID binding, paid balance, permanent ownership, request IDs and purchase/refund audit | Existing Cloud Run/Firestore service; server-authoritative value mutations; actual purchase lifecycle still unqualified |
| Public site | Static requests; explicit Web Google sign-in/deletion when chosen | Hosting provider; identity/API modules after the configured sign-in action; no site analytics or remote fonts |

No Firebase Analytics, Crashlytics, remote gameplay analytics, contact-list, microphone, camera or location feature is implemented. This does not mean Google SDKs or Hosting process no technical data. [Advertising SDK disclosure](https://developers.google.com/admob/android/next-gen/privacy/play-data-disclosure) · [Firebase privacy](https://firebase.google.com/support/privacy).

## Advertising choices and identity are independent

Advertising starts disabled with age unspecified. An adult declaration is not age verification. Declining, unavailable advertising or a refused consent choice leaves puzzles and free restart usable. A local switch is not a substitute for Google's configured privacy form. The controller checks UMP eligibility and supports the required privacy-options route before advertising requests. Turning its switch off blocks new controller requests and discards cached ads; it does not recall data already processed by Google. [UMP implementation](https://developers.google.com/admob/android/privacy), reviewed 27 October 2026.

The owner reports the European message as Published, with refusal/close choices and the game's six languages. That report is not independent observation of the console or a recorded in-game acceptance/refusal. Ad readiness, applicable audience/region declarations, app-ads.txt verification and live serving remain separate gates. Sample creatives do not certify those settings. [Owner checklist](PRODUCTION_ONBOARDING.md).

Firebase initializes for explicit identity or restoration of a prior configured session; its default auto-initialization/data collection are disabled in the app. Signing in does not enable ads, authorize spending or upload progression. The code does not write emails or raw identity/purchase tokens to the play journal. The named Auth SDK manages its session; paid-wallet requests require the matching current Google/Firebase user.

## Wallet operation, retention and deletion

Cloud Run is deployed. Recorded public routing/CORS checks and owner-reported Web deletion have narrower scope than a qualified paid-wallet lifecycle. Play license-test purchase, pending/refund reconciliation, RTDN and scheduled maintenance remain unqualified; Scheduler/RTDN and automatic audit purging are not active. [Observed deployment boundaries](PRODUCTION_RELEASE.md).

The server implementation verifies Firebase Google tokens, independently queries Play and transactionally binds/credits an allowlisted purchase before consumption. Active-account audit records retain opaque tokens/order references for reconciliation; direct Firestore clients are denied. Application request-body/token logging is not enabled. Platform logs/backups require deliberate owner configuration.

With a configured wallet origin, Android deletion calls `DELETE /v1/account` after confirmation and requires recent Google authentication. A server error does not fall back to deleting only Firebase. Implemented deletion freezes value changes, removes the app's Firebase user and active wallet/ownership/request data, and redacts raw purchase data. Durable recovery jobs exist in code but their automatic operation has not been qualified or enabled by the pending scheduler. The configurable minimal hashed audit default is **365 days**, not a legal requirement or a promise that automatic purge is already running. [Server contract](../server/README.md).

The delivered Android APK has no wallet origin and cannot create a paid wallet; its account action therefore uses Firebase app-user deletion. Neither route deletes the person's Google Account, local offline save or already exported files. Deletion is not a Play refund request.

The public Web route has supplied Firebase Web configuration and the real API origin. Its Google control is enabled on authorized hosts and can perform real app-account deletion after explicit sign-in and confirmation. `preparation: true` is a visible qualification banner, not an authentication/deletion lock. Web success is owner-reported; independent end-to-end qualification remains open. [Public-page behavior and evidence](PUBLIC_PAGES.md). Google's requirement distinguishes the in-app route and an external deletion resource; merely writing these guides does not complete it. [Official requirement](https://support.google.com/googleplay/android-developer/answer/13327111?hl=en), reviewed 27 October 2026.

## Explicit exports, permissions and support

Challenge sharing contains the board, its code and game/repository link. The P cards include their matching walls and key badges. Journal exports contain selected local observations and can reveal play habits. Sharing opens Android's chooser; the player selects any destination. The game does not select recipients or automatically upload the journal. Its cache provider grants scoped read access. Already delivered copies are controlled by their recipients.

SDK-contributed Internet, advertising-ID and Billing permissions must be assessed against the exact shipped manifest and SDK configuration. A declared permission alone does not establish transmitted data. [Current binary evidence](VALIDATION.md) · [Third-party facilities](THIRD_PARTY_NOTICES.md).

Use **nabz0r+gaming@gmail.com** for private support. Do not post account identifiers, tokens, passwords or identity documents in public GitHub issues. The display label nabilux does not establish a legal person or organization; publisher obligations and final Play Data safety/privacy declarations still need review. The website redesign and repository documentation do not themselves certify compliance or commercial approval.

## 1.8 local practice state

The optional/replayable interactive tutorial stores a local lesson/phase/typed-history record and a one-time welcome-gift receipt in existing private game preferences. It does not transmit tutorial steps, record Google credentials or add an analytics SDK. Guest and signed-in play share the same local practice reward; Google login does not create a second entitlement. Local uninstall/data removal can remove this local state; this is not a server premium grant. [Tutorial contract](TUTORIAL.md).

## Preserved 1.9 local-state note

> **1.9 local-state addition:** guardian attempts/once-only coin receipts, explicit power-sample receipts and the free avatar-style choice stay in the device profile. Public research uses aggregate published statistics, without personal records. No age field, demographic targeting, new remote analytics or gameplay cloud save was added. Existing account deletion and Google SDK boundaries remain below.
