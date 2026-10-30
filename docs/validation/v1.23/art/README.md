# ArrowScene123 — native presentation contract

The new [`ArrowScene123`](../../../../app/src/main/java/com/androidhit/lumadrift/ui/ArrowScene123.java) draws the four existing direction colours as small glazed toy pieces. A large sculpted pointer keeps the centre. A numbered key tag occupies the left edge; a relay rail occupies the bottom edge. A piece with both roles, including U29's key/relay, uses both reservations without sharing the former upper-right badge position. A local highlight is supplied by the caller; the helper has no idle animation, model, action, persistence, reward or inference.

```java
private final ArrowScene123 arrow123 = new ArrowScene123();

// size is the visible piece size, not the entire grid cell.
arrow123.tile(canvas, x, y, size, tile.direction, tile.switchMask,
              keyNumber, selected);
// keyNumber == 0 means no key. Calculate a positive relation number from
// the actual level only when that Tile belongs to that Board.
```

The renderer owns **both tile badges**. Do not also draw `KeyLinkScene122.key()` over a tile already carrying the new key tag. Keep that legacy method for unrelated old/tutorial callers. Root integration covers campaign, practice, tactical pieces and travelling pieces with their actual board identity.

`KeyLinkScene122.barrier()` delegates only paint to:

```java
arrow123.lock(canvas, x, y, size, keyNumber,
              openingProgress, opacity, highlighted);
```

Its existing actual key-state tracking, inspection, 700 ms opening/fade and disappearance remain in `KeyLinkScene122`. The renderer does not start an opening. The same relation number/colour identifies the key and its lock; a shackle and gold lock body distinguish a barrier from a movement piece. Reduced motion continues to use the scene's existing immediate settled state.

Before candidate C, the single-digit relation number was increased from 18 to 24 normalized units, with baseline 13.5. At a 45-unit tile this is a 10.8-unit font. The capsule footprint stays unchanged. This is the only art change after B's frozen source; B captures remain B evidence.

[`test-arrow-scene-v123.sh`](../../../../scripts/test-arrow-scene-v123.sh) compiles the real helper against Android API 36 and passes **800 host assertions** for badge reservations at sizes 24–88, translated positions, unchanged direction-colour mapping and invalid dimensions. The direction reservation includes all four rotations and its pale rim. These checks qualify the declared geometric contract, not Android pixel rasterization, tactile comfort or human symbol recognition.

Candidate B's [native discovery](../native/candidate-b-clarity-discovery/report.json) records five original 1080×2400 PNGs after real retained U42 resume, the contextual talent sheet and wardrobe trial/cancel/return. They were inspected with `view_image`: the new main arrows are legible and material has relief. The old attempt still has its saved water/ice layers; these captures do not prove a new shell-free attempt. U29 key+relay and the larger C number require their own later native captures. No host approximation is relabelled as a native image.

Art source SHA-256 for candidate C input:

- `ArrowScene123.java`: `861615664991bf197e0ee00a2f60ad0b6f7d6dcd2e877c9dc2387b4655a82717`.
- `KeyLinkScene122.java`: `97d3a9804ccc3397b02f84b3398c63c212d6efc3f8d1e54838df5eeacbc4b52e`.

## Rendu final E inspecté

Les8 [PNG natifs E rc2](../native/candidate-e-canonical-media.json) ont été
inspectés : flèche centrale contrastée, clé24 et relais séparés sur U29,
serrure distincte. Le plateau crème et les points viennent de l'intégration
LumaView ; le helper conserve ses empreintes figées et sa géométrie testée.
La [qualification native](../native/README.md) couvre les gestes réels,
modalités de reprise et limites. Aucun rendu hôte approximatif n'est présenté
comme screenshot Android.
