# Optional Google sign-in

The Lab integrates **Android Credential Manager 1.6.0**, **Google ID 1.2.1** and **Firebase Authentication 25.0.0**. The SDK route is implemented; this repository contains no owner's OAuth/Firebase configuration and does not claim a completed real Google login. Guest play remains available without an account.

## What happens

Settings → Account opens a native screen. With no configuration, it explains the missing setup and keeps the guest state. It does not invent a user, open an unrelated project's login, or ask for a Google password.

Once the owner configures the correct project, an explicit Sign in with Google action opens Credential Manager using the **Web client ID** and a new random nonce. The returned Google credential is passed to Firebase Authentication. The UI presents an authenticated account only after Firebase's server accepts it. Raw ID tokens, emails and account IDs are never added to the play journal or application logs by this code.

Sign-out clears the app's Firebase session and Credential Manager state for this app. Account deletion requests deletion of this app's Firebase authentication user, with a confirmation and recent-login handling. It does not delete the person's Google Account or the game's local progress. A production publisher also needs an account-deletion web route and the associated store declarations before release.

Google sign-in does not enable advertising, spend money, upload saves or transfer Lab gold. Firebase Analytics, Firestore, Cloud Save and Crashlytics are not included. The optional identity service is separate from the advertising consent controls.

## Configure the actual app

1. Create/select the owner's Firebase project. Add an Android app for `com.androidhit.lumadrift.sandbox` when testing this Lab. A future production package requires its own matching registration. Enable Google as a Firebase Authentication sign-in provider and configure the Google Auth Platform consent/branding details and test audience.
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

Tokens are handled by the Google/Firebase SDKs, not copied into game preferences. `previously_signed_in` is a local restoration hint, not proof of identity. The authenticated state always comes from Firebase's current user. No account state authorizes paid currency: real billing fulfillment remains disabled until its own verified server ledger exists.

The official, unmodified Google G asset appears only on the Google sign-in control. It is Google branding, distinct from this game's original companions. A production release still needs its branding and OAuth configuration reviewed in the owner's project.

## Primary references

Checked 26 October 2026: [Credential Manager Google sign-in](https://developer.android.com/identity/sign-in/credential-manager-siwg), [implementation](https://developer.android.com/identity/sign-in/credential-manager-siwg-implementation), [Credential Manager release notes](https://developer.android.com/jetpack/androidx/releases/credentials), [Google ID release notes](https://developers.google.com/identity/android-credential-manager/releases), [Firebase Google authentication](https://firebase.google.com/docs/auth/android/google-signin), [Firebase Android setup](https://firebase.google.com/docs/android/setup), [Google button branding](https://developers.google.com/identity/branding-guidelines).

[Validation](VALIDATION.md) records actual installed-app coverage; [Privacy](PRIVACY.md) describes the two application channels.
