#!/usr/bin/env python3
"""Copy exact released binaries and passed native PNGs into the existing website.

All inputs are checked before writing public bytes. No image transformation,
app build, provider action, network request or deployment is performed.
"""
import argparse
import hashlib
import json
from pathlib import Path
import struct

ROOT = Path(__file__).resolve().parents[1]
VERSION = '1.15.0-rc1'
APK_SHA = '07b5bfc2632edc3d34f7583ac71c8343b52d7b926ed526aeb7a88599253df0f9'


def sha(raw):
    return hashlib.sha256(raw).hexdigest()


def safe(value, prefix):
    assert value.startswith(prefix), value
    path = (ROOT / value).resolve()
    assert ROOT in path.parents, value
    return path


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--capture-map', type=Path, default=ROOT / 'docs/validation/site-v1.15/capture-map.json')
    args = parser.parse_args()
    release_path = ROOT / 'releases' / ('manifest-' + VERSION + '.json')
    release = json.loads(release_path.read_text())
    assert release['version'] == VERSION and release['version_code'] == 17
    pending = []
    for extension in ('apk', 'aab'):
        name = 'luma-drift-' + VERSION + '.' + extension
        data = (ROOT / 'releases' / name).read_bytes()
        entry = release['artifacts'][name]
        assert sha(data) == entry['sha256'] and len(data) == entry['bytes'], name
        if extension == 'apk':
            assert sha(data) == APK_SHA
        pending.append((ROOT / 'public/releases' / name, data))
    pending.append((ROOT / 'public/releases' / release_path.name, release_path.read_bytes()))
    declared = json.loads(args.capture_map.read_text())['images']
    records, seen = [], set()
    for item in declared:
        public = safe(item['public'], 'public/assets/screenshots/v1.15/')
        source_prefix = next((prefix for prefix in ('docs/screenshots/v1.15/', 'docs/validation/v1.15/')
                              if item['source'].startswith(prefix)), None)
        assert source_prefix, item['source']
        original = safe(item['source'], source_prefix)
        evidence = safe(item['evidence'], 'docs/validation/')
        assert item['public'] not in seen
        seen.add(item['public'])
        assert not any(x in item['source'] for x in ('analysis-crop', '/analysis/'))
        data = original.read_bytes()
        assert data.startswith(b'\x89PNG\r\n\x1a\n')
        width, height = struct.unpack('>II', data[16:24])
        assert width >= 800 and height > width
        digest = sha(data)
        report = json.loads(evidence.read_text())
        assert report.get('passed') is True
        assert (report.get('apk_sha256') or report.get('game_apk_sha256')) == APK_SHA
        indexed = report.get('screenshots', report.get('evidence_sha256', {}))
        assert any(value == digest and (item['source'] == key or item['source'].endswith('/' + key))
                   for key, value in indexed.items()), item['source']
        assert item['locale'] in ('fr', 'en') and item['public'].endswith('-' + item['locale'] + '.png')
        assert item['description']
        record = dict(item, sha256=digest, width=width, height=height)
        records.append(record)
        pending.append((public, data))
    for path, data in pending:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(data)
    output = ROOT / 'docs/validation/site-v1.15/screenshot-provenance.json'
    output.write_text(json.dumps({'schemaVersion': 1, 'release': VERSION,
                                 'screenshotsEdited': False, 'images': records}, indent=2) + '\n')
    print(json.dumps({'passed': True, 'release': VERSION, 'native_originals': len(records),
                      'hosted_production_apk_sha256': APK_SHA, 'binary_files': 3}))


if __name__ == '__main__':
    main()
