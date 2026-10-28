#!/usr/bin/env python3
"""Read the four exact 1.13 public packages at an immutable GitHub commit."""
import argparse
import concurrent.futures
import datetime
import hashlib
import json
import pathlib
import re
import time
import urllib.request

ROOT=pathlib.Path(__file__).resolve().parents[1]

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--commit',required=True)
    parser.add_argument('--output',default='docs/validation/v1.13/publication.json')
    args=parser.parse_args()
    if not re.fullmatch(r'[0-9a-f]{40}',args.commit):parser.error('An immutable forty-character commit is required')
    expected={}
    for version in ['1.13.0-rc2','1.13.0-sandbox-rc2']:
        manifest=json.loads((ROOT/'releases'/('manifest-'+version+'.json')).read_text())
        for extension in ['apk','aab']:
            name='luma-drift-'+version+'.'+extension;expected[name]=manifest['artifacts'][name]
    def probe(entry):
        name,wanted=entry;url='https://raw.githubusercontent.com/nabz0r/AndroidHit/'+args.commit+'/releases/'+name
        for attempt in range(3):
            try:
                digest=hashlib.sha256();size=0
                with urllib.request.urlopen(urllib.request.Request(url,headers={'User-Agent':'LumaDrift-release-integrity/1.13'}),timeout=90) as response:
                    status=response.status
                    while True:
                        chunk=response.read(1024*1024)
                        if not chunk:break
                        size+=len(chunk);digest.update(chunk)
                observed=digest.hexdigest()
                if status!=200 or size!=wanted['bytes'] or observed!=wanted['sha256']:raise AssertionError('Public package mismatch: '+name)
                return {'path':'releases/'+name,'url':url,'http_status':status,'bytes':size,'sha256':observed,'matches_frozen_package':True}
            except Exception:
                if attempt==2:raise
                time.sleep(attempt+1)
    with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:results=list(pool.map(probe,expected.items()))
    record={'status':'PASS','recorded_at_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'repository':'nabz0r/AndroidHit','source_commit':args.commit,'packages':results,'scope':'Independent streamed public HTTP responses at an immutable commit; exact sizes and SHA-256 versus the packaged release manifests. This does not establish Play publication, provider transactions or human acceptance.'}
    output=pathlib.Path(args.output)
    if not output.is_absolute():output=ROOT/output
    output.parent.mkdir(parents=True,exist_ok=True);output.write_text(json.dumps(record,indent=2)+'\n')
    print(json.dumps({'status':'PASS','source_commit':args.commit,'public_packages':len(results),'bytes':sum(x['bytes'] for x in results),'evidence':str(output)}))

if __name__=='__main__':main()
