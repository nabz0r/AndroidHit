#!/usr/bin/env python3
"""Stream and hash every deployed1.18 Firebase payload, independently of management receipts."""
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
SOURCE=ROOT/'docs/validation/site-v1.18/release/source-manifest.json'


def probe(entry):
    path,expected=entry;digest=hashlib.sha256();size=0
    request=urllib.request.Request(ORIGIN+path,headers={'User-Agent':'LumaDrift-site-integrity/1.18','Cache-Control':'no-cache','Accept-Encoding':'identity'})
    with urllib.request.urlopen(request,timeout=180) as response:
        status=response.status;final=response.url
        while True:
            data=response.read(1024*1024)
            if not data:break
            size+=len(data);digest.update(data)
    assert status==200 and final==ORIGIN+path and size==expected['bytes'] and digest.hexdigest()==expected['sha256'],'Public response differs: '+path
    return {'path':path,'url':final,'http_status':status,'bytes':size,'sha256':digest.hexdigest(),'matches_deployed_source':True}


def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--apk-sha256',required=True);p.add_argument('--commit',required=True)
    p.add_argument('--deployment',type=Path,required=True);p.add_argument('--output',type=Path,required=True)
    a=p.parse_args();assert re.fullmatch('[a-f0-9]{64}',a.apk_sha256) and re.fullmatch('[a-f0-9]{40}',a.commit)
    assert not a.output.exists(),'Public-origin evidence must be new'
    raw=subprocess.check_output(['git','show',a.commit+':docs/validation/site-v1.18/release/source-manifest.json'],cwd=ROOT)
    source=json.loads(raw);deployed=json.loads(a.deployment.read_text());source_sha=hashlib.sha256(raw).hexdigest()
    assert source['status']==deployed['status']=='PASS' and source['edition']=='1.18'
    assert source_sha==deployed['source_manifest_sha256'] and SOURCE.read_bytes()==raw,'Source manifest differs from exact deployment commit'
    assert source['release']==deployed['android_release']=='1.18.0-prototype1'
    assert source['production_apk_sha256']==deployed['production_apk_sha256']==a.apk_sha256
    assert source['lab_apk_sha256']==deployed['lab_apk_sha256'] and source['artifact_source_commit']==deployed['artifact_source_commit']
    assert deployed['source_commit']==a.commit and deployed['cli_deploy_complete'] and deployed['live_management']['version_status']=='FINALIZED'
    tree=subprocess.check_output(['git','rev-parse',a.commit+':public'],cwd=ROOT,text=True).strip();assert tree==deployed['source_public_tree']
    aliases={'/':'/index.html','/privacy/':'/privacy/index.html','/delete-account/':'/delete-account/index.html'}
    entries=list(source['files'].items())+[(alias,source['files'][target]) for alias,target in aliases.items()]
    results=[];failures=[]
    with concurrent.futures.ThreadPoolExecutor(max_workers=6) as pool:
        future_to_path={pool.submit(probe,entry):entry[0] for entry in entries}
        for future in concurrent.futures.as_completed(future_to_path):
            try:results.append(future.result())
            except Exception as error:failures.append({'path':future_to_path[future],'error':str(error)})
    results.sort(key=lambda item:item['path'])
    record={'status':'PASS' if not failures else 'FAIL','recorded_at_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'source_commit':a.commit,'artifact_source_commit':source['artifact_source_commit'],'production_apk_sha256':a.apk_sha256,'lab_apk_sha256':source['lab_apk_sha256'],'source_public_tree':tree,'origin':ORIGIN,'android_release':source['release'],'payloads_expected':len(source['files']),'directory_aliases_expected':len(aliases),'responses_verified':len(results),'bytes_verified':sum(item['bytes'] for item in results),'responses':results,'failures':failures,'source_manifest_sha256':source_sha,'management_receipt_sha256':hashlib.sha256(a.deployment.read_bytes()).hexdigest(),'scope':'Independent streamed HTTPS bytes of every Hosting file, both exact signed H APKs and three directory aliases, checked against the committed audited source manifest. APK Git blobs are not required. Browser rendering, physical-phone behavior, Play publication and provider transactions are separate.'}
    a.output.parent.mkdir(parents=True,exist_ok=True);a.output.write_text(json.dumps(record,indent=2)+'\n')
    print(json.dumps({key:record[key] for key in ('status','source_commit','responses_verified','bytes_verified','failures')}))
    if failures:raise SystemExit(1)


if __name__=='__main__':main()
