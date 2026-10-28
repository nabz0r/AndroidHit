#!/usr/bin/env python3
"""Run SDK Instrumentation against the isolated QA emulator; restore original prefs exactly."""
from pathlib import Path
from storage_fault import StorageFaultController
import argparse, hashlib, json, os, re, shutil, subprocess, tempfile, time, traceback, xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[2]
PACKAGE='com.androidhit.lumadrift.sandbox'
RUNNER=PACKAGE+'.test/com.androidhit.lumadrift.qa.v110.CandidateUiInstrumentation'
REMOTE='/sdcard/Android/data/'+PACKAGE+'/files/v110-sdk-ui'

def main():
 global PACKAGE,RUNNER,REMOTE
 p=argparse.ArgumentParser();p.add_argument('--serial',default='emulator-5580');p.add_argument('--cases',default='tutorial,gestures,map,journey,recovery');p.add_argument('--label',default='pilot');p.add_argument('--test-apk');p.add_argument('--expected-apk');p.add_argument('--journey-through',type=int,default=100);p.add_argument('--variant',choices=['lab','production'],default='lab');p.add_argument('--compact',action='store_true');p.add_argument('--capture-all',action='store_true');a=p.parse_args()
 if a.variant=='production':
  if a.cases!='startup':raise SystemExit('Production permits only the read-only startup/audio runner')
  PACKAGE='com.androidhit.lumadrift';RUNNER=PACKAGE+'.test/com.androidhit.lumadrift.qa.v110.ProductionStartupInstrumentation';REMOTE='/sdcard/Android/data/'+PACKAGE+'/files/v110-sdk-ui'
 elif 'startup' in a.cases.split(','):raise SystemExit('Startup observer is production-only')
 if not a.test_apk:a.test_apk=str(ROOT/'app/build/outputs/apk/androidTest'/a.variant/'release'/('app-'+a.variant+'-release-androidTest.apk'))
 allowed={'tutorial','gestures','map','challenge','journey','recovery','powers','landscape','startup','guardians','hub','avatar','story','menus','rewards','styles','languages'}
 pending={'event-retry','guardian-storage-fault'}
 requested=set(a.cases.split(','))
 if requested & pending:raise SystemExit('UI cases await the stable1.10 contract: '+','.join(sorted(requested & pending)))
 if not requested<=allowed or not a.cases:raise SystemExit('Unknown or empty UI case set')
 if not 1<=a.journey_through<=1000:raise SystemExit('journey-through must be between1 and1000')
 if a.serial not in ['emulator-5580','emulator-5590'] or not re.fullmatch(r'[a-zA-Z0-9_-]{1,40}',a.label):raise SystemExit('Explicit owned emulator-5580/5590 QA target and safe label required')
 adb=str(Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'))/'platform-tools/adb')
 def cmd(*parts,timeout=60):return subprocess.check_output([adb,'-s',a.serial,*parts],stderr=subprocess.STDOUT,timeout=timeout)
 def shell(*parts):return cmd('shell',*parts).decode(errors='replace')
 def files():
  result=shell('su','0','ls','-1',prefdir).splitlines()
  if any(not re.fullmatch(r'[A-Za-z0-9_.-]+',n) for n in result):raise AssertionError('Unsafe or incomplete preference file list')
  return result
 out=ROOT/'docs/validation/v1.10'/('ui-sdk-'+a.label);shots=ROOT/'docs/screenshots/v1.10'/('ui-sdk-'+a.label);
 if out.exists() or shots.exists():raise SystemExit('Use a fresh label: output already exists')
 out.mkdir(parents=True);shots.mkdir(parents=True)
 report={'passed':False,'restored':False,'scope':'Native SDK Instrumentation, AccessibilityNodeProvider actions and touchscreen events from observed native node bounds; no browser/desktop/shell input/profile injection','cases':a.cases,'journey_through':a.journey_through,'serial':a.serial,'capture_policy':'all' if a.capture_all else 'curated-original-milestones'}
 prefdir='/data/user/0/'+PACKAGE+'/shared_prefs';backup=Path(tempfile.mkdtemp(prefix='luma-v110-ui-original-prefs-'));original={};ready=False;owner='';error=None;rotation=None;display_size=None;fault=None
 try:
  assert shell('getprop','ro.kernel.qemu').strip()=='1','QA emulator only'
  assert shell('getconf','PAGESIZE').strip()=='16384','Designated QA runtime must use16KiB pages'
  if a.compact or {'tutorial','landscape','avatar','story','styles','guardians','guardian-storage-fault'} & set(a.cases.split(',')):rotation={key:shell('settings','get','system',key).strip() for key in ['user_rotation','accelerometer_rotation']};report['rotation_before']=rotation
  details=shell('dumpsys','package',PACKAGE);version_name='1.10.0-rc3' if a.variant=='production' else '1.10.0-sandbox-rc3';code=11;assert ('versionName='+version_name) in details and ('versionCode='+str(code)+' ') in details,'Install the explicitly selected frozen rc3 release candidate first'
  if a.compact:display_size=shell('wm','size').strip();report['display_size_before']=display_size
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
  report.update({'test_apk_sha256':hashlib.sha256(Path(a.test_apk).read_bytes()).hexdigest(),'wrapper_source_sha256':hashlib.sha256(Path(__file__).read_bytes()).hexdigest(),'storage_fault_source_sha256':hashlib.sha256((ROOT/'scripts/v110-ui-test/storage_fault.py').read_bytes()).hexdigest() if 'guardian-storage-fault' in a.cases.split(',') else None,'runner_source_sha256':hashlib.sha256((ROOT/'scripts/v110-ui-test/java/com/androidhit/lumadrift/qa/v110'/('ProductionStartupInstrumentation.java' if a.variant=='production' else 'CandidateUiInstrumentation.java')).read_bytes()).hexdigest(),'package':PACKAGE,'backup_location':str(backup),'backup_manifest':{n:hashlib.sha256(v).hexdigest() for n,v in original.items()},'device':{'api':shell('getprop','ro.build.version.sdk').strip(),'abi':shell('getprop','ro.product.cpu.abi').strip(),'page_size':shell('getconf','PAGESIZE').strip(),'density':shell('wm','density').strip(),'size':shell('wm','size').strip()}})
  crash_before=shell('logcat','-d','-b','crash')
  install=cmd('install','-r',a.test_apk).decode();(out/'test-apk-install.txt').write_text(install);assert 'Success' in install
  shell('su','0','rm','-rf',REMOTE)
  if 'guardian-storage-fault' in a.cases.split(','):
   assert a.variant=='lab','Storage fault is restricted to the current guest Lab QA release'
   fault=StorageFaultController(adb,a.serial,prefdir,REMOTE,out);fault.start()
  run=cmd('shell','am','instrument','-w','-r','-e','cases',a.cases,'-e','journeyThrough',str(a.journey_through),'-e','compact',str(a.compact).lower(),'-e','captureAll',str(a.capture_all).lower(),'-e','storageFaultInfrastructure',str(fault is not None).lower(),RUNNER,timeout=1800).decode(errors='replace');(out/'instrumentation.txt').write_text(run)
  crash_after=shell('logcat','-d','-b','crash')
  crash_delta=crash_after[len(crash_before):] if crash_after.startswith(crash_before) else crash_after
  if crash_delta.strip():(out/'native-crash-delta.txt').write_text(crash_delta)
  with tempfile.TemporaryDirectory(prefix='luma-v110-ui-captures-') as tmp:
   cmd('pull',REMOTE,tmp);pulled=Path(tmp)/'v110-sdk-ui'
   for file in pulled.iterdir():
    dest=shots/file.name if file.suffix=='.png' else out/file.name;shutil.copyfile(file,dest)
   report['screenshots']={f.name:hashlib.sha256(f.read_bytes()).hexdigest() for f in shots.glob('*.png')}
   assert (pulled/'report.json').is_file(),'Native instrumentation produced no report; inspect instrumentation.txt and native-crash-delta.txt'
   native=json.loads((pulled/'report.json').read_text());report['native']=native
  assert not crash_delta.strip(),'New native crash buffer entries; inspect native-crash-delta.txt'
  report['passed']=bool(native.get('passed'))
  assert report['passed'],'Instrumentation checks failed; inspect failure.txt'
 except BaseException as failure:
  error=failure;report['error']=str(failure);report['traceback']=traceback.format_exc()
 finally:
  if fault is not None:
   try:report['storage_fault_infrastructure']=fault.close()
   except BaseException as restoration:report['storage_fault_restore_error']=str(restoration);error=error or restoration
  if ready:
   try:
    shell('am','force-stop',PACKAGE)
    for name in files():
     if name not in original:shell('su','0','rm',prefdir+'/'+name)
    for name,value in original.items():
     staging='/data/local/tmp/luma-v110-ui-restore-'+name;cmd('push',str(backup/name),staging);shell('su','0','cp',staging,prefdir+'/'+name);shell('su','0','chown',owner,prefdir+'/'+name);shell('su','0','chmod','600',prefdir+'/'+name);shell('rm',staging)
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
 print(json.dumps({'passed':report['passed'],'expected_failure_observed':report.get('expected_failure_observed',False),'restored':report['restored'],'checks':report['native']['checkCount'],'screenshots':len(report['screenshots']),'output':str(out)},indent=2))
if __name__=='__main__':main()
