#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-companion-battle-v119.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then task_java_bin="$JAVA_HOME/bin"
elif [[ -x /opt/homebrew/opt/openjdk@17/bin/javac ]]; then task_java_bin=/opt/homebrew/opt/openjdk@17/bin
else task_java_bin="$(dirname "$(command -v javac)")"; fi
"$task_java_bin/javac" -Xlint:all -d "$task_build" \
    "$task_root/scripts/test-support/android/graphics/Color.java" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/data/Catalog.java" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/data/CompanionBattle.java" \
    "$task_root/scripts/CompanionBattleV119Test.java"
"$task_java_bin/java" -Xmx256m -cp "$task_build" CompanionBattleV119Test
