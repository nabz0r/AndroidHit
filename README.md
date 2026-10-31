# Luma Drift

An Android puzzle game with little companions, linked keys and a world to explore.

**1.24 is locally qualified; final publication is pending.** Candidate D is
production `1.24.0-rc2`, separate Lab `1.24.0-sandbox-rc2`, code 33. Its immutable
artifact source object is `787fba142b8edd1f840b9b70cb7749167edacd24`; this does not
mean that main or the public website has been updated to 1.24.

The new loop connects first wins to companion growth: win → player XP → a new
rank → one copy for the captain actually played → companion level → more talent
uses on the next attempt. Existing attempts keep their captured capacities.
Bright original art, Nunito, a clearer result and reversible wardrobe returns
accompany the change. The moving map, minimap, four rooms and gameplay equipment
remain; outfits are cosmetic.

The production profile created fresh on B and upgraded to D passed 254 assertions
with 46 actual SDK taps. Retained production separately passes 60 checks/46 taps.
Lab passes 11 phases with 568 assertions/118 touches/39 PNGs across retained and fresh
profiles. SDK test-ad loading is observed; display, early close and an actual
35-Coin ad reward remain unqualified. The new simulation executed 8,000 synthetic
trajectories: it exercises the engine, not human perception, enjoyment or
retention. Final main/CI/Hosting/public-origin receipts remain pending.

- [Release changes, exact artifacts and pending gates](docs/RELEASE_1_24.md)
- [1.24 qualification register](docs/validation/v1.24/README.md)
- [Exact candidate D metadata](releases/manifest-1.24.0-rc2.json)
- [Synthetic 1.24 study](docs/validation/v1.24/simulation/README.md)
- [New FR/EN website source and local checks](docs/validation/site-v1.24/README.md)
- [Website](https://luma-drift-production.web.app/)
- [Historical releases](docs/archive/README.md)

The last verified public delivery is [1.23](docs/validation/site-v1.23/README.md),
Firebase version `cf8b817321bf33d5`. Its original receipts remain unchanged.
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
