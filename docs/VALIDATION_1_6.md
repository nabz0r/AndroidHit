# Luma Drift 1.6 — qualification record

27 October 2026. Production `1.6.0-rc1`, Lab `1.6.0-sandbox`, versionCode 7. This is an internal playable candidate. Technical acceptance does not establish player retention, commercial success or provider approval.

## Exact artifacts

| Channel | APK SHA-256 | Bytes |
|---|---|---:|
| Production | `3d840e9a7598ca28d4bf12525b312f4d4f671fc613d5ae88082819547abfc1c1` | 5,528,646 |
| Lab | `0f8d7c04df45331b844b276c8149d9c276e8687f190c2cc7d2e1e236fbaa1a45` | 5,528,321 |

[Production manifest](../releases/manifest-1.6.0-rc1.json) · [Lab manifest](../releases/manifest-1.6.0-sandbox.json) · [All binary hashes](validation/v1.6/binary-sha256.txt).

Both release variants and bundles build successfully with JDK 21/Java 17 source, Gradle 8.14.3, AGP 8.11 and Android 36. Both release lint tasks complete with 0 errors and 8 warnings each (durable commit, Credential Manager generic error handling, tool-version notices, draw allocations and native preview text). [Production lint](validation/v1.6/lint-production.txt), [Lab lint](validation/v1.6/lint-lab.txt). [Exact build log](validation/v1.6/release-build.txt). APKs pass apksigner with the pinned public RSA certificate (`dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287`); both signed AAB payloads pass JarFile verification and pinned official bundletool 1.18.1 schema validation. Both APKs pass ZIP alignment with `-P 16`; eight 64-bit native payloads / 20 LOAD segments satisfy 16KB ELF alignment. Sources and artifact hashes stay unchanged during verification. This binary check alone does not prove runtime on 16KB pages or Google Play acceptance. [Binary evidence](validation/v1.6/binary-verification.txt), [machine-readable audit](validation/v1.6/binary-final-candidate.json).

## Host-side checks

| Area | Result | Scope/evidence |
|---|---:|---|
| Frozen V3 progression | 582,831 checks | [1,240 boards, independent exact oracle, seal dependencies, certificates, codes, budgets/history](validation/v1.6/progression-tests.txt) |
| Original engine | 437,039 assertions | [Original/event/dual-gate cases](validation/v1.6/engine-tests.txt) |
| Move budget | 390,327 checks | [Policies, corruption, continuation accounting](validation/v1.6/move-budget-tests.txt) |
| Historical difficulty | 161,985 checks | [Preserved G/H audit](validation/v1.6/difficulty-tests.txt) |
| P sealed prototypes | 10,425 checks | [All subsets × 16 gates](validation/v1.6/sealed-tests.txt) |
| Store/data/localization | 8,882 checks | [Store data](validation/v1.6/data-tests.txt) |
| Earned economy | 6,608 checks | [Prices, loot, gold-model accounting](validation/v1.6/economy-tests.txt) |
| Collection/events | 87,121 checks | [Ownership, calendar periods](validation/v1.6/collection-events-tests.txt) |
| Inventory presentation | 1,482 checks | [Read-only snapshots and source states](validation/v1.6/inventory-tests.txt) |
| Layout geometry | 1,491 checks | [Width, insets, scrolling, 12dp floor, font cap](validation/v1.6/layout-tests.txt) |
| Identity session | 16 scenarios / 203 assertions | [Real controller with SDK doubles](validation/v1.6/identity-tests.txt) |
| Receipt recovery | 42 checks | [Model continuation receipts](validation/v1.6/commerce-receipts-tests.txt) |
| Wallet lifecycle | 19 checks | [Provider doubles](validation/v1.6/wallet-recovery-tests.txt) |
| Billing policy/copy | 248 checks | [Policy checks](validation/v1.6/billing-policy-tests.txt) |
| Meta/journal | 362 checks +JSON export | [First clears, claims, journal](validation/v1.6/meta-tests.txt) |
| Audio | 37 lifecycle/focus +65 DSP | [JVM model and synthesized PCM](validation/v1.6/audio-tests.txt) |

