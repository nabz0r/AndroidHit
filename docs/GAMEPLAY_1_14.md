# 1.14 — decisions, teaching and fair failure

The owner reported that Journey15 and many other boards felt impossible. The latest installed1.13 reproduces a concrete accounting failure: five taps on a blocked piece leave all10 pieces and no undo history, but reduce16 available actions to11 for a12-action minimum. Undo cannot refund those rejected taps; Spark then declines to consume stock because the attempt lacks actions. A certificate that clears the starting board did not test that experience. [Native reproduction](validation/v1.13/ui-bugs/ui-sdk-level15-branches-a/README.md).

The owner also rejected unlimited Undo. This release keeps an actual action budget and consequential choices. Making every attempt unconditionally winnable would remove the planning problem.

## Reference mechanisms and Luma decisions

King describes level-specific goals, a visible progress counter, a briefing before play and explicit harder-level warnings in its [official mode guide](https://candycrush.zendesk.com/hc/en-us/articles/360000754897-Which-game-modes-will-I-find). Its [special-candy guide](https://candycrush.zendesk.com/hc/en-us/articles/211939685-Creating-and-combining-Special-Candies) connects deliberate combinations to distinct effects. For Luma the useful design principles are an immediately legible goal, predictable action costs and a visible consequence when a path opens. These sources do not establish an ideal Luma move allowance or a particular Undo quota.

Crash Fever's [official introduction](https://crashfever.com/faq/2022082523946/) directs newcomers through lower-difficulty ability tests that unlock functions, then guide missions. Its [official quest rules](https://crashfever.com/faq/faq_cat/faq6/) distinguish taps, turns, unit skills and a charged Fever action; they also explain victory and defeat. Luma adopts the principles of staged learning and understandable action/effect relationships. Its directional planning remains its own game; this update does not claim to implement Crash Fever combat, its economy or its Fever system.

## Play contract

- A successful piece exit costs one action. Opening a closed directional gate costs one action. A switch changes its displayed side only after its piece exits.
- A refused tap changes neither the board nor the action ledger. It explains the actual blocking side, piece or K seal and highlights that cause. A wrong-direction swipe also does not spend an action.
- One return is available per attempt. It restores the previous position and refunds that action. Its use persists across menus, rotation, backgrounding and a cold process; it remains usable at zero actions if still available. Returning does not generate more returns.
- Zero actions with pieces remaining is a failed attempt. A free restart begins a new attempt. The new failure screen prioritizes restart, the one remaining return when available, and Home; it does not route the player to a new paid continuation offer.
- Before committing a switch or gate opening, its immediate changes and one-action cost are visible. A preview and its cancellation cost nothing. The preview does not certify an optimal choice.
- The game highlights pieces newly made playable by the actual move. It does not automatically remove them or give extra actions for rapid tapping. Thinking time is unrestricted.
- The move limit and stars remain based on exact productive cost plus a fixed authored margin. Time, payments and personal failure history do not secretly alter a board.

## A concrete planning decision

N28 has eight pieces and twelve actions. Its three switches can open the right, bottom and left sides in sequence. Exiting those switches in that order, then clearing the other pieces, costs eight actions. Opening all three sides manually first changes the same switches into closures: reopening the sides adds further costs. The native failure test spends twelve legal actions and still has two pieces left. The one Return refunds one action; it cannot erase the whole inefficient plan. Free restart then permits the eight-action win. [Actual failure, Return and retry](validation/v1.14/ui-bugs/ui-sdk-final30-gallery-rc2-a/prepare/budget-failure-observation.json).

This is the intended consequence: the player can see what a switch changes before confirming, anticipate an order, and learn from a costly choice. Historical L15 instead lost actions on a refused touch without changing the board or producing undo history. That accounting fault is removed. New N15 teaches only one manual gate, with five pieces and a six-action minimum; switches are introduced separately at N17.

## Learning route N1–N1000

The N identity separates the new route from historical L codes. Existing completed-level progress, coins, cosmetics and house remain. An active L attempt keeps its exact old geometry; the ledger upgrades to fair refused taps and a limited correction when it validates. Old shared families continue to resolve their original boards. Very old policy1 attempts finish under their original allowance; restarting begins the current policy. Previously acquired continuations settle exactly once even if the ledger upgrade refunded rejected taps and the attempt is no longer exhausted.

| Boards | Deliberate lesson |
|---|---|
| 1–4 | Move along the large arrow; short unblocked exits |
| 5–8 | Clear a piece blocking a row or column |
| 9–12 | Match a K piece to its sealed cells |
| 13–16 | Touch and open a closed side; no switch or key mixed in |
| 17–20 | Distinguish movement arrow from the small switch target |
| 21–24 | Read two switch effects and choose their order |
| 25–27 | Combine a key with learned switch rules |
| 28–30 | Combine three single-side switches and a key |
| 31–1000 | Revisit the learned rules under increasing density, dependency and planning constraints, with bounded rises and easier recovery boards |

The30 opening boards are explicitly authored, independently explored and unique. The970 later boards keep their audited physical geometry while being reordered under mechanical phase caps. The complete manifest reports size, pieces, switch count, key/seal count, minimum actions, choice penalties and labels for every board. [Authoring rows](validation/v1.14/audit-learning/audit-learning-authoring.csv) · [All-branch costs](validation/v1.14/audit-learning/audit-learning-branches.csv).

These are structural controls, not a measured human difficulty curve. Native execution must additionally check that the rendered controls can play the boards and that failures/retries preserve the rules. First-time player observation is still needed to establish comprehension, enjoyment and actual pass rates.
