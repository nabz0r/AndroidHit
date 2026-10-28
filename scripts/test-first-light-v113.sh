#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-firstlight-v113.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
task_java_bin="${JAVA_HOME:-/tmp/luma-jdk21/jdk-21.0.13+9/Contents/Home}/bin"
"$task_java_bin/javac" -d "$task_build" \
 "$task_root"/scripts/test-support/android/annotation/*.java \
 "$task_root"/scripts/test-support/android/content/*.java \
 "$task_root"/scripts/test-support/android/graphics/*.java \
 "$task_root"/app/src/main/java/com/androidhit/lumadrift/data/*.java \
 "$task_root"/app/src/main/java/com/androidhit/lumadrift/engine/*.java \
 "$task_root"/app/src/main/java/com/androidhit/lumadrift/onboarding/*.java \
 "$task_root/scripts/FirstLightV113Test.java"
"$task_java_bin/java" -cp "$task_build" FirstLightV113Test
