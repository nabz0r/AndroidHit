# Development

Luma Drift 1.0.0 is a native Android application: Java 17, compile/target SDK 36, minimum SDK 26 (Android 8.0). AGP 8.11.0, Gradle 8.14.3. No third-party runtime libraries, game engine, web content or network service is required.

## Structure

- `engine/`: immutable board definitions, deterministic campaign/event generation, mutable board state, exact solver and undo history. Pure Java.
- `data/`: coin inventory, unlocks, one-time rewards, date-scoped events, cosmetics, settings and six complete language sets.
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
python3 scripts/smoke-emulator.py --serial emulator-5580 --reset
```

The data test uses small JVM-only Android stubs under `scripts/test-support`; these are not included in the APK. It checks the persistence contract in memory, not Android's disk implementation. The interaction test verifies actual process restart and installed-app behavior. `--reset` clears only Luma Drift's data on the selected emulator; physical device serials are rejected.

A successful compile does not establish runtime behavior. Inspect screenshots and the crash buffer after interaction tests. For a new release, repeat install, launch, a complete campaign puzzle, a gate puzzle, an event, shop debit, language changes, background audio pause and save restoration.

## Persistence and economy

A single versioned SharedPreferences store holds coins, best stars, unlocks, equipped cosmetics, stock and event reward claims. Small synchronous transactions intentionally persist economy changes before the action returns. A separate session save holds the board type, campaign index or event period, action history and star-affecting counts. The history is replayed atomically through the engine; malformed histories are rejected.

Event periods are captured at board creation, saved unchanged and checked before awarding rewards. An old board cannot collect the next day's reward. The clock and local data are user-controlled: this is not an anti-cheat system and must not be used as a real-money economy.

Deferred Flow actions capture the current board and a generation token. Navigation, backgrounding, restart or undo invalidates them. Victory disables underlying actions before the result panel appears. Accessibility actions capture their target and navigation epoch rather than referring to a changing hit-list index.

## Licensing and future integrations

The existing AGPL-3.0 license is preserved. The app includes the license text and a source link under Settings → Credits & license. Integrating proprietary payment/advertising SDKs is a separate licensing and privacy decision; none is included in this release. Real monetization also requires product validation and the relevant Play/AdMob setup. See the archived research for economic hypotheses, not measured results for this game.
