#!/usr/bin/env python3
"""Create one new empty task-owned v124 fresh QA device; never copy or replace an existing profile."""
from pathlib import Path
import datetime
import hashlib
import json
import os
import socket
import subprocess

ROOT=Path(__file__).resolve().parents[2]
SDK=Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'))
NAME='Luma124-Fresh-QA'
PORT=5636
SERIAL='emulator-5636'
IMAGE='system-images;android-36;google_apis_ps16k;arm64-v8a'
OUT=ROOT/'docs/validation/v1.24/native/device-creation-fresh'

def main():
    avd=Path.home()/'.android/avd'
    assert not (avd/(NAME+'.avd')).exists() and not (avd/(NAME+'.ini')).exists(), 'Never replace an existing AVD'
    assert not OUT.exists(), 'Creation evidence must be new'
    for port in (PORT,PORT+1):
        with socket.socket() as probe:
            assert probe.connect_ex(('127.0.0.1',port))!=0, 'Task port is already occupied'
    source=SDK/'system-images/android-36/google_apis_ps16k/arm64-v8a/source.properties'
    assert source.is_file(), 'Exact local ARM 16KiB image required'
    command=[str(SDK/'cmdline-tools/omni-15859902/bin/avdmanager'),'create','avd','--name',NAME,'--package',IMAGE,'--device','pixel_6']
    env=os.environ.copy()
    env['JAVA_HOME']='/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home'
    OUT.mkdir(parents=True)
    result=subprocess.run(command,input='no\n',text=True,capture_output=True,env=env)
    (OUT/'avdmanager.txt').write_text(result.stdout+result.stderr)
    assert result.returncode==0, 'Creation failed; preserve evidence'
    config=avd/(NAME+'.avd/config.ini')
    receipt={'created_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'avd':NAME,'serial':SERIAL,'port':PORT,'fresh':True,'owner':'v124 fresh core-loop native QA','scope':'New empty task-owned AVD. No existing AVD or userdata copied, opened, replaced or modified. Game and observer not installed at creation.','command':command,'image':IMAGE,'source_properties_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'config_sha256':hashlib.sha256(config.read_bytes()).hexdigest()}
    (OUT/'creation.json').write_text(json.dumps(receipt,indent=2)+'\n')
    launch=[str(SDK/'emulator/emulator'),'-avd',NAME,'-port',str(PORT),'-no-window','-no-snapshot','-no-boot-anim','-no-audio','-gpu','swiftshader_indirect','-netdelay','none','-netspeed','full']
    with (OUT/'emulator.log').open('wb') as log:
        process=subprocess.Popen(launch,stdout=log,stderr=subprocess.STDOUT,start_new_session=True,env=env)
    (OUT/'launch.json').write_text(json.dumps({'pid':process.pid,'command':launch},indent=2)+'\n')
    print(json.dumps({'serial':SERIAL,'avd':NAME,'pid':process.pid,'receipt':str(OUT/'creation.json'),'receipt_sha256':hashlib.sha256((OUT/'creation.json').read_bytes()).hexdigest()}))

if __name__=='__main__':main()
