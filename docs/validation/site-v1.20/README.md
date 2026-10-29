# Website 1.20 — tooling staged, native media required

The current `public/` tree remains the delivered1.19 source. No 1.20 native images, package collection, prepared public page, deployment or public render is claimed. The 1.20 generator, shop/home/victory copy and assets are staged for later exact-candidate qualification.

## Prepared tools

`scripts/package-release-v120.py` validates a supplied frozen candidate report, complete source inputs, exact APK bytes, actual production/Lab package identities, `1.20.0[-sandbox]-rc1`, code 28 and the retained signing certificate. It defaults to checks; `--collect` explicitly writes immutable canonical ignored files and metadata. It never builds or publishes.

`scripts/prepare-site-v120.py` requires six records: `home`, `shop`, `victory`, each in FR and EN. Every original 1080×2400 PNG must be bound by a passing exact-installed-hash native report for the production1.20 package/code 28. The new artifact metadata commit must identify the exact release. Only a successful `--write` copies the staged `site-v120.js/css`, original PNGs, physical external APKs and manifest into `public/`, then creates the source/provenance records. Old gallery/audio sections and legal/account/configuration/archive files are preserved. No new 1.20 capture map is invented before actual native evidence exists.

`scripts/check-site-v120.py` checks those later local bytes, references, provenance and preserved files without network requests. `scripts/deploy-site-v120.py` defaults to preflight and requires explicit `--deploy`, one clean exact source commit, full ignored physical APK copies, immutable metadata and a fresh receipt directory outside the checkout. It deploys only to the existing Firebase project.

The approximately 369MB current APKs are external files; Git metadata alone is not their presence. The later1.19→1.20 exact-delta transport must reconstruct and compare complete final byte counts/hashes before deployment. No Cloud Shell action or APK transfer was performed by this staging task.

## Recorded staging checks

Python compilation and JavaScript syntax pass. A synthetic **in-memory template-only** check verifies markup/copy and exact preservation of the old gallery/audio sections. An actual negative call with 1.19 metadata is refused before writes. The old public index remains SHA-256 `626d0247cf194cf6e075b78514f5b8a68c818d53a80a76ef33d630ba3a94d5ea` before and after. [Staged-tool scope](staging/tool-checks/report.json). Synthetic values are not saved as candidate HTML, native evidence, package metadata or a public page.

This is not a final source checker/browser/native pass. Local FR/EN desktop/mobile, keyboard, visible images, deliberate audio and bounded gallery loading must be checked after the real page is prepared.

## Public-origin permission boundary

Browser access to `https://luma-drift-production.web.app` was explicitly refused during the preceding qualification. Do not switch tools, hosts or request mechanisms to work around that refusal. The versioned `verify-site-v120-publication.py` is intentionally a no-network refusal stub. Local browser rendering and authenticated Firebase Hosting management remain separate usable scopes. Management `DEPLOY`/`FINALIZED` cannot establish public byte or rendering acceptance. Report that boundary plainly in the later release receipt.

The existing1.19 public-byte report remains historical evidence of its own earlier authorized scope; it does not grant a new origin-access permission or qualify1.20.
