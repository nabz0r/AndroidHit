#!/bin/sh
set -eu
TASK_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
TASK_JAVA_HOME=${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}
TASK_BUILD=$(mktemp -d "${TMPDIR:-/tmp}/luma-journey-world-v122.XXXXXX")
trap 'rm -rf "$TASK_BUILD"' EXIT HUP INT TERM
cd "$TASK_ROOT"
"$TASK_JAVA_HOME/bin/javac" -Xlint:all -d "$TASK_BUILD" \
    app/src/main/java/com/androidhit/lumadrift/ui/JourneyWorld122.java \
    app/src/main/java/com/androidhit/lumadrift/ui/JourneyCamera122.java scripts/JourneyWorld122Test.java
"$TASK_JAVA_HOME/bin/java" -cp "$TASK_BUILD" JourneyWorld122Test
TASK_ANDROID_JAR=${ANDROID_HOME:-$HOME/Library/Android/sdk}/platforms/android-36/android.jar
if [ -f "$TASK_ANDROID_JAR" ]; then
    "$TASK_JAVA_HOME/bin/javac" -Xlint:all -cp "$TASK_ANDROID_JAR:$TASK_BUILD" -d "$TASK_BUILD" \
        app/src/main/java/com/androidhit/lumadrift/ui/SagaMap.java \
        app/src/main/java/com/androidhit/lumadrift/ui/JourneyMapScene.java \
        app/src/main/java/com/androidhit/lumadrift/ui/JourneyWorldScene122.java
    echo 'JourneyWorldScene122: Android 36 Canvas compilation passed.'
else
    echo 'JourneyWorldScene122: Android 36 jar unavailable; renderer compilation not performed.'
fi
