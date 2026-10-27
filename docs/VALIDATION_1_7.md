# Luma Drift 1.7 validation

27 October 2026 · production `1.7.0-rc1` / Lab `1.7.0-sandbox`, code8.

| Area | Executed result | Evidence |
|---|---|---|
| Board solvability | All 1,000 Journey +240 Remix initial allowances; 49,063,063 assertions | [Independent audit](validation/v1.7/board-audit.md) |
| Rewards and wardrobe | 100,953 checks; 100,000 disclosed-odds draw simulation | [Model/storage tests](validation/v1.7/rewards-cosmetics-tests.txt) |
| Native UI/gameplay | Final Lab72 assertions,41 captures; preference and rotation restoration | [SDK acceptance](validation/v1.7/SDK_UI_FINAL.md) |
| Production startup/audio | Final production7 assertions; AudioFlinger advancing, background pause/resume | [SDK acceptance](validation/v1.7/SDK_UI_FINAL.md) |
| Binary integrity | Four signed APK/AAB files, source freeze, ZIP/ELF16KB and bundletool validation | [Binary audit](validation/v1.7/binary-verification.txt) |
| Public artifact delivery | Four independent raw HTTPS downloads match exact final hashes and sizes | [Publication](validation/v1.7/publication.json) |
| Server | 62 unit/HTTP +3 actual Firestore-emulator tests; deployed00005-pps, health200/private unauth401 | [Server tests](../server/validation/social-v1.7/README.md), [rollout](validation/v1.7/cloud-run.json) |
| Website | 2,219 source checks,24 deletion-core tests,18 local CUA bilingual/responsive views | [Website record](validation/site-v1.7/README.md) |

[Detailed scope and raw evidence](validation/v1.7/README.md) · [Exact artifact identities](RELEASE.md) · [Release behavior](RELEASE_1_7.md).

These checks do not establish actual two-account Google/social transactions, real checkout/refund or live-ad eligibility, Play approval, physical-speaker quality, human difficulty, retention or revenue. Local earned progress is not synchronized by Google sign-in. Paid cosmetic draws remain closed by independent APK/server gates. Human playtesting and real provider acceptance remain separate work.
