#!/usr/bin/env python3
"""Verify public HTTPS bytes of every audited 1.16 Firebase Hosting payload and directory alias."""
import argparse
import concurrent.futures
import datetime
import hashlib
import json
from pathlib import Path
import re
import subprocess
import urllib.request

ROOT=Path(__file__).resolve().parents[1]
ORIGIN='https://luma-drift-production.web.app'
FOLDER=ROOT/'docs/validation/site-v1.16'
RELEASE='1.16.0-rc1'
APK_SHA=None

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--apk-sha256',required=True)
    parser.add_argument('--commit',required=True)
    parser.add_argument('--deployment',type=Path,default=FOLDER/'deployment.json',help='Deployment receipt; can remain outside the checkout')
    parser.add_argument('--output',type=Path,default=FOLDER/'public-origin.json')
    args=parser.parse_args()
    global APK_SHA
    APK_SHA=args.apk_sha256
    assert re.fullmatch(r'[a-f0-9]{64}',APK_SHA),'Independent final APK hash required'
    assert re.fullmatch(r'[a-f0-9]{40}',args.commit),'Immutable source commit required'
    manifest_path=FOLDER/'release/source-manifest.json'
    source=json.loads(manifest_path.read_text());deployed=json.loads(args.deployment.read_text())
    manifest_sha=hashlib.sha256(manifest_path.read_bytes()).hexdigest()
    assert source['status']==deployed['status']=='PASS','Source audit and deployment must pass'
    assert manifest_sha==deployed['source_manifest_sha256'],'Source manifest differs from deployment receipt'
    assert source['release']==deployed['android_release']==RELEASE,'Wrong Android release'
    assert source['production_apk_sha256']==deployed['production_apk_sha256']==APK_SHA,'Wrong production APK identity'
    assert source['artifact_source_commit']==deployed['artifact_source_commit'],'Different immutable local artifact commit'
    assert deployed['source_commit']==args.commit and deployed['cli_deploy_complete'],'Incomplete or different deployment'
    assert deployed['live_management']['version_status']=='FINALIZED','Live version is not finalized'
    tree=subprocess.check_output(['git','rev-parse',args.commit+':public'],cwd=ROOT,text=True).strip()
    assert tree==deployed['source_public_tree'],'Public source tree differs from deployment'
    aliases={'/':'/index.html','/privacy/':'/privacy/index.html','/delete-account/':'/delete-account/index.html'}
    entries=list(source['files'].items())+[(alias,source['files'][target]) for alias,target in aliases.items()]
    def probe(entry):
        path,expected=entry;digest=hashlib.sha256();size=0
        request=urllib.request.Request(ORIGIN+path,headers={'User-Agent':'LumaDrift-site-integrity/1.16','Cache-Control':'no-cache'})
        with urllib.request.urlopen(request,timeout=90) as response:
            status=response.status;final=response.url
            while True:
                data=response.read(1024*1024)
                if not data:break
                size+=len(data);digest.update(data)
        actual=digest.hexdigest()
        assert status==200 and size==expected['bytes'] and actual==expected['sha256'],'Published response differs: '+path
        assert final==ORIGIN+path,'Unexpected public-origin redirect: '+path
        return {'path':path,'url':final,'http_status':status,'bytes':size,'sha256':actual,'matches_deployed_source':True,'directory_alias':path in aliases}
    with concurrent.futures.ThreadPoolExecutor(max_workers=6) as pool:results=list(pool.map(probe,entries))
    record={'status':'PASS','recorded_at_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'source_commit':args.commit,'artifact_source_commit':source['artifact_source_commit'],'production_apk_sha256':APK_SHA,'source_public_tree':tree,'origin':ORIGIN,'android_release':RELEASE,'payloads_verified':len(source['files']),'directory_aliases_verified':len(aliases),'responses':results,'source_manifest_sha256':manifest_sha,
            'scope':'Independent public HTTPS bytes of every Hosting payload including exact signed production APK/AAB/manifest, plus three directory aliases. Browser rendering is separate; no Play publication or provider transaction is inferred.'}
    output=args.output if args.output.is_absolute() else ROOT/args.output
    output.parent.mkdir(parents=True,exist_ok=True);output.write_text(json.dumps(record,indent=2)+'\n')
    print(json.dumps({key:record[key] for key in ('status','source_commit','payloads_verified','directory_aliases_verified')}))

if __name__=='__main__':main()
