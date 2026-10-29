#!/usr/bin/env python3
"""Install supplied frozen production bytes once on the new empty task-owned AVD."""
from pathlib import Path
import argparse,datetime,hashlib,json,os,re,subprocess
ROOT=Path(__file__).resolve().parents[2]
SIGNER='dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287'
PROFILES={'emulator-5596':('Luma118-Fresh-Art-QA','native-fresh','a1e6532a7cd1e5b86704e17cc80dde21c1bd72128fb232d52e9d4c2e37940cc9'),
          'emulator-5598':('Luma118-Final-G-QA','native-fresh-g','4cc69a091d08cfa83a9d70876d8d78ffd1a7e79d11df24f8da616ebd9e01c588'),
          'emulator-5600':('Luma118-Final-H-QA','native-fresh-h','a17296f11091474f6ebdf8520a47edaf51ca471e0ee563908dd77e8b12bf31d7')}
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
    os.environ.setdefault('JAVA_HOME','/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home')
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('--apk',type=Path,required=True);p.add_argument('--sha256',required=True);p.add_argument('--version',required=True);p.add_argument('--code',type=int,required=True);p.add_argument('--label',required=True);p.add_argument('--serial',choices=sorted(PROFILES),default='emulator-5596');a=p.parse_args()
    avd,evidence_root,receipt_sha=PROFILES[a.serial]
    receipt=ROOT/'docs/validation/v1.18'/evidence_root/'device-creation/creation.json'
    assert sha(receipt)==receipt_sha,'Exact authorized fresh-device provenance required'
    assert re.fullmatch('[A-Za-z0-9_-]{1,60}',a.label) and re.fullmatch('[a-f0-9]{64}',a.sha256) and sha(a.apk)==a.sha256,'Explicit frozen bytes required'
    out=ROOT/'docs/validation/v1.18'/evidence_root/a.label;assert not out.exists(),'Preserve previous install attempt evidence';out.mkdir()
    sdk=Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'));adb=sdk/'platform-tools/adb';tools=sdk/'build-tools/36.0.0'
    def call(*cmd):return subprocess.check_output([str(adb),'-s',a.serial,*map(str,cmd)],stderr=subprocess.STDOUT,timeout=240)
    report={'installed':False,'apk_sha256':a.sha256,'apk_path':str(a.apk.resolve()),'serial':a.serial,'avd':avd,'created_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'creation_receipt_sha256':receipt_sha,'scope':'First production install on newly created empty task-owned AVD; no existing profile, clear, uninstall, replace or launch.'}
    try:
        assert call('emu','avd','name').decode().splitlines()[0].strip()==avd,'Live AVD differs'
        assert call('shell','getprop','sys.boot_completed').strip()==b'1','Fresh device not booted'
        for package in ['com.androidhit.lumadrift','com.androidhit.lumadrift.sandbox']:
            absent=subprocess.run([str(adb),'-s',a.serial,'shell','pm','path',package],capture_output=True,text=True,timeout=30)
            (out/(package+'-before.txt')).write_text(absent.stdout+absent.stderr)
            assert absent.returncode in [0,1] and not absent.stdout.strip(),'Never install over an existing target package'
        signature=subprocess.check_output([str(tools/'apksigner'),'verify','--verbose','--print-certs',str(a.apk)],stderr=subprocess.STDOUT).decode();(out/'signature.txt').write_text(signature)
        assert re.findall(r'Signer #1 certificate SHA-256 digest: ([a-f0-9]+)',signature)==[SIGNER],'Signing identity differs'
        badging=subprocess.check_output([str(tools/'aapt2'),'dump','badging',str(a.apk)],stderr=subprocess.STDOUT).decode();(out/'manifest.txt').write_text(badging)
        identity=re.search(r"package: name='([^']+)' versionCode='([^']+)' versionName='([^']+)'",badging)
        assert identity and identity.groups()==('com.androidhit.lumadrift',str(a.code),a.version),'Frozen production manifest identity differs'
        # No -r: installation cannot replace an existing package. No launch follows.
        command=['install',str(a.apk.resolve())];report['command']=[str(adb),'-s',a.serial,*command]
        (out/'install.txt').write_bytes(call(*command));report['installed']=True
        (out/'package-after.txt').write_bytes(call('shell','pm','path','com.androidhit.lumadrift'))
    except BaseException as failure:report['error']=str(failure);raise
    finally:(out/'report.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report))
if __name__=='__main__':main()
