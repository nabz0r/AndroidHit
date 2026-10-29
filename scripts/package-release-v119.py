#!/usr/bin/env python3
"""Collect exact frozen 1.19 APKs as ignored external files; never build, publish or deploy."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess

ROOT=Path(__file__).resolve().parents[1]
SIGNER='dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287'

def sha(path):
    digest=hashlib.sha256()
    with path.open('rb') as source:
        for chunk in iter(lambda:source.read(1024*1024),b''):digest.update(chunk)
    return digest.hexdigest()
def bounded(name):
    path=Path(name);path=path if path.is_absolute() else ROOT/path;path=path.resolve()
    assert ROOT in path.parents and path.is_file(),'Missing bounded candidate input: '+str(name)
    return path
def candidate_apk(entry):
    name=entry.get('artifact',entry.get('path'))
    assert isinstance(name,str),'Explicit frozen APK path required'
    path=Path(name);path=path if path.is_absolute() else ROOT/path
    assert path.is_file(),'Missing explicit frozen APK: '+str(path)
    return path.resolve()

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--candidate-report',required=True,type=Path)
    parser.add_argument('--build-tools',type=Path,default=Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'))/'build-tools/36.0.0')
    parser.add_argument('--collect',action='store_true',help='Write immutable canonical APK copies/manifest only after all checks pass')
    args=parser.parse_args();candidate=bounded(args.candidate_report);record=json.loads(candidate.read_text());apks={}
    assert record.get('source_inputs'),'Frozen source manifest required'
    for entry in record['source_inputs']:
        source=bounded(entry['path'])
        assert source.stat().st_size==entry['bytes'] and sha(source)==entry['sha256'],'Frozen app source changed: '+entry['path']
    for channel in ('production','lab'):
        entry=record['apks'][channel];source=candidate_apk(entry);expected_package='com.androidhit.lumadrift'+('.sandbox' if channel=='lab' else '')
        assert sha(source)==entry['sha256'] and source.stat().st_size==entry['bytes'],'Frozen APK bytes changed: '+channel
        info=subprocess.check_output([str(args.build_tools/'aapt2'),'dump','badging',str(source)],text=True)
        identity=re.search(r"package: name='([^']+)' versionCode='(\d+)' versionName='([^']+)'",info);assert identity,'APK package identity missing'
        package,code,version=identity.group(1),int(identity.group(2)),identity.group(3)
        prefix='1.19.0-sandbox-' if channel=='lab' else '1.19.0-'
        assert version==('1.19.0-sandbox-prototype2' if channel=='lab' else '1.19.0-prototype2') and package==expected_package and code==27 and code==entry['code'] and version==entry['version'] and version.startswith(prefix),'Wrong 1.19 channel identity'
        assert re.fullmatch('[A-Za-z0-9.-]+',version),'Unsafe canonical APK name'
        verified=subprocess.check_output([str(args.build_tools/'apksigner'),'verify','--print-certs',str(source)],text=True)
        signers=re.findall(r'Signer #\d+ certificate SHA-256 digest: ([a-f0-9]+)',verified)
        assert signers==[SIGNER],'Unexpected signer certificate'
        target=ROOT/'releases'/('luma-drift-'+version+'.apk');relative=str(target.relative_to(ROOT))
        assert subprocess.run(['git','check-ignore','--quiet',relative],cwd=ROOT).returncode==0,'Canonical external APK requires explicit ignore: '+relative
        assert subprocess.run(['git','ls-files','--error-unmatch',relative],cwd=ROOT,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode!=0,'APK must not be a Git blob'
        if target.exists():assert sha(target)==entry['sha256'] and target.stat().st_size==entry['bytes'],'Never replace earlier canonical bytes'
        apks[channel]={'path':relative,'sha256':entry['sha256'],'bytes':entry['bytes'],'version':version,'code':code,'application_id':package,'signer_certificate_sha256':SIGNER}
    production=apks['production'];manifest=ROOT/'releases'/('manifest-'+production['version']+'.json')
    result={'edition':'1.19','version':production['version'],'version_code':27,'artifact_policy':'Ignored external signed APK files; never require Git blobs','candidate_report':str(candidate.relative_to(ROOT)),'candidate_report_sha256':sha(candidate),'apks':apks,'publicly_published':False,'live_services_validated':False}
    encoded=json.dumps(result,indent=2)+'\n'
    if manifest.exists():assert manifest.read_text()==encoded,'Never overwrite an existing artifact metadata record'
    if args.collect:
        manifest.parent.mkdir(parents=True,exist_ok=True)
        for channel,entry in apks.items():
            target=ROOT/entry['path']
            if not target.exists():shutil.copyfile(candidate_apk(record['apks'][channel]),target)
            assert sha(target)==entry['sha256'] and target.stat().st_size==entry['bytes'],'Canonical copy differs'
        manifest.write_text(encoded)
    print(json.dumps({'passed':True,'collected':args.collect,'manifest':str(manifest.relative_to(ROOT)),'apks':apks,'published':False,'deployed':False}))

if __name__=='__main__':main()
