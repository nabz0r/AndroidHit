#!/usr/bin/env python3
"""Rehearse exactly the workflow's sparse payload and run lines, without Git/network."""
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
WORKFLOW = ROOT / '.github/workflows/puzzle-contracts.yml'
JDK = Path('/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home')

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def write(name, value):
    path = OUT / name
    temporary = path.with_suffix(path.suffix + '.tmp')
    temporary.write_text(json.dumps(value, indent=2) + '\n')
    temporary.replace(path)

def main():
    assert not (OUT / 'launch.json').exists(), 'Preserve previous attempts'
    lines = WORKFLOW.read_text().splitlines()
    patterns = []
    sparse = False
    commands = []
    block = False
    for line in lines:
        if line.strip() == 'sparse-checkout: |':
            sparse = True
            continue
        if sparse:
            if line.startswith('            /'):
                patterns.append(line.strip()[1:])
                continue
            if line.strip():
                sparse = False
        if line.startswith('        run: '):
            value = line.split('run: ', 1)[1]
            block = value == '|'
            if not block:
                commands.append(value)
            continue
        if block:
            if line.startswith('          ') and line.strip() and not line.strip().startswith('#'):
                commands.append(line.strip())
            elif line.strip():
                block = False
    files = set()
    for pattern in patterns:
        if pattern.endswith('/'):
            files.update(p for p in (ROOT / pattern).rglob('*') if p.is_file())
        else:
            files.update(p for p in ROOT.glob(pattern) if p.is_file())
    inputs = {str(p.relative_to(ROOT)): sha(p) for p in sorted(files)}
    assert len(commands) == 23 and len(inputs) == 160
    assert not any('/identity/' in name or '/commerce/' in name or name.endswith('LumaView.java') for name in inputs)
    write('input-sha256.json', inputs)
    frozen = json.loads((ROOT / 'docs/validation/v1.17/candidate-f/source-freeze.json').read_text())
    assert all(sha(ROOT / name) == digest for name, digest in frozen.items())
    environment = dict(os.environ, JAVA_HOME=str(JDK), RUNNER_TEMP=str(OUT / 'generated'))
    Path(environment['RUNNER_TEMP']).mkdir()
    started = time.monotonic()
    record = {'candidate': 'F 1.17.0-rc4/code22',
              'production_apk_sha256': 'fe556ae6f0420f41afb7e5db044ef925fe79d0695f0ac6190d6870daf2e8d5a8',
              'workflow_sha256': sha(WORKFLOW), 'source_sha256': inputs,
              'sparse_files': len(inputs), 'command_count': len(commands),
              'java_version': subprocess.check_output([str(JDK / 'bin/java'), '-version'], stderr=subprocess.STDOUT, text=True),
              'node': shutil.which('node'), 'started_at_utc': datetime.datetime.now(datetime.timezone.utc).isoformat(),
              'scope': 'Exact local sparse-copy rehearsal of every current workflow run line. No remote Actions, Android/device/browser access, app build, network, source/test change or release acceptance.',
              'prior_14_command_135_file_proof': 'docs/validation/v1.17/ci-host-rehearsal-f/summary.json'}
    write('launch.json', record)
    results = []
    with tempfile.TemporaryDirectory(prefix='luma-ci-complete-f-') as temporary:
        snapshot = Path(temporary) / 'source'
        for name, digest in inputs.items():
            target = snapshot / name
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(ROOT / name, target)
            assert sha(target) == digest
        for index, command in enumerate(commands, 1):
            script = next((part for part in command.split() if part.startswith('scripts/')), 'web-controller')
            stem = Path(script).stem
            log = OUT / f'{index:02}-{stem}.txt'
            begin = time.monotonic()
            with log.open('w') as output:
                process = subprocess.run(command, cwd=snapshot, env=environment, shell=True,
                                         executable='/bin/sh', stdout=output, stderr=subprocess.STDOUT, timeout=600)
            result = {'command': command, 'exit_code': process.returncode,
                      'log': str(log.relative_to(ROOT)), 'sha256': sha(log),
                      'seconds': round(time.monotonic() - begin, 3)}
            results.append(result)
            write('progress.json', {'steps': results, 'completed_commands': index, 'command_count': len(commands)})
            print(json.dumps(result), flush=True)
            if process.returncode:
                break
        assert all(sha(ROOT / name) == digest for name, digest in inputs.items()), 'Workflow inputs changed during rehearsal'
        assert all(sha(ROOT / name) == digest for name, digest in frozen.items()), 'App inputs changed during rehearsal'
    record.update(passed=len(results) == len(commands) and all(r['exit_code'] == 0 for r in results),
                  steps=results, wall_seconds=round(time.monotonic() - started, 3),
                  all_161_apk_inputs_unchanged=len(frozen) == 161,
                  finished_at_utc=datetime.datetime.now(datetime.timezone.utc).isoformat())
    write('summary.json', record)
    print(json.dumps({'passed': record['passed'], 'commands': len(results), 'sparse_files': len(inputs), 'wall_seconds': record['wall_seconds']}), flush=True)
    return 0 if record['passed'] else 1

if __name__ == '__main__':
    raise SystemExit(main())
