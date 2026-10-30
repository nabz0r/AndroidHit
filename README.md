# Luma Drift

An Android puzzle game with little companions, linked keys and a world to explore.

The 1.23 release focuses on reading the board: sculpted arrows, a single move
counter, contextual companion skills and a reversible outfit preview. New Journey
attempts start without the repeated fire/ice/water preparations. Saved attempts
retain their exact state. The map, movement, minimap and tactical Observatory remain.

**Candidate E is locally qualified:** production `1.23.0-rc2`, separate Lab
`1.23.0-sandbox-rc2`, code 31. Its production emulator runs passed 495 checks with
142 actual SDK touchscreen gestures; Lab passed 27 checks with four gestures and
left seven production preference files byte-identical. The separate study ran
8,000 synthetic trajectories. Git publication, exact-commit CI, Hosting and public
response acceptance for 1.23 are still pending. The website link below is not a
claim that these new downloads are already live.

- [Website](https://luma-drift-production.web.app/)
- [Release changes and validation](docs/RELEASE_1_23.md)
- [Android evidence](docs/validation/v1.23/README.md)
- [Exact E APK identities](releases/manifest-1.23.0-rc2.json)
- [Synthetic 1.23 study](docs/validation/cognitive-lab-v123/README.md)
- [Historical releases](docs/archive/README.md)

The laboratory uses generated profiles, not human players. Its trajectories and
probes do not establish human comprehension, enjoyment or retention. Native
screens and SDK gestures establish a different, explicitly bounded scope.

Use JDK 17, Android SDK 36 and the Gradle wrapper. Signing inputs are private and
ignored. The production and Lab package IDs keep their separate data.

```sh
./gradlew :app:assembleProductionRelease :app:assembleLabRelease
bash scripts/test-journey-clarity-v123.sh /tmp/luma-clarity123-check
bash scripts/test-clarity-layout-v123.sh
bash scripts/test-cognitive-lab-v123.sh
python3 scripts/check-site-v123.py --structure-only
node --test scripts/test-site-v123-runtime.mjs
python3 scripts/test-deploy-site-v123.py
python3 scripts/test-stage-site-v123-rest.py
python3 scripts/test-collect-site-v123-public-bytes.py
```

[Development](docs/DEVELOPMENT.md) · [Native observer](scripts/v123-ui-test/README.md)
· [Notices](docs/THIRD_PARTY_NOTICES.md). Source and original repository assets:
**AGPL-3.0**. No Crash Fever, Animal Crossing or Candy Crush art, characters, audio
or source code is included. Ads remain in test mode and real purchases are closed;
this direct APK release does not imply a Play Store publication.
