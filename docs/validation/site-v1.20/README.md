# Website 1.20 — C3 delivered for playtesting

Application/artifact/native proofs are published at [C2 `7ea28bc6`](https://github.com/nabz0r/AndroidHit/commit/7ea28bc6125e73d0543b264081266aeff510b071); the exact six-F-frame website, intentional policy baseline, preparation and local-QA source are published at [C3 `eaae0fba`](https://github.com/nabz0r/AndroidHit/commit/eaae0fbad15b074409f608e5a2afe7913c3bee3b). [C2 record](../v1.20/publication/c2-source.json) · [C3 record](../v1.20/publication/c3-site-source.json). C3's public tree is `8194e39a276bd7652e3f5c58e68a4f0ad986b416`.

[Preparation](preparation.json) binds the complete collected APKs and six original exact-F home/shop/victory images in FR/EN. [Source contracts](source-contracts.json) pass 1,092 checks over 508 payloads. [Local browser review](browser-local/report.json) records actual FR/EN views at measured 320/375/1280/2360 widths, visible images, privacy/title correction, a gallery selection and keyboard focus. Intermediate/unsettled observations remain in that report; they are not silently renamed as final visual acceptance. E/D Android images are not reused as F images.

[The derived local Hosting record](hosting/deployment.json) transcribes management UI observations confirming C3 as DEPLOY/FINALIZED, version `94f0979d7a02c3b2`, release `1793312983998000`, at `2026-10-29T22:29:43.998Z`. The release message is exactly `Website1.20 from eaae0fbad15b074409f608e5a2afe7913c3bee3b`. [Canonical delivery record](../v1.20/publication/release-delivery.json). The complete production/Lab files were reconstructed and matched to canonical hashes before upload; public-origin download-byte and browser/render observations were not performed under the saved permission boundary.

The local record is **DERIVED_UI_OBSERVATION**, not a byte-exact export of the original JSON receipts. Four originals remain in Cloud Shell and are inventoried by size/hash. Their attempted local export failed on a technical download timeout, not an authorization refusal. This does not reverse the observed FINALIZED deployment or the pre-upload APK verification. The saved public-origin boundary is a separate limit.

[Production APK](https://luma-drift-production.web.app/releases/luma-drift-1.20.0-rc1.apk) · [Separate Lab APK](https://luma-drift-production.web.app/releases/luma-drift-1.20.0-sandbox-rc1.apk). The immutable artifact/preparation records retain their original creation/predeployment flags; current delivery status is reported by the separate Hosting and delivery receipts. This is playtest delivery, not Play publication, production-provider activation or measured retention.

[Hosted C3 CI](../v1.20/publication/c3-ci.json), [run 40963457821](https://github.com/nabz0r/AndroidHit/actions/runs/40963457821), passes both jobs at the published C3 head: host puzzle/UI/economy, Node website, pinned art and premium-transport checks. It does not build/install Android or observe the public site.

## Prepared tools

`scripts/package-release-v120.py` validates a supplied frozen candidate report, complete source inputs, exact APK bytes, actual production/Lab package identities, `1.20.0[-sandbox]-rc1`, code 28 and the retained signing certificate. It defaults to checks; `--collect` explicitly writes immutable canonical ignored files and metadata. It never builds or publishes.

`scripts/prepare-site-v120.py` requires six records: `home`, `shop`, `victory`, each in FR and EN. Every original 1080×2400 PNG is required to be bound by a passing exact-installed-hash native report for the production 1.20 package/code 28. The new artifact metadata commit must identify the exact release. Only a successful `--write` copies the staged `site-v120.js/css`, original PNGs, physical external APKs and manifest into `public/`, then creates the source/provenance records. Old gallery/audio/account/configuration/archive files are preserved. The policy is intentionally updated to the separately accepted final baseline; it is not byte-identical to the preceding 1.19 C3 policy. No new 1.20 capture map is invented before actual native evidence exists.

`scripts/check-site-v120.py` checks those later local bytes, references, provenance and preserved files without network requests. `scripts/deploy-site-v120.py` defaults to preflight and requires explicit `--deploy`, one clean exact source commit, full ignored physical APK copies, immutable metadata and a fresh receipt directory outside the checkout. It deploys only to the existing Firebase project.

The approximately 369 MB current APKs are external files; Git metadata alone is not their presence. The later 1.19→1.20 exact-delta transport must reconstruct and compare complete final byte counts/hashes before deployment. No Cloud Shell action or APK transfer was performed by this staging task.

## Intentional policy update, staged separately

The complete proposed FR/EN policy is [staged outside public](staging/privacy-v120-final.html). Its [old/new hash record](staging/legal-update-final.json) identifies the exact 1.19 C3 source policy and the intended changes: local energy/attempt data, test-Gold refill receipts and recovery limits, the closed undeployed production energy path, and advertising wording that no longer promises every restart is free. Authentication, social data, journal, account deletion, site-provider and contact sections remain byte-identical.

After production F acceptance, the release owner explicitly copied this file into local `public/privacy/index.html` before preparation. The local hash matches the staged `37329ca9dda90469cb3d71d3e157ef58d0b057df49ca915706e4f1b929134bf7`. This final policy baseline is part of the exact C3 source confirmed by the Hosting receipt; no independent public GET is claimed. That accepted legal source becomes preparation's preservation baseline, with both hashes and the reason retained; the staging record keeps its original pre-copy state. The [final local-application receipt](legal-application-final.json) records the accepted baseline; the earlier review/application remains preserved. Local browser review also corrected the document title from 1.13 to 1.20 in this final file. It must not be described as a byte-identical preservation of the preceding 1.19 C3 policy. Deletion/authentication/contact files are not part of this delta.

## Recorded staging checks

Python compilation and JavaScript syntax pass. A synthetic **in-memory template-only** check verifies markup/copy and exact preservation of the old gallery/audio sections. An actual negative call with 1.19 metadata is refused before writes. During that historical staging check, the old public index stayed SHA-256 `626d0247cf194cf6e075b78514f5b8a68c818d53a80a76ef33d630ba3a94d5ea` before and after. [Staged-tool scope](staging/tool-checks/report.json). Synthetic values are not saved as candidate HTML, native evidence, package metadata or a public page.

A separate [local runtime suite](runtime-contracts/README.md) passes 57 Node tests, including 9 staged 1.20 behaviours with DOM doubles and historical contracts. It does not prepare a 1.20 page or qualify browser rendering or remote CI.

The earlier staging checks are historical and do not substitute for the now recorded source/provenance and local browser scopes above. No public-origin rendering or audio-quality claim is made. Runtime tests, localhost observation and Hosting delivery keep separate receipts.

## Public-origin permission boundary

Browser access to `https://luma-drift-production.web.app` was explicitly refused during the preceding qualification. Do not switch tools, hosts or request mechanisms to work around that refusal. The versioned `verify-site-v120-publication.py` is intentionally a no-network refusal stub. Local browser rendering and authenticated Firebase Hosting management remain separate usable scopes. Management `DEPLOY`/`FINALIZED` cannot establish public byte or rendering acceptance. Report that boundary plainly in the later release receipt.

The existing 1.19 public-byte report remains historical evidence of its own earlier authorized scope; it does not grant a new origin-access permission or qualify 1.20.
