#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
task_out="$(mktemp -d "${TMPDIR:-/tmp}/luma122-verify.XXXXXX")"
trap 'rm -rf "$task_out"' EXIT
bash "$task_root/scripts/test-cognitive-lab.sh"
for task_trial in a b; do bash "$task_root/scripts/run-cognitive-lab-v122.sh" --smoke --output "$task_out/$task_trial"; done
python3 - "$task_out" <<'PY'
import csv,gzip,json,pathlib,sys,collections
base=pathlib.Path(sys.argv[1]);a=base/'a';b=base/'b'
files=[p.name for p in a.iterdir() if p.name!='provenance.json']
for name in files: assert (a/name).read_bytes()==(b/name).read_bytes(),name
summary=json.loads((a/'summary.json').read_text());assert summary['totals']['trajectories']==84
expected_scenarios=['control_v121','cause_immediate','cause_repeat','cause_fading','team_information','team_execution','v122_combined']
assert summary['config']['scenarios']==expected_scenarios==[row['id'] for row in summary['scenarios']], 'Protocol scenario metadata must be exact JSON arrays in execution order'
assert json.loads((a/'report.json').read_text())['summary']['config']['scenarios']==expected_scenarios
assert summary['config']['initialKnowledgeBonus']==0 and summary['config']['learningRateMultiplier']==1
records={}
for path in a.glob('*.csv.gz'):
 with gzip.open(path,'rt') as f: rows=list(csv.DictReader(f))
 assert rows and all(None not in row for row in rows),path.name
 records[path.name]=rows
blocks=collections.Counter(r['scenario'] for r in records['blocked-events.csv.gz'])
levels=collections.Counter();reasons=collections.Counter()
for row in records['level-results.csv.gz']:
 assert int(row['blocked'])==sum(int(row[k]) for k in ['closedGateBlocked','pieceBlocked','barrierBlocked'])
 levels[row['scenario']]+=int(row['blocked']);reasons[row['outcome']]+=1
assert levels==blocks
for r in records['day-results.csv.gz']:
 if r['stopReason']=='day_action_cap':assert int(r['decisions'])==120 and r['administrativeActionCap']=='true'
 if r['stopReason']=='repeated_budget_exhaustion':assert int(r['budgetExhaustedAttempts'])==3
for r in records['chapter-stage-results.csv.gz']:
 assert int(r['chargeAfter'])<=int(r['chargeBefore'])
 if r['policy']=='manual':assert int(r['automatedExits'])==int(r['skillUses'])==0
chapter=json.loads((a/'tactical-chapter.json').read_text());assert len(chapter['comparisons'])==18
assert all(r['runs']==12 for r in chapter['comparisons'])
for row in summary['scenarios']:
 m=row['mechanics'];assert row['totals']['blocked']==sum(m[k] for k in ['closedGateBlocked','pieceBlocked','barrierBlocked'])
 assert row['totals']['attempts']==sum(m['outcomes'].values())
print(json.dumps({'passed':True,'deterministicFiles':files,'trajectories':84,'exhaustiveBlockedReasons':True,'stopAttribution':True,'separateChapterConditions':18,'administrativeAndHorizonCensoringSeparate':True}))
PY
node --test "$task_root/public/lab/cognitive/tests/lab.test.mjs"
