#!/usr/bin/env python3
"""Exact-source Cloud Shell operator. --plan is offline; nothing runs on import.

No credentials or response tokens are written to disk. --deploy is the explicit
publication mode; --stage-only downloads pinned 1.23 bases but never authenticates.
All original operation logs/receipts live outside a new sparse checkout.
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
import tempfile

REPO = 'https://github.com/nabz0r/AndroidHit.git'
SOURCE = 'docs/validation/site-v1.24/release/source-manifest.json'
PREVIOUS = '5cc870701c8039e664d1c00a90d0586c3d00521c'
PREVIOUS_DEPLOY = 'c99ac52903eca6707d3f4d644d7c480c79b63e68'
LIVE = 'sites/luma-drift-production/versions/cf8b817321bf33d5'
PROD = '2cb544e6ee341d346e7b6c90a75c46c3b7fa909ce236b5048677476920e7f67e'
LAB = '1929ee6bc8644ee936bfe7db917fd02e3000598f6b83d053af51a8c5d5cd238b'
SPARSE = ('.gitignore', 'scripts/stage-site-v124-rest.py', SOURCE)


def require(ok, message):
    if not ok:
        raise RuntimeError(message)


def now():
    return datetime.datetime.now(datetime.timezone.utc).isoformat()


def save(path, value):
    path.write_text(json.dumps(value, indent=2) + '\n')


def plan(commit):
    return dict(source_commit=commit, repository=REPO, sparse_initial_files=SPARSE,
                previous_source_commit=PREVIOUS, expected_live=LIVE,
                production_apk_sha256=PROD, lab_apk_sha256=LAB,
                staging_base_download_host='luma-drift-production.web.app',
                auth='gcloud auth print-access-token --quiet; stdout in memory, deploy stdin only',
                receipts='/tmp/luma124-cloud-<C2-prefix>-<unique>/receipts/',
                publication_command_requires='--deploy', network_executed=False)


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('commit', help='Exact published C2 SHA, 40 lowercase hex characters')
    mode = p.add_mutually_exclusive_group(required=True)
    mode.add_argument('--plan', action='store_true', help='Offline plan only; no Git, network, auth or new directories')
    mode.add_argument('--stage-only', action='store_true', help='Exact clone and pinned base downloads only; no authentication/deployment')
    mode.add_argument('--deploy', action='store_true', help='Stage, acquire in-memory OAuth token and publish through the exact REST helper')
    a = p.parse_args()
    require(re.fullmatch(r'[a-f0-9]{40}', a.commit), 'Exact C2 SHA required')
    if a.plan:
        print(json.dumps(plan(a.commit), indent=2))
        return 0
    require(sys.platform.startswith('linux'), 'Execution is for the coordinator-owned Linux Cloud Shell VM only')
    require(shutil.which('git') is not None and (not a.deploy or shutil.which('gcloud') is not None),
            'Required Git/gcloud executable missing')
    os.umask(0o077)
    work = Path(tempfile.mkdtemp(prefix='luma124-cloud-' + a.commit[:12] + '-', dir='/tmp')).resolve()
    checkout = work / 'source'
    receipts = work / 'receipts'
    receipts.mkdir(mode=0o700)
    record = dict(plan(a.commit), started_utc=now(), status='STARTED', workspace=str(work),
                  checkout=str(checkout), receipts=str(receipts), staging=str(receipts / 'staging.json'),
                  deployment=str(receipts / 'deployment' / 'deployment.json'),
                  script_sha256=hashlib.sha256(Path(__file__).read_bytes()).hexdigest())
    record.pop('network_executed', None)
    record['network_requested'] = True
    save(receipts / 'operator.json', record)
    print(json.dumps({'workspace':str(work), 'receipts':str(receipts), 'source_commit':a.commit}), flush=True)
    env = os.environ.copy()
    env['CLOUDSDK_CORE_DISABLE_PROMPTS'] = '1'
    git_env = env.copy()
    for key in list(git_env):
        if key.startswith(('GIT_TRACE', 'GIT_CONFIG_')) or key in ('GIT_CURL_VERBOSE', 'GIT_ASKPASS', 'SSH_ASKPASS'):
            git_env.pop(key, None)
    git_env.update(GIT_TERMINAL_PROMPT='0', GIT_CONFIG_NOSYSTEM='1', GIT_CONFIG_GLOBAL=os.devnull,
                   GIT_CONFIG_SYSTEM=os.devnull, GIT_ASKPASS='/bin/false')
    git_prefix = ['git', '-c', 'credential.helper=', '-c', 'http.extraHeader=']

    def logged(command, filename, *, cwd=None, timeout=300, input_bytes=None, child_env=None):
        with (receipts / filename).open('xb') as output:
            result = subprocess.run(command, cwd=cwd, env=child_env or env, input=input_bytes,
                                    stdout=output, stderr=subprocess.STDOUT, timeout=timeout)
        require(result.returncode == 0, 'Command failed; inspect ' + str(receipts / filename))

    def git_bytes(*parts):
        return subprocess.check_output(git_prefix + list(parts), cwd=checkout, env=git_env,
                                       stderr=subprocess.DEVNULL, timeout=180)

    def fetch(commit, label):
        require(re.fullmatch(r'[a-f0-9]{40}', commit), 'Pinned dependency commit required')
        logged(git_prefix + ['fetch', '--no-tags', '--depth=1', '--filter=blob:none', 'origin', commit],
               label + '.log', cwd=checkout, child_env=git_env)
        require(git_bytes('rev-parse', commit + '^{commit}').decode().strip() == commit,
                'Fetched dependency identity differs')

    try:
        logged(git_prefix + ['clone', '--filter=blob:none', '--no-checkout', '--depth=1',
                            '--single-branch', '--branch=main', REPO, str(checkout)],
               'clone.log', child_env=git_env)
        fetch(a.commit, 'fetch-c2')
        # Configure exact file patterns before checkout: no root sibling or historic
        # public APK checkout. The committed staging helper restores audited inputs.
        logged(git_prefix + ['config', 'core.sparseCheckout', 'true'], 'sparse-config.log', cwd=checkout, child_env=git_env)
        logged(git_prefix + ['config', 'core.sparseCheckoutCone', 'false'], 'sparse-cone.log', cwd=checkout, child_env=git_env)
        (checkout / '.git/info/sparse-checkout').write_text(''.join('/' + name + '\n' for name in SPARSE))
        logged(git_prefix + ['checkout', '--detach', a.commit], 'checkout.log', cwd=checkout, child_env=git_env)
        require(git_bytes('rev-parse', 'HEAD').decode().strip() == a.commit and
                not git_bytes('status', '--porcelain', '--untracked-files=all').strip(),
                'Exact C2 sparse checkout is not clean')
        source = json.loads((checkout / SOURCE).read_text())
        require(source.get('status') == 'PASS' and source.get('edition') == '1.24' and
                source.get('release') == '1.24.0-rc2' and source.get('native_capture_provenance_verified') and
                source.get('package_identity_verified') and source.get('production_apk_sha256') == PROD and
                source.get('lab_apk_sha256') == LAB, 'C2 is not the accepted D rc2 source manifest')
        artifact = source.get('artifact_source_commit', '')
        require(isinstance(artifact, str) and re.fullmatch(r'[a-f0-9]{40}', artifact),
                'C2 must contain a real immutable C1 artifact commit')
        fetch(PREVIOUS, 'fetch-previous-c3')
        fetch(PREVIOUS_DEPLOY, 'fetch-previous-deployment-c2')
        if artifact not in (a.commit, PREVIOUS):
            fetch(artifact, 'fetch-artifact-c1')
        logged([sys.executable, 'scripts/stage-site-v124-rest.py', '--commit', a.commit,
                '--download-bases', '--report', str(receipts / 'staging.json')],
               'staging.log', cwd=checkout, timeout=2400, child_env=git_env)
        staged = json.loads((receipts / 'staging.json').read_text())
        require(staged.get('passed') and staged.get('sourceCommit') == a.commit and
                staged['apks']['production']['sha256'] == PROD and staged['apks']['lab']['sha256'] == LAB,
                'Staging receipt is not the exact pair of D APKs')
        require(not git_bytes('status', '--porcelain', '--untracked-files=all').strip(), 'Staging changed tracked source')
        record.update(artifact_source_commit=artifact, staging_passed=True)
        if a.stage_only:
            record['status'] = 'STAGED_ONLY'
        else:
            # Capture only stdout in RAM. Never include this value in argv, logs,
            # environment, an exception message, JSON or any disk-backed stream.
            acquired = subprocess.run(['gcloud', 'auth', 'print-access-token', '--quiet'],
                                      stdin=subprocess.DEVNULL, stdout=subprocess.PIPE,
                                      stderr=subprocess.DEVNULL, env=env, timeout=60)
            token = acquired.stdout.strip()
            require(acquired.returncode == 0 and 20 < len(token) < 8192 and
                    not any(chr(char).isspace() for char in token), 'gcloud token acquisition failed; no auth fallback attempted')
            command = [sys.executable, 'scripts/deploy-site-v124.py', '--commit', a.commit,
                       '--apk-sha256', PROD, '--expected-live', LIVE,
                       '--previous-source-commit', PREVIOUS, '--transport', 'rest', '--token-stdin',
                       '--output', str(receipts / 'deployment'), '--deploy']
            try:
                logged(command, 'deployment.log', cwd=checkout, timeout=7200, input_bytes=token + b'\n', child_env=git_env)
            finally:
                token = b''
                acquired = None
            deployed = json.loads((receipts / 'deployment/deployment.json').read_text())
            require(deployed.get('status') == 'DEPLOY/FINALIZED' and deployed.get('sourceCommit') == a.commit,
                    'Final Hosting management receipt differs; inspect before any new attempt')
            record.update(status='DEPLOY/FINALIZED', version=deployed['version'],
                          scope='Authenticated Hosting management only; public response bytes and rendering still need separate qualification.')
        record['finished_utc'] = now()
        save(receipts / 'operator.json', record)
        print(json.dumps(record, indent=2))
        return 0
    except Exception as error:
        # No credential is interpolated into an error above or any child argv.
        record.update(status='FAIL', finished_utc=now(), error=str(error),
                      failure_scope='Stop and inspect original receipts/logs. No automatic operator rerun, alternate auth or network route; the committed client retains its bounded transport retries.')
        save(receipts / 'operator.json', record)
        print(json.dumps({'status':'FAIL', 'receipts':str(receipts), 'error':str(error)}), file=sys.stderr)
        return 1


if __name__ == '__main__':
    sys.exit(main())
