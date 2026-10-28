# Native visual identity audit — 1.11 framework

This independent SDK runner copies the immutable [1.10 framework](../v110-ui-test/README.md). The [source inventory](../../docs/validation/v1.11/ui-bugs/v110-framework-provenance.json) records all 117 copied source files. The original framework and its reports are preserved; no old PASS is relabelled as visual proof.

The first subject is the exact installed **Lab 1.10.0-sandbox-rc3/code 11**, SHA-256 `d1b085b4d9bbcf450037069ed15c7cea89b0e3e759b16e587cf3ca016a7c493f`. A later candidate requires its own explicit version, code and frozen APK. Preparation or compilation is not a result. [Audit evidence and status](../../docs/validation/v1.11/ui-bugs/README.md).

## What was missing

The old avatar scenario checked read-only preview/pin state and navigation. It never bought or equipped clothing. Style tests read the stored style ID and Active style label; their screenshots had no image comparison. The fourteen actual mint samples covered tab/chapter fills, not worn art. A correct saved ID beside unchanged artwork could pass those assertions. This is a coverage gap, not by itself a reproduced product defect.

## Actual action and raster protocol

`avatar-visual` runs after genuinely earned Journey progression. It requires 20 real clears and 2100 earned LumaCoins, then performs explicit native purchases/equips of four clothing slots, earned head gear, Miko, a free visual style and Lagoon. Buying clothing alone must leave the worn appearance unchanged. Closing previews and reopening an equipped offer must change no ownership/balance or rendered identity. The existing menu, wrong-direction gesture and free-recovery scenarios provide independent negative checks; no Google, ad, paid-currency, shell input, reflection or preference/progression injection is used.

Reduce motion is enabled through its visible Settings control. Each raster observation takes three real SDK screenshots. We retain the original full frame and an explicitly named **unscaled analysis crop**. RGB changes above 8/channel are counted; repeat-frame noise must stay below 0.1%. Change acceptance requires more than 64 pixels and more than four times the measured noise plus 32. These thresholds describe deterministic rendering visibility, not human perceptual similarity or artistic quality.

The portrait crop uses an observed non-clickable `Equipped companion portrait` accessibility node where available. The 1.10 fallback derives its scale/position from the real visible Hat slot: central art logical x109–281/y127–299, excluding labels, prices and slot glyphs. It never renders an expected avatar using product code. The theme comparison uses a blank left gutter in the observed game window, excluding text/cards/board/portrait; Home and gameplay are compared separately.

The host executes two instrumentations, with a real target force-stop and absent PID between them. The second process must have a different measured PID, read the completed UI actions’ saved identities/balance, and match the last actual equipped art crop. A warm Activity recreation is not described as cold persistence. Expected observations are audit files, never written into game preferences.

Visual mismatches are recorded without immediately aborting the remaining raster checks, so the same run can examine persistence. Missing controls, unsafe preconditions or failed state transitions still abort. A global FAIL remains a FAIL even if a later cold phase passes.

## Device and restoration

The host accepts emulator-5580 / `hexkeep-play-36-16k` for Lab/production and, exclusively for production, emulator-5586 / `Luma-Release111-K`. It confirms the exact AVD pair, emulator identity, actual 16384-byte pages and the installed APK’s SHA-256. It refuses stored signed-in identities and backs up the complete private preference file set outside Git. It restores original bytes, ownership/permissions, file set and rotation settings in `finally`. Emulator 5582 is never eligible. It installs only the independent test APK, not a game update.

```sh
JAVA_HOME=/tmp/luma-jdk21/jdk-21.0.13+9/Contents/Home \
  ./gradlew --no-daemon -I scripts/v111-ui-test/init.gradle \
  -x :app:compileLabReleaseJavaWithJavac -x :app:minifyLabReleaseWithR8 \
  :app:assembleLabReleaseAndroidTest

python3 scripts/v111-ui-test/run.py --label UNIQUE-LABEL \
  --cases journey,avatar-visual,menus,gestures,recovery --journey-through 100 \
  --expected-apk /absolute/path/to/frozen-lab.apk \
  --expected-version 1.10.0-sandbox-rc3 --expected-code 11
```

Reports and per-phase actions go under `docs/validation/v1.11/ui-bugs/ui-sdk-LABEL`; original screenshots and labelled derivative crops go under `docs/screenshots/v1.11/ui-bugs/ui-sdk-LABEL`. `prepare` and `cold` retain distinct files/results. Provider acceptance, physical-phone quality, accessibility comfort, human enjoyment, retention and commercial readiness remain outside this protocol. Play and real commerce remain paused.


## Separate production and fresh-install routes

Production earned-visual input is restricted to `avatar-visual,journey,menus,gestures,recovery`; provider, ad and LumaGold actions remain forbidden. Build its test APK with `-PlumaUiTarget=production-visual`, the production AndroidTest task and the same excluded product compile/minify tasks. Startup/audio is a separate test APK compiled with `-PlumaUiTarget=production`; it is not a substitute for appearance or gameplay coverage. Supply `--variant production`, the exact frozen production APK, version/code and its matching test APK explicitly. A Journey 40 route is 40 real UI levels, not 100.

The new fresh helper targets only the explicitly owned `Luma-Release111-J` on emulator 5584 or `Luma-Release111-K` on emulator 5586, with actual 16 KiB pages, a genuinely absent Lab package and no profile deletion. It defaults to `tutorial` alone, with explicit 1.11.0-sandbox-rc1/code 12 arguments. The first-install account/commerce boundary remains unchanged. A fresh run has its own evidence; no existing guest is relabelled as fresh.

The main Lab50 wrapper source is archived before the production-only K guard extension: [provenance](../../docs/validation/v1.11/ui-bugs/rc2-lab-runner-provenance.json). The original fresh J wrapper is also [archived](../../docs/validation/v1.11/ui-bugs/rc1-fresh-wrapper-provenance.json). Exact-byte reports are linked to those inputs rather than a later script version.

## Recorded final RC2 qualification

[Final method and scoped results](../../docs/validation/v1.11/ui-bugs/SDK_UI_FINAL.md) link the unchanged raw reports. The final Lab route runs 50 actual Journey levels and passes 1,589 checks (115 original frames + 19 labelled analysis crops). Production runs 40 actual levels and passes 935 checks (41 originals + 19 crops). Both perform the 16 within-run raster comparisons and a real target force-stop/new-process verification, then restore the original profile and rotation settings. These counts do not establish 100 native levels on 1.11.

The baseline 1.10 RC3 remains a global FAIL with two reproduced unchanged-theme backgrounds; four clothing slots, gear, companion and free-style changes did work after explicit Equip in that bounded route. Intermediate fixture failures and RC1 fresh evidence remain separate. Final fresh K and production startup are separately scoped in their own reports.

`analyze-raster.py --label LABEL` independently re-reads the original full frames, verifies that every saved crop contains exactly their observed pixels, and recalculates all 16 native changed-pixel/RGB means using Pillow/ImageChops. It never calls a product renderer or creates expected art. This step requires Pillow (available in the bundled workspace Python). Only the retained third frame per state can be independently re-read; three-frame stability is recorded by the native runner, not reconstructed from missing earlier frames. The baseline and both final reports have successful independent measurement records; the baseline's product acceptance remains FAIL.
