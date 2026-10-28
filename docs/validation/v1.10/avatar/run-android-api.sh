#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/../../../.." && pwd)"
task_report="$task_root/docs/validation/v1.10/avatar"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-portrait-v110-android.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then
    task_java_bin="$JAVA_HOME/bin"
elif [[ -x /tmp/luma-jdk21/jdk-21.0.13+9/Contents/Home/bin/javac ]]; then
    task_java_bin=/tmp/luma-jdk21/jdk-21.0.13+9/Contents/Home/bin
else
    task_java_bin="$(dirname "$(command -v javac)")"
fi
task_sdk="${ANDROID_HOME:-$HOME/Library/Android/sdk}/platforms/android-36/android.jar"
[[ -f "$task_sdk" ]] || { echo 'Android 36 platform not found.' >&2; exit 1; }
task_sources=(
    data/Catalog.java data/AvatarStyles.java data/AvatarGearCatalog.java
    data/AvatarLoadout.java data/CosmeticCatalog.java data/Strings.java data/V110Strings.java
    ui/AvatarStage.java ui/KawaiiArt.java ui/CompanionPortrait.java
)
task_arguments=()
for task_source in "${task_sources[@]}"; do
    task_arguments+=("$task_root/app/src/main/java/com/androidhit/lumadrift/$task_source")
done
"$task_java_bin/javac" -cp "$task_sdk" -d "$task_build" "${task_arguments[@]}"
python3 - "$task_report/android-api-compile.json" "$task_sdk" "$task_java_bin/javac" <<'PY'
import datetime, hashlib, json, pathlib, subprocess, sys
output, sdk, javac = map(pathlib.Path, sys.argv[1:])
report = {
    'passed': True,
    'retrieved_at_utc': datetime.datetime.now(datetime.timezone.utc).isoformat(),
    'compiler': subprocess.check_output([str(javac), '-version'], text=True).strip(),
    'android_platform': 36,
    'android_jar_sha256': hashlib.sha256(sdk.read_bytes()).hexdigest(),
    'source_files': 10,
    'scope': 'Standalone compilation against real Android API classes; no app build, APK, install or raster evidence.'
}
output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n')
print(json.dumps(report, ensure_ascii=False, indent=2))
PY
