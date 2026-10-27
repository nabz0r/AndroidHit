# Luma Drift documentation

**A little spark. A great escape.** Read the rules, inspect the delivered game, and keep test evidence separate from the next proposal.

Current delivery: **[1.5.0-rc1 production APK](../releases/luma-drift-1.5.0-rc1.apk)** · [Play AAB](../releases/luma-drift-1.5.0-rc1.aab) · [Install, signatures and exact hashes](RELEASE.md). Production and the isolated Lab use version code **6**. This is a playable quality candidate with **sample ads forced and checkout blocked**, not a commercially activated Play release.

The game contains **1,000 Journey boards, 240 Remix boards, six Sealed passages prototypes, 24 companions, six themes, 18 equipment items in three slots and six powers**. Its six languages are French, English, German, Spanish, Portuguese and Italian. Progress and equipment stay on the device; optional Google identity does not provide cloud saves.

<p><img src="screenshots/v1.5/home-fr.png" width="225" alt="Actual French game lobby"><img src="screenshots/v1.5/collection-equipment-fr.png" width="225" alt="Actual equipment collection"><img src="screenshots/v1.5/sharing/sealed-p6-card.png" width="225" alt="Actual exported sealed puzzle with matching keys and walls"></p>

## Play and understand

| Guide | What it answers |
|---|---|
| [Release and installation](RELEASE.md) | Which APK to install, channel separation, signing and checksums |
| [1.5 changes](RELEASE_1_5.md) | Lobby, fixed progression, keys/walls, equipment and powers |
| [Illustrated tour](VISUAL_GUIDE.md) | Real Android views and the distinction from website illustrations |
| [Gameplay](GAMEPLAY.md) | Arrow/gate rules, actions, rewards and fair friend challenges |
| [Avatar equipment](AVATAR_EQUIPMENT.md) | All 18 items, sources, prices, three slots and exact benefit limits |
| [Powers and recovery](MOVE_BUDGET_POWERUPS.md) | Six effects, one-charge confirmation, Flow persistence and continuations |
| [Companions and events](COMPANIONS_EVENTS.md) | Collection, local daily/weekly play and ownership |
| [Validation](VALIDATION.md) | Executed checks, actual APK identities, synthetic profiles and remaining limits |

## One visual identity

| Guide | What it answers |
|---|---|
| [Design system](DESIGN_SYSTEM.md) | Night palette, pastel arrows, companion-led composition, type, motion and image provenance |
| [Art and audio](ART_AUDIO.md) | Code-drawn game art, themes, original sound and measured playback |
| [Brand assets](store/README.md) | Promotional originals, editable assets and export sizes |
| [Store copy](STORE_LISTING.md) | Truthful 1.5 copy drafts, localized short descriptions and screenshot order |
| [Public website](PUBLIC_PAGES.md) | FR/EN homepage, policy/deletion routes, local checks and deployment evidence |

The game, public pages and GitHub presentation share a visual family. The website hero combines original Nova art with an unedited Android capture; the composite itself is web artwork. Local page captures and [independent delivery of all 23 Hosting payloads plus three aliases](validation/site-v1.5/delivery.json) retain distinct scopes. [Earlier Hosting observations](validation/hosting-preview.md) concern their recorded revision.

## Identity, privacy and commercial gates

| Guide | What it answers |
|---|---|
| [Data and privacy inventory](PRIVACY.md) | Local saves/equipment, Google SDK paths, exports and account deletion |
| [Optional Google sign-in](GOOGLE_SIGN_IN.md) | Real identity, guest play and absence of cloud progression |
| [Monetization configuration](MONETIZATION_SETUP.md) | Current production/Lab flags, sample placements and purchase gates |
| [Android purchased-gold wallet](WALLET_ANDROID.md) | Server-authoritative paid funds, permanent cosmetics and account recovery |
| [Owner onboarding](PRODUCTION_ONBOARDING.md) | Existing public IDs and remaining provider/account steps |
| [Déploiement pas à pas — français](DEPLOIEMENT_PAS_A_PAS.md) | Existing Cloud Run/Hosting project, qualification and activation steps |
| [1.4 production foundation](PRODUCTION_RELEASE.md) | Historical implementation and separately dated deployment follow-up |
| [Server](../server/README.md) | Purchase verification, Firestore transactions, IAM and scheduled recovery |

The deployed wallet and earlier public pages have recorded routing/CORS observations. Web sign-in/deletion and the AdMob message's Published state are owner-reported. Completed Android login, installed UMP acceptance/refusal, Play license-test purchases, RTDN/Scheduler, automatic audit purge and commercial approval remain unqualified. The current Android APK embeds an empty wallet origin. A new site design does not remove any of these gates.

## Build, evidence and next decisions

| Guide | What it answers |
|---|---|
| [Development](DEVELOPMENT.md) | Architecture, tools, signing and reproducible checks |
| [Level design](LEVEL_DESIGN.md) | G/H progression, historical code compatibility and separate P prototypes |
| [Difficulty](DIFFICULTY.md) | Structural audits and why exact-solver results are not player retention |
| [Roadmap](ROADMAP.md) | Physical-device/player studies and later commercial gates |
| [Hit audit](HIT_AUDIT.md) | Unproven product assumptions and a measurable observation protocol |
| [Changelog](CHANGELOG.md) | Delivered scope by version |
| [Third-party notices](THIRD_PARTY_NOTICES.md) | SDK, Google branding and source/audio provenance |
| [Research archive](RESEARCH.md) | Dated market, licensing and initial concept decisions |

Historical [1.1](RELEASE_1_1.md), [1.2](RELEASE_1_2.md), [1.3](RELEASE_1_3.md) and [1.4](RELEASE_1_4.md) reports retain their original packages, hashes, dates and limitations. Current exact-byte scope is in [VALIDATION.md](VALIDATION.md); old screenshots and synthetic tests are not relabelled as final-release evidence.
