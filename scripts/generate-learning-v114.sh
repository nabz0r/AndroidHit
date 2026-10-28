#!/bin/sh
set -eu
TASK_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
TASK_JDK=${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}
TASK_CLASSES=$(mktemp -d "${TMPDIR:-/tmp}/luma-learning-author.XXXXXX")
trap 'rm -rf "$TASK_CLASSES"' EXIT HUP INT TERM
TASK_ENGINE=$(find "$TASK_ROOT/app/src/main/java/com/androidhit/lumadrift/engine" -name '*.java' ! -name 'LearningLayouts.java')
mkdir -p "$TASK_CLASSES/bootstrap"
printf '%s\n' 'package com.androidhit.lumadrift.engine; final class LearningLayouts { static final String[] JOURNEY=new String[1000]; }' > "$TASK_CLASSES/bootstrap/LearningLayouts.java"
"$TASK_JDK/bin/javac" -Xlint:all -d "$TASK_CLASSES" $TASK_ENGINE "$TASK_CLASSES/bootstrap/LearningLayouts.java" "$TASK_ROOT/scripts/GenerateStrategyV4.java" "$TASK_ROOT/scripts/GenerateLearningV114.java"
"$TASK_JDK/bin/java" -Xmx768m -cp "$TASK_CLASSES" GenerateLearningV114 "$TASK_ROOT"
