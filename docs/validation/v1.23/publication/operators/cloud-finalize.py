#!/usr/bin/env python3
"""Finalize one exact STAGED_ONLY Cloud Shell workspace; never clone or restage.

--check-only performs local validation without auth/output writes. --deploy gets
one gcloud token in RAM and passes it only through the committed deployer's stdin.
Original operator/staging files are never modified; final receipts are exclusive.
"""
import argparse
import datetime
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys

REPO = 'https://github.com/nabz0r/AndroidHit.git'
SOURCE = 'docs/validation/site-v1.23/release/source-manifest.json'
PREVIOUS = 'b122c0acbd172a7912c69b0b6914b8b0f9b2a243'
LIVE = 'sites/luma-drift-production/versions/24e34cf6e6af1ed8'
PROD = '429a443c9228787c7b854d36a40f0bcf7d52fdc8a3601214cd27b5cfae6945fb'
LAB = '099d4dc7d38cb4552b3b1ad46f7e3ccb70b304c7c8a1c6c332e16ada00ba558c'
SIGNER = 'dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287'
OPERATOR_SHA = '595e6328a0d2f80e5f91e188bc9f6c43d604b3278a14aea51a00dc29ee8135be'
PROD_BYTES, LAB_BYTES = 369002457, 368985756


def require(ok, message):
    if not ok:
        raise RuntimeError(message)


def sha(path):
    h = hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda:stream.read(1024*1024), b''):
            h.update(block)
    return h.hexdigest()


def regular(path):
    require(path.is_file() and not path.is_symlink(), 'Regular file required: ' + str(path))
    return path


def safe_file(root, relative):
    require(isinstance(relative, str) and re.fullmatch(r'[A-Za-z0-9._/-]+', relative) and
            not relative.startswith('/') and all(x not in ('', '.', '..') for x in relative.split('/')),
            'Bounded normalized input path required')
    parts = relative.split('/')
    require(not any(root.joinpath(*parts[:n]).is_symlink() for n in range(1,len(parts)+1)), 'Symlink input refused')
    return regular(root / relative)


def environment():
    env = os.environ.copy()
    for key in list(env):
        if key.startswith(('GIT_TRACE','GIT_CONFIG_')) or key in ('GIT_CURL_VERBOSE','GIT_ASKPASS','SSH_ASKPASS'):
            env.pop(key, None)
    env.update(CLOUDSDK_CORE_DISABLE_PROMPTS='1', GIT_TERMINAL_PROMPT='0', GIT_CONFIG_NOSYSTEM='1',
               GIT_CONFIG_GLOBAL=os.devnull, GIT_CONFIG_SYSTEM=os.devnull, GIT_ASKPASS='/bin/false',
               GIT_NO_LAZY_FETCH='1')
    return env


