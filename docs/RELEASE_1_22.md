# Luma Drift 1.22 — living map and tactical choices

Production `1.22.0-rc1` / Lab `1.22.0-sandbox-rc1`, Android code **30**.
Qualification and publication status are recorded in the
[delivery register](validation/v1.22/README.md). A successful build alone is not
release acceptance.

## Native changes

The Journey/Remix map uses an independent two-dimensional camera and a minimap.
Selecting an accessible node moves the actual equipped captain along the route;
short trips follow the path and direction. Pan, pinch and minimap inspection
change the view. “Follow me” restores tracking. Exploring never grants a level,
spends energy or starts a puzzle. Play stays explicit and available while the
presentation moves. Long or cross-sector trips settle without a forced wait.
Reduced motion retains the destination and state. Extremely short windows use a
scrollable level selector; locked Remix keeps its progression explanation.

Keys and their locks share a larger silhouette, color and number. Inspecting a
lock highlights its real key association. A real key exit sends a short visual
signal and opens its linked locks; Undo restores their actual state. A blocked
move displays the engine's immediate cause for 3.2 seconds. Holding a revealed
piece opens explicit consequence inspection, including gate changes and newly
available exits. Holding a covered piece explains its free preparation instead.
The campaign's ordinary taps and rules remain unchanged. U43/U48 display the
inspection affordance, alongside its earlier introduction at U29/U31.

The Echo Observatory is a separate **T122** chapter with twelve new boards and
two episodes. Each run crosses four boards, including two route choices. Nova,
Hoot and Orbit are loaned, with six shared Charge and one use of each talent for
the entire run: information (2), one-exit rewind (2), or up to two assisted exits
(3). Using two roles enables one explicit Resonance. Retries and stage changes
retain spent Charge. Every board remains manually solvable.

The chapter keeps a separate durable session. It consumes no campaign energy,
paid power or elemental shell. Its Undo has no Coin cost and does not refund the
productive action already spent. Completing an episode for the first time grants
120 earned Coins and its local souvenir marker. Replays do not repeat that
milestone; the loan does not grant ownership or copies. Finished settlement can
resume after a cold process. Corrupted chapter state has an explicit confirmed
restart path that preserves campaign and wallet.

## Evidence and interpretation

The design study combines the historical U43/U48 concentration, the versioned
synthetic lab and identified primary research. The
[1.22 lab](validation/cognitive-lab-v122/README.md) runs 21,000 campaign
trajectories, 84,000 probes without assistance and 54,000 chapter runs. Immediate
causal feedback gives a modest modeled improvement; execution talents increase
assisted progression while some unaided probe results decline. Both outcomes are
published. The native key inspection and visual map are outside the simulation.

[Current host and Android evidence](validation/v1.22/README.md) distinguish engine
contracts, durable preference doubles, installed package identity, real emulator
gestures and original screenshots. The [lab source binding](validation/cognitive-lab-v122/candidate-c-source-binding.json)
retains the exact generation sources and identifies later uncompiled UI changes.

The website preparation is restricted to the exact tested Android bytes and original FR/EN captures,
with the archive, gallery, account/privacy routes and deliberate audio controls
preserved. Its schematic browser demo has fictional levels and locks; it is not
the Android game or player progress.

No human participation, retention lift, millions-of-players forecast, physical
phone frame-rate measurement or Play approval follows from these checks. Real
checkout and production provider acceptance keep their prior gates.
