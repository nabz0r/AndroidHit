# v1.18 archive and publication scope

The user requested publication of this fan prototype. Git staging and transport are separate from the exact H APK/runtime checks. The mechanical inventory does not determine intellectual-property permission; the [Riot policy note](publication-preflight/riot-policy.md) records that separately.

The proposed offline-audit source selection contains **1,194 paths / 801,972,189 bytes**, plus public website files and compact final evidence. It retains all 296 runtime art files, 148 canonical original PNGs, all 148 pre-bounds metadata files, the 15 superseded PNGs actually referenced by the integrity report, and the G rig revision receipt with all 148 before JSONs. Source prompts, gallery entry points and final H JSON reports remain in Git. The source selection is [listed here](publication-preflight/proposed-audit-ready-git-paths.txt).

Run the full structural/RGBA audit after installing Pillow and NumPy:

```sh
python3 scripts/v118-art/audit-assets.py --rig-revision-receipt docs/validation/v1.18/rig-g-all-proposals/applied.json
```

## Retained locally, excluded from ordinary Git

No file was deleted, reset or relocated. **5,375 files / 6,641,167,465 logical bytes** remain at their original repository-relative locations. These include intermediate/rejected/staged results not needed by the current audit, repeated observer source snapshots, unselected native/diagnostic captures, R8 mappings, observer APKs and intermediate signed candidates. The final H APKs and their public copies also remain local for separate binary distribution; they exceed GitHub's ordinary Git file limit.

The portable [SHA-256 index](publication-preflight/local-retained-sha256.json) gives each excluded file's relative path, size and digest. Index SHA-256: `8aabb7b3b678c9599060a29b3b67cfc7d66b1835d84742d197933f25dc793f55`. Its `archiveUri` is null: an external archive upload is **not** claimed. Copy files with their relative paths, verify against this index, and record a durable archive URI before claiming remote availability of the complete historical archive. Previous E/G failures remain retained and indexed; H results do not replace them.

Twenty selected H PNGs remain in Git, including fresh first light/home, original studio controls, FR/EN site sources, compact controls and cold entry. The public website's own captures and future site evidence are not excluded. Links from retained documentation to an excluded intermediate path mean **local-retained evidence**, not a public downloadable file. Signature/package/manifest verification is independent of these archive choices.

## Binary distribution

GitHub [blocks ordinary Git files over 100 MiB](https://docs.github.com/en/repositories/working-with-files/managing-large-files/about-large-files-on-github). The H production and Lab APKs are 368,887,773 and 368,887,460 bytes. Distribute their exact signed bytes using a release asset or the owner's hosting transport, not a Git blob. GitHub's [release documentation](https://docs.github.com/en/repositories/releasing-projects-on-github/about-releases) currently permits each release asset below 2 GiB. No binary distribution was performed by this audit.

The appended v1.18 ignore rules are recorded in [gitignore-application.json](publication-preflight/gitignore-application.json); no files were staged or committed by the inventory/archive step.
