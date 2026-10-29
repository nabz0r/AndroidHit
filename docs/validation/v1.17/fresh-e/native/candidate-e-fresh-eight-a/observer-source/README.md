# 1.17 E fresh first-run UI

This separate framework observer has no target app/model/engine/renderer,
preference or reflection dependency. It sends actual touchscreen input to
visible SDK accessibility targets and records original full-frame captures.
Only the newly created receipt-owned `Luma117-Fresh-E-QA` / `emulator-5590` is
allowed; no existing AVD, especially retained5586, is addressed.

`build.gradle` registers one standalone task without target compile/package/
bundle dependencies. It compiles against public Android36 framework classes,
packages only its own observer and frozen U1–U10 guidance JSONs, and signs with
the existing release signing configuration without printing credentials:

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
  ./gradlew -I scripts/v117-fresh-ui/build.gradle :app:packageV117FreshObserver
```

No app source or four target packages are rebuilt. Their before/after hashes
are retained separately. The wrapper permits only E SHA-256
`48134c248b88a277d93d6745be6efd7a825f13629ea591e6b41e11cbefabf1eb`,
production code21 and the expected release signer.

Phases are `onboarding` (genuine install, three First Light exits, starter home,
all seven practice lessons and the legitimate once-only gift), `campaign`
(eight first U victories using real touches), `arrivals` (180+260 actual local
coin draws and equipment) and `cold` (distinct process, exact observed wallet,
equipped-portrait description and last result). Each phase requires a fresh
evidence label. Only onboarding can install the target, and only if absent.
It never clears, reinstalls, seeds or writes target state. Failed attempts are
retained; resume later phases on the same real save rather than starting over.

`run.py --phase PHASE --label UNIQUE_LABEL` records into
`docs/validation/v1.17/fresh-e/native/`. Cold additionally takes `--checkpoint`
pointing at the preceding phase's observer-owned UI checkpoint JSON. This is
an observation file, not a target save. At the end, remove only this exact
observer and stop only the newly created AVD normally, preserving userdata and
the ownership/creation/shutdown receipts. No provider/account/ad/payment action
belongs to this route.
