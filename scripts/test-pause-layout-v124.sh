#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
task_output="$(mktemp -d /tmp/luma124-pause-layout.XXXXXX)"
trap 'rm -rf "$task_output"' EXIT
task_java="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}"
"$task_java/bin/javac" -d "$task_output" app/src/main/java/com/androidhit/lumadrift/ui/PauseLayout124.java app/src/main/java/com/androidhit/lumadrift/ui/CompanionSheetLayout123.java app/src/main/java/com/androidhit/lumadrift/data/Clarity124Strings.java scripts/PauseLayout124Test.java
"$task_java/bin/java" -cp "$task_output" PauseLayout124Test
