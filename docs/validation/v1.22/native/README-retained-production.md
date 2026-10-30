# Native 1.22 — production, retained profile

Candidate C passes the retained-profile checks below on the task emulator
`emulator-5602`, `Luma119-Battle-QA`. These are actual native touchscreen
gestures, Android accessibility observations, original screenshots and
read-only saved-state checks. No profile was injected or cleared; the game was
never uninstalled. No ownership, progress, currency, account or purchase was
manufactured.

The machine-readable [accepted manifest](retained-production-accepted.json)
lists exact candidate hashes, report hashes and per-phase totals. Candidate B
and C executions remain separate.

| Candidate | Accepted phases | Assertions | Actual gestures | Original PNG | Original videos |
| --- | ---: | ---: | ---: | ---: | ---: |
| B | 6 | 1,505 | 138 | 34 | 6 |
| C | 4 | 417 | 96 | 31 | 4 |

An assertion count is not a count of unique product requirements. Screen
recordings cover their explicitly bounded first 25–60 seconds; the full native
gesture program is recorded in each report and `requested-steps.json`.

## Final C observations

- `candidate-c-tactical-cold-claimed`: a separate process after B→C upgrade
  preserves the actual chapter codec and the claimed reward. Coins remain
  1,007; reopening grants no second reward.
- `candidate-c-site-media-fr-en-retry`: actual home, world map, campaign U42
  key/lock inspection and the loaned tactical trio in French and English.
  The [eight canonical media](candidate-c-canonical-media.json) were visually
  inspected and retain their original pixels and hashes.
- `candidate-c-retained-world-keys-fr-retry`: actual camera pan, changed
  observed node positions, two-pointer pinch, minimap inspection and Follow;
  selecting a locked node cannot enter it. Map exploration and walking do not
  debit energy or change campaign progress, ownership or board history. The
  three original walking images show Nova at U35, moving between U39/U40, then
  arrived at U41; camera and minimap correspond. The actual Play flow enters
  U42. Exits 0, 1 and 2 release key 1 and open its associated lock; a real Undo
  restores the key and lock.
- `candidate-c-compact-reduced-fr-en`: 720×1280, density 320, font scale 1.3.
  French/English home and map retain readable primary controls, accessible Play
  and a bounded minimap. With reduced motion enabled through Settings, the
  lock is absent immediately after the real key exit and remains open. Two map
  captures separated by an explicit 1,600 ms wait contain **zero different RGB
  pixels** in the game rectangle, excluding Android status/gesture bars; see
  the [read-only pixel comparison](candidate-c-reduced-motion-pixel-comparison.json).

Reduced motion was restored to `false` through the actual Settings UI. The
[display receipt](display-candidate-c-compact-restore.json) proves exact
restoration to the original 1080×2400, density 420 and font scale 1.0. The task
emulator was then explicitly handed to sandbox QA; this observer owner made no
further device changes.

The coordinator separately tested C first installation, locked Remix in the
short-window fallback, scrolling and cold English on `emulator-5626`: 51
assertions, 19 gestures and 12 PNG. Those results are excluded from the retained
totals above. See `candidate-c-fresh-*` and `final-fresh-device` for that scope.

## Earlier B engine evidence

The two world language phases and the corrected key/Undo phase pass on B.
The three distinct chapter phases execute the real four-board KEYS and RELAYS
routes using public gesture certificates, not target model calls:

- Loaned Nova/Hoot/Orbit remain unowned where initially unowned.
- Shared reserve starts at six. GUIDE costs two, REWIND costs two and FLOW costs
  three; an already-used talent cannot spend again.
- Manual Undo does not refund previously spent charge.
- A separate process restores the actual in-progress chapter byte for byte.
- Each completed four-board episode grants exactly 120 Coins: 767→887→1,007.
  Ordinary campaign session energy is not debited by tactical play.

These runs remain labeled B even though the coordinator's B→C binding shows
the same chapter engine/session sources. C has its own cold claimed-state and
native rendering checks above.

## Observer binding and retained failures

The SDK-only observer SHA is
`de0c47173b4268f0d0b81c17c6af7afea600eb6cc691a3590249de495bf3eb59`.
The [compilation receipt](../observer-builds/production-de0c4717.json) and
[source audit](retained-observer-source-audit.json) bind all **64 compiled
inputs**—manifest, Java and packaged fixture assets—to the source archive of
each of the ten accepted B/C phases. Runtime host gesture JSON is recorded
separately and was compared with the actual native requested program. No target
classes or solver are imported, and no target preference is written by the
observer.

All failed and preliminary evidence remains unchanged:

- `candidate-b-world-fr` is excluded because its preliminary archived Java did
  not match the older installed observer. `candidate-b-world-fr-final` is the
  accepted rerun with matching compiled sources.
- `candidate-a-keys-fr` failed at the test's Undo label regex; the corrected B
  and C runs execute and verify the actual Undo.
- `candidate-c-site-media-fr-en` failed at test navigation: Android Back during
  active campaign play only pauses/resumes. The accepted retry uses the actual
  pause control then “Leave level” / “Quitter le niveau”.
- `candidate-c-retained-world-keys-fr` failed before any gesture because an
  unnecessary test preamble assumed the previous screen. Each runner starts a
  fresh Adventure Activity; the corrected retry passed.

Candidate A migration/first-launch preservation and world phases remain
historical A evidence and are not relabeled as final C.

Native world gestures cover the first sector, with the retained U45 access and
an actual U42 board. Chapter completion covers the eight stages chosen across
the two episodes, not all twelve catalog layouts or every route combination.
The certificates supply the QA move sequence; they are not a limited-perception
player model. Exhaustive host geometry/model checks are a separate scope.

This acceptance concerns emulator rendering, gestures and saved-state behavior.
It does not measure human understanding, enjoyment or retention, physical-phone
performance, or store approval.
