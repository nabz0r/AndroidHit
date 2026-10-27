# Website 1.8 — source, browser and delivery evidence

The existing Firebase site remains `luma-drift-production`; no migration or new hosting project is involved. Source is the repository's `public/` directory. The home page presents the actual 1.8 candidate, seven interactive lessons, reversible L/M/T planning, original Dream Glade artwork, clear possession and currencies, with real advertising and purchases still closed.

- [Frozen source manifest](release/source-manifest.json): **58 hosted payloads**, excluding tests, with SHA-256 and byte lengths. No network byte comparison is implied.
- [Offline checker](release/static-check.json): **2,508 assertions**, including 67 local references, APK metadata, FR/EN copy and eight static text-contrast combinations.
- [Native image provenance](captures.json): ten unedited PNG copies matching the passing final Lab menu run byte for byte. That guest profile is distinct from the separately recorded 100-level playthrough.
- [Browser matrix](browser-views.json): the home, privacy and deletion pages in FR/EN at 320, 390 and 1366 CSS pixels; no horizontal overflow, language control and anchor route checks. The browser uses a scrollbar, so document client width can be narrower than innerWidth. Only read-only DOM measurements were evaluated; navigation/language changes used the browser controls.
- [Final local screenshot](../../screenshots/site-v1.8/desktop-home-final-fr.png): the fully loaded French desktop view, with the real Lab home PNG. This is a **local source preview**, not a render of the Firebase public origin.

The public JS suites pass 32 tests (24 synthetic deletion boundaries, eight language routes). These tests do not execute real sign-in or account deletion. Runtime Firebase configuration, deletion flow and privacy page text remain unchanged; the shared visual style is refreshed.

Firebase publication is a separate delivery step. Its management-plane/CLI receipt will identify the exact committed source and payload hashes; public-origin rendering and served-byte comparison are not part of this review. The existing Cloud Run server is unchanged.
