# Standalone data-runner dependency repair — 1.19

The parent reported that historical C1 Actions run40942831539 stopped at the data step. A local pre-fix reproduction of `scripts/test-data.sh` confirms20 javac errors starting with missing `com.androidhit.lumadrift.engine` imports from `CompanionBattleAction`. [Exact local failure](before-data-compilation.txt). The remote C1 run remains failed historical evidence; this later harness repair does not rewrite or cancel it.

The audit covers **27 shell runners** compiling `data/*.java`. Ten already included engine sources; **17** did not. Each of those17 receives only an `engine/*.java` source dependency line. The dependency repair changes no application source, test assertion, provider configuration or workflow policy. A separate current-contract test update is recorded below. [Before/after runner hashes](runner-audit.json) · [Initially tested source hashes](tested-source-sha256.json).

Only the17 affected runners were executed. Every optional report/image path pointed to a fresh temporary directory outside the repository, so historical reports and previews were not overwritten. All17 now pass compilation; **15 fully pass runtime assertions**. These include data9,142, current home1.15/1.16, economy/meta, profile/summon60,713, wardrobe/rewards100,953, motion, objective443 and local store persistence scopes. Each original command’s complete output is retained beside [results.json](results.json).

Two unrelated historical runtime assertions become visible after compilation:

- `HomeV113Test` expects twelve furnishings and the old six-by-six room; the current catalogue/rooms expanded in later releases. Its first retained failure is “twelve original objects”.
- `InventoryPresentationTest` expects24 companions; the current catalogue contains37. Its first retained failure is “Full catalogs represented exactly once”.

Neither runner appears in the current `puzzle-contracts.yml` workflow. Accordingly the initial `results.json` retains `passed:false` for that17-run execution, recording the completed dependency repair,15 passes and2 historical runtime failures.

The parent then authorized a separate explicit current-contract update of those two tests. `HomeV113Test` now asserts42 furnishings and a10×10 room, checks that the12 original identities remain present, tests actual current boundaries/path obstruction, and retains explicit6×6 legacy-save validation. `InventoryPresentationTest` now asserts37 distinct companion identities, exactly24 originals plus13 guests, and exact equality with presentation identities. Ownership, persistence/refusal, navigation and read-only assertions remain. No application or APK input changed.

Only those two runners were repeated, again with temporary outputs outside the repository. Both pass: **1,553 HomeV113 assertions** and **1,592 inventory-presentation assertions**. [Distinct final result, exact test source hashes and commands](final-contracts/results.json). Their earlier failures and the initial source-hash manifest remain unchanged. Thus all17 affected runners have passing runtime evidence across the initial15 successes and this two-run follow-up; there was no second full17-run execution.

These are JVM harness/model outcomes, not Android execution or final CI success. Final remote C3 CI must establish its own result on the published source.
