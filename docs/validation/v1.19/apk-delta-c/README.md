# Exact APK transport: candidate C / prototype2 / code 27

Status: **PASS**. These two patches reconstruct the exact frozen C APKs from the existing 1.18 APKs. They are distinct from the retained candidate-B patches and proofs under `../apk-delta/`; no B payload or earlier release bytes were replaced.

| Variant | Patch bytes | Reconstructed APK bytes | Reconstructed APK SHA-256 |
|---|---:|---:|---|
| Production | 3,851,204 | 368,904,157 | `86e310ea661af5644b47a96b22df85f148f6a9508ee87234339be7b093e8a526` |
| Lab | 3,845,011 | 368,903,840 | `448d359abb6d4943c6eadf9f03c221fd35d5fa18c21441817bb4c7a31bb87d11` |

Total transport is **7,696,215 bytes**, replacing the upload of two APKs totalling 737,807,997 bytes when their exact existing bases are already in Cloud Shell. Final APK files still occupy their full size and belong to the separate Hosting payload. The patches are not installable APKs, app-store updates or Git APK blobs.

## Cloud Shell reconstruction

From the intended repository revision, with Python 3.9+ and the exact existing 1.18 APKs present:

```sh
python3 scripts/apk-byte-delta.py apply \
  --base public/releases/luma-drift-1.18.0-prototype1.apk \
  --patch releases/delta-1.18-to-1.19-prototype2-production.json.gz \
  --output public/releases/luma-drift-1.19.0-prototype2.apk \
  --expected-target-sha256 86e310ea661af5644b47a96b22df85f148f6a9508ee87234339be7b093e8a526 \
  --patch-sha256 d4d566c022f752700017c0b2b8e2f7b06d012d7eac79d3a59c12051c8ce71fc8 \
  --report /tmp/luma-v119-prototype2-production-delta.json

python3 scripts/apk-byte-delta.py apply \
  --base public/releases/luma-drift-1.18.0-sandbox-prototype1.apk \
  --patch releases/delta-1.18-to-1.19-prototype2-lab.json.gz \
  --output public/releases/luma-drift-1.19.0-sandbox-prototype2.apk \
  --expected-target-sha256 448d359abb6d4943c6eadf9f03c221fd35d5fa18c21441817bb4c7a31bb87d11 \
  --patch-sha256 0d99c1da6a7babc76cadb4fd2c25e36744ddb4ce6540c7690abcb8361d06f4d1 \
  --report /tmp/luma-v119-prototype2-lab-delta.json
```

An existing destination is refused unless `--replace` is supplied intentionally. A missing/different base is refused. Wrong patch, range, literal, final hash or size leaves an existing destination intact. Publication uses an atomic same-directory replacement only after reconstruction and verification. No dependency installation, network call, ZIP extraction, member recompression, signing operation or signing key is involved.

## Evidence and limits

[Reconstruction proof](reconstruction-proof.json) pins the C candidate record and script hashes. [Production build](production-build.json) / [apply](production-apply.json) and [Lab build](lab-build.json) / [apply](lab-apply.json) record the exact compressed byte ranges and final hashes. Every reused member was checked by compressed-data SHA-256 and an independent byte comparison before reuse. The rebuilt complete APKs were then independently compared to C byte for byte.

Android Build Tools 36 verifies the rebuilt files' APK v2 signatures: [production](production-apksigner.txt), [Lab](lab-apksigner.txt). Both have the expected certificate SHA-256 `dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287`. The signing block, ZIP headers, changed member bytes and ZIP tail remain exact literals; the files were never repacked or resigned.

[Transport tests](transport-tests.txt) retain 18 passing generated-fixture cases. The shared [transport description](../apk-delta/README.md#how-the-patch-works) explains strict size/count limits, malformed-data refusal and the atomic process. Both real C patch builds were reproduced with identical output hashes. Rebuilt local C files remain under `/tmp/luma-v119/delta-reconstructed-c/`.

This verifies byte transport and signatures. It does not repeat installation, runtime, native usability, ads or payment qualification, and it does not publish or deploy the site. Those outcomes require their own evidence for the same C hashes.
