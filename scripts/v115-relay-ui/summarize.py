#!/usr/bin/env python3
"""Combine completed native batches without turning planned/unplayed levels into evidence."""
from pathlib import Path
import argparse,hashlib,json

ROOT=Path(__file__).resolve().parents[2]
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--pilot',default='docs/validation/v1.15/relay-native-e-pilot100/host-report.json')
    parser.add_argument('--continuation',default='docs/validation/v1.15/relay-native-e-continue1000/host-report.json')
    parser.add_argument('--output',default='docs/validation/v1.15/relay-native-summary.json')
    args=parser.parse_args();paths=[ROOT/args.pilot,ROOT/args.continuation]
    reports=[json.loads(path.read_text()) for path in paths]
    assert all(report.get('passed') and report['native']['passed'] for report in reports),'Every required native batch must pass'
    assert len({report['apk_sha256'] for report in reports})==1,'Candidate APK must be identical across batches'
    assert all(report['installed_apk_sha256']==report['apk_sha256'] for report in reports),'Installed APK bytes must match frozen candidate'
    assert all(report['fixture_manifest_sha256']==reports[0]['fixture_manifest_sha256'] for report in reports),'Frozen certificates must be identical across observers'
    for path,report in zip(paths,reports):
        events=[json.loads(line) for line in (path.parent/'native/events.jsonl').read_text().splitlines() if line]
        touches=[event for event in events if event.get('input')=='UiAutomation touchscreen']
        assert len(touches)==report['native']['touchCount'],'Every reported physical gesture must have its native journal event'
        assert [event['touch'] for event in touches]==list(range(1,len(touches)+1)),'Gesture journal must be continuous'
        assert all(event['bounds'][2]>event['bounds'][0] and event['bounds'][3]>event['bounds'][1] for event in touches),'Gesture bounds must be nonempty observed targets'
    played=[level for report in reports for level in report['native']['levels']]
    assert [level['number'] for level in played]==list(range(1,1001)),'Each U level must be physically played once, without a gap or overlap'
    for level in played:
        fixture=json.loads((ROOT/('scripts/v115-relay-ui/assets/fixtures/u'+str(level['number'])+'.json')).read_text())
        assert level['history']==','.join(str(step['action']) for step in fixture['steps']),'Actual persisted history must equal that board certificate'
        assert level['certificateExits']==fixture['pieces'] and level['initialStars']==0 and level['stars']==3,'Every completion must be a genuine first win'
        assert level['coinDelta']==55,'Every first three-star win must pay its actual 55-coin reward'
    captures=[str(path.parent/'native'/(name+'.png')) for path,report in zip(paths,reports) for name in report['native']['captures']]
    assert all(Path(path).is_file() for path in captures),'Every claimed native screenshot must exist'
    summary={'passed':True,'candidate_apk_sha256':reports[0]['apk_sha256'],'fixture_manifest_sha256':reports[0]['fixture_manifest_sha256'],'levels':1000,'actual_certificate_exits':sum(level['certificateExits'] for level in played),'extra_probe_exits':sum(level.get('extraProbeExits',0) for level in played),'native_checks':sum(report['native']['checkCount'] for report in reports),'actual_touch_gestures':sum(report['native']['touchCount'] for report in reports),'native_original_pngs':len(captures),'earned_first_win_coins':sum(level['coinDelta'] for level in played),'observer_versions':[{'report':str(path.relative_to(ROOT)),'report_sha256':digest(path),'apk_sha256':report['test_apk_sha256'],'runner_source_sha256':report['runner_sha256'],'completed':report['native']['completed'],'wall_seconds':report['wall_seconds'],'timing':report.get('timing',{})} for path,report in zip(paths,reports)],'scope':'Exactly U1–U1000 completed by Android SDK touchscreen actions and real Next controls on the exact signed candidate; read-only target preferences; no progression, history or reward injection. Distinct host model solvability evidence is separate. The two observer timing scopes cover different boards, and continuation shares the development host with another owned QA emulator. These are QA durations, not a controlled app-performance or physical-phone benchmark. Human enjoyment/difficulty remain unmeasured.'}
    output=ROOT/args.output;output.write_text(json.dumps(summary,indent=2)+'\n');print(json.dumps(summary,indent=2))
if __name__=='__main__':main()
