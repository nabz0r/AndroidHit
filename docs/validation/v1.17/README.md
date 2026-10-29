# Luma Drift 1.17 qualification register

**Status: F frozen and binary-verified; global native acceptance and publication pending.** Production is `1.17.0-rc4`, Lab `1.17.0-sandbox-rc4`, code **22**. The production APK is **71,827,197 bytes**, SHA-256 `fe556ae6f0420f41afb7e5db044ef925fe79d0695f0ac6190d6870daf2e8d5a8`. The [F freeze](candidate-f/source-freeze.json) identifies **161 inputs**. The [binary audit](candidate-f/binaries/binary-final-candidate.json) and [build/lint](candidate-f/build.log) pass.

F adds cached responsive root geometry to close the two compact composition findings from the [grouped E review](review/grouped-e-layout-review.json). The [exact sparse F rehearsal](ci-host-rehearsal-f/summary.json) passes **14 commands / 135 files**, including **202,444 assertions over 420 root layouts**. This is host evidence; native F captures must independently close the visual findings. Earlier A–E results retain their own bytes and scopes and do not certify F.

[Release and four-artifact table](../../RELEASE_1_17.md) ·
[Architecture](../../DEVELOPMENT.md) · [Art/audio scope](../../ART_AUDIO.md) ·
[Research](../../design/v1.17/reference-research.md) ·
[Website staging](../site-v1.17/README.md).

## Preserved candidate E scopes before compact composition rejection

Each row is a separate observed scope. Their totals must not be interpreted as
all-screen, audio, full-level or publication acceptance.

| Scope | Observed result | Evidence and boundary |
| --- | --- | --- |
| Four production/Lab APK/AAB files | PASS: exact version/package/bytes, source freeze, signatures, bundles and static 16 KiB alignment | [Independent binary audit](candidate-e/binaries/binary-final-candidate.json), [verification log](candidate-e/binaries/binary-verification.txt), [four builds and both lint tasks](candidate-e/build.log) |
| Retained-profile C→E update, packaged art, main objects/forms and cold persistence | 619 checks / 20 original PNGs PASS | [Upgrade report](native-interface/candidate-e-upgrade/host-report.json). The total includes **6 baseline checks on C**, then **613 on E**: 538 asset checks, 59 preparation checks and 16 cold checks. The 20 captures show E. |
| Compact German/Italian target regression | 19 checks / 2 PNGs PASS | [Regression report](native-interface/candidate-e-clipped-language/host-report.json), actual 320 dp width: 880×1440 pixels at 440 dpi. Original display restored. This dedicated regression is separate from the full compact route. |
| Owned Nova/Miko/Halo looks | 78 checks / 27 PNGs PASS | [Rig route](native-interface/candidate-e-owned-rigs/host-report.json), actual wardrobe/profile/collection and five outfit previews; no acquisition or ownership injection. |
| Normal six-language browsing | 276 checks / 177 PNGs PASS | [Normal route](native-interface/candidate-e-sixlangs-normal/host-report.json), actual 1080×2340 native screen. Full compact and secondary-route qualification remain separate. |
| Genuine empty-device first launch, tutorial, eight first wins, two funded draws and cold persistence | 133 public-UI checks / 105 real gestures / 49 original PNGs PASS | [Fresh aggregate](fresh-e/summary-e.json), [detailed scope](fresh-e/README.md). New E-only AVD, no seed; normally stopped with userdata retained. |
| Separate native Canvas gallery | 144 complete looks, 24 isolated garments, 24 style samples / 50 PNGs PASS | [E gallery report](art/native/candidate-e-art/host-report.json). Public pure renderer only; not application UI, ownership, acquisition or achieved-outfit evidence. |
| C→E gallery pixel preservation | 50 / 50 complete PNG files exactly equal | [Independent byte comparison](review/c-to-e-renderer-pixel-equality.json). All 50 C sheets were directly viewed; E was independently re-rendered. No crop, resize or substitution. |
| Inspected actual UI and full-gallery visual scope | PASS with documented nonblocking limits | [E visual review](review/visual-candidate-e.json): 14 original app frames directly viewed plus the complete 50-sheet gallery equality proof. The wardrobe P2 is closed; sampled contrast improves from C's **1.93:1** to E's **5.45:1**. This is the inspected visual scope, not global release acceptance. |

