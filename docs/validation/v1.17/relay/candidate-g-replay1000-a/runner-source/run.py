#!/usr/bin/env python3
"""Replay earned U levels on the exact final 1.17 APK and the explicitly owned QA AVD."""
from pathlib import Path
import argparse
import hashlib
import json
import os
import re
import select
import shutil
import subprocess
import tempfile
import time
import traceback
import zipfile
import uuid

ROOT = Path(__file__).resolve().parents[2]
HERE = Path(__file__).resolve().parent
FIXTURES = ROOT / 'scripts/v115-relay-ui/assets/fixtures'
TEST_PACKAGE = 'com.androidhit.lumadrift.relaytest.v117'
RUNNER_SOURCE = HERE / 'java/com/androidhit/lumadrift/qa/v117relay/RelayInstrumentation.java'
RUNNER = TEST_PACKAGE + '/com.androidhit.lumadrift.qa.v117relay.RelayInstrumentation'
SIGNER_SHA256 = 'dcc06861630bb1ac0daf76f8f2d22de633238c8b36a199050ba5bdcb5ddcc287'


def sha(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def verify_fixtures():
    manifest = json.loads((FIXTURES / 'manifest.json').read_text())
    assert manifest['family'] == 'U' and manifest['count'] == 1000
    for path, digest in manifest['source_sha256'].items():
        assert sha(ROOT / path) == digest, 'Frozen engine/oracle source differs: ' + path
    assert len(manifest['fixture_sha256']) == 1000
    exits = 0
    for number in range(1, 1001):
        name = 'u' + str(number) + '.json'
        path = FIXTURES / name
        assert sha(path) == manifest['fixture_sha256'][name], 'Frozen certificate differs: ' + name
        fixture = json.loads(path.read_text())
        assert fixture['number'] == number and fixture['contentVersion'] == 7 and fixture['policy'] == 5
        assert fixture['pieces'] == len(fixture['steps'])
        exits += len(fixture['steps'])
    return manifest, exits


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--verify-fixtures-only', action='store_true')
    p.add_argument('--serial')
    p.add_argument('--expected-avd', default='Luma-Refonte115-QA')
    p.add_argument('--expected-apk')
    p.add_argument('--sha256')
    p.add_argument('--test-apk')
    p.add_argument('--expected-version', help='Explicit frozen production version; mandatory for Android replay')
    p.add_argument('--expected-code', type=int, help='Explicit frozen code; mandatory for Android replay')
    p.add_argument('--label')
    p.add_argument('--from', dest='first', type=int, default=1)
    p.add_argument('--through', type=int, default=1000)
    p.add_argument('--timeout', type=int, default=18000)
    a = p.parse_args()
    manifest, certificate_exits = verify_fixtures()
    if a.verify_fixtures_only:
        print(json.dumps({'passed': True, 'scope': 'Read-only frozen host certificates; no Android run', 'levels': 1000, 'certificate_exits': certificate_exits, 'manifest_sha256': sha(FIXTURES / 'manifest.json')}))
        return
    assert a.expected_avd == 'Luma-Refonte115-QA' and a.serial == 'emulator-5586', 'Exact owned AVD and matching serial required'
    assert a.expected_code and a.expected_code > 0 and a.expected_version and re.fullmatch(r'1\.17\.0-rc[1-9][0-9]*', a.expected_version), 'Explicit frozen production 1.17 version/code required'
    assert a.label and re.fullmatch(r'[A-Za-z0-9_-]{1,50}', a.label), 'Safe new evidence label required'
    assert 1 <= a.first <= a.through <= 1000, 'Bounded requested replay range'
    assert a.expected_apk and a.test_apk and a.sha256, 'Explicit final APK/hash and observer required'
    apk, observer = Path(a.expected_apk).resolve(), Path(a.test_apk).resolve()
    assert apk.is_file() and re.fullmatch(r'[a-f0-9]{64}', a.sha256) and sha(apk) == a.sha256, 'Exact final candidate bytes required'
    assert observer.is_file(), 'Separate signed framework observer required'
    sdk = Path(os.environ.get('ANDROID_HOME', Path.home() / 'Library/Android/sdk'))
    adb = str(sdk / 'platform-tools/adb')
    signer = str(sdk / 'build-tools/36.0.0/apksigner')
    signing_env = os.environ.copy()
    signing_env.setdefault('JAVA_HOME', '/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home')
    signature_proofs = {}
    for name, path in [('candidate', apk), ('observer', observer)]:
        proof = subprocess.check_output([signer, 'verify', '--verbose', '--print-certs', str(path)], stderr=subprocess.STDOUT, env=signing_env).decode()
        observed = re.findall(r'Signer #1 certificate SHA-256 digest: ([a-f0-9]+)', proof)
        assert observed == [SIGNER_SHA256], name + ' signing identity differs'
        signature_proofs[name] = proof
    with zipfile.ZipFile(observer) as archive:
        assert hashlib.sha256(archive.read('assets/fixtures/manifest.json')).hexdigest() == sha(FIXTURES / 'manifest.json')
        for name, digest in manifest['fixture_sha256'].items():
            assert hashlib.sha256(archive.read('assets/fixtures/' + name)).hexdigest() == digest, 'Packaged observer certificate differs: ' + name
    package = 'com.androidhit.lumadrift'

    def call(*parts, timeout=90):
        return subprocess.check_output([adb, '-s', a.serial, *parts], stderr=subprocess.STDOUT, timeout=timeout)

    def shell(*parts):
        return call('shell', *parts).decode(errors='replace')

    def installed(pkg):
        result = subprocess.run([adb, '-s', a.serial, 'shell', 'pm', 'path', pkg], capture_output=True, text=True, timeout=60)
        assert result.returncode in (0, 1), 'Package query failed'
        lines = [line[8:] for line in result.stdout.splitlines() if line.startswith('package:')]
        assert len(lines) <= 1, 'Split installations are outside the exact-APK scope'
        assert all(line.startswith('/data/app/') for line in lines), 'Unexpected installation path'
        return lines[0] if lines else None

    assert shell('getprop', 'ro.kernel.qemu').strip() == '1', 'Emulator-only qualification'
    assert call('emu', 'avd', 'name').decode().splitlines()[0].strip() == a.expected_avd, 'Exact retained task-owned AVD required'
    out = ROOT / 'docs/validation/v1.17/relay' / a.label
    assert not out.exists(), 'Evidence directory must be new; preserve failed/interrupted attempts'
    out.mkdir(parents=True)
    frozen_observer = out / 'relay-observer.apk'
    shutil.copyfile(observer, frozen_observer)
    assert sha(observer) == sha(frozen_observer)
    observer = frozen_observer
    for name, proof in signature_proofs.items():
        (out / (name + '-signature.txt')).write_text(proof)
    snapshots = {}
    for path in sorted(HERE.rglob('*')):
        if path.is_file() and '__pycache__' not in path.parts:
            relative = path.relative_to(HERE)
            destination = out / 'runner-source' / relative
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(path, destination)
            snapshots[str(relative)] = sha(destination)
    (out / 'source-snapshot.json').write_text(json.dumps(snapshots, indent=2) + '\n')
    report = {'passed': False, 'package': package, 'serial': a.serial, 'avd': a.expected_avd, 'from': a.first, 'through': a.through, 'apk_sha256': a.sha256, 'expected_version': a.expected_version, 'expected_code': a.expected_code, 'test_apk_sha256': sha(observer), 'wrapper_sha256': sha(__file__), 'runner_sha256': sha(RUNNER_SOURCE), 'fixture_manifest_sha256': sha(FIXTURES / 'manifest.json'), 'fixture_count': 1000, 'scope': 'Exact explicitly identified signed production 1.17 APK; retained task-owned fully completed QA profile. Real UI map jump, keyboard Done, certificate touchscreen exits and Next objects. Read-only preferences; replay rewards must remain zero; no state injection, seeding, account, ad or purchase. Initial puzzle session is naturally replaced by actual replay, while earned profile/ownership/house/stock and original language are checked.'}
    report['run_token'] = str(uuid.uuid4())
    started = time.monotonic()
    remote = '/sdcard/Android/data/' + package + '/files/v117-relay-native/' + a.label
    observer_installed_here = False
    try:
        candidate_path = installed(package)
        assert candidate_path, 'Final game must already be installed by its owner; this runner never installs/upgrades the game'
        with tempfile.TemporaryDirectory(prefix='luma-v117-installed-') as temp:
            copied = Path(temp) / 'candidate.apk'
            call('pull', candidate_path, str(copied))
            report['installed_apk_sha256'] = sha(copied)
            assert report['installed_apk_sha256'] == a.sha256, 'Installed game differs from final frozen APK'
            previous = installed(TEST_PACKAGE)
            if previous:
                copied_test = Path(temp) / 'observer.apk'
                call('pull', previous, str(copied_test))
                assert sha(copied_test) == sha(observer), 'Different observer installed; preserve its ownership before replacing it'
            else:
                (out / 'observer-install.txt').write_bytes(call('install', '-t', str(observer), timeout=180))
                observer_installed_here = True
        report['device'] = {'sdk': shell('getprop', 'ro.build.version.sdk').strip(), 'abi': shell('getprop', 'ro.product.cpu.abi').strip(), 'page_size': shell('getconf', 'PAGESIZE').strip(), 'size': shell('wm', 'size').strip(), 'density': shell('wm', 'density').strip()}
        assert 'Override' not in report['device']['size'] and 'Override' not in report['device']['density'], 'General UI qualification must restore the normal owned display first'
        command = [adb, '-s', a.serial, 'shell', 'am', 'instrument', '-w', '-r', '-e', 'expectedVersion', a.expected_version, '-e', 'expectedCode', str(a.expected_code), '-e', 'label', a.label, '-e', 'runToken', report['run_token'], '-e', 'from', str(a.first), '-e', 'through', str(a.through), RUNNER]
        report['instrumentation_arguments'] = command[5:]
        with (out / 'instrumentation.txt').open('wb') as log:
            process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
            deadline = time.monotonic() + a.timeout
            while process.poll() is None:
                if time.monotonic() > deadline:
                    process.terminate()
                    raise TimeoutError('Host deadline reached; only native progress.json completions are evidence')
                ready, _, _ = select.select([process.stdout], [], [], 1)
                if ready:
                    chunk = os.read(process.stdout.fileno(), 16384)
                    if chunk:
                        log.write(chunk)
                        log.flush()
                        for line in chunk.decode(errors='replace').splitlines():
                            if 'PASS real U' in line or 'FAIL' in line or 'INSTRUMENTATION_RESULT' in line:
                                print(line, flush=True)
            tail = process.stdout.read()
            if tail:
                log.write(tail)
            report['instrumentation_exit_code'] = process.returncode
        call('pull', remote, str(out / 'native'), timeout=180)
        native = json.loads((out / 'native/report.json').read_text())
        assert sha(out / 'native/fixture-manifest.json') == sha(FIXTURES / 'manifest.json')
        assert native.get('runToken') == report['run_token'] and native.get('label') == a.label, 'Remote report does not belong to this exact execution'
        instrumentation_text = (out / 'instrumentation.txt').read_text(errors='replace')
        assert re.search(r'INSTRUMENTATION_CODE:\s*-1\s*$', instrumentation_text), 'Instrumentation did not finish RESULT_OK'
        assert 'INSTRUMENTATION_RESULT: failure=' not in instrumentation_text, 'Instrumentation reports a failure'
        report['native'] = native
        with tempfile.TemporaryDirectory(prefix='luma-v117-after-') as temp:
            copied = Path(temp) / 'candidate.apk'
            call('pull', installed(package), str(copied))
            report['installed_apk_sha256_after'] = sha(copied)
            assert report['installed_apk_sha256_after'] == a.sha256, 'Candidate changed during qualification'
        report['passed'] = bool(native.get('passed')) and native.get('completed') == a.through - a.first + 1 and process.returncode == 0
        levels = native.get('levels', [])
        report['timing'] = {'observed_levels': len(levels), 'observed_loop_ms': sum(item['elapsedMs'] for item in levels), 'observed_certificate_exits': sum(item['certificateExits'] for item in levels), 'note': 'Observed Android emulator replay duration with checks/captures. No unplayed level is counted; not a human difficulty, audio quality or physical-phone performance measurement.'}
    except BaseException as error:
        report['failure'] = str(error)
        (out / 'host-failure.txt').write_text(traceback.format_exc())
        try:
            if not (out / 'native').exists():
                call('pull', remote, str(out / 'native'), timeout=180)
        except BaseException:
            pass
    finally:
        if observer_installed_here:
            try:
                previous = installed(TEST_PACKAGE)
                if previous:
                    with tempfile.TemporaryDirectory(prefix='luma-v117-cleanup-') as temp:
                        copied = Path(temp) / 'observer.apk'
                        call('pull', previous, str(copied))
                        assert sha(copied) == sha(observer), 'Observer identity changed before cleanup'
                    (out / 'observer-uninstall.txt').write_bytes(call('uninstall', TEST_PACKAGE))
                    report['observer_removed'] = not bool(installed(TEST_PACKAGE))
            except BaseException as error:
                report['cleanup_failure'] = str(error)
                report['passed'] = False
        report['wall_seconds'] = round(time.monotonic() - started, 3)
        report['evidence_sha256'] = {str(path.relative_to(out)): sha(path) for path in sorted(out.rglob('*')) if path.is_file() and path.name != 'host-report.json'}
        (out / 'host-report.json').write_text(json.dumps(report, indent=2, ensure_ascii=False) + '\n')
        print(json.dumps({'passed': report['passed'], 'report': str(out / 'host-report.json'), 'completed': report.get('native', {}).get('completed'), 'wall_seconds': report['wall_seconds']}), flush=True)
    raise SystemExit(0 if report['passed'] else 1)


if __name__ == '__main__':
    main()
