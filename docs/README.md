# Luma Drift documentation

**Play, understand the system, inspect the evidence, and decide what to build next.**

Current delivery: **[Luma Drift 1.4.0-rc1 production APK](../releases/luma-drift-1.4.0-rc1.apk)** · [Play AAB](../releases/luma-drift-1.4.0-rc1.aab). The production package contains the full puzzle and the owner's public Google/AdMob configuration, with sample creatives forced and checkout blocked until a wallet service is deployed/configured. Server source and FR/EN public pages are prepared, not hosted or commercially activated. [Production release](PRODUCTION_RELEASE.md) · [Current evidence](VALIDATION.md).

Preserved [stable 1.1](../releases/luma-drift-1.1.0.apk), [Lab 1.2](../releases/luma-drift-1.2.0-sandbox.apk) and [Lab 1.3](../releases/luma-drift-1.3.0-sandbox.apk) keep their original bytes and historical screenshots/reports. Lab currency remains isolated.

| Start here | What it answers |
|---|---|
| [Release and installation](RELEASE.md) | Which APK to install, signing and checksums |
| [Illustrated tour](VISUAL_GUIDE.md) | The interface and rules, explained with real game captures |
| [Gameplay](GAMEPLAY.md) | Shared puzzle rules, stable rewards and separate Lab economy |
| [Companions and events](COMPANIONS_EVENTS.md) | The Lab's 24 companions, preview/buy/equip flow and local event presentation |
| [Validation](VALIDATION.md) | Checks actually executed and device limitations |
| [Changelog](CHANGELOG.md) | What changed in each version |

## Product and presentation

| Document | What it answers |
|---|---|
| [Hit potential audit](HIT_AUDIT.md) | Why success is unproven, what improves the premise, and how to test it |
| [Roadmap](ROADMAP.md) | Staged work with explicit entry and exit criteria |
| [Store listing kit](STORE_LISTING.md) | Truthful copy, screenshot order and preparation still required |
| [Monetization research](MONETIZATION_RESEARCH.md) | Official-source evidence, SDK choices and limits of test mode |
| [Lab setup](MONETIZATION_SETUP.md) | No-charge tests, consent gates and future account/backend work |
| [Optional Google sign-in](GOOGLE_SIGN_IN.md) | Configuration, verified identity boundary and guest-first limits |
| [Production release](PRODUCTION_RELEASE.md) | Implemented server/client boundary, paid cosmetics and remaining activation |
| [Android wallet](WALLET_ANDROID.md) | Real checkout, account binding, local/paid separation and recovery |
| [Public pages](PUBLIC_PAGES.md) | FR/EN site, privacy/deletion flow, hosting gates and browser checks |
| [Server](../server/README.md) | Firestore/Play verification, tests, managed IAM and deployment |
| [Production onboarding](PRODUCTION_ONBOARDING.md) | Exact owner accounts, public IDs, setup order and remaining server work |
| [Brand kit](store/README.md) | Original promotional artwork, editable vector assets and provenance |
| [Art and audio](ART_AUDIO.md) | Original vector identity, companions, palettes and synthesized sound |

## Build and data

| Document | What it answers |
|---|---|
| [Development](DEVELOPMENT.md) | Architecture, build, signing and tests |
| [Level design](LEVEL_DESIGN.md) | Original campaign compatibility, Remix teaching boards and generator decisions |
| [Difficulty audit](DIFFICULTY.md) | Frozen 480-board order, structural score, remaining spikes and reproducible CSV |
| [Move-limit research](MOVE_LIMITS_RESEARCH.md) | Official precedents and action/recovery design rationale |
| [Action budgets and powers](MOVE_BUDGET_POWERUPS.md) | Exact allowance, six powers, unique rewards and saved-ledger contracts |
| [Privacy](PRIVACY.md) | Local data, optional exports and actual permissions |
| [Third-party notices](THIRD_PARTY_NOTICES.md) | SDK dependencies, branding provenance and their separate terms |
| [Initial research archive](RESEARCH.md) | The historical market study and original project assumptions |

The main README distinguishes installed-game captures from locally rendered `web-*` website captures. Graphics intended as explanatory diagrams or store artwork must be labeled separately. A roadmap item or store-copy draft is not proof that a feature ships; release evidence is the source of truth.
