# 1.23 — Journey teaching, saved preparation and complete-look transaction

The new Journey factory `ElementalShell121.createJourney123(level,mode,index)`
starts mode0 U attempts bare. It preserves the level/index identity check.
`create`, ES121 encoding/decoding and expedition mode5 remain unchanged. An
already saved preparation is decoded as saved; it is not silently cleared by
an upgrade. Restarting or starting another Journey uses the new bare factory.

No U layout, ID, certificate, move policy, reward, Coin price, session-energy
eligibility or capacity changes in this core patch. The original +3 productive
correction margin remains; removing free preparation taps does not itself
spend or refund a move. Resource pacing remains a separate experimental/human
question, rather than a reason to manufacture improved performance by easing
U43/U48. The first real order trap at U27 remains present.

`LearningGuide.contextJourney123` adds visible teaching contexts for inspection
at U13–16, anticipation at U17–28 and the actual first double relays at U61–64.
`inspectionRecommended` reads remaining relay marks without using a solver.
The UI must expose the contextual HUD and preview; the core methods alone do
not establish that a player has seen or understood the teaching. Six languages
describe immediate consequences, without claiming that preview certifies the
whole solution. Mechanic lesson descriptions no longer promise unlimited/free
Undo. `relay_undo_current_note` explicitly states two free returns, later shown
Coin prices, and the spent exit not being refunded. The historical Undo note
remains available for older policies.

`GameStore.applyCosmeticLook123(Map<Slot,String>, boolean original)` accepts a
complete owned four-slot draft. It validates every ID/slot before one existing
`LocalSaveTransactions.write` commits all four selections and the presentation
flag. Invalid/partial/unowned drafts perform no new write. The original costume
still retains four valid choices. Failed writes use the existing typed retry
boundary; a failed disk commit does not create a partially durable outfit.
This does not grant clothing, debit Coins, change gear or compose the squad.

## Verification

Run `bash scripts/test-journey-clarity-v123.sh /absolute/new/output`.

- [Journey](journey.json): **52,861 assertions**, all 1,000 actual Board
  certificates manually replayed at the unchanged minimum, **5,950** historical
  preparation-prefix roundtrips, all **1,096** reachable U1–27 engine states,
  retained U27 legal trap and full +3 recovery on six selected boards.
- [Complete outfit](wardrobe.json): **79 assertions**, nine refused drafts
  without writes, one complete five-key commit, cold reconstruction, caller
  draft mutation after acceptance, and four failing commits across both
  memory-before-disk modes.
- [Historical shells](historical-shell-regression.json): **291,912** checks.
- [Full relay engine](relay-host-regression.json): **13,312,469** checks across
  all **191,954** reachable U states and **471,517** legal transitions.
- [Economy](economy-regression.json): **47,008** checks; the zero-spend certified
  route and existing correction-price contracts remain possible and unchanged.
- [Session energy](energy-regression.json): **6,044** checks, including all
  6,000 mode/level eligibility decisions and pending-debit recovery.
- Existing `test-learning-guide.sh`: **5,089** six-language/feedback checks.

[Compiled host inputs](source-inputs.json) bind the new suite. These are Java
engine and persistence-double checks, not synthetic player trajectories,
Android rendering, physical storage failures, human comprehension or enjoyment.
Native integration and the separate 1.23 lab runs have their own receipts.

## Harness corrections preserved

The first new test compilation attempted to subclass final legacy test doubles;
the new test now owns independent preference doubles. Its first funding setup
also requested 2,000 Coins through a public API capped at 500 and was refused;
the corrected test explicitly verifies four separate bounded public grants.
Neither failure required a production-code change.

An invocation of the historical relay runner omitted its output argument and
briefly rewrote its historical timing receipt. The new receipt was moved here,
and the old `docs/validation/v1.15/relay-engine/relay-host.json` was restored
byte-for-byte from Git (SHA256
`e77b0f2f0045805d185ea02b8d0ffe37b8b6d4799e6aeb8cebed537c1e06509f`).
