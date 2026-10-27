#!/usr/bin/env python3
"""Run SDK Instrumentation against the isolated QA emulator; restore original prefs exactly."""
from pathlib import Path
import argparse, hashlib, json, os, re, shutil, subprocess, tempfile, time, traceback, xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[2]
PACKAGE='com.androidhit.lumadrift.sandbox'
RUNNER=PACKAGE+'.test/com.androidhit.lumadrift.qa.CandidateUiInstrumentation'
REMOTE='/sdcard/Android/data/'+PACKAGE+'/files/v17-sdk-ui'

def main():
 global PACKAGE,RUNNER,REMOTE
 p=argparse.ArgumentParser();p.add_argument('--serial',default='emulator-5580');p.add_argument('--cases',default='menus,map,social,capsule,challenge,journey,landscape,languages');p.add_argument('--label',default='pilot');p.add_argument('--test-apk');p.add_argument('--variant',choices=['lab','production'],default='lab');a=p.parse_args()
 if a.variant=='production':
  if a.cases!='startup':raise SystemExit('Production permits only the read-only startup/audio runner')
  PACKAGE='com.androidhit.lumadrift';RUNNER=PACKAGE+'.test/com.androidhit.lumadrift.qa.ProductionStartupInstrumentation';REMOTE='/sdcard/Android/data/'+PACKAGE+'/files/v17-sdk-ui'
 elif 'startup' in a.cases.split(','):raise SystemExit('Startup observer is production-only')
 if not a.test_apk:a.test_apk=str(ROOT/'app/build/outputs/apk/androidTest'/a.variant/'release'/('app-'+a.variant+'-release-androidTest.apk'))
 if not set(a.cases.split(','))<=set(['menus','map','challenge','journey','languages','social','capsule','landscape','startup']) or not a.cases:raise SystemExit('Unknown or empty UI case set')
 if a.serial!='emulator-5580' or not re.fullmatch(r'[a-zA-Z0-9_-]{1,40}',a.label):raise SystemExit('Explicit emulator-5580 QA target and safe label required')
 adb=str(Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'))/'platform-tools/adb')
 def cmd(*parts,timeout=60):return subprocess.check_output([adb,'-s',a.serial,*parts],stderr=subprocess.STDOUT,timeout=timeout)
 def shell(*parts):return cmd('shell',*parts).decode(errors='replace')
 def files():
  result=shell('su','0','ls','-1',prefdir).splitlines()
  if any(not re.fullmatch(r'[A-Za-z0-9_.-]+',n) for n in result):raise AssertionError('Unsafe or incomplete preference file list')
  return result
 out=ROOT/'docs/validation/v1.7'/('ui-sdk-'+a.label);shots=ROOT/'docs/screenshots/v1.7'/('ui-sdk-'+a.label);
 if out.exists() or shots.exists():raise SystemExit('Use a fresh label: output already exists')
 out.mkdir(parents=True);shots.mkdir(parents=True)
 report={'passed':False,'restored':False,'scope':'Native SDK Instrumentation and AccessibilityNodeProvider actions only; no browser/desktop/manual input/profile injection','cases':a.cases,'serial':a.serial}
 prefdir='/data/user/0/'+PACKAGE+'/shared_prefs';backup=Path(tempfile.mkdtemp(prefix='luma-v17-ui-original-prefs-'));original={};ready=False;owner='';error=None;rotation=None
 try:
  assert shell('getprop','ro.kernel.qemu').strip()=='1','QA emulator only'
  if 'landscape' in a.cases.split(','):rotation={key:shell('settings','get','system',key).strip() for key in ['user_rotation','accelerometer_rotation']};report['rotation_before']=rotation
  details=shell('dumpsys','package',PACKAGE);assert ('versionName=1.7.0-rc1' if a.variant=='production' else 'versionName=1.7.0-sandbox') in details and 'versionCode=8 ' in details,'Install the 1.7/code8 Lab candidate first'
  shell('am','force-stop',PACKAGE)
  owner=shell('su','0','stat','-c','%u:%g','/data/user/0/'+PACKAGE).strip();assert re.fullmatch(r'\d+:\d+',owner)
  for name in files():
   value=cmd('exec-out','su','0','cat',prefdir+'/'+name);original[name]=value;(backup/name).write_bytes(value)
  ready=True
  for raw in original.values():
   for node in ET.fromstring(raw):
    if node.get('name')=='previously_signed_in' and node.get('value')=='true':raise AssertionError('Refuses a stored signed-in Google identity; guest-only test')
  apkpath=shell('pm','path',PACKAGE).strip().removeprefix('package:');report['apk_sha256']=shell('sha256sum',apkpath).split()[0]
  report.update({'test_apk_sha256':hashlib.sha256(Path(a.test_apk).read_bytes()).hexdigest(),'runner_source_sha256':hashlib.sha256((ROOT/'scripts/v17-ui-test/java/com/androidhit/lumadrift/qa'/('ProductionStartupInstrumentation.java' if a.variant=='production' else 'CandidateUiInstrumentation.java')).read_bytes()).hexdigest(),'package':PACKAGE,'backup_location':str(backup),'backup_manifest':{n:hashlib.sha256(v).hexdigest() for n,v in original.items()},'device':{'api':shell('getprop','ro.build.version.sdk').strip(),'abi':shell('getprop','ro.product.cpu.abi').strip(),'page_size':shell('getconf','PAGESIZE').strip(),'density':shell('wm','density').strip(),'size':shell('wm','size').strip()}})
  crash_before=shell('logcat','-d','-b','crash')
  install=cmd('install','-r',a.test_apk).decode();(out/'test-apk-install.txt').write_text(install);assert 'Success' in install
  shell('su','0','rm','-rf',REMOTE)
  run=cmd('shell','am','instrument','-w','-r','-e','cases',a.cases,RUNNER,timeout=600).decode(errors='replace');(out/'instrumentation.txt').write_text(run)
  with tempfile.TemporaryDirectory(prefix='luma-v17-ui-captures-') as tmp:
   cmd('pull',REMOTE,tmp);pulled=Path(tmp)/'v17-sdk-ui';native=json.loads((pulled/'report.json').read_text());report['native']=native
   for file in pulled.iterdir():
    dest=shots/file.name if file.suffix=='.png' else out/file.name;shutil.copyfile(file,dest)
   report['screenshots']={f.name:hashlib.sha256(f.read_bytes()).hexdigest() for f in shots.glob('*.png')}
  crash_after=shell('logcat','-d','-b','crash');assert crash_after==crash_before,'New native crash buffer entries'
  report['passed']=bool(native.get('passed'));assert report['passed'],'Instrumentation checks failed; inspect failure.txt'
 except BaseException as failure:
  error=failure;report['error']=str(failure);report['traceback']=traceback.format_exc()
 finally:
  if ready:
   try:
    shell('am','force-stop',PACKAGE)
    for name in files():
     if name not in original:shell('su','0','rm',prefdir+'/'+name)
    for name,value in original.items():
     staging='/data/local/tmp/luma-v17-ui-restore-'+name;cmd('push',str(backup/name),staging);shell('su','0','cp',staging,prefdir+'/'+name);shell('su','0','chown',owner,prefdir+'/'+name);shell('su','0','chmod','600',prefdir+'/'+name);shell('rm',staging)
    assert set(files())==set(original),'Preference file set differs after restoration'
    for name,value in original.items():assert cmd('exec-out','su','0','cat',prefdir+'/'+name)==value,'Preference restoration mismatch: '+name
    report['restored']=True
   except BaseException as restoration:report['restore_error']=str(restoration);error=error or restoration
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
 print(json.dumps({'passed':report['passed'],'restored':report['restored'],'checks':report['native']['checkCount'],'screenshots':len(report['screenshots']),'output':str(out)},indent=2))
if __name__=='__main__':main()
