# Native companion-team qualification — 1.19

**Candidate C passes the native scopes below:** `1.19.0-prototype2`, version code `27`, production package `com.androidhit.lumadrift`. The APK installed and pulled back from the device in every C phase has SHA-256 `86e310ea661af5644b47a96b22df85f148f6a9508ee87234339be7b093e8a526`. Candidate A is historical progression evidence. Candidate B is superseded and contains a preserved failed navigation test; neither is the final acceptance build.

[The final index](candidate-c-final-index.json) records every report hash, scope, count, six website originals, video and restoration receipts. These are **executed assertions and gestures**, including repeated preflight checks, rather than distinct features or human playtesters.

| Candidate | Passing reports | Assertions | Real gestures | Original PNGs | Status |
| --- | ---: | ---: | ---: | ---: | --- |
| A | 8 | 726 | 428 | 53 | Intermediate; fresh progression earned here |
| B | 11 | 372 | 178 | 45 | Superseded; one additional failed compact report excluded from these passing totals |
| **C** | **10** | **419** | **209** | **58** | **Final native scopes pass** |

## Device, inputs and earned progression

The separately created empty `Luma119-Battle-QA` AVD is `emulator-5602`, Android 36 ARM64 with 16 KiB pages. [Creation](device-creation/creation.json) and [empty boot](device-creation/empty-boot.json) record its provenance before the first authorized game install. No existing player profile or userdata was copied, opened, replaced or cleared. The [new SDK-only observer](../../../../scripts/v119-ui-test/README.md) is separate from the frozen 1.18 harness and interacts with actual native controls at observed accessibility bounds. Public U1–U30 certificates supply real piece gestures; the observer does not import a target solver or edit game state.

On A, the guest genuinely completed the three introductory exits and first victories of U1–U30. The observed account received 55 LumaCoins for each first campaign victory. Five real avatar draws cost 180 earned coins each; one duplicate returned 45 coins. Outcomes were recorded as committed receipts and were not forced. Earned-coin purchases acquired Miko, Orbit, Lune and Hoot, followed by real ready daily/weekly rewards and the 360-coin Tula purchase. The resulting ten owned companions are `buzz, hoot, lune, miko, nova, orbit, pip, quill, tula, zig`, with 220 coins. Team changes throughout qualification used only those actually owned companions.

[A→B](candidate-b-upgrade-preservation/preference-equality.json) and [B→C](candidate-c-upgrade-preservation/preference-equality.json) were explicitly authorized updates on this task AVD. Both prove exact equality of the four observed preference maps before the new candidate's first Activity launch. Seed keys are consistently excluded from read-only snapshots. **The 30 first clears were earned on A and retained on C; they are not 30 fresh C clears.** No ownership, currency, progress, clock or seed was injected.

## Final C execution

| Report | Assertions / gestures / PNGs | Actual scope |
| --- | --- | --- |
| [Upgrade preservation](candidate-c-upgrade-preservation/report.json) | 6 / 0 / 0 | Exact installed C bytes and preserved observed profile before Activity launch |
| [Compact, reduced motion](candidate-c-compact-reduced-motion/report.json) | 39 / 23 / 8 | Real Settings toggle on/off; actual All levels and Rewards centres; skill-page scrolling, Guide, Pause/Resume and unchanged battle |
| [Compact FR/EN](candidate-c-compact-fr-en/report.json) | 33 / 21 / 8 | Real language changes, team slots, French actual-centre map/rewards navigation and U25 finish |
| [Core team and combo](candidate-c-core-team-combo/report.json) | 83 / 31 / 15 | Nova GUIDE, Miko TRAIL, Orbit FLOW; three visible actors; once-used refusal; explicit combination and genuine U25 finish |
| [Pulse, rewind and credit](candidate-c-pulse-rewind/report.json) | 66 / 34 / 7 | Lune PULSE, Hoot REWIND, empty-history refusal, undo/re-exit without energy farming, explicit combo and genuine U25 finish |
| [Vision](candidate-c-vision/report.json) | 43 / 27 / 3 | Owned Tula VISION, actual legal-piece highlights, correct debit/usage and genuine U25 finish |
| [Six languages](candidate-c-six-languages/report.json) | 39 / 20 / 7 | Actual EN/FR/DE/ES/PT/IT settings and squad views, owned team checked in each language |
| [Expedition transient views](candidate-c-expedition-transient/report.json) | 47 / 24 / 6 | Solo Nova; real expedition stage 2 after stage 1 on B; mode 5 and exact battle preserved through squad/skill pages; actual stage win |
| [Cold preparation](candidate-c-cold-prepare/report.json) | 24 / 19 / 1 | Owned three-member team, U25, Guide/Trail and a saved ready combo |
| [Cold resume](candidate-c-cold-resume/report.json) | 39 / 10 / 3 | Distinct process, exact observed state restored, explicit saved combo then real U25 finish |

