#!/usr/bin/env python3
"""Exact final production D on retained task AVD; a first upgrade is explicit and preserves saves."""
from pathlib import Path
import json,subprocess,sys
ROOT=Path(__file__).resolve().parents[2]
phase=sys.argv[1]
assert phase.replace('-','').isalnum() and phase.islower()
observer=json.loads(Path('/tmp/luma124-production-final-observer-d/build-receipt.json').read_text())
command=[sys.executable,str(ROOT/'scripts/v124-native/production-final/run.py'),'--device','retained','--manifest',str(ROOT/'releases/manifest-1.24.0-rc2.json'),'--manifest-sha256','0d2325d2081f69f84dd25183262978bd665d374b22444ecd1623f2df4eeaabe2','--candidate',str(ROOT/'releases/luma-drift-1.24.0-rc2.apk'),'--sha256','2cb544e6ee341d346e7b6c90a75c46c3b7fa909ce236b5048677476920e7f67e','--observer',observer['apk'],'--observer-sha256',observer['sha256'],'--version','1.24.0-rc2','--code','33','--phase',phase,'--label','retained-'+phase,'--steps',str(ROOT/'docs/validation/v1.24/native/retained-steps'/f'{phase}.json')]
if len(sys.argv)>2:
    assert sys.argv[2]=='upgrade'
    command+=['--upgrade-from-sha256','92954de5519efdb6436a2cbed42fc24a6e79cf4fdf8cf55025e2136eef0065ed']
subprocess.run(command,check=True)
