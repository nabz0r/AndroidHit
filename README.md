<p align="center"><img src="docs/store/luma-drift-feature.png" alt="Luma Drift — lights, arrows and changing gates" width="920"></p>

<h1 align="center">Luma Drift</h1>
<p align="center"><strong>A small light. A great escape.</strong><br>Tap. Change the gates. Find the order.</p>
<p align="center"><a href="https://raw.githubusercontent.com/nabz0r/AndroidHit/main/releases/luma-drift-1.1.0.apk"><strong>Download Android APK · 1.1.0</strong></a> · <a href="docs/RELEASE.md">Install & checksums</a> · <a href="docs/VISUAL_GUIDE.md">Visual guide</a> · <a href="docs/ROADMAP.md">Roadmap</a></p>
<p align="center">480 campaign & Remix boards · 6 languages · Original art & audio · Offline · No ads or real-money purchases</p>

## One move changes the next

Each light follows its arrow. A clear path and an open gate let it escape. A switch changes one gate; a paired Remix switch changes two. Choose the order and empty the board. Undo and restart are free, blocked taps cost nothing, and there is no countdown.

<table>
<tr><td align="center"><img src="docs/screenshots/v1.1/home.png" width="235" alt="Real Luma Drift home screen with rank, continue and challenge actions"><br><strong>Your next escape</strong><br>Continue, grow your rank, share a challenge.</td><td align="center"><img src="docs/screenshots/v1.1/play.png" width="235" alt="Actual direction-and-gate puzzle on Android"><br><strong>Read the path</strong><br>Arrows, switches and visible gate states.</td><td align="center"><img src="docs/screenshots/v1.1/remix.png" width="235" alt="Actual Remix puzzle with paired switch badges"><br><strong>Change two gates</strong><br>240 extra boards after Journey level 12.</td></tr>
</table>

Screenshots show the installed app. See the [illustrated walkthrough](docs/VISUAL_GUIDE.md) for the shop, collection, missions and local journal.

## Inside version 1.1

| Play | Collect | Return & share |
| --- | --- | --- |
| 240 original Journey boards across eight chapters | Six vector companions and six color themes | Three daily missions with earned-coin rewards |
| 240 Remix boards with paired gate switches | Optional star badge after seven distinct clears | First-clear XP, ranks and days explored |
| Three daily puzzles and seven weekly stages | Spark, Vision and Flow powers | Exact C/R/E friend codes and actual-board share cards |
| Contextual coaching and a safe first opening | Earned currency only; no paid solutions | Opt-in local journal with explicit JSON export |

French, English, German, Spanish, Portuguese and Italian are included. Music, effects, haptics and reduced motion have separate settings. Interrupted boards and progression stay on the device. Version 1.1 keeps the original boards and 1.0 save format compatible.

There is no account, network permission, automatic telemetry, online leaderboard or backend. Android's share sheet opens only after a player requests a challenge or journal export. Friend challenges use the same starting board and award no coins, XP or progression; powers are unavailable. Daily/weekly events follow the device date.

## Could this be a hit?

**The product is playable; market success is unproven.** This release strengthens the first session, the gate mechanic, return goals and friend handoff. Engine tests establish solvability and consistency. They do not measure fun, retention or acquisition cost. The shop currently generates no real revenue.

The [hit audit](docs/HIT_AUDIT.md) explains the product choices and evidence gaps. The [roadmap](docs/ROADMAP.md) puts observation of first-time players and D1/D7 return ahead of additional monetization. Its decision thresholds are project hypotheses, not claimed industry benchmarks.

## Build & verify

Native Java 17 and Android Canvas. Minimum API 26, target/compile API 36; Gradle 8.14.3 and AGP 8.11.0. No third-party runtime library or game engine.

```sh
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk
./gradlew assembleDebug

scripts/test-engine.sh
scripts/test-data.sh
scripts/test-meta.sh
./gradlew assembleRelease bundleRelease lintRelease
```

Release builds need your private signing configuration; see [development](docs/DEVELOPMENT.md). Keep the existing signing key when updating an installed package. Private keys and local properties are excluded from this repository. The [validation report](docs/VALIDATION.md) separates logic tests, signature checks, emulator interaction and physical-device limits.

## Explore the project

| For… | Read |
| --- | --- |
| Playing and understanding every feature | [Visual guide](docs/VISUAL_GUIDE.md) · [Gameplay & economy](docs/GAMEPLAY.md) |
| What's new and what comes next | [Changelog](docs/CHANGELOG.md) · [Roadmap](docs/ROADMAP.md) · [Hit audit](docs/HIT_AUDIT.md) |
| Installing and checking the delivery | [Release](docs/RELEASE.md) · [Validation](docs/VALIDATION.md) · [Artifact manifest](releases/manifest.json) |
| Building or changing the game | [Architecture & development](docs/DEVELOPMENT.md) · [Level design](docs/LEVEL_DESIGN.md) |
| Art, sound and store preparation | [Art & audio](docs/ART_AUDIO.md) · [Store listing](docs/STORE_LISTING.md) · [Brand kit](docs/store/README.md) |
| Data and provenance | [Privacy](docs/PRIVACY.md) · [License](LICENSE) · [Research archive](docs/RESEARCH.md) |

[All documentation](docs/README.md). The initial French research is retained as historical planning; its proposed Godot stack and revenue scenarios do not describe this native implementation or measured business results.

## License

[GNU AGPL v3](LICENSE), including the original code, vector graphics and synthesized sounds. Android platform facilities and build tools retain their own licenses; the Gradle wrapper is [Apache 2.0](https://github.com/gradle/gradle/blob/master/LICENSE). Third-party game names in the research are references. Trademark availability, Play approval and commercial success have not been established.
