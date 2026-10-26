# Public developer, privacy and account-deletion pages

Prepared on **26 October 2026**, with public contact/configuration checks and Hosting preview deployment recorded on **27 October 2026**, for the production application `com.androidhit.lumadrift`. The static site is in [`public/`](../public/). The [qualification preview](https://luma-drift-production--qualification-uht107xg.web.app) serves the pages; the final origin currently returns 404. This is not yet a completed Google Play privacy/deletion submission. [Deployed preview evidence](validation/hosting-preview.md).

The pages use the Luma Drift palette, original code-drawn puzzle artwork, responsive layouts, keyboard focus, reduced-motion handling and French/English controls. The Google sign-in control reuses the app’s unmodified official Google G asset; it is Google branding, not Luma Drift artwork. They need no build framework, remote font or analytics package. Switching language uses a URL parameter, without a cookie or local-storage entry.

| File | Purpose |
|---|---|
| [`public/index.html`](../public/index.html) | Developer landing page and privacy/account links |
| [`public/privacy/index.html`](../public/privacy/index.html) | FR/EN privacy information, data paths and deletion/retention explanation |
| [`public/delete-account/index.html`](../public/delete-account/index.html) | In-app instructions and prepared authenticated external deletion flow |
| [`public/site-config.js`](../public/site-config.js) | Public publisher, contact, Firebase Web and wallet settings |
| [`public/app-ads.txt`](../public/app-ads.txt) | Supplied publisher’s authorized advertising seller |
| [`public/tests/`](../public/tests/) | Static, request-boundary and browser checks; exclude from hosting |

## Present state and publication gates

Production Google/AdMob identifiers are integrated into Android separately. The separate Firebase **Web** configuration is integrated into `public/site-config.js`: project `luma-drift-production`, app `1:369472826910:web:47e79b67e7a3abd0c3080f`, auth domain `luma-drift-production.firebaseapp.com` and its public Web API key. The owner supplied the public publisher label **nabilux** and support email **nabz0r+gaming@gmail.com**. No legal entity is inferred from that label. These contacts appear in both languages, including the static HTML fallback.

The site now points to the deployed API `https://luma-wallet-369472826910.europe-west1.run.app`. Its Google sign-in button is configured and enabled; identity modules load only after a click. The qualification preview's five sampled resources returned 200 and matched repository bytes. Firebase's public configuration confirms both final domains and the exact preview hostname are authorized. The owner has applied the API allowlist update. Independent preflights from the final and preview origins both returned HTTP 204 with their exact allowed origins. The allowed methods are GET, POST, DELETE and OPTIONS, with Authorization and Content-Type headers. The existing Android candidate is unchanged and still lacks its wallet origin.

The Firebase-generated example supplied by the owner also included an Analytics import and measurement ID. The site integrates only the identity configuration it uses; it still imports no Analytics SDK and makes no default analytics requests. This describes the website's code, not a claim about every setting of the owner's Firebase project.

Before declaring these pages as the final policy or account-deletion route, complete these gates:

1. Keep the supplied public publisher label and support address accurate and review the policy against the actual publisher's obligations. The contact fields are now filled; this is not an identity or legal review. Privacy requests must never use a public GitHub issue containing account details.
2. Qualify the deployed preview, then publish and verify the final hosting origin. The final project `web.app` origin currently returns 404; do not enter the temporary preview URL in Play or AdMob.
3. Keep the configured **Web app** identity and Google provider. Both final domains and the actual preview hostname are already confirmed in Firebase's public configuration. CLI channel deployment normally attempts to add the preview domain; check its presence before adding it manually. For another project, register its own Web app; do not copy an Android `:android:` app ID.
4. Keep the configured HTTPS `walletApiBaseUrl` and allow the site's exact origin for `DELETE` and `OPTIONS`, and `Authorization`/`Content-Type` headers. The current final and preview origins have both passed their preflights; preserve them and authorize any different future preview origin separately. Wildcard authentication origins and browser cookies are not required.
5. Review the privacy statement against the deployed build, actual log retention, transaction-audit retention, hosting and provider settings. The pages label services under qualification and disclose a planned 365-day minimal audit period. That is a configured retention choice, not a legal requirement. Scheduler/RTDN are not deployed, and no automatic purge or deletion-retry operation has been established. The current maintenance sequence also reconciles Play before purging, so pending Play permissions can prevent it reaching the purge. Qualify both permissions and maintenance before claiming automatic operation.
6. Successful real Google sign-in, recent-authentication rejection, deletion and failure/retry tests on the final domain and live API. The local tests below use synthetic identity; they are not substitutes for these checks.

The owner reports reaching “Identity verified” after Google sign-in on the preview, with deletion confirmation unchecked. The agent did not observe the flow or receive a log/screenshot. Server deletion, authenticated wallet access, payments and Android Google login remain unqualified. [Owner report and evidence boundaries](validation/hosting-preview.md).

The next actual-service check explicitly tests app-account deletion with a distinct Google test account. Choose “Cancel and sign out”, sign in again, and within five minutes select the confirmation checkbox and “Permanently delete my account”. This can delete the actual app account and wallet while leaving the Google Account intact. No deletion result or Firebase Console state is established yet.

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

Serve the contents of `public/` as the site root, excluding `tests/**`. The directory includes a `.nojekyll` marker and works as static content. The [root Firebase Hosting configuration](../firebase.json) targets site `luma-drift-production`; it is separate from `server/firebase.json`, which configures Firestore. The owner deployed channel `qualification`, version `332f610a434fe4a6`; the final site remains unpublished. The [French step-by-step guide](DEPLOIEMENT_PAS_A_PAS.md) covers preview qualification and publication without rerunning `firebase init` or overwriting the pages. Its structure is:

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

## Executed validation

The following checks passed locally:

- **24** request/configuration tests: disabled missing configuration, Android-vs-Web app mismatch, auth-domain mismatch, insecure/off-origin URL forms, fresh bearer token, no-cookie/no-query/redirect behavior, authentication errors, rate limits, ambiguous results and non-JSON failure.
- **3** static HTML pages and **37** internal asset/navigation links validated, with both languages, CSP, referrer policy, no inline handlers/scripts and the exact app-ads publisher line.
- **18** current browser page states: 3 pages × FR/EN × widths 320, 390 and 1366 pixels. Exact supplied contact, visible qualification banner, configured Google button without clicking it, and no horizontal overflow, default external network requests or browser exceptions. Two additional isolated missing-wallet states retain the disabled control.
- Synthetic browser Google modules + backend responses exercised explicit confirmation, successful deletion, expired-session reauthentication, rejected ambiguous success, cancel-without-delete and public contact/language switching. All external requests were denied by default; only synthetic modules and three synthetic-token deletion requests were fulfilled. No real Google or wallet operation was attempted.
- Desktop and mobile screenshots of all three pages were generated. The landing and account-deletion layouts were visually inspected. These are website captures, not screenshots proving a shipped Google login or production service.

The contact/configuration follow-up ran under **Node 26.7.0, Playwright 1.62.1 and Chromium 149.0.7827.55**. [Current scope and report](validation/public-contact-preview.md). The original candidate evidence remains preserved separately.

Reproduce with Node.js and an available Playwright/Chromium installation:

```sh
node --test public/tests/deletion-core.test.mjs
node public/tests/check-static.mjs
node public/tests/browser-check.mjs
```

Optional environment variables `LUMA_PLAYWRIGHT_MODULE`, `LUMA_CHROMIUM_EXECUTABLE` and `LUMA_PAGES_QA_DIR` select an existing Playwright package/browser and screenshot directory. The browser check starts an ephemeral loopback-only server and intercepts the synthetic identity/API requests. It does not delete a real account.

`node public/tests/check-static.mjs --production` is an additional release gate. The current contact, Web identity and HTTPS API fields are complete, but preparation mode deliberately keeps the release gate closed. Passing the gate still requires the manual review and live checks listed above. The current browser test pins the supplied contact and qualification flag; update those expectations deliberately when changing publication state.

These local checks establish no hosted-site availability, real OAuth popup, live API deletion, production consent approval or legal approval. The later [independent preview HTTP/configuration probes](validation/hosting-preview.md) establish only their stated deployed-read scope.

## Primary references

Checked 26 October 2026: [Google Play account deletion](https://support.google.com/googleplay/android-developer/answer/13327111?hl=en), [Play user-data policy](https://support.google.com/googleplay/android-developer/answer/10144311?hl=en), [Firebase Web setup](https://firebase.google.com/docs/web/setup), [Google sign-in on the web](https://firebase.google.com/docs/auth/web/google-signin), [Firebase privacy](https://firebase.google.com/support/privacy), [Google app-ads.txt format](https://developers.google.com/admob/android/app-ads), [AdMob root hosting and discovery](https://support.google.com/admob/answer/9363762?hl=en).
