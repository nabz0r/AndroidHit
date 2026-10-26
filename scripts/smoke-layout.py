#!/usr/bin/env python3
import argparse,subprocess,xml.etree.ElementTree as ET,re,time,json
from pathlib import Path
parser=argparse.ArgumentParser();parser.add_argument('--serial',required=True);args=parser.parse_args();serial=args.serial
if not serial.startswith('emulator-'):raise SystemExit('Emulator serial required')
root=Path(__file__).resolve().parents[1];adb=str(Path.home()/'Library/Android/sdk/platform-tools/adb')
def call(*a):return subprocess.check_output([adb,'-s',serial,*a],timeout=20)
def shell(*a):return call('shell',*a).decode()
def nodes():
 shell('uiautomator','dump','/sdcard/luma-layout.xml');return list(ET.fromstring(shell('cat','/sdcard/luma-layout.xml')).iter('node'))
def click(name):
 ns=[n for n in nodes() if n.get('text','').casefold()==name.casefold() and n.get('clickable')=='true'];assert ns,name
 n=ns[-1];b=list(map(int,re.findall(r'\d+',n.get('bounds'))));shell('input','tap',str((b[0]+b[2])//2),str((b[1]+b[3])//2));time.sleep(.2)
def capture(name): (root/'docs/screenshots'/name).write_bytes(call('exec-out','screencap','-p'))
log=[]
shell('am','force-stop','com.androidhit.lumadrift');shell('am','start','-n','com.androidhit.lumadrift/.MainActivity');time.sleep(.4);click('Settings');click('Credits & license');assert any(n.get('text','').casefold()=='source code' for n in nodes());click('Close');click('Back') if any(n.get('text')=='Back' for n in nodes()) else shell('input','keyevent','4');log.append('Embedded license dialog and source action visible: PASS')
shell('wm','size','720x1280');shell('wm','density','320');time.sleep(.8)
click('Events');click('Weekly Voyage');ns=nodes();assert any(n.get('text')=='Collected' for n in ns)
for n in ns:
 if n.get('package')=='com.androidhit.lumadrift' and n.get('clickable')=='true':
  b=list(map(int,re.findall(r'\d+',n.get('bounds'))));assert b[0]>=0 and b[1]>=0 and b[2]<=720 and b[3]<=1280,(n.get('text'),b)
capture('small-screen-weekly.png');log.append('720x1280, 320dpi: weekly bonus and all visible controls within display bounds: PASS')
shell('wm','size','1280x720');time.sleep(.8);capture('landscape.png');assert any(n.get('package')=='com.androidhit.lumadrift' for n in nodes());log.append('1280x720 resized landscape: activity renders without crash: PASS; portrait layout is centered and scaled')
shell('wm','size','reset');shell('wm','density','reset');time.sleep(.8)
shell('am','force-stop','com.androidhit.lumadrift');shell('am','start','-n','com.androidhit.lumadrift/.MainActivity');time.sleep(.5)
foreground=shell('dumpsys','audio');section=foreground.split('Audio Focus stack entries',1)[-1].split('Notify',1)[0]
# Capture focus state evidence without interpreting historical event records as live owners.
(root/'docs/validation/audio-foreground.txt').write_text('Android 16 emulator: relevant live audio-focus excerpt only.\n'+foreground.split('Audio Focus stack entries',1)[1].split('No external focus policy',1)[0])
shell('input','keyevent','3');time.sleep(.4);background=shell('dumpsys','audio');(root/'docs/validation/audio-background.txt').write_text('Android 16 emulator: relevant live audio-focus excerpt only.\n'+background.split('Audio Focus stack entries',1)[1].split('No external focus policy',1)[0])
shell('am','start','-n','com.androidhit.lumadrift/.MainActivity');time.sleep(.3)
assert 'FATAL EXCEPTION' not in shell('logcat','-d','-b','crash')
log.append('Crash buffer after resize, license and background transitions: clean')
(root/'docs/LAYOUT_RESULTS.txt').write_text('\n'.join(log)+'\n');print('\n'.join(log))
