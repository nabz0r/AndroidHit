# Luma Drift — signed Android deliveries

Repository delivery: **26 October 2026**. The newest app is **Luma Drift Lab 1.2.0-sandbox**, a separate test installation. **Luma Drift 1.1.0** remains available as the offline stable game. Neither has been published to Google Play.

## Choose and install

| | New Lab | Stable offline game |
| --- | --- | --- |
| Download | **[Lab APK](https://raw.githubusercontent.com/nabz0r/AndroidHit/main/releases/luma-drift-1.2.0-sandbox.apk)** | **[Stable APK](https://raw.githubusercontent.com/nabz0r/AndroidHit/main/releases/luma-drift-1.1.0.apk)** |
| Exact APK bytes | 3,874,065 | 120,336 |
| Version / code | 1.2.0-sandbox / 3 | 1.1.0 / 2 |
| Package | `com.androidhit.lumadrift.sandbox` | `com.androidhit.lumadrift` |
| Collection | 24 companions, six themes | Six companions, six themes |
| Monetization | Optional Google test ads and no-charge simulated gold packs | Earned coins only; no ads/billing SDK |
| Account | Google adapter prepared; owner configuration absent, guest mode works | Guest/local only |

Both declare Android 8/API26 minimum and target API36. Open the downloaded APK and follow Android's installation prompt. The downloading application may require permission to install packages; Luma Drift itself requests no installer privilege.

The Lab installs **beside** stable, with separate save data and wallet. It does not migrate stable progression or turn simulated gold into purchased currency. Installing Lab over an earlier Lab with the same signature preserves its own local data. Do not uninstall or clear app data if you want to keep local progress.

## What's in the Lab

The 480 Journey/Remix boards, original art/audio and six languages remain. The Lab adds 18 original companions, paged previews with explicit purchase confirmation, clearer daily/weekly event goals and rewards, calendar-aware reset countdowns, bounded logarithmic item-price bands, adult/opt-in advertising controls, UMP privacy choices, real Google rewarded **test** ads, three no-charge gold simulations and a guest-first Google account screen.

An actual Google sample ad rendered and granted exactly 35 test coins after its earned-reward callback. The purchase simulator exercised cancellation, pending across restart, completion and duplicate prevention. These tests demonstrate no real payment or income. Real checkout is disabled; a production wallet backend is absent. [Test setup](MONETIZATION_SETUP.md) · [Primary-source research](MONETIZATION_RESEARCH.md) · [Collection/event screen guide](COMPANIONS_EVENTS.md).

Google sign-in requires the owner's matching OAuth and Firebase project configuration. The delivered default explains this and stays in guest mode. Credential Manager/Firebase Auth code is included, but actual account selection, login, logout and deletion remain unvalidated without that configuration. No cloud save is provided. [Google setup](GOOGLE_SIGN_IN.md).

## Exact integrity

| Artifact | Bytes | SHA-256 |
| --- | ---: | --- |
| [Lab APK](../releases/luma-drift-1.2.0-sandbox.apk) | 3,874,065 | `46a34273daae7c75f882b9ec7239bbe68f10e6f9c58e82d4f0a27e2593c4333f` |
| [Lab AAB](../releases/luma-drift-1.2.0-sandbox.aab) | 7,166,459 | `6bf8942c1a127f52c13b8c0e05042a329f6bbefcc8736986ffe0ea539bc3b6f6` |
| [Stable APK](../releases/luma-drift-1.1.0.apk) | 120,336 | `08c4852c63d9a6c806dae740a8cc2f9fe4a49cae4263b06b8b554118027050fb` |
| [Stable AAB](../releases/luma-drift-1.1.0.aab) | 120,494 | `d85d498c87690d0b30918aca823d3cf59b2afb51e2b5ce129964002b5b61f020` |

AAB files are not directly installable. [All checksums](../releases/SHA256SUMS.txt) · [Lab source/artifact manifest](../releases/manifest-1.2.0-sandbox.json) · [Stable manifest](../releases/manifest-1.1.0.json).

Both use the same private RSA-3072 release key, whose public certificate SHA-256 is:

```text
dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287
```

The [public certificate](../releases/luma-release-certificate.pem) contains no private key. The [Lab APK signature verifies with scheme v2](validation/lab/apk-signature.txt), [bundletool validation passes](validation/lab/bundle-validation.txt), and the [AAB JAR signature verifies](validation/lab/aab-signature.txt) with self-signed-certificate/timestamp/streaming-layout qualifications. These are package checks, not Play Console approval. Preserve the private signing key securely for future updates. The manifests establish exact delivered bytes and source inputs, not timestamp-independent reproducible signing.

## Evidence and remaining limits

[Validation](VALIDATION.md) separates deterministic simulations, installed Android interaction and package integrity. The Lab's runtime checks use an ARM64 Android 16 emulator. No physical-phone acceptance, subjective sound audition, full TalkBack session, live Google login, license-test purchase, production revenue or market-retention result is claimed. Google SDK traffic is separate from the optional local journal; see [privacy](PRIVACY.md) and [third-party notices](THIRD_PARTY_NOTICES.md).

The [stable 1.1 release record](RELEASE_1_1.md), [stable validation](VALIDATION_1_1.md) and [source checkpoint](https://github.com/nabz0r/AndroidHit/tree/15751bec3f332f60ba1ba6fef0c0b82f47f941c0) preserve the tested offline delivery, including its genuine signed 1.0 → 1.1 upgrade. Version 1.0 packages remain for provenance.
