#!/usr/bin/env python3
"""Create and boot the explicitly authorized new empty v118 fresh QA AVD only."""
from pathlib import Path
import datetime, hashlib, json, os, socket, subprocess

ROOT=Path(__file__).resolve().parents[2]
SDK=Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'))
NAME='Luma118-Fresh-Art-QA'; PORT=5596; SERIAL='emulator-5596'
IMAGE='system-images;android-36;google_apis_ps16k;arm64-v8a'
OUT=ROOT/'docs/validation/v1.18/native-fresh/device-creation'

def main():
    avd=Path.home()/'.android/avd'
    assert not (avd/(NAME+'.avd')).exists() and not (avd/(NAME+'.ini')).exists(), 'Never replace an existing AVD'
    assert not (OUT/'creation.json').exists(), 'Creation receipt must be new'
    for port in [PORT,PORT+1]:
        with socket.socket() as probe:
            assert probe.connect_ex(('127.0.0.1',port))!=0, 'Authorized port already in use'
    source=SDK/'system-images/android-36/google_apis_ps16k/arm64-v8a/source.properties'
    assert source.is_file(), 'Exact local ARM image required'
    manager=SDK/'cmdline-tools/omni-15859902/bin/avdmanager'
    command=[str(manager),'create','avd','--name',NAME,'--package',IMAGE,'--device','pixel_6']
    env=os.environ.copy(); env['JAVA_HOME']='/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home'
    OUT.mkdir(parents=True,exist_ok=True)
    result=subprocess.run(command,input='no\n',text=True,capture_output=True,env=env)
    (OUT/'avdmanager.txt').write_text(result.stdout+result.stderr)
    assert result.returncode==0, 'New AVD creation failed; preserve output'
    config=avd/(NAME+'.avd/config.ini')
    receipt={'created_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'avd':NAME,'serial':SERIAL,'port':PORT,'fresh':True,'owner':'v118 fresh production art qualification','scope':'New empty task-owned AVD only; no existing AVD/profile copied, opened or modified. No application installed at creation.','command':command,'image':IMAGE,'source_properties_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'config_sha256':hashlib.sha256(config.read_bytes()).hexdigest()}
    (OUT/'creation.json').write_text(json.dumps(receipt,indent=2)+'\n')
    command=[str(SDK/'emulator/emulator'),'-avd',NAME,'-port',str(PORT),'-no-window','-no-snapshot','-no-boot-anim','-no-audio','-gpu','swiftshader_indirect','-netdelay','none','-netspeed','full']
    with (OUT/'emulator.log').open('wb') as log:
        process=subprocess.Popen(command,stdout=log,stderr=subprocess.STDOUT,start_new_session=True,env=env)
    (OUT/'launch.json').write_text(json.dumps({'pid':process.pid,'command':command},indent=2)+'\n')
    print(json.dumps({'serial':SERIAL,'avd':NAME,'pid':process.pid,'creation_receipt':str(OUT/'creation.json'),'receipt_sha256':hashlib.sha256((OUT/'creation.json').read_bytes()).hexdigest()}))

if __name__=='__main__': main()
