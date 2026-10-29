# 1.17 website staging

This is an unpublished template for the existing Firebase Hosting site
`luma-drift-production.web.app`. It does not establish APK acceptance, native
capture acceptance, visual browser acceptance or public deployment.
`public/index.html` remains on the previous edition until the final inputs pass.

The new French/English page uses “Un monde à toucher” / “A world at your
fingertips”, the four object-based game scenes, 24 companions, 24 garments,
42 furnishings and 50 arrangements. The four optional audio previews are exact
10-second renders from the app synthesizer at music gain 0.27, biome 0. They
are not recordings of phone playback. The provenance file records byte-exact
sources and the passing DSP report. They never autoplay.

## Completed staging checks

- `check-site-v117-staging.py`: 160 DOM, bilingual copy, references, audio and
  accessibility-name checks. Seven evidence placeholders are deliberately
  unresolved; `release_ready` is false.
- The current and previous edition controller tests both pass: 18 tests total.
  The shared script and styles preserve the previous homepage and legal pages.
- The final checker parser self-test passes. No final release audit or browser
  rendering claim has been made.

## Required final inputs

1. The signed production `1.17.0-rc1`, Android code 19, canonical APK/AAB/manifest,
   independent APK SHA-256 and its actual source-freeze JSON.
2. Eight genuine, full-size, unedited native screenshots in each of French and
   English: adventure, home, collection, atelier, profile, summon, reveal and
   badges. `capture-map.json` must index each original and a passing native
   report from the same exact final APK. Existing captures and mockups cannot
   substitute for these inputs.
3. A passing `docs/validation/v1.17/relay/summary.json` covering the real native
   touch replays of all 1,000 levels on that exact APK. Preparation reruns the
   relay summarizer and validates every referenced native batch and hash. It
   cannot publish the staged full1000 statement from a placeholder.

## Final local preparation and acceptance

Run `prepare-site-v117.py --apk-sha256 FINAL_SHA --source-freeze SOURCE_FREEZE
--check-only` first. Without `--check-only`, it copies the exact binary,
screenshots and audio and writes the entry HTML last. The input template has
no generated screenshot substitutes.

Then run `check-site-v117.py --apk-sha256 FINAL_SHA --source-commit ARTIFACT_COMMIT`
and inspect both French and English in an actual browser at widths 320, 375,
768 and 1280, including keyboard focus, reduced motion, gallery and audio
players. Retain screenshots and a browser report under this edition folder.

Publication is a separate root-agent step. `deploy-site-v117.py` requires an
explicit immutable clean source commit, the exact accepted APK, frozen input
hashes and the existing Firebase project. Its dry run is the default;
`--deploy` must be explicit. `verify-site-v117-publication.py` subsequently
compares public HTTPS responses and aliases to the accepted deployment receipt.
No deployment was performed by this staging task.

The privacy page, deletion flow, public configuration, commerce qualification
gates and Firebase configuration are retained byte for byte and audited again
by the final checker.
