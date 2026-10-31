#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-cognitive124.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then task_java_bin="$JAVA_HOME/bin"
elif [[ -x /opt/homebrew/opt/openjdk@17/bin/javac ]]; then task_java_bin=/opt/homebrew/opt/openjdk@17/bin
else task_java_bin="$(dirname "$(command -v javac)")"; fi
"$task_java_bin/javac" -Xlint:all -d "$task_build" \
 "$task_root"/scripts/test-support/android/graphics/Color.java \
 "$task_root"/app/src/main/java/com/androidhit/lumadrift/engine/*.java \
 "$task_root/app/src/main/java/com/androidhit/lumadrift/data/EconomyCurve121.java" \
 "$task_root/app/src/main/java/com/androidhit/lumadrift/data/Catalog.java" \
 "$task_root/app/src/main/java/com/androidhit/lumadrift/data/CompanionProgression.java" \
 "$task_root/app/src/main/java/com/androidhit/lumadrift/data/RankCopyRules124.java" \
 "$task_root/app/src/main/java/com/androidhit/lumadrift/data/CompanionBattle.java" \
 "$task_root/app/src/main/java/com/androidhit/lumadrift/data/CompanionBattleAction.java" \
 "$task_root/app/src/main/java/com/androidhit/lumadrift/data/TacticalChapterCatalog.java" \
 "$task_root/app/src/main/java/com/androidhit/lumadrift/data/TacticalChapterRun.java" \
 "$task_root/app/src/main/java/com/androidhit/lumadrift/data/TacticalChapterAction.java" \
 "$task_root"/scripts/cognitive_lab/*.java "$task_root"/scripts/cognitive_lab/v124/*.java
cd "$task_root"
"$task_java_bin/java" -Xmx768m -cp "$task_build" cognitive_lab.CognitiveLab124 --root "$task_root" "$@"
