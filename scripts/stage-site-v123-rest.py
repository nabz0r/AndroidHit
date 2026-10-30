#!/usr/bin/env python3
"""Stage an exact clean 1.23 commit for REST Hosting; never authenticate or deploy.

Missing audited Git inputs are restored from that same commit. Historical raw
Hosting files may stay absent. Only --download-bases permits two fixed public
1.22 APK URLs; their manifest hashes are checked before exact delta application.
"""
import argparse
import hashlib
import json
from pathlib import Path, PurePosixPath
import re
import subprocess
import sys
import tempfile
import types
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
SOURCE = 'docs/validation/site-v1.23/release/source-manifest.json'
BASELINE = 'docs/validation/site-v1.22/release/source-manifest.json'
PREVIOUS122 = 'b122c0acbd172a7912c69b0b6914b8b0f9b2a243'
OLD_MANIFEST = 'releases/manifest-1.22.0-rc1.json'
HOST = 'luma-drift-production.web.app'
SIGNER = 'dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287'
SHA = re.compile(r'[a-f0-9]{64}')
RETIRED_PATHS = frozenset(['/releases/luma-drift-1.15.0-rc1.aab', '/releases/luma-drift-1.15.0-rc1.apk', '/releases/luma-drift-1.16.0-rc1.aab', '/releases/luma-drift-1.16.0-rc1.apk', '/releases/luma-drift-1.17.0-rc6.aab', '/releases/luma-drift-1.17.0-rc6.apk', '/releases/luma-drift-1.18.0-prototype1.apk', '/releases/luma-drift-1.18.0-sandbox-prototype1.apk', '/releases/luma-drift-1.19.0-prototype2.apk', '/releases/luma-drift-1.19.0-sandbox-prototype2.apk', '/releases/luma-drift-1.20.0-rc1.apk', '/releases/luma-drift-1.20.0-sandbox-rc1.apk', '/releases/luma-drift-1.21.0-rc1.apk', '/releases/luma-drift-1.21.0-sandbox-rc1.apk'])
CHUNK = 1024 * 1024


def require(condition, message):
    if not condition:
        raise ValueError(message)


def git(*args):
    return subprocess.check_output(['git', *args], cwd=ROOT)


def digest(path):
    result = hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda: stream.read(CHUNK), b''):
            result.update(block)
    return result.hexdigest()


def safe_path(name):
    require(isinstance(name, str) and name and '\\' not in name and ':' not in name,
            'Invalid repository-relative input path')
    relative = PurePosixPath(name)
    require(not relative.is_absolute() and all(part not in ('', '.', '..') for part in name.split('/')),
            'Input path must be bounded and normalized: ' + name)
    target = ROOT.joinpath(*relative.parts)
    require(ROOT.resolve() in target.resolve().parents, 'Input escapes checkout: ' + name)
    require(not any(ROOT.joinpath(*relative.parts[:count]).is_symlink()
                    for count in range(1, len(relative.parts) + 1)),
            'Symlink input refused: ' + name)
    return target


def committed_file(commit, name, restored, expected=None, size=None):
    target = safe_path(name)
    row = git('ls-tree', '-z', commit, '--', name).split(b'\0')[0]
    require(row and row.split(b' ', 1)[0] in (b'100644', b'100755'),
            'Input must be an ordinary file in the exact commit: ' + name)
    data = git('show', commit + ':' + name)
    actual = hashlib.sha256(data).hexdigest()
    require(expected is None or SHA.fullmatch(expected) and actual == expected,
            'Committed input SHA-256 differs: ' + name)
    require(size is None or len(data) == size, 'Committed input size differs: ' + name)
    if target.exists():
        require(target.is_file() and digest(target) == actual, 'Existing input differs from commit: ' + name)
    else:
        target.parent.mkdir(parents=True, exist_ok=True)
        # Exclusive creation also refuses a file raced into the sparse checkout.
        with target.open('xb') as stream:
            stream.write(data)
        if row.startswith(b'100755 '):
            target.chmod(0o755)
        restored.append(name)
    return target


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        raise ValueError('Base APK redirect refused')


def download_base(entry, folder):
    name = Path(entry['path']).name
    require(name in ('luma-drift-1.22.0-rc1.apk', 'luma-drift-1.22.0-sandbox-rc1.apk'),
            'Unexpected base APK name')
    url = 'https://' + HOST + '/releases/' + name
    target = folder / name
    opener = urllib.request.build_opener(NoRedirect())
    request = urllib.request.Request(url, headers={'Accept-Encoding': 'identity'}, method='GET')
    total, result = 0, hashlib.sha256()
    with opener.open(request, timeout=60) as response, target.open('xb') as stream:
        require(response.status == 200 and response.geturl() == url, 'Unexpected base APK response')
        length = response.headers.get('Content-Length')
        require(length is None or int(length) == entry['bytes'], 'Base response length differs')
        for block in iter(lambda: response.read(CHUNK), b''):
            total += len(block)
            require(total <= entry['bytes'], 'Base download exceeds pinned size')
            result.update(block)
            stream.write(block)
    require(total == entry['bytes'] and result.hexdigest() == entry['sha256'], 'Base APK SHA-256/size differs')
    return target


