# Cloud Run deployment follow-up: public health route

**26 October 2026.** This follow-up records a live deployment issue and a local, tested correction. It does not replace the original 1.4 APK/AAB validation or qualify actual purchases.

## Deployment evidence and failure

The owner reported successful Cloud Run revisions `luma-wallet-00001-tgd` and `luma-wallet-00002-w7t`, with service URL `https://luma-wallet-369472826910.europe-west1.run.app`. The second revision updated `SERVICE_URL` from its bootstrap value to that origin. Database creation output supplied by the owner identifies the default Firestore database as Standard/Native in `europe-west1`; IAM and rules/index deployment success were owner-reported. Cloud configuration was not read through authenticated administrative APIs by this agent.

The owner's first external health request returned a Google-branded HTML 404. Independent unauthenticated HTTPS probes against the supplied live origin reproduced these results before redeployment of the correction:

| GET path | HTTP | Body / content type |
|---|---:|---|
| `/healthz` | 404 | Google HTML error page, `text/html; charset=UTF-8` |
| `/` | 404 | `{"error":{"code":"NOT_FOUND"}}`, JSON |
| `/health` | 404 | `{"error":{"code":"NOT_FOUND"}}`, JSON; new route not deployed yet |
| `/v1/wallet` | 401 | `{"error":{"code":"UNAUTHENTICATED"}}`, JSON |
| `/healthz/` | 404 | `{"error":{"code":"NOT_FOUND"}}`, JSON |

This establishes that the application is reachable and rejects the sampled anonymous wallet request. It does not establish signed-in wallet access, database IAM, successful Play verification, consumption, refunds or deletion.

Google documents reserved URL paths, including some paths ending in `z`, and recommends avoiding all such endings. Together with the route-specific probes, this explains why `/healthz` was intercepted before reaching the application's JSON handler. The runbook should have used a compatible route from the start. [Official Cloud Run known issues](https://docs.cloud.google.com/run/docs/known-issues#reserved-url-paths).

## Correction and local validation

The canonical process-health endpoint is now **`GET /health`**, returning `{"status":"ok"}` with `Cache-Control: no-store`. `/healthz` remains an application-level alias for existing local callers; it must not be used as a public Cloud Run probe. Both exact health paths remain available when normal request slots are full. The health check performs no Firebase authentication, wallet operation or provider/database request. A query string remains rejected and POST is not accepted.

Observed local command, using Node **24.22.1** on macOS arm64:

```sh
node --test --test-concurrency=1 test/*.test.js
```

Result summary: **43 tests, 43 passed, 0 failed, 0 cancelled, 0 skipped, 0 todo**. This includes one new HTTP regression test and the extended existing saturation test. Tests use actual loopback HTTP sockets and injected identity/payment providers. The original 42-case release transcript remains unchanged. No additional Firestore-emulator run was needed for this stateless route correction.

## Owner redeployment

After fetching the corrected `main`, deploy the source from `server/` to the existing Cloud Run service, preserving its current environment. Do not reapply the original bootstrap environment file: `SERVICE_URL` has already been corrected in Cloud Run.

```sh
git -C ~/AndroidHit-deploiement pull --ff-only &&
cd ~/AndroidHit-deploiement/server &&
gcloud run deploy luma-wallet --source=. \
  --project=luma-drift-production --region=europe-west1 \
  --service-account=luma-wallet@luma-drift-production.iam.gserviceaccount.com &&
curl --fail-with-body --max-time 60 --write-out '\nHTTP %{http_code}\n' \
  https://luma-wallet-369472826910.europe-west1.run.app/health
```

## Verification after owner redeployment

The owner reported successful `/health` JSON HTTP 200 after fetching and deploying the correction. Independent external HTTPS probes then confirmed:

| Request | HTTP | Observed result |
|---|---:|---|
| `GET /health` | 200 | `{"status":"ok"}` |
| `GET /v1/wallet` without credentials | 401 | `{"error":{"code":"UNAUTHENTICATED"}}` |
| `OPTIONS /v1/account`, origin `https://luma-drift-production.web.app`, requested method DELETE and headers authorization/content-type | 204 | `Access-Control-Allow-Origin: https://luma-drift-production.web.app` |

The public routing correction is therefore verified on the deployed service. No authenticated wallet, purchase, successful deletion or hosted public page is established by these probes.
