# Public contact and preview preparation

Recorded **27 October 2026**. Website-only follow-up to the original 1.4 candidate; no Android package was rebuilt. [Machine-readable browser result](public-contact-preview.json).

## Configured state

- Public publisher label: **nabilux**. No legal person or company is inferred from the label.
- Public support address: **nabz0r+gaming@gmail.com**, visible in FR/EN pages and static HTML fallbacks. Every tested email link decodes to this exact recipient; the plus is encoded as `%2B`.
- API origin: `https://luma-wallet-369472826910.europe-west1.run.app`, matching the already deployed service. [Separate live anonymous probes](cloud-run-health-route.md).
- Firebase Web identity is configured; Google sign-in is available only after an explicit action. `preparation: true` keeps the qualification banner visible and does not disable real operations.
- Both final Firebase domains were owner-confirmed. A preview hostname still needs separate Firebase Auth and exact API-origin authorization before real sign-in tests.

## Executed local checks

| Check | Observed result |
|---|---|
| Request/configuration tests | 24 passed, 0 failed |
| Static links and pages | 3 HTML pages, 37 local references passed |
| Configured browser rendering | 18 states: 3 pages × FR/EN × 320/390/1366 px; correct public contact and banner; no overflow, browser exceptions or default external requests |
| Missing API configuration | Separate FR/EN contexts retain the disabled Google control and send no deletion request |
| Synthetic identity/deletion | Explicit confirmation, successful response, expired session, invalid success and cancellation checked; exactly 3 synthetic deletion requests |
| Visual review | Local mobile landing/deletion pages and desktop privacy page inspected from the six generated captures |

The browser run used **Node 26.7.0, Playwright 1.62.1 and Chromium 149.0.7827.55**. Core/static checks used Node 26.7.0. The final browser run was repeated after the email-link and interrupted-deletion copy changes. All external browser requests were denied by default; only in-memory synthetic Firebase modules and synthetic-token API responses were fulfilled. The real-configuration Google button was never clicked.

## Qualification still required

No Hosting deployment, real OAuth login, account deletion, Play purchase, live ad or scheduled purge is established by these local checks. The production static gate remains deliberately closed by `preparation: true`. The site is ready for a Hosting preview, followed by actual Google/deletion tests using a distinct test account. Confirmed deletion can affect that real account during preview.

The pages disclose the configured/planned 365-day minimal audit period without claiming an active purge. Scheduler/RTDN and Play permissions are pending; maintenance currently calls Play reconciliation before purge, which must be qualified before automatic retention enforcement is advertised. Synchronous account deletion can be tested independently of Play approval; interrupted-operation retries are not currently scheduled.

The existing APK still has no wallet origin, forces sample ads and blocks checkout. This website configuration does not activate sales or replace the Android/Play acceptance checks.
