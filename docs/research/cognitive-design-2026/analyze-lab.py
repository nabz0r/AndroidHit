#!/usr/bin/env python3
"""Read-only reproducible analysis of published Luma Cognitive Lab CSV/JSON.
No package dependency. No simulation rerun or source mutation.
Usage: python3 docs/research/cognitive-design-2026/analyze-lab.py
"""
import argparse, collections, csv, gzip, hashlib, json, pathlib

ap=argparse.ArgumentParser();ap.add_argument('--root',default=str(pathlib.Path(__file__).resolve().parents[3]));ap.add_argument('--output',default=str(pathlib.Path(__file__).resolve().with_name('lab-analysis.json')));args=ap.parse_args()
root=pathlib.Path(args.root);data=root/'public/lab/cognitive/data'
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def loadcsv(filename):
 with gzip.open(data/filename,'rt',newline='') as f: records=list(csv.DictReader(f))
 for row in records:
  for key,value in list(row.items()):
   if key in {'profileId','scenario','outcome'}:continue
   if key=='stopReasons':row[key]=json.loads(value)
   elif value in {'true','false'}:row[key]=value=='true'
   else:
    try:row[key]=int(value)
    except ValueError:row[key]=float(value)
 return records
L=loadcsv('level-results.csv.gz');T=loadcsv('trajectories.csv.gz');S=json.loads((data/'summary.json').read_text());P=json.loads((data/'profiles.json').read_text())
SCENARIOS=[s['id'] for s in S['scenarios']]
SUM_FIELDS=['actions','exits','blocked','undos','shellTaps','focusRequests','focusUses','paidUndoCoins','budgetUsed','budgetRemaining','remaining','modeledSeconds','coachCueObservations','deadEndWarningObservations','focusCueObservations']
def fraction(n,d):return {'numerator':n,'denominator':d,'ratio':n/d if d else None}
def aggregate(rows):
 outcomes=collections.Counter(r['outcome'] for r in rows);a={'attempts':len(rows),'outcomes':dict(outcomes)}
 for key in SUM_FIELDS:a[key]=sum(r[key] for r in rows)
 a['pooledBlockedAllInteractions']=fraction(a['blocked'],a['actions'])
 a['shellShareAllInteractions']=fraction(a['shellTaps'],a['actions'])
 a['budgetExhaustionPerStartedAttempt']=fraction(outcomes['budget_exhausted'],len(rows))
 a['completionPerStartedAttempt']=fraction(outcomes['completed'],len(rows))
 a['completionAmongResolvedAttempts']=fraction(outcomes['completed'],len(rows)-outcomes['horizon_censored'])
 return a
def quantile(values):
 a=sorted(values)
 return {str(q):a[round((len(a)-1)*q)] for q in [0,.1,.25,.5,.75,.9,1]} # nearest index, ties use Python round
out={'schemaVersion':1,'kind':'read_only_descriptive_reanalysis','sourceRoot':str(root),'sources':{},'method':{'cohort':'100 generated profiles x 30 repetitions x 3 scenarios; no human participants','fractions':'All fractions retain numerator and denominator. Pooled ratios differ from mean per-trajectory ratios reported by lab summary.','outcomes':'budget_exhausted is a resolved failed attempt; horizon_censored is unfinished at configured eight-day horizon, not failed.','stopReasons':'One final recorded reason per configured trajectory-day (24,000 per scenario), not every failed attempt. IMPORTANT: stop label budget_exhausted persists after a nonterminal failure if the subsequent loop exits at the daily cap. It is not a count of three-failure days. Stops can occur on a day with zero decisions; do not divide every stop count by gameplay sessions.','quantile':'Sorted nearest-index values: sorted[round((n-1)*q)]','profileSubgroups':'Descriptive comparisons of generated parameter strata; neither calibrated human segments nor causal parameter effects.','pairedFirstAttempts':'Restrict to profile+repetition pairs reaching specified level under both scenarios; retain first attempt. Common-arrival selection and different paths remain.','teaching':'Hypothetical +0.16 initial rule knowledge, 1.7x learning, 12 model seconds at U1/U11; not a shipped UI intervention.','rawModeledSessionMinutes':'Trajectory CSV stores summed minutes over sessions. Summary meanSessionMinutes averages per-trajectory session means. Derived quantiles divide raw minutes by trajectory sessions.','coverage':'Sequential campaign scope U1-U400; actual highest attempted levels reported separately. Held-out probe levels are separate experiments, not campaign coverage.'},'scenarios':{},'profilePairedTeaching':{},'commonFirstAttempts':{}}
for file in ['summary.json','profiles.json','level-results.csv.gz','trajectories.csv.gz']:
 p=data/file;out['sources'][str(p.relative_to(root))]={'sha256':digest(p),'bytes':p.stat().st_size}
