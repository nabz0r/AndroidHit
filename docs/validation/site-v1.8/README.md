# Website 1.8 — source, browser and delivery evidence

The existing Firebase site remains `luma-drift-production`; no migration or new hosting project is involved. Source is the repository's `public/` directory. The home page presents the actual 1.8 candidate, seven interactive lessons, reversible L/M/T planning, original Dream Glade artwork, clear possession and currencies, with real advertising and purchases still closed.

- [Frozen source manifest](release/source-manifest.json): **58 hosted payloads**, excluding tests, with SHA-256 and byte lengths. No network byte comparison is implied.
- [Offline checker](release/static-check.json): **2,508 assertions**, including 67 local references, APK metadata, FR/EN copy and eight static text-contrast combinations.
- [Native image provenance](captures.json): ten unedited PNG copies matching the passing final Lab menu run byte for byte. That guest profile is distinct from the separately recorded 100-level playthrough.
- [Browser matrix](browser-views.json): the home, privacy and deletion pages in FR/EN at 320, 390 and 1366 CSS pixels; no horizontal overflow, language control and anchor route checks. The browser uses a scrollbar, so document client width can be narrower than innerWidth. Only read-only DOM measurements were evaluated; navigation/language changes used the browser controls.
- [Final local screenshot](../../screenshots/site-v1.8/desktop-home-final-fr.png): the fully loaded French desktop view, with the real Lab home PNG. This is a **local source preview**, not a render of the Firebase public origin.

The public JS suites pass 32 tests (24 synthetic deletion boundaries, eight language routes). These tests do not execute real sign-in or account deletion. Runtime Firebase configuration, deletion flow and privacy page text remain unchanged; the shared visual style is refreshed.

## Firebase delivery · 27 October 2026

**Published at 14:59:00.486 UTC** from exact main [`5cab179b`](https://github.com/nabz0r/AndroidHit/commit/5cab179b6eff488e641b75e5c6dfaa0e843d6553), after all 58 payload hashes matched in the clean authenticated Cloud Shell checkout. The Firebase CLI reported **Deploy complete!**; the management channel identifies active finalized version **`4324523b7540b1a1`** on the existing live site.

[Delivery record](delivery.json) · [Observed CLI receipt](cloudshell-deploy.txt) · [Selected live metadata](firebase-live.json) · [Native browser screenshot of the receipt](../../screenshots/site-v1.8/firebase-live-version.png).

This is CLI/management confirmation plus predeployment source hashes. Public-origin rendering and served-byte comparison were not requested. The local preview above remains labelled local. The existing Cloud Run server was unchanged and not redeployed. Later documentation commits retain the identical hosted public tree `c575a999c6411531d2382e707cbf69c1f7696a09`.
