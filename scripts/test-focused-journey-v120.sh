#!/bin/sh
set -eu
TASK_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
TASK_JDK=${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}
TASK_CLASSES=$(mktemp -d "${TMPDIR:-/tmp}/luma-focused-v120.XXXXXX")
trap 'rm -rf "$TASK_CLASSES"' EXIT HUP INT TERM
"$TASK_JDK/bin/javac" -d "$TASK_CLASSES" \
 "$TASK_ROOT"/scripts/test-support/android/annotation/*.java \
 "$TASK_ROOT"/scripts/test-support/android/content/*.java \
 "$TASK_ROOT"/scripts/test-support/android/graphics/*.java \
 "$TASK_ROOT"/app/src/main/java/com/androidhit/lumadrift/data/*.java \
 "$TASK_ROOT"/app/src/main/java/com/androidhit/lumadrift/engine/*.java \
 "$TASK_ROOT/app/src/main/java/com/androidhit/lumadrift/ui/PlaySceneLayout.java" \
 "$TASK_ROOT/app/src/main/java/com/androidhit/lumadrift/ui/VictoryLayout.java" \
 "$TASK_ROOT/scripts/RewardsCosmeticsV17Test.java" "$TASK_ROOT/scripts/FocusedJourneyV120Test.java"
"$TASK_JDK/bin/java" -Xmx768m -cp "$TASK_CLASSES" FocusedJourneyV120Test "${1:-$TASK_ROOT/docs/validation/v1.20/host/focused-journey.json}"
