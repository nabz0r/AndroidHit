# Luma Circle and prepared premium cosmetic draws

The 1.7 source adds authenticated friendships and finite **earned hint gifts** to the existing wallet service. The Android social base URL is independent of the paid-wallet base URL. Configuring social does not enable Play Billing, paid draws or production ads.

## Identity and privacy

Every endpoint below requires the same verified, non-revoked Firebase Google ID token as the wallet. There is no email lookup, address-book import, public nickname search, anonymous social account or public profile directory. Opening Circle after explicit Google sign-in creates a random 16-character friend code. Codes are generated on the server, checked for collisions atomically and cannot be selected by the client. Friend responses expose a nickname, companion ID and code, never Firebase UID, email or purchase information.

Map position sharing starts **off**. `shareProgress` is the explicit opt-in; hidden positions return `level: null`. Visible levels are clamped to 1–1000 and explicitly `progressVerified: false`: they are device-reported progress, not cheat-resistant competition or cloud-save evidence. The Android refresh publishes the current device level and companion only when the saved sharing opt-in is true. A device-local, account-scoped cache lasts at most 15 minutes and returns no friends after sign-out.

## API contract

All POST bodies are exact schemas. Additional keys, client reward amounts, prices and emails are rejected. All mutation request IDs are 8–160 characters from `[A-Za-z0-9_:-]`.

| Route | Body / response |
|---|---|
| `GET /v1/social` | `me`, `friends`, `incoming`, `outgoing`, `gifts`, `limits`, `serverTimeMs` |
| `POST /v1/social/profile` | `{nickname, avatar, level, shareProgress}`; nickname 1–32 characters, no controls/bidi format characters or angle brackets; companion ID only, never URL |
| `POST /v1/social/request` | `{code, requestId}`; creates a pending request, never auto-accepts a crossed request |
| `POST /v1/social/respond` | `{code, accept, requestId}`; recipient accepts or declines |
| `POST /v1/social/remove` | `{code, requestId}`; removes mutual friendship or withdraws the pending invitation |
| `POST /v1/social/gift` | `{code, requestId}`; accepted friends only; returns `giftId` |
| `POST /v1/social/claim` | `{giftId, requestId}`; recipient only; returns the same stable `grant` on retry |

A friend projection is `{code, nickname, avatar, level, shareProgress, progressVerified:false}`. `level` is null when not shared. A gift grant is **always** `{id:giftId, kind:"power", itemId:"hint", amount:1}`. The caller cannot choose its kind or value.

The Android `SocialActivity` opens from an explicit intent. It presents invitation approval/decline, withdrawal, removal confirmation, profile-sharing consent, visible limits, gifts and account access. Offline, unconfigured, not-yet-deployed and sign-in-required states are shown honestly. No sample friends or fake offline gift rewards are created.

## Limits and replay handling

- At most **40 accepted friends** and **20 incoming / 20 outgoing active invitations** per profile.
- At most **20 new outgoing invitations per UTC day**. Incoming requests expire after 7 days. A crossed invitation requires an explicit acceptance.
- At most **3 gift sends per UTC day**, with **1 gift to the same friend per day**. The sender's local clock is never consulted. Sender/recipient profiles and gift record update in one transaction.
- A gift ID binds sender, recipient and UTC day. Retrying with a different request ID on the same day returns the same gift without spending another daily send. A same request ID on a later day replays its original result during the receipt window.
- At most **5 first gift claims per recipient per UTC day**, across all senders. Replaying an already claimed gift does not consume another claim. This bounds basic many-sender gift farming without claiming complete Sybil resistance.
- At most **64 unexpired gift records** in an inbox. Gifts expire after **7 days**. Claims are durable and re-return the same grant until expiry; claiming does not mint LumaGold or modify the paid wallet.
- Social mutation receipt documents expire after **30 days** and are removed by the existing authenticated maintenance job. Reusing an ID for a different action/peer returns `REQUEST_CONFLICT` during that window.
- The existing durable per-UID request rate limit applies to every social route across Cloud Run instances. Unknown code requests have no public listing API and still consume that limit. Accounts can create other accounts; this is not asserted to be a complete Sybil defense.

