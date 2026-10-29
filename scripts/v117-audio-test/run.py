#!/usr/bin/env python3
"""Exact-candidate digital audio UI observation on the released task-owned AVD only."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import tempfile
import time
import traceback
import uuid

ROOT=Path(__file__).resolve().parents[2]
PACKAGE='com.androidhit.lumadrift'
OBSERVER='com.androidhit.lumadrift.audio117test'
RUNNER=OBSERVER+'/com.androidhit.lumadrift.qa.v117audio.AudioInstrumentation'

def sha(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--serial',required=True,choices=['emulator-5586'])
    parser.add_argument('--expected-avd',required=True,choices=['Luma-Refonte115-QA'])
    parser.add_argument('--expected-apk',required=True)
    parser.add_argument('--sha256',required=True)
    parser.add_argument('--test-apk',required=True)
    parser.add_argument('--expected-version',required=True)
    parser.add_argument('--expected-code',type=int,required=True)
    parser.add_argument('--label',required=True)
    parser.add_argument('--recover-language',help='Explicit visible language label for UI-only recovery after a documented prior observer timeout')
    parser.add_argument('--recover-music',choices=['on','off'])
    parser.add_argument('--recover-effects',choices=['on','off'])
    parser.add_argument('--recover-only',action='store_true',help='Perform only separately reported documented UI recovery; never claim five-context audio coverage')
    parser.add_argument('--protected-origin',type=Path,help='Read-only original protected-before snapshot from a preserved failed audio attempt; never written to the app')
    parser.add_argument('--protected-origin-sha256',help='Explicit digest of the original protected snapshot; both options required together')
    parser.add_argument('--device-handoff-confirmed',action='store_true',required=True,
                        help='Acknowledge the asset_pipeline117 owner has explicitly released this exact AVD; this flag does not supply authorization')
    parser.add_argument('--host-audio-unavailable',action='store_true',required=True,
                        help='Record known no-host-audio configuration; this run never claims physical listening')
    args=parser.parse_args()
    recovery=[args.recover_language,args.recover_music,args.recover_effects]
    if any(recovery) and not all(recovery):parser.error('Supply all three documented UI recovery values together')
    if args.recover_only and not (all(recovery) and args.protected_origin):parser.error('Recovery-only requires all documented visible values and the original protected-state checkpoint')
    assert bool(args.protected_origin)==bool(args.protected_origin_sha256),'Both protected origin path and digest are required together'
    if args.protected_origin:assert args.protected_origin.is_file() and sha(args.protected_origin)==args.protected_origin_sha256,'Original protected-state checkpoint digest differs'
    if not re.fullmatch('[a-f0-9]{64}',args.sha256):parser.error('Exact frozen production APK SHA-256 required')
    if not re.fullmatch('[A-Za-z0-9_-]{1,55}',args.label):parser.error('Safe new evidence label required')
    apk=Path(args.expected_apk).resolve();observer=Path(args.test_apk).resolve()
    assert apk.is_file() and sha(apk)==args.sha256,'Frozen production candidate hash mismatch'
    assert observer.is_file(),'Separately built signed observer APK required'
    sdk=Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'))
    adb=str(sdk/'platform-tools/adb')
    def call(*parts,timeout=60):
        return subprocess.check_output([adb,'-s',args.serial,*parts],stderr=subprocess.STDOUT,timeout=timeout)
    def shell(*parts):return call('shell',*parts).decode(errors='replace')
    def package_path(package):
        query=subprocess.run([adb,'-s',args.serial,'shell','pm','path',package],capture_output=True,text=True,timeout=30)
        assert query.returncode in [0,1],'Package path query failed'
        value=query.stdout.strip()
        assert not value or value.startswith('package:/data/app/'),'Unexpected package path'
        assert '\n' not in value,'Split package candidate unsupported by this exact-APK observer'
        return value.removeprefix('package:')
    assert shell('getprop','ro.kernel.qemu').strip()=='1','Only Android emulator allowed'
    assert call('emu','avd','name').decode().splitlines()[0].strip()==args.expected_avd,'AVD identity differs from released task device'
    installed=package_path(PACKAGE);assert installed,'Final production candidate must already be installed; observer never installs/replaces it'
    assert shell('sha256sum',installed).split()[0]==args.sha256,'Installed APK differs from exact frozen final candidate'
    details=shell('dumpsys','package',PACKAGE)
    assert 'versionName='+args.expected_version in details and 'versionCode='+str(args.expected_code)+' ' in details,'Installed candidate version/code differ'
    assert not package_path(OBSERVER),'Audio observer must initially be absent'
    out=ROOT/'docs/validation/v1.17/audio/native'/args.label
    shots=ROOT/'docs/screenshots/v1.17/audio'/args.label
    assert not out.exists() and not shots.exists(),'Evidence must be a new unique run; failures are never overwritten'
    out.mkdir(parents=True);shots.mkdir(parents=True)
    frozen=out/'audio-observer.apk';shutil.copyfile(observer,frozen);observer=frozen
    sources={}
    for source in sorted((ROOT/'scripts/v117-audio-test').rglob('*')):
        if not source.is_file() or '__pycache__' in source.parts:continue
        rel=source.relative_to(ROOT/'scripts/v117-audio-test');target=out/'runner-source'/rel
        target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(source,target);sources[str(rel)]=sha(target)
    (out/'source-snapshot.json').write_text(json.dumps(sources,indent=2)+'\n')
    if args.protected_origin:shutil.copyfile(args.protected_origin,out/'protected-origin.json')
    run_token=str(uuid.uuid4())
    report={'passed':False,'serial':args.serial,'avd':args.expected_avd,'package':PACKAGE,'apk_sha256':args.sha256,'run_token':run_token,
            'observer_apk_sha256':sha(observer),'expected_version':args.expected_version,'expected_code':args.expected_code,
            'host_audio_output_available':False,'physical_listening_performed':False,
            'prior_failed_observer_ui_recovery':({'language':args.recover_language,'music':args.recover_music,'effects':args.recover_effects} if all(recovery) else None),
            'protected_origin_sha256':args.protected_origin_sha256,
            'recovery_only':args.recover_only,
            'scope':'Actual UI input, all five native audio contexts including an already earned U1 opened without playing, independent music/effects settings and Android HOME/resume from that puzzle. Complete game/meta and non-active-puzzle session preferences are compared read-only; naturally changed active sessions are recorded, never injected. No target hooks, preference writes, purchases, puzzle moves, provider actions, app installation/replacement, display changes or other device. No physical speaker/headphone audition or external audio-focus contender.',
            'device':{'sdk':shell('getprop','ro.build.version.sdk').strip(),'abi':shell('getprop','ro.product.cpu.abi').strip(),'size':shell('wm','size').strip(),'density':shell('wm','density').strip()},'screenshots':{}}
    if args.recover_only:report['scope']='Separate documented UI-only recovery to original language/music/effects and home, independently compared to the original pre-failure full protected snapshot. No five-context playback claim from this phase. No preference writes, state injection, spending or puzzle action.'
    installed_observer=False;error=None;started=time.monotonic()
    crash_before=shell('logcat','-d','-b','crash')
    try:
        result=call('install',str(observer));(out/'observer-install.txt').write_bytes(result)
        assert b'Success' in result;installed_observer=True
        assert shell('sha256sum',package_path(OBSERVER)).split()[0]==sha(observer),'Installed observer hash differs'
        shell('am','force-stop',PACKAGE)
        instrument=['shell','am','instrument','-w','-r','-e','expectedVersion',args.expected_version,'-e','expectedCode',str(args.expected_code),'-e','runToken',run_token,'-e','label',args.label]
        if all(recovery):instrument+=['-e','recoveryLanguage',args.recover_language,'-e','recoveryMusic',str(args.recover_music=='on').lower(),'-e','recoveryEffects',str(args.recover_effects=='on').lower()]
        if args.recover_only:instrument+=['-e','recoveryOnly','true']
        instrument.append(RUNNER)
        result=call(*instrument,timeout=240)
        (out/'instrumentation.txt').write_bytes(result)
        instrumentation_ok=bool(re.search(rb'(?m)^INSTRUMENTATION_CODE: -1\s*$',result)) and b'INSTRUMENTATION_FAILED' not in result
        remote='/sdcard/Android/data/'+PACKAGE+'/files/v117-digital-audio/'+args.label
        with tempfile.TemporaryDirectory(prefix='luma-digital-audio117-') as temporary:
            call('pull',remote,temporary);pulled=Path(temporary)/args.label
            assert (pulled/'report.json').is_file(),'Fresh native report absent; instrumentation output retained'
            native=json.loads((pulled/'report.json').read_text());report['native']=native
            assert native.get('runToken')==run_token and native.get('label')==args.label,'Native report is not from this exact token/label'
            expected=set(native.get('captures',[]))
            for file in pulled.iterdir():
                if not file.is_file():continue
                if file.suffix=='.png':
                    if file.stem not in expected:continue
                    shutil.copyfile(file,shots/file.name);report['screenshots'][file.name]=sha(file)
                elif file.suffix in ['.json','.txt']:shutil.copyfile(file,out/file.name)
        assert instrumentation_ok,'Native instrumentation did not finish with RESULT_OK; fresh failure evidence retained'
        assert report['native']['passed'] and report['native']['settingsRestored'] and report['native']['protectedStatePreserved'],'Native digital audio observation or complete-state restoration failed'
        assert report['native'].get('recoveryOnly')==args.recover_only,'Native route does not match the requested recovery/clean phase'
        if not args.recover_only:assert {item['scene'] for item in report['native']['observations'] if 'scene' in item}=={'ADVENTURE','HOME','COLLECTION','ATELIER','PUZZLE'},'All five actual native contexts must be observed'
        assert (out/'protected-before.json').read_bytes()==(out/'protected-after.json').read_bytes(),'Independent complete protected-state byte comparison failed'
        report['protected_state_byte_identical']=True
        report['protected_before_sha256']=sha(out/'protected-before.json');report['protected_after_sha256']=sha(out/'protected-after.json')
        if args.protected_origin:
            assert (out/'protected-origin.json').read_bytes()==(out/'protected-after.json').read_bytes(),'Current result differs from original pre-failure complete protected state'
            report['original_pre_failure_protected_state_byte_identical']=True
        crash_after=shell('logcat','-d','-b','crash');delta=crash_after[len(crash_before):] if crash_after.startswith(crash_before) else crash_after
        (out/'crash-delta.txt').write_text(delta);assert not delta.strip(),'New Android crash entries'
        report['passed']=True
    except BaseException as failure:
        error=failure;report['error']=str(failure);report['traceback']=traceback.format_exc()
    finally:
        shell('am','force-stop',PACKAGE)
        if installed_observer:
            cleanup=call('uninstall',OBSERVER);(out/'observer-uninstall.txt').write_bytes(cleanup)
        report['observer_removed']=not package_path(OBSERVER)
        final=package_path(PACKAGE);report['installed_candidate_sha256_after']=shell('sha256sum',final).split()[0] if final else None
        report['exact_candidate_retained']=report['installed_candidate_sha256_after']==args.sha256
        report['wall_seconds']=round(time.monotonic()-started,3)
        report['passed']=report['passed'] and report['observer_removed'] and report['exact_candidate_retained']
        report['evidence_sha256']={str(file.relative_to(out)):sha(file) for file in sorted(out.rglob('*')) if file.is_file() and file.name!='host-report.json'}
        (out/'host-report.json').write_text(json.dumps(report,indent=2,ensure_ascii=False)+'\n')
    if error:raise error
    assert report['passed'],'Cleanup or candidate-integrity check failed'
    print(json.dumps({'passed':True,'checks':report['native']['checkCount'],'captures':len(report['screenshots']),'physical_listening':False,'report':str(out/'host-report.json')},indent=2))

if __name__=='__main__':main()
