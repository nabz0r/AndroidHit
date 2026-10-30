# 1.22 website source and acceptance

The initial website source and local browser evidence are published at
[C2 `13ebe82be9b57569df50627449cc748f5245521c`](https://github.com/nabz0r/AndroidHit/commit/13ebe82be9b57569df50627449cc748f5245521c),
tree `5def59f208af92ac2dd3d7da850daddaa849ac11`. Its Android artifact metadata
comes from [C1 `716bf73066892d034510db80630cd1f227738372`](https://github.com/nabz0r/AndroidHit/commit/716bf73066892d034510db80630cd1f227738372).
The existing Firebase destination is `luma-drift-production`; this release does
not create a second site. The later public archive-switch failure is corrected
by the three-file update published at
[C3 `b122c0acbd172a7912c69b0b6914b8b0f9b2a243`](https://github.com/nabz0r/AndroidHit/commit/b122c0acbd172a7912c69b0b6914b8b0f9b2a243),
tree `9ab8c8440104138d779a45e7e897d20ce523091c`. Exact C3 is deployed as
Firebase version `24e34cf6e6af1ed8`, with independent public byte and browser
receipts. Its public source tree is `cf95386ee10d4c6ff4117cf419e3ad02a8b4cc0c`.

| Scope | Recorded state |
| --- | --- |
| Exact C production/Lab Android acceptance | Passed; [native register](../v1.22/README.md) |
| C3 source and hosted CI | Published; two workflows, three jobs and 47 steps SUCCESS, attempt 1 |
| Offline and local browser | Passed, with separate receipts below |
| C3 authenticated Hosting deployment | DEPLOY/FINALIZED, version `24e34cf6e6af1ed8` |
| Public response-byte chain | C2: 35 complete bodies; C3: three changed bodies; provider preservation PASS |
| Public corrected lab | 15 checks, including eight responsive states and rapid FR/EN reversals PASS |
| Public homepage and demo | Accepted on unchanged C2/C3 inputs; final C3 homepage revisited |
| Historical C2 archive race | FAIL retained, excluded from final acceptance |

Source publication, loopback rendering and Hosting management are different
observations. None substitutes for the other rows.

## Exact inputs and preservation

[Preparation](preparation.json) and the [source manifest](release/source-manifest.json)
bind production `1.22.0-rc1`, Lab `1.22.0-sandbox-rc1`, code 30, and the eight
original 1080×2400 FR/EN Android PNGs accepted for production candidate C. The
signed APKs remain ignored external files, with exact bytes, package identities
and signer in the [canonical artifact manifest](../../../releases/manifest-1.22.0-rc1.json).
They are not required as Git blobs. Capture selection and the actual passing
native report are recorded in the [capture map](staging/candidate-c-capture-map.json).
No A/B or failed-phase image is relabelled as C.

The committed C2 source manifest has SHA-256
`414444c62f2ce925d9e53974ca44d72866e4f81ee36047421f11158a77ae5ff5`.
Its [comparison](prepared-source-diff.json) records **570 physical payloads /
4,393,037,761 bytes**. Against the historical 539-file baseline, 535 files remain
unchanged, four change intentionally, 31 are added and none removed. The four
updates are `/index.html`, `/lab/cognitive/index.html`, `/lab/cognitive/lab.js`
and `/lab/cognitive/lab.css`. Gallery/audio markup, old APKs, prior lab data/ZIP,
privacy and account/authentication routes remain preserved.

The current [delivered C3 manifest](release/source-manifest.json) has
SHA-256 `b3f612758736370b15b47ebc4a72eb6a95fedf8342cb67fe3429f967d2549995`.
Its [exact committed-C2 comparison](incremental-source-diff.json) retains all 570
physical paths: **567 unchanged, three changed, no addition or removal**. The
changes are only lab JavaScript, its HTML cache key and the current source ZIP.
The new total is 4,393,039,737 bytes. Homepage, Android APKs, native PNGs, dataset
bytes and snapshots stay exact; no Android build or experiment rerun is involved.

The new lab keeps 21,000 synthetic campaign trajectories, 84,000 assistance-free
probes and 54,000 separate tactical runs distinct. Its [versioned report](../cognitive-lab-v122/README.md)
states zero human participants and records the final source binding and archive.
The browser [map/key demo source](../../../public/lab/ui-1-22/index.html) uses
fictional state and changes no Android save, ownership, reward or payment.

## Offline and actual local browser evidence

[Source QA](source-qa-prepared.json) passes **1,248** offline link, payload,
preservation and native-provenance checks. [Runtime tests](runtime-tests-prepared.json)
pass **87** JavaScript tests across the actual 1.22 homepage/demo and preserved
historical fixtures; the [original test output](runtime-tests-prepared.txt)
retains their names and scope. [Transport fixtures](transport-tests-prepared.json)
separately pass six REST-state, eight sparse-staging and eighteen byte-delta
tests. These fixtures neither contact Firebase nor fetch real public APKs.

The C2 [actual browser register](browser-local/report.json) binds the reviewed public
directory and source hashes. It records **24 responsive states**: homepage, lab
and demo, each in FR/EN at 320, 375, 768 and 1280 widths. Its **20 original CUA
JPEG screenshots** retain the returned bytes and hashes; they are local browser
captures, separate from the eight native Android originals.

- [Homepage](browser-local/homepage/report.json): **40 individually visible native-image
  loads** across the eight states, involving the eight original PNGs; correct
  locale/title/download association; Hoot's fourth form and jump; and a deliberately
  started audio preview observed playing for **6.24 seconds**, without autoplay.
- [Lab](browser-local/lab/report.json): current 1.22 and historical 1.21 selection,
  seven current scenarios, and execution→control switches with their actual
  mechanics/probe counters updated.
- [Schematic demo](browser-local/demo/report.json): destination travel, minimap
  exploration, recentering, explicit key A activation and separate key B selection.

No warnings or errors were observed in the recorded local browser scopes.
These results do not establish public-origin rendering, phone-speaker quality,
physical-phone comfort, human understanding, enjoyment or retention.

C3 separately passes [1,248 source checks](source-qa-incremental.json),
[87 site/fixture tests](runtime-tests-incremental.json) and
[47 offline transport tests](transport-tests-incremental.json): twelve deployment,
nine collector, eight staging and eighteen byte-delta tests. Provider doubles
verify retention of 572 paths, the exact C2 baseline, refusal of unrelated changes
and the 600-second timeout for uploads alone. Their APK-free overlay test is scoped to
`rest_deploy`; the complete CLI retains its physical APK identity gate and reuses
the exact local C2 APK copies. Lab's asynchronous switching tests and source-kit
checks are retained in [the versioned lab evidence](../cognitive-lab-v122/async-version-switch-fix/).

The source-bound [actual local correction review](browser-local/lab-racefix/report.json)
passes ready 7→3→7 scenario-count transitions, three rapid reversals, FR/EN and
the current ZIP selection, with no observed browser errors. This remains loopback
evidence, separate from the public reports below. Exact C3 also passes
[hosted CI](../v1.22/publication/c3/ci/receipt.json): **two workflows, three jobs,
47 successful steps**, first attempt, with three original complete logs and no
rerun. Runs are [41081153263](https://github.com/nabz0r/AndroidHit/actions/runs/41081153263)
and [41081153386](https://github.com/nabz0r/AndroidHit/actions/runs/41081153386).

## Deployment and independent public checks

The original [C2 management receipt](hosting/c2/qualified-600s/deployment.json)
records exact source `13ebe82be9b57569df50627449cc748f5245521c`, version
`990135f689f144c0`, release `1793372275660000` at
`2026-10-30T14:57:55.660Z`, and **572 provider paths**. All retained historical
provider hashes and serving configuration are preserved; 27 overlay blobs upload,
with eight provider cache hits. The earlier 60-second transport failure and
timeout-only operator are kept with the [original C2 remote evidence](hosting/c2/).
The successful attempt uses a bounded 600-second upload budget. No 1.21 base APK
is downloaded publicly during staging.

The independent [original C2 response receipt](public-origin-c2.json) is PASS:
**35 complete bodies /803,549,445 bytes**, including both signed APKs, the ZIP
and six gzip datasets. Its hashes match the committed C2 source. This proves the
selected response bytes, not all 570 raw responses or browser behavior.

The later [public failure receipt](browser-public/lab/archive-race-c2-failure.json)
records the current selector paired with completed archive 9,000/three-scenario
data after rapid archive→current selection. It remains FAIL. Byte correctness and
the earlier loopback checks do not erase this additional interaction failure.

The independent [public byte collector](../../../scripts/collect-site-v122-public-bytes.py)
retains its original 35-response mode. Its new exact
`--previous-source-commit 13ebe82be9b57569df50627449cc748f5245521c` mode selects
only the correction's **three actual changed bodies /32,426,083 bytes**. It
refuses other paths, additions, deletions or Android/native/config identity
changes, and records the previous source manifest's SHA-256 in the new receipt.
The unchanged APKs/CSV need no second download. Nine offline collector tests pass.
Only its explicit `--fetch` mode requests the fixed HTTPS origin; it sends no
credentials, disables proxies and follows no redirect, retry or alternative route.
Every body is streamed through exact size/SHA-256 checks, with HTTP status,
content type and identity encoding recorded. An access/security refusal stops the
entire collection; a partial receipt cannot become PASS.

The original [C3 management receipt](hosting/c3/deployment/deployment.json)
records source `b122c0acbd172a7912c69b0b6914b8b0f9b2a243`, version
`24e34cf6e6af1ed8`, release `1793374323071000` at
`2026-10-30T15:32:03.071Z`. It preserves all 572 provider paths and configuration,
with **569 provider hashes unchanged and three uploaded blobs**. The
[download collection record](hosting/c3/collection-receipt.json) and
[original index](hosting/c3/original-index.json) retain the 12 original remote
receipts, their archive and checked hashes without rewriting their contents.

The [original C3 public response receipt](public-origin-c3.json) is PASS for the
three complete corrected bodies, **32,426,083 bytes**, tied to the previous C2
manifest. The [final byte-evidence bridge](public-origin-final-bridge.json) joins
those observations to C2's original 35 bodies and the exact 567 raw/569 provider
identities retained across C3. It does not label all 35 as fresh C3 downloads.
The original response receipts remain separate from the
[offline receipt validator](../../../scripts/verify-site-v122-publication.py),
which makes no requests itself.

## Actual public rendering and interactions

The [final public browser register](browser-public/final-acceptance.json) links
the accepted observations and excludes the historical C2 archive race:

- [Corrected lab at C3](browser-public/lab-racefix/report.json): **15 checks**,
  including current seven scenarios/21,000 trajectories/18 chapter rows,
  archive three/9,000/zero, current ZIP identity, three rapid FR/EN reversals,
  eight ready responsive states, final settled current data and no observed
  warnings/errors.
- [Public homepage](browser-public/homepage/report.json): eight FR/EN states and
  40 individually visible native-image loads observed at C2, with all relevant
  homepage/native inputs unchanged in C3. The [final C3 desktop revisit](browser-public/homepage/final-c3-revisit.json)
  separately confirms the fully loaded 1080×2400 source image.
- [Public schematic demo](browser-public/demo/report.json): seven interactions
  and eight responsive states, observed while C2 served its byte-identical C3
  demo sources. Destination travel, independent camera/minimap, recentering,
  locked refusal and explicit key behavior are covered.

The four public [lab JPEGs](browser-public/lab-racefix/) and four public
[demo JPEGs](browser-public/demo/) at FR/EN 320/1280 were also visually reviewed
without editing. Text and controls are readable; no overlap or horizontal
clipping is visible in those eight frames. Expected vertical scrolling at mobile
widths remains. This static visual review complements the interaction receipts.

Play publication and live provider transactions retain their separate
qualification gates.