The native earned-item ledger atomically stores the gift ID and one local hint charge per account. A full stock of 999 does not burn the local receipt: the player can retry after using a charge. A claimed gift remains available as “Check delivery” until expiry, allowing a lost response or process restart to recover the charge. These are **device-earned items**, not paid currency or a server-authoritative cross-device consumable balance. Reinstallation/cross-device progress remains local; this release does not claim cloud save or competitive anti-cheat.

## Deletion and maintenance

The wallet deletion job first freezes the account (`DELETING`). Social mutation transactions check that state for both participants. Before Firebase Auth deletion, it deletes the profile/code, removes reciprocal friends and invitations, erases sent/received gift documents and their inbox entries, and removes the owner's social request receipts. Provider failure keeps the durable wallet deletion job pending for retry, without reopening social access. Peers' operation receipts contain opaque hashes, no deleted nickname/profile, and expire within 30 days.

The Android account controller uses server account deletion whenever `SOCIAL_API_BASE_URL` is configured, even if paid Billing is disabled. If the server is unavailable it reports a pending/error state; it does not silently delete Auth alone and leave active relations. The public deletion page already calls that same server endpoint. Maintenance cleans expired gifts and social request receipts before the Play refund poll, so unavailable Play permissions do not block social retention cleanup. No new composite indexes or broader IAM roles are required. Existing Firestore deny-all client rules cover the new collections.

## Prepared premium cosmetic draw — disabled by default

`PAID_COSMETIC_DRAWS_ENABLED=false` is the production default. This is server infrastructure only, **not a qualified paid product or active checkout UI**. Real payments, regional/age eligibility, Play products and store disclosures still require their separate qualification. Never change this flag merely to test an APK.

Authenticated `GET /v1/wallet/cosmetic-catalog` discloses `enabled`, currency `LumaGold`, price **100**, duplicate refund **50**, new-object guarantee **20**, current pity count, owned IDs and all 24 objects with the **actual conditional per-item probability** for the next draw. Common items have normal weight 750 each (8 × 7.5% = 60%), rare 375 each (8 × 3.75% = 30%), epic 125 each (8 × 1.25% = 10%). Hats, dresses, shoes and gloves are cosmetic only, including Halloween/Valentine motifs. The IDs match the native wardrobe catalogue.

`POST /v1/wallet/cosmetic-draw` accepts only `{requestId}` and fails with `PAID_COSMETIC_DRAWS_DISABLED` while closed. When explicitly enabled in a separately qualified environment, server crypto chooses the item; one transaction debits the verified premium wallet, grants permanent account ownership, updates pity and saves the result. Repeated requests return their persisted result. A duplicate returns exactly 50 LumaGold (half the 100 price), not local earned Lumacoins. After 19 consecutive duplicates, the next draw selects only unowned objects using their renormalized weights; when all 24 are owned the normal pool remains and every draw receives the disclosed duplicate refund. No draw outcome affects puzzle difficulty, damage or scoring.

Firestore retry callbacks reuse a private random seed and perform deterministic rejection sampling, avoiding modulo bias and random rerolls inside retried transactions. No client-supplied reward, ownership, price or probability is accepted. Premium ownership remains on the server; the candidate's native wardrobe currently uses the separate earned wardrobe and does not advertise a paid entitlement restore flow.

## Deployment

1. Run the Node and local Firestore suites described in [TESTING.md](TESTING.md). Build/install the APK and exercise native guest/unavailable states separately.
2. Deploy this **same server directory** to the existing `luma-wallet` service with its existing managed service account and unchanged identity configuration. Preserve all existing environment variables. Add/keep `PAID_COSMETIC_DRAWS_ENABLED=false`; no social credentials or new roles are needed.
3. Retain `min-instances=0`, bounded `max-instances` and existing concurrency/budget controls. Social requests use several transactional document reads/writes; the billing budget is an alert, not a hard spending stop.
4. Use `/health` for service readiness. A health 200 proves readiness only; anonymous `/v1/social` must return 401. Qualify two real Google test accounts, opt-in visibility, invitation acceptance, finite gift exchange and deletion separately before claiming end-to-end production social success.
5. Keep `WALLET_API_BASE_URL` / paid checkout gates and the paid-draw flag closed until separate payment qualification. The APK's `SOCIAL_API_BASE_URL` may point to this deployed origin independently.

Emulator tests are loopback-only `demo-luma-wallet`; no test creates provider accounts, spends real money, sends a real gift or touches production data.
