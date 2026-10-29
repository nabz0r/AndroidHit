# Exact APK transport: 1.18 → candidate B of 1.19

Both candidate-B APKs were reconstructed locally from the exact existing 1.18 APK and a small patch. The rebuilt files match candidate B **byte for byte**, have its pinned full SHA-256, and pass Android Build Tools 36 `apksigner verify --verbose --print-certs` with the expected certificate. This is transport verification; it does not repeat installation, gameplay or provider qualification.

| Variant | Patch bytes | Reconstructed APK bytes | Compressed member ranges reused |
|---|---:|---:|---:|
| Production | 3,850,793 | 368,904,153 | 830 |
| Lab | 3,845,513 | 368,903,840 | 829 |

The two patches total **7,696,306 bytes**, replacing a 737,807,993-byte APK upload when the exact bases are already in Cloud Shell. The resulting APK files still occupy their full size and must be included in the separate Hosting deployment. The patch files are transport artifacts, not installable updates or Git APK blobs.

## Reconstruction in an existing deployment checkout

Requires Python 3.9 or newer, the matching existing base APKs under `public/releases/`, and the committed script and patches. There are no dependencies to install, no network request, no ZIP extraction, and no signing key. Run from the repository root after obtaining the intended source revision:

```sh
python3 scripts/apk-byte-delta.py apply \
  --base public/releases/luma-drift-1.18.0-prototype1.apk \
  --patch releases/delta-1.18-to-1.19-production.json.gz \
  --output public/releases/luma-drift-1.19.0-prototype1.apk \
  --expected-target-sha256 d7a9b562a35f085346c47669400466c79d069e9e4ad297a83fb0a60ec27ef5d5 \
  --patch-sha256 01d79fd7969c3b149c06109a03e9514b23e62e26937dbbdbeefb04c57289d7ba \
  --report /tmp/luma-v119-production-delta.json

python3 scripts/apk-byte-delta.py apply \
  --base public/releases/luma-drift-1.18.0-sandbox-prototype1.apk \
  --patch releases/delta-1.18-to-1.19-lab.json.gz \
  --output public/releases/luma-drift-1.19.0-sandbox-prototype1.apk \
  --expected-target-sha256 46a2ee9bacb1f093859eb84031ab1dac6a8dc5e0582cb9d27d8b887d181adcf5 \
  --patch-sha256 ed1e3061d6708ccd243f2ef7cfaae7c4e747ed4b56417f43435f2fe1cd222298 \
  --report /tmp/luma-v119-lab-delta.json
```

An existing output is refused by default. Use `--replace` only for an intended replacement. Even then, the existing file remains intact if any base, range, literal, size or final hash fails; the new file becomes visible only after successful reconstruction, flushing and verification. A missing or different 1.18 base must be supplied through a separate authorized transport; the script neither downloads nor guesses it.

## How the patch works

The build command reads ZIP local-header offsets without extracting members. An entry can be reused only when name, compression method, compressed length, uncompressed length, **compressed-data SHA-256 and independent chunk-by-chunk byte equality** agree between base and target. Each reuse operation identifies a base offset, byte count and SHA-256. Every gap, changed member, alignment byte, local header, central directory, APK signing block and final ZIP tail is a literal. The APK is never repacked or resigned.

The deterministic gzip JSON carries exact base/target sizes and full hashes, ordered copy/literal operations, individual hashes and checked totals. Apply independently requires the expected target hash from the caller, checks the whole base hash, hashes each copied range and literal, and verifies the entire completed output before atomic publication. Artifact metadata names never select a path. Output or report aliases of input artifacts and symlink outputs are refused.

Bounds are explicit: APK at most 1 GiB, compressed patch at most 10 MiB, expanded JSON at most 64 MiB, total literals at most 32 MiB, literals individually at most 1 MiB, and at most 20,000 operations. Invalid counts, out-of-base offsets, inconsistent totals, duplicate JSON keys, malformed base64 and unsupported schemas are refused. Build refuses a patch over 10 MiB and asks for another transport rather than silently embedding an APK.

Build usage is documented by `python3 scripts/apk-byte-delta.py build --help`. Both the independently expected base and target hashes are required. Two real build passes produced identical gzip hashes for each variant. The scripts use the Python standard library only.

## Recorded evidence

- [Complete reconstruction proof](reconstruction-proof.json): source candidate record hash, exact script hashes, original/rebuilt artifact hashes, independent full-byte comparison and signer certificate.
- [Production build](production-build.json), [production apply](production-apply.json), [production signature](production-apksigner.txt).
- [Lab build](lab-build.json), [lab apply](lab-apply.json), [lab signature](lab-apksigner.txt).
- [Transport tests](transport-tests.txt): 18 passing cases, including deterministic offsets/header/tail preservation, separate byte-equality verification, malformed data, incorrect hashes, bounded gzip and refused replacement preserving the previous file.

The original local targets remain under `/tmp/luma-v119/candidate-b/`; the independently rebuilt local files remain under `/tmp/luma-v119/delta-reconstructed/`. No APK was added to Git, no provider action occurred, and this transport work did not publish or deploy anything.
