# 1.11 RC2 — native visual identity qualification

The exact final Lab and production APKs pass their bounded native routes, including visibly different equipped art, actual theme backgrounds and ordinary cold-process persistence. The earlier published 1.10 RC3 baseline retains two reproduced theme failures. No historical FAIL or RC1 result is relabelled as final.

## Frozen subjects and independent inputs

| Subject | Code | APK SHA-256 |
| --- | ---: | --- |
| Lab `1.11.0-sandbox-rc2` | 12 | `65bfbeab4f8b488e483a1622568540caf0315f72cf33d94876c75eafc9b4b642` |
| Production `1.11.0-rc2` | 12 | `5a77d0c5b15dcdbb919bc3ea248ac9ac7b1b642898ee0fdd6cd8f855a1d09811` |

The independent Candidate source SHA-256 is `5e29a15ec184114010a70255c0c29d7ae68a99baa691dc0265712860fee589db`. Lab test APK SHA-256 is `7a0e2b6a48a19eaf6ea80a1338f420aea7253249c32172e0716203f47c16c61f`; production visual test APK is `30a76bf57c54bdc62e32a5c4027fe8ccc3ffb687495fd82d1e4e83e58e1d5faf`. [Build provenance](final-harness-builds.json), [exact Lab host archive](rc2-lab-runner-provenance.json) and [production host source](../production-startup-k/runner-source/run.py) preserve inputs. The historical [1.10 framework inventory](v110-framework-provenance.json) and 106 oracle fixtures remain unchanged.

The two retained AVDs run Android 36 / arm64-v8a with actual 16,384-byte pages, `1080×2340` display and density 440. Lab uses explicitly owned `hexkeep-play-36-16k` / emulator 5580; production uses `Luma-Release111-K` / emulator 5586. Emulator 5582 is excluded. Product APKs were already frozen; these runners install only their independent test APKs.

## Exact final results

| Route | Actual Journey levels | Checks | Original full frames | Unscaled analysis crops | Profile / rotation restored |
| --- | ---: | ---: | ---: | ---: | --- |
| [Lab final](ui-sdk-final-rc2-lab50/host-report.json) | 1–50 | **1,589 PASS** | 115 | 19 | true / true |
| [Production final](ui-sdk-final-rc2-production40/host-report.json) | 1–40 | **935 PASS** | 41 | 19 | true / true |

Lab has 1,567 preparation checks and 22 separate cold checks; production has 912 preparation checks and 23 cold checks. The extra production check explicitly verifies its restricted scope. Every board on the recorded routes has separate successful clear, visible-three-star and certified-budget assertions: **150 Lab / 120 production**. No profile or progress was injected to fund purchases. Native SDK accessibility actions and real touchscreen events act on observed nodes/bounds; shell input is forbidden.

Lab additionally records tutorial skip/resume and seven practice lessons, physical gate drags, Home/objective/wardrobe/story/shop navigation, map/challenge, wrong-direction gestures, free recovery, powers/rewards, styles, guardians, landscape and six languages. Its 14 real selected-fill pixels and four footer targets pass. Production intentionally runs the smaller Journey/appearance/menu/gesture/recovery set, with two selected-fill pixels and one footer target. Minimum observed full footer height is 48.36 dp in both. Opening a menu or pinning a preview must not silently grant an item, equip it or debit currency.

[Lab analysis](rc2-lab-analysis.json) / [CSV](rc2-lab-raster-comparisons.csv) and [production analysis](rc2-production-analysis.json) / [CSV](rc2-production-raster-comparisons.csv) derive their counts from raw reports without changing them.

## Actual art and cold process

Each route explicitly buys four previously unowned clothing pieces, closes the purchase offer and verifies unchanged worn art, then selects Equip and measures changed portrait pixels. Earned Lantern Leaf, Miko and the free Retro visual style must separately change the art. The original production [Miko frame](../../../screenshots/v1.11/ui-bugs/ui-sdk-final-rc2-production40/prepare/visual-equipped-miko-original.png) shows the actual companion, four equipped clothes and three gear slots, with compact item names. This original capture was visually inspected; its analysis crop remains separately labelled.

