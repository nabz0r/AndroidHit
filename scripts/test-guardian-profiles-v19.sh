#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-guardian-profiles-v19.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then task_java_bin="$JAVA_HOME/bin"
elif [[ -x /opt/homebrew/opt/openjdk@17/bin/javac ]]; then task_java_bin=/opt/homebrew/opt/openjdk@17/bin
else task_java_bin="$(dirname "$(command -v javac)")"; fi
task_output="${1:-$task_root/docs/validation/v1.9/journey/guardian-profiles}"
mkdir -p "$task_output" "$task_build/source/app/src/main"
cp -R "$task_root/app/src/main/java" "$task_build/source/app/src/main/java"
python3 - "$task_root/docs/research/2026-new-player/profile-anchors.json" "$task_output/profile-inputs.tsv" <<'PY'
import json,pathlib,sys
source=json.loads(pathlib.Path(sys.argv[1]).read_text())
rows=['id\toff_plan\tblocked_probe\thelp_after\tsession_min\tsession_max\tlatency_min\tlatency_max\tretry_limit\ttutorial\tanchors']
for p in source['profiles']:
 a=p['assumed_parameters'];rows.append('\t'.join(map(str,[p['id'],a['off_plan_rate'],a['blocked_probe_rate'],a['help_after_unproductive_actions'],*a['session_window_minutes'],*a['action_latency_seconds'],a['retry_limit'],a['tutorial'],';'.join(p['anchors'])])))
pathlib.Path(sys.argv[2]).write_text('\n'.join(rows)+'\n')
PY
"$task_java_bin/javac" -d "$task_build" \
    "$task_root"/scripts/test-support/android/annotation/*.java \
    "$task_root"/scripts/test-support/android/content/*.java \
    "$task_root"/scripts/test-support/android/graphics/*.java \
    "$task_build"/source/app/src/main/java/com/androidhit/lumadrift/data/*.java \
    "$task_build"/source/app/src/main/java/com/androidhit/lumadrift/engine/*.java \
    "$task_build"/source/app/src/main/java/com/androidhit/lumadrift/onboarding/*.java \
    "$task_root/scripts/RewardsCosmeticsV17Test.java" "$task_root/scripts/GuardianProfilesV19Test.java"
"$task_java_bin/java" -Xmx768m -cp "$task_build" GuardianProfilesV19Test "$task_output"
python3 - "$task_root" "$task_build/source" "$task_output" <<'PY'
import hashlib,json,pathlib,sys
root,source,out=map(pathlib.Path,sys.argv[1:])
paths=[p for folder in ('data','engine','onboarding') for p in (source/'app/src/main/java/com/androidhit/lumadrift'/folder).glob('*.java')]
manifest={str(p.relative_to(source)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(paths)}
for name in ['scripts/GuardianProfilesV19Test.java','scripts/test-guardian-profiles-v19.sh','scripts/RewardsCosmeticsV17Test.java','docs/research/2026-new-player/profile-anchors.json']:
 manifest[name]=hashlib.sha256((root/name).read_bytes()).hexdigest()
(out/'source-inputs.json').write_text(json.dumps({'method':'Application Java sources copied before compilation; no app source or existing evidence changed.','sha256':manifest},indent=2)+'\n')
reports={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(out.glob('*')) if p.is_file() and p.name!='report-hashes.json'}
(out/'report-hashes.json').write_text(json.dumps({'sha256':reports},indent=2)+'\n')
PY
