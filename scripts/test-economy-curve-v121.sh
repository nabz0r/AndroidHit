#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-economy-curve-v121.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
task_java="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}"
"$task_java/bin/javac" -Xlint:all -d "$task_build" \
 "$task_root"/scripts/test-support/android/annotation/*.java \
 "$task_root"/scripts/test-support/android/content/*.java \
 "$task_root"/scripts/test-support/android/graphics/*.java \
 "$task_root"/app/src/main/java/com/androidhit/lumadrift/data/*.java \
 "$task_root"/app/src/main/java/com/androidhit/lumadrift/engine/*.java \
 "$task_root/scripts/RewardsCosmeticsV17Test.java" "$task_root/scripts/EconomyCurveV121Test.java"
"$task_java/bin/java" -Xmx512m -cp "$task_build" EconomyCurveV121Test "$@"
