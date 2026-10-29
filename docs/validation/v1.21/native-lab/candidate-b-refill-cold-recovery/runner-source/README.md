# Separate 1.21 Lab native observer

SDK-only package `com.androidhit.lumadrift.sandbox.v121test`, targeting only `com.androidhit.lumadrift.sandbox`. Derived from the production 1.21 observer with the guarded 1.20 Lab purchase flow and seven-map read-only snapshots. The existing Lab F profile is preserved: U1–U10 earned, unlocked11,730Coins,450 app-provided test Gold and its prior completed purchase receipts. Exact old APK: `7c8ccfde3a0050b35b0312a61e2cb07973c594629bbf97493ec8bbe2829410cf`.

Only the existing task AVD `Luma119-Battle-QA`, `emulator-5602`, is allowed; the runner pins its immutable creation receipt. All new candidate/observer bytes, signer, version and installed bytes must match. Snapshot-only upgrade audit precedes the first new Activity. The profile is never cleared, copied into the app or replaced. No preference writes, Gold grants, clock edits, solver calls, external identities, ads, billing or real money.

The bounded refill phase uses actual confirmed eligible restarts to drain naturally available energy, then requests and confirms one50testGold→10energy refill. It checks450→400testGold, stock0, empty outbox and730Coins unchanged. One real eligible U11 attempt consumes one energy, making the cold9-energy result able to detect duplicate credits. The next phase promptly recreates the process and resumes that same run; real elapsed refill ticks remain outside any promise of fixed energy after ten minutes. The actual count and receipt/journal equality are recorded. Original450-Gold purchase receipts must remain present.

Production preferences are snapshotted read-only before and after all Lab work and compared exactly. Lab tests establish local sandbox behavior only. They do not enable or qualify production billing or real Gold purchases.
