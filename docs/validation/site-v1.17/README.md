# Luma Drift 1.17 website delivery register

**Status: F preview accepted locally; final publication pending.** The existing
Firebase Hosting site is `luma-drift-production.web.app`. Its public entry page
still serves 1.16 until the final F replay/canonical-package gates pass.
This directory keeps preview, offline audit, deployment and public-browser
results separate.

The FR/EN page presents the four illustrated places, 24 original companions,
24 garments, 42 furnishings and 50 arrangements. It uses ordinary text links
and language controls with visible keyboard focus. The game's no-visible-button
contract does not remove accessible web audio controls.

## Completed exact F preview

[Capture map](capture-map.json) indexes **sixteen unedited 1080×2340 native
F screenshots**, eight destinations in each FR/EN. They come from the passing
normal six-language route on the physical task-QA clone, with the same retained
profile and exact production hash
`fe556ae6f0420f41afb7e5db044ef925fe79d0695f0ac6190d6870daf2e8d5a8`.
The clone receipt and passing native report preserve attribution; these are
actual app captures, not mockups or pure-render sheets.

The unpublished [F browser report](browser-preview-f/report.json) covers
FR/EN at widths **320, 375, 768 and 1280**, localized gallery sources and names,
no horizontal overflow, visible keyboard focus and four deliberate audio
playbacks. Media are stopped and the viewport override is reset afterward.
The [preview inputs](preview-f-inputs.json) identify the scratch HTML/assets;
the preview explicitly withheld the unfinished full1,000 acceptance claim.
It is not a release acceptance, Firebase receipt or public-browser report.

Four optional [audio previews](audio-provenance.json) are exact 10-second
exports from the app synthesizer, biome 0, gain 0.27. They are not phone
recordings and never autoplay. Browser playbacks verify the actual resources,
duration and one-player-at-a-time behavior; digital Android playback has its
own native record.

Earlier staging passes 160 DOM/bilingual/reference/audio/accessibility-name
checks and all 18 current/previous controller tests. The final parser self-test
also passes. Historical preview E remains separately labelled and does not
supply F acceptance.

## Final required inputs and procedure

1. Accepted production **1.17.0-rc4 / code22**, four canonical production/Lab
   APK/AAB files, manifests, independent hashes and the actual 161-input F freeze.
2. The sixteen exact captures in the map, each indexed by a passing F native
   report. The public-copy process preserves all original PNG bytes.
3. A passing `docs/validation/v1.17/relay/summary.json` covering actual native
   touches through all1,000 boards on the same F APK, with zero repeated
   first-win coins. Preparation reruns the independent summarizer and checks
   every native batch and hash. No placeholder can satisfy this gate.

Run `prepare-site-v117.py --apk-sha256 FINAL_SHA --source-freeze SOURCE_FREEZE
--check-only` first, then without `--check-only`. It copies exact releases,
images and audio, and writes the entry HTML last.

Run `check-site-v117.py --apk-sha256 FINAL_SHA --source-commit ARTIFACT_COMMIT`.
That audit freezes all public payloads, original inputs, native reports,
release manifests and checker sources. It verifies code22 and the immutable
artifact commit, independently from the browser preview.

`deploy-site-v117.py` requires an explicit clean immutable commit, exact
accepted APK, frozen input hashes and the existing Firebase project.
Dry run is the default; publication uses explicit `--deploy`. Its receipt
stays outside the checkout until delivery is complete. Then
`verify-site-v117-publication.py` compares every public HTTPS payload plus
three directory aliases with that source receipt. Actual public FR/EN
responsive, keyboard, gallery and audio browser acceptance follows.

The privacy page, account-deletion flow, configuration, commercial gates and
Firebase setup retain their original bytes, checked by the final audit.
No Play upload, purchase or provider activation is inferred from Hosting.
