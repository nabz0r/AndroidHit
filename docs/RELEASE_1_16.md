# Luma Drift 1.16 — a closer home and a player identity

29 October2026 · production **1.16.0-rc1**, Lab **1.16.0-sandbox-rc1**, Android version code **18**.

The home no longer shrinks twice. Its camera uses the actual scene area, starts at a closer framing, pans with a drag and offers an explicit whole-room view. The existing42 furnishings and three10×10 rooms are preserved. Collection and Atelier use larger original illustrations; accented pill actions and icon medallions replace the previous repeated rectangular treatment.

The local player card now has an editable1–18-character nickname, a chosen flag and three badge slots. Six badges record actual victories, furnishing and collection milestones; a locked badge cannot be equipped, and an earned badge stays earned after redecorating. This identity is stored on the device separately from the Google account name.

Two new earned-LumaCoin banners have their own fixed, versioned pools: **23 companions for180 coins** (free Nova excluded) and **five complete four-piece outfits for260 coins**. Exact per-item odds and duplicate returns are visible before a draw. Debit, outcome, ownership and the last-reveal receipt commit together; replaying a receipt never charges again. Equipping an outfit changes all four clothing slots atomically. Existing direct purchases, individual clothing capsules, ownership and12 free avatar styles remain available.

The five complete outfits assemble existing original clothing designs; this edition does not claim23 newly drawn avatars or five entirely new sets of artwork. Puzzle-engine sources remain byte-for-byte equal to the qualified1.15 engine. The prior continuous1,000-level run is historical evidence, not a new1.16 marathon.

## Delivery and evidence

Final candidate **B** production APK: **7,986,806 bytes**, SHA-256 `516be7f14ff2b80683de658b6df767f4b19dbb44580d57be36a307d330ed5a66`.

[Production APK](../releases/luma-drift-1.16.0-rc1.apk) · [Production AAB](../releases/luma-drift-1.16.0-rc1.aab) · [Manifest](../releases/manifest-1.16.0-rc1.json) · [Separate Lab APK](../releases/luma-drift-1.16.0-sandbox-rc1.apk).

The independent four-binary signature, manifest, bundle and static16KiB alignment audit passes against126 frozen app/build inputs. Both release lint tasks pass with zero errors and13 existing warnings per flavor. [Binary evidence](validation/v1.16/candidate-b/binaries/binary-final-candidate.json) · [Qualification register](validation/v1.16/README.md).

Final B passes993 native assertions plus four pre-update baseline assertions, with263 original images across normal/320dp, six languages, cold persistence and complete23-avatar/five-outfit catalogues. [Exact aggregate](validation/v1.16/native-final-summary.json). Website publication has a separate receipt. Earlier A evidence remains distinct: a real1.15→A upgrade preserves the retained profile, then native draw/equip/cold tests pass; the final B adjusts clothing-title spacing discovered in visual review. No Play or GitHub release publication, real provider checkout or physical-phone performance is implied by the local binary checks.

Install the signed production APK over the existing production app to retain the local save. The separately named Lab package does not replace production. Existing active puzzles retain their original rules.
