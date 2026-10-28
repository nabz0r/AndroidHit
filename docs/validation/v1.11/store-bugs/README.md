# Store and equipment audit — v1.11

This audit separates persisted selection from visible rendering. It uses the real `GameStore`, catalogs, `InventoryPresentation` and `AvatarLoadout` on JVM preference doubles. No Android profile, Google account or payment provider is edited or injected. Native interaction and renderer qualification are separate tasks.

## Normal purchase and equipment chains

All 72 catalog identities pass their normal acquisition/equipment/cold-read chain: 24 characters, six themes, 24 clothes and 18 equipment items. Earned unlock setup calls the actual store APIs for 150 Journey clears, lifetime weekly wins, event stars and ten claimed login gifts. Coin funding uses explicit existing grant calls within their normal limits; no preference values are seeded. Clothes and equipment remain in their own four and three slots, and repeated purchases do not debit an owned item again.

These checks establish distinct persisted IDs, ownership and inventory states. They do not establish that every character or equipped object is visibly distinct in the renderer. The normal chains did not reproduce a lost selection after a successful save, so they do not explain the reported invisible equipment by themselves.

## Reproduced read-boundary bug and correction

The committed baseline is `10c78c7cfead1c20b7fb2c7636273c21c11d2f56`. After a refused purchase or equipment commit, character/theme/gear getters read preferences without first settling the exact pending local transaction. Clothes already had the appropriate boundary.

Two controlled failure modes reproduce the problem: a commit leaves memory unchanged, or updates memory before refusing the disk write. Legacy readers can therefore return an old selection or advertise a new undurable one without surfacing the failure. Inventory construction compounds the problem: it reads ownership/loadout first, then `coins()` flushes the pending transaction, leaving a single snapshot with old ownership or equipment and the new durable balance.

`baseline.json` records 28 failing expectations from this shared mechanism. They are 28 controlled symptoms, not a claim of 28 independent product defects. `baseline-source-inputs.json` identifies the exact original sources and test helper used.

The correction adds the existing `LocalSaveTransactions.flush(prefs)` boundary at the start of seven getters: character selection, theme selection, their ownership readers, gear ownership, recorded gear acquisition source and the gear loadout. A repeatedly refused write raises the existing typed save error. A successful read retry settles the original operation before returning a snapshot. No key, price, reward, ownership rule, selection or slot is changed automatically. Neither catalogs, inventory presentation nor loadout rules require a source change.

The dedicated regression passes **683 assertions with zero reproduced read-boundary symptoms**. A separate **17-assertion external-ownership lifecycle** verifies revocation, the existing starter fallback, denied equipment, no duplicate local debit or fabricated local ownership, and retention of locally earned choices after a cold guest read. These tests use an explicit mutable provider double, not real entitlement qualification.

## Reproduced first-initialization defect and correction

`initialization-observation.json` and the retained `InitProbe.java` reproduce a separate existing constructor defect: the first initialization commit is not checked. With memory-before-disk application, the in-memory 180-coin balance can buy Miko for 160 while the `initialized` flag never becomes durable. A cold reconstruction then keeps Miko's separately committed ownership but resets the wallet to 180. Without memory application, the initial model exposes zero coins before reconstruction.

The constructor now writes the same initial values through `LocalSaveTransactions` under its existing global lock. It flushes another handle's pending transaction before checking `initialized`, preventing a second initial grant. Only the constructor catches `LocalSaveException`, because the exact initial transaction remains pending; the first protected read or action retries it or exposes the existing typed error. Other exception classes are not swallowed. No key, initial balance, starting stock or first-arrival flag changes.

The **41-assertion initialization regression** checks both failure modes, repeated refusals on the same and another handle, cold reconstruction before and after commit, a real 160-coin purchase, the durable 20-coin balance across later handles/restarts, exact initial stock, denied actions while initialization remains refused and 24 concurrent first handles. The historical initialization observation is retained and was independently reproduced using the exact committed pre-fix store in a temporary compile; see `initialization-observation-provenance.json`.

Source inspection confirms that `syncStyle()` first reads the selected theme through the protected getter and catches the typed error into the existing local-save barrier. Before that call, construction reads default Journey progression for chapter/metadata migration (0/1); it does not display an economy/appearance value or perform an acquisition/equipment action. Therefore this report does not claim that absolutely no getter executes before the barrier. Native first-start error handling is qualified separately by the integrated app tests.

Source review also found that the native character/theme preview callback ignored an equipment API returning `false` if external ownership disappeared after opening the preview. The model correctly rejects that case. The integration task now checks the returned `applied` flag and shows the unavailable-item notice instead of the success path. This is a source-level observation here, not a claim of real-provider or native callback acceptance; renderer/native changes are qualified separately.

## Reproduction and scope

```sh
bash scripts/test-store-appearance-v111.sh docs/validation/v1.11/store-bugs/fixed.json
bash scripts/test-store-externals-v111.sh docs/validation/v1.11/store-bugs/externals.json
bash scripts/test-store-init-v111.sh docs/validation/v1.11/store-bugs/initialization-fixed.json
```

`baseline.json` was recorded before the correction using `--baseline`; it is not overwritten by the fixed run. All new regression output is redirected into this v1.11 folder. In `ObjectiveModelsV110Test` and `JourneyRewardsV19Test`, two fixtures now create a second store handle before arming the refusal being tested. Construction itself now retries pending transactions, so constructing a handle inside a getter/claim refusal assertion would otherwise consume that one simulated failure before the actual operation. The same assertions and counts remain; historical reports are untouched.

| Regression | Result |
| --- | --- |
| Appearance / initialization / external ownership | 683 / 41 / 17 assertions, PASS |
| Data / meta / economy / inventory | 8,882 / 362 / 6,611 / 1,482 assertions, PASS; journal JSON/Unicode export also passes |
| Cosmetics / objective models / local saves / Journey rewards | 100,953 / 443 / 229 / 65,736 assertions, PASS |
| Tutorial / move budget | 1,454 / 390,327 assertions, PASS |
| Strategy V4 | 33,611 contracts and 773,092,810 audit assertions over all 1,240 boards, PASS |
| Guardians | 472,484,607 assertions over all 100 encounters, PASS |

The strategy test was initially started without its explicit internal report-directory argument. It was stopped before writing the timed audit outputs and restarted with `docs/validation/v1.11/store-bugs/strategy-regression`; the historical v1.8 engine evidence remains byte-identical to the committed files. The guardian report directory was normalized to `guardian-regression/` after its successful run; its recorded data and source hashes are unchanged.

`source-inputs.json` records the final scoped implementation/test sources; `regression-summary.json` records the actual report hashes and results. This audit is not a native rendering, real account, physical-flash, commercial or player-retention result.
