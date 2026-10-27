# Luma Drift 1.5 — avatar, planning and sealed passages

**27 October 2026 · 1.5.0-rc1 / code 6 · `com.androidhit.lumadrift`**

**[Download Android APK](https://raw.githubusercontent.com/nabz0r/AndroidHit/main/releases/luma-drift-1.5.0-rc1.apk)** · [Play AAB](../releases/luma-drift-1.5.0-rc1.aab) · [Changes and rules](RELEASE_1_5.md) · [Executed validation](VALIDATION.md).

The quality candidate adds an integrated lobby, honest Google/guest profile, 18 avatar equipment items, 1,000 Journey boards plus 240 Remix, six key/wall prototype challenges, explicit power confirmation/recovery and repaired original ambient music. Equipment is earned locally with fixed sources and bounded benefits. [Full equipment table](AVATAR_EQUIPMENT.md).

## Choose the channel

| | Production | Isolated Lab | Preserved offline |
|---|---|---|---|
| Version | 1.5.0-rc1 / code 6 | 1.5.0-sandbox / code 6 | 1.1.0 |
| Package | `com.androidhit.lumadrift` | `com.androidhit.lumadrift.sandbox` | `com.androidhit.lumadrift` |
| Wallet | Earned coins; separate server wallet adapter, checkout gated | Local test wallet and simulation | Earned coins |
| Ads | Google sample units forced | Google sample units | No ad SDK |
| Installation | Updates production/stable using the existing signing identity | Can coexist with production | Historical binary |

Production preserves existing local progress and inventory. New runs use fixed G/H progression and budget policy 2; an unfinished 1.4 run keeps its B/Q board and budget policy 1 until finished or abandoned. C/R/B/Q codes retain their old layouts. P codes identify the six unlimited, reward-free sealed challenges.

## Exact artifacts

| File | Bytes | SHA-256 |
|---|---:|---|
| [luma-drift-1.5.0-rc1.apk](../releases/luma-drift-1.5.0-rc1.apk) | 3,989,078 | `33c4dbd6c14fa3e72ec52c7d98475b1a9738b175bad9216c4bc4c7c5bdd817a2` |
| [luma-drift-1.5.0-rc1.aab](../releases/luma-drift-1.5.0-rc1.aab) | 7,312,603 | `94264e1c62b04338646be6255c259c85a491366e4a652993712b2ca38fb41d7e` |
| [luma-drift-1.5.0-sandbox.apk](../releases/luma-drift-1.5.0-sandbox.apk) | 3,988,753 | `d926fea0f52f0e868b3364d8e1c19e16941f9d9831636483971ef518e3f39c53` |
| [luma-drift-1.5.0-sandbox.aab](../releases/luma-drift-1.5.0-sandbox.aab) | 7,304,111 | `31f01806bef9d3b35a90cf9f6d8102a088832204f6ae92cc0026b9a9534d5203` |

[Public download verification](validation/v1.5/publication.json) · [SHA256SUMS](../releases/SHA256SUMS.txt) · [Production manifest](../releases/manifest-1.5.0-rc1.json) · [Lab manifest](../releases/manifest-1.5.0-sandbox.json) · [Binary audit](validation/v1.5/binary-final-candidate.json).

The APKs pass v2 signature verification against the existing RSA-3072 certificate, SHA-256 `dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287`. Every AAB payload was verified with JarFile against that pinned public certificate; official bundletool 1.18.1 validates both bundles. ZIP/native ELF 16 KiB alignment checks also pass. Signature and structural checks do not establish Play approval or physical-device quality. [Reproducible verifier](../scripts/verify-v15-binaries.py).

## Install and qualify

For APK installation, allow installs from your selected download source. Keep the existing package installed to retain local saves; uninstalling or clearing storage removes local progress/equipment. The Lab's simulated balances never become purchased gold. Google Play distribution uses the production AAB and the owner's Play App Signing/internal test setup.

This candidate keeps `FORCE_TEST_ADS=true` and an empty embedded wallet origin. It does not activate real ads, paid gold or paid equipment. The supplied Firebase and AdMob public identifiers remain in place for separate account/consent qualification. [Activation walkthrough](DEPLOIEMENT_PAS_A_PAS.md) · [Server wallet](WALLET_ANDROID.md).

A successful build is only one part of release evidence. [Validation](VALIDATION.md) distinguishes exact installed bytes, runtime checks, synthetic emulator profiles, preserved old saves and remaining physical/player/commercial checks.

Preserved: [1.4 release](RELEASE_1_4.md), [1.4 validation](VALIDATION_1_4.md), [1.3](RELEASE_1_3.md), [1.2](VALIDATION_1_2.md), [offline 1.1](VALIDATION_1_1.md).
