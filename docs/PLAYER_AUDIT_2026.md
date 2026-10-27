# New-player audit — tutorial to Journey 100

The audit found three concrete problems beyond mathematical solvability: a tutorial gesture that could cancel itself, earned gifts that were announced on a route where they could not be claimed, and help that the game encouraged but then penalised in its star rating. Version 1.9 repairs those paths, offers an explicit first sample of each unlocked power, and adds optional encounters and free visual treatments. It **preserves the published L/M/T puzzles**. It does not establish a calibrated human difficulty curve or predict a hit.

## What the 2026 data actually says

The [source registry](research/2026-new-player/sources.json) contains six primary publications, their populations, sample sizes, collection dates, verified aggregates and access limits. “Published in 2026” does not always mean “collected in 2026”.

| Primary source | Publication | Fieldwork and population | Use in this audit |
| --- | --- | --- | --- |
| [ESA / YouGov](https://www.theesa.com/two-thirds-of-americans-play-video-games-every-week-according-tonew-report-from-the-entertainment-software-association/) | 3 June 2026 | February 2026; 13,545 US respondents; household participation includes children | Mobile/puzzle coverage; the [separate motivation chart](https://www.theesa.com/resources/essential-facts-about-the-us-video-game-industry/2026-data/) explicitly limits its base to players aged 18+ |
| [AARP / NORC](https://www.aarp.org/pri/topics/technology/internet-media-devices/play-purpose-video-games-older-adults/) | 22 October 2026 | US players aged 50+; 1,014 quantitative respondents; March qualitative interviews and May/June quantitative dates | Solo, relaxation and session diversity; not a skill estimate |
| [Deloitte Digital Media Trends](https://www.deloitte.com/us/en/insights/industry/technology/digital-media-trends-consumption-habits-survey.html?go_to_origin=true) | 25 March 2026 | October–November **2025**; 3,575 US consumers | Community and discovery contexts; cross-media fandom, not puzzle performance |
| [Ofcom quantitative tracker](https://www.ofcom.org.uk/siteassets/resources/documents/research-and-data/media-literacy-research/adults/adults-media-use-and-attitudes-2026/adults-media-use-and-attitudes-2026-report.pdf?v=415430) | 2 April 2026 | September–November **2025**; 7,533 UK respondents aged 16+ | Task confidence and actual ability must be tested separately |
| [Ofcom Media Lives](https://www.ofcom.org.uk/siteassets/resources/documents/research-and-data/media-literacy-research/adults/adults-media-use-and-attitudes-2026/adults-media-lives-2026-report.pdf?v=415484) | April 2026 | October **2025**; 20 longitudinal UK participants | Illustrative context about interrupted schedules and social play, not population percentages |
| [MILDECA / Toluna Harris Interactive](https://www.drogues.gouv.fr/barometre-ecrans-mildeca-harris-interactive-2026-les-depenses-dans-les-jeux-video-un-enjeu-de) | 27 July 2026 | June 2026; 2,158 French respondents aged 15–75 | Audit clear prices, optional spending and voluntary exits; associations do not establish causation |

For adult Gen Z, ESA's explicitly 18+ motivation chart reports 60% playing to pass time/relax and 70% to have fun; the HTML does not give that subgroup's sample size. AARP reports that 75% of its 50+ players play to relax, and 65% describe typical sessions of 15–60 minutes. Those observations concern different samples and questions: they neither rank generations nor define a first-session target for Luma Drift. [ESA adult chart](https://www.theesa.com/resources/essential-facts-about-the-us-video-game-industry/2026-data/), [AARP research summary](https://www.aarp.org/pri/topics/technology/internet-media-devices/play-purpose-video-games-older-adults/).

The overall ESA Gen Z participation category and Deloitte's Gen Z category include minors; Ofcom's 16+ sample does too. They cannot be relabelled adult-only findings. The studies are US, UK and French evidence, not worldwide Android measurements. Media-literacy performance is not puzzle skill, and a stated relaxation motive is not a clinical benefit.

The minimal collection retained aggregate facts and short source locators only. No social accounts, posts, participant biographies, advertising identifiers or respondent microdata were collected. The AARP origin returned HTTP 403; readable official indexed excerpts supplied its summaries. The research and press summaries differ about a May fieldwork wave, so both descriptions remain recorded. [Retrieval hashes](research/2026-new-player/retrieval-manifest.json) identify **our extraction files**, not raw origin responses or downloaded full reports.

## Six behaviours, not six demographic stereotypes

[profile-anchors.json](research/2026-new-player/profile-anchors.json) declares the inputs and sensitivity ranges. Research motivates coverage dimensions; it does not estimate the numerical values below.

| Synthetic profile | Main test context | Assumed off-plan / blocked-probe rates |
| --- | --- | --- |
| Practising novice | Completes and reopens practice; asks for visible help | 20% / 12% |
| Impatient skipper | Skips practice, explores, then resumes learning | 30% / 18% |
| Careful explorer | Reads and previews before committing | 10% / 4% |
| Interrupted commuter | Short modelled windows and cold reconstruction | 15% / 8% |
| Independent planner | Efficient puzzle play and structural variety | 2% / 1% |
| Social collector | Checks owned items between level bursts | 15% / 8% |

An off-plan action is an available legal action outside the reference plan, not a measured human mistake. Each behaviour can be crossed with an **adult Gen Z 18+** or **older-adult 50+** reporting label using identical parameters. The labels do not change mistakes, prices, help, spending, retry rules or difficulty. Duplicating an age label would not produce independent evidence, so no demographic leaderboard is reported.

## Measured game behaviour and corrections

| Finding | Evidence | Resulting behaviour |
| --- | --- | --- |
| The full optional practice contains **27 real actions**, not just seven Next taps | [Tutorial phase trace](validation/v1.9/journey/candidate/tutorial-steps.csv) | Keep Later, saved-step resume and replay; one completion gift, no campaign stock spent |
| Lesson 4's coach illustrated a 15-point drift that the old scroll recogniser could cancel on a compact screen | [Reproduced native baseline defect](validation/v1.9/ui-baseline-lesson4/README.md) | Reserve a typed gate target, show a stationary tap coach and an explicit preview/open button; native touch regression remains separate from JVM policy tests |
| Home's ready-gift route opened Rewards, while actual collection lived deep in Shop | [Reward baseline](validation/v1.9/rewards/BASELINE.md) | Show the next named Journey gift and its real claim control first on Rewards, with direct home/victory entry |
| Claiming five-clear gifts later could change their contents | [Reward implementation](validation/v1.9/rewards/IMPLEMENTATION.md) | Freeze new gift eligibility to the milestone; preserve old receipts and exact retries |
| Shield, Focus and Trail could unlock before a first free copy arrived | [Actual before/after reward ledger](validation/v1.9/journey/candidate/actual-reward-ledger.csv) | One optional, explicitly claimed free charge per power at its unchanged unlock; full stock preserves the claim |
| A hint encouraged during learning could remove the third star | [Paired grading comparison](validation/v1.9/journey/comparison.json) | Stars use actions and extension history; hint count, elapsed time and raw Undo count are excluded |
| L23 has a structural score jump from **17 to 85** | [All 100 level measurements](validation/v1.9/journey/candidate/levels-1-100.csv) | Preserve its geometry; explain the action target, preview consequences, expose replayable practice and free recovery; offer a separate optional encounter rather than claiming that the jump disappeared |

The structural score counts authored dependencies and gate choices. It is not a probability of failure, and other adjacent jumps remain. This release improves explanation, recovery, reward delivery and variety around that content. It does not silently rewrite published challenge identities or advertise a human-calibrated Candy Crush curve.

## Paired simulation results

The same harness ran the archived **39908e2** source and the candidate, with real Board, MoveBudget, TutorialSession, GameStore and MetaProgress APIs and in-memory Android preference adapters. Input hashes and all attempts are retained in the [journey evidence index](validation/v1.9/journey/README.md).

- [Baseline](validation/v1.9/journey/baseline-39908e2/summary.json) and [candidate](validation/v1.9/journey/candidate/summary.json) each exercised **16,000 independent attempts** and **72 sequential journeys** across six behaviours, three sensitivity settings and four seeds.
- The [comparison](validation/v1.9/journey/comparison.json) confirms identical first-100 geometry, budgets, structural metrics and paired independent action outcomes. The fixes are not represented as a new completion curve for unchanged boards.
- All 72 sequential candidate journeys reached level 100 under their declared help/recovery rules, with **7,216 level attempts** and **814 restored synthetic interruptions**. Some runs used earned coins to buy hint packs; none used advertisements, LumaGold or real-money purchases.
- In the independent controls, random legal choices completed 1,583/1,600 attempts and the one-step physical heuristic 1,589/1,600. Exploratory probing is sensitive to the assumed rate: 1,494, 1,171 and 646 completions at 5%, 15% and 30% blocked-probe settings. These are scripts, not surveyed players. The explicit guided recovery control completed 1,600/1,600.

The collector's menu reads check ownership only: no real friends, Google accounts or backend gifts are simulated. The injected calendar advances every ten clears for reward-rollover coverage; that is a test schedule, not observed return behaviour. Latency intervals and session durations are **modelled action time**, not measured human playtime.

The new optional guardians have their own [100-encounter oracle](validation/v1.9/journey/guardians/summary.json) and [reward/store contracts](validation/v1.9/journey/guardian-store/summary.json). Their outcomes are not mixed into the unchanged L/M puzzle comparison. Native touch, compact layouts, final APK startup and rendered atlas evidence belong to the separate release qualification.

## What still requires people

No D1/D7, conversion, churn, satisfaction, cognitive improvement or desire-to-continue rate is estimated. A follow-up playtest should recruit consenting adults with varied puzzle experience, available time and accessibility needs; observe the first gate explanation, L21–30, a power claim, and a voluntary pause/return. It should ask whether choices and rewards were understood and whether continuing felt appealing, while allowing departure without pressure. Simulated completion cannot answer those questions.
