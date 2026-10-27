# Optional Google sign-in

For production account creation, the exact public Firebase fields and the Play/AdMob handoff, start with [production onboarding](PRODUCTION_ONBOARDING.md). The Lab setup below remains separate from the production package.

The app integrates **Android Credential Manager 1.6.0**, **Google ID 1.2.1** and **Firebase Authentication 25.0.0**. The **1.6 production candidate contains the owner's public configuration** for `com.androidhit.lumadrift`, Firebase project `luma-drift-production`, with the supplied Web OAuth client ID. Its separately built Lab has no production identity asset. Guest play remains available without an account. The real Google credential-provider UI was opened and cancelled where recorded; completed Firebase login, sign-out and deletion still require actual-account qualification. [Current evidence](VALIDATION.md).

Production public configuration is in `app/src/production/assets/identity-config.json`. The ignored main-source configuration described below remains an optional local development override; do not point the Lab at production user data. Play App Signing's public certificate fingerprints must also be registered once available.

## What happens

The home profile card or Settings → Account opens a native screen. With no configuration, it explains the missing setup and keeps the guest state. It does not invent a user, open an unrelated project's login, or ask for a Google password.

Once the owner configures the correct project, an explicit Sign in with Google action opens Credential Manager using the **Web client ID** and a new random nonce. The returned Google credential is passed to Firebase Authentication. The UI presents an authenticated account only after Firebase's server accepts it. Raw ID tokens, emails and account IDs are never added to the play journal or application logs by this code.

For an existing account that needs a fresh login, the same explicit button calls Firebase reauthentication with the selected Google credential. Choosing another person's Google account does not silently reauthenticate the existing account; switching accounts requires signing out first. A cancelled provider flow remains guest or unverified, with sign-in available again. Returning to the foreground never opens a Google chooser automatically.

Sign-out clears the app's Firebase session and Credential Manager state for this app. With a configured production wallet origin, account deletion calls its authenticated deletion API and requires recent login; service failure does not fall back to removing only Firebase. Without that origin, this build cannot create a paid wallet and uses Firebase app-user deletion. Neither deletes the person's Google Account or local offline progress. The external deletion page is now hosted on the stable Firebase site; Web sign-in/deletion is owner-reported, with independent public-resource/CORS observations recorded in [Hosting evidence](validation/hosting-preview.md). Android account success and complete provider/deletion qualification remain separate.

Google sign-in does not enable advertising, spend money, upload saves or transfer Lab gold. The Android app includes no Firebase Analytics, Firestore client, Cloud Save or Crashlytics. The separately implemented server uses Firestore for purchased-gold and permanent cosmetic ownership; offline board progress is not uploaded. The optional identity service is separate from the advertising consent controls.

## Lab/local development setup

1. Create/select the owner's Firebase project. Add an Android app for `com.androidhit.lumadrift.sandbox` when testing this Lab. Production already has its matching registration/configuration described above. Enable Google as a Firebase Authentication sign-in provider and configure the Google Auth Platform consent/branding details and test audience.
2. Register the certificate fingerprints used by the installed build. The supplied direct-distribution release key has SHA-1 `b6c36f612e94f0402e2eb5510e89a87bdd6dbed1` and SHA-256 `dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287`. If Play App Signing uses a different signing certificate, register its fingerprint as well. Debug builds have a different key.
3. Copy `app/src/main/assets/identity-config.example.json` to local file `app/src/main/assets/identity-config.json` and fill the four public identifiers from the same project:

```json
{
  "webClientId": "YOUR_WEB_OAUTH_CLIENT_ID.apps.googleusercontent.com",
  "apiKey": "YOUR_FIREBASE_ANDROID_API_KEY",
  "appId": "YOUR_FIREBASE_ANDROID_APP_ID",
  "projectId": "YOUR_FIREBASE_PROJECT_ID"
}
```

4. Rebuild and install the signed Lab. The config file is ignored by Git. It contains public client identifiers, not a service-account key. Restrict applicable API keys to the registered application and keep privileged server credentials outside the APK and repository. This implementation initializes the named Firebase app programmatically; it does not require the Google Services Gradle plugin or bundle Analytics.
5. Test the real account chooser, cancellation, no-device-account flow, network failure, sign-out, repeat login, expired/recent-login requirements and account deletion using an authorized test account. Do not count the missing-configuration screen as a passing authentication test.

## Initialization and data boundaries

The Firebase auto-initialization provider is removed. Firebase Auth is initialized only when a valid configuration is present and a login is requested, or when the local marker indicates a previously authenticated session. Default Firebase data collection is disabled; authentication itself still necessarily contacts Google/Firebase and processes identity/session data when used.

Tokens are handled by the Google/Firebase SDKs, not copied into game preferences. `previously_signed_in` is a local restoration hint, not proof of identity. The app also keeps the expected Firebase UID in private preferences after a verified sign-in or validated legacy restoration. This prevents a different cached user from silently taking over the displayed account or wallet identity. Neither a local marker nor the presence of a cached Firebase user is sufficient to display **Connected**.

A signed-in Google/Firebase account authorizes authenticated requests to the separate paid-wallet service only when its deployment origin is configured. Server verification and transactional fulfillment authorize purchased value; identity alone never grants gold. The delivered candidate keeps that origin empty.

## Observable session and lifecycle

