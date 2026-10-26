#!/usr/bin/env python3
"""Check production Canvas/native-store bounds; restore emulator display overrides."""
import argparse
import importlib.util
import time
from pathlib import Path

root=Path(__file__).resolve().parents[1]
s=importlib.util.spec_from_file_location('production_layout_base',root/'scripts/smoke-v11.py')
m=importlib.util.module_from_spec(s);s.loader.exec_module(m)
p=argparse.ArgumentParser(description=__doc__);p.add_argument('--serial',required=True);a=p.parse_args()
v=m.V11Smoke(a.serial);report=[]
def override(kind):
    return next((line.split(':',1)[1].strip() for line in v.shell('wm',kind).splitlines() if line.startswith('Override ')),'reset')
size,density=override('size'),override('density')
try:
    for dimensions in ['720x1280','1280x720']:
        v.shell('wm','size',dimensions);v.shell('wm','density','320');time.sleep(.6)
        v.launch();v.home();v.click('Shop');v.click('Gold & rewards')
        width,height=map(int,dimensions.split('x'));current=v.nodes();visible=0
        for n in current:
            if n.get('clickable')!='true':continue
            try:left,top,right,bottom=v.bounds(n)
            except AssertionError:continue
            if right<=0 or bottom<=0 or left>=width or top>=height:continue
            assert 0<=left<right<=width and 0<=top<bottom<=height,(dimensions,m.text(n),n.get('bounds'))
            visible+=1
        assert visible>=1
        # Native content is intentionally scrollable on short windows. Check a
        # second viewport rather than requiring off-screen controls to be visible.
        v.shell('input','swipe',str(width//2),str(int(height*.8)),str(width//2),str(int(height*.35)),'450')
        time.sleep(.25)
        for n in v.nodes():
            if n.get('clickable')!='true':continue
            try:left,top,right,bottom=v.bounds(n)
            except AssertionError:continue
            if right<=0 or bottom<=0 or left>=width or top>=height:continue
            assert 0<=left<right<=width and 0<=top<bottom<=height,(dimensions,m.text(n),n.get('bounds'))
        path=root/'docs/screenshots/production'/('wallet-'+dimensions+'.png')
        path.write_bytes(v.command('exec-out','screencap','-p'))
        v.shell('input','keyevent','4');time.sleep(.3);v.home()
        report.append(f'PASS {dimensions}, density320: Canvas navigation, {visible} initial visible store actions and a scrolled viewport stay inside display; screenshot recorded. No purchase/ad requested.')
finally:
    v.shell('wm','size',size);v.shell('wm','density',density)
    report.append('Original emulator display size/density overrides restored.')
    (root/'docs/validation/v1.4/layout-ui.txt').write_text('\n'.join(report)+'\n')
print('\n'.join(report))
