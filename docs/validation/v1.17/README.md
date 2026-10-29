# Luma Drift 1.17 qualification register

**Frozen F is under final qualification.** Production `1.17.0-rc4`, isolated Lab
`1.17.0-sandbox-rc4`, code **22**. Exact production APK: **71,827,197 bytes**,
SHA-256 `fe556ae6f0420f41afb7e5db044ef925fe79d0695f0ac6190d6870daf2e8d5a8`.
The [source freeze](candidate-f/source-freeze.json) contains **161 inputs**;
[four-package audit](candidate-f/binaries/binary-final-candidate.json) and
[build/lint](candidate-f/build.log) pass. Remaining secondary routes and the
actual 1,000-level replay are running; final Git/Firebase publication is pending.

[Release and exact four-artifact table](../../RELEASE_1_17.md) ·
[Architecture](../../DEVELOPMENT.md) · [Art/audio](../../ART_AUDIO.md) ·
[Primary research](../../design/v1.17/reference-research.md) ·
[Website register](../site-v1.17/README.md).

## Current exact F native evidence

| Scope | Result | Evidence |
| --- | --- | --- |
| Retained E→F upgrade, assets, forms, home gestures and cold persistence | 619 checks / 20 PNGs: 6 baseline E, 613 F | [Upgrade](native-interface/candidate-f-upgrade/host-report.json) |
| EN/DE four roots at actual 320 dp, fonts 1.0 and 1.3 | 285 checks / 8 PNGs each | [Default](native-interface/candidate-f-roots-compact/host-report.json), [large](native-interface/candidate-f-roots-compact-largefont/host-report.json) |
| Original clipped DE/IT language target | 19 checks / 2 PNGs | [Regression](native-interface/candidate-f-clipped-language/host-report.json) |
| Six-language primary browsing, all companion/furniture catalogues and five outfit previews | 276 checks / 177 PNGs at each normal and compact size | [Normal](native-interface/candidate-f-sixlangs-normal/host-report.json), [compact](native-interface/candidate-f-sixlangs-compact/host-report.json) |
| Owned Nova/Miko/Halo wardrobe/profile/collection and fifteen outfit previews | 78 checks / 27 PNGs | [Actual UI](native-interface/candidate-f-owned-rigs/host-report.json) |
| Compact secondary EN/DE | 205 checks / 298 PNGs | [Completed scope](native-interface/candidate-f-secondary-compact-preflight2/host-report.json); four compact languages and normal EN/FR still running |
| Empty-device first launch, tutorial, first U1–U8 wins, earned draws and distinct cold process | 133 checks / 105 actual gestures / 49 original PNGs | [Fresh F aggregate](fresh-f/summary-f.json), [scope](fresh-f/README.md) |
| Isolated pure-render gallery | 144 complete looks, 24 garments, 24 styles / 50 PNGs | [F gallery](art/native/candidate-f-art/host-report.json); not app UI or ownership |
| Direct visual inspection | 58 actual UI frames, plus all 50 gallery sheets through exact C→F byte equality | [F visual report](review/visual-candidate-f.json), [gallery equality](review/c-to-f-renderer-pixel-equality.json) |
| Installed digital audio | 38 checks / 8 PNGs / 130 AudioFlinger dumps | [Native review](review/audio-native-final-f.json) |
| Three native scene transitions | 23 checks and original SDK video | [Report](native-interface/candidate-f-transitions-v2/host-report.json) |
| Cached enlarged-home rendering | 19 checks / 2 PNGs | [Steady render](native-interface/candidate-f-steady-home/host-report.json) |

Every current native report identifies the same exact F hash. Normal/compact
primary browsing covers EN, FR, DE, ES, PT and IT. Secondary coverage is six
compact languages (two on the original, four on its identical physical clone)
and normal EN/FR. The unfinished secondary scopes above are not counted as
passes, and a complete six-by-two secondary matrix is not claimed.

The clone was copied only after normal source shutdown: seven disk hashes
match, then the full protected save/session matches after boot. Its
[creation receipt](secondary-clone/creation.json), guarded wrapper and
[baseline identity](native-interface/candidate-f-clone-identity/identity-comparison.json)
keep separate interactions attributable. This is not a second migration test.
No preferences, money, ownership, progression or histories are seeded. The
actual fresh F installation is separate again and retains its own userdata.

Fresh F genuinely earns 440 coins from eight victories; the observed starting
180 plus tutorial 100 yield 720. Tula and Moonlight draws spend 180 + 260 and
leave 280. Seven equipment slots, receipt and balance survive a new process
without another debit. The retained profile's 1,000-board route is replay
qualification and requires zero repeated first-win rewards.

The installed audio checks observe the four places, HOME/background silence,
resume and independent music/effects settings. Music/effects and French are
restored. These digital emulator observations do not include physical speaker
listening, a real external audio-focus competitor or a native puzzle-audio run.
The original SDK transition video shows routes/fades; its capture cadence and
PTS gaps do not prove 60 fps phone performance.

## Current host checks

The [F sparse rehearsal](ci-host-rehearsal-f/summary.json) copies exactly the
workflow's **135 allowed files**, passes all **14 commands**, and records source
and log hashes. This is local JDK 17/Node execution; remote Actions is a
separate publication result.

