#!/usr/bin/env python3
import pathlib,subprocess,tempfile,hashlib,json,datetime,os,argparse
R=pathlib.Path(__file__).resolve().parents[2]
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
p=argparse.ArgumentParser();p.add_argument('--output',required=True,type=pathlib.Path);a=p.parse_args();out=a.output.resolve();out.mkdir(parents=True,exist_ok=False)
sources=sorted([*R.glob('scripts/test-support/android/annotation/*.java'),*R.glob('scripts/test-support/android/content/*.java'),*R.glob('scripts/test-support/android/graphics/*.java'),*R.glob('app/src/main/java/com/androidhit/lumadrift/data/*.java'),*R.glob('app/src/main/java/com/androidhit/lumadrift/engine/*.java'),R/'scripts/playtest-2026-10-31/EconomyStateStress.java',R/'scripts/playtest-2026-10-31/RankCopyV124Test.java'])
r={'status':'RUNNING','startedAtUtc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'sourceInputs':{str(p.relative_to(R)):sha(p) for p in sources},'runnerSha256':sha(pathlib.Path(__file__)),'scope':'Host public model/API tests only; no corrected APK, native replay, build/version/publication or human validation.'}
j=pathlib.Path(os.environ.get('JAVA_HOME','/opt/homebrew/opt/openjdk@17'))/'bin'
try:
 with tempfile.TemporaryDirectory(prefix='luma124-rank-copy-')as t:
  for name,cmd in [('compile',[str(j/'javac'),'-Xlint:all','-d',t,*map(str,sources)]),('run',[str(j/'java'),'-Xmx512m','-cp',t,'RankCopyV124Test',str(out/'report.json')])]:
   q=subprocess.run(cmd,cwd=R,capture_output=True);(out/(name+'.stdout.log')).write_bytes(q.stdout);(out/(name+'.stderr.log')).write_bytes(q.stderr)
   if q.returncode:raise RuntimeError(f'{name} exit {q.returncode}; original logs retained')
   print(q.stdout.decode(),end='')
 assert all(sha(R/p)==h for p,h in r['sourceInputs'].items()),'input drift during run'
 r['status']='PASS'
except BaseException as e:r['status']='FAIL';r['error']=str(e);raise
finally:
 r['completedAtUtc']=datetime.datetime.now(datetime.timezone.utc).isoformat();r['outputs']={str(p.relative_to(out)):{'sha256':sha(p),'bytes':p.stat().st_size} for p in sorted(out.rglob('*')) if p.is_file()};(out/'receipt.json').write_text(json.dumps(r,indent=2)+'\n')
