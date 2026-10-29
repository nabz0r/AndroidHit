# Luma Drift1.16 qualification register

Final candidate B is accepted and delivered locally against126 frozen app/build inputs. Final native normal/compact/cold acceptance and canonical packaging pass. Website publication has its own subsequent receipt. Source implementation is `0ebc57e4`; its visual spacing correction is `4021b872`.

## Host and binary evidence

| Scope | Result | Evidence |
| --- | --- | --- |
| Local nickname, flags, earned badges, both versioned draw pools, all outcomes/equips, refused writes, retry, replay, cold reconstruction and concurrent handles |60,712 checks PASS | [Model](model/profile-summon.json) |
| Adaptive camera,100 cells,42 objects/two orientations, inverse/picking/pan/clipped render across six viewport sizes |149,208 checks PASS | [Geometry](model/home-geometry.txt) |
| Prior camera geometry |6,494 checks PASS | [Prior geometry contract](model/legacy-geometry.txt) |
| Prior appearance/home/external-ownership models |683 /2,074 /17 checks PASS | [Appearance](model/legacy-appearance.json), [home](model/legacy-home.json), [external](model/legacy-externals.json) |
| Legacy rewards/wardrobe/capsules/quests/gift persistence |100,953 checks PASS | [Additional regressions](model/additional-regressions.json) |
| Explicit local-save commit/read refusal and retry |229 assertions/13 scenarios PASS | [Save regression](model/local-save-regression/summary.json) |
| New text in six languages |366 format/column checks PASS | [Copy](model/copy-six-languages.json) |
| Complete puzzle-engine source preservation |28 files identical to1.15E | [Hashes](model/engine-preservation.json) |
| Production/Lab APK+AAB signatures, IDs, code18, min26/target36, bundles and static alignment |PASS | [Binary audit](candidate-b/binaries/binary-final-candidate.json) |
| Release lint |0 errors /13 warnings each flavor | [Lint](candidate-b/lint-summary.json) |

The [official reference audit](../../research/v1.16/reference-audit.md) records design observations without reusing brand artwork. [Source review](independent-source-review.md) found and fixed six routing, clipping, input and save-retry issues; [visual review](visual-review.md) separately distinguishes A from B.

## Final B native acceptance

The [independent aggregation](native-final-summary.json) verifies every host report, exact final APK hash, cleanup result and all263 full PNG hashes/dimensions. There are997 qualification assertions:993 execute on B and four read-only baseline assertions observe installed A before the exact B update. Historical A and1.15 screenshots are excluded.

| Scope | Checks / original PNG | Evidence |
| --- | --- | --- |
| Baseline, exact in-place A→B update, home/pan/picking, pseudo/flag/badges, two real draws/equip/replay and distinct cold process |163 /29 | [Host report](native-interface/candidate-b-upgrade-a/host-report.json) |
| Six-language normal browse |272 /81 | [Host report](native-interface/candidate-b-normal-a/host-report.json) |
| Six-language actual320dp compact browse |272 /81 | [Host report](native-interface/candidate-b-compact-a/host-report.json) |
| Both explicit banners, all23 avatar names and stable Inky footer, allfive outfits and Aurora footer, six languages |145 /36 | [Normal supplement](native-interface/candidate-b-banners-normal-a/host-report.json) |
| Same complete catalogue supplement on320dp compact |145 /36 | [Compact supplement](native-interface/candidate-b-banners-compact-a/host-report.json) |

Final B really draws Halo for180coins and Star parade for260; the owned outfit returns the announced100coins, so51,850 becomes51,510. Equip/replay/cold checks preserve all progression, room layouts, powers and puzzle-session values. The earlier actual1.15→A migration and legacy preservation remain separately recorded below; B changes only collection artwork/caption spacing relative to A.

The original browse's `summon-companions-*`/`avatar-pool-bottom-*` filenames after English are historical naming errors: the chosen outfit tab survived between languages, and the one-scroll avatar frame was not the end of23. They certify the visible screen only. The explicit normal/compact supplements close that coverage gap and prove complete catalogue reachability without new draws. A stale older failure.txt copied by earlier wrappers is also annotated; native passed reports and fresh indexed images remain authoritative.

## Preserved candidate A evidence

A production APK `f2bea6df427c214d141b24a64945508e2b9b0947118aafc0a293ced64658400a` is not the delivered B. In `candidate-a-upgrade-a`, the actual retained1.15 APK is observed read-only before install-r; exact legacy preferences and the puzzle session survive the real17→18 update. The observer then misses Android's uppercase SAVE label before any draw; this failed attempt remains failed.

The corrected observer's `candidate-a-upgrade-b` continues on that same18 profile:129 prepare checks and30 cold checks pass (163 including baseline), with exactly180+260 earned coins spent, Miko and Star parade obtained/equipped and every room layout, progression, power ledger and puzzle session preserved. A separate six-language normal browse passes272 checks/81 original frames. A is superseded after the outfit feet/title spacing finding; these scopes do not certify B's exact bytes. [A summary](candidate-a/native-results.json).

## Native method and limits

Only the retained task-owned `Luma-Refonte115-QA` / `emulator-5586` is used. Wrappers guard its exact AVD name, package/version and installed APK hash. The separate signed observer sends actual touchscreen events to observed accessibility bounds and uses ACTION_SET_TEXT on the visible nickname field; preferences are read only for verification. No progress, coins, house coordinates or receipt outcomes are injected. The task-owned showcase AVD and prior user devices are untouched.

Normal QA is1080×2340 at440dpi. Compact QA is880×1440 at440dpi (320dp wide), restored afterward. Original screenshots include the full native frame and are never edited. Summon seeds and credentials are excluded from exported observations. Cold checks force-stop the target and require a distinct process ID. Host failure doubles do not establish physical flash-failure behavior, and emulator rendering does not measure phone performance or human enjoyment. Test ads and paid-provider gates remain unchanged.

## Final delivery

The [canonical package comparison](delivery-integrity.json) passes for all four artifacts and126frozen inputs. Only the task-owned AVD was used and it is now [stopped normally](device/shutdown.json), with the profile retained, display restored and observer removed. The existing [website and exact downloads](https://luma-drift-production.web.app/#screens) are published at 2026-10-29T00:01:49.165Z from `ea1980f7dbc3bfe65233065d4d902beba083301f`; all151public payloads plus three aliases match. [Hosting and browser evidence](../site-v1.16/README.md).
