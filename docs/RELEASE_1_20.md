# Luma Drift 1.20 — integration candidate

The next source version is production `1.20.0-rc1`, separate Lab `1.20.0-sandbox-rc1`, code 28. This document records ongoing integration; no final signed 1.20 APK hash, native acceptance, Hosting delivery or Play publication is claimed yet.

## Implemented menu scope

- A light home with the equipped companion, actual owned squad, clear Play action, real ready-gift badge and visible shop entry.
- Five fixed destinations: Home, Squad, Collection, Gifts and Boutique. The house and workshop remain secondary destinations.
- Persistent menu LumaCoins, account LumaGold and session-energy header. Unavailable Gold remains unavailable. Shared scroll/drawing/touch/accessibility coordinates reserve its space; the puzzle has a separate read-only strip.
- Actual daily/weekly objectives presented as gifts. A committed positive claim opens an announced reveal that waits for the player; explicit close fades out 180ms, or immediately with reduced motion. Claimed rows have no clickable claim action.
- An illustrated shop with a featured-item carousel, actual stock/prices/ownership and previews. Existing arrival pools/odds and durable purchases remain authoritative.

[Menu implementation and exact boundaries](UX_1_20_MENUS.md) · [72,282 host assertions over 504 configurations](validation/v1.20/menu/README.md).

The gameplay/result and session-energy changes have separate owners and evidence. Session energy must remain distinct from the shared charge used by companion skills. Any premium energy purchase must be server-authoritative, respect the test/production gate and reconcile an exact receipt. None of the menu work enables real checkout, awards Gold from a puzzle, or establishes an external provider transaction.

## Packaging and publication gates

The 1.20 packaging scripts require a new frozen candidate report, actual production/Lab identities and code 28, the retained signer, complete source hashes and exact APK bytes. They cannot collect old 1.19 packages as1.20. Large APKs remain ignored external artifacts with immutable metadata. The intended later transport reuses1.19 byte bases through exact deltas; resulting full hashes must match before deployment.

The website generator and assets are staged outside `public/`. They require six accepted exact1.20 original home/shop/victory captures, FR and EN, and the new immutable artifact commit. It preserves gallery/audio markup, legal/account/configuration files and older releases. No screenshot is synthesized or relabelled from1.19. [Staging and checks](validation/site-v1.20/README.md).

Public-origin browser access was refused during 1.19 qualification. The 1.20 tools do not fetch or render that origin through another mechanism. Local-site browser review and authenticated Hosting management are the allowed separate scopes. A later successful management deployment must not be described as public-render acceptance; public rendering remains unverified under this permission boundary.

Native1.20 qualification still needs to establish actual menu navigation and scrolling, wallet visibility and bounds, claimed-gift inactivity, successful/failed claims and reveals, shop previews, gameplay/results, session persistence and supported language/compact states on the final signed package. Physical-phone comfort, sound quality, human comprehension, voluntary return and retention remain distinct outcomes. Test counts do not establish a hit.
