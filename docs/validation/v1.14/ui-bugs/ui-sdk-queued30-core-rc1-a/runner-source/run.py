#!/usr/bin/env python3
"""Run SDK Instrumentation against the isolated QA emulator; restore original prefs exactly."""
from pathlib import Path
from storage_fault import StorageFaultController
import argparse, hashlib, json, os, re, shutil, subprocess, tempfile, time, traceback, xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[2]
PACKAGE='com.androidhit.lumadrift.sandbox'
TEST_PACKAGE='com.androidhit.lumadrift.learningtest'
RUNNER=TEST_PACKAGE+'/com.androidhit.lumadrift.qa.v114.CandidateUiInstrumentation'
REMOTE='/sdcard/Android/data/'+PACKAGE+'/files/v114-sdk-ui'

def main():
 global PACKAGE,RUNNER,REMOTE
 p=argparse.ArgumentParser();p.add_argument('--serial',default='emulator-5580');p.add_argument('--cases',default='house-smoke');p.add_argument('--label',default='pilot');p.add_argument('--test-apk');p.add_argument('--expected-apk');p.add_argument('--journey-through',type=int,default=100);p.add_argument('--variant',choices=['lab','production'],default='lab');p.add_argument('--compact',action='store_true');p.add_argument('--capture-all',action='store_true');p.add_argument('--journey-from',type=int,default=1);p.add_argument('--batch-size',type=int,default=0);p.add_argument('--bulk',action='store_true');p.add_argument('--bulk-cache',action='store_true');p.add_argument('--queued-input',action='store_true');p.add_argument('--capture-first',type=int,default=30);p.add_argument('--house-locale-count',type=int,default=6);p.add_argument('--exercise-budget-failure',action='store_true');p.add_argument('--catalog-family',choices=['N','L'],default='N');p.add_argument('--upgrade-gate');p.add_argument('--old-apk');p.add_argument('--expected-version',default='1.14.0-sandbox-rc1');p.add_argument('--expected-code',type=int,default=15);a=p.parse_args()
 if a.variant=='production':
  production_cases=set(a.cases.split(','));bounded={'avatar-visual','journey','menus','gestures','recovery','shop-focus','house-smoke','house-locales','house-layout'}
  if a.cases!='startup' and (not production_cases<=bounded or ('avatar-visual' not in production_cases and not production_cases<= {'shop-focus','house-smoke','house-locales','house-layout'})):raise SystemExit('Production permits startup, bounded house/shops browsing or earned visual qualification only')
  PACKAGE='com.androidhit.lumadrift';runner_class='ProductionStartupInstrumentation' if a.cases=='startup' else 'CandidateUiInstrumentation';RUNNER=TEST_PACKAGE+'/com.androidhit.lumadrift.qa.v114.'+runner_class;REMOTE='/sdcard/Android/data/'+PACKAGE+'/files/v114-sdk-ui'
 elif 'startup' in a.cases.split(','):raise SystemExit('Startup observer is production-only')
 if not a.test_apk:a.test_apk=str(ROOT/'app/build/qualification-artifacts/v1.14'/('learning-'+a.variant+'-rc1-test.apk'))
 allowed={'tutorial','gestures','map','challenge','journey','recovery','powers','landscape','startup','guardians','hub','avatar','story','menus','rewards','styles','languages','avatar-visual','shop-focus','shop-locales','power-return','house-smoke','house','house-locales','house-layout','firstlight','puzzle-locales','level15-audit','learning-core','legacy-upgrade'}
 pending={'event-retry','guardian-storage-fault'}
 requested=set(a.cases.split(','))
 if requested & pending:raise SystemExit('UI cases await the stable1.10 contract: '+','.join(sorted(requested & pending)))
 if not requested<=allowed or not a.cases:raise SystemExit('Unknown or empty UI case set')
 if a.upgrade_gate and (a.cases!='legacy-upgrade' or a.variant!='lab' or not a.old_apk):raise SystemExit('Upgrade gate requires Lab-only legacy-upgrade and a frozen --old-apk')
 if 'legacy-upgrade'in requested and not a.upgrade_gate:raise SystemExit('Legacy upgrade requires explicit old APK and release gate')
 if not 1<=a.journey_through<=1000:raise SystemExit('journey-through must be between1 and1000')
 if not 1<=a.journey_from<=a.journey_through or not 0<=a.batch_size<=100 or not 0<=a.capture_first<=1000 or not 1<=a.house_locale_count<=6:raise SystemExit('Invalid bounded journey range/batch/capture settings')
 if a.queued_input and not(a.bulk and a.bulk_cache):raise SystemExit('Queued input requires --bulk --bulk-cache and remains confined to verified Journey touches')
 if a.batch_size and requested!={'journey'}:raise SystemExit('Batching is Journey-only so each batch has explicit earned-continuation scope')
 if (a.serial,a.variant) not in {('emulator-5580','lab'),('emulator-5586','production')} or not re.fullmatch(r'[a-zA-Z0-9_-]{1,40}',a.label):raise SystemExit('Owned5580 Lab or explicitly owned5586 production-only target and safe label required')
 adb=str(Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'))/'platform-tools/adb')
 def cmd(*parts,timeout=60):return subprocess.check_output([adb,'-s',a.serial,*parts],stderr=subprocess.STDOUT,timeout=timeout)
 def shell(*parts):return cmd('shell',*parts).decode(errors='replace')
 def files():
  result=shell('su','0','ls','-1',prefdir).splitlines()
  if any(not re.fullmatch(r'[A-Za-z0-9_.-]+',n) for n in result):raise AssertionError('Unsafe or incomplete preference file list')
  return result
 def upgrade_release():
  gate=Path(a.upgrade_gate);deadline=time.monotonic()+1800
  print(json.dumps({'milestone':'Real RC2 L15 checkpoint ready; waiting for frozen new APK gate','gate':str(gate)}),flush=True)
  (out/'waiting-for-release.json').write_text(json.dumps({'scope':'Original guest backup held; real RC2 checkpoint retained; no upgrade occurred yet','gate':str(gate)},indent=2)+'\n')
  while not gate.is_file():
   if time.monotonic()>deadline:raise TimeoutError('Frozen upgrade gate did not arrive in1800 seconds; original guest will be restored')
   time.sleep(1)
  release=json.loads(gate.read_text());assert release['version']==a.expected_version and release['code']==a.expected_code,'Upgrade gate version/code mismatch'
  candidate=Path(release['apk']);assert candidate.is_file() and hashlib.sha256(candidate.read_bytes()).hexdigest()==release['sha256'],'Upgrade candidate differs from its explicit gate hash'
  if release.get('source_freeze'):
   freeze=Path(release['source_freeze']);assert hashlib.sha256(freeze.read_bytes()).hexdigest()==release['source_freeze_sha256'],'Upgrade source-freeze proof changed'
  report['upgrade_gate_validated']=release
  shell('am','force-stop',PACKAGE);before={name:cmd('exec-out','su','0','cat',prefdir+'/'+name)for name in files()}
  install=cmd('install','-r',str(candidate)).decode();(out/'upgrade-install.txt').write_text(install);assert 'Success'in install
  report['upgrade_install_succeeded']=True
  after={name:cmd('exec-out','su','0','cat',prefdir+'/'+name)for name in files()};assert before==after,'APK installation changed saved data before first new launch'
  actual_path=shell('pm','path',PACKAGE).strip().removeprefix('package:');actual_hash=shell('sha256sum',actual_path).split()[0];assert actual_hash==release['sha256'],'Installed upgrade bytes differ'
  report['upgrade']={'release_gate':release,'installed_apk_sha256':actual_hash,'checkpoint_preferences_unchanged_before_first_new_launch':True,'checkpoint_pref_sha256':{name:hashlib.sha256(value).hexdigest()for name,value in before.items()},'app_installation_intentionally_retained':'The explicitly requested1.14 update remains installed; original preference files are restored in final cleanup, without downgrading'}
  print(json.dumps({'milestone':'Exact new APK installed; original checkpoint bytes unchanged before new launch','sha256':actual_hash}),flush=True)
 out=ROOT/'docs/validation/v1.14/ui-bugs'/('ui-sdk-'+a.label);shots=ROOT/'docs/screenshots/v1.14/ui-bugs'/('ui-sdk-'+a.label);
 if out.exists() or shots.exists():raise SystemExit('Use a fresh label: output already exists')
 out.mkdir(parents=True);shots.mkdir(parents=True)
 report={'passed':False,'restored':False,'scope':'Native SDK Instrumentation, AccessibilityNodeProvider actions and touchscreen events from observed native node bounds; no browser/desktop/shell input/profile injection','cases':a.cases,'journey_from':a.journey_from,'journey_through':a.journey_through,'batch_size':a.batch_size,'bulk':a.bulk,'bulk_cache':a.bulk_cache,'queued_input':a.queued_input,'catalog_family':a.catalog_family,'serial':a.serial,'capture_policy':'original-full-frames-plus-labelled-unscaled-analysis-crops' if 'avatar-visual' in requested else ('all' if a.capture_all else 'curated-original-milestones')}
 prefdir='/data/user/0/'+PACKAGE+'/shared_prefs';backup=Path(tempfile.mkdtemp(prefix='luma-v114-ui-original-prefs-'));original={};ready=False;owner='';error=None;rotation=None;display_size=None;fault=None;test_installed=False
 try:
  assert shell('getprop','ro.kernel.qemu').strip()=='1','QA emulator only'
  expected_avd='hexkeep-play-36-16k' if a.serial=='emulator-5580' else 'Luma-Release111-K'
  assert cmd('emu','avd','name').decode().splitlines()[0].strip()==expected_avd,'Exact owned AVD identity required'
  assert shell('getconf','PAGESIZE').strip()=='16384','Designated QA runtime must use16KiB pages'
  rotation={key:shell('settings','get','system',key).strip() for key in ['user_rotation','accelerometer_rotation']};report['rotation_before']=rotation
  details=shell('dumpsys','package',PACKAGE);version_name='1.13.0-sandbox-rc2'if a.upgrade_gate else a.expected_version;code=14 if a.upgrade_gate else a.expected_code;assert ('versionName='+version_name) in details and ('versionCode='+str(code)+' ') in details,'Install the explicitly selected frozen version/code first'
  if a.compact or 'house-layout' in requested:display_size=shell('wm','size').strip();report['display_size_before']=display_size
  shell('am','force-stop',PACKAGE)
  owner=shell('su','0','stat','-c','%u:%g','/data/user/0/'+PACKAGE).strip();assert re.fullmatch(r'\d+:\d+',owner)
  for name in files():
   value=cmd('exec-out','su','0','cat',prefdir+'/'+name);original[name]=value;(backup/name).write_bytes(value)
  ready=True
  for raw in original.values():
   for node in ET.fromstring(raw):
    if node.get('name')=='previously_signed_in' and node.get('value')=='true':raise AssertionError('Refuses a stored signed-in Google identity; guest-only test')
  apkpath=shell('pm','path',PACKAGE).strip().removeprefix('package:');report['apk_sha256']=shell('sha256sum',apkpath).split()[0]
  expected=Path(a.old_apk) if a.upgrade_gate else (Path(a.expected_apk) if a.expected_apk else ROOT/'releases'/('luma-drift-'+version_name+'.apk'))
  assert expected.is_file(),'Frozen expected APK required: '+str(expected)
  report['expected_apk_sha256']=hashlib.sha256(expected.read_bytes()).hexdigest()
  assert report['apk_sha256']==report['expected_apk_sha256'],'Installed APK differs from the frozen candidate'
  report.update({'test_apk_sha256':hashlib.sha256(Path(a.test_apk).read_bytes()).hexdigest(),'wrapper_source_sha256':hashlib.sha256(Path(__file__).read_bytes()).hexdigest(),'storage_fault_source_sha256':hashlib.sha256((ROOT/'scripts/v114-ui-test/storage_fault.py').read_bytes()).hexdigest() if 'guardian-storage-fault' in a.cases.split(',') else None,'runner_source_sha256':hashlib.sha256((ROOT/'scripts/v114-ui-test/java/com/androidhit/lumadrift/qa/v114'/('ProductionStartupInstrumentation.java' if a.cases=='startup' else 'CandidateUiInstrumentation.java')).read_bytes()).hexdigest(),'package':PACKAGE,'backup_location':str(backup),'backup_manifest':{n:hashlib.sha256(v).hexdigest() for n,v in original.items()},'device':{'api':shell('getprop','ro.build.version.sdk').strip(),'abi':shell('getprop','ro.product.cpu.abi').strip(),'page_size':shell('getconf','PAGESIZE').strip(),'density':shell('wm','density').strip(),'size':shell('wm','size').strip()}})
  crash_before=shell('logcat','-d','-b','crash')
  prior_test=subprocess.run([adb,'-s',a.serial,'shell','pm','path',TEST_PACKAGE],capture_output=True,text=True,timeout=30);assert prior_test.returncode in (0,1) and not prior_test.stdout.strip(),'Isolated hometest package must initially be absent'
  install=cmd('install','-r',a.test_apk).decode();(out/'test-apk-install.txt').write_text(install);assert 'Success' in install
  test_installed=True;test_path=shell('pm','path',TEST_PACKAGE).strip().removeprefix('package:');report['installed_test_apk_sha256']=shell('sha256sum',test_path).split()[0];assert report['installed_test_apk_sha256']==report['test_apk_sha256'],'Installed isolated native test APK differs from the frozen file'
  shell('su','0','rm','-rf',REMOTE)
  if 'guardian-storage-fault' in a.cases.split(','):
   assert a.variant=='lab','Storage fault is restricted to the current guest Lab QA release'
   fault=StorageFaultController(adb,a.serial,prefdir,REMOTE,out);fault.start()
  phases=['prepare','cold'] if {'avatar-visual','house','learning-core'} & requested else ['prepare']
  if a.upgrade_gate:phases=['legacy-prepare','upgrade-verify','legacy-cold','learning-cold']
  batch_ranges={}
  if a.batch_size:
   phases=[]
   for start in range(a.journey_from,a.journey_through+1,a.batch_size):
    end=min(a.journey_through,start+a.batch_size-1);phase=f'batch-{start:04d}-{end:04d}';phases.append(phase);batch_ranges[phase]=(start,end)
   report['batching_scope']='Single original guest backup; actual earned progress retained between distinct native batch processes; original restored only in final cleanup'
  phase_results=[];report['phases']=phase_results;report['screenshots']={}
  previous_pid=None
  for phase in phases:
   if a.upgrade_gate and phase=='upgrade-verify':upgrade_release()
   if a.upgrade_gate and phase!='legacy-prepare':
    previous_pid=phase_results[-1]['native'].get('processId');shell('am','force-stop',PACKAGE)
    probe=subprocess.run([adb,'-s',a.serial,'shell','pidof',PACKAGE],capture_output=True,text=True,timeout=30);assert probe.returncode in (0,1) and not probe.stdout.strip(),'Upgrade cold target survives force-stop'
    report.setdefault('upgrade_process_boundaries',[]).append({'phase':phase,'previous_process_id':previous_pid,'force_stop':True,'package_absent_before_restart':True})
    for name in shell('su','0','ls','-1',REMOTE).splitlines():
     assert re.fullmatch(r'[A-Za-z0-9_.-]+',name),'Unsafe test-output filename'
     if 'checkpoint'not in name:shell('su','0','rm',REMOTE+'/'+name)
   if phase in batch_ranges:
    previous_pid=phase_results[-1]['native'].get('processId') if phase_results else None
    shell('am','force-stop',PACKAGE)
    probe=subprocess.run([adb,'-s',a.serial,'shell','pidof',PACKAGE],capture_output=True,text=True,timeout=30)
    assert probe.returncode in (0,1) and not probe.stdout.strip(),'Batch target survives force-stop'
    shell('su','0','rm','-rf',REMOTE)
    report.setdefault('batch_process_boundaries',[]).append({'batch':phase,'previous_process_id':previous_pid,'force_stop':True,'package_absent_before_restart':True})
   if phase=='cold':
    assert phase_results[-1]['native'].get('visualCheckpointReady') or phase_results[-1]['native'].get('houseCheckpointReady') or phase_results[-1]['native'].get('learningCheckpointReady'),'Prepare produced no valid checkpoint; cold verification cannot fabricate a prior identity'
    previous_pid=phase_results[-1]['native'].get('processId');shell('am','force-stop',PACKAGE)
    probe=subprocess.run([adb,'-s',a.serial,'shell','pidof',PACKAGE],capture_output=True,text=True,timeout=30);assert probe.returncode in (0,1) and not probe.stdout.strip(),'App process survives force-stop'
    report['cold_process_boundary']={'force_stop':True,'package_absent_from_pidof_before_restart':True,'previous_instrumented_process_id':previous_pid}
   phase_cases=('learning-core'if'learning-core'in requested else 'house' if 'house' in requested else 'avatar-visual') if phase=='cold' else a.cases
   if a.upgrade_gate:phase_cases={'legacy-prepare':'journey,legacy-upgrade-prepare','upgrade-verify':'legacy-upgrade-verify','legacy-cold':'legacy-upgrade-cold','learning-cold':'learning-core'}[phase]
   batch_from,batch_through=batch_ranges.get(phase,(a.journey_from,a.journey_through))
   active_version='1.13.0-sandbox-rc2'if a.upgrade_gate and phase=='legacy-prepare'else a.expected_version;active_code=14 if a.upgrade_gate and phase=='legacy-prepare'else a.expected_code
   active_family='L'if a.upgrade_gate and phase=='legacy-prepare'else a.catalog_family
   if a.upgrade_gate and phase=='legacy-prepare':batch_from,batch_through=1,14
   run=cmd('shell','am','instrument','-w','-r','-e','cases',phase_cases,'-e','journeyFrom',str(batch_from),'-e','journeyThrough',str(batch_through),'-e','catalogFamily',active_family,'-e','bulk',str(a.bulk).lower(),'-e','bulkCache',str(a.bulk_cache).lower(),'-e','queuedInput',str(a.queued_input).lower(),'-e','captureFirst',str(a.capture_first),'-e','houseLocaleCount',str(a.house_locale_count),'-e','exerciseBudgetFailure',str(a.exercise_budget_failure).lower(),'-e','compact',str(a.compact).lower(),'-e','captureAll',str(a.capture_all).lower(),'-e','visualPhase',('cold'if phase=='learning-cold'else'prepare'if phase in batch_ranges or a.upgrade_gate else phase),'-e','expectedVersion',active_version,'-e','expectedCode',str(active_code),'-e','storageFaultInfrastructure',str(fault is not None).lower(),RUNNER,timeout=1800).decode(errors='replace')
   phase_out=out/phase;phase_shots=shots/phase;phase_out.mkdir();phase_shots.mkdir();(phase_out/'instrumentation.txt').write_text(run)
   with tempfile.TemporaryDirectory(prefix='luma-v114-ui-captures-') as tmp:
    cmd('pull',REMOTE,tmp);pulled=Path(tmp)/'v114-sdk-ui'
    if not (pulled/'report.json').is_file():
     orphan=[]
     for file in pulled.iterdir():
      if not file.is_file():continue
      dest=(phase_shots if file.suffix=='.png' else phase_out)/file.name;shutil.copyfile(file,dest)
      if file.suffix=='.png':orphan.append({'file':phase+'/'+file.name,'sha256':hashlib.sha256(file.read_bytes()).hexdigest(),'scope':'Unedited SDK original from a crashed/incomplete process; no native pass report'})
     report['incomplete_native_originals']=orphan
     raise AssertionError('Native instrumentation produced no report; original SDK files and any crash delta are retained')

    native=json.loads((pulled/'report.json').read_text());capture_names=set(native.get('captures',[]))
    for file in pulled.iterdir():
     if file.suffix=='.png':
      if file.stem not in capture_names:continue
      dest=phase_shots/file.name
     else:dest=phase_out/file.name
     shutil.copyfile(file,dest)
    hashes={phase+'/'+f.name:hashlib.sha256(f.read_bytes()).hexdigest() for f in phase_shots.glob('*.png')};report['screenshots'].update(hashes);report['capture_kind_counts']={'original_full_frames':sum(not name.endswith('-analysis-crop.png') for name in report['screenshots']),'unscaled_analysis_crops':sum(name.endswith('-analysis-crop.png') for name in report['screenshots'])}
    phase_results.append({'phase':phase,'journey_from':batch_from,'journey_through':batch_through,'native':native,'screenshots':hashes})
    (out/'host-progress.json').write_text(json.dumps({'scope':'Incomplete until final host-report passed and restored; no pass is inferred from ongoing batches','completed_batches':len(phase_results),'planned_batches':len(phases),'phases':[{'phase':x['phase'],'passed':x['native'].get('passed'),'checks':x['native'].get('checkCount')}for x in phase_results]},indent=2)+'\n')
   if a.upgrade_gate and phase!='legacy-prepare':
    assert native.get('processId') and native.get('processId')!=previous_pid,'Upgrade continuation requires a distinct real process'
    report['upgrade_process_boundaries'][-1]['restarted_process_id']=native.get('processId')
   if phase in batch_ranges:
    assert native.get('processId') and native.get('processId')!=previous_pid,'Batch must execute in a distinct real target process'
    report['batch_process_boundaries'][-1]['restarted_process_id']=native.get('processId')
   if phase=='cold':
    report['cold_process_boundary']['restarted_instrumented_process_id']=native.get('processId')
    assert native.get('processId') and native.get('processId')!=previous_pid,'Cold phase did not observe a distinct actual target process'
   if not native.get('passed') and not native.get('visualCheckpointReady') and not native.get('houseCheckpointReady'):raise AssertionError('Prepare failed before persistent checkpoint; raw failure preserved')
  crash_after=shell('logcat','-d','-b','crash');crash_delta=crash_after[len(crash_before):] if crash_after.startswith(crash_before) else crash_after
  if crash_delta.strip():(out/'native-crash-delta.txt').write_text(crash_delta)
  report['native']={'passed':all(x['native'].get('passed') for x in phase_results),'checks':[c for x in phase_results for c in x['native'].get('checks',[])],'checkCount':sum(x['native'].get('checkCount',0) for x in phase_results),'scope':('Real RC2 checkpoint, APK upgrade and separate quota cold processes; no seeding'if a.upgrade_gate else 'Real earned-progress batches in distinct processes, original guest restored after all batches'if a.batch_size else 'Separate real prepare and cold processes; observed raster deltas plus persistent identities; no seeding' if len(phase_results)>1 else 'Actual SDK single-process observation; no profile seeding')}
  assert not crash_delta.strip(),'New native crash buffer entries; inspect native-crash-delta.txt'
  report['passed']=report['native']['passed'];assert report['passed'],'Instrumentation checks failed; inspect phase reports/actions and original images'
 except BaseException as failure:
  error=failure;report['error']=str(failure);report['traceback']=traceback.format_exc()
  if 'crash_before' in locals():
   try:
    crash_after=shell('logcat','-d','-b','crash');crash_delta=crash_after[len(crash_before):] if crash_after.startswith(crash_before) else crash_after
    if crash_delta.strip():(out/'native-crash-delta.txt').write_text(crash_delta);report['new_native_crash_entries']=True
   except BaseException as capture_failure:report['crash_capture_error']=str(capture_failure)

 finally:
  if test_installed:
   try:
    removed='Success' in cmd('uninstall',TEST_PACKAGE).decode();probe=subprocess.run([adb,'-s',a.serial,'shell','pm','path',TEST_PACKAGE],capture_output=True,text=True,timeout=30)
    report['isolated_test_package_removed']=removed and probe.returncode in (0,1) and not probe.stdout.strip();assert report['isolated_test_package_removed'],'Could not prove isolated learningtest package removal'
   except BaseException as restoration:report['test_package_restore_error']=str(restoration);error=error or restoration
  if ready and report.get('apk_sha256'):
   try:
    final_path=shell('pm','path',PACKAGE).strip().removeprefix('package:');final_hash=shell('sha256sum',final_path).split()[0]
    expected_hash=report['upgrade_gate_validated']['sha256']if report.get('upgrade_install_succeeded')else report['apk_sha256']
    report['installed_apk_sha256_after_run']=final_hash;report['installed_apk_matches_expected_after_run']=final_hash==expected_hash;assert final_hash==expected_hash,'Target APK changed unexpectedly during qualification'
   except BaseException as restoration:report['final_apk_verification_error']=str(restoration);error=error or restoration
  if fault is not None:
   try:report['storage_fault_infrastructure']=fault.close()
   except BaseException as restoration:report['storage_fault_restore_error']=str(restoration);error=error or restoration
  if ready:
   try:
    shell('am','force-stop',PACKAGE)
    for name in files():
     if name not in original:shell('su','0','rm',prefdir+'/'+name)
    for name,value in original.items():
     staging='/data/local/tmp/luma-v114-ui-restore-'+name;cmd('push',str(backup/name),staging);shell('su','0','cp',staging,prefdir+'/'+name);shell('su','0','chown',owner,prefdir+'/'+name);shell('su','0','chmod','600',prefdir+'/'+name);shell('rm',staging)
    assert set(files())==set(original),'Preference file set differs after restoration'
    for name,value in original.items():assert cmd('exec-out','su','0','cat',prefdir+'/'+name)==value,'Preference restoration mismatch: '+name
    report['restored']=True
   except BaseException as restoration:report['restore_error']=str(restoration);error=error or restoration
  if display_size is not None:
   try:
    current_size=shell('wm','size').strip()
    if current_size!=display_size:
     override=re.search(r'Override size: (\d+x\d+)',display_size);shell('wm','size',override.group(1) if override else 'reset')
    assert shell('wm','size').strip()==display_size,'Display dimensions differ after restoration'
    report['display_size_restored']=True
   except BaseException as restoration:report['display_restore_error']=str(restoration);error=error or restoration
  if rotation is not None:
   try:
    for key,value in rotation.items():
     if value=='null':shell('settings','delete','system',key)
     else:shell('settings','put','system',key,value)
    assert {key:shell('settings','get','system',key).strip() for key in rotation}==rotation,'Rotation settings differ after restoration'
    report['rotation_settings_restored']=True
   except BaseException as restoration:report['rotation_restore_error']=str(restoration);error=error or restoration
  if error:report['passed']=False
  (out/'host-report.json').write_text(json.dumps(report,indent=2)+'\n')
 if error:raise error
 print(json.dumps({'passed':report['passed'],'expected_failure_observed':report.get('expected_failure_observed',False),'restored':report['restored'],'checks':report.get('native',{}).get('checkCount',0),'screenshots':len(report['screenshots']),'output':str(out)},indent=2))
if __name__=='__main__':main()
