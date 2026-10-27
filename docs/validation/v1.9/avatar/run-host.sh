#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/../../../.." && pwd)"
task_report="$task_root/docs/validation/v1.9/avatar"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-avatar-v19.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then
    task_java_bin="$JAVA_HOME/bin"
elif [[ -x /tmp/luma-jdk21/jdk-21.0.13+9/Contents/Home/bin/javac ]]; then
    task_java_bin=/tmp/luma-jdk21/jdk-21.0.13+9/Contents/Home/bin
else
    task_java_bin="$(dirname "$(command -v javac)")"
fi
"$task_java_bin/javac" -d "$task_build" \
    "$task_root/scripts/test-support/android/graphics/Color.java" \
    "$task_report"/host/android/graphics/*.java \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/data/Catalog.java" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/data/AvatarStyles.java" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/ui/AvatarStage.java" \
    "$task_report/AvatarStylesTest.java"
"$task_java_bin/java" -cp "$task_build" AvatarStylesTest "$task_report"
