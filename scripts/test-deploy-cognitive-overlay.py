#!/usr/bin/env python3
"""Offline tests for clone-only deployment; no auth or network is used."""
import copy
import contextlib
import io
import gzip
import hashlib
import json
from pathlib import Path
import subprocess
import tempfile
import types
import unittest
import urllib.error

MODULE = types.ModuleType('overlay')
MODULE.__file__ = str(Path(__file__).with_name('deploy-cognitive-overlay.py'))
exec(compile(Path(MODULE.__file__).read_text(), MODULE.__file__, 'exec'), MODULE.__dict__)
MODULE.time.sleep = lambda _: None


def git(root, *args):
    return subprocess.check_output(['git', *args], cwd=root, stderr=subprocess.DEVNULL).decode().strip()


def fixture(root):
    previous = {'/index.html': {'sha256': 'a' * 64, 'bytes': 10}}
    previous.update({'/historical/large-%03d.apk' % i: {'sha256': 'b' * 64, 'bytes': 300_000_000}
                     for i in range(524)})
    baseline = {'status': 'PASS', 'release': '1.21.0-rc1', 'files': previous}
    config = {'hosting': {'site': MODULE.SITE, 'public': 'public'}}
    source = {'schemaVersion': 1, 'status': 'PASS', 'release': 'cognitive-lab-1',
              'androidReleaseUnchanged': '1.21.0-rc1', 'baselineManifest': MODULE.BASELINE,
              'preservedPayloads': 524, 'intentionalChanges': ['/index.html']}
    current = copy.deepcopy(previous)
    names = ['/index.html', '/lab/cognitive/data/provenance.json',
             '/lab/cognitive/index.html', '/lab/cognitive/local.zip']
    for name in names:
        raw = ('new fixture bytes for ' + name).encode()
        path = root / ('public' + name)
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(raw)
        current[name] = {'sha256': MODULE.sha(raw), 'bytes': len(raw)}
    for name, data in ((MODULE.BASELINE, baseline), (MODULE.CONFIG, config)):
        path = root / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(data))
    source.update(files=current, newPayloads=sorted(set(current) - set(previous)),
                  baselineManifestSha256=MODULE.sha((root / MODULE.BASELINE).read_bytes()),
                  firebaseConfigSha256=MODULE.sha((root / MODULE.CONFIG).read_bytes()),
                  simulationProvenanceSha256=current['/lab/cognitive/data/provenance.json']['sha256'])
    (root / MODULE.MANIFEST).parent.mkdir(parents=True, exist_ok=True)
    (root / MODULE.MANIFEST).write_text(json.dumps(source))
    git(root, 'init', '-q')
    git(root, 'config', 'user.email', 'fixture@example.invalid')
    git(root, 'config', 'user.name', 'Offline fixture')
    git(root, 'add', '.')
    git(root, 'commit', '-qm', 'fixture')
    commit = git(root, 'rev-parse', 'HEAD')
    return MODULE.validate_source(root, commit)


