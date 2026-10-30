#!/usr/bin/env python3
"""Collect the 35 new/changed 1.22 public response identities, only with --fetch.

One fixed HTTPS origin, no credentials, proxy, redirects, retries or fallback.
Bodies stream through SHA-256 without being saved. A refusal stops the entire
collection. Response bytes establish neither browser rendering nor human use.
"""
import argparse
import datetime
import hashlib
import json
from pathlib import Path, PurePosixPath
import re
import ssl
import subprocess
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
ORIGIN = 'https://luma-drift-production.web.app'
SOURCE = 'docs/validation/site-v1.22/release/source-manifest.json'
BASELINE = 'docs/validation/cognitive-lab/site-manifest.json'
CHUNK = 1024 * 1024
MIMES = {'.html': {'text/html'}, '.css': {'text/css'},
         '.js': {'text/javascript', 'application/javascript'},
         '.mjs': {'text/javascript', 'application/javascript'},
         '.json': {'application/json'}, '.png': {'image/png'},
         '.zip': {'application/zip', 'application/x-zip-compressed'},
         '.gz': {'application/gzip', 'application/x-gzip'},
         '.apk': {'application/vnd.android.package-archive', 'application/octet-stream'}}


class CollectionError(ValueError):
    def __init__(self, message, refused=False, response=None):
        super().__init__(message)
        self.refused = refused
        self.response = response


def require(condition, message, refused=False, response=None):
    if not condition:
        raise CollectionError(message, refused, response)


def utc():
    return datetime.datetime.now(datetime.timezone.utc).isoformat()


def file_sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def bounded_path(name):
    require(isinstance(name, str) and re.fullmatch(r'/[A-Za-z0-9._/-]+', name)
            and str(PurePosixPath(name)) == name and '..' not in PurePosixPath(name).parts
            and not name.startswith('//'), 'Unbounded or noncanonical response path')


def select(source, baseline):
    require(source.get('status') == 'PASS' and source.get('edition') == '1.22'
            and source.get('native_capture_provenance_verified') and source.get('package_identity_verified'),
            'An accepted 1.22 source manifest is required')
    before, current = baseline['files'], source['files']
    require(len(before) == 539 and set(before) <= set(current), 'Historical physical baseline differs')
    selected = {name: value for name, value in sorted(current.items()) if before.get(name) != value}
    require(len(selected) == 35, 'Expected exactly 31 new and four changed physical paths')
    for name, value in selected.items():
        bounded_path(name)
        require(set(value) == {'sha256', 'bytes'} and re.fullmatch(r'[a-f0-9]{64}', value['sha256'])
                and type(value['bytes']) is int and 0 < value['bytes'] <= 1024 ** 3,
                'Invalid pinned response identity: ' + name)
        require(Path(name).suffix in MIMES, 'Unqualified content type: ' + name)
    for name in ('/releases/luma-drift-1.22.0-rc1.apk', '/releases/luma-drift-1.22.0-sandbox-rc1.apk',
                 '/lab/cognitive/cognitive-lab-v122-local.zip'):
        require(name in selected, 'Missing full release payload: ' + name)
    require(sum(name.endswith('.csv.gz') for name in selected) == 6, 'Six raw gzip datasets required')
    return selected


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        raise CollectionError('Public access redirected; stopped without following it or using another route', True)


def response_identity(path, expected, commit, opener, timeout=30, max_seconds=900):
    require(re.fullmatch(r'[a-f0-9]{40}', commit), 'Exact source commit required')
    bounded_path(path)
    url = ORIGIN + path + '?source_commit=' + commit
    parsed = urllib.parse.urlsplit(url)
    require(parsed.scheme == 'https' and parsed.netloc == 'luma-drift-production.web.app'
            and parsed.path == path and not parsed.fragment, 'Only the fixed HTTPS origin is allowed')
    request = urllib.request.Request(url, headers={'Accept-Encoding': 'identity'}, method='GET')
    started = time.monotonic()
    metadata = {'url': url, 'requestedEncoding': 'identity', 'startedAtUtc': utc()}
    try:
        with opener.open(request, timeout=timeout) as response:
            metadata.update(status=response.status, contentType=response.headers.get('Content-Type', ''),
                            contentEncoding=response.headers.get('Content-Encoding', ''),
                            contentLength=response.headers.get('Content-Length'),
                            etag=response.headers.get('ETag'), lastModified=response.headers.get('Last-Modified'),
                            date=response.headers.get('Date'), cacheControl=response.headers.get('Cache-Control'),
                            age=response.headers.get('Age'))
            require(response.geturl() == url, 'Public response URL changed; collection stopped', True, metadata)
            require(response.status == 200, 'Unexpected public HTTP status',
                    response.status in (401, 403, 407, 429, 451), metadata)
            mime = metadata['contentType'].split(';', 1)[0].strip().lower()
            require(mime in MIMES[Path(path).suffix], 'Unexpected public content type: ' + path, response=metadata)
            require(metadata['contentEncoding'].lower().strip() in ('', 'identity'),
                    'Server did not honor identity response encoding: ' + path, response=metadata)
            length = metadata['contentLength']
            require(length is None or length.isdigit() and int(length) == expected['bytes'],
                    'Public response Content-Length differs: ' + path, response=metadata)
            total, digest = 0, hashlib.sha256()
            while True:
                require(time.monotonic() - started <= max_seconds, 'Response exceeded its time bound', response=metadata)
                block = response.read(min(CHUNK, expected['bytes'] - total + 1))
                if not block:
                    break
                total += len(block)
                require(total <= expected['bytes'], 'Response exceeds pinned size: ' + path, response=metadata)
                digest.update(block)
            observed = {'sha256': digest.hexdigest(), 'bytes': total}
            metadata.update(observed, durationSeconds=round(time.monotonic() - started, 3))
            require(observed == expected, 'Public response SHA-256/size differs; no fallback attempted: ' + path,
                    response=metadata)
            return observed, metadata
    except urllib.error.HTTPError as error:
        metadata.update(status=error.code, contentType=error.headers.get('Content-Type', ''))
        raise CollectionError('Public HTTP ' + str(error.code) + '; stopped without retry or alternative route',
                              error.code in (401, 403, 407, 429, 451), metadata) from error
    except (ssl.SSLError, urllib.error.URLError) as error:
        security = isinstance(error, ssl.SSLError) or isinstance(getattr(error, 'reason', None), ssl.SSLError)
        raise CollectionError(('TLS/security refusal' if security else 'Public connection failed')
                              + '; collection stopped without an alternative route', security, metadata) from error


