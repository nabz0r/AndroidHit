from pathlib import Path
import json,hashlib,csv,statistics,struct,datetime,collections,sys
root=Path('/Users/n/androidhit');label=sys.argv[1];out=root/'docs/validation/v1.14/ui-bugs'/('ui-sdk-'+label);host=out/'host-report.json';r=json.loads(host.read_text())
for flag in ['passed','restored','isolated_test_package_removed','rotation_settings_restored','installed_apk_matches_expected_after_run']:assert r[flag],flag
assert r['apk_sha256']=='cbc74c3214f3c64c888f34b9452be162d3e3e168f1c218d1a4537410ef005ff0'
assert r['test_apk_sha256']=='474477eac3f613834b78c66a90001e280e1ba6bf88e3bfafdfcd2a0d2f6f95ea'
assert r['journey_from']==801 and r['journey_through']==1000 and r['batch_size']==100 and r['catalog_family']=='N' and not r['queued_input'] and len(r['phases'])==2
sp=r['synthetic_unlock_prerequisite'];proofpath=out/sp['proof_file'];proofraw=proofpath.read_bytes();assert hashlib.sha256(proofraw).hexdigest()==sp['proof_sha256'];proof=json.loads(proofraw)
assert proof['passed'] and proof['delta_count']==1 and proof['key']=='unlocked' and proof['after_value']==801 and proof['before']['raw_xml_sha256']==r['backup_manifest']
before=proof['before']['values'];after=proof['after']['values'];expected=json.loads(json.dumps(before));expected['luma_drift_save_v1.xml']['unlocked']={'type':'Integer','value':801};assert after==expected
assert proof['before']['raw_xml_sha256'].keys()==proof['after']['raw_xml_sha256'].keys()
for name,sha in proof['before']['raw_xml_sha256'].items():
 if name!=proof['preference_file']:assert proof['after']['raw_xml_sha256'][name]==sha
levels=[];timings=[];pids=[];fixture=root/'docs/validation/v1.14/native-sdk-board-ready-rc2/runner-source/assets/fixtures';readiness=[]
for phase in r['phases']:
 native=phase['native'];assert native['passed'] and native['syntheticUnlockPrerequisite'] and all(c['pass'] for c in native['checks']);pids.append(native['processId']);name=phase['phase'];obs=json.loads((out/name/'journey-observations.json').read_text());assert obs['syntheticUnlockPrerequisite'];obs=obs['levels'];assert [x['level'] for x in obs]==list(range(phase['journey_from'],phase['journey_through']+1));actions=json.loads((out/name/'actions.json').read_text());events=[x for x in actions if x.get('observation')=='typed-action-timing' and not x.get('diagnostic')];by=collections.defaultdict(list)
 for x in events:by[x['level']].append(x)
 ready=[x for x in actions if x.get('observation')=='journey-board-readiness'];assert [x['level'] for x in ready]==[x['level'] for x in obs]
 for x in ready:assert x['observedPieces']==x['expectedPieces'] and x['timeoutMs']==4000 and x['waitMs']<=4500
 readiness.extend(ready)
 for x in obs:
  n=x['level'];f=json.loads((fixture/f'n{n}.json').read_text());e=by[n];assert x['mode']==0 and x['budgetPolicy']==4 and x['contentVersion']==6 and x['runId'];assert x['starsBefore']==0 and x['starsAfter']==3 and x['savedBudgetUsedAtWin']==len(f['steps'])==x['certifiedProductiveActions'];assert x['observedInitialPieces']==f['pieces'];assert [a['action'] for a in e]==[s['action'] for s in f['steps']];assert len(e)==len(f['steps']);prefix=[]
  for index,a in enumerate(e):
   prefix.append(str(a['action']));assert a['budgetBefore']==index and a['budgetAfter']==index+1 and a['historyAfter']==','.join(prefix)
  x['sourcePhase']=name;levels.append(x)
 timings.extend(events)
assert [x['level'] for x in levels]==list(range(801,1001));assert len({x['runId'] for x in levels})==200
assert all(x['package_absent_before_restart'] and x['force_stop'] for x in r['batch_process_boundaries']);assert pids[0]!=pids[1]
for key,sha in r['screenshots'].items():
 p=root/'docs/screenshots/v1.14/ui-bugs'/('ui-sdk-'+label)/key;raw=p.read_bytes();assert hashlib.sha256(raw).hexdigest()==sha and struct.unpack('>II',raw[16:24])==(1080,2340)
fields=['level','mode','budgetPolicy','contentVersion','runId','fixtureGridSize','observedInitialPieces','fixtureMinimum','fixtureAllowance','fixtureMargin','certifiedProductiveActions','manualGateActions','switchPieceExits','automatedSolveDurationMs','entryUiDurationMs','savedElapsedAtWinMs','savedBudgetUsedAtWin','starsBefore','starsAfter','coinsBefore','coinsAfter','coinDelta','sourcePhase']
with(out/'journey801-1000.csv').open('w',newline='') as f:
 w=csv.DictWriter(f,fieldnames=fields,extrasaction='ignore');w.writeheader();w.writerows(levels)
groups=collections.defaultdict(list)
for t in timings:groups[t['kind']].append(t['durationMs'])
log=Path('/tmp/luma-v114-coverage801-1000-d-run.txt');start=getattr(log.stat(),'st_birthtime',None);end=host.stat().st_mtime
s={'scope':'Separate real SDK Journey801..1000 after an expressly synthetic sole unlocked=801 prerequisite. Not organically earned continuity from1, not an overall1000marathon PASS, not human solve time or a fun test. All200 real touchscreen action histories, finite-budget charges, fresh stars and native victories independently rechecked here after global D PASS/restoration.','passed':True,'restored':True,'recorded_at_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'source_host_report_sha256':hashlib.sha256(host.read_bytes()).hexdigest(),'synthetic_unlock_prerequisite':sp,'apk_sha256':r['apk_sha256'],'test_apk_sha256':r['test_apk_sha256'],'native_checks':r['native']['checkCount'],'levels_cleared':200,'three_star_victories':200,'productive_actions':len(timings),'manual_gate_actions':sum(x['manualGateActions'] for x in levels),'switch_piece_exits':sum(x['switchPieceExits'] for x in levels),'normal_solve_duration_ms':sum(x['automatedSolveDurationMs'] for x in levels),'entry_ui_duration_ms':sum(x['entryUiDurationMs'] for x in levels),'original_full_frame_count':len(r['screenshots']),'actual_distinct_process_ids':pids,'grid_distribution':dict(collections.Counter(x['fixtureGridSize'] for x in levels)),'readiness_wait_ms':{'max':max(x['waitMs'] for x in readiness),'total':sum(x['waitMs'] for x in readiness),'poll_timeout':4000},'timing_by_action_kind':{k:{'count':len(v),'total_ms':sum(v),'mean_ms':statistics.mean(v),'median_ms':statistics.median(v)} for k,v in groups.items()},'host_wall_time_basis':'Host stdout log creation to final host-report write including setup, captures, batches and cleanup.','host_log_created_utc':datetime.datetime.fromtimestamp(start,datetime.timezone.utc).isoformat() if start else None,'host_report_written_utc':datetime.datetime.fromtimestamp(end,datetime.timezone.utc).isoformat(),'host_wall_duration_seconds':end-start if start else None}
(out/'seeded200-summary.json').write_text(json.dumps(s,indent=2)+'\n');print(json.dumps({k:s[k] for k in ['passed','restored','native_checks','levels_cleared','productive_actions','original_full_frame_count','normal_solve_duration_ms','host_wall_duration_seconds']},indent=2))
