#!/bin/sh
set -eu
TASK_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
TASK_JAVA_HOME=${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}
TASK_TEST_CLASSES=$(mktemp -d "${TMPDIR:-/tmp}/luma-progression-v3.XXXXXX")
trap 'rm -rf "$TASK_TEST_CLASSES"' EXIT HUP INT TERM
"$TASK_JAVA_HOME/bin/javac" -Xlint:all -d "$TASK_TEST_CLASSES" "$TASK_ROOT"/app/src/main/java/com/androidhit/lumadrift/engine/*.java "$TASK_ROOT/scripts/ProgressionAnalysis.java" "$TASK_ROOT/scripts/GenerateProgressionV3.java" "$TASK_ROOT/scripts/ProgressionV3Test.java" "$TASK_ROOT/scripts/LegacyContentFingerprint.java"
cd "$TASK_ROOT"
"$TASK_JAVA_HOME/bin/java" -Xmx768m -cp "$TASK_TEST_CLASSES" LegacyContentFingerprint > "$TASK_TEST_CLASSES/legacy-content-after.sha256"
diff -u docs/validation/v1.6/legacy-content-before.sha256 "$TASK_TEST_CLASSES/legacy-content-after.sha256"
cp "$TASK_TEST_CLASSES/legacy-content-after.sha256" docs/validation/v1.6/legacy-content-after.sha256
"$TASK_JAVA_HOME/bin/java" -Xmx768m -cp "$TASK_TEST_CLASSES" ProgressionV3Test "${1:-docs/data}"
