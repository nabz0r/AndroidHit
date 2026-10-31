#!/usr/bin/env python3
from pathlib import Path
import json,subprocess,sys
root=Path(__file__).resolve().parents[2]
phase=sys.argv[1]
assert phase.replace('-','').isalnum() and phase.islower()
b=json.loads(Path('/tmp/luma-playtest-20261031-production-observer/build-receipt.json').read_text())
subprocess.run([sys.executable,str(Path(__file__).with_name('run.py')),'--device','retained','--candidate',str(root/'releases/luma-drift-1.23.0-rc2.apk'),'--sha256','429a443c9228787c7b854d36a40f0bcf7d52fdc8a3601214cd27b5cfae6945fb','--observer',b['apk'],'--observer-sha256',b['sha256'],'--version','1.23.0-rc2','--code','31','--phase',phase,'--label',phase,'--steps',str(root/'docs/validation/playtest-2026-10-31/native-production-steps'/f'{phase}.json')],check=True)
