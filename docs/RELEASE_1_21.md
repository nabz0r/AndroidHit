# Luma Drift 1.21 — delivered for playtesting

**[Production APK](https://luma-drift-production.web.app/releases/luma-drift-1.21.0-rc1.apk)** · **[Separate Lab APK](https://luma-drift-production.web.app/releases/luma-drift-1.21.0-sandbox-rc1.apk)** · **[Playable simulation](https://luma-drift-production.web.app/lab/ui-1-21/index.html)** · [Website](https://luma-drift-production.web.app/)

Production `1.21.0-rc1` and isolated Lab `1.21.0-sandbox-rc1`, code 29, are delivered for playtesting. The exact B production and Lab packages have passed their separately indexed native scopes. Application source, native evidence and artifact metadata are published at [C1 `35bb8222`](https://github.com/nabz0r/AndroidHit/commit/35bb82229d8175968919b5979264e5094a1cc8dc). Website source and local QA are published at [C2 `71bba893`](https://github.com/nabz0r/AndroidHit/commit/71bba893a5fbfeb808e5c44840b6701862255453). [CI run 40976045727](https://github.com/nabz0r/AndroidHit/actions/runs/40976045727) passes both jobs and all 36 steps. [Firebase Hosting](validation/site-v1.21/hosting/deployment.json) confirms C2 as DEPLOY/FINALIZED, version `d08a2aa131c05c4f`, released `2026-10-30T00:08:28.331Z`. The [delivery register](validation/v1.21/publication/release-delivery.json) binds the exact original receipt bytes separately from local browser and native evidence. Eight original B site captures and the independent browser simulation are ready.

## Exact candidate identity

[Frozen B source/package report](validation/v1.21/candidate-b/candidate.json) · [Canonical artifact manifest](../releases/manifest-1.21.0-rc1.json). The B report freezes 493 inputs. It retains its historical creation status `SIGNED_CANDIDATE_PENDING_NATIVE`; later acceptance and delivery must use separate records rather than rewriting these frozen inputs.

| Channel | Version / code | Bytes | SHA-256 |
| --- | --- | ---: | --- |
| Production | `1.21.0-rc1` / 29 | 368,969,685 | `e382a73037418146bf57e1530998efeb312953ea8b9b5bb61b548af7cbf90bcc` |
| Isolated Lab | `1.21.0-sandbox-rc1` / 29 | 368,952,988 | `9c0998683f09a1d940a97a0dc5badea98a5ab5c84ea2ba814917e30f8df5235b` |

The retained signer certificate is `dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287`. Large APKs are ignored external artifacts; metadata and exact-byte checks belong in Git. [Canonical collection](validation/v1.21/publication/package-collection.json) verifies source-input hashes, package/version identity, signature, sizes and both APK hashes, and refuses to replace earlier canonical bytes.

The download routes above are included in the exact clean C2 Hosting payload. Both physical APK copies and all 525 payload hashes pass before deployment; live management confirms the release. Independent public response bytes and public browser rendering remain unverified under the saved permission refusal. The manifest's `publicly_published:false` retains its creation-time state; the separate delivery receipt records the later deployment.

## Resulting interaction

The home gives the equipped avatar a large full-body stage, actual companion level/copies and a separate player-level/XP bar. The avatar's style and the squad's puzzle effects have distinct routes. The evolution screen shows the actual authored forms at companion levels 1/3/6/10 and the real puzzle-skill explanation. Copies change companion level and forms; they do not silently increase a fixed puzzle-skill effect.

Play is followed by direct Home, Living room, Kitchen and Garden shortcuts. Kitchen is a real fourth independently saved ambient room. The optional catalogue item named `kitchen` remains a separate 440-Coin furnishing; opening the room does not grant that furniture or spend Coins. Original living/bedroom/garden save bytes and the existing house/atelier routes are preserved.

The boutique has an illustrated featured product, explicit price/stock/ownership and preview-before-confirmation routes. The companion banner still has the actual 23-entry fixed pool for 180 earned LumaCoins; Nova is the free starter and excluded. Signature looks remain a separate pool. Odds, duplicate returns, random-result status and account Gold's availability remain explicit. No changing pity state, near-miss staging or artificial urgency was added.

The map crosses ten original illustrated zones. Eligible new U11+ and expedition attempts add ice/fire/water shells with two/two/one free clearing taps. Direction and key cues are clearer, the backdrop reacts to actual play, and three distinct manual exits without correction earn one free Focus per attempt. Shell-clearing taps do not spend Coins or count as exits. The Focus is separate from inventory and companion Charge. Existing attempts keep their stored identity and state.

Scene fades last 300 ms with bounded travel. Coins, known Gold balances and XP animate toward actual persisted values; unavailable Gold remains unavailable. Reduced motion suppresses decorative/transition motion.

## Economy and progression roles

| Value | Actual 1.21 role |
| --- | --- |
| Earned LumaCoins | Device-local wins/gifts; powers, looks, arrivals and explicit eligible Undo fees. They do not become purchased Gold. |
| Player XP | First-clear progression; 120 XP per player level. Replays do not farm first-clear XP. It does not evolve companions. |
| Companion copies | Companion level and forms, with the existing cumulative thresholds. Targeted expeditions provide a separate choice from random arrivals. |
| Talent Charge | Attempt-local shared squad resource, starts at 3 and caps at 6; distinct exits credit it once. It is separate from session energy. |
| Session energy | Starts at 10; capacity grows up to 16 with campaign progress without filling the counter. Regeneration remains +1 per 10 minutes. An eligible new attempt costs 1; same-attempt resume is free. |
| LumaGold | Separate account wallet. Production real purchases remain closed. The 50-Gold/10-energy refill is a separate zero-energy Lab test route. |

`EconomyCurve121` depends on campaign level and the paid-Undo ordinal, without spending/failure/session profiling. U1–U10 keep base first-clear rewards and boost prices. The logarithmic reward multiplier reaches ×2 at U1000; the base boost multiplier reaches ×1.4 before the existing avatar discount. Star upgrades pay only the newly earned entitlement difference. Eligible new attempts have two free Undos; later fees are displayed and confirmed in earned Coins, grow with level/paid-Undo count and cap at 40. The undone move remains counted. [Model source](../app/src/main/java/com/androidhit/lumadrift/data/EconomyCurve121.java) · [Host arithmetic and limits](validation/v1.21/host/economy-analysis.md).

## Executed evidence and its limits

[Host evidence index](validation/v1.21/host/README.md) records 60 accepted A commands/scopes, preserving the original 57-pass/1-fail integration run and the corrected transition-test expectation. B changes only presentation/map-copy inputs; all 154 protected host model/build dependencies remain unchanged. The B comparison reuses the original model evidence under its actual identity and separately runs 240 map-copy JVM checks plus 21 staged site/demo tests. It does not relabel A model runs as B native execution.

The new host scopes include all 1,000 certified elemental plans and actual companion exits, Focus anti-farming, kitchen save isolation, economy/Undo fault recovery, energy-cap upgrades and menu/map geometry. The independent summon analysis executes 800,000 fixed-seed draws against the actual source pools; modeled cohorts and probabilities are not measured retention or player spending.

[Eight original B site captures](validation/v1.21/native/site-media-candidate-b-accepted/capture-map.json) bind full 1080×2400 FR/EN home/shop/map/puzzle PNGs to passed phase reports and the exact installed production SHA-256. The original phase PNGs are never edited or relabelled. [Production B native acceptance](validation/v1.21/native/candidate-b-final-index.json) passes 13 phases: 574 native assertions, 149 actual touch actions, 52 original PNGs and two videos, with 38 derived comparisons counted separately. Its scopes cover profile preservation, blocked accounting, FR/EN surfaces, living/kitchen/garden selectors, actual earned-Coin draw cancellation/confirmation and cold recovery, two-free/paid Undo with cold resume, elemental shells/Focus with cold resume, and compact reduced-motion navigation. Earlier A failures remain preserved.

[Isolated Lab B acceptance](validation/v1.21/native-lab/candidate-b-final-index.json) passes three phases: 99 native assertions, 41 actual touches, nine original PNGs and one video, plus 24 separate derived comparisons. The paid test refill actually debits 450→400 test Gold at zero energy; energy then follows 0→10→9 and remains 9 across a distinct cold process. [All six production preference maps remain identical before/after Lab](validation/v1.21/native/production-profile-preserved-after-lab.json). This is isolated Lab test-currency evidence, not a real purchase. Both native indexes identify API 36 ARM64, 16 KiB pages and the exact candidate hash; these are emulator scopes.

[The independent browser demo](validation/site-v1.21/staging/lab/ui-1-21/index.html) uses actual companion sheets and explicitly fictional in-memory balances, mini-puzzle state and teaching rewards. Its source-derived 23-entry draw pool, duplicate compensation, copy thresholds and interactive U-level curve have 12 model tests; the curve matches outputs generated by the native Java class with a bound source hash. Nine site-runtime checks cover language/metadata/links/audio controls. [Actual local browser QA](validation/site-v1.21/browser-local/simulation/report.json) covers four views × two languages × four requested widths (320/375/768/1280), zero horizontal overflow, all four form previews, one complete teaching loop and no observed console errors. It accesses no Android save or real payment. Operating-system reduced-motion preference was not changed; its code/CSS handling was inspected separately.

## Website delivery and evidence

The existing Firebase site is retained. The prepared homepage uses only the exact eight B captures and preserves existing gallery/audio markup, assets, earlier APK archives, authentication, account and deletion pages. The intentional factual privacy amendment records previous/current hashes and its original source commit; it describes local 10→16 capacity and elemental/Focus/Undo state/receipts without announcing a new server service.

| Evidence | Result and scope |
| --- | --- |
| B build/lint/signed-byte identity | PASS; exact manifest above |
| Original B site-media provenance | PASS; eight source-bound native PNGs |
| Complete B production/Lab native acceptance | PASS within the 13 production and three Lab phases above; derived comparisons separate |
| Local simulation/model/responsive checks | PASS within the scoped evidence above; [final complete hosted website command](validation/site-v1.21/source-qa-prepared.json) passes 78/78 tests against the prepared homepage |
| Complete staged homepage browser QA | PASS: [8 responsive language/width states and 40 actually visible native image checks](validation/site-v1.21/browser-local/homepage/report.json), preserved gallery, deliberate audio switching, keyboard focus and corrected privacy title |
| Immutable application/artifact source | Published C1 `35bb82229d8175968919b5979264e5094a1cc8dc`; exact manifest bound |
| Hosted CI | PASS: [CI run 40976045727](https://github.com/nabz0r/AndroidHit/actions/runs/40976045727), exact C2 head, two successful jobs / 36 successful steps / none failed or skipped. [Provider receipts and full logs](validation/v1.21/publication/c2/actions-40976045727/summary.json) |
| Exact website source/payload manifest | PASS locally: 525 payloads / 3,575,292,556 bytes; 507 files pinned at the preparation checkpoint after the privacy update. Compared with 1.20: 506 byte-identical files, explicit homepage/privacy changes, 17 new files and none removed ([source comparison](validation/site-v1.21/hosting/previous-source-diff.json)); eight native originals; one reviewed factual privacy amendment. [Prepared source manifest](validation/site-v1.21/release/source-manifest.json) binds C1. Homepage bytes match the reviewed preview exactly. [1,143 offline source checks](validation/site-v1.21/source-qa-prepared.json) pass. Website source published at [C2 `71bba893`](https://github.com/nabz0r/AndroidHit/commit/71bba893a5fbfeb808e5c44840b6701862255453). |
| Prepared public-directory browser QA | PASS: [4 FR/EN × mobile/desktop states, 20 individually visible original image loads and working simulation navigation](validation/site-v1.21/browser-local/prepared/report.json), zero observed errors. Plain loopback serving of actual public files; no staging overlay. |
| Firebase DEPLOY/FINALIZED receipt | PASS: [original remote JSON receipt](validation/site-v1.21/hosting/deployment.json), version `d08a2aa131c05c4f`, released `2026-10-30T00:08:28.331Z`, exact clean C2 and all 525 physical payload hashes verified. Exported gzip/base64 through authenticated UI, decoded and checked against the remote SHA-256; not a derived-only transcription. |
| Public-origin byte/render verification | Unverified; no claim from local or management evidence |

[Website preparation and source-preservation workflow](validation/site-v1.21/README.md). Package identity, native execution, model tests, local browser rendering, authenticated Hosting management and public-origin delivery remain separate. These results do not establish physical-phone comfort, human enjoyment, retention, Play publication or real-provider purchase/refund acceptance.
