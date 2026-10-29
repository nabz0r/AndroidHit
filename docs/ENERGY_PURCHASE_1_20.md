# Session energy and premium refill protocol1.20

The prepared offer is **10 session-energy units for50 LumaGold**, available to purchase only at0/10. The price is an explicit design assumption; no conversion, retention or revenue improvement has been measured. LumaCoins never pay for this pack. The Lab uses a dedicated500-unit premium test balance and never imports earned Coins or contacts the production wallet.

## Activation boundary

- Android `EnergyPurchaseController.PRODUCTION_ENERGY_ENABLED=false`.
- Server `energyRefillsEnabled=false` unless the explicitly validated `ENERGY_REFILLS_ENABLED=true` environment value is supplied.
- The example configuration remains false. No Cloud Run/backend deployment, provider setup, real checkout or real Gold debit was performed for this work.
- The existing Gold purchase integration remains under its own configuration/Play acceptance gates. Adding this SKU does not qualify those providers.

## One purchase, one stock entry, one receipt

| Phase | Durable data before an external effect | Result / recovery |
|---|---|---|
| Confirmed purchase | Account-scoped `SPEND` outbox with independent spend/use UUIDs | Retry the exact spend UUID after timeout or process recreation. Server debit50 and stock+1 occur in one transaction. |
| Pack owned | `USE` outbox, original use UUID | One stored pack can wait indefinitely. It is requested only when local energy is zero. |
| Pack consumed | Server receipt `{id, sku, kind:"energy", itemId, amount:10, owner:SHA256(uid)}` | Local `CREDIT` phase is saved before any local credit. The client checks every field, including exact owner and amount. |
| Local energy credited | Atomic session-energy state plus `energy_receipt_<SHA256(sku + newline + exactUID + newline + useUUID)>` | The outbox is cleared only after `hasReceipt` confirms the durable marker. A replay returns no second credit. |

Gold amounts and prices are never client request fields. The wallet endpoint requires the existing verified Google Firebase identity and checks its revocation. A duplicate request with another action or SKU conflicts. Purchased stock and spend/use receipts live under the authenticated account; a different account cannot spend its inventory. Request receipts are retained until normal account deletion.

If regeneration raises local energy while a response is in flight, the pack/receipt remains pending. It is applied when energy returns to zero, so all ten paid units fit. A crash after credit but before outbox acknowledgement is resolved using `hasReceipt`, even if energy is now full. Failed local commits block outbound requests, and ambiguous failed acknowledgements retain enough durable information to retry.

Two independent controllers reuse the same account outbox. Cached stock rejects older wallet versions. A definitive server `INSUFFICIENT_ITEM` settles an unconsumed use request, avoiding a permanent lock if another device already consumed that stock. Other ambiguous errors preserve the UUID for recovery.

## Android integration

`EnergyPurchaseController(Context, SessionEnergyStore, Listener)` exposes `onForeground`, `buyRefill`, `useStoredRefill`, `close`, `available`, `isBusy`, `isTestMode`, `ownedRefills`, `testGoldBalance`, `priceGold`, `packAmount`, and `statusLabel(language)`. The listener has `onChanged()` and `onError(String key)`; the screen shows the localized status, not an internal error code.

The screen owns the explicit price confirmation. Guests and a closed unused production offer start no wallet HTTP client or Google sign-in. `onForeground` only restores existing account work or refreshes an available signed-in inventory. `close` rejects late callbacks. Production and Lab never share balances or receipt namespaces.

`ProductionWalletClient.useInventory` only allowlists the energy SKU, persists no bearer token, refuses redirects, bounds responses to128KiB, and uses12s connection/20s read timeouts. The controller owns the durable UUIDs. The parser requires `Integer`/`Long` balances and versions, rejecting decimal and overflowing `org.json` numeric representations.

## Verified scope and limits

[Measured test records](validation/v1.20/energy-purchase/README.md) cover process recreation, lost replies after commits, failed disk acknowledgement, stale/cross-account callbacks, malformed receipts, natural regeneration during a response, concurrent account transactions, and the actual client request/response code with explicit transport doubles.

Unconsumed server packs restore across devices. Local energy and its applied markers remain installation-local, with local offline regeneration. They are not a server energy account or a cross-device anti-cheat guarantee. Clearing app storage after a pack was consumed also clears its local energy/outbox; there is no automatic recovery of that consumed installation-local energy. Provider test purchases, physical phones, real-money transactions, and a production energy backend were not used. Production activation needs its own acceptance run, including interrupted delivery, account changes and these installation-local limits.
