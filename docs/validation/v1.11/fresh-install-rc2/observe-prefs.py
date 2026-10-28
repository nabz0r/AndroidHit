#!/usr/bin/env python3
"""Read-only persisted XML observer on the explicitly owned fresh K AVD."""
from pathlib import Path
import datetime,hashlib,json,subprocess,time,xml.etree.ElementTree as ET

OUT=Path(__file__).resolve().parent
ADB='/Users/n/Library/Android/sdk/platform-tools/adb'
TARGET='/data/user/0/com.androidhit.lumadrift.sandbox/shared_prefs/luma_drift_save_v1.xml'
previous=None;first=None;samples=[];errors=0;started=time.monotonic()
while time.monotonic()-started<1200 and not (OUT/'observer.stop').exists():
    try:
        result=subprocess.run([ADB,'-s','emulator-5586','exec-out','su','0','cat',TARGET],stdout=subprocess.PIPE,stderr=subprocess.PIPE,timeout=10)
        if result.returncode==0:
            raw=result.stdout;digest=hashlib.sha256(raw).hexdigest()
            if digest!=previous:
                values={}
                for e in ET.fromstring(raw):
                    if e.tag=='int':values[e.get('name')]=int(e.get('value'))
                    elif e.tag=='boolean':values[e.get('name')]=e.get('value')=='true'
                    elif e.tag=='string':values[e.get('name')]=e.text or ''
                fields={k:values.get(k) for k in ['initialized','coins','booster_hint','booster_vision','booster_flow','character','skin','owned_character_nova','owned_skin_lumina','tutorial_v18_fresh_profile','tutorial_v18_reward_claimed','tutorial_v18_state']}
                sample={'observed_at_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'elapsed_seconds':round(time.monotonic()-started,3),'xml_sha256':digest,'fields':fields}
                samples.append(sample);previous=digest
                with (OUT/'persisted-observations.jsonl').open('a') as stream:stream.write(json.dumps(sample)+'\n')
                if first is None and fields['initialized']:
                    first=sample;(OUT/'first-initialized-prefs.xml').write_bytes(raw);(OUT/'first-initialized.json').write_text(json.dumps(sample,indent=2)+'\n')
                (OUT/'latest-observed-prefs.xml').write_bytes(raw)
        else:errors+=1
    except (ET.ParseError,ValueError,subprocess.TimeoutExpired):errors+=1
    time.sleep(.25)
report={'scope':'Read-only adb exec-out su0 cat of the real persisted XML on fresh emulator5586. Observer starts before installation/launch; never writes device state. No profile injection or disk-fault simulation.','samples':len(samples),'unavailable_or_incomplete_reads':errors,'first_initialized':first,'last_observed':samples[-1] if samples else None,'stopped_by_sentinel':(OUT/'observer.stop').exists()}
if first:
    values=first['fields'];report['observed_starter_exact']=values['coins']==180 and values['booster_hint']==3 and values['booster_vision']==3 and values['booster_flow']==2 and values['character']=='nova' and values['skin']=='lumina' and values['owned_character_nova'] is True and values['owned_skin_lumina'] is True and values['tutorial_v18_reward_claimed'] is not True
(OUT/'persisted-observer-report.json').write_text(json.dumps(report,indent=2)+'\n')
