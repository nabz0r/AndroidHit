# 1.13 qualification — home around the main puzzle

28 October 2026. This record separates implementation, source tests, exact frozen artifacts, installed Android observation and publication. A prepared asset, a successful compile or an intermediate native run is not a completed delivery claim.

## Current RC2 replacement

Production **1.13.0-rc2**, Lab **1.13.0-sandbox-rc2**, code **14**. [Release and candidate state](../../RELEASE_1_13.md) · [Home contract](../../HOME_1_13.md).

The retained full13 route found a real tutorial crash in RC1: Later followed by quick Resume left obsolete feedback timing active and led to empty lesson-text lookup. RC2 resets that feedback and guards terminal lesson text. The new build, signed hashes and installed tests must identify RC2. No RC1 pass total or screenshot source is relabelled as current replacement acceptance.

The [RC2 source/artifact freeze](source-freeze-rc2.json) identifies production APK `c68be62fd940e753fc06c4b5677a48cafaf077a4710755b3bdf96467d571e31b` (**7,806,558 bytes**) and Lab APK `85cd018d5d7ee17f7343921d5367859e261acb7bdc1443bcd02d6bd7618953fa` (**7,789,865 bytes**). [All four exact artifacts](../../RELEASE_1_13.md#current-rc2-candidate). Production remains `com.androidhit.lumadrift`; Lab remains `com.androidhit.lumadrift.sandbox`. Source tests, signed-package checks, native execution, public-download availability and Play publication remain separate layers.

## Completed source checks

| Scope | Executed result | Evidence / runner |
| --- | ---: | --- |
| Home catalogue, six-language strings, 6×6 immutable layout/BFS, ownership versus placement, wishes, old saves and local-save failures | 891 assertions pass | [Machine report](home-model.json) · [execution](home-model.txt); `scripts/test-home-v113.sh` |
| Actual three-piece opening, real switch/gate behaviour and history recovery | 18 checks pass | [Execution](first-light.txt); `scripts/test-first-light-v113.sh` / `FirstLightV113Test.java` |
| First-profile initialization regression | 41 checks pass | [Execution](preserved-initialization.txt); `scripts/test-store-init-v111.sh` |
| Store/appearance regression | 683 checks pass | [Execution](preserved-appearance.txt); `scripts/test-store-appearance-v111.sh` |
| Earned power purchase/retry regression | 1,670 checks pass | [Execution](preserved-powers.txt); `scripts/test-store-booster-v112.sh` |
| Existing seven-lesson tutorial/store regression | 1,454 checks pass | [Execution](preserved-tutorial.txt); `scripts/test-tutorial-v18.sh` |
| Current FR/EN website controller | 8 tests pass | `public/tests/site-language-v113.test.mjs` |

The 891-check report supersedes the earlier 873-check source tally after adding the final home action labels in all six languages and correcting the opening-history fixture to a real legal order. The earlier tally is not added to the current total.

These are host/JVM or local JavaScript results. Home durability tests explicitly cover refusal before memory update and Android-style memory-before-disk update, exact retries, separate store handles and cold preference recreation. They do not themselves reproduce a physical storage fault or certify native touch/rendering.

## Current RC2 acceptance register

| Layer | Current recorded state |
| --- | --- |
| Four app artifacts | [RC2 binary audit](binaries-rc2/binary-final-candidate.json) and [verification](binaries-rc2/binary-verification.txt) pass: APK/AAB signatures, 16 KiB ZIP/native alignment, official bundletool and stable source/artifact hashes |
| Full Gradle/lint verification | [RC2 build](build-rc2.txt) succeeds in 1m10s; warning output retained; prior RC1 build remains historical |
| Targeted RC2 tutorial regression | [Fast-resume pilot](ui-bugs/ui-sdk-rc2-tutorial-fast/host-report.json): 90 native checks pass, 15 original frames, exact RC2 Lab hash, prior profile restored; Later→Resume at lesson2 and gate-preview occur below the former 2400ms deadline without a crash |
| Native final Lab room/Journey/tutorial run | [RC2 full13](ui-bugs/ui-sdk-rc2-full13/host-report.json): 798 checks pass (792 prepare +6 cold), 13 Journey boards actually played, 83 original full frames plus four labelled crops; house purchases/placement, six languages, 320dp portrait/landscape and distinct cold process; original profile/display/rotation restored |
| Genuine fresh opening and cold process | [RC2 fresh](ui-bugs/ui-sdk-rc2-fresh/host-report.json): 55 native checks pass, 17 original full frames plus four labelled crops; Lab genuinely absent before install on emulator-5590, saved switch history crosses process5161→5361, new guest retained |
| Production startup | [RC2 startup](ui-bugs/ui-sdk-rc2-production-startup/host-report.json): seven native checks pass, one original full frame, exact production APK, original profile/rotation restored |
| Production house | [RC2 house](ui-bugs/ui-sdk-rc2-production-house/host-report.json): 35 native checks pass, ten original full frames plus four labelled crops, exact production APK, original profile restored |
| Website source | [Final source audit](../site-v1.13/release/static-check.json): 2,150 assertions pass; 108 public payloads, exact RC2 metadata and nine byte-identical original native captures; RC1 provenance rejected |
| Website browser rendering | [Final local browser report](../site-v1.13/browser/report.json): 14 recorded states and seven original JPEGs; FR/EN desktop/mobile, compact width, lower sections, decoded images, controls and legal/download routes reviewed |
| GitHub publication | [Source `67774c14`](https://github.com/nabz0r/AndroidHit/commit/67774c143a60017049207adcedf2ea63fe048d97) published on `main`; [four immutable public downloads](publication.json) return HTTP200 and exact frozen bytes/SHA-256, 38,006,707 bytes total |
| Firebase Hosting | [Finalized delivery](../site-v1.13/deployment.json) from `67774c14`, 28 October 2026,16:03:50.235 UTC, version `170cf6d048c1b40f`; [public-origin verification](../site-v1.13/public-origin.json) matches all 108 payloads /77,610,834 bytes and three aliases, HTTP200 and exact SHA-256 |
| Google Play | No 1.13 upload, review or publication claimed |

The targeted RC2 pilot preserves the exact active tutorial states `T1|1|1|ACTIVE|` and `T1|3|1|ACTIVE|`. Recorded quick Resume timings are 766ms and 1145ms, both below the former 2400ms feedback window. This is a focused regression result, not completion of the full room/Journey, fresh-install or production acceptance routes.

The [outer profile restoration receipt](initial-profile-restoration.json) verifies the original six preference XML files by SHA-256 and absence of the test package on the three owned test emulators. [Owned-emulator shutdown](owned-emulator-shutdown.json) records their graceful data-saved stop; the unrelated remaining emulator was not modified. These cleanup receipts are separate from candidate behavior.

## Native protocol and retained intermediate work

The [1.13 runner](../../../scripts/v113-ui-test/README.md) has a separate SDK test package and preserves the historical 1.12 runner/fixtures. The retained-profile route backs up/restores all original private preference files and display/rotation settings. Purchases use LumaCoins earned by real Journey clears; buying, previewing, placing, rotating, storing and a distinct cold-process restoration are separate checks. The fresh route refuses an existing Lab profile and proves an action history survives a true process boundary before finishing the opening.

The final-byte [Lab20 run](ui-bugs/ui-sdk-final1-lab20/host-report.json) failed its overall prepare phase; its cold phase passed, and its original profile was restored. It remains a failed whole run, not a qualified screenshot source. The subsequent RC1 full13 route then reproduced a real Later→Resume tutorial crash. Its original failure is retained, the candidate is replaced with RC2, and new native qualification has its own source/hash identity.

The initial [intermediate house-smoke source](ui-bugs/ui-sdk-intermediate-a-house-smoke/README.md) is retained under its original label. Its observation is not evidence for the final frozen APKs above. All failures, runner changes and subsequent source freezes retain their own labels and hashes.

Original final screenshots belong under `docs/screenshots/v1.13/…`. Website copies require byte identity and explicit public/source/report/hash/locale/dimension provenance. Analysis crops or renderer examples must not be presented as original installed full-frame captures.

## Preserved rules and unresolved work

The candidate leaves the 1,000 Journey +240 Remix layouts, action budgets, challenge identities, starter 180-LumaCoin balance, progression and existing equipment unchanged. The documented L22→L23 structural jump and Guardian 4→5 mechanic transition remain. No human study of comprehension, emotional attachment, enjoyment, D1/D7 retention or international appeal has been performed by these tests.

Physical Android comfort, larger fonts, TalkBack and native-speaker copy review remain distinct. Real Google/two-account social/gifts, consent/decline, live advertising eligibility and the licensed Play purchase/pending/refund/recovery lifecycle are independently qualified. Test ads remain forced; checkout and premium draws remain closed. House/gameplay state stays on this device; Google sign-in provides no gameplay cloud save.

## Preserved RC1 artifact and scoped acceptance record

This section records the superseded RC1 only. The reproduced full13 process crash prevents it from being accepted as the final release. Its narrower passes below do not establish RC2 behavior.

### Exact historical RC1 candidate identity

Production **1.13.0-rc1**, Lab **1.13.0-sandbox-rc1**, code **14**. [Release and four artifact hashes](../../RELEASE_1_13.md) · [Home contract](../../HOME_1_13.md) · [Exact source freeze](source-freeze.json).

| Frozen APK | Bytes | SHA-256 |
| --- | ---: | --- |
| Production | 7,806,562 | `9ded8f30d51479454f2ccc40ef4f4e5d6444812850a4779572bc69c818484762` |
| Lab | 7,789,865 | `3726e9779d7c00d4667412e5020e4d1a9b15f11b49388c957b473954c1e67ebb` |

Packages are `com.androidhit.lumadrift` and `com.androidhit.lumadrift.sandbox`. The [source freeze](source-freeze.json) binds the final game/build inputs to these four signed hashes. Artifact identity alone does not establish installed acceptance, GitHub availability or a Play upload; signatures and native observations have their own executed records below.

### Historical RC1 scoped register

| Layer | Current recorded state |
| --- | --- |
| Four app artifacts | [Binary audit](binaries/binary-final-candidate.json) and [verification summary](binaries/binary-verification.txt) pass: pinned APK/AAB signatures, 16 KiB ZIP alignment, eight 64-bit native payloads /20 LOAD segments, official bundletool validation; source and artifact hashes stayed stable |
| Full Gradle/lint verification | [Final3 build log](build.txt): production/Lab APK+AAB and both lint tasks finish successfully in 1m33s, 119 actionable tasks; experimental lint/SDK/deprecation warnings are retained |
| Native final Lab room/Journey run | RC1 full13 reproduced the Later→Resume tutorial crash; no overall release acceptance; superseded by RC2 |
| Genuine fresh opening and cold process | [Final1 fresh N](ui-bugs/ui-sdk-final1-fresh-n/README.md): 55 native checks pass on the exact Lab APK, 17 original full frames plus four labelled analysis crops; Lab absent before installation, saved switch history crosses a distinct cold process, new guest retained |
| Production startup | [Final1 production startup](ui-bugs/ui-sdk-final1-production-startup/README.md): seven native checks pass, one original full frame, exact production APK; prior profile/rotation restored |
| Production house | [Final1 production house](ui-bugs/ui-sdk-final1-production-house/README.md): 35 native checks pass, ten original full frames plus four labelled crops; visible cuddle/rest/lamp, movement and wardrobe, prior profile restored |
| Website source | RC1 source preparation superseded by RC2 before final public delivery |
| Website browser rendering | Early structure checks only; never RC1 final populated acceptance |
| GitHub publication | RC1 did not become the final 1.13 delivery |
| Firebase Hosting | No RC1 website deployment; replacement RC2 has separate delivery evidence |
| Google Play | No 1.13 upload, review or publication claimed |

This register is intentionally updated from actual final reports. Do not substitute earlier 1.12 test totals or an intermediate 1.13 run for current frozen-byte acceptance.
