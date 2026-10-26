# Firebase Hosting preview deployment

**27 October 2026.** The owner deployed the `qualification` preview channel on site `luma-drift-production`. [Observed HTTP/configuration results](hosting-preview.json).

- Preview: <https://luma-drift-production--qualification-uht107xg.web.app>
- Owner-reported Hosting version: `332f610a434fe4a6`.
- Owner-reported expiration text: `2026-11-02 23:05:45`; the supplied CLI output does not identify its timezone.
- This channel deploy does not publish the final site. The CLI's separate `Hosting URL` line is not evidence that the final origin serves these pages.

## Independent read-only observations

All five resources returned HTTP 200 and exactly matched repository bytes: `/`, `/privacy/index.html`, `/delete-account/index.html`, `/site-config.js` and `/app-ads.txt`. Their responses supplied `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY` and `Referrer-Policy: no-referrer`. The served configuration contains the supplied nabilux/contact, the actual Cloud Run origin and `preparation: true`; the advertising seller line matches the repository. These HTTP checks do not execute browser identity or establish an AdMob crawler approval.

The public Firebase SDK configuration endpoint returned HTTP 200 for project number `369472826910`, with both final domains and the exact preview hostname already authorized. No user token, login or administrative project change was involved. Firebase CLI 15.34.1 normally synchronizes preview domains with Auth; its [official source](https://github.com/firebase/firebase-tools/blob/v15.34.1/src/commands/hosting-channel-deploy.ts) attempts this during channel deployment. No manual duplicate-domain entry is needed here.

An actual `OPTIONS /v1/account` request to Cloud Run, with the preview Origin and requested DELETE/authorization/content-type, returned **403 `ORIGIN_NOT_ALLOWED`**, without `Access-Control-Allow-Origin`. No account deletion was requested. The previously observed final-site preflight remains a separate check. [Cloud Run evidence](cloud-run-health-route.md).

## Owner CORS update — completed

Add the preview HTTPS origin to `ALLOWED_WEB_ORIGINS`, preserving the final origin and all other environment variables. From Cloud Shell:

```sh
gcloud run services update luma-wallet \
  --project=luma-drift-production \
  --region=europe-west1 \
  --update-env-vars='^|^ALLOWED_WEB_ORIGINS=https://luma-drift-production.web.app,https://luma-drift-production--qualification-uht107xg.web.app'
```

The alternate dictionary delimiter keeps the comma inside this single environment variable's value. [Official gcloud escaping](https://docs.cloud.google.com/sdk/gcloud/reference/topic/escaping). The owner has now reported this command completed with `Done`; the agent did not run an administrative update.

Independent follow-up probes on 27 October confirmed both final and preview `OPTIONS /v1/account` return **204**, each with its exact `Access-Control-Allow-Origin`, allowed methods `GET, POST, DELETE, OPTIONS` and headers `Authorization, Content-Type`. `/health` still returns **200** with `{"status":"ok"}`. [Follow-up JSON](hosting-preview-cors.json). The earlier 403 in the initial report is preserved as the pre-update observation.

## Owner-reported Google login — succeeded

On 27 October the owner reported the real preview displayed **« Identité vérifiée »** after Google sign-in, without ticking the deletion confirmation. [Scoped owner report](hosting-google-login.json). This is owner-reported UI evidence: the agent did not inspect the account, browser session, ID token, Firebase user record or authenticated server logs. It establishes neither an Android login nor authenticated wallet access, deletion or purchases. No personal account identifier or token was requested or recorded.

The next qualification step is the separate, explicit deletion test on the distinct Google test account. First select **« Annuler et me déconnecter »** and sign back in with the same test account, so the server's five-minute recent-authentication requirement is met. Then read and select the confirmation and press **« Supprimer définitivement mon compte »**. This deletes the app account/wallet through the real server; it does not delete the Google Account. Record the actual final message before declaring success. A plus-tag email alias is not a distinct Google account, and the banner does not neutralize deletion against the real service.

The owner-reported Web login is the only new authenticated result. No server deletion, purchase, live ad, Scheduler/RTDN delivery or audit purge is established. The existing Android APK/AAB remain unchanged.