The visual record keeps three nonblocking limits explicit: a thin sole-like edge
under Jelli's front shoe; twelve free styles are surrounding accent treatments,
not twelve newly painted costumes; some high collars approach the lower chin.
It does not claim bespoke cloth simulation, perfect tailoring or every-pixel
accessibility acceptance.

The fresh E route plays three First Light exits, the starter home, all seven
practice lessons and eight genuine U first victories. The victories earn 440
coins. With the observed 180 starting coins and 100 tutorial gift, the visible
wallet reaches 720; actual Zig and Star parade draws spend 180 + 260 and leave
280. The equipped portrait, seven slots, balance and saved reveal survive a
distinct cold process with no additional debit. The failed first cold observer
attempt and its missing-scroll correction remain separately recorded; it adds
no win or draw to the accepted totals. The main retained 5586 profile is untouched
by this separate fresh-device work.

## Current host checks actually executed

The [E sparse-only rehearsal](ci-host-rehearsal-e/summary.json) copies exactly
the workflow's **132 permitted files** into an isolated directory, then passes
all **13 configured host commands**, recording source and log hashes. It ran
locally with JDK 17 and Node's built-in test runner. This is not a remote GitHub
Actions execution, Android build or native gameplay run. The earlier
[initial rehearsal](ci-host-rehearsal/summary.json) remains historical.

| Scope | Observed result | Evidence |
| --- | --- | --- |
| Scene timing, opacity, lifecycle completion, reduced motion and root gesture ownership | 10,922 checks PASS | [Log](ci-host-rehearsal-e/09-test-scene-transition-v117.txt) |
| Sprite aspect fit, anchors, atlas seams and invalid geometry | 19,857 checks PASS | [Log](ci-host-rehearsal-e/10-test-art-v117-geometry.txt) |
| Fifty actual PCM loops, preservation, earcons and controlled worker lifecycle | 1,714 checks PASS: 1,638 DSP + 76 lifecycle-model checks | [Current rehearsal](ci-host-rehearsal-e/11-test-audio-scenes-v117.txt), [exports/source hashes](audio/scenes/final-b/verification.json) |
| Current/retained FR/EN controller, legal links and deliberate audio previews | 18 tests PASS | [Log](ci-host-rehearsal-e/12-website-controller.txt) |
| All U boards and every reachable relay state/transition | 13,312,469 host checks PASS | [Log](ci-host-rehearsal-e/02-test-relay-v115.txt) |
| Retained local profile, badges, both pools, recoverable receipts and equips | 60,713 checks PASS | [Log](ci-host-rehearsal-e/07-test-profile-summon-v116.txt) |
| Retained N pacing, strict budgets, home migration/camera, guide and power/reward contracts | Every configured command PASS | [All commands and hashed logs](ci-host-rehearsal-e/summary.json) |

The audio report's source hashes match frozen E. Any subsequent audio source
change requires a fresh run. Stubbed `AudioTrack` checks do not establish actual
Android playback; generated PCM previews are not phone recordings. The initial
new-loop seam failures remain in [their failed record](audio/scenes/initial-seam-failure.json).
Website staging checks and actual Hosting/browser acceptance have their own
[register](../site-v1.17/README.md); no staging pass is a publication receipt.

## Preserved intermediate findings

Failed or superseded runs remain under their original bytes. Later success does
not rewrite their status or count them as final E executions.

| Candidate / scope | Retained result and boundary |
| --- | --- |
| A/B functional native observations | [A first look](native-interface/candidate-a-firstlook/host-report.json), [B first look](native-interface/candidate-b-firstlook/host-report.json), [B owned rigs](native-interface/candidate-b-owned-rigs/host-report.json). Functional observations only on those APKs. |
| B gallery and visual review | [Mechanical gallery report](art/native/candidate-b-art/host-report.json); [visual rejection](review/visual-candidate-b.json) retains misplaced glove/boot remnants, silhouette holes, headwear/neckline and scenery/material findings. |
| C wardrobe contrast P2 | [Original C review](review/visual-candidate-c.json): grey status text crosses wood; measured sample **1.93:1**. E's separate actual wardrobe frames close this finding at **5.45:1**. |
| C compact language failure | [Failed host report](native-interface/candidate-c-sixlangs-compact/host-report.json), [raw native failure](native-interface/candidate-c-sixlangs-compact/browse/instrumentation.txt). 60 captures retained: touching visible Deutsch wrongly reached Italian because an offscreen Italian hit target shifted upward. Display restored and owned profile retained. E's dedicated regression passes separately. |
| C genuine fresh route | [C aggregate](fresh/summary-c.json): 133 checks / 104 touches / 48 original PNGs; eight wins, Lune and Star parade acquired/equipped, cold persistence. Its own AVD is normally stopped with userdata retained; this does not certify E. |
| D genuine onboarding only | [D aggregate](fresh-d/summary-d.json): 30 checks / 37 touches / 21 original PNGs. Normally stopped with userdata retained when E superseded it; no D campaign, draw or cold run is claimed. |
| Source/asset review | [Independent review](review/independent-review.md), [asset coverage audit](assets/final-art-audit.json). Coverage alone does not establish visual quality. |

