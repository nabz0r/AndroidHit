#!/bin/sh
set -eu
TASK_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
TASK_JAVA_HOME=${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}
TASK_BUILD=$(mktemp -d "${TMPDIR:-/tmp}/luma-saga-map-v17.XXXXXX")
trap 'rm -rf "$TASK_BUILD"' EXIT HUP INT TERM
cd "$TASK_ROOT"
"$TASK_JAVA_HOME/bin/javac" -Xlint:all -d "$TASK_BUILD" \
    app/src/main/java/com/androidhit/lumadrift/ui/ExperienceLayout.java \
    app/src/main/java/com/androidhit/lumadrift/ui/SagaMap.java scripts/SagaMapTest.java
"$TASK_JAVA_HOME/bin/java" -cp "$TASK_BUILD" SagaMapTest
