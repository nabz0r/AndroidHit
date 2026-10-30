#!/usr/bin/env python3
"""One-off exact-C2 operator replay; extends only pinned upload POST timeout.

Runs the unchanged committed deployer and all its preflights. A new clone lets
populateFiles reuse already accepted content hashes. The failed CREATED version
is retained as evidence and never released by this procedure. Token: stdin only.
"""
import argparse
import importlib.util
import json
from pathlib import Path
import re
import sys

COMMIT = '13ebe82be9b57569df50627449cc748f5245521c'
EXPECTED_LIVE = 'sites/luma-drift-production/versions/d88a6817932258f4'
APK = '9b2593388de22b1f50f49bea05b9b02008d4574f5cbb9012fb0df5a64c47a44e'
UPLOAD_URL = re.compile(r'https://upload-firebasehosting\.googleapis\.com/upload/sites/luma-drift-production/versions/[A-Za-z0-9_-]+/files/[a-f0-9]{64}')


class UploadBudget:
    def __init__(self, original):
        self.original = original

    def open(self, request, timeout=60):
        # The unchanged Client checks URL and credentials and its original
        # opener retains NoRedirect. No destination or content is substituted.
        extended = request.get_method() == 'POST' and UPLOAD_URL.fullmatch(request.full_url)
        return self.original.open(request, timeout=600 if extended else timeout)


def load(name, path):
    spec = importlib.util.spec_from_file_location(name, path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--checkout', required=True, type=Path)
    parser.add_argument('--failed', required=True, type=Path)
    parser.add_argument('--output', required=True, type=Path)
    args = parser.parse_args()
    root = args.checkout.resolve()
    deployer = load('luma_exact_c2_deployer', root / 'scripts/deploy-site-v122.py')
    original = deployer.rest_deploy

    def replay(source, commit, expected_live, output, deploy, token):
        assert commit == COMMIT and expected_live == EXPECTED_LIVE and deploy
        helper = load('luma_exact_c2_provider', root / 'scripts/deploy-cognitive-overlay.py')
        client = helper.Client(token)
        client._opener = UploadBudget(client._opener)
        previous = json.loads((args.failed / 'uploads.json').read_text())
        metadata = json.loads((args.failed / 'deployment-preflight.json').read_text())
        assert metadata['source_commit'] == COMMIT and metadata['production_apk_sha256'] == APK
        helper.live_channel(client, EXPECTED_LIVE)
        version = helper.version(client, previous['version'], 'CREATED')
        active = helper.files(client, previous['version'])
        pending = helper.files(client, previous['version'], 'EXPECTED')
        baseline = json.loads((args.failed / 'baseline-provider-files.json').read_text())
        overlay = json.loads((args.failed / 'overlay-source-and-gzip-hashes.json').read_text())
        expected = dict(baseline)
        expected.update({path: row['gzipSha256'] for path, row in overlay.items()})
        assert not set(active) & set(pending) and dict(active, **pending) == expected
        assert version.get('config', {}) == json.loads((args.failed / 'baseline-provider-config.json').read_text())
        helper.save(output, 'prior-created-version-state.json', {
            'version': previous['version'], 'status': 'CREATED', 'active': active,
            'expected': pending, 'liveUnchanged': EXPECTED_LIVE, 'neverReleasedByThisProcedure': True,
            'transportAdjustment': 'Only pinned upload POST socket timeout60s to600s; unchanged original preflight/clone/hash/config/finalize/release code.',
        })
        print(json.dumps({'priorCreatedVersion': previous['version'], 'activePaths': len(active), 'pendingPaths': len(pending), 'uploadTimeoutSeconds': 600}), flush=True)
        return original(source, commit, expected_live, output, deploy, token, client=client, helper=helper)

    deployer.rest_deploy = replay
    sys.argv = ['deploy-site-v122.py', '--apk-sha256', APK, '--commit', COMMIT,
                '--output', str(args.output), '--transport', 'rest',
                '--expected-live', EXPECTED_LIVE, '--token-stdin', '--deploy']
    deployer.main()


if __name__ == '__main__':
    main()
