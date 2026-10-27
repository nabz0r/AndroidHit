#!/bin/sh
set -eu
TASK_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
TASK_JAVA_HOME=${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}
TASK_BUILD=$(mktemp -d "${TMPDIR:-/tmp}/luma-audio-biomes-v17.XXXXXX")
trap 'rm -rf "$TASK_BUILD"' EXIT HUP INT TERM
cd "$TASK_ROOT"
TASK_REPORT=${1:-docs/validation/v1.7/audio}
mkdir -p "$TASK_REPORT"
find scripts/test-support/audio -name '*.java' -print0 | xargs -0 "$TASK_JAVA_HOME/bin/javac" \
    -Xlint:all -d "$TASK_BUILD" \
    app/src/main/java/com/androidhit/lumadrift/audio/Synth.java \
    app/src/main/java/com/androidhit/lumadrift/audio/AudioEngine.java \
    scripts/AudioEngineTest.java scripts/AudioTest.java scripts/AudioBiomeEngineTest.java scripts/AudioBiomeTest.java
"$TASK_JAVA_HOME/bin/java" -cp "$TASK_BUILD" com.androidhit.lumadrift.audio.AudioEngineTest
"$TASK_JAVA_HOME/bin/java" -cp "$TASK_BUILD" com.androidhit.lumadrift.audio.AudioTest "$TASK_REPORT"
"$TASK_JAVA_HOME/bin/java" -cp "$TASK_BUILD" com.androidhit.lumadrift.audio.AudioBiomeEngineTest
"$TASK_JAVA_HOME/bin/java" -cp "$TASK_BUILD" com.androidhit.lumadrift.audio.AudioBiomeTest "$TASK_REPORT"
