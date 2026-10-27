# Luma Drift 1.6 — playable candidate

**27 October 2026 · declared production `1.6.0-rc1` / Lab `1.6.0-sandbox` · version code 7**

The signed production and Lab artifacts are collected in [Install and verify](RELEASE.md), with exact SHA-256, certificate, package and version metadata. Both release builds/lint tasks complete; binary schema, signature and alignment checks pass. [Complete qualification record](VALIDATION_1_6.md) separates host tests, installed observations and outstanding human/provider acceptance. Earlier 1.5 artifacts and evidence retain their original bytes and scope.

## Candidate changes

| Area | Implemented source behavior | Contract |
| --- | --- | --- |
| Journey | Ten-board saga pages with current/locked/completed states and a prelevel briefing for objective, element and authored difficulty | [UX candidate](UX_1_6.md) |
| Content | Frozen J/K V3 route: 1,000 Journey and 240 Remix; key lessons at Journey 12/14/17, then sparse recurrence | [Content method and compatibility](PROGRESSION_V3.md) |
| Rhythm | Introduction, Practice, Peak and Recovery descriptors; Normal, Hard and Expert are authored comparative labels | [Exact model audit](data/progression-v3-summary.md) |
| Inventory | Companion/theme/equipment/power categories, owned/equipped/locked presentation, acquisition/effect copy and preview before a confirmed action | [UX contract](UX_1_6.md), [equipment limits](AVATAR_EQUIPMENT.md) |
| Layout | Scroll-aware screen layout, separated navigation/content regions and refreshed foreground hierarchy | [UX acceptance requirements](UX_1_6.md) |
| Identity | Immutable Guest/Refreshing/Connected/Reauthentication-required/Error snapshots, optional silent foreground refresh and guards against stale account callbacks | [Identity setup and boundaries](GOOGLE_SIGN_IN.md) |
| Art | Decorative aurora artwork alongside existing vector pieces/companions; information remains in foreground labels/shapes | [Presentation method](UX_1_6.md#art-and-visual-feedback) |

Existing six-power ownership/confirmation/recovery, local events, earned economy, original music and equipment persistence continue under their established contracts. This revision does not activate commercial sales, live ad units or gameplay cloud saves. A new presentation and session model do not qualify a real Google provider login.

## Rules and fairness

A piece needs a clear exit ray and an open directional gate. Switches toggle their gate masks after exit. A sealed fixed cell blocks crossing rays while its matching key remains; Undo restores the key and closes its cells again. Opening seals alone does not finish a board: every piece must leave.

Journey uses keys on **167/1,000 boards**, Remix on **29/240**. The first key lesson at Journey 12 has at most nine pieces, one key/one seal, neutralized switches and open gates, isolating the new rule. Boards 14 and 17 are Normal/Practice combinations. Each certificate clears in N exits within a fixed N+3–5 starting allowance. No board, margin or label depends on spending, ads, losses or the individual player. Free restart remains available; no certificate requires a power or paid continuation.

The six existing P workshop boards keep their original unlimited-action, no-power/no-reward rules. C/R/B/Q/G/H/P/E layouts and code checksums remain unchanged. New J/K identifiers avoid silently changing any historical share.

## Compatibility

| Saved content | Layout on restoration | Allowance |
| --- | --- | --- |
| Content 2 | Historical B/Q through `legacyGet()` / `legacyRemix()` | Saved supported policy, including original policy 1 |
| Content 3 | G/H through `getV2()` / `remixV2()` | Original policy-2 frozen margin |
| Content 4 | Current J/K through `get()` / `remix()` | Policy 2 with the V3 board's fixed margin |
| Friend/P/event | Parsed family through `ChallengeCode.resolve()` | Its original mode/policy; friend practice remains unlimited |

Local stars, unlocks, inventory and equipment are not reset or advertised as cloud data. Host tests reconstruct old active histories, gate states and budget counters; the [actual API 26 Lab replacement](validation/v1.6/upgrade-api26.json) preserves a real 1.5 G1 run and its 8/9 actions, then starts a fresh J2 at 11/11. Preserved [1.4→1.5 upgrade evidence](validation/v1.5/active-save-upgrade.json) does not establish a 1.5→1.6 upgrade result.

## Recorded host-side evidence

| Check | Recorded result | Evidence |
| --- | --- | --- |
| V3 solutions, independent exact reference, real seal dependencies, uniqueness, saved histories/budgets | 582,831 checks passed across 1,240 new boards | [Progression test](validation/v1.6/progression-tests.txt) |
| Original engine, alternate moves, seeded events and paired switches | 437,039 assertions passed | [Engine test](validation/v1.6/engine-tests.txt) |
| Action accounting, fixed policies, corrupted saves and continuations | 390,327 checks passed | [Budget test](validation/v1.6/move-budget-tests.txt) |
| Preserved historical/V2 structural audit | 161,985 checks passed | [Difficulty test](validation/v1.6/difficulty-tests.txt) |
| Six P boards, all subsets × 16 gates and independent sealed-cell oracle | 10,425 checks passed | [Sealed test](validation/v1.6/sealed-tests.txt) |
| Historical layout/seed/solution/code/allowance compatibility | Eight pre-change/post-change family fingerprints match | [Before](validation/v1.6/legacy-content-before.sha256), [after](validation/v1.6/legacy-content-after.sha256) |
| Deterministic authoring | Regenerated frozen V3 source is byte-identical | [Reproduction and input/output hashes](validation/v1.6/progression-reproduction.txt) |

Additional integrated host checks are collected under [validation/v1.6](validation/v1.6/); their named report and source revision define their scope. JVM storage/provider adapters are synthetic test doubles. Exact-subset success and random-tap simulations are technical baselines, not human completion, enjoyment, retention or business results. [Board-level data](data/progression-v3-audit.csv) · [Standalone curves](data/progression-v3-curves.svg).

## Final artifact and runtime qualification

The exact four APK/AAB identities, source input hashes, release build, eight lint warnings per variant and binary checks are recorded in [1.6 qualification](VALIDATION_1_6.md). Installed tests identify their own APK hash and platform. Earlier candidate screenshots are explicitly separated from final artifact tests.

Remaining acceptance includes human difficulty/readability sessions, physical phones/audio, full TalkBack/large-text behavior, authorized real provider login and commercial purchase/ad qualification. A technically solved board is not a measured human difficulty or retention result.

Default source settings force sample ads and leave the wallet origin empty; actual final BuildConfig and manifest values must be recorded when packaging. Actual API 26/36 emulator reports identify the exact candidate; commercial/provider qualification and human tests remain separate. Do not attach a preserved 1.5 checksum or screenshot to a rebuilt 1.6 artifact. [Development and packaging](DEVELOPMENT.md) · [Qualification roadmap](ROADMAP.md).

Installed final acceptance includes 88 Lab and 43 production menu checks, six languages/compact/landscape/font 1.3, actual 16KB-kernel launch, real audio playback, actual 1.5 Lab save replacement, final Guest/provider cancellation, single-charge powers, key/Undo behavior and Flow pause/help/rotation. [Exact scopes, synthetic profiles and retained pilot failures](VALIDATION_1_6.md). Human readability/retention and authorized real provider/commercial operations remain unqualified.
