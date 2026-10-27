# Luma Drift 1.4 — signed production candidate

**26 October 2026 · 1.4.0-rc1 · version code 5 · `com.androidhit.lumadrift`**

**[Download production APK](https://raw.githubusercontent.com/nabz0r/AndroidHit/main/releases/luma-drift-1.4.0-rc1.apk)** · [Play AAB](../releases/luma-drift-1.4.0-rc1.aab) · [Validation](VALIDATION.md) · [Production scope](PRODUCTION_RELEASE.md).

This is a preparation candidate with the full puzzle, configured Google identity/AdMob IDs and an implemented real purchase adapter. It forces Google sample creatives and blocks checkout while the deployed wallet origin is absent. It does not claim a completed Google login, owner Play payment, hosted policy or live advertising.

## Choose the installation

| | Production candidate | Current Lab | Preserved stable |
|---|---|---|---|
| Version | 1.4.0-rc1 / code 5 | 1.4.0-sandbox / code 5 | 1.1.0 |
| Package | `com.androidhit.lumadrift` | `com.androidhit.lumadrift.sandbox` | `com.androidhit.lumadrift` |
| APK | [Production](../releases/luma-drift-1.4.0-rc1.apk) | [Lab](../releases/luma-drift-1.4.0-sandbox.apk) | [Offline stable](../releases/luma-drift-1.1.0.apk) |
| Wallet | Earned local coins; separate server paid wallet when deployed | Local simulator/test wallet | Earned local coins |
| Network | Optional Google and configured wallet services | Google sample-ad routes | No Internet permission |

Production **updates the stable package** with the same signing key; these two cannot be installed alongside each other. The Lab can coexist with production and never transfers simulated funds into its paid wallet. A genuine 1.1 → 1.4 emulator update preserved the observed French choice and 180 earned coins. Prior interrupted-board migration follows the content-version rules; this particular upgrade check started without an active puzzle. See [before](validation/v1.4/upgrade-before.json) and [after](validation/v1.4/upgrade-after.json).

## Exact production artifacts

| File | Bytes | SHA-256 |
|---|---:|---|
| `luma-drift-1.4.0-rc1.apk` | 3,907,158 | `1684633cd69ca625925b906d7500ab5d8250ce58ca584257f4888bf470631dd9` |
| `luma-drift-1.4.0-rc1.aab` | 7,205,688 | `2a5f3f22aa2b68c982b205141252e4fd6861915dbf05696725c32d185e12cfa1` |

The production APK/AAB downloaded from GitHub main were compared byte for byte with the signed tested files: [remote publication evidence](validation/v1.4/remote-publication.json).

The [artifact manifest](../releases/manifest.json) records both channels, all preserved packages and source-input hashes. [SHA256SUMS](../releases/SHA256SUMS.txt) can verify downloaded bytes. Packaging refuses to overwrite a versioned artifact with different bytes.

APK v2 signature verification passed with the existing RSA-3072 certificate, SHA-256 `dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287`. The final candidate was installed and exercised on Android API 36. AAB validation with bundletool and per-payload JarFile signature verification passed. The self-signed certificate is the app's signing identity, not a public certificate-chain or Play approval. Standard jarsigner also reports manifest-order/JarInputStream warnings for the AGP bundle; [the pinned JarFile verification](validation/v1.4/aab-payload-signature.txt) independently verifies every payload.

The arm64/x86_64 native library load segments are aligned to at least 16 KiB, and APK 16 KiB ZIP alignment passed. These structural checks do not establish a run on a 16 KiB device. [Native alignment](validation/v1.4/native-alignment.json).

## Install and test

For direct APK installation, permit installation from the selected download source in Android settings. Preserve your local save before changing channels or uninstalling. Google Play distribution uses the **production AAB**, not the `.sandbox` AAB. It still needs the owner's Play record, signing setup, products, merchant configuration and internal-track qualification.

The candidate leaves the wallet endpoint empty. Entering public IDs does not activate real payments or production ad traffic. Follow [owner onboarding](PRODUCTION_ONBOARDING.md), [server deployment](../server/DEPLOYMENT.md) and [public-page setup](PUBLIC_PAGES.md) before preparing a new live configuration.

[Preserved 1.3 release](RELEASE_1_3.md) · [Preserved 1.3 validation](VALIDATION_1_3.md) · [1.2 evidence](VALIDATION_1_2.md) · [1.1 evidence](VALIDATION_1_1.md).
