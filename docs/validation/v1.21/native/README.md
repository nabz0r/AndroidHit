# Native production B acceptance

Candidate B **passes this native technical scope**. The [final index](candidate-b-final-index.json) binds every accepted phase, original image/video, observer and installed APK. It verifies **295 evidence hashes** and records **574 native assertions, 149 actual touch actions, 52 original PNGs and 2 original recordings across 13 phases**. **38 derived comparisons** are reported separately; they are not added to the native assertion total. Actual scrolling also has native evidence and is not padded into the touch count.

| Identity | Exact value |
| --- | --- |
| Package | `com.androidhit.lumadrift` |
| Version | `1.21.0-rc1`, code `29` |
| Production APK SHA-256 | `e382a73037418146bf57e1530998efeb312953ea8b9b5bb61b548af7cbf90bcc` |
| Observer APK SHA-256 | `6cd58cada407778df628704b9930982053867ee388935222d02ab2cfb68a0f66` |
| Signer certificate SHA-256 | `dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287` |
| Task emulator | `Luma119-Battle-QA`, `emulator-5602`, Android 36, arm64, 16 KiB pages |
| Original display | 1080 × 2400, density 420, font scale 1.0 |

The [frozen source manifest](../candidate-b/candidate.json) contains the release owner's 493 app inputs. The dedicated observer kit is [scripts/v121-ui-test](../../../../scripts/v121-ui-test/). It uses observed accessibility targets, real touchscreen gestures and read-only preference snapshots. No fixture injection, profile clearing, currency grant, progress write or clock change was used. Preserved production 1.20 F was updated to A, then A to B, with exact installed-byte and signer checks.

## Accepted native phases

| Report | Native assertions | Touch actions | Original PNGs |
| --- | ---: | ---: | ---: |
| [Exact A → B update](candidate-b-upgrade-preservation/report.json) | 6 | 0 | 0 |
| [FR/EN home, evolution, economy, shop, map](candidate-b-media-fr-en/report.json) | 32 | 26 | 16 |
| [U42 shells and free Focus before cold](candidate-b-shell-focus-prepare-cold/report.json) | 115 | 14 | 4 |
| [Cold resume, actual U42/U43 wins and gift](candidate-b-shell-focus-cold-resume/report.json) | 137 | 26 | 6 |
| [Living, kitchen and garden selectors](candidate-b-rooms-real-selectors/report.json) | 16 | 10 | 4 |
| [Earned Coin draw: odds, cancel, confirm](candidate-b-earned-gacha-confirm-cancel/report.json) | 23 | 10 | 4 |
| [Draw receipt cold recovery](candidate-b-earned-gacha-cold-recovery/report.json) | 8 | 5 | 1 |
| [Two free Undo uses, paid cancel/confirm](candidate-b-paid-undo-prepare-cold/report.json) | 74 | 15 | 2 |
| [Paid Undo cold recovery and actual U44 win](candidate-b-paid-undo-cold-resume/report.json) | 68 | 11 | 2 |
| [U45 blocked feedback and actual accounting](candidate-b-blocked-feedback-accounting/report.json) | 42 | 10 | 1 |
| [Compact FR/EN, reduced motion, free replay](candidate-b-compact-reduced-fr-en/report.json) | 41 | 22 | 12 |
| [Production snapshot before Lab](production-before-lab/report.json) | 6 | 0 | 0 |
| [Production snapshot after Lab](production-after-lab/report.json) | 6 | 0 | 0 |
| **Accepted B total** | **574** | **149** | **52** |

## Observed results

