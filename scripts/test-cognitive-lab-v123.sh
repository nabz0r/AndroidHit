#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma123-verify.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then task_java_bin="$JAVA_HOME/bin"; elif [[ -x /opt/homebrew/opt/openjdk@17/bin/javac ]]; then task_java_bin=/opt/homebrew/opt/openjdk@17/bin; else task_java_bin="$(dirname "$(command -v javac)")"; fi
"$task_java_bin/javac" -d "$task_build" "$task_root"/scripts/test-support/android/graphics/Color.java "$task_root"/app/src/main/java/com/androidhit/lumadrift/engine/*.java "$task_root/app/src/main/java/com/androidhit/lumadrift/data/EconomyCurve121.java" "$task_root/app/src/main/java/com/androidhit/lumadrift/data/Catalog.java" "$task_root/app/src/main/java/com/androidhit/lumadrift/data/CompanionBattle.java" "$task_root/app/src/main/java/com/androidhit/lumadrift/data/CompanionBattleAction.java" "$task_root/app/src/main/java/com/androidhit/lumadrift/data/TacticalChapterCatalog.java" "$task_root/app/src/main/java/com/androidhit/lumadrift/data/TacticalChapterRun.java" "$task_root/app/src/main/java/com/androidhit/lumadrift/data/TacticalChapterAction.java" "$task_root"/scripts/cognitive_lab/*.java "$task_root/scripts/cognitive_lab/tests/CognitiveLab123Test.java"
"$task_java_bin/java" -Xmx768m -cp "$task_build" cognitive_lab.CognitiveLab123Test "$task_root"
for task_trial in a b; do
 "$task_java_bin/java" -Xmx768m -cp "$task_build" cognitive_lab.CognitiveLab123 --root "$task_root" --smoke --output "$task_build/$task_trial"
 python3 "$task_root/scripts/verify-cognitive-lab-v123.py" --data "$task_build/$task_trial" --require-current-source
done
python3 - "$task_build" <<'PY'
import pathlib,sys,json
p=pathlib.Path(sys.argv[1]);files=sorted(x.name for x in (p/'a').iterdir() if x.name!='provenance.json')
for name in files:assert (p/'a'/name).read_bytes()==(p/'b'/name).read_bytes(),name
s=json.loads((p/'a/summary.json').read_text());assert s['totals']['trajectories']==48
print(json.dumps({'status':'PASS','deterministicPayloads':files,'smokeTrajectoriesPerRun':48,'horizonRegression':True,'sourceBound':True}))
PY
