# Luma Drift 1.18 — local prototype H

## Retour du propriétaire —2026-10-29

L’essai humain de l’APK1.18H signale un échec de clarté de sélection, d’interaction, de navigation et de sensation de fluidité. La refonte UX est ouverte : les acceptances automatisées restent des validations fonctionnelles dans leur périmètre et n’impliquent pas une UX validée. Aucun FPS ni diagnostic causal de performance n’est inventé. Les APK, manifestes et témoins H restent immuables. La [proposition1.19](../../design/v1.19/UX_RETHINK_FR.md) et sa maquette locale sont des éléments de revue, pas une nouvelle implémentation Android ni une évolution de l’économie.


H is the delivered local prototype, code25. The published website and public1.17 release have not been replaced. This register distinguishes the tested H bytes from the earlier E/F/G candidates.

## Exact artifacts

| Variant | File | SHA-256 |
|---|---|---|
| Production | [APK](../../../releases/luma-drift-1.18.0-prototype1.apk) | `3a30526941d69bd6e65cd148c7eb7fa5b47647f85f7b53f9a043b26b5abdbbd2` |
| Lab | [APK](../../../releases/luma-drift-1.18.0-sandbox-prototype1.apk) | `96981f3d9b366713b0842204c69e3a1e46a9e31bfb6d0196b1e278ef0902ee06` |

Both use signer certificate SHA-256 `dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287`. The files are approximately369MB each. [Delivery manifest](../../../releases/manifest-1.18.0-prototype1.json) records sizes, versions, source witness and evidence hashes. The final source inputs were rechecked after native execution.

## H validation

| Scope | Result | Evidence |
|---|---|---|
| Build and lint | Both signed variants compile;0errors/13existing warnings each | [Frozen build](candidates/final-art-h/build.txt), [lint](candidates/final-art-h/lint-summary.json) |
| Art integrity | 148PNG/WebP pairs are RGBA-exact;2,960source polygons;26,640attachments;zero alpha≥128 pixels omitted/shared between domains | [Audit](art-integrity.json), [asset manifest](../../art/v118/assets-manifest.json) |
| Native isolated Canvas | 12,093checks;2,960frames;6,112composites;320PNG;zero decode/metadata failures or static fallback;cache within48MiB | [H renderer](native-motion-render/final-art-h/report.json), [registry](native-motion-render/final-art-h/native/registry.json) |
| Retained task profile | 118checks/73actual gestures/61captures;5processes/3recreations;40studio pose selections, four forms, wardrobe choice and cold restoration | [H UI summary](native-final-h-summary.json) |
| Fresh production | 588checks/170actual gestures/138captures;actual three-piece introduction from four empty preference files; six languages;720×1280/density320; distinct cold process | [H fresh summary](native-fresh-h/final-art-h-summary.json) |
| Packaged bytes | Signers/versions/hashes match;296art JSON/WebP files equal sources and each other across variants | [H package proof](native-fresh-h/final-art-h-package-proof/report.json) |

The only expected variant asset differences are the production-only identity configuration and two variant-specific dexopt profiles; all other common assets must match. The failed older overbroad equality assertion remains recorded.

Studio preview cycles compare the complete four preference maps and leave them unchanged. Wardrobe cycles permit only `companion_original_costume_v118` to change. The retained profile ends with105coins,Ashe equipped at level2/GUARD,Nova2/GUARD,Miko2,Orbit2 and four completed expeditions. The fresh profile ends with180coins and Nova alone; no synthetic grant or imported save was used.

G's targeted body review covers11sheets/220poses and documents residual tiny contacts/fringes. [All320 H native PNGs are byte-identical to G](native-motion-render/final-art-h/identical-render-to-g.json), binding that [targeted review](native-motion-render/final-art-g/targeted-body-review.json) to H. This does not turn a targeted review into universal apparel approval.

The task-owned5594 and5600 devices retain their profiles after graceful shutdown. Earlier5596/5598 profiles were not reset or reused as fresh H evidence. Observers were removed and display overrides restored.

## Model and historical scopes

Current source contracts pass17,559route/model checks,995actual-store checks including118wardrobe-mode checks and52real U certificates,1,093,910motion/geometry checks, and60,714profile/summon regression assertions. Store totals include318certificate actions and677fixed-scope checks; route-dependent action totals can vary. See [model](expedition-model.json), [store](expedition-store.json), [motion](companion-motion-model.json), [profile/summon](profile-summon-regression.json).

Actual expedition wins, earned guest acquisition and all three signatures were recorded on E: [549checks/317gestures](native-final-e-summary.json). H has separate exact-byte presentation/save evidence. E/F/G remain historical candidates with their original failures; their gameplay or visual results are not relabeled as H execution.

## Limits

All37identities have four authored bodies at levels1/3/6/10, each with20poses including four stances. Intermediate copy levels add small details. Fixed rarity is separate from copy level.

Original costumes are the default when no earlier explicit garment choice exists. Existing saved wardrobe selections remain visible. Equipping a garment or full outfit activates that wardrobe atomically; switching back to the original costume preserves ownership and all four selections. The studio's outfit switch is a separate temporary preview.

Explicit legacy clothing overlays still fit some poses approximately. The renderer preserves the original body beneath transparent items; no current attachment polygon is treated as a true segmented limb. Every frame remains `anchorsMeasured:false` and `apparelQualified:false`. Source integrity, successful rendering and a clean original costume do not prove all outfit combinations fit.

Tiny silhouette/fringe contacts remain in the targeted review. Physical phones, player understanding, comfort, retention, rare-item value and repeated-run balance remain unmeasured. The [Mini LoL notices](../../THIRD_PARTY_NOTICES.md) identify the requested fan-art series and its reference provenance.
