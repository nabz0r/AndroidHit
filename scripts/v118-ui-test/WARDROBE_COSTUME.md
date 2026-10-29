# Wardrobe presentation qualification

Run these three phases in order against the same frozen H candidate and SDK observer, after `motion-outfit-preview` and `motion-outfit-cold`. Each phase uses the existing runner, which force-stops the game before launch. No new device is needed. Scenario preparation and a successful observer build are not native execution evidence.

1. `wardrobe-costume-choice` opens the home and wardrobe through visible native UI. It captures the implicit original costume, chooses the existing outfit with `wardrobe_costume_toggle`, checks its real Activity recreation, returns to the original, then leaves the outfit selected for the next process.
2. `wardrobe-costume-cold` checks the retained outfit in a distinct process, returns to the original costume, and checks a second real Activity recreation.
3. `wardrobe-costume-original-cold` checks the retained original costume in another distinct process. The profile finishes with the original costume selected.

The retained task profile must initially have neither `companion_original_costume_v118` nor any explicit `cosmetic_slot_v17_*` key, so the first observation can prove the implicit default. An old explicit cosmetic selection is a different migration case. A failed attempt remains evidence; resetting preferences to make this precondition pass is not part of this scenario.

All input is actual native navigation, scrolling and the named wardrobe toggle. The phases do not equip an item, buy anything, grant currency or copies, clear a puzzle, call a solver, or invoke a target method. The SDK only reads preferences. The costume label regexes cover the six existing translations; the home navigation helper follows the established English/French task profile.

After all three actual runs pass, compare their **host evidence directories**, each containing `report.json` and `native/`:

```sh
python3 scripts/v118-ui-test/compare-costume-choice.py \
  --choice /absolute/path/to/wardrobe-choice-evidence \
  --worn-cold /absolute/path/to/wardrobe-worn-cold-evidence \
  --original-cold /absolute/path/to/wardrobe-original-cold-evidence \
  --output /absolute/path/to/new-wardrobe-comparison.json
```

The comparison requires three fresh passing phases with distinct processes/tokens, identical candidate/observer bytes, the expected four actual toggle touches and two actual Activity recreations, and exact state continuity before each cold launch. It compares every field in the four observed preference files at fifteen checkpoints. Only `luma_drift_save_v1.companion_original_costume_v118` may differ. Coins, item ownership, selected cosmetics, gear, equipped companion, copies and both puzzle sessions remain strict. Boolean types and absent keys are distinguished. Seed-named keys are outside the existing SDK snapshot scope and are not claimed as compared.

The captured original/outfit portraits still need visual review. This scenario proves the choice, persistence and observed state preservation; it does not qualify clothing fit across all characters or phones.
