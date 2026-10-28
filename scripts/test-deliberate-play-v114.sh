#!/bin/sh
set -eu
TASK_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
TASK_JDK=${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}
TASK_CLASSES=$(mktemp -d "${TMPDIR:-/tmp}/luma-deliberate-v114.XXXXXX")
trap 'rm -rf "$TASK_CLASSES"' EXIT HUP INT TERM
"$TASK_JDK/bin/javac" -Xlint:all -d "$TASK_CLASSES" "$TASK_ROOT"/app/src/main/java/com/androidhit/lumadrift/engine/*.java "$TASK_ROOT/scripts/DeliberatePlayV114Test.java"
"$TASK_JDK/bin/java" -Xmx512m -cp "$TASK_CLASSES" DeliberatePlayV114Test
