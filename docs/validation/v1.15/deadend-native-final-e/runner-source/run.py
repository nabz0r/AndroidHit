#!/usr/bin/env python3
"""Final native dead-end scope; run only after exclusive AVD handoff."""
from pathlib import Path
import argparse,hashlib,json,subprocess,tempfile,shutil,traceback,zipfile
ROOT=Path(__file__).resolve().parents[2];HERE=Path(__file__).resolve().parent
PACKAGE='com.androidhit.lumadrift';TEST='com.androidhit.lumadrift.deadendtest';RUNNER=TEST+'/com.androidhit.lumadrift.qa.v115deadend.DeadEndInstrumentation'
def sha(p):return hashlib.sha256(Path(p).read_bytes()).hexdigest()
def main():
 p=argparse.ArgumentParser();p.add_argument('--serial',required=True);p.add_argument('--expected-avd',required=True);p.add_argument('--expected-apk',required=True);p.add_argument('--sha256',required=True);p.add_argument('--test-apk',required=True);p.add_argument('--label',required=True);a=p.parse_args()
 assert a.serial=='emulator-5586' and a.expected_avd=='Luma-Refonte115-QA','Only the explicitly handed-off primary QA AVD'
 assert a.label.replace('-','').replace('_','').isalnum() and len(a.label)<=50
 apk=Path(a.expected_apk).resolve();test=Path(a.test_apk).resolve();assert sha(apk)==a.sha256=='07b5bfc2632edc3d34f7583ac71c8343b52d7b926ed526aeb7a88599253df0f9'
 fixture=ROOT/'docs/validation/v1.15/deadend-fixture.json';assert sha(fixture)==sha(HERE/'assets/deadend-fixture.json')
 with zipfile.ZipFile(test) as z:assert hashlib.sha256(z.read('assets/deadend-fixture.json')).hexdigest()==sha(fixture),'Packaged fixture differs'
 source=json.loads(fixture.read_text());assert all(sha(ROOT/path)==digest for path,digest in source['sourceSha256'].items()),'Host fixture provenance changed'
 base=['/Users/n/Library/Android/sdk/platform-tools/adb','-s',a.serial]
 def call(*args,timeout=90):return subprocess.check_output(base+list(args),stderr=subprocess.STDOUT,timeout=timeout)
 def shell(*args):return call('shell',*args).decode(errors='replace')
 def installed(pkg):
  result=subprocess.run(base+['shell','pm','path',pkg],capture_output=True,text=True,timeout=30);return result.stdout.strip().removeprefix('package:')
 assert call('emu','avd','name').decode().splitlines()[0]==a.expected_avd and shell('getprop','ro.kernel.qemu').strip()=='1'
 assert not installed(TEST),'A different observer must not be replaced'
 actual=installed(PACKAGE);assert actual and shell('sha256sum',actual).split()[0]==a.sha256,'Exact candidate must already be installed'
 out=ROOT/'docs/validation/v1.15'/('deadend-native-'+a.label);shots=ROOT/'docs/screenshots/v1.15'/('deadend-native-'+a.label);assert not out.exists() and not shots.exists();out.mkdir();shots.mkdir(parents=True)
 shutil.copyfile(test,out/'deadend-observer.apk');shutil.copyfile(fixture,out/'host-fixture.json');shutil.copytree(HERE,out/'runner-source',ignore=shutil.ignore_patterns('__pycache__'))
 original={'size':shell('wm','size').strip(),'density':shell('wm','density').strip()}
 r={'passed':False,'serial':a.serial,'avd':a.expected_avd,'apk_sha256':a.sha256,'test_apk_sha256':sha(test),'fixture_sha256':sha(fixture),'phases':[],'screenshots':{},'display_before':original,'scope':'Real ACTION_VIEW public U31; legal wrong order; native dead-end panel and free Undo; cold resume and complete solution in unrewarded mode4; no injected preferences/history/currency/stock or private engine call'};installed_test=False;error=None
 try:
  (out/'test-install.txt').write_bytes(call('install','-t',str(test)));installed_test=True;assert shell('sha256sum',installed(TEST)).split()[0]==sha(test)
  crash_before=shell('logcat','-d','-b','crash');previous=None
  for phase in ['prepare','cold']:
   shell('am','force-stop',PACKAGE)
   log=call('shell','am','instrument','-w','-r','-e','expectedVersion','1.15.0-rc1','-e','expectedCode','17','-e','label',a.label,'-e','phase',phase,RUNNER,timeout=300);(out/(phase+'-instrumentation.txt')).write_bytes(log)
   remote='/sdcard/Android/data/'+PACKAGE+'/files/v115-deadend-native/'+a.label
   with tempfile.TemporaryDirectory(prefix='luma-deadend-') as tmp:
    call('pull',remote,tmp);pulled=Path(tmp)/a.label;data=json.loads((pulled/phase/'report.json').read_text());r['phases'].append(data);shutil.copytree(pulled/phase,out/phase)
    if (pulled/'checkpoint.json').exists():shutil.copyfile(pulled/'checkpoint.json',out/'checkpoint.json')
    dest=shots/phase;dest.mkdir()
    for name in data['captures']:
     png=pulled/phase/(name+'.png');shutil.copyfile(png,dest/png.name);r['screenshots'][phase+'/'+png.name]=sha(png)
   assert data['passed'],'Native phase '+phase+' failed'
   if previous is not None:assert previous!=data['processId'],'Cold process was reused'
   previous=data['processId']
  after=shell('logcat','-d','-b','crash');delta=after[len(crash_before):] if after.startswith(crash_before) else after;assert not delta.strip(),'New native crash entries'
  r['passed']=True
 except BaseException as failure:error=failure;r['error']=str(failure);(out/'host-failure.txt').write_text(traceback.format_exc())
 finally:
  shell('am','force-stop',PACKAGE)
  if installed_test:(out/'test-uninstall.txt').write_bytes(call('uninstall',TEST))
  r['test_package_absent']=not installed(TEST);r['candidate_sha256_after']=shell('sha256sum',installed(PACKAGE)).split()[0];r['exact_candidate_retained']=r['candidate_sha256_after']==a.sha256
  r['display_after']={'size':shell('wm','size').strip(),'density':shell('wm','density').strip()};r['display_unchanged']=r['display_after']==original;r['passed']=r['passed'] and r['test_package_absent'] and r['exact_candidate_retained'] and r['display_unchanged'];(out/'host-report.json').write_text(json.dumps(r,ensure_ascii=False,indent=2)+'\n')
 if error:raise error
 print(json.dumps({'passed':r['passed'],'checks':sum(x['checkCount'] for x in r['phases']),'touches':sum(x['touchCount'] for x in r['phases']),'screenshots':len(r['screenshots']),'report':str(out/'host-report.json')}));return 0 if r['passed'] else 1
if __name__=='__main__':raise SystemExit(main())
