#!/usr/bin/env python3
"""Observe the exact frozen 1.15 candidate on the explicitly owned QA AVD only."""
from pathlib import Path
import argparse,hashlib,json,os,re,select,subprocess,tempfile,time,traceback

ROOT=Path(__file__).resolve().parents[2]
HERE=Path(__file__).resolve().parent
TEST_PACKAGE='com.androidhit.lumadrift.relaytest'
RUNNER=TEST_PACKAGE+'/com.androidhit.lumadrift.qa.v115relay.RelayInstrumentation'
def sha(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--serial',required=True)
    parser.add_argument('--expected-avd',default='Luma-Refonte115-Showcase-QA')
    parser.add_argument('--expected-apk',required=True)
    parser.add_argument('--sha256',required=True)
    parser.add_argument('--test-apk',required=True)
    parser.add_argument('--variant',choices=['production','lab'],default='production')
    parser.add_argument('--expected-version',required=True)
    parser.add_argument('--expected-code',type=int,default=17)
    parser.add_argument('--label',required=True)
    parser.add_argument('--from',dest='first',type=int,default=1)
    parser.add_argument('--through',type=int,default=100)
    parser.add_argument('--install-if-absent',action='store_true')
    parser.add_argument('--timeout',type=int,default=18000)
    args=parser.parse_args()
    assert re.fullmatch(r'emulator-[0-9]+',args.serial),'Explicit emulator serial required'
    assert args.expected_avd=='Luma-Refonte115-Showcase-QA','Only the newly owned task QA AVD is authorized'
    assert re.fullmatch(r'[A-Za-z0-9_-]{1,50}',args.label),'Safe unique evidence label required'
    assert 1<=args.first<=args.through<=1000,'Journey range1..1000'
    apk=Path(args.expected_apk).resolve();observer=Path(args.test_apk).resolve()
    assert apk.is_file() and re.fullmatch(r'[a-f0-9]{64}',args.sha256) and sha(apk)==args.sha256,'Exact frozen candidate SHA-256 required'
    assert observer.is_file(),'Separate signed release observer APK required'
    fixture_manifest=json.loads((HERE/'assets/fixtures/manifest.json').read_text())
    assert all(sha(ROOT/path)==digest for path,digest in fixture_manifest['source_sha256'].items()),'Frozen fixture host sources have changed; preserve and explicitly regenerate fixtures before qualification'
    adb=str(Path(os.environ.get('ANDROID_HOME',str(Path.home()/'Library/Android/sdk')))/'platform-tools/adb')
    package='com.androidhit.lumadrift'+('.sandbox' if args.variant=='lab' else '')
    def call(*parts,timeout=90):return subprocess.check_output([adb,'-s',args.serial,*parts],stderr=subprocess.STDOUT,timeout=timeout)
    def shell(*parts):return call('shell',*parts).decode(errors='replace')
    def installed(pkg):
        proc=subprocess.run([adb,'-s',args.serial,'shell','pm','path',pkg],capture_output=True,text=True,timeout=60)
        assert proc.returncode in (0,1),'Package query failed'
        lines=[line[8:] for line in proc.stdout.splitlines() if line.startswith('package:')]
        assert len(lines)<=1,'Split installation is outside exact single-APK scope'
        return lines[0] if lines else None
    assert shell('getprop','ro.kernel.qemu').strip()=='1','Emulator-only observer'
    assert call('emu','avd','name').decode().splitlines()[0].strip()==args.expected_avd,'Exact owned QA AVD required'
    out=ROOT/'docs/validation/v1.15'/('relay-native-'+args.label)
    assert not out.exists(),'Evidence directory must be new'
    out.mkdir(parents=True)
    report={'passed':False,'package':package,'serial':args.serial,'avd':args.expected_avd,'from':args.first,'through':args.through,'apk_sha256':args.sha256,'test_apk_sha256':sha(observer),'expected_version':args.expected_version,'expected_code':args.expected_code,'wrapper_sha256':sha(__file__),'runner_sha256':sha(HERE/'java/com/androidhit/lumadrift/qa/v115relay/RelayInstrumentation.java'),'fixture_manifest_sha256':sha(HERE/'assets/fixtures/manifest.json'),'scope':'Exact installed APK on newly owned QA AVD. Actual touchscreen certificates and Next, read-only target preferences, no synthetic progression/history/coins, no existing user device. QA profile retained for genuine subsequent batches.'}
    started=time.monotonic()
    try:
        candidate_path=installed(package)
        if not candidate_path:
            assert args.install_if_absent,'Candidate absent; explicit --install-if-absent required'
            (out/'candidate-install.txt').write_bytes(call('install',str(apk),timeout=180))
            candidate_path=installed(package)
        assert candidate_path,'Candidate installation not found'
        with tempfile.TemporaryDirectory(prefix='luma-u-installed-') as temp:
            copied=Path(temp)/'candidate.apk';call('pull',candidate_path,str(copied));report['installed_apk_sha256']=sha(copied);assert report['installed_apk_sha256']==args.sha256,'Installed candidate bytes differ from exact approved APK'
            previous=installed(TEST_PACKAGE)
            if previous:
                copied_test=Path(temp)/'observer.apk';call('pull',previous,str(copied_test));assert sha(copied_test)==sha(observer),'A different observer is already installed; preserve its ownership before replacement'
            else:(out/'observer-install.txt').write_bytes(call('install','-t',str(observer),timeout=180))
        report['device']={'sdk':shell('getprop','ro.build.version.sdk').strip(),'abi':shell('getprop','ro.product.cpu.abi').strip(),'display':shell('wm','size').strip(),'density':shell('wm','density').strip()}
        command=[adb,'-s',args.serial,'shell','am','instrument','-w','-r','-e','expectedVersion',args.expected_version,'-e','expectedCode',str(args.expected_code),'-e','label',args.label,'-e','from',str(args.first),'-e','through',str(args.through),RUNNER]
        report['instrumentation_arguments']=command[5:]
        with (out/'instrumentation.txt').open('wb') as log:
            process=subprocess.Popen(command,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
            deadline=time.monotonic()+args.timeout
            while process.poll() is None:
                if time.monotonic()>deadline:
                    process.terminate();raise TimeoutError('Observer host timeout; completed levels remain only those recorded in native progress.json')
                ready,_,_=select.select([process.stdout],[],[],1)
                if ready:
                    chunk=os.read(process.stdout.fileno(),16384)
                    if chunk:
                        log.write(chunk);log.flush()
                        for line in chunk.decode(errors='replace').splitlines():
                            if 'PASS real U' in line or 'FAIL' in line or 'INSTRUMENTATION_RESULT' in line:print(line,flush=True)
            tail=process.stdout.read()
            if tail:log.write(tail)
            report['instrumentation_exit_code']=process.returncode
        remote='/sdcard/Android/data/'+package+'/files/v115-relay-native/'+args.label
        call('pull',remote,str(out/'native'),timeout=180)
        native=json.loads((out/'native/report.json').read_text())
        assert sha(out/'native/fixture-manifest.json')==sha(HERE/'assets/fixtures/manifest.json'),'Packaged observer fixture manifest differs from frozen host inventory'
        report['native']=native;report['passed']=bool(native.get('passed')) and native.get('completed')==args.through-args.first+1 and process.returncode==0
        levels=native.get('levels',[])
        if levels:
            elapsed=sum(item['elapsedMs'] for item in levels);actions=sum(item['certificateExits']+item.get('extraProbeExits',0) for item in levels)
            report['timing']={'observed_levels':len(levels),'observed_level_loop_ms':elapsed,'observed_exits_including_undo_probes':actions,'average_ms_per_observed_exit_including_capture_and_next_overhead':round(elapsed/max(1,actions),2),'note':'Observed emulator execution time with screenshots and SDK checks, not human completion time; no unplayed level is counted as tested.'}
            if args.through<1000:
                remaining=sum(len(json.loads((HERE/('assets/fixtures/u'+str(n)+'.json')).read_text())['steps']) for n in range(args.through+1,1001))
                report['timing']['unplayed_certificate_exits']=remaining
                report['timing']['projection_seconds_if_observed_rate_persisted']=round(elapsed/max(1,actions)*remaining/1000,1)
    except BaseException as failure:
        report['failure']=str(failure);(out/'host-failure.txt').write_text(traceback.format_exc())
        try:call('pull','/sdcard/Android/data/'+package+'/files/v115-relay-native/'+args.label,str(out/'native'),timeout=180)
        except BaseException:pass
    finally:
        report['wall_seconds']=round(time.monotonic()-started,3)
        report['evidence_sha256']={str(path.relative_to(out)):sha(path) for path in sorted(out.rglob('*')) if path.is_file() and path.name!='host-report.json'}
        (out/'host-report.json').write_text(json.dumps(report,indent=2,ensure_ascii=False)+'\n')
        print(json.dumps({'passed':report['passed'],'report':str(out/'host-report.json'),'wall_seconds':report['wall_seconds']}),flush=True)
    raise SystemExit(0 if report['passed'] else 1)
if __name__=='__main__':main()
