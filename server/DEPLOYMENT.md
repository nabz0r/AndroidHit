# Production deployment runbook

**Prepared instructions only. No commands below were run against the owner's cloud project.** They create billable resources and change IAM. Run them after the owner has linked Cloud Billing/Blaze, finished Play merchant onboarding, and approved the deployment. Do not download, upload, paste or commit a service-account JSON private key. Cloud Run uses an attached managed identity.

## Prerequisites and identifiers

- Owner project: `luma-drift-production` (Firebase and underlying Google Cloud project).
- Production Android package: `com.androidhit.lumadrift`, never the `.sandbox` Lab package.
- Firebase Authentication Google provider enabled, correct OAuth Web client ID and Play signing certificate registered.
- Play Console internal release uploaded; exactly `luma_gold_500`, `luma_gold_1400`, `luma_gold_3600` active as one-time **Buy** products with multi-quantity disabled. Real money prices/countries configured in Play.
- Owner developer website, support address, privacy, deletion and terms pages finalized. The external deletion site's exact HTTPS origin goes in `ALLOWED_WEB_ORIGINS`, and Firebase's Authentication authorized-domain list must contain its host. A full site URL with a path is not a CORS origin.
- Local Node 24, gcloud CLI and Firebase CLI. Commands below use a dedicated new project; do not replace another application's Firestore rules with this deny-all ruleset.

Use budget alerts, an initial maximum instance count, provider quotas, and monitoring. Budget alerts are not hard spending caps. Firestore, Cloud Run, Cloud Build, Artifact Registry, Pub/Sub, Scheduler and logging may generate costs even when the app is small.

## 1. Enable APIs and create managed identities

Run from the `server` directory. The task-specific shell variables below are public identifiers:

```sh
task_project=luma-drift-production
task_region=europe-west1
task_number=$(gcloud projects describe "$task_project" --format='value(projectNumber)')
task_runtime="luma-wallet@$task_project.iam.gserviceaccount.com"
task_push="luma-rtdn@$task_project.iam.gserviceaccount.com"
task_scheduler="luma-scheduler@$task_project.iam.gserviceaccount.com"

gcloud services enable run.googleapis.com cloudbuild.googleapis.com \
  artifactregistry.googleapis.com firestore.googleapis.com \
  identitytoolkit.googleapis.com androidpublisher.googleapis.com \
  pubsub.googleapis.com cloudscheduler.googleapis.com iam.googleapis.com \
  iamcredentials.googleapis.com --project "$task_project"

gcloud iam service-accounts create luma-wallet --project "$task_project"
gcloud iam service-accounts create luma-rtdn --project "$task_project"
gcloud iam service-accounts create luma-scheduler --project "$task_project"

gcloud projects add-iam-policy-binding "$task_project" \
  --member="serviceAccount:$task_runtime" --role=roles/datastore.user

gcloud iam roles create lumaWalletAuth --project "$task_project" \
  --title='Luma wallet user verification and deletion' \
  --permissions=firebaseauth.users.get,firebaseauth.users.delete --stage=GA
gcloud projects add-iam-policy-binding "$task_project" \
  --member="serviceAccount:$task_runtime" \
  --role="projects/$task_project/roles/lumaWalletAuth"
```

