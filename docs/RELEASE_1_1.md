# Luma Drift 1.1.0

> Preserved 1.1 delivery. Its artifact sizes, hashes and runtime claims remain historical. Use [the current release](RELEASE.md) and [current validation](VALIDATION.md) for 1.5.


Repository release: **26 October 2026**. Package `com.androidhit.lumadrift`, version code **2**, minimum API26/Android8, target API36/Android 16. This is a signed repository delivery; Google Play publication has not occurred.

## Download and install

| Artifact | Exact bytes | Use |
| --- | ---: | --- |
| **[Signed APK](https://raw.githubusercontent.com/nabz0r/AndroidHit/main/releases/luma-drift-1.1.0.apk)** | 120,336 | Install on Android |
| [Signed AAB](../releases/luma-drift-1.1.0.aab) | 120,494 | Input for future Play Console submission; not directly installable |
| [Public certificate](../releases/luma-release-certificate.pem) | 1,497 | Check signing identity; no private key |

Open the APK from the downloading application and use Android's installation prompt. Android may ask to allow that application to install packages. Luma Drift itself does not request installer privileges.

**Updating 1.0:** install 1.1 over it with the same package/signing identity. Do not uninstall first if you want to preserve local progress. A signed in-place upgrade was exercised on the Android 16 emulator: three prior clears, an interrupted level 4, balance 300 after a purchase and explicit French language survived; 105 XP migrated once and old clears did not complete today's missions. [Upgrade evidence](validation/upgrade-results.txt).

## Exact integrity

APK SHA-256:

```text
08c4852c63d9a6c806dae740a8cc2f9fe4a49cae4263b06b8b554118027050fb
```

AAB SHA-256:

```text
d85d498c87690d0b30918aca823d3cf59b2afb51e2b5ce129964002b5b61f020
```

Signing-certificate SHA-256:

```text
dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287
```

[All artifact checksums](../releases/SHA256SUMS.txt) · [Source-input and artifact manifest](../releases/manifest-1.1.0.json) · [APK signature output](validation/apk-signature.txt).

The APK uses one locally generated RSA-3072 release signer and passes APK Signature Scheme v2 verification. It is not signed with Android's debug key. The self-signed certificate is normal for direct distribution and does not imply a Google review. Private signing material stays outside Git; preserve it securely for future updates.

The AAB passes [bundletool validation](validation/bundle-validation.txt). Its [JAR signature](validation/aab-signature.txt) verifies with self-signed-certificate, timestamp and streaming-layout qualifications from the verifier. This is not Play Console acceptance. The manifest records exact delivered bytes and build-source inputs; it does not promise a bit-identical rebuild with another signing key or timestamp.

## What changed

240 new Remix boards with paired gate switches, contextual first moves, missions, XP/ranks, a cosmetic badge, fair C/R/E friend challenges, original share cards and an optional local play journal. The original 240 boards, local events, six companions/themes/languages, three powers, synthesized audio and 1.0 progression remain. [Detailed changelog](CHANGELOG.md) · [Visual guide](VISUAL_GUIDE.md).

## Delivery boundaries

No real-money billing, ads, automatic telemetry, account or backend. The earned-coin shop generates no real revenue. Local events follow the device clock. Friend sharing opens Android's share sheet and requires the player's choice of destination. Codes open installed apps; no installation referral service is included.

The runtime evidence comes from an ARM64 Android 16/API36 emulator. Physical-phone sound, comfort, battery behavior, older-device compatibility, full TalkBack acceptance and player retention remain unmeasured. Read [VALIDATION_1_1.md](VALIDATION_1_1.md) for exact coverage.

Version 1.0 packages remain in [releases](../releases/) for provenance and upgrade reproduction; 1.1 remains the stable offline delivery; see [the current release index](RELEASE.md) for the separate Lab. Clearing app data or uninstalling removes local progression. [Privacy](PRIVACY.md).
