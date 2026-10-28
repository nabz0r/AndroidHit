#!/usr/bin/env python3
"""Observe persisted XML on the owned fresh AVD; never writes device state."""
from pathlib import Path
import datetime,hashlib,json,subprocess,time,xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parent
ADB='/Users/n/Library/Android/sdk/platform-tools/adb'
PACKAGE='com.androidhit.lumadrift.sandbox'
previous=None
started=time.monotonic()
first=None
samples=[]
while time.monotonic()-started<900 and not (ROOT/'observer.stop').exists():
    result=subprocess.run([ADB,'-s','emulator-5584','exec-out','run-as',PACKAGE,'cat','shared_prefs/luma_drift_save_v1.xml'],stdout=subprocess.PIPE,stderr=subprocess.PIPE,timeout=10)
    if result.returncode==0:
        raw=result.stdout
        digest=hashlib.sha256(raw).hexdigest()
        if digest!=previous:
            try:
                values={}
                for element in ET.fromstring(raw):
                    key=element.get('name')
                    if element.tag=='int':values[key]=int(element.get('value'))
                    elif element.tag=='boolean':values[key]=element.get('value')=='true'
                    elif element.tag=='string':values[key]=element.text or ''
                observed={k:values.get(k) for k in ['initialized','coins','booster_hint','booster_vision','booster_flow','character','skin','owned_character_nova','owned_skin_lumina','tutorial_v18_fresh_profile','tutorial_v18_reward_claimed','tutorial_v18_state']}
                sample={'observed_at_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'elapsed_seconds':round(time.monotonic()-started,3),'xml_sha256':digest,'fields':observed}
                if first is None and observed['initialized']:
                    first=sample
                    (ROOT/'first-initialized-prefs.xml').write_bytes(raw)
                    (ROOT/'first-initialized.json').write_text(json.dumps(sample,indent=2)+'\n')
                previous=digest;samples.append(sample)
                with (ROOT/'persisted-observations.jsonl').open('a') as stream:stream.write(json.dumps(sample)+'\n')
                (ROOT/'latest-observed-prefs.xml').write_bytes(raw)
            except (ET.ParseError,ValueError):pass
    time.sleep(.25)
summary={'scope':'Read-only exec-out/run-as cat of the actual persisted shared_prefs XML on newly created emulator5584. No profile injection or write. This does not establish a cold process before the starter was observed.','samples':len(samples),'first_initialized':first,'last_observed':samples[-1] if samples else None,'stopped_by_sentinel':(ROOT/'observer.stop').exists()}
if first:
    values=first['fields']
    summary['observed_starter_exact']=values['coins']==180 and values['booster_hint']==3 and values['booster_vision']==3 and values['booster_flow']==2 and values['character']=='nova' and values['skin']=='lumina' and values['owned_character_nova'] is True and values['owned_skin_lumina'] is True and values['tutorial_v18_reward_claimed'] is not True
(ROOT/'persisted-observer-report.json').write_text(json.dumps(summary,indent=2)+'\n')
