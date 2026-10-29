# Companion team gameplay contract — 1.19

This document specifies the native Java model `CompanionBattle`. Integration, rendered Android behavior, saved-board effects and physical-device feel require separate evidence. A browser concept is not an Android delivery.

## Team and ownership

One to three distinct existing catalogue IDs form a board's team. A new guest may start with Nova only. `State.start` validates ID existence, team size and duplicates; the caller must validate actual ownership before starting or restoring a team. This model never grants an avatar, changes ownership, reads a clock or touches either currency.

All 37 current identities have a definition containing their own ID, signature key and catalogue color. There are **six effect families**, with bounded parameter variants. This is not a claim of 37 different rule engines, unique animations or unique rendered effects.

| Family | Board effect contract | Amount | Duration | Energy |
| --- | --- | --- | --- | --- |
| GUIDE | Show one solver-confirmed next action | 1 | 8 s | 2 |
| VISION | Highlight currently legal exits; legality does not itself prove a winning continuation | 1 highlight session | 5–7 s | 2 |
| TRAIL | Show a certified safe continuation | 2–4 actions | 8 s | 2; 3 for four actions |
| PULSE | Execute one certified safe piece exit | 1 | Immediate | 2 |
| FLOW | Execute a certified sequence of piece exits | 2–3 | Immediate | 3 |
| REWIND | Restore saved move history | 1–2 undo actions | Immediate | 2; 3 for two undos |

Nova uses GUIDE (1 action, 8 s, 2 energy); Miko uses TRAIL (3 actions, 8 s, 2 energy); Orbit uses FLOW (2 exits, 3 energy). `CompanionBattle.find(id)` is the complete parameter registry for other identities. Parameters are not derived from rarity, spending or time.

## Shared energy and combo

Each board starts at **3 energy**, capped at **6**. A successfully exited piece ID earns one energy exactly once for that board. Valid piece IDs are 0–63; manual gate actions 64–67 earn nothing. Rewinding and exiting the same piece again earns nothing. A receipt is recorded even at the energy cap, so overflow cannot be harvested after spending.

Each team member can use its talent once per board. Using two talents from different families makes one combo ready. The combo executes one additional certified piece exit only after an explicit player command; it costs no extra energy. Once spent it cannot recharge in that board, including after a third family's talent. Normal successful combo exits may receive the ordinary once-per-piece energy credit.

## Caller and persistence boundary

`State` is immutable. `use`, `useCombo`, and `creditExit` return a new state; a refused transition returns `null`, except a repeated valid exit receipt returns the same unchanged instance. Old objects remain unchanged. `canUse` checks only the per-run slot, charge and energy contract; it does **not** assert that a board effect is possible.

Before spending a charge, the Android caller must establish that the intended effect can execute: a certified solution for hint/trail/exit effects, legal pieces for VISION, and enough valid saved history for REWIND. Information highlights must not describe every legal move as safe. A multi-action talent must use a valid certified prefix; it must not silently claim more exits than it executes. Whether a shorter prefix is allowed must be communicated by the UI and validated by the caller.

Apply the board effect and corresponding companion transition within the saved session's existing transactional/rollback boundary. Do not grant a charge when a save fails; do not debit an unavailable effect. Asynchronous UI requests may pass the exact previous `encode()` into `use(slot, expectedEncodedState)` or `useCombo(expectedEncodedState)`; a stale identity is refused without consumption. A repeated request against the same immutable predecessor returns the same candidate state, so the caller must commit the accepted successor and never apply the same board command twice.

The versioned canonical codec records team IDs, energy, used slots, credited 64-bit piece mask, last family, combo flags and a bounded event sequence. Decode replays the sequence from the initial contract and requires byte-exact equality of all encoded fields. Unknown versions, fabricated fields, duplicate use/credit events, impossible costs, invalid IDs, noncanonical numbers and oversized records are rejected. The event sequence contains no telemetry or wall-clock data. Decode is integrity validation for accidental corruption, **not cryptographic authentication** against a device owner editing local data.

The model does not change the certified puzzle generator, movement rules, budgets, inventory powers, prices, ads, purchases or server wallet. Existing expedition skills remain a separate contract until explicitly integrated.

## Reproducible model checks

Run `bash scripts/test-companion-battle-v119.sh` using a JDK. The suite checks all identity definitions, one/two/three-member teams, immutable inputs and outputs, rejected/stale/repeated commands, all 64 credit bits, cap overflow, undo/re-exit farming, every ordered pair of distinct characters, once-only combo behavior, strict corrupt-record rejection and deterministic randomized transition/restore sequences. These are model checks, not native UI or physical-device acceptance.
