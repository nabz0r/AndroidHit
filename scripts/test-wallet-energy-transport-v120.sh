#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-energy-transport.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
task_java="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17}"
if [[ ! -x "$task_java/bin/javac" ]]; then task_java="$(dirname "$(dirname "$(command -v javac)")")"; fi
task_json="${LUMA_JSON_JAR:-}"
if [[ -z "$task_json" ]]; then task_json="$(rg --files "$HOME/.gradle/caches/modules-2/files-2.1/org.json/json" | rg '/json-[^/]+\.jar$' | sort | tail -1)"; fi
test -f "$task_json"
find "$task_root/scripts/test-support/vault" -name '*.java' -print0 | xargs -0 "$task_java/bin/javac" -Xlint:all -cp "$task_json" -d "$task_build" \
 "$task_root/scripts/test-support/audio/android/os/Handler.java" \
 "$task_root/scripts/test-support/audio/android/os/Looper.java" \
 "$task_root/scripts/test-support/audio/android/os/SystemClock.java" \
 "$task_root/scripts/test-support/energy/com/androidhit/lumadrift/commerce/CommerceConfig.java" \
 "$task_root/app/src/main/java/com/androidhit/lumadrift/commerce/BillingPolicy.java" \
 "$task_root/app/src/main/java/com/androidhit/lumadrift/wallet/ProductionWalletClient.java" \
 "$task_root/scripts/WalletEnergyTransportV120Test.java"
"$task_java/bin/java" -cp "$task_build:$task_json" com.androidhit.lumadrift.wallet.WalletEnergyTransportV120Test
