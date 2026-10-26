# Development

Luma Drift 1.1.0 is a native Android application: Java 17, compile/target SDK 36, minimum SDK 26 (Android 8.0). AGP 8.11.0, Gradle 8.14.3. No third-party runtime libraries, game engine, web content or network service is required.

## Structure

- `engine/`: immutable board definitions, deterministic campaign/Remix/event generation, mutable board state, exact solver and undo history. Pure Java.
- `data/`: coin inventory, unlocks, one-time rewards, date-scoped events, cosmetics, settings and six complete language sets. MetaProgress adds first-clear XP, ranks, unique claims and migration; PlayJournal is a separate optional local observation store.
- `sharing/`: exact C/R/E challenge codes, actual-board cards, grant-scoped read-only provider and Android Sharesheet JSON export.
- `audio/`: original PCM synthesis, SoundPool effects and AudioTrack music; asynchronous preparation and audio focus.
- `LumaView`: adaptive Canvas rendering, original vector companions, touch previews, item animations, menus and virtual accessibility nodes.
- `MainActivity`: Android lifecycle, insets, modern back callback and old-Android fallback.

The reference canvas is 390 units wide with at least 780 units of usable height. Smaller windows scale the complete layout down; wider windows center it. System bars and cutouts are excluded from the interactive area. Very small windows can reduce practical touch-target size; physical-device accessibility review remains necessary.

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

If release signing is not configured, the release APK is unsigned and must be signed before installation. The debug build remains available for development. [Android signing documentation](https://developer.android.com/studio/publish/app-signing).

## Tests

```sh
scripts/test-engine.sh
scripts/test-data.sh
scripts/test-meta.sh
python3 scripts/smoke-emulator.py --serial emulator-5580 --reset
python3 scripts/smoke-v11.py --serial emulator-5580
```

The data test uses small JVM-only Android stubs under `scripts/test-support`; these are not included in the APK. It checks the persistence contract in memory, not Android's disk implementation. The interaction test verifies actual process restart and installed-app behavior. `--reset` clears only Luma Drift's data on the selected emulator; physical device serials are rejected.

A successful compile does not establish runtime behavior. Inspect screenshots and the crash buffer after interaction tests. For a new release, repeat install, launch, a complete campaign puzzle, a gate puzzle, an event, shop debit, language changes, background audio pause and save restoration.

## Persistence and economy

A single versioned SharedPreferences store holds coins, best stars, unlocks, equipped cosmetics, stock and event reward claims. Small synchronous transactions intentionally persist economy changes before the action returns. A separate session save holds the board type, campaign index or event period, action history and star-affecting counts. The history is replayed atomically through the engine; malformed histories are rejected.

Event periods are captured at board creation, saved unchanged and checked before awarding rewards. An old board cannot collect the next day's reward. The clock and local data are user-controlled: this is not an anti-cheat system and must not be used as a real-money economy.

Deferred Flow actions capture the current board and a generation token. Navigation, backgrounding, opening help, restart or undo invalidates them. Help and pause stop the local active-play timer. Victory disables underlying actions before the result panel appears. Accessibility actions capture their target and navigation epoch rather than referring to a changing hit-list index.

## Upgrade and challenge contracts

Version 1.1 keeps all original campaign IDs and frozen campaign/event generation. Version 1.0 stars, currency, cosmetics, inventory, settings and active history remain compatible. XP is derived once from existing completed boards; old progress does not complete today's missions. Remix uses independent stars/unlocks and becomes available after campaign board 12. Exact generation and frozen fingerprints are documented in [LEVEL_DESIGN.md](LEVEL_DESIGN.md).

Friend codes are bounded, checksummed `LD1-C/R/E-…` payloads. The custom `lumadrift://challenge/CODE` scheme is an Android deep link, not a verified HTTPS App Link or dynamic installation link. Import uses a native input dialog and never reads the clipboard automatically. Friend boards give no coins, XP, mission progress or powers. Cached exports are confined to the provider's shared directory; it rejects writes and traversal. URI read grants accompany explicit Sharesheet launches. Sharing logs a handoff, not a completed send. Clear Journal removes local observations and staged journal exports; it cannot recall copies saved elsewhere.

## Release collection

After building and verifying signing, run `python3 scripts/package-release.py`. Versioned APK/AAB files, exact source-input SHA-256 hashes and artifact checksums are recorded without private signing files. Preserve old packages to support upgrade checks. Never overwrite an older version's artifact with newer bytes.

## Licensing and future integrations

The existing AGPL-3.0 license is preserved. The app includes the license text and a source link under Settings → Credits & license. Integrating proprietary payment/advertising SDKs is a separate licensing and privacy decision; none is included in this release. Real monetization also requires product validation and the relevant Play/AdMob setup. See the archived research for economic hypotheses, not measured results for this game.
