#!/usr/bin/env python3
"""First-install SDK qualification on the explicitly owned second QA emulator; no profile injection."""
from pathlib import Path
import hashlib,json,os,shutil,subprocess,tempfile,traceback
ROOT=Path(__file__).resolve().parents[2]
SERIAL='emulator-5582';PACKAGE='com.androidhit.lumadrift.sandbox'
REMOTE='/sdcard/Android/data/'+PACKAGE+'/files/v18-sdk-ui'
ADB=str(Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'))/'platform-tools/adb')
def adb(*parts,timeout=60):return subprocess.check_output([ADB,'-s',SERIAL,*parts],stderr=subprocess.STDOUT,timeout=timeout)
def shell(*parts):return adb('shell',*parts).decode(errors='replace')
def sha(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()
def main():
    apk=ROOT/'releases/luma-drift-1.8.0-sandbox.apk';test=ROOT/'app/build/outputs/apk/androidTest/lab/release/app-lab-release-androidTest.apk'
    out=ROOT/'docs/validation/v1.8/ui-sdk-fresh-guest';shots=ROOT/'docs/screenshots/v1.8/ui-sdk-fresh-guest'
    assert not out.exists() and not shots.exists(),'Do not overwrite first-install evidence'
    assert shell('getprop','ro.kernel.qemu').strip()=='1','Owned QA emulator only'
    absent=subprocess.run([ADB,'-s',SERIAL,'shell','pm','path',PACKAGE],stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=60)
    assert absent.returncode in [0,1] and not absent.stdout.strip(),'Fresh qualification requires the Lab package to be genuinely absent; never clears a prior profile'
    out.mkdir(parents=True);shots.mkdir(parents=True)
    rotation={key:shell('settings','get','system',key).strip() for key in ['user_rotation','accelerometer_rotation']}
    production=shell('pm','path','com.androidhit.lumadrift').strip().removeprefix('package:')
    production_hash=shell('sha256sum',production).split()[0] if production else None
    report={'passed':False,'serial':SERIAL,'package':PACKAGE,'fresh_lab_package_absent_before_install':True,'scope':'First real Lab installation; SDK accessibility and native touchscreen actions; no profile injection, account, ad or payment','apk_sha256':sha(apk),'test_apk_sha256':sha(test),'runner_source_sha256':sha(ROOT/'scripts/v18-ui-test/java/com/androidhit/lumadrift/qa/CandidateUiInstrumentation.java'),'device':{'api':shell('getprop','ro.build.version.sdk').strip(),'abi':shell('getprop','ro.product.cpu.abi').strip(),'page_size':shell('getconf','PAGESIZE').strip()},'rotation_before':rotation,'production_apk_sha256_before':production_hash}
    error=None
    try:
        before=shell('logcat','-d','-b','crash')
        (out/'target-install.txt').write_bytes(adb('install',str(apk)));(out/'test-install.txt').write_bytes(adb('install','-r',str(test)))
        installed=shell('pm','path',PACKAGE).strip().removeprefix('package:');report['installed_apk_sha256']=shell('sha256sum',installed).split()[0];assert report['installed_apk_sha256']==report['apk_sha256']
        run=adb('shell','am','instrument','-w','-r','-e','cases','tutorial,gestures','-e','expectFreshTutorial','true',PACKAGE+'.test/com.androidhit.lumadrift.qa.CandidateUiInstrumentation',timeout=600);(out/'instrumentation.txt').write_bytes(run)
        after=shell('logcat','-d','-b','crash');delta=after[len(before):] if after.startswith(before) else after
        if delta.strip():(out/'native-crash-delta.txt').write_text(delta)
        with tempfile.TemporaryDirectory(prefix='luma-v18-fresh-sdk-') as tmp:
            adb('pull',REMOTE,tmp);pulled=Path(tmp)/'v18-sdk-ui'
            for file in pulled.iterdir():shutil.copyfile(file,(shots if file.suffix=='.png' else out)/file.name)
            assert (pulled/'report.json').exists(),'Native report missing; inspect instrumentation/crash evidence'
            native=json.loads((pulled/'report.json').read_text());report['native']=native
        report['screenshots']={p.name:sha(p) for p in shots.glob('*.png')}
        assert not delta.strip(),'New native crash entries'
        assert native['passed'],'Native first-install qualification failed'
        report['passed']=True
    except BaseException as failure:error=failure;report['error']=str(failure);report['traceback']=traceback.format_exc()
    finally:
        shell('am','force-stop',PACKAGE)
        for key,value in rotation.items():
            if value=='null':shell('settings','delete','system',key)
            else:shell('settings','put','system',key,value)
        report['rotation_settings_restored']={key:shell('settings','get','system',key).strip() for key in rotation}==rotation
        report['production_apk_sha256_after']=shell('sha256sum',production).split()[0] if production else None
        report['production_apk_unchanged']=report['production_apk_sha256_after']==production_hash
        report['new_guest_test_profile_retained']=True
        (out/'host-report.json').write_text(json.dumps(report,indent=2)+'\n')
    if error:raise error
    assert report['rotation_settings_restored'] and report['production_apk_unchanged']
    print(json.dumps({'passed':True,'checks':report['native']['checkCount'],'screenshots':len(report['screenshots']),'output':str(out)},indent=2))
if __name__=='__main__':main()
