#!/bin/sh
set -eu
TASK_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
TASK_JAVA_HOME=${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}
TASK_GENERATOR_CLASSES=$(mktemp -d "${TMPDIR:-/tmp}/luma-author-v3.XXXXXX")
trap 'rm -rf "$TASK_GENERATOR_CLASSES"' EXIT HUP INT TERM
"$TASK_JAVA_HOME/bin/javac" -d "$TASK_GENERATOR_CLASSES" "$TASK_ROOT"/app/src/main/java/com/androidhit/lumadrift/engine/*.java "$TASK_ROOT/scripts/ProgressionAnalysis.java" "$TASK_ROOT/scripts/GenerateProgressionV3.java"
"$TASK_JAVA_HOME/bin/java" -Xmx768m -cp "$TASK_GENERATOR_CLASSES" GenerateProgressionV3 "$TASK_ROOT"
