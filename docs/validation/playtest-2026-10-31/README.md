# Playtest and diagnosis —31 October 2026

Two separate scopes are retained here: the exact published 1.23 E baseline, and source-level repairs leading to 1.24. Native acceptance of 1.24 lives in [the new register](../v1.24/README.md).

| Evidence | Executed scope | Boundary |
| --- | --- | --- |
| [Baseline simulation](simulation/README.md) | 8,000 synthetic trajectories; 377,413 attempts; 372,673 wins | Headless puzzle only; no human perception, graphics, decor, ads or premium account |
| [Production native](native-production/summary.json) | 10 programs; 156 checks; 81 SDK gestures; exact E APK | Actual rooms, earned purchases, previews, equipment, movement and preservation of an active U29 |
| [Lab native](native-lab/README.md) | 10 programs; 1,287 checks; 441 SDK gestures; 38 original PNGs | Test Gold, real energy debits and refills; ad consent/load observed, no provider-earned callback proved |
| [Economy](economy/README.md) | 10,000 local sessions and 10,000 server-model sessions, plus callback/persistence failures | Controlled adapters and local services, not live purchases or impressions |
| [Core review](CORE_LOOP_REVIEW.md) | XP and companion progression had no direct Journey consequence | Diagnosis, not evidence of human enjoyment |
| [Rank/copy contracts](rank-copy-124/README.md) | Actual captain, atomic copy rewards, migration, failed writes and cold recovery | Host contracts; the subsequent native runs have their own identities |

The production run retained three harness stops and the Lab run retained three. The original failures were not erased. Two production navigation defects were reproduced: closing an equipment preview lost its tab, and returning after applying equipment reopened its preview. These are fixed and retested in 1.24.

The chosen loop is first-clear XP → player rank → copy for the actual run captain → companion level → talent capacity on the next puzzle. Decoration continues to use earned Coins. [Implementation contract](CORE_LOOP_MVP_CONTRACT.md) and [visual review](UX_REVIEW.md) keep the mechanical and visual scopes distinct.

No study in this directory measures human retention, calibrated IQ, clinical outcomes or market demand. Production and Lab devices, balances, receipts and teardown records remain separate.
