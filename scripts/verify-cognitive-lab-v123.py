#!/usr/bin/env python3
"""Exhaustive versioned 1.23 output validation; no campaign rerun or source mutation."""
import argparse,collections,csv,gzip,hashlib,json,pathlib

ROOT=pathlib.Path(__file__).resolve().parents[1]
ap=argparse.ArgumentParser();ap.add_argument('--data',type=pathlib.Path,default=ROOT/'docs/validation/cognitive-lab-v123/latest');ap.add_argument('--require-current-source',action='store_true');ap.add_argument('--receipt',type=pathlib.Path);args=ap.parse_args()
D=args.data
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def read(name):return json.loads((D/name).read_text())
def rows(name):
 with gzip.open(D/name,'rt',newline='') as f:
  for row in csv.DictReader(f):assert None not in row,(name,'CSV schema');yield row
s,p,pr=read('summary.json'),read('paired-comparisons.json'),read('provenance.json')
expected=['manual_native_123','cause_hidden_123','loaned_mixed_123','energy_unlimited_123']
assert s['config']['scenarios']==expected==[r['id'] for r in s['scenarios']]
assert s['scope']['humanParticipants']==0 and s['config']['nativeFactory']=='ElementalShell121.createJourney123'
assert pr['sourceHashesStableBeforeAndAfter'] and 'app/src/main/java/com/androidhit/lumadrift/LumaView.java' not in pr['sourceSha256']
for name,item in pr['outputs'].items():assert sha(D/name)==item['sha256'] and (D/name).stat().st_size==item['bytes'],name
if args.require_current_source:
 for name,h in pr['sourceSha256'].items():assert sha(ROOT/name)==h,('Source drift',name)
n=s['config']['profiles']*s['config']['repetitions'];day_count=s['config']['days']
tx=collections.Counter();attempts=collections.Counter();outcomes=collections.defaultdict(collections.Counter);total=collections.defaultdict(collections.Counter);blocks=collections.Counter();day=collections.Counter();decisions=collections.Counter();state=collections.Counter();links=collections.Counter();causal_links=collections.Counter();probe=collections.Counter();probe_completed=collections.Counter()
exhaustion_days=collections.Counter()
for r in rows('trajectories.csv.gz'):
 sc=r['scenario'];tx[sc]+=1
 assert int(r['shellTaps'])==0 and int(r['coinsEnd'])==180+int(r['coinsEarned'])-int(r['coinsSpent'])
 assert int(r['attempts'])==sum(json.loads(r['outcomes']).values())
 if sc=='energy_unlimited_123':assert int(r['energySpent'])==0
for r in rows('level-results.csv.gz'):
 sc=r['scenario'];attempts[sc]+=1;outcomes[sc][r['outcome']]+=1
 assert int(r['shellTaps'])==0
 assert int(r['exits'])==int(r['manualExits'])+int(r['automatedExits'])
 assert int(r['blocked'])==sum(int(r[k]) for k in ['closedGateBlocked','pieceBlocked','barrierBlocked'])
 for k in ['actions','blocked','manualExits','automatedExits']:total[sc][k]+=int(r[k])
 if r['outcome']=='horizon_censored':assert int(r['remaining'])>0 and int(r['budgetRemaining'])>0 and int(r['actions'])<s['config']['maxAttemptActions']
 if r['outcome']=='budget_exhausted':
  assert int(r['remaining'])>0 and int(r['budgetRemaining'])==0
  exhaustion_days[(sc,r['profileId'],r['repetition'],r['endDay'])]+=1
 if r['outcome']=='completed':assert int(r['remaining'])==0
for r in rows('blocked-events.csv.gz'):
 blocks[r['scenario']]+=1;assert r['blockedReason'] in ['CLOSED_GATE','BLOCKED_PATH','LOCKED_BARRIER']
