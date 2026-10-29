#!/usr/bin/env python3
"""Deploy an exact clean1.19 site commit to existing Firebase; 1.19 APKs are external manifest-verified bytes.

Nothing deploys without --deploy. Receipts are outside the checkout. Management
confirmation does not claim that public response bytes or browser rendering passed.
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
RELEASE='1.19.0-prototype2'
SOURCE='docs/validation/site-v1.19/release/source-manifest.json'


def run(*args):return subprocess.check_output(args,cwd=ROOT,text=True).strip()
def sha(path):
    digest=hashlib.sha256()
    with path.open('rb') as source:
        for chunk in iter(lambda:source.read(1024*1024),b''):digest.update(chunk)
    return digest.hexdigest()
def payloads():
    return {'/'+str(path.relative_to(ROOT/'public')):path for path in (ROOT/'public').rglob('*') if path.is_file() and not any(part.startswith('.') or part=='tests' for part in path.relative_to(ROOT/'public').parts)}
def safe_file(name):
    path=(ROOT/name).resolve()
    assert ROOT.resolve() in path.parents and path.is_file(),'Missing bounded input: '+name
    return path


def validate_artifact_manifest(source, expected_sha):
    """The commit proves metadata identity, not presence of a >100MiB APK Git blob."""
    artifact_commit=source['artifact_source_commit'];manifest_name=source['artifact_manifest']
    assert re.fullmatch('[a-f0-9]{40}',artifact_commit),'Immutable artifact metadata commit required'
    manifest_path=safe_file(manifest_name)
    assert sha(manifest_path)==source['artifact_manifest_sha256'],'Canonical artifact manifest changed'
    committed=subprocess.check_output(['git','show',artifact_commit+':'+manifest_name],cwd=ROOT)
    assert hashlib.sha256(committed).hexdigest()==source['artifact_manifest_sha256'],'Immutable artifact metadata differs'
    release=json.loads(committed)
    expected={'production':expected_sha,'lab':source['lab_apk_sha256']}
    for channel,digest in expected.items():
        entry=release['apks'][channel]
        assert entry['sha256']==digest and entry['code']==27,'1.19 channel identity differs: '+channel
        assert entry['signer_certificate_sha256']=='dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287','Unexpected release signer metadata'
        public=ROOT/'public/releases'/Path(entry['path']).name
        assert public.is_file() and sha(public)==digest and public.stat().st_size==entry['bytes'],'Ignored physical 1.19 download differs: '+str(public)
        relative=str(public.relative_to(ROOT))
        assert subprocess.run(['git','ls-files','--error-unmatch',relative],cwd=ROOT,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode!=0,'1.19 APK must not be a Git blob'
        assert subprocess.run(['git','check-ignore','--quiet',relative],cwd=ROOT).returncode==0,'Physical 1.19 APK must be explicitly ignored'
    return artifact_commit


def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--apk-sha256',required=True);p.add_argument('--commit',required=True)
    p.add_argument('--output',required=True,type=Path);p.add_argument('--deploy',action='store_true')
    a=p.parse_args();assert re.fullmatch('[a-f0-9]{64}',a.apk_sha256) and re.fullmatch('[a-f0-9]{40}',a.commit)
    assert run('git','rev-parse','HEAD')==a.commit,'Unexpected deployment source commit'
    assert not run('git','status','--porcelain','--untracked-files=all'),'Deployment checkout must be clean'
    output=a.output.resolve();assert output!=ROOT and ROOT.resolve() not in output.parents,'Receipts must be outside checkout'
    assert not output.exists(),'Use a new receipt directory; earlier attempts remain evidence'
    source=json.loads(safe_file(SOURCE).read_text())
    assert source.get('native_capture_provenance_verified') and source.get('package_identity_verified'),'Native/publication prerequisites missing'
    assert source['status']=='PASS' and source['edition']=='1.19' and source['release']==RELEASE
    assert source['production_apk_sha256']==a.apk_sha256 and source['frozen_inputs'],'Wrong or incomplete audited 1.19 payload'
    # The payload manifest itself must belong to the exact deployment commit.
    committed=subprocess.check_output(['git','show',a.commit+':'+SOURCE],cwd=ROOT)
    assert hashlib.sha256(committed).hexdigest()==sha(ROOT/SOURCE),'Source manifest is not in exact deployment commit'
    for name,digest in source['frozen_inputs'].items():assert sha(safe_file(name))==digest,'Frozen audit input changed: '+name
    assert sha(ROOT/'firebase.json')==source['firebase_config_sha256']
    config=json.loads((ROOT/'firebase.json').read_text())
    assert config['hosting']['site']==PROJECT and config['hosting']['public']=='public','Unexpected existing Hosting destination'
    actual=payloads();assert set(actual)==set(source['files']),'Hosting payload file set changed'
    for name,path in actual.items():
        entry=source['files'][name];assert sha(path)==entry['sha256'] and path.stat().st_size==entry['bytes'],'Hosting payload changed: '+name
    artifact_commit=validate_artifact_manifest(source,a.apk_sha256)
    record={'status':'PASS','recorded_at_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'edition':'1.19','android_release':RELEASE,'source_commit':a.commit,'artifact_source_commit':artifact_commit,'production_apk_sha256':a.apk_sha256,'lab_apk_sha256':source['lab_apk_sha256'],'source_public_tree':run('git','rev-parse','HEAD:public'),'source_checkout_clean':True,'hosting_payloads':len(actual),'payload_bytes':sum(entry['bytes'] for entry in source['files'].values()),'source_manifest':SOURCE,'source_manifest_sha256':sha(ROOT/SOURCE),'all_source_payloads_hash_verified_on_deployment_checkout':True,'apk_git_blobs_required':False,'external_apk_metadata_and_physical_copies_verified':True,'public_origin_requested':False,'github_publication_required':False,'firebase_cli_version':run('firebase','--version')}
    output.mkdir(parents=True);(output/'deployment-preflight.json').write_text(json.dumps(record,indent=2)+'\n');print(json.dumps(record),flush=True)
    if not a.deploy:return
    message='Website1.19 from '+a.commit
    result=subprocess.run(['firebase','deploy','--only','hosting','--project',PROJECT,'--non-interactive','--message',message],cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    (output/'deploy-cli-excerpt.txt').write_text(result.stdout);print(result.stdout,flush=True)
    assert result.returncode==0 and 'Deploy complete!' in result.stdout,'CLI did not confirm deployment'
    channels=json.loads(run('firebase','hosting:channel:list','--project',PROJECT,'--json'));assert channels['status']=='success'
    live=next(channel for channel in channels['result']['channels'] if channel['name'].endswith('/channels/live'))
    deployed=live['release'];version=deployed['version']
    assert deployed['type']=='DEPLOY' and version['status']=='FINALIZED' and deployed['message']==message,'Live management identity differs'
    safe={'channel':live['name'],'url':live['url'],'release_name':deployed['name'],'release_type':deployed['type'],'release_time':deployed['releaseTime'],'release_message':deployed['message'],'version_name':version['name'],'version_status':version['status'],'management_provider':'authenticated firebase hosting:channel:list --json','headers':version['config'].get('headers',[])}
    (output/'live-management.json').write_text(json.dumps(safe,indent=2)+'\n')
    record.update(site=PROJECT,hosting_url=live['url'],cli_deploy_complete=True,live_management=safe,public_origin_byte_or_render_verified=False,scope='Clean exact source commit and every physical Hosting file, including external 1.19 APK copies, verified before Hosting-only deployment. Management confirms DEPLOY/FINALIZED. Public HTTP bytes and rendering are separate; no Play/provider transaction is inferred.')
    (output/'deployment.json').write_text(json.dumps(record,indent=2)+'\n');print('LUMA_HOSTING_RECEIPT '+json.dumps(record),flush=True)


if __name__=='__main__':main()
