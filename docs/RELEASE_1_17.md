# Luma Drift 1.17 — a world at your fingertips

29 October 2026 · production **1.17.0-rc6**, isolated Lab **1.17.0-sandbox-rc6**, Android code **24**.

**H passes exact native acceptance and canonical package integrity.** The signed [production APK](../releases/luma-drift-1.17.0-rc6.apk), [AAB](../releases/luma-drift-1.17.0-rc6.aab), [Lab APK](../releases/luma-drift-1.17.0-sandbox-rc6.apk) and [Lab AAB](../releases/luma-drift-1.17.0-sandbox-rc6.aab) match all four audited hashes below. Git main, public downloads and Firebase delivery have separate publication receipts.

H closes two reproduced defects: Guardian exits no longer push the encounter back onto menu history; legacy puzzle gates no longer cover the move budget or instructions. Daily/Weekly previews now show their actual mode and stage. `PlaySceneLayout` supplies the live board, gates, reading bands and action targets. The board keeps its safe width in short windows, nominal gate/action targets retain48dp, real scrolling reveals the complete footer, and measured copy is followed by three compact object/caption columns or a fitting full power grid. Real scrolling reaches the last action; Help, Pause, powers and victory stay anchored to the visible window.

The [162-input freeze](validation/v1.17/candidate-h/source-freeze.json) is SHA-256 `b6af495fadab8c20cf6379b591297da38e10edb4b694aad0adcf521f75711b88`. Source commit: `bb622960f52e5ba41e75a6dfa8b30dbb4b98a291`. H retains159 G inputs; only the view/release version change and one geometry helper is added. All28 engine sources, both audio sources and all23 original art PNGs remain byte-identical. [Exact comparison](validation/v1.17/candidate-h/change-from-g.json).

## Exact audited H outputs

| Output | Bytes | SHA-256 |
| --- | ---: | --- |
| Production APK | 71,827,197 | `15546854a856ecc5052aa79acdd49724439b2082fd638da68cf7747be1924a47` |
| Production AAB | 74,724,470 | `cfad9ff262e843ef0a2c7982964ff7ca41abf0e6df57cefa2c7c6b3d7d4e799b` |
| Isolated Lab APK | 71,826,884 | `0cc905def9679c6c4e3b63b90f454137abf6ad9bc431f86e9a7d175891cbde4b` |
| Isolated Lab AAB | 74,716,146 | `034317b1af295cfc9f2acb9c621f446218bc9e5967182bddf2af1be0b40e8e75` |

[Build/lint](validation/v1.17/candidate-h/build.log) passes in 49.014seconds. The [independent four-package audit](validation/v1.17/candidate-h/binaries/binary-final-candidate.json) checks IDs, versions/code24, minAPI26, targetAPI36, existing certificate `dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287`, AAB signatures, pinned bundletool1.18.1 and static16KiB ZIP/ELF alignment. Package integrity is separate from native runtime acceptance and public delivery.

## What changes

Four illustrated places—Adventure, Home, Collection and Workshop—use touchable
scene objects and free captions instead of visible game button faces, pills,
tabs and a bottom navigation bar. A portal starts/resumes play; books, mirrors,
wardrobe rails, capsules, entrances and blueprints lead to their corresponding
tasks. Optional root swipes supplement named objects. Costs, odds, ownership,
confirmation meanings and semantic/keyboard actions remain explicit.

The bundled art supplies original sprites for **24 companions, 24 garments and
42 furnishings**, three new place illustrations and room materials. The same
portrait composes body, hat, dress, paired shoes and gloves across the game.
Species-specific rig metadata separates anatomy from existing saved item IDs.
The three **10 × 10 rooms**, close-up/whole-room views, pan, pinch, local nickname,
chosen flag, six earned badges and three badge slots are retained.

Scene presentation uses a bounded **220 ms crossfade**, input guards and immediate
completion for reduced motion or lifecycle interruption. Rendering and
transitions do not repeat navigation, drawing or store mutations.

