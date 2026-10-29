#!/usr/bin/env python3
"""Qualify exact installed v118 art through isolated real Android Canvas APIs."""
from pathlib import Path
import argparse
import hashlib
import json
import os
import re
import shutil
import struct
import subprocess
import sys
import tempfile
import time
import uuid
import zipfile

ROOT = Path(__file__).resolve().parents[2]
HERE = Path(__file__).resolve().parent
TEST = 'com.androidhit.lumadrift.motiontest.v118'
RUNNER = TEST + '/com.androidhit.lumadrift.qa.v118motion.MotionInstrumentation'
SIGNER = 'dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287'
HEX = re.compile(r'[a-f0-9]{64}')
SCOPE = ('Isolated real Android Canvas through public pure renderer APIs on the receipt-owned5594. '
         'No Activity, touchscreen, game state, ownership or save verification. '
         'Exact source assets, installed APK identity, output hashes and coverage are checked. '
         'Decode/render success does not qualify anatomical garment fitting or visual quality.')


def require(value, message):
    if not value:
        raise ValueError(message)


def sha(path):
    h = hashlib.sha256()
    with Path(path).open('rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def write_json(path, value):
    path.write_text(json.dumps(value, indent=2, sort_keys=True) + '\n')


def catalogue():
    ids = re.findall(r'new CharacterItem\("([a-z0-9_]+)"',
                     (ROOT / 'app/src/main/java/com/androidhit/lumadrift/data/Catalog.java').read_text())
    garments = re.findall(r'new Item\("([a-z0-9_]+)"',
                          (ROOT / 'app/src/main/java/com/androidhit/lumadrift/data/CosmeticCatalog.java').read_text())
    require(len(ids) == 37 and len(set(ids)) == 37, 'Expected exact expanded37-character source catalogue')
    require(len(garments) == 24 and len(set(garments)) == 24, 'Expected existing24 unique garment designs')
    return ids, garments


def packaged_art(apk, complete, ids):
    """Bind every packaged runtime art byte to the current source tree, not a filename only."""
    prefix = 'assets/art/v118/'
    local = ROOT / 'app/src/main/assets/art/v118'
    hashes, sheets = {}, set()
    with zipfile.ZipFile(apk) as archive:
        names = archive.namelist()
        require(len(names) == len(set(names)), 'Duplicate ZIP entries refused')
        packed = {name for name in names if name.startswith(prefix) and not name.endswith('/')}
        source = {prefix + str(p.relative_to(local)) for p in local.rglob('*') if p.is_file()}
        require(packed == source and packed, 'Packaged v118 art set differs from source assets')
        for name in sorted(packed):
            relative = name.removeprefix('assets/')
            data = archive.read(name)
            digest = hashlib.sha256(data).hexdigest()
            require(sha(ROOT / 'app/src/main/assets' / relative) == digest,
                    'Packaged/source art bytes differ: ' + relative)
            hashes[relative] = digest
            if not name.endswith('-poses.json'):
                continue
            meta = json.loads(data)
            identity, form = meta['id'], meta['form']
            require(identity in ids and type(form) is int and 0 <= form < 4, 'Unknown art identity/form')
            require((identity, form) not in sheets, 'Duplicate art identity/form')
            require(meta['schema'] == 1 and meta['columns'] == 4 and meta['rows'] == 5,
                    'Frozen20-pose schema required')
            require(meta['levelMilestone'] == [1, 3, 6, 10][form], 'Incorrect evolution milestone')
            poses = meta['frames']
            require(len(poses) == 20 and {p['poseIndex'] for p in poses} == set(range(20)),
                    'Exactly20 unique poses per sheet required')
            require(meta['sourceRectsMeasured'] is True and meta['pixelEquivalent'] is True,
                    'Measured crops and lossless original-pixel provenance required')
            image = 'assets/' + meta['image']
            require(image in packed, 'Metadata image is absent from exact APK')
            require(hashlib.sha256(archive.read(image)).hexdigest() == meta['webpSha256'],
                    'Metadata image hash differs from APK bytes')
            sheets.add((identity, form))
    require(('nova', 0) in sheets, 'Individual garment samples require Nova first form')
    if complete:
        require(sheets == {(identity, form) for identity in ids for form in range(4)},
                'Final qualification requires all148 sheets/2960 frames')
    return hashes, sheets


def snapshot_sources(output, variant):
    """Freeze source and compiled API witnesses; this is not a formal source-to-dex proof."""
    api = ROOT / f'app/build/intermediates/javac/{variant}Release/compile{variant.title()}ReleaseJavaWithJavac/classes'
    require(api.is_dir(), 'Compile the exact release variant API before running')
    java_root = ROOT / 'app/src/main/java'
    sources = sorted(java_root.rglob('*.java'))
    require(sources, 'Game Java source witness missing')
    for source in sources:
        top_class = api / source.relative_to(java_root).with_suffix('.class')
        require(top_class.is_file() and top_class.stat().st_mtime_ns >= source.stat().st_mtime_ns,
                'Release API is older than current source: ' + str(source.relative_to(ROOT)))
    frozen = {}
    for source in sources:
        relative = source.relative_to(ROOT)
        target = output / 'target-source' / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(source, target)
        frozen[str(relative)] = sha(target)
    runner = {}
    for source in sorted(HERE.rglob('*')):
        if not source.is_file() or '__pycache__' in source.parts:
            continue
        relative = source.relative_to(HERE)
        target = output / 'runner-source' / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(source, target)
        runner[str(relative)] = sha(target)
    classes = {}
    with zipfile.ZipFile(output / 'compiled-api-witness.zip', 'w', zipfile.ZIP_DEFLATED) as archive:
        for source in sorted(api.rglob('*.class')):
            relative = str(source.relative_to(api))
            classes[relative] = sha(source)
            archive.write(source, relative)
    require(classes, 'Compiled API witness empty')
    witness = {'variant': variant, 'java_sources_sha256': frozen, 'runner_sources_sha256': runner,
               'compiled_api_sha256': classes,
               'scope': 'Exact source and compiled API snapshot captured before the run; source freshness checked. '
                        'This records the build witness present and does not establish a formal Java-source-to-APK-dex mapping.'}
    write_json(output / 'source-witness.json', witness)
    return frozen, runner, classes, api


def verify_frozen(frozen, runner, classes, api):
    require({str(p.relative_to(ROOT)): sha(p) for p in sorted((ROOT / 'app/src/main/java').rglob('*.java'))} == frozen,
            'Game source changed during qualification')
    actual = {str(p.relative_to(HERE)): sha(p) for p in sorted(HERE.rglob('*'))
              if p.is_file() and '__pycache__' not in p.parts}
    require(actual == runner, 'Observer source changed during qualification')
    require({str(p.relative_to(api)): sha(p) for p in sorted(api.rglob('*.class'))} == classes,
            'Compiled API witness changed during qualification')


def verify_native(directory, token, version, sheets, garments, complete):
    report = json.loads((directory / 'report.json').read_text())
    registry = json.loads((directory / 'registry.json').read_text())
    require(report.get('passed') is True and report.get('runToken') == token,
            'Only the current successful native execution counts')
    require(report.get('version') == version and report.get('code') == 25, 'Native version/code differs')
    checks = report['checks']
    require(checks and all(item.get('passed') is True for item in checks), 'Native check failed or missing')
    require(registry['frames'] == len(sheets) * 20 and not registry['metadataFailures'] and not registry['decodeFailures'],
            'Exact native registry has missing or refused frames')
    require(0 <= registry['qualifiedFrames'] <= registry['frames'], 'Invalid apparel qualification count')
    require(0 <= registry['cacheBytes'] <= registry['cacheLimit'], 'Native cache exceeds its budget')
    expected = {(identity, form, look) for identity, form in sheets for look in [0, 1]}
    renders = report['renders']
    actual = {(r['character'], r['form'], r['look']) for r in renders}
    require(actual == expected and len(renders) == len(expected), 'Native renders omit or duplicate a form/look')
    expected_images = {}
    for item in renders:
        require(item['poses'] == 20, 'Every native sheet must contain20 composites')
        name = f"{item['character']}-form{item['form']}-{'body' if item['look'] == 0 else 'dew-gear'}.png"
        require(item['image'] == name, 'Native sheet name does not bind its form/look')
        expected_images[name] = (960, 1550)
    expected_images.update({'garment-' + name + '.png': (960, 620) for name in garments})
    require(report['composites'] == len(sheets) * 40 + len(garments) * 8, 'Native composite count differs')
    require(set(report['images']) == set(expected_images), 'Native image manifest differs from coverage contract')
    require({p.name for p in directory.glob('*.png')} == set(expected_images), 'Pulled PNG set differs from native manifest')
    for name, dimensions in expected_images.items():
        digest = report['images'][name]
        require(isinstance(digest, str) and HEX.fullmatch(digest), 'Malformed native image hash')
        path = directory / name
        require(path.is_file() and sha(path) == digest, 'Native PNG bytes/hash differ: ' + name)
        with path.open('rb') as stream:
            header = stream.read(24)
        require(len(header) == 24 and header[:8] == b'\x89PNG\r\n\x1a\n' and header[12:16] == b'IHDR'
                and struct.unpack('>II', header[16:24]) == dimensions, 'Native PNG dimensions differ: ' + name)
    if complete:
        require(registry['frames'] == 2960 and report['composites'] == 6112 and len(expected_images) == 320,
                'Final37-character coverage must be2960 frames/6112 composites/320 PNGs')
    return {'checks': len(checks), 'frames': registry['frames'], 'qualified_frames': registry['qualifiedFrames'],
            'composites': report['composites'], 'images': len(expected_images), 'complete': complete}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--serial', required=True)
    parser.add_argument('--expected-avd', required=True)
    parser.add_argument('--creation-receipt', required=True, type=Path)
    parser.add_argument('--creation-sha256', required=True)
    parser.add_argument('--expected-apk', required=True, type=Path)
    parser.add_argument('--sha256', required=True)
    parser.add_argument('--test-apk', required=True, type=Path)
    parser.add_argument('--test-sha256', required=True)
    parser.add_argument('--variant', choices=['production', 'lab'], default='lab')
    parser.add_argument('--expected-version', required=True)
    parser.add_argument('--expected-code', required=True, type=int)
    parser.add_argument('--complete', choices=['true', 'false'], default='true')
    parser.add_argument('--label', required=True)
    args = parser.parse_args()
    complete = args.complete == 'true'
    require(args.serial == 'emulator-5594' and args.expected_avd == 'Luma118-Expedition-QA',
            'Only the newly task-owned5594 device is authorized')
    require(HEX.fullmatch(args.creation_sha256) and sha(args.creation_receipt) == args.creation_sha256,
            'Exact immutable creation receipt hash required')
    receipt = json.loads(args.creation_receipt.read_text())
    require(receipt.get('fresh') is True and receipt.get('serial') == args.serial
            and receipt.get('avd') == args.expected_avd and receipt.get('port') == 5594
            and receipt.get('owner') == 'v118 expedition qualification', 'Task device provenance differs')
    require(HEX.fullmatch(args.sha256) and sha(args.expected_apk) == args.sha256, 'Exact frozen APK bytes required')
    require(HEX.fullmatch(args.test_sha256) and sha(args.test_apk) == args.test_sha256, 'Exact frozen observer bytes required')
    require(args.expected_code == 25 and args.expected_version.startswith('1.18.'), 'Only v118 code25 candidate supported')
    require(re.fullmatch(r'[A-Za-z0-9_-]{1,60}', args.label), 'Safe new evidence label required')
    ids, garments = catalogue()
    assets, sheets = packaged_art(args.expected_apk, complete, ids)
    sdk = Path(os.environ.get('ANDROID_HOME', Path.home() / 'Library/Android/sdk'))
    adb = sdk / 'platform-tools/adb'
    env = os.environ.copy()
    env.setdefault('JAVA_HOME', '/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home')

    def call(*parts, timeout=90):
        return subprocess.check_output([str(adb), '-s', args.serial, *map(str, parts)],
                                       stderr=subprocess.STDOUT, timeout=timeout, env=env)

    def installed(package):
        result = subprocess.run([str(adb), '-s', args.serial, 'shell', 'pm', 'path', package],
                                capture_output=True, text=True, timeout=30, env=env)
        require(result.returncode in [0, 1], 'Package query failed')
        paths = [line[8:] for line in result.stdout.splitlines() if line.startswith('package:')]
        require(len(paths) <= 1 and all(path.startswith('/data/app/') for path in paths),
                'Only a single exact installed APK is supported')
        return paths[0] if paths else None

    require(call('shell', 'getprop', 'ro.kernel.qemu').decode().strip() == '1', 'Emulator only')
    require(call('emu', 'avd', 'name').decode().splitlines()[0].strip() == args.expected_avd,
            'Live AVD identity differs')
    package = 'com.androidhit.lumadrift' + ('.sandbox' if args.variant == 'lab' else '')
    target = installed(package)
    require(target, 'Owner must install exact candidate; this runner never installs or upgrades the game')
    require(not installed(TEST), 'Observer must initially be absent')
    output = ROOT / 'docs/validation/v1.18/native-motion-render' / args.label
    require(not output.exists(), 'Evidence label must be new; failed attempts remain immutable')
    output.mkdir(parents=True)
    token = str(uuid.uuid4())
    host = {'passed': False, 'serial': args.serial, 'avd': args.expected_avd, 'package': package,
            'apk_sha256': args.sha256, 'observer_sha256': args.test_sha256,
            'creation_receipt_sha256': args.creation_sha256, 'run_token': token,
            'expected_version': args.expected_version, 'expected_code': args.expected_code,
            'complete': complete, 'source_sheets': len(sheets), 'scope': SCOPE}
    started, observer_install_attempted = time.monotonic(), False
    try:
        observer = output / 'motion-observer.apk'
        shutil.copyfile(args.test_apk, observer)
        shutil.copyfile(args.creation_receipt, output / 'device-creation.json')
        require(sha(observer) == args.test_sha256 and sha(output / 'device-creation.json') == args.creation_sha256,
                'Frozen observer or creation receipt changed while copied')
        frozen, runner, classes, api = snapshot_sources(output, args.variant)
        write_json(output / 'packaged-art-sha256.json', assets)
        for name, path, identity in [('candidate', args.expected_apk, package), ('observer', observer, TEST)]:
            proof = subprocess.check_output([str(sdk / 'build-tools/36.0.0/apksigner'), 'verify', '--verbose',
                                             '--print-certs', str(path)], stderr=subprocess.STDOUT, env=env).decode()
            (output / (name + '-signature.txt')).write_text(proof)
            require(re.findall(r'Signer #1 certificate SHA-256 digest: ([a-f0-9]+)', proof) == [SIGNER],
                    name + ' signing identity differs')
            manifest = subprocess.check_output([str(sdk / 'build-tools/36.0.0/aapt2'), 'dump', 'badging',
                                                str(path)], stderr=subprocess.STDOUT, env=env).decode()
            (output / (name + '-manifest.txt')).write_text(manifest)
            info = re.search(r"package: name='([^']+)' versionCode='([^']+)' versionName='([^']+)'", manifest)
            require(info and info[1] == identity, name + ' package identity differs')
            if name == 'candidate':
                require(info[2] == str(args.expected_code) and info[3] == args.expected_version,
                        'Frozen APK version/code differs')
            else:
                tree = subprocess.check_output([str(sdk / 'build-tools/36.0.0/aapt2'), 'dump', 'xmltree',
                                                str(path), '--file', 'AndroidManifest.xml'],
                                               stderr=subprocess.STDOUT, env=env).decode()
                (output / 'observer-manifest-tree.txt').write_text(tree)
                targets = re.findall(r':targetPackage\([^)]*\)="([^"]+)"', tree)
                require(targets == [package] and ':name(0x01010003)="com.androidhit.lumadrift.qa.v118motion.MotionInstrumentation"' in tree,
                        'Observer instrumentation target/class differs from exact selected game')
                require(not re.search(r'E: (?:activity|service|receiver|provider)\b', tree),
                        'Pure observer must not declare additional application components')
        with tempfile.TemporaryDirectory(prefix='luma118-motion-installed-') as temporary:
            actual = Path(temporary) / 'installed.apk'
            call('pull', target, actual, timeout=180)
            require(sha(actual) == args.sha256, 'Installed APK bytes differ from exact frozen candidate')
        host['installed_sha256_before'] = args.sha256
        host['device'] = {key: call('shell', *command).decode().strip() for key, command in {
            'sdk': ['getprop', 'ro.build.version.sdk'], 'abi': ['getprop', 'ro.product.cpu.abi'],
            'page_size': ['getconf', 'PAGESIZE'], 'size': ['wm', 'size'], 'density': ['wm', 'density']}.items()}
        observer_install_attempted = True
        (output / 'observer-install.txt').write_bytes(call('install', '-t', observer, timeout=180))
        call('shell', 'am', 'force-stop', package)
        command = ['shell', 'am', 'instrument', '-w', '-r', '-e', 'complete', args.complete,
                   '-e', 'runToken', token, '-e', 'expectedVersion', args.expected_version, RUNNER]
        (output / 'instrumentation-command.json').write_text(json.dumps(command) + '\n')
        result = call(*command, timeout=1800)
        (output / 'instrumentation.txt').write_bytes(result)
        remote = '/sdcard/Android/data/' + package + '/files/v118-motion-render/' + token
        call('pull', remote, output / 'native', timeout=300)
        require(b'INSTRUMENTATION_CODE: -1' in result and b'INSTRUMENTATION_FAILED' not in result,
                'Current instrumentation failed; no stale output qualifies')
        host['coverage'] = verify_native(output / 'native', token, args.expected_version, sheets, garments, complete)
        verify_frozen(frozen, runner, classes, api)
        after_assets, after_sheets = packaged_art(args.expected_apk, complete, ids)
        require(after_assets == assets and after_sheets == sheets and sha(args.expected_apk) == args.sha256,
                'Frozen source assets or candidate changed during qualification')
        with tempfile.TemporaryDirectory(prefix='luma118-motion-after-') as temporary:
            actual = Path(temporary) / 'installed.apk'
            require(installed(package) == target, 'Installed package path changed during run')
            call('pull', target, actual, timeout=180)
            require(sha(actual) == args.sha256, 'Installed candidate changed during run')
        host['installed_sha256_after'] = args.sha256
        host['passed'] = True
    except Exception as failure:
        host['error'] = str(failure)
        try:
            (output / 'failure-android-runtime.txt').write_bytes(
                call('logcat', '-d', '-v', 'threadtime', '-s', 'AndroidRuntime:V', timeout=30))
        except Exception as diagnostic:
            host['diagnostic_error'] = str(diagnostic)
    finally:
        if observer_install_attempted:
            try:
                if installed(TEST):
                    (output / 'observer-uninstall.txt').write_bytes(call('uninstall', TEST))
                require(not installed(TEST), 'Observer remains installed after cleanup')
            except Exception as failure:
                host['observer_cleanup_error'] = str(failure)
                host['passed'] = False
        host['elapsed_seconds'] = round(time.monotonic() - started, 3)
        host['evidence_sha256'] = {str(path.relative_to(output)): sha(path)
                                   for path in sorted(output.rglob('*')) if path.is_file()}
        write_json(output / 'report.json', host)
        print(json.dumps({'passed': host['passed'], 'evidence': str(output), 'complete': complete,
                          'coverage': host.get('coverage'), 'error': host.get('error')}))
    return 0 if host['passed'] else 1


if __name__ == '__main__':
    sys.exit(main())
