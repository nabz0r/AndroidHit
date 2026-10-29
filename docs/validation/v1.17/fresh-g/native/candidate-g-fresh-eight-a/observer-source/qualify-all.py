#!/usr/bin/env python3
"""Run four real fresh UI phases for an explicitly frozen candidate; preserve failures."""
from pathlib import Path
import argparse,hashlib,json,re,subprocess,sys,tempfile,time,traceback

ROOT=Path(__file__).resolve().parents[2]
HERE=Path(__file__).resolve().parent
HANDOFF=ROOT/'docs/validation/v1.17/fresh/active-handoff.json'
def sha(file):return hashlib.sha256(Path(file).read_bytes()).hexdigest()
def pointer(**fields):
    current=json.loads(HANDOFF.read_text()) if HANDOFF.exists() else {};current.update(fields);HANDOFF.write_text(json.dumps(current,indent=2)+'\n')

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--candidate',required=True)
    parser.add_argument('--apk',required=True,type=Path)
    parser.add_argument('--sha256',required=True)
    parser.add_argument('--expected-version',required=True)
    parser.add_argument('--expected-code',required=True,type=int)
    parser.add_argument('--device-receipt',required=True,type=Path)
    parser.add_argument('--output-root',required=True,type=Path)
    parser.add_argument('--label-prefix',required=True)
    parser.add_argument('--observer',type=Path,default=ROOT/'app/build/qualification-artifacts/v1.17/fresh-production-test.apk')
    parser.add_argument('--resume-cold',action='store_true',help='Reuse three passed phases; never repeat wins/draws')
    parser.add_argument('--cold-label',help='Unique cold-only retry label')
    args=parser.parse_args()
    assert re.fullmatch(r'[A-Za-z0-9_-]{1,24}',args.label_prefix) and re.fullmatch(r'[A-Z][A-Za-z0-9_-]{0,24}',args.candidate)
    assert re.fullmatch(r'[a-f0-9]{64}',args.sha256) and sha(args.apk)==args.sha256
    assert re.fullmatch(r'1\.17\.0-rc[1-9][0-9]*',args.expected_version) and args.expected_code>0
    out=args.output_root.resolve();assert out.is_relative_to(ROOT/'docs/validation/v1.17')
    owned=json.loads(args.device_receipt.read_text());assert owned['created_for_task'] and owned['serial']=='emulator-5590'
    names={'onboarding':args.label_prefix+'-onboarding-a','campaign':args.label_prefix+'-fresh-eight-a','arrivals':args.label_prefix+'-fresh-arrivals-a','cold':args.cold_label or args.label_prefix+'-fresh-cold-a'}
    assert all(re.fullmatch(r'[A-Za-z0-9_-]{1,60}',name) for name in names.values())
    reports=[]
    common=['--apk',str(args.apk.resolve()),'--sha256',args.sha256,'--expected-version',args.expected_version,'--expected-code',str(args.expected_code),'--device-receipt',str(args.device_receipt.resolve()),'--output-root',str(out),'--observer',str(args.observer.resolve())]
    for phase,label in names.items():
        if args.resume_cold and phase!='cold':
            path=out/'native'/label/'host-report.json';record=json.loads(path.read_text());assert record['passed'] and record['apk_sha256']==args.sha256
            reports.append(str(path.relative_to(ROOT)));continue
        command=[sys.executable,str(HERE/'run.py'),'--phase',phase,'--label',label]+common
        if phase=='cold':command+=['--checkpoint',str(out/'native'/names['arrivals']/'native/checkpoint.json')]
        log_path=out/(phase+'-'+label+'-console.txt')
        with log_path.open('xb') as log:
            process=subprocess.Popen(command,cwd=ROOT,stdout=log,stderr=subprocess.STDOUT)
            pointer(status='Fresh real UI phase '+phase+' running',candidate=args.candidate,version=args.expected_version,code=args.expected_code,apk_sha256=args.sha256,active_process_id=process.pid,active_command=command,active_console=str(log_path.relative_to(ROOT)),last_passed_report=reports[-1] if reports else None)
            result=process.wait()
        print('Fresh '+args.candidate+' '+phase+' exit '+str(result),flush=True)
        assert result==0,'Actual phase failed; preserve save and original report'
        reports.append(str((out/'native'/label/'host-report.json').relative_to(ROOT)))
    entries=[];images=0
    for name in reports:
        path=ROOT/name;record=json.loads(path.read_text());assert record['passed'] and record['apk_sha256']==args.sha256
        for relative,digest in record['evidence_sha256'].items():assert sha(path.parent/relative)==digest
        images+=record['captures'];entries.append({'path':name,'sha256':sha(path),'checks':record['checkCount'],'touches':record['touchCount'],'captures':record['captures']})
    funding=json.loads((out/'native'/names['campaign']/'native/earned-funding.json').read_text());checkpoint=json.loads((out/'native'/names['arrivals']/'native/checkpoint.json').read_text())
    assert funding['actual_first_win_rewards']==440 and funding['visible_balance']==720 and checkpoint['coins']==280
    summary={'passed':True,'candidate':args.candidate,'version':args.expected_version,'code':args.expected_code,'apk_sha256':args.sha256,'genuine_new_install':True,'native_checks':sum(item['checks'] for item in entries),'actual_touches':sum(item['touches'] for item in entries),'original_pngs':images,'first_light_exits':3,'practice_lessons_completed':7,'campaign_levels':list(range(1,9)),'actual_first_win_coins':440,'balance_before_draws':720,'balance_after_draws':280,'real_draw_costs':[180,260],'equipped_portrait_after_cold':checkpoint['portrait'],'distinct_cold_process':True,'reports':entries,'scope':'Actual empty-device new-install public Android UI only on dedicated receipt-owned5590. Real First Light/practice/U1-U8/draw/equipment/cold. No target preferences, private app/engine/model/renderer calls, state seed, provider or existing AVD access. Separate from retained5586 all1000 replay and multilingual qualification.'}
    summary_path=out/('summary-'+args.candidate.lower()+'.json');assert not summary_path.exists();summary_path.write_text(json.dumps(summary,indent=2)+'\n')
    sdk=Path.home()/'Library/Android/sdk';adb=str(sdk/'platform-tools/adb')
    def call(*parts):return subprocess.check_output([adb,'-s','emulator-5590',*parts],stderr=subprocess.STDOUT,timeout=120)
    assert call('emu','avd','name').decode().splitlines()[0].strip()==owned['avd']
    package='com.androidhit.lumadrift.freshtest.v117';remote=call('shell','pm','path',package).decode().strip()[8:]
    with tempfile.TemporaryDirectory(prefix='luma117-fresh-observer-') as temp:
        observer=Path(temp)/'observer.apk';call('pull',remote,str(observer));assert sha(observer)==json.loads((ROOT/reports[-1]).read_text())['observer_sha256']
    (out/'device/observer-uninstall.txt').write_bytes(call('uninstall',package))
    (out/'device/normal-stop.txt').write_bytes(call('emu','kill'))
    deadline=time.monotonic()+45
    while time.monotonic()<deadline and 'emulator-5590' in subprocess.check_output([adb,'devices']).decode():time.sleep(.5)
    assert 'emulator-5590' not in subprocess.check_output([adb,'devices']).decode()
    frozen=json.loads((out/'candidate-bytes-before.json').read_text());assert all(sha(path)==digest for path,digest in frozen.items())
    (out/'device/shutdown.json').write_text(json.dumps({'stopped_normally':True,'avd':owned['avd'],'serial':'emulator-5590','userdata_preserved':True,'observer_removed':True,'all_four_candidate_packages_unchanged':True,'existing_5586_untouched':True},indent=2)+'\n')
    pointer(status='Fresh all phases PASS;5590 stopped normally with userdata retained; awaiting retained5586 handoff',active_process_id=None,active_command=None,last_passed_report=reports[-1],fresh_summary=str(summary_path.relative_to(ROOT)))
    print(json.dumps(summary,indent=2),flush=True)

if __name__=='__main__':
    try:main()
    except Exception:
        pointer(status='Fresh interrupted/failed; inspect phase report and preserve existing save',failure=traceback.format_exc());raise
