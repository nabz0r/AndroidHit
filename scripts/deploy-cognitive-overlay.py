#!/usr/bin/env python3
"""Clone the existing Firebase version and upload only the audited Cognitive Lab overlay.

Default is authenticated read-only preflight. --deploy creates a new version and
releases it only after exact management-file and config verification. Historical
APKs are preserved server-side and are never downloaded, recompressed or uploaded.
Receipts prove management identities; they do not prove public HTTP bytes/rendering.

Official API contracts:
https://firebase.google.com/docs/reference/hosting/rest/v1beta1/sites.versions/clone
https://firebase.google.com/docs/reference/hosting/rest/v1beta1/sites.versions/populateFiles
https://firebase.google.com/docs/hosting/api-deploy
"""
import argparse
import datetime
import gzip
import hashlib
import io
import json
from pathlib import Path, PurePosixPath
import re
import subprocess
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
SITE = 'luma-drift-production'
API = 'https://firebasehosting.googleapis.com/v1beta1/'
UPLOAD_HOST = 'upload-firebasehosting.googleapis.com'
EXPECTED_LIVE = 'sites/luma-drift-production/versions/d08a2aa131c05c4f'
FIREBASE_RESERVED = frozenset(('/__/firebase/init.js', '/__/firebase/init.json'))
UPDATE_ALLOWED = frozenset(('/lab/cognitive/index.html', '/lab/cognitive/lab.js',
                            '/lab/cognitive/cognitive-lab-local.zip'))
MANIFEST = 'docs/validation/cognitive-lab/site-manifest.json'
BASELINE = 'docs/validation/site-v1.21/release/source-manifest.json'
CONFIG = 'firebase.json'
HEX = re.compile(r'[a-f0-9]{64}\Z')
VERSION = re.compile(r'sites/' + SITE + r'/versions/[A-Za-z0-9_-]+\Z')
OPERATION = re.compile(r'projects/[A-Za-z0-9_-]+/operations/[A-Za-z0-9_-]+\Z')
RELEASE_NAME = re.compile(r'sites/' + SITE + r'/(?:channels/live/)?releases/([A-Za-z0-9_-]+)\Z')


class DeploymentError(RuntimeError):
    """Deliberately sanitized failure; request headers and token never appear."""


def require(condition, message):
    if not condition:
        raise DeploymentError(message)


def sha(data):
    return hashlib.sha256(data).hexdigest()


def canonical_hash(value):
    return sha(json.dumps(value, sort_keys=True, separators=(',', ':')).encode())


def git(root, *args, binary=False):
    result = subprocess.run(['git', *args], cwd=root, stdout=subprocess.PIPE,
                            stderr=subprocess.PIPE, check=False)
    require(result.returncode == 0, 'Git verification failed: ' + args[0])
    return result.stdout if binary else result.stdout.decode().strip()


def tracked_bytes(root, commit, name):
    data = (root / name).read_bytes()
    require(data == git(root, 'show', commit + ':' + name, binary=True),
            'Working metadata or overlay differs from exact commit: ' + name)
    return data


def valid_path(name):
    return (isinstance(name, str) and name.startswith('/') and
            not name.startswith('//') and '\\' not in name and '\x00' not in name and
            str(PurePosixPath(name)) == name and '..' not in PurePosixPath(name).parts)


def compressed(data):
    # GzipFile with an empty name, fixed mtime and level produces a stable header
    # across supported Python versions; no local filename or wall clock enters it.
    target = io.BytesIO()
    with gzip.GzipFile(filename='', mode='wb', fileobj=target, mtime=0, compresslevel=9) as handle:
        handle.write(data)
    return target.getvalue()


