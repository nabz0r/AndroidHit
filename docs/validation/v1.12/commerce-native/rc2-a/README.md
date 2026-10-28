# Intermediate Commerce browse — retained provenance limit

This first attempt ran the production RC2 APK `de581253b64232ae45bcbd647862dd470d522068f26516f6417bf515e882403d` and recorded 45 passing native assertions / 36 original full-frame screenshots in six languages. The [host record](host-report.json) verifies exact restoration of the three original guest preference files, retained Lab APK/preferences and prior ordinary test package. No game source, financial action, ad opt-in or identity action was changed.

**This attempt is not the current qualification.** The host recorded the actual test APK at installation as `6182d5d65d8a149a8dd1f65447e2387aa5f32016026abdd5727122e140aa34d1`, but those exact APK bytes were not preserved. A second harness build subsequently replaced the shared generic AndroidTest output; the later file had SHA `34897345f94597989fc91b1d4924711a16a092faab7345b84eec2d14f62d151e` and a different runner. That later file is not evidence of the tested Commerce APK.

The original [native report](report.json), [navigation actions](actions.json), node dumps and PNGs remain intact. The [runner snapshot](runner-source/) matches the per-run source SHA-256 inventory exactly. `run.py` and `init.gradle` were reconstructed by reversing only the later isolation patch and were hash-checked against the original host record; this is source provenance, not a retained binary claim.

The replacement qualification uses a dedicated test package, a signed APK copied to a unique path inside the packaging task, and an explicit installed-test-APK SHA verification. See the parent [Commerce result](../README.md) for the current isolated replay. The original runtime result has not been relabeled as failed or rewritten to match later evidence.