Five contexts across ten biomes supply **50 synthesized arrangements**: ten
historical puzzle themes plus forty place-specific arrangements. Three new
scene/fabric/reveal earcons join the eleven preserved effects. Music and effects
remain independently controlled. [Art and actual DSP previews](ART_AUDIO.md).

The U1–U1000 rules and existing saves remain. Piece exits control doors; repeated
Undo is free. Companion arrivals retain 23 possible companions for 180 earned
LumaCoins, excluding free Nova. Five complete outfits cost 260 earned coins per
arrival. Fixed odds, duplicate returns, recoverable receipts, existing direct
purchases, individual clothing capsules and twelve free styles remain.

## Acceptance and delivery

**U1–U1000 pass on this exact production APK: 49,688 native checks, 12,017 actual touch gestures and 10,974 certified piece exits.** This retained profile already owned all stars; replay adds zero coins, XP or mission credit. Complete game/meta and non-active-session state remain byte-identical. A separate genuine new install covers U1–U8 first wins, onboarding, actual Buzz/Aurora acquisition and cold persistence.

The [independent final index](validation/v1.17/acceptance-h.json) verifies every accepted report token and original indexed image hash against H. Primary browsing covers six languages in both sizes; secondary browsing covers six compact languages and normal EN/FR. The [qualification register](validation/v1.17/README.md) keeps each count and scope separate. F stopped at817 certified levels and G at101; their failures and original receipts remain immutable.

The source/clone/fresh test roles stay distinct. Retained5586 owns sound/replay; the same physical task clone5592 owns UI/art, authorized through creation→G→H receipts; a never-existing H AVD5590 owns first install and real first wins/draws. No progression, coins, possessions or histories are seeded. The clone is not a second migration qualification. Main/compact primary browsing covers six languages; secondary browsing has explicit six-compact and normal EN/FR scopes.

The pure-render gallery covers144 companion/look compositions,24 garments and24 style samples. It proves rendering separately from ownership. Actual UI routes equip only possessions earned by the QA save. Generated PCM exports, Android digital playback and physical-speaker listening are distinct evidence classes.

## Public delivery

The four canonical packages are on [Git main](https://github.com/nabz0r/AndroidHit) and their [independent immutable public downloads](validation/v1.17/publication.json) match293,094,697 total bytes and all four hashes. The remote [contract workflow](https://github.com/nabz0r/AndroidHit/actions/runs/40834726178) passes on the deployed source.

The existing [website and native gallery](https://luma-drift-production.web.app/#screens) is deployed from clean `fb341930fc7820dcb19326ee4c0c354f3101f610`; Hosting `a8f7c1e30f14cd29` finalized **2026-10-29T07:19:42.453Z**. Its174 payloads/3aliases match exact public bytes. Eight responsive FR/EN states, localized gallery, keyboard focus and four deliberate10-second audio playbacks pass. [Delivery register](validation/site-v1.17/README.md). The [direct APK](https://luma-drift-production.web.app/releases/luma-drift-1.17.0-rc6.apk) is the exact accepted H file.

## Installation and measured limits

Install production over the existing same-signed production app and retain application data. Lab has a separate package/profile. Android8/API26+ remains configured; older active puzzles retain their actual original rules and action ledger.

Music and effects are independent. Five contexts across ten biomes are tested as synthesized output; native lifecycle/track observations have their own records. Physical speaker listening, real external audio-focus competition, human comprehension/retention and real-phone performance are not established by emulator or host checks. SDK video capture does not certify60fps.

Test ads remain forced, real checkout and paid premium draws stay closed. Google sign-in does not synchronize the local game/profile/home. No Play upload or provider activation is part of this delivery. [Installation register](RELEASE.md) · [Art/audio](ART_AUDIO.md) · [Website/publication register](validation/site-v1.17/README.md).
