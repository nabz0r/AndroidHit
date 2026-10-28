# Luma Drift 1.13 — Solve a little. Come home.

28 October 2026 · production **1.13.0-rc2**, isolated Lab **1.13.0-sandbox-rc2**, version code **14**.

The main game remains the puzzle. This candidate gives earned rewards a concrete destination: a little furnished home, the actually equipped companion, a hug and a furnishing to work towards. A short real puzzle introduces the first switch before the first room visit.

[Home rules and economy](HOME_1_13.md) · [Executed qualification](validation/v1.13/README.md) · [Architecture](DEVELOPMENT.md) · [Current roadmap](ROADMAP.md).

## What changes for the player

- **Arrange a little room.** A room seen from three quarters has twelve original furnishings. Move, rotate and store an owned object; preview its complete footprint before confirming. Furniture cannot overlap another solid object or the companion. A rug can sit under furniture.
- **Start with a home.** Bed, lamp, plant, rug and cushion are included without spending the existing 180-LumaCoin opening balance. Seven additional furnishings use existing earned LumaCoins. Four wall finishes and four floors each include two free choices.
- **Spend time with the companion.** Call it across a clear floor path, share a hug and use furniture. The scene draws the actual owned/equipped character, style, clothing and gear. Rest, lamp and cuddle animations are temporary; confirmed layout, ownership, appearance and companion position persist.
- **Choose one wish.** Pin one unowned furnishing and see actual LumaCoins progress. Buying grants ownership, then the player explicitly chooses placement. Storing a furnishing preserves ownership.
- **Reach the first rule sooner.** A skippable three-piece board uses the real engine to show a switch changing a gate. Its actual action history survives a restart. Completing or skipping it grants no campaign clear, coins or power. Existing progressed/tutorial profiles are not forced into the new opening. The seven optional lessons remain separate and replayable.
- **Keep the puzzle nearby.** Play remains an explicit home action; the existing 1,000 Journey and 240 Remix boards, daily/weekly activities and saved in-progress board are preserved.

## Current RC2 candidate

Production **1.13.0-rc2**, separate Lab **1.13.0-sandbox-rc2**, code **14**, replace the unshipped RC1 after its Later→Resume tutorial crash was reproduced. The correction resets obsolete lesson feedback and guards terminal lesson text lookup. New signed hashes, binary verification and final native evidence must identify RC2; the historical RC1 observations below do not certify the replacement.

[Production APK](../releases/luma-drift-1.13.0-rc2.apk) · [AAB](../releases/luma-drift-1.13.0-rc2.aab) · [Production manifest](../releases/manifest-1.13.0-rc2.json) · [Lab APK](../releases/luma-drift-1.13.0-sandbox-rc2.apk) · [Lab manifest](../releases/manifest-1.13.0-sandbox-rc2.json).

| Current signed artifact | Bytes | SHA-256 |
| --- | ---: | --- |
| [Production APK](../releases/luma-drift-1.13.0-rc2.apk) | 7,806,558 | `c68be62fd940e753fc06c4b5677a48cafaf077a4710755b3bdf96467d571e31b` |
| [Production AAB](../releases/luma-drift-1.13.0-rc2.aab) | 11,210,021 | `dd8d4ddc67e03f9dc6f8f85a816b494d5628b6f11cb64cb24ad97e48e7264982` |
| [Lab APK](../releases/luma-drift-1.13.0-sandbox-rc2.apk) | 7,789,865 | `85cd018d5d7ee17f7343921d5367859e261acb7bdc1443bcd02d6bd7618953fa` |
| [Lab AAB](../releases/luma-drift-1.13.0-sandbox-rc2.aab) | 11,200,263 | `1e109751f93ef80d94b2b71251700e019f6bd73af28aa59229fb686f79d52f18` |

