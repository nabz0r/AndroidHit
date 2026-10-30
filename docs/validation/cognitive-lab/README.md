# Cognitive Lab 1 — executed study and delivery

30 October 2026. [Method and local commands](../../COGNITIVE_LAB.md) ·
[Interactive FR/EN report](https://luma-drift-production.web.app/lab/cognitive/index.html) ·
[Independent model review](model-review.md).

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
- **Eight offline deployment tests** cover exact source identity, sparse checkouts,
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

[Independent public-byte verification](public-origin-overlay.json) passes all
**15 changed/new HTTPS responses / 14,353,082 bytes**, including the local ZIP
and both raw gzip CSV files. [Actual public browser checks](browser-public/report.json)
pass FR/EN at 320/375/768/1280 pixels with no observed console errors or
horizontal overflow. Earlier saved origin-access refusals remain historical;
the current authorized browser observation succeeded. Older APK responses were
not redownloaded in this check.

Source publication, remote CI, authenticated Firebase management and
public-origin observation are recorded separately after release. A finalized
Hosting release is not by itself a public-byte or public-render verification.
The Android release remains 1.21.0-rc1 / code 29.
