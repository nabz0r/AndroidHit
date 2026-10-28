#!/usr/bin/env bash
set -euo pipefail
task_dir="$(cd "$(dirname "$0")" && pwd)"
task_root="$(cd "$task_dir/../../../.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-v112-booster-baseline.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
mkdir -p "$task_build/source"
cp "$task_dir/baseline-GameStore.java" "$task_build/source/GameStore.java"
task_java="${JAVA_HOME:-/tmp/luma-jdk21/jdk-21.0.13+9/Contents/Home}/bin"
if [[ ! -x "$task_java/javac" ]]; then task_java="$(dirname "$(command -v javac)")"; fi
task_sources=()
for task_source in "$task_root"/app/src/main/java/com/androidhit/lumadrift/data/*.java; do
    if [[ "$(basename "$task_source")" != GameStore.java ]]; then task_sources+=("$task_source"); fi
done
"$task_java/javac" -d "$task_build/classes" "$task_root"/scripts/test-support/android/annotation/*.java "$task_root"/scripts/test-support/android/content/*.java "$task_root"/scripts/test-support/android/graphics/*.java "${task_sources[@]}" "$task_build/source/GameStore.java" "$task_root/scripts/RewardsCosmeticsV17Test.java" "$task_dir/BoosterCommitObservation.java"
"$task_java/java" -cp "$task_build/classes" BoosterCommitObservation "${1:-$task_build/baseline-observation.json}"
