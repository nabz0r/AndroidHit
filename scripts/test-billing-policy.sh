#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-billing-policy.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then
    task_java_bin="$JAVA_HOME/bin"
elif [[ -x /opt/homebrew/opt/openjdk@17/bin/javac ]]; then
    task_java_bin=/opt/homebrew/opt/openjdk@17/bin
else
    task_java_bin="$(dirname "$(command -v javac)")"
fi
"$task_java_bin/javac" -d "$task_build" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/data/Strings.java" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/commerce/BillingPolicy.java" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/commerce/WalletStrings.java" \
    "$task_root/scripts/BillingPolicyTest.java"
"$task_java_bin/java" -cp "$task_build" com.androidhit.lumadrift.commerce.BillingPolicyTest
