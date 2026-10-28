#!/usr/bin/env python3
"""Bounded existing SDK menu/avatar cases on retained J; no profile restoration/injection."""
import datetime,hashlib,json,re,subprocess,sys,traceback,xml.etree.ElementTree as ET
from pathlib import Path

ROOT=Path(__file__).resolve().parents[4]
CASE=sys.argv[1] if len(sys.argv)==2 else ''
assert CASE in ['menus','avatar']
LABEL='menu-followup' if CASE=='menus' else 'avatar-followup'
OUT=ROOT/'docs/validation/v1.11/fresh-install'/LABEL
SHOTS=ROOT/'docs/screenshots/v1.11/fresh-install'/LABEL
assert not OUT.exists() and not SHOTS.exists(),'Never overwrite evidence'
OUT.mkdir();SHOTS.mkdir()
ADB='/Users/n/Library/Android/sdk/platform-tools/adb'
PACKAGE='com.androidhit.lumadrift.sandbox'
REMOTE='/sdcard/Android/data/'+PACKAGE+'/files/v111-sdk-ui/'
def cmd(*parts,timeout=30):return subprocess.check_output([ADB,'-s','emulator-5584',*parts],stderr=subprocess.STDOUT,timeout=timeout)
def shell(*parts):return cmd('shell',*parts).decode()
def digest(raw):return hashlib.sha256(raw).hexdigest()
def state(label):
    values={}
    for name in ['luma_drift_save_v1','luma_drift_meta_v1']:
        raw=cmd('exec-out','su','0','cat','/data/user/0/'+PACKAGE+'/shared_prefs/'+name+'.xml')
        (OUT/(label+'-'+name+'.xml')).write_bytes(raw)
        for e in ET.fromstring(raw):values[name+'/'+e.get('name')]=e.text if e.tag=='string' else e.get('value')
    return values

report={'passed':False,'case':CASE,'serial':'emulator-5584','scope':'Existing installed native SDK case. No new test/app source or build, no profile injection/restoration, no purchases or progression. Avatar case may pin the explicitly authorized reversible cosmetic goal.'}
try:
    assert cmd('emu','avd','name').decode().splitlines()[0]=='Luma-Release111-J'
    assert shell('getconf','PAGESIZE').strip()=='16384'
    for target,expected,key in [(PACKAGE,'6f9edf10cddc283a6ae6d0ae876f3196b52660ddc6ed992aeea18bcdb698db78','app'),(PACKAGE+'.test','7a0e2b6a48a19eaf6ea80a1338f420aea7253249c32172e0716203f47c16c61f','test')]:
        path=shell('pm','path',target).strip().removeprefix('package:');assert path.startswith('/data/app/');actual=shell('sha256sum',path).split()[0];assert actual==expected;report[key+'_sha256']=actual
    before=state('before');crash_before=cmd('shell','logcat','-d','-b','crash');(OUT/'crash-before.txt').write_bytes(crash_before)
    report['rotation_before']={k:shell('settings','get','system',k).strip() for k in ['user_rotation','accelerometer_rotation']}
    result=cmd('shell','am','instrument','-w','-r','-e','cases',CASE,'-e','expectedVersion','1.11.0-sandbox-rc1','-e','expectedCode','12',PACKAGE+'.test/com.androidhit.lumadrift.qa.v111.CandidateUiInstrumentation',timeout=600);(OUT/'instrumentation.txt').write_bytes(result)
    native_raw=cmd('exec-out','cat',REMOTE+'report.json');(OUT/'report.json').write_bytes(native_raw);native=json.loads(native_raw);report['native']=native
    for name in ['actions.json']:
        (OUT/name).write_bytes(cmd('exec-out','cat',REMOTE+name))
    frames=[]
    for stem in dict.fromkeys(native['captures']):
        assert re.fullmatch(r'[a-zA-Z0-9_-]+',stem)
        for suffix,dest in [('.json',OUT),('.png',SHOTS)]:
            raw=cmd('exec-out','cat',REMOTE+stem+suffix);(dest/(stem+suffix)).write_bytes(raw)
            if suffix=='.png':frames.append({'name':stem+suffix,'sha256':digest(raw)})
    report['screenshots']=frames
    after=state('after');changed={key:{'before':before.get(key),'after':after.get(key)} for key in sorted(set(before)|set(after)) if before.get(key)!=after.get(key)};report['persisted_changes']=changed
    assert set(changed)<=({'luma_drift_save_v1/collection_goal_v110'} if CASE=='avatar' else set()),'Unexpected persisted economy/progression change'
    crash_after=cmd('shell','logcat','-d','-b','crash');(OUT/'crash-after.txt').write_bytes(crash_after);assert crash_after==crash_before,'New native crash buffer entry';report['crash_buffer_unchanged']=True
    report['rotation_after']={k:shell('settings','get','system',k).strip() for k in report['rotation_before']};assert report['rotation_after']==report['rotation_before'];report['rotation_preserved']=True
    assert native['passed'],'Native case failed'
    report['passed']=True
except BaseException as error:
    report['error']=str(error);report['traceback']=traceback.format_exc()
    for name in ['report.json','failure.txt','failure.json']:
        try:(OUT/('failure-remote-'+name)).write_bytes(cmd('exec-out','cat',REMOTE+name))
        except Exception:pass
finally:
    report['completed_at_utc']=datetime.datetime.now(datetime.timezone.utc).isoformat();report['new_guest_retained']=True
    (OUT/'host-report.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps({'passed':report['passed'],'case':CASE,'checks':report.get('native',{}).get('checkCount'),'screenshots':len(report.get('screenshots',[])),'changed_preferences':report.get('persisted_changes')},indent=2))
if not report['passed']:sys.exit(1)
