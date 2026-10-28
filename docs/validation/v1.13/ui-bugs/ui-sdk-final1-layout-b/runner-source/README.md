# Native SDK qualification — 1.13 home

This separate harness preserves the 1.12 runner and records the exact original file inventory in [provenance-v112.json](provenance-v112.json). All 105 gameplay fixtures and their manifest retain their original bytes. Compiling this harness prepares the tests; runtime pass claims require a retained native report and original frames.

The retained-profile wrapper accepts only `emulator-5580` / `hexkeep-play-36-16k` for Lab, or `emulator-5586` / `Luma-Release111-K` for production. It checks the actual 16 KiB page size, installed version/code and frozen APK SHA-256. It refuses a stored Google identity, backs up/restores the complete original private preference file set byte for byte, and restores original rotation and any temporary display override. Other emulators are ineligible.

## Home routes

- `house-smoke`: actual room scene, accessible controls, movement, visible cuddle, wardrobe navigation and unchanged puzzle/economy values.
- `house`: real earned furniture purchase with an exact LumaCoins debit, separate placement, rotate/move/store, surfaces and a distinct cold process checkpoint that preserves room, current board and coins. Requires a sufficient genuinely earned balance; the `journey` route can earn it first.
- `house-locales`: six actual languages, original room/catalogue/detail frames and reachable localized controls.
- `house-layout`: actual 320 dp display, landscape, reduced motion and reachable house controls, with restored original display/rotation afterwards.

The copied Journey, gesture, recovery, shop and avatar routes remain available for focused regression adaptation. Original 1.12 runtime reports are historical evidence about 1.12, not 1.13 results.

## Build and run

Use an explicitly frozen app candidate and version/code. The initial defaults are 1.13 RC1/code 14:

```sh
JAVA_HOME=/tmp/luma-jdk21/jdk-21.0.13+9/Contents/Home \
  ./gradlew --no-daemon -I scripts/v113-ui-test/init.gradle \
  -x :app:compileLabReleaseJavaWithJavac -x :app:minifyLabReleaseWithR8 \
  :app:assembleLabReleaseAndroidTest

python3 scripts/v113-ui-test/run.py --label UNIQUE-LABEL \
  --cases house-smoke --expected-apk /absolute/path/to/frozen-lab.apk \
  --expected-version 1.13.0-sandbox-rc1 --expected-code 14
```

The SDK test package is isolated as `com.androidhit.lumadrift.hometest`, verified against its frozen APK and removed afterwards; the historical ordinary `.test` package is not replaced. The packaging task copies the signed test APK to `app/build/qualification-artifacts/v1.13/home-lab-rc1-test.apk` (or the corresponding production file).

The wrapper verifies that the installed target matches the frozen APK before installing its SDK test APK. It never installs or replaces the target automatically. `install-candidate.py` is a separate original-profile-preserving update step owned by the release operator.

New raw output goes under `docs/validation/v1.13/ui-bugs/ui-sdk-LABEL`; original PNG frames and explicitly labelled unscaled analysis crops go under `docs/screenshots/v1.13/ui-bugs/ui-sdk-LABEL`. No product renderer creates an expected screenshot. Every run needs a new label; failures remain preserved.

The separate `run-fresh-home.py` wrapper accepts only explicitly owned `emulator-5588` / `Luma-Release113-Home-N`, with the Lab package genuinely absent before installation. It refuses an existing profile and never clears it. Its first process saves the actual switch action, then the wrapper force-stops it and proves a distinct process resumes that history before completing the other two exits and entering the house. This route cannot establish a first-install result on an already retained guest.

Scope: these protocols establish observed native behavior, persistence and reachability on their recorded emulator configuration. They do not measure human comprehension, delight, retention, artistic preference or physical-phone comfort, and they do not invoke real Google, ads or Play purchase acceptance.
