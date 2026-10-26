# Luma Drift 1.0.0

Released in this repository on 26 October 2026. Package: `com.androidhit.lumadrift`; version code 1; minimum Android API 26; target API 36.

## Download and install

- **[Signed Android APK](../releases/luma-drift-1.0.0.apk)** — installable package, 89,712 bytes.
- [Signed Android App Bundle](../releases/luma-drift-1.0.0.aab) — publication input, 89,879 bytes; not directly installable.
- [Checksums](../releases/SHA256SUMS.txt), [artifact and source-input manifest](../releases/manifest.json), [public signing certificate](../releases/luma-release-certificate.pem).

Open the APK on an Android device and, if Android asks, allow installation from the application used to open that file. The permission is managed by Android. The app itself does not request installer privileges. Existing versions with the same application ID require the same signing certificate for an update.

This is a repository release, not a claim of Google Play publication or approval. The AAB has passed bundletool validation but has not been uploaded to Play Console.

## Integrity and signing

APK SHA-256:

```text
8c5a24cf9ba420b6a8e5402126c953b8dfa5dd19e9d42a8265c943bc402776d0
```

AAB SHA-256:

```text
6ab4f5b1bae15d798c24a5afda22b0c98a73ee351b2ecfc07fd924110402939b
```

Certificate SHA-256:

```text
dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287
```

The APK is release-signed with a locally generated RSA-3072 certificate, not the Android debug key. APK Signature Scheme v2 verification passed. The certificate is self-signed, as is normal for this direct Android distribution workflow; this does not mean Google has reviewed the application. Private signing material remains local and is excluded from Git.

The AAB's JAR signature verifies. The Java verifier reports the self-signed certificate, missing timestamp and streaming/JAR layout warnings for the generated bundle. Separate bundletool validation passes. These checks are not Play Console acceptance.

## Included

240 campaign boards; eight chapters; six companions; six themes; three powers; earned-coin shop; local daily and weekly puzzle events; six languages; original vector art and procedural audio; local saves; undo/restart; reduced motion; accessibility nodes and keyboard controls.

## Deliberate boundaries

No real-money purchase, advertising, analytics, multiplayer, cloud save or remotely operated event service. No server or runtime model inference. All currencies and dates are local and are unsuitable for a financial economy without a different trust model.

The initial validated runtime is the Android 16/API 36 ARM64 emulator. A physical-phone session, subjective sound audition and Google Play review remain unverified. Minimum SDK is a declared build compatibility level; see [VALIDATION.md](VALIDATION.md) for the actual platform checks rather than assuming every supported version was exercised.

## Controls

Tap a piece to preview its direction, then release on it to play. A small switch badge points to the gate that changes after that piece leaves. An impossible tap leaves the game unchanged. The three powers can be earned or purchased using game coins; they are optional. Undo and restart are always free.

The interface follows the system language when supported. Settings allows an explicit language, sound, music, haptic and reduced-motion selection. Credits includes the license text and source link. Uninstalling or clearing app data removes local progress.
