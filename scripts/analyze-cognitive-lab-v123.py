#!/usr/bin/env python3
"""Descriptive analysis of the source-bound 1.23 run; no experiment rerun."""
import argparse,collections,csv,gzip,hashlib,json,pathlib
ROOT=pathlib.Path(__file__).resolve().parents[1]
ap=argparse.ArgumentParser();ap.add_argument('--data',type=pathlib.Path,default=ROOT/'docs/validation/cognitive-lab-v123/latest');ap.add_argument('--output',type=pathlib.Path,default=ROOT/'docs/validation/cognitive-lab-v123/analysis.json');args=ap.parse_args();D=args.data
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def read(n):return json.loads((D/n).read_text())
def rows(n):
 with gzip.open(D/n,'rt',newline='') as f:
  yield from csv.DictReader(f)
def frac(n,d):return {'numerator':n,'denominator':d,'ratio':n/d if d else None}
def quantiles(v):
 v=sorted(v);return {str(q):v[round((len(v)-1)*q)] for q in [0,.1,.25,.5,.75,.9,1]}
FIELDS=['actions','exits','manualExits','automatedExits','blocked','closedGateBlocked','pieceBlocked','barrierBlocked','repeatedErrors','undos','paidUndoCoins','focusUses','causalCueObservations','skillRequests','skillUses','skillUnavailable']
def empty():return {'starts':0,'outcomes':collections.Counter(),'totals':collections.Counter(),'arrivals':set()}
def add(a,r):
 a['starts']+=1;a['outcomes'][r['outcome']]+=1;a['arrivals'].add((r['profileId'],r['repetition']))
 for k in FIELDS:a['totals'][k]+=int(r[k])
def finish(a):
 t,o=a['totals'],a['outcomes'];return {'starts':a['starts'],'arrivingTrajectories':len(a['arrivals']),'outcomes':dict(o),'totals':dict(t),'exhaustionPerStart':frac(o['budget_exhausted'],a['starts']),'completionPerStart':frac(o['completed'],a['starts']),'blockedPerAllDecision':frac(t['blocked'],t['actions']),'blockedPerExitOrBlockedTap':frac(t['blocked'],t['exits']+t['blocked']),'automatedExitShare':frac(t['automatedExits'],t['exits'])}
s,p,pr=read('summary.json'),read('paired-comparisons.json'),read('provenance.json');scenarios=s['config']['scenarios'];levels=collections.defaultdict(empty);whole=collections.defaultdict(empty);first={};count=0
for r in rows('level-results.csv.gz'):
 count+=1;sc=r['scenario'];n=int(r['level']);add(levels[sc,n],r);add(whole[sc],r)
 assert int(r['shellTaps'])==0
 if r['outcome']=='horizon_censored':assert int(r['budgetRemaining'])>0 and int(r['remaining'])>0
 if n in [27,43,48]:first.setdefault((sc,n,r['profileId'],r['repetition']),r)
tx=collections.defaultdict(list)
for r in rows('trajectories.csv.gz'):tx[r['scenario']].append(r)
stops=collections.defaultdict(collections.Counter)
for r in rows('day-results.csv.gz'):stops[r['scenario']][r['stopReason']]+=1
probe_groups=collections.defaultdict(collections.Counter)
for r in rows('probe-results.csv.gz'):
 g=probe_groups[r['trainingScenario'],int(r['level'])];g['probes']+=1;g[r['outcome']]+=1;g['blocked']+=int(r['blocked']);g['actions']+=int(r['actions'])
event_counts=collections.defaultdict(collections.Counter)
for r in rows('manual-assistance-events.csv.gz'):
 g=event_counts[r['scenario']];g['eventRows']+=1
 if r['directCueFollow']=='true':g['directCueFollowedManualExits']+=1;g['source_'+r['cueSource']]+=1
 if int(r['visibleCauseRule'])>=0:g['manualExitsWithCauseVisible']+=1
