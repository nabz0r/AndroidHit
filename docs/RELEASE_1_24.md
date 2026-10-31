# 1.24 — companion growth and Pulse presentation

31 October 2026. **Candidate D, source/CI, Hosting and public delivery are accepted
for playtesting within the scopes below.** Production is `1.24.0-rc2`, Lab is
`1.24.0-sandbox-rc2`, both code 33. [Qualification register](validation/v1.24/README.md)
contains the original evidence links and open gates.

The [verified artifact source object](validation/v1.24/publication/c1/git-publication.json)
is `787fba142b8edd1f840b9b70cb7749167edacd24`, tree
`8a07f1f8d0808d229cb6134056330a0eb6864a8b`. Its 522 native build inputs match D.
The receipt explicitly records that main was not updated at this stage. This
source object is not a website deployment.

Published source C2b is `66119799d6fbe5254a8d37d50db76afe8daffbdc`, tree
`ee94f27e3c9dbbbc49c0d95568424c9d4af2118a`. Its
[CI receipt](validation/v1.24/publication/c2-ci/c2b-ci.json) records two successful
workflows and three successful jobs. App tree `81eba0e25202124ebebb5f4468d5dd8ba8ac3897`
and public tree `d003cdc3ca4a4aa87ea19a5ece1222fa49c9975b` are unchanged from C2.
The [original C2 failure](validation/v1.24/publication/c2-ci/c2-ci-first-verdict.json)
and [22-step local correction/recheck](validation/v1.24/publication/c2b-local/final-receipt.json)
remain separate: historical furniture counts, the initial decoration badge count
and renderer compile dependencies were corrected in tests/harness/sparse inputs,
without changing the app, public payloads or 69 frozen site inputs.

## What changes

- First eligible wins retain the existing 25/30/35 XP reward. Every new 120-XP
  player rank grants one copy to the captain captured at the start of the run,
  not the companion currently displayed at home. The existing rank becomes a
  migration floor; no retrospective copy backlog is paid.
- Companion level determines the next attempt's capacity: level 1 has one use,
  level 2 two, levels 3–5 three and levels 6–10 four. Charge costs and effects
  remain unchanged. An already-started attempt keeps its snapshot, and legacy
  decoded attempts keep their one-use policy. A captain at the 92-copy maximum
  receives 45 existing LumaCoins instead, within the same idempotent rank claim.
- The victory result names the actual captain, rank/copy reward and future
  capacity. Rank compensation is already included in its Coins total. Replays,
  cancellation and failed boards do not grant another copy. Legacy runs without
  a durable captain receive their normal XP without an invented recipient.
- Original white/cyan Pulse surfaces, Nunito, sculpted arrows, key/lock sprites,
  resource icons and illustrated furniture/equipment replace the old runtime
  presentation. The house keeps four rooms and saved ownership/layout IDs;
  48 furnishings and 18 gameplay equipment pieces remain distinct from outfits.
- Contextual skill sheets show exact Charge cost and remaining uses. Wardrobe
  preview/cancel retains its tab, page, scroll and draft; applying equipment
  returns without reopening the preview in a history loop. Trying on, worn and
  Apply remain distinct.
- Earned ad-Coins use a replayable pending journal and a stable reward identity.
  Insufficient isolated Lab Gold now resolves an unpaid refill to a durable
  refusal; it no longer becomes a generic pending confirmation on foreground.
  Already-paid spends replay without a second debit. Production network failures
  retain their existing retry identity; CONTINUATION and POWER rules are unchanged.

[Rank/copy recovery contract](validation/playtest-2026-10-31/rank-copy-124/README.md),
[ad reward recovery](validation/playtest-2026-10-31/economy/README.md),
[Lab energy refusal fix](validation/v1.24/energy-refusal/README.md),
[wardrobe/result contract](validation/v1.24/victory-wardrobe-local/README.md),
[UI contract](validation/v1.24/ui-pulse/README.md) and
[art provenance](art/v1.24/README.md) keep their own source/host evidence scopes.
A reward cannot be guaranteed if a process dies before any durable write; storage
failure/corruption is not presented as a successful durable grant.

## Exact D artifacts