def validate_source(root, commit):
    require(re.fullmatch(r'[a-f0-9]{40}', commit) is not None, 'Exact 40-character commit required')
    require(git(root, 'rev-parse', 'HEAD') == commit, 'HEAD differs from requested source commit')
    require(not git(root, 'status', '--porcelain', '--untracked-files=all'), 'Source checkout must be clean')
    metadata = tracked_bytes(root, commit, MANIFEST)
    source = json.loads(metadata)
    require(source.get('status') == 'PASS' and source.get('release') == 'cognitive-lab-1',
            'An audited Cognitive Lab 1 manifest is required')
    require(source.get('androidReleaseUnchanged') == '1.21.0-rc1', 'Unexpected Android release metadata')
    require(source.get('baselineManifest') == BASELINE, 'Unexpected baseline manifest')
    baseline_bytes = tracked_bytes(root, commit, BASELINE)
    require(sha(baseline_bytes) == source['baselineManifestSha256'], 'Frozen baseline manifest hash differs')
    baseline = json.loads(baseline_bytes)
    require(baseline.get('status') == 'PASS' and baseline.get('release') == '1.21.0-rc1',
            'Historical baseline metadata is not the audited 1.21 release')
    config_bytes = tracked_bytes(root, commit, CONFIG)
    require(sha(config_bytes) == source['firebaseConfigSha256'], 'Source Hosting config hash differs')
    local_config = json.loads(config_bytes)['hosting']
    require(local_config['site'] == SITE and local_config['public'] == 'public', 'Unexpected Hosting destination')
    previous, current = baseline['files'], source['files']
    require(len(previous) == 525, 'Historical baseline must have exactly 525 payload paths')
    for rows in (previous, current):
        for name, entry in rows.items():
            require(valid_path(name) and HEX.fullmatch(entry['sha256']) is not None and
                    isinstance(entry['bytes'], int) and entry['bytes'] >= 0,
                    'Malformed source payload metadata')
    require(set(previous).issubset(current), 'Historical source payload removed')
    changed = sorted(name for name in previous if previous[name] != current[name])
    added = sorted(set(current) - set(previous))
    require(changed == ['/index.html'] and source['intentionalChanges'] == changed,
            'Only the existing /index.html may change')
    require(added and added == source['newPayloads'] and
            all(name.startswith('/lab/cognitive/') for name in added),
            'Only the audited /lab/cognitive/ additions may be deployed')
    require(source['preservedPayloads'] == len(previous) - 1, 'Preserved source count differs')
    tree = git(root, 'ls-tree', '-r', '--name-only', commit, 'public/lab/cognitive').splitlines()
    deployable = {'/' + name.removeprefix('public/') for name in tree
                  if not any(part.startswith('.') or part == 'tests'
                             for part in PurePosixPath(name).parts[1:])}
    require(deployable == set(added), 'Committed deployable Lab files differ from audited additions')
    overlay, blobs = {}, {}
    for name in sorted(changed + added):
        relative = 'public' + name
        path = (root / relative).resolve()
        require(root.resolve() in path.parents and path.is_file(), 'Missing bounded overlay input: ' + name)
        raw = tracked_bytes(root, commit, relative)
        entry = current[name]
        require(sha(raw) == entry['sha256'] and len(raw) == entry['bytes'], 'Overlay source hash differs: ' + name)
        encoded = compressed(raw)
        digest = sha(encoded)
        overlay[name] = {'rawSha256': sha(raw), 'rawBytes': len(raw),
                         'gzipSha256': digest, 'gzipBytes': len(encoded)}
        blobs[digest] = encoded
    require(overlay['/lab/cognitive/data/provenance.json']['rawSha256'] ==
            source['simulationProvenanceSha256'], 'Simulation provenance metadata differs')
    return {'commit': commit, 'root': root.resolve(), 'manifestSha256': sha(metadata),
            'baselineManifestSha256': sha(baseline_bytes), 'baselineFiles': previous,
            'allSourceFiles': current, 'overlay': overlay, 'blobs': blobs,
            'publicTree': git(root, 'rev-parse', commit + ':public'),
            'firebaseConfigSha256': sha(config_bytes)}


def access_token():
    # stdout is captured in process memory only; stderr is discarded because an
    # auth diagnostic can contain sensitive context. No environment/token logging.
    for args in (['gcloud', 'auth', 'print-access-token', '--quiet'],
                 ['gcloud', 'auth', 'application-default', 'print-access-token', '--quiet']):
        try:
            result = subprocess.run(args, stdout=subprocess.PIPE, stderr=subprocess.DEVNULL,
                                    check=False, timeout=45)
        except (OSError, subprocess.TimeoutExpired):
            continue
        value = result.stdout.decode(errors='replace').strip()
        if result.returncode == 0 and 20 < len(value) < 8192 and not any(c.isspace() for c in value):
            return value
    raise DeploymentError('No usable existing gcloud or application-default access token; no deployment attempted')


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        raise DeploymentError('Provider HTTP redirect refused; credentials remain on the pinned Google host')


