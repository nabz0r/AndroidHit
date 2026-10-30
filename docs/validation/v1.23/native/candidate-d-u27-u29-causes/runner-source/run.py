#!/usr/bin/env python3
"""Run an exact-byte candidate on the retained task AVD for the authorized 1.23 candidate, using an SDK-only observer."""
from pathlib import Path
import argparse
import base64
import datetime
import hashlib
import json
import os
import re
import shutil
import subprocess
import tempfile
import time
import uuid

ROOT=Path(__file__).resolve().parents[2]
HERE=Path(__file__).resolve().parent
OUT=ROOT/'docs/validation/v1.23/native'
SERIAL='emulator-5602'
AVD='Luma119-Battle-QA'
RECEIPT_SHA='022362d42b38acf665a2266d1fb2dbec40981f5640d6955d7d86c94c1a1196ae'
PACKAGE='com.androidhit.lumadrift'
TEST=PACKAGE+'.v123test'
RUNNER=TEST+'/com.androidhit.lumadrift.qa.v123.BattleInstrumentation'
SIGNER='dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287'

def sha(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()

def main():
    global SERIAL, AVD, RECEIPT_SHA
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--device',choices=['retained','fresh','final-fresh'],default='retained',help='Only the three pinned task-owned AVDs; never an arbitrary serial')
    parser.add_argument('--candidate',type=Path,required=True)
    parser.add_argument('--sha256',required=True)
    parser.add_argument('--observer',type=Path,required=True)
    parser.add_argument('--observer-sha256',required=True)
    parser.add_argument('--version',required=True)
    parser.add_argument('--code',type=int,required=True)
    parser.add_argument('--phase',required=True)
    parser.add_argument('--label',required=True)
    parser.add_argument('--steps',type=Path,required=True)
    parser.add_argument('--first-install',action='store_true',help='Explicitly allow first game install only while target is absent on this new AVD')
    parser.add_argument('--fresh',action='store_true',help='Require empty game and session preferences before the first Activity launch')
    parser.add_argument('--snapshot-only',action='store_true',help='Read preferences without launching any Activity or sending UI input')
    parser.add_argument('--upgrade-from-sha256',help='Explicitly authorized update on this task AVD only, preserving data and requiring exact previous installed APK bytes')
    parser.add_argument('--record-video-seconds',type=int,default=0,help='Record 1–60 seconds of actual native UI on this task device only')
    args=parser.parse_args()
    assert args.version == '1.23.0-rc1' and args.code == 31, 'Explicit 1.23 code31 identity only'
    devices={'retained':('emulator-5602','Luma119-Battle-QA','docs/validation/v1.19/native/device-creation/creation.json','022362d42b38acf665a2266d1fb2dbec40981f5640d6955d7d86c94c1a1196ae'),'fresh':('emulator-5630','Luma123-Fresh-QA','docs/validation/v1.23/native/device-creation-fresh/creation.json','ced8bcf8a1f4db49f593baa80b901af1b63e715cad34fc9e5c290fa6ada9614b'),'final-fresh':('emulator-5632','Luma123-FinalFresh-QA','docs/validation/v1.23/native/device-creation-final-fresh-attempt-2/creation.json','9095a9f3f23c029b0d27e3784c20b688c50f2c5f9978219b5f6cae534930cd32')}
    SERIAL,AVD,receipt_path,RECEIPT_SHA=devices[args.device]
    receipt=ROOT/receipt_path
    assert sha(receipt)==RECEIPT_SHA,'Exact immutable creation receipt required'
    provenance=json.loads(receipt.read_text())
    assert provenance['fresh'] and provenance['avd']==AVD and provenance['serial']==SERIAL,'Task-owned device identity differs'
    assert re.fullmatch('[a-f0-9]{64}',args.sha256) and sha(args.candidate)==args.sha256,'Explicit frozen exact game hash required'
    assert re.fullmatch('[a-f0-9]{64}',args.observer_sha256) and sha(args.observer)==args.observer_sha256,'Explicit exact observer hash required'
    assert re.fullmatch('[a-z][a-z0-9_-]{1,50}',args.phase) and re.fullmatch('[A-Za-z0-9_-]{1,65}',args.label),'Safe distinct phase and evidence label required'
    steps=json.loads(args.steps.read_text())
    assert isinstance(steps,list) and 1<=len(steps)<=250,'Explicit bounded native UI program required'
    assert 0<=args.record_video_seconds<=60,'Native video must be bounded to at most 60 seconds'
    assert not (args.first_install and args.upgrade_from_sha256),'First install and update are separate explicit operations'
    if args.upgrade_from_sha256:assert re.fullmatch('[a-f0-9]{64}',args.upgrade_from_sha256),'Exact prior candidate hash required for an authorized update'
    out=OUT/args.label
    assert not out.exists(),'Never overwrite accepted or failed evidence'
    sdk=Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'))
    adb=sdk/'platform-tools/adb'
    env=os.environ.copy()
    env.setdefault('JAVA_HOME','/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home')
    def call(*parts,timeout=90):return subprocess.check_output([str(adb),'-s',SERIAL,*[str(part) for part in parts]],stderr=subprocess.STDOUT,timeout=timeout,env=env)
    def shell(*parts):return call('shell',*parts).decode(errors='replace')
    def installed(package):
        result=subprocess.run([str(adb),'-s',SERIAL,'shell','pm','path',package],capture_output=True,text=True,timeout=30)
        assert result.returncode in (0,1),'Package query failed'
        paths=[line[8:] for line in result.stdout.splitlines() if line.startswith('package:')]
        assert len(paths)<=1 and all(path.startswith('/data/app/') for path in paths),'One exact installed APK required'
        return paths[0] if paths else None
    assert call('emu','avd','name').decode().splitlines()[0].strip()==AVD,'Live task AVD identity differs'
    assert shell('getprop','ro.kernel.qemu').strip()=='1','Task emulator only'
    assert not installed(TEST),'Observer must initially be absent'
    if args.first_install:assert not installed(PACKAGE),'First-install mode never replaces an existing game or profile'
    else:assert installed(PACKAGE),'Owner must authorize first install; runner never upgrades the game'
    out.mkdir(parents=True)
    shutil.copyfile(receipt,out/'device-creation.json')
    (out/'requested-steps.json').write_text(json.dumps(steps,ensure_ascii=False,indent=2)+'\n')
    snapshots={}
    for path in sorted(HERE.rglob('*')):
        if path.is_file() and '__pycache__' not in path.parts:
            relative=path.relative_to(HERE);copy=out/'runner-source'/relative;copy.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(path,copy);snapshots[str(relative)]=sha(copy)
    (out/'source-snapshot.json').write_text(json.dumps(snapshots,indent=2)+'\n')
    shutil.copyfile(args.observer,out/'battle-observer.apk')
    report={'passed':False,'created_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'serial':SERIAL,'avd':AVD,'package':PACKAGE,'phase':args.phase,'expected_version':args.version,'expected_code':args.code,'apk_sha256':args.sha256,'observer_sha256':args.observer_sha256,'creation_receipt_sha256':RECEIPT_SHA,'first_install':args.first_install,'fresh':args.fresh,'snapshot_only':args.snapshot_only,'upgrade_from_sha256':args.upgrade_from_sha256,'run_token':str(uuid.uuid4()),'scope':'Task-owned QA AVD only. Real native UI gestures and read-only preferences. Observer installed/uninstalled; first game install requires absence. An owner-authorized update requires its explicit previous installed APK hash and preserves data. Never clear/uninstall the game, inject fixtures or grant ownership/currency/progress. No external identity, ad or real-money purchase.'}
    started=time.monotonic();observer_installed=False
    try:
        for label,path in [('candidate',args.candidate),('observer',args.observer)]:
            proof=subprocess.check_output([str(sdk/'build-tools/36.0.0/apksigner'),'verify','--verbose','--print-certs',str(path)],stderr=subprocess.STDOUT,env=env).decode()
            (out/(label+'-signature.txt')).write_text(proof)
            assert re.findall(r'Signer #1 certificate SHA-256 digest: ([a-f0-9]+)',proof)==[SIGNER],label+' signer identity differs'
        if args.first_install:(out/'first-install.txt').write_bytes(call('install',args.candidate,timeout=180))
        if args.upgrade_from_sha256:
            with tempfile.TemporaryDirectory(prefix='luma123-before-update-') as temp:
                previous=Path(temp)/'installed.apk';call('pull',installed(PACKAGE),previous)
                report['before_update_installed_sha256']=sha(previous)
                assert sha(previous)==args.upgrade_from_sha256,'Previous installed candidate differs; refuse update'
            (out/'authorized-update.txt').write_bytes(call('install','-r',args.candidate,timeout=180))
        with tempfile.TemporaryDirectory(prefix='luma123-installed-proof-') as temp:
            actual=Path(temp)/'installed.apk';call('pull',installed(PACKAGE),actual)
            report['installed_sha256']=sha(actual);assert sha(actual)==args.sha256,'Installed bytes differ from the frozen candidate'
        report['device']={key:shell(*command).strip() for key,command in {'sdk':['getprop','ro.build.version.sdk'],'abi':['getprop','ro.product.cpu.abi'],'page_size':['getconf','PAGESIZE'],'size':['wm','size'],'density':['wm','density']}.items()}
        (out/'observer-install.txt').write_bytes(call('install','-t',args.observer,timeout=180));observer_installed=True
        shell('am','force-stop',PACKAGE)
        encoded=base64.b64encode(json.dumps(steps,separators=(',',':'),ensure_ascii=False).encode()).decode()
        command=['shell','am','instrument','-w','-r','-e','phase',args.phase,'-e','runToken',report['run_token'],'-e','expectedVersion',args.version,'-e','expectedCode',str(args.code),'-e','stepsBase64',encoded,'-e','fresh',str(args.fresh).lower(),'-e','snapshotOnly',str(args.snapshot_only).lower(),RUNNER]
        video=None
        if args.record_video_seconds:
            remote_video='/data/local/tmp/luma123-'+report['run_token']+'.mp4'
            video_log=(out/'screenrecord.txt').open('wb')
            video=subprocess.Popen([str(adb),'-s',SERIAL,'shell','screenrecord','--time-limit',str(args.record_video_seconds),'--bit-rate','3000000','--size','720x1600',remote_video],stdout=video_log,stderr=subprocess.STDOUT)
        try:
            result=call(*command,timeout=1800);(out/'instrumentation.txt').write_bytes(result)
        finally:
            if video is not None:
                try:
                    video.wait(timeout=args.record_video_seconds+15)
                    video_log.close()
                    assert video.returncode==0,'Native screen recording failed'
                    call('pull',remote_video,out/'native-ui-recording.mp4',timeout=90)
                    report['native_video']={'requested_seconds':args.record_video_seconds,'path':'native-ui-recording.mp4','sha256':sha(out/'native-ui-recording.mp4'),'scope':'Unedited actual task-emulator screen recording during this native gesture program; not a frame-rate or phone performance benchmark.'}
                except Exception as failure:report['video_error']=str(failure)
        remote='/sdcard/Android/data/'+PACKAGE+'/files/v123-native/'+report['run_token']+'/'+args.phase
        call('pull',remote,out/'native',timeout=180)
        native_path=out/'native/report.json'
        assert native_path.exists(),'Instrumentation process exited before writing its native report: '+result.decode(errors='replace')[-1200:]
        native=json.loads(native_path.read_text());report['native']=native
        assert b'INSTRUMENTATION_CODE: -1' in result and b'INSTRUMENTATION_FAILED' not in result,'Current instrumentation execution failed'
        assert native['phase']==args.phase and native['runToken']==report['run_token'],'Native output does not belong to this exact execution'
        assert native['passed'],'Actual native assertions failed'
        report['passed']=True
    except BaseException as failure:
        report['error']=str(failure)
        try:(out/'failure-android-runtime.txt').write_bytes(call('logcat','-d','-v','threadtime','-s','AndroidRuntime:V',timeout=30))
        except Exception as diagnostic:report['diagnostic_error']=str(diagnostic)
        raise
    finally:
        if observer_installed:
            try:(out/'observer-uninstall.txt').write_bytes(call('uninstall',TEST))
            except Exception as failure:report['observer_cleanup_error']=str(failure);report['passed']=False
        report['elapsed_seconds']=round(time.monotonic()-started,3)
        report['evidence_sha256']={str(path.relative_to(out)):sha(path) for path in sorted(out.rglob('*')) if path.is_file() and 'runner-source' not in path.parts}
        (out/'report.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
        print(json.dumps({'passed':report['passed'],'evidence':str(out),'phase':args.phase,'checks':report.get('native',{}).get('checkCount'),'touches':report.get('native',{}).get('touchCount'),'error':report.get('error')}))

if __name__=='__main__':main()
