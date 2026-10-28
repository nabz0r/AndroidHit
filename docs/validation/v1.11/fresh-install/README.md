# 1.11 RC1 — genuine fresh guest installation

**PASS: 83 native assertions and 15 unique original PNGs**, using `1.11.0-sandbox-rc1` / code 12 on a newly created Android 36 arm64 QA device with 16 KiB pages. This qualifies this bounded first-install/tutorial run; it is not a production-device, account, advertising, purchase or whole-release acceptance.

The game APK is `releases/luma-drift-1.11.0-sandbox-rc1.apk`, SHA-256 `6f9edf10cddc283a6ae6d0ae876f3196b52660ddc6ed992aeea18bcdb698db78`. The separately compiled native test APK is SHA-256 `7a0e2b6a48a19eaf6ea80a1338f420aea7253249c32172e0716203f47c16c61f`. The copied runner source is SHA-256 `5e29a15ec184114010a70255c0c29d7ae68a99baa691dc0265712860fee589db`. [Runner provenance](runner-provenance.json) retains the exact arguments and source snapshot.

## What was exercised

- The actual first installation automatically offers optional practice on a fresh guest profile.
- All seven lessons advance only through the required actions; practice consumes no earned coins or real power charges.
- Skipping and reopening preserve the exact lesson, phase and typed action history.
- Real SDK rotation recreates the activity in landscape and back while preserving practice state and a gate preview.
- The gate coach is exercised with a native touchscreen drag from observed accessibility bounds. Double confirmation records one opening.
- First completion grants exactly **100 LumaCoins and 2 Sparks**. The durable receipt prevents a second claim; replay remains free.

The requested case is **`tutorial` only**. Several piece steps use SDK accessibility clicks; the gate uses actual native touchscreen injection. This is not a claim that every step was executed as a finger drag. No shell input commands, profile seeding, account, ad or payment action were used.

See the unchanged native [host report](host-report.json), [assertions](report.json), [action trace](actions.json), and [instrumentation output](instrumentation.txt). The native capture list contains 16 capture operations: a repeated landscape filename is retained as its final original frame, giving **15 unique PNGs**. The [screenshot manifest](screenshot-manifest.json) records their original hashes and dimensions: portrait `1080×2340`, landscape `2340×1080`.

The [fresh Home frame](../../../screenshots/v1.11/fresh-install/home-en.png) visibly shows 180 LumaCoins and a guest profile. The [gift frame](../../../screenshots/v1.11/fresh-install/tutorial-reward-en.png) shows the collected state. These two frames were also visually inspected without editing them.

## New device and preservation

`Luma-Release111-J`, emulator port **5584**, was created from the already installed `system-images;android-36;google_apis_ps16k;arm64-v8a` image. No userdata was copied. It has 2 GiB RAM, 4 CPU cores, a `1080×2340` display and density 440. [Preparation](avd-preparation.json), original/new configuration files and [before-install observations](absence-before-any-install.json) are retained.

Lab, production and the Lab test package were all **absent before any installation**. The helper independently rechecked Lab absence, installed the exact APK, verified the installed bytes, and ran the supplied instrumentation with `expectFreshTutorial=true`, the exact version and code. Production remained absent. Rotation settings were restored and the newly created guest is retained; this is not a claim that a pre-existing profile was restored. Neither port 5580 nor port 5582 was mutated.

The helper initially produced evidence in `ui-bugs/ui-sdk-fresh-j`. The 37 native output files were moved byte-exactly into the requested `fresh-install` directories, without PNG edits or JSON rewrites. [Relocation provenance](native-evidence-relocation.json) records both paths and checksums. Original helper outputs are not duplicated elsewhere.

## Persisted-state observation limit

The background observer attempted read-only `run-as` access to the persisted XML. That method is unavailable for this non-debuggable release APK; it collected **zero samples**. The observer was stopped and its [zero-sample report](persisted-observer-report.json) is retained. It did not write device state.

The first successful read, using read-only `su 0 cat`, occurred **after** the tutorial gift. [The observation](post-tutorial-prefs-observation.json) and [exact persisted XML](post-tutorial-prefs.xml) show initialized state, Nova/Lumina ownership, **280 coins / 5 Hint / 3 Vision / 2 Flow** and the welcome receipt. The runner then leaves a replay paused at `T1|0|0|SKIPPED|`; the complete state was asserted earlier and is captured in the native report.

Therefore **180 coins / 3 Hint / 3 Vision / 2 Flow are not claimed as directly observed on disk before the gift in this run**. The Home frame shows the initial coins, and native assertions establish a single +100/+2 gift with no practice spending. The separate [41-check initialization host probe](../store-bugs/initialization-fixed.json) covers initial persistence and refusal semantics; it is separate evidence, not a replacement native observation. No second device, reset or extra test was performed to conceal this measurement gap. This run also does not establish a fresh process relaunch of the starter state.

## Stable evidence

The [before](crash-buffer-before.txt) and [after](crash-buffer-after.txt) crash buffers are byte-identical; [comparison](crash-buffer-comparison.json) records no new entries. The native helper independently checked its own crash delta.

The AVD remains running for review with the new guest preserved. Its live stdout file was moved outside the repository to `/tmp/luma-v111-fresh-j-emulator.log`; only a frozen final-120-line startup excerpt and [provenance](emulator-log-provenance.json) remain here. The observer is stopped. No evidence file in this directory continues to receive background writes. [Evidence manifest](evidence-manifest.json) hashes the retained files and original native frames.

## Separate menu and avatar follow-ups on the same retained guest

These existing SDK cases were subsequently authorized, without a new build, fresh helper, reset or restored profile. They use the same already installed game and test bytes. The original 83-check report and 15 images above are unchanged.

| Case | Native assertions | Original PNGs | Actual persisted change |
| --- | ---: | ---: | --- |
| [Menus](menu-followup/host-report.json) | 72 PASS | 8 | None in game/meta preferences |
| [Avatar](avatar-followup/host-report.json) | 45 PASS | 7 | `collection_goal_v110` set to `cosmetic:hat_cupid` |

Menus visits objective tabs and shop categories without an automatic claim or purchase. Avatar previews a locked Cupid hat, closes the preview, explicitly pins it as a reversible goal, checks the appearance/equipment tabs, rotates the wardrobe, and checks collection/wardrobe footer targets. The pin grants no item. Both follow-ups retain the guest, preserve rotation, and have identical before/after crash buffers. Their raw XML and per-field comparisons distinguish the one authorized goal change from economic or progression state. The helper [source](run-followup.py) only invokes those existing SDK cases and reads evidence.

The native [wardrobe frame](../../../screenshots/v1.11/fresh-install/avatar-followup/avatar-slots-en.png) was visually inspected. The new names appear under all four clothing slots and three equipment slots, and the portrait is visible between them. **A readability limitation is reproduced:** even the starter English `Gloves · Dew` is ellipsized to `Gloves · D…` in its narrow card. Hat/Outfit/Shoes starter names and Moonbud/Firefly Ring/Pebble Heart are fully visible in this frame. The full glove name remains in the actual [accessible snapshot](avatar-followup/avatar-slots-en.json), portrait description and the list/dialogue. This observation was reported to the parent before release; the 45 passing assertions do not establish that every name fits visually. No product code was changed by this reviewer.
