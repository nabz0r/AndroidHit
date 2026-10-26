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

## 1.2.0-sandbox — Prepare monetization without real sales

**Delivered test application.** The Lab APK is packaged and installed; covered Android 16 SDK and UI cases passed. The stable 1.1 APK and its evidence remain available separately. The owner requested test mode, so this milestone does not activate production advertising or payments. [Executed checks and remaining limits](VALIDATION.md).

| Workstream | Delivered Lab scope | Acceptance contract |
|---|---|---|
| Isolation | `com.androidhit.lumadrift.sandbox`, own save and wallet, visible Lab label | Install beside stable; verify no balance/progress contamination |
| Rewarded test ad | Adult declaration + explicit opt-in + UMP eligibility + manual load; Google sample IDs | Observe refusal/unavailable path and any actual test creative separately; earn 35 once only on the reward callback |
| Gold simulation | Native no-charge 500/1,400/3,600 pack dialog; cancel/pending/complete; idempotent grant | Pending survives restart, no premature credit, duplicate completion adds zero |
| Billing boundary | Billing 9.1 connection/product queries; actual checkout disabled | SDK status and disabled checkout evidence; no claim of a license-test purchase |
| Item economy | Progression bands based on distinct campaign + Remix clears | Formula/boundary tests and later free-player affordability observations |
| Local observations | Optional journal ad callbacks; no Firebase Analytics or remote gameplay analytics | Default-off behavior, export inspection, test-labelled values excluded from income |
| Expanded collection | 24 total companions, original six preserved, three pages of eight, native preview/buy/equip | Catalog/purchase contracts, all-page visual review and installed interaction |
| Clear local events | Goals, stage progress/rewards, next stage, reset countdown and visible bonus | Device-calendar boundary tests and daily/weekly UI/claim checks |
| Optional identity | Credential Manager + Firebase Auth; no owner config by default, guest-first UI | Not-configured behavior now; actual provider login/sign-out/deletion only after owner setup |

The [Lab setup guide](MONETIZATION_SETUP.md) distinguishes a local simulation, a Google test ad, a Billing query and a future Play license-test purchase. Passing one does not establish the others. [Research](MONETIZATION_RESEARCH.md) records the official sources used before implementation.

[Companion/event logic checks](validation/lab/collection-events-tests.txt) record 87,121 checks, including 604 time zones and 14,496 reset-boundary scenarios. This is evidence for deterministic contracts, not a human visual review. [Collection guide](COMPANIONS_EVENTS.md) · [Optional identity setup](GOOGLE_SIGN_IN.md).

The [installed collection/event run](validation/lab/collection-events-ui-smoke.txt) separately browsed the 24 cards, exercised native preview/purchase/equip, completed ten event boards and checked one-time bonuses across the six-language loop. A Google rewarded **test** creative and +35 reward, UMP refusal and local purchase simulations were also exercised. Identity evidence remains limited to [guest/not-configured English/French handling](validation/lab/identity-ui.txt). Production account setup, actual Google login, Play license-test purchases and an authoritative wallet remain future work.

## Next — Physical quality and comprehension

**Entry:** use an identified stable build, with the tested package and device recorded. Test Lab-specific surfaces separately when that package has passed its own checks.

Test on at least one modest physical Android phone and one current phone, plus a smaller viewport and a large-screen configuration. Cover suspend/resume, interruptions, actual speakers/headphones, rapid taps, temperature/battery behavior, text readability and TalkBack. Fix regressions before the next cohort. Do not infer low-end performance from a powerful development host.

Observe 20 first-time players using the protocol in [HIT_AUDIT.md](HIT_AUDIT.md). Prioritize recurring confusion over requests for more features. Localize any changed help in all six languages and review it with speakers before marking translation quality validated.

**Exit:** publish a short device matrix, recurring confusion counts, fixes and retest results. Internal comprehension targets are met or explicitly revised with reasons.

## Next — Measure voluntary return

**Entry:** first-session friction and physical-device blockers have been addressed.

Run a fresh 100–300-player test, wait for complete D1 and D7 windows, and report enrolled denominators, missing observations and channel. The optional local journal can explain individual sessions; it does not aggregate cohorts. Use a consented study process to establish return rates.

**Exit:** a versioned cohort report, including the result if it is disappointing. The working targets are 60% reaching board 5, 30% D1 return and 10% D7 return. These are project hypotheses, not industry benchmarks. If the result is weak, revise onboarding, early difficulty or the hook and test a fresh cohort before spending more.

## Next — Store readiness and discovery experiments

**Entry:** player evidence justifies a public test.

Complete publisher identity, support contact, privacy policy hosting, content rating, target-audience declarations, Data safety, signing enrollment and the account's required testing process in Play Console. Run the Play pre-launch report. Resolve the actual report rather than assuming an uploaded bundle is approved.

For personal developer accounts created after 13 November 2023, Google's current guidance requires a closed test with at least 12 opted-in testers continuously for 14 days before applying for production access. Applicability depends on the actual publisher account; satisfying the count does not guarantee approval. [Official testing requirements](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en), checked 26 October 2026.

Test truthful store variants one variable at a time when traffic supports it: icon readability, first gameplay screenshot and explanation of gate consequences. Record visitors, installs, retained players and creative version. Do not optimize click-through by promising gameplay the app does not contain.

**Exit:** store review outcome, real conversion data and a measured source of qualified new players. The current work does not submit the app or contact testers.

## Later — Commercial tests and expanded content

The Lab prepares test controls now; **real traffic and real sales remain future work**. Before production, configure the owner's AdMob/UMP and Play accounts, verify actual license-test purchase behavior, qualify a backend wallet and token-verification service, review SDK terms and complete store/privacy declarations. Remove simulation controls from the production route. No service is deployed merely by documenting its contract.

Only run commercial experiments after retention is worth protecting. Model net revenue using observed impressions, fill, acquisition cost and retention; do not substitute a hypothetical eCPM or test paid-event callback for a business result. Preserve stable earned progress and keep Lab gold outside production entitlements.

Consider more mechanics, curated level packs, seasonal art or synchronized events only when player evidence points to a specific need. Optional managed identity is prepared in the Lab, but cloud saves, a verified wallet, remote leaderboards and live operations still require separate services, an operating budget and privacy review; none is implied by a Google sign-in adapter or local event system.

## Stop or change direction when

- Players understand the rule but still choose not to return after two focused revisions.
- Acquiring retained players costs more than the conservative measured value they create.
- Maintaining additional systems consumes the time needed to improve the puzzle itself.

The outcome can be a smaller sustainable puzzle, a mechanic pivot, or ending the experiment. Adding content indefinitely is not a growth strategy.
