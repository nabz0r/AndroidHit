# Native Lab B acceptance

Candidate B **passes this separate Lab technical scope**. The [final index](candidate-b-final-index.json) verifies **66 evidence hashes** and records **99 native assertions, 41 actual touch actions, 9 original PNGs and 1 original recording across 3 phases**. **24 derived comparisons** remain separate from the native assertion count. Actual scrolling has its own native evidence and is not padded into touch counts.

| Identity | Exact value |
| --- | --- |
| Package | `com.androidhit.lumadrift.sandbox` |
| Version | `1.21.0-sandbox-rc1`, code `29` |
| Lab APK SHA-256 | `9c0998683f09a1d940a97a0dc5badea98a5ab5c84ea2ba814917e30f8df5235b` |
| Observer APK SHA-256 | `fdc7c422346018a6563c198e91d43c34571d8ba2548f8184a3eafe48702f5534` |
| Prior installed Lab F SHA-256 | `7c8ccfde3a0050b35b0312a61e2cb07973c594629bbf97493ec8bbe2829410cf` |
| Retained signer SHA-256 | `dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287` |
| Task emulator | `Luma119-Battle-QA`, `emulator-5602`, Android 36, arm64, 16 KiB pages |

The dedicated [Lab observer kit](../../../../scripts/v121-lab-ui-test/) targets only the sandbox package. It uses real native controls and read-only snapshots. The earned Lab profile from 1.20 F is updated in place: no app uninstall/clear, test Gold seed/grant, energy grant, progress injection or clock change is used. Natural elapsed-time refills are respected. This scope uses local sandbox test Gold; no external identity or real-money transaction occurs.

| Report | Native assertions | Touch actions | Original PNGs |
| --- | ---: | ---: | ---: |
| [Exact old F → B preservation](candidate-b-upgrade-preservation/report.json) | 6 | 0 | 0 |
| [Real energy drain and confirmed paid refill](candidate-b-actual-paid-refill/report.json) | 73 | 34 | 6 |
| [Same-attempt cold recovery](candidate-b-refill-cold-recovery/report.json) | 20 | 7 | 3 |
| **Accepted Lab B total** | **99** | **41** | **9** |

## Observed transaction and recovery

1. The old earned U1–U10 profile, frontier 11, **730 Coins**, **450 test Gold**, capacity 10 and prior purchase/energy-credit receipts are preserved. [Exact preservation](candidate-b-preserved-lab-profile-audit.json) finds zero differences across all **seven** snapshot maps, including the Lab purchase journal. The inherited helper's scope prose says six-map; its loop actually compares every map in the seven-map baseline.
2. An actual new eligible U11 attempt and **nine confirmed native one-energy restarts** reduce energy to zero. The [restart record](candidate-b-actual-paid-refill/native/genuine-energy-restarts.json) records the real count. No direct state edit substitutes for those actions.
3. The actual energy page discloses **50 test Gold → 10 energy**. Confirming the native prompt changes test Gold **450 → 400** and energy **0 → 10**. Coins remain **730**. The actual journal has exactly one new spent receipt, one new used receipt and one new energy-credit receipt; prior receipts remain present, stock is zero and outbox is empty.
4. One actual new eligible U11 attempt consumes exactly one energy: **10 → 9**, bound to that run ID. After real process death, resuming that same attempt retains **9 energy**, **400 test Gold**, the same run ID, budget/history, elemental shells and Focus state. Both entire transaction/energy journals remain identical; no credit or debit is repeated.
5. FR energy and puzzle states plus the final EN home are captured. The real confirmation and 400 test Gold/10 energy page images were visually inspected, together with the cold 9 energy page; no blocking layout issue was found.

The 24 separate derived comparisons comprise full old-profile preservation (1), actual purchase/receipt accounting (10), cold session fields and full journal equality (12), and original recording metadata (1). The [unedited 60.048-second recording](candidate-b-actual-paid-refill/native-ui-recording.mp4) is 720 × 1600; its exact SHA and metadata are in the [video audit](candidate-b-video-metadata-audit.json).

The [production profile isolation audit](../native/production-profile-preserved-after-lab.json) compares every field of all six production snapshot maps before/after the entire Lab scope and finds **zero differences**. This cross-scope comparison is counted once in the production derived total, not repeated in Lab's 24. Final [device handoff](../native/final-device-handoff.json) confirms both game profiles remain installed, observers removed, original 1080 × 2400/density 420/font 1.0 restored, and the original AVD creation receipt unchanged. After accepting both indexes, the release owner authorized [guarded emulator shutdown](../native/device-termination.json). Its userdata is retained; the prior indexes and live-handoff receipt remain unchanged.

This result concerns one guarded Android emulator and one actual local test Gold transaction. It does not validate production payments, a billing provider, physical-device experience, statistical RNG frequencies, retention, audio or frame-time performance. Exhaustive prior Lab progression is retained as historical evidence; it is not recounted as newly executed 1.21 coverage.
