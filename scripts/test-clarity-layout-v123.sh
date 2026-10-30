#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_java="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-clarity-layout123.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
"$task_java/bin/javac" -Xlint:all -d "$task_build" "$task_root/app/src/main/java/com/androidhit/lumadrift/ui/CompanionSheetLayout123.java" "$task_root/app/src/main/java/com/androidhit/lumadrift/ui/PlaySceneLayout.java" "$task_root/scripts/ClarityLayout123Test.java"
"$task_java/bin/java" -cp "$task_build" ClarityLayout123Test
