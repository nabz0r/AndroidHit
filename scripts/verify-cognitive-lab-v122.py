#!/usr/bin/env python3
"""Verify complete versioned exports and exact denominator/conservation identities."""
import csv,gzip,json,pathlib,hashlib,collections
ROOT=pathlib.Path(__file__).resolve().parents[1];D=ROOT/'docs/validation/cognitive-lab-v122/latest'
def records(name):
 with gzip.open(D/name,'rt') as stream:
  for row in csv.DictReader(stream):
   assert None not in row,(name,'CSV schema mismatch')
   yield row
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
s=json.loads((D/'summary.json').read_text());p=json.loads((D/'paired-comparisons.json').read_text());pr=json.loads((D/'provenance.json').read_text())
for name,value in pr['outputs'].items():assert sha(D/name)==value['sha256'],name
assert s['config']['scenarios']==['control_v121','cause_immediate','cause_repeat','cause_fading','team_information','team_execution','v122_combined']==[r['id'] for r in s['scenarios']], 'Exact protocol scenario list required'
scenarios={r['id']:r for r in s['scenarios']};main=collections.Counter();attempts=collections.Counter();blocks=collections.Counter();days=collections.Counter();outcomes=collections.defaultdict(collections.Counter);level_blocks=collections.Counter();maxlevel=collections.defaultdict(int)
for r in records('trajectories.csv.gz'):
 scenario=r['scenario'];main[scenario]+=1
 assert int(r['coinsEnd'])==180+int(r['coinsEarned'])-int(r['coinsSpent'])
 assert int(r['blocked'])==sum(int(r[k]) for k in ['closedGateBlocked','pieceBlocked','barrierBlocked'])
 assert int(r['attempts'])==sum(json.loads(r['outcomes']).values())
for r in records('level-results.csv.gz'):
 scenario=r['scenario'];attempts[scenario]+=1;outcomes[scenario][r['outcome']]+=1;level_blocks[scenario]+=int(r['blocked']);maxlevel[scenario]=max(maxlevel[scenario],int(r['level']))
 assert int(r['exits'])==int(r['manualExits'])+int(r['automatedExits'])
 assert int(r['blocked'])==sum(int(r[k]) for k in ['closedGateBlocked','pieceBlocked','barrierBlocked'])
for r in records('blocked-events.csv.gz'):
 blocks[r['scenario']]+=1;assert r['blockedReason'] in ['CLOSED_GATE','BLOCKED_PATH','LOCKED_BARRIER']
for r in records('day-results.csv.gz'):
 days[r['scenario']]+=1
 if r['stopReason']=='day_action_cap':assert int(r['decisions'])==120 and r['administrativeActionCap']=='true'
 if r['stopReason']=='repeated_budget_exhaustion':assert int(r['budgetExhaustedAttempts'])==3
for scenario,row in scenarios.items():
 assert main[scenario]==3000 and days[scenario]==24000
 assert attempts[scenario]==row['totals']['attempts']
 assert blocks[scenario]==level_blocks[scenario]==row['totals']['blocked']
 assert dict(outcomes[scenario])==row['mechanics']['outcomes']
 assert maxlevel[scenario]==row['mechanics']['highestAttemptedLevel']
for row in p['transferWithoutCues']['scenarios']:assert sum(row['outcomes'].values())==row['probes']==12000 and 'unknown' not in row['outcomes']
previous={};stage_count=0
for r in records('chapter-stage-results.csv.gz'):
 stage_count+=1;key=tuple(r[k] for k in ['profileId','repetition','episode','capacity','policy']);stage=int(r['stage']);before=int(r['chargeBefore']);after=int(r['chargeAfter'])
 assert after<=before and after>=0
 if key in previous:assert stage==previous[key][0]+1 and before==previous[key][1]
 else:assert stage==0 and before==int(r['capacity'])
 previous[key]=(stage,after)
 if r['policy']=='manual':assert int(r['automatedExits'])==int(r['skillUses'])==0
chapter_rows=sum(1 for _ in records('chapter-results.csv.gz'));assert chapter_rows==54000
old=json.loads((ROOT/'public/lab/cognitive/data/summary.json').read_text());old_control=old['scenarios'][0];control=scenarios['control_v121']
assert old_control['totals']==control['totals']
receipt={'schemaVersion':1,'passed':True,'modelVersion':s['config']['modelVersion'],'campaignTrajectories':sum(main.values()),'attemptRows':sum(attempts.values()),'blockedEventRows':sum(blocks.values()),'configuredDayRows':sum(days.values()),'chapterRuns':chapter_rows,'chapterStageRows':stage_count,'cueFreeProbes':sum(r['probes'] for r in p['transferWithoutCues']['scenarios']),'historicalControlTotalsExactlyPreserved':True,'completeReasonAndOutcomeConservation':True,'correctDayStopAttribution':True,'chapterResourceContinuity':True,'simulationProvenanceSha256':sha(D/'provenance.json'),'humanParticipants':0}
(ROOT/'docs/validation/cognitive-lab-v122/validation.json').write_text(json.dumps(receipt,indent=2)+'\n');print(json.dumps(receipt))
