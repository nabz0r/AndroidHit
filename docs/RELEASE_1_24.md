# 1.24 — companion growth and Pulse presentation

31 October 2026. **Candidate D is locally built and qualified within the scopes
below; publication is pending.** Production is `1.24.0-rc2`, Lab is
`1.24.0-sandbox-rc2`, both code 33. [Qualification register](validation/v1.24/README.md)
contains the original evidence links and open gates.

The [verified artifact source object](validation/v1.24/publication/c1/git-publication.json)
is `787fba142b8edd1f840b9b70cb7749167edacd24`, tree
`8a07f1f8d0808d229cb6134056330a0eb6864a8b`. Its 522 native build inputs match D.
The receipt explicitly records that main was not updated at this stage. This
source object is not a website deployment or a completed release publication.

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
  Actual ad display, early close and earned +35 remain **unqualified**. Four
  stopped phases stay outside the five-phase totals. Ads opt-in was revoked and
  Lab 5634/5638 stopped normally with data preserved. The
  [final Lab index](validation/v1.24/native-lab/index.json) joins retained and
  fresh: 11 PASS phases, 568 assertions/118 touches/39 PNGs; 522 app inputs remain
  unchanged. [Fresh summary](validation/v1.24/native-lab/fresh-d-summary.json).
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

No host/emulator/synthetic result establishes human comprehension, emotion,
enjoyment, retention, physical-device comfort, real ad fulfillment or real-money
commerce. The exact native formats, profile origins and preserved failed observer
attempts remain visible in the underlying reports.

## Remaining closeout fields

- **Final source/main and CI:** pending the final source commit/tree and exact
  workflow/job outcomes. C1 above is the artifact-source object only.
- **Firebase and public origin:** pending exact Hosting version/release/config,
  provider-map preservation, complete public response hashes and actual public
  FR/EN browser records. The last verified live release remains 1.23 at
  `cf8b817321bf33d5`; local 1.24 source application does not replace that evidence.

These fields are deliberately open for the coordinator's final receipts. Test
ads, closed real purchases and the absence of a Play publication remain unchanged.
