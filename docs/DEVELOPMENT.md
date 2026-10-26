# Development

Current source builds **Luma Drift Lab 1.3.0-sandbox**, version code 4 (signed delivery; recorded final-binary Android 16 checks passed): Java 17, compile/target SDK 36, minimum SDK 26 (Android 8.0), AGP 8.11.0 and Gradle 8.14.3. The puzzle uses Android Canvas. The signed **stable 1.1.0 APK/AAB** remain preserved separately.

| Contract | Stable 1.1.0 | Lab 1.3.0-sandbox |
|---|---|---|
| Application ID | `com.androidhit.lumadrift` | `com.androidhit.lumadrift.sandbox` |
| Java namespace | `com.androidhit.lumadrift` | `com.androidhit.lumadrift` |
| Android label | Luma Drift | Luma Drift Lab |
| Runtime SDKs | Android platform only | GMA Next-Gen 1.6.0, UMP 4.0.0, Billing 9.1.0, Credential Manager 1.6.0, Google ID 1.2.1, Firebase Auth 25.0.0 and dependencies |
| Release processing | Original stable build configuration | R8 minification and resource shrinking |
| Data | Stable app-private saves | Separate app-private saves and test wallet |
| External services | No Internet permission/backend | Google test-ad/consent/Billing routes; no production wallet service |