The runtime needs Firestore data access and only Firebase user get/delete permissions, not Editor, Owner, or Firebase project administration. The custom permissions correspond to [Identity Platform's user APIs](https://cloud.google.com/identity-platform/docs/access-control). Pub/Sub and Scheduler identities have no wallet, Firebase-user or Play permissions.

In Play Console **Users and permissions**, invite the runtime service-account **email** and scope access to Luma Drift. Grant the documented billing access needed to view financial/order data and manage orders/subscriptions; do not grant account administrator. This is separate from Google Cloud IAM. Enable the Android Publisher API in the same project. [Official API access setup](https://developers.google.com/android-publisher/getting_started).

## 2. Create the database and close all direct client access

If the `(default)` Firestore Native database already exists, check its location and skip the create command. Database location cannot be casually changed later.

```sh
gcloud firestore databases create --database='(default)' --location="$task_region" \
  --type=firestore-native --project "$task_project"
npx --yes firebase-tools@15.34.1 deploy --only firestore:rules,firestore:indexes \
  --project "$task_project" --config firebase.json
```

Wait until indexes finish building. `firestore.rules` denies **every Firebase client read and write**, including signed-in players. The Admin SDK's managed identity accesses data through IAM instead. Never start with Firestore's temporary test-mode public rules: the active ledger contains opaque purchase tokens. Verify client SDK and unauthenticated REST reads/writes receive permission denied before enabling purchases. These deny rules were exercised against the local emulator; the owner must still verify the deployed rules.

## 3. Build/deploy Cloud Run, then record its canonical HTTPS URL

Cloud Run must expose the Firebase-authenticated client API, so the service permits unauthenticated network invocation at the Cloud Run IAM layer. **Every wallet route independently verifies Firebase Google identity**, and internal routes independently verify their specific Google OIDC identity. `allow-unauthenticated` does not bypass those application checks.

The first revision can use a deliberately non-working internal OIDC audience until the actual service URL is known. Do not put this temporary URL into the Android build or external deletion page:

```sh
gcloud run deploy luma-wallet --source . --project "$task_project" --region "$task_region" \
  --service-account="$task_runtime" --allow-unauthenticated \
  --min-instances=0 --max-instances=3 --concurrency=32 \
  --memory=512Mi --cpu=1 --timeout=300 \
  --set-env-vars="GOOGLE_CLOUD_PROJECT=$task_project,PLAY_PACKAGE_NAME=com.androidhit.lumadrift,SERVICE_URL=https://bootstrap.invalid,PUBSUB_PUSH_SERVICE_ACCOUNT=$task_push,SCHEDULER_SERVICE_ACCOUNT=$task_scheduler,RTDN_SUBSCRIPTION=projects/$task_project/subscriptions/luma-rtdn,AUDIT_RETENTION_DAYS=365,ALLOW_TEST_PURCHASES=true"

task_service_url=$(gcloud run services describe luma-wallet --project "$task_project" \
  --region "$task_region" --format='value(status.url)')
gcloud run services update luma-wallet --project "$task_project" --region "$task_region" \
  --update-env-vars="SERVICE_URL=$task_service_url,ALLOWED_WEB_ORIGINS=https://nabz0r.github.io"
```

Replace the example public origin with the actual owner site. Source deployment uses the supplied Dockerfile (Node 24, non-root process). The deployer/build identities need the [source deployment permissions](https://cloud.google.com/run/docs/deploying-source-code); grant these to deployment identities, not to the runtime. If organization policy forbids unauthenticated invocation, use an approved gateway that forwards Firebase tokens; do not weaken the verifier to work around that policy.

Register this URL in the Android production configuration only after all steps and internal-track tests pass. No endpoint was invented or embedded as if it already existed in this deliverable.

## 4. Configure authenticated Pub/Sub RTDN

```sh
gcloud pubsub topics create luma-rtdn --project "$task_project"
gcloud pubsub topics create luma-rtdn-dead-letter --project "$task_project"
gcloud pubsub topics add-iam-policy-binding luma-rtdn --project "$task_project" \
  --member=serviceAccount:google-play-developer-notifications@system.gserviceaccount.com \
  --role=roles/pubsub.publisher

gcloud iam service-accounts add-iam-policy-binding "$task_push" --project "$task_project" \
  --member="serviceAccount:service-$task_number@gcp-sa-pubsub.iam.gserviceaccount.com" \
  --role=roles/iam.serviceAccountTokenCreator

gcloud pubsub subscriptions create luma-rtdn --project "$task_project" \
  --topic=luma-rtdn --push-endpoint="$task_service_url/internal/rtdn" \
  --push-auth-service-account="$task_push" --push-auth-token-audience="$task_service_url" \
  --min-retry-delay=10s --max-retry-delay=600s \
  --dead-letter-topic=luma-rtdn-dead-letter --max-delivery-attempts=20
gcloud pubsub topics add-iam-policy-binding luma-rtdn-dead-letter --project "$task_project" \
  --member="serviceAccount:service-$task_number@gcp-sa-pubsub.iam.gserviceaccount.com" \
  --role=roles/pubsub.publisher
gcloud pubsub subscriptions add-iam-policy-binding luma-rtdn --project "$task_project" \
  --member="serviceAccount:service-$task_number@gcp-sa-pubsub.iam.gserviceaccount.com" \
  --role=roles/pubsub.subscriber
gcloud pubsub subscriptions create luma-rtdn-dead-letter-review --project "$task_project" \
  --topic=luma-rtdn-dead-letter
```

Grant the deploying operator `iam.serviceAccounts.actAs` on the push identity if required; do not download its key. [Pub/Sub authentication configuration](https://cloud.google.com/pubsub/docs/authenticate-push-subscriptions).

In Play Console monetization settings, enter topic `projects/luma-drift-production/topics/luma-rtdn`, enable one-time product/void notifications and send a test notification. Check a successful delivery with the exact subscription field and OIDC audience. Monitor the dead-letter review subscription, with a documented redrive/reconciliation procedure. Pub/Sub retries do not replace the scheduled reconciliation job.

## 5. Schedule maintenance, retries, audit purge and alerts

```sh
gcloud scheduler jobs create http luma-wallet-maintenance --project "$task_project" \
  --location="$task_region" --schedule='*/5 * * * *' --time-zone=Etc/UTC \
  --uri="$task_service_url/internal/maintenance" --http-method=POST \
  --headers=Content-Type=application/json --message-body='{}' \
  --oidc-service-account-email="$task_scheduler" --oidc-token-audience="$task_service_url" \
  --attempt-deadline=180s --max-retry-attempts=5 \
  --min-backoff=10s --max-backoff=300s
gcloud scheduler jobs run luma-wallet-maintenance --project "$task_project" --location="$task_region"
```

Keep the Google-managed Cloud Scheduler service agent's normal role intact; the job uses `luma-scheduler`, not that service agent, as its identity. [Scheduler OIDC guidance](https://cloud.google.com/scheduler/docs/http-target-auth).

Alert on repeated 5xx, scheduler failures, Pub/Sub oldest unacked age/dead letters, purchases with overdue `nextConsumeAtMs`, pending/deletion backlog, audit purge lag and billing budgets. Keep request-body logging off; never log ID tokens, purchase tokens or user emails. Configure Cloud Logging, Firestore backups/PITR, and any export retention deliberately and reflect it in the public privacy policy. `AUDIT_RETENTION_DAYS=365` is enforced by the scheduled bounded purge; no Firestore TTL enablement is implied. If volume reaches the 100-job/page limits or the initial three-instance cap, review quotas and observed latency before increasing capacity. The current tests do not establish a production throughput figure.

## 6. Internal-track acceptance gate before real sales

Keep Google Play license testers separate from ordinary track membership and use official test payment instruments. Test approved, declined and delayed/cancelled purchases; bind each to the correct Google/Firebase UID. Confirm exactly one credit on duplicate callbacks, offline retries, app restart and two devices. Verify Google consumption/acknowledgement, restore after clearing app storage, permanent companion/skin ownership, logout isolation, and insufficient funds. Confirm a second Google account cannot claim the first account's token.

Exercise a real license-test refund/void and inspect the wallet debt/revocation once, including a spent purchase. Test authenticated RTDN, blocked forged Pub/Sub requests, the maintenance recovery path, direct Firestore client denials, and recent-login account deletion from Android and the external web page. Confirm required privacy/terms/contact pages and Play Data safety answers describe what is actually deployed.

Only after recording those results should the Android billing gate receive the deployed endpoint and be enabled. The implementation's local tests prove its modeled behavior and emulator transactions; they do not prove that the owner's Play/IAM/merchant/project configuration is working.
