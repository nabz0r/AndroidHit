# Full UI smoke before the sharing-image repair

The installed production APK was `1.5.0-rc1`, code 6, SHA-256 **`24a128e3f25fc73e698e46e7d46a30b3358e8d88f08dd59d2b9e8984e3ddf049`**. This run finished on 27 October 2026 at 01:04:51 UTC: **58 cases passed, 96 captures** across English, French, German, Spanish, Portuguese and Italian, plus English at 720×1280 and 1280×720 with density 320.

[JSON results](ui-smoke.json) and [text results](ui-smoke.txt) retain the exact installed identity, timestamps, cases and capture paths. All PNGs and XML snapshots were archived together. The copied [binary/source manifest](binary-candidate.json) binds the earlier binary to its source hashes; [helper source bytes](smoke-v15-source.txt) preserve the version used for this run. The source snapshot is evidence, not an executable entry point. Use the current [`scripts/smoke-v15.py`](../../../../scripts/smoke-v15.py) to reproduce a selected scope.

This report predates the independent `ShareComposer` PNG-layout repair and does not qualify sharing or a rebuilt APK. A later English/French regression report must record the new installed APK hash. It must not relabel the other four languages or these two display overrides as newly executed on that later binary.

## Observations and limits

- Home, Settings, the guest account page, all three collection tabs and their owned filters, the Lantern Leaf preview, both power-shop pages, a cancelled Spark offer and all six Sealed passages menu entries passed in every requested language/layout. The helper inspected the first catalog page of each tab, not every item preview.
- The observed profile had one Journey clear, 235 local coins, three Spark items and the three starter equipment items. No profile was seeded or cleared. All eight closed Spark offers retained 235 coins and stock 3; quantity ×3 and price 45 matched the shop card.
- The final one-coin wording and earned-currency confirmation appeared in the installed APK. A second reader inspected the ten main EN/FR home, equipment, preview, offer and sealed-menu images without finding blocking clipping, overlap or amount/benefit disagreement. Additional DE/ES/PT/IT offer samples and both display-format samples were inspected visually.
- Landscape keeps the app in a centered, reduced portrait column. Navigation and digital bounds passed, but this is not an adaptive wide layout or evidence of physical-phone reading/touch comfort. Long native equipment text uses a scrollable dialog and is not all visible in its initial landscape viewport.
- The caller's explicit English choice and original display overrides (`size=reset`, `density=reset`) were restored, with no restoration errors. No gameplay, login, ad, purchase, equipment change or explicit reward claim was confirmed. Normal app entry can record a visit or persist an already-eligible local unlock.
- Accessibility labels/XML and digital bounds do not establish TalkBack usability, native-speaker editorial quality, physical-device quality or human retention. Separate power, migration, audio and sealed-board reports cover their own stated scopes.