class FakeClient:
    def __init__(self, source, fault=None):
        self.source, self.fault, self.calls, self.uploads = source, fault, [], []
        self.old = MODULE.EXPECTED_LIVE
        self.new = 'sites/' + MODULE.SITE + '/versions/new-fixture-version'
        self.operation = 'projects/123456/operations/fixture-operation'
        self.serving = {'headers': [{'glob': '**', 'headers': {'X-Frame-Options': 'DENY'}}]}
        self.maps = {self.old: {path: (hashlib.sha256(path.encode()).hexdigest(), 'ACTIVE')
                               for path in source['baselineFiles']}}
        self.maps[self.old].update({path: (hashlib.sha256(path.encode()).hexdigest(), 'ACTIVE')
                                   for path in MODULE.FIREBASE_RESERVED})
        if fault == 'unknown_reserved_extra':
            self.maps[self.old]['/__/firebase/unexpected.json'] = ('c' * 64, 'ACTIVE')
        self.states, self.live, self.live_reads = {self.old: 'FINALIZED'}, self.old, 0
        self.message = 'Previous release'
        self.release_name = 'sites/' + MODULE.SITE + '/releases/old-fixture'

    def api(self, method, resource, body=None, **query):
        self.calls.append((method, resource, copy.deepcopy(body), query))
        if resource.endswith('/channels/live'):
            self.live_reads += 1
            if self.fault == 'race_before_clone' and self.live_reads == 2:
                self.live = 'sites/' + MODULE.SITE + '/versions/concurrent-version'
            if self.fault == 'race_before_release' and self.live_reads == 3:
                self.live = 'sites/' + MODULE.SITE + '/versions/concurrent-version'
            return {'name': 'sites/' + MODULE.SITE + '/channels/live', 'url': 'https://example.invalid',
                    'release': {'name': self.release_name, 'message': self.message, 'type': 'DEPLOY',
                                'version': {'name': self.live, 'status': 'FINALIZED'}}}
        if resource.endswith('/versions:clone'):
            assert body == {'sourceVersion': self.old, 'finalize': False}
            self.maps[self.new] = copy.deepcopy(self.maps[self.old])
            if self.fault == 'clone_file_changed':
                self.maps[self.new]['/historical/large-000.apk'] = ('f' * 64, 'ACTIVE')
            self.states[self.new] = 'CREATED'
            return {'name': self.operation, 'done': False}
        if resource == self.operation:
            return {'name': self.operation, 'done': True,
                    'response': {'name': self.new, 'status': 'CREATED'}}
        if resource.endswith('/files'):
            name = resource.removesuffix('/files')
            status = query['status']
            rows = [{'path': path, 'hash': row[0], 'status': row[1]}
                    for path, row in sorted(self.maps[name].items()) if row[1] == status]
            offset = int(query.get('pageToken', 0))
            page = {'files': rows[offset:offset + 200]}
            if offset + 200 < len(rows):
                page['nextPageToken'] = str(offset + 200)
            return page
        if resource.endswith(':populateFiles'):
            name = resource.removesuffix(':populateFiles')
            wanted = body['files']
            assert set(wanted) == set(self.source['overlay'])
            required = sorted(set(wanted.values()))[1:]  # One provider-cache hit.
            for path, digest in wanted.items():
                self.maps[name][path] = (digest, 'EXPECTED' if digest in required else 'ACTIVE')
            base = 'https://' + MODULE.UPLOAD_HOST + '/upload/' + name + '/files'
            if self.fault == 'unexpected_hash':
                required.append('e' * 64)
            if self.fault == 'untrusted_upload':
                base = 'https://example.invalid/upload'
            return {'uploadRequiredHashes': required, 'uploadUrl': base}
        if method == 'PATCH':
            assert body == {'status': 'FINALIZED'} and query == {'updateMask': 'status'}
            self.states[resource] = 'FINALIZED'
        if resource in self.states:
            serving = copy.deepcopy(self.serving)
            if self.fault == 'clone_config_changed' and resource == self.new:
                serving['redirects'] = [{'glob': '**', 'location': 'https://example.invalid'}]
            return {'name': resource, 'status': self.states[resource], 'config': serving,
                    'fileCount': str(len(self.maps[resource]))}
        if resource.endswith('/releases'):
            assert query == {'versionName': self.new}
            self.live = self.new
            self.message = body['message']
            self.release_name = 'sites/' + MODULE.SITE + '/releases/new-fixture'
            return {'name': self.release_name, 'message': self.message, 'type': 'DEPLOY',
                    'version': {'name': self.new, 'status': 'FINALIZED'}}
        raise AssertionError((method, resource))

    def request(self, method, url, body, upload=False):
        assert method == 'POST' and upload
        MODULE.Client.check_url(url)
        digest = url.rsplit('/', 1)[-1]
        assert MODULE.sha(body) == digest and gzip.decompress(body)
        self.uploads.append((digest, len(body)))
        for path, item in self.maps[self.new].items():
            if item[0] == digest:
                self.maps[self.new][path] = (digest, 'ACTIVE')
        return {'uploaded': True}


def execute(*args):
    with contextlib.redirect_stdout(io.StringIO()):
        return MODULE.execute(*args)


class OverlayTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name) / 'source'
        self.root.mkdir()
        self.source = fixture(self.root)
        self.receipts = Path(self.temp.name) / 'receipts'
        self.receipts.mkdir()

    def tearDown(self):
        self.temp.cleanup()

    def test_sparse_source_requires_no_historical_apks(self):
        self.assertFalse((self.root / 'public/historical').exists())
        self.assertEqual(len(self.source['baselineFiles']), 525)
        self.assertEqual(len(self.source['overlay']), 4)
        self.assertEqual(MODULE.compressed(b'hello'), MODULE.compressed(b'hello'))
        (self.root / 'public/index.html').write_text('changed')
        with self.assertRaises(MODULE.DeploymentError):
            MODULE.validate_source(self.root, self.source['commit'])

    def test_readonly_preflight_has_no_mutations(self):
        client = FakeClient(self.source)
        result = execute(self.source, client, client.old, self.receipts, False)
        self.assertEqual(result['mode'], 'read-only-preflight')
        self.assertTrue(all(call[0] == 'GET' for call in client.calls))
        self.assertFalse(client.uploads)

    def test_exact_clone_overlay_and_cache_hit(self):
        client = FakeClient(self.source)
        result = execute(self.source, client, client.old, self.receipts, True)
        self.assertEqual(result['status'], 'DEPLOY/FINALIZED')
        self.assertEqual(result['verifiedProviderFiles'], 530)
        self.assertEqual(result['historicalPhysicalSourcePaths'], 525)
        self.assertEqual(result['retainedHistoricalPhysicalSourcePaths'], 524)
        self.assertEqual(result['historicalProviderFiles'], 527)
        self.assertEqual(result['retainedHistoricalProviderPaths'], 526)
        self.assertEqual(result['expectedFinalPhysicalSourcePaths'], 528)
        self.assertEqual(result['expectedFinalProviderPaths'], 530)
        self.assertEqual(result['firebaseManagedReservedPathCount'], 2)
        self.assertEqual(result['uploadedOverlayBlobs'], 3)
        self.assertFalse(result['historicalApksUploaded'])
        for path, row in client.maps[client.old].items():
            if path != '/index.html':
                self.assertEqual(client.maps[client.new][path], row)
        self.assertEqual(len([c for c in client.calls if c[1].endswith('/releases')]), 1)
        self.assertEqual(client.live_reads, 4)

    def test_exact_reserved_extras_only(self):
        client = FakeClient(self.source, 'unknown_reserved_extra')
        with self.assertRaises(MODULE.DeploymentError):
            execute(self.source, client, client.old, self.receipts, True)
        self.assertTrue(all(call[0] == 'GET' for call in client.calls))
        self.assertFalse(client.uploads)
        del client.maps[client.old]['/__/firebase/unexpected.json']
        del client.maps[client.old]['/__/firebase/init.js']
        with self.assertRaises(MODULE.DeploymentError):
            execute(self.source, client, client.old, self.receipts, True)
        self.assertTrue(all(call[0] == 'GET' for call in client.calls))

    def test_faults_never_release(self):
        for fault in ('clone_file_changed', 'clone_config_changed', 'unexpected_hash',
                      'untrusted_upload', 'race_before_clone', 'race_before_release'):
            with self.subTest(fault=fault):
                directory = self.receipts / fault
                directory.mkdir()
                client = FakeClient(self.source, fault)
                with self.assertRaises(MODULE.DeploymentError):
                    execute(self.source, client, client.old, directory, True)
                self.assertFalse(any(c[1].endswith('/releases') for c in client.calls))
                if fault in ('unexpected_hash', 'untrusted_upload', 'race_before_clone'):
                    self.assertFalse(client.uploads)

    def test_auth_host_pinning_and_errors_do_not_leak_token(self):
        for url in ('http://firebasehosting.googleapis.com/v1beta1/sites/x',
                    'https://firebasehosting.googleapis.com.evil.invalid/v1beta1/sites/x',
                    'https://example.invalid/upload',
                    'https://user@firebasehosting.googleapis.com/v1beta1/sites/x'):
            with self.assertRaises(MODULE.DeploymentError):
                MODULE.Client.check_url(url)
        token = 'do_not_log_credential_marker_123456'
        client = MODULE.Client(token)
        class RefusingOpener:
            def open(self, request, timeout):
                assert request.headers['Authorization'] == 'Bearer ' + token
                raise urllib.error.HTTPError(request.full_url, 403, token, {}, None)
        client._opener = RefusingOpener()
        with self.assertRaises(MODULE.DeploymentError) as failure:
            client.api('GET', 'sites/' + MODULE.SITE + '/channels/live')
        self.assertNotIn(token, str(failure.exception))


if __name__ == '__main__':
    unittest.main()
