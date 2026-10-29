#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-adventure-v119.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then task_java_bin="$JAVA_HOME/bin"
elif [[ -x /opt/homebrew/opt/openjdk@17/bin/javac ]]; then task_java_bin=/opt/homebrew/opt/openjdk@17/bin
else task_java_bin="$(dirname "$(command -v javac)")"; fi
"$task_java_bin/javac" -d "$task_build" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/ui/Adventure119Layout.java" \
    "$task_root/scripts/AdventureV119GeometryTest.java"
"$task_java_bin/java" -cp "$task_build" AdventureV119GeometryTest
