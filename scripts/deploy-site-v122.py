#!/usr/bin/env python3
"""Deploy an exact clean1.22 site commit to existing Firebase; 1.22 APKs are external manifest-verified bytes.

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
import importlib.util
import sys

ROOT=Path(__file__).resolve().parents[1]
PROJECT='luma-drift-production'
RELEASE='1.22.0-rc1'
SOURCE='docs/validation/site-v1.22/release/source-manifest.json'


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
        assert entry['sha256']==digest and entry['code']==30,'1.22 channel identity differs: '+channel
        assert entry['signer_certificate_sha256']=='dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287','Unexpected release signer metadata'
        public=ROOT/'public/releases'/Path(entry['path']).name
        assert public.is_file() and sha(public)==digest and public.stat().st_size==entry['bytes'],'Ignored physical 1.22 download differs: '+str(public)
        relative=str(public.relative_to(ROOT))
        assert subprocess.run(['git','ls-files','--error-unmatch',relative],cwd=ROOT,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode!=0,'1.22 APK must not be a Git blob'
        assert subprocess.run(['git','check-ignore','--quiet',relative],cwd=ROOT).returncode==0,'Physical 1.22 APK must be explicitly ignored'
    return artifact_commit


def rest_deploy(source,commit,expected_live,output,deploy,token,*,client=None,helper=None):
    """Clone all541 prior provider paths; upload only changed/new audited1.22 files.

    Reuses the historical, tested pinned-host/no-redirect Firebase client. Its
    credentials stay in memory. Historical APKs are never recompressed/uploaded.
    API contract: https://firebase.google.com/docs/hosting/api-deploy
    """
    if helper is None:
        helper_path=ROOT/'scripts/deploy-cognitive-overlay.py'
        spec=importlib.util.spec_from_file_location('luma_hosting_rest122',helper_path)
        helper=importlib.util.module_from_spec(spec);spec.loader.exec_module(helper)
    assert helper.VERSION.fullmatch(expected_live),'Exact expected live version resource required'
    assert isinstance(token,str) and 20<len(token)<8192 and not any(char.isspace() for char in token),'A current OAuth token is required through stdin only'
    client=client if client is not None else helper.Client(token)
    baseline_path=ROOT/'docs/validation/cognitive-lab/site-manifest.json';baseline=json.loads(baseline_path.read_text())
    before=baseline['files'];current=source['files']
    assert len(before)==539 and set(before)<=set(current),'Retain all539 historical physical paths plus2Firebase-managed paths'
    changed=sorted(name for name in before if before[name]!=current[name]);added=sorted(set(current)-set(before))
    allowed={'/index.html','/lab/cognitive/index.html','/lab/cognitive/lab.js','/lab/cognitive/lab.css'}
    assert set(changed)<=allowed,'Unexpected historical overwrite: '+str(sorted(set(changed)-allowed))
    live=helper.live_channel(client,expected_live);old=helper.version(client,expected_live,'FINALIZED');old_files=helper.files(client,expected_live)
    assert set(old_files)==set(before)|set(helper.FIREBASE_RESERVED),'Live historical file set differs from exact baseline'
    overlay,blobs={},{}
    for name in changed+added:
        raw=(ROOT/'public'/name.lstrip('/')).read_bytes();entry=current[name]
        assert hashlib.sha256(raw).hexdigest()==entry['sha256'] and len(raw)==entry['bytes'],'Overlay changed: '+name
        encoded=helper.compressed(raw);digest=helper.sha(encoded)
        overlay[name]={'rawSha256':entry['sha256'],'rawBytes':entry['bytes'],'gzipSha256':digest,'gzipBytes':len(encoded)};blobs[digest]=encoded
    expected=dict(old_files);expected.update({name:row['gzipSha256'] for name,row in overlay.items()})
    preflight={'status':'PASS','edition':'1.22','sourceCommit':commit,'expectedLiveVersion':expected_live,'live':live,'historicalProviderPaths':len(old_files),'historicalPhysicalPaths':len(before),'preservedHistoricalPaths':len(old_files)-len(changed),'intentionalChanges':changed,'addedPaths':added,'finalExpectedProviderPaths':len(expected),'historicalApksUploaded':False,'publicOriginRequested':False,'publicOriginBytesVerified':False,'publicOriginRenderingVerified':False}
    helper.save(output,'rest-preflight.json',preflight);helper.save(output,'baseline-provider-files.json',old_files);helper.save(output,'baseline-provider-config.json',old.get('config',{}));helper.save(output,'overlay-source-and-gzip-hashes.json',overlay)
    print(json.dumps({'rest_preflight':'PASS','historicalProviderPaths':len(old_files),'changed':len(changed),'added':len(added),'deployRequested':deploy}),flush=True)
    if not deploy:return preflight
    helper.live_channel(client,expected_live)
    name=helper.clone(client,expected_live,output)
    assert helper.version(client,name,'CREATED').get('config',{})==old.get('config',{}) and helper.files(client,name)==old_files,'Clone did not preserve every historical provider hash/config'
    assert not helper.files(client,name,'EXPECTED'),'Historical cloned files unexpectedly need uploads'
    required=[];upload_base='https://'+helper.UPLOAD_HOST+'/upload/'+name+'/files'
    rows=list(overlay.items())
    for offset in range(0,len(rows),1000):
        response=client.api('POST',name+':populateFiles',{'files':{path:row['gzipSha256'] for path,row in rows[offset:offset+1000]}})
        assert response.get('uploadUrl')==upload_base,'Unexpected pinned upload resource'
        hashes=response.get('uploadRequiredHashes',[]);assert isinstance(hashes,list) and set(hashes)<=set(blobs),'Provider requested non-overlay bytes'
        required.extend(hashes)
    required=sorted(set(required));uploaded=[]
    for digest in required:
        assert helper.sha(blobs[digest])==digest,'Compressed upload bytes changed'
        client.request('POST',upload_base+'/'+digest,blobs[digest],upload=True);uploaded.append(digest)
        helper.save(output,'uploads.json',{'version':name,'requiredHashes':required,'uploadedHashes':uploaded,'historicalBlobsUploaded':0})
        print(json.dumps({'uploaded':len(uploaded),'required':len(required)}),flush=True)
    actual=helper.files(client,name);assert actual==expected and not helper.files(client,name,'EXPECTED'),'Provider hashes differ or upload remains pending'
    helper.save(output,'verified-provider-files.json',actual)
    finalized=client.api('PATCH',name,{'status':'FINALIZED'},updateMask='status')
    assert finalized.get('name')==name and finalized.get('status')=='FINALIZED' and finalized.get('config',{})==old.get('config',{}),'Finalized version/config differs'
    assert helper.files(client,name)==expected,'Finalized provider files differ'
    if 'fileCount' in finalized:assert int(finalized['fileCount'])==len(expected),'Finalized file count differs'
    helper.save(output,'finalized-version.json',{'name':name,'status':'FINALIZED','fileCount':finalized.get('fileCount'),'finalizeTime':finalized.get('finalizeTime')})
    helper.live_channel(client,expected_live)
    message='Website1.22 from '+commit
    release=client.api('POST','sites/'+PROJECT+'/releases',{'message':message},versionName=name)
    assert release.get('version',{}).get('name')==name and release.get('message')==message and release.get('type')=='DEPLOY','Release identity differs; inspect management before any retry'
    after=helper.live_channel(client,name);assert after['releaseMessage']==message and helper.same_release(after['releaseName'],release.get('name')),'Live management release identity differs'
    result=dict(preflight,status='DEPLOY/FINALIZED',recordedAtUtc=datetime.datetime.now(datetime.timezone.utc).isoformat(),version=name,live=after,sourcePublicTree=run('git','rev-parse','HEAD:public'),providerServingConfigUnchanged=True,historicalProviderHashesPreserved=True,uploadedOverlayBlobs=len(uploaded),releaseConcurrencyScope='Expected live checked immediately before release; API offers no atomic compare-and-swap')
    helper.save(output,'deployment.json',result);print('LUMA_HOSTING_RECEIPT '+json.dumps(result),flush=True);return result


def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--apk-sha256',required=True);p.add_argument('--commit',required=True)
    p.add_argument('--output',required=True,type=Path);p.add_argument('--deploy',action='store_true')
    p.add_argument('--transport',choices=('cli','rest'),default='cli')
    p.add_argument('--expected-live',help='Required REST precondition: exact sites/.../versions/... resource')
    p.add_argument('--token-stdin',action='store_true',help='Read one current OAuth token from stdin; never persist or print it')
    a=p.parse_args();assert re.fullmatch('[a-f0-9]{64}',a.apk_sha256) and re.fullmatch('[a-f0-9]{40}',a.commit)
    assert run('git','rev-parse','HEAD')==a.commit,'Unexpected deployment source commit'
    assert not run('git','status','--porcelain','--untracked-files=all'),'Deployment checkout must be clean'
    output=a.output.resolve();assert output!=ROOT and ROOT.resolve() not in output.parents,'Receipts must be outside checkout'
    assert not output.exists(),'Use a new receipt directory; earlier attempts remain evidence'
    source=json.loads(safe_file(SOURCE).read_text())
    assert source.get('native_capture_provenance_verified') and source.get('package_identity_verified'),'Native/publication prerequisites missing'
    assert source['status']=='PASS' and source['edition']=='1.22' and source['release']==RELEASE
    assert source['production_apk_sha256']==a.apk_sha256 and source['frozen_inputs'],'Wrong or incomplete audited 1.22 payload'
    # The payload manifest itself must belong to the exact deployment commit.
    committed=subprocess.check_output(['git','show',a.commit+':'+SOURCE],cwd=ROOT)
    assert hashlib.sha256(committed).hexdigest()==sha(ROOT/SOURCE),'Source manifest is not in exact deployment commit'
    for name,digest in source['frozen_inputs'].items():assert sha(safe_file(name))==digest,'Frozen audit input changed: '+name
    assert sha(ROOT/'firebase.json')==source['firebase_config_sha256']
    config=json.loads((ROOT/'firebase.json').read_text())
    assert config['hosting']['site']==PROJECT and config['hosting']['public']=='public','Unexpected existing Hosting destination'
    actual=payloads()
    baseline=json.loads((ROOT/'docs/validation/cognitive-lab/site-manifest.json').read_text())['files'] if a.transport=='rest' else {}
    missing=set(source['files'])-set(actual)
    assert not (set(actual)-set(source['files'])),'Unexpected physical Hosting file'
    assert not missing or a.transport=='rest' and missing<=set(baseline),'Missing nonhistorical Hosting payload'
    for name,path in actual.items():
        entry=source['files'][name];assert sha(path)==entry['sha256'] and path.stat().st_size==entry['bytes'],'Hosting payload changed: '+name
    artifact_commit=validate_artifact_manifest(source,a.apk_sha256)
    record={'status':'PASS','recorded_at_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'edition':'1.22','android_release':RELEASE,'source_commit':a.commit,'artifact_source_commit':artifact_commit,'production_apk_sha256':a.apk_sha256,'lab_apk_sha256':source['lab_apk_sha256'],'source_public_tree':run('git','rev-parse','HEAD:public'),'source_checkout_clean':True,'hosting_payloads':len(source['files']),'physical_payloads_present':len(actual),'historical_raw_payloads_absent':sorted(missing),'payload_bytes':sum(entry['bytes'] for entry in source['files'].values()),'source_manifest':SOURCE,'source_manifest_sha256':sha(ROOT/SOURCE),'all_source_payloads_hash_verified_on_deployment_checkout':not missing,'present_source_payloads_hash_verified':True,'apk_git_blobs_required':False,'new_external_apk_metadata_and_physical_copies_verified':True,'public_origin_requested':False,'github_publication_required':False,'transport':a.transport}
    output.mkdir(parents=True);(output/'deployment-preflight.json').write_text(json.dumps(record,indent=2)+'\n');print(json.dumps(record),flush=True)
    if a.transport=='rest':
        assert a.expected_live and a.token_stdin,'REST requires an exact expected-live resource and token through stdin'
        token=sys.stdin.readline(8193).strip();rest_deploy(source,a.commit,a.expected_live,output,a.deploy,token);return
    if not a.deploy:return
    message='Website1.22 from '+a.commit
    result=subprocess.run(['firebase','deploy','--only','hosting','--project',PROJECT,'--non-interactive','--message',message],cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    (output/'deploy-cli-excerpt.txt').write_text(result.stdout);print(result.stdout,flush=True)
    assert result.returncode==0 and 'Deploy complete!' in result.stdout,'CLI did not confirm deployment'
    channels=json.loads(run('firebase','hosting:channel:list','--project',PROJECT,'--json'));assert channels['status']=='success'
    live=next(channel for channel in channels['result']['channels'] if channel['name'].endswith('/channels/live'))
    deployed=live['release'];version=deployed['version']
    assert deployed['type']=='DEPLOY' and version['status']=='FINALIZED' and deployed['message']==message,'Live management identity differs'
    safe={'channel':live['name'],'url':live['url'],'release_name':deployed['name'],'release_type':deployed['type'],'release_time':deployed['releaseTime'],'release_message':deployed['message'],'version_name':version['name'],'version_status':version['status'],'management_provider':'authenticated firebase hosting:channel:list --json','headers':version['config'].get('headers',[])}
    (output/'live-management.json').write_text(json.dumps(safe,indent=2)+'\n')
    record.update(site=PROJECT,hosting_url=live['url'],cli_deploy_complete=True,live_management=safe,public_origin_byte_or_render_verified=False,scope='Clean exact source commit and every physical Hosting file, including external 1.22 APK copies, verified before Hosting-only deployment. Management confirms DEPLOY/FINALIZED. Public HTTP bytes and rendering are separate; no Play/provider transaction is inferred.')
    (output/'deployment.json').write_text(json.dumps(record,indent=2)+'\n');print('LUMA_HOSTING_RECEIPT '+json.dumps(record),flush=True)


if __name__=='__main__':main()
