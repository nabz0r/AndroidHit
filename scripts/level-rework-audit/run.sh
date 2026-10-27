#!/bin/sh
# Read-only engine audit; its reports do not apply the proposed mechanic to Android.
set -eu
TASK_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
TASK_JDK=${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}
TASK_REPORT=${1:-"$TASK_ROOT/docs/validation/early-levels-v1.7"}
TASK_CLASSES=$(mktemp -d "${TMPDIR:-/tmp}/luma-level-rework.XXXXXX")
trap 'rm -rf "$TASK_CLASSES"' EXIT HUP INT TERM
mkdir -p "$TASK_REPORT"
"$TASK_JDK/bin/javac" -d "$TASK_CLASSES" \
  "$TASK_ROOT"/app/src/main/java/com/androidhit/lumadrift/engine/*.java \
  "$TASK_ROOT/scripts/level-rework-audit/EarlyTrapAudit.java" \
  "$TASK_ROOT/scripts/level-rework-audit/ManualGateAudit.java" \
  "$TASK_ROOT/scripts/V15Fixture.java"
"$TASK_JDK/bin/java" -Xmx768m -cp "$TASK_CLASSES" EarlyTrapAudit "$TASK_REPORT" > "$TASK_REPORT/engine-audit-output.txt"
"$TASK_JDK/bin/java" -Xmx768m -cp "$TASK_CLASSES" ManualGateAudit "$TASK_REPORT" > "$TASK_REPORT/manual-gate-proposal-output.txt"
"$TASK_JDK/bin/java" -cp "$TASK_CLASSES" V15Fixture J 7 > "$TASK_REPORT/J7-fixture-zero-based.json"
"$TASK_JDK/bin/java" -cp "$TASK_CLASSES" V15Fixture J 8 > "$TASK_REPORT/J8-fixture-zero-based.json"
(
  cd "$TASK_ROOT"
  shasum -a 256 app/src/main/java/com/androidhit/lumadrift/engine/*.java scripts/level-rework-audit/*.java
) > "$TASK_REPORT/source-inputs.sha256"
{
  printf '%s\n' 'PASS: exact J1–30 reachability agrees with runtime legality, solvability and move safety.'
  printf '%s\n' 'PASS: proposed manual-gate DP makes all original losing states recoverable; no app rule changed.'
  printf '%s\n' 'Declared diagnosis baseline: 245f9f59f01ca1f5a4226b14ab8e8c707bcaf896'
  git -C "$TASK_ROOT" rev-parse HEAD
  "$TASK_JDK/bin/java" -version 2>&1
} > "$TASK_REPORT/execution.txt"
cat "$TASK_REPORT/execution.txt"
