#!/usr/bin/env python3
"""One guarded native Lab upgrade, only after exact old/new candidates have been frozen."""
from pathlib import Path
import argparse, hashlib, json, os, re, shutil, subprocess, tempfile, traceback
ROOT=Path(__file__).resolve().parents[2]
PACKAGE='com.androidhit.lumadrift.sandbox'
OBSERVER='com.androidhit.lumadrift.upgradetest'
RUNNER=OBSERVER+'/com.androidhit.lumadrift.qa.v115upgrade.UpgradeInstrumentation'
REMOTE='/sdcard/Android/data/'+PACKAGE+'/files/v115-upgrade-ui'

def sha(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()
def main():
    p=argparse.ArgumentParser()
    p.add_argument('--serial',required=True)
    p.add_argument('--expected-avd',default='Luma-Refonte115-QA')
    p.add_argument('--old-apk',required=True);p.add_argument('--old-sha256',required=True)
    p.add_argument('--new-apk',required=True);p.add_argument('--new-sha256',required=True)
    p.add_argument('--test-apk',required=True);p.add_argument('--test-sha256',required=True)
    p.add_argument('--old-version',default='1.14.0-sandbox-rc2');p.add_argument('--new-version',default='1.15.0-sandbox-rc1')
    p.add_argument('--label',required=True)
    p.add_argument('--resume-old-run',help='Continue exact old first-exit profile from a preserved failed observation, without clearing it')
    p.add_argument('--finish-old',action='store_true')
    p.add_argument('--purchase-cactus',action='store_true',help='Observe genuine130-coin cactus purchase, Cancel, Rotate and explicit placement after migration proof')
    p.add_argument('--execute-frozen-candidates',action='store_true',help='Explicit final execution gate after source/APK freeze')
    a=p.parse_args()
    assert a.execute_frozen_candidates,'No execution before the root agent freezes candidates and supplies --execute-frozen-candidates'
    assert a.serial=='emulator-5586' and a.expected_avd=='Luma-Refonte115-QA','Only the newly owned exact QA AVD is in scope'
    assert re.fullmatch(r'[A-Za-z0-9_-]{1,50}',a.label),'Fresh safe evidence label required'
    paths={name:Path(getattr(a,name+'_apk')).resolve() for name in ['old','new','test']}
    expected={name:getattr(a,name+'_sha256') for name in paths}
    for name,path in paths.items():assert path.is_file() and re.fullmatch(r'[0-9a-f]{64}',expected[name]) and sha(path)==expected[name],name+' candidate does not match explicit frozen SHA256'
    sdk=Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'))
    build_tools=sdk/'build-tools/36.0.0'
    host_env=os.environ.copy()
    if not host_env.get('JAVA_HOME') and Path('/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home').exists():host_env['JAVA_HOME']='/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home'
    identities={};certificates={}
    for name,path in paths.items():
        badging=subprocess.check_output([str(build_tools/'aapt2'),'dump','badging',str(path)],env=host_env,text=True,stderr=subprocess.STDOUT)
        match=re.search(r"package: name='([^']+)' versionCode='([^']*)' versionName='([^']*)'",badging)
        assert match,'Cannot verify APK package identity before installation'
        identities[name]={'package':match[1],'code':int(match[2]) if match[2].isdigit() else None,'version':match[3]}
        assert match[1]==(OBSERVER if name=='test' else PACKAGE),'APK package mismatch; production installation must never be replaced'
        if name!='test':assert int(match[2])==(16 if name=='old' else 17) and match[3]==(a.old_version if name=='old' else a.new_version),'Wrong frozen target version'
        signing=subprocess.check_output([str(build_tools/'apksigner'),'verify','--print-certs',str(path)],env=host_env,text=True,stderr=subprocess.STDOUT)
        match=re.search(r'Signer #1 certificate SHA-256 digest: ([0-9a-f]{64})',signing)
        assert match,'Verified signer digest absent'
        certificates[name]=match[1]
    assert len(set(certificates.values()))==1,'Old app, new app and observer must have the same verified signing certificate'
    adb=str(sdk/'platform-tools/adb')
    def call(*parts,timeout=90):return subprocess.check_output([adb,'-s',a.serial,*parts],stderr=subprocess.STDOUT,timeout=timeout)
    def shell(*parts):return call('shell',*parts).decode(errors='replace')
    def query(package):
        value=subprocess.run([adb,'-s',a.serial,'shell','pm','path',package],text=True,capture_output=True,timeout=45)
        assert value.returncode in [0,1],'Package query failed'
        result=value.stdout.strip();assert not result or result.startswith('package:/data/app/'),'Unexpected package path'
        return result.removeprefix('package:')
    assert shell('getprop','ro.kernel.qemu').strip()=='1','Emulator only'
    assert call('emu','avd','name').decode().splitlines()[0].strip()==a.expected_avd,'Wrong emulator identity'
    prior=None;prior_sha=None
    if a.resume_old_run:
        prior_path=Path(a.resume_old_run).resolve();prior=json.loads((prior_path/'host-report.json').read_text())
        prior_native=prior_path/'old/report-old.json'
        assert not prior['passed'] and len(prior['phases'])==1 and prior['phases'][0]['phase']=='old' and prior_native.is_file(),'Only a preserved failed old observation can be continued'
        assert prior['candidates']['old']==expected['old'] and prior['candidates']['new']==expected['new'] and prior['retained_target_sha256']==expected['old'] and prior['production_installation_unchanged'],'Prior run must retain exact old/new target identity and untouched production'
        existing=query(PACKAGE);assert existing and shell('sha256sum',existing).split()[0]==expected['old'],'Continuation requires the exact preserved old APK'
        prior_sha=sha(prior_native)
    else:assert not query(PACKAGE),'Separate Lab target must be absent; the harness never clears or overwrites a prior Lab profile'
    assert not query(OBSERVER),'Upgrade observer must initially be absent'
    production=query('com.androidhit.lumadrift')
    production_hash=shell('sha256sum',production).split()[0] if production else None
    out=ROOT/'docs/validation/v1.15/upgrade-native'/a.label
    shots=ROOT/'docs/screenshots/v1.15/upgrade-native'/a.label
    assert not out.exists() and not shots.exists(),'Fresh output directories required'
    out.mkdir(parents=True);shots.mkdir(parents=True)
    report={'passed':False,'serial':a.serial,'avd':a.expected_avd,'target':PACKAGE,'candidates':expected,'identities':identities,'verified_certificates':certificates,'continued_old_native_report_sha256':prior_sha,'finish_old_requested':a.finish_old,'purchase_cactus_requested':a.purchase_cactus,'source_sha256':sha(__file__),'observer_source_sha256':sha(ROOT/'scripts/v115-upgrade-ui/java/com/androidhit/lumadrift/qa/v115upgrade/UpgradeInstrumentation.java'),'production_apk_sha256_before':production_hash,'phases':[],'scope':'Lab-only real APK update on newly owned QA AVD. Original six-by-six state created by actual observed floor touch, then genuine N15 public challenge first exit. Typed preference reads, native accessibility and screenshots compare exact migration. No save values or progress are injected. Production installation is not modified.','screenshots':{}}
    error=None;test_installed=False;target_installed=False;last_target_hash=None
    try:
        if not prior:
            result=call('install',str(paths['old']));(out/'old-install.txt').write_bytes(result);assert b'Success' in result
        else:(out/'old-retained-profile.json').write_text(json.dumps({'prior_native_report_sha256':prior_sha,'old_profile_retained_without_reset':True})+'\n')
        target_installed=True;last_target_hash=expected['old']
        assert shell('sha256sum',query(PACKAGE)).split()[0]==expected['old'],'Installed old bytes differ'
        result=call('install',str(paths['test']));(out/'observer-install.txt').write_bytes(result);assert b'Success' in result;test_installed=True
        assert shell('sha256sum',query(OBSERVER)).split()[0]==expected['test'],'Installed observer bytes differ'
        for phase,version,code in [('old',a.old_version,16),('new',a.new_version,17)]:
            if phase=='new':
                assert report['phases'][-1]['native']['passed'],'Upgrade requires a genuinely passed old checkpoint'
                result=call('install','-r',str(paths['new']));(out/'new-install-update.txt').write_bytes(result);assert b'Success' in result;last_target_hash=expected['new']
                assert shell('sha256sum',query(PACKAGE)).split()[0]==expected['new'],'Installed new bytes differ'
            details=shell('dumpsys','package',PACKAGE);assert 'versionName='+version in details and 'versionCode='+str(code)+' ' in details,'Installed phase version/code differ'
            shell('am','force-stop',PACKAGE)
            alive=subprocess.run([adb,'-s',a.serial,'shell','pidof',PACKAGE],text=True,capture_output=True,timeout=30)
            assert alive.returncode in [0,1] and not alive.stdout.strip(),'App process still alive at phase boundary'
            phase_out=out/phase;phase_shots=shots/phase;phase_out.mkdir();phase_shots.mkdir()
            run=call('shell','am','instrument','-w','-r','-e','phase',phase,'-e','resumeObservedOld','true' if prior else 'false','-e','priorOldReportSha',prior_sha or 'none', '-e','expectedVersion',version,'-e','expectedCode',str(code),'-e','finishOld','true' if a.finish_old else 'false','-e','purchaseCactus','true' if a.purchase_cactus else 'false',RUNNER,timeout=480)
            (phase_out/'instrumentation.txt').write_bytes(run)
            with tempfile.TemporaryDirectory(prefix='luma-v115-upgrade-pull-') as tmp:
                call('pull',REMOTE,tmp);pulled=Path(tmp)/'v115-upgrade-ui'
                assert (pulled/('report-'+phase+'.json')).exists(),'Native phase report absent; instrumentation retained'
                native=json.loads((pulled/('report-'+phase+'.json')).read_text());captures=set(native.get('captures',[]))
                for file in pulled.iterdir():
                    if not file.is_file():continue
                    if file.suffix=='.png':
                        if file.stem not in captures:continue
                        shutil.copyfile(file,phase_shots/file.name);report['screenshots'][phase+'/'+file.name]=sha(file)
                    else:shutil.copyfile(file,phase_out/file.name)
            report['phases'].append({'phase':phase,'native':native});assert native['passed'],'Native '+phase+' upgrade observation failed'
        report['passed']=True
    except BaseException as failure:
        error=failure;report['error']=str(failure);report['traceback']=traceback.format_exc()
    finally:
        if target_installed:shell('am','force-stop',PACKAGE)
        if test_installed:(out/'observer-uninstall.txt').write_bytes(call('uninstall',OBSERVER))
        report['observer_absent_after']=not query(OBSERVER)
        current=query(PACKAGE);report['retained_target_sha256']=shell('sha256sum',current).split()[0] if current else None
        report['owned_qa_lab_profile_retained']=True
        current_production=query('com.androidhit.lumadrift');report['production_apk_sha256_after']=shell('sha256sum',current_production).split()[0] if current_production else None
        report['production_installation_unchanged']=report['production_apk_sha256_after']==production_hash
        report['passed']=report['passed'] and report['observer_absent_after'] and report['retained_target_sha256']==last_target_hash and report['production_installation_unchanged']
        (out/'host-report.json').write_text(json.dumps(report,indent=2,ensure_ascii=False)+'\n')
    if error:raise error
    assert report['passed'],'Post-run acceptance failed'
    print(json.dumps({'passed':True,'checks':sum(x['native']['checkCount'] for x in report['phases']),'report':str(out/'host-report.json')},indent=2))
if __name__=='__main__':main()
