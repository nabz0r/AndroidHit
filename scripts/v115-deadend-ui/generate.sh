#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
LEVEL=${1:-31}
OUTPUT=${2:-"$ROOT/docs/validation/v1.15/deadend-fixture.json"}
TASK_JAVA_HOME=${JAVA_HOME:-/opt/homebrew/opt/openjdk@17}
TASK_TEMP=$(mktemp -d "${TMPDIR:-/tmp}/luma-v115-deadend.XXXXXX")
trap 'rm -rf "$TASK_TEMP"' EXIT HUP INT TERM
find "$ROOT/app/src/main/java/com/androidhit/lumadrift/engine" -name '*.java' -print > "$TASK_TEMP/sources.txt"
"$TASK_JAVA_HOME/bin/javac" -encoding UTF-8 -d "$TASK_TEMP/classes" @"$TASK_TEMP/sources.txt" "$ROOT/scripts/v115-deadend-ui/DeadEndFixture.java"
"$TASK_JAVA_HOME/bin/java" -cp "$TASK_TEMP/classes" DeadEndFixture "$LEVEL" > "$TASK_TEMP/fixture.json"
python3 - "$ROOT" "$TASK_TEMP/fixture.json" "$OUTPUT" <<'PY'
import hashlib, json, pathlib, sys
root, source, destination = map(pathlib.Path, sys.argv[1:])
data = json.loads(source.read_text())
inputs = [root / 'scripts/v115-deadend-ui/DeadEndFixture.java', root / 'scripts/v115-deadend-ui/generate.sh']
inputs += sorted((root / 'app/src/main/java/com/androidhit/lumadrift/engine').glob('*.java'))
data['sourceSha256'] = {str(p.relative_to(root)): hashlib.sha256(p.read_bytes()).hexdigest() for p in inputs}
destination.parent.mkdir(parents=True, exist_ok=True)
destination.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n')
print(f"PASS U{data['levelNumber']}: {len(data['steps'])} legal exit(s), dead end, one undo soluble, full recovery replayed. {destination}")
print(f"Code: {data['code']}. Actions: {[s['actionId'] for s in data['steps']]}. Gates: {[s['after']['gateMask'] for s in data['steps']]}.")
PY
