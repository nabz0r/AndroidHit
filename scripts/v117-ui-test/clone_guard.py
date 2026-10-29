"""Host-only validation for the one explicitly authorized stopped-disk QA clone.

This validates immutable provenance records, never emulator disks or application state.
The caller still verifies the live AVD name, installed APK bytes and native profile.
"""
from pathlib import Path
import hashlib
import json
import re

ROOT=Path(__file__).resolve().parents[2]
SOURCE=('emulator-5586','Luma-Refonte115-QA')
CLONE=('emulator-5592','Luma117-Secondary-F-QA')
VERSION='1.17.0-rc4'
CODE=22
APK='fe556ae6f0420f41afb7e5db044ef925fe79d0695f0ac6190d6870daf2e8d5a8'
SCOPE='Physical clone of task-owned retained QA disk; no preference injection; separate native interaction evidence, not a second migration qualification'

def sha(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()

def validate_receipt(path,digest,serial,avd,apk_sha256):
    assert (serial,avd)==CLONE,'Only the explicitly authorized secondary F clone is supported'
    receipt=Path(path).resolve()
    assert receipt==ROOT/'docs/validation/v1.17/secondary-clone/creation.json','Exact root-created clone receipt path required'
    assert re.fullmatch(r'[a-f0-9]{64}',digest or '') and sha(receipt)==digest,'Explicit immutable clone receipt hash differs'
    data=json.loads(receipt.read_text())
    assert data.get('schema_version')==1 and data.get('passed') is True,'Root clone creation must have completed successfully'
    assert (data.get('source_serial'),data.get('source_avd'))==SOURCE,'Clone must originate from the retained task-owned QA AVD'
    assert (data.get('clone_serial'),data.get('clone_avd'))==CLONE,'Clone identity differs'
    assert data.get('apk_sha256')==apk_sha256==APK and data.get('production_version')==VERSION and data.get('production_code')==CODE,'Exact frozen F production identity required'
    assert data.get('source_stopped_before_copy') is True and isinstance(data.get('copy_method'),str) and data['copy_method'].strip(),'Stopped physical disk copy provenance required'
    disks=data.get('disks',[])
    assert disks and any('userdata' in disk.get('name','').lower() for disk in disks),'Physical retained userdata disk must be included'
    for disk in disks:
        assert re.fullmatch(r'[a-f0-9]{64}',disk.get('source_sha256','')) and disk['source_sha256']==disk.get('clone_sha256') and disk.get('bytes',0)>0,'Source and clone disk bytes must match at the stopped copy boundary'
    baseline=data.get('baseline',{})
    report_path=Path(baseline.get('report',''))
    if not report_path.is_absolute():report_path=ROOT/report_path
    assert report_path.resolve()==ROOT/'docs/validation/v1.17/native-interface/candidate-f-clone-baseline/host-report.json','Exact final source baseline report required'
    assert baseline.get('pathhash')==sha(report_path) and baseline.get('completed_level')==1000 and baseline.get('coins')==51510,'Recorded source baseline identity differs'
    report=json.loads(report_path.read_text())
    assert report.get('passed') is True and report.get('serial')==SOURCE[0] and report.get('avd')==SOURCE[1] and report.get('apk_sha256')==APK,'Source baseline did not pass on the original exact F AVD'
    phases=report.get('phases',[])
    assert len(phases)==1 and phases[0].get('phase')=='baseline' and phases[0]['native'].get('passed') is True and phases[0]['native'].get('checkCount')==6,'Read-only six-check source baseline required'
    snapshot=report_path.parent/'baseline/baseline.json';state=json.loads(snapshot.read_text())['legacySave']
    assert state.get('coins')==51510 and state.get('unlocked',0)>=1000 and state.get('stars_1000',0)>0,'Source baseline must retain its genuine completed U1000 profile'
    boot=data.get('afterbootidentity',{})
    assert boot.get('passed') is True and boot.get('serial')==CLONE[0] and boot.get('avd')==CLONE[1] and boot.get('apk_sha256')==APK and boot.get('version')==VERSION and boot.get('code')==CODE,'Root must verify exact clone identity after boot'
    accepted_scopes={SCOPE,SCOPE+'. Final passed state additionally requires observed clone boot identity and exact installed APK metadata.'}
    assert data.get('scope') in accepted_scopes,'Clone scope must explicitly exclude a second migration qualification'
    return {'scope':SCOPE,'receipt_path':str(receipt),'receipt_sha256':digest,'source_serial':SOURCE[0],'source_avd':SOURCE[1],'clone_serial':CLONE[0],'clone_avd':CLONE[1],'source_baseline_report_sha256':sha(report_path),'source_baseline_snapshot_sha256':sha(snapshot),'physical_copy_disks':len(disks)}
