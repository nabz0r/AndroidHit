# Tactical chapter 1.22 — gameplay and persistence contract

This chapter uses twelve new, hand-authored layouts and the existing U engine. It does not replace or rebalance the campaign, modify its saved session, or require ownership of the loaned companions. Host checks establish mechanical and persistence properties. They do not establish human enjoyment, retention, or the feel of Android interactions.

## The playable route

Two episodes, `KEYS` and `RELAYS`, each contain six possible layouts. A sortie plays four: one entry, one of two first branches, one of two second branches, and one finale. Both second branches remain available regardless of the first branch. Every layout is manually solvable without talents. A key's actual exit opens all of its linked barriers; Undo restores those barriers. Switch pieces retain the existing gate XOR rules, including legal exits that can close a gate needed later. Manual gate actions are unavailable.

| Episode | Stage | Layout IDs |
| --- | --- | --- |
| Keys | Entry | `t122_keys_entry` |
| Keys | First branch | `t122_keys_first_key`, `t122_keys_first_relay` |
| Keys | Second branch | `t122_keys_second_key`, `t122_keys_second_relay` |
| Keys | Finale | `t122_keys_finale` |
| Relays | Entry | `t122_relays_entry` |
| Relays | First branch | `t122_relays_first_key`, `t122_relays_first_relay` |
| Relays | Second branch | `t122_relays_second_key`, `t122_relays_second_relay` |
| Relays | Finale | `t122_relays_finale` |

The stable board family is **T122**, version `t122_1`. The engine's numeric fields 901–912 fit its existing budget API. They are not references to campaign U901–U912. `Layout.matches` checks the complete definition, including geometry, barriers, gates, seed and certificate. Load a chapter board through `run.currentLevel()`, never through `RelayCatalog.get(number)`.

`TacticalChapterCatalog` owns explicit tile coordinates, barriers and a complete legal certificate for each layout. Every `Level` construction checks its certificate by linear replay in the real engine. The host suite also compares the twelve geometries against U1–U1000. The catalog returns a fresh `Level`, so modifying a legacy public certificate array cannot corrupt later loads.

## A reserve across four boards

The loan trio grants access to these three existing definitions during the sortie:

| Slot | Companion | Role | Charge | Actual effect |
| --- | --- | --- | --- | --- |
| 0 | Nova | GUIDE | 2 | Show one certified next exit for eight seconds; no move is played |
| 1 | Hoot | REWIND | 2 | Restore the last actual exit, including gates and key barriers |
| 2 | Orbit | FLOW | 3 | Execute up to two exits from a certified continuation; each costs a normal board action |

Each talent is available **once per sortie**, across all four boards and all retries. The initial reserve is **6 Charge**. Neither manual exits nor automatic exits replenish it. Leaving the screen, advancing a stage, using Undo, retrying a layout and cold recreation do not reset used talents or Charge. A retry explicitly resets that layout's board and action budget while retaining the sortie's talent ledger.

Two different used families enable one explicit Resonance: one certified exit, once per sortie, at zero extra Charge. It still costs a normal productive board action. A failed, stale or inapplicable command changes neither the board nor the resource ledger. REWIND without history is inapplicable. No talent bypasses a key or changes a gate outside ordinary engine effects.

Capacities 4, 6 and 8 are explicit experimental options in the pure model. They are not personalized based on a player or spending. At 4, GUIDE plus REWIND is affordable; at 6, FLOW plus either other role is also affordable; at 8, all three costs sum to 7. This demonstrates distinct feasible portfolios, not that 6 is empirically optimal.

The chapter uses focused relay budget policy 6. Blocked taps cost no action. Manual Undo and REWIND have no coin cost, but the productive action spent before Undo is not refunded. There are no shells, session-energy charges, item powers or paid extensions in this isolated session.

## Native integration

`TacticalChapterSession122(Context, GameStore)` owns `luma_tactical122` preferences. It exposes `run()`, read copies from `board()` and `budget()`, `undos()`, `hintId()`, `startEpisode`, `choose`, `move`, `undo`, `useTalent`, `useCombo`, `resetAttempt`, `finishIfSolved`, `save` and `reload`. Drawing code must use these chapter copies, rather than the campaign's active board. A same-episode start resumes an active sortie; switching episodes cannot silently replace one.

Normal moves expose only the engine's immediate mechanical result. The session searches for a complete continuation only after an explicit GUIDE, FLOW or Resonance request. `TacticalChapterAction.prepare` itself performs no search: it validates a supplied continuation by linear replay, clones the board and budget, and returns one complete draft. REWIND requires no continuation. An agent must not receive the content certificate or hidden safety verdicts during ordinary play. Evaluation-only oracles remain separate.

Every accepted state change persists **run, selected layout, actual history, budget policy/counters, Undo count, retry count and active guide cue in one preference transaction**, through `LocalSaveTransactions`. The immutable model records route selection before the board opens. It accepts a victory only for `currentVictoryId()`, the exact selected layout and an actually solved, replayable history. Another valid manual order is accepted; matching the preferred certificate is not required.

Both codecs reject future versions, malformed or noncanonical fields, duplicate events, incoherent counters and invalid histories. A corrupt session restores no partial board, reserve or reward and retains the raw record until an explicit new episode. Failed disk writes do not publish the draft into the live session. A warm retry flushes the pending whole transaction; a true cold process restores the previous durable tuple.

## Fixed completion and crash recovery

A first completion of each episode earns **120 Coins and its local souvenir marker**. Stars and talent use do not change that reward. Replaying that episode does not pay a second milestone. The trio stays loaned: this chapter grants no character ownership, companion copies or home-project contribution.

Wallet and chapter state occupy different preference files. Their settlement is idempotent, not a single cross-file transaction: persist the finished unclaimed sortie; call `GameStore.grantMilestone("tactical122_" + episode, 120)` using the lowercase episode token; then persist `run.claim()` and the episode's souvenir flag together. Cold recreation resumes either an already saved last exit or an interrupted settlement. A wallet receipt already present returns no second reward. Never call the wallet while holding `LocalSaveTransactions.LOCK`; the two-phase implementation avoids lock inversion with another GameStore handle.

The souvenir is an earned local episode marker. This implementation does not silently grant a furniture catalog item or an unrelated home project's progress.

## Verification

```sh
bash scripts/test-tactical-chapter-v122.sh
bash scripts/test-tactical-session-v122.sh
```

The pure suite covers all layouts and route/capacity combinations, distinct campaign geometry, actual key and gate effects, legal traps and repair, alternate legal completions, immutable drafts, exact identity, no recharge, one-use limits, canonical restore and corrupt records. The session suite exercises the real GameStore with durable preference doubles: all branch routes, isolated campaign state, loans, retries, active hints, write failures before and after memory updates, cold recovery, wallet/claim crash windows, parallel store/session handles and corrupt-session refusal. Native rendering, real Android filesystem failures, gestures, accessibility and player acceptance require separate release checks.
