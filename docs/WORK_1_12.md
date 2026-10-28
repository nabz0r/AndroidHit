# 1.12 implementation and acceptance record

Request: reduce popup/box density, improve the shop and game experience, expose LumaGold, make items more appealing and support clear cognitive choices.

Baseline: `954efd5ea63c4d471816202ece05d31bb9e55016`, delivered 1.11 RC2 / code 12. Original historical sources, artifacts and qualification records remain unchanged.

Implemented:

- Dual named currencies, distinct silhouettes, explicit unknown/last-known account balance. Cache is UID/version scoped, read only at UI refresh/resume/auth changes, and never authorizes a spend.
- Lighter shared surfaces and pill buttons; open Home/shop illustrations and original six-power characters shared across the native game.
- Normal item pages replace modal browsing/equip/earned-purchase dialogs. Explicit purchase then Equip; receipt-bound capsule details/result; semantic effect/stock/source text.
- Context-preserving power details from a live board and restoration of long-page height/scroll.
- Pastel account-wallet layout with source prices/disabled controls, consent and shorter purpose-specific ad flow preserved.
- Reproduced and corrected the pre-existing untracked earned-power purchase failure using the existing local transaction mechanism.

Host work: 438 independent art checks, 150 actual wallet-cache/commerce-copy checks, 99 game-copy checks; existing 683 appearance, 41 initialization and 17 external-ownership regressions pass. The focused power purchase failure suite and retained economy/local-save regressions are separately recorded under [store validation](validation/v1.12/store/).

RC1 native preview: the first attempt failed on a fixture expecting `Stock:` where the actual existing detail copy says `In stock:`. Its raw failure is preserved. The corrected preview passes 184 checks / 41 original PNGs, including all six shop locales and a genuine earned three-power pack purchase. It is intermediate evidence, not final-byte RC2 acceptance.

Final Lab RC2 passes 1,179 checks over 40 actual Journey clears, with 96 original PNGs + 19 analysis crops, 16 independently verified art comparisons and a separate cold process. The original profile and rotation are restored.

Separate final production startup/audio passes 7 checks/1 original, earned shop passes 35/8 originals, and account-wallet UI passes 45/36 originals across six languages. All retained profiles are restored exactly. The first Commerce test APK was not retained before a shared Gradle output was replaced; its observations remain intermediate. A dedicated test application ID and unique frozen test artifact remove that ambiguity in the final replay.

Final build, package and installed qualification are recorded in [the current validation index](validation/v1.12/README.md). No server, Hosting or Play mutation is part of this UI release.
