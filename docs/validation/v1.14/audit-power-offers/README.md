# Current power offers and the retired Shield archive

Blocked attempts become free under the new deliberate run policy. Shield would therefore advertise an effect that no longer helps. The current offer policy is centralised in **`Catalog.isPowerOffered(id)`**: five powers remain offered; Shield and unknown IDs are not offered.

The six historical catalogue definitions and unlock identities remain intact. This is an offer retirement, not an inventory migration or currency conversion.

## New actions

- The current inventory snapshot, next-power announcement and objective/trial dashboard omit Shield.
- An unclaimed new Shield trial returns `NOT_OFFERED`; it does not consume a receipt or create stock.
- A new Shield pack purchase returns false without a new debit or grant.
- Future undecided Journey gifts use only currently offered powers. A milestone's eligible set is still frozen to its earned Journey unlock, so claiming immediately or later gives the same item under the current offer policy.

UI entry points must use the same offer predicate. The generic stock/settlement APIs still recognise the historical ID so an already-authorised request can settle safely; recognising an archived ID is not permission to advertise a new offer.

## Historical archive and exact retries

- Existing `booster_shield` stock is retained. No automatic replacement, deletion, refund or additional currency occurs.
- Both milestone receipt namespaces (`power_loot_v1_*`, `power_loot_v19_*`), the historical trial receipt and already-recorded ad/gift receipts remain acknowledged.
- A recorded historical trial still returns `ALREADY_CLAIMED`.
- Old effect escrow can be restored, cancelled or completed exactly once.
- A pending tracked trial/purchase write is flushed before the new offer is refused. The original exact stock/receipt or debit/stock pair owns that retry.
- A milestone item already selected before retirement is not recomputed during retry. Its old Shield choice is saved exactly, under both commit-refused-before-memory and Android memory-before-disk failure models. Only a subsequent undecided gift uses the current offer list.

The tests inject old pending decisions as explicit compatibility fixtures. They exercise real current store APIs and real preference-double commit failures, not a physical Android storage fault or an upgrade between live processes.

## Executed checks

`bash scripts/test-power-offers-v114.sh` passes [868 dedicated checks](audit-power-offers.json), including all200 future milestone rewards claimed immediately and after deferral, historical stock/receipts, escrow, cold recreation and exact pre-retirement retries.

Current regression suites also pass:

| Scope | Checks | Evidence |
|---|---:|---|
| Current earned power pack durability | 1,514 | [Execution](audit-store-purchases.txt) / [report](audit-store-purchases.json) |
| Trials, stable Journey gifts and action grading | 65,651 | [Execution](audit-rewards.txt) / [report](audit-rewards.json) |
| Objective dashboard and claims | 443 | [Execution](audit-objectives.txt) / [report](audit-objectives.json) |
| Inventory presentation | 1,480 | [Execution](audit-inventory.txt) |
| Economy, receipts, escrow and atomic callbacks | 6,611 | [Execution](audit-economy.txt) |

Historical release reports keep their original six-offer counts and execution scope. The maintained tests now assert five current offers while retaining the six stable definitions and historical stock/receipt assertions. These JVM checks do not qualify native rendering, advertisements, purchases or external services.
