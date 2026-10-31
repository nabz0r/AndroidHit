#!/usr/bin/env python3
"""Deploy an exact clean 1.24 site commit to existing Firebase; 1.24 APKs are external manifest-verified bytes.

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
import urllib.parse

ROOT=Path(__file__).resolve().parents[1]
PROJECT='luma-drift-production'
RELEASE='1.24.0-rc2'
SOURCE='docs/validation/site-v1.24/release/source-manifest.json'
BASELINE_SOURCE='docs/validation/site-v1.23/release/source-manifest.json'
PREVIOUS123='5cc870701c8039e664d1c00a90d0586c3d00521c'
PREVIOUS123_LIVE='sites/luma-drift-production/versions/cf8b817321bf33d5'
PREVIOUS123_DEPLOY_SOURCE = 'c99ac52903eca6707d3f4d644d7c480c79b63e68'
PREVIOUS123_PUBLIC_TREE = '62e59825f561bb4857510d3e602d43ec87535dd5'
REQUIRED_ART = frozenset('/assets/art-v124/' + name for name in ['arrows.webp', 'icons.webp', 'locks.webp', 'elements.webp', 'materials.webp', 'gear-head.webp', 'gear-aura.webp', 'gear-charm.webp', 'world-adventure.webp', 'world-collection.webp', 'world-atelier.webp', 'Nunito.ttf', 'OFL-Nunito.txt', 'furniture-0.webp', 'furniture-1.webp', 'furniture-2.webp', 'furniture-3.webp', 'furniture-4.webp', 'furniture-5.webp', 'furniture-6.webp', 'furniture-7.webp'])

SIGNER='dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287'
RETIRED_PATHS = frozenset()
SCREEN_KEYS=('puzzle','map','companion','home')
REQUIRED_ADDITIONS=frozenset(
    ['/assets/site-v124.css','/assets/site-v124.js',
     '/releases/manifest-1.24.0-rc2.json',
     '/releases/luma-drift-1.24.0-rc2.apk',
     '/releases/luma-drift-1.24.0-sandbox-rc2.apk',
     '/lab/clarity-124/index.html']+
    ['/assets/screenshots/v1.24/'+key+'-'+lang+'.png'
     for key in SCREEN_KEYS for lang in ('fr','en')]) | REQUIRED_ART


def validate_files(files):
    assert isinstance(files,dict) and files,'A complete physical source map is required'
    for name,row in files.items():
        assert isinstance(name,str) and re.fullmatch(r'/[A-Za-z0-9._/-]+',name) and not name.startswith('//') and all(part not in ('','.','..') for part in name[1:].split('/')),'Unbounded public path: '+str(name)
        assert isinstance(row,dict) and set(row)=={'sha256','bytes'} and re.fullmatch('[a-f0-9]{64}',row['sha256']) and type(row['bytes']) is int and 0<row['bytes']<=1024**3,'Invalid payload identity: '+name


def allowed_addition(name):
    return name in REQUIRED_ADDITIONS or name.startswith('/lab/clarity-124/') or bool(re.fullmatch(r'/releases/deltas/1\.24\.0-rc2/(production|lab)(-build\.json|\.json\.gz)', name))



def run(*args):return subprocess.check_output(args,cwd=ROOT,text=True).strip()
def sha(path):
    digest=hashlib.sha256()
    with path.open('rb') as source:
        for chunk in iter(lambda:source.read(1024*1024),b''):digest.update(chunk)
    return digest.hexdigest()
def payloads():
    return {'/'+str(path.relative_to(ROOT/'public')):path for path in (ROOT/'public').rglob('*') if path.is_file() and not any(part.startswith('.') or part=='tests' for part in path.relative_to(ROOT/'public').parts)}
def safe_file(name):
    assert isinstance(name,str) and name and '\\' not in name and ':' not in name and not name.startswith('/') and all(part not in ('','.','..') for part in name.split('/')),'Invalid bounded input: '+str(name)
    parts=name.split('/');path=(ROOT/name).resolve()
    assert not any(ROOT.joinpath(*parts[:n]).is_symlink() for n in range(1,len(parts)+1)),'Symlink input refused: '+name
    assert ROOT.resolve() in path.parents and path.is_file(),'Missing bounded input: '+name
    return path


def committed_source_bytes(commit):
    assert re.fullmatch('[a-f0-9]{40}',commit),'Exact previous source commit required'
    for baseline_commit in (commit, PREVIOUS123_DEPLOY_SOURCE):
        assert run('git','rev-parse',baseline_commit+':public') == PREVIOUS123_PUBLIC_TREE, 'Accepted1.23 closeout/deployment public-tree bridge differs'
    return subprocess.check_output(['git','show',commit+':'+BASELINE_SOURCE],cwd=ROOT)


def deployment_baseline(source,previous_source_commit=PREVIOUS123):
    assert previous_source_commit==PREVIOUS123,'Only the exact accepted 1.23 closeout source is a baseline'
    data=committed_source_bytes(previous_source_commit);previous=json.loads(data)
    assert previous['status']=='PASS' and previous['edition']=='1.23' and previous['release']=='1.23.0-rc2','Previous committed source is not accepted 1.23 closeout'
    assert source['status']=='PASS' and source['edition']=='1.24' and source['release']==RELEASE,'Accepted 1.24 source required'
    assert source['native_capture_provenance_verified'] and source['package_identity_verified'],'Exact native/artifact qualification required'
    assert source['firebase_config_sha256']==previous['firebase_config_sha256'],'Hosting config must remain unchanged'
    before,current=previous['files'],source['files'];validate_files(before);validate_files(current)
    assert len(before)==576 and set(before)-set(current)==RETIRED_PATHS,'Historical public files may not be deleted'
    assert source.get('retired_files', {})=={name:before[name] for name in RETIRED_PATHS},'1.24 does not retire historical payloads'
    changed=sorted(name for name in before if name in current and before[name]!=current[name]);added=sorted(set(current)-set(before))
    assert changed==['/index.html'],'Only the homepage may overwrite an existing 1.23 payload'
    assert REQUIRED_ADDITIONS<=set(added),'Missing required 1.24 release, native captures, assets or report'
    assert all(allowed_addition(name) for name in added),'Unexpected 1.24 addition: '+str([name for name in added if not allowed_addition(name)])
    return before,changed,added,hashlib.sha256(data).hexdigest()


def configure_upload_timeout(client,helper):
    """Keep the existing authenticated/pinned/retrying client; extend uploads only."""
    original=client._opener
    class UploadOpener:
        def open(self,request,timeout=60):
            helper.Client.check_url(request.full_url)
            upload=request.get_method()=='POST' and urllib.parse.urlsplit(request.full_url).hostname==helper.UPLOAD_HOST
            return original.open(request,timeout=600 if upload else timeout)
    client._opener=UploadOpener()
    return client


def validate_artifact_manifest(source, expected_sha):
    """Verify committed code33 metadata and both ignored physical APKs, never Git APK blobs."""
    artifact_commit=source['artifact_source_commit'];manifest_name=source['artifact_manifest']
    assert re.fullmatch('[a-f0-9]{40}',artifact_commit),'Immutable artifact metadata commit required'
    assert manifest_name=='releases/manifest-1.24.0-rc2.json','Exact 1.24 canonical manifest required'
    manifest_path=safe_file(manifest_name)
    assert sha(manifest_path)==source['artifact_manifest_sha256'],'Canonical artifact manifest changed'
    committed=subprocess.check_output(['git','show',artifact_commit+':'+manifest_name],cwd=ROOT)
    assert hashlib.sha256(committed).hexdigest()==source['artifact_manifest_sha256'],'Immutable artifact metadata differs'
    release=json.loads(committed)
    assert release['edition']=='1.24' and release['version']==RELEASE and release['version_code']==33,'Exact 1.24 release metadata required'
    for channel,digest in {'production':expected_sha,'lab':source['lab_apk_sha256']}.items():
        entry=release['apks'][channel];version='1.24.0-sandbox-rc2' if channel=='lab' else RELEASE
        assert entry['sha256']==digest and entry['code']==33 and entry['version']==version and entry['path']=='releases/luma-drift-'+version+'.apk','1.24 channel identity differs: '+channel
        assert entry['signer_certificate_sha256']==SIGNER,'Unexpected release signer metadata'
        public=safe_file('public/'+entry['path']);public_entry=source['files']['/'+entry['path']]
        assert public_entry=={'sha256':digest,'bytes':entry['bytes']},'Public APK metadata differs'
        assert sha(public)==digest and public.stat().st_size==entry['bytes'],'Ignored physical 1.24 download differs: '+str(public)
        relative=str(public.relative_to(ROOT.resolve()))
        assert subprocess.run(['git','ls-files','--error-unmatch',relative],cwd=ROOT,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode!=0,'1.24 APK must not be a Git blob'
        assert subprocess.run(['git','check-ignore','--quiet',relative],cwd=ROOT).returncode==0,'Physical 1.24 APK must be explicitly ignored'
    return artifact_commit


def clone_preserving_baseline(client, old_name, output, helper):
    """Clone the complete accepted version without exclusion filters."""
    return helper.clone(client, old_name, output)


def rest_deploy(source,commit,expected_live,output,deploy,token,*,client=None,helper=None,previous_source_commit=PREVIOUS123):
    """Clone all 578 accepted1.23 provider paths; upload only the audited 1.24 overlay.

    Reuses the historical, tested pinned-host/no-redirect Firebase client. Its
    credentials stay in memory. Historical APKs are never recompressed/uploaded.
    API contract: https://firebase.google.com/docs/hosting/api-deploy
    """
    if helper is None:
        helper_path=ROOT/'scripts/deploy-cognitive-overlay.py'
        spec=importlib.util.spec_from_file_location('luma_hosting_rest124',helper_path)
        helper=importlib.util.module_from_spec(spec);spec.loader.exec_module(helper)
    assert helper.VERSION.fullmatch(expected_live),'Exact expected live version resource required'
    assert isinstance(token,str) and 20<len(token)<8192 and not any(char.isspace() for char in token),'A current OAuth token is required through stdin only'
    before,changed,added,baseline_sha=deployment_baseline(source,previous_source_commit);current=source['files']
    assert expected_live==PREVIOUS123_LIVE,'1.24 overlay requires the exact accepted1.23 live version'
    client=client if client is not None else configure_upload_timeout(helper.Client(token),helper)
    live=helper.live_channel(client,expected_live);old=helper.version(client,expected_live,'FINALIZED');old_files=helper.files(client,expected_live)
    assert live['releaseMessage']=='Website1.23 from '+PREVIOUS123_DEPLOY_SOURCE,'Live release is not the exact accepted1.23 source'
    assert set(old_files)==set(before)|set(helper.FIREBASE_RESERVED),'Live historical file set differs from exact baseline'
    overlay,blobs={},{}
    for name in changed+added:
        raw=(ROOT/'public'/name.lstrip('/')).read_bytes();entry=current[name]
        assert hashlib.sha256(raw).hexdigest()==entry['sha256'] and len(raw)==entry['bytes'],'Overlay changed: '+name
        encoded=helper.compressed(raw);digest=helper.sha(encoded)
        overlay[name]={'rawSha256':entry['sha256'],'rawBytes':entry['bytes'],'gzipSha256':digest,'gzipBytes':len(encoded)};blobs[digest]=encoded
    preserved_provider=dict(old_files)
    expected=dict(preserved_provider);expected.update({name:row['gzipSha256'] for name,row in overlay.items()})
    preflight={'status':'PASS','edition':'1.24','sourceCommit':commit,'expectedLiveVersion':expected_live,'live':live,'baselineMode':'exact-1.23-closeout-public-tree','previousSourceCommit':previous_source_commit,'previousDeploymentSourceCommit':PREVIOUS123_DEPLOY_SOURCE,'baselinePublicTree':PREVIOUS123_PUBLIC_TREE,'baselineSourceManifestSha256':baseline_sha,'historicalProviderPaths':len(old_files),'historicalPhysicalPaths':len(before),'preservedHistoricalPaths':len(old_files)-len(changed)-len(RETIRED_PATHS),'retiredPaths':sorted(RETIRED_PATHS),'retiredPhysicalPaths':len(RETIRED_PATHS),'intentionalChanges':changed,'addedPaths':added,'finalExpectedProviderPaths':len(expected),'managementTimeoutSeconds':60,'uploadTimeoutSeconds':600,'historicalApksUploaded':False,'publicOriginRequested':False,'publicOriginBytesVerified':False,'publicOriginRenderingVerified':False}
    helper.save(output,'rest-preflight.json',preflight);helper.save(output,'baseline-provider-files.json',old_files);helper.save(output,'baseline-provider-config.json',old.get('config',{}));helper.save(output,'overlay-source-and-gzip-hashes.json',overlay)
    print(json.dumps({'rest_preflight':'PASS','historicalProviderPaths':len(old_files),'changed':len(changed),'added':len(added),'deployRequested':deploy}),flush=True)
    if not deploy:return preflight
    helper.live_channel(client,expected_live)
    name=clone_preserving_baseline(client,expected_live,output,helper)
    assert helper.version(client,name,'CREATED').get('config',{})==old.get('config',{}) and helper.files(client,name)==preserved_provider,'Clone differs from exact baseline without deletion or changed config'
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
    message='Website1.24 from '+commit
    release=client.api('POST','sites/'+PROJECT+'/releases',{'message':message},versionName=name)
    assert release.get('version',{}).get('name')==name and release.get('message')==message and release.get('type')=='DEPLOY','Release identity differs; inspect management before any retry'
    after=helper.live_channel(client,name);assert after['releaseMessage']==message and helper.same_release(after['releaseName'],release.get('name')),'Live management release identity differs'
    result=dict(preflight,status='DEPLOY/FINALIZED',recordedAtUtc=datetime.datetime.now(datetime.timezone.utc).isoformat(),version=name,live=after,sourcePublicTree=run('git','rev-parse','HEAD:public'),providerServingConfigUnchanged=True,retainedHistoricalProviderHashesPreserved=True,historicalPayloadDeletions=0,uploadedOverlayBlobs=len(uploaded),releaseConcurrencyScope='Expected live checked immediately before release; API offers no atomic compare-and-swap')
    helper.save(output,'deployment.json',result);print('LUMA_HOSTING_RECEIPT '+json.dumps(result),flush=True);return result


def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--apk-sha256',required=True);p.add_argument('--commit',required=True)
    p.add_argument('--output',required=True,type=Path);p.add_argument('--deploy',action='store_true')
    p.add_argument('--transport',choices=('rest',),default='rest')
    p.add_argument('--expected-live',required=True,help='Exact accepted 1.23 closeout live version resource')
    p.add_argument('--previous-source-commit',default=PREVIOUS123,help='Pinned accepted 1.23 closeout source')
    p.add_argument('--token-stdin',action='store_true',required=True,help='Read OAuth token from stdin; never persist or print it')
    a=p.parse_args();assert re.fullmatch('[a-f0-9]{64}',a.apk_sha256) and re.fullmatch('[a-f0-9]{40}',a.commit)
    assert a.previous_source_commit==PREVIOUS123 and a.expected_live==PREVIOUS123_LIVE,'Only the exact accepted1.23 source/live baseline is permitted'
    assert run('git','rev-parse','HEAD')==a.commit,'Unexpected deployment source commit'
    assert not run('git','status','--porcelain','--untracked-files=all'),'Deployment checkout must be clean'
    output=a.output.resolve();assert output!=ROOT.resolve() and ROOT.resolve() not in output.parents,'Receipts must be outside checkout'
    assert not output.exists(),'Use a new receipt directory; earlier attempts remain evidence'
    source=json.loads(safe_file(SOURCE).read_text())
    assert source.get('native_capture_provenance_verified') and source.get('package_identity_verified'),'Native/publication prerequisites missing'
    assert source['status']=='PASS' and source['edition']=='1.24' and source['release']==RELEASE
    assert source['production_apk_sha256']==a.apk_sha256 and source['frozen_inputs'],'Wrong or incomplete audited 1.24 payload'
    committed=subprocess.check_output(['git','show',a.commit+':'+SOURCE],cwd=ROOT)
    assert hashlib.sha256(committed).hexdigest()==sha(ROOT/SOURCE),'Source manifest is not in exact deployment commit'
    for name,digest in source['frozen_inputs'].items():assert sha(safe_file(name))==digest,'Frozen audit input changed: '+name
    assert sha(ROOT/'firebase.json')==source['firebase_config_sha256']
    config=json.loads((ROOT/'firebase.json').read_text())
    assert config['hosting']['site']==PROJECT and config['hosting']['public']=='public','Unexpected existing Hosting destination'
    before,changed,added,baseline_sha=deployment_baseline(source,a.previous_source_commit)
    actual=payloads();missing=set(source['files'])-set(actual)
    assert not(set(actual)-set(source['files'])),'Unexpected physical Hosting file'
    preserved={name for name in before if name in source['files'] and before[name]==source['files'][name]}
    assert missing<=preserved,'Missing new or changed Hosting payload'
    for name,path in actual.items():
        entry=source['files'][name];assert sha(path)==entry['sha256'] and path.stat().st_size==entry['bytes'],'Hosting payload changed: '+name
    artifact_commit=validate_artifact_manifest(source,a.apk_sha256)
    record={'status':'PASS','edition':'1.24','android_release':RELEASE,'source_commit':a.commit,'artifact_source_commit':artifact_commit,'production_apk_sha256':a.apk_sha256,'lab_apk_sha256':source['lab_apk_sha256'],'source_public_tree':run('git','rev-parse','HEAD:public'),'source_checkout_clean':True,'hosting_payloads':len(source['files']),'physical_payloads_present':len(actual),'historical_raw_payloads_absent':sorted(missing),'source_manifest':SOURCE,'source_manifest_sha256':sha(ROOT/SOURCE),'previous_source_commit':PREVIOUS123,'baseline_manifest_sha256':baseline_sha,'all_source_payloads_hash_verified_on_deployment_checkout':not missing,'present_source_payloads_hash_verified':True,'new_external_apk_metadata_and_physical_copies_verified':True,'public_origin_requested':False,'transport':'rest'}
    output.mkdir(parents=True);(output/'deployment-preflight.json').write_text(json.dumps(record,indent=2)+'\n');print(json.dumps(record),flush=True)
    token=sys.stdin.readline(8193).strip()
    rest_deploy(source,a.commit,a.expected_live,output,a.deploy,token,previous_source_commit=a.previous_source_commit)


if __name__=='__main__':main()
