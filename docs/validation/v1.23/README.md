# Android 1.23 qualification — candidate E

**Local qualification passed. Publication remains pending.** The current candidate
is production `1.23.0-rc2` / separate Lab `1.23.0-sandbox-rc2`, Android code 31.
[Candidate identities and 501 frozen inputs](candidate-e/candidate.json), the
original build/package/signer outputs beside that file, and the
[canonical release manifest](../../../releases/manifest-1.23.0-rc2.json) bind the
exact signed files. Git publication, exact-commit CI, Hosting, public response
bytes and public browser acceptance for 1.23 are separate pending gates.

| Channel | Application ID | Exact APK SHA-256 |
| --- | --- | --- |
| Production | `com.androidhit.lumadrift` | `429a443c9228787c7b854d36a40f0bcf7d52fdc8a3601214cd27b5cfae6945fb` |
| Lab | `com.androidhit.lumadrift.sandbox` | `099d4dc7d38cb4552b3b1ad46f7e3ccb70b304c7c8a1c6c332e16ada00ba558c` |

The [E acceptance record](native/candidate-e-acceptance.json) joins four actual
production emulator runs: **495 checks and 142 SDK touchscreen gestures**.

| E run | Checks | Gestures | Scope |
| --- | ---: | ---: | --- |
| [Retained upgrade](native/candidate-e-retained-context/report.json) | 64 | 17 | Upgrade from private D, preserved attempt/context |
| [True first install, FR/EN](native/candidate-e-genuine-fresh-fr-en/report.json) | 72 | 29 | Fresh userdata, actual first onboarding and puzzles |
| [Critical paths and canonical media](native/candidate-e-critical-fr-en/report.json) | 214 | 78 | Modal swipe isolation, skill refusal/cost/once, Bundle recreation, timer/effects, wardrobe and FR/EN captures |
| [World navigation](native/candidate-e-world-navigation-fr/report.json) | 145 | 18 | Native map/minimap and actual navigation |

These are scripted checks and gestures, not counts of distinct players or games.
The [compiled observer binding](native/compiled-observer-source-binding.json)
records the observer inputs. The [canonical media record](native/candidate-e-canonical-media.json)
and [capture map](native/capture-map.json) associate all eight original
1080×2400 PNGs with this exact production APK: puzzle, skills, wardrobe and map,
each in French and English. No crop, redraw or resize is used for those originals.

The [Lab E report](native-lab/candidate-e-launch-fr-en/report.json) passed
**27 checks and four gestures** for its distinct package, real launch/settings
and FR/EN capture scope. Upgrade preferences were preserved before launch; seven
production preference files remained byte-identical before/after the Lab phase.
This is not a repeat of production gameplay, purchase, account or provider tests.

## Preserved earlier evidence and limits

The [private D rc1 record](native/candidate-d-private-qualification.json) retains
its own **506 checks / 182 gestures**. The checked
[D→E source delta](candidate-e/source-delta-from-d.json) changes two of 501 inputs:
the rc2 version name and `modernAction` palette conversion. The other 499 inputs,
engine, persistence, action handlers, geometry and assets are unchanged. D's
compact/font/reduced-motion and U27/U29 key/Undo suites are bridged through that
bounded delta; they are not relabelled as new E executions. E repeats the upgrade,
first-install, critical-context and map paths listed above.

B and C discovery runs, the earlier failed selectors and device-creation failure,
and separately named retries remain under `native/` with their original verdicts.
Neither an earlier failure nor its passing retry is silently replaced. The
[earlier clarity audit](../clarity-audit-2026-10-30/README.md) remains immutable.

Recorded visual limits remain: some cosmetic/tab captions ellipsize on the D
640×960/font-1.3 display, while full accessibility names and sticky actions remain
available. The fresh E Android system status clock is partly clipped on the
initial fresh captures; game content remains inside the safe view, and the eight
canonical retained captures have a normal system bar.

## Model, study and source delivery

The [core model](core-model/) replays all 1,000 solutions, preserves saved shell
states and budgets, and validates atomic outfit commits. The [synthetic 1.23 study](../cognitive-lab-v123/README.md)
executed **8,000 trajectories, 380,137 attempts and 40,000 probes**. Its 8,000 cold
probes are a single shared reference for 32,000 trained probes. Its 104 oracle
controls and 58 engine/runner source inputs remain distinct from agents and UI
context; no artwork or text comprehension is simulated.

The [rc2 transport receipt](site-transport/rc2/receipt.json) records **37 passing
offline tests**, with the private rc1 logs/sources preserved separately. The
[cleanup manifest](cleanup/public-retirements.json) records 14 public binary
copies removed locally, with their full original hashes and retained canonical
files. Hosting retirement and public HTTP 404 verification are still pending.
The [prepared site source](../site-v1.23/release/source-manifest.json) links exact
E APK/capture identities; its local PASS is not a publication receipt.

This qualification does not establish a physical-phone performance benchmark,
human comprehension, enjoyment, emotion, retention, Play approval or successful
real commerce. Ads remain in test mode and real purchases remain closed.