- Fresh U42 contains ICE/FIRE/WATER shells. Five actual taps each remove exactly one layer without consuming action budget, history, Undo, battle/Focus state or Coins. Three distinct manual exits earn one free Focus; activation consumes no hint inventory or currency. The ES121/Focus state survives real process death and resume.
- Actual three-star U42 and U43 clears each award **67 Coins and 35 XP**. The earned **50 Coin gift** reveals once, stays on screen until explicit continuation and is not credited twice.
- Actual room selectors persist living → kitchen → garden → living. Wallet, progression, XP and squad remain unchanged.
- One real **180 Coin** companion draw follows the displayed odds. Opening/cancelling the confirmation leaves the wallet unchanged. Confirmation changes Coins **919 → 739**, grants exactly one new Prickly copy and retains the equipped Nova/squad. Cold last-arrival recovery returns the same receipt and balance. Receipt checks cover the stated duplicate compensation rules; this draw was not a duplicate.
- U44's first two Undo uses are free. Cancelling the third-use **15 Coin** confirmation changes nothing; confirming changes Coins **739 → 724**, persists one `used:15` UUID receipt and empties the pending journal. Cold resume preserves the same run/receipt and does not debit again. An actual one-star U44 completion awards **43 Coins and 25 XP**.
- A fresh U45 blocked tile displays the red below-board **“Blocked · 0 moves lost”** feedback. Actual action budget stays zero, history stays empty and Coins remain unchanged. This check removes the real shell first, then tests the blocked exit.
- Compact **720 × 1280 / density 320 / font 1.3** menus, map, shop and a free U1 replay pass in FR/EN. Replay rewards are zero Coins/XP. The two original reduced-motion map PNGs are byte-identical across an actual wait. Full accessibility labels and lower routes remain reachable where compact captions are ellipsized.
- The final production profile has frontier **45**, **767 Coins**, **1485 XP**, Nova equipped, squad `nova,miko,orbit`, one Prickly copy and the living room. The [complete six-map before/after Lab comparison](production-profile-preserved-after-lab.json) has **zero differences**.

## Derived checks and media

The separate 38 comparisons comprise: full A → B profile preservation (1), shell/Focus cold state (11), draw cold state (6), paid Undo cold state (11), room neutrality (4), reduced-motion/display/video metadata (4), and full production isolation before/after Lab (1). A full-map comparison counts once regardless of its number of fields. The final [device handoff audit](final-device-handoff.json) is separate from these totals: both game packages remain installed, no observer package remains, original display/font settings and creation receipt are preserved, and the AVD was left running for the release owner's handoff. After the owner accepted both indexes, a separate [guarded shutdown receipt](device-termination.json) records stopping that task emulator with its userdata retained. Neither acceptance index nor the earlier live-handoff receipt was rewritten.

The two original, unedited task-emulator recordings are [menu motion](candidate-b-media-fr-en/native-ui-recording.mp4) (59.891 s) and [gameplay/cold continuation](candidate-b-shell-focus-cold-resume/native-ui-recording.mp4) (59.965 s), each 720 × 1600. Their hashes and metadata are in the index and [media audit](candidate-b-display-motion-media-audit.json).

Eight original **1080 × 2400** FR/EN home/shop/map/puzzle images are listed in the [accepted site capture map](site-media-candidate-b-accepted/capture-map.json), with [provenance](site-media-candidate-b-accepted/provenance.json). Each map entry points back to its original phase PNG and passed outer report; copied exports are excluded from the 52 PNG total. The earlier partial export failure remains retained and is not the publication source.

## Retained failures and limits

[Candidate A was rejected](CANDIDATE_A_REVIEW.md). Its two failed menu reports, original images and recordings remain intact. Correcting the observer's premature scrolling heuristic still reproduced the application's measured-content scroll bug. B fixes that route defect, home XP/settings and rarity overlap, and map icon/caption issues. Original B home/map/shop/puzzle/economy/room/draw/compact images were inspected; no blocking B layout defect was found. A phases and the failed partial exporter are excluded from B acceptance totals.

This is technical acceptance on one guarded, headless software-rendered emulator. It does not establish physical-device feel, audio quality, frame-time performance, user comprehension, enjoyment, retention, conversion or store/provider approval. Native play here covers U42–U44, U45's blocked path and a U1 replay. Verification of all 1,000 authored puzzles/geometry belongs to the separate host scope; it is not described as 1,000 native plays. A single draw validates its real transaction/reveal/cold path, not random rarity frequencies. Lab test Gold and production billing remain separate scopes.
