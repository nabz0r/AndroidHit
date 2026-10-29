# Separate 1.17 native art gallery

This observer produces **QA RENDER** sheets through the installed app's public,
pure `CompanionPortrait` and `CharacterSprites` drawing APIs. The images are
**not screenshots of game UI**, inventory ownership, achieved outfits, play,
saved state or interaction. They supplement the normal native UI qualification.

It launches no Activity, injects no input, and never accesses `GameStore`, the
engine, reflection or preferences. It uses the packaged art, fixed time 0,
reduced motion and no equipment overlay. It writes only its separate evidence
directory beneath the target's external files directory.

Outputs are 24 six-look companion sheets (144 combinations), 24 isolated
garment previews and two 12-style sheets for Nova and Halo. Each combination
records exact character, four garment IDs and an art-region pixel hash. PNG
hashes and the library's decode/cache observations are retained. Visual rig
inspection still requires a human/agent review of the sheets; automated
nonempty/uniqueness checks cannot establish artistic quality or garment fit.

## Prepare and build after source freeze

Only after the root confirms final candidate bytes:

```sh
python3 scripts/v117-art-test/prepare-sources.py
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
  ./gradlew -I scripts/v117-art-test/init.gradle :app:assembleProductionReleaseAndroidTest
```

The observer is copied to
`app/build/qualification-artifacts/v1.17/art-production-test.apk`. Public target
APIs must remain callable through the final release's R8 mapping. Do not add a
keep rule or rebuild a candidate silently; coordinate any such need with root.

## Exclusive device run

Only the current exclusive owner of `Luma-Refonte115-QA` / `emulator-5586` may
run the wrapper. The final game must already be installed. No AVD is started,
no game is installed/cleared/upgraded, and no display override is applied.

The separately authorized physical clone `Luma117-Secondary-F-QA` / `emulator-5592`
is also accepted only with explicit `--clone-receipt` and
`--clone-receipt-sha256`. The shared host guard validates the root's stopped-disk
copy, original exact-F U1000/51,510 baseline and clone boot identity. Its source
and the hashed receipt are copied into the evidence. Clone results are separate
native render evidence and do not claim a second migration qualification.

```sh
python3 scripts/v117-art-test/run.py \
  --serial emulator-5586 --expected-avd Luma-Refonte115-QA \
  --expected-apk PATH_TO_FINAL_APK --sha256 FINAL_SHA256 \
  --source-freeze PATH_TO_FINAL_SOURCE_FREEZE_JSON \
  --test-apk app/build/qualification-artifacts/v1.17/art-production-test.apk \
  --label final-art-gallery
```

The wrapper guards production version/code24, final APK/signing identity,
packaged original art, renderer source freeze, observer witness, unique run
token, fresh local/remote labels and every resulting PNG. It installs only its
own separate observer package, removing it after its exact identity is checked.
Failed attempts are retained rather than overwritten. Output lives under
`docs/validation/v1.17/art/native/LABEL/`.

G/code23 gallery runs on the existing F physical clone require the original hashed `--clone-receipt` plus the separate hashed `--clone-candidate-authorization` and `--clone-candidate-authorization-sha256` from root. The shared guard verifies the original stopped-copy provenance, F base, exact authorized G bytes/version/code and the explicit retained-upgrade scope. The renderer observer still never installs/upgrades the target; actual G UI upgrade qualification is separate.

Current H/code24 clone runs additionally require `--clone-successor-authorization` plus `--clone-successor-authorization-sha256`. The shared guard validates the entire immutable F physical-copy → authorized G → authorized H chain; the render wrapper never changes the installed target or calls game state. All three root records are copied into the gallery evidence.
