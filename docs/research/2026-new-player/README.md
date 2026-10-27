# 2026 evidence for the first-player audit

Retrieved **27 October 2026**. This folder records six primary publications issued in 2026, the dates of their underlying fieldwork, and the limits of their use in Luma Drift. The results are aggregate survey evidence, not Luma Drift telemetry or measurements of its players.

## Evidence and scope

| ID | Primary publication | Geography and scope | Publication / collection |
| --- | --- | --- | --- |
| ESA26 | [ESA / YouGov — Essential Facts](https://www.theesa.com/two-thirds-of-americans-play-video-games-every-week-according-tonew-report-from-the-entertainment-software-association/) | US game participation and platforms; includes children | 3 June 2026 / February 2026 |
| AARP26 | [AARP — Play on Purpose](https://www.aarp.org/pri/topics/technology/internet-media-devices/play-purpose-video-games-older-adults/) | US players aged 50+, motivations and sessions | 22 October 2026 / March, May and June 2026 |
| DMT26 | [Deloitte — Digital Media Trends](https://www.deloitte.com/us/en/insights/industry/technology/digital-media-trends-consumption-habits-survey.html?go_to_origin=true) | US media fandom, discovery and social activity; not puzzle retention | 25 March 2026 / October–November **2025** |
| OFQ26 | [Ofcom — Adults’ Media Use and Attitudes](https://www.ofcom.org.uk/siteassets/resources/documents/research-and-data/media-literacy-research/adults/adults-media-use-and-attitudes-2026/adults-media-use-and-attitudes-2026-report.pdf?v=415430) | UK digital participation and task confidence; ages 16+ | 2 April 2026 / September–November **2025** |
| OFL26 | [Ofcom — Adults’ Media Lives](https://www.ofcom.org.uk/siteassets/resources/documents/research-and-data/media-literacy-research/adults/adults-media-use-and-attitudes-2026/adults-media-lives-2026-report.pdf?v=415484) | UK qualitative longitudinal context; not prevalence estimates | April 2026 / October **2025** |
| MIL26 | [MILDECA / Toluna Harris Interactive — Baromètre Écrans](https://www.drogues.gouv.fr/barometre-ecrans-mildeca-harris-interactive-2026-les-depenses-dans-les-jeux-video-un-enjeu-de) | France, self-reported practices and expenditure; ages 15–75 | 27 July 2026 / June 2026 |

Precise populations, sample sizes, verified facts, access limitations and locators are in [sources.json](sources.json). The minimal machine-readable extraction is [aggregate-observations.json](aggregate-observations.json); its hash and retrieval record are in [retrieval-manifest.json](retrieval-manifest.json). No person, social account, comment, participant biography or advertising identifier was scraped or retained. No protected full report was copied into the repository. Publicly readable sources do not automatically have an open redistribution licence.

The AARP origin returned HTTP 403 to direct reading. Its official indexed research and press-release excerpts were readable through the research search tool. This narrower access is recorded explicitly; no claim is made that the underlying questionnaire, microdata or full report was downloaded. The two AARP summaries describe the collection dates differently, so both descriptions are retained rather than silently reconciled.

## How evidence informs the audit

The evidence supports testing several different ways to arrive, learn, pause, recover and enjoy a game. It does **not** provide a measured error rate for our arrows, a desired first-session length, a willingness to pay after failure, or a Luma Drift completion or retention curve. The numerical simulation inputs are therefore engineering stress assumptions, with references explaining their relevance rather than pretending to estimate their values.

[profile-anchors.json](profile-anchors.json) defines six behavioural profiles. None is an age bucket: a knowledgeable older player can use the expert profile; a digitally confident young adult can use the puzzle-novice profile. Adult Gen Z (18+) and older-adult (50+) labels may be crossed with **every** profile for reporting, with identical mechanics and parameters. Such duplicate labels are coverage labels, not six empirically sampled demographic populations.

The ESA participation and Deloitte Gen Z groups contain minors. Their aggregate percentages must not be relabelled “adult Gen Z”. ESA separately labels its motivation chart as players aged 18+: those observations can describe adult Gen Z motivations, with subgroup size unavailable. Ofcom’s 16+ sample also contains minors. There is no verified adult-only Gen Z puzzle-performance dataset in this collection; the audit makes no demographic skill ranking. The US, UK and French studies cannot establish worldwide Android behaviour.

## Required simulation reporting

1. Record catalogue, seed, action policy, assumed error/help settings and session stop rules.
2. Preserve baseline and revised results; include failure cases, budgets exhausted, free undos, restarts, help used, rewards earned and interruptions restored.
3. Call synthetic elapsed values **modelled time**, never measured human time. Report ranges/sensitivity, not a prediction of commercial retention.
4. Do not fabricate D1/D7, conversion, churn, satisfaction or “desire to continue”. Completion and next-goal visibility are observable product properties; enjoyment and return intent require consenting human playtests.
5. Audit voluntary exits, transparent prices and deterministic rewards alongside pacing. Do not use age or presumed vulnerability to change difficulty, prices or paid prompts.

No finding here establishes a therapeutic or cognitive-health effect of this game. Reported reasons for playing are respondents’ motivations, not clinical outcomes.