Building current `main` does not reproduce the stable 1.1 package. [Stable source is preserved at commit 15751be](https://github.com/nabz0r/AndroidHit/tree/15751bec3f332f60ba1ba6fef0c0b82f47f941c0); its [original validation record](https://github.com/nabz0r/AndroidHit/blob/15751bec3f332f60ba1ba6fef0c0b82f47f941c0/docs/VALIDATION.md) stays scoped to that release. Do not overwrite versioned stable artifacts with Lab bytes. Different application IDs allow stable and Lab to coexist without sharing preferences. Lab 1.3 updates the same sandbox application as 1.2; it does not create another isolated wallet. Keep 1.2 artifacts and logs as historical evidence rather than overwriting them with 1.3 bytes.

## Structure

- `engine/`: immutable original board definitions, deterministic generation, frozen `BalancedCatalog` progression order, mutable `Board`, exact solver, Undo history and separate `MoveBudget` attempt ledger. Pure Java; ordering/score analysis does not run on a phone.
- `data/`: coin inventory, unlocks, one-time rewards, date-scoped events, cosmetics, settings and six complete language sets. The six-power catalog supplies unlock milestones and pack sizes; GameStore persists unique free-inventory and continuation transactions. MetaProgress adds first-clear XP, ranks, unique claims and migration; PlayJournal is a separate optional local observation store.
- `sharing/`: exact C/R/E original and B/Q balanced challenge codes, actual-board cards, grant-scoped read-only provider and Android Sharesheet JSON export.
- `audio/`: original PCM synthesis, SoundPool effects and AudioTrack music; asynchronous preparation and audio focus.
- `commerce/`: native ordinary commerce and purpose-specific continuation/power surfaces, guarded rewarded test ads and UMP. Only ordinary commerce starts Billing connection/product queries; real checkout is disabled. Pack simulation is clearly labelled no-charge.
- `identity/`: native optional account screen, bounded public configuration loader and Credential Manager → Firebase Auth verification adapter. Default configuration is absent; no fake profile or cloud save.
- `LumaView`: adaptive Canvas rendering, original vector companions, touch previews, item animations, menus and virtual accessibility nodes.
- `MainActivity`: Android lifecycle, insets, modern back callback and old-Android fallback.

The reference canvas is 390 units wide with at least 780 units of usable height. Smaller windows scale the complete layout down; wider windows center it. System bars and cutouts are excluded from the interactive area. Very small windows can reduce practical touch-target size; physical-device accessibility review remains necessary.

The Lab commerce screen uses native scrollable Android controls rather than the game canvas. The delivered Lab 1.2 has historical Android 16 runtime evidence for test commerce, collection/events and the missing-identity-config guard; stable screenshots do not stand in for those checks. The covered results do not establish actual provider login, a full accessibility audit, physical-device usability or production services. See [VALIDATION.md](VALIDATION.md).

## Build and signing

Set `JAVA_HOME` to a JDK 17 installation and `ANDROID_HOME` to the Android SDK. Either set the SDK environment variable or create an untracked `local.properties` containing `sdk.dir=/absolute/path/to/sdk`.

```sh
./gradlew assembleDebug
./gradlew lintRelease
```

Create a release keystore locally using Android's documented signing workflow. Keep it backed up securely; updates to the distributed APK require the same signing identity. Local file `signing.properties`:

```properties
storeFile=.signing/your-private-release.jks
storePassword=YOUR_LOCAL_SECRET
keyAlias=your-alias
keyPassword=YOUR_LOCAL_SECRET
```

These values are examples, not shipped credentials. `.signing/`, keystores and `signing.properties` are ignored. The published APK uses a locally generated RSA-3072 release certificate. The private key is not in the public repository. The build deliberately does not silently substitute the Android debug key for a release key.

```sh
./gradlew assembleRelease bundleRelease lintRelease
```

The Lab uses `android.experimental.lint.version=9.4.0` in `gradle.properties`: the standalone analyzer is newer than the bundled AGP analyzer to handle the SDKs' Kotlin 2.4 metadata. This is an experimental tooling override, not a runtime feature. AGP remains 8.11.0 and Gradle 8.14.3. The final [Lab 1.3 lint output](validation/v1.3/lint.txt) has **0 errors and 4 warnings** about target SDK, compile SDK, Gradle and a newer preview lint; no “no issues found” claim applies to this release.

If release signing is not configured, the release APK is unsigned and must be signed before installation. The debug build remains available for development. [Android signing documentation](https://developer.android.com/studio/publish/app-signing).

## Tests

```sh
scripts/test-engine.sh
scripts/test-data.sh
scripts/test-meta.sh
scripts/test-economy.sh
scripts/test-collection-events.sh
scripts/test-difficulty.sh
scripts/test-move-budget.sh
python3 scripts/smoke-emulator.py --serial emulator-5580 --reset
python3 scripts/smoke-v11.py --serial emulator-5580
```

The data test uses small JVM-only Android stubs under `scripts/test-support`; these are not included in the APK. It checks the persistence contract in memory, not Android's disk implementation. The interaction test verifies actual process restart and installed-app behavior. `--reset` clears only Luma Drift's data on the selected emulator; physical device serials are rejected.

A successful compile does not establish runtime behavior. Inspect screenshots and the crash buffer after interaction tests. For a new release, repeat install, launch, a complete campaign puzzle, a gate puzzle, an event, shop debit, language changes, background audio pause and save restoration.

The existing 1.1 interaction scripts and logs are stable-release evidence. Select the intended package explicitly when writing Lab scripts; never let a reset intended for the sandbox clear a stable save. In addition to puzzle regression, check side-by-side isolation, no-charge cancel/pending/completion, repeated transaction delivery, opt-out/age gates, UMP failure, manual ad loading and exact permissions in the merged Lab manifest. SDK tests, actual Google license-test purchases and a deployed wallet service are separate layers; see [MONETIZATION_SETUP.md](MONETIZATION_SETUP.md).

Historical 1.2 evidence follows; it does not qualify changed 1.3 gameplay/reward routes. The collection/event suite records 24 unique companion IDs/shapes with original-six compatibility and reset-boundary contracts: [87,121 checks across 604 time zones and 14,496 boundary scenarios](validation/lab/collection-events-tests.txt). It does not render silhouettes or establish touch comfort. A [separate actual UI run](validation/lab/collection-events-ui-smoke.txt) browsed all three collection pages, cancelled a preview without debit, bought Miko for exactly 160, re-equipped without debit, completed ten event boards, checked unique bonuses and exercised all six languages. [Detailed design and captures](COMPANIONS_EVENTS.md).

The [final-binary collection recheck](validation/lab/collection-final-ui-smoke.txt) separately covers corrected card margins, all 24 cards, an exact 220-coin Orbit purchase, zero-cost repeated confirmation/equip and daily/weekly replay/claimed-bonus behavior. The [identity guard run](validation/lab/identity-ui.txt) covers English/French guest and absent-configuration states only.

The new [difficulty suite](validation/v1.3/difficulty-tests.txt) covers all 480 original/balanced layout identities, source permutations, fixed tutorial prefixes, exact solutions and measured curve changes. [MoveBudget](validation/v1.3/move-budget-tests.txt), [economy](validation/v1.3/economy-tests.txt) and [data/localization](validation/v1.3/data-tests.txt) reports cover their recorded JVM cases. They do not demonstrate an Android ad completion, disk durability fault or player difficulty. Final 1.3 interaction reports are kept under `docs/validation/v1.3/`. The [installed run](validation/v1.3/move-power-ui-smoke.txt) checked action/exhaustion persistence, two exact 100-gold extensions, cap/restart, unlimited friend mode, six powers, a pack purchase and free loot, then two actual Google sample-ad rewards (+10 actions and Spark ×1, no extra gold). It used explicit synthetic progression/inventory and five seeded clears, restored all six original game-owned preference files byte for byte and independently checked the installed APK SHA-256. This is not a 75-level playthrough, production ad/purchase proof or physical-device quality audit. A separate [existing-save exact-zero run](validation/v1.3/exact-zero-ui.txt) performed four exit/Undo cycles then the five certified Journey-1 exits, earning two stars/45 coins/30 XP without fixture edits, an ad or a purchase.

## Persistence and economy

A single versioned SharedPreferences store holds coins, best stars, unlocks, equipped cosmetics, stock and event reward claims. Small synchronous transactions intentionally persist economy changes before the action returns. A separate session save holds the board type, index/period, successful move history and star-affecting counts. Lab 1.3 adds content version 2, a run UUID, used/extra actions, continuation count and pending/last-applied receipt, plus Shield charges. History and ledger must restore together; malformed snapshots are rejected. Blocked attempts and successful Undos cannot be reconstructed from removal history alone, so the action ledger is independently persisted.

Event periods are captured at board creation, saved unchanged and checked before awarding rewards. An old board cannot collect the next day's reward. The clock and local data are user-controlled: this is not an anti-cheat system and must not be used as a real-money economy.

In the Lab, `EconomyCurve` uses only distinct campaign/Remix clears, with bands at 0/30/90/210/450. Prices are rounded up to five coins and bounded at twice base; cosmetics remain fixed. `GameStore.grantGold` persists a local unique-transaction marker and wallet credit together. It supports test delivery idempotency, not verified ownership or server authority. Pending simulator state has its own Lab preferences. [Formula and prices](GAMEPLAY.md#lab-item-prices).

`MoveBudget` grants N plus a public mode/level margin. Present-piece attempts and successful Undo spend one; a charged Shield protects blocked attempts. Gold and earned-ad extensions share a maximum of two per run, adding `max(5, remaining pieces +3)` actions. The 100-gold debit and requested reward use an attempt-scoped idempotency key; a continuation state is persisted before acknowledging a reward. The ledger is local and user-controlled, not a paid-wallet authority. [Exact rules](GAMEPLAY.md#lab-13-action-allowance).

`Catalog` contains six powers, unlocks at Journey 1/5/12/25/45/75 and pack sizes 3/3/2/2/2/2. The native preview, use, purchase and +1 earned-ad route honor `unlockedLevel()`, not just stock ownership. Every five distinct Journey clears permits one deterministic eligible inventory claim; milestone credit and stock change persist together. No paid random loot is included.

Deferred Flow actions capture the current board and a generation token. Navigation, backgrounding, opening help, restart or undo invalidates them. Help and pause stop the local active-play timer. Victory disables underlying actions before the result panel appears. Accessibility actions capture their target and navigation epoch rather than referring to a changing hit-list index.

## Upgrade and challenge contracts

Version 1.1 keeps all original campaign IDs and frozen campaign/event generation. Version 1.0 stars, currency, cosmetics, inventory, settings and active history remain compatible. XP is derived once from existing completed boards; old progress does not complete today's missions. Remix uses independent stars/unlocks and becomes available after campaign board 12. Exact generation and frozen fingerprints are documented in [LEVEL_DESIGN.md](LEVEL_DESIGN.md).

Lab 1.3 uses `BalancedCatalog` for Journey/Remix without modifying `LevelCatalog` originals. Content version 2 deliberately abandons an old interrupted sandbox board on update, with a six-language notice; wallet, inventory, best stars, unlocks, cosmetics and settings remain. New sessions retain their full move ledger and Shield state. Progress records refer to their existing numbered slots, whose layouts can change under the new sequence; the original layout identities remain available through C/R codes. [Measured ordering and provenance](DIFFICULTY.md).

Friend codes are bounded, checksummed `LD1-C/R/E/B/Q-…` payloads. C/R retain original Journey/Remix identities, E retains event seeds, and B/Q identify the frozen balanced order. The custom `lumadrift://challenge/CODE` scheme is an Android deep link, not a verified HTTPS App Link or dynamic installation link. Import uses a native input dialog and never reads the clipboard automatically. Friend boards give no coins, XP, mission progress, powers or continuations; their action allowance is unlimited. Cached exports are confined to the provider's shared directory; it rejects writes and traversal. URI read grants accompany explicit Sharesheet launches. Sharing logs a handoff, not a completed send. Clear Journal removes local observations and staged journal exports; it cannot recall copies saved elsewhere.

## Optional Google identity configuration

The Lab reads an ignored `app/src/main/assets/identity-config.json` containing four public configuration values: `webClientId`, `apiKey`, `appId` and `projectId`. They must match the owner's Firebase/OAuth project, exact Lab application ID and signing certificate configuration. No owner values are shipped; malformed/missing configuration yields a not-configured guest state. Never place service-account keys, OAuth client secrets or signing keys in this file.

`GoogleAccountController` requests a Google ID credential with a nonce, then passes it to Firebase Auth; parsing a JWT is not treated as verified identity. Firebase default data collection is disabled and no Firebase Analytics dependency is included. SDK authentication can still communicate externally. Sign-out and remote Firebase app-user deletion use the real SDK routes when configured; there is no substitute fake-success path. Local game progress is neither uploaded nor erased by these identity controls. Account choice does not authorize advertising or real purchases.

Use [GOOGLE_SIGN_IN.md](GOOGLE_SIGN_IN.md) for the owner configuration and test plan. Default guest/not-configured checks, actual provider login, session restoration, sign-out and deletion are separate evidence; do not claim the configured flow passed without a real project and account. See [third-party notices](THIRD_PARTY_NOTICES.md) for SDK/branding provenance.

## Release collection

After building and verifying signing, run `python3 scripts/package-release.py`. Versioned APK/AAB files, exact source-input SHA-256 hashes and artifact checksums are recorded without private signing files. Preserve old packages to support upgrade checks. Never overwrite an older version's artifact with newer bytes.

The preserved delivered Lab 1.2 outputs are [luma-drift-1.2.0-sandbox.apk](../releases/luma-drift-1.2.0-sandbox.apk) and [luma-drift-1.2.0-sandbox.aab](../releases/luma-drift-1.2.0-sandbox.aab). The delivered current outputs are [luma-drift-1.3.0-sandbox.apk](../releases/luma-drift-1.3.0-sandbox.apk) and [luma-drift-1.3.0-sandbox.aab](../releases/luma-drift-1.3.0-sandbox.aab). The APK contains 3,890,449 bytes, SHA-256 `313cee4693293c24d0775ca73bb00fc063e799f523bcb67e82a1bd975c4ba2da`; the AAB contains 7,191,002 bytes, SHA-256 `89d496da2f700d8c34f83ebf9752a23d03975c5e81e1d4bfba28858b3e919fa5`. Exact bytes, hashes, signature/bundle results and final-binary runtime scope belong in [RELEASE.md](RELEASE.md) and [VALIDATION.md](VALIDATION.md). Stable 1.1 signature/upgrade results apply to the stable application ID. Installing the Lab alongside it is an isolation test, not an in-place upgrade.

## Licensing and future integrations

The existing AGPL-3.0 license is preserved for the original project. The app includes the license text and a source link under Settings → Credits & license. The Lab's Google SDKs, Android dependencies and build tools retain their own terms; the repository license does not relicense them. Review the exact distribution/SDK terms before production activation rather than assuming compatibility or approval from a successful build.

No real monetization is enabled. It requires the owner's Play/AdMob configuration, suitable consent and audience handling, actual license-test lifecycle evidence and an authoritative token-verifying wallet backend. Changing the sandbox boolean alone does not provide those dependencies or enable checkout. [Primary-source research](MONETIZATION_RESEARCH.md) · [Test and activation guide](MONETIZATION_SETUP.md).

The historical **Lab 1.2** [final-binary advertising observation](validation/commerce-ad-metrics-smoke.txt) inspected only the Lab's own staged journal on a root-capable emulator. It recorded one impression, one 35-gold reward and one paid callback at USD 0 micros / UNKNOWN / test=true. Export chooser cancellation, journal/cache clearing and persisted opt-out were verified. This is SDK test evidence, not income or a production analytics backend.
