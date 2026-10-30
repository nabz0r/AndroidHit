#!/usr/bin/env python3
"""Reconcile and describe one isolated 1.22 run; never writes published artifacts."""
import collections, csv, gzip, hashlib, json, pathlib, re

HERE = pathlib.Path(__file__).resolve().parent
ROOT = HERE.parents[3]
DATA = HERE / 'run-2026103001'
def digest(p): return hashlib.sha256(p.read_bytes()).hexdigest()
def read(name): return json.loads((DATA / name).read_text())
def rows(name):
    with gzip.open(DATA / name, 'rt', newline='') as f:
        for r in csv.DictReader(f):
            assert None not in r, (name, 'extra CSV fields')
            yield r
def frac(n, d): return {'numerator': n, 'denominator': d, 'ratio': n / d if d else None}
def quantiles(v):
    v = sorted(v)
    return {str(q): v[round((len(v)-1)*q)] for q in (0, .1, .25, .5, .75, .9, 1)}

summary, paired, provenance = read('summary.json'), read('paired-comparisons.json'), read('provenance.json')
scenarios = summary['config']['scenarios']
assert summary['config']['seed'] == 2026103001 and len(scenarios) == 7
assert summary['config']['profiles'] == 100 and summary['config']['repetitions'] == 30
for name, item in provenance['outputs'].items():
    assert digest(DATA / name) == item['sha256'] and (DATA / name).stat().st_size == item['bytes'], name
for path, sha in provenance['sourceSha256'].items(): assert digest(ROOT / path) == sha, path
protected = json.loads((HERE / 'source-state-before.json').read_text())
for path, item in protected['preexistingArtifacts'].items(): assert digest(ROOT / path) == item['sha256'], path

FIELDS = ('actions', 'exits', 'blocked', 'undos', 'shellTaps', 'focusUses', 'paidUndoCoins',
          'coachCueObservations', 'deadEndWarningObservations', 'focusCueObservations', 'manualExits',
          'automatedExits', 'closedGateBlocked', 'pieceBlocked', 'barrierBlocked', 'repeatedErrors',
          'causalCueObservations', 'skillUses', 'skillUnavailable')
def empty(): return {'attempts': 0, 'outcomes': collections.Counter(), 'totals': collections.Counter(), 'arrivals': set(), 'censoredAtExhaustedBudget': 0}
def add(a, r):
    a['attempts'] += 1; a['outcomes'][r['outcome']] += 1
    a['arrivals'].add((r['profileId'], r['repetition']))
    if r['outcome']=='horizon_censored' and int(r['budgetRemaining'])==0 and int(r['remaining'])>0:
        a['censoredAtExhaustedBudget'] += 1
    for k in FIELDS: a['totals'][k] += int(r[k])
def finish(a):
    t, o = a['totals'], a['outcomes']; n = a['attempts']
    return {'attempts': n, 'arrivingTrajectories': len(a['arrivals']), 'outcomes': dict(o), 'totals': dict(t),
            'horizonCensoredAlreadyAtZeroBudget': a['censoredAtExhaustedBudget'],
            'exhaustionSensitivityIncludingZeroBudgetAtHorizon': frac(o['budget_exhausted']+a['censoredAtExhaustedBudget'], n),
            'completionPerStart': frac(o['completed'], n),
            'exhaustionPerStart': frac(o['budget_exhausted'], n),
            'blockedPerAllInteraction': frac(t['blocked'], t['actions']),
            'blockedPerExitOrBlockedTap': frac(t['blocked'], t['exits']+t['blocked']),
            'shellShareAllInteractions': frac(t['shellTaps'], t['actions']),
            'automatedExitShare': frac(t['automatedExits'], t['exits']),
            'repeatedErrorShareOfBlocks': frac(t['repeatedErrors'], t['blocked'])}

