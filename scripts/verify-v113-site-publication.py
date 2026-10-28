#!/usr/bin/env python3
"""Compare the published Firebase payload with the exact 1.13 release manifest."""
import argparse
import concurrent.futures
import datetime
import hashlib
import json
import pathlib
import re
import subprocess
import urllib.request

ROOT = pathlib.Path(__file__).resolve().parents[1]
ORIGIN = 'https://luma-drift-production.web.app'

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--commit', required=True)
    parser.add_argument('--output', default='docs/validation/site-v1.13/public-origin.json')
    args = parser.parse_args()
    if not re.fullmatch(r'[0-9a-f]{40}', args.commit):
        parser.error('An immutable forty-character source commit is required')
    folder = ROOT / 'docs/validation/site-v1.13'
    source = json.loads((folder / 'release/source-manifest.json').read_text())
    deployed = json.loads((folder / 'deployment.json').read_text())
    assert source['status'] == deployed['status'] == 'PASS'
    assert source['release'] == deployed['android_release'] == '1.13.0-rc2'
    assert deployed['source_commit'] == args.commit
    assert deployed['cli_deploy_complete'] and deployed['live_management']['version_status'] == 'FINALIZED'
    tree = subprocess.check_output(['git', 'rev-parse', args.commit + ':public'], cwd=ROOT, text=True).strip()
    assert tree == deployed['source_public_tree']
    entries = list(source['files'].items())
    aliases = {'/': '/index.html', '/privacy/': '/privacy/index.html', '/delete-account/': '/delete-account/index.html'}
    entries += [(alias, source['files'][target]) for alias, target in aliases.items()]

    def probe(entry):
        path, expected = entry
        digest = hashlib.sha256()
        size = 0
        request = urllib.request.Request(ORIGIN + path, headers={
            'User-Agent': 'LumaDrift-site-integrity/1.13', 'Cache-Control': 'no-cache',
        })
        with urllib.request.urlopen(request, timeout=90) as response:
            status = response.status
            final_url = response.url
            while True:
                data = response.read(1024 * 1024)
                if not data:
                    break
                size += len(data)
                digest.update(data)
        actual = digest.hexdigest()
        assert status == 200 and size == expected['bytes'] and actual == expected['sha256'], 'Published payload differs: ' + path
        return {'path': path, 'url': final_url, 'http_status': status,
                'bytes': size, 'sha256': actual, 'matches_deployed_source': True,
                'directory_alias': path in aliases}

    with concurrent.futures.ThreadPoolExecutor(max_workers=6) as pool:
        results = list(pool.map(probe, entries))
    record = {
        'status': 'PASS', 'recorded_at_utc': datetime.datetime.now(datetime.timezone.utc).isoformat(),
        'source_commit': args.commit, 'source_public_tree': tree, 'origin': ORIGIN,
        'android_release': '1.13.0-rc2', 'payloads_verified': len(source['files']),
        'directory_aliases_verified': len(aliases), 'responses': results,
        'source_manifest_sha256': hashlib.sha256((folder / 'release/source-manifest.json').read_bytes()).hexdigest(),
        'scope': 'Independent public HTTPS response bytes for every Hosting payload and three directory aliases. Browser rendering is recorded separately; no provider transaction or Play publication is inferred.',
    }
    output = pathlib.Path(args.output)
    if not output.is_absolute():
        output = ROOT / output
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(record, indent=2) + '\n')
    print(json.dumps({k: record[k] for k in ('status', 'source_commit', 'payloads_verified', 'directory_aliases_verified')}))

if __name__ == '__main__':
    main()
