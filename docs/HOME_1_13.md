# Your little home — 1.13 contract

The puzzle is the main game. The house gives existing earned LumaCoins a visible, personal use between boards: a room, the companion already equipped and a furnishing the player chooses to work towards. It is one room, not a village simulator or a cloud social space.

## Room and interactions

The room is drawn from three quarters over a **6×6 integer grid**. Bed and sofa have a 2×1 footprint, rotated to 1×2. The rug covers 2×2, remains traversable and can underlay solid furniture; other objects occupy one tile. There is one owned instance of each catalogue object.

Tap a free floor tile to call the companion. Movement follows a four-neighbour shortest path around solid furniture; blocked or unreachable destinations are refused. The valid destination is saved before the path animates. Confirmed position persists, while the walking animation is temporary.

The cuddle action produces a short visible reaction. Furniture offers named activities such as rest, sit, light, read or play. A rest pose may visually settle the companion onto a furnishing; the durable floor position remains valid outside solid footprints. Cuddle, light and rest/activity poses do not create needs, neglect penalties, progression rewards or persistent relationship points, and return to their default after recreation/relaunch. The room draws the actual equipped character, free visual style, four clothing slots and three existing equipment slots; unowned previews grant nothing.

## Furnishings and earned prices

| ID | Furnishing | Footprint | LumaCoins | Starter | Interaction |
| --- | --- | --- | ---: | --- | --- |
| `bed` | Moonfold bed | 2×1 | 0 | Yes | Rest |
| `sofa` | Cloud nook sofa | 2×1 | 240 | No | Sit |
| `lamp` | Warmglow lamp | 1×1 | 0 | Yes | Light |
| `plant` | Little leaf plant | 1×1 | 0 | Yes | Admire |
| `rug` | Soft orbit rug | 2×2, traversable | 0 | Yes | Relax |
| `table` | Teatime table | 1×1 | 140 | No | Tea |
| `bookshelf` | Story nook shelf | 1×1 | 220 | No | Read |
| `cushion` | Suncrumb cushion | 1×1 | 0 | Yes | Sit |
| `plush` | Pocket star plush | 1×1 | 120 | No | Cuddle |
| `gate_lamp` | Clicklight lantern | 1×1 | 180 | No | Switch |
| `mobile` | Drifting arrows mobile | 1×1 | 160 | No | Watch |
| `stool` | Pebble stool | 1×1 | 100 | No | Sit |

Catalogue labels and descriptions are provided in English, French, German, Spanish, Portuguese and Italian. Rendering may group related named furniture interactions into a shared pose; labels are not twelve independent minigames.

| Surface | Free choices | Additional earned choices |
| --- | --- | --- |
| Walls | Vanilla (`wallpaper_cream`), Sage (`wallpaper_sage`) | Dusk (`wallpaper_dusk`) 180; Peach (`wallpaper_peach`) 160 |
| Floor | Honey oak (`floor_oak`), Cloud (`floor_cloud`) | Rose (`floor_rose`) 180; Mint (`floor_mint`) 160 |

The existing new-profile balance remains **180 LumaCoins**, with the existing power inventory. No additional starter coins are granted. These furnishings and surfaces use existing earned coins and never LumaGold or a paid random draw.

## Looking, buying, placing and storing

1. Browse an item to see its name, description, price and actual ownership.
2. Optionally pin one unowned furnishing as a wish. Its progress is the current LumaCoins balance capped at the item price. Surface finishes are outside this furniture wish.
3. Confirm an affordable purchase. Debit and ownership are one transaction; the purchase clears that item’s wish and leaves it unplaced.
4. Choose a tile and rotation. A draft preview does not change the saved room or spend coins. The entire solid footprint must fit and remain free, including the companion cell.
5. Confirm placement, or cancel. Moving a placed object replaces its previous placement. Store away removes its placement while keeping ownership; re-placement is free.

Wall/floor ownership and equipping are separate. Free choices are always owned; paid choices must be acquired before equipping. One room layout and one selected wall/floor pair are saved per local profile.

## Starter and existing saves

When home keys are absent, reading the room returns this arrangement without a migration write: bed `(0,0)`, lamp `(5,0)`, plant `(0,5)`, rug `(2,2)`, cushion `(4,4)`, companion `(3,3)`. Vanilla walls and honey oak floor are selected. Existing coins, equipment, wardrobe, progression and an active puzzle are preserved.

Home preferences are stored inside `luma_drift_save_v1`: `home_layout_v113`, `home_owned_v113_<id>`, `home_wallpaper_v113`, `home_floor_v113` and `home_goal_v113`. The versioned layout is one immutable record containing placements/rotations and companion coordinates. Unknown, malformed or unsafe layouts fall back to the starter; unowned paid placements are filtered. No layout read grants ownership.

All home reads flush pending tracked writes. Mutations share `LocalSaveTransactions.LOCK` and `write`; a failed commit remains an exact pending transaction until Retry succeeds. A memory-before-disk refusal must not be reported as a completed action. A distinct cold process sees the preceding complete disk state if it starts before the pending write was durably retried. The source tests distinguish this from a successful retry followed by cold restoration.

## First little puzzle

`FirstLight` supplies a real three-piece board with one switch. Fresh eligible profiles can exit pieces, skip or finish. A unique action history of IDs 0–2 and a seen marker live in `first_light_history_v113` and `first_light_seen_v113`; finishing atomically clears history and records the marker. The opening grants no coins, powers, stars or Journey unlock. Existing tutorial state or campaign progression avoids forced replay.

The full seven optional lessons remain separate, with their existing once-only 100-LumaCoin/two-Spark reward only when those lessons are completed. Skipping or finishing the three-piece opening does not claim it.

## Privacy and evidence

The house and opening add device-local game state. When the existing optional journal is enabled, it can include home actions; it remains off by default, bounded and exported only through the player’s explicit Android share flow. There is no new analytics SDK, background room upload, demographic profile or gameplay cloud save. [Data inventory](PRIVACY.md).

[891 model/durability checks](validation/v1.13/home-model.json) cover catalogue/languages, immutable placement, collisions, safe paths, wishes, real earned purchases, refusals, multi-handle retries, cold restoration and old saves. The actual opening board has its own test. Original native frames and final installed reports are indexed in [qualification](validation/v1.13/README.md); source assertions alone do not validate touch, animation, human appeal or physical-phone comfort.
