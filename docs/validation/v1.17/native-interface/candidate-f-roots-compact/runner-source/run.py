#!/usr/bin/env python3
"""Qualify the frozen1.17 candidate on a retained task-owned AVD through actual native UI only."""
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

ROOT=Path(__file__).resolve().parents[2]
TEST_PACKAGE='com.androidhit.lumadrift.qualification117test'
RUNNER=TEST_PACKAGE+'/com.androidhit.lumadrift.qa.v117.InterfaceInstrumentation'

def sha(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()

def main():
    p=argparse.ArgumentParser()
    p.add_argument('--serial',required=True)
    p.add_argument('--expected-avd',default='Luma-Refonte115-QA')
    p.add_argument('--expected-apk',required=True)
    p.add_argument('--sha256',required=True)
    p.add_argument('--test-apk',required=True)
    p.add_argument('--variant',choices=['production','lab'],default='production')
    p.add_argument('--expected-version',default='1.17.0-rc4')
    p.add_argument('--expected-code',type=int,default=22)
    p.add_argument('--label',required=True)
    p.add_argument('--install-if-absent',action='store_true')
    p.add_argument('--compact',action='store_true',help='Owned QA display880x1440 at440dpi; original size/density restored explicitly')
    p.add_argument('--font-scale',type=float,choices=[1.0,1.3],help='Explicit task-owned QA system font scale; exact original setting restored on success or failure')
    p.add_argument('--languages',nargs='+',choices=['en','fr','de','es','pt','it'],help='Explicit secondary-route language subset for a short preflight; omitted means all six. Partial runs never claim full coverage')
    p.add_argument('--route',choices=['baseline','upgrade','browse','banners','assets','rig','performance','secondary','transitions','language-regression','root-regression'],default='upgrade',help='Baseline reads installed state; upgrade validates retained identity,102sprites/6paintings and cold persistence; browse/banners capture six languages; rig previews owned actors; performance observes steady actualHome; secondary browses all six-language retained menus; transitions records8seconds of actual native routes; root-regression checks actual independent48dp root targets')
    p.add_argument('--expected-base-sha256',help='Explicitly permit replacing only these known task-owned installed APK bytes with the frozen candidate, preserving its data')
    a=p.parse_args()
    assert re.fullmatch(r'emulator-[0-9]+',a.serial),'Explicit emulator serial required'
    assert a.expected_avd=='Luma-Refonte115-QA' and a.serial=='emulator-5586','Exact authorized task-owned AVD and matching serial required'
    assert re.fullmatch(r'[A-Za-z0-9_-]{1,50}',a.label),'Safe unique evidence label required'
    assert not (a.route=='upgrade' and a.compact),'Run retained upgrade validation on the normal QA display; use browse for compact review'
    assert a.languages is None or a.route=='secondary','Language subsets are only valid for explicitly partial secondary routes'
    assert a.languages is None or len(a.languages)==len(set(a.languages)),'Secondary subset contains duplicate languages'
    apk=Path(a.expected_apk).resolve();test=Path(a.test_apk).resolve()
    assert apk.is_file() and re.fullmatch('[a-f0-9]{64}',a.sha256) and sha(apk)==a.sha256,'Explicit frozen candidate hash required'
    assert test.is_file(),'Separate signed interface observer APK required'
    region_manifest=ROOT/'scripts/v117-ui-test/assets/render-regions.json'
    regions=json.loads(region_manifest.read_text())
    assert regions['metadata_java_sha256']==sha(ROOT/'app/src/main/java/com/androidhit/lumadrift/ui/ArtRegions117.java'),'Frozen region fixture and renderer metadata source differ'
    region_sha=sha(region_manifest)
    package='com.androidhit.lumadrift'+('.sandbox' if a.variant=='lab' else '')
    remote='/sdcard/Android/data/'+package+'/files/v117-interface-ui'
    adb=str(Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'))/'platform-tools/adb')
    def call(*parts,timeout=90):return subprocess.check_output([adb,'-s',a.serial,*parts],stderr=subprocess.STDOUT,timeout=timeout)
    def shell(*parts):return call('shell',*parts).decode(errors='replace')
    def query(pkg):
        r=subprocess.run([adb,'-s',a.serial,'shell','pm','path',pkg],capture_output=True,text=True,timeout=60)
        assert r.returncode in [0,1],'Package query failed'
        value=r.stdout.strip();assert not value or value.startswith('package:/data/app/'),'Unexpected package path'
        return value.removeprefix('package:')
    assert shell('getprop','ro.kernel.qemu').strip()=='1','Emulator-only qualification'
    assert call('emu','avd','name').decode().splitlines()[0].strip()==a.expected_avd,'Exact newly owned AVD required'
    out=ROOT/'docs/validation/v1.17/native-interface'/a.label
    shots=ROOT/'docs/screenshots/v1.17/native-interface'/a.label
    assert not out.exists() and not shots.exists(),'Evidence output must not already exist'
    assert not query(TEST_PACKAGE),'Separate observer package must initially be absent'
    installed=query(package)
    assert installed or a.install_if_absent,'Candidate absent; provide explicit --install-if-absent'
    original_display={'size':shell('wm','size').strip(),'density':shell('wm','density').strip()}
    out.mkdir(parents=True);shots.mkdir(parents=True)
    observer=out/'interface-observer.apk';shutil.copyfile(test,observer)
    assert sha(observer)==sha(test),'Observer freeze copy differs';test=observer
    source_snapshot={}
    source_root=ROOT/'scripts/v117-ui-test'
    for source in sorted(source_root.rglob('*')):
        if not source.is_file() or '__pycache__' in source.parts:continue
        relative=source.relative_to(source_root);destination=out/'runner-source'/relative
        destination.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(source,destination)
        source_snapshot[str(relative)]=sha(destination)
    (out/'source-snapshot.json').write_text(json.dumps(source_snapshot,indent=2)+'\n')
    report={'passed':False,'package':package,'serial':a.serial,'avd':a.expected_avd,'expected_version':a.expected_version,'expected_code':a.expected_code,'apk_sha256':a.sha256,'test_apk_sha256':sha(test),'wrapper_source_sha256':sha(__file__),'runner_source_sha256':sha(ROOT/'scripts/v117-ui-test/java/com/androidhit/lumadrift/qa/v117/InterfaceInstrumentation.java'),'candidate_present_before_test':bool(installed),'scope':'Retained task-owned U1000 profile; exact1.17 candidate; original art, object/gesture navigation, home camera, nickname IME, badges, catalog and existing reveal with cold persistence; no spending, state injection, provider action or user device access.','phases':[],'screenshots':{},'device':{'sdk':shell('getprop','ro.build.version.sdk').strip(),'abi':shell('getprop','ro.product.cpu.abi').strip(),'page_size':shell('getconf','PAGESIZE').strip(),'density':shell('wm','density').strip(),'size':shell('wm','size').strip()}}
    report['display_before']=original_display
    report['frozen_observer_apk']=str(observer)
    report['compact']=a.compact
    report['route']=a.route
    report['secondary_languages']=a.languages if a.languages is not None else ['en','fr','de','es','pt','it']
    report['region_manifest_sha256']=region_sha
    report['region_metadata_source_sha256']=regions['metadata_java_sha256']
    report['font_scale']=shell('settings','get','system','font_scale').strip()
    original_font_scale=report['font_scale']
    started=time.monotonic();error=None;observer_installed=False;display_changed=False;font_changed=False
    def native_phase(phase,version,code):
        if report['phases']:
            assert report['phases'][-1]['native']['passed'],'Each phase requires a genuinely passed predecessor'
        previous_pid=report['phases'][-1]['native']['processId'] if report['phases'] else None
        shell('am','force-stop',package)
        absent=subprocess.run([adb,'-s',a.serial,'shell','pidof',package],capture_output=True,text=True,timeout=30)
        assert absent.returncode in [0,1] and not absent.stdout.strip(),'Target remains alive before native process boundary'
        phase_out=out/phase;phase_shots=shots/phase;phase_out.mkdir();phase_shots.mkdir()
        run=call('shell','am','instrument','-w','-r','-e','phase',phase,'-e','expectedVersion',version,'-e','expectedCode',str(code),'-e','regionManifestSha256',region_sha,'-e','languages',','.join(report['secondary_languages']),RUNNER,timeout=2400 if phase=='secondary' else 900)
        (phase_out/'instrumentation.txt').write_bytes(run)
        with tempfile.TemporaryDirectory(prefix='luma-v117-interface-pull-') as tmp:
            call('pull',remote,tmp);pulled=Path(tmp)/'v117-interface-ui'
            assert (pulled/'report.json').exists(),'Native report absent; instrumentation output retained'
            native=json.loads((pulled/'report.json').read_text());captures=set(native.get('captures',[]))
            assert len(captures)==len(native.get('captures',[])),'Native phase reused a screenshot identifier'
            for file in pulled.iterdir():
                if not file.is_file():continue
                if file.suffix=='.png':
                    if file.stem not in captures:continue
                    shutil.copyfile(file,phase_shots/file.name);report['screenshots'][phase+'/'+file.name]=sha(file)
                elif file.name=='failure.txt':
                    if not native.get('passed',False):shutil.copyfile(file,phase_out/file.name)
                    else:native['ignored_stale_failure_file_sha256']=sha(file)
                elif file.name in ['report.json','baseline.json','before.json','checkpoint.json','secondary-coverage.json'] or file.stem in captures or '-coverage-' in file.name or file.name=='assets.json' or file.name.startswith('performance') or file.name.startswith('transitions'):shutil.copyfile(file,phase_out/file.name)
        report['phases'].append({'phase':phase,'native':native,'expected_version':version,'expected_code':code})
        print(json.dumps({'phase':phase,'passed':native.get('passed',False),'checks':native.get('checkCount',0),'captures':len(native.get('captures',[]))}),flush=True)
        assert native['passed'],'Native interface '+phase+' failed'
        if previous_pid is not None:assert previous_pid!=native['processId'],'Independent phase reused its predecessor process'
    try:
        if not installed:
            install=call('install',str(apk));(out/'target-install.txt').write_bytes(install);assert b'Success' in install
            installed=query(package)
        installed_before_hash=shell('sha256sum',installed).split()[0];report['installed_apk_sha256_before']=installed_before_hash
        needs_upgrade=installed_before_hash!=a.sha256
        if needs_upgrade:
            assert a.route=='upgrade','Only the explicitly selected upgrade route can replace installed base bytes'
            assert a.expected_base_sha256 and re.fullmatch('[a-f0-9]{64}',a.expected_base_sha256) and installed_before_hash==a.expected_base_sha256,'Installed bytes are neither the frozen candidate nor an explicitly permitted upgrade base'
        before=shell('logcat','-d','-b','crash')
        result=call('install',str(test));(out/'test-install.txt').write_bytes(result);assert b'Success' in result;observer_installed=True
        assert shell('sha256sum',query(TEST_PACKAGE)).split()[0]==sha(test),'Installed observer differs from frozen test APK'
        if a.route in ['baseline','upgrade']:
            details=shell('dumpsys','package',package)
            base_version=re.search(r'versionName=([^\s]+)',details);base_code=re.search(r'versionCode=([0-9]+) ',details)
            assert base_version and base_code,'Installed baseline version metadata absent'
            report['baseline_apk_sha256']=installed_before_hash
            native_phase('baseline',base_version.group(1),int(base_code.group(1)))
        if needs_upgrade:
            upgrade=call('install','-r',str(apk));(out/'target-upgrade.txt').write_bytes(upgrade);assert b'Success' in upgrade
            installed=query(package)
        assert shell('sha256sum',installed).split()[0]==a.sha256,'Installed candidate differs from frozen APK'
        details=shell('dumpsys','package',package);assert 'versionName='+a.expected_version in details and 'versionCode='+str(a.expected_code)+' ' in details,'Installed version/code differs'
        if a.compact:
            shell('am','force-stop',package);display_changed=True
            shell('wm','size','880x1440');shell('wm','density','440')
            size=shell('wm','size').strip();density=shell('wm','density').strip()
            assert 'Override size: 880x1440' in size,'Compact QA size override not applied'
            actual_density=re.findall(r'(?:Physical|Override) density:\s*(\d+)',density)
            assert actual_density and actual_density[-1]=='440','Compact QA density not applied'
            report['compact_scope']='Actual native320dp-wide display,880x1440 pixels at440dpi; original display restored after both native processes'
        if a.font_scale is not None:
            shell('am','force-stop',package);font_changed=True
            shell('settings','put','system','font_scale',str(a.font_scale))
            assert abs(float(shell('settings','get','system','font_scale').strip())-a.font_scale)<.001,'Explicit QA font scale not applied'
            report['font_scale_during']=a.font_scale
        report['display_during']={'size':shell('wm','size').strip(),'density':shell('wm','density').strip()}
        if a.route=='upgrade':
            native_phase('assets',a.expected_version,a.expected_code)
            native_phase('prepare',a.expected_version,a.expected_code)
            native_phase('cold',a.expected_version,a.expected_code)
        elif a.route=='browse':native_phase('browse',a.expected_version,a.expected_code)
        elif a.route=='banners':native_phase('banners',a.expected_version,a.expected_code)
        elif a.route=='assets':native_phase('assets',a.expected_version,a.expected_code)
        elif a.route=='rig':native_phase('rig',a.expected_version,a.expected_code)
        elif a.route=='performance':native_phase('performance',a.expected_version,a.expected_code)
        elif a.route=='secondary':native_phase('secondary',a.expected_version,a.expected_code)
        elif a.route=='transitions':native_phase('transitions',a.expected_version,a.expected_code)
        elif a.route=='language-regression':native_phase('language-regression',a.expected_version,a.expected_code)
        elif a.route=='root-regression':native_phase('root-regression',a.expected_version,a.expected_code)
        after=shell('logcat','-d','-b','crash');delta=after[len(before):] if after.startswith(before) else after
        if delta.strip():(out/'crash-delta.txt').write_text(delta)
        assert not delta.strip(),'New native crash entries'
        report['passed']=True
    except BaseException as failure:
        error=failure;report['error']=str(failure);report['traceback']=traceback.format_exc()
    finally:
        shell('am','force-stop',package)
        if observer_installed:
            cleanup=call('uninstall',TEST_PACKAGE);(out/'test-uninstall.txt').write_bytes(cleanup)
        try:
            if font_changed:
                if original_font_scale=='null':shell('settings','delete','system','font_scale')
                else:shell('settings','put','system','font_scale',original_font_scale)
            report['font_scale_after']=shell('settings','get','system','font_scale').strip()
            report['font_scale_restored']=report['font_scale_after']==original_font_scale
            if display_changed:
                for kind in ['density','size']:
                    match=re.search(r'Override '+kind+r':\s*([0-9]+(?:x[0-9]+)?)',original_display[kind])
                    shell('wm',kind,match.group(1) if match else 'reset')
            report['display_after']={'size':shell('wm','size').strip(),'density':shell('wm','density').strip()}
            report['display_restored']=report['display_after']==original_display
        except BaseException as failure:
            report['display_restored']=False;report['display_restore_error']=str(failure)
        report['isolated_test_package_absent']=not query(TEST_PACKAGE)
        actual=query(package);report['candidate_sha256_after']=shell('sha256sum',actual).split()[0] if actual else None
        report['exact_candidate_retained']=report['candidate_sha256_after']==a.sha256
        report['owned_qa_profile_retained']=True
        report['elapsed_seconds']=round(time.monotonic()-started,3)
        report['passed']=report['passed'] and report['isolated_test_package_absent'] and report['exact_candidate_retained'] and report['display_restored'] and report.get('font_scale_restored',False)
        (out/'host-report.json').write_text(json.dumps(report,indent=2,ensure_ascii=False)+'\n')
    if error:raise error
    assert report['passed'],'Final cleanup/candidate acceptance failed'
    print(json.dumps({'passed':True,'checks':sum(x['native']['checkCount'] for x in report['phases']),'screenshots':len(report['screenshots']),'report':str(out/'host-report.json')},indent=2))

if __name__=='__main__':main()
