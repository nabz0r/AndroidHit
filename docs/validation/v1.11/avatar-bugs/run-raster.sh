#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/../../../.." && pwd)"
task_dir="$task_root/docs/validation/v1.11/avatar-bugs"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-avatar-raster.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
task_java=/tmp/luma-jdk21/jdk-21.0.13+9/Contents/Home/bin
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then task_java="$JAVA_HOME/bin"; fi
task_sources=()
task_mode="${1:-candidate}"
case "$task_mode" in baseline|candidate|contract) ;; *) printf '%s\n' 'Expected baseline, candidate or contract' >&2; exit 2 ;; esac
task_baseline=10c78c7cfead1c20b7fb2c7636273c21c11d2f56
for task_source in data/Catalog.java data/AvatarStyles.java data/AvatarGearCatalog.java data/AvatarLoadout.java data/CosmeticCatalog.java ui/AvatarStage.java ui/KawaiiArt.java ui/CompanionPortrait.java; do
    task_path="app/src/main/java/com/androidhit/lumadrift/$task_source"
    if [[ "$task_mode" == baseline ]]; then
        mkdir -p "$task_build/source/$(dirname "$task_source")"
        git -C "$task_root" show "$task_baseline:$task_path" > "$task_build/source/$task_source"
        task_sources+=("$task_build/source/$task_source")
    else
        task_sources+=("$task_root/$task_path")
    fi
done
"$task_java/javac" -d "$task_build" "$task_dir"/host/android/graphics/*.java "${task_sources[@]}" "$task_dir/RasterAudit.java" "$task_dir/RasterContract.java"
if [[ "$task_mode" == contract ]]; then
    "$task_java/java" -Djava.awt.headless=true -cp "$task_build" RasterContract "$task_dir"
else
    "$task_java/java" -Djava.awt.headless=true -cp "$task_build" RasterAudit "$task_dir" "$task_mode"
fi
