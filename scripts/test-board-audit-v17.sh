#!/bin/sh
set -eu
TASK_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
TASK_JAVA_HOME=${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}
TASK_TEST_CLASSES=$(mktemp -d "${TMPDIR:-/tmp}/luma-board-audit-v17.XXXXXX")
trap 'rm -rf "$TASK_TEST_CLASSES"' EXIT HUP INT TERM
cd "$TASK_ROOT"
TASK_REPORT=${1:-docs/validation/v1.7}
mkdir -p "$TASK_REPORT"
"$TASK_JAVA_HOME/bin/javac" -Xlint:all -d "$TASK_TEST_CLASSES" \
  "$TASK_ROOT"/app/src/main/java/com/androidhit/lumadrift/engine/*.java \
  "$TASK_ROOT/scripts/BoardAuditV17.java" "$TASK_ROOT/scripts/LegacyContentFingerprint.java"
"$TASK_JAVA_HOME/bin/java" -Xmx768m -cp "$TASK_TEST_CLASSES" LegacyContentFingerprint > "$TASK_REPORT/board-audit-legacy.sha256"
diff -u docs/validation/v1.6/legacy-content-before.sha256 "$TASK_REPORT/board-audit-legacy.sha256"
"$TASK_JAVA_HOME/bin/java" -Xmx768m -cp "$TASK_TEST_CLASSES" BoardAuditV17 "$TASK_REPORT"