all_groups = collections.defaultdict(empty); levels = collections.defaultdict(empty); bands = collections.defaultdict(empty)
selected = {8, 9, 11, 13, 17, 21, 25, 29, 31, 35, 43, 48, 60}
first = collections.defaultdict(dict); level_rows = 0
for r in rows('level-results.csv.gz'):
    level_rows += 1; s, n = r['scenario'], int(r['level'])
    assert int(r['exits']) == int(r['manualExits'])+int(r['automatedExits'])
    assert int(r['blocked']) == sum(int(r[k]) for k in ('closedGateBlocked','pieceBlocked','barrierBlocked'))
    add(all_groups[s], r); add(levels[s, n], r); add(bands[s, (n-1)//10*10+1], r)
    if n in selected and s in ('control_v121', 'cause_immediate'):
        first[n, r['profileId'], r['repetition']].setdefault(s, r)

ts = collections.defaultdict(list); trajectory_rows = 0
for r in rows('trajectories.csv.gz'):
    trajectory_rows += 1; ts[r['scenario']].append(r)
    assert int(r['coinsEnd']) == 180+int(r['coinsEarned'])-int(r['coinsSpent'])
    assert int(r['attempts']) == sum(json.loads(r['outcomes']).values())

block_counts = collections.Counter(); block_cues = collections.Counter(); repeated_counts = collections.Counter()
for r in rows('blocked-events.csv.gz'):
    block_counts[r['scenario']] += 1
    assert r['blockedReason'] in ('CLOSED_GATE','BLOCKED_PATH','LOCKED_BARRIER')
    if int(r['sameStatePieceReasonCount']) > 1: repeated_counts[r['scenario']] += 1
    if int(r['visibleCauseRule']) >= 0: block_cues[r['scenario']] += 1

days = collections.defaultdict(collections.Counter); by_day = collections.defaultdict(collections.Counter); day_rows = 0
for r in rows('day-results.csv.gz'):
    day_rows += 1; s = r['scenario']; n = int(r['decisions']); reason = r['stopReason']
    days[s][reason] += 1; by_day[s, int(r['day'])][reason] += 1
    if reason == 'day_action_cap': assert n == 120 and r['administrativeActionCap'] == 'true'
    if reason == 'repeated_budget_exhaustion': assert int(r['budgetExhaustedAttempts']) == 3

chapter_groups = collections.defaultdict(collections.Counter); chapter_rows = 0
for r in rows('chapter-results.csv.gz'):
    chapter_rows += 1; g = chapter_groups[r['episode'], int(r['capacity']), r['policy']]
    g['runs'] += 1; g[r['outcome']] += 1
    for k in ('actions', 'manualExits', 'automatedExits', 'blocked', 'skillUses', 'skillUnavailable'): g[k] += int(r[k])
stage_groups = collections.defaultdict(collections.Counter); stage_rows = 0; previous = {}
for r in rows('chapter-stage-results.csv.gz'):
    stage_rows += 1; g = stage_groups[r['episode'], int(r['capacity']), r['policy'], int(r['stage']), r['layoutId']]
    g['starts'] += 1; g[r['outcome']] += 1
    before, after = int(r['chargeBefore']), int(r['chargeAfter'])
    assert 0 <= after <= before
    key = tuple(r[k] for k in ('profileId','repetition','episode','capacity','policy')); stage = int(r['stage'])
    if key in previous: assert (stage, before) == (previous[key][0]+1, previous[key][1])
    else: assert stage == 0 and before == int(r['capacity'])
    previous[key] = stage, after
    if r['policy'] == 'manual': assert int(r['skillUses']) == int(r['automatedExits']) == 0

# Exact authored row fields, read as data. No certificate is passed to an agent.
layout_source = ROOT / 'app/src/main/java/com/androidhit/lumadrift/engine/RelayLayouts.java'
raw_layouts = re.findall(r'^\s*"([^"\n]*)",?$', layout_source.read_text(), re.M)
assert len(raw_layouts) == 1000
catalog = []
for n, raw in enumerate(raw_layouts[:400], 1):
    f = raw.split('|'); tiles = [[int(x) for x in t.split(',')] for t in f[4].split(';')]
    walls = [[int(x) for x in b.split(',')] for b in f[5].split(';')] if f[5] else []
    keys = {b[2] for b in walls}; switches = sum(t[3] != 0 for t in tiles)
    # Shell source assigns ICE2/FIRE2/WATER1 to three actual IDs for every U11+.
    catalog.append({'level': n, 'size': int(f[0]), 'pieces': len(tiles), 'closedInitialGates': 4-bin(int(f[1])).count('1'),
                    'switchPieces': switches, 'multiGateSwitches': sum(bin(t[3]).count('1')>1 for t in tiles),
                    'barriers': len(walls), 'distinctKeys': len(keys), 'extraShellTaps': 5 if n>=11 else 0,
                    'shellKinds': ['ICE','FIRE','WATER'] if n>=11 else [], 'mistakeAllowance': int(f[3]),
                    'authoredDecisionPoints': int(f[7]), 'authoredForcedOptimalActions': int(f[8]),
                    'authoredMaxOneChoicePenalty': int(f[9]), 'authoredDependencyDepth': int(f[10]),
                    'authoredStructuralScore': int(f[11]), 'authoredDifficulty': ['NORMAL','HARD','EXPERT'][int(f[12])],
                    'authoredBeat': ['INTRODUCTION','PRACTICE','PEAK','RECOVERY'][int(f[13])], 'lesson': f[14]})

out = {'schemaVersion': 1, 'kind': 'new_engine_run_descriptive_audit', 'runId': summary['runId'],
       'sourceCommitBeforeRun': protected['sourceCommit'], 'seed': 2026103001,
       'method': {'cohort': '100 generated profiles x30 repetitions x7 paired conditions. Seed resamples profiles as well as stochastic decisions; not independent human participants.',
                  'fractions': 'Exact pooled numerators/denominators. Summary and paired comparisons instead use mean per-trajectory metrics; do not substitute these denominators.',
                  'arrivals': 'Distinct profile/repetition pairs actually reaching each level; later levels are selected survivors, not a complete-cohort difficulty trial.',
                  'commonFirstAttempts': 'First attempt among matched profile/repetition pairs reaching the level in both conditions; common-arrival selection and different prior paths remain.',
                  'stops': 'Daily utility and availability formulas are assumptions. Administrative daily caps and horizon censoring are not spontaneous player dropout. Boundary audit: some horizon-censored attempts already have zero budget and pieces remaining. Original outcome labels are preserved; exhaustionSensitivityIncludingZeroBudgetAtHorizon separately includes those known-zero-budget rows.',
                  'limitedPerception': 'Bounded fallible mental rule model; no hidden solution supplied to search. Control still has explicit native U1 coach, earned Focus and dead-end warning.',
                  'oracleBoundary': 'No new omniscient player cohort executed. Native certified solutions may serve explicitly activated GUIDE/TRAIL/PULSE/FLOW/resonance and existing native help; automated exits and cue exposures retained separately.',
                  'catalog': 'Read frozen RelayLayouts data and ElementalShell121 assignment; authored structural scores are not calibrated human difficulty. Native visual previews/key inspection are excluded by runner.',
                  'quantiles': 'Nearest sorted index round((n-1)*q), Python ties-to-even.',
                  'limits': 'No rendered UI, sprite identification, emotion, human comprehension, enjoyment, retention or clinical/IQ calibration measured.'},
       'counts': {'campaignTrajectories': trajectory_rows, 'attemptRows': level_rows, 'blockedEvents': sum(block_counts.values()),
                  'configuredDays': day_rows, 'cueFreeProbes': sum(r['probes'] for r in paired['transferWithoutCues']['scenarios']),
                  'chapterRuns': chapter_rows, 'chapterStages': stage_rows, 'humanParticipants': 0},
       'scenarios': {}, 'commonFirstAttempts': {}, 'catalogU1U400': catalog,
       'pairedComparisons': paired['comparisons'], 'transferWithoutCues': paired['transferWithoutCues'],
       'chapterConditions': [{'episode': e, 'capacity': c, 'policy': p, **dict(v)} for (e,c,p),v in chapter_groups.items()],
       'chapterStageLayouts': [{'episode': e, 'capacity': c, 'policy': p, 'stage': st, 'layout': l, **dict(v)} for (e,c,p,st,l),v in stage_groups.items()],
       'sourceProvenance': {'path': str((DATA/'provenance.json').relative_to(ROOT)), 'sha256': digest(DATA/'provenance.json'),
                            'sourceCount': len(provenance['sourceSha256']), 'allCurrentSourcesMatch': True,
                            'protectedPreexistingFiles': len(protected['preexistingArtifacts']), 'allProtectedBytesUnchanged': True,
                            'runElapsedSeconds': provenance['elapsedSeconds']}}
for s in scenarios:
    a = finish(all_groups[s]); z = next(x for x in summary['scenarios'] if x['id'] == s); trajectories = ts[s]
    assert len(trajectories) == 3000 and sum(days[s].values()) == 24000
    assert a['attempts'] == z['totals']['attempts'] and a['outcomes'] == z['mechanics']['outcomes']
    assert a['totals']['blocked'] == block_counts[s] == z['totals']['blocked']
    assert a['totals']['repeatedErrors'] == repeated_counts[s]
    assert a['totals']['actions'] == z['totals']['actions']
    a['trajectoryQuantiles'] = {k: quantiles([float(r[k]) for r in trajectories]) for k in ('levelsCleared','actions','blocked','coinsSpent','energySpent','meanKnowledge')}
    a['stopsPer24000ConfiguredDays'] = dict(days[s]); a['dayStopCounts'] = {str(d): dict(by_day[s,d]) for d in range(8)}
    a['trajectoryStopExposure'] = {reason: frac(sum(reason in json.loads(r['stopReasons']) for r in trajectories), 3000) for reason in days[s]}
    a['blockedWithPriorCauseVisible'] = frac(block_cues[s], block_counts[s])
    a['perLevel'] = {str(n): finish(v) for (condition,n),v in levels.items() if condition == s}
    a['tenLevelBands'] = {f'{n}-{n+9}': finish(v) for (condition,n),v in bands.items() if condition == s}
    a['highestAttemptedLevel'] = max(int(n) for n in a['perLevel'])
    a['reachAtSelectedLevels'] = {str(n): frac(len(levels[s,n]['arrivals']), 3000) for n in sorted(selected)}
    a['u43u48ExhaustionShare'] = frac(sum(levels[s,n]['outcomes']['budget_exhausted'] for n in (43,48)), a['outcomes'].get('budget_exhausted',0))
    out['scenarios'][s] = a
for n in sorted(selected):
    matched = [v for (level,_,_),v in first.items() if level == n and len(v) == 2]
    out['commonFirstAttempts'][str(n)] = {}
    for s in ('control_v121','cause_immediate'):
        g=empty()
        for v in matched: add(g,v[s])
        out['commonFirstAttempts'][str(n)][s] = finish(g)
by_profile=collections.defaultdict(dict)
for p in summary['profileResults']: by_profile[p['profileId']][p['scenario']]=p
out['profilePairedImmediate']={}
for metric in ('meanLevelsCleared','blockedRate','completionRate','meanKnowledge'):
    diffs=[v['cause_immediate'][metric]-v['control_v121'][metric] for v in by_profile.values()]
    out['profilePairedImmediate'][metric]={'profiles':100,'positive':sum(x>1e-12 for x in diffs),'negative':sum(x< -1e-12 for x in diffs),'zero':sum(abs(x)<=1e-12 for x in diffs),'quantiles':quantiles(diffs)}
assert trajectory_rows==21000 and day_rows==168000 and chapter_rows==54000
assert out['counts']['cueFreeProbes']==84000
target=HERE/'analysis.json';target.write_text(json.dumps(out,ensure_ascii=False,indent=2)+'\n')
receipt={'status':'PASS_WITH_LIMITATION','analysisSha256':digest(target),'counts':out['counts'],'provenance':out['sourceProvenance'],
         'protocolIssue': {'kind':'horizon_label_on_already_exhausted_attempt', 'rows':sum(a['censoredAtExhaustedBudget'] for a in all_groups.values()),
                           'byScenario':{s:a['censoredAtExhaustedBudget'] for s,a in all_groups.items()},
                           'rawOutputsModified':False,'sourceModified':False,'treatment':'Preserve original labels and report separate exhaustion sensitivity.'}}
(HERE/'analysis-validation.json').write_text(json.dumps(receipt,indent=2)+'\n')
print(json.dumps(receipt,indent=2))
