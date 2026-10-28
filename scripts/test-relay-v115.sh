#!/bin/sh
set -eu
TASK_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
TASK_JDK=${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}
TASK_CLASSES=$(mktemp -d "${TMPDIR:-/tmp}/luma-relay-test.XXXXXX")
trap 'rm -rf "$TASK_CLASSES"' EXIT HUP INT TERM
TASK_REPORT=${1:-"$TASK_ROOT/docs/validation/v1.15/relay-engine"}
mkdir -p "$TASK_REPORT"
"$TASK_JDK/bin/javac" -Xlint:all -d "$TASK_CLASSES" "$TASK_ROOT"/app/src/main/java/com/androidhit/lumadrift/engine/*.java "$TASK_ROOT"/app/src/main/java/com/androidhit/lumadrift/progression/LearningGuide.java "$TASK_ROOT"/app/src/main/java/com/androidhit/lumadrift/progression/LearningStrings.java "$TASK_ROOT/scripts/RelayV115Test.java"
"$TASK_JDK/bin/java" -Xmx768m -cp "$TASK_CLASSES" RelayV115Test "$TASK_REPORT"
