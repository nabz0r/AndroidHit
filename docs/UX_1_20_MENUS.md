# A quieter home, visible wallets and real gifts — 1.20

The Android menu source now gives the player one clear next action: **Play**. A light background, the equipped companion and the actual owned team lead the home. Five persistent destinations are Home, Squad, Collection, Gifts and Boutique. The house and workshop remain explicit secondary home destinations. This changes the main menu presentation; it does not remove either activity or add new avatar ownership.

The header distinguishes a guest save on this device from an actually connected Google identity. The connection label does not promise cloud synchronization. Coins, account Gold and session energy stay in a separate fixed header while menu content scrolls. Unavailable Gold is a dash with an explicit accessible explanation, never a fabricated balance. Coins use a short visual count transition; accessibility exposes the durable amount immediately. Energy opens the root-owned session-energy page. This energy is distinct from the attempt-local charge used by companion talents.

## Layout and navigation

`Menu120Layout` supplies pure home, wallet, fixed-navigation and reveal geometry. The menu container reserves the wallet band before measuring its viewport. Drawing, hit targets and accessibility bounds share the same scroll-plus-inset transformation. Home panning/picking compensates that inset too. The five navigation targets use independent full-width lanes, preserving a physical 48dp minimum even at240dp width. Short windows scroll content instead of squeezing all destinations onto one screen.

The puzzle has a separate read-only42-unit wallet strip supplied to the gameplay owner; it does not inherit the scrolling menu container. The gameplay owner reserves its header space and places the strip outside board input. A preview overload can animate displayed Coins while the accessible value remains the real balance. There are no miniature purchase targets above the puzzle.

## Gifts and quests

Home's gift count comes from the actual `ObjectiveDashboard` ready set. It counts earned, unclaimed receipts; an incomplete quest does not receive a ready badge. The shortcut opens ready gifts, or today's objectives when none are ready. The result-screen owner can use the same helper and destination.

Daily login, daily/weekly quests, event completion, Journey milestones and power trials still call their existing durable claim APIs. Presentation does not grant anything by itself. A successful positive result opens a gift reveal, refreshes the objective snapshot and announces the real amount or item. A refused commit or zero/duplicate result cannot trigger a success reveal. Claim receipts remain authoritative and idempotent.

Claimed rows are non-interactive semantic text with a completed mark. They have no enabled claim button or old callback. Incomplete rows show actual progress and an appropriate play route. Ready rows expose an explicit **Open gift** action. The gift illustration is a native vector drawing, not a purchasable item or loot probability promise.

The reveal fades in, remains until a click or Back, then fades out over 180ms. There is no reading deadline. Reduced motion shows a stable frame and closes immediately on the explicit action. Landscape geometry keeps the reward text and continuation visible. The existing original reveal/coin sounds accompany successful claims and respect the saved audio settings.

## Boutique

The shop now uses an original illustrated storefront and one featured product at a time, with explicit previous/next controls. Powers show their actual current stock, unlock level, pack amount and dynamic LumaCoins price. Looks show the current companion wearing the selected garment as a preview, with owned/equipped state. Companion and signature-arrival entries lead to the existing pool/odds review, using their actual180/260 earned-coin prices. Browsing never performs a draw or purchase.

LumaGold stays a separate account wallet. Its page links to the existing wallet, premium wardrobe and the root-owned energy page; it does not invent a local Gold balance or activate live checkout. Premium energy purchase processing, provider gates and session accounting belong to the separate energy/backend integration. Puzzle victories and menu gifts here award LumaCoins, not LumaGold.

## Executed checks and open acceptance

The new host scope passes **72,282 assertions across504 physical layout configurations**:240–600dp widths,360–1200dp heights, six densities and normal/larger text. It checks independent target centres, physical touch floors, fixed-wallet/navigation clearance, scroll/touch/accessibility transforms, reveal timing and six-language formatting. Actual `GameStore`/`MetaProgress` calls cover real claims, duplicate zero results, current dashboard replacement, cold receipt restoration and both refused-commit models. [Host evidence](validation/v1.20/menu/README.md).

These are host layout/data contracts. They do not establish native pixels, every translated screen, actual audio output, frame times, owner comprehension or physical-phone comfort. Production Java compilation passed during integration, separately from final APK packaging. The exact release still requires signed-package identity, native gestures and actual home/shop/victory captures before the website may change. No 1.20 site deployment or commercial activation is claimed by this document.
