#!/usr/bin/env python3
"""Prepare or verify an additive Cognitive Lab release on the existing Firebase site.

Historical 1.21 receipts remain immutable. This manifest describes the later
research dashboard; it neither creates an Android release nor changes an APK.
"""
import argparse
import datetime
import hashlib
import json
from pathlib import Path
import re
import shutil
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[1]
PUBLIC = ROOT / 'public'
BASELINE = ROOT / 'docs/validation/site-v1.21/release/source-manifest.json'
MANIFEST = ROOT / 'docs/validation/cognitive-lab/site-manifest.json'
DATA = ROOT / 'docs/validation/cognitive-lab/latest'


def sha(path):
    h = hashlib.sha256()
    with path.open('rb') as handle:
        for block in iter(lambda: handle.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def payloads():
    return {('/' + p.relative_to(PUBLIC).as_posix()): p for p in PUBLIC.rglob('*')
            if p.is_file() and not any(part.startswith('.') or part == 'tests'
                                      for part in p.relative_to(PUBLIC).parts)}


def zip_files(destination, files):
    with zipfile.ZipFile(destination, 'w', zipfile.ZIP_DEFLATED, compresslevel=9) as bundle:
        for name, path in sorted(files.items()):
            info = zipfile.ZipInfo(name, (2026, 10, 30, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            info.external_attr = (0o755 if path.suffix == '.sh' else 0o644) << 16
            bundle.writestr(info, path.read_bytes())


def run(*args):
    return subprocess.check_output(args, cwd=ROOT, text=True).strip()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--prepare', action='store_true')
    parser.add_argument('--verify', action='store_true')
    parser.add_argument('--deploy', action='store_true')
    parser.add_argument('--commit')
    parser.add_argument('--receipts', type=Path)
    args = parser.parse_args()
    if not any((args.prepare, args.verify, args.deploy)):
        parser.error('Choose --prepare, --verify or --deploy')
    baseline = json.loads(BASELINE.read_text())
    if args.prepare:
        destination = PUBLIC / 'lab/cognitive/data'
        destination.mkdir(parents=True, exist_ok=True)
        required = ('summary.json', 'profiles.json', 'paired-comparisons.json',
                    'traces.json', 'provenance.json')
        for name in required:
            assert (DATA / name).is_file(), 'Missing completed simulation output: ' + name
        for path in sorted(DATA.iterdir()):
            if path.is_file() and path.suffix in ('.json', '.csv', '.gz', '.md', '.tsv'):
                shutil.copyfile(path, destination / path.name)
        bundle = {'summary': json.loads((DATA / 'summary.json').read_text()),
                  'profiles': json.loads((DATA / 'profiles.json').read_text()),
                  'paired': json.loads((DATA / 'paired-comparisons.json').read_text()),
                  'traces': json.loads((DATA / 'traces.json').read_text())}
        snapshot = PUBLIC / 'lab/cognitive/snapshot.js'
        snapshot.write_text('window.COGNITIVE_LAB_SNAPSHOT = ' +
                            json.dumps(bundle, ensure_ascii=False, separators=(',', ':')) + ';\n')
        page = PUBLIC / 'lab/cognitive/index.html'
        page_text = page.read_text()
        for asset in ('snapshot.js', 'lab-core.js', 'lab.js', 'lab.css'):
            asset_file = page.parent / asset
            assert asset_file.is_file(), 'Incomplete dashboard: ' + asset
            page_text = re.sub(r'(["\'])' + re.escape(asset) + r'(?:\?v=[a-f0-9]+)?(["\'])',
                               lambda match: match[1] + asset + '?v=' + sha(asset_file)[:16] + match[2],
                               page_text)
        page.write_text(page_text)
        # The offline source archive contains only code, study configuration and
        # results. No credentials, Android assets, save files or APKs are included.
        sources = {}
        for folder in ('scripts/cognitive_lab', 'scripts/test-support/android',
                       'app/src/main/java/com/androidhit/lumadrift/engine',
                       'app/src/main/java/com/androidhit/lumadrift/data',
                       'app/src/main/java/com/androidhit/lumadrift/onboarding'):
            for path in (ROOT / folder).rglob('*'):
                if path.is_file() and path.suffix in ('.java', '.py', '.json', '.md', '.sh'):
                    sources[path.relative_to(ROOT).as_posix()] = path
        for name in ('scripts/run-cognitive-lab.sh', 'scripts/test-cognitive-lab.sh',
                     'docs/COGNITIVE_LAB.md', 'scripts/package-cognitive-lab.py',
                     'app/src/main/java/com/androidhit/lumadrift/LumaView.java'):
            path = ROOT / name
            if path.is_file():
                sources[name] = path
        for path in (PUBLIC / 'lab/cognitive').rglob('*'):
            if path.is_file() and path.suffix in ('.html', '.css', '.js', '.mjs', '.json', '.gz'):
                sources[path.relative_to(ROOT).as_posix()] = path
        zip_files(PUBLIC / 'lab/cognitive/cognitive-lab-local.zip', sources)
        actual = payloads()
        previous = baseline['files']
        assert set(previous).issubset(actual), 'An earlier public download or asset was removed'
        changed = []
        for name, entry in previous.items():
            if sha(actual[name]) != entry['sha256']:
                changed.append(name)
        assert changed == ['/index.html'], 'Unexpected earlier site changes: ' + str(changed)
        additions = set(actual) - set(previous)
        assert additions and all(name.startswith('/lab/cognitive/') for name in additions)
        record = {
            'schemaVersion': 1, 'status': 'PASS', 'release': 'cognitive-lab-1',
            'androidReleaseUnchanged': '1.21.0-rc1',
            'baselineManifest': str(BASELINE.relative_to(ROOT)),
            'baselineManifestSha256': sha(BASELINE),
            'preservedPayloads': len(previous) - 1,
            'intentionalChanges': changed, 'newPayloads': sorted(additions),
            'firebaseConfigSha256': sha(ROOT / 'firebase.json'),
            'files': {name: {'sha256': sha(path), 'bytes': path.stat().st_size}
                      for name, path in sorted(actual.items())},
            'simulationProvenanceSha256': sha(DATA / 'provenance.json'),
            'scope': 'Synthetic research dashboard and exact reproducible results. '
                     'No new APK, human retention, calibrated IQ or clinical diagnosis.',
        }
        MANIFEST.parent.mkdir(parents=True, exist_ok=True)
        MANIFEST.write_text(json.dumps(record, indent=2) + '\n')
        print(json.dumps({'prepared': True, 'payloads': len(actual),
                          'preserved': record['preservedPayloads'],
                          'added': len(additions), 'changed': changed}))
    if args.verify or args.deploy:
        record = json.loads(MANIFEST.read_text())
        assert record['status'] == 'PASS'
        assert record['baselineManifestSha256'] == sha(BASELINE)
        assert record['firebaseConfigSha256'] == sha(ROOT / 'firebase.json')
        actual = payloads()
        assert set(actual) == set(record['files']), 'Hosting file set differs from manifest'
        for name, path in actual.items():
            entry = record['files'][name]
            assert path.stat().st_size == entry['bytes'] and sha(path) == entry['sha256'], name
        print(json.dumps({'verified': True, 'payloads': len(actual), 'manifestSha256': sha(MANIFEST)}))
    if args.deploy:
        assert args.commit and run('git', 'rev-parse', 'HEAD') == args.commit
        assert not run('git', 'status', '--porcelain', '--untracked-files=all'), 'Checkout must be clean'
        assert args.receipts, 'An external receipt directory is required'
        output = args.receipts.resolve()
        assert output != ROOT and ROOT not in output.parents and not output.exists()
        output.mkdir(parents=True)
        message = 'Cognitive Lab 1 from ' + args.commit
        preflight = {'sourceCommit': args.commit, 'publicTree': run('git', 'rev-parse', 'HEAD:public'),
                     'manifestSha256': sha(MANIFEST), 'payloads': len(actual),
                     'allPhysicalPayloadHashesVerified': True, 'sourceCheckoutClean': True}
        (output / 'preflight.json').write_text(json.dumps(preflight, indent=2) + '\n')
        result = subprocess.run(['firebase', 'deploy', '--only', 'hosting', '--project',
                                 'luma-drift-production', '--non-interactive', '--message', message],
                                cwd=ROOT, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        (output / 'deploy.log').write_text(result.stdout)
        print(result.stdout, flush=True)
        assert result.returncode == 0 and 'Deploy complete!' in result.stdout
        management = json.loads(run('firebase', 'hosting:channel:list', '--project',
                                    'luma-drift-production', '--json'))
        (output / 'management-original.json').write_text(json.dumps(management, indent=2) + '\n')
        live = next(item for item in management['result']['channels'] if item['name'].endswith('/channels/live'))
        release = live['release']
        assert release['message'] == message and release['type'] == 'DEPLOY'
        assert release['version']['status'] == 'FINALIZED'
        receipt = dict(preflight, recordedAtUtc=datetime.datetime.now(datetime.timezone.utc).isoformat(),
                       hostingUrl=live['url'], releaseTime=release['releaseTime'],
                       releaseName=release['name'], version=release['version']['name'],
                       status='DEPLOY/FINALIZED', publicOriginBytesVerified=False,
                       publicOriginRenderingVerified=False)
        (output / 'deployment.json').write_text(json.dumps(receipt, indent=2) + '\n')
        print('COGNITIVE_LAB_RECEIPT ' + json.dumps(receipt), flush=True)


if __name__ == '__main__':
    main()
