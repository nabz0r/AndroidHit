#!/bin/sh
set -eu
TASK_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
TASK_JAVA_HOME=${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}
TASK_TEST_CLASSES=$(mktemp -d "${TMPDIR:-/tmp}/luma-difficulty.XXXXXX")
trap 'rm -rf "$TASK_TEST_CLASSES"' EXIT HUP INT TERM
"$TASK_JAVA_HOME/bin/javac" -d "$TASK_TEST_CLASSES" "$TASK_ROOT"/app/src/main/java/com/androidhit/lumadrift/engine/Direction.java "$TASK_ROOT"/app/src/main/java/com/androidhit/lumadrift/engine/Tile.java "$TASK_ROOT"/app/src/main/java/com/androidhit/lumadrift/engine/Level.java "$TASK_ROOT"/app/src/main/java/com/androidhit/lumadrift/engine/Board.java "$TASK_ROOT"/app/src/main/java/com/androidhit/lumadrift/engine/Solver.java "$TASK_ROOT"/app/src/main/java/com/androidhit/lumadrift/engine/LevelCatalog.java "$TASK_ROOT"/app/src/main/java/com/androidhit/lumadrift/engine/RemixCatalog.java "$TASK_ROOT"/app/src/main/java/com/androidhit/lumadrift/engine/BalancedCatalog.java "$TASK_ROOT/scripts/DifficultyTest.java"
cd "$TASK_ROOT"
"$TASK_JAVA_HOME/bin/java" -Xmx512m -cp "$TASK_TEST_CLASSES" DifficultyTest "$@"
