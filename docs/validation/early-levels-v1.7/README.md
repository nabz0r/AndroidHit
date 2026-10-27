# Early-level diagnosis and proposal — 1.7 source baseline

**Open design issue; no rules or budgets have been changed in the shipped 1.7 release.** Baseline: `245f9f59f01ca1f5a4226b14ab8e8c707bcaf896`. This evidence is separate from the ongoing real Google/two-account qualification.

[Full review](../../LEVEL_MANAGEMENT_REVIEW.md) · [Detailed diagnosis](REPORT.md) · [Next-release proposal](NEXT_RELEASE_PROPOSAL.md) · [Executed record](execution.txt) · [Exact source hashes](source-inputs.sha256).

Run `./scripts/level-rework-audit/run.sh` from the repository root with JDK17. It compiles only the pure Java engine and audit sources into a disposable temporary directory, runs both exact models, and writes small reports here. An optional first argument selects another report directory. It does not start Android, connect an account, modify gameplay or deploy a service. The default JDK is the Homebrew JDK17 installation; `JAVA_HOME` can point to another JDK17 installation.

- [J1–30 metrics](J1-30-metrics.csv) and [summary](summary.json): 2,254 reachable states, 5,639 legal transitions, 420 losing states; runtime legality, exact solvability and move safety agree with an independent cell-ray model.
- [J7 reproduction](J7-reproduction.md), [all J7 states](J7-states.csv), [zero-based automation fixture](J7-fixture-zero-based.json).
- [J8 reproduction](J8-reproduction.md), [all J8 states](J8-states.csv), [zero-based automation fixture](J8-fixture-zero-based.json).
- [J13 reproduction](J13-reproduction.md) and [all J13 states](J13-states.csv) retain the same initial-solvability versus later-trap distinction.
- [Manual-gate proposal metrics](manual-gate-proposal-metrics.csv) and [model output](manual-gate-proposal-output.txt): all old losing states become recoverable with at most two additional gate openings. This is a simulation of proposed rules, not an implemented Android feature.
- [Engine audit output](engine-audit-output.txt) retains per-level totals. The mathematical uniform-legal completion probabilities are reference-agent statistics, not measured player difficulty or retention.

Both audits passed on the recorded source. No APK, AAB, version, app source, public site, live account, advertisement or paid transaction was changed by this documentation task.
