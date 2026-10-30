#!/usr/bin/env python3
"""Collect 1.23 public response identities, only with --fetch.

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
SOURCE = 'docs/validation/site-v1.23/release/source-manifest.json'
BASELINE = 'docs/validation/site-v1.22/release/source-manifest.json'
PREVIOUS122 = 'b122c0acbd172a7912c69b0b6914b8b0f9b2a243'
REQUIRED_ADDITIONS = frozenset(
    ['/assets/site-v123.css', '/assets/site-v123.js', '/lab/clarity-123/index.html',
     '/releases/manifest-1.23.0-rc2.json', '/releases/luma-drift-1.23.0-rc2.apk',
     '/releases/luma-drift-1.23.0-sandbox-rc2.apk'] +
    ['/assets/screenshots/v1.23/' + key + '-' + language + '.png'
     for key in ('puzzle', 'skills', 'wardrobe', 'map') for language in ('fr', 'en')])

RETIRED_PATHS = frozenset(['/releases/luma-drift-1.15.0-rc1.aab', '/releases/luma-drift-1.15.0-rc1.apk', '/releases/luma-drift-1.16.0-rc1.aab', '/releases/luma-drift-1.16.0-rc1.apk', '/releases/luma-drift-1.17.0-rc6.aab', '/releases/luma-drift-1.17.0-rc6.apk', '/releases/luma-drift-1.18.0-prototype1.apk', '/releases/luma-drift-1.18.0-sandbox-prototype1.apk', '/releases/luma-drift-1.19.0-prototype2.apk', '/releases/luma-drift-1.19.0-sandbox-prototype2.apk', '/releases/luma-drift-1.20.0-rc1.apk', '/releases/luma-drift-1.20.0-sandbox-rc1.apk', '/releases/luma-drift-1.21.0-rc1.apk', '/releases/luma-drift-1.21.0-sandbox-rc1.apk'])
CHUNK = 1024 * 1024
MIMES = {'.html': {'text/html'}, '.css': {'text/css'},
         '.js': {'text/javascript', 'application/javascript'},
         '.mjs': {'text/javascript', 'application/javascript'},
         '.json': {'application/json'}, '.png': {'image/png'},
         '.svg': {'image/svg+xml'}, '.webp': {'image/webp'},
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


def validate_entries(selected):
    for name, value in selected.items():
        bounded_path(name)
        require(set(value) == {'sha256', 'bytes'} and re.fullmatch(r'[a-f0-9]{64}', value['sha256'])
                and type(value['bytes']) is int and 0 < value['bytes'] <= 1024 ** 3,
                'Invalid pinned response identity: ' + name)
        require(Path(name).suffix in MIMES, 'Unqualified content type: ' + name)


def select(source, baseline):
    require(source.get('status') == 'PASS' and source.get('edition') == '1.23'
            and source.get('release') == '1.23.0-rc2'
            and source.get('native_capture_provenance_verified') and source.get('package_identity_verified'),
            'An accepted 1.23 source manifest is required')
    require(baseline.get('status') == 'PASS' and baseline.get('edition') == '1.22'
            and baseline.get('release') == '1.22.0-rc1', 'Accepted C3 baseline required')
    require(source['firebase_config_sha256'] == baseline['firebase_config_sha256'], 'Hosting config changed')
    before, current = baseline['files'], source['files']
    require(len(before) == 570 and set(before)-set(current) == RETIRED_PATHS, 'Only the 14 explicitly retired binaries may be deleted')
    require(source.get('retired_files') == {name:before[name] for name in RETIRED_PATHS}, 'Retired metadata must match immutable C3 rows')
    changed = sorted(name for name in before if name in current and before[name] != current[name])
    added = set(current) - set(before)
    require(changed == ['/index.html'], 'Only the homepage may overwrite a C3 payload')
    require(REQUIRED_ADDITIONS <= added, 'Missing required 1.23 release, native capture, asset or report')
    require(all(name in REQUIRED_ADDITIONS or name.startswith('/lab/clarity-123/') for name in added),
            'Unexpected new public path')
    selected = {name: value for name, value in sorted(current.items()) if before.get(name) != value}
    validate_entries(selected)
    for channel, name in [('production', '/releases/luma-drift-1.23.0-rc2.apk'),
                          ('lab', '/releases/luma-drift-1.23.0-sandbox-rc2.apk')]:
        require(selected[name]['sha256'] == source[channel + '_apk_sha256'], 'Full APK identity differs: ' + channel)
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


def retired_response(path, commit, opener):
    """Verify public retirement without reading any obsolete APK/AAB body."""
    require(path in RETIRED_PATHS, 'Retirement probe outside exact allowlist')
    require(re.fullmatch(r'[a-f0-9]{40}', commit), 'Exact source commit required')
    bounded_path(path)
    url = ORIGIN + path + '?source_commit=' + commit
    request = urllib.request.Request(url, headers={'Accept-Encoding': 'identity'}, method='GET')
    metadata = {'url': url, 'expectedStatus': 404, 'bodyRead': False}
    try:
        with opener.open(request, timeout=30) as response:
            require(response.geturl() == url, 'Retirement response redirected', True, metadata)
            metadata['status'] = response.status
            require(response.status == 404, 'Retired path is still served; stopped before reading its body', response=metadata)
            return metadata
    except urllib.error.HTTPError as error:
        metadata.update(status=error.code, contentType=error.headers.get('Content-Type', ''))
        require(error.geturl() == url, 'Retirement response redirected', True, metadata)
        if error.code == 404:
            error.close()
            return metadata
        raise CollectionError('Retired path returned HTTP ' + str(error.code) + '; no fallback attempted',
                              error.code in (401, 403, 407, 429, 451), metadata) from error
    except (ssl.SSLError, urllib.error.URLError) as error:
        security = isinstance(error, ssl.SSLError) or isinstance(getattr(error, 'reason', None), ssl.SSLError)
        raise CollectionError('Retirement response connection/security failure; no fallback attempted', security, metadata) from error


def collect(selected, commit, receipt, opener, save, retired=()):
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
    receipt.setdefault('retired_responses', {})
    for path in sorted(retired):
        try:
            receipt['retired_responses'][path] = retired_response(path, commit, opener)
        except CollectionError as error:
            receipt.update(status='REFUSED' if error.refused else 'FAIL', failed_path=path,
                           error=str(error), stopped_at_utc=utc(), no_fallback_attempted=True)
            if error.response is not None:
                receipt['retired_responses'][path] = error.response
            save(receipt)
            raise
        save(receipt)
    receipt.update(status='PASS', completed_at_utc=utc(), verified_bytes=sum(row['bytes'] for row in receipt['files'].values()))
    save(receipt)
    return receipt


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--commit', required=True)
    parser.add_argument('--output', required=True, type=Path)
    parser.add_argument('--previous-source-commit', default=PREVIOUS122, help='Pinned accepted 1.22 C3 baseline')
    parser.add_argument('--fetch', action='store_true', help='Explicitly perform the one-origin GETs after deployment authorization')
    args = parser.parse_args()
    require(re.fullmatch(r'[a-f0-9]{40}', args.commit), 'Exact source commit required')
    require(args.previous_source_commit == PREVIOUS122,
            'Only exact accepted C3 is a response baseline')
    output = args.output.resolve()
    require(ROOT.resolve() != output and ROOT.resolve() not in output.parents and not output.exists(),
            'Use a new receipt path outside checkout')
    committed = subprocess.check_output(['git', 'show', args.commit + ':' + SOURCE], cwd=ROOT)
    source_path, baseline_path = ROOT / SOURCE, ROOT / BASELINE
    require(hashlib.sha256(committed).hexdigest() == file_sha(source_path), 'Local manifest differs from exact source commit')
    source = json.loads(committed)
    baseline_committed = subprocess.check_output(['git', 'show', PREVIOUS122 + ':' + BASELINE], cwd=ROOT)
    require(hashlib.sha256(baseline_committed).hexdigest() == file_sha(baseline_path), 'Local C3 baseline differs from its pinned commit')
    selected = select(source, json.loads(baseline_committed))
    selection = 'All new 1.23 paths and the changed homepage against exact 1.22 C3'
    receipt = {'schema': 'luma-site123-public-response-v1', 'status': 'RUNNING' if args.fetch else 'PLANNED',
               'origin': ORIGIN, 'source_commit': args.commit, 'source_manifest_sha256': file_sha(source_path),
               'baseline_manifest_sha256': hashlib.sha256(baseline_committed).hexdigest(),
               'previous_source_commit': args.previous_source_commit,
               'previous_source_manifest_sha256': hashlib.sha256(baseline_committed).hexdigest() if args.previous_source_commit else None,
               'started_at_utc': utc(),
               'selected_paths': len(selected), 'expected_bytes': sum(row['bytes'] for row in selected.values()),
               'retired_paths': sorted(RETIRED_PATHS), 'retired_responses': {},
               'selection': selection,
               'expected_files': selected, 'files': {}, 'responses': {},
               'network_requested': args.fetch, 'credentials_used': False, 'redirects_followed': False,
               'rendering_verified': False, 'scope': 'Selected new/changed complete HTTP bodies plus HTTP404 checks for the exact14 retired binary paths without reading their bodies; unchanged responses and browser rendering remain separate evidence.'}
    output.parent.mkdir(parents=True, exist_ok=True)
    # Preserve the original incrementally updated JSON bytes at the caller's path;
    # no later transformation is needed for a release evidence bundle.
    save = lambda value: output.write_text(json.dumps(value, indent=2) + '\n')
    save(receipt)
    if not args.fetch:
        print(json.dumps(receipt))
        return 0
    opener = urllib.request.build_opener(urllib.request.ProxyHandler({}), NoRedirect())
    try:
        result = collect(selected, args.commit, receipt, opener, save, RETIRED_PATHS)
    except (CollectionError, OSError) as error:
        if receipt['status'] == 'RUNNING':
            receipt.update(status='FAIL', error=str(error), stopped_at_utc=utc(), no_fallback_attempted=True)
            save(receipt)
        print('Public collection stopped: ' + str(error), file=sys.stderr)
        return 1
    print(json.dumps({'status': result['status'], 'responses': len(result['files']), 'retirementsVerified': len(result['retired_responses']), 'bytes': result['verified_bytes'],
                      'receipt': str(output), 'rendering_verified': False}))
    return 0


if __name__ == '__main__':
    sys.exit(main())