[RC2 source/artifact freeze](validation/v1.13/source-freeze-rc2.json) · [RC2 successful build](validation/v1.13/build-rc2.txt) · [RC2 binary verification](validation/v1.13/binaries-rc2/binary-verification.txt). Android 8+ / API26; production package `com.androidhit.lumadrift`, Lab package `com.androidhit.lumadrift.sandbox`.

## Main publication

The RC2 source is published on `main` at [commit `67774c14`](https://github.com/nabz0r/AndroidHit/commit/67774c143a60017049207adcedf2ea63fe048d97), tree `10fa8ffd092aaedd3fb66efb82a84be4b96b858d`. The [independent public-download receipt](validation/v1.13/publication.json) records all four immutable APK/AAB routes returning HTTP200 with the exact packaged bytes and SHA-256 hashes: **38,006,707 bytes total**. This verifies public binary delivery; Play publication and Firebase website delivery keep their own records.

## Local save and economy

The room uses the existing device-local save and LumaCoins. There is no home currency, automatic coin grant, LumaGold conversion, server decoration state or cloud progression. Missing room keys return a free starter arrangement without rewriting the old wallet. Existing progression, wardrobe and equipment IDs remain.

Furniture/surface debit and ownership share one exact tracked commit. All room reads settle pending writes; refusal exposes the existing Retry barrier. Placement/companion position use one complete immutable layout record. Repeating a retry cannot double-charge an owned object. The source tests cover refusals before memory changes and Android-style memory-before-disk failure, including other store handles and cold restoration. These are controlled preference-double tests, not an assertion that every physical storage failure has been reproduced.

## Qualification and limits

The completed source checks cover **891 home/catalogue/local-save assertions** and **18 checks on the actual opening board**, plus retained store/tutorial regressions. The [RC2 build](validation/v1.13/build-rc2.txt) succeeds in 1m10s. Its [binary audit](validation/v1.13/binaries-rc2/binary-verification.txt) verifies both APK/AAB signatures, 16 KiB ZIP/native alignment, official bundletool and stable source/artifact hashes. Native acceptance requires exact RC2 receipts in [the execution register](validation/v1.13/README.md). The real RC1 tutorial crash means its successful smoke scopes, binary checks and any incomplete run cannot qualify RC2. The [targeted RC2 fast-resume pilot](validation/v1.13/ui-bugs/ui-sdk-rc2-tutorial-fast/host-report.json) passes 90 native checks with 15 original frames and the prior profile restored. It exercises quick Later→Resume below the old feedback deadline while preserving exact active lesson state. Separate exact RC2 reports pass [55 genuine fresh-Lab checks](validation/v1.13/ui-bugs/ui-sdk-rc2-fresh/host-report.json), [seven production startup checks](validation/v1.13/ui-bugs/ui-sdk-rc2-production-startup/host-report.json) and [35 production house checks](validation/v1.13/ui-bugs/ui-sdk-rc2-production-house/host-report.json). The [complete RC2 retained-Lab route](validation/v1.13/ui-bugs/ui-sdk-rc2-full13/host-report.json) passes **798 native checks** across 13 actually played Journey boards, room purchases/placement, six languages, compact/landscape controls and a distinct cold process, with 83 original full frames plus four labelled crops and original preferences/display restored.

The house and earlier atelier/wardrobe changes are not player research. Human comprehension, emotional attachment, voluntary return, D1/D7 retention, native-speaker copy and physical-phone comfort remain unmeasured or separately unqualified. Puzzle geometry, fixed budgets and progression were not changed: the documented **L22→L23 structural increase (17→85)** and the **Guardian 4→5 cellwise introduction** remain open design work.

Test ads remain forced. Real checkout, premium draws, real Google/two-account/social acceptance and the licensed purchase/pending/refund/recovery lifecycle are separate gates. Google sign-in does not synchronize the home or gameplay. No server feature deployment or Play publication is inferred from this candidate.

## Website

The published FR/EN website leads with the home and keeps the puzzle prominent. It uses versioned native capture paths, exact APK download information, and existing privacy/deletion pages. Public screenshots must remain byte-identical to original final-candidate captures and carry source/report/hash provenance. The English hug capture is explicitly labelled in both website languages; site decoration is distinguished from game imagery.

The [source audit](validation/site-v1.13/release/static-check.json) passes 2,150 assertions and the [local browser review](validation/site-v1.13/browser/report.json) records 14 states and seven original JPEGs. Nine original native images carry exact RC2 source/report/hash provenance. [Firebase Hosting delivery](validation/site-v1.13/deployment.json) is confirmed from source `67774c143a60017049207adcedf2ea63fe048d97`, public tree `98634dede88c30cf677e25ab4c3840079cd6873c`: **108 files /77,610,834 bytes**, version `170cf6d048c1b40f`, finalized **28 October 2026,16:03:50.235 UTC**. [Independent public-origin verification](validation/site-v1.13/public-origin.json) confirms HTTP200 and exact bytes/SHA-256 for all 108 files plus `/`, `/privacy/` and `/delete-account/`. Later documentation-only commits are not relabelled as the deployed source. [Separate evidence scopes](validation/site-v1.13/).

## Preserved releases

[1.12 atelier and tracked power purchases](RELEASE_1_12.md) · [1.11 equipment visibility](RELEASE_1_11.md) · [1.10 companion/objectives/journal](RELEASE_1_10.md). Their original artifacts, failures, native checks and deployment receipts retain their dated scope.

## Historical RC1 candidate — not released as the final delivery

A real crash was reproduced in the retained full13 route: selecting Later in the tutorial and immediately resuming could leave an expired feedback deadline active, leading to an empty lesson-text key and process failure. The root fix clears obsolete feedback and guards terminal lesson lookup. RC1 is excluded from final acceptance; its original APKs, manifests, native frames, failed reports and successful scoped smoke observations remain unchanged.

### Preserved exact RC1 artifacts

| Artifact | Bytes | SHA-256 |
| --- | ---: | --- |
| [Production APK](../releases/luma-drift-1.13.0-rc1.apk) | 7,806,562 | `9ded8f30d51479454f2ccc40ef4f4e5d6444812850a4779572bc69c818484762` |
| [Production AAB](../releases/luma-drift-1.13.0-rc1.aab) | 11,210,041 | `ccc89c4b524bdbcaea9f717c83e062748ae599c5d248e492975874da72ee76e4` |
| [Lab APK](../releases/luma-drift-1.13.0-sandbox-rc1.apk) | 7,789,865 | `3726e9779d7c00d4667412e5020e4d1a9b15f11b49388c957b473954c1e67ebb` |
| [Lab AAB](../releases/luma-drift-1.13.0-sandbox-rc1.aab) | 11,200,257 | `1b2637adae149cc21d7f69ec3547a21a4609897da17e8e4a139b3b445ac87284` |

[Production manifest](../releases/manifest-1.13.0-rc1.json) · [Lab manifest](../releases/manifest-1.13.0-sandbox-rc1.json) · [Checksums](../releases/SHA256SUMS.txt) · [Exact source/artifact freeze](validation/v1.13/source-freeze.json).

Production package `com.androidhit.lumadrift`; Lab uses `com.androidhit.lumadrift.sandbox`. Android 8+ / API 26. These are identified candidate artifacts, not proof of a Play upload or public download. Signature/integrity verification, final native acceptance, GitHub delivery and website deployment have their own evidence in the qualification record.

The preserved [RC1 build](validation/v1.13/build.txt), [source freeze](validation/v1.13/source-freeze.json) and [binary audit](validation/v1.13/binaries/binary-verification.txt) retain their original hashes. RC1 native scopes passed 55 fresh-Lab checks, seven production startup checks and 35 production house checks; those narrower observations did not cover the later reproduced crash and do not qualify RC2.
