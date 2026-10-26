# Move limits and optional continuation — research

**Checked 26 October 2026 · Primary game/Google sources · Historical proposal and design rationale**

This note records the research before the 1.3 implementation. The implemented 1.3 contract is now documented in [gameplay](GAMEPLAY.md#lab-13-action-allowance), with [board measurements](DIFFICULTY.md); its [final runtime/package evidence](VALIDATION.md) remains separate from the research.

The preserved Lab **1.2.0-sandbox** has no action-budget failure system. At the research stage, the 1.3 formula and recovery contract were not yet frozen. The proposal below is retained as that decision record; the delivered rules are documented in gameplay. Nothing here retroactively changes the 1.2 APK, screenshots or validation.

## What the referenced games establish

| Official source | Observed rule | What it does not establish |
|---|---|---|
| Rovio, [Angry Birds Match: how to play](https://roviosupport.helpshift.com/hc/en/37-angry-birds-match/faq/3028-how-do-i-play-angry-birds-match/) | A visible move allowance; complete the targets before it runs out | One rule shared by every Angry Birds game |
| Rovio, [Dream Blast: spending coins](https://roviosupport.helpshift.com/hc/en/4-angry-birds-dream-blast/faq/3058-what-should-i-spend-my-coins-on/) | Coins can buy extra moves after running out | A universal number of extra moves, price, or ad offer |
| Dream Games, [Royal Match: playing levels](https://dreamgames.helpshift.com/hc/en/3-royal-match/faq/4-how-can-i-play-the-levels/) | A move count is shown; failure precedes retry; coins can buy additional moves | That this mechanic by itself improves this project's retention or income |
| WonderPlanet, [Crash Fever quest FAQ](https://crashfever.com/faq/faq_cat/faq6/) | HP reaching zero causes defeat; a Polygon can continue eligible quests, restoring HP and other battle resources | A match-puzzle move allowance or proof of an ad-funded continuation |

“Angry Birds” is a franchise, not a single ruleset. The two Rovio puzzle titles above give a more precise comparison than treating the slingshot games as the same genre. Crash Fever's official Japanese FAQ describes a battle revival, rather than Luma Drift's one-piece-per-exit puzzle. The Crash Fever summary here is an English paraphrase of that Japanese source. Its [currency FAQ](https://crashfever.com/faq/) also identifies battle continuation as a Polygon use and lists both earned and purchase routes for Polygons.

These examples support a recognizable **visible allowance → exhausted state → explicit retry/continuation choice**. They do not supply an industry-standard formula, conversion rate, fair price or guarantee of success. We are not copying their screens, art, names, level data or proprietary balancing.

## Google requirements relevant to the offer

Google requires clear disclosure of the action and promised reward before a rewarded ad, an affirmative opt-in, the ability to dismiss/decline, and delivery when the required action is completed. Declining must not impede normal app use. Rewards must stay within the allowed non-monetary/non-transferable scope; the publisher remains responsible for granting them. [Rewarded-ad policy](https://support.google.com/admanager/answer/7496282?hl=en).

For this project, an always-available free restart and ordinary puzzle access are deliberate safeguards around an optional continuation. They are project design decisions, not a claim that these controls alone certify compliance.

The Next-Gen SDK documents an earned-reward callback separately from impression, dismissal and failure callbacks. Tests must use test ads. It also documents optional server-side verification; that is a separate service, not something a local receipt or callback implements automatically. [Rewarded SDK guide](https://developers.google.com/admob/android/next-gen/rewarded). UMP's `canRequestAds()` must be checked before requests, with current consent information. [UMP integration](https://developers.google.com/admob/android/privacy).

The Lab should retain its explicit adult declaration, advertising opt-in and UMP gate. Restart must still work with ads refused, no fill, no connectivity, unknown/minor age or missing account configuration. Google sign-in must remain unrelated to ad permission.

## Why a successful-removal counter would be cosmetic here

Luma Drift starts with `N` pieces. Every successful ordinary move removes exactly one piece. A no-undo solution therefore always contains exactly **N successful removals**, regardless of order. A budget that counts only those removals cannot distinguish an efficient legal solution from another legal solution; setting it below N would make an unassisted clear impossible.

To make the requested budget meaningful while preserving the existing mechanic, count committed actions:

| Action | Proposed allowance cost | Reason |
|---|---:|---|
| Tap a present piece, whether it exits or is blocked | 1 | A committed attempt is the player's decision |
| Undo that actually restores a removed piece | 1 | Recovery is useful but remains a deliberate action |
| Undo with no history | 0 | No board action occurred |
| Tap empty space, touch preview, keyboard/TalkBack focus | 0 | Browsing or an imprecise empty tap is not a puzzle decision |
| Help, pause, settings, background time | 0 | No thinking-time or accessibility penalty |
| Spark or Vision | 0 extra budget cost | They reveal information; their existing inventory/star rules still apply |
| Each actual Flow removal | 1 | It is a board move; do not charge both the button and the removals |
| Restart | Free; reset the attempt allowance | Keeps every board replayable without payment or advertising |

If `b` blocked attempts and `u` successful undos occur before a clear, successful removals total `N + u`, so this model spends **N + b + 2u** actions. Undo costs once immediately and the restored piece costs another action when removed again. Explain this in the UI before it can exhaust a run; “undo is free” would no longer accurately describe the move allowance, even if it still costs no currency.

This is an audit of the existing engine, not evidence that penalizing mistakes improves enjoyment. First-time comprehension and accessibility testing are especially important when blocked taps change from free feedback to consumed allowance.

## Proposed contract to settle before coding

1. **Base allowance:** use N plus a visible, non-negative error allowance. Validate a known complete solution of every published board within the initial allowance, without items, gold or ads. Tutorial generosity and the exact formula remain decisions to freeze in the implementation contract.
2. **Exhaustion:** when the last action clears the board, victory wins over the exhausted panel. Otherwise pause board actions and queued Flow work. Preserve the exact board/gates/history; do not secretly restart or randomize it.
3. **Choices:** show free Restart prominently, optional `+K moves for G gold`, and an optional ad offer stating exactly `+K moves after the earned reward`. State any per-attempt limit before acceptance. Do not introduce a timer, fake scarcity, surprise price escalation or a mandatory paid/ad route.
4. **Gold:** charge and add allowance in one durable local transaction tied to the current attempt. Reject insufficient balance and duplicate requests without partial debit. This remains local sandbox gold, not an authoritative purchased-wallet service.
5. **Ad:** use the normal consent gates and an explicit placement-specific reward. Grant only once on the earned callback, not on load, impression, close or failure. Bind it to the offer, board and attempt; no delayed callback can credit a different board after restart/navigation. A continuation offer must not silently become the shop's existing 35-gold reward or grant both rewards by accident.
6. **Persistence:** store allowance spent/granted, attempt identity and continuation claim state separately from `Board.history()`. History alone loses blocked attempts and undone actions. Process restoration must not refill the allowance or repeat a grant. Starting a fresh restart creates a new attempt.
7. **Dead ends:** an order can leave the gates unsolvable even with allowance remaining. Extra moves do not automatically repair that state. Explain recovery through undo/restart; do not promise a win from buying or watching. Do not consume a power or sell an automatic solution that the exact solver cannot find.
8. **Fair sharing:** freeze whether friend practice uses the same allowance or remains unrestricted. Codes identify a board, not a paid entitlement, balance, move budget or competitive record. Keep friend-mode progression/power restrictions explicit.

For illustration only, N = 12 and an allowance of 18 permits a clean 12-action solution plus six blocked attempts, or three successful undo/re-remove pairs. **18 is not a committed formula.** K, G, per-attempt continuation limits, tutorial treatment and star treatment must be fixed before implementation and then documented as project choices.

## Acceptance evidence needed

- Every campaign/Remix solution and covered event seed clears within the initial budget for its mode; all published content remains free to complete.
- Correct cost for legal/blocked attempts, successful/no-op undo, Flow and empty/focus actions; victory at exactly zero is still a win.
- Exhaustion stops queued actions; pause/help/background never spend allowance; rapid taps cannot create duplicate spends or grants.
- Restart is free at every relevant state, including no network, refused ads, insufficient gold and unavailable SDK services.
- Gold debit/grant is atomic and idempotent; persistence/relaunch preserves exhausted or extended state without refilling or double-credit.
- Test-ad reward, dismissal, failure, refusal, duplicate callback and stale-attempt behavior are distinguished. No fabricated SDK success or live income is claimed.
- Six-language rule text, readable remaining allowance, accessible control labels, honest offer amounts and target identity are checked in the installed final APK.

Run a small player test before judging the economy. Measure comprehension, avoidable mis-taps, restarts, unassisted completion and voluntary return alongside continuation use. More exhausted runs or ad starts can represent added friction rather than a better game. There is no player or revenue result for this proposal yet.
