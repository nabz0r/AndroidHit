#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-audio.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then
    task_java_bin="$JAVA_HOME/bin"
elif [[ -x /opt/homebrew/opt/openjdk@17/bin/javac ]]; then
    task_java_bin=/opt/homebrew/opt/openjdk@17/bin
else
    task_java_bin="$(dirname "$(command -v javac)")"
fi
find "$task_root/scripts/test-support/audio" -name '*.java' -print0 | xargs -0 "$task_java_bin/javac" \
    -d "$task_build" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/audio/Synth.java" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/audio/AudioEngine.java" \
    "$task_root/scripts/AudioTest.java" "$task_root/scripts/AudioEngineTest.java"
"$task_java_bin/java" -cp "$task_build" com.androidhit.lumadrift.audio.AudioEngineTest
"$task_java_bin/java" -cp "$task_build" com.androidhit.lumadrift.audio.AudioTest "$@"
