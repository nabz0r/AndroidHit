#!/usr/bin/env python3
"""First-install SDK qualification on the explicitly owned second QA emulator; no profile injection."""
from pathlib import Path
import argparse,hashlib,json,os,re,shutil,subprocess,tempfile,traceback
ROOT=Path(__file__).resolve().parents[2]
SERIAL='emulator-5584';PACKAGE='com.androidhit.lumadrift.sandbox'
REMOTE='/sdcard/Android/data/'+PACKAGE+'/files/v112-sdk-ui'
ADB=str(Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'))/'platform-tools/adb')
def adb(*parts,timeout=60):return subprocess.check_output([ADB,'-s',SERIAL,*parts],stderr=subprocess.STDOUT,timeout=timeout)
def shell(*parts):return adb('shell',*parts).decode(errors='replace')
def sha(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()
def main():
    global SERIAL
    p=argparse.ArgumentParser();p.add_argument('--serial',choices=['emulator-5584','emulator-5586'],default='emulator-5584');p.add_argument('--test-apk',required=True);p.add_argument('--expected-apk',default=str(ROOT/'releases/luma-drift-1.12.0-sandbox-rc2.apk'));p.add_argument('--label',default='fresh-j');p.add_argument('--expected-version',default='1.12.0-sandbox-rc2');p.add_argument('--expected-code',type=int,default=13);p.add_argument('--cases',choices=['tutorial','tutorial,gestures'],default='tutorial');a=p.parse_args();SERIAL=a.serial
    assert re.fullmatch(r'[a-zA-Z0-9_-]{1,40}',a.label),'Safe unique evidence label required'
    apk=Path(a.expected_apk);test=Path(a.test_apk)
    out=ROOT/'docs/validation/v1.12/ui-bugs'/('ui-sdk-'+a.label);shots=ROOT/'docs/screenshots/v1.12/ui-bugs'/('ui-sdk-'+a.label)
    assert not out.exists() and not shots.exists(),'Do not overwrite first-install evidence'
    assert shell('getprop','ro.kernel.qemu').strip()=='1','Owned QA emulator only'
    expected_avd={'emulator-5584':'Luma-Release111-J','emulator-5586':'Luma-Release111-K'}[SERIAL]
    assert adb('emu','avd','name').decode().splitlines()[0].strip()==expected_avd,'Exact explicitly owned fresh J/K AVD required'
    assert shell('getconf','PAGESIZE').strip()=='16384','Fresh J/K qualification requires its actual16KiB runtime'
    absent=subprocess.run([ADB,'-s',SERIAL,'shell','pm','path',PACKAGE],stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=60)
    assert absent.returncode in [0,1] and not absent.stdout.strip(),'Fresh qualification requires the Lab package to be genuinely absent; never clears a prior profile'
    out.mkdir(parents=True);shots.mkdir(parents=True)
    rotation={key:shell('settings','get','system',key).strip() for key in ['user_rotation','accelerometer_rotation']}
    production_query=subprocess.run([ADB,'-s',SERIAL,'shell','pm','path','com.androidhit.lumadrift'],stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=60)
    assert production_query.returncode in [0,1],'Could not inspect production package presence'
    production=production_query.stdout.decode().strip().removeprefix('package:')
    assert not production or production.startswith('/data/app/'),'Unexpected production package path response'
    production_hash=shell('sha256sum',production).split()[0] if production else None
    report={'passed':False,'serial':SERIAL,'package':PACKAGE,'fresh_lab_package_absent_before_install':True,'scope':'First real Lab installation; SDK accessibility and native touchscreen actions; no profile injection, account, ad or payment','apk_sha256':sha(apk),'test_apk_sha256':sha(test),'wrapper_source_sha256':sha(Path(__file__)),'cases':a.cases,'expected_version':a.expected_version,'expected_code':a.expected_code,'runner_source_sha256':sha(ROOT/'scripts/v112-ui-test/java/com/androidhit/lumadrift/qa/v112/CandidateUiInstrumentation.java'),'device':{'api':shell('getprop','ro.build.version.sdk').strip(),'abi':shell('getprop','ro.product.cpu.abi').strip(),'page_size':shell('getconf','PAGESIZE').strip()},'rotation_before':rotation,'production_apk_sha256_before':production_hash,'production_package_present_before_install':bool(production)}
    error=None
    try:
        before=shell('logcat','-d','-b','crash')
        (out/'target-install.txt').write_bytes(adb('install',str(apk)));(out/'test-install.txt').write_bytes(adb('install','-r',str(test)))
        installed=shell('pm','path',PACKAGE).strip().removeprefix('package:');report['installed_apk_sha256']=shell('sha256sum',installed).split()[0];assert report['installed_apk_sha256']==report['apk_sha256']
        run=adb('shell','am','instrument','-w','-r','-e','cases',a.cases,'-e','expectedVersion',a.expected_version,'-e','expectedCode',str(a.expected_code),'-e','expectFreshTutorial','true',PACKAGE+'.test/com.androidhit.lumadrift.qa.v112.CandidateUiInstrumentation',timeout=600);(out/'instrumentation.txt').write_bytes(run)
        after=shell('logcat','-d','-b','crash');delta=after[len(before):] if after.startswith(before) else after
        if delta.strip():(out/'native-crash-delta.txt').write_text(delta)
        with tempfile.TemporaryDirectory(prefix='luma-v18-fresh-sdk-') as tmp:
            adb('pull',REMOTE,tmp);pulled=Path(tmp)/'v112-sdk-ui'
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
        after_query=subprocess.run([ADB,'-s',SERIAL,'shell','pm','path','com.androidhit.lumadrift'],stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=60);assert after_query.returncode in [0,1]
        after_path=after_query.stdout.decode().strip().removeprefix('package:');report['production_package_present_after_test']=bool(after_path)
        report['production_apk_sha256_after']=shell('sha256sum',after_path).split()[0] if after_path else None
        report['production_state_unchanged']=after_path==production and report['production_apk_sha256_after']==production_hash
        report['production_apk_unchanged']=report['production_apk_sha256_after']==production_hash if production else None
        report['new_guest_test_profile_retained']=True
        (out/'host-report.json').write_text(json.dumps(report,indent=2)+'\n')
    if error:raise error
    assert report['rotation_settings_restored'] and report['production_state_unchanged']
    print(json.dumps({'passed':True,'checks':report['native']['checkCount'],'screenshots':len(report['screenshots']),'output':str(out)},indent=2))
if __name__=='__main__':main()
