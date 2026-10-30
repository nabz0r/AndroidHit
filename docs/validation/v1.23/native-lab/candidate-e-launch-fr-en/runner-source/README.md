# Native Lab1.23 rc2 isolation smoke

SDK-only clone of the historical observer, which remains unchanged. This runner
accepts only pinned retained5602, exact final E Lab099d4dc7… and production429a443c…
with version1.23.0-sandbox-rc2/code31. Upgrade additionally requires the exact
preserved Lab1.22 SHA882481a2… and checks all sandbox preference files before and
after installation, before first Activity launch. It hashes all persistent
production preferences before/after the entire Lab phase and refuses any change.

Allowed UI smoke operations are snapshots, FR/EN selection, captures and read-only
assertions only. No purchase, currency grant, progression, clearing, fixture
injection or game uninstall. The observer alone is installed/uninstalled. Build
outputs and each execution evidence directory must be new. APK signature and
compiled source receipt are verified. This tests isolation/launch on the task
emulator, not purchases, humans or physical-device performance.
