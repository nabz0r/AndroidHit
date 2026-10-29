#!/usr/bin/env python3
"""Independent final evidence index. Reads finalized reports; never operates a device."""
from pathlib import Path
import datetime, hashlib, json, subprocess, sys

ROOT=Path('/Users/n/androidhit')
BASE=ROOT/'docs/validation/v1.17'
APK='15546854a856ecc5052aa79acdd49724439b2082fd638da68cf7747be1924a47'
FREEZE='b6af495fadab8c20cf6379b591297da38e10edb4b694aad0adcf521f75711b88'
VERSION='1.17.0-rc6'

def sha(path): return hashlib.sha256(path.read_bytes()).hexdigest()
def read(path): return json.loads(path.read_text())
def relative(path): return str(path.relative_to(ROOT))

def main():
    frozen_path=BASE/'candidate-h/source-freeze.json'
    assert sha(frozen_path)==FREEZE
    frozen=read(frozen_path)
    assert len(frozen)==162
    for name,digest in frozen.items(): assert sha(ROOT/name)==digest, name
    candidate=read(BASE/'candidate-h/candidate.json')
    artifacts=[]
    for entry in candidate['artifacts']:
        path=Path(entry['path'])
        assert sha(path)==entry['sha256'] and path.stat().st_size==entry['bytes'],str(path)
        canonical=ROOT/'releases'/path.name
        assert canonical.is_file() and sha(canonical)==entry['sha256'], 'Canonical package must equal audited H: '+path.name
        artifacts.append({k:v for k,v in entry.items() if k!='path'}|{'name':path.name})
    for version,channel,package in [(VERSION,'production','com.androidhit.lumadrift'),('1.17.0-sandbox-rc6','lab','com.androidhit.lumadrift.sandbox')]:
        manifest=read(ROOT/'releases'/('manifest-'+version+'.json'))
        assert manifest['version']==version and manifest['version_code']==24 and manifest['channel']==channel and manifest['application_id']==package
        assert all(manifest['source_inputs_sha256'][name]==digest for name,digest in frozen.items())
        assert manifest['configuration']['force_test_ads'] and not manifest['configuration']['paid_cosmetic_draws_enabled'] and not manifest['configuration']['wallet_api_configured']
        for entry in artifacts:
            if entry['name'].startswith('luma-drift-'+version+'.'):
                assert manifest['artifacts'][entry['name']]=={'bytes':entry['bytes'],'sha256':entry['sha256']}
    host=read(BASE/'ci-host-rehearsal-h-complete/summary.json')
    assert host['passed'] and host['command_count']==24 and host['sparse_files']==163
    for name,digest in host['source_sha256'].items(): assert sha(ROOT/name)==digest,name
    for step in host['steps']:
        assert step['exit_code']==0
    labels=[
      'candidate-h-main-upgrade-a','candidate-h-upgrade',
      'candidate-h-modes-normal-v2','candidate-h-modes-compact-largefont-v2',
      'candidate-h-sixlangs-normal','candidate-h-sixlangs-compact',
      'candidate-h-root-compact','candidate-h-root-compact-font13',
      'candidate-h-language-compact','candidate-h-owned-rigs',
      'candidate-h-transitions','candidate-h-performance',
      'candidate-h-secondary-compact','candidate-h-secondary-normal',
      'candidate-h-final-baseline',
    ]
    records=[]; tokens=set(); verified_pngs=0
    for label in labels:
        path=BASE/'native-interface'/label/'host-report.json'
        report=read(path)
        assert report['passed'] and report['apk_sha256']==APK,label
        assert report['expected_version']==VERSION and report['expected_code']==24,label
        assert report['exact_candidate_retained'] and report['owned_qa_profile_retained'],label
        phases=[]
        for phase in report['phases']:
            native=phase['native'];token=phase['host_run_token']
            assert native['passed'] and native['runToken']==token and native['phase']==phase['phase'],label
            assert token not in tokens,(label,token)
            tokens.add(token)
            assert all(check['passed'] for check in native['checks']),label
            assert native['checkCount']==len(native['checks']),label
            # Retained upgrade includes one explicitly identified G pre-upgrade baseline.
            code=phase['expected_code']
            assert code==24 or (phase['phase']=='baseline' and label in labels[:2] and code==23),label
            phases.append({'phase':phase['phase'],'token':token,'code':code,'checks':native['checkCount']})
        pngs=report['screenshots']
        actual_capture_keys={phase['phase']+'/'+capture+'.png' for phase in report['phases'] for capture in phase['native']['captures']}
        assert set(pngs)==actual_capture_keys,(label,'Only fresh tokenized native captures qualify')
        for name,digest in pngs.items():
            file=ROOT/'docs/screenshots/v1.17/native-interface'/label/name
            assert sha(file)==digest,str(file)
            assert file.read_bytes()[:8]==b'\x89PNG\r\n\x1a\n',str(file)
        verified_pngs+=len(pngs)
        records.append({'path':relative(path),'sha256':sha(path),'phases':phases,
                        'indexed_original_pngs':len(pngs),'compact':report['compact'],
                        'font_scale_before':report['font_scale'],
                        'font_scale_during':report.get('font_scale_during',float(report['font_scale'])),
                        'font_scale_after':report['font_scale_after'],
                        'configured_secondary_languages':report['secondary_languages'],
                        'scope':report['scope']})
    controller_path=BASE/'native-interface/candidate-h-controller/status.json'
    controller=read(controller_path)
    assert controller['passed'] and len(controller['completed'])==len(controller['plan'])==12
    for row in controller['completed']:
        assert sha(ROOT/row['report'])==row['sha256'],row['label']
    fresh_path=BASE/'fresh-h/summary-h.json';fresh=read(fresh_path)
    assert fresh['passed'] and fresh['apk_sha256']==APK and fresh['native_checks']==133
    assert fresh['campaign_levels']==list(range(1,9)) and fresh['actual_first_win_coins']==440
    for entry in fresh['reports']:
        path=ROOT/entry['path'];assert sha(path)==entry['sha256']
        report=read(path);assert report['passed'] and report['apk_sha256']==APK
        assert report['native']['runToken']==report['run_token']
        assert all(c['passed'] for c in report['native']['checks'])
        for name,digest in report['evidence_sha256'].items():
            if name.endswith('.apk'):continue # Observer APKs remain local, deliberately not in public Git.
            assert sha(path.parent/name)==digest,str(path.parent/name)
    audio_path=BASE/'audio/native/candidate-h-main-audio-b/host-report.json';audio=read(audio_path)
    assert audio['passed'] and audio['apk_sha256']==APK
    assert audio['native']['runToken']==audio['run_token']
    assert all(c['passed'] for c in audio['native']['checks'])
    assert audio['protected_state_byte_identical'] and audio['exact_candidate_retained']
    for name,digest in audio['evidence_sha256'].items():
        if name.endswith('.apk'):continue
        assert sha(audio_path.parent/name)==digest,str(audio_path.parent/name)
    gallery_path=BASE/'art/native/candidate-h-art-gallery/host-report.json';gallery=read(gallery_path)
    assert gallery['passed'] and gallery['apk_sha256']==APK
    gallery_native=read(gallery_path.parent/gallery['native_report'])
    assert gallery_native['passed'] and gallery_native['runToken']==gallery['run_token'] and gallery_native['version']==VERSION and gallery_native['code']==24
    assert all(check['passed'] for check in gallery_native['checks'])
    assert (gallery['composites'],gallery['garment_previews'],gallery['style_samples'],gallery['images'])==(144,24,24,50)
    for name,digest in gallery['evidence_sha256'].items():
        if name.endswith('.apk'):continue
        assert sha(gallery_path.parent/name)==digest,str(gallery_path.parent/name)
    relay_path=BASE/'relay/summary.json';relay=read(relay_path)
    assert relay['passed'] and relay['candidate_apk_sha256']==APK and relay['code']==24
    assert relay['levels']==1000 and relay['new_first_win_coins']==0
    for entry in relay['reports']: assert sha(ROOT/entry['path'])==entry['sha256']
    main_handoff=read(BASE/'relay/active-handoff.json')
    assert main_handoff['watcher_finished'] and main_handoff['stopped_normally'] and main_handoff['userdata_preserved']
    shutdown_path=ROOT/main_handoff['shutdown_receipt'];shutdown=read(shutdown_path)
    assert sha(shutdown_path)==main_handoff['shutdown_receipt_sha256']
    assert shutdown['passed'] and shutdown['full1000'] and shutdown['apk_sha256']==APK
    assert shutdown['protected_state_byte_identical'] and shutdown['source_inputs_stable'] and shutdown['own_observer_removed_by_wrapper']
    fresh_shutdown=read(BASE/'fresh-h/device/shutdown.json')
    assert fresh_shutdown['stopped_normally'] and fresh_shutdown['userdata_preserved'] and fresh_shutdown['observer_removed'] and fresh_shutdown['all_four_candidate_packages_unchanged']
    clone_shutdown_path=BASE/'secondary-clone/final-h-shutdown.json'
    clone_shutdown=read(clone_shutdown_path)
    assert clone_shutdown['passed'] and clone_shutdown['apk_sha256']==APK and clone_shutdown['code']==24
    assert clone_shutdown['version']==VERSION and clone_shutdown['source_freeze_sha256']==FREEZE
    assert clone_shutdown['all_legacy_values_identical_to_pre_h_g_baseline'] and clone_shutdown['source_inputs_verified']==162
    assert clone_shutdown['four_frozen_binary_hashes_verified'] and clone_shutdown['settings_restored']
    assert clone_shutdown['ui_and_art_observers_absent'] and clone_shutdown['normal_emulator_disconnected'] and clone_shutdown['userdata_retained']
    review_path=BASE/'review/visual-candidate-h.json';review=read(review_path)
    assert review['apk_sha256']==APK
    for row in review['images']:assert sha(ROOT/row['path'])==row['sha256']
    proof_paths=[BASE/'candidate-h/candidate.json',BASE/'candidate-h/binaries/binary-final-candidate.json',
                 BASE/'ci-host-rehearsal-h-complete/summary.json',controller_path,
                 BASE/'native-interface/candidate-h-modes-v2-acceptance.json',BASE/'native-interface/candidate-h-clone-acceptance.json',fresh_path,audio_path,
                 gallery_path,relay_path,shutdown_path,clone_shutdown_path,BASE/'fresh-h/device/shutdown.json',review_path,BASE/'review/motion-h/review.json',
                 BASE/'review/audio-native-final-h.json']
    result={'schema_version':1,'passed':True,'recorded_at_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),
            'audit_source_sha256':sha(Path(__file__)),
            'candidate':'H','version':VERSION,'code':24,'apk_sha256':APK,
            'app_source_commit':candidate['source_commit'],'source_freeze_sha256':FREEZE,
            'frozen_app_config_inputs':len(frozen),'four_packages':artifacts,
            'interface_reports':records,'indexed_interface_original_pngs_hash_verified':verified_pngs,
            'other_evidence':{relative(p):sha(p) for p in proof_paths},
            'fresh_install':{'real_first_wins':'U1-U8','checks':133,'coins':440,'draws':'Buzz + Aurora dream'},
            'retained_replay':{'levels':1000,'duplicate_first_win_coins':0},
            'scope':'Independent local read-only hash/token/index audit of finalized H evidence. No native input, new builds, screenshot editing, preference writes or public publication inferred. Check counts are kept per scope; a G pre-upgrade baseline is explicitly code23.',
            'limits':['Retained replay is not 1000 first wins on a fresh install.',
                      'Primary6languages in2sizes; secondary6compact and normalEN/FR.',
                      'Gallery144composites/24garments/24styles is isolated rendering, separate from ownership.',
                      'Digital audio and emulator capture do not establish physical listening, external focus competition or phone60fps.',
                      'Human retention and commercial live services are not validated.']}
    target=BASE/'acceptance-h.json'
    target.write_text(json.dumps(result,indent=2)+'\n')
    print(json.dumps({'passed':True,'interface_reports':len(records),'original_interface_PNGs':verified_pngs,'evidence':relative(target)}))

if __name__=='__main__':main()
