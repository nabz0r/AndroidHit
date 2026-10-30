#!/usr/bin/env python3
"""Offline collector tests; fabricated response bodies never touch a network."""
import contextlib
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
import urllib.error

ROOT = Path(__file__).resolve().parents[1]
SCRIPT = ROOT / 'scripts/collect-site-v122-public-bytes.py'
MODULE = types.ModuleType('public_collector122')
MODULE.__file__ = str(SCRIPT)
exec(compile(SCRIPT.read_bytes(), str(SCRIPT), 'exec'), MODULE.__dict__)


class Response(io.BytesIO):
    def __init__(self, url, body, mime, status=200, headers=None):
        super().__init__(body)
        self.url, self.status = url, status
        self.headers = {'Content-Type': mime, 'Content-Length': str(len(body)), **(headers or {})}
        self.read_amounts = []

    def geturl(self):
        return self.url

    def read(self, amount):
        self.read_amounts.append(amount)
        return super().read(amount)


class Opener:
    def __init__(self, factory):
        self.factory, self.requests = factory, []

    def open(self, request, timeout):
        self.requests.append(request)
        return self.factory(request)


class CollectorTests(unittest.TestCase):
    def setUp(self):
        self.source = json.loads((ROOT / MODULE.SOURCE).read_text())
        self.baseline = json.loads((ROOT / MODULE.BASELINE).read_text())
        self.selected = MODULE.select(self.source, self.baseline)
        self.commit = 'c' * 40
        self.guard = patch.object(MODULE.urllib.request.OpenerDirector, 'open', side_effect=AssertionError('Network prohibited in tests'))
        self.guard.start()

    def tearDown(self):
        self.guard.stop()

    def test_actual_manifests_select_all35_and_include_both_full_signed_apks(self):
        self.assertEqual(len(self.selected), 35)
        self.assertEqual(sum(name.endswith('.apk') for name in self.selected), 2)
        self.assertEqual(sum(name.endswith('.csv.gz') for name in self.selected), 6)
        self.assertEqual(self.selected['/releases/luma-drift-1.22.0-rc1.apk'],{'sha256':self.source['production_apk_sha256'],'bytes':368986073})
        self.assertEqual(self.selected['/releases/luma-drift-1.22.0-sandbox-rc1.apk'],{'sha256':self.source['lab_apk_sha256'],'bytes':368985756})

    def test_all35_mocked_responses_have_fixed_url_identity_and_exact_receipt_map(self):
        payloads = {name: ('fixture ' + name).encode() for name in self.selected}
        selected = {name: {'sha256': hashlib.sha256(raw).hexdigest(), 'bytes': len(raw)} for name, raw in payloads.items()}

        def response(request):
            name = MODULE.urllib.parse.urlsplit(request.full_url).path
            mime = sorted(MODULE.MIMES[Path(name).suffix])[0]
            return Response(request.full_url, payloads[name], mime)

        opener = Opener(response)
        receipt = {'status': 'RUNNING', 'files': {}, 'responses': {}}
        saved = []
        with contextlib.redirect_stdout(io.StringIO()):
            result = MODULE.collect(selected, self.commit, receipt, opener, lambda value: saved.append(copy.deepcopy(value)))
        self.assertEqual(result['status'], 'PASS')
        self.assertEqual(result['files'], selected)
        self.assertEqual(len(result['responses']), 35)
        self.assertEqual(len(saved), 36)
        for request in opener.requests:
            self.assertTrue(request.full_url.startswith(MODULE.ORIGIN + '/'))
            self.assertEqual(MODULE.urllib.parse.urlsplit(request.full_url).query, 'source_commit=' + self.commit)
            self.assertEqual(request.headers, {'Accept-encoding': 'identity'})

    def test_access_refusal_stops_before_any_second_request(self):
        def denied(request):
            raise urllib.error.HTTPError(request.full_url, 403, 'Forbidden', {'Content-Type': 'text/html'}, None)

        opener = Opener(denied)
        receipt = {'status': 'RUNNING', 'files': {}, 'responses': {}}
        with self.assertRaises(MODULE.CollectionError):
            MODULE.collect(self.selected, self.commit, receipt, opener, lambda value: None)
        self.assertEqual(len(opener.requests), 1)
        self.assertEqual(receipt['status'], 'REFUSED')
        self.assertEqual(receipt['files'], {})
        self.assertTrue(receipt['no_fallback_attempted'])

    def test_hash_type_encoding_and_size_mismatches_refuse(self):
        expected = {'sha256': hashlib.sha256(b'good').hexdigest(), 'bytes': 4}
        for raw, mime, headers in [(b'evil', 'text/html', {}), (b'good', 'application/json', {}),
                                   (b'good', 'text/html', {'Content-Encoding': 'gzip'}),
                                   (b'longer', 'text/html', {'Content-Length': '4'})]:
            with self.subTest(raw=raw, mime=mime, headers=headers):
                response = Response(MODULE.ORIGIN + '/index.html?source_commit=' + self.commit, raw, mime, headers=headers)
                with self.assertRaises(MODULE.CollectionError):
                    MODULE.response_identity('/index.html', expected, self.commit, Opener(lambda request: response))
                if raw == b'longer':
                    self.assertEqual(response.read_amounts, [5])

    def test_redirect_and_tls_refusals_have_no_fallback(self):
        with self.assertRaises(MODULE.CollectionError) as failure:
            MODULE.NoRedirect().redirect_request(None, None, 302, 'redirect', {}, 'https://elsewhere.invalid/')
        self.assertTrue(failure.exception.refused)

        def failed_tls(request):
            raise urllib.error.URLError(MODULE.ssl.SSLCertVerificationError('fixture certificate refusal'))

        with self.assertRaises(MODULE.CollectionError) as failure:
            MODULE.response_identity('/index.html', {'sha256': 'a' * 64, 'bytes': 4}, self.commit, Opener(failed_tls))
        self.assertTrue(failure.exception.refused)

    def test_noncanonical_paths_and_missing_payloads_refuse_before_network(self):
        source = copy.deepcopy(self.source)
        entry = source['files'].pop('/assets/site-v122.css')
        source['files']['/../escape.css'] = entry
        with self.assertRaises(MODULE.CollectionError):
            MODULE.select(source, self.baseline)
        with self.assertRaises(MODULE.CollectionError):
            MODULE.response_identity('//other.invalid/index.html', entry, self.commit, Opener(lambda request: self.fail('Unexpected request')))

    def test_receipt_shape_is_accepted_by_existing_offline_verifier(self):
        # This is a synthetic schema test, not an observed-public receipt.
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / 'synthetic-schema-only.json'
            path.write_text(json.dumps({'status': 'PASS', 'origin': MODULE.ORIGIN,
                                        'source_commit': self.commit, 'files': self.selected,
                                        'responses': {}}))
            result = subprocess.run(['python3', str(ROOT / 'scripts/verify-site-v122-publication.py'),
                                     '--receipt', str(path), '--commit', self.commit], capture_output=True, text=True)
            self.assertEqual(result.returncode, 0, result.stderr)
            self.assertEqual(json.loads(result.stdout)['recordedResponsePaths'], 35)

    def test_incremental_actual_three_changed_responses_exclude_unchanged_apks_and_datasets(self):
        previous=copy.deepcopy(self.source);current=copy.deepcopy(previous);bodies={}
        for name in MODULE.INCREMENTAL_ALLOWED:
            bodies[name]=('new fixture '+name).encode();current['files'][name]={'sha256':hashlib.sha256(bodies[name]).hexdigest(),'bytes':len(bodies[name])}
        selected=MODULE.select_incremental(current,previous)
        self.assertEqual(set(selected),MODULE.INCREMENTAL_ALLOWED)
        opener=Opener(lambda request:Response(request.full_url,bodies[MODULE.urllib.parse.urlsplit(request.full_url).path],sorted(MODULE.MIMES[Path(MODULE.urllib.parse.urlsplit(request.full_url).path).suffix])[0]))
        receipt={'status':'RUNNING','files':{},'responses':{}}
        with contextlib.redirect_stdout(io.StringIO()):MODULE.collect(selected,self.commit,receipt,opener,lambda value:None)
        self.assertEqual(len(opener.requests),3);self.assertEqual(receipt['status'],'PASS')
        self.assertFalse(any('.apk' in request.full_url or '.csv.gz' in request.full_url for request in opener.requests))

    def test_incremental_unknown_added_deleted_and_android_identity_changes_refuse(self):
        previous=copy.deepcopy(self.source)
        for mutation in (lambda value:value['files']['/index.html'].update(sha256='0'*64),lambda value:value['files'].update({'/new.json':{'sha256':'0'*64,'bytes':1}}),lambda value:value['files'].pop('/index.html'),lambda value:value.update(production_apk_sha256='0'*64)):
            with self.subTest(mutation=mutation):
                current=copy.deepcopy(previous);mutation(current)
                with self.assertRaises(MODULE.CollectionError):MODULE.select_incremental(current,previous)


if __name__ == '__main__':
    unittest.main()
