# Production preparation — 1.4.0 release candidate

Owner request: implement the gold-purchase server, prepare public privacy/deletion pages and a production Android build, test/document the result and publish it on `main`.

## Work and acceptance

1. **Server wallet:** Firebase-authenticated HTTP service; Google Play verification; Firestore transactions; unique fulfillment; durable recovery/consumption; refund notifications/reconciliation; account deletion. Exercise failure, concurrency and replay scenarios without fabricating Google receipts.
2. **Android production channel:** separate production/Lab build configurations, supplied public Firebase/AdMob IDs, three rewarded placements, real Billing flow and server-only paid balance. Keep checkout blocked while deployment/configuration is missing; keep Lab simulation isolated.
3. **Useful purchased currency:** spend paid gold on permanent companions/skins through the server; restore account-bound ownership. Never import editable local coins into paid gold. Existing local gameplay, powers and rewards remain available offline.
4. **Public resources:** responsive FR/EN developer, privacy and deletion pages; app-ads.txt; explicit owner/hosting configuration still required. No invented support address or claimed publication.
5. **Delivery evidence:** server and game regression tests, Android build/lint/signature/bundle checks, installed production UI/gameplay checks and screenshots, refreshed documentation and versioned artifacts published on `main`.

## External milestones

Google sign-in requires a Web OAuth client ID: the first supplied Android Firebase file has none. Play license tests require the owner's products/account/track; production wallet deployment requires Cloud billing and scoped service permissions. AdMob serving requires account/app/consent qualification. These are separate from local tests and a successfully built APK.

No paid cloud resources or production traffic are activated by this preparation task.
