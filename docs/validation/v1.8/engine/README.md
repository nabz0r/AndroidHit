# 1.8 V4 engine source evidence

These reports qualify the new pure Java rules and frozen L/M/T content. They do not establish installed Android UI/lifecycle quality, real Google qualification, human enjoyment, retention, live advertising or paid checkout. Historical C/R/B/Q/G/H/P/E fingerprints and the separately tested J/K route remain preserved.

[Rules and APIs](../../../PROGRESSION_V4.md) · [Final exhaustive result](strategy-all-states.txt) · [Per-board traversal](strategy-all-states.csv) · [Final execution](strategy-execution.txt) · [Source hashes](source-inputs.sha256).

The final exhaustive run passes **773,092,810 assertions** over all 1,240 new boards, **13,254,058 reachable action states** and **57,403,554 legal transitions**. The independent bottom-up oracle covers **31,230,944 gate states**. Every reached state has the same exact minimum productive cost in the runtime planner; every allowed exit/gate transition, immediate preview and Undo is checked. An additional 13,839 complete plans and typed save replays are sampled.

The oracle computes child piece-subsets first, then independently relaxes every four-bit gate cube from state 15 down to 0. Runtime uses a sparse memoized search with certificate preferences. It does not call the independent oracle or trust its values. The model checks did not drive the Android UI through millions of positions.

[Targeted contracts](strategy-contracts.txt) add **33,611 checks**: all 1,240 boards are unique under D4 transforms, translations and reordered tile IDs; invalid ranges/typed histories/certificates are rejected; policy 2 migrates to reversible policy 3 without changing its base or fabricating blocked-attempt refunds; Undo refunds at zero; unlimited old friend challenges migrate; extensions remain restorable after a refund. There are 591 boards with keys, 1,161 with a measured meaningful optimal-route decision and 8,830 reauthored switch masks.

The T event catalogue deterministically selects from the 980 audited L21–L1000 geometries. The audit reaches a representative seed for each selectable source and exercises extreme signed seeds. Thus its finite geometry closure is covered; no claim is made to enumerate every 64-bit seed or provide a unique new layout for each one.

Measured cache peaks are **279,897 entries and 9,437,184 bytes of steady primitive arrays**. The configured bound is 524,288 entries. Growth can temporarily retain old and new arrays. The final host run's worst observed individual cost call is 33.89 ms and its elapsed time 50.18 s. These are development-host measurements, not physical Android latency, total application memory or low-end performance guarantees.

## Authored content and reproducibility

[Authoring metrics](strategy-authoring.csv) record each productive minimum, margin, gate/key count, forced/meaningful choices, one-choice cost penalty, dependency depth and comparative score/labels. Timing columns describe that authoring run. The [separate regeneration](regeneration.txt) produces byte-identical frozen `StrategyV4Layouts.java`; timing CSV values are not expected to be byte-identical.

Run with JDK 17:

```sh
./scripts/test-strategy-v4.sh
```

Compiled classes stay in a disposable temporary directory. The runner writes source evidence here and never builds, signs, installs or publishes an APK. Source files and tests are identified in [source-inputs.sha256](source-inputs.sha256).

## Preserved rules

- [Legacy engine](legacy-engine.txt): 437,039 assertions, including historical event and paired-switch seeds.
- [Legacy budget](legacy-budget.txt): 390,327 checks, 2,722 certified boards, corrupt/valid snapshots and unlimited friend accounting.
- [Historical P seals](legacy-sealed.txt): 10,425 checks including every piece-subset × 16 gate states of the six preserved P boards.
- [Preserved V3 progression](legacy-progression-v3.txt): 582,831 checks, all 1,240 J/K boards, independent model, old sessions, geometry uniqueness, codes and budgets. Its [technical audit](progression-v3-audit.csv) and [historical summary](progression-v3-summary.md) retain their original rule scope.
- [Historical family fingerprints](legacy-content.sha256): match the pre-change baseline exactly.

The retained budget tests intentionally validate policy 1/2 paid-Undo accounting. That historical behavior is not a statement that new policy 3 Undo is charged. Installed 1.8 acceptance and the ongoing real-account tests belong to their separate reports.
