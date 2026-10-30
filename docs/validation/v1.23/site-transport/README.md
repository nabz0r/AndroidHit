# 1.23 transport contract — offline qualification

The current [rc2 receipt](rc2/receipt.json) records **37 passing offline tests** on Python 3.9.6: 18 deployment tests, 10 staging tests and 9 public-response tests. It binds the six new scripts/tests and their three actual shared helpers by SHA-256. These fixtures do not establish a real upload, Android signing acceptance, public response, or browser rendering. Those gates need their own original receipts.

The earlier [rc1 receipt](receipt.json) and logs are preserved unchanged. Its six exact script/test inputs are retained in the [rc1 source snapshot](rc1-source-snapshot.zip), whose entries match that receipt's hashes. They qualify the private rc1 preparation only. The rc2 production scripts change only 1.23 version/path literals; the tests additionally refuse rc1 metadata. The rc2 receipt links both sets without relabelling the old execution.

## Fixed input and allowed change

The sole baseline is accepted 1.22 C3 source `b122c0acbd172a7912c69b0b6914b8b0f9b2a243`, Hosting version `sites/luma-drift-production/versions/24e34cf6e6af1ed8`, with 570 physical paths and 572 provider paths. The current source manifest must declare accepted 1.23 native captures and package identity. Immutable APK metadata must identify `1.23.0-rc2` / `1.23.0-sandbox-rc2`, version code 31 and the established signer. The canonical manifest is `releases/manifest-1.23.0-rc2.json`; committed delta inputs are under `releases/deltas/1.23.0-rc2/`.

The next version replaces only `/index.html`, adds the two 1.23 assets, eight native captures, 1.23 release metadata and both APKs, and adds the report at `/lab/clarity-123/`. Extra additions are bounded to that report directory. It retires exactly the [14 verified public binary duplicates](../cleanup/public-retirements.json), whose raw hashes and sizes must match every original C3 row in `retired_files`. Canonical `releases/` copies stay outside this retirement.

Firebase supports `versions.clone` with [`exclude.regexes`](https://firebase.google.com/docs/reference/hosting/rest/v1beta1/sites.versions/clone). The deployer uses escaped, anchored exact paths. Before any overlay upload, it requires that the clone contain the complete old provider map minus those 14 paths and have unchanged serving configuration. Of the original 572 provider paths, 557 remain unchanged, one homepage is replaced, and 14 are retired. The final count is `558 + added path count`. Original finalized versions are left intact.

## Commands and scope

All commands take the exact published source commit. Stage reports, deployment receipts and response receipts must use new paths outside the checkout. A clean checkout and committed frozen inputs are required. Neither staging nor collection uses a credential. Deployment reads its OAuth token from stdin only; it never obtains, persists, or prints it.

```text
python3 scripts/stage-site-v123-rest.py --commit <source-commit> --report <new-external-json>
python3 scripts/deploy-site-v123.py --commit <source-commit> --apk-sha256 <production-sha256> --expected-live sites/luma-drift-production/versions/24e34cf6e6af1ed8 --output <new-external-directory> --token-stdin
python3 scripts/collect-site-v123-public-bytes.py --commit <source-commit> --output <new-external-json>
```

Staging reconstructs ignored physical 1.23 APKs from the verified local 1.22 bases and committed delta bytes. An explicit `--download-bases` permits only the two fixed 1.22 URLs, with full size/hash checks. Existing canonical target bytes are never overwritten. It does not build or deploy.

The deployment command above performs management preflight only. `--deploy` authorizes the separate REST mutation. No CLI transport is available. Management calls retain the existing 60-second timeout; only pinned upload requests use 600 seconds. The expected live version is checked before clone and immediately before release. Firebase does not provide an atomic compare-and-swap release here, and the receipt states that limit.

The collection command above writes a planned receipt only. `--fetch` performs the separate public gate. It streams all new and changed complete response bodies through SHA-256 without saving them, checks content type, encoding, length and exact URL, then requires HTTP 404 for all 14 retired paths without reading their bodies. It follows no redirect, uses no proxy, credential, retry or fallback, and stops on a refusal or mismatch. This qualifies selected HTTP identities; it does not claim that every unchanged response was fetched again or that the UI rendered correctly.

## Fault coverage

The deployment fixtures verify exact source/live identity, old provider hashes/configuration, exact retirement filters, required captures/assets/APKs/report, unknown changes, unsafe paths, code 31 metadata and signer, physical ignored APK identities, upload-only timeout extension, provider rejection, ignored or excessive exclusion, and live-version races. The staging fixtures exercise sparse inputs, delta roundtrips, missing local bases, refusal to overwrite, altered metadata, a changed baseline and unwanted path changes. The collector fixtures exercise full successful reads, 14 retirement responses, no reads after unexpected HTTP 200, access refusal, wrong hashes/length/type/encoding, redirects, TLS refusal, and unsafe paths.

The original 1.22 scripts and tests were kept unchanged. Their separate regression runs passed 12 deployment, 8 staging and 9 collector tests; the original stdout was not added to this new receipt, so its 37-test count covers only the three 1.23 test files.

During development, test-fixture errors were corrected before this final run: a missing fixture brace, Python 3.9 HTTP-error fixtures without a response stream, and a macOS `/var` versus `/private/var` alias in a relative-path assertion. The latter also made the production file validator normalize the checkout root before computing the relative path. No real provider failure was observed or erased by these local fixture corrections.
