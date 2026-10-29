#!/usr/bin/env python3
"""Observe exact installed bytes on the one explicitly created task-owned v118 device."""
from pathlib import Path
import argparse, hashlib, json, os, re, shutil, subprocess, tempfile, time, uuid

ROOT=Path(__file__).resolve().parents[2]
HERE=Path(__file__).resolve().parent
TEST='com.androidhit.lumadrift.expeditiontest.v118'
RUNNER=TEST+'/com.androidhit.lumadrift.qa.v118.ExpeditionInstrumentation'
SIGNER='dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287'

def sha(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--serial',required=True)
    p.add_argument('--expected-avd',required=True)
    p.add_argument('--creation-receipt',required=True,type=Path)
    p.add_argument('--creation-sha256',required=True)
    p.add_argument('--expected-apk',required=True,type=Path)
    p.add_argument('--sha256',required=True)
    p.add_argument('--test-apk',required=True,type=Path)
    p.add_argument('--variant',choices=['production','lab'],default='lab')
    p.add_argument('--expected-version',required=True)
    p.add_argument('--expected-code',required=True,type=int)
    p.add_argument('--phase',required=True)
    p.add_argument('--label',required=True)
    a=p.parse_args()
    assert a.serial=='emulator-5594' and a.expected_avd=='Luma118-Expedition-QA','Exact newly created task-owned AVD/port required'
    assert sha(a.creation_receipt)==a.creation_sha256,'Explicit immutable creation receipt hash required'
    receipt=json.loads(a.creation_receipt.read_text())
    assert receipt['fresh'] and receipt['serial']==a.serial and receipt['avd']==a.expected_avd,'Task device provenance differs'
    assert a.expected_apk.is_file() and re.fullmatch('[a-f0-9]{64}',a.sha256) and sha(a.expected_apk)==a.sha256,'Frozen exact candidate hash required'
    assert a.test_apk.is_file() and re.fullmatch('[A-Za-z0-9_-]{1,60}',a.label) and re.fullmatch('[a-z][a-z0-9_-]{1,50}',a.phase),'Frozen observer and safe new evidence label/phase required'
    sdk=Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'));adb=sdk/'platform-tools/adb'
    env=os.environ.copy();env.setdefault('JAVA_HOME','/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home')
    def call(*parts,timeout=90):return subprocess.check_output([str(adb),'-s',a.serial,*[str(x) for x in parts]],stderr=subprocess.STDOUT,timeout=timeout,env=env)
    def shell(*parts):return call('shell',*parts).decode(errors='replace')
    def installed(pkg):
        r=subprocess.run([str(adb),'-s',a.serial,'shell','pm','path',pkg],capture_output=True,text=True,timeout=30)
        assert r.returncode in [0,1],'Package query failed'
        lines=[x[8:] for x in r.stdout.splitlines() if x.startswith('package:')]
        assert len(lines)<=1 and all(x.startswith('/data/app/') for x in lines),'One exact installed APK required'
        return lines[0] if lines else None
    assert shell('getprop','ro.kernel.qemu').strip()=='1','Emulator only'
    assert call('emu','avd','name').decode().splitlines()[0].strip()==a.expected_avd,'Live AVD identity differs'
    package='com.androidhit.lumadrift'+('.sandbox' if a.variant=='lab' else '')
    target=installed(package);assert target,'Owner must install the exact candidate; this runner never installs/upgrades the game'
    assert not installed(TEST),'Separate observer must initially be absent'
    out=ROOT/'docs/validation/v1.18/native-expedition'/a.label
    assert not out.exists(),'Evidence directory must be new; failed attempts remain immutable'
    out.mkdir(parents=True)
    observer=out/'expedition-observer.apk';shutil.copyfile(a.test_apk,observer)
    shutil.copyfile(a.creation_receipt,out/'device-creation.json')
    snapshots={}
    for source in sorted(HERE.rglob('*')):
        if source.is_file() and '__pycache__' not in source.parts:
            rel=source.relative_to(HERE);dest=out/'runner-source'/rel;dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(source,dest);snapshots[str(rel)]=sha(dest)
    (out/'source-snapshot.json').write_text(json.dumps(snapshots,indent=2)+'\n')
    report={'passed':False,'serial':a.serial,'avd':a.expected_avd,'package':package,'phase':a.phase,'expected_version':a.expected_version,'expected_code':a.expected_code,'apk_sha256':a.sha256,'observer_sha256':sha(observer),'creation_receipt_sha256':a.creation_sha256,'run_token':str(uuid.uuid4()),'scope':'Actual native UI and read-only preference observations on a newly task-owned QA emulator. No target install/erase, preference writes, target methods, solver calls, provider, account, ad or external purchase. Earned-coin acquisitions are exercised only by named scenarios. Original existing profiles remain untouched.'}
    started=time.monotonic();observer_installed=False
    try:
        for name,path in [('candidate',a.expected_apk),('observer',observer)]:
            proof=subprocess.check_output([str(sdk/'build-tools/36.0.0/apksigner'),'verify','--verbose','--print-certs',str(path)],env=env,stderr=subprocess.STDOUT).decode()
            (out/(name+'-signature.txt')).write_text(proof)
            observed=re.findall(r'Signer #1 certificate SHA-256 digest: ([a-f0-9]+)',proof)
            assert observed==[SIGNER],name+' signature identity differs'
        with tempfile.TemporaryDirectory(prefix='luma118-native-installed-') as temp:
            actual=Path(temp)/'installed.apk';call('pull',target,actual);report['installed_sha256']=sha(actual)
            assert sha(actual)==a.sha256,'Exact installed candidate bytes differ'
        report['device']={key:shell(*cmd).strip() for key,cmd in {'sdk':['getprop','ro.build.version.sdk'],'abi':['getprop','ro.product.cpu.abi'],'page_size':['getconf','PAGESIZE'],'size':['wm','size'],'density':['wm','density']}.items()}
        (out/'observer-install.txt').write_bytes(call('install','-t',observer,timeout=180));observer_installed=True
        shell('am','force-stop',package)
        command=['shell','am','instrument','-w','-r','-e','phase',a.phase,'-e','runToken',report['run_token'],'-e','expectedVersion',a.expected_version,'-e','expectedCode',str(a.expected_code),RUNNER]
        result=call(*command,timeout=900);(out/'instrumentation.txt').write_bytes(result)
        remote='/sdcard/Android/data/'+package+'/files/v118-native/'+report['run_token']+'/'+a.phase
        call('pull',remote,out/'native',timeout=180)
        native=json.loads((out/'native/report.json').read_text());report['native']=native
        assert b'INSTRUMENTATION_CODE: -1' in result and b'INSTRUMENTATION_FAILED' not in result,'Current native instrumentation failed; no stale report can qualify'
        assert native['phase']==a.phase and native['runToken']==report['run_token'],'Fresh report phase/execution token differs'
        assert native['passed'],'Native observer assertions failed'
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
        report['evidence_sha256']={str(x.relative_to(out)):sha(x) for x in sorted(out.rglob('*')) if x.is_file() and 'runner-source' not in x.parts}
        (out/'report.json').write_text(json.dumps(report,indent=2)+'\n')
        print(json.dumps({'passed':report['passed'],'evidence':str(out),'phase':a.phase,'checks':report.get('native',{}).get('checkCount'),'touches':report.get('native',{}).get('touchCount'),'error':report.get('error')}))

if __name__=='__main__':main()
