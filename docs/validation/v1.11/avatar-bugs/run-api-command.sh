#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/../../../.." && pwd)"
task_dir="$task_root/docs/validation/v1.11/avatar-bugs"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-avatar-contract.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
task_java=/tmp/luma-jdk21/jdk-21.0.13+9/Contents/Home/bin
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then task_java="$JAVA_HOME/bin"; fi
task_sources=()
for task_source in data/Catalog.java data/AvatarStyles.java data/AvatarGearCatalog.java data/AvatarLoadout.java data/CosmeticCatalog.java data/Strings.java data/V110Strings.java data/V111Strings.java ui/AvatarStage.java ui/KawaiiArt.java ui/CompanionPortrait.java; do
    task_sources+=("$task_root/app/src/main/java/com/androidhit/lumadrift/$task_source")
done
task_sdk="${ANDROID_HOME:-$HOME/Library/Android/sdk}/platforms/android-36/android.jar"
"$task_java/javac" -cp "$task_sdk" -d "$task_build" "${task_sources[@]}"
printf '%s\n' 'PASS standalone compilation against real Android36 API; no app build/install.' > "$task_dir/android-api-compile.txt"
mkdir -p "$task_dir/command-regression"
"$task_java/javac" -d "$task_build" "$task_root"/docs/validation/v1.10/avatar/host/android/graphics/*.java "${task_sources[@]}" "$task_root/docs/validation/v1.10/avatar/CompanionPortraitTest.java" "$task_dir/V111StringsTest.java"
"$task_java/java" -cp "$task_build" CompanionPortraitTest "$task_dir/command-regression" > "$task_dir/command-regression.txt"
"$task_java/java" -cp "$task_build" V111StringsTest "$task_dir" > "$task_dir/v111-strings.txt"
