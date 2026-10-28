#!/bin/sh
set -eu
TASK_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
TASK_JDK=${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}
TASK_CLASSES=$(mktemp -d "${TMPDIR:-/tmp}/luma-audit-v114.XXXXXX")
trap 'rm -rf "$TASK_CLASSES"' EXIT HUP INT TERM
TASK_REPORT=${1:-"$TASK_ROOT/docs/validation/v1.14/audit-journey"}
mkdir -p "$TASK_REPORT"
"$TASK_JDK/bin/javac" -Xlint:all -d "$TASK_CLASSES" "$TASK_ROOT"/app/src/main/java/com/androidhit/lumadrift/engine/*.java "$TASK_ROOT/scripts/AuditJourneyV114.java"
"$TASK_JDK/bin/java" -Xmx768m -cp "$TASK_CLASSES" AuditJourneyV114 "$TASK_REPORT" "${2:-1000}"
