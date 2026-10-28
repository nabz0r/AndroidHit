#!/usr/bin/env python3
"""Deploy only Hosting from an exact clean commit; inspect authenticated management.

Run inside a clean checkout with Firebase CLI already authenticated.
No public Hosting URL is requested. Authentication stays inside Firebase CLI.
"""
import argparse, datetime, hashlib, json, pathlib, subprocess
ROOT = pathlib.Path(__file__).resolve().parents[1]
PROJECT = 'luma-drift-production'
MANIFEST = 'docs/validation/site-v1.14/release/source-manifest.json'
def run(*args):
    return subprocess.check_output(args, cwd=ROOT, text=True).strip()
def sha(p):
    return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--commit', required=True)
    parser.add_argument('--output', required=True, type=pathlib.Path)
    parser.add_argument('--deploy', action='store_true')
    args=parser.parse_args()
    assert len(args.commit)==40 and all(c in '0123456789abcdef' for c in args.commit)
    assert run('git','rev-parse','HEAD')==args.commit, 'Unexpected source commit'
    assert not run('git','status','--porcelain','--untracked-files=all'), 'Checkout must be clean'
    assert not (ROOT.resolve() == args.output.resolve() or ROOT.resolve() in args.output.resolve().parents), 'Evidence output must be outside the checkout'
    manifest=json.loads((ROOT/MANIFEST).read_text())
    assert manifest['status']=='PASS' and manifest.get('release')=='1.14.0-rc2' and manifest.get('edition')=='1.14', 'Current 1.14 offline audit must pass'
    artifact_commit=manifest.get('artifact_source_commit','')
    assert len(artifact_commit)==40 and all(c in '0123456789abcdef' for c in artifact_commit), 'An immutable artifact commit is required'
    release_path=ROOT/'releases/manifest-1.14.0-rc2.json'
    assert sha(release_path)==manifest['release_manifest_sha256'], 'Release manifest changed after website audit'
    release=json.loads(release_path.read_text())
    artifact=release['artifacts']['luma-drift-1.14.0-rc2.apk']
    apk=ROOT/'releases/luma-drift-1.14.0-rc2.apk'
    assert sha(apk)==artifact['sha256']==manifest['production_apk_sha256'] and apk.stat().st_size==artifact['bytes'], 'APK changed after website audit'
    committed=subprocess.check_output(['git','show',artifact_commit+':releases/luma-drift-1.14.0-rc2.apk'],cwd=ROOT)
    assert hashlib.sha256(committed).hexdigest()==artifact['sha256'] and len(committed)==artifact['bytes'], 'Immutable download commit contains different APK bytes'
    assert sha(ROOT/'scripts/check-site-v114.py')==manifest['runner_sha256'], 'Checker changed after website audit'
    assert sha(ROOT/'scripts/check-site-v17.py')==manifest['base_runner_sha256'], 'Shared parser changed after website audit'
    assert sha(ROOT/'docs/validation/site-v1.14/screenshot-provenance.json')==manifest['screenshot_provenance_sha256'], 'Screenshot provenance changed after website audit'
    assert sha(ROOT/'docs/validation/site-v1.14/privacy-retained-policy.json')==manifest['privacy_retention_sha256'], 'Privacy retention evidence changed after website audit'
    assert sha(ROOT/'public/tests/site-language-v114.test.mjs')==manifest['language_test_sha256'], 'Bilingual tests changed after website audit'
    assert manifest.get('native_capture_inputs'), 'Native capture inputs were not frozen'
    for name,expected in manifest['native_capture_inputs'].items():
        source=(ROOT/name).resolve()
        assert ROOT.resolve() in source.parents and source.is_file() and sha(source)==expected, 'Native evidence changed after website audit: '+name
    assert sha(ROOT/'firebase.json')==manifest['firebase_config_sha256']
    config=json.loads((ROOT/'firebase.json').read_text())
    assert config['hosting']['site']==PROJECT and config['hosting']['public']=='public'
    actual={'/'+str(p.relative_to(ROOT/'public')).replace('\\','/') for p in (ROOT/'public').rglob('*') if p.is_file() and not any(x.startswith('.') or x=='tests' for x in p.relative_to(ROOT/'public').parts)}
    assert actual==set(manifest['files']), 'Unexpected public payload set'
    for rel, expected in manifest['files'].items():
        p=ROOT/'public'/rel.lstrip('/')
        assert p.stat().st_size==expected['bytes'] and sha(p)==expected['sha256'], rel
    args.output.mkdir(parents=True,exist_ok=True)
    record={'status':'PASS','recorded_at_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'source_commit':args.commit,'artifact_source_commit':artifact_commit,'production_apk_sha256':artifact['sha256'],'source_public_tree':run('git','rev-parse','HEAD:public'),'source_checkout_clean':True,'hosting_payloads':len(actual),'payload_bytes':sum(v['bytes'] for v in manifest['files'].values()),'source_manifest':MANIFEST,'source_manifest_sha256':sha(ROOT/MANIFEST),'all_source_payloads_hash_verified_on_deployment_checkout':True,'public_origin_requested':False,'firebase_cli_version':run('firebase','--version')}
    (args.output/'deployment-preflight.json').write_text(json.dumps(record,indent=2)+'\n')
    print(json.dumps(record),flush=True)
    if not args.deploy:return
    result=subprocess.run(['firebase','deploy','--only','hosting','--project',PROJECT,'--non-interactive','--message','Website1.14 from '+args.commit],cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    (args.output/'deploy-cli-excerpt.txt').write_text(result.stdout)
    print(result.stdout,flush=True)
    assert result.returncode==0 and 'Deploy complete!' in result.stdout, 'Hosting deployment did not confirm completion'
    channels=json.loads(run('firebase','hosting:channel:list','--project',PROJECT,'--json'))
    assert channels['status']=='success'
    channel=next(c for c in channels['result']['channels'] if c['name'].endswith('/channels/live'))
    release=channel['release'];version=release['version']
    assert release['type']=='DEPLOY' and version['status']=='FINALIZED'
    assert release['message']=='Website1.14 from '+args.commit, 'Live release message differs from deployed source'
    safe={'channel':channel['name'],'url':channel['url'],'release_name':release['name'],'release_type':release['type'],'release_time':release['releaseTime'],'release_message':release['message'],'version_name':version['name'],'version_status':version['status'],'management_provider':'authenticated firebase hosting:channel:list --json','headers':version['config']['headers']}
    (args.output/'live-management.json').write_text(json.dumps(safe,indent=2)+'\n')
    record.update(site=PROJECT,hosting_url=channel['url'],edition='1.14',android_release='1.14.0-rc2',cli_deploy_complete=True,live_management=safe,public_origin_byte_or_render_verified=False,scope='Exact clean source and every Hosting payload checked before Firebase CLI completion. Live channel management reports DEPLOY/FINALIZED. Localhost browser review is separate; no public-origin byte/render or real provider acceptance is inferred.')
    (args.output/'deployment.json').write_text(json.dumps(record,indent=2)+'\n')
    print('LUMA_HOSTING_RECEIPT '+json.dumps(record),flush=True)
if __name__=='__main__':main()
