# Public developer, privacy and account-deletion pages

Prepared on **26 October 2026** for the production application `com.androidhit.lumadrift`. The static site is in [`public/`](../public/). It is **not hosted by this commit** and is not yet a completed Google Play privacy/deletion submission.

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

Production Google/AdMob identifiers are being integrated into Android separately. The supplied Firebase Android configuration is **not a Firebase Web app configuration**. The website intentionally keeps the web sign-in control disabled until all its required public configuration is present. It makes no request for account deletion in this state and does not show a fabricated success.

Before public publication as the policy or account-deletion route, the owner must supply or configure:

1. The actual publisher’s legal name and a private support email. No name, email address, processing promise or legal owner has been invented. Privacy requests must never use a public GitHub issue containing account details.
2. A hosting origin/domain. Firebase Hosting can serve this directory on the production project’s `web.app` subdomain, or an owner-controlled domain. No such URL is claimed to be live yet.
3. A **Web app** registered in the same Firebase project, `luma-drift-production`, for external account deletion. Copy its public `apiKey`, `authDomain`, `projectId` and `appId` into `site-config.js`. Its app ID must contain `:web:`. Do not copy the Android `:android:` app ID. Enable the Google authentication provider and authorize the published domain. The Android Google provider/client configuration does not replace this Web registration.
4. The deployed HTTPS wallet API URL in `walletApiBaseUrl`. The backend must allow the site’s exact origin for `DELETE` and `OPTIONS`, and `Authorization`/`Content-Type` headers. Wildcard authentication origins and browser cookies are not required.
5. A reviewed privacy statement matching the deployed build, actual log retention, transaction-audit retention, hosting and provider settings. The current page visibly labels production preparation and planned services; update those statements when the services actually launch. The backend’s prepared audit default is 365 days with a scheduled purge. The owner must validate that retention choice and monitor purge completion; this is not a claim that a production purge job is running.
6. Successful real Google sign-in, recent-authentication rejection, deletion and failure/retry tests on the final domain and live API. The local tests below use synthetic identity; they are not substitutes for these checks.

Keep `preparation: true` until these gates pass. Setting it to `false` hides the shared preparation banner; it does **not** rewrite the privacy policy or certify compliance. Review both language versions before changing it.

The [Google Play requirement](https://support.google.com/googleplay/android-developer/answer/13327111?hl=en) includes a functional external deletion pathway for people who have uninstalled the app. In-app instructions alone do not complete that requirement. The current disabled external route must therefore not be declared as a finished store deletion flow.

## External deletion design

The web route uses the official Firebase JavaScript SDK **13.1.0**, pinned to the version shown in the [Firebase setup documentation](https://firebase.google.com/docs/web/setup) when checked on 26 October 2026. The two Google-hosted modules load only after an explicit sign-in click and valid public configuration. There is no Firebase Analytics import.

1. The player opens the deletion page and explicitly selects Google sign-in. Firebase Auth uses `inMemoryPersistence` and an account chooser.
2. The page displays the verified account using text nodes, then requires a separate checkbox acknowledging removal of the app account and wallet.
3. The page requests a fresh Firebase ID token and sends `DELETE /v1/account` over HTTPS with `Authorization: Bearer …`. No token is placed in a query string, HTML, local storage or request body. Requests omit cookies, disable caching and reject redirects.
4. Only a successful HTTP response containing `{ "deleted": true }` displays completion. Authentication expiry requires another sign-in; ambiguous bodies, timeouts, network failure and backend errors never display success. A failed connection does not claim that a previously received deletion was rolled back.
5. The page signs out after completion or cancellation. Closing/canceling the account chooser does not request deletion. The app’s Google Account itself remains intact.

The backend requires recent authentication and performs the actual authenticated account/wallet deletion. Its minimal hashed transaction audit is distinct from active wallet data. The page explains retained audit information and that local offline saves/exports are separate. Deletion is not a refund request.

The browser’s content security policy permits only its own assets plus the Google identity modules/frames and Google Cloud endpoint families needed by this prepared flow. If using a custom wallet host or custom Firebase Auth domain, explicitly update the CSP and the strict configuration validator; do not loosen it to arbitrary scripts/origins. HTTP is accepted only for loopback development by the request helper, not by the production preflight.

A configured private support address appears on the policy, deletion page and footer. It offers a private contact for sign-in/access problems. Do not add service-account JSON, OAuth secrets, ID tokens, bank details or passwords to `site-config.js`.

## Hosting and app-ads.txt

Serve the contents of `public/` as the site root, excluding `tests/**`. The directory includes a `.nojekyll` marker and works as static content. The repository now includes a [root Firebase Hosting configuration](../firebase.json), targeting site `luma-drift-production`; it is separate from `server/firebase.json`, which configures Firestore. No hosting deployment has been executed. The [French step-by-step guide](DEPLOIEMENT_PAS_A_PAS.md) covers setup, preview qualification and publication without rerunning `firebase init` or overwriting the pages. Its structure is:

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
- **18** browser page states: 3 pages × FR/EN × widths 320, 390 and 1366 pixels. No horizontal overflow, default external network requests or browser exceptions. The narrow-screen decorative-orbit overflow found by the first check was fixed and the entire matrix passed again.
- Synthetic browser Google modules + backend responses exercised explicit confirmation, successful deletion, expired-session reauthentication, rejected ambiguous success, cancel-without-delete and configured private contact/language switching.
- Desktop and mobile screenshots of all three pages were generated. The landing and account-deletion layouts were visually inspected. These are website captures, not screenshots proving a shipped Google login or production service.

Reproduce with Node.js and an available Playwright/Chromium installation:

```sh
node --test public/tests/deletion-core.test.mjs
node public/tests/check-static.mjs
node public/tests/browser-check.mjs
```

Optional environment variables `LUMA_PLAYWRIGHT_MODULE`, `LUMA_CHROMIUM_EXECUTABLE` and `LUMA_PAGES_QA_DIR` select an existing Playwright package/browser and screenshot directory. The browser check starts an ephemeral loopback-only server and intercepts the synthetic identity/API requests. It does not delete a real account.

`node public/tests/check-static.mjs --production` is an additional release gate. It intentionally fails while preparation mode, legal contact, Web identity or HTTPS API configuration is missing. Passing it still requires the manual review and live checks listed above.

No hosted-site availability, real OAuth popup, live API deletion, production consent approval or legal approval is established by these checks.

## Primary references

Checked 26 October 2026: [Google Play account deletion](https://support.google.com/googleplay/android-developer/answer/13327111?hl=en), [Play user-data policy](https://support.google.com/googleplay/android-developer/answer/10144311?hl=en), [Firebase Web setup](https://firebase.google.com/docs/web/setup), [Google sign-in on the web](https://firebase.google.com/docs/auth/web/google-signin), [Firebase privacy](https://firebase.google.com/support/privacy), [Google app-ads.txt format](https://developers.google.com/admob/android/app-ads), [AdMob root hosting and discovery](https://support.google.com/admob/answer/9363762?hl=en).
