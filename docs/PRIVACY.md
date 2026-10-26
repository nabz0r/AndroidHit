# Luma Drift 1.1 privacy

Effective date: **26 October 2026**.

Luma Drift works offline. This version contains no account creation, advertising SDK, remote analytics SDK, Internet permission or backend. The application does not automatically send gameplay, progress or device identifiers to AndroidHit.

## Data kept on the device

The app stores campaign and Remix progression, earned coins, inventory, cosmetics, language/settings, interrupted-game history, XP/rank, mission claims, cumulative days played and cosmetic badge state in its private local storage. Daily and weekly puzzles and missions read the local calendar date. Audio is synthesized locally.

Saves do not synchronize between devices. Removing the app or clearing its application storage removes them. There is no recovery account or cloud restore. Android's automatic backup and transfer rules exclude the saved data.

## Optional local journal

The play journal is **disabled by default**. Enabling it in Settings starts a local observation window. It can record:

- UTC timestamps and local dates;
- app version and selected interface language on a foreground session;
- gameplay mode, board number and event identity where applicable;
- board starts, completions and exits, result stars, powers, undo count, remaining pieces and active play duration where recorded;
- challenge opening and a share-sheet handoff.

The journal keeps at most 2,000 event entries, plus its local session/day counters and observation state. It does not record a player account, advertising identifier, contact list, message recipient, location, microphone, camera or payment details. Its return indicators describe only this device's enabled observation window; they are not an aggregate retention report.

Disabling the journal stops new entries and leaves the existing local record available. Clearing it removes its observations and staged local journal JSON exports, resets the observation window, and leaves gameplay progression intact. If still enabled, a new window begins. Clearing or disabling cannot recall copies the player already exported to another application or person.

## Sharing is initiated by the player

**Challenge sharing** creates a PNG card and accompanying text containing a reproducible board code, an app link and the repository address. The card shows the initial puzzle/gates and, when shared after completion, result stars. The code identifies a board number or seed, not a player or saved wallet. Opening a code cannot grant progression rewards or powers.

**Journal export** creates a JSON file containing the local observations and summary fields. It can reveal play times and habits. The export control opens Android's share sheet; the player chooses whether and where to send the file. The game does not choose a contact or send it automatically.

Created share files are staged in the app's cache. A read-only content provider grants access for the explicit Android sharing flow; it does not expose the private save directory. The cache is periodically pruned and can also be removed by Android. After a file reaches a selected receiving app, that app and the recipient control their own copies and may transfer it over the network under their own policies.

Do not include someone else's journal in a public issue without their consent. Challenge cards and journal exports serve different purposes; sharing a challenge does not attach the journal.

## Permissions and external services

The only declared permission is vibration for optional haptics. The game does not request contacts, location, microphone, camera or shared-media permissions. A receiving share app can use its own permissions and network access; Luma Drift's lack of Internet permission does not prevent the app the player chooses from sending an exported file.

Android, the distribution service and an external browser opened through the source-code link have their own policies. This statement describes the application, not all behavior of the operating system or other applications.

## Currency and contact

The shop uses earned virtual coins. No real payment details are entered or processed. Device owners can change local dates and saves; no anti-cheat or financial guarantee is provided.

For issues, use the public [GitHub issue tracker](https://github.com/nabz0r/AndroidHit/issues). Do not post passwords, private signing keys or sensitive personal information.

If a later version adds networking, remote analytics, advertising, account recovery or payments, this policy and the corresponding store declarations must be updated before that release. This document is not a completed Play Console Data safety submission.
