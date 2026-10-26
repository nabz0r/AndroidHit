# Luma Drift 1.0.0 privacy

Effective date: 26 October 2026.

Luma Drift works offline. This release contains no account creation, advertising SDK, analytics SDK, network permission or backend. The application does not send game progress or device identifiers to AndroidHit.

Progress, earned coins, inventory, cosmetics, settings and interrupted-game history are stored in the app's private local storage. Daily and weekly puzzles read the device's local calendar date. Removing the app or clearing its data removes those saves. The app does not provide cloud synchronization or a recovery account.

The app requests vibration permission for optional haptics. It does not request contacts, location, microphone, camera or shared-media access. Music and effects are synthesized locally.

Android itself, the app distribution service and any external browser used to open the optional source-code link operate under their own policies. Automated cloud backup and device-transfer rules exclude the app's saved data. This statement describes the application code, not all behavior of the operating system.

The shop uses earned virtual coins. No real payment details are entered or processed. Event dates and local saves can be changed by the device owner; no anti-cheat or financial guarantee is provided.

For issues, use the public [GitHub issue tracker](https://github.com/nabz0r/AndroidHit/issues). Do not include passwords, private signing keys or sensitive personal information in an issue.

If a later version adds networking, analytics, advertising or payments, this policy and the corresponding Store declarations must be updated before release.
