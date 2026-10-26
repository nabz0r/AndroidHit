# Can Luma Drift become a hit?

**The game has a workable product premise. A hit is still unproven.** Version 1.0 shipped a playable puzzle, original art and audio, an earned-currency shop, and substantial offline content. Those are production capabilities. They do not establish that strangers understand the hook, return voluntarily, share it, or can be acquired profitably.

Assessment date: **26 October 2026**. This is a product diagnosis, not a revenue forecast. Actual release checks are recorded separately in [VALIDATION.md](VALIDATION.md).

## The proposition worth testing

> Tap a light to let it escape. Some exits change the gates. One clear path can change every next choice.

The direction-and-gate rule can be demonstrated in a few moves. There is no timer, energy wait, paid solution, or required account. The visual identity and synthesized sound give the mechanic a recognizable setting without a large art budget. Free undo makes experimenting safe. Keep those advantages.

The distinctive moment is **seeing a switch change the future board**. If the early experience feels like generic arrow removal, players may leave before they discover that moment.

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

## What code cannot establish

- **Fun and retention:** solvability tests prove that covered boards can be finished. They do not prove that the difficulty curve is enjoyable.
- **Distribution:** publishing an APK to GitHub does not create Play discovery, a creator audience, or an acquisition channel.
- **Virality:** a share button creates a handoff. Organic growth requires people to use it and recipients to engage.
- **Income:** the current shop uses earned coins. The delivered game has no ads, real-money billing or revenue.
- **Production reach:** emulator checks do not establish comfort on low-end physical phones, audible sound quality, or complete TalkBack usability.

Google's quality guidance treats user value and continuing engagement as essential and warns that adding features can also add clutter. It also distinguishes acquisition from evidence of product value. This supports focusing on comprehension and return before expanding the shop. [Android core value guidance](https://developer.android.com/quality/core-value), checked 26 October 2026.

## A small test that can change the product

### Round 1: observe 20 first-time players

Use a mix of regular puzzle players and occasional mobile players, across the supported languages and several physical devices. Record the exact APK version and source of recruitment. Obtain consent for observation or any exported journal. Do not contact or enroll people automatically.

Give one neutral instruction: “Play this game as you normally would.” Before explaining anything, observe the first three minutes. Then ask the player to explain what a switch does. Note where they stop, why they use undo, whether a gate state is readable, and whether they voluntarily start another board. Test with sound both enabled and disabled. Include a reduced-motion and TalkBack session.

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

**Keep monetization out of the way until the loop survives testing.** If the cohort does not return, more ads or purchasable powers will not repair the underlying problem. If it does return, prototype one clearly optional rewarded-ad placement and a simple ad-removal purchase in a separate test build; measure interruption, consent, technical impact and retention before rollout. This is a future experiment, not a feature in this release.

Any commercial integration needs the actual publisher accounts, SDK terms, regional privacy/consent configuration, billing setup, store declarations and verified behavior. No credentials, payouts, trademark availability or Google Play approval are implied by this repository.

## Evidence to earn next

The next meaningful milestone is **a cohort report with observed comprehension, physical-device quality and D1/D7 results**. A passing build and polished screenshots make that test possible. Only real player behavior and viable distribution can answer whether Luma Drift has hit potential.

Continue with the [roadmap](ROADMAP.md), [store preparation](STORE_LISTING.md) and [release evidence](VALIDATION.md).
