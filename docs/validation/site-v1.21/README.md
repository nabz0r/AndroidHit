# Website 1.21 preparation

The existing Firebase site remains the publication destination. This directory stages the 1.21 homepage, its independent browser simulation and exact-byte release workflow. No deployment is performed by preparation or local tests. The prepared [source manifest](release/source-manifest.json) now binds artifact metadata commit [`35bb8222`](https://github.com/nabz0r/AndroidHit/commit/35bb82229d8175968919b5979264e5094a1cc8dc), 525 physical Hosting payloads / 3,575,292,556 bytes, 507 preserved earlier payloads and the separately reviewed privacy amendment. The homepage is byte-identical to its browser-reviewed staging preview. [Final source QA](source-qa-prepared.json) passes 1,143 offline source/provenance/link checks and 78/78 tests in the complete hosted website command against the prepared homepage. [Actual public-directory loopback review](browser-local/prepared/report.json) separately passes four FR/EN mobile/desktop states, 20 visible native image loads and simulation navigation with zero observed console errors. Website source publication and Hosting remain separate.

The 1.21 homepage requires eight **new, unedited** 1080×2400 native PNGs: home, shop, map and puzzle, each in French and English. Each capture-map row supplies `key`, `locale`, `source` and `report`. The accepted native report must bind that PNG's SHA-256, the installed production APK SHA-256, the exact 1.21.0-rc1 package and version code 29. Earlier screenshots are never relabelled. [Production B](../v1.21/native/candidate-b-final-index.json) and [isolated Lab B](../v1.21/native-lab/candidate-b-final-index.json) now have separate accepted native indexes; the source and Hosting delivery steps remain independent.

`scripts/package-release-v121.py --candidate-report <frozen-report> --collect` verifies the two exact signed APKs, their source-input freeze and package identities, then creates ignored external canonical files and a small manifest. It refuses to replace existing canonical artifact bytes.

The staged privacy amendment is recorded in `staging/legal-update-final.json`. It changes only the date/version and the factual description of local energy capacity and elemental/Focus/Undo attempt state and receipts. Account, authentication, social, provider, contact and deletion descriptions retain their existing text. Apply `staging/privacy-index.html` to `public/privacy/index.html` before the homepage write; the preparation script checks both hashes and the immutable previous source commit.

After the exact candidate and captures pass:

1. Apply the factual privacy amendment.
2. Run `scripts/prepare-site-v121.py --capture-map <accepted-map> --artifact-commit <40-character-metadata-commit> --write`.
3. Run `scripts/check-site-v121.py` and the local browser checks against the prepared homepage.
4. Publish and deploy through the parent release task with `scripts/deploy-site-v121.py`. Deployment requires a clean exact commit, all frozen source-input hashes, every physical Hosting payload and the existing Firebase site identity.

`--preview` writes only `staging/index.preview.html` and a source URL map after the same exact artifact/capture checks. `scripts/serve-site-v121-preview.py` serves that map on loopback with existing public assets as fallback. It applies no public homepage/assets and performs no deployment. The accepted B page has passed [eight local FR/EN states, 40 individually visible native image checks and preserved gallery/audio/keyboard/privacy checks](browser-local/homepage/report.json). [Desktop delivery preview](browser-local/homepage/fr-1280-final-delivery-preview.png) and [four exact native surfaces together](browser-local/homepage/en-1280-native-surfaces-final.png) are local browser screenshots, not public-origin observations.

Preparation preserves earlier APK downloads, gallery markup and assets, audio markup and previews, account/auth/deletion pages and all other earlier public payloads. It copies only the new homepage, new versioned styles/script, the independent simulation, accepted captures and the new manifest/APK payloads. The source manifest records preserved files and the intentional privacy amendment separately.

## Independent browser simulation

`staging/lab/ui-1-21/` becomes `/lab/ui-1-21/index.html` during preparation. It uses actual authored local companion sheets. Its mini puzzle, initial balances and 45 Coin / 35 XP teaching reward are explicitly fictional. No Android account, save or payment is accessed. State lives only in memory and resets on reload.

It demonstrates free elemental taps, manual exits, Nova's two-Charge Guide, a once-per-attempt free Focus, one simulated first-clear reward, all 23 fixed SummonCatalog probabilities, the exact 180 Coin arrival price and duplicate compensation, and Nova's 7→8-copy level 3/form 2 milestone. Free form previews do not grant ownership.

The shop's interactive U-level slider copies `EconomyCurve121`: first-clear rewards 1→2, base boost prices 1→1.4 before avatar discount, the first paid Undo after two free uses and energy capacity 10→16. Increasing the capacity grants no energy. The slider changes no balance. `staging/native-economy-fixture.json` contains outputs produced by running the native Java class and binds its source hash.

Local checks:

- `scripts/test-menu-v121.sh`: 5,232 pure geometry and translated-format checks at the time of handoff; this does not qualify Android rendering.
- `node --test scripts/test-site-v121-runtime.mjs scripts/test-simulation-v121.mjs`: 21 checks, including all 10,000 uniform draw rolls and parity with the native economy fixture.
- `browser-local/simulation/report.json`: actual local IAB checks for four views×two languages×four requested widths 320/375/768/1280, no horizontal overflow, a complete teaching loop, final evolution and zero observed console errors. It binds the reviewed simulation sources. Motion reduction was inspected in code/CSS; the operating-system preference was not changed.
- `source-qa-node.json`: current 21-test receipt after final test-file whitespace hygiene, with exact script/style hashes; prior host receipts retain their original identities.
- `source-contracts.json`: in-memory homepage template checks using synthetic placeholder artifact metadata. It does not assert a released 1.21 APK or native capture and writes no public payload.

Local browser evidence, native Android evidence, authenticated Firebase management evidence and public-origin byte/render verification remain separate. None establishes human enjoyment, retention, real-device acceptance, Play publication or real purchase approval.
