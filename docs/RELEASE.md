# Luma Drift Lab 1.3 — signed Android delivery

**26 October 2026 · 1.3.0-sandbox · code 4 · test configuration**

**[Download the signed 1.3 APK](https://raw.githubusercontent.com/nabz0r/AndroidHit/main/releases/luma-drift-1.3.0-sandbox.apk)** · [Validation](VALIDATION.md) · [Rules and six powers](MOVE_BUDGET_POWERUPS.md).

## Choose the app

| | Current Lab 1.3 | Preserved stable 1.1 |
| --- | --- | --- |
| Android label | Luma Drift Lab | Luma Drift |
| Package | `com.androidhit.lumadrift.sandbox` | `com.androidhit.lumadrift` |
| Install | [Lab APK](../releases/luma-drift-1.3.0-sandbox.apk) | [Offline APK](../releases/luma-drift-1.1.0.apk) |
| Boards | 240 Journey +240 Remix in a measured, frozen order | Original 240 Journey +240 Remix |
| Play | Limited actions; free restart; optional continuations | Unlimited attempts |
| Collection | 24 original companions, six themes | Six companions, six themes |
| Powers | Six with progression unlocks, shop, free milestone loot and test-ad rewards | Three powers with earned-coin shop |
| Google services | Optional test ads/consent, disabled real checkout, unconfigured Google identity adapter | No Internet/account/ad/billing SDK |

Both declare Android 8/API26 minimum and target API36. Neither is published to Google Play. Open the downloaded APK and follow Android's installation prompt. Android may require the downloading app to be allowed to install packages; the game itself requests no installer permission.

The Lab installs beside stable with its own wallet and save. **An update from Lab 1.2 to 1.3 uses the same package and signing key.** Local gold, XP, inventory, cosmetics and numbered star/unlock progress remain. The new order changes which layout appears at many positions; existing stars stay at their numbered positions. An old interrupted attempt restarts with an explanation because its board/order and action ledger changed. There is no transfer of simulated gold to stable or a purchased wallet. Do not uninstall or clear app data if you want to preserve local progress.

## What changed

- **A real action allowance:** a present-piece attempt consumes one action, whether successful or blocked; a successful Undo also consumes one. Empty taps and menus cost none. Every initial board has a verified solution inside its allowance. Solving on the last action wins.
- **A clear continuation choice:** restart for free, spend 100 local gold, or explicitly watch a rewarded test ad. An earned continuation adds `max(5, remaining pieces + 3)` actions. Both sources share two continuations per attempt. An allowance cannot repair every unsafe gate state or guarantee a win.
- **Six powers:** Spark, Vision, Flow, Shield, Focus and Trail unlock at Journey levels 1/5/12/25/45/75. Shop packs show quantities and progression-only prices. A power ad grants one selected unlocked unit; free deterministic loot grants one unit every five distinct Journey clears. No paid loot draw.
- **A measured order for all 480 campaign/Remix boards:** original layouts and solutions retained, fixed teaching prefixes, reduced structural difficulty jumps inside chapters. [Exact score and before/after results](DIFFICULTY.md). This is not a human difficulty or retention result.
- **The Lab collection/events remain:** 24 original companions, previews, six languages, explicit event goals, visible rewards, reset countdown and one-time bonuses.

Advertising has three separate offers: ordinary commerce gives **35 test gold**, continuation gives **extra actions only**, and a power offer gives **one item only**. Each needs the actual earned callback, adult declaration, explicit opt-in and UMP request eligibility. Gold packs remain clearly labelled local simulations; **real checkout is disabled**. [Setup and evidence boundaries](MONETIZATION_SETUP.md).

Google sign-in is coded through Credential Manager/Firebase Auth, but the delivered configuration has no owner OAuth/Firebase project. The account screen stays in guest mode and explains setup. No actual Google login or cloud save is claimed. [Configure identity](GOOGLE_SIGN_IN.md).

## Exact package integrity

| Artifact | Bytes | SHA-256 |
| --- | ---: | --- |
| [Lab 1.3 APK](../releases/luma-drift-1.3.0-sandbox.apk) | 3,890,449 | `313cee4693293c24d0775ca73bb00fc063e799f523bcb67e82a1bd975c4ba2da` |
| [Lab 1.3 AAB](../releases/luma-drift-1.3.0-sandbox.aab) | 7,191,002 | `89d496da2f700d8c34f83ebf9752a23d03975c5e81e1d4bfba28858b3e919fa5` |
| [Stable 1.1 APK](../releases/luma-drift-1.1.0.apk) | 120,336 | `08c4852c63d9a6c806dae740a8cc2f9fe4a49cae4263b06b8b554118027050fb` |

AAB files are not directly installable. [All checksums](../releases/SHA256SUMS.txt) · [Source/artifact manifest](../releases/manifest-1.3.0-sandbox.json) · [Public certificate](../releases/luma-release-certificate.pem).

The RSA-3072 release certificate SHA-256 remains:

```text
dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287
```

[APK scheme-v2 verification](validation/v1.3/apk-signature.txt), [bundletool validation](validation/v1.3/bundle-validation.txt) and [AAB JAR verification](validation/v1.3/aab-signature.txt) establish package integrity. The JAR verifier reports self-signed-certificate, timestamp and streaming-layout qualifications. This is not Play approval or a timestamp-independent reproducible signed build. Private signing material is excluded from Git and must be preserved securely for future updates.

## Validation and history

The [current validation report](VALIDATION.md) separates deterministic simulations, installed Android interaction, actual SDK callbacks and remaining account/device limits. Synthetic milestone/inventory fixtures are explicitly marked and restored after their tests; they do not demonstrate that 75 levels were played. No physical-phone acceptance, live Google account, Play license-test purchase, production income or market-retention result is claimed.

Earlier deliveries remain downloadable: [Lab 1.2 record](RELEASE_1_2.md) / [validation](VALIDATION_1_2.md), [stable 1.1 record](RELEASE_1_1.md) / [validation](VALIDATION_1_1.md), and the original 1.0 packages in [releases](../releases/). [Privacy](PRIVACY.md) · [Third-party notices](THIRD_PARTY_NOTICES.md).
