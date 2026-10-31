#!/usr/bin/env python3
"""Isolated host tests; preserves each run and binds compiled/executed source inputs."""
import argparse,datetime,hashlib,json,os,pathlib,shutil,subprocess,tempfile
R=pathlib.Path(__file__).resolve().parents[2]
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def run(cmd,base,name):
 p=subprocess.run(cmd,cwd=R,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
 (base/(name+'.stdout.log')).write_bytes(p.stdout);(base/(name+'.stderr.log')).write_bytes(p.stderr)
 if p.returncode:raise RuntimeError(f'{name} exit {p.returncode}; original logs preserved')
 return p.stdout.decode()
def main():
 a=argparse.ArgumentParser();a.add_argument('--output',type=pathlib.Path,required=True);a.add_argument('--sessions',type=int,default=10000);a.add_argument('--steps',type=int,default=40);a.add_argument('--seed',type=int,default=2026103102);a.add_argument('--wallet-sessions',type=int,default=10000);a.add_argument('--wallet-steps',type=int,default=24);v=a.parse_args()
 base=v.output.resolve();base.mkdir(parents=True,exist_ok=False)
 java=pathlib.Path(os.environ.get('JAVA_HOME','/opt/homebrew/opt/openjdk@17'));java=java/'bin/java';javac=java.with_name('javac')
 sources=sorted([*R.glob('scripts/test-support/android/annotation/*.java'),*R.glob('scripts/test-support/android/content/*.java'),*R.glob('scripts/test-support/android/graphics/*.java'),*R.glob('app/src/main/java/com/androidhit/lumadrift/data/*.java'),*R.glob('app/src/main/java/com/androidhit/lumadrift/engine/*.java'),R/'scripts/playtest-2026-10-31/EconomyStateStress.java'])
 js=sorted([*R.glob('server/src/*.js'),R/'server/test/helpers.js',R/'scripts/playtest-2026-10-31/wallet-state-stress.mjs'])
 receipt={'schemaVersion':1,'status':'RUNNING','startedAtUtc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'head':subprocess.check_output(['git','rev-parse','HEAD'],cwd=R,text=True).strip(),'compiledJavaInputs':{str(p.relative_to(R)):digest(p) for p in sources},'walletSourceInputs':{str(p.relative_to(R)):digest(p) for p in js},'runnerSha256':digest(pathlib.Path(__file__)),'arguments':vars(v)|{'output':str(base)},'networkRequested':False,'scope':'Host model/API stress using explicit persistence/clock/provider adapters; no native replay, provider, payment or human acceptance.'}
 try:
  with tempfile.TemporaryDirectory(prefix='luma123-economy-model-')as tmp:
   run([str(javac),'-Xlint:all','-d',tmp,*map(str,sources)],base,'compile')
   print(run([str(java),'-Xmx512m','-cp',tmp,'EconomyStateStress',str(base/'model'),str(v.seed),str(v.sessions),str(v.steps)],base,'model'),end='')
  print(run(['node',str(R/'scripts/playtest-2026-10-31/wallet-state-stress.mjs'),str(base/'wallet'),str(v.seed+1),str(v.wallet_sessions),str(v.wallet_steps)],base,'wallet'),end='')
  assert all(digest(R/p)==s for m in ['compiledJavaInputs','walletSourceInputs'] for p,s in receipt[m].items()),'source drift during run'
  receipt['status']='PASS'
 except BaseException as e:
  receipt['status']='FAIL';receipt['error']=str(e);raise
 finally:
  receipt['completedAtUtc']=datetime.datetime.now(datetime.timezone.utc).isoformat();receipt['outputs']={str(p.relative_to(base)):{'sha256':digest(p),'bytes':p.stat().st_size} for p in sorted(base.rglob('*')) if p.is_file()};(base/'receipt.json').write_text(json.dumps(receipt,indent=2)+'\n')
if __name__=='__main__':main()
