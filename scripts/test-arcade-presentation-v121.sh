#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-arcade-presentation-v121.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
task_java="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}"
"$task_java/bin/javac" -Xlint:all -d "$task_build" \
 "$task_root"/app/src/main/java/com/androidhit/lumadrift/engine/*.java \
 "$task_root/app/src/main/java/com/androidhit/lumadrift/ui/RewardMotion121.java" \
 "$task_root/scripts/ArcadePresentationV121Test.java"
"$task_java/bin/java" -Xmx512m -cp "$task_build" ArcadePresentationV121Test "$@"