class Client:
    def __init__(self, token):
        self._token = token
        self._opener = urllib.request.build_opener(NoRedirect())

    @staticmethod
    def check_url(url):
        parsed = urllib.parse.urlsplit(url)
        require(parsed.scheme == 'https' and parsed.username is None and parsed.password is None and
                parsed.port is None and not parsed.fragment, 'Unsafe provider endpoint')
        allowed = ((parsed.hostname == 'firebasehosting.googleapis.com' and parsed.path.startswith('/v1beta1/')) or
                   (parsed.hostname == UPLOAD_HOST and parsed.path.startswith('/upload/sites/' + SITE + '/versions/')))
        require(allowed, 'Endpoint is outside the pinned Firebase management/upload services')

    def request(self, method, url, body=None, *, upload=False):
        self.check_url(url)
        data = body if upload else (None if body is None else json.dumps(body, separators=(',', ':')).encode())
        headers = {'Authorization': 'Bearer ' + self._token,
                   'Content-Type': 'application/octet-stream' if upload else 'application/json'}
        attempts = 4 if method == 'GET' or upload else 1
        for trial in range(attempts):
            try:
                request = urllib.request.Request(url, data=data, headers=headers, method=method)
                with self._opener.open(request, timeout=60) as response:
                    content = response.read()
                if upload:
                    return {'uploaded': True}
                return json.loads(content) if content else {}
            except urllib.error.HTTPError as error:
                # Never persist/print server bodies or Request objects (credentials).
                retry = error.code in (429, 500, 502, 503, 504)
                if not retry or trial + 1 == attempts:
                    raise DeploymentError('Firebase ' + method + ' failed with HTTP ' + str(error.code)) from None
            except (urllib.error.URLError, TimeoutError, OSError):
                if trial + 1 == attempts:
                    raise DeploymentError('Firebase ' + method + ' failed with a bounded transport error') from None
            except (ValueError, json.JSONDecodeError):
                raise DeploymentError('Firebase returned invalid JSON') from None
            time.sleep(2 ** trial)

    def api(self, method, resource, body=None, **query):
        require(not resource.startswith('/') and '://' not in resource, 'Invalid API resource')
        url = API + resource
        if query:
            url += '?' + urllib.parse.urlencode(query)
        return self.request(method, url, body)


def live_channel(client, expected):
    channel = client.api('GET', 'sites/' + SITE + '/channels/live')
    release = channel.get('release', {})
    version = release.get('version', {})
    require(channel.get('name') == 'sites/' + SITE + '/channels/live', 'Unexpected live channel resource')
    require(version.get('name') == expected and version.get('status') == 'FINALIZED',
            'Live version differs from the exact expected version; refusing to overwrite it')
    require(release.get('type') == 'DEPLOY', 'Expected an active DEPLOY release')
    return {'channel': channel['name'], 'url': channel.get('url'),
            'releaseName': release.get('name'), 'releaseTime': release.get('releaseTime'),
            'releaseMessage': release.get('message'), 'releaseType': release['type'],
            'versionName': version['name'], 'versionStatus': version['status']}


def same_release(first, second):
    """The default-site and channels/live names alias the same release ID."""
    if not isinstance(first, str) or not isinstance(second, str):
        return False
    left, right = RELEASE_NAME.fullmatch(first), RELEASE_NAME.fullmatch(second)
    return bool(left and right and left.group(1) == right.group(1))


def version(client, name, expected_status):
    require(VERSION.fullmatch(name) is not None, 'Unexpected version resource')
    value = client.api('GET', name)
    require(value.get('name') == name and value.get('status') == expected_status, 'Unexpected version state')
    return value


def files(client, name, status='ACTIVE'):
    require(VERSION.fullmatch(name) is not None, 'Unexpected version file resource')
    result, seen, token = {}, set(), None
    while True:
        query = {'pageSize': 1000, 'status': status}
        if token:
            query['pageToken'] = token
        page = client.api('GET', name + '/files', **query)
        for entry in page.get('files', []):
            path, digest = entry.get('path'), entry.get('hash', '')
            require(valid_path(path) and HEX.fullmatch(digest) is not None, 'Invalid provider file identity')
            require(path not in result and entry.get('status') == status, 'Duplicate or unexpected provider file state')
            result[path] = digest
        token = page.get('nextPageToken')
        if not token:
            return dict(sorted(result.items()))
        require(token not in seen and len(seen) < 100, 'Provider pagination did not terminate')
        seen.add(token)


def save(output, name, value):
    (output / name).write_text(json.dumps(value, indent=2, sort_keys=True) + '\n')


def clone(client, old_name, output):
    operation = client.api('POST', 'sites/' + SITE + '/versions:clone',
                           {'sourceVersion': old_name, 'finalize': False})
    operation_name = operation.get('name', '')
    require(OPERATION.fullmatch(operation_name) is not None, 'Unexpected clone operation resource')
    save(output, 'clone-operation.json', {'name': operation_name, 'done': bool(operation.get('done'))})
    deadline = time.monotonic() + 600
    while not operation.get('done'):
        require(time.monotonic() < deadline, 'Clone operation exceeded the ten-minute bound; not released')
        time.sleep(2)
        operation = client.api('GET', operation_name)
        require(operation.get('name') == operation_name, 'Clone operation identity changed')
    require('error' not in operation, 'Clone operation returned an error; not released')
    result = operation.get('response', {})
    name = result.get('name', '')
    require(VERSION.fullmatch(name) is not None and name != old_name and result.get('status') == 'CREATED',
            'Clone did not return a distinct mutable CREATED version')
    save(output, 'clone-operation.json', {'name': operation_name, 'done': True,
                                         'versionName': name, 'versionStatus': result['status']})
    return name


