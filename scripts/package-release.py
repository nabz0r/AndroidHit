#!/usr/bin/env python3
"""Collect a built flavor without replacing a previously published version's bytes."""
from pathlib import Path
import argparse
import datetime
import hashlib
import json
import re
import shutil

root = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--variant', choices=('production', 'lab'), default='production')
args = parser.parse_args()
variant = args.variant
release = root / 'releases'
release.mkdir(exist_ok=True)
output = root / 'app/build/outputs'
metadata = json.loads((output / f'apk/{variant}/release/output-metadata.json').read_text())
element, = metadata['elements']
version = element['versionName']
package = metadata['applicationId']
expected = 'com.androidhit.lumadrift' + ('.sandbox' if variant == 'lab' else '')
if package != expected or not re.fullmatch(r'[0-9A-Za-z.-]+', version):
    raise SystemExit('Unexpected package/version; refusing artifact collection')

artifacts = [
    (output / f'apk/{variant}/release' / element['outputFile'], release / f'luma-drift-{version}.apk'),
    (output / f'bundle/{variant}Release/app-{variant}-release.aab', release / f'luma-drift-{version}.aab'),
]
for source, target in artifacts:
    if not source.is_file():
        raise SystemExit(f'Missing build output: {source}')
    if target.exists() and target.read_bytes() != source.read_bytes():
        raise SystemExit(f'{target.name} already exists with different bytes; choose a new version')
for source, target in artifacts:
    shutil.copyfile(source, target)

old_manifest = release / 'manifest.json'
if old_manifest.exists():
    old = json.loads(old_manifest.read_text())
    archive = release / f"manifest-{old['version']}.json"
    if not archive.exists():
        shutil.copyfile(old_manifest, archive)

build_config = (root / f'app/build/generated/source/buildConfig/{variant}/release/com/androidhit/lumadrift/BuildConfig.java').read_text()
def flag(name):
    match = re.search(rf'boolean {name} = (true|false);', build_config)
    if not match:
        raise SystemExit(f'Missing compiled flag: {name}')
    return match.group(1) == 'true'

inputs = [*(root/'app/src').rglob('*'),
          root/'app/build.gradle', root/'app/proguard-rules.pro', root/'build.gradle', root/'settings.gradle', root/'gradle.properties',
          *(root/'gradle').rglob('*'), root/'gradlew']
paths = sorted({p for p in inputs if p.is_file()}, key=str)
record = {
    'version': version, 'version_code': element['versionCode'], 'channel': variant,
    'application_id': package, 'build_date': datetime.date.today().isoformat(),
    'configuration': {'sandbox': flag('MODE_SANDBOX'), 'force_test_ads': flag('FORCE_TEST_ADS'),
                      'paid_cosmetic_draws_enabled': flag('PAID_COSMETIC_DRAWS_ENABLED'),
                      'wallet_api_configured': bool(re.search(r'String WALLET_API_BASE_URL = "[^"]+";', build_config)),
                      'social_api_configured': bool(re.search(r'String SOCIAL_API_BASE_URL = "https://[^"]+";', build_config)),
                      'live_services_validated': False},
    'source_inputs_sha256': {str(p.relative_to(root)): hashlib.sha256(p.read_bytes()).hexdigest() for p in paths},
    'artifacts': {p.name: {'bytes': p.stat().st_size, 'sha256': hashlib.sha256(p.read_bytes()).hexdigest()}
                  for p in sorted(release.iterdir()) if p.suffix in ('.apk', '.aab', '.pem')},
}
text = json.dumps(record, indent=2) + '\n'
(release/'manifest.json').write_text(text)
(release/f'manifest-{version}.json').write_text(text)
(release/'SHA256SUMS.txt').write_text(''.join(value['sha256'] + '  ' + name + '\n' for name, value in record['artifacts'].items()))
print(json.dumps({'version':version,'package':package,'configuration':record['configuration'],
                  'new_artifacts':{target.name:record['artifacts'][target.name] for _,target in artifacts}},indent=2))
