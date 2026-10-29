#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-companion-motion-v118.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then task_java_bin="$JAVA_HOME/bin"
elif [[ -x /opt/homebrew/opt/openjdk@17/bin/javac ]]; then task_java_bin=/opt/homebrew/opt/openjdk@17/bin
else task_java_bin="$(dirname "$(command -v javac)")"; fi
"$task_java_bin/javac" -Xlint:all -d "$task_build" \
    "$task_root"/scripts/test-support/android/annotation/*.java \
    "$task_root"/scripts/test-support/android/content/*.java \
    "$task_root"/scripts/test-support/android/graphics/*.java \
    "$task_root"/app/src/main/java/com/androidhit/lumadrift/data/*.java \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/ui/CompanionRig.java" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/ui/CompanionMotion.java" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/ui/CompanionRig118.java" \
    "$task_root/scripts/CompanionMotionV118Test.java"
"$task_java_bin/java" -Xmx512m -cp "$task_build" CompanionMotionV118Test "$@"
