#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_build="$(mktemp -d "${TMPDIR:-/tmp}/luma-prism-story-v110.XXXXXX")"
trap 'rm -rf "$task_build"' EXIT
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then task_java_bin="$JAVA_HOME/bin"
elif [[ -x /opt/homebrew/opt/openjdk@17/bin/javac ]]; then task_java_bin=/opt/homebrew/opt/openjdk@17/bin
else task_java_bin="$(dirname "$(command -v javac)")"; fi
task_output="${1:-$task_root/docs/validation/v1.10/prism-story}"
"$task_java_bin/javac" -d "$task_build" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/engine/PrismStory.java" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/engine/GuardianCatalog.java" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/engine/GuardianEncounter.java" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/engine/Direction.java" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/data/Strings.java" \
    "$task_root/app/src/main/java/com/androidhit/lumadrift/data/PrismStoryStrings.java" \
    "$task_root/scripts/PrismStoryV110Test.java"
"$task_java_bin/java" -Xmx256m -cp "$task_build" PrismStoryV110Test "$task_output"
python3 - "$task_root" "$task_output" <<'PY'
from pathlib import Path
import csv,hashlib,json,subprocess,sys
root,out=map(Path,sys.argv[1:]);baseline='ec57167'
names=['app/src/main/java/com/androidhit/lumadrift/engine/PrismStory.java','app/src/main/java/com/androidhit/lumadrift/data/PrismStoryStrings.java','scripts/PrismStoryV110Test.java','scripts/test-prism-story-v110.sh']
frozen=['app/src/main/java/com/androidhit/lumadrift/engine/'+name+'.java' for name in ['StrategyCatalog','StrategyV4Layouts','MoveBudget','Board','ChallengeCode','GuardianCatalog','GuardianEncounter']]
fingerprints={}
for name in frozen:
 before=subprocess.check_output(['git','show',baseline+':'+name],cwd=root);after=(root/name).read_bytes()
 fingerprints[name]={'baseline_sha256':hashlib.sha256(before).hexdigest(),'current_sha256':hashlib.sha256(after).hexdigest(),'unchanged':before==after}
 assert before==after, 'Frozen puzzle source changed: '+name
(out/'source-inputs.json').write_text(json.dumps({'baseline_commit':subprocess.check_output(['git','rev-parse',baseline],cwd=root,text=True).strip(),'story_sha256':{n:hashlib.sha256((root/n).read_bytes()).hexdigest() for n in names},'frozen_puzzle_sources':fingerprints},indent=2)+'\n')
p=root/'docs/validation/v1.9/journey/candidate/levels-1-100.csv';levels={int(r['level']):r for r in csv.DictReader(p.open())}
assert levels[22]['structural_score']=='17' and levels[23]['structural_score']=='85'
summary={'source':'docs/validation/v1.9/journey/candidate/levels-1-100.csv','source_sha256':hashlib.sha256(p.read_bytes()).hexdigest(),'L22':levels[22],'L23':levels[23],'interpretation':'Existing measured structural score and choices, not a human fail-rate or retention curve. Geometry and budgets remain frozen in 1.10.','next_study':'Observe switch preview prediction, gate ordering, blocked-probe recovery, Guardian4to5 action comprehension and comfort. Mixed-axis Guardian content or additional training requires a separately versioned future proposal; neither is implemented here.'}
(out/'unchanged-structural-backlog.json').write_text(json.dumps(summary,indent=2)+'\n')
PY
