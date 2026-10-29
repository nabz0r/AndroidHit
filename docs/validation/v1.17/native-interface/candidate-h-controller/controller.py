import json,subprocess,sys,time,os,hashlib
from pathlib import Path
R=Path('/Users/n/androidhit');OUT=R/'docs/validation/v1.17/native-interface';STATE=OUT/'candidate-h-controller';STATE.mkdir(exist_ok=False)
base=['/Library/Developer/CommandLineTools/usr/bin/python3', '/Users/n/androidhit/scripts/v117-ui-test/run.py', '--serial', 'emulator-5592', '--expected-avd', 'Luma117-Secondary-F-QA', '--clone-receipt', '/Users/n/androidhit/docs/validation/v1.17/secondary-clone/creation.json', '--clone-receipt-sha256', '6316b6a5b5561f515b8b46a4cd14559f91c65be241770ba6a37ef3e9d56ebaa9', '--clone-candidate-authorization', '/Users/n/androidhit/docs/validation/v1.17/secondary-clone/candidate-g-authorization.json', '--clone-candidate-authorization-sha256', 'a8d763b6c63eecc84db8ab855e4dad7c6d24c1a1fbd0b3e1d9388f87be6e48f8', '--clone-successor-authorization', '/Users/n/androidhit/docs/validation/v1.17/secondary-clone/candidate-h-authorization.json', '--clone-successor-authorization-sha256', '055cb978afa2c073700346afa336516c68694cc276492023d362d56c849035b5', '--expected-apk', '/tmp/luma-v117-candidate-h/luma-drift-1.17.0-rc6.apk', '--sha256', '15546854a856ecc5052aa79acdd49724439b2082fd638da68cf7747be1924a47', '--test-apk', '/tmp/luma-v117-candidate-h/interface-v117-footer-test.apk']
art=['/Library/Developer/CommandLineTools/usr/bin/python3', '/Users/n/androidhit/scripts/v117-art-test/run.py', '--serial', 'emulator-5592', '--expected-avd', 'Luma117-Secondary-F-QA', '--clone-receipt', '/Users/n/androidhit/docs/validation/v1.17/secondary-clone/creation.json', '--clone-receipt-sha256', '6316b6a5b5561f515b8b46a4cd14559f91c65be241770ba6a37ef3e9d56ebaa9', '--clone-candidate-authorization', '/Users/n/androidhit/docs/validation/v1.17/secondary-clone/candidate-g-authorization.json', '--clone-candidate-authorization-sha256', 'a8d763b6c63eecc84db8ab855e4dad7c6d24c1a1fbd0b3e1d9388f87be6e48f8', '--clone-successor-authorization', '/Users/n/androidhit/docs/validation/v1.17/secondary-clone/candidate-h-authorization.json', '--clone-successor-authorization-sha256', '055cb978afa2c073700346afa336516c68694cc276492023d362d56c849035b5', '--expected-apk', '/tmp/luma-v117-candidate-h/luma-drift-1.17.0-rc6.apk', '--sha256', '15546854a856ecc5052aa79acdd49724439b2082fd638da68cf7747be1924a47', '--test-apk', '/tmp/luma-v117-candidate-h/art-production-test.apk', '--source-freeze', '/Users/n/androidhit/docs/validation/v1.17/candidate-h/source-freeze.json', '--label', 'candidate-h-art-gallery']
jobs=[
 ('candidate-h-sixlangs-normal','browse',[]),
 ('candidate-h-sixlangs-compact','browse',['--compact']),
 ('candidate-h-root-compact','root-regression',['--compact','--font-scale','1.0']),
 ('candidate-h-root-compact-font13','root-regression',['--compact','--font-scale','1.3']),
 ('candidate-h-language-compact','language-regression',['--compact']),
 ('candidate-h-owned-rigs','rig',[]),
 ('candidate-h-art-gallery','art',[]),
 ('candidate-h-transitions','transitions',[]),
 ('candidate-h-performance','performance',[]),
 ('candidate-h-secondary-compact','secondary',['--compact']),
 ('candidate-h-secondary-normal','secondary',['--languages','en','fr']),
 ('candidate-h-final-baseline','baseline',[]),
]
def sha(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()
freeze_path=R/'docs/validation/v1.17/candidate-h/source-freeze.json'
freeze=json.loads(freeze_path.read_text());candidate=json.loads((R/'docs/validation/v1.17/candidate-h/candidate.json').read_text())
def immutable():
 assert sha(freeze_path)=='b6af495fadab8c20cf6379b591297da38e10edb4b694aad0adcf521f75711b88'
 assert all(sha(R/name)==digest for name,digest in freeze.items()),'Frozen app source changed'
 assert all(sha(a['path'])==a['sha256'] and Path(a['path']).stat().st_size==a['bytes'] for a in candidate['artifacts']),'Four H frozen artifacts changed'
immutable()
observer=Path('/tmp/luma-v117-candidate-h/interface-v117-footer-test.apk')
assert sha(observer)=='e33fc6b0f40fd96bb67d0a7ab6a1ca0b3d579126a38d65d0bbf42c4b1f878403'
observer_sources=json.loads(Path(str(observer)+'.source.json').read_text())
assert all(sha(R/name)==digest for name,digest in observer_sources.items()),'Observer source freeze changed before matrix'
record={'passed':False,'pid':os.getpid(),'plan':jobs,'source_freeze_sha256':sha(freeze_path),'observer_sha256':sha(observer),'serial':'emulator-5592','scope':'Exact H retained physical-clone native UI matrix; not another migration. Secondary compact six languages, secondary normal EN/FR; primary six languages both dimensions. Artwork gallery is separate pure-render evidence.','completed':[],'running':None}
def save(): (STATE/'status.json').write_text(json.dumps(record,indent=2)+'\n')
try:
 for label,route,extra in jobs:
  assert sha(observer)=='e33fc6b0f40fd96bb67d0a7ab6a1ca0b3d579126a38d65d0bbf42c4b1f878403'
  assert all(sha(R/name)==digest for name,digest in observer_sources.items()),'Observer sources changed during matrix'
  record['running']=label;save();start=time.monotonic();print('START '+label,flush=True)
  with (STATE/(label+'.log')).open('w') as log:subprocess.run(art if route=='art' else base+['--route',route,'--label',label]+extra,cwd=R,stdout=log,stderr=subprocess.STDOUT,check=True)
  path=(R/'docs/validation/v1.17/art/native'/label if route=='art' else OUT/label)/'host-report.json';proof=json.loads(path.read_text());assert proof['passed']
  if route!='art':assert all(x['native']['runToken']==x['host_run_token'] and x['phase']==x['native']['phase'] for x in proof['phases'])
  record['completed'].append({'label':label,'route':route,'report':str(path.relative_to(R)),'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'elapsed_seconds':round(time.monotonic()-start,3),'images':proof['images'] if route=='art' else len(proof['screenshots']),'checks':None if route=='art' else sum(x['native']['checkCount'] for x in proof['phases'])});record['running']=None;save();print('PASS '+label,flush=True)
 immutable();record['final_immutable_app_sources']=len(freeze);record['final_four_binary_hashes_verified']=True;record['passed']=True;save()
except BaseException as exc:
 record['error']=repr(exc);save();raise
