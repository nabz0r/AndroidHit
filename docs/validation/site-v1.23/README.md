# Luma Drift 1.23 — public delivery

The [existing website](https://luma-drift-production.web.app/) serves production
`1.23.0-rc2` and separate Lab `1.23.0-sandbox-rc2`, code 31, from Hosting version
`cf8b817321bf33d5`. Source, native execution, HTTP identity and browser rendering
have distinct evidence below. This is a direct APK delivery, not a Play release.

## Source and qualification

Artifact source **C1** is `2db51f1f208d5a13c023f67df32450410b873c96`;
site source **C2** is `c99ac52903eca6707d3f4d644d7c480c79b63e68`.
Their [C1](../v1.23/publication/c1/git-publication.json) /
[C2 Git receipts](../v1.23/publication/c2/git-publication.json) and
[tree comparison](../v1.23/publication/c2/ci/input-delta-from-c1.json) preserve
identical app/public/scripts/workflow trees between them.

| Gate | Original evidence | Accepted scope |
| --- | --- | --- |
| Exact-commit CI | [C1](../v1.23/publication/c1/ci/receipt.json), [C2](../v1.23/publication/c2/ci/receipt.json) | Each: two workflows, three jobs, 51 reported steps including setup/cleanup |
| Native E | [Qualification](../v1.23/README.md) | 495 checks / 142 SDK touches; separate Lab 27 checks / four touches |
| Source and captures | [Prepared manifest](release/source-manifest.json) | Exact E APKs and eight original FR/EN native PNGs |
| Hosting | [Deployment](hosting/receipts/final-deployment/deployment.json), [provider map](hosting/receipts/final-deployment/verified-provider-files.json) | FINALIZED/live, 578 expected provider paths, serving config preserved |
| Public HTTP | [Original collector JSON](public-response.json) | 21 complete bodies / 753,990,693 bytes, plus 14 HTTP 404 checks |
| Public rendering | [Browser report](browser-public/README.md) | Actual FR/EN public-origin observations and captured states |

The [final cross-check](delivery-check.json) reconciles these separate receipts.

The [canonical APK manifest](../../../releases/manifest-1.23.0-rc2.json) binds
production SHA256 `429a443c9228787c7b854d36a40f0bcf7d52fdc8a3601214cd27b5cfae6945fb`
and Lab `099d4dc7d38cb4552b3b1ad46f7e3ccb70b304c7c8a1c6c332e16ada00ba558c`.
The published [synthetic report](https://luma-drift-production.web.app/lab/clarity-123/index.html)
retains ZIP SHA256 `07d35901a7f8ddf6b25f92e5bf8c41f7ecb3dadaaf55984720ac40cc909ba01e`.

## Hosting and public responses

The new version starts from accepted 1.22 C3 and preserves 557 historical provider
paths, replaces only the homepage, adds 20 paths and retires exactly 14 obsolete
binary copies. Its 578 provider paths comprise 576 physical source paths and two
reserved paths. The complete provider map and serving configuration were checked
before release. The live-version check is not an atomic compare-and-swap.

The [receipt import](hosting/inbound.json) preserves 22 original operation files
against the [export index](hosting/receipt-export-index.json). The downloaded
archive contained 74,002 bytes, SHA256
`c13e54fc6a7caa569e2746e4d1c4b83fad14497d672255bc22fb3a6cba5fb8b4`.
The original management receipt explicitly leaves public bytes/rendering false;
the separate HTTP and browser records establish those later gates.

The collector fetched the 21 new/changed bodies in full, including both APKs,
eight native captures and the source ZIP. It checked exact SHA256, size, MIME,
identity encoding and fixed HTTPS URLs, with no credentials, proxy, redirect or
fallback. The two APK bodies account for 737,988,213 of the 753,990,693 bytes.
All 14 retired paths returned HTTP 404 without reading their bodies: 35 requests
in total. The 555 unchanged physical responses were not fetched again; preservation
of their hashes belongs to the provider-map gate.

## Rendering and limits

[Public browser observations](browser-public/README.md) are separate from the
[local rehearsal](browser-local/README.md). Their original observations and JPEGs
record eight homepage states in FR/EN at viewport widths 320/375/736/1280:
40 loaded, visible native-image occurrences across eight unique PNG sources.
The lab covers four conditions, six language alternations and eight responsive
states, plus one additional FR320 bottom view. No horizontal overflow or browser
warning/error was recorded in those observations. The reviewer independently
inspected four JPEG samples; 17 homepage and nine lab JPEGs remain indexed.
Hoot form 4 / Jump rendered in the gallery. Four audio elements were observed
paused with autoplay off and preload none; playback was not tested.

The [study](../cognitive-lab-v123/README.md) executed 8,000 generated-agent
trajectories, 40,000 probes and 104 separate oracle controls. Its agents do not
read the artwork. Neither these technical gates nor the public browser review
establish human comprehension, emotion, enjoyment, retention, physical-phone
performance, Play approval or real commerce. Ads stay in test mode and real
purchases remain closed. [Cleanup accounting](../v1.23/cleanup/README.md) retains
the canonical artifacts and historical evidence without rewriting Git history.
