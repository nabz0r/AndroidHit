#!/usr/bin/env python3
"""Offline staging contracts using tiny ZIPs in a disposable Git checkout."""
import copy
import hashlib
import io
import json
from pathlib import Path
import subprocess
import tempfile
import types
import unittest
from unittest.mock import patch
import zipfile


SCRIPTS = Path(__file__).resolve().parent


def module(name, path):
    result = types.ModuleType(name)
    result.__file__ = str(path)
    exec(compile(path.read_bytes(), str(path), 'exec'), result.__dict__)
    return result


STAGE = module('staging122', SCRIPTS / 'stage-site-v122-rest.py')
DELTA = module('delta122', SCRIPTS / 'apk-byte-delta.py')


class StagingTests(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.root = Path(self.temporary.name) / 'checkout'
        self.root.mkdir()
        self.previous_root = STAGE.ROOT
        STAGE.ROOT = self.root
        self.git('init', '-q')
        self.git('config', 'user.email', 'fixture@example.invalid')
        self.git('config', 'user.name', 'Offline fixture')
        self.put('.gitignore', 'releases/*.apk\npublic/releases/*.apk\n')
        self.put('scripts/apk-byte-delta.py', (SCRIPTS / 'apk-byte-delta.py').read_bytes())
        self.put('docs/audit/input.txt', 'accepted offline fixture')
        self.put('firebase.json', '{"hosting":{"site":"luma-drift-production","public":"public"}}\n')
        self.put('public/index.html', 'current fixture homepage')
        old = {'edition': '1.21', 'version_code': 29, 'apks': {}}
        artifact = {'edition': '1.22', 'version': '1.22.0-rc1', 'version_code': 30, 'apks': {}}
        for channel in ('production', 'lab'):
            version = '1.22.0-sandbox-rc1' if channel == 'lab' else '1.22.0-rc1'
            prior_version = '1.21.0-sandbox-rc1' if channel == 'lab' else '1.21.0-rc1'
            base = self.root / ('releases/luma-drift-' + prior_version + '.apk')
            target = Path(self.temporary.name) / (channel + '-target.apk')
            for path, changed in ((base, 'old'), (target, 'new')):
                path.parent.mkdir(parents=True, exist_ok=True)
                with zipfile.ZipFile(path, 'w', compression=zipfile.ZIP_DEFLATED) as archive:
                    archive.writestr('unchanged.dat', b'unchanged fixture ' * 1000)
                    archive.writestr('classes.dex', (channel + changed).encode())
            old['apks'][channel] = self.meta(base, str(base.relative_to(self.root)))
            current = self.meta(target, 'releases/luma-drift-' + version + '.apk')
            current.update(version=version, code=30, signer_certificate_sha256=STAGE.SIGNER)
            artifact['apks'][channel] = current
            prefix = self.root / ('releases/deltas/1.22.0-rc1/' + channel)
            prefix.parent.mkdir(parents=True, exist_ok=True)
            DELTA.build(base, target, str(prefix) + '.json.gz', DELTA.digest_path(base), DELTA.digest_path(target), report=str(prefix) + '-build.json')
        baseline = {'/index.html': {'sha256': 'a' * 64, 'bytes': 4}}
        baseline.update({'/historical/%03d.apk' % i: {'sha256': 'b' * 64, 'bytes': 300_000_000} for i in range(538)})
        files = copy.deepcopy(baseline)
        files['/index.html'] = self.meta(self.root / 'public/index.html')
        for entry in artifact['apks'].values():
            files['/' + entry['path']] = {'sha256': entry['sha256'], 'bytes': entry['bytes']}
        self.write_json(STAGE.OLD_MANIFEST, old)
        self.write_json(STAGE.BASELINE, {'files': baseline})
        manifest_name = 'releases/manifest-1.22.0-rc1.json'
        self.write_json(manifest_name, artifact)
        source = {'status': 'PASS', 'edition': '1.22', 'release': '1.22.0-rc1',
                  'native_capture_provenance_verified': True, 'package_identity_verified': True,
                  'frozen_inputs': {'docs/audit/input.txt': STAGE.digest(self.root / 'docs/audit/input.txt')},
                  'artifact_manifest': manifest_name, 'artifact_manifest_sha256': STAGE.digest(self.root / manifest_name),
                  'firebase_config_sha256': STAGE.digest(self.root / 'firebase.json'), 'files': files}
        for channel, entry in artifact['apks'].items():
            source[channel + '_apk_sha256'] = entry['sha256']
        self.write_json(STAGE.SOURCE, source)
        self.git('add', '.')
        self.git('commit', '-qm', 'offline fixture')
        self.commit = self.git('rev-parse', 'HEAD').strip()
        self.block_network = patch.object(STAGE.urllib.request.OpenerDirector, 'open', side_effect=AssertionError('Unexpected network'))
        self.block_network.start()

    def tearDown(self):
        self.block_network.stop()
        STAGE.ROOT = self.previous_root
        self.temporary.cleanup()

    def git(self, *args):
        return subprocess.check_output(['git', *args], cwd=self.root, text=True)

    def put(self, name, data):
        path = self.root / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(data.encode() if isinstance(data, str) else data)

    def write_json(self, name, value):
        self.put(name, json.dumps(value, indent=2) + '\n')

    def meta(self, path, name=None):
        result = {'sha256': hashlib.sha256(path.read_bytes()).hexdigest(), 'bytes': path.stat().st_size}
        if name:
            result['path'] = name
        return result

    def sparse_remove(self, name):
        self.git('update-index', '--skip-worktree', name)
        (self.root / name).unlink()

    def amend(self, name, update):
        path = self.root / name
        value = json.loads(path.read_text())
        update(value)
        self.write_json(name, value)
        self.git('add', name)
        self.git('commit', '-qm', 'fixture mutation')
        self.commit = self.git('rev-parse', 'HEAD').strip()

    def test_sparse_restore_reconstructs_exact_outputs_and_preserves_clean_checkout(self):
        for name in ('.gitignore', 'docs/audit/input.txt', 'public/index.html', 'releases/deltas/1.22.0-rc1/lab.json.gz'):
            self.sparse_remove(name)
        result = STAGE.stage(self.commit)
        self.assertTrue(result['passed'])
        self.assertEqual(len(result['historicalRawPayloadsAbsent']), 538)
        self.assertEqual(len(result['restoredCommittedFiles']), 4)
        self.assertTrue(all(row['reconstructed'] for row in result['apks'].values()))
        self.assertFalse(result['downloadedBaseChannels'])
        self.assertFalse(self.git('status', '--porcelain').strip())
        repeated = STAGE.stage(self.commit)
        self.assertTrue(all(not row['reconstructed'] for row in repeated['apks'].values()))

    def test_dirty_or_wrong_commit_is_refused(self):
        with self.assertRaisesRegex(ValueError, 'HEAD'):
            STAGE.stage('a' * 40)
        self.put('unexpected.txt', 'untracked')
        with self.assertRaisesRegex(ValueError, 'clean'):
            STAGE.stage(self.commit)

    def test_missing_base_never_downloads_without_explicit_flag(self):
        for path in (self.root / 'releases').glob('*.apk'):
            path.unlink()
        with self.assertRaisesRegex(ValueError, '--download-bases'):
            STAGE.stage(self.commit)

    def test_explicit_download_requests_only_fixed_identity_urls_then_verifies_bytes(self):
        data = {path.name: path.read_bytes() for path in (self.root / 'releases').glob('*.apk')}
        for path in (self.root / 'releases').glob('*.apk'):
            path.unlink()
        requests = []

        class Response(io.BytesIO):
            status = 200

            def __init__(self, url, raw):
                super().__init__(raw)
                self.url = url
                self.headers = {'Content-Length': str(len(raw))}

            def geturl(self):
                return self.url

        class Opener:
            def open(self, request, timeout):
                requests.append(request)
                name = request.full_url.rsplit('/', 1)[-1]
                return Response(request.full_url, data[name])

        with patch.object(STAGE.urllib.request, 'build_opener', return_value=Opener()):
            result = STAGE.stage(self.commit, True)
        self.assertEqual(result['downloadedBaseChannels'], ['production', 'lab'])
        self.assertEqual(len(requests), 2)
        for request in requests:
            self.assertTrue(request.full_url.startswith('https://' + STAGE.HOST + '/releases/luma-drift-1.21.0'))
            self.assertEqual(request.headers, {'Accept-encoding': 'identity'})

    def test_existing_canonical_is_never_overwritten(self):
        self.put('public/releases/luma-drift-1.22.0-rc1.apk', 'wrong bytes')
        with self.assertRaisesRegex(ValueError, 'Never replace'):
            STAGE.stage(self.commit)
        self.assertEqual((self.root / 'public/releases/luma-drift-1.22.0-rc1.apk').read_text(), 'wrong bytes')

    def test_patch_hash_from_build_receipt_is_enforced(self):
        self.amend('releases/deltas/1.22.0-rc1/production-build.json', lambda value: value['patch'].update(sha256='c' * 64))
        with self.assertRaisesRegex(ValueError, 'SHA-256 differs'):
            STAGE.stage(self.commit)

    def test_frozen_hash_and_bounded_paths_are_enforced(self):
        self.amend(STAGE.SOURCE, lambda value: value['frozen_inputs'].update({'docs/audit/input.txt': 'd' * 64}))
        with self.assertRaisesRegex(ValueError, 'SHA-256 differs'):
            STAGE.stage(self.commit)
        for name in ('../escape', '/absolute', 'docs/../escape', 'HEAD:other', 'docs\\escape', 'docs//escape'):
            with self.subTest(name=name), self.assertRaises(ValueError):
                STAGE.safe_path(name)

    def test_public_download_redirect_is_refused(self):
        with self.assertRaisesRegex(ValueError, 'redirect'):
            STAGE.NoRedirect().redirect_request(None, None, 302, 'redirect', {}, 'https://other.invalid')


if __name__ == '__main__':
    unittest.main()
