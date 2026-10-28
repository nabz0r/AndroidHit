# Native SDK qualification — 1.12 UI

This is a new copy of the preserved [1.11 framework](../v111-ui-test/README.md), with an [exact source inventory](provenance-v111.json). All 105 gameplay fixtures and their manifest remain byte-identical. Historical scripts and reports are unchanged. Compiling this harness is preparation; it is not a passing runtime result.

The runner accepts only the explicitly owned `emulator-5580` / `hexkeep-play-36-16k` pair (Lab or production), or `emulator-5586` / `Luma-Release111-K` for production only. It checks the actual 16 KiB page size, installed version/code and frozen APK SHA-256. It refuses a stored Google identity and backs up/restores the original private preference file set and rotation state. Emulator 5582 is never eligible.

## New scope

`shop-focus` exercises named LumaCoins and LumaGold states, separate currency node bounds, all three explicit shop categories, and normal in-canvas power/clothing details. The details must expose the real canvas accessibility host and an explicit Close action. Selection and Close must not alter currency, stock, ownership, equipment or progression. One explicit earned three-Spark pack purchase then checks the displayed quantity and price against the actual stock increase and exact debit. Native bounds check that the tested category and Close controls retain a 48 dp target (0.5 dp rounding tolerance). Premium navigation does not open any account, wallet, ad or real-money provider control.

The copied `avatar-visual` protocol still requires genuinely earned progression and performs explicit earned purchases, exact debit checks, separate Equip, real native portrait/theme raster comparisons and a true force-stop/new-process persistence phase. Previews and purchases without Equip must not change worn art. The added `shop-locales` route records actual Home currencies, shop powers, power detail, appearance and LumaGold pages in all six languages. The older general six-language route remains available separately. Reduced motion is enabled through the visible setting for raster comparisons.

The Lab-only `power-return` route consumes actual hint charges through confirmations, makes a real board move, and checks Close plus rotation/System Back from the truly empty-power detail. It requires the same live run, history and move budget on return and no root-navigation footer while the game is suspended. No balance or stock is injected.

Shop tabs now use semantic selected-state and actual target bounds instead of the old flat mint color oracle, because the organic redesign has no flat-color tab contract. Historical mint-pixel assertions in other unchanged screens remain as separate checks; they are not described as evidence about shop visual appeal.

## Build and run

Use a frozen app candidate and explicit version/code; the defaults are preparation placeholders for 1.12 RC2/code 13.

```sh
JAVA_HOME=/tmp/luma-jdk21/jdk-21.0.13+9/Contents/Home \
  ./gradlew --no-daemon -I scripts/v112-ui-test/init.gradle \
  -x :app:compileLabReleaseJavaWithJavac -x :app:minifyLabReleaseWithR8 \
  :app:assembleLabReleaseAndroidTest

python3 scripts/v112-ui-test/run.py --label UNIQUE-LABEL \
  --cases journey,shop-focus,shop-locales,power-return,avatar-visual,menus,gestures,recovery,powers,languages \
  --journey-through 40 --expected-apk /absolute/path/to/frozen-lab.apk \
  --expected-version 1.12.0-sandbox-rc2 --expected-code 13
```

The production visual route is separately compiled with `-PlumaUiTarget=production-visual`, the production AndroidTest task, its matching test APK and `--variant production`. Production permits startup alone, the narrow `shop-focus` case alone, or bounded earned-visual/Journey/menu/negative cases with avatar-visual. No provider acceptance.

New raw output goes under `docs/validation/v1.12/ui-bugs/ui-sdk-LABEL`; originals and explicitly labeled unscaled analysis crops go under `docs/screenshots/v1.12/ui-bugs/ui-sdk-LABEL`. `analyze-raster.py --label LABEL` independently verifies the actual retained original/crop pixels. No product renderer produces an expected screenshot.

Scope limits: this protocol does not measure human comprehension, delight, retention, artistic quality or physical-phone comfort. It does not enable or accept real Google, AdMob or Play purchases and does not deploy the public website.
