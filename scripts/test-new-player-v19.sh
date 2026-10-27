#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-new-player-v19.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then task_java_bin="$JAVA_HOME/bin"
elif [[ -x /opt/homebrew/opt/openjdk@17/bin/javac ]]; then task_java_bin=/opt/homebrew/opt/openjdk@17/bin
else task_java_bin="$(dirname "$(command -v javac)")"; fi
task_label="${1:-candidate}"
task_output="${2:-$task_root/docs/validation/v1.9/journey/$task_label}"
mkdir -p "$task_output"
task_source_root="$task_build/source"
task_source_ref="${3:-}"
mkdir -p "$task_source_root"
if [[ -n "$task_source_ref" ]]; then
    git -C "$task_root" archive --format=tar "$task_source_ref" app/src/main/java | tar -xf - -C "$task_source_root"
else
    mkdir -p "$task_source_root/app/src/main"
    cp -R "$task_root/app/src/main/java" "$task_source_root/app/src/main/java"
fi
python3 - "$task_root/docs/research/2026-new-player/profile-anchors.json" "$task_output/profile-inputs.tsv" <<'PY'
import json,pathlib,sys
source=json.loads(pathlib.Path(sys.argv[1]).read_text())
rows=['id\toff_plan\tblocked_probe\thelp_after\tsession_min\tsession_max\tlatency_min\tlatency_max\tretry_limit\ttutorial\tanchors']
for p in source['profiles']:
 a=p['assumed_parameters'];rows.append('\t'.join(map(str,[p['id'],a['off_plan_rate'],a['blocked_probe_rate'],a['help_after_unproductive_actions'],*a['session_window_minutes'],*a['action_latency_seconds'],a['retry_limit'],a['tutorial'],';'.join(p['anchors'])])))
pathlib.Path(sys.argv[2]).write_text('\n'.join(rows)+'\n')
PY
python3 - "$task_source_root/app/src/main/java/com/androidhit/lumadrift/LumaView.java" "$task_build/BaselineV19Score.java" "$task_output/baseline-score-extraction.json" <<'PY'
import hashlib,json,pathlib,re,sys
view=pathlib.Path(sys.argv[1]);text=view.read_text()
match=re.search(r'stopTimer\(\);\s*resultStars\s*=\s*([^;]+);',text)
assert match,'Missing actual finishLevel scoring expression'
expression=match.group(1)
legacy=expression.startswith('hintsUsed==0&&moveBudget.extensions()==0?')
body='return '+expression+';' if legacy else 'throw new AssertionError("Current source must use the public JourneyExperience API");'
pathlib.Path(sys.argv[2]).write_text('import com.androidhit.lumadrift.engine.MoveBudget; final class BaselineV19Score { static int stars(int hintsUsed, MoveBudget moveBudget) { '+body+' } }\n')
pathlib.Path(sys.argv[3]).write_text(json.dumps({'legacy_expression_compiled':legacy,'expression':expression,'source_sha256':hashlib.sha256(view.read_bytes()).hexdigest(),'method':'Exact expression extracted from application finishLevel, not an independently reimplemented scoring formula.'},indent=2)+'\n')
PY
"$task_java_bin/javac" -d "$task_build" \
    "$task_root"/scripts/test-support/android/annotation/*.java \
    "$task_root"/scripts/test-support/android/content/*.java \
    "$task_root"/scripts/test-support/android/graphics/*.java \
    "$task_source_root"/app/src/main/java/com/androidhit/lumadrift/data/*.java \
    "$task_source_root"/app/src/main/java/com/androidhit/lumadrift/engine/*.java \
    "$task_source_root"/app/src/main/java/com/androidhit/lumadrift/onboarding/*.java \
    "$task_root/scripts/NewPlayerV19Audit.java" "$task_build/BaselineV19Score.java"
"$task_java_bin/java" -Xmx768m -cp "$task_build" NewPlayerV19Audit "$task_output" "$task_label"
python3 - "$task_root" "$task_output" "$task_label" "$task_source_root" "$task_source_ref" <<'PY'
import hashlib,json,pathlib,subprocess,sys
root,out,label=pathlib.Path(sys.argv[1]),pathlib.Path(sys.argv[2]),sys.argv[3]
source,ref=pathlib.Path(sys.argv[4]),sys.argv[5]
paths=[p for folder in ('app/src/main/java/com/androidhit/lumadrift/engine','app/src/main/java/com/androidhit/lumadrift/data','app/src/main/java/com/androidhit/lumadrift/onboarding') for p in (source/folder).glob('*.java')]
paths += [source/'app/src/main/java/com/androidhit/lumadrift/LumaView.java']
manifest={str(p.relative_to(source)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(paths)}
for name in ('scripts/NewPlayerV19Audit.java','scripts/test-new-player-v19.sh','docs/research/2026-new-player/profile-anchors.json'):
 manifest[name]=hashlib.sha256((root/name).read_bytes()).hexdigest()
(out/'source-inputs.json').write_text(json.dumps({'label':label,'source_ref':ref or 'working-tree','head':subprocess.check_output(['git','rev-parse',ref or 'HEAD'],cwd=root,text=True).strip(),'sha256':manifest},indent=2)+'\n')
PY