def execute(source, client, expected_live, output, deploy, update_context=None):
    def checked_live():
        current = live_channel(client, expected_live)
        if update_context is not None:
            require(current['releaseMessage'] == update_context['priorReleaseMessage'] and
                    same_release(current['releaseName'], update_context['priorReleaseName']),
                    'Follow-up live release identity differs from the exact prior receipt')
        return current

    live = checked_live()
    old = version(client, expected_live, 'FINALIZED')
    old_files = files(client, expected_live)
    physical_baseline = set(source['baselineFiles'])
    require(not physical_baseline.intersection(FIREBASE_RESERVED),
            'Firebase-managed reserved paths must not be declared as physical source payloads')
    require(set(old_files) == physical_baseline.union(FIREBASE_RESERVED),
            'Live provider paths differ from the exact physical baseline plus two Firebase-managed reserved paths')
    if update_context is not None:
        require(old_files == update_context['priorProviderFiles'] and
                old.get('config', {}) == update_context['priorProviderConfig'],
                'Follow-up current provider hashes or config differ from the prior deployment receipts')
    overlay = source['overlay']
    expected_files = dict(old_files)
    expected_files.update({name: item['gzipSha256'] for name, item in overlay.items()})
    overwritten = physical_baseline.intersection(overlay)
    preflight = {'status': 'PASS', 'mode': 'deploy' if deploy else 'read-only-preflight',
                 'sourceCommit': source['commit'], 'sourcePublicTree': source['publicTree'],
                 'sourceCheckoutClean': True, 'manifestSha256': source['manifestSha256'],
                 'baselineManifestSha256': source['baselineManifestSha256'],
                 'firebaseConfigSha256': source['firebaseConfigSha256'],
                 'expectedLiveVersion': expected_live, 'live': live,
                 'historicalProviderFiles': len(old_files), 'retainedHistoricalPaths': len(old_files) - len(overwritten),
                 'historicalPhysicalSourcePaths': len(physical_baseline),
                 'retainedHistoricalPhysicalSourcePaths': len(physical_baseline) - len(overwritten),
                 'retainedHistoricalProviderPaths': len(old_files) - len(overwritten),
                 'firebaseManagedReservedPaths': sorted(FIREBASE_RESERVED),
                 'firebaseManagedReservedPathCount': len(FIREBASE_RESERVED),
                 'overlayPaths': len(overlay), 'finalExpectedPaths': len(expected_files),
                 'expectedFinalPhysicalSourcePaths': len(source['allSourceFiles']),
                 'expectedFinalProviderPaths': len(expected_files),
                 'overlayRawSourceHashesMatchTrackedManifest': True,
                 'oldProviderFileMapSha256': canonical_hash(old_files),
                 'expectedProviderFileMapSha256': canonical_hash(expected_files),
                 'providerServingConfigSha256': canonical_hash(old.get('config', {})),
                 'historicalApksDownloaded': False, 'historicalApksUploaded': False,
                 'historicalRawFilesPresentOrRehashedRequired': False,
                 'publicOriginRequested': False, 'publicOriginBytesVerified': False,
                 'publicOriginRenderingVerified': False,
                 'identityScope': 'Overlay raw bytes equal exact Git/source-manifest hashes. Historical physical paths and exactly /__/firebase/init.js plus /__/firebase/init.json are preserved by cloning authenticated provider compressed hashes/config. These two Firebase-managed reserved paths are provider-only entries, not physical source files. Historical raw source bytes are not reverified on this sparse checkout. Public HTTP and rendering remain separate evidence.'}
    if update_context is not None:
        preflight.update({key: value for key, value in update_context.items()
                          if key not in ('priorProviderFiles', 'priorProviderConfig')})
    save(output, 'preflight.json', preflight)
    save(output, 'overlay-source-and-gzip-hashes.json', overlay)
    save(output, 'baseline-provider-files.json', old_files)
    save(output, 'baseline-provider-config.json', old.get('config', {}))
    print(json.dumps({'preflight': 'PASS', 'commit': source['commit'], 'overlayPaths': len(overlay),
                      'preservedPhysicalSourcePaths': len(physical_baseline) - len(overwritten),
                      'preservedProviderPaths': len(old_files) - len(overwritten),
                      'firebaseManagedReservedPaths': len(FIREBASE_RESERVED),
                      'finalPhysicalSourcePaths': len(source['allSourceFiles']),
                      'finalProviderPaths': len(expected_files), 'deployRequested': deploy}), flush=True)
    if not deploy:
        return preflight

    # Recheck immediately before the first mutation, independently of preflight.
    checked_live()
    name = clone(client, expected_live, output)
    cloned = version(client, name, 'CREATED')
    require(cloned.get('config', {}) == old.get('config', {}), 'Clone changed provider serving config')
    require(files(client, name) == old_files, 'Clone did not preserve every historical provider hash')
    require(not files(client, name, 'EXPECTED'), 'Cloned historical files unexpectedly need uploading')
    requested = {path: row['gzipSha256'] for path, row in overlay.items()}
    populated = client.api('POST', name + ':populateFiles', {'files': requested})
    required = populated.get('uploadRequiredHashes', [])
    require(isinstance(required, list) and len(required) == len(set(required)) and
            set(required).issubset(source['blobs']), 'Provider requested a non-overlay upload hash')
    upload_base = populated.get('uploadUrl', '')
    wanted_base = 'https://' + UPLOAD_HOST + '/upload/' + name + '/files'
    require(upload_base == wanted_base, 'Unexpected upload URL; refusing to send any bytes')
    uploaded = []
    for digest in required:
        require(HEX.fullmatch(digest) is not None, 'Invalid requested upload identity')
        payload = source['blobs'][digest]
        require(sha(payload) == digest, 'In-memory gzip payload identity changed')
        client.request('POST', upload_base + '/' + digest, payload, upload=True)
        uploaded.append(digest)
        save(output, 'uploads.json', {'version': name, 'requiredHashes': required,
                                      'uploadedHashes': uploaded, 'historicalBlobsUploaded': 0})
    if not required:
        save(output, 'uploads.json', {'version': name, 'requiredHashes': [],
                                      'uploadedHashes': [], 'historicalBlobsUploaded': 0})
    actual = files(client, name)
    require(actual == expected_files and not files(client, name, 'EXPECTED'),
            'Overlay provider file identities differ or files still need uploading')
    require(version(client, name, 'CREATED').get('config', {}) == old.get('config', {}),
            'Provider serving config changed while adding overlay')
    save(output, 'verified-provider-files.json', actual)
    finalized = client.api('PATCH', name, {'status': 'FINALIZED'}, updateMask='status')
    require(finalized.get('name') == name and finalized.get('status') == 'FINALIZED' and
            finalized.get('config', {}) == old.get('config', {}), 'Finalized provider version identity/config differs')
    require(files(client, name) == expected_files, 'Finalized provider paths/hashes differ')
    if 'fileCount' in finalized:
        require(int(finalized['fileCount']) == len(expected_files), 'Finalized provider file count differs')
    save(output, 'finalized-version.json', {'name': name, 'status': finalized['status'],
                                           'fileCount': finalized.get('fileCount'),
                                           'finalizeTime': finalized.get('finalizeTime'),
                                           'providerConfigSha256': canonical_hash(finalized.get('config', {}))})

    # An optimistic concurrency check: if another deploy happened, leave this
    # finalized version unreleased. The API does not offer a release compare-and-swap.
    before_release = checked_live()
    save(output, 'live-immediately-before-release.json', before_release)
    message = 'Cognitive Lab 1 from ' + source['commit']
    release = client.api('POST', 'sites/' + SITE + '/releases', {'message': message}, versionName=name)
    require(release.get('version', {}).get('name') == name and release.get('message') == message and
            release.get('type') == 'DEPLOY', 'Release response identity differs; inspect management state')
    after = live_channel(client, name)
    require(after['releaseMessage'] == message and same_release(after['releaseName'], release.get('name')),
            'Live channel does not reference the exact new release')
    result = dict(preflight, status='DEPLOY/FINALIZED',
                  recordedAtUtc=datetime.datetime.now(datetime.timezone.utc).isoformat(),
                  clonedFrom=expected_live, version=name, live=after,
                  uploadedOverlayBlobs=len(uploaded), historicalProviderHashesPreserved=True,
                  providerServingConfigUnchanged=True, verifiedProviderFiles=len(expected_files),
                  releaseConcurrencyScope='Expected old live version checked immediately before release; API has no atomic compare-and-swap',
                  publicOriginRequested=False, publicOriginBytesVerified=False,
                  publicOriginRenderingVerified=False)
    save(output, 'deployment.json', result)
    print('COGNITIVE_LAB_OVERLAY_RECEIPT ' + json.dumps(result, sort_keys=True), flush=True)
    return result