def stage(commit, download_bases=False):
    require(re.fullmatch(r'[a-f0-9]{40}', commit), 'Exact 40-character commit required')
    require(git('rev-parse', 'HEAD').decode().strip() == commit, 'HEAD is not the requested commit')
    require(not git('status', '--porcelain', '--untracked-files=no').strip(), 'Staging checkout must be clean')
    restored = []
    # Sparse checkouts can omit this tracked file; restore its exact committed
    # rules before asking Git to classify the ignored physical APK inputs.
    committed_file(commit, '.gitignore', restored)
    require(not git('status', '--porcelain', '--untracked-files=all').strip(), 'Staging checkout must be clean')
    source_path = committed_file(commit, SOURCE, restored)
    source = json.loads(source_path.read_text())
    require(source['status'] == 'PASS' and source['edition'] == '1.23'
            and source['release'] == '1.23.0-rc2'
            and source['native_capture_provenance_verified'] and source['package_identity_verified'],
            'Accepted exact native 1.23 source manifest required')
    require(source.get('frozen_inputs'), 'Frozen source inputs required')
    for name, expected in source['frozen_inputs'].items():
        committed_file(commit, name, restored, expected)
    artifact_path = committed_file(commit, source['artifact_manifest'], restored, source['artifact_manifest_sha256'])
    artifact = json.loads(artifact_path.read_text())
    require(artifact['edition'] == '1.23' and artifact['version'] == '1.23.0-rc2'
            and artifact['version_code'] == 31, 'Unexpected canonical release')
    old_bytes = git('show', PREVIOUS122 + ':' + OLD_MANIFEST)
    old = json.loads(committed_file(commit, OLD_MANIFEST, restored, hashlib.sha256(old_bytes).hexdigest()).read_text())
    require(old['edition'] == '1.22' and old['version_code'] == 30, 'Unexpected base release')
    baseline_bytes = git('show', PREVIOUS122 + ':' + BASELINE)
    previous = json.loads(committed_file(commit, BASELINE, restored, hashlib.sha256(baseline_bytes).hexdigest()).read_text())
    require(previous['status'] == 'PASS' and previous['edition'] == '1.22' and previous['release'] == '1.22.0-rc1', 'Exact accepted C3 source required')
    baseline = previous['files']
    require(len(baseline) == 570 and set(baseline)-set(source['files']) == RETIRED_PATHS, 'Only the 14 explicitly retired binaries may be deleted')
    require(source.get('retired_files') == {name:baseline[name] for name in RETIRED_PATHS}, 'Retired metadata must match immutable C3 rows')
    require(previous['firebase_config_sha256'] == source['firebase_config_sha256'], 'Hosting config must remain unchanged')
    changed = sorted(name for name in baseline if name in source['files'] and baseline[name] != source['files'][name])
    added = set(source['files']) - set(baseline)
    required = {'/assets/site-v123.css', '/assets/site-v123.js', '/lab/clarity-123/index.html',
                '/releases/manifest-1.23.0-rc2.json', '/releases/luma-drift-1.23.0-rc2.apk',
                '/releases/luma-drift-1.23.0-sandbox-rc2.apk'}
    required.update('/assets/screenshots/v1.23/' + key + '-' + language + '.png'
                    for key in ('puzzle', 'skills', 'wardrobe', 'map') for language in ('fr', 'en'))
    require(changed == ['/index.html'] and required <= added, 'Only homepage overwrite plus complete 1.23 additions are permitted')
    require(all(name in required or name.startswith('/lab/clarity-123/') for name in added), 'Unexpected new public path')
    for channel in ('production', 'lab'):
        base = old['apks'][channel]
        require(baseline['/' + base['path']] == {'sha256': base['sha256'], 'bytes': base['bytes']}, 'Base APK differs from immutable C3 public identity')
    committed_file(commit, 'firebase.json', restored, source['firebase_config_sha256'])
    helper_path = committed_file(commit, 'scripts/apk-byte-delta.py', restored)
    helper = types.ModuleType('exact_apk_delta123')
    helper.__file__ = str(helper_path)
    exec(compile(helper_path.read_bytes(), str(helper_path), 'exec'), helper.__dict__)
    external = {'/' + entry['path'] for entry in artifact['apks'].values()}
    for name, entry in source['files'].items():
        require(name.startswith('/') and not name.startswith('//'), 'Invalid public payload path')
        target = safe_path('public' + name)
        if name in external:
            continue
        if target.exists():
            require(target.is_file() and digest(target) == entry['sha256'] and target.stat().st_size == entry['bytes'],
                    'Present public payload differs: ' + name)
            continue
        if name in baseline and entry == baseline[name]:
            continue
        committed_file(commit, 'public' + name, restored, entry['sha256'], entry['bytes'])
    completed, downloaded = {}, []
    with tempfile.TemporaryDirectory(prefix='luma123-apk-bases-') as folder_name:
        for channel in ('production', 'lab'):
            current, base = artifact['apks'][channel], old['apks'][channel]
            expected_version = '1.23.0-sandbox-rc2' if channel == 'lab' else '1.23.0-rc2'
            require(current['version'] == expected_version and current['code'] == 31
                    and current['path'] == 'releases/luma-drift-' + expected_version + '.apk'
                    and current['signer_certificate_sha256'] == SIGNER,
                    'Canonical APK identity differs: ' + channel)
            require(current['sha256'] == source[channel + '_apk_sha256'], 'Source APK identity differs: ' + channel)
            target = safe_path('public/' + current['path'])
            public_entry = source['files']['/' + current['path']]
            require(public_entry['sha256'] == current['sha256'] and public_entry['bytes'] == current['bytes'],
                    'Source public APK metadata differs')
            relative = str(target.relative_to(ROOT))
            require(subprocess.run(['git', 'check-ignore', '--quiet', relative], cwd=ROOT).returncode == 0,
                    'Canonical physical APK must be ignored')
            require(not git('ls-files', '--', relative).strip(), 'Canonical APK must not be a Git blob')
            prefix = 'releases/deltas/1.23.0-rc2/' + channel
            build = json.loads(committed_file(commit, prefix + '-build.json', restored).read_text())
            require(build['passed'] and build['operation'] == 'build' and build['schema'] == helper.SCHEMA,
                    'Accepted delta build receipt required')
            for meta, expected in ((build['base'], base), (build['target'], current)):
                require(meta['sha256'] == expected['sha256'] and meta['bytes'] == expected['bytes'],
                        'Delta receipt does not match pinned manifests: ' + channel)
            require(build['patch']['name'] == channel + '.json.gz', 'Wrong patch name')
            patch = committed_file(commit, prefix + '.json.gz', restored, build['patch']['sha256'], build['patch']['bytes'])
            if target.exists():
                require(target.is_file() and digest(target) == current['sha256'] and target.stat().st_size == current['bytes'],
                        'Never replace existing canonical APK bytes')
                completed[channel] = {'path': relative, 'sha256': current['sha256'], 'bytes': current['bytes'], 'reconstructed': False}
                continue
            candidates = [safe_path(base['path']), safe_path('public/' + base['path'])]
            base_path = next((path for path in candidates if path.exists()), None)
            if base_path is None:
                require(download_bases, 'Missing local 1.22 base APK; explicit --download-bases required: ' + channel)
                base_path = download_base(base, Path(folder_name))
                downloaded.append(channel)
            require(base_path.is_file() and base_path.stat().st_size == base['bytes'] and digest(base_path) == base['sha256'],
                    'Local base APK SHA-256/size differs: ' + channel)
            target.parent.mkdir(parents=True, exist_ok=True)
            result = helper.apply(base_path, patch, target, current['sha256'], patch_sha=build['patch']['sha256'])
            require(result['verified_output_bytes'] == current['bytes'], 'Reconstructed APK size differs')
            completed[channel] = {'path': relative, 'sha256': current['sha256'], 'bytes': current['bytes'], 'reconstructed': True}
    require(not git('status', '--porcelain', '--untracked-files=all').strip(), 'Checkout changed during staging')
    missing = sorted(name for name in source['files'] if not safe_path('public' + name).exists())
    preserved = {name for name, entry in baseline.items() if name in source['files'] and source['files'][name] == entry}
    require(set(missing) <= preserved, 'Missing new or changed audited public payload')
    return {'passed': True, 'sourceCommit': commit, 'sourceManifestSha256': digest(source_path),
            'restoredCommittedFiles': restored, 'historicalRawPayloadsAbsent': missing,
            'previousSourceCommit': PREVIOUS122, 'baselinePhysicalPaths': 570, 'retiredPaths': sorted(RETIRED_PATHS),
            'downloadBasesRequested': download_bases, 'downloadedBaseChannels': downloaded,
            'baseDownloadHost': HOST if downloaded else None, 'apks': completed,
            'authenticated': False, 'uploaded': False, 'deployed': False,
            'scope': 'Exact byte staging only; signer/native acceptance derives from the frozen release evidence. Historical provider preservation is checked by the separate REST deployer.'}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--commit', required=True)
    parser.add_argument('--report', required=True, type=Path)
    parser.add_argument('--download-bases', action='store_true')
    args = parser.parse_args()
    report = args.report.resolve()
    require(report != ROOT.resolve() and ROOT.resolve() not in report.parents and not report.exists(),
            'Use a new report path outside checkout')
    report.parent.mkdir(parents=True, exist_ok=True)
    try:
        result = stage(args.commit, args.download_bases)
    except (ValueError, KeyError, OSError, subprocess.SubprocessError) as error:
        result = {'passed': False, 'sourceCommit': args.commit, 'error': str(error),
                  'authenticated': False, 'uploaded': False, 'deployed': False}
        report.write_text(json.dumps(result, indent=2) + '\n')
        print(json.dumps(result), file=sys.stderr)
        return 1
    report.write_text(json.dumps(result, indent=2) + '\n')
    print(json.dumps(result))
    return 0


if __name__ == '__main__':
    sys.exit(main())