Original application PNGs remain under `docs/screenshots/v1.17/native-interface/`.
Isolated sheets remain under `art/native/` with their QA RENDER captions; neither
they nor analysis crops substitute for actual native website captures. C, D and
E source-freeze directories retain their own identities.

## Remaining acceptance gates

The owner replaces each marker only with actual final evidence. A passed scoped
row above is not a passed global gate below.

| Marker | Required evidence | Current status |
| --- | --- | --- |
| `FINAL_117_CANDIDATE_IDENTITY` | Exact candidate APK and source freeze | F frozen and independently verified; global acceptance pending |
| `FINAL_117_ARTIFACTS` | Four canonical delivery files/manifests and matching audited bytes | Four build outputs audited PASS; canonical delivery/publication pending |
| `FINAL_117_BINARY_AUDIT` | Four builds, both lint tasks, certificates, bundle validation, ZIP/ELF 16 KiB | PASS on F; [audit](candidate-f/binaries/binary-final-candidate.json) |
| `FINAL_117_NATIVE_INTERFACE` | Normal/compact six-language routes, forms, home gestures, transitions/input guards and cold persistence | Earlier E scopes above remain separate; exact F matrix pending |
| `FINAL_117_VISUAL_ACCEPTANCE` | Actual final UI plus complete rig/art inspection | E later rejected for compact root composition; exact F visual closure pending |
| `FINAL_117_INSTALLED_AUDIO` | Exact installed context selection, focus/mute/background behavior and scoped listening evidence | DSP/model PASS; installed qualification pending |
| `FINAL_117_NATIVE_REPLAY` | Verified `relay/summary.json`: U1–U1000 actual touch journals, three stars, zero duplicate rewards | Observer/fixtures prepared; F full replay not launched |
| `FINAL_117_WEBSITE_ACCEPTANCE` | Sixteen exact original FR/EN captures, offline acceptance and 320/375/768/1280 browser review | Staged; final acceptance pending |
| `FINAL_117_PUBLICATION` | Clean immutable commit, verified public downloads, Firebase receipt and public-byte comparison | Not performed for 1.17 |

The [relay preparation](relay/preparation-e.json) verifies the existing 1,000
frozen fixtures and 10,974 certificate exits without device access. The
[resume pointer](relay/active-handoff.json) records the separately signed observer
and detached launcher; it is ready but is not a completion report. Launch requires
explicit exclusive handoff of task-owned `Luma-Refonte115-QA` / `emulator-5586`
after current interface and audio qualification.

That retained profile already genuinely completed every U board with three
stars. Replay must preserve earned progression, coins, ownership, appearance,
profile, receipts, room layout, power stock and settings, and restore the original
language. **Zero duplicate first-win coins** are required. Its saved puzzle
session naturally becomes the replay session through normal UI. No target win
hooks, progression seeding, preference writes or fake wins are permitted.

## Limits retained at delivery

Native touch/capture evidence, isolated rendering, host state enumeration, PCM
synthesis, installed playback and physical speaker evaluation are distinct
scopes. Emulator execution time is not phone performance or human difficulty.
No human comprehension, enjoyment, retention or audience size is measured.
Test ads and closed checkout/premium gates remain; real Google/provider/payment/
Play qualification is separate. Local game, profile and house data are not
synchronized by Google identity.

Keep 1.15/1.16 marathons, hashes, screenshots and Hosting receipts under their own
release labels. They do not establish 1.17 delivery.
