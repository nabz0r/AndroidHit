# Luma Drift privacy — stable game and test Lab

Effective date: **26 October 2026**.

This statement distinguishes the **stable Luma Drift 1.1.0 game**, application ID `com.androidhit.lumadrift`, from **Luma Drift Lab 1.2.0-sandbox**, application ID `com.androidhit.lumadrift.sandbox`. Their private saves and wallets are separate. Lab test gold does not transfer to the stable game.

| | Stable 1.1.0 | Lab 1.2.0-sandbox |
|---|---|---|
| Puzzle play | Offline | Offline |
| Advertising/Billing SDK | Absent | Google test advertising, UMP and Billing query integration |
| Optional identity | No account | Credential Manager + Firebase Auth; owner configuration absent by default |
| Internet permission | Absent | Present; Google SDK routes can communicate externally |
| Real payments | Absent | Disabled; clearly labelled local simulation only |
| Optional app journal | Device-local, off by default | Device-local, off by default, with optional test-commerce callbacks |
| AndroidHit gameplay backend | None | None |

Neither app automatically uploads its play journal to AndroidHit. **The Lab's use of Google SDKs is a separate external-data path.** “Local journal” does not mean “no SDK network traffic.” Lab SDK/runtime verification is recorded separately from stable evidence in [VALIDATION.md](VALIDATION.md).

## Data kept on the device

Both apps store their own campaign and Remix progression, coins, inventory, cosmetics, language/settings, interrupted-game history, XP/rank, mission claims, cumulative days played and cosmetic badge state in private local storage. The Lab additionally stores its coarse age declaration, explicit ad opt-in, simulated pending/completed transaction state and one-time reward markers. If optional identity is configured and used, Firebase Auth can persist its SDK session; the app also remembers that a prior sign-in occurred. Daily and weekly puzzles and missions read the local calendar date. Audio is synthesized locally.

Saves do not synchronize between devices. Removing the app or clearing its application storage removes them. Optional Lab sign-in does not add cloud save, progress recovery or a purchased-wallet service. Android's automatic backup and transfer rules exclude the saved game data.

## Optional local journal

The play journal is **disabled by default**. Enabling it in Settings starts a local observation window. It can record:

- UTC timestamps and local dates;
- app version and selected interface language on a foreground session;
- gameplay mode, board number and event identity where applicable;
- board starts, completions and exits, result stars, powers, undo count, remaining pieces and active play duration where recorded;
- challenge opening and a share-sheet handoff.

When enabled in the Lab, the journal can also record test purchase outcomes, ad eligibility/loading, impressions, earned rewards and paid-event callback values such as currency, micros and precision. Such values are diagnostics from test mode, not real revenue. No Firebase Analytics or remote gameplay analytics service is included. Optional Firebase Auth is a separate identity service, not the destination of the play journal.

The journal keeps at most 2,000 event entries, plus its local session/day counters and observation state. It does not record a player account, advertising identifier, contact list, message recipient, location, microphone, camera or payment details. Its return indicators describe only this device's enabled observation window; they are not an aggregate retention report.

Disabling the journal stops new entries and leaves the existing local record available. Clearing it removes its observations and staged local journal JSON exports, resets the observation window, and leaves gameplay progression intact. If still enabled, a new window begins. Clearing or disabling cannot recall copies the player already exported to another application or person.

## Sharing is initiated by the player

**Challenge sharing** creates a PNG card and accompanying text containing a reproducible board code, an app link and the repository address. The card shows the initial puzzle/gates and, when shared after completion, result stars. The code identifies a board number or seed, not a player or saved wallet. Opening a code cannot grant progression rewards or powers.

**Journal export** creates a JSON file containing the local observations and summary fields. It can reveal play times and habits. The export control opens Android's share sheet; the player chooses whether and where to send the file. The game does not choose a contact or send it automatically.

Created share files are staged in the app's cache. A read-only content provider grants access for the explicit Android sharing flow; it does not expose the private save directory. The cache is periodically pruned and can also be removed by Android. After a file reaches a selected receiving app, that app and the recipient control their own copies and may transfer it over the network under their own policies.

Do not include someone else's journal in a public issue without their consent. Challenge cards and journal exports serve different purposes; sharing a challenge does not attach the journal.

## Permissions and external services

Stable 1.1 declares only vibration for optional haptics and has no Internet permission. The Lab's source manifest additionally declares Internet and network-state access; SDK dependencies can contribute merged manifest entries. The final package evidence must be used to identify its exact permission set. Neither app requests contacts, location, microphone, camera or shared-media permissions for the puzzle or sharing flow.

