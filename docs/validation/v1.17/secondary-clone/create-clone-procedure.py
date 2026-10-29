from pathlib import Path
import datetime, hashlib, json, os, shutil, subprocess, time

ROOT=Path('/Users/n/androidhit')
SDK=Path('/Users/n/Library/Android/sdk')
ADB=SDK/'platform-tools/adb'
SOURCE=Path('/Users/n/.android/avd/Luma-Refonte115-QA.avd')
DEST=Path('/Users/n/.android/avd/Luma117-Secondary-F-QA.avd')
REGISTRY=DEST.with_suffix('.ini')
OUT=ROOT/'docs/validation/v1.17/secondary-clone'
APK_SHA='fe556ae6f0420f41afb7e5db044ef925fe79d0695f0ac6190d6870daf2e8d5a8'

def sha(path):
    digest=hashlib.sha256()
    with Path(path).open('rb') as stream:
        for block in iter(lambda:stream.read(4*1024*1024),b''):digest.update(block)
    return digest.hexdigest()

def adb(*args):return subprocess.check_output([str(ADB),'-s','emulator-5586',*args],text=True,stderr=subprocess.STDOUT)

assert not DEST.exists() and not REGISTRY.exists() and not OUT.exists(), 'Never overwrite any AVD or earlier receipt'
baseline=ROOT/'docs/validation/v1.17/native-interface/candidate-f-clone-baseline/host-report.json'
report=json.loads(baseline.read_text())
assert report['passed'] and report['apk_sha256']==APK_SHA and report['expected_code']==22
assert report['serial']=='emulator-5586' and report['avd']=='Luma-Refonte115-QA'
assert len(report['phases'])==1 and report['phases'][0]['phase']=='baseline' and report['phases'][0]['native']['passed']
data_path=baseline.parent/'baseline/baseline.json'
data=json.loads(data_path.read_text());assert data['legacySave']['coins']==51510 and data['legacySave']['stars_1000']>0
assert data['version']=='1.17.0-rc4' and data['versionCode']==22
assert adb('emu','avd','name').splitlines()[0]=='Luma-Refonte115-QA'
processes=subprocess.check_output(['ps','-Ao','pid,command'],text=True).splitlines()
owners=[line for line in processes if 'qemu-system-aarch64-headless' in line and '-avd Luma-Refonte115-QA -port 5586 ' in line]
assert len(owners)==1,owners
pid=int(owners[0].split()[0]);OUT.mkdir(parents=True)
(OUT/'source-process.txt').write_text(owners[0]+'\n')
shutdown=adb('emu','kill');(OUT/'source-shutdown.txt').write_text(shutdown)
deadline=time.time()+60
while time.time()<deadline:
    command=subprocess.run(['ps','-p',str(pid),'-o','command='],text=True,capture_output=True).stdout
    if 'qemu-system-aarch64-headless' not in command:break
    time.sleep(.25)
else:raise RuntimeError('Owned source emulator did not shut down normally; no copy attempted')
assert 'emulator-5586\tdevice' not in subprocess.check_output([str(ADB),'devices'],text=True)
disks=[p for p in sorted(SOURCE.iterdir()) if p.is_file() and ('.img' in p.name or p.name=='initrd')]
assert any(p.name=='userdata-qemu.img.qcow2' for p in disks)
before={p.name:(sha(p),p.stat().st_size) for p in disks}
subprocess.run(['/bin/cp','-cR',str(SOURCE),str(DEST)],check=True)
records=[]
for name,(digest,size) in before.items():
    assert sha(SOURCE/name)==digest and sha(DEST/name)==digest
    records.append({'name':name,'bytes':size,'source_sha256':digest,'clone_sha256':digest})
config=DEST/'config.ini';config_before=sha(config)
settings=dict(line.split('=',1) for line in config.read_text().splitlines() if '=' in line)
settings.update({'AvdId':'Luma117-Secondary-F-QA','avd.id':'Luma117-Secondary-F-QA','avd.name':'Luma117-Secondary-F-QA','avd.ini.displayname':'Luma117-Secondary-F-QA'})
config.write_text(''.join(key+'='+value+'\n' for key,value in settings.items()))
REGISTRY.write_text('avd.ini.encoding=UTF-8\npath='+str(DEST)+'\npath.rel=avd/Luma117-Secondary-F-QA.avd\ntarget=android-36\n')
removed=[]
for name in ['hardware-qemu.ini','hardware-qemu.ini.lock','multiinstance.lock','emu-launch-params.txt']:
    path=DEST/name
    if path.exists():assert path.is_file();path.unlink();removed.append(name)
infos={}
for name in ['userdata-qemu.img.qcow2','encryptionkey.img.qcow2','cache.img.qcow2']:
    infos[name]=json.loads(subprocess.check_output([str(SDK/'emulator/qemu-img'),'info','--output=json',str(DEST/name)],text=True))
    backing=infos[name].get('backing-filename')
    if backing:
        resolved=(DEST/backing).resolve() if not Path(backing).is_absolute() else Path(backing).resolve()
        assert resolved.parent==DEST or str(resolved).startswith(str(SDK/'system-images')+'/'), 'Writable source AVD backing would violate isolation: '+str(resolved)
record={'schema_version':1,'passed':False,'source_serial':'emulator-5586','source_avd':'Luma-Refonte115-QA','clone_serial':'emulator-5592','clone_avd':'Luma117-Secondary-F-QA','apk_sha256':APK_SHA,'production_version':'1.17.0-rc4','production_code':22,'source_stopped_before_copy':True,'source_qemu_pid_before':pid,'copied_at_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'copy_method':'APFS copy-on-write whole AVD directory via /bin/cp -cR after normal emulator shutdown; every mutable disk byte hash checked before and after. No guest preference edits.','source_directory':str(SOURCE),'clone_directory':str(DEST),'disks':records,'baseline':{'report':str(baseline.relative_to(ROOT)),'pathhash':sha(baseline),'data':str(data_path.relative_to(ROOT)),'data_sha256':sha(data_path),'completed_level':1000,'coins':51510},'clone_config_before_sha256':config_before,'clone_config_after_sha256':sha(config),'removed_clone_runtime_metadata':removed,'qemu_images':infos,'scope':'Physical clone of task-owned retained QA disk; no preference injection; separate native interaction evidence, not a second migration qualification. Final passed state additionally requires observed clone boot identity and exact installed APK metadata.'}
(OUT/'creation-pending-boot.json').write_text(json.dumps(record,indent=2)+'\n')
print(json.dumps({'copied':True,'disk_count':len(records),'source_stopped':True,'pending':'creation-pending-boot.json'},indent=2),flush=True)
