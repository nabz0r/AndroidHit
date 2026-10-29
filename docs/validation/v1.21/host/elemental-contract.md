# ElementalShell121 model contract

This is a pure attempt-local overlay around existing U pieces. It changes no
`Level`, `Tile`, `Barrier`, `Board`, certified exit plan, gate rule, currency or
action budget. Native integration and touch/render/persistence acceptance remain
separate work.

`create(level, mode, index)` assigns shells only to relay progression 6 in
campaign mode 0 or expedition mode 5, with the actual U index equal to the level
number and greater than 10. The first ten boards, N/L historical boards, events,
Remix and friend challenges stay bare. A mismatched relay index throws rather
than silently assigning shells to the wrong puzzle.

A stable SHA-256 ranking of actual tile IDs selects at most three distinct
pieces: Ice has two layers, Fire two and Water one. U11–U1000 each need at most
five direct preparation taps per attempt. There is no timer, random draw,
regeneration, spend or economy dependency. A tap changes exactly one layer and
returns `CRACKED`, `CLEARED` or `NONE`; after clearing, `kind(id)` returns `NONE`.
Capture the prior kind before a tap when choosing the final feedback effect.

Integration must consume a shell tap before attempting an ordinary arrow exit.
The final clearing tap frees the piece; it does not also exit it. Only the next
ordinary arrow gesture uses the existing action budget. Invalid IDs and already
cleared pieces return no effect. `totalLayers()` and `remainingCount()` always
describe current, not initial, state.

For PULSE/FLOW, prepare and validate the real existing companion draft first.
After its full session transaction succeeds, compare old/new remaining-piece
masks and call `clear(id)` only for IDs actually removed. `clear` is idempotent.
GUIDE/VISION/TRAIL only reveal information and cannot clear a shell. The model
cannot enforce the caller's commit ordering; the host test demonstrates actual
certified draft exits and information-only drafts separately.

Broken shells remain broken after Undo in the same attempt, including companion
rewind. A new attempt creates the deterministic initial set again. Historical
saved attempts with no shell field use `empty(level)`; never retroactively add
new layers to an already saved run.

The canonical codec binds full puzzle identity: number, chapter, geometry, gate
mask, event/seed/progression metadata, every dense tile ID/position/arrow/switch,
every barrier/key association and both certified plans. Rows contain only
positive remaining layers of that puzzle's deterministic assigned element, in
ascending ID order. Missing rows mean cleared layers. Strict decoding rejects
unknown versions, wrong identities, extra fields, duplicate or reordered IDs,
wrong elements, excessive/noncanonical numbers and malformed records with a
single null result; the caller can then choose `empty(level)`. It never partially
restores a corrupted record. The codec is a local consistency boundary, not
authenticated server storage.