A receiving share app can use its own permissions and network access. The stable game's lack of Internet permission does not prevent the app the player chooses from sending an exported file.

Android, the distribution service and an external browser opened through the source-code link have their own policies. This statement describes the application, not all behavior of the operating system or other applications.

## Lab: Google advertising, consent and Billing

The Lab integrates **GMA Next-Gen 1.6.0**, **UMP 4.0.0** and **Billing 9.1.0**. The advertising controller requires a declared adult state and a separate explicit opt-in before requesting UMP consent information. Unknown/minor declarations block that route. This is a test gate, not age verification or a declaration that the game is approved for a particular audience.

UMP can contact Google to determine applicable choices. Before ad initialization/requests, the controller checks UMP's `canRequestAds()` state. The player still has to press the manual load control; the controller does not automatically preload an ad on game launch. Advertising uses Google's sample test identifiers. The privacy-options control opens the supported UMP form when required; turning off the app's ad opt-in prevents new controller requests and discards its cached ad state. It cannot recall information previously processed by Google.

The SDK can process information such as IP address, interactions, diagnostics and device/account identifiers according to its configuration and Google's disclosure. Test creative does not remove those data considerations. An app-level opt-in is not a substitute for the owner's configured consent messages, regional handling or Play declarations. No owner-configured consent/account approval, absence of all pre-consent SDK traffic, or universal compliance is claimed by source inspection or a screenshot. [Google SDK disclosure](https://developers.google.com/admob/android/next-gen/privacy/play-data-disclosure) · [UMP integration](https://developers.google.com/admob/android/privacy).

Opening the Lab commerce screen also starts the **Play Billing connection/product-query route**, independently of advertising opt-in. It can communicate with Google Play. Its actual checkout remains disabled; it does not grant gold from an unverified Google purchase observation, fabricate a Google receipt, acknowledge/consume a real purchase or deploy a verification server. The native simulator creates only local no-charge state. There is no real card/payment entry in that simulator.

The owner has not configured production AdMob identifiers, Play gold products, license testers or a verified wallet backend. Account-specific consent, real Play test purchases and production data handling require further configuration and evidence before activation. [Lab setup and remaining dependencies](MONETIZATION_SETUP.md).

## Lab: optional Google identity

Settings → Account provides guest play and an optional sign-in route using Credential Manager **1.6.0**, Google ID **1.2.1** and Firebase Auth **25.0.0**. The default delivered configuration does not contain the owner's OAuth/Firebase identifiers. It reports that setup is missing and does not fabricate a profile or successful login.

With valid configuration and an explicit sign-in action, Credential Manager obtains a Google credential and Firebase Auth sends it to its managed authentication service for verification. The UI can show a returned display name or email, and the SDK can persist an authentication session. A previously signed-in, configured session may be restored when the account screen is opened. The app does not manually store/log the Google ID token or put identity fields into its play journal. This does not mean the authentication providers receive no data; identity processing is distinct from the local game save.

The adapter disables Firebase's default data collection setting and does not include Firebase Analytics. Authentication still requires its own network processing. Signing in does not enable advertising consent, upload progression, attach an identity to a paid wallet or create cloud saves. Advertising opt-in remains separate.

Sign-out clears the app's Firebase session and requests credential-state clearing while preserving local puzzle progress. The confirmed account-deletion action requests deletion of the **Firebase app account**, not the person's Google account, and can require a recent sign-in. It does not erase local game saves or copies of journal files already shared. Clearing the app's local data remains a separate Android action. These are implemented SDK operations; successful configured login, sign-out and remote deletion have not been established without the owner's setup. [Configuration, provider data references and evidence limits](GOOGLE_SIGN_IN.md).

## Currency and contact

The [recorded identity UI check](validation/lab/identity-ui.txt) exercised English/French guest and missing-configuration handling. It did not authenticate a real Google account. Test advertising and consent-form observations use Google's sample configuration; they do not qualify the owner's future consent setup or establish revenue. [Full executed scope](VALIDATION.md).

Stable 1.1 uses earned virtual coins. The Lab also credits simulated packs and earned test-ad rewards into its separate local wallet, with no real charge or redeemable value. Device owners can change local dates and saves; no anti-cheat or financial guarantee is provided.

For issues, use the public [GitHub issue tracker](https://github.com/nabz0r/AndroidHit/issues). Do not post passwords, private signing keys or sensitive personal information.

Any future change to production networking, advertising, remote analytics, account recovery or real payments requires an updated statement and store declarations matching the exact delivered behavior. This document describes the stable release and present test scope; it is not a completed Play Console Data safety submission.