| Real action | Lab changed pixels | Production changed pixels |
| --- | ---: | ---: |
| Equip Cupid hat | 8,847 | 8,841 |
| Equip Little witch outfit | 7,799 | 7,801 |
| Equip Cupid shoes | 3,397 | 3,395 |
| Equip Little witch gloves | 12,158 | 12,160 |
| Equip earned Lantern Leaf | 3,968 | 3,969 |
| Select companion Miko | 37,462 | 37,465 |
| Select free Retro style | 24,642 | 24,626 |
| Select Lagoon: blank Home background | 26,576 | 26,576 |
| Select Lagoon: blank gameplay background | 26,576 | 26,576 |

Counts are maximum RGB-channel differences above 8 within observed, unscaled regions. The two blank theme regions contain 26,576 pixels each. Compare within a route; different captures and geometry do not support exact cross-version or cross-device pixel equality. Closing a preview, each clothing purchase without Equip and changing only the background all leave the worn portrait unchanged (0 pixels).

Reduced motion is explicitly enabled through actual Settings. Three native screenshots per state measure stability; all 19 observations per route report 0 repeat-noise pixels above RGB 8. A positive art change must exceed 64 pixels and four times the measured repeat noise plus 32. This is a visibility regression threshold, not a human-perception or artistic-quality score.

The host force-stops the target, confirms no target PID, then starts an independent instrumentation process. Measured PIDs are **7727→9849 Lab** and **3982→5250 production**. The new process reads all ten actual identity fields and the balance created by the completed UI actions. Its actual final portrait differs by 0 pixels from the previous observed portrait. Finally, original guest preferences/file set and rotation settings are restored. Production's original guest was genuinely created by the preceding [first production startup](../production-startup-k/README.md), rather than seeded or claimed as an older profile.

Independent [Lab](ui-sdk-final-rc2-lab50/host-raster-verification.json) and [production](ui-sdk-final-rc2-production40/host-raster-verification.json) Pillow/ImageChops checks verify that all 19 crops are exact subregions of original full-frame PNGs and reproduce every one of the 16 native pixel counts/RGB means. Only the retained third frame is independently re-read; the two prior stability frames are not retained, so their stability remains a native observation.

## Separate evidence and honest limits

The fresh K [RC2 route](../fresh-install-rc2/README.md) separately passes 83 tutorial, 45 avatar and 36 language assertions with 47 original frames. The production [startup/audio route](../production-startup-k/README.md) separately passes seven assertions with one original frame. Digital mixed-frame progress and background/foreground pause/resume do not establish speaker quality or listening preference.

Baseline [A](ui-sdk-baseline-rc3-a/host-report.json) and [B](ui-sdk-baseline-rc3-b/host-report.json) are preserved harness failures. Baseline [C](ui-sdk-baseline-rc3-c/host-report.json) globally fails 929 recorded checks because both theme regions remain unchanged, while its 22-check ordinary cold phase passes. The original [analysis](baseline-analysis.json) and independent [PNG verification](ui-sdk-baseline-rc3-c/host-raster-verification.json) retain that FAIL. Its explicit clothing equips did visibly change art; this bounded observation does not dismiss a confusing purchase/equip flow or cover every item.

This qualification covers **50 native Lab levels and 40 native production levels on 1.11**, not 100 or 1,000 native levels. It does not qualify a phone, larger fonts/TalkBack comfort, every catalog-language fit, human enjoyment/difficulty/retention, two real Google accounts, friends/gifts, real ads or LumaGold purchases. Server Gold is unknown without sign-in, not assumed zero. Ordinary force-stop persistence is not pending-write failure or process-death recovery. No native 1.11 storage-fault result is asserted. Play and real commerce remain paused; package integrity, publication and Hosting have their own parent-owned receipts.
