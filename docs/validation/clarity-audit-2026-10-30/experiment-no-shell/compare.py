#!/usr/bin/env python3
"""Exhaustive checks and paired, profile-cluster shell-ablation comparison."""
import collections,csv,gzip,hashlib,json,pathlib,random
HERE=pathlib.Path(__file__).resolve().parent
ROOT=HERE.parents[3]
PROTOCOL=json.loads((HERE/'protocol.json').read_text())
VARIANTS=['control-disabled','no-shell-enabled']
SCENARIOS=['control_v121','cause_immediate','cause_repeat','cause_fading','team_information','team_execution','v122_combined']
CSV_NAMES=['trajectories.csv.gz','level-results.csv.gz','blocked-events.csv.gz','day-results.csv.gz','chapter-results.csv.gz','chapter-stage-results.csv.gz']
SUMS=['actions','exits','blocked','undos','shellTaps','focusUses','paidUndoCoins','manualExits','automatedExits','closedGateBlocked','pieceBlocked','barrierBlocked','repeatedErrors','causalCueObservations','skillRequests','skillUses','skillUnavailable','coachCueObservations','focusCueObservations','deadEndWarningObservations']
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def frac(n,d):return {'numerator':n,'denominator':d,'ratio':n/d if d else None}
def records(d,n):
    with gzip.open(d/n,'rt',newline='') as f:
        for r in csv.DictReader(f):assert None not in r;yield r
def quantiles(xs):
    xs=sorted(xs)
    return {str(q):xs[round((len(xs)-1)*q)] for q in [0,.1,.25,.5,.75,.9,1]}
gate=json.loads((HERE/'disabled-replay-gate.json').read_text())
assert gate['status']=='PASS' and gate['sameProfilesBytes'] and set(gate['csvFiles'])==set(CSV_NAMES)
for file,item in gate['csvFiles'].items():
    assert item['byteIdentical'] and sha(HERE/'control-disabled'/file)==item['forkDisabled']==item['base']
    assert sha(HERE.parent/'simulation/run-2026103001'/file)==item['base']
def empty():return {'attempts':0,'outcomes':collections.Counter(),'totals':collections.Counter(),'zeroBudgetAtHorizon':0}
def add(a,r):
    a['attempts']+=1;a['outcomes'][r['outcome']]+=1
    for k in SUMS:a['totals'][k]+=int(r[k])
    if r['outcome']=='horizon_censored' and int(r['budgetRemaining'])==0 and int(r['remaining'])>0:a['zeroBudgetAtHorizon']+=1
def finalize(a):
    t,o=a['totals'],a['outcomes'];return {**a,'totals':dict(t),'outcomes':dict(o),
        'exhaustionPerStart':frac(o['budget_exhausted'],a['attempts']),
        'exhaustionSensitivityPerStart':frac(o['budget_exhausted']+a['zeroBudgetAtHorizon'],a['attempts']),
        'blockedAllInteractions':frac(t['blocked'],t['actions']),
        'blockedPerExitOrBlockedTap':frac(t['blocked'],t['exits']+t['blocked']),
        'shellShareAllInteractions':frac(t['shellTaps'],t['actions']),
        'automatedExitShare':frac(t['automatedExits'],t['exits'])}
