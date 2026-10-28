# Production commerce native browse — 1.12

This independent framework runner uses the real accessible Android window. It navigates from Main through Shop, LumaGold and the visible wallet button. It uses no application class, reflection, hidden Activity entry, input shell command, profile seeding or expected renderer image.

The wrapper accepts only retained `Luma-Release111-K` on emulator 5586, Android 36 with actual 16 KiB pages. It refuses an advertising opt-in or stored signed-in Google identity before any launch, backs up the complete original preference file set and the ordinary SDK test APK identity, verifies the exact frozen production APK/version/code, and checks that upgrading did not change original preferences before first launch. The original guest files are restored byte for byte after success or failure. The retained Lab profile and APK are also checked unchanged. The isolated test package `com.androidhit.lumadrift.commercetest` must initially be absent, is verified by installed APK SHA-256 and is removed afterwards. The ordinary SDK test package is never replaced. No other emulator is eligible.

The bounded click allowlist contains only language/settings/home/shop/skip/wallet navigation. No login, age, privacy option, ad load/watch/opt-in, purchase, simulation, refund or refresh control is invoked. Normal billing connection/catalogue query when opening Commerce is permitted; this is not Google Play purchase acceptance. The frozen preparation APK has test ads, an empty wallet API URL and paid draws disabled. Advertising is not requested with the original explicit opt-out.

All six supported languages receive original full-frame screenshots of Home, the in-game LumaGold page, Commerce hero, account status, first cosmetic offer and the unchanged optional consent controls. Assertions observe the actual unknown premium state, distinct earned currency label, disabled first cosmetic spending button, unchecked consent, and unchanged saved economic/progression values. These are usability primitives and safety checks, not measurements of comprehension, aesthetic preference or retention.

Build only against a frozen production candidate:

```sh
JAVA_HOME=/tmp/luma-jdk21/jdk-21.0.13+9/Contents/Home \
  ./gradlew --no-daemon --offline -I scripts/v112-commerce-ui/init.gradle \
  -x :app:compileProductionReleaseJavaWithJavac -x :app:minifyProductionReleaseWithR8 \
  :app:assembleProductionReleaseAndroidTest

python3 scripts/v112-commerce-ui/run.py \
  --apk releases/luma-drift-1.12.0-rc2.apk \
  --test-apk app/build/qualification-artifacts/v1.12/commerce-isolated-rc2-test.apk \
  --version 1.12.0-rc2 --code 13 --label UNIQUE-LABEL
```

Each label produces new immutable output under `docs/validation/v1.12/commerce-native/` and original screenshots under `docs/screenshots/v1.12/commerce-native/`. Failed native attempts retain their actual failure and successful restoration; they are never silently relabeled as product acceptance.

The packaging task copies the signed test APK to that unique path before the Gradle task finishes. Do not use the shared generic AndroidTest output path while another harness may build. The retained `rc2-a` attempt used the earlier shared package/output arrangement; its runtime checks remain historical evidence with an explicit artifact-retention limit. The isolated replay is the current qualification.
