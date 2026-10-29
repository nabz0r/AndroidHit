# Luma Drift 1.17 — a world at your fingertips

29 October 2026 · production **1.17.0-rc5**, isolated Lab
**1.17.0-sandbox-rc5**, Android code **23**.

**Frozen candidate G is under final qualification.** It fixes the real Guardian
Back loop reproduced during supplementary mode testing on F. Build/lint,
independent binary audit and the complete 23-command host rehearsal pass.
Native qualification on the exact G bytes is in progress. Canonical delivery,
Git main and the existing Firebase site remain gated by those results.

The three Guardian exits now persist the encounter, remove Guardian board/list
entries from menu history, and return without pushing the board again. A
refused save still blocks leaving. The fix also handles the empty-encounter
fallback. [Source review](validation/v1.17/review/source-candidate-g.json).
F's replay was deliberately stopped at **817 accepted levels**, not represented
as a complete run. Its [interruption receipt](validation/v1.17/relay/candidate-f-replay1000-a-interruption.json)
and original failures remain preserved.

G changes only `LumaView.java` and the release version relative to F. Its
[161-input freeze](validation/v1.17/candidate-g/source-freeze.json), SHA-256
`fa86b270c91797729e1d30344635f131d1865458666caf50b84f8c40f89d2e90`,
retains the other **159 inputs**, including all 28 engine sources, both audio
sources and all 23 original sprite PNGs. Source commit:
`44734f43646342ef62d045143992f7e4a4f8b597`.

## Audited G outputs — final delivery pending

| Output | Bytes | SHA-256 |
| --- | ---: | --- |
| Production APK | 71,827,197 | `f85c258341082462b51c6e6ddd466edc07b17b97044d72cfa27c39d20d2cd8a8` |
| Production AAB | 74,722,054 | `97a56d62a47cec81a4f775465db987629a1f6d02c7a99074f17b8b8abf40a385` |
| Isolated Lab APK | 71,826,884 | `5fe81532a4e5281a7efd7755b4a9e07b4fb910e6fbc865bf18b7b0c87d2a3f06` |
| Isolated Lab AAB | 74,713,860 | `914e520bf527eb24409953d1aa0e493e550da7907995407403221522dedf0dfc` |

[Build/lint](validation/v1.17/candidate-g/build.log) passes in 50 seconds.
[Independent audit](validation/v1.17/candidate-g/binaries/binary-final-candidate.json)
checks all four IDs, versions, code 23, min API 26, target API 36, existing
certificate `dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287`,
AAB signatures, pinned bundletool 1.18.1 and static 16 KiB ZIP/ELF alignment.
These are actual local outputs, not yet final public downloads.

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

## Current G evidence and remaining work

The [complete sparse rehearsal](validation/v1.17/ci-host-rehearsal-g-complete/summary.json)
passes **23 commands from exactly 160 files**. It covers the current U relay,
retained level families, 100 Guardians, P challenges, store/event boundaries,
root geometry, scene/input handling, sprites, DSP/lifecycle models and website
controllers. F's [supplementary legacy matrix](validation/v1.17/legacy-host-f/README.md)
explains finite catalogues versus sampled historical procedural seeds. Its
nine added suites were then rerun in the exact G rehearsal. This is local
host evidence; remote Actions remains separate.

The clone's actual [F→G upgrade](validation/v1.17/native-interface/candidate-g-upgrade/host-report.json)
passes **621 checks / 20 original PNGs**, including a genuine pre-update baseline
and cold process. The original [physical-copy receipt](validation/v1.17/secondary-clone/creation.json)
remains immutable; a separate [G authorization](validation/v1.17/secondary-clone/candidate-g-authorization.json)
permits only the retained F→G update on that same task clone. This is not a
second disk copy or a second global migration qualification. No preferences,
coins, unlocks, ownership or puzzle history are injected.

Current native work covers the three Guardian returns, other-mode HUD/help,
a real zero-reward P1 clear, fresh first wins/draws/cold persistence, all root
scenes/catalogues/outfits at normal and compact sizes, secondary menus, native
art and digital audio, and a new actual U1–U1000 replay on G. Primary browsing
covers six languages at both sizes; full secondary routes cover six compact
languages and normal EN/FR. No complete six-by-two secondary matrix is claimed.

The renderer gallery is a distinct scope: 144 companion/outfit compositions,
24 garments and 24 style samples. It does not prove game ownership or acquisition.
Actual native screenshots, pure-render sheets and analysis derivatives stay
separately labelled. Earlier B/C/E/F findings retain their original identity in
the [qualification register](validation/v1.17/README.md).

Before final publication, the exact G replay must independently establish
all 1,000 boards with three stars and zero repeated first-win coins on a
retained completed profile. Fresh eight-level victories are separate genuine
first wins. Native interfaces, native digital audio and visual review must
also complete. Four canonical files/manifests, sixteen exact G FR/EN website
captures, strict offline audit, clean Git/Firebase commits, remote CI and all
public HTTPS byte checks then receive their own receipts.

## Installation and measured limits

After acceptance, install production over the existing production app with
the same signing identity and retain application data. Lab has a separate
package/profile. Android 8/API 26+ remains the configured minimum. An older
active puzzle retains its original rules and action ledger.

Generated PCM exports are not phone recordings. Digital emulator audio checks,
physical speaker listening and competition with a real external focus owner
are separate scopes. SDK video capture cadence does not prove 60 fps phone
performance. Human comprehension, enjoyment, retention and future audience
size are not measured by these checks.

Test ads remain forced, real checkout and paid premium draws stay closed.
Google sign-in does not synchronize the local game/profile/home. This release
performs no Play upload or provider activation. [Installation register](RELEASE.md) ·
[Art/audio scope](ART_AUDIO.md) · [Website delivery register](validation/site-v1.17/README.md).
