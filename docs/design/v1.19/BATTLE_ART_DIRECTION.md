# Battle companions: visual contract for 1.19

This addition makes the active team visible beside the puzzle through an open portrait dock and six short skill effects. It uses the existing original companion renderer through a callback; it does not replace its identity, evolution, clothing, equipment or sprite registry. The host must provide the actual equipped look and complete accessibility label.

## Native presentation

- Three equal portrait columns, without boxed character cards, decorative frames or modal interruptions. A quiet tinted oval provides separation from the scene. Selected portraits have a thin coloured ring; available portraits have a slow subpixel breathing accent. The portrait callback receives the authored happy pose when selected and the ready stance otherwise.
- Each occupied column shows the character name, skill signature, a numeric energy cost, and supplied availability text. Used state is written rather than encoded only by colour. An empty ID shows an empty medallion and the supplied “free slot” copy, without a portrait or cost. Ownership and availability remain the host's decision.
- One lower action combines the shared energy count, six stable pips, combo name, and its actual supplied state. Pips compare current energy with the maximum; they are not a timer or a chance display. Ready state uses a violet tint and a small arrow, without urgency, flashing or simulated readiness.
- Six board-clipped, 760 ms effects follow the contract's family order: guide rays, vision eye, trail ribbon, pulse rays, flow ribbon and rewind arc. Target coordinates come from the host's real board result. The renderer never invents a move, hints at an invalid move, removes a piece, changes energy, or awards anything.
- Reduced motion removes breathing and particle travel. The same short skill interval shows a steady low-opacity mark. There is no camera shake, white flash, artificial near miss, or input lock in this component.

## Geometry and accessibility

`CompanionBattleLayout` is the single geometry source for the scene and returned hit rectangles. A normal dock is 144 logical units high. Three targets occupy the upper 90 units; a six-unit gutter separates the lower 48-unit combo target. The targets use half-open edges and never overlap. The parent owns hit routing and must reject unavailable, unowned, spent or stale actions.

The overload accepting `minimumTouchLogical` and `minimumText` preserves physical sizing under the host's canvas transform. Pass `48 * density / canvasScale` for the touch floor and the existing experience text floor. Required height is `CompanionBattleLayout.heightFor(minimumTouchLogical)`, equivalent to `max(144, 96 + max(48, minimumTouchLogical))`. The parent reserves that full space and scrolls the complete puzzle on short screens; it must not shrink the board or scale this dock down to fit.

Text is measured with the real Android `Paint`. Long labels are ellipsized at their requested size. Above a 13-unit text floor, the visual name line yields to signature and state; the complete name, effect, cost and state remain in the host's semantic label. At very large fonts the decorative portrait becomes smaller to retain readable action text. Above a 15-unit text floor the energy label yields to its current/maximum number while its full semantic label remains with the host. These choices require native glyph and TalkBack review, particularly for long translations and font scale 1.3.

## API

```java
CompanionBattleScene.Slot(
    String id, String name, String signature, String family,
    int color, int energyCost, boolean ready, boolean used,
    boolean selected, String statusLabel);

CompanionBattleScene.Snapshot(
    Slot[] slots, int energy, int maxEnergy, String energyLabel,
    String comboLabel, String comboStatus, boolean comboReady, boolean comboSpent);

CompanionBattleScene.Layout draw(
    Canvas canvas, RectF bounds, Snapshot snapshot, long elapsed,
    boolean reducedMotion, CompanionBattleScene.Portrait portrait,
    float minimumTouchLogical, float minimumText);

boolean drawSkillEffect(
    Canvas canvas, RectF board, int color, long elapsedSinceSkill,
    boolean reducedMotion, int effectOrdinal, float targetX, float targetY);
```

`Layout.actions[3]`, `Layout.combo` and `Layout.slotAt(x, y)` expose the same geometry used by the draw pass. The six-argument `draw` and one-argument `Layout` constructor remain available with 48/12 logical defaults. Snapshot text is supplied already localized; no translation or business rules are embedded in the renderer.

## Component verification

`bash scripts/test-companion-battle-v119-geometry.sh` passes **14,859 spatial checks across 412 layouts**. It checks containment, non-overlap, half-open hit routing, invalid bounds, and caller touch floors from 48 through 78 logical units. Independent arithmetic checks that those enlarged targets preserve a 48 dp minimum under the caller's transform. It does not emulate gameplay or claim native visual acceptance.

The scene was also compiled in an isolated temporary output directory with the actual Android 36 SDK jar, `Catalog`, and `CompanionMotion`. This verifies API and type compatibility without touching the shared Gradle outputs. Final integration, actual companion art/equipment, six translated states, actual font metrics at larger fonts, and the host's scene offsets still require the release's native screenshot and interaction checks.