def load_variant(name):
    d=HERE/name;s=json.loads((d/'summary.json').read_text());p=json.loads((d/'paired-comparisons.json').read_text());pr=json.loads((d/'provenance.json').read_text())
    assert s['config']['seed']==2026103001 and s['config']['experimentalFork']['noShells']==(name=='no-shell-enabled')
    for file,v in pr['outputs'].items():assert sha(d/file)==v['sha256'] and (d/file).stat().st_size==v['bytes']
    for file,v in pr['sourceSha256'].items():assert sha(HERE/'model'/file)==v==PROTOCOL['forkInputsSha256'][file]
    tx={};counts=collections.Counter();groups=collections.defaultdict(empty);levels=collections.defaultdict(empty);first={};horizon_by_pair=collections.Counter()
    prefix=hashlib.sha256();prefix_count=0
    for r in records(d,'trajectories.csv.gz'):
        counts['trajectories.csv.gz']+=1;k=(r['scenario'],r['profileId'],int(r['repetition']));assert k not in tx;tx[k]=r
        assert int(r['coinsEnd'])==180+int(r['coinsEarned'])-int(r['coinsSpent'])
        assert int(r['attempts'])==sum(json.loads(r['outcomes']).values())
    for r in records(d,'level-results.csv.gz'):
        counts['level-results.csv.gz']+=1;sc=r['scenario'];n=int(r['level']);add(groups[sc],r);add(levels[sc,n],r)
        assert int(r['exits'])==int(r['manualExits'])+int(r['automatedExits'])
        assert int(r['blocked'])==sum(int(r[k]) for k in ['closedGateBlocked','pieceBlocked','barrierBlocked'])
        if name=='no-shell-enabled':assert int(r['shellTaps'])==0
        if n<=10:prefix.update((json.dumps(r,sort_keys=True,separators=(',',':'))+'\n').encode());prefix_count+=1
        k=(sc,r['profileId'],int(r['repetition']))
        if r['outcome']=='horizon_censored' and int(r['budgetRemaining'])==0 and int(r['remaining'])>0:horizon_by_pair[k]+=1
        if n in {11,25,29,31,43,48,60} and sc in {'control_v121','cause_immediate','v122_combined'}:first.setdefault((sc,n,r['profileId'],int(r['repetition'])),r)
    blocks=collections.defaultdict(collections.Counter)
    for r in records(d,'blocked-events.csv.gz'):
        counts['blocked-events.csv.gz']+=1;blocks[r['scenario']][r['blockedReason']]+=1
        assert r['blockedReason'] in ['CLOSED_GATE','BLOCKED_PATH','LOCKED_BARRIER']
    days=collections.defaultdict(collections.Counter);day_decisions=collections.Counter()
    for r in records(d,'day-results.csv.gz'):
        counts['day-results.csv.gz']+=1;days[r['scenario']][r['stopReason']]+=1;day_decisions[r['scenario']]+=int(r['decisions'])
        if r['stopReason']=='day_action_cap':assert int(r['decisions'])==120 and r['administrativeActionCap']=='true'
    for r in records(d,'chapter-results.csv.gz'):counts['chapter-results.csv.gz']+=1
    previous={}
    for r in records(d,'chapter-stage-results.csv.gz'):
        counts['chapter-stage-results.csv.gz']+=1;k=tuple(r[t] for t in ['profileId','repetition','episode','capacity','policy']);st=int(r['stage']);before=int(r['chargeBefore']);after=int(r['chargeAfter'])
        assert 0<=after<=before
        if k in previous:assert (st,before)==(previous[k][0]+1,previous[k][1])
        else:assert st==0 and before==int(r['capacity'])
        previous[k]=st,after
    assert counts['trajectories.csv.gz']==21000 and counts['day-results.csv.gz']==168000 and counts['chapter-results.csv.gz']==54000
    result={'counts':dict(counts),'scenarioResults':{},'transfer':p['transferWithoutCues'],'earlyU1U10Rows':prefix_count,'earlyU1U10Sha256':prefix.hexdigest(),'provenanceSha256':sha(d/'provenance.json'),'elapsedSeconds':pr['elapsedSeconds']}
    for sc in SCENARIOS:
        a=finalize(groups[sc]);ss=next(t for t in s['scenarios'] if t['id']==sc);a['summaryMeanMetrics']={k:ss[k] for k in ['meanLevelsCleared','meanActions','blockedRate','meanSessionMinutes']}
        assert a['attempts']==ss['totals']['attempts'] and a['outcomes']==ss['mechanics']['outcomes']
        assert sum(blocks[sc].values())==a['totals']['blocked']==ss['totals']['blocked']
        assert a['totals']['actions']==day_decisions[sc]==ss['totals']['actions']
        assert sum(days[sc].values())==24000
        a['dailyStops']=dict(days[sc]);a['blockingReasons']=dict(blocks[sc]);a['perLevel']={str(n):finalize(v) for (condition,n),v in levels.items() if condition==sc}
        selected=[r for (condition,_,_),r in tx.items() if condition==sc];assert len(selected)==3000
        a['trajectoryQuantiles']={k:quantiles([int(r[k]) for r in selected]) for k in ['levelsCleared','actions','shellTaps','undos','coinsSpent','energySpent']}
        a['highestAttemptedLevel']=max(int(k) for k in a['perLevel'])
        result['scenarioResults'][sc]=a
    assert sum(r['probes'] for r in result['transfer']['scenarios'])==84000
    for r in result['transfer']['scenarios']:assert sum(r['outcomes'].values())==12000 and 'unknown' not in r['outcomes']
    return result,tx,first,horizon_by_pair

results={};trajectories={};firsts={};boundary={}
for name in VARIANTS:results[name],trajectories[name],firsts[name],boundary[name]=load_variant(name)
assert (HERE/VARIANTS[0]/'profiles.json').read_bytes()==(HERE/VARIANTS[1]/'profiles.json').read_bytes()
assert results[VARIANTS[0]]['earlyU1U10Sha256']==results[VARIANTS[1]]['earlyU1U10Sha256']
for file in CSV_NAMES[-2:]:assert (HERE/VARIANTS[0]/file).read_bytes()==(HERE/VARIANTS[1]/file).read_bytes()
assert trajectories[VARIANTS[0]].keys()==trajectories[VARIANTS[1]].keys()

METRICS=['levelsCleared','actions','blocked','blockedRate','shellTaps','manualExits','automatedExits','undos','coinsSpent','energySpent','budgetExhaustedAttempts','budgetExhaustionIncludingBoundary','completionRate']
def value(row,key,boundary_count):
    if key=='blockedRate':return int(row['blocked'])/int(row['actions'])
    if key=='completionRate':return int(row['levelsCleared'])/int(row['attempts'])
    if key=='budgetExhaustionIncludingBoundary':return int(row['budgetExhaustedAttempts'])+boundary_count
    return int(row[key])
