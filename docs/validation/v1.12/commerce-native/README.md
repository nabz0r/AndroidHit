# Commerce native qualification — production 1.12 RC2

**Current result: PASS, 45 native assertions and 36 original full-frame PNGs across English, French, German, Spanish, Portuguese and Italian.** The isolated [RC2-b host receipt](rc2-b/host-report.json) pins production `1.12.0-rc2` / code 13, API 36 ARM64 with actual 16 KiB pages on retained `Luma-Release111-K`, emulator 5586.

- Game APK: `releases/luma-drift-1.12.0-rc2.apk`, SHA-256 `de581253b64232ae45bcbd647862dd470d522068f26516f6417bf515e882403d`.
- Signed SDK test APK: `app/build/qualification-artifacts/v1.12/commerce-isolated-rc2-test.apk`, SHA-256 `9c4dafe9ff6c51906a4536666bc15f0e1807b0b4a4ec24d7fd9c1e2fd00d8b65`. Its local bytes are retained outside Git. The wrapper verifies those exact bytes after installation into the dedicated `com.androidhit.lumadrift.commercetest` package. [Manifest](rc2-b/test-apk-manifest.txt), [signature](rc2-b/test-apk-signature.txt), [build](rc2-b/test-build.txt).

The native SDK allowlist navigates through visible Main → Shop → LumaGold → View wallet in each language. It observes named separate currencies, a genuinely unknown premium balance rather than a fabricated zero, the disabled first cosmetic spending button, unchanged explicit advertising opt-out, and unchanged saved economic/progression values. The actual [actions](rc2-b/actions.json), [native assertions](rc2-b/report.json), node dumps and original images are preserved. No login, ad load/watch/opt-in, age change, purchase, simulation, refund or refresh control was invoked. Normal Play catalogue connection/query on entering the wallet is allowed by this protocol; no available paid pack, real price or transaction acceptance is claimed.

The three existing guest preference files were backed up before installing the candidate and restored byte for byte after the run. The retained Lab APK and all its preferences are unchanged. The ordinary SDK test package was never replaced; the isolated test package was removed after qualification. The game was force-stopped after restoration. This is an existing guest browse, not a new profile or a live account test.

The preparation gates remain test ads, empty wallet API URL and paid draws disabled. No Firebase/public website origin was accessed. This does not qualify real Google login, ads, gold purchases/refunds, physical-phone comfort, human comprehension, artistic preference or retention.

Original frames inspected include the French hero and Italian consent section, following earlier English hero/offer and German hero checks: labels and both currency ribbons remain readable, the unknown state is visible, and disabled purchase controls remain distinct. Scrolling can naturally clip adjacent rows at the viewport edge; this is not represented as an overlap failure. Independent [PNG integrity](rc2-b/integrity.json) checks all 36 original hashes, chunk CRCs and decompressed scanline streams. Reproduce with `python3 scripts/v112-commerce-ui/verify.py --label rc2-b`.

[French hero](../../../screenshots/v1.12/commerce-native/rc2-b/commerce-hero-fr.png) · [English offers](../../../screenshots/v1.12/commerce-native/rc2-b/commerce-offers-en.png) · [Italian consent](../../../screenshots/v1.12/commerce-native/rc2-b/commerce-consent-it.png) · [Current runner](../../../../scripts/v112-commerce-ui/README.md)

The [earlier RC2-a attempt](rc2-a/README.md) remains sealed as intermediate evidence with a documented test-APK retention limit. Current qualification and release documentation refer to RC2-b; historical native results are not silently rewritten.
