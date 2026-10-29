# H immutable build and binary audit

Candidate: production `1.17.0-rc6`, Lab `1.17.0-sandbox-rc6`, versionCode24. Source commit `bb622960f52e5ba41e75a6dfa8b30dbb4b98a291`.

`build-and-audit.py` recorded the exact six-task Gradle command, all162 application inputs before building and their unchanged hashes afterward. Four release artifacts and both lint variants passed in49.014s. Each lint report contains0 Fatal/Error and13 warnings; this is not a zero-warning claim.

The independent existing verifier checked all four package/version/minSdk26/targetSdk36 identities, pinned public signing certificate, APK signatures, signed AAB metadata and bundletool1.18.1 validation. All8 bundled64-bit native payloads /20 LOAD segments pass16KB alignment checks. The bundletool JAR hash and every command/output remain under `binaries/`. No private key is read by the binary verifier.

All162 frozen application inputs stay identical during build/audit. Relative to G,159 prior inputs match, `LumaView.java` and `app/build.gradle` change, and `PlaySceneLayout.java` is added. The28 engine files,2 audio files and23 v117 PNGs remain byte-identical. See `change-from-g.json` for their exact hashes. This freeze covers `app/src` plus five build/config inputs; it does not purport to pin the whole checkout or Gradle wrapper.

The four byte-identical copies below are read-only in `/tmp/luma-v117-candidate-h/`; `candidate.json` records metadata and hashes in both locations.

| Artifact | Bytes | SHA256 |
|---|---:|---|
| `luma-drift-1.17.0-rc6.apk` |71,827,197|`15546854a856ecc5052aa79acdd49724439b2082fd638da68cf7747be1924a47`|
| `luma-drift-1.17.0-rc6.aab` |74,724,470|`cfad9ff262e843ef0a2c7982964ff7ca41abf0e6df57cefa2c7c6b3d7d4e799b`|
| `luma-drift-1.17.0-sandbox-rc6.apk` |71,826,884|`0cc905def9679c6c4e3b63b90f454137abf6ad9bc431f86e9a7d175891cbde4b`|
| `luma-drift-1.17.0-sandbox-rc6.aab` |74,716,146|`034317b1af295cfc9f2acb9c621f446218bc9e5967182bddf2af1be0b40e8e75`|

Freeze SHA256: `b6af495fadab8c20cf6379b591297da38e10edb4b694aad0adcf521f75711b88`. Binary report SHA256: `d742dfd6fe41642672dbd39e8cb2b510bbc6df11b445e21079a97a891b473c70`.

This dossier establishes local build/binary integrity. It does not establish native H acceptance or publication. G's Guardian return fix passed its actual native regression, while G's independent visual review rejected the legacy HUD. H's shared geometry/source review is under `../review/source-candidate-h-preflight.json`; the new exact-H native HUD/overlay smoke is a release gate before a further1000-level replay.
