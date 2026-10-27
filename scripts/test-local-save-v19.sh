#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-local-save-v19.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then task_java_bin="$JAVA_HOME/bin"
elif [[ -x /opt/homebrew/opt/openjdk@17/bin/javac ]]; then task_java_bin=/opt/homebrew/opt/openjdk@17/bin
else task_java_bin="$(dirname "$(command -v javac)")"; fi
task_output="${1:-$task_root/docs/validation/v1.9/local-save}"
"$task_java_bin/javac" -d "$task_build" \
    "$task_root"/scripts/test-support/android/annotation/*.java \
    "$task_root"/scripts/test-support/android/content/*.java \
    "$task_root"/scripts/test-support/android/graphics/*.java \
    "$task_root"/app/src/main/java/com/androidhit/lumadrift/data/*.java \
    "$task_root"/app/src/main/java/com/androidhit/lumadrift/engine/*.java \
    "$task_root"/app/src/main/java/com/androidhit/lumadrift/onboarding/*.java \
    "$task_root/scripts/RewardsCosmeticsV17Test.java" "$task_root/scripts/LocalSaveV19Test.java"
"$task_java_bin/java" -Xmx768m -cp "$task_build" LocalSaveV19Test "$task_output"
python3 - "$task_root" "$task_output" <<'PY'
import hashlib,json,pathlib,sys
root,out=map(pathlib.Path,sys.argv[1:])
names=['app/src/main/java/com/androidhit/lumadrift/data/GameStore.java','app/src/main/java/com/androidhit/lumadrift/data/LocalSaveTransactions.java','app/src/main/java/com/androidhit/lumadrift/data/MetaProgress.java','scripts/RewardsCosmeticsV17Test.java','scripts/LocalSaveV19Test.java','scripts/test-local-save-v19.sh']
(out/'source-inputs.json').write_text(json.dumps({'sha256':{name:hashlib.sha256((root/name).read_bytes()).hexdigest() for name in names}},indent=2)+'\n')
PY