paired={}
for sc in SCENARIOS:
    paired[sc]={}
    for index,metric in enumerate(METRICS):
        per_profile=collections.defaultdict(list)
        for key,b in trajectories[VARIANTS[0]].items():
            if key[0]!=sc:continue
            t=trajectories[VARIANTS[1]][key]
            per_profile[key[1]].append(value(t,metric,boundary[VARIANTS[1]][key])-value(b,metric,boundary[VARIANTS[0]][key]))
        assert len(per_profile)==100 and all(len(v)==30 for v in per_profile.values())
        means=[sum(v)/30 for _,v in sorted(per_profile.items())];rng=random.Random(2026103001+index)
        boot=sorted(sum(means[rng.randrange(100)] for _ in range(100))/100 for _ in range(1000))
        paired[sc][metric]={'differenceNoShellMinusControl':sum(means)/100,'profileClusterResample95':[boot[25],boot[974]],'pairs':3000,'profileClusters':100,'profileMeanQuantiles':quantiles(means),'profilesPositive':sum(x>1e-12 for x in means),'profilesNegative':sum(x< -1e-12 for x in means),'profilesZero':sum(abs(x)<=1e-12 for x in means)}
common_first={}
for sc in ['control_v121','cause_immediate','v122_combined']:
    common_first[sc]={}
    for n in [11,25,29,31,43,48,60]:
        keys={k for k in firsts[VARIANTS[0]] if k[0]==sc and k[1]==n}&firsts[VARIANTS[1]].keys()
        common_first[sc][str(n)]={}
        for v in VARIANTS:
            a=empty()
            for k in sorted(keys):add(a,firsts[v][k])
            common_first[sc][str(n)][v]=finalize(a)
protected=json.loads((HERE.parent/'simulation/source-state-before.json').read_text())
for file,v in protected['preexistingArtifacts'].items():assert sha(ROOT/file)==v['sha256']
for file,v in PROTOCOL['originalInputsSha256'].items():assert sha(ROOT/file)==v
out={'schemaVersion':1,'id':'clarity-no-shell-v1','status':'RECONCILED_WITH_KNOWN_HORIZON_LABEL_LIMITATION','seed':2026103001,'sourceCommitBeforeRun':PROTOCOL['baseSourceCommit'],
     'method':{'paired':'Same profiles.json bytes, profile/repetition/scenario IDs, geometry catalogue and keyed random seeds. Later state/action paths and campaign exposure diverge.',
               'intervention':'Only shell initialization switches to empty. Existing Board/budget/energy/economy/help/agent and timing/utility formulas unchanged. Fork metadata is explicit and not shipped.',
               'clusterIntervals':'1000 resamples of100 whole-profile mean paired differences, each from30 repetitions; within-model cohort sensitivity, not human population confidence.',
               'rateDenominators':'Pooled counters use exact total numerators/denominators. Paired rates use each trajectory denominator and mean paired difference.',
               'transfer':'No-shell applies to training and held-out probes. Probe completion cannot isolate acquired learning because the tested shell environment also changes.',
               'knowledgeMetric':'Not compared: an absent shell rule is not trained; averaging that unused rule with four shared rules would misstate common-rule learning.',
               'manual':'Tap-executed exits include following explicit assistance. Complete CSV records cue observations but not exact full-cohort guided manual exits; do not infer unaided mastery.',
               'horizon':'Original outcome labels retained; separate sensitivity includes horizon_censored rows already at zero budget with pieces remaining.',
               'earlyLevels':'Exact row-hash equality for all U1-U10 attempt records is a negative control before shell introduction.',
               'chapter':'Already shell-free; both chapter CSVs are byte-identical across flags, a second negative control.',
               'limits':'Structured visible state, no rendering, sprite recognition, emotion, human understanding, enjoyment or retention measurement.'},
     'variants':results,'pairedDifferences':paired,'commonFirstAttempts':common_first,
     'verification':{'profilesByteIdentical':True,'sixCsvDisabledReplayByteIdentical':True,'earlyU1U10RowsIdentical':True,'bothChapterCsvByteIdentical':True,'all52OriginalInputsUnchanged':True,'all43ProtectedArtifactsUnchanged':True,'noShellTapsInEnabled':True,'completeCsvCountsConservation':True}}
p=HERE/'comparison.json';p.write_text(json.dumps(out,indent=2,sort_keys=True)+'\n')
print(json.dumps({'comparisonSha256':sha(p),'counts':{k:v['counts'] for k,v in results.items()},'zeroBudgetAtHorizon':{k:sum(s['zeroBudgetAtHorizon'] for s in v['scenarioResults'].values()) for k,v in results.items()},'verification':out['verification']},indent=2))
