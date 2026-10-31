#!/usr/bin/env python3
"""Exhaustive versioned 1.24 output validation; no campaign rerun or source mutation."""
import argparse,collections,csv,gzip,hashlib,json,pathlib

ROOT=pathlib.Path(__file__).resolve().parents[1]
ap=argparse.ArgumentParser();ap.add_argument('--data',type=pathlib.Path,default=ROOT/'docs/validation/cognitive-lab-v124/latest');ap.add_argument('--require-current-source',action='store_true');ap.add_argument('--receipt',type=pathlib.Path);args=ap.parse_args()
D=args.data
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def read(name):return json.loads((D/name).read_text())
def rows(name):
 with gzip.open(D/name,'rt',newline='') as f:
  for row in csv.DictReader(f):assert None not in row,(name,'CSV schema');yield row
s,p,pr=read('summary.json'),read('paired-comparisons.json'),read('provenance.json')
expected=['native_rank_124','single_use_control_124','manual_control_124','loaned_trio_rank_124']
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
 if sc!='loaned_trio_rank_124':assert total[sc]['automatedExits']==0
t=p['commonEnvironmentTransfer'];assert probe['cold_untrained']==t['coldProbeCount']==n*4 and probe_completed['cold_untrained']==t['coldCompleted']
for z in t['scenarios']:
 assert probe[z['scenario']]==z['probes']==n*4 and probe_completed[z['scenario']]==z['completed']
 assert z['completed']-z['coldReferenceCompleted']==z['trainedOnlyCompleted']-z['coldOnlyCompleted']
 assert sum(z['outcomes'].values())==z['probes']
oracle=read('oracle-controls.json');assert oracle['policyReceivesOracle'] is False and len(oracle['boards'])==104 and all(r['solved'] for r in oracle['boards'])
# New exhaustive reward/capacity ledgers are joined to the actual ended attempts.
def txkey(r):return (r['scenario'],r['profileId'],r['repetition'])
def attemptkey(r):return (*txkey(r),r['attempt'])
def skill_capacity(level):return 4 if level>=6 else 3 if level>=3 else 2 if level>=2 else 1
def companion_level(copies):return sum(copies>=n for n in [1,2,8,14,22,32,44,58,74,92])
completed={attemptkey(r):(int(r['level']),int(r['stars'])) for r in rows('level-results.csv.gz') if r['outcome']=='completed'}
progress_counts=collections.Counter();rank_awards=collections.Counter();xp_totals=collections.Counter();overflow=collections.Counter();last={};seen=set()
for r in rows('companion-progression.csv.gz'):
 key=txkey(r);ak=attemptkey(r);assert ak not in seen and ak in completed;seen.add(ak)
 level,stars=completed[ak];assert level==int(r['level']) and r['captain']=='nova'
 before_xp,before_copies=last.get(key,(0,1));earned=20+5*stars;after=before_xp+earned;rank_count=after//120-before_xp//120
 assert int(r['xpBefore'])==before_xp and int(r['xpEarned'])==earned and int(r['xpAfter'])==after
 assert int(r['rankBefore'])==1+before_xp//120 and int(r['rankAfter'])==1+after//120 and int(r['ranksAwarded'])==rank_count
 copies=min(92,before_copies+rank_count);coins=max(0,before_copies+rank_count-92)*45
 assert int(r['copiesBefore'])==before_copies and int(r['copiesAfter'])==copies and int(r['overflowCoins'])==coins
 for suffix,count in [('Before',before_copies),('After',copies)]:
  assert int(r['level'+suffix])==companion_level(count) and int(r['nextCapacity'+suffix])==skill_capacity(companion_level(count))
 last[key]=(after,copies);progress_counts[key[0]]+=1;rank_awards[key[0]]+=rank_count;xp_totals[key[0]]+=earned;overflow[key[0]]+=coins