out={'schemaVersion':1,'runId':s['runId'],'seed':s['config']['seed'],'humanParticipants':0,'provenanceSha256':sha(D/'provenance.json'),
 'method':{'pooledRates':'Exact numerators/denominators; distinguish from mean per-trajectory rates in paired JSON.','arrivals':'Later levels condition on actually arriving trajectories; retry starts retained.','firstAttempts':'Matched profile/repetition first arrivals at U27/U43/U48; common-arrival selection and different prior exposure remain.','manual':'Tap execution may follow native cues. Exact direct-cue-follow events counted separately; absence of a cue on one action does not prove no prior help.','probes':'Same shell-free budget/geometry/no-aid environment in all regimes and cold reference; only final learned state transplanted.8000 cold rows reused as reference, not reexecuted four times.','learning':'Common four-rule state is an uncalibrated model variable. Probe differences do not establish human cognition.','returns':'Availability,utility,modeledD1/D7 and decision timing are assumptions, not human retention or speed.','oracle':'104 engine certification boards separate from all8000 limited-perception trajectories.','historical':'New cohort seed and immediate terminal-state semantics differ from1.22; no causal before/after release claim.'},
 'counts':{'trajectories':sum(len(v) for v in tx.values()),'attemptRows':count,'configuredDays':sum(sum(v.values()) for v in stops.values()),'coldProbes':p['commonEnvironmentTransfer']['coldProbeCount'],'trainedProbes':p['commonEnvironmentTransfer']['trainedProbeCount'],'oracleBoardsSeparate':104},'scenarios':{},'commonFirstAttempts':{},'pairedComparisons':p['comparisons'],'commonEnvironmentTransfer':p['commonEnvironmentTransfer'],'probeByLevel':[{'trainingScenario':sc,'level':n,**dict(v)} for (sc,n),v in probe_groups.items()]}
for sc in scenarios:
 a=finish(whole[sc]);ss=next(z for z in s['scenarios'] if z['id']==sc)
 assert a['starts']==ss['totals']['attempts'] and a['outcomes']==ss['mechanics']['outcomes']
 a['summaryMeanMetrics']={k:ss[k] for k in ['meanLevelsCleared','blockedRate','meanSessionMinutes','meanCommonRuleKnowledge']}
 a['trajectoryQuantiles']={k:quantiles([float(r[k]) for r in tx[sc]]) for k in ['levelsCleared','actions','undos','coinsSpent','energySpent']}
 a['resources']={k:sum(int(r[k]) for r in tx[sc]) for k in ['energySpent','energyRefilled','coinsSpent','coinsEarned','coinsEnd']}
 a['dailyStops']=dict(stops[sc]);a['configuredDays']=sum(stops[sc].values());a['manualAssistance']=dict(event_counts[sc])
 a['perLevel']={str(n):finish(v) for (condition,n),v in levels.items() if condition==sc};a['highestAttemptedLevel']=max(int(n) for n in a['perLevel'])
 for metric in ['directCueFollowedManualExits','manualExitsWithCauseVisible']:assert event_counts[sc][metric]==ss[metric]
 out['scenarios'][sc]=a
for sc in scenarios[1:]:
 out['commonFirstAttempts'][sc]={}
 for n in [27,43,48]:
  ids={(pid,rep) for condition,level,pid,rep in first if condition==sc and level==n}&{(pid,rep) for condition,level,pid,rep in first if condition==scenarios[0] and level==n}
  out['commonFirstAttempts'][sc][str(n)]={}
  for condition in [scenarios[0],sc]:
   a=empty()
   for pid,rep in sorted(ids):add(a,first[condition,n,pid,rep])
   out['commonFirstAttempts'][sc][str(n)][condition]=finish(a)
args.output.parent.mkdir(parents=True,exist_ok=True);args.output.write_text(json.dumps(out,indent=2,sort_keys=True)+'\n')
print(json.dumps({'status':'PASS','output':str(args.output),'sha256':sha(args.output),'counts':out['counts']},indent=2))
