# Luma Drift

An Android puzzle game with little companions, linked keys and a world to explore.

**1.24 is delivered for playtesting: source, CI, Hosting and public checks pass.** Candidate D is
production `1.24.0-rc2`, separate Lab `1.24.0-sandbox-rc2`, code 33. Its immutable
artifact source object is `787fba142b8edd1f840b9b70cb7749167edacd24`.
Published source C2b is `66119799d6fbe5254a8d37d50db76afe8daffbdc`;
both workflows and all three CI jobs pass on that exact commit. Their scope
remains separate from deployment evidence. The public site now serves 1.24 from
that source, Firebase version `ded69bffe410a798`, released
`2026-10-31T10:11:12.461Z`.

The new loop connects first wins to companion growth: win → player XP → a new
rank → one copy for the captain actually played → companion level → more talent
uses on the next attempt. Existing attempts keep their captured capacities.
Bright original art, Nunito, a clearer result and reversible wardrobe returns
accompany the change. The moving map, minimap, four rooms and gameplay equipment
remain; outfits are cosmetic.

The production profile created fresh on B and upgraded to D passed 254 assertions
with 46 actual SDK taps. Retained production separately passes 60 checks/46 taps.
Lab's immutable baseline passes 11 phases with 568 assertions/118 touches/39 PNGs.
A separate SDK supplement passes three phases with 142 assertions/22 gestures/10
PNGs: an actual test ad grants 35 Coins, 730→765, retained after a cold process.
Android Back and early close remain unqualified; no Back action was sent.
The new simulation executed 8,000 synthetic
trajectories: it exercises the engine, not human perception, enjoyment or
retention. [Public delivery evidence](docs/validation/site-v1.24/README.md) records
54 complete responses/711,388,755 bytes, both exact APKs and actual FR/EN browser
checks at 320×740 and 1280×720. The earlier auth failure remains historical.

- [Production Android download](https://luma-drift-production.web.app/releases/luma-drift-1.24.0-rc2.apk)
- [Separate Lab Android download](https://luma-drift-production.web.app/releases/luma-drift-1.24.0-sandbox-rc2.apk)
- [Release changes and exact artifacts](docs/RELEASE_1_24.md)
- [1.24 qualification register](docs/validation/v1.24/README.md)
- [Exact candidate D metadata](releases/manifest-1.24.0-rc2.json)
- [Synthetic 1.24 study](docs/validation/v1.24/simulation/README.md)
- [New FR/EN website source and local checks](docs/validation/site-v1.24/README.md)
- [Website](https://luma-drift-production.web.app/)
- [Historical releases](docs/archive/README.md)

The previous [1.23 delivery](docs/validation/site-v1.23/README.md), Firebase version
`cf8b817321bf33d5`, and the failed pre-deployment authorization remain unchanged
historical records. The successful Hosting update preserves 577 historical provider
hashes and serving config, with no payload deletion.
Local source, installed APKs, CI, Hosting management and public response bytes
are separate evidence scopes. No physical-phone acceptance, human study or Play
Store publication is implied.

Use JDK 17, Android SDK 36 and the Gradle wrapper. Signing inputs are private and
ignored. Production and Lab package IDs keep separate data.

```sh
./gradlew :app:assembleProductionRelease :app:assembleLabRelease
bash scripts/test-companion-battle-v124.sh
bash scripts/test-battle-dock-v124.sh
bash scripts/test-pause-layout-v124.sh
bash scripts/test-cognitive-lab-v124.sh
python3 scripts/check-site-v124.py --structure-only
node scripts/test-site-v124-runtime.mjs
python3 scripts/test-deploy-site-v124.py
python3 scripts/test-stage-site-v124-rest.py
python3 scripts/test-collect-site-v124-public-bytes.py
python3 scripts/test-prepare-site-v124.py
```

[Development](docs/DEVELOPMENT.md) · [Notices](docs/THIRD_PARTY_NOTICES.md).
Source and original repository assets: **AGPL-3.0**. No Crash Fever, Animal Crossing
or Candy Crush artwork, characters, audio or source code is included. Ads remain
in test mode and real purchases are closed. Historical art-set tests must use
their original release checkout; their recorded outcomes are not silently
relabelled as 1.24 acceptance.
