# Native 1.10 qualification — rc3 accepted within the scopes below

The selected release is **production 1.10.0-rc3 / Lab 1.10.0-sandbox-rc3, code 11**. The [production manifest](../../../releases/manifest-1.10.0-rc3.json) and [Lab manifest](../../../releases/manifest-1.10.0-sandbox-rc3.json) identify the four current APK/AAB files. Builds, source tests and earlier native runs do not substitute for current-byte acceptance.

| Current rc3 run | Exact package SHA-256 | Recorded status |
| --- | --- | --- |
| [Fresh guest I](ui-sdk-fresh-guest-i/host-report.json) | Lab `d1b085b4d9bbcf450037069ed15c7cea89b0e3e759b16e587cf3ca016a7c493f` | **91 checks PASS / 17 original PNGs** |
| [Main final3-full100](ui-sdk-final3-full100/host-report.json) | Same Lab hash | **2,514 checks PASS / 96 original PNGs**; original profile and rotation restored |
| [Production startup](ui-sdk-production-final3/host-report.json) | `29a68cdcaeb61850d55afd101f03bd8c42a075cc6d6631205fc176e499dd70b4` | **7 checks PASS / 1 original PNG**; original environment restored |

Fresh I uses emulator-5598, API 36 ARM64 with **actual 16 KiB pages**. Lab was genuinely absent before installation; the newly created guest is retained, rotation settings restored and production absent before/after. Its91 checks cover first arrival, optional interactive tutorial/gestures, skip/resume, recreation and the once-only gift. It is not a restored older guest or preserved-production-APK comparison. No Google account, ad, purchase or profile injection is involved.

The separate production rc3 run uses emulator-5580, API 36 ARM64 with actual 16 KiB pages. Its seven passing assertions verify the exact installed version, real accessible window, process-owned audio track with advancing mixed frames, actual background transition, music pause and foreground resume. The wrapper restored its original environment. It operates no gameplay, menu, account, ad or payment control; the wrapper’s generic `journey_through:100` metadata is not a production Journey run.

## Main acceptance method and new safeguards

The independent [1.10 SDK framework](../../../scripts/v110-ui-test/README.md) operates the installed APK through its actual accessibility controls and native touchscreen input at observed node bounds. It seeds no progression, reward, stock, identity or equipped item. The main wrapper checks exact installed/expected APK bytes, rejects stored signed-in identities, backs up original private preference files outside Git and restores the full file set/bytes plus rotation settings in `finally`.

The main route exercises seven tutorial lessons with saved-phase/preview recovery, hub/selected navigation, preview/pin without grants or automatic equipment, ten read-only story chapters with real counters and locked Guardian access, real map/challenge gestures, Journey 1–100, zero-budget recovery, earned powers/rewards, styles, Guardians 1/5 and six-language navigation. The completed rc3 report contains **300 passing per-board assertions**: completion, visible three stars and certified budget for each of Journey 1–100. All 2,514 recorded assertions pass. The full route is SDK-operated and follows certified action paths; this does not measure the experience or completion rate of unaided people.

Two safeguards were added during this release:

- **Actual selected pixels:** fourteen flat controls (Shop Powers/Appearance, companion Appearance/Equipment, chapter chips 1–10) must be semantically selected and have actual opaque mint `ffe1fff2` in an original `UiAutomation.takeScreenshot()` bitmap, at centreX and 20%down the fully visible node, with RGB tolerance ±3. This excludes gradient surfaces; it catches passing small palette indices as transparent ARGB colours.
- **Entire footer targets:** SDK scrolling reaches stable observed bottom bounds for Home Enter a challenge, companion Open inventory, notebook Prism guardians and Collection `›` after Companions/All. The visible native target must retain at least 47dp height and fit entirely above the fixed navigation. This catches inaccessible lower content clipped by the page-height/viewport calculation.

The rc3 [action observations](ui-sdk-final3-full100/actions.json) record all fourteen selected samples as exactly `ffe1fff2`; all four footer targets retain **48.36dp** of visible height and stay above the fixed navigation after three stable bottom observations. The four original footer PNGs are included in the 96 main captures.

Rotation checks use the actual SDK display rotation, original screenshot dimensions and return-to-prior native view dimensions, twice consecutively within a bounded wait. Screen, selected-state and saved history are asserted separately. A same-named header or fixed sleep is not proof that portrait restoration finished.

