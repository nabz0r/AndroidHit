# Can Luma Drift become a hit?

> 1.5 follow-up: the current candidate addresses the old fixed-looking allowance, low planning pressure, missing music, ambiguous ownership and fragmented lobby. It adds a measured 1,000-board route, equipment and six sealed-passage prototypes. [Implemented scope and current evidence](RELEASE_1_5.md). These changes do not establish human comprehension, retention or revenue; the [roadmap study](ROADMAP.md) remains the next product gate.


**The game has a workable product premise. A hit is still unproven.** Version 1.0 shipped a playable puzzle, original art and audio, an earned-currency shop, and substantial offline content. Those are production capabilities. They do not establish that strangers understand the hook, return voluntarily, share it, or can be acquired profitably.

Assessment date: **26 October 2026**. This is a product diagnosis, not a revenue forecast. Actual release checks are recorded separately in [VALIDATION.md](VALIDATION.md).

The **1.5 production candidate** adds the current lobby, equipment and fixed progression to the implemented Google/ad/Billing/wallet foundation. Its Android origin remains empty, sample ads are forced and checkout is blocked. The server and earlier qualification pages have routing/deployment observations; no real Play payment or Android provider qualification follows from them. Earlier Lab/store observations below remain historical. The website/design refresh improves presentation, not the missing real-player retention, acquisition or revenue evidence. [Current scope](RELEASE_1_5.md) · [Validation](VALIDATION.md) · [Web revision boundary](PUBLIC_PAGES.md#15-visual-refresh-and-new-evidence).

## The proposition worth testing

> Tap a light to let it escape. Some exits change the gates. One clear path can change every next choice.

The direction-and-gate rule can be demonstrated in a few moves. There is no timer, energy wait, paid solution, or required account. The visual identity and synthesized sound give the mechanic a recognizable setting without a large art budget. The stable version's free Undo makes experimentation forgiving. Lab 1.3 keeps free restart but gives Undo and blocked taps an action cost, so that change must earn its place in player observation.

The distinctive moment is **seeing a switch change the future board**. If the early experience feels like generic arrow removal, players may leave before they discover that moment.

The Lab's 24-companion collection gives earned coins more visible goals, and the clearer event screen explains what a return session offers. These are product hypotheses, not observed retention gains. The 12,200-coin companion catalog fits within the original campaign's 13,380-coin initial-plus-three-star income before other spending; that does not measure whether players enjoy earning it. Optional Google identity must remain skippable: it adds no gameplay cloud save; the new configured wallet can restore purchased cosmetics and should not become a first-session obstacle. [Collection/event details](COMPANIONS_EVENTS.md) · [Identity limits](GOOGLE_SIGN_IN.md).

Delivered Lab 1.2 has historical Android 16 test-ad/reward, simulated-purchase and collection/event interactions. That closes technical integration questions for the recorded cases; it does not supply the missing player cohort. A +35 test-gold callback is not revenue, and a successful event regression is not evidence that people voluntarily return. [Executed checks](VALIDATION.md).

## Diagnosis and response

| Product question | Evidence in 1.0 | Response in the 1.1 scope | What still needs people |
|---|---|---|---|
| Is the first action obvious? | A five-rule help panel precedes play; initial boards teach movement before gates | Contextual board coaching and guided first moves, with optional full help | Watch first-time players without explaining the rules |
| Is there a clear reason to choose this puzzle? | Gate switches add order-dependent decisions; the first five boards have none | 240 optional Remix boards after campaign board 12, with dual-gate switches; original boards preserved | Test whether players notice, describe and enjoy the rule |
| Is there a satisfying reason to return? | Campaign stars, cosmetics, local daily/weekly boards | Three daily missions, persistent XP/rank and a seven-board cosmetic badge, with no missed-day punishment | Measure voluntary return, not just a completed test script |
| Can a player invite another player? | No challenge handoff | Reproducible C/R/E codes and original board cards through Android's share sheet; friend challenges give no progression gains | Observe whether players choose to share and whether recipients play |
| Can a solo developer learn from a test? | No behavioral record | Optional local play journal, off by default, with explicit JSON export | Recruit consenting testers and analyze results; there is no automatic cohort backend |
| Does the package convey the real game? | Genuine 1.0 screenshots and documentation | Updated screenshots, store copy, release evidence and roadmap | Run store listing experiments once there is enough eligible traffic |

The 1.1 column describes delivered behavior. [CHANGELOG.md](CHANGELOG.md) and [VALIDATION.md](VALIDATION.md) distinguish implementation and executed verification; it does not imply completed market validation.

## What 1.3 changes — and risks

The frozen 480-board order reduces mean adjacent structural score jumps from **12.21 to 4.26** in Journey and **14.45 to 3.57** in Remix. All retained layouts are solvable. These are transparent model measurements, not human difficulty ratings or evidence of a Candy Crush quality curve. Original chapter pools stay intact; Journey 1–6 and Remix 1–3 remain fixed. [Method, remaining spikes and CSV](DIFFICULTY.md).

The action allowance makes blocked attempts and Undo consequential; counting only successful removals would be cosmetic because every solution removes exactly one piece per move. Free restart and a certified solution inside the initial allowance prevent a required purchase, but the new penalty may also discourage useful experimentation or punish imprecise touches. A legal gate-changing move can still leave an unsolvable position. Two optional extensions do not guarantee recovery. Observe those failure cases before tightening any margin.

Six powers and a free claim every five distinct Journey clears add teaching and inventory goals. Their value is a hypothesis. In particular, check whether players understand Vision's legal exits versus Focus's safe exits, and whether the two-page shop or unlocks distract from the first gate puzzle. The ordinary commerce ad still grants 35 test gold. New ad placements grant only the requested continuation or one unlocked power, without adding that gold; the delivered 1.2 +35-gold result does not validate the new routes.

For the next study, record **unassisted clears, blocked attempts, accidental taps, Undos, exhausted attempts, free restarts, optional extension use and refusal**, along with comments and return. Compare a fixed build with the previous forgiving rule before attributing changes to monetization or difficulty. A lower structural score jump can coexist with worse comprehension. Final 1.3 runtime checks and actual player data remain distinct evidence. The [installed run](validation/v1.3/move-power-ui-smoke.txt) used explicit synthetic progression/inventory, restored six preference files, and confirmed two real test-ad rewards without extra gold; that does not measure comprehension or return.

## What code cannot establish

- **Fun and retention:** solvability tests prove that covered boards can be finished. They do not prove that the difficulty curve is enjoyable.
- **Distribution:** publishing an APK to GitHub does not create Play discovery, a creator audience, or an acquisition channel.
- **Virality:** a share button creates a handoff. Organic growth requires people to use it and recipients to engage.
- **Income:** stable 1.1 uses earned coins and has no ads or billing SDK. The separate Lab uses test advertising and simulated packs, with real checkout disabled. Neither establishes revenue.
- **Production reach:** emulator checks do not establish comfort on low-end physical phones, audible sound quality, or complete TalkBack usability.

Google's quality guidance treats user value and continuing engagement as essential and warns that adding features can also add clutter. It also distinguishes acquisition from evidence of product value. This supports focusing on comprehension and return before expanding the shop. [Android core value guidance](https://developer.android.com/quality/core-value), checked 26 October 2026.

## A small test that can change the product

### Round 1: observe 20 first-time players

Use a mix of regular puzzle players and occasional mobile players, across the supported languages and several physical devices. Record the exact APK version and source of recruitment. Obtain consent for observation or any exported journal. Do not contact or enroll people automatically.

Give one neutral instruction: “Play this game as you normally would.” Before explaining anything, observe the first three minutes. Then ask the player to explain what a switch does. Note where they stop, why they use Undo, whether an action was spent as expected, whether a gate state is readable, and whether they voluntarily start another board. Test with sound both enabled and disabled. Include a reduced-motion and TalkBack session.

**Internal decision targets, chosen for this project:** at least 16 of 20 make a legal move within 20 seconds of entering the first board; at least 14 of 20 explain the gate rule after meeting it; at least 14 of 20 finish the first three campaign boards without verbal help. These are working targets, not industry benchmarks or claims of statistical certainty. If a repeated confusion appears in five sessions, fix it before buying traffic.

### Round 2: a fresh cohort of 100–300 players

Recruit people who did not participate in the tutorial test. Keep recruitment channels visible in the results; do not mix paid installs with personal contacts into one unexplained average. Use one stable build, then wait for each participant's observation window to finish.

| Measure | Definition for this study | Internal decision target |
|---|---|---|
| First-session depth | Players completing campaign board 5 / new study players | At least 60% |
| D1 return | New study players who play on the next local calendar day / eligible new study players | At least 30% |
| D7 return | New study players who play on local calendar day 7 after first play / eligible new study players | At least 10% |
| Unprompted sharing | Players who choose the challenge share flow without a researcher asking | Record the proportion and recipient outcome; no fabricated viral coefficient |
| Friction | Stuck states, repeated failed taps, abandoned boards and explicit player comments | Identify and remove the most frequent avoidable cause |

The D1/D7 thresholds are **internal go/no-go hypotheses**, not sourced market medians. Report numerators, denominators, missing participants, recruitment channel, build and uncertainty. At 100 players, 30 D1 returns is 30% with a rough 95% interval of 22–40%; a small cohort is a signal, not proof of a market.

The local journal is a diagnostic aid. It does not upload data, identify a global cohort, deduplicate reinstalls across phones, or prove that a non-exporting player churned. For retention, define the enrolled denominator and follow-up method before the study. Report export-only participation separately; export volunteers are a selected sample. Timezone changes, clock changes and missing exports can distort local-day comparisons.

## Monetization decision

**Protect the puzzle while preparing monetization in the separate Lab.** The owner requested test mode, so the implementation uses optional Google test advertising and clearly labelled gold-pack simulations. It is preparation for a later experiment, not a live revenue launch. Measure interruption, consent handling, reward correctness and retention separately. If players do not return, more monetization controls will not repair the underlying problem.

The Lab's action margins, six-power unlocks and item-price curve are balancing hypotheses, not formulas proven to create hits. It uses distinct campaign + Remix clears only, with thresholds and sample prices documented in [gameplay](GAMEPLAY.md#lab-item-prices). It does not react to failures, ad refusal or spending. Test free-player affordability at each band before considering a production economy. The stable 1.1 shop retains its original fixed prices.

Owner public configuration and the wallet implementation/deployment are now present, but actual provider access, regional consent, billing lifecycle and store declarations remain qualification work. No-charge simulations do not establish those outcomes. Ad callbacks with currency/micros/precision from a test opportunity must be excluded from revenue reporting. No credentials, payouts, trademark availability or Google Play approval are implied by this repository. [Official-source research](MONETIZATION_RESEARCH.md).

## Evidence to earn next

The next meaningful milestone is **a cohort report with observed comprehension, physical-device quality and D1/D7 results**. A passing build and polished screenshots make that test possible. Only real player behavior and viable distribution can answer whether Luma Drift has hit potential.

Continue with the [roadmap](ROADMAP.md), [store preparation](STORE_LISTING.md) and [release evidence](VALIDATION.md).
