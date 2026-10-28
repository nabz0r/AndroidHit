# Host dead-end fixture for the native U probe

Run `sh scripts/v115-deadend-ui/generate.sh` from the repository. An optional first argument chooses another U level; the second chooses an output path. Requires Java 17 and Python 3. The default uses U31 and writes `docs/validation/v1.15/deadend-fixture.json`.

The generator compiles the existing unmodified host engine, resolves an actual public U challenge code, searches breadth first over legal exits from soluble prefixes, and exports a shortest legal path into an insoluble state. It replays the full witness, performs one real `Board.undo()`, checks the exact recovered history, then legally replays a full recovered solution. The JSON includes the exact source hashes. Output has no timestamp and can be regenerated and compared byte for byte.

`row` and `col` are zero based. `displayRow`, `displayCol`, and the English native accessibility label prefix are one based. `steps[].before` and `steps[].after` include the history, numerical gate mask, named open gates, remaining pieces, legal exits, and mechanical status. `undo.after` is the expected state immediately after one Undo; `undo.solutionSteps` can complete the recovered board through real SDK touches.

For the public deep link, E uses session mode 4 and content version 4; the resolved board itself uses relay progression version 6 and budget policy 5. A friend challenge has an unlimited budget and `budget_used` remains zero. These session expectations describe the source contract and require native confirmation.

This is host engine evidence, not an independent rules audit or proof of native interaction. It does not connect to a device or emulator, modify Android preferences, or change app sources. The native observer must import only the generated fixture and use the public deep link and SDK accessibility/touch APIs.
