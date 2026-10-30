# Cognitive Lab 1 — executed study and delivery

30 October 2026. [Method and local commands](../../COGNITIVE_LAB.md) ·
[Interactive FR/EN report](https://luma-drift-production.web.app/lab/cognitive/index.html) ·
[Independent model review](model-review.md) · [Final delivery register](release-delivery.json).

## Executed study

The full run executes **100 synthetic profiles × 30 repetitions × 3 scenarios =
9,000 main trajectories**, each covering eight modeled days. It records
**305,510 started attempts and 3,142,360 decision interactions** on the actual U
puzzle engine. Four held-out geometries produce **12,000 matched probe pairs**
(24,000 probe attempts). Four additional ablations produce **800 trajectories**
on a fixed 20-profile × 10-repetition subset. These scopes are separate.

Training draws from U1–U400; the largest started campaign level is U88. The
400-level cap cannot be reached within the 960-interaction study horizon:
even the minimum required exits total 3,094. Held-out U401/U573/U721/U1000 are
outside training. Returns require an actual interaction; no zero-action
administratively capped session is counted as play.

The recorded Java runtime on this Mac is available in
[provenance.json](latest/provenance.json), separately from simulated time. A
complete run takes approximately 14 seconds after compilation on this host;
this is a run-specific observation, not a hardware-independent benchmark.

| Modeled scenario | Mean unique levels cleared | Mean blocked-decision share | Mean session minutes |
| --- | ---: | ---: | ---: |
| Current assumptions | 35.527 | 5.413% | 4.019 |
| Hypothetical teaching support | 36.836 | 4.064% | 3.822 |
| Muted reward salience | 26.410 | 5.191% | 3.044 |

The paired teaching comparison increases modeled clears by **1.309** (whole-profile
bootstrap interval **1.077 to 1.528**) and reduces blocked share by **1.350
percentage points** (**1.239 to 1.465** points lower). Reward effects follow the
explicit reward-sensitive utility and time-budget assumptions; they do not
demonstrate an empirical effect of rewards on people. Intervals describe
variation within the synthetic model, not uncertainty about a human population.

Knowledge-only transplantation to held-out boards raises modeled probe
completion from **92.992% to 98.267%**. This measures the agents' behavior on
unseen geometry, not human cognitive improvement. The small reduced-planning
ablation effect is retained rather than hidden: its mean-clear interval spans
zero. No model is selected on held-out results.

## Validation

- **28,139 host assertions** cover bounded decisions, source separation, current
  boards, native reward/energy parity, IQ-label independence and model controls.
- Two fresh smoke runs reproduce seven output payloads byte for byte. Runtime
  provenance is intentionally separate from deterministic payloads.
- Independent reconciliation reads all **9,000 trajectory rows and 305,510
  attempt rows** and checks exact counts, Coin conservation, source/output
  hashes and all nine paired metric point estimates.
- All **300 selected, attributed decision records** have checked state
  transitions, with 100 records per scenario. They are limited diagnostic
  samples, not representative samples of every profile or a complete action
  history.
- The unchanged native model's separate exhaustive U-board contract passes
  **13,312,469 checks over 191,954 reachable states and 471,517 transitions**.
  This is host execution, not a new Android acceptance run.
- **11 dashboard tests** cover real-data import, finite/range/identity validation,
  safe text rendering, inert CSV formulas, chart geometry and retry attribution.
  Actual local browser checks cover FR/EN at 320/375/768/1280 pixels, scenario
  changes, profile selection, optional IQ labels and full local report import.
- **Eleven offline deployment tests** cover exact source identity, sparse checkouts,
  preserved provider hashes/config, forbidden paths/hosts and concurrent changes.

The historical v1.9 solver-driven simulations are preserved. Their outputs are
not relabeled as this study. IQ labels are independently assigned, uncalibrated
and noncausal. Cognitive and continuation measures are model outputs. The study
has **zero human participants** and diagnoses no addiction.

## Download and reproduce

The [local ZIP](../../../public/lab/cognitive/cognitive-lab-local.zip) includes
the runner, actual engine sources, native test adapters, dashboard and full
recorded data. Open `public/lab/cognitive/index.html` after extraction for the
offline snapshot. Run `bash scripts/test-cognitive-lab.sh` for independent
archive self-tests, then `bash scripts/run-cognitive-lab.sh` to generate a new
study. Java 17 and Python 3 are required; no model download, API key, account or
network service is needed for these commands.

[Archive validation](archive-validation.json) records the separate temporary
extraction and self-test. Raw [trajectories](latest/trajectories.csv.gz) and
[attempts](latest/level-results.csv.gz) are gzip-compressed CSV; the
[single-file report](latest/report.json) is importable in the dashboard.

The final archive is **6,579,468 bytes**, SHA-256
`c21452b86a9b999f3f720243b5960f5ce77fcd671f165d8a1ca47d86a02d063e`.
All 115 extracted files, local asset links, 28,139 model assertions, deterministic
smoke outputs and 11 dashboard tests pass from that exact independent extraction.
Game-home and fresh-ZIP links use the hosted destinations so they remain usable
from an extracted local dashboard. The [earlier archive self-test](archive-validation-before-final.json)
is preserved as historical evidence for its different 6,578,412-byte archive;
it does not identify the final download.

## Publication scopes

The [site manifest](site-manifest.json) binds all physical payloads and an
additive change to the existing Firebase site: the new dashboard and a homepage
research link. **524 earlier payloads retain identical raw SHA-256 hashes**;
only the homepage changes, and no earlier file is removed. APKs, account/legal
pages, original art/audio and historical receipts remain preserved.

An authenticated read-only preflight on the original source commit stopped
before mutation because Firebase additionally stores `/__/firebase/init.js` and
`/__/firebase/init.json`. The deployer now requires those exact two reserved
paths and preserves their provider hashes too; arbitrary additions remain
rejected. Thus the source manifest has **539 physical payloads**, while the
expected cloned provider version has **541 paths**, including **526 retained
historical provider paths**. These counts must not be conflated.

The site was released from source
[`8f3653d0`](https://github.com/nabz0r/AndroidHit/commit/8f3653d0a1a538f67c3136a8fff900a2beb652a9)
as Firebase version `92e40c3a51003cb2` on **2026-10-30T01:34:11.791Z**.
The post-release checker initially treated the default-site and live-channel
release-name aliases as different releases. Both refer to release ID
`1793324051791000`; the version and message matched. A later read-only recovery
verifies the existing release and preserves the initial failure receipt. It
does not deploy a second version.

A subsequent navigation correction is deployed from
[`64c0d930`](https://github.com/nabz0r/AndroidHit/commit/64c0d9302680febbc19bc444ae4d315fd9a2a0f0),
Firebase version **`d88a6817932258f4`**, on **2026-10-30T02:06:57.153Z**.
Its [exact final receipt](hosting/final/deployment.json) changes only dashboard
HTML, export-link JavaScript and the ZIP. It clones the exact earlier version,
retains **538 of its 541 provider hashes**, verifies all 541 final entries and
preserves serving configuration. Source study data and every older Android
download remain unchanged. [Management originals and integrity](hosting/README.md).

[Independent public-byte verification](public-origin-overlay.json) passes all
**15 changed/new HTTPS responses / 14,353,398 bytes**, including the local ZIP
and both raw gzip CSV files. [Actual public browser checks](browser-public/report.json)
pass FR/EN at 320/375/768/1280 pixels with no observed console errors or
horizontal overflow. Earlier saved origin-access refusals remain historical;
the current authorized browser observation succeeded. Older APK responses were
not redownloaded in this check.
The [initial HTTPS receipt](public-origin-overlay-initial.json) and
[initial browser review](browser-public/report-initial.json) remain separately
preserved. The current response/browser records identify final source `64c0d930`.

Source publication, remote CI, authenticated Firebase management and
public-origin observation are recorded separately after release. A finalized
Hosting release is not by itself a public-byte or public-render verification.
The Android release remains 1.21.0-rc1 / code 29.

## Final source validation and workspace maintenance

Both workflows pass on **final source `64c0d930`**:
[Cognitive Lab contracts](https://github.com/nabz0r/AndroidHit/actions/runs/40989676600)
and [Puzzle contracts](https://github.com/nabz0r/AndroidHit/actions/runs/40989676563).
All **three jobs / 45 steps** succeed, including the **11 offline deployment
tests**. [Lab job records](ci-64c0d930-40989676600.json) and
[engine/art job records](ci-64c0d930-40989676563.json) retain their exact source
identity. [Final local integrity](final-local-integrity.json) confirms all 40
simulation-source hashes and the documentation links after cleanup.
[GitHub run metadata](ci-final-runs.json) binds both workflow results to that
exact final source. The closing documentation commit changes no tested code
or public payload and deliberately skips redundant CI.

Both earlier workflows also pass on validator/source commit
[`ea1b7fcc`](https://github.com/nabz0r/AndroidHit/commit/ea1b7fcca28caf119ee140a387ead703fae94f2c):
[Cognitive Lab contracts](https://github.com/nabz0r/AndroidHit/actions/runs/40987738952)
and [Puzzle contracts](https://github.com/nabz0r/AndroidHit/actions/runs/40987738959).
All three jobs and 45 steps succeed, including the eight deployment tests.
The exact returned job records are retained in
[the lab receipt](ci-ea1b7fcc-40987738952.json) and
[the engine/art receipt](ci-ea1b7fcc-40987738959.json). This commit retains the
same public tree as initial deployed source `8f3653d0`; it fixes only management
verification and records the separate public checks. The later navigation-only
correction changes the dashboard HTML, export-link JavaScript and ZIP, with its
own exact archive and delivery checks. The simulated engine and all study data
remain unchanged.

The separately requested [workspace cleanup](../cleanup-2026-10-30/README.md)
removes only ignored regenerable outputs and compacts Git storage. Its source,
release-copy and object-preservation checks pass. Tests were not rerun merely
to recreate the removed compilation caches.
