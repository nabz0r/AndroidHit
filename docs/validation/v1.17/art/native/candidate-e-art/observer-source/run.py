#!/usr/bin/env python3
"""Run isolated native art renders on the explicitly owned, already installed final APK."""
from pathlib import Path
import argparse
import hashlib
import json
import os
import re
import shutil
import subprocess
import tempfile
import time
import traceback
import uuid
import zipfile

ROOT=Path(__file__).resolve().parents[2]
HERE=Path(__file__).resolve().parent
TEST='com.androidhit.lumadrift.arttest.v117'
TARGET='com.androidhit.lumadrift'
RUNNER=TEST+'/com.androidhit.lumadrift.qa.v117art.ArtInstrumentation'
SIGNER='dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287'

def sha(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--serial',required=True)
    parser.add_argument('--expected-avd',required=True)
    parser.add_argument('--expected-apk',type=Path,required=True)
    parser.add_argument('--sha256',required=True)
    parser.add_argument('--source-freeze',type=Path,required=True)
    parser.add_argument('--test-apk',type=Path,required=True)
    parser.add_argument('--label',required=True)
    parser.add_argument('--timeout',type=int,default=1800)
    args=parser.parse_args()
    assert args.serial=='emulator-5586' and args.expected_avd=='Luma-Refonte115-QA','Exact explicitly owned AVD/serial required'
    assert re.fullmatch(r'[A-Za-z0-9_-]{1,50}',args.label),'Fresh bounded evidence label required'
    assert re.fullmatch(r'[a-f0-9]{64}',args.sha256) and sha(args.expected_apk)==args.sha256,'Exact accepted candidate hash required'
    witness_path=HERE/'assets/source-manifest.json';witness=json.loads(witness_path.read_text());freeze=json.loads(args.source_freeze.read_text())
    assert witness and all(sha(ROOT/name)==digest and freeze.get(name)==digest for name,digest in witness.items()),'Actual final APK sources must match packaged render witnesses'
    with zipfile.ZipFile(args.test_apk) as observer:
        assert observer.read('assets/source-manifest.json')==witness_path.read_bytes(),'Observer source witness differs from current prepared sources'
    with zipfile.ZipFile(args.expected_apk) as game:
        for name,digest in witness.items():
            if name.startswith('app/src/main/assets/'):
                assert hashlib.sha256(game.read('assets/'+name[len('app/src/main/assets/'):])).hexdigest()==digest,'Actual packaged original art differs: '+name
    sdk=Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'))
    adb=str(sdk/'platform-tools/adb');signer=str(sdk/'build-tools/36.0.0/apksigner')
    signatures={}
    for kind,file in (('game',args.expected_apk),('observer',args.test_apk)):
        proof=subprocess.check_output([signer,'verify','--verbose','--print-certs',str(file)],stderr=subprocess.STDOUT).decode()
        assert re.findall(r'Signer #1 certificate SHA-256 digest: ([a-f0-9]+)',proof)==[SIGNER],kind+' signer differs'
        signatures[kind]=proof
    def call(*parts,timeout=90):return subprocess.check_output([adb,'-s',args.serial,*parts],stderr=subprocess.STDOUT,timeout=timeout)
    def shell(*parts):return call('shell',*parts).decode(errors='replace')
    def installed(package):
        process=subprocess.run([adb,'-s',args.serial,'shell','pm','path',package],capture_output=True,text=True,timeout=60)
        assert process.returncode in (0,1),'Package query failed'
        paths=[line[8:] for line in process.stdout.splitlines() if line.startswith('package:')]
        assert len(paths)<=1 and all(path.startswith('/data/app/') for path in paths),'Unexpected split/installation identity'
        return paths[0] if paths else None
    def installed_hash(package):
        path=installed(package)
        if path is None:return None
        with tempfile.TemporaryDirectory(prefix='luma117-art-installed-') as temp:
            local=Path(temp)/'installed.apk';call('pull',path,str(local));return sha(local)
    assert shell('getprop','ro.kernel.qemu').strip()=='1','Emulator-only gallery qualification'
    assert call('emu','avd','name').decode().splitlines()[0].strip()==args.expected_avd,'Exact task-owned AVD required'
    assert installed_hash(TARGET)==args.sha256,'Final game must already be installed by its exclusive owner; never install or upgrade it here'
    output=ROOT/'docs/validation/v1.17/art/native'/args.label
    assert not output.exists(),'Fresh local evidence directory required'
    output.mkdir(parents=True)
    observer=output/'art-observer.apk';shutil.copyfile(args.test_apk,observer)
    assert sha(observer)==sha(args.test_apk)
    shutil.copyfile(args.source_freeze,output/'final-source-freeze.json')
    for kind,proof in signatures.items():(output/(kind+'-signature.txt')).write_text(proof)
    for file in HERE.rglob('*'):
        if file.is_file() and '__pycache__' not in file.parts:
            target=output/'observer-source'/file.relative_to(HERE);target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(file,target)
    run_token=str(uuid.uuid4());owned_observer=False;started=time.monotonic()
    report={'passed':False,'run_token':run_token,'apk_sha256':args.sha256,'version':'1.17.0-rc3','code':21,'observer_sha256':sha(observer),'source_witness_sha256':sha(witness_path),'final_source_freeze_sha256':sha(args.source_freeze),'serial':args.serial,'avd':args.expected_avd,'scope':'Isolated native Android Canvas QA render gallery through public pure renderer APIs. Not application UI, ownership, interaction, achieved outfits or gameplay evidence. No target installation, Activity launch, input injection, GameStore, engine, reflection or preferences.'}
    try:
        previous=installed_hash(TEST)
        assert previous is None or previous==sha(observer),'Different observer present; preserve its ownership'
        if previous is None:
            (output/'observer-install.txt').write_bytes(call('install','-t',str(observer),timeout=180));owned_observer=True
        command=[adb,'-s',args.serial,'shell','am','instrument','-w','-r','-e','label',args.label,'-e','runToken',run_token,RUNNER]
        result=subprocess.run(command,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=args.timeout)
        (output/'instrumentation.txt').write_bytes(result.stdout)
        print(result.stdout.decode(errors='replace'))
        native=output/'native';remote='/sdcard/Android/data/'+TARGET+'/files/v117-art-native/'+args.label
        # Retrieve native failure traces too; a stale label still fails run-token validation.
        pull=subprocess.run([adb,'-s',args.serial,'pull',remote,str(native)],stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=180)
        (output/'pull.txt').write_bytes(pull.stdout)
        assert result.returncode==0 and b'INSTRUMENTATION_CODE: -1' in result.stdout and b'INSTRUMENTATION_FAILED' not in result.stdout,'Native observer did not complete successfully'
        assert pull.returncode==0,'Native evidence retrieval failed'
        proof=json.loads((native/'report.json').read_text())
        assert proof['passed'] is True and proof['runToken']==run_token and proof['code']==21,'Current successful native execution identity required'
        assert len(proof['composites'])==144 and len(proof['garments'])==24 and len(proof['styles'])==24,'Incomplete native art matrix'
        assert len(proof['images'])==50 and all(sha(native/name)==digest for name,digest in proof['images'].items()),'Every native gallery image must match its recorded hash'
        assert json.loads((native/'source-manifest.json').read_text())==witness,'Native observer used a different source witness'
        assert all(check['passed'] for check in proof['checks']),'A native art assertion failed'
        assert installed_hash(TARGET)==args.sha256,'Installed game identity changed during isolated rendering'
        report.update(passed=True,composites=144,garment_previews=24,style_samples=24,images=50,native_report='native/report.json')
    except Exception:
        (output/'host-failure.txt').write_text(traceback.format_exc());raise
    finally:
        if owned_observer:
            try:
                assert installed_hash(TEST)==sha(observer),'Do not uninstall an observer with changed identity'
                (output/'observer-uninstall.txt').write_bytes(call('uninstall',TEST))
            except Exception:
                report['passed']=False;(output/'observer-cleanup-failure.txt').write_text(traceback.format_exc())
        report['elapsed_seconds']=round(time.monotonic()-started,3)
        report['evidence_sha256']={str(file.relative_to(output)):sha(file) for file in sorted(output.rglob('*')) if file.is_file() and file.name!='host-report.json'}
        (output/'host-report.json').write_text(json.dumps(report,indent=2)+'\n')
    assert report['passed'],'Qualification or observer cleanup failed'

if __name__=='__main__':main()
