# Luma Drift 1.17 website delivery register

**Delivered: H (1.17.0-rc6 / code 24), four exact signed packages, documentation and source on Git main, and the existing Firebase website. Native acceptance, public download hashes, responsive FR/EN browser states and deliberate audio playback pass within their recorded scopes. Earlier candidates retain their original historical results.**

**Delivered H.** The existing [Firebase site](https://luma-drift-production.web.app/#screens) is live from clean source `fb341930fc7820dcb19326ee4c0c354f3101f610`, public tree `dcaf3a3b090d0808529a3533e84d43411596b6dc`. Hosting version `a8f7c1e30f14cd29` is DEPLOY/FINALIZED at **2026-10-29T07:19:42.453Z**. Native H passes all1,000 retained replay boards and the complete scoped screen/art/audio/fresh routes before public preparation.

| Scope | Result | Evidence |
| --- | --- | --- |
| Offline HTML/DOM/references/source/binary checks | PASS3261assertions,3HTMLpages,91localreferences | [Static audit](release/static-check.json) |
| Exact Hosting payload set |174files /306,306,983bytes from clean source | [Frozen source manifest](release/source-manifest.json), [deployment](deployment.json), [management](live-management.json) |
| Independent public HTTPS | PASS174payloads plus3directoryaliases,HTTP200/exactbytes/SHA-256 | [Public origin](public-origin.json) |
| Actual public responsive browser | PASS8FR/ENstates, widths320/375/768/1280, localized gallery/nohorizontaloverflow, visible keyboard focus | [Browser report](browser-public/report.json) |
| Deliberate browser audio | PASS4actual10second resources, noautoplay, oneplayeratatime; allpausedafterward | [Raw observations](browser-public/audio.json) |
| Production and isolatedLab Git downloads | PASS4exactAPKs/AABs,293,094,697totalbytes | [Immutable Git publication](../v1.17/publication.json) |

The [H capture map](capture-map.json) and [provenance](screenshot-provenance.json) identify16 original1080×2340PNG files, copied byte for byte from the passing normal277check/177PNG six-language run. They are native application captures, separate from isolated renderer sheets. The independent visual review directly inspects all16 site sources within its97-original H subset. F’s map and localhost preview remain historical below.

The four optional [audio previews](audio-provenance.json) are10second actual synthesizer exports, biome0/defaultgain0.27, with a sample ending fade. They are not physical phone recordings. The page uses ordinary text links and accessible native web media controls. Music/effects/native lifecycle/physical listening retain their distinct evidence scopes. No account/provider/Play operation is performed by this delivery.

## Preserved exact F preview

[Archived F capture map](capture-map-candidate-f.json) indexes **sixteen unedited 1080×2340 native
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

## Reproduce the prepared source and delivery checks

1. Accepted production **1.17.0-rc6 / code24**, four canonical production/Lab
   APK/AAB files, manifests, independent hashes and the actual 162-input H freeze.
2. The sixteen final H captures in the current map, each indexed by a passing H native
   report. The public-copy process preserves all original PNG bytes.
3. A passing `docs/validation/v1.17/relay/summary.json` covering actual native
   touches through all1,000 boards on the same H APK, with zero repeated
   first-win coins. Preparation reruns the independent summarizer and checks
   every native batch and hash. No placeholder can satisfy this gate.

Run `prepare-site-v117.py --apk-sha256 FINAL_SHA --source-freeze SOURCE_FREEZE
--check-only` first, then without `--check-only`. It copies exact releases,
images and audio, and writes the entry HTML last.

Run `check-site-v117.py --apk-sha256 FINAL_SHA --source-commit ARTIFACT_COMMIT`.
That audit freezes all public payloads, original inputs, native reports,
release manifests and checker sources. It verifies code24 and the immutable
artifact commit, independently from the browser preview.

`deploy-site-v117.py` requires an explicit clean immutable commit, exact
accepted APK, frozen input hashes and the existing Firebase project.
Dry run is the default; publication uses explicit `--deploy`. Its receipt
stays outside the checkout until delivery is complete. Then
`verify-site-v117-publication.py` compares every public HTTPS payload plus
three directory aliases with that source receipt. Actual public FR/EN
responsive, keyboard, gallery and audio browser acceptance is recorded separately above.

The privacy page, account-deletion flow, configuration, commercial gates and
Firebase setup retain their original bytes, checked by the final audit.
No Play upload, purchase or provider activation is inferred from Hosting.
