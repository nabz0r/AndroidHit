#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-guardian-v19.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then task_java_bin="$JAVA_HOME/bin"
elif [[ -x /opt/homebrew/opt/openjdk@17/bin/javac ]]; then task_java_bin=/opt/homebrew/opt/openjdk@17/bin
else task_java_bin="$(dirname "$(command -v javac)")"; fi
"$task_java_bin/javac" -d "$task_build" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/engine/Direction.java" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/engine/GuardianCatalog.java" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/engine/GuardianEncounter.java" \
    "$task_root/scripts/GuardianV19Test.java"
"$task_java_bin/java" -Xmx768m -cp "$task_build" GuardianV19Test "${1:-$task_root/docs/validation/v1.9/journey/guardians}"
python3 - "$task_root" "${1:-$task_root/docs/validation/v1.9/journey/guardians}" <<'PY'
import hashlib,json,pathlib,sys
root,out=map(pathlib.Path,sys.argv[1:])
names=['app/src/main/java/com/androidhit/lumadrift/engine/GuardianCatalog.java','app/src/main/java/com/androidhit/lumadrift/engine/GuardianEncounter.java','app/src/main/java/com/androidhit/lumadrift/engine/Direction.java','scripts/GuardianV19Test.java','scripts/test-guardian-v19.sh']
(out/'source-inputs.json').write_text(json.dumps({'sha256':{name:hashlib.sha256((root/name).read_bytes()).hexdigest() for name in names}},indent=2)+'\n')
PY
