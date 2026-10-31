#!/usr/bin/env python3
import pathlib,subprocess,tempfile,hashlib,json,argparse,datetime,os
R=pathlib.Path(__file__).resolve().parents[2];p=argparse.ArgumentParser();p.add_argument('--output',required=True,type=pathlib.Path);p.add_argument('--baseline',action='store_true');a=p.parse_args();out=a.output.resolve();out.mkdir(parents=True,exist_ok=False)
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
original=R/'docs/validation/v1.24/native-lab/b01-upgrade-discovery/native/retained-after-ui.json';fixture=json.loads(original.read_text())['luma_energy_purchases_v120'];(out/'inherited-wallet.json').write_text(json.dumps(fixture,indent=2)+'\n')
sources=sorted([*R.glob('scripts/test-support/android/annotation/*.java'),*R.glob('scripts/test-support/android/content/*.java'),*R.glob('scripts/test-support/android/graphics/*.java'),*R.glob('app/src/main/java/com/androidhit/lumadrift/data/*.java'),*R.glob('app/src/main/java/com/androidhit/lumadrift/engine/*.java'),R/'scripts/test-support/energy/com/androidhit/lumadrift/commerce/CommerceConfig.java',R/'scripts/test-support/energy/com/androidhit/lumadrift/wallet/ProductionWalletClient.java',R/'app/src/main/java/com/androidhit/lumadrift/commerce/BillingPolicy.java',R/'app/src/main/java/com/androidhit/lumadrift/wallet/EnergyPurchaseController.java',R/'app/src/main/java/com/androidhit/lumadrift/wallet/EnergyPurchaseJournal.java',R/'scripts/playtest-2026-10-31/EnergyRefusalV124Test.java'])
overrides={}
if a.baseline:
 manifest=json.loads((R/'docs/validation/v1.24/candidate-b/candidate.json').read_text());bound={row['path']:row['sha256']for row in manifest['source_inputs']}
 for name in ['EnergyPurchaseController.java','EnergyPurchaseJournal.java']:
  relative='app/src/main/java/com/androidhit/lumadrift/wallet/'+name;body=subprocess.check_output(['git','show','HEAD:'+relative],cwd=R);assert hashlib.sha256(body).hexdigest()==bound[relative],'Baseline source must match exact frozen candidate B';target=out/'baseline-source'/name;target.parent.mkdir(exist_ok=True);target.write_bytes(body);sources[sources.index(R/relative)]=target;overrides[relative]={'compiledFile':str(target),'sha256':sha(target)}
def label(p):
 try:return str(p.relative_to(R))
 except ValueError:return str(p)
r={'status':'RUNNING','startedAtUtc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'sourceInputs':{label(p):sha(p)for p in sources},'baselineOverrides':overrides,'runnerSha256':sha(pathlib.Path(__file__)),'nativeFixtureSource':str(original.relative_to(R)),'nativeFixtureSourceSha256':sha(original),'fixtureInitialization':'Real journal.seedLab() before overlay: native observer omits all keys containing seed. No other native state is invented.','scope':'Actual local Java models/controller, readback fixture and explicit prefs/HTTP doubles only; no emulator mutation, APK build or provider call.'}
j=pathlib.Path(os.environ.get('JAVA_HOME','/opt/homebrew/opt/openjdk@17'))/'bin';jar=sorted((pathlib.Path.home()/'.gradle/caches/modules-2/files-2.1/org.json/json').rglob('json-*.jar'))[-1]
try:
 with tempfile.TemporaryDirectory(prefix='luma124-energy-refusal-')as tmp:
  for name,cmd in [('compile',[str(j/'javac'),'-Xlint:all','-cp',str(jar),'-d',tmp,*map(str,sources)]),('run',[str(j/'java'),'-cp',tmp+':'+str(jar),'com.androidhit.lumadrift.wallet.EnergyRefusalV124Test',str(out/'inherited-wallet.json'),str(out/'report.json')])]:
   q=subprocess.run(cmd,cwd=R,capture_output=True);(out/(name+'.stdout.log')).write_bytes(q.stdout);(out/(name+'.stderr.log')).write_bytes(q.stderr);print(q.stdout.decode(),end='')
   if q.returncode:raise RuntimeError(name+' exit '+str(q.returncode)+'; original logs retained')
 assert all(sha(p)==r['sourceInputs'][label(p)]for p in sources),'Source drift during host run'
 r['status']='PASS'
except BaseException as e:r['status']='FAIL';r['error']=str(e);raise
finally:
 r['completedAtUtc']=datetime.datetime.now(datetime.timezone.utc).isoformat();r['outputs']={str(p.relative_to(out)):{'sha256':sha(p),'bytes':p.stat().st_size}for p in out.rglob('*')if p.is_file()};(out/'receipt.json').write_text(json.dumps(r,indent=2)+'\n')
