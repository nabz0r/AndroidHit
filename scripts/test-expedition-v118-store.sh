#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-expedition-v118-store.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then task_java_bin="$JAVA_HOME/bin"
elif [[ -x /opt/homebrew/opt/openjdk@17/bin/javac ]]; then task_java_bin=/opt/homebrew/opt/openjdk@17/bin
else task_java_bin="$(dirname "$(command -v javac)")"; fi
"$task_java_bin/javac" -Xlint:all -d "$task_build" \
    "$task_root"/scripts/test-support/android/annotation/*.java \
    "$task_root"/scripts/test-support/android/content/*.java \
    "$task_root"/scripts/test-support/android/graphics/*.java \
    "$task_root"/app/src/main/java/com/androidhit/lumadrift/data/*.java \
    "$task_root"/app/src/main/java/com/androidhit/lumadrift/engine/*.java \
    "$task_root/scripts/RewardsCosmeticsV17Test.java" \
    "$task_root/scripts/ExpeditionV118StoreTest.java"
"$task_java_bin/java" -Xmx512m "-Dluma.test.root=$task_root" -cp "$task_build" ExpeditionV118StoreTest "$@"
