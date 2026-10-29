# Luma Drift 1.20 — native qualification

**Final production candidate F passes the scoped acceptance below.** [Machine-readable final index](candidate-f-final-index.json) binds every accepted report, screenshot and derived comparison to the installed APK. Historical candidates and failed observer attempts remain separate.

- Production: `1.20.0-rc1`, code28, SHA256 `e4fa1a70dbb2e842c9a16a9f22d0f2a49a4316ccbc6013fe0acfa233093f5d26`.
- One task-owned emulator: `Luma119-Battle-QA` / `emulator-5602`, API36 ARM64 /16KiB pages. Original earned guest profile retained throughout the authorized1.19→1.20 updates; no profile clear, cloned userdata, grants or clock changes.
- Accepted F: **11 reports / 457 native assertions / 180 touchscreen actions / 76 original PNGs / 1 unedited native video**. 38 separate read-only derived assertions are counted separately. Back and accessibility scrolling are not included in the touchscreen-action count. These are guarded test checks, not player samples.

| Accepted exact F report | Assertions | Touchscreen actions | Original PNGs |
| --- | ---: | ---: | ---: |
| [upgrade-preservation](candidate-f-upgrade-preservation/report.json) | 6 | 0 | 0 |
| [tiny-wallet-swipes](candidate-f-tiny-wallet-swipes/report.json) | 119 | 24 | 2 |
| [preflight-fr](candidate-f-preflight-fr/report.json) | 44 | 16 | 8 |
| [cold-victory-back](candidate-f-cold-victory-back/report.json) | 15 | 0 | 2 |
| [two-star-english-gift](candidate-f-two-star-english-gift/report.json) | 37 | 17 | 7 |
| [expedition-prepare-cold](candidate-f-expedition-prepare-cold/report.json) | 29 | 17 | 3 |
| [expedition-cold](candidate-f-expedition-cold/report.json) | 27 | 11 | 2 |
| [u41-zero-before-cold-retry](candidate-f-u41-zero-before-cold-retry/report.json) | 19 | 7 | 1 |
| [u41-cold-zero-grade-one](candidate-f-u41-cold-zero-grade-one/report.json) | 51 | 18 | 4 |
| [compact-fr-en-reduced](candidate-f-compact-fr-en-reduced/report.json) | 47 | 30 | 22 |
| [six-menu-languages](candidate-f-six-menu-languages/report.json) | 63 | 40 | 25 |

Actual first clears prove all three grades: U39=3★ with zero corrections (+55Coins/+35XP), U40=2★ with one correction (+45/+30), U41=1★ with two corrections (+35/+25). The displayed stopwatch excludes a real paused three-second wait; elapsed time does not set the grade. Invalid/replay/time policy and all1,000 authored initial solutions are covered by separate host tests.

The result survives process recreation until an explicit action. Waiting and closing do not grant twice; hardware Back durably dismisses the receipt. A real journey gift grants exactly one Flow item and one claim receipt, remains visible after five seconds, and closes only on input. [Gift comparison](candidate-f-two-star-english-gift/real-gift-snapshot-comparison.json) proves no second mutation on closing.

Session energy is separate from skill Charge and currencies. New eligible U39 consumes one confirmed energy. A real restart consumes one and creates a new run identity. U41 with two corrections cold-restores at0energy; an attempted paid restart is blocked without changing that run, and resuming it is free. Completing it at0 gives the actual1★ reward. U1 remains free at0. Real ten-minute refills later occur normally; the device clock was never changed. [Cold zero-energy comparison](candidate-f-u41-cold-zero-grade-one/actual-zero-cold-board-comparison.json) pins the durable board/budget/team and energy record. A genuine expedition stage also preserves mode5/team/Guide/budget across cold resume without a second debit, then advances to stage3.

At720×1280/d320/font1.3, FR/EN home, map, gifts, shop, team and collection targets are used at their actual centres. The wallet stays fixed during play scrolling; moves/time and complete result art remain readable. Reduced motion is enabled and disabled through the real settings page. Small content scrolls; this is not a claim of zero ellipsis or every purchase CTA above the fold. At640×960/d320/font1.3, a real piece intersects the fixed HUD. **16 real swipes covering four directions**, including that intersection, begin after restoring the scroll maximum each time and change no run/history/budget/Undo/team/currency state. [HUD overlap receipt](candidate-f-tiny-wallet-swipes/native/wallet-swipe-overlap.json). Display/font and animation choice are restored afterward.

Six-language checks cover home, shop, owned team and rewards in FR/EN/DE/ES/PT/IT. Claimed legacy daily quests are observed as text with no clickable control. The same profile has no claimed event bonus, so that state is not fabricated for native testing.

[Six exact F media entries](../../site-v1.20/native-capture-map.json) supply home/shop/victory ×FR/EN at original1080×2400 for the site. [Unedited F recording](candidate-f-preflight-fr/native-ui-recording.mp4) shows actual exits, streak reactions and the saved result; it is not an FPS/audio/phone benchmark.

Exact E companion traces remain historical: [Guide/Trail/Flow/combo](candidate-e-core-three-families/report.json), [Pulse/Rewind/Vision and no Undo farming](candidate-e-pulse-rewind-vision/report.json),129 assertions/74 touchscreen actions. They are **not relabelled F**. Frozen source manifests show only `LumaView.java` changed from E to F for the HUD guard; all six core model hashes are retained in the final index.

Excluded attempts are preserved: [C guest startup crash](candidate-c-preflight-fr/report.json); [D compact home-recognizer failure](candidate-d-compact-fr-en-reduced/report.json); [F restart icon matcher failure](candidate-f-u41-zero-before-cold/report.json). The F retry resumes the same legitimately started U41 rather than starting another attempt. The earlier [E HUD diagnostic](candidate-e-tiny-wallet-swipes/report.json) had a scroll-position limitation and is not used as final HUD proof.

Native checks do not establish physical-device performance, sound quality, enjoyment, retention, live Google sign-in, ads, real billing or provider/store acceptance. Native award cold checks cover READY and active runs; PENDING interruption stages are separate host tests. Lab test-only purchases, if run, are documented independently in `../native-lab/`; they do not activate real-money purchases.

[Separate Lab purchase and cold qualification](../native-lab/README.md) is now complete: 48 native assertions / 13 touchscreen actions / 6 original PNGs plus 12 derived checks. It preserves all six observed production maps. [Task-device finish receipt](device-resume/finish.json) records restored display, removed observers and guarded shutdown with both earned profiles retained. [Shutdown confirmation](device-resume/finish-confirmed.json) confirms the device is no longer attached.
