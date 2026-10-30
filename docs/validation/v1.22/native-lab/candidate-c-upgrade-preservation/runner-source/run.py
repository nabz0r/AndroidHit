#!/usr/bin/env python3
"""Exact-byte Lab122 observer on the pinned task AVD; production preference isolation is mandatory."""
from pathlib import Path
import argparse,base64,datetime,hashlib,json,os,re,shutil,subprocess,tempfile,time,uuid
ROOT=Path(__file__).resolve().parents[2];HERE=Path(__file__).resolve().parent
OUT=ROOT/'docs/validation/v1.22/native-lab';SERIAL='emulator-5602';AVD='Luma119-Battle-QA'
RECEIPT_SHA='022362d42b38acf665a2266d1fb2dbec40981f5640d6955d7d86c94c1a1196ae'
PACKAGE='com.androidhit.lumadrift.sandbox';PRODUCTION='com.androidhit.lumadrift';TEST=PACKAGE+'.v122test'
RUNNER=TEST+'/com.androidhit.lumadrift.qa.v122.BattleInstrumentation'
SIGNER='dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287'
CANDIDATE_SHA='882481a2da12cc0e4a36af804d211a12c942fab2bbaef88d80ed740e2a3490f8'
PRODUCTION_SHA='9b2593388de22b1f50f49bea05b9b02008d4574f5cbb9012fb0df5a64c47a44e'
PRIOR_LAB_SHA='9c0998683f09a1d940a97a0dc5badea98a5ab5c84ea2ba814917e30f8df5235b'
ALLOWED_OPS={'snapshot','capture','capture-fast','adventure','language','assert-pref','assert-label','assert-id','remember-value','assert-value-unchanged'}
def sha(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()
def main():
 p=argparse.ArgumentParser(description=__doc__)
 for key in ['candidate','observer','steps']:p.add_argument('--'+key,type=Path,required=True)
 for key in ['sha256','observer-sha256','production-sha256','version','phase','label']:p.add_argument('--'+key,required=True)
 p.add_argument('--code',type=int,required=True);p.add_argument('--snapshot-only',action='store_true');p.add_argument('--upgrade-from-sha256')
 a=p.parse_args();receipt=ROOT/'docs/validation/v1.19/native/device-creation/creation.json'
 assert sha(receipt)==RECEIPT_SHA,'Exact immutable creation receipt required';provenance=json.loads(receipt.read_text());assert provenance['fresh'] and provenance['avd']==AVD and provenance['serial']==SERIAL
 assert a.sha256==CANDIDATE_SHA and sha(a.candidate)==CANDIDATE_SHA,'Only frozen candidateC Lab122 bytes authorized'
 assert a.production_sha256==PRODUCTION_SHA,'Exact productionC isolation reference required'
 assert re.fullmatch('[a-f0-9]{64}',a.observer_sha256) and sha(a.observer)==a.observer_sha256
 build_receipt_path=a.observer.parent/'build-receipt.json';build_receipt=json.loads(build_receipt_path.read_text());assert build_receipt['sha256']==a.observer_sha256 and build_receipt['package']==TEST and build_receipt['target']==PACKAGE,'Exact observer build receipt required'
 current_sources={str(path.relative_to(HERE)):sha(path) for path in sorted(HERE.rglob('*')) if path.is_file() and '__pycache__' not in path.parts};assert build_receipt['source_sha256']==current_sources,'Observer source differs from the compiled SDK build receipt'
 assert a.version=='1.22.0-sandbox-rc1' and a.code==30,'Exact new sandbox version/code required'
 assert not a.upgrade_from_sha256 or a.upgrade_from_sha256==PRIOR_LAB_SHA,'Only recorded preserved1.21 sandbox may be upgraded'
 assert re.fullmatch('[a-z][a-z0-9_-]{1,50}',a.phase) and re.fullmatch('[A-Za-z0-9_-]{1,65}',a.label)
 steps=json.loads(a.steps.read_text());assert isinstance(steps,list) and 1<=len(steps)<=40
 assert all(s.get('op') in ALLOWED_OPS for s in steps),'This smoke runner refuses purchases, currency changes, progression, solver/certificate execution and direct writes'
 out=OUT/a.label;assert not out.exists(),'Never overwrite earlier accepted or failed evidence'
 sdk=Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'));adb=sdk/'platform-tools/adb';env=os.environ.copy();env.setdefault('JAVA_HOME','/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home')
 def call(*parts,timeout=90):return subprocess.check_output([str(adb),'-s',SERIAL,*map(str,parts)],stderr=subprocess.STDOUT,timeout=timeout,env=env)
 def shell(*parts):return call('shell',*parts).decode(errors='replace')
 def installed(package):
  r=subprocess.run([str(adb),'-s',SERIAL,'shell','pm','path',package],capture_output=True,text=True,timeout=30);assert r.returncode in (0,1)
  paths=[line[8:] for line in r.stdout.splitlines() if line.startswith('package:')];assert len(paths)<=1 and all(x.startswith('/data/app/') for x in paths)
  return paths[0] if paths else None
 def installed_hash(package):
  path=installed(package);assert path,'Expected preserved package absent: '+package
  with tempfile.TemporaryDirectory(prefix='luma122-installed-proof-') as d:
   target=Path(d)/'installed.apk';call('pull',path,target,timeout=180);return sha(target)
 assert call('emu','avd','name').decode().splitlines()[0].strip()==AVD and shell('getprop','ro.kernel.qemu').strip()=='1','Pinned task emulator only'
 assert installed(PACKAGE) and installed(PRODUCTION),'Never first-install or replace a missing preserved profile'
 assert not installed(TEST),'Observer must initially be absent'
 assert installed_hash(PRODUCTION)==PRODUCTION_SHA,'Production installed bytes differ'
 previous_hash=installed_hash(PACKAGE);assert previous_hash==(a.upgrade_from_sha256 or CANDIDATE_SHA),'Sandbox installed bytes differ from explicit update/control reference'
 out.mkdir(parents=True);shutil.copyfile(receipt,out/'device-creation.json');shutil.copyfile(a.observer,out/'battle-observer.apk');shutil.copyfile(build_receipt_path,out/'observer-build-receipt.json');(out/'requested-steps.json').write_text(json.dumps(steps,indent=2)+'\n')
 sources={}
 for path in sorted(HERE.rglob('*')):
  if path.is_file() and '__pycache__' not in path.parts:
   relative=path.relative_to(HERE);copy=out/'runner-source'/relative;copy.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(path,copy);sources[str(relative)]=sha(copy)
 (out/'source-snapshot.json').write_text(json.dumps(sources,indent=2)+'\n')
 report={'passed':False,'created_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'serial':SERIAL,'avd':AVD,'package':PACKAGE,'phase':a.phase,'expected_version':a.version,'expected_code':a.code,'apk_sha256':a.sha256,'observer_sha256':a.observer_sha256,'observer_build_receipt_sha256':sha(build_receipt_path),'production_apk_sha256':PRODUCTION_SHA,'previous_lab_apk_sha256':previous_hash,'creation_receipt_sha256':RECEIPT_SHA,'snapshot_only':a.snapshot_only,'upgrade_from_sha256':a.upgrade_from_sha256,'run_token':str(uuid.uuid4()),'scope':'Pinned task emulator only. Snapshot persistent production preferences before and after Lab and refuse any change. Preserve old sandbox preferences byte-for-byte before first1.22 Activity. Actual UI language/capture smoke only; no grants, clears, preference writes, injected fixtures, progression or purchases.'}
 started=time.monotonic();observer_installed=False;production_before=None;production_after=None
 def preference_snapshot(package,name):
  cls='ProductionSnapshot' if package==PRODUCTION else 'SandboxSnapshot';component=TEST+'/com.androidhit.lumadrift.qa.v122.'+cls
  result=call('shell','am','instrument','-w','-r','-e','runToken',report['run_token'],'-e','snapshotName',name,component,timeout=120)
  (out/(name+'-instrumentation.txt')).write_bytes(result);assert b'INSTRUMENTATION_CODE: -1' in result and b'INSTRUMENTATION_FAILED' not in result,name+' snapshot process failed'
  remote='/sdcard/Android/data/'+package+'/files/v122-native-lab-isolation/'+report['run_token']+'/'+name;call('pull',remote,out/name,timeout=90)
  snapshot=json.loads((out/name/'report.json').read_text());assert snapshot['passed'] and snapshot['package']==package and snapshot['runToken']==report['run_token'] and snapshot['preferenceFiles'],'Nonempty preserved preference files required';return snapshot
 try:
  for label,path in [('candidate',a.candidate),('observer',a.observer)]:
   proof=subprocess.check_output([str(sdk/'build-tools/36.0.0/apksigner'),'verify','--verbose','--print-certs',str(path)],stderr=subprocess.STDOUT,env=env).decode();(out/(label+'-signature.txt')).write_text(proof);assert re.findall(r'Signer #1 certificate SHA-256 digest: ([a-f0-9]+)',proof)==[SIGNER],label+' signer differs'
  (out/'observer-install.txt').write_bytes(call('install','-t',a.observer,timeout=180));observer_installed=True
  production_before=preference_snapshot(PRODUCTION,'production-before')
  if a.upgrade_from_sha256:
   lab_before=preference_snapshot(PACKAGE,'sandbox-before-upgrade');(out/'authorized-update.txt').write_bytes(call('install','-r',a.candidate,timeout=180));assert installed_hash(PACKAGE)==CANDIDATE_SHA
   lab_after=preference_snapshot(PACKAGE,'sandbox-after-upgrade-before-activity');assert lab_before['preferenceFiles']==lab_after['preferenceFiles'],'Upgrade altered preserved Lab preferences before first new Activity';report['upgrade_preferences_exactly_preserved']=True
  report['installed_sha256']=installed_hash(PACKAGE);assert report['installed_sha256']==CANDIDATE_SHA
  report['device']={key:shell(*command).strip() for key,command in {'sdk':['getprop','ro.build.version.sdk'],'abi':['getprop','ro.product.cpu.abi'],'size':['wm','size'],'density':['wm','density']}.items()}
  encoded=base64.b64encode(json.dumps(steps,separators=(',',':'),ensure_ascii=False).encode()).decode()
  command=['shell','am','instrument','-w','-r','-e','phase',a.phase,'-e','runToken',report['run_token'],'-e','expectedVersion',a.version,'-e','expectedCode',str(a.code),'-e','stepsBase64',encoded,'-e','snapshotOnly',str(a.snapshot_only).lower(),RUNNER]
  result=call(*command,timeout=1200);(out/'instrumentation.txt').write_bytes(result);remote='/sdcard/Android/data/'+PACKAGE+'/files/v122-native-lab/'+report['run_token']+'/'+a.phase;call('pull',remote,out/'native',timeout=180)
  native=json.loads((out/'native/report.json').read_text());report['native']=native;assert b'INSTRUMENTATION_CODE: -1' in result and b'INSTRUMENTATION_FAILED' not in result and native['passed'] and native['runToken']==report['run_token'],'Native smoke failed'
  production_after=preference_snapshot(PRODUCTION,'production-after');assert production_before['preferenceFiles']==production_after['preferenceFiles'],'Lab activity altered production persistent preferences'
  report['production_preferences_exactly_preserved']=True;report['production_preference_file_count']=len(production_before['preferenceFiles']);report['passed']=True
 except BaseException as failure:
  report['error']=str(failure)
  try:(out/'failure-android-runtime.txt').write_bytes(call('logcat','-d','-v','threadtime','-s','AndroidRuntime:V',timeout=30))
  except Exception as diagnostic:report['diagnostic_error']=str(diagnostic)
  raise
 finally:
  if observer_installed and production_before is not None and production_after is None:
   try:
    production_after=preference_snapshot(PRODUCTION,'production-after-failure');report['production_preferences_exactly_preserved']=production_before['preferenceFiles']==production_after['preferenceFiles'];report['production_preference_file_count']=len(production_before['preferenceFiles'])
    if not report['production_preferences_exactly_preserved']:report['production_isolation_error']='Production persistent preferences changed during failed Lab run'
   except Exception as diagnostic:report['production_after_failure_error']=str(diagnostic)
  if observer_installed:
   try:(out/'observer-uninstall.txt').write_bytes(call('uninstall',TEST))
   except Exception as failure:report['observer_cleanup_error']=str(failure);report['passed']=False
  report['elapsed_seconds']=round(time.monotonic()-started,3);report['evidence_sha256']={str(p.relative_to(out)):sha(p) for p in sorted(out.rglob('*')) if p.is_file() and 'runner-source' not in p.parts};(out/'report.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps({'passed':report['passed'],'evidence':str(out),'phase':a.phase,'checks':report.get('native',{}).get('checkCount'),'touches':report.get('native',{}).get('touchCount'),'production_isolation':report.get('production_preferences_exactly_preserved',False),'error':report.get('error')}))
if __name__=='__main__':main()
