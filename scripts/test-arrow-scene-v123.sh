#!/bin/sh
set -eu
task_root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
task_sdk=${ANDROID_HOME:-"$HOME/Library/Android/sdk"}
task_jar="$task_sdk/platforms/android-36/android.jar"
task_java=${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}
task_classes=$(mktemp -d "${TMPDIR:-/tmp}/luma-arrow123.XXXXXX")
trap 'rm -rf "$task_classes"' EXIT HUP INT TERM
"$task_java/bin/javac" -cp "$task_jar" -sourcepath "$task_root/app/src/main/java" -d "$task_classes" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/engine/Direction.java" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/ui/ArrowScene123.java" \
    "$task_root/scripts/ArrowSceneV123GeometryTest.java"
"$task_java/bin/java" -cp "$task_classes:$task_jar" ArrowSceneV123GeometryTest