| Contract | Result | Evidence |
| --- | --- | --- |
| Shared live root geometry | 202,444 assertions / 420 layouts | [Log](ci-host-rehearsal-f/10-test-root-scenes-v117.txt) |
| Scene timing, opacity, lifecycle, reduced motion and gesture ownership | 10,922 checks | [Log](ci-host-rehearsal-f/09-test-scene-transition-v117.txt) |
| Sprite aspect, anchors, atlas seams and invalid geometry | 19,857 checks | [Log](ci-host-rehearsal-f/11-test-art-v117-geometry.txt) |
| Fifty PCM loops, fourteen effects and worker lifecycle model | 1,714 checks: 1,638 DSP + 76 lifecycle | [Log](ci-host-rehearsal-f/12-test-audio-scenes-v117.txt), [PCM/source report](audio/scenes/final-b/verification.json) |
| Current/previous web controller, legal links and deliberate audio | 18 tests | [Log](ci-host-rehearsal-f/13-website-controller.txt) |
| All U boards and reachable relay state/transition graph | 13,312,469 checks / 1,000 boards | [Log](ci-host-rehearsal-f/02-test-relay-v115.txt) |
| Profile, badges, both pools, receipts and equipment | 60,713 checks | [Log](ci-host-rehearsal-f/07-test-profile-summon-v116.txt) |
| Retained N pacing, budgets, home, guide and reward contracts | All configured commands pass | [Full summary](ci-host-rehearsal-f/summary.json) |

The PCM report keeps its historical final-b directory name; its audio sources
match frozen F. Four 10-second previews are exact app-synthesizer exports,
biome 0 and gain 0.27, not phone recordings. Host AudioTrack doubles do not
replace native playback. [Initial seam failures](audio/scenes/initial-seam-failure.json)
remain preserved.

## Preserved intermediate findings

Failed or superseded runs remain under their original bytes. Later success does
not rewrite their status or count them as final F executions.

| Candidate / scope | Retained result and boundary |
| --- | --- |
| A/B functional native observations | [A first look](native-interface/candidate-a-firstlook/host-report.json), [B first look](native-interface/candidate-b-firstlook/host-report.json), [B owned rigs](native-interface/candidate-b-owned-rigs/host-report.json). Functional observations only on those APKs. |
| B gallery and visual review | [Mechanical gallery report](art/native/candidate-b-art/host-report.json); [visual rejection](review/visual-candidate-b.json) retains misplaced glove/boot remnants, silhouette holes, headwear/neckline and scenery/material findings. |
| C wardrobe contrast P2 | [Original C review](review/visual-candidate-c.json): grey status text crosses wood; measured sample **1.93:1**. E's separate actual wardrobe frames close this finding at **5.45:1**. |
| C compact language failure | [Failed host report](native-interface/candidate-c-sixlangs-compact/host-report.json), [raw native failure](native-interface/candidate-c-sixlangs-compact/browse/instrumentation.txt). 60 captures retained: touching visible Deutsch wrongly reached Italian because an offscreen Italian hit target shifted upward. Display restored and owned profile retained. E's dedicated regression passes separately. |
| C genuine fresh route | [C aggregate](fresh/summary-c.json): 133 checks / 104 touches / 48 original PNGs; eight wins, Lune and Star parade acquired/equipped, cold persistence. Its own AVD is normally stopped with userdata retained; this does not certify F. |
| D genuine onboarding only | [D aggregate](fresh-d/summary-d.json): 30 checks / 37 touches / 21 original PNGs. Normally stopped with userdata retained when E superseded it; no D campaign, draw or cold run is claimed. |
| Source/asset review | [Independent review](review/independent-review.md), [asset coverage audit](assets/final-art-audit.json). Coverage alone does not establish visual quality. |

Original application PNGs remain under `docs/screenshots/v1.17/native-interface/`.
Isolated sheets remain under `art/native/` with their QA RENDER captions; neither
they nor analysis crops substitute for actual native website captures. C, D and
E source-freeze directories retain their own identities.

## F observer failures retained separately

- The first compact secondary observer swiped inside Android's mandatory
  gesture band and switched to an older Lab app. The wrapper stopped on the
  foreign foreground package before further input. F data remained intact.
  [Triage and actual-UI recovery](native-interface/candidate-f-motion-recovery/probe-triage.json)
  and [successful guarded retry](native-interface/candidate-f-secondary-compact-preflight2/host-report.json)
  retain separate statuses. The corrected probe uses observed free background
  outside the system band and checks foreground package.
- The first audio observer timed out after HOME; the next source call was
  synchronous Activity resume, but no captured stack proves the blocking cause.
  Four-scene/background samples remain in the failed attempt. The observer
  changed to asynchronous start with a bounded foreground wait;
  the APK did not change. [Triage](review/audio-observer-timeout-triage.json) ·
  [successful digital scope](review/audio-native-final-f.json).
- The first video observer could not read the SDK shell-owned recording.
  The original MP4 remains. A shell hash/read witness corrected only the
  observer; [retry](native-interface/candidate-f-transitions-v2/host-report.json)
  passes on the unchanged APK.

Raw failed logs retain their exact original output. Signed test-observer APKs
are local validation intermediates and are ignored in the current delivery
tree; their hashes, sources and reports remain attributable. The four canonical
production/Lab release files are the intended distributed packages.

## Pending delivery work

Finish secondary compact FR/ES/PT/IT and normal EN/FR; finish and independently
aggregate the real U1–U1000 F replay; stage exact canonical packages; run strict
website preparation/offline acceptance; publish clean Git/Firebase commits;
verify remote CI and all public HTTPS bytes. [Live replay pointer](relay/active-handoff.json)
counts only actually completed native levels and is not a final pass report.

## Limits retained at delivery

Native touch/capture, isolated rendering, host graph enumeration, PCM exports,
installed playback and physical listening are separate scopes. API36 arm64
native devices report actual 16,384-byte pages; this qualifies the observed
emulators, not all phones/provider paths. Execution time is not phone FPS or
human difficulty. No comprehension, enjoyment, retention or audience size is
measured. Test ads and closed checkout/premium gates remain. Local game,
profile and house are not synchronized by Google identity. Historical 1.15/1.16
marathons and Hosting receipts do not establish 1.17 delivery.
