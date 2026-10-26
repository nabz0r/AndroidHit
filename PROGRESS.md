# Production preparation progress

Updated 26 October 2026. Target: **1.4.0-rc1, version code 5**.

| Work | Status | Evidence / remaining work |
|---|---|---|
| Public owner configuration | Received for Android | Production Firebase package/project, public key and supplied OAuth Web client match; three owner rewarded IDs integrated. Support/domain not yet supplied. |
| Build channels | Built and signed | Production 1.4.0-rc1 and Lab 1.4.0-sandbox / code 5; sample creatives, empty wallet origin. |
| Purchase/wallet server | Implemented and locally verified | 42 Node 24 cases + 2 actual Firestore emulator cases, source hashes checked; no deployed service. |
| Android Billing and wallet | Implemented | Real checkout/recovery and server-only paid gold; permanent cosmetic equip bridge. Endpoint gate remains closed. |
| Public pages | Prepared and browser-tested | 24 core cases, 37 local refs, 18 responsive FR/EN states; owner contact/Web config/hosting pending. |
| Verification | Candidate checks passed | 895,714 gameplay checks, 309 client-boundary checks, 42 + 2 server cases; signed final APK installed, six languages, actual Google provider cancel, gameplay/restart/upgrade. No live purchase/login claim. |
| Publication | Published to main | Release commit `b87c756`; all 156 release files matched remote Git objects, and raw production APK/AAB downloads matched the tested files byte for byte. |

The candidate must not claim live payments, Google sign-in, production advertising, cloud durability or store approval from simulations.
