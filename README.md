<p align="center"><img src="docs/store/luma-drift-feature.png" alt="Luma Drift — lights, arrows and changing gates" width="920"></p>

<h1 align="center">Luma Drift</h1>
<p align="center"><strong>A small light. A great escape.</strong><br>Tap. Change the gates. Find the order.</p>
<p align="center"><a href="https://raw.githubusercontent.com/nabz0r/AndroidHit/main/releases/luma-drift-1.2.0-sandbox.apk"><strong>Download Luma Drift Lab · 1.2.0-sandbox</strong></a><br><a href="https://raw.githubusercontent.com/nabz0r/AndroidHit/main/releases/luma-drift-1.1.0.apk">Stable offline APK · 1.1.0</a> · <a href="docs/RELEASE.md">Install & checksums</a> · <a href="docs/ROADMAP.md">Roadmap</a></p>
<p align="center">480 campaign & Remix boards · 6 languages · Original art & audio<br>Stable offline game + separate monetization test Lab</p>

## Choose the right app

| | Stable game | Luma Drift Lab |
| --- | --- | --- |
| Version | **1.1.0** | **1.2.0-sandbox** · code 3 |
| Android name | Luma Drift | Luma Drift Lab |
| Application ID | `com.androidhit.lumadrift` | `com.androidhit.lumadrift.sandbox` |
| Status | Signed, tested delivery | Delivered test APK; covered Android 16 flows passed |
| Money and ads | Earned coins only; no ads or billing SDK | No-charge gold simulation; optional Google test ads |
| Collection | Six original companions, six themes | 24 companions across three pages, six themes |
| Account | Guest/local play only | Optional Google/Firebase Auth adapter; owner configuration absent |
| Network | No Internet permission | Google SDK routes can contact Google; see [privacy](docs/PRIVACY.md) |
| Install | **[Stable APK](https://raw.githubusercontent.com/nabz0r/AndroidHit/main/releases/luma-drift-1.1.0.apk)** | **[Lab APK](https://raw.githubusercontent.com/nabz0r/AndroidHit/main/releases/luma-drift-1.2.0-sandbox.apk)** |

The Lab installs beside the stable game with its own save and wallet. Simulated gold never transfers to the stable app. **No real checkout, production ad identifiers or purchase-verification backend is enabled.** Current `main` prepares the Lab; [stable 1.1 source is preserved at commit 15751be](https://github.com/nabz0r/AndroidHit/tree/15751bec3f332f60ba1ba6fef0c0b82f47f941c0), along with its APK. [Test guide and account setup](docs/MONETIZATION_SETUP.md) · [Research and design decisions](docs/MONETIZATION_RESEARCH.md).

## One move changes the next

Each light follows its arrow. A clear path and an open gate let it escape. A switch changes one gate; a paired Remix switch changes two. Choose the order and empty the board. Undo and restart are free, blocked taps cost nothing, and there is no countdown.

<table>
<tr><td align="center"><img src="docs/screenshots/v1.1/home.png" width="235" alt="Real Luma Drift home screen with rank, continue and challenge actions"><br><strong>Your next escape</strong><br>Continue, grow your rank, share a challenge.</td><td align="center"><img src="docs/screenshots/v1.1/play.png" width="235" alt="Actual direction-and-gate puzzle on Android"><br><strong>Read the path</strong><br>Arrows, switches and visible gate states.</td><td align="center"><img src="docs/screenshots/v1.1/remix.png" width="235" alt="Actual Remix puzzle with paired switch badges"><br><strong>Change two gates</strong><br>240 extra boards after Journey level 12.</td></tr>
</table>

Screenshots show the installed **stable 1.1 app**. See the [illustrated walkthrough](docs/VISUAL_GUIDE.md) for the shop, collection, missions and local journal. They do not depict or validate the later Lab SDK integration.

## Inside version 1.1

| Play | Collect | Return & share |
| --- | --- | --- |
| 240 original Journey boards across eight chapters | Six vector companions and six color themes | Three daily missions with earned-coin rewards |
| 240 Remix boards with paired gate switches | Optional star badge after seven distinct clears | First-clear XP, ranks and days explored |
| Three daily puzzles and seven weekly stages | Spark, Vision and Flow powers | Exact C/R/E friend codes and actual-board share cards |
| Contextual coaching and a safe first opening | Earned currency only; no paid solutions | Opt-in local journal with explicit JSON export |

French, English, German, Spanish, Portuguese and Italian are included. Music, effects, haptics and reduced motion have separate settings. Interrupted boards and progression stay on the device. Version 1.1 keeps the original boards and 1.0 save format compatible.

Stable 1.1 has no account, network permission, automatic telemetry, online leaderboard or backend. Android's share sheet opens only after a player requests a challenge or journal export. Friend challenges use the same starting board and award no coins, XP or progression; powers are unavailable. Daily/weekly events follow the device date.

## Inside the separate Lab

The Lab keeps the puzzle and expands its collection, event presentation and optional test surfaces:

- **24 original companions:** 18 additional silhouettes join the original six, with three pages of eight, native preview dialogs and explicit buy/equip actions. All remain cosmetic and use game coins. [Collection and events](docs/COMPANIONS_EVENTS.md).
- **Clearer local events:** visible goals, completed stages, reward ranges, a next-stage action, reset countdown and completion bonus. They still follow the device calendar and do not imply a remote event service.
- **Optional Google account:** Settings opens a guest-first account surface. Credential Manager and Firebase Auth are integrated, but the owner configuration is absent, so the delivered default shows “not configured.” No successful live login or cloud save is claimed. Account choice and advertising choice remain independent. [Configuration and limits](docs/GOOGLE_SIGN_IN.md).
- **Rewarded test ads:** declare an adult state, explicitly opt in, pass UMP's request-eligibility gate, then manually load an ad. An earned-reward callback grants 35 test gold once; a load or dismissal alone grants nothing. An unavailable service leaves play usable.
- **Three simulated packs:** 500, 1,400 or 3,600 gold. Native controls simulate Cancel, Pending or Complete and clearly say no charge. Pending survives restart, completion grants once, and repeating the same transaction adds zero. The real Play checkout route stays disabled.
- **Transparent item prices:** a bounded progression curve uses distinct campaign + Remix clears. It does not use spending, failures, ad choices or a personal willingness-to-pay estimate. Cosmetics keep fixed prices. [Exact bands and prices](docs/GAMEPLAY.md#lab-item-prices).
- **Optional local observations:** impression and paid-event callbacks can enter the journal when enabled. There is no Firebase Analytics or remote gameplay analytics. Optional Firebase Auth serves identity only. Test values are not revenue, and Google's SDK traffic is separate from the local journal.

<table>
<tr><td align="center"><img src="docs/screenshots/lab/collection-page-1.png" width="220" alt="Lab companion collection page 1 with original characters and two additions"><br><strong>Familiar lights</strong><br>Original companions, preserved.</td><td align="center"><img src="docs/screenshots/lab/collection-page-2.png" width="220" alt="Lab companion collection page 2 with owl, bee, turtle, jellyfish, moth, cloud, sun and snail"><br><strong>New silhouettes</strong><br>24 companions, three pages.</td><td align="center"><img src="docs/screenshots/lab/collection-page-3.png" width="220" alt="Lab companion collection page 3 with axolotl, cactus, hedgehog, penguin, planet, dragon, panda and squid"><br><strong>Your constellation</strong><br>Earn, preview and equip.</td></tr>
<tr><td align="center"><img src="docs/screenshots/lab/companion-preview.png" width="220" alt="Native companion preview with explicit purchase confirmation"><br><strong>Look before buying</strong><br>Opening a preview spends nothing.</td><td align="center"><img src="docs/screenshots/lab/events-daily-overview.png" width="220" alt="Daily Drift goal, three stages, countdown and 100 coin completion bonus"><br><strong>A daily escape</strong><br>Goal, stages and bonus in view.</td><td align="center"><img src="docs/screenshots/lab/events-weekly-overview.png" width="220" alt="Weekly Voyage goal, seven stages and 250 coin completion bonus"><br><strong>A longer voyage</strong><br>Seven local weekly puzzles.</td></tr>
<tr><td align="center"><img src="docs/screenshots/lab/test-ad.png" width="220" alt="Actual Google SDK creative visibly labelled Test Ad"><br><strong>Real SDK, test creative</strong><br>Optional reward; no revenue claim.</td><td align="center"><img src="docs/screenshots/lab/ump-form.png" width="220" alt="Actual Google UMP Publisher Test Ads consent form with refusal option"><br><strong>Choices stay visible</strong><br>Google's test consent form.</td><td align="center"><img src="docs/screenshots/lab/account.png" width="220" alt="Lab account screen honestly showing guest play and missing owner Google configuration"><br><strong>Guest comes first</strong><br>Owner sign-in setup is still required.</td></tr>
</table>

These are actual installed-Lab captures. Google's **Test Ad / Publisher Test Ads** screens are SDK test material, not a Luma Drift rating, store listing or production approval. The test run displayed a rewarded creative and credited **35 test gold**, exercised UMP refusal and no-charge pack cancel/pending/restart/idempotence, browsed all 24 companions, and cleared all ten daily/weekly stages with one-time bonuses. Six-language collection/event checks passed. The opt-in local journal captured one actual test impression, reward and paid callback at USD 0 micros; it was cleared and disabled afterward. Google account evidence covers guest/missing-configuration behavior in English and French; actual login remains untested.

The [validation report](docs/VALIDATION.md) identifies the exact packages, executed checks and remaining limits. Owner-configured consent, Play license-test purchases, production fulfillment, live identity and commercial results remain separate future work. Paid-event callback observation is not claimed from an SDK test-ad screenshot.

## Could this be a hit?

**The stable game is playable; market success is unproven.** Version 1.1 strengthens the first session, the gate mechanic, return goals and friend handoff. Engine tests establish solvability and consistency. They do not measure fun, retention or acquisition cost. Neither the stable shop nor Lab simulations establish real revenue.

The [hit audit](docs/HIT_AUDIT.md) explains the product choices and evidence gaps. The [roadmap](docs/ROADMAP.md) puts observation of first-time players and D1/D7 return ahead of additional monetization. Its decision thresholds are project hypotheses, not claimed industry benchmarks.

## Build & verify

Current source builds **Luma Drift Lab 1.2.0-sandbox**: Java 17 and Android Canvas, minimum API 26, target/compile API 36; Gradle 8.14.3 and AGP 8.11.0. The Lab adds GMA Next-Gen 1.6.0, UMP 4.0.0, Billing 9.1.0, Credential Manager 1.6.0, Google ID 1.2.1, Firebase Auth 25.0.0, desugaring and transitive dependencies. Release builds use R8/resource shrinking. Stable 1.1 predates these integrations and has no third-party runtime SDK.

```sh
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk
./gradlew assembleDebug

scripts/test-engine.sh
scripts/test-data.sh
scripts/test-meta.sh
scripts/test-economy.sh
scripts/test-collection-events.sh
./gradlew assembleRelease bundleRelease lintRelease
```

Release builds need your private signing configuration; see [development](docs/DEVELOPMENT.md). Keep the existing signing key when updating an installed package. Private keys and local properties are excluded from this repository. The [validation report](docs/VALIDATION.md) separates logic tests, signature checks, emulator interaction and physical-device limits.

Lab lint reports **0 errors and 4 update advisories**, using the standalone lint 9.4.0 experimental override required for the SDK dependency metadata. The build remains on the recorded AGP/Gradle/SDK versions; see [tooling details](docs/DEVELOPMENT.md).

## Explore the project

| For… | Read |
| --- | --- |
| Playing and understanding every feature | [Visual guide](docs/VISUAL_GUIDE.md) · [Gameplay & economy](docs/GAMEPLAY.md) |
| Exploring the Lab's collection and local events | [Companions & events](docs/COMPANIONS_EVENTS.md) |
| What's new and what comes next | [Changelog](docs/CHANGELOG.md) · [Roadmap](docs/ROADMAP.md) · [Hit audit](docs/HIT_AUDIT.md) |
| Installing and checking the delivery | [Release](docs/RELEASE.md) · [Validation](docs/VALIDATION.md) · [Artifact manifest](releases/manifest.json) |
| Building or changing the game | [Architecture & development](docs/DEVELOPMENT.md) · [Level design](docs/LEVEL_DESIGN.md) |
| Understanding monetization tests | [Lab setup](docs/MONETIZATION_SETUP.md) · [Primary-source research](docs/MONETIZATION_RESEARCH.md) |
| Configuring optional Google identity | [Google sign-in](docs/GOOGLE_SIGN_IN.md) |
| Art, sound and store preparation | [Art & audio](docs/ART_AUDIO.md) · [Store listing](docs/STORE_LISTING.md) · [Brand kit](docs/store/README.md) |
| Data and provenance | [Privacy](docs/PRIVACY.md) · [License](LICENSE) · [Third-party notices](docs/THIRD_PARTY_NOTICES.md) · [Research archive](docs/RESEARCH.md) |

[All documentation](docs/README.md). The initial French research is retained as historical planning; its proposed Godot stack and revenue scenarios do not describe this native implementation or measured business results.

## License

[GNU AGPL v3](LICENSE), including the original code, vector graphics and synthesized sounds. Android platform facilities, Google SDKs, transitive dependencies and build tools retain their own terms; the Gradle wrapper is [Apache 2.0](https://github.com/gradle/gradle/blob/master/LICENSE). The repository license does not grant rights in those SDKs or third-party marks. Production integration needs its own terms review. Trademark availability, Play approval and commercial success have not been established.
