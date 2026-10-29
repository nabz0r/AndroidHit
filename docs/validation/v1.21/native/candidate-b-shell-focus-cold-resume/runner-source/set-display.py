#!/usr/bin/env python3
"""Apply or restore compact display settings only on the explicitly task-owned v121 AVD."""
from pathlib import Path
import argparse
import datetime
import json
import re
import subprocess

ROOT=Path(__file__).resolve().parents[2]
ADB=Path.home()/'Library/Android/sdk/platform-tools/adb'
SERIAL='emulator-5602'
OUT=ROOT/'docs/validation/v1.21/native'

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('mode',choices=['compact','tiny','restore'])
    parser.add_argument('--label',default='',help='New candidate-specific receipt label; preserves earlier display attempts')
    args=parser.parse_args()
    def call(*parts):return subprocess.check_output([str(ADB),'-s',SERIAL,*parts],text=True).strip()
    assert call('emu','avd','name').splitlines()[0]=='Luma119-Battle-QA','Only the newly created task AVD is authorized'
    assert call('shell','getprop','ro.kernel.qemu')=='1','Emulator required'
    def state():return {'size':call('shell','wm','size'),'density':call('shell','wm','density'),'font_scale':call('shell','settings','get','system','font_scale')}
    assert not args.label or re.fullmatch('[A-Za-z0-9_-]{1,40}',args.label),'Safe candidate receipt label required'
    prefix='display-'+(args.label+'-' if args.label else '')
    out=OUT/(prefix+args.mode+'.json')
    assert not out.exists(),'Preserve every earlier display receipt'
    before=state()
    if args.mode in ('compact','tiny'):
        assert 'Override' not in before['size'] and 'Override' not in before['density'],'Do not overwrite an unknown existing display override'
        call('shell','wm','size','720x1280' if args.mode=='compact' else '640x960');call('shell','wm','density','320');call('shell','settings','put','system','font_scale','1.3')
    else:
        sources=[path for path in [OUT/(prefix+'compact.json'),OUT/(prefix+'tiny.json')] if path.exists()];assert len(sources)==1,'Exactly one original display receipt required';original=json.loads(sources[0].read_text())['before']
        call('shell','wm','size','reset');call('shell','wm','density','reset')
        if original['font_scale']=='null':call('shell','settings','delete','system','font_scale')
        else:call('shell','settings','put','system','font_scale',original['font_scale'])
    after=state()
    if args.mode=='restore':assert after==original,'Original task display settings must be restored exactly'
    receipt={'observed_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'serial':SERIAL,'avd':'Luma119-Battle-QA','mode':args.mode,'before':before,'after':after,'scope':'Task-owned AVD display/font settings only. No existing user device or game preference writes.'}
    out.write_text(json.dumps(receipt,indent=2)+'\n');print(json.dumps(receipt))

if __name__=='__main__':main()
