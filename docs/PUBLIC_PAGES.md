# Public developer, privacy and account-deletion pages

The **1.7 website source refresh** targets the existing Firebase site and production application `com.androidhit.lumadrift`. Its source remains in [`public/`](../public/); no hosting migration is involved. It presents the continuous 1–1,000 map, ten atmospheres, safe current-state hints/dead-end recovery, 24 visual wardrobe items in four slots, seven-visit gifts, daily/weekly quests, earned capsules and clearly separate LumaCoins/LumaGold. It links the signed **1.7.0-rc1 APK**, independently verified at 5,594,242 bytes and SHA-256 `7eeaa816…`. Its eleven new images now match the passing final Lab SDK capture report byte for byte. Hosting publication remains a separate pending step.

The latest preserved published record is the [website 1.6 delivery](validation/site-v1.6/README.md), on 27 October 2026. It verified 36 source payloads on the clean deployment checkout and recorded Firebase completion without a public-origin byte comparison. That record is not evidence of 1.7 publication. The [1.7 local qualification record](validation/site-v1.7/README.md) collects the current source hashes, CUA matrix and captures; Hosting completion remains pending.

The existing [stable site](https://luma-drift-production.web.app/) and [qualification preview](https://luma-drift-production--qualification-uht107xg.web.app) have earlier deployment observations dated **26–27 October 2026**. Independent reads of those earlier revisions and owner-reported Google sign-in/deletion success do not verify the 1.6 website or a real account transaction in this review. This is not a completed Google Play privacy/deletion submission. [Historical preview evidence](validation/hosting-preview.md).

The pages use the Luma Drift palette, original code-drawn puzzle artwork, responsive layouts, keyboard focus, reduced-motion handling and French/English controls. The Google sign-in control reuses the app’s unmodified official Google G asset; it is Google branding, not Luma Drift artwork. They need no build framework, remote font or analytics package. Switching language uses a URL parameter, without a cookie or local-storage entry.

| File | Purpose |
|---|---|
| [`public/index.html`](../public/index.html) | 1.7 candidate homepage, exact APK route, final native captures and explicit qualification boundaries |
| [`public/privacy/index.html`](../public/privacy/index.html) | FR/EN privacy information, data paths and deletion/retention explanation |
| [`public/delete-account/index.html`](../public/delete-account/index.html) | In-app instructions and prepared authenticated external deletion flow |
| [`public/site-config.js`](../public/site-config.js) | Public publisher, contact, Firebase Web and wallet settings |
| [`public/app-ads.txt`](../public/app-ads.txt) | Supplied publisher’s authorized advertising seller |
| [`scripts/check-site-v17.py`](../scripts/check-site-v17.py) | Current offline source, DOM, final-image provenance, policy and APK/hash consistency checks |
| [`public/tests/`](../public/tests/) | Request-boundary tests and preserved static/browser runners; exclude from hosting |

## 1.7 website source and qualification boundaries

The homepage uses the original Aurora Atlas and retained Nova vector with a refreshed composition. Product-image paths point at versioned 1.7 captures; the source checker accepts a new product image only when its bytes exactly match the final SDK capture directory, never a pilot. FR/EN native image alternatives cover Home, map, wardrobe and rewards. A composition frame or glow is web layout, not an Android control. [Release](RELEASE_1_7.md) · [Design](DESIGN_SYSTEM.md) · [UX](UX_1_7.md).

The source explicitly distinguishes:

- All 1,000 Journey and 240 Remix boards initially solve inside their fixed budgets; a later legal gate choice can still doom a route. Hints follow the real remaining state, with Undo/free restart for a dead route.
- The new 24-item visual wardrobe and the separate 18 legacy functional equipment items.
- Seven claimed visits without lost-day reset, daily/weekly quests and permanent seasonal ownership.
- Local earned LumaCoins and account LumaGold, without automatic conversion. The earned capsule costs 150 LumaCoins, with 60/30/10% categories, 7.5/3.75/1.25% per item, and 25/60/100 duplicate compensation.
- Prepared authenticated friends, opt-in map snapshots and one-hint gifts, with three sends/five claims per UTC day and one send per friend. No fictitious friend markers or completed real two-account claim.
- Prepared native account premium catalogue and server transactions, with both APK and server draw activation false. Their future account ownership/conditional probabilities are separate from local earned ownership; there is no active paid sale.

Privacy/deletion copy now includes the social code, chosen nickname, avatar, opt-in reported progress/timestamp, mutual relations, requests, gifts and operation receipts. Requests/gifts expire functionally after seven days; receipts are planned for thirty days. Automatic maintenance scheduling is not deployed/qualified, so expiration is not a promise of physical deletion. Server social/wallet cleanup must precede deleting the Firebase identity, with retry on cleanup failure. No address-book/Google-contact import is requested. The policy also distinguishes six Android languages from two website languages.

`check-site-v17.py` performs no network request and launches no browser. At release freeze it verifies the independently supplied APK SHA-256 against the local signed artifact/manifest, all page references, final native-image hashes, FR/EN DOM structure, copy, legal retention/deletion sequence, CSP, retained Firebase/contact settings, JS syntax and unchanged payload snapshot. Its output is source evidence, not remote APK availability or deployed bytes. The historical v16 runner/logs stay untouched.

Executed source checks now pass: [2,219 offline assertions](validation/site-v1.7/static-check.json) over three pages/66 references and [24 deletion-core tests](validation/site-v1.7/deletion-core-tests.txt). The [source manifest](validation/site-v1.7/source-manifest.json) fingerprints 47 Hosting payloads; the [asset record](validation/site-v1.7/source-assets.json) records all 37 image/vector assets and connects the eleven new copies to the [passing final Lab run](validation/v1.7/ui-sdk-final-menus2/host-report.json), exact Lab hash and 32 capture hashes. The new product captions explicitly say Lab, with an actual guest qualification session, no injected profile and original preferences restored.

The [final CUA localhost matrix](validation/site-v1.7/README.md) now passes 18 FR/EN views at measured CSS widths 320/390/1366, with loaded localized images and actual English navigation through privacy/deletion. Guest deletion remains disabled and confirmation hidden. Its five page captures are local renders. Hosting publication still requires its separate exact-commit/Firebase completion record. Real Google authentication, deletion, two-account interaction, live ads/payments, accessibility certification and retention are not inferred from website tests.

## Preserved 1.6 website delivery and local qualification · 27 October 2026

The published website uses final production 1.6 Android captures for the map, briefing, Home and inventory. The map shows ten levels per page; the briefing explains a fresh attempt; Home resumes an existing practice challenge. Keys and seals enter ordinary Journey at level 12, while the six unlimited workshop challenges remain separate. The page distinguishes local gameplay saves from optional Google identity and makes no Google Play launch claim. [Native release and exact binary](RELEASE_1_6.md) · [Design system](DESIGN_SYSTEM.md) · [Art/capture provenance](ART_AUDIO.md).

| Local check | Recorded result | Boundary |
|---|---|---|
| [Offline source/assets](validation/site-v1.6/static-check.json) | 2,012 assertions passed across three HTML pages and 66 local references | No browser launch, network request or remote APK availability check |
| [CUA browser matrix](validation/site-v1.6/browser-check.json) | 18 states: three pages × FR/EN × 320/390/1366 px; no measured horizontal overflow, wrong-language visible text, multiple visible H1s or broken visible images | Localhost rendering and real page/language controls only; no public Hosting-origin access |
| Language and deletion controls | EN button works; navigation through privacy to deletion preserves EN; checkbox remains unchecked and deletion disabled; five localized homepage image elements load their EN sources | No Google chooser, authentication or real deletion executed |
| [Deletion request core](validation/site-v1.6/deletion-core-tests.txt) | 24 tests passed | Request/configuration contracts; not provider or live API execution |

Six [local page captures](screenshots/site-v1.6/) accompany these results. The [asset record](validation/site-v1.6/source-assets.json) identifies the final native captures, retained historical assets, copied Aurora Atlas environment and original web rule illustration separately. The [source manifest](validation/site-v1.6/source-manifest.json) fingerprints 36 public payloads and explicitly records `network_verified: false`. The [qualification record](validation/site-v1.6/README.md) records the final Firebase deployment and its exact source commit. Local PASS results and deployment confirmation do not constitute complete accessibility review, native-speaker review or identity/commerce qualification.

<a id="15-visual-refresh-and-new-evidence"></a>

## Historical 1.5 visual refresh · 27 October 2026

The repository's 1.5 presentation aligned the homepage, legal pages and GitHub materials with that game revision: night/pastel palette, original companion vectors, seven source Android screenshots and one Android-generated challenge card, feature explanations and a direct route to the signed **1.5.0-rc1 APK**. That website refresh changed no Android binary. Its original assets and existing validation retain their scope. [Visual guide](VISUAL_GUIDE.md) · [Preserved APK/hash](RELEASE.md).

**Published 27 October 2026:** source revision [9eebee1588f2](https://github.com/nabz0r/AndroidHit/commit/9eebee1588f2086ac91baecb6647cc63d21ae10a) was deployed to the existing Firebase site. Independent HTTPS reads confirm all **23 public payloads plus three directory aliases** match the frozen source bytes, with security/cache headers checked. [Delivery result](validation/site-v1.5/delivery.json) · [Scoped acceptance](validation/site-v1.5/README.md). Local screenshots remain local renders; earlier OAuth/CORS observations and owner testimony retain their original scope. This visual deployment does not qualify Android identity, commerce or a real web-account transaction.

That revision's evidence is recorded in [`validation/site-v1.5/`](validation/site-v1.5/), with local website captures in [`screenshots/site-v1.5/`](screenshots/site-v1.5/):

| Local check | Recorded result | Boundary |
|---|---|---|
| [Static routes/assets](validation/site-v1.5/static-check.txt) | Three HTML pages, 44 internal links; preparation mode retained | Reports `productionReady: false`; not a Hosting deployment |
| [Browser matrix](validation/site-v1.5/browser-check.txt) | 18 FR/EN states at 320/390/1366 px; APK actions, localized loaded screenshots, language continuity, keyboard skip and reduced motion | No real Google authentication or live deletion; zero default external requests |
| Missing configuration / deletion handling | Two isolated missing-wallet states and three strictly synthetic DELETE requests; cancel sends none | Synthetic identity/request interception only; not a real account-flow qualification |
| [Deletion request core](validation/site-v1.5/deletion-core.txt) | 24 tests passed | URL/token/HTTP-error/ambiguous-success contracts, not provider execution |

The [asset manifest](validation/site-v1.5/source-assets.json) records exact copies of 12 source assets: seven Android screenshots, one Android-generated challenge card and four original promotional assets. It distinguishes the preceding unchanged-gameplay P1 capture from final-APK captures/exports. The [source manifest](validation/site-v1.5/source-manifest.json) identifies the 23 public source files tested. Its deployment evidence is the dated `delivery.json` linked above. These local and delivery results do not replace or relabel the older 18-state contact/configuration run, and none verifies the 1.6 revision.

The 1.5 redesign retained the supplied public publisher/support and Firebase/wallet fields, `preparation: true`, Google/deletion confirmation boundaries, no Analytics import and no live-purchase claim. The 1.7 source retains those configuration boundaries. A configured deletion page can still affect a real app account after explicit actions; the banner is not a safety lock.

## Configuration, historical observations and publication gates

Production Google/AdMob identifiers are integrated into Android separately. The separate Firebase **Web** configuration is integrated into `public/site-config.js`: project `luma-drift-production`, app `1:369472826910:web:47e79b67e7a3abd0c3080f`, auth domain `luma-drift-production.firebaseapp.com` and its public Web API key. The owner supplied the public publisher label **nabilux** and support email **nabz0r+gaming@gmail.com**. No legal entity is inferred from that label. These contacts appear in both languages, including the static HTML fallback.

The source points to the API `https://luma-wallet-369472826910.europe-west1.run.app`. Its Google sign-in button is configured and enabled; identity modules load only after a click. In the **26–27 October 2026 qualification record**, the preview's five sampled resources returned 200 and matched repository bytes. Firebase's public configuration listed both final domains and the exact preview hostname as authorized, and the owner reported applying the API allowlist update. Independent preflights then returned HTTP 204 with their exact allowed origins. The allowed methods were GET, POST, DELETE and OPTIONS, with Authorization and Content-Type headers. These are historical network observations, not a recheck for 1.6. The preserved 1.5 Android package lacked its wallet origin; a website redesign changes no APK or gameplay code.

The Firebase-generated example supplied by the owner also included an Analytics import and measurement ID. The site integrates only the identity configuration it uses; it still imports no Analytics SDK and makes no default analytics requests. This describes the website's code, not a claim about every setting of the owner's Firebase project.

Before declaring these pages as the final policy or account-deletion route, complete these gates:

1. Keep the supplied public publisher label and support address accurate and review the policy against the actual publisher's obligations. The contact fields are now filled; this is not an identity or legal review. Privacy requests must never use a public GitHub issue containing account details.
2. Retain the owner-reported stable Google sign-in/deletion result and distinguish it from independent runtime proof. On 26–27 October 2026 the five sampled static resources returned HTTP 200 matching the recorded earlier repository revision. Use the stable URLs rather than the temporary preview URL for eventual Play/AdMob declarations; no provider approval is implied.
3. Keep the configured **Web app** identity and Google provider. Both final domains and the actual preview hostname were listed in Firebase's public configuration in the 26–27 October 2026 record. CLI channel deployment normally attempts to add the preview domain; check its presence before adding it manually. For another project, register its own Web app; do not copy an Android `:android:` app ID.
4. Keep the configured HTTPS `walletApiBaseUrl` and allow the site's exact origin for `DELETE` and `OPTIONS`, and `Authorization`/`Content-Type` headers. The recorded final and preview origins passed their preflights on 26–27 October 2026; preserve them and authorize any different future preview origin separately. Wildcard authentication origins and browser cookies are not required.
5. Review the privacy statement against the deployed build, actual log retention, transaction-audit retention, hosting and provider settings. The pages label services under qualification and disclose a planned 365-day minimal audit period. That is a configured retention choice, not a legal requirement. Scheduler/RTDN are not deployed, and no automatic purge or deletion-retry operation has been established. The current maintenance sequence also reconciles Play before purging, so pending Play permissions can prevent it reaching the purge. Qualify both permissions and maintenance before claiming automatic operation.
6. Successful real Google sign-in, recent-authentication rejection, deletion and failure/retry tests on the final domain and live API. The local tests below use synthetic identity; they are not substitutes for these checks.

The **26–27 October 2026 owner report** gives the exact message “Ton compte Luma Drift et son portefeuille serveur ont été supprimés” after guided Web deletion tests on the preview and stable domain. This is owner testimony, without independent observation, a log/screenshot, a measured DELETE status or Firebase Console verification. It does not establish a new 1.6 test. Authenticated wallet GET, payments and successful Android Google login still require their own qualification. [Owner report and evidence boundaries](validation/hosting-preview.md).

The earlier qualification revision was observed at the stable origin on **27 October 2026** with `preparation: true` and closed purchases/live ads. That observation predates the separately recorded 1.5 homepage deployment and does not verify the 1.6 candidate. The owner reported the same successful account flow on the stable domain; the Google Account itself stays intact. The same dated owner report gives `https://luma-drift-production.web.app/privacy/index.html`, saved European-consent draft/settings and the Published state in AdMob, with six languages, refusal/close enabled and GDPR-region targeting. There is no independent console observation or proof of UMP display in the game, app-ads.txt verification, ad readiness or revenue activation. Test the message in the identified production APK while retaining sample ads; the separate `--production` gate and banner removal remain later steps after all release gates pass.

Keep `preparation: true` until these gates pass. This flag only controls the shared qualification banner: it does **not** disable sign-in or deletion. A confirmed deletion against the configured real API can take effect during preview. Use a separate Google test account, not an email alias of the owner's account. Setting the flag to `false` does not rewrite the privacy policy or certify compliance. Review both language versions before changing it.

The [Google Play requirement](https://support.google.com/googleplay/android-developer/answer/13327111?hl=en) includes a functional external deletion pathway for people who have uninstalled the app. In-app instructions alone do not complete that requirement. The configured but unqualified external route must therefore not be declared as a finished store deletion flow.

## External deletion design

The web route uses the official Firebase JavaScript SDK **13.1.0**, pinned to the version shown in the [Firebase setup documentation](https://firebase.google.com/docs/web/setup) when checked on 26 October 2026. The two Google-hosted modules load only after an explicit sign-in click and valid public configuration. There is no Firebase Analytics import.

1. The player opens the deletion page and explicitly selects Google sign-in. Firebase Auth uses `inMemoryPersistence` and an account chooser.
2. The page displays the verified account using text nodes, then requires a separate checkbox acknowledging removal of the app account and wallet.
3. The page requests a fresh Firebase ID token and sends `DELETE /v1/account` over HTTPS with `Authorization: Bearer …`. No token is placed in a query string, HTML, local storage or request body. Requests omit cookies, disable caching and reject redirects.
4. Only a successful HTTP response containing `{ "deleted": true }` displays completion. Authentication expiry requires another sign-in; ambiguous bodies, timeouts, network failure and backend errors never display success. A failed connection does not claim that a previously received deletion was rolled back.
5. The page signs out after completion or cancellation. Closing/canceling the account chooser does not request deletion. The app’s Google Account itself remains intact.

The backend requires authentication within five minutes and implements authenticated account/wallet deletion. This synchronous endpoint does not call Play and can be qualified before Play identity approval. Its minimal hashed transaction audit is distinct from active wallet data. The page explains retained audit information and that local offline saves/exports are separate. Deletion is not a refund request. If an interrupted operation needs recovery, contact support; automatic retry is not running at this stage.

The browser’s content security policy permits only its own assets plus the Google identity modules/frames and Google Cloud endpoint families needed by this prepared flow. If using a custom wallet host or custom Firebase Auth domain, explicitly update the CSP and the strict configuration validator; do not loosen it to arbitrary scripts/origins. HTTP is accepted only for loopback development by the request helper, not by the production preflight.

The public support address appears on the policy, deletion page and footer and offers private correspondence for sign-in/access problems. Mail links encode the literal plus in `nabz0r+gaming@gmail.com` as `%2B`; browser checks verify that the decoded recipient matches exactly. Do not add service-account JSON, OAuth secrets, ID tokens, bank details or passwords to `site-config.js`.

## Hosting and app-ads.txt

Serve the contents of `public/` as the site root, excluding `tests/**`. The directory includes a `.nojekyll` marker and works as static content. The [root Firebase Hosting configuration](../firebase.json) targets site `luma-drift-production`; it is separate from `server/firebase.json`, which configures Firestore. In the **26–27 October 2026 record**, the owner reported cloning channel `qualification` to the same site’s `live` channel. Independent reads then confirmed all five stable-origin resources with the qualification flag preserved; that preview source version was `332f610a434fe4a6`. Those reads do not verify a later website revision. The [French step-by-step guide](DEPLOIEMENT_PAS_A_PAS.md) covers preview qualification and publication without rerunning `firebase init` or overwriting the pages. Its structure is:

```json
{
  "hosting": {
    "site": "luma-drift-production",
    "public": "public",
    "ignore": ["firebase.json", "**/.*", "tests/**", "**/tests/**"],
    "headers": [
      { "source": "**", "headers": [
        { "key": "X-Content-Type-Options", "value": "nosniff" },
        { "key": "Referrer-Policy", "value": "no-referrer" },
        { "key": "X-Frame-Options", "value": "DENY" }
      ]},
      { "source": "/site-config.js", "headers": [
        { "key": "Cache-Control", "value": "no-store" }
      ]}
    ]
  }
}
```

Do not run a deployment against an unreviewed target or imply that saving this example publishes a site. Prefer a real HTTP `Content-Security-Policy` header matching the pages’ policy when configuring the final host; the HTML also supplies a CSP. The final host should serve HTTPS and avoid logging authorization headers or request bodies.

Required final URLs are the site root (developer website), `/privacy/index.html`, `/delete-account/index.html`, and `/app-ads.txt`. Confirm they load anonymously on the actual domain before entering them in Play Console or AdMob. The English pages can be opened with `?lang=en`.

The file contains:

```text
google.com, pub-6914185503996225, DIRECT, f08c47fec0942fa0
```

The publisher ID comes from the owner’s supplied AdMob IDs; the Google exchange/certification format is documented in [Google’s app-ads.txt guide](https://developers.google.com/admob/android/app-ads). Compare it with the personalized snippet shown in the owner’s AdMob account before requesting verification.

**A GitHub project Pages URL such as `https://nabz0r.github.io/AndroidHit/` alone does not put the file at `https://nabz0r.github.io/app-ads.txt`.** AdMob discovers the file from the hostname root. Use a domain/site root that can serve `/app-ads.txt`, or arrange the correct root file on that hostname. See [AdMob’s hosting and crawler rules](https://support.google.com/admob/answer/9363762?hl=en).

## Historical contact/configuration validation · 26–27 October 2026

The following checks passed locally for the earlier contact/configuration revision. The current 1.6 results are in the dedicated section above:

- **24** request/configuration tests: disabled missing configuration, Android-vs-Web app mismatch, auth-domain mismatch, insecure/off-origin URL forms, fresh bearer token, no-cookie/no-query/redirect behavior, authentication errors, rate limits, ambiguous results and non-JSON failure.
- **3** static HTML pages and **37** internal asset/navigation links validated, with both languages, CSP, referrer policy, no inline handlers/scripts and the exact app-ads publisher line.
- **18** recorded pre-redesign browser page states: 3 pages × FR/EN × widths 320, 390 and 1366 pixels. Exact supplied contact, visible qualification banner, configured Google button without clicking it, and no horizontal overflow, default external network requests or browser exceptions. Two additional isolated missing-wallet states retain the disabled control.
- Synthetic browser Google modules + backend responses exercised explicit confirmation, successful deletion, expired-session reauthentication, rejected ambiguous success, cancel-without-delete and public contact/language switching. All external requests were denied by default; only synthetic modules and three synthetic-token deletion requests were fulfilled. No real Google or wallet operation was attempted.
- Desktop and mobile screenshots of all three pages were generated. The landing and account-deletion layouts were visually inspected. These are website captures, not screenshots proving a shipped Google login or production service.

The earlier contact/configuration follow-up ran under **Node 26.7.0, Playwright 1.62.1 and Chromium 149.0.7827.55**. [Recorded pre-redesign scope and report](validation/public-contact-preview.md). The original candidate evidence remains preserved separately.

The historical run used Node.js and an available Playwright/Chromium installation:

```sh
node --test public/tests/deletion-core.test.mjs
node public/tests/check-static.mjs
node public/tests/browser-check.mjs
```

Optional environment variables `LUMA_PLAYWRIGHT_MODULE`, `LUMA_CHROMIUM_EXECUTABLE` and `LUMA_PAGES_QA_DIR` select an existing Playwright package/browser and screenshot directory. The browser check starts an ephemeral loopback-only server and intercepts the synthetic identity/API requests. It does not delete a real account.

For the 1.6 source check, use `python3 scripts/check-site-v16.py` and the request-core command above. The preserved `public/tests/browser-check.mjs` still expects the 1.5 APK, three localized images and the earlier workshop-only copy; it was **not executed as a 1.6 browser check**. The 1.6 rendering evidence instead comes from the separate CUA localhost run linked above. Do not treat the old runner's assumptions or historical synthetic account flow as new 1.6 results.

Publishing the qualification pages at the stable origin while the banner remains visible is separate from commercial activation. `node public/tests/check-static.mjs --production` is an additional, later release gate. The current contact, Web identity and HTTPS API fields are complete, but preparation mode deliberately keeps the release gate closed. Passing the gate still requires the manual review and live checks listed above. The current browser test pins the supplied contact and qualification flag; update those expectations deliberately when changing publication state.

These local checks establish no hosted-site availability, real OAuth popup, live API deletion, production consent approval or legal approval. The later [independent preview HTTP/configuration probes](validation/hosting-preview.md) establish only their stated deployed-read scope.

## Primary references

Checked 26 October 2026: [Google Play account deletion](https://support.google.com/googleplay/android-developer/answer/13327111?hl=en), [Play user-data policy](https://support.google.com/googleplay/android-developer/answer/10144311?hl=en), [Firebase Web setup](https://firebase.google.com/docs/web/setup), [Google sign-in on the web](https://firebase.google.com/docs/auth/web/google-signin), [Firebase privacy](https://firebase.google.com/support/privacy), [Google app-ads.txt format](https://developers.google.com/admob/android/app-ads), [AdMob root hosting and discovery](https://support.google.com/admob/answer/9363762?hl=en).