`GoogleAccountController(Activity, Listener)` reports an immutable `SessionSnapshot`; `snapshot()` returns its current value. Home and the native Account screen use the same contract:

| State | Meaning and available action |
| --- | --- |
| `GUEST` | No verified session is presented. Offline play remains available; Google sign-in is offered when configured. |
| `CONNECTED` | A non-anonymous Firebase user with the `google.com` provider has passed the sign-in/refresh checks and the SDK has returned a non-empty Firebase ID token. Account actions may be offered. |
| `REFRESHING` | An explicit sign-in, silent foreground check or account operation is pending. The previous profile is hidden and conflicting actions are disabled. |
| `REAUTH_REQUIRED` | A known authentication/recent-login failure requires the explicit Google sign-in action. A cached token alone does not dismiss this state. |
| `ERROR` | The session could not be checked, for example because the network failed. Reconnect or a later foreground retry can recover; offline play remains available. |

`signedIn` is true only for `CONNECTED`. The snapshot's `uid`, `displayName` and `email` are empty in every other state, so stale personalization cannot survive a failed refresh or sign-out. `hasGoogleAccount` describes local account presence for actions such as sign-out; it must not be used as a Connected badge. Display names are bounded and stripped of control characters. The action flags `canSignIn`, `canSignOut` and `canDeleteAccount` avoid a dead end after an error or recent-login requirement.

The screen calls `onResume()` for a silent user `reload()` followed by SDK `getIdToken(false)` validation, `onPause()` on background entry and `close()` when destroyed. Firebase Auth-state and ID-token listeners, plus shared session-preference notifications, update observers when another account screen signs in or out. The controller removes those listeners on close. Asynchronous completions use the main executor rather than an Activity-scoped task listener that would be removed at `onStop()` while Google's provider UI is open.

`reauth_required` is shared and persisted: a recent-login/authentication failure survives screen recreation and cannot leave another screen showing Connected. Successful explicit Firebase sign-in/reauthentication clears it and notifies other observers. Request generations, explicit sign-out consent revocation and expected-UID checks reject obsolete results. Sign-out removes the visible identity immediately and calls Credential Manager's `clearCredentialStateAsync`; failure of provider cleanup cannot restore the app session.

The wallet uses `currentWalletUser`, which requires the matching, non-anonymous Google user and rejects a known `reauth_required` state. A network failure while obtaining a wallet token reports wallet unavailability rather than falsely asserting an invalid account. Firebase authentication errors still require reconnecting. These client gates supplement the server's token verification; they do not authorize a purchase or grant value.

## 1.6 validation scope

Run `python3 scripts/test-identity-session.py` from the repository root. At source freeze it passed **16 scenarios / 203 assertions** against the real controller with deterministic Android, Firebase and Credential Manager doubles. Coverage includes guest/configuration guards, provider and UID checks, foreground refresh, network/authentication failures, cancellation and nonce binding, late results after sign-out, listener cleanup, shared reauthentication across screens/recreation and all six language copies. This test does not contact Google or prove a real account login.

On API 36 emulator `emulator-5556`, the signed production **1.6.0-rc1 / code 7 candidate**, installed SHA-256 `69d91af4db0c22e051992dcfa7557ecbe9068598e4d1333ff5706fda3e71a965`, preserved Guest through Home/Account navigation, Google provider opening and cancellation, background return, Account configuration recreation and a process restart. No real account was selected; real connected-profile, sign-out, reconnect and deletion flows remain unqualified. The [candidate UI report](validation/v1.6/identity-candidate-ui.txt) and [installed metadata](validation/v1.6/identity-candidate-metadata.json) identify this artifact. They do not qualify a later rebuilt final APK; its installed hash/startup repeat is recorded separately when performed.

The official, unmodified Google G asset appears only on the Google sign-in control. It is Google branding, distinct from this game's original companions. A production release still needs its branding and OAuth configuration reviewed in the owner's project.

## Primary references

Session references checked 27 October 2026: [Firebase Auth listeners and sign-out](https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth), [Firebase user reload and reauthentication](https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseUser), [Credential Manager Google sign-in and provider-state cleanup](https://developer.android.com/identity/sign-in/credential-manager-siwg), [Google button branding](https://developers.google.com/identity/branding-guidelines).

Additional setup/version references: [implementation](https://developer.android.com/identity/sign-in/credential-manager-siwg-implementation), [Credential Manager release notes](https://developer.android.com/jetpack/androidx/releases/credentials), [Google ID release notes](https://developers.google.com/identity/android-credential-manager/releases), [Firebase Google authentication](https://firebase.google.com/docs/auth/android/google-signin), [Firebase Android setup](https://firebase.google.com/docs/android/setup).

[Validation](VALIDATION.md) records actual installed-app coverage; [Privacy](PRIVACY.md) describes the two application channels.

## Final 1.6 installed cancellation repeat

The exact final production APK (`3d840e9a7598ca28d4bf12525b312f4d4f671fc613d5ae88082819547abfc1c1`) was exercised on API 36 with a 16KB-page kernel. Guest → actual Google Play services provider → Back before account selection returns to Guest and an enabled sign-in action. Home/background/relaunch stays Guest without reopening the provider. All original preferences, including the existing practice run, are restored byte for byte. [Final evidence](validation/v1.6/identity-final-ui.json). This does not qualify successful OAuth/Firebase authentication, a real token revocation, sign-out or account deletion.
