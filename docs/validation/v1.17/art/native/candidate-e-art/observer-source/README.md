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

```sh
python3 scripts/v117-art-test/run.py \
  --serial emulator-5586 --expected-avd Luma-Refonte115-QA \
  --expected-apk PATH_TO_FINAL_APK --sha256 FINAL_SHA256 \
  --source-freeze PATH_TO_FINAL_SOURCE_FREEZE_JSON \
  --test-apk app/build/qualification-artifacts/v1.17/art-production-test.apk \
  --label final-art-gallery
```

The wrapper guards production version/code21, final APK/signing identity,
packaged original art, renderer source freeze, observer witness, unique run
token, fresh local/remote labels and every resulting PNG. It installs only its
own separate observer package, removing it after its exact identity is checked.
Failed attempts are retained rather than overwritten. Output lives under
`docs/validation/v1.17/art/native/LABEL/`.
