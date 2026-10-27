# Installation and release artifacts

**Luma Drift 1.7 · 27 October 2026 · code 8**

**[Download Android APK · 1.7.0-rc1](https://raw.githubusercontent.com/nabz0r/AndroidHit/main/releases/luma-drift-1.7.0-rc1.apk)** · [Play AAB](../releases/luma-drift-1.7.0-rc1.aab) · [Isolated Lab APK](../releases/luma-drift-1.7.0-sandbox.apk) · [Changes](RELEASE_1_7.md) · [Executed validation](VALIDATION_1_7.md).

Install the production APK over the existing Luma Drift installation to retain local progress, coins, clothing and interrupted boards. Keep the application data; uninstalling removes the local save. Lab is a separate package and its progress/currency is not transferred into production. Android 8.0 or later is required.

The candidate adds exact dead-end recovery and safe hints, the continuous 1–1,000 map, seven visit rewards, daily/weekly quests, four-slot cosmetic wardrobe, earned capsules, ten original biome arrangements and the private friend-circle client/service. All 1,240 starting boards remain solvable inside their unchanged allowances. [Rules and limits](RELEASE_1_7.md).

| File | Bytes | SHA-256 |
|---|---:|---|
| [luma-drift-1.7.0-rc1.apk](../releases/luma-drift-1.7.0-rc1.apk) | 5,594,242 | `7eeaa816853533f441acfc73f4ad3f6af0fb2cbdd6fd0eae1fda3f64c388a2bf` |
| [luma-drift-1.7.0-rc1.aab](../releases/luma-drift-1.7.0-rc1.aab) | 8,948,843 | `b5bfc1e51b272bc8046c3e79efcf95ae79814913e87d522ff6062e3f81d6ec75` |
| [luma-drift-1.7.0-sandbox.apk](../releases/luma-drift-1.7.0-sandbox.apk) | 5,593,925 | `d7fea345c1f33cc8e114688ee06d8b60ed1278cc29eeb328c677c6c1858ba049` |
| [luma-drift-1.7.0-sandbox.aab](../releases/luma-drift-1.7.0-sandbox.aab) | 8,941,491 | `2e6f8412e42979b00238689134abd335b10fc040780d6f36b6085fed37a757c8` |

Both APKs use the existing pinned public signing identity; both AAB signatures and bundletool validation pass. ZIP/native ELF 16KB alignment passes. Final Lab and production were actually installed on API36 arm64 with a 16,384-byte page kernel. Lab passes 72 UI/gameplay checks; production passes 7 startup/audio observations. [Binary/signature record](validation/v1.7/binary-final-candidate.json) · [Native SDK record](validation/v1.7/SDK_UI_FINAL.md) · [Public HTTPS download verification](validation/v1.7/publication.json) · [Production manifest](../releases/manifest-1.7.0-rc1.json) · [Lab manifest](../releases/manifest-1.7.0-sandbox.json) · [Checksums](../releases/SHA256SUMS.txt).

Production updates `com.androidhit.lumadrift`; Lab uses `com.androidhit.lumadrift.sandbox`. Production forces sample ads, keeps real checkout disabled and closes premium cosmetic draws in the APK and server. The separate social/catalogue endpoint is configured and deployed; real two-Google-account acceptance remains open. No real purchase, live-ad eligibility or physical-device enjoyment was validated. [Activation guide](DEPLOIEMENT_PAS_A_PAS.md).

The sections below preserve earlier deliveries, hashes and tests. They do not describe the current 1.7 package.

## Preserved 1.6 delivery

**Luma Drift 1.6 · 27 October 2026 · code 7**

**[Download Android APK · 1.6.0-rc1](https://raw.githubusercontent.com/nabz0r/AndroidHit/main/releases/luma-drift-1.6.0-rc1.apk)** · [Play AAB](../releases/luma-drift-1.6.0-rc1.aab) · [Isolated Lab APK](../releases/luma-drift-1.6.0-sandbox.apk) · [Changes](RELEASE_1_6.md) · [Executed validation](VALIDATION_1_6.md).

The internal candidate adds ten-node saga pages, prelevel goals and action limits, three-row inventory with ownership filters, explicit account-session states, Aurora Atlas art and a fixed J/K progression with key/seal lessons. Existing G/H active saves and historical share codes retain their original board and allowance. Production updates `com.androidhit.lumadrift`; Lab uses the separate `.sandbox` package. Keep the installed application and its data when updating.

| File | Bytes | SHA-256 |
|---|---:|---|
| [luma-drift-1.6.0-rc1.apk](../releases/luma-drift-1.6.0-rc1.apk) | 5,528,646 | `3d840e9a7598ca28d4bf12525b312f4d4f671fc613d5ae88082819547abfc1c1` |
| [luma-drift-1.6.0-rc1.aab](../releases/luma-drift-1.6.0-rc1.aab) | 8,865,601 | `ed5d1a76dce4935a7b042a6507cefded8193cf22859830770a7b2814c974df07` |
| [luma-drift-1.6.0-sandbox.apk](../releases/luma-drift-1.6.0-sandbox.apk) | 5,528,321 | `0f8d7c04df45331b844b276c8149d9c276e8687f190c2cc7d2e1e236fbaa1a45` |
| [luma-drift-1.6.0-sandbox.aab](../releases/luma-drift-1.6.0-sandbox.aab) | 8,855,495 | `ad5dbf567ff2188438a7d1bfa0d7b28c6fb5424541a2419daba6927d701a37b5` |

[Public downloads verified against the tested bytes](validation/v1.6/publication.json). Both APKs verify against the existing public RSA signing certificate; both AABs pass signature and pinned bundletool validation. ZIP/native ELF 16KB alignment passes. [Binary record](validation/v1.6/binary-final-candidate.json) · [Production manifest](../releases/manifest-1.6.0-rc1.json) · [Lab manifest](../releases/manifest-1.6.0-sandbox.json) · [Checksums](../releases/SHA256SUMS.txt).

The delivered build forces official sample ads and keeps its wallet origin empty. It does not activate real checkout or live ad units. Google provider/server qualification and Play commercial approval remain separate. [Activation guide](DEPLOIEMENT_PAS_A_PAS.md).

The files, hashes, signatures and runtime references below identify the preserved **1.5** delivery. Do not relabel them as 1.6 evidence or attempt to infer a newer package's configuration from them.

## Preserved Luma Drift 1.5 — avatar, planning and sealed passages

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

The [1.5 website/design refresh](DESIGN_SYSTEM.md) changes presentation only; these Android artifacts retain their listed hashes. Website publication has its own [revision-specific evidence](PUBLIC_PAGES.md#15-visual-refresh-and-new-evidence).