def validate(workspace, commit, env):
    require(re.fullmatch(r'[a-f0-9]{40}', commit), 'Exact C2 SHA required')
    work = Path(workspace)
    require(work.is_absolute() and work == work.resolve() and work.is_dir() and not work.is_symlink() and
            work.parent == Path('/tmp').resolve() and
            re.fullmatch(r'luma123-cloud-' + commit[:12] + r'-[A-Za-z0-9_-]+', work.name),
            'Exact original task-owned temporary workspace required')
    require(work.stat().st_uid == os.getuid() and work.stat().st_mode & 0o077 == 0, 'Workspace must be owner-only')
    checkout, receipts = work / 'source', work / 'receipts'
    require(checkout.is_dir() and not checkout.is_symlink() and receipts.is_dir() and not receipts.is_symlink(),
            'Original source/receipts directories required')
    require((checkout / '.git').is_dir() and not (checkout / '.git').is_symlink(), 'Ordinary original Git clone required')
    op_path, stage_path = regular(receipts / 'operator.json'), regular(receipts / 'staging.json')
    op, stage = json.loads(op_path.read_text()), json.loads(stage_path.read_text())
    require(op.get('status') == 'STAGED_ONLY' and op.get('staging_passed') is True and
            op.get('source_commit') == commit and op.get('repository') == REPO and
            op.get('previous_source_commit') == PREVIOUS and op.get('expected_live') == LIVE and
            op.get('production_apk_sha256') == PROD and op.get('lab_apk_sha256') == LAB and
            op.get('script_sha256') == OPERATOR_SHA, 'Original exact STAGED_ONLY operator identity differs')
    for key, path in {'workspace':work,'checkout':checkout,'receipts':receipts,'staging':stage_path,
                      'deployment':receipts / 'deployment/deployment.json'}.items():
        require(op.get(key) == str(path), 'Original operator path differs: ' + key)
    require(not (receipts / 'deployment').exists(), 'Original operator already has a deployment attempt; inspect it first')
    require(stage.get('passed') is True and stage.get('sourceCommit') == commit and
            stage.get('previousSourceCommit') == PREVIOUS and stage.get('baselinePhysicalPaths') == 570 and
            stage.get('downloadBasesRequested') is True and stage.get('authenticated') is False and
            stage.get('uploaded') is False and stage.get('deployed') is False,
            'Exact staging-only PASS receipt required')
    for path in (receipts / 'final-operator.json', receipts / 'final-deployment', receipts / 'final-deployment.log'):
        require(not path.exists() and not path.is_symlink(), 'Never overwrite an earlier finalization: ' + str(path))

    def git(*parts):
        return subprocess.check_output(['git','-c','credential.helper=','-c','http.extraHeader=',*parts],
                                       cwd=checkout, env=env, stderr=subprocess.DEVNULL, timeout=180)
    require(git('rev-parse','HEAD').decode().strip() == commit and
            not git('status','--porcelain','--untracked-files=all').strip(), 'Exact C2 HEAD and clean checkout required')
    require(git('remote','get-url','origin').decode().strip() == REPO, 'Original public GitHub origin differs')
    source_path = safe_file(checkout, SOURCE)
    require(source_path.read_bytes() == git('show',commit + ':' + SOURCE) and
            sha(source_path) == stage.get('sourceManifestSha256'), 'Committed C2 source manifest differs from staging')
    source = json.loads(source_path.read_text())
    require(source.get('status') == 'PASS' and source.get('edition') == '1.23' and source.get('release') == '1.23.0-rc2' and
            source.get('native_capture_provenance_verified') and source.get('package_identity_verified') and
            source.get('production_apk_sha256') == PROD and source.get('lab_apk_sha256') == LAB,
            'Accepted final E rc2 source identity required')
    artifact = source.get('artifact_source_commit','')
    require(isinstance(artifact,str) and re.fullmatch(r'[a-f0-9]{40}',artifact) and
            op.get('artifact_source_commit') == artifact, 'Original immutable C1 artifact commit differs')
    frozen = source.get('frozen_inputs',{})
    require(isinstance(frozen,dict) and {'scripts/deploy-site-v123.py','scripts/deploy-cognitive-overlay.py'} <= frozen.keys(),
            'Complete frozen deployer inputs required')
    for name, digest in frozen.items():
        require(isinstance(digest,str) and re.fullmatch(r'[a-f0-9]{64}',digest) and sha(safe_file(checkout,name)) == digest,
                'Frozen C2 input differs: ' + name)
    config = safe_file(checkout,'firebase.json')
    require(sha(config) == source.get('firebase_config_sha256'), 'Frozen Hosting config differs')
    cfg = json.loads(config.read_text())['hosting']
    require(cfg.get('site') == 'luma-drift-production' and cfg.get('public') == 'public', 'Unexpected Hosting destination')
    manifest_name = 'releases/manifest-1.23.0-rc2.json'
    require(source.get('artifact_manifest') == manifest_name, 'Exact canonical artifact manifest required')
    manifest_path = safe_file(checkout,manifest_name)
    require(sha(manifest_path) == source.get('artifact_manifest_sha256') and
            manifest_path.read_bytes() == git('show',artifact + ':' + manifest_name), 'Immutable artifact manifest differs')
    release = json.loads(manifest_path.read_text())
    require(release.get('edition') == '1.23' and release.get('version') == '1.23.0-rc2' and
            release.get('version_code') == 31, 'Canonical E rc2/code31 metadata required')
    for channel,digest,size in [('production',PROD,PROD_BYTES),('lab',LAB,LAB_BYTES)]:
        entry, staged = release['apks'][channel], stage['apks'][channel]
        version = '1.23.0-sandbox-rc2' if channel == 'lab' else '1.23.0-rc2'
        relative = 'public/releases/luma-drift-' + version + '.apk'
        require(entry.get('sha256') == digest and entry.get('bytes') == size and entry.get('version') == version and
                entry.get('code') == 31 and entry.get('signer_certificate_sha256') == SIGNER and
                entry.get('path') == relative.removeprefix('public/'), 'Canonical APK metadata differs: ' + channel)
        require(staged.get('path') == relative and staged.get('sha256') == digest and staged.get('bytes') == size and
                source['files'].get('/'+entry['path']) == {'sha256':digest,'bytes':size}, 'Staged/source APK identity differs: ' + channel)
        path = safe_file(checkout,relative)
        require(path.stat().st_size == size and sha(path) == digest, 'Existing reconstructed APK bytes differ: ' + channel)
    require(not git('status','--porcelain','--untracked-files=all').strip(), 'Validation changed the checkout')
    return dict(work=work,checkout=checkout,receipts=receipts,source_manifest_sha256=sha(source_path),
                original_operator_sha256=sha(op_path),original_staging_sha256=sha(stage_path),
                artifact_source_commit=artifact,source_public_tree=git('rev-parse','HEAD:public').decode().strip(),
                frozen_inputs_checked=len(frozen))


