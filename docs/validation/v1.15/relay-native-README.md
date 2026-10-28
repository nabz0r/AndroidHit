# Native U1–U1000 qualification — candidate E

**PASS.** The exact signed production candidate `1.15.0-rc1`, code 17, was played through U1–U1000 on the owned `Luma-Refonte115-QA` emulator (`emulator-5586`). Its APK SHA-256 was verified before both batches and again after U1000:

`07b5bfc2632edc3d34f7583ac71c8343b52d7b926ed526aeb7a88599253df0f9`

The [strict aggregate](relay-native-summary.json) verifies 1,000 distinct first wins in order, 10,974 certificate exits plus two extra exits for the Undo probe, 12,016 actual touchscreen gestures, 49,575 native assertions, 112 original PNG captures and 55,000 actually earned LumaCoins. Both batches pass. No progression, stars, coins, history or solution actions were written into the target profile. A separate fresh-device/showcase route is outside this continuous coverage.

The observer imports only Android framework APIs. The host-frozen certificates identify pieces by row/column; Android accessible bounds are observed anew on each board. Real touchscreen events play each exit and each Next control. The observer then reads the persisted history/action count and verifies the visible gate states after each relay. It also verifies content identity 7, budget policy 5, free blocked attempts, an inert closed U13 gate and two real refunded U13 Undo operations. The application performs its own internal guidance normally; the observer never calls a target solver, renderer or private method.

## Independent batches and observer provenance

| Scope | Native checks | Actual touch gestures | Original PNGs | Observed wall time | Report |
| --- | ---: | ---: | ---: | ---: | --- |
| U1–U100 pilot | 4,082 | 763 | 72 | 233.962 s | [Pilot](relay-native-e-pilot100/host-report.json) |
| U101–U1000 continuation | 45,493 | 11,253 | 40 | 2,786.929 s | [Continuation](relay-native-e-continue1000/host-report.json) |

The pilot observer APK hash is `66345209b15a5a198213c6f66f95eff7bd7ff73a200951e5c4cdee7a26ed8868`. Its [source and signature proof](relay-native-observer-e/observer.json) are retained separately. The continuation observer hash is `1da776d1b65c9208a85d0d88d8940845d9765323e70b1747a5e68b059d3e5f47`, with a separate [source and signature proof](relay-native-observer-fast/observer.json). Both match the target signer and package the same 1,000 fixtures.

The continuation removes redundant accessibility queries while retaining exact history/action checks after every unique touchscreen tap and all four gate labels after every relay. Recorded level-loop time averaged 336.81 ms per observed exit in the pilot and 246.95 ms in continuation, including their checks/captures. These cover different boards; another owned QA emulator shared the development host during part of continuation. They are QA durations, not a controlled app-performance comparison, human solving time or physical-phone performance evidence. Motion settings on 5586 were not changed during the route.

## Visual scope and cleanup

Initial/cleared-board captures cover the first 30 levels, periodic boundaries, U99/U101/U999/U1000 and the selected blocked/Undo probes. Original U13, U300 and U1000 frames were inspected. The U1000 capture named `u1000-victory.png` precedes the displayed reward panel by one rendered frame and shows the genuinely cleared board. Its adjacent accessibility snapshot exposes the victory/Next state; this frame must not be captioned as pixels of the reward panel. [U13 victory](relay-native-e-pilot100/native/u13-victory.png) shows the complete panel.

After aggregation, only the owned observer package was removed. The [final installed-byte check](relay-native-final-device.json) confirms the production APK was unchanged, the earned profile was retained and the emulator was left running for the next separately recorded scope. All touch events, incremental progress, assertions and capture hashes remain in the two reports. Host exhaustive model validation is recorded separately under `relay-engine/`; these SDK runs establish actual candidate execution, not human enjoyment or calibrated difficulty.

Reproduce the aggregate with `python3 scripts/v115-relay-ui/summarize.py`. The SDK observer/build/runner instructions are in [the dedicated harness](../../../scripts/v115-relay-ui/README.md).
