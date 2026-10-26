# Luma Drift roadmap

**Build the memorable rule. Observe players. Scale only when the evidence improves.**

Updated 26 October 2026. This roadmap records priorities and exit criteria. Future stages are proposals, not delivered features or committed launch dates. See [CHANGELOG.md](CHANGELOG.md) for release scope and [VALIDATION.md](VALIDATION.md) for checks actually executed.

## 1.0 — A playable foundation

Delivered: 240 deterministic campaign boards in eight chapters; free undo/restart; three powers; six original companions and six themes; earned-coin shop; local daily and weekly events; six languages; original vector art and synthesized music/effects; local save/resume. The original release has build, logic, signature and Android 16 emulator evidence. Physical-device acceptance and player retention remain unmeasured.

## 1.1 — Make the rule easier to learn and worth discussing

Delivered in 1.1; executed runtime coverage belongs in [VALIDATION.md](VALIDATION.md).

| Workstream | Release scope | Acceptance evidence |
|---|---|---|
| Learn by playing | Contextual movement, blocking and gate coaching; guided first moves and optional full help | Fresh-install interaction, gate introduction and accessibility labels |
| Distinctive decisions | Optional 240-board Remix route after campaign board 12, with dual-gate switches; original campaign retained | Exact-solver coverage, determinism, undo and save restoration |
| Fair return loop | Three daily missions, XP/rank and seven-board cosmetic badge; no lost-day punishment or forced streak | Reward idempotency, period rollover, rank and persistence tests |
| Player-to-player handoff | Reproducible C/R/E codes, original board cards and Android sharing; no friend-mode powers or progression gains | Code round trip, invalid-code rejection, handoff and reward rules |
| Learn without a backend | Local journal off by default, explicit JSON export and clear controls | Default state, bounded storage, exported fields and clear behavior |
| Presentation and delivery | Updated interface, screenshots, store assets, documentation, signed APK/AAB | Build/lint, installed final-binary interaction, signature and checksums |

The release validation document is the authority for completion of these checks. A line in this roadmap is not a passing test result.

## 1.2 — Physical quality and comprehension

**Entry:** the 1.1 release candidate passes its automated and emulator checks.

Test on at least one modest physical Android phone and one current phone, plus a smaller viewport and a large-screen configuration. Cover suspend/resume, interruptions, actual speakers/headphones, rapid taps, temperature/battery behavior, text readability and TalkBack. Fix regressions before the next cohort. Do not infer low-end performance from a powerful development host.

Observe 20 first-time players using the protocol in [HIT_AUDIT.md](HIT_AUDIT.md). Prioritize recurring confusion over requests for more features. Localize any changed help in all six languages and review it with speakers before marking translation quality validated.

**Exit:** publish a short device matrix, recurring confusion counts, fixes and retest results. Internal comprehension targets are met or explicitly revised with reasons.

## 1.3 — Measure voluntary return

**Entry:** first-session friction and physical-device blockers have been addressed.

Run a fresh 100–300-player test, wait for complete D1 and D7 windows, and report enrolled denominators, missing observations and channel. The optional local journal can explain individual sessions; it does not aggregate cohorts. Use a consented study process to establish return rates.

**Exit:** a versioned cohort report, including the result if it is disappointing. The working targets are 60% reaching board 5, 30% D1 return and 10% D7 return. These are project hypotheses, not industry benchmarks. If the result is weak, revise onboarding, early difficulty or the hook and test a fresh cohort before spending more.

## 1.4 — Store readiness and discovery experiments

**Entry:** player evidence justifies a public test.

Complete publisher identity, support contact, privacy policy hosting, content rating, target-audience declarations, Data safety, signing enrollment and the account's required testing process in Play Console. Run the Play pre-launch report. Resolve the actual report rather than assuming an uploaded bundle is approved.

For personal developer accounts created after 13 November 2023, Google's current guidance requires a closed test with at least 12 opted-in testers continuously for 14 days before applying for production access. Applicability depends on the actual publisher account; satisfying the count does not guarantee approval. [Official testing requirements](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en), checked 26 October 2026.

Test truthful store variants one variable at a time when traffic supports it: icon readability, first gameplay screenshot and explanation of gate consequences. Record visitors, installs, retained players and creative version. Do not optimize click-through by promising gameplay the app does not contain.

**Exit:** store review outcome, real conversion data and a measured source of qualified new players. The current work does not submit the app or contact testers.

## Later — Commercial tests and expanded content

Only after retention is worth protecting, test optional rewarded ads and a clearly described purchase in a separate integration milestone. Recheck privacy and regional requirements against the chosen SDKs. Model net revenue using observed impressions, fill, acquisition cost and retention; do not substitute a hypothetical eCPM for a business result.

Consider more mechanics, curated level packs, seasonal art or synchronized events only when player evidence points to a specific need. Cloud saves, accounts, remote leaderboards and live operations require a backend, operating budget and a revised privacy model; none is implied by the local event system.

## Stop or change direction when

- Players understand the rule but still choose not to return after two focused revisions.
- Acquiring retained players costs more than the conservative measured value they create.
- Maintaining additional systems consumes the time needed to improve the puzzle itself.

The outcome can be a smaller sustainable puzzle, a mechanic pivot, or ending the experiment. Adding content indefinitely is not a growth strategy.
