#!/usr/bin/env python3
"""Retry after the exact preserved pre-deployer auth failure; new final2 receipts only. Never clone or restage.

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
SOURCE = 'docs/validation/site-v1.24/release/source-manifest.json'
PREVIOUS = '5cc870701c8039e664d1c00a90d0586c3d00521c'
LIVE = 'sites/luma-drift-production/versions/cf8b817321bf33d5'
PROD = '2cb544e6ee341d346e7b6c90a75c46c3b7fa909ce236b5048677476920e7f67e'
LAB = '1929ee6bc8644ee936bfe7db917fd02e3000598f6b83d053af51a8c5d5cd238b'
SIGNER = 'dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287'
OPERATOR_SHA = '8291593809f68b6ff1a1e7fe00de85f2317d6ebab52b70640c52b9a21b35111a'
PROD_BYTES, LAB_BYTES = 340290322, 340290009
C2 = '66119799d6fbe5254a8d37d50db76afe8daffbdc'
C1 = '787fba142b8edd1f840b9b70cb7749167edacd24'
C2_TREE = 'ee94f27e3c9dbbbc49c0d95568424c9d4af2118a'
PUBLIC_TREE = 'd003cdc3ca4a4aa87ea19a5ece1222fa49c9975b'
PREVIOUS_DEPLOY = 'c99ac52903eca6707d3f4d644d7c480c79b63e68'
BASELINE_PUBLIC_TREE = '62e59825f561bb4857510d3e602d43ec87535dd5'

ORIGINAL_COMMIT = '39c0985b24ecc4cf6ef00b6eb1c1392ebe3758da'
WORK = Path('/tmp/luma124-cloud-39c0985b24ec-g5pa_wji')
APP_TREE = '81eba0e25202124ebebb5f4468d5dd8ba8ac3897'
SOURCE_SHA = '67237fb745f52863e075c476f8d8eca938469bba5288bf4e65edff196a5384bd'
ORIGINAL_OP_SHA = 'f547d2ee51fae21cbd31a57d63040f7258126593135de228c672c129f22f15fc'
ORIGINAL_STAGE_SHA = 'cd4410973f4734e4373cd6b168696d57e285ae34aa5aefe087ba73fa67c41eba'
PREVIOUS_AUTH_FAILURE_SHA = '08f2958b07e50d564274de24ce115765291e1a015ff1c9fabcb08d4eb129c451'
RESTAGE_SHA = 'ae50494c446cd87ba27cee44ca20730e131f3eb3718c3b0db75edac8055d33cc'
ALLOWED_DELTA = ['.github/workflows/puzzle-contracts.yml','docs/validation/v1.24/publication/operator-preparation/stage-c2-observation.json','scripts/HomeV115GeometryTest.java','scripts/HomeV115Test.java','scripts/ProfileSummonV116Test.java','scripts/test-journey-world-v122.sh']


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
    require(commit == C2, 'Exact reviewed target C2b SHA required')
    work = Path(workspace)
    require(work == WORK and work.is_dir() and not work.is_symlink() and work == work.resolve(),
            'Exact original39 workspace required; it is not named from target C2b')
    require(work.stat().st_uid == os.getuid() and work.stat().st_mode & 0o077 == 0, 'Owner-only workspace required')
    checkout, original, receipts = work/'source', work/'receipts', work/'receipts-c2b'
    require(all(p.is_dir() and not p.is_symlink() for p in (checkout,original,receipts,checkout/'.git')), 'Original clone and separate receipt directories required')
    op_path, old_stage_path = regular(original/'operator.json'), regular(original/'staging.json')
    require(sha(op_path) == ORIGINAL_OP_SHA and sha(old_stage_path) == ORIGINAL_STAGE_SHA, 'Original39 evidence differs')
    op, old = json.loads(op_path.read_text()), json.loads(old_stage_path.read_text())
    require(op.get('status') == 'STAGED_ONLY' and op.get('source_commit') == ORIGINAL_COMMIT and op.get('staging_passed') is True and
            op.get('script_sha256') == OPERATOR_SHA and op.get('repository') == REPO and op.get('expected_live') == LIVE and
            op.get('previous_source_commit') == PREVIOUS and op.get('production_apk_sha256') == PROD and op.get('lab_apk_sha256') == LAB,
            'Exact original39 staged operator required')
    require(old.get('passed') is True and old.get('sourceCommit') == ORIGINAL_COMMIT and
            old.get('sourceManifestSha256') == SOURCE_SHA and old.get('downloadBasesRequested') is True and
            not any(old.get(k) is not False for k in ('authenticated','uploaded','deployed')), 'Original39 staging scope differs')
    for key,path in {'workspace':work,'checkout':checkout,'receipts':original,'staging':old_stage_path,'deployment':original/'deployment/deployment.json'}.items():
        require(op.get(key) == str(path), 'Original39 operator path differs: '+key)
    require(not (original/'deployment').exists(), 'Original stage already has a deployment attempt')
    lift_path, stage_path = regular(receipts/'restage-operator.json'), regular(receipts/'staging.json')
    lift, stage = json.loads(lift_path.read_text()), json.loads(stage_path.read_text())
    require(lift.get('status') == 'STAGED_ONLY' and lift.get('originalStageCommit') == ORIGINAL_COMMIT and lift.get('sourceCommit') == C2 and
            lift.get('originalOperatorSha256') == ORIGINAL_OP_SHA and lift.get('originalStagingSha256') == ORIGINAL_STAGE_SHA and
            lift.get('scriptSha256') == RESTAGE_SHA and lift.get('stagingSha256') == sha(stage_path) and lift.get('frozenInputsChecked') == 69 and
            lift.get('workspace') == str(work) and lift.get('checkout') == str(checkout), 'Reviewed39 to C2b restage chain differs')
    require(stage.get('passed') is True and stage.get('sourceCommit') == C2 and stage.get('sourceManifestSha256') == SOURCE_SHA and
            stage.get('previousSourceCommit') == PREVIOUS and stage.get('previousDeploymentSourceCommit') == PREVIOUS_DEPLOY and
            stage.get('baselinePhysicalPaths') == 576 and stage.get('baselinePublicTree') == BASELINE_PUBLIC_TREE and stage.get('retiredPaths') == [] and
            stage.get('downloadBasesRequested') is False and stage.get('downloadedBaseChannels') == [], 'Exact reused C2b stage required')
    require(all(stage.get(k) is False and lift.get(k) is False for k in ('authenticated','uploaded','deployed')), 'Restage exceeded staging-only scope')
    prior = regular(receipts/'final-operator.json')
    require(sha(prior) == PREVIOUS_AUTH_FAILURE_SHA, 'Preserved initial authentication failure differs')
    failed = json.loads(prior.read_text())
    require(failed.get('status') == 'FAIL' and failed.get('source_commit') == C2 and
            failed.get('error') == 'gcloud token acquisition failed; no auth fallback attempted' and
            not (receipts/'final-deployment').exists() and not (receipts/'final-deployment.log').exists(),
            'Only the proven pre-deployer authentication failure may precede this attempt')
    for path in (receipts/'final2-operator.json',receipts/'final2-deployment',receipts/'final2-deployment.log'):
        require(not path.exists() and not path.is_symlink(), 'Never overwrite a finalization attempt')
    def git(*parts):
        return subprocess.check_output(['git','-c','credential.helper=','-c','http.extraHeader=',*parts], cwd=checkout, env=env, stderr=subprocess.DEVNULL, timeout=180)
    require(git('rev-parse','HEAD').decode().strip() == C2 and git('rev-parse','HEAD^{tree}').decode().strip() == C2_TREE and
            git('rev-parse','HEAD:public').decode().strip() == PUBLIC_TREE and git('rev-parse','HEAD:app').decode().strip() == APP_TREE and
            not git('status','--porcelain','--untracked-files=all').strip(), 'Exact clean C2b checkout and trees required')
    require(git('remote','get-url','origin').decode().strip() == REPO, 'Pinned origin required')
    require(git('diff','--name-only',ORIGINAL_COMMIT,C2).decode().splitlines() == ALLOWED_DELTA, 'Unexpected39 to C2b source delta')
    source_path=safe_file(checkout,SOURCE)
    require(sha(source_path) == SOURCE_SHA and source_path.read_bytes() == git('show',C2+':'+SOURCE) == git('show',ORIGINAL_COMMIT+':'+SOURCE), 'Committed source manifest differs')
    source=json.loads(source_path.read_text())
    require(source.get('status') == 'PASS' and source.get('edition') == '1.24' and source.get('release') == '1.24.0-rc2' and
            source.get('native_capture_provenance_verified') and source.get('package_identity_verified') and
            source.get('production_apk_sha256') == PROD and source.get('lab_apk_sha256') == LAB and
            source.get('artifact_source_commit') == C1 and op.get('artifact_source_commit') == C1, 'Exact D/C1 identity required')
    require(source.get('previous_source_commit') == PREVIOUS and source.get('previous_deployment_source_commit') == PREVIOUS_DEPLOY and
            source.get('baseline_public_tree') == BASELINE_PUBLIC_TREE, 'Accepted baseline bridge differs')
    frozen=source.get('frozen_inputs',{})
    require(isinstance(frozen,dict) and len(frozen) == 69 and {'scripts/deploy-site-v124.py','scripts/deploy-cognitive-overlay.py'} <= frozen.keys(), 'Exact69 frozen inputs required')
    for name,digest in frozen.items():
        require(isinstance(digest,str) and re.fullmatch(r'[a-f0-9]{64}',digest) and sha(safe_file(checkout,name)) == digest and
                hashlib.sha256(git('show',C2+':'+name)).hexdigest() == digest, 'Frozen input differs: '+name)
    config=safe_file(checkout,'firebase.json')
    require(sha(config) == source.get('firebase_config_sha256'), 'Frozen Hosting config differs')
    cfg=json.loads(config.read_text())['hosting']
    require(cfg.get('site') == 'luma-drift-production' and cfg.get('public') == 'public', 'Pinned Hosting destination required')
    manifest_name='releases/manifest-1.24.0-rc2.json'
    require(source.get('artifact_manifest') == manifest_name, 'Canonical artifact manifest required')
    manifest_path=safe_file(checkout,manifest_name)
    require(sha(manifest_path) == source.get('artifact_manifest_sha256') and manifest_path.read_bytes() == git('show',C1+':'+manifest_name), 'Immutable C1 artifact manifest differs')
    release=json.loads(manifest_path.read_text())
    require(release.get('edition') == '1.24' and release.get('version') == '1.24.0-rc2' and release.get('version_code') == 33, 'D rc2/code33 metadata required')
    for channel,digest,size in [('production',PROD,PROD_BYTES),('lab',LAB,LAB_BYTES)]:
        entry,staged=release['apks'][channel],stage['apks'][channel]
        version='1.24.0-sandbox-rc2' if channel == 'lab' else '1.24.0-rc2'
        relative='public/releases/luma-drift-'+version+'.apk'
        require(entry.get('sha256') == digest and entry.get('bytes') == size and entry.get('version') == version and entry.get('code') == 33 and
                entry.get('signer_certificate_sha256') == SIGNER and entry.get('path') == relative.removeprefix('public/'), 'Canonical APK metadata differs')
        require(staged.get('path') == relative and staged.get('sha256') == digest and staged.get('bytes') == size and staged.get('reconstructed') is False and
                source['files'].get('/'+entry['path']) == {'sha256':digest,'bytes':size}, 'Reused staged/source APK identity differs')
        path=safe_file(checkout,relative)
        require(path.stat().st_size == size and sha(path) == digest, 'Existing APK bytes differ')
    require(not git('status','--porcelain','--untracked-files=all').strip(), 'Validation changed checkout')
    return dict(work=work,checkout=checkout,receipts=receipts,source_manifest_sha256=SOURCE_SHA,original_stage_commit=ORIGINAL_COMMIT,
                original_operator_sha256=sha(op_path),original_staging_sha256=sha(old_stage_path),restage_operator_sha256=sha(lift_path),
                target_staging_sha256=sha(stage_path),previous_auth_failure_sha256=sha(prior),artifact_source_commit=C1,source_public_tree=PUBLIC_TREE,frozen_inputs_checked=69)


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
    os.umask(0o077);receipts=state['receipts'];final=receipts/'final2-operator.json'
    record={key:str(value) if isinstance(value,Path) else value for key,value in state.items()}
    record.update(status='STARTED',source_commit=a.commit,previous_source_commit=PREVIOUS,expected_live=LIVE,
                  production_apk_sha256=PROD,lab_apk_sha256=LAB,started_utc=datetime.datetime.now(datetime.timezone.utc).isoformat(),
                  script_sha256=sha(Path(__file__)),deployment=str(receipts/'final2-deployment/deployment.json'),
                  scope='Finalize existing verified stage only; no clone, reconstruction or original receipt overwrite.')
    with final.open('x') as stream:stream.write(json.dumps(record,indent=2)+'\n')
    try:
        acquired=subprocess.run(['gcloud','auth','print-access-token','--quiet'],stdin=subprocess.DEVNULL,
                                stdout=subprocess.PIPE,stderr=subprocess.DEVNULL,env=env,timeout=60)
        token=acquired.stdout.strip()
        require(acquired.returncode==0 and 20<len(token)<8192 and not any(chr(c).isspace() for c in token),
                'gcloud token acquisition failed; no auth fallback attempted')
        command=[sys.executable,'scripts/deploy-site-v124.py','--commit',a.commit,'--apk-sha256',PROD,
                 '--expected-live',LIVE,'--previous-source-commit',PREVIOUS,'--transport','rest','--token-stdin',
                 '--output',str(receipts/'final2-deployment'),'--deploy']
        try:
            with (receipts/'final2-deployment.log').open('xb') as output:
                result=subprocess.run(command,cwd=state['checkout'],env=env,input=token+b'\n',
                                      stdout=output,stderr=subprocess.STDOUT,timeout=7200)
            require(result.returncode==0,'Deployment failed; inspect the separate final2-deployment.log before any new attempt')
        finally:
            token=b'';acquired=None
        deployed=json.loads(regular(receipts/'final2-deployment/deployment.json').read_text())
        require(deployed.get('status')=='DEPLOY/FINALIZED' and deployed.get('sourceCommit')==a.commit,
                'Final Hosting management receipt identity differs')
        require(sha(state['work']/'receipts/operator.json')==state['original_operator_sha256'] and
                sha(state['work']/'receipts/staging.json')==state['original_staging_sha256'] and
                sha(receipts/'restage-operator.json')==state['restage_operator_sha256'] and
                sha(receipts/'staging.json')==state['target_staging_sha256'] and
                sha(receipts/'final-operator.json')==state['previous_auth_failure_sha256'],'Original staging or authentication failure evidence changed during finalization')
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
