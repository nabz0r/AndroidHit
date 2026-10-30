# Hosting management originals

The [initial deployment](deployment.json) and [read-only recovery](recovery.json)
are byte-identical copies from Cloud Shell. The complete
[original receipt archive](original-receipts.tar.gz) contains all 17 files from
the first refused preflight, successful reserved-path preflight, deployment and
read-only recovery. [The portable index](receipt-index.json) records every
member's size and SHA-256. Archive SHA-256:
`d1e05c0768994b249d56a5586ad83362337ad1239839f7817e0b53219d865edd`.

The export was reconstructed from numbered, visible terminal blocks, then
verified against the remote archive hash. Credential tokens are never emitted
or recorded by the deployment client.

The initial release is version `92e40c3a51003cb2`, source `8f3653d0`, at
2026-10-30T01:34:11.791Z. Its original postcheck failed because it compared the
default-site and live-channel release resource names literally. The exact
release ID, version and source message match. Recovery at validator commit
`ea1b7fcc` makes only GET requests and preserves the original failure receipt.
It is an observation of that existing release, not a second deployment.

Provider receipts verify all 541 compressed file hashes and serving config.
The [independent HTTPS response check](../public-origin-overlay.json) and
[actual responsive browser observation](../browser-public/report.json) have
separate scopes. Historical APKs are preserved server-side; they are not
downloaded or uploaded by the additive overlay.

## Final navigation correction

[Final deployment](final/deployment.json) is **DEPLOY/FINALIZED**, source
`64c0d9302680febbc19bc444ae4d315fd9a2a0f0`, version `d88a6817932258f4`,
released **2026-10-30T02:06:57.153Z**. Exactly three dashboard paths differ from
the initial lab version; all other 538 provider hashes and serving config match.

[All ten final JSON originals](final-receipt-index.json) retain their exact
remote bytes. To reduce transfer size, Cloud Shell encoded unchanged lines as
references to the earlier indexed originals and new lines as base64 literals.
The [gzip transport envelope](final-transfer.json.gz), SHA-256
`fc63b5c7d0497df80f4407af32ce2ce01014068ec4dbdaa1a1a3def59129ac16`,
was exported through visible numbered blocks. Each reconstructed JSON then
matches its independent remote byte length and SHA-256. The index and
[independent final map check](final-integrity.json) document this verification.
This preserves original bytes rather than deriving a new serialization.
