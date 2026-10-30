#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-journey-clarity-v123.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
task_java="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}"
task_output="${1:-$task_root/docs/validation/v1.23/core-model}"
"$task_java/bin/javac" -Xlint:all -d "$task_build" \
 "$task_root"/scripts/test-support/android/annotation/*.java \
 "$task_root"/scripts/test-support/android/content/*.java \
 "$task_root"/scripts/test-support/android/graphics/*.java \
 "$task_root"/app/src/main/java/com/androidhit/lumadrift/engine/*.java \
 "$task_root"/app/src/main/java/com/androidhit/lumadrift/data/*.java \
 "$task_root"/app/src/main/java/com/androidhit/lumadrift/progression/*.java \
 "$task_root/scripts/JourneyClarityV123Test.java" "$task_root/scripts/WardrobeLookV123Test.java"
"$task_java/bin/java" -Xmx512m -cp "$task_build" JourneyClarityV123Test "$task_output/journey.json"
"$task_java/bin/java" -Xmx512m -cp "$task_build" WardrobeLookV123Test "$task_output/wardrobe.json"
python3 - "$task_root" "$task_output" <<'PY'
import hashlib,json,pathlib,sys
root,out=map(pathlib.Path,sys.argv[1:])
paths=[]
for base in ['app/src/main/java/com/androidhit/lumadrift/engine','app/src/main/java/com/androidhit/lumadrift/data','app/src/main/java/com/androidhit/lumadrift/progression','scripts/test-support/android']:
    paths.extend(p for p in (root/base).rglob('*.java'))
paths.extend(root/p for p in ['scripts/JourneyClarityV123Test.java','scripts/WardrobeLookV123Test.java','scripts/test-journey-clarity-v123.sh'])
record={'scope':'Host compilation inputs; no Android/UI/art qualification','sha256':{str(p.relative_to(root)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(set(paths))}}
(out/'source-inputs.json').write_text(json.dumps(record,indent=2)+'\n')
PY
