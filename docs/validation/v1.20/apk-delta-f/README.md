# Exact APK transport: accepted production candidate F / 1.20 RC1 / code 28

Transport status: **PASS**, promoted to the canonical delta paths after the parent accepted production F. Hosting deployment remains separately gated. Earlier B–E stages remain superseded intermediates outside the canonical release paths.

| Variant | Patch bytes | Reconstructed APK bytes | Reconstructed APK SHA-256 |
|---|---:|---:|---|
| Production | 3,872,028 | 368,936,921 | `e4fa1a70dbb2e842c9a16a9f22d0f2a49a4316ccbc6013fe0acfa233093f5d26` |
| Lab | 3,868,528 | 368,920,220 | `7c8ccfde3a0050b35b0312a61e2cb07973c594629bbf97493ec8bbe2829410cf` |

The two patches total **7,740,556 bytes**, using the exact retained 1.19 prototype2 APKs as bases. The reconstructed APK pair still occupies 737,857,141 bytes and forms part of the separately audited Hosting payload. Patches are transport data, not installable APKs or Play Store updates. No APK is added as a Git blob.

## Reconstruction in the exact intended checkout

Use Python 3.9+ and the exact physical 1.19 prototype2 APKs. The production base SHA-256 is `86e310ea661af5644b47a96b22df85f148f6a9508ee87234339be7b093e8a526`; the Lab base is `448d359abb6d4943c6eadf9f03c221fd35d5fa18c21441817bb4c7a31bb87d11`.

```sh
python3 scripts/apk-byte-delta.py apply \
  --base public/releases/luma-drift-1.19.0-prototype2.apk \
  --patch releases/delta-1.19-prototype2-to-1.20-rc1-production.json.gz \
  --output public/releases/luma-drift-1.20.0-rc1.apk \
  --expected-target-sha256 e4fa1a70dbb2e842c9a16a9f22d0f2a49a4316ccbc6013fe0acfa233093f5d26 \
  --patch-sha256 9ca03378990d64f447d8ae8f51e67a936417681dfac95c2c3c8631ddf2b9a338 \
  --report /tmp/luma-v120-production-delta.json

python3 scripts/apk-byte-delta.py apply \
  --base public/releases/luma-drift-1.19.0-sandbox-prototype2.apk \
  --patch releases/delta-1.19-prototype2-to-1.20-rc1-lab.json.gz \
  --output public/releases/luma-drift-1.20.0-sandbox-rc1.apk \
  --expected-target-sha256 7c8ccfde3a0050b35b0312a61e2cb07973c594629bbf97493ec8bbe2829410cf \
  --patch-sha256 72d8b410e0fcfb4bf8858209f9696e3f2fee80eae53a00d36a6a6e912bf6cde5 \
  --report /tmp/luma-v120-lab-delta.json
```

Existing destinations are refused by default. A different base, malformed patch, invalid range, literal digest, final digest or size is refused before atomic output publication. No ZIP member is extracted, recompressed or resigned. Unchanged compressed ZIP data is reused only after byte comparison; ZIP headers, changed data, signing blocks and tails remain exact literals.

## Evidence and qualification boundaries

[Canonical proof](reconstruction-proof.json) links the original [staged transport proof](staging-reconstruction-proof.json), immutable [signed build record](candidate-build-record.json), enriched canonical candidate record, and [snapshot of the production native acceptance index](production-native-acceptance-index.json). Its captured native index SHA-256 is `78f1316a2774e00d6cd9ff33d3987ed78a4f81d0bdb9197d9a8ef5b929027556`; the snapshot is retained even if later evidence adds separate Lab results.

[Production build](production-build.json) / [apply](production-apply.json) and [Lab build](lab-build.json) / [apply](lab-apply.json) record the exact source, patch and reconstructed APK hashes. Complete reconstructed files were independently compared byte for byte with the signed F originals. [Production](production-rebuild.json) and [Lab](lab-rebuild.json) second builds generated identical patches.

Android Build Tools 36 verified the reconstructed [production](production-apksigner.txt) and [Lab](lab-apksigner.txt) APK v2 signatures with the retained certificate SHA-256 `dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287`. [Transport tests](transport-tests.txt) passed all 18 generated-fixture cases, including bounded malformed data and atomic destination preservation.

These checks establish exact byte transport and preserved signing. Production native acceptance is linked as separate evidence supplied by the parent. This directory does not claim Lab runtime acceptance, physical-device usability, provider approval, real purchases, retention, a Git publication, a Hosting deployment or public-origin rendering. Publication and Hosting require their own exact artifact/source commits and management receipts.
