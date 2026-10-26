#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-wallet-recovery.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then
    task_java_bin="$JAVA_HOME/bin"
elif [[ -x /opt/homebrew/opt/openjdk@17/bin/javac ]]; then
    task_java_bin=/opt/homebrew/opt/openjdk@17/bin
else
    task_java_bin="$(dirname "$(command -v javac)")"
fi
"$task_java_bin/javac" -d "$task_build" \
    "$task_root/scripts/test-support/recovery/android/content/Context.java" \
    "$task_root/scripts/test-support/recovery/android/app/Activity.java" \
    "$task_root/scripts/test-support/recovery/com/androidhit/lumadrift/commerce/CommerceConfig.java" \
    "$task_root/scripts/test-support/recovery/com/androidhit/lumadrift/commerce/PlayGoldBilling.java" \
    "$task_root/scripts/test-support/recovery/com/androidhit/lumadrift/wallet/ProductionWalletClient.java" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/wallet/WalletRecoveryController.java" \
    "$task_root/scripts/WalletRecoveryTest.java"
"$task_java_bin/java" -cp "$task_build" com.androidhit.lumadrift.wallet.WalletRecoveryTest
