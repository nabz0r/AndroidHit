#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-home-geometry.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then task_java_bin="$JAVA_HOME/bin"
elif [[ -x /opt/homebrew/opt/openjdk@17/bin/javac ]]; then task_java_bin=/opt/homebrew/opt/openjdk@17/bin
else task_java_bin="$(dirname "$(command -v javac)")"; fi
"$task_java_bin/javac" -d "$task_build" \
    "$task_root"/scripts/test-support/android/annotation/*.java \
    "$task_root"/scripts/test-support/android/content/*.java \
    "$task_root"/scripts/test-support/android/graphics/*.java \
    "$task_root"/scripts/home-render-support/android/graphics/*.java \
    "$task_root"/scripts/home-render-support/com/androidhit/lumadrift/ui/*.java \
    "$task_root"/app/src/main/java/com/androidhit/lumadrift/data/*.java \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/ui/HomeScene.java" \
    "$task_root/scripts/HomeV115GeometryTest.java"
"$task_java_bin/java" -Djava.awt.headless=true -Xmx512m -cp "$task_build" HomeV115GeometryTest "$@"
