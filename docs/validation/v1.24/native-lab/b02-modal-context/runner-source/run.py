#!/usr/bin/env python3
"""Exact-byte Lab124 observer on the pinned task AVD; production preference isolation is mandatory."""
from pathlib import Path
import argparse,base64,datetime,hashlib,json,os,re,shutil,subprocess,tempfile,time,uuid
ROOT=Path(__file__).resolve().parents[2];HERE=Path(__file__).resolve().parent
OUT=ROOT/'docs/validation/v1.24/native-lab';SERIAL='emulator-5634';AVD='Luma123-RC2-Fresh-QA'
RECEIPT_SHA='1cca407cfd82d4078854c912a6b22af1e1293e743e9d88a227757105a29daf7e'
PACKAGE='com.androidhit.lumadrift.sandbox';PRODUCTION='com.androidhit.lumadrift';TEST=PACKAGE+'.v124labtest'
RUNNER=TEST+'/com.androidhit.lumadrift.qa.v124lab.BattleInstrumentation'
SIGNER='dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287'
PRIOR_LAB_SHA='099d4dc7d38cb4552b3b1ad46f7e3ccb70b304c7c8a1c6c332e16ada00ba558c'
PRODUCTION_SHA='429a443c9228787c7b854d36a40f0bcf7d52fdc8a3601214cd27b5cfae6945fb'
ALLOWED_OPS={'drain-energy', 'scroll', 'tap', 'top', 'language', 'capture-fast', 'assert-int-delta', 'solve', 'back', 'earn-through', 'solve-active', 'assert-id', 'wait', 'adventure', 'capture', 'assert-label', 'piece', 'assert-pref', 'snapshot', 'firstlight', 'assert-energy', 'assert-victory', 'remember-value', 'open-level', 'assert-value-unchanged', 'tap-id','assert-battle','assert-battle-unchanged','assert-clock-advance','remember-battle','remember-clock','seek-id','assert-controls'}
def sha(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()
def main():
 p=argparse.ArgumentParser(description=__doc__)
 for key in ['candidate','observer','steps','candidate-manifest']:p.add_argument('--'+key,type=Path,required=True)
 for key in ['sha256','observer-sha256','production-sha256','version','phase','label','manifest-sha256']:p.add_argument('--'+key,required=True)
 p.add_argument('--size',choices=['1080x2400','1080x1300']);p.add_argument('--font-scale',choices=['1.0','1.3'])
 p.add_argument('--first-install',action='store_true');p.add_argument('--code',type=int,required=True);p.add_argument('--snapshot-only',action='store_true');p.add_argument('--upgrade-from-sha256')
 a=p.parse_args();a.candidate_manifest=a.candidate_manifest.resolve();receipt=ROOT/'docs/validation/v1.23/native/device-creation-rc2-fresh/creation.json'
 assert sha(receipt)==RECEIPT_SHA,'Exact immutable creation receipt required';provenance=json.loads(receipt.read_text());assert provenance['fresh'] and provenance['avd']==AVD and provenance['serial']==SERIAL
 assert a.candidate_manifest.resolve().is_relative_to((ROOT/'docs/validation/v1.24').resolve()),'Only 1.24 candidate manifests inside the evidence tree'
 assert re.fullmatch('[a-f0-9]{64}',a.manifest_sha256) and sha(a.candidate_manifest)==a.manifest_sha256,'Exact coordinator-pinned candidate manifest required'
 candidate_manifest=json.loads(a.candidate_manifest.read_text());artifact=candidate_manifest['apks']['lab']
 CANDIDATE_SHA=artifact['sha256']
 assert re.fullmatch('[a-f0-9]{64}',CANDIDATE_SHA) and a.sha256==CANDIDATE_SHA and sha(a.candidate)==CANDIDATE_SHA and a.candidate.stat().st_size==artifact['bytes'],'Only frozen 1.24 Lab candidate bytes authorized'
 assert artifact['version']=='1.24.0-sandbox-rc1' and artifact['code']==32,'Manifest must identify 1.24 Lab code32'
 assert not a.first_install,'This 1.24 harness preserves the existing Lab profile; first install is not authorized'
 assert a.production_sha256==PRODUCTION_SHA,'Exact productionE isolation reference required'
 assert re.fullmatch('[a-f0-9]{64}',a.observer_sha256) and sha(a.observer)==a.observer_sha256
 build_receipt_path=a.observer.parent/'build-receipt.json';build_receipt=json.loads(build_receipt_path.read_text());assert build_receipt['sha256']==a.observer_sha256 and build_receipt['package']==TEST and build_receipt['target']==PACKAGE,'Exact observer build receipt required'
 current_sources={str(path.relative_to(HERE)):sha(path) for path in sorted(HERE.rglob('*')) if path.is_file() and '__pycache__' not in path.parts};assert build_receipt['source_sha256']==current_sources,'Observer source differs from the compiled SDK build receipt'
 assert a.version=='1.24.0-sandbox-rc1' and a.code==32,'Exact new sandbox version/code required'
 assert a.upgrade_from_sha256 in (None,PRIOR_LAB_SHA),'Only coordinator-authorized upgrade from exact 1.23 E Lab is supported'
 assert re.fullmatch('[a-z][a-z0-9_-]{1,50}',a.phase) and re.fullmatch('[A-Za-z0-9_-]{1,65}',a.label)
 steps=json.loads(a.steps.read_text());assert isinstance(steps,list) and 1<=len(steps)<=160
 assert all(s.get('op') in ALLOWED_OPS for s in steps),'Only explicit bounded SDK UI gestures and read-only assertions authorized'
 expected={entry['path']:entry for entry in candidate_manifest['source_inputs']}
 assert len(expected)==len(candidate_manifest['source_inputs']) and 450<=len(expected)<=650,'Complete bounded app input manifest required'
 for name,entry in expected.items():
  path=ROOT/name
  assert (name.startswith('app/src/') or name in ('app/build.gradle','app/proguard-rules.pro','build.gradle','gradle.properties','settings.gradle')) and '..' not in Path(name).parts and not path.is_symlink() and path.resolve().is_relative_to(ROOT.resolve()),'Only bounded regular app/build inputs'
  assert path.is_file() and path.stat().st_size==entry['bytes'] and sha(path)==entry['sha256'],'Frozen app input differs: '+name
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
  with tempfile.TemporaryDirectory(prefix='luma124-installed-proof-') as d:
   target=Path(d)/'installed.apk';call('pull',path,target,timeout=180);return sha(target)
 assert call('emu','avd','name').decode().splitlines()[0].strip()==AVD and shell('getprop','ro.kernel.qemu').strip()=='1','Pinned task emulator only'
 assert installed(PRODUCTION),'Preserved production profile must be present'
 assert (not installed(PACKAGE)) if a.first_install else bool(installed(PACKAGE)), 'First install requires absence; ordinary run requires presence'
 assert not installed(TEST),'Observer must initially be absent'
 assert installed_hash(PRODUCTION)==PRODUCTION_SHA,'Production installed bytes differ'
 previous_hash=None if a.first_install else installed_hash(PACKAGE);assert previous_hash in ((PRIOR_LAB_SHA,) if a.upgrade_from_sha256 else (CANDIDATE_SHA,)),'Sandbox installed bytes differ'
 out.mkdir(parents=True);shutil.copyfile(a.candidate_manifest,out/'candidate-manifest.json');shutil.copyfile(receipt,out/'device-creation.json');shutil.copyfile(a.observer,out/'battle-observer.apk');shutil.copyfile(build_receipt_path,out/'observer-build-receipt.json');(out/'requested-steps.json').write_text(json.dumps(steps,indent=2)+'\n')
 sources={}
 for path in sorted(HERE.rglob('*')):
  if path.is_file() and '__pycache__' not in path.parts:
   relative=path.relative_to(HERE);copy=out/'runner-source'/relative;copy.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(path,copy);sources[str(relative)]=sha(copy)
 (out/'source-snapshot.json').write_text(json.dumps(sources,indent=2)+'\n')
 report={'passed':False,'created_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'serial':SERIAL,'avd':AVD,'package':PACKAGE,'phase':a.phase,'expected_version':a.version,'expected_code':a.code,'apk_sha256':a.sha256,'observer_sha256':a.observer_sha256,'observer_build_receipt_sha256':sha(build_receipt_path),'production_apk_sha256':PRODUCTION_SHA,'previous_lab_apk_sha256':previous_hash,'creation_receipt_sha256':RECEIPT_SHA,'snapshot_only':a.snapshot_only,'upgrade_from_sha256':a.upgrade_from_sha256,'first_install':a.first_install,'candidate_manifest':str(a.candidate_manifest.relative_to(ROOT)),'candidate_manifest_sha256':a.manifest_sha256,'frozen_source_count':len(expected),'run_token':str(uuid.uuid4()),'scope':'Pinned 5634 task emulator only. Snapshot persistent production preferences before and after Lab and refuse any change. Preserve old sandbox preferences byte-for-byte before first 1.24 Activity. Actual SDK touchscreen Lab simulations, optional test-ad UI, earned campaign gestures, cosmetics; no direct grants, clears, preference writes, injected fixtures/progress, external identity or real commerce.'}
 started=time.monotonic();observer_installed=False;production_before=None;production_after=None;configuration_changed=False
 original_size=shell('wm','size');original_font=shell('settings','get','system','font_scale').strip();report['original_configuration']={'size':original_size.strip(),'font_scale':original_font}
 def preference_snapshot(package,name):
  cls='ProductionSnapshot' if package==PRODUCTION else 'SandboxSnapshot';component=TEST+'/com.androidhit.lumadrift.qa.v124lab.'+cls
  result=call('shell','am','instrument','-w','-r','-e','runToken',report['run_token'],'-e','snapshotName',name,component,timeout=120)
  (out/(name+'-instrumentation.txt')).write_bytes(result);assert b'INSTRUMENTATION_CODE: -1' in result and b'INSTRUMENTATION_FAILED' not in result,name+' snapshot process failed'
  remote='/sdcard/Android/data/'+package+'/files/v124-native-lab-isolation/'+report['run_token']+'/'+name;call('pull',remote,out/name,timeout=90)
  snapshot=json.loads((out/name/'report.json').read_text());assert snapshot['passed'] and snapshot['package']==package and snapshot['runToken']==report['run_token'] and snapshot['preferenceFiles'],'Nonempty preserved preference files required';return snapshot
 try:
  for label,path in [('candidate',a.candidate),('observer',a.observer)]:
   proof=subprocess.check_output([str(sdk/'build-tools/36.0.0/apksigner'),'verify','--verbose','--print-certs',str(path)],stderr=subprocess.STDOUT,env=env).decode();(out/(label+'-signature.txt')).write_text(proof);assert re.findall(r'Signer #1 certificate SHA-256 digest: ([a-f0-9]+)',proof)==[SIGNER],label+' signer differs'
  (out/'observer-install.txt').write_bytes(call('install','-t',a.observer,timeout=180));observer_installed=True
  production_before=preference_snapshot(PRODUCTION,'production-before')
  if a.first_install:(out/'first-install.txt').write_bytes(call('install',a.candidate,timeout=180))
  if a.upgrade_from_sha256:
   lab_before=preference_snapshot(PACKAGE,'sandbox-before-upgrade');(out/'authorized-update.txt').write_bytes(call('install','-r',a.candidate,timeout=180));assert installed_hash(PACKAGE)==CANDIDATE_SHA
   lab_after=preference_snapshot(PACKAGE,'sandbox-after-upgrade-before-activity');assert lab_before['preferenceFiles']==lab_after['preferenceFiles'],'Upgrade altered preserved Lab preferences before first new Activity';report['upgrade_preferences_exactly_preserved']=True
  report['installed_sha256']=installed_hash(PACKAGE);assert report['installed_sha256']==CANDIDATE_SHA
  if a.size or a.font_scale:
   configuration_changed=True
   if a.size:shell('wm','size',a.size)
   if a.font_scale:shell('settings','put','system','font_scale',a.font_scale)
   time.sleep(1)
  report['device']={key:shell(*command).strip() for key,command in {'sdk':['getprop','ro.build.version.sdk'],'abi':['getprop','ro.product.cpu.abi'],'size':['wm','size'],'density':['wm','density'],'font_scale':['settings','get','system','font_scale']}.items()}
  encoded=base64.b64encode(json.dumps(steps,separators=(',',':'),ensure_ascii=False).encode()).decode()
  command=['shell','am','instrument','-w','-r','-e','phase',a.phase,'-e','runToken',report['run_token'],'-e','expectedVersion',a.version,'-e','expectedCode',str(a.code),'-e','stepsBase64',encoded,'-e','snapshotOnly',str(a.snapshot_only).lower(),'-e','fresh',str(a.first_install).lower(),RUNNER]
  result=call(*command,timeout=1200);(out/'instrumentation.txt').write_bytes(result);remote='/sdcard/Android/data/'+PACKAGE+'/files/v124-native-lab/'+report['run_token']+'/'+a.phase;call('pull',remote,out/'native',timeout=180)
  native=json.loads((out/'native/report.json').read_text());report['native']=native;assert b'INSTRUMENTATION_CODE: -1' in result and b'INSTRUMENTATION_FAILED' not in result and native['passed'] and native['runToken']==report['run_token'],'Native Lab UI phase failed'
  production_after=preference_snapshot(PRODUCTION,'production-after');assert production_before['preferenceFiles']==production_after['preferenceFiles'],'Lab activity altered production persistent preferences'
  report['production_preferences_exactly_preserved']=True;report['production_preference_file_count']=len(production_before['preferenceFiles']);report['passed']=True
 except BaseException as failure:
  report['error']=str(failure)
  try:(out/'failure-android-runtime.txt').write_bytes(call('logcat','-d','-v','threadtime','-s','AndroidRuntime:V',timeout=30))
  except Exception as diagnostic:report['diagnostic_error']=str(diagnostic)
  raise
 finally:
  if configuration_changed:
   try:
    old_override=re.findall(r'Override size: ([0-9]+x[0-9]+)',original_size);shell('wm','size',old_override[0] if old_override else 'reset')
    if re.fullmatch(r'[0-9]+(?:\.[0-9]+)?',original_font):shell('settings','put','system','font_scale',original_font)
    else:shell('settings','delete','system','font_scale')
    report['configuration_restored']=shell('wm','size')==original_size and shell('settings','get','system','font_scale').strip()==original_font
    if not report['configuration_restored']:report['passed']=False
   except Exception as failure:report['configuration_restore_error']=str(failure);report['passed']=False
  if observer_installed and production_before is not None and production_after is None:
   try:
    production_after=preference_snapshot(PRODUCTION,'production-after-failure');report['production_preferences_exactly_preserved']=production_before['preferenceFiles']==production_after['preferenceFiles'];report['production_preference_file_count']=len(production_before['preferenceFiles'])
    if not report['production_preferences_exactly_preserved']:report['production_isolation_error']='Production persistent preferences changed during failed Lab run'
   except Exception as diagnostic:report['production_after_failure_error']=str(diagnostic)
  if observer_installed:
   try:(out/'observer-uninstall.txt').write_bytes(call('uninstall',TEST))
   except Exception as failure:report['observer_cleanup_error']=str(failure);report['passed']=False
  report['elapsed_seconds']=round(time.monotonic()-started,3);report['evidence_sha256']={str(p.relative_to(out)):sha(p) for p in sorted(out.rglob('*')) if p.is_file() and 'runner-source' not in p.parts};(out/'report.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps({'passed':report['passed'],'evidence':str(out),'phase':a.phase,'checks':report.get('native',{}).get('checkCount'),'touches':report.get('native',{}).get('touchCount'),'production_isolation':report.get('production_preferences_exactly_preserved',False),'error':report.get('error')}))
 if not report['passed']:raise SystemExit(1)
if __name__=='__main__':main()