All PNGs are original SDK captures, copied without resizing, recolouring, stitching or overlaid text. The completed rc3 run includes French [Home](../../screenshots/v1.10/ui-sdk-final3-full100/home-fr-localized.png), [companion](../../screenshots/v1.10/ui-sdk-final3-full100/avatar-fr.png), [ready rewards](../../screenshots/v1.10/ui-sdk-final3-full100/ready-fr.png), [notebook](../../screenshots/v1.10/ui-sdk-final3-full100/story-fr.png), [shop appearances](../../screenshots/v1.10/ui-sdk-final3-full100/shop-looks-fr.png) and [events](../../screenshots/v1.10/ui-sdk-final3-full100/events-fr.png), each at its original **1080×2340** resolution. The returned-portrait companion and four footer captures have that same original resolution. No earlier image is relabelled as rc3 media. Capture hashes and original native selected flags stay with each report. The original 106 fixture JSON files and their manifest remain byte-identical to the 1.9 source oracle, not silently reauthored puzzles.

## Intermediate reports retained with their original hashes

| Intermediate run | Original Lab/production hash | Scope/result |
| --- | --- | --- |
| [Companion pilot](ui-sdk-companion-pilot/host-report.json) | Lab `aee689d42d2266c866a7f21939540e260a1fd406273f69f885559fc87433a9a5` | Raw **FAIL after 202 recorded checks**,15 PNGs; Rewards requested without five real Journey clears |
| [Fresh F](ui-sdk-fresh-guest-f/host-report.json) | Same early Lab hash | PASS 85/17; first installation, earlier portrait-return synchronization |
| [rc1 full route](ui-sdk-final-full100/host-report.json) | Lab `70e693abb589f409a74446cc2593a0faefe30967c73c32fc6d6bebf7bc5272f4` | PASS 2,472/92; main environment/rotation restored |
| [Fresh G](ui-sdk-fresh-guest-g/host-report.json) | Same rc1 Lab hash | PASS 91/17; new guest retained, rotation restored |
| [rc1 production startup](ui-sdk-production-final/host-report.json) | `c5b6c1fd155867d887674b820f813209c1fa23ab759f180e4c6c349f3caf2579` | PASS 7/1; startup/digital audio only, environment restored |
| [rc2 full route](ui-sdk-final2-full100/host-report.json) | Lab `ad6acd88571998ea5f83ad698e709a817844998be468fe821a203fd325083a78` | PASS 2,500/92; includes 14 selected-pixel checks, environment/rotation restored |
| [Fresh H](ui-sdk-fresh-guest-h/host-report.json) | Same rc2 Lab hash | PASS 91/17; first installation |

These are genuine engineering results for their exact intermediate bytes. They do not qualify rc3. The original raw pilot failure, partial checks, asynchronous landscape capture and later passing reports remain unchanged. A successful optimal playthrough did not cover the later identified clipped-footer defect; rc3 adds explicit target-visibility acceptance.

Eight unreleased rc1/rc2 APK/AAB files are preserved byte for byte **locally in an ignored build folder**, with [recorded hashes/paths](intermediate-retention.json). Their [rc1 manifests](rc1-artifact-manifests/manifest-1.10.0-rc1.json) and [rc2 manifests](rc2-artifact-manifests/manifest-1.10.0-rc2.json), reports and captures remain documentation. Those eight binaries are not published download links on main. Published 1.9 and earlier artifacts remain unchanged. The earlier F/pilot hash has its own report provenance; it is not claimed to be one of those eight retained outputs.

## Limits

All completed runs listed here use API 36 ARM64 emulators with actual 16 KiB pages. Source assertions and scripted optimal moves do not measure human difficulty or retention. Digital audio observations concern process-owned mixed frames, pause and resume, not speaker/headphone audition.

No 1.10 native filesystem-fault run is claimed: event/Guardian fault selectors remain blocked. Source persistence tests are separate and do not become real disk-failure or process-death evidence. These reports establish no real Google SSO, two-account gifts, commercial ad or purchase acceptance, physical-phone quality, low-end latency, TalkBack/large-font comfort, native-speaker review, human enjoyment, revenue, acquisition cost or a hit outcome. Play and real commerce remain paused.