All six families were executed on C through real controls. This does not claim 37 independent engines: catalogue identities use six shared families with their declared parameters. Once-only talent use, energy credit and combination state are recorded in the actual `battle_v119` session value.

[Cold preservation](candidate-c-cold-preservation.json) compares process IDs `18594` and `18793`: all four observed maps are exactly equal, including `nova,miko,orbit`, energy `0`, usage mask `3`, credited piece mask `1`, ready/unspent combo, history `0`, action budget and the same run ID. The saved combination is subsequently activated through the UI. This is process-death persistence on the qualified emulator, not a cloud/Google account synchronization test.

## Preserved B failure and C regression

[B's compact reduced-motion report](candidate-b-reduced-motion/report.json) failed after 12 assertions and 5 gestures, with 2 original captures. At 720×1280/density 320/font scale 1.3, the All levels target `[33,928,236,1070]` overlapped Rewards `[92,992,627,1076]`. Touching the real All levels centre opened Rewards. The failed report and inputs are retained unchanged. No alternate corner tap or accessibility `ACTION_CLICK` was used to bypass the error.

C's two compact reports repeat actual centre touches successfully, including real map selection/board play and Rewards navigation. Compact content uses normal vertical scrolling and some labels have controlled ellipses at the larger font size; full semantic labels remain available. This is a functional compact pass, not a claim that no visible text is abbreviated. The original normal format is 1080×2400/density 420/font scale 1.0.

## Original C media

These six current C images were supplied to the website agent only after their exact-installed-hash reports passed. They show an earned guest profile with 30 retained clears and 220 coins; unavailable server Gold is displayed as a dash. French uses Nova/Miko/Orbit; English uses Nova/Tula/Orbit. They are native original pixels, not the earlier browser proposal.

| View | French | English |
| --- | --- | --- |
| Adventure | [FR original](candidate-c-core-team-combo/native/fr-adventure-final.png) | [EN original](candidate-c-six-languages/native/en-adventure-final.png) |
| Owned team | [FR original](candidate-c-core-team-combo/native/fr-three-member-team-final.png) | [EN original](candidate-c-six-languages/native/en-actual-team.png) |
| Puzzle | [FR original](candidate-c-core-team-combo/native/three-real-actors-before-skills.png) | [EN original](candidate-c-vision/native/tula-real-legal-highlights.png) |

The [unedited C recording](candidate-c-core-team-combo/native-ui-recording.mp4) lasts 45.000889 seconds, H.264 720×1600, SHA-256 `1735f91b34187466f1544cd9466dbc6129a50cc3885095ed32db591c98f884df`. Actual Miko detail, explicit combo result and Orbit's board effect are visible at reviewed moments. Original fast captures also show active poses, talent rings, Guide and Trail targets. Derived video-review frames outside the repository are not counted among the 58 original C PNGs. B's older recording remains intermediate evidence only.

## Restoration and limits

[Display restoration](display-candidate-c-restore.json) and [device finish](device-finish.json) confirm the original display/font settings, reduced motion restored off through the real UI, observer absent, game force-stopped and only this guarded task AVD shut down. Its legitimate profile and installed C remain available; no game uninstall or data clear occurred.

This is one isolated emulator profile. Audio output was disabled. No physical-phone comfort, sound quality, frame-time benchmark, human comprehension, retention or enjoyment is measured. Six-language coverage is the squad/settings path, not every app screen. These runs do not qualify external Google login, ad interaction, a real-money/server transaction or Play publication. No public website deployment is claimed by native evidence alone.
