#!/usr/bin/env python3
"""Reject the actual stale rig failure and wrong phase/token without device access."""
from pathlib import Path
import importlib.util,json
HERE=Path(__file__).resolve().parent
spec=importlib.util.spec_from_file_location('interface_host',HERE/'run.py');host=importlib.util.module_from_spec(spec);spec.loader.exec_module(host)
checks=0
def case(output,native,phase,token,accept):
 global checks
 checks+=1
 try:host.validate_native_execution(output,native,phase,token)
 except AssertionError:
  if accept:raise
 else:
  if not accept:raise AssertionError('Stale or incomplete execution accepted')
ok=b'INSTRUMENTATION_CODE: -1\n';token='01234567-89ab-cdef-0123-456789abcdef';proof={'phase':'rig','runToken':token}
case(ok,proof,'rig',token,True)
case(b'Activity manager help\n',proof,'rig',token,False)
case(b'INSTRUMENTATION_FAILED\n'+ok,proof,'rig',token,False)
case(ok,{'phase':'modes','runToken':token},'rig',token,False)
case(ok,{'phase':'rig'},'rig',token,False)
case(ok,{'phase':'rig','runToken':'previous'},'rig',token,False)
raw=HERE.parents[1]/'docs/validation/v1.17/native-interface/candidate-g-owned-rigs/rig'
if raw.is_dir():case((raw/'instrumentation.txt').read_bytes(),json.loads((raw/'report.json').read_text()),'rig',token,False)
print(f'PASS {checks} execution provenance guards; includes actual preserved Activity Manager/stale-report regression when available; no device access')
