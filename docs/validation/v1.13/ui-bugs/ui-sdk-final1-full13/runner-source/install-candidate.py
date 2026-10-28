#!/usr/bin/env python3
"""Preserve the real owned guest profile before an APK update; never create a test profile."""
from pathlib import Path
import argparse,hashlib,json,os,re,subprocess,tempfile,xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[2]

def main():
 p=argparse.ArgumentParser();p.add_argument('--variant',choices=['lab'],required=True);p.add_argument('--apk',required=True);p.add_argument('--sha256',required=True);p.add_argument('--label',required=True);a=p.parse_args()
 assert re.fullmatch(r'[a-zA-Z0-9_-]{1,40}',a.label),'Safe unique label required'
 apk=Path(a.apk);assert apk.is_file() and hashlib.sha256(apk.read_bytes()).hexdigest()==a.sha256,'Frozen candidate bytes do not match explicit hash'
 for line in subprocess.check_output(['ps','-axo','pid=,command='],text=True).splitlines():
  fields=line.strip().split(None,1)
  if len(fields)==2 and re.match(r'^(?:\S*/)?python(?:3(?:\.\d+)?)?\s+\S*run\.py(?:\s|$)',fields[1]):raise AssertionError('A native run.py is still active; no APK update permitted')
 adb=str(Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'))/'platform-tools/adb');package='com.androidhit.lumadrift'+('.sandbox' if a.variant=='lab' else '')
 def cmd(*parts):return subprocess.check_output([adb,'-s','emulator-5580',*parts],stderr=subprocess.STDOUT,timeout=120)
 def shell(*parts):return cmd('shell',*parts).decode(errors='replace')
 assert cmd('emu','avd','name').decode().splitlines()[0].strip()=='hexkeep-play-36-16k','Exact owned AVD required'
 assert shell('getprop','ro.kernel.qemu').strip()=='1' and shell('getconf','PAGESIZE').strip()=='16384','Owned16KiB emulator required'
 out=ROOT/'docs/validation/v1.13/ui-bugs'/('install-'+a.label);assert not out.exists(),'Evidence label already exists';out.mkdir(parents=True)
 prefdir='/data/user/0/'+package+'/shared_prefs';backup=Path(tempfile.mkdtemp(prefix='luma-v113-before-apk-update-'));original={};ready=False;report={'passed':False,'package':package,'serial':'emulator-5580','scope':'Actual original guest preference backup before package update; no game launch, profile seeding, purchase or provider action','candidate_apk_sha256':a.sha256,'backup_location':str(backup)}
 def names():
  result=shell('su','0','ls','-1',prefdir).splitlines();assert all(re.fullmatch(r'[A-Za-z0-9_.-]+',n) for n in result),'Unexpected preference file name';return result
 try:
  old=shell('pm','path',package).strip().removeprefix('package:');assert old,'Original package must already exist'
  report['original_apk_sha256']=shell('sha256sum',old).split()[0];shell('am','force-stop',package)
  for name in names():
   raw=cmd('exec-out','su','0','cat',prefdir+'/'+name);original[name]=raw;(backup/name).write_bytes(raw)
   for node in ET.fromstring(raw):assert not(node.get('name')=='previously_signed_in' and node.get('value')=='true'),'Refuses an existing signed-in Google identity'
  ready=True;report['pre_update_preferences_sha256']={n:hashlib.sha256(v).hexdigest() for n,v in original.items()}
  (out/'install.txt').write_text(cmd('install','-r',str(apk)).decode(errors='replace'))
  new=shell('pm','path',package).strip().removeprefix('package:');assert shell('sha256sum',new).split()[0]==a.sha256,'Installed package differs from selected frozen bytes'
  assert set(names())==set(original),'APK update changed original preference file set before test launch'
  for name,raw in original.items():assert cmd('exec-out','su','0','cat',prefdir+'/'+name)==raw,'APK update changed original preference bytes before test launch: '+name
  report['profile_unchanged_before_first_candidate_launch']=True;report['passed']=True
 except BaseException as e:report['error']=str(e);raise
 finally:
  report['original_backup_complete']=ready;(out/'report.json').write_text(json.dumps(report,indent=2)+'\n')
 print(json.dumps({'passed':True,'original_backup_complete':True,'package':package,'profile_unchanged_before_first_candidate_launch':True,'report':str(out/'report.json')}))

if __name__=='__main__':main()
