#!/usr/bin/env python3
"""Pure host guard-contract tests with temporary JSON fixtures; no AVD or game state."""
import copy
import hashlib
import json
from pathlib import Path
import tempfile
import clone_guard as guard

def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()

def main():
    checks=0
    with tempfile.TemporaryDirectory(prefix='luma117-clone-guard-host-') as directory:
        guard.ROOT=Path(directory).resolve()
        baseline=guard.ROOT/'docs/validation/v1.17/native-interface/candidate-f-clone-baseline/host-report.json'
        baseline.parent.mkdir(parents=True)
        baseline.write_text(json.dumps({'passed':True,'serial':guard.SOURCE[0],'avd':guard.SOURCE[1],'apk_sha256':guard.APK,'phases':[{'phase':'baseline','native':{'passed':True,'checkCount':6}}]}))
        snapshot=baseline.parent/'baseline/baseline.json';snapshot.parent.mkdir()
        snapshot.write_text(json.dumps({'legacySave':{'coins':51510,'unlocked':1000,'stars_1000':3}}))
        receipt=guard.ROOT/'docs/validation/v1.17/secondary-clone/creation.json';receipt.parent.mkdir(parents=True)
        valid={'schema_version':1,'passed':True,'source_serial':guard.SOURCE[0],'source_avd':guard.SOURCE[1],'clone_serial':guard.CLONE[0],'clone_avd':guard.CLONE[1],'apk_sha256':guard.APK,'production_version':guard.VERSION,'production_code':guard.CODE,'source_stopped_before_copy':True,'copy_method':'Stopped physical QCOW2 copy','disks':[{'name':'userdata-qemu.img.qcow2','source_sha256':'a'*64,'clone_sha256':'a'*64,'bytes':12345}],'baseline':{'report':str(baseline),'pathhash':digest(baseline),'completed_level':1000,'coins':51510},'afterbootidentity':{'passed':True,'serial':guard.CLONE[0],'avd':guard.CLONE[1],'apk_sha256':guard.APK,'version':guard.VERSION,'code':guard.CODE},'scope':guard.SCOPE}
        def run(data,expected=True,receipt_digest=None):
            nonlocal checks
            receipt.write_text(json.dumps(data));checks+=1
            try:guard.validate_receipt(receipt,receipt_digest or digest(receipt),*guard.CLONE,guard.APK)
            except AssertionError:
                if expected:raise
            else:
                if not expected:raise AssertionError('Invalid provenance accepted')
        run(valid)
        mutations=[('schema_version',0),('passed',False),('source_serial','emulator-5554'),('source_avd','unrelated'),('clone_serial','emulator-5594'),('clone_avd','unrelated'),('apk_sha256','b'*64),('production_version','1.16.0'),('production_code',21),('source_stopped_before_copy',False),('copy_method',''),('disks',[]),('scope','another migration')]
        for key,value in mutations:
            bad=copy.deepcopy(valid);bad[key]=value;run(bad,False)
        for key,value in [('source_sha256','broken'),('clone_sha256','b'*64),('bytes',0),('name','cache.img')]:
            bad=copy.deepcopy(valid);bad['disks'][0][key]=value;run(bad,False)
        for key,value in [('coins',50),('completed_level',1),('pathhash','c'*64),('report',str(baseline.parent/'other.json'))]:
            bad=copy.deepcopy(valid);bad['baseline'][key]=value;run(bad,False)
        for key,value in [('passed',False),('serial','emulator-5594'),('avd','other'),('apk_sha256','b'*64),('version','old'),('code',21)]:
            bad=copy.deepcopy(valid);bad['afterbootidentity'][key]=value;run(bad,False)
        run(valid,False,'0'*64)
        for key,value in [('coins',50),('unlocked',1),('stars_1000',0)]:
            state={'coins':51510,'unlocked':1000,'stars_1000':3};state[key]=value;snapshot.write_text(json.dumps({'legacySave':state}));run(valid,False)
    print(f'PASS {checks} pure-host clone provenance checks; temporary fixtures only, no emulator or game access')
if __name__=='__main__':main()
