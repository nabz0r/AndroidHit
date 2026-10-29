#!/usr/bin/env python3
"""Execute unmodified existing legacy contracts against an exact F source copy."""
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
import datetime
import hashlib
import json
import os
import shutil
import subprocess
import tempfile
import time

ROOT = Path(__file__).resolve().parents[4]
OUT = Path(__file__).resolve().parent
FREEZE = ROOT / 'docs/validation/v1.17/candidate-f/source-freeze.json'
APK = 'fe556ae6f0420f41afb7e5db044ef925fe79d0695f0ac6190d6870daf2e8d5a8'
JDK = Path('/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home')
SUITES = [
    ('strategy', 'scripts/test-strategy-v4.sh', 'strategy'),
    ('guardians', 'scripts/test-guardian-v19.sh', 'guardians'),
    ('engine', 'scripts/test-engine.sh', None),
    ('sealed', 'scripts/test-sealed.sh', None),
    ('progression-v3', 'scripts/test-progression-v3.sh', 'progression-v3'),
    ('move-budget', 'scripts/test-move-budget.sh', None),
    ('guardian-store', 'scripts/test-guardian-store-v19.sh', 'guardian-store'),
    ('data', 'scripts/test-data.sh', None),
    ('collection-events', 'scripts/test-collection-events.sh', None),
]
TESTS = [
    'EngineTest', 'SealedTest', 'ProgressionV3Test', 'ProgressionAnalysis',
    'GenerateProgressionV3', 'LegacyContentFingerprint', 'StrategyV4Audit',
    'StrategyV4ContractTest', 'GuardianV19Test', 'RewardsCosmeticsV17Test',
    'GuardianStoreV19Test', 'MoveBudgetTest', 'DataTest', 'CollectionEventsTest',
]

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def write(name, value):
    path = OUT / name
    temporary = path.with_suffix(path.suffix + '.tmp')
    temporary.write_text(json.dumps(value, indent=2) + '\n')
    temporary.replace(path)

def main():
    assert not (OUT / 'launch.json').exists(), 'Preserve prior execution evidence'
    freeze = json.loads(FREEZE.read_text())
    assert freeze and all(sha(ROOT / name) == digest for name, digest in freeze.items())
    names = {str(p.relative_to(ROOT)) for folder in ('engine', 'data', 'onboarding')
             for p in (ROOT / 'app/src/main/java/com/androidhit/lumadrift' / folder).glob('*.java')}
    names.update('scripts/' + test + '.java' for test in TESTS)
    names.update(suite[1] for suite in SUITES)
    names.update(str(p.relative_to(ROOT)) for p in (ROOT / 'scripts/test-support').rglob('*.java'))
    names.add('docs/validation/v1.6/legacy-content-before.sha256')
    inputs = {name: sha(ROOT / name) for name in sorted(names)}
    write('input-sha256.json', inputs)
    version = subprocess.check_output([str(JDK / 'bin/java'), '-version'], stderr=subprocess.STDOUT, text=True)
    historical_before = {str(p.relative_to(ROOT)): sha(p) for version in ('v1.5', 'v1.6', 'v1.8', 'v1.9', 'v1.11', 'v1.14', 'v1.15')
                         for p in (ROOT / 'docs/validation' / version).rglob('*') if p.is_file()}
    started = time.monotonic()
    with tempfile.TemporaryDirectory(prefix='luma-legacy-host-f-') as temporary:
        snapshot = Path(temporary) / 'source'
        for name, digest in inputs.items():
            target = snapshot / name
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(ROOT / name, target)
            assert sha(target) == digest
        record = {'candidate': 'F 1.17.0-rc4/code22', 'production_apk_sha256': APK,
                  'source_freeze': str(FREEZE.relative_to(ROOT)), 'source_freeze_sha256': sha(FREEZE),
                  'all_frozen_inputs_verified_before': len(freeze),
                  'execution_source': str(snapshot), 'method': 'Unmodified existing suites and byte-identical application sources in an isolated copy; historical progression-script side effects stay in that disposable copy.',
                  'java_version': version, 'started_at_utc': datetime.datetime.now(datetime.timezone.utc).isoformat(),
                  'scope': 'Host models only. No APK build, Android/device/browser interaction, new tests or application source edit.'}
        write('launch.json', record)
        environment = dict(os.environ, JAVA_HOME=str(JDK))
        def run(suite):
            name, script, subfolder = suite
            command = ['bash', script]
            if subfolder:
                command.append(str(OUT / subfolder))
            log = OUT / (name + '.log')
            begin = time.monotonic()
            with log.open('w') as output:
                process = subprocess.run(command, cwd=snapshot, env=environment, stdout=output,
                                         stderr=subprocess.STDOUT, timeout=600)
            result = {'name': name, 'command': command, 'cwd': str(snapshot), 'exit_code': process.returncode,
                      'seconds': round(time.monotonic() - begin, 3), 'log': str(log.relative_to(ROOT)),
                      'log_sha256': sha(log)}
            write(name + '-execution.json', result)
            print(json.dumps(result), flush=True)
            return result
        with ThreadPoolExecutor(max_workers=2) as pool:
            results = list(pool.map(run, SUITES))
        assert all(sha(ROOT / name) == digest for name, digest in freeze.items()), 'Final app inputs changed'
        assert all(sha(ROOT / name) == digest for name, digest in inputs.items()), 'Suite inputs changed'
        assert all((ROOT / name).is_file() and sha(ROOT / name) == digest for name, digest in historical_before.items()), 'Historical evidence changed'
        record.update(passed=all(r['exit_code'] == 0 for r in results), suites=len(results), results=results,
                      all_frozen_inputs_verified_after=len(freeze), historical_files_unchanged=len(historical_before),
                      elapsed_seconds=round(time.monotonic() - started, 3))
        write('execution-summary.json', record)
        print(json.dumps({'passed': record['passed'], 'suites': len(results), 'elapsed_seconds': record['elapsed_seconds']}), flush=True)
        return 0 if record['passed'] else 1

if __name__ == '__main__':
    raise SystemExit(main())
