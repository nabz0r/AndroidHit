# Journey world 1.22 — presentation contract

This module supplies the new native adventure-map geometry, presentation travel,
camera and Canvas drawing. It owns **no progression, unlock, puzzle, energy,
currency, ownership or transaction**. Integration and exact-package native
acceptance are separate from the model checks recorded below.

## World and identity

`JourneyWorld122` keeps the actual catalogue IDs: Journey 1–1000 and Remix 1–240.
Each sector contains at most 100 consecutive IDs; Remix's third sector ends at
240. A sector is a 960×1680 world with ten serpentine rows. The row islands and
their connecting paths use the same node and cubic-edge coordinates as avatar
travel. A node is not a new puzzle, a substitute level or a purchased gate.

`node(level)` gives a point inside that sector. `sector(level)`,
`firstInSector(sector,total)` and `lastInSector(sector,total)` identify the actual
range. `hitLevel(x,y,sector,total,radius)` chooses the nearest real node in that
range or returns -1 for empty terrain. The controller remains responsible for
the actual unlock check, displayed metadata and physical screen-size targets.

## Avatar travel

Create `Travel.start(from,to,total,uptime,reducedMotion)`. Sample it with the same
uptime used for the rest of the scene. `TravelFrame` exposes position,
directional velocity, fractional route position, destination, last node reached,
moving state and whether travel was abbreviated. A short local journey follows
successive actual path edges, including turns. It lasts at most 1500 ms.

`retarget(target,uptime,reducedMotion)` creates a new trip beginning at the
current fractional position, so reversing or choosing another nearby node does
not teleport the avatar. Regressing clock samples cannot reverse its progress.
Cross-sector selections and trips of more than twelve edges arrive immediately
and report `abbreviated=true`; no imaginary path is drawn between sectors and no
long trip becomes a mandatory wait. Reduced motion also arrives immediately.

Travel is presentation only. Its `levelReached` is **not** the player's highest
unlocked or completed level. Opening the level briefing or pressing Play must not
wait for travel. Pausing/recreating may settle the presentation at its selected
destination; it must not replay a purchase, energy debit or victory.

## Independent camera and minimap

`JourneyCamera122.configure(viewWidth,viewHeight)` uses logical viewport units.
The camera clamps the visible world at every zoom. `panBy(dx,dy)` takes finger
displacement in viewport units. Pan, explicit `centerOn` and zoom interrupt
follow. Later calls to `follow(avatarX,avatarY)` respect that interruption;
`resumeFollow` is the explicit recenter command. Dragging the map or inspecting a
place in the minimap therefore does not move the avatar.

`setZoomAt(zoom,screenX,screenY)` retains the world point under the gesture
anchor, except when a world boundary requires clamping. The minimum zoom fits
the viewport within the world; maximum zoom is 1.65, or that minimum if a very
large viewport requires more. `Frame` supplies `bounds`, reversible
`worldToScreen`/`screenToWorld`, zoom and follow state.

The controller clips its map rectangle, translates to the rectangle's origin,
scales by `frame.zoom`, then translates by `-frame.left,-frame.top`. Hit
coordinates must subtract that rectangle's origin before `screenToWorld`.
Fixed wallet, navigation, minimap and command controls stay outside this world
transform and retain gesture ownership.

`worldToMini`, `miniPoint`, `miniToWorld` and `miniViewport` share one rectangular
projection. A minimap click changes camera framing, not avatar destination or
progress. The overlay shows the current sector's route, actual unlocked extent,
avatar marker when in this sector, and the camera rectangle. A compact layout
must leave a reachable close/recenter route and reserve overlay hit regions
before world-node hits. The renderer does not invent interactive regions.

## Renderer and existing art

`JourneyWorldScene122.drawWorld` draws terrain and exact paths in world
coordinates, with viewport culling. `drawNode` renders actual node state.
`drawMinimap` draws the fixed-screen overlay. The renderer reuses the ten
`JourneyMapScene` palettes and adds no bitmap payload. Avatar art remains the
controller's existing authored companion renderer; walking orientation can use
`CompanionMotion.facingForVelocity`. Decorative route lights and clouds stop in
reduced motion. Terrain, path, state and minimap information stay present.

World and minimap share private cached paths and node points, built once for
each actual sector/catalogue pair and never mutated after construction. The
minimap transforms those paths instead of rebuilding 99 paths and their sampled
points every frame. Its node markers and camera rectangle use primitive
coordinates, avoiding per-frame minimap projection objects. This bounds repeated
geometry work; it is not a measured frame-rate claim.

The old `SagaMap` and `JourneyMapScene` APIs remain unchanged. This new module
does not claim physical-device comfort, Android touch delivery, frame rate,
accessibility comprehension or a validated increase in enjoyment.

## Executed host verification

Run `sh scripts/test-journey-world-v122.sh`. On 30 October 2026 it passed
**171,865 assertions** under Java 17, followed by Android 36 Canvas compilation.
The tests cover every supported catalogue ID, partial sectors, exact bidirectional
edges and bounds, camera/minimap inverse projection, path-preserving forward and
reverse trips, mid-trip redirection, regressing samples, maximum duration,
abbreviated sector/long trips and immediate reduced-motion arrival. Seven
viewport sizes from 240×140 to 2000×2000 execute 3,500 deterministic mixed camera
operations and verify bounds, zoom anchors and explicit follow interruption.
These are actual pure-model executions and renderer compilation, not a native
APK recording or a human playtest.