def validate_update(source, receipts):
    """Bind a narrowly allowed follow-up to an exact earlier Git/provider release."""
    receipt_hashes = {}
    def read(name):
        path = receipts / name
        require(path.is_file() and not path.is_symlink(), 'Missing bounded follow-up receipt: ' + name)
        raw = path.read_bytes()
        receipt_hashes[name] = sha(raw)
        return json.loads(raw)

    deployed = read('deployment.json')
    prior = read('preflight.json')
    prior_files = read('verified-provider-files.json')
    prior_config = read('baseline-provider-config.json')
    finalized = read('finalized-version.json')
    prior_overlay = read('overlay-source-and-gzip-hashes.json')
    original_files = read('baseline-provider-files.json')
    old_commit = deployed.get('deploymentSourceCommit', deployed.get('sourceCommit', ''))
    old_name = deployed.get('version', '')
    require(deployed.get('status') == 'DEPLOY/FINALIZED' and
            re.fullmatch(r'[a-f0-9]{40}', old_commit) is not None and
            isinstance(old_name, str) and VERSION.fullmatch(old_name) is not None,
            'Follow-up needs an exact previously finalized deployment receipt')
    require(prior.get('status') == 'PASS' and prior.get('sourceCommit') == old_commit and
            finalized.get('name') == old_name and finalized.get('status') == 'FINALIZED',
            'Prior deployment source/version receipts disagree')
    old_manifest_bytes = git(source['root'], 'show', old_commit + ':' + MANIFEST, binary=True)
    old_manifest = json.loads(old_manifest_bytes)
    require(sha(old_manifest_bytes) == prior.get('manifestSha256') == deployed.get('manifestSha256') and
            git(source['root'], 'rev-parse', old_commit + ':public') == prior.get('sourcePublicTree') == deployed.get('sourcePublicTree'),
            'Prior receipt is not proven by the exact older Git manifest/public tree')
    require(old_manifest.get('status') == 'PASS' and old_manifest.get('release') == 'cognitive-lab-1' and
            old_manifest.get('baselineManifestSha256') == source['baselineManifestSha256'] and
            old_manifest.get('firebaseConfigSha256') == source['firebaseConfigSha256'] and
            sha(git(source['root'], 'show', old_commit + ':' + CONFIG, binary=True)) == source['firebaseConfigSha256'],
            'Follow-up changed historical metadata or Hosting config')
    old_physical = old_manifest['files']
    require(set(old_physical) == set(source['allSourceFiles']), 'Follow-up may not add or remove any physical path')
    changed = sorted(path for path in old_physical if old_physical[path] != source['allSourceFiles'][path])
    require(changed and set(changed).issubset(UPDATE_ALLOWED),
            'Follow-up changes paths outside the exact dashboard index, script and offline archive allowlist')
    require(set(prior_overlay) == set(source['overlay']), 'Prior overlay footprint differs from current audited overlay')
    require(set(prior_files) == set(old_physical).union(FIREBASE_RESERVED) and
            all(isinstance(value, str) and HEX.fullmatch(value) for value in prior_files.values()) and
            canonical_hash(prior_files) == prior.get('expectedProviderFileMapSha256') == deployed.get('expectedProviderFileMapSha256'),
            'Prior full provider file map identity differs')
    require(canonical_hash(original_files) == prior.get('oldProviderFileMapSha256') and
            set(original_files) == set(source['baselineFiles']).union(FIREBASE_RESERVED),
            'Prior original historical provider map differs')
    for path, row in prior_overlay.items():
        raw = git(source['root'], 'show', old_commit + ':public' + path, binary=True)
        encoded = compressed(raw)
        expected = {'rawSha256': sha(raw), 'rawBytes': len(raw),
                    'gzipSha256': sha(encoded), 'gzipBytes': len(encoded)}
        require(row == expected and old_physical[path] == {'sha256': sha(raw), 'bytes': len(raw)} and
                prior_files[path] == sha(encoded), 'Prior overlay is not proven by old Git bytes/provider identity: ' + path)
    for path, value in original_files.items():
        if path not in prior_overlay:
            require(prior_files[path] == value, 'Prior historical provider hash changed outside the original overlay')
    config_hash = canonical_hash(prior_config)
    require(config_hash == prior.get('providerServingConfigSha256') == deployed.get('providerServingConfigSha256') ==
            finalized.get('providerConfigSha256'), 'Prior serving config receipts disagree')
    prior_live = deployed.get('live', {})
    old_message = 'Cognitive Lab 1 from ' + old_commit
    require(prior_live.get('versionName') == old_name and prior_live.get('releaseMessage') == old_message and
            isinstance(prior_live.get('releaseName'), str) and RELEASE_NAME.fullmatch(prior_live['releaseName']) is not None,
            'Prior live release receipt identity differs')
    narrowed = dict(source)
    narrowed['baselineFiles'] = old_physical
    narrowed['overlay'] = {path: source['overlay'][path] for path in changed}
    narrowed['blobs'] = {row['gzipSha256']: source['blobs'][row['gzipSha256']]
                         for row in narrowed['overlay'].values()}
    context = {'deploymentKind': 'audited-cognitive-followup',
               'priorDeploymentSourceCommit': old_commit, 'priorVersion': old_name,
               'priorManifestSha256': sha(old_manifest_bytes),
               'priorSourcePublicTree': prior['sourcePublicTree'],
               'priorReleaseMessage': old_message, 'priorReleaseName': prior_live['releaseName'],
               'priorProviderFileMapSha256': canonical_hash(prior_files),
               'priorReceiptSha256': receipt_hashes,
               'changedPaths': changed, 'allowedFollowupPaths': sorted(UPDATE_ALLOWED),
               'priorProviderFiles': prior_files, 'priorProviderConfig': prior_config,
               'followupScope': 'Only the listed dashboard index/script/offline archive raw bytes differ. Current full provider map/config and exact prior release are verified before cloning; every other provider hash is preserved.'}
    return narrowed, old_name, context


