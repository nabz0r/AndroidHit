import datetime,hashlib,json,os,re,subprocess,time
from pathlib import Path
root=Path('/Users/n/androidhit');out=Path('/tmp/luma124-ci-home-fix/remaining');out.mkdir(exist_ok=False);runner=out/'runner-temp';(runner/'luma-puzzle-contracts').mkdir(parents=True)
workflow=root/'.github/workflows/puzzle-contracts.yml';lines=workflow.read_text().splitlines();steps=[];name=None;index=0
while index<len(lines):
 line=lines[index]
 if line.startswith('  packaged-companion-art:'):break
 if line.startswith('      - name: '):name=line.split(': ',1)[1]
 if line.startswith('        run:'):
  command=line.split('run:',1)[1].strip()
  if command=='|':
   parts=[];index+=1
   while index<len(lines)and(lines[index].startswith('          ')or not lines[index].strip()):
    parts.append(lines[index][10:]if lines[index].startswith('          ')else'');index+=1
   index-=1;command='\n'.join(parts).strip()
  steps.append({'name':name,'originalCommand':command})
 index+=1
start=next(i for i,s in enumerate(steps)if s['name']=='Check three-room ownership, migration and camera geometry');steps=steps[start:]
env=os.environ.copy();env['JAVA_HOME']='/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home';env['RUNNER_TEMP']=str(runner)
jar=Path('/Users/n/.gradle/caches/modules-2/files-2.1/org.json/json/20240303/ebb88e8fb5122b7506d5cf1d69f1ccdb790d22a/json-20240303.jar');assert hashlib.sha256(jar.read_bytes()).hexdigest()=='3cf6cd6892e32e2b4c1c39e0f52f5248a2f5b37646fdfbb79a66b46b618414ed';env['LUMA_JSON_JAR']=str(jar)
source=json.loads((root/'docs/validation/site-v1.24/release/source-manifest.json').read_text());native=json.loads((root/'docs/validation/v1.24/candidate-d/candidate.json').read_text())
def sha(path):
 h=hashlib.sha256()
 with path.open('rb')as f:
  for b in iter(lambda:f.read(1048576),b''):h.update(b)
 return h.hexdigest()
def preserved():
 for name,digest in source['frozen_inputs'].items():assert sha(root/name)==digest,name
 for row in native['source_inputs']:assert sha(root/row['path'])==row['sha256'],row['path']
preserved();results=[]
for number,step in enumerate(steps,1):
 command=step['originalCommand'];adaptations=[]
 if 'curl --fail' in command:
  command='bash scripts/test-energy-purchase-v120.sh\nbash scripts/test-wallet-energy-transport-v120.sh';adaptations.append('Use the already cached org.json20240303 JAR after verifying the exact CI SHA256; no dependency download. Same two test commands.')
 if step['name']=='Check three-room ownership, migration and camera geometry':
  command=command.replace('bash scripts/test-home-v115.sh','bash scripts/test-home-v115.sh "$RUNNER_TEMP/luma-puzzle-contracts/home115.json"').replace('bash scripts/test-home-v115-geometry.sh','bash scripts/test-home-v115-geometry.sh "$RUNNER_TEMP/luma-puzzle-contracts/all48-furniture-rotations.png"');adaptations.append('Write new home JSON and optional Java2D preview under RUNNER_TEMP only; all native/assertion calls unchanged.')
 path=out/('%02d.log'%number);started=time.monotonic()
 with path.open('xb')as log:r=subprocess.run(['bash','-euo','pipefail','-c',command],cwd=root,env=env,stdout=log,stderr=subprocess.STDOUT,timeout=300)
 result={'name':step['name'],'exitCode':r.returncode,'elapsedSeconds':round(time.monotonic()-started,3),'originalCommand':step['originalCommand'],'executedCommand':command,'adaptations':adaptations,'originalCombinedOutput':str(path),'outputSha256':sha(path)};results.append(result)
 receipt={'status':'RUNNING','startedFromStep':steps[0]['name'],'workflowSha256':sha(workflow),'stepCount':len(steps),'completedSteps':len(results),'steps':results,'env':{'JAVA_HOME':env['JAVA_HOME'],'RUNNER_TEMP':env['RUNNER_TEMP'],'LUMA_JSON_JAR':str(jar)},'jsonJarSha256':sha(jar),'networkRequested':False,'appOrPublicMutation':False}
 (out/'receipt.json').write_text(json.dumps(receipt,indent=2)+'\n');print(json.dumps({'step':number,'total':len(steps),'name':step['name'],'exitCode':r.returncode,'seconds':result['elapsedSeconds']}),flush=True)
 if r.returncode!=0:
  receipt['status']='FAIL';(out/'receipt.json').write_text(json.dumps(receipt,indent=2)+'\n');print(path.read_text()[-3000:],flush=True);raise SystemExit(1)
preserved();receipt['status']='PASS';receipt['verifiedFrozenSiteInputs']=len(source['frozen_inputs']);receipt['verifiedNativeBuildInputs']=len(native['source_inputs']);receipt['completedAtUtc']=datetime.datetime.now(datetime.timezone.utc).isoformat();(out/'receipt.json').write_text(json.dumps(receipt,indent=2)+'\n');print(json.dumps({'status':'PASS','remainingWorkflowSteps':len(steps),'siteInputsUnchanged':len(source['frozen_inputs']),'nativeInputsUnchanged':len(native['source_inputs'])}),flush=True)
