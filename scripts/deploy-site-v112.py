#!/usr/bin/env python3
"""Deploy only Hosting from an exact clean commit; inspect authenticated management.

Run inside a clean checkout with Firebase CLI and gcloud already authenticated.
No public Hosting URL is requested. Tokens stay in process memory and are never logged.
"""
import argparse, datetime, hashlib, json, pathlib, subprocess, urllib.request
ROOT = pathlib.Path(__file__).resolve().parents[1]
PROJECT = 'luma-drift-production'
MANIFEST = 'docs/validation/site-v1.12/release/source-manifest.json'
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
    assert manifest['status']=='PASS'
    assert sha(ROOT/'firebase.json')==manifest['firebase_config_sha256']
    config=json.loads((ROOT/'firebase.json').read_text())
    assert config['hosting']['site']==PROJECT and config['hosting']['public']=='public'
    actual={'/'+str(p.relative_to(ROOT/'public')).replace('\\','/') for p in (ROOT/'public').rglob('*') if p.is_file() and not any(x.startswith('.') or x=='tests' for x in p.relative_to(ROOT/'public').parts)}
    assert actual==set(manifest['files']), 'Unexpected public payload set'
    for rel, expected in manifest['files'].items():
        p=ROOT/'public'/rel.lstrip('/')
        assert p.stat().st_size==expected['bytes'] and sha(p)==expected['sha256'], rel
    args.output.mkdir(parents=True,exist_ok=True)
    record={'status':'PASS','recorded_at_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'source_commit':args.commit,'source_public_tree':run('git','rev-parse','HEAD:public'),'source_checkout_clean':True,'hosting_payloads':len(actual),'payload_bytes':sum(v['bytes'] for v in manifest['files'].values()),'source_manifest':MANIFEST,'source_manifest_sha256':sha(ROOT/MANIFEST),'all_source_payloads_hash_verified_on_deployment_checkout':True,'public_origin_requested':False,'firebase_cli_version':run('firebase','--version')}
    (args.output/'deployment-preflight.json').write_text(json.dumps(record,indent=2)+'\n')
    print(json.dumps(record),flush=True)
    if not args.deploy:return
    result=subprocess.run(['firebase','deploy','--only','hosting','--project',PROJECT,'--non-interactive','--message','Website1.12 from '+args.commit],cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    (args.output/'deploy-cli-excerpt.txt').write_text(result.stdout)
    print(result.stdout,flush=True)
    assert result.returncode==0 and 'Deploy complete!' in result.stdout, 'Hosting deployment did not confirm completion'
    token=run('gcloud','auth','print-access-token')
    def get(path):
        req=urllib.request.Request('https://firebasehosting.googleapis.com/v1beta1/'+path,headers={'Authorization':'Bearer '+token})
        with urllib.request.urlopen(req,timeout=45) as response:return json.load(response)
    channel=get('projects/'+PROJECT+'/sites/'+PROJECT+'/channels/live')
    release=channel['release'];version=release['version']
    if version.get('status')!='FINALIZED':version=get(version['name'])
    assert release['type']=='DEPLOY' and version['status']=='FINALIZED'
    safe={'channel':channel['name'],'url':channel['url'],'release_name':release['name'],'release_type':release['type'],'release_time':release['releaseTime'],'version_name':version['name'],'version_status':version['status']}
    (args.output/'live-management.json').write_text(json.dumps(safe,indent=2)+'\n')
    record.update(site=PROJECT,hosting_url=channel['url'],edition='1.12',android_release='1.12.0-rc2',cli_deploy_complete=True,live_management=safe,public_origin_byte_or_render_verified=False,scope='Exact clean source and every Hosting payload checked before Firebase CLI completion. Live channel management reports DEPLOY/FINALIZED. Localhost browser review is separate; no public-origin byte/render or real provider acceptance is inferred.')
    (args.output/'deployment.json').write_text(json.dumps(record,indent=2)+'\n')
    print('LUMA_HOSTING_RECEIPT '+json.dumps(record),flush=True)
if __name__=='__main__':main()
