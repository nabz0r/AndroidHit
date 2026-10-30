#!/usr/bin/env python3
"""Bounded display/font changes on one of the three identity-pinned 1.23 QA AVDs."""
from pathlib import Path
import argparse, datetime, hashlib, json, re, subprocess
ROOT=Path(__file__).resolve().parents[2]
ADB=Path.home()/'Library/Android/sdk/platform-tools/adb'
OUT=ROOT/'docs/validation/v1.23/native'
DEVICES={
 'retained':('emulator-5602','Luma119-Battle-QA','docs/validation/v1.19/native/device-creation/creation.json','022362d42b38acf665a2266d1fb2dbec40981f5640d6955d7d86c94c1a1196ae'),
 'fresh':('emulator-5630','Luma123-Fresh-QA','docs/validation/v1.23/native/device-creation-fresh/creation.json','ced8bcf8a1f4db49f593baa80b901af1b63e715cad34fc9e5c290fa6ada9614b'),
 'final-fresh':('emulator-5632','Luma123-FinalFresh-QA','docs/validation/v1.23/native/device-creation-final-fresh-attempt-2/creation.json','9095a9f3f23c029b0d27e3784c20b688c50f2c5f9978219b5f6cae534930cd32')}
def main():
 parser=argparse.ArgumentParser(description=__doc__)
 parser.add_argument('mode',choices=['compact','tiny','landscape','restore'])
 parser.add_argument('--device',choices=DEVICES,default='retained')
 parser.add_argument('--label',required=True)
 args=parser.parse_args()
 assert re.fullmatch('[A-Za-z0-9_-]{1,40}',args.label),'Safe unique receipt label required'
 serial,avd,creation,expected=DEVICES[args.device]
 assert hashlib.sha256((ROOT/creation).read_bytes()).hexdigest()==expected,'Pinned device creation receipt differs'
 def call(*parts): return subprocess.check_output([str(ADB),'-s',serial,*parts],text=True,timeout=20).strip()
 assert call('emu','avd','name').splitlines()[0]==avd,'Only the pinned task AVD is authorized'
 assert call('shell','getprop','ro.kernel.qemu')=='1','Emulator required'
 def state(): return {'size':call('shell','wm','size'),'density':call('shell','wm','density'),'font_scale':call('shell','settings','get','system','font_scale')}
 prefix='display-'+args.device+'-'+args.label+'-'
 out=OUT/(prefix+args.mode+'.json');before=state()
 assert not out.exists(),'Preserve earlier display evidence'
 if args.mode=='restore':
  sources=[OUT/(prefix+x+'.json') for x in ['compact','tiny','landscape'] if (OUT/(prefix+x+'.json')).exists()]
  assert len(sources)==1,'Exactly one matching original display receipt required'
  source=json.loads(sources[0].read_text())
  assert source['status']=='PASS' and source['serial']==serial and source['avd']==avd,'Original successful receipt required'
  assert before==source['after'],'Do not replace a display setting changed by another owner'
  original=source['before']
 else:
  assert 'Override' not in before['size'] and 'Override' not in before['density'],'Unknown display override must not be replaced'
  assert before['font_scale'] in ('null','1.0'),'Unknown original font scale must not be replaced'
 receipt={'observed_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'serial':serial,'avd':avd,'creation_sha256':expected,'mode':args.mode,'before':before,'status':'STARTED','scope':'Task-owned AVD display/font only; no game preferences, reset, clear or grant.'}
 with out.open('x') as stream: stream.write(json.dumps(receipt,indent=2)+'\n')
 try:
  if args.mode=='restore':
   call('shell','wm','size','reset');call('shell','wm','density','reset')
   if original['font_scale']=='null': call('shell','settings','delete','system','font_scale')
   else: call('shell','settings','put','system','font_scale',original['font_scale'])
  else:
   call('shell','wm','size',{'compact':'720x1280','tiny':'640x960','landscape':'1280x720'}[args.mode]);call('shell','wm','density','320');call('shell','settings','put','system','font_scale','1.3')
  receipt['after']=state()
  if args.mode=='restore': assert receipt['after']==original,'Original display must be restored exactly'
  receipt['status']='PASS'
 except Exception as error:
  receipt.update(status='FAIL',error=str(error));out.write_text(json.dumps(receipt,indent=2)+'\n');raise
 out.write_text(json.dumps(receipt,indent=2)+'\n');print(json.dumps(receipt))
if __name__=='__main__':main()
