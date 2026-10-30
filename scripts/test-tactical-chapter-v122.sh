#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-tactical-v122.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then task_java_bin="$JAVA_HOME/bin"
elif [[ -x /opt/homebrew/opt/openjdk@17/bin/javac ]]; then task_java_bin=/opt/homebrew/opt/openjdk@17/bin
else task_java_bin="$(dirname "$(command -v javac)")"; fi
task_engines=()
while IFS= read -r file; do task_engines+=("$file"); done < <(find "$task_root/app/src/main/java/com/androidhit/lumadrift/engine" -name '*.java' -type f | sort)
"$task_java_bin/javac" -Xlint:all -d "$task_build" \
    "$task_root/scripts/test-support/android/graphics/Color.java" \
    "${task_engines[@]}" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/data/Catalog.java" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/data/CompanionBattle.java" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/data/TacticalChapterCatalog.java" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/data/TacticalChapterRun.java" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/data/TacticalChapterAction.java" \
    "$task_root/scripts/TacticalChapterV122Test.java"
"$task_java_bin/java" -Xmx384m -cp "$task_build" TacticalChapterV122Test "$@"
