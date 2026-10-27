# Guardian receipt, avatar-style and copy integration

Run `bash scripts/test-guardian-store-v19.sh`. This suite uses the real `GameStore`, `GuardianEncounter`, `AvatarStyles` and `V19Strings` with the established `RewardsCosmeticsV17Test.Ctx` adapter, including memory-before-disk failures and cold reconstruction.

The suite completes all 1,000 Journey progression records, checks every ten-clear Guardian unlock, solves each of the 100 real encounters and requests its one 100-LumaCoin reward. It verifies duplicate and independent-handle calls, cold receipts, full-wallet refusal without consuming the receipt, exact cap boundary, failed commits both before and after memory update, process death before a recovery flush, and 24 concurrent two-handle claims. A charged blocked-probe fixture also proves that Undo works at zero actions and refunds only the reversed productive hit.

All 12 avatar treatments persist across handles/reconstruction, produce 288 unique companion/style combinations, and leave the currency, inventory, companion ownership, progression, pack price and equipped-gear benefits unchanged. Invalid IDs are rejected and corrupted saved IDs safely render the default. The six-language V19 vocabulary is checked with correctly typed formatting arguments and explicit locale fallback.

`completeGuardian` remains a local caller contract, like `completeLevel`: the gameplay controller must first establish that the encounter is solved. The integration test does that with a real encounter; the separate native UI regression must prove that the installed reward button is reached only after victory. This suite is not server anti-cheat verification, Google sign-in qualification, payment verification or real friend/gift traffic.
