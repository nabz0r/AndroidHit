#!/bin/sh
set -eu
TASK_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
TASK_JDK=${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}
TASK_CLASSES=$(mktemp -d "${TMPDIR:-/tmp}/luma-strategy-v4.XXXXXX")
trap 'rm -rf "$TASK_CLASSES"' EXIT HUP INT TERM
TASK_REPORT=${1:-"$TASK_ROOT/docs/validation/v1.8/engine"}
mkdir -p "$TASK_REPORT"
"$TASK_JDK/bin/javac" -Xlint:all -d "$TASK_CLASSES" "$TASK_ROOT"/app/src/main/java/com/androidhit/lumadrift/engine/*.java "$TASK_ROOT/scripts/StrategyV4Audit.java" "$TASK_ROOT/scripts/StrategyV4ContractTest.java" "$TASK_ROOT/scripts/ProgressionAnalysis.java" "$TASK_ROOT/scripts/LegacyContentFingerprint.java"
"$TASK_JDK/bin/java" -cp "$TASK_CLASSES" LegacyContentFingerprint > "$TASK_REPORT/legacy-content.sha256"
diff -u "$TASK_ROOT/docs/validation/v1.6/legacy-content-before.sha256" "$TASK_REPORT/legacy-content.sha256"
"$TASK_JDK/bin/java" -Xmx512m -cp "$TASK_CLASSES" StrategyV4ContractTest > "$TASK_REPORT/strategy-contracts.txt"
cat "$TASK_REPORT/strategy-contracts.txt"
"$TASK_JDK/bin/java" -Xmx768m -cp "$TASK_CLASSES" StrategyV4Audit "$TASK_REPORT"
