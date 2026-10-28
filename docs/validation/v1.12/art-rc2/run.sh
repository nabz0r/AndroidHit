#!/usr/bin/env bash
set -euo pipefail
task_dir="$(cd "$(dirname "$0")" && pwd)"
task_root="$(cd "$task_dir/../../../.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-v112-art-build.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
task_out="${1:-$(mktemp -d "${TMPDIR:-/tmp}/luma-v112-art-output.XXXXXX")}"
mkdir -p "$task_out"
task_java="${JAVA_HOME:-/tmp/luma-jdk21/jdk-21.0.13+9/Contents/Home}/bin"
if [[ ! -x "$task_java/javac" ]]; then task_java="$(dirname "$(command -v javac)")"; fi
"$task_java/javac" -d "$task_build" "$task_dir"/host/android/graphics/*.java "$task_root/app/src/main/java/com/androidhit/lumadrift/ui/ShopIllustrations.java" "$task_dir/ArtRasterTest.java"
"$task_java/java" -Djava.awt.headless=true -cp "$task_build" ArtRasterTest "$task_out"
printf 'Outputs: %s\n' "$task_out"
