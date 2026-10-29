#!/usr/bin/env python3
"""Deploy the exact audited 1.16 payload from a clean commit, to existing Firebase Hosting only.

Firebase CLI must already be authenticated. Receipts stay outside the repository until
copied back after publication. This script does not query the public site or use GitHub.
"""
import argparse
import datetime
import hashlib
import json
from pathlib import Path
import re
import subprocess

ROOT=Path(__file__).resolve().parents[1]
PROJECT='luma-drift-production'
RELEASE='1.16.0-rc1'
SOURCE='docs/validation/site-v1.16/release/source-manifest.json'
APK_SHA=None

def run(*args):return subprocess.check_output(args,cwd=ROOT,text=True).strip()
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def payloads():
    return {'/'+str(p.relative_to(ROOT/'public')).replace('\\','/'):p for p in (ROOT/'public').rglob('*') if p.is_file() and not any(x.startswith('.') or x=='tests' for x in p.relative_to(ROOT/'public').parts)}

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--apk-sha256',required=True)
    parser.add_argument('--commit',required=True);parser.add_argument('--output',required=True,type=Path);parser.add_argument('--deploy',action='store_true')
    args=parser.parse_args()
    global APK_SHA
    APK_SHA=args.apk_sha256
    assert re.fullmatch(r'[a-f0-9]{64}',APK_SHA),'Independent final APK hash required'
    assert re.fullmatch(r'[a-f0-9]{40}',args.commit),'Exact forty-character source commit required'
    assert run('git','rev-parse','HEAD')==args.commit,'Unexpected source commit'
    assert not run('git','status','--porcelain','--untracked-files=all'),'Checkout must be clean'
    output=args.output.resolve()
    assert ROOT.resolve()!=output and ROOT.resolve() not in output.parents,'Deployment receipt must be outside checkout'
    source=json.loads((ROOT/SOURCE).read_text())
    assert source['status']=='PASS' and source.get('release')==RELEASE and source.get('edition')=='1.16','Current offline audit must pass'
    assert source.get('production_apk_sha256')==APK_SHA,'Audited APK is not the delivered production candidate'
    assert source.get('frozen_inputs'),'Audit must freeze checker, tests, original screenshots and native reports'
    for name,expected in source['frozen_inputs'].items():
        path=(ROOT/name).resolve()
        assert ROOT.resolve() in path.parents and path.is_file() and sha(path)==expected,'Audited input changed: '+name
    assert sha(ROOT/'firebase.json')==source['firebase_config_sha256'],'Firebase configuration changed after audit'
    cfg=json.loads((ROOT/'firebase.json').read_text())
    assert cfg['hosting']['site']==PROJECT and cfg['hosting']['public']=='public','Unexpected Hosting destination'
    actual=payloads()
    assert set(actual)==set(source['files']),'Public payload set changed after audit'
    for name,path in actual.items():
        expected=source['files'][name]
        assert path.stat().st_size==expected['bytes'] and sha(path)==expected['sha256'],'Public payload changed: '+name
    release_path=ROOT/'releases'/('manifest-'+RELEASE+'.json')
    release=json.loads(release_path.read_text())
    artifact_commit=source.get('artifact_source_commit','')
    assert re.fullmatch(r'[a-f0-9]{40}',artifact_commit),'Immutable local artifact commit required'
    for suffix in ('apk','aab'):
        name='luma-drift-'+RELEASE+'.'+suffix;entry=release['artifacts'][name]
        assert sha(ROOT/'releases'/name)==sha(ROOT/'public/releases'/name)==entry['sha256'],'Canonical and hosted release differ: '+name
        committed=subprocess.check_output(['git','show',artifact_commit+':releases/'+name],cwd=ROOT)
        assert hashlib.sha256(committed).hexdigest()==entry['sha256'] and len(committed)==entry['bytes'],'Immutable local artifact commit differs: '+name
    record={
        'status':'PASS','recorded_at_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),
        'edition':'1.16','android_release':RELEASE,'source_commit':args.commit,'artifact_source_commit':artifact_commit,
        'production_apk_sha256':APK_SHA,'source_public_tree':run('git','rev-parse','HEAD:public'),
        'source_checkout_clean':True,'hosting_payloads':len(actual),'payload_bytes':sum(x['bytes'] for x in source['files'].values()),
        'source_manifest':SOURCE,'source_manifest_sha256':sha(ROOT/SOURCE),'all_source_payloads_hash_verified_on_deployment_checkout':True,
        'public_origin_requested':False,'github_publication_required':False,'firebase_cli_version':run('firebase','--version'),
    }
    output.mkdir(parents=True,exist_ok=True);(output/'deployment-preflight.json').write_text(json.dumps(record,indent=2)+'\n')
    print(json.dumps(record),flush=True)
    if not args.deploy:return
    message='Website1.16 from '+args.commit
    result=subprocess.run(['firebase','deploy','--only','hosting','--project',PROJECT,'--non-interactive','--message',message],cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    (output/'deploy-cli-excerpt.txt').write_text(result.stdout);print(result.stdout,flush=True)
    assert result.returncode==0 and 'Deploy complete!' in result.stdout,'Firebase CLI did not confirm completion'
    channels=json.loads(run('firebase','hosting:channel:list','--project',PROJECT,'--json'))
    assert channels['status']=='success','Hosting management query failed'
    live=next(c for c in channels['result']['channels'] if c['name'].endswith('/channels/live'))
    deployed=live['release'];version=deployed['version']
    assert deployed['type']=='DEPLOY' and version['status']=='FINALIZED' and deployed['message']==message,'Live management identity differs from deployed source'
    safe={'channel':live['name'],'url':live['url'],'release_name':deployed['name'],'release_type':deployed['type'],'release_time':deployed['releaseTime'],'release_message':deployed['message'],'version_name':version['name'],'version_status':version['status'],'management_provider':'authenticated firebase hosting:channel:list --json','headers':version['config'].get('headers',[])}
    (output/'live-management.json').write_text(json.dumps(safe,indent=2)+'\n')
    record.update(site=PROJECT,hosting_url=live['url'],cli_deploy_complete=True,live_management=safe,public_origin_byte_or_render_verified=False,
                  scope='Exact clean commit and every Hosting payload hash verified before Hosting-only deployment. Firebase management confirms DEPLOY/FINALIZED. Public response bytes and browser rendering are separate checks; no Play/provider transaction is inferred.')
    (output/'deployment.json').write_text(json.dumps(record,indent=2)+'\n');print('LUMA_HOSTING_RECEIPT '+json.dumps(record),flush=True)

if __name__=='__main__':main()
