# 1.11 — pre-Play bug hunt

28 October 2026. Owner-reported defects: character choices appear unchanged; equipped items are not visible. Play publication is paused by scope while these paths are investigated. The investigation began before any1.11 build was qualified; final scoped results are recorded below.

## Acceptance

1. Reproduce on exact 1.10 RC3 installed bytes and retain observed results, including failures.
2. Separate character identity, free treatment, theme, cosmetic clothing and functional gear.
3. Verify purchase with earned coins, equipment, immediate visual result, Home/wardrobe/game consistency and a real cold launch. Preview/cancel must not grant or spend; a repeated action must not double debit.
4. Inspect actual native pixels in bounded portrait/scene regions, with reduced motion and matching state. Accessibility labels and stored IDs alone do not qualify visual changes.
5. Broaden the hunt to navigation, dialog sizing, save failure/retry, tutorial and gameplay recovery. Preserve fixed puzzle identities, existing progress and economy.
6. Produce signed APK/AAB candidates, package verification, installed Lab and production acceptance, exact screenshots and scoped docs. No real ads, wallet checkout, provider operation or Play upload during this hunt.

## Work split

- Root: LumaView integration, visible theme application, error feedback and release packaging.
- Renderer audit: CompanionPortrait, AvatarStage and clothing/gear layers.
- Store audit: ownership, durable equipment and cold reads.
- Native QA: an independent1.11 SDK harness, actual before/after pixels and separate cold-launch stages. The original1.10 harness/evidence remains unchanged.

The first confirmed test gap is that1.10 avatar acceptance never bought/equipped a clothing item and style acceptance compared IDs/labels without portrait pixels. The1.10 native selection-colour checks covered controls, not the companion. This explains how the report could pass while these visible outcomes remained untested. Source hypotheses are recorded separately until reproduced.

## Corrections under qualification

- Shared portrait composition releases the face, ears and body silhouette from oversized clothing. Head/Aura/Charm are separated and enlarged. Catalog IDs, prices and effects are unchanged.
- Successful character/clothing/gear equipment opens the wardrobe at its portrait; the actual names appear under the four clothing slots and three gear slots. The companion picker is reachable directly. A purchase does not silently equip clothing: its owned offer now opens with an explicit Equip action.
- Character/theme preview callbacks check whether equipment actually succeeded before reporting success. Capsule equipment uses the same local-save error boundary. A read-only accessible portrait describes the actual seven worn slots without advertising a click action.
- The chosen theme now contributes its hue to the garden and animated play scene; previously the garden ignored the provided theme background, while the style backdrop covered it during play.
- Seven character/theme/gear readers settle exact pending save transactions before constructing visible inventory state. First-profile initialization now retains an exact pending grant after refused disk writes; 41 controlled initialization assertions pass.

Host raster comparisons are evidence about drawing differences, not human recognition. The independent Android test compares actual art crops, earns its coin budget by clearing levels, and crosses a real force-stop/new-process boundary. Baseline failures and fixture failures remain separate.

## Native follow-up and RC2

RC1 fresh-install follow-up reproduced a new compact-label defect: the initial English gloves name already truncated to `Gloves · D…`. RC2 removes the repeated slot prefix from the small worn-name caption and uses two bounded lines when the variant contains a longer phrase. Complete translated names remain in accessibility, inventory and offers. Clothing cards/hits gain two logical units of height (90 instead of88), keeping their existing separation from the central portrait. RC1 evidence and signed bytes are retained as intermediate qualification; they are not relabelled as RC2 acceptance.

Baseline C on the published1.10 RC3 completed40 levels and crossed a real cold process boundary. It confirms two actual scene defects: Home/play gutter comparisons each change0 pixels when the theme changes. Clothing, earned gear, Miko and free treatment each already change actual portrait pixels in that route; the visibility/feedback improvement does not falsely claim a missing normal-save selection or a nonexistent total renderer cache.

## Completed RC2 qualification

Frozen production1.11.0-rc2 and Lab1.11.0-sandbox-rc2, code12, pass signature/bundle/16KiB checks. Installed Lab:1,589 assertions/50 clears/115original frames +19crops; installed production:935/40/41originals +19crops. Both cross real cold process boundaries and restore original guest files/rotation. FreshK passes83tutorial/45avatar/36language checks and directly observes the initial persistent starter grant. First production startup/audio passes7. The original clothing/character choices are durable in normal baseline play; the native-reproduced scene defects and compact-caption defect are corrected. Controlled pending-save/initialization failures remain model evidence, distinct from native disk-fault acceptance.

Publication is a separate final step after exact source/artifact/report consistency checks. No Firebase Hosting, Cloud Run or Play release is changed by this hunt.
