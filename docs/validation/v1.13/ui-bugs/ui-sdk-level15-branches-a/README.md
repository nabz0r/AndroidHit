# RC2 level 15: blocked taps can make an unchanged board impossible within budget

The original RC2 behavior is reproduced natively: **five blocked taps spend five moves while all 10 pieces remain and productive history stays empty**. The attempt then has 11 moves left for a certified minimum of 12. Undo has no productive action to reverse and refunds nothing. Spark refuses with the visible message **“Not enough moves to clear this attempt”**, retaining its three charges. The budget banner still appears teal at 11/16; it does not warn about insufficient completion budget until help is requested.

This explains a reproducible route to an impossible *attempt*, rather than an inherently unsolvable level 15. The behavior is in shared move handling, so it can recur across levels. Five exploratory taps are enough here even though no piece has moved.

A second branch performs one real exit followed by 15 blocked taps, reaching 0/16. Undo restores that successful exit and refunds one move; the initial 10-piece board is back, but only 1/16 move remains. A free Restart restores 16 moves and the same Spark then works normally, consuming one actual stock charge without spending a move.

Gate and gesture observations: wrong-direction dragging changes neither board nor budget; a correct drag and a tap each produce one exit; touching an already open gate is free; cancelling gate/switch previews is free. Taking the switch early legally closes the up gate, which can be reopened manually. Current RC2 allows repeated productive Undo; three such actions reverse the gate/switch sequence exactly. This describes current behavior and is not a recommendation for unlimited Undo.

Finally, a legal gate/switch-first order **different from the certificate order** wins with three stars in 12 productive actions: `67,4,8,0,2,1,3,7,9,64,5,6`. The level itself is completable after a fresh attempt.

**263 native assertions pass**: 211 earn access through actual Journey 1–14, then 52 observe L15 branches and helper operations. These passing checks prove the diagnostic observations, not that the reproduced policy is acceptable. The run retains 45 raw 1080×2340 full frames, including 15 L15 diagnostic frames. Guest preferences are restored byte-exactly, original rotation is restored and the isolated hometest package is removed.

[Host report](host-report.json), [native branch events](prepare/level 15-audit.json), [diagnostic summary and raw image hashes](diagnostic-summary.json), [exact pre-run source](source-snapshot.json).

No app/engine/release/site changes are made. New product rules and their limited Undo quota require separate 1.14 qualification.
