#!/usr/bin/env python3
"""Run SDK Instrumentation against the isolated QA emulator; restore original prefs exactly."""
from pathlib import Path
from storage_fault import StorageFaultController
import argparse, hashlib, json, os, re, shutil, subprocess, tempfile, time, traceback, xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[2]
PACKAGE='com.androidhit.lumadrift.sandbox'
TEST_PACKAGE='com.androidhit.lumadrift.hometest'
RUNNER=TEST_PACKAGE+'/com.androidhit.lumadrift.qa.v113.CandidateUiInstrumentation'
REMOTE='/sdcard/Android/data/'+PACKAGE+'/files/v113-sdk-ui'

def main():
 global PACKAGE,RUNNER,REMOTE
 p=argparse.ArgumentParser();p.add_argument('--serial',default='emulator-5580');p.add_argument('--cases',default='house-smoke');p.add_argument('--label',default='pilot');p.add_argument('--test-apk');p.add_argument('--expected-apk');p.add_argument('--journey-through',type=int,default=100);p.add_argument('--variant',choices=['lab','production'],default='lab');p.add_argument('--compact',action='store_true');p.add_argument('--capture-all',action='store_true');p.add_argument('--expected-version',default='1.13.0-sandbox-rc2');p.add_argument('--expected-code',type=int,default=14);a=p.parse_args()
 if a.variant=='production':
  production_cases=set(a.cases.split(','));bounded={'avatar-visual','journey','menus','gestures','recovery','shop-focus','house-smoke','house-locales','house-layout'}
  if a.cases!='startup' and (not production_cases<=bounded or ('avatar-visual' not in production_cases and not production_cases<= {'shop-focus','house-smoke','house-locales','house-layout'})):raise SystemExit('Production permits startup, bounded house/shops browsing or earned visual qualification only')
  PACKAGE='com.androidhit.lumadrift';runner_class='ProductionStartupInstrumentation' if a.cases=='startup' else 'CandidateUiInstrumentation';RUNNER=TEST_PACKAGE+'/com.androidhit.lumadrift.qa.v113.'+runner_class;REMOTE='/sdcard/Android/data/'+PACKAGE+'/files/v113-sdk-ui'
 elif 'startup' in a.cases.split(','):raise SystemExit('Startup observer is production-only')
 if not a.test_apk:a.test_apk=str(ROOT/'app/build/qualification-artifacts/v1.13'/('home-'+a.variant+'-rc2-test.apk'))
 allowed={'tutorial','gestures','map','challenge','journey','recovery','powers','landscape','startup','guardians','hub','avatar','story','menus','rewards','styles','languages','avatar-visual','shop-focus','shop-locales','power-return','house-smoke','house','house-locales','house-layout','firstlight','puzzle-locales','level15-audit'}
 pending={'event-retry','guardian-storage-fault'}
 requested=set(a.cases.split(','))
 if requested & pending:raise SystemExit('UI cases await the stable1.10 contract: '+','.join(sorted(requested & pending)))
 if not requested<=allowed or not a.cases:raise SystemExit('Unknown or empty UI case set')
 if not 1<=a.journey_through<=1000:raise SystemExit('journey-through must be between1 and1000')
 if (a.serial,a.variant) not in {('emulator-5580','lab'),('emulator-5586','production')} or not re.fullmatch(r'[a-zA-Z0-9_-]{1,40}',a.label):raise SystemExit('Owned5580 Lab or explicitly owned5586 production-only target and safe label required')
 adb=str(Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'))/'platform-tools/adb')
 def cmd(*parts,timeout=60):return subprocess.check_output([adb,'-s',a.serial,*parts],stderr=subprocess.STDOUT,timeout=timeout)
 def shell(*parts):return cmd('shell',*parts).decode(errors='replace')
 def files():
  result=shell('su','0','ls','-1',prefdir).splitlines()
  if any(not re.fullmatch(r'[A-Za-z0-9_.-]+',n) for n in result):raise AssertionError('Unsafe or incomplete preference file list')
  return result
 out=ROOT/'docs/validation/v1.13/ui-bugs'/('ui-sdk-'+a.label);shots=ROOT/'docs/screenshots/v1.13/ui-bugs'/('ui-sdk-'+a.label);
 if out.exists() or shots.exists():raise SystemExit('Use a fresh label: output already exists')
 out.mkdir(parents=True);shots.mkdir(parents=True)
 report={'passed':False,'restored':False,'scope':'Native SDK Instrumentation, AccessibilityNodeProvider actions and touchscreen events from observed native node bounds; no browser/desktop/shell input/profile injection','cases':a.cases,'journey_through':a.journey_through,'serial':a.serial,'capture_policy':'original-full-frames-plus-labelled-unscaled-analysis-crops' if 'avatar-visual' in requested else ('all' if a.capture_all else 'curated-original-milestones')}
 prefdir='/data/user/0/'+PACKAGE+'/shared_prefs';backup=Path(tempfile.mkdtemp(prefix='luma-v113-ui-original-prefs-'));original={};ready=False;owner='';error=None;rotation=None;display_size=None;fault=None;test_installed=False
 try:
  assert shell('getprop','ro.kernel.qemu').strip()=='1','QA emulator only'
  expected_avd='hexkeep-play-36-16k' if a.serial=='emulator-5580' else 'Luma-Release111-K'
  assert cmd('emu','avd','name').decode().splitlines()[0].strip()==expected_avd,'Exact owned AVD identity required'
  assert shell('getconf','PAGESIZE').strip()=='16384','Designated QA runtime must use16KiB pages'
  rotation={key:shell('settings','get','system',key).strip() for key in ['user_rotation','accelerometer_rotation']};report['rotation_before']=rotation
  details=shell('dumpsys','package',PACKAGE);version_name=a.expected_version;code=a.expected_code;assert ('versionName='+version_name) in details and ('versionCode='+str(code)+' ') in details,'Install the explicitly selected frozen version/code first'
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
  expected=Path(a.expected_apk) if a.expected_apk else ROOT/'releases'/('luma-drift-'+version_name+'.apk')
  assert expected.is_file(),'Frozen expected APK required: '+str(expected)
  report['expected_apk_sha256']=hashlib.sha256(expected.read_bytes()).hexdigest()
  assert report['apk_sha256']==report['expected_apk_sha256'],'Installed APK differs from the frozen candidate'
  report.update({'test_apk_sha256':hashlib.sha256(Path(a.test_apk).read_bytes()).hexdigest(),'wrapper_source_sha256':hashlib.sha256(Path(__file__).read_bytes()).hexdigest(),'storage_fault_source_sha256':hashlib.sha256((ROOT/'scripts/v113-ui-test/storage_fault.py').read_bytes()).hexdigest() if 'guardian-storage-fault' in a.cases.split(',') else None,'runner_source_sha256':hashlib.sha256((ROOT/'scripts/v113-ui-test/java/com/androidhit/lumadrift/qa/v113'/('ProductionStartupInstrumentation.java' if a.cases=='startup' else 'CandidateUiInstrumentation.java')).read_bytes()).hexdigest(),'package':PACKAGE,'backup_location':str(backup),'backup_manifest':{n:hashlib.sha256(v).hexdigest() for n,v in original.items()},'device':{'api':shell('getprop','ro.build.version.sdk').strip(),'abi':shell('getprop','ro.product.cpu.abi').strip(),'page_size':shell('getconf','PAGESIZE').strip(),'density':shell('wm','density').strip(),'size':shell('wm','size').strip()}})
  crash_before=shell('logcat','-d','-b','crash')
  prior_test=subprocess.run([adb,'-s',a.serial,'shell','pm','path',TEST_PACKAGE],capture_output=True,text=True,timeout=30);assert prior_test.returncode in (0,1) and not prior_test.stdout.strip(),'Isolated hometest package must initially be absent'
  install=cmd('install','-r',a.test_apk).decode();(out/'test-apk-install.txt').write_text(install);assert 'Success' in install
  test_installed=True;test_path=shell('pm','path',TEST_PACKAGE).strip().removeprefix('package:');report['installed_test_apk_sha256']=shell('sha256sum',test_path).split()[0];assert report['installed_test_apk_sha256']==report['test_apk_sha256'],'Installed isolated native test APK differs from the frozen file'
  shell('su','0','rm','-rf',REMOTE)
  if 'guardian-storage-fault' in a.cases.split(','):
   assert a.variant=='lab','Storage fault is restricted to the current guest Lab QA release'
   fault=StorageFaultController(adb,a.serial,prefdir,REMOTE,out);fault.start()
  phases=['prepare','cold'] if {'avatar-visual','house'} & requested else ['prepare']
  phase_results=[];report['phases']=phase_results;report['screenshots']={}
  previous_pid=None
  for phase in phases:
   if phase=='cold':
    assert phase_results[-1]['native'].get('visualCheckpointReady') or phase_results[-1]['native'].get('houseCheckpointReady'),'Prepare produced no valid checkpoint; cold verification cannot fabricate a prior identity'
    previous_pid=phase_results[-1]['native'].get('processId');shell('am','force-stop',PACKAGE)
    probe=subprocess.run([adb,'-s',a.serial,'shell','pidof',PACKAGE],capture_output=True,text=True,timeout=30);assert probe.returncode in (0,1) and not probe.stdout.strip(),'App process survives force-stop'
    report['cold_process_boundary']={'force_stop':True,'package_absent_from_pidof_before_restart':True,'previous_instrumented_process_id':previous_pid}
   phase_cases=('house' if 'house' in requested else 'avatar-visual') if phase=='cold' else a.cases
   run=cmd('shell','am','instrument','-w','-r','-e','cases',phase_cases,'-e','journeyThrough',str(a.journey_through),'-e','compact',str(a.compact).lower(),'-e','captureAll',str(a.capture_all).lower(),'-e','visualPhase',phase,'-e','expectedVersion',version_name,'-e','expectedCode',str(code),'-e','storageFaultInfrastructure',str(fault is not None).lower(),RUNNER,timeout=1800).decode(errors='replace')
   phase_out=out/phase;phase_shots=shots/phase;phase_out.mkdir();phase_shots.mkdir();(phase_out/'instrumentation.txt').write_text(run)
   with tempfile.TemporaryDirectory(prefix='luma-v113-ui-captures-') as tmp:
    cmd('pull',REMOTE,tmp);pulled=Path(tmp)/'v113-sdk-ui'
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
    phase_results.append({'phase':phase,'native':native,'screenshots':hashes})
   if phase=='cold':
    report['cold_process_boundary']['restarted_instrumented_process_id']=native.get('processId')
    assert native.get('processId') and native.get('processId')!=previous_pid,'Cold phase did not observe a distinct actual target process'
   if not native.get('passed') and not native.get('visualCheckpointReady') and not native.get('houseCheckpointReady'):raise AssertionError('Prepare failed before persistent checkpoint; raw failure preserved')
  crash_after=shell('logcat','-d','-b','crash');crash_delta=crash_after[len(crash_before):] if crash_after.startswith(crash_before) else crash_after
  if crash_delta.strip():(out/'native-crash-delta.txt').write_text(crash_delta)
  report['native']={'passed':all(x['native'].get('passed') for x in phase_results),'checks':[c for x in phase_results for c in x['native'].get('checks',[])],'checkCount':sum(x['native'].get('checkCount',0) for x in phase_results),'scope':('Separate real prepare and cold processes; observed raster deltas plus persistent identities; no seeding' if len(phase_results)>1 else 'Actual SDK single-process observation; no profile seeding')}
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
    report['isolated_test_package_removed']='Success' in cmd('uninstall',TEST_PACKAGE).decode();assert report['isolated_test_package_removed'],'Could not remove isolated home test package'
   except BaseException as restoration:report['test_package_restore_error']=str(restoration);error=error or restoration
  if fault is not None:
   try:report['storage_fault_infrastructure']=fault.close()
   except BaseException as restoration:report['storage_fault_restore_error']=str(restoration);error=error or restoration
  if ready:
   try:
    shell('am','force-stop',PACKAGE)
    for name in files():
     if name not in original:shell('su','0','rm',prefdir+'/'+name)
    for name,value in original.items():
     staging='/data/local/tmp/luma-v113-ui-restore-'+name;cmd('push',str(backup/name),staging);shell('su','0','cp',staging,prefdir+'/'+name);shell('su','0','chown',owner,prefdir+'/'+name);shell('su','0','chmod','600',prefdir+'/'+name);shell('rm',staging)
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
  (out/'host-report.json').write_text(json.dumps(report,indent=2)+'\n')
 if error:raise error
 print(json.dumps({'passed':report['passed'],'expected_failure_observed':report.get('expected_failure_observed',False),'restored':report['restored'],'checks':report.get('native',{}).get('checkCount',0),'screenshots':len(report['screenshots']),'output':str(out)},indent=2))
if __name__=='__main__':main()
