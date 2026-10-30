#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-cognitive-test.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then
    task_java_bin="$JAVA_HOME/bin"
elif [[ -x /opt/homebrew/opt/openjdk@17/bin/javac ]]; then
    task_java_bin=/opt/homebrew/opt/openjdk@17/bin
else
    task_java_bin="$(dirname "$(command -v javac)")"
fi
"$task_java_bin/javac" -d "$task_build" \
 "$task_root"/scripts/test-support/android/annotation/*.java \
 "$task_root"/scripts/test-support/android/content/*.java \
 "$task_root"/scripts/test-support/android/graphics/*.java \
 "$task_root"/app/src/main/java/com/androidhit/lumadrift/engine/*.java \
 "$task_root"/app/src/main/java/com/androidhit/lumadrift/data/*.java \
 "$task_root"/scripts/cognitive_lab/*.java "$task_root"/scripts/cognitive_lab/tests/*.java
"$task_java_bin/java" -Xmx768m -cp "$task_build" cognitive_lab.CognitiveLabTest "$task_root"
for task_trial in a b; do
 "$task_java_bin/java" -Xmx768m -cp "$task_build" cognitive_lab.CognitiveLab --root "$task_root" --smoke --output "$task_build/$task_trial"
done
python3 - "$task_build" <<'PY'
import csv,gzip,hashlib,json,pathlib,sys
base=pathlib.Path(sys.argv[1]); files=['summary.json','profiles.json','traces.json','paired-comparisons.json','report.json','trajectories.csv.gz','level-results.csv.gz']
for name in files:
    assert (base/'a'/name).read_bytes()==(base/'b'/name).read_bytes(),f'Non-deterministic {name}'
summary=json.loads((base/'a'/'summary.json').read_text()); assert summary['totals']['trajectories']==36
assert len(summary['profileResults'])==18
assert len(json.loads((base/'a'/'profiles.json').read_text())['profiles'])==6
for name in ['trajectories.csv.gz','level-results.csv.gz']:
    with gzip.open(base/'a'/name,'rt') as source:
        rows=list(csv.DictReader(source)); assert rows and all(None not in row for row in rows),name
traces=json.loads((base/'a'/'traces.json').read_text())['traces']
assert {t['scenario'] for t in traces}=={'current_model','teaching_support','reward_muted'}
assert all('attempt' in t and 'decisionSeed' in t and 'presentMask' in t['observation'] for t in traces)
print(json.dumps({'passed':True,'deterministicPayloads':files,'mainTrajectories':36,'profileScenarioAggregates':18,'csvSchemasValid':True,'traceScenarios':3}))
PY
