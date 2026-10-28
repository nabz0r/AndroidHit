#!/usr/bin/env python3
"""Real UI toggle on the second, explicitly owned showcase emulator only."""
from pathlib import Path
import subprocess,re,time,json,hashlib,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[2]
ADB='/Users/n/Library/Android/sdk/platform-tools/adb'
BASE=[ADB,'-s','emulator-5588'];AVD='Luma-Refonte115-Showcase-QA'
OUT=ROOT/'docs/validation/v1.15/ui-bugs/showcase-reduce-motion'
SHOTS=ROOT/'docs/screenshots/v1.15/ui-bugs/showcase-reduce-motion'
assert not OUT.exists() and not SHOTS.exists();OUT.mkdir(parents=True);SHOTS.mkdir(parents=True)
def call(*args):return subprocess.check_output(BASE+list(args),stderr=subprocess.STDOUT,timeout=45)
def shell(*args):return call('shell',*args)
assert call('emu','avd','name').decode().splitlines()[0]==AVD
package='com.androidhit.lumadrift';apk=shell('pm','path',package).decode().strip().removeprefix('package:')
assert shell('sha256sum',apk).decode().split()[0]=='07b5bfc2632edc3d34f7583ac71c8343b52d7b926ed526aeb7a88599253df0f9'
(OUT/'bootstrap-observer-uninstall.txt').write_bytes(call('uninstall','com.androidhit.lumadrift.relaytest'))
actions=[]
def launch():
 shell('am','force-stop',package);shell('am','start','-W','-n',package+'/com.androidhit.lumadrift.MainActivity');time.sleep(1)
def dump(name):
 shell('uiautomator','dump','/sdcard/showcase-motion.xml');data=shell('cat','/sdcard/showcase-motion.xml');(OUT/(name+'.xml')).write_bytes(data);return ET.fromstring(data)
def find(root,label):
 for node in root.iter('node'):
  if node.get('content-desc')==label or node.get('text')==label:return node
 raise AssertionError('Native control absent: '+label)
def tap(node,label):
 bounds=list(map(int,re.findall(r'\d+',node.get('bounds'))));assert len(bounds)==4
 x=(bounds[0]+bounds[2])//2;y=(bounds[1]+bounds[3])//2;shell('input','tap',str(x),str(y));actions.append({'label':label,'bounds':bounds,'tap':[x,y],'input':'ADB native touchscreen at observed accessibility bounds'});time.sleep(.3)
def capture(name):
 data=call('exec-out','screencap','-p');(SHOTS/(name+'.png')).write_bytes(data)
launch();tap(find(dump('adventure-before'),'Settings'),'Settings');tree=dump('settings-before');find(tree,'Reduce motion: Off');capture('settings-motion-off');tap(find(tree,'Reduce motion: Off'),'Reduce motion: Off');find(dump('settings-after'),'Reduce motion: On');capture('settings-motion-on')
launch();tap(find(dump('adventure-cold'),'Settings'),'Settings');find(dump('settings-cold'),'Reduce motion: On');capture('settings-motion-on-cold')
shell('input','keyevent','4')
report={'passed':True,'avd':AVD,'serial':'emulator-5588','apk_sha256':'07b5bfc2632edc3d34f7583ac71c8343b52d7b926ed526aeb7a88599253df0f9','observed_before':'Reduce motion: Off','observed_after':'Reduce motion: On','observed_cold':'Reduce motion: On','value_confirmed_via':'Actual native accessibility control state, including a distinct cold launch; no preference edit','actions':actions,'scope':'Only the second fresh showcase profile; genuine U1–U100 already completed; app setting changed through the visible control; no coins, stock or progress edited','screenshots':{str(p.relative_to(ROOT)):hashlib.sha256(p.read_bytes()).hexdigest() for p in SHOTS.iterdir()}}
(OUT/'report.json').write_text(json.dumps(report,indent=2));print(json.dumps({'passed':True,'reduce_motion':True,'cold_confirmed':True,'report':str(OUT/'report.json')}))
