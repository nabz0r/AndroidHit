#!/usr/bin/env python3
"""First-install home SDK qualification on the explicitly owned second QA emulator; no profile injection."""
from pathlib import Path
import argparse,hashlib,json,os,re,shutil,subprocess,tempfile,traceback
ROOT=Path(__file__).resolve().parents[2]
SERIAL='emulator-5588';PACKAGE='com.androidhit.lumadrift.sandbox'
REMOTE='/sdcard/Android/data/'+PACKAGE+'/files/v113-sdk-ui'
ADB=str(Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'))/'platform-tools/adb')
def adb(*parts,timeout=60):return subprocess.check_output([ADB,'-s',SERIAL,*parts],stderr=subprocess.STDOUT,timeout=timeout)
def shell(*parts):return adb('shell',*parts).decode(errors='replace')
def sha(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()
def main():
    global SERIAL
    p=argparse.ArgumentParser();p.add_argument('--serial',choices=['emulator-5588'],default='emulator-5588');p.add_argument('--test-apk',required=True);p.add_argument('--expected-apk',default=str(ROOT/'releases/luma-drift-1.13.0-sandbox-rc1.apk'));p.add_argument('--label',default='fresh-home');p.add_argument('--expected-version',default='1.13.0-sandbox-rc1');p.add_argument('--expected-code',type=int,default=14);p.add_argument('--cases',choices=['firstlight,house-smoke','firstlight,house-smoke,house-locales'],default='firstlight,house-smoke');a=p.parse_args();SERIAL=a.serial
    assert re.fullmatch(r'[a-zA-Z0-9_-]{1,40}',a.label),'Safe unique evidence label required'
    apk=Path(a.expected_apk);test=Path(a.test_apk)
    out=ROOT/'docs/validation/v1.13/ui-bugs'/('ui-sdk-'+a.label);shots=ROOT/'docs/screenshots/v1.13/ui-bugs'/('ui-sdk-'+a.label)
    assert not out.exists() and not shots.exists(),'Do not overwrite first-install evidence'
    assert shell('getprop','ro.kernel.qemu').strip()=='1','Owned QA emulator only'
    expected_avd={'emulator-5588':'Luma-Release113-Home-N'}[SERIAL]
    assert adb('emu','avd','name').decode().splitlines()[0].strip()==expected_avd,'Exact explicitly owned Lab AVD required'
    assert shell('getconf','PAGESIZE').strip()=='16384','Fresh owned Lab qualification requires its actual16KiB runtime'
    absent=subprocess.run([ADB,'-s',SERIAL,'shell','pm','path',PACKAGE],stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=60)
    assert absent.returncode in [0,1] and not absent.stdout.strip(),'Fresh qualification requires the Lab package to be genuinely absent; never clears a prior profile'
    out.mkdir(parents=True);shots.mkdir(parents=True)
    rotation={key:shell('settings','get','system',key).strip() for key in ['user_rotation','accelerometer_rotation']}
    production_query=subprocess.run([ADB,'-s',SERIAL,'shell','pm','path','com.androidhit.lumadrift'],stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=60)
    assert production_query.returncode in [0,1],'Could not inspect production package presence'
    production=production_query.stdout.decode().strip().removeprefix('package:')
    assert not production or production.startswith('/data/app/'),'Unexpected production package path response'
    production_hash=shell('sha256sum',production).split()[0] if production else None
    report={'passed':False,'serial':SERIAL,'package':PACKAGE,'fresh_lab_package_absent_before_install':True,'scope':'First real Lab installation; SDK accessibility and native touchscreen actions; no profile injection, account, ad or payment','apk_sha256':sha(apk),'test_apk_sha256':sha(test),'wrapper_source_sha256':sha(Path(__file__)),'cases':a.cases,'expected_version':a.expected_version,'expected_code':a.expected_code,'runner_source_sha256':sha(ROOT/'scripts/v113-ui-test/java/com/androidhit/lumadrift/qa/v113/CandidateUiInstrumentation.java'),'device':{'api':shell('getprop','ro.build.version.sdk').strip(),'abi':shell('getprop','ro.product.cpu.abi').strip(),'page_size':shell('getconf','PAGESIZE').strip()},'rotation_before':rotation,'production_apk_sha256_before':production_hash,'production_package_present_before_install':bool(production)}
    error=None;test_installed=False
    try:
        before=shell('logcat','-d','-b','crash')
        prior_test=subprocess.run([ADB,'-s',SERIAL,'shell','pm','path','com.androidhit.lumadrift.hometest'],capture_output=True,text=True,timeout=30);assert prior_test.returncode in (0,1) and not prior_test.stdout.strip(),'Isolated hometest must be absent initially'
        (out/'target-install.txt').write_bytes(adb('install',str(apk)));test_result=adb('install','-r',str(test));(out/'test-install.txt').write_bytes(test_result);assert b'Success' in test_result;test_installed=True
        test_path=shell('pm','path','com.androidhit.lumadrift.hometest').strip().removeprefix('package:');report['installed_test_apk_sha256']=shell('sha256sum',test_path).split()[0];assert report['installed_test_apk_sha256']==sha(test)
        installed=shell('pm','path',PACKAGE).strip().removeprefix('package:');report['installed_apk_sha256']=shell('sha256sum',installed).split()[0];assert report['installed_apk_sha256']==report['apk_sha256']
        report['phases']=[];report['screenshots']={};previous_pid=None
        for phase,cases,first_phase in [('prepare','firstlight','pause'),('cold',a.cases,'resume')]:
            if phase=='cold':
                assert report['phases'][-1]['native'].get('firstLightCheckpointReady'),'Fresh first-light prepare produced no valid observed checkpoint'
                previous_pid=report['phases'][-1]['native']['processId'];shell('am','force-stop',PACKAGE)
                absent_pid=subprocess.run([ADB,'-s',SERIAL,'shell','pidof',PACKAGE],capture_output=True,text=True,timeout=30);assert absent_pid.returncode in (0,1) and not absent_pid.stdout.strip(),'Target survives actual force-stop'
                report['cold_process_boundary']={'force_stop':True,'absent_before_relaunch':True,'prepare_process_id':previous_pid}
            phase_out=out/phase;phase_shots=shots/phase;phase_out.mkdir();phase_shots.mkdir()
            run=adb('shell','am','instrument','-w','-r','-e','cases',cases,'-e','expectedVersion',a.expected_version,'-e','expectedCode',str(a.expected_code),'-e','expectFreshHome','true','-e','firstLightPhase',first_phase,'com.androidhit.lumadrift.hometest/com.androidhit.lumadrift.qa.v113.CandidateUiInstrumentation',timeout=600);(phase_out/'instrumentation.txt').write_bytes(run)
            with tempfile.TemporaryDirectory(prefix='luma-v113-fresh-sdk-') as tmp:
                adb('pull',REMOTE,tmp);pulled=Path(tmp)/'v113-sdk-ui';assert (pulled/'report.json').exists(),'Native report missing; inspect instrumentation/crash evidence'
                native=json.loads((pulled/'report.json').read_text());names=set(native.get('captures',[]))
                for file in pulled.iterdir():
                    if file.suffix=='.png' and file.stem not in names:continue
                    shutil.copyfile(file,(phase_shots if file.suffix=='.png' else phase_out)/file.name)
                report['phases'].append({'phase':phase,'native':native});report['screenshots'].update({phase+'/'+p.name:sha(p) for p in phase_shots.glob('*.png')})
            assert native['passed'],'Native first-install '+phase+' qualification failed'
            if phase=='cold':
                report['cold_process_boundary']['cold_process_id']=native.get('processId');assert native.get('processId') and native['processId']!=previous_pid,'Actual cold phase did not use a distinct process'
        after=shell('logcat','-d','-b','crash');delta=after[len(before):] if after.startswith(before) else after
        if delta.strip():(out/'native-crash-delta.txt').write_text(delta)
        report['native']={'passed':all(p['native']['passed'] for p in report['phases']),'checkCount':sum(p['native']['checkCount'] for p in report['phases'])}
        assert not delta.strip(),'New native crash entries'
        report['passed']=True
    except BaseException as failure:error=failure;report['error']=str(failure);report['traceback']=traceback.format_exc()
    finally:
        shell('am','force-stop',PACKAGE)
        if test_installed:report['isolated_test_package_removed']='Success' in adb('uninstall','com.androidhit.lumadrift.hometest').decode()
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
