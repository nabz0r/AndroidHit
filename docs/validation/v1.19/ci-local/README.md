# Local reproduction of the updated CI sparse payload

Status: **PASS**, using the actual new 1.19 homepage and exactly the first CI job's selected source paths. [Report](report.json) pins the workflow and 349 copied source files by SHA-256, records every command, and hashes its retained output. Java 17 was selected explicitly. This is local host execution, not a remote GitHub Actions result or native Android acceptance.

The first 1.19 artifact/source publication retained the older workflow. That workflow did not select `CompanionBattleLayout.java`, while the new retained play-geometry runner imports it. The updated workflow now includes that helper and all four 1.19 battle, action, storage and geometry contracts. This change repairs CI packaging; the earlier publication and candidate-B APK bytes remain immutable. No historical CI success is inferred here.

The 1.18 website tests now read an exact frozen HTML fixture at `docs/validation/site-v1.18/index.template.html`. Its pinned SHA-256 is `a256963ec96ecf79d6e7c19ec3f4a6914df992c342808bd01bfc63c6209d9d3e`. The original 1.18 JavaScript and gallery contracts remain active. Current 1.19 tests separately execute its real JavaScript against a narrow local DOM double and inspect the current homepage's navigation, paired screenshot routes, six skill families, save limits, fixed candidate hashes, provider-availability copy, deliberate gallery loading and manual audio controls.

Executed checks:

- Website 1.19, retained 1.18, 1.17 and 1.16: **48 passing Node cases**.
- Companion battle state: **88,946 passing host checks**.
- Actual U boards × all 37 companion definitions: **1,083,113 passing host action checks**.
- Owned-team storage, recovery and localized copy: **799 passing host checks**.
- Dock geometry: **14,859 passing spatial checks across 412 layouts**.
- Retained play geometry plus dock composition: **403,778 passing checks across 3,568 layouts**.
- Exact APK delta transport: **18 passing generated-fixture cases**.

The sparse specification retains one pre-existing unmatched `.gitattributes` path. Git sparse checkout permits that path to be absent; no test input was substituted for it. No APKs, native screenshot pixels, account credentials, SDK or provider endpoint were required by these checks. Exact native capture provenance and visual review belong to the separate website/native qualification evidence.
