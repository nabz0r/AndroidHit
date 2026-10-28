# Luma Drift 1.15 — a redesigned Journey and a larger home

28 October 2026 · production **1.15.0-rc1**, Lab **1.15.0-sandbox-rc1**, Android version code **17**.

**Delivered locally.** The frozen E artifacts complete native acceptance: all1,000 U boards, normal/compact interfaces, six languages, actual Lab upgrade, powers, earned decoration, cold persistence and wrong-order recovery. Both task-owned QA emulators are stopped with their profiles retained. No remote publication is implied.

The release responds to a concrete player report: manual gate openings felt like an extra click without a useful decision, the home was too small, objects differed too little, and long pages and text-heavy panels made the interface crowded.

## Resulting behavior

- Four dedicated destinations: Adventure, Home, Collection and Atelier. The puzzle has its own screen, with no global navigation or wallet. Adventure opens the next level directly.
- A new 1,000-board U route: pieces control gates, keys release seals and order creates consequences. U has no manual gate-opening toll. Every correction is freely reversible and rejected taps cost nothing.
- Larger portrait-oriented scenes, a visual six-item inventory grid, an enlarged wardrobe with separate clothing/equipment selection, an illustrated power tray and concise victory panels.
- Three independent 10 × 10 rooms, shared zoom/pan and touch geometry, forty-two different furniture designs and two orientations. Purchases and placements remain separate and durable.
- Six-language navigation, rules and catalogue. Existing earned balances, equipment, ownership, historical challenge boards and active runs remain reconstructible.

## Delivered bytes

[Production APK](../releases/luma-drift-1.15.0-rc1.apk) · [Production AAB](../releases/luma-drift-1.15.0-rc1.aab) · [Production manifest](../releases/manifest-1.15.0-rc1.json) · [Separate Lab APK](../releases/luma-drift-1.15.0-sandbox-rc1.apk) · [Local delivery register](validation/v1.15/delivery-local.json).

The production APK uses the existing `com.androidhit.lumadrift` package and pinned signing certificate, with version code17. Install it over the existing signed production app to retain local data. The separately named Lab package does not update production. The observed migration is explicitly a Lab 1.14 →1.15 test on the isolated QA emulator.

Production APK: **7,970,422 bytes**, SHA-256 `07b5bfc2632edc3d34f7583ac71c8343b52d7b926ed526aeb7a88599253df0f9`.

Lab APK: **7,953,725 bytes**, SHA-256 `edf9810ec093e6dd87bf59c25a72e310e1f9a478ab15b85a87532b97333200df`.

The [final delivery comparison](validation/v1.15/delivery-integrity.json) confirms all four released artifacts equal frozen E byte for byte and all121 app/build inputs remain unchanged. Immutable inputs and artifacts are indexed in [candidate E](validation/v1.15/candidate-e/candidate.json). [Binary verification](validation/v1.15/binaries-e/binary-final-candidate.json) passes for signatures, manifest identities, bundle validity and static 16 KiB alignment independently of runtime qualification. Earlier candidate images and failed observer attempts remain dated intermediate evidence. E fixes a genuine compact-room defect found in D: expanded furniture touch targets now stay centred and their outer margin responds, while foreground sprite depth retains priority.

The [continuous U1–U1000 Android route](validation/v1.15/relay-native-summary.json) passes49,575 native checks and12,016 actual touchscreen gestures, earning55,000 first-win coins without injected state. The [separate recovery probe](validation/v1.15/deadend-native-final-e/host-report.json) passes167 checks: a legal bad order creates a real dead end, free Undo restores solvability, cold launch retains it and an actual public-challenge victory changes no campaign rewards. Other native interface, upgrade and purchase scopes remain separately indexed.

An older active puzzle keeps its original rules. To start U immediately at your current Journey level, tap its numbered node in Adventure. Resume and Restart on the older attempt preserve that old board.

## Scope and limits

The home supports one instance of a design per room; it does not implement arbitrary quantities within one room. All three rooms are directly accessible. U stars acknowledge completion; no extra optimization score or measured human difficulty is claimed. Portrait phone layouts are the supported presentation. The pre-existing website, Play publication and provider activation are outside this APK delivery.

[Gameplay and compatibility](GAMEPLAY_1_15.md) · [Evidence](validation/v1.15/README.md) · [Design audit](AUDIT_REFONTE_UI_2026_10_28.md).
