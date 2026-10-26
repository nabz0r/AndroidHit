# Changelog

Release scope is separated from executed checks in [VALIDATION.md](VALIDATION.md). APK bytes and checksums are recorded in [RELEASE.md](RELEASE.md).

## 1.2.0-sandbox — Luma Drift Lab

**Delivered 26 October 2026 — separate test app, version code 3.** Current source targets `com.androidhit.lumadrift.sandbox`, displayed as Luma Drift Lab. It can coexist with stable 1.1 and has separate private data. This is not a production monetization release. [Download Lab APK](../releases/luma-drift-1.2.0-sandbox.apk) · [Exact package checks and runtime evidence](VALIDATION.md).

### Implemented test scope

- GMA Next-Gen 1.6.0, UMP 4.0.0 and Billing 9.1.0 dependencies, with R8/resource shrinking for the release build.
- A native Lab screen with declared-adult and explicit advertising opt-in gates. UMP request eligibility precedes background ad initialization; loading requires a separate manual action. Sample Google identifiers only. An earned callback grants 35 test gold once per opportunity.
- Optional local ad impression and paid-event observations, using the existing off-by-default journal. No Firebase Analytics, remote gameplay analytics or attribution backend. Optional Firebase Auth serves identity separately; SDK network behavior is disclosed.
- Three no-charge pack simulations: 500, 1,400 and 3,600 gold. Cancel/pending grants nothing; pending state persists; completion is credited once with a unique local transaction. A repeated completed transaction grants zero.
- A real Billing connection/query adapter with real checkout disabled. No fabricated Google receipt, production prices or token-verification service.
- Progression-only booster pricing: bands at 0/30/90/210/450 distinct campaign + Remix clears, rounded to five coins and bounded. Cosmetics and puzzle access remain unchanged.
- 24 companions total: original six preserved plus 18 distinct Canvas silhouettes. Three pages of eight with native preview, explicit purchase and equip. Expanded collection costs 12,200 earned coins; six themes remain.
- Clearer daily/weekly event goals, progress, stage rewards, next-stage action, calendar countdown and always-visible bonus control. A JVM check records 87,121 checks over collection contracts and 604 time zones/14,496 reset-boundary scenarios; it is not installed-UI evidence.
- Optional Google sign-in adapter through Credential Manager 1.6.0, Google ID 1.2.1 and Firebase Auth 25.0.0. Owner configuration is absent by default: honest guest/not-configured UI, no fake identity, no live-login claim and no cloud save. Sign-out and app-account deletion use SDK operations when configured. Advertising consent remains independent.
- Dual-artifact documentation, privacy distinctions and an activation guide that keeps production accounts and server fulfillment outside the delivered test scope.

The stable **1.1.0 APK/AAB and its evidence are preserved**, with [source checkpoint 15751be](https://github.com/nabz0r/AndroidHit/tree/15751bec3f332f60ba1ba6fef0c0b82f47f941c0). Simulated balances cannot transfer into stable saves or become purchased currency. See [Lab setup](MONETIZATION_SETUP.md), [research](MONETIZATION_RESEARCH.md) and [gameplay/economy](GAMEPLAY.md).

### Executed checks and limits

Lab logic suites record 435,314 engine assertions, 7,083 data checks, 362 meta checks plus JSON validation, 3,474 economy checks and 87,121 collection/calendar checks. Lint uses standalone 9.4.0 through an experimental override and reports **0 errors, 4 update advisories**; it is not a “no issues” result.

Actual Android 16 interactions cover a displayed Google test rewarded ad with +35 test gold, UMP refusal, simulated pack cancellation/pending/restart/idempotence, all 24 companion cards/three pages and native purchase/equip, all three daily plus seven weekly boards and unique bonus claims, and all six languages in the collection/event loop. Account tests cover English/French guest and missing-configuration handling only. A final-binary journal run also observed one impression, one reward and an actual paid-event callback (USD 0 micros / UNKNOWN / test=true), then cleared the journal/export cache and restored opt-out. These test values establish no income. Real Google login, Play checkout and production ad demand/revenue remain unvalidated. [Actual metrics evidence](validation/commerce-ad-metrics-smoke.txt).

## 1.1.0 — Growth and clarity update

**Released 26 October 2026 — signed package and Android 16 emulator checks passed.** See [stable validation](VALIDATION_1_1.md) for its final release evidence; implementation alone does not establish a passing runtime check.

### Scope

- Contextual board coaching for movement, blocking and gate switches, including guided first moves; help remains available in Settings and Pause.
- A 240-board optional Remix route that unlocks after campaign board 12. Switches can change two gates at once. The 240 original campaign layouts and their progress are preserved.
- Three optional daily missions, permanent XP/rank, distinct days played and a cosmetic badge after seven first board completions. No missed-day punishment. Replaying a completed board cannot farm XP or mission credit.
- Versioned C/R/E challenge codes for campaign, Remix and event boards, manually entered or opened through the app link. Android sharing offers an original card rendered from the board plus its code. Friend challenges grant no coins, XP, mission credit or campaign unlocks, and powers are unavailable.
- A local play journal, disabled by default, with explicit JSON export and clear controls. It has no analytics server or silent upload. Local return indicators describe this device's observation window, not cohort retention.
- Interface, interaction and accessibility refinements. Final release screenshots and store artwork accompany the delivery, subject to the capture evidence in [VALIDATION.md](VALIDATION.md).
- Product audit, staged roadmap, store listing draft and expanded English documentation.

See source, final package metadata and validation evidence for the precise delivered behavior. No improved retention, revenue, Play approval or viral growth has been measured.

## 1.0.0 — 26 October 2026

### Added

- Native Java/Android Canvas offline extraction puzzle with directional pieces and toggle gates.
- 240 deterministic campaign boards across eight chapters, with exact solver and free undo/restart.
- Three optional powers: Spark, Vision and Flow.
- Six original animated vector companions and six visual themes, unlocked with earned coins.
- Three-stage daily and seven-stage weekly local events, with once-per-period completion bonuses.
- French, English, German, Spanish, Portuguese and Italian, automatic language selection and fallback.
- Original synthesized soundtrack and eleven effect cues, separate sound/music controls, haptics and reduced motion.
- Private local progress, settings, economy and interrupted-session restoration.
- Keyboard navigation, virtual accessibility labels and system-bar insets.
- Signed release APK, Android App Bundle, source and documentation.

### Evidence and limits

The original release records 190,238 engine assertions, 3,219 data assertions, passing release build/lint and Android 16 emulator interaction. Those counts belong to 1.0.0 and must not be presented as the totals for later revisions. Physical-device testing, market retention, monetization and Google Play publication were not established.
