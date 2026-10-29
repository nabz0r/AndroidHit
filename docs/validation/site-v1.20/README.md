# Website1.20 — accepted F media, publication pending

Production F passes [its accepted native scope](../v1.20/native/README.md), and all six original exact-F home/shop/victory frames in FR/EN are [recorded](native-capture-map.json). [Canonical local collection](../v1.20/publication/package-collection.json) also passes. Source preparation, local-site browser acceptance, immutable Git/source publication, Hosting and public render retain separate gates and are not inferred from those results. E/D images remain intermediate and are not used as F media. The release owner has copied the intentional FR/EN policy update into local public source before preparation; this is not a Hosting delivery.

## Prepared tools

`scripts/package-release-v120.py` validates a supplied frozen candidate report, complete source inputs, exact APK bytes, actual production/Lab package identities, `1.20.0[-sandbox]-rc1`, code 28 and the retained signing certificate. It defaults to checks; `--collect` explicitly writes immutable canonical ignored files and metadata. It never builds or publishes.

`scripts/prepare-site-v120.py` requires six records: `home`, `shop`, `victory`, each in FR and EN. Every original 1080×2400 PNG is required to be bound by a passing exact-installed-hash native report for the production 1.20 package/code 28. The new artifact metadata commit must identify the exact release. Only a successful `--write` copies the staged `site-v120.js/css`, original PNGs, physical external APKs and manifest into `public/`, then creates the source/provenance records. Old gallery/audio sections and legal/account/configuration/archive files are preserved. No new 1.20 capture map is invented before actual native evidence exists.

`scripts/check-site-v120.py` checks those later local bytes, references, provenance and preserved files without network requests. `scripts/deploy-site-v120.py` defaults to preflight and requires explicit `--deploy`, one clean exact source commit, full ignored physical APK copies, immutable metadata and a fresh receipt directory outside the checkout. It deploys only to the existing Firebase project.

The approximately 369MB current APKs are external files; Git metadata alone is not their presence. The later 1.19→1.20 exact-delta transport must reconstruct and compare complete final byte counts/hashes before deployment. No Cloud Shell action or APK transfer was performed by this staging task.

## Intentional policy update, staged separately

The complete proposed FR/EN policy is [staged outside public](staging/privacy-v120-final.html). Its [old/new hash record](staging/legal-update-final.json) identifies the exact C3 source policy and the intended changes: local energy/attempt data, test-Gold refill receipts and recovery limits, the closed undeployed production energy path, and advertising wording that no longer promises every restart is free. Authentication, social data, journal, account deletion, site-provider and contact sections remain byte-identical.

After production F acceptance, the release owner explicitly copied this file into local `public/privacy/index.html` before preparation. The local hash matches the staged37329ca9dda90469cb3d71d3e157ef58d0b057df49ca915706e4f1b929134bf7. This is not a deployed policy. That accepted legal source becomes preparation's preservation baseline, with both hashes and the reason retained; the staging record keeps its original pre-copy state. The [final local-application receipt](legal-application-final.json) records the accepted baseline; the earlier review/application remains preserved. Local browser review also corrected the document title from1.13 to1.20 in this final file. It must not be described as a byte-identical preservation of the preceding C3 policy. Deletion/authentication/contact files are not part of this delta.

## Recorded staging checks

Python compilation and JavaScript syntax pass. A synthetic **in-memory template-only** check verifies markup/copy and exact preservation of the old gallery/audio sections. An actual negative call with 1.19 metadata is refused before writes. The old public index remains SHA-256 `626d0247cf194cf6e075b78514f5b8a68c818d53a80a76ef33d630ba3a94d5ea` before and after. [Staged-tool scope](staging/tool-checks/report.json). Synthetic values are not saved as candidate HTML, native evidence, package metadata or a public page.

A separate [local runtime suite](runtime-contracts/README.md) passes57 Node tests, including9 staged1.20 behaviours with DOM doubles and historical contracts. It does not prepare a1.20 page or qualify browser rendering or remote CI.

This is not a final source checker/browser/native pass. Local FR/EN desktop/mobile, keyboard, visible images, deliberate audio and bounded gallery loading must be checked after the real page is prepared.

## Public-origin permission boundary

Browser access to `https://luma-drift-production.web.app` was explicitly refused during the preceding qualification. Do not switch tools, hosts or request mechanisms to work around that refusal. The versioned `verify-site-v120-publication.py` is intentionally a no-network refusal stub. Local browser rendering and authenticated Firebase Hosting management remain separate usable scopes. Management `DEPLOY`/`FINALIZED` cannot establish public byte or rendering acceptance. Report that boundary plainly in the later release receipt.

The existing 1.19 public-byte report remains historical evidence of its own earlier authorized scope; it does not grant a new origin-access permission or qualify 1.20.
