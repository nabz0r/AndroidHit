# Initial playtest measurement fixture failure

This preserved automated run genuinely clears Journey levels 1–5 on the unchanged RC2 Lab APK, then stops before level 6 actions because the newly added measurement helper counts only labels beginning with `Piece`. The visible key piece is actually named `Key 1, Piece, row…`, so the helper incorrectly reports eight visible pieces instead of nine. The actual native screenshot and node dump retain all nine. This is a QA measurement defect, not an observed app failure, and the global run is not passed.

Original preferences and rotation are restored and the isolated hometest package is removed. The corrected retry uses a new label. Exact pre-run source, native failure and original frames remain preserved.