for r in rows('day-results.csv.gz'):
 sc=r['scenario'];day[sc]+=1;decisions[sc]+=int(r['decisions'])
 assert int(r['budgetExhaustedAttempts'])==exhaustion_days[(sc,r['profileId'],r['repetition'],r['day'])],('Wrong exhaustion day',r)
 if r['stopReason']=='day_action_cap':assert int(r['decisions'])==120 and r['administrativeActionCap']=='true'
 if r['stopReason']=='repeated_budget_exhaustion':assert int(r['budgetExhaustedAttempts'])==3
 if sc=='energy_unlimited_123':assert r['stopReason']!='energy_empty'
for r in rows('manual-assistance-events.csv.gz'):
 if r['directCueFollow']=='true':links[r['scenario']]+=1;assert r['decisionReason'].startswith('follow_visible_')
 if int(r['visibleCauseRule'])>=0:causal_links[r['scenario']]+=1
for r in rows('learned-rules.csv.gz'):
 sc=r['scenario'];state[sc]+=1
 assert abs(float(r['commonRuleMean'])-sum(float(r[k]) for k in ['gate','path','switch','barrier'])/4)<1e-12
for r in rows('probe-results.csv.gz'):
 sc=r['trainingScenario'];probe[sc]+=1;probe_completed[sc]+=r['outcome']=='completed'
 assert r['outcome'] in ['completed','budget_exhausted','agent_give_up','attempt_action_cap']
 if r['outcome']=='completed':assert int(r['remaining'])==0
 if r['outcome']=='budget_exhausted':assert int(r['remaining'])>0 and int(r['budgetRemaining'])==0
for z in s['scenarios']:
 sc=z['id'];assert tx[sc]==state[sc]==n and day[sc]==n*day_count
 assert attempts[sc]==z['totals']['attempts'] and dict(outcomes[sc])==z['mechanics']['outcomes']
 assert total[sc]['actions']==decisions[sc]==z['totals']['actions']
 assert total[sc]['blocked']==blocks[sc]==z['totals']['blocked']
 assert links[sc]==z['directCueFollowedManualExits'] and causal_links[sc]==z['manualExitsWithCauseVisible']
 assert total[sc]['manualExits']==z['mechanics']['manualExits'] and total[sc]['automatedExits']==z['mechanics']['automatedExits']
 if sc!='loaned_mixed_123':assert total[sc]['automatedExits']==0
 if sc=='cause_hidden_123':assert causal_links[sc]==z['mechanics']['causalCueObservations']==0
t=p['commonEnvironmentTransfer'];assert probe['cold_untrained']==t['coldProbeCount']==n*4 and probe_completed['cold_untrained']==t['coldCompleted']
for z in t['scenarios']:
 assert probe[z['scenario']]==z['probes']==n*4 and probe_completed[z['scenario']]==z['completed']
 assert z['completed']-z['coldReferenceCompleted']==z['trainedOnlyCompleted']-z['coldOnlyCompleted']
 assert sum(z['outcomes'].values())==z['probes']
oracle=read('oracle-controls.json');assert oracle['policyReceivesOracle'] is False and len(oracle['boards'])==104 and all(r['solved'] for r in oracle['boards'])
r={'schemaVersion':1,'status':'PASS','modelVersion':s['config']['modelVersion'],'seed':s['config']['seed'],'trajectories':sum(tx.values()),'attemptRows':sum(attempts.values()),'blockedEvents':sum(blocks.values()),'dayRows':sum(day.values()),'learnedStateRows':sum(state.values()),'directCueFollowedManualExits':sum(links.values()),'coldProbeRows':probe['cold_untrained'],'trainedProbeRows':sum(v for k,v in probe.items() if k!='cold_untrained'),'oracleBoardsSeparate':104,'zeroBudgetHorizonRows':0,'allBudgetExhaustionsOnActualEndingDay':True,'allOutputHashesMatch':True,'currentSourceRequiredAndMatched':args.require_current_source,'provenanceSha256':sha(D/'provenance.json'),'humanParticipants':0}
if args.receipt:args.receipt.parent.mkdir(parents=True,exist_ok=True);args.receipt.write_text(json.dumps(r,indent=2)+'\n')
print(json.dumps(r,indent=2))
