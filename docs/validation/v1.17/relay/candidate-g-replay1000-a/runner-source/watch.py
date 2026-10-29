#!/usr/bin/env python3
"""Track one detached exact-candidate replay, verify all1000, optionally stop its AVD.

No device action occurs before complete native PASS and independent aggregation.
--stop-after-pass requires the owner's explicit authorization for normal5586 stop.
"""
from pathlib import Path
import argparse,hashlib,json,os,re,subprocess,sys,tempfile,time,traceback

ROOT=Path(__file__).resolve().parents[2]
HERE=Path(__file__).resolve().parent

def sha(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()
def read_json(path):
    try:return json.loads(Path(path).read_text())
    except (FileNotFoundError,json.JSONDecodeError):return None

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--launch-record',required=True,type=Path)
    parser.add_argument('--source-freeze',required=True,type=Path)
    parser.add_argument('--summary',type=Path,default=ROOT/'docs/validation/v1.17/relay/summary.json')
    parser.add_argument('--stop-after-pass',action='store_true')
    parser.add_argument('--expected-restored-language',default='fr',choices=['en','fr','de','es','pt','it'])
    args=parser.parse_args()
    launch=read_json(args.launch_record);assert launch and launch['label'] and launch['apk_sha256']
    assert re.fullmatch(r'1\.17\.0-rc[1-9][0-9]*',launch['version']) and launch['code']>0
    folder=args.launch_record.resolve().parent;pointer=folder/'active-handoff.json';report=ROOT/launch['expected_report'];console=ROOT/launch['console'];summary=args.summary.resolve()
    state=read_json(pointer);assert state['label']==launch['label'] and state['apk_sha256']==launch['apk_sha256']
    summarizer=HERE/'summarize.py';summarizer_hash=sha(summarizer);frozen=json.loads(args.source_freeze.read_text());assert all(sha(ROOT/p)==digest for p,digest in frozen.items())
    assert not summary.exists(),'Preserve any prior summary; inspect before starting another accepted run'
    def update(**values):
        state.update(values,watcher_pid=os.getpid(),progress_observed_unix=time.time())
        temporary=pointer.with_suffix('.watch-tmp');temporary.write_text(json.dumps(state,indent=2)+'\n');temporary.replace(pointer)
    update(status='RUNNING; only actual completed native levels count',watcher_command=[sys.executable,__file__]+sys.argv[1:],source_freeze=str(args.source_freeze.resolve()),source_freeze_sha256=sha(args.source_freeze),watcher_source_sha256=sha(__file__))
    deadline=time.monotonic()+18500
    while time.monotonic()<deadline:
        text=console.read_text() if console.exists() else ''
        rows=re.findall(r'PASS real U(\d+); completed=(\d+); elapsed_ms=(\d+)',text)
        if rows:
            number,completed,elapsed=map(int,rows[-1]);update(last_observed_level=number,native_levels_completed=completed,observed_native_elapsed_ms=elapsed)
        result=read_json(report)
        if result is None:time.sleep(10);continue
        assert result.get('passed'),'Native replay did not pass; preserve its partial/failed evidence'
        assert result['apk_sha256']==launch['apk_sha256'] and result['expected_version']==launch['version'] and result['expected_code']==launch['code']
        assert result['native']['completed']==1000 and sha(summarizer)==summarizer_hash
        command=[sys.executable,str(summarizer),'--report',str(report),'--output',str(summary)]
        log=folder/(launch['label']+'-summary-verify.txt')
        with log.open('xb') as output:verified=subprocess.run(command,cwd=ROOT,stdout=output,stderr=subprocess.STDOUT)
        assert verified.returncode==0,'Independent full1000 aggregation failed; inspect verification log'
        final=read_json(summary);assert final['passed'] and final['levels']==1000 and final['candidate_apk_sha256']==launch['apk_sha256'] and final['version']==launch['version'] and final['code']==launch['code'] and final['new_first_win_coins']==0
        update(status='PASS full1000 native + independent aggregation; global publication separate',replay_process_finished=True,summary=str(summary.relative_to(ROOT)),summary_sha256=sha(summary),native_checks=final['native_checks'],actual_touch_gestures=final['actual_touch_gestures'],native_original_pngs=final['native_original_pngs'],new_first_win_coins=0,active_process_id=None)
        if args.stop_after_pass:finish_device(args,launch,result,report,final,frozen,folder,update)
        update(watcher_finished=True);print(json.dumps(state),flush=True);return
    raise TimeoutError('Watcher deadline reached; native process is not automatically interrupted')

def finish_device(args,launch,result,report,summary,frozen,folder,update):
    native=report.parent/'native'
    for relative,digest in result['evidence_sha256'].items():assert sha(report.parent/relative)==digest
    before=(native/'protected-before.json').read_bytes();after=(native/'protected-after.json').read_bytes();assert before==after
    saved=json.loads(after);assert all(saved['stars_'+str(number)]==3 for number in range(1,1001))
    checks=[json.loads(line) for line in (native/'checks.jsonl').read_text().splitlines() if line]
    assert any(c['passed'] and c['message']=='Original language durably restored through visible UI' for c in checks)
    events=[json.loads(line) for line in (native/'events.jsonl').read_text().splitlines() if line]
    language=args.expected_restored_language;label={'en':'English','fr':'Français','de':'Deutsch','es':'Español','pt':'Português','it':'Italiano'}[language]
    assert any(e.get('reason')=='Restore original language '+language and e.get('label')==label for e in events)
    assert all(sha(ROOT/path)==digest for path,digest in frozen.items()),'App inputs changed during replay'
    assert result['installed_apk_sha256_after']==result['installed_apk_sha256']==launch['apk_sha256'] and result['observer_removed']
    adb=str(Path.home()/'Library/Android/sdk/platform-tools/adb')
    def call(*parts):return subprocess.check_output([adb,'-s','emulator-5586',*parts],stderr=subprocess.STDOUT,timeout=90)
    assert call('emu','avd','name').decode().splitlines()[0].strip()=='Luma-Refonte115-QA'
    assert call('shell','getprop','ro.kernel.qemu').decode().strip()=='1'
    installed=call('shell','pm','path','com.androidhit.lumadrift').decode().splitlines();assert len(installed)==1 and installed[0].startswith('package:/data/app/')
    with tempfile.TemporaryDirectory(prefix='luma117-final-before-stop-') as temp:
        apk=Path(temp)/'installed.apk';call('pull',installed[0][8:],str(apk));assert sha(apk)==launch['apk_sha256']
    assert not call('shell','pm','path','com.androidhit.lumadrift.relaytest.v117').strip(),'Do not remove an unexpected observer'
    ini=Path.home()/'.android/avd/Luma-Refonte115-QA.ini';settings=dict(row.split('=',1) for row in ini.read_text().splitlines() if '=' in row);userdata=Path(settings['path']);assert userdata.is_dir()
    (folder/(launch['label']+'-normal-stop.txt')).write_bytes(call('emu','kill'))
    deadline=time.monotonic()+60
    while time.monotonic()<deadline and 'emulator-5586' in subprocess.check_output([adb,'devices']).decode():time.sleep(.5)
    assert 'emulator-5586' not in subprocess.check_output([adb,'devices']).decode() and userdata.is_dir() and any(userdata.glob('userdata*'))
    assert all(sha(ROOT/path)==digest for path,digest in frozen.items())
    receipt={'passed':True,'candidate':launch['candidate'],'version':launch['version'],'code':launch['code'],'apk_sha256':launch['apk_sha256'],'serial':'emulator-5586','avd':'Luma-Refonte115-QA','full1000':True,'summary_sha256':sha(args.summary),'host_report_sha256':sha(report),'protected_state_byte_identical':True,'protected_before_sha256':hashlib.sha256(before).hexdigest(),'protected_after_sha256':hashlib.sha256(after).hexdigest(),'language_restored':language,'coins_retained':saved['coins'],'settings_retained':{k:saved.get(k) for k in ['sound','music','haptics','_session_reduce_motion']},'source_inputs_stable':True,'source_input_count':len(frozen),'source_freeze_sha256':sha(args.source_freeze),'installed_candidate_verified_before_stop':True,'own_observer_removed_by_wrapper':True,'stopped_normally':True,'userdata_preserved':True,'userdata_directory':str(userdata),'other_devices_untouched':True,'verified_unix':time.time()}
    path=folder/(launch['label']+'-shutdown.json');assert not path.exists();path.write_text(json.dumps(receipt,indent=2)+'\n')
    update(status='PASS full1000 + aggregate + restored profile/settings/source checks;5586 stopped normally with userdata retained',shutdown_receipt=str(path.relative_to(ROOT)),shutdown_receipt_sha256=sha(path),stopped_normally=True,userdata_preserved=True)

if __name__=='__main__':
    try:main()
    except Exception:
        # Native process is never terminated by this observer of its evidence.
        failure=traceback.format_exc()
        try:
            index=sys.argv.index('--launch-record');record=Path(sys.argv[index+1]);launch=read_json(record);pointer=record.resolve().parent/'active-handoff.json';state=read_json(pointer)
            if launch and state and state.get('label')==launch.get('label'):
                state.update(status='Watcher failed; inspect actual report/log, native process not automatically interrupted',watcher_failure=failure,watcher_finished=True)
                temporary=pointer.with_suffix('.watch-failed');temporary.write_text(json.dumps(state,indent=2)+'\n');temporary.replace(pointer)
        except Exception:pass
        print(failure,file=sys.stderr,flush=True);raise
