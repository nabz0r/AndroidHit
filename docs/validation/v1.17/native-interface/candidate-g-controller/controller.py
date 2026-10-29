#!/usr/bin/env python3
"""Exact G sequential clone qualification; wrappers validate both immutable root receipts."""
import hashlib,json,subprocess,sys,time,traceback
from pathlib import Path
ROOT=Path('/Users/n/androidhit'); OUT=ROOT/'docs/validation/v1.17/native-interface/candidate-g-controller'
OUT.mkdir(exist_ok=False)
APK='/tmp/luma-v117-candidate-g/luma-drift-1.17.0-rc5.apk'; SHA='f85c258341082462b51c6e6ddd466edc07b17b97044d72cfa27c39d20d2cd8a8'
PROBE='/tmp/luma-v117-candidate-g/interface-v117-observed-test.apk'; ART='/tmp/luma-v117-candidate-g/art-production-test.apk'
base=['--serial','emulator-5592','--expected-avd','Luma117-Secondary-F-QA','--clone-receipt',str(ROOT/'docs/validation/v1.17/secondary-clone/creation.json'),'--clone-receipt-sha256','6316b6a5b5561f515b8b46a4cd14559f91c65be241770ba6a37ef3e9d56ebaa9','--clone-candidate-authorization',str(ROOT/'docs/validation/v1.17/secondary-clone/candidate-g-authorization.json'),'--clone-candidate-authorization-sha256','a8d763b6c63eecc84db8ab855e4dad7c6d24c1a1fbd0b3e1d9388f87be6e48f8','--expected-apk',APK,'--sha256',SHA]
plan=[]
def ui(label,route,*extra):plan.append({'label':label,'command':[sys.executable,str(ROOT/'scripts/v117-ui-test/run.py'),*base,'--test-apk',PROBE,'--label',label,'--route',route,*extra],'report':str(ROOT/'docs/validation/v1.17/native-interface'/label/'host-report.json')})
ui('candidate-g-owned-rigs','rig')
plan.append({'label':'candidate-g-art','command':[sys.executable,str(ROOT/'scripts/v117-art-test/run.py'),*base,'--source-freeze',str(ROOT/'docs/validation/v1.17/candidate-g/source-freeze.json'),'--test-apk',ART,'--label','candidate-g-art'],'report':str(ROOT/'docs/validation/v1.17/art/native/candidate-g-art/host-report.json')})
ui('candidate-g-roots-compact','root-regression','--compact','--font-scale','1.0')
ui('candidate-g-roots-compact-largefont','root-regression','--compact','--font-scale','1.3')
ui('candidate-g-clipped-language','language-regression','--compact')
ui('candidate-g-sixlangs-normal','browse')
ui('candidate-g-sixlangs-compact','browse','--compact')
ui('candidate-g-secondary-compact','secondary','--compact')
ui('candidate-g-secondary-normal','secondary','--languages','en','fr')
ui('candidate-g-steady-home','performance')
ui('candidate-g-transitions','transitions')
ui('candidate-g-final-baseline','baseline')
for prerequisite in ['candidate-g-upgrade','candidate-g-other-modes-v2']:
    p=ROOT/'docs/validation/v1.17/native-interface'/prerequisite/'host-report.json';j=json.loads(p.read_text());assert j['passed'] and j['apk_sha256']==SHA
sh=lambda p:hashlib.sha256(Path(p).read_bytes()).hexdigest()
assert sh(APK)==SHA and sh(PROBE)=='17421ab7ff24e2ef42dbe2fdc09504597cbf3033599a7e84d3d9a9294c333415' and sh(ART)=='77d1b721bfe31530a3bd6a79f02b1073e6ffe74df2b98f8469aaaf79dee7b6ea'
(OUT/'controller.py').write_bytes(Path(__file__).read_bytes());(OUT/'plan.json').write_text(json.dumps(plan,indent=2)+'\n')
state={'passed':False,'apk_sha256':SHA,'scope':'Exact G physical-clone observations; no second migration qualification. Secondary compact six languages; normal secondary English/French only. Native render gallery is separate from app UI.','completed':[],'started_utc':time.strftime('%Y-%m-%dT%H:%M:%SZ',time.gmtime())}
def save(): (OUT/'status.json').write_text(json.dumps(state,indent=2)+'\n')
try:
    for phase in plan:
        state['current']=phase['label'];save();print('START '+phase['label'],flush=True);started=time.monotonic()
        with (OUT/(phase['label']+'.log')).open('wb') as log: result=subprocess.run(phase['command'],cwd=ROOT,stdout=log,stderr=subprocess.STDOUT,timeout=3600)
        assert result.returncode==0,phase['label']+' wrapper failed'
        report=json.loads(Path(phase['report']).read_text());assert report['passed'] and report['apk_sha256']==SHA,phase['label']+' exact native report failed'
        entry={'label':phase['label'],'report':phase['report'],'report_sha256':sh(phase['report']),'elapsed_seconds':round(time.monotonic()-started,3)};state['completed'].append(entry);save();print('PASS '+json.dumps(entry),flush=True)
    state['passed']=True;state['current']='complete';state['finished_utc']=time.strftime('%Y-%m-%dT%H:%M:%SZ',time.gmtime());save()
except BaseException:
    state['failure']=traceback.format_exc();save();raise