def collect(selected, commit, receipt, opener, save):
    for path, expected in selected.items():
        try:
            observed, metadata = response_identity(path, expected, commit, opener)
        except CollectionError as error:
            receipt.update(status='REFUSED' if error.refused else 'FAIL', failed_path=path,
                           error=str(error), stopped_at_utc=utc(), no_fallback_attempted=True)
            if error.response is not None:
                receipt['responses'][path] = error.response
            save(receipt)
            raise
        receipt['files'][path] = observed
        receipt['responses'][path] = metadata
        save(receipt)
        print(json.dumps({'verifiedPaths': len(receipt['files']), 'expectedPaths': len(selected), 'path': path}), flush=True)
    receipt.update(status='PASS', completed_at_utc=utc(), verified_bytes=sum(row['bytes'] for row in receipt['files'].values()))
    save(receipt)
    return receipt


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--commit', required=True)
    parser.add_argument('--output', required=True, type=Path)
    parser.add_argument('--fetch', action='store_true', help='Explicitly perform the one-origin GETs after deployment authorization')
    args = parser.parse_args()
    require(re.fullmatch(r'[a-f0-9]{40}', args.commit), 'Exact C2 commit required')
    output = args.output.resolve()
    require(ROOT.resolve() != output and ROOT.resolve() not in output.parents and not output.exists(),
            'Use a new receipt path outside checkout')
    committed = subprocess.check_output(['git', 'show', args.commit + ':' + SOURCE], cwd=ROOT)
    source_path, baseline_path = ROOT / SOURCE, ROOT / BASELINE
    require(hashlib.sha256(committed).hexdigest() == file_sha(source_path), 'Local manifest differs from exact C2 commit')
    baseline_committed = subprocess.check_output(['git', 'show', args.commit + ':' + BASELINE], cwd=ROOT)
    require(hashlib.sha256(baseline_committed).hexdigest() == file_sha(baseline_path), 'Local historical baseline differs from C2')
    source = json.loads(committed)
    selected = select(source, json.loads(baseline_committed))
    receipt = {'schema': 'luma-site122-public-response-v1', 'status': 'RUNNING' if args.fetch else 'PLANNED',
               'origin': ORIGIN, 'source_commit': args.commit, 'source_manifest_sha256': file_sha(source_path),
               'baseline_manifest_sha256': file_sha(baseline_path), 'started_at_utc': utc(),
               'selected_paths': len(selected), 'expected_bytes': sum(row['bytes'] for row in selected.values()),
               'selection': 'All31newand4changedphysicalpathsagainsthistorical539baseline',
               'expected_files': selected, 'files': {}, 'responses': {},
               'network_requested': args.fetch, 'credentials_used': False, 'redirects_followed': False,
               'rendering_verified': False, 'scope': 'Only the 35 new/changed complete HTTP response bodies; historical paths and browser rendering are separate evidence.'}
    output.parent.mkdir(parents=True, exist_ok=True)
    # Preserve the original incrementally updated JSON bytes at the caller's path;
    # no later transformation is needed by verify-site-v122-publication.py.
    save = lambda value: output.write_text(json.dumps(value, indent=2) + '\n')
    save(receipt)
    if not args.fetch:
        print(json.dumps(receipt))
        return 0
    opener = urllib.request.build_opener(urllib.request.ProxyHandler({}), NoRedirect())
    try:
        result = collect(selected, args.commit, receipt, opener, save)
    except (CollectionError, OSError) as error:
        if receipt['status'] == 'RUNNING':
            receipt.update(status='FAIL', error=str(error), stopped_at_utc=utc(), no_fallback_attempted=True)
            save(receipt)
        print('Public collection stopped: ' + str(error), file=sys.stderr)
        return 1
    print(json.dumps({'status': result['status'], 'responses': len(result['files']), 'bytes': result['verified_bytes'],
                      'receipt': str(output), 'rendering_verified': False}))
    return 0


if __name__ == '__main__':
    sys.exit(main())
