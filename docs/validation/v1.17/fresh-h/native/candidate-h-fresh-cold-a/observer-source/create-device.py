#!/usr/bin/env python3
"""Create only a never-existing task-owned fresh AVD; no clone, reset or reused userdata."""
from pathlib import Path
import argparse,hashlib,json,os,re,socket,subprocess

ROOT=Path(__file__).resolve().parents[2]
SDK=Path.home()/'Library/Android/sdk'
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--name',required=True)
parser.add_argument('--output',required=True,type=Path)
args=parser.parse_args()
assert re.fullmatch(r'Luma117-Fresh-[A-Z][A-Za-z0-9_-]*-QA',args.name),'Explicit task-specific new AVD name required'
NAME=args.name
OUTPUT=args.output.resolve();assert OUTPUT.is_relative_to(ROOT/'docs/validation/v1.17')
AVD=Path.home()/'.android/avd'/str(NAME+'.avd')
assert not AVD.exists() and not AVD.with_suffix('.ini').exists(),'Never replace or adopt any existing AVD'
assert 'emulator-5590' not in subprocess.check_output([str(SDK/'platform-tools/adb'),'devices']).decode()
for port in (5590,5591):
    connection=socket.socket();connection.bind(('127.0.0.1',port));connection.close()
OUTPUT.mkdir(parents=True,exist_ok=True)
environment=os.environ.copy();environment.setdefault('JAVA_HOME','/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home')
command=[str(SDK/'cmdline-tools/omni-15859902/bin/avdmanager'),'create','avd','--name',NAME,'--package','system-images;android-36;google_apis_ps16k;arm64-v8a','--device','pixel_7']
result=subprocess.run(command,input='no\n',stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,env=environment)
(OUTPUT/'creation.txt').write_text(result.stdout);assert result.returncode==0,result.stdout
config=AVD/'config.ini'
settings={'hw.ramSize':'3072','hw.cpu.ncore':'2','disk.dataPartition.size':'6G','hw.gpu.enabled':'yes','hw.gpu.mode':'auto','showDeviceFrame':'no','fastboot.forceColdBoot':'yes','firstboot.bootFromDownloadableSnapshot':'no','firstboot.bootFromLocalSnapshot':'no','firstboot.saveToLocalSnapshot':'no'}
lines=[]
for line in config.read_text().splitlines():
    key=line.split('=',1)[0];lines.append(key+'='+settings.pop(key) if key in settings else line)
lines += [key+'='+value for key,value in settings.items()];config.write_text('\n'.join(lines)+'\n')
ownership={'created_for_task':True,'avd':NAME,'avd_directory':str(AVD),'port':5590,'serial':'emulator-5590','new_empty_userdata':True,'system_image':'system-images;android-36;google_apis_ps16k;arm64-v8a','existing_AVDs_untouched':True,'config_sha256':hashlib.sha256(config.read_bytes()).hexdigest()}
(OUTPUT/'ownership.json').write_text(json.dumps(ownership,indent=2)+'\n')
log=(OUTPUT/'emulator.txt').open('wb');command=[str(SDK/'emulator/emulator'),'-avd',NAME,'-port','5590','-no-window','-no-snapshot','-no-boot-anim','-gpu','auto']
process=subprocess.Popen(command,stdout=log,stderr=subprocess.STDOUT,start_new_session=True)
(OUTPUT/'launch.json').write_text(json.dumps({'pid':process.pid,'command':command},indent=2)+'\n')
print(json.dumps(ownership))
