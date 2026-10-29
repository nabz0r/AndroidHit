#!/usr/bin/env python3
"""Fresh C public-UI route on the newly created, receipt-owned 5590 AVD only."""
from pathlib import Path
import argparse, base64, hashlib, json, os, re, shutil, subprocess, tempfile, time, traceback, uuid

ROOT=Path(__file__).resolve().parents[2]
HERE=Path(__file__).resolve().parent
SHA='eb67c6214d2868b21d02a0367d743bd2fed45b408b4d544523b8876ef3b78fff'
SIGNER='dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287'
TARGET='com.androidhit.lumadrift';TEST=TARGET+'.freshtest.v117'
RUNNER=TEST+'/com.androidhit.lumadrift.qa.v117fresh.FreshInstrumentation'
def sha(file):return hashlib.sha256(Path(file).read_bytes()).hexdigest()

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--phase',choices=('onboarding','campaign','arrivals','cold'),required=True)
    parser.add_argument('--label',required=True)
    parser.add_argument('--apk',type=Path,default=Path('/tmp/luma-v117-candidate-c/luma-drift-1.17.0-rc1.apk'))
    parser.add_argument('--observer',type=Path,default=ROOT/'app/build/qualification-artifacts/v1.17/fresh-production-test.apk')
    parser.add_argument('--checkpoint',type=Path)
    parser.add_argument('--fresh-install-receipt',type=Path,help='Continue a verified new install only when its previous observer failed before any instrumentation/Activity launch')
    args=parser.parse_args();assert re.fullmatch(r'[A-Za-z0-9_-]{1,60}',args.label)
    ownership=ROOT/'docs/validation/v1.17/fresh/device/ownership.json';owned=json.loads(ownership.read_text())
    assert owned['created_for_task'] is True and owned['avd']=='Luma117-Fresh-QA' and owned['serial']=='emulator-5590' and owned['port']==5590
    assert sha(args.apk)==SHA,'Only independently accepted C APK bytes are allowed'
    sdk=Path.home()/'Library/Android/sdk';adb=str(sdk/'platform-tools/adb');signer=str(sdk/'build-tools/36.0.0/apksigner')
    signing_env=os.environ.copy();signing_env.setdefault('JAVA_HOME','/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home')
    def call(*parts,timeout=90):return subprocess.check_output([adb,'-s','emulator-5590',*parts],stderr=subprocess.STDOUT,timeout=timeout)
    def shell(*parts):return call('shell',*parts).decode(errors='replace')
    assert call('emu','avd','name').decode().splitlines()[0].strip()==owned['avd'],'Never address another existing AVD'
    assert shell('getprop','ro.kernel.qemu').strip()=='1' and shell('getprop','sys.boot_completed').strip()=='1'
    def package_path(package):
        process=subprocess.run([adb,'-s','emulator-5590','shell','pm','path',package],capture_output=True,text=True,timeout=60)
        assert process.returncode in (0,1)
        paths=[line[8:] for line in process.stdout.splitlines() if line.startswith('package:')];assert len(paths)<=1
        return paths[0] if paths else None
    def package_hash(package):
        path=package_path(package)
        if path is None:return None
        assert path.startswith('/data/app/')
        with tempfile.TemporaryDirectory(prefix='luma117-fresh-bytes-') as temp:
            file=Path(temp)/'installed.apk';call('pull',path,str(file));return sha(file)
    output=ROOT/'docs/validation/v1.17/fresh/native'/args.label;assert not output.exists(),'Keep all previous attempts'
    output.mkdir(parents=True);report={'passed':False,'phase':args.phase,'apk_sha256':SHA,'observer_sha256':sha(args.observer),'serial':'emulator-5590','avd':owned['avd'],'run_token':str(uuid.uuid4()),'scope':'New receipt-owned empty AVD only; real public SDK touch route, no target preferences/private app calls, state injection, seeding or existing AVD access'}
    shutil.copyfile(args.observer,output/'fresh-observer.apk')
    for file in HERE.rglob('*'):
        if file.is_file() and '__pycache__' not in file.parts:
            target=output/'observer-source'/file.relative_to(HERE);target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(file,target)
    started=time.monotonic()
    try:
        for name,file in (('candidate',args.apk),('observer',args.observer)):
            proof=subprocess.check_output([signer,'verify','--verbose','--print-certs',str(file)],stderr=subprocess.STDOUT,env=signing_env).decode();assert re.findall(r'Signer #1 certificate SHA-256 digest: ([a-f0-9]+)',proof)==[SIGNER];(output/(name+'-signature.txt')).write_text(proof)
        installed=package_hash(TARGET)
        if args.phase=='onboarding':
            if installed is None:
                (output/'fresh-install.txt').write_bytes(call('install',str(args.apk),timeout=180));report['genuine_new_install']=True
            else:
                assert installed==SHA and args.fresh_install_receipt,'Never clear/reinstall or adopt an existing game'
                receipt=json.loads(args.fresh_install_receipt.read_text());assert receipt.get('genuine_new_install') is True and receipt['apk_sha256']==SHA and receipt['avd']==owned['avd'] and not (args.fresh_install_receipt.parent/'instrumentation.txt').exists(),'Only resume the actual untouched first install whose observer failed before launch'
                report['genuine_new_install']=True;report['new_install_receipt']=str(args.fresh_install_receipt.relative_to(ROOT))
        else:assert installed==SHA,'Continue only the same freshly installed C save; never reset it'
        assert package_hash(TARGET)==SHA
        old_test=package_hash(TEST)
        if old_test!=sha(args.observer):
            # Only this newly created AVD/test package is owned here. Target is never updated.
            if old_test is not None:
                assert list((ROOT/'docs/validation/v1.17/fresh/native').glob('*/host-report.json')),'Unknown existing observer ownership'
            (output/'observer-install.txt').write_bytes(call('install','-r','-t',str(args.observer),timeout=180))
        if args.phase=='cold':(output/'cold-force-stop.txt').write_bytes(call('shell','am','force-stop',TARGET))
        (output/'wake.txt').write_bytes(call('shell','input','keyevent','KEYCODE_WAKEUP'))
        (output/'unlock.txt').write_bytes(call('shell','wm','dismiss-keyguard'))
        command=[adb,'-s','emulator-5590','shell','am','instrument','-w','-r','-e','phase',args.phase,'-e','label',args.label,'-e','runToken',report['run_token']]
        if args.checkpoint:
            checkpoint=json.loads(args.checkpoint.read_text());encoded=base64.b64encode(json.dumps(checkpoint,separators=(',',':')).encode()).decode();command+=['-e','checkpoint64',encoded];shutil.copyfile(args.checkpoint,output/'expected-ui-checkpoint.json')
        command.append(RUNNER)
        proc=subprocess.run(command,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=900);(output/'instrumentation.txt').write_bytes(proc.stdout);print(proc.stdout.decode(errors='replace'))
        remote='/sdcard/Android/data/'+TARGET+'/files/v117-fresh-native/'+args.label
        pull=subprocess.run([adb,'-s','emulator-5590','pull',remote,str(output/'native')],stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=180);(output/'pull.txt').write_bytes(pull.stdout)
        assert pull.returncode==0,'Original evidence unavailable'
        native=json.loads((output/'native/report.json').read_text());report['native']=native
        assert native['runToken']==report['run_token'] and native['phase']==args.phase,'Only this execution counts'
        assert proc.returncode==0 and re.search(rb'INSTRUMENTATION_CODE:\s*-1\s*$',proc.stdout) and native['passed'] is True,'Native route failed; preserve attempted observations'
        assert package_hash(TARGET)==SHA,'Installed C bytes changed'
        report.update(passed=True,checkCount=native['checkCount'],touchCount=native['touchCount'],captures=len(native['captures']),installed_apk_sha256_after=SHA)
    except Exception as error:
        report['failure']=traceback.format_exc();(output/'host-failure.txt').write_text(report['failure'])
        if getattr(error,'output',None):(output/'host-command-failure.txt').write_bytes(error.output)
    finally:
        report['elapsed_seconds']=round(time.monotonic()-started,3);report['evidence_sha256']={str(file.relative_to(output)):sha(file) for file in sorted(output.rglob('*')) if file.is_file() and file.name!='host-report.json'}
        (output/'host-report.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps({'passed':report['passed'],'report':str(output/'host-report.json')}))
    raise SystemExit(0 if report['passed'] else 1)
if __name__=='__main__':main()