[Canonical manifest](../releases/manifest-1.24.0-rc2.json), including signer and
package IDs, binds these ignored external APKs:

| Channel | Bytes | SHA-256 |
| --- | ---: | --- |
| Production | 340,290,322 | `2cb544e6ee341d346e7b6c90a75c46c3b7fa909ce236b5048677476920e7f67e` |
| Lab | 340,290,009 | `1929ee6bc8644ee936bfe7db917fd02e3000598f6b83d053af51a8c5d5cd238b` |

The [D build log](validation/v1.24/candidate-d/build.txt), signatures and badging
remain beside its [candidate report](validation/v1.24/candidate-d/candidate.json).
Earlier A/B/C artifacts retain their own identities and results.

## Executed validation

- **Production, B-created fresh profile updated to D:** 254 accepted assertions,
  46 actual taps. D preserves U5 state, starts U6 with two uses, applies two GUIDE
  activations separated by a manual exit, survives cold resume and reaches U8.
  U7 awards the next captain copy; final state is 280 XP and three Nova copies.
  This is not a direct empty-profile first installation of D.
  [Original register](validation/v1.24/native/production-final/README.md).
- **Retained production D:** three phases pass 60 assertions/46 taps: map/gallery
  account for 43/32 and wardrobe apply/return for 17/14. The
  [retained index](validation/v1.24/native/production-final/retained-candidate-d.json)
  preserves the exact installed hash; B's historical tests are not merged into D.
- **Retained Lab D:** six phases pass 330 assertions/53 touches/22 original PNGs,
  with production preferences preserved, short/large-font context checks and
  real lifecycle/Charge/refusal paths. Three SDK lifecycle controls are labelled
  separately from touchscreen touches.
  [Summary](validation/v1.24/native-lab/retained-d-summary.json).
- **Fresh Lab D:** five accepted phases pass 238 assertions/65 touches/17 PNGs.
  U11 energy 10→9 and resume 9→9, confirmed restarts to 0, a neutral refill
  cancellation, then test Gold 500→450 and energy 0→10 are observed. The refill
  has one spent marker, one used marker and one energy receipt; cold readback
  retains 730 Coins/450 Gold/10 energy. Minor/UMP/Do not consent/test-ad ready
  paths pass, but two Watch attempts stop at the SystemUI foreground boundary.
  Those earlier phases did not qualify ad display, early close or earned +35. Four
  stopped phases stay outside the five-phase totals. Ads opt-in was revoked and
  Lab 5634/5638 stopped normally with data preserved. The
  [final Lab index](validation/v1.24/native-lab/index.json) joins retained and
  fresh: 11 PASS phases, 568 assertions/118 touches/39 PNGs; 522 app inputs remain
  unchanged. [Fresh summary](validation/v1.24/native-lab/fresh-d-summary.json).
- **Separate Lab SDK supplement, same D APK:** three PASS phases record 142
  assertions/22 gestures/10 PNGs; three stopped phases remain separate. After one
  precisely identified Android full-screen help acknowledgement, an actual SDK
  Test Ad displays Reward granted and credits 730→765 Coins. Cold restart retains
  765, with the pending entry empty and the transaction marker present; test Gold
  450 and energy 10 are unchanged. No callback or reward was injected. Android Back
  and early close remain **unqualified**: zero Back and zero SDK Close actions
  were sent. Ads opt-in is revoked and 5638 stopped normally with its profile
  preserved. The [supplement](validation/v1.24/native-lab/sdk-diagnosis/summary.json)
  does not rewrite or merge into the immutable 568-assertion index. Its
  [collection receipt](validation/v1.24/native-lab/sdk-diagnosis/collection-receipt.json)
  records 823 byte-verified local copies, including nine observer APKs retained
  locally and ignored in Git; 823 is not a claim of tracked publication.
- **Synthetic model:** 8,000 trajectories, 350,874 attempts and 40,000 probes;
  104 oracle controls are separate. The policy can repeatedly request GUIDE
  before following its visible cue. Nova's evolving-capacity mean is 42.5065
  clears versus 43.1440 in its one-use control. This does not show human decline
  or justify a talent nerf. The model sees structured rules, not art or text,
  and does not test Android transactions, LumaGold, ads, equipment or decoration.
  [Method/results](validation/v1.24/simulation/README.md).