for file in ['scripts/cognitive_lab/CognitiveLab.java','scripts/cognitive_lab/CognitiveAgent.java','scripts/cognitive_lab/Environment.java','app/src/main/java/com/androidhit/lumadrift/engine/MoveBudget.java']:
 p=root/file;out['sources'][file]={'sha256':digest(p),'bytes':p.stat().st_size,'use':'Interpret metric semantics and model intervention; no code executed from this source.'}
for scenario in SCENARIOS:
 rows=[r for r in L if r['scenario']==scenario];ts=[r for r in T if r['scenario']==scenario];groups=collections.defaultdict(list)
 for row in rows:groups[row['level']].append(row)
 a=aggregate(rows);summary=next(r for r in S['scenarios'] if r['id']==scenario);stops=collections.Counter();exposed=collections.Counter()
 for row in ts:stops.update(row['stopReasons']);exposed.update(row['stopReasons'].keys())
 assert len(rows)==sum(r['attempts'] for r in ts)==summary['totals']['attempts']
 assert a['outcomes'].get('completed',0)==sum(r['levelsCleared'] for r in ts)==summary['totals']['levelsCleared']
 assert a['actions']==sum(r['actions'] for r in ts)==summary['totals']['actions']
 assert a['paidUndoCoins']==sum(r['coinsSpent'] for r in ts)==summary['totals']['coinsSpent']
 assert sum(stops.values())==len(ts)*S['config']['days']
 target=[r for r in rows if r['level'] in {43,48}];target_a=aggregate(target)
 failure_days=collections.Counter((r['profileId'],r['repetition'],r['endDay']) for r in rows if r['outcome']=='budget_exhausted')
 failure_hist=collections.Counter(failure_days.values());three_failure_days=sum(n for failures,n in failure_hist.items() if failures>=3)
 sticky_budget_labels=stops['budget_exhausted']-three_failure_days
 assert sticky_budget_labels>=0
 a['attemptFailureDayCounts']={str(k):v for k,v in sorted(failure_hist.items())}
 a['stopLabelAudit']={'budgetExhaustedRecordedLabels':stops['budget_exhausted'],'daysWithThreeExhaustedAttempts':three_failure_days,'persistentBudgetLabelsOnCapTerminationInferred':sticky_budget_labels,'explicitDayActionCapLabels':stops['day_action_cap'],'capTerminationDaysIncludingPersistentBudgetLabelsInferred':stops['day_action_cap']+sticky_budget_labels,'inferenceBasis':'CognitiveLab.run sets stop=budget_exhausted before continue; only 3 failures break directly. All published outcomes are completed, budget_exhausted or horizon_censored; no campaign cap reached. After nonterminal failure, a subsequent explicit break overrides stop. A while-condition daily-cap exit preserves it. Derived from current source, not directly logged daily actions.'}
 a.update({'trajectoryCount':len(ts),'gameplaySessions':sum(r['sessions'] for r in ts),'highestAttemptedLevel':max(groups),'distinctLevelsAttempted':len(groups),'summaryMeanTrajectoryBlockedRate':summary['blockedRate'],'summaryMeanTrajectoryCompletionRate':summary['completionRate'],'unfinishedShareOfUncompletedAttempts':fraction(a['outcomes'].get('horizon_censored',0),len(rows)-a['outcomes'].get('completed',0)),'concentrationU43U48':{'attemptShare':fraction(len(target),len(rows)),'exhaustionShare':fraction(target_a['outcomes'].get('budget_exhausted',0),a['outcomes'].get('budget_exhausted',0)),'paidUndoCoinShare':fraction(target_a['paidUndoCoins'],a['paidUndoCoins'])},'stopReasonCounts':dict(stops),'trajectoriesExposedToStop':{k:fraction(v,len(ts)) for k,v in exposed.items()},'economy':{'anyUndo':fraction(sum(r['undos']>0 for r in ts),len(ts)),'anyPaidUndo':fraction(sum(r['coinsSpent']>0 for r in ts),len(ts)),'paidUndoOverStarting180':fraction(sum(r['coinsSpent']>180 for r in ts),len(ts)),'minimumFinalCoins':min(r['coinsEnd'] for r in ts),'energySpent':sum(r['energySpent'] for r in ts),'energyRefilled':sum(r['energyRefilled'] for r in ts)},'quantiles':{key:quantile([r[key] for r in ts]) for key in ['levelsCleared','actions','blocked','undos','coinsSpent','energySpent','meanKnowledge']},'meanSessionMinutesQuantiles':quantile([r['modeledSessionMinutes']/r['sessions'] for r in ts])})
 out['scenarios'][scenario]={'overall':a,'bands':{f'{lo}-{hi}':aggregate([r for r in rows if lo<=r['level']<=hi]) for lo,hi in [(1,10),(11,20),(21,30),(31,40),(41,60),(61,100),(101,400)]},'perLevel':{str(k):aggregate(v) for k,v in sorted(groups.items())}}