assert seen==set(completed)
caps={};cap_counts=collections.Counter()
for r in rows('attempt-companion-capacities.csv.gz'):
 ak=attemptkey(r);assert ak not in caps;sc=r['scenario'];team=json.loads(r['team']);levels=json.loads(r['levels']);capacities=json.loads(r['capacities']);xp=int(r['xpAtStart']);copies=int(r['captainCopiesAtStart'])
 assert team==(['nova','hoot','orbit'] if sc=='loaned_trio_rank_124' else ['nova'])
 assert copies==min(92,1+xp//120) and levels==[companion_level(copies)]+([1,1] if len(team)==3 else [])
 if sc=='manual_control_124':assert capacities==[] and r['policyVersion']=='disabled'
 elif sc=='single_use_control_124':assert capacities==[1] and r['policyVersion']=='v119_1'
 else:assert capacities==[skill_capacity(lv) for lv in levels] and r['policyVersion']=='v124_1'
 caps[ak]=(team,capacities);cap_counts[sc]+=1
requests=collections.Counter();uses=collections.Counter();repeated=collections.Counter();refused=collections.Counter();spent_charge=collections.Counter();use_counts={}
for r in rows('companion-use-events.csv.gz'):
 ak=attemptkey(r);assert ak in caps;team,capacities=caps[ak];slot=int(r['slot']);combo=r['combo']=='true';sc=r['scenario'];success=r['result']!='skill_unavailable'
 assert sc!='manual_control_124' and 0<=slot<len(team) and int(r['capacity'])==capacities[slot]
 before=int(r['usedBefore']);after=int(r['usedAfter']);charged=int(r['chargeSpent']);previous=use_counts.get((ak,slot),0)
 assert before==previous and after==before+(1 if success and not combo else 0) and int(r['remainingAfter'])==capacities[slot]-after
 assert 0<=after<=capacities[slot] and 0<=int(r['chargeBefore'])<=6 and 0<=int(r['chargeAfter'])<=6
 assert charged==((3 if team[slot]=='orbit' else 2) if success and not combo else 0)
 if success:assert int(r['chargeBefore'])-charged<=int(r['chargeAfter'])<=min(6,int(r['chargeBefore'])-charged+int(r['automatedExits']))
 else:assert before==after and r['chargeBefore']==r['chargeAfter'] and int(r['automatedExits'])==0
 use_counts[ak,slot]=after;requests[sc]+=1;uses[sc]+=success;refused[sc]+=not success;spent_charge[sc]+=charged
 repeated[sc]+=success and not combo and after>1
state_counts=collections.Counter()
for r in rows('progression-trajectories.csv.gz'):
 key=txkey(r);xp,copies=last.get(key,(0,1));sc=r['scenario'];assert r['captain']=='nova'
 assert int(r['xp'])==xp and int(r['rank'])==1+xp//120 and int(r['rankAwards'])==xp//120 and int(r['captainCopies'])==copies
 assert int(r['captainLevel'])==companion_level(copies) and int(r['nextCapacity'])==skill_capacity(companion_level(copies));state_counts[sc]+=1
for z in s['scenarios']:
 sc=z['id'];m=z['mechanics'];g=z['progression'];assert cap_counts[sc]==z['totals']['attempts'] and progress_counts[sc]==z['totals']['levelsCleared'] and state_counts[sc]==n
 assert requests[sc]==m['skillRequests'] and uses[sc]==m['skillUses'] and refused[sc]==m['skillUnavailable']
 assert repeated[sc]==g['repeatedSuccessfulSkillUses'] and rank_awards[sc]==g['rankAwards'] and xp_totals[sc]==g['playerXpEarned'] and overflow[sc]==g['overflowCoins']
 if sc in ['single_use_control_124','manual_control_124']:assert repeated[sc]==0
r={'schemaVersion':1,'status':'PASS','modelVersion':s['config']['modelVersion'],'seed':s['config']['seed'],'trajectories':sum(tx.values()),'attemptRows':sum(attempts.values()),'blockedEvents':sum(blocks.values()),'dayRows':sum(day.values()),'learnedStateRows':sum(state.values()),'directCueFollowedManualExits':sum(links.values()),'coldProbeRows':probe['cold_untrained'],'trainedProbeRows':sum(v for k,v in probe.items() if k!='cold_untrained'),'oracleBoardsSeparate':104,'zeroBudgetHorizonRows':0,'allBudgetExhaustionsOnActualEndingDay':True,'allOutputHashesMatch':True,'currentSourceRequiredAndMatched':args.require_current_source,'provenanceSha256':sha(D/'provenance.json'),'humanParticipants':0,'rankRewardRows':sum(progress_counts.values()),'rankAwards':sum(rank_awards.values()),'capturedAttemptCapacityRows':sum(cap_counts.values()),'skillRequestRows':sum(requests.values()),'repeatedSuccessfulSkillUses':dict(repeated),'chargeSpentByScenario':dict(spent_charge),'allRewardsAndCapturedCapacitiesConserved':True}
if args.receipt:args.receipt.parent.mkdir(parents=True,exist_ok=True);args.receipt.write_text(json.dumps(r,indent=2)+'\n')
print(json.dumps(r,indent=2))
