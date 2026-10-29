#!/usr/bin/env python3
"""Execute E's authorized fresh UI phases; retain progress if interrupted, then normal stop."""
from pathlib import Path
import argparse,hashlib,json,re,subprocess,sys,tempfile,time,traceback

ROOT=Path(__file__).resolve().parents[2]
HERE=Path(__file__).resolve().parent
OUT=ROOT/'docs/validation/v1.17/fresh-e'
HANDOFF=ROOT/'docs/validation/v1.17/fresh/active-handoff.json'
SHA='48134c248b88a277d93d6745be6efd7a825f13629ea591e6b41e11cbefabf1eb'
def sha(file):return hashlib.sha256(Path(file).read_bytes()).hexdigest()
def pointer(**fields):
    current=json.loads(HANDOFF.read_text());current.update(fields);HANDOFF.write_text(json.dumps(current,indent=2)+'\n')

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--resume-cold',action='store_true',help='Reuse the three already-passed actual phases; never repeat wins or draws')
    parser.add_argument('--cold-label',default='candidate-e-fresh-cold-a')
    args=parser.parse_args();assert re.fullmatch(r'[A-Za-z0-9_-]{1,60}',args.cold_label)
    names={'onboarding':'candidate-e-onboarding-a','campaign':'candidate-e-fresh-eight-a','arrivals':'candidate-e-fresh-arrivals-a','cold':args.cold_label}
    reports=[]
    for phase,label in names.items():
        if args.resume_cold and phase!='cold':
            path=OUT/'native'/label/'host-report.json';record=json.loads(path.read_text());assert record['passed'] and record['apk_sha256']==SHA
            reports.append(str(path.relative_to(ROOT)));continue
        command=[sys.executable,str(HERE/'run.py'),'--phase',phase,'--label',label]
        if phase=='cold':command+=['--checkpoint',str(OUT/'native'/names['arrivals']/'native/checkpoint.json')]
        with (OUT/(phase+'-console.txt')).open('wb') as log:
            process=subprocess.Popen(command,cwd=ROOT,stdout=log,stderr=subprocess.STDOUT)
            pointer(status='E fresh real UI phase '+phase+' running',active_process_id=process.pid,active_command=command,last_passed_report=reports[-1] if reports else None)
            result=process.wait()
        print('Fresh E '+phase+' exit '+str(result),flush=True)
        assert result==0,'Actual phase failed; preserve current save and inspect original report'
        reports.append(str((OUT/'native'/label/'host-report.json').relative_to(ROOT)))
    entries=[];images=0
    for name in reports:
        path=ROOT/name;record=json.loads(path.read_text());assert record['passed'] and record['apk_sha256']==SHA
        for relative,digest in record['evidence_sha256'].items():assert sha(path.parent/relative)==digest
        images+=record['captures'];entries.append({'path':name,'sha256':sha(path),'checks':record['checkCount'],'touches':record['touchCount'],'captures':record['captures']})
    funding=json.loads((OUT/'native'/names['campaign']/'native/earned-funding.json').read_text());checkpoint=json.loads((OUT/'native'/names['arrivals']/'native/checkpoint.json').read_text())
    assert funding['actual_first_win_rewards']==440 and funding['visible_balance']==720 and checkpoint['coins']==280
    summary={'passed':True,'candidate':'E','version':'1.17.0-rc3','code':21,'apk_sha256':SHA,'genuine_new_install':True,'native_checks':sum(item['checks'] for item in entries),'actual_touches':sum(item['touches'] for item in entries),'original_pngs':images,'first_light_exits':3,'practice_lessons_completed':7,'campaign_levels':list(range(1,9)),'actual_first_win_coins':440,'balance_before_draws':720,'balance_after_draws':280,'real_draw_costs':[180,260],'equipped_portrait_after_cold':checkpoint['portrait'],'distinct_cold_process':True,'reports':entries,'scope':'Actual empty-device new-install public Android UI route only, on dedicated E5590. Real First Light/practice/U1-U8/draw/equipment/cold observations. No target preference access, private app/engine/model/renderer calls, state injection, seeding, real provider or existing AVD access. Does not replace retained5586 full1000 replay or six-language compact qualification.'}
    (OUT/'summary-e.json').write_text(json.dumps(summary,indent=2)+'\n')
    sdk=Path.home()/'Library/Android/sdk';adb=str(sdk/'platform-tools/adb')
    def call(*parts):return subprocess.check_output([adb,'-s','emulator-5590',*parts],stderr=subprocess.STDOUT,timeout=120)
    assert call('emu','avd','name').decode().splitlines()[0].strip()=='Luma117-Fresh-E-QA'
    package='com.androidhit.lumadrift.freshtest.v117';remote=call('shell','pm','path',package).decode().strip()[8:]
    with tempfile.TemporaryDirectory(prefix='luma117-e-observer-') as temp:
        observer=Path(temp)/'observer.apk';call('pull',remote,str(observer));assert sha(observer)==json.loads((ROOT/reports[-1]).read_text())['observer_sha256']
    (OUT/'device/observer-uninstall.txt').write_bytes(call('uninstall',package))
    (OUT/'device/normal-stop.txt').write_bytes(call('emu','kill'))
    deadline=time.monotonic()+45
    while time.monotonic()<deadline and 'emulator-5590' in subprocess.check_output([adb,'devices']).decode():time.sleep(.5)
    assert 'emulator-5590' not in subprocess.check_output([adb,'devices']).decode()
    frozen=json.loads((OUT/'candidate-bytes-before.json').read_text());assert all(sha(path)==digest for path,digest in frozen.items())
    (OUT/'device/shutdown.json').write_text(json.dumps({'stopped_normally':True,'avd':'Luma117-Fresh-E-QA','serial':'emulator-5590','userdata_preserved':True,'observer_removed':True,'all_four_E_packages_unchanged':True,'existing_5586_untouched':True},indent=2)+'\n')
    pointer(status='Fresh E all phases PASS; E5590 stopped normally with userdata retained; waiting explicit5586 handoff',active_process_id=None,active_command=None,last_passed_report=reports[-1],fresh_summary='docs/validation/v1.17/fresh-e/summary-e.json')
    print(json.dumps(summary,indent=2),flush=True)

if __name__=='__main__':
    try:main()
    except BaseException:
        pointer(status='Fresh E interrupted/failed; inspect phase report and preserve existing save',failure=traceback.format_exc())
        raise