def recover(source, client, output):
    """Read-only verification of a release already made by an earlier run.

    A later validator commit is allowed only when its exact public tree and
    manifest are unchanged. No mutation endpoint is called on this path.
    """
    def receipt(name):
        path = output / name
        require(path.is_file() and not path.is_symlink(), 'Missing bounded recovery receipt: ' + name)
        return json.loads(path.read_text())

    previous = receipt('preflight.json')
    operation = receipt('clone-operation.json')
    verified = receipt('verified-provider-files.json')
    finalized = receipt('finalized-version.json')
    uploads = receipt('uploads.json')
    baseline_files = receipt('baseline-provider-files.json')
    baseline_config = receipt('baseline-provider-config.json')
    overlay = receipt('overlay-source-and-gzip-hashes.json')
    deployment_commit = previous.get('sourceCommit', '')
    require(re.fullmatch(r'[a-f0-9]{40}', deployment_commit) is not None and
            previous.get('status') == 'PASS' and previous.get('mode') == 'deploy',
            'Recovery requires an earlier exact deployment preflight')
    require(previous.get('sourcePublicTree') == source['publicTree'] and
            previous.get('manifestSha256') == source['manifestSha256'] and
            previous.get('baselineManifestSha256') == source['baselineManifestSha256'] and
            previous.get('firebaseConfigSha256') == source['firebaseConfigSha256'],
            'Validator source public tree or immutable deployment metadata differs')
    require(git(source['root'], 'rev-parse', deployment_commit + ':public') == source['publicTree'] and
            sha(git(source['root'], 'show', deployment_commit + ':' + MANIFEST, binary=True)) == source['manifestSha256'] and
            sha(git(source['root'], 'show', deployment_commit + ':' + CONFIG, binary=True)) == source['firebaseConfigSha256'],
            'Original deployment commit does not prove the same public tree and source metadata')
    followup = previous.get('deploymentKind') == 'audited-cognitive-followup'
    changed = previous.get('changedPaths', []) if followup else list(source['overlay'])
    require(isinstance(changed, list) and changed and
            (not followup or set(changed).issubset(UPDATE_ALLOWED)) and
            overlay == {path: source['overlay'][path] for path in changed},
            'Recovery overlay bytes or gzip identities differ')
    require(operation.get('done') is True and operation.get('versionStatus') == 'CREATED',
            'Recovery clone did not finish as a mutable version')
    name = operation.get('versionName', '')
    require(isinstance(name, str) and VERSION.fullmatch(name) is not None and
            finalized.get('name') == name and finalized.get('status') == 'FINALIZED' and
            uploads.get('version') == name, 'Recovery version receipts disagree')
    if followup:
        old_commit = previous.get('priorDeploymentSourceCommit', '')
        require(re.fullmatch(r'[a-f0-9]{40}', old_commit) is not None, 'Missing follow-up prior source identity')
        old_manifest = git(source['root'], 'show', old_commit + ':' + MANIFEST, binary=True)
        require(sha(old_manifest) == previous.get('priorManifestSha256'), 'Follow-up prior source manifest differs')
        physical_baseline = set(json.loads(old_manifest)['files'])
        require(physical_baseline == set(source['allSourceFiles']), 'Follow-up recovery path footprint differs')
    else:
        physical_baseline = set(source['baselineFiles'])
    require(set(baseline_files) == physical_baseline.union(FIREBASE_RESERVED) and
            all(isinstance(value, str) and HEX.fullmatch(value) for value in baseline_files.values()) and
            canonical_hash(baseline_files) == previous.get('oldProviderFileMapSha256'),
            'Recovery historical provider file identities differ')
    expected_files = dict(baseline_files)
    expected_files.update({path: row['gzipSha256'] for path, row in source['overlay'].items()})
    require(verified == expected_files and canonical_hash(expected_files) == previous.get('expectedProviderFileMapSha256') and
            set(expected_files) == set(source['allSourceFiles']).union(FIREBASE_RESERVED),
            'Recovery final provider identities differ from the exact source overlay')
    config_hash = canonical_hash(baseline_config)
    require(config_hash == previous.get('providerServingConfigSha256') and
            config_hash == finalized.get('providerConfigSha256'), 'Recovery provider config receipts disagree')
    required, uploaded = uploads.get('requiredHashes'), uploads.get('uploadedHashes')
    require(isinstance(required, list) and isinstance(uploaded, list) and
            len(required) == len(set(required)) and set(required) == set(uploaded) and
            set(required).issubset(source['blobs']) and uploads.get('historicalBlobsUploaded') == 0,
            'Recovery uploads are incomplete or include historical blobs')
    message = 'Cognitive Lab 1 from ' + deployment_commit
    live = live_channel(client, name)
    require(live.get('releaseMessage') == message and
            isinstance(live.get('releaseName'), str) and RELEASE_NAME.fullmatch(live['releaseName']) is not None,
            'Recovery live release message or resource alias differs')
    actual_version = version(client, name, 'FINALIZED')
    require(actual_version.get('config', {}) == baseline_config and
            files(client, name) == expected_files and not files(client, name, 'EXPECTED'),
            'Recovery live provider files or serving config differ')
    if 'fileCount' in actual_version:
        require(int(actual_version['fileCount']) == len(expected_files), 'Recovery provider file count differs')
    failure_path = output / 'failure.json'
    failure_hash = sha(failure_path.read_bytes()) if failure_path.is_file() else None
    record = dict(previous, status='DEPLOY/FINALIZED', sourceCommit=deployment_commit,
                  deploymentSourceCommit=deployment_commit, validatorSourceCommit=source['commit'],
                  recoveredReadOnly=True, recoveryProviderWrites=0,
                  recordedAtUtc=datetime.datetime.now(datetime.timezone.utc).isoformat(),
                  version=name, live=live, uploadedOverlayBlobs=len(uploaded),
                  historicalProviderHashesPreserved=True, providerServingConfigUnchanged=True,
                  verifiedProviderFiles=len(expected_files),
                  originalFailurePreserved=failure_hash is not None, originalFailureSha256=failure_hash,
                  releaseIdentityScope='Current live exact version, deployed-source message and allowed default/live release resource verified; original POST response was not stored by the earlier failed postcheck',
                  publicOriginRequested=False, publicOriginBytesVerified=False,
                  publicOriginRenderingVerified=False)
    save(output, 'recovery.json', record)
    save(output, 'deployment.json', record)
    require(not failure_path.is_file() or sha(failure_path.read_bytes()) == failure_hash,
            'Original failure receipt changed during recovery')
    print('COGNITIVE_LAB_OVERLAY_RECOVERY_RECEIPT ' + json.dumps(record, sort_keys=True), flush=True)
    return record


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--commit', required=True, help='Exact clean Git HEAD (40 hex characters)')
    parser.add_argument('--output', '--receipts', required=True, type=Path, help='New external receipt directory')
    parser.add_argument('--expected-live-version', default=EXPECTED_LIVE)
    parser.add_argument('--deploy', action='store_true', help='Explicitly clone, upload overlay, finalize and release')
    parser.add_argument('--recover', action='store_true',
                        help='Read-only verification of an already released version using existing --output receipts')
    parser.add_argument('--update-from-receipts', type=Path,
                        help='Audit a narrow dashboard index/script/archive follow-up against prior finalized receipts; --deploy releases it')
    args = parser.parse_args()
    require(not (args.deploy and args.recover), '--recover cannot be combined with --deploy')
    require(not (args.recover and args.update_from_receipts), '--recover cannot be combined with --update-from-receipts')
    require(VERSION.fullmatch(args.expected_live_version) is not None, 'Expected live version must belong to the fixed site')
    output = args.output.resolve()
    require(output != ROOT and ROOT not in output.parents, 'Receipts must be outside the source checkout')
    require(output.is_dir() if args.recover else not output.exists(),
            'Recovery requires an existing receipt directory; preflight/deploy requires a new directory')
    source = validate_source(ROOT, args.commit)
    update_context = None
    expected_live = args.expected_live_version
    if args.update_from_receipts:
        source, expected_live, update_context = validate_update(source, args.update_from_receipts.resolve())
        require(args.expected_live_version in (EXPECTED_LIVE, expected_live),
                'Explicit expected-live version differs from the prior finalized update receipt')
    client = Client(access_token())
    if not args.recover:
        output.mkdir(parents=True, exist_ok=False)
    try:
        if args.recover:
            recover(source, client, output)
        else:
            execute(source, client, expected_live, output, args.deploy, update_context)
    except Exception as error:
        # Only controlled error text is safe; an arbitrary library exception can
        # include a request object. Keep unknown failures deliberately generic.
        detail = str(error) if isinstance(error, DeploymentError) else 'Unexpected local/provider response; no public verification claimed'
        failure_name = ('recovery-failure-' + datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%S%fZ') + '.json') if args.recover else 'failure.json'
        save(output, failure_name, {'status': 'FAILED', 'reason': detail,
                                      'deployRequested': args.deploy, 'sourceCommit': args.commit,
                                      'publicOriginRequested': False})
        raise DeploymentError(detail) from None


if __name__ == '__main__':
    try:
        main()
    except DeploymentError as error:
        print('OVERLAY_DEPLOY_FAILED: ' + str(error), file=sys.stderr)
        sys.exit(1)
