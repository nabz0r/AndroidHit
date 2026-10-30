# Workspace cleanup — 30 October 2026

The owner requested removal of heavy and obsolete local files alongside the
Cognitive Lab delivery. The cleanup removes reproducible, ignored outputs and
compacts Git storage while retaining the complete recorded history and evidence.

| Operation | Result | Measurement |
| --- | ---: | --- |
| Ignored output removal | 22,120 file/link entries; 3,704 directories | 4,968,416,699 logical bytes; 5,058,641,920 allocated bytes |
| Git storage compaction | All 31,335 object IDs and 16 refs retained | 6,097,510,400 allocated bytes saved |
| Combined allocation reduction | 11,156,152,320 bytes (11.16 GB) | Sum of the two scoped allocation measurements |
| Volume free-space observations | +5,046,661,120 then +6,078,746,624 bytes | +11,125,407,744 bytes (11.13 GB), including concurrent volume/APFS effects |

Logical file lengths, allocated blocks and the free space of the entire APFS
volume are different measurements. The two volume observations are bounded
before/after readings, not exclusive physical attribution to individual files.

Removed directories are `app/build`, root `build`, repository `.gradle` and
`server/node_modules`, plus three ignored Python cache directories and two
Finder metadata files. Every target was ignored and contained zero tracked
files. No backend process was using the dependencies. Java 17 Gradle status
showed an idle daemon, which was stopped before removing compilation caches.

Sources, signing material, Android saves, the global Gradle cache, historical
validation captures and canonical/local/public releases remain. Large historical
1.18 intermediate evidence also remains under its existing
[archive scope](../v1.18/ARCHIVE_SCOPE.md); it is not silently treated as disposable.
No public download is removed and no Git history is rewritten.

## Repeat or restore

The [cleanup script](../../../scripts/clean-workspace.py) defaults to a dry run:

```sh
python3 scripts/clean-workspace.py
python3 scripts/clean-workspace.py --apply --receipt docs/validation/cleanup-YYYY-MM-DD/applied.json
```

Its fixed allowlist refuses tracked targets, symlink targets/ancestors, parent
traversal and unrelated source paths. It skips dependencies when an active
backend is found and refuses compilation cleanup when Gradle reports a busy
daemon. Receipts record subprocess outcomes and separate size/free-space fields.

Build outputs regenerate with the normal Java 17 Android build. Restore server
dependencies with `npm ci --prefix server` using the committed lockfile. Python
caches regenerate on demand. No Android rebuild was needed to validate removal
of ignored outputs; exact existing release copies were hashed separately.

Git maintenance used `git gc --prune=never`, with reflog and stale-worktree
expiration disabled. All object IDs, refs, HEAD and index bytes were compared
before/after. `git fsck --connectivity-only` passes; retained dangling objects are
allowed and summarized in the receipt.

## Evidence

- [Dry-run inventory](dry-run.json), [applied removal](applied.json), and
  [final dry run: zero remaining targets](after-dry-run.json).
- [Six refusal/dry-run boundary checks](guards.json).
- [Source hashes, four latest release copies and zero tracked deletions](post-checks.json).
- [Git compaction and connectivity receipt](git-maintenance.json).
