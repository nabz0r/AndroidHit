#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-audio-scenes-v117.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then task_java_bin="$JAVA_HOME/bin"
elif [[ -x /opt/homebrew/opt/openjdk@17/bin/javac ]]; then task_java_bin=/opt/homebrew/opt/openjdk@17/bin
else task_java_bin="$(dirname "$(command -v javac)")"; fi
cd "$task_root"
task_report="${1:-docs/validation/v1.17/audio/scenes}"
mkdir -p "$task_report"
"$task_java_bin/javac" -Xlint:all -d "$task_build" \
    scripts/test-support/audio/android/content/*.java \
    scripts/test-support/audio/android/media/*.java \
    scripts/test-support/audio/android/os/*.java \
    scripts/test-support/audio/android/util/*.java \
    app/src/main/java/com/androidhit/lumadrift/audio/Synth.java \
    app/src/main/java/com/androidhit/lumadrift/audio/AudioEngine.java \
    scripts/AudioSceneV117Test.java
"$task_java_bin/java" -Xmx256m -cp "$task_build" com.androidhit.lumadrift.audio.AudioSceneV117Test "$task_report" "${@:2}"
