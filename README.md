<h1 align="center">Luma Drift</h1>
<p align="center"><strong>Your companion. A goal of your own.</strong><br>Read the gates. Restore the Prism. Put together a little look.</p>
<p align="center"><a href="https://raw.githubusercontent.com/nabz0r/AndroidHit/main/releases/luma-drift-1.10.0-rc3.apk"><strong>Download signed Android APK · 1.10.0-rc3</strong></a><br><a href="docs/RELEASE_1_10.md">Release & evidence</a> · <a href="docs/UX_1_10.md">Experience</a> · <a href="docs/README.md">Documentation</a> · <a href="docs/ROADMAP.md">Roadmap</a></p>
<p align="center">1,000 Journey boards · 240 Remix · 100 optional guardians<br>7 playable lessons · 24 companions × 12 free visual treatments · 6 powers · 6 languages</p>

<p align="center"><img src="docs/screenshots/v1.10/ui-sdk-final3-full100/home-fr-localized.png" width="230" alt="Actual French rc3 Home: guest, pinned Cupid goal and resumed level11"><img src="docs/screenshots/v1.10/ui-sdk-final3-full100/avatar-fr.png" width="230" alt="Actual French rc3 companion with four cosmetic and three equipment slots"><img src="docs/screenshots/v1.10/ui-sdk-final3-full100/story-fr.png" width="230" alt="Actual French rc3 journal: chapter2, two claimed fragments and zero clears in its band"></p>

Unedited **Lab 1.10.0-sandbox-rc3** Android captures. The native route really completes the first 100 Journey levels, then replays boards: Home resumes **L11**, while the journal shows chapter 2. The Cupid goal is affordable but still unowned; its preview grants nothing. These are local guest states, not Google identity or payment evidence. [Capture provenance](docs/validation/site-v1.10/captures.json).

**Website delivery:** the 1.10 website source and current screenshots are prepared in this repository. The [last confirmed Firebase Hosting delivery remains 1.9](docs/validation/site-v1.9/deployment.json); local browser review and Hosting 1.10 deployment are pending because the desktop is locked. The [website record](docs/validation/site-v1.10/README.md) distinguishes source validation from deployment. A source push does not deploy the site.

## Companion & Objectives — 1.10

- **Know what is available.** Home displays real unclaimed rewards, a chosen collection goal and its actual progress. Today, This week, Journey and Ready to collect each have a distinct objective tab. Viewing a page does not collect anything.
- **See the same companion everywhere.** One portrait combines the selected character/style with hat, dress, shoes, gloves, head equipment, aura and charm. Inventory, wardrobe and full-character previews share it. Twelve visual treatments are free; existing equipment retains its stated bounded effects.
- **Choose one item to work towards.** Pin an item from the existing 24 cosmetics and 18 equipment pieces. Read its progress and actual acquisition source. Previewing, viewing a source, purchasing and equipping stay separate actions; unowned items cannot be equipped by previewing them.
- **Read the Prism journal.** Ten original chapter stories follow the existing biomes. Journey clears and claimed optional guardian fragments are separate facts. Reading the journal grants no reward and makes no guardian mandatory.
- **Find the next event action.** The next daily/weekly stage and the existing 100/250-LumaCoin completion bonuses are visible earlier. Bonus claims retain their once-only receipt across local-save failure and retry.
- **Keep the currencies clear.** The shop separates Powers, Appearance and the account LumaGold wallet. Local earned LumaCoins remain distinct from premium account funds. Test ads are forced, real checkout is gated and paid cosmetic draws stay disabled.

<p align="center"><img src="docs/screenshots/v1.10/ui-sdk-final3-full100/ready-fr.png" width="230" alt="Actual French rc3 ready list: six unclaimed rewards"><img src="docs/screenshots/v1.10/ui-sdk-final3-full100/shop-looks-fr.png" width="230" alt="Actual French rc3 appearance shop: earned-coin capsule and unowned hats"><img src="docs/screenshots/v1.10/ui-sdk-final3-full100/avatar-preview-cupid-en.png" width="230" alt="Actual English rc3 unowned Cupid preview with price, goal and Close controls"></p>

The pastel native dialogs, visible selected tabs and reachable lower controls are checked separately from their accessibility state. The earlier candidate's transparent selection surfaces and clipped footers remain in the [qualification history](docs/validation/v1.10/README.md). [UX contract](docs/UX_1_10.md) · [Readability/provenance matrix](docs/validation/v1.10/ux-readability/README.md) · [Journal model](docs/PRISM_STORY_1_10.md).

## Every move still matters

A piece follows its arrow. Keys dissolve matching seals, switches preview their gate changes, and a closed gate can be opened manually for one action. Each L/M board keeps its fixed productive-action allowance and margin. **Undo is free and refunds the reversed action, including at zero moves. Restart is free.** Briefings show the action target for three stars; hints and Undo counters are not score penalties.

Seven optional, replayable practice boards teach arrows, blockers, keys, gates, switches, refunded Undo and Spark. Completing them grants **100 LumaCoins + two Sparks once**. Practice consumes no inventory and grants no campaign wins. Six powers retain explicit free one-charge samples at their existing unlock levels.

