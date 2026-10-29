# Journey map source checks

`JourneyMapScene` adds an original Canvas sky archipelago: ten palette/landmark identities, layered floating terrain, a completed gold route, active current-level marker, slow clouds and route lights. Reduced motion selects a stationary equivalent. The current captain appears in the map header and alongside the current ordinary node, with a direct squad/skill route. Guardian and social markers retain their real actions.

The existing `SagaMap.x`, `y`, `FIRST_Y`, `STEP`, `height`, culling and `scrollTo` contracts are unchanged. Journey remains 1–1000 and Remix 1–240. New sector captions clamp the last Remix sector at 240. The map now includes the same fixed five-destination navigation as other menu pages.

`scripts/test-saga-map-v17.sh` passed **177,034 pure geometry assertions**: every Journey/Remix selected-level jump across 13 viewports, 266.67-unit landscape included; bounded virtual culling; precise 100-level sector boundaries; dressed captain horizontal containment and clearance from its current level control; pinned header clipping; final page controls. New scene and copy classes also compiled against Android 36 with `javac -Xlint:all`.

These source checks establish geometry and compilation only. Actual Android rendering, text measurement, gesture delivery and screenshots require the integrated signed-candidate native phases under `native/`. No progress, economy, save format or engine logic is changed by these map classes.
