#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/../.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-upgrade-fixture.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then task_java_bin="$JAVA_HOME/bin"
elif [[ -x /opt/homebrew/opt/openjdk@17/bin/javac ]]; then task_java_bin=/opt/homebrew/opt/openjdk@17/bin
else task_java_bin="$(dirname "$(command -v javac)")"; fi
"$task_java_bin/javac" -d "$task_build/classes" "$task_root"/app/src/main/java/com/androidhit/lumadrift/engine/*.java "$task_root/scripts/v114-ui-test/LearningUiFixture.java"
"$task_java_bin/java" -cp "$task_build/classes" LearningUiFixture "$task_build/fixtures" 15
cp "$task_build/fixtures/n15.json" "$task_root/scripts/v115-upgrade-ui/assets/fixtures/n15.json"
cd "$task_root"
./gradlew -I scripts/v115-upgrade-ui/init.gradle :app:assembleLabReleaseAndroidTest
