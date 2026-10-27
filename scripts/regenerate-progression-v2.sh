#!/usr/bin/env bash
set -euo pipefail
# Offline authoring tool. Writes generated Java + metrics to a caller-selected directory.
# It never installs those outputs into the app automatically.
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-generate-classes.XXXXXX")"
task_output="${1:-$(mktemp -d "${TMPDIR:-/tmp}/luma-generated-content.XXXXXX")}"
trap 'rm -rf "$task_build"' EXIT
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then
    task_java_bin="$JAVA_HOME/bin"
elif [[ -x /opt/homebrew/opt/openjdk@17/bin/javac ]]; then
    task_java_bin=/opt/homebrew/opt/openjdk@17/bin
else
    task_java_bin="$(dirname "$(command -v javac)")"
fi
"$task_java_bin/javac" -d "$task_build" "$task_root"/app/src/main/java/com/androidhit/lumadrift/engine/*.java "$task_root/scripts/GenerateProgressionV2.java"
"$task_java_bin/java" -Xmx1g -cp "$task_build" com.androidhit.lumadrift.engine.GenerateProgressionV2 "$task_output"
echo "Generated outputs: $task_output"