- **Website source:** eight exact D native FR/EN captures, the new Lab link,
  1,413 local source checks and scoped offline transport/runtime tests pass.
  575 existing payloads remain unchanged; no public payload is retired in 1.24.
  [Local source evidence](validation/site-v1.24/README.md). The coordinator
  separately recorded [local browser observations](validation/site-v1.24/browser-local/observations.json):
  FR/EN homepage at 320/1280, mobile report, Miko gallery/pose and audio start/stop.
  These are local observations, not public-origin acceptance.
- **Public delivery:** [original Hosting receipts](validation/site-v1.24/hosting/reprise-20261031/collection.json)
  bind source C2b to version `ded69bffe410a798`, release `1793441472461000` at
  `2026-10-31T10:11:12.461Z`. The 631-entry provider map preserves 577 historical
  hashes and serving config; 54 overlay uploads delete no payload. The separate
  [HTTP collection](validation/site-v1.24/public-origin/responses.json) passes all
  54 selected new/changed complete responses, 711,388,755 bytes including both D
  APKs. [Actual public browser checks](validation/site-v1.24/browser-public/receipt.json)
  cover FR/EN home at 1280×720 and 320×740, all eight native images loaded, Miko
  pose 18, one explicit audio start/stop and the mobile report/11 expanded raw links.
  Nine accepted JPEG captures and one failed viewport-setup attempt are kept
  separately; the initial FR header frame and later FR native frame are both preserved.

No host/emulator/synthetic result establishes human comprehension, emotion,
enjoyment, retention, physical-device comfort, production-ad fulfillment or real-money
commerce. The exact native formats, profile origins and preserved failed observer
attempts remain visible in the underlying reports.

## Delivery status and preserved history

- **Source/main and CI:** accepted at C2b above. Exact runs are
  [Cognitive Lab 41156960006](https://github.com/nabz0r/AndroidHit/actions/runs/41156960006)
  and [Puzzle 41156960012](https://github.com/nabz0r/AndroidHit/actions/runs/41156960012),
  with three successful jobs. This documentation closeout records the accepted
  source and CI; it does not change app/public payloads.
  C1 remains the immutable artifact-source object.
- **Firebase/public origin:** accepted by the distinct management, full-response
  and actual-browser receipts above. Hosting used exact C2b, rather than the later
  documentation-only main `4cf73bd9d3d65b865cde5a49376396d7e44751b8`.
- **Exact public artifacts:** [production](https://luma-drift-production.web.app/releases/luma-drift-1.24.0-rc2.apk),
  [separate Lab](https://luma-drift-production.web.app/releases/luma-drift-1.24.0-sandbox-rc2.apk)
  and [canonical manifest](https://luma-drift-production.web.app/releases/manifest-1.24.0-rc2.json)
  match the bytes/hashes above; the HTTP receipt verifies their complete bodies.

The earlier [AUTH_REQUIRED Cloud collection](validation/site-v1.24/hosting/pre-deploy-auth-failure/collection.json)
verifies 31 original files plus their original index. Its
[31,746-byte archive](validation/site-v1.24/hosting/pre-deploy-auth-failure/inbound.tgz)
has SHA-256 `a58d8ab07773f1ec182951582f7e89861a26f45196d9f462b782fc6027dad49d`.
The [handoff](validation/v1.24/publication/operator-preparation/c2b/auth-retry/handoff.json)
records that failed period, not the current deployment state. After authorization
was restored and the old VM expired, a fresh pinned stage reconstructed the same
D APK bytes and the reviewed finalizer completed. The successful collection has
33 originals plus their index; its
[91,661-byte archive](validation/site-v1.24/hosting/reprise-20261031/inbound.tgz)
has SHA-256 `21c16722f8d21b6925eec7e316baba7d83a2a88cef2047ba422c1feb01658d05`.
Both collections remain immutable. Credentials and account identifiers are absent
from the collected evidence.
Test ads, closed real purchases and the absence of a Play publication remain unchanged.
