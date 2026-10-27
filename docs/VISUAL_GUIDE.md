# Luma Drift 1.5 — illustrated tour

These are installed Android captures of the identified 1.5 candidate, not mockups. They establish displayed behavior; physical-device and player acceptance remain separate. [Exact binary and tests](VALIDATION.md) · [Historical 1.1 tour](VISUAL_GUIDE_1_1.md).

The French lobby, profile, collection and shop captures below come from the final production APK (`33c4dbd6…`). The wall-before/open gameplay pair comes from the preceding full functional pass; only the exported-image renderer changed afterward. [Source comparison](validation/v1.5/binary-source-diff.json).

## Start from your avatar

<p><img src="screenshots/v1.5/home-fr.png" width="270" alt="French integrated lobby"><img src="screenshots/v1.5/profile-guest-fr.png" width="270" alt="Guest account view"></p>

The profile card clearly identifies a guest or actual Google session. The central avatar shows equipped gear beside the next board. Play resumes an interrupted attempt; the four activity cards lead to events, missions, collection and shop. Google identity does not imply cloud-save support.

## Own, equip or unlock

<p><img src="screenshots/v1.5/collection-equipment-fr.png" width="270" alt="Equipment inventory"><img src="screenshots/v1.5/equipment-preview-fr.png" width="270" alt="Exact equipment effect and unlock condition"></p>

Three tabs separate companions, themes and functional equipment. The hero displays the current loadout. Status badges and an owned filter answer what is already available; a preview states the slot, rarity, effect and exact source. Acquisition through weekly/event milestones is permanent. [All 18 items](AVATAR_EQUIPMENT.md).

## Understand the price before spending

<p><img src="screenshots/v1.5/shop-fr.png" width="270" alt="Earned-coin power shop"><img src="screenshots/v1.5/power-offer-fr.png" width="270" alt="Pack confirmation with earned-coin price"></p>

Stock and pack quantities are explicit. Opening/cancelling an offer spends nothing. Gameplay power activation separately confirms one charge, then shows the active effect; a running effect cannot consume another charge of the same power. Purchased-gold/account offers live in their separate, currently gated commerce screen.

## Release a key, open a passage

<p><img src="screenshots/v1.5/sealed-wall-before.png" width="270" alt="Numbered key and fixed wall"><img src="screenshots/v1.5/sealed-wall-open.png" width="270" alt="Open passage after the key exits"></p>

Matching key pieces dissolve fixed walls. Undo brings them back. Later prototypes mix this rule with gates, so the order of a key's exit can matter. The six P challenges are unlimited, reward-free practice. Journey instead uses 1,000 new fixed layouts and per-board action limits; 240 Remix provide paired gate-switch puzzles.

Open **Events → Sealed passages** to play P1–P6. The exported challenge card also includes matching walls and key badges:

<p><img src="screenshots/v1.5/sharing/sealed-p6-card.png" width="420" alt="Actual final-APK P6 share card with K1/K2 keys and matching sealed walls"></p>

This PNG was generated on the final APK. The Android chooser was cancelled; no message was sent.

## Evidence and limits

Six-language and small-window UI reports identify exact package hashes, inspected states and original-setting restoration. Lab equipment/power screenshots may use the explicitly disclosed temporary test profile; those coins, ownership and unlocks are not presented as earned progression. Real provider login, live ads, purchases, physical touch/audio quality and retention remain separate qualification.

## Website and GitHub presentation

The 1.5 homepage uses the same night/pastel visual family, original companion vectors and clearly sourced game captures. [Design system](DESIGN_SYSTEM.md) explains palette, layout, language and image provenance. The public site is French/English; six-language support describes the Android game.

<p><img src="screenshots/site-v1.5/home-en-1366-hero.png" width="690" alt="Local desktop rendering of the English 1.5 homepage"><img src="screenshots/site-v1.5/home-fr-390-hero.png" width="240" alt="Local mobile hero rendering of the French 1.5 homepage"></p>

These are **local browser renders**, including original Nova vector art and the sourced game view; they are not screenshots of a confirmed deployed revision. [FR/EN responsive checks](validation/site-v1.5/browser-check.txt) passed on 18 local states, with synthetic-only deletion tests and no real sign-in.

Website captures belong in [`screenshots/site-v1.5/`](screenshots/site-v1.5/) and their checks/manifests in [`validation/site-v1.5/`](validation/site-v1.5/). Local webpage captures demonstrate the rendered source revision, not a deployed page or new Android behavior. The subsequent [Hosting delivery record](validation/site-v1.5/delivery.json) identifies the published revision and confirms the served payloads match these rendered sources. Existing Android images above keep their original final/predecessor labels. [Web evidence boundary](PUBLIC_PAGES.md#15-visual-refresh-and-new-evidence).
