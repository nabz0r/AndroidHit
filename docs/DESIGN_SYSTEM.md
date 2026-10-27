# Luma Drift design system

**Shared direction for the Android game, public site and GitHub presentation · 1.6**

A night sky, a small companion and a readable puzzle form the identity. The presentation should help a player see an arrow, understand a gate, recognize their avatar and choose the next action. Product screenshots, explanatory artwork and test evidence each retain their own label.

## Color and contrast

| Reference token | Color | Intended use |
|---|---|---|
| Night indigo | `#12142e` | Page background and the surrounding night sky |
| Mint | `#99ecda` | Clear paths, positive controls and selected states |
| Peach | `#ffb194` | Main play/download action and a warm directional accent |
| Lilac | `#baa5ff` | Companion decoration, gates and secondary emphasis |
| Ivory | `#f7f5ff` | Main reading text and headings |
| Soft gold | `#ffe29a` | Earned coins, key badges and explicit rewards |

These are shared presentation references, not a claim that every game theme has identical pixels. The Android catalog is authoritative for its six themes: Lumina uses the closely related `#11142b` background, `#232744` cards, `#a99aff` and `#7ee8d1` accents. Other themes deliberately vary. The in-game key/reward gold can use `#ffd992`; the shared web/asset soft-gold reference is `#ffe29a`. Use the family coherently instead of recoloring installed captures to imitate a web token.

Keep reading text light on dark surfaces. Pastel arrows use dark ink. A state needs a label, outline, icon or shape as well as color: direction arrows, switches, K1/K2 keys, owned/equipped badges and power Active labels carry meaning. Responsive screenshots and focus checks do not certify contrast, physical readability or complete TalkBack usability; those remain explicit acceptance work.

## Shapes and composition

Use rounded cards, soft rims, restrained star particles and a small original companion. Leave quiet space around the board. Put the current puzzle and primary action ahead of secondary systems. In the lobby, the profile, avatar, next board and activities belong in one composition; on the site, explain the arrow/gate rule before listing all systems. The 1.6 saga map shows ten levels per page. A fresh board has a separate briefing with its objective, element, authored difficulty label and fixed action allowance. These are real game controls, not decorative website statistics.

Arrows remain clear at small sizes. Switch badges show their own directional change. Sealed cells have a lock and a matching K number; their key pieces carry the same badge. A promotional board that shows those mechanics must preserve their relationship. Original code-drawn SVG/Canvas artwork can explain a mechanic, but must not be described as an installed screenshot or a verified playable level unless it is one.

Companions and themes change appearance. Headwear, Aura and Charm have the separately stated limited effects. Equipment previews show ownership, slot, source and benefit; rarity is not an implied extra ability or a drop probability. The 1.6 inventory uses three full-width rows per page, category tabs and All/Owned/To unlock filters. Keep those states visible when choosing product captures. [Exact equipment contract](AVATAR_EQUIPMENT.md) · [Native 1.6 presentation](UX_1_6.md).

## Typography, controls and language

Use a strong, short display heading and familiar system sans-serif reading text. The Android game uses platform sans-serif families; the website can use system fonts without a remote font request. Preserve enough space for German, French, Portuguese, Spanish and Italian text instead of shrinking a critical price or source caption beyond comfortable reading.

A primary button should describe the action: play, download, confirm one charge or buy the stated pack. Price and quantity remain together. Local earned coins and account purchased gold are visibly distinct. Avoid dashboard-like decorative statistics, fictional ranks, fake review stars, invented users or revenue figures.

The game supports six languages. Public privacy/deletion pages and the website currently use French and English; do not imply that six-language Android support is six-language web/store localization. Localized marketing copy is a draft until reviewed by speakers and checked with its matching imagery.

## Motion and sound

Small companion movement, light particles and short transitions support the puzzle. Reduced-motion preferences must have a usable static presentation. Avoid motion that hides an arrow, masks a price or blocks a critical control. Web reduced-motion behavior and Android's saved setting are separate controls.

The game uses original synthesized effects and an eight-bar ambient loop. The website must not claim browser audio playback or autoplay the soundtrack merely because the Android build has music. [Audio source, measurements and Android playback limits](ART_AUDIO.md).

## One family, three surfaces

| Surface | Use | Evidence boundary |
|---|---|---|
| Android | Actual lobby, boards, collection, equipment and shop | Identify package/build and whether a temporary synthetic profile was used |
| Public website | Download route, explain the game and provide policy/support/deletion links | Distinguish local renders from the exact deployed revision |
| GitHub | Clear delivery links, short explanations, diagrams and original art | Link release hashes and test reports; repository publication is not Play approval |

Use real game captures for product behavior, original vectors for explanatory art and responsive page captures for website review. Keep those categories visible in alt text and captions. Do not stretch a phone capture to another aspect ratio or remove an important caveat from a store caption. A phone frame, surrounding glow or crop belongs to the web composition; it does not turn the composition into an Android screenshot. Keep the unedited source capture available and record any exported derivative separately.

## Reference artifacts

- [Installed 1.6 French lobby](screenshots/v1.6/ui/final-production/home-fr.png), [ten-level saga map](screenshots/v1.6/ui/final-production/map-fr.png), [prelevel briefing](screenshots/v1.6/ui/final-production/briefing-fr.png) and [companion inventory](screenshots/v1.6/ui/final-production/companions-en.png).
- [Final production capture report](validation/v1.6/ui-final-production/report.json) and [device context](validation/v1.6/ui-device-runtime.json). Home resumes a preserved practice challenge; it is not a fresh Journey session.
- [Historical 1.5 P6 share card](screenshots/v1.5/sharing/sealed-p6-card.png), with matching keys and walls. The six workshop challenges keep their original identities; 1.6 also teaches keys and seals within ordinary Journey and Remix.
- [Illustrated tour](VISUAL_GUIDE.md), [brand assets and provenance](store/README.md), [public-page implementation](PUBLIC_PAGES.md).
- [Executed 1.6 validation and exact-byte boundaries](VALIDATION_1_6.md). A polished image does not establish retention, provider activation or physical-device quality.

## Environment and website 1.6 scope

Android 1.6 keeps the direction/color geometry and six palette themes, with one bundled generated Aurora Atlas environment behind opaque or quietened controls. The exact prompt and image hash are in [art provenance](art/aurora-atlas-v1.json). This image is environment art; the board, route, text and controls are rendered by the game. Existing Nova vectors and the [1.5 brand kit](store/README.md) retain their original provenance and are not relabelled as new 1.6 screenshots.

The website 1.6 refresh is published through Firebase Hosting. Its [separate evidence](validation/site-v1.6/README.md) records source/asset provenance, executed local checks and the deployment outcome with their individual scopes. The [1.5 site qualification](validation/site-v1.5/README.md) remains the dated record for that revision; neither Android acceptance nor a new local page render updates it.
