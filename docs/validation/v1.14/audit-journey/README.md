# Frozen 1.13 Journey: branch and budget audit

This audits the preserved **L1–L1000 V4** corpus independently of its stored action certificates. It does not measure players, understanding, fun or a success probability. Existing levels remain unchanged.

Run `JAVA_HOME=… sh scripts/audit-journey-v114.sh`. The oracle reconstructs ray obstructions and key/seal dependencies directly from immutable geometry. It evaluates all spatial subsets and all sixteen gate masks in bottom-up order. For each state it computes an exact minimum and three exact maximum completion lengths:

1. **All legal actions:** every legal piece exit and every closed-gate opening allowed by the runtime.
2. **Immediately useful gates:** legal exits plus a gate opening only when that gate immediately enables a spatially free piece to exit.
3. **Exits before gates:** use a legal exit whenever one exists; otherwise open an immediately useful gate.

These policies bound possible paths; none is a model of human behaviour. Blocked taps, Undo, powers and extensions are excluded. Gate openings cannot repeat without an intervening switch exit, so this action graph is finite.

## Executed result

[Summary](audit-summary.json): **1,000 boards and 24,966,384 independent reference gate states**. The exact initial minimum agrees with the published minimum on every board.

- 922 boards have at least one entirely legal action sequence whose length exceeds its original budget.
- 859 still have such a sequence when gates must immediately enable an exit.
- 628 still have such a sequence under the exits-before-gates restriction.

These are counts of content properties, **not failure rates and not 922 bugs**. A puzzle can intentionally charge for inefficient productive choices. The design issue is whether the consequence is legible, taught and introduced progressively.

[All rows](audit-all-branches.csv) retain exact policy bounds. [Witnesses](audit-budget-witnesses.json) give an executable longest-policy prefix until the remaining budget is below the independent minimum, or a complete non-deficient path. A witness is not claimed to be the earliest or shortest way to fail.

## Level 15 and the reported impossibility

The recorded 1.13 L15 identity is `LD1-L-F-8202A432`: 5×5, ten pieces, one switch, no key/seal, exact minimum twelve actions and original budget sixteen. **Every legal path completes in twelve actions** under all three policies. Thus this particular board has no productive-order budget trap. This does not invalidate the user's report: a refused tap charged an action in the old UI, manual gate opening can be missed, another preserved family can display the same level number, and a retained attempt can have an already-spent budget.

The native 1.13 SDK replay separately completed this L15 in twelve actions and three stars. It followed a known solution and therefore establishes touch/execution for that sequence, not first-time comprehension. The next implementation should preserve the old identity while making the active family, remaining budget, blocked cause and manual gate action explicit.

## Concrete later counterexample

For L24, the immediately-useful-gates witness is:

```text
65,4,7,8,9,67,0,1,67,5,66,6,67,2,64,3
```

After these sixteen legal actions, three more actions are necessary but only two remain. The cost is caused by legitimate gate/switch order, with no blocked tap or power involved. The original L23/L24 structural scores of85/84 reflect alternative costs along one reference optimal path; they are neither human difficulty measurements nor counts of all possible decisions.

The separate N corpus introduces each rule in isolation before combined gate choices. Its exact content, retained choice depth and phase caps are recorded in [the N content audit](../audit-learning/README.md).
