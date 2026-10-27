# Luma Drift documentation

**Play, understand the system, inspect the evidence, and decide what to build next.**

**Current development candidate: [Luma Drift 1.5](RELEASE_1_5.md).** Its source work covers an avatar-centered lobby, [three equipment slots and 18 items](AVATAR_EQUIPMENT.md), 1,000 Journey boards plus 240 Remix boards, power-use safeguards and [repaired original music](validation/v1.5/audio/README.md). Integrated build, migration and installed runtime acceptance are tracked in the candidate report; this heading does not replace the identified 1.4 downloads below or claim a published 1.5 package. Commercial activation and real-player retention remain pending.

Current delivery: **[Luma Drift 1.4.0-rc1 production APK](../releases/luma-drift-1.4.0-rc1.apk)** · [Play AAB](../releases/luma-drift-1.4.0-rc1.aab). The unchanged production APK contains the full puzzle and the owner's public Google/AdMob configuration, with sample creatives forced and checkout blocked because its embedded wallet origin is empty. Cloud Run is now deployed, but Play identity verification, purchase qualification, Scheduler and RTDN remain pending; automatic audit purging is not active. The FR/EN qualification pages are now published on the stable domain and preview; stable-domain OAuth/deletion is owner-reported only and commercial activation remains pending. [Production release](PRODUCTION_RELEASE.md) · [Current evidence](VALIDATION.md).

The site configuration includes the public publisher name **nabilux**, support **nabz0r+gaming@gmail.com**, supplied Firebase Web identifiers and `https://luma-wallet-369472826910.europe-west1.run.app`. Final and preview Auth domains are confirmed; both API preflights now return HTTP 204 with their exact allowed origins. The owner reports the Web message “Ton compte Luma Drift et son portefeuille serveur ont été supprimés” after the guided deletion tests on the preview and stable domain. This is not independently observed or backed by a log/screenshot/Firebase Console check. Authenticated wallet GET, payments and Android Google login remain unqualified. Independent HTTPS reads now confirm the stable site’s five resources and repository-identical bytes. The owner reports the privacy URL, European-consent draft/settings and **Published** state in AdMob, with six game languages, refusal/close enabled and GDPR-region targeting. This has no independent console observation or proof of UMP display in the game, app-ads.txt verification or ad readiness. Next test the message in the existing production APK with sample ads retained. [Consent qualification instructions](DEPLOIEMENT_PAS_A_PAS.md#9-faire-valider-les-annonces-admob). `preparation: true` and closed purchases/live ads remain. [Preview access and observed HTTPS results](validation/hosting-preview.md). `preparation: true` shows a banner and does not disable the configured Google/deletion flow. The observed health/anonymous-wallet responses are HTTP 200/401, not proof of a paid purchase. [Deployment evidence](validation/cloud-run-health-route.md) · [Site qualification](PUBLIC_PAGES.md).

Preserved [stable 1.1](../releases/luma-drift-1.1.0.apk), [Lab 1.2](../releases/luma-drift-1.2.0-sandbox.apk) and [Lab 1.3](../releases/luma-drift-1.3.0-sandbox.apk) keep their original bytes and historical screenshots/reports. Lab currency remains isolated.

| Start here | What it answers |
|---|---|
| [Release and installation](RELEASE.md) | Which APK to install, signing and checksums |
| [1.5 candidate](RELEASE_1_5.md) | New lobby, 1,000 + 240 progression, six sealed-wall prototypes, equipment, powers/audio and exact acceptance status |
| [Avatar equipment](AVATAR_EQUIPMENT.md) | Three slots, 18 fixed items, bounded benefits and device-local ownership |
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
| [Déploiement pas à pas — français](DEPLOIEMENT_PAS_A_PAS.md) | Owner console steps, Cloud Shell, Hosting, purchase tests and ad activation |
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
