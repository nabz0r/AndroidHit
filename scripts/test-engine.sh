#!/bin/sh
set -eu
TASK_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
TASK_JAVA_HOME=${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}
TASK_TEST_CLASSES=$(mktemp -d "${TMPDIR:-/tmp}/luma-engine.XXXXXX")
trap 'rm -rf "$TASK_TEST_CLASSES"' EXIT HUP INT TERM
"$TASK_JAVA_HOME/bin/javac" -d "$TASK_TEST_CLASSES" "$TASK_ROOT"/app/src/main/java/com/androidhit/lumadrift/engine/*.java "$TASK_ROOT/scripts/EngineTest.java"
"$TASK_JAVA_HOME/bin/java" -Xmx512m -cp "$TASK_TEST_CLASSES" EngineTest
