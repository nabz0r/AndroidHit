#!/usr/bin/env python3
"""Bounded production wallet visual browse on owned K only; exact original guest prefs restored."""
from pathlib import Path
import argparse,hashlib,json,os,re,shutil,subprocess,tempfile,traceback,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[2];PACKAGE='com.androidhit.lumadrift';SERIAL='emulator-5586';REMOTE='/sdcard/Android/data/'+PACKAGE+'/files/v112-commerce-native'
def main():
 p=argparse.ArgumentParser();p.add_argument('--apk',required=True);p.add_argument('--test-apk',required=True);p.add_argument('--version',default='1.12.0-rc2');p.add_argument('--code',default='13');p.add_argument('--label',default='rc2');a=p.parse_args()
 assert re.fullmatch(r'[a-zA-Z0-9_-]{1,30}',a.label)
 out=ROOT/'docs/validation/v1.12/commerce-native'/a.label;shots=ROOT/'docs/screenshots/v1.12/commerce-native'/a.label
 assert not out.exists() and not shots.exists(),'Immutable new label required';out.mkdir(parents=True);shots.mkdir(parents=True)
 adb=Path(os.environ.get('ANDROID_HOME','/Users/n/Library/Android/sdk'))/'platform-tools/adb'
 def cmd(*args,timeout=60):return subprocess.check_output([str(adb),'-s',SERIAL,*args],stderr=subprocess.STDOUT,timeout=timeout)
 def shell(*args):return cmd('shell',*args).decode(errors='replace')
 def sha(raw):return hashlib.sha256(raw).hexdigest()
 def snapshot(package):
  directory='/data/user/0/'+package+'/shared_prefs';names=shell('su','0','ls','-1',directory).splitlines();assert all(re.fullmatch(r'[a-zA-Z0-9_.-]+',n) for n in names);return {n:cmd('exec-out','su','0','cat',directory+'/'+n) for n in names}
 backup=Path(tempfile.mkdtemp(prefix='luma-commerce-k-original-'));original=None;owner=None;prior_test=None;report={'passed':False,'restored':False,'scope':'Native SDK visible guest browsing only; no ad/age/login/purchase/simulation controls, normal Play catalogue query allowed; only owned K5586'};error=None
 try:
  assert cmd('emu','avd','name').decode().splitlines()[0]=='Luma-Release111-K';assert shell('getconf','PAGESIZE').strip()=='16384';assert shell('getprop','ro.build.version.sdk').strip()=='36'
  shell('am','force-stop',PACKAGE);original=snapshot(PACKAGE);owner=shell('su','0','stat','-c','%u:%g','/data/user/0/'+PACKAGE).strip();assert re.fullmatch(r'\d+:\d+',owner)
  for name,data in original.items():(backup/name).write_bytes(data)
  all_values={}
  for name,data in original.items():
   for node in ET.fromstring(data):
    if node.get('name')=='previously_signed_in':assert node.get('value')!='true','Guest required'
    if node.get('name')=='explicit_ads_opt_in':assert node.get('value')!='true','Ads opt-out required'
    all_values[node.get('name')]=node.get('value',node.text)
  assert not all_values.get('firebase_uid') and not all_values.get('uid'),'No persisted Google UID allowed'
  report.update({'serial':SERIAL,'avd':'Luma-Release111-K','api':'36','page_size':16384,'original_file_count':len(original),'backup_location':str(backup),'original_prefs':{n:sha(b) for n,b in original.items()},'preflight_guest':True,'preflight_ads_opted_out':True,'device_density':shell('wm','density').strip(),'device_size':shell('wm','size').strip()})
  lab=snapshot(PACKAGE+'.sandbox');report['lab_before']={n:sha(b) for n,b in lab.items()}
  lab_path=shell('pm','path',PACKAGE+'.sandbox').strip().removeprefix('package:');report['lab_apk_before']=shell('sha256sum',lab_path).split()[0]
  # Preserve an existing signed SDK test package as well as the three original guest files.
  existing=subprocess.run([str(adb),'-s',SERIAL,'shell','pm','path',PACKAGE+'.test'],capture_output=True,text=True,timeout=30)
  if existing.returncode==0 and existing.stdout.strip():
   prior_test=backup/'prior-test.apk';prior_test.write_bytes(cmd('exec-out','cat',existing.stdout.strip().removeprefix('package:')));report['prior_test_sha256']=sha(prior_test.read_bytes())
  for kind,path in [('game',Path(a.apk)),('test',Path(a.test_apk))]:
   assert path.is_file();report[kind+'_apk_sha256']=sha(path.read_bytes());(out/(kind+'-install.txt')).write_bytes(cmd('install','-r',str(path),timeout=180))
  assert snapshot(PACKAGE)==original,'Upgrade before first launch changed guest prefs'
  details=shell('dumpsys','package',PACKAGE);assert 'versionName='+a.version in details and 'versionCode='+a.code+' ' in details
  installed=shell('pm','path',PACKAGE).strip().removeprefix('package:');assert shell('sha256sum',installed).split()[0]==report['game_apk_sha256'];report['version']=a.version;report['version_code']=int(a.code)
  before=shell('logcat','-d','-b','crash');shell('su','0','rm','-rf',REMOTE)
  runner=PACKAGE+'.test/com.androidhit.lumadrift.qa.commerce112.CommerceInstrumentation'
  run=cmd('shell','am','instrument','-w','-r','-e','expectedVersion',a.version,'-e','expectedCode',a.code,runner,timeout=600);(out/'instrumentation.txt').write_bytes(run)
  with tempfile.TemporaryDirectory(prefix='luma-commerce-pull-') as tmp:
   cmd('pull',REMOTE,tmp);pulled=Path(tmp)/'v112-commerce-native';assert (pulled/'report.json').is_file()
   for f in pulled.iterdir():shutil.copyfile(f,(shots if f.suffix=='.png' else out)/f.name)
  native=json.loads((out/'report.json').read_text());report['native']=native;report['screenshots']={f.name:sha(f.read_bytes()) for f in shots.glob('*.png')};after=shell('logcat','-d','-b','crash');delta=after[len(before):] if after.startswith(before) else after;(out/'crash-delta.txt').write_text(delta);assert not delta.strip(),'New crash buffer entries';assert native['passed'],'Native checks failed'
  report['passed']=True
 except BaseException as e:error=e;report['error']=str(e);report['traceback']=traceback.format_exc()
 finally:
  if original is not None:
   try:
    shell('am','force-stop',PACKAGE);current=snapshot(PACKAGE)
    for name in current:
     if name not in original:shell('su','0','rm','/data/user/0/'+PACKAGE+'/shared_prefs/'+name)
    for name,data in original.items():
     staging='/data/local/tmp/luma-commerce-restore-'+name;cmd('push',str(backup/name),staging);dest='/data/user/0/'+PACKAGE+'/shared_prefs/'+name;shell('su','0','cp',staging,dest);shell('su','0','chown',owner,dest);shell('su','0','chmod','600',dest);shell('rm',staging)
    assert snapshot(PACKAGE)==original;report['restored']=True;report['restored_prefs']={n:sha(b) for n,b in snapshot(PACKAGE).items()}
    if 'lab' in locals():assert snapshot(PACKAGE+'.sandbox')==lab;assert shell('sha256sum',lab_path).split()[0]==report['lab_apk_before'];report['lab_unchanged']=True
    if prior_test is not None:report['prior_test_restore']=cmd('install','-r',str(prior_test),timeout=180).decode().strip();restored_test=shell('pm','path',PACKAGE+'.test').strip().removeprefix('package:');assert shell('sha256sum',restored_test).split()[0]==report['prior_test_sha256'];report['prior_test_restored']=True
   except BaseException as e:report['restore_error']=str(e);error=error or e
  report['runner_sources']={str(f.relative_to(ROOT)):sha(f.read_bytes()) for f in (ROOT/'scripts/v112-commerce-ui').rglob('*') if f.is_file() and '__pycache__' not in str(f)};(out/'host-report.json').write_text(json.dumps(report,indent=2)+'\n')
 if error:raise error
 print(json.dumps({'passed':report['passed'],'restored':report['restored'],'checks':report['native']['checkCount'],'screenshots':len(report['screenshots']),'output':str(out)},indent=2))
if __name__=='__main__':main()