These checks use JVM Android/storage/provider adapters where named. They do not perform a payment, an ad completion or real Google authentication. Eight historical content family fingerprints match before/after, and frozen V3 generation is byte-identical. [Compatibility](PROGRESSION_V3.md#frozen-identities-and-save-compatibility), [reproduction](validation/v1.6/progression-reproduction.txt).

An initial audio-model race and its failed output are retained; the test now waits for the worker's first scheduled fade before advancing virtual time, retaining the volume assertions. Android audio code is unchanged. [Failure and correction](validation/v1.6/audio-test-handoff.md).

## Installed Android acceptance

| Installed check | Actual result | Evidence |
|---|---|---|
| Final Lab menus/layout | 88 checks PASS: six languages, All/Owned/Locked, previews, back stack, 320dp, landscape scrolling and font preference 1.3 | [Report](validation/v1.6/ui-final-lab/report.json) |
| Final production menus | 43 checks PASS: menus, inventory, prelevel navigation, EN/FR; no spending | [Report](validation/v1.6/ui-final-production/report.json) |
| Actual 16KB kernel | Both final packages installed/launched on API 36 arm64-v8a; `getconf PAGESIZE=16384`, 440dpi | [Measured environment](validation/v1.6/ui-device-runtime.json) |
| Actual 1.5→1.6 upgrade | Lab API 26: real G1 run, 8/9 actions/history/UUID/stocks/coins preserved; J1 preview cancelled without loss; completed old G1 then fresh J2 at 11/11 | [Upgrade](validation/v1.6/upgrade-api26.json) |
| Final production audio | Same AudioFlinger track advanced 22,491 frames, paused in background and resumed foreground | [Audio runtime](validation/v1.6/audio-runtime.json) |

Menu runs restore all preferences byte for byte and restore display dimensions, density, rotation and font preference. Upgrade tests start from an absent application and restore that initial absence. The screenshots in README come from the exact final production APK. The companion-tab caption abbreviates at 130% font preference; its accessible label and item cards remain complete. These observations do not establish a complete accessibility review.

The final Lab powers/gameplay run uses an explicitly synthetic emulator profile (Journey access, 10,000 local test coins, six charges and one equipped charm). Every original preference file is restored byte for byte. This is not earned progression or real IAP.

- Spark: preview/cancel costs 0, confirmation costs 1, active reactivation and process recreation cost0.
- Flow: pause, Help and process recreation freeze observed intermediate exits; explicit resume produces exactly 3 total exits/actions and one charge. Final-piece Flow wins once with no reward/debit replay.
- Shield 3 precedes Charm 1; the first four blocked attempts are protected, the fifth spends one action; recreation retains remaining protection.
- Final Journey page exposes 991–1000 only; the certified 1000 UI solution wins 3 stars and Next does not create 1001.
- Starfold local purchase: preview/cancel costs 0; explicit confirmation spends exactly 600 synthetic earned coins, owns/equips once, and survives recreation.
- Vision/Focus/Trail: one charge, no second charge while active, bounded saved duration and exact targets through pause/background/recreation.
- Journey 12: key opens real seals, Undo restores them, replay reopens, and certified completion fits the fixed budget without a power or continuation.

[Consolidated powers/gameplay acceptance](validation/v1.6/powers-ui-smoke.txt) and its [machine-readable case provenance](validation/v1.6/powers-union.json) identify each completed case. The individual PASS scenarios above are the union of the [first run](validation/v1.6/powers-starfold-selector-failure.txt), [equipment run](validation/v1.6/powers-pref-read-failure.txt) and [effects/key run](validation/v1.6/powers-effects-keys-ui-smoke.txt), all on the exact final Lab hash. Earlier runs stopped at a hidden pagination selector, an ambiguous item/slot prefix, or a raw XML read racing a preference write; these pilot failures and profile restoration evidence are retained. The new pilot now scrolls to the footer, selects the full item card and uses bounded stable XML reads. Product assertions were preserved; passed cases were not relabelled as a single uninterrupted suite.

Flow rotation also passes for both Pause and Help: the overlay remains after landscape/portrait recreation, no exit occurs while frozen, and explicit resume completes exactly three total exits/actions with one item and no stale callback. [Actual rotation report](validation/v1.6/powers-rotation-ui-smoke.txt).

Final production identity PASS on the exact `3d840e9a…` APK: Guest → actual Google Play services provider → Android Back before account selection → Guest with enabled sign-in; Home/background/relaunch remains Guest without automatic provider reopening. All three preferences and the existing P1 session are restored byte for byte; crash buffer is empty. [Final identity report](validation/v1.6/identity-final-ui.json), [readable sequence](validation/v1.6/identity-final-ui.txt). No real account credential was selected, authenticated or deleted. Successful authentication, real revocation/reconnect and account operations remain unqualified. The earlier candidate identity report is kept with its own hash.

## Boundaries

The 12dp text minimum can grow to 15.6dp with font preference 1.3; larger preferences remain capped. Several compact controls and eight-cell board targets remain below 48dp. Automated geometry and visible emulator captures do not establish a complete TalkBack audit, 200% text adaptation, calibrated contrast for every overlay or physical-phone legibility.

No authorized Google account was selected during automated work. Provider opening/cancellation and deterministic token/session tests are separate from successful real OAuth/Firebase login, reconnect, account switching, sign-out or deletion. Production configuration keeps sample ads forced and the wallet origin empty. Earlier hosting/deletion owner progress is preserved in its dated records; this release does not silently activate purchases or live ads.

No human difficulty study, D1/D7 cohort, revenue result, physical speaker/headphone audition, Bluetooth test, thermal/battery benchmark or physical-device acceptance is claimed. Rotation of an already completed victory returns to Home; its earned reward remains durable. Active pause/help/restart overlays are saved across Activity recreation so Flow does not silently resume.
