#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-elemental-shell-v121.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
task_java="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}"
"$task_java/bin/javac" -Xlint:all -d "$task_build" \
 "$task_root/scripts/test-support/android/graphics/Color.java" \
 "$task_root"/app/src/main/java/com/androidhit/lumadrift/engine/*.java \
 "$task_root/app/src/main/java/com/androidhit/lumadrift/data/Catalog.java" \
 "$task_root/app/src/main/java/com/androidhit/lumadrift/data/CompanionProgression.java" "$task_root/app/src/main/java/com/androidhit/lumadrift/data/CompanionBattle.java" \
 "$task_root/app/src/main/java/com/androidhit/lumadrift/data/CompanionBattleAction.java" \
 "$task_root/scripts/ElementalShellV121Test.java"
"$task_java/bin/java" -Xmx512m -cp "$task_build" ElementalShellV121Test "$@"