Every ten distinct Journey clears unlocks another optional Prism guardian. Two-layer pieces need two actions; from guardian 5, marked pieces move one cell per action. Free guidance, Undo and restart remain. A solved guardian offers its existing **100 earned LumaCoins once**, only when claimed. The main route does not require it.

The world retains ten 100-level atmospheres, original synthesized music, separate music/effects controls and reduced motion. The 24 companions × 12 styles are **288 code-drawn combinations**, not 288 hand-painted images. Cosmetic clothing and existing bounded-benefit equipment remain different systems. A missed visit does not reset the seven-gift cycle.

[Gameplay](docs/GAMEPLAY.md) · [Tutorial](docs/TUTORIAL.md) · [Guardians](docs/GUARDIANS.md) · [Art/audio](docs/ART_AUDIO.md) · [Economy](docs/ECONOMY_1_7.md).

## Executed checks and limits

The final rc3 [native full-route run](docs/validation/v1.10/ui-sdk-final3-full100/host-report.json) passes **2,514 assertions** with **96 original PNGs**, covering the first 100 Journey levels, objectives, preview/ownership, goal persistence, all ten journal chapters, styles, guardians, navigation, footer bounds and six languages. Original preferences and rotation are restored. A [genuine fresh installation I](docs/validation/v1.10/ui-sdk-fresh-guest-i/README.md) separately passes **91 checks** and retains its new guest. [Production startup and actual audio](docs/validation/v1.10/SDK_UI_FINAL.md) are separate from the Lab route. These are emulator observations, not physical-device or human-comprehension acceptance.

Source checks separately cover **6,544 portrait/translation assertions**, **443 objective/store assertions**, and **308,412 read-only journal assertions** across every clear-count boundary from 0 to 1000. Existing economy, inventory, data, local-save and Journey regressions are retained in the [qualification index](docs/validation/v1.10/README.md). Signed APK/AAB checks, official bundletool and 16KiB ZIP/native alignment belong to the [exact binary audit](docs/validation/v1.10/binaries/binary-final-candidate.json).

**Puzzle geometry and difficulty are unchanged.** The documented L22→L23 structural jump from 17 to 85 remains. Structural scores, source simulations and successful scripted clears do not predict human enjoyment, D1/D7 retention or revenue. [The 2026 research and synthetic audit](docs/PLAYER_AUDIT_2026.md) keeps published survey aggregates distinct from assumed game profiles. No individual social data is scraped. [Current backlog](docs/PRISM_STORY_1_10.md#unchanged-structural-backlog).

## Signed artifacts

| Artifact | Download | SHA-256 / manifest |
| --- | --- | --- |
| Production APK · 7.76 MB | [1.10.0-rc3](releases/luma-drift-1.10.0-rc3.apk) | `29a68cdcaeb61850d55afd101f03bd8c42a075cc6d6631205fc176e499dd70b4` |
| Production AAB | [1.10.0-rc3](releases/luma-drift-1.10.0-rc3.aab) | [Manifest](releases/manifest-1.10.0-rc3.json) |
| Separate Lab APK/AAB | [Lab APK](releases/luma-drift-1.10.0-sandbox-rc3.apk) · [AAB](releases/luma-drift-1.10.0-sandbox-rc3.aab) | [Manifest](releases/manifest-1.10.0-sandbox-rc3.json) |

Android 8+ / API26; version code **11**; production package `com.androidhit.lumadrift`. Install over the existing app to retain saves. Signing identity is unchanged. [Installation](docs/RELEASE.md) · [Checksums](releases/SHA256SUMS.txt) · [Historical 1.9 release and publication](docs/RELEASE_1_9.md). Intermediate rc1/rc2 reports remain identified by their original hashes; their unshipped binaries are retained locally and their manifests are archived, not offered as current downloads.

Google sign-in is optional and does not synchronize local gameplay saves. Actual two-account friends/gifts, consent, live ads and Play purchase lifecycle still need provider qualification. The Play Console walkthrough is paused for app improvement; this release does not announce a Play launch. No server code or remote gameplay-analytics SDK changes here.

[Privacy](docs/PRIVACY.md) · [Production setup](docs/DEPLOIEMENT_PAS_A_PAS.md) · [Social qualification](docs/QUALIFICATION_SOCIALE.md) · [Server](server/README.md).

## Build and inspect

Use JDK21, the Android SDK and the Gradle wrapper; Java source compatibility is17. Private signing files are ignored and never published.

```sh
./gradlew assembleProductionRelease bundleProductionRelease lintProductionRelease
./gradlew assembleLabRelease bundleLabRelease lintLabRelease
bash scripts/test-objective-models-v110.sh
bash scripts/test-prism-story-v110.sh
./docs/validation/v1.10/avatar/run-host.sh
python3 scripts/check-site-v110.py --phase release --apk-sha256 29a68cdcaeb61850d55afd101f03bd8c42a075cc6d6631205fc176e499dd70b4
node --test public/tests/*.test.mjs
```

[Development](docs/DEVELOPMENT.md) · [Native SDK QA runner](scripts/v110-ui-test/README.md) · [Roadmap](docs/ROADMAP.md). Original game source and repository assets: **AGPL-3.0**. [Notices](docs/THIRD_PARTY_NOTICES.md). No Candy Crush or Crash Fever artwork, characters, audio or code is included.