def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('workspace');p.add_argument('commit')
    mode=p.add_mutually_exclusive_group(required=True)
    mode.add_argument('--check-only',action='store_true');mode.add_argument('--deploy',action='store_true')
    a=p.parse_args();require(sys.platform.startswith('linux'),'Execution is for the owned Linux Cloud Shell VM only')
    env=environment();state=validate(a.workspace,a.commit,env)
    if a.check_only:
        print(json.dumps({'status':'VALIDATED_ONLY','source_commit':a.commit,'workspace':a.workspace,
                          'source_manifest_sha256':state['source_manifest_sha256'],'authenticated':False,'deployed':False}))
        return 0
    require(shutil.which('gcloud') is not None,'gcloud executable required')
    os.umask(0o077);receipts=state['receipts'];final=receipts/'final-operator.json'
    record={key:str(value) if isinstance(value,Path) else value for key,value in state.items()}
    record.update(status='STARTED',source_commit=a.commit,previous_source_commit=PREVIOUS,expected_live=LIVE,
                  production_apk_sha256=PROD,lab_apk_sha256=LAB,started_utc=datetime.datetime.now(datetime.timezone.utc).isoformat(),
                  script_sha256=sha(Path(__file__)),deployment=str(receipts/'final-deployment/deployment.json'),
                  scope='Finalize existing verified stage only; no clone, reconstruction or original receipt overwrite.')
    with final.open('x') as stream:stream.write(json.dumps(record,indent=2)+'\n')
    try:
        acquired=subprocess.run(['gcloud','auth','print-access-token','--quiet'],stdin=subprocess.DEVNULL,
                                stdout=subprocess.PIPE,stderr=subprocess.DEVNULL,env=env,timeout=60)
        token=acquired.stdout.strip()
        require(acquired.returncode==0 and 20<len(token)<8192 and not any(chr(c).isspace() for c in token),
                'gcloud token acquisition failed; no auth fallback attempted')
        command=[sys.executable,'scripts/deploy-site-v123.py','--commit',a.commit,'--apk-sha256',PROD,
                 '--expected-live',LIVE,'--previous-source-commit',PREVIOUS,'--transport','rest','--token-stdin',
                 '--output',str(receipts/'final-deployment'),'--deploy']
        try:
            with (receipts/'final-deployment.log').open('xb') as output:
                result=subprocess.run(command,cwd=state['checkout'],env=env,input=token+b'\n',
                                      stdout=output,stderr=subprocess.STDOUT,timeout=7200)
            require(result.returncode==0,'Deployment failed; inspect the separate final-deployment.log before any new attempt')
        finally:
            token=b'';acquired=None
        deployed=json.loads(regular(receipts/'final-deployment/deployment.json').read_text())
        require(deployed.get('status')=='DEPLOY/FINALIZED' and deployed.get('sourceCommit')==a.commit,
                'Final Hosting management receipt identity differs')
        require(sha(receipts/'operator.json')==state['original_operator_sha256'] and
                sha(receipts/'staging.json')==state['original_staging_sha256'],'Original staging evidence changed during finalization')
        record.update(status='DEPLOY/FINALIZED',version=deployed['version'],
                      scope='Authenticated Hosting management only; public response bytes and rendering remain separate.')
        code=0
    except Exception as error:
        record.update(status='FAIL',error=str(error),scope='Stop and inspect; no operator rerun or alternate authentication/network route.')
        code=1
    record['finished_utc']=datetime.datetime.now(datetime.timezone.utc).isoformat()
    final.write_text(json.dumps(record,indent=2)+'\n');print(json.dumps(record,indent=2))
    return code


if __name__=='__main__':sys.exit(main())
