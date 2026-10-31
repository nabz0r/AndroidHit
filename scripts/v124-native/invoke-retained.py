#!/usr/bin/env python3
"""Exact production B on retained task AVD; a first upgrade is explicit and preserves saves."""
from pathlib import Path
import json,subprocess,sys
ROOT=Path(__file__).resolve().parents[2]
phase=sys.argv[1]
assert phase.replace('-','').isalnum() and phase.islower()
observer=json.loads(Path('/tmp/luma124-production-observer-b2/build-receipt.json').read_text())
command=[sys.executable,str(ROOT/'scripts/v124-native/production/run.py'),'--device','retained','--manifest',str(ROOT/'releases/manifest-1.24.0-rc1.json'),'--manifest-sha256','b1539d1cea74940cf5bbb7bc43baecf3fbbf7ba2c36fd1c3508d7cb9f5047247','--candidate',str(ROOT/'releases/luma-drift-1.24.0-rc1.apk'),'--sha256','92954de5519efdb6436a2cbed42fc24a6e79cf4fdf8cf55025e2136eef0065ed','--observer',observer['apk'],'--observer-sha256',observer['sha256'],'--version','1.24.0-rc1','--code','32','--phase',phase,'--label','retained-'+phase,'--steps',str(ROOT/'docs/validation/v1.24/native/retained-steps'/f'{phase}.json')]
if len(sys.argv)>2:
    assert sys.argv[2]=='upgrade'
    command+=['--upgrade-from-sha256','429a443c9228787c7b854d36a40f0bcf7d52fdc8a3601214cd27b5cfae6945fb']
subprocess.run(command,check=True)