by=collections.defaultdict(dict)
for r in S['profileResults']:by[r['profileId']][r['scenario']]=r
profile_params={r['id']:r['parameters'] for r in P['profiles']};paired=[]
for profile,d in sorted(by.items()):
 b=d['current_model'];t=d['teaching_support'];paired.append({'profileId':profile,'parameters':profile_params[profile],'differences':{k:t[k]-b[k] for k in ['blockedRate','meanLevelsCleared','completionRate','meanKnowledge','modeledD1','modeledD7']}})
for key in paired[0]['differences']:
 values=[p['differences'][key] for p in paired]
 out['profilePairedTeaching'][key]={'profiles':len(values),'positive':sum(v>0 for v in values),'negative':sum(v<0 for v in values),'zero':sum(v==0 for v in values),'quantiles':quantile(values)}
low=sorted(paired,key=lambda p:p['parameters']['learningRate']);out['profilePairedTeaching']['learningRateQuartiles']={}
for label,group in [('lowest25',low[:25]),('highest25',low[-25:])]:
 out['profilePairedTeaching']['learningRateQuartiles'][label]={'n':len(group),'learningRateRange':[min(r['parameters']['learningRate'] for r in group),max(r['parameters']['learningRate'] for r in group)],'meanDifferences':{k:sum(r['differences'][k] for r in group)/len(group) for k in ['blockedRate','meanLevelsCleared','completionRate','meanKnowledge']}}
out['profilePairedTeaching']['profiles']=paired
for level in [8,11,14,25,29,31,43,48]:
 first=collections.defaultdict(dict)
 for row in L:
  if row['level']==level and row['scenario'] in ['current_model','teaching_support']:
   key=(row['profileId'],row['repetition']);first[key].setdefault(row['scenario'],row)
 common=[d for d in first.values() if len(d)==2]
 out['commonFirstAttempts'][str(level)]={s:aggregate([d[s] for d in common]) for s in ['current_model','teaching_support']}
# All published main CSV outcomes checked, not inferred from the header.
assert set(r['outcome'] for r in L)=={'completed','budget_exhausted','horizon_censored'}
out['verifiedMainOutcomes']=sorted(set(r['outcome'] for r in L));out['readCounts']={'levelRows':len(L),'trajectoryRows':len(T),'profiles':len(P['profiles'])}
output=pathlib.Path(args.output);output.write_text(json.dumps(out,ensure_ascii=False,indent=2)+'\n')
print(json.dumps({'output':str(output),'bytes':output.stat().st_size,'sha256':digest(output),'readCounts':out['readCounts']},ensure_ascii=False))
