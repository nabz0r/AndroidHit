#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-energy-v120.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
task_java_bin="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}/bin"
"$task_java_bin/javac" -d "$task_build" \
 "$task_root"/scripts/test-support/android/annotation/*.java \
 "$task_root"/scripts/test-support/android/content/*.java \
 "$task_root"/scripts/test-support/android/graphics/*.java \
 "$task_root"/app/src/main/java/com/androidhit/lumadrift/data/*.java \
 "$task_root"/app/src/main/java/com/androidhit/lumadrift/engine/*.java \
 "$task_root"/app/src/main/java/com/androidhit/lumadrift/onboarding/*.java \
 "$task_root/app/src/main/java/com/androidhit/lumadrift/ui/PuzzleReaction.java" \
 "$task_root/scripts/SessionEnergyV120Test.java"
"$task_java_bin/java" -Xmx512m -cp "$task_build" SessionEnergyV120Test
