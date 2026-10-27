# Guardians of the Prism — 1.9

Nova restores a Prism by meeting its guardians. One optional encounter opens after every ten **distinct completed Journey levels**: Guardian 1 after ten, Guardian 5 after fifty, through Guardian 100 after one thousand. They use one active guardian save slot, separate from the Journey attempt. Opening a different guardian replaces that active attempt; unlocks and claimed receipts remain. They do not lock the ordinary Journey, replace published L/M/T layouts or count as Journey/quest wins.

Home links the guardian list; a victory offers the next available guardian. The list preserves all unlocked meetings, including missed earlier ones. Encounter names cycle through twelve themes; the actual scenery uses the player's selected Avatar Studio treatment. That setting and the actual work meter support the story; there is no opponent countdown, fabricated near miss or health unrelated to game state.

## Two new visible rules

**Layers:** a thick piece shows separate slabs and a `×2` badge. The first successful gesture peels one layer and costs one action. Once one layer remains, the next gesture moves/exits it according to its arrow. A piece can be peeled before its path is clear; it still needs a free path to move afterward.

**One cell at a time:** introduced at Guardian 5. A footprint-marked piece moves exactly one cell per gesture, then exits on the next step over the edge. Its next cell must be free. Ordinary thin pieces continue to exit along a clear arrow path in one action. A wrong-direction swipe plays no action; tapping is an equivalent control.

Each encounter has four to eight pieces and ordered row dependencies. Its exact initial minimum equals the sum of real remaining layers and necessary movement/exit work. The fixed budget adds a published margin. A successful action reduces that work by one; a genuinely blocked piece attempt spends one action without reducing work. Empty/missing targets spend nothing.

## Recovery, saving and rewards

Undo restores the previous productive piece state and refunds one action, even at zero. It cannot erase earlier blocked attempts. Free restart restores the original encounter and allowance. The free next-gesture hint highlights an actual legal action; it uses no ordinary inventory. The boss mode has no ads, premium debit, continuation checkout, campaign power consumption or thinking timer.

The attempt records its repeated piece-ID history and spent budget in a versioned bounded save. Restore reconstructs and validates each action. A solved but unclaimed encounter remains solved when reopened while it is the retained active guardian; an already collected encounter can be replayed for practice. A failed save blocks further play behind Retry. Only visible piece targets own a touch; hidden pieces cannot intercept Retry. Back/list navigation waits for a successful save, so opening another guardian cannot replace a failed pending attempt.

The first solved encounter grants **100 local earned LumaCoins** on explicit collection. Currency and the per-guardian receipt commit atomically. Repeated taps, a second store handle, replay, process recreation and concurrent claims cannot grant another reward. A full coin balance preserves the unclaimed reward. A typed local-save failure keeps a full-screen retry boundary during rendering and activity resume, including repeated commit failures; retry flushes the same receipt rather than replaying its grant. [Repeated-failure contracts](validation/v1.9/local-save/README.md) and native fault runs have separate evidence. This local caller contract is not a server purchase or anti-cheat service.

## Executed evidence

The [independent guardian oracle](validation/v1.9/journey/guardians/summary.json) covers all 100 unique patterns, 6,197,718 reachable productive states and 34,045,573 productive transitions, with 472,484,607 assertions. It independently checks coordinate/layer movement, exact remaining work, all productive transitions, sampled plans, blocked budgets, zero-action recovery, repeated-ID saves and malformed input. These are engine assertions, not numbers of players.

The [store integration](validation/v1.9/journey/guardian-store/README.md) solves all 100 real encounters before reward requests and exercises persistence failures, full wallets, concurrency, styles and languages. Native installed acceptance has its separate [release index](validation/v1.9/README.md); source oracles do not prove touch comfort, physical-device performance or human difficulty.

[New-player audit](PLAYER_AUDIT_2026.md) · [UX](UX_1_9.md) · [Release](RELEASE_1_9.md).
