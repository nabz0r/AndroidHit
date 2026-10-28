# Scoped native UI results — 1.12 RC2

| Exact scope | Result | Original frames | Explicit analysis crops |
| --- | --- | ---: | ---: |
| [Lab tutorial, levels 1–40, menus, power recovery, six shop languages, earned appearance/raster and real cold process](ui-sdk-final-rc2-lab40/README.md) | PASS: 1,179 checks (1,157 + 22 cold) | 96 | 19 |
| [Production startup and lifecycle/audio](ui-sdk-final-rc2-production-startup/README.md) | PASS: 7 checks | 1 | 0 |
| [Production narrow earned shop flow](ui-sdk-final-rc2-production-shop/README.md) | PASS: 35 checks | 8 | 0 |

Lab APK `1.12.0-sandbox-rc2` / code 13 SHA-256 `0f55c38c29d5872588200a5675cb154809eb62897d1133d96b48a82ab12b91e4`. Production APK `1.12.0-rc2` / code 13 SHA-256 `de581253b64232ae45bcbd647862dd470d522068f26516f6417bf515e882403d`.

All three use explicitly owned emulator 5580 / `hexkeep-play-36-16k`, Android 36 / ARM64 / real 16 KiB pages, with version and installed APK hash checks. [Frozen independent test inputs](test-builds/rc2-source/inputs.json) and [test APK identities](test-builds/final-test-apks.json) are separate from the game. Each installed test APK was also measured on the device. All three restore the original preference bytes; [cross-integrity](integrity-after-production.json) verifies that production work left the Lab package/test/preferences unchanged and retained the original rotation state. No user emulator 5582 action occurs.

Lab alone performs the 40 actual Journey clears, 120 clear/star/budget checks, six-language shop route, real earned clothing/companion/theme purchases, actual equipped raster and forced-process cold persistence. Independent Pillow verifies 19 retained pixel-exact crops and recalculates all 16 native deltas. No saved ID alone is treated as proof of a visible item. Purchases and Equip are separate actions.

The truly empty-power detail case consumes four earned Spark charges through actual confirmations, makes a real move, then verifies that Close plus rotation/System Back return to the same live run/history/budget. It does not synthesize stock or progression. The shop purchase checks the exact displayed stock/debit. Preview and Close remain read-only.

## Preserved intermediate record

[RC1-a](ui-sdk-preview-rc1-a/host-report.json) stops on an independent fixture typo: it expected `Stock:` while the existing English text is `In stock:`. The actual semantic stock and effect were present. Its original failed report/screens remain unchanged. The corrected [RC1-b preview](ui-sdk-preview-rc1-b/host-report.json) passes 184 checks / 41 originals across menu and six-language shop browsing; its test inputs remain under [test-builds](test-builds/README.md). It is not relabelled as final RC2 evidence. The RC2 stock oracle reflects the deliberate removal of the technical `/999` cap from visible item text.

These scopes do not establish human comprehension, enjoyment, retention, physical-phone quality, real provider acceptance or Play readiness. They do not deploy Hosting or a server. The separate Commerce UI qualification on K5586 has its own inputs and reports and is not counted in this table.

[Cleanup receipt](qa-device-shutdown.json) records the subsequent graceful stop of only owned 5580, preserving its AVD directory/data and installed candidates. It does not control K5586 or user 5582.
